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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Txn {
    private static Logger logger = LoggerFactory.getLogger(Txn.class);

    private final Store store;
    private final StoreTransaction stxn;
    private final Set<DataStructure> changedObjects = new HashSet<>();

    Txn(Store store) {
        this(store, store.beginTransaction());
    }

    Txn(Store store, StoreTransaction stxn) {
        this.store = store;
        this.stxn = stxn;
    }

    public void commit() throws IOException {
        try {
            // this is a very naive implementation for now
            // all the work is happening at commit time
            // this is pretty pessimistic
            // it might be better to do this as early as
            // when we are getting the lock on the object
            changedObjects.forEach(ds -> ds.asyncUpsert(ds, this));
            store.commit(stxn);
        } catch (Exception xcp) {
            logger.error("Committing txn failed with {}", changedObjects);
            throw new IOException(xcp);
        } finally {
            changedObjects.clear();
        }
    }

    public void abort() throws IOException {
        try {
            store.abort(stxn);
        } catch (Exception xcp) {
            throw new IOException(xcp);
        } finally {
            changedObjects.clear();
        }
    }

    public void rollback() {
        store.rollback(stxn);
    }

    StoreTransaction getStoreTransaction() {
        return stxn;
    }

    void addToChangedObjects(DataStructure ds) {
        changedObjects.add(ds);
    }

    void removeFromChangedObjects(DataStructure ds) {
        changedObjects.remove(ds);
    }
}
