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

package org.carbon.copy.planner;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import org.carbon.copy.GuiceJUnit4Runner;
import org.carbon.copy.data.structures.DataStructureModule;
import org.carbon.copy.data.structures.InternalDataStructureFactory;
import org.carbon.copy.data.structures.Table;
import org.carbon.copy.data.structures.Tuple;
import org.carbon.copy.data.structures.Txn;
import org.carbon.copy.data.structures.TxnManager;
import org.carbon.copy.data.structures.TxnManagerModule;
import org.carbon.copy.GuiceModules;
import org.carbon.copy.data.structures.Catalog;
import org.carbon.copy.parser.ParsingResult;
import org.carbon.copy.parser.QueryPaserModule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(GuiceJUnit4Runner.class)
@GuiceModules({ DataStructureModule.class, TxnManagerModule.class, QueryPlannerModule.class, QueryPaserModule.class })
public class QueryPlannerTest {
    @Inject
    private Catalog catalog;

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

        ParsingResult pr = Mockito.mock(ParsingResult.class);
        Mockito.when(pr.getTableNames()).thenReturn(getTableNamesForNames(tablesNames));
        Mockito.when(pr.getProjectionColumnNames()).thenReturn(ImmutableList.of("t1_narf"));
        Mockito.when(pr.getExpressionText()).thenReturn("");

        QueryPlanner planner = new QueryPlannerImpl(catalog, dsFactory, txnManager);
        QueryPlan qp = planner.generateQueryPlan(pr);
        List<UnaryQueryPlanSwimLane> swimLanes = getSwimLanesFromPlan(qp);
        assertEquals(1, swimLanes.size());
        UnaryQueryPlanSwimLane sl = swimLanes.get(0);
        Table t1 = getTable(tablesNames.get(0));
        assertEquals(t1.getId(), getBase(sl).getId());
    }

    /**
     * This test executes a query like this:
     * SELECT t2_narf FROM t2 WHERE t2_narf = 'void'
     */
    @Test
    public void testSelectionAndProjection() throws Exception {
        List<String> tablesNames = ImmutableList.of("t2");
        createTable(tablesNames);

        ParsingResult pr = Mockito.mock(ParsingResult.class);
        Mockito.when(pr.getTableNames()).thenReturn(getTableNamesForNames(tablesNames));
        Mockito.when(pr.getProjectionColumnNames()).thenReturn(ImmutableList.of("t2_narf"));
        Mockito.when(pr.getExpressionText()).thenReturn("t2_narf='void'");

        QueryPlanner planner = new QueryPlannerImpl(catalog, dsFactory, txnManager);
        QueryPlan qp = planner.generateQueryPlan(pr);
        List<UnaryQueryPlanSwimLane> swimLanes = getSwimLanesFromPlan(qp);
        assertEquals(1, swimLanes.size());
        UnaryQueryPlanSwimLane sl = swimLanes.get(0);
        Table t1 = getTable(tablesNames.get(0));
        assertEquals(t1.getId(), getBase(sl).getId());
    }

    /**
     * This test executes a query like this:
     * SELECT t1_narf, t2_moep FROM t1, t2 WHERE t1.id = t2.id
     */
    @Test
    public void testSimpleJoin() throws Exception {
        createTable("t1", "t2");

        ParsingResult pr = Mockito.mock(ParsingResult.class);
        Mockito.when(pr.getTableNames()).thenReturn(getTableNamesForNames("t1", "t2"));
        Mockito.when(pr.getProjectionColumnNames()).thenReturn(ImmutableList.of("t1_narf", "t2_moep"));
        Mockito.when(pr.getJoins()).thenReturn(Collections.singletonList(new ParsingResult.BinaryOperation(getTableName("t1") + ".id", "==", getTableName("t2") + ".id")));

        QueryPlanner planner = new QueryPlannerImpl(catalog, dsFactory, txnManager);
        QueryPlan qp = planner.generateQueryPlan(pr);
        List<UnaryQueryPlanSwimLane> swimLanes = getSwimLanesFromPlan(qp);
        assertEquals(3, swimLanes.size());
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

    private void createTable(String... tableNames) throws IOException {
        createTable(Arrays.asList(tableNames));
    }

    private void createTable(String tableName) throws IOException {
        String tn = getTableName(tableName);
        Txn txn = txnManager.beginTransaction();
        Table.Builder builder = Table.newBuilder(tn)
                .withColumn("tup_num", String.class)
                .withColumn("moep", String.class)
                .withColumn("foo", String.class);
        Table table = dsFactory.newTable(builder, txn);

        catalog.create(table, txn);
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

    private List<String> getTableNamesForNames(String... tableNames) {
        return getTableNamesForNames(Arrays.asList(tableNames));
    }

    private List<String> getTableNamesForNames(List<String> tableNames) {
        List<String> tns = new ArrayList<>();
        for (String tn : tableNames) {
            tns.add(getTableName(tn));
        }
        return tns;
    }

    private String getTableName(String tableName) {
        return "table_" + tableName + "_" + getClass().getName().replaceAll("\\.", "_");
    }

    @SuppressWarnings("unchecked")
    private List<UnaryQueryPlanSwimLane> getSwimLanesFromPlan(QueryPlan plan) throws NoSuchFieldException, IllegalAccessException {
        Field field = plan.getClass().getDeclaredField("swimLanes");
        field.setAccessible(true);
        return (List<UnaryQueryPlanSwimLane>) field.get(plan);
    }

    @SuppressWarnings("unchecked")
    private Table getBase(UnaryQueryPlanSwimLane swimLane) throws NoSuchFieldException, IllegalAccessException {
        Field field = swimLane.getClass().getDeclaredField("base");
        field.setAccessible(true);
        return (Table) field.get(swimLane);
    }
}
