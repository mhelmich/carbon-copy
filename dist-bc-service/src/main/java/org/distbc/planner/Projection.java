package org.distbc.planner;

import org.distbc.data.structures.TempTable;
import org.distbc.data.structures.Txn;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
        tempTable.keys()
                .map(tempTable::get)
                .map(tuple -> tuple.subTuple(indexesToFilerOn))
                .forEach(tuple -> tempTable.insert(tuple, txn));

        return tempTable;
    }
}
