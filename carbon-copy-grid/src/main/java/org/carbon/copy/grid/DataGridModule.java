package org.carbon.copy.grid;

import com.google.inject.AbstractModule;

public class DataGridModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Cache.class).to(CacheImpl.class);
    }
}
