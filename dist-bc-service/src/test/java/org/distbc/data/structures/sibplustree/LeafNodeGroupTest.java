package org.distbc.data.structures.sibplustree;

import org.junit.Ignore;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@Ignore
public class LeafNodeGroupTest {

    private int leafNodeSize = 3;
    private int numberOfNodesInLeafNodeGroup = 3;

    @Test
    public void testSplit() {
        LeafNodeGroup<Integer, String> lng = new LeafNodeGroup<>(leafNodeSize, numberOfNodesInLeafNodeGroup);
        NodeIdxAndIdx p = NodeIdxAndIdx.of(0, 0);
        while (!NodeIdxAndIdx.INVALID.equals(p)) {
            lng.put(p, p.nodeIdx * leafNodeSize + p.idx, UUID.randomUUID().toString());
            p = lng.plusOne(p);
        }

        LeafNodeGroup<Integer, String> newLng = lng.split();

        assertNotNull(lng.getKey(NodeIdxAndIdx.of(0, 0)));
        assertNotNull(lng.getKey(NodeIdxAndIdx.of(0, 1)));
        assertNotNull(lng.getKey(NodeIdxAndIdx.of(0, 2)));
        assertNotNull(lng.getKey(NodeIdxAndIdx.of(1, 0)));
        assertNotNull(lng.getKey(NodeIdxAndIdx.of(1, 1)));
        assertNotNull(lng.getKey(NodeIdxAndIdx.of(1, 2)));

        assertNull(lng.getKey(NodeIdxAndIdx.of(2, 0)));
        assertNull(lng.getKey(NodeIdxAndIdx.of(2, 1)));
        assertNull(lng.getKey(NodeIdxAndIdx.of(2, 2)));

        assertNotNull(newLng.getKey(NodeIdxAndIdx.of(0, 0)));
        assertNotNull(newLng.getKey(NodeIdxAndIdx.of(0, 1)));
        assertNotNull(newLng.getKey(NodeIdxAndIdx.of(0, 2)));

        assertNull(newLng.getKey(NodeIdxAndIdx.of(1, 0)));
        assertNull(newLng.getKey(NodeIdxAndIdx.of(1, 1)));
        assertNull(newLng.getKey(NodeIdxAndIdx.of(1, 2)));
        assertNull(newLng.getKey(NodeIdxAndIdx.of(2, 0)));
        assertNull(newLng.getKey(NodeIdxAndIdx.of(2, 1)));
        assertNull(newLng.getKey(NodeIdxAndIdx.of(2, 2)));

        assertEquals(lng, newLng.previous);
        assertEquals(newLng, lng.next);
    }

//    @Test
//    public void testInsertAndShiftRightInDifferentNodes() throws Exception {
//        int numNodes = 2;
//        int nodeSize = 3;
//        LeafNodeGroup lng = new LeafNodeGroup(numNodes, nodeSize);
//        assertEquals(numNodes, lng.nodes.size());
//        lng.nodes.forEach(n -> assertEquals(nodeSize, n.keys.size()));
//        lng.nodes.forEach(n -> assertEquals(nodeSize, n.values.size()));
//
//        int offset = 2;
//        Integer key1 = 19;
//        String value1 = UUID.randomUUID().toString();
//        // test inserting at a particular spot
//        lng.put(0, offset, key1, value1);
//        assertOnValueAtPosition(lng, 0, offset, key1, value1);
//        assertTrue(lng.isFull(lng.absolutePosition(0, offset)));
//        assertFalse(lng.isFull(lng.absolutePosition(1, 0)));
//
//        Integer key2 = 18;
//        String value2 = UUID.randomUUID().toString();
//        lng.put(0, offset, key2, value2);
//        assertOnValueAtPosition(lng, 0, offset, key2, value2);
//        assertOnValueAtPosition(lng, 1, 0, key1, value1);
//        assertTrue(lng.isFull(lng.absolutePosition(0, offset)));
//        assertTrue(lng.isFull(lng.absolutePosition(1, 0)));
//    }
//
//    @Test
//    public void testInsertAndShiftLeftAcrossMultipleNodes() throws Exception {
//        int numNodes = 4;
//        int nodeSize = 3;
//        LeafNodeGroup lng = new LeafNodeGroup(numNodes, nodeSize);
//        assertEquals(numNodes, lng.nodes.size());
//        lng.nodes.forEach(n -> assertEquals(nodeSize, n.keys.size()));
//        lng.nodes.forEach(n -> assertEquals(nodeSize, n.values.size()));
//        Map<Integer, String> keyToValue = new HashMap<>();
//
//        int offset = 2;
//        // we search in both directions
//        // hence we need to fill up everything right from our working area
//        // otherwise the search will find closer open spots on the right
//        fillRightFrom(lng, 2, offset);
//        for (int i = (nodeSize * 2) + offset; i < (nodeSize * numNodes); i++) {
//            assertTrue(lng.isFull(i));
//        }
//
//
//        int emptySlotsLeft = (nodeSize * 2) + offset;
//        for (int i = 0; i < emptySlotsLeft; i++) {
//            Integer key = i;
//            String value = key.toString();
//            // test inserting at a particular spot
//            lng.put(2, offset, key, value);
//            keyToValue.put(key, value);
//        }
//
//        for (int i = 0; i < (nodeSize * numNodes); i++) {
//            assertTrue(lng.isFull(i));
//        }
//
//        for (int i = 1; i <= emptySlotsLeft; i++) {
//            assertEquals(keyToValue.get(i -1), lng.getValue(i / nodeSize, i % nodeSize));
//        }
//    }
//
//    @Test
//    public void testInsertAndShiftRightAcrossMultipleNodes() throws Exception {
//        int numNodes = 4;
//        int nodeSize = 3;
//        LeafNodeGroup lng = new LeafNodeGroup(numNodes, nodeSize);
//        assertEquals(numNodes, lng.nodes.size());
//        lng.nodes.forEach(n -> assertEquals(nodeSize, n.keys.size()));
//        lng.nodes.forEach(n -> assertEquals(nodeSize, n.values.size()));
//        Map<Integer, String> keyToValue = new HashMap<>();
//
//        int offset = 2;
//        // we search in both directions
//        // hence we need to fill up everything left from our working area
//        // otherwise the search will find closer open spots on the left
//        fillLeftFrom(lng, 0, offset);
//        for (int i = 0; i < 2; i++) {
//            assertTrue(lng.isFull(i));
//        }
//
//        for (int i = 0; i < 9; i++) {
//            Integer key = 19 - i;
//            String value = UUID.randomUUID().toString();
//            // test inserting at a particular spot
//            lng.put(0, offset, key, value);
//            keyToValue.put(key, value);
//        }
//
//        for (int i = 2; i < 11; i++) {
//            assertTrue(lng.isFull(i));
//            assertEquals(keyToValue.get(i + 9), lng.getValue(i / nodeSize, i % nodeSize));
//        }
//
//        for (int i = 11; i <= 12; i++) {
//            assertFalse(lng.isFull(i));
//        }
//    }
//
//    @Test
//    public void testInsertAndShiftRightAcrossMultipleNodesThenFillGap() throws Exception {
//        int numNodes = 4;
//        int nodeSize = 3;
//        LeafNodeGroup lng = new LeafNodeGroup(numNodes, nodeSize);
//        assertEquals(numNodes, lng.nodes.size());
//        lng.nodes.forEach(n -> assertEquals(nodeSize, n.keys.size()));
//        lng.nodes.forEach(n -> assertEquals(nodeSize, n.values.size()));
//        Map<Integer, String> keyToValue = new HashMap<>();
//
//        int offset = 2;
//        fillLeftFrom(lng, 0, offset);
//        for (int i = 0; i < 2; i++) {
//            assertTrue(lng.isFull(i));
//        }
//
//        for (int i = 0; i < 9; i++) {
//            Integer key = 19 - i;
//            String value = UUID.randomUUID().toString();
//            // test inserting at a particular spot
//            lng.put(0, offset, key, value);
//            keyToValue.put(key, value);
//        }
//
//        for (int i = 2; i < 11; i++) {
//            assertTrue(lng.isFull(i));
//            assertEquals(keyToValue.get(i + 9), lng.getValue(i / nodeSize, i % nodeSize));
//        }
//
//        for (int i = 11; i <= 12; i++) {
//            assertFalse(lng.isFull(i));
//        }
//
//        // now delete one item from the middle somewhere and see the gap filled
//        assertTrue(lng.isFull(lng.absolutePosition(2, 2)));
//        lng.delete(2, 2);
//        assertFalse(lng.isFull(lng.absolutePosition(2, 2)));
//        int key = 10;
//        String value = UUID.randomUUID().toString();
//        lng.put(0, offset, key, value);
//        assertTrue(lng.isFull(lng.absolutePosition(2, 2)));
//        assertEquals(value, lng.getValue(0, offset));
//        assertEquals(keyToValue.get(16), lng.getValue(2, 2));
//    }
//
//    @Test
//    public void testShiftRightInSameNode() throws Exception {
//        int numNodes = 2;
//        int nodeSize = 3;
//        LeafNodeGroup lng = new LeafNodeGroup(numNodes, nodeSize);
//        assertEquals(numNodes, lng.nodes.size());
//        lng.nodes.forEach(n -> assertEquals(nodeSize, n.keys.size()));
//        lng.nodes.forEach(n -> assertEquals(nodeSize, n.values.size()));
//
//        int offset = 1;
//        Integer key1 = 19;
//        String value1 = UUID.randomUUID().toString();
//        // test inserting at a particular spot
//        lng.put(0, offset, key1, value1);
//        assertOnValueAtPosition(lng, 0, offset, key1, value1);
//        assertTrue(lng.isFull(lng.absolutePosition(0, offset)));
//        assertFalse(lng.isFull(lng.absolutePosition(0, offset + 1)));
//
//        Integer key2 = 18;
//        String value2 = UUID.randomUUID().toString();
//        lng.put(0, offset, key2, value2);
//        assertOnValueAtPosition(lng, 0, offset, key2, value2);
//        assertOnValueAtPosition(lng, 0, offset + 1, key1, value1);
//        assertTrue(lng.isFull(lng.absolutePosition(0, offset)));
//        assertTrue(lng.isFull(lng.absolutePosition(0, offset + 1)));
//    }
//
//    @Test
//    public void testShiftLeftInSameNode() throws Exception {
//        int numNodes = 2;
//        int nodeSize = 3;
//        LeafNodeGroup lng = new LeafNodeGroup(numNodes, nodeSize);
//        assertEquals(numNodes, lng.nodes.size());
//        lng.nodes.forEach(n -> assertEquals(nodeSize, n.keys.size()));
//        lng.nodes.forEach(n -> assertEquals(nodeSize, n.values.size()));
//
//        int offset = 1;
//        Integer key1 = 19;
//        String value1 = UUID.randomUUID().toString();
//        // test inserting at a particular spot
//        lng.put(0, offset, key1, value1);
//        assertOnValueAtPosition(lng, 0, offset, key1, value1);
//        assertTrue(lng.isFull(lng.absolutePosition(0, offset)));
//        assertFalse(lng.isFull(lng.absolutePosition(0, offset + 1)));
//
//        Integer key2 = 18;
//        String value2 = UUID.randomUUID().toString();
//        // put also one to the right
//        // the code looks to the right first and then to the left
//        lng.put(0, offset, key2, value2);
//        assertOnValueAtPosition(lng, 0, offset, key2, value2);
//        assertOnValueAtPosition(lng, 0, offset + 1, key1, value1);
//        assertTrue(lng.isFull(lng.absolutePosition(0, offset)));
//        assertTrue(lng.isFull(lng.absolutePosition(0, offset + 1)));
//
//        Integer key3 = 17;
//        String value3 = UUID.randomUUID().toString();
//        // put also one to the right
//        // the code looks to the right first and then to the left
//        lng.put(0, offset, key3, value3);
//        assertOnValueAtPosition(lng, 0, offset, key3, value3);
//        assertOnValueAtPosition(lng, 0, offset + 1, key1, value1);
//        assertOnValueAtPosition(lng, 0, offset - 1, key2, value2);
//        assertTrue(lng.isFull(lng.absolutePosition(0, offset)));
//        assertTrue(lng.isFull(lng.absolutePosition(0, offset + 1)));
//        assertTrue(lng.isFull(lng.absolutePosition(0, offset - 1)));
//    }
//
//    @Test
//    public void testInsertAndDelete() throws Exception {
//        int numNodes = 2;
//        int nodeSize = 3;
//        LeafNodeGroup lng = new LeafNodeGroup(numNodes, nodeSize);
//        assertEquals(numNodes, lng.nodes.size());
//        lng.nodes.forEach(n -> assertEquals(nodeSize, n.keys.size()));
//        lng.nodes.forEach(n -> assertEquals(nodeSize, n.values.size()));
//
//        int offset = 1;
//        Integer key1 = 19;
//        String value1 = UUID.randomUUID().toString();
//        // test inserting at a particular spot
//        lng.put(0, offset, key1, value1);
//        assertOnValueAtPosition(lng, 0, offset, key1, value1);
//
//        assertEquals(value1, lng.getValue(0, offset));
//        assertTrue(lng.isFull(lng.absolutePosition(0, offset)));
//
//        lng.delete(0, offset);
//        assertOnValueAtPosition(lng, 0, offset, null, null);
//        assertFalse(lng.isFull(lng.absolutePosition(0, offset)));
//        lng.nodes.forEach(n -> assertEquals(nodeSize, n.keys.size()));
//        lng.nodes.forEach(n -> assertEquals(nodeSize, n.values.size()));
//    }
//
//    @Test
//    public void testSplit() throws Exception {
//        int numNodes = 3;
//        int nodeSize = 2;
//        LeafNodeGroup oldNode = new LeafNodeGroup(numNodes, nodeSize);
//        assertEquals(numNodes, oldNode.nodes.size());
//        oldNode.nodes.forEach(n -> assertEquals(nodeSize, n.keys.size()));
//        oldNode.nodes.forEach(n -> assertEquals(nodeSize, n.values.size()));
//        Map<Integer, String> keysToValue = new HashMap<>();
//
//        for (int i = 0; i < nodeSize * numNodes; i++) {
//            Integer key = 19 - i;
//            String value = UUID.randomUUID().toString();
//            assertTrue(oldNode.hasSpace());
//            oldNode.put(0, 0, key, value);
//            assertEquals((numNodes * nodeSize) - (i + 1), oldNode.numEmptySlots);
//            keysToValue.put(key, value);
//        }
//
//
//        assertFalse(oldNode.hasSpace());
//        assertEquals(0, oldNode.numEmptySlots);
//        LeafNodeGroup newNode = oldNode.split();
//        assertTrue(oldNode.hasSpace());
//        assertTrue(newNode.hasSpace());
//        assertEquals(4, oldNode.numEmptySlots);
//        assertEquals(2, newNode.numEmptySlots);
//        assertEquals(numNodes, oldNode.nodes.size());
//        assertEquals(numNodes, newNode.nodes.size());
//
//        int lowestKey = 19 - (nodeSize * numNodes) + 1;
//        for (int i = 0; i < nodeSize * numNodes; i++) {
//            if (i < nodeSize) {
//                assertEquals(keysToValue.get(lowestKey + i), oldNode.getValue(0, i));
//            } else {
//                int absoluteOffset = i - nodeSize;
//                assertEquals(keysToValue.get(lowestKey + i), newNode.getValue(absoluteOffset / nodeSize, absoluteOffset % nodeSize));
//            }
//        }
//
//        // verify the old node
//        for (int i = 0; i < numNodes * nodeSize; i++) {
//            if (i < nodeSize) {
//                assertTrue(oldNode.isFull(i));
//            } else {
//                assertFalse(oldNode.isFull(i));
//            }
//            // this insured indirectly that the keys array
//            // has enough entries and none of them are null
//            assertNotNull(oldNode.getHighestKeys());
//        }
//        // verify the new node
//        for (int i = 0; i < numNodes * nodeSize; i++) {
//            if (i < 2 * nodeSize) {
//                assertTrue(newNode.isFull(i));
//            } else {
//                assertFalse(newNode.isFull(i));
//            }
//            assertNotNull(newNode.getHighestKeys());
//        }
//    }
//
//    // this includes filling the index/offset you specify
//    private void fillRightFrom(LeafNodeGroup lng, int nodeIndex, int nodeOffset) {
//        // -1 converts from sizes to indexes
//        fill(lng, nodeIndex, nodeOffset, lng.numberOfNodes -1, lng.nodeSize -1);
//    }
//
//    // this includes filling the index/offset you specify
//    private void fillLeftFrom(LeafNodeGroup lng, int nodeIndex, int nodeOffset) {
//        fill(lng, 0, 0, nodeIndex, nodeOffset);
//    }
//
//    private void fill(LeafNodeGroup lng, int fromNodeIndex, int fromNodeOffset, int toNodeIndex, int toNodeOffset) {
//        Random r = new Random();
//
//        if (fromNodeIndex == toNodeIndex) {
//            for (int i = fromNodeOffset; i < toNodeOffset; i++) {
//                Integer key = r.nextInt();
//                String value = UUID.randomUUID().toString();
//                lng.put(toNodeIndex, i, key, value);
//            }
//        }
//
//        // i < toNodeIndex is short for i <= toNodeIndex -1
//        for (int i = fromNodeIndex; i < toNodeIndex; i++) {
//            for (int j = fromNodeOffset; j < lng.nodeSize; j++) {
//                Integer key = r.nextInt();
//                String value = UUID.randomUUID().toString();
//                lng.put(i, j, key, value);
//            }
//        }
//
//        if (fromNodeIndex != toNodeIndex) {
//            for (int j = 0; j <= toNodeOffset; j++) {
//                Integer key = r.nextInt();
//                String value = UUID.randomUUID().toString();
//                lng.put(toNodeIndex, j, key, value);
//            }
//        }
//    }
//
//    private void assertOnValueAtPosition(LeafNodeGroup lng, int nodeIndex, int nodeOffset, Integer key, String value) {
//        assertEquals(lng.nodes.get( nodeIndex ).keys.get( nodeOffset ), key);
//        assertEquals(lng.nodes.get( nodeIndex ).values.get( nodeOffset ), value);
//    }
}
