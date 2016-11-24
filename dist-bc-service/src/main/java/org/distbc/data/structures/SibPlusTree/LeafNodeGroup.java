package org.distbc.data.structures.SibPlusTree;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * An entire leaf node group identified by being on level zero of the tree.
 * This is merely a container for several LeafNodes providing surrounding
 * logic for shifting (and hence packing) data into nodes and node groups.
 *
 * They also implement splits on their level. Much of the black magic related to
 * tree house-keeping happens in this class.
 *
 * TODO: make this thread-safe / multi thread it
 * - you could conceive of CAS operations and optimistic locking happening
 * - beware of the order in which you do things: everything needs to be searchable by concurrent threads
 * - CAS operations might only operate on the "full array" - that could be the source of truth
 */
class LeafNodeGroup extends NodeGroup {
    List<LeafNode> nodes;
    private LeafNodeGroup next;
    private LeafNodeGroup previous;
    LeafNodeGroup(int numberOfNodes, int nodeSize) {
        this(numberOfNodes, nodeSize, true);
    }

    private LeafNodeGroup(int numberOfNodes, int nodeSize, boolean init) {
        super(numberOfNodes, nodeSize);
        if (init) {
            // the vector allows to extend the underlying array
            // ArrayLists don't do that for me
            Vector<LeafNode> v = new Vector<>(numberOfNodes);
            v.setSize(numberOfNodes);
            this.nodes = new ArrayList<>(v);
            for (int i = 0; i < numberOfNodes; i++) {
                nodes.set(i, new LeafNode(nodeSize));
            }
        }
    }

    // entry point for all shifting
    // the only thing we do is deciding which way we have to shift
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
        Pair<Integer, String> spillOver;
        if (emptyNodeIndex == toFillNodeIndex) {
            // if indexes are the same we just need to shift within a node
            // only the offsets matter in this case
            spillOver = nodes.get(emptyNodeIndex).shift(emptyOffset, toFillOffset, null, null);
        } else {
            // we shift between nodes and to the left
            // that means we go from the offset and carry over from the left end (zero)
            spillOver = nodes.get(emptyNodeIndex).shift(emptyOffset, 0, null, null);
        }

        // all nodes in between from and to
        // just shift from one end to the other
        Integer spillOverKey = spillOver.getLeft();
        String spillOverValue = spillOver.getRight();
        for (int i = emptyNodeIndex -1; i > toFillNodeIndex && i > 0; i--) {
            spillOver = nodes.get(i).shift(nodes.get(i).keys.size() -1, 0, spillOverKey, spillOverValue);
            spillOverKey = spillOver.getLeft();
            spillOverValue = spillOver.getRight();
        }

        // in case there were more than one node to shift across
        // we shift from the very right until "toFillIndex"
        // TODO: this needs better testing
        if (emptyNodeIndex != toFillNodeIndex) {
            nodes.get(toFillNodeIndex).shift(nodes.get(toFillNodeIndex).keys.size() -1, toFillOffset, spillOverKey, spillOverValue);
        }
    }

    private void shiftRight(int emptyNodeIndex, int emptyOffset, int toFillNodeIndex, int toFillOffset) {
        markFull(toFillNodeIndex, toFillOffset);

        Pair<Integer, String> spillOver;
        if (emptyNodeIndex == toFillNodeIndex) {
            spillOver = nodes.get(emptyNodeIndex).shift(emptyOffset, toFillOffset, null, null);
        } else {
            spillOver = nodes.get(emptyNodeIndex).shift(emptyOffset, nodes.get(emptyNodeIndex).keys.size() -1, null, null);
        }

        Integer spillOverKey = spillOver.getLeft();
        String spillOverValue = spillOver.getRight();
        for (int i = emptyNodeIndex +1; i < toFillNodeIndex && i < nodes.size(); i++) {
            spillOver = nodes.get(i).shift(0, nodes.get(i).keys.size() -1, spillOverKey, spillOverValue);
            spillOverKey = spillOver.getLeft();
            spillOverValue = spillOver.getRight();
        }

        if (emptyNodeIndex != toFillNodeIndex) {
            nodes.get(toFillNodeIndex).shift(0, toFillOffset, spillOverKey, spillOverValue);
        }
    }

    void put(int nodeIndex, int nodeOffset, Integer key, String value) {
        int desiredPosition = absolutePosition(nodeIndex, nodeOffset);
        if (isFull(desiredPosition)) {
            int closestEmptyPosition = findClosestEmptySlotFrom(desiredPosition);
            shift(desiredPosition, closestEmptyPosition);
        }
        // FIXME: unroll this code to avoid setting the bit again
        markFull(nodeIndex, nodeOffset);
        nodes.get(nodeIndex).put(nodeOffset, key, value);
    }

    void delete(int nodeIndex, int nodeOffset) {
        nodes.get(nodeIndex).put(nodeOffset, null, null);
        markEmpty(nodeIndex, nodeOffset);
    }

    /**
     * TODO: in a later version I could experiment with different spacing algorithms
     *       - not packing everything to the left but distribute it sparsely
     */
    LeafNodeGroup split() {
        // splitting works in a way that the right half of a node group
        // moves to the be left half of a node group in the new node
        // that might work well because we generally shift values to the right

        // we always split to the right
        LeafNodeGroup newLng = new LeafNodeGroup(numberOfNodes, nodeSize, false);
        // number of nodes could be odd as well
        // in that case the right split node group has one more node
        int splitNodeIndex = numberOfNodes / 2;
        int numNodesMovedToTheNewNode = nodes.size() - splitNodeIndex;

        // create node data structure
        List<LeafNode> oldNodesList = nodes.subList(0, splitNodeIndex);
        List<LeafNode> newNodesList = nodes.subList(splitNodeIndex, nodes.size());

        oldNodesList = createNodeList(oldNodesList, numberOfNodes);
        newNodesList = createNodeList(newNodesList, numberOfNodes);

        this.nodes = oldNodesList;
        newLng.nodes = newNodesList;

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

    private List<LeafNode> createNodeList(List<LeafNode> nodesList, int numberOfNodes) {
        Vector<LeafNode> v = new Vector<>(nodesList);
        v.setSize(numberOfNodes);
        // fill all fields with leaf nodes
        for (int i = nodesList.size(); i < numberOfNodes; i++) {
            v.set(i, new LeafNode(nodeSize));
        }
        return new ArrayList<>(v);
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
        // assuming there will be nulls in the list
        nodes.forEach(n -> l.add(n.getKey(n.keys.size() - 1)));
        return l;
    }
}
