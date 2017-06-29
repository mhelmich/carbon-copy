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

package org.carbon.copy;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import org.carbon.copy.calcite.CalciteModule;
import org.carbon.copy.data.structures.Catalog;
import org.carbon.copy.data.structures.DataStructureFactory;
import org.carbon.copy.data.structures.DataStructureModule;
import org.carbon.copy.data.structures.Table;
import org.carbon.copy.data.structures.TempTable;
import org.carbon.copy.data.structures.Tuple;
import org.carbon.copy.data.structures.Txn;
import org.carbon.copy.data.structures.TxnManager;
import org.carbon.copy.data.structures.TxnManagerModule;
import org.carbon.copy.parser.ParsingResult;
import org.carbon.copy.parser.QueryParser;
import org.carbon.copy.parser.QueryPaserModule;
import org.carbon.copy.planner.QueryPlan;
import org.carbon.copy.planner.QueryPlanner;
import org.carbon.copy.planner.QueryPlannerModule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(GuiceJUnit4Runner.class)
@GuiceModules({ DataStructureModule.class, TxnManagerModule.class, QueryPlannerModule.class, QueryPaserModule.class, CalciteModule.class })
public class InterfaceTest {
    @Inject
    private QueryParser queryParser;

    @Inject
    private QueryPlanner queryPlanner;

    @Inject
    private DataStructureFactory dsFactory;

    @Inject
    private TxnManager txnManager;

    @Inject
    private Catalog catalog;

    private static final ThreadFactory tf = new ThreadFactoryBuilder().setNameFormat("query-worker-%d").build();
    private static final ExecutorService es = Executors.newFixedThreadPool(3, tf);

    @Test
    @Ignore
    public void testBasic() throws Exception {
        Table t = createDummyTable();
        String query = "select tup_num from " + t.getName() + " where foo <= '2_tup_foo'";
        ParsingResult pr = queryParser.parse(query);
        QueryPlan qp = queryPlanner.generateQueryPlan(pr);
        TempTable tuples = qp.execute(es);
        assertNotNull(tuples);
        Set<Tuple> resultSet = tuples.keys()
                .map(tuples::get)
                .collect(Collectors.toSet());
        assertEquals(2, resultSet.size());
        Set<Integer> expectedResults = new HashSet<Integer>() {{
            add(1);
            add(2);
        }};
        for (Tuple tup : resultSet) {
            assertEquals(1, tup.getTupleSize());
            Integer i = (Integer) tup.get(0);
            assertTrue(expectedResults.remove(i));
        }
        assertTrue(expectedResults.isEmpty());
    }

    @Test
    @Ignore
    public void testBasicBooleanExpression() throws Exception {
        Table t = createDummyTable();
        String query = "select tup_num from " + t.getName() + " where foo <= '2_tup_foo' and moep='__moep__'";
        ParsingResult pr = queryParser.parse(query);
        QueryPlan qp = queryPlanner.generateQueryPlan(pr);
        TempTable tuples = qp.execute(es);
        assertNotNull(tuples);
        Set<Tuple> resultSet = tuples.keys()
                .map(tuples::get)
                .collect(Collectors.toSet());
        assertEquals(1, resultSet.size());
        Set<Integer> expectedResults = new HashSet<Integer>() {{
            add(1);
        }};
        for (Tuple tup : resultSet) {
            assertEquals(1, tup.getTupleSize());
            Integer i = (Integer) tup.get(0);
            assertTrue(expectedResults.remove(i));
        }
        assertTrue(expectedResults.isEmpty());
    }

    @Test
    public void testCalciteConnectionNoTable() throws SQLException {
        try (Connection connection = getCalciteConnection()) {
            try (ResultSet tables = connection.getMetaData().getTables(null, "carbon-copy", null, null)) {
                assertFalse(tables.next());
            }
        }
    }

    @Test
    public void testCalciteConnectionListTables() throws SQLException, IOException {
        Table t1 = createDummyTable();
        try (Connection connection = getCalciteConnection()) {
            try (ResultSet tables = connection.getMetaData().getTables(null, "carbon-copy", null, null)) {
                assertTrue(tables.next());
                String tableName = tables.getString("TABLE_NAME");
                assertEquals(t1.getName(), tableName);
                assertFalse(tables.next());
            }
        }

        Table t2 = createDummyTable();
        try (Connection connection = getCalciteConnection()) {
            try (ResultSet tables = connection.getMetaData().getTables(null, "carbon-copy", null, null)) {
                assertTrue(tables.next());
                String tableName = tables.getString("TABLE_NAME");
                assertEquals(t1.getName(), tableName);
                assertTrue(tables.next());
                tableName = tables.getString("TABLE_NAME");
                assertEquals(t2.getName(), tableName);
                assertFalse(tables.next());
            }
        }
    }

    @Test
    public void testCalciteQuery() throws IOException, SQLException {
        Table t = createDummyTable("MARCO");
        try (Connection connection = getCalciteConnection()) {

            String tableName;
            try (ResultSet tables = connection.getMetaData().getTables(null, "carbon-copy", null, null)) {
                assertTrue(tables.next());
                tableName = tables.getString("TABLE_NAME");
                assertEquals(t.getName(), tableName);
            }

            try (Statement statement = connection.createStatement()) {
                String sql = "SELECT * FROM " + tableName + " WHERE moep = 'moep' AND 2 = tup_num";
                try (ResultSet resultSet = statement.executeQuery(sql)) {
                    Set<Integer> tupNums = new HashSet<>(2);

                    while (resultSet.next()) {
                        tupNums.add(resultSet.getInt("tup_num"));
                    }

                    assertEquals(1, tupNums.size());
                    assertTrue(tupNums.remove(2));
                }
            }
        }
    }

    private Connection getCalciteConnection() throws SQLException {
        Properties props = new Properties();
        props.put("model",
                "inline:"
                        + "{\n"
                        + "  version: '1.0',\n"
                        + "  defaultSchema: 'carbon-copy',\n"
                        + "   schemas: [\n"
                        + "     {\n"
                        + "       type: 'custom',\n"
                        + "       name: 'carbon-copy',\n"
                        + "       factory: 'org.carbon.copy.calcite.SchemaFactory',\n"
                        + "       operand: {\n"
                        + "           some: 'text'\n"
                        + "       }\n"
                        + "     }\n"
                        + "   ]\n"
                        + "}");
        return DriverManager.getConnection("jdbc:calcite:", props);
    }

    private Table createDummyTable() throws IOException {
        String tableName = "narf_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().replaceAll("-", "");
        return createDummyTable(tableName, 1, 2, 3);
    }

    private Table createDummyTable(String tableName) throws IOException {
        return createDummyTable(tableName, 1, 2, 3);
    }

    private Table createDummyTable(String tableName, int... ids) throws IOException {
        Table.Builder tableBuilder = Table.newBuilder(tableName.toUpperCase())
                .withColumn("tup_num".toUpperCase(), Integer.class)
                .withColumn("moep".toUpperCase(), String.class)
                .withColumn("foo".toUpperCase(), String.class);

        Txn txn = txnManager.beginTransaction();
        Table table = dsFactory.newTable(tableBuilder, txn);

        for (int id : ids) {
            Tuple tup = new Tuple(3);
            tup.put(0, id);
            tup.put(1, (id % 2 == 0) ? "moep" : "__moep__");
            tup.put(2, id + "_tup_foo");
            table.insert(tup, txn);
        }

        catalog.create(table, txn);
        txn.commit();

        return table;
    }
}
