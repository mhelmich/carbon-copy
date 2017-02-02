package org.distbc.data.structures;

import co.paralleluniverse.galaxy.Store;

import java.util.ArrayList;
import java.util.Vector;

/**
 *  Modelled after the BTree by Robert Sedgewick and Kevin Wayne.
 *  Check out their useful website to learn more about basic data structures.
 *  http://algs4.cs.princeton.edu
 */
class BTree<Key extends Comparable<Key>, Value> extends DataStructure {
    // helper B-tree node data type
    private final class Node {
        private int numChildren;
        // pointers to children
        private ArrayList<Entry> children;

        private Node(int numChildren) {
            Vector<Entry> v = new Vector<>(MAX_NODE_SIZE);
            v.setSize(MAX_NODE_SIZE);
            children = new ArrayList<>(v);
            this.numChildren = numChildren;
        }
    }

    // always remember
    // internal nodes: only use key and next
    // leaf nodes: only use key and value
    private final class Entry {
        private Key key;
        private final Value val;
        // helper field to iterate over array entries
        private Node next;
        private Entry(Key key, Value val, Node next) {
            this.key  = key;
            this.val  = val;
            this.next = next;
        }

        Node getChildNode() {
            return next;
        }

        void setChildNode(Node next) {
            this.next = next;
        }
    }

    // max children per B-tree node = MAX_NODE_SIZE-1
    // (must be even and greater than 2)
    // this is gotta be >= 4
    private static final int MAX_NODE_SIZE = 4;

    private Node root;
    // height of the tree
    private int height;

    public BTree(Store store, Txn txn) {
        super(store);
        asyncUpsert(this, txn);
        root = new Node(0);
    }

    public BTree(Store store, long id) {
        super(store, id);
        root = new Node(0);
    }

    public BTree(Store store, long id, Txn txn) {
        super(store, id);
        root = new Node(0);
    }

    public Value get(Key key) {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");
        checkDataStructureRetrieved();
        return search(root, key, height);
    }

    public void put(Key key, Value value) {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");
        checkDataStructureRetrieved();
        innerPut(key, value);
    }

    public Iterable<Key> keys() {
        return null;
    }

    public void delete(Key key) {
        put(key, null);
    }

    /////////////////////////////////////////////////////////////
    //////////////////////////////////////////////
    // internal unit testable data structure implementation

    private void innerPut(Key key, Value value) {
        Node insertedNode = insert(root, key, value, height);
        if (insertedNode == null) return;

        // insert doesn't return null means we gotta
        // split the top-most node
        Node newNode = new Node(2);
        newNode.children.set(0, newEntry(root.children.get(0).key, null, root));
        newNode.children.set(1, newEntry(insertedNode.children.get(0).key, null, insertedNode));
        root = newNode;
        height++;
    }

    private Entry newEntry(Key key, Value value, Node next) {
        return new Entry(key, value, next);
    }

    private Value search(Node x, Key key, int height) {
        ArrayList<Entry> children = x.children;

        if (height > 0) {
            // internal node
            // find the right child node to descend to
            for (int j = 0; j < x.numChildren; j++) {
                if (j + 1 == x.numChildren || lessThan(key, children.get(j + 1).key)) {
                    return search(children.get(j).getChildNode(), key, height - 1);
                }
            }
        } else {
            // leaf node
            // find the right key (if it's there) and return it
            for (int j = 0; j < x.numChildren; j++) {
                if (equal(key, children.get(j).key)) {
                    return children.get(j).val;
                }
            }
        }
        return null;
    }

    private Node insert(Node x, Key key, Value value, int height) {
        int j;
        Entry entryToInsert = newEntry(key, value, null);

        if (height > 0 ) {
            // internal node
            for (j = 0; j < x.numChildren; j++) {
                if ((j + 1 == x.numChildren) || lessThan(key, x.children.get(j + 1).key)) {
                    Node insertedNode = insert(x.children.get(j++).getChildNode(), key, value, height - 1);
                    // we're done, bubble up through recursion
                    if (insertedNode == null) return null;
                    entryToInsert.key = insertedNode.children.get(0).key;
                    entryToInsert.setChildNode(insertedNode);
                    break;
                }
            }
        } else {
            // leaf node
            for (j = 0; j < x.numChildren; j++) {
                if (lessThan(key, x.children.get(j).key)) break;
            }
        }

        // move all children over one slot
        for (int i = x.numChildren; i > j; i--) {
            x.children.set(i, x.children.get(i - 1));
        }
        // drop the new one into the right spot
        x.children.set(j, entryToInsert);
        x.numChildren++;
        // if we have space, end recursion
        // if not, split the node
        return (x.numChildren < MAX_NODE_SIZE) ? null : split(x);
    }

    private Node split(Node oldNode) {
        Node newNode = new Node(MAX_NODE_SIZE / 2);
        oldNode.numChildren = MAX_NODE_SIZE / 2;
        for (int j = 0; j < MAX_NODE_SIZE / 2; j++) {
            newNode.children.set(j, oldNode.children.get((MAX_NODE_SIZE / 2) + j));
        }
        return newNode;
    }

    @Override
    public String toString() {
        return toString(root, height, "") + "\n";
    }

    private String toString(Node x, int height, String indent) {
        StringBuilder sb = new StringBuilder();
        ArrayList<Entry> children = x.children;

        if (height == 0) {
            for (int j = 0; j < x.numChildren; j++) {
                sb.append(indent).append(children.get(j).key).append(" ").append(children.get(j).val).append("\n");
            }
        }
        else {
            for (int j = 0; j < x.numChildren; j++) {
                if (j > 0) sb.append(indent).append("(").append(children.get(j).key).append(")\n");
                sb.append(toString(children.get(j).getChildNode(), height-1, indent + "     "));
            }
        }
        return sb.toString();
    }

    private boolean lessThan(Key k1, Key k2) {
        return k1.compareTo(k2) < 0;
    }

    private boolean equal(Key k1, Key k2) {
        return k1.compareTo(k2) == 0;
    }

    /////////////////////////////////////////////////////////////
    //////////////////////////////////////////////
    // galaxy-specific serialization overrides

    @Override
    void serialize(SerializerOutputStream out) {

    }

    @Override
    void deserialize(SerializerInputStream in) {

    }
}
