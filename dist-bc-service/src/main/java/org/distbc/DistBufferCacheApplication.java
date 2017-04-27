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