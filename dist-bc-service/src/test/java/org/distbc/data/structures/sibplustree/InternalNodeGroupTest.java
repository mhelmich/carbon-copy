package org.distbc.data.structures.sibplustree;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@Ignore
public class InternalNodeGroupTest {
    private final int nodeSize = 2;
    private final int numNodes = 3;

    @Test
    public void testSplit() {
        InternalNodeGroup<Integer> ing = getFullInternNodeGroup();
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

    @Test
    public void testShiftOneNodeRight() {
        NodeIdxAndIdx theBeginning = NodeIdxAndIdx.of(0, 0);
        NodeIdxAndIdx makeEmpty = NodeIdxAndIdx.of(1, 0);
        InternalNodeGroup<Integer> ing = getFullInternNodeGroup();
        ing.put(makeEmpty, null);
        ing.put(NodeIdxAndIdx.of(1, 1), null);
        ing.setChildNodeOnNode(1, null);

        NodeIdxAndIdx isEmpty = ing.findNodeIndexOfEmptyNodeFrom(theBeginning);
        assertEquals(makeEmpty, isEmpty);

        ing.shiftNodesOneRight(theBeginning, isEmpty);

        assertEquals(theBeginning, ing.findNodeIndexOfEmptyNodeFrom(theBeginning));
        assertEquals(NodeIdxAndIdx.INVALID, ing.findClosestEmptySlotFrom(NodeIdxAndIdx.of(1, 0)));

        assertNotNull(ing.getChildForNode(1));
        assertNull(ing.getChildForNode(0));
    }

    private InternalNodeGroup<Integer> getFullInternNodeGroup(int level) {
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

    private InternalNodeGroup<Integer> getFullInternNodeGroup() {
        return getFullInternNodeGroup(1);
    }
}
