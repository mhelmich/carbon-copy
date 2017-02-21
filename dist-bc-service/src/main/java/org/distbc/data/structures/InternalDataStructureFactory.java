package org.distbc.data.structures;

/**
 * Don't even try to use guice assisted injection
 * I was dancing around for two days and it didn't line up for me until I decided
 * to innerPut this factory together myself and move on with my life
 */
public interface InternalDataStructureFactory extends DataStructureFactory {
    <Key extends Comparable<Key>, Value> DataBlock<Key, Value> newDataBlock(Txn txn);
    <Key extends Comparable<Key>, Value> DataBlock<Key, Value> loadDataBlock(long id);
    <Key extends Comparable<Key>, Value> DataBlock<Key, Value> loadDataBlockProxy(long id);
    <Key extends Comparable<Key>, Value> DataBlock<Key, Value> loadDataBlockForWrites(long id, Txn txn);

    <Key extends Comparable<Key>, Value> ChainingHash<Key, Value> newChainingHash(Txn txn);
    <Key extends Comparable<Key>, Value> ChainingHash<Key, Value> newChainingHashWithNumBuckets(int numBuckets, Txn txn);
    <Key extends Comparable<Key>, Value> ChainingHash<Key, Value> loadChainingHash(long id);
    <Key extends Comparable<Key>, Value> ChainingHash<Key, Value> loadChainingHashForWrites(long id, Txn txn);

    <Key extends Comparable<Key>, Value> BTreeNode<Key, Value> newBTreeNode(int numChildren, Txn txn);
    <Key extends Comparable<Key>, Value> BTreeNode<Key, Value> loadBTreeNode(long id);
    <Key extends Comparable<Key>, Value> BTreeNode<Key, Value> loadBTreeNodeProxy(long id);

    <Key extends Comparable<Key>, Value> BTree<Key, Value> newBTree(Txn txn);
    <Key extends Comparable<Key>, Value> BTree<Key, Value> loadBTree(long id);
    <Key extends Comparable<Key>, Value> BTree<Key, Value> loadBTreeForWrites(long id, Txn txn);

    Table newTable(Txn txn);
    Table loadTable(long id);
    Table loadTableForWrites(long id, Txn txn);
}
