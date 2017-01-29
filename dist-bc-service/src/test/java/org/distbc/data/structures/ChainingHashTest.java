package org.distbc.data.structures;

import co.paralleluniverse.galaxy.Store;
import com.google.inject.Inject;
import org.distbc.DataStructureModule;
import org.distbc.GuiceJUnit4Runner;
import org.distbc.GuiceModules;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@RunWith(GuiceJUnit4Runner.class)
@GuiceModules({ DataStructureModule.class })
public class ChainingHashTest {

    @Inject
    private DataStructureFactory dsFactory;

    @Inject
    private Store store;

    @Test
    public void testBasic() {
        Map<String, String> m = new HashMap<>();
        ChainingHash<String, String> h = dsFactory.newChainingHash();

        // this needs to be
        int count = ChainingHash.DEFAULT_NUM_BUCKETS * 3;
        for (int i = 0; i < count; i++) {
            String key = UUID.randomUUID().toString();
            String value = UUID.randomUUID().toString();
            m.put(key, value);
            h.put(key, value);
        }

        m.entrySet().forEach(e -> assertEquals(e.getValue(), h.get(e.getKey())));
    }

    @Test
    public void testResize() {
        Map<String, String> m = new HashMap<>();
        ChainingHash<String, String> h = new ChainingHash<String, String>(store, dsFactory) {
            @Override
            DataBlock<String, String> newDataBlock() {
                return new DataBlock<String, String>(store) {
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
            h.put(key, value);
        }

        m.entrySet().forEach(e -> assertEquals(e.getValue(), h.get(e.getKey())));
    }

    @Test
    public void testDelete() {
        Map<String, String> m = new HashMap<>();
        ChainingHash<String, String> h = dsFactory.newChainingHash();

        String key = UUID.randomUUID().toString();
        String value = UUID.randomUUID().toString();

        m.put(key, value);
        h.put(key, value);

        m.put("12", "12");
        h.put("12", "12");

        key = UUID.randomUUID().toString();
        value = UUID.randomUUID().toString();

        m.put(key, value);
        h.put(key, value);

        m.put("13", "13");
        h.put("13", "13");

        key = UUID.randomUUID().toString();
        value = UUID.randomUUID().toString();

        m.put(key, value);
        h.put(key, value);

        int count = ChainingHash.DEFAULT_NUM_BUCKETS * 2;
        for (int i = 0; i < count; i++) {
            key = UUID.randomUUID().toString();
            value = UUID.randomUUID().toString();
            m.put(key, value);
            h.put(key, value);
        }
        m.entrySet().forEach(e -> assertEquals(e.getValue(), h.get(e.getKey())));

        m.remove("13");
        h.delete("13");
        m.entrySet().forEach(e -> assertEquals(e.getValue(), h.get(e.getKey())));

        m.remove("12");
        h.delete("12");
        m.entrySet().forEach(e -> assertEquals(e.getValue(), h.get(e.getKey())));

        h.delete("12");
        m.entrySet().forEach(e -> assertEquals(e.getValue(), h.get(e.getKey())));
    }

    @Test
    public void testKeys() {
        Map<String, String> m = new HashMap<>();
        ChainingHash<String, String> h = dsFactory.newChainingHash();

        // this needs to be
        int count = ChainingHash.DEFAULT_NUM_BUCKETS * 3;
        for (int i = 0; i < count; i++) {
            String key = UUID.randomUUID().toString();
            String value = UUID.randomUUID().toString();
            m.put(key, value);
            h.put(key, value);
        }

        for (String s : h.keys()) {
            assertEquals(m.get(s), h.get(s));
        }
    }
}
