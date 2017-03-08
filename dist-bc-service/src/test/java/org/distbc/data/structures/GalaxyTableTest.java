package org.distbc.data.structures;

import com.google.inject.Inject;
import org.distbc.GuiceJUnit4Runner;
import org.distbc.GuiceModules;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(GuiceJUnit4Runner.class)
@GuiceModules({ DataStructureModule.class, TxnManagerModule.class })
public class GalaxyTableTest {
    @Inject
    private InternalDataStructureFactory dsFactory;

    @Inject
    private TxnManager txnManager;

    @Test
    public void testBasic() throws IOException {
        Txn txn = txnManager.beginTransaction();

        Table.Builder tableBuilder = Table.Builder.newBuilder("narf")
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

        Set<Tuple> rs = table2.scan(tuple -> tuple.getGuid().equals(guid1));
        assertEquals(1, rs.size());
        Tuple readTuple = rs.iterator().next();
        assertEquals(guid1, readTuple.getGuid());

        rs = table2.scan(tuple -> tuple.get(1).equals("moep"));
        assertEquals(2, rs.size());
        Set<GUID> expectedResultSet = new HashSet<>();
        expectedResultSet.add(guid1);
        expectedResultSet.add(guid3);
        for (Tuple rt : rs) {
            assertTrue(expectedResultSet.contains(rt.getGuid()));
            assertTrue(expectedResultSet.removeIf(guid -> guid.equals(rt.getGuid())));
        }
        assertTrue(expectedResultSet.isEmpty());
    }
}
