package org.distbc.data.structures.SibPlusTree;

import java.util.Arrays;

/**
 * Created by mhelmich on 10/12/16.
 */
class InternalNodeGroup extends NodeGroup {
    final short level;
    final InternalNode[] nodes;
    InternalNodeGroup(short level, int numberOfNodes, int nodeSize) {
        super(numberOfNodes, nodeSize);
        this.level = level;
        this.nodes = new InternalNode[numberOfNodes];
        Arrays.fill(this.nodes, new InternalNode(nodeSize));
    }
}
