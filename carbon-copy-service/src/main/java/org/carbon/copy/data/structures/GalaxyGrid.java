package org.carbon.copy.data.structures;

import co.paralleluniverse.galaxy.Cluster;
import co.paralleluniverse.galaxy.Messenger;
import co.paralleluniverse.galaxy.Store;

public interface GalaxyGrid {
    Store store();
    Messenger messenger();
    Cluster cluster();
    void start() throws InterruptedException;
    void stop();
    boolean isStarted();
}
