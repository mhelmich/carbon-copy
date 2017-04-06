package org.distbc.parser;

import org.distbc.parser.gen.SQLParser;
import org.distbc.parser.gen.SQLParserBaseListener;

import java.util.ArrayList;
import java.util.List;

class SQLParserListener extends SQLParserBaseListener implements ParsingResult {

    private List<String> tableNames = new ArrayList<>();
    private List<String> projectionColumnNames = new ArrayList<>();
    private List<String> whereClauses = new ArrayList<>();
    private List<String> joinClauses = new ArrayList<>();

    @Override
    public void exitTable_name(SQLParser.Table_nameContext ctx) {
        tableNames.add(ctx.getText());
    }

    @Override
    public void exitColumn_name(SQLParser.Column_nameContext ctx) {
        projectionColumnNames.add(ctx.getText());
    }

    @Override
    public void exitSimple_expression(SQLParser.Simple_expressionContext ctx) {
        whereClauses.add(ctx.getText());
    }

    @Override
    public List<String> getTableNames() {
        return tableNames;
    }

    @Override
    public List<String> getProjectionColumnNames() {
        return projectionColumnNames;
    }

    @Override
    public List<String> getWhereClauses() {
        return whereClauses;
    }

    @Override
    public List<String> getJoinClauses() {
        return joinClauses;
    }
}
