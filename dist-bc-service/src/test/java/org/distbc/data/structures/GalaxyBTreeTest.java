package org.distbc.data.structures;

import com.google.inject.Inject;
import org.distbc.GuiceJUnit4Runner;
import org.distbc.GuiceModules;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

@RunWith(GuiceJUnit4Runner.class)
@GuiceModules({ DataStructureModule.class, TxnManagerModule.class })
public class GalaxyBTreeTest {
    @Inject
    private DataStructureFactory dsFactory;

    @Inject
    private TxnManager txnManager;

    @Test
    public void testBasic() throws IOException {
        long treeId;
        Txn txn = txnManager.beginTransaction();
        BTree<String, String> t = dsFactory.newBTree(txn);
        t.put("key_1", "value_1", txn);
        t.put("key_2", "value_2", txn);
        t.put("key_3", "value_3", txn);
        treeId = t.getId();
        txn.commit();

        BTree<String, String> t2 = dsFactory.loadBTree(treeId);
        assertEquals("value_2", t2.get("key_2"));
        assertEquals("value_3", t2.get("key_3"));
        assertEquals("value_1", t2.get("key_1"));
    }
}
