package org.distbc.data.structures;

import com.google.inject.Inject;
import org.distbc.GuiceJUnit4Runner;
import org.distbc.GuiceModules;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(GuiceJUnit4Runner.class)
@GuiceModules({ DataStructureModule.class, TxnManagerModule.class })
public class GalaxyChainingHashTest {
    @Inject
    private DataStructureFactory dsFactory;

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
}
