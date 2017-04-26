package org.distbc.planner;

import org.distbc.data.structures.TempTable;

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
    public TempTable apply(TempTable tempTable) {
        List<Integer> indexesToFilerOn = new LinkedList<>();
        columnNamesToProjectTo.forEach(columnName -> indexesToFilerOn.add(columnsAvailableInTuple.indexOf(columnName)));
//        tempTable.project(tuple -> tuple.subTuple(indexesToFilerOn));
        return tempTable;
    }
}
