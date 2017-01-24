package org.distbc.data.structures;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class ChainingHashTest {
    @Test
    public void testBasic() {
        Map<String, String> m = new HashMap<>();
        ChainingHash<String, String> h = new ChainingHash<>();

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
        ChainingHash<String, String> h = new ChainingHash<String, String>() {
            @Override
            DataBlock<String, String> newDataBlock() {
                return new DataBlock<String, String>() {
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
}
