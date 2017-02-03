package org.distbc.data.structures;

/**
 * always remember
 * internal nodes: only use key and childNode
 * leaf nodes: only use key and value
 */
class BTreeEntry<Key extends Comparable<Key>, Value> {
    private Key key;
    private final Value val;
    // helper field to iterate over array entries
    private BTreeNode<Key, Value> childNode;

    BTreeEntry(Key key, Value val) {
        this(key, val, null);
    }

    BTreeEntry(Key key, BTreeNode<Key, Value> childNode) {
        this(key, null, childNode);
    }

    BTreeEntry(Key key, Value val, BTreeNode<Key, Value> childNode) {
        this.key  = key;
        this.val  = val;
        this.childNode = childNode;
    }

    Key getKey() {
        return key;
    }

    void setKey(Key key) {
        this.key = key;
    }

    Value getValue() {
        return val;
    }

    BTreeNode<Key, Value> getChildNode() {
        return childNode;
    }

    void setChildNode(BTreeNode<Key, Value> childNode) {
        this.childNode = childNode;
    }
}
