package org.distbc;

import co.paralleluniverse.galaxy.Cluster;
import com.codahale.metrics.health.HealthCheck;
import com.google.inject.Inject;

public class GalaxyHealthCheck extends HealthCheck {

    @Inject
    private Cluster cluster;

    GalaxyHealthCheck() {
    }

    protected Result check() throws Exception {
        return cluster.isOnline() ? Result.healthy() : Result.unhealthy("Galaxy cluster is not online!");
    }
}
