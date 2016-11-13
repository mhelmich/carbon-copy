package org.distbc.data.structures.SibPlusTree;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Leaf node of the sib tree.
 * This contains all keys and values and pointers to the siblings.
 * When traversing value comparison might spill into sibling nodes due to
 * concurrent update or inserts.
 */
class LeafNode extends Node {
    // array of key value
    List<Integer> keys;
    List<String> values;
    LeafNode next;
    LeafNode previous;

    LeafNode(int size) {
        // a vector is synchronized array
        // but for a vector you can specify a size
        Vector<Integer> v1 = new Vector<>(size);
        v1.setSize(size);
        keys = new ArrayList<>(v1);

        Vector<String> v2 = new Vector<>(size);
        v2.setSize(size);
        values = new ArrayList<>(v2);
    }

    void put(int offset, Integer key, String value) {
        keys.set(offset, key);
        values.set(offset, value);
    }

    void delete(int offset) {
        put(offset, null, null);
    }

    @Override
    Integer getKey(int nodeOffset) {
        return keys.get(nodeOffset);
    }

    String getValue(Integer key) {
        int index = keys.indexOf(key);
        if (index > 0) {
            return values.get(index);
        }
        return null;
    }

    Pair<Integer, String> shift(int from, int to, Integer fillInKey, String fillInValue) {
        Integer carryOverKey = keys.remove(to);
        String carryOverValue = values.remove(to);
        keys.add(from, fillInKey);
        values.add(from, fillInValue);
        return new ImmutablePair<>(carryOverKey, carryOverValue);
    }
}
