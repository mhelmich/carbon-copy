/*
 * Copyright 2017 Marco Helmich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
