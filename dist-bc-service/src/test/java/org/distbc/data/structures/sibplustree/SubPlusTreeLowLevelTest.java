package org.distbc.data.structures.sibplustree;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SubPlusTreeLowLevelTest {
    private int leafNodeSize = 3;
    private int numberOfNodesInLeafNodeGroup = 3;

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
        assertNull(ing2.getKey(0, 1));

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
    }
}
