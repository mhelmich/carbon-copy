package org.distbc.data.structures;

import com.esotericsoftware.kryo.DefaultSerializer;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Vector;

@DefaultSerializer(Tuple.TupleSerializer.class)
class Tuple extends Sizable {
    private GUID guid;
    // some sort of tuple metadata
    // a mapping of column name to index
    // and tuple size
    private int tupleSize;
    private ArrayList<Object> data;

    @SuppressWarnings("unused")
    private Tuple(GUID guid, ArrayList<Object> data, int tupleSize) {
        this.guid = guid;
        this.data = data;
        this.tupleSize = tupleSize;
    }

    Tuple(int size) {
        tupleSize = size;
        Vector<Object> v = new Vector<>(tupleSize);
        v.setSize(tupleSize);
        data = new ArrayList<>(v);
        guid = GUID.randomGUID();
    }

    GUID getGuid() {
        return guid;
    }

    Object get(int idx) {
        return data.get(idx);
    }

    void put(int idx, Object o) {
        Object existingO = data.get(idx);
        if (existingO != null) {
            subtractObjectToObjectSize(existingO);
        }
        addObjectToObjectSize(o);
        data.set(idx, o);
    }

    Tuple immutableCopy() {
        return new Tuple(guid, new ArrayList<>(data), tupleSize) {
            @Override
            void put(int idx, Object o) { }
        };
    }

    public final static class TupleSerializer extends Serializer<Tuple> {
        @Override
        public void write(Kryo kryo, Output output, Tuple tuple) {
            kryo.writeClassAndObject(output, tuple.guid);
            output.writeInt(tuple.data.size());
            for (int i = 0; i < tuple.data.size(); i++) {
                kryo.writeClassAndObject(output, tuple.data.get(i));
            }
        }

        @Override
        public Tuple read(Kryo kryo, Input input, Class<Tuple> aClass) {
            GUID guid = (GUID) kryo.readClassAndObject(input);
            int size = input.readInt();
            Vector<Object> v = new Vector<>(size);
            v.setSize(size);
            ArrayList<Object> data = new ArrayList<>(v);
            for (int i = 0; i < size; i++) {
                data.set(i, kryo.readClassAndObject(input));
            }

            try {
                Constructor<Tuple> ctor = aClass.getDeclaredConstructor(GUID.class, ArrayList.class, int.class);
                ctor.setAccessible(true);
                return ctor.newInstance(guid, data, size);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
