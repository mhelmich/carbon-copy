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

package org.carbon.copy.data.structures.experimental.cct;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Ignore
public class LeafNodeGroupTest {

    private final int nodeSize = 3;
    private final int numNodes = 4;
    private String prefix = "narf_";

    @Test
    public void testShiftWithinNode() {
        LeafNodeGroup<Integer, String> lng = new LeafNodeGroup<>(nodeSize, numNodes);
        lng.put(1, 17, prefix + 17);
        assertTrue(lng.isFull(1));
        assertEquals((nodeSize * numNodes) - 1, lng.getEmptySlots());

        lng.shiftOneRight(1, 2);
        assertFalse(lng.isFull(1));
        assertTrue(lng.isFull(2));

        assertEquals(Integer.valueOf(17), lng.getKey(2));
        assertEquals(prefix + 17, lng.getValue(2));
        assertEquals((nodeSize * numNodes) - 1, lng.getEmptySlots());
    }

    @Test
    public void testShiftAcrossNodes() {
        LeafNodeGroup<Integer, String> lng = new LeafNodeGroup<>(nodeSize, numNodes);
        lng.put(2, 17, prefix + 17);
        assertTrue(lng.isFull(2));
        assertEquals((nodeSize * numNodes) - 1, lng.getEmptySlots());

        lng.shiftOneRight(1, 4);
        assertFalse(lng.isFull(2));
        assertTrue(lng.isFull(3));

        assertEquals(Integer.valueOf(17), lng.getKey(3));
        assertEquals(prefix + 17, lng.getValue(3));
        assertEquals((nodeSize * numNodes) - 1, lng.getEmptySlots());
    }

    @Test
    public void testShiftingAGroup() {
        LeafNodeGroup<Integer, String> lng = new LeafNodeGroup<>(nodeSize, numNodes);
        lng.put(2, 17, prefix + 17);
        lng.put(3, 18, prefix + 18);
        lng.put(4, 19, prefix + 19);
        lng.put(5, 20, prefix + 20);

        assertTrue(lng.isFull(2));
        assertTrue(lng.isFull(3));
        assertTrue(lng.isFull(4));
        assertTrue(lng.isFull(5));
        assertFalse(lng.isFull(6));

        lng.shiftOneRight(2, 6);

        assertFalse(lng.isFull(2));
        assertTrue(lng.isFull(3));
        assertTrue(lng.isFull(4));
        assertTrue(lng.isFull(5));
        assertTrue(lng.isFull(6));
    }

    @Test
    public void testSplit() {
        LeafNodeGroup<Integer, String> lng = new LeafNodeGroup<>(nodeSize, numNodes);
        for (int i = 0; i < numNodes * nodeSize; i++) {
            assertTrue(lng.hasEmptySlots());
            lng.put(i, i * 3, prefix + i);
            assertTrue(lng.isFull(i));
        }

        assertFalse(lng.hasEmptySlots());

        LeafNodeGroup<Integer, String> newLng = lng.split();
        assertTrue(lng.hasEmptySlots());
        assertTrue(newLng.hasEmptySlots());
        assertEquals((nodeSize * numNodes) / 2, lng.getEmptySlots());
        assertEquals((nodeSize * numNodes) / 2, newLng.getEmptySlots());

        for (int i = 0; i < numNodes * nodeSize; i++) {
            boolean shouldBeFull = (i < (numNodes * nodeSize) / 2);
            assertEquals(shouldBeFull, lng.isFull(i));
            assertEquals(shouldBeFull, newLng.isFull(i));
        }
    }

    @Test
    public void testSplittingTheMiddleOfThreeNodeGroups() {
        LeafNodeGroup<Integer, String> lng1 = new LeafNodeGroup<>(nodeSize, numNodes);
        LeafNodeGroup<Integer, String> lng2 = new LeafNodeGroup<>(nodeSize, numNodes);
        LeafNodeGroup<Integer, String> lng3 = new LeafNodeGroup<>(nodeSize, numNodes);

        lng1.next = lng2;
        lng2.previous = lng1;
        lng2.next = lng3;
        lng3.previous = lng2;

        for (int i = 0; i < numNodes * nodeSize; i++) {
            assertTrue(lng2.hasEmptySlots());
            lng2.put(i, i * 3, prefix + i);
            assertTrue(lng2.isFull(i));
        }

        assertFalse(lng2.hasEmptySlots());
        LeafNodeGroup<Integer, String> newLng = lng2.split();

        assertEquals(lng1.next, lng2);
        assertEquals(lng2.previous, lng1);

        assertEquals(lng2.next, newLng);
        assertEquals(newLng.previous, lng2);

        assertEquals(newLng.next, lng3);
        assertEquals(lng3.previous, newLng);
    }
}
