package org.distbc.data.structures;

import org.junit.Test;

import java.util.UUID;

/**
 * Created by mhelmich on 10/12/16.
 */
public class SPTTest {

    @Test
    public void testBasic() throws Exception {
        SPT tree = new SPT();
        int key = 13;
        String value = UUID.randomUUID().toString();
        tree.insert(key, value);
    }

}
