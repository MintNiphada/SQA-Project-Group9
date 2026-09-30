package com.google.javascript.jscomp;

import org.junit.Test;

class ExploitAssignsTest extends CompilerTestCase {

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new PeepholeOptimizationsPass(compiler, new ExploitAssigns());
  }

  @Override
  protected int getNumRepetitions() {
    return 2;
  }

  @Test
  public void testSimpleAssign() {
    test("x = 1; y = x;", "y = x = 1;");
  }

  @Test
  public void testImmutableValues() {
    test("x = 1; y = 1;", "y = x = 1;");
    test("x = true; y = true;", "y = x = true;");
    test("x = false; y = false;", "y = x = false;");
    test("x = null; y = null;", "y = x = null;");
    test("x = 'hello'; y = 'hello';", "y = x = 'hello';");
  }

  @Test
  public void testMultipleChainedAssigns() {
    test("a = 1; b = 1; c = 1;", "c = b = a = 1;");
    test("a = 1; b = a; c = b;", "c = b = a = 1;");
    test("x = y = 1; z = x;", "z = x = y = 1;");
    test("x = 1; y = x; z = y;", "z = y = x = 1;");
  }

  @Test
  public void testIfStatement() {
    test("x = 1; if (x) {}", "if (x = 1) {}");
    test("x = true; if (x) { foo(); }", "if (x = true) { foo(); }");
  }

  @Test
  public void testReturnStatement() {
    test("function f() { x = 1; return x; }", "function f() { return x = 1; }");
    test("function f() { x = 'a'; return 'a'; }", "function f() { return x = 'a'; }");
  }

  @Test
  public void testLogicalAndOrHook() {
    test("x = 1; x && foo();", "(x = 1) && foo();");
    test("x = 1; x || foo();", "(x = 1) || foo();");
    test("x = 1; x ? foo() : bar();", "(x = 1) ? foo() : bar();");
  }

  @Test
  public void testVarInitialization() {
    test("x = 1; var y = x;", "var y = x = 1;");
    test("x = 'str'; var y = 'str';", "var y = x = 'str';");
    testSame("x = 1; var y;");
  }

  @Test
  public void testThisPropertyAssignment() {
    test("this.foo = 1; x = this.foo;", "x = this.foo = 1;");
    test("x = 1; this.foo = x;", "this.foo = x = 1;");
    test("this.a = 1; this.b = this.a;", "this.b = this.a = 1;");
  }

  @Test
  public void testObjectPropertyAssignment() {
    test("obj.foo = 1; x = 1;", "x = obj.foo = 1;");
    test("x = 1; a.b = 1;", "a.b = x = 1;");
    testSame("obj.foo = bar(); x = obj.foo;");
    testSame("x = 1; a.b = x;");
    testSame("x = 1; (a = b).c = x;");
  }

  @Test
  public void testSafeReplacement() {
    test("x = a; a.b = x;", "a.b = x = a;");
    testSame("x = a.b; a = 1;");
    testSame("x = a.b; a.b = 2;");
  }

  @Test
  public void testNestedAssign() {
    test("x = y = 2; z = 2;", "z = x = y = 2;");
  }

  @Test
  public void testNoCollapseWhenValuesDiffer() {
    testSame("x = 1; y = 2;");
    testSame("x = true; y = false;");
    testSame("x = 'a'; y = 'b';");
  }

  @Test
  public void testNoCollapseNonQualifiedName() {
    testSame("x = 1; foo() = x;");
    testSame("x = 1; x();");
  }

  @Test
  public void testExprResultDiving() {
    test("x = 1; (y = x);", "y = x = 1;");
  }
}