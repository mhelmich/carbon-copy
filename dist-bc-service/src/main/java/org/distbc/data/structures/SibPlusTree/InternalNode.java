package org.distbc.data.structures.SibPlusTree;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

class InternalNode extends Node {
    List<Integer> keys;
    NodeGroup child;

    InternalNode(int size) {
        Vector<Integer> v = new Vector<>(size);
        v.setSize(size);
        keys = new ArrayList<>(v);
    }
}
