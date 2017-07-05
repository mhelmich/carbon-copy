package org.carbon.copy.calcite;

import com.google.inject.AbstractModule;

public class CalciteModule extends AbstractModule {
    @Override
    protected void configure() {
        // yupp, not the fine British way
        // read the comment in Injector for more context
        requestStaticInjection(Injector.class);

        // bind and start avatica server
        AvaticaServer as = new AvaticaServerImpl();
        bind(AvaticaServer.class).toInstance(as);
        as.start();

        try {
            Class.forName(EmbeddedCarbonCopyDriver.class.getName());
        } catch (ClassNotFoundException xcp) {
            throw new RuntimeException(xcp);
        }
    }
}
