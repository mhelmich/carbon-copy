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
        TempTable tt = createDummyTempTable();

        AtomicInteger i = new AtomicInteger(0);
        tt.keys().forEach(guid -> {
            assertNotNull(tt.get(guid));
            assertEquals(guid, tt.get(guid).getGuid());
            i.getAndAdd(1);
        });

        assertEquals(3, i.get());
    }

    @Test
    public void testRemoveColumnNames() throws IOException {
        TempTable tt = createDummyTempTable();

        Txn txn = txnManager.beginTransaction();
        tt.removeColumnWithName("narf", txn);
        txn.commit();

        List<Tuple> columns = tt.getColumnMetadata();
        assertEquals(2, columns.size());
        assertEquals("id", columns.get(0).get(0));
        assertEquals("moep", columns.get(1).get(0));
    }

    @Test
    public void testDelete() throws IOException {
        TempTable tt = createDummyTempTable();

        Txn txn = txnManager.beginTransaction();
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
        TempTable tt = createDummyTempTable();

        Txn txn = txnManager.beginTransaction();
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

    private TempTable createDummyTempTable() throws IOException {
        Txn txn = txnManager.beginTransaction();
        TempTable.Builder tableBuilder = TempTable.newBuilder()
                .withColumn("id", Integer.class)
                .withColumn("moep", String.class)
                .withColumn("narf", String.class);
        TempTable tt = dsFactory.newTempTable(tableBuilder, txn);

        Tuple tup1 = new Tuple(3);
        tup1.put(0, 1);
        tup1.put(1, "moep_" + UUID.randomUUID().toString());
        tup1.put(2, "tup1_foo");

        Tuple tup2 = new Tuple(3);
        tup2.put(0, 3);
        tup2.put(1, "__moep__" + UUID.randomUUID().toString());
        tup2.put(2, "tup2_foo");

        Tuple tup3 = new Tuple(3);
        tup3.put(0, 5);
        tup3.put(1, "moep_" + UUID.randomUUID().toString());
        tup3.put(2, "tup3_foo");

        tt.insert(tup1, txn);
        tt.insert(tup2, txn);
        tt.insert(tup3, txn);

        txn.commit();

        return tt;
    }
}
