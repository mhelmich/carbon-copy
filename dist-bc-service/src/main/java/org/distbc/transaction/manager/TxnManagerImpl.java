package org.distbc.transaction.manager;

import co.paralleluniverse.galaxy.Store;
import com.google.inject.Inject;

import java.io.IOException;
import java.util.function.Consumer;

public class TxnManagerImpl implements TxnManager {
    private final Store store;

    @Inject
    TxnManagerImpl(Store store) {
        this.store = store;
    }

    public void doTransactionally(Consumer<Txn> l) throws Exception {
        Txn t = beginTransaction();
        try {
            l.accept(t);
            t.commit();
        } catch (Exception xcp) {
            t.rollback();
            t.abort();
            throw new IOException(xcp);
        }
    }

    public Txn beginTransaction() {
        return new Txn(store, store.beginTransaction());
    }
}
