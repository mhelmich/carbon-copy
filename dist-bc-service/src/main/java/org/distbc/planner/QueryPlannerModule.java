package org.distbc.planner;

import com.google.inject.AbstractModule;

public class QueryPlannerModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(QueryPlanner.class).to(QueryPlannerImpl.class);
    }
}
