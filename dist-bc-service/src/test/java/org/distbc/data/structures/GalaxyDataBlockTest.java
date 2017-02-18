package org.distbc.data.structures;

import com.google.inject.Inject;
import org.distbc.GuiceJUnit4Runner;
import org.distbc.GuiceModules;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

@RunWith(GuiceJUnit4Runner.class)
@GuiceModules({ DataStructureModule.class, TxnManagerModule.class })
public class GalaxyDataBlockTest {
    @Inject
    private InternalDataStructureFactory dsFactory;

    @Inject
    private TxnManager txnManager;

    @Test
    public void testBasicPutGet() throws IOException {
        Txn t = txnManager.beginTransaction();
        DataBlock<Integer, Long> db = dsFactory.newDataBlock(t);
        db.put(123, 123L, t);
        t.commit();

        // time goes by
        DataBlock<Integer, Long> db2 = dsFactory.loadDataBlock(db.getId());
        assertEquals(Long.valueOf(123), db2.get(123));
        assertNull(db2.get(125));
    }

    @Test(expected = IOException.class)
    public void testDoubleCommit() throws IOException {
        Txn t = txnManager.beginTransaction();
        DataBlock<Integer, Long> db = dsFactory.newDataBlock(t);
        db.put(123, 123L, t);

        try {
            t.commit();
        } catch (Exception xcp) {
            fail(xcp.getMessage());
        }

        t.commit();
    }

    @Test
    public void testPutGetDelete() throws IOException {
        Txn t = txnManager.beginTransaction();
        DataBlock<Integer, Long> db = dsFactory.newDataBlock(t);
        db.put(123, 123L, t);
        db.put(124, 124L, t);
        db.put(125, 125L, t);
        t.commit();

        DataBlock<Integer, Long> db2 = dsFactory.loadDataBlock(db.getId());
        assertEquals(Long.valueOf(125), db2.get(125));
        assertEquals(Long.valueOf(123), db2.get(123));
        assertEquals(Long.valueOf(124), db2.get(124));
        assertNull(db2.get(127));

        t = txnManager.beginTransaction();
        DataBlock<Integer, Long> db3 = dsFactory.loadDataBlockForWrites(db.getId(), t);
        db3.delete(123, t);
        t.commit();

        DataBlock<Integer, Long> db4 = dsFactory.loadDataBlock(db.getId());
        assertEquals(Long.valueOf(125), db4.get(125));
        assertNull(db4.get(123));
        assertEquals(Long.valueOf(124), db4.get(124));
        assertNull(db4.get(127));
    }

    @Test
    public void testPutIfPossibleGetDelete() throws IOException {
        Txn t = txnManager.beginTransaction();
        DataBlock<Integer, Long> db = dsFactory.newDataBlock(t);
        db.putIfPossible(123, 123L, t);
        db.putIfPossible(124, 124L, t);
        db.putIfPossible(125, 125L, t);
        t.commit();

        DataBlock<Integer, Long> db2 = dsFactory.loadDataBlock(db.getId());
        assertEquals(Long.valueOf(125), db2.get(125));
        assertEquals(Long.valueOf(123), db2.get(123));
        assertEquals(Long.valueOf(124), db2.get(124));
        assertNull(db2.get(127));

        t = txnManager.beginTransaction();
        DataBlock<Integer, Long> db3 = dsFactory.loadDataBlockForWrites(db.getId(), t);
        db3.delete(123, t);
        t.commit();

        DataBlock<Integer, Long> db4 = dsFactory.loadDataBlock(db.getId());
        assertEquals(Long.valueOf(125), db4.get(125));
        assertNull(db4.get(123));
        assertEquals(Long.valueOf(124), db4.get(124));
        assertNull(db4.get(127));
    }

    @Test
    public void testUUIDSerialization() throws IOException {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        UUID uuid3 = UUID.randomUUID();

        Txn t = txnManager.beginTransaction();
        DataBlock<UUID, String> db = dsFactory.newDataBlock(t);
        db.put(uuid1, uuid1.toString(), t);
        db.put(uuid3, uuid3.toString(), t);
        db.put(uuid2, uuid2.toString(), t);
        t.commit();

        DataBlock<UUID, String> db2 = dsFactory.loadDataBlock(db.getId());
        assertEquals(uuid3.toString(), db2.get(uuid3));
        assertEquals(uuid2.toString(), db2.get(uuid2));
        assertEquals(uuid1.toString(), db2.get(uuid1));
    }
}
