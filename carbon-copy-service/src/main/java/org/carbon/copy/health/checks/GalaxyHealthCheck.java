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

package org.carbon.copy.health.checks;

import co.paralleluniverse.galaxy.Cluster;
import com.codahale.metrics.health.HealthCheck;
import com.google.inject.Inject;

public class GalaxyHealthCheck extends HealthCheck {

    @Inject
    private Cluster cluster;

    public GalaxyHealthCheck() { }

    protected Result check() throws Exception {
        return cluster.isOnline() ? Result.healthy() : Result.unhealthy("Galaxy cluster is not online!");
    }
}
