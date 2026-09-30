package com.google.javascript.jscomp.parsing;

import com.google.common.collect.ImmutableSet;
import com.google.javascript.jscomp.parsing.Config.LanguageMode;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.head.CompilerEnvirons;
import com.google.javascript.rhino.head.ErrorReporter;
import com.google.javascript.rhino.head.EvaluatorException;
import com.google.javascript.rhino.head.Parser;
import com.google.javascript.rhino.head.ast.AstRoot;
import com.google.javascript.rhino.jstype.SimpleSourceFile;
import com.google.javascript.rhino.jstype.StaticSourceFile;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TargetClassTest {

  private static class TestErrorReporter implements ErrorReporter {
    private final List<String> errors = new ArrayList<String>();
    private final List<String> warnings = new ArrayList<String>();

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
      error(message, sourceName, line, lineSource, lineOffset);
      return new EvaluatorException(message);
    }

    public boolean hasErrors() {
      return !errors.isEmpty();
    }

    public boolean hasWarnings() {
      return !warnings.isEmpty();
    }
  }

  private Node parseAndTransform(String source, LanguageMode mode, boolean isIdeMode, TestErrorReporter reporter) {
    CompilerEnvirons env = new CompilerEnvirons();
    env.setLanguageVersion(com.google.javascript.rhino.head.Context.VERSION_1_8);
    env.setRecordingComments(true);
    env.setRecordingLocalJsDocComments(true);
    env.setWarnTrailingComma(true);
    if (isIdeMode) {
      env.setIdeMode(true);
      env.setRecoverFromErrors(true);
    }

    Parser parser = new Parser(env, reporter);
    AstRoot root = parser.parse(source, "test.js", 1);

    Set<String> annotationNames = new HashSet<String>();
    Set<String> suppressionNames = new HashSet<String>();
    Config config = new Config(annotationNames, suppressionNames, isIdeMode, mode, false);
    StaticSourceFile sourceFile = new SimpleSourceFile("test.js", false);

    return IRFactory.transformTree(root, sourceFile, source, config, reporter);
  }

  private Node parseAndTransform(String source, LanguageMode mode) {
    TestErrorReporter reporter = new TestErrorReporter();
    return parseAndTransform(source, mode, false, reporter);
  }

  private Node parseAndTransform(String source) {
    return parseAndTransform(source, LanguageMode.ECMASCRIPT5);
  }

  @Test
  public void testSimpleExpressions() {
    Node node = parseAndTransform("var a = 1 + 2 * 3;");
    Assert.assertNotNull(node);
    Assert.assertEquals(Token.SCRIPT, node.getType());
    Node varNode = node.getFirstChild();
    Assert.assertEquals(Token.VAR, varNode.getType());
  }

  @Test
  public void testDirectives() {
    Node node = parseAndTransform("'use strict'; var x = 1;");
    Assert.assertNotNull(node);
    Set<String> directives = node.getDirectives();
    Assert.assertNotNull(directives);
    Assert.assertTrue(directives.contains("use strict"));
    Assert.assertEquals(Token.VAR, node.getFirstChild().getType());
  }

  @Test
  public void testFunctionDirectives() {
    Node node = parseAndTransform("function foo() { 'use strict'; return 42; }");
    Node fn = node.getFirstChild();
    Assert.assertEquals(Token.FUNCTION, fn.getType());
    Node block = fn.getLastChild();
    Assert.assertNotNull(block.getDirectives());
    Assert.assertTrue(block.getDirectives().contains("use strict"));
  }

  @Test
  public void testUnaryExpressions() {
    Node node = parseAndTransform("var a = -5; var b = +5; var c = !false; var d = ~0; var e = typeof a; var f = void 0;");
    Assert.assertNotNull(node);
    
    Node node2 = parseAndTransform("var x = 1; x++; ++x; x--; --x; delete x.prop; delete x['prop']; delete x;");
    Assert.assertNotNull(node2);
  }

  @Test
  public void testInvalidDeleteAndIncDec() {
    TestErrorReporter reporter = new TestErrorReporter();
    parseAndTransform("delete (1 + 2);", LanguageMode.ECMASCRIPT5, false, reporter);
    Assert.assertTrue(reporter.hasErrors());
    Assert.assertTrue(reporter.errors.get(0).contains("Invalid delete operand"));

    TestErrorReporter reporter2 = new TestErrorReporter();
    parseAndTransform("++(1 + 2);", LanguageMode.ECMASCRIPT5, false, reporter2);
    Assert.assertTrue(reporter2.hasErrors());
    Assert.assertTrue(reporter2.errors.get(0).contains("invalid increment target"));

    TestErrorReporter reporter3 = new TestErrorReporter();
    parseAndTransform("--(1 + 2);", LanguageMode.ECMASCRIPT5, false, reporter3);
    Assert.assertTrue(reporter3.hasErrors());
    Assert.assertTrue(reporter3.errors.get(0).contains("invalid decrement target"));
  }

  @Test
  public void testControlStructures() {
    String src = "if (true) { a(); } else { b(); }\n" +
                 "while (false) { break; }\n" +
                 "do { continue; } while (false);\n" +
                 "for (var i = 0; i < 10; i++) { }\n" +
                 "for (var k in obj) { }\n" +
                 "switch (a) { case 1: break; default: break; }\n" +
                 "try { throw 1; } catch (e) { } finally { }\n" +
                 "with (obj) { }\n";
    Node node = parseAndTransform(src);
    Assert.assertNotNull(node);
  }

  @Test
  public void testLabelsAndBreakContinue() {
    String src = "loop1: for (var i = 0; i < 5; i++) { break loop1; continue loop1; }";
    Node node = parseAndTransform(src);
    Assert.assertNotNull(node);
    Assert.assertEquals(Token.SCRIPT, node.getType());
    Assert.assertEquals(Token.LABEL, node.getFirstChild().getType());
  }

  @Test
  public void testObjectLiteral() {
    String src = "var obj = { a: 1, 'b': 2, 3: 3, get c() { return 0; }, set c(val) { } };";
    Node node = parseAndTransform(src, LanguageMode.ECMASCRIPT5);
    Assert.assertNotNull(node);
  }

  @Test
  public void testObjectLiteralEs3GettersSettersReported() {
    TestErrorReporter reporter = new TestErrorReporter();
    parseAndTransform("var obj = { get a() { return 1; } };", LanguageMode.ECMASCRIPT3, false, reporter);
    Assert.assertTrue(reporter.hasErrors());
    Assert.assertTrue(reporter.errors.get(0).contains("getters are not supported in Internet Explorer"));

    TestErrorReporter reporter2 = new TestErrorReporter();
    parseAndTransform("var obj = { set a(x) { } };", LanguageMode.ECMASCRIPT3, false, reporter2);
    Assert.assertTrue(reporter2.hasErrors());
    Assert.assertTrue(reporter2.errors.get(0).contains("setters are not supported in Internet Explorer"));
  }

  @Test
  public void testObjectLiteralGetterSetterParamErrors() {
    CompilerEnvirons env = new CompilerEnvirons();
    env.setLanguageVersion(com.google.javascript.rhino.head.Context.VERSION_1_8);
    Parser parser = new Parser(env, new TestErrorReporter());
    AstRoot root = parser.parse("var obj = { get a(x) { return 1; }, set b() { } };", "test.js", 1);

    TestErrorReporter reporter = new TestErrorReporter();
    Config config = new Config(new HashSet<String>(), new HashSet<String>(), false, LanguageMode.ECMASCRIPT5, false);
    StaticSourceFile sourceFile = new SimpleSourceFile("test.js", false);
    IRFactory.transformTree(root, sourceFile, "var obj = { get a(x) { return 1; }, set b() { } };", config, reporter);

    Assert.assertTrue(reporter.hasErrors());
  }

  @Test
  public void testArrayLiteralAndDestructuring() {
    Node node = parseAndTransform("var arr = [1, 2, , 4];");
    Assert.assertNotNull(node);
  }

  @Test
  public void testRegExpLiteral() {
    Node node = parseAndTransform("var re = /abc/gi; var re2 = /def/;");
    Assert.assertNotNull(node);
  }

  @Test
  public void testReservedKeywordsInEs5Strict() {
    TestErrorReporter reporter = new TestErrorReporter();
    parseAndTransform("var let = 1;", LanguageMode.ECMASCRIPT5_STRICT, false, reporter);
    Assert.assertTrue(reporter.hasErrors());
    Assert.assertTrue(reporter.errors.get(0).contains("identifier is a reserved word"));
  }

  @Test
  public void testCommentsAndJsDoc() {
    String src = "/** @fileoverview Test file \n * @license MIT */\n" +
                 "/** @type {number} */\n" +
                 "var x = 10;\n" +
                 "/* @suspicious */\n" +
                 "// regular line comment\n";
    TestErrorReporter reporter = new TestErrorReporter();
    Node node = parseAndTransform(src, LanguageMode.ECMASCRIPT5, false, reporter);
    Assert.assertNotNull(node);
    Assert.assertNotNull(node.getJSDocInfo());
    Assert.assertTrue(reporter.hasWarnings());
    Assert.assertTrue(reporter.warnings.get(0).contains("Non-JSDoc comment has annotations"));
  }

  @Test
  public void testVerticalTabStringLiteral() {
    String src = "var s = '\\v'; var s2 = '\u000B';";
    Node node = parseAndTransform(src);
    Assert.assertNotNull(node);
  }

  @Test
  public void testConditionalAndParenthesizedExpression() {
    String src = "var x = (a ? (b + c) : d);";
    Node node = parseAndTransform(src);
    Assert.assertNotNull(node);
  }

  @Test
  public void testIdeModePositions() {
    String src = "function foo(a, b) { return a + b; }";
    TestErrorReporter reporter = new TestErrorReporter();
    Node node = parseAndTransform(src, LanguageMode.ECMASCRIPT5, true, reporter);
    Assert.assertNotNull(node);
    Assert.assertTrue(node.getLength() >= 0);
  }

  @Test
  public void testEmptyAndSingleStatements() {
    String src = "; if (true) foo(); while(false) bar();";
    Node node = parseAndTransform(src);
    Assert.assertNotNull(node);
  }

  @Test
  public void testTryWithoutCatchWithFinally() {
    String src = "try { doSomething(); } finally { cleanUp(); }";
    Node node = parseAndTransform(src);
    Assert.assertNotNull(node);
    Node tryNode = node.getFirstChild().getFirstChild();
    Assert.assertEquals(Token.TRY, tryNode.getType());
  }

  @Test
  public void testNewExpression() {
    String src = "var obj = new MyClass(1, 2);";
    Node node = parseAndTransform(src);
    Assert.assertNotNull(node);
    Node varNode = node.getFirstChild();
    Node nameNode = varNode.getFirstChild();
    Node newTarget = nameNode.getFirstChild();
    Assert.assertEquals(Token.NEW, newTarget.getType());
  }

  @Test
  public void testAssignmentOperations() {
    String src = "a = 1; a += 2; a -= 3; a *= 4; a /= 5; a %= 6; a &= 7; a |= 8; a ^= 9; a <<= 1; a >>= 2; a >>>= 3;";
    Node node = parseAndTransform(src);
    Assert.assertNotNull(node);
  }

  @Test
  public void testBinaryAndLogicalOperations() {
    String src = "var res = (a == b) && (a != b) && (a === b) && (a !== b) && (a < b) && (a <= b) && (a > b) && (a >= b) && (a in b) && (a instanceof b) || (a | b) ^ (a & b) << 1 >> 2 >>> 3;";
    Node node = parseAndTransform(src);
    Assert.assertNotNull(node);
  }
}