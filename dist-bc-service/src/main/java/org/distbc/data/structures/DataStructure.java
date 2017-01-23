package org.distbc.data.structures;

import co.paralleluniverse.common.io.ByteBufferInputStream;
import co.paralleluniverse.common.io.ByteBufferOutputStream;
import co.paralleluniverse.common.io.Persistable;
import co.paralleluniverse.galaxy.Grid;
import com.esotericsoftware.kryo.Kryo;
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
        // configure kryo instance, customize settings
        kryo.register(SkipList.class, 0);
        kryo.register(BTree.class, 1);
        return kryo;
    };

    static KryoPool kryoPool = new KryoPool.Builder(kryoFactory).build();

    @Inject
    Grid grid;

    public void write(ByteBuffer bb) {
        try (OutputStream out = new ByteBufferOutputStream(bb)) {
            serialize(out);
        } catch (IOException xcp) {
            throw new RuntimeException(xcp);
        }
    }

    public void read(ByteBuffer bb) {
        try (InputStream in = new ByteBufferInputStream(bb)) {
            deserialize(in);
        } catch (IOException xcp) {
            throw new RuntimeException(xcp);
        }
    }

    abstract void serialize(OutputStream out);
    abstract void deserialize(InputStream out);
}
