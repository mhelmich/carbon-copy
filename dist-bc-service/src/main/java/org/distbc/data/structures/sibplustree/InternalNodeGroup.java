package org.distbc.data.structures.sibplustree;

import org.apache.commons.lang3.StringUtils;

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

        Vector<NodeGroup<K>> vc = new Vector<>(nodeSize + 1);
        vc.setSize(nodeSize + 1);
        this.children = new ArrayList<>(vc);
    }

    void setChildNodeOnNode(int nodeIdx, @Nullable NodeGroup<K> child) {
        this.children.set(nodeIdx, child);
    }

    NodeGroup<K> getChildForNode(int nodeIdx) {
        return this.children.get(nodeIdx);
    }

    @Override
    int getLevel() {
        return this.level;
    }

    @Override
    K getKey(int index, int offset) {
        try {
            return this.keys.get(index).get(offset);
        } catch (Exception xcp) {
            // ArrayOutOfBounds might happen here
            // may or may not happen when we're looking for a key bigger than
            // the biggest key in the tree
            return null;
        }
    }

    void put(NodeIdxAndIdx indexes, @Nullable K key) {
        doBookKeepingForPut(indexes, key == null, false);
        putKey(indexes, key);
    }

    private void putKey(NodeIdxAndIdx indexes, @Nullable K key) {
        this.keys.get(indexes.nodeIdx).set(indexes.idx, key);
    }

    InternalNodeGroup<K> split() {
        InternalNodeGroup<K> newIng = new InternalNodeGroup<>(this.level, this.nodeSize, this.numNodes);
        int divider = (int) Math.ceil(this.numNodes / 2.0);
        List<ArrayList<K>> subListOldIngKeys = this.keys.subList(divider, this.numNodes);

        for (int i = 0; i < subListOldIngKeys.size(); i++) {
            for (int j = 0; j < subListOldIngKeys.get(i).size(); j++) {
                newIng.put(NodeIdxAndIdx.of(i, j), subListOldIngKeys.get(i).get(j));
            }
            newIng.setChildNodeOnNode(i, this.children.get(divider + i));
        }

        for (int i = 0; i < subListOldIngKeys.size(); i++) {
            int nodeIdx = divider + i;
            for (int j = 0; j < subListOldIngKeys.get(i).size(); j++) {
                put(NodeIdxAndIdx.of(nodeIdx, j), null);
            }
            setChildNodeOnNode(nodeIdx, null);
        }

        return newIng;
    }

    NodeIdxAndIdx findNodeIndexOfEmptyNodeFrom(NodeIdxAndIdx indexes) {
        for (int i = indexes.nodeIdx; i < numNodes; i++) {
            if (isNodeEmpty(i)) {
                return NodeIdxAndIdx.of(i, 0);
            }
        }
        return NodeIdxAndIdx.INVALID;
    }

    void shiftNodesOneRight(NodeIdxAndIdx from, NodeIdxAndIdx to) {
        if (from.nodeIdx < to.nodeIdx) {
            for (int i = to.nodeIdx - 1; i > -1; i--) {
                Collections.swap(this.keys, i, i + 1);
            }

            swapBits(from.nodeIdx, to.nodeIdx);

            // "to" should be empty
            // hence a swap should have the same effect as shifting
            Collections.swap(this.children, from.nodeIdx, to.nodeIdx);
        }
    }

    @Override
    public String toString() {
        return StringUtils.join(keys, ", ");
    }
}
