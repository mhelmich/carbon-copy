package org.distbc.data.structures;

import co.paralleluniverse.galaxy.TimeoutException;

import java.io.IOException;

public interface Catalog {
    void create(String name, TopLevelDataStructure ds) throws InterruptedException, TimeoutException, IOException;
    <T extends TopLevelDataStructure> T get(String name, Class<T> klass);
}
