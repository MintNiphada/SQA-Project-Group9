package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;

public class CollapsePropertiesTest extends CompilerTestCase {

  private boolean enableCollapsePropertiesOnExternTypes = false;
  private boolean inlineAliases = true;

  public CollapsePropertiesTest() {
    super();
  }

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new CollapseProperties(
        compiler, enableCollapsePropertiesOnExternTypes, inlineAliases);
  }

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    enableCollapsePropertiesOnExternTypes = false;
    inlineAliases = true;
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  @Test
  public void testBasicCollapse() {
    test("var a = {}; a.b = 1;", "var a$b = 1;");
  }

  @Test
  public void testNestedCollapse() {
    test("var a = {}; a.b = {}; a.b.c = 1;", "var a$b$c = 1;");
  }

  @Test
  public void testObjLitCollapse() {
    test("var a = {b: 1, c: 2};", "var a$b = 1; var a$c = 2;");
  }

  @Test
  public void testObjLitWithFunction() {
    test("var a = {b: function() { return 1; }};",
         "var a$b = function() { return 1; };");
  }

  @Test
  public void testFunctionPropertyCollapse() {
    test("function a() {} a.b = 1;", "function a() {} var a$b = 1;");
  }

  @Test
  public void testPropertyWithDollarSign() {
    test("var a = {}; a.b$c = 1;", "var a$b$0c = 1;");
  }

  @Test
  public void testNamespaceRedefinedWarningGlobal() {
    testWarning("var a = {}; a = {};",
        CollapseProperties.NAMESPACE_REDEFINED_WARNING);
  }

  @Test
  public void testNamespaceRedefinedWarningLocal() {
    testWarning("var a = {}; function f() { a = {}; }",
        CollapseProperties.NAMESPACE_REDEFINED_WARNING);
  }

  @Test
  public void testNamespaceAliasingWarning() {
    testWarning("var a = {}; var b = a; b.c = 1;",
        CollapseProperties.UNSAFE_NAMESPACE_WARNING);
  }

  @Test
  public void testUnsafeThisWarning() {
    testWarning("var a = {}; a.b = function() { return this.c; };",
        CollapseProperties.UNSAFE_THIS);
  }

  @Test
  public void testSafeThisConstructor() {
    testSame("var a = {}; /** @constructor */ a.b = function() { this.c = 1; };");
  }

  @Test
  public void testSafeThisDocInfo() {
    testSame("var a = {}; /** @this {Object} */ a.b = function() { return this.c; };");
  }

  @Test
  public void testStubForLateProperty() {
    test("var a = {}; function f() { a.b = 1; }",
         "var a = {}; var a$b; function f() { a$b = 1; }");
  }

  @Test
  public void testInlineAliasesSimple() {
    inlineAliases = true;
    test("var a = {b: 1}; function f() { var x = a; return x.b; }",
         "var a$b = 1; function f() { var x = null; return a$b; }");
  }

  @Test
  public void testInlineAliasesDisabled() {
    inlineAliases = false;
    testSame("var a = {b: 1}; function f() { var x = a; return x.b; }");
  }

  @Test
  public void testCollapseOnExternTypes() {
    enableCollapsePropertiesOnExternTypes = true;
    test("String.foo = 1; var x = String.foo;",
         "var String$foo = 1; var x = String$foo;");
  }

  @Test
  public void testComplexAssignmentTwinRef() {
    test("var a = {}; var x; x = a.b = 1;",
         "var a = {}; var a$b; var x; x = a$b = 1;");
  }

  @Test
  public void testNonCollapsiblePropertyAccess() {
    testSame("var a = {}; var k = 'b'; a[k] = 1;");
  }

  @Test
  public void testObjLitNumericKey() {
    test("var a = {1: 'num'};", "var a$1 = 'num';");
  }

  @Test
  public void testObjLitInvalidIdentifierKey() {
    test("var a = {'default': 1, 'var': 2};",
         "var a$default = 1; var a$var = 2;");
  }

  @Test
  public void testConstantAnnotationPreserved() {
    test("var a = {}; /** @const */ a.B = 1;",
         "/** @const */ var a$B = 1;");
  }

  @Test
  public void testEliminateObjectLiteral() {
    test("var a = {b: 1}; var x = a.b;",
         "var a$b = 1; var x = a$b;");
  }

  @Test
  public void testDoNotEliminateIfAliased() {
    testSame("var a = {b: 1}; var c = a;");
  }
}