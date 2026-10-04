package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CoalesceVariableNamesTest {
  private Compiler compiler;
  private boolean usePseudoNames;

  @Before
  public void setUp() {
    compiler = new Compiler();
    usePseudoNames = false;
  }

  private void test(String js, String expected) {
    Node root = compiler.parseTestCode(js);
    Node externs = new Node(Token.BLOCK);
    CoalesceVariableNames pass = new CoalesceVariableNames(compiler, usePseudoNames);
    pass.process(externs, root);
    String actual = compiler.toSource(root);
    assertEquals(expected, actual);
  }

  private void testSame(String js) {
    test(js, js);
  }

  @Test
  public void testSimpleCoalesce() {
    test(
        "function f() { var x = 1; use(x); var y = 2; use(y); }",
        "function f(){var x=1;use(x);x=2;use(x)}");
  }

  @Test
  public void testInterferencePreventsCoalesce() {
    testSame("function f() { var x = 1; var y = 2; use(x); use(y); }");
  }

  @Test
  public void testGlobalScopeIgnored() {
    testSame("var x = 1; use(x); var y = 2; use(y);");
  }

  @Test
  public void testPseudoNames() {
    usePseudoNames = true;
    test(
        "function f() { var x = 1; use(x); var y = 2; use(y); }",
        "function f(){var x_y=1;use(x_y);x_y=2;use(x_y)}");
  }

  @Test
  public void testPseudoNamesCollision() {
    usePseudoNames = true;
    test(
        "function f() { var x_y = 0; var x = 1; use(x); var y = 2; use(y); }",
        "function f(){var x_y=0;var x_y$=1;use(x_y$);x_y$=2;use(x_y$)}");
  }

  @Test
  public void testForInLoopVar() {
    test(
        "function f(obj) { var x = 1; use(x); for (var y in obj) { use(y); } }",
        "function f(obj){var x=1;use(x);for(x in obj){use(x)}}");
  }

  @Test
  public void testForLoopVar() {
    test(
        "function f() { var x = 1; use(x); for (var y = 0; y < 10; y++) { use(y); } }",
        "function f(){var x=1;use(x);for(x=0;x<10;x++){use(x)}}");
  }

  @Test
  public void testForLoopVarWithoutInit() {
    test(
        "function f() { var x = 1; use(x); for (var y; ; ) { use(y); } }",
        "function f(){var x=1;use(x);for(;;){use(x)}}");
  }

  @Test
  public void testMultipleVarsInDeclaration() {
    test(
        "function f() { var x = 1; use(x); var a, y = 2; use(y); use(a); }",
        "function f(){var x=1;use(x);var a;x=2;use(x);use(a)}");
  }

  @Test
  public void testFunctionDeclarationsNotCoalesced() {
    testSame("function f() { function g() {} function h() {} g(); h(); }");
  }

  @Test
  public void testParametersNotCoalescedTogether() {
    testSame("function f(x, y) { use(x); use(y); }");
  }

  @Test
  public void testAssignmentOperations() {
    test(
        "function f() { var x = 1; x += 1; use(x); var y = 2; y += 2; use(y); }",
        "function f(){var x=1;x+=1;use(x);x=2;x+=2;use(x)}");
  }

  @Test
  public void testNestedFunctions() {
    test(
        "function f() { function g() { var a = 1; use(a); var b = 2; use(b); } g(); }",
        "function f(){function g(){var a=1;use(a);a=2;use(a)}g()}");
  }

  @Test
  public void testUnusedVars() {
    test(
        "function f() { var x; var y; }",
        "function f(){var x;var x}");
  }

  @Test
  public void testDirectConstructor() {
    CoalesceVariableNames pass = new CoalesceVariableNames(compiler, false);
    assertNotNull(pass);
    Node externs = new Node(Token.BLOCK);
    Node root = new Node(Token.BLOCK);
    pass.process(externs, root);
    assertTrue(root.isEmpty());
  }
}
