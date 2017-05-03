// Generated from /Users/mhelmich/playground/projects/dist-bc/dist-bc-service/src/main/resources/SQLParser.g4 by ANTLR 4.7

package org.distbc.parser.gen;

import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class SQLParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.7", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		SELECT=1, FROM=2, WHERE=3, AND=4, OR=5, XOR=6, IS=7, NULL=8, LIKE=9, IN=10, 
		EXISTS=11, ALL=12, ANY=13, TRUE=14, FALSE=15, DIVIDE=16, MOD=17, BETWEEN=18, 
		REGEXP=19, PLUS=20, MINUS=21, NEGATION=22, VERTBAR=23, BITAND=24, POWER_OP=25, 
		BINARY=26, SHIFT_LEFT=27, SHIFT_RIGHT=28, ESCAPE=29, ASTERISK=30, RPAREN=31, 
		LPAREN=32, RBRACK=33, LBRACK=34, COLON=35, ALL_FIELDS=36, EQ=37, LTH=38, 
		GTH=39, NOT_EQ=40, NOT=41, LET=42, GET=43, SEMI=44, COMMA=45, DOT=46, 
		COLLATE=47, INNER=48, OUTER=49, JOIN=50, CROSS=51, USING=52, INDEX=53, 
		KEY=54, ORDER=55, GROUP=56, BY=57, FOR=58, USE=59, IGNORE=60, PARTITION=61, 
		STRAIGHT_JOIN=62, NATURAL=63, LEFT=64, RIGHT=65, OJ=66, ON=67, ID=68, 
		QUOTED_LITERAL=69, INT=70, NEWLINE=71, WS=72, USER_VAR=73;
	public static final int
		RULE_stmt = 0, RULE_schema_name = 1, RULE_select_clause = 2, RULE_table_name = 3, 
		RULE_table_alias = 4, RULE_column_name = 5, RULE_column_name_in_where_clause = 6, 
		RULE_column_name_alias = 7, RULE_index_name = 8, RULE_column_list = 9, 
		RULE_column_list_clause = 10, RULE_where_clause = 11, RULE_expression = 12, 
		RULE_element = 13, RULE_literal_value = 14, RULE_right_element = 15, RULE_left_element = 16, 
		RULE_target_element = 17, RULE_relational_op = 18, RULE_expr_op = 19, 
		RULE_between_op = 20, RULE_is_or_is_not = 21, RULE_simple_expression = 22, 
		RULE_table_references = 23, RULE_table_reference = 24, RULE_table_factor1 = 25, 
		RULE_table_factor2 = 26, RULE_table_factor3 = 27, RULE_table_factor4 = 28, 
		RULE_table_atom = 29, RULE_join_clause = 30, RULE_join_condition = 31, 
		RULE_index_hint_list = 32, RULE_index_options = 33, RULE_index_hint = 34, 
		RULE_index_list = 35, RULE_partition_clause = 36, RULE_partition_names = 37, 
		RULE_partition_name = 38, RULE_subquery_alias = 39, RULE_subquery = 40;
	public static final String[] ruleNames = {
		"stmt", "schema_name", "select_clause", "table_name", "table_alias", "column_name", 
		"column_name_in_where_clause", "column_name_alias", "index_name", "column_list", 
		"column_list_clause", "where_clause", "expression", "element", "literal_value", 
		"right_element", "left_element", "target_element", "relational_op", "expr_op", 
		"between_op", "is_or_is_not", "simple_expression", "table_references", 
		"table_reference", "table_factor1", "table_factor2", "table_factor3", 
		"table_factor4", "table_atom", "join_clause", "join_condition", "index_hint_list", 
		"index_options", "index_hint", "index_list", "partition_clause", "partition_names", 
		"partition_name", "subquery_alias", "subquery"
	};

	private static final String[] _LITERAL_NAMES = {
		null, null, null, null, null, null, "'xor'", "'is'", "'null'", "'like'", 
		"'in'", "'exists'", "'all'", "'any'", "'true'", "'false'", null, null, 
		"'between'", "'regexp'", "'+'", "'-'", "'~'", "'|'", "'&'", "'^'", "'binary'", 
		"'<<'", "'>>'", "'escape'", "'*'", "')'", "'('", "']'", "'['", "':'", 
		"'.*'", "'='", "'<'", "'>'", "'!='", "'not'", "'<='", "'>='", "';'", "','", 
		"'.'", "'collate'", "'inner'", "'outer'", "'join'", "'cross'", "'using'", 
		"'index'", "'key'", "'order'", "'group'", "'by'", "'for'", "'use'", "'ignore'", 
		"'partition'", "'straight_join'", "'natural'", "'left'", "'right'", "'oj'", 
		"'on'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, "SELECT", "FROM", "WHERE", "AND", "OR", "XOR", "IS", "NULL", "LIKE", 
		"IN", "EXISTS", "ALL", "ANY", "TRUE", "FALSE", "DIVIDE", "MOD", "BETWEEN", 
		"REGEXP", "PLUS", "MINUS", "NEGATION", "VERTBAR", "BITAND", "POWER_OP", 
		"BINARY", "SHIFT_LEFT", "SHIFT_RIGHT", "ESCAPE", "ASTERISK", "RPAREN", 
		"LPAREN", "RBRACK", "LBRACK", "COLON", "ALL_FIELDS", "EQ", "LTH", "GTH", 
		"NOT_EQ", "NOT", "LET", "GET", "SEMI", "COMMA", "DOT", "COLLATE", "INNER", 
		"OUTER", "JOIN", "CROSS", "USING", "INDEX", "KEY", "ORDER", "GROUP", "BY", 
		"FOR", "USE", "IGNORE", "PARTITION", "STRAIGHT_JOIN", "NATURAL", "LEFT", 
		"RIGHT", "OJ", "ON", "ID", "QUOTED_LITERAL", "INT", "NEWLINE", "WS", "USER_VAR"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "SQLParser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public SQLParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class StmtContext extends ParserRuleContext {
		public List<Select_clauseContext> select_clause() {
			return getRuleContexts(Select_clauseContext.class);
		}
		public Select_clauseContext select_clause(int i) {
			return getRuleContext(Select_clauseContext.class,i);
		}
		public StmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).enterStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).exitStmt(this);
		}
	}

	public final StmtContext stmt() throws RecognitionException {
		StmtContext _localctx = new StmtContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_stmt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(83); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(82);
				select_clause();
				}
				}
				setState(85); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==SELECT );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Schema_nameContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(SQLParser.ID, 0); }
		public Schema_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_schema_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).enterSchema_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).exitSchema_name(this);
		}
	}

	public final Schema_nameContext schema_name() throws RecognitionException {
		Schema_nameContext _localctx = new Schema_nameContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_schema_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(87);
			match(ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Select_clauseContext extends ParserRuleContext {
		public TerminalNode SELECT() { return getToken(SQLParser.SELECT, 0); }
		public Column_list_clauseContext column_list_clause() {
			return getRuleContext(Column_list_clauseContext.class,0);
		}
		public TerminalNode FROM() { return getToken(SQLParser.FROM, 0); }
		public Table_referencesContext table_references() {
			return getRuleContext(Table_referencesContext.class,0);
		}
		public Where_clauseContext where_clause() {
			return getRuleContext(Where_clauseContext.class,0);
		}
		public Select_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_select_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).enterSelect_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).exitSelect_clause(this);
		}
	}

	public final Select_clauseContext select_clause() throws RecognitionException {
		Select_clauseContext _localctx = new Select_clauseContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_select_clause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(89);
			match(SELECT);
			setState(90);
			column_list_clause();
			setState(91);
			match(FROM);
			setState(92);
			table_references();
			setState(94);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WHERE) {
				{
				setState(93);
				where_clause();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Table_nameContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(SQLParser.ID, 0); }
		public Table_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_table_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).enterTable_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).exitTable_name(this);
		}
	}

	public final Table_nameContext table_name() throws RecognitionException {
		Table_nameContext _localctx = new Table_nameContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_table_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(96);
			match(ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Table_aliasContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(SQLParser.ID, 0); }
		public Table_aliasContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_table_alias; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).enterTable_alias(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).exitTable_alias(this);
		}
	}

	public final Table_aliasContext table_alias() throws RecognitionException {
		Table_aliasContext _localctx = new Table_aliasContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_table_alias);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(98);
			match(ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Column_nameContext extends ParserRuleContext {
		public List<TerminalNode> ID() { return getTokens(SQLParser.ID); }
		public TerminalNode ID(int i) {
			return getToken(SQLParser.ID, i);
		}
		public List<TerminalNode> DOT() { return getTokens(SQLParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(SQLParser.DOT, i);
		}
		public Column_name_aliasContext column_name_alias() {
			return getRuleContext(Column_name_aliasContext.class,0);
		}
		public Schema_nameContext schema_name() {
			return getRuleContext(Schema_nameContext.class,0);
		}
		public Table_aliasContext table_alias() {
			return getRuleContext(Table_aliasContext.class,0);
		}
		public TerminalNode USER_VAR() { return getToken(SQLParser.USER_VAR, 0); }
		public TerminalNode ASTERISK() { return getToken(SQLParser.ASTERISK, 0); }
		public Column_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_column_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).enterColumn_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).exitColumn_name(this);
		}
	}

	public final Column_nameContext column_name() throws RecognitionException {
		Column_nameContext _localctx = new Column_nameContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_column_name);
		try {
			setState(124);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(107);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
				case 1:
					{
					setState(103);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
					case 1:
						{
						setState(100);
						schema_name();
						setState(101);
						match(DOT);
						}
						break;
					}
					setState(105);
					match(ID);
					setState(106);
					match(DOT);
					}
					break;
				}
				setState(109);
				match(ID);
				setState(111);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
				case 1:
					{
					setState(110);
					column_name_alias();
					}
					break;
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(116);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
				case 1:
					{
					setState(113);
					table_alias();
					setState(114);
					match(DOT);
					}
					break;
				}
				setState(118);
				match(ID);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(119);
				match(USER_VAR);
				setState(121);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
				case 1:
					{
					setState(120);
					column_name_alias();
					}
					break;
				}
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(123);
				match(ASTERISK);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Column_name_in_where_clauseContext extends ParserRuleContext {
		public Column_nameContext column_name() {
			return getRuleContext(Column_nameContext.class,0);
		}
		public Table_aliasContext table_alias() {
			return getRuleContext(Table_aliasContext.class,0);
		}
		public List<TerminalNode> DOT() { return getTokens(SQLParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(SQLParser.DOT, i);
		}
		public Column_name_aliasContext column_name_alias() {
			return getRuleContext(Column_name_aliasContext.class,0);
		}
		public Schema_nameContext schema_name() {
			return getRuleContext(Schema_nameContext.class,0);
		}
		public TerminalNode USER_VAR() { return getToken(SQLParser.USER_VAR, 0); }
		public TerminalNode ASTERISK() { return getToken(SQLParser.ASTERISK, 0); }
		public Column_name_in_where_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_column_name_in_where_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).enterColumn_name_in_where_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).exitColumn_name_in_where_clause(this);
		}
	}

	public final Column_name_in_where_clauseContext column_name_in_where_clause() throws RecognitionException {
		Column_name_in_where_clauseContext _localctx = new Column_name_in_where_clauseContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_column_name_in_where_clause);
		int _la;
		try {
			setState(151);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,13,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(129);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,8,_ctx) ) {
				case 1:
					{
					setState(126);
					table_alias();
					setState(127);
					match(DOT);
					}
					break;
				}
				setState(131);
				column_name();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(140);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,10,_ctx) ) {
				case 1:
					{
					setState(135);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
					case 1:
						{
						setState(132);
						schema_name();
						setState(133);
						match(DOT);
						}
						break;
					}
					setState(137);
					table_alias();
					setState(138);
					match(DOT);
					}
					break;
				}
				setState(142);
				column_name();
				setState(144);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ID) {
					{
					setState(143);
					column_name_alias();
					}
				}

				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(146);
				match(USER_VAR);
				setState(148);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ID) {
					{
					setState(147);
					column_name_alias();
					}
				}

				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(150);
				match(ASTERISK);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Column_name_aliasContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(SQLParser.ID, 0); }
		public Column_name_aliasContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_column_name_alias; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).enterColumn_name_alias(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).exitColumn_name_alias(this);
		}
	}

	public final Column_name_aliasContext column_name_alias() throws RecognitionException {
		Column_name_aliasContext _localctx = new Column_name_aliasContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_column_name_alias);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(153);
			match(ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Index_nameContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(SQLParser.ID, 0); }
		public Index_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_index_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).enterIndex_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).exitIndex_name(this);
		}
	}

	public final Index_nameContext index_name() throws RecognitionException {
		Index_nameContext _localctx = new Index_nameContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_index_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(155);
			match(ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Column_listContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(SQLParser.LPAREN, 0); }
		public List<Column_nameContext> column_name() {
			return getRuleContexts(Column_nameContext.class);
		}
		public Column_nameContext column_name(int i) {
			return getRuleContext(Column_nameContext.class,i);
		}
		public TerminalNode RPAREN() { return getToken(SQLParser.RPAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(SQLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SQLParser.COMMA, i);
		}
		public Column_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_column_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).enterColumn_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).exitColumn_list(this);
		}
	}

	public final Column_listContext column_list() throws RecognitionException {
		Column_listContext _localctx = new Column_listContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_column_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(157);
			match(LPAREN);
			setState(158);
			column_name();
			setState(163);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(159);
				match(COMMA);
				setState(160);
				column_name();
				}
				}
				setState(165);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(166);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Column_list_clauseContext extends ParserRuleContext {
		public List<Column_nameContext> column_name() {
			return getRuleContexts(Column_nameContext.class);
		}
		public Column_nameContext column_name(int i) {
			return getRuleContext(Column_nameContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(SQLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SQLParser.COMMA, i);
		}
		public Column_list_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_column_list_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).enterColumn_list_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).exitColumn_list_clause(this);
		}
	}

	public final Column_list_clauseContext column_list_clause() throws RecognitionException {
		Column_list_clauseContext _localctx = new Column_list_clauseContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_column_list_clause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(168);
			column_name();
			setState(173);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(169);
				match(COMMA);
				setState(170);
				column_name();
				}
				}
				setState(175);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Where_clauseContext extends ParserRuleContext {
		public TerminalNode WHERE() { return getToken(SQLParser.WHERE, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public Where_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_where_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).enterWhere_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).exitWhere_clause(this);
		}
	}

	public final Where_clauseContext where_clause() throws RecognitionException {
		Where_clauseContext _localctx = new Where_clauseContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_where_clause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(176);
			match(WHERE);
			setState(177);
			expression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExpressionContext extends ParserRuleContext {
		public List<Simple_expressionContext> simple_expression() {
			return getRuleContexts(Simple_expressionContext.class);
		}
		public Simple_expressionContext simple_expression(int i) {
			return getRuleContext(Simple_expressionContext.class,i);
		}
		public List<Expr_opContext> expr_op() {
			return getRuleContexts(Expr_opContext.class);
		}
		public Expr_opContext expr_op(int i) {
			return getRuleContext(Expr_opContext.class,i);
		}
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).enterExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).exitExpression(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		ExpressionContext _localctx = new ExpressionContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_expression);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(179);
			simple_expression();
			setState(185);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,16,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(180);
					expr_op();
					setState(181);
					simple_expression();
					}
					} 
				}
				setState(187);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,16,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ElementContext extends ParserRuleContext {
		public Literal_valueContext literal_value() {
			return getRuleContext(Literal_valueContext.class,0);
		}
		public Column_name_in_where_clauseContext column_name_in_where_clause() {
			return getRuleContext(Column_name_in_where_clauseContext.class,0);
		}
		public TerminalNode USER_VAR() { return getToken(SQLParser.USER_VAR, 0); }
		public TerminalNode ID() { return getToken(SQLParser.ID, 0); }
		public ElementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_element; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).enterElement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).exitElement(this);
		}
	}

	public final ElementContext element() throws RecognitionException {
		ElementContext _localctx = new ElementContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_element);
		try {
			setState(194);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,17,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(188);
				literal_value();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(189);
				column_name_in_where_clause();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(190);
				match(USER_VAR);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				{
				setState(191);
				match(VERTBAR);
				setState(192);
				match(ID);
				setState(193);
				match(VERTBAR);
				}
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Literal_valueContext extends ParserRuleContext {
		public TerminalNode QUOTED_LITERAL() { return getToken(SQLParser.QUOTED_LITERAL, 0); }
		public TerminalNode INT() { return getToken(SQLParser.INT, 0); }
		public TerminalNode NULL() { return getToken(SQLParser.NULL, 0); }
		public Literal_valueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_literal_value; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).enterLiteral_value(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).exitLiteral_value(this);
		}
	}

	public final Literal_valueContext literal_value() throws RecognitionException {
		Literal_valueContext _localctx = new Literal_valueContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_literal_value);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(196);
			_la = _input.LA(1);
			if ( !(((((_la - 8)) & ~0x3f) == 0 && ((1L << (_la - 8)) & ((1L << (NULL - 8)) | (1L << (QUOTED_LITERAL - 8)) | (1L << (INT - 8)))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Right_elementContext extends ParserRuleContext {
		public ElementContext element() {
			return getRuleContext(ElementContext.class,0);
		}
		public Right_elementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_right_element; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).enterRight_element(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).exitRight_element(this);
		}
	}

	public final Right_elementContext right_element() throws RecognitionException {
		Right_elementContext _localctx = new Right_elementContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_right_element);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(198);
			element();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Left_elementContext extends ParserRuleContext {
		public ElementContext element() {
			return getRuleContext(ElementContext.class,0);
		}
		public Left_elementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_left_element; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).enterLeft_element(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).exitLeft_element(this);
		}
	}

	public final Left_elementContext left_element() throws RecognitionException {
		Left_elementContext _localctx = new Left_elementContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_left_element);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(200);
			element();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Target_elementContext extends ParserRuleContext {
		public ElementContext element() {
			return getRuleContext(ElementContext.class,0);
		}
		public Target_elementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_target_element; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).enterTarget_element(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).exitTarget_element(this);
		}
	}

	public final Target_elementContext target_element() throws RecognitionException {
		Target_elementContext _localctx = new Target_elementContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_target_element);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(202);
			element();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Relational_opContext extends ParserRuleContext {
		public TerminalNode EQ() { return getToken(SQLParser.EQ, 0); }
		public TerminalNode LTH() { return getToken(SQLParser.LTH, 0); }
		public TerminalNode GTH() { return getToken(SQLParser.GTH, 0); }
		public TerminalNode NOT_EQ() { return getToken(SQLParser.NOT_EQ, 0); }
		public TerminalNode LET() { return getToken(SQLParser.LET, 0); }
		public TerminalNode GET() { return getToken(SQLParser.GET, 0); }
		public Relational_opContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_relational_op; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).enterRelational_op(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).exitRelational_op(this);
		}
	}

	public final Relational_opContext relational_op() throws RecognitionException {
		Relational_opContext _localctx = new Relational_opContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_relational_op);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(204);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << EQ) | (1L << LTH) | (1L << GTH) | (1L << NOT_EQ) | (1L << LET) | (1L << GET))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Expr_opContext extends ParserRuleContext {
		public TerminalNode AND() { return getToken(SQLParser.AND, 0); }
		public TerminalNode XOR() { return getToken(SQLParser.XOR, 0); }
		public TerminalNode OR() { return getToken(SQLParser.OR, 0); }
		public TerminalNode NOT() { return getToken(SQLParser.NOT, 0); }
		public Expr_opContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr_op; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).enterExpr_op(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).exitExpr_op(this);
		}
	}

	public final Expr_opContext expr_op() throws RecognitionException {
		Expr_opContext _localctx = new Expr_opContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_expr_op);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(206);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << AND) | (1L << OR) | (1L << XOR) | (1L << NOT))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Between_opContext extends ParserRuleContext {
		public TerminalNode BETWEEN() { return getToken(SQLParser.BETWEEN, 0); }
		public Between_opContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_between_op; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).enterBetween_op(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).exitBetween_op(this);
		}
	}

	public final Between_opContext between_op() throws RecognitionException {
		Between_opContext _localctx = new Between_opContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_between_op);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(208);
			match(BETWEEN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Is_or_is_notContext extends ParserRuleContext {
		public TerminalNode IS() { return getToken(SQLParser.IS, 0); }
		public TerminalNode NOT() { return getToken(SQLParser.NOT, 0); }
		public Is_or_is_notContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_is_or_is_not; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).enterIs_or_is_not(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).exitIs_or_is_not(this);
		}
	}

	public final Is_or_is_notContext is_or_is_not() throws RecognitionException {
		Is_or_is_notContext _localctx = new Is_or_is_notContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_is_or_is_not);
		try {
			setState(213);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,18,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(210);
				match(IS);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(211);
				match(IS);
				setState(212);
				match(NOT);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Simple_expressionContext extends ParserRuleContext {
		public Left_elementContext left_element() {
			return getRuleContext(Left_elementContext.class,0);
		}
		public Relational_opContext relational_op() {
			return getRuleContext(Relational_opContext.class,0);
		}
		public Right_elementContext right_element() {
			return getRuleContext(Right_elementContext.class,0);
		}
		public Target_elementContext target_element() {
			return getRuleContext(Target_elementContext.class,0);
		}
		public Between_opContext between_op() {
			return getRuleContext(Between_opContext.class,0);
		}
		public TerminalNode AND() { return getToken(SQLParser.AND, 0); }
		public Is_or_is_notContext is_or_is_not() {
			return getRuleContext(Is_or_is_notContext.class,0);
		}
		public TerminalNode NULL() { return getToken(SQLParser.NULL, 0); }
		public Simple_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_simple_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).enterSimple_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).exitSimple_expression(this);
		}
	}

	public final Simple_expressionContext simple_expression() throws RecognitionException {
		Simple_expressionContext _localctx = new Simple_expressionContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_simple_expression);
		try {
			setState(229);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,19,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(215);
				left_element();
				setState(216);
				relational_op();
				setState(217);
				right_element();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(219);
				target_element();
				setState(220);
				between_op();
				setState(221);
				left_element();
				setState(222);
				match(AND);
				setState(223);
				right_element();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(225);
				target_element();
				setState(226);
				is_or_is_not();
				setState(227);
				match(NULL);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Table_referencesContext extends ParserRuleContext {
		public List<Table_referenceContext> table_reference() {
			return getRuleContexts(Table_referenceContext.class);
		}
		public Table_referenceContext table_reference(int i) {
			return getRuleContext(Table_referenceContext.class,i);
		}
		public List<Join_clauseContext> join_clause() {
			return getRuleContexts(Join_clauseContext.class);
		}
		public Join_clauseContext join_clause(int i) {
			return getRuleContext(Join_clauseContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(SQLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SQLParser.COMMA, i);
		}
		public Table_referencesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_table_references; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).enterTable_references(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).exitTable_references(this);
		}
	}

	public final Table_referencesContext table_references() throws RecognitionException {
		Table_referencesContext _localctx = new Table_referencesContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_table_references);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(231);
			table_reference();
			setState(237);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((((_la - 45)) & ~0x3f) == 0 && ((1L << (_la - 45)) & ((1L << (COMMA - 45)) | (1L << (INNER - 45)) | (1L << (JOIN - 45)) | (1L << (CROSS - 45)) | (1L << (STRAIGHT_JOIN - 45)) | (1L << (NATURAL - 45)) | (1L << (LEFT - 45)) | (1L << (RIGHT - 45)))) != 0)) {
				{
				setState(235);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case COMMA:
					{
					{
					setState(232);
					match(COMMA);
					setState(233);
					table_reference();
					}
					}
					break;
				case INNER:
				case JOIN:
				case CROSS:
				case STRAIGHT_JOIN:
				case NATURAL:
				case LEFT:
				case RIGHT:
					{
					setState(234);
					join_clause();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(239);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Table_referenceContext extends ParserRuleContext {
		public Table_factor1Context table_factor1() {
			return getRuleContext(Table_factor1Context.class,0);
		}
		public Table_atomContext table_atom() {
			return getRuleContext(Table_atomContext.class,0);
		}
		public Table_referenceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_table_reference; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).enterTable_reference(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).exitTable_reference(this);
		}
	}

	public final Table_referenceContext table_reference() throws RecognitionException {
		Table_referenceContext _localctx = new Table_referenceContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_table_reference);
		try {
			setState(242);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,22,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(240);
				table_factor1();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(241);
				table_atom();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Table_factor1Context extends ParserRuleContext {
		public Table_factor2Context table_factor2() {
			return getRuleContext(Table_factor2Context.class,0);
		}
		public TerminalNode JOIN() { return getToken(SQLParser.JOIN, 0); }
		public Table_atomContext table_atom() {
			return getRuleContext(Table_atomContext.class,0);
		}
		public Join_conditionContext join_condition() {
			return getRuleContext(Join_conditionContext.class,0);
		}
		public TerminalNode INNER() { return getToken(SQLParser.INNER, 0); }
		public TerminalNode CROSS() { return getToken(SQLParser.CROSS, 0); }
		public Table_factor1Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_table_factor1; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).enterTable_factor1(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).exitTable_factor1(this);
		}
	}

	public final Table_factor1Context table_factor1() throws RecognitionException {
		Table_factor1Context _localctx = new Table_factor1Context(_ctx, getState());
		enterRule(_localctx, 50, RULE_table_factor1);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(244);
			table_factor2();
			setState(253);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,25,_ctx) ) {
			case 1:
				{
				setState(246);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==INNER || _la==CROSS) {
					{
					setState(245);
					_la = _input.LA(1);
					if ( !(_la==INNER || _la==CROSS) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					}
				}

				setState(248);
				match(JOIN);
				setState(249);
				table_atom();
				setState(251);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,24,_ctx) ) {
				case 1:
					{
					setState(250);
					join_condition();
					}
					break;
				}
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Table_factor2Context extends ParserRuleContext {
		public Table_factor3Context table_factor3() {
			return getRuleContext(Table_factor3Context.class,0);
		}
		public TerminalNode STRAIGHT_JOIN() { return getToken(SQLParser.STRAIGHT_JOIN, 0); }
		public Table_atomContext table_atom() {
			return getRuleContext(Table_atomContext.class,0);
		}
		public TerminalNode ON() { return getToken(SQLParser.ON, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public Table_factor2Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_table_factor2; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).enterTable_factor2(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).exitTable_factor2(this);
		}
	}

	public final Table_factor2Context table_factor2() throws RecognitionException {
		Table_factor2Context _localctx = new Table_factor2Context(_ctx, getState());
		enterRule(_localctx, 52, RULE_table_factor2);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(255);
			table_factor3();
			setState(262);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,27,_ctx) ) {
			case 1:
				{
				setState(256);
				match(STRAIGHT_JOIN);
				setState(257);
				table_atom();
				setState(260);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,26,_ctx) ) {
				case 1:
					{
					setState(258);
					match(ON);
					setState(259);
					expression();
					}
					break;
				}
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Table_factor3Context extends ParserRuleContext {
		public List<Table_factor4Context> table_factor4() {
			return getRuleContexts(Table_factor4Context.class);
		}
		public Table_factor4Context table_factor4(int i) {
			return getRuleContext(Table_factor4Context.class,i);
		}
		public TerminalNode JOIN() { return getToken(SQLParser.JOIN, 0); }
		public Join_conditionContext join_condition() {
			return getRuleContext(Join_conditionContext.class,0);
		}
		public TerminalNode LEFT() { return getToken(SQLParser.LEFT, 0); }
		public TerminalNode RIGHT() { return getToken(SQLParser.RIGHT, 0); }
		public TerminalNode OUTER() { return getToken(SQLParser.OUTER, 0); }
		public Table_factor3Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_table_factor3; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).enterTable_factor3(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).exitTable_factor3(this);
		}
	}

	public final Table_factor3Context table_factor3() throws RecognitionException {
		Table_factor3Context _localctx = new Table_factor3Context(_ctx, getState());
		enterRule(_localctx, 54, RULE_table_factor3);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(264);
			table_factor4();
			setState(273);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,29,_ctx) ) {
			case 1:
				{
				setState(265);
				_la = _input.LA(1);
				if ( !(_la==LEFT || _la==RIGHT) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(267);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OUTER) {
					{
					setState(266);
					match(OUTER);
					}
				}

				setState(269);
				match(JOIN);
				setState(270);
				table_factor4();
				setState(271);
				join_condition();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Table_factor4Context extends ParserRuleContext {
		public List<Table_atomContext> table_atom() {
			return getRuleContexts(Table_atomContext.class);
		}
		public Table_atomContext table_atom(int i) {
			return getRuleContext(Table_atomContext.class,i);
		}
		public TerminalNode NATURAL() { return getToken(SQLParser.NATURAL, 0); }
		public TerminalNode JOIN() { return getToken(SQLParser.JOIN, 0); }
		public TerminalNode LEFT() { return getToken(SQLParser.LEFT, 0); }
		public TerminalNode RIGHT() { return getToken(SQLParser.RIGHT, 0); }
		public TerminalNode OUTER() { return getToken(SQLParser.OUTER, 0); }
		public Table_factor4Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_table_factor4; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).enterTable_factor4(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).exitTable_factor4(this);
		}
	}

	public final Table_factor4Context table_factor4() throws RecognitionException {
		Table_factor4Context _localctx = new Table_factor4Context(_ctx, getState());
		enterRule(_localctx, 56, RULE_table_factor4);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(275);
			table_atom();
			setState(285);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,32,_ctx) ) {
			case 1:
				{
				setState(276);
				match(NATURAL);
				setState(281);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LEFT || _la==RIGHT) {
					{
					setState(277);
					_la = _input.LA(1);
					if ( !(_la==LEFT || _la==RIGHT) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(279);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==OUTER) {
						{
						setState(278);
						match(OUTER);
						}
					}

					}
				}

				setState(283);
				match(JOIN);
				setState(284);
				table_atom();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Table_atomContext extends ParserRuleContext {
		public Table_nameContext table_name() {
			return getRuleContext(Table_nameContext.class,0);
		}
		public Partition_clauseContext partition_clause() {
			return getRuleContext(Partition_clauseContext.class,0);
		}
		public Table_aliasContext table_alias() {
			return getRuleContext(Table_aliasContext.class,0);
		}
		public Index_hint_listContext index_hint_list() {
			return getRuleContext(Index_hint_listContext.class,0);
		}
		public SubqueryContext subquery() {
			return getRuleContext(SubqueryContext.class,0);
		}
		public Subquery_aliasContext subquery_alias() {
			return getRuleContext(Subquery_aliasContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(SQLParser.LPAREN, 0); }
		public Table_referencesContext table_references() {
			return getRuleContext(Table_referencesContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(SQLParser.RPAREN, 0); }
		public TerminalNode OJ() { return getToken(SQLParser.OJ, 0); }
		public List<Table_referenceContext> table_reference() {
			return getRuleContexts(Table_referenceContext.class);
		}
		public Table_referenceContext table_reference(int i) {
			return getRuleContext(Table_referenceContext.class,i);
		}
		public TerminalNode LEFT() { return getToken(SQLParser.LEFT, 0); }
		public TerminalNode OUTER() { return getToken(SQLParser.OUTER, 0); }
		public TerminalNode JOIN() { return getToken(SQLParser.JOIN, 0); }
		public TerminalNode ON() { return getToken(SQLParser.ON, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public Table_atomContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_table_atom; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).enterTable_atom(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).exitTable_atom(this);
		}
	}

	public final Table_atomContext table_atom() throws RecognitionException {
		Table_atomContext _localctx = new Table_atomContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_table_atom);
		int _la;
		try {
			setState(313);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,36,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(287);
				table_name();
				setState(289);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PARTITION) {
					{
					setState(288);
					partition_clause();
					}
				}

				setState(292);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ID) {
					{
					setState(291);
					table_alias();
					}
				}

				setState(295);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==USE || _la==IGNORE) {
					{
					setState(294);
					index_hint_list();
					}
				}

				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				{
				setState(297);
				subquery();
				setState(298);
				subquery_alias();
				}
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				{
				setState(300);
				match(LPAREN);
				setState(301);
				table_references();
				setState(302);
				match(RPAREN);
				}
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				{
				setState(304);
				match(OJ);
				setState(305);
				table_reference();
				setState(306);
				match(LEFT);
				setState(307);
				match(OUTER);
				setState(308);
				match(JOIN);
				setState(309);
				table_reference();
				setState(310);
				match(ON);
				setState(311);
				expression();
				}
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Join_clauseContext extends ParserRuleContext {
		public TerminalNode JOIN() { return getToken(SQLParser.JOIN, 0); }
		public Table_atomContext table_atom() {
			return getRuleContext(Table_atomContext.class,0);
		}
		public Join_conditionContext join_condition() {
			return getRuleContext(Join_conditionContext.class,0);
		}
		public TerminalNode INNER() { return getToken(SQLParser.INNER, 0); }
		public TerminalNode CROSS() { return getToken(SQLParser.CROSS, 0); }
		public TerminalNode STRAIGHT_JOIN() { return getToken(SQLParser.STRAIGHT_JOIN, 0); }
		public TerminalNode ON() { return getToken(SQLParser.ON, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public Table_factor4Context table_factor4() {
			return getRuleContext(Table_factor4Context.class,0);
		}
		public TerminalNode LEFT() { return getToken(SQLParser.LEFT, 0); }
		public TerminalNode RIGHT() { return getToken(SQLParser.RIGHT, 0); }
		public TerminalNode OUTER() { return getToken(SQLParser.OUTER, 0); }
		public TerminalNode NATURAL() { return getToken(SQLParser.NATURAL, 0); }
		public Join_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_join_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).enterJoin_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).exitJoin_clause(this);
		}
	}

	public final Join_clauseContext join_clause() throws RecognitionException {
		Join_clauseContext _localctx = new Join_clauseContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_join_clause);
		int _la;
		try {
			setState(346);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case INNER:
			case JOIN:
			case CROSS:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(316);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==INNER || _la==CROSS) {
					{
					setState(315);
					_la = _input.LA(1);
					if ( !(_la==INNER || _la==CROSS) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					}
				}

				setState(318);
				match(JOIN);
				setState(319);
				table_atom();
				setState(321);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==USING || _la==ON) {
					{
					setState(320);
					join_condition();
					}
				}

				}
				}
				break;
			case STRAIGHT_JOIN:
				enterOuterAlt(_localctx, 2);
				{
				{
				setState(323);
				match(STRAIGHT_JOIN);
				setState(324);
				table_atom();
				setState(327);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ON) {
					{
					setState(325);
					match(ON);
					setState(326);
					expression();
					}
				}

				}
				}
				break;
			case LEFT:
			case RIGHT:
				enterOuterAlt(_localctx, 3);
				{
				{
				setState(329);
				_la = _input.LA(1);
				if ( !(_la==LEFT || _la==RIGHT) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(331);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OUTER) {
					{
					setState(330);
					match(OUTER);
					}
				}

				setState(333);
				match(JOIN);
				setState(334);
				table_factor4();
				setState(335);
				join_condition();
				}
				}
				break;
			case NATURAL:
				enterOuterAlt(_localctx, 4);
				{
				{
				setState(337);
				match(NATURAL);
				setState(342);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LEFT || _la==RIGHT) {
					{
					setState(338);
					_la = _input.LA(1);
					if ( !(_la==LEFT || _la==RIGHT) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(340);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==OUTER) {
						{
						setState(339);
						match(OUTER);
						}
					}

					}
				}

				setState(344);
				match(JOIN);
				setState(345);
				table_atom();
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Join_conditionContext extends ParserRuleContext {
		public TerminalNode ON() { return getToken(SQLParser.ON, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<Expr_opContext> expr_op() {
			return getRuleContexts(Expr_opContext.class);
		}
		public Expr_opContext expr_op(int i) {
			return getRuleContext(Expr_opContext.class,i);
		}
		public TerminalNode USING() { return getToken(SQLParser.USING, 0); }
		public Column_listContext column_list() {
			return getRuleContext(Column_listContext.class,0);
		}
		public Join_conditionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_join_condition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).enterJoin_condition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).exitJoin_condition(this);
		}
	}

	public final Join_conditionContext join_condition() throws RecognitionException {
		Join_conditionContext _localctx = new Join_conditionContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_join_condition);
		int _la;
		try {
			setState(360);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ON:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(348);
				match(ON);
				setState(349);
				expression();
				setState(355);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << AND) | (1L << OR) | (1L << XOR) | (1L << NOT))) != 0)) {
					{
					{
					setState(350);
					expr_op();
					setState(351);
					expression();
					}
					}
					setState(357);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				}
				break;
			case USING:
				enterOuterAlt(_localctx, 2);
				{
				{
				setState(358);
				match(USING);
				setState(359);
				column_list();
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Index_hint_listContext extends ParserRuleContext {
		public List<Index_hintContext> index_hint() {
			return getRuleContexts(Index_hintContext.class);
		}
		public Index_hintContext index_hint(int i) {
			return getRuleContext(Index_hintContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(SQLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SQLParser.COMMA, i);
		}
		public Index_hint_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_index_hint_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).enterIndex_hint_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).exitIndex_hint_list(this);
		}
	}

	public final Index_hint_listContext index_hint_list() throws RecognitionException {
		Index_hint_listContext _localctx = new Index_hint_listContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_index_hint_list);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(362);
			index_hint();
			setState(367);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,46,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(363);
					match(COMMA);
					setState(364);
					index_hint();
					}
					} 
				}
				setState(369);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,46,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Index_optionsContext extends ParserRuleContext {
		public TerminalNode INDEX() { return getToken(SQLParser.INDEX, 0); }
		public TerminalNode KEY() { return getToken(SQLParser.KEY, 0); }
		public TerminalNode FOR() { return getToken(SQLParser.FOR, 0); }
		public TerminalNode JOIN() { return getToken(SQLParser.JOIN, 0); }
		public TerminalNode ORDER() { return getToken(SQLParser.ORDER, 0); }
		public TerminalNode BY() { return getToken(SQLParser.BY, 0); }
		public TerminalNode GROUP() { return getToken(SQLParser.GROUP, 0); }
		public Index_optionsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_index_options; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).enterIndex_options(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).exitIndex_options(this);
		}
	}

	public final Index_optionsContext index_options() throws RecognitionException {
		Index_optionsContext _localctx = new Index_optionsContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_index_options);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(370);
			_la = _input.LA(1);
			if ( !(_la==INDEX || _la==KEY) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(379);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==FOR) {
				{
				setState(371);
				match(FOR);
				setState(377);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case JOIN:
					{
					{
					setState(372);
					match(JOIN);
					}
					}
					break;
				case ORDER:
					{
					{
					setState(373);
					match(ORDER);
					setState(374);
					match(BY);
					}
					}
					break;
				case GROUP:
					{
					{
					setState(375);
					match(GROUP);
					setState(376);
					match(BY);
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Index_hintContext extends ParserRuleContext {
		public TerminalNode USE() { return getToken(SQLParser.USE, 0); }
		public Index_optionsContext index_options() {
			return getRuleContext(Index_optionsContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(SQLParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(SQLParser.RPAREN, 0); }
		public Index_listContext index_list() {
			return getRuleContext(Index_listContext.class,0);
		}
		public TerminalNode IGNORE() { return getToken(SQLParser.IGNORE, 0); }
		public Index_hintContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_index_hint; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).enterIndex_hint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).exitIndex_hint(this);
		}
	}

	public final Index_hintContext index_hint() throws RecognitionException {
		Index_hintContext _localctx = new Index_hintContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_index_hint);
		int _la;
		try {
			setState(395);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case USE:
				enterOuterAlt(_localctx, 1);
				{
				setState(381);
				match(USE);
				setState(382);
				index_options();
				setState(383);
				match(LPAREN);
				setState(385);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ID) {
					{
					setState(384);
					index_list();
					}
				}

				setState(387);
				match(RPAREN);
				}
				break;
			case IGNORE:
				enterOuterAlt(_localctx, 2);
				{
				setState(389);
				match(IGNORE);
				setState(390);
				index_options();
				setState(391);
				match(LPAREN);
				setState(392);
				index_list();
				setState(393);
				match(RPAREN);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Index_listContext extends ParserRuleContext {
		public List<Index_nameContext> index_name() {
			return getRuleContexts(Index_nameContext.class);
		}
		public Index_nameContext index_name(int i) {
			return getRuleContext(Index_nameContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(SQLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SQLParser.COMMA, i);
		}
		public Index_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_index_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).enterIndex_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).exitIndex_list(this);
		}
	}

	public final Index_listContext index_list() throws RecognitionException {
		Index_listContext _localctx = new Index_listContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_index_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(397);
			index_name();
			setState(402);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(398);
				match(COMMA);
				setState(399);
				index_name();
				}
				}
				setState(404);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Partition_clauseContext extends ParserRuleContext {
		public TerminalNode PARTITION() { return getToken(SQLParser.PARTITION, 0); }
		public TerminalNode LPAREN() { return getToken(SQLParser.LPAREN, 0); }
		public Partition_namesContext partition_names() {
			return getRuleContext(Partition_namesContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(SQLParser.RPAREN, 0); }
		public Partition_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_partition_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).enterPartition_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).exitPartition_clause(this);
		}
	}

	public final Partition_clauseContext partition_clause() throws RecognitionException {
		Partition_clauseContext _localctx = new Partition_clauseContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_partition_clause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(405);
			match(PARTITION);
			setState(406);
			match(LPAREN);
			setState(407);
			partition_names();
			setState(408);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Partition_namesContext extends ParserRuleContext {
		public List<Partition_nameContext> partition_name() {
			return getRuleContexts(Partition_nameContext.class);
		}
		public Partition_nameContext partition_name(int i) {
			return getRuleContext(Partition_nameContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(SQLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SQLParser.COMMA, i);
		}
		public Partition_namesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_partition_names; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).enterPartition_names(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).exitPartition_names(this);
		}
	}

	public final Partition_namesContext partition_names() throws RecognitionException {
		Partition_namesContext _localctx = new Partition_namesContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_partition_names);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(410);
			partition_name();
			setState(415);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(411);
				match(COMMA);
				setState(412);
				partition_name();
				}
				}
				setState(417);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Partition_nameContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(SQLParser.ID, 0); }
		public Partition_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_partition_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).enterPartition_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).exitPartition_name(this);
		}
	}

	public final Partition_nameContext partition_name() throws RecognitionException {
		Partition_nameContext _localctx = new Partition_nameContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_partition_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(418);
			match(ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Subquery_aliasContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(SQLParser.ID, 0); }
		public Subquery_aliasContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_subquery_alias; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).enterSubquery_alias(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).exitSubquery_alias(this);
		}
	}

	public final Subquery_aliasContext subquery_alias() throws RecognitionException {
		Subquery_aliasContext _localctx = new Subquery_aliasContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_subquery_alias);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(420);
			match(ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SubqueryContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(SQLParser.LPAREN, 0); }
		public Select_clauseContext select_clause() {
			return getRuleContext(Select_clauseContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(SQLParser.RPAREN, 0); }
		public SubqueryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_subquery; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).enterSubquery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLParserListener ) ((SQLParserListener)listener).exitSubquery(this);
		}
	}

	public final SubqueryContext subquery() throws RecognitionException {
		SubqueryContext _localctx = new SubqueryContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_subquery);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(422);
			match(LPAREN);
			setState(423);
			select_clause();
			setState(424);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3K\u01ad\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\3\2\6\2"+
		"V\n\2\r\2\16\2W\3\3\3\3\3\4\3\4\3\4\3\4\3\4\5\4a\n\4\3\5\3\5\3\6\3\6\3"+
		"\7\3\7\3\7\5\7j\n\7\3\7\3\7\5\7n\n\7\3\7\3\7\5\7r\n\7\3\7\3\7\3\7\5\7"+
		"w\n\7\3\7\3\7\3\7\5\7|\n\7\3\7\5\7\177\n\7\3\b\3\b\3\b\5\b\u0084\n\b\3"+
		"\b\3\b\3\b\3\b\5\b\u008a\n\b\3\b\3\b\3\b\5\b\u008f\n\b\3\b\3\b\5\b\u0093"+
		"\n\b\3\b\3\b\5\b\u0097\n\b\3\b\5\b\u009a\n\b\3\t\3\t\3\n\3\n\3\13\3\13"+
		"\3\13\3\13\7\13\u00a4\n\13\f\13\16\13\u00a7\13\13\3\13\3\13\3\f\3\f\3"+
		"\f\7\f\u00ae\n\f\f\f\16\f\u00b1\13\f\3\r\3\r\3\r\3\16\3\16\3\16\3\16\7"+
		"\16\u00ba\n\16\f\16\16\16\u00bd\13\16\3\17\3\17\3\17\3\17\3\17\3\17\5"+
		"\17\u00c5\n\17\3\20\3\20\3\21\3\21\3\22\3\22\3\23\3\23\3\24\3\24\3\25"+
		"\3\25\3\26\3\26\3\27\3\27\3\27\5\27\u00d8\n\27\3\30\3\30\3\30\3\30\3\30"+
		"\3\30\3\30\3\30\3\30\3\30\3\30\3\30\3\30\3\30\5\30\u00e8\n\30\3\31\3\31"+
		"\3\31\3\31\7\31\u00ee\n\31\f\31\16\31\u00f1\13\31\3\32\3\32\5\32\u00f5"+
		"\n\32\3\33\3\33\5\33\u00f9\n\33\3\33\3\33\3\33\5\33\u00fe\n\33\5\33\u0100"+
		"\n\33\3\34\3\34\3\34\3\34\3\34\5\34\u0107\n\34\5\34\u0109\n\34\3\35\3"+
		"\35\3\35\5\35\u010e\n\35\3\35\3\35\3\35\3\35\5\35\u0114\n\35\3\36\3\36"+
		"\3\36\3\36\5\36\u011a\n\36\5\36\u011c\n\36\3\36\3\36\5\36\u0120\n\36\3"+
		"\37\3\37\5\37\u0124\n\37\3\37\5\37\u0127\n\37\3\37\5\37\u012a\n\37\3\37"+
		"\3\37\3\37\3\37\3\37\3\37\3\37\3\37\3\37\3\37\3\37\3\37\3\37\3\37\3\37"+
		"\3\37\5\37\u013c\n\37\3 \5 \u013f\n \3 \3 \3 \5 \u0144\n \3 \3 \3 \3 "+
		"\5 \u014a\n \3 \3 \5 \u014e\n \3 \3 \3 \3 \3 \3 \3 \5 \u0157\n \5 \u0159"+
		"\n \3 \3 \5 \u015d\n \3!\3!\3!\3!\3!\7!\u0164\n!\f!\16!\u0167\13!\3!\3"+
		"!\5!\u016b\n!\3\"\3\"\3\"\7\"\u0170\n\"\f\"\16\"\u0173\13\"\3#\3#\3#\3"+
		"#\3#\3#\3#\5#\u017c\n#\5#\u017e\n#\3$\3$\3$\3$\5$\u0184\n$\3$\3$\3$\3"+
		"$\3$\3$\3$\3$\5$\u018e\n$\3%\3%\3%\7%\u0193\n%\f%\16%\u0196\13%\3&\3&"+
		"\3&\3&\3&\3\'\3\'\3\'\7\'\u01a0\n\'\f\'\16\'\u01a3\13\'\3(\3(\3)\3)\3"+
		"*\3*\3*\3*\3*\2\2+\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \"$&(*,.\60"+
		"\62\64\668:<>@BDFHJLNPR\2\b\4\2\n\nGH\4\2\'*,-\4\2\6\b++\4\2\62\62\65"+
		"\65\3\2BC\3\2\678\2\u01c4\2U\3\2\2\2\4Y\3\2\2\2\6[\3\2\2\2\bb\3\2\2\2"+
		"\nd\3\2\2\2\f~\3\2\2\2\16\u0099\3\2\2\2\20\u009b\3\2\2\2\22\u009d\3\2"+
		"\2\2\24\u009f\3\2\2\2\26\u00aa\3\2\2\2\30\u00b2\3\2\2\2\32\u00b5\3\2\2"+
		"\2\34\u00c4\3\2\2\2\36\u00c6\3\2\2\2 \u00c8\3\2\2\2\"\u00ca\3\2\2\2$\u00cc"+
		"\3\2\2\2&\u00ce\3\2\2\2(\u00d0\3\2\2\2*\u00d2\3\2\2\2,\u00d7\3\2\2\2."+
		"\u00e7\3\2\2\2\60\u00e9\3\2\2\2\62\u00f4\3\2\2\2\64\u00f6\3\2\2\2\66\u0101"+
		"\3\2\2\28\u010a\3\2\2\2:\u0115\3\2\2\2<\u013b\3\2\2\2>\u015c\3\2\2\2@"+
		"\u016a\3\2\2\2B\u016c\3\2\2\2D\u0174\3\2\2\2F\u018d\3\2\2\2H\u018f\3\2"+
		"\2\2J\u0197\3\2\2\2L\u019c\3\2\2\2N\u01a4\3\2\2\2P\u01a6\3\2\2\2R\u01a8"+
		"\3\2\2\2TV\5\6\4\2UT\3\2\2\2VW\3\2\2\2WU\3\2\2\2WX\3\2\2\2X\3\3\2\2\2"+
		"YZ\7F\2\2Z\5\3\2\2\2[\\\7\3\2\2\\]\5\26\f\2]^\7\4\2\2^`\5\60\31\2_a\5"+
		"\30\r\2`_\3\2\2\2`a\3\2\2\2a\7\3\2\2\2bc\7F\2\2c\t\3\2\2\2de\7F\2\2e\13"+
		"\3\2\2\2fg\5\4\3\2gh\7\60\2\2hj\3\2\2\2if\3\2\2\2ij\3\2\2\2jk\3\2\2\2"+
		"kl\7F\2\2ln\7\60\2\2mi\3\2\2\2mn\3\2\2\2no\3\2\2\2oq\7F\2\2pr\5\20\t\2"+
		"qp\3\2\2\2qr\3\2\2\2r\177\3\2\2\2st\5\n\6\2tu\7\60\2\2uw\3\2\2\2vs\3\2"+
		"\2\2vw\3\2\2\2wx\3\2\2\2x\177\7F\2\2y{\7K\2\2z|\5\20\t\2{z\3\2\2\2{|\3"+
		"\2\2\2|\177\3\2\2\2}\177\7 \2\2~m\3\2\2\2~v\3\2\2\2~y\3\2\2\2~}\3\2\2"+
		"\2\177\r\3\2\2\2\u0080\u0081\5\n\6\2\u0081\u0082\7\60\2\2\u0082\u0084"+
		"\3\2\2\2\u0083\u0080\3\2\2\2\u0083\u0084\3\2\2\2\u0084\u0085\3\2\2\2\u0085"+
		"\u009a\5\f\7\2\u0086\u0087\5\4\3\2\u0087\u0088\7\60\2\2\u0088\u008a\3"+
		"\2\2\2\u0089\u0086\3\2\2\2\u0089\u008a\3\2\2\2\u008a\u008b\3\2\2\2\u008b"+
		"\u008c\5\n\6\2\u008c\u008d\7\60\2\2\u008d\u008f\3\2\2\2\u008e\u0089\3"+
		"\2\2\2\u008e\u008f\3\2\2\2\u008f\u0090\3\2\2\2\u0090\u0092\5\f\7\2\u0091"+
		"\u0093\5\20\t\2\u0092\u0091\3\2\2\2\u0092\u0093\3\2\2\2\u0093\u009a\3"+
		"\2\2\2\u0094\u0096\7K\2\2\u0095\u0097\5\20\t\2\u0096\u0095\3\2\2\2\u0096"+
		"\u0097\3\2\2\2\u0097\u009a\3\2\2\2\u0098\u009a\7 \2\2\u0099\u0083\3\2"+
		"\2\2\u0099\u008e\3\2\2\2\u0099\u0094\3\2\2\2\u0099\u0098\3\2\2\2\u009a"+
		"\17\3\2\2\2\u009b\u009c\7F\2\2\u009c\21\3\2\2\2\u009d\u009e\7F\2\2\u009e"+
		"\23\3\2\2\2\u009f\u00a0\7\"\2\2\u00a0\u00a5\5\f\7\2\u00a1\u00a2\7/\2\2"+
		"\u00a2\u00a4\5\f\7\2\u00a3\u00a1\3\2\2\2\u00a4\u00a7\3\2\2\2\u00a5\u00a3"+
		"\3\2\2\2\u00a5\u00a6\3\2\2\2\u00a6\u00a8\3\2\2\2\u00a7\u00a5\3\2\2\2\u00a8"+
		"\u00a9\7!\2\2\u00a9\25\3\2\2\2\u00aa\u00af\5\f\7\2\u00ab\u00ac\7/\2\2"+
		"\u00ac\u00ae\5\f\7\2\u00ad\u00ab\3\2\2\2\u00ae\u00b1\3\2\2\2\u00af\u00ad"+
		"\3\2\2\2\u00af\u00b0\3\2\2\2\u00b0\27\3\2\2\2\u00b1\u00af\3\2\2\2\u00b2"+
		"\u00b3\7\5\2\2\u00b3\u00b4\5\32\16\2\u00b4\31\3\2\2\2\u00b5\u00bb\5.\30"+
		"\2\u00b6\u00b7\5(\25\2\u00b7\u00b8\5.\30\2\u00b8\u00ba\3\2\2\2\u00b9\u00b6"+
		"\3\2\2\2\u00ba\u00bd\3\2\2\2\u00bb\u00b9\3\2\2\2\u00bb\u00bc\3\2\2\2\u00bc"+
		"\33\3\2\2\2\u00bd\u00bb\3\2\2\2\u00be\u00c5\5\36\20\2\u00bf\u00c5\5\16"+
		"\b\2\u00c0\u00c5\7K\2\2\u00c1\u00c2\7\31\2\2\u00c2\u00c3\7F\2\2\u00c3"+
		"\u00c5\7\31\2\2\u00c4\u00be\3\2\2\2\u00c4\u00bf\3\2\2\2\u00c4\u00c0\3"+
		"\2\2\2\u00c4\u00c1\3\2\2\2\u00c5\35\3\2\2\2\u00c6\u00c7\t\2\2\2\u00c7"+
		"\37\3\2\2\2\u00c8\u00c9\5\34\17\2\u00c9!\3\2\2\2\u00ca\u00cb\5\34\17\2"+
		"\u00cb#\3\2\2\2\u00cc\u00cd\5\34\17\2\u00cd%\3\2\2\2\u00ce\u00cf\t\3\2"+
		"\2\u00cf\'\3\2\2\2\u00d0\u00d1\t\4\2\2\u00d1)\3\2\2\2\u00d2\u00d3\7\24"+
		"\2\2\u00d3+\3\2\2\2\u00d4\u00d8\7\t\2\2\u00d5\u00d6\7\t\2\2\u00d6\u00d8"+
		"\7+\2\2\u00d7\u00d4\3\2\2\2\u00d7\u00d5\3\2\2\2\u00d8-\3\2\2\2\u00d9\u00da"+
		"\5\"\22\2\u00da\u00db\5&\24\2\u00db\u00dc\5 \21\2\u00dc\u00e8\3\2\2\2"+
		"\u00dd\u00de\5$\23\2\u00de\u00df\5*\26\2\u00df\u00e0\5\"\22\2\u00e0\u00e1"+
		"\7\6\2\2\u00e1\u00e2\5 \21\2\u00e2\u00e8\3\2\2\2\u00e3\u00e4\5$\23\2\u00e4"+
		"\u00e5\5,\27\2\u00e5\u00e6\7\n\2\2\u00e6\u00e8\3\2\2\2\u00e7\u00d9\3\2"+
		"\2\2\u00e7\u00dd\3\2\2\2\u00e7\u00e3\3\2\2\2\u00e8/\3\2\2\2\u00e9\u00ef"+
		"\5\62\32\2\u00ea\u00eb\7/\2\2\u00eb\u00ee\5\62\32\2\u00ec\u00ee\5> \2"+
		"\u00ed\u00ea\3\2\2\2\u00ed\u00ec\3\2\2\2\u00ee\u00f1\3\2\2\2\u00ef\u00ed"+
		"\3\2\2\2\u00ef\u00f0\3\2\2\2\u00f0\61\3\2\2\2\u00f1\u00ef\3\2\2\2\u00f2"+
		"\u00f5\5\64\33\2\u00f3\u00f5\5<\37\2\u00f4\u00f2\3\2\2\2\u00f4\u00f3\3"+
		"\2\2\2\u00f5\63\3\2\2\2\u00f6\u00ff\5\66\34\2\u00f7\u00f9\t\5\2\2\u00f8"+
		"\u00f7\3\2\2\2\u00f8\u00f9\3\2\2\2\u00f9\u00fa\3\2\2\2\u00fa\u00fb\7\64"+
		"\2\2\u00fb\u00fd\5<\37\2\u00fc\u00fe\5@!\2\u00fd\u00fc\3\2\2\2\u00fd\u00fe"+
		"\3\2\2\2\u00fe\u0100\3\2\2\2\u00ff\u00f8\3\2\2\2\u00ff\u0100\3\2\2\2\u0100"+
		"\65\3\2\2\2\u0101\u0108\58\35\2\u0102\u0103\7@\2\2\u0103\u0106\5<\37\2"+
		"\u0104\u0105\7E\2\2\u0105\u0107\5\32\16\2\u0106\u0104\3\2\2\2\u0106\u0107"+
		"\3\2\2\2\u0107\u0109\3\2\2\2\u0108\u0102\3\2\2\2\u0108\u0109\3\2\2\2\u0109"+
		"\67\3\2\2\2\u010a\u0113\5:\36\2\u010b\u010d\t\6\2\2\u010c\u010e\7\63\2"+
		"\2\u010d\u010c\3\2\2\2\u010d\u010e\3\2\2\2\u010e\u010f\3\2\2\2\u010f\u0110"+
		"\7\64\2\2\u0110\u0111\5:\36\2\u0111\u0112\5@!\2\u0112\u0114\3\2\2\2\u0113"+
		"\u010b\3\2\2\2\u0113\u0114\3\2\2\2\u01149\3\2\2\2\u0115\u011f\5<\37\2"+
		"\u0116\u011b\7A\2\2\u0117\u0119\t\6\2\2\u0118\u011a\7\63\2\2\u0119\u0118"+
		"\3\2\2\2\u0119\u011a\3\2\2\2\u011a\u011c\3\2\2\2\u011b\u0117\3\2\2\2\u011b"+
		"\u011c\3\2\2\2\u011c\u011d\3\2\2\2\u011d\u011e\7\64\2\2\u011e\u0120\5"+
		"<\37\2\u011f\u0116\3\2\2\2\u011f\u0120\3\2\2\2\u0120;\3\2\2\2\u0121\u0123"+
		"\5\b\5\2\u0122\u0124\5J&\2\u0123\u0122\3\2\2\2\u0123\u0124\3\2\2\2\u0124"+
		"\u0126\3\2\2\2\u0125\u0127\5\n\6\2\u0126\u0125\3\2\2\2\u0126\u0127\3\2"+
		"\2\2\u0127\u0129\3\2\2\2\u0128\u012a\5B\"\2\u0129\u0128\3\2\2\2\u0129"+
		"\u012a\3\2\2\2\u012a\u013c\3\2\2\2\u012b\u012c\5R*\2\u012c\u012d\5P)\2"+
		"\u012d\u013c\3\2\2\2\u012e\u012f\7\"\2\2\u012f\u0130\5\60\31\2\u0130\u0131"+
		"\7!\2\2\u0131\u013c\3\2\2\2\u0132\u0133\7D\2\2\u0133\u0134\5\62\32\2\u0134"+
		"\u0135\7B\2\2\u0135\u0136\7\63\2\2\u0136\u0137\7\64\2\2\u0137\u0138\5"+
		"\62\32\2\u0138\u0139\7E\2\2\u0139\u013a\5\32\16\2\u013a\u013c\3\2\2\2"+
		"\u013b\u0121\3\2\2\2\u013b\u012b\3\2\2\2\u013b\u012e\3\2\2\2\u013b\u0132"+
		"\3\2\2\2\u013c=\3\2\2\2\u013d\u013f\t\5\2\2\u013e\u013d\3\2\2\2\u013e"+
		"\u013f\3\2\2\2\u013f\u0140\3\2\2\2\u0140\u0141\7\64\2\2\u0141\u0143\5"+
		"<\37\2\u0142\u0144\5@!\2\u0143\u0142\3\2\2\2\u0143\u0144\3\2\2\2\u0144"+
		"\u015d\3\2\2\2\u0145\u0146\7@\2\2\u0146\u0149\5<\37\2\u0147\u0148\7E\2"+
		"\2\u0148\u014a\5\32\16\2\u0149\u0147\3\2\2\2\u0149\u014a\3\2\2\2\u014a"+
		"\u015d\3\2\2\2\u014b\u014d\t\6\2\2\u014c\u014e\7\63\2\2\u014d\u014c\3"+
		"\2\2\2\u014d\u014e\3\2\2\2\u014e\u014f\3\2\2\2\u014f\u0150\7\64\2\2\u0150"+
		"\u0151\5:\36\2\u0151\u0152\5@!\2\u0152\u015d\3\2\2\2\u0153\u0158\7A\2"+
		"\2\u0154\u0156\t\6\2\2\u0155\u0157\7\63\2\2\u0156\u0155\3\2\2\2\u0156"+
		"\u0157\3\2\2\2\u0157\u0159\3\2\2\2\u0158\u0154\3\2\2\2\u0158\u0159\3\2"+
		"\2\2\u0159\u015a\3\2\2\2\u015a\u015b\7\64\2\2\u015b\u015d\5<\37\2\u015c"+
		"\u013e\3\2\2\2\u015c\u0145\3\2\2\2\u015c\u014b\3\2\2\2\u015c\u0153\3\2"+
		"\2\2\u015d?\3\2\2\2\u015e\u015f\7E\2\2\u015f\u0165\5\32\16\2\u0160\u0161"+
		"\5(\25\2\u0161\u0162\5\32\16\2\u0162\u0164\3\2\2\2\u0163\u0160\3\2\2\2"+
		"\u0164\u0167\3\2\2\2\u0165\u0163\3\2\2\2\u0165\u0166\3\2\2\2\u0166\u016b"+
		"\3\2\2\2\u0167\u0165\3\2\2\2\u0168\u0169\7\66\2\2\u0169\u016b\5\24\13"+
		"\2\u016a\u015e\3\2\2\2\u016a\u0168\3\2\2\2\u016bA\3\2\2\2\u016c\u0171"+
		"\5F$\2\u016d\u016e\7/\2\2\u016e\u0170\5F$\2\u016f\u016d\3\2\2\2\u0170"+
		"\u0173\3\2\2\2\u0171\u016f\3\2\2\2\u0171\u0172\3\2\2\2\u0172C\3\2\2\2"+
		"\u0173\u0171\3\2\2\2\u0174\u017d\t\7\2\2\u0175\u017b\7<\2\2\u0176\u017c"+
		"\7\64\2\2\u0177\u0178\79\2\2\u0178\u017c\7;\2\2\u0179\u017a\7:\2\2\u017a"+
		"\u017c\7;\2\2\u017b\u0176\3\2\2\2\u017b\u0177\3\2\2\2\u017b\u0179\3\2"+
		"\2\2\u017c\u017e\3\2\2\2\u017d\u0175\3\2\2\2\u017d\u017e\3\2\2\2\u017e"+
		"E\3\2\2\2\u017f\u0180\7=\2\2\u0180\u0181\5D#\2\u0181\u0183\7\"\2\2\u0182"+
		"\u0184\5H%\2\u0183\u0182\3\2\2\2\u0183\u0184\3\2\2\2\u0184\u0185\3\2\2"+
		"\2\u0185\u0186\7!\2\2\u0186\u018e\3\2\2\2\u0187\u0188\7>\2\2\u0188\u0189"+
		"\5D#\2\u0189\u018a\7\"\2\2\u018a\u018b\5H%\2\u018b\u018c\7!\2\2\u018c"+
		"\u018e\3\2\2\2\u018d\u017f\3\2\2\2\u018d\u0187\3\2\2\2\u018eG\3\2\2\2"+
		"\u018f\u0194\5\22\n\2\u0190\u0191\7/\2\2\u0191\u0193\5\22\n\2\u0192\u0190"+
		"\3\2\2\2\u0193\u0196\3\2\2\2\u0194\u0192\3\2\2\2\u0194\u0195\3\2\2\2\u0195"+
		"I\3\2\2\2\u0196\u0194\3\2\2\2\u0197\u0198\7?\2\2\u0198\u0199\7\"\2\2\u0199"+
		"\u019a\5L\'\2\u019a\u019b\7!\2\2\u019bK\3\2\2\2\u019c\u01a1\5N(\2\u019d"+
		"\u019e\7/\2\2\u019e\u01a0\5N(\2\u019f\u019d\3\2\2\2\u01a0\u01a3\3\2\2"+
		"\2\u01a1\u019f\3\2\2\2\u01a1\u01a2\3\2\2\2\u01a2M\3\2\2\2\u01a3\u01a1"+
		"\3\2\2\2\u01a4\u01a5\7F\2\2\u01a5O\3\2\2\2\u01a6\u01a7\7F\2\2\u01a7Q\3"+
		"\2\2\2\u01a8\u01a9\7\"\2\2\u01a9\u01aa\5\6\4\2\u01aa\u01ab\7!\2\2\u01ab"+
		"S\3\2\2\2\67W`imqv{~\u0083\u0089\u008e\u0092\u0096\u0099\u00a5\u00af\u00bb"+
		"\u00c4\u00d7\u00e7\u00ed\u00ef\u00f4\u00f8\u00fd\u00ff\u0106\u0108\u010d"+
		"\u0113\u0119\u011b\u011f\u0123\u0126\u0129\u013b\u013e\u0143\u0149\u014d"+
		"\u0156\u0158\u015c\u0165\u016a\u0171\u017b\u017d\u0183\u018d\u0194\u01a1";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}