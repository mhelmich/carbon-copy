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

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import org.carbon.copy.GuiceJUnit4Runner;
import org.carbon.copy.GuiceModules;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;

@RunWith(GuiceJUnit4Runner.class)
@GuiceModules({ DataStructureModule.class, TxnManagerModule.class })
public class GalaxyBTreeTest {
    @Inject
    private InternalDataStructureFactory dsFactory;

    @Inject
    private TxnManager txnManager;

    @Test
    public void testBasicWithOneNode() throws IOException {
        long treeId;
        Txn txn = txnManager.beginTransaction();
        BTree<String, String> t = dsFactory.newBTree(txn);
        t.put("key_1", "value_1", txn);
        t.put("key_2", "value_2", txn);
        t.put("key_3", "value_3", txn);
        treeId = t.getId();
        txn.commit();

        BTree<String, String> t2 = dsFactory.loadBTree(treeId);
        assertEquals("value_2", t2.get("key_2"));
        assertEquals("value_3", t2.get("key_3"));
        assertEquals("value_1", t2.get("key_1"));
    }

    @Test
    public void testBasicWithMultipleNodes() throws IOException {
        long treeId;
        int count = BTree.MAX_NODE_SIZE * 3;
        Txn txn = txnManager.beginTransaction();
        BTree<Integer, String> t = dsFactory.newBTree(txn);
        for (int i = 0; i < count; i++) {
            t.put(i, "value_" + i, txn);
        }
        treeId = t.getId();
        txn.commit();

        BTree<Integer, String> t2 = dsFactory.loadBTree(treeId);
        for (int i = 0; i < count; i++) {
            assertEquals("value_" + i, t2.get(i));
        }
    }

    @Test
    public void testBasicWithMultipleNodesReadInViaDump() throws IOException {
        long treeId;
        int count = BTree.MAX_NODE_SIZE * 3;
        Txn txn = txnManager.beginTransaction();
        BTree<Integer, String> t = dsFactory.newBTree(txn);
        for (int i = 0; i < count; i++) {
            t.put(i, "value_" + i, txn);
        }
        treeId = t.getId();
        txn.commit();
        System.err.println(t.dump());

        BTree<Integer, String> t2 = dsFactory.loadBTree(treeId);
        System.err.println(t2.dump());
        for (int i = 0; i < count; i++) {
            assertEquals("value_" + i, t2.get(i));
        }
    }

    @Test
    public void testValueIterationWithMultipleNodes() throws IOException {
        long treeId;
        int count = BTree.MAX_NODE_SIZE * 3;
        Txn txn = txnManager.beginTransaction();
        BTree<Integer, String> t = dsFactory.newBTree(txn);
        for (int i = 0; i < count; i++) {
            t.put(i, "value_" + i, txn);
        }
        treeId = t.getId();
        txn.commit();

        BTree<Integer, String> t2 = dsFactory.loadBTree(treeId);
        int assertionCount = 0;
        for (Integer i : t2.keys()) {
            assertEquals(Integer.valueOf(assertionCount), i);
            assertionCount++;
        }

        assertEquals(count, assertionCount);
    }

    @Test
    public void testCreateReadWriteRead() throws IOException {
        long treeId;
        int count = BTree.MAX_NODE_SIZE * 3;
        Txn txn1 = txnManager.beginTransaction();

        BTree<Integer, String> t1 = dsFactory.newBTree(txn1);
        for (int i = 0; i < count; i++) {
            t1.put(i, "value_" + i, txn1);
        }
        treeId = t1.getId();
        txn1.commit();


        Txn txn2 = txnManager.beginTransaction();
        BTree<Integer, String> t2 = dsFactory.loadBTreeForWrites(treeId, txn2);
        for (int i = 0; i < count; i++) {
            t2.put(i, "value_" + (-i), txn2);
        }
        txn2.commit();

        BTree<Integer, String> t3 = dsFactory.loadBTree(treeId);
        for (int i = 0; i < count; i++) {
            assertEquals("value_" + (-i), t3.get(i));
        }
    }

    @Test
    public void testBTreeWithTupleAsKey() throws IOException {
        // create a random but deterministic set of test data
        List<String> testStrings = new ArrayList<>();
        testStrings.add("03f68232-a973-44e6-ab0f-503f16360551");
        testStrings.add("0c95abf7-9bf3-4ca0-a3ce-152331123e63");
        testStrings.add("27f5aabc-8030-447a-94b1-af1494e826eb");
        testStrings.add("2a1287e2-71a1-4f4d-b6cc-3a5556fe07bd");
        testStrings.add("3622f901-55cd-4322-b9e2-c62331fb47f0");
        testStrings.add("3dc00c04-ce1c-4184-8009-6e58b96b66da");
        testStrings.add("55a02110-2094-40c2-af2d-a1b3d9d9dde4");
        testStrings.add("5a06c35e-ee63-403f-a056-4fb9a5ac9875");
        testStrings.add("6d5fcbf9-8e3c-462b-a8be-90c658dc8bc2");
        testStrings.add("7025cd98-78a4-4073-892f-5991ebfd3ac8");
        // three times the same string
        testStrings.add("770287c9-a2fe-433a-9892-13096a384c72");
        testStrings.add("770287c9-a2fe-433a-9892-13096a384c72");
        testStrings.add("770287c9-a2fe-433a-9892-13096a384c72");
        testStrings.add("915f1372-f6fc-49b4-991e-db7746696209");
        testStrings.add("977226a9-b805-4941-a9fd-c4a7c2377403");
        testStrings.add("b121a7dd-9b6d-4b56-864e-7d9de638dbdb");
        testStrings.add("bd54ff4d-8723-40cd-9891-6f4b41817fd8");
        testStrings.add("c253a084-9414-4b9b-bacc-5bcd9bf588af");
        testStrings.add("c66ffec1-7744-4f0a-97fa-e059319747c3");
        testStrings.add("cf2fe233-1cd8-4e4d-932f-3bd4b7baab3f");

        List<String> assertString = ImmutableList.copyOf(testStrings);

        List<Integer> testInts = new ArrayList<>();
        testInts.add(1);
        testInts.add(1);
        testInts.add(1);
        testInts.add(1);
        testInts.add(1);
        testInts.add(1);
        testInts.add(1);
        testInts.add(1);
        testInts.add(1);
        testInts.add(1);
        // these line up with the same strings
        testInts.add(34);
        testInts.add(45);
        testInts.add(56);
        testInts.add(1);
        testInts.add(1);
        testInts.add(1);
        testInts.add(1);
        testInts.add(1);
        testInts.add(1);
        testInts.add(1);

        Random r = new Random();
        Txn txn1 = txnManager.beginTransaction();
        BTree<Tuple, GUID> index = dsFactory.newBTree(txn1);

        int originalSize = testStrings.size();
        for (int i = 0; i < originalSize; i++) {
            Tuple tuple = new Tuple(2);
            int idx = (testStrings.size() > 0)
                    ? Math.abs(r.nextInt()) % testStrings.size()
                    : 0;

            String s = testStrings.remove(idx);
            Integer integer = testInts.remove(idx);
            tuple.put(0, s);
            tuple.put(1, integer);
            index.put(tuple, GUID.randomGUID(), txn1);
        }

        txn1.commit();

        BTree<Tuple, GUID> index2 = dsFactory.loadBTree(index.getId());
        int i = 0;
        for (Tuple t : index2.keys()) {
            assertEquals(assertString.get(i), t.get(0));
            i++;
        }
    }

    @Test
    public void testRangeScan() throws IOException {
        // create a random but deterministic set of test data
        List<String> testStrings = new ArrayList<>();
        testStrings.add("03f68232-a973-44e6-ab0f-503f16360551");
        testStrings.add("0c95abf7-9bf3-4ca0-a3ce-152331123e63");
        testStrings.add("27f5aabc-8030-447a-94b1-af1494e826eb");
        testStrings.add("2a1287e2-71a1-4f4d-b6cc-3a5556fe07bd");
        testStrings.add("3622f901-55cd-4322-b9e2-c62331fb47f0");
        testStrings.add("3dc00c04-ce1c-4184-8009-6e58b96b66da");
        testStrings.add("55a02110-2094-40c2-af2d-a1b3d9d9dde4");
        testStrings.add("5a06c35e-ee63-403f-a056-4fb9a5ac9875");
        testStrings.add("6d5fcbf9-8e3c-462b-a8be-90c658dc8bc2");
        testStrings.add("7025cd98-78a4-4073-892f-5991ebfd3ac8");
        // three times the same string
        testStrings.add("770287c9-a2fe-433a-9892-13096a384c72");
        testStrings.add("770287c9-a2fe-433a-9892-13096a384c72");
        testStrings.add("770287c9-a2fe-433a-9892-13096a384c72");
        testStrings.add("915f1372-f6fc-49b4-991e-db7746696209");
        testStrings.add("977226a9-b805-4941-a9fd-c4a7c2377403");
        testStrings.add("b121a7dd-9b6d-4b56-864e-7d9de638dbdb");
        testStrings.add("bd54ff4d-8723-40cd-9891-6f4b41817fd8");
        testStrings.add("c253a084-9414-4b9b-bacc-5bcd9bf588af");
        testStrings.add("c66ffec1-7744-4f0a-97fa-e059319747c3");
        testStrings.add("cf2fe233-1cd8-4e4d-932f-3bd4b7baab3f");

        List<Integer> testInts = new ArrayList<>();
        testInts.add(1);
        testInts.add(1);
        testInts.add(1);
        testInts.add(1);
        testInts.add(1);
        testInts.add(1);
        testInts.add(1);
        testInts.add(1);
        testInts.add(1);
        testInts.add(1);
        // these line up with the same strings
        testInts.add(34);
        testInts.add(45);
        testInts.add(56);
        testInts.add(1);
        testInts.add(1);
        testInts.add(1);
        testInts.add(1);
        testInts.add(1);
        testInts.add(1);
        testInts.add(1);

        List<GUID> testGuids = new ArrayList<>();
        testGuids.add(GUID.randomGUID());
        testGuids.add(GUID.randomGUID());
        testGuids.add(GUID.randomGUID());
        testGuids.add(GUID.randomGUID());
        testGuids.add(GUID.randomGUID());
        testGuids.add(GUID.randomGUID());
        testGuids.add(GUID.randomGUID());
        testGuids.add(GUID.randomGUID());
        testGuids.add(GUID.randomGUID());
        testGuids.add(GUID.randomGUID());
        // these line up with the same strings
        testGuids.add(GUID.randomGUID());
        testGuids.add(GUID.randomGUID());
        testGuids.add(GUID.randomGUID());
        testGuids.add(GUID.randomGUID());
        testGuids.add(GUID.randomGUID());
        testGuids.add(GUID.randomGUID());
        testGuids.add(GUID.randomGUID());
        testGuids.add(GUID.randomGUID());
        testGuids.add(GUID.randomGUID());
        testGuids.add(GUID.randomGUID());

        List<GUID> assertGUID = ImmutableList.copyOf(testGuids);

        Random r = new Random();
        Txn txn1 = txnManager.beginTransaction();
        BTree<Tuple, GUID> index = dsFactory.newBTree(txn1);

        int originalSize = testStrings.size();
        for (int i = 0; i < originalSize; i++) {
            Tuple tuple = new Tuple(2);
            int idx = (testStrings.size() > 0)
                    ? Math.abs(r.nextInt()) % testStrings.size()
                    : 0;

            String s = testStrings.remove(idx);
            Integer integer = testInts.remove(idx);
            GUID guid = testGuids.remove(idx);
            tuple.put(0, s);
            tuple.put(1, integer);
            index.put(tuple, guid, txn1);
        }

        txn1.commit();

        BTree<Tuple, GUID> index2 = dsFactory.loadBTree(index.getId());
        Tuple fromTuple = new Tuple(2);
        fromTuple.put(0, "6d5fcbf9-8e3c-462b-a8be-90c658dc8bc2");
        fromTuple.put(1, 1);
        Tuple toTuple = new Tuple(2);
        toTuple.put(0, "770287c9-a2fe-433a-9892-13096a384c72");
        toTuple.put(1, 99);
        Iterable<GUID> iter = index2.get(fromTuple, toTuple);

        int i = 0;
        int assertionIndex = 8;
        for (GUID guid : iter) {
            assertEquals(assertGUID.get(assertionIndex), guid);
            assertionIndex++;
            i++;
        }
        assertEquals(5, i);
    }

    @Test
    public void testIntegerFirstInTuple() throws IOException {
        // create a random but deterministic set of test data
        List<String> testStrings = new ArrayList<>();
        testStrings.add("03f68232-a973-44e6-ab0f-503f16360551");
        testStrings.add("0c95abf7-9bf3-4ca0-a3ce-152331123e63");
        testStrings.add("27f5aabc-8030-447a-94b1-af1494e826eb");
        testStrings.add("2a1287e2-71a1-4f4d-b6cc-3a5556fe07bd");
        testStrings.add("3622f901-55cd-4322-b9e2-c62331fb47f0");
        testStrings.add("3dc00c04-ce1c-4184-8009-6e58b96b66da");
        testStrings.add("55a02110-2094-40c2-af2d-a1b3d9d9dde4");
        testStrings.add("5a06c35e-ee63-403f-a056-4fb9a5ac9875");
        testStrings.add("6d5fcbf9-8e3c-462b-a8be-90c658dc8bc2");
        testStrings.add("7025cd98-78a4-4073-892f-5991ebfd3ac8");
        // three times the same string
        testStrings.add("770287c9-a2fe-433a-9892-13096a384c72");
        testStrings.add("770287c9-a2fe-433a-9892-13096a384c72");
        testStrings.add("770287c9-a2fe-433a-9892-13096a384c72");
        testStrings.add("915f1372-f6fc-49b4-991e-db7746696209");
        testStrings.add("977226a9-b805-4941-a9fd-c4a7c2377403");
        testStrings.add("b121a7dd-9b6d-4b56-864e-7d9de638dbdb");
        testStrings.add("bd54ff4d-8723-40cd-9891-6f4b41817fd8");
        testStrings.add("c253a084-9414-4b9b-bacc-5bcd9bf588af");
        testStrings.add("c66ffec1-7744-4f0a-97fa-e059319747c3");
        testStrings.add("cf2fe233-1cd8-4e4d-932f-3bd4b7baab3f");

        List<Integer> testInts = new ArrayList<>();
        testInts.add(-1);
        testInts.add(0);
        testInts.add(1);
        testInts.add(2);
        testInts.add(3);
        testInts.add(5);
        testInts.add(7);
        testInts.add(11);
        testInts.add(13);
        // these line up with the same strings
        testInts.add(34);
        testInts.add(45);
        testInts.add(56);
        testInts.add(67);
        testInts.add(70);
        testInts.add(77);
        testInts.add(85);
        testInts.add(123);
        testInts.add(125);
        testInts.add(187);
        testInts.add(1234);

        List<GUID> testGuids = new ArrayList<>();
        testGuids.add(GUID.randomGUID());
        testGuids.add(GUID.randomGUID());
        testGuids.add(GUID.randomGUID());
        testGuids.add(GUID.randomGUID());
        testGuids.add(GUID.randomGUID());
        testGuids.add(GUID.randomGUID());
        testGuids.add(GUID.randomGUID());
        testGuids.add(GUID.randomGUID());
        testGuids.add(GUID.randomGUID());
        testGuids.add(GUID.randomGUID());
        // these line up with the same strings
        testGuids.add(GUID.randomGUID());
        testGuids.add(GUID.randomGUID());
        testGuids.add(GUID.randomGUID());
        testGuids.add(GUID.randomGUID());
        testGuids.add(GUID.randomGUID());
        testGuids.add(GUID.randomGUID());
        testGuids.add(GUID.randomGUID());
        testGuids.add(GUID.randomGUID());
        testGuids.add(GUID.randomGUID());
        testGuids.add(GUID.randomGUID());

        List<GUID> assertGUID = ImmutableList.copyOf(testGuids);

        Random r = new Random();
        Txn txn1 = txnManager.beginTransaction();
        BTree<Tuple, GUID> index = dsFactory.newBTree(txn1);

        int originalSize = testStrings.size();
        for (int i = 0; i < originalSize; i++) {
            Tuple tuple = new Tuple(2);
            int idx = (testStrings.size() > 0)
                    ? Math.abs(r.nextInt()) % testStrings.size()
                    : 0;

            String s = testStrings.remove(idx);
            Integer integer = testInts.remove(idx);
            GUID guid = testGuids.remove(idx);
            tuple.put(0, integer);
            tuple.put(1, s);
            index.put(tuple, guid, txn1);
        }

        txn1.commit();

        BTree<Tuple, GUID> index2 = dsFactory.loadBTree(index.getId());
        Tuple fromTuple = new Tuple(2);
        fromTuple.put(0, 4);
        fromTuple.put(1, "6d5fcbf9-8e3c-462b-a8be-90c658dc8bc2");
        Tuple toTuple = new Tuple(2);
        toTuple.put(0, 99);
        toTuple.put(1, "770287c9-a2fe-433a-9892-13096a384c72");
        Iterable<GUID> iter = index2.get(fromTuple, toTuple);

        int i = 0;
        int assertionIndex = 5;
        for (GUID guid : iter) {
            assertEquals(assertGUID.get(assertionIndex), guid);
            assertionIndex++;
            i++;
        }
        assertEquals(11, i);
    }
}
