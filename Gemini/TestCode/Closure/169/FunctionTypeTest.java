package com.google.javascript.rhino.jstype;

import com.google.common.collect.ImmutableList;
import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class FunctionTypeTest {
  private JSTypeRegistry registry;
  private ObjectType objectType;
  private JSType numberType;
  private JSType stringType;
  private JSType booleanType;
  private JSType unknownType;
  private JSType voidType;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(null);
    objectType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    unknownType = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    voidType = registry.getNativeType(JSTypeNative.VOID_TYPE);
  }

  @Test
  public void testConstructorAndKind() {
    FunctionType ctor = registry.createConstructorType("Foo", null, null, null);
    Assert.assertTrue(ctor.isConstructor());
    Assert.assertFalse(ctor.isInterface());
    Assert.assertFalse(ctor.isOrdinaryFunction());
    Assert.assertTrue(ctor.canBeCalled());
    Assert.assertSame(ctor, ctor.toMaybeFunctionType());
    Assert.assertTrue(ctor.hasInstanceType());
    Assert.assertNotNull(ctor.getInstanceType());
    Assert.assertEquals("Foo", ctor.getReferenceName());
  }

  @Test
  public void testInterfaceKind() {
    Node fnNode = new Node(Token.FUNCTION);
    FunctionType iface = FunctionType.forInterface(registry, "IFoo", fnNode);
    Assert.assertFalse(iface.isConstructor());
    Assert.assertTrue(iface.isInterface());
    Assert.assertFalse(iface.isOrdinaryFunction());
    Assert.assertTrue(iface.hasInstanceType());
    Assert.assertEquals("IFoo", iface.getReferenceName());
    Assert.assertSame(fnNode, iface.getSource());
  }

  @Test
  public void testOrdinaryFunction() {
    FunctionType fn = registry.createFunctionType(numberType, stringType);
    Assert.assertFalse(fn.isConstructor());
    Assert.assertFalse(fn.isInterface());
    Assert.assertTrue(fn.isOrdinaryFunction());
    Assert.assertFalse(fn.hasInstanceType());
    Assert.assertEquals(numberType, fn.getReturnType());
    Assert.assertFalse(fn.isReturnTypeInferred());
  }

  @Test
  public void testStructAndDict() {
    FunctionType superCtor = registry.createConstructorType("SuperClass", null, null, null);
    FunctionType subCtor = registry.createConstructorType("SubClass", null, null, null);
    subCtor.setPrototypeBasedOn(superCtor.getInstanceType());

    Assert.assertFalse(superCtor.makesStructs());
    Assert.assertFalse(superCtor.makesDicts());

    superCtor.setStruct();
    Assert.assertTrue(superCtor.makesStructs());
    Assert.assertFalse(superCtor.makesDicts());
    Assert.assertTrue(subCtor.makesStructs());

    FunctionType dictCtor = registry.createConstructorType("DictClass", null, null, null);
    dictCtor.setDict();
    Assert.assertTrue(dictCtor.makesDicts());
    Assert.assertFalse(dictCtor.makesStructs());

    FunctionType ordFn = registry.createFunctionType(numberType);
    Assert.assertFalse(ordFn.makesStructs());
    Assert.assertFalse(ordFn.makesDicts());
  }

  @Test
  public void testMinMaxArguments() {
    Node paramList = new Node(Token.PARAM_LIST);
    Node reqParam = Node.newString(Token.NAME, "a");
    reqParam.setJSType(numberType);
    Node optParam = Node.newString(Token.NAME, "b");
    optParam.setJSType(stringType);
    optParam.setOptionalArg(true);
    paramList.addChildToBack(reqParam);
    paramList.addChildToBack(optParam);

    ArrowType arrow = new ArrowType(registry, paramList, numberType);
    FunctionType fn = new FunctionType(registry, "fn", null, arrow, null, null, false, false);

    Assert.assertEquals(1, fn.getMinArguments());
    Assert.assertEquals(2, fn.getMaxArguments());

    Node varParam = Node.newString(Token.NAME, "c");
    varParam.setJSType(booleanType);
    varParam.setVarArgs(true);
    paramList.addChildToBack(varParam);

    Assert.assertEquals(1, fn.getMinArguments());
    Assert.assertEquals(Integer.MAX_VALUE, fn.getMaxArguments());

    FunctionType noParamFn = registry.createFunctionType(numberType);
    Assert.assertEquals(0, noParamFn.getMinArguments());
    Assert.assertEquals(0, noParamFn.getMaxArguments());
  }

  @Test
  public void testParametersIterable() {
    Node paramList = new Node(Token.PARAM_LIST);
    Node p1 = Node.newString(Token.NAME, "a");
    paramList.addChildToBack(p1);
    ArrowType arrow = new ArrowType(registry, paramList, numberType);
    FunctionType fn = new FunctionType(registry, "f", null, arrow, null, null, false, false);
    int count = 0;
    for (Node p : fn.getParameters()) {
      Assert.assertSame(p1, p);
      count++;
    }
    Assert.assertEquals(1, count);

    ArrowType nullParamsArrow = new ArrowType(registry, null, numberType);
    FunctionType nullParamsFn = new FunctionType(registry, "f2", null, nullParamsArrow, null, null, false, false);
    Assert.assertFalse(nullParamsFn.getParameters().iterator().hasNext());
  }

  @Test
  public void testPrototypeOperations() {
    FunctionType ctor = registry.createConstructorType("ProtoClass", null, null, null);
    ObjectType proto = ctor.getPrototype();
    Assert.assertNotNull(proto);
    Assert.assertEquals("ProtoClass.prototype", proto.getReferenceName());
    Assert.assertSame(ctor, proto.getOwnerFunction());

    Property slot = ctor.getSlot("prototype");
    Assert.assertNotNull(slot);
    Assert.assertSame(proto, slot.getType());

    Set<String> ownProps = ctor.getOwnPropertyNames();
    Assert.assertTrue(ownProps.contains("prototype"));

    FunctionType anon = registry.createFunctionType(numberType);
    ObjectType anonProto = anon.getPrototype();
    Assert.assertTrue(anonProto.isUnknownType());

    ObjectType dummyObj = new PrototypeObjectType(registry, "CustomProto", objectType);
    boolean setRes = ctor.setPrototype(dummyObj, null);
    Assert.assertTrue(setRes);
    Assert.assertSame(dummyObj, ctor.getPrototype());
    Assert.assertNull(proto.getOwnerFunction());

    Assert.assertFalse(ctor.setPrototype(null, null));
    Assert.assertFalse(ctor.setPrototype(ctor.getInstanceType(), null));

    boolean defRes = ctor.defineProperty("prototype", dummyObj, false, null);
    Assert.assertTrue(defRes);
    boolean defResNonObj = ctor.defineProperty("prototype", numberType, false, null);
    Assert.assertFalse(defResNonObj);
  }

  @Test
  public void testInterfacesHierarchy() {
    FunctionType iA = FunctionType.forInterface(registry, "IA", null);
    FunctionType iB = FunctionType.forInterface(registry, "IB", null);
    FunctionType iC = FunctionType.forInterface(registry, "IC", null);

    iB.setExtendedInterfaces(ImmutableList.of(iA.getInstanceType()));
    Assert.assertEquals(1, iB.getExtendedInterfacesCount());

    FunctionType ctor = registry.createConstructorType("Impl", null, null, null);
    ctor.setImplementedInterfaces(ImmutableList.of(iB.getInstanceType(), iC.getInstanceType()));
    Assert.assertTrue(ctor.hasImplementedInterfaces());

    List<ObjectType> ownIfaces = ImmutableList.copyOf(ctor.getOwnImplementedInterfaces());
    Assert.assertEquals(2, ownIfaces.size());

    Set<ObjectType> allExtended = (Set<ObjectType>) iB.getAllExtendedInterfaces();
    Assert.assertTrue(allExtended.contains(iA.getInstanceType()));

    Set<ObjectType> allImplemented = (Set<ObjectType>) ctor.getAllImplementedInterfaces();
    Assert.assertTrue(allImplemented.contains(iB.getInstanceType()));
    Assert.assertTrue(allImplemented.contains(iA.getInstanceType()));
    Assert.assertTrue(allImplemented.contains(iC.getInstanceType()));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testSetImplementedInterfacesOnNonConstructor() {
    FunctionType fn = registry.createFunctionType(numberType);
    fn.setImplementedInterfaces(ImmutableList.of(objectType));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testSetExtendedInterfacesOnNonInterface() {
    FunctionType ctor = registry.createConstructorType("Foo", null, null, null);
    ctor.setExtendedInterfaces(ImmutableList.of(objectType));
  }

  @Test
  public void testBuiltinPropertiesCallBindApply() {
    FunctionType fn = registry.createFunctionType(stringType, numberType);
    JSType callProp = fn.getPropertyType("call");
    Assert.assertTrue(callProp.isFunctionType());

    JSType bindProp = fn.getPropertyType("bind");
    Assert.assertTrue(bindProp.isFunctionType());

    JSType applyProp = fn.getPropertyType("apply");
    Assert.assertTrue(applyProp.isFunctionType());

    FunctionType bindRet = fn.getBindReturnType(1);
    Assert.assertNotNull(bindRet);
    Assert.assertEquals(stringType, bindRet.getReturnType());

    FunctionType bindRetAll = fn.getBindReturnType(-1);
    Assert.assertNotNull(bindRetAll);
  }

  @Test
  public void testSupAndInfHelper() {
    FunctionType fn1 = registry.createFunctionType(numberType, stringType);
    FunctionType fn2 = registry.createFunctionType(numberType, stringType);
    FunctionType supSame = fn1.supAndInfHelper(fn2, true);
    Assert.assertSame(fn1, supSame);

    FunctionType fnSub = registry.createFunctionType(numberType, objectType);
    FunctionType fnSuper = registry.createFunctionType(numberType, unknownType);
    FunctionType supResult = fnSub.supAndInfHelper(fnSuper, true);
    Assert.assertNotNull(supResult);
    FunctionType infResult = fnSub.supAndInfHelper(fnSuper, false);
    Assert.assertNotNull(infResult);

    FunctionType topInstance = registry.getNativeFunctionType(JSTypeNative.FUNCTION_INSTANCE_TYPE);
    FunctionType supWithTop = fn1.supAndInfHelper(topInstance, true);
    Assert.assertSame(topInstance, supWithTop);
    FunctionType infWithTop = fn1.supAndInfHelper(topInstance, false);
    Assert.assertSame(fn1, infWithTop);
  }

  @Test
  public void testSuperClassConstructorAndTopMostDefiningType() {
    FunctionType superCtor = registry.createConstructorType("SuperClass", null, null, null);
    superCtor.getPrototype().defineProperty("prop", stringType, false, null);

    FunctionType subCtor = registry.createConstructorType("SubClass", null, null, null);
    subCtor.setPrototypeBasedOn(superCtor.getInstanceType());

    Assert.assertSame(superCtor, subCtor.getSuperClassConstructor());
    ObjectType definingType = subCtor.getTopMostDefiningType("prop");
    Assert.assertSame(superCtor.getInstanceType(), definingType);
  }

  @Test
  public void testTopDefiningInterface() {
    FunctionType iA = FunctionType.forInterface(registry, "IA", null);
    iA.getPrototype().defineProperty("prop", stringType, false, null);

    FunctionType iB = FunctionType.forInterface(registry, "IB", null);
    iB.setExtendedInterfaces(ImmutableList.of(iA.getInstanceType()));

    ObjectType topIface = FunctionType.getTopDefiningInterface(iB.getInstanceType(), "prop");
    Assert.assertSame(iA.getInstanceType(), topIface);
  }

  @Test
  public void testEquivalenceAndHashCode() {
    FunctionType fn1 = registry.createFunctionType(numberType, stringType);
    FunctionType fn2 = registry.createFunctionType(numberType, stringType);
    Assert.assertTrue(fn1.checkFunctionEquivalenceHelper(fn2, true));
    Assert.assertTrue(fn1.hasEqualCallType(fn2));
    Assert.assertEquals(fn1.hashCode(), fn2.hashCode());

    FunctionType ctor1 = registry.createConstructorType("C1", null, null, null);
    FunctionType ctor2 = registry.createConstructorType("C2", null, null, null);
    Assert.assertFalse(ctor1.checkFunctionEquivalenceHelper(ctor2, false));
    Assert.assertFalse(ctor1.checkFunctionEquivalenceHelper(fn1, false));

    FunctionType iface1 = FunctionType.forInterface(registry, "I1", null);
    FunctionType iface2 = FunctionType.forInterface(registry, "I1", null);
    FunctionType iface3 = FunctionType.forInterface(registry, "I3", null);
    Assert.assertTrue(iface1.checkFunctionEquivalenceHelper(iface2, false));
    Assert.assertFalse(iface1.checkFunctionEquivalenceHelper(iface3, false));
    Assert.assertFalse(iface1.checkFunctionEquivalenceHelper(ctor1, false));
    Assert.assertEquals("I1".hashCode(), iface1.hashCode());
  }

  @Test
  public void testToStringAndDebugString() {
    FunctionType ctor = registry.createConstructorType("MyCtor", null, null, null);
    String str = ctor.toString();
    Assert.assertTrue(str.startsWith("function (new:MyCtor"));

    Node paramList = new Node(Token.PARAM_LIST);
    Node optParam = Node.newString(Token.NAME, "opt");
    optParam.setJSType(registry.createUnionType(stringType, voidType));
    optParam.setOptionalArg(true);
    Node varParam = Node.newString(Token.NAME, "var");
    varParam.setJSType(registry.createUnionType(numberType, voidType));
    varParam.setVarArgs(true);
    paramList.addChildToBack(optParam);
    paramList.addChildToBack(varParam);

    ArrowType arrow = new ArrowType(registry, paramList, numberType);
    FunctionType fn = new FunctionType(registry, "customFn", null, arrow, objectType, null, false, false);
    String fnStr = fn.toString();
    Assert.assertTrue(fnStr.contains("this:Object"));
    Assert.assertTrue(fnStr.contains("string="));
    Assert.assertTrue(fnStr.contains("...[number]"));

    String debugStr = fn.toDebugHashCodeString();
    Assert.assertNotNull(debugStr);
  }

  @Test
  public void testSubtyping() {
    FunctionType fn1 = registry.createFunctionType(numberType, stringType);
    FunctionType fn2 = registry.createFunctionType(numberType, stringType);
    Assert.assertTrue(fn1.isSubtype(fn2));

    FunctionType iface = FunctionType.forInterface(registry, "I", null);
    Assert.assertTrue(fn1.isSubtype(iface));
    Assert.assertFalse(iface.isSubtype(fn1));
    Assert.assertTrue(fn1.isSubtype(objectType));
  }

  @Test
  public void testVisitorAndClone() {
    FunctionType ctor = registry.createConstructorType("CloneCtor", null, null, null);
    Visitor<String> visitor = new Visitor<String>() {
      public String caseNoType() { return "no"; }
      public String caseEnumElementType(EnumElementType type) { return "enum"; }
      public String caseAllType() { return "all"; }
      public String caseBooleanType() { return "bool"; }
      public String caseNoObjectType() { return "noObj"; }
      public String caseFunctionType(FunctionType type) { return "function"; }
      public String caseObjectType(ObjectType type) { return "obj"; }
      public String caseUnknownType() { return "unknown"; }
      public String caseNullType() { return "null"; }
      public String caseNamedType(NamedType type) { return "named"; }
      public String caseRecordType(RecordType type) { return "record"; }
      public String caseTemplateType(TemplateType type) { return "template"; }
      public String caseStringType() { return "string"; }
      public String caseVoidType() { return "void"; }
      public String caseUnionType(UnionType type) { return "union"; }
      public String caseNumberType() { return "number"; }
    };
    Assert.assertEquals("function", ctor.visit(visitor));

    FunctionType cloned = ctor.cloneWithoutArrowType();
    Assert.assertNotNull(cloned);
    Assert.assertEquals(ctor.getReferenceName(), cloned.getReferenceName());
  }

  @Test
  public void testResolveInternalAndCaching() {
    FunctionType ctor = registry.createConstructorType("Resolvable", null, null, null);
    FunctionType sub = registry.createConstructorType("SubResolvable", null, null, null);
    sub.setPrototypeBasedOn(ctor.getInstanceType());

    StaticScope<JSType> scope = new SimpleSlot("test", null, false);
    ErrorReporter reporter = null;
    JSType resolved = ctor.resolveInternal(reporter, scope);
    Assert.assertSame(ctor, resolved);

    ctor.clearCachedValues();
    Assert.assertTrue(ctor.hasCachedValues());
  }

  @Test
  public void testSourceAndTemplates() {
    Node fnNode1 = new Node(Token.FUNCTION);
    Node fnNode2 = new Node(Token.FUNCTION);
    FunctionType ctor = new FunctionType(registry, "Templated", fnNode1,
        new ArrowType(registry, new Node(Token.PARAM_LIST), numberType),
        null, ImmutableList.of("T"), true, false);

    Assert.assertSame(fnNode1, ctor.getSource());
    Assert.assertEquals(1, ctor.getTemplateTypeNames().size());
    Assert.assertEquals("T", ctor.getTemplateTypeNames().get(0));
    Assert.assertTrue(ctor.hasAnyTemplateInternal());

    ctor.getPrototype();
    ctor.setSource(fnNode2);
    Assert.assertSame(fnNode2, ctor.getSource());
    ctor.setSource(null);
    Assert.assertNull(ctor.getSource());
  }

  @Test
  public void testIsInstanceType() {
    FunctionType u2u = registry.getNativeFunctionType(JSTypeNative.U2U_CONSTRUCTOR_TYPE);
    Assert.assertTrue(u2u.isInstanceType());

    FunctionType fn = registry.createFunctionType(numberType);
    Assert.assertFalse(fn.isInstanceType());
  }
}
