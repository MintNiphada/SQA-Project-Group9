package com.google.javascript.rhino.jstype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;
import com.google.javascript.rhino.Token;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class FunctionTypeTest {

  private JSTypeRegistry registry;
  private SimpleErrorReporter errorReporter;

  private JSType numberType;
  private JSType stringType;
  private JSType booleanType;
  private JSType voidType;
  private ObjectType objectType;
  private ObjectType functionInstanceType;

  @Before
  public void setUp() {
    errorReporter = new SimpleErrorReporter();
    registry = new JSTypeRegistry(errorReporter);

    numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    voidType = registry.getNativeType(JSTypeNative.VOID_TYPE);
    objectType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    functionInstanceType = registry.getNativeObjectType(JSTypeNative.FUNCTION_INSTANCE_TYPE);
  }

  @Test
  public void testOrdinaryFunctionConstructors() {
    FunctionType fn1 = new FunctionType(registry, "f1", null, null, numberType);
    assertTrue(fn1.isOrdinaryFunction());
    assertFalse(fn1.isConstructor());
    assertFalse(fn1.isInterface());
    assertTrue(fn1.isFunctionType());
    assertTrue(fn1.canBeCalled());
    assertEquals("f1", fn1.getReferenceName());
    assertEquals(numberType, fn1.getReturnType());
    assertNull(fn1.getParametersNode());
    assertNull(fn1.getSource());
    assertNull(fn1.getTemplateTypeName());
    assertEquals(registry.getNativeObjectType(JSTypeNative.UNKNOWN_TYPE), fn1.getTypeOfThis());

    FunctionType fn2 = new FunctionType(registry, "f2", null, null, stringType, objectType);
    assertEquals(objectType, fn2.getTypeOfThis());

    FunctionType fn3 = new FunctionType(registry, "f3", null, null, booleanType, objectType, "T");
    assertEquals("T", fn3.getTemplateTypeName());
  }

  @Test
  public void testConstructorFunctionCreation() {
    Node fnNode = new Node(Token.FUNCTION);
    FunctionType ctor = new FunctionType(
        registry, "MyClass", fnNode, null, null, null, null, true, false);

    assertTrue(ctor.isConstructor());
    assertFalse(ctor.isOrdinaryFunction());
    assertFalse(ctor.isInterface());
    assertTrue(ctor.hasInstanceType());
    assertNotNull(ctor.getInstanceType());
    assertEquals(fnNode, ctor.getSource());

    // Setting source node
    Node newFnNode = new Node(Token.FUNCTION);
    ctor.setSource(newFnNode);
    assertEquals(newFnNode, ctor.getSource());

    // Changing instance type
    ObjectType customInstance = new InstanceObjectType(registry, ctor, false);
    ctor.setInstanceType(customInstance);
    assertSame(customInstance, ctor.getInstanceType());
  }

  @Test
  public void testInterfaceFunctionCreation() {
    Node fnNode = new Node(Token.FUNCTION);
    FunctionType iface = new FunctionType(registry, "MyInterface", fnNode);

    assertTrue(iface.isInterface());
    assertFalse(iface.isConstructor());
    assertFalse(iface.isOrdinaryFunction());
    assertTrue(iface.hasInstanceType());
    assertNotNull(iface.getInstanceType());
    assertNull(iface.getReturnType());
    assertNull(iface.getParametersNode());
    assertEquals("MyInterface", iface.getReferenceName());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInterfaceCreationWithNullNameThrows() {
    new FunctionType(registry, null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidSourceNodeTypeThrows() {
    Node invalidNode = new Node(Token.NAME);
    new FunctionType(registry, "invalid", invalidNode, null, numberType);
  }

  @Test(expected = IllegalStateException.class)
  public void testGetInstanceTypeOnOrdinaryFunctionThrows() {
    FunctionType fn = new FunctionType(registry, "foo", null, null, numberType);
    fn.getInstanceType();
  }

  @Test
  public void testIsInstanceType() {
    FunctionType fn = new FunctionType(registry, "f", null, null, numberType);
    assertFalse(fn.isInstanceType());

    JSType u2u = registry.getNativeType(JSTypeNative.U2U_CONSTRUCTOR_TYPE);
    if (u2u instanceof FunctionType) {
      assertTrue(u2u.isInstanceType());
    }
  }

  @Test
  public void testTypeOfThisHandlingNoObjectType() {
    ObjectType noObjectType = registry.getNativeObjectType(JSTypeNative.NO_OBJECT_TYPE);
    FunctionType ctor = new FunctionType(
        registry, "NoObjCtor", null, null, null, noObjectType, null, true, false);
    assertEquals(objectType, ctor.getTypeOfThis());
  }

  @Test
  public void testParametersAndArgumentCounts() {
    FunctionType noParams = new FunctionType(registry, "noParams", null, null, numberType);
    assertEquals(0, noParams.getMinArguments());
    assertEquals(Integer.MAX_VALUE, noParams.getMaxArguments());
    assertFalse(noParams.getParameters().iterator().hasNext());

    Node paramList = new Node(Token.LP);
    Node req1 = Node.newString(Token.NAME, "req1");
    req1.setJSType(numberType);
    Node opt1 = Node.newString(Token.NAME, "opt1");
    opt1.setJSType(stringType);
    opt1.setOptionalArg(true);
    Node req2 = Node.newString(Token.NAME, "req2");
    req2.setJSType(booleanType);
    paramList.addChildToBack(req1);
    paramList.addChildToBack(opt1);
    paramList.addChildToBack(req2);

    FunctionType fn = new FunctionType(registry, "fn", null, paramList, voidType);
    assertEquals(3, fn.getMinArguments());
    assertEquals(3, fn.getMaxArguments());

    int count = 0;
    for (Node p : fn.getParameters()) {
      assertNotNull(p);
      count++;
    }
    assertEquals(3, count);

    Node paramListVarArgs = new Node(Token.LP);
    Node reqA = Node.newString(Token.NAME, "a");
    reqA.setJSType(numberType);
    Node varArg = Node.newString(Token.NAME, "rest");
    varArg.setJSType(stringType);
    varArg.setVarArgs(true);
    paramListVarArgs.addChildToBack(reqA);
    paramListVarArgs.addChildToBack(varArg);

    FunctionType fnVar = new FunctionType(registry, "fnVar", null, paramListVarArgs, voidType);
    assertEquals(1, fnVar.getMinArguments());
    assertEquals(Integer.MAX_VALUE, fnVar.getMaxArguments());
  }

  @Test
  public void testPrototypeLazyInitializationAndSetting() {
    FunctionType fn = new FunctionType(registry, "fn", null, null, numberType);
    assertFalse(fn.hasCachedValues());

    FunctionPrototypeType proto = fn.getPrototype();
    assertNotNull(proto);
    assertSame(proto, fn.getPrototype());
    assertTrue(fn.hasCachedValues());

    assertFalse(fn.setPrototype(null));

    FunctionPrototypeType newProto = new FunctionPrototypeType(registry, fn, null);
    assertTrue(fn.setPrototype(newProto));
    assertSame(newProto, fn.getPrototype());

    fn.setPrototypeBasedOn(objectType);
    assertEquals(objectType, fn.getPrototype().getImplicitPrototype());

    FunctionType freshFn = new FunctionType(registry, "fresh", null, null, numberType);
    freshFn.setPrototypeBasedOn(objectType);
    assertNotNull(freshFn.getPrototype());
    assertEquals(objectType, freshFn.getPrototype().getImplicitPrototype());
  }

  @Test
  public void testSetPrototypeCannotBeSameAsInstanceType() {
    FunctionType ctor = new FunctionType(
        registry, "Ctor", null, null, null, null, null, true, false);
    FunctionPrototypeType invalidProto = (FunctionPrototypeType) (ObjectType) ctor.getInstanceType();
    assertFalse(ctor.setPrototype(invalidProto));
  }

  @Test
  public void testSuperClassConstructorAndSubTypes() {
    FunctionType parentCtor = new FunctionType(
        registry, "Parent", null, null, null, null, null, true, false);
    FunctionType childCtor = new FunctionType(
        registry, "Child", null, null, null, null, null, true, false);

    assertNull(childCtor.getSuperClassConstructor());
    assertNull(parentCtor.getSubTypes());

    childCtor.setPrototypeBasedOn(parentCtor.getInstanceType());

    assertEquals(parentCtor, childCtor.getSuperClassConstructor());
    assertNotNull(parentCtor.getSubTypes());
    assertEquals(1, parentCtor.getSubTypes().size());
    assertSame(childCtor, parentCtor.getSubTypes().get(0));
  }

  @Test
  public void testImplementedInterfacesHierarchy() {
    FunctionType iface1 = new FunctionType(registry, "I1", null);
    FunctionType iface2 = new FunctionType(registry, "I2", null);
    FunctionType iface3 = new FunctionType(registry, "I3", null);

    iface2.setPrototypeBasedOn(iface1.getInstanceType());
    assertEquals(iface1, iface2.getSuperClassConstructor());

    FunctionType parentCtor = new FunctionType(
        registry, "Parent", null, null, null, null, null, true, false);
    parentCtor.setImplementedInterfaces(ImmutableList.of(iface3.getInstanceType()));

    FunctionType childCtor = new FunctionType(
        registry, "Child", null, null, null, null, null, true, false);
    childCtor.setPrototypeBasedOn(parentCtor.getInstanceType());
    childCtor.setImplementedInterfaces(ImmutableList.of(iface2.getInstanceType()));

    Iterable<ObjectType> directAndSuperIfaces = childCtor.getImplementedInterfaces();
    List<ObjectType> directList = Lists.newArrayList(directAndSuperIfaces);
    assertEquals(2, directList.size());
    assertTrue(directList.contains(iface2.getInstanceType()));
    assertTrue(directList.contains(iface3.getInstanceType()));

    Iterable<ObjectType> allIfaces = childCtor.getAllImplementedInterfaces();
    List<ObjectType> allList = Lists.newArrayList(allIfaces);
    assertEquals(3, allList.size());
    assertTrue(allList.contains(iface1.getInstanceType()));
    assertTrue(allList.contains(iface2.getInstanceType()));
    assertTrue(allList.contains(iface3.getInstanceType()));
  }

  @Test
  public void testPropertiesAndDynamicCallApply() {
    Node paramList = new Node(Token.LP);
    Node param1 = Node.newString(Token.NAME, "arg");
    param1.setJSType(numberType);
    paramList.addChildToBack(param1);

    FunctionType fn = new FunctionType(registry, "myFunc", null, paramList, stringType, objectType);

    assertTrue(fn.hasProperty("prototype"));
    assertTrue(fn.isPropertyTypeInferred("prototype"));
    assertEquals(fn.getPrototype(), fn.getPropertyType("prototype"));

    JSType callPropType = fn.getPropertyType("call");
    assertTrue(callPropType.isFunctionType());
    FunctionType callFn = (FunctionType) callPropType;
    assertEquals(stringType, callFn.getReturnType());
    assertNotNull(callFn.getParametersNode());

    JSType applyPropType = fn.getPropertyType("apply");
    assertTrue(applyPropType.isFunctionType());
    FunctionType applyFn = (FunctionType) applyPropType;
    assertEquals(stringType, applyFn.getReturnType());

    FunctionType fnNoParams = new FunctionType(registry, "noParams", null, null, booleanType);
    JSType callNoParams = fnNoParams.getPropertyType("call");
    assertTrue(callNoParams.isFunctionType());
    assertEquals(booleanType, ((FunctionType) callNoParams).getReturnType());

    assertTrue(fn.defineProperty("prototype", objectType, false, false));
    assertFalse(fn.defineProperty("prototype", numberType, false, false));

    fn.defineProperty("customProp", numberType, true, false);
    assertTrue(fn.hasProperty("customProp"));
    assertTrue(fn.isPropertyTypeInferred("customProp"));
    assertEquals(numberType, fn.getPropertyType("customProp"));
  }

  @Test
  public void testLeastSupertypeAndGreatestSubtype() {
    FunctionType fn1 = new FunctionType(registry, "f1", null, null, numberType);
    FunctionType fn2 = new FunctionType(registry, "f2", null, null, stringType);

    assertSame(fn1, fn1.getLeastSupertype(fn1));
    assertSame(fn1, fn1.getGreatestSubtype(fn1));

    JSType fnInst = registry.getNativeType(JSTypeNative.FUNCTION_INSTANCE_TYPE);
    assertSame(fnInst, fn1.getLeastSupertype(fnInst));
    assertSame(fnInst, ((FunctionType) fnInst).getLeastSupertype(fn1));

    assertSame(fn1, fn1.getGreatestSubtype(fnInst));
    assertSame(fn1, ((FunctionType) fnInst).getGreatestSubtype(fn1));

    JSType u2u = registry.getNativeType(JSTypeNative.U2U_CONSTRUCTOR_TYPE);
    assertEquals(u2u, fn1.getLeastSupertype(fn2));

    JSType noObj = registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE);
    assertEquals(noObj, fn1.getGreatestSubtype(fn2));

    assertEquals(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE),
        fn1.getLeastSupertype(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE)));
    assertEquals(registry.getNativeType(JSTypeNative.NO_TYPE),
        fn1.getGreatestSubtype(numberType));
  }

  @Test
  public void testHasUnknownSupertype() {
    FunctionType base = new FunctionType(
        registry, "Base", null, null, null, null, null, true, false);
    assertFalse(base.hasUnknownSupertype());

    FunctionType sub = new FunctionType(
        registry, "Sub", null, null, null, null, null, true, false);
    sub.setPrototypeBasedOn(base.getInstanceType());
    assertFalse(sub.hasUnknownSupertype());

    FunctionType unknownSuper = new FunctionType(
        registry, "UnknownSuper", null, null, null, null, null, true, false);
    unknownSuper.setPrototypeBasedOn(registry.getNativeObjectType(JSTypeNative.UNKNOWN_TYPE));
    assertTrue(unknownSuper.hasUnknownSupertype());
  }

  @Test
  public void testGetTopMostDefiningType() {
    FunctionType base = new FunctionType(
        registry, "Base", null, null, null, null, null, true, false);
    base.getPrototype().defineProperty("prop", numberType, false, false);

    FunctionType mid = new FunctionType(
        registry, "Mid", null, null, null, null, null, true, false);
    mid.setPrototypeBasedOn(base.getInstanceType());
    mid.getPrototype().defineProperty("prop", numberType, false, false);

    FunctionType sub = new FunctionType(
        registry, "Sub", null, null, null, null, null, true, false);
    sub.setPrototypeBasedOn(mid.getInstanceType());
    sub.getPrototype().defineProperty("prop", numberType, false, false);

    assertEquals(base.getInstanceType(), sub.getTopMostDefiningType("prop"));
  }

  @Test
  public void testEqualsAndHashCode() {
    FunctionType fn1 = new FunctionType(registry, "f", null, null, numberType, objectType);
    FunctionType fn2 = new FunctionType(registry, "f", null, null, numberType, objectType);
    FunctionType fn3 = new FunctionType(registry, "f", null, null, stringType, objectType);

    assertEquals(fn1, fn2);
    assertEquals(fn1.hashCode(), fn2.hashCode());
    assertFalse(fn1.equals(fn3));
    assertFalse(fn1.equals(null));
    assertFalse(fn1.equals("string"));

    FunctionType ctor1 = new FunctionType(
        registry, "C1", null, null, null, null, null, true, false);
    FunctionType ctor2 = new FunctionType(
        registry, "C1", null, null, null, null, null, true, false);
    assertFalse(ctor1.equals(ctor2));
    assertEquals(ctor1, ctor1);

    FunctionType iface1 = new FunctionType(registry, "I", null);
    FunctionType iface2 = new FunctionType(registry, "I", null);
    FunctionType iface3 = new FunctionType(registry, "OtherI", null);
    assertEquals(iface1, iface2);
    assertEquals(iface1.hashCode(), iface2.hashCode());
    assertFalse(iface1.equals(iface3));
    assertFalse(iface1.equals(fn1));
    assertFalse(fn1.equals(iface1));
    assertFalse(ctor1.equals(iface1));

    assertTrue(fn1.hasEqualCallType(fn2));
    assertFalse(fn1.hasEqualCallType(fn3));
  }

  @Test
  public void testToStringFormatting() {
    FunctionType fnInst = (FunctionType) registry.getNativeType(JSTypeNative.FUNCTION_INSTANCE_TYPE);
    assertEquals("Function", fnInst.toString());

    FunctionType emptyFn = new FunctionType(registry, null, null, null, null);
    assertEquals("function (): ?", emptyFn.toString());

    Node params = new Node(Token.LP);
    Node p1 = Node.newString(Token.NAME, "a");
    p1.setJSType(numberType);
    Node p2 = Node.newString(Token.NAME, "rest");
    JSType unionVar = registry.createUnionType(stringType, voidType);
    p2.setJSType(unionVar);
    p2.setVarArgs(true);
    params.addChildToBack(p1);
    params.addChildToBack(p2);

    FunctionType fn = new FunctionType(registry, null, null, params, booleanType, objectType);
    assertEquals("function (this:Object, number, ...[string]): boolean", fn.toString());
  }

  @Test
  public void testIsSubtype() {
    FunctionType fn1 = new FunctionType(registry, "f1", null, null, numberType);
    FunctionType fn2 = new FunctionType(registry, "f2", null, null, numberType);
    assertTrue(fn1.isSubtype(fn2));

    FunctionType iface = new FunctionType(registry, "I", null);
    assertTrue(fn1.isSubtype(iface));
    assertFalse(iface.isSubtype(fn1));

    JSType union = registry.createUnionType(fn2, stringType);
    assertTrue(fn1.isSubtype(union));
    assertFalse(fn1.isSubtype(stringType));

    FunctionType ctor = new FunctionType(
        registry, "C", null, null, null, null, null, true, false);
    assertTrue(ctor.isSubtype(functionInstanceType));
  }

  @Test
  public void testVisitorPattern() throws Exception {
    FunctionType fn = new FunctionType(registry, "f", null, null, numberType);

    Class<?> visitorClass = Class.forName("com.google.javascript.rhino.jstype.Visitor");
    Object visitorProxy = Proxy.newProxyInstance(
        visitorClass.getClassLoader(),
        new Class<?>[] { visitorClass },
        new InvocationHandler() {
          @Override
          public Object invoke(Object proxy, Method method, Object[] args) {
            if ("caseFunctionType".equals(method.getName())) {
              return "visitedFunction";
            }
            return null;
          }
        });

    Method visitMethod = FunctionType.class.getMethod("visit", visitorClass);
    Object result = visitMethod.invoke(fn, visitorProxy);
    assertEquals("visitedFunction", result);
  }

  @Test
  public void testResolveInternal() {
    FunctionType ctor = new FunctionType(
        registry, "Ctor", null, null, null, null, null, true, false);
    FunctionType iface = new FunctionType(registry, "Iface", null);
    ctor.setImplementedInterfaces(ImmutableList.of(iface.getInstanceType()));

    FunctionType subCtor = new FunctionType(
        registry, "SubCtor", null, null, null, null, null, true, false);
    subCtor.setPrototypeBasedOn(ctor.getInstanceType());

    JSType resolved = ctor.resolve(errorReporter, null);
    assertNotNull(resolved);
    assertTrue(resolved.isFunctionType());
  }
}