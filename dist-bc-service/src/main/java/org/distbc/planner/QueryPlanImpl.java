package org.distbc.planner;

import org.distbc.data.structures.Queryable;
import org.distbc.data.structures.Tuple;

import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;

public class QueryPlanImpl implements QueryPlan {

    void addQueryableAndPredicate(Queryable queryable, Predicate<Tuple> predicate) {
    }

    @Override
    public Set<Object> execute() {
        return Collections.emptySet();
    }
}
