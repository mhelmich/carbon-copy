package org.distbc.data.structures.sibplustree;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Vector;

abstract class NodeGroup<K extends Comparable<K>> {

    private BitSet full;
    private int numEmptySlots;
    int nodeSize;
    int numNodes;

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

    private void markEmpty(NodeIdxAndIdx indexes) {
        int idx = indexes.nodeIdx * nodeSize + indexes.idx;
        full.clear(idx);
    }

    private void markFull(NodeIdxAndIdx indexes) {
        int idx = indexes.nodeIdx * nodeSize + indexes.idx;
        full.set(idx);
    }

    boolean isNodeEmpty(int nodeIdx) {
        return full.get(nodeIdx * nodeSize, nodeIdx * nodeSize + nodeSize - 1).isEmpty();
    }

    void swapBits(int nodeIdxFrom, int nodeIdxTo) {
        int fromBaseIdx = nodeIdxFrom * nodeSize;
        BitSet tmpFrom = full.get(fromBaseIdx, fromBaseIdx + nodeSize);
        int toBaseIdx = nodeIdxTo * nodeSize;
        BitSet tmpTo = full.get(toBaseIdx, toBaseIdx + nodeSize);
        for (int i = 0; i < nodeSize; i++) {
            full.set(fromBaseIdx + i, tmpTo.get(i));
            full.set(toBaseIdx + i, tmpFrom.get(i));
        }
    }

    int getLevel() {
        return 0;
    }

    abstract K getKey(int index, int offset);

    K getKey(NodeIdxAndIdx indexes) {
        return getKey(indexes.nodeIdx, indexes.idx);
    }

    NodeIdxAndIdx findClosestEmptySlotFrom(NodeIdxAndIdx indexes) {
        return findClosestEmptySlotFrom(indexes.nodeIdx, indexes.idx);
    }

    private NodeIdxAndIdx findClosestEmptySlotFrom(int nodeIdx, int idx) {
        int startIdx = nodeIdx * nodeSize + idx;
        int emptySlotIdx = full.nextClearBit(startIdx);
        return (emptySlotIdx > -1 && emptySlotIdx < nodeSize * numNodes)
                ? NodeIdxAndIdx.of(emptySlotIdx / nodeSize, emptySlotIdx % nodeSize)
                : NodeIdxAndIdx.INVALID;
    }

    void doBookKeepingForPut(NodeIdxAndIdx indexes, boolean isKeyAndValueNull, boolean isShifting) {
        // a put with nulls is a delete
        // delete and shift might transport null values
        if (isKeyAndValueNull) {
            markEmpty(indexes);
            // when we shift we don't want to modify the empty slots
            // because we...yeah...only shift
            if (!isShifting) {
                this.numEmptySlots++;
            }
        } else {
            markFull(indexes);
            if (!isShifting) {
                // when we shift we don't want to modify the empty slots
                // because we...yeah...only shift
                this.numEmptySlots--;
            }
        }
    }

    K getHighestKey() {
        return getKey(numNodes - 1, nodeSize - 1);
    }

    NodeIdxAndIdx plusOne(NodeIdxAndIdx indexes) {
        int newIdx = indexes.nodeIdx * nodeSize + indexes.idx;
        newIdx = newIdx + 1;
        return (newIdx < nodeSize * numNodes) ? NodeIdxAndIdx.of(newIdx / nodeSize, newIdx % nodeSize) : NodeIdxAndIdx.INVALID;
    }
}
