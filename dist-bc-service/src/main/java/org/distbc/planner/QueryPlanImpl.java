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

import org.distbc.data.structures.Queryable;
import org.distbc.data.structures.TempTable;
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
