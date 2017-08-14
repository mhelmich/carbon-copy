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

import co.paralleluniverse.galaxy.Cluster;
import co.paralleluniverse.galaxy.Messenger;
import co.paralleluniverse.galaxy.Store;
import com.google.inject.Inject;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class GalaxyDistHashTest extends GalaxyBaseTest {
    private static Logger logger = LoggerFactory.getLogger(GalaxyDistHashTest.class);

    @Inject
    private InternalDataStructureFactory dsFactory;

    @Inject
    private TxnManager txnManager;

    @Inject
    private Store store;

    @Inject
    private Cluster cluster;

    @Inject
    private Messenger messenger;

    @Test
    public void testPuttingValues() throws Exception {
        String str = UUID.randomUUID().toString();

        Txn txn = txnManager.beginTransaction();
        DistHash<Integer, String> dh = dsFactory.newDistHash(txn);
        dh.put(123, str, txn);
        txn.commit();

        String receivedStr = dh.get(123);
        assertEquals(str, receivedStr);
    }

    @Test
    public void testPuttingValuesWithReloading() throws Exception {
        String str = UUID.randomUUID().toString();

        Txn txn = txnManager.beginTransaction();
        DistHash<Integer, String> dh = dsFactory.newDistHash(txn);
        dh.put(123, str, txn);
        txn.commit();

        DistHash<Integer, String> dh2 = dsFactory.loadDistHash(dh.getId());
        String receivedStr = dh2.get(123);
        assertEquals(str, receivedStr);
    }

    @Test
    public void testNullKey() throws IOException {
        String str = UUID.randomUUID().toString();

        Txn txn = txnManager.beginTransaction();
        DistHash<Integer, String> dh = dsFactory.newDistHash(txn);
        dh.put(123, str, txn);
        txn.commit();

        String receivedStr = dh.get(456);
        assertNull(receivedStr);
    }

    @Test
    @Ignore
    public void testListAllKeys() throws IOException {
        Set<String> keys = new HashSet<>();
        Set<Long> values = new HashSet<>();
        Random r = new Random();

        Txn txn = txnManager.beginTransaction();
        DistHash<String, Long> dh = dsFactory.newDistHash(txn);
        for (int i = 0; i < 10000; i++) {
            String key = "key_" + i;
            Long value = r.nextLong();
            dh.put(key, value, txn);
            keys.add(key);
            values.add(value);
        }

        txn.commit();

        dh.keys().forEach(key -> {
            assertTrue(keys.remove(key));
            assertTrue(values.remove(dh.get(key)));
        });

        assertTrue(keys.isEmpty());
        assertTrue(values.isEmpty());
    }

    @Test(expected = IllegalStateException.class)
    public void testNewDistHashRollback() throws IOException {
        Random r = new Random();
        Txn txn = txnManager.beginTransaction();
        DistHash<String, Long> db = dsFactory.newDistHash(txn);
        long blockId = db.getId();
        db.put(UUID.randomUUID().toString(), r.nextLong(), txn);
        txn.rollback();

        // this call will throw
        dsFactory.loadDistHash(blockId);
    }

    @Test
    public void testMultiThreadedPuttingAndGetting() throws IOException, InterruptedException {
        Random r = new Random();
        Txn txn = txnManager.beginTransaction();
        DistHash<Long, String> dh = dsFactory.newDistHash(txn);
        long blockId = dh.getId();
        long originalKey = r.nextLong();
        dh.put(originalKey, UUID.randomUUID().toString(), txn);
        txn.commit();

        String originalValue = dsFactory.<Long, String>loadDistHash(blockId).get(originalKey);
        assertNotNull(originalValue);

        int count = 5;
        ExecutorService es = Executors.newFixedThreadPool(count);
        List<Future<Set<Long>>> futureList = new LinkedList<>();
        for (int i = 0; i < count; i++) {
            Future<Set<Long>> f = es.submit(new PutterGetter(blockId, dsFactory, txnManager));
            futureList.add(f);
        }

        es.shutdown();
        es.awaitTermination(10, TimeUnit.SECONDS);

        Set<Long> allKeys = new HashSet<>();
        futureList.forEach(f -> {
            try {
                allKeys.addAll(f.get());
            } catch (InterruptedException | ExecutionException e) {
                fail();
            }
        });

        DistHash<Long, String> dh2 = dsFactory.loadDistHash(blockId);
        allKeys.forEach(k -> assertNotNull(dh2.get(k)));
    }

    private static class PutterGetter implements Callable<Set<Long>> {
        private final Random rand = new Random();
        private final long distHashId;
        private final InternalDataStructureFactory dsFactory;
        private final TxnManager txnManager;

        PutterGetter(long distHashId, InternalDataStructureFactory dsFactory, TxnManager txnManager) {
            this.distHashId = distHashId;
            this.dsFactory = dsFactory;
            this.txnManager = txnManager;
        }

        @Override
        public Set<Long> call() throws Exception {
            Txn txn = txnManager.beginTransaction();
            DistHash<Long, String> dhRW = dsFactory.loadDistHashForWrites(distHashId, txn);
            int count = 47;
            Set<Long> putValues = new HashSet<>();
            for (int i = 0; i < count; i++) {
                long key = rand.nextLong();
                putValues.add(key);
                dhRW.put(key, UUID.randomUUID().toString(), txn);
                logger.info("put {}", key);
            }

            try {
                txn.commit();
            } catch (IOException e) {
                logger.error("Couldn't commit txn", e);
            }

            logger.info("Thread {} committed txn {}", Thread.currentThread().getName(), txn);

            DistHash<Long, String> dhR = dsFactory.loadDistHash(distHashId);
            putValues.forEach(l ->
                    assertNotNull(dhR.get(l))
            );

            return putValues;
        }
    }
}
