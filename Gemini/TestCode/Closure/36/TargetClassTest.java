package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class InlineVariablesTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
  }

  private void test(String js, String expected, InlineVariables.Mode mode, boolean inlineAllStrings) {
    Compiler c = new Compiler();
    CompilerOptions options = new CompilerOptions();
    c.initOptions(options);
    Node externsNode = c.parseTestCode("");
    Node rootNode = c.parseTestCode(js);
    Assert.assertEquals(0, c.getErrorCount());

    InlineVariables pass = new InlineVariables(c, mode, inlineAllStrings);
    pass.process(externsNode, rootNode);

    String actual = c.toSource(rootNode).trim();
    String normalizedExpected = c.toSource(c.parseTestCode(expected)).trim();
    Assert.assertEquals(normalizedExpected, actual);
  }

  private void testSame(String js, InlineVariables.Mode mode, boolean inlineAllStrings) {
    test(js, js, mode, inlineAllStrings);
  }

  private void testAll(String js, String expected) {
    test(js, expected, InlineVariables.Mode.ALL, true);
  }

  private void testLocals(String js, String expected) {
    test(js, expected, InlineVariables.Mode.LOCALS_ONLY, true);
  }

  private void testConstants(String js, String expected) {
    test(js, expected, InlineVariables.Mode.CONSTANTS_ONLY, true);
  }

  @Test
  public void testModeEnums() {
    Assert.assertEquals(3, InlineVariables.Mode.values().length);
    Assert.assertEquals(InlineVariables.Mode.CONSTANTS_ONLY, InlineVariables.Mode.valueOf("CONSTANTS_ONLY"));
    Assert.assertEquals(InlineVariables.Mode.LOCALS_ONLY, InlineVariables.Mode.valueOf("LOCALS_ONLY"));
    Assert.assertEquals(InlineVariables.Mode.ALL, InlineVariables.Mode.valueOf("ALL"));
  }

  @Test
  public void testInlineSimpleVariables() {
    testAll("var x = 1; var y = x;", "var y = 1;");
    testAll("var x = 'hello'; var y = x;", "var y = 'hello';");
    testAll("var x = true; var y = x;", "var y = true;");
    testAll("var x = null; var y = x;", "var y = null;");
    testAll("var x = void 0; var y = x;", "var y = void 0;");
  }

  @Test
  public void testInlineLocalsOnly() {
    // Global variable should NOT be inlined in LOCALS_ONLY mode
    testLocals("var x = 1; var y = x;", "var x = 1; var y = x;");
    // Local variable inside function SHOULD be inlined
    testLocals(
        "function f() { var x = 1; return x; }",
        "function f() { return 1; }"
    );
  }

  @Test
  public void testInlineConstantsOnly() {
    // Non-constant variable should NOT be inlined
    testConstants("var x = 1; var y = x;", "var x = 1; var y = x;");
    // Constant named variable should be inlined
    testConstants("var CONST_X = 1; var y = CONST_X;", "var y = 1;");
  }

  @Test
  public void testInlineMultipleReferencesToImmutable() {
    testAll("var x = 1; var y = x + x;", "var y = 1 + 1;");
    testAll("var x = 'str'; var y = x + x;", "var y = 'str' + 'str';");
  }

  @Test
  public void testInlineUninitializedVariable() {
    testAll("var x; var y = x;", "var y = void 0;");
    testAll("var x; var y = x; var z = x;", "var y = void 0; var z = void 0;");
  }

  @Test
  public void testInlineThisAlias() {
    testAll(
        "function f() { var self = this; return self.foo; }",
        "function f() { return this.foo; }"
    );
  }

  @Test
  public void testDoNotInlinedThisIfEscaped() {
    testSame(
        "function f() { var self = this; function g() { return self; } }",
        InlineVariables.Mode.ALL,
        true
    );
  }

  @Test
  public void testDoNotInlineGetPropCallContext() {
    // Calling an inlined method property changes 'this' context, should not be inlined
    testSame(
        "var a = b.c; a();",
        InlineVariables.Mode.ALL,
        true
    );
    // Passing as argument is fine
    testAll(
        "var a = b.c; f(a);",
        "f(b.c);"
    );
  }

  @Test
  public void testDoNotInlineAcrossSideEffects() {
    // Function calls may change 'y'
    testSame(
        "var x = y; f(); var z = x;",
        InlineVariables.Mode.ALL,
        true
    );
  }

  @Test
  public void testInlineAcrossSideEffectsIfLiteral() {
    testAll(
        "var x = 5; f(); var z = x;",
        "f(); var z = 5;"
    );
    testAll(
        "var x = function() {}; f(); var z = x;",
        "f(); var z = function() {};"
    );
  }

  @Test
  public void testInlineAliasCandidates() {
    testAll(
        "function f(a) { var b = a; var c = b; return c; }",
        "function f(a) { return a; }"
    );
  }

  @Test
  public void testArgumentsEscaped() {
    // If arguments escape, do not inline aliased parameters
    testSame(
        "function f(x) { var y = x; var args = arguments; return y + args[0]; }",
        InlineVariables.Mode.ALL,
        true
    );
  }

  @Test
  public void testStringInliningHeuristic() {
    // When inlineAllStrings is false, long strings used multiple times should not be inlined
    test(
        "var LONG_NAME = 'very_long_string_constant_that_should_not_be_inlined_multiple_times';"
            + "var a = LONG_NAME; var b = LONG_NAME; var c = LONG_NAME; var d = LONG_NAME;",
        "var LONG_NAME = 'very_long_string_constant_that_should_not_be_inlined_multiple_times';"
            + "var a = LONG_NAME; var b = LONG_NAME; var c = LONG_NAME; var d = LONG_NAME;",
        InlineVariables.Mode.ALL,
        false
    );
    // When inlineAllStrings is true, it should inline
    test(
        "var LONG_NAME = 'very_long_string';"
            + "var a = LONG_NAME;",
        "var a = 'very_long_string';",
        InlineVariables.Mode.ALL,
        true
    );
  }

  @Test
  public void testDoNotInlineSpecialRenamePropertyFunction() {
    testSame(
        "var JSCompiler_renameProperty = function(a) { return a; }; var x = JSCompiler_renameProperty;",
        InlineVariables.Mode.ALL,
        true
    );
  }

  @Test
  public void testSeparateDeclarationAndInitialization() {
    testAll(
        "var a; a = 1; var b = a;",
        "var b = 1;"
    );
    testAll(
        "function f() { var a; a = 1; return a; }",
        "function f() { return 1; }"
    );
  }

  @Test
  public void testDoNotInlinedIfAssignedMultipleTimes() {
    testSame(
        "var a = 1; a = 2; var b = a;",
        InlineVariables.Mode.ALL,
        true
    );
  }

  @Test
  public void testDoNotInlinedIfExported() {
    testSame(
        "var _x = 1; var y = _x;",
        InlineVariables.Mode.ALL,
        true
    );
  }

  @Test
  public void testInlineFunctionDeclarations() {
    testAll(
        "function f() { var x = function() { return 1; }; return x(); }",
        "function f() { return (function() { return 1; })(); }"
    );
  }

  @Test
  public void testSubclassRelationshipCallNotAliased() {
    // goog.inherits call should not inline class constructor
    testSame(
        "function goog$inherits(a, b) {}"
            + "var Sub = function() {};"
            + "var Super = function() {};"
            + "goog$inherits(Sub, Super);",
        InlineVariables.Mode.ALL,
        true
    );
  }

  @Test
  public void testMultiVarDeclaration() {
    testAll(
        "var a = 1, b = 2; var c = a + b;",
        "var c = 1 + 2;"
    );
    testAll(
        "var a = 1, b = 2; var c = a;",
        "var b = 2; var c = 1;"
    );
  }
}