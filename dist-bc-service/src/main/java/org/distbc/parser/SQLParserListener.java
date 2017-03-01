package org.distbc.parser;

import org.distbc.parser.gen.SQLParser;
import org.distbc.parser.gen.SQLParserBaseListener;

import java.util.ArrayList;
import java.util.List;

public class SQLParserListener extends SQLParserBaseListener {

    private List<String> tableNames = new ArrayList<>();
    private List<String> columnNames = new ArrayList<>();

    @Override
    public void enterTable_name(SQLParser.Table_nameContext ctx) {
        tableNames.add(ctx.getText());
    }

    @Override
    public void enterColumn_name(SQLParser.Column_nameContext ctx) {
        columnNames.add(ctx.getText());
    }

    public List<String> getTableNames() {
        return tableNames;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }
}
