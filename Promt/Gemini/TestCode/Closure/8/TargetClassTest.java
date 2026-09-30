package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Test;

public class CollapseVariableDeclarationsTest {

  private Node compileAndProcess(String js) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    Node root = compiler.parseTestCode(js);
    CollapseVariableDeclarations pass = new CollapseVariableDeclarations(compiler);
    Node externs = new Node(Token.BLOCK);
    pass.process(externs, root);
    return root;
  }

  private String processToSource(String js) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    Node root = compiler.parseTestCode(js);
    CollapseVariableDeclarations pass = new CollapseVariableDeclarations(compiler);
    Node externs = new Node(Token.BLOCK);
    pass.process(externs, root);
    return compiler.toSource(root);
  }

  @Test(expected = IllegalStateException.class)
  public void testNormalizedLifeCycleThrowsException() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);
    new CollapseVariableDeclarations(compiler);
  }

  @Test
  public void testBasicVarCollapsing() {
    String js = "var a; var b = 1; var c = 2;";
    Node root = compileAndProcess(js);
    Node script = root.getFirstChild();
    Assert.assertEquals(1, script.getChildCount());
    Node varNode = script.getFirstChild();
    Assert.assertEquals(Token.VAR, varNode.getType());
    Assert.assertEquals(3, varNode.getChildCount());
  }

  @Test
  public void testMultipleVarsCollapsed() {
    String js = "var a = 1; var b = 2; var c = 3;";
    String source = processToSource(js);
    Assert.assertTrue(source.contains("var a=1,b=2,c=3") || source.contains("var a = 1, b = 2, c = 3"));
  }

  @Test
  public void testMultipleDeclarationsPerVarNode() {
    String js = "var a = 1, b = 2; var c = 3, d = 4;";
    Node root = compileAndProcess(js);
    Node script = root.getFirstChild();
    Assert.assertEquals(1, script.getChildCount());
    Node varNode = script.getFirstChild();
    Assert.assertEquals(Token.VAR, varNode.getType());
    Assert.assertEquals(4, varNode.getChildCount());
  }

  @Test
  public void testAssignRedeclaration() {
    String js = "var a = 1; a = 2;";
    Node root = compileAndProcess(js);
    Node script = root.getFirstChild();
    Assert.assertEquals(1, script.getChildCount());
    Node varNode = script.getFirstChild();
    Assert.assertEquals(Token.VAR, varNode.getType());
    Assert.assertEquals(2, varNode.getChildCount());
    Assert.assertNotNull(varNode.getJSDocInfo());
    Assert.assertTrue(varNode.getJSDocInfo().getSuppressions().contains("duplicate"));
  }

  @Test
  public void testStubVarBlacklistingPreventsRedeclaration() {
    String js = "var a; a = 1;";
    Node root = compileAndProcess(js);
    Node script = root.getFirstChild();
    Assert.assertEquals(2, script.getChildCount());
    Assert.assertEquals(Token.VAR, script.getFirstChild().getType());
    Assert.assertEquals(Token.EXPR_RESULT, script.getLastChild().getType());
  }

  @Test
  public void testIfElseVarsNotCollapsed() {
    String js = "if (true) { var a = 1; } else { var b = 2; }";
    Node root = compileAndProcess(js);
    Node script = root.getFirstChild();
    Node ifNode = script.getFirstChild();
    Assert.assertEquals(Token.IF, ifNode.getType());
  }

  @Test
  public void testNonAssignExpressionBreaksChain() {
    String js = "var a = 1; alert(a); var b = 2;";
    Node root = compileAndProcess(js);
    Node script = root.getFirstChild();
    Assert.assertEquals(3, script.getChildCount());
    Assert.assertEquals(Token.VAR, script.getFirstChild().getType());
    Assert.assertEquals(Token.EXPR_RESULT, script.getChildAtIndex(1).getType());
    Assert.assertEquals(Token.VAR, script.getChildAtIndex(2).getType());
  }

  @Test
  public void testPropertyAssignNotRedeclared() {
    String js = "var a = {}; a.b = 1; var c = 2;";
    Node root = compileAndProcess(js);
    Node script = root.getFirstChild();
    Assert.assertEquals(3, script.getChildCount());
  }

  @Test
  public void testAssignFromOuterScopeNotRedeclared() {
    String js = "var a = 1; function f() { a = 2; var b = 3; }";
    Node root = compileAndProcess(js);
    Node script = root.getFirstChild();
    Node fn = script.getLastChild();
    Node fnBlock = fn.getLastChild();
    Assert.assertEquals(2, fnBlock.getChildCount());
    Assert.assertEquals(Token.EXPR_RESULT, fnBlock.getFirstChild().getType());
    Assert.assertEquals(Token.VAR, fnBlock.getLastChild().getType());
  }

  @Test
  public void testNoVarsNoCollapse() {
    String js = "x = 1; y = 2;";
    Node root = compileAndProcess(js);
    Node script = root.getFirstChild();
    Assert.assertEquals(2, script.getChildCount());
    Assert.assertEquals(Token.EXPR_RESULT, script.getFirstChild().getType());
    Assert.assertEquals(Token.EXPR_RESULT, script.getLastChild().getType());
  }

  @Test
  public void testEmptyScript() {
    String js = "";
    Node root = compileAndProcess(js);
    Node script = root.getFirstChild();
    Assert.assertEquals(0, script.getChildCount());
  }

  @Test
  public void testMultipleFunctionsWithCollapse() {
    String js = "function f1() { var a = 1; var b = 2; } function f2() { var c = 3; var d = 4; }";
    Node root = compileAndProcess(js);
    Node script = root.getFirstChild();
    Assert.assertEquals(2, script.getChildCount());

    Node fn1Block = script.getFirstChild().getLastChild();
    Assert.assertEquals(1, fn1Block.getChildCount());
    Assert.assertEquals(Token.VAR, fn1Block.getFirstChild().getType());

    Node fn2Block = script.getLastChild().getLastChild();
    Assert.assertEquals(1, fn2Block.getChildCount());
    Assert.assertEquals(Token.VAR, fn2Block.getFirstChild().getType());
  }

  @Test
  public void testVarInsideBlock() {
    String js = "{ var a = 1; var b = 2; }";
    Node root = compileAndProcess(js);
    Node script = root.getFirstChild();
    Node block = script.getFirstChild();
    Assert.assertEquals(1, block.getChildCount());
    Assert.assertEquals(Token.VAR, block.getFirstChild().getType());
    Assert.assertEquals(2, block.getFirstChild().getChildCount());
  }
}