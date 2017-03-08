package org.distbc.data.structures;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public interface Queryable {
    Set<Tuple> scan(Predicate<Tuple> predicate);
    Set<Tuple> scan(Predicate<Tuple> predicate, Function<Tuple, Tuple> projection);
    List<String> getColumnNames();
}
