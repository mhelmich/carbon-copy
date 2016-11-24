package org.distbc.data.structures.SibPlusTree;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class InternalNodeGroupTest {
    @Test
    public void testPutGetDelete() throws Exception {
        InternalNodeGroup ing = new InternalNodeGroup(1, 3, 2);
        Integer key = 19;
        ing.put(1, 0, key);
        assertEquals(key, ing.getKey(1, 0));
        ing.delete(1, 0);
        assertNull(ing.getKey(1, 0));
    }

    @Test
    public void testIndexOfEmptyNode() throws Exception {
        InternalNodeGroup ing = new InternalNodeGroup(1, 3, 2);
        assertEquals(0, ing.indexOfFirstEmptyNodeFromNode(0));
        ing.put(0, 1, 19);
        assertEquals(1, ing.indexOfFirstEmptyNodeFromNode(0));
    }
}
