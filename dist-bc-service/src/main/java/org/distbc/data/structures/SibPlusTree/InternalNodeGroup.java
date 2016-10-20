package org.distbc.data.structures.SibPlusTree;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by mhelmich on 10/12/16.
 */
class InternalNodeGroup extends NodeGroup {
    final short level;
    final List<InternalNode> nodes;
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
}
