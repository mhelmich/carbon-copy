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

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TupleTest {
    @Test
    public void testBasic() {
        Tuple t = newTuple(5);
        t.put(0, "narf");
        t.put(1, 17);
        t.put(2, Long.MAX_VALUE);
        t.put(3, "narf_2");
        t.put(4, "MrMoep");

        assertEquals("narf", t.get(0));
        assertEquals(17, t.get(1));
        assertEquals(Long.MAX_VALUE, t.get(2));
        assertEquals("narf_2", t.get(3));
        assertEquals("MrMoep", t.get(4));
    }

    @Test
    public void testCompare() {
        List<Tuple> ts = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Tuple t = newTuple(1);
            t.put(0, i);
            ts.add(t);
        }

        Collections.shuffle(ts);
        Collections.sort(ts);

        for (int i = 0; i < 10; i++) {
            assertEquals(i, ts.get(i).get(0));
        }
    }

    @Test
    public void testCompareMultipleDimensions() {
        List<Tuple> ts = new ArrayList<>();

        Tuple t = newTuple(2);
        t.put(0, "AAA");
        t.put(1, 2);
        ts.add(t);

        t = newTuple(2);
        t.put(0, "AAA");
        t.put(1, 1);
        ts.add(t);

        t = newTuple(2);
        t.put(0, "BBB");
        t.put(1, 3);
        ts.add(t);

        t = newTuple(2);
        t.put(0, "BBB");
        t.put(1, 2);
        ts.add(t);

        t = newTuple(2);
        t.put(0, "BB");
        t.put(1, 2);
        ts.add(t);

        t = newTuple(2);
        t.put(0, "AA");
        t.put(1, null);
        ts.add(t);

        t = newTuple(2);
        t.put(0, "AAA");
        t.put(1, null);
        ts.add(t);

        t = newTuple(2);
        t.put(0, null);
        t.put(1, 1);
        ts.add(t);

        Collections.shuffle(ts);
        Collections.sort(ts);

        assertEquals("AA", ts.get(0).get(0));
        assertEquals(null, ts.get(0).get(1));

        assertEquals("AAA", ts.get(1).get(0));
        assertEquals(1, ts.get(1).get(1));

        assertEquals("AAA", ts.get(2).get(0));
        assertEquals(2, ts.get(2).get(1));

        assertEquals("AAA", ts.get(3).get(0));
        assertEquals(null, ts.get(3).get(1));

        assertEquals("BB", ts.get(4).get(0));
        assertEquals(2, ts.get(4).get(1));

        assertEquals("BBB", ts.get(5).get(0));
        assertEquals(2, ts.get(5).get(1));

        assertEquals("BBB", ts.get(6).get(0));
        assertEquals(3, ts.get(6).get(1));

        assertEquals(null, ts.get(7).get(0));
        assertEquals(1, ts.get(7).get(1));
    }

    private Tuple newTuple(int size) {
        return new Tuple(size);
    }
}
