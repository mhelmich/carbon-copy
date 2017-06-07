/*
 *
 *  Copyright 2017 Marco Helmich
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.carbon.copy.data.structures;

import co.paralleluniverse.galaxy.Store;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Index extends TopLevelDataStructure {
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

    public Stream<Tuple> keys() {
        return StreamSupport.stream(bTree.keys().spliterator(), false);
    }

    public static Builder newBuilder(String name) {
        return new Builder(name);
    }

    public static class Builder {
        private List<Tuple> columnMetadata = new ArrayList<>();
        private final String name;

        private Builder(String name) {
            this.name = name;
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
