package org.distbc.parser;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.distbc.parser.gen.SQLLexer;
import org.distbc.parser.gen.SQLParser;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ParserTest {
    @Test
    public void testBasic() {
        String input = "select * from narf";
        CharStream stream = new ANTLRInputStream(input);
        SQLLexer lex = new SQLLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        SQLParser parser = new SQLParser(tokens);
        SQLParser.StmtContext stmtCtx = parser.stmt();
        SQLParser.Select_clauseContext scCtx = stmtCtx.select_clause(0);

        List<SQLParser.Column_nameContext> columnNames = scCtx.column_list_clause().column_name();
        assertEquals(1, columnNames.size());
        for (SQLParser.Column_nameContext cn : columnNames) {
            assertEquals("*", cn.getText());
        }

        List<SQLParser.Table_referenceContext> tables = scCtx.table_references().table_reference();
        assertEquals(1, tables.size());
        for (SQLParser.Table_referenceContext table : tables) {
            assertEquals("narf", table.getText());
        }
    }
}
