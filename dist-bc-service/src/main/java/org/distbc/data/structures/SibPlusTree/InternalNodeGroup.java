package org.distbc.data.structures.SibPlusTree;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

class InternalNodeGroup extends NodeGroup {
    private final short level;
    private List<InternalNode> nodes;

    // CONVENIENCE LAZY-NESS CTOR
    // don't use in prod!!!
    InternalNodeGroup(int level, int numberOfNodes, int nodeSize) {
        this((short) level, numberOfNodes, nodeSize);
    }

    InternalNodeGroup(short level, int numberOfNodes, int nodeSize) {
        super(numberOfNodes, nodeSize);
        this.level = level;
        Vector<InternalNode> v = new Vector<>(numberOfNodes);
        v.setSize(numberOfNodes);
        this.nodes = new ArrayList<>(v);
        for (int i = 0; i < numberOfNodes; i++) {
            nodes.set(i, new InternalNode(nodeSize));
        }
    }

    void put(int nodeIndex, int nodeOffset, Integer key) {
        nodes.get(nodeIndex).keys.set(nodeOffset, key);
        markFull(nodeIndex, nodeOffset);
    }

    void delete(int nodeIndex, int nodeOffset) {
        nodes.get(nodeIndex).keys.set(nodeOffset, null);
        markEmpty(nodeIndex, nodeOffset);
    }

    void setChild(int nodeIndex, NodeGroup child) {
        nodes.get(nodeIndex).child = child;
    }

    NodeGroup getChild(int nodeIndex) {
        return nodes.get(nodeIndex).child;
    }

    @Override
    Integer getKey(int nodeIndex, int nodeOffset) {
        return nodes.get(nodeIndex).keys.get(nodeOffset);
    }

    void setKey(int nodeIndex, int nodeOffset, Integer key) {
        nodes.get(nodeIndex).keys.set(nodeOffset, key);
    }

    @Override
    short getLevel() {
        return level;
    }

    @Override
    List<Integer> getHighestKeys() {
        List<Integer> l = new ArrayList<>(nodes.size());
        // this might be more complicated
        // assuming there will be nulls in the
        nodes.forEach(n -> l.add(n.getKey(n.keys.size() - 1)));
        return l;
    }

    void swapNodes(int fromIndex, int toIndex) {

    }

    int indexOfFirstEmptyNodeFromNode(int fromNodeIndex) {
        // TODO : search on both sides
        for (int i = fromNodeIndex * nodeSize; i < numberOfNodes; i++) {
            if (isNodeEmpty(i)) {
                return i;
            }
        }
        return -1;
    }

    private boolean isNodeEmpty(int nodeIndex) {
        int offset = nodeSize * nodeIndex;
        boolean isEmpty = true;
        for (int i = 0; i < nodeSize && isEmpty; i++) {
            isEmpty &= isEmpty(offset + i);
        }
        return isEmpty;
    }
}
