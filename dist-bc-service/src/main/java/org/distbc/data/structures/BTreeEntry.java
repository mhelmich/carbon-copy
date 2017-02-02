package org.distbc.data.structures;

/**
 * always remember
 * internal nodes: only use key and next
 * leaf nodes: only use key and value
 */
class BTreeEntry<Key extends Comparable<Key>, Value> {
    private Key key;
    private final Value val;
    // helper field to iterate over array entries
    private BTreeNode<Key, Value> next;

    BTreeEntry(Key key, Value val, BTreeNode<Key, Value> next) {
        this.key  = key;
        this.val  = val;
        this.next = next;
    }

    Key getKey() {
        return key;
    }

    void setKey(Key key) {
        this.key = key;
    }

    Value getVal() {
        return val;
    }

    BTreeNode<Key, Value> getChildNode() {
        return next;
    }

    void setChildNode(BTreeNode<Key, Value> next) {
        this.next = next;
    }
}
