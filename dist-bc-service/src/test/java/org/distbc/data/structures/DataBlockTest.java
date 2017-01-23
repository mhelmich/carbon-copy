package org.distbc.data.structures;

import org.junit.Test;

import java.nio.ByteBuffer;

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

        int cap = 16;
        ByteBuffer bb = ByteBuffer.allocate(cap);
        db.write(bb);
        System.err.println("byte size: " + bb.position());
        assertTrue(cap > bb.remaining());
        bb.rewind();

        DataBlock<Integer, Integer> db2 = new DataBlock<>();
        db2.read(bb);

        assertEquals(Integer.valueOf(5), db2.get(5));
        assertEquals(Integer.valueOf(6), db2.get(6));
    }
}
