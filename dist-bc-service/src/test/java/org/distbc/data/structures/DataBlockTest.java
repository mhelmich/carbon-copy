package org.distbc.data.structures;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
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

        int cap = 10;
        ByteBuffer bb = ByteBuffer.allocate(cap);
        db.write(bb);
        System.err.println("byte size: " + bb.position());
        assertTrue(cap > bb.remaining());
        bb.rewind();

        DataBlock<Integer, Integer> db2 = new DataBlock<>();
        db2.read(bb);

        assertEquals(Integer.valueOf(5), db2.get(5));
        assertEquals(Integer.valueOf(6), db2.get(6));

        DataBlock<String, String> db3 = new DataBlock<>();
        String val1 = UUID.randomUUID().toString();
        String val2 = UUID.randomUUID().toString();
        String val3 = UUID.randomUUID().toString();
        db3.put("1", val1);
        db3.put("2", val2);
        db3.put("3", val3);

        cap = 4096;
        ByteBuffer bb2 = ByteBuffer.allocate(cap);
        db3.write(bb2);
        System.err.println("byte size: " + bb2.position());
        assertTrue(cap > bb2.remaining());
        bb2.rewind();

        DataBlock<String, String> db4 = new DataBlock<>();
        db4.read(bb2);
        assertEquals(val2, db4.get("2"));
        assertEquals(val3, db4.get("3"));
        assertEquals(val1, db4.get("1"));
    }
}
