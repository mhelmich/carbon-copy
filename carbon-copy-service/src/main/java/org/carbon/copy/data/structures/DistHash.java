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
import co.paralleluniverse.galaxy.Store;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.hash.Funnel;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.inject.Provider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This is an implementation of a distributed hash map.
 * It achieves distribution by agreeing on a node for a key by rendez-vous hashing and message-passing.
 * This guarantees that the key in question is at most one hop away.
 * As galaxy sends messages using the same mechanism as it uses the reach consensus about data, messages
 * are pretty much guaranteed to arrive as long as data updates are delivered and ordered with those data updates.
 * In theory this means a nice order and *somewhat* consistent view on data.
 */
class DistHash<Key extends Comparable<Key>, Value> extends DataStructure {
    private final Funnel<Key> keyFunnel = (Funnel<Key>) (key, into) ->
            into.putString(key.toString(), Charsets.UTF_8);

    private final Funnel<Short> nodeInfoFunnel = (Funnel<Short>) (nodeId, into) ->
            into.putShort(nodeId);

    private final InternalDataStructureFactory dsFactory;
    private final Cluster cluster;
    private final Messenger messenger;
    private final HashFunction hf;

    private int hashTableSize;
    private HashMap<Short, Long> hashTable;

    DistHash(Store store, InternalDataStructureFactory dsFactory, Cluster cluster, Messenger messenger, Txn txn) {
        super(store);
        this.hashTableSize = cluster.getNodes().size();
        this.hashTable = new HashMap<>(this.hashTableSize);
        asyncUpsert(txn);

        this.dsFactory = dsFactory;
        this.cluster = cluster;
        this.messenger = messenger;
        this.hf = Hashing.murmur3_128(getMyNodeId(cluster));

        checkDataStructureRetrieved();
        txn.addToChangedObjects(this);
        txn.addToCreatedObjects(this);
    }

    DistHash(Store store, InternalDataStructureFactory dsFactory, Cluster cluster, Messenger messenger, long id) {
        super(store, id);
        this.dsFactory = dsFactory;
        this.cluster = cluster;
        this.messenger = messenger;
        this.hf = Hashing.murmur3_128(getMyNodeId(cluster));
        asyncLoadForReads();
    }

    public void put(Key key, Value val, Txn txn) {
        if (txn == null) throw new IllegalArgumentException("Txn cannot be null");
        checkDataStructureRetrieved();
        Short nodeId = rendezVousHashTheKeyToANode(key);
        Long blockId = hashTable.get(nodeId);
        try {
            blockId = sendPutRequest(nodeId, key, val, blockId);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
        hashTable.put(nodeId, blockId);
    }

    private Long sendPutRequest(Short nodeId, Key key, Value val, Long blockId) throws InterruptedException, ExecutionException, TimeoutException {
        PutRequest pr = new PutRequest(key, val, blockId);
        Future<Long> f = messenger.send(nodeId, pr);
        return f.get(TIMEOUT_SECS, TimeUnit.SECONDS);
    }

    public Value get(Key key) {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");
        checkDataStructureRetrieved();
        try {
            return sendGetRequest(key);
        } catch (InterruptedException | ExecutionException | TimeoutException xcp) {
            throw new RuntimeException(xcp);
        }
    }

    private Value sendGetRequest(Key key) throws InterruptedException, ExecutionException, TimeoutException {
        Short nodeId = rendezVousHashTheKeyToANode(key);
        Long blockId = hashTable.get(nodeId);

        // if the blockId is null, there's no record
        if (blockId == null) {
            return null;
        }

        GetRequest getReq = new GetRequest(key, blockId);
        Future<Value> f = messenger.send(nodeId, getReq);
        return f.get(TIMEOUT_SECS, TimeUnit.SECONDS);
    }

    public boolean delete(Key key, Txn txn) {
        if (txn == null) throw new IllegalArgumentException("Txn cannot be null");
        checkDataStructureRetrieved();
        return true;
    }

    /**
     * This is equivalent of a distributed full table scan.
     * It might make sense to move all the data since that's what will happen anyway.
     * So no message-passing here. On top of that this is also a very lazy way to implement this :)
     */
    public Iterable<Key> keys() {
        return () -> new Iterator<Key>() {
            private int iterHashTableIndex = 0;
            private int iterHashTableSize = hashTable.size();
            private List<Long> iterHashTable = new ArrayList<>(hashTable.values());
            private Iterator<Key> iter;

            @Override
            public boolean hasNext() {
                if ((iter == null || !iter.hasNext()) && iterHashTableIndex < iterHashTableSize) {
                    do {
                        Long nextHashRoot = iterHashTable.get(iterHashTableIndex);
                        iterHashTableIndex++;
                        ChainingHash<Key, Value> ch = dsFactory.loadChainingHash(nextHashRoot);
                        iter = ch.keys().iterator();
                        if (iter.hasNext()) {
                            return true;
                        }
                    } while (iterHashTableIndex < iterHashTableSize);
                }
                return iter != null && iter.hasNext();
            }

            @Override
            public Key next() {
                return iter.next();
            }
        };
    }

    // my little implementation of rendez-vous hashing
    Short rendezVousHashTheKeyToANode(Key key) {
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
    // messaging in galaxy is cool as a mechanism
    // but a little cumbersome to implement
    // there is listeners listening to topics and galaxy dispatches
    // messages of a particular topic to the respective messenger
    // mostly though you want to have conversations with other nodes
    // and therefore you need to implement a protocol of messages sent back and forth
    // this clutters up a little bit but I guess that's what it is
    // until I sit down and build something like the avro protocol servers

    private static final String PUT_REQUEST_TOPIC = "req:P";
    private static final String PUT_RESPONSE_TOPIC = "resp:P";
    private static final String GET_REQUEST_TOPIC = "req:G";
    private static final String GET_RESPONSE_TOPIC = "resp:G";

    /**
     * This class is a data container for a put request.
     * It's mostly used as encapsulation of de-serialization logic
     * and the wire format.
     */
    static class PutRequest extends BaseMessage {
        // wire format:
        //  1. requestId
        //  2. key
        //  3. value
        //  4. blockId (optional)
        final Comparable key;
        final Object value;
        final Long blockId;

        // data constructor for sending a message
        PutRequest(Comparable key, Object value, Long blockId) {
            this.key = key;
            this.value = value;
            this.blockId = blockId;
        }

        // constructor for receiving a put request
        // and converting that into a pojo
        PutRequest(byte[] bytes) {
            try (In in = getIn(bytes)) {
                this.requestId = (UUID) in.read();
                this.key = (Comparable) in.read();
                this.value = in.read();
                this.blockId = (Long) in.read();
            } catch (Exception xcp) {
                throw new RuntimeException(xcp);
            }
        }

        @Override
        String getTopic() {
            return PUT_REQUEST_TOPIC;
        }

        @Override
        void toBytes(Out out) {
            out.write(requestId);
            out.write(key);
            out.write(value);
            out.write(blockId);
        }
    }

    /**
     * data container for the response to a put request
     */
    static class PutResponse extends BaseMessage {
        // wire format:
        //  1. requestId
        //  2. blockId
        final Long blockId;

        PutResponse(Long blockId) {
            this.blockId = blockId;
        }

        PutResponse(byte[] bytes) {
            try (In in = getIn(bytes)) {
                this.requestId = (UUID) in.read();
                this.blockId = (Long) in.read();
            } catch (Exception xcp) {
                throw new RuntimeException(xcp);
            }
        }

        @Override
        String getTopic() {
            return PUT_RESPONSE_TOPIC;
        }

        @Override
        void toBytes(Out out) {
            out.write(requestId);
            out.write(blockId);
        }
    }

    /**
     * This listener receives a put request (including key and value).
     * The recipient of this message executes the request locally
     * and replies with the root of the hash containing the value.
     */
    static class PutRequestMessageListener extends BaseMessageListener {
        final static String TOPIC = PUT_REQUEST_TOPIC;

        private final Provider<InternalDataStructureFactory> dsFactory;
        private final Provider<TxnManager> txnManager;
        private final Provider<Messenger> messenger;

        PutRequestMessageListener(
                Provider<InternalDataStructureFactory> dsFactory,
                Provider<TxnManager> txnManager,
                Provider<Messenger> messenger
        ) {
            this.dsFactory = dsFactory;
            this.txnManager = txnManager;
            this.messenger = messenger;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void handle(short fromNode, byte[] bytes) throws IOException {
            PutRequest req = new PutRequest(bytes);

            Long blockId;
            Txn txn = beginTransaction();
            try {
                ChainingHash ch = (req.blockId == null) ?
                        newChainingHash(txn) :
                        loadChainingHashForWrites(req.blockId, txn);

                put(ch, req.key, req.value, txn);
                blockId = ch.getId();
            } finally {
                txn.commit();
            }

            PutResponse resp = new PutResponse(blockId);
            replyTo(fromNode, req.requestId, resp);
        }

        @SuppressWarnings("unchecked")
        protected void put(ChainingHash ch, Comparable key, Object value, Txn txn) {
            ch.put(key, value, txn);
        }

        protected void replyTo(short toNode, UUID requestId, BaseMessage messageToSend) {
            messenger.get().replyTo(toNode, requestId, messageToSend);
        }

        protected Txn beginTransaction() {
            return txnManager.get().beginTransaction();
        }

        protected ChainingHash newChainingHash(Txn txn) {
            return dsFactory.get().newChainingHash(txn);
        }

        protected ChainingHash loadChainingHashForWrites(long blockId, Txn txn) {
            return dsFactory.get().loadChainingHashForWrites(blockId, txn);
        }
    }

    static class PutResponseMessageListener extends BaseMessageListener {
        // wire format:
        //  1. requestId
        //  2. blockId
        final static String TOPIC = PUT_RESPONSE_TOPIC;

        private final Provider<Messenger> messenger;

        PutResponseMessageListener(Provider<Messenger> messenger) {
            this.messenger = messenger;
        }

        @Override
        protected void handle(short fromNode, byte[] bytes) {
            PutResponse resp = new PutResponse(bytes);
            complete(resp.requestId, resp.blockId);
        }

        protected void complete(UUID requestId, Object blockId) {
            messenger.get().complete(requestId, blockId);
        }
    }

    static class GetRequest extends BaseMessage {
        // wire format:
        //  1. request id
        //  2. key
        //  3. blockId
        final Comparable key;
        final Long blockId;

        GetRequest(Comparable key, Long blockId) {
            this.key = key;
            this.blockId = blockId;
        }

        GetRequest(byte[] bytes) {
            try (In in = getIn(bytes)) {
                this.requestId = (UUID) in.read();
                this.key = (Comparable) in.read();
                this.blockId = (Long) in.read();
            } catch (Exception xcp) {
                throw new RuntimeException(xcp);
            }
        }

        @Override
        String getTopic() {
            return GET_REQUEST_TOPIC;
        }

        @Override
        void toBytes(Out out) {
            out.write(requestId);
            out.write(key);
            out.write(blockId);
        }
    }

    static class GetResponse extends BaseMessage {
        // wire format:
        //  1. request id
        //  2. value (optional)
        final Object value;

        GetResponse(Object value) {
            this.value = value;
        }

        GetResponse(byte[] bytes) {
            try (In in = getIn(bytes)) {
                this.requestId = (UUID) in.read();
                this.value = in.read();
            } catch (Exception xcp) {
                throw new RuntimeException(xcp);
            }
        }

        @Override
        String getTopic() {
            return GET_RESPONSE_TOPIC;
        }

        @Override
        void toBytes(Out out) {
            out.write(requestId);
            out.write(value);
        }
    }

    static class GetRequestMessageListener extends BaseMessageListener {
        // wire format:
        //  1. key
        //  2. request id
        //  3. blockId
        final static String TOPIC = GET_REQUEST_TOPIC;

        private final Provider<InternalDataStructureFactory> dsFactory;
        private final Provider<Messenger> messenger;

        GetRequestMessageListener(Provider<InternalDataStructureFactory> dsFactory, Provider<Messenger> messenger) {
            this.dsFactory = dsFactory;
            this.messenger = messenger;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void handle(short fromNode, byte[] bytes) throws IOException {
            GetRequest req = new GetRequest(bytes);

            if (req.blockId != null) {
                ChainingHash ch = loadChainingHash(req.blockId);
                Object value = ch.get(req.key);

                GetResponse resp = new GetResponse(value);
                replyTo(fromNode, req.requestId, resp);
            } else {
                throw new RuntimeException("blockId is null");
            }
        }

        protected ChainingHash loadChainingHash(long blockId) {
            return dsFactory.get().loadChainingHash(blockId);
        }

        protected void replyTo(short toNode, UUID requestId, BaseMessage messageToSend) {
            messenger.get().replyTo(toNode, requestId, messageToSend);
        }
    }

    static class GetResponseMessageListener extends BaseMessageListener {
        // wire format:
        //  1. request id
        //  2. value (optional)
        final static String TOPIC = GET_RESPONSE_TOPIC;

        private final Provider<Messenger> messenger;

        GetResponseMessageListener(Provider<Messenger> messenger) {
            this.messenger = messenger;
        }

        @Override
        protected void handle(short fromNode, byte[] bytes) throws IOException {
            GetResponse resp = new GetResponse(bytes);
            complete(resp.requestId, resp.value);
        }

        protected void complete(UUID requestId, Object value) {
            messenger.get().complete(requestId, value);
        }
    }
}
