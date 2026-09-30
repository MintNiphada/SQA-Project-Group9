package com.google.javascript.jscomp;

import com.google.common.base.Supplier;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TargetClassTest extends CompilerTestCase {

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    enableNormalize();
  }

  @Override
  protected CompilerPass getProcessor(final Compiler compiler) {
    return new InlineObjectLiterals(
        compiler,
        compiler.getUniqueNameIdSupplier());
  }

  @Test
  public void testDirectPassExecution() {
    Compiler compiler = new Compiler();
    Supplier<String> idSupplier = new Supplier<String>() {
      private int id = 0;
      @Override
      public String get() {
        return String.valueOf(id++);
      }
    };
    InlineObjectLiterals pass = new InlineObjectLiterals(compiler, idSupplier);
    Node externs = IR.block();
    Node root = IR.block();
    pass.process(externs, root);
    Assert.assertNotNull(pass);
    Assert.assertEquals("JSCompiler_object_inline_", InlineObjectLiterals.VAR_PREFIX);
  }

  @Test
  public void testSimpleObjectInlining() {
    test(
        "function f() { var a = {x: 1, y: 2}; return a.x + a.y; }",
        "function f() {" +
        "  var JSCompiler_object_inline_x_0 = 1;" +
        "  var JSCompiler_object_inline_y_1 = 2;" +
        "  return JSCompiler_object_inline_x_0 + JSCompiler_object_inline_y_1;" +
        "}");
  }

  @Test
  public void testObjectWithNoProperties() {
    test(
        "function f() { var a = {}; return a.x; }",
        "function f() {" +
        "  var JSCompiler_object_inline_x_0;" +
        "  return JSCompiler_object_inline_x_0;" +
        "}");
  }

  @Test
  public void testUninitializedVarAssignedLater() {
    test(
        "function f() { var a; a = {x: 1}; return a.x; }",
        "function f() {" +
        "  var JSCompiler_object_inline_x_0;" +
        "  JSCompiler_object_inline_x_0 = 1, true;" +
        "  return JSCompiler_object_inline_x_0;" +
        "}");
  }

  @Test
  public void testReassignmentToObjectLiteral() {
    test(
        "function f() { var a = {x: 1}; a = {x: 2}; return a.x; }",
        "function f() {" +
        "  var JSCompiler_object_inline_x_0 = 1;" +
        "  JSCompiler_object_inline_x_0 = 2, true;" +
        "  return JSCompiler_object_inline_x_0;" +
        "}");
  }

  @Test
  public void testReassignmentWithMissingKeysSetsUndefined() {
    test(
        "function f() { var a = {x: 1, y: 2}; a = {x: 3}; return a.x + a.y; }",
        "function f() {" +
        "  var JSCompiler_object_inline_x_0 = 1;" +
        "  var JSCompiler_object_inline_y_1 = 2;" +
        "  JSCompiler_object_inline_x_0 = 3, JSCompiler_object_inline_y_1 = void 0, true;" +
        "  return JSCompiler_object_inline_x_0 + JSCompiler_object_inline_y_1;" +
        "}");
  }

  @Test
  public void testReassignmentToEmptyObjectLiteral() {
    test(
        "function f() { var a = {x: 1}; a = {}; return a.x; }",
        "function f() {" +
        "  var JSCompiler_object_inline_x_0 = 1;" +
        "  JSCompiler_object_inline_x_0 = void 0, true;" +
        "  return JSCompiler_object_inline_x_0;" +
        "}");
  }

  @Test
  public void testGlobalObjectNotForbiddenToRemainGlobal() {
    testSame("var a = {x: 1}; var b = a.x;");
  }

  @Test
  public void testObjectEscapesViaCallTarget() {
    testSame("function f() { var a = {fn: function() { return 1; }}; return a.fn(); }");
  }

  @Test
  public void testObjectEscapesViaFunctionArgument() {
    testSame("function f() { var a = {x: 1}; g(a); return a.x; }");
  }

  @Test
  public void testNonObjectLiteralAssignment() {
    testSame("function f() { var a = 5; return a; }");
    testSame("function f() { var a = {x: 1}; a = 5; return a.x; }");
  }

  @Test
  public void testGetterNotSupported() {
    testSame("function f() { var a = { get x() { return 1; } }; return a.x; }");
  }

  @Test
  public void testSetterNotSupported() {
    testSame("function f() { var a = { set x(val) { } }; return a.x; }");
  }

  @Test
  public void testSelfReferentialAssignmentExcluded() {
    testSame("function f() { var a = {x: 1, y: a.x}; return a.y; }");
  }

  @Test
  public void testRenamePropertyFunctionNotForbidden() {
    testSame("function f() { var JSCompiler_renameProperty = {x: 1}; return JSCompiler_renameProperty.x; }");
  }

  @Test
  public void testMultipleObjectsInSameScope() {
    test(
        "function f() {" +
        "  var a = {x: 1};" +
        "  var b = {y: 2};" +
        "  return a.x + b.y;" +
        "}",
        "function f() {" +
        "  var JSCompiler_object_inline_x_0 = 1;" +
        "  var JSCompiler_object_inline_y_1 = 2;" +
        "  return JSCompiler_object_inline_x_0 + JSCompiler_object_inline_y_1;" +
        "}");
  }

  @Test
  public void testObjectReferencedInsideNestedFunction() {
    test(
        "function f() {" +
        "  var a = {x: 1};" +
        "  function g() { return a.x; }" +
        "  return g();" +
        "}",
        "function f() {" +
        "  var JSCompiler_object_inline_x_0 = 1;" +
        "  function g() { return JSCompiler_object_inline_x_0; }" +
        "  return g();" +
        "}");
  }

  @Test
  public void testObjectWithThreeAssignments() {
    test(
        "function f() {" +
        "  var a = {x: 1, y: 2, z: 3};" +
        "  a = {x: 4, y: 5, z: 6};" +
        "  return a.x + a.y + a.z;" +
        "}",
        "function f() {" +
        "  var JSCompiler_object_inline_x_0 = 1;" +
        "  var JSCompiler_object_inline_y_1 = 2;" +
        "  var JSCompiler_object_inline_z_2 = 3;" +
        "  JSCompiler_object_inline_x_0 = 4, " +
        "  JSCompiler_object_inline_y_1 = 5, " +
        "  JSCompiler_object_inline_z_2 = 6, true;" +
        "  return JSCompiler_object_inline_x_0 + JSCompiler_object_inline_y_1 + JSCompiler_object_inline_z_2;" +
        "}");
  }
}