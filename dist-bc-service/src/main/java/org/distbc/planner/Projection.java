package org.distbc.planner;

import org.distbc.data.structures.Queryable;
import org.distbc.data.structures.Tuple;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

class Projection implements Operation {
    private final List<String> columnNamesToProjectTo;
    private final List<String> columnsAvailableInTuple;

    Projection(List<String> columnNamesToProjectTo, List<String> columnsAvailableInTuple) {
        this.columnNamesToProjectTo = columnNamesToProjectTo;
        this.columnsAvailableInTuple = columnsAvailableInTuple;
    }

    @Override
    public Queryable apply(Queryable queryable) {
        List<Integer> indexesToFilerOn = new LinkedList<>();
        columnNamesToProjectTo.forEach(columnName -> indexesToFilerOn.add(columnsAvailableInTuple.indexOf(columnName)));
        Set<Tuple> tuples = queryable.project(tuple -> tuple.subTuple(indexesToFilerOn));
        return new TempTable(tuples);
    }
}
