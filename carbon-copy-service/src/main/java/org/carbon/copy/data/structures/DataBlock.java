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

package org.carbon.copy.data.structures;

import co.paralleluniverse.galaxy.Store;

import java.io.IOException;
import java.util.Iterator;

/**
 * The foundation of all other data structures.
 * It's merely a linked list of keys and values that is being shoved into a blob of data.
 */
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

        // mostly done for debugging purposes
        // remove this if it's too much
        @Override
        public String toString() {
            return key.toString() + " - " + value.toString();
        }
    }

    private Node first;

    DataBlock(Store store, Txn txn) {
        super(store);
        asyncUpsert(txn);
        txn.addToCreatedObjects(this);
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
        if (!innerPutIfPossible(key, val)) {
            int size = sizeOfObject(key) + sizeOfObject(val);
            throw new IllegalStateException("Object size is " + (size + size()) + " bytes and exceeds the limit of " + getMaxByteSize() + " bytes");
        }
    }

    boolean putIfPossible(Key key, Value val, Txn txn) {
        if (txn == null) throw new IllegalArgumentException("Txn cannot be null");
        checkDataStructureRetrieved();
        boolean didPut = innerPutIfPossible(key, val);
        if (didPut) {
            txn.addToChangedObjects(this);
        }
        return didPut;
    }

    public boolean delete(Key key, Txn txn) {
        if (txn == null) throw new IllegalArgumentException("Txn cannot be null");
        checkDataStructureRetrieved();
        boolean didDelete = innerDelete(key);
        if (didDelete) {
            txn.addToChangedObjects(this);
        }
        return didDelete;
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

        Node x = first;
        // iterate through all keys :/
        // I know, I know
        while (x != null) {
            if (x.key.equals(key)) {
                x.value = val;
                // we're done...let's get out of here
                return;
            }
            x = x.next;
        }

        // we reached here that means, there is no node with the key in question
        // hence we go and create a new node
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
            // no need to be afraid of null
            first = first.next;
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
        if (x != null) {
            Class keyKlass = x.key.getClass();
            Class valueKlass = x.value.getClass();
            // write in the class information at the beginning
            out.writeType(keyKlass);
            out.writeType(valueKlass);
            while (x != null) {
                // subsequently just shove in the data
                // without type information
                out.writeObject(x.key, keyKlass);
                out.writeObject(x.value, valueKlass);
                x = x.next;
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    void deserialize(SerializerInputStream in) {
        boolean shouldDoIt = true;
        try {
            if (in.available() >= 8) {
                // slurp out type info first
                Class<Key> keyKlass = in.readType();
                Class<Value> valueKlass = in.readType();
                while (shouldDoIt && in.available() > 0) {
                    // on the upside we don't need to worry
                    // about type information anymore :)
                    Key key = in.readObject(keyKlass);
                    shouldDoIt = key != null;
                    if (shouldDoIt) {
                        // if we have a key, there must be a value
                        Value value = in.readObject(valueKlass);
                        first = new Node(key, value, first);
                        addObjectToObjectSize(key);
                        addObjectToObjectSize(value);
                    }
                }
            }
        } catch (IOException xcp) {
            throw new IllegalStateException(xcp);
        }
    }
}
