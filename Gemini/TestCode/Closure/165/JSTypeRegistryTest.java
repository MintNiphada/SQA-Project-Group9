package com.google.javascript.rhino.jstype;

import com.google.common.collect.ImmutableList;
import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.RecordTypeBuilder.RecordProperty;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSTypeRegistryTest {

  private static class TestErrorReporter implements ErrorReporter {
    private final List<String> warnings = new ArrayList<String>();
    private final List<String> errors = new ArrayList<String>();

    @Override
    public void warning(String message, String sourceName, int line, int lineOffset) {
      warnings.add(message);
    }

    @Override
    public void error(String message, String sourceName, int line, int lineOffset) {
      errors.add(message);
    }
  }

  private TestErrorReporter reporter;
  private JSTypeRegistry registry;

  @Before
  public void setUp() {
    reporter = new TestErrorReporter();
    registry = new JSTypeRegistry(reporter, false);
  }

  @Test
  public void testInitialBuiltInTypes() {
    Assert.assertNotNull(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE));
    Assert.assertNotNull(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    Assert.assertNotNull(registry.getNativeType(JSTypeNative.STRING_TYPE));
    Assert.assertNotNull(registry.getNativeType(JSTypeNative.ALL_TYPE));
    Assert.assertNotNull(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE));
    Assert.assertNotNull(registry.getNativeType(JSTypeNative.CHECKED_UNKNOWN_TYPE));
    Assert.assertNotNull(registry.getNativeType(JSTypeNative.VOID_TYPE));
    Assert.assertNotNull(registry.getNativeType(JSTypeNative.NULL_TYPE));
    Assert.assertNotNull(registry.getNativeType(JSTypeNative.OBJECT_TYPE));
    Assert.assertNotNull(registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE));
    Assert.assertNotNull(registry.getNativeFunctionType(JSTypeNative.FUNCTION_FUNCTION_TYPE));
    Assert.assertFalse(registry.shouldTolerateUndefinedValues());
    Assert.assertSame(reporter, registry.getErrorReporter());
  }

  @Test
  public void testConstructorWithTolerateUndefinedValues() {
    JSTypeRegistry reg = new JSTypeRegistry(reporter, true);
    Assert.assertTrue(reg.shouldTolerateUndefinedValues());
  }

  @Test
  public void testResolveMode() {
    Assert.assertEquals(JSTypeRegistry.ResolveMode.LAZY_NAMES, registry.getResolveMode());
    registry.setResolveMode(JSTypeRegistry.ResolveMode.IMMEDIATE);
    Assert.assertEquals(JSTypeRegistry.ResolveMode.IMMEDIATE, registry.getResolveMode());
    registry.setResolveMode(JSTypeRegistry.ResolveMode.LAZY_EXPRESSIONS);
    Assert.assertEquals(JSTypeRegistry.ResolveMode.LAZY_EXPRESSIONS, registry.getResolveMode());
  }

  @Test
  public void testTemplateTypeName() {
    Assert.assertNull(registry.getType("T"));
    registry.setTemplateTypeName("T");
    JSType type = registry.getType("T");
    Assert.assertNotNull(type);
    Assert.assertTrue(type.isTemplateType());
    registry.clearTemplateTypeName();
    Assert.assertNull(registry.getType("T"));
  }

  @Test
  public void testDeclareAndOverwriteType() {
    JSType numType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    Assert.assertTrue(registry.declareType("my.custom.Num", numType));
    Assert.assertFalse(registry.declareType("my.custom.Num", numType));
    Assert.assertTrue(registry.hasNamespace("my"));
    Assert.assertTrue(registry.hasNamespace("my.custom"));
    Assert.assertFalse(registry.hasNamespace("other"));
    Assert.assertEquals(numType, registry.getType("my.custom.Num"));

    JSType strType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    registry.overwriteDeclaredType("my.custom.Num", strType);
    Assert.assertEquals(strType, registry.getType("my.custom.Num"));
  }

  @Test(expected = IllegalStateException.class)
  public void testOverwriteUndeclaredTypeThrows() {
    registry.overwriteDeclaredType("nonexistent", registry.getNativeType(JSTypeNative.NUMBER_TYPE));
  }

  @Test
  public void testForwardDeclareType() {
    Assert.assertFalse(registry.isForwardDeclaredType("com.example.Foo"));
    registry.forwardDeclareType("com.example.Foo");
    Assert.assertTrue(registry.isForwardDeclaredType("com.example.Foo"));
  }

  @Test
  public void testPropertyRegistrationAndLookup() {
    ObjectType objType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    ObjectType arrayType = registry.getNativeObjectType(JSTypeNative.ARRAY_TYPE);

    registry.registerPropertyOnType("customProp", objType);
    registry.registerPropertyOnType("customProp", arrayType);

    Assert.assertTrue(registry.canPropertyBeDefined(objType, "customProp"));
    Assert.assertFalse(registry.canPropertyBeDefined(objType, "nonExistentProp"));

    JSType subtype = registry.getGreatestSubtypeWithProperty(objType, "customProp");
    Assert.assertNotNull(subtype);

    JSType subtypeCached = registry.getGreatestSubtypeWithProperty(objType, "customProp");
    Assert.assertEquals(subtype, subtypeCached);

    JSType noneSubtype = registry.getGreatestSubtypeWithProperty(objType, "nonExistentProp");
    Assert.assertTrue(noneSubtype.isNoType());

    List<JSType> typesWithProp = ImmutableList.copyOf(registry.getTypesWithProperty("customProp"));
    Assert.assertFalse(typesWithProp.isEmpty());
    Assert.assertTrue(ImmutableList.copyOf(registry.getTypesWithProperty("nonExistent")).isEmpty());

    List<ObjectType> refTypes = ImmutableList.copyOf(registry.getEachReferenceTypeWithProperty("customProp"));
    Assert.assertFalse(refTypes.isEmpty());
    Assert.assertTrue(ImmutableList.copyOf(registry.getEachReferenceTypeWithProperty("nonExistent")).isEmpty());

    registry.unregisterPropertyOnType("customProp", objType);
    registry.unregisterPropertyOnType("nonExistentProp", objType);
  }

  @Test
  public void testRegisterPropertyOnUnionAndNamedType() {
    JSType numType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    ObjectType objType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    JSType union = registry.createUnionType(numType, objType);

    registry.registerPropertyOnType("unionProp", union);
    Assert.assertTrue(registry.canPropertyBeDefined(objType, "unionProp"));

    NamedType named = (NamedType) registry.createNamedType("Object", "dummy.js", 1, 1);
    named.resolveInternal(reporter, null);
    registry.registerPropertyOnType("namedProp", named);
    Assert.assertTrue(registry.canPropertyBeDefined(objType, "namedProp"));
  }

  @Test
  public void testInterfaceImplementors() {
    ObjectType iface = registry.createObjectType("MyInterface", null, null);
    FunctionType ctor = registry.createConstructorType("MyClass", null, null, null);
    registry.registerTypeImplementingInterface(ctor, iface);
    Assert.assertTrue(registry.getDirectImplementors(iface).contains(ctor));
  }

  @Test
  public void testFindCommonSuperObject() {
    ObjectType error = registry.getNativeObjectType(JSTypeNative.ERROR_TYPE);
    ObjectType typeError = registry.getNativeObjectType(JSTypeNative.TYPE_ERROR_TYPE);
    ObjectType evalError = registry.getNativeObjectType(JSTypeNative.EVAL_ERROR_TYPE);

    ObjectType common = registry.findCommonSuperObject(typeError, evalError);
    Assert.assertTrue(common.isEquivalentTo(error));

    ObjectType obj = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    ObjectType commonWithObj = registry.findCommonSuperObject(typeError, obj);
    Assert.assertTrue(commonWithObj.isEquivalentTo(obj));
  }

  @Test
  public void testGenerations() {
    registry.setLastGeneration(false);
    Assert.assertFalse(registry.isLastGeneration());
    registry.setLastGeneration(true);
    Assert.assertTrue(registry.isLastGeneration());

    StaticScope<JSType> scope = null;
    registry.getType(scope, "UnknownTypeFoo", "src.js", 1, 1);
    registry.resolveTypesInScope(scope);
    registry.incrementGeneration();
    registry.clearNamedTypes();
  }

  @Test
  public void testTypeCreationHelpers() {
    JSType num = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType str = registry.getNativeType(JSTypeNative.STRING_TYPE);

    JSType optNum = registry.createOptionalType(num);
    Assert.assertTrue(optNum.isUnionType());

    JSType optAll = registry.createOptionalType(registry.getNativeType(JSTypeNative.ALL_TYPE));
    Assert.assertSame(registry.getNativeType(JSTypeNative.ALL_TYPE), optAll);

    JSType nullNum = registry.createNullableType(num);
    Assert.assertTrue(nullNum.isUnionType());

    JSType optNullNum = registry.createOptionalNullableType(num);
    Assert.assertTrue(optNullNum.isUnionType());

    JSType defaultUnion = registry.createDefaultObjectUnion(num);
    Assert.assertTrue(defaultUnion.isUnionType());

    JSType unionNative = registry.createUnionType(JSTypeNative.NUMBER_TYPE, JSTypeNative.STRING_TYPE);
    Assert.assertTrue(unionNative.isUnionType());

    EnumType enumType = registry.createEnumType("MyEnum", null, num);
    Assert.assertNotNull(enumType);

    ArrowType arrow = registry.createArrowType(new Node(Token.PARAM_LIST));
    Assert.assertNotNull(arrow);

    ObjectType objProto = registry.getNativeObjectType(JSTypeNative.OBJECT_PROTOTYPE);
    ObjectType customObj = registry.createObjectType(objProto);
    Assert.assertNotNull(customObj);

    ObjectType anonObj = registry.createAnonymousObjectType();
    Assert.assertNotNull(anonObj);

    ObjectType nativeAnon = registry.createNativeAnonymousObjectType();
    Assert.assertNotNull(nativeAnon);

    Assert.assertTrue(registry.resetImplicitPrototype(customObj, anonObj));
    Assert.assertFalse(registry.resetImplicitPrototype(num, anonObj));

    ParameterizedType paramType = registry.createParameterizedType(
        registry.getNativeObjectType(JSTypeNative.ARRAY_TYPE), num);
    Assert.assertNotNull(paramType);

    FunctionType ifaceType = registry.createInterfaceType("AnInterface", new Node(Token.FUNCTION));
    Assert.assertNotNull(ifaceType);
  }

  @Test
  public void testFunctionTypeCreationVariants() {
    JSType num = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType str = registry.getNativeType(JSTypeNative.STRING_TYPE);
    ObjectType obj = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);

    FunctionType fn1 = registry.createFunctionType(num, str);
    Assert.assertNotNull(fn1);

    FunctionType fn2 = registry.createFunctionType(num, Collections.singletonList(str));
    Assert.assertNotNull(fn2);

    FunctionType fn3 = registry.createFunctionTypeWithVarArgs(num, str);
    Assert.assertNotNull(fn3);

    FunctionType fn4 = registry.createFunctionTypeWithVarArgs(num, Collections.singletonList(str));
    Assert.assertNotNull(fn4);

    FunctionType fn5 = registry.createFunctionType(num, true, str);
    Assert.assertNotNull(fn5);

    FunctionType fn6 = registry.createFunctionType(num, false, str);
    Assert.assertNotNull(fn6);

    JSType fn7 = registry.createFunctionType(obj, num, Collections.singletonList(str));
    Assert.assertNotNull(fn7);

    JSType fn8 = registry.createFunctionTypeWithVarArgs(obj, num, Collections.singletonList(str));
    Assert.assertNotNull(fn8);

    FunctionType ctor1 = registry.createConstructorType(num, str);
    Assert.assertNotNull(ctor1);

    FunctionType ctor2 = registry.createConstructorTypeWithVarArgs(num, str);
    Assert.assertNotNull(ctor2);

    FunctionType ctor3 = registry.createConstructorType(num, true, str);
    Assert.assertNotNull(ctor3);

    FunctionType ctor4 = registry.createConstructorType(num, false, str);
    Assert.assertNotNull(ctor4);

    FunctionType newReturnFn = registry.createFunctionTypeWithNewReturnType(fn1, str);
    Assert.assertSame(str, newReturnFn.getReturnType());

    FunctionType newThisFn = registry.createFunctionTypeWithNewThisType(fn1, obj);
    Assert.assertSame(obj, newThisFn.getTypeOfThis());
  }

  @Test
  public void testRecordTypeCreation() {
    Map<String, RecordProperty> map = new HashMap<String, RecordProperty>();
    map.put("x", new RecordProperty(registry.getNativeType(JSTypeNative.NUMBER_TYPE), null));
    RecordType record = registry.createRecordType(map);
    Assert.assertNotNull(record);
    Assert.assertTrue(record.hasProperty("x"));
  }

  @Test
  public void testCreateFromTypeNodesRecord() {
    Node lc = new Node(Token.LC);
    Node colon = new Node(Token.COLON, Node.newString("a"), Node.newString("number"));
    Node plain = Node.newString("'b'");
    lc.addChildToBack(colon);
    lc.addChildToBack(plain);

    JSType type = registry.createFromTypeNodes(lc, "test.js", null);
    Assert.assertTrue(type.isRecordType());
  }

  @Test
  public void testCreateFromTypeNodesDuplicateRecordField() {
    Node lc = new Node(Token.LC);
    Node field1 = Node.newString("dup");
    Node field2 = Node.newString("dup");
    lc.addChildToBack(field1);
    lc.addChildToBack(field2);

    JSType type = registry.createFromTypeNodes(lc, "test.js", null);
    Assert.assertTrue(type.isRecordType());
    Assert.assertFalse(reporter.warnings.isEmpty());
  }

  @Test
  public void testCreateFromTypeNodesBasicTokens() {
    Node bang = new Node(Token.BANG, Node.newString("number"));
    JSType bangType = registry.createFromTypeNodes(bang, "test.js", null);
    Assert.assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), bangType);

    Node qmarkVal = new Node(Token.QMARK, Node.newString("number"));
    JSType qmarkType = registry.createFromTypeNodes(qmarkVal, "test.js", null);
    Assert.assertTrue(qmarkType.isUnionType());

    Node qmarkEmpty = new Node(Token.QMARK);
    JSType unknownType = registry.createFromTypeNodes(qmarkEmpty, "test.js", null);
    Assert.assertSame(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), unknownType);

    Node equalsNode = new Node(Token.EQUALS, Node.newString("number"));
    JSType optType = registry.createFromTypeNodes(equalsNode, "test.js", null);
    Assert.assertTrue(optType.isUnionType());

    Node ellipsis = new Node(Token.ELLIPSIS, Node.newString("number"));
    JSType varargsType = registry.createFromTypeNodes(ellipsis, "test.js", null);
    Assert.assertTrue(varargsType.isUnionType());

    Node star = new Node(Token.STAR);
    Assert.assertSame(registry.getNativeType(JSTypeNative.ALL_TYPE), registry.createFromTypeNodes(star, "test.js", null));

    Node lb = new Node(Token.LB);
    Assert.assertSame(registry.getNativeType(JSTypeNative.ARRAY_TYPE), registry.createFromTypeNodes(lb, "test.js", null));

    Node pipe = new Node(Token.PIPE, Node.newString("number"), Node.newString("string"));
    JSType pipeType = registry.createFromTypeNodes(pipe, "test.js", null);
    Assert.assertTrue(pipeType.isUnionType());

    Node empty = new Node(Token.EMPTY);
    Assert.assertSame(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), registry.createFromTypeNodes(empty, "test.js", null));

    Node voidNode = new Node(Token.VOID);
    Assert.assertSame(registry.getNativeType(JSTypeNative.VOID_TYPE), registry.createFromTypeNodes(voidNode, "test.js", null));
  }

  @Test
  public void testCreateFromTypeNodesParameterizedAndIndexed() {
    Node arrayNode = Node.newString("Array");
    Node typeParams = new Node(Token.BLOCK, Node.newString("string"));
    arrayNode.addChildToBack(typeParams);
    JSType paramResult = registry.createFromTypeNodes(arrayNode, "test.js", null);
    Assert.assertTrue(paramResult.isUnionType() || paramResult.isParameterizedType());

    Node objectNode = Node.newString("Object");
    Node indexParams = new Node(Token.BLOCK, Node.newString("string"), Node.newString("number"));
    objectNode.addChildToBack(indexParams);
    JSType indexResult = registry.createFromTypeNodes(objectNode, "test.js", null);
    Assert.assertNotNull(indexResult);

    registry.identifyNonNullableName("CustomEnum");
    registry.declareType("CustomEnum", registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    Node enumNode = Node.newString("CustomEnum");
    JSType enumResult = registry.createFromTypeNodes(enumNode, "test.js", null);
    Assert.assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), enumResult);
  }

  @Test
  public void testCreateFromTypeNodesFunction() {
    Node fn = new Node(Token.FUNCTION);
    Node thisNode = new Node(Token.THIS, Node.newString("Object"));
    Node paramList = new Node(Token.PARAM_LIST);
    paramList.addChildToBack(new Node(Token.EQUALS, Node.newString("number")));
    paramList.addChildToBack(new Node(Token.ELLIPSIS));
    paramList.addChildToBack(new Node(Token.ELLIPSIS, Node.newString("string")));
    paramList.addChildToBack(Node.newString("boolean"));
    Node retNode = Node.newString("string");

    fn.addChildToBack(thisNode);
    fn.addChildToBack(paramList);
    fn.addChildToBack(retNode);

    JSType fnType = registry.createFromTypeNodes(fn, "test.js", null);
    Assert.assertTrue(fnType.isFunctionType());

    Node fnNew = new Node(Token.FUNCTION);
    Node newNode = new Node(Token.NEW, Node.newString("number"));
    fnNew.addChildToBack(newNode);
    fnNew.addChildToBack(new Node(Token.EMPTY));
    JSType fnNewType = registry.createFromTypeNodes(fnNew, "test.js", null);
    Assert.assertTrue(fnNewType.isFunctionType());
    Assert.assertFalse(reporter.warnings.isEmpty());
  }

  @Test
  public void testLazyExpressionsMode() {
    registry.setResolveMode(JSTypeRegistry.ResolveMode.LAZY_EXPRESSIONS);
    Node nodeWithNames = new Node(Token.PIPE, Node.newString("number"), Node.newString("string"));
    JSType expr = registry.createFromTypeNodes(nodeWithNames, "test.js", null);
    Assert.assertTrue(expr instanceof UnresolvedTypeExpression);

    Node nodeWithoutNames = new Node(Token.STAR);
    JSType resolvedDirectly = registry.createFromTypeNodes(nodeWithoutNames, "test.js", null);
    Assert.assertSame(registry.getNativeType(JSTypeNative.ALL_TYPE), resolvedDirectly);
  }

  @Test(expected = IllegalStateException.class)
  public void testCreateFromTypeNodesInvalidTokenThrows() {
    Node invalid = new Node(Token.CALL);
    registry.createFromTypeNodes(invalid, "test.js", null);
  }
}
