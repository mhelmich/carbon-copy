package org.distbc.data.structures.SibPlusTree;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * This class represents a node in the sib tree that is not on level zero.
 * It only contains keys and has no references to siblings. It holds however
 * a reference to a node group (internal or leaf) in next lower level of the tree.
 */
class InternalNode extends Node {
    List<Integer> keys;
    NodeGroup child;

    InternalNode(int size) {
        // a vector is synchronized array
        // but for a vector you can specify a size
        Vector<Integer> v = new Vector<>(size);
        v.setSize(size);
        keys = new ArrayList<>(v);
    }

    @Override
    Integer getKey(int nodeOffset) {
        return keys.get(nodeOffset);
    }
}
