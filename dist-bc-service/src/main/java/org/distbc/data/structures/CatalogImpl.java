package org.distbc.data.structures;

import co.paralleluniverse.galaxy.Store;
import co.paralleluniverse.galaxy.StoreTransaction;
import co.paralleluniverse.galaxy.TimeoutException;
import com.google.common.primitives.Longs;
import com.google.inject.Inject;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

class CatalogImpl implements Catalog {
    private static final String CATALOG_ROOT_NAME = "CATALOG_ROOT";

    private final Store store;
    private final InternalDataStructureFactory dsFactory;
    private final TxnManager txnManager;

    private Long catalogRootId;
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
