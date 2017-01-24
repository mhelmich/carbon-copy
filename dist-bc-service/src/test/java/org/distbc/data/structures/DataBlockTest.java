package org.distbc.data.structures;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DataBlockTest {
    @Test
    public void testBasic() {
        DataBlock<Integer, Integer> db = new DataBlock<>();
        db.put(5, 5);
        db.put(6, 6);

        assertEquals(Integer.valueOf(5), db.get(5));
        assertEquals(Integer.valueOf(6), db.get(6));
        assertNull(db.get(7));
    }

    @Test
    public void testDups() {
        DataBlock<Integer, Integer> db = new DataBlock<>();
        db.put(5, 5);
        db.put(5, 6);

        assertEquals(Integer.valueOf(6), db.get(5));
        assertNull(db.get(6));
    }

    @Test
    public void testSerialization() {
        DataBlock<Integer, Integer> db = new DataBlock<>();
        db.put(5, 5);
        db.put(6, 6);
        assertEquals(Integer.valueOf(5), db.get(5));
        assertEquals(Integer.valueOf(6), db.get(6));

        ByteBuffer bb = ByteBuffer.allocateDirect(DataStructure.MAX_BYTE_SIZE);
        db.write(bb);
        assertEquals(24, db.size());
        assertEquals(1549, bb.position());
        assertTrue(DataStructure.MAX_BYTE_SIZE > bb.remaining());

        bb.rewind();
        DataBlock<Integer, Integer> db2 = new DataBlock<>();
        db2.read(bb);

        assertEquals(Integer.valueOf(5), db2.get(5));
        assertEquals(Integer.valueOf(6), db2.get(6));
        assertEquals(db.size(), db2.size());


        DataBlock<String, String> db3 = new DataBlock<>();
        String val1 = UUID.randomUUID().toString();
        String val2 = UUID.randomUUID().toString();
        String val3 = UUID.randomUUID().toString();
        db3.put("1", val1);
        db3.put("2", val2);
        db3.put("3", val3);

        ByteBuffer bb2 = ByteBuffer.allocateDirect(DataStructure.MAX_BYTE_SIZE);
        db3.write(bb2);
        assertEquals(119, db3.size());
        assertTrue(1665 >= bb2.position());
        assertTrue(DataStructure.MAX_BYTE_SIZE > bb2.remaining());
        bb2.rewind();

        DataBlock<String, String> db4 = new DataBlock<>();
        db4.read(bb2);
        assertEquals(val2, db4.get("2"));
        assertEquals(val3, db4.get("3"));
        assertEquals(val1, db4.get("1"));
        assertEquals(db3.size(), db4.size());
    }

    @Test
    public void testPutIfPossible() {
        DataBlock<Integer, Integer> db = new DataBlock<Integer, Integer>() {
            @Override
            int getMaxByteSize() {
                return 22;
            }
        };

        assertTrue(db.putIfPossible(3, 3));
        assertTrue(db.putIfPossible(5, 5));
        assertFalse(db.putIfPossible(7, 7));
    }

    @Test
    public void testDeleteMiddle() {
        DataBlock<Integer, Integer> db = new DataBlock<>();
        db.put(3, 3);
        db.put(5, 5);
        db.put(7, 7);

        assertEquals(Integer.valueOf(5), db.get(5));
        assertEquals(Integer.valueOf(3), db.get(3));
        assertEquals(Integer.valueOf(7), db.get(7));

        db.delete(5);

        assertNull(db.get(5));
        assertEquals(Integer.valueOf(3), db.get(3));
        assertEquals(Integer.valueOf(7), db.get(7));
        assertEquals(24, db.size());
    }

    @Test
    public void testDeleteFirst() {
        DataBlock<Integer, Integer> db = new DataBlock<>();
        db.put(3, 3);
        db.put(5, 5);
        db.put(7, 7);

        assertEquals(Integer.valueOf(5), db.get(5));
        assertEquals(Integer.valueOf(3), db.get(3));
        assertEquals(Integer.valueOf(7), db.get(7));
        assertEquals(32, db.size());

        db.delete(7);

        assertNull(db.get(7));
        assertEquals(Integer.valueOf(3), db.get(3));
        assertEquals(Integer.valueOf(5), db.get(5));
        assertEquals(24, db.size());
    }

    @Test
    public void testDeleteLast() {
        DataBlock<Integer, Integer> db = new DataBlock<>();
        db.put(3, 3);
        db.put(5, 5);
        db.put(7, 7);

        assertEquals(Integer.valueOf(5), db.get(5));
        assertEquals(Integer.valueOf(3), db.get(3));
        assertEquals(Integer.valueOf(7), db.get(7));
        assertEquals(32, db.size());

        db.delete(3);

        assertNull(db.get(3));
        assertEquals(Integer.valueOf(5), db.get(5));
        assertEquals(Integer.valueOf(7), db.get(7));
        assertEquals(24, db.size());
    }

    @Test
    public void testKeys() {
        DataBlock<Integer, Integer> db = new DataBlock<>();
        db.put(3, 3);
        db.put(5, 5);
        db.put(7, 7);
        assertEquals(32, db.size());

        Set<Integer> keys = new HashSet<>();
        for (Integer key : db.keys()) {
            keys.add(key);
        }

        assertEquals(3, keys.size());
        assertTrue(keys.contains(3));
        assertTrue(keys.contains(5));
        assertTrue(keys.contains(7));
        assertFalse(keys.contains(2));
    }
}
