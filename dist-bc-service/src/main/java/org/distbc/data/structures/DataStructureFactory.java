package org.distbc.data.structures;

/**
 * Don't even try to use guice assisted injection
 * I was dancing around for two days and it didn't line up for me until I decided
 * to innerPut this factory together myself and move on with my life
 */
public interface DataStructureFactory {
    <Key extends Comparable<Key>, Value> DataBlock<Key, Value> newDataBlock(Txn txn);
    <Key extends Comparable<Key>, Value> DataBlock<Key, Value> loadDataBlock(long id);
    <Key extends Comparable<Key>, Value> DataBlock<Key, Value> loadDataBlockForWrites(long id, Txn txn);

    <Key extends Comparable<Key>, Value> ChainingHash<Key, Value> newChainingHash();
    <Key extends Comparable<Key>, Value> ChainingHash<Key, Value> newChainingHashWithNumBuckets(int numBuckets);
    <Key extends Comparable<Key>, Value> ChainingHash<Key, Value> loadChainingHash(long id);
    <Key extends Comparable<Key>, Value> ChainingHash<Key, Value> loadChainingHashForWrites(long id, Txn txn);
}
