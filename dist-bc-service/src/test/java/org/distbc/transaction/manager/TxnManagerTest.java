package org.distbc.transaction.manager;

import com.google.inject.Inject;
import org.distbc.GuiceJUnit4Runner;
import org.distbc.GuiceModules;
import org.distbc.data.structures.ChainingHash;
import org.distbc.data.structures.DataStructureFactory;
import org.distbc.data.structures.DataStructureModule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RunWith(GuiceJUnit4Runner.class)
@GuiceModules({ DataStructureModule.class, TxnManagerModule.class })
public class TxnManagerTest {

    @Inject
    private DataStructureFactory dsFactory;

    @Inject
    private TxnManager txnManager;

    @Test
    public void testBasicTransaction() throws Exception {
        int count = 100;
        Map<String, String> map = new HashMap<>();

        ChainingHash<String, String> hash = dsFactory.newChainingHash();
        txnManager.doTransactionally(txn -> {
            for (int i = 0; i < count; i++) {
                String key = UUID.randomUUID().toString();
                String value = UUID.randomUUID().toString();

                map.put(key, value);
                hash.put(key, value);
            }
        });
    }
}