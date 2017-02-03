package org.distbc.data.structures;

import co.paralleluniverse.galaxy.Store;
import com.google.inject.Inject;

public class DataStructureFactoryImpl implements DataStructureFactory {

    private final Store store;

    @Inject
    DataStructureFactoryImpl(Store store) {
        this.store = store;
    }

    @Override
    public <Key extends Comparable<Key>, Value> DataBlock<Key, Value> newDataBlock(Txn txn) {
        return new DataBlock<>(store, txn);
    }

    @Override
    public <Key extends Comparable<Key>, Value> DataBlock<Key, Value> loadDataBlock(long id) {
        return new DataBlock<>(store, id);
    }

    @Override
    public <Key extends Comparable<Key>, Value> DataBlock<Key, Value> loadDataBlockForWrites(long id, Txn txn) {
        return new DataBlock<>(store, id, txn);
    }

    @Override
    public <Key extends Comparable<Key>, Value> ChainingHash<Key, Value> newChainingHash(Txn txn) {
        return new ChainingHash<>(store, this, txn);
    }

    @Override
    public <Key extends Comparable<Key>, Value> ChainingHash<Key, Value> newChainingHashWithNumBuckets(int numBuckets, Txn txn) {
        return new ChainingHash<>(store, this, numBuckets, txn);
    }

    @Override
    public <Key extends Comparable<Key>, Value> ChainingHash<Key, Value> loadChainingHash(long id) {
        return new ChainingHash<>(store, this, id);
    }

    @Override
    public <Key extends Comparable<Key>, Value> ChainingHash<Key, Value> loadChainingHashForWrites(long id, Txn txn) {
        return new ChainingHash<>(store, this, id, txn);
    }

    @Override
    public <Key extends Comparable<Key>, Value> BTreeNode<Key, Value> newBTreeNode(int numChildren, Txn txn) {
        return new BTreeNode<>(store, this, numChildren, txn);
    }

    @Override
    public <Key extends Comparable<Key>, Value> BTreeNode<Key, Value> newBTreeNode(long id) {
        return new BTreeNode<>(store, this, id);
    }

    @Override
    public <Key extends Comparable<Key>, Value> BTree<Key, Value> newBTree(Txn txn) {
        return new BTree<>(store, this, txn);
    }
}
