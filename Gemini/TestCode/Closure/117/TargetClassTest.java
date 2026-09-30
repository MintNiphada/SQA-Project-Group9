package com.google.javascript.jscomp;

import static com.google.javascript.rhino.jstype.JSTypeNative.ALL_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.ARRAY_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.BOOLEAN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NO_OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NO_RESOLVED_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NO_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NULL_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NUMBER_OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NUMBER_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.STRING_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.UNKNOWN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.VOID_TYPE;

import com.google.common.collect.ImmutableList;
import com.google.javascript.jscomp.Scope.Var;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.TemplateType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Iterator;

public class TypeValidatorTest {

  private Compiler compiler;
  private JSTypeRegistry registry;
  private TypeValidator validator;

  @Before
  public void setUp() {
    compiler = new Compiler();
    compiler.initCompilerOptionsIfTesting();
    registry = compiler.getTypeRegistry();
    validator = new TypeValidator(compiler);
  }

  private JSType getNative(JSTypeNative typeId) {
    return registry.getNativeType(typeId);
  }

  private NodeTraversal createTraversal() {
    return new NodeTraversal(compiler, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {}
    });
  }

  @Test
  public void testSetShouldReport() {
    validator.setShouldReport(false);
    Node n = IR.name("x");
    validator.expectValidTypeofName(createTraversal(), n, "invalid_type");
    Assert.assertEquals(0, compiler.getErrorCount());
    Assert.assertEquals(0, compiler.getWarningCount());

    validator.setShouldReport(true);
    validator.expectValidTypeofName(createTraversal(), n, "invalid_type");
    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testExpectObject() {
    NodeTraversal t = createTraversal();
    Node n = IR.name("x");

    Assert.assertTrue(validator.expectObject(t, n, getNative(OBJECT_TYPE), "msg"));
    Assert.assertTrue(validator.expectObject(t, n, getNative(UNKNOWN_TYPE), "msg"));
    Assert.assertFalse(validator.expectObject(t, n, getNative(NUMBER_TYPE), "msg"));
    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testExpectActualObject() {
    NodeTraversal t = createTraversal();
    Node n = IR.name("x");

    validator.expectActualObject(t, n, getNative(OBJECT_TYPE), "msg");
    Assert.assertEquals(0, compiler.getWarningCount());

    validator.expectActualObject(t, n, getNative(NUMBER_TYPE), "msg");
    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testExpectAnyObject() {
    NodeTraversal t = createTraversal();
    Node n = IR.name("x");

    validator.expectAnyObject(t, n, getNative(OBJECT_TYPE), "msg");
    Assert.assertEquals(0, compiler.getWarningCount());

    validator.expectAnyObject(t, n, getNative(NUMBER_TYPE), "msg");
    Assert.assertEquals(1, compiler.getWarningCount());

    validator.expectAnyObject(t, n, getNative(NO_TYPE), "msg");
    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testExpectString() {
    NodeTraversal t = createTraversal();
    Node n = IR.name("x");

    validator.expectString(t, n, getNative(STRING_TYPE), "msg");
    Assert.assertEquals(0, compiler.getWarningCount());

    validator.expectString(t, n, getNative(NUMBER_TYPE), "msg");
    Assert.assertEquals(0, compiler.getWarningCount()); // number matches string context

    validator.expectString(t, n, getNative(VOID_TYPE), "msg");
    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testExpectNumber() {
    NodeTraversal t = createTraversal();
    Node n = IR.name("x");

    validator.expectNumber(t, n, getNative(NUMBER_TYPE), "msg");
    Assert.assertEquals(0, compiler.getWarningCount());

    validator.expectNumber(t, n, getNative(STRING_TYPE), "msg");
    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testExpectBitwiseable() {
    NodeTraversal t = createTraversal();
    Node n = IR.name("x");

    validator.expectBitwiseable(t, n, getNative(NUMBER_TYPE), "msg");
    validator.expectBitwiseable(t, n, getNative(STRING_TYPE), "msg");
    validator.expectBitwiseable(t, n, getNative(BOOLEAN_TYPE), "msg");
    Assert.assertEquals(0, compiler.getWarningCount());

    ObjectType structType = registry.createObjectType("StructObj", null, null);
    validator.expectBitwiseable(t, n, structType, "msg");
    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testExpectStringOrNumber() {
    NodeTraversal t = createTraversal();
    Node n = IR.name("x");

    validator.expectStringOrNumber(t, n, getNative(NUMBER_TYPE), "msg");
    validator.expectStringOrNumber(t, n, getNative(STRING_TYPE), "msg");
    Assert.assertEquals(0, compiler.getWarningCount());

    validator.expectStringOrNumber(t, n, getNative(VOID_TYPE), "msg");
    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testExpectNotNullOrUndefined() {
    NodeTraversal t = createTraversal();
    Node n = IR.name("x");

    Assert.assertTrue(validator.expectNotNullOrUndefined(t, n, getNative(NUMBER_TYPE), "msg", getNative(OBJECT_TYPE)));
    Assert.assertTrue(validator.expectNotNullOrUndefined(t, n, getNative(UNKNOWN_TYPE), "msg", getNative(OBJECT_TYPE)));
    Assert.assertTrue(validator.expectNotNullOrUndefined(t, n, getNative(NO_TYPE), "msg", getNative(OBJECT_TYPE)));
    Assert.assertEquals(0, compiler.getWarningCount());

    Assert.assertFalse(validator.expectNotNullOrUndefined(t, n, getNative(NULL_TYPE), "msg", getNative(OBJECT_TYPE)));
    Assert.assertEquals(1, compiler.getWarningCount());

    Assert.assertFalse(validator.expectNotNullOrUndefined(t, n, getNative(VOID_TYPE), "msg", getNative(OBJECT_TYPE)));
    Assert.assertEquals(2, compiler.getWarningCount());

    JSType union = registry.createUnionType(getNative(NULL_TYPE), getNative(NO_RESOLVED_TYPE));
    Assert.assertTrue(validator.expectNotNullOrUndefined(t, n, union, "msg", getNative(OBJECT_TYPE)));

    Node getPropNode = IR.getprop(IR.name("a"), IR.string("b"));
    Scope s = Scope.createGlobalScope(new Node(Token.ROOT));
    Scope localScope = new Scope(s, new Node(Token.FUNCTION));
    NodeTraversal localTraversal = new NodeTraversal(compiler, null, new SyntacticScopeCreator(compiler));
    localTraversal.traverseAtScope(localScope);
    Assert.assertTrue(validator.expectNotNullOrUndefined(localTraversal, getPropNode, getNative(NULL_TYPE), "msg", getNative(OBJECT_TYPE)));
  }

  @Test
  public void testExpectSwitchMatchesCase() {
    NodeTraversal t = createTraversal();
    Node caseNode = IR.caseNode(IR.number(1), IR.block());

    validator.expectSwitchMatchesCase(t, caseNode, getNative(NUMBER_TYPE), getNative(NUMBER_TYPE));
    Assert.assertEquals(0, compiler.getWarningCount());

    validator.expectSwitchMatchesCase(t, caseNode, getNative(NUMBER_OBJECT_TYPE), getNative(NUMBER_TYPE));
    Assert.assertEquals(0, compiler.getWarningCount());

    validator.expectSwitchMatchesCase(t, caseNode, getNative(STRING_TYPE), getNative(NUMBER_TYPE));
    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testExpectIndexMatch() {
    NodeTraversal t = createTraversal();
    Node objNode = IR.name("arr");
    Node indexNode = IR.name("i");
    Node getElemNode = IR.getelem(objNode, indexNode);

    validator.expectIndexMatch(t, getElemNode, getNative(UNKNOWN_TYPE), getNative(STRING_TYPE));
    Assert.assertEquals(0, compiler.getWarningCount());

    validator.expectIndexMatch(t, getElemNode, getNative(ARRAY_TYPE), getNative(NUMBER_TYPE));
    Assert.assertEquals(0, compiler.getWarningCount());

    validator.expectIndexMatch(t, getElemNode, getNative(OBJECT_TYPE), getNative(STRING_TYPE));
    Assert.assertEquals(0, compiler.getWarningCount());

    validator.expectIndexMatch(t, getElemNode, getNative(BOOLEAN_TYPE), getNative(NUMBER_TYPE));
    Assert.assertEquals(1, compiler.getWarningCount());

    ObjectType structType = registry.createObjectType("StructType", null, null);
    structType.setStruct();
    validator.expectIndexMatch(t, getElemNode, structType, getNative(STRING_TYPE));
    Assert.assertEquals(2, compiler.getWarningCount());
  }

  @Test
  public void testExpectCanAssignToPropertyOf() {
    NodeTraversal t = createTraversal();
    Node ownerNode = IR.name("owner");
    ownerNode.setJSType(getNative(OBJECT_TYPE));
    Node n = IR.name("prop");

    Assert.assertTrue(validator.expectCanAssignToPropertyOf(t, n, getNative(NUMBER_TYPE), getNative(NUMBER_TYPE), ownerNode, "p"));
    Assert.assertTrue(validator.expectCanAssignToPropertyOf(t, n, getNative(NUMBER_TYPE), getNative(NO_TYPE), ownerNode, "p"));

    Assert.assertFalse(validator.expectCanAssignToPropertyOf(t, n, getNative(STRING_TYPE), getNative(NUMBER_TYPE), ownerNode, "p"));
    Assert.assertEquals(1, compiler.getWarningCount());

    FunctionType interfaceCtor = registry.createInterfaceType("AnInterface", null, ImmutableList.<TemplateType>of());
    ObjectType prototype = interfaceCtor.getPrototype();
    ownerNode.setJSType(prototype);
    FunctionType fn1 = registry.createFunctionType(getNative(NUMBER_TYPE));
    FunctionType fn2 = registry.createFunctionType(getNative(STRING_TYPE));
    Assert.assertTrue(validator.expectCanAssignToPropertyOf(t, n, fn1, fn2, ownerNode, "method"));
  }

  @Test
  public void testExpectCanAssignTo() {
    NodeTraversal t = createTraversal();
    Node n = IR.name("x");

    Assert.assertTrue(validator.expectCanAssignTo(t, n, getNative(NUMBER_TYPE), getNative(NUMBER_TYPE), "msg"));
    Assert.assertFalse(validator.expectCanAssignTo(t, n, getNative(STRING_TYPE), getNative(NUMBER_TYPE), "msg"));
    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testExpectArgumentMatchesParameter() {
    NodeTraversal t = createTraversal();
    Node callee = IR.name("foo");
    Node call = IR.call(callee, IR.string("arg"));
    Node arg = call.getLastChild();

    validator.expectArgumentMatchesParameter(t, arg, getNative(STRING_TYPE), getNative(STRING_TYPE), call, 1);
    Assert.assertEquals(0, compiler.getWarningCount());

    validator.expectArgumentMatchesParameter(t, arg, getNative(STRING_TYPE), getNative(NUMBER_TYPE), call, 1);
    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testExpectCanOverride() {
    NodeTraversal t = createTraversal();
    Node n = IR.name("prop");

    validator.expectCanOverride(t, n, getNative(NUMBER_TYPE), getNative(NUMBER_TYPE), "p", getNative(OBJECT_TYPE));
    Assert.assertEquals(0, compiler.getWarningCount());

    validator.expectCanOverride(t, n, getNative(STRING_TYPE), getNative(NUMBER_TYPE), "p", getNative(OBJECT_TYPE));
    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testExpectCanCast() {
    NodeTraversal t = createTraversal();
    Node n = IR.name("x");

    validator.expectCanCast(t, n, getNative(OBJECT_TYPE), getNative(OBJECT_TYPE));
    Assert.assertEquals(0, compiler.getWarningCount());

    validator.expectCanCast(t, n, getNative(NUMBER_TYPE), getNative(STRING_TYPE));
    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testExpectSuperType() {
    NodeTraversal t = createTraversal();
    Node n = IR.name("SubClass");

    FunctionType superCtor = registry.createConstructorType("Super", null, null, null, null);
    ObjectType superType = superCtor.getInstanceType();

    FunctionType subCtor = registry.createConstructorType("Sub", null, null, null, null);
    subCtor.setPrototypeBasedOn(superType);
    ObjectType subType = subCtor.getInstanceType();

    validator.expectSuperType(t, n, superType, subType);
    Assert.assertEquals(0, compiler.getWarningCount());

    FunctionType otherCtor = registry.createConstructorType("Other", null, null, null, null);
    ObjectType otherType = otherCtor.getInstanceType();
    validator.expectSuperType(t, n, otherType, subType);
    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testExpectAllInterfaceProperties() {
    NodeTraversal t = createTraversal();
    Node n = IR.name("ImplemClass");

    FunctionType ifaceCtor = registry.createInterfaceType("MyIface", null, ImmutableList.<TemplateType>of());
    ifaceCtor.getPrototype().defineDeclaredProperty("foo", getNative(STRING_TYPE), null);

    FunctionType classCtor = registry.createConstructorType("MyClass", null, null, null, null);
    classCtor.setImplementedInterfaces(ImmutableList.of(ifaceCtor.getInstanceType()));

    validator.expectAllInterfaceProperties(t, n, classCtor);
    Assert.assertEquals(1, compiler.getWarningCount());

    classCtor.getInstanceType().defineDeclaredProperty("foo", getNative(NUMBER_TYPE), null);
    validator.expectAllInterfaceProperties(t, n, classCtor);
    Assert.assertEquals(2, compiler.getWarningCount());

    classCtor.getInstanceType().defineDeclaredProperty("foo", getNative(STRING_TYPE), null);
    validator.expectAllInterfaceProperties(t, n, classCtor);
    Assert.assertEquals(2, compiler.getWarningCount());
  }

  @Test
  public void testExpectUndeclaredVariable() {
    Node root = new Node(Token.ROOT);
    Scope scope = Scope.createGlobalScope(root);
    Node nameNode = IR.name("x");
    nameNode.setLineno(1);
    CompilerInput input = new CompilerInput(SourceFile.fromCode("test.js", "var x;"));
    Var var = scope.declare("x", nameNode, getNative(NUMBER_TYPE), input);

    Node newNameNode = IR.name("x");
    newNameNode.setLineno(2);
    Node parent = IR.var(newNameNode);

    Var result = validator.expectUndeclaredVariable("test.js", input, newNameNode, parent, var, "x", getNative(STRING_TYPE));
    Assert.assertNotNull(result);
    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testGetReadableJSTypeName() {
    Node n = IR.name("x");
    n.setJSType(getNative(NUMBER_TYPE));
    Assert.assertEquals("number", validator.getReadableJSTypeName(n, false));

    Node getProp = IR.getprop(IR.name("a"), IR.string("b"));
    ObjectType objType = registry.createObjectType("MyType", null, null);
    objType.defineDeclaredProperty("b", getNative(STRING_TYPE), null);
    getProp.getFirstChild().setJSType(objType);
    getProp.setJSType(getNative(STRING_TYPE));
    Assert.assertTrue(validator.getReadableJSTypeName(getProp, false).contains("MyType.b"));
  }

  @Test
  public void testTypeMismatch() {
    JSError error = JSError.make("test.js", 1, 1, TypeValidator.TYPE_MISMATCH_WARNING, "err");
    TypeValidator.TypeMismatch mismatch1 = new TypeValidator.TypeMismatch(getNative(NUMBER_TYPE), getNative(STRING_TYPE), error);
    TypeValidator.TypeMismatch mismatch2 = new TypeValidator.TypeMismatch(getNative(STRING_TYPE), getNative(NUMBER_TYPE), error);
    TypeValidator.TypeMismatch mismatch3 = new TypeValidator.TypeMismatch(getNative(NUMBER_TYPE), getNative(BOOLEAN_TYPE), error);

    Assert.assertEquals(mismatch1, mismatch2);
    Assert.assertNotEquals(mismatch1, mismatch3);
    Assert.assertFalse(mismatch1.equals("not a mismatch"));
    Assert.assertEquals(mismatch1.hashCode(), mismatch1.hashCode());
    Assert.assertNotNull(mismatch1.toString());
  }

  @Test
  public void testGetMismatches() {
    NodeTraversal t = createTraversal();
    Node n = IR.name("x");
    validator.expectCanAssignTo(t, n, getNative(STRING_TYPE), getNative(NUMBER_TYPE), "msg");

    Iterator<TypeValidator.TypeMismatch> iter = validator.getMismatches().iterator();
    Assert.assertTrue(iter.hasNext());
    TypeValidator.TypeMismatch mismatch = iter.next();
    Assert.assertTrue(mismatch.typeA.isEquivalentTo(getNative(STRING_TYPE)));
    Assert.assertTrue(mismatch.typeB.isEquivalentTo(getNative(NUMBER_TYPE)));
  }
}