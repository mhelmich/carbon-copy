package org.distbc.data.structures.cct;

import com.google.common.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

public class Tree2<K extends Comparable<K>, V extends Comparable<V>> {
    private final int numberOfNodesInInternalNodeGroup;
    private final int numberOfNodesInLeafNodeGroup;
    private final int internalNodeSize;
    private final int leafNodeSize;

    private InternalNodeGroup<K> root;

    @VisibleForTesting
    Tree2(int leafNodeSize, int numberOfNodesInLeafNodeGroup) {
        this.numberOfNodesInLeafNodeGroup = numberOfNodesInLeafNodeGroup;
        this.numberOfNodesInInternalNodeGroup = leafNodeSize;
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

    private LeafNodeGroup<K, V> newLeafNodeGroup() {
        return new LeafNodeGroup<>(this.leafNodeSize, this.numberOfNodesInLeafNodeGroup);
    }

    private InternalNodeGroup<K> newInternalNodeGroup(int level) {
        assert level > 0;
        return new InternalNodeGroup<>(level, this.internalNodeSize, this.numberOfNodesInInternalNodeGroup);
    }

    public synchronized void put(K key, V value) {
        List<Breadcrumb> breadcrumbs = searchTree(key, root);
        Breadcrumb bc = breadcrumbs.get(breadcrumbs.size() - 1);
        int insertionIdx = findInsertionIndex(key, bc);
        LeafNodeGroup<K, V> lng = (LeafNodeGroup<K, V>) bc.ing.getChild(bc.idxIntoIng);
        int emptyIdx = lng.findClosestEmptySlotFrom(insertionIdx);
        if (emptyIdx < 0) {
            splitNodes(lng, breadcrumbs);
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
            doHighKeyBusiness(breadcrumbs, insertionIdx, emptyIdx);
        }
        System.err.println(toString());
    }

    private void doHighKeyBusiness(List<Breadcrumb> breadcrumbs, int insertionIdx, int emptyIdx) {
        // high keys need to be set under two circumstances
        // 1. we shift a high key
        // 2. we change a high key
        Breadcrumb parent = breadcrumbs.get(breadcrumbs.size() - 1);
        int mod = (emptyIdx + 1) % leafNodeSize;
        boolean isLastItem = emptyIdx > 0 && (emptyIdx + 1) % leafNodeSize == 0 && !((emptyIdx + 1) % (leafNodeSize * numberOfNodesInLeafNodeGroup) == 0);

        if (isLastItem) {
            int idx = emptyIdx;
            LeafNodeGroup<K, V> lng = (LeafNodeGroup<K, V>) parent.ing.getChild(parent.idxIntoIng);
            K key = lng.getKey(idx);
            parent.ing.put(parent.idxIntoIng, key);
        } else if (mod > 0 && (emptyIdx - insertionIdx) >= mod) {
            int idx = (emptyIdx - insertionIdx) / leafNodeSize + (leafNodeSize - 1);
            LeafNodeGroup<K, V> lng = (LeafNodeGroup<K, V>) parent.ing.getChild(parent.idxIntoIng);
            K key = lng.getKey(idx);
            parent.ing.put(parent.idxIntoIng, key);
        }

        NodeGroup<K> ng = parent.ing.getChild(parent.idxIntoIng);
        boolean shouldDoIt = (emptyIdx + 1) % ng.getTotalNodeGroupSize() == 0;
        for (int i = breadcrumbs.size() - 2; shouldDoIt && i >= 0; i--) {
            Breadcrumb gp = breadcrumbs.get(i);
            Breadcrumb p = breadcrumbs.get(i + 1);
            gp.ing.put(gp.idxIntoIng, ng.getKey(ng.getTotalNodeGroupSize() - 1));
            shouldDoIt = (gp.idxIntoIng + 1) % internalNodeSize == 0;
        }
    }

    private void splitNodes(LeafNodeGroup<K, V> lng, List<Breadcrumb> nodeTrace) {
        LeafNodeGroup<K, V> newLng = lng.split();
        // see whether the parent has an empty slot
        Breadcrumb nai = nodeTrace.remove(nodeTrace.size() - 1);
        int nodeToVacate = nai.idxIntoIng + 1;
        int emptyNodeIdx = nai.ing.findNodeIndexOfEmptyNodeFrom(nodeToVacate);
        // maybe we're lucky and find an empty slot in the node
        if (emptyNodeIdx < 0) {
            splitNodes(nai.ing, nodeTrace);
        } else {
            nai.ing.shiftNodesOneRight(nodeToVacate, emptyNodeIdx);
            nai.ing.setChildNodeOnNode(nodeToVacate, newLng);
            nai.ing.setChildNodeOnNode(nai.idxIntoIng, lng);
        }
    }

    private void splitNodes(InternalNodeGroup<K> ing, List<Breadcrumb> nodeTrace) {
        if (!nodeTrace.isEmpty()) {
            InternalNodeGroup<K> newIng = ing.split();
            Breadcrumb nai = nodeTrace.remove(nodeTrace.size() - 1);
            int nodeToVacate = nai.idxIntoIng + 1;
            int emptyNodeIdx = nai.ing.findNodeIndexOfEmptyNodeFrom(nodeToVacate);
            if (emptyNodeIdx < 0) {
                splitNodes(nai.ing, nodeTrace);
            } else {
                nai.ing.shiftNodesOneRight(nodeToVacate, emptyNodeIdx);
                nai.ing.setChildNodeOnNode(nodeToVacate, newIng);
                nai.ing.setChildNodeOnNode(nai.idxIntoIng, ing);
            }
        } else {
            InternalNodeGroup<K> newRoot = newInternalNodeGroup(ing.getLevel() + 1);
            InternalNodeGroup<K> newIng = ing.split();
            newRoot.setChildNodeOnNode(0, ing);
            newRoot.setChildNodeOnNode(1, newIng);
            this.root = newRoot;
        }
    }

    private int findInsertionIndex(K key, Breadcrumb bc) {
        NodeGroup<K> ng = bc.ing.getChild(bc.idxIntoIng);
        int i = bc.idxIntoIng * leafNodeSize;
        while (compareTo(key, ng.getKey(i)) > 0) {
            i++;
            if (i >= ng.getTotalNodeGroupSize()) {
                return -1;
            }
        }

        return i;
    }

//    private int findInsertionIndex(K key, NodeGroup<K> ng) {
//        int i = 0;
//        while (compareTo(key, ng.getKey(i)) > 0) {
//            i++;
//            if (i >= ng.getTotalNodeGroupSize()) {
//                return -1;
//            }
//        }
//
//        return i;
//    }
//
//    private LeafNodeGroup<K, V> putSearchLeafNodeGroup(K key, InternalNodeGroup<K> ing, /* inout */ List<Breadcrumb> nodeTrace) {
//        InternalNodeGroup<K> ng = ing;
//        Breadcrumb parentBC = null;
//
//        do {
////            Breadcrumb bc = putFindIndexOfNextInternalNodeGroup(key, ng, parentBC);
//            Breadcrumb bc = findIndexOfNextInternalNodeGroup(key, ng, parentBC);
//            nodeTrace.add(bc);
//            // I can do that because I know better
//            // level > 1 :)
//            ng = (InternalNodeGroup<K>) ng.getChild(bc.idxIntoIng);
//            parentBC = bc;
//        } while (ng.getLevel() > 1);
//
//        // ng has now a level 1 (aka the next child pointer will be the leaf)
////        Breadcrumb bc = putFindIndexOfNextLeafNodeGroup(key, ng, parentBC);
//        Breadcrumb bc = findIndexOfNextInternalNodeGroup(key, ng, parentBC);
//        nodeTrace.add(bc);
//
//        LeafNodeGroup<K, V> lng = (LeafNodeGroup<K, V>) ng.getChild(bc.idxIntoIng);
//        if (lng == null) {
//            lng = newLeafNodeGroup();
//            ng.setChildNode(bc.idxIntoIng, lng);
//            // FIXME
//            // link up previous and next
//        }
//
//        return lng;
//    }

    private List<Breadcrumb> searchTree(K key, InternalNodeGroup<K> ing) {
        NodeGroup<K> ng = ing;
        Breadcrumb parentBC = null;
        List<Breadcrumb> breadcrumbs = new ArrayList<>();

        do {
            InternalNodeGroup<K> tmp = (InternalNodeGroup<K>) ng;
            Breadcrumb bc = findIndexOfNextInternalNodeGroup(key, tmp, parentBC);
            breadcrumbs.add(bc);
            // I can do that because I know better
            // level > 1 :)
            ng = tmp.getChild(bc.idxIntoIng);
            parentBC = bc;
        } while (ng.getLevel() > 0);

        return breadcrumbs;
    }

    private Breadcrumb findIndexOfNextInternalNodeGroup(K key, InternalNodeGroup<K> ing, Breadcrumb parentBC) {
        int startIdx = (parentBC != null) ? parentBC.idxIntoIng * internalNodeSize : 0;
        int idx = startIdx;
        boolean shouldBranchIntoThisNode = parentBC == null ? ing.getKey(0) == null : parentBC.ing.getKey(parentBC.idxIntoIng) == null;

        while (ing.getKey(idx) != null && compareTo(key, ing.getKey(idx)) > 0) {
            idx++;
        }

        idx = Math.min(ing.getTotalNodeGroupSize(), Math.max(startIdx, (shouldBranchIntoThisNode) ? idx : idx + 1));
        return newBreadcrumb(ing, idx, ing.getKey(idx) == null);
    }

//    private Breadcrumb putFindIndexOfNextInternalNodeGroup(K key, InternalNodeGroup<K> ing, Breadcrumb parentBC) {
//        int startIdx = (parentBC != null) ? parentBC.idxIntoIng * internalNodeSize : 0;
//        int idx = startIdx;
//        boolean shouldBranchIntoThisNode = parentBC == null ? ing.getKey(0) == null : parentBC.ing.getKey(parentBC.idxIntoIng) == null;
//
//        while (idx < ing.getTotalNodeGroupSize()
//                && (ing.getKey(idx) == null || compareTo(key, ing.getKey(idx)) > 0)) {
//            if (ing.getKey(idx) == null) {
//                int nextFullIdx = ing.findClosestFullSlotFrom(idx);
//                if (nextFullIdx < 0) {
//                    idx = (shouldBranchIntoThisNode) ? idx - 1: idx;
//                    idx = Math.max(startIdx, idx);
//                    break;
//                } else {
//                    idx = nextFullIdx;
//                }
//            } else {
//                idx++;
//            }
//        }
//
//        return newBreadcrumb(ing, idx, ing.getKey(idx) == null);
//    }

//    private Breadcrumb putFindIndexOfNextLeafNodeGroup(K key, InternalNodeGroup<K> ing, Breadcrumb parentBC) {
//        int startIdx = (parentBC != null) ? parentBC.idxIntoIng * internalNodeSize : 0;
//        int idx = startIdx;
//        boolean nextParentValueIsNull = true;
//        boolean shouldBranchIntoThisNode = parentBC == null || parentBC.ing.getKey(parentBC.idxIntoIng) == null;
//
//        while (idx < ing.getTotalNodeGroupSize() && (ing.getKey(idx) == null || compareTo(key, ing.getKey(idx)) > 0)) {
//            if (ing.getKey(idx) == null) {
//                int nextFullIdx = ing.findClosestFullSlotFrom(idx);
//                if (nextFullIdx < 0) {
//                    idx = (shouldBranchIntoThisNode) ? idx - 1 : idx;
//                    idx = Math.max(startIdx, idx);
//                    break;
//                } else {
//                    idx = nextFullIdx;
//                }
//            } else {
//                idx++;
//                nextParentValueIsNull = false;
//            }
//        }
//
//        idx = Math.min(ing.getTotalNodeGroupSize() - 1, idx);
//        // convert from absolute index to node index
//        return newBreadcrumb(ing, idx, nextParentValueIsNull);
//    }

    public Set<V> get(K key) {
        List<Breadcrumb> breadcrumbs = searchTree2(key, root);
        Breadcrumb bc = breadcrumbs.get(breadcrumbs.size() - 1);
        int idx = findInsertionIndex(key, bc);
        LeafNodeGroup<K, V> lng = (LeafNodeGroup<K, V>) bc.ing.getChild(bc.idxIntoIng);
        Set<V> resultSet = new HashSet<>();
        while (idx < lng.getTotalNodeGroupSize()
                && lng.getKey(idx) != null
                && lng.getKey(idx).equals(key)) {
            resultSet.add(lng.getValue(idx));
            idx++;
        }
        return resultSet;
    }

    private List<Breadcrumb> searchTree2(K key, InternalNodeGroup<K> ing) {
        NodeGroup<K> ng = ing;
        Breadcrumb parentBC = null;
        List<Breadcrumb> breadcrumbs = new ArrayList<>();

        do {
            InternalNodeGroup<K> tmp = (InternalNodeGroup<K>) ng;
            Breadcrumb bc = findIndexOfNextInternalNodeGroup2(key, tmp, parentBC);
            breadcrumbs.add(bc);
            // I can do that because I know better
            // level > 1 :)
            ng = tmp.getChild(bc.idxIntoIng);
            parentBC = bc;
        } while (ng.getLevel() > 0);

        return breadcrumbs;
    }

    private Breadcrumb findIndexOfNextInternalNodeGroup2(K key, InternalNodeGroup<K> ing, Breadcrumb parentBC) {
        int startIdx = (parentBC != null) ? parentBC.idxIntoIng * internalNodeSize : 0;
        int idx = startIdx;
        boolean shouldBranchIntoThisNode = parentBC == null ? ing.getKey(0) != null : parentBC.ing.getKey(parentBC.idxIntoIng) == null;

        while (ing.getKey(idx) != null && compareTo(key, ing.getKey(idx)) > 0) {
            idx++;
        }

        idx = Math.min(ing.getTotalNodeGroupSize(), Math.max(startIdx, (shouldBranchIntoThisNode) ? idx - 1 : idx));
        return newBreadcrumb(ing, idx, ing.getKey(idx) == null);
    }

//    private LeafNodeGroup<K, V> getSearchLeafNodeGroup(K key, InternalNodeGroup<K> ing, /* inout */ List<Breadcrumb> nodeTrace) {
//        InternalNodeGroup<K> ng = ing;
//
//        while (ng.getLevel() > 1) {
//            Breadcrumb parentBC = (nodeTrace.size() > 0) ? nodeTrace.get(nodeTrace.size() - 1) : null;
//            // we might get away with calling the put flavor here
//            Breadcrumb bc = putFindIndexOfNextInternalNodeGroup(key, ng, parentBC);
//            nodeTrace.add(bc);
//            // I can do that because I know better
//            // level > 1 :)
//            ng = (InternalNodeGroup<K>) ng.getChild(bc.idxIntoIng);
//        }
//
//        // ng has now a level 1 (aka the next child pointer will be the leaf)
//        Breadcrumb parentBC = (nodeTrace.size() > 0) ? nodeTrace.get(nodeTrace.size() - 1) : null;
//        Breadcrumb bc = getFindIndexOfNextLeafNodeGroup(key, ng, parentBC);
//        nodeTrace.add(bc);
//
//        // leaf nodes have one element more than internal nodes
//        // the conversion doesn't work here because ng is an InternalNodeGroup
//        // pointing to a LeafNodeGroup
//        return (LeafNodeGroup<K, V>) ng.getChildForNode(bc.idxIntoIng / leafNodeSize);
//    }

//    private Breadcrumb getFindIndexOfNextLeafNodeGroup(K key, InternalNodeGroup<K> ing, Breadcrumb parentBC) {
//        int idx = 0;
//        boolean keyedOfNonNull = false;
//        boolean shouldBranchIntoThisNode = !(parentBC != null && parentBC.parentValueIsNull);
//
//        while (idx < ing.getTotalNodeGroupSize() && (ing.getKey(idx) == null || compareTo(key, ing.getKey(idx)) > 0)) {
//            if (ing.getKey(idx) == null) {
//                int nextFullIdx = ing.findClosestFullSlotFrom(idx);
//                if (nextFullIdx < 0) {
//                    idx = (shouldBranchIntoThisNode) ? idx - 1 : idx;
//                    idx = Math.max(0, idx);
//                    break;
//                } else {
//                    idx = nextFullIdx;
//                }
//            } else {
//                idx++;
//                keyedOfNonNull = true;
//            }
//        }
//
//        idx = (shouldBranchIntoThisNode) ? idx + 1 : idx;
//        idx = Math.min(ing.getTotalNodeGroupSize() - 1, idx);
//        // convert from absolute index to node index
//        return newBreadcrumb(ing, idx, keyedOfNonNull);
//    }

    public void delete(K key) {
        put(key, null);
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

    private Breadcrumb newBreadcrumb(InternalNodeGroup<K> ing, int idxIntoIng, boolean isFull) {
        return new Breadcrumb(ing, idxIntoIng, isFull);
    }

    /**
     * This class is a container for the node trace.
     * It keeps track of node groups and the index into the node group,
     * which was used to descend to the next lower level.
     */
    @VisibleForTesting
    class Breadcrumb {
        final InternalNodeGroup<K> ing;
        final int idxIntoIng;
        final boolean parentValueIsNull;
        private Breadcrumb(InternalNodeGroup<K> ing, int idxIntoIng, boolean parentValueIsNull) {
            this.ing = ing;
            this.idxIntoIng = idxIntoIng;
            this.parentValueIsNull = parentValueIsNull;
        }

        @Override
        public String toString() {
            return idxIntoIng + "_" + parentValueIsNull + "_" + ing.toString();
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
