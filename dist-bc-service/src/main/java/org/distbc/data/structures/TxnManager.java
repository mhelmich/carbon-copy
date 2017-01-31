package org.distbc.data.structures;

import java.util.function.Consumer;

public interface TxnManager {
    Txn beginTransaction();
    void doTransactionally(Consumer<Txn> lambda) throws Exception;
}
