package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

public class DeadAssignmentsEliminationTest extends CompilerTestCase {

  public DeadAssignmentsEliminationTest() {
    super();
  }

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new DeadAssignmentsElimination(compiler);
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  @Test
  public void testNullExterns() {
    try {
      Compiler compiler = new Compiler();
      DeadAssignmentsElimination dae = new DeadAssignmentsElimination(compiler);
      dae.process(null, new Node(Token.BLOCK));
      fail("Expected NullPointerException");
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void testNullRoot() {
    try {
      Compiler compiler = new Compiler();
      DeadAssignmentsElimination dae = new DeadAssignmentsElimination(compiler);
      dae.process(new Node(Token.BLOCK), null);
      fail("Expected NullPointerException");
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void testGlobalScopeIgnored() {
    testSame("var x = 1; x = 2;");
  }

  @Test
  public void testInnerFunctionIgnored() {
    testSame("function f() { var x = 1; function g() { return x; } x = 2; }");
  }

  @Test
  public void testNoRemovableAssigns() {
    testSame("function f() { var x; return 1; }");
  }

  @Test
  public void testSimpleDeadAssignment() {
    test("function f() { var x; x = 1; }", "function f() { var x; 1; }");
    test("function f() { var x; x = 1; return 2; }", "function f() { var x; 1; return 2; }");
  }

  @Test
  public void testSelfAssignment() {
    test("function f() { var x = 1; x = x; return x; }",
         "function f() { var x = 1; x; return x; }");
  }

  @Test
  public void testCompoundAssignment() {
    test("function f() { var x = 1; x += 2; return 3; }",
         "function f() { var x = 1; x + 2; return 3; }");
    test("function f() { var x = 1; x -= 2; return 3; }",
         "function f() { var x = 1; x - 2; return 3; }");
    test("function f() { var x = 1; x *= 2; return 3; }",
         "function f() { var x = 1; x * 2; return 3; }");
  }

  @Test
  public void testIncDecExpressions() {
    test("function f() { var x = 1; x++; return 2; }",
         "function f() { var x = 1; void 0; return 2; }");
    test("function f() { var x = 1; x--; return 2; }",
         "function f() { var x = 1; void 0; return 2; }");
    test("function f() { var x = 1; ++x; return 2; }",
         "function f() { var x = 1; void 0; return 2; }");
    test("function f() { var x = 1; --x; return 2; }",
         "function f() { var x = 1; void 0; return 2; }");
  }

  @Test
  public void testIncDecInFor() {
    test("function f() { var x = 1; for (var i = 0; i < 10; x++) { i++; } return 2; }",
         "function f() { var x = 1; for (var i = 0; i < 10; ) { i++; } return 2; }");
  }

  @Test
  public void testControlStructures() {
    test("function f() { var x; if (x = 1) {} return 2; }",
         "function f() { var x; if (1) {} return 2; }");
    test("function f() { var x; while (x = 1) {} return 2; }",
         "function f() { var x; while (1) {} return 2; }");
    test("function f() { var x; do {} while (x = 1); return 2; }",
         "function f() { var x; do {} while (1); return 2; }");
    test("function f() { var x; for (; x = 1;) {} return 2; }",
         "function f() { var x; for (; 1;) {} return 2; }");
    test("function f() { var x; switch (x = 1) { case (x = 2): return (x = 3); } return 4; }",
         "function f() { var x; switch (1) { case 2: return 3; } return 4; }");
  }

  @Test
  public void testForInUnchanged() {
    testSame("function f() { var x = {}; for (var k in x) {} }");
  }

  @Test
  public void testMultipleDeadAssignments() {
    test("function f() { var x, y; x = y = 1; }",
         "function f() { var x, y; 1; }");
  }

  @Test
  public void testCommaAssignments() {
    test("function f(x, y) { var a; return (a = x, a = y, a); }",
         "function f(x, y) { var a; return (x, a = y, a); }");
    test("function f(x, a) { return (a = x, a = 1, a); }",
         "function f(x, a) { return (x, a = 1, a); }");
  }

  @Test
  public void testUndeclaredOrNonLocalName() {
    testSame("function f() { y = 1; return 2; }");
    testSame("function f() { var obj = {}; obj.x = 1; return 2; }");
  }

  @Test
  public void testVariableReadBeforeKillInExpression() {
    testSame("function f(a) { if ((a = 1) && (a == 1)) { return a; } }");
    test("function f(a) { if ((a = 1) && (a = 2)) { return a; } }",
         "function f(a) { if (1 && (a = 2)) { return a; } }");
  }

  @Test
  public void testSubexpressionIncDecNotRemovable() {
    testSame("function f() { var x = 1; return x++; }");
    testSame("function f() { var x = 1; return ++x; }");
    testSame("function f() { var x = 1; var y = x++; return y; }");
  }

  @Test
  public void testLiveVariableNotRemoved() {
    testSame("function f() { var x = 1; x = 2; return x; }");
    testSame("function f(x) { x = x + 1; return x; }");
  }
}