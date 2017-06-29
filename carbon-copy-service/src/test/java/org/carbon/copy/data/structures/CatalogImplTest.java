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

import co.paralleluniverse.galaxy.Store;
import com.google.inject.Inject;
import org.carbon.copy.GuiceJUnit4Runner;
import org.carbon.copy.GuiceModules;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(GuiceJUnit4Runner.class)
@GuiceModules({ DataStructureModule.class, TxnManagerModule.class })
public class CatalogImplTest {

    @Inject
    private Store store;

    @Inject
    private InternalDataStructureFactory dsFactory;

    @Inject
    private TxnManager txnManager;

    @Test
    public void testBasic() throws IOException {
        String tableName = "TABLE_" + System.currentTimeMillis();
        Txn txn = txnManager.beginTransaction();
        Table.Builder builder = Table.newBuilder(tableName)
                .withColumn("NARF", Integer.class);
        Table table = dsFactory.newTable(builder, txn);

        CatalogImpl c = new CatalogImpl(store, dsFactory, txnManager);
        c.create(table, txn);
        txn.commit();
        long id = table.getId();
        Table readTable = c.get(tableName, Table.class);
        assertEquals(id, readTable.getId());
    }

    @Test
    public void testExistingRoot() throws IOException {
        String tableName = "TABLE_" + System.currentTimeMillis();
        CatalogImpl c1 = new CatalogImpl(store, dsFactory, txnManager);

        Table.Builder builder = Table.newBuilder(tableName)
                .withColumn("NARF", Integer.class);

        Txn txn = txnManager.beginTransaction();
        Table table1 = dsFactory.newTable(builder, txn);
        c1.create(table1, txn);
        txn.commit();

        CatalogImpl c2 = new CatalogImpl(store, dsFactory, txnManager);
        Table readTable = c2.get(tableName, Table.class);
        assertEquals(table1.getId(), readTable.getId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonExistingTable() throws IOException {
        String tableName = "TABLE_" + System.currentTimeMillis();
        CatalogImpl c1 = new CatalogImpl(store, dsFactory, txnManager);

        Table.Builder builder = Table.newBuilder(tableName)
                .withColumn("NARF", Integer.class);

        Txn txn = txnManager.beginTransaction();
        Table table1 = dsFactory.newTable(builder, txn);
        c1.create(table1, txn);
        txn.commit();

        CatalogImpl c2 = new CatalogImpl(store, dsFactory, txnManager);
        c2.get("some_other_table_name", Table.class);
    }

    @Test
    public void testGetAllTablesNames() throws IOException {
        String tableName1 = "TABLE_" + System.currentTimeMillis();
        CatalogImpl c1 = new CatalogImpl(store, dsFactory, txnManager);

        Table.Builder builder = Table.newBuilder(tableName1)
                .withColumn("NARF", Integer.class);

        Txn txn = txnManager.beginTransaction();
        Table table1 = dsFactory.newTable(builder, txn);
        c1.create(table1, txn);
        txn.commit();

        CatalogImpl c2 = new CatalogImpl(store, dsFactory, txnManager);
        Map<String, Table> tablesNames = c2.listTables();
        assertEquals(1, tablesNames.size());
        assertNotNull(tablesNames.remove(tableName1));
        assertEquals(0, tablesNames.size());

        String tableName2 = "TABLE_" + System.currentTimeMillis();
        builder = Table.newBuilder(tableName2)
                .withColumn("NARF", Integer.class);

        txn = txnManager.beginTransaction();
        Table table2 = dsFactory.newTable(builder, txn);
        c1.create(table2, txn);
        txn.commit();

        tablesNames = c2.listTables();
        assertEquals(2, tablesNames.size());
        assertNotNull(tablesNames.remove(tableName1));
        assertNotNull(tablesNames.remove(tableName2));
        assertEquals(0, tablesNames.size());
    }
}
