package org.distbc.planner;

import org.distbc.parser.ParsingResult;

public interface QueryPlanner {
    QueryPlan generateQueryPlan(ParsingResult parsingResult);
}
