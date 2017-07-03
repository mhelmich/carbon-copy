package org.carbon.copy.calcite;

import org.apache.calcite.DataContext;
import org.apache.calcite.adapter.java.AbstractQueryableTable;
import org.apache.calcite.linq4j.AbstractEnumerable;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.linq4j.QueryProvider;
import org.apache.calcite.linq4j.Queryable;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.TranslatableTable;
import org.apache.commons.lang3.tuple.Pair;
import org.carbon.copy.data.structures.Catalog;
import org.carbon.copy.data.structures.Table;
import org.carbon.copy.data.structures.Tuple;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Gotta be public! Reflective call will not be able to reach this class iff it's public.
 */
public class CarbonCopyTable extends AbstractQueryableTable implements TranslatableTable {

    private final Catalog catalog;
    private final Table table;

    CarbonCopyTable(Catalog catalog, Table table) {
        super(Object[].class);
        this.catalog = catalog;
        this.table = table;
    }

    @Override
    public RelDataType getRowType(RelDataTypeFactory relDataTypeFactory) {
        List<Pair<String, RelDataType>> metadata =
                table.getSortedColumnMetadata().stream()
                .map(pair -> Pair.of(pair.getLeft(), toCalciteType(relDataTypeFactory, (String)pair.getRight().get(2))))
                .collect(Collectors.toList());
        return relDataTypeFactory.createStructType(metadata);
    }

    private RelDataType toCalciteType(RelDataTypeFactory relDataTypeFactory, String klassName) {
        try {
            Class klass = Class.forName(klassName);
            RelDataType javaType = relDataTypeFactory.createJavaType(klass);
            RelDataType sqlType = relDataTypeFactory.createSqlType(javaType.getSqlTypeName());
            return relDataTypeFactory.createTypeWithNullability(sqlType, true);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * VOODOO!!!
     * This method is being called via reflection from TableScan.
     */
    @SuppressWarnings("UnusedDeclaration unchecked")
    public Enumerable<Object> scan(DataContext dataContext, String booleanJavaSource, Integer[] columnIndexesForThePredicate) {

        if (canDoFilter(booleanJavaSource, columnIndexesForThePredicate)) {

            CarbonCopyPredicate predicate = CompilerUtil.compileBooleanExpression(booleanJavaSource);
            Stream<Object[]> resultStream = table.keys()
                    .map(table::get)
                    .filter(predicate::test)
                    .map(Tuple::toObjectArray);

            AtomicBoolean cancelFlag = DataContext.Variable.CANCEL_FLAG.get(dataContext);
            return new AbstractEnumerable<Object>() {
                @Override
                public Enumerator<Object> enumerator() {
                    return new CarbonCopyEnumerator<>(resultStream, cancelFlag);
                }
            };
        } else {
            throw new IllegalArgumentException("You're asking me to filter and scan but you don't give me any predicate information.");
        }
    }

    /**
     * VOODOO!!!
     * This method is being called via reflection from TableScan.
     */
    @SuppressWarnings("UnusedDeclaration")
    public Enumerable<Object> scanAndProject(DataContext dataContext, String booleanJavaSource, Integer[] columnIndexesForThePredicate, Integer[] columnIndexesToProjectTo) {
        if (canDoFilter(booleanJavaSource, columnIndexesForThePredicate) && canDoProject(columnIndexesToProjectTo)) {
            CarbonCopyPredicate predicate = CompilerUtil.compileBooleanExpression(booleanJavaSource);
            Stream<Object[]> resultStream = table.keys()
                    .map(table::get)
                    .filter(predicate::test)
                    .map(tuple -> tuple.subTuple(columnIndexesToProjectTo))
                    .map(Tuple::toObjectArray);

            AtomicBoolean cancelFlag = DataContext.Variable.CANCEL_FLAG.get(dataContext);
            return new AbstractEnumerable<Object>() {
                @Override
                public Enumerator<Object> enumerator() {
                    return new CarbonCopyEnumerator<>(resultStream, cancelFlag);
                }
            };
        } else {
            throw new IllegalArgumentException("You're asking me to filter and project but you don't give me enough information.");
        }
    }

    /**
     * VOODOO!!!
     * This method is being called via reflection from TableScan.
     */
    @SuppressWarnings("UnusedDeclaration")
    public Enumerable<Object> project(DataContext dataContext, Integer[] columnIndexesToProjectTo) {
        if (canDoProject(columnIndexesToProjectTo)) {
            Stream<Object[]> resultStream = table.keys()
                    .map(table::get)
                    .map(tuple -> tuple.subTuple(columnIndexesToProjectTo))
                    .map(Tuple::toObjectArray);

            AtomicBoolean cancelFlag = DataContext.Variable.CANCEL_FLAG.get(dataContext);
            return new AbstractEnumerable<Object>() {
                @Override
                public Enumerator<Object> enumerator() {
                    return new CarbonCopyEnumerator<>(resultStream, cancelFlag);
                }
            };
        } else {
            throw new IllegalArgumentException("You're asking me to filter and project but you don't give me enough information.");
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public Enumerable<Object> fullTableScan(DataContext dataContext) {
        AtomicBoolean cancelFlag = DataContext.Variable.CANCEL_FLAG.get(dataContext);
        Stream<Object[]> resultStream = table.keys()
                .map(table::get)
                .map(Tuple::toObjectArray);

        return new AbstractEnumerable<Object>() {
            @Override
            public Enumerator<Object> enumerator() {
                return new CarbonCopyEnumerator<>(resultStream, cancelFlag);
            }
        };
    }

    private boolean canDoFilter(String booleanJavaSource, Integer[] columnIndexesForThePredicate) {
        return booleanJavaSource != null && !booleanJavaSource.isEmpty()
                && columnIndexesForThePredicate != null && columnIndexesForThePredicate.length > 0;
    }

    private boolean canDoProject(Integer[] columnIndexesToProjectTo) {
        return columnIndexesToProjectTo != null && columnIndexesToProjectTo.length > 0;
    }

    @Override
    public String toString() {
        return table.toString();
    }

    @Override
    public <T> Queryable<T> asQueryable(QueryProvider queryProvider, SchemaPlus schema, String tableName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RelNode toRel(RelOptTable.ToRelContext toRelContext, RelOptTable relOptTable) {
        return new TableScan(toRelContext.getCluster(), relOptTable, this);
    }
}
