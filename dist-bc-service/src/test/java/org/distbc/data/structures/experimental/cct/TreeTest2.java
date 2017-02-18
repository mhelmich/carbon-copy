package org.distbc.data.structures.experimental.cct;

import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Ignore
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

    @Test
    public void testPutGetSameKey() {
        Tree2<Integer, String> t = new Tree2<>(leafNodeSize, numberOfNodesInLeafNodeGroup);
        t.put(19, "prefix_19");
        t.put(19, "prefix_20");
        t.put(19, "prefix_21");
        Set<String> rs = t.get(19);
        assertEquals(3, rs.size());
        assertTrue(rs.contains("prefix_19"));
        assertTrue(rs.contains("prefix_20"));
        assertTrue(rs.contains("prefix_21"));
    }

    @Test
    public void testPutGetDecreasingOrder() {
        Tree2<Integer, String> t = new Tree2<>(leafNodeSize, numberOfNodesInLeafNodeGroup);
        Map<Integer, String> m = new HashMap<>();
        int numElements = leafNodeSize * numberOfNodesInLeafNodeGroup;
        for (int i = numElements; i > 0; i--) {
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

    @Test
    public void testPutGetAlternatingOrder() {
        Tree2<Integer, String> t = new Tree2<>(leafNodeSize, numberOfNodesInLeafNodeGroup);
        Map<Integer, String> m = new HashMap<>();
        int numElements = (leafNodeSize * numberOfNodesInLeafNodeGroup) / 2;
        for (int i = 0; i < numElements; i++) {
            int num = i;
            String str = "prefix_" + num;
            t.put(num, str);
            m.put(num, str);

            num = -i;
            str = "prefix_" + num;
            t.put(num, str);
            m.put(num, str);
        }

        System.err.println(t.toString());

        m.entrySet().forEach(e -> {
                Set<String> s = t.get(e.getKey());
                assertEquals("at " + e.toString(), 1, s.size());
                assertTrue(s.contains(e.getValue()));
            }
        );
    }

    @Test
    public void testPutGetRandomKeys() {
        Tree2<Integer, String> t = new Tree2<>(leafNodeSize, numberOfNodesInLeafNodeGroup);
        Map<Integer, String> m = new HashMap<>();
        int numElements = leafNodeSize * numberOfNodesInLeafNodeGroup;
        Random random = new Random();

        for (int i = 0; i < numElements; i++) {
            int num = random.nextInt(1000);
            String str = "prefix_" + num;
            t.put(num, str);
            m.put(num, str);
        }

        m.entrySet().forEach(e -> {
                Set<String> s = t.get(e.getKey());
                assertEquals(1, s.size());
                assertTrue(s.contains(e.getValue()));
            }
        );
    }

    @Test
    public void testSplittingSplits() {
        Tree2<Integer, String> t = new Tree2<>(leafNodeSize, numberOfNodesInLeafNodeGroup);
        Map<Integer, String> m = new HashMap<>();
        // yea yea, I know
        // the point is:
        // I can only insert few elements as I though because the split is right leaning
        // once I change that I can put more
        // however, just don't change the sizes and this test will pass
        int numElements = (int) Math.floor(10 * leafNodeSize * numberOfNodesInLeafNodeGroup);

        for (int i = 0; i < numElements; i++) {
            String str = "prefix_" + i;
            try {
                t.put(i, str);
                m.put(i, str);
            } catch (Exception xcp) {
                System.err.println(i + "_" + str);
                throw xcp;
            }
        }

        m.entrySet().forEach(e -> {
                Set<String> s = t.get(e.getKey());
                assertEquals(1, s.size());
                assertTrue(s.contains(e.getValue()));
            }
        );
    }
}
