package org.distbc.data.structures;

/*
 B-tree.

 Limitations
 -----------
 -  Assumes nodeSize is even and nodeSize >= 4

 */

import java.util.ArrayList;
import java.util.Vector;

/**
 *  The {@code BTree} class represents an ordered symbol table of generic
 *  key-value pairs.
 *  It supports the <em>put</em>, <em>get</em>, <em>contains</em>,
 *  <em>size</em>, and <em>is-empty</em> methods.
 *  A symbol table implements the <em>associative array</em> abstraction:
 *  when associating a value with a key that is already in the symbol table,
 *  the convention is to replace the old value with the new value.
 *  Unlike {@link java.util.Map}, this class uses the convention that
 *  values cannot be {@code null}—setting the
 *  value associated with a key to {@code null} is equivalent to deleting the key
 *  from the symbol table.
 *  <p>
 *  This implementation uses a B-tree. It requires that
 *  the key type implements the {@code Comparable} interface and calls the
 *  {@code compareTo()} and method to compare two keys. It does not call either
 *  {@code equals()} or {@code hashCode()}.
 *  The <em>get</em>, <em>put</em>, and <em>contains</em> operations
 *  each make log<sub><em>numChildren</em></sub>(<em>size</em>) probes in the worst case,
 *  where <em>size</em> is the number of key-value pairs
 *  and <em>numChildren</em> is the branching factor.
 *  The <em>size</em>, and <em>is-empty</em> operations take constant time.
 *  Construction takes constant time.
 *  <p>
 *  For additional documentation, see
 *  <a href="http://algs4.cs.princeton.edu/62btree">Section 6.2</a> of
 *  <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 */
public class BTree<Key extends Comparable<Key>, Value> {
    // max children per B-tree node = nodeSize-1
    // (must be even and greater than 2)
    private static final int nodeSize = 4;

    private Node root;       // root of the B-tree
    private int height;      // height of the B-tree
    private int size;           // number of key-value pairs in the B-tree

    // helper B-tree node data type
    private final class Node {
        private int numChildren;                   // number of children
        private ArrayList<Entry> children;         // the array of children

        // create a node with numChildren children
        private Node(int numChildren) {
            Vector<Entry> v = new Vector<>(nodeSize);
            v.setSize(nodeSize);
            children = new ArrayList<>(v);
            this.numChildren = numChildren;
        }
    }

    // internal nodes: only use key and next
    // external nodes: only use key and value
    private final class Entry {
        private Key key;
        private final Value val;
        private Node next;     // helper field to iterate over array entries
        private Entry(Key key, Value val, Node next) {
            this.key  = key;
            this.val  = val;
            this.next = next;
        }

        Node getNext() {
            return next;
        }

        void setNext(Node next) {
            this.next = next;
        }
    }

    private Entry newEntry(Key key, Value val, Node next) {
        return new Entry(key, val, next);
    }

    /**
     * Initializes an empty B-tree.
     */
    public BTree() {
        root = new Node(0);
    }

    /**
     * Returns true if this symbol table is empty.
     * @return {@code true} if this symbol table is empty; {@code false} otherwise
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Returns the number of key-value pairs in this symbol table.
     * @return the number of key-value pairs in this symbol table
     */
    public int size() {
        return size;
    }

    /**
     * Returns the height of this B-tree (for debugging).
     *
     * @return the height of this B-tree
     */
    public int height() {
        return height;
    }


    /**
     * Returns the value associated with the given key.
     *
     * @param  key the key
     * @return the value associated with the given key if the key is in the symbol table
     *         and {@code null} if the key is not in the symbol table
     * @throws IllegalArgumentException if {@code key} is {@code null}
     */
    public Value get(Key key) {
        if (key == null) throw new IllegalArgumentException("argument to get() is null");
        return search(root, key, height);
    }

    private Value search(Node x, Key key, int height) {
        ArrayList<Entry> children = x.children;

        // leaf node
        if (height == 0) {
            for (int j = 0; j < x.numChildren; j++) {
                if (eq(key, children.get(j).key)) return children.get(j).val;
            }
        } else {
        // internal node
            for (int j = 0; j < x.numChildren; j++) {
                if (j+1 == x.numChildren || less(key, children.get(j+1).key)) {
                    return search(children.get(j).getNext(), key, height-1);
                }
            }
        }
        return null;
    }


    /**
     * Inserts the key-value pair into the symbol table, overwriting the old value
     * with the new value if the key is already in the symbol table.
     * If the value is {@code null}, this effectively deletes the key from the symbol table.
     *
     * @param  key the key
     * @param  val the value
     * @throws IllegalArgumentException if {@code key} is {@code null}
     */
    public void put(Key key, Value val) {
        if (key == null) throw new IllegalArgumentException("argument key to put() is null");
        Node insertedNode = insert(root, key, val, height);
        size++;
        if (insertedNode == null) return;

        // need to split root
        Node newNode = new Node(2);
        newNode.children.set(0, newEntry(root.children.get(0).key, null, root));
        newNode.children.set(1, newEntry(insertedNode.children.get(0).key, null, insertedNode));
        root = newNode;
        height++;
    }

    private Node insert(Node x, Key key, Value val, int height) {
        int j;
        Entry entry = newEntry(key, val, null);

        // leaf node
        if (height == 0) {
            for (j = 0; j < x.numChildren; j++) {
                if (less(key, x.children.get(j).key)) break;
            }
        } else {
        // internal node
            for (j = 0; j < x.numChildren; j++) {
                if ((j+1 == x.numChildren) || less(key, x.children.get(j+1).key)) {
                    Node insertedNode = insert(x.children.get(j++).getNext(), key, val, height-1);
                    if (insertedNode == null) return null;
                    entry.key = insertedNode.children.get(0).key;
                    entry.setNext(insertedNode);
                    break;
                }
            }
        }

        for (int i = x.numChildren; i > j; i--) {
            x.children.set(i, x.children.get(i-1));
        }
        x.children.set(j, entry);
        x.numChildren++;
        return (x.numChildren < nodeSize) ? null : split(x);
    }

    // split node in half
    private Node split(Node h) {
        Node t = new Node(nodeSize / 2);
        h.numChildren = nodeSize / 2;
        for (int j = 0; j < nodeSize / 2; j++) {
            t.children.set(j, h.children.get((nodeSize / 2) + j));
        }
        return t;
    }

    public String toString() {
        return toString(root, height, "") + "\n";
    }

    private String toString(Node x, int height, String indent) {
        StringBuilder sb = new StringBuilder();
        ArrayList<Entry> children = x.children;

        if (height == 0) {
            for (int j = 0; j < x.numChildren; j++) {
                sb.append(indent).append(children.get(j).key).append(" ").append(children.get(j).val).append("\n");
            }
        }
        else {
            for (int j = 0; j < x.numChildren; j++) {
                if (j > 0) sb.append(indent).append("(").append(children.get(j).key).append(")\n");
                sb.append(toString(children.get(j).getNext(), height-1, indent + "     "));
            }
        }
        return sb.toString();
    }

    private boolean less(Key k1, Key k2) {
        return k1.compareTo(k2) < 0;
    }

    private boolean eq(Key k1, Key k2) {
        return k1.compareTo(k2) == 0;
    }
}

/*
 * ****************************************************************************
 Copyright 2002-2016, Robert Sedgewick and Kevin Wayne.

 This file is part of algs4.jar, which accompanies the textbook

 Algorithms, 4th edition by Robert Sedgewick and Kevin Wayne,
 Addison-Wesley Professional, 2011, ISBN 0-321-57351-X.
 http://algs4.cs.princeton.edu


 algs4.jar is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 algs4.jar is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with algs4.jar.  If not, see http://www.gnu.org/licenses.
 */
