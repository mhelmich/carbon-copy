package org.distbc.data.structures.cct;

import com.google.common.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Tree<K extends Comparable<K>, V extends Comparable<V>> {

    private final int numberOfNodesInInternalNodeGroup;
    private final int numberOfNodesInLeafNodeGroup;
    private final int internalNodeSize;
    private final int leafNodeSize;

    private InternalNodeGroup<K> root;

    @VisibleForTesting
    Tree(int leafNodeSize, int numberOfNodesInLeafNodeGroup) {
        this.numberOfNodesInLeafNodeGroup = numberOfNodesInLeafNodeGroup;
        this.numberOfNodesInInternalNodeGroup = leafNodeSize - 1;
        this.internalNodeSize = numberOfNodesInLeafNodeGroup - 1;
        this.leafNodeSize = leafNodeSize;

        root = new InternalNodeGroup<>(1, this.internalNodeSize, this.numberOfNodesInInternalNodeGroup);
        LeafNodeGroup<K, V> lng = new LeafNodeGroup<>(this.leafNodeSize, this.numberOfNodesInLeafNodeGroup);
        root.setChildNodeOnNode(0, lng);
    }

    @VisibleForTesting
    Tree(int leafNodeSize, int numberOfNodesInLeafNodeGroup, InternalNodeGroup<K> root) {
        this.numberOfNodesInLeafNodeGroup = numberOfNodesInLeafNodeGroup;
        this.numberOfNodesInInternalNodeGroup = leafNodeSize - 1;
        this.internalNodeSize = numberOfNodesInLeafNodeGroup - 1;
        this.leafNodeSize = leafNodeSize;

        this.root = root;
    }

    public synchronized void put(K key, V value) {
        List<NodeAndIndex> nodeTrace = new ArrayList<>();
        LeafNodeGroup<K, V> lng = searchLeafNodeGroup(key, root, /* inout */ nodeTrace);
        int insertionIdx = findInsertionIndex(key, lng);
        int emptyIdx = lng.findClosestEmptySlotFrom(insertionIdx);
        if (emptyIdx < 0) {
            // there's no space in lng anymore, let's make a new one
            splitNodes(lng, nodeTrace);
            // the magic of recursive calls
            // we still need at the current key
            // I don't see a reason why this should run dead
            // at the very least as long as put is single-threaded
            put(key, value);
        } else {
            // there's still space in lng
            lng.maybeShiftOneRight(insertionIdx);
            lng.put(insertionIdx, key, value);
            // do the highest keys business
            NodeAndIndex parent = nodeTrace.get(nodeTrace.size() - 1);
            setHighestKeysParent(parent, lng);
            if (nodeTrace.size() > 1) {
                NodeAndIndex grandParent = nodeTrace.get(nodeTrace.size() - 2);
                setHighestKeysGrandParent(grandParent, parent.ing);
            }
        }
    }

    private void setHighestKeysParent(NodeAndIndex nai, NodeGroup<K> ng) {
        nai.ing.setChildNodeOnNode(nai.nodeIdxIntoIng, ng);
    }

    private void setHighestKeysGrandParent(NodeAndIndex nai, NodeGroup<K> ng) {
        nai.ing.setGrandChildNodeOnNode(nai.nodeIdxIntoIng, ng);
    }

    private void splitNodes(LeafNodeGroup<K, V> lng, List<NodeAndIndex> nodeTrace) {
        LeafNodeGroup<K, V> newLng = lng.split();
        // see whether the parent has an empty slot
        NodeAndIndex nai = nodeTrace.remove(nodeTrace.size() - 1);
        int nodeToVacate = nai.nodeIdxIntoIng + 1;
        int emptyNodeIdx = nai.ing.findNodeIndexOfEmptyNodeFrom(nodeToVacate);
        // maybe we're lucky and find an empty slot in the node
        if (emptyNodeIdx < 0) {
            splitNodes(nai.ing, nodeTrace);
        } else {
            nai.ing.shiftNodesOneRight(nodeToVacate, emptyNodeIdx);
            nai.ing.setChildNodeOnNode(nodeToVacate, newLng);
            nai.ing.setChildNodeOnNode(nai.nodeIdxIntoIng, lng);
        }
    }

    // recursive for now
    private void splitNodes(InternalNodeGroup<K> ing, List<NodeAndIndex> nodeTrace) {
        if (!nodeTrace.isEmpty()) {
            InternalNodeGroup<K> newIng = ing.split();
            NodeAndIndex nai = nodeTrace.remove(nodeTrace.size() - 1);
            int nodeToVacate = nai.nodeIdxIntoIng + 1;
            int emptyNodeIdx = nai.ing.findNodeIndexOfEmptyNodeFrom(nodeToVacate);
            if (emptyNodeIdx < 0) {
                splitNodes(nai.ing, nodeTrace);
            } else {
                nai.ing.shiftNodesOneRight(nodeToVacate, emptyNodeIdx);
                nai.ing.setChildNodeOnNode(nodeToVacate, newIng);
                nai.ing.setChildNodeOnNode(nai.nodeIdxIntoIng, ing);
            }
        } else {
            // TODO
            // handle the case where we also need to split the root node
            InternalNodeGroup<K> newRoot = new InternalNodeGroup<>(ing.getLevel() + 1, internalNodeSize, numberOfNodesInInternalNodeGroup);
            InternalNodeGroup<K> newIng = ing.split();
            newRoot.setChildNodeOnNode(0, ing);
            newRoot.setChildNodeOnNode(1, newIng);
            this.root = newRoot;
        }
    }

    public Set<V> get(K key) {
        List<NodeAndIndex> nodeTrace = new LinkedList<>();
        LeafNodeGroup<K, V> lng = searchLeafNodeGroup(key, root, /* inout */ nodeTrace);
        int idx = findIndexOfFirstKey(key, lng);
        Set<V> resultSet = new HashSet<>();
        while (idx < lng.getTotalNodeGroupSize()
                && lng.getKey(idx) != null
                && lng.getKey(idx).equals(key)) {
            resultSet.add(lng.getValue(idx));
            idx++;
        }
        return resultSet;
    }

    public void delete(K key) {
        put(key, null);
    }

    @VisibleForTesting
    LeafNodeGroup<K, V> searchLeafNodeGroup(K key, InternalNodeGroup<K> ing, /* inout */ List<NodeAndIndex> nodeTrace) {
        int nodeIdx;
        InternalNodeGroup<K> ng = ing;

        while (ng.getLevel() > 1) {
            nodeIdx = findIndexOfNextNodeGroup(key, ng);
            nodeTrace.add(newNodeAndIndex(ng, nodeIdx));
            // I can do that because I know better
            // level > 1 :)
            ng = (InternalNodeGroup<K>) ng.getChild(nodeIdx);
        }

        // ng has now a level 1 (aka the next child pointer will be the leaf)
        nodeIdx = findIndexOfNextNodeGroup(key, ng);
        nodeTrace.add(newNodeAndIndex(ng, nodeIdx));
        assert ng.getChildForNode(nodeIdx) != null;
        return (LeafNodeGroup<K, V>) ng.getChildForNode(nodeIdx);
    }

    /**
     * This is a very delicate method.
     * It needs to find the right node index in various different situations.
     * The different use cases are:
     * 1. while traversing the nodes for search
     * 2. while traversing the nodes for put
     * 3.
     */
    private int findIndexOfNextNodeGroup(K key, InternalNodeGroup<K> ing) {
        // the findInsertionIndex method is built for LeafNodeGroups
        // LeafNodeGroups are one field longer
        // keep that in mind
        int idx = checkHighLow(key, ing);
        idx = (idx < 0) ? findIndexOfFirstKeySkippingNull(key, ing) : idx;
        idx = (idx < 0) ? (ing.findFirstNonNullChild() - 1) * internalNodeSize : idx;
        idx = (idx < 0) ? findIndexOfFirstKey(key, ing) : idx;
        // convert from absolute index to node index
        return (idx < 0) ? -1 : idx / internalNodeSize;
    }

    private int findInsertionIndex(K key, NodeGroup<K> ng) {
        int insideBounds = checkHighLow(key, ng);
        return (insideBounds < 0) ? findIndexOfFirstKey(key, ng) : insideBounds;
    }

    /**
     * This method checks to see whether key is smaller than the smallest
     * or larger than the largest value in ng.
     * This structure is a little bit odd. We return -1 to signal the caller
     * to proceed searching. It's the C way of doing things since lambdas and
     * passing function pointers down, rip the code apart and are messy too.
     */
    private int checkHighLow(K key, NodeGroup<K> ng) {
        // if ing#getKey returns null, we want to return
        // hence null greater needs to be true
        K firstKeyInLeafNodeGroup = ng.getKey(0);
        if (firstKeyInLeafNodeGroup == null
                || compareTo(key, firstKeyInLeafNodeGroup) < 0) {
            return 0;
        }

        int lastIdxInLeafGroup = ng.getTotalNodeGroupSize() - 1;
        K lastKeyInLeafNodeGroup = ng.getKey(lastIdxInLeafGroup);
        if (lastKeyInLeafNodeGroup != null
                && compareTo(key, lastKeyInLeafNodeGroup) > 0) {
            return lastIdxInLeafGroup;
        }

        return -1;
    }

    /**
     * This method finds the first key in the ng.
     * It returns immediately when it finds null though.
     */
    private int findIndexOfFirstKey(K key, NodeGroup<K> ng) {
        int i = 0;
        while (compareTo(key, ng.getKey(i)) > 0) {
            i++;
            if (i >= ng.getTotalNodeGroupSize()) {
                return -1;
            }
        }

        return i;
    }

    /**
     * This method finds the first key in the ng.
     * It skips null and proceeds searching.
     * The story is this:
     * There are different requirements for put and get.
     * For get we want to skip null because NodeGroups can be sparse.
     * And even though there are all these fancy tools and mechanisms
     * (like lambdas and interfaces and whatnot), it's also pretty simple
     * to unroll your code a little bit and do things the good'ol C way.
     */
    private int findIndexOfFirstKeySkippingNull(K key, NodeGroup<K> ng) {
        int i = 0;
        while (ng.getKey(i) == null || compareTo(key, ng.getKey(i)) > 0) {
            i++;
            if (i >= ng.getTotalNodeGroupSize()) {
                return -1;
            }
        }

        return i;
    }

    private int compareTo(K k1, K k2) {
        if (k2 == null) {
            return -1;
        } else if (k1 == null) {
            return 1;
        } else {
            return k1.compareTo(k2);
        }
    }

    private NodeAndIndex newNodeAndIndex(InternalNodeGroup<K> ing, int nodeIdxIntoIng) {
        return new NodeAndIndex(ing, nodeIdxIntoIng);
    }

    /**
     * This class is a container for the node trace.
     * It keeps track of node groups and the index into the node group,
     * which was used to descend to the next lower level.
     */
    @VisibleForTesting
    class NodeAndIndex {
        final InternalNodeGroup<K> ing;
        final int nodeIdxIntoIng;
        private NodeAndIndex(InternalNodeGroup<K> ing, int nodeIdxIntoIng) {
            this.ing = ing;
            this.nodeIdxIntoIng = nodeIdxIntoIng;
        }
    }
}