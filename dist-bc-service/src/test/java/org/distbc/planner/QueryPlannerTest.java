package org.distbc.planner;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import org.distbc.GuiceJUnit4Runner;
import org.distbc.GuiceModules;
import org.distbc.data.structures.Catalog;
import org.distbc.data.structures.DataStructureModule;
import org.distbc.data.structures.InternalDataStructureFactory;
import org.distbc.data.structures.Queryable;
import org.distbc.data.structures.Table;
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
     * SELECT t1_narf from t1
     */
    @Test
    public void testBasic() throws IOException, NoSuchFieldException, IllegalAccessException {
        List<String> tablesNames = ImmutableList.of("t1");
        createTable(tablesNames);

        QueryPlanner planner = new QueryPlannerImpl(catalog);
        ParsingResult pr = new ParsingResult() {
            @Override
            public List<String> getTableNames() {
                return new ArrayList<>(getTableNamesForNames(tablesNames));
            }

            @Override
            public List<String> getColumnNames() {
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
        };

        QueryPlan qp = planner.generateQueryPlan(pr);
        List<QueryPlanSwimLane> swimLanes = getSwimLanesFromPlan(qp);
        assertEquals(1, swimLanes.size());
        QueryPlanSwimLane sl = swimLanes.get(0);
        Table t1 = getTable(tablesNames.get(0));
        assertEquals(t1.getId(), getLeaf(sl).getId());
        List<Operation> ops = getOperations(sl);
        assertEquals(1, ops.size());
        assertTrue(ops.get(0) instanceof Projection);
        Projection p = (Projection) ops.get(0);
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
                .withColumn(tableName + "_narf", Integer.class)
                .withColumn(tableName + "_moep", String.class);
        Table table = dsFactory.newTable(builder, txn);

        catalog.create(tn, table, txn);
        txn.commit();
        long id = table.getId();
        Table readTable = catalog.get(tn, Table.class);
        assertEquals(id, readTable.getId());
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
    private Queryable getLeaf(QueryPlanSwimLane swimLane) throws NoSuchFieldException, IllegalAccessException {
        Field field = swimLane.getClass().getDeclaredField("leaf");
        field.setAccessible(true);
        return (Queryable) field.get(swimLane);
    }

    @SuppressWarnings("unchecked")
    private List<Operation> getOperations(QueryPlanSwimLane swimLane) throws NoSuchFieldException, IllegalAccessException {
        Field field = swimLane.getClass().getDeclaredField("ops");
        field.setAccessible(true);
        return (List<Operation>) field.get(swimLane);
    }
}
