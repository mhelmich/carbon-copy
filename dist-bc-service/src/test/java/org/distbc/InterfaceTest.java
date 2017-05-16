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

package org.distbc;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import org.distbc.data.structures.Catalog;
import org.distbc.data.structures.DataStructureFactory;
import org.distbc.data.structures.DataStructureModule;
import org.distbc.data.structures.GUID;
import org.distbc.data.structures.Table;
import org.distbc.data.structures.TempTable;
import org.distbc.data.structures.Tuple;
import org.distbc.data.structures.Txn;
import org.distbc.data.structures.TxnManager;
import org.distbc.data.structures.TxnManagerModule;
import org.distbc.parser.ParsingResult;
import org.distbc.parser.QueryParser;
import org.distbc.parser.QueryPaserModule;
import org.distbc.planner.QueryPlan;
import org.distbc.planner.QueryPlanner;
import org.distbc.planner.QueryPlannerModule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(GuiceJUnit4Runner.class)
@GuiceModules({ DataStructureModule.class, TxnManagerModule.class, QueryPlannerModule.class, QueryPaserModule.class})
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
    public void testBasic() throws Exception {
        Table t = createDummyTable();
        String query = "select tup_num from " + t.getName() + " where foo <= 'tup2_foo'";
        ParsingResult pr = queryParser.parse(query);
        QueryPlan qp = queryPlanner.generateQueryPlan(pr);
        TempTable tuples = qp.execute(es);
        assertNotNull(tuples);
        Set<Tuple> resultSet = tuples.keys()
                .map(tuples::get)
                .collect(Collectors.toSet());
        assertEquals(2, resultSet.size());
        Set<String> expectedResults = new HashSet<String>() {{
            add("tup2_narf");
            add("tup1_narf");
        }};
        for (Tuple tup : resultSet) {
            assertEquals(1, tup.getTupleSize());
            assertTrue(expectedResults.remove(tup.get(0).toString()));
        }
        assertTrue(expectedResults.isEmpty());
    }

    @Test
    public void testBasicBooleanExpression() throws Exception {
        Table t = createDummyTable();
        String query = "select tup_num from " + t.getName() + " where foo <= 'tup2_foo' and moep='__moep__'";
        ParsingResult pr = queryParser.parse(query);
        QueryPlan qp = queryPlanner.generateQueryPlan(pr);
        TempTable tuples = qp.execute(es);
        assertNotNull(tuples);
        Set<Tuple> resultSet = tuples.keys()
                .map(tuples::get)
                .collect(Collectors.toSet());
        assertEquals(1, resultSet.size());
        Set<String> expectedResults = new HashSet<String>() {{
            add("tup2_narf");
        }};
        for (Tuple tup : resultSet) {
            assertEquals(1, tup.getTupleSize());
            assertTrue(expectedResults.remove(tup.get(0).toString()));
        }
        assertTrue(expectedResults.isEmpty());
    }

    private Table createDummyTable() throws IOException {
        return createDummyTable(null);
    }

    private Table createDummyTable(List<GUID> guids) throws IOException {
        Table.Builder tableBuilder = Table.newBuilder("narf_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().replaceAll("-", ""))
                .withColumn("tup_num", String.class)
                .withColumn("moep", String.class)
                .withColumn("foo", String.class);

        Txn txn = txnManager.beginTransaction();
        Table table = dsFactory.newTable(tableBuilder, txn);

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

        if (guids == null) {
            table.insert(tup1, txn);
            table.insert(tup2, txn);
            table.insert(tup3, txn);
        } else {
            guids.add(table.insert(tup1, txn));
            guids.add(table.insert(tup2, txn));
            guids.add(table.insert(tup3, txn));
        }

        catalog.create(table, txn);
        txn.commit();

        return table;
    }
}
