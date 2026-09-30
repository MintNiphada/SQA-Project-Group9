package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;

class FlowSensitiveInlineVariablesTest extends CompilerTestCase {

  public FlowSensitiveInlineVariablesTest() {
    enableNormalize();
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  @Override
  protected CompilerPass getProcessor(final Compiler compiler) {
    return new FlowSensitiveInlineVariables(compiler);
  }

  public void testSimpleInline() {
    inline("function f() { var x = 1; return x; }",
           "function f() { var x; return 1; }");
  }

  public void testNoInlineGlobal() {
    testSame("var x = 1; var y = x;");
  }

  public void testSimpleAssignInline() {
    inline("function f() { var x; x = 1; return x; }",
           "function f() { var x; return 1; }");
  }

  public void testMultipleUsesNotIdentical() {
    testSame("function f() { var x = 1; return x + x; }");
  }

  public void testNoInlineWithinLoop() {
    testSame("function f() { var x = 1; while (true) { alert(x); } }");
    testSame("function f() { var x = 1; for (var i = 0; i < 10; i++) { alert(x); } }");
    testSame("function f() { var x = 1; do { alert(x); } while (true); }");
  }

  public void testNoInlineSideEffectRhs() {
    testSame("function f() { var x = externalFunc(); return x; }");
    testSame("function f() { var x = new CustomObject(); return x; }");
  }

  public void testNoInlineSideEffectBetween() {
    testSame("function f() { var x = 1; modifyState(); return x; }");
  }

  public void testNoInlineObjectOrArrayLiterals() {
    testSame("function f() { var x = {}; return x; }");
    testSame("function f() { var x = []; return x; }");
    testSame("function f() { var x = /abc/; return x; }");
  }

  public void testNoInlineGetPropOrGetElem() {
    testSame("function f(a) { var x = a.b; return x; }");
    testSame("function f(a) { var x = a['b']; return x; }");
  }

  public void testInlineLabeledAssign() {
    inline("function f() { var x; label: { x = 1; } return x; }",
           "function f() { var x; return 1; }");
  }

  public void testNoInlineAssignInExpression() {
    testSame("function f() { var x, y; y = (x = 1); return x; }");
  }

  public void testNoInlineFunctionParam() {
    testSame("function f(x) { return x; }");
  }

  public void testNoInlineIfUsedInAssignmentLhs() {
    testSame("function f() { var x = 1; x = 2; return x; }");
  }

  public void testNoInlineIncrementDecrement() {
    testSame("function f() { var x = 1; x++; return x; }");
    testSame("function f() { var x = 1; x--; return x; }");
  }

  public void testInlineWithAdjacentNodes() {
    inline("function f() { var x = 1; var y = x + 2; return y; }",
           "function f() { var x; var y = 1 + 2; return y; }");
  }

  public void testNoInlineCatchBlock() {
    testSame("function f() { try { } catch (e) { return e; } }");
  }

  public void testNoInlineOuterScopeVarDependency() {
    testSame("function f() { var outer = 1; function g() { var x = outer; return x; } }");
  }

  public void testDoNotInlineAcrossSideEffectCall() {
    testSame("function f(a) { var x = a; callSideEffect(); return x; }");
  }

  public void testInlineMultipleStatements() {
    inline("function f() { var a = 2; var b = 3; return a + b; }",
           "function f() { var a; var b = 3; return 2 + b; }");
  }

  public void testNoInlineIfModifiedOnPath() {
    testSame("function f(cond) { var x = 1; if (cond) { x = 2; } return x; }");
  }

  public void testCheckLeftOfSideEffect() {
    testSame("function f() { var x = 1; foo(), alert(x); }");
  }

  public void testCheckRightOfSideEffect() {
    testSame("function f() { var x; x = 1, foo(); return x; }");
  }

  public void testForInSideEffects() {
    testSame("function f(obj) { var x = 1; for (var k in obj) {} return x; }");
  }

  public void testNoInlineExportedVariable() {
    testSame("function f() { var _x = 1; return _x; }");
  }

  private void inline(String src, String expected) {
    test(src, expected);
  }
}