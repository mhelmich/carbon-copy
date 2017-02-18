package org.distbc.data.structures.experimental.sibplustree;

import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

class LeafNodeGroup<K extends Comparable<K>, V extends Comparable<V>> extends NodeGroup<K> {

    private final ArrayList<ArrayList<K>> keys;
    private final ArrayList<ArrayList<V>> values;

    LeafNodeGroup<K, V> next;
    LeafNodeGroup<K, V> previous;

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

    @Override
    K getKey(int nodeIdx, int idx) {
        try {
            return this.keys.get(nodeIdx).get(idx);
        } catch (Exception xcp) {
            // ArrayOutOfBounds might happen here
            // may or may not happen when we're looking for a key bigger than
            // the biggest key in the tree
            return null;
        }
    }

    private V getValue(int nodeIdx, int idx) {
        return this.values.get(nodeIdx).get(idx);
    }

    V getValue(NodeIdxAndIdx indexes) {
        return getValue(indexes.nodeIdx, indexes.idx);
    }

    void maybeShiftOneRight(NodeIdxAndIdx from, NodeIdxAndIdx to) {
        if ((from.nodeIdx * nodeSize + from.idx) < (to.nodeIdx * nodeSize + to.idx)) {
            shiftOneRight(from, to);
        }
    }

    private void shiftOneRight(NodeIdxAndIdx fromIndexes, NodeIdxAndIdx toIndexes) {
        int from = fromIndexes.nodeIdx * nodeSize + fromIndexes.idx;
        int to = toIndexes.nodeIdx * nodeSize + toIndexes.idx;

        for (int i = to; i > from; i--) {
            NodeIdxAndIdx iIndexes = NodeIdxAndIdx.of(i / nodeSize, i % nodeSize);
            int j = i - 1;
            NodeIdxAndIdx jIndexes = NodeIdxAndIdx.of(j / nodeSize, j % nodeSize);
            K key = getKey(jIndexes);
            V value = getValue(jIndexes);
            put(iIndexes, key, value, true);
        }
    }

    void put(NodeIdxAndIdx indexes, @Nullable K key, @Nullable V value) {
        put(indexes, key, value, false);
    }

    private void put(NodeIdxAndIdx indexes, @Nullable K key, @Nullable V value, boolean isShifting) {
        doBookKeepingForPut(indexes, key == null && value == null, isShifting);
        putKey(indexes, key);
        putValue(indexes, value);
    }

    private void putKey(NodeIdxAndIdx indexes, @Nullable K key) {
        this.keys.get(indexes.nodeIdx).set(indexes.idx, key);
    }

    private void putValue(NodeIdxAndIdx indexes, @Nullable V value) {
        this.values.get(indexes.nodeIdx).set(indexes.idx, value);
    }

    LeafNodeGroup<K, V> split() {
        LeafNodeGroup<K, V> newLng = new LeafNodeGroup<>(this.nodeSize, this.numNodes);
        int divider = (int) Math.ceil(this.numNodes / 2.0);
        List<ArrayList<K>> subListOldLngKeys = this.keys.subList(divider, this.numNodes);
        List<ArrayList<V>> subListOldLngValues = this.values.subList(divider, this.numNodes);

        for (int i = 0; i < subListOldLngKeys.size(); i++) {
            for (int j = 0; j < subListOldLngKeys.get(i).size(); j++) {
                newLng.put(NodeIdxAndIdx.of(i, j), subListOldLngKeys.get(i).get(j), subListOldLngValues.get(i).get(j));
            }
        }

        newLng.previous = this;
        newLng.next = this.next;
        if (this.next != null) {
            this.next.previous = newLng;
        }
        this.next = newLng;

        for (int i = divider; i < this.numNodes; i++) {
            for (int j = 0; j < this.nodeSize; j++) {
                put(NodeIdxAndIdx.of(i, j), null, null);
            }
        }

        return newLng;
    }

    @Override
    public String toString() {
        return StringUtils.join(keys, ", ");
    }
}
