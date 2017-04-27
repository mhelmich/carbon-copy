/*
 *
 *  Copyright 2017 Marco Helmich
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

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
