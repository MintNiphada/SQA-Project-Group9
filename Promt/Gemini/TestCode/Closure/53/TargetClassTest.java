package com.google.javascript.jscomp;

import org.junit.Test;

public class TargetClassTest extends CompilerTestCase {

  @Override
  protected CompilerPass getProcessor(final Compiler compiler) {
    return new InlineObjectLiterals(
        compiler,
        compiler.getUniqueNameIdSupplier());
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    enableNormalize();
  }

  @Test
  public void testSimpleInline() {
    test("function f() { var x = {a: 1}; return x.a; }",
         "function f() { var JSCompiler_object_inline_a_0 = 1; return JSCompiler_object_inline_a_0; }");
  }

  @Test
  public void testMultiplePropertiesInline() {
    test("function f() { var x = {a: 1, b: 2}; return x.a + x.b; }",
         "function f() { var JSCompiler_object_inline_a_0 = 1; var JSCompiler_object_inline_b_1 = 2; " +
         "return JSCompiler_object_inline_a_0 + JSCompiler_object_inline_b_1; }");
  }

  @Test
  public void testReassignment() {
    test("function f() { var x = {a: 1}; x = {a: 2}; return x.a; }",
         "function f() { var JSCompiler_object_inline_a_0 = 1; " +
         "JSCompiler_object_inline_a_0 = 2, true; return JSCompiler_object_inline_a_0; }");
  }

  @Test
  public void testReassignmentWithDifferentKeys() {
    test("function f() { var x = {a: 1}; x = {b: 2}; return x.a + x.b; }",
         "function f() { var JSCompiler_object_inline_a_0 = 1; var JSCompiler_object_inline_b_1 = void 0; " +
         "JSCompiler_object_inline_b_1 = 2, JSCompiler_object_inline_a_0 = void 0, true; " +
         "return JSCompiler_object_inline_a_0 + JSCompiler_object_inline_b_1; }");
  }

  @Test
  public void testUninitializedVar() {
    test("function f() { var x; x = {a: 1}; return x.a; }",
         "function f() { var JSCompiler_object_inline_a_0 = void 0; " +
         "JSCompiler_object_inline_a_0 = 1, true; return JSCompiler_object_inline_a_0; }");
  }

  @Test
  public void testGlobalNotForbidden() {
    testSame("var x = {a: 1}; var y = x.a;");
  }

  @Test
  public void testMethodCallNotForbidden() {
    testSame("function f() { var x = {a: function() {}}; x.a(); }");
  }

  @Test
  public void testPassedAsArgument() {
    testSame("function f() { var x = {a: 1}; g(x); }");
  }

  @Test
  public void testGetterNotSupported() {
    testSame("function f() { var x = { get a() { return 1; } }; return x.a; }");
  }

  @Test
  public void testSetterNotSupported() {
    testSame("function f() { var x = { set a(v) { } }; x.a = 1; }");
  }

  @Test
  public void testSelfReferentialAssignment() {
    testSame("function f() { var x = {a: 1, b: x.a}; }");
  }

  @Test
  public void testNonObjectAssignment() {
    testSame("function f() { var x = {a: 1}; x = 5; return x.a; }");
  }

  @Test
  public void testRenamePropertyFunctionNotForbidden() {
    testSame("function f() { var JSCompiler_renameProperty = {a: 1}; return JSCompiler_renameProperty.a; }");
  }

  @Test
  public void testMultipleAssignmentsWithMissingProperty() {
    test("function f() { var x = {a: 1, b: 2}; x = {a: 3}; return x.a + x.b; }",
         "function f() { var JSCompiler_object_inline_a_0 = 1; var JSCompiler_object_inline_b_1 = 2; " +
         "JSCompiler_object_inline_a_0 = 3, JSCompiler_object_inline_b_1 = void 0, true; " +
         "return JSCompiler_object_inline_a_0 + JSCompiler_object_inline_b_1; }");
  }

  @Test
  public void testNestedFunctionBlacklist() {
    test("function f() { var a = 1; var x = {b: a}; return x.b; }",
         "function f() { var a = 1; var JSCompiler_object_inline_b_0 = a; return JSCompiler_object_inline_b_0; }");
  }
}