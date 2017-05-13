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

package org.distbc.planner;

import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;
import org.distbc.data.structures.GUID;
import org.distbc.data.structures.Table;
import org.distbc.data.structures.Tuple;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

class OpSelection implements Supplier<Set<GUID>> {

    private final Set<String> columnNames;
    private final Table tableToUse;
    private final String expression;

    OpSelection(Set<String> columnNames, Table tableToUse, String expression) {
        this.columnNames = columnNames;
        this.tableToUse = tableToUse;
        this.expression = expression;
    }

    @Override
    public Set<GUID> get() {
        List<Tuple> tableMetadata = tableToUse.getColumnMetadata();
        // TODO -- we might want to filter this to only columns we need
        Map<String, Integer> columnNameToIndex = tableMetadata.stream()
                .collect(Collectors.toMap(tuple -> (String)tuple.get(0), tuple -> (Integer)tuple.get(1)));
        Map<String, Boolean> columnNameToNumeric = tableMetadata.stream()
                .collect(Collectors.toMap(tuple -> (String)tuple.get(0), tuple -> {
                    try {
                        return Number.class.isAssignableFrom(Class.forName((String)tuple.get(2)));
                    } catch (ClassNotFoundException e) {
                        return false;
                    }
                }));

        Evaluator eval = new Evaluator();

        String tmp = expression;
        for (String columnName : columnNames) {
            Boolean isNumber = columnNameToNumeric.get(columnName);
            if (isNumber != null && isNumber) {
                // replace all column names that are not within single quotes
                tmp = tmp.replaceAll(
                        columnName +   // replace this string
                        "(?=" +        // followed by
                          "(?:" +      // start a non-capture group -- these things will be skipped
                            "[^\']*" + // 0 or more non-quote characters
                            "\'" +     // 1 quote
                            "[^\']*" + // 0 or more non-quote characters
                            "\'" +     // 1 quote
                          ")*" +       // 0 or more repetition of non-capture group (multiple of 2 quotes will be even)
                          "[^\']*" +   // finally 0 or more non-quotes
                          "$" +        // till the end  (this is necessary, else every comma will satisfy the condition)
                        ")",
                        "#{" + columnName + "}"
                );

            } else {
                // this contains single quotes!!! to make non-numeric values a string
                // replace all column names that are not within single quotes
                tmp = tmp.replaceAll(
                        columnName +   // replace this string
                        "(?=" +        // followed by
                          "(?:" +      // start a non-capture group -- these things will be skipped
                            "[^\']*" + // 0 or more non-quote characters
                            "\'" +     // 1 quote
                            "[^\']*" + // 0 or more non-quote characters
                            "\'" +     // 1 quote
                          ")*" +       // 0 or more repetition of non-capture group (multiple of 2 quotes will be even)
                          "[^\']*" +   // finally 0 or more non-quotes
                          "$" +        // till the end  (this is necessary, else every comma will satisfy the condition)
                        ")",
                        "'#{" + columnName + "}'"
                );
            }
        }
        // this is used into the closure and needs to be final
        String newExpression = tmp;


        Predicate<Tuple> p = tuple -> {
            for (String columnName : columnNames) {
                // TODO -- yeah sure, we could micro-optimize this by not making the lookup all the time
                eval.putVariable(columnName, tuple.get(columnNameToIndex.get(columnName)).toString());
            }

            try {
                return eval.getBooleanResult(newExpression);
            } catch (EvaluationException xcp) {
                throw new RuntimeException("invalid expression " + newExpression, xcp);
            }
        };

        // do the actual selection
        return tableToUse.keys()
                .map(tableToUse::get)
                .filter(p)
                .map(Tuple::getGuid)
                .collect(Collectors.toSet());
    }

    @Override
    public String toString() {
        return expression;
    }
}