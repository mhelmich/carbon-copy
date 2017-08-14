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

import co.paralleluniverse.galaxy.Store;
import org.junit.Test;
import org.mockito.Mockito;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.StreamSupport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class DataBlockTest {

    @Test
    public void testBasic() {
        DataBlock<Integer, Integer> db = newDataBlock();
        db.innerPut(5, 5);
        db.innerPut(6, 6);

        assertEquals(Integer.valueOf(5), db.get(5));
        assertEquals(Integer.valueOf(6), db.get(6));
        assertNull(db.get(7));
    }

    @Test
    public void testDups() {
        DataBlock<Integer, Integer> db = newDataBlock();
        db.innerPut(5, 5);
        db.innerPut(5, 6);

        assertEquals(Integer.valueOf(6), db.get(5));
        assertNull(db.get(6));
    }

    @Test
    public void testSerialization() {
        DataBlock<Integer, Integer> db = newDataBlock();
        db.innerPut(5, 5);
        db.innerPut(6, 6);
        assertEquals(Integer.valueOf(5), db.get(5));
        assertEquals(Integer.valueOf(6), db.get(6));

        ByteBuffer bb = ByteBuffer.allocateDirect(DataStructure.MAX_BYTE_SIZE);
        db.write(bb);
        assertEquals(78, db.size());
        assertEquals(14, bb.position());
        assertTrue(DataStructure.MAX_BYTE_SIZE > bb.remaining());

        bb.rewind();
        DataBlock<Integer, Integer> db2 = newDataBlock();
        db2.read(bb);

        assertEquals(Integer.valueOf(5), db2.get(5));
        assertEquals(Integer.valueOf(6), db2.get(6));
        assertEquals(db.size(), db2.size());


        DataBlock<String, String> db3 = newDataBlock();
        String val1 = UUID.randomUUID().toString();
        String val2 = UUID.randomUUID().toString();
        String val3 = UUID.randomUUID().toString();
        db3.innerPut("1", val1);
        db3.innerPut("2", val2);
        db3.innerPut("3", val3);

        ByteBuffer bb2 = ByteBuffer.allocateDirect(DataStructure.MAX_BYTE_SIZE);
        db3.write(bb2);
        assertEquals(189, db3.size());
        assertTrue(1665 >= bb2.position());
        assertTrue(DataStructure.MAX_BYTE_SIZE > bb2.remaining());
        bb2.rewind();

        DataBlock<String, String> db4 = newDataBlock();
        db4.read(bb2);
        assertEquals(val2, db4.get("2"));
        assertEquals(val3, db4.get("3"));
        assertEquals(val1, db4.get("1"));
        assertEquals(db3.size(), db4.size());
    }

    @Test
    public void testPutIfPossible() {
        Txn txn = Mockito.mock(Txn.class);
        when(txn.getStoreTransaction()).thenReturn(null);
        Store s = Mockito.mock(Store.class);
        DataBlock<Integer, Integer> db = new DataBlock<Integer, Integer>(s, txn) {
            @Override
            int getMaxByteSize() {
                return 80;
            }
        };

        assertTrue(db.innerPutIfPossible(3, 3));
        assertTrue(db.innerPutIfPossible(5, 5));
        assertFalse(db.innerPutIfPossible(7, 7));
    }

    @Test
    public void testDeleteMiddle() {
        DataBlock<Integer, Integer> db = newDataBlock();
        db.innerPut(3, 3);
        db.innerPut(5, 5);
        db.innerPut(7, 7);

        assertEquals(Integer.valueOf(5), db.get(5));
        assertEquals(Integer.valueOf(3), db.get(3));
        assertEquals(Integer.valueOf(7), db.get(7));

        db.innerDelete(5);

        assertNull(db.get(5));
        assertEquals(Integer.valueOf(3), db.get(3));
        assertEquals(Integer.valueOf(7), db.get(7));
        assertEquals(78, db.size());
    }

    @Test
    public void testDeleteFirst() {
        DataBlock<Integer, Integer> db = newDataBlock();
        db.innerPut(3, 3);
        db.innerPut(5, 5);
        db.innerPut(7, 7);

        assertEquals(Integer.valueOf(5), db.get(5));
        assertEquals(Integer.valueOf(3), db.get(3));
        assertEquals(Integer.valueOf(7), db.get(7));
        assertEquals(88, db.size());

        db.innerDelete(7);

        assertNull(db.get(7));
        assertEquals(Integer.valueOf(3), db.get(3));
        assertEquals(Integer.valueOf(5), db.get(5));
        assertEquals(88, db.size());
    }

    @Test
    public void testDeleteLast() {
        DataBlock<Integer, Integer> db = newDataBlock();
        db.innerPut(3, 3);
        db.innerPut(5, 5);
        db.innerPut(7, 7);

        assertEquals(Integer.valueOf(5), db.get(5));
        assertEquals(Integer.valueOf(3), db.get(3));
        assertEquals(Integer.valueOf(7), db.get(7));
        assertEquals(88, db.size());

        db.innerDelete(3);

        assertNull(db.get(3));
        assertEquals(Integer.valueOf(5), db.get(5));
        assertEquals(Integer.valueOf(7), db.get(7));
        assertEquals(78, db.size());
    }

    @Test
    public void testKeys() {
        DataBlock<Integer, Integer> db = newDataBlock();
        db.innerPut(3, 3);
        db.innerPut(5, 5);
        db.innerPut(7, 7);
        assertEquals(88, db.size());

        Set<Integer> keys = new HashSet<>();
        for (Integer key : db.keys()) {
            keys.add(key);
        }

        assertEquals(3, keys.size());
        assertTrue(keys.contains(3));
        assertTrue(keys.contains(5));
        assertTrue(keys.contains(7));
        assertFalse(keys.contains(2));
    }

    @Test
    public void testOverrideKeys() {
        DataBlock<Integer, Integer> db = newDataBlock();
        db.innerPut(3, 3);
        db.innerPut(5, 5);
        db.innerPut(7, 7);

        assertEquals(Integer.valueOf(3), db.get(3));
        assertEquals(Integer.valueOf(5), db.get(5));
        assertEquals(Integer.valueOf(7), db.get(7));

        db.innerPut(5, 17);
        assertEquals(Integer.valueOf(17), db.get(5));
        List<Integer> allKeys = new LinkedList<>();
        StreamSupport.stream(db.keys().spliterator(), false).forEach(allKeys::add);
        assertEquals(1, Collections.frequency(allKeys, 5));
    }

    private <Key extends Comparable<Key>, Value> DataBlock<Key, Value> newDataBlock() {
        Txn txn = Mockito.mock(Txn.class);
        when(txn.getStoreTransaction()).thenReturn(null);
        Store s = Mockito.mock(Store.class);
        return new DataBlock<>(s, txn);
    }
}
