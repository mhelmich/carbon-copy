package org.distbc.planner;

import org.distbc.data.structures.Tuple;

import java.util.Set;

public interface QueryPlan {
    Set<Tuple> execute();
}
