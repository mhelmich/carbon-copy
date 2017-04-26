package org.distbc.data.structures;

import co.paralleluniverse.galaxy.Store;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class Index extends TopLevelDataStructure implements Queryable {
    // this tree holds the index data
    private BTree<Tuple, GUID> bTree;

    Index(Store store, InternalDataStructureFactory dsFactory, Builder builder, Txn txn) {
        super(store, dsFactory, txn, builder.getName());
        // create new tree
        bTree = dsFactory.newBTree(txn);
        txn.addToChangedObjects(bTree);
        // wait for it to be upserted and
        // have an id
        bTree.checkDataStructureRetrieved();
        addObjectToObjectSize(bTree.getId());
        // only then upsert yourself
        asyncUpsert(txn);
        addColumns(txn, builder.getColumnMetadata());
    }

    Index(Store store, InternalDataStructureFactory dsFactory, long id) {
        super(store, dsFactory, id);
        asyncLoadForReads();
    }

    Index(Store store, InternalDataStructureFactory dsFactory, long id, Txn txn) {
        super(store, dsFactory, id);
        asyncLoadForWrites(txn);
    }

    public void insert(Tuple tuple, GUID guid, Txn txn) {
        checkDataStructureRetrieved();
        verifyDataColumnTypes(tuple);
        bTree.put(tuple, guid, txn);
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
        if (bTree != null) {
            out.writeObject(bTree.getId());
        }
    }

    @Override
    void deserialize(SerializerInputStream in) {
        super.deserialize(in);
        Long tmp = (Long) in.readObject();
        bTree = dsFactory.loadBTree(tmp);
    }
}
