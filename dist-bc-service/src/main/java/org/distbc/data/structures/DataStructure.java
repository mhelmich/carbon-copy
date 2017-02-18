package org.distbc.data.structures;

import co.paralleluniverse.common.io.ByteBufferInputStream;
import co.paralleluniverse.common.io.ByteBufferOutputStream;
import co.paralleluniverse.common.io.Persistable;
import co.paralleluniverse.galaxy.Store;
import co.paralleluniverse.galaxy.TimeoutException;
import com.esotericsoftware.kryo.Kryo;
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
 * Abstract classes and super classes are a red flag for guice
 * The combination of which is even worse
 * But this way I managed to build a fairly nice interface that should allow
 * easy addition of new data structures without going through the hassle
 * of setting up galaxy, worry about serialization, etc.
 */
abstract class DataStructure extends Sizable implements Persistable {
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

        return kryo;
    };

    private static KryoPool kryoPool = new KryoPool.Builder(kryoFactory).build();

    private final Store store;
    private long id = -1;
    private ListenableFuture<Persistable> dataFuture = null;
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

    long getId() {
        return id;
    }

    private boolean isLoaded() {
        return isLoaded;
    }

    void asyncLoadForReads() {
        asyncLoadForReads(this);
    }

    <T extends DataStructure> void asyncLoadForReads(T o) {
        if (isLoaded()) return;
        if (dataFuture != null) {
            throw new IllegalStateException("Can't override loadable future");
        }
        dataFuture = getAsync(getId(), o);
    }

    void asyncLoadForWrites(Txn txn) {
        asyncLoadForWrites(this, txn);
    }

    <T extends DataStructure> void asyncLoadForWrites(T o, Txn txn) {
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
        } catch (TimeoutException xcp) {
            throw new RuntimeException(xcp);
        }
    }

    boolean checkDataStructureRetrieved() {
        if (creationFuture != null) {
            try {
                Long id = creationFuture.get(5, TimeUnit.SECONDS);
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
                dataFuture.get(5, TimeUnit.SECONDS);
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

    public void write(ByteBuffer compressedBB) {
        ByteBuffer uncompressedBB = ByteBuffer.allocateDirect(compressedBB.capacity());
        try (SerializerOutputStream out = new SerializerOutputStream(new ByteBufferOutputStream(uncompressedBB))) {
            serialize(out);
        } catch (IOException xcp) {
            throw new RuntimeException(xcp);
        }

        if (uncompressedBB.position() > 0) {
            try {
                uncompressedBB.rewind();
                int compressedSize = Snappy.compress(uncompressedBB, compressedBB);
                compressedBB.position(compressedSize);
            } catch (IOException xcp) {
                throw new RuntimeException(xcp);
            }
        }
    }

    public void read(ByteBuffer compressedBB) {
        ByteBuffer uncompressedBB;
        try {
            int uncompressedLength = Snappy.uncompressedLength(compressedBB);
            uncompressedBB = ByteBuffer.allocateDirect(uncompressedLength);
            Snappy.uncompress(compressedBB, uncompressedBB);
        } catch (IOException xcp) {
            throw new RuntimeException(xcp);
        }

        try (SerializerInputStream in = new SerializerInputStream(new ByteBufferInputStream(uncompressedBB))) {
            deserialize(in);
        } catch (IOException xcp) {
            throw new RuntimeException(xcp);
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

    abstract void serialize(SerializerOutputStream out);
    abstract void deserialize(SerializerInputStream in);

    @Override
    public final int hashCode() {
        return 31 + ((getId() == -1) ? 0 : (int) (getId() % Integer.MAX_VALUE));
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final DataStructure other = (DataStructure) obj;
        return !(getId() == other.getId() && getId() == -1) && getId() == other.getId();
    }

    @Override
    public String toString() {
        return String.valueOf(getId());
    }

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
