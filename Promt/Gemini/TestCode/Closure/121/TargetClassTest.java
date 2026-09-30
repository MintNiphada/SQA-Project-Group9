package com.google.javascript.jscomp;

import org.junit.Test;

public class InlineVariablesTest extends CompilerTestCase {

  private InlineVariables.Mode mode = InlineVariables.Mode.ALL;
  private boolean inlineAllStrings = false;

  public InlineVariablesTest() {
    enableNormalize();
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    mode = InlineVariables.Mode.ALL;
    inlineAllStrings = false;
  }

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new InlineVariables(compiler, mode, inlineAllStrings);
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  @Test
  public void testModeConstantsOnly() {
    mode = InlineVariables.Mode.CONSTANTS_ONLY;
    test("var x = 1; var y = x + 1;", "var x = 1; var y = x + 1;");
    test("/** @const */ var X = 1; var y = X + 1;", "var y = 1 + 1;");
    test("var X = 1; var y = X + 1;", "var y = 1 + 1;");
  }

  @Test
  public void testModeLocalsOnly() {
    mode = InlineVariables.Mode.LOCALS_ONLY;
    test("var x = 1; var y = x + 1;", "var x = 1; var y = x + 1;");
    test("function f() { var x = 1; return x + 1; }",
         "function f() { return 1 + 1; }");
  }

  @Test
  public void testModeAll() {
    mode = InlineVariables.Mode.ALL;
    test("var x = 1; var y = x + 1;", "var y = 1 + 1;");
  }

  @Test
  public void testInlineAllStringsOption() {
    mode = InlineVariables.Mode.CONSTANTS_ONLY;
    inlineAllStrings = false;
    testSame("var A_LONG_STRING = 'abcdefghijklmnopqrstuvwxyz'; var x = A_LONG_STRING + A_LONG_STRING;");

    inlineAllStrings = true;
    test("var A_LONG_STRING = 'abcdefghijklmnopqrstuvwxyz'; var x = A_LONG_STRING + A_LONG_STRING;",
         "var x = 'abcdefghijklmnopqrstuvwxyz' + 'abcdefghijklmnopqrstuvwxyz';");
  }

  @Test
  public void testStringWorthInliningDefinedVar() {
    mode = InlineVariables.Mode.CONSTANTS_ONLY;
    inlineAllStrings = false;
    test("/** @define {string} */ var STR = 'short'; var x = STR;",
         "/** @define {string} */ var STR = 'short'; var x = 'short';");
  }

  @Test
  public void testInlineWellDefinedVariable() {
    test("var x = 5; var y = x; var z = x;", "var y = 5; var z = 5;");
    test("var x = true; if (x) { var y = x; }", "if (true) { var y = true; }");
    test("var x = null; var y = x;", "var y = null;");
  }

  @Test
  public void testInlineThisAlias() {
    test("function f() { var self = this; return self.foo() + self.bar(); }",
         "function f() { return this.foo() + this.bar(); }");
    testSame("function f() { var self = this; function g() { return self; } return g(); }");
  }

  @Test
  public void testInlineSingleReferenceLiteral() {
    test("var a = [1, 2]; var b = a;", "var b = [1, 2];");
    test("var a = {x: 1}; var b = a;", "var b = {x: 1};");
    test("var a = function() { return 1; }; var b = a();",
         "var b = (function() { return 1; })();");
  }

  @Test
  public void testInlineSingleReferenceModerateMotion() {
    test("var a = 1; var b = 2; var c = a + b;", "var b = 2; var c = 1 + b;");
    test("var a; a = 1; var b = a;", "var b = 1;");
  }

  @Test
  public void testCannotInlineAcrossModifications() {
    testSame("var a = 1; a++; var b = a;");
    testSame("var a = 1; a--; var b = a;");
    testSame("var a = 1; a += 2; var b = a;");
  }

  @Test
  public void testCannotInlineAcrossSideEffects() {
    testSame("var a = foo(); bar(); var b = a;");
    testSame("var a = obj.prop; modifyObj(); var b = a;");
  }

  @Test
  public void testDoNotInliningCallContextChange() {
    testSame("var a = b.c; a();");
    test("var a = b.c; f(a);", "f(b.c);");
  }

  @Test
  public void testDoNotInliningSubclassOrSingletonCalls() {
    testSame("var SubClass = function() {}; goog.inherits(SubClass, SuperClass);");
    testSame("var Foo = function() {}; goog.addSingletonGetter(Foo);");
  }

  @Test
  public void testAliasInlining() {
    test("function f(x) { var y = x; return y + y; }",
         "function f(x) { return x + x; }");
    test("function f(x) { var y = x; var z = y; return z; }",
         "function f(x) { return x; }");
  }

  @Test
  public void testArgumentsEscapedOrModified() {
    testSame("function f() { var a = arguments; return a; }");
    testSame("function f() { var a = arguments; a[0] = 1; return a; }");
    test("function f() { var a = arguments[0]; return a + 1; }",
         "function f() { return arguments[0] + 1; }");
  }

  @Test
  public void testUninitializedVariables() {
    test("var x; var y = x; var z = x;", "var y = void 0; var z = void 0;");
    test("function f() { var x; return x; }", "function f() { return void 0; }");
  }

  @Test
  public void testDeclarationWithOnlyAssignment() {
    test("var x; x = 1;", "1;");
  }

  @Test
  public void testForbiddenInlining() {
    testSame("var JSCompiler_RenameProperty = function(a) { return a; }; JSCompiler_RenameProperty('foo');");
  }

  @Test
  public void testDifferentBasicBlocks() {
    testSame("var x = 1; if (true) { var y = x; }");
    testSame("var x = 1; while (true) { var y = x; }");
  }

  @Test
  public void testForLoopsNotValidDeclaration() {
    testSame("for (var x = 1; x < 10; x++) { alert(x); }");
    testSame("for (var x in obj) { alert(x); }");
  }

  @Test
  public void testMultipleVarsInDeclaration() {
    test("var a = 1, b = 2; var c = a + b;", "var c = 1 + 2;");
    test("var a = 1, b = 2; var c = a;", "var b = 2; var c = 1;");
  }

  @Test
  public void testFunctionDeclarationInlining() {
    test("function f() {} var g = f;", "var g = function f() {};");
  }

  @Test
  public void testNonInlinableFunction() {
    testSame("function f() { if (true) { return 1; } } var g = f();");
  }

  @Test
  public void testImmutableConstantsWithDifferentValues() {
    test("var A = 1, B = 'hello', C = true, D = null, E = void 0; var res = A + B + C + D + E;",
         "var res = 1 + 'hello' + true + null + void 0;");
  }
}