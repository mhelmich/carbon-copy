package org.distbc.data.structures;

import com.esotericsoftware.kryo.DefaultSerializer;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

/**
 * This the public interface of a global unique id.
 * Right now it's just a wrapper around a java UUID. This might change in the future.
 */
@DefaultSerializer(GUID.GUIDSerializer.class)
public class GUID extends Sizable implements Comparable<GUID> {
    private final UUID internalId;

    @SuppressWarnings("unused")
    private GUID(UUID uuid) {
        this.internalId = uuid;
    }

    private GUID() {
        internalId = UUID.randomUUID();
        addObjectToObjectSize(internalId.getMostSignificantBits());
        addObjectToObjectSize(internalId.getLeastSignificantBits());
    }

    public static GUID randomGUID() {
        return new GUID();
    }

    @Override
    public String toString() {
        return internalId.toString();
    }

    @Override
    public int hashCode() {
        return internalId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if ((null == obj) || (obj.getClass() != GUID.class)) {
            return false;
        }
        GUID id = (GUID)obj;
        return this.internalId.equals(id.internalId);
    }

    @Override
    public int compareTo(GUID val) {
        return this.internalId.compareTo(val.internalId);
    }

    public final static class GUIDSerializer extends Serializer<GUID> {
        @Override
        public void write(Kryo kryo, Output output, GUID guid) {
            kryo.writeClassAndObject(output, guid.internalId);
        }

        @Override
        public GUID read(Kryo kryo, Input input, Class<GUID> aClass) {
            UUID uuid = (UUID) kryo.readClassAndObject(input);
            try {
                Constructor<GUID> ctor = aClass.getDeclaredConstructor(UUID.class);
                ctor.setAccessible(true);
                return ctor.newInstance(uuid);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
