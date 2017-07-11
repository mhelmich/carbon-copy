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

package org.carbon.copy.data.structures;

import co.paralleluniverse.galaxy.Cluster;
import co.paralleluniverse.galaxy.Messenger;
import co.paralleluniverse.galaxy.Store;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.hash.Funnel;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.inject.Provider;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

class DistHash <Key extends Comparable<Key>, Value> extends DataStructure {
    private static Logger logger = LoggerFactory.getLogger(DistHash.class);

    private static final Map<String, Pair<CountDownLatch, Long>> outstandingPutRequests = new HashMap<>();
    private static final Map<String, Pair<CountDownLatch, Object>> outstandingGetRequests = new HashMap<>();

    private final Funnel<Key> keyFunnel = (Funnel<Key>) (key, into) ->
            into.putString(key.toString(), Charsets.UTF_8);

    private final Funnel<Short> nodeInfoFunnel = (Funnel<Short>) (nodeId, into) ->
            into.putShort(nodeId);

    private final Cluster cluster;
    private final Messenger messenger;
    private final HashFunction hf;

    private int hashTableSize;
    private HashMap<Short, Long> hashTable;

    DistHash(Store store, Cluster cluster, Messenger messenger, Txn txn) {
        super(store);
        this.hashTableSize = cluster.getNodes().size();
        this.hashTable = new HashMap<>(this.hashTableSize);
        asyncUpsert(txn);

        this.cluster = cluster;
        this.messenger = messenger;
        this.hf = Hashing.murmur3_128(getMyNodeId(cluster));

        checkDataStructureRetrieved();
        txn.addToChangedObjects(this);
    }

    DistHash(Store store, Cluster cluster, Messenger messenger, long id) {
        super(store, id);
        this.cluster = cluster;
        this.messenger = messenger;
        this.hf = Hashing.murmur3_128(getMyNodeId(cluster));
        checkDataStructureRetrieved();
    }

    public void put(Key key, Value val, Txn txn) {
        if (txn == null) throw new IllegalArgumentException("Txn cannot be null");
        checkDataStructureRetrieved();
        Short nodeId = findNodeInCluster(key);
        Long blockId = hashTable.get(nodeId);
        String requestId = UUID.randomUUID().toString();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (Output out = new Output(baos)) {
                Kryo kryo = kryoPool.borrow();
                try {
                    kryo.writeClassAndObject(out, key);
                    kryo.writeClassAndObject(out, val);
                    kryo.writeClassAndObject(out, requestId);
                    kryo.writeObjectOrNull(out, blockId, Long.class);
                } finally {
                    kryoPool.release(kryo);
                }
            }

            CountDownLatch latch = new CountDownLatch(1);
            outstandingPutRequests.put(requestId, Pair.of(latch, null));
            messenger.send(nodeId, PutRequestMessageListener.TOPIC, baos.toByteArray());

            latch.await(TIMEOUT_SECS, TimeUnit.MINUTES);
            Pair<CountDownLatch, Long> pair = outstandingPutRequests.remove(requestId);
            if (pair != null) {
                hashTable.put(nodeId, pair.getRight());
            } else {
                throw new IllegalStateException("Couldn't find request id " + requestId);
            }
        } catch (IOException | InterruptedException xcp) {
            throw new RuntimeException(xcp);
        }
    }

    public Value get(Key key) {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");
        checkDataStructureRetrieved();
        return fireOffGetRequest(key);
    }

    public boolean delete(Key key, Txn txn) {
        if (txn == null) throw new IllegalArgumentException("Txn cannot be null");
        checkDataStructureRetrieved();
        return true;
    }

    Short findNodeInCluster(Key key) {
        Set<Short> allNodes = getNodes();
        Map<Short, Long> nodeToHash = new HashMap<>(allNodes.size());
        allNodes.forEach(nodeId -> nodeToHash.put(nodeId, computeHashForGalaxyNode(key, nodeId)));
        Optional<Map.Entry<Short, Long>> maxOpt = nodeToHash.entrySet().stream().max(Map.Entry.comparingByValue());
        if (maxOpt.isPresent()) {
            return maxOpt.get().getKey();
        } else {
            throw new IllegalStateException("Can't find suitable node to place key: " + key);
        }
    }

    private long computeHashForGalaxyNode(Key key, Short nodeId) {
        return hf.newHasher()
                .putObject(key, keyFunnel)
                .putObject(nodeId, nodeInfoFunnel)
                .hash()
                .asLong();
    }

    protected Set<Short> getNodes() {
        Set<Short> nodes = cluster.getNodes();
        return (nodes.size() > 0) ? nodes : ImmutableSet.of(cluster.getMyNodeId());
    }

    protected short getMyNodeId(Cluster cluster) {
        return cluster.getMyNodeId();
    }

    @SuppressWarnings("unchecked")
    private Value fireOffGetRequest(Key key) {
        Short nodeId = findNodeInCluster(key);
        Long blockId = hashTable.get(nodeId);

        // if the blockId is null, there's no record
        if (blockId == null) {
            return null;
        }

        String requestId = UUID.randomUUID().toString();
        CountDownLatch latch = new CountDownLatch(1);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (Output out = new Output(baos)) {
                Kryo kryo = kryoPool.borrow();
                try {
                    kryo.writeClassAndObject(out, key);
                    kryo.writeClassAndObject(out, requestId);
                    kryo.writeClassAndObject(out, blockId);
                } finally {
                    kryoPool.release(kryo);
                }
            }

            outstandingGetRequests.put(requestId, Pair.of(latch, null));
            messenger.send(nodeId, GetRequestMessageListener.TOPIC, baos.toByteArray());

            latch.await(TIMEOUT_SECS, TimeUnit.MINUTES);
            Pair<CountDownLatch, Object> pair = outstandingGetRequests.remove(requestId);
            if (pair != null) {
                return (Value) pair.getRight();
            } else {
                throw new IllegalStateException("Couldn't find request id " + requestId);
            }
        } catch (IOException | InterruptedException xcp) {
            throw new RuntimeException(xcp);
        }

    }

    /////////////////////////////////////////////////////////////
    //////////////////////////////////////////////
    // galaxy-specific serialization overrides

    @Override
    void serialize(SerializerOutputStream out) {
        out.writeObject(hashTableSize);
        hashTable.forEach((key, value) -> {
            out.writeObject(key);
            out.writeObject(value);
        });
    }

    @Override
    void deserialize(SerializerInputStream in) {
        try {
            Integer tmp = (Integer) in.readObject();
            hashTableSize = (tmp != null) ? tmp : 0;

            for (int i = 0; i < hashTableSize && in.available() > 0; i++) {
                Short nodeId = (Short) in.readObject();
                Long blockId = (Long) in.readObject();
                hashTable.put(nodeId, blockId);
            }
        } catch (IOException xcp) {
            throw new RuntimeException(xcp);
        }
    }

    /////////////////////////////////////////////////////////////
    //////////////////////////////////////////////
    // galaxy-specific messenger classes

    static class PutRequestMessageListener extends BaseMessageListener {
        // wire format:
        //  1. key
        //  2. value
        //  3. requestId
        //  4. blockId (optional)
        final static String TOPIC = "req:P";

        private final Provider<InternalDataStructureFactory> dsFactory;
        private final Provider<TxnManager> txnManager;
        private final Provider<Messenger> messenger;

        PutRequestMessageListener(Provider<InternalDataStructureFactory> dsFactory, Provider<TxnManager> txnManager, Provider<Messenger> messenger) {
            this.dsFactory = dsFactory;
            this.txnManager = txnManager;
            this.messenger = messenger;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void messageReceived(short fromNode, byte[] bytes) {
            try {
                Comparable key;
                Object value;
                String requestId;
                Long blockId;
                try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
                    try (Input in = new Input(bais)) {
                        Kryo kryo = kryoPool.borrow();
                        try {
                            key = (Comparable) kryo.readClassAndObject(in);
                            value = kryo.readClassAndObject(in);
                            requestId = (String) kryo.readClassAndObject(in);
                            blockId = (Long) kryo.readClassAndObject(in);
                        } finally {
                            kryoPool.release(kryo);
                        }
                    }
                }

                logger.info("{} {} {} {}", getClass().getName(), key, value, blockId);
                Txn txn = txnManager.get().beginTransaction();

                try {
                    ChainingHash ch = (blockId == null) ?
                            dsFactory.get().newChainingHash(txn) :
                            dsFactory.get().loadChainingHashForWrites(blockId, txn);

                    logger.info("{}", ch.toString());

                    ch.put(key, value, txn);
                    blockId = ch.getId();
                } finally {
                    txn.commit();
                }

                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    try (Output out = new Output(baos)) {
                        Kryo kryo = kryoPool.borrow();
                        try {
                            kryo.writeClassAndObject(out, requestId);
                            kryo.writeClassAndObject(out, blockId);
                        } finally {
                            kryoPool.release(kryo);
                        }
                    }
                    messenger.get().send(fromNode, PutResponseMessageListener.TOPIC, baos.toByteArray());
                } catch (IOException xcp) {
                    throw new RuntimeException(xcp);
                }

            } catch (IOException xcp) {
                throw new RuntimeException(xcp);
            }
        }
    }

    static class PutResponseMessageListener extends BaseMessageListener {
        // wire format:
        //  1. requestId
        //  2. blockId
        final static String TOPIC = "resp:P";

        @Override
        public void messageReceived(short fromNode, byte[] bytes) {
            try {
                String requestId;
                Long blockId;
                try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
                    try (Input in = new Input(bais)) {
                        Kryo kryo = kryoPool.borrow();
                        try {
                            requestId = (String) kryo.readClassAndObject(in);
                            blockId = (Long) kryo.readClassAndObject(in);
                        } finally {
                            kryoPool.release(kryo);
                        }
                    }
                }

                Pair<CountDownLatch, Long> oldPair = outstandingPutRequests.get(requestId);
                outstandingPutRequests.put(requestId, Pair.of(oldPair.getLeft(), blockId));
                oldPair.getLeft().countDown();
            } catch (IOException xcp) {
                throw new RuntimeException(xcp);
            }
        }
    }

    static class GetRequestMessageListener extends BaseMessageListener {
        // wire format:
        //  1. key
        //  2. request id
        //  3. blockId
        final static String TOPIC = "req:G";

        private final Provider<InternalDataStructureFactory> dsFactory;
        private final Provider<Messenger> messenger;

        GetRequestMessageListener(Provider<InternalDataStructureFactory> dsFactory, Provider<Messenger> messenger) {
            this.dsFactory = dsFactory;
            this.messenger = messenger;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void messageReceived(short fromNode, byte[] bytes) {
            try {
                Comparable key;
                String requestId;
                Long blockId;

                try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
                    try (Input in = new Input(bais)) {
                        Kryo kryo = kryoPool.borrow();
                        try {
                            key = (Comparable) kryo.readClassAndObject(in);
                            requestId = (String) kryo.readClassAndObject(in);
                            blockId = kryo.readObjectOrNull(in, Long.class);
                        } finally {
                            kryoPool.release(kryo);
                        }
                    }
                }

                if (blockId != null) {
                    ChainingHash ch = dsFactory.get().loadChainingHash(blockId);
                    Object value = ch.get(key);

                    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                        try (Output out = new Output(baos)) {
                            Kryo kryo = kryoPool.borrow();
                            try {
                                kryo.writeClassAndObject(out, requestId);
                                if (value == null) {
                                    kryo.writeClass(out, String.class);
                                    kryo.writeObjectOrNull(out, null, String.class);
                                } else {
                                    kryo.writeClassAndObject(out, value);
                                }
                            } finally {
                                kryoPool.release(kryo);
                            }
                        }
                        messenger.get().send(fromNode, GetResponseMessageListener.TOPIC, baos.toByteArray());
                    }
                } else {
                    throw new RuntimeException("blockId is null");
                }

            } catch (IOException xcp) {
                throw new RuntimeException(xcp);
            }
        }
    }

    static class GetResponseMessageListener extends BaseMessageListener {
        // wire format:
        //  1. request id
        //  2. value (optional)
        final static String TOPIC = "resp:G";

        @Override
        public void messageReceived(short i, byte[] bytes) {
            try {
                String requestId;
                Object value;
                try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
                    try (Input in = new Input(bais)) {
                        Kryo kryo = kryoPool.borrow();
                        try {
                            requestId = (String) kryo.readClassAndObject(in);
                            value = kryo.readClassAndObject(in);
                        } finally {
                            kryoPool.release(kryo);
                        }
                    }
                }

                Pair<CountDownLatch, Object> oldPair = outstandingGetRequests.get(requestId);
                outstandingGetRequests.put(requestId, Pair.of(oldPair.getLeft(), value));
                oldPair.getLeft().countDown();
            } catch (IOException xcp) {
                throw new RuntimeException(xcp);
            }
        }
    }
}
