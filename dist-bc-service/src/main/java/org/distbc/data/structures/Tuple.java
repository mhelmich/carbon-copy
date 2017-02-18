package org.distbc.data.structures;

import co.paralleluniverse.galaxy.Store;

import java.util.ArrayList;
import java.util.Vector;

class Tuple extends DataStructure {
    // some sort of tuple metadata
    // a mapping of column name to index
    // and tuple size
    private int tupleSize;
    private ArrayList<Object> data;

    Tuple(Store store) {
        super(store);
    }

    Tuple(Store store, int size) {
        this(store);
        tupleSize = size;
        Vector<Object> v = new Vector<>(tupleSize);
        v.setSize(tupleSize);
        data = new ArrayList<>(v);
    }

    Tuple(Store store, long id) {
        super(store, id);
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

    /////////////////////////////////////////////////////////////
    //////////////////////////////////////////////
    // galaxy-specific serialization overrides

    @Override
    void serialize(SerializerOutputStream out) {
        out.writeObject(tupleSize);
        for (int i = 0; i < tupleSize; i++) {
            out.writeObject(data.get(i));
        }
    }

    @Override
    void deserialize(SerializerInputStream in) {
        Integer size = (Integer) in.readObject();
        tupleSize = (size != null) ? size : 0;

        Vector<Object> v = new Vector<>(tupleSize);
        v.setSize(tupleSize);
        data = new ArrayList<>(v);

        for (int i = 0; i < tupleSize; i++) {
            data.set(i, in.readObject());
        }
    }
}
