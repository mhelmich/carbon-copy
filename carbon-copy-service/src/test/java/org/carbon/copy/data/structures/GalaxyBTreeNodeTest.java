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

package org.carbon.copy.data.structures;

import com.google.inject.Inject;
import org.carbon.copy.GuiceJUnit4Runner;
import org.carbon.copy.GuiceModules;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;

@RunWith(GuiceJUnit4Runner.class)
@GuiceModules({ DataStructureModule.class, TxnManagerModule.class })
public class GalaxyBTreeNodeTest {
    @Inject
    private InternalDataStructureFactory dsFactory;

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
