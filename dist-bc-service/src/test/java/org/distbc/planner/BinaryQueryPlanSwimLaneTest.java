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

package org.distbc.planner;

import com.google.inject.Inject;
import org.distbc.GuiceJUnit4Runner;
import org.distbc.GuiceModules;
import org.distbc.data.structures.DataStructureModule;
import org.distbc.data.structures.InternalDataStructureFactory;
import org.distbc.data.structures.TempTable;
import org.distbc.data.structures.Tuple;
import org.distbc.data.structures.Txn;
import org.distbc.data.structures.TxnManager;
import org.distbc.data.structures.TxnManagerModule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@RunWith(GuiceJUnit4Runner.class)
@GuiceModules({ DataStructureModule.class, TxnManagerModule.class })
public class BinaryQueryPlanSwimLaneTest {
    @Inject
    private InternalDataStructureFactory dsFactory;

    @Inject
    private TxnManager txnManager;

    @Test
    public void testBasic() throws Exception {
        TempTable tt1 = createDummyTempTable("t1", 2, 3, 5);
        TempTable tt2 = createDummyTempTable("t2", 1, 3, 6);

        BinaryQueryPlanSwimLane sl = new BinaryQueryPlanSwimLane(dsFactory, txnManager, tt1, 0, tt2, 0);
        TempTable result = sl.call();
        long numItems = result.keys().count();
        assertEquals(1, numItems);
        List<Tuple> metadata = result.getColumnMetadata();
        assertEquals(6, metadata.size());
        assertEquals("t1.id", metadata.get(0).get(0).toString());
        assertEquals("t1.moep", metadata.get(1).get(0).toString());
        assertEquals("t1.narf", metadata.get(2).get(0).toString());
        assertEquals("t2.id", metadata.get(3).get(0).toString());
        assertEquals("t2.moep", metadata.get(4).get(0).toString());
        assertEquals("t2.narf", metadata.get(5).get(0).toString());
    }

    private TempTable createDummyTempTable(String namePrefix, int... ids) throws IOException {
        Txn txn = txnManager.beginTransaction();
        TempTable.Builder tableBuilder = TempTable.newBuilder()
                .withColumn(namePrefix + ".id", Integer.class)
                .withColumn(namePrefix + ".moep", String.class)
                .withColumn(namePrefix + ".narf", String.class);
        TempTable tt = dsFactory.newTempTable(tableBuilder, txn);

        Tuple tup1 = new Tuple(3);
        tup1.put(0, ids[0]);
        tup1.put(1, "moep_" + UUID.randomUUID().toString());
        tup1.put(2, "tup1_foo");

        Tuple tup2 = new Tuple(3);
        tup2.put(0, ids[1]);
        tup2.put(1, "__moep__" + UUID.randomUUID().toString());
        tup2.put(2, "tup2_foo");

        Tuple tup3 = new Tuple(3);
        tup3.put(0, ids[2]);
        tup3.put(1, "moep_" + UUID.randomUUID().toString());
        tup3.put(2, "tup3_foo");

        tt.insert(tup1, txn);
        tt.insert(tup2, txn);
        tt.insert(tup3, txn);

        txn.commit();

        return tt;
    }
}
