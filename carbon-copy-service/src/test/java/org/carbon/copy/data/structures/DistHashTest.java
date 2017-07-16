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
import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;

public class DistHashTest {
    @Test
    public void testHashing() throws IOException {
        DistHash<Integer, String> dh = new DistHash<Integer, String>(Mockito.mock(Store.class), Mockito.mock(InternalDataStructureFactory.class), Mockito.mock(Cluster.class), Mockito.mock(Messenger.class), Mockito.mock(Txn.class)) {
            @Override
            protected Set<Short> getNodes() {
                return ImmutableSet.of(
                        (short)1,
                        (short)45,
                        (short)123
                );
            }

            @Override
            protected short getMyNodeId(Cluster cluster) {
                return (short)456;
            }
        };

        Short nodeId = dh.rendezVousHashTheKeyToANode(17);
        assertEquals(Short.valueOf((short)1), nodeId);

//        SizeOf sizeOf = SizeOf.newInstance();
//        long shallowSize = sizeOf.sizeOf(someObject);
//        long deepSize = sizeOf.deepSizeOf(someObject);
    }

    @Test
    public void testSerialization() {
        String key = "some_string";
        Integer value = 123;
        Long blockId = Long.MAX_VALUE;
        UUID requestId = UUID.randomUUID();

        DistHash.PutRequest putReq = new DistHash.PutRequest(key, value, blockId);
        putReq.setRequestId(requestId);
        DistHash.PutRequest putReq2 = new DistHash.PutRequest(putReq.toByteArray());
        assertEquals(putReq.key, putReq2.key);
        assertEquals(putReq.value, putReq2.value);
        assertEquals(putReq.blockId, putReq2.blockId);
        assertEquals(requestId, putReq2.requestId);

        DistHash.PutResponse putResp = new DistHash.PutResponse(blockId);
        DistHash.PutResponse putResp2 = new DistHash.PutResponse(putResp.toByteArray());
        assertEquals(putResp.blockId, putResp2.blockId);

        DistHash.GetRequest getReq = new DistHash.GetRequest(key, blockId);
        getReq.setRequestId(requestId);
        DistHash.GetRequest getReq2 = new DistHash.GetRequest(getReq.toByteArray());
        assertEquals(getReq.key, getReq2.key);
        assertEquals(getReq.blockId, getReq2.blockId);

        DistHash.GetResponse getResp = new DistHash.GetResponse(key);
        DistHash.GetResponse getResp2 = new DistHash.GetResponse(getResp.toByteArray());
        assertEquals(getResp.value, getResp2.value);
        assertEquals(requestId, putReq2.requestId);
    }


    @Test
    public void testPutRequestResponse() throws IOException {
        String initialKey = "some_string";
        Integer initialValue = 123;
        UUID initialRequestId = UUID.randomUUID();
        Long hashId = 123456789L;
        // use this to stash the message from the mock
        // it can reach into the closure
        BaseMessage[] messageBuffer = new BaseMessage[1];

        DistHash.PutRequest request = new DistHash.PutRequest(initialKey, initialValue, null);
        request.setRequestId(initialRequestId);

        DistHash.PutRequestMessageListener putRequestListener = new DistHash.PutRequestMessageListener(null, null, null) {
            @Override
            protected Txn beginTransaction() {
                return Mockito.mock(Txn.class);
            }

            @Override
            protected ChainingHash newChainingHash(Txn txn) {
                ChainingHash hash = Mockito.mock(ChainingHash.class);
                Mockito.when(hash.getId()).thenReturn(hashId);
                return hash;
            }

            @Override
            protected ChainingHash loadChainingHashForWrites(long blockId, Txn txn) {
                fail();
                return null;
            }

            @Override
            protected void put(ChainingHash ch, Comparable key, Object value, Txn txn) {
                assertEquals(initialKey, key);
                assertEquals(initialValue, value);
            }

            @Override
            protected void replyTo(short toNode, UUID requestId, BaseMessage messageToSend) {
                messageToSend.setRequestId(requestId);
                messageBuffer[0] = messageToSend;
            }
        };
        putRequestListener.messageReceived((short)15, request.toByteArray());

        DistHash.PutResponseMessageListener putResponseListener = new DistHash.PutResponseMessageListener(null) {
            @Override
            protected void complete(UUID requestId, Object blockId) {
                assertEquals(hashId, blockId);
                assertEquals(initialRequestId, requestId);
            }
        };
        putResponseListener.messageReceived((short)17, messageBuffer[0].toByteArray());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetRequestResponse() throws IOException {
        String initialKey = "some_string";
        Integer initialValue = 123;
        UUID initialRequestId = UUID.randomUUID();
        Long hashId = 123456789L;
        // use this to stash the message from the mock
        // it can reach into the closure
        BaseMessage[] messageBuffer = new BaseMessage[1];

        DistHash.GetRequest request = new DistHash.GetRequest(initialKey, hashId);
        request.setRequestId(initialRequestId);

        DistHash.GetRequestMessageListener getRequestListener = new DistHash.GetRequestMessageListener(null, null) {
            @Override
            protected ChainingHash loadChainingHash(long blockId) {
                ChainingHash hash = Mockito.mock(ChainingHash.class);
                Mockito.when(hash.get(anyString())).thenReturn(initialValue);
                return hash;
            }

            @Override
            protected void replyTo(short toNode, UUID requestId, BaseMessage messageToSend) {
                messageToSend.setRequestId(requestId);
                messageBuffer[0] = messageToSend;
            }
        };
        getRequestListener.messageReceived((short)15, request.toByteArray());

        DistHash.GetResponseMessageListener getResponseListener = new DistHash.GetResponseMessageListener(null) {
            @Override
            protected void complete(UUID requestId, Object value) {
                assertEquals(initialRequestId, requestId);
                assertEquals(initialValue, value);
            }
        };
        getResponseListener.messageReceived((short)17, messageBuffer[0].toByteArray());
    }
}
