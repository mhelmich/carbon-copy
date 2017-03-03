package org.distbc.data.structures;

import co.paralleluniverse.galaxy.Cluster;
import co.paralleluniverse.galaxy.Grid;
import co.paralleluniverse.galaxy.Messenger;
import co.paralleluniverse.galaxy.Store;
import com.google.inject.AbstractModule;

/**
 * This class configures all things data structures and below
 */
public class DataStructureModule extends AbstractModule {
    @Override
    protected void configure() {
        Grid g;
        try {
            g = Grid.getInstance("../config/peer.xml", "../config/peer.properties");
            if (!g.cluster().isOnline()) {
                g.goOnline();
            }
        } catch (Exception xcp) {
            // when we catch any exception, there's no point in bringing this node online
            // fail the startup
            throw new RuntimeException(xcp);
        }

        bind(Store.class).toInstance(g.store());
        bind(Cluster.class).toInstance(g.cluster());
        bind(Messenger.class).toInstance(g.messenger());

        bind(InternalDataStructureFactory.class).to(DataStructureFactoryImpl.class);
        bind(DataStructureFactory.class).to(DataStructureFactoryImpl.class);
    }
}
