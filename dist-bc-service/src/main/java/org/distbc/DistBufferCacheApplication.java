package org.distbc;

import co.paralleluniverse.galaxy.Grid;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class DistBufferCacheApplication extends Application<DistBufferCacheConfiguration> {
    public static void main(String[] args) throws Exception {
        new DistBufferCacheApplication().run(args);
    }

    @Override
    public String getName() {
        return "dist-bc";
    }

    @Override
    public void initialize(Bootstrap<DistBufferCacheConfiguration> bootstrap) {
        // no op
    }

    @Override
    public void run(DistBufferCacheConfiguration configuration, Environment environment) {
        Grid g = startupGalaxy(configuration);

        DistBufferCacheResourceImpl resource = new DistBufferCacheResourceImpl(g);
        environment.jersey().register(resource);

        environment.healthChecks().register("galaxy", new GalaxyHealthCheck(g));
    }

    private Grid startupGalaxy(DistBufferCacheConfiguration configuration) {
        Grid g;
        try {
            g = Grid.getInstance(configuration.getDefaultPeerXml(), configuration.getDefaultPeerProperties());
            g.goOnline();
        } catch (Exception xcp) {
            // when we catch any exception, there's no point in bringing this node online
            // fail the startup
            throw new RuntimeException(xcp);
        }
        return g;
    }
}