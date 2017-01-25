package org.distbc.data.structures;

import co.paralleluniverse.galaxy.Grid;
import co.paralleluniverse.galaxy.TimeoutException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class GalaxyDataBlockTest {

    private static Grid grid;

    @BeforeClass
    public static void startupGalaxy() {
        try {
            grid = Grid.getInstance("../config/peer.xml", "../config/peer.properties");
            if (!grid.cluster().isOnline()) {
                grid.goOnline();
            }
        } catch (Exception xcp) {
            // when we catch any exception, there's no point in bringing this node online
            // fail the startup
            throw new RuntimeException(xcp);
        }
    }

    @AfterClass
    public static void shutdownGalaxy() {
        grid.cluster().goOffline();
    }

    @Test
    public void testPutGet() throws TimeoutException {
        DataBlock<Integer, Long> db = new DataBlock<>();
        db.put(123, 123L);
        long dbId = grid.store().put(db, null);
        grid.store().release(dbId);
        // time goes by
        DataBlock<Integer, Long> db2 = new DataBlock<>();
        grid.store().get(dbId, db2);
        assertEquals(Long.valueOf(123), db2.get(123));
        assertNull(db2.get(125));
    }

    @Test
    @Ignore // until I figured out how I can get this stupid DI working
    public void testCtorWithId() throws TimeoutException {
        DataBlock<Integer, Long> db = new DataBlock<>();
        db.put(123, 123L);
        long dbId = grid.store().put(db, null);
        grid.store().release(dbId);

        // tic toc tic toc
        DataBlock<Integer, Long> db2 = new DataBlock<>(dbId);
        assertEquals(Long.valueOf(123L), db2.get(123));
    }
}
