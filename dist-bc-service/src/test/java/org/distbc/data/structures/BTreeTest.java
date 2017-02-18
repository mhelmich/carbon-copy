package org.distbc.data.structures;

import co.paralleluniverse.galaxy.Store;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

public class BTreeTest {
    @Test
    public void testBasic() {
        BTree<String, String> t = newTree();
        Txn txn = Mockito.mock(Txn.class);

        t.put("www.cs.princeton.edu", "128.112.136.12", txn);
        t.put("www.cs.princeton.edu", "128.112.136.11", txn);
        t.put("www.princeton.edu",    "128.112.128.15", txn);
        t.put("www.yale.edu",         "130.132.143.21", txn);
        t.put("www.simpsons.com",     "209.052.165.60", txn);
        t.put("www.apple.com",        "17.112.152.32", txn);
        t.put("www.amazon.com",       "207.171.182.16", txn);
        t.put("www.ebay.com",         "66.135.192.87", txn);
        t.put("www.cnn.com",          "64.236.16.20", txn);
        t.put("www.google.com",       "216.239.41.99", txn);
        t.put("www.nytimes.com",      "199.239.136.200", txn);
        t.put("www.microsoft.com",    "207.126.99.140", txn);
        t.put("www.dell.com",         "143.166.224.230", txn);
        t.put("www.slashdot.org",     "66.35.250.151", txn);
        t.put("www.espn.com",         "199.181.135.201", txn);
        t.put("www.weather.com",      "63.111.66.11", txn);
        t.put("www.yahoo.com",        "216.109.118.65", txn);

        assertEquals("63.111.66.11", t.get("www.weather.com"));
        assertEquals("128.112.128.15", t.get("www.princeton.edu"));
    }

    @Test
    public void testRandomIntString() {
        BTree<Integer, String> t = newTree();
        Map<Integer, String> m = new HashMap<>();
        int count = 10000;
        Random r = new Random();
        Txn txn = Mockito.mock(Txn.class);

        for (int i = 0; i < count; i++) {
            int key = r.nextInt();
            String value = UUID.randomUUID().toString();
            t.put(key, value, txn);
            if (r.nextBoolean() && !m.containsKey(key)) m.put(key, value);
        }

        for (Map.Entry<Integer, String> e : m.entrySet()) {
            assertEquals("on key " + e.getKey(), e.getValue(), t.get(e.getKey()));
        }
    }

    @Test
    public void testDups() {
        BTree<Integer, String> t = newTree();
        Txn txn = Mockito.mock(Txn.class);
        t.put(5, "narf_5", txn);
        t.put(5, "narf_6", txn);

        assertEquals("narf_6", t.get(5));
    }

    @Test
    public void testDupsMultipleNodes() {
        BTree<Integer, String> t = newTree();
        Txn txn = Mockito.mock(Txn.class);

        for (int i = 0; i < BTree.MAX_NODE_SIZE * 3; i++) {
            t.put(5, "narf_" + i, txn);
        }

        assertEquals("narf_" + (BTree.MAX_NODE_SIZE * 3 - 1), t.get(5));
    }

    @Test
    public void testDelete() {
        BTree<Integer, String> t = newTree();
        Txn txn = Mockito.mock(Txn.class);
        t.put(5, "narf_5", txn);
        t.put(6, "narf_6", txn);
        t.put(7, "narf_7", txn);

        t.put(6, null, txn);

        assertEquals("narf_5", t.get(5));
        assertNull(t.get(6));
        assertEquals("narf_7", t.get(7));
    }

    @Test
    public void testKeys() {
        int count = 100;
        BTree<Integer, String> t = newTree();
        Txn txn = Mockito.mock(Txn.class);
        for (int i = 0; i < count; i++) {
            t.put(i, "value_" + i, txn);
        }

        int numKeys = 0;
        for (Integer key : t.keys()) {
            assertEquals("value_" + key, t.get(key));
            numKeys++;
        }

        assertEquals(count, numKeys);
    }

    private <Key extends Comparable<Key>, Value> BTree<Key, Value> newTree() {
        Txn txn = Mockito.mock(Txn.class);
        when(txn.getStoreTransaction()).thenReturn(null);
        Store s = Mockito.mock(Store.class);
        return new BTree<>(s, new DataStructureFactoryImpl(s), txn);
    }
}
