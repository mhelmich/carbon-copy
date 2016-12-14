package org.distbc.data.structures.cct;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
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
        InternalNodeGroup<K> newIng = new InternalNodeGroup<>(this.level, this.nodeSize, this.numNodes);
        List<ArrayList<K>> subListOldLngKeys = this.keys.subList(this.numNodes / 2, this.numNodes);

        for (int i = 0; i < subListOldLngKeys.size(); i++) {
            for (int j = 0; j <subListOldLngKeys.get(i).size(); j++) {
                newIng.put((i * nodeSize) + j, subListOldLngKeys.get(i).get(j));
            }
            newIng.setChildNodeOnNode(i, this.children.get((this.numNodes / 2) + i));
        }

        for (int i = (this.numNodes / 2) * this.nodeSize; i < this.numNodes * this.nodeSize; i++) {
            delete(i);
            if (i % this.nodeSize == 0) {
                setChildNodeOnNode(i / this.nodeSize, null);
            }
        }

        return newIng;
    }

    NodeGroup<K> getChildForNode(int nodeIdx) {
        return this.children.get(nodeIdx);
    }

    int findFirstNonNullChild() {
        return this.children.indexOf(null);
    }

    NodeGroup<K> getChild(int idx) {
        return getChildForNode(idx / nodeSize);
    }

    void setChildNode(int idx, @Nullable NodeGroup<K> child) {
        assert idx % nodeSize == 0;
        setChildNodeOnNode(idx / nodeSize, child);
    }

    void setChildNodeOnNode(int nodeIdx, @Nullable NodeGroup<K> child) {
        assert nodeIdx <= numNodes;
        this.children.set(nodeIdx, child);
        if (child != null) {
            // now set the keys automagically for this
            List<K> keysToSet = child.getHighestKeys();
            int baseIdx = nodeIdx * nodeSize;
            for (int i = 0; i < keysToSet.size() - 1; i++) {
                put(baseIdx + i, keysToSet.get(i), true);
            }
        }
    }

    void setGrandChildNodeOnNode(int nodeIdx, @Nullable NodeGroup<K> grandChild) {
        if (grandChild != null) {
            List<K> hks = grandChild.getHighestKeys();
            K hk = hks.get(hks.size() - 1);
            put((nodeIdx * nodeSize) + (nodeSize - 1), hk);
        }
    }

    int findNodeIndexOfEmptyNodeFrom(int idx) {
        int resIdx = findIndexOfEmptyNodeFrom(idx);
        return (resIdx < 0) ? -1 : resIdx / this.nodeSize;
    }

    int findIndexOfEmptyNodeFrom(int idx) {
        // this is the absolute index at the start of a node
        int beginningOfNodeAfterIdx = (idx / nodeSize) * nodeSize;

        while (beginningOfNodeAfterIdx < getTotalNodeGroupSize()) {
            if (isNodeEmpty(beginningOfNodeAfterIdx)) {
                return beginningOfNodeAfterIdx;
            }
            beginningOfNodeAfterIdx += nodeSize;
        }

        return -1;
    }

    private boolean isNodeEmpty(int idx) {
        assert idx % nodeSize == 0;

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
    void shiftNodesOneRight(int from, int to) {
        if (from < to) {
            int fromIdx = from * this.nodeSize;
            // this is an inclusive node index
            // and the node includes all fields until nodeSize - 1
            int toIdx = (to * this.nodeSize) + (this.nodeSize - 1);
            for (int i = 0; i < this.nodeSize; i++) {
                shiftOneRight(fromIdx, toIdx);
            }
            // "to" should be empty
            // hence a swap should have the same effect as shifting
            Collections.swap(this.children, from, to);
        }
    }

    @Override
    void shiftOneRight(int from, int to) {
        for (int i = to; i > from; i--) {
            K key = getKey(i - 1);
            put(i, key, true);
        }

        // unrolling the delete method
        put(from, null, true);
    }

    void put(int idx, @Nullable K key) {
        put(idx, key, false);
    }

    private void put(int idx, @Nullable K key, boolean dontDoBookkeeping) {
        doBookKeepingForPut(idx, key == null, dontDoBookkeeping);
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

    @Override
    @VisibleForTesting
    K getKey(int idx) {
        Pair<Integer, Integer> p = relativeAddress(idx);
        return getKey(p.getLeft(), p.getRight());
    }

    int getLevel() {
        return this.level;
    }
}
