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

public class DistHashTest {
    @Test
    public void testHashing() throws IOException {
        DistHash<Integer, String> dh = new DistHash<Integer, String>(Mockito.mock(Store.class), Mockito.mock(Cluster.class), Mockito.mock(Messenger.class), Mockito.mock(Txn.class)) {
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
}
