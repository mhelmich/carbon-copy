package org.distbc.data.structures;

import co.paralleluniverse.galaxy.Store;
import co.paralleluniverse.galaxy.TimeoutException;
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
    public void testBasic() throws TimeoutException, InterruptedException, IOException {
        String tableName = "table_" + System.currentTimeMillis();
        Txn txn = txnManager.beginTransaction();
        Table.Builder builder = new Table.Builder()
                .withColumn("narf", Integer.class);
        Table table = dsFactory.newTable(builder, txn);

        CatalogImpl c = new CatalogImpl(store, dsFactory, txnManager);
        c.create(tableName, table);
        long id = table.getId();
        Table readTable = c.get(tableName, Table.class);
        assertEquals(id, readTable.getId());
    }
}
