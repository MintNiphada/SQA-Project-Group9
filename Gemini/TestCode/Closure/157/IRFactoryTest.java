package com.google.javascript.jscomp.parsing;

import com.google.common.collect.ImmutableList;
import com.google.javascript.jscomp.mozilla.rhino.CompilerEnvirons;
import com.google.javascript.jscomp.mozilla.rhino.ErrorReporter;
import com.google.javascript.jscomp.mozilla.rhino.EvaluatorException;
import com.google.javascript.jscomp.mozilla.rhino.Parser;
import com.google.javascript.jscomp.mozilla.rhino.Token.CommentType;
import com.google.javascript.jscomp.mozilla.rhino.ast.ArrayLiteral;
import com.google.javascript.jscomp.mozilla.rhino.ast.Assignment;
import com.google.javascript.jscomp.mozilla.rhino.ast.AstNode;
import com.google.javascript.jscomp.mozilla.rhino.ast.AstRoot;
import com.google.javascript.jscomp.mozilla.rhino.ast.Block;
import com.google.javascript.jscomp.mozilla.rhino.ast.BreakStatement;
import com.google.javascript.jscomp.mozilla.rhino.ast.CatchClause;
import com.google.javascript.jscomp.mozilla.rhino.ast.Comment;
import com.google.javascript.jscomp.mozilla.rhino.ast.ConditionalExpression;
import com.google.javascript.jscomp.mozilla.rhino.ast.ContinueStatement;
import com.google.javascript.jscomp.mozilla.rhino.ast.DoLoop;
import com.google.javascript.jscomp.mozilla.rhino.ast.ElementGet;
import com.google.javascript.jscomp.mozilla.rhino.ast.EmptyExpression;
import com.google.javascript.jscomp.mozilla.rhino.ast.ExpressionStatement;
import com.google.javascript.jscomp.mozilla.rhino.ast.ForInLoop;
import com.google.javascript.jscomp.mozilla.rhino.ast.ForLoop;
import com.google.javascript.jscomp.mozilla.rhino.ast.FunctionCall;
import com.google.javascript.jscomp.mozilla.rhino.ast.FunctionNode;
import com.google.javascript.jscomp.mozilla.rhino.ast.IfStatement;
import com.google.javascript.jscomp.mozilla.rhino.ast.InfixExpression;
import com.google.javascript.jscomp.mozilla.rhino.ast.KeywordLiteral;
import com.google.javascript.jscomp.mozilla.rhino.ast.Label;
import com.google.javascript.jscomp.mozilla.rhino.ast.LabeledStatement;
import com.google.javascript.jscomp.mozilla.rhino.ast.Name;
import com.google.javascript.jscomp.mozilla.rhino.ast.NewExpression;
import com.google.javascript.jscomp.mozilla.rhino.ast.NumberLiteral;
import com.google.javascript.jscomp.mozilla.rhino.ast.ObjectLiteral;
import com.google.javascript.jscomp.mozilla.rhino.ast.ObjectProperty;
import com.google.javascript.jscomp.mozilla.rhino.ast.ParenthesizedExpression;
import com.google.javascript.jscomp.mozilla.rhino.ast.PropertyGet;
import com.google.javascript.jscomp.mozilla.rhino.ast.RegExpLiteral;
import com.google.javascript.jscomp.mozilla.rhino.ast.ReturnStatement;
import com.google.javascript.jscomp.mozilla.rhino.ast.Scope;
import com.google.javascript.jscomp.mozilla.rhino.ast.StringLiteral;
import com.google.javascript.jscomp.mozilla.rhino.ast.SwitchCase;
import com.google.javascript.jscomp.mozilla.rhino.ast.SwitchStatement;
import com.google.javascript.jscomp.mozilla.rhino.ast.ThrowStatement;
import com.google.javascript.jscomp.mozilla.rhino.ast.TryStatement;
import com.google.javascript.jscomp.mozilla.rhino.ast.UnaryExpression;
import com.google.javascript.jscomp.mozilla.rhino.ast.VariableDeclaration;
import com.google.javascript.jscomp.mozilla.rhino.ast.VariableInitializer;
import com.google.javascript.jscomp.mozilla.rhino.ast.WhileLoop;
import com.google.javascript.jscomp.mozilla.rhino.ast.WithStatement;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class IRFactoryTest {

  private static class TestErrorReporter implements ErrorReporter {
    final List<String> errors = new ArrayList<String>();
    final List<String> warnings = new ArrayList<String>();

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
  }

  private TestErrorReporter errorReporter;

  @Before
  public void setUp() {
    errorReporter = new TestErrorReporter();
  }

  private AstRoot parseRhino(String source, Config.LanguageMode mode, boolean acceptConst) {
    CompilerEnvirons env = new CompilerEnvirons();
    env.setRecordingComments(true);
    env.setRecordingLocalJsDocComments(true);
    env.setLanguageVersion(com.google.javascript.jscomp.mozilla.rhino.Context.VERSION_1_8);
    Parser p = new Parser(env);
    return p.parse(source, "test.js", 1);
  }

  private Node testTransform(String source, Config.LanguageMode mode, boolean acceptConst) {
    AstRoot root = parseRhino(source, mode, acceptConst);
    Config config = new Config(new HashSet<String>(), Collections.<String>emptySet(), true, mode, acceptConst);
    return IRFactory.transformTree(root, source, config, errorReporter);
  }

  private Node testTransform(String source) {
    return testTransform(source, Config.LanguageMode.ECMASCRIPT5, true);
  }

  @Test
  public void testSimpleExpressions() {
    Node node = testTransform("var a = 1 + 2 * 3;");
    Assert.assertEquals(Token.SCRIPT, node.getType());
    Assert.assertEquals(Token.VAR, node.getFirstChild().getType());
  }

  @Test
  public void testDirectives() {
    Node node = testTransform("'use strict'; var x = 1;");
    Assert.assertNotNull(node.getDirectives());
    Assert.assertTrue(node.getDirectives().contains("use strict"));
    Assert.assertEquals(Token.VAR, node.getFirstChild().getType());
  }

  @Test
  public void testFunctionWithDirectives() {
    Node node = testTransform("function f() { 'use strict'; return 1; }");
    Node fn = node.getFirstChild();
    Assert.assertEquals(Token.FUNCTION, fn.getType());
    Node body = fn.getFirstChild().getNext().getNext();
    Assert.assertNotNull(body.getDirectives());
    Assert.assertTrue(body.getDirectives().contains("use strict"));
  }

  @Test
  public void testUnnamedFunction() {
    Node node = testTransform("(function() {})();");
    Assert.assertEquals(Token.SCRIPT, node.getType());
    Assert.assertEquals(0, errorReporter.errors.size());
  }

  @Test
  public void testFunctionCallAndNew() {
    Node node = testTransform("foo(1, 2); new Bar(3);");
    Node call = node.getFirstChild().getFirstChild();
    Node newExpr = node.getFirstChild().getNext().getFirstChild();
    Assert.assertEquals(Token.CALL, call.getType());
    Assert.assertEquals(Token.NEW, newExpr.getType());
  }

  @Test
  public void testUnaryExpressions() {
    Node node = testTransform("var a = -5; var b = !true; var c = ~1; var d = +2; var e = typeof a; var f = void 0;");
    Assert.assertEquals(Token.SCRIPT, node.getType());
    Node varA = node.getFirstChild();
    Node numA = varA.getFirstChild().getFirstChild();
    Assert.assertEquals(Token.NUMBER, numA.getType());
    Assert.assertEquals(-5.0, numA.getDouble(), 0.0);
  }

  @Test
  public void testUnaryIncDec() {
    Node node = testTransform("a++; ++a; a--; --a;");
    Assert.assertEquals(0, errorReporter.errors.size());
  }

  @Test
  public void testInvalidIncDecTarget() {
    testTransform("5++;");
    Assert.assertTrue(errorReporter.errors.size() > 0);
  }

  @Test
  public void testInvalidDecTarget() {
    testTransform("--5;");
    Assert.assertTrue(errorReporter.errors.size() > 0);
  }

  @Test
  public void testControlStructures() {
    String src = "if (true) { a = 1; } else { a = 2; }\n"
        + "while (false) { break; }\n"
        + "do { continue; } while (false);\n"
        + "for (var i = 0; i < 10; i++) {}\n"
        + "for (var k in obj) {}\n"
        + "with (obj) { a = 1; }\n"
        + "try { throw 1; } catch (e) { a = 2; } finally { a = 3; }";
    Node node = testTransform(src);
    Assert.assertEquals(Token.SCRIPT, node.getType());
    Assert.assertEquals(0, errorReporter.errors.size());
  }

  @Test
  public void testTryWithoutCatch() {
    Node node = testTransform("try { a = 1; } finally { a = 2; }");
    Assert.assertEquals(Token.SCRIPT, node.getType());
    Assert.assertEquals(0, errorReporter.errors.size());
  }

  @Test
  public void testLabelsAndBreakContinue() {
    String src = "lbl: while(true) { break lbl; continue lbl; }";
    Node node = testTransform(src);
    Assert.assertEquals(Token.SCRIPT, node.getType());
    Assert.assertEquals(0, errorReporter.errors.size());
  }

  @Test
  public void testSwitchStatement() {
    String src = "switch(x) { case 1: a=1; break; default: a=2; }";
    Node node = testTransform(src);
    Assert.assertEquals(Token.SCRIPT, node.getType());
    Assert.assertEquals(0, errorReporter.errors.size());
  }

  @Test
  public void testObjectLiteral() {
    String src = "var obj = { a: 1, 'b': 2, 3: 4, get c() { return 1; }, set c(val) { this.x = val; } };";
    Node node = testTransform(src, Config.LanguageMode.ECMASCRIPT5, true);
    Assert.assertEquals(Token.SCRIPT, node.getType());
    Assert.assertEquals(0, errorReporter.errors.size());
  }

  @Test
  public void testObjectLiteralES3GetSetWarning() {
    String src = "var obj = { get c() { return 1; }, set c(val) { } };";
    testTransform(src, Config.LanguageMode.ECMASCRIPT3, true);
    Assert.assertEquals(2, errorReporter.errors.size());
  }

  @Test
  public void testObjectLiteralGetterParamError() {
    AstRoot root = new AstRoot();
    ObjectLiteral obj = new ObjectLiteral();
    ObjectProperty prop = new ObjectProperty();
    prop.setIsGetter();
    prop.setLeft(new Name(0, "g"));
    FunctionNode fn = new FunctionNode();
    fn.setParams(ImmutableList.<AstNode>of(new Name(0, "param1")));
    fn.setBody(new Block());
    prop.setRight(fn);
    obj.addElement(prop);
    root.addChild(new ExpressionStatement(obj));
    Config config = new Config(new HashSet<String>(), Collections.<String>emptySet(), true, Config.LanguageMode.ECMASCRIPT5, true);
    IRFactory.transformTree(root, "", config, errorReporter);
    Assert.assertEquals(1, errorReporter.errors.size());
  }

  @Test
  public void testObjectLiteralSetterParamError() {
    AstRoot root = new AstRoot();
    ObjectLiteral obj = new ObjectLiteral();
    ObjectProperty prop = new ObjectProperty();
    prop.setIsSetter();
    prop.setLeft(new Name(0, "s"));
    FunctionNode fn = new FunctionNode();
    fn.setParams(Collections.<AstNode>emptyList());
    fn.setBody(new Block());
    prop.setRight(fn);
    obj.addElement(prop);
    root.addChild(new ExpressionStatement(obj));
    Config config = new Config(new HashSet<String>(), Collections.<String>emptySet(), true, Config.LanguageMode.ECMASCRIPT5, true);
    IRFactory.transformTree(root, "", config, errorReporter);
    Assert.assertEquals(1, errorReporter.errors.size());
  }

  @Test
  public void testArrayLiteralAndDestructuring() {
    String src = "var arr = [1, 2, 3];";
    Node node = testTransform(src);
    Assert.assertEquals(Token.SCRIPT, node.getType());
  }

  @Test
  public void testDestructuringAssignmentReport() {
    AstRoot root = new AstRoot();
    ArrayLiteral arr = new ArrayLiteral();
    arr.setIsDestructuring(true);
    root.addChild(new ExpressionStatement(arr));
    ObjectLiteral obj = new ObjectLiteral();
    obj.setIsDestructuring(true);
    root.addChild(new ExpressionStatement(obj));
    Config config = new Config(new HashSet<String>(), Collections.<String>emptySet(), true, Config.LanguageMode.ECMASCRIPT5, true);
    IRFactory.transformTree(root, "", config, errorReporter);
    Assert.assertEquals(2, errorReporter.errors.size());
  }

  @Test
  public void testInvalidAssignmentTarget() {
    testTransform("1 = 2;");
    Assert.assertTrue(errorReporter.errors.size() > 0);
  }

  @Test
  public void testReservedKeywordsInES5Strict() {
    testTransform("var let = 1;", Config.LanguageMode.ECMASCRIPT5_STRICT, true);
    Assert.assertTrue(errorReporter.errors.size() > 0);
  }

  @Test
  public void testReservedKeywordsInES5() {
    testTransform("var class = 1;", Config.LanguageMode.ECMASCRIPT5, true);
    Assert.assertTrue(errorReporter.errors.size() > 0);
  }

  @Test
  public void testConstKeywordDisabled() {
    AstRoot root = new AstRoot();
    VariableDeclaration decl = new VariableDeclaration();
    decl.setType(com.google.javascript.jscomp.mozilla.rhino.Token.CONST);
    decl.addVariable(new VariableInitializer());
    root.addChild(decl);
    Config config = new Config(new HashSet<String>(), Collections.<String>emptySet(), true, Config.LanguageMode.ECMASCRIPT5, false);
    IRFactory.transformTree(root, "", config, errorReporter);
    Assert.assertTrue(errorReporter.errors.size() > 0);
  }

  @Test
  public void testRegExpLiteral() {
    Node node = testTransform("var re = /abc/g;");
    Assert.assertEquals(Token.SCRIPT, node.getType());
  }

  @Test
  public void testConditionalExpression() {
    Node node = testTransform("var x = a ? b : c;");
    Assert.assertEquals(Token.SCRIPT, node.getType());
  }

  @Test
  public void testElementGetAndPropertyGet() {
    Node node = testTransform("var x = a.b; var y = a[b];");
    Assert.assertEquals(Token.SCRIPT, node.getType());
  }

  @Test
  public void testParenthesizedAndEmpty() {
    Node node = testTransform("var x = (1); ;");
    Assert.assertEquals(Token.SCRIPT, node.getType());
  }

  @Test
  public void testJSDocParsingAndFileOverview() {
    String src = "/** @fileoverview Test overview \n * @license MIT */\n/** @param {number} x */ function f(x) {}";
    Node node = testTransform(src);
    Assert.assertNotNull(node.getJSDocInfo());
    Node fn = node.getFirstChild();
    Assert.assertNotNull(fn.getJSDocInfo());
  }

  @Test
  public void testCommentsWithoutFileOverview() {
    AstRoot root = new AstRoot();
    Comment c = new Comment(0, 15, CommentType.JSDOC, "/** Regular comment */");
    root.addComment(c);
    Config config = new Config(new HashSet<String>(), Collections.<String>emptySet(), true, Config.LanguageMode.ECMASCRIPT5, true);
    Node node = IRFactory.transformTree(root, "/** Regular comment */", config, errorReporter);
    Assert.assertNotNull(node);
  }

  @Test
  public void testCatchConditionUnsupported() {
    AstRoot root = new AstRoot();
    TryStatement tryStmt = new TryStatement();
    tryStmt.setTryBlock(new Block());
    CatchClause catchClause = new CatchClause();
    catchClause.setVarName(new Name(0, "e"));
    catchClause.setCatchCondition(new Name(0, "cond"));
    catchClause.setBody(new Block());
    tryStmt.addCatchClause(catchClause);
    root.addChild(tryStmt);

    Config config = new Config(new HashSet<String>(), Collections.<String>emptySet(), true, Config.LanguageMode.ECMASCRIPT5, true);
    IRFactory.transformTree(root, "", config, errorReporter);
    Assert.assertEquals(1, errorReporter.errors.size());
  }

  @Test
  public void testNestedLabels() {
    String src = "lbl1: lbl2: while(true) {}";
    Node node = testTransform(src);
    Assert.assertEquals(Token.LABEL, node.getFirstChild().getType());
  }

  @Test
  public void testScopeAndBlockTransformation() {
    AstRoot root = new AstRoot();
    Scope scope = new Scope();
    scope.addChild(new EmptyExpression());
    root.addChild(scope);
    Config config = new Config(new HashSet<String>(), Collections.<String>emptySet(), true, Config.LanguageMode.ECMASCRIPT5, true);
    Node node = IRFactory.transformTree(root, "", config, errorReporter);
    Assert.assertEquals(Token.SCRIPT, node.getType());
  }
}
