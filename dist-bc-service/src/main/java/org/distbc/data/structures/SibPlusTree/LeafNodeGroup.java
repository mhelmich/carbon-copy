package org.distbc.data.structures.SibPlusTree;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * TODO: make this thread-safe / multi thread it
 * - you could conceive of CAS operations and optimistic locking happening
 * - beware of the order in which you do things: everything needs to be searchable by concurrent threads
 *
 */
class LeafNodeGroup extends NodeGroup {
    List<LeafNode> nodes;
    LeafNodeGroup next;
    LeafNodeGroup previous;
    LeafNodeGroup(int numberOfNodes, int nodeSize) {
        super(numberOfNodes, nodeSize);
        Vector<LeafNode> v = new Vector<>(numberOfNodes);
        v.setSize(numberOfNodes);
        this.nodes = new ArrayList<>(v);
        for (int i = 0; i < numberOfNodes; i++) {
            nodes.set(i, new LeafNode(nodeSize));
        }
    }

    private void shift(int absoluteOffsetToShiftFrom, int absoluteOffsetToShiftTo) {
        int fromNodeIndex = absoluteOffsetToShiftFrom / nodeSize;
        int fromNodeOffset = (absoluteOffsetToShiftFrom % nodeSize);
        int toNodeIndex = absoluteOffsetToShiftTo / nodeSize;
        int toNodeOffset = absoluteOffsetToShiftTo % nodeSize;
        if (absoluteOffsetToShiftFrom > absoluteOffsetToShiftTo) {
            shiftLeft(fromNodeIndex, fromNodeOffset, toNodeIndex, toNodeOffset);
        } else {
            shiftRight(fromNodeIndex, fromNodeOffset, toNodeIndex, toNodeOffset);
        }
        markEmpty(absoluteOffsetToShiftFrom);
    }

    private void shiftLeft(int emptyNodeIndex, int emptyOffset, int toFillNodeIndex, int toFillOffset) {
        markFull(toFillNodeIndex, toFillOffset);
        // TODO: actually make this work and test this
        Pair<Integer, String> spillOver;
        if (emptyNodeIndex == toFillNodeIndex) {
            spillOver = nodes.get(emptyNodeIndex).shiftLeft(emptyOffset, toFillOffset, null, null);
        } else {
            spillOver = nodes.get(emptyNodeIndex).shiftLeft(emptyOffset, 0, null, null);
        }

        Integer spillOverKey = spillOver.getLeft();
        String spillOverValue = spillOver.getRight();
        for (int i = emptyNodeIndex -1; i > toFillNodeIndex && i > 0; i--) {
            spillOver = nodes.get(i).shiftLeft(nodes.get(i).keys.size() -1, 0, spillOverKey, spillOverValue);
            spillOverKey = spillOver.getLeft();
            spillOverValue = spillOver.getRight();
        }

        if (emptyNodeIndex != toFillNodeIndex) {
            nodes.get(toFillNodeIndex).shiftLeft(emptyOffset, 0, spillOverKey, spillOverValue);
        }
    }

    private void shiftRight(int emptyNodeIndex, int emptyOffset, int toFillNodeIndex, int toFillOffset) {
        markFull(toFillNodeIndex, toFillOffset);

        Pair<Integer, String> spillOver;
        if (emptyNodeIndex == toFillNodeIndex) {
            spillOver = nodes.get(emptyNodeIndex).shiftRight(emptyOffset, toFillOffset, null, null);
        } else {
            spillOver = nodes.get(emptyNodeIndex).shiftRight(emptyOffset, nodes.get(emptyNodeIndex).keys.size() -1, null, null);
        }

        Integer spillOverKey = spillOver.getLeft();
        String spillOverValue = spillOver.getRight();
        for (int i = emptyNodeIndex +1; i < toFillNodeIndex && i < nodes.size(); i++) {
            spillOver = nodes.get(i).shiftRight(0, nodes.get(i).keys.size() -1, spillOverKey, spillOverValue);
            spillOverKey = spillOver.getLeft();
            spillOverValue = spillOver.getRight();
        }

        if (emptyNodeIndex != toFillNodeIndex) {
            nodes.get(toFillNodeIndex).shiftRight(0, toFillOffset, spillOverKey, spillOverValue);
        }
    }

    void put(int nodeIndex, int nodeOffset, Integer key, String value) {
        int desiredPosition = absolutePosition(nodeIndex, nodeOffset);
        if (isFull(desiredPosition)) {
            int closestEmptyPosition = findClosestEmptySlotFrom(desiredPosition);
            shift(desiredPosition, closestEmptyPosition);
        }
        nodes.get(nodeIndex).put(nodeOffset, key, value);
        // FIXME: unroll this code to avoid setting the bit again
        markFull(nodeIndex, nodeOffset);
    }

    void delete(int nodeIndex, int nodeOffset) {
        nodes.get(nodeIndex).put(nodeOffset, null, null);
        markEmpty(nodeIndex, nodeOffset);
    }

    void merge() {}

    /**
     * TODO: in a later version I could experiment with different spacing algorithms
     *       - not packing everything to the left but distribute it sparsely
     */
    LeafNodeGroup split() {
        // splitting works in a way that the right half of a node group
        // moves to the be left half of a node group in the new node
        // that might work well because we generally shift values to the right

        // we always split to the right
        LeafNodeGroup newLng = new LeafNodeGroup(numberOfNodes, nodeSize);
        // number of nodes could be odd as well
        // in that case the right split node group has one more node
        int splitNodeIndex = numberOfNodes / 2;
        int numNodesMovedToTheNewNode = nodes.size() - splitNodeIndex;

        // create node data structure
        Vector<LeafNode> v = new Vector<>(nodes.subList(splitNodeIndex, nodes.size()));
        v.setSize(numberOfNodes);
        newLng.nodes = new ArrayList<>(v);
        // set full bits
        for (int i = 0; i < numNodesMovedToTheNewNode * nodeSize; i++) {
            newLng.markFull(i);
        }
        // set available space for new node
        newLng.numEmptySlots = (short) Math.min((numberOfNodes - numNodesMovedToTheNewNode) * nodeSize, Short.MAX_VALUE);
        // set prev and next
        newLng.previous = this;
        newLng.next = this.next;

        // set prev and next for this
        this.next = newLng;
        // reset nodes for this
        for (int i = splitNodeIndex; i < this.nodes.size(); i++) {
            this.nodes.set(i, new LeafNode(nodeSize));
        }
        // set full bits for this
        for (int i = splitNodeIndex * nodeSize; i < this.nodes.size() * nodeSize; i++) {
            this.markEmpty(i);
        }
        // set available space for this
        this.numEmptySlots = (short) Math.min(nodeSize * numNodesMovedToTheNewNode, Short.MAX_VALUE);

        return newLng;
    }

    @Override
    Integer getKey(int nodeIndex, int nodeOffset) {
        return nodes.get(nodeIndex).getKey(nodeOffset);
    }

    String getValue(int nodeIndex, int nodeOffset) {
        return nodes.get(nodeIndex).values.get(nodeOffset);
    }

    @Override
    short getLevel() {
        return 0;
    }

    @Override
    List<Integer> getHighestKeys() {
        // array lists can contain nulls
        // linked lists can't -- thanks java
        List<Integer> l = new ArrayList<>(nodes.size());
        // this might be more complicated
        // assuming there will be nulls in the
        nodes.forEach(n -> l.add(n.getKey(n.keys.size() - 1)));
        return l;
    }
}
