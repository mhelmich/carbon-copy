package org.distbc.planner;

import org.distbc.data.structures.TempTable;

import java.util.function.Function;

interface Operation extends Function<TempTable, TempTable> {
}
