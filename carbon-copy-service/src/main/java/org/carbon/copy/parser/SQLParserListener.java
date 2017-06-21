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

import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.TokenStreamRewriter;
import org.carbon.copy.parser.gen.SQLParser;
import org.carbon.copy.parser.gen.SQLParserBaseListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class SQLParserListener extends SQLParserBaseListener implements ParsingResult {

    private final List<String> projectionColumnNames = new ArrayList<>();
//    private final Map<String, List<String>> tableNameToWhereClauses = new HashMap<>();
    private final List<BinaryOperation> selections = new ArrayList<>();
    private final Map<String, StringBuffer> tableNameToSelectionString = new HashMap<>();
    private final List<BinaryOperation> joins = new ArrayList<>();
    private final Map<String, String> tableAliasToTableName = new HashMap<>();
    // this rewriter contains operation string that are compatible with the expression evaluation
    // library we are using downstream
    private final TokenStreamRewriter operationRewriter;
    // this rewriter contains column names that are fully qualified
    private final TokenStreamRewriter columnNameRewriter;

    private String expressionText = "";

    SQLParserListener(TokenStream tokens) {
        this.operationRewriter = new TokenStreamRewriter(tokens);
        this.columnNameRewriter = new TokenStreamRewriter(tokens);
    }

    @Override
    public void exitColumn_name(SQLParser.Column_nameContext ctx) {
        projectionColumnNames.add(ctx.getText());
    }

    @Override
    public void exitRelational_op(SQLParser.Relational_opContext ctx) {
        // this code is here because of a library that is used to execute boolean expressions
        // it practically converts SQL syntax into the syntax of the library
        // not as nice from a code organization perspective but better than parsing the statement multiple times
        if (ctx.getToken(SQLParser.EQ, 0) != null && ctx.EQ().equals(ctx.getToken(SQLParser.EQ, 0))) {
            operationRewriter.replace(ctx.getStart(), "==");
        }

        SQLParser.Simple_expressionContext simpleExpressionCtx = (SQLParser.Simple_expressionContext) ctx.getParent();
        SQLParser.ElementContext leftElementContext = simpleExpressionCtx.left_element().element();
        SQLParser.ElementContext rightElementContext = simpleExpressionCtx.right_element().element();

        if (leftElementContext.column_name_in_where_clause() != null && rightElementContext.column_name_in_where_clause() == null) {
            selections.add(new BinaryOperation(leftElementContext.getText(), operationRewriter.getText(ctx.getSourceInterval()), rightElementContext.getText()));
            String[] tableAndColumnName = columnNameRewriter.getText(leftElementContext.getSourceInterval()).split("\\.");
            addSelectionForTable(tableAndColumnName[0], operationRewriter.getText(leftElementContext.getSourceInterval()));
        } else if (leftElementContext.column_name_in_where_clause() == null && rightElementContext.column_name_in_where_clause() != null) {
            selections.add(new BinaryOperation(rightElementContext.getText(), operationRewriter.getText(ctx.getSourceInterval()), leftElementContext.getText()));
            String[] tableAndColumnName = columnNameRewriter.getText(leftElementContext.getSourceInterval()).split("\\.");
            addSelectionForTable(tableAndColumnName[0], operationRewriter.getText(leftElementContext.getSourceInterval()));
        } else if (leftElementContext.column_name_in_where_clause() != null && rightElementContext.column_name_in_where_clause() != null) {
            // if we have a column name in both sides, this is a join
            joins.add(new BinaryOperation(leftElementContext.getText(), operationRewriter.getText(ctx.getSourceInterval()), rightElementContext.getText()));
        }
    }

    private void addSelectionForTable(String tableName, String addSelection) {
        StringBuffer selectionText = tableNameToSelectionString.computeIfAbsent(tableName, key -> new StringBuffer());
        selectionText.append(addSelection);
    }

    @Override
    public void exitExpression(SQLParser.ExpressionContext ctx) {
        // this text has all whitespaces stripped out
        // and all non-relevant characters are stripped out too
        // in order to get the original string, you can do something like this:
        // ctx.start.getInputStream()
        this.expressionText = operationRewriter.getText(ctx.getSourceInterval());
    }

    @Override
    public void exitTable_atom(SQLParser.Table_atomContext ctx) {
        String alias = (ctx.table_alias() != null) ? ctx.table_alias().getText() : ctx.table_name().getText();
        tableAliasToTableName.put(alias, ctx.table_name().getText());
    }

    @Override
    public void exitExpr_op(SQLParser.Expr_opContext ctx) {
        // this code is here because of a library that is used to execute boolean expressions
        // it practically converts SQL syntax into the syntax of the library
        // not as nice from a code organization perspective but better than parsing the statement multiple times
        if (ctx.getToken(SQLParser.AND, 0) != null && ctx.AND().equals(ctx.getToken(SQLParser.AND, 0))) {
            operationRewriter.replace(ctx.getStart(), "&&");
        } else if (ctx.getToken(SQLParser.OR, 0) != null && ctx.OR().equals(ctx.getToken(SQLParser.OR, 0))) {
            operationRewriter.replace(ctx.getStart(), "||");
        }
    }

    @Override
    public void exitColumn_name_in_where_clause(SQLParser.Column_name_in_where_clauseContext ctx) {
        if (ctx.table_alias() != null) {
            String tableName = tableAliasToTableName.get(ctx.table_alias().getText());
            if (tableName != null) {
                columnNameRewriter.insertBefore(ctx.getStart(), tableName + ".");
            }
        } else if (tableAliasToTableName.size() == 1) {
            String theOnlyTableNameWeHave = tableAliasToTableName.values().iterator().next();
            columnNameRewriter.insertBefore(ctx.getStart(), theOnlyTableNameWeHave + ".");
        }
    }

    @Override
    public List<String> getTableNames() {
        return new ArrayList<>(tableAliasToTableName.values());
    }

    @Override
    public List<String> getProjectionColumnNames() {
        return projectionColumnNames;
    }

    @Override
    public List<BinaryOperation> getSelections() {
        return selections;
    }

    @Override
    public List<BinaryOperation> getJoins() {
        return joins;
    }

    @Override
    public String getExpressionText() {
        return expressionText;
    }
}
