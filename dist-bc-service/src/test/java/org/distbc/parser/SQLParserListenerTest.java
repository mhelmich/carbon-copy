package org.distbc.parser;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.distbc.parser.gen.SQLLexer;
import org.distbc.parser.gen.SQLParser;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class SQLParserListenerTest {
    @Test
    public void testBasic() {
        String input = "select * from narf";
        SQLParserListener listener = parse(input);

        assertEquals(1, listener.getProjectionColumnNames().size());
        assertEquals("*", listener.getProjectionColumnNames().get(0));
        assertEquals(1, listener.getTableNames().size());
        assertEquals("narf", listener.getTableNames().get(0));
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
    }

    @Test
    public void testMultipleTables() {
        String input = "select col from tab1,tab2, tab3 , tab4";
        SQLParserListener listener = parse(input);

        assertEquals(1, listener.getProjectionColumnNames().size());
        assertEquals("col", listener.getProjectionColumnNames().get(0));
        assertEquals(4, listener.getTableNames().size());
        for (int i = 0; i < listener.getTableNames().size(); i++) {
            assertEquals("tab" + (i + 1), listener.getTableNames().get(i));
        }
    }

    @Test
    public void testSelection() {
        String input = "select col from tab1 where col2 = 13";
        SQLParserListener listener = parse(input);

        assertEquals(1, listener.getProjectionColumnNames().size());
        assertEquals("col", listener.getProjectionColumnNames().get(0));
        assertEquals(1, listener.getTableNames().size());
        assertEquals(1, listener.getWhereClauses().size());
        assertEquals("col2=13", listener.getWhereClauses().get(0));

        SQLParser parser = new SQLParser(new CommonTokenStream(new SQLLexer(new ANTLRInputStream(listener.getWhereClauses().get(0)))));
        List<String> exprComponents = parser.simple_expression().children.stream().map(ParseTree::getText).collect(Collectors.toList());
        assertEquals(3, exprComponents.size());
        assertEquals("col2", exprComponents.get(0));
        assertEquals("=", exprComponents.get(1));
        assertEquals("13", exprComponents.get(2));
    }

    @Test
    public void testStringLiteral() {
        String input = "col2 = \"string\"";
        CharStream stream = new ANTLRInputStream(input);
        SQLLexer lex = new SQLLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        SQLParser parser = new SQLParser(tokens);
        SQLParser.Simple_expressionContext elementContext = parser.simple_expression();

        SQLParserListener listener = new SQLParserListener();
        ParseTreeWalker.DEFAULT.walk(listener, elementContext);
        assertEquals(1, listener.getWhereClauses().size());
        assertEquals("col2=\"string\"", listener.getWhereClauses().get(0));
    }

    private SQLParserListener parse(String query) {
        CharStream stream = new ANTLRInputStream(query);
        SQLLexer lex = new SQLLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        SQLParser parser = new SQLParser(tokens);
        SQLParser.StmtContext stmtCtx = parser.stmt();

        SQLParserListener listener = new SQLParserListener();
        ParseTreeWalker.DEFAULT.walk(listener, stmtCtx);
        return listener;
    }
}
