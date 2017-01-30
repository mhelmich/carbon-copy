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
        super(store, id);
        this.dsFactory = dsFactory;
    }

    public Value get(Key key) {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");
        int i = hash(key);
        return getDataBlock(i).get(key);
    }

    public void put(Key key, Value val) {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");

        int i = hash(key);
        DataBlock<Key, Value> db = getDataBlock(i);
        if (db == null) {
            DataBlock<Key, Value> newDB = newDataBlock();
            newDB.put(key, val);
            hashTable.set(i, newDB);
        } else {
            if (!db.putIfPossible(key, val)) {
                resize(hashTableSize * EXPANSION_FACTOR);
                put(key, val);
            }
        }
    }

    public void delete(Key key) {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");
        int i = hash(key);
        hashTable.get(i).delete(key);
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
                if (dbIter == null || !dbIter.hasNext()) {
                    dbIter = hashTable.get(i).keys().iterator();
                    i++;
                }
                return dbIter.next();
            }
        };
    }

    private int hash(Key key) {
        return (key.hashCode() & 0x7fffffff) % hashTableSize;
    }

    private void resize(int newNumBuckets) {
        // resizing by copying
        ChainingHash<Key, Value> temp = dsFactory.newChainingHashWithNumBuckets(newNumBuckets);
        for (int i = 0; i < hashTableSize; i++) {
            DataBlock<Key, Value> db = getDataBlock(i);
            if (db != null) {
                db.keys().forEach(k -> temp.put(k, db.get(k)));
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

    @Override
    public int size() {
        return super.size();
    }
}
