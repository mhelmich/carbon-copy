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

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import org.distbc.GuiceJUnit4Runner;
import org.distbc.GuiceModules;
import org.distbc.data.structures.Catalog;
import org.distbc.data.structures.DataStructureModule;
import org.distbc.data.structures.InternalDataStructureFactory;
import org.distbc.data.structures.Table;
import org.distbc.data.structures.TempTable;
import org.distbc.data.structures.Tuple;
import org.distbc.data.structures.Txn;
import org.distbc.data.structures.TxnManager;
import org.distbc.data.structures.TxnManagerModule;
import org.distbc.parser.ParsingResult;
import org.distbc.parser.QueryPaserModule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

@RunWith(GuiceJUnit4Runner.class)
@GuiceModules({ DataStructureModule.class, TxnManagerModule.class, QueryPlannerModule.class, QueryPaserModule.class})
public class QueryPlannerTest {
    @Inject
    Catalog catalog;

    @Inject
    private InternalDataStructureFactory dsFactory;

    @Inject
    private TxnManager txnManager;

    /**
     * This test basically executes a query like this:
     * SELECT t1_narf FROM t1
     */
    @Test
    public void testProjection() throws Exception {
        List<String> tablesNames = ImmutableList.of("t1");
        createTable(tablesNames);

        QueryPlanner planner = new QueryPlannerImpl(catalog, dsFactory, txnManager);
        ParsingResult pr = new ParsingResult() {
            @Override
            public List<String> getTableNames() {
                return new ArrayList<>(getTableNamesForNames(tablesNames));
            }

            @Override
            public List<String> getProjectionColumnNames() {
                return ImmutableList.of("t1_narf");
            }

            @Override
            public List<String> getWhereClauses() {
                return Collections.emptyList();
            }

            @Override
            public List<String> getJoinClauses() {
                return Collections.emptyList();
            }

            @Override
            public List<BinaryOperation> getBinaryOperations() {
                return Collections.emptyList();
            }
        };

        QueryPlan qp = planner.generateQueryPlan(pr);
        List<QueryPlanSwimLane> swimLanes = getSwimLanesFromPlan(qp);
        assertEquals(1, swimLanes.size());
        QueryPlanSwimLane sl = swimLanes.get(0);
        Table t1 = getTable(tablesNames.get(0));
        assertNotEquals(t1.getId(), getBase(sl).getId());
        List<Operation> ops = getOperations(sl);
        assertEquals(1, ops.size());
        assertTrue(ops.get(0) instanceof Projection);
    }

    /**
     * This test executes a query like this:
     * SELECT t2_narf FROM t2 WHERE t2_narf = 'void'
     */
    @Test
    public void testSelectionAndProjection() throws Exception {
        List<String> tablesNames = ImmutableList.of("t2");
        createTable(tablesNames);

        QueryPlanner planner = new QueryPlannerImpl(catalog, dsFactory, txnManager);
        ParsingResult pr = new ParsingResult() {
            @Override
            public List<String> getTableNames() {
                return new ArrayList<>(getTableNamesForNames(tablesNames));
            }

            @Override
            public List<String> getProjectionColumnNames() {
                return ImmutableList.of("t2_narf");
            }

            @Override
            public List<String> getWhereClauses() {
                return ImmutableList.of("t2_narf = 'void'");
            }

            @Override
            public List<String> getJoinClauses() {
                return Collections.emptyList();
            }

            @Override
            public List<BinaryOperation> getBinaryOperations() {
                return Collections.emptyList();
            }
        };

        QueryPlan qp = planner.generateQueryPlan(pr);
        List<QueryPlanSwimLane> swimLanes = getSwimLanesFromPlan(qp);
        assertEquals(1, swimLanes.size());
        QueryPlanSwimLane sl = swimLanes.get(0);
        Table t1 = getTable(tablesNames.get(0));
        assertNotEquals(t1.getId(), getBase(sl).getId());
        List<Operation> ops = getOperations(sl);
        assertEquals(2, ops.size());
        assertTrue(ops.get(0) instanceof Projection);
    }

    private Table getTable(String tableName) throws IOException {
        String tn = getTableName(tableName);
        return catalog.get(tn, Table.class);
    }

    private void createTable(List<String> tableNames) throws IOException {
        for (String tn : tableNames) {
            createTable(tn);
        }
    }

    private void createTable(String tableName) throws IOException {
        String tn = getTableName(tableName);
        Txn txn = txnManager.beginTransaction();
        Table.Builder builder = Table.Builder.newBuilder(tn)
                .withColumn("tup_num", String.class)
                .withColumn("moep", String.class)
                .withColumn("foo", String.class);
        Table table = dsFactory.newTable(builder, txn);

        catalog.create(tn, table, txn);
        txn.commit();
        long id = table.getId();
        Table readTable = catalog.get(tn, Table.class);
        assertEquals(id, readTable.getId());

        txn = txnManager.beginTransaction();
        table = dsFactory.newTable(builder, txn);
        Tuple tup1 = new Tuple(3);
        tup1.put(0, "tup1_narf");
        tup1.put(1, "moep");
        tup1.put(2, "tup1_foo");

        Tuple tup2 = new Tuple(3);
        tup2.put(0, "tup2_narf");
        tup2.put(1, "__moep__");
        tup2.put(2, "tup2_foo");

        Tuple tup3 = new Tuple(3);
        tup3.put(0, "tup3_narf");
        tup3.put(1, "moep");
        tup3.put(2, "tup3_foo");

        table.insert(tup1, txn);
        table.insert(tup2, txn);
        table.insert(tup3, txn);
        txn.commit();
    }

    private List<String> getTableNamesForNames(List<String> tableNames) {
        List<String> tns = new ArrayList<>();
        for (String tn : tableNames) {
            tns.add(getTableName(tn));
        }
        return tns;
    }

    private String getTableName(String tableName) {
        return "table_" + tableName + "_" + getClass().getName();
    }

    @SuppressWarnings("unchecked")
    private List<QueryPlanSwimLane> getSwimLanesFromPlan(QueryPlan plan) throws NoSuchFieldException, IllegalAccessException {
        Field field = plan.getClass().getDeclaredField("swimLanes");
        field.setAccessible(true);
        return (List<QueryPlanSwimLane>) field.get(plan);
    }

    @SuppressWarnings("unchecked")
    private TempTable getBase(QueryPlanSwimLane swimLane) throws NoSuchFieldException, IllegalAccessException {
        Field field = swimLane.getClass().getDeclaredField("base");
        field.setAccessible(true);
        return (TempTable) field.get(swimLane);
    }

    @SuppressWarnings("unchecked")
    private List<Operation> getOperations(QueryPlanSwimLane swimLane) throws NoSuchFieldException, IllegalAccessException {
        Field field = swimLane.getClass().getDeclaredField("ops");
        field.setAccessible(true);
        return (List<Operation>) field.get(swimLane);
    }
}
