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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
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
import java.util.List;

public class TypeValidatorTest {

  private Compiler compiler;
  private JSTypeRegistry registry;
  private TypeValidator validator;
  private NodeTraversal traversal;

  @Before
  public void setUp() {
    compiler = new Compiler();
    compiler.initCompilerOptionsIfTesting();
    registry = compiler.getTypeRegistry();
    validator = new TypeValidator(compiler);
    traversal = new NodeTraversal(compiler, null);
  }

  @Test
  public void testExpectObject() {
    Node node = new Node(Token.NAME);
    JSType stringType = registry.getNativeType(STRING_TYPE);
    JSType objectType = registry.getNativeType(OBJECT_TYPE);

    Assert.assertFalse(validator.expectObject(traversal, node, stringType, "expected object"));
    Assert.assertEquals(1, compiler.getWarningCount());

    Assert.assertTrue(validator.expectObject(traversal, node, objectType, "expected object"));
    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testExpectActualObject() {
    Node node = new Node(Token.NAME);
    JSType stringType = registry.getNativeType(STRING_TYPE);
    JSType objectType = registry.getNativeType(OBJECT_TYPE);

    validator.expectActualObject(traversal, node, stringType, "msg");
    Assert.assertEquals(1, compiler.getWarningCount());

    validator.expectActualObject(traversal, node, objectType, "msg");
    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testExpectAnyObject() {
    Node node = new Node(Token.NAME);
    JSType stringType = registry.getNativeType(STRING_TYPE);
    JSType noObjectType = registry.getNativeType(NO_OBJECT_TYPE);

    validator.expectAnyObject(traversal, node, stringType, "msg");
    Assert.assertEquals(1, compiler.getWarningCount());

    validator.expectAnyObject(traversal, node, noObjectType, "msg");
    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testExpectString() {
    Node node = new Node(Token.NAME);
    JSType numberType = registry.getNativeType(NUMBER_TYPE);
    JSType stringType = registry.getNativeType(STRING_TYPE);

    validator.expectString(traversal, node, numberType, "msg");
    Assert.assertEquals(1, compiler.getWarningCount());

    validator.expectString(traversal, node, stringType, "msg");
    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testExpectNumber() {
    Node node = new Node(Token.NAME);
    JSType stringType = registry.getNativeType(STRING_TYPE);
    JSType numberType = registry.getNativeType(NUMBER_TYPE);

    validator.expectNumber(traversal, node, stringType, "msg");
    Assert.assertEquals(1, compiler.getWarningCount());

    validator.expectNumber(traversal, node, numberType, "msg");
    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testExpectBitwiseable() {
    Node node = new Node(Token.NAME);
    JSType objectType = registry.getNativeType(OBJECT_TYPE);
    JSType numberType = registry.getNativeType(NUMBER_TYPE);

    validator.expectBitwiseable(traversal, node, objectType, "msg");
    Assert.assertEquals(1, compiler.getWarningCount());

    validator.expectBitwiseable(traversal, node, numberType, "msg");
    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testExpectStringOrNumber() {
    Node node = new Node(Token.NAME);
    JSType objectType = registry.getNativeType(OBJECT_TYPE);
    JSType stringType = registry.getNativeType(STRING_TYPE);

    validator.expectStringOrNumber(traversal, node, objectType, "msg");
    Assert.assertEquals(1, compiler.getWarningCount());

    validator.expectStringOrNumber(traversal, node, stringType, "msg");
    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testExpectNotNullOrUndefined() {
    Node node = new Node(Token.NAME);
    JSType nullType = registry.getNativeType(NULL_TYPE);
    JSType stringType = registry.getNativeType(STRING_TYPE);

    Assert.assertFalse(validator.expectNotNullOrUndefined(traversal, node, nullType, "msg", stringType));
    Assert.assertEquals(1, compiler.getWarningCount());

    Assert.assertTrue(validator.expectNotNullOrUndefined(traversal, node, stringType, "msg", stringType));
    Assert.assertEquals(1, compiler.getWarningCount());

    Node getPropNode = new Node(Token.GETPROP, new Node(Token.THIS), Node.newString("x"));
    Node rootNode = new Node(Token.FUNCTION, new Node(Token.NAME), new Node(Token.PARAM_LIST), new Node(Token.BLOCK));
    NodeTraversal scopedTraversal = new NodeTraversal(compiler, null);
    scopedTraversal.traverse(rootNode);
    Assert.assertTrue(validator.expectNotNullOrUndefined(scopedTraversal, getPropNode, nullType, "msg", stringType));

    JSType unresolved = registry.createNamedType("ForwardDeclaredType", null, 0, 0);
    JSType unionWithUnresolved = registry.createUnionType(nullType, unresolved);
    Assert.assertTrue(validator.expectNotNullOrUndefined(traversal, node, unionWithUnresolved, "msg", stringType));
  }

  @Test
  public void testExpectSwitchMatchesCase() {
    Node parent = new Node(Token.CASE, new Node(Token.NAME));
    JSType stringType = registry.getNativeType(STRING_TYPE);
    JSType numberType = registry.getNativeType(NUMBER_TYPE);

    validator.expectSwitchMatchesCase(traversal, parent, stringType, numberType);
    Assert.assertEquals(1, compiler.getWarningCount());

    validator.expectSwitchMatchesCase(traversal, parent, stringType, stringType);
    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testExpectIndexMatch() {
    Node node = new Node(Token.NAME);
    JSType unknownType = registry.getNativeType(UNKNOWN_TYPE);
    JSType stringType = registry.getNativeType(STRING_TYPE);
    JSType numberType = registry.getNativeType(NUMBER_TYPE);
    JSType arrayType = registry.getNativeType(ARRAY_TYPE);
    JSType objectType = registry.getNativeType(OBJECT_TYPE);
    JSType booleanType = registry.getNativeType(BOOLEAN_TYPE);

    validator.expectIndexMatch(traversal, node, unknownType, stringType);
    Assert.assertEquals(0, compiler.getWarningCount());

    validator.expectIndexMatch(traversal, node, arrayType, stringType);
    Assert.assertEquals(1, compiler.getWarningCount());

    validator.expectIndexMatch(traversal, node, arrayType, numberType);
    Assert.assertEquals(1, compiler.getWarningCount());

    validator.expectIndexMatch(traversal, node, objectType, stringType);
    Assert.assertEquals(1, compiler.getWarningCount());

    validator.expectIndexMatch(traversal, node, booleanType, stringType);
    Assert.assertEquals(2, compiler.getWarningCount());

    ObjectType indexedObj = registry.createObjectType("CustomIndexObj", null);
    indexedObj.defineDeclaredProperty("index", numberType, null);
    indexedObj.setPropertyJSType("index", numberType);
    ObjectType withIndexType = registry.createObjectType("Indexed", null);
    withIndexType.setIndexedType(numberType);
    validator.expectIndexMatch(traversal, node, withIndexType, stringType);
    Assert.assertEquals(3, compiler.getWarningCount());
  }

  @Test
  public void testExpectCanAssignToPropertyOf() {
    Node owner = Node.newString("owner");
    Node n = Node.newString("val");
    JSType stringType = registry.getNativeType(STRING_TYPE);
    JSType numberType = registry.getNativeType(NUMBER_TYPE);

    Assert.assertFalse(validator.expectCanAssignToPropertyOf(traversal, n, stringType, numberType, owner, "foo"));
    Assert.assertEquals(1, compiler.getWarningCount());

    Assert.assertTrue(validator.expectCanAssignToPropertyOf(traversal, n, stringType, stringType, owner, "foo"));
    Assert.assertEquals(1, compiler.getWarningCount());

    FunctionType fnType1 = registry.createConstructorType("Foo1", null, null, null);
    FunctionType fnType2 = registry.createConstructorType("Foo2", null, null, null);
    Assert.assertFalse(validator.expectCanAssignToPropertyOf(traversal, n, fnType1, fnType2, owner, "foo"));
  }

  @Test
  public void testExpectCanAssignTo() {
    Node n = Node.newString("val");
    JSType stringType = registry.getNativeType(STRING_TYPE);
    JSType numberType = registry.getNativeType(NUMBER_TYPE);

    Assert.assertFalse(validator.expectCanAssignTo(traversal, n, stringType, numberType, "err"));
    Assert.assertEquals(1, compiler.getWarningCount());

    Assert.assertTrue(validator.expectCanAssignTo(traversal, n, stringType, stringType, "err"));
    Assert.assertEquals(1, compiler.getWarningCount());

    FunctionType fnType1 = registry.createConstructorType("Foo1", null, null, null);
    FunctionType fnType2 = registry.createConstructorType("Foo2", null, null, null);
    Assert.assertFalse(validator.expectCanAssignTo(traversal, n, fnType1, fnType2, "err"));
  }

  @Test
  public void testExpectArgumentMatchesParameter() {
    Node call = new Node(Token.CALL, Node.newString("myFunc"));
    Node arg = Node.newString("arg");
    JSType stringType = registry.getNativeType(STRING_TYPE);
    JSType numberType = registry.getNativeType(NUMBER_TYPE);

    validator.expectArgumentMatchesParameter(traversal, arg, stringType, numberType, call, 1);
    Assert.assertEquals(1, compiler.getWarningCount());

    validator.expectArgumentMatchesParameter(traversal, arg, stringType, stringType, call, 1);
    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testExpectCanOverride() {
    Node n = Node.newString("prop");
    JSType stringType = registry.getNativeType(STRING_TYPE);
    JSType numberType = registry.getNativeType(NUMBER_TYPE);
    JSType objectType = registry.getNativeType(OBJECT_TYPE);

    validator.expectCanOverride(traversal, n, stringType, numberType, "prop", objectType);
    Assert.assertEquals(1, compiler.getWarningCount());

    validator.setShouldReport(false);
    validator.expectCanOverride(traversal, n, stringType, numberType, "prop", objectType);
    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testExpectSuperType() {
    Node n = new Node(Token.NAME);
    FunctionType superCtor = registry.createConstructorType("Super", null, null, null);
    FunctionType subCtor = registry.createConstructorType("Sub", null, null, null);
    ObjectType superInstance = superCtor.getInstanceType();
    ObjectType subInstance = subCtor.getInstanceType();

    validator.expectSuperType(traversal, n, superInstance, subInstance);
    Assert.assertEquals(1, compiler.getWarningCount());

    FunctionType otherSuperCtor = registry.createConstructorType("OtherSuper", null, null, null);
    subCtor.setPrototypeBasedOn(otherSuperCtor.getInstanceType());
    validator.expectSuperType(traversal, n, superInstance, subInstance);
    Assert.assertEquals(2, compiler.getWarningCount());
  }

  @Test
  public void testExpectCanCast() {
    Node n = new Node(Token.NAME);
    JSType stringType = registry.getNativeType(STRING_TYPE);
    JSType numberType = registry.getNativeType(NUMBER_TYPE);
    JSType objectType = registry.getNativeType(OBJECT_TYPE);

    validator.expectCanCast(traversal, n, stringType, numberType);
    Assert.assertEquals(1, compiler.getWarningCount());

    validator.expectCanCast(traversal, n, objectType, objectType);
    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testExpectUndeclaredVariable() {
    Node n = Node.newString("x");
    Node varNode = new Node(Token.VAR, n);
    Scope scope = new Scope(varNode, null);
    Scope.Var var = scope.declare("x", n, registry.getNativeType(STRING_TYPE), null, false);

    validator.expectUndeclaredVariable("test.js", n, varNode, var, "x", registry.getNativeType(NUMBER_TYPE));
    Assert.assertEquals(registry.getNativeType(STRING_TYPE), n.getJSType());

    Node fnNode = new Node(Token.FUNCTION, Node.newString("f"), new Node(Token.PARAM_LIST), new Node(Token.BLOCK));
    validator.expectUndeclaredVariable("test.js", n, fnNode, var, "x", registry.getNativeType(NUMBER_TYPE));
    Assert.assertEquals(registry.getNativeType(STRING_TYPE), fnNode.getJSType());

    CompilerInput input = new CompilerInput(SourceFile.fromCode("input.js", "var x;"));
    n.setStaticSourceFile(input.getSourceFile());
    n.setLineno(10);
    var.input = input;

    validator.expectUndeclaredVariable("test.js", n, varNode, var, "x", registry.getNativeType(NUMBER_TYPE));
    Assert.assertEquals(1, compiler.getWarningCount());

    Node getPropNode = new Node(Token.GETPROP, new Node(Token.THIS), Node.newString("x"));
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(false);
    docBuilder.recordSuppression("duplicate");
    JSDocInfo docInfo = docBuilder.build(getPropNode);
    getPropNode.setJSDocInfo(docInfo);

    validator.expectUndeclaredVariable("test.js", getPropNode, varNode, var, "x", registry.getNativeType(STRING_TYPE));
    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testExpectAllInterfaceProperties() {
    Node n = new Node(Token.NAME);
    FunctionType ifaceType = registry.createInterfaceType("MyInterface", null);
    ObjectType ifaceInstance = ifaceType.getInstanceType();
    ifaceType.getPrototype().defineDeclaredProperty("foo", registry.getNativeType(STRING_TYPE), null);

    FunctionType implType = registry.createConstructorType("MyImpl", null, null, null);
    implType.setImplementedInterfaces(ImmutableList.of(ifaceInstance));

    validator.expectAllInterfaceProperties(traversal, n, implType);
    Assert.assertEquals(1, compiler.getWarningCount());

    implType.getPrototype().defineDeclaredProperty("foo", registry.getNativeType(STRING_TYPE), null);
    validator.expectAllInterfaceProperties(traversal, n, implType);
    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testGetReadableJSTypeName() {
    Node nameNode = Node.newString("x");
    nameNode.setJSType(registry.getNativeType(NUMBER_TYPE));
    Assert.assertEquals("number", validator.getReadableJSTypeName(nameNode, false));

    Node noTypeNode = Node.newString("y");
    Assert.assertEquals("y", validator.getReadableJSTypeName(noTypeNode, false));

    FunctionType fnType = registry.createFunctionType(registry.getNativeType(NUMBER_TYPE));
    Node fnNode = new Node(Token.CALL);
    fnNode.setJSType(fnType);
    Assert.assertEquals("function", validator.getReadableJSTypeName(fnNode, false));

    FunctionType ctorType = registry.createConstructorType("MyClass", null, null, null);
    Node ctorNode = new Node(Token.NEW);
    ctorNode.setJSType(ctorType.getInstanceType());
    Assert.assertEquals("MyClass", validator.getReadableJSTypeName(ctorNode, false));

    Node getProp = new Node(Token.GETPROP, ctorNode, Node.newString("myProp"));
    ctorType.getPrototype().defineDeclaredProperty("myProp", registry.getNativeType(STRING_TYPE), null);
    Assert.assertEquals("MyClass.prototype.myProp", validator.getReadableJSTypeName(getProp, false));
  }

  @Test
  public void testTypeMismatchEqualsHashCodeAndToString() {
    JSType stringType = registry.getNativeType(STRING_TYPE);
    JSType numberType = registry.getNativeType(NUMBER_TYPE);
    JSType boolType = registry.getNativeType(BOOLEAN_TYPE);

    TypeValidator.TypeMismatch mismatch1 = new TypeValidator.TypeMismatch(stringType, numberType);
    TypeValidator.TypeMismatch mismatch2 = new TypeValidator.TypeMismatch(numberType, stringType);
    TypeValidator.TypeMismatch mismatch3 = new TypeValidator.TypeMismatch(stringType, boolType);

    Assert.assertTrue(mismatch1.equals(mismatch2));
    Assert.assertTrue(mismatch2.equals(mismatch1));
    Assert.assertFalse(mismatch1.equals(mismatch3));
    Assert.assertFalse(mismatch1.equals("non-mismatch"));

    Assert.assertEquals(mismatch1.hashCode(), mismatch1.hashCode());
    Assert.assertEquals("(" + stringType + ", " + numberType + ")", mismatch1.toString());

    List<TypeValidator.TypeMismatch> mismatches = Lists.newArrayList(validator.getMismatches());
    Assert.assertEquals(0, mismatches.size());
  }

  @Test
  public void testFunctionMismatchHierarchy() {
    FunctionType fnTypeA = registry.createFunctionType(registry.getNativeType(STRING_TYPE),
        registry.createParameters(registry.getNativeType(STRING_TYPE)));
    FunctionType fnTypeB = registry.createFunctionType(registry.getNativeType(NUMBER_TYPE),
        registry.createParameters(registry.getNativeType(NUMBER_TYPE)));

    Node n = new Node(Token.NAME);
    validator.expectCanAssignTo(traversal, n, fnTypeA, fnTypeB, "mismatch");

    Iterator<TypeValidator.TypeMismatch> it = validator.getMismatches().iterator();
    Assert.assertTrue(it.hasNext());
    TypeValidator.TypeMismatch mm = it.next();
    Assert.assertNotNull(mm);
  }
}
