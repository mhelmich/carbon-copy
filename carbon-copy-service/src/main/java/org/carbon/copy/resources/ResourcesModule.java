package org.carbon.copy.resources;

import com.google.inject.AbstractModule;

public class ResourcesModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(CarbonCopyResource.class).to(CarbonCopyResourceImpl.class);
    }
}
