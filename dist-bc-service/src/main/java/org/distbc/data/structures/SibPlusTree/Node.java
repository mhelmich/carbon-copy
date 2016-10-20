package org.distbc.data.structures.SibPlusTree;

/**
 * Created by mhelmich on 10/12/16.
 */
abstract class Node {
    protected boolean getBit(byte bite, int pos) {
        return (bite & (1 << (pos % 8))) != 0;
    }

    protected boolean getBit(byte[] bites, int pos) {
        return getBit(bites[pos / 8], pos);
    }

    protected byte setBit(byte bite, int pos, boolean b) {
        byte posBit = (byte) (1 << (pos % 8));
        if (b) {
            bite |= posBit;
        } else {
            bite &= (255 - posBit);
        }
        return bite;
    }

    protected byte[] setBit(byte[] bites, int pos, boolean b) {
        byte bite = bites[pos / 8];
        byte newBite = setBit(bite, pos, b);
        bites[pos / 8] = newBite;
        return bites;
    }
}
