package org.distbc.planner;

import org.distbc.data.structures.Queryable;
import org.distbc.data.structures.Tuple;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

class QueryPlanImpl implements QueryPlan {
    List<QueryPlanSwimLane> swimLanes = new LinkedList<>();

    void addSwimLane(QueryPlanSwimLane sl) {
        swimLanes.add(sl);
    }

    @Override
    public Set<Tuple> execute() {
        TempTable tt;
        ExecutorService es = Executors.newFixedThreadPool(swimLanes.size());

        try {
            while (!swimLanes.isEmpty()) {
                Set<Future<Queryable>> futures = swimLanes.stream()
                        .map(es::submit)
                        .collect(Collectors.toSet());
            }
        } finally {
            es.shutdown();
        }

        return Collections.emptySet();
    }
}
