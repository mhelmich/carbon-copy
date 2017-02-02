package org.distbc.data.structures;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Helper and container for a bunch of items that belong into a tree
 */
class BTreeNode<Key extends Comparable<Key>, Value> {
    private int numChildren;
    // pointers to children
    private ArrayList<BTreeEntry<Key, Value>> children;

    BTreeNode(int numChildren) {
        Vector<BTreeEntry<Key, Value>> v = new Vector<>(BTree.MAX_NODE_SIZE);
        v.setSize(BTree.MAX_NODE_SIZE);
        children = new ArrayList<>(v);
        this.numChildren = numChildren;
    }

    int getNumChildren() {
        return numChildren;
    }

    void setNumChildren(int newNumChildren) {
        this.numChildren = newNumChildren;
    }

    ArrayList<BTreeEntry<Key, Value>> getChildren() {
        return children;
    }
}
