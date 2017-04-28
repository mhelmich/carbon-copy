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

package org.distbc.data.structures;

import com.google.inject.Inject;
import org.distbc.GuiceJUnit4Runner;
import org.distbc.GuiceModules;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(GuiceJUnit4Runner.class)
@GuiceModules({ DataStructureModule.class, TxnManagerModule.class })
public class GalaxyTempTableTest {
    @Inject
    private InternalDataStructureFactory dsFactory;

    @Inject
    private TxnManager txnManager;

    @Test
    public void testBasic() throws IOException {
        Table t = createDummyTable();
        Txn txn = txnManager.beginTransaction();
        TempTable tt = dsFactory.newTempTableFromTable(t, txn);
        txn.commit();

        AtomicInteger i = new AtomicInteger(0);
        t.keys().forEach(guid -> {
            assertNotNull(tt.get(guid));
            assertEquals(guid, tt.get(guid).getGuid());
            assertEquals(t.get(guid), tt.get(guid));
            i.getAndAdd(1);
        });

        assertEquals(3, i.get());
    }

    @Test
    public void testRemoveColumnNames() throws IOException {
        Table t = createDummyTable();
        Txn txn = txnManager.beginTransaction();
        TempTable tt = dsFactory.newTempTableFromTable(t, txn);
        txn.commit();

        txn = txnManager.beginTransaction();
        tt.removeColumnWithName("foo".toUpperCase(), txn);
        txn.commit();

        List<Tuple> columns = tt.getColumnMetadata();
        assertEquals(2, columns.size());
        assertEquals("tup_num".toUpperCase(), columns.get(0).get(0));
        assertEquals("moep".toUpperCase(), columns.get(1).get(0));
    }

    @Test
    public void testDelete() throws IOException {
        Table t = createDummyTable();
        Txn txn = txnManager.beginTransaction();
        TempTable tt = dsFactory.newTempTableFromTable(t, txn);
        txn.commit();

        txn = txnManager.beginTransaction();
        List<GUID> keys = tt.keys().collect(Collectors.toList());
        tt.delete(keys.get(1), txn);
        txn.commit();

        keys.remove(1);

        txn = txnManager.beginTransaction();
        TempTable tt2 = dsFactory.loadTempTableFromId(tt.getId(), txn);

        AtomicInteger i = new AtomicInteger(0);
        tt2.keys()
                .forEach(guid -> {
                    assertTrue(keys.contains(guid));
                    i.getAndAdd(1);
                });

        assertEquals(2, i.get());
    }

    @Test
    public void testSaveLoad() throws IOException {
        Table t = createDummyTable();
        Txn txn = txnManager.beginTransaction();
        TempTable tt = dsFactory.newTempTableFromTable(t, txn);
        txn.commit();

        txn = txnManager.beginTransaction();
        TempTable tt2 = dsFactory.loadTempTableFromId(tt.getId(), txn);
        txn.commit();

        AtomicInteger i = new AtomicInteger(0);
        tt.keys()
                .forEach(guid -> {
                    assertNotNull(tt2.get(guid));
                    i.getAndAdd(1);
                });

        assertEquals(3, i.get());

        AtomicInteger i2 = new AtomicInteger(0);
        tt2.keys()
                .forEach(guid -> {
                    assertNotNull(tt.get(guid));
                    i2.getAndAdd(1);
                });

        assertEquals(3, i2.get());
    }

    private Table createDummyTable() throws IOException {
        Txn txn = txnManager.beginTransaction();

        Table.Builder tableBuilder = Table.Builder.newBuilder("narf_" + UUID.randomUUID().toString() + "_" + System.currentTimeMillis())
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
