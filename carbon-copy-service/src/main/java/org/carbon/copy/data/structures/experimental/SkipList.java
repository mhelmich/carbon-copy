/*
 *
 *  Copyright 2017 Marco Helmich
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.carbon.copy.data.structures.experimental;

import com.google.common.base.Optional;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class SkipList<Key extends Comparable<Key>, Value extends Comparable<Value>> {

//    private static Logger logger = LoggerFactory.getLogger(SkipList.class);

    // TODO : implement Persistable
    // TODO : read ConcurrentLinkedQueue.java for usage of CAS instructions

    private static transient int MAX_LEVEL = 32;
    private static transient double P = 0.25;

    private class Node implements Comparable<Node> {
        final Key key;
        final Value value;
        Node backward;
        final Node[] forward;

        Node(Key k, Value v, int level) {
            key = k;
            value = v;
            forward = createNodeArray(level);
        }

        public int compareTo(Node o) {
            return key.compareTo(o.key);
        }
    }

    @SuppressWarnings("unchecked")
    private final Node[] createNodeArray(int size) {
        return (Node[]) Array.newInstance(Node.class, size);
    }

    private Node head = new Node(null, null, MAX_LEVEL);
    // use this for serializing from the tail
    // the algorithm has a best case runtime if it's deserialized backwards
    private Node tail = new Node(null, null, MAX_LEVEL);
    private int highestLevel;
    private int size;
    private Random random = new Random();

    private int getRandomLevel() {
        int level = 1;
        while ((random.nextInt() & 0xFFFF) < (P * 0xFFFF)) {
            level += 1;
        }
        return (level < MAX_LEVEL) ? level : MAX_LEVEL;
    }

    public void insert(Key key, Value value) {
        Node[] update = createNodeArray(MAX_LEVEL);
        Node x = head;
        for (int i = this.highestLevel; i >= 0; i--) {
            while (x.forward[i] != null && x.forward[i].key != null) {
                int cmp = x.forward[i].key.compareTo(key);
                if (cmp < 0 || (cmp == 0 && x.forward[i].value.compareTo(value) < 0)) {
                    x = x.forward[i];
                } else {
                    break;
                }
            }
            update[i] = x;
        }

        int newLevel = getRandomLevel();
        if (newLevel > highestLevel) {
            for (int i = highestLevel; i < newLevel; i++) {
                update[i] = head;
            }
            highestLevel = newLevel;
        }

        Node newNode = new Node(key, value, newLevel);
        for (int i = 0; i < newLevel; i++) {
            newNode.forward[i] = update[i].forward[i];
            update[i].forward[i] = newNode;
        }

        newNode.backward = update[0].equals(head) ? head : update[0];
        if (newNode.forward[0] != null) {
            newNode.forward[0].backward = newNode;
        } else {
            tail = newNode;
        }

        size++;
    }

    public void delete(Key key) {
        Node[] update = createNodeArray(highestLevel + 1);
        Node x = head;
        for (int i = highestLevel; i >= 0; i--) {
            while (x.forward[i] != null) {
                int cmp = x.forward[i].key.compareTo(key);
                if (cmp < 0) {
                    x = x.forward[i];
                } else {
                    break;
                }
            }
            update[i] = x;
        }

        x = x.forward[0];
        while (x != null && x.key.compareTo(key) == 0) {
            Node next = x.forward[0];
            deleteNode(x, update);
            size--;
            x = next;
        }
    }

    private void deleteNode(Node nodeToDelete, Node[] update) {
        for (int i = 0; i < highestLevel; i++) {
            if (update[i].forward[i] != null && update[i].forward[i].compareTo(nodeToDelete) == 0) {
                update[i].forward[i] = nodeToDelete.forward[i];
            }
        }

        if (nodeToDelete.forward[0] != null) {
            nodeToDelete.forward[0].backward = nodeToDelete.backward;
        } else {
            tail = nodeToDelete.backward;
        }

        while ( highestLevel > 1 && head.forward[highestLevel - 1] == null ) {
            highestLevel--;
        }
        highestLevel--;
    }

    public Optional<Value> floor(Key key) {
        Node x = head;
        for (int i = this.highestLevel; i >= 0; i--) {
            while (x.forward[i] != null) {
                int cmp = x.forward[i].key.compareTo(key);
                if (cmp < 0) {
                    x = x.forward[i];
                } else {
                    break;
                }
            }
        }
        x = x.forward[0];
        return x.key.compareTo(key) == 0 ? Optional.of(x.value) : Optional.absent();
    }

    public Set<Value> search(Key key) {
        Node x = head;
        for (int i = this.highestLevel; i >= 0; i--) {
            while (x.forward[i] != null) {
                int cmp = x.forward[i].key.compareTo(key);
                if (cmp < 0) {
                    x = x.forward[i];
                } else {
                    break;
                }
            }
        }
        x = x.forward[0];

        Set<Value> resultSet = new HashSet<>();
        while (x.key.compareTo(key) == 0) {
            resultSet.add(x.value);
            x = x.forward[0];
        }

        return resultSet.isEmpty() ? Collections.emptySet() : resultSet;
    }

    public int size() {
        return size;
    }

    public void write(ByteBuffer byteBuffer) {

    }

    public void read(ByteBuffer byteBuffer) {

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(size * 32);
        Node x = head.forward[0];
        do {
            sb.append(x.key.toString()).append(" ==> ");
            x = x.forward[0];
        } while (x.forward[0] != null);

        return sb.toString();
    }
}
