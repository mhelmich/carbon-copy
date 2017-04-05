package org.distbc.data.structures;

import co.paralleluniverse.galaxy.Store;

/**
 * This acts mostly as insulation to the actual base class DataStructure.
 * I'm a bit hesitant to let the entire world refer to that base class directly.
 * Especially since all direct implementations are generics where all classes,
 * users interact with are not generics (e.g. Index, Table).
 */
public abstract class TopLevelDataStructure extends DataStructure {
    // not private so that it can be set by implementors
    String name;

    TopLevelDataStructure(Store store, String name) {
        super(store);
        this.name = name;
        addObjectToObjectSize(name);
    }

    TopLevelDataStructure(Store store, long id) {
        super(store, id);
    }

    public String getName() {
        return name;
    }

    @Override
    void serialize(SerializerOutputStream out) {
        out.writeObject(name);
    }

    @Override
    void deserialize(SerializerInputStream in) {
        this.name = (String) in.readObject();
    }
}
