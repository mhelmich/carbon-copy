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

import co.paralleluniverse.galaxy.Store;
import com.google.inject.Inject;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class GalaxyDataBlockTest extends GalaxyBaseTest {
    @Inject
    private InternalDataStructureFactory dsFactory;

    @Inject
    private TxnManager txnManager;

    @Inject
    private Store store;

    @Test
    public void testBasicPutGet() throws IOException {
        Txn t = txnManager.beginTransaction();
        DataBlock<Integer, Long> db = dsFactory.newDataBlock(t);
        db.put(123, 123L, t);
        t.commit();

        // time goes by
        DataBlock<Integer, Long> db2 = dsFactory.loadDataBlock(db.getId());
        assertEquals(Long.valueOf(123), db2.get(123));
        assertNull(db2.get(125));
    }

    @Test(expected = IOException.class)
    public void testDoubleCommit() throws IOException {
        Txn t = txnManager.beginTransaction();
        DataBlock<Integer, Long> db = dsFactory.newDataBlock(t);
        db.put(123, 123L, t);

        try {
            t.commit();
        } catch (Exception xcp) {
            fail(xcp.getMessage());
        }

        t.commit();
    }

    @Test
    public void testPutGetDelete() throws IOException {
        Txn t = txnManager.beginTransaction();
        DataBlock<Integer, Long> db = dsFactory.newDataBlock(t);
        db.put(123, 123L, t);
        db.put(124, 124L, t);
        db.put(125, 125L, t);
        t.commit();

        DataBlock<Integer, Long> db2 = dsFactory.loadDataBlock(db.getId());
        assertEquals(Long.valueOf(125), db2.get(125));
        assertEquals(Long.valueOf(123), db2.get(123));
        assertEquals(Long.valueOf(124), db2.get(124));
        assertNull(db2.get(127));

        t = txnManager.beginTransaction();
        DataBlock<Integer, Long> db3 = dsFactory.loadDataBlockForWrites(db.getId(), t);
        db3.delete(123, t);
        t.commit();

        DataBlock<Integer, Long> db4 = dsFactory.loadDataBlock(db.getId());
        assertEquals(Long.valueOf(125), db4.get(125));
        assertNull(db4.get(123));
        assertEquals(Long.valueOf(124), db4.get(124));
        assertNull(db4.get(127));
    }

    @Test
    public void testPutIfPossibleGetDelete() throws IOException {
        Txn t = txnManager.beginTransaction();
        DataBlock<Integer, Long> db = dsFactory.newDataBlock(t);
        db.putIfPossible(123, 123L, t);
        db.putIfPossible(124, 124L, t);
        db.putIfPossible(125, 125L, t);
        t.commit();

        DataBlock<Integer, Long> db2 = dsFactory.loadDataBlock(db.getId());
        assertEquals(Long.valueOf(125), db2.get(125));
        assertEquals(Long.valueOf(123), db2.get(123));
        assertEquals(Long.valueOf(124), db2.get(124));
        assertNull(db2.get(127));

        t = txnManager.beginTransaction();
        DataBlock<Integer, Long> db3 = dsFactory.loadDataBlockForWrites(db.getId(), t);
        db3.delete(123, t);
        t.commit();

        DataBlock<Integer, Long> db4 = dsFactory.loadDataBlock(db.getId());
        assertEquals(Long.valueOf(125), db4.get(125));
        assertNull(db4.get(123));
        assertEquals(Long.valueOf(124), db4.get(124));
        assertNull(db4.get(127));
    }

    @Test
    public void testUUIDSerialization() throws IOException {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        UUID uuid3 = UUID.randomUUID();

        Txn t = txnManager.beginTransaction();
        DataBlock<UUID, String> db = dsFactory.newDataBlock(t);
        db.put(uuid1, uuid1.toString(), t);
        db.put(uuid3, uuid3.toString(), t);
        db.put(uuid2, uuid2.toString(), t);
        t.commit();

        DataBlock<UUID, String> db2 = dsFactory.loadDataBlock(db.getId());
        assertEquals(uuid3.toString(), db2.get(uuid3));
        assertEquals(uuid2.toString(), db2.get(uuid2));
        assertEquals(uuid1.toString(), db2.get(uuid1));
    }

    @Test
    public void testEmpty() throws IOException {
        Txn t = txnManager.beginTransaction();
        DataBlock<UUID, String> db = dsFactory.newDataBlock(t);
        t.commit();

        DataBlock<UUID, String> db2 = dsFactory.loadDataBlock(db.getId());
        assertNull(db2.get(UUID.randomUUID()));
    }

    @Test(expected = IllegalStateException.class)
    public void testTonsToValues() throws IOException {
        Random r = new Random();
        Txn t = txnManager.beginTransaction();
        DataBlock<String, Long> db = dsFactory.newDataBlock(t);

        for (int i = 0; i < 10000; i++) {
            db.put(UUID.randomUUID().toString(), r.nextLong(), t);
        }

        t.commit();
    }

    @Test
    public void testOverflow() throws IOException {
        Set<String> keys = new HashSet<>();
        Set<Long> values = new HashSet<>();
        Random r = new Random();

        Txn txn = txnManager.beginTransaction();
        DataBlock<String, Long> db = dsFactory.newDataBlock(txn);
        String key;
        Long value;
        int i = 0;
        boolean shouldDoIt;
        do {
            key = "key_" + i++;
            value = r.nextLong();
            shouldDoIt = db.putIfPossible(key, value, txn);
            if (shouldDoIt) {
                keys.add(key);
                values.add(value);
            }
        } while (shouldDoIt);

        txn.commit();

        DataBlock<String, Long> db2 = dsFactory.loadDataBlock(db.getId());
        db2.keys().forEach(k -> {
            assertTrue(keys.remove(k));
            assertTrue(values.remove(db2.get(k)));
        });

        assertTrue(keys.isEmpty());
        assertTrue(values.isEmpty());
    }

    @Test
    public void testUnderflow() throws IOException {
        Set<String> keys = new HashSet<>();
        Set<Long> values = new HashSet<>();
        Random r = new Random();

        Txn txn = txnManager.beginTransaction();
        DataBlock<String, Long> db = dsFactory.newDataBlock(txn);
        String key;
        Long value;
        int i = 0;
        boolean shouldDoIt;
        do {
            key = "key_" + i++;
            value = r.nextLong();
            shouldDoIt = db.putIfPossible(key, value, txn);
            if (shouldDoIt) {
                keys.add(key);
                values.add(value);
            }
        } while (shouldDoIt && i < 7000);

        txn.commit();

        DataBlock<String, Long> db2 = dsFactory.loadDataBlock(db.getId());
        db2.keys().forEach(k -> {
            assertTrue(keys.remove(k));
            assertTrue(values.remove(db2.get(k)));
        });
    }

    @Test
    public void testConcurrentTransactionsDifferentBlocks() throws IOException {
        Random r = new Random();
        Txn txn = txnManager.beginTransaction();
        DataBlock<String, Long> db1 = dsFactory.newDataBlock(txn);
        DataBlock<String, Long> db2 = dsFactory.newDataBlock(txn);
        txn.commit();

        Txn txn1 = txnManager.beginTransaction();
        Txn txn2 = txnManager.beginTransaction();

        db1.put(UUID.randomUUID().toString(), r.nextLong(), txn1);
        db2.put(UUID.randomUUID().toString(), r.nextLong(), txn2);

        txn1.commit();
        txn2.commit();
    }

    @Test
    public void testTransactionCanLockSameBlockAfterRelease() throws IOException {
        Random r = new Random();
        Txn txn = txnManager.beginTransaction();
        DataBlock<String, Long> db = dsFactory.newDataBlock(txn);
        txn.commit();

        Txn txn1 = txnManager.beginTransaction();
        db.put(UUID.randomUUID().toString(), r.nextLong(), txn1);
        txn1.commit();

        Txn txn2 = txnManager.beginTransaction();
        DataBlock<String, Long> db2 = dsFactory.loadDataBlockForWrites(db.getId(), txn2);
        db2.put(UUID.randomUUID().toString(), r.nextLong(), txn2);
        txn2.commit();
    }

    @Test(expected = IllegalStateException.class)
    public void testNewDataBlockRollback() throws IOException {
        Random r = new Random();
        Txn txn = txnManager.beginTransaction();
        DataBlock<String, Long> db = dsFactory.newDataBlock(txn);
        long blockId = db.getId();
        db.put(UUID.randomUUID().toString(), r.nextLong(), txn);
        txn.rollback();

        // this call will throw
        dsFactory.loadDataBlock(blockId);
    }

    @Test
    public void testSingleCommits() throws IOException {
        Set<String> keys = new HashSet<>();
        Set<Long> values = new HashSet<>();
        Random r = new Random();

        Txn creationTxn = txnManager.beginTransaction();
        DataBlock<String, Long> db = dsFactory.newDataBlock(creationTxn);
        creationTxn.commit();
        String key;
        Long value;
        int i = 0;
        boolean shouldDoIt;
        do {
            Txn txn = txnManager.beginTransaction();
            DataBlock<String, Long> db2 = dsFactory.loadDataBlockForWrites(db.getId(), txn);
            key = "key_" + i++;
            value = r.nextLong();
            shouldDoIt = db2.putIfPossible(key, value, txn);
            if (shouldDoIt) {
                keys.add(key);
                values.add(value);
                txn.commit();
            } else {
                txn.rollback();
            }
        } while (shouldDoIt && i < 7000);

        DataBlock<String, Long> db3 = dsFactory.loadDataBlock(db.getId());
        db3.keys().forEach(k -> {
            assertTrue(keys.remove(k));
            assertTrue(values.remove(db3.get(k)));
        });
    }

    @Test
    public void testDeserializationEdgeCasePlainEmptyStream() throws IOException {
        byte[] uncompressedToRead = new byte[] {
                2, -10, 1, 9, -10, 1
        };

        DataBlock<String, Long> db = new DataBlock<>(store, -99, false);
        try (DataStructure.SerializerInputStream in = new DataStructure.SerializerInputStream(new ByteArrayInputStream(uncompressedToRead))) {
            // this used to throw in case the stream was plain empty
            // the reason is that kryo internally *might* write down a integer as type specifier
            // and that type specifier needs to be present even for null values
            db.deserialize(in);
        }
    }

    @Test
    public void testDeserializationEdgeCaseNullKeyButNoValue() throws IOException {
        byte[] uncompressedToRead = new byte[] {
                2, -10, 1, 9, -10, 1, 0
        };

        DataBlock<String, Long> db = new DataBlock<>(store, -99, false);
        try (DataStructure.SerializerInputStream in = new DataStructure.SerializerInputStream(new ByteArrayInputStream(uncompressedToRead))) {
            // this used to throw
            db.deserialize(in);
        }
    }
}
