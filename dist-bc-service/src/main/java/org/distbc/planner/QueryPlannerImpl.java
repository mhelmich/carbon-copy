package org.distbc.planner;

import com.google.inject.Inject;
import org.distbc.data.structures.Catalog;
import org.distbc.data.structures.Table;
import org.distbc.data.structures.TopLevelDataStructure;
import org.distbc.data.structures.Tuple;
import org.distbc.parser.ParsingResult;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class QueryPlannerImpl implements QueryPlanner {

    private final Catalog catalog;

    @Inject
    QueryPlannerImpl(Catalog catalog) {
        this.catalog = catalog;
    }

    @Override
    public QueryPlan generateQueryPlan(ParsingResult parsingResult) {
        List<Table> tables = parsingResult.getTableNames().stream()
                .map(tableName -> {
                    try {
                        return catalog.get(tableName, Table.class);
                    } catch (IOException xcp) {
                        throw new RuntimeException(xcp);
                    }
                })
                .sorted(Comparator.comparing(TopLevelDataStructure::getName))
                .collect(Collectors.toList());

        Predicate<Tuple> p = null;
        QueryPlanImpl qp = new QueryPlanImpl();

        for (Table table : tables) {
            qp.addQueryableAndPredicate(table, p);
        }

        return qp;
    }
}
