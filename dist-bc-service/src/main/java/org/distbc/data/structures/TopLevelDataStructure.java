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

package org.distbc.data.structures;

import co.paralleluniverse.galaxy.Store;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * This acts mostly as insulation to the actual base class DataStructure.
 * I'm a bit hesitant to let the entire world refer to that base class directly.
 * Especially since all direct implementations are generics where all classes,
 * users interact with are not generics (e.g. Index, Table).
 */
public abstract class TopLevelDataStructure extends DataStructure {
    final InternalDataStructureFactory dsFactory;
    private String name;
    // this data holds all column names of this data structure
    // this data structure needs to establish ordering
    // at the very least that's important for indexes
    ChainingHash<String, Tuple> columnMetadata;

    TopLevelDataStructure(Store store, InternalDataStructureFactory dsFactory, Txn txn, String name) {
        super(store);
        this.dsFactory = dsFactory;
        columnMetadata = dsFactory.newChainingHash(txn);
        txn.addToChangedObjects(columnMetadata);
        columnMetadata.checkDataStructureRetrieved();
        addObjectToObjectSize(columnMetadata.getId());
        this.name = name;
        addObjectToObjectSize(name);
    }

    TopLevelDataStructure(Store store, InternalDataStructureFactory dsFactory, long id) {
        super(store, id);
        this.dsFactory = dsFactory;
    }

    public String getName() {
        return name;
    }

    /**
     * The list of column names returned is sorted by the columns appearance
     * in the data structure itself.
     */
    public List<String> getColumnNames() {
        return StreamSupport.stream(columnMetadata.keys().spliterator(), false)
                .map(cn -> columnMetadata.get(cn))
                .sorted(Comparator.comparingInt(o -> (Integer) o.get(1)))
                .map(t -> (String) t.get(0))
                .collect(Collectors.toList());
    }

    public Tuple getColumnMetadataByColumnName(String columnName) {
        return columnMetadata.get(columnName);
    }

    public List<Tuple> getColumnMetadata() {
        return getColumnNames().stream()
                .map(cn -> columnMetadata.get(cn))
                .collect(Collectors.toList());
    }

    /**
     * Tuple structure:
     *  | index | description              | type     |
     *  |-------+--------------------------+----------|
     *  |  0    | column name              | String   |
     *  |  1    | index of column in table | Int      |
     *  |  2    | data type name of column | String   |
     */
    void addColumns(Txn txn, Tuple... columns) {
        checkDataStructureRetrieved();
        for (Tuple column : columns) {
            verifyAddColumnTypes(column);
            String columnName = column.get(0).toString().toUpperCase();
            column.put(0, columnName);
            this.columnMetadata.put(columnName, column, txn);
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

    void verifyDataColumnTypes(Tuple dataTuple) {
        for (String columnName : columnMetadata.keys()) {
            Tuple metadataTuple = columnMetadata.get(columnName);
            Integer idx = (Integer) metadataTuple.get(1);
            String klassName = (String) metadataTuple.get(2);
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

    @Override
    void serialize(SerializerOutputStream out) {
        out.writeObject(name);
        out.writeObject(columnMetadata.getId());
    }

    @Override
    void deserialize(SerializerInputStream in) {
        this.name = (String) in.readObject();
        Long tmp = (Long) in.readObject();
        columnMetadata = dsFactory.loadChainingHash(tmp);
    }
}
