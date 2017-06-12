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

package org.carbon.copy.planner;

import org.carbon.copy.data.structures.DataStructureFactory;
import org.carbon.copy.data.structures.GUID;
import org.carbon.copy.data.structures.TempTable;
import org.carbon.copy.data.structures.Tuple;
import org.carbon.copy.data.structures.Txn;
import org.carbon.copy.data.structures.TxnManager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

class BinaryQueryPlanSwimLane extends AbstractSwimLane {
    private final Future<TempTable> source1F;
    private final String nameJoinColumn1;
    private final Future<TempTable> source2F;
    private final String nameJoinColumn2;

    BinaryQueryPlanSwimLane(DataStructureFactory dsFactory, TxnManager txnManager, Future<TempTable> source1F, String nameJoinColumn1, Future<TempTable> source2F, String nameJoinColumn2) {
        super(dsFactory, txnManager);
        this.source1F = source1F;
        this.nameJoinColumn1 = nameJoinColumn1;
        this.source2F = source2F;
        this.nameJoinColumn2 = nameJoinColumn2;
    }

    @Override
    public TempTable call() throws Exception {
        TempTable source1 = source1F.get();
        TempTable source2 = source2F.get();

        int idxJoinColumn1 = source1.getColumnIndexForName(nameJoinColumn1);
        int idxJoinColumn2 = source2.getColumnIndexForName(nameJoinColumn2);

        Map<GUID, GUID> guids = hashJoin(source1, idxJoinColumn1, source2, idxJoinColumn2);

        TempTable.Builder ttBuilder = buildBuilder(source1, source2);
        Txn txn = txnManager.beginTransaction();
        TempTable tt = dsFactory.newTempTable(ttBuilder, txn);

        // add data
        for (Map.Entry<GUID, GUID> guid : guids.entrySet()) {
            Tuple t1 = source1.get(guid.getKey());
            Tuple t2 = source2.get(guid.getValue());
            Tuple tupleToKeep = concat(t1, t2);
            tt.insert(tupleToKeep, txn);
        }

        txn.commit();
        return tt;
    }

    private Map<GUID, GUID> hashJoin(TempTable source1, int idxJoinColumn1, TempTable source2, int idxJoinColumn2) {
        Map<Comparable, GUID> allValuesToGuidsFromSource1 = source1.keys()
                .map(source1::get)
                .collect(Collectors.toMap(
                        t -> t.get(idxJoinColumn1),
                        Tuple::getGuid
                ));

        return source2.keys()
                .map(source2::get)
                .filter(t -> allValuesToGuidsFromSource1.containsKey(t.get(idxJoinColumn2)))
                .collect(Collectors.toMap(
                        t -> allValuesToGuidsFromSource1.get(t.get(idxJoinColumn2)),
                        Tuple::getGuid
                ));
    }

    private Tuple concat(Tuple t1, Tuple t2) {
        int t1Size = t1.getTupleSize();
        int t2Size = t2.getTupleSize();
        Tuple newTuple = new Tuple(t1Size + t2Size);

        for (int i = 0; i < t1Size; i++) {
            newTuple.put(i, t1.get(i));
        }

        for (int i = 0; i < t2Size; i++) {
            newTuple.put(t1Size + i, t2.get(i));
        }

        return newTuple;
    }

    private TempTable.Builder buildBuilder(TempTable source1, TempTable source2) {
        TempTable.Builder ttBuilder = TempTable.newBuilder();
        // add metadata
        List<Tuple> source1Metadata = source1.getColumnMetadata();
        for (Tuple metadata : source1Metadata) {
            ttBuilder.withColumn(metadata.get(0).toString(), (int)metadata.get(1), metadata.get(2).toString());
        }

        int startIdx = source1Metadata.size();
        for (Tuple metadata : source2.getColumnMetadata()) {
            int idx = startIdx + (int)metadata.get(1);
            ttBuilder.withColumn(metadata.get(0).toString(), idx, metadata.get(2).toString());
        }

        return ttBuilder;
    }
}
