package com.google.javascript.jscomp;

import com.google.common.collect.Lists;
import com.google.javascript.rhino.Node;
import org.junit.Assert;
import org.junit.Test;

public class TargetClassTest {

  private Compiler testCompiler(String externs, String js,
                                boolean collapseExterns, boolean inlineAliases) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT5);
    compiler.init(
        Lists.newArrayList(SourceFile.fromCode("externs.js", externs)),
        Lists.newArrayList(SourceFile.fromCode("testcode.js", js)),
        options);
    Node root = compiler.parseInputs();
    Assert.assertNotNull(root);
    Node externsNode = root.getFirstChild();
    Node mainRoot = root.getLastChild();

    CollapseProperties pass = new CollapseProperties(compiler, collapseExterns, inlineAliases);
    pass.process(externsNode, mainRoot);
    return compiler;
  }

  private void test(String js, String expected) {
    test("", js, expected, false, true);
  }

  private void test(String externs, String js, String expected,
                    boolean collapseExterns, boolean inlineAliases) {
    Compiler compiler = testCompiler(externs, js, collapseExterns, inlineAliases);
    Assert.assertEquals("Unexpected compiler errors: " + Lists.newArrayList(compiler.getErrors()),
        0, compiler.getErrorCount());

    if (expected != null) {
      Compiler expectedCompiler = new Compiler();
      CompilerOptions options = new CompilerOptions();
      options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT5);
      expectedCompiler.init(
          Lists.newArrayList(SourceFile.fromCode("externs.js", externs)),
          Lists.newArrayList(SourceFile.fromCode("expected.js", expected)),
          options);
      expectedCompiler.parseInputs();
      String expectedSource = expectedCompiler.toSource();
      String actualSource = compiler.toSource();
      Assert.assertEquals(expectedSource, actualSource);
    }
  }

  private void testWarning(String js, DiagnosticType warning) {
    Compiler compiler = testCompiler("", js, false, true);
    Assert.assertTrue("Expected warning: " + warning.key, compiler.getWarningCount() > 0);
    boolean found = false;
    for (JSError error : compiler.getWarnings()) {
      if (error.getType().equals(warning)) {
        found = true;
        break;
      }
    }
    Assert.assertTrue("Expected warning type " + warning.key + " not found among: " +
        Lists.newArrayList(compiler.getWarnings()), found);
  }

  @Test
  public void testCollapseSimpleProperty() {
    test("var a = {}; a.b = 1; var c = a.b;",
         "var a$b = 1; var c = a$b;");
  }

  @Test
  public void testCollapseObjectLiteral() {
    test("var a = {b: 1, c: 2}; var d = a.b + a.c;",
         "var a$b = 1; var a$c = 2; var d = a$b + a$c;");
  }

  @Test
  public void testCollapseNestedObjectLiteral() {
    test("var a = {b: {c: 1}}; var d = a.b.c;",
         "var a$b$c = 1; var d = a$b$c;");
  }

  @Test
  public void testCollapseFunctionProperty() {
    test("function a() {} a.b = 1; var c = a.b;",
         "function a() {} var a$b = 1; var c = a$b;");
  }

  @Test
  public void testCallFlatteningFreeCall() {
    test("var a = {b: function() { return 1; }}; var c = a.b();",
         "var a$b = function() { return 1; }; var c = a$b();");
  }

  @Test
  public void testPropertyWithDollarSign() {
    test("var a = {}; a.b$c = 1; var d = a.b$c;",
         "var a$b$0c = 1; var d = a$b$0c;");
  }

  @Test
  public void testInlineAliasesInLocalScope() {
    test("var a = {b: 1}; function f() { var x = a; return x.b; }",
         "var a$b = 1; function f() { var x = null; return a$b; }");
  }

  @Test
  public void testStubForUndeclaredProperties() {
    test("var a = {}; function f() { a.b = 1; }",
         "var a = {}; var a$b; function f() { a$b = 1; }");
  }

  @Test
  public void testSimpleStubDeclaration() {
    test("var a = {}; a.b;",
         "var a = {}; var a$b;");
  }

  @Test
  public void testComplexAssignment() {
    test("var a = {}; var x = (a.b = 1);",
         "var a = {}; var a$b; var x = (a$b = 1);");
  }

  @Test
  public void testUnsafeNamespaceWarning() {
    testWarning("var a = {}; a.b = 1; var c = a; c.b = 2;",
        CollapseProperties.UNSAFE_NAMESPACE_WARNING);
  }

  @Test
  public void testNamespaceRedefinedWarning() {
    testWarning("var a = {}; a.b = 1; a = {};",
        CollapseProperties.NAMESPACE_REDEFINED_WARNING);
  }

  @Test
  public void testNamespaceDeletedWarning() {
    testWarning("var a = {}; a.b = 1; delete a;",
        CollapseProperties.NAMESPACE_REDEFINED_WARNING);
  }

  @Test
  public void testUnsafeThisWarning() {
    testWarning("var a = {}; a.b = function() { return this.x; };",
        CollapseProperties.UNSAFE_THIS);
  }

  @Test
  public void testNonJsIdentifierKeys() {
    test("var a = { '1': 10, 'foo-bar': 20 };",
         "var a$1 = 10; var a$2 = 20;");
  }

  @Test
  public void testGettersAndSettersNotCollapsed() {
    test("var a = { get b() { return 1; }, set b(x) {} };",
         "var a = { get b() { return 1; }, set b(x) {} };");
  }

  @Test
  public void testCollapsePropertiesOnExternTypes() {
    test("String.foo = 1;", "String.foo = 1; var String$foo = 1;", "String.foo = 1;", true, true);
  }

  @Test
  public void testConstantAnnotationPreserved() {
    test("/** @const */ var a = {}; /** @const */ a.B = 1; var c = a.B;",
         "/** @const */ var a$B = 1; var c = a$B;");
  }
}