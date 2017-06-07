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

package org.carbon.copy.parser;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.carbon.copy.parser.gen.SQLLexer;
import org.carbon.copy.parser.gen.SQLParser;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ParserTest {
    @Test
    public void testBasic() {
        String input = "select * from narf";
        CodePointCharStream stream = CharStreams.fromString(input);
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
