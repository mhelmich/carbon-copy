package org.distbc.data.structures;

import com.google.inject.Inject;
import org.distbc.GuiceJUnit4Runner;
import org.distbc.GuiceModules;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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

        t.keys().forEach(guid -> {
            assertNotNull(tt.get(guid));
            assertEquals(guid, tt.get(guid).getGuid());
            assertEquals(t.get(guid), tt.get(guid));
        });
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
