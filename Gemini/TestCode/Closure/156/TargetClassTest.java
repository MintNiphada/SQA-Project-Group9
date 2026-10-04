package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Assert;
import org.junit.Test;

public class CollapsePropertiesTest {

  private Compiler compiler;

  private Node test(String js, boolean collapseExterns, boolean inlineAliases) {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    Node externsNode = Node.newString("externs");
    Node rootNode = compiler.parseTestCode(js);
    CollapseProperties pass = new CollapseProperties(compiler, collapseExterns, inlineAliases);
    pass.process(externsNode, rootNode);
    return rootNode;
  }

  private Node test(String js) {
    return test(js, false, true);
  }

  @Test
  public void testCollapseSimpleObjectLiteral() {
    test("var a = {b: 1}; var c = a.b;");
    Assert.assertEquals(0, compiler.getErrorCount());
    Assert.assertEquals(0, compiler.getWarningCount());
    String output = compiler.toSource();
    Assert.assertTrue(output.contains("var a$b = 1;"));
    Assert.assertTrue(output.contains("var c = a$b;"));
  }

  @Test
  public void testCollapseNestedObjectLiteral() {
    test("var a = {b: {c: 2}}; var d = a.b.c;");
    Assert.assertEquals(0, compiler.getErrorCount());
    Assert.assertEquals(0, compiler.getWarningCount());
    String output = compiler.toSource();
    Assert.assertTrue(output.contains("var a$b$c = 2;"));
    Assert.assertTrue(output.contains("var d = a$b$c;"));
  }

  @Test
  public void testCollapseFunctionProperties() {
    test("function a() {} a.b = 1; var c = a.b;");
    Assert.assertEquals(0, compiler.getErrorCount());
    Assert.assertEquals(0, compiler.getWarningCount());
    String output = compiler.toSource();
    Assert.assertTrue(output.contains("var a$b = 1;"));
    Assert.assertTrue(output.contains("var c = a$b;"));
  }

  @Test
  public void testCollapseObjectLiteralWithDollarSign() {
    test("var a = {b$c: 1}; var d = a.b$c;");
    Assert.assertEquals(0, compiler.getErrorCount());
    Assert.assertEquals(0, compiler.getWarningCount());
    String output = compiler.toSource();
    Assert.assertTrue(output.contains("var a$b$0c = 1;"));
    Assert.assertTrue(output.contains("var d = a$b$0c;"));
  }

  @Test
  public void testCollapseNonIdentifierKeyInObjectLiteral() {
    test("var a = {'foo bar': 1};");
    Assert.assertEquals(0, compiler.getErrorCount());
    Assert.assertEquals(0, compiler.getWarningCount());
    String output = compiler.toSource();
    Assert.assertTrue(output.contains("var a$1 = 1;"));
  }

  @Test
  public void testGetterSetterProperty() {
    test("var a = { get b() { return 1; }, set b(v) {} };");
    Assert.assertEquals(0, compiler.getErrorCount());
    Assert.assertEquals(0, compiler.getWarningCount());
  }

  @Test
  public void testUnsafeNamespaceWarningOnAlias() {
    test("var a = {b: 1}; var c = a; c.b;");
    Assert.assertEquals(1, compiler.getWarningCount());
    JSError warning = compiler.getWarnings()[0];
    Assert.assertEquals(CollapseProperties.UNSAFE_NAMESPACE_WARNING.key, warning.getType().key);
  }

  @Test
  public void testNamespaceRedefinedWarning() {
    test("var a = {b: 1}; a = {c: 2};");
    Assert.assertEquals(1, compiler.getWarningCount());
    JSError warning = compiler.getWarnings()[0];
    Assert.assertEquals(CollapseProperties.NAMESPACE_REDEFINED_WARNING.key, warning.getType().key);
  }

  @Test
  public void testUnsafeThisWarning() {
    test("var a = {}; a.b = function() { return this.x; };");
    Assert.assertEquals(1, compiler.getWarningCount());
    JSError warning = compiler.getWarnings()[0];
    Assert.assertEquals(CollapseProperties.UNSAFE_THIS.key, warning.getType().key);
  }

  @Test
  public void testSafeThisConstructor() {
    test("var a = {}; /** @constructor */ a.b = function() { this.x = 1; };");
    Assert.assertEquals(0, compiler.getWarningCount());
  }

  @Test
  public void testSafeThisWithDoc() {
    test("var a = {}; /** @this {Object} */ a.b = function() { return this.x; };");
    Assert.assertEquals(0, compiler.getWarningCount());
  }

  @Test
  public void testInlineLocalAlias() {
    test("var a = {b: 1}; function f() { var x = a; return x.b; }", false, true);
    Assert.assertEquals(0, compiler.getErrorCount());
    String output = compiler.toSource();
    Assert.assertTrue(output.contains("var a$b = 1;"));
  }

  @Test
  public void testDoNotInlineAliasesWhenDisabled() {
    test("var a = {b: 1}; function f() { var x = a; return x.b; }", false, false);
    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testStubsForPropertiesAssignedInLocalScope() {
    test("var a = {}; function f() { a.b = 1; }");
    Assert.assertEquals(0, compiler.getErrorCount());
    String output = compiler.toSource();
    Assert.assertTrue(output.contains("var a$b;"));
  }

  @Test
  public void testComplexAssignmentTwinReferences() {
    test("var a = {}; var b; b = (a.c = 1);");
    Assert.assertEquals(0, compiler.getErrorCount());
    String output = compiler.toSource();
    Assert.assertTrue(output.contains("var a$c;"));
  }

  @Test
  public void testCollapseOnExternTypes() {
    test("String.foo = 1; var x = String.foo;", true, true);
    Assert.assertEquals(0, compiler.getErrorCount());
    String output = compiler.toSource();
    Assert.assertTrue(output.contains("var String$foo = 1;"));
  }

  @Test
  public void testNoCollapseOnExternTypesWhenDisabled() {
    test("String.foo = 1; var x = String.foo;", false, true);
    Assert.assertEquals(0, compiler.getErrorCount());
    String output = compiler.toSource();
    Assert.assertTrue(output.contains("String.foo = 1;"));
  }

  @Test
  public void testAssignDeclarationNotSimpleName() {
    test("var a = {}; a.b = {}; a.b.c = 1; var d = a.b.c;");
    Assert.assertEquals(0, compiler.getErrorCount());
    String output = compiler.toSource();
    Assert.assertTrue(output.contains("var a$b$c = 1;"));
    Assert.assertTrue(output.contains("var d = a$b$c;"));
  }

  @Test
  public void testConstantPropertyPreserved() {
    test("var a = {}; /** @const */ a.B = 1; var c = a.B;");
    Assert.assertEquals(0, compiler.getErrorCount());
    String output = compiler.toSource();
    Assert.assertTrue(output.contains("var a$B = 1;"));
    Assert.assertTrue(output.contains("var c = a$B;"));
  }

  @Test
  public void testMultiplePropertiesOnFunction() {
    test("function f() {} f.a = 1; f.b = 2; var x = f.a + f.b;");
    Assert.assertEquals(0, compiler.getErrorCount());
    String output = compiler.toSource();
    Assert.assertTrue(output.contains("var f$a = 1;"));
    Assert.assertTrue(output.contains("var f$b = 2;"));
    Assert.assertTrue(output.contains("var x = f$a + f$b;"));
  }
}
