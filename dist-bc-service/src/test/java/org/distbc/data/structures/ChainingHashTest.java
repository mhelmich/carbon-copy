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

package org.distbc.data.structures;

import co.paralleluniverse.galaxy.Store;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class ChainingHashTest {
    @Test
    public void testBasic() throws IOException {
        Map<String, String> m = new HashMap<>();
        Txn txn = Mockito.mock(Txn.class);
        when(txn.getStoreTransaction()).thenReturn(null);
        ChainingHash<String, String> h = newChainingHash(txn);

        // this needs to be
        int count = ChainingHash.DEFAULT_NUM_BUCKETS * 3;
        for (int i = 0; i < count; i++) {
            String key = UUID.randomUUID().toString();
            String value = UUID.randomUUID().toString();
            m.put(key, value);
            h.put(key, value, txn);
        }

        m.entrySet().forEach(e -> assertEquals(e.getValue(), h.get(e.getKey())));
    }

    @Test
    public void testResize() throws IOException {
        Map<String, String> m = new HashMap<>();
        Store s = Mockito.mock(Store.class);
        Txn txn = Mockito.mock(Txn.class);
        when(txn.getStoreTransaction()).thenReturn(null);
        ChainingHash<String, String> h = new ChainingHash<String, String>(s, new DataStructureFactoryImpl(s), txn) {
            @Override
            DataBlock<String, String> newDataBlock(Txn txn) {
                return new DataBlock<String, String>(s, txn) {
                    @Override
                    int getMaxByteSize() {
                        return 8;
                    }
                };
            }
        };

        int count = ChainingHash.DEFAULT_NUM_BUCKETS * 11;
        for (int i = 0; i < count; i++) {
            String key = UUID.randomUUID().toString();
            String value = UUID.randomUUID().toString();
            m.put(key, value);
            h.put(key, value, txn);
        }

        m.entrySet().forEach(e -> assertEquals(e.getValue(), h.get(e.getKey())));
    }

    @Test
    public void testDelete() throws IOException {
        Map<String, String> m = new HashMap<>();
        Txn txn = Mockito.mock(Txn.class);
        when(txn.getStoreTransaction()).thenReturn(null);
        ChainingHash<String, String> h = newChainingHash(txn);

        String key = UUID.randomUUID().toString();
        String value = UUID.randomUUID().toString();

        m.put(key, value);
        h.put(key, value, txn);

        m.put("12", "12");
        h.put("12", "12", txn);

        key = UUID.randomUUID().toString();
        value = UUID.randomUUID().toString();

        m.put(key, value);
        h.put(key, value, txn);

        m.put("13", "13");
        h.put("13", "13", txn);

        key = UUID.randomUUID().toString();
        value = UUID.randomUUID().toString();

        m.put(key, value);
        h.put(key, value, txn);

        int count = ChainingHash.DEFAULT_NUM_BUCKETS * 2;
        for (int i = 0; i < count; i++) {
            key = UUID.randomUUID().toString();
            value = UUID.randomUUID().toString();
            m.put(key, value);
            h.put(key, value, txn);
        }

        m.entrySet().forEach(e -> assertEquals(e.getValue(), h.get(e.getKey())));

        m.remove("13");
        h.delete("13", txn);
        m.entrySet().forEach(e -> assertEquals(e.getValue(), h.get(e.getKey())));

        m.remove("12");
        h.delete("12", txn);
        m.entrySet().forEach(e -> assertEquals(e.getValue(), h.get(e.getKey())));

        h.delete("12", txn);
        m.entrySet().forEach(e -> assertEquals(e.getValue(), h.get(e.getKey())));
    }

    @Test
    public void testKeys() throws IOException {
        Map<String, String> m = new HashMap<>();
        Txn txn = Mockito.mock(Txn.class);
        when(txn.getStoreTransaction()).thenReturn(null);
        ChainingHash<String, String> h = newChainingHash(txn);

        // this needs to be
        int count = ChainingHash.DEFAULT_NUM_BUCKETS * 3;
        for (int i = 0; i < count; i++) {
            String key = UUID.randomUUID().toString();
            String value = UUID.randomUUID().toString();
            m.put(key, value);
            h.put(key, value, txn);
        }

        for (String s : h.keys()) {
            assertEquals(m.get(s), h.get(s));
        }
    }

    @Test
    public void testKeysWithLastBucketBeingNull() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Map<String, String> m = new HashMap<>();
        Txn txn = Mockito.mock(Txn.class);
        when(txn.getStoreTransaction()).thenReturn(null);
        ChainingHash<String, String> h = newChainingHash(txn);

        for (int i = 0; i < 7; i++) {
            String value = getValueForNotLastBucket();
            m.put(value, value);
            h.put(value, value, txn);
        }

        int count = 0;
        for (String k : h.keys()) {
            assertTrue(m.containsKey(k));
            count++;
        }

        assertEquals(7, count);
    }

    @SuppressWarnings("unused")
    @Test
    public void testEmptyHash() {
        Txn txn = Mockito.mock(Txn.class);
        when(txn.getStoreTransaction()).thenReturn(null);
        ChainingHash<String, String> h = newChainingHash(txn);

        int count = 0;
        for (String k : h.keys()) {
            count++;
        }

        assertEquals(0, count);
    }

    private <Key extends Comparable<Key>, Value> ChainingHash<Key, Value> newChainingHash(Txn txn) {
        Store s = Mockito.mock(Store.class);
        return new ChainingHash<>(s, new DataStructureFactoryImpl(s), txn);
    }

    private String getValueForNotLastBucket() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        int count = 27;
        int i = 0;
        do {
            String value = UUID.randomUUID().toString();
            int bucket = getHashForValue(value);
            if (bucket != 4) {
                return value;
            }
            i++;
        } while (i < count);
        return null;
    }

    private <Key extends Comparable<Key>, Value> int getHashForValue(Key k) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Txn txn = Mockito.mock(Txn.class);
        when(txn.getStoreTransaction()).thenReturn(null);
        ChainingHash<Key, Value> h = newChainingHash(txn);
        Method m = h.getClass().getDeclaredMethod("hash", Comparable.class);
        m.setAccessible(true);
        return (Integer) m.invoke(h, k);
    }
}
