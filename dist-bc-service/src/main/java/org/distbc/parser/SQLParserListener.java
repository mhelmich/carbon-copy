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

import org.distbc.parser.gen.SQLParser;
import org.distbc.parser.gen.SQLParserBaseListener;

import java.util.ArrayList;
import java.util.List;

class SQLParserListener extends SQLParserBaseListener implements ParsingResult {

    private List<String> tableNames = new ArrayList<>();
    private List<String> projectionColumnNames = new ArrayList<>();
    private List<String> whereClauses = new ArrayList<>();
    private List<String> joinClauses = new ArrayList<>();
    private List<String> columnNamesInWhereClauses = new ArrayList<>();
    private List<BinaryOperation> binaryOperations = new ArrayList<>();
    private String expressionText = null;

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
    public void exitColumn_name_in_where_clause(SQLParser.Column_name_in_where_clauseContext ctx) {
        columnNamesInWhereClauses.add(ctx.getText());
    }

    @Override
    public void exitRelational_op(SQLParser.Relational_opContext ctx) {
        SQLParser.Simple_expressionContext simpleExpressionCtx = (SQLParser.Simple_expressionContext) ctx.getParent();
        SQLParser.ElementContext leftElementContext = simpleExpressionCtx.left_element().element();
        SQLParser.ElementContext rightElementContext = simpleExpressionCtx.right_element().element();
        if (leftElementContext.column_name_in_where_clause() != null) {
            binaryOperations.add(new BinaryOperation(leftElementContext.getText(), ctx.getText(), rightElementContext.getText()));
        } else {
            binaryOperations.add(new BinaryOperation(rightElementContext.getText(), ctx.getText(), leftElementContext.getText()));
        }
    }

    @Override
    public void exitExpression(SQLParser.ExpressionContext ctx) {
        // this text has all whitespaces stripped out
        // and all non-relevant characters stripped out too
        // in order to get the original string, you can do something like this:
        // ctx.start.getInputStream()
        this.expressionText = ctx.getText();
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

    @Override
    public List<BinaryOperation> getBinaryOperations() {
        return binaryOperations;
    }

    @Override
    public String getExpressionText() {
        return expressionText;
    }
}
