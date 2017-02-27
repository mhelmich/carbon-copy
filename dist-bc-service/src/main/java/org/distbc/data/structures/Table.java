package org.distbc.data.structures;

import co.paralleluniverse.galaxy.Store;

import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Table extends DataStructure {
    private final InternalDataStructureFactory dsFactory;
    // this data holds all the data
    private ChainingHash<GUID, Tuple> data;
    // this data holds all column names to index
    private ChainingHash<String, Tuple> columns;

    private Table(Store store, InternalDataStructureFactory dsFactory, Txn txn) {
        super(store);
        this.dsFactory = dsFactory;
        // create new data
        data = dsFactory.newChainingHash(txn);
        columns = dsFactory.newChainingHash(txn);
        txn.addToChangedObjects(data);
        txn.addToChangedObjects(columns);
        // wait for it to be upserted and
        // have an id
        data.checkDataStructureRetrieved();
        columns.checkDataStructureRetrieved();
        // only then upsert yourself
        asyncUpsert(txn);
    }

    Table(Store store, InternalDataStructureFactory dsFactory, Txn txn, Tuple... columns) {
        this(store, dsFactory, txn);
        addColumns(txn, columns);
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
        verifyDataColumnTypes(tuple);
        data.put(tuple.getGuid(), tuple, txn);
        return tuple.getGuid();
    }

    private void verifyDataColumnTypes(Tuple dataTuple) {
        for (String columnName : columns.keys()) {
            Tuple metadataTuple = columns.get(columnName);
            Integer idx = (Integer) metadataTuple.get(0);
            String klassName = (String) metadataTuple.get(1);
            Class klass;
            try {
                klass = Class.forName(klassName);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException(e);
            }
            if (!klass.equals(dataTuple.get(idx).getClass())) {
                throw new IllegalArgumentException();
            }
        }
    }

    public Tuple get(GUID guid) {
        checkDataStructureRetrieved();
        Tuple t = data.get(guid);
        return (t != null) ? t.immutableCopy() : null;
    }

    public Set<Tuple> scan(Predicate<Tuple> predicate) {
        return StreamSupport.stream(data.keys().spliterator(), false)
                .map(guid -> data.get(guid))
                .filter(predicate)
                .map(Tuple::immutableCopy)
                .collect(Collectors.toSet());
    }

    public Set<Tuple> scan(Predicate<Tuple> predicate, Function<Tuple, Tuple> projection) {
        if (projection != null) {
            return StreamSupport.stream(data.keys().spliterator(), false)
                    .map(guid -> data.get(guid))
                    .filter(predicate)
                    .map(projection)
                    .map(Tuple::immutableCopy)
                    .collect(Collectors.toSet());
        } else {
            return scan(predicate);
        }
    }

    /**
     * Tuple structure:
     *  | index | description              | type     |
     *  |-------+--------------------------+----------|
     *  |  0    | column name              | String   |
     *  |  1    | index of column in table | Int      |
     *  |  2    | data type of column      | Class ?? |
     */
    public void addColumns(Txn txn, Tuple... columns) {
        checkDataStructureRetrieved();
        for (Tuple column : columns) {
            verifyAddColumnTypes(column);
            Tuple newTuple = column.subTuple(1, column.getTupleSize());
            this.columns.put(column.get(0).toString().toUpperCase(), newTuple, txn);
        }
    }

    private void verifyAddColumnTypes(Tuple tuple) {
        if (
                tuple.getTupleSize() != 3
                || !String.class.equals(tuple.get(0).getClass())
                || !Integer.class.equals(tuple.get(1).getClass())
                || !String.class.equals(tuple.get(2).getClass())
            ) {
            throw new IllegalArgumentException();
        }
    }

    /////////////////////////////////////////////////////////////
    //////////////////////////////////////////////
    // galaxy-specific serialization overrides

    @Override
    void serialize(SerializerOutputStream out) {
        if (data != null && columns != null) {
            out.writeObject(data.getId());
            out.writeObject(columns.getId());
        }
    }

    @Override
    void deserialize(SerializerInputStream in) {
        Long tmp = (Long) in.readObject();
        data = dsFactory.loadChainingHash(tmp);
        tmp = (Long) in.readObject();
        columns = dsFactory.loadChainingHash(tmp);
    }
}
