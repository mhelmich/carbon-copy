package org.distbc.data.structures;

import co.paralleluniverse.galaxy.Store;

import java.util.ArrayList;

/**
 *  Modelled after the BTree by Robert Sedgewick and Kevin Wayne.
 *  Check out their useful website to learn more about basic data structures.
 *  http://algs4.cs.princeton.edu
 */
class BTree<Key extends Comparable<Key>, Value> extends DataStructure {
    // max children per B-tree node = MAX_NODE_SIZE-1
    // (must be even and greater than 2)
    // this is gotta be >= 4
    static final int MAX_NODE_SIZE = 4;

    private BTreeNode<Key, Value> root;
    // height of the tree
    private int height;

    BTree(Store store, Txn txn) {
        super(store);
        asyncUpsert(this, txn);
        root = new BTreeNode<>(0);
    }

    BTree(Store store, long id) {
        super(store, id);
        root = new BTreeNode<>(0);
    }

    BTree(Store store, long id, Txn txn) {
        super(store, id);
        root = new BTreeNode<>(0);
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
        BTreeNode<Key, Value> insertedNode = insert(root, key, value, height);
        if (insertedNode == null) return;

        // insert doesn't return null means we gotta
        // split the top-most node
        BTreeNode<Key, Value> newNode = new BTreeNode<>(2);
        newNode.getChildren().set(0, newEntry(root.getChildren().get(0).getKey(), null, root));
        newNode.getChildren().set(1, newEntry(insertedNode.getChildren().get(0).getKey(), null, insertedNode));
        root = newNode;
        height++;
    }

    private BTreeEntry<Key, Value> newEntry(Key key, Value value, BTreeNode<Key, Value> next) {
        return new BTreeEntry<>(key, value, next);
    }

    private Value search(BTreeNode<Key, Value> x, Key key, int height) {
        ArrayList<BTreeEntry<Key, Value>> children = x.getChildren();

        if (height > 0) {
            // internal node
            // find the right child node to descend to
            for (int j = 0; j < x.getNumChildren(); j++) {
                if (j + 1 == x.getNumChildren() || lessThan(key, children.get(j + 1).getKey())) {
                    return search(children.get(j).getChildNode(), key, height - 1);
                }
            }
        } else {
            // leaf node
            // find the right key (if it's there) and return it
            for (int j = 0; j < x.getNumChildren(); j++) {
                if (equal(key, children.get(j).getKey())) {
                    return children.get(j).getVal();
                }
            }
        }
        return null;
    }

    private BTreeNode<Key, Value> insert(BTreeNode<Key, Value> x, Key key, Value value, int height) {
        int j;
        BTreeEntry<Key, Value> entryToInsert = newEntry(key, value, null);

        if (height > 0 ) {
            // internal node
            for (j = 0; j < x.getNumChildren(); j++) {
                if ((j + 1 == x.getNumChildren()) || lessThan(key, x.getChildren().get(j + 1).getKey())) {
                    BTreeNode<Key, Value> insertedNode = insert(x.getChildren().get(j++).getChildNode(), key, value, height - 1);
                    // we're done, bubble up through recursion
                    if (insertedNode == null) return null;
                    entryToInsert.setKey(insertedNode.getChildren().get(0).getKey());
                    entryToInsert.setChildNode(insertedNode);
                    break;
                }
            }
        } else {
            // leaf node
            for (j = 0; j < x.getNumChildren(); j++) {
                if (lessThan(key, x.getChildren().get(j).getKey())) break;
            }
        }

        // move all children over one slot
        for (int i = x.getNumChildren(); i > j; i--) {
            x.getChildren().set(i, x.getChildren().get(i - 1));
        }
        // drop the new one into the right spot
        x.getChildren().set(j, entryToInsert);
        x.setNumChildren(x.getNumChildren() + 1);
        // if we have space, end recursion
        // if not, split the node
        return (x.getNumChildren() < MAX_NODE_SIZE) ? null : split(x);
    }

    private BTreeNode<Key, Value> split(BTreeNode<Key, Value> oldNode) {
        BTreeNode<Key, Value> newNode = new BTreeNode<>(MAX_NODE_SIZE / 2);
        oldNode.setNumChildren(MAX_NODE_SIZE / 2);
        for (int j = 0; j < MAX_NODE_SIZE / 2; j++) {
            newNode.getChildren().set(j, oldNode.getChildren().get((MAX_NODE_SIZE / 2) + j));
        }
        return newNode;
    }

    @Override
    public String toString() {
        return toString(root, height, "") + "\n";
    }

    private String toString(BTreeNode<Key, Value> x, int height, String indent) {
        StringBuilder sb = new StringBuilder();
        ArrayList<BTreeEntry<Key, Value>> children = x.getChildren();

        if (height == 0) {
            for (int j = 0; j < x.getNumChildren(); j++) {
                sb.append(indent).append(children.get(j).getKey()).append(" ").append(children.get(j).getVal()).append("\n");
            }
        }
        else {
            for (int j = 0; j < x.getNumChildren(); j++) {
                if (j > 0) sb.append(indent).append("(").append(children.get(j).getKey()).append(")\n");
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
