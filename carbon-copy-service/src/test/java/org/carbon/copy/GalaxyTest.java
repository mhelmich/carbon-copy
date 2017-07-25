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

package org.carbon.copy;

import co.paralleluniverse.galaxy.Grid;
import co.paralleluniverse.galaxy.ItemState;
import co.paralleluniverse.galaxy.StoreTransaction;
import co.paralleluniverse.galaxy.TimeoutException;
import com.google.common.util.concurrent.ListenableFuture;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GalaxyTest {
    private static final String PEER_XML = "../config/peer.xml";
    private static final String PEER_PROPS = "../config/peer.properties";

    @BeforeClass
    public static void initGalaxy() throws InterruptedException {
        Grid.getInstance(PEER_XML, PEER_PROPS).goOnline();
    }

    @Test
    public void testBasic() throws InterruptedException, TimeoutException {
        Random r = new Random();
        byte[] bites = new byte[32768];
        r.nextBytes(bites);
        Grid g = Grid.getInstance(PEER_XML, PEER_PROPS);
        StoreTransaction txn = g.store().beginTransaction();
        long id = g.store().put(bites, txn);
        g.store().commit(txn);
        byte[] readBites = g.store().get(id);
        assertTrue(Arrays.equals(bites, readBites));
    }

    @Test
    public void testPutAsync() throws InterruptedException, ExecutionException {
        Random r = new Random();
        byte[] bites = new byte[32768];
        r.nextBytes(bites);
        Grid g = Grid.getInstance(PEER_XML, PEER_PROPS);
        StoreTransaction txn = g.store().beginTransaction();
        ListenableFuture<Long> f = g.store().putAsync(bites, txn);
        Long id = f.get();
        assertEquals(-1, g.store().getVersion(1901L));
        assertEquals(1, g.store().getVersion(id));
        g.store().commit(txn);
        assertEquals(1, g.store().getVersion(id));

        txn = g.store().beginTransaction();
        ListenableFuture<Void> f2 = g.store().delAsync(id, txn);
        f2.get();
        assertEquals(1, g.store().getVersion(id));
        g.store().commit(txn);
        assertEquals(1, g.store().getVersion(id));
    }

    @Test
    public void testCreateDeleteInSameTxn() throws InterruptedException, ExecutionException {
        Random r = new Random();
        byte[] bites = new byte[32768];
        r.nextBytes(bites);
        Grid g = Grid.getInstance(PEER_XML, PEER_PROPS);
        StoreTransaction txn = g.store().beginTransaction();
        ListenableFuture<Long> f = g.store().putAsync(bites, txn);
        Long id = f.get();
        ListenableFuture<Void> f2 = g.store().delAsync(id, txn);
        f2.get();
        g.store().commit(txn);
    }

    @Test(expected = IllegalStateException.class)
    public void testTwoTransactionsNotPinnedException() throws InterruptedException, TimeoutException {
        Random r = new Random();
        byte[] originalBites = new byte[32768];
        r.nextBytes(originalBites);
        byte[] bites1 = new byte[32768];
        r.nextBytes(bites1);
        byte[] bites2 = new byte[32768];
        r.nextBytes(bites2);

        Grid g = Grid.getInstance(PEER_XML, PEER_PROPS);
        StoreTransaction puttingTxn = g.store().beginTransaction();

        long blockId = g.store().put(originalBites, puttingTxn);

        StoreTransaction txn1 = g.store().beginTransaction();
        StoreTransaction txn2 = g.store().beginTransaction();

        g.store().getx(blockId, txn1);
        g.store().getx(blockId, txn2);

        g.store().set(blockId, bites1, txn1);
        g.store().set(blockId, bites2, txn2);

        g.store().commit(txn1);
        // this should fail
        // the two transactions trample on each others feet because
        // the cache (the underlying galaxy class) keeps track of blocks by id
        // and in this case we
        // 1. pinned a block on this machine
        // 2. committed the first transaction -> we unpinned the block
        // 3. committing the second transaction and as prerequisite the cache checks whether the block is pinned ... which it isn't
        g.store().commit(txn2);
    }

    @Test
    public void testAsyncStoreMethods() throws InterruptedException, java.util.concurrent.TimeoutException, ExecutionException {
        Random r = new Random();
        byte[] bites = new byte[32768];
        r.nextBytes(bites);
        Grid g = Grid.getInstance(PEER_XML, PEER_PROPS);
        ListenableFuture<Long> f = g.store().putAsync(bites, null);
        long id = f.get(5, TimeUnit.SECONDS);
        assertTrue(id > 0);
        g.store().release(id);

        byte[] newBites = new byte[32768];
        r.nextBytes(newBites);

        boolean isPinned = g.store().tryPin(id, ItemState.OWNED, null);
        assertTrue(isPinned);
        ListenableFuture<Void> f2 = g.store().setAsync(id, newBites, null);
        g.store().release(id);

        Void v = f2.get(5, TimeUnit.SECONDS);
        ListenableFuture<byte[]> f3 = g.store().getAsync(id);
        byte[] readBites = f3.get(5, TimeUnit.SECONDS);
        assertTrue(Arrays.equals(newBites, readBites));
    }
}
