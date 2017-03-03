package org.distbc.planner;

import co.paralleluniverse.galaxy.Store;
import com.google.inject.Inject;
import org.distbc.data.structures.DataStructureFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Catalog {
    private static final Map<String, Long> namesToIds = new ConcurrentHashMap<>(32, .9f, 2);
    private final DataStructureFactory dsFactory;
    private final Store store;

    @Inject
    private Catalog(DataStructureFactory dsFactory, Store store) {
        this.dsFactory = dsFactory;
        this.store = store;
    }



//            logger.info("created skip list size [{}]", sl.size());
//    final Output output = new UnsafeMemoryOutput(1024);
//
//    long root = -1;
//    Store store = grid.store();
//
//    Long id = namesToId.get(index);
//        if (id == null) {
//        StoreTransaction txn = store.beginTransaction();
//        try {
//            root = store.getRoot(index, txn);
//            if (store.isRootCreated(root, txn)) {
//                store.set(root, output.toBytes(), txn); // initialize root
//            }
//            store.commit(txn);
//        } catch (Exception ex) {
//            logger.error("Couldn't create root", ex);
//            store.rollback(txn);
//            try {
//                store.abort(txn);
//            } catch (Exception xcp2) {
//                logger.error("Couldn't abort transaction", xcp2);
//                throw new RuntimeException(xcp2);
//            }
//        }



    public Object get(String name) {
        if (!namesToIds.containsKey(name)) {
            namesToIds.put(name, -1L);
        }

        return namesToIds.get(name);
    }
}
