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

package org.carbon.copy.planner;

import com.google.inject.Inject;
import org.carbon.copy.data.structures.Catalog;
import org.carbon.copy.data.structures.DataStructureFactory;
import org.carbon.copy.data.structures.Table;
import org.carbon.copy.data.structures.TempTable;
import org.carbon.copy.data.structures.TopLevelDataStructure;
import org.carbon.copy.data.structures.TxnManager;
import org.carbon.copy.parser.ParsingResult;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.FutureTask;
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

        // figure out which tables have a non-join mutation to them
        // create a swim lane for the respective mutation
        Map<String, UnaryQueryPlanSwimLane> tableNameToSwimLane = tables.stream()
                .collect(Collectors.toMap(
                        TopLevelDataStructure::getName,
                        table -> {
                            UnaryQueryPlanSwimLane sl = new UnaryQueryPlanSwimLane(dsFactory, txnManager, table);
                            // TODO -- do more figuring out here
                            // we need to make sure that we only use columns that exist in this table
                            // also take care of fully qualified column names (something like t1.column1)
                            Set<String> columnsToSelectOn = parsingResult.getSelections().stream()
                                    .map(bo -> bo.operand1)
                                    .collect(Collectors.toSet());
                            sl.addSelection(columnsToSelectOn, table, parsingResult.getExpressionText());
                            sl.addProjection(parsingResult.getProjectionColumnNames(), table.getColumnNames());
                            qp.addSwimLane(sl);
                            return sl;
                        }
                ));

        List<ParsingResult.BinaryOperation> joins = orderJoinsInExecutionOrder(parsingResult);
        joins.forEach(bo -> {
            String[] left = bo.operand1.split("\\.");
            String[] right = bo.operand2.split("\\.");

            String leftTableName = left[0];
            String leftColumnName = left[1];
            String rightTableName = right[0];
            String rightColumnName = right[1];

            UnaryQueryPlanSwimLane leftSwimLane = tableNameToSwimLane.get(leftTableName);
            FutureTask<TempTable> leftFuture = new FutureTask<>(leftSwimLane);
            UnaryQueryPlanSwimLane rightSwimLane = tableNameToSwimLane.get(rightTableName);
            FutureTask<TempTable> rightFuture = new FutureTask<>(rightSwimLane);

            BinaryQueryPlanSwimLane sl = new BinaryQueryPlanSwimLane(dsFactory, txnManager, leftFuture, leftColumnName, rightFuture, rightColumnName);
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

    private List<ParsingResult.BinaryOperation> orderJoinsInExecutionOrder(ParsingResult parsingResult) {
        return parsingResult.getJoins();
    }
}
