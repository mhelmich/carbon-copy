package org.distbc.planner;

import org.distbc.data.structures.Queryable;

import java.util.function.Function;

interface Operation extends Function<Queryable, Queryable> {
}