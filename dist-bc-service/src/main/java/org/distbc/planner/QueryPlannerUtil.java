package org.distbc.planner;

import org.distbc.parser.ParsingResult;

public class QueryPlannerUtil {
    private static final QueryPlannerUtil instance = new QueryPlannerUtil();
    private QueryPlannerUtil() {}

    public static QueryPlannerUtil get() {
        return instance;
    }

    public QueryPlan generateQueryPlan(ParsingResult parsingResult) {
        return new QueryPlanImpl();
    }
}
