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

}
