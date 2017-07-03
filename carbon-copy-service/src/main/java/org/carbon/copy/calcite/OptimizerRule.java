package org.carbon.copy.calcite;


import org.apache.calcite.plan.RelOptRule;
import org.apache.calcite.plan.RelOptRuleCall;
import org.apache.calcite.plan.RelOptRuleOperand;
import org.apache.calcite.rel.logical.LogicalFilter;
import org.apache.calcite.rel.logical.LogicalProject;
import org.apache.calcite.rex.RexCall;
import org.apache.calcite.rex.RexInputRef;
import org.apache.calcite.rex.RexLiteral;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.rex.RexSlot;
import org.apache.calcite.rex.RexVisitorImpl;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

abstract class OptimizerRule extends RelOptRule {

    static final OptimizerRule PROJECT_FILTER_SCAN
            = new ProjectFilterScanOptimizerRule();

    static final OptimizerRule FILTER_SCAN
            = new FilterScanOptimizerRule();

    static final OptimizerRule PROJECT_SCAN
            = new ProjectScanOptimizerRule();

    private OptimizerRule(RelOptRuleOperand rule, String description) {
        super(rule, "CarbonCopyOptimizerRule:" + description);
    }

    private static class ProjectFilterScanOptimizerRule extends OptimizerRule {
        private ProjectFilterScanOptimizerRule() {
            super(operand(
                    LogicalProject.class,
                    operand(LogicalFilter.class,
                            operand(TableScan.class, none()))
            ), "project-filter-scan");
        }

        @Override
        public void onMatch(RelOptRuleCall call) {
            LogicalProject project = call.rel(0);
            List<Integer> columnIndexesToProjectTo = project.getChildExps().stream()
                    .map(rexNode -> (RexInputRef)rexNode)
                    .map(RexSlot::getIndex)
                    .collect(Collectors.toList());

            LogicalFilter filter = call.rel(1);
            RexToJavaPredicateTranslator translator = new RexToJavaPredicateTranslator();
            String javaSource = filter.getChildExps().get(0).accept(translator);
            TableScan scan = call.rel(2);

            call.transformTo(
                    new TableScan(
                            scan.getCluster(),
                            scan.getTable(),
                            scan.getCarbonCopyTable(),
                            javaSource,
                            translator.getColumnIndexesForPredicate(),
                            columnIndexesToProjectTo
                    )
            );
        }
    }

    private static class FilterScanOptimizerRule extends OptimizerRule {
        private FilterScanOptimizerRule() {
            super(operand(
                    LogicalFilter.class,
                    operand(TableScan.class, none())),
                    "filter-scan");
        }

        @Override
        public void onMatch(RelOptRuleCall call) {
            LogicalFilter filter = call.rel(0);
            RexToJavaPredicateTranslator translator = new RexToJavaPredicateTranslator();
            String javaSource = filter.getChildExps().get(0).accept(translator);
            TableScan scan = call.rel(1);

            call.transformTo(
                    new TableScan(
                            scan.getCluster(),
                            scan.getTable(),
                            scan.getCarbonCopyTable(),
                            javaSource,
                            translator.getColumnIndexesForPredicate()
                    )
            );
        }
    }

    private static class ProjectScanOptimizerRule extends OptimizerRule {
        private ProjectScanOptimizerRule() {
            super(operand(
                    LogicalProject.class,
                    operand(TableScan.class, none())),
                    "project-scan");
        }

        @Override
        public void onMatch(RelOptRuleCall call) {
            LogicalProject project = call.rel(0);
            List<Integer> columnIndexesToProjectTo = project.getChildExps().stream()
                    .map(rexNode -> (RexInputRef)rexNode)
                    .map(RexSlot::getIndex)
                    .collect(Collectors.toList());
            TableScan scan = call.rel(1);

            call.transformTo(
                    new TableScan(
                            scan.getCluster(),
                            scan.getTable(),
                            scan.getCarbonCopyTable(),
                            columnIndexesToProjectTo
                    )
            );
        }
    }

    /**
     * comment about this visitor converting the expression tree into java code
     */
    private static class RexToJavaPredicateTranslator extends RexVisitorImpl<String> {
        private final List<Integer> columnIndexesForPredicate = new LinkedList<>();

        RexToJavaPredicateTranslator() {
            super(true);
        }

        @Override
        public String visitCall(RexCall call) {
            final String op;
            switch (call.getKind()) {
                case AND:
                    return concatBooleansOperations(" && ", call.getOperands());
                case OR:
                    return concatBooleansOperations(" || ", call.getOperands());
                case EQUALS:
                    op = "==";
                    break;
                case NOT_EQUALS:
                    op = "!=";
                    break;
                case GREATER_THAN:
                    op = ">";
                    break;
                case GREATER_THAN_OR_EQUAL:
                    op = ">=";
                    break;
                case LESS_THAN:
                    op = "<";
                    break;
                case LESS_THAN_OR_EQUAL:
                    op = "<=";
                    break;
                default:
                    throw new UnsupportedOperationException("I've never seen operation " + call.getOperator().getKind().toString());
            }

            // this code is only supposed to run in case we are dealing with an arithmetic operation
            // boolean operations "and" and "or" are never supposed to reach this code
            Comparable<String> left = call.getOperands().get(0).accept(this);
            Comparable<String> right = call.getOperands().get(1).accept(this);
            // I want this to be java code ... something like this
            // Objects.compare(left, right, Comparator.naturalOrder());
            return String.format("java.util.Objects.compare(%s, %s, org.carbon.copy.calcite.CarbonCopyComparator.COMPARATOR) %s 0", left, right, op);
        }

        private String concatBooleansOperations(String operation, List<RexNode> operands) {
            List<String> strs = new LinkedList<>();
            for (RexNode rn : operands) {
                strs.add(rn.accept(this));
            }
            return StringUtils.join(strs, operation);
        }

        @Override
        public String visitLiteral(RexLiteral literal) {
            if (literal == null) {
                return "null";
            } else if (SqlTypeName.CHAR_TYPES.contains(literal.getTypeName())) {
                return "\"" + literal.getValue3().toString() + "\"";
            } else {
                return literal.getValue3().toString();
            }
        }

        @Override
        public String visitInputRef(RexInputRef inputRef) {
            columnIndexesForPredicate.add(inputRef.getIndex());
            // it is important to keep the name of the function parameter
            // in sync with the function template
            return "tuple.get(" + inputRef.getIndex() + ")";
        }

        List<Integer> getColumnIndexesForPredicate() {
            return columnIndexesForPredicate;
        }
    }
}
