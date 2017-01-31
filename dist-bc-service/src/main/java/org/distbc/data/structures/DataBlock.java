package org.distbc.data.structures;

import co.paralleluniverse.galaxy.Store;

import java.util.Iterator;

public class DataBlock<Key extends Comparable<Key>, Value> extends DataStructure {
    /**
     * Internal node implementing a linked list
     * insertion is O(1)
     * search / deletion is O(n)
     */
    private class Node {
        Key key;
        Value value;
        Node next;

        Node(Key key, Value value, Node next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    private Node first;

    DataBlock(Store store) {
        super(store);
    }

    DataBlock(Store store, long id) {
        super(store, id);
        // load data for reads aggressively
        asyncLoadForReads(this);
    }

    DataBlock(Store store, long id, Txn txn) {
        super(store, id);
        // load data for writes aggressively too
        asyncLoadForWrites(this, txn);
    }

    public Value get(Key key) {
        if (key == null) throw new IllegalArgumentException("key can't be null");
        checkDataStructureRetrieved();
        Node x = first;
        while (x != null) {
            if (x.key.equals(key)) {
                return x.value;
            }
            x = x.next;
        }
        return null;
    }

    public void put(Key key, Value val, Txn txn) {
        if (txn == null) throw new IllegalArgumentException("Txn cannot be null");
        innerPut(key, val);
    }

    public boolean putIfPossible(Key key, Value val, Txn txn) {
        if (txn == null) throw new IllegalArgumentException("Txn cannot be null");
        return innerPutIfPossible(key, val);
    }

    public void delete(Key key, Txn txn) {
        if (txn == null) throw new IllegalArgumentException("Txn cannot be null");
        innerDelete(key);
    }

    public Iterable<Key> keys() {
        checkDataStructureRetrieved();
        return () -> new Iterator<Key>() {
            Node x = first;

            @Override
            public boolean hasNext() {
                return x != null;
            }

            @Override
            public Key next() {
                Node n = x;
                x = x.next;
                return n.key;
            }
        };
    }

    /////////////////////////////////////////////////////////////
    //////////////////////////////////////////////
    // internal unit testable data structure implementation

    void innerPut(Key key, Value val) {
        if (key == null) throw new IllegalArgumentException("key can't be null");
        addObjectToObjectSize(key);
        addObjectToObjectSize(val);
        first = new Node(key, val, first);
    }

    boolean innerPutIfPossible(Key key, Value val) {
        int size = sizeOfObject(key) + sizeOfObject(val);
        if (isUnderMaxByteSize(size)) {
            innerPut(key, val);
            return true;
        } else {
            return false;
        }
    }

    void innerDelete(Key key) {
        if (key == null) throw new IllegalArgumentException("key can't be null");

        if (key.equals(first.key)) {
            first = (first.next != null) ? first.next : null;
            return;
        }

        Node x = first;
        while (x != null) {
            if (x.next != null && x.next.key.equals(key)) {
                subtractObjectToObjectSize(key);
                subtractObjectToObjectSize(x.next.value);
                x.next = x.next.next;
                return;
            }
            x = x.next;
        }
    }

    /////////////////////////////////////////////////////////////
    //////////////////////////////////////////////
    // galaxy-specific serialization overrides

    @Override
    void serialize(SerializerOutputStream out) {
        Node x = first;
        while (x != null) {
            out.writeObject(x.key);
            out.writeObject(x.value);
            x = x.next;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    void deserialize(SerializerInputStream in) {
        boolean shouldDoIt;
        do {
            Key key = (Key) in.readObject();
            Value value = (Value) in.readObject();
            shouldDoIt = key != null && value != null;
            if (shouldDoIt) first = new Node(key, value, first);
            addObjectToObjectSize(key);
            addObjectToObjectSize(value);
        } while (shouldDoIt);
    }
}
