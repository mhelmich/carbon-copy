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
import org.distbc.data.structures.GUID;
import org.distbc.data.structures.Table;
import org.distbc.data.structures.TempTable;
import org.distbc.data.structures.Txn;
import org.distbc.data.structures.TxnManager;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

public class QueryPlanSwimLane implements Callable<TempTable> {
    private final DataStructureFactory dsFactory;
    private final TxnManager txnManager;
    private final Table base;
    private OpSelection selection;
    private OpProjection projection;

    QueryPlanSwimLane(DataStructureFactory dsFactory, TxnManager txnManager, Table base) {
        this.dsFactory = dsFactory;
        this.txnManager = txnManager;
        this.base = base;
    }

    void addSelection(Set<String> columnNames, Table tableToUse, String expression) {
        this.selection = new OpSelection(columnNames, tableToUse, expression);
    }

    void addProjection(List<String> columnNamesToProjectTo, List<String> columnsAvailableInTuple) {
        this.projection = new OpProjection(columnNamesToProjectTo, columnsAvailableInTuple);
    }

    public TempTable call() throws Exception {
        List<Integer> columnIndexesToKeep = projection.get();
        Set<GUID> guidsToKeep =  selection.get();
        Txn txn = txnManager.beginTransaction();
        try {
            TempTable tt = dsFactory.newTempTable(txn);
            OpMaterialize materialize = new OpMaterialize(base, tt, guidsToKeep, columnIndexesToKeep);
            tt = materialize.apply(txn);
            txn.commit();
            return tt;
        } catch (IOException e) {
            txn.rollback();
            throw new RuntimeException(e);
        }
    }
}
