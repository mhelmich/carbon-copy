package org.distbc.data.structures;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import com.google.common.base.Optional;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by mhelmich on 10/1/16.
 */
public class SkipListTest {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(SkipList.class);

    @Test
    public void testBasic() throws Exception {
        int count = 1000;
        Random r = new Random();
        SkipList<String, String> sl = new SkipList<String, String>();
        Map<String, String> map = new HashMap<String, String>(count);
        assertTrue(sl.size() == 0);

        for (int i = 0; i < count; i++) {
            String key = UUID.randomUUID().toString();
            String value = UUID.randomUUID().toString();
            sl.insert(key, value);
            map.put(key, value);
        }
        assertEquals(count, sl.size());

        Map.Entry<String, String> e = getIthElement(map, r.nextInt(map.size()));
        assertEquals(e.getValue(), sl.floor(e.getKey()).get());
    }

    @Test
    public void testThreeInsertsAndMiddleDelete() throws Exception {
        SkipList<String, String> sl = new SkipList<String, String>();

        String key1 = "key_000";
        String value1 = UUID.randomUUID().toString();
        sl.insert(key1, value1);

        String key2 = "key_111";
        String value2 = UUID.randomUUID().toString();
        sl.insert(key2, value2);

        String key3 = "key_333";
        String value3 = UUID.randomUUID().toString();
        sl.insert(key3, value3);

        assertEquals(3, sl.size());
        assertEquals(value2, sl.floor(key2).get());

        sl.delete(key2);
        // verify the delete worked
        assertEquals(2, sl.size());
        assertEquals(Optional.absent(), sl.floor(key2));
        // verify the rest of the list can still be navigated
        assertEquals(value3, sl.floor(key3).get());
        assertEquals(value1, sl.floor(key1).get());
    }

    @Test
    public void testSerialization() throws Exception {
        Kryo kryo = new Kryo();
        kryo.register(SkipList.class, 0);

        SkipList sl = new SkipList();
        for (int i = 0; i < 350; i++) {
            String key = UUID.randomUUID().toString();
            String value = UUID.randomUUID().toString();
            sl.insert(key, value);
        }

        ByteBufferOutput umo = new ByteBufferOutput(32768, 32768);
        kryo.writeObject(umo, sl);
        umo.close();
        logger.info("byte size: " + umo.total());
        umo.release();
    }

    private <K, V> Map.Entry<K, V> getIthElement(Map<K, V> m, int i) {
        i = i % m.size();
        for (Map.Entry<K, V> e : m.entrySet()) {
            i--;
            if (i == 0) return e;
        }
        return null;
    }
}
