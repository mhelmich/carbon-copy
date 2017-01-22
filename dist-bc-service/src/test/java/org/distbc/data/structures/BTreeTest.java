package org.distbc.data.structures;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class BTreeTest {
    @Test
    public void testBasic() {
        BTree<String, String> t = new BTree<>();

        t.put("www.cs.princeton.edu", "128.112.136.12");
        t.put("www.cs.princeton.edu", "128.112.136.11");
        t.put("www.princeton.edu",    "128.112.128.15");
        t.put("www.yale.edu",         "130.132.143.21");
        t.put("www.simpsons.com",     "209.052.165.60");
        t.put("www.apple.com",        "17.112.152.32");
        t.put("www.amazon.com",       "207.171.182.16");
        t.put("www.ebay.com",         "66.135.192.87");
        t.put("www.cnn.com",          "64.236.16.20");
        t.put("www.google.com",       "216.239.41.99");
        t.put("www.nytimes.com",      "199.239.136.200");
        t.put("www.microsoft.com",    "207.126.99.140");
        t.put("www.dell.com",         "143.166.224.230");
        t.put("www.slashdot.org",     "66.35.250.151");
        t.put("www.espn.com",         "199.181.135.201");
        t.put("www.weather.com",      "63.111.66.11");
        t.put("www.yahoo.com",        "216.109.118.65");

        System.err.println(t.toString());

        assertEquals("63.111.66.11", t.get("www.weather.com"));
        assertEquals("128.112.128.15", t.get("www.princeton.edu"));
    }

    @Test
    public void testRandomIntString() {
        BTree<Integer, String> t = new BTree<>();
        Map<Integer, String> m = new HashMap<>();
        int count = 10000;
        Random r = new Random();

        for (int i = 0; i < count; i++) {
            int key = r.nextInt();
            String value = UUID.randomUUID().toString();
            t.put(key, value);
            if (r.nextBoolean() && !m.containsKey(key)) m.put(key, value);
        }

        for (Map.Entry<Integer, String> e : m.entrySet()) {
            assertEquals("on key " + e.getKey(), e.getValue(), t.get(e.getKey()));
        }
    }

    @Test
    public void testDups() {
        BTree<Integer, String> t = new BTree<>();
        t.put(5, "narf_5");
        t.put(5, "narf_6");

        assertEquals("narf_5", t.get(5));
    }

    @Test
    public void testDelete() {
        BTree<Integer, String> t = new BTree<>();
        t.put(5, "narf_5");
        t.put(6, "narf_6");
        t.put(7, "narf_7");

        t.put(6, null);

        assertEquals("narf_5", t.get(5));
        assertNull(t.get(6));
        assertEquals("narf_7", t.get(7));
    }
}
