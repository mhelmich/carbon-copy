package org.distbc.data.structures.cct;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Vector;

abstract class NodeGroup<K extends Comparable<K>> {
    private BitSet full;
    int nodeSize;
    int numNodes;
    int numEmptySlots;

    NodeGroup(int nodeSize, int numNodes) {
        full = new BitSet(nodeSize * numNodes);
        this.nodeSize = nodeSize;
        this.numNodes = numNodes;
        this.numEmptySlots = nodeSize * numNodes;
    }

    ArrayList<ArrayList<K>> initKeyLists() {
        Vector<ArrayList<K>> vks = new Vector<>(numNodes);
        vks.setSize(numNodes);
        ArrayList<ArrayList<K>> keys = new ArrayList<>(vks);
        for (int i = 0; i < numNodes; i++) {
            keys.set(i, newKeyList());
        }
        return keys;
    }

    private ArrayList<K> newKeyList() {
        Vector<K> vk = new Vector<>(nodeSize);
        vk.setSize(nodeSize);
        return new ArrayList<>(vk);
    }

    @VisibleForTesting
    int getEmptySlots() {
        return this.numEmptySlots;
    }

    boolean hasEmptySlots() {
        return getEmptySlots() > 0;
    }

    void markEmpty(int idx) {
        full.clear(idx);
    }

    void markFull(int idx) {
        full.set(idx);
    }

    boolean isEmpty(int idx) {
        return !full.get(idx);
    }

    boolean isFull(int idx) {
        return full.get(idx);
    }

    int findClosestEmptySlotFrom(int idx) {
        return full.nextClearBit(idx);
    }

    Pair<Integer, Integer> relativeAddress(int idx) {
        return Pair.of(idx / nodeSize, idx % nodeSize);
    }

    abstract K getKey(int index, int offset);

    List<K> getHighestKeys() {
        // array lists can contain nulls
        // linked lists can't -- thanks java
        List<K> l = new ArrayList<>(numNodes);
        // this might be more complicated
        // assuming there will be nulls in the list
        for (int i = 0; i < numNodes; i++) {
            l.add(getKey(i, nodeSize - 1));
        }
        return l;
    }
}
