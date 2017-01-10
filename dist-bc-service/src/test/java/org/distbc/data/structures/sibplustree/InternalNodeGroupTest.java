package org.distbc.data.structures.sibplustree;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class InternalNodeGroupTest {
    private final int nodeSize = 2;
    private final int numNodes = 3;

    @Test
    public void testSplit() {
        InternalNodeGroup<Integer> ing = new InternalNodeGroup<>(1, nodeSize, numNodes);
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

        InternalNodeGroup<Integer> newIng = ing.split();

        assertEquals(NodeIdxAndIdx.of(2, 0), ing.findClosestEmptySlotFrom(NodeIdxAndIdx.of(0, 0)));
        assertFalse(ing.isNodeEmpty(0));
        assertFalse(ing.isNodeEmpty(1));
        assertTrue(ing.isNodeEmpty(2));

        assertEquals(NodeIdxAndIdx.of(1, 0), newIng.findClosestEmptySlotFrom(NodeIdxAndIdx.of(0, 0)));
        assertFalse(newIng.isNodeEmpty(0));
        assertTrue(newIng.isNodeEmpty(1));
        assertTrue(newIng.isNodeEmpty(2));

        assertNotNull(ing.getChildForNode(0));
        assertNotNull(ing.getChildForNode(1));
        assertNull(ing.getChildForNode(2));

        assertNotNull(newIng.getChildForNode(0));
        assertNull(newIng.getChildForNode(1));
        assertNull(newIng.getChildForNode(2));
    }

//    @Test
//    public void testShiftWithinNode() {
//        InternalNodeGroup<Integer> ing = new InternalNodeGroup<>(1, nodeSize, numNodes);
//        ing.put(1, 17);
//        assertTrue(ing.isFull(1));
//        assertEquals((nodeSize * numNodes) - 1, ing.getEmptySlots());
//
//        ing.shiftOneRight(1, 2);
//        assertFalse(ing.isFull(1));
//        assertTrue(ing.isFull(2));
//
//        assertEquals(Integer.valueOf(17), ing.getKey(2));
//        assertEquals((nodeSize * numNodes) - 1, ing.getEmptySlots());
//    }
//
//    @Test
//    public void testShiftAcrossNodes() {
//        InternalNodeGroup<Integer> ing = new InternalNodeGroup<>(1, nodeSize, numNodes);
//        ing.put(2, 17);
//        assertTrue(ing.isFull(2));
//        assertEquals((nodeSize * numNodes) - 1, ing.getEmptySlots());
//
//        ing.shiftOneRight(2, 3);
//        assertFalse(ing.isFull(2));
//        assertTrue(ing.isFull(3));
//
//        assertEquals(Integer.valueOf(17), ing.getKey(3));
//        assertEquals((nodeSize * numNodes) - 1, ing.getEmptySlots());
//    }
//
//    @Test
//    public void testShiftingAGroup() {
//        InternalNodeGroup<Integer> ing = new InternalNodeGroup<>(1, nodeSize, numNodes);
//        ing.put(2, 17);
//        ing.put(3, 18);
//        ing.put(4, 19);
//        ing.put(5, 20);
//
//        assertTrue(ing.isFull(2));
//        assertTrue(ing.isFull(3));
//        assertTrue(ing.isFull(4));
//        assertTrue(ing.isFull(5));
//        assertFalse(ing.isFull(6));
//
//        ing.shiftOneRight(2, 6);
//
//        assertFalse(ing.isFull(2));
//        assertTrue(ing.isFull(3));
//        assertTrue(ing.isFull(4));
//        assertTrue(ing.isFull(5));
//        assertTrue(ing.isFull(6));
//    }
//
//    @Test
//    public void testFindNextEmptyNode() {
//        InternalNodeGroup<Integer> ing = new InternalNodeGroup<>(1, nodeSize, numNodes);
//        for (int i = 0; i < nodeSize; i++) {
//            assertTrue(ing.hasEmptySlots());
//            int idx = nodeSize + i;
//            ing.put(idx, i * 3);
//            assertTrue(ing.isFull(idx));
//        }
//
//        assertEquals(6, ing.findIndexOfEmptyNodeFrom(nodeSize + 1));
//
//        for (int i = 3 * nodeSize; i < nodeSize * numNodes; i++) {
//            assertTrue(ing.hasEmptySlots());
//            ing.put(i, i * 3);
//            assertTrue(ing.isFull(i));
//        }
//
//        assertEquals(-1, ing.findIndexOfEmptyNodeFrom(10));
//    }
}
