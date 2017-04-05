package org.distbc.planner;

import org.distbc.data.structures.Queryable;
import org.distbc.data.structures.Tuple;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class TempTable implements Queryable {
    private final Set<Tuple> tuples;
    private long id;

    TempTable() {
        this(-1, Collections.emptySet());
    }

    TempTable(Set<Tuple> tuples) {
        this(-1L, tuples);
    }

    TempTable(long id, Set<Tuple> tuples) {
        this.id = id;
        this.tuples = tuples;
    }

    @Override
    public Set<Tuple> scan(Predicate<Tuple> predicate) {
        return Collections.emptySet();
    }

    @Override
    public Set<Tuple> project(Function<Tuple, Tuple> projection) {
        return Collections.emptySet();
    }

    @Override
    public Set<Tuple> scan(Predicate<Tuple> predicate, Function<Tuple, Tuple> projection) {
        return Collections.emptySet();
    }

    @Override
    public List<String> getColumnNames() {
        return Collections.emptyList();
    }

    @Override
    public long getId() {
        return id;
    }
}
