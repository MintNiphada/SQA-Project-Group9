package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Assert;
import org.junit.Test;

public class TargetClassTest {

  private void test(String js, String expected) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCodingConvention(new ClosureCodingConvention());
    Node externs = new Node(com.google.javascript.rhino.Token.BLOCK);
    Node root = compiler.parseTestCode(js);
    FlowSensitiveInlineVariables pass = new FlowSensitiveInlineVariables(compiler);
    pass.process(externs, root);
    if (expected != null) {
      Node expectedRoot = compiler.parseTestCode(expected);
      String diff = compiler.toSource(root);
      String exp = compiler.toSource(expectedRoot);
      Assert.assertEquals(exp, diff);
    }
  }

  private void testSame(String js) {
    test(js, js);
  }

  @Test
  public void testSimpleInlining() {
    test("function f() { var x = 1; return x; }", "function f() { return 1; }");
  }

  @Test
  public void testAssignInlining() {
    test("function f() { var x; x = 1; return x; }", "function f() { var x; return 1; }");
  }

  @Test
  public void testLabeledAssignInlining() {
    test("function f() { var x; label: x = 1; return x; }", "function f() { var x; return 1; }");
  }

  @Test
  public void testNoInlineGlobalScope() {
    testSame("var x = 1; var y = x;");
  }

  @Test
  public void testNoInlineFunctionParam() {
    testSame("function f(x) { return x; }");
  }

  @Test
  public void testNoInlineMultipleUses() {
    testSame("function f() { var x = 1; return x + x; }");
  }

  @Test
  public void testNoInlineWithinLoop() {
    testSame("function f() { var x = 1; while(true) { alert(x); } }");
  }

  @Test
  public void testNoInlineSideEffectsOnRhs() {
    testSame("function f() { var x = foo(); return x; }");
  }

  @Test
  public void testNoInlineRValueAssignment() {
    testSame("function f() { var x; var y = (x = 1); return x; }");
  }

  @Test
  public void testNoInlineGetProp() {
    testSame("function f() { var x = a.b; return x; }");
  }

  @Test
  public void testNoInlineGetElem() {
    testSame("function f() { var x = a[0]; return x; }");
  }

  @Test
  public void testNoInlineArrayLit() {
    testSame("function f() { var x = [1, 2]; return x; }");
  }

  @Test
  public void testNoInlineObjectLit() {
    testSame("function f() { var x = {a: 1}; return x; }");
  }

  @Test
  public void testNoInlineRegExp() {
    testSame("function f() { var x = /abc/; return x; }");
  }

  @Test
  public void testNoInlineNew() {
    testSame("function f() { var x = new Foo(); return x; }");
  }

  @Test
  public void testNoInlineCatchVar() {
    testSame("function f() { try {} catch(e) { var x = e; return x; } }");
  }

  @Test
  public void testNoInlineSideEffectOnPath() {
    testSame("function f() { var x = 1; foo(); return x; }");
  }

  @Test
  public void testSideEffectPredicate() {
    testSame("function f() { var x = 1; delete a.b; return x; }");
    testSame("function f() { var x = 1; new Foo(); return x; }");
  }

  @Test
  public void testInlineChainedAssignmentTarget() {
    test("function f() { var x = 1; var y = x; return y; }", "function f() { var y = 1; return y; }");
  }

  @Test
  public void testExportedNameNotGathered() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCodingConvention(new CodingConvention.DefaultCodingConvention() {
      @Override
      public boolean isExported(String name) {
        return "_exported".equals(name);
      }
    });
    Node externs = new Node(com.google.javascript.rhino.Token.BLOCK);
    Node root = compiler.parseTestCode("function f() { var _exported = 1; return _exported; }");
    FlowSensitiveInlineVariables pass = new FlowSensitiveInlineVariables(compiler);
    pass.process(externs, root);
    Assert.assertEquals("function f() { var _exported = 1; return _exported; }", compiler.toSource(root));
  }

  @Test
  public void testDependencyTracking() {
    test("function f() { var a = 1; var b = a; var c = b; return c; }",
         "function f() { var b = 1; var c = b; return c; }");
  }

  @Test
  public void testMaxVariablesBailout() {
    StringBuilder sb = new StringBuilder("function f() {\n");
    for (int i = 0; i <= LiveVariablesAnalysis.MAX_VARIABLES_TO_ANALYZE + 5; i++) {
      sb.append("var v").append(i).append(" = 1;\n");
    }
    sb.append("return v0;\n}");
    testSame(sb.toString());
  }

  @Test
  public void testExitScopeAndVisitCoverage() {
    FlowSensitiveInlineVariables pass = new FlowSensitiveInlineVariables(new Compiler());
    pass.exitScope(null);
    pass.visit(null, null, null);
  }
}
