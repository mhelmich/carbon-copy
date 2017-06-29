package org.carbon.copy.calcite;

import com.google.inject.AbstractModule;

public class CalciteModule extends AbstractModule {
    @Override
    protected void configure() {
        // yupp, not the fine British way
        // read the comment in Injector for more context
        requestStaticInjection(Injector.class);
    }
}
