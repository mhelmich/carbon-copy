package org.distbc;

import co.paralleluniverse.galaxy.Grid;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.distbc.data.structures.SkipList;

public class DistBufferCacheApplication extends Application<DistBufferCacheConfiguration> {
    public static void main(String[] args) throws Exception {
        new DistBufferCacheApplication().run(args);
    }

    private static KryoFactory kryoFactory = () -> {
        Kryo kryo = new Kryo();
        // configure kryo instance, customize settings
        kryo.register(SkipList.class, 0);
        return kryo;
    };

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

        KryoPool pool = new KryoPool.Builder(kryoFactory).softReferences().build();

        DistBufferCacheResourceImpl resource = new DistBufferCacheResourceImpl(g, pool);
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