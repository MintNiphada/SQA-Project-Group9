package com.google.javascript.jscomp;

import com.google.javascript.jscomp.AbstractCompiler.LifeCycleStage;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import org.junit.Assert;
import org.junit.Test;

public class TargetClassTest {

  private Node runPass(
      String js,
      boolean removeGlobals,
      boolean preserveFunctionExpressionNames,
      boolean modifyCallSites) {
    Compiler compiler = new Compiler();
    Node externs = IR.block();
    Node root = compiler.parseTestCode(js);
    compiler.setLifeCycleStage(LifeCycleStage.NORMALIZED);
    RemoveUnusedVars pass =
        new RemoveUnusedVars(
            compiler,
            removeGlobals,
            preserveFunctionExpressionNames,
            modifyCallSites);
    pass.process(externs, root);
    return root;
  }

  private String toSource(Node root) {
    Compiler compiler = new Compiler();
    return compiler.toSource(root);
  }

  @Test
  public void testRemoveUnusedSimpleVar() {
    String js = "var unused = 1;";
    Node root = runPass(js, true, false, false);
    String result = toSource(root);
    Assert.assertFalse(result.contains("unused"));
  }

  @Test
  public void testKeepUsedVar() {
    String js = "var used = 1; alert(used);";
    Node root = runPass(js, true, false, false);
    String result = toSource(root);
    Assert.assertTrue(result.contains("used"));
    Assert.assertTrue(result.contains("alert"));
  }

  @Test
  public void testDoNotRemoveGlobalsWhenFlagFalse() {
    String js = "var globalVar = 1;";
    Node root = runPass(js, false, false, false);
    String result = toSource(root);
    Assert.assertTrue(result.contains("globalVar"));
  }

  @Test
  public void testRemoveUnusedFunctionDeclaration() {
    String js = "function unusedFn() { return 42; }";
    Node root = runPass(js, true, false, false);
    String result = toSource(root);
    Assert.assertFalse(result.contains("unusedFn"));
  }

  @Test
  public void testKeepUsedFunctionDeclaration() {
    String js = "function usedFn() { return 42; } usedFn();";
    Node root = runPass(js, true, false, false);
    String result = toSource(root);
    Assert.assertTrue(result.contains("usedFn"));
  }

  @Test
  public void testRemoveSideEffectFreeAssigns() {
    String js = "var x = 1; x = 2;";
    Node root = runPass(js, true, false, false);
    String result = toSource(root);
    Assert.assertFalse(result.contains("x"));
  }

  @Test
  public void testKeepVarWithSideEffectsInInitialValue() {
    String js = "var a = externalSideEffect();";
    Node root = runPass(js, true, false, false);
    String result = toSource(root);
    Assert.assertFalse(result.contains("var a"));
    Assert.assertTrue(result.contains("externalSideEffect()"));
  }

  @Test
  public void testRemoveOneOfMultipleDeclarations() {
    String js = "var a = 1, b = 2; alert(a);";
    Node root = runPass(js, true, false, false);
    String result = toSource(root);
    Assert.assertTrue(result.contains("a"));
    Assert.assertFalse(result.contains("b"));
  }

  @Test
  public void testRemoveUnusedFunctionArgs() {
    String js = "function f(a, b) { return a; } f(1, 2);";
    Node root = runPass(js, true, false, false);
    String result = toSource(root);
    Assert.assertTrue(result.contains("function f(a)"));
  }

  @Test
  public void testArgumentsEscapedPreservesParams() {
    String js = "function f(a, b) { return arguments[0]; } f(1, 2);";
    Node root = runPass(js, true, false, false);
    String result = toSource(root);
    Assert.assertTrue(result.contains("a, b") || result.contains("a,b"));
  }

  @Test
  public void testPreserveFunctionExpressionNames() {
    String js = "var f = function myNamedFn() { return 1; }; f();";
    Node rootKeep = runPass(js, true, true, false);
    String resultKeep = toSource(rootKeep);
    Assert.assertTrue(resultKeep.contains("myNamedFn"));

    Node rootRemove = runPass(js, true, false, false);
    String resultRemove = toSource(rootRemove);
    Assert.assertFalse(resultRemove.contains("myNamedFn"));
  }

  @Test
  public void testPropertyAssignsOnUnreferencedVar() {
    String js = "var obj = {}; obj.foo = 1; obj.bar = 2;";
    Node root = runPass(js, true, false, false);
    String result = toSource(root);
    Assert.assertFalse(result.contains("obj"));
  }

  @Test
  public void testPropertyAssignOnUnknownValueKeepsVar() {
    String js = "function test(param) { param.foo = 1; } test({});";
    Node root = runPass(js, true, false, false);
    String result = toSource(root);
    Assert.assertTrue(result.contains("foo"));
  }

  @Test
  public void testForInLoopsPreserved() {
    String js = "var obj = {a: 1}; for (var k in obj) { alert(k); }";
    Node root = runPass(js, true, false, false);
    String result = toSource(root);
    Assert.assertTrue(result.contains("for"));
    Assert.assertTrue(result.contains("k"));
  }

  @Test
  public void testModifyCallSites() {
    String js = "function f(a, b) { return a; } f(1, 2);";
    Node root = runPass(js, true, false, true);
    String result = toSource(root);
    Assert.assertFalse(result.contains("2"));
  }

  @Test
  public void testModifyCallSitesWithMultipleCallers() {
    String js = "function f(a, b, c) { return a; } f(1, 2, 3); f(4, 5, 6);";
    Node root = runPass(js, true, false, true);
    String result = toSource(root);
    Assert.assertTrue(result.contains("f(1)"));
    Assert.assertTrue(result.contains("f(4)"));
  }

  @Test
  public void testModifyCallSitesWithSideEffectArg() {
    String js = "function f(a, b) { return a; } f(1, sideEffect());";
    Node root = runPass(js, true, false, true);
    String result = toSource(root);
    Assert.assertTrue(result.contains("sideEffect()"));
  }

  @Test
  public void testObjectLiteralSetterGetterParameters() {
    String js = "var x = { set a(val) { this._a = val; }, get a() { return this._a; } }; alert(x.a);";
    Node root = runPass(js, true, false, false);
    String result = toSource(root);
    Assert.assertTrue(result.contains("set a(val)"));
  }

  @Test
  public void testProcessWithExplicitDefinitionFinder() {
    Compiler compiler = new Compiler();
    Node externs = IR.block();
    Node root = compiler.parseTestCode("function f(x) { return 1; } f(2);");
    compiler.setLifeCycleStage(LifeCycleStage.NORMALIZED);
    SimpleDefinitionFinder defFinder = new SimpleDefinitionFinder(compiler);
    defFinder.process(externs, root);

    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, true, false, true);
    pass.process(externs, root, defFinder);
    String result = toSource(root);
    Assert.assertTrue(result.contains("f()"));
  }

  @Test(expected = IllegalStateException.class)
  public void testProcessThrowsIfNotNormalized() {
    Compiler compiler = new Compiler();
    Node externs = IR.block();
    Node root = compiler.parseTestCode("var a = 1;");
    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, true, false, false);
    pass.process(externs, root);
  }
}