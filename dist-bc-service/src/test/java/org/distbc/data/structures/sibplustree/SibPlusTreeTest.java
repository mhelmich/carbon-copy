package org.distbc.data.structures.sibplustree;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SibPlusTreeTest {
    private static final Logger logger = Logger.getLogger(SibPlusTreeTest.class);

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

        m.entrySet().forEach(e -> {
                Set<String> s = t.get(e.getKey());
                assertEquals(1, s.size());
                assertTrue(s.contains(e.getValue()));
            }
        );
    }

}
