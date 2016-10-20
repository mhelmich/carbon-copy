package org.distbc.data.structures;

import co.paralleluniverse.common.io.Persistable;

/**
 * Created by mhelmich on 10/7/16.
 */
abstract class DataStructure implements Persistable {

    // TODO : kryo business could go here
    // that seems to common across all data structures
    protected static final int MAX_BYTE_SIZE = 32768;
}
