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
import com.google.inject.Module;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.carbon.copy.calcite.CalciteModule;
import org.carbon.copy.data.structures.DataStructureModule;
import org.carbon.copy.data.structures.TxnManagerModule;

import java.util.Arrays;
import java.util.List;

public class CarbonCopyApplication extends Application<CarbonCopyConfiguration> {
    public static void main(String[] args) throws Exception {
        new CarbonCopyApplication().run(args);
    }

    @Override
    public String getName() {
        return "dist-bc";
    }

    @Override
    public void initialize(Bootstrap<CarbonCopyConfiguration> bootstrap) {
        Guice.createInjector(allModules());
    }

    @Override
    public void run(CarbonCopyConfiguration configuration, Environment environment) {
        environment.jersey().register(CarbonCopyResourceImpl.class);
        environment.healthChecks().register("galaxy", new GalaxyHealthCheck());
    }

    private List<Module> allModules() {
        return Arrays.asList(
                new CalciteModule(),
                new DataStructureModule(),
                new TxnManagerModule()
        );
    }
}