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

import org.distbc.data.structures.DataStructureFactory;
import org.distbc.data.structures.GUID;
import org.distbc.data.structures.TempTable;
import org.distbc.data.structures.Tuple;
import org.distbc.data.structures.Txn;
import org.distbc.data.structures.TxnManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JoiningQueryPlanSwimLane extends AbstractSwimLane {
    private final TempTable source1;
    private final int idxJoinColumn1;
    private final TempTable source2;
    private final int idxJoinColumn2;

    JoiningQueryPlanSwimLane(DataStructureFactory dsFactory, TxnManager txnManager, TempTable source1, int idxJoinColumn1, TempTable source2, int idxJoinColumn2) {
        super(dsFactory, txnManager);
        this.source1 = source1;
        this.idxJoinColumn1 = idxJoinColumn1;
        this.source2 = source2;
        this.idxJoinColumn2 = idxJoinColumn2;
    }

    @Override
    public TempTable call() throws Exception {
        /////////////////////////////////
        /////////////////////////
        // hash join baby!!!
        Map<Comparable, GUID> allValuesToGuidsFromSource1 = source1.keys()
                .map(source1::get)
                .collect(Collectors.toMap(
                        t -> t.get(idxJoinColumn1),
                        Tuple::getGuid
                ));

        List<GUID> guidsToKeepFromSource1 = new ArrayList<>();
        List<GUID> guidsToKeepFromSource2 = new ArrayList<>();
        Iterator<GUID> it = source2.keys().iterator();
        while (it.hasNext()) {
            GUID guid = it.next();
            Tuple t = source2.get(guid);
            Comparable key = t.get(idxJoinColumn2);
            if (allValuesToGuidsFromSource1.containsKey(key)) {
                guidsToKeepFromSource1.add(allValuesToGuidsFromSource1.get(key));
                guidsToKeepFromSource2.add(guid);
            }
        }

        TempTable.Builder ttBuilder = TempTable.newBuilder();
        // add metadata
        for (Tuple metadata : source1.getColumnMetadata()) {
            ttBuilder.withColumn(metadata.get(0).toString(), (int)metadata.get(1), metadata.get(2).toString());
        }

        int startIdx = source1.getColumnMetadata().size();
        for (Tuple metadata : source2.getColumnMetadata()) {
            int idx = startIdx + (int)metadata.get(1);
            ttBuilder.withColumn(metadata.get(0).toString(), idx, metadata.get(2).toString());
        }

        Txn txn = txnManager.beginTransaction();
        TempTable tt = dsFactory.newTempTable(ttBuilder, txn);

        // add data
        for (int i = 0; i < guidsToKeepFromSource1.size(); i++) {
            Tuple t1 = source1.get(guidsToKeepFromSource1.get(i));
            Tuple t2 = source2.get(guidsToKeepFromSource2.get(i));
            Tuple tupleToKeep = concat(t1, t2);
            tt.insert(tupleToKeep, txn);
        }

        txn.commit();

        return tt;
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
}
