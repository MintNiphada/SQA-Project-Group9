package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

class FlowSensitiveInlineVariablesTest extends CompilerTestCase {

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected CompilerPass getProcessor(final Compiler compiler) {
    return new FlowSensitiveInlineVariables(compiler);
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  @Test
  public void testSimpleVarInline() {
    test("function f() { var x = 1; return x; }",
         "function f() { var x; return 1; }");
  }

  @Test
  public void testSimpleAssignInline() {
    test("function f() { var x; x = 1; return x; }",
         "function f() { var x; return 1; }");
  }

  @Test
  public void testLabeledAssignInline() {
    test("function f() { var x; L: x = 1; return x; }",
         "function f() { var x; return 1; }");
    test("function f() { var x; L1: L2: x = 1; return x; }",
         "function f() { var x; return 1; }");
  }

  @Test
  public void testGlobalScopeNoInline() {
    testSame("var x = 1; var y = x;");
  }

  @Test
  public void testMultipleUsesNoInline() {
    testSame("function f() { var x = 1; return x + x; }");
  }

  @Test
  public void testUseWithinLoopNoInline() {
    testSame("function f() { var x = 1; while (true) { return x; } }");
    testSame("function f() { var x = 1; for (;;) { return x; } }");
    testSame("function f() { var x = 1; do { return x; } while (true); }");
  }

  @Test
  public void testAssignUsedAsRValueNoInline() {
    testSame("function f() { var x; var y = (x = 1); return x; }");
  }

  @Test
  public void testGetPropNoInline() {
    testSame("function f() { var x = a.b; return x; }");
  }

  @Test
  public void testGetElemNoInline() {
    testSame("function f() { var x = a[0]; return x; }");
  }

  @Test
  public void testArrayLiteralNoInline() {
    testSame("function f() { var x = [1, 2]; return x; }");
  }

  @Test
  public void testObjectLiteralNoInline() {
    testSame("function f() { var x = {a: 1}; return x; }");
  }

  @Test
  public void testRegExpLiteralNoInline() {
    testSame("function f() { var x = /abc/; return x; }");
  }

  @Test
  public void testNewNoInline() {
    testSame("function f() { var x = new Foo(); return x; }");
  }

  @Test
  public void testSideEffectBetweenDefAndUseNoInline() {
    testSame("function f() { var x = 1; g(); return x; }");
    testSame("function f() { var x = 1; new g(); return x; }");
  }

  @Test
  public void testSideEffectInDefRhsNoInline() {
    testSame("function f() { var x = g(); return x; }");
  }

  @Test
  public void testSideEffectOnRightOfDef() {
    testSame("function f() { var x; (x = 1), g(); return x; }");
  }

  @Test
  public void testSideEffectOnLeftOfUse() {
    testSame("function f() { var x = 1; return g(), x; }");
  }

  @Test
  public void testFunctionParameterNoInline() {
    testSame("function f(x) { return x; }");
  }

  @Test
  public void testReassignedVarNoInline() {
    testSame("function f() { var x = 1; x = 2; return x; }");
  }

  @Test
  public void testDependentOnOuterScopeVarsNoInline() {
    testSame("var y = 1; function f() { var x = y; y = 2; return x; }");
  }

  @Test
  public void testUnaryExpressionsOnUse() {
    testSame("function f() { var x = 1; x++; return x; }");
    testSame("function f() { var x = 1; x--; return x; }");
  }

  @Test
  public void testCatchVariableNoInline() {
    testSame("function f() { try {} catch (e) { return e; } }");
  }

  @Test
  public void testExportedVariablesNoInline() {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    FlowSensitiveInlineVariables pass = new FlowSensitiveInlineVariables(compiler);
    assertNotNull(pass);
  }

  @Test
  public void testDirectPassMethods() {
    Compiler compiler = new Compiler();
    FlowSensitiveInlineVariables pass = new FlowSensitiveInlineVariables(compiler);
    Node root = compiler.parseTestCode("function f() { var x = 1; return x; }");
    pass.process(null, root);
    pass.visit(null, root, null);
    pass.exitScope(null);
    assertTrue(root.hasChildren());
  }

  @Test
  public void testChainedInlining() {
    test("function f() { var x = 1; var y = x; return y; }",
         "function f() { var x = 1; var y; return x; }");
  }

  @Test
  public void testExpressionInlining() {
    test("function f() { var x = 1 + 2; return x; }",
         "function f() { var x; return 1 + 2; }");
  }

  @Test
  public void testInlineInConditional() {
    test("function f() { var x = 1; if (true) { return x; } }",
         "function f() { var x; if (true) { return 1; } }");
  }

  @Test
  public void testNoInlineAcrossBranchesWithSideEffects() {
    testSame("function f() { var x = 1; if (cond) { g(); } return x; }");
  }

  @Test
  public void testDoNotInlineWhenUsedInAssignLhs() {
    testSame("function f() { var x = 1; var y; y = x = 2; return x; }");
  }
}