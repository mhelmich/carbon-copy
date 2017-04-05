package org.distbc.data.structures;

import com.esotericsoftware.kryo.DefaultSerializer;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

@DefaultSerializer(Tuple.TupleSerializer.class)
public class Tuple extends Sizable implements Comparable<Tuple> {
    private GUID guid;
    // some sort of tuple metadata
    // a mapping of column name to index
    // and tuple size
    private int tupleSize;
    private ArrayList<Comparable> data;

    /**
     * Private ctor for serializer use only.
     */
    @SuppressWarnings("unused")
    private Tuple(GUID guid, ArrayList<Comparable> data, int tupleSize) {
        this.guid = guid;
        this.data = data;
        this.tupleSize = tupleSize;
    }

    Tuple(int size) {
        tupleSize = size;
        Vector<Comparable> v = new Vector<>(tupleSize);
        v.setSize(tupleSize);
        data = new ArrayList<>(v);
        guid = GUID.randomGUID();
    }

    public GUID getGuid() {
        return guid;
    }

    public Object get(int idx) {
        return data.get(idx);
    }

    public void put(int idx, Comparable o) {
        Object existingO = data.get(idx);
        if (existingO != null) {
            subtractObjectToObjectSize(existingO);
        }
        addObjectToObjectSize(o);
        data.set(idx, o);
    }

    public int getTupleSize() {
        return tupleSize;
    }

    Tuple subTuple(int from, int to) {
        int size = to - from;
        Tuple newTuple = new Tuple(size);
        for (int i = 0; i < size; i++) {
            newTuple.put(i, data.get(i + from));
        }
        return newTuple;
    }

    public Tuple subTuple(List<Integer> indexes) {
        Tuple t = new Tuple(indexes.size());
        for (int i = 0; i < indexes.size(); i++) {
            t.put(i, data.get(indexes.get(i)));
        }
        return t;
    }

    Tuple immutableCopy() {
        return new Tuple(guid, new ArrayList<>(data), tupleSize) {
            @Override
            public void put(int idx, Comparable o) { }
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public int compareTo(@Nonnull Tuple o) {
        for (int i = 0; i < tupleSize; i++) {
            if (data.get(i) != null && o.data.get(i) != null) {
                int cmp = data.get(i).compareTo(o.data.get(i));
                if (cmp != 0) {
                    return cmp;
                }
            // the last two if clauses will sort nulls to the back
            } else if (data.get(i) != null) {
                return -1;
            } else if (o.data.get(i) != null) {
                return 1;
            }
        }

        // this happens when all items of a tuple are null
        return 0;
    }

    @Override
    public String toString() {
        return StringUtils.join(data, " - ");
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ArrayList<Comparable> data = new ArrayList<>();
        private Builder() {}

        public Builder add(Comparable item) {
            data.add(item);
            return this;
        }

        public Tuple build() {
            Tuple t = new Tuple(data.size());
            for (int i = 0; i < data.size(); i++) {
                t.put(i, data.get(i));
            }
            return t;
        }
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
