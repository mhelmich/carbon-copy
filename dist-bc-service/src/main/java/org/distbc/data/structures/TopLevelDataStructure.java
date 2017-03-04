package org.distbc.data.structures;

import co.paralleluniverse.galaxy.Store;

/**
 * This acts mostly as insulation to the actual base class DataStructure.
 * I'm a bit hesitant to let the entire world refer to that base class directly.
 * Especially since all direct implementations are generics where all classes,
 * users interact with are not generics (e.g. Index, Table).
 */
public abstract class TopLevelDataStructure extends DataStructure {
    TopLevelDataStructure(Store store) {
        super(store);
    }

    TopLevelDataStructure(Store store, long id) {
        super(store, id);
    }

    @Override
    abstract void serialize(SerializerOutputStream out);

    @Override
    abstract void deserialize(SerializerInputStream in);
}
