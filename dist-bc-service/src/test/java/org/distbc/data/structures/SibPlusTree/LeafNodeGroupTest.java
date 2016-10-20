package org.distbc.data.structures.SibPlusTree;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LeafNodeGroupTest {

    @Test
    public void testInsertAndShiftRightInDifferentNodes() throws Exception {
        int numNodes = 2;
        int nodeSize = 3;
        LeafNodeGroup lng = new LeafNodeGroup(numNodes, nodeSize);
        assertEquals(numNodes, lng.nodes.size());
        lng.nodes.forEach(n -> assertEquals(nodeSize, n.keys.size()));
        lng.nodes.forEach(n -> assertEquals(nodeSize, n.values.size()));

        int offset = 2;
        Integer key1 = 19;
        String value1 = UUID.randomUUID().toString();
        // test inserting at a particular spot
        lng.put(0, offset, key1, value1);
        assertOnValueAtPosition(lng, 0, offset, key1, value1);
        assertTrue(lng.isFull(lng.absolutePosition(0, offset)));
        assertFalse(lng.isFull(lng.absolutePosition(1, 0)));

        Integer key2 = 18;
        String value2 = UUID.randomUUID().toString();
        lng.put(0, offset, key2, value2);
        assertOnValueAtPosition(lng, 0, offset, key2, value2);
        assertOnValueAtPosition(lng, 1, 0, key1, value1);
        assertTrue(lng.isFull(lng.absolutePosition(0, offset)));
        assertTrue(lng.isFull(lng.absolutePosition(1, 0)));
    }

    @Test
    public void testInsertAndShiftRightAcrossMultipleNodes() throws Exception {
        int numNodes = 4;
        int nodeSize = 3;
        LeafNodeGroup lng = new LeafNodeGroup(numNodes, nodeSize);
        assertEquals(numNodes, lng.nodes.size());
        lng.nodes.forEach(n -> assertEquals(nodeSize, n.keys.size()));
        lng.nodes.forEach(n -> assertEquals(nodeSize, n.values.size()));
        Map<Integer, String> keyToValue = new HashMap<>();

        int offset = 2;
        for (int i = 0; i < 9; i++) {
            Integer key = 19 - i;
            String value = UUID.randomUUID().toString();
            // test inserting at a particular spot
            lng.put(0, offset, key, value);
            keyToValue.put(key, value);
        }

        for (int i = 0; i < 2; i++) {
            assertFalse(lng.isFull(i));
        }

        for (int i = 2; i < 11; i++) {
            assertTrue(lng.isFull(i));
            assertEquals(keyToValue.get(i + 9), lng.getValue(i / nodeSize, i % nodeSize));
        }

        for (int i = 11; i <= 12; i++) {
            assertFalse(lng.isFull(i));
        }
    }

    @Test
    public void testInsertAndShiftRightAcrossMultipleNodesThenFillGap() throws Exception {
        int numNodes = 4;
        int nodeSize = 3;
        LeafNodeGroup lng = new LeafNodeGroup(numNodes, nodeSize);
        assertEquals(numNodes, lng.nodes.size());
        lng.nodes.forEach(n -> assertEquals(nodeSize, n.keys.size()));
        lng.nodes.forEach(n -> assertEquals(nodeSize, n.values.size()));
        Map<Integer, String> keyToValue = new HashMap<>();

        int offset = 2;
        for (int i = 0; i < 9; i++) {
            Integer key = 19 - i;
            String value = UUID.randomUUID().toString();
            // test inserting at a particular spot
            lng.put(0, offset, key, value);
            keyToValue.put(key, value);
        }

        for (int i = 0; i < 2; i++) {
            assertFalse(lng.isFull(i));
        }

        for (int i = 2; i < 11; i++) {
            assertTrue(lng.isFull(i));
            assertEquals(keyToValue.get(i + 9), lng.getValue(i / nodeSize, i % nodeSize));
        }

        for (int i = 11; i <= 12; i++) {
            assertFalse(lng.isFull(i));
        }

        // now delete one item from the middle somewhere and see the gap filled
        assertTrue(lng.isFull(lng.absolutePosition(2, 2)));
        lng.delete(2, 2);
        assertFalse(lng.isFull(lng.absolutePosition(2, 2)));
        int key = 10;
        String value = UUID.randomUUID().toString();
        lng.put(0, offset, key, value);
        assertTrue(lng.isFull(lng.absolutePosition(2, 2)));
        assertEquals(value, lng.getValue(0, offset));
        assertEquals(keyToValue.get(16), lng.getValue(2, 2));
    }

    @Test
    public void testShiftRightInSameNode() throws Exception {
        int numNodes = 2;
        int nodeSize = 3;
        LeafNodeGroup lng = new LeafNodeGroup(numNodes, nodeSize);
        assertEquals(numNodes, lng.nodes.size());
        lng.nodes.forEach(n -> assertEquals(nodeSize, n.keys.size()));
        lng.nodes.forEach(n -> assertEquals(nodeSize, n.values.size()));

        int offset = 1;
        Integer key1 = 19;
        String value1 = UUID.randomUUID().toString();
        // test inserting at a particular spot
        lng.put(0, offset, key1, value1);
        assertOnValueAtPosition(lng, 0, offset, key1, value1);
        assertTrue(lng.isFull(lng.absolutePosition(0, offset)));
        assertFalse(lng.isFull(lng.absolutePosition(0, offset + 1)));

        Integer key2 = 18;
        String value2 = UUID.randomUUID().toString();
        lng.put(0, offset, key2, value2);
        assertOnValueAtPosition(lng, 0, offset, key2, value2);
        assertOnValueAtPosition(lng, 0, offset + 1, key1, value1);
        assertTrue(lng.isFull(lng.absolutePosition(0, offset)));
        assertTrue(lng.isFull(lng.absolutePosition(0, offset + 1)));
    }

    @Test
    public void testInsertAndDelete() throws Exception {
        int numNodes = 2;
        int nodeSize = 3;
        LeafNodeGroup lng = new LeafNodeGroup(numNodes, nodeSize);
        assertEquals(numNodes, lng.nodes.size());
        lng.nodes.forEach(n -> assertEquals(nodeSize, n.keys.size()));
        lng.nodes.forEach(n -> assertEquals(nodeSize, n.values.size()));

        int offset = 1;
        Integer key1 = 19;
        String value1 = UUID.randomUUID().toString();
        // test inserting at a particular spot
        lng.put(0, offset, key1, value1);
        assertOnValueAtPosition(lng, 0, offset, key1, value1);

        assertEquals(value1, lng.getValue(0, offset));
        assertTrue(lng.isFull(lng.absolutePosition(0, offset)));

        lng.delete(0, offset);
        assertOnValueAtPosition(lng, 0, offset, null, null);
        assertFalse(lng.isFull(lng.absolutePosition(0, offset)));
        lng.nodes.forEach(n -> assertEquals(nodeSize, n.keys.size()));
        lng.nodes.forEach(n -> assertEquals(nodeSize, n.values.size()));
    }

    @Test
    public void testSplit() throws Exception {
        int numNodes = 3;
        int nodeSize = 2;
        LeafNodeGroup oldNode = new LeafNodeGroup(numNodes, nodeSize);
        assertEquals(numNodes, oldNode.nodes.size());
        oldNode.nodes.forEach(n -> assertEquals(nodeSize, n.keys.size()));
        oldNode.nodes.forEach(n -> assertEquals(nodeSize, n.values.size()));
        Map<Integer, String> keysToValue = new HashMap<>();

        for (int i = 0; i < nodeSize * numNodes; i++) {
            Integer key = 19 - i;
            String value = UUID.randomUUID().toString();
            assertTrue(oldNode.hasSpace());
            oldNode.put(0, 0, key, value);
            assertEquals((numNodes * nodeSize) - (i + 1), oldNode.numEmptySlots);
            keysToValue.put(key, value);
        }


        assertFalse(oldNode.hasSpace());
        assertEquals(0, oldNode.numEmptySlots);
        LeafNodeGroup newNode = oldNode.split();
        assertTrue(oldNode.hasSpace());
        assertTrue(newNode.hasSpace());
        assertEquals(4, oldNode.numEmptySlots);
        assertEquals(2, newNode.numEmptySlots);
        assertEquals(numNodes, oldNode.nodes.size());
        assertEquals(numNodes, newNode.nodes.size());

        int lowestKey = 19 - (nodeSize * numNodes) + 1;
        for (int i = 0; i < nodeSize * numNodes; i++) {
            if (i < nodeSize) {
                assertEquals(keysToValue.get(lowestKey + i), oldNode.getValue(0, i));
            } else {
                int absoluteOffset = i - nodeSize;
                assertEquals(keysToValue.get(lowestKey + i), newNode.getValue(absoluteOffset / nodeSize, absoluteOffset % nodeSize));
            }
        }

        // verify the old node
        for (int i = 0; i < numNodes * nodeSize; i++) {
            if (i < nodeSize) {
                assertTrue(oldNode.isFull(i));
            } else {
                assertFalse(oldNode.isFull(i));
            }
        }
        // verify the new node
        for (int i = 0; i < numNodes * nodeSize; i++) {
            if (i < 2 * nodeSize) {
                assertTrue(newNode.isFull(i));
            } else {
                assertFalse(newNode.isFull(i));
            }
        }
    }

    private void assertOnValueAtPosition(LeafNodeGroup lng, int nodeIndex, int nodeOffset, Integer key, String value) {
        assertEquals(lng.nodes.get( nodeIndex ).keys.get( nodeOffset ), key);
        assertEquals(lng.nodes.get( nodeIndex ).values.get( nodeOffset ), value);
    }
}
