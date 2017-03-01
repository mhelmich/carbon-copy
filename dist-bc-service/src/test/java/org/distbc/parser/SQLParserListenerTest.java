package org.distbc.parser;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.distbc.parser.gen.SQLLexer;
import org.distbc.parser.gen.SQLParser;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SQLParserListenerTest {
    @Test
    public void testBasic() {
        String input = "select * from narf";
        SQLParserListener listener = parse(input);

        assertEquals(1, listener.getColumnNames().size());
        assertEquals("*", listener.getColumnNames().get(0));
        assertEquals(1, listener.getTableNames().size());
        assertEquals("narf", listener.getTableNames().get(0));
    }

    @Test
    public void testMultipleColumns() {
        String input = "select col1, col2,col3 , col4 from narf";
        SQLParserListener listener = parse(input);

        assertEquals(4, listener.getColumnNames().size());
        for (int i = 0; i < listener.getColumnNames().size(); i++) {
            assertEquals("col" + (i + 1), listener.getColumnNames().get(i));
        }
        assertEquals(1, listener.getTableNames().size());
        assertEquals("narf", listener.getTableNames().get(0));
    }

    @Test
    public void testMultipleTables() {
        String input = "select col from tab1,tab2, tab3 , tab4";
        SQLParserListener listener = parse(input);

        assertEquals(1, listener.getColumnNames().size());
        assertEquals("col", listener.getColumnNames().get(0));
        assertEquals(4, listener.getTableNames().size());
        for (int i = 0; i < listener.getTableNames().size(); i++) {
            assertEquals("tab" + (i + 1), listener.getTableNames().get(i));
        }
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
