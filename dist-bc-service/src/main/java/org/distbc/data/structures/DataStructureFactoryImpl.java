package org.distbc.data.structures;

import co.paralleluniverse.galaxy.Store;
import com.google.inject.Inject;

public class DataStructureFactoryImpl implements InternalDataStructureFactory {

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
        return new DataBlock<>(store, id, true);
    }

    @Override
    public <Key extends Comparable<Key>, Value> DataBlock<Key, Value> loadDataBlockProxy(long id) {
        return new DataBlock<>(store, id, false);
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
    public <Key extends Comparable<Key>, Value> BTreeNode<Key, Value> loadBTreeNode(long id) {
        return new BTreeNode<>(store, this, id, true);
    }

    @Override
    public <Key extends Comparable<Key>, Value> BTreeNode<Key, Value> loadBTreeNodeProxy(long id) {
        return new BTreeNode<>(store, this, id, false);
    }

    @Override
    public <Key extends Comparable<Key>, Value> BTree<Key, Value> newBTree(Txn txn) {
        return new BTree<>(store, this, txn);
    }

    @Override
    public <Key extends Comparable<Key>, Value> BTree<Key, Value> loadBTree(long id) {
        return new BTree<>(store, this, id);
    }

    @Override
    public <Key extends Comparable<Key>, Value> BTree<Key, Value> loadBTreeForWrites(long id, Txn txn) {
        return new BTree<>(store, this, id, txn);
    }

    @Override
    public Table newTable(Table.Builder builder, Txn txn) {
        return new Table(store, this, builder, txn);
    }

    @Override
    public Table loadTable(long id) {
        return new Table(store, this, id);
    }

    @Override
    public Table loadTableForWrites(long id, Txn txn) {
        return new Table(store, this, id, txn);
    }

    @Override
    public Index newIndex(Index.Builder builder, Txn txn) {
        return new Index(store, this, builder, txn);
    }

    @Override
    public Index loadIndex(long id) {
        return new Index(store, this, id);
    }

    @Override
    public Index loadIndexForWrites(long id, Txn txn) {
        return new Index(store, this, id, txn);
    }
}
