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
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class GalaxyDistHashTest extends GalaxyBaseTest {
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
    public void testListAllKeys() throws IOException {
        Set<String> keys = new HashSet<>();
        Set<Long> values = new HashSet<>();
        Random r = new Random();

        Txn txn = txnManager.beginTransaction();
        DistHash<String, Long> dh = dsFactory.newDistHash(txn);
        for (int i = 0; i < 10; i++) {
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
}
