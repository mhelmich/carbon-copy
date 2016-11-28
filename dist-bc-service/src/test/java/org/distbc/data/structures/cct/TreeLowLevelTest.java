package org.distbc.data.structures.cct;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TreeLowLevelTest {
    private final int nodeSize = 3;
    private final int numNodes = 4;

    @Test
    public void testSplitWithChildNodes() {
        LeafNodeGroup<Integer, String> newLng = null;
        LeafNodeGroup<Integer, String> lng = getFullLeafNodeGroup();
        InternalNodeGroup<Integer> ing = new InternalNodeGroup<>(1, nodeSize, numNodes);
        ing.setChildNode(0, lng);

        if(!lng.hasEmptySlots()) {
            newLng = lng.split();
            // the index is coming from the search
            int nodeIdx = ing.findIndexOfEmptyNodeFrom(3);
            ing.setChildNode(nodeIdx, newLng);
        } else {
            fail();
        }

        int idx = lng.findClosestEmptySlotFrom(0);
        assertEquals(6, idx);
        assertTrue(lng.isFull(0));
        lng.shiftOneRight(0, idx);
        assertFalse(lng.isFull(0));
        lng.put(0, -1, "prefix_-1");

        for (int i = -1; i < (nodeSize * numNodes) / 2; i++) {
            assertEquals(Integer.valueOf(i), lng.getKey(i + 1));
        }

        assertEquals(lng, ing.getChild(0));
        assertEquals(newLng, ing.getChild(nodeSize));
    }

    @Test
    public void testSplitInternalNodeGroups() {
        InternalNodeGroup<Integer> newParentIng = null;
        InternalNodeGroup<Integer> parentIng2 = null;
        InternalNodeGroup<Integer> ing = getFullInternalNodeGroup(1);
        InternalNodeGroup<Integer> parentIng = getFullInternalNodeGroup(2);
        parentIng.setChildNode(0, ing);

        if (!ing.hasEmptySlots()) {
            InternalNodeGroup<Integer> ing2 = ing.split();
            // this node should be full
            int nodeIdx = parentIng.findIndexOfEmptyNodeFrom(0);
            if (nodeIdx < 0) {
                // split the parent node
                parentIng2 = parentIng.split();
                // and create new parent node
                newParentIng = new InternalNodeGroup<>(3, nodeSize, numNodes);
                newParentIng.setChildNodeOnNode(0, parentIng);
                newParentIng.setChildNodeOnNode(1, parentIng2);
            } else {
                fail();
            }

            // find node to shift to
            int to = parentIng.findNodeIndexOfEmptyNodeFrom(1);
            // 0 is ourselves
            // 1 is taken be the next one
            // 2 should be vacated by the split
            assertEquals(2, to);
            // shift
            parentIng.shiftNodesOneRight(1, to);
            for (int i = nodeSize; i < 2 * nodeSize; i++) {
                assertNull(parentIng.getKey(i));
                assertTrue(parentIng.isEmpty(i));
            }

            assertNull(parentIng.getChildForNode(1));
            parentIng.setChildNodeOnNode(1, ing2);
        } else {
            fail();
        }

        assertEquals(parentIng, newParentIng.getChild(0));
        assertEquals(parentIng2, newParentIng.getChild(nodeSize));
        assertEquals(ing, parentIng.getChild(0));
    }

    private LeafNodeGroup<Integer, String> getFullLeafNodeGroup() {
        LeafNodeGroup<Integer, String> lng = new LeafNodeGroup<>(nodeSize, numNodes);
        for (int i = 0; i < numNodes * nodeSize; i++) {
            assertTrue(lng.hasEmptySlots());
            lng.put(i, i, "prefix_" + i);
            assertTrue(lng.isFull(i));
        }
        return lng;
    }

    @SuppressWarnings("unchecked")
    private InternalNodeGroup<Integer> getFullInternalNodeGroup(int level) {
        InternalNodeGroup<Integer> ing = new InternalNodeGroup<>(level, nodeSize, numNodes);
        for (int i = 0; i < numNodes * nodeSize; i++) {
            assertTrue(ing.hasEmptySlots());
            ing.put(i, i);
            assertTrue(ing.isFull(i));
        }
        for (int i = 0; i < numNodes; i++) {
            ing.setChildNode(i * nodeSize, Mockito.mock(InternalNodeGroup.class));
        }
        return ing;
    }
}
