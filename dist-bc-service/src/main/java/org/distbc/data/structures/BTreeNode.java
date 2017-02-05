package org.distbc.data.structures;

import co.paralleluniverse.galaxy.Store;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Helper and container for a bunch of items that belong into a tree
 */
class BTreeNode<Key extends Comparable<Key>, Value> extends DataStructure {
    private int numChildren;
    // list of entries
    private ArrayList<BTreeEntry<Key, Value>> entries;

    private final DataStructureFactory dsFactory;

    BTreeNode(Store store, DataStructureFactory dsFactory, long id) {
        super(store, id);
        Vector<BTreeEntry<Key, Value>> v = new Vector<>(BTree.MAX_NODE_SIZE);
        v.setSize(BTree.MAX_NODE_SIZE);
        entries = new ArrayList<>(v);
        this.dsFactory = dsFactory;
        asyncLoadForReads(this);
    }

    BTreeNode(Store store, DataStructureFactory dsFactory, int numChildren, Txn txn) {
        super(store);
        Vector<BTreeEntry<Key, Value>> v = new Vector<>(BTree.MAX_NODE_SIZE);
        v.setSize(BTree.MAX_NODE_SIZE);
        entries = new ArrayList<>(v);
        this.numChildren = numChildren;
        this.dsFactory = dsFactory;
        asyncUpsert(this, txn);

        // happens after upsert is kicked off
        addObjectToObjectSize(numChildren);
    }

    int getNumChildren() {
        checkDataStructureRetrieved();
        return numChildren;
    }

    void setNumChildren(int newNumChildren) {
        this.numChildren = newNumChildren;
    }

    void setEntryAt(int idx, BTreeEntry<Key, Value> entry, Txn txn) {
        checkDataStructureRetrieved();
        txn.addToChangedObjects(this);
        addObjectToObjectSize(entry.getKey());
        addObjectToObjectSize(entry.getValue());
        addObjectToObjectSize((entry.getChildNode() != null) ? entry.getChildNode().getId() : null);
        entries.set(idx, entry);
    }

    BTreeEntry<Key, Value> getEntryAt(int idx) {
        checkDataStructureRetrieved();
        return entries.get(idx);
    }

    /////////////////////////////////////////////////////////////
    //////////////////////////////////////////////
    // galaxy-specific serialization overrides

    @Override
    void serialize(SerializerOutputStream out) {
        out.writeObject(numChildren);
        for (int i = 0; i < numChildren; i++) {
            BTreeEntry<Key, Value> entry = entries.get(i);
            if (entry != null) {
                out.writeObject(entry.getKey());
                out.writeObject(entry.getValue());
                BTreeNode<Key, Value> n = entry.getChildNode();
                out.writeObject((n != null) ? n.getId() : null);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    void deserialize(SerializerInputStream in) {
        try {
            // the leading byte is the size of the hash table
            Integer tmp = (Integer) in.readObject();
            numChildren = (tmp != null) ? tmp : 0;

            for (int i = 0; i < numChildren && in.available() > 0; i++) {
                Key key = (Key) in.readObject();
                Value value = (Value) in.readObject();
                Long id = (Long) in.readObject();

                BTreeNode<Key, Value> node = (id != null) ? dsFactory.loadBTreeNode(id) : null;
                BTreeEntry<Key, Value> entry = new BTreeEntry<>(key, value, node);
                entries.set(i, entry);
            }
        } catch (IOException xcp) {
            throw new RuntimeException(xcp);
        }
    }
}
