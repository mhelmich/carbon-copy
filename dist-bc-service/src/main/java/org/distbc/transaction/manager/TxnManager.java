package org.distbc.transaction.manager;

import java.util.function.Consumer;

public interface TxnManager {
    Txn beginTransaction();
    void doTransactionally(Consumer<Txn> l) throws Exception;
}
