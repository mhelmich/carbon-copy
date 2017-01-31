package org.distbc.data.structures;

import co.paralleluniverse.galaxy.Store;
import co.paralleluniverse.galaxy.StoreTransaction;

import java.io.IOException;

public final class Txn {
    private final Store store;
    private final StoreTransaction stxn;

    Txn(Store store) {
        this.store = store;
        this.stxn = store.beginTransaction();
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
