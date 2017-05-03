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

// Generated from /Users/mhelmich/playground/projects/dist-bc/dist-bc-service/src/main/resources/SQLLexer.g4 by ANTLR 4.6

package org.distbc.parser.gen;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.RuntimeMetaData;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.VocabularyImpl;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.LexerATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class SQLLexer extends Lexer {
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
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"SELECT", "FROM", "WHERE", "AND", "OR", "XOR", "IS", "NULL", "LIKE", "IN", 
		"EXISTS", "ALL", "ANY", "TRUE", "FALSE", "DIVIDE", "MOD", "BETWEEN", "REGEXP", 
		"PLUS", "MINUS", "NEGATION", "VERTBAR", "BITAND", "POWER_OP", "BINARY", 
		"SHIFT_LEFT", "SHIFT_RIGHT", "ESCAPE", "ASTERISK", "RPAREN", "LPAREN", 
		"RBRACK", "LBRACK", "COLON", "ALL_FIELDS", "EQ", "LTH", "GTH", "NOT_EQ", 
		"NOT", "LET", "GET", "SEMI", "COMMA", "DOT", "COLLATE", "INNER", "OUTER", 
		"JOIN", "CROSS", "USING", "INDEX", "KEY", "ORDER", "GROUP", "BY", "FOR", 
		"USE", "IGNORE", "PARTITION", "STRAIGHT_JOIN", "NATURAL", "LEFT", "RIGHT", 
		"OJ", "ON", "ID", "QUOTED_LITERAL", "INT", "NEWLINE", "WS", "USER_VAR", 
		"USER_VAR_SUBFIX1", "USER_VAR_SUBFIX2", "USER_VAR_SUBFIX3", "USER_VAR_SUBFIX4"
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


	public SQLLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "SQLLexer.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2K\u0240\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64\t"+
		"\64\4\65\t\65\4\66\t\66\4\67\t\67\48\t8\49\t9\4:\t:\4;\t;\4<\t<\4=\t="+
		"\4>\t>\4?\t?\4@\t@\4A\tA\4B\tB\4C\tC\4D\tD\4E\tE\4F\tF\4G\tG\4H\tH\4I"+
		"\tI\4J\tJ\4K\tK\4L\tL\4M\tM\4N\tN\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2"+
		"\3\2\3\2\3\2\5\2\u00aa\n\2\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\5\3\u00b4\n"+
		"\3\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\5\4\u00c0\n\4\3\5\3\5\3\5\3"+
		"\5\3\5\3\5\3\5\3\5\5\5\u00ca\n\5\3\6\3\6\3\6\3\6\3\6\3\6\5\6\u00d2\n\6"+
		"\3\7\3\7\3\7\3\7\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3\t\3\n\3\n\3\n\3\n\3\n\3"+
		"\13\3\13\3\13\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\r\3\r\3\r\3\r\3\16\3\16\3"+
		"\16\3\16\3\17\3\17\3\17\3\17\3\17\3\20\3\20\3\20\3\20\3\20\3\20\3\21\3"+
		"\21\3\21\3\21\5\21\u0106\n\21\3\22\3\22\3\22\3\22\5\22\u010c\n\22\3\23"+
		"\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\24\3\24\3\24\3\24\3\24\3\24\3\24"+
		"\3\25\3\25\3\26\3\26\3\27\3\27\3\30\3\30\3\31\3\31\3\32\3\32\3\33\3\33"+
		"\3\33\3\33\3\33\3\33\3\33\3\34\3\34\3\34\3\35\3\35\3\35\3\36\3\36\3\36"+
		"\3\36\3\36\3\36\3\36\3\37\3\37\3 \3 \3!\3!\3\"\3\"\3#\3#\3$\3$\3%\3%\3"+
		"%\3&\3&\3\'\3\'\3(\3(\3)\3)\3)\3*\3*\3*\3*\3+\3+\3+\3,\3,\3,\3-\3-\3."+
		"\3.\3/\3/\3\60\3\60\3\60\3\60\3\60\3\60\3\60\3\60\3\61\3\61\3\61\3\61"+
		"\3\61\3\61\3\62\3\62\3\62\3\62\3\62\3\62\3\63\3\63\3\63\3\63\3\63\3\64"+
		"\3\64\3\64\3\64\3\64\3\64\3\65\3\65\3\65\3\65\3\65\3\65\3\66\3\66\3\66"+
		"\3\66\3\66\3\66\3\67\3\67\3\67\3\67\38\38\38\38\38\38\39\39\39\39\39\3"+
		"9\3:\3:\3:\3;\3;\3;\3;\3<\3<\3<\3<\3=\3=\3=\3=\3=\3=\3=\3>\3>\3>\3>\3"+
		">\3>\3>\3>\3>\3>\3?\3?\3?\3?\3?\3?\3?\3?\3?\3?\3?\3?\3?\3?\3@\3@\3@\3"+
		"@\3@\3@\3@\3@\3A\3A\3A\3A\3A\3B\3B\3B\3B\3B\3B\3C\3C\3C\3D\3D\3D\3E\3"+
		"E\3E\7E\u01e6\nE\fE\16E\u01e9\13E\3F\3F\3F\3F\3F\3F\3F\3F\7F\u01f3\nF"+
		"\fF\16F\u01f6\13F\3F\3F\3F\3F\3F\3F\3F\3F\3F\7F\u0201\nF\fF\16F\u0204"+
		"\13F\3F\5F\u0207\nF\3G\6G\u020a\nG\rG\16G\u020b\3H\5H\u020f\nH\3H\3H\3"+
		"H\3H\3I\6I\u0216\nI\rI\16I\u0217\3I\3I\3J\3J\3J\3J\3J\5J\u0221\nJ\3K\3"+
		"K\6K\u0225\nK\rK\16K\u0226\3K\3K\3L\3L\6L\u022d\nL\rL\16L\u022e\3L\3L"+
		"\3M\3M\6M\u0235\nM\rM\16M\u0236\3M\3M\3N\3N\6N\u023d\nN\rN\16N\u023e\2"+
		"\2O\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35"+
		"\20\37\21!\22#\23%\24\'\25)\26+\27-\30/\31\61\32\63\33\65\34\67\359\36"+
		";\37= ?!A\"C#E$G%I&K\'M(O)Q*S+U,W-Y.[/]\60_\61a\62c\63e\64g\65i\66k\67"+
		"m8o9q:s;u<w=y>{?}@\177A\u0081B\u0083C\u0085D\u0087E\u0089F\u008bG\u008d"+
		"H\u008fI\u0091J\u0093K\u0095\2\u0097\2\u0099\2\u009b\2\3\2\b\5\2C\\aa"+
		"c|\3\2))\3\2$$\5\2\13\f\17\17\"\"\3\2bb\7\2&&\62;C\\aac|\2\u025a\2\3\3"+
		"\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2"+
		"\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3"+
		"\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2"+
		"%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2\2\61"+
		"\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2\2\67\3\2\2\2\29\3\2\2\2\2;\3\2\2\2"+
		"\2=\3\2\2\2\2?\3\2\2\2\2A\3\2\2\2\2C\3\2\2\2\2E\3\2\2\2\2G\3\2\2\2\2I"+
		"\3\2\2\2\2K\3\2\2\2\2M\3\2\2\2\2O\3\2\2\2\2Q\3\2\2\2\2S\3\2\2\2\2U\3\2"+
		"\2\2\2W\3\2\2\2\2Y\3\2\2\2\2[\3\2\2\2\2]\3\2\2\2\2_\3\2\2\2\2a\3\2\2\2"+
		"\2c\3\2\2\2\2e\3\2\2\2\2g\3\2\2\2\2i\3\2\2\2\2k\3\2\2\2\2m\3\2\2\2\2o"+
		"\3\2\2\2\2q\3\2\2\2\2s\3\2\2\2\2u\3\2\2\2\2w\3\2\2\2\2y\3\2\2\2\2{\3\2"+
		"\2\2\2}\3\2\2\2\2\177\3\2\2\2\2\u0081\3\2\2\2\2\u0083\3\2\2\2\2\u0085"+
		"\3\2\2\2\2\u0087\3\2\2\2\2\u0089\3\2\2\2\2\u008b\3\2\2\2\2\u008d\3\2\2"+
		"\2\2\u008f\3\2\2\2\2\u0091\3\2\2\2\2\u0093\3\2\2\2\3\u00a9\3\2\2\2\5\u00b3"+
		"\3\2\2\2\7\u00bf\3\2\2\2\t\u00c9\3\2\2\2\13\u00d1\3\2\2\2\r\u00d3\3\2"+
		"\2\2\17\u00d7\3\2\2\2\21\u00da\3\2\2\2\23\u00df\3\2\2\2\25\u00e4\3\2\2"+
		"\2\27\u00e7\3\2\2\2\31\u00ee\3\2\2\2\33\u00f2\3\2\2\2\35\u00f6\3\2\2\2"+
		"\37\u00fb\3\2\2\2!\u0105\3\2\2\2#\u010b\3\2\2\2%\u010d\3\2\2\2\'\u0115"+
		"\3\2\2\2)\u011c\3\2\2\2+\u011e\3\2\2\2-\u0120\3\2\2\2/\u0122\3\2\2\2\61"+
		"\u0124\3\2\2\2\63\u0126\3\2\2\2\65\u0128\3\2\2\2\67\u012f\3\2\2\29\u0132"+
		"\3\2\2\2;\u0135\3\2\2\2=\u013c\3\2\2\2?\u013e\3\2\2\2A\u0140\3\2\2\2C"+
		"\u0142\3\2\2\2E\u0144\3\2\2\2G\u0146\3\2\2\2I\u0148\3\2\2\2K\u014b\3\2"+
		"\2\2M\u014d\3\2\2\2O\u014f\3\2\2\2Q\u0151\3\2\2\2S\u0154\3\2\2\2U\u0158"+
		"\3\2\2\2W\u015b\3\2\2\2Y\u015e\3\2\2\2[\u0160\3\2\2\2]\u0162\3\2\2\2_"+
		"\u0164\3\2\2\2a\u016c\3\2\2\2c\u0172\3\2\2\2e\u0178\3\2\2\2g\u017d\3\2"+
		"\2\2i\u0183\3\2\2\2k\u0189\3\2\2\2m\u018f\3\2\2\2o\u0193\3\2\2\2q\u0199"+
		"\3\2\2\2s\u019f\3\2\2\2u\u01a2\3\2\2\2w\u01a6\3\2\2\2y\u01aa\3\2\2\2{"+
		"\u01b1\3\2\2\2}\u01bb\3\2\2\2\177\u01c9\3\2\2\2\u0081\u01d1\3\2\2\2\u0083"+
		"\u01d6\3\2\2\2\u0085\u01dc\3\2\2\2\u0087\u01df\3\2\2\2\u0089\u01e2\3\2"+
		"\2\2\u008b\u0206\3\2\2\2\u008d\u0209\3\2\2\2\u008f\u020e\3\2\2\2\u0091"+
		"\u0215\3\2\2\2\u0093\u021b\3\2\2\2\u0095\u0222\3\2\2\2\u0097\u022a\3\2"+
		"\2\2\u0099\u0232\3\2\2\2\u009b\u023c\3\2\2\2\u009d\u009e\7u\2\2\u009e"+
		"\u009f\7g\2\2\u009f\u00a0\7n\2\2\u00a0\u00a1\7g\2\2\u00a1\u00a2\7e\2\2"+
		"\u00a2\u00aa\7v\2\2\u00a3\u00a4\7U\2\2\u00a4\u00a5\7G\2\2\u00a5\u00a6"+
		"\7N\2\2\u00a6\u00a7\7G\2\2\u00a7\u00a8\7E\2\2\u00a8\u00aa\7V\2\2\u00a9"+
		"\u009d\3\2\2\2\u00a9\u00a3\3\2\2\2\u00aa\4\3\2\2\2\u00ab\u00ac\7h\2\2"+
		"\u00ac\u00ad\7t\2\2\u00ad\u00ae\7q\2\2\u00ae\u00b4\7o\2\2\u00af\u00b0"+
		"\7H\2\2\u00b0\u00b1\7T\2\2\u00b1\u00b2\7Q\2\2\u00b2\u00b4\7O\2\2\u00b3"+
		"\u00ab\3\2\2\2\u00b3\u00af\3\2\2\2\u00b4\6\3\2\2\2\u00b5\u00b6\7y\2\2"+
		"\u00b6\u00b7\7j\2\2\u00b7\u00b8\7g\2\2\u00b8\u00b9\7t\2\2\u00b9\u00c0"+
		"\7g\2\2\u00ba\u00bb\7Y\2\2\u00bb\u00bc\7J\2\2\u00bc\u00bd\7G\2\2\u00bd"+
		"\u00be\7T\2\2\u00be\u00c0\7G\2\2\u00bf\u00b5\3\2\2\2\u00bf\u00ba\3\2\2"+
		"\2\u00c0\b\3\2\2\2\u00c1\u00c2\7c\2\2\u00c2\u00c3\7p\2\2\u00c3\u00ca\7"+
		"f\2\2\u00c4\u00c5\7(\2\2\u00c5\u00ca\7(\2\2\u00c6\u00c7\7C\2\2\u00c7\u00c8"+
		"\7P\2\2\u00c8\u00ca\7F\2\2\u00c9\u00c1\3\2\2\2\u00c9\u00c4\3\2\2\2\u00c9"+
		"\u00c6\3\2\2\2\u00ca\n\3\2\2\2\u00cb\u00cc\7q\2\2\u00cc\u00d2\7t\2\2\u00cd"+
		"\u00ce\7~\2\2\u00ce\u00d2\7~\2\2\u00cf\u00d0\7Q\2\2\u00d0\u00d2\7T\2\2"+
		"\u00d1\u00cb\3\2\2\2\u00d1\u00cd\3\2\2\2\u00d1\u00cf\3\2\2\2\u00d2\f\3"+
		"\2\2\2\u00d3\u00d4\7z\2\2\u00d4\u00d5\7q\2\2\u00d5\u00d6\7t\2\2\u00d6"+
		"\16\3\2\2\2\u00d7\u00d8\7k\2\2\u00d8\u00d9\7u\2\2\u00d9\20\3\2\2\2\u00da"+
		"\u00db\7p\2\2\u00db\u00dc\7w\2\2\u00dc\u00dd\7n\2\2\u00dd\u00de\7n\2\2"+
		"\u00de\22\3\2\2\2\u00df\u00e0\7n\2\2\u00e0\u00e1\7k\2\2\u00e1\u00e2\7"+
		"m\2\2\u00e2\u00e3\7g\2\2\u00e3\24\3\2\2\2\u00e4\u00e5\7k\2\2\u00e5\u00e6"+
		"\7p\2\2\u00e6\26\3\2\2\2\u00e7\u00e8\7g\2\2\u00e8\u00e9\7z\2\2\u00e9\u00ea"+
		"\7k\2\2\u00ea\u00eb\7u\2\2\u00eb\u00ec\7v\2\2\u00ec\u00ed\7u\2\2\u00ed"+
		"\30\3\2\2\2\u00ee\u00ef\7c\2\2\u00ef\u00f0\7n\2\2\u00f0\u00f1\7n\2\2\u00f1"+
		"\32\3\2\2\2\u00f2\u00f3\7c\2\2\u00f3\u00f4\7p\2\2\u00f4\u00f5\7{\2\2\u00f5"+
		"\34\3\2\2\2\u00f6\u00f7\7v\2\2\u00f7\u00f8\7t\2\2\u00f8\u00f9\7w\2\2\u00f9"+
		"\u00fa\7g\2\2\u00fa\36\3\2\2\2\u00fb\u00fc\7h\2\2\u00fc\u00fd\7c\2\2\u00fd"+
		"\u00fe\7n\2\2\u00fe\u00ff\7u\2\2\u00ff\u0100\7g\2\2\u0100 \3\2\2\2\u0101"+
		"\u0102\7f\2\2\u0102\u0103\7k\2\2\u0103\u0106\7x\2\2\u0104\u0106\7\61\2"+
		"\2\u0105\u0101\3\2\2\2\u0105\u0104\3\2\2\2\u0106\"\3\2\2\2\u0107\u0108"+
		"\7o\2\2\u0108\u0109\7q\2\2\u0109\u010c\7f\2\2\u010a\u010c\7\'\2\2\u010b"+
		"\u0107\3\2\2\2\u010b\u010a\3\2\2\2\u010c$\3\2\2\2\u010d\u010e\7d\2\2\u010e"+
		"\u010f\7g\2\2\u010f\u0110\7v\2\2\u0110\u0111\7y\2\2\u0111\u0112\7g\2\2"+
		"\u0112\u0113\7g\2\2\u0113\u0114\7p\2\2\u0114&\3\2\2\2\u0115\u0116\7t\2"+
		"\2\u0116\u0117\7g\2\2\u0117\u0118\7i\2\2\u0118\u0119\7g\2\2\u0119\u011a"+
		"\7z\2\2\u011a\u011b\7r\2\2\u011b(\3\2\2\2\u011c\u011d\7-\2\2\u011d*\3"+
		"\2\2\2\u011e\u011f\7/\2\2\u011f,\3\2\2\2\u0120\u0121\7\u0080\2\2\u0121"+
		".\3\2\2\2\u0122\u0123\7~\2\2\u0123\60\3\2\2\2\u0124\u0125\7(\2\2\u0125"+
		"\62\3\2\2\2\u0126\u0127\7`\2\2\u0127\64\3\2\2\2\u0128\u0129\7d\2\2\u0129"+
		"\u012a\7k\2\2\u012a\u012b\7p\2\2\u012b\u012c\7c\2\2\u012c\u012d\7t\2\2"+
		"\u012d\u012e\7{\2\2\u012e\66\3\2\2\2\u012f\u0130\7>\2\2\u0130\u0131\7"+
		">\2\2\u01318\3\2\2\2\u0132\u0133\7@\2\2\u0133\u0134\7@\2\2\u0134:\3\2"+
		"\2\2\u0135\u0136\7g\2\2\u0136\u0137\7u\2\2\u0137\u0138\7e\2\2\u0138\u0139"+
		"\7c\2\2\u0139\u013a\7r\2\2\u013a\u013b\7g\2\2\u013b<\3\2\2\2\u013c\u013d"+
		"\7,\2\2\u013d>\3\2\2\2\u013e\u013f\7+\2\2\u013f@\3\2\2\2\u0140\u0141\7"+
		"*\2\2\u0141B\3\2\2\2\u0142\u0143\7_\2\2\u0143D\3\2\2\2\u0144\u0145\7]"+
		"\2\2\u0145F\3\2\2\2\u0146\u0147\7<\2\2\u0147H\3\2\2\2\u0148\u0149\7\60"+
		"\2\2\u0149\u014a\7,\2\2\u014aJ\3\2\2\2\u014b\u014c\7?\2\2\u014cL\3\2\2"+
		"\2\u014d\u014e\7>\2\2\u014eN\3\2\2\2\u014f\u0150\7@\2\2\u0150P\3\2\2\2"+
		"\u0151\u0152\7#\2\2\u0152\u0153\7?\2\2\u0153R\3\2\2\2\u0154\u0155\7p\2"+
		"\2\u0155\u0156\7q\2\2\u0156\u0157\7v\2\2\u0157T\3\2\2\2\u0158\u0159\7"+
		">\2\2\u0159\u015a\7?\2\2\u015aV\3\2\2\2\u015b\u015c\7@\2\2\u015c\u015d"+
		"\7?\2\2\u015dX\3\2\2\2\u015e\u015f\7=\2\2\u015fZ\3\2\2\2\u0160\u0161\7"+
		".\2\2\u0161\\\3\2\2\2\u0162\u0163\7\60\2\2\u0163^\3\2\2\2\u0164\u0165"+
		"\7e\2\2\u0165\u0166\7q\2\2\u0166\u0167\7n\2\2\u0167\u0168\7n\2\2\u0168"+
		"\u0169\7c\2\2\u0169\u016a\7v\2\2\u016a\u016b\7g\2\2\u016b`\3\2\2\2\u016c"+
		"\u016d\7k\2\2\u016d\u016e\7p\2\2\u016e\u016f\7p\2\2\u016f\u0170\7g\2\2"+
		"\u0170\u0171\7t\2\2\u0171b\3\2\2\2\u0172\u0173\7q\2\2\u0173\u0174\7w\2"+
		"\2\u0174\u0175\7v\2\2\u0175\u0176\7g\2\2\u0176\u0177\7t\2\2\u0177d\3\2"+
		"\2\2\u0178\u0179\7l\2\2\u0179\u017a\7q\2\2\u017a\u017b\7k\2\2\u017b\u017c"+
		"\7p\2\2\u017cf\3\2\2\2\u017d\u017e\7e\2\2\u017e\u017f\7t\2\2\u017f\u0180"+
		"\7q\2\2\u0180\u0181\7u\2\2\u0181\u0182\7u\2\2\u0182h\3\2\2\2\u0183\u0184"+
		"\7w\2\2\u0184\u0185\7u\2\2\u0185\u0186\7k\2\2\u0186\u0187\7p\2\2\u0187"+
		"\u0188\7i\2\2\u0188j\3\2\2\2\u0189\u018a\7k\2\2\u018a\u018b\7p\2\2\u018b"+
		"\u018c\7f\2\2\u018c\u018d\7g\2\2\u018d\u018e\7z\2\2\u018el\3\2\2\2\u018f"+
		"\u0190\7m\2\2\u0190\u0191\7g\2\2\u0191\u0192\7{\2\2\u0192n\3\2\2\2\u0193"+
		"\u0194\7q\2\2\u0194\u0195\7t\2\2\u0195\u0196\7f\2\2\u0196\u0197\7g\2\2"+
		"\u0197\u0198\7t\2\2\u0198p\3\2\2\2\u0199\u019a\7i\2\2\u019a\u019b\7t\2"+
		"\2\u019b\u019c\7q\2\2\u019c\u019d\7w\2\2\u019d\u019e\7r\2\2\u019er\3\2"+
		"\2\2\u019f\u01a0\7d\2\2\u01a0\u01a1\7{\2\2\u01a1t\3\2\2\2\u01a2\u01a3"+
		"\7h\2\2\u01a3\u01a4\7q\2\2\u01a4\u01a5\7t\2\2\u01a5v\3\2\2\2\u01a6\u01a7"+
		"\7w\2\2\u01a7\u01a8\7u\2\2\u01a8\u01a9\7g\2\2\u01a9x\3\2\2\2\u01aa\u01ab"+
		"\7k\2\2\u01ab\u01ac\7i\2\2\u01ac\u01ad\7p\2\2\u01ad\u01ae\7q\2\2\u01ae"+
		"\u01af\7t\2\2\u01af\u01b0\7g\2\2\u01b0z\3\2\2\2\u01b1\u01b2\7r\2\2\u01b2"+
		"\u01b3\7c\2\2\u01b3\u01b4\7t\2\2\u01b4\u01b5\7v\2\2\u01b5\u01b6\7k\2\2"+
		"\u01b6\u01b7\7v\2\2\u01b7\u01b8\7k\2\2\u01b8\u01b9\7q\2\2\u01b9\u01ba"+
		"\7p\2\2\u01ba|\3\2\2\2\u01bb\u01bc\7u\2\2\u01bc\u01bd\7v\2\2\u01bd\u01be"+
		"\7t\2\2\u01be\u01bf\7c\2\2\u01bf\u01c0\7k\2\2\u01c0\u01c1\7i\2\2\u01c1"+
		"\u01c2\7j\2\2\u01c2\u01c3\7v\2\2\u01c3\u01c4\7a\2\2\u01c4\u01c5\7l\2\2"+
		"\u01c5\u01c6\7q\2\2\u01c6\u01c7\7k\2\2\u01c7\u01c8\7p\2\2\u01c8~\3\2\2"+
		"\2\u01c9\u01ca\7p\2\2\u01ca\u01cb\7c\2\2\u01cb\u01cc\7v\2\2\u01cc\u01cd"+
		"\7w\2\2\u01cd\u01ce\7t\2\2\u01ce\u01cf\7c\2\2\u01cf\u01d0\7n\2\2\u01d0"+
		"\u0080\3\2\2\2\u01d1\u01d2\7n\2\2\u01d2\u01d3\7g\2\2\u01d3\u01d4\7h\2"+
		"\2\u01d4\u01d5\7v\2\2\u01d5\u0082\3\2\2\2\u01d6\u01d7\7t\2\2\u01d7\u01d8"+
		"\7k\2\2\u01d8\u01d9\7i\2\2\u01d9\u01da\7j\2\2\u01da\u01db\7v\2\2\u01db"+
		"\u0084\3\2\2\2\u01dc\u01dd\7q\2\2\u01dd\u01de\7l\2\2\u01de\u0086\3\2\2"+
		"\2\u01df\u01e0\7q\2\2\u01e0\u01e1\7p\2\2\u01e1\u0088\3\2\2\2\u01e2\u01e7"+
		"\t\2\2\2\u01e3\u01e6\t\2\2\2\u01e4\u01e6\5\u008dG\2\u01e5\u01e3\3\2\2"+
		"\2\u01e5\u01e4\3\2\2\2\u01e6\u01e9\3\2\2\2\u01e7\u01e5\3\2\2\2\u01e7\u01e8"+
		"\3\2\2\2\u01e8\u008a\3\2\2\2\u01e9\u01e7\3\2\2\2\u01ea\u01f4\7)\2\2\u01eb"+
		"\u01ec\7^\2\2\u01ec\u01f3\7^\2\2\u01ed\u01ee\7)\2\2\u01ee\u01f3\7)\2\2"+
		"\u01ef\u01f0\7^\2\2\u01f0\u01f3\7)\2\2\u01f1\u01f3\n\3\2\2\u01f2\u01eb"+
		"\3\2\2\2\u01f2\u01ed\3\2\2\2\u01f2\u01ef\3\2\2\2\u01f2\u01f1\3\2\2\2\u01f3"+
		"\u01f6\3\2\2\2\u01f4\u01f2\3\2\2\2\u01f4\u01f5\3\2\2\2\u01f5\u01f7\3\2"+
		"\2\2\u01f6\u01f4\3\2\2\2\u01f7\u0207\7)\2\2\u01f8\u0202\7$\2\2\u01f9\u01fa"+
		"\7^\2\2\u01fa\u0201\7^\2\2\u01fb\u01fc\7$\2\2\u01fc\u0201\7$\2\2\u01fd"+
		"\u01fe\7^\2\2\u01fe\u0201\7$\2\2\u01ff\u0201\n\4\2\2\u0200\u01f9\3\2\2"+
		"\2\u0200\u01fb\3\2\2\2\u0200\u01fd\3\2\2\2\u0200\u01ff\3\2\2\2\u0201\u0204"+
		"\3\2\2\2\u0202\u0200\3\2\2\2\u0202\u0203\3\2\2\2\u0203\u0205\3\2\2\2\u0204"+
		"\u0202\3\2\2\2\u0205\u0207\7$\2\2\u0206\u01ea\3\2\2\2\u0206\u01f8\3\2"+
		"\2\2\u0207\u008c\3\2\2\2\u0208\u020a\4\62;\2\u0209\u0208\3\2\2\2\u020a"+
		"\u020b\3\2\2\2\u020b\u0209\3\2\2\2\u020b\u020c\3\2\2\2\u020c\u008e\3\2"+
		"\2\2\u020d\u020f\7\17\2\2\u020e\u020d\3\2\2\2\u020e\u020f\3\2\2\2\u020f"+
		"\u0210\3\2\2\2\u0210\u0211\7\f\2\2\u0211\u0212\3\2\2\2\u0212\u0213\bH"+
		"\2\2\u0213\u0090\3\2\2\2\u0214\u0216\t\5\2\2\u0215\u0214\3\2\2\2\u0216"+
		"\u0217\3\2\2\2\u0217\u0215\3\2\2\2\u0217\u0218\3\2\2\2\u0218\u0219\3\2"+
		"\2\2\u0219\u021a\bI\2\2\u021a\u0092\3\2\2\2\u021b\u0220\7B\2\2\u021c\u0221"+
		"\5\u0095K\2\u021d\u0221\5\u0097L\2\u021e\u0221\5\u0099M\2\u021f\u0221"+
		"\5\u009bN\2\u0220\u021c\3\2\2\2\u0220\u021d\3\2\2\2\u0220\u021e\3\2\2"+
		"\2\u0220\u021f\3\2\2\2\u0221\u0094\3\2\2\2\u0222\u0224\7b\2\2\u0223\u0225"+
		"\n\6\2\2\u0224\u0223\3\2\2\2\u0225\u0226\3\2\2\2\u0226\u0224\3\2\2\2\u0226"+
		"\u0227\3\2\2\2\u0227\u0228\3\2\2\2\u0228\u0229\7b\2\2\u0229\u0096\3\2"+
		"\2\2\u022a\u022c\7)\2\2\u022b\u022d\n\3\2\2\u022c\u022b\3\2\2\2\u022d"+
		"\u022e\3\2\2\2\u022e\u022c\3\2\2\2\u022e\u022f\3\2\2\2\u022f\u0230\3\2"+
		"\2\2\u0230\u0231\7)\2\2\u0231\u0098\3\2\2\2\u0232\u0234\7$\2\2\u0233\u0235"+
		"\n\4\2\2\u0234\u0233\3\2\2\2\u0235\u0236\3\2\2\2\u0236\u0234\3\2\2\2\u0236"+
		"\u0237\3\2\2\2\u0237\u0238\3\2\2\2\u0238\u0239\7$\2\2\u0239\u009a\3\2"+
		"\2\2\u023a\u023d\t\7\2\2\u023b\u023d\5]/\2\u023c\u023a\3\2\2\2\u023c\u023b"+
		"\3\2\2\2\u023d\u023e\3\2\2\2\u023e\u023c\3\2\2\2\u023e\u023f\3\2\2\2\u023f"+
		"\u009c\3\2\2\2\32\2\u00a9\u00b3\u00bf\u00c9\u00d1\u0105\u010b\u01e5\u01e7"+
		"\u01f2\u01f4\u0200\u0202\u0206\u020b\u020e\u0217\u0220\u0226\u022e\u0236"+
		"\u023c\u023e\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}