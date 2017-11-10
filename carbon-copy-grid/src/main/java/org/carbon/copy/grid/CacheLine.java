package org.carbon.copy.grid;

import java.nio.ByteBuffer;
import java.util.Set;

class CacheLine {
    private long id;
    private volatile CacheLineState state;
    private volatile long version;
    private short owner = -1;
    private Set<Short> sharers;
    private byte flags;
    private ByteBuffer data;
}
