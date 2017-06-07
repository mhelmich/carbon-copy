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

package org.carbon.copy.planner;

import org.carbon.copy.data.structures.GUID;
import org.carbon.copy.data.structures.Index;
import org.carbon.copy.data.structures.Table;
import org.carbon.copy.data.structures.TempTable;
import org.carbon.copy.data.structures.Txn;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * This operation takes a set up TempTable (as in all the metadata is in place)
 * and fills it with based on lists with GUIDs and column indexes.
 */
class OpMaterialize implements Function<Txn, TempTable> {

    private final Table tableToUse;
    private final Index indexToUse;
    private final TempTable resultSet;
    private final Set<GUID> idsToKeep;
    private final List<Integer> columnIndexesToKeep;

    OpMaterialize(Table table, TempTable tempTable, Set<GUID> idsToKeep, List<Integer> columnIndexesToKeep) {
        this.tableToUse = table;
        this.indexToUse = null;
        this.resultSet = tempTable;
        this.idsToKeep = idsToKeep;
        this.columnIndexesToKeep = columnIndexesToKeep;
    }

    private TempTable materializeFromTable(Txn txn) {
        idsToKeep.stream()
                .map(tableToUse::get)
                .map(tuple -> tuple.subTuple(columnIndexesToKeep))
                .forEach(tuple -> resultSet.insert(tuple, txn));
        return resultSet;
    }

    private TempTable materializeFromIndex(Txn txn) {
        return resultSet;
    }

    @Override
    public TempTable apply(Txn txn) {
        if (tableToUse != null) {
            return materializeFromTable(txn);
        } else {
            return materializeFromIndex(txn);
        }
    }
}
