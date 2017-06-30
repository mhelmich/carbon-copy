package org.carbon.copy.calcite;

import org.apache.calcite.DataContext;
import org.apache.calcite.adapter.enumerable.EnumerableConvention;
import org.apache.calcite.adapter.enumerable.EnumerableRel;
import org.apache.calcite.adapter.enumerable.EnumerableRelImplementor;
import org.apache.calcite.adapter.enumerable.PhysType;
import org.apache.calcite.adapter.enumerable.PhysTypeImpl;
import org.apache.calcite.linq4j.tree.Blocks;
import org.apache.calcite.linq4j.tree.Expressions;
import org.apache.calcite.linq4j.tree.Types;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.rel.RelWriter;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rel.type.RelDataTypeField;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

class TableScan extends org.apache.calcite.rel.core.TableScan implements EnumerableRel {

    private final CarbonCopyTable carbonCopyTable;
    private final String javaFilterExpression;
    private final List<Integer> columnIndexesForThePredicate;
    private final List<Integer> columnIndexesToProjectTo;

    TableScan(RelOptCluster cluster, RelOptTable table, CarbonCopyTable carbonCopyTable) {
        super(cluster, cluster.traitSetOf(EnumerableConvention.INSTANCE), table);
        this.carbonCopyTable = carbonCopyTable;
        this.javaFilterExpression = "";
        this.columnIndexesForThePredicate = Collections.emptyList();
        this.columnIndexesToProjectTo = Collections.emptyList();
    }

    TableScan(RelOptCluster cluster, RelOptTable table, CarbonCopyTable carbonCopyTable, List<Integer> columnIndexesToProjectTo) {
        super(cluster, cluster.traitSetOf(EnumerableConvention.INSTANCE), table);
        this.carbonCopyTable = carbonCopyTable;
        this.javaFilterExpression = "";
        this.columnIndexesForThePredicate = Collections.emptyList();
        this.columnIndexesToProjectTo = columnIndexesToProjectTo;
    }

    TableScan(RelOptCluster cluster, RelOptTable table, CarbonCopyTable carbonCopyTable, String javaFilterExpression, List<Integer> columnIndexesForThePredicate) {
        super(cluster, cluster.traitSetOf(EnumerableConvention.INSTANCE), table);
        this.carbonCopyTable = carbonCopyTable;
        this.javaFilterExpression = javaFilterExpression;
        this.columnIndexesForThePredicate = columnIndexesForThePredicate;
        this.columnIndexesToProjectTo = Collections.emptyList();
    }

    TableScan(RelOptCluster cluster, RelOptTable table, CarbonCopyTable carbonCopyTable, String javaFilterExpression, List<Integer> columnIndexesForThePredicate, List<Integer> columnIndexesToProjectTo) {
        super(cluster, cluster.traitSetOf(EnumerableConvention.INSTANCE), table);
        this.carbonCopyTable = carbonCopyTable;
        this.javaFilterExpression = javaFilterExpression;
        this.columnIndexesForThePredicate = columnIndexesForThePredicate;
        this.columnIndexesToProjectTo = columnIndexesToProjectTo;
    }

    CarbonCopyTable getCarbonCopyTable() {
        return carbonCopyTable;
    }

    /**
     * This tells calcite how CarbonCopy is able to manipulate the query tree.
     * All these rules are being executed during query optimization and the optimizer picks the cheapest of them.
     * Each of these rules must have a corresponding implementation in the TableScan to work :)
     */
    @Override
    public void register(RelOptPlanner planner) {
        planner.addRule(OptimizerRule.FILTER_SCAN);
        planner.addRule(OptimizerRule.PROJECT_SCAN);
        planner.addRule(OptimizerRule.PROJECT_FILTER_SCAN);
    }

    /**
     * Explains to calcite how rows look like.
     * This is interesting for projections and intermediate results.
     */
    @Override
    public RelDataType deriveRowType() {
        final List<RelDataTypeField> fieldList = table.getRowType().getFieldList();
        final RelDataTypeFactory.FieldInfoBuilder builder = getCluster().getTypeFactory().builder();
        if (canDoProject()) {
            for (int field : columnIndexesToProjectTo) {
                builder.add(fieldList.get(field));
            }
        } else {
            for (RelDataTypeField field : fieldList) {
                builder.add(field);
            }
        }
        return builder.build();
    }

    /**
     * As it turns out the optimizer uses this information to find the cheapest plan.
     * It's important for this to be implemented and accurate at all times!
     */
    @Override
    public RelWriter explainTerms(RelWriter pw) {
        return super.explainTerms(pw)
                .item("javaFilterExpression", javaFilterExpression)
                .item("columnIndexesForThePredicate", columnIndexesForThePredicate)
                .item("columnIndexesToProjectTo", columnIndexesToProjectTo);
    }

    // this refers to a method in CarbonCopyTable that does the heavy lifting for us
    private static final Method SCAN_CALLBACK =
            Types.lookupMethod(
                    CarbonCopyTable.class,
                    "scan",
                    DataContext.class,
                    String.class,
                    Integer[].class
            );

    private static final Method PROJECT_CALLBACK =
            Types.lookupMethod(
                    CarbonCopyTable.class,
                    "project",
                    DataContext.class,
                    Integer[].class
            );

    private static final Method SCAN_AND_PROJECT_CALLBACK =
            Types.lookupMethod(
                    CarbonCopyTable.class,
                    "scanAndProject",
                    DataContext.class,
                    String.class,
                    Integer[].class,
                    Integer[].class
            );

    private static final Method FULL_TABLE_SCAN_CALLBACK =
            Types.lookupMethod(
                    CarbonCopyTable.class,
                    "fullTableScan",
                    DataContext.class
            );

    /**
     * This is being called when the table scan is executed.
     * This acts as a proxy that forwards calls to a method back on the respective table object.
     */
    @Override
    public Result implement(EnumerableRelImplementor implementor, Prefer prefer) {
        PhysType physType =
                PhysTypeImpl.of(
                        implementor.getTypeFactory(),
                        getRowType(),
                        prefer.preferArray());

        // not the prettiest code but this decides on basis of what TableScan instance I am which method on the table to call.
        // I rather have this kind of code here as opposed to on the table (which this will eventually call reflectively).
        // The table just gets all information handed and knows what to do.
        if (canDoScan() && canDoProject()) {
            return implementor.result(
                    physType,
                    Blocks.toBlock(
                            Expressions.call(table.getExpression(CarbonCopyTable.class),
                                    SCAN_AND_PROJECT_CALLBACK,
                                    implementor.getRootExpression(),
                                    Expressions.constant(javaFilterExpression),
                                    Expressions.constant(columnIndexesForThePredicate.toArray(new Integer[columnIndexesForThePredicate.size()])),
                                    Expressions.constant(columnIndexesToProjectTo.toArray(new Integer[columnIndexesToProjectTo.size()]))
                            )));
        } else if (canDoScan()) {
            return implementor.result(
                    physType,
                    Blocks.toBlock(
                            Expressions.call(table.getExpression(CarbonCopyTable.class),
                                    SCAN_CALLBACK,
                                    implementor.getRootExpression(),
                                    Expressions.constant(javaFilterExpression),
                                    Expressions.constant(columnIndexesForThePredicate.toArray(new Integer[columnIndexesForThePredicate.size()]))
                            )));
        } else if (canDoProject()) {
            return implementor.result(
                    physType,
                    Blocks.toBlock(
                            Expressions.call(table.getExpression(CarbonCopyTable.class),
                                    PROJECT_CALLBACK,
                                    implementor.getRootExpression(),
                                    Expressions.constant(columnIndexesToProjectTo.toArray(new Integer[columnIndexesToProjectTo.size()]))
                            )));
        } else {
            return implementor.result(
                    physType,
                    Blocks.toBlock(
                            Expressions.call(table.getExpression(CarbonCopyTable.class),
                                    FULL_TABLE_SCAN_CALLBACK,
                                    implementor.getRootExpression()
                            )));
        }
    }

    private boolean canDoScan() {
        return javaFilterExpression != null && !javaFilterExpression.isEmpty()
                && columnIndexesForThePredicate != null && !columnIndexesForThePredicate.isEmpty();
    }

    private boolean canDoProject() {
        return columnIndexesToProjectTo != null && !columnIndexesToProjectTo.isEmpty();
    }
}
