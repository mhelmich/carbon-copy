package org.distbc.data.structures;

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
        BTreeNode<Integer, Integer> node = newBTreeNode(2);
        node.setEntryAt(0, new BTreeEntry<>(5, 5));
        node.setEntryAt(1, new BTreeEntry<>(6, 6));
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
        BTreeNode<Integer, Integer> child1 = newBTreeNodeWithId(125);
        BTreeNode<Integer, Integer> child2 = newBTreeNodeWithId(123);

        BTreeNode<Integer, Integer> node = newBTreeNode(2);
        node.setEntryAt(0, new BTreeEntry<>(5, child1));
        node.setEntryAt(1, new BTreeEntry<>(6, child2));
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
        BTreeNode<String, String> leaf1 = newBTreeNodeWithId(125);
        leaf1.setNumChildren(3);
        primeEntriesList(leaf1);
        leaf1.setEntryAt(0, new BTreeEntry<>("key__1", "zs.rtgv nmk"));
        leaf1.setEntryAt(1, new BTreeEntry<>("key__2", " xsertgb mk"));
        leaf1.setEntryAt(2, new BTreeEntry<>("key__3", "xdr56yhji9ok"));
        BTreeNode<String, String> leaf2 = newBTreeNodeWithId(123);
        leaf2.setNumChildren(1);
        primeEntriesList(leaf2);
        leaf2.setEntryAt(0, new BTreeEntry<>("key__5", "wertgh"));

        BTreeNode<String, String> internalNode = newBTreeNode(2);
        internalNode.setEntryAt(0, new BTreeEntry<>("key__3", leaf1));
        internalNode.setEntryAt(1, new BTreeEntry<>("key__5", leaf2));
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
        return new BTreeNode<>(s, new DataStructureFactoryImpl(s), numChildren, txn);
    }

    private <Key extends Comparable<Key>, Value> BTreeNode<Key, Value> newBTreeNodeWithId(long id) {
        Txn txn = Mockito.mock(Txn.class);
        when(txn.getStoreTransaction()).thenReturn(null);
        Store s = Mockito.mock(Store.class);
        return new BTreeNode<>(s, new DataStructureFactoryImpl(s), id);
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
