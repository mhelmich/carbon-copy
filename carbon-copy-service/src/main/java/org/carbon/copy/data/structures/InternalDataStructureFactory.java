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

/**
 * Use me to get (or create) data structures.
 * Don't even think about using them directly!
 *
 * Don't even try to use guice assisted injection
 * I was dancing around for two days and it didn't line up for me until I decided
 * to put this factory together myself and move on with my life
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

    <Key extends Comparable<Key>, Value> DistHash<Key, Value> newDistHash(Txn txn);
    <Key extends Comparable<Key>, Value> DistHash<Key, Value> loadDistHash(long id);
    <Key extends Comparable<Key>, Value> DistHash<Key, Value> loadDistHashForWrites(long id, Txn txn);
}
