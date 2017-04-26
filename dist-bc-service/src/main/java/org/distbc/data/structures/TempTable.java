package org.distbc.data.structures;

import co.paralleluniverse.galaxy.Store;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * This is the only mutable instance of a top level data structure class.
 * The idea is that TempTables are used for query processing and the data in them shrinks or expands with
 * every query element that is being executed on them.
 * They serve as intermediate query result as well as final result set that can be queried continuously.
 */
public class TempTable extends TopLevelDataStructure {
    // this data holds all the data
    private ChainingHash<GUID, Tuple> data;

    /**
     * This loads an existing TempTable from an id.
     */
    TempTable(Store store, InternalDataStructureFactory dsFactory, long id, Txn txn) {
        super(store, dsFactory, id);
        asyncLoadForWrites(txn);
    }

    /**
     * This creates a new TempTable from an existing Table.
     * This is clearly different from the upper ctor where we want to load an existing result set or something.
     * This is intended to be called at the start of every query.
     */
    TempTable(Store store, InternalDataStructureFactory dsFactory, Table tableToWrap, Txn txn) {
        super(store, dsFactory, txn, "temp_" + tableToWrap.getId() + "_" + UUID.randomUUID().toString());
        // create new data
        data = dsFactory.newChainingHash(txn);
        txn.addToChangedObjects(data);
        // wait for it to be upserted and
        // have an id
        data.checkDataStructureRetrieved();
        addObjectToObjectSize(data.getId());
        // only then upsert yourself
        asyncUpsert(txn);
        checkDataStructureRetrieved();
        // make a copy of the underlying table...for now
        // this adds all columns of the original table in the temp table
        tableToWrap.getColumnNames().stream()
                .map(tableToWrap::getColumnMetadataByColumnName)
                .forEach(tuple -> addColumns(txn, tuple));
        // this copies the data which is hugely inefficient but good for now
        tableToWrap.keys()
                .forEach(guid -> insert(tableToWrap.getMutable(guid), txn));
    }

//    TempTable(Store store, InternalDataStructureFactory dsFactory, Index indexToWrap, Txn txn) {
//        super(store, dsFactory, txn, "temp_" + indexToWrap.getId() + "_" + UUID.randomUUID().toString());
//        // create new data
//        data = dsFactory.newChainingHash(txn);
//        txn.addToChangedObjects(data);
//        // wait for it to be upserted and
//        // have an id
//        data.checkDataStructureRetrieved();
//        addObjectToObjectSize(data.getId());
//        // only then upsert yourself
//        asyncUpsert(txn);
//        checkDataStructureRetrieved();
//        // make a copy of the underlying table...for now
//        // this adds all columns of the original table in the temp table
//        indexToWrap.getColumnNames().stream()
//                .map(indexToWrap::getColumnMetadataByColumnName)
//                .forEach(tuple -> addColumns(txn, tuple));
//        // TODO
//        // this copies the data which is hugely inefficient but good for now
//        indexToWrap.keys()
//                .forEach(guid -> insert(indexToWrap.getMutable(guid), txn));
//    }

    @Override
    public List<String> getColumnNames() {
        return null;
    }

    public void insert(Tuple tuple, Txn txn) {
        checkDataStructureRetrieved();
        verifyDataColumnTypes(tuple);
        data.put(tuple.getGuid(), tuple, txn);
    }

    public Stream<GUID> keys() {
        checkDataStructureRetrieved();
        return StreamSupport.stream(data.keys().spliterator(), false);
    }

    public Tuple get(GUID guid) {
        checkDataStructureRetrieved();
        Tuple t = data.get(guid);
        return (t != null) ? t.immutableCopy() : null;
    }

    /////////////////////////////////////////////////////////////
    //////////////////////////////////////////////
    // galaxy-specific serialization overrides

    @Override
    void serialize(SerializerOutputStream out) {
        super.serialize(out);
        if (data != null) {
            out.writeObject(data.getId());
        }
    }

    @Override
    void deserialize(SerializerInputStream in) {
        super.deserialize(in);
        Long tmp = (Long) in.readObject();
        data = dsFactory.loadChainingHash(tmp);
    }
}
