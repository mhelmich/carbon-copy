package org.distbc.data.structures.SibPlusTree;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class SibPlusTreeTest {

    private static final Logger logger = Logger.getLogger(SibPlusTreeTest.class);

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

    @Test
    @Ignore
    public void testSplit() throws Exception {
        SibPlusTree tree = new SibPlusTree();
        int count = 2000;
        List<String> values = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            String value = UUID.randomUUID().toString();
            values.add(value);
            logger.info("inserting object #" + i);
            tree.put(i, value);
//            assertEquals(value, tree.search(i));
        }

        assertEquals(count, values.size());
    }

}
