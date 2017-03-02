package org.distbc.planner;

import java.util.Set;

public interface QueryPlan {
    Set<Object> execute();
}
