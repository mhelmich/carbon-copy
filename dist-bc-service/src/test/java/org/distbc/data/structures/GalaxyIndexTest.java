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

package org.distbc.data.structures;

import com.google.inject.Inject;
import org.distbc.GuiceJUnit4Runner;
import org.distbc.GuiceModules;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@RunWith(GuiceJUnit4Runner.class)
@GuiceModules({ DataStructureModule.class, TxnManagerModule.class })
public class GalaxyIndexTest {
    @Inject
    private InternalDataStructureFactory dsFactory;

    @Inject
    private TxnManager txnManager;

    @Test
    public void testBasic() throws IOException {
        List<GUID> assertionGUIDs = new ArrayList<>();
        Txn txn = txnManager.beginTransaction();

        Index.Builder indexBuilder = Index.newBuilder("narf")
                .withColumn("number", Integer.class)
                .withColumn("foo", String.class);

        Index idx = dsFactory.newIndex(indexBuilder, txn);

        Tuple tup1 = Tuple.builder()
                .add(123)
                .add("bbb__" + UUID.randomUUID().toString())
                .build();
        assertionGUIDs.add(GUID.randomGUID());

        Tuple tup2 = Tuple.builder()
                .add(456)
                .add("aaa__" + UUID.randomUUID().toString())
                .build();
        assertionGUIDs.add(GUID.randomGUID());

        Tuple tup3 = Tuple.builder()
                .add(789)
                .add("ccc__" + UUID.randomUUID().toString())
                .build();
        assertionGUIDs.add(GUID.randomGUID());

        idx.insert(tup1, assertionGUIDs.get(0), txn);
        idx.insert(tup2, assertionGUIDs.get(1), txn);
        idx.insert(tup3, assertionGUIDs.get(2), txn);
        txn.commit();

        long indexId = idx.getId();

        Index loadedIdx = dsFactory.loadIndex(indexId);
        assertEquals(assertionGUIDs.get(0), loadedIdx.get(tup1));
        assertEquals(assertionGUIDs.get(1), loadedIdx.get(tup2));
        assertEquals(assertionGUIDs.get(2), loadedIdx.get(tup3));

        Tuple fromTup = Tuple.builder()
                .add(100)
                .add(tup1.get(1).toString())
                .build();

        Tuple toTup = Tuple.builder()
                .add(500)
                .add("this string doesnt matter at all")
                .build();

        int assertionCounter = 0;
        for (GUID guid : loadedIdx.get(fromTup, toTup)) {
            assertEquals(assertionGUIDs.get(assertionCounter), guid);
            assertionCounter++;
        }

        assertEquals(2, assertionCounter);
    }

    @Test
    public void testGetColumnNames() throws IOException {
        Index.Builder indexBuilder = Index.newBuilder("narf")
                .withColumn("number", Integer.class)
                .withColumn("foo", String.class)
                .withColumn("foobar", Long.class);

        Txn txn = txnManager.beginTransaction();
        Index idx = dsFactory.newIndex(indexBuilder, txn);
        idx.checkDataStructureRetrieved();
        long idxId = idx.getId();
        txn.commit();

        Index newAndShinyIndex = dsFactory.loadIndex(idxId);
        List<String> columnNames = newAndShinyIndex.getColumnNames();
        assertEquals("number", columnNames.get(0));
        assertEquals("foo", columnNames.get(1));
        assertEquals("foobar", columnNames.get(2));
    }
}
