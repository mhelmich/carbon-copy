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

package org.distbc.data.structures;

import co.paralleluniverse.galaxy.Store;
import co.paralleluniverse.galaxy.StoreTransaction;
import co.paralleluniverse.galaxy.TimeoutException;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import com.google.inject.Inject;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * This guy keeps track of everything inside dist-bc (all top-level objects anyways).
 * It holds a map of all top-level objects to their roots and takes care of
 * all sorts of accounting tasks (such as mapping indexes to tables, etc.)
 * It also has convenience methods to load tables and indexes.
 *
 * The structure of the CATALOG_ROOT byte[].
 *   | Index |  Usage                            | Date Type |
 *   +-------+-----------------------------------+-----------+
 *   |   0   | id for names to ids               | long      |
 *   |   1   | id for index names to table names | long      |
 */
class CatalogImpl implements Catalog {
    private static final String CATALOG_ROOT_NAME = "CATALOG_ROOT";

    private final Store store;
    private final InternalDataStructureFactory dsFactory;
    private final TxnManager txnManager;

    // the id of the hash containing the names of all
    // high-level objects
    private Long catalogRootId;
    // this map contains all high-level objects
    // it maps their names to the galaxy ids that are their roots
    private Long namesToIdsId;
    private ChainingHash<String, Long> namesToIds;
    // this map contains the names of all tables and which indexes "they own"
    // TODO -- actually build logic around this
    private Long tablesToIndexesId;
    private ChainingHash<String, String> tablesToIndexes;

    @Inject
    CatalogImpl(Store store, InternalDataStructureFactory dsFactory, TxnManager txnManager) {
        this.store = store;
        this.dsFactory = dsFactory;
        this.txnManager = txnManager;
    }

    @Override
    public <T extends TopLevelDataStructure> T get(String name, Class<T> klass) {
        try {
            Long id = getIdForName(name);
            if (id != null && id != -1L) {
                return loadById(id, klass);
            } else {
                throw new IllegalArgumentException("Data structure with name " + name + " doesn't exist!");
            }
        } catch (ExecutionException | IOException | TimeoutException xcp) {
            throw new RuntimeException(xcp);
        }
    }

    @Override
    public void create(TopLevelDataStructure ds, Txn txn) throws IOException {
        if (catalogRootId == null) {
            try {
                initCatalogRootId();
            } catch (TimeoutException xcp) {
                throw new IOException(xcp);
            }
        }

        ChainingHash<String, Long> namesToIds = dsFactory.loadChainingHashForWrites(namesToIdsId, txn);
        namesToIds.put(ds.getName(), ds.getId(), txn);
    }

//    public List<String> getIndexesFor(Table table) throws TimeoutException, IOException {
//        if (tablesToIndexes == null) {
//            if (catalogRootId == null) {
//                initCatalogRootId();
//            }
//            tablesToIndexes = dsFactory.loadChainingHash(tablesToIndexesId);
//        }
//        return Collections.emptyList();
//    }

    ///////////////////////////////////////////////////////////
    //////////////////////////////////////////////
    /////////////////////////////////
    // This method is a little bit dicey since this determines
    // how the catalog root is serialized. Refer to the
    // table at the top of this file for context.
    private synchronized void initCatalogRootId() throws TimeoutException, IOException {
        if (catalogRootId != null) return;

        Txn txn = txnManager.beginTransaction();
        StoreTransaction internalTxn = txn.getStoreTransaction();
        try {
            catalogRootId = store.getRoot(CATALOG_ROOT_NAME, internalTxn);
            ///////////////////////////////////////////////////////
            ///////////////////////////////////////////
            // be very careful when you change the ordering here
            if (store.isRootCreated(catalogRootId, internalTxn)) {
                // I just created it for you
                ChainingHash<String, Long> namesToIds = dsFactory.newChainingHash(txn);
                ChainingHash<String, Long> tablesToIndexes = dsFactory.newChainingHash(txn);
                byte[] catalogRoot = writeLongToByteArray(namesToIds.getId(), tablesToIndexes.getId());
                store.set(catalogRootId, catalogRoot, internalTxn);
                namesToIdsId = namesToIds.getId();
                tablesToIndexesId = tablesToIndexes.getId();
            } else {
                // was there before
                byte[] catalogRoot = store.get(catalogRootId);
                namesToIdsId = readLongAtIndex(catalogRoot, 0);
                tablesToIndexesId = readLongAtIndex(catalogRoot, 1);
            }
            txn.commit();
        } catch (Exception xcp) {
            txn.rollback();
            txn.abort();
        }
    }

    private Long getIdForName(String name) throws ExecutionException, TimeoutException, IOException {
        if (namesToIds == null) {
            if (catalogRootId == null) {
                initCatalogRootId();
            }
            namesToIds = dsFactory.loadChainingHash(namesToIdsId);
        }
        return namesToIds.get(name);
    }

    // I don't really like this code but this should be ok for now
    // I gotta read up in Feign how they create implementations of interfaces
    private <T extends TopLevelDataStructure> T loadById(Long id, Class<T> klass) {
        TopLevelDataStructure ds;
        if (Index.class.equals(klass)) {
            ds = dsFactory.loadIndex(id);
        } else if (Table.class.equals(klass)) {
            ds = dsFactory.loadTable(id);
        } else {
            return null;
        }
        return klass.cast(ds);
    }

    private byte[] writeLongToByteArray(long... ls) {
        byte[][] bites = new byte[ls.length][];
        for (int i = 0; i < ls.length; i++) {
            bites[i] = Longs.toByteArray(ls[i]);
        }
        return Bytes.concat(bites);
    }

    private long readLongAtIndex(byte[] bites, int idx) {
        // just as a reminder: a long are 8 bytes :)
        int start = Long.BYTES * idx;
        Preconditions.checkArgument(bites.length >= start + Long.BYTES, "array too small: %s < %s", bites.length, start + Long.BYTES);
        return Longs.fromBytes(
                bites[start],
                bites[start + 1],
                bites[start + 2],
                bites[start + 3],
                bites[start + 4],
                bites[start + 5],
                bites[start + 6],
                bites[start + 7]
        );
    }
}
