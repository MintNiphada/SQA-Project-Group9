package com.google.javascript.jscomp;

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Test;

class RemoveUnusedVarsTest {

  private Compiler createNormalizedCompiler() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);
    return compiler;
  }

  private Node parse(Compiler compiler, String js) {
    Node root = compiler.parseTestCode(js);
    return root;
  }

  @Test(expected = IllegalStateException.class)
  public void testUnnormalizedCompilerThrows() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    Node externs = IR.block();
    Node root = parse(compiler, "var a = 1;");
    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, true, false, false);
    pass.process(externs, root);
  }

  @Test
  public void testRemoveUnusedGlobalVar() {
    Compiler compiler = createNormalizedCompiler();
    Node externs = IR.block();
    Node root = parse(compiler, "var unused = 10; var used = 20; alert(used);");

    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, true, false, false);
    pass.process(externs, root);

    String result = compiler.toSource(root);
    Assert.assertFalse(result.contains("unused"));
    Assert.assertTrue(result.contains("used"));
    Assert.assertTrue(result.contains("alert"));
  }

  @Test
  public void testPreserveUnusedGlobalVarWhenRemoveGlobalsFalse() {
    Compiler compiler = createNormalizedCompiler();
    Node externs = IR.block();
    Node root = parse(compiler, "var unused = 10;");

    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, false, false, false);
    pass.process(externs, root);

    String result = compiler.toSource(root);
    Assert.assertTrue(result.contains("unused"));
  }

  @Test
  public void testRemoveUnusedLocalVar() {
    Compiler compiler = createNormalizedCompiler();
    Node externs = IR.block();
    Node root = parse(compiler, "function foo() { var unused = 1; var used = 2; return used; } foo();");

    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, false, false, false);
    pass.process(externs, root);

    String result = compiler.toSource(root);
    Assert.assertFalse(result.contains("unused"));
    Assert.assertTrue(result.contains("used"));
  }

  @Test
  public void testRemoveUnusedFunctionDeclaration() {
    Compiler compiler = createNormalizedCompiler();
    Node externs = IR.block();
    Node root = parse(compiler, "function unusedFn() { return 1; } function usedFn() { return 2; } usedFn();");

    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, true, false, false);
    pass.process(externs, root);

    String result = compiler.toSource(root);
    Assert.assertFalse(result.contains("unusedFn"));
    Assert.assertTrue(result.contains("usedFn"));
  }

  @Test
  public void testPreserveFunctionExpressionNames() {
    Compiler compiler = createNormalizedCompiler();
    Node externs = IR.block();
    Node root = parse(compiler, "var f = function named() { return 1; }; f();");

    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, false, true, false);
    pass.process(externs, root);

    String result = compiler.toSource(root);
    Assert.assertTrue(result.contains("named"));
  }

  @Test
  public void testRemoveFunctionExpressionNamesWhenNotPreserved() {
    Compiler compiler = createNormalizedCompiler();
    Node externs = IR.block();
    Node root = parse(compiler, "var f = function named() { return 1; }; f();");

    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, false, false, false);
    pass.process(externs, root);

    String result = compiler.toSource(root);
    Assert.assertFalse(result.contains("named"));
  }

  @Test
  public void testSideEffectAssignmentKeepsExpression() {
    Compiler compiler = createNormalizedCompiler();
    Node externs = IR.block();
    Node root = parse(compiler, "function sideEffect() { alert(1); return 2; } var unused = sideEffect();");

    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, true, false, false);
    pass.process(externs, root);

    String result = compiler.toSource(root);
    Assert.assertFalse(result.contains("unused"));
    Assert.assertTrue(result.contains("sideEffect()"));
  }

  @Test
  public void testMultipleVarDeclarations() {
    Compiler compiler = createNormalizedCompiler();
    Node externs = IR.block();
    Node root = parse(compiler, "var a = 1, b = 2, c = 3; alert(b);");

    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, true, false, false);
    pass.process(externs, root);

    String result = compiler.toSource(root);
    Assert.assertFalse(result.contains("var a"));
    Assert.assertTrue(result.contains("b"));
    Assert.assertFalse(result.contains("c = 3"));
  }

  @Test
  public void testArgumentsEscapedMarksAllParamsReferenced() {
    Compiler compiler = createNormalizedCompiler();
    Node externs = IR.block();
    Node root = parse(compiler, "function foo(a, b, c) { return arguments[0]; } foo(1, 2, 3);");

    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, false, false, false);
    pass.process(externs, root);

    String result = compiler.toSource(root);
    Assert.assertTrue(result.contains("a"));
    Assert.assertTrue(result.contains("b"));
    Assert.assertTrue(result.contains("c"));
  }

  @Test
  public void testRemoveUnreferencedFunctionArgsAtEnd() {
    Compiler compiler = createNormalizedCompiler();
    Node externs = IR.block();
    Node root = parse(compiler, "function foo(a, b, c) { return a; } foo(1, 2, 3);");

    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, false, false, false);
    pass.process(externs, root);

    String result = compiler.toSource(root);
    Assert.assertTrue(result.contains("a"));
    Assert.assertFalse(result.contains("b"));
    Assert.assertFalse(result.contains("c"));
  }

  @Test
  public void testPropertyAssignsInterpreted() {
    Compiler compiler = createNormalizedCompiler();
    Node externs = IR.block();
    Node root = parse(compiler, "var x = {}; x.a = 1;");

    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, true, false, false);
    pass.process(externs, root);

    String result = compiler.toSource(root);
    Assert.assertEquals("", result.trim());
  }

  @Test
  public void testPropertyAssignsKeptIfAliasedOrUnknown() {
    Compiler compiler = createNormalizedCompiler();
    Node externs = IR.block();
    Node root = parse(compiler, "function getObj() { return {}; } var x = getObj(); x.a = 1;");

    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, true, false, false);
    pass.process(externs, root);

    String result = compiler.toSource(root);
    Assert.assertTrue(result.contains("getObj()"));
  }

  @Test
  public void testPrototypeAssigns() {
    Compiler compiler = createNormalizedCompiler();
    Node externs = IR.block();
    Node root = parse(compiler, "function Foo() {} Foo.prototype.bar = function() { return 1; };");

    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, true, false, false);
    pass.process(externs, root);

    String result = compiler.toSource(root);
    Assert.assertEquals("", result.trim());
  }

  @Test
  public void testCallSiteOptimizerBasic() {
    Compiler compiler = createNormalizedCompiler();
    Node externs = IR.block();
    Node root = parse(compiler, "function f(a, b) { return a; } f(1, 2);");

    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, true, false, true);
    pass.process(externs, root);

    String result = compiler.toSource(root);
    Assert.assertTrue(result.contains("f(1)"));
  }

  @Test
  public void testCallSiteOptimizerReplaceWithZeroForMiddleArg() {
    Compiler compiler = createNormalizedCompiler();
    Node externs = IR.block();
    Node root = parse(compiler, "function f(a, b, c) { return a + c; } f(1, 2, 3);");

    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, true, false, true);
    pass.process(externs, root);

    String result = compiler.toSource(root);
    Assert.assertTrue(result.contains("f(1, 0, 3)"));
  }

  @Test
  public void testForInLoopVariablePreserved() {
    Compiler compiler = createNormalizedCompiler();
    Node externs = IR.block();
    Node root = parse(compiler, "var obj = {a: 1}; for (var k in obj) { alert(1); }");

    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, true, false, false);
    pass.process(externs, root);

    String result = compiler.toSource(root);
    Assert.assertTrue(result.contains("for(var k in obj)"));
  }

  @Test
  public void testInheritanceCallRemovedWhenSubclassUnreferenced() {
    Compiler compiler = createNormalizedCompiler();
    Node externs = IR.block();
    Node root = parse(compiler,
        "function goog() {} goog.inherits = function(child, parent) {};\n" +
        "function Super() {}\n" +
        "function Sub() {}\n" +
        "goog.inherits(Sub, Super);\n");

    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, true, false, false);
    pass.process(externs, root);

    String result = compiler.toSource(root);
    Assert.assertFalse(result.contains("Sub"));
  }

  @Test
  public void testInheritanceCallPreservedWhenSubclassReferenced() {
    Compiler compiler = createNormalizedCompiler();
    Node externs = IR.block();
    Node root = parse(compiler,
        "var goog = {}; goog.inherits = function(child, parent) {};\n" +
        "function Super() {}\n" +
        "function Sub() {}\n" +
        "goog.inherits(Sub, Super);\n" +
        "new Sub();");

    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, true, false, false);
    pass.process(externs, root);

    String result = compiler.toSource(root);
    Assert.assertTrue(result.contains("Sub"));
    Assert.assertTrue(result.contains("goog.inherits(Sub, Super)"));
  }

  @Test
  public void testObjectLiteralSetterParameterNotRemoved() {
    Compiler compiler = createNormalizedCompiler();
    Node externs = IR.block();
    Node root = parse(compiler, "var obj = { set a(val) { alert(1); } }; alert(obj);");

    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, false, false, false);
    pass.process(externs, root);

    String result = compiler.toSource(root);
    Assert.assertTrue(result.contains("set a(val)"));
  }

  @Test
  public void testAssignWithGetElemRemoval() {
    Compiler compiler = createNormalizedCompiler();
    Node externs = IR.block();
    Node root = parse(compiler, "function getIdx() { return 0; } var x = []; x[getIdx()] = 1;");

    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, true, false, false);
    pass.process(externs, root);

    String result = compiler.toSource(root);
    Assert.assertTrue(result.contains("getIdx()"));
    Assert.assertFalse(result.contains("x = []"));
  }
}