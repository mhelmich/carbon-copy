package org.carbon.copy.data.structures;

import co.paralleluniverse.galaxy.Store;

class DistHash<Key extends Comparable<Key>, Value> extends DataStructure {
    DistHash(Store store) {
        super(store);
    }

    @Override
    void serialize(SerializerOutputStream out) {

    }

    @Override
    void deserialize(SerializerInputStream in) {

    }
}
