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

public class Table extends TopLevelDataStructure {
    // this data holds all the data
    private ChainingHash<GUID, Tuple> data;

    private Table(Store store, InternalDataStructureFactory dsFactory, Txn txn, String dsName) {
        super(store, dsFactory, txn, dsName);
        // create new data
        data = dsFactory.newChainingHash(txn);
        txn.addToChangedObjects(data);
        // wait for it to be upserted and
        // have an id
        data.checkDataStructureRetrieved();
        addObjectToObjectSize(data.getId());
        // only then upsert yourself
        asyncUpsert(txn);
    }

    Table(Store store, InternalDataStructureFactory dsFactory, Builder builder, Txn txn) {
        this(store, dsFactory, txn, builder.getName());
        addColumns(txn, builder.getColumnMetadata());
    }

    Table(Store store, InternalDataStructureFactory dsFactory, long id) {
        super(store, dsFactory, id);
        asyncLoadForReads();
    }

    Table(Store store, InternalDataStructureFactory dsFactory, long id, Txn txn) {
        super(store, dsFactory, id);
        asyncLoadForWrites(txn);
    }

    public GUID insert(Tuple tuple, Txn txn) {
        checkDataStructureRetrieved();
        verifyDataColumnTypes(tuple);
        data.put(tuple.getGuid(), tuple, txn);
        return tuple.getGuid();
    }

    public Stream<GUID> keys() {
        return StreamSupport.stream(data.keys().spliterator(), false);
    }

    public Tuple get(GUID guid) {
        Tuple t = getMutable(guid);
        return (t != null) ? t.immutableCopy() : null;
    }

    Tuple getMutable(GUID guid) {
        checkDataStructureRetrieved();
        return data.get(guid);
    }

//    @Override
//    public Set<Tuple> scan(Predicate<Tuple> predicate) {
//        checkDataStructureRetrieved();
//        return StreamSupport.stream(data.keys().spliterator(), false)
//                .map(guid -> data.get(guid))
//                .filter(predicate)
//                .map(Tuple::immutableCopy)
//                .collect(Collectors.toSet());
//    }
//
//    @Override
//    public Set<Tuple> scan(Predicate<Tuple> predicate, Function<Tuple, Tuple> projection) {
//        checkDataStructureRetrieved();
//        if (projection != null) {
//            return StreamSupport.stream(data.keys().spliterator(), false)
//                    .map(guid -> data.get(guid))
//                    .filter(predicate)
//                    .map(projection)
//                    .map(Tuple::immutableCopy)
//                    .collect(Collectors.toSet());
//        } else {
//            return scan(predicate);
//        }
//    }
//
//    @Override
//    public Set<Tuple> project(Function<Tuple, Tuple> projection) {
//        return StreamSupport.stream(data.keys().spliterator(), false)
//                .map(guid -> data.get(guid))
//                .map(projection)
//                .collect(Collectors.toSet());
//    }

    public static Builder newBuilder(String name) {
        return new Builder(name);
    }

    public static class Builder {
        private List<Tuple> columnMetadata = new ArrayList<>();
        private final String name;

        private Builder(String name) {
            this.name = name.toUpperCase();
        }

        public Builder withColumn(String name, int index, Class type) {
            Tuple col = new Tuple(3);
            col.put(0, name.toUpperCase());
            col.put(1, index);
            col.put(2, type.getCanonicalName());
            columnMetadata.add(col);
            return this;
        }

        public Builder withColumn(String name, Class type) {
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
