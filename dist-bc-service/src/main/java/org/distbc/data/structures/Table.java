package org.distbc.data.structures;

import co.paralleluniverse.galaxy.Store;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Table extends DataStructure {
    private final InternalDataStructureFactory dsFactory;
    private ChainingHash<GUID, Tuple> hash;

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

    public GUID insert(Tuple tuple, Txn txn) {
        checkDataStructureRetrieved();
        hash.put(tuple.getGuid(), tuple, txn);
        return tuple.getGuid();
    }

    public Tuple get(GUID guid) {
        checkDataStructureRetrieved();
        return hash.get(guid);
    }

    public Set<Tuple> scan(Predicate<Tuple> predicate) {
        return StreamSupport.stream(hash.keys().spliterator(), false)
                .map(guid -> hash.get(guid))
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
