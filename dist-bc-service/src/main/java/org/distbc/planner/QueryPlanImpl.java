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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class QueryPlanImpl implements QueryPlan {
    private List<QueryPlanSwimLane> swimLanes = new LinkedList<>();

    void addSwimLane(QueryPlanSwimLane sl) {
        swimLanes.add(sl);
    }

    @Override
    public TempTable execute(ExecutorService es) throws Exception {
        // TODO -- this has to take care of the juggling of swim lanes
//        Set<Future<TempTable>> futures = swimLanes.stream()
//                .map(es::submit)
//                .collect(Collectors.toSet());

        QueryPlanSwimLane sl = swimLanes.get(0);
        Future<TempTable> f = es.submit(sl);

        try {
            return f.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException xcp) {
            throw new Exception(xcp);
        }
    }
}
