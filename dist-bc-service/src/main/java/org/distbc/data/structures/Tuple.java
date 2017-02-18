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
    // some sort of tuple metadata
    // a mapping of column name to index
    // and tuple size
    private int tupleSize;
    private ArrayList<Object> data;

    @SuppressWarnings("unused")
    private Tuple(ArrayList<Object> data, int tupleSize) {
        this.data = data;
        this.tupleSize = tupleSize;
    }

    Tuple(int size) {
        tupleSize = size;
        Vector<Object> v = new Vector<>(tupleSize);
        v.setSize(tupleSize);
        data = new ArrayList<>(v);
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

    public final static class TupleSerializer extends Serializer<Tuple> {
        @Override
        public void write(Kryo kryo, Output output, Tuple tuple) {
            output.writeInt(tuple.data.size());
            for (int i = 0; i < tuple.data.size(); i++) {
                kryo.writeClassAndObject(output, tuple.data.get(i));
            }
        }

        @Override
        public Tuple read(Kryo kryo, Input input, Class<Tuple> aClass) {
            int size = input.readInt();
            Vector<Object> v = new Vector<>(size);
            v.setSize(size);
            ArrayList<Object> data = new ArrayList<>(v);
            for(int i = 0; i < size; i++) {
                data.set(i, kryo.readClassAndObject(input));
            }

            try {
                Constructor<Tuple> ctor = aClass.getDeclaredConstructor(ArrayList.class, int.class);
                ctor.setAccessible(true);
                return ctor.newInstance(data, size);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
