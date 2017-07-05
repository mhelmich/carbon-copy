package org.carbon.copy.data.structures;

import co.paralleluniverse.galaxy.Cluster;
import co.paralleluniverse.galaxy.Grid;
import co.paralleluniverse.galaxy.Messenger;
import co.paralleluniverse.galaxy.Store;

class GalaxyGridImpl implements GalaxyGrid {

    private final Grid grid;

    GalaxyGridImpl(String configFile, String propertiesFile) {
        try {
            this.grid = Grid.getInstance(configFile, propertiesFile);
        } catch (InterruptedException xcp) {
            throw new RuntimeException(xcp);
        }
    }

    @Override
    public Store store() {
        return grid.store();
    }

    @Override
    public Messenger messenger() {
        return grid.messenger();
    }

    @Override
    public Cluster cluster() {
        return grid.cluster();
    }

    @Override
    public void start() throws InterruptedException {
        grid.goOnline();
    }

    @Override
    public void stop() {
        grid.cluster().goOffline();
    }

    @Override
    public boolean isStarted() {
        return grid.cluster().isOnline();
    }
}
