package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;

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
  public void testSimpleInlineVar() {
    test("function f() { var x = 1; return x; }",
         "function f() { return 1; }");
  }

  @Test
  public void testSimpleInlineAssign() {
    test("function f() { var x; x = 1; return x; }",
         "function f() { var x; return 1; }");
  }

  @Test
  public void testInlineWithLabel() {
    test("function f() { var x; L: x = 1; return x; }",
         "function f() { var x; return 1; }");
  }

  @Test
  public void testNoInlineGlobalScope() {
    testSame("var x = 1; print(x);");
  }

  @Test
  public void testNoInlineMultipleUses() {
    testSame("function f() { var x = 1; return x + x; }");
  }

  @Test
  public void testNoInlineWithinLoop() {
    testSame("function f() { var x = 1; while(true) { alert(x); } }");
    testSame("function f() { var x = 1; for(var i = 0; i < 10; i++) { alert(x); } }");
    testSame("function f() { var x = 1; do { alert(x); } while(true); }");
  }

  @Test
  public void testNoInlineGetPropOrElem() {
    testSame("function f(a) { var x = a.b; g(); return x; }");
    testSame("function f(a) { var x = a[0]; g(); return x; }");
  }

  @Test
  public void testNoInlineObjectOrArrayLiterals() {
    testSame("function f() { var x = {}; g(); return x; }");
    testSame("function f() { var x = []; g(); return x; }");
  }

  @Test
  public void testNoInlineRegExpOrNew() {
    testSame("function f() { var x = /abc/; g(); return x; }");
    testSame("function f() { var x = new Object(); g(); return x; }");
  }

  @Test
  public void testNoInlineSideEffectsInRhs() {
    testSame("function f() { var x = g(); return x; }");
    testSame("function f() { var x = (y = 1); return x; }");
  }

  @Test
  public void testNoInlineWhenAssignUsedAsRValue() {
    testSame("function f() { var x, y; y = (x = 1); return x; }");
  }

  @Test
  public void testNoInlineAcrossSideEffects() {
    testSame("function f(b) { var x = 1; modify(b); return x + b; }");
    testSame("function f(b) { var x = 1; delete b.c; return x; }");
    testSame("function f(b) { var x = 1; new Constructor(); return x; }");
  }

  @Test
  public void testCheckRightOfSideEffects() {
    testSame("function f(b) { var x; x = 1, modify(b); return x; }");
  }

  @Test
  public void testCheckLeftOfSideEffects() {
    testSame("function f(b) { var x; x = 1; modify(b), print(x); }");
  }

  @Test
  public void testDependencyInlining() {
    test("function f() { var x = 1; var y = x + 1; return y; }",
         "function f() { var x = 1; return x + 1; }");
  }

  @Test
  public void testMultipleAssignmentsNoInline() {
    testSame("function f(c) { var x; if (c) { x = 1; } else { x = 2; } return x; }");
  }

  @Test
  public void testSelfAssignmentAndIncDec() {
    testSame("function f(x) { x++; return x; }");
    testSame("function f(x) { x--; return x; }");
    testSame("function f(x) { x += 1; return x; }");
  }

  @Test
  public void testCatchVariableNoInline() {
    testSame("function f() { try {} catch (e) { return e; } }");
  }

  @Test
  public void testFunctionParameterNoInline() {
    testSame("function f(x) { return x; }");
  }

  @Test
  public void testPathWithSideEffects() {
    testSame("function f(b, c) { var x = 1; if (c) { modify(b); } else { noop(); } return x; }");
  }

  @Test
  public void testMaxVariablesBailout() {
    StringBuilder sb = new StringBuilder("function f() {\n");
    for (int i = 0; i < LiveVariablesAnalysis.MAX_VARIABLES_TO_ANALYZE + 5; i++) {
      sb.append("var v").append(i).append(" = 1;\n");
    }
    sb.append("return v0;\n}");
    testSame(sb.toString());
  }

  @Test
  public void testProcessDirectly() {
    Compiler compiler = new Compiler();
    FlowSensitiveInlineVariables pass = new FlowSensitiveInlineVariables(compiler);
    Node externs = new Node(Token.BLOCK);
    Node root = new Node(Token.BLOCK);
    pass.process(externs, root);
  }
}