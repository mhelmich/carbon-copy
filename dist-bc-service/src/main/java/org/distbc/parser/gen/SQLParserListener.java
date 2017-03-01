// Generated from /Users/mhelmich/playground/projects/dist-bc/dist-bc-service/src/main/resources/SQLParser.g4 by ANTLR 4.6

package org.distbc.parser.gen;

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link SQLParser}.
 */
public interface SQLParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link SQLParser#stmt}.
	 * @param ctx the parse tree
	 */
	void enterStmt(SQLParser.StmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#stmt}.
	 * @param ctx the parse tree
	 */
	void exitStmt(SQLParser.StmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#schema_name}.
	 * @param ctx the parse tree
	 */
	void enterSchema_name(SQLParser.Schema_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#schema_name}.
	 * @param ctx the parse tree
	 */
	void exitSchema_name(SQLParser.Schema_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#select_clause}.
	 * @param ctx the parse tree
	 */
	void enterSelect_clause(SQLParser.Select_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#select_clause}.
	 * @param ctx the parse tree
	 */
	void exitSelect_clause(SQLParser.Select_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#table_name}.
	 * @param ctx the parse tree
	 */
	void enterTable_name(SQLParser.Table_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#table_name}.
	 * @param ctx the parse tree
	 */
	void exitTable_name(SQLParser.Table_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#table_alias}.
	 * @param ctx the parse tree
	 */
	void enterTable_alias(SQLParser.Table_aliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#table_alias}.
	 * @param ctx the parse tree
	 */
	void exitTable_alias(SQLParser.Table_aliasContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#column_name}.
	 * @param ctx the parse tree
	 */
	void enterColumn_name(SQLParser.Column_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#column_name}.
	 * @param ctx the parse tree
	 */
	void exitColumn_name(SQLParser.Column_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#column_name_alias}.
	 * @param ctx the parse tree
	 */
	void enterColumn_name_alias(SQLParser.Column_name_aliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#column_name_alias}.
	 * @param ctx the parse tree
	 */
	void exitColumn_name_alias(SQLParser.Column_name_aliasContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#index_name}.
	 * @param ctx the parse tree
	 */
	void enterIndex_name(SQLParser.Index_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#index_name}.
	 * @param ctx the parse tree
	 */
	void exitIndex_name(SQLParser.Index_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#column_list}.
	 * @param ctx the parse tree
	 */
	void enterColumn_list(SQLParser.Column_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#column_list}.
	 * @param ctx the parse tree
	 */
	void exitColumn_list(SQLParser.Column_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#column_list_clause}.
	 * @param ctx the parse tree
	 */
	void enterColumn_list_clause(SQLParser.Column_list_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#column_list_clause}.
	 * @param ctx the parse tree
	 */
	void exitColumn_list_clause(SQLParser.Column_list_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#from_clause}.
	 * @param ctx the parse tree
	 */
	void enterFrom_clause(SQLParser.From_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#from_clause}.
	 * @param ctx the parse tree
	 */
	void exitFrom_clause(SQLParser.From_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#select_key}.
	 * @param ctx the parse tree
	 */
	void enterSelect_key(SQLParser.Select_keyContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#select_key}.
	 * @param ctx the parse tree
	 */
	void exitSelect_key(SQLParser.Select_keyContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#where_clause}.
	 * @param ctx the parse tree
	 */
	void enterWhere_clause(SQLParser.Where_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#where_clause}.
	 * @param ctx the parse tree
	 */
	void exitWhere_clause(SQLParser.Where_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(SQLParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(SQLParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#element}.
	 * @param ctx the parse tree
	 */
	void enterElement(SQLParser.ElementContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#element}.
	 * @param ctx the parse tree
	 */
	void exitElement(SQLParser.ElementContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#right_element}.
	 * @param ctx the parse tree
	 */
	void enterRight_element(SQLParser.Right_elementContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#right_element}.
	 * @param ctx the parse tree
	 */
	void exitRight_element(SQLParser.Right_elementContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#left_element}.
	 * @param ctx the parse tree
	 */
	void enterLeft_element(SQLParser.Left_elementContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#left_element}.
	 * @param ctx the parse tree
	 */
	void exitLeft_element(SQLParser.Left_elementContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#target_element}.
	 * @param ctx the parse tree
	 */
	void enterTarget_element(SQLParser.Target_elementContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#target_element}.
	 * @param ctx the parse tree
	 */
	void exitTarget_element(SQLParser.Target_elementContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#relational_op}.
	 * @param ctx the parse tree
	 */
	void enterRelational_op(SQLParser.Relational_opContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#relational_op}.
	 * @param ctx the parse tree
	 */
	void exitRelational_op(SQLParser.Relational_opContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#expr_op}.
	 * @param ctx the parse tree
	 */
	void enterExpr_op(SQLParser.Expr_opContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#expr_op}.
	 * @param ctx the parse tree
	 */
	void exitExpr_op(SQLParser.Expr_opContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#between_op}.
	 * @param ctx the parse tree
	 */
	void enterBetween_op(SQLParser.Between_opContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#between_op}.
	 * @param ctx the parse tree
	 */
	void exitBetween_op(SQLParser.Between_opContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#is_or_is_not}.
	 * @param ctx the parse tree
	 */
	void enterIs_or_is_not(SQLParser.Is_or_is_notContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#is_or_is_not}.
	 * @param ctx the parse tree
	 */
	void exitIs_or_is_not(SQLParser.Is_or_is_notContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#simple_expression}.
	 * @param ctx the parse tree
	 */
	void enterSimple_expression(SQLParser.Simple_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#simple_expression}.
	 * @param ctx the parse tree
	 */
	void exitSimple_expression(SQLParser.Simple_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#table_references}.
	 * @param ctx the parse tree
	 */
	void enterTable_references(SQLParser.Table_referencesContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#table_references}.
	 * @param ctx the parse tree
	 */
	void exitTable_references(SQLParser.Table_referencesContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#table_reference}.
	 * @param ctx the parse tree
	 */
	void enterTable_reference(SQLParser.Table_referenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#table_reference}.
	 * @param ctx the parse tree
	 */
	void exitTable_reference(SQLParser.Table_referenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#table_factor1}.
	 * @param ctx the parse tree
	 */
	void enterTable_factor1(SQLParser.Table_factor1Context ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#table_factor1}.
	 * @param ctx the parse tree
	 */
	void exitTable_factor1(SQLParser.Table_factor1Context ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#table_factor2}.
	 * @param ctx the parse tree
	 */
	void enterTable_factor2(SQLParser.Table_factor2Context ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#table_factor2}.
	 * @param ctx the parse tree
	 */
	void exitTable_factor2(SQLParser.Table_factor2Context ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#table_factor3}.
	 * @param ctx the parse tree
	 */
	void enterTable_factor3(SQLParser.Table_factor3Context ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#table_factor3}.
	 * @param ctx the parse tree
	 */
	void exitTable_factor3(SQLParser.Table_factor3Context ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#table_factor4}.
	 * @param ctx the parse tree
	 */
	void enterTable_factor4(SQLParser.Table_factor4Context ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#table_factor4}.
	 * @param ctx the parse tree
	 */
	void exitTable_factor4(SQLParser.Table_factor4Context ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#table_atom}.
	 * @param ctx the parse tree
	 */
	void enterTable_atom(SQLParser.Table_atomContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#table_atom}.
	 * @param ctx the parse tree
	 */
	void exitTable_atom(SQLParser.Table_atomContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#join_clause}.
	 * @param ctx the parse tree
	 */
	void enterJoin_clause(SQLParser.Join_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#join_clause}.
	 * @param ctx the parse tree
	 */
	void exitJoin_clause(SQLParser.Join_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#join_condition}.
	 * @param ctx the parse tree
	 */
	void enterJoin_condition(SQLParser.Join_conditionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#join_condition}.
	 * @param ctx the parse tree
	 */
	void exitJoin_condition(SQLParser.Join_conditionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#index_hint_list}.
	 * @param ctx the parse tree
	 */
	void enterIndex_hint_list(SQLParser.Index_hint_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#index_hint_list}.
	 * @param ctx the parse tree
	 */
	void exitIndex_hint_list(SQLParser.Index_hint_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#index_options}.
	 * @param ctx the parse tree
	 */
	void enterIndex_options(SQLParser.Index_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#index_options}.
	 * @param ctx the parse tree
	 */
	void exitIndex_options(SQLParser.Index_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#index_hint}.
	 * @param ctx the parse tree
	 */
	void enterIndex_hint(SQLParser.Index_hintContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#index_hint}.
	 * @param ctx the parse tree
	 */
	void exitIndex_hint(SQLParser.Index_hintContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#index_list}.
	 * @param ctx the parse tree
	 */
	void enterIndex_list(SQLParser.Index_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#index_list}.
	 * @param ctx the parse tree
	 */
	void exitIndex_list(SQLParser.Index_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#partition_clause}.
	 * @param ctx the parse tree
	 */
	void enterPartition_clause(SQLParser.Partition_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#partition_clause}.
	 * @param ctx the parse tree
	 */
	void exitPartition_clause(SQLParser.Partition_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#partition_names}.
	 * @param ctx the parse tree
	 */
	void enterPartition_names(SQLParser.Partition_namesContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#partition_names}.
	 * @param ctx the parse tree
	 */
	void exitPartition_names(SQLParser.Partition_namesContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#partition_name}.
	 * @param ctx the parse tree
	 */
	void enterPartition_name(SQLParser.Partition_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#partition_name}.
	 * @param ctx the parse tree
	 */
	void exitPartition_name(SQLParser.Partition_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#subquery_alias}.
	 * @param ctx the parse tree
	 */
	void enterSubquery_alias(SQLParser.Subquery_aliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#subquery_alias}.
	 * @param ctx the parse tree
	 */
	void exitSubquery_alias(SQLParser.Subquery_aliasContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#subquery}.
	 * @param ctx the parse tree
	 */
	void enterSubquery(SQLParser.SubqueryContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#subquery}.
	 * @param ctx the parse tree
	 */
	void exitSubquery(SQLParser.SubqueryContext ctx);
}