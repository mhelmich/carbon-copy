package org.distbc.data.structures;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

public class ChainingHash<Key extends Comparable<Key>, Value> extends DataStructure {
    private static final int EXPANSION_FACTOR = 3;
    static final int DEFAULT_NUM_BUCKETS = 4;

    private int hashTableSize;
    private ArrayList<DataBlock<Key, Value>> hashTable;

    ChainingHash() {
        this(DEFAULT_NUM_BUCKETS);
    }

    ChainingHash(int initNumBuckets) {
        super();
        this.hashTableSize = initNumBuckets;
        Vector<DataBlock<Key, Value>> v = new Vector<>(initNumBuckets);
        v.setSize(initNumBuckets);
        hashTable = new ArrayList<>(v);
    }

    ChainingHash(long id) {
        super(id);
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

    private int hash(Key key) {
        return (key.hashCode() & 0x7fffffff) % hashTableSize;
    }

    private void resize(int newNumBuckets) {
        // resizing by copying
        ChainingHash<Key, Value> temp = new ChainingHash<>(newNumBuckets);
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
        return hashTable.get(i);
    }

    DataBlock<Key, Value> newDataBlock() {
        return new DataBlock<>();
    }

    @Override
    void serialize(SerializerOutputStream out) {
        for (int i = 0; i < hashTableSize; i++) {
            DataBlock<Key, Value> db = getDataBlock(i);
            out.writeObject((db != null) ? db.getId() : null);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    void deserialize(SerializerInputStream in) {
        int i = 0;
        try {
            while (in.available() > 0) {
                Long id = (Long) in.readObject();
                DataBlock<Key, Value> db = (id != null) ? new DataBlock<>(id) : null;
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