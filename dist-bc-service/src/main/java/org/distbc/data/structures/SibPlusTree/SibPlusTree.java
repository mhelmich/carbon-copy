package org.distbc.data.structures.SibPlusTree;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class SibPlusTree {
    private static final int MAX_BYTE_SIZE = 32768;

    private InternalNodeGroup root;

    private final int keySizeInBytes;
    private final int valueSizeInBytes;
    private final int pointerSizeInBytes;
    private final int numberOfNodesInInternalNodeGroup;
    private final int numberOfNodesInLeafNodeGroup;
    private final int internalNodeSize;
    private final int leafNodeSize;

    SibPlusTree() {
        // TODO : find a way to plug in real sizes of types
        keySizeInBytes = 4;
        pointerSizeInBytes = 8;
        valueSizeInBytes = 16;
        leafNodeSize = 32;

        numberOfNodesInLeafNodeGroup = ((MAX_BYTE_SIZE * 8) - (2 * pointerSizeInBytes * 8)) / ((((keySizeInBytes + valueSizeInBytes) * 8) + 1) * leafNodeSize);
        internalNodeSize = numberOfNodesInLeafNodeGroup - 1;
        numberOfNodesInInternalNodeGroup = ((MAX_BYTE_SIZE * 8) - (pointerSizeInBytes * 8)) / (internalNodeSize * ((keySizeInBytes * 8) + 1));

        // allocate memory greedily
        root = new InternalNodeGroup((short)1, numberOfNodesInInternalNodeGroup, internalNodeSize);
        LeafNodeGroup lng = new LeafNodeGroup(numberOfNodesInLeafNodeGroup, leafNodeSize);
        root.setChild(0, lng);
    }

    public void put(Integer key, String value) {
//        List<InternalNodeGroup> treeTrace = new ArrayList<>();
        NodeGroup ng = root;
        int offset = 0;
        while (!(ng instanceof LeafNodeGroup)) {
            Pair<NodeGroup, Integer> newNGAndOffset = searchThroughInternalNodes(key, (InternalNodeGroup) ng, offset);
            ng = newNGAndOffset.getLeft();
            offset = newNGAndOffset.getRight();
        }

        LeafNodeGroup leafNG = (LeafNodeGroup) ng;
        int withinNodeOffset = searchInLeafNodeGroup(key, leafNG, offset);

        if (leafNG.hasSpace()) {
            leafNG.put(offset, withinNodeOffset, key, value);
        } else {
            // split happens..!
            // and that will likely turn into a recurse affair
            // since internal node groups must be split as well
        }
    }

    public String search(Integer key) {
        // iterative search instead of recursive
        NodeGroup ng = root;
        int offset = 0;
        while (!(ng instanceof LeafNodeGroup)) {
            Pair<NodeGroup, Integer> newNGAndOffset = searchThroughInternalNodes(key, (InternalNodeGroup) ng, offset);
            ng = newNGAndOffset.getLeft();
            offset = newNGAndOffset.getRight();
        }

        LeafNodeGroup leafNG = (LeafNodeGroup) ng;
        int withinNodeOffset = searchInLeafNodeGroup(key, leafNG, offset);
        return leafNG.getValue(offset, withinNodeOffset);
    }

    private Pair<NodeGroup, Integer> searchThroughInternalNodes(Integer key, InternalNodeGroup n, int offset) {
        for (int i = offset; i < numberOfNodesInInternalNodeGroup; i++) {
            for (int j = 0; j < internalNodeSize; j++) {
                if (n.getKey(i, j) == null) {
                    return new ImmutablePair<>(n.getChild(i), j);
                }
                int cmp = key.compareTo(n.getKey(i, j));
                if (cmp < 0) {
                    continue;
                } else if (cmp == 0) {
                    return new ImmutablePair<>(n.getChild(i), j);
                } else if (cmp > 0) {
                    return new ImmutablePair<>(n.getChild(i), j - 1);
                }
            }
        }

        // TODO : what to do here???
        throw new IllegalStateException("Can't do anything with the key you gave me");
    }

    private int searchInLeafNodeGroup(Integer key, LeafNodeGroup n, int offset) {
        for (int i = offset; i < numberOfNodesInLeafNodeGroup; i++) {
            for (int j = 0; j < leafNodeSize; j++) {
                if (n.getKey(i, j) == null) {
                    return j;
                }

                int cmp = key.compareTo(n.getKey(i, j));
                if (cmp < 0) {
                    continue;
                } else if (cmp == 0) {
                    return j;
                } else if (cmp > 0) {
                    return j - 1;
                }
            }
        }
        return 0;
    }
}
