package com.google.javascript.jscomp;

import org.junit.Test;

public class InlineObjectLiteralsTest extends CompilerTestCase {

  public InlineObjectLiteralsTest() {
    super();
  }

  @Override
  protected CompilerPass getProcessor(final Compiler compiler) {
    return new InlineObjectLiterals(
        compiler,
        compiler.getUniqueNameIdSupplier());
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  @Test
  public void testObject1() {
    test("function local(){ var a = {x: 1}; return a.x; }",
         "function local(){ var JSCompiler_object_inline_x_0 = 1; return JSCompiler_object_inline_x_0; }");
  }

  @Test
  public void testObject2() {
    test("function local(){ var a = {x: 1, y: 2}; return a.x + a.y; }",
         "function local(){ var JSCompiler_object_inline_x_0 = 1; var JSCompiler_object_inline_y_1 = 2; return JSCompiler_object_inline_x_0 + JSCompiler_object_inline_y_1; }");
  }

  @Test
  public void testObject3() {
    test("function local(){ var a = {x: 1}; a.x = 2; return a.x; }",
         "function local(){ var JSCompiler_object_inline_x_0 = 1; JSCompiler_object_inline_x_0 = 2; return JSCompiler_object_inline_x_0; }");
  }

  @Test
  public void testEmptyObject() {
    test("function local(){ var a = {}; return 1; }",
         "function local(){ return 1; }");
  }

  @Test
  public void testMultipleAssignments() {
    test("function local(){ var a = {x: 1}; a = {x: 2}; return a.x; }",
         "function local(){ var JSCompiler_object_inline_x_0 = 1; (JSCompiler_object_inline_x_0 = 2, true); return JSCompiler_object_inline_x_0; }");
  }

  @Test
  public void testMultipleKeysMultipleAssignments() {
    test("function local(){ var a = {x: 1, y: 2}; a = {x: 3, y: 4}; return a.x + a.y; }",
         "function local(){ var JSCompiler_object_inline_x_0 = 1; var JSCompiler_object_inline_y_1 = 2; (JSCompiler_object_inline_y_1 = 4, (JSCompiler_object_inline_x_0 = 3, true)); return JSCompiler_object_inline_x_0 + JSCompiler_object_inline_y_1; }");
  }

  @Test
  public void testReassignmentWithMissingKey() {
    test("function local(){ var a = {x: 1, y: 2}; a = {x: 3}; return a.x + a.y; }",
         "function local(){ var JSCompiler_object_inline_x_0 = 1; var JSCompiler_object_inline_y_1 = 2; (JSCompiler_object_inline_y_1 = void 0, (JSCompiler_object_inline_x_0 = 3, true)); return JSCompiler_object_inline_x_0 + JSCompiler_object_inline_y_1; }");
  }

  @Test
  public void testEmptyAssignment() {
    test("function local(){ var a = {x: 1}; a = {}; return a.x; }",
         "function local(){ var JSCompiler_object_inline_x_0 = 1; (JSCompiler_object_inline_x_0 = void 0, true); return JSCompiler_object_inline_x_0; }");
  }

  @Test
  public void testLatePropertyAssignment() {
    test("function local(){ var a = {x: 1}; a.y = 2; return a.x + a.y; }",
         "function local(){ var JSCompiler_object_inline_x_0 = 1; var JSCompiler_object_inline_y_1; JSCompiler_object_inline_y_1 = 2; return JSCompiler_object_inline_x_0 + JSCompiler_object_inline_y_1; }");
  }

  @Test
  public void testVarWithoutAssignment() {
    test("function local(){ var a; a = {x: 1}; return a.x; }",
         "function local(){ var JSCompiler_object_inline_x_0; (JSCompiler_object_inline_x_0 = 1, true); return JSCompiler_object_inline_x_0; }");
  }

  @Test
  public void testGlobalNotModified() {
    testSame("var a = {x: 1}; a.x;");
  }

  @Test
  public void testMethodCallBailsOut() {
    testSame("function local(){ var a = {x: function(){}}; a.x(); }");
  }

  @Test
  public void testEscapedObjectBailsOut() {
    testSame("function local(){ var a = {x: 1}; foo(a); }");
    testSame("function local(){ var a = {x: 1}; return a; }");
  }

  @Test
  public void testGetterSetterBailsOut() {
    testSame("function local(){ var a = { get x() { return 1; } }; return a.x; }");
    testSame("function local(){ var a = { set x(val) { } }; a.x = 1; }");
  }

  @Test
  public void testSelfReferentialAssignmentBailsOut() {
    testSame("function local(){ var a = {x: 1, y: a.x}; return a.y; }");
    testSame("function local(){ var a = {x: (a = {x: 1})}; return a.x; }");
  }

  @Test
  public void testNonObjectLitAssignmentBailsOut() {
    testSame("function local(){ var a = {x: 1}; a = 5; return a.x; }");
  }

  @Test
  public void testDeletedPropertyBailsOut() {
    testSame("function local(){ var a = {x: 1}; delete a.x; }");
  }

  @Test
  public void testUndefinedPropertyReadBailsOut() {
    testSame("function local(){ var a = {x: 1}; return a.y; }");
  }

  @Test
  public void testSpecialRenamePropertyForbidden() {
    testSame("function local(){ var JSCompiler_renameProperty = {x: 1}; return JSCompiler_renameProperty.x; }");
  }

  @Test
  public void testNestedFunctionBlacklist() {
    test("function local(){ var a = {x: 1}; var b = {y: a.x}; return b.y; }",
         "function local(){ var JSCompiler_object_inline_x_0 = 1; var JSCompiler_object_inline_y_1 = JSCompiler_object_inline_x_0; return JSCompiler_object_inline_y_1; }");
  }

  @Test
  public void testMultipleVarsInScope() {
    test("function local(){ var a = {x: 1}; var b = {y: 2}; return a.x + b.y; }",
         "function local(){ var JSCompiler_object_inline_x_0 = 1; var JSCompiler_object_inline_y_1 = 2; return JSCompiler_object_inline_x_0 + JSCompiler_object_inline_y_1; }");
  }
}