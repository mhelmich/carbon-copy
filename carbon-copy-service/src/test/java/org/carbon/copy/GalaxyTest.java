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
import co.paralleluniverse.galaxy.StoreTransaction;
import co.paralleluniverse.galaxy.TimeoutException;
import com.google.common.util.concurrent.ListenableFuture;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutionException;

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
}
