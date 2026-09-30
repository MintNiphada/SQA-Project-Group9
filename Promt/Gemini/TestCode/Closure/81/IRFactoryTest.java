package com.google.javascript.jscomp.parsing;

import com.google.javascript.jscomp.mozilla.rhino.CompilerEnvirons;
import com.google.javascript.jscomp.mozilla.rhino.Context;
import com.google.javascript.jscomp.mozilla.rhino.ErrorReporter;
import com.google.javascript.jscomp.mozilla.rhino.EvaluatorException;
import com.google.javascript.jscomp.mozilla.rhino.Parser;
import com.google.javascript.jscomp.mozilla.rhino.ast.AstRoot;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class IRFactoryTest {

  private List<String> errors;
  private List<String> warnings;
  private ErrorReporter errorReporter;

  @Before
  public void setUp() {
    errors = new ArrayList<String>();
    warnings = new ArrayList<String>();
    errorReporter = new ErrorReporter() {
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
        return new EvaluatorException(message, sourceName, line, lineSource, lineOffset);
      }
    };
  }

  private Config createConfig(boolean acceptES5, boolean acceptConst) {
    Set<String> empty = Collections.emptySet();
    return new Config(empty, empty, true, acceptES5, acceptConst);
  }

  private Node parseAndTransform(String js, boolean acceptES5, boolean acceptConst) {
    CompilerEnvirons env = new CompilerEnvirons();
    env.setRecordingComments(true);
    env.setRecordingLocalJsDocComments(true);
    env.setLanguageVersion(Context.VERSION_1_8);
    env.setReservedKeywordAsIdentifier(true);
    env.setIdeMode(true);

    Parser p = new Parser(env, errorReporter);
    AstRoot ast = p.parse(js, "test.js", 1);
    return IRFactory.transformTree(ast, js, createConfig(acceptES5, acceptConst), errorReporter);
  }

  private Node parseAndTransform(String js) {
    return parseAndTransform(js, true, true);
  }

  @Test
  public void testSimpleExpressions() {
    Node root = parseAndTransform("var a = 1 + 2;");
    assertNotNull(root);
    assertEquals(Token.SCRIPT, root.getType());
    assertEquals(0, errors.size());

    Node varNode = root.getFirstChild();
    assertEquals(Token.VAR, varNode.getType());
    Node nameNode = varNode.getFirstChild();
    assertEquals("a", nameNode.getString());
    Node addNode = nameNode.getFirstChild();
    assertEquals(Token.ADD, addNode.getType());
    assertEquals(1.0, addNode.getFirstChild().getDouble(), 0.0);
    assertEquals(2.0, addNode.getLastChild().getDouble(), 0.0);
  }

  @Test
  public void testDirectives() {
    Node root = parseAndTransform("'use strict';\nvar x = 1;");
    assertNotNull(root);
    Set<String> directives = root.getDirectives();
    assertNotNull(directives);
    assertTrue(directives.contains("use strict"));
    assertEquals(Token.VAR, root.getFirstChild().getType());

    Node fnRoot = parseAndTransform("function f() { 'use strict'; return 42; }");
    Node fn = fnRoot.getFirstChild();
    assertEquals(Token.FUNCTION, fn.getType());
    Node block = fn.getLastChild();
    Set<String> fnDirectives = block.getDirectives();
    assertNotNull(fnDirectives);
    assertTrue(fnDirectives.contains("use strict"));
  }

  @Test
  public void testFunctions() {
    Node root = parseAndTransform("function foo(x, y) { return x + y; }");
    Node fn = root.getFirstChild();
    assertEquals(Token.FUNCTION, fn.getType());
    assertEquals("foo", fn.getFirstChild().getString());

    Node params = fn.getFirstChild().getNext();
    assertEquals(Token.LP, params.getType());
    assertEquals(2, params.getChildCount());

    Node anonRoot = parseAndTransform("(function() { return 1; });");
    assertNotNull(anonRoot);
  }

  @Test
  public void testArrayLiteral() {
    Node root = parseAndTransform("var arr = [1, , 3];");
    Node varNode = root.getFirstChild();
    Node arrLit = varNode.getFirstChild().getFirstChild();
    assertEquals(Token.ARRAYLIT, arrLit.getType());
    int[] skipIndexes = (int[]) arrLit.getProp(Node.SKIP_INDEXES_PROP);
    assertNotNull(skipIndexes);
    assertEquals(1, skipIndexes.length);
    assertEquals(1, skipIndexes[0]);
  }

  @Test
  public void testObjectLiteral() {
    Node root = parseAndTransform("var obj = { a: 1, 'b': 2, 3: 4 };");
    Node varNode = root.getFirstChild();
    Node objLit = varNode.getFirstChild().getFirstChild();
    assertEquals(Token.OBJECTLIT, objLit.getType());
    assertEquals(3, objLit.getChildCount());

    Node keyA = objLit.getFirstChild();
    assertEquals(Token.STRING, keyA.getType());
    assertEquals("a", keyA.getString());
  }

  @Test
  public void testGettersAndSetters() {
    Node root = parseAndTransform("var o = { get x() { return 1; }, set x(v) { this.x = v; } };");
    assertEquals(0, errors.size());
    Node varNode = root.getFirstChild();
    Node objLit = varNode.getFirstChild().getFirstChild();
    assertEquals(Token.OBJECTLIT, objLit.getType());

    Node getProp = objLit.getFirstChild();
    assertEquals(Token.GET, getProp.getType());
    Node setProp = getProp.getNext();
    assertEquals(Token.SET, setProp.getType());
  }

  @Test
  public void testGettersAndSettersErrorsInES3() {
    parseAndTransform("var o = { get x() { return 1; }, set x(v) { this.x = v; } };", false, true);
    assertTrue(errors.size() >= 2);
  }

  @Test
  public void testGetterWithParam() {
    parseAndTransform("var o = { get x(invalid) { return 1; } };", true, true);
    assertTrue(errors.size() > 0);
  }

  @Test
  public void testSetterWithoutParam() {
    parseAndTransform("var o = { set x() { } };", true, true);
    assertTrue(errors.size() > 0);
  }

  @Test
  public void testControlStructures() {
    String js = "if (true) { a(); } else { b(); }\n" +
                "while (false) { continue; }\n" +
                "do { break; } while(true);\n" +
                "for (var i = 0; i < 10; i++) {}\n" +
                "for (var k in obj) {}\n" +
                "with (o) { bar(); }\n" +
                "try { throw 1; } catch (e) { } finally { }";
    Node root = parseAndTransform(js);
    assertEquals(0, errors.size());
    assertNotNull(root);
    assertEquals(7, root.getChildCount());
  }

  @Test
  public void testSwitchStatement() {
    String js = "switch (x) {\n" +
                "  case 1: break;\n" +
                "  case 2: return;\n" +
                "  default: break;\n" +
                "}";
    Node root = parseAndTransform(js);
    assertEquals(0, errors.size());
    Node switchNode = root.getFirstChild();
    assertEquals(Token.SWITCH, switchNode.getType());
    assertEquals(4, switchNode.getChildCount()); // expr, case1, case2, default
  }

  @Test
  public void testLabels() {
    String js = "outer: for (;;) { inner: while (true) { break outer; continue inner; } }";
    Node root = parseAndTransform(js);
    assertEquals(0, errors.size());
    Node labelNode = root.getFirstChild();
    assertEquals(Token.LABEL, labelNode.getType());
  }

  @Test
  public void testUnaryAndParentheses() {
    String js = "var x = -(+(!(~a)));\n" +
                "x++;\n" +
                "--x;\n" +
                "(x);";
    Node root = parseAndTransform(js);
    assertEquals(0, errors.size());
    assertNotNull(root);
  }

  @Test
  public void testInvalidAssignmentAndIncrements() {
    parseAndTransform("1 = 2;");
    assertTrue(errors.size() > 0);

    errors.clear();
    parseAndTransform("1++;");
    assertTrue(errors.size() > 0);

    errors.clear();
    parseAndTransform("--1;");
    assertTrue(errors.size() > 0);
  }

  @Test
  public void testConstKeyword() {
    parseAndTransform("const x = 1;", true, true);
    assertEquals(0, errors.size());

    errors.clear();
    parseAndTransform("const x = 1;", true, false);
    assertTrue(errors.size() > 0);
  }

  @Test
  public void testRegExpAndLiterals() {
    String js = "var r1 = /abc/g;\n" +
                "var r2 = /xyz/;\n" +
                "var n = null;\n" +
                "var t = true;\n" +
                "var f = false;\n" +
                "var th = this;\n" +
                "var c = a ? b : c;";
    Node root = parseAndTransform(js);
    assertEquals(0, errors.size());
    assertNotNull(root);
  }

  @Test
  public void testElementGetAndPropertyGet() {
    String js = "var a = obj.prop;\n" +
                "var b = obj['prop'];\n" +
                "var c = new Foo(1, 2);";
    Node root = parseAndTransform(js);
    assertEquals(0, errors.size());
    assertNotNull(root);
  }

  @Test
  public void testJSDocFileoverviewAndLicense() {
    String js = "/**\n * @fileoverview A test file.\n * @license MIT\n */\n" +
                "/**\n * @param {number} x\n */\n" +
                "function test(x) {}\n";
    Node root = parseAndTransform(js);
    assertEquals(0, errors.size());
    JSDocInfo fileDoc = root.getJSDocInfo();
    assertNotNull(fileDoc);
    assertEquals("MIT", fileDoc.getLicense());

    Node fnNode = root.getFirstChild();
    JSDocInfo fnDoc = fnNode.getJSDocInfo();
    assertNotNull(fnDoc);
  }

  @Test
  public void testNegativeNumberLiteral() {
    String js = "var x = -5;";
    Node root = parseAndTransform(js);
    assertEquals(0, errors.size());
    Node varNode = root.getFirstChild();
    Node numberNode = varNode.getFirstChild().getFirstChild();
    assertEquals(Token.NUMBER, numberNode.getType());
    assertEquals(-5.0, numberNode.getDouble(), 0.0);
  }

  @Test
  public void testEmptyReturnAndEmptyStatement() {
    String js = "function f() { return; }; ;";
    Node root = parseAndTransform(js);
    assertEquals(0, errors.size());
    assertNotNull(root);
  }
}