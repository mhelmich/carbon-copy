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

package org.carbon.copy.resources;

import com.google.inject.Inject;
import org.carbon.copy.calcite.EmbeddedCarbonCopyDriver;
import org.carbon.copy.data.structures.Catalog;
import org.carbon.copy.data.structures.DataStructureFactory;
import org.carbon.copy.data.structures.Txn;
import org.carbon.copy.data.structures.TxnManager;
import org.carbon.copy.dtos.ColumnBuilder;
import org.carbon.copy.dtos.Table;
import org.carbon.copy.dtos.TableBuilder;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

class CarbonCopyResourceImpl implements CarbonCopyResource {
    private final DataStructureFactory dsFactory;
    private final TxnManager txnManager;
    private final Catalog catalog;

    @Inject
    CarbonCopyResourceImpl(DataStructureFactory dsFactory, TxnManager txnManager, Catalog catalog) {
        this.dsFactory = dsFactory;
        this.txnManager = txnManager;
        this.catalog = catalog;
    }

    @Override
    public Set<Object[]> query(String query) throws Exception {
        try (Connection conn = DriverManager.getConnection(EmbeddedCarbonCopyDriver.CONNECT_STRING_PREFIX)) {
            try (Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery(query)) {
                    Set<Object[]> results = new HashSet<>();
                    int numColumns = rs.getMetaData().getColumnCount();
                    while (rs.next()) {
                        Object[] objects = new Object[numColumns];
                        for (int i = 0; i < numColumns; i++) {
                            objects[i] = rs.getObject(i);
                        }
                        results.add(objects);
                    }
                    return results;
                }
            }
        }
    }

    @Override
    public Table createTable(TableBuilder tableBuilder) throws SQLException {
        try {
            org.carbon.copy.data.structures.Table.Builder carbonCopyTableBuilder = org.carbon.copy.data.structures.Table.newBuilder(tableBuilder.name);

            for (ColumnBuilder column : tableBuilder.columnMetadata) {
                carbonCopyTableBuilder.withColumn(column.name, Class.forName(column.typeName));
            }

            org.carbon.copy.data.structures.Table carbonCopyTable = createCarbonCopyTable(carbonCopyTableBuilder);
            return new Table(carbonCopyTable.getName());
        } catch (Exception xcp) {
            throw new SQLException(xcp);
        }
    }

    private org.carbon.copy.data.structures.Table createCarbonCopyTable(org.carbon.copy.data.structures.Table.Builder tableBuilder) throws IOException {
        Txn txn = txnManager.beginTransaction();
        org.carbon.copy.data.structures.Table table = dsFactory.newTable(tableBuilder, txn);
        catalog.create(table, txn);
        txn.commit();
        return table;
    }
}
