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

import org.distbc.data.structures.TempTable;
import org.distbc.data.structures.Txn;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Naive implementation of a projection.
 * It goes through the metadata and every single tuple to copy the columns it needs.
 */
@Deprecated
class Projection implements Operation {
    private final List<String> columnNamesToProjectTo;
    private final List<String> columnsAvailableInTuple;

    Projection(List<String> columnNamesToProjectTo, List<String> columnsAvailableInTuple) {
        this.columnNamesToProjectTo = columnNamesToProjectTo;
        this.columnsAvailableInTuple = columnsAvailableInTuple;
    }

    @Override
    public TempTable apply(TempTable tempTable, Txn txn) {
        List<Integer> indexesToFilerOn = new LinkedList<>();
        columnNamesToProjectTo.forEach(columnName -> indexesToFilerOn.add(columnsAvailableInTuple.indexOf(columnName)));

        // remove columns out of metadata
        List<String> tmp = new ArrayList<>(columnsAvailableInTuple);
        tmp.removeAll(columnNamesToProjectTo);
        tmp.forEach(cn -> tempTable.removeColumnWithName(cn, txn));

        // remove columns out of data
        // in the potentially most naive way known to human kind
        tempTable.keys()
                .map(tempTable::get)
                .map(tuple -> tuple.subTuple(indexesToFilerOn))
                .forEach(tuple -> tempTable.insert(tuple, txn));

        return tempTable;
    }
}
