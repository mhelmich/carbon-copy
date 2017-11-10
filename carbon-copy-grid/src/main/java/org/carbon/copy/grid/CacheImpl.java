package org.carbon.copy.grid;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class CacheImpl implements Cache {
    private final Map<Long, CacheLine> owned = new HashMap<>();
    private final Map<Long, CacheLine> shared = new HashMap<>();

    private final ConcurrentHashMap<Short, UdpGridClient> nodeIdToClient = new ConcurrentHashMap<>();
    private final UdpGridServer server;

    CacheImpl(int serverPort) {
        server = new UdpGridServer(serverPort, this);
    }

    void addServer(short nodeId, String host, int port) {
        nodeIdToClient.put(nodeId, new UdpGridClient(host, port, this));
    }

    @Override
    public Message handleMessage(Message request) {
        switch (request.type) {
            case GET:
                return handleGET((Message.GET)request);
            default:
                throw new RuntimeException("I don't know message type " + request.type);
        }
    }

    private Message.ACK handleGET(Message.GET msg) {
        return new Message.ACK(msg);
    }
}
