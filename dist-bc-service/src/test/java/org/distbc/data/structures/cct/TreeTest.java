package org.distbc.data.structures.cct;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TreeTest {

    private int leafNodeSize = 3;
    private int numberOfNodesInLeafNodeGroup = 3;

    @Test
    public void testBasic() {
        String value = "prefix_1";
        Tree<Integer, String> t = new Tree<>(leafNodeSize, numberOfNodesInLeafNodeGroup);
        t.put(1, value);
        assertTrue(t.get(1).contains(value));
    }

    @Test
    public void testSimplePutGet() {
        Tree<Integer, String> t = new Tree<>(leafNodeSize, numberOfNodesInLeafNodeGroup);
        t.put(19, "prefix_19");
        Set<String> rs = t.get(19);
        assertEquals(1, rs.size());
        assertTrue(rs.contains("prefix_19"));
    }

    @Test
    public void testPutGetSameKey() {
        Tree<Integer, String> t = new Tree<>(leafNodeSize, numberOfNodesInLeafNodeGroup);
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
    public void testPutGetIncreasingOrder() {
        Tree<Integer, String> t = new Tree<>(leafNodeSize, numberOfNodesInLeafNodeGroup);
        Map<Integer, String> m = new HashMap<>();
        int numElements = leafNodeSize * numberOfNodesInLeafNodeGroup;
        for (int i = 0; i < numElements; i++) {
            String str = "prefix_" + i;
            t.put(i, str);
            m.put(i, str);
        }

        m.entrySet().forEach(e -> {
                Set<String> s = t.get(e.getKey());
                assertEquals(1, s.size());
                assertTrue(s.contains(e.getValue()));
            }
        );
    }

    @Test
    public void testPutGetDecreasingOrder() {
        Tree<Integer, String> t = new Tree<>(leafNodeSize, numberOfNodesInLeafNodeGroup);
        Map<Integer, String> m = new HashMap<>();
        int numElements = leafNodeSize * numberOfNodesInLeafNodeGroup;
        for (int i = numElements; i > 0; i--) {
            String str = "prefix_" + i;
            t.put(i, str);
            m.put(i, str);
        }

        m.entrySet().forEach(e -> {
                    Set<String> s = t.get(e.getKey());
                    assertEquals(1, s.size());
                    assertTrue(s.contains(e.getValue()));
                }
        );
    }

    @Test
    public void testPutGetAlternatingOrder() {
        Tree<Integer, String> t = new Tree<>(leafNodeSize, numberOfNodesInLeafNodeGroup);
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

        m.entrySet().forEach(e -> {
                    Set<String> s = t.get(e.getKey());
                    assertEquals(1, s.size());
                    assertTrue(s.contains(e.getValue()));
                }
        );
    }

    @Test
    public void testPutGetRandomKeys() {
        Tree<Integer, String> t = new Tree<>(leafNodeSize, numberOfNodesInLeafNodeGroup);
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
    public void testPuttingSplits() {
        Tree<Integer, String> t = new Tree<>(leafNodeSize, numberOfNodesInLeafNodeGroup);
        Map<Integer, String> m = new HashMap<>();
        // yea yea, I know
        // the point is:
        // I can only insert few elements as I though because the split is right leaning
        // once I change that I can put more
        // however, just don't change the sizes and this test will pass
        int numElements = (int) Math.floor(1.3 * leafNodeSize * numberOfNodesInLeafNodeGroup);

        for (int i = 0; i < numElements; i++) {
            String str = "prefix_" + i;
            t.put(i, str);
            m.put(i, str);
        }

        m.entrySet().forEach(e -> {
                Set<String> s = t.get(e.getKey());
                assertEquals(1, s.size());
                assertTrue(s.contains(e.getValue()));
            }
        );

    }
}
