package org.distbc.data.structures;

import co.paralleluniverse.galaxy.Store;
import com.google.inject.Inject;
import org.distbc.GuiceJUnit4Runner;
import org.distbc.GuiceModules;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@RunWith(GuiceJUnit4Runner.class)
@GuiceModules({ DataStructureModule.class, TxnManagerModule.class })
@Ignore
public class ChainingHashTest {

    @Inject
    private DataStructureFactory dsFactory;

    @Inject
    private Store store;

    @Inject
    private TxnManager txnManager;

    @Test
    public void testBasic() throws IOException {
        Map<String, String> m = new HashMap<>();
        ChainingHash<String, String> h = dsFactory.newChainingHash();

        // this needs to be
        int count = ChainingHash.DEFAULT_NUM_BUCKETS * 3;
        Txn txn = txnManager.beginTransaction();
        for (int i = 0; i < count; i++) {
            String key = UUID.randomUUID().toString();
            String value = UUID.randomUUID().toString();
            m.put(key, value);
            h.put(value, key, txn);
        }
        txn.commit();

        m.entrySet().forEach(e -> assertEquals(e.getValue(), h.get(e.getKey())));
    }

    @Test
    public void testResize() throws IOException {
        Map<String, String> m = new HashMap<>();
        ChainingHash<String, String> h = new ChainingHash<String, String>(store, dsFactory) {
            @Override
            DataBlock<String, String> newDataBlock(Txn txn) {
                return new DataBlock<String, String>(store, txn) {
                    @Override
                    int getMaxByteSize() {
                        return 8;
                    }
                };
            }
        };

        int count = ChainingHash.DEFAULT_NUM_BUCKETS * 11;
        Txn txn = txnManager.beginTransaction();
        for (int i = 0; i < count; i++) {
            String key = UUID.randomUUID().toString();
            String value = UUID.randomUUID().toString();
            m.put(key, value);
            h.put(value, key, txn);
        }
        txn.commit();

        m.entrySet().forEach(e -> assertEquals(e.getValue(), h.get(e.getKey())));
    }

    @Test
    public void testDelete() throws IOException {
        Map<String, String> m = new HashMap<>();
        ChainingHash<String, String> h = dsFactory.newChainingHash();

        String key = UUID.randomUUID().toString();
        String value = UUID.randomUUID().toString();

        Txn txn = txnManager.beginTransaction();
        m.put(key, value);
        h.put(value, key, txn);

        m.put("12", "12");
        h.put("12", "12", txn);

        key = UUID.randomUUID().toString();
        value = UUID.randomUUID().toString();

        m.put(key, value);
        h.put(value, key, txn);

        m.put("13", "13");
        h.put("13", "13", txn);

        key = UUID.randomUUID().toString();
        value = UUID.randomUUID().toString();

        m.put(key, value);
        h.put(value, key, txn);

        int count = ChainingHash.DEFAULT_NUM_BUCKETS * 2;
        for (int i = 0; i < count; i++) {
            key = UUID.randomUUID().toString();
            value = UUID.randomUUID().toString();
            m.put(key, value);
            h.put(value, key, txn);
        }
        txn.commit();

        m.entrySet().forEach(e -> assertEquals(e.getValue(), h.get(e.getKey())));

        txn = txnManager.beginTransaction();
        m.remove("13");
        h.delete("13", txn);
        txn.commit();
        m.entrySet().forEach(e -> assertEquals(e.getValue(), h.get(e.getKey())));

        txn = txnManager.beginTransaction();
        m.remove("12");
        h.delete("12", txn);
        txn.commit();
        m.entrySet().forEach(e -> assertEquals(e.getValue(), h.get(e.getKey())));

        txn = txnManager.beginTransaction();
        h.delete("12", txn);
        txn.commit();
        m.entrySet().forEach(e -> assertEquals(e.getValue(), h.get(e.getKey())));
    }

    @Test
    public void testKeys() throws IOException {
        Map<String, String> m = new HashMap<>();
        ChainingHash<String, String> h = dsFactory.newChainingHash();

        // this needs to be
        int count = ChainingHash.DEFAULT_NUM_BUCKETS * 3;
        Txn txn = txnManager.beginTransaction();
        for (int i = 0; i < count; i++) {
            String key = UUID.randomUUID().toString();
            String value = UUID.randomUUID().toString();
            m.put(key, value);
            h.put(value, key, txn);
        }
        txn.commit();

        for (String s : h.keys()) {
            assertEquals(m.get(s), h.get(s));
        }
    }
}
