package org.distbc.data.structures.SibPlusTree;

import org.junit.Ignore;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * Created by mhelmich on 10/7/16.
 */
public class SibPlusTreeTest {

    @Test
    @Ignore // for now...
    public void testBasic() throws Exception {
        Integer key = 5;
        String value = UUID.randomUUID().toString();
        SibPlusTree tree = new SibPlusTree();
        tree.insert(key, value);
        String foundValue = tree.search(key);
        assertEquals(value, foundValue);
    }

}
