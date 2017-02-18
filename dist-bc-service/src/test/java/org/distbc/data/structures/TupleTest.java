package org.distbc.data.structures;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TupleTest {
    @Test
    public void testBasic() {
        Tuple t = newTuple(5);
        t.put(0, "narf");
        t.put(1, 17);
        t.put(2, Long.MAX_VALUE);
        t.put(3, "narf_2");
        t.put(4, "MrMoep");

        assertEquals("narf", t.get(0));
        assertEquals(17, t.get(1));
        assertEquals(Long.MAX_VALUE, t.get(2));
        assertEquals("narf_2", t.get(3));
        assertEquals("MrMoep", t.get(4));
    }

    private Tuple newTuple(int size) {
        return new Tuple(size);
    }
}
