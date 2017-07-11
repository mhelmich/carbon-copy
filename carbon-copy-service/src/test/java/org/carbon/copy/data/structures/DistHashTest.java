package org.carbon.copy.data.structures;

import co.paralleluniverse.galaxy.Cluster;
import co.paralleluniverse.galaxy.Messenger;
import co.paralleluniverse.galaxy.Store;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Set;

import static org.junit.Assert.assertEquals;

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

        Short nodeId = dh.findNodeInCluster(17);
        assertEquals(Short.valueOf((short)1), nodeId);

//        SizeOf sizeOf = SizeOf.newInstance();
//        long shallowSize = sizeOf.sizeOf(someObject);
//        long deepSize = sizeOf.deepSizeOf(someObject);

    }
}
