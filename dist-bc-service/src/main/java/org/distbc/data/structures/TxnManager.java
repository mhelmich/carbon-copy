package org.distbc.data.structures;

import co.paralleluniverse.galaxy.Store;
import co.paralleluniverse.galaxy.StoreTransaction;
import com.google.inject.Inject;

import java.io.IOException;
import java.util.function.Consumer;

public class TxnManager {
    private final Store store;

    @Inject
    TxnManager(Store store) {
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

    public static class Txn {
        private final Store store;

        private final StoreTransaction stxn;

        private Txn(Store store, StoreTransaction stxn) {
            this.store = store;
            this.stxn = stxn;
        }

        public void commit() throws InterruptedException {
            store.commit(stxn);
        }

        public void abort() throws InterruptedException {
            store.abort(stxn);
        }
        public void rollback() {
            store.rollback(stxn);
        }
    }
}
