package org.carbon.copy.data.structures;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

import static org.carbon.copy.data.structures.DataStructure.kryoPool;

abstract class BaseMessage {

    protected UUID requestId;

    In getIn(byte[] bytes) {
        return new In(kryoPool.borrow(), new Input(new ByteArrayInputStream(bytes)));
    }

    void send(co.paralleluniverse.galaxy.Messenger messenger, short nodeId) {
        messenger.send(nodeId, getTopic(), toByteArray());
    }

    private byte[] toByteArray() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (Output out = new Output(baos)) {
                Kryo kryo = kryoPool.borrow();
                try {
                    toBytes(new Out(kryo, out));
                } finally {
                    kryoPool.release(kryo);
                }
            }

            return baos.toByteArray();
        } catch (IOException xcp) {
            throw new RuntimeException(xcp);
        }
    }

    void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }

    abstract String getTopic();
    abstract void toBytes(Out out);

    static class Out implements AutoCloseable {
        private final Kryo kryo;
        private final Output out;

        Out(Kryo kryo, Output out) {
            this.kryo = kryo;
            this.out = out;
        }

        void write(Object o) {
            kryo.writeClassAndObject(out, o);
        }

        <T> void writeNull(T o, Class<T> klass) {
            kryo.writeClass(out, klass);
            kryo.writeObjectOrNull(out, o, klass);
        }

        @Override
        public void close() throws Exception {
            try {
                kryoPool.release(kryo);
            } finally {
                out.close();
            }
        }
    }

    static class In implements AutoCloseable {
        private final Kryo kryo;
        private final Input in;

        In(Kryo kryo, Input in) {
            this.kryo = kryo;
            this.in = in;
        }

        Object read() {
            return kryo.readClassAndObject(in);
        }

        @Override
        public void close() throws Exception {
            try {
                kryoPool.release(kryo);
            } finally {
                in.close();
            }
        }
    }
}
