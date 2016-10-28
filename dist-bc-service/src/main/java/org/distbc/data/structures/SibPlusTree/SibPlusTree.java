package org.distbc.data.structures.SibPlusTree;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class SibPlusTree {
    private static final Logger logger = Logger.getLogger(SibPlusTree.class);
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
        List<Pair<InternalNodeGroup, Integer>> treeTrace = new ArrayList<>();
        NodeGroup ng = root;
        int offset = 0;
        while (ng.getLevel() > 0) {
            treeTrace.add(Pair.of((InternalNodeGroup) ng, offset));
            Pair<NodeGroup, Integer> newNGAndOffset = searchThroughInternalNodes(key, (InternalNodeGroup) ng, offset);
            ng = newNGAndOffset.getLeft();
            offset = newNGAndOffset.getRight();
        }

        LeafNodeGroup leafNG = (LeafNodeGroup) ng;
        int withinNodeOffset = searchInLeafNodeGroup(key, leafNG, offset);

        if (leafNG.hasSpace()) {
            // the easy case, we just drop the new key into the leaf node group
            leafNG.put(offset, withinNodeOffset, key, value);
            List<Integer> highestKeysLeaf = leafNG.getHighestKeys();
            // there will always be a parent...always
            InternalNodeGroup parent = treeTrace.get(treeTrace.size() - 1).getLeft();
            int parentOffset = treeTrace.get(treeTrace.size() - 1).getRight();
            applyHighestKeysToParent(parent, parentOffset, highestKeysLeaf);
            if (treeTrace.size() > 1) {
                InternalNodeGroup grandParent = treeTrace.get(treeTrace.size() - 2).getLeft();
                int grandParentOffset = treeTrace.get(treeTrace.size() - 2).getRight();
                Integer highestKey = highestKeysLeaf.get(highestKeysLeaf.size());
                applyHighestKeysToGrandParent(grandParent, grandParentOffset, highestKey);
            }
        } else {
            // split happens..!
            // and that will likely turn into a recurse affair
            // where recursive means all the way up to the root
            // since internal node groups must be split as well
            logger.info(treeTrace);
            logger.info(leafNG.getHighestKeys());

            // keep in mind that all keys still need to reachable while the split is done
            // the things I have to do:
            // in order to have clearer naming
            LeafNodeGroup oldLeafNG = leafNG;
            // 1. split oldLeafNG
            // the old and new lng are now half full
            // and packed to the left
            // aka all empty space is at the end of the group
            LeafNodeGroup newLeafNG = oldLeafNG.split();
            // 2. get highest values to copy to parent node
            // this is what needs to be carried over to the parent and grandparent
            List<Integer> highestKeysOldLeaf = oldLeafNG.getHighestKeys();
            List<Integer> highestKeysNewLeaf = newLeafNG.getHighestKeys();

            // 3. figure out whether parent has space
            int lastIndexInTreeTrace = treeTrace.size() - 1;
            Pair<InternalNodeGroup, Integer> parentNodeGroupAndOffset = treeTrace.get(lastIndexInTreeTrace);
            InternalNodeGroup parentNodeGroup = parentNodeGroupAndOffset.getLeft();
            Integer nodeIndex = parentNodeGroupAndOffset.getRight();
            if (parentNodeGroup.hasSpace()) {
                // we make space for the new key (shift) in the two corresponding parent nodes
                // for my new child nodes
                int nodeOffset = searchInsertPosition(key, parentNodeGroup, nodeIndex);
                parentNodeGroup.put(nodeIndex, nodeOffset, key);
            } else {
                // we need to split the parent as well
                // TODO: build me in an iterative manner -- this might need some whiteboarding
                // key is to only always with three different levels at once
            }
            
            throw new NotImplementedException("splits");
        }
    }

    private void applyHighestKeysToParent(InternalNodeGroup ng, int nodeIndex, List<Integer> highestKeys) {

    }

    private void applyHighestKeysToGrandParent(InternalNodeGroup ng, int nodeIndex, Integer highestKey) {

    }

    private Integer searchInsertPosition(Integer key, InternalNodeGroup n, int offset) {
        for (int i = offset; i < numberOfNodesInInternalNodeGroup; i++) {
            for (int j = 0; j < internalNodeSize; j++) {
                if (n.getKey(i, j) == null) {
                    return j;
                }
                int cmp = key.compareTo(n.getKey(i, j));
                if (cmp <= 0) {
                    return j;
                }
            }
        }
        // TODO : what to do here???
        throw new IllegalStateException("Can't do anything with the key you gave me");
    }

    public String search(Integer key) {
        // iterative search instead of recursive
        NodeGroup ng = root;
        int offset = 0;
        while (ng.getLevel() > 0) {
            // while we didn't arrive at the bottom, continue stepping down
            Pair<NodeGroup, Integer> newNGAndOffset = searchThroughInternalNodes(key, (InternalNodeGroup) ng, offset);
            ng = newNGAndOffset.getLeft();
            offset = newNGAndOffset.getRight();
        }

        // we arrive at the bottom and have a safe cast
        // yes, this better be safe!
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
                if (cmp <= 0) {
                    return new ImmutablePair<>(n.getChild(i), j);
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
                if (cmp <= 0) {
                    return j;
                }
            }
        }
        // TODO : what to do here???
        throw new IllegalStateException("Can't do anything with the key you gave me");
    }
}
