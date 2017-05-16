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

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.distbc.parser.gen.SQLLexer;
import org.distbc.parser.gen.SQLParser;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SQLParserListenerTest {
    @Test
    public void testBasic() {
        String input = "select * from narf";
        SQLParserListener listener = parse(input);

        assertEquals(1, listener.getProjectionColumnNames().size());
        assertEquals("*", listener.getProjectionColumnNames().get(0));
        assertEquals(1, listener.getTableNames().size());
        assertEquals("narf", listener.getTableNames().get(0));
        assertEquals("", listener.getExpressionText());
    }

    @Test
    public void testMultipleColumns() {
        String input = "select col1, col2,col3 , col4 from narf";
        SQLParserListener listener = parse(input);

        assertEquals(4, listener.getProjectionColumnNames().size());
        for (int i = 0; i < listener.getProjectionColumnNames().size(); i++) {
            assertEquals("col" + (i + 1), listener.getProjectionColumnNames().get(i));
        }
        assertEquals(1, listener.getTableNames().size());
        assertEquals("narf", listener.getTableNames().get(0));
        assertEquals("", listener.getExpressionText());
    }

    @Test
    public void testMultipleTables() {
        String input = "select col from tab1,tab2, tab3 , tab4";
        SQLParserListener listener = parse(input);

        assertEquals(1, listener.getProjectionColumnNames().size());
        assertEquals("col", listener.getProjectionColumnNames().get(0));
        assertEquals(4, listener.getTableNames().size());
        List<String> columnNames = new ArrayList<>(listener.getTableNames());
        for (int i = 0; i < listener.getTableNames().size(); i++) {
            assertTrue(columnNames.contains("tab" + (i + 1)));
            assertTrue(columnNames.remove("tab" + (i + 1)));
        }
        assertTrue(columnNames.isEmpty());
        assertEquals("", listener.getExpressionText());
    }

    @Test
    public void testSelection() {
        String input = "SELECT col from tab1 where col2 = 13";
        SQLParserListener listener = parse(input);

        assertEquals(1, listener.getProjectionColumnNames().size());
        assertEquals("col", listener.getProjectionColumnNames().get(0));
        assertEquals(1, listener.getTableNames().size());
        assertEquals("col2==13", listener.getExpressionText());
    }

    @Test
    public void testSelectionWithBooleanExpression() {
        String input = "select col from tab1 where col2 = 13 and col1= 'narf'";
        SQLParserListener listener = parse(input);

        assertEquals(1, listener.getProjectionColumnNames().size());
        assertEquals("col", listener.getProjectionColumnNames().get(0));
        assertEquals(1, listener.getTableNames().size());
        assertEquals("col2==13&&col1=='narf'", listener.getExpressionText());
    }

    @Test
    public void testSelectionWithBooleanExpression2() {
        String input = "select col from tab1 where col2 < 13 or col1>= 'narf'";
        SQLParserListener listener = parse(input);

        assertEquals(1, listener.getProjectionColumnNames().size());
        assertEquals("col", listener.getProjectionColumnNames().get(0));
        assertEquals(1, listener.getTableNames().size());
        assertEquals("col2<13||col1>='narf'", listener.getExpressionText());
    }

    @Test
    public void testSelectionWithBooleanExpression3() {
        String input = "select col from tab1 where col2 = 13 or col1 = 'narf' or col3 = 17 and col4 > 19 AND col5 = 23";
        SQLParserListener listener = parse(input);

        assertEquals(1, listener.getProjectionColumnNames().size());
        assertEquals("col", listener.getProjectionColumnNames().get(0));
        assertEquals(1, listener.getTableNames().size());
        assertEquals("col2==13||col1=='narf'||col3==17&&col4>19&&col5==23", listener.getExpressionText());
    }

    @Test
    public void testStringLiteral() {
        String input = "col2 = \"string\"";
        CodePointCharStream stream = CharStreams.fromString(input);
        SQLLexer lex = new SQLLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        SQLParser parser = new SQLParser(tokens);
        SQLParser.Simple_expressionContext elementContext = parser.simple_expression();

        SQLParserListener listener = new SQLParserListener(tokens);
        ParseTreeWalker.DEFAULT.walk(listener, elementContext);
    }

    @Test
    public void testBinaryOperations() {
        SQLParserListener listener = parse("select col from tab1 where col2 = 13");
        assertEquals(1, listener.getProjectionColumnNames().size());
        assertEquals("col", listener.getProjectionColumnNames().get(0));
        assertEquals(1, listener.getTableNames().size());
        assertEquals(1, listener.getSelections().size());
        assertEquals("col2==13", listener.getExpressionText());
        ParsingResult.BinaryOperation bo = listener.getSelections().get(0);
        assertEquals("col2", bo.operand1);
        assertEquals("==", bo.operation);
        assertEquals("13", bo.operand2);

        listener = parse("select col from tab1 where 13 = col2");
        assertEquals(1, listener.getProjectionColumnNames().size());
        assertEquals("col", listener.getProjectionColumnNames().get(0));
        assertEquals(1, listener.getTableNames().size());
        assertEquals(1, listener.getSelections().size());
        assertEquals("13==col2", listener.getExpressionText());
        bo = listener.getSelections().get(0);
        assertEquals("col2", bo.operand1);
        assertEquals("==", bo.operation);
        assertEquals("13", bo.operand2);
    }

    @Test
    public void testSimpleJoins() {
        SQLParserListener listener = parse("select * from narf, moep where narf.id = moep.id");
        assertEquals(1, listener.getProjectionColumnNames().size());
        assertEquals("*", listener.getProjectionColumnNames().get(0));
        assertEquals(2, listener.getTableNames().size());
        assertEquals(0, listener.getSelections().size());
        assertEquals("narf.id==moep.id", listener.getExpressionText());

        assertEquals(1, listener.getJoins().size());
        ParsingResult.BinaryOperation join = listener.getJoins().get(0);
        assertEquals("narf.id", join.operand1);
        assertEquals("==", join.operation);
        assertEquals("moep.id", join.operand2);
    }

    @Test
    public void testJoins() {
        SQLParserListener listener = parse("select col from tab1 t1, tab2 t2 where col2 = 13 AND t1.id = t2.id");
        assertEquals(1, listener.getProjectionColumnNames().size());
        assertEquals("col", listener.getProjectionColumnNames().get(0));
        assertEquals(2, listener.getTableNames().size());
        assertEquals(1, listener.getSelections().size());
        assertEquals("col2==13&&t1.id==t2.id", listener.getExpressionText());
        ParsingResult.BinaryOperation bo = listener.getSelections().get(0);
        assertEquals("col2", bo.operand1);
        assertEquals("==", bo.operation);
        assertEquals("13", bo.operand2);

        assertEquals(1, listener.getJoins().size());
        ParsingResult.BinaryOperation join = listener.getJoins().get(0);
        assertEquals("t1.id", join.operand1);
        assertEquals("==", join.operation);
        assertEquals("t2.id", join.operand2);
    }

    private SQLParserListener parse(String query) {
        CodePointCharStream stream = CharStreams.fromString(query);
        SQLLexer lex = new SQLLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        SQLParser parser = new SQLParser(tokens);
        SQLParser.StmtContext stmtCtx = parser.stmt();

        SQLParserListener listener = new SQLParserListener(tokens);
        ParseTreeWalker.DEFAULT.walk(listener, stmtCtx);
        return listener;
    }
}
