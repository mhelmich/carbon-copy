package org.distbc.data.structures;

import com.google.inject.Inject;
import org.distbc.GuiceJUnit4Runner;
import org.distbc.GuiceModules;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(GuiceJUnit4Runner.class)
@GuiceModules({ DataStructureModule.class, TxnManagerModule.class })
public class GalaxyChainingHashTest {
    @Inject
    private InternalDataStructureFactory dsFactory;

    @Inject
    private TxnManager txnManager;

    @Test
    public void testBasicPutGet() throws IOException {
        Txn t = txnManager.beginTransaction();
        ChainingHash<Integer, Long> hash = dsFactory.newChainingHash(t);
        hash.put(123, 123L, t);
        t.commit();

        // time goes by
        ChainingHash<Integer, Long> db2 = dsFactory.loadChainingHash(hash.getId());
        assertEquals(Long.valueOf(123), db2.get(123));
        assertNull(db2.get(125));
    }

    @Test
    public void testPutGetDelete() throws IOException {
        int count = 123;
        Txn t = txnManager.beginTransaction();
        ChainingHash<Integer, Long> hash = dsFactory.newChainingHash(t);
        for (int i = 0; i < count; i++) {
            hash.put(i, (long) i, t);
        }
        t.commit();

        // time goes by
        ChainingHash<Integer, Long> hash2 = dsFactory.loadChainingHash(hash.getId());
        assertPresent(0, count, hash2);
        assertNull(hash2.get(125));

        t = txnManager.beginTransaction();
        ChainingHash<Integer, Long> hash3 = dsFactory.loadChainingHashForWrites(hash.getId(), t);
        assertTrue(hash3.delete(111, t));
        assertTrue(hash3.delete(99, t));
        t.commit();

        ChainingHash<Integer, Long> hash4 = dsFactory.loadChainingHash(hash.getId());
        assertPresent(0, count, hash2);
        assertNull(hash4.get(125));

        assertPresent(0, 99, hash4);
        assertPresent(100, 111, hash4);
        assertPresent(112, count, hash4);
        assertNull(hash4.get(99));
        assertNull(hash4.get(111));
    }

    private void assertPresent(int from, int to, ChainingHash<Integer, Long> hash) {
        for (int i = from; i < to; i++) {
            assertEquals(Long.valueOf(i), hash.get(i));
        }
    }
}
