package com.google.javascript.rhino.jstype;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FunctionTypeTest {

  private JSTypeRegistry registry;
  private JSType numberType;
  private JSType stringType;
  private JSType booleanType;
  private ObjectType objectType;
  private ObjectType functionInstanceType;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(new SimpleErrorReporter());
    numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    objectType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    functionInstanceType = registry.getNativeObjectType(JSTypeNative.FUNCTION_INSTANCE_TYPE);
  }

  @Test
  public void testOrdinaryFunctionCreation() {
    Node fnNode = new Node(Token.FUNCTION);
    Node param1 = Node.newString(Token.NAME, "a");
    param1.setJSType(numberType);
    Node params = new Node(Token.LP, param1);

    ArrowType arrow = registry.createArrowType(params, stringType);
    FunctionType fn = new FunctionType(
        registry, "foo", fnNode, arrow, null, null, false, false);

    Assert.assertFalse(fn.isConstructor());
    Assert.assertFalse(fn.isInterface());
    Assert.assertTrue(fn.isOrdinaryFunction());
    Assert.assertTrue(fn.isFunctionType());
    Assert.assertTrue(fn.canBeCalled());
    Assert.assertFalse(fn.hasInstanceType());
    Assert.assertEquals("foo", fn.getName());
    Assert.assertEquals(fnNode, fn.getSource());
    Assert.assertEquals(stringType, fn.getReturnType());
    Assert.assertFalse(fn.isReturnTypeInferred());
    Assert.assertNotNull(fn.getInternalArrowType());
    Assert.assertNull(fn.getTemplateTypeName());
    Assert.assertNotNull(fn.getTypeOfThis());
  }

  @Test
  public void testConstructorCreation() {
    Node fnNode = new Node(Token.FUNCTION);
    ArrowType arrow = registry.createArrowType(new Node(Token.LP), numberType);
    FunctionType ctor = new FunctionType(
        registry, "MyClass", fnNode, arrow, null, "T", true, false);

    Assert.assertTrue(ctor.isConstructor());
    Assert.assertFalse(ctor.isInterface());
    Assert.assertFalse(ctor.isOrdinaryFunction());
    Assert.assertTrue(ctor.hasInstanceType());
    Assert.assertEquals("T", ctor.getTemplateTypeName());
    Assert.assertNotNull(ctor.getInstanceType());
    Assert.assertEquals(ctor.getInstanceType(), ctor.getTypeOfThis());
  }

  @Test
  public void testConstructorWithNoObjectType() {
    ObjectType noObj = registry.getNativeObjectType(JSTypeNative.NO_OBJECT_TYPE);
    ArrowType arrow = registry.createArrowType(new Node(Token.LP), numberType);
    FunctionType ctor = new FunctionType(
        registry, "NoObjClass", null, arrow, noObj, null, true, false);

    Assert.assertEquals(noObj, ctor.getInstanceType());
    // getTypeOfThis falls back to OBJECT_TYPE if isNoObjectType
    Assert.assertEquals(objectType, ctor.getTypeOfThis());
  }

  @Test
  public void testInterfaceCreation() {
    Node fnNode = new Node(Token.FUNCTION);
    FunctionType iface = FunctionType.forInterface(registry, "MyInterface", fnNode);

    Assert.assertTrue(iface.isInterface());
    Assert.assertFalse(iface.isConstructor());
    Assert.assertFalse(iface.isOrdinaryFunction());
    Assert.assertTrue(iface.hasInstanceType());
    Assert.assertNotNull(iface.getInstanceType());
    Assert.assertEquals("MyInterface", iface.getReferenceName());
  }

  @Test
  public void testIsInstanceType() {
    FunctionType u2u = registry.getNativeFunctionType(JSTypeNative.U2U_CONSTRUCTOR_TYPE);
    Assert.assertTrue(u2u.isInstanceType());

    FunctionType normalFn = registry.createFunctionType(numberType, new JSType[0]);
    Assert.assertFalse(normalFn.isInstanceType());
  }

  @Test
  public void testMinAndMaxArguments() {
    Node param1 = Node.newString(Token.NAME, "a");
    param1.setJSType(numberType);

    Node param2 = Node.newString(Token.NAME, "b");
    param2.setJSType(stringType);
    param2.setOptionalArg(true);

    Node param3 = Node.newString(Token.NAME, "c");
    param3.setJSType(booleanType);
    param3.setVarArgs(true);

    Node params = new Node(Token.LP, param1, param2, param3);
    ArrowType arrow = registry.createArrowType(params, numberType);
    FunctionType fn = new FunctionType(
        registry, "fn", null, arrow, null, null, false, false);

    Assert.assertEquals(1, fn.getMinArguments());
    Assert.assertEquals(Integer.MAX_VALUE, fn.getMaxArguments());

    Node fixedParams = new Node(Token.LP, param1.cloneNode(), param2.cloneNode());
    ArrowType arrowFixed = registry.createArrowType(fixedParams, numberType);
    FunctionType fnFixed = new FunctionType(
        registry, "fnFixed", null, arrowFixed, null, null, false, false);
    Assert.assertEquals(1, fnFixed.getMinArguments());
    Assert.assertEquals(2, fnFixed.getMaxArguments());

    // Empty params
    ArrowType arrowEmpty = registry.createArrowType(new Node(Token.LP), numberType);
    FunctionType fnEmpty = new FunctionType(
        registry, "fnEmpty", null, arrowEmpty, null, null, false, false);
    Assert.assertEquals(0, fnEmpty.getMinArguments());
    Assert.assertEquals(0, fnEmpty.getMaxArguments());
  }

  @Test
  public void testGetParameters() {
    Node param1 = Node.newString(Token.NAME, "x");
    Node params = new Node(Token.LP, param1);
    ArrowType arrow = registry.createArrowType(params, numberType);
    FunctionType fn = new FunctionType(
        registry, "fn", null, arrow, null, null, false, false);

    int count = 0;
    for (Node p : fn.getParameters()) {
      count++;
      Assert.assertEquals("x", p.getString());
    }
    Assert.assertEquals(1, count);

    ArrowType arrowNullParams = new ArrowType(registry, null, numberType);
    FunctionType fnNullParams = new FunctionType(
        registry, "fnNull", null, arrowNullParams, null, null, false, false);
    Assert.assertFalse(fnNullParams.getParameters().iterator().hasNext());
  }

  @Test
  public void testPrototypeHandling() {
    FunctionType ctor = registry.createConstructorType("Super", null, null, null);
    FunctionPrototypeType proto = ctor.getPrototype();
    Assert.assertNotNull(proto);
    Assert.assertTrue(ctor.hasCachedValues());

    // Setting null prototype returns false
    Assert.assertFalse(ctor.setPrototype(null));

    // Setting prototype to getInstanceType() on constructor returns false
    Assert.assertFalse(ctor.setPrototype((FunctionPrototypeType) (ObjectType) ctor.getInstanceType()));

    // setPrototypeBasedOn
    FunctionType subCtor = registry.createConstructorType("Sub", null, null, null);
    subCtor.setPrototypeBasedOn(ctor.getInstanceType());
    Assert.assertEquals(ctor.getInstanceType(), subCtor.getPrototype().getImplicitPrototype());

    // Call setPrototypeBasedOn again when prototype already exists
    subCtor.setPrototypeBasedOn(objectType);
    Assert.assertEquals(objectType, subCtor.getPrototype().getImplicitPrototype());
  }

  @Test
  public void testSuperClassConstructorAndSubTypes() {
    FunctionType superClass = registry.createConstructorType("Super", null, null, null);
    FunctionType subClass = registry.createConstructorType("Sub", null, null, null);

    subClass.setPrototypeBasedOn(superClass.getInstanceType());

    Assert.assertEquals(superClass, subClass.getSuperClassConstructor());
    Assert.assertNotNull(superClass.getSubTypes());
    Assert.assertTrue(superClass.getSubTypes().contains(subClass));
  }

  @Test
  public void testHasUnknownSupertype() {
    FunctionType ctor = registry.createConstructorType("Foo", null, null, null);
    Assert.assertFalse(ctor.hasUnknownSupertype());

    FunctionType badCtor = registry.createConstructorType("Bad", null, null, null);
    badCtor.getPrototype().setImplicitPrototype(registry.getNativeObjectType(JSTypeNative.UNKNOWN_TYPE));
    Assert.assertTrue(badCtor.hasUnknownSupertype());
  }

  @Test
  public void testGetTopMostDefiningType() {
    FunctionType superCtor = registry.createConstructorType("Super", null, null, null);
    superCtor.getPrototype().defineDeclaredProperty("foo", numberType, false);

    FunctionType subCtor = registry.createConstructorType("Sub", null, null, null);
    subCtor.setPrototypeBasedOn(superCtor.getInstanceType());

    JSType top = subCtor.getTopMostDefiningType("foo");
    Assert.assertEquals(superCtor.getInstanceType(), top);
  }

  @Test
  public void testInterfaces() {
    FunctionType iface1 = FunctionType.forInterface(registry, "I1", null);
    FunctionType iface2 = FunctionType.forInterface(registry, "I2", null);

    FunctionType ctor = registry.createConstructorType("Impl", null, null, null);
    ctor.setImplementedInterfaces(ImmutableList.of(iface1.getInstanceType(), iface2.getInstanceType()));

    List<ObjectType> direct = Lists.newArrayList(ctor.getImplementedInterfaces());
    Assert.assertEquals(2, direct.size());

    Set<ObjectType> all = new HashSet<ObjectType>();
    for (ObjectType iface : ctor.getAllImplementedInterfaces()) {
      all.add(iface);
    }
    Assert.assertTrue(all.contains(iface1.getInstanceType()));
    Assert.assertTrue(all.contains(iface2.getInstanceType()));
  }

  @Test
  public void testInterfaceInheritance() {
    FunctionType baseIface = FunctionType.forInterface(registry, "BaseIface", null);
    FunctionType subIface = FunctionType.forInterface(registry, "SubIface", null);
    subIface.getPrototype().setImplicitPrototype(baseIface.getInstanceType());

    FunctionType ctor = registry.createConstructorType("C", null, null, null);
    ctor.setImplementedInterfaces(ImmutableList.of(subIface.getInstanceType()));

    Set<ObjectType> all = new HashSet<ObjectType>();
    for (ObjectType iface : ctor.getAllImplementedInterfaces()) {
      all.add(iface);
    }
    Assert.assertTrue(all.contains(subIface.getInstanceType()));
    Assert.assertTrue(all.contains(baseIface.getInstanceType()));
  }

  @Test
  public void testHasPropertyAndGetPropertyType() {
    FunctionType fn = registry.createFunctionType(numberType, new JSType[0]);
    Assert.assertTrue(fn.hasProperty("prototype"));
    Assert.assertTrue(fn.hasOwnProperty("prototype"));
    Assert.assertTrue(fn.isPropertyTypeInferred("prototype"));
    Assert.assertNotNull(fn.getPropertyType("prototype"));

    // Lazy "call" property
    Assert.assertTrue(fn.hasProperty("call"));
    JSType callProp = fn.getPropertyType("call");
    Assert.assertNotNull(callProp);
    Assert.assertTrue(callProp.isFunctionType());

    // Lazy "apply" property
    Assert.assertTrue(fn.hasProperty("apply"));
    JSType applyProp = fn.getPropertyType("apply");
    Assert.assertNotNull(applyProp);
    Assert.assertTrue(applyProp.isFunctionType());
  }

  @Test
  public void testLazyCallPropertyWithNullParams() {
    ArrowType arrowNull = new ArrowType(registry, null, numberType);
    FunctionType fn = new FunctionType(
        registry, "fn", null, arrowNull, null, null, false, false);
    JSType callProp = fn.getPropertyType("call");
    Assert.assertNotNull(callProp);
    Assert.assertTrue(callProp.isFunctionType());
  }

  @Test
  public void testDefineProperty() {
    FunctionType fn = registry.createFunctionType(numberType, new JSType[0]);
    ObjectType newProto = registry.createObjectType("NewProto", null, objectType);
    boolean res = fn.defineProperty("prototype", newProto, false, false);
    Assert.assertTrue(res);

    // Re-defining same prototype is a no-op true
    Assert.assertTrue(fn.defineProperty("prototype", fn.getPrototype(), false, false));

    // Defining non-object prototype returns false
    Assert.assertFalse(fn.defineProperty("prototype", numberType, false, false));

    // Regular property definition
    Assert.assertTrue(fn.defineProperty("customProp", stringType, false, false));
    Assert.assertEquals(stringType, fn.getPropertyType("customProp"));
  }

  @Test
  public void testSubtyping() {
    FunctionType fn1 = registry.createFunctionType(numberType, numberType);
    FunctionType fn2 = registry.createFunctionType(numberType, numberType);

    Assert.assertTrue(fn1.isSubtype(fn2));
    Assert.assertTrue(fn2.isSubtype(fn1));

    // Interface subtyping
    FunctionType iface = FunctionType.forInterface(registry, "InterfaceA", null);
    Assert.assertTrue(fn1.isSubtype(iface));
    Assert.assertFalse(iface.isSubtype(fn1));

    // Subtyping against non-function object
    Assert.assertTrue(fn1.isSubtype(objectType));
  }

  @Test
  public void testEquivalenceAndHashCode() {
    FunctionType fn1 = registry.createFunctionType(numberType, numberType);
    FunctionType fn2 = registry.createFunctionType(numberType, numberType);
    FunctionType fn3 = registry.createFunctionType(stringType, numberType);

    Assert.assertTrue(fn1.isEquivalentTo(fn2));
    Assert.assertFalse(fn1.isEquivalentTo(fn3));
    Assert.assertEquals(fn1.hashCode(), fn2.hashCode());
    Assert.assertTrue(fn1.hasEqualCallType(fn2));

    FunctionType iface1 = FunctionType.forInterface(registry, "I", null);
    FunctionType iface2 = FunctionType.forInterface(registry, "I", null);
    FunctionType iface3 = FunctionType.forInterface(registry, "OtherI", null);

    Assert.assertTrue(iface1.isEquivalentTo(iface2));
    Assert.assertFalse(iface1.isEquivalentTo(iface3));
    Assert.assertEquals(iface1.hashCode(), iface2.hashCode());

    // Non-function equivalence
    Assert.assertFalse(fn1.isEquivalentTo(numberType));
  }

  @Test
  public void testLeastSupertypeAndGreatestSubtype() {
    FunctionType fn1 = registry.createFunctionType(numberType, numberType);
    FunctionType fn2 = registry.createFunctionType(numberType, numberType);

    JSType sup = fn1.getLeastSupertype(fn2);
    Assert.assertEquals(fn1, sup);

    JSType inf = fn1.getGreatestSubtype(fn2);
    Assert.assertEquals(fn1, inf);

    // Merge ordinary functions piecewise
    FunctionType fnNumToNum = registry.createFunctionType(numberType, numberType);
    FunctionType fnStrToNum = registry.createFunctionType(stringType, numberType);

    JSType mergedSup = fnNumToNum.getLeastSupertype(fnStrToNum);
    Assert.assertTrue(mergedSup.isFunctionType());

    JSType mergedInf = fnNumToNum.getGreatestSubtype(fnStrToNum);
    Assert.assertTrue(mergedInf.isFunctionType());

    // Subtype ordering degenerate cases
    FunctionType subFn = registry.createFunctionType(numberType, numberType, stringType);
    FunctionType superFn = registry.createFunctionType(numberType, numberType);
    Assert.assertNotNull(subFn.getLeastSupertype(superFn));
    Assert.assertNotNull(subFn.getGreatestSubtype(superFn));

    // With function instance type
    JSType fnInst = registry.getNativeType(JSTypeNative.FUNCTION_INSTANCE_TYPE);
    Assert.assertEquals(fnInst, fn1.getLeastSupertype(fnInst));
    Assert.assertEquals(fn1, fn1.getGreatestSubtype(fnInst));
    Assert.assertEquals(fnInst, fnInst.getLeastSupertype(fn1));
    Assert.assertEquals(fn1, fnInst.getGreatestSubtype(fn1));

    // With non-function type
    Assert.assertNotNull(fn1.getLeastSupertype(numberType));
    Assert.assertNotNull(fn1.getGreatestSubtype(numberType));
  }

  @Test
  public void testToStringAndDebugHashCodeString() {
    FunctionType fn = registry.createFunctionType(numberType, numberType, stringType);
    String str = fn.toString();
    Assert.assertTrue(str.startsWith("function ("));
    Assert.assertTrue(str.endsWith("): number"));

    String debugStr = fn.toDebugHashCodeString();
    Assert.assertTrue(debugStr.startsWith("function ("));

    // Function instance type string
    JSType fnInst = registry.getNativeType(JSTypeNative.FUNCTION_INSTANCE_TYPE);
    Assert.assertEquals("Function", fnInst.toString());
    Assert.assertNotNull(fnInst.toDebugHashCodeString());

    // Varargs function toString
    Node varParam = Node.newString(Token.NAME, "rest");
    varParam.setJSType(registry.createUnionType(stringType, registry.getNativeType(JSTypeNative.VOID_TYPE)));
    varParam.setVarArgs(true);
    ArrowType arrowVar = registry.createArrowType(new Node(Token.LP, varParam), numberType);
    FunctionType fnVar = new FunctionType(
        registry, "fnVar", null, arrowVar, null, null, false, false);
    Assert.assertTrue(fnVar.toString().contains("...[string]"));
  }

  @Test
  public void testVisitor() {
    FunctionType fn = registry.createFunctionType(numberType, new JSType[0]);
    Visitor<String> visitor = new Visitor<String>() {
      @Override
      public String caseNoType() { return "no"; }
      @Override
      public String caseEnumElementType(EnumElementType type) { return "enumElem"; }
      @Override
      public String caseAllType() { return "all"; }
      @Override
      public String caseBooleanType() { return "bool"; }
      @Override
      public String caseNoObjectType() { return "noObj"; }
      @Override
      public String caseFunctionType(FunctionType type) { return "function"; }
      @Override
      public String caseObjectType(ObjectType type) { return "obj"; }
      @Override
      public String caseUnknownType() { return "unknown"; }
      @Override
      public String caseNullType() { return "null"; }
      @Override
      public String caseNamedType(NamedType type) { return "named"; }
      @Override
      public String caseNumberType() { return "num"; }
      @Override
      public String caseStringType() { return "str"; }
      @Override
      public String caseVoidType() { return "void"; }
      @Override
      public String caseUnionType(UnionType type) { return "union"; }
      @Override
      public String caseTemplateType(TemplateType type) { return "template"; }
    };

    Assert.assertEquals("function", fn.visit(visitor));
  }

  @Test
  public void testSetSourceAndSetInstanceType() {
    FunctionType fn = registry.createFunctionType(numberType, new JSType[0]);
    Node src = new Node(Token.FUNCTION);
    fn.setSource(src);
    Assert.assertEquals(src, fn.getSource());

    fn.setInstanceType(objectType);
    Assert.assertEquals(objectType, fn.getTypeOfThis());
  }

  @Test
  public void testResolveInternal() {
    SimpleErrorReporter reporter = new SimpleErrorReporter();
    FunctionType iface = FunctionType.forInterface(registry, "Iface", null);
    FunctionType ctor = registry.createConstructorType("MyClass", null, null, null);
    ctor.setImplementedInterfaces(ImmutableList.of(iface.getInstanceType()));

    FunctionType subCtor = registry.createConstructorType("MySubClass", null, null, null);
    subCtor.setPrototypeBasedOn(ctor.getInstanceType());

    JSType resolved = ctor.resolve(reporter, null);
    Assert.assertNotNull(resolved);
    Assert.assertTrue(resolved.isResolved());
  }

  @Test(expected = IllegalStateException.class)
  public void testGetInstanceTypeOnOrdinaryFunctionThrows() {
    FunctionType fn = registry.createFunctionType(numberType, new JSType[0]);
    fn.getInstanceType();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithInvalidSourceNodeThrows() {
    Node invalidNode = new Node(Token.VAR);
    ArrowType arrow = registry.createArrowType(new Node(Token.LP), numberType);
    new FunctionType(registry, "test", invalidNode, arrow, null, null, false, false);
  }
}