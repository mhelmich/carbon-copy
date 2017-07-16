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

import com.google.inject.Inject;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class GalaxyChainingHashTest extends GalaxyBaseTest {
    @Inject
    private InternalDataStructureFactory dsFactory;

    @Inject
    private TxnManager txnManager;

    @Test
    public void testBasicPutGet() throws IOException {
        Txn t = txnManager.beginTransaction();
        ChainingHash<Integer, Long> hash = dsFactory.newChainingHash(t);
        hash.put(123, 123L, t);
        t.commit();

        // time goes by
        ChainingHash<Integer, Long> db2 = dsFactory.loadChainingHash(hash.getId());
        assertEquals(Long.valueOf(123), db2.get(123));
        assertNull(db2.get(125));
    }

    @Test
    public void testPutGetDelete() throws IOException {
        int count = 123;
        Txn t = txnManager.beginTransaction();
        ChainingHash<Integer, Long> hash = dsFactory.newChainingHash(t);
        for (int i = 0; i < count; i++) {
            hash.put(i, (long) i, t);
        }
        t.commit();

        // time goes by
        ChainingHash<Integer, Long> hash2 = dsFactory.loadChainingHash(hash.getId());
        assertPresent(0, count, hash2);
        assertNull(hash2.get(125));

        t = txnManager.beginTransaction();
        ChainingHash<Integer, Long> hash3 = dsFactory.loadChainingHashForWrites(hash.getId(), t);
        assertTrue(hash3.delete(111, t));
        assertTrue(hash3.delete(99, t));
        t.commit();

        ChainingHash<Integer, Long> hash4 = dsFactory.loadChainingHash(hash.getId());
        assertPresent(0, count, hash2);
        assertNull(hash4.get(125));

        assertPresent(0, 99, hash4);
        assertPresent(100, 111, hash4);
        assertPresent(112, count, hash4);
        assertNull(hash4.get(99));
        assertNull(hash4.get(111));
    }

    @Test
    public void testStoreReadEmptyHash() throws IOException {
        Txn t = txnManager.beginTransaction();
        ChainingHash<Integer, Long> hash = dsFactory.newChainingHash(t);
        hash.checkDataStructureRetrieved();
        t.commit();

        t = txnManager.beginTransaction();
        ChainingHash<Integer, Long> hash2 = dsFactory.loadChainingHash(hash.getId());
        hash2.checkDataStructureRetrieved();
        hash2.put(123, 123L, t);
        t.commit();
    }

    @Test
    public void testSchemaHash() throws IOException {
        Txn t = txnManager.beginTransaction();
        ChainingHash<String, Tuple> hash = dsFactory.newChainingHash(t);

        Tuple t1 = new Tuple(3);
        t1.put(0, "tup_num");
        t1.put(1, 0);
        t1.put(2, Integer.class.getCanonicalName());
        hash.put("id", t1, t);

        Tuple t2 = new Tuple(3);
        t2.put(0, "foo");
        t2.put(1, 1);
        t2.put(2, String.class.getCanonicalName());
        hash.put("foo", t2, t);

        Tuple t3 = new Tuple(3);
        t3.put(0, "bar");
        t3.put(1, 2);
        t3.put(2, String.class.getCanonicalName());
        hash.put("bar", t3, t);

        t.commit();
    }

    @Test
    public void testTonsOfPairs() throws IOException {
        int count = 10000;
        Txn t = txnManager.beginTransaction();
        ChainingHash<Integer, Long> hash = dsFactory.newChainingHash(t);
        for (int i = 0; i < count; i++) {
            hash.put(i, (long) i, t);
        }
        t.commit();

        for (int i = 0; i < count; i++) {
            assertEquals(Long.valueOf(i), hash.get(i));
        }
    }

    private void assertPresent(int from, int to, ChainingHash<Integer, Long> hash) {
        for (int i = from; i < to; i++) {
            assertEquals(Long.valueOf(i), hash.get(i));
        }
    }
}
