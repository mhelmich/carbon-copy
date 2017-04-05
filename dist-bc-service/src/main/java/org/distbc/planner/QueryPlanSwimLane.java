package org.distbc.planner;

import org.distbc.data.structures.Queryable;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

public class QueryPlanSwimLane implements Callable<Queryable> {
    private Queryable leaf;
    private List<Operation> ops = new LinkedList<>();

    QueryPlanSwimLane(Queryable leaf) {
        this.leaf = leaf;
    }

    void addOperation(Operation op) {
        ops.add(op);
    }

    @Override
    public Queryable call() throws Exception {
        Queryable tempResult = leaf;
        for (Operation op : ops) {
            tempResult = op.apply(tempResult);
        }
        return tempResult;
    }
}
