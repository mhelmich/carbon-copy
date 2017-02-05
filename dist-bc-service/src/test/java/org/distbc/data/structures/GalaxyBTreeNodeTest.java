package org.distbc.data.structures;

import co.paralleluniverse.galaxy.Store;
import com.google.inject.Inject;
import org.distbc.GuiceJUnit4Runner;
import org.distbc.GuiceModules;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;

@RunWith(GuiceJUnit4Runner.class)
@GuiceModules({ DataStructureModule.class, TxnManagerModule.class })
public class GalaxyBTreeNodeTest {
    @Inject
    private Store store;

    @Inject
    private DataStructureFactory dsFactory;

    @Inject
    private TxnManager txnManager;

    @Test
    public void testBasicPutGet() throws Exception {
        AtomicLong id = new AtomicLong(-1);
        Txn txn = txnManager.beginTransaction();
        BTreeNode<String, String> node1 = dsFactory.newBTreeNode(3, txn);
        node1.setEntryAt(0, new BTreeEntry<>("key_key_key", "12345__ABCDE"), txn);
        node1.setEntryAt(1, new BTreeEntry<>("some_key", "booooyeah"), txn);
        node1.setEntryAt(2, new BTreeEntry<>("one_more_key", "testing_test"), txn);
        txn.commit();
        id.set(node1.getId());

        // tic toc tic toc
        BTreeNode<String, String> node2 = dsFactory.loadBTreeNode(id.get());
        assertEquals(3, node2.getNumChildren());
        assertEquals("key_key_key", node2.getEntryAt(0).getKey());
        assertEquals("12345__ABCDE", node2.getEntryAt(0).getValue());
        assertEquals("some_key", node2.getEntryAt(1).getKey());
        assertEquals("booooyeah", node2.getEntryAt(1).getValue());
        assertEquals("one_more_key", node2.getEntryAt(2).getKey());
        assertEquals("testing_test", node2.getEntryAt(2).getValue());
    }
}
