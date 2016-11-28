package org.distbc.data.structures.cct;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

class InternalNodeGroup<K extends Comparable<K>> extends NodeGroup<K> {
    private final ArrayList<ArrayList<K>> keys;
    // each node has a key linking to its child in the tree
    private ArrayList<NodeGroup<K>> children;
    private final int level;

    InternalNodeGroup(int level, int nodeSize, int numNodes) {
        super(nodeSize, numNodes);
        this.keys = initKeyLists();
        this.level = level;

        Vector<NodeGroup<K>> vc = new Vector<>(numNodes);
        vc.setSize(numNodes);
        this.children = new ArrayList<>(vc);
    }

    InternalNodeGroup<K> split() {
        InternalNodeGroup<K> newLng = new InternalNodeGroup<>(this.level, this.nodeSize, this.numNodes);
        List<ArrayList<K>> subListOldLngKeys = this.keys.subList(this.numNodes / 2, this.numNodes);

        for (int i = 0; i < subListOldLngKeys.size(); i++) {
            for (int j = 0; j <subListOldLngKeys.get(i).size(); j++) {
                newLng.put((i * nodeSize) + j, subListOldLngKeys.get(i).get(j));
            }
            newLng.setChildNodeOnNode(i, this.children.get((this.numNodes / 2) + i));
        }

        for (int i = (this.numNodes / 2) * this.nodeSize; i < this.numNodes * this.nodeSize; i++) {
            delete(i);
            setChildNode(i, null);
        }

        return newLng;
    }

    NodeGroup<K> getChild(int idx) {
        return this.children.get(idx / nodeSize);
    }

    void setChildNode(int idx, NodeGroup<K> child) {
        setChildNodeOnNode(idx / nodeSize, child);
    }

    private void setChildNodeOnNode(int nodeIdx, NodeGroup<K> child) {
        this.children.set(nodeIdx, child);
    }

    int findIndexOfEmptyNodeFrom(int idx) {
        int beginningOfNodeAfterIdx = (idx / nodeSize) * nodeSize;

        while (beginningOfNodeAfterIdx < nodeSize * numNodes) {
            if (isNodeEmpty(beginningOfNodeAfterIdx)) {
                return beginningOfNodeAfterIdx;
            }
            beginningOfNodeAfterIdx += nodeSize;
        }

        return -1;
    }

    private boolean isNodeEmpty(int idx) {
        if (idx % nodeSize != 0) {
            return false;
        }

        int foundIdx = findClosestEmptySlotFrom(idx);
        if (foundIdx == idx) {
            for (int i = 0; i < nodeSize; i++) {
                int j = findClosestEmptySlotFrom(foundIdx + i);
                if (j != foundIdx + i) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    /**
     * The spec for this is very narrow!
     * You can only shift a continuous block of values.
     * If you shift a range and there's an empty field in there
     * the resulting node group will be wrong.
     */
    void shiftOneRight(int from, int to) {
        for (int i = to; i > from; i--) {
            K key = getKey(i - 1);
            put(i, key);
        }

        delete(from);
    }

    void put(int idx, K key) {
        if (key == null && isFull(idx)) {
            if (isFull(idx)) {
                markEmpty(idx);
                this.numEmptySlots++;
            }
        } else {
            if (isEmpty(idx)) {
                markFull(idx);
                this.numEmptySlots--;
            }
        }
        Pair<Integer, Integer> p = relativeAddress(idx);
        putKey(p.getLeft(), p.getRight(), key);
    }

    private void putKey(int index, int offset, K key) {
        this.keys.get(index).set(offset, key);
    }

    private void delete(int idx) {
        // when passing in nulls
        // put takes care of the bit set
        // and the number of empty slots
        put(idx, null);
    }

    @Override
    K getKey(int index, int offset) {
        return this.keys.get(index).get(offset);
    }

    @VisibleForTesting
    K getKey(int idx) {
        Pair<Integer, Integer> p = relativeAddress(idx);
        return getKey(p.getLeft(), p.getRight());
    }
}
