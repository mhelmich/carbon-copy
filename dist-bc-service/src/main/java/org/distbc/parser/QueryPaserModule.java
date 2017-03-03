package org.distbc.parser;

import com.google.inject.AbstractModule;

public class QueryPaserModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(QueryParser.class).to(QueryParserImpl.class);
    }
}
