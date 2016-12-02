package org.distbc.data.structures.cct;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.ObjectUtils;

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
        List<InternalNodeGroup<K>> nodeTrace = new LinkedList<>();
        LeafNodeGroup<K, V> lng = searchLeafNodeGroup(key, root, /* inout */ nodeTrace);
        int insertionIdx = findInsertionIndex(key, lng);
        lng.maybeShiftOneRight(insertionIdx);
        lng.put(insertionIdx, key, value);
    }

    public Set<V> get(K key) {
        List<InternalNodeGroup<K>> nodeTrace = new LinkedList<>();
        LeafNodeGroup<K, V> lng = searchLeafNodeGroup(key, root, /* inout */ nodeTrace);
        int idx = findIndexOfFirstKey(key, lng);
        Set<V> resultSet = new HashSet<>();
        while (lng.getKey(idx) != null && lng.getKey(idx).equals(key)) {
            resultSet.add(lng.getValue(idx));
            idx++;
        }
        return resultSet;
    }

    public void delete(K key) {
        put(key, null);
    }

    @VisibleForTesting
    LeafNodeGroup<K, V> searchLeafNodeGroup(K key, InternalNodeGroup<K> ing, /* inout */ List<InternalNodeGroup<K>> nodeTrace) {
        int idx;
        InternalNodeGroup<K> ng = ing;

        while (ng.getLevel() > 1) {
            idx = findIndexOfNextNodeGroup(key, ng);
            // I can do that because I know better
            // level > 1 :)
            nodeTrace.add(ng);
            ng = (InternalNodeGroup<K>) ng.getChild(idx);
        }

        nodeTrace.add(ng);
        // ng has now a level 1 (aka the next child pointer will be the leaf)
        idx = findIndexOfNextNodeGroup(key, ng);
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
        return findIndexOfFirstKey(key, lng);
    }

    private int findIndexOfFirstKey(K key, LeafNodeGroup<K, V> lng) {
        // if ing#getKey returns null, we want to return
        // hence null greater needs to be true
        if (ObjectUtils.compare(key, lng.getKey(0), true) < 0) {
            return 0;
        }

        // the order in which the arguments are passed in seems to flip the semantic
        // of the boolean parameter "nullGreater"
        if (ObjectUtils.compare(lng.getKey(this.leafNodeSize - 1), key) > 0) {
            return this.leafNodeSize - 1;
        }

        int i = 0;
        while (ObjectUtils.compare(key, lng.getKey(i)) > 0) {
            i++;
        }

        return i;
    }
}