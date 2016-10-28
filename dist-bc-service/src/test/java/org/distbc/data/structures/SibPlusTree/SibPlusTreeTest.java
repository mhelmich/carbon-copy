package org.distbc.data.structures.SibPlusTree;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class SibPlusTreeTest {

    @Test
    public void testBasic() throws Exception {
        Integer key = 5;
        String value = UUID.randomUUID().toString();
        SibPlusTree tree = new SibPlusTree();
        tree.put(key, value);
        String foundValue = tree.search(key);
        assertEquals(value, foundValue);
    }

    @Test
    public void testThreePuts() throws Exception {
        SibPlusTree tree = new SibPlusTree();
        Integer key1 = 5;
        String value1 = UUID.randomUUID().toString();
        Integer key2 = 4;
        String value2 = UUID.randomUUID().toString();
        Integer key3 = 3;
        String value3 = UUID.randomUUID().toString();
        tree.put(key1, value1);
        tree.put(key2, value2);
        tree.put(key3, value3);

        assertEquals(value3, tree.search(key3));
        assertEquals(value2, tree.search(key2));
        assertEquals(value1, tree.search(key1));
    }

}
