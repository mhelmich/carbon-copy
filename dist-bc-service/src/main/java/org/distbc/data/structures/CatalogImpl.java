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
import com.google.common.primitives.Longs;
import com.google.inject.Inject;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * This guy keeps track of everything inside dist-bc (all top-level objects anyways).
 * It holds a map of all top-level objects to their roots and takes care of
 * all sorts of accounting tasks (such as mapping indexes to tables, etc.)
 * It also has convenience methods to load tables and indexes.
 */
class CatalogImpl implements Catalog {
    private static final String CATALOG_ROOT_NAME = "CATALOG_ROOT";

    private final Store store;
    private final InternalDataStructureFactory dsFactory;
    private final TxnManager txnManager;

    private Long catalogRootId;
    // this map contains all high-level objects
    // it maps their names to the galaxy ids that are their roots
    private ChainingHash<String, Long> namesToIds;

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
    public void create(String name, TopLevelDataStructure ds, Txn txn) throws IOException {
        if (catalogRootId == null) {
            try {
                initCatalogRootId();
            } catch (TimeoutException xcp) {
                throw new IOException(xcp);
            }
        }

        ChainingHash<String, Long> namesToIds = dsFactory.loadChainingHashForWrites(catalogRootId, txn);
        namesToIds.put(name, ds.getId(), txn);
    }

    private synchronized void initCatalogRootId() throws TimeoutException, IOException {
        if (catalogRootId != null) return;

        Txn txn = txnManager.beginTransaction();
        StoreTransaction internalTxn = txn.getStoreTransaction();
        try {
            Long id = store.getRoot(CATALOG_ROOT_NAME, internalTxn);
            if (store.isRootCreated(id, internalTxn)) {
                // I just created it for you
                ChainingHash<String, Long> namesToIds = dsFactory.newChainingHash(txn);
                byte[] catalogRoot = Longs.toByteArray(namesToIds.getId());
                store.set(id, catalogRoot, internalTxn);
                catalogRootId = namesToIds.getId();
            } else {
                // was there before
                byte[] catalogRoot = store.get(id);
                catalogRootId = Longs.fromByteArray(catalogRoot);
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
            namesToIds = dsFactory.loadChainingHash(catalogRootId);
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
}
