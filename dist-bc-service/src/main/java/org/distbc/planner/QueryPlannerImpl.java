package org.distbc.planner;

import org.distbc.parser.ParsingResult;

public class QueryPlannerImpl implements QueryPlanner {
    @Override
    public QueryPlan generateQueryPlan(ParsingResult parsingResult) {
        return new QueryPlanImpl();
    }
}
