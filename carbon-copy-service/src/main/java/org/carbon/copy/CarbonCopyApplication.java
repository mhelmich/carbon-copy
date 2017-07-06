/*
 *
 *  Copyright 2017 Marco Helmich
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.carbon.copy;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.dropwizard.Application;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.carbon.copy.calcite.AvaticaServer;
import org.carbon.copy.calcite.CalciteModule;
import org.carbon.copy.data.structures.DataStructureModule;
import org.carbon.copy.data.structures.GalaxyGrid;
import org.carbon.copy.data.structures.TxnManagerModule;
import org.carbon.copy.health.checks.GalaxyHealthCheck;
import org.carbon.copy.resources.CarbonCopyResource;
import org.carbon.copy.resources.ResourcesModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CarbonCopyApplication extends Application<CarbonCopyConfiguration> {
    private static Logger logger = LoggerFactory.getLogger(CarbonCopyApplication.class);

    public static void main(String[] args) throws Exception {
        new CarbonCopyApplication().run(args);
    }

    @Override
    public String getName() {
        return "carbon-copy";
    }

    @Override
    public void initialize(Bootstrap<CarbonCopyConfiguration> bootstrap) { }

    @Override
    public void run(CarbonCopyConfiguration configuration, Environment environment) {
        logger.info("Starting carbon copy node with the following galaxy config files: {} {}", configuration.getDefaultPeerXml(), configuration.getDefaultPeerProperties());
        environment.healthChecks().register("galaxy", new GalaxyHealthCheck());
        Injector injector = Guice.createInjector(
                new DataStructureModule(configuration.getDefaultPeerXml(), configuration.getDefaultPeerProperties()),
                new TxnManagerModule(),
                new CalciteModule(),
                new ResourcesModule()
        );

        environment.lifecycle().manage(new Managed() {
            @Override
            public void start() throws Exception {
                injector.getInstance(AvaticaServer.class).start();
            }

            @Override
            public void stop() throws Exception {
                injector.getInstance(AvaticaServer.class).stop();
            }
        });

        environment.lifecycle().manage(new Managed() {
            @Override
            public void start() throws Exception {
                injector.getInstance(GalaxyGrid.class).start();
            }

            @Override
            public void stop() throws Exception {
                injector.getInstance(GalaxyGrid.class).stop();
            }
        });

        // register from the guice injector
        environment.jersey().register(injector.getInstance(CarbonCopyResource.class));
    }
}