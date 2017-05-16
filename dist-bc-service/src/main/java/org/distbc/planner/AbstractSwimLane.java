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

package org.distbc.planner;

import org.distbc.data.structures.DataStructureFactory;
import org.distbc.data.structures.TempTable;
import org.distbc.data.structures.TxnManager;

import java.util.concurrent.Callable;

abstract class AbstractSwimLane implements Callable<TempTable> {
    final DataStructureFactory dsFactory;
    final TxnManager txnManager;

    AbstractSwimLane(DataStructureFactory dsFactory, TxnManager txnManager) {
        this.dsFactory = dsFactory;
        this.txnManager = txnManager;
    }

    @Override
    public abstract TempTable call() throws Exception;
}
