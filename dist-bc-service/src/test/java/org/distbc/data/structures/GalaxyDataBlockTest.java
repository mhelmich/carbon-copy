package org.distbc.data.structures;

import co.paralleluniverse.galaxy.Store;
import co.paralleluniverse.galaxy.TimeoutException;
import com.google.inject.Inject;
import org.distbc.GuiceJUnit4Runner;
import org.distbc.GuiceModules;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(GuiceJUnit4Runner.class)
@GuiceModules({ DataStructureModule.class })
public class GalaxyDataBlockTest {

    @Inject
    private Store store;

    @Inject
    private DataStructureFactory dsFactory;

    @Test
    public void testPutGet() throws TimeoutException {
        DataBlock<Integer, Long> db = dsFactory.newDataBlock();
        db.put(123, 123L);
        long dbId = store.put(db, null);
        store.release(dbId);

        // time goes by
        DataBlock<Integer, Long> db2 = dsFactory.newDataBlock();
        store.get(dbId, db2);
        assertEquals(Long.valueOf(123), db2.get(123));
        assertNull(db2.get(125));
    }

    @Test
    public void testCtorWithId() throws TimeoutException {
        DataBlock<Integer, Long> db = dsFactory.newDataBlock();
        db.put(123, 123L);
        long dbId = store.put(db, null);
        store.release(dbId);

        // tic toc tic toc
        DataBlock<Integer, Long> db2 = dsFactory.loadDataBlock(dbId);
        assertEquals(Long.valueOf(123L), db2.get(123));
    }
}
