package com.google.javascript.jscomp;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class FlowSensitiveInlineVariablesTest extends CompilerTestCase {

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new FlowSensitiveInlineVariables(compiler);
  }

  @Test
  public void testInlineSimpleVar() {
    test("function f(){var x=1; alert(x)}", "function f(){var x; alert(1)}");
  }

  @Test
  public void testInlineAssign() {
    test("function f(){var x; x=1; alert(x)}", "function f(){var x; alert(1)}");
  }

  @Test
  public void testNoInlineParameter() {
    testSame("function f(a){alert(a)}");
  }

  @Test
  public void testNoInlineMultipleUses() {
    testSame("function f(){var x=1; alert(x); alert(x)}");
  }

  @Test
  public void testNoInlineDefHasSideEffects() {
    testSame("function f(){var x=foo(); alert(x)}");
  }

  @Test
  public void testNoInlinePathSideEffect() {
    testSame("function f(){var x=1; foo(); alert(x)}");
  }

  @Test
  public void testInlineAdjacentStatements() {
    test("function f(){var x=1; alert(x)}", "function f(){var x; alert(1)}");
  }

  @Test
  public void testNoInlineGetPropRHS() {
    testSame("function f(){var x=a.b; alert(x)}");
  }

  @Test
  public void testNoInlineAssignUsedAsRValue() {
    testSame("function f(){var x; var y=(x=1); alert(x)}");
  }

  @Test
  public void testNoInlineUseInLoop() {
    testSame("function f(){var x=1; while(cond){alert(x)}}");
  }

  @Test
  public void testNoInlineGlobal() {
    testSame("var x=1; alert(x);");
  }

  @Test
  public void testNoInlineVarWithSideEffectSibling() {
    testSame("function f(){var x=1, y=foo(); alert(x)}");
  }

  @Test
  public void testNoInlineUseWithSideEffectLeft() {
    testSame("function f(){var x=1; (foo(), alert(x))}");
  }

  @Test
  public void testNoInlineIfUseInCatch() {
    testSame("function f(){try{}catch(e){alert(e)}}");
  }

  @Test
  public void testNoInlineIfUseInParamList() {
    testSame("function f(a){function g(b){alert(b)} g(a)}");
  }

  @Test
  public void testNoInlineIfUseInIncDec() {
    testSame("function f(){var x=1; x++; alert(x)}");
  }

  @Test
  public void testNoInlineIfNameExported() {
    testSame("function f(){var x=1; goog.exportSymbol('x', x); alert(x)}");
  }
}
