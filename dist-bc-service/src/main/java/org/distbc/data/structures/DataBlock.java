package org.distbc.data.structures;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.InputStream;
import java.io.OutputStream;

public class DataBlock<Key extends Comparable<Key>, Value> extends DataStructure {

    private Node first;

    private class Node {
        Key key;
        Value value;
        Node next;

        Node(Key key, Value value, Node next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    public Value get(Key key) {
        if (key == null) throw new IllegalArgumentException("key can't be null");
        Node x = first;
        while (x != null) {
            if (x.key.equals(key)) {
                return x.value;
            }
            x = x.next;
        }
        return null;
    }

    public void put(Key key, Value val) {
        if (key == null) throw new IllegalArgumentException("key can't be null");
        first = new Node(key, val, first);
    }

    @Override
    void serialize(OutputStream out) {
        try (Output o = new Output(out)) {
            Kryo k = kryoPool.borrow();
            Node x = first;
            try {
                while (x != null) {
                    k.writeClassAndObject(o, x.key);
                    k.writeClassAndObject(o, x.value);
                    x = x.next;
                }
            } finally {
                kryoPool.release(k);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    void deserialize(InputStream in) {
        try (Input i = new Input(in)) {
            boolean shouldDoIt;
            Kryo k = kryoPool.borrow();
            try {
                do {
                    Key key = (Key) k.readClassAndObject(i);
                    Value value = (Value) k.readClassAndObject(i);
                    shouldDoIt = key != null && value != null;
                    if (shouldDoIt) first = new Node(key, value, first);
                } while (shouldDoIt);
            } finally {
                kryoPool.release(k);
            }
        }
    }

    @Override
    public int size() {
        return 0;
    }
}
