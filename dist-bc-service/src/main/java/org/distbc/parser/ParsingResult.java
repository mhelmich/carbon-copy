package org.distbc.parser;

import java.util.List;

public interface ParsingResult {
    List<String> getTableNames();
    List<String> getProjectionColumnNames();
    List<String> getWhereClauses();
    List<String> getJoinClauses();
    List<BinaryOperation> getBinaryOperations();

    class UnaryOperation {
        final String operand1;
        final String operation;

        UnaryOperation(String operand1, String operation) {
            this.operand1 = operand1;
            this.operation = operation;
        }
    }

    class BinaryOperation {
        public final String operand1;
        public final String operand2;
        public final String operation;

        BinaryOperation(String operand1, String operation, String operand2) {
            this.operand1 = operand1;
            this.operand2 = operand2;
            this.operation = operation;
        }
    }
}
