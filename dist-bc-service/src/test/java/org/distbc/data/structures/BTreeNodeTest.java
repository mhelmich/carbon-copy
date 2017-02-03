package org.distbc.data.structures;

import co.paralleluniverse.galaxy.Store;
import org.junit.Test;
import org.mockito.Mockito;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

public class BTreeNodeTest {
    @Test
    public void testSerializationLeafNode() {
        BTreeNode<Integer, Integer> node = newBTreeNode(2);
        node.getChildren().set(0, new BTreeEntry<>(5, 5));
        node.getChildren().set(1, new BTreeEntry<>(6, 6));
        assertEquals(Integer.valueOf(5), node.getChildren().get(0).getKey());
        assertEquals(Integer.valueOf(6), node.getChildren().get(1).getKey());

        ByteBuffer bb = ByteBuffer.allocateDirect(DataStructure.MAX_BYTE_SIZE);
        node.write(bb);
        bb.rewind();

        BTreeNode<Integer, Integer> node2 = newBTreeNode(0);
        node2.read(bb);
        assertEquals(2, node2.getNumChildren());
        assertEquals(Integer.valueOf(5), node2.getChildren().get(0).getKey());
        assertEquals(Integer.valueOf(6), node2.getChildren().get(1).getKey());
        assertNull(node2.getChildren().get(2));
    }

    @Test
    public void testSerializationInternalNode() {
        BTreeNode<Integer, Integer> child1 = newBTreeNodeWithId(125);
        BTreeNode<Integer, Integer> child2 = newBTreeNodeWithId(123);

        BTreeNode<Integer, Integer> node = newBTreeNode(2);
        node.getChildren().set(0, new BTreeEntry<>(5, child1));
        node.getChildren().set(1, new BTreeEntry<>(6, child2));
        assertEquals(Integer.valueOf(5), node.getChildren().get(0).getKey());
        assertEquals(125, node.getChildren().get(0).getChildNode().getId());
        assertEquals(Integer.valueOf(6), node.getChildren().get(1).getKey());
        assertEquals(123, node.getChildren().get(1).getChildNode().getId());

        ByteBuffer bb = ByteBuffer.allocateDirect(DataStructure.MAX_BYTE_SIZE);
        node.write(bb);
        bb.rewind();

        BTreeNode<Integer, Integer> node2 = newBTreeNode(0);
        node2.read(bb);
        assertEquals(2, node2.getNumChildren());
        assertEquals(Integer.valueOf(5), node2.getChildren().get(0).getKey());
        assertEquals(125, node2.getChildren().get(0).getChildNode().getId());
        assertEquals(Integer.valueOf(6), node2.getChildren().get(1).getKey());
        assertEquals(123, node.getChildren().get(1).getChildNode().getId());
        assertNull(node2.getChildren().get(2));
    }

    @Test
    public void testSerializationTwoLevels() {
        BTreeNode<String, String> leaf1 = newBTreeNodeWithId(125);
        BTreeNode<String, String> leaf2 = newBTreeNodeWithId(123);

        BTreeNode<String, String> node = newBTreeNode(2);
        node.getChildren().set(0, new BTreeEntry<>("", leaf1));
        node.getChildren().set(1, new BTreeEntry<>("", leaf2));
        assertEquals("", node.getChildren().get(0).getKey());
        assertEquals(125, node.getChildren().get(0).getChildNode().getId());
        assertEquals("", node.getChildren().get(1).getKey());
        assertEquals(123, node.getChildren().get(1).getChildNode().getId());
    }

    private <Key extends Comparable<Key>, Value> BTreeNode<Key, Value> newBTreeNode(int numChildren) {
        Txn txn = Mockito.mock(Txn.class);
        when(txn.getStoreTransaction()).thenReturn(null);
        Store s = Mockito.mock(Store.class);
        return new BTreeNode<>(s, new DataStructureFactoryImpl(s), numChildren, txn);
    }

    private <Key extends Comparable<Key>, Value> BTreeNode<Key, Value> newBTreeNodeWithId(long id) {
        Txn txn = Mockito.mock(Txn.class);
        when(txn.getStoreTransaction()).thenReturn(null);
        Store s = Mockito.mock(Store.class);
        return new BTreeNode<>(s, new DataStructureFactoryImpl(s), id);
    }
}
