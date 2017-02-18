package org.distbc.data.structures;

import com.google.inject.Inject;
import org.distbc.GuiceJUnit4Runner;
import org.distbc.GuiceModules;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(GuiceJUnit4Runner.class)
@GuiceModules({ DataStructureModule.class, TxnManagerModule.class })
public class TxnManagerTest {

    @Inject
    private InternalDataStructureFactory dsFactory;

    @Inject
    private TxnManager txnManager;

    @Test
    public void testBasicTransaction() throws Exception {
        int count = 100;
        Map<String, String> map = new HashMap<>();
        AtomicLong id = new AtomicLong(-1L);

        txnManager.doTransactionally(txn -> {
            DataBlock<String, String> db = dsFactory.newDataBlock(txn);
            for (int i = 0; i < count; i++) {
                String key = UUID.randomUUID().toString();
                String value = UUID.randomUUID().toString();

                map.put(key, value);
                db.put(key, value, txn);
                id.set(db.getId());
            }
        });

        DataBlock<String, String> db = dsFactory.loadDataBlock(id.get());
        map.entrySet().forEach(e -> assertEquals(e.getValue(), db.get(e.getKey())));
    }

    /**
     * Rolling back an empty object fails with an NPE.
     * I still need to find a way around that...
     */
    @Test
    @Ignore
    public void testChangingExistingHash() throws Exception {
        AtomicLong id = new AtomicLong(-1L);

        txnManager.doTransactionally(txn -> {
            DataBlock<Integer, String> db = dsFactory.newDataBlock(txn);
            for (int i = 0; i < 10; i++) {
                String value = UUID.randomUUID().toString();

                db.put(i, value, txn);
                id.set(db.getId());
            }
            throw new RuntimeException("BOOOOM -- this has been planted for your test");
        });

        DataBlock<Integer, String> db = dsFactory.loadDataBlock(id.get());
        assertNull(db.get(7));
        assertNull(db.get(3));
        assertNull(db.get(5));
    }
}
