/*
 *
 *  Copyright 2017 Marco Helmich
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.carbon.copy.data.structures.experimental.sibplustree;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@Ignore
public class SibPlusTreeTest {
//    private static final Logger logger = Logger.getLogger(SibPlusTreeTest.class);

    private int leafNodeSize = 3;
    private int numberOfNodesInLeafNodeGroup = 3;
    private String prefix = "narf_";

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

        m.forEach((key, value) -> {
            Set<String> s = t.get(key);
            assertEquals(1, s.size());
            assertTrue(s.contains(value));
        });
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

        m.forEach((key, value) -> {
            Set<String> s = t.get(key);
            assertEquals(1, s.size());
            assertTrue(s.contains(value));
        });
    }

    @Test
    @Ignore
    public void testSplits() throws IllegalAccessException {
        int count = 35;
        SibPlusTree<Integer, String> t = new SibPlusTree<>(leafNodeSize, numberOfNodesInLeafNodeGroup);
        for (int i = 0; i <= count; i++) {
            t.put(i, UUID.randomUUID().toString());
        }

        t.put(7, UUID.randomUUID().toString());

        InternalNodeGroup<Integer> root = getRoot(t);
        assertNotNull(root);
        assertEquals(Integer.valueOf(17), root.getKey(NodeIdxAndIdx.of(0, 0)));

        InternalNodeGroup<Integer> ing2 = (InternalNodeGroup<Integer>) root.getChildForNode(0);
        assertNotNull(ing2);
        assertNull(ing2.getKey(NodeIdxAndIdx.of(0, 0)));
        assertEquals(Integer.valueOf(17), ing2.getKey(NodeIdxAndIdx.of(0, 1)));

        InternalNodeGroup<Integer> ing1 = (InternalNodeGroup<Integer>) ing2.getChildForNode(0);
        assertNotNull(ing1);
        assertEquals(Integer.valueOf(2), ing1.getKey(NodeIdxAndIdx.of(0, 0)));
        assertEquals(Integer.valueOf(5), ing1.getKey(NodeIdxAndIdx.of(0, 1)));
        assertEquals(Integer.valueOf(8), ing1.getKey(NodeIdxAndIdx.of(1, 0)));

        Set<String> rs = t.get(7);
        assertEquals(2, rs.size());
    }

    @Test
    public void testSplitMultipleLevels() throws IllegalAccessException {
        SibPlusTree<Integer, String> t = new SibPlusTree<>(leafNodeSize, numberOfNodesInLeafNodeGroup);
        InternalNodeGroup<Integer> ing3 = getFullInternNodeGroup(3);
        InternalNodeGroup<Integer> ing2 = getFullInternNodeGroup(2);
        InternalNodeGroup<Integer> ing1 = getFullInternNodeGroup(1);
        LeafNodeGroup<Integer, String> lng = getFullLeafNodeGroup(t);
        ing1.setChildNodeOnNode(1, lng);
        ing2.setChildNodeOnNode(1, ing1);
        ing3.setChildNodeOnNode(1, ing2);

        setRootNode(t, ing3);

        List<Breadcrumb<Integer>> breadcrumbs = new ArrayList<>();
        breadcrumbs.add(Breadcrumb.of(ing3, NodeIdxAndIdx.of(1, 1)));
        breadcrumbs.add(Breadcrumb.of(ing2, NodeIdxAndIdx.of(1, 1)));
        breadcrumbs.add(Breadcrumb.of(ing1, NodeIdxAndIdx.of(1, 1)));
        t.splitNodes(ing3, breadcrumbs);
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

        m.forEach((key, value) -> {
            Set<String> s = t.get(key);
            assertEquals(1, s.size());
            assertTrue(s.contains(value));
        });
    }

    private InternalNodeGroup<Integer> getFullInternNodeGroup(int level) {
        int nodeSize = leafNodeSize - 1;
        int numNodes = numberOfNodesInLeafNodeGroup - 1;
        InternalNodeGroup<Integer> ing = new InternalNodeGroup<>(level, nodeSize, numNodes);
        for (int i = 0; i < numNodes; i++) {
            for (int j = 0; j < nodeSize; j++) {
                NodeIdxAndIdx here = NodeIdxAndIdx.of(i, j);
                assertEquals(here, ing.findClosestEmptySlotFrom(here));
                ing.put(here, ((i * nodeSize) + j) * 3);
                assertEquals(ing.plusOne(here), ing.findClosestEmptySlotFrom(here));
            }

            @SuppressWarnings("unchecked")
            NodeGroup<Integer> ng = (NodeGroup<Integer>) Mockito.mock(NodeGroup.class);
            ing.setChildNodeOnNode(i, ng);
        }

        return ing;
    }

    private LeafNodeGroup<Integer, String> getFullLeafNodeGroup(SibPlusTree<Integer, String> t) {
        LeafNodeGroup<Integer, String> lng = t.newLeafNodeGroup();
        NodeIdxAndIdx p = NodeIdxAndIdx.of(0, 0);
        while (!NodeIdxAndIdx.INVALID.equals(p)) {
            assertEquals(p, lng.findClosestEmptySlotFrom(p));
            lng.put(p, ((p.nodeIdx * leafNodeSize) + p.idx) * 3, prefix + p.toString());
            assertEquals(p, lng.findClosestFullSlotFrom(p));
            p = lng.plusOne(p);
        }

        assertEquals(NodeIdxAndIdx.INVALID, lng.findClosestEmptySlotFrom(NodeIdxAndIdx.of(0, 0)));
        return lng;
    }

    private void setRootNode(SibPlusTree<Integer, String> t, InternalNodeGroup<Integer> root) throws IllegalAccessException {
        FieldUtils.writeField(t, "root", root, true);
    }

    @SuppressWarnings("unchecked")
    private InternalNodeGroup<Integer> getRoot(SibPlusTree<Integer, String> t) throws IllegalAccessException {
        return (InternalNodeGroup<Integer>) FieldUtils.readField(t, "root", true);
    }
}
