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

import org.distbc.data.structures.TempTable;
import org.distbc.data.structures.Tuple;
import org.distbc.data.structures.Txn;
import org.distbc.parser.ParsingResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

class Selection implements Operation {
    private final List<ParsingResult.BinaryOperation> bos;

    Selection(List<ParsingResult.BinaryOperation> bos) {
        this.bos = bos;
    }

    @Override
    public TempTable apply(TempTable tempTable, Txn txn) {
        List<Tuple> tableMetadata = tempTable.getColumnMetadata();
        Map<String, Integer> columnNameToIndex = new HashMap<>();
        // TODO -- we might want to filter this to only columns we need
        tableMetadata.forEach(tuple -> columnNameToIndex.put((String)tuple.get(0), (Integer)tuple.get(1)));

        Predicate<Tuple> p = tuple -> {
            AtomicBoolean b = new AtomicBoolean(false);
            bos.forEach(binaryOperation -> {
                Integer idx = columnNameToIndex.get(binaryOperation.operand1);
                if (idx != null) {
                    int cmp = tuple.get(idx).compareTo(binaryOperation.operand2);
                    switch(binaryOperation.operation) {
                        case "=":
                            b.getAndSet(cmp == 0);
                            break;
                        case ">":
                            b.getAndSet(cmp > 0);
                            break;
                        case ">=":
                            b.getAndSet(cmp >= 0);
                            break;
                        case "<":
                            b.getAndSet(cmp < 0);
                            break;
                        case "<=":
                            b.getAndSet(cmp <= 0);
                            break;
                        case "<>":
                            b.getAndSet(cmp != 0);
                            break;
                        default:
                            throw new IllegalArgumentException("Couldn't parse comparator " + binaryOperation.operation);
                    }
                } else {
                    throw new IllegalArgumentException("Couldn't find column " + binaryOperation.operand1);
                }
            });
            // if this returns true, the record will be deleted from the result set
            // if this return false, the record is being kept in the result set
            return !b.get();
        };

        // do the actual selection
        tempTable.keys()
                .map(tempTable::get)
                .filter(p)
                .forEach(tuple -> tempTable.delete(tuple.getGuid(), txn));
        return tempTable;
    }
}
