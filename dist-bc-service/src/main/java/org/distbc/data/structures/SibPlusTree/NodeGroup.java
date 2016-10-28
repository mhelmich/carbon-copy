package org.distbc.data.structures.SibPlusTree;

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
        int bitArraySize = numberOfNodes * nodeSize;
        numEmptySlots = (short) Math.min(bitArraySize, Short.MAX_VALUE);
        full = new byte[
                (bitArraySize % 8 == 0)
                        ? bitArraySize / 8
                        : (bitArraySize / 8) + 1
                ];
        this.numberOfNodes = numberOfNodes;
        this.nodeSize = nodeSize;
    }

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

    boolean isFull(int pos) {
        return getBit(full, pos);
    }

    // TODO: convince Marco to finally toss this code away
//        protected int getNextEmptyAfterPosInSameNode(int numNode, int positionInNode) {
//            int ceiling = (numNode + 1) * nodeSize;
//            int floor = (numNode * nodeSize) + positionInNode;
//            for (int i = ceiling; i > floor; i--) {
//                if (!getBit(full, i)) return Math.max(i - 1, 0);
//            }
//            return -1;
//        }

    int findClosestEmptySlotFrom(int position) {
        if (!getBit(full, position)) return position;

        int offsetFromPosition = 1;
        while ((position + offsetFromPosition) < (full.length * 8)
                || (position - offsetFromPosition) >= 0) {
            if ((position + offsetFromPosition) < (full.length * 8) && !getBit(full, (position + offsetFromPosition))) {
                return position + offsetFromPosition;
            }

            // TODO
            // this would enable search in both directions
            // right now code is not written for this so I commented it out
//            if ((position - offsetFromPosition >= 0) && !getBit(full, (position - offsetFromPosition))) {
//                return position - offsetFromPosition;
//            }
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
}
