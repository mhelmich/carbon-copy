package org.distbc.data.structures.SibPlusTree;

import java.util.List;

abstract class NodeGroup {
    // bit array for full spots in the node group
    private byte[] full;
    // number of empty slots
    short numEmptySlots;
    // meta-data about the node
    // this could be static per tree
    // since it never changes for a particular tree
    transient int numberOfNodes;
    transient int nodeSize;

    NodeGroup(int numberOfNodes, int nodeSize) {
        int numBitsNecessary = numberOfNodes * nodeSize;
        numEmptySlots = (short) Math.min(numBitsNecessary, Short.MAX_VALUE);
        // one byte is eight bits (yes, just like in your CS101)
        // if numBitsNecessary mod 8 has no remainder, we just need as many bytes
        // and the division results in zero
        // if there is a remainder, we need one more byte
        // this might result in empty bits at the end but a byte is the smallest data type
        full = new byte[
                (numBitsNecessary % 8 == 0)
                        ? numBitsNecessary / 8
                        : (numBitsNecessary / 8) + 1
                ];
        this.numberOfNodes = numberOfNodes;
        this.nodeSize = nodeSize;
    }

    ///////////////////////////////////////////////
    ////////////////////////////////////
    //// This family of methods does black magic on byte arrays.
    // They interpret the byte array as a series of bits and flip each bit
    // separately to mark a particular slot in the node group as occupied or not.
    // These methods are the basis for all methods
    private boolean getBit(byte bite, int pos) {
        return (bite & (1 << (pos % 8))) != 0;
    }

    private boolean getBit(byte[] bites, int pos) {
        return getBit(bites[pos / 8], pos % 8);
    }

    private byte setBit(byte bite, int pos, boolean b) {
        byte posBit = (byte) (1 << (pos % 8));
        if (b) {
            bite |= posBit;
        } else {
            bite &= (255 - posBit);
        }
        return bite;
    }

    private byte[] setBit(byte[] bites, int pos, boolean b) {
        byte bite = bites[pos / 8];
        byte newBite = setBit(bite, pos, b);
        bites[pos / 8] = newBite;
        return bites;
    }

    void markFull(int nodeIndex, int nodeOffset) {
        markFull(absolutePosition(nodeIndex, nodeOffset));
    }

    void markFull(int pos) {
        numEmptySlots--;
        setBit(full, pos, true);
    }

    void markEmpty(int nodeIndex, int nodeOffset) {
        markEmpty(absolutePosition(nodeIndex, nodeOffset));
    }

    void markEmpty(int pos) {
        numEmptySlots++;
        setBit(full, pos, false);
    }

    boolean isEmpty(int pos) {
        return !getBit(full, pos);
    }

    boolean isFull(int pos) {
        return getBit(full, pos);
    }

    /**
     * Returns the index of the closest empty slot in this node group.
     * "Closest" can be either on the left or right side of position (which is meant
     * to say "desired position" really).
     *
     * If there are two empty slots with the same distance to position,
     * it will return the empty slot with the greater index (aka to the right of position).
     *
     * @param position
     * @return
     */
    int findClosestEmptySlotFrom(int position) {
        if (!getBit(full, position)) return position;

        int offsetFromPosition = 1;
        while ((position + offsetFromPosition) < (numberOfNodes * nodeSize)
                || (position - offsetFromPosition) >= 0) {
            if ((position + offsetFromPosition) < (numberOfNodes * nodeSize) && !getBit(full, (position + offsetFromPosition))) {
                return position + offsetFromPosition;
            }

            if ((position - offsetFromPosition >= 0) && !getBit(full, (position - offsetFromPosition))) {
                return position - offsetFromPosition;
            }
            offsetFromPosition++;
        }

        throw new IllegalStateException("Couldn't find empty slot in node group");
    }

    boolean hasSpace() {
        return numEmptySlots > 0;
    }

    int absolutePosition(int nodeIndex, int nodeOffset) {
        return (nodeIndex * nodeSize) + nodeOffset;
    }

    abstract short getLevel();

    abstract Integer getKey(int nodeIndex, int nodeOffset);

    abstract List<Integer> getHighestKeys();
}
