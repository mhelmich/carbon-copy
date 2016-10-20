package org.distbc.data.structures.SibPlusTree;

import java.util.Vector;

/**
 * Created by mhelmich on 10/12/16.
 */
class InternalNode extends Node {
    Integer[] keys;
    NodeGroup child;

    @SuppressWarnings("unchecked")
    InternalNode(int size) {
        Vector<Integer> v = new Vector<>(size);
        v.setSize(size);
//            keys = (Integer[]) v.toArray();
        keys = v.toArray(new Integer[] {});
    }
}
