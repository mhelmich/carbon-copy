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

import co.paralleluniverse.common.io.ByteBufferInputStream;
import co.paralleluniverse.common.io.ByteBufferOutputStream;
import co.paralleluniverse.common.io.Persistable;
import co.paralleluniverse.galaxy.Store;
import co.paralleluniverse.galaxy.TimeoutException;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;
import com.google.common.util.concurrent.ListenableFuture;
import de.javakaffee.kryoserializers.UUIDSerializer;
import org.xerial.snappy.Snappy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    private boolean isLoaded() {
        return isLoaded;
    }

    void asyncLoadForReads() {
        asyncLoadForReads(this);
    }

    private <T extends DataStructure> void asyncLoadForReads(T o) {
        if (isLoaded()) return;
        if (dataFuture != null) {
            throw new IllegalStateException("Can't override loadable future");
        }
        dataFuture = getAsync(getId(), o);
    }

    void asyncLoadForWrites(Txn txn) {
        asyncLoadForWrites(this, txn);
    }

    private <T extends DataStructure> void asyncLoadForWrites(T o, Txn txn) {
        if (isLoaded()) return;
        if (dataFuture != null) {
            throw new IllegalStateException("Can't override loadable future");
        }

        if (getId() == -1) {
            creationFuture = putAsync(o, txn);
        } else {
            dataFuture = getxAsync(getId(), o, txn);
        }
    }

    void asyncUpsert(Txn txn) {
        asyncUpsert(this, txn);
    }

    <T extends DataStructure> void asyncUpsert(T o, Txn txn) {
        try {
            if (getId() == -1) {
                creationFuture = putAsync(o, txn);
            } else {
                store.set(o.getId(), o, txn.getStoreTransaction());
            }
        } catch (KryoException xcp) {
            // this error handling gives us a better clue on what exactly went wrong
            // for example in the sizing of our ByteBuffers
            int estimatedSize = o.size();
            ByteBuffer bb = ByteBuffer.allocateDirect(Math.max(estimatedSize * 2, MAX_BYTE_SIZE));
            o.write(bb);
            int actuallyUsed = bb.position();
            throw new RuntimeException("cache line " + toString() + " failed to upsert! Estimated size: [" + estimatedSize + "]; Actual size: [" + actuallyUsed + "]", xcp);
        } catch (TimeoutException xcp) {
            throw new RuntimeException(xcp);
        }
    }

    <T extends DataStructure> void asyncDelete(Txn txn) {
        if (getId() > -1) {
            deleteAsync(this, txn);
        }
    }

    <T extends DataStructure> void asyncDelete(T o, Txn txn) {
        if (getId() > -1) {
            deleteAsync(o, txn);
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
        ByteBuffer uncompressedBB = ByteBuffer.allocateDirect(compressedBB.capacity());
        try (SerializerOutputStream out = new SerializerOutputStream(new ByteBufferOutputStream(uncompressedBB))) {
            serialize(out);
        } catch (IOException | KryoException xcp) {
            throw new RuntimeException(xcp);
        }

        if (uncompressedBB.position() > 0) {
            try {
                uncompressedBB.rewind();
                int compressedSize = Snappy.compress(uncompressedBB, compressedBB);
                compressedBB.position(compressedSize);
            } catch (IOException xcp) {
                throw new RuntimeException(xcp);
            } catch (IllegalArgumentException xcp) {
                throw new IllegalArgumentException("Compressing " + this.getClass().getSimpleName() + " _ " + getId() + " failed! uncompressedBB: " + uncompressedBB.capacity() + " compressedBB: " + compressedBB.capacity(), xcp);
            }
        }
    }

    // has to be public because of Galaxy
    public void read(ByteBuffer compressedBB) {
        ByteBuffer uncompressedBB;
        // snappy doesn't like it when you give it a ByteBuffer out of
        // which it can't read data (aka remaining == 0)
        // in that case we skip this code all together and don't call into
        // deserialize of the data structure either
        if (compressedBB.remaining() > 0) {
            try {
                int uncompressedLength = Snappy.uncompressedLength(compressedBB);
                uncompressedBB = ByteBuffer.allocateDirect(uncompressedLength);
                Snappy.uncompress(compressedBB, uncompressedBB);
            } catch (IOException xcp) {
                throw new RuntimeException(xcp);
            } catch (IllegalArgumentException xcp) {
                throw new IllegalArgumentException("Uncompressing " + this.getClass().getSimpleName() + " _ " + getId() + " failed!", xcp);
            }

            try (SerializerInputStream in = new SerializerInputStream(new ByteBufferInputStream(uncompressedBB))) {
                deserialize(in);
            } catch (IOException | KryoException xcp) {
                throw new RuntimeException(xcp);
            }
        }
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

    private <T extends DataStructure> void deleteAsync(T o, Txn txn) {
        store.delAsync(o.getId(), txn.getStoreTransaction());
    }

    // implementations get to decide how to serialize themselves
    // this is the call back that will be eventually invoked by Galaxy
    abstract void serialize(SerializerOutputStream out);
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

        private SerializerInputStream(InputStream i) {
            in = new Input(i);
            kryo = kryoPool.borrow();
        }

        @Override
        public int read() throws IOException {
            return in.read();
        }

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
}
