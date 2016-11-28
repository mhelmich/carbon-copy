package org.distbc.data.structures.cct;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

class LeafNodeGroup<K extends Comparable<K>, V extends Comparable<V>> extends NodeGroup<K> {

    private final ArrayList<ArrayList<K>> keys;
    private final ArrayList<ArrayList<V>> values;

    @VisibleForTesting
    LeafNodeGroup next;
    @VisibleForTesting
    LeafNodeGroup previous;

    LeafNodeGroup(int nodeSize, int numNodes) {
        super(nodeSize, numNodes);
        this.keys = initKeyLists();

        Vector<ArrayList<V>> vvs = new Vector<>(numNodes);
        vvs.setSize(numNodes);
        this.values = new ArrayList<>(vvs);
        for (int i = 0; i < numNodes; i++) {
            this.values.set(i, newValueList());
        }
    }

    private ArrayList<V> newValueList() {
        Vector<V> vv = new Vector<>(nodeSize);
        vv.setSize(nodeSize);
        return new ArrayList<>(vv);
    }

    @VisibleForTesting
    LeafNodeGroup<K, V> split() {
        LeafNodeGroup<K, V> newLng = new LeafNodeGroup<>(this.nodeSize, this.numNodes);
        List<ArrayList<K>> subListOldLngKeys = this.keys.subList(this.numNodes / 2, this.numNodes);
        List<ArrayList<V>> subListOldLngValues = this.values.subList(this.numNodes / 2, this.numNodes);

        for (int i = 0; i < subListOldLngKeys.size(); i++) {
            for (int j = 0; j <subListOldLngKeys.get(i).size(); j++) {
                newLng.put((i * nodeSize) + j, subListOldLngKeys.get(i).get(j), subListOldLngValues.get(i).get(j));
            }
        }

        newLng.previous = this;
        newLng.next = this.next;
        if (this.next != null) {
            this.next.previous = newLng;
        }
        this.next = newLng;

        for (int i = (this.numNodes / 2) * this.nodeSize; i < this.numNodes * this.nodeSize; i++) {
            delete(i);
        }

        return newLng;
    }

    /**
     * The spec for this is very narrow!
     * You can only shift a continuous block of values.
     * If you shift a range and there's an empty field in there
     * the resulting node group will be wrong.
     */
    @Override
    void shiftOneRight(int from, int to) {
        for (int i = to; i > from; i--) {
            K key = getKey(i - 1);
            V value = getValue(i - 1);
            put(i, key, value, true);
        }

        // unrolling the delete method
        put(from, null, null, true);
    }

    private void delete(int idx) {
        // when passing in nulls
        // put takes care of the bit set
        // and the number of empty slots
        put(idx, null, null);
    }

    @Override
    K getKey(int index, int offset) {
        return this.keys.get(index).get(offset);
    }

    private V getValue(int index, int offset) {
        return this.values.get(index).get(offset);
    }

    @VisibleForTesting
    K getKey(int idx) {
        Pair<Integer, Integer> p = relativeAddress(idx);
        return getKey(p.getLeft(), p.getRight());
    }

    @VisibleForTesting
    V getValue(int idx) {
        Pair<Integer, Integer> p = relativeAddress(idx);
        return getValue(p.getLeft(), p.getRight());
    }

    @VisibleForTesting
    void put(int idx, K key, V value) {
        put(idx, key, value, false);
    }

    private void put(int idx, K key, V value, boolean isShifting) {
        doBookKeepingForPut(idx, key == null && value == null, isShifting);
        putKey(idx, key);
        putValue(idx, value);
    }

    private void putKey(int idx, K key) {
        Pair<Integer, Integer> p = relativeAddress(idx);
        putKey(p.getLeft(), p.getRight(), key);
    }

    private void putValue(int idx, V value) {
        Pair<Integer, Integer> p = relativeAddress(idx);
        putValue(p.getLeft(), p.getRight(), value);
    }

    private void putKey(int index, int offset, K key) {
        this.keys.get(index).set(offset, key);
    }

    private void putValue(int index, int offset, V value) {
        this.values.get(index).set(offset, value);
    }
}
