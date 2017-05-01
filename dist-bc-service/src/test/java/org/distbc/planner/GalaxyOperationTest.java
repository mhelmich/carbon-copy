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

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import org.distbc.GuiceJUnit4Runner;
import org.distbc.GuiceModules;
import org.distbc.data.structures.DataStructureModule;
import org.distbc.data.structures.GUID;
import org.distbc.data.structures.InternalDataStructureFactory;
import org.distbc.data.structures.Table;
import org.distbc.data.structures.TempTable;
import org.distbc.data.structures.Tuple;
import org.distbc.data.structures.Txn;
import org.distbc.data.structures.TxnManager;
import org.distbc.data.structures.TxnManagerModule;
import org.distbc.parser.ParsingResult;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(GuiceJUnit4Runner.class)
@GuiceModules({ DataStructureModule.class, TxnManagerModule.class })
public class GalaxyOperationTest {
    @Inject
    private InternalDataStructureFactory dsFactory;

    @Inject
    private TxnManager txnManager;

    @Test
    public void testProjection() throws IOException {
        Table t = createDummyTable();
        Txn txn = txnManager.beginTransaction();
        TempTable ttOld = dsFactory.newTempTableFromTable(t, txn);
        txn.commit();

        Projection p = new Projection(ImmutableList.of("tup_num".toUpperCase(), "foo".toUpperCase()), ttOld.getColumnNames());
        Txn txn2 = txnManager.beginTransaction();
        TempTable tt = p.apply(ttOld, txn2);
        txn2.commit();

        t.keys().forEach(guid -> {
            assertNotNull(tt.get(guid));
            assertEquals(guid, tt.get(guid).getGuid());
            assertNotEquals(t.get(guid), tt.get(guid));
        });
    }

    @Test
    public void testSelection() throws IOException {
        Table t = createDummyTable();
        Txn txn = txnManager.beginTransaction();
        TempTable ttOld = dsFactory.newTempTableFromTable(t, txn);
        txn.commit();

        Selection s = new Selection(ImmutableList.of(
                new ParsingResult.BinaryOperation("moep", "=", "moep")
        ));
        Txn txn2 = txnManager.beginTransaction();
        TempTable tt = s.apply(ttOld, txn2);
        txn2.commit();

        tt.keys().forEach(guid -> {
            assertNotNull(t.get(guid));
            assertEquals(guid, tt.get(guid).getGuid());
            assertEquals(t.get(guid), tt.get(guid));
        });

        assertEquals(2, tt.keys().count());
    }

    @Test
    public void testSelection2() throws IOException {
        Table t = createDummyTable();
        Txn txn = txnManager.beginTransaction();
        TempTable ttOld = dsFactory.newTempTableFromTable(t, txn);
        txn.commit();

        Selection s = new Selection(ImmutableList.of(
                new ParsingResult.BinaryOperation("moep", "=", "__moep__")
        ));
        Txn txn2 = txnManager.beginTransaction();
        TempTable tt = s.apply(ttOld, txn2);
        txn2.commit();

        tt.keys().forEach(guid -> {
            assertNotNull(t.get(guid));
            assertEquals(guid, tt.get(guid).getGuid());
            assertEquals(t.get(guid), tt.get(guid));
        });

        assertEquals(1, tt.keys().count());
    }

    @Test
    public void testSelection3() throws IOException {
        Table t = createDummyTable();
        Txn txn = txnManager.beginTransaction();
        TempTable ttOld = dsFactory.newTempTableFromTable(t, txn);
        txn.commit();

        Selection s = new Selection(ImmutableList.of(
                new ParsingResult.BinaryOperation("tup_num", "=", "tup3_narf")
        ));
        Txn txn2 = txnManager.beginTransaction();
        TempTable tt = s.apply(ttOld, txn2);
        txn2.commit();

        tt.keys().forEach(guid -> {
            assertNotNull(t.get(guid));
            assertEquals(guid, tt.get(guid).getGuid());
            assertEquals(t.get(guid), tt.get(guid));
        });

        assertEquals(1, tt.keys().count());
    }

    @Test
    public void testSelection4() throws IOException {
        Table t = createDummyTable();
        Txn txn = txnManager.beginTransaction();
        TempTable ttOld = dsFactory.newTempTableFromTable(t, txn);
        txn.commit();

        Selection s = new Selection(ImmutableList.of(
                new ParsingResult.BinaryOperation("tup_num", "<=", "tup3_narf")
        ));
        Txn txn2 = txnManager.beginTransaction();
        TempTable tt = s.apply(ttOld, txn2);
        txn2.commit();

        tt.keys().forEach(guid -> {
            assertNotNull(t.get(guid));
            assertEquals(guid, tt.get(guid).getGuid());
            assertEquals(t.get(guid), tt.get(guid));
        });

        assertEquals(3, tt.keys().count());
    }

    @Test
    public void testOpMaterialized() throws IOException {
        List<GUID> guids = new LinkedList<>();
        Table t = createDummyTable(guids);
        Txn txn = txnManager.beginTransaction();
        TempTable tt = dsFactory.newTempTable(txn);
        tt.addColumnWithName(txn, "tup_num", 0, String.class);
        txn.commit();

        Set<GUID> idsToKeep = new HashSet<GUID>() {{
            add(guids.get(1));
        }};

        List<Integer> columnIndexesToKeep = new LinkedList<>();
        columnIndexesToKeep.add(2);

        OpMaterialize op = new OpMaterialize(t, tt, idsToKeep, columnIndexesToKeep);
        txn = txnManager.beginTransaction();
        TempTable res = op.apply(txn);
        txn.commit();

        assertEquals(tt.getId(), res.getId());

        for (GUID guid : idsToKeep) {
            assertNotNull(res.get(guid));
            assertEquals(1, res.get(guid).getTupleSize());
            assertEquals("tup2_foo", res.get(guid).get(0));
        }

        assertNotNull(tt.getColumnMetadataByColumnName("TUP_NUM"));
    }

    @Test
    public void testOpProjection() throws IOException {
        List<String> columnNamesToProjectTo = new LinkedList<String>() {{
            add("moep");
        }};
        List<String> columnsAvailableInTuple = new LinkedList<String>() {{
            add("tup_num");
            add("moep");
            add("foo");
        }};

        OpProjection op = new OpProjection(columnNamesToProjectTo, columnsAvailableInTuple);
        List<Integer> indexToKeep = op.get();

        assertEquals(1, indexToKeep.size());
        assertEquals(Integer.valueOf(1), indexToKeep.get(0));
    }

    @Test
    public void testOpSelection() throws IOException {
        Table t = createDummyTable();

        List<ParsingResult.BinaryOperation> bos = new LinkedList<ParsingResult.BinaryOperation>() {{
            add(new ParsingResult.BinaryOperation("FOO", "<=", "tup2_foo"));
        }};

        OpSelection op = new OpSelection(bos, t);
        Set<GUID> res = op.get();

        assertEquals(2, res.size());
        for (GUID guid : res) {
            assertNotNull(t.get(guid));
        }
    }

    @Test
    public void testAllOpsTogether() throws IOException {
        List<GUID> guids = new LinkedList<>();
        Table t = createDummyTable(guids);

        /////////////////////////////
        ///////////////////////
        // PROJECTION
        List<String> columnNamesToProjectTo = new LinkedList<String>() {{
            add("tup_num".toUpperCase());
        }};
        List<String> columnsAvailableInTuple = t.getColumnNames();
        OpProjection pro = new OpProjection(columnNamesToProjectTo, columnsAvailableInTuple);
        List<Integer> indexesToKeep = pro.get();

        /////////////////////////////
        ///////////////////////
        // SELECTION
        List<ParsingResult.BinaryOperation> bos = new LinkedList<ParsingResult.BinaryOperation>() {{
            add(new ParsingResult.BinaryOperation("FOO", "<=", "tup2_foo"));
        }};
        OpSelection sel = new OpSelection(bos, t);
        Set<GUID> res = sel.get();

        /////////////////////////////
        ///////////////////////
        // MATERIALIZE
        Txn txn = txnManager.beginTransaction();

        TempTable tt = dsFactory.newTempTable(txn);
        tt.addColumnWithName(txn, "tup_num", 0, String.class);

        OpMaterialize mat = new OpMaterialize(t, tt, res, indexesToKeep);
        txn = txnManager.beginTransaction();
        TempTable resultSet = mat.apply(txn);
        txn.commit();

        assertEquals(2, resultSet.keys().count());
    }

    private Table createDummyTable() throws IOException {
        return createDummyTable(null);
    }

    private Table createDummyTable(List<GUID> guids) throws IOException {
        Txn txn = txnManager.beginTransaction();

        Table.Builder tableBuilder = Table.Builder.newBuilder("narf_" + System.currentTimeMillis() + "_" + this.getClass().getName())
                .withColumn("tup_num", String.class)
                .withColumn("moep", String.class)
                .withColumn("foo", String.class);

        Table table = dsFactory.newTable(tableBuilder, txn);

        Tuple tup1 = new Tuple(3);
        tup1.put(0, "tup1_narf");
        tup1.put(1, "moep");
        tup1.put(2, "tup1_foo");

        Tuple tup2 = new Tuple(3);
        tup2.put(0, "tup2_narf");
        tup2.put(1, "__moep__");
        tup2.put(2, "tup2_foo");

        Tuple tup3 = new Tuple(3);
        tup3.put(0, "tup3_narf");
        tup3.put(1, "moep");
        tup3.put(2, "tup3_foo");

        if (guids == null) {
            table.insert(tup1, txn);
            table.insert(tup2, txn);
            table.insert(tup3, txn);
        } else {
            guids.add(table.insert(tup1, txn));
            guids.add(table.insert(tup2, txn));
            guids.add(table.insert(tup3, txn));
        }

        txn.commit();

        return table;
    }
}
