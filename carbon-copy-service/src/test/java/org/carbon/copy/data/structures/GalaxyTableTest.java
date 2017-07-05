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

package org.carbon.copy.data.structures;

import com.google.inject.Inject;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class GalaxyTableTest extends GalaxyBaseTest {
    @Inject
    private InternalDataStructureFactory dsFactory;

    @Inject
    private TxnManager txnManager;

    @Test
    public void testBasic() throws IOException {
        Txn txn = txnManager.beginTransaction();

        Table.Builder tableBuilder = Table.newBuilder("narf_" + UUID.randomUUID().toString())
                .withColumn("tup_num", String.class)
                .withColumn("moep", String.class)
                .withColumn("foo", String.class);

        Table table1 = dsFactory.newTable(tableBuilder, txn);

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

        GUID guid1 = table1.insert(tup1, txn);
        GUID guid2 = table1.insert(tup2, txn);
        GUID guid3 = table1.insert(tup3, txn);
        txn.commit();

        long tableId = table1.getId();

        Table table2 = dsFactory.loadTable(tableId);
        assertEquals("tup2_narf", table2.get(guid2).get(0));
        assertEquals("moep", table2.get(guid1).get(1));
        assertEquals("tup3_foo", table2.get(guid3).get(2));
    }

    @Test
    public void testWithVariousSchemas() throws IOException {
        Table.Builder tableBuilder = Table.newBuilder("narf_" + UUID.randomUUID().toString())
                .withColumn("id", Integer.class)
                .withColumn("moep", String.class)
                .withColumn("foo", Long.class);

        Txn txn = txnManager.beginTransaction();
        Table table1 = dsFactory.newTable(tableBuilder, txn);

        Tuple tup1 = new Tuple(3);
        tup1.put(0, 123);
        tup1.put(1, "moep");
        tup1.put(2, Long.MAX_VALUE);

        Tuple tup2 = new Tuple(3);
        tup2.put(0, 234);
        tup2.put(1, "__moep__");
        tup2.put(2, 19L);

        Tuple tup3 = new Tuple(3);
        tup3.put(0, 345);
        tup3.put(1, "moep");
        tup3.put(2, 1234567890123450L);

        GUID guid1 = table1.insert(tup1, txn);
        GUID guid2 = table1.insert(tup2, txn);
        GUID guid3 = table1.insert(tup3, txn);
        txn.commit();

        long tableId = table1.getId();

        Table table2 = dsFactory.loadTable(tableId);
        assertEquals(234, table2.get(guid2).get(0));
        assertEquals("moep", table2.get(guid1).get(1));
        assertEquals(1234567890123450L, table2.get(guid3).get(2));
    }

    @Test
    public void testColumnsAndAll() throws IOException {
        Txn txn = txnManager.beginTransaction();

        Table.Builder tableBuilder = Table.newBuilder("narf")
                .withColumn("TUP_NUM", String.class)
                .withColumn("MOEP", Integer.class)
                .withColumn("FOO", String.class);

        Table table1 = dsFactory.newTable(tableBuilder, txn);
        txn.commit();
        long t1Id = table1.getId();

        Table table2 = dsFactory.loadTable(t1Id);
        assertEquals(t1Id, table2.getId());

        List<String> cols = table2.getColumnNames();
        assertEquals(3, cols.size());
        assertEquals("TUP_NUM", cols.get(0));
        assertEquals("MOEP", cols.get(1));
        assertEquals("FOO", cols.get(2));
    }
}
