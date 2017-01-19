package org.distbc.data.structures.sibplustree;

import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SibPlusTreeTest {
//    private static final Logger logger = Logger.getLogger(SibPlusTreeTest.class);

    private int leafNodeSize = 3;
    private int numberOfNodesInLeafNodeGroup = 3;

    @Before
    public void before() {
        // FIXME: configure a proper log4j.properties
        BasicConfigurator.configure();
    }

    @Test
    public void testCreateAndToString() throws Exception {
        SibPlusTree<Integer, String> tree = new SibPlusTree<>(leafNodeSize, numberOfNodesInLeafNodeGroup);
        System.err.println(tree.toString());
    }

    @Test
    public void testPutGetIncreasingOrder() {
        SibPlusTree<Integer, String> t = new SibPlusTree<>(leafNodeSize, numberOfNodesInLeafNodeGroup);
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
    public void testPutGetSameKey() {
        SibPlusTree<Integer, String> t = new SibPlusTree<>(leafNodeSize, numberOfNodesInLeafNodeGroup);
        t.put(19, "prefix_19");
        t.put(19, "prefix_20");
        t.put(19, "prefix_21");
        Set<String> rs = t.get(19);
        assertEquals(3, rs.size());
        assertTrue(rs.contains("prefix_19"));
        assertTrue(rs.contains("prefix_20"));
        assertTrue(rs.contains("prefix_21"));

        rs = t.get(20);
        assertTrue(rs.isEmpty());
    }

    @Test
    public void testPutGetDecreasingOrder() {
        SibPlusTree<Integer, String> t = new SibPlusTree<>(leafNodeSize, numberOfNodesInLeafNodeGroup);
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
        SibPlusTree<Integer, String> t = new SibPlusTree<>(leafNodeSize, numberOfNodesInLeafNodeGroup);
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
        SibPlusTree<Integer, String> t = new SibPlusTree<>(leafNodeSize, numberOfNodesInLeafNodeGroup);
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
    @Ignore
    public void testSplittingSplits() {
        SibPlusTree<Integer, String> t = new SibPlusTree<>(leafNodeSize, numberOfNodesInLeafNodeGroup);
        Map<Integer, String> m = new HashMap<>();
        // yea yea, I know
        // the point is:
        // I can only insert few elements as I though because the split is right leaning
        // once I change that I can put more
        // however, just don't change the sizes and this test will pass
        int numElements = (int) Math.floor(10 * leafNodeSize * numberOfNodesInLeafNodeGroup);

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
