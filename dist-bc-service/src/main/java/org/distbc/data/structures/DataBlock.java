/*
 *
 *  Copyright 2017 Marco Helmich
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.distbc.data.structures;

import co.paralleluniverse.galaxy.Store;

import java.util.Iterator;

class DataBlock<Key extends Comparable<Key>, Value> extends DataStructure {
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

    DataBlock(Store store, Txn txn) {
        super(store);
        asyncUpsert(txn);
    }

    DataBlock(Store store, long id, boolean shouldLoad) {
        super(store, id);
        if (shouldLoad) {
            // load data for reads aggressively
            asyncLoadForReads();
        }
    }

    DataBlock(Store store, long id, Txn txn) {
        super(store, id);
        // load data for writes aggressively too
        asyncLoadForWrites(txn);
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
        checkDataStructureRetrieved();
        txn.addToChangedObjects(this);
        innerPut(key, val);
    }

    public boolean putIfPossible(Key key, Value val, Txn txn) {
        if (txn == null) throw new IllegalArgumentException("Txn cannot be null");
        checkDataStructureRetrieved();
        boolean didPut = innerPutIfPossible(key, val);
        if (didPut) {
            txn.addToChangedObjects(this);
        }
        return didPut;
    }

    public void delete(Key key, Txn txn) {
        if (txn == null) throw new IllegalArgumentException("Txn cannot be null");
        checkDataStructureRetrieved();
        if (innerDelete(key)) {
            txn.addToChangedObjects(this);
        }
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

    boolean innerDelete(Key key) {
        if (key == null) throw new IllegalArgumentException("key can't be null");
        if (first == null) return false;

        if (key.equals(first.key)) {
            first = (first.next != null) ? first.next : null;
            return true;
        }

        Node x = first;
        while (x != null) {
            if (x.next != null && x.next.key.equals(key)) {
                subtractObjectToObjectSize(key);
                subtractObjectToObjectSize(x.next.value);
                x.next = x.next.next;
                return true;
            }
            x = x.next;
        }

        return false;
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
