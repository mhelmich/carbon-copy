package org.distbc.data.structures;

import co.paralleluniverse.galaxy.Store;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.function.Function;
import java.util.function.Predicate;

public class Index extends TopLevelDataStructure implements Queryable {
    private final InternalDataStructureFactory dsFactory;
    // this tree holds the index data
    private BTree<Tuple, GUID> bTree;
    // this data holds all column names to index
    private ChainingHash<String, Tuple> columns;
    // this list holds the column names
    private List<String> columnNames;

    Index(Store store, InternalDataStructureFactory dsFactory, Builder builder, Txn txn) {
        super(store, builder.getName());
        this.dsFactory = dsFactory;
        // create new tree
        bTree = dsFactory.newBTree(txn);
        columns = dsFactory.newChainingHash(txn);
        txn.addToChangedObjects(bTree);
        txn.addToChangedObjects(columns);
        // wait for it to be upserted and
        // have an id
        bTree.checkDataStructureRetrieved();
        columns.checkDataStructureRetrieved();
        addObjectToObjectSize(bTree.getId());
        addObjectToObjectSize(columns.getId());
        // only then upsert yourself
        asyncUpsert(txn);
        addColumns(txn, builder.getColumnMetadata());
    }

    Index(Store store, InternalDataStructureFactory dsFactory, long id) {
        super(store, id);
        this.dsFactory = dsFactory;
        asyncLoadForReads();
    }

    Index(Store store, InternalDataStructureFactory dsFactory, long id, Txn txn) {
        super(store, id);
        this.dsFactory = dsFactory;
        asyncLoadForWrites(txn);
    }

    public void insert(Tuple tuple, GUID guid, Txn txn) {
        checkDataStructureRetrieved();
        verifyDataColumnTypes(tuple);
        bTree.put(tuple, guid, txn);
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

    public GUID get(Tuple tuple) {
        checkDataStructureRetrieved();
        verifyDataColumnTypes(tuple);
        return bTree.get(tuple);
    }

    public Iterable<GUID> get(Tuple fromTuple, Tuple toTuple) {
        checkDataStructureRetrieved();
        verifyDataColumnTypes(fromTuple);
        verifyDataColumnTypes(toTuple);
        return bTree.get(fromTuple, toTuple);
    }

    @Override
    public List<String> getColumnNames() {
        if (columnNames == null) {
            Vector<String> v = new Vector<>(1);
            v.setSize(1);
            for (String name : columns.keys()) {
                Tuple t = columns.get(name);
                Integer idx = (Integer) t.get(0);
                v.setSize(Math.max(v.size(), idx + 1));
                v.set(idx, name);
            }
            columnNames = new ArrayList<>(v);
        }

        return columnNames;
    }

    @Override
    public Set<Tuple> scan(Predicate<Tuple> predicate) {
        return Collections.emptySet();
    }

    @Override
    public Set<Tuple> project(Function<Tuple, Tuple> projection) {
        return Collections.emptySet();
    }

    @Override
    public Set<Tuple> scan(Predicate<Tuple> predicate, Function<Tuple, Tuple> projection) {
        return Collections.emptySet();
    }

    /**
     * Tuple structure:
     *  | index | description              | type     |
     *  |-------+--------------------------+----------|
     *  |  0    | column name              | String   |
     *  |  1    | index of column in table | Int      |
     *  |  2    | data type name of column | String   |
     */
    private void addColumns(Txn txn, Tuple... columns) {
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

    public static class Builder {
        private List<Tuple> columnMetadata = new ArrayList<>();
        private final String name;

        private Builder(String name) {
            this.name = name;
        }

        public static Index.Builder newBuilder(String name) {
            return new Builder(name);
        }

        public Index.Builder withColumn(String name, int index, Class type) {
            Tuple col = new Tuple(3);
            col.put(0, name);
            col.put(1, index);
            col.put(2, type.getCanonicalName());
            columnMetadata.add(col);
            return this;
        }

        public Index.Builder withColumn(String name, Class type) {
            return withColumn(name, columnMetadata.size(), type);
        }

        private Tuple[] getColumnMetadata() {
            return columnMetadata.toArray(new Tuple[columnMetadata.size()]);
        }

        private String getName() {
            return name;
        }
    }

    /////////////////////////////////////////////////////////////
    //////////////////////////////////////////////
    // galaxy-specific serialization overrides

    @Override
    void serialize(SerializerOutputStream out) {
        super.serialize(out);
        if (bTree != null && columns != null) {
            out.writeObject(bTree.getId());
            out.writeObject(columns.getId());
        }
    }

    @Override
    void deserialize(SerializerInputStream in) {
        super.deserialize(in);
        Long tmp = (Long) in.readObject();
        bTree = dsFactory.loadBTree(tmp);
        tmp = (Long) in.readObject();
        columns = dsFactory.loadChainingHash(tmp);
    }
}
