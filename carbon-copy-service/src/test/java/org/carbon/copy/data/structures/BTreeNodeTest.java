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

package org.carbon.copy.data.structures;

import co.paralleluniverse.galaxy.Store;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Vector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

public class BTreeNodeTest {
    @Test
    public void testSerializationLeafNode() {
        Txn txn = Mockito.mock(Txn.class);
        BTreeNode<Integer, Integer> node = newBTreeNode(2);
        node.setEntryAt(0, new BTreeEntry<>(5, 5), txn);
        node.setEntryAt(1, new BTreeEntry<>(6, 6), txn);
        assertEquals(Integer.valueOf(5), node.getEntryAt(0).getKey());
        assertEquals(Integer.valueOf(6), node.getEntryAt(1).getKey());

        ByteBuffer bb = ByteBuffer.allocateDirect(DataStructure.MAX_BYTE_SIZE);
        node.write(bb);
        bb.rewind();

        BTreeNode<Integer, Integer> node2 = newBTreeNode(0);
        node2.read(bb);
        assertEquals(2, node2.getNumChildren());
        assertEquals(Integer.valueOf(5), node2.getEntryAt(0).getKey());
        assertEquals(Integer.valueOf(6), node2.getEntryAt(1).getKey());
        assertNull(node2.getEntryAt(2));
    }

    @Test
    public void testSerializationInternalNode() {
        Txn txn = Mockito.mock(Txn.class);
        BTreeNode<Integer, Integer> child1 = newBTreeNodeWithId(125);
        BTreeNode<Integer, Integer> child2 = newBTreeNodeWithId(123);

        BTreeNode<Integer, Integer> node = newBTreeNode(2);
        node.setEntryAt(0, new BTreeEntry<>(5, child1), txn);
        node.setEntryAt(1, new BTreeEntry<>(6, child2), txn);
        assertEquals(Integer.valueOf(5), node.getEntryAt(0).getKey());
        assertEquals(125, node.getEntryAt(0).getChildNode().getId());
        assertEquals(Integer.valueOf(6), node.getEntryAt(1).getKey());
        assertEquals(123, node.getEntryAt(1).getChildNode().getId());

        ByteBuffer bb = ByteBuffer.allocateDirect(DataStructure.MAX_BYTE_SIZE);
        node.write(bb);
        bb.rewind();

        BTreeNode<Integer, Integer> node2 = newBTreeNode(0);
        node2.read(bb);
        assertEquals(2, node2.getNumChildren());
        assertEquals(Integer.valueOf(5), node2.getEntryAt(0).getKey());
        assertEquals(125, node2.getEntryAt(0).getChildNode().getId());
        assertEquals(Integer.valueOf(6), node2.getEntryAt(1).getKey());
        assertEquals(123, node.getEntryAt(1).getChildNode().getId());
        assertNull(node2.getEntryAt(2));
    }

    @Test
    public void testSerializationTwoLevels() throws Exception {
        Txn txn = Mockito.mock(Txn.class);
        BTreeNode<String, String> leaf1 = newBTreeNodeWithId(125);
        leaf1.setNumChildren(3);
        primeEntriesList(leaf1);
        leaf1.setEntryAt(0, new BTreeEntry<>("key__1", "zs.rtgv nmk"), txn);
        leaf1.setEntryAt(1, new BTreeEntry<>("key__2", " xsertgb mk"), txn);
        leaf1.setEntryAt(2, new BTreeEntry<>("key__3", "xdr56yhji9ok"), txn);
        BTreeNode<String, String> leaf2 = newBTreeNodeWithId(123);
        leaf2.setNumChildren(1);
        primeEntriesList(leaf2);
        leaf2.setEntryAt(0, new BTreeEntry<>("key__5", "wertgh"), txn);

        BTreeNode<String, String> internalNode = newBTreeNode(2);
        internalNode.setEntryAt(0, new BTreeEntry<>("key__3", leaf1), txn);
        internalNode.setEntryAt(1, new BTreeEntry<>("key__5", leaf2), txn);
        assertEquals("key__3", internalNode.getEntryAt(0).getKey());
        assertEquals(125, internalNode.getEntryAt(0).getChildNode().getId());
        assertEquals("key__5", internalNode.getEntryAt(1).getKey());
        assertEquals(123, internalNode.getEntryAt(1).getChildNode().getId());

        ByteBuffer bb = ByteBuffer.allocateDirect(internalNode.size());
        internalNode.write(bb);
        bb.rewind();

        BTreeNode<String, String> internalNode2 = newBTreeNode(0);
        internalNode2.read(bb);
        assertEquals("key__3", internalNode2.getEntryAt(0).getKey());
        assertEquals(125, internalNode2.getEntryAt(0).getChildNode().getId());
        assertEquals("key__5", internalNode2.getEntryAt(1).getKey());
        assertEquals(123, internalNode2.getEntryAt(1).getChildNode().getId());
    }

    private <Key extends Comparable<Key>, Value> BTreeNode<Key, Value> newBTreeNode(int numChildren) {
        Txn txn = Mockito.mock(Txn.class);
        when(txn.getStoreTransaction()).thenReturn(null);
        Store s = Mockito.mock(Store.class);
        return new BTreeNode<>(s, new DataStructureFactoryImpl(s, null, null), numChildren, txn);
    }

    private <Key extends Comparable<Key>, Value> BTreeNode<Key, Value> newBTreeNodeWithId(long id) {
        Txn txn = Mockito.mock(Txn.class);
        when(txn.getStoreTransaction()).thenReturn(null);
        Store s = Mockito.mock(Store.class);
        return new BTreeNode<>(s, new DataStructureFactoryImpl(s, null, null), id);
    }

    private <Key extends Comparable<Key>, Value> void primeEntriesList(BTreeNode<Key, Value> node) throws NoSuchFieldException, IllegalAccessException {
        Field field = node.getClass().getDeclaredField("entries");
        field.setAccessible(true);
        Vector<BTreeEntry<Key, Value>> v = new Vector<>(BTree.MAX_NODE_SIZE);
        v.setSize(BTree.MAX_NODE_SIZE);
        ArrayList<BTreeEntry<Key, Value>> entries = new ArrayList<>(v);
        field.set(node, entries);
    }
}
