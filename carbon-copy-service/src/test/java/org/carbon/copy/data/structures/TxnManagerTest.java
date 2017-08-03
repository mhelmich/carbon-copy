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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TxnManagerTest extends GalaxyBaseTest {

    @Inject
    private Store store;

    @Inject
    private InternalDataStructureFactory dsFactory;

    @Inject
    private TxnManager txnManager;

    @Test
    public void testBasicTransaction() throws Exception {
        int count = 100;
        Map<String, String> map = new HashMap<>();
        AtomicLong id = new AtomicLong(-1L);

        txnManager.doTransactionally(txn -> {
            DataBlock<String, String> db = dsFactory.newDataBlock(txn);
            for (int i = 0; i < count; i++) {
                String key = UUID.randomUUID().toString();
                String value = UUID.randomUUID().toString();

                map.put(key, value);
                db.put(key, value, txn);
                id.set(db.getId());
            }
        });

        DataBlock<String, String> db = dsFactory.loadDataBlock(id.get());
        map.forEach((key, value) -> assertEquals(value, db.get(key)));
    }

    @Test(expected = IOException.class)
    public void testChangingExistingHash() throws Exception {
        AtomicLong id = new AtomicLong(-1L);

        txnManager.doTransactionally(txn -> {
            DataBlock<Integer, String> db = dsFactory.newDataBlock(txn);
            for (int i = 0; i < 10; i++) {
                String value = UUID.randomUUID().toString();

                db.put(i, value, txn);
                id.set(db.getId());
            }
            throw new RuntimeException("BOOOOM -- this has been planted for your test");
        });

        DataBlock<Integer, String> db = dsFactory.loadDataBlock(id.get());
        assertNull(db.get(7));
        assertNull(db.get(3));
        assertNull(db.get(5));
    }

    @Test
    public void testLocalLocksDoubleLockingSameThread() throws IOException {
        long dbId = createNewDataBlockAndGetId();
        TxnManagerImpl impl = (TxnManagerImpl) txnManager;

        impl.lock(dbId);
        impl.lock(dbId);
    }

    @Test
    public void testLocalLocksDoubleLockingDifferentThreads() throws IOException, InterruptedException {
        long dbId = createNewDataBlockAndGetId();
        TxnManagerImpl impl = (TxnManagerImpl) txnManager;
        ExecutorService es = Executors.newFixedThreadPool(1);

        FailingToLockCallable callable = new FailingToLockCallable(dbId, impl);

        try {
            impl.lock(dbId);
            es.submit(callable);
            callable.exceptionLatch.await(60, TimeUnit.SECONDS);
        } finally {
            es.shutdown();
        }
    }

    private static class FailingToLockCallable implements Callable<Void> {
        private final CountDownLatch exceptionLatch = new CountDownLatch(1);
        private final long blockId;
        private final TxnManagerImpl impl;

        FailingToLockCallable(long blockId, TxnManagerImpl impl) {
            this.blockId = blockId;
            this.impl = impl;
        }

        @Override
        public Void call() throws Exception {
            try {
                impl.lock(blockId);
            } catch (Exception xcp) {
                exceptionLatch.countDown();
            }
            return null;
        }
    }

    @Test
    public void testLocalLocksPassingOnALockDifferentThreads() throws IOException, InterruptedException {
        long dbId = createNewDataBlockAndGetId();
        CountDownLatch addToWaitingListLatch = new CountDownLatch(1);
        TxnManagerImpl impl = new TxnManagerImpl(store) {
            @Override
            protected void waitFor(Future f) throws InterruptedException, ExecutionException, TimeoutException {
                addToWaitingListLatch.countDown();
                super.waitFor(f);
            }
        };
        ExecutorService es = Executors.newFixedThreadPool(1);
        SucceedingToLockCallable succeedingCallable = new SucceedingToLockCallable(dbId, impl);

        try {
            impl.lock(dbId);
            es.submit(succeedingCallable);
            addToWaitingListLatch.await(60, TimeUnit.SECONDS);
            impl.release(dbId);
            assertTrue(succeedingCallable.gotLockLatch.await(60, TimeUnit.SECONDS));
        } finally {
            es.shutdown();
        }

        assertEquals(1, succeedingCallable.exceptionLatch.getCount());
    }

    private static class SucceedingToLockCallable implements Callable<Void> {
        private final CountDownLatch exceptionLatch = new CountDownLatch(1);
        private final CountDownLatch gotLockLatch = new CountDownLatch(1);
        private final long blockId;
        private final TxnManagerImpl impl;

        SucceedingToLockCallable(long blockId, TxnManagerImpl impl) {
            this.blockId = blockId;
            this.impl = impl;
        }

        @Override
        public Void call() throws Exception {
            try {
                impl.lock(blockId);
                gotLockLatch.countDown();
            } catch (Exception xcp) {
                exceptionLatch.countDown();
            }
            return null;
        }
    }

    private DataBlock<Integer, Integer> createNewDataBlock() throws IOException {
        Txn txn = txnManager.beginTransaction();
        DataBlock<Integer, Integer> db = dsFactory.newDataBlock(txn);
        db.put(17, 17, txn);
        txn.commit();
        return db;
    }

    private long createNewDataBlockAndGetId() throws IOException {
        return createNewDataBlock().getId();
    }
}
