package com.google.javascript.jscomp;

import static com.google.javascript.rhino.jstype.JSTypeNative.ARRAY_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.BOOLEAN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NO_OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NULL_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NUMBER_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.STRING_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.UNKNOWN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.VOID_TYPE;

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfoBuilder;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Iterator;

public class TypeValidatorTest {

  private Compiler compiler;
  private JSTypeRegistry registry;
  private TypeValidator validator;
  private NodeTraversal traversal;
  private Node dummyNode;

  @Before
  public void setUp() {
    compiler = new Compiler();
    compiler.initCompilerOptionsIfTesting();
    registry = compiler.getTypeRegistry();
    validator = new TypeValidator(compiler);
    traversal = new NodeTraversal(compiler, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {}
    });
    dummyNode = IR.name("testNode");
    dummyNode.setLineno(1);
  }

  @Test
  public void testTypeMismatchEqualsAndHashCode() {
    JSType num = registry.getNativeType(NUMBER_TYPE);
    JSType str = registry.getNativeType(STRING_TYPE);
    JSType bool = registry.getNativeType(BOOLEAN_TYPE);

    TypeValidator.TypeMismatch mismatch1 = new TypeValidator.TypeMismatch(num, str, null);
    TypeValidator.TypeMismatch mismatch2 = new TypeValidator.TypeMismatch(str, num, null);
    TypeValidator.TypeMismatch mismatch3 = new TypeValidator.TypeMismatch(num, bool, null);

    Assert.assertEquals(mismatch1, mismatch2);
    Assert.assertEquals(mismatch2, mismatch1);
    Assert.assertNotEquals(mismatch1, mismatch3);
    Assert.assertNotEquals(mismatch1, null);
    Assert.assertNotEquals(mismatch1, "someString");

    Assert.assertEquals(mismatch1.hashCode(), mismatch2.hashCode());
    Assert.assertNotNull(mismatch1.toString());
  }

  @Test
  public void testSetShouldReport() {
    validator.setShouldReport(false);
    validator.expectValidTypeofName(traversal, dummyNode, "customType");
    Assert.assertEquals(0, compiler.getWarningCount());
    Assert.assertEquals(0, compiler.getErrorCount());

    validator.setShouldReport(true);
    validator.expectValidTypeofName(traversal, dummyNode, "customType");
    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testExpectObject() {
    JSType obj = registry.getNativeType(OBJECT_TYPE);
    JSType num = registry.getNativeType(NUMBER_TYPE);

    Assert.assertTrue(validator.expectObject(traversal, dummyNode, obj, "must be object"));
    Assert.assertEquals(0, compiler.getWarningCount());

    Assert.assertFalse(validator.expectObject(traversal, dummyNode, num, "must be object"));
    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testExpectActualObject() {
    JSType obj = registry.getNativeType(OBJECT_TYPE);
    JSType num = registry.getNativeType(NUMBER_TYPE);

    validator.expectActualObject(traversal, dummyNode, obj, "actual object");
    Assert.assertEquals(0, compiler.getWarningCount());

    validator.expectActualObject(traversal, dummyNode, num, "actual object");
    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testExpectAnyObject() {
    JSType obj = registry.getNativeType(OBJECT_TYPE);
    JSType num = registry.getNativeType(NUMBER_TYPE);
    JSType empty = registry.getNativeType(NO_OBJECT_TYPE);

    validator.expectAnyObject(traversal, dummyNode, obj, "any object");
    Assert.assertEquals(0, compiler.getWarningCount());

    validator.expectAnyObject(traversal, dummyNode, empty, "any object");
    Assert.assertEquals(0, compiler.getWarningCount());

    validator.expectAnyObject(traversal, dummyNode, num, "any object");
    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testExpectString() {
    JSType str = registry.getNativeType(STRING_TYPE);
    JSType obj = registry.getNativeType(OBJECT_TYPE);

    validator.expectString(traversal, dummyNode, str, "msg");
    Assert.assertEquals(0, compiler.getWarningCount());

    validator.expectString(traversal, dummyNode, obj, "msg");
    Assert.assertEquals(0, compiler.getWarningCount()); // Object matches string context via toString
  }

  @Test
  public void testExpectNumber() {
    JSType num = registry.getNativeType(NUMBER_TYPE);
    JSType bool = registry.getNativeType(BOOLEAN_TYPE);

    validator.expectNumber(traversal, dummyNode, num, "msg");
    Assert.assertEquals(0, compiler.getWarningCount());

    validator.expectNumber(traversal, dummyNode, bool, "msg");
    Assert.assertEquals(0, compiler.getWarningCount()); // Boolean matches number context
  }

  @Test
  public void testExpectBitwiseable() {
    JSType num = registry.getNativeType(NUMBER_TYPE);
    JSType str = registry.getNativeType(STRING_TYPE);
    JSType obj = registry.getNativeType(OBJECT_TYPE);

    validator.expectBitwiseable(traversal, dummyNode, num, "msg");
    Assert.assertEquals(0, compiler.getWarningCount());

    validator.expectBitwiseable(traversal, dummyNode, str, "msg");
    Assert.assertEquals(0, compiler.getWarningCount());

    validator.expectBitwiseable(traversal, dummyNode, obj, "msg");
    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testExpectStringOrNumber() {
    JSType num = registry.getNativeType(NUMBER_TYPE);
    JSType str = registry.getNativeType(STRING_TYPE);
    JSType obj = registry.getNativeType(OBJECT_TYPE);

    validator.expectStringOrNumber(traversal, dummyNode, num, "msg");
    Assert.assertEquals(0, compiler.getWarningCount());

    validator.expectStringOrNumber(traversal, dummyNode, str, "msg");
    Assert.assertEquals(0, compiler.getWarningCount());

    validator.expectStringOrNumber(traversal, dummyNode, obj, "msg");
    Assert.assertEquals(0, compiler.getWarningCount());
  }

  @Test
  public void testExpectNotNullOrUndefined() {
    JSType num = registry.getNativeType(NUMBER_TYPE);
    JSType nullType = registry.getNativeType(NULL_TYPE);
    JSType voidType = registry.getNativeType(VOID_TYPE);
    JSType unknown = registry.getNativeType(UNKNOWN_TYPE);

    Assert.assertTrue(validator.expectNotNullOrUndefined(traversal, dummyNode, num, "msg", num));
    Assert.assertTrue(validator.expectNotNullOrUndefined(traversal, dummyNode, unknown, "msg", num));
    Assert.assertFalse(validator.expectNotNullOrUndefined(traversal, dummyNode, nullType, "msg", num));
    Assert.assertFalse(validator.expectNotNullOrUndefined(traversal, dummyNode, voidType, "msg", num));

    // Special case: getprop in non-global scope
    Node getPropNode = IR.getprop(IR.name("this"), IR.string("x"));
    getPropNode.setLineno(1);
    Node fnNode = IR.function(IR.name("fn"), IR.paramList(), IR.block(IR.exprResult(getPropNode)));
    NodeTraversal innerTraversal = new NodeTraversal(compiler, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {}
    });
    innerTraversal.traverse(fnNode);
    // Directly testing getprop edge case behavior
    Assert.assertTrue(validator.expectNotNullOrUndefined(innerTraversal, getPropNode, nullType, "msg", num));
  }

  @Test
  public void testExpectSwitchMatchesCase() {
    Node caseNode = IR.caseNode(dummyNode, IR.block());
    Node switchNode = new Node(Token.SWITCH, IR.name("x"), caseNode);

    JSType num = registry.getNativeType(NUMBER_TYPE);
    JSType str = registry.getNativeType(STRING_TYPE);

    validator.expectSwitchMatchesCase(traversal, switchNode, num, num);
    Assert.assertEquals(0, compiler.getWarningCount());

    validator.expectSwitchMatchesCase(traversal, switchNode, num, str);
    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testExpectIndexMatch() {
    Node objNode = IR.name("arr");
    Node indexNode = IR.number(0);
    Node getElemNode = IR.getelem(objNode, indexNode);

    JSType arrayType = registry.getNativeType(ARRAY_TYPE);
    JSType num = registry.getNativeType(NUMBER_TYPE);
    JSType str = registry.getNativeType(STRING_TYPE);
    JSType unknown = registry.getNativeType(UNKNOWN_TYPE);

    // Array access with number index
    validator.expectIndexMatch(traversal, getElemNode, arrayType, num);
    Assert.assertEquals(0, compiler.getWarningCount());

    // Array access with string index (warning)
    validator.expectIndexMatch(traversal, getElemNode, arrayType, str);
    Assert.assertEquals(1, compiler.getWarningCount());

    // Unknown object access
    validator.expectIndexMatch(traversal, getElemNode, unknown, str);
    Assert.assertEquals(1, compiler.getWarningCount());

    // Object type access with string index
    JSType objType = registry.getNativeType(OBJECT_TYPE);
    validator.expectIndexMatch(traversal, getElemNode, objType, str);
    Assert.assertEquals(1, compiler.getWarningCount());

    // Struct type access error
    ObjectType structType = registry.createObjectType("MyStruct", null);
    structType.setStruct();
    validator.expectIndexMatch(traversal, getElemNode, structType, str);
    Assert.assertTrue(compiler.getWarningCount() >= 2);
  }

  @Test
  public void testExpectCanAssignToPropertyOf() {
    JSType num = registry.getNativeType(NUMBER_TYPE);
    JSType str = registry.getNativeType(STRING_TYPE);
    Node owner = IR.name("obj");
    owner.setJSType(registry.getNativeType(OBJECT_TYPE));

    Assert.assertTrue(validator.expectCanAssignToPropertyOf(traversal, dummyNode, num, num, owner, "p"));
    Assert.assertFalse(validator.expectCanAssignToPropertyOf(traversal, dummyNode, str, num, owner, "p"));
  }

  @Test
  public void testExpectCanAssignTo() {
    JSType num = registry.getNativeType(NUMBER_TYPE);
    JSType str = registry.getNativeType(STRING_TYPE);

    Assert.assertTrue(validator.expectCanAssignTo(traversal, dummyNode, num, num, "assignment"));
    Assert.assertFalse(validator.expectCanAssignTo(traversal, dummyNode, str, num, "assignment"));

    Iterator<TypeValidator.TypeMismatch> mismatches = validator.getMismatches().iterator();
    Assert.assertTrue(mismatches.hasNext());
    TypeValidator.TypeMismatch mismatch = mismatches.next();
    Assert.assertEquals(str, mismatch.typeA);
    Assert.assertEquals(num, mismatch.typeB);
  }

  @Test
  public void testExpectArgumentMatchesParameter() {
    JSType num = registry.getNativeType(NUMBER_TYPE);
    JSType str = registry.getNativeType(STRING_TYPE);
    Node callNode = IR.call(IR.name("fn"), dummyNode);

    validator.expectArgumentMatchesParameter(traversal, dummyNode, num, num, callNode, 1);
    Assert.assertEquals(0, compiler.getWarningCount());

    validator.expectArgumentMatchesParameter(traversal, dummyNode, str, num, callNode, 1);
    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testExpectCanOverride() {
    JSType num = registry.getNativeType(NUMBER_TYPE);
    JSType str = registry.getNativeType(STRING_TYPE);
    JSType owner = registry.getNativeType(OBJECT_TYPE);

    validator.expectCanOverride(traversal, dummyNode, num, num, "prop", owner);
    Assert.assertEquals(0, compiler.getWarningCount());

    validator.expectCanOverride(traversal, dummyNode, str, num, "prop", owner);
    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testExpectCanCast() {
    JSType num = registry.getNativeType(NUMBER_TYPE);
    JSType str = registry.getNativeType(STRING_TYPE);

    validator.expectCanCast(traversal, dummyNode, num, num);
    Assert.assertEquals(0, compiler.getWarningCount());

    validator.expectCanCast(traversal, dummyNode, num, str);
    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testGetReadableJSTypeName() {
    Node simpleName = IR.name("myVar");
    simpleName.setJSType(registry.getNativeType(NUMBER_TYPE));
    Assert.assertEquals("myVar", validator.getReadableJSTypeName(simpleName, false));

    Node getProp = IR.getprop(IR.name("a"), IR.string("b"));
    getProp.setJSType(registry.getNativeType(STRING_TYPE));
    Assert.assertNotNull(validator.getReadableJSTypeName(getProp, true));

    Node fnNode = IR.name("myFn");
    fnNode.setJSType(registry.getNativeType(OBJECT_TYPE));
    Assert.assertNotNull(validator.getReadableJSTypeName(fnNode, false));
  }

  @Test
  public void testExpectUndeclaredVariable() {
    Scope scope = Scope.createGlobalScope(new Node(Token.SCRIPT));
    CompilerInput input = new CompilerInput(SourceFile.fromCode("test.js", "var x;"));
    Node varNode = IR.name("x");
    varNode.setLineno(1);
    Node parentNode = IR.var(varNode);

    Scope.Var var = scope.declare("x", varNode, registry.getNativeType(NUMBER_TYPE), input, false);

    // Duplicate declaration with differing types
    validator.expectUndeclaredVariable("test.js", input, varNode, parentNode, var, "x", registry.getNativeType(STRING_TYPE));
    Assert.assertEquals(1, compiler.getWarningCount());

    // Duplicate declaration with suppression
    JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
    builder.recordSuppressions(Collections.singleton("duplicate"));
    JSDocInfo info = builder.build(varNode);
    varNode.setJSDocInfo(info);
    validator.expectUndeclaredVariable("test.js", input, varNode, parentNode, var, "x", registry.getNativeType(NUMBER_TYPE));
    Assert.assertEquals(1, compiler.getWarningCount());
  }
}