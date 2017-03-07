package org.distbc.data.structures;

import co.paralleluniverse.galaxy.Store;
import com.google.inject.Inject;
import org.distbc.GuiceJUnit4Runner;
import org.distbc.GuiceModules;
import org.distbc.parser.QueryPaserModule;
import org.distbc.planner.QueryPlannerModule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

@RunWith(GuiceJUnit4Runner.class)
@GuiceModules({ DataStructureModule.class, TxnManagerModule.class, QueryPlannerModule.class, QueryPaserModule.class})
public class CatalogImplTest {

    @Inject
    private Store store;

    @Inject
    private InternalDataStructureFactory dsFactory;

    @Inject
    private TxnManager txnManager;

    @Test
    public void testBasic() throws IOException {
        String tableName = "table_" + System.currentTimeMillis();
        Txn txn = txnManager.beginTransaction();
        Table.Builder builder = new Table.Builder()
                .withColumn("narf", Integer.class);
        Table table = dsFactory.newTable(builder, txn);

        CatalogImpl c = new CatalogImpl(store, dsFactory, txnManager);
        c.create(tableName, table, txn);
        txn.commit();
        long id = table.getId();
        Table readTable = c.get(tableName, Table.class);
        assertEquals(id, readTable.getId());
    }

    @Test
    public void testExistingRoot() throws IOException {
        String tableName = "table_" + System.currentTimeMillis();
        CatalogImpl c1 = new CatalogImpl(store, dsFactory, txnManager);

        Table.Builder builder = new Table.Builder()
                .withColumn("narf", Integer.class);

        Txn txn = txnManager.beginTransaction();
        Table table1 = dsFactory.newTable(builder, txn);
        c1.create(tableName, table1, txn);
        txn.commit();

        CatalogImpl c2 = new CatalogImpl(store, dsFactory, txnManager);
        Table readTable = c2.get(tableName, Table.class);
        assertEquals(table1.getId(), readTable.getId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonExistingTable() throws IOException {
        String tableName = "table_" + System.currentTimeMillis();
        CatalogImpl c1 = new CatalogImpl(store, dsFactory, txnManager);

        Table.Builder builder = new Table.Builder()
                .withColumn("narf", Integer.class);

        Txn txn = txnManager.beginTransaction();
        Table table1 = dsFactory.newTable(builder, txn);
        c1.create(tableName, table1, txn);
        txn.commit();

        CatalogImpl c2 = new CatalogImpl(store, dsFactory, txnManager);
        c2.get("some_other_table_name", Table.class);
    }
}
