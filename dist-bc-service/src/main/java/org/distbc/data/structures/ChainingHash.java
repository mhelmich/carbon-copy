package org.distbc.data.structures;

import co.paralleluniverse.galaxy.Store;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

class ChainingHash<Key extends Comparable<Key>, Value> extends DataStructure {
    private static final int EXPANSION_FACTOR = 3;
    static final int DEFAULT_NUM_BUCKETS = 4;

    private int hashTableSize;
    private ArrayList<DataBlock<Key, Value>> hashTable;

    private final InternalDataStructureFactory dsFactory;

    ChainingHash(Store store, InternalDataStructureFactory dsFactory, Txn txn) {
        this(store, dsFactory, DEFAULT_NUM_BUCKETS, txn);
    }

    ChainingHash(Store store, InternalDataStructureFactory dsFactory, int initNumBuckets, Txn txn) {
        super(store);
        this.dsFactory = dsFactory;
        asyncUpsert(txn);
        this.hashTableSize = initNumBuckets;
        Vector<DataBlock<Key, Value>> v = new Vector<>(initNumBuckets);
        v.setSize(initNumBuckets);
        hashTable = new ArrayList<>(v);
        // increment object (this is the hash) since we allocate
        // a DataBlock pointer (which is a Long)
        for (int i = 0; i < initNumBuckets; i++) {
            addObjectToObjectSize(Long.MAX_VALUE);
        }
    }

    ChainingHash(Store store, InternalDataStructureFactory dsFactory, long id) {
        super(store, id);
        this.dsFactory = dsFactory;
        // load data for reads aggressively
        asyncLoadForReads();
    }

    ChainingHash(Store store, InternalDataStructureFactory dsFactory, long id, Txn txn) {
        super(store, id);
        this.dsFactory = dsFactory;
        // load data for writes aggressively too
        asyncLoadForWrites(txn);
    }

    public Value get(Key key) {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");
        int i = hash(key);
        DataBlock<Key, Value> db = getDataBlock(i);
        return (db != null) ? db.get(key) : null;
    }

    public void put(Key key, Value val, Txn txn) {
        if (txn == null) throw new IllegalArgumentException("Txn cannot be null");
        checkDataStructureRetrieved();
        txn.addToChangedObjects(this);
        innerPut(key, val, txn);
    }

    public boolean delete(Key key, Txn txn) {
        if (txn == null) throw new IllegalArgumentException("Txn cannot be null");
        checkDataStructureRetrieved();
        return innerDelete(key, txn);
    }

    public Iterable<Key> keys() {
        return () -> new Iterator<Key>() {
            private int i = 0;
            private Iterator<Key> dbIter;

            @Override
            public boolean hasNext() {
                if (dbIter == null || !dbIter.hasNext()) {
                    while (i < hashTableSize && (dbIter == null || !dbIter.hasNext())) {
                        DataBlock<Key, Value> db = getDataBlock(i);
                        i++;
                        if (db != null) {
                            dbIter = db.keys().iterator();
                            if (dbIter.hasNext()) {
                                return true;
                            }
                        }
                    }

                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public Key next() {
                return dbIter.next();
            }
        };
    }

    /////////////////////////////////////////////////////////////
    //////////////////////////////////////////////
    // internal unit testable data structure implementation

    private void innerPut(Key key, Value val, Txn txn) {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");

        int i = hash(key);
        DataBlock<Key, Value> db = getDataBlock(i, txn);
        if (db == null) {
            DataBlock<Key, Value> newDB = newDataBlock(txn);
            newDB.put(key, val, txn);
            hashTable.set(i, newDB);
        } else {
            if (!db.putIfPossible(key, val, txn)) {
                resize(hashTableSize * EXPANSION_FACTOR, txn);
                put(key, val, txn);
            }
        }
    }

    private boolean innerDelete(Key key, Txn txn) {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");
        int i = hash(key);
        DataBlock<Key, Value> db = getDataBlock(i, txn);
        boolean didDelete = (db != null) && db.innerDelete(key);
        if (db != null && didDelete) {
            txn.addToChangedObjects(db);
        }
        return didDelete;
    }

    private int hash(Key key) {
        return (key.hashCode() & 0x7fffffff) % hashTableSize;
    }

    private void resize(int newNumBuckets, Txn txn) {
        // resizing by copying
        ChainingHash<Key, Value> temp = dsFactory.newChainingHashWithNumBuckets(newNumBuckets, txn);
        for (int i = 0; i < hashTableSize; i++) {
            DataBlock<Key, Value> db = getDataBlock(i);
            if (db != null) {
                db.keys().forEach(k -> temp.put(k, db.get(k), txn));
                db.asyncDelete(txn);
            }
        }

        this.hashTableSize  = temp.hashTableSize;
        this.hashTable = temp.hashTable;
    }

    private DataBlock<Key, Value> getDataBlock(int hash, Txn txn) {
        DataBlock<Key, Value> db = hashTable.get(hash);
        if (db != null) {
            db.asyncLoadForWrites(txn);
            db.checkDataStructureRetrieved();
        }
        return db;
    }

    private DataBlock<Key, Value> getDataBlock(int hash) {
        DataBlock<Key, Value> db = hashTable.get(hash);
        if (db != null) {
            db.asyncLoadForReads();
            db.checkDataStructureRetrieved();
        }
        return db;
    }

    DataBlock<Key, Value> newDataBlock(Txn txn) {
        return dsFactory.newDataBlock(txn);
    }

    /////////////////////////////////////////////////////////////
    //////////////////////////////////////////////
    // galaxy-specific serialization overrides

    @Override
    void serialize(SerializerOutputStream out) {
        // stick in the hash table size as leading byte
        out.writeObject(hashTableSize);
        for (int i = 0; i < hashTableSize; i++) {
            DataBlock<Key, Value> db = getDataBlock(i);
            out.writeObject((db != null) ? db.getId() : null);
        }
    }

    @Override
    void deserialize(SerializerInputStream in) {
        Integer tmp;
        try {
            // the leading byte is the size of the hash table
            tmp = (Integer) in.readObject();
            hashTableSize = (tmp != null) ? tmp : 0;

            Vector<DataBlock<Key, Value>> v = new Vector<>(hashTableSize);
            v.setSize(hashTableSize);
            hashTable = new ArrayList<>(v);

            for (int i = 0; i < hashTableSize && in.available() > 0; i++) {
                Long id = (Long) in.readObject();
                DataBlock<Key, Value> db = (id != null) ? dsFactory.loadDataBlockProxy(id) : null;
                hashTable.set(i, db);
                addObjectToObjectSize(id);
            }
        } catch (IOException xcp) {
            throw new RuntimeException(xcp);
        }
    }
}
