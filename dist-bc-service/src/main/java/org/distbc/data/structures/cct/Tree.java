package org.distbc.data.structures.cct;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.ObjectUtils;

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
        lng.maybeShiftOneRight(insertionIdx);
        lng.put(insertionIdx, key, value);
    }

    public Set<V> get(K key) {
        List<NodeAndIndex> nodeTrace = new LinkedList<>();
        LeafNodeGroup<K, V> lng = searchLeafNodeGroup(key, root, /* inout */ nodeTrace);
        int idx = findIndexOfFirstKey(key, lng);
        Set<V> resultSet = new HashSet<>();
        while (idx < lng.getTotalNodeSize()
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
        int idx;
        InternalNodeGroup<K> ng = ing;

        while (ng.getLevel() > 1) {
            idx = findIndexOfNextNodeGroup(key, ng);
            nodeTrace.add(newNodeAndIndex(ng, idx));
            // I can do that because I know better
            // level > 1 :)
            ng = (InternalNodeGroup<K>) ng.getChild(idx);
        }

        // ng has now a level 1 (aka the next child pointer will be the leaf)
        idx = findIndexOfNextNodeGroup(key, ng);
        nodeTrace.add(newNodeAndIndex(ng, idx));
        return (LeafNodeGroup<K, V>) ng.getChild(idx);
    }

    private int findIndexOfNextNodeGroup(K key, InternalNodeGroup<K> ing) {
        // if ing#getKey returns null, we want to return
        // hence null greater needs to be true
        if (ObjectUtils.compare(key, ing.getKey(0), true) < 0) {
            return 0;
        }

        if (ObjectUtils.compare(key, ing.getKey(this.internalNodeSize - 1)) > 0) {
            return this.internalNodeSize;
        }

        int i = 0;
        while (ObjectUtils.compare(key, ing.getKey(i)) > 0) {
            i++;
        }

        return i;
    }

    private int findInsertionIndex(K key, LeafNodeGroup<K, V> lng) {
        // if ing#getKey returns null, we want to return
        // hence null greater needs to be true
        K firstKeyInLeafNodeGroup = lng.getKey(0);
        if (firstKeyInLeafNodeGroup == null || compareTo(key, firstKeyInLeafNodeGroup) < 0) {
            return 0;
        }

        // the order in which the arguments are passed in seems to flip the semantic
        // of the boolean parameter "nullGreater"
        int lastIdxInLeafGroup = (numberOfNodesInLeafNodeGroup * leafNodeSize) - 1;
        K lastKeyInLeafNodeGroup = lng.getKey(lastIdxInLeafGroup);
        if (lastKeyInLeafNodeGroup != null
                && compareTo(key, lastKeyInLeafNodeGroup) < 0) {
            return lastIdxInLeafGroup;
        }

        return findIndexOfFirstKey(key, lng);
    }

    private int findIndexOfFirstKey(K key, LeafNodeGroup<K, V> lng) {
        int i = 0;
        while (compareTo(key, lng.getKey(i)) > 0) {
            i++;
            if (i >= leafNodeSize * numberOfNodesInLeafNodeGroup) {
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