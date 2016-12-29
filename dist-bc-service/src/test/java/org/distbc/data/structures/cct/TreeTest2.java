package org.distbc.data.structures.cct;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TreeTest2 {
    private int leafNodeSize = 3;
    private int numberOfNodesInLeafNodeGroup = 3;

    @Test
    public void testBasic() {
        String value = "prefix_1";
        Tree2<Integer, String> t = new Tree2<>(leafNodeSize, numberOfNodesInLeafNodeGroup);
        t.put(1, value);
        assertTrue(t.get(1).contains(value));
    }

    @Test
    public void testPutGetIncreasingOrder() {
        Tree2<Integer, String> t = new Tree2<>(leafNodeSize, numberOfNodesInLeafNodeGroup);
        Map<Integer, String> m = new HashMap<>();
        int numElements = leafNodeSize * numberOfNodesInLeafNodeGroup;
        for (int i = 0; i < numElements; i++) {
            String str = "prefix_" + i;
            t.put(i, str);
            m.put(i, str);
        }

        System.err.println(t.toString());

        m.entrySet().forEach(e -> {
                Set<String> s = t.get(e.getKey());
                assertEquals(1, s.size());
                assertTrue(s.contains(e.getValue()));
            }
        );
    }

    @Test
    public void testPutGetIncreasingOrderWithSplit() {
        Tree2<Integer, String> t = new Tree2<>(leafNodeSize, numberOfNodesInLeafNodeGroup);
        Map<Integer, String> m = new HashMap<>();
        int numElements = 2 * leafNodeSize * numberOfNodesInLeafNodeGroup;
        for (int i = 0; i < numElements; i++) {
            String str = "prefix_" + i;
            t.put(i, str);
            m.put(i, str);
        }

        System.err.println(t.toString());

        m.entrySet().forEach(e -> {
                Set<String> s = t.get(e.getKey());
                assertEquals("at " + e.toString(), 1, s.size());
                assertTrue(s.contains(e.getValue()));
            }
        );
    }
}
