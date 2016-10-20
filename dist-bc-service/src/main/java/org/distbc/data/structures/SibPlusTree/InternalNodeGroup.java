package org.distbc.data.structures.SibPlusTree;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by mhelmich on 10/12/16.
 */
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

    Integer getKey(int nodeIndex, int nodeOffset) {
        return nodes.get(nodeIndex).keys.get(nodeOffset);
    }

    InternalNodeGroup split() {
        InternalNodeGroup newNode = new InternalNodeGroup(level, numberOfNodes, nodeSize);
        return newNode;
    }
}
