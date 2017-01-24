package org.distbc.data.structures;

import co.paralleluniverse.common.io.ByteBufferInputStream;
import co.paralleluniverse.common.io.ByteBufferOutputStream;
import co.paralleluniverse.common.io.Persistable;
import co.paralleluniverse.galaxy.Grid;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;
import com.google.inject.Inject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

abstract class DataStructure implements Persistable {
    // TODO : kryo business could go here
    // that seems to common across all data structures
    private static final int MAX_BYTE_SIZE = 32768;

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

    @Inject
    Grid grid;

    private long id;

    DataStructure() {}

    DataStructure(long id) {
        this.id = id;
    }

    public void write(ByteBuffer bb) {
        try (KryoOutputStream out = new KryoOutputStream(new ByteBufferOutputStream(bb))) {
            serialize(out);
        } catch (IOException xcp) {
            throw new RuntimeException(xcp);
        }
    }

    public void read(ByteBuffer bb) {
        try (KryoInputStream in = new KryoInputStream(new ByteBufferInputStream(bb))) {
            deserialize(in);
        } catch (IOException xcp) {
            throw new RuntimeException(xcp);
        }
    }

    abstract void serialize(KryoOutputStream out);
    abstract void deserialize(KryoInputStream out);

    static class KryoOutputStream extends OutputStream {
        private Kryo kryo;
        private Output out;

        private KryoOutputStream(OutputStream o) {
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

    static class KryoInputStream extends InputStream {
        private final Kryo kryo;
        private final Input in;

        private KryoInputStream(InputStream i) {
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
