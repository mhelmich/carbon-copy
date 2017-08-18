/*
 *
 *  Copyright 2017 Marco Helmich
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.carbon.copy.data.structures;

import co.paralleluniverse.galaxy.Store;
import co.paralleluniverse.galaxy.StoreTransaction;
import com.google.common.util.concurrent.ListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.carbon.copy.data.structures.DataStructure.TIMEOUT_SECS;

public class Txn {
    private static Logger logger = LoggerFactory.getLogger(Txn.class);

    private final Store store;
    // the TxnManagerImpl is the place where local locking happens and multiple threads coordinate
    // I'm explicitly touching the implementing class (as opposed to the interface)
    // what looks like a breach of abstraction enables me to not expose the "internal" methods in the interface
    private final TxnManagerImpl txnManager;
    // any data structures in this set will be upserted
    // this obviously doesn't work well when data is deleted ;)
    private final Set<DataStructure> changedObjects = new HashSet<>();
    // all data structures in this set will be deleted
    private final Set<DataStructure> deletedObjects = new HashSet<>();
    // all data structures in this set have been newly created in this transaction
    // keeping track of this set of data structures is not crazy interesting for a successful commit
    // but is interesting in case we need to roll back a transaction ... in that case we have to delete
    // all the data structures in this set
    private final Set<DataStructure> createdObjects = new HashSet<>();
    // this boolean indicates whether this transaction has been committed or rolled back on
    // you can't reuse a Txn object ever!
    private boolean txnEnded = false;

    Txn(Store store, TxnManagerImpl txnManager) {
        this.store = store;
        this.txnManager = txnManager;
    }

    // As opposed to the rest of the application this class (as well as the TxnManager internally) is
    // well aware of what cache lines are and galaxy primitives -- this class only thinks in terms of ids.
    // Assumptions for things that happened already before entering this method
    //   - remote and local locks for all blocks in this transaction have been acquired and are still being held
    // What we do from then on is:
    // 1. double-check we have the exclusive Galaxy lock for all cache lines necessary
    //    - call asyncGetX on all of the cache lines that have changed or will be deleted
    // 2. make all changes to the cache lines that have changed
    // 3. call delete on all cache lines that have been deleted
    // 4. wait for all async calls to finish
    //    - if successful, just proceed
    //    - if unsuccessful, roll back all other changes
    //      - this might include deleting all newly created cache lines
    // 5. release the exclusive, node-internal transaction lock
    public void commit() throws IOException {
        if (txnEnded) {
            throw new IOException("Txn ended already! You can't reuse a Txn ever!");
        }

        try {
            // in a first step we try to acquire locks for all objects in our transaction
            doubleCheckAllBlocksArePinned();

            // this is a very naive implementation for now
            // all the work is happening at commit time
            // this is pretty pessimistic
            // it might be better to do this as early as
            // when we are getting the lock on the object
            List<ListenableFuture> writingFutures = new LinkedList<>();

            writingFutures.addAll(
                    changedObjects.stream()
                            .filter(ds -> !deletedObjects.contains(ds))
                            .map(ds -> ds.asyncUpsert(ds, this))
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList())
            );

            writingFutures.addAll(
                    deletedObjects.stream()
                            .map(ds -> ds.asyncDelete(ds, this))
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList())
            );

            writingFutures.forEach(f -> wait(f, TIMEOUT_SECS));

        } catch (Exception xcp) {
            logger.error("Committing txn failed with {} {} {}", changedObjects, createdObjects, deletedObjects);
            throw new IOException(xcp);
        } finally {
            releaseAllTheBlocksYouHave();
            txnEnded = true;
        }
    }

    private void doubleCheckAllBlocksArePinned() {
        List<ListenableFuture> lockingFutures = new LinkedList<>();

        lockingFutures.addAll(
                changedObjects.stream()
                        .filter(ds -> !deletedObjects.contains(ds))
                        .sorted(Comparator.comparingLong(DataStructure::getId))
                        .map(ds -> asyncLockBlocks(ds.getId()))
                        .collect(Collectors.toList())
        );

        lockingFutures.addAll(
                deletedObjects.stream()
                        .sorted(Comparator.comparingLong(DataStructure::getId))
                        .map(ds -> asyncLockBlocks(ds.getId()))
                        .collect(Collectors.toList())
        );

        lockingFutures.forEach(f -> wait(f, TIMEOUT_SECS));
    }

    private void wait(ListenableFuture f, int timeoutSecs) {
        try {
            f.get(timeoutSecs, TimeUnit.SECONDS);
        } catch (Exception xcp) {
            throw new RuntimeException(xcp);
        }
    }

    // undo (delete) newly created blocks
    // just toss away all other changes
    public void rollback() throws IOException {
        if (txnEnded) {
            throw new IOException("Txn ended already! You can't reuse a Txn ever!");
        }

        try {
            // delete blocks of which we know that they have been created as part of this transaction
            List<ListenableFuture> f1 = createdObjects.stream()
                    .map(ds -> ds.asyncDelete(ds, this))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            f1.forEach(f -> wait(f, TIMEOUT_SECS));
        } catch (Exception xcp) {
            throw new IOException(xcp);
        } finally {
            releaseAllTheBlocksYouHave();
            txnEnded = true;
        }
    }

    private ListenableFuture<byte[]> asyncLockBlocks(long blockId) {
        return store.getxAsync(blockId, null);
    }

    // This method is being kept in for historical purposes.
    // It used to necessary when carbon copy was relying on galaxy transactions.
    // These days carbon copy implements its own transactions
    // and strictly speaking this is not necessary anymore.
    // This method though forces developers of data structures to pass the carbon copy Txn
    // into all major DataStructure methods that have to do with serialization.
    // And that a) feels good and b) might come in handy in the future.
    // That's why I'm keeping this method around ... for now.
    StoreTransaction getStoreTransaction() {
        return null;
    }

    // This method needs to be called by data structures so that the transaction knows
    // that this data structure needs to be serialized and updated
    void addToChangedObjects(DataStructure ds) {
        deletedObjects.remove(ds);
        txnManager.lock(ds.getId());
        changedObjects.add(ds);
    }

    // This method needs to be called by data structures so that the transaction knows
    // that this data structure will be deleted
    void addToDeletedObjects(DataStructure ds) {
        changedObjects.remove(ds);
        txnManager.lock(ds.getId());
        deletedObjects.add(ds);
    }

    // This method needs to be called by data structures so that the transaction knows
    // that this data structure is newly created just now.
    // This is mostly interesting for the rollback case.
    void addToCreatedObjects(DataStructure ds) {
        txnManager.lock(ds.getId());
        createdObjects.add(ds);
    }

    private void releaseAllTheBlocksYouHave() {
        // dedup all the ids
        Set<Long> allTheLockedIds = new HashSet<>();
        allTheLockedIds.addAll(
                changedObjects.stream()
                        .map(DataStructure::getId)
                        .collect(Collectors.toSet())
        );
        allTheLockedIds.addAll(
                deletedObjects.stream()
                        .map(DataStructure::getId)
                        .collect(Collectors.toSet())
        );
        allTheLockedIds.addAll(
                createdObjects.stream()
                        .map(DataStructure::getId)
                        .collect(Collectors.toSet())
        );

        try {
            // this calls into the TxnManager to release blocks from this transaction
            // we might still choose to hold on to the remote lock and pass the local lock
            // to a different thread
            allTheLockedIds.forEach(txnManager::release);
        } finally {
            changedObjects.clear();
            deletedObjects.clear();
            createdObjects.clear();
        }
    }

    @Override
    public String toString() {
        return "ended? " + txnEnded + " - " + changedObjects + " - " + createdObjects + " - " + deletedObjects;
    }
}
