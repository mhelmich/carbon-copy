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
import org.distbc.data.structures.DataStructureModule;
import org.distbc.data.structures.InternalDataStructureFactory;
import org.distbc.data.structures.Table;
import org.distbc.data.structures.TempTable;
import org.distbc.data.structures.Tuple;
import org.distbc.data.structures.Txn;
import org.distbc.data.structures.TxnManager;
import org.distbc.data.structures.TxnManagerModule;
import org.distbc.parser.ParsingResult;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(GuiceJUnit4Runner.class)
@GuiceModules({ DataStructureModule.class, TxnManagerModule.class })
public class GalaxyOperationTest {
    @Inject
    private InternalDataStructureFactory dsFactory;

    @Inject
    private TxnManager txnManager;

    @Test
    public void testProjection() throws IOException {
        Table t = createDummyTable();
        Txn txn = txnManager.beginTransaction();
        TempTable ttOld = dsFactory.newTempTableFromTable(t, txn);
        txn.commit();

        Projection p = new Projection(ImmutableList.of("tup_num".toUpperCase(), "foo".toUpperCase()), ttOld.getColumnNames());
        Txn txn2 = txnManager.beginTransaction();
        TempTable tt = p.apply(ttOld, txn2);
        txn2.commit();

        t.keys().forEach(guid -> {
            assertNotNull(tt.get(guid));
            assertEquals(guid, tt.get(guid).getGuid());
            assertNotEquals(t.get(guid), tt.get(guid));
        });
    }

    @Test
    public void testSelection() throws IOException {
        Table t = createDummyTable();
        Txn txn = txnManager.beginTransaction();
        TempTable ttOld = dsFactory.newTempTableFromTable(t, txn);
        txn.commit();

        Selection s = new Selection(ImmutableList.of(
                new ParsingResult.BinaryOperation("moep", "=", "moep")
        ));
        Txn txn2 = txnManager.beginTransaction();
        TempTable tt = s.apply(ttOld, txn2);
        txn2.commit();

        AtomicInteger i = new AtomicInteger(0);
        tt.keys().forEach(guid -> {
            assertNotNull(t.get(guid));
            assertEquals(guid, tt.get(guid).getGuid());
            assertEquals(t.get(guid), tt.get(guid));
            i.getAndAdd(1);
        });

        assertEquals(2, i.get());
    }

    private Table createDummyTable() throws IOException {
        Txn txn = txnManager.beginTransaction();

        Table.Builder tableBuilder = Table.Builder.newBuilder("narf_" + System.currentTimeMillis())
                .withColumn("tup_num", String.class)
                .withColumn("moep", String.class)
                .withColumn("foo", String.class);

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

        table.insert(tup1, txn);
        table.insert(tup2, txn);
        table.insert(tup3, txn);
        txn.commit();

        return table;
    }
}
