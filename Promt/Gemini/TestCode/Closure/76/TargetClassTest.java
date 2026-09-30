package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Test;

public class DeadAssignmentsEliminationTest extends CompilerTestCase {

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new DeadAssignmentsElimination(compiler);
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  @Test
  public void testSimpleDeadAssignment() {
    test("function f() { var x; x = 1; }",
         "function f() { var x; 1; }");
  }

  @Test
  public void testDeadAssignmentWithSubsequentAssign() {
    test("function f() { var x = 1; x = 2; }",
         "function f() { var x = 1; 2; }");
  }

  @Test
  public void testLiveAssignment() {
    testSame("function f() { var x = 1; x = 2; return x; }");
  }

  @Test
  public void testIdentityAssignment() {
    test("function f() { var x = 1; x = x; return x; }",
         "function f() { var x = 1; x; return x; }");
  }

  @Test
  public void testAssignmentOps() {
    test("function f() { var x = 1; x += 2; }",
         "function f() { var x = 1; x + 2; }");
    test("function f() { var x = 1; x -= 2; }",
         "function f() { var x = 1; x - 2; }");
    test("function f() { var x = 1; x *= 2; }",
         "function f() { var x = 1; x * 2; }");
    test("function f() { var x = 1; x /= 2; }",
         "function f() { var x = 1; x / 2; }");
  }

  @Test
  public void testIncDec() {
    test("function f() { var x = 1; x++; }",
         "function f() { var x = 1; void 0; }");
    test("function f() { var x = 1; x--; }",
         "function f() { var x = 1; void 0; }");
    test("function f() { var x = 1; ++x; }",
         "function f() { var x = 1; void 0; }");
    test("function f() { var x = 1; --x; }",
         "function f() { var x = 1; void 0; }");
  }

  @Test
  public void testIncDecInForLoop() {
    test("function f() { var x = 1; for (; ; x++) {} }",
         "function f() { var x = 1; for (; ; ) {} }");
  }

  @Test
  public void testIncDecInComma() {
    test("function f() { var x = 1; var y = (x++, 2); return y; }",
         "function f() { var x = 1; var y = 2; return y; }");
  }

  @Test
  public void testConditionAssignments() {
    test("function f() { var x; if (x = 1) { return 0; } }",
         "function f() { var x; if (1) { return 0; } }");
    test("function f() { var x; while (x = 1) { break; } }",
         "function f() { var x; while (1) { break; } }");
    test("function f() { var x; do { break; } while (x = 1); }",
         "function f() { var x; do { break; } while (1); }");
    test("function f() { var x; for (; x = 1;) { break; } }",
         "function f() { var x; for (; 1;) { break; } }");
  }

  @Test
  public void testSwitchAndCase() {
    test("function f() { var x; switch (x = 1) { default: } }",
         "function f() { var x; switch (1) { default: } }");
    test("function f() { var x; switch (1) { case x = 1: break; } }",
         "function f() { var x; switch (1) { case 1: break; } }");
  }

  @Test
  public void testReturnAssignment() {
    test("function f() { var x; return x = 1; }",
         "function f() { var x; return 1; }");
  }

  @Test
  public void testNestedAssignments() {
    test("function f() { var x, y; x = y = 1; }",
         "function f() { var x, y; 1; }");
  }

  @Test
  public void testGlobalScopeUntouched() {
    testSame("var x = 1; x = 2;");
    testSame("x = 1;");
  }

  @Test
  public void testClosureEscapedVariable() {
    testSame("function f() { var x = 1; function g() { return x; } return g; }");
  }

  @Test
  public void testNoRemovableAssigns() {
    testSame("function f() { var x = 1; return x; }");
  }

  @Test
  public void testForInUntouched() {
    testSame("function f() { var obj = {}, x; for (x in obj) {} }");
  }

  @Test
  public void testLogicalAndOrExpressions() {
    test("function f(a) { var x; (x = 1) && a; }",
         "function f(a) { var x; 1 && a; }");
    test("function f(a) { var x; (x = 1) || a; }",
         "function f(a) { var x; 1 || a; }");
  }

  @Test
  public void testHookExpressions() {
    test("function f(a) { var x; a ? (x = 1) : (x = 2); }",
         "function f(a) { var x; a ? 1 : 2; }");
    test("function f(a) { var x; (x = 1) ? 2 : 3; }",
         "function f(a) { var x; 1 ? 2 : 3; }");
  }

  @Test
  public void testVariableReadAfterAssignmentInCondition() {
    testSame("function f(a) { var x; if ((x = a) && x) { return 1; } }");
    testSame("function f() { var a, x; if ((a = 1) && (x = a)) {} a = 2; }");
    test("function f() { var a, x; if ((x = a) && (a = 1)) {} a = 2; }",
         "function f() { var a, x; if ((x = a) && 1) {} a = 2; }");
  }

  @Test
  public void testHookBranchReadBeforeKill() {
    testSame("function f(b) { var a, x; if (b ? (x = a) : (x = 1)) {} a = 2; }");
    testSame("function f(b) { var a, x; if (b ? (x = 1) : (x = a)) {} a = 2; }");
    test("function f(b) { var a; if (b ? (a = 1) : (a = 2)) {} a = 3; }",
         "function f(b) { var a; if (b ? 1 : 2) {} a = 3; }");
  }

  @Test(expected = NullPointerException.class)
  public void testProcessNullExterns() {
    Compiler compiler = new Compiler();
    DeadAssignmentsElimination dae = new DeadAssignmentsElimination(compiler);
    dae.process(null, new Node(0));
  }

  @Test(expected = NullPointerException.class)
  public void testProcessNullRoot() {
    Compiler compiler = new Compiler();
    DeadAssignmentsElimination dae = new DeadAssignmentsElimination(compiler);
    dae.process(new Node(0), null);
  }
}