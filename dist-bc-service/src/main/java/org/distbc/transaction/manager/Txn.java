package org.distbc.transaction.manager;

import co.paralleluniverse.galaxy.Store;
import co.paralleluniverse.galaxy.StoreTransaction;

public final class Txn {
    private final Store store;
    private final StoreTransaction stxn;

    Txn(Store store, StoreTransaction stxn) {
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
