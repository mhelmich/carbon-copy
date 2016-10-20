package org.distbc;

import co.paralleluniverse.galaxy.Grid;
import com.codahale.metrics.health.HealthCheck;

/**
 * Created by mhelmich on 10/4/16.
 */
public class GalaxyHealthCheck extends HealthCheck {

    private Grid grid;

    GalaxyHealthCheck(Grid grid) {
        this.grid = grid;
    }

    protected Result check() throws Exception {
        return grid.cluster().isOnline() ? Result.healthy() : Result.unhealthy("Galaxy cluster is not online!");
    }
}
