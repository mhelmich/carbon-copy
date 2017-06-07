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
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@Ignore
public class SibPlusTreeLowLevelTest {
    private int leafNodeSize = 3;
    private int numberOfNodesInLeafNodeGroup = 3;
    private String prefix = "narf_";

    @Test
    public void testDoHighKeyBusiness() {
        SibPlusTree<Integer, String> t = new SibPlusTree<>(leafNodeSize, numberOfNodesInLeafNodeGroup);

        List<Breadcrumb<Integer>> breadcrumbs = new ArrayList<>();

        InternalNodeGroup<Integer> ing1 = t.newInternalNodeGroup(2);
        InternalNodeGroup<Integer> ing2 = t.newInternalNodeGroup(1);
        LeafNodeGroup<Integer, String> lng = t.newLeafNodeGroup();

        lng.put(NodeIdxAndIdx.of(0, 2), 99, UUID.randomUUID().toString());
        lng.put(NodeIdxAndIdx.of(1, 2), 111, UUID.randomUUID().toString());
        lng.put(NodeIdxAndIdx.of(2, 2), 222, UUID.randomUUID().toString());

        ing1.setChildNodeOnNode(1, ing2);
        ing2.setChildNodeOnNode(0, lng);

        breadcrumbs.add(Breadcrumb.of(ing1, NodeIdxAndIdx.of(0, 1)));
        breadcrumbs.add(Breadcrumb.of(ing2, NodeIdxAndIdx.of(0, 0)));

        NodeIdxAndIdx insertionIdx = NodeIdxAndIdx.of(0, 2);
        NodeIdxAndIdx emptyIdx = NodeIdxAndIdx.of(0, 2);
        t.doHighKeyBusiness(breadcrumbs, insertionIdx, emptyIdx);

        insertionIdx = NodeIdxAndIdx.of(2, 2);
        emptyIdx = NodeIdxAndIdx.of(2, 2);
        t.doHighKeyBusiness(breadcrumbs, insertionIdx, emptyIdx);

        assertEquals(Integer.valueOf(222), ing1.getKey(0, 1));
        assertEquals(Integer.valueOf(99), ing2.getKey(0, 0));
        assertEquals(Integer.valueOf(111), ing2.getKey(0, 1));

        ///////////////////////////////////////
        /////////////////////////////
        //////////////////
        // second case
        breadcrumbs.clear();

        breadcrumbs.add(Breadcrumb.of(ing1, NodeIdxAndIdx.of(0, 0)));
        breadcrumbs.add(Breadcrumb.of(ing2, NodeIdxAndIdx.of(0, 1)));

        insertionIdx = NodeIdxAndIdx.of(1, 2);
        emptyIdx = NodeIdxAndIdx.of(1, 2);
        t.doHighKeyBusiness(breadcrumbs, insertionIdx, emptyIdx);

        assertEquals(Integer.valueOf(222), ing1.getKey(0, 1));
        assertEquals(Integer.valueOf(99), ing2.getKey(0, 0));
        assertEquals(Integer.valueOf(111), ing2.getKey(0, 1));

        ///////////////////////////////////////
        /////////////////////////////
        //////////////////
        // third case
        breadcrumbs.clear();

        ing1 = t.newInternalNodeGroup(1);
        ing1.setChildNodeOnNode(1, lng);
        ing2 = t.newInternalNodeGroup(2);
        ing2.setChildNodeOnNode(1, ing1);
        InternalNodeGroup<Integer> ing3 = t.newInternalNodeGroup(3);
        ing3.setChildNodeOnNode(0, ing2);

        breadcrumbs.add(Breadcrumb.of(ing3, NodeIdxAndIdx.of(0, 0)));
        breadcrumbs.add(Breadcrumb.of(ing2, NodeIdxAndIdx.of(1, 1)));
        breadcrumbs.add(Breadcrumb.of(ing1, NodeIdxAndIdx.of(1, 1)));

        insertionIdx = NodeIdxAndIdx.of(2, 2);
        emptyIdx = NodeIdxAndIdx.of(2, 2);
        t.doHighKeyBusiness(breadcrumbs, insertionIdx, emptyIdx);

        assertNull(ing1.getKey(0, 1));
        assertNull(ing2.getKey(0, 0));
        assertEquals(Integer.valueOf(222), ing2.getKey(1, 1));
    }

    @Test
    public void testFourLevels() {
        SibPlusTree<Integer, String> t = new SibPlusTree<>(leafNodeSize, numberOfNodesInLeafNodeGroup);

        InternalNodeGroup<Integer> ing4 = t.newInternalNodeGroup(4);
        InternalNodeGroup<Integer> ing3 = t.newInternalNodeGroup(3);
        InternalNodeGroup<Integer> ing2 = t.newInternalNodeGroup(2);
        InternalNodeGroup<Integer> ing1 = t.newInternalNodeGroup(1);
        LeafNodeGroup<Integer, String> lng = getFullLeafNodeGroup(t);

        ing1.setChildNodeOnNode(1, lng);
        ing2.setChildNodeOnNode(1, ing1);
        ing3.setChildNodeOnNode(1, ing2);
        ing4.setChildNodeOnNode(1, ing3);

        ///////////////////////////////////////
        /////////////////////////////
        //////////////////
        // first case
        List<Breadcrumb<Integer>> breadcrumbs = new ArrayList<>(4);
        breadcrumbs.add(Breadcrumb.of(ing3, NodeIdxAndIdx.of(1, 1)));
        breadcrumbs.add(Breadcrumb.of(ing2, NodeIdxAndIdx.of(1, 1)));
        breadcrumbs.add(Breadcrumb.of(ing1, NodeIdxAndIdx.of(1, 0)));
        NodeIdxAndIdx insertionIdx = NodeIdxAndIdx.of(0, 2);
        NodeIdxAndIdx emptyIdx = NodeIdxAndIdx.of(0, 2);
        t.doHighKeyBusiness(breadcrumbs, insertionIdx, emptyIdx);

        assertEquals(Integer.valueOf(6), ing1.getKey(NodeIdxAndIdx.of(1, 0)));
        assertEquals(Integer.valueOf(15), ing1.getKey(NodeIdxAndIdx.of(1, 1)));
        assertEquals(Integer.valueOf(24), ing2.getKey(NodeIdxAndIdx.of(1, 1)));
        assertNull(ing3.getKey(NodeIdxAndIdx.of(1, 1)));

        ///////////////////////////////////////
        /////////////////////////////
        //////////////////
        // second case
        breadcrumbs.clear();
        breadcrumbs.add(Breadcrumb.of(ing4, NodeIdxAndIdx.of(1, 1)));
        breadcrumbs.add(Breadcrumb.of(ing3, NodeIdxAndIdx.of(1, 1)));
        breadcrumbs.add(Breadcrumb.of(ing2, NodeIdxAndIdx.of(1, 1)));
        breadcrumbs.add(Breadcrumb.of(ing1, NodeIdxAndIdx.of(1, 2)));
        insertionIdx = NodeIdxAndIdx.of(2, 2);
        emptyIdx = NodeIdxAndIdx.of(2, 2);
        t.doHighKeyBusiness(breadcrumbs, insertionIdx, emptyIdx);

        assertEquals(Integer.valueOf(6), ing1.getKey(NodeIdxAndIdx.of(1, 0)));
        assertEquals(Integer.valueOf(15), ing1.getKey(NodeIdxAndIdx.of(1, 1)));
        assertNull(ing2.getKey(NodeIdxAndIdx.of(1, 0)));
        assertEquals(Integer.valueOf(24), ing2.getKey(NodeIdxAndIdx.of(1, 1)));
        assertNull(ing3.getKey(NodeIdxAndIdx.of(1, 0)));
        assertNull(ing3.getKey(NodeIdxAndIdx.of(1, 1)));
        assertNull(ing4.getKey(NodeIdxAndIdx.of(1, 1)));
    }

    @Test
    public void testFourLevelsColdStart() {
        SibPlusTree<Integer, String> t = new SibPlusTree<>(leafNodeSize, numberOfNodesInLeafNodeGroup);

        InternalNodeGroup<Integer> ing4 = t.newInternalNodeGroup(4);
        InternalNodeGroup<Integer> ing3 = t.newInternalNodeGroup(3);
        InternalNodeGroup<Integer> ing2 = t.newInternalNodeGroup(2);
        InternalNodeGroup<Integer> ing1 = t.newInternalNodeGroup(1);
        LeafNodeGroup<Integer, String> lng = getFullLeafNodeGroup(t);

        ing1.setChildNodeOnNode(1, lng);
        ing2.setChildNodeOnNode(1, ing1);
        ing3.setChildNodeOnNode(1, ing2);
        ing4.setChildNodeOnNode(1, ing3);

        List<Breadcrumb<Integer>> breadcrumbs = new ArrayList<>(4);
        breadcrumbs.add(Breadcrumb.of(ing4, NodeIdxAndIdx.of(1, 1)));
        breadcrumbs.add(Breadcrumb.of(ing3, NodeIdxAndIdx.of(1, 1)));
        breadcrumbs.add(Breadcrumb.of(ing2, NodeIdxAndIdx.of(1, 1)));
        breadcrumbs.add(Breadcrumb.of(ing1, NodeIdxAndIdx.of(1, 2)));
        NodeIdxAndIdx insertionIdx = NodeIdxAndIdx.of(2, 2);
        NodeIdxAndIdx emptyIdx = NodeIdxAndIdx.of(2, 2);
        t.doHighKeyBusiness(breadcrumbs, insertionIdx, emptyIdx);

        assertNull(ing1.getKey(NodeIdxAndIdx.of(1, 0)));
        assertNull(ing1.getKey(NodeIdxAndIdx.of(1, 1)));
        assertNull(ing2.getKey(NodeIdxAndIdx.of(1, 0)));
        assertEquals(Integer.valueOf(24), ing2.getKey(NodeIdxAndIdx.of(1, 1)));
        assertNull(ing3.getKey(NodeIdxAndIdx.of(1, 0)));
        assertNull(ing3.getKey(NodeIdxAndIdx.of(1, 1)));
        assertNull(ing4.getKey(NodeIdxAndIdx.of(1, 0)));
        assertNull(ing4.getKey(NodeIdxAndIdx.of(1, 1)));
    }

    @Test
    public void testSplits() throws IllegalAccessException {
        int count = 35;
        SibPlusTree<Integer, String> t = new SibPlusTree<>(leafNodeSize, numberOfNodesInLeafNodeGroup);
        for (int i = 0; i <= count; i++) {
            t.put(i, UUID.randomUUID().toString());
        }

        System.err.println(t.toString());

        InternalNodeGroup<Integer> root = getRoot(t);
        List<Breadcrumb<Integer>> breadcrumbs = t.searchTree(7, root);
        LeafNodeGroup<Integer, String> lng = (LeafNodeGroup<Integer, String>) breadcrumbs.get(breadcrumbs.size() - 1).ing.getChildForNode(breadcrumbs.get(breadcrumbs.size() - 1).indexes.nodeIdx);
        t.splitNodes(lng, breadcrumbs);
        System.err.println(t.toString());
        root = getRoot(t);
        System.err.println(root.toString());
    }

    @SuppressWarnings("unchecked")
    private InternalNodeGroup<Integer> getRoot(SibPlusTree<Integer, String> t) throws IllegalAccessException {
        return (InternalNodeGroup<Integer>) FieldUtils.readField(t, "root", true);
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
}
