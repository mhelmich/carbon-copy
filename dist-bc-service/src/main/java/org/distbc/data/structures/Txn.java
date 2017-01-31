package org.distbc.data.structures;

import co.paralleluniverse.galaxy.Store;
import co.paralleluniverse.galaxy.StoreTransaction;

import java.io.IOException;

public class Txn {
    private final Store store;
    private final StoreTransaction stxn;

    Txn(Store store) {
        this(store, store.beginTransaction());
    }

    Txn(Store store, StoreTransaction stxn) {
        this.store = store;
        this.stxn = stxn;
    }

    public void commit() throws IOException {
        try {
            store.commit(stxn);
        } catch (Exception xcp) {
            throw new IOException(xcp);
        }
    }

    public void abort() throws IOException {
        try {
            store.abort(stxn);
        } catch (Exception xcp) {
            throw new IOException(xcp);
        }
    }

    public void rollback() {
        store.rollback(stxn);
    }

    StoreTransaction getStoreTransaction() {
        return stxn;
    }
}
