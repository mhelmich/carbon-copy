package org.distbc.parser;

import org.distbc.parser.gen.SQLParser;
import org.distbc.parser.gen.SQLParserBaseListener;

import java.util.ArrayList;
import java.util.List;

class SQLParserListener extends SQLParserBaseListener implements ParsingResult {

    private List<String> tableNames = new ArrayList<>();
    private List<String> columnNames = new ArrayList<>();
    private List<String> whereClauses = new ArrayList<>();
    private List<String> joinClauses = new ArrayList<>();

    @Override
    public void enterTable_name(SQLParser.Table_nameContext ctx) {
        tableNames.add(ctx.getText());
    }

    @Override
    public void enterColumn_name(SQLParser.Column_nameContext ctx) {
        columnNames.add(ctx.getText());
    }

    @Override public void enterSimple_expression(SQLParser.Simple_expressionContext ctx) {
        whereClauses.add(ctx.getText());
    }

    @Override
    public List<String> getTableNames() {
        return tableNames;
    }

    @Override
    public List<String> getColumnNames() {
        return columnNames;
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
