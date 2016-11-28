package org.distbc.data.structures.cct;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

    private LeafNodeGroup<Integer, String> getFullLeafNodeGroup() {
        LeafNodeGroup<Integer, String> lng = new LeafNodeGroup<>(nodeSize, numNodes);
        for (int i = 0; i < numNodes * nodeSize; i++) {
            assertTrue(lng.hasEmptySlots());
            lng.put(i, i, "prefix_" + i);
            assertTrue(lng.isFull(i));
        }
        return lng;
    }
}
