package org.distbc.data.structures;

import co.paralleluniverse.galaxy.Store;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

public class ChainingHash<Key extends Comparable<Key>, Value> extends DataStructure {
    private static final int EXPANSION_FACTOR = 3;
    static final int DEFAULT_NUM_BUCKETS = 4;

    private int hashTableSize;
    private ArrayList<DataBlock<Key, Value>> hashTable;

    private final DataStructureFactory dsFactory;

    ChainingHash(Store store, DataStructureFactory dsFactory) {
        this(store, dsFactory, DEFAULT_NUM_BUCKETS);
    }

    ChainingHash(Store store, DataStructureFactory dsFactory, int initNumBuckets) {
        super(store);
        this.dsFactory = dsFactory;
        this.hashTableSize = initNumBuckets;
        Vector<DataBlock<Key, Value>> v = new Vector<>(initNumBuckets);
        v.setSize(initNumBuckets);
        hashTable = new ArrayList<>(v);
    }

    ChainingHash(Store store, DataStructureFactory dsFactory, long id) {
        this(store, dsFactory, id, null);
    }

    ChainingHash(Store store, DataStructureFactory dsFactory, long id, Txn txn) {
        super(store, id, txn);
        this.dsFactory = dsFactory;
    }

    public Value get(Key key) {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");
        int i = hash(key);
        return getDataBlock(i).get(key);
    }

    void innerPut(Key key, Value val, Txn txn) {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");

        int i = hash(key);
        DataBlock<Key, Value> db = getDataBlock(i);
        if (db == null) {
            DataBlock<Key, Value> newDB = newDataBlock();
            newDB.put(key, val, txn);
            hashTable.set(i, newDB);
        } else {
            if (!db.putIfPossible(key, val, txn)) {
                resize(hashTableSize * EXPANSION_FACTOR, txn);
                put(val, key, txn);
            }
        }
    }

    public void put(Value val, Key key, Txn txn) {
        if (txn == null) throw new IllegalArgumentException("Txn cannot be null");
        innerPut(key, val, txn);
    }

    void innerDelete(Key key) {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");
        int i = hash(key);
        hashTable.get(i).innerDelete(key);
    }

    public void delete(Key key, Txn txn) {
        if (txn == null) throw new IllegalArgumentException("Txn cannot be null");
        innerDelete(key);
    }

    public Iterable<Key> keys() {
        return () -> new Iterator<Key>() {
            private int i = 0;
            private Iterator<Key> dbIter;

            @Override
            public boolean hasNext() {
                return i < hashTableSize || dbIter.hasNext();
            }

            @Override
            public Key next() {
                while (dbIter == null || !dbIter.hasNext()) {
                    DataBlock<Key, Value> db = hashTable.get(i);
                    if (db != null) {
                        dbIter = db.keys().iterator();
                    }
                    i++;
                }
                return dbIter.next();
            }
        };
    }

    private int hash(Key key) {
        return (key.hashCode() & 0x7fffffff) % hashTableSize;
    }

    private void resize(int newNumBuckets, Txn txn) {
        // resizing by copying
        ChainingHash<Key, Value> temp = dsFactory.newChainingHashWithNumBuckets(newNumBuckets);
        for (int i = 0; i < hashTableSize; i++) {
            DataBlock<Key, Value> db = getDataBlock(i);
            if (db != null) {
                db.keys().forEach(k -> temp.put(db.get(k), k, txn));
            }
        }

        this.hashTableSize  = temp.hashTableSize;
        this.hashTable = temp.hashTable;
    }

    private DataBlock<Key, Value> getDataBlock(int i) {
        DataBlock<Key, Value> db = hashTable.get(i);
        if (db != null) {
            db.checkDataStructureRetrieved();
        }
        return db;
    }

    DataBlock<Key, Value> newDataBlock() {
        // increment object (this is the hash) since we allocate
        // a DataBlock pointer (which is a Long)
        addObjectToObjectSize(123L);
        return dsFactory.newDataBlock();
    }

    @Override
    void serialize(SerializerOutputStream out) {
        for (int i = 0; i < hashTableSize; i++) {
            DataBlock<Key, Value> db = getDataBlock(i);
            out.writeObject((db != null) ? db.getId() : null);
        }
    }

    @Override
    void deserialize(SerializerInputStream in) {
        int i = 0;
        try {
            while (in.available() > 0) {
                Long id = (Long) in.readObject();
                DataBlock<Key, Value> db = (id != null) ? dsFactory.loadDataBlock(id) : null;
                hashTable.set(i, db);
                i++;
            }
        } catch (IOException xcp) {
            throw new RuntimeException(xcp);
        }
    }
}
