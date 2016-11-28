package org.distbc.data.structures.cct;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class InternalNodeGroupTest {

    private final int nodeSize = 3;
    private final int numNodes = 4;

    @Test
    public void testSplit() {
        InternalNodeGroup<Integer> ing = new InternalNodeGroup<>(1, nodeSize, numNodes);
        for (int i = 0; i < numNodes * nodeSize; i++) {
            assertTrue(ing.hasEmptySlots());
            ing.put(i, i * 3);
            assertTrue(ing.isFull(i));
        }

        assertFalse(ing.hasEmptySlots());

        InternalNodeGroup<Integer> newIng = ing.split();
        assertTrue(ing.hasEmptySlots());
        assertTrue(newIng.hasEmptySlots());
        assertEquals((nodeSize * numNodes) / 2, ing.getEmptySlots());
        assertEquals((nodeSize * numNodes) / 2, newIng.getEmptySlots());

        for (int i = 0; i < numNodes * nodeSize; i++) {
            boolean shouldBeFull = (i < (numNodes * nodeSize) / 2);
            assertEquals(shouldBeFull, ing.isFull(i));
            assertEquals(shouldBeFull, newIng.isFull(i));
        }
    }

    @Test
    public void testSplitWithChildNodes() {
        LeafNodeGroup<Integer, String> lng = getFullLeafNodeGroup();
        InternalNodeGroup<Integer> ing = new InternalNodeGroup<>(1, nodeSize, numNodes);
        ing.setChildNode(0, lng);

        if(!lng.hasEmptySlots()) {
            LeafNodeGroup<Integer, String> newLng = lng.split();
            // the index is coming from the search
            int nodeIdx = ing.findIndexOfEmptyNodeFrom(3);
            ing.setChildNode(nodeIdx, newLng);
        }

        int idx = lng.findClosestEmptySlotFrom(0);
        assertTrue(lng.isFull(0));
        lng.shiftOneRight(0, idx);
        assertFalse(lng.isFull(0));
        lng.put(0, -1, "prefix_-1");
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

    @Test
    public void testShiftWithinNode() {
        InternalNodeGroup<Integer> ing = new InternalNodeGroup<>(1, nodeSize, numNodes);
        ing.put(1, 17);
        assertTrue(ing.isFull(1));

        ing.shiftOneRight(1, 2);
        assertFalse(ing.isFull(1));
        assertTrue(ing.isFull(2));

        assertEquals(Integer.valueOf(17), ing.getKey(2));
        assertEquals((nodeSize * numNodes) - 1, ing.getEmptySlots());
    }

    @Test
    public void testShiftAcrossNodes() {
        InternalNodeGroup<Integer> ing = new InternalNodeGroup<>(1, nodeSize, numNodes);
        ing.put(2, 17);
        assertTrue(ing.isFull(2));

        ing.shiftOneRight(2, 3);
        assertFalse(ing.isFull(2));
        assertTrue(ing.isFull(3));

        assertEquals(Integer.valueOf(17), ing.getKey(3));
        assertEquals((nodeSize * numNodes) - 1, ing.getEmptySlots());
    }

    @Test
    public void testShiftingAGroup() {
        InternalNodeGroup<Integer> ing = new InternalNodeGroup<>(1, nodeSize, numNodes);
        ing.put(2, 17);
        ing.put(3, 18);
        ing.put(4, 19);
        ing.put(5, 20);

        assertTrue(ing.isFull(2));
        assertTrue(ing.isFull(3));
        assertTrue(ing.isFull(4));
        assertTrue(ing.isFull(5));
        assertFalse(ing.isFull(6));

        ing.shiftOneRight(2, 6);

        assertFalse(ing.isFull(2));
        assertTrue(ing.isFull(3));
        assertTrue(ing.isFull(4));
        assertTrue(ing.isFull(5));
        assertTrue(ing.isFull(6));
    }

    @Test
    public void testFindNextEmptyNode() {
        InternalNodeGroup<Integer> ing = new InternalNodeGroup<>(1, nodeSize, numNodes);
        for (int i = 0; i < nodeSize; i++) {
            assertTrue(ing.hasEmptySlots());
            int idx = nodeSize + i;
            ing.put(idx, i * 3);
            assertTrue(ing.isFull(idx));
        }

        assertEquals(6, ing.findIndexOfEmptyNodeFrom(1));

        for (int i = 3 * nodeSize; i < nodeSize * numNodes; i++) {
            assertTrue(ing.hasEmptySlots());
            ing.put(i, i * 3);
            assertTrue(ing.isFull(i));
        }

        assertEquals(-1, ing.findIndexOfEmptyNodeFrom(10));
    }
}
