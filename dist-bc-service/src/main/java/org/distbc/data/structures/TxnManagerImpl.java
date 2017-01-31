package org.distbc.data.structures;

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

    public void doTransactionally(Consumer<Txn> lambda) throws Exception {
        Txn txn = beginTransaction();
        try {
            lambda.accept(txn);
            txn.commit();
        } catch (Exception xcp) {
            txn.rollback();
            txn.abort();
            throw new IOException(xcp);
        }
    }

    public Txn beginTransaction() {
        return new Txn(store);
    }
}
