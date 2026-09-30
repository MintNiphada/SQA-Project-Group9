package com.google.javascript.jscomp.parsing;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.javascript.jscomp.parsing.Config.LanguageMode;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.head.CompilerEnvirons;
import com.google.javascript.rhino.head.ErrorReporter;
import com.google.javascript.rhino.head.EvaluatorException;
import com.google.javascript.rhino.head.Parser;
import com.google.javascript.rhino.head.ast.AstRoot;
import com.google.javascript.rhino.head.ast.Comment;
import com.google.javascript.rhino.head.Token.CommentType;
import com.google.javascript.rhino.jstype.SimpleSourceFile;
import com.google.javascript.rhino.jstype.StaticSourceFile;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

class IRFactoryTest {

  private List<String> errors;
  private List<String> warnings;

  private ErrorReporter errorReporter = new ErrorReporter() {
    @Override
    public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
      warnings.add(message);
    }

    @Override
    public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
      errors.add(message);
    }

    @Override
    public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
      errors.add(message);
      return new EvaluatorException(message);
    }
  };

  @Before
  public void setUp() {
    errors = new ArrayList<String>();
    warnings = new ArrayList<String>();
  }

  private Node parse(String source, Config config) {
    CompilerEnvirons env = new CompilerEnvirons();
    env.setRecordingComments(true);
    env.setRecordingLocalJsDocComments(true);
    env.setWarnTrailingComma(true);
    if (config.languageMode == LanguageMode.ECMASCRIPT5_STRICT) {
      env.setStrictMode(true);
    }
    Parser parser = new Parser(env, errorReporter);
    AstRoot astRoot = parser.parse(source, "testcode", 1);
    StaticSourceFile sourceFile = new SimpleSourceFile("testcode", false);
    return IRFactory.transformTree(astRoot, sourceFile, source, config, errorReporter);
  }

  private Config createConfig(LanguageMode mode, boolean isIdeMode, boolean acceptConst) {
    Set<String> empty = Collections.emptySet();
    return new Config(empty, empty, isIdeMode, mode, acceptConst);
  }

  private Config defaultConfig() {
    return createConfig(LanguageMode.ECMASCRIPT5, false, false);
  }

  @Test
  public void testDirectives() {
    String code = "'use strict'; var x = 1;";
    Node root = parse(code, defaultConfig());
    assertNotNull(root);
    assertEquals(Token.SCRIPT, root.getType());
    Set<String> directives = root.getDirectives();
    assertNotNull(directives);
    assertTrue(directives.contains("use strict"));
    assertEquals(1, root.getChildCount());
  }

  @Test
  public void testVariableDeclarations() {
    String code = "var a = 1, b = 2; var c;";
    Node root = parse(code, defaultConfig());
    assertEquals(Token.SCRIPT, root.getType());
    assertEquals(2, root.getChildCount());
    Node var1 = root.getFirstChild();
    assertEquals(Token.VAR, var1.getType());
    assertEquals(2, var1.getChildCount());
    Node nameA = var1.getFirstChild();
    assertEquals(Token.NAME, nameA.getType());
    assertEquals("a", nameA.getString());
    assertTrue(nameA.hasChildren());
    assertEquals(1.0, nameA.getFirstChild().getDouble(), 0.0);
  }

  @Test
  public void testFunctionDeclarations() {
    String code = "function foo(a, b) { 'use strict'; return a + b; }";
    Node root = parse(code, defaultConfig());
    Node fn = root.getFirstChild();
    assertEquals(Token.FUNCTION, fn.getType());
    Node fnName = fn.getFirstChild();
    assertEquals("foo", fnName.getString());
    Node params = fnName.getNext();
    assertEquals(Token.PARAM_LIST, params.getType());
    assertEquals(2, params.getChildCount());
    Node body = params.getNext();
    assertEquals(Token.BLOCK, body.getType());
    assertTrue(body.getDirectives().contains("use strict"));
  }

  @Test
  public void testFunctionExpression() {
    String code = "var f = function(x) { return x; };";
    Node root = parse(code, defaultConfig());
    Node varNode = root.getFirstChild();
    Node nameNode = varNode.getFirstChild();
    Node fnNode = nameNode.getFirstChild();
    assertEquals(Token.FUNCTION, fnNode.getType());
    Node fnName = fnNode.getFirstChild();
    assertEquals("", fnName.getString());
  }

  @Test
  public void testControlStructures() {
    String code = ""
        + "if (true) { var a = 1; } else { var a = 2; }\n"
        + "while (false) { break; }\n"
        + "do { continue; } while (false);\n"
        + "for (var i = 0; i < 10; i++) {}\n"
        + "for (var k in obj) {}\n"
        + "switch (x) { case 1: break; default: break; }\n"
        + "try { throw 1; } catch (e) { } finally { }";

    Node root = parse(code, defaultConfig());
    assertNotNull(root);
    assertEquals(7, root.getChildCount());

    Node ifNode = root.getFirstChild();
    assertEquals(Token.IF, ifNode.getType());

    Node whileNode = ifNode.getNext();
    assertEquals(Token.WHILE, whileNode.getType());

    Node doNode = whileNode.getNext();
    assertEquals(Token.DO, doNode.getType());

    Node forNode = doNode.getNext();
    assertEquals(Token.FOR, forNode.getType());

    Node forInNode = forNode.getNext();
    assertEquals(Token.FOR, forInNode.getType());

    Node switchNode = forInNode.getNext();
    assertEquals(Token.SWITCH, switchNode.getType());

    Node tryNode = switchNode.getNext();
    assertEquals(Token.TRY, tryNode.getType());
  }

  @Test
  public void testLabels() {
    String code = "lbl1: lbl2: while (true) { break lbl1; continue lbl2; }";
    Node root = parse(code, defaultConfig());
    Node label1 = root.getFirstChild();
    assertEquals(Token.LABEL, label1.getType());
    Node labelName1 = label1.getFirstChild();
    assertEquals(Token.LABEL_NAME, labelName1.getType());
    assertEquals("lbl1", labelName1.getString());
  }

  @Test
  public void testObjectAndArrayLiterals() {
    String code = "var obj = { a: 1, 'b': 2, 3: 4, get x() { return 0; }, set x(v) {} }; var arr = [1, 2, , 3];";
    Node root = parse(code, defaultConfig());
    Node var1 = root.getFirstChild();
    Node objLit = var1.getFirstChild().getFirstChild();
    assertEquals(Token.OBJECTLIT, objLit.getType());
    assertEquals(5, objLit.getChildCount());

    Node getter = objLit.getChildAtIndex(3);
    assertEquals(Token.GETTER_DEF, getter.getType());
    assertEquals("x", getter.getString());

    Node setter = objLit.getChildAtIndex(4);
    assertEquals(Token.SETTER_DEF, setter.getType());
    assertEquals("x", setter.getString());

    Node var2 = var1.getNext();
    Node arrLit = var2.getFirstChild().getFirstChild();
    assertEquals(Token.ARRAYLIT, arrLit.getType());
    assertEquals(4, arrLit.getChildCount());
    assertEquals(Token.EMPTY, arrLit.getChildAtIndex(2).getType());
  }

  @Test
  public void testUnaryAndBinaryExpressions() {
    String code = "var r = !(typeof +(-5) == 'number') && (a != b) || (c === d) ^ (e | f) & (g >> 1) >>> 2 << 3 % 4 / 2 * 1;";
    Node root = parse(code, defaultConfig());
    assertNotNull(root);
  }

  @Test
  public void testUnaryMinusOptimization() {
    String code = "var x = -5;";
    Node root = parse(code, defaultConfig());
    Node varNode = root.getFirstChild();
    Node numNode = varNode.getFirstChild().getFirstChild();
    assertEquals(Token.NUMBER, numNode.getType());
    assertEquals(-5.0, numNode.getDouble(), 0.0);
  }

  @Test
  public void testDeleteAndIncDecTargetErrors() {
    String code = "delete (1 + 2); (1 + 2)++;";
    parse(code, defaultConfig());
    assertTrue(errors.size() >= 2);
  }

  @Test
  public void testRegExpLiteral() {
    String code = "var re1 = /abc/g; var re2 = /xyz/;";
    Node root = parse(code, defaultConfig());
    Node var1 = root.getFirstChild();
    Node re1 = var1.getFirstChild().getFirstChild();
    assertEquals(Token.REGEXP, re1.getType());
    assertEquals("abc", re1.getFirstChild().getString());
    assertEquals("g", re1.getFirstChild().getNext().getString());

    Node var2 = var1.getNext();
    Node re2 = var2.getFirstChild().getFirstChild();
    assertEquals(Token.REGEXP, re2.getType());
    assertEquals("xyz", re2.getFirstChild().getString());
    assertNull(re2.getFirstChild().getNext());
  }

  @Test
  public void testVerticalTabSlashV() {
    String code = "var s = '\\v';";
    Node root = parse(code, defaultConfig());
    Node strNode = root.getFirstChild().getFirstChild().getFirstChild();
    assertEquals(Token.STRING, strNode.getType());
    assertEquals(Boolean.TRUE, strNode.getProp(Node.SLASH_V));
  }

  @Test
  public void testSuspiciousComment() {
    String code = "/* @type {number} */ var x = 1;\n/** @type {number} */ var y = 2;";
    parse(code, defaultConfig());
    assertEquals(1, warnings.size());
    assertTrue(warnings.get(0).contains("Non-JSDoc comment has annotations"));
  }

  @Test
  public void testReservedKeywordsES5() {
    String code = "var let = 1;";
    parse(code, createConfig(LanguageMode.ECMASCRIPT5_STRICT, false, false));
    assertFalse(errors.isEmpty());
  }

  @Test
  public void testGetterSetterES3Warning() {
    String code = "var o = { get a() { return 1; }, set a(v) {} };";
    parse(code, createConfig(LanguageMode.ECMASCRIPT3, false, false));
    assertTrue(errors.size() >= 2);
  }

  @Test
  public void testWithStatement() {
    String code = "with (obj) { a = 1; }";
    Node root = parse(code, defaultConfig());
    assertEquals(Token.WITH, root.getFirstChild().getType());
  }

  @Test
  public void testConditionalHookAndParentheses() {
    String code = "var x = (a ? b : c);";
    Node root = parse(code, defaultConfig());
    Node hookNode = root.getFirstChild().getFirstChild().getFirstChild();
    assertEquals(Token.HOOK, hookNode.getType());
    assertEquals(Boolean.TRUE, hookNode.getProp(Node.PARENTHESIZED_PROP));
  }

  @Test
  public void testFileOverviewAndLicense() {
    String code = "/**\n * @fileoverview Description\n * @license MIT\n */\nvar x = 1;";
    Node root = parse(code, defaultConfig());
    assertNotNull(root.getJSDocInfo());
    assertNotNull(root.getJSDocInfo().getLicense());
  }

  @Test
  public void testIdeModeNodeLengths() {
    String code = "var x = 1 + 2;";
    Node root = parse(code, createConfig(LanguageMode.ECMASCRIPT5, true, false));
    assertNotNull(root);
    assertTrue(root.getFirstChild().getLength() > 0);
  }
}

public class TargetClassTest extends IRFactoryTest {
}