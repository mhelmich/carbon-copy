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

package org.distbc.planner;

import com.google.inject.Inject;
import org.distbc.data.structures.Catalog;
import org.distbc.data.structures.DataStructureFactory;
import org.distbc.data.structures.Table;
import org.distbc.data.structures.TopLevelDataStructure;
import org.distbc.data.structures.TxnManager;
import org.distbc.parser.ParsingResult;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * While every sensible query plan can be imagined as a tree this query planner thinks of as a series of independent
 * query plans (swim lanes) that merge together at some point.
 * Therefore, it starts looking at every table independently and tries to figure out how it can mutate each table.
 * At some point two of these swim lanes merge and become one.
 *
 * Limitations:
 *    - column names need to be unique
 *    - only lower case keywords
 */
class QueryPlannerImpl implements QueryPlanner {

    private final Catalog catalog;
    private final DataStructureFactory dsFactory;
    private final TxnManager txnManager;

    @Inject
    QueryPlannerImpl(Catalog catalog, DataStructureFactory dsFactory, TxnManager txnManager) {
        this.catalog = catalog;
        this.dsFactory = dsFactory;
        this.txnManager = txnManager;
    }

    @Override
    public QueryPlan generateQueryPlan(ParsingResult parsingResult) {
        QueryPlanImpl qp = new QueryPlanImpl();
        List<Table> tables = orderTablesInExecutionOrder(parsingResult);
        tables.forEach(table -> {
            UnaryQueryPlanSwimLane sl = new UnaryQueryPlanSwimLane(dsFactory, txnManager, table);
            // TODO -- do more figuring out here
            // we need to make sure that we only use columns that exist in this table
            Set<String> columnsToSelectOn = parsingResult.getSelections().stream()
                    .map(bo -> bo.operand1)
                    .collect(Collectors.toSet());
            sl.addSelection(columnsToSelectOn, table, parsingResult.getExpressionText());
            sl.addProjection(parsingResult.getProjectionColumnNames(), table.getColumnNames());
            qp.addSwimLane(sl);
        });
        return qp;
    }

    private List<Table> orderTablesInExecutionOrder(ParsingResult parsingResult) {
        return parsingResult.getTableNames().stream()
                .map(tableName -> catalog.get(tableName, Table.class))
                .sorted(Comparator.comparing(TopLevelDataStructure::getName))
                .collect(Collectors.toList());
    }
}
