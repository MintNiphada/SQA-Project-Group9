package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Assert;
import org.junit.Test;

public class TargetClassTest {

  private void test(String js, String expected, InlineVariables.Mode mode, boolean inlineAllStrings) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    Node root = compiler.parseTestCode(js);
    Assert.assertNotNull(root);
    Assert.assertEquals(0, compiler.getErrorCount());

    Node externs = new Node(132);
    Node mainRoot = new Node(132, externs, root);

    InlineVariables pass = new InlineVariables(compiler, mode, inlineAllStrings);
    pass.process(externs, root);

    Compiler expectedCompiler = new Compiler();
    expectedCompiler.initOptions(options);
    Node expectedRoot = expectedCompiler.parseTestCode(expected);

    String actualSource = compiler.toSource(root);
    String expectedSource = expectedCompiler.toSource(expectedRoot);
    Assert.assertEquals(expectedSource, actualSource);
  }

  private void testSame(String js, InlineVariables.Mode mode, boolean inlineAllStrings) {
    test(js, js, mode, inlineAllStrings);
  }

  @Test
  public void testInlineConstantsOnly() {
    test("var CONST_VAL = 1; var y = CONST_VAL;", "var y = 1;", InlineVariables.Mode.CONSTANTS_ONLY, false);
  }

  @Test
  public void testDoNotInlineNonConstantInConstantsOnlyMode() {
    testSame("var a = 1; var b = a;", InlineVariables.Mode.CONSTANTS_ONLY, false);
  }

  @Test
  public void testInlineLocalsOnly() {
    test("function f() { var a = 1; var b = a; return b; }", "function f() { var b = 1; return b; }", InlineVariables.Mode.LOCALS_ONLY, false);
  }

  @Test
  public void testDoNotInlineGlobalInLocalsOnlyMode() {
    testSame("var a = 1; var b = a;", InlineVariables.Mode.LOCALS_ONLY, false);
  }

  @Test
  public void testInlineAllMode() {
    test("var a = 1; var b = a;", "var b = 1;", InlineVariables.Mode.ALL, false);
  }

  @Test
  public void testInlineStringWorthInlining() {
    test("var s = 'a'; var x = s;", "var x = 'a';", InlineVariables.Mode.ALL, false);
  }

  @Test
  public void testInlineAllStringsOption() {
    test("var VERY_LONG_STRING = 'abcdefghijklmnopqrstuvwxyz1234567890'; var x = VERY_LONG_STRING; var y = VERY_LONG_STRING;",
         "var x = 'abcdefghijklmnopqrstuvwxyz1234567890'; var y = 'abcdefghijklmnopqrstuvwxyz1234567890';",
         InlineVariables.Mode.ALL, true);
  }

  @Test
  public void testDoNotInlineLongStringWhenNotWorthIt() {
    testSame("var s = 'abcdefghijklmnopqrstuvwxyz1234567890'; var a = s; var b = s; var c = s; var d = s;",
             InlineVariables.Mode.ALL, false);
  }

  @Test
  public void testInlineAliasVariable() {
    test("var a = 1; var b = a; var c = b;", "var c = 1;", InlineVariables.Mode.ALL, false);
  }

  @Test
  public void testInlineFunctionDeclaration() {
    test("function f() {} var x = f;", "var x = function f() {};", InlineVariables.Mode.ALL, false);
  }

  @Test
  public void testDoNotInlinedIntoCallContext() {
    testSame("var o = { m: function() {} }; var f = o.m; f();", InlineVariables.Mode.ALL, false);
  }

  @Test
  public void testInlineThisAlias() {
    test("function f() { var self = this; return self.x; }", "function f() { return this.x; }", InlineVariables.Mode.ALL, false);
  }

  @Test
  public void testUndefinedVariableInlining() {
    test("var a; var b = a; var c = a;", "var b = void 0; var c = void 0;", InlineVariables.Mode.ALL, false);
  }

  @Test
  public void testVariableSeparatedAssignmentAndDeclaration() {
    test("var a; a = 1; var b = a;", "var b = 1;", InlineVariables.Mode.ALL, false);
  }

  @Test
  public void testSideEffectsPreventMotion() {
    testSame("var a = 1; externalEffect(); var b = a;", InlineVariables.Mode.ALL, false);
  }

  @Test
  public void testMultipleVarsInSingleDeclaration() {
    test("var a = 1, b = 2; var c = a + b;", "var c = 1 + 2;", InlineVariables.Mode.ALL, false);
  }

  @Test
  public void testUnassignedVarWithSingleAssignmentRemoved() {
    test("var a; a = 5;", ";", InlineVariables.Mode.ALL, false);
  }

  @Test
  public void testModeValues() {
    InlineVariables.Mode[] modes = InlineVariables.Mode.values();
    Assert.assertEquals(3, modes.length);
    Assert.assertEquals(InlineVariables.Mode.CONSTANTS_ONLY, InlineVariables.Mode.valueOf("CONSTANTS_ONLY"));
    Assert.assertEquals(InlineVariables.Mode.LOCALS_ONLY, InlineVariables.Mode.valueOf("LOCALS_ONLY"));
    Assert.assertEquals(InlineVariables.Mode.ALL, InlineVariables.Mode.valueOf("ALL"));
  }
}
