package org.distbc.data.structures.cct;

import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TreeTest {

    private int leafNodeSize = 3;
    private int numberOfNodesInLeafNodeGroup = 3;
    private int internalNodeSize = 2;
    private int numberOfNodesInInternalNodeGroup = 2;

    @Test
    public void testBasic() {
        String value = "prefix_1";
        Tree<Integer, String> t = new Tree<>(leafNodeSize, numberOfNodesInLeafNodeGroup);
        t.put(1, value);
        assertTrue(t.get(1).contains(value));
    }

    @Test
    public void testSearch() {
        List<InternalNodeGroup<Integer>> nodeTrace = new LinkedList<>();
        InternalNodeGroup<Integer> ing = new InternalNodeGroup<>(1,internalNodeSize, numberOfNodesInInternalNodeGroup);

        LeafNodeGroup<Integer, String> lng1 = getFullLeafNodeGroup(1);
        LeafNodeGroup<Integer, String> lng2 = getFullLeafNodeGroup(10);

        ing.setChildNodeOnNode(0, lng1);
        ing.setChildNodeOnNode(1, lng2);

        Tree<Integer, String> t = new Tree<>(leafNodeSize, numberOfNodesInLeafNodeGroup, ing);
        LeafNodeGroup<Integer, String> lng = t.searchLeafNodeGroup(9, ing, nodeTrace);
        assertEquals(lng1, lng);

        Set<String> rs = t.get(9);
        assertEquals(1, rs.size());
        assertTrue(rs.contains("prefix_9"));
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
            int num = i;
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
    public void testPutGetDecreasingOrder() {
        Tree<Integer, String> t = new Tree<>(leafNodeSize, numberOfNodesInLeafNodeGroup);
        Map<Integer, String> m = new HashMap<>();
        int numElements = leafNodeSize * numberOfNodesInLeafNodeGroup;
        for (int i = numElements; i > 0; i--) {
            int num = i;
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

    }

    private LeafNodeGroup<Integer, String> getFullLeafNodeGroup(int seed) {
        LeafNodeGroup<Integer, String> lng = new LeafNodeGroup<>(leafNodeSize, numberOfNodesInLeafNodeGroup);
        for (int i = 0; i < leafNodeSize * numberOfNodesInLeafNodeGroup; i++) {
            assertTrue(lng.hasEmptySlots());
            int n = (seed + i) * 3;
            lng.put(i, n, "prefix_" + n);
            assertTrue(lng.isFull(i));
        }
        return lng;
    }
}
