package org.distbc.data.structures;

import co.paralleluniverse.galaxy.Store;

import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Table extends DataStructure {
    private final InternalDataStructureFactory dsFactory;
    private ChainingHash<UUID, Tuple> hash;

    Table(Store store, InternalDataStructureFactory dsFactory, Txn txn) {
        super(store);
        this.dsFactory = dsFactory;
        // create new hash
        hash = dsFactory.newChainingHash(txn);
        txn.addToChangedObjects(hash);
        // wait for it to be upserted and
        // have an id
        hash.checkDataStructureRetrieved();
        // only then upsert yourself
        asyncUpsert(txn);
    }

    Table(Store store, InternalDataStructureFactory dsFactory, long id) {
        super(store, id);
        this.dsFactory = dsFactory;
        asyncLoadForReads();
    }

    Table(Store store, InternalDataStructureFactory dsFactory, long id, Txn txn) {
        super(store, id);
        this.dsFactory = dsFactory;
        asyncLoadForWrites(txn);
    }

    public UUID insert(Tuple tuple, Txn txn) {
        checkDataStructureRetrieved();
        UUID uuid = UUID.randomUUID();
        hash.put(uuid, tuple, txn);
        return uuid;
    }

    public Tuple get(UUID uuid) {
        checkDataStructureRetrieved();
        return hash.get(uuid);
    }

    public Set<Tuple> scan(Predicate<Tuple> predicate) {
        return StreamSupport.stream(hash.keys().spliterator(), false)
                .map(uuid -> hash.get(uuid))
                .filter(predicate)
                .map(Tuple::immutableCopy)
                .collect(Collectors.toSet());
    }

    /////////////////////////////////////////////////////////////
    //////////////////////////////////////////////
    // galaxy-specific serialization overrides

    @Override
    void serialize(SerializerOutputStream out) {
        if (hash != null) {
            out.writeObject(hash.getId());
        }
    }

    @Override
    void deserialize(SerializerInputStream in) {
        Long tmp = (Long) in.readObject();
        hash = dsFactory.loadChainingHash(tmp);
    }
}
