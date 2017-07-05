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

package org.carbon.copy;

import com.google.inject.Inject;
import org.carbon.copy.data.structures.Catalog;
import org.carbon.copy.data.structures.DataStructureFactory;
import org.carbon.copy.data.structures.Txn;
import org.carbon.copy.data.structures.TxnManager;
import org.carbon.copy.dtos.ColumnBuilder;
import org.carbon.copy.dtos.Table;
import org.carbon.copy.dtos.TableBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

class CarbonCopyResourceImpl implements CarbonCopyResource {
    private static Logger logger = LoggerFactory.getLogger(CarbonCopyResourceImpl.class);
    private static final Map<String, Long> namesToId = new ConcurrentHashMap<>();

    private final DataStructureFactory dsFactory;

    private final TxnManager txnManager;

    private final Catalog catalog;

    @Inject
    CarbonCopyResourceImpl(DataStructureFactory dsFactory, TxnManager txnManager, Catalog catalog) {
        this.dsFactory = dsFactory;
        this.txnManager = txnManager;
        this.catalog = catalog;
    }

    public Set<Object> query(String query) throws Exception {
//        ParsingResult pr = queryParser.parse(query);
//        logger.info("All the tables I want to access: {}", StringUtils.join(pr.getTableNames(), ", "));
//        logger.info("All the columns I want to access: {}", StringUtils.join(pr.getProjectionColumnNames(), ", "));
//        TempTable tuples = queryPlanner.generateQueryPlan(pr).execute(es);
//        logger.info("#tuples {}", tuples.size());
//        return tuples.keys().collect(Collectors.toSet());
        return Collections.emptySet();
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
