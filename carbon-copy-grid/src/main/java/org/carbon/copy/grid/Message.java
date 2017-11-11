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

import io.netty.buffer.ByteBuf;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

abstract class Message implements Externalizable, Cloneable {

    // NEVER CHANGE THE ORDINALS!!!
    // first parameter is the ordinal
    // the second parameter is the message byte size after reading the first type byte
    enum MessageType {
        PUT((byte)0, 999),
        GET((byte)1, 10),
        ACK((byte)2, 0),
        INVALIDATE((byte)3, 999),
        INVALIDATE_ACK((byte)4, 999),
        BACKUP((byte)5, 999),
        BACUP_ACK((byte)6, 999);

        private final static Map<Byte, MessageType> lookUp = new HashMap<>(MessageType.values().length);
        static {
            Set<Byte> ordinals = new HashSet<>(MessageType.values().length);
            for (MessageType type : MessageType.values()) {
                lookUp.put(type.ordinal, type);

                if (!ordinals.add(type.ordinal)) {
                    throw new RuntimeException("Can't add ordinal " + type.ordinal + " twice!");
                }
            }
        }

        final byte ordinal;
        final int byteSize;
        MessageType(byte ordinal, int byteSize) {
            this.ordinal = ordinal;
            this.byteSize = byteSize;
        }

        static MessageType fromByte(byte b) {
            return lookUp.get(b);
        }
    }

    private final static AtomicLong messageIdGenerator = new AtomicLong(Long.MIN_VALUE);

    final MessageType type;
    private long messageId;
    // this field has two semantics
    // 1. in a request it is the sender
    // 2. in a response it's the receiver
    private short node;

    Message(MessageType type, Message inResponseTo) {
        this(type, inResponseTo.messageId, inResponseTo.node);
    }

    private Message(MessageType type, long messageId, short node) {
        this.type = type;
        this.messageId = messageId;
        this.node = node;
    }

    long getMessageId() {
        return messageId;
    }

    short getNode() {
        return node;
    }

    private static long newMessageId() {
        try {
            return messageIdGenerator.incrementAndGet();
        } catch (Exception xcp) {
            messageIdGenerator.set(Long.MIN_VALUE);
            return messageIdGenerator.incrementAndGet();
        }
    }

    static Message newMessage(MessageType type, ByteBuf bites) {
        switch (type) {
            case GET:
                return new GET(bites.readShort(), bites.readLong());
            default:
                throw new RuntimeException("Unknown message type");
        }
    }

    static abstract class Request extends Message {
        Request(MessageType type, short toNodeId) {
            super(type, Message.newMessageId(), toNodeId);
        }
    }

    static abstract class Response extends Message {
        Response(MessageType type, Message inResponseTo) {
            super(type, inResponseTo);
        }
    }

    static class GET extends Message {
        private long lineId;

        GET(short node, long lineId) {
            super(MessageType.GET, Message.newMessageId(), node);
            this.lineId = lineId;
        }

        // convenience ctor
        // I got tired of the constant casting
        GET(int node, int lineId) {
            this((short)node, (long)lineId);
        }

        public GET() {
            this(-1, -1);
        }

        long getLineId() {
            return lineId;
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {

        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

        }
    }

    static class ACK extends Message {
        ACK(Message inResponseTo) {
            super(MessageType.ACK, inResponseTo);
        }

        public ACK() {
            super(MessageType.ACK, Message.newMessageId(), (short)-1);
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {

        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

        }
    }
}
