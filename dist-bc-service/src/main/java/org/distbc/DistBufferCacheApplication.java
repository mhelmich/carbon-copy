package org.distbc;

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
        environment.jersey().register(DistBufferCacheResourceImpl.class);
        environment.healthChecks().register("galaxy", new GalaxyHealthCheck());
    }
}