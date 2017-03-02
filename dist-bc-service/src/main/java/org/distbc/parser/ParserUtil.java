package org.distbc.parser;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.distbc.parser.gen.SQLLexer;
import org.distbc.parser.gen.SQLParser;

public class ParserUtil {
    private final static ParserUtil instance = new ParserUtil();
    private ParserUtil() {}

    public static ParserUtil get() {
        return instance;
    }

    public ParsingResult parse(String query) {
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
