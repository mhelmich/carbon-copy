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
import org.xerial.snappy.Snappy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

/**
 * Abstract classes and super classes are a red flag for guice
 * The combination of which is even worse
 * But this way I managed to build a fairly nice interface that should allow
 * easy addition of new data structures without going through the hassle
 * of setting up galaxy, worry about serialization, etc.
 */
abstract class DataStructure implements Persistable {
    static final int MAX_BYTE_SIZE = 32768;

    // actually there's a +1 is for the kryo byte to identify the class
    // however I hope that compression and not having the +1
    // even out in the long run :)
    private static final int LONG_FIELD_SIZE     = 8;
    private static final int INT_FIELD_SIZE      = 4;
    private static final int SHORT_FIELD_SIZE    = 2;
    private static final int CHAR_FIELD_SIZE     = 2;
    private static final int BYTE_FIELD_SIZE     = 1;
    private static final int BOOLEAN_FIELD_SIZE  = 1;
    private static final int DOUBLE_FIELD_SIZE   = 8;
    private static final int FLOAT_FIELD_SIZE    = 4;

    private static KryoFactory kryoFactory = () -> {
        Kryo kryo = new Kryo();
        kryo.setRegistrationRequired(true);
        // configure kryo instance, customize settings
//        kryo.register(Integer.class, 0);
//        kryo.register(String.class, 1);
//        kryo.register(SkipList.class);
//        kryo.register(BTree.class);

        // the first few integers are taken (0 .. 9)
        // these are obviously kryo internals and subject to change at any point in time
        // [0=[0, int], 9=[9, void], 8=[8, double], 7=[7, long], 6=[6, short], 5=[5, char],
        //  4=[4, byte], 3=[3, boolean], 2=[2, float], 1=[1, String]]

        return kryo;
    };

    private static KryoPool kryoPool = new KryoPool.Builder(kryoFactory).build();

    private Store store;
    private long id = -1;
    private ListenableFuture<Persistable> dataFuture = null;

    int currentObjectSize = 0;

    DataStructure(Store store) {
        this.store = store;
    }

    /**
     * Convenience constructor for read-only use
     * @param store
     * @param id
     */
    DataStructure(Store store, long id) {
        this(store);
        this.id = id;
    }

    boolean isUnderMaxByteSize(int addSize) {
        return currentObjectSize + addSize <= getMaxByteSize();
    }

    int getMaxByteSize() {
        return MAX_BYTE_SIZE;
    }

    long getId() {
        return id;
    }

    <T extends DataStructure> void loadForReads(T o) {
        get(getId(), o);
    }

    <T extends DataStructure> void asyncLoadForReads(T o) {
        if (dataFuture != null) {
            throw new IllegalStateException("Can't override loadable future");
        }
        dataFuture = getAsync(getId(), this);
    }

//    <T extends DataStructure> void loadForWrites(T o) {
//        getX(getId(), o);
//    }

//    void save() {
//        loadForWrites(this);
//    }

    // there is some sort of overhead included
    // I suppose that has to do with leading magic bytes
    // this needs to be a fairly performant method though
    // since it's called during serialization ... twice ... unnecessarily
    @Override
    public int size() {
        return 8;
    }

    public void write(ByteBuffer compressedBB) {
        ByteBuffer uncompressedBB = ByteBuffer.allocateDirect(compressedBB.capacity());
        try (SerializerOutputStream out = new SerializerOutputStream(new ByteBufferOutputStream(uncompressedBB))) {
            serialize(out);
        } catch (IOException xcp) {
            throw new RuntimeException(xcp);
        }

        try {
            uncompressedBB.rewind();
            int compressedSize = Snappy.compress(uncompressedBB, compressedBB);
            compressedBB.position(compressedSize);
        } catch (IOException xcp) {
            throw new RuntimeException(xcp);
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

    void checkDataStructureRetrieved() {
        if (dataFuture == null) {
            return;
        }

        try {
            Persistable persistable = dataFuture.get(5, TimeUnit.SECONDS);
            if (persistable != null && dataFuture.isDone()) {
                dataFuture = null;
            }
        } catch (Exception xcp) {
            throw new RuntimeException(xcp);
        }
    }

    private <T extends DataStructure> void get(long id, T o) {
        try {
            store.get(id, o);
        } catch (TimeoutException xcp) {
            throw new RuntimeException(xcp);
        }
    }

    private <T extends DataStructure> ListenableFuture<Persistable> getAsync(long id, T o) {
        return store.getAsync(id, o);
    }

    private <T extends DataStructure> T getX(long id) {
        return null;
    }

    private <T extends DataStructure> T getX(long id, T o) {
        return null;
    }

    private <T extends DataStructure> void put(T o) {
        long id = -1;
        try {
            id = store.put(o, null);
        } catch (TimeoutException xcp) {
            throw new RuntimeException(xcp);
        } finally {
            store.release(id);
        }
        this.id = id;
    }

    abstract void serialize(SerializerOutputStream out);
    abstract void deserialize(SerializerInputStream in);

    int sizeOfObject(Object o) {
        if (o == null) return 0;
        Class type = o.getClass();
        if (Integer.class.equals(type)) {
            return INT_FIELD_SIZE;
        } else if (String.class.equals(type)) {
            return ((String)o).getBytes().length;
        } else if (Long.class.equals(type)) {
            return LONG_FIELD_SIZE;
        } else if (Short.class.equals(type)) {
            return SHORT_FIELD_SIZE;
        } else if (Byte.class.equals(type)) {
            return BYTE_FIELD_SIZE;
        } else if (Boolean.class.equals(type)) {
            return BOOLEAN_FIELD_SIZE;
        } else if (Character.class.equals(type)) {
            return CHAR_FIELD_SIZE;
        } else if (Double.class.equals(type)) {
            return DOUBLE_FIELD_SIZE;
        } else if (Float.class.equals(type)) {
            return FLOAT_FIELD_SIZE;
        } else if (Persistable.class.isAssignableFrom(o.getClass())) {
            return ((Persistable)o).size();
        } else {
            throw new IllegalArgumentException ("unrecognized type: " + o.getClass());
        }
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
