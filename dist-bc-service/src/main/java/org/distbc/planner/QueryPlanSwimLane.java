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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

public class QueryPlanSwimLane implements Callable<Queryable> {
    private Queryable leaf;
    private List<Operation> ops = new LinkedList<>();

    QueryPlanSwimLane(TempTable leaf) {

    }

    QueryPlanSwimLane(Queryable leaf) {
        this.leaf = leaf;
    }

    void addOperation(Operation op) {
        ops.add(op);
    }

    @Override
    public Queryable call() throws Exception {
        Queryable tempResult = leaf;
//        for (Operation op : ops) {
//            tempResult = op.apply(tempResult);
//        }
        return tempResult;
    }
}
