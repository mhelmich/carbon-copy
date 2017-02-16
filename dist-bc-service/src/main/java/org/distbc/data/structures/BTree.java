package org.distbc.data.structures;

import co.paralleluniverse.galaxy.Store;

import java.util.Iterator;

/**
 *  Modelled after the BTree by Robert Sedgewick and Kevin Wayne.
 *  Check out their useful website to learn more about basic data structures.
 *  http://algs4.cs.princeton.edu
 */
class BTree<Key extends Comparable<Key>, Value> extends DataStructure {
    // max children per B-tree node = MAX_NODE_SIZE - 1
    // (must be even and greater than 2)
    // this is gotta be >= 4
    static final int MAX_NODE_SIZE = 4;

    private final DataStructureFactory dsFactory;
    private BTreeNode<Key, Value> root;

    // height of the tree
    private int height;

    BTree(Store store, DataStructureFactory dsFactory, Txn txn) {
        super(store);
        this.dsFactory = dsFactory;
        asyncUpsert(this, txn);
        root = newNode(0, txn);
        addObjectToObjectSize(height);
    }

    BTree(Store store, DataStructureFactory dsFactory, long id) {
        super(store, id);
        this.dsFactory = dsFactory;
        asyncLoadForReads(this);
    }

    BTree(Store store, DataStructureFactory dsFactory, long id, Txn txn) {
        super(store, id);
        this.dsFactory = dsFactory;
        asyncLoadForWrites(this, txn);
    }

    public Value get(Key key) {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");
        checkDataStructureRetrieved();
        return search(root, key, height);
    }

    public void put(Key key, Value value, Txn txn) {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");
        checkDataStructureRetrieved();
        txn.addToChangedObjects(this);
        innerPut(key, value, txn);
    }

    public Iterable<Key> keys() {
        BTreeNode<Key, Value> first = depthFirstSearch(root, height);

        return () -> new Iterator<Key>() {
            BTreeNode<Key, Value> node = first;
            int idx = 0;

            @Override
            public boolean hasNext() {
                return idx < node.getNumChildren() || node.getNext() != null;
            }

            @Override
            public Key next() {
                if (idx >= node.getNumChildren()) {
                    node = node.getNext();
                    node.asyncLoadForReads();
                    idx = 0;
                }

                Key k = node.getEntryAt(idx).getKey();
                idx++;
                return k;
            }
        };
    }

    public void delete(Key key, Txn txn) {
        put(key, null, txn);
    }

    public String dump() {
        return dump(root, height, "") + "\n";
    }

    /////////////////////////////////////////////////////////////
    //////////////////////////////////////////////
    // internal unit testable data structure implementation

    private void innerPut(Key key, Value value, Txn txn) {
        BTreeNode<Key, Value> insertedNode = insert(root, key, value, height, txn);
        if (insertedNode == null) return;

        // insert doesn't return null means we gotta
        // split the top-most node
        BTreeNode<Key, Value> newNode = newNode(2, txn);
        newNode.setEntryAt(0, newEntry(root.getEntryAt(0).getKey(), root), txn);
        newNode.setEntryAt(1, newEntry(insertedNode.getEntryAt(0).getKey(), insertedNode), txn);
        root = newNode;
        txn.addToChangedObjects(this);
        height++;
    }

    private BTreeNode<Key, Value> newNode(int numChildren, Txn txn) {
        return dsFactory.newBTreeNode(numChildren, txn);
    }

    private BTreeEntry<Key, Value> newEntry(Key key, Value value) {
        return new BTreeEntry<>(key, value);
    }

    private BTreeEntry<Key, Value> newEntry(Key key, BTreeNode<Key, Value> next) {
        return new BTreeEntry<>(key, next);
    }

    private Value search(BTreeNode<Key, Value> x, Key key, int height) {
        x.checkDataStructureRetrieved();
        if (height > 0) {
            // internal node
            // find the right child node to descend to
            for (int j = 0; j < x.getNumChildren(); j++) {
                if (j + 1 == x.getNumChildren() || lessThan(key, x.getEntryAt(j + 1).getKey())) {
                    return search(x.getChildNodeAt(j), key, height - 1);
                }
            }
        } else {
            // leaf node
            // find the right key (if it's there) and return it
            for (int j = 0; j < x.getNumChildren(); j++) {
                if (equal(key, x.getEntryAt(j).getKey())) {
                    return x.getEntryAt(j).getValue();
                }
            }
        }
        return null;
    }

    private BTreeNode<Key, Value> insert(BTreeNode<Key, Value> x, Key key, Value value, int height, Txn txn) {
        int j;
        BTreeEntry<Key, Value> entryToInsert = newEntry(key, value);

        if (height > 0 ) {
            // internal node
            for (j = 0; j < x.getNumChildren(); j++) {
                if ((j + 1 == x.getNumChildren()) || lessThan(key, x.getEntryAt(j + 1).getKey())) {
                    BTreeNode<Key, Value> insertedNode = insert(x.getEntryAt(j++).getChildNode(), key, value, height - 1, txn);
                    // we're done, bubble up through recursion
                    if (insertedNode == null) return null;
                    entryToInsert.setKey(insertedNode.getEntryAt(0).getKey());
                    entryToInsert.setChildNode(insertedNode);
                    break;
                }
            }
        } else {
            // leaf node
            for (j = 0; j < x.getNumChildren(); j++) {
                if (lessThan(key, x.getEntryAt(j).getKey())) {
                    break;
                }
            }
        }

        // move all children over one slot
        for (int i = x.getNumChildren(); i > j; i--) {
            x.setEntryAt(i, x.getEntryAt(i - 1), txn);
        }
        // drop the new one into the right spot
        x.setEntryAt(j, entryToInsert, txn);
        x.setNumChildren(x.getNumChildren() + 1);
        // if we have space, end recursion
        // if not, split the node
        return (x.getNumChildren() < MAX_NODE_SIZE) ? null : split(x, height, txn);
    }

    private BTreeNode<Key, Value> split(BTreeNode<Key, Value> oldNode, int height, Txn txn) {
        BTreeNode<Key, Value> newNode = newNode(MAX_NODE_SIZE / 2, txn);
        oldNode.setNumChildren(MAX_NODE_SIZE / 2);
        for (int j = 0; j < MAX_NODE_SIZE / 2; j++) {
            newNode.setEntryAt(j, oldNode.getEntryAt((MAX_NODE_SIZE / 2) + j), txn);
        }
        if (height == 0) {
            newNode.setNext(oldNode.getNext());
            oldNode.setNext(newNode);
        }
        return newNode;
    }

    private BTreeNode<Key, Value> depthFirstSearch(BTreeNode<Key, Value> node, int height) {
        if (height > 0 && node.getNumChildren() > 0) {
            BTreeNode<Key, Value> child = node.getChildNodeAt(0);
            return depthFirstSearch(child, height - 1);
        } else {
            return node;
        }
    }

    private String dump(BTreeNode<Key, Value> x, int height, String indent) {
        StringBuilder sb = new StringBuilder();
        x.checkDataStructureRetrieved();

        if (height == 0) {
            for (int j = 0; j < x.getNumChildren(); j++) {
                sb.append(indent).append(x.getEntryAt(j).getKey()).append(" _ ").append(x.toString()).append(" ").append(x.getEntryAt(j).getValue()).append("\n");
            }
        } else {
            for (int j = 0; j < x.getNumChildren(); j++) {
                if (j > 0) {
                    sb.append(indent).append("(").append(x.getEntryAt(j).getKey()).append(" _ ").append(x.toString()).append(")\n");
                }

                BTreeNode<Key, Value> node = x.getEntryAt(j).getChildNode();
                node.asyncLoadForReads();
                sb.append(dump(node, height - 1, indent + "     "));
            }
        }
        return sb.toString();
    }

    private boolean lessThan(Key k1, Key k2) {
        return k1.compareTo(k2) < 0;
    }

    private boolean equal(Key k1, Key k2) {
        return k1.compareTo(k2) == 0;
    }

    /////////////////////////////////////////////////////////////
    //////////////////////////////////////////////
    // galaxy-specific serialization overrides

    @Override
    void serialize(SerializerOutputStream out) {
        if (root != null) {
            out.writeObject(root.getId());
            out.writeObject(height);
        }
    }

    @Override
    void deserialize(SerializerInputStream in) {
        Long rootId = (Long) in.readObject();
        if (rootId != null) {
            root = dsFactory.loadBTreeNode(rootId);
        }
        Integer height = (Integer) in.readObject();
        this.height = (height != null) ? height : 0;
    }
}
