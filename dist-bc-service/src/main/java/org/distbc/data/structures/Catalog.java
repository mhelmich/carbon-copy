package org.distbc.data.structures;

import java.io.IOException;

public interface Catalog {
    void create(String name, TopLevelDataStructure ds) throws IOException;
    <T extends TopLevelDataStructure> T get(String name, Class<T> klass) throws IOException;
}
