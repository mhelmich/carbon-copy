package org.distbc.data.structures;

import co.paralleluniverse.galaxy.Store;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class ChainingHashTest {
    @Test
    public void testBasic() throws IOException {
        Map<String, String> m = new HashMap<>();
        Txn txn = Mockito.mock(Txn.class);
        when(txn.getStoreTransaction()).thenReturn(null);
        ChainingHash<String, String> h = newChainingHash(txn);

        // this needs to be
        int count = ChainingHash.DEFAULT_NUM_BUCKETS * 3;
        for (int i = 0; i < count; i++) {
            String key = UUID.randomUUID().toString();
            String value = UUID.randomUUID().toString();
            m.put(key, value);
            h.put(key, value, txn);
        }

        m.entrySet().forEach(e -> assertEquals(e.getValue(), h.get(e.getKey())));
    }

    @Test
    public void testResize() throws IOException {
        Map<String, String> m = new HashMap<>();
        Store s = Mockito.mock(Store.class);
        Txn txn = Mockito.mock(Txn.class);
        when(txn.getStoreTransaction()).thenReturn(null);
        ChainingHash<String, String> h = new ChainingHash<String, String>(s, new DataStructureFactoryImpl(s), txn) {
            @Override
            DataBlock<String, String> newDataBlock(Txn txn) {
                return new DataBlock<String, String>(s, txn) {
                    @Override
                    int getMaxByteSize() {
                        return 8;
                    }
                };
            }
        };

        int count = ChainingHash.DEFAULT_NUM_BUCKETS * 11;
        for (int i = 0; i < count; i++) {
            String key = UUID.randomUUID().toString();
            String value = UUID.randomUUID().toString();
            m.put(key, value);
            h.put(key, value, txn);
        }

        m.entrySet().forEach(e -> assertEquals(e.getValue(), h.get(e.getKey())));
    }

    @Test
    public void testDelete() throws IOException {
        Map<String, String> m = new HashMap<>();
        Txn txn = Mockito.mock(Txn.class);
        when(txn.getStoreTransaction()).thenReturn(null);
        ChainingHash<String, String> h = newChainingHash(txn);

        String key = UUID.randomUUID().toString();
        String value = UUID.randomUUID().toString();

        m.put(key, value);
        h.put(key, value, txn);

        m.put("12", "12");
        h.put("12", "12", txn);

        key = UUID.randomUUID().toString();
        value = UUID.randomUUID().toString();

        m.put(key, value);
        h.put(key, value, txn);

        m.put("13", "13");
        h.put("13", "13", txn);

        key = UUID.randomUUID().toString();
        value = UUID.randomUUID().toString();

        m.put(key, value);
        h.put(key, value, txn);

        int count = ChainingHash.DEFAULT_NUM_BUCKETS * 2;
        for (int i = 0; i < count; i++) {
            key = UUID.randomUUID().toString();
            value = UUID.randomUUID().toString();
            m.put(key, value);
            h.put(key, value, txn);
        }

        m.entrySet().forEach(e -> assertEquals(e.getValue(), h.get(e.getKey())));

        m.remove("13");
        h.delete("13", txn);
        m.entrySet().forEach(e -> assertEquals(e.getValue(), h.get(e.getKey())));

        m.remove("12");
        h.delete("12", txn);
        m.entrySet().forEach(e -> assertEquals(e.getValue(), h.get(e.getKey())));

        h.delete("12", txn);
        m.entrySet().forEach(e -> assertEquals(e.getValue(), h.get(e.getKey())));
    }

    @Test
    public void testKeys() throws IOException {
        Map<String, String> m = new HashMap<>();
        Txn txn = Mockito.mock(Txn.class);
        when(txn.getStoreTransaction()).thenReturn(null);
        ChainingHash<String, String> h = newChainingHash(txn);

        // this needs to be
        int count = ChainingHash.DEFAULT_NUM_BUCKETS * 3;
        for (int i = 0; i < count; i++) {
            String key = UUID.randomUUID().toString();
            String value = UUID.randomUUID().toString();
            m.put(key, value);
            h.put(key, value, txn);
        }

        for (String s : h.keys()) {
            assertEquals(m.get(s), h.get(s));
        }
    }

    private <Key extends Comparable<Key>, Value> ChainingHash<Key, Value> newChainingHash(Txn txn) {
        Store s = Mockito.mock(Store.class);
        return new ChainingHash<>(s, new DataStructureFactoryImpl(s), txn);
    }
}
