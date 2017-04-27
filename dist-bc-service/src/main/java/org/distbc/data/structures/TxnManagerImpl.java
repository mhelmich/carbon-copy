/*
 *
 *  Copyright 2017 Marco Helmich
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

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
