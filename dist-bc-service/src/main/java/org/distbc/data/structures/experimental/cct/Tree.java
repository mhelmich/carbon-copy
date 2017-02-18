package org.distbc.data.structures.experimental.cct;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

public class Tree<K extends Comparable<K>, V extends Comparable<V>> {

    private final int numberOfNodesInInternalNodeGroup;
    private final int numberOfNodesInLeafNodeGroup;
    private final int internalNodeSize;
    private final int leafNodeSize;

    private InternalNodeGroup<K> root;

    Tree(int leafNodeSize, int numberOfNodesInLeafNodeGroup) {
        this.numberOfNodesInLeafNodeGroup = numberOfNodesInLeafNodeGroup;
        this.numberOfNodesInInternalNodeGroup = leafNodeSize - 1;
        this.internalNodeSize = numberOfNodesInLeafNodeGroup - 1;
        this.leafNodeSize = leafNodeSize;

        // we need at least a tree with depth 3
        // in order to get basic routing right
        root = newInternalNodeGroup(2);
        InternalNodeGroup<K> intermediate = newInternalNodeGroup(1);
        root.setChildNodeOnNode(0, intermediate);
        LeafNodeGroup<K, V> lng = newLeafNodeGroup();
        intermediate.setChildNodeOnNode(0, lng);
        System.err.println(toString());
    }

    Tree(int leafNodeSize, int numberOfNodesInLeafNodeGroup, InternalNodeGroup<K> root) {
        this.numberOfNodesInLeafNodeGroup = numberOfNodesInLeafNodeGroup;
        this.numberOfNodesInInternalNodeGroup = leafNodeSize - 1;
        this.internalNodeSize = numberOfNodesInLeafNodeGroup - 1;
        this.leafNodeSize = leafNodeSize;

        this.root = root;
    }

    public synchronized void put(K key, V value) {
        List<Breadcrumb> breadcrumbs = searchTree(key, root);
        Breadcrumb bc = breadcrumbs.get(breadcrumbs.size() - 1);
        LeafNodeGroup<K, V> lng = (LeafNodeGroup<K, V>) bc.ing.getChild(bc.idxIntoIng);
        int startIdx = (bc.idxIntoIng % internalNodeSize) * leafNodeSize;
        int insertionIdx = findInsertionIndex(key, lng, startIdx);
    }

    private List<Breadcrumb> searchTree(K key, InternalNodeGroup<K> ing) {
        NodeGroup<K> ng = ing;
        Breadcrumb parentBC = null;
        List<Breadcrumb> breadcrumbs = new ArrayList<>(ing.getLevel());

        do {
            InternalNodeGroup<K> tmp = (InternalNodeGroup<K>) ng;
            Breadcrumb bc = findIndexOfNextInternalNodeGroup(key, tmp, parentBC);
            breadcrumbs.add(bc);
            // I can do that because I know better
            // level > 1 :)
            ng = tmp.getChild(bc.idxIntoIng);
            parentBC = bc;
        } while (ng != null && ng.getLevel() > 0);

        return breadcrumbs;
    }

    private Breadcrumb findIndexOfNextInternalNodeGroup(K key, InternalNodeGroup<K> ing, Breadcrumb parentBC) {
        int nodeIdx = (parentBC != null) ? parentBC.idxIntoIng : 0;
        int startIdx = (parentBC != null) ? parentBC.idxIntoIng * internalNodeSize : 0;
        int idx = startIdx;

        while (ing.getKey(idx) != null && compareTo(key, ing.getKey(idx)) > 0) {
            idx++;
        }

        idx = Math.max(startIdx, idx);
        idx = Math.min(ing.getTotalNodeGroupSize(), idx);
        return newBreadcrumb(ing, idx);
    }

    private int findInsertionIndex(K key, NodeGroup<K> ng, int startIdx) {
        int i = startIdx;
        while (compareTo(key, ng.getKey(i)) > 0) {
            i++;
            if (i >= ng.getTotalNodeGroupSize()) {
                return -1;
            }
        }

        return i;
    }

    public Set<V> get(K key) {
        List<NodeAndIndex> nodeTrace = new LinkedList<>();
        LeafNodeGroup<K, V> lng = searchLeafNodeGroupForGets(key, root, /* inout */ nodeTrace);
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

    LeafNodeGroup<K, V> searchLeafNodeGroupForGets(K key, InternalNodeGroup<K> ing, /* inout */ List<NodeAndIndex> nodeTrace) {
        int nodeIdx;
        InternalNodeGroup<K> ng = ing;

        while (ng.getLevel() > 1) {
            nodeIdx = findIndexOfNextNodeGroupForGets(key, ng);
            nodeTrace.add(newNodeAndIndex(ng, nodeIdx));
            // I can do that because I know better
            // level > 1 :)
            ng = (InternalNodeGroup<K>) ng.getChildForNode(nodeIdx);
        }

        // ng has now a level 1 (aka the next child pointer will be the leaf)
        nodeIdx = findIndexOfNextNodeGroupForGets(key, ng);
        nodeTrace.add(newNodeAndIndex(ng, nodeIdx));
        if (ng.getChildForNode(nodeIdx) == null) {
            LeafNodeGroup<K, V> newLng = newLeafNodeGroup();
            ng.setChildNodeOnNode(nodeIdx, newLng);
        }
        return (LeafNodeGroup<K, V>) ng.getChildForNode(nodeIdx);
    }

    private int findIndexOfNextNodeGroupForGets(K key, InternalNodeGroup<K> ing) {
        // the findInsertionIndex method is built for LeafNodeGroups
        // LeafNodeGroups are one field longer
        // keep that in mind
//        int idx = checkHighLow(key, ing);
//        idx = (idx < 0) ? findIndexOfFirstKeySkippingNullForGets(key, ing) : idx;
        int idx = findIndexOfFirstKeySkippingNullForGets(key, ing);
        // convert from absolute index to node index
        return (idx < 0) ? -1 : idx / internalNodeSize;
    }

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

    private int findIndexOfFirstKeySkippingNullForGets(K key, NodeGroup<K> ng) {
        int i = 0;
        while (ng.getKey(i) == null || compareTo(key, ng.getKey(i)) > 0) {
            if (ng.getKey(i) == null) {
                // if the key at i is null, let's fast-forward to the next
                // non-null key
                int nextFullIdx = ng.findClosestFullSlotFrom(i);
                if (nextFullIdx < 0) {
                    // in case there is none, we found a winner
                    return (i > 0) ? i - 1 : 0;
                } else {
                    // if there is, let's proceed
                    i = nextFullIdx;
                }
            } else {
                i++;
            }
            if (i >= ng.getTotalNodeGroupSize()) {
                return ng.getTotalNodeGroupSize() - 1;
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

    private LeafNodeGroup<K, V> newLeafNodeGroup() {
        return new LeafNodeGroup<>(this.leafNodeSize, this.numberOfNodesInLeafNodeGroup);
    }

    private InternalNodeGroup<K> newInternalNodeGroup(int level) {
        assert level > 0;
        return new InternalNodeGroup<>(level, this.internalNodeSize, this.numberOfNodesInInternalNodeGroup);
    }

    private NodeAndIndex newNodeAndIndex(InternalNodeGroup<K> ing, int nodeIdxIntoIng) {
        return new NodeAndIndex(ing, nodeIdxIntoIng);
    }

    /**
     * This class is a container for the node trace.
     * It keeps track of node groups and the index into the node group,
     * which was used to descend to the next lower level.
     */
    class NodeAndIndex {
        final InternalNodeGroup<K> ing;
        final int nodeIdxIntoIng;
        private NodeAndIndex(InternalNodeGroup<K> ing, int nodeIdxIntoIng) {
            this.ing = ing;
            this.nodeIdxIntoIng = nodeIdxIntoIng;
        }
    }

    private Breadcrumb newBreadcrumb(InternalNodeGroup<K> ing, int idxIntoIng) {
        return new Breadcrumb(ing, idxIntoIng);
    }

    class Breadcrumb {
        final InternalNodeGroup<K> ing;
        private final int idxIntoIng;
        private Breadcrumb(InternalNodeGroup<K> ing, int idxIntoIng) {
            this.ing = ing;
            this.idxIntoIng = idxIntoIng;
        }

        @Override
        public String toString() {
            return idxIntoIng + "_" + ing.toString();
        }
    }

    @Override
    public String toString() {
        Vector<ArrayList<NodeGroup<K>>> v = new Vector<>(root.getLevel() + 1);
        v.setSize(root.getLevel() + 1);
        ArrayList<ArrayList<NodeGroup<K>>> nodeGroups = new ArrayList<>(v);
        traverseIng(root, nodeGroups);

        StringBuilder sb = new StringBuilder();

        for (int i = nodeGroups.size(); i > 0; i--) {
            for (int j = 0; j < nodeGroups.get(i - 1).size(); j++) {
                NodeGroup<K> ng = nodeGroups.get(i - 1).get(j);
                sb.append(
                        (ng != null) ? ng.toString() : "NULL"
                )
                        .append(" || ");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    private void traverseIng(InternalNodeGroup<K> ng, ArrayList<ArrayList<NodeGroup<K>>> nodeGroups) {
        if (nodeGroups.get(ng.getLevel()) == null) {
            nodeGroups.set(ng.getLevel(), new ArrayList<>());
        }
        nodeGroups.get(ng.getLevel()).add(ng);
        for (int i = 0; i < numberOfNodesInInternalNodeGroup; i++) {
            if (ng.getLevel() > 1) {
                InternalNodeGroup<K> ing = (InternalNodeGroup<K>)ng.getChildForNode(i);
                if (ing == null) {
                    if (nodeGroups.get(ng.getLevel() - 1) == null) {
                        nodeGroups.set(ng.getLevel() - 1, new ArrayList<>());
                    }
                    nodeGroups.get(ng.getLevel() - 1).add(null);
                } else {
                    traverseIng((InternalNodeGroup<K>)ng.getChildForNode(i), nodeGroups);
                }
            } else {
                traverseLng((LeafNodeGroup<K, V>)ng.getChildForNode(i), nodeGroups);
            }
        }
    }

    private void traverseLng(LeafNodeGroup<K, V> ng, ArrayList<ArrayList<NodeGroup<K>>> nodeGroups) {
        if (nodeGroups.get(0) == null) {
            nodeGroups.set(0, new ArrayList<>());
        }
        nodeGroups.get(0).add(ng);
    }
}