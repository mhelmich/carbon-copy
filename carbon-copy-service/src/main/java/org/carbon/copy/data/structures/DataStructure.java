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

import co.paralleluniverse.common.io.Persistable;
import co.paralleluniverse.galaxy.RefNotFoundException;
import co.paralleluniverse.galaxy.Store;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;
import com.google.common.primitives.Shorts;
import com.google.common.util.concurrent.ListenableFuture;
import de.javakaffee.kryoserializers.UUIDSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xerial.snappy.Snappy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * This class wants to make you forget (as good as possible) what underlying library is being used.
 * It should abstract out all the dealings you should ever be having with Galaxy.
 *
 * Abstract classes and super classes are a red flag for guice
 * The combination of which is even worse
 * But this way I managed to build a fairly nice interface that should allow
 * easy addition of new data structures without going through the hassle
 * of setting up galaxy, worry about serialization, etc.
 *
 * Don't you ever call any of these constructors directly! They are made to be called by
 * an implementation of DataStructureFactory.
 */
abstract class DataStructure extends Sizable implements Persistable {
    static final int TIMEOUT_SECS = 5;
    private static KryoFactory kryoFactory = () -> {
        Kryo kryo = new Kryo();
        kryo.setRegistrationRequired(true);
        // configure kryo instance, customize settings
        // the first few integers are taken (0 .. 9)
        // these are obviously kryo internals and subject to change at any point in time
        // [0=[0, int], 9=[9, void], 8=[8, double], 7=[7, long], 6=[6, short], 5=[5, char],
        //  4=[4, byte], 3=[3, boolean], 2=[2, float], 1=[1, String]]

        kryo.register(DataBlock.class, 10);
        kryo.register(Tuple.class, 11);
        kryo.register(ChainingHash.class, 12);
        kryo.register(BTreeNode.class, 13);
        kryo.register(BTree.class, 14);
        kryo.register(UUID.class, new UUIDSerializer(), 15);
        kryo.register(GUID.class, 16);

        return kryo;
    };

    static KryoPool kryoPool = new KryoPool.Builder(kryoFactory).build();

    private final Store store;
    private long id = -1;
    // future used to load data
    private ListenableFuture<Persistable> dataFuture = null;
    // future to create a new data structure
    private ListenableFuture<Long> creationFuture = null;
    private boolean isLoaded = false;

    DataStructure(Store store) {
        this(store, -1);
    }

    /**
     * Convenience constructor for read-only use
     */
    DataStructure(Store store, long id) {
        this.store = store;
        this.id = id;
    }

    public long getId() {
        checkDataStructureRetrieved();
        return id;
    }

    boolean isLoaded() {
        return isLoaded;
    }

    ListenableFuture<Persistable> asyncLoadForReads() {
        try {
            return asyncLoadForReads(this);
        } catch (RefNotFoundException xcp) {
            throw new IllegalStateException(xcp);
        }
    }

    private <T extends DataStructure> ListenableFuture<Persistable> asyncLoadForReads(T o) {
        if (dataFuture != null) {
            throw new IllegalStateException("Can't override loadable future");
        }
        dataFuture = getAsync(getId(), o);
        return dataFuture;
    }

    ListenableFuture asyncLoadForWrites(Txn txn) {
        return asyncLoadForWrites(this, txn);
    }

    private <T extends DataStructure> ListenableFuture asyncLoadForWrites(T o, Txn txn) {
        if (dataFuture != null) {
            throw new IllegalStateException("Can't override loadable future");
        }

        if (getId() == -1) {
            creationFuture = putAsync(o, txn);
            return creationFuture;
        } else {
            dataFuture = getxAsync(getId(), o, txn);
            return dataFuture;
        }
    }

    ListenableFuture asyncUpsert(Txn txn) {
        return asyncUpsert(this, txn);
    }

    <T extends DataStructure> ListenableFuture asyncUpsert(T o, Txn txn) {
        try {
            if (getId() == -1) {
                creationFuture = putAsync(o, txn);
                return creationFuture;
            } else {
                return setAsync(o.getId(), o, txn);
            }
        } catch (KryoException xcp) {
            // this error handling gives us a better clue on what exactly went wrong
            // for example in the sizing of our ByteBuffers
            int estimatedSize = o.size();
            ByteBuffer bb = ByteBuffer.allocateDirect(Math.max(estimatedSize * 2, MAX_BYTE_SIZE));
            o.write(bb);
            int actuallyUsed = bb.position();
            throw new RuntimeException("cache line " + toString() + " failed to upsert! Estimated size: [" + estimatedSize + "]; Actual size: [" + actuallyUsed + "]", xcp);
        }
    }

    <T extends DataStructure> ListenableFuture<Void> asyncDelete(Txn txn) {
        if (getId() > -1) {
            return deleteAsync(this, txn);
        } else {
            return null;
        }
    }

    <T extends DataStructure> ListenableFuture<Void> asyncDelete(T o, Txn txn) {
        if (getId() > -1) {
            return deleteAsync(o, txn);
        } else {
            return null;
        }
    }

    /**
     * Was the data you ask for retrieved?
     */
    boolean checkDataStructureRetrieved() {
        if (creationFuture != null) {
            try {
                Long id = creationFuture.get(TIMEOUT_SECS, TimeUnit.SECONDS);
                if (id != null && creationFuture.isDone()) {
                    this.id = id;
                    creationFuture = null;
                    isLoaded = true;
                    return true;
                }
            } catch (Exception xcp) {
                throw new RuntimeException(xcp);
            }
        } else if (dataFuture != null) {
            try {
                dataFuture.get(TIMEOUT_SECS, TimeUnit.SECONDS);
                if (dataFuture.isDone()) {
                    dataFuture = null;
                    isLoaded = true;
                    return true;
                }
            } catch (Exception xcp) {
                throw new RuntimeException(xcp);
            }
        }

        return false;
    }

    // has to be public because of Galaxy
    public void write(ByteBuffer compressedBB) {
        CompressingByteBufferOutputStream baos = new CompressingByteBufferOutputStream(compressedBB.capacity());
        try (SerializerOutputStream out = new SerializerOutputStream(baos)) {
            serialize(out);
        } catch (IOException | KryoException xcp) {
            throw new RuntimeException("Byte Array [" + baos.size() + "] ByteBuffer [" + compressedBB.capacity() + "]", xcp);
        }

        try {
            baos.writeTo(compressedBB);
        } catch (IOException xcp) {
            throw new RuntimeException("Byte Array [" + baos.size() + "] ByteBuffer [" + compressedBB.capacity() + "]", xcp);
        }
    }

    // has to be public because of Galaxy
    public void read(ByteBuffer compressedBB) {
        // snappy doesn't like it when you give it a ByteBuffer out of
        // which it can't read data (aka remaining == 0)
        // in that case we skip this code all together and don't call into
        // deserialize of the data structure either
        if (compressedBB.remaining() > 0) {
            try (SerializerInputStream in = new SerializerInputStream(new DecompressingByteBufferInputStream(compressedBB))) {
                deserialize(in);
            } catch (IOException | KryoException | IllegalStateException xcp) {
                compressedBB.rewind();
                byte[] uncompressed = DecompressingByteBufferInputStream.readFrom(compressedBB);
                int uncompressedLength = uncompressed.length;
                throw new RuntimeException("Byte Array [" + uncompressedLength + "] ByteBuffer [" + compressedBB.capacity() + "]", xcp);
            }
        }
    }

    private <T extends DataStructure> ListenableFuture<Void> setAsync(long id, T o, Txn txn) {
        return store.setAsync(id, o, txn.getStoreTransaction());
    }

    private <T extends DataStructure> ListenableFuture<Persistable> getAsync(long id, T o) {
        return store.getAsync(id, o);
    }

    private <T extends DataStructure> ListenableFuture<Persistable> getxAsync(long id, T o, Txn txn) {
        return store.getxAsync(id, o, txn.getStoreTransaction());
    }

    private <T extends DataStructure> ListenableFuture<Long> putAsync(T o, Txn txn) {
        return store.putAsync(o, txn.getStoreTransaction());
    }

    private <T extends DataStructure> ListenableFuture<Void> deleteAsync(T o, Txn txn) {
        return store.delAsync(o.getId(), txn.getStoreTransaction());
    }

    // implementations get to decide how to serialize themselves
    // this is the call back that will be eventually invoked by Galaxy

    /**
     * NB: Design your data structure in a way that it doesn't require trailing zeros.
     * All trailing zeros in this output stream will be purged and not serialized.
     */
    abstract void serialize(SerializerOutputStream out);

    /**
     * Is called with the byte stream that galaxy delivered.
     * Beware: The byte stream might contain an unknown number of trailing zeros. This is likely to be an artifact of decompression.
     * Data structure deserialization needs to accommodate for that.
     */
    abstract void deserialize(SerializerInputStream in);

    ////////////////////////////////////////////////
    //////////////////////////////////
    // this method is not allowed to call getId()
    // directly because the transaction might step onto itself
    // if the transaction is running and calls hashCode on a data structure,
    // the transaction might block itself
    // I saw this mostly in tests but never went to the bottom of it
    @Override
    public final int hashCode() {
        return 31 + ((id == -1) ? super.hashCode() : (int) (id % Integer.MAX_VALUE));
    }

    ////////////////////////////////////////////////
    //////////////////////////////////
    // this method is not allowed to call getId()
    // directly because the transaction might step onto itself
    // if the transaction is running and calls hashCode on a data structure,
    // the transaction might block itself
    // I saw this mostly in tests but never went to the bottom of it
    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final DataStructure other = (DataStructure) obj;
        return !(id == other.id && id == -1) && id == other.id;
    }

    ////////////////////////////////////////////////
    //////////////////////////////////
    // this method is not allowed to call getId()
    // directly because the transaction might step onto itself
    // if the transaction is running and calls hashCode on a data structure,
    // the transaction might block itself
    // I saw this mostly in tests but never went to the bottom of it
    @Override
    public String toString() {
        return getClass().getSimpleName() + " - " + String.valueOf(id) + " - " + size();
    }

    //////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////
    //////////////////////////////////////
    // These classes deal with kryo and galaxy.
    // They make sure kryo instances are returned to the pool properly.
    // These are being passed on to the implementations of this class
    // so that implementors have it easy to serialize their data structures.
    static class SerializerOutputStream extends OutputStream {
        private Kryo kryo;
        private Output out;

        private SerializerOutputStream(OutputStream o) {
            out = new Output(o);
            kryo = kryoPool.borrow();
        }

        @Override
        public void write(int b) throws IOException {
            out.write(b);
        }

        // being called to just write type information
        // the usual pattern would look like this:
        // 1. write type information by grabbing the first value -- use writeType(Class)
        // 2. stash the same class object you pass in here
        // 3. use the same class object to write your object data -- use writeObject(Object,Class)
        void writeType(Class klass) {
            kryo.writeClass(out, klass);
        }

        void writeObject(Object o, Class klass) {
            kryo.writeObjectOrNull(out, o, klass);
        }

        // write an object and its type information into the byte stream
        void writeObject(Object o) {
            kryo.writeClassAndObject(out, o);
        }

        @Override
        public void close() throws IOException {
            try {
                out.close();
            } finally {
                try {
                    kryoPool.release(kryo);
                } finally {
                    super.close();
                }
            }
        }
    }

    static class SerializerInputStream extends InputStream {
        private final Kryo kryo;
        private final Input in;

        // visible for testing
        SerializerInputStream(InputStream i) {
            in = new Input(i);
            kryo = kryoPool.borrow();
        }

        @Override
        public int read() throws IOException {
            return in.read();
        }

        // read type information from the byte stream
        Class readType() {
            return kryo.readClass(in).getType();
        }

        // read an object from the byte stream after you know the type
        <T> T readObject(Class<T> klass) {
            return kryo.readObjectOrNull(in, klass);
        }

        // read an object that has accompanying type information in the byte stream
        Object readObject() {
            return kryo.readClassAndObject(in);
        }

        @Override
        public int available() throws IOException {
            return in.available();
        }

        @Override
        public void close() throws IOException {
            try {
                in.close();
            } finally {
                try {
                    kryoPool.release(kryo);
                } finally {
                    super.close();
                }
            }
        }
    }

    //////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////
    //////////////////////////////////////
    // These stream implementations let us deal with compressed and uncompressed
    // various byte representations
    // They will interfere with a ByteBuffer and Snappy
    static class CompressingByteBufferOutputStream extends ByteArrayOutputStream {
        private static final Logger logger = LoggerFactory.getLogger(CompressingByteBufferOutputStream.class);

        CompressingByteBufferOutputStream(int size) {
            super(size);
        }

        synchronized void writeTo(ByteBuffer out) throws IOException {
            if (!out.isDirect()) {
                throw new IllegalArgumentException("out is not direct ByteBuffer");
            } else {
                // this makes a copy of the byte buffer
                byte[] compressed = Snappy.rawCompress(buf, count);
                try {
                    if (compressed.length > out.capacity()) {
                        // CRAP!!!
                        // this puts us in a *very* uncomfortable situation
                        // we simply can't write this block and need to explode this transaction
                        String errorMessage = String.format("Data with length %d doesn't fit in buffer with size %d", compressed.length, out.capacity());
                        throw new IllegalStateException(errorMessage);
                    }

                    // make the leading 2 bytes the size
                    // of the compressed data
                    byte[] payloadSizeHeader = Shorts.toByteArray((short)compressed.length);
                    out.position(0);
                    // make sure the limit of the byte buffer has the length
                    // of the byte array we want to put in there
                    out.limit(payloadSizeHeader.length + compressed.length);
                    out.put(payloadSizeHeader, 0, payloadSizeHeader.length);
                    // put my byte array at the beginning of the byte buffer
                    out.put(compressed, 0, compressed.length);
                } catch (BufferOverflowException | IllegalArgumentException | IllegalStateException xcp) {
                    logger.error("Bytebuffer size [{}] pos [{}] lim [{}] compressed length [{}]", out.capacity(), out.position(), out.limit(), compressed.length, xcp);
                    throw new IllegalStateException(xcp);
                }
            }
        }
    }

    static class DecompressingByteBufferInputStream extends ByteArrayInputStream {
        DecompressingByteBufferInputStream(ByteBuffer in) {
            super(readFrom(in));
        }

        private static byte[] readFrom(ByteBuffer in) {
            if (!in.isDirect()) {
                throw new IllegalArgumentException("in is not direct ByteBuffer");
            } else {
                // the leading 2 bytes are the length of
                // the compressed array
                byte[] payloadSizeHeader = new byte[2];
                in.get(payloadSizeHeader);
                short compressedArraySize = Shorts.fromBytes(payloadSizeHeader[0], payloadSizeHeader[1]);

                // this makes a copy of the byte buffer
                byte[] compressed = new byte[compressedArraySize];
                in.get(compressed);
                try {
                    // beware: sometimes decompression leads to (an unknown number of) trailing zeros
                    // those trailing zeros can cause a problem in the deserialize method of data structures
                    // we can either a) take care of that in data structures or b) trim all trailing zeros here
                    // but really only the data structure knows what the byte stream is supposed to look like
                    // however you could expect trailing zeros to be an optional part of a data structure
                    // which can have a default value
                    return Snappy.uncompress(compressed);
                } catch (IOException xcp) {
                    throw new IllegalStateException(xcp);
                }
            }
        }
    }
}
