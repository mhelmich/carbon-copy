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

package org.carbon.copy;

import co.paralleluniverse.common.io.ByteBufferInputStream;
import co.paralleluniverse.common.io.ByteBufferOutputStream;
import org.junit.Test;
import org.xerial.snappy.Snappy;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SnappyTest {
    @Test
    public void testBasic() throws IOException {
        Random r = new Random();
        byte[] bites = new byte[1024 * 32];
        r.nextBytes(bites);
        ByteBuffer bb1 = ByteBuffer.allocateDirect(bites.length);
        try (ByteBufferOutputStream bbos = new ByteBufferOutputStream(bb1)) {
            bbos.write(bites);
        }

        bb1.clear();

        int maxCompressedSize = Snappy.maxCompressedLength(bites.length);
        ByteBuffer bb2 = ByteBuffer.allocateDirect(maxCompressedSize);
        Snappy.compress(bb1, bb2);

        ByteBuffer bb3 = ByteBuffer.allocateDirect(Snappy.uncompressedLength(bb2));
        int uncompressedSize = Snappy.uncompress(bb2, bb3);
        assertEquals(bites.length, uncompressedSize);
        byte[] bites2 = new byte[Snappy.uncompressedLength(bb2)];
        try (ByteBufferInputStream bbis = new ByteBufferInputStream(bb3)) {
            bbis.readFully(bites2);
        }
        assertTrue(Arrays.equals(bites, bites2));
    }
}
