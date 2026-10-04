package com.google.javascript.rhino.jstype;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class FunctionTypeTest {

  private JSTypeRegistry registry;
  private FunctionType ordinaryFn;
  private FunctionType constructorFn;
  private FunctionType interfaceFn;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(null);
    ordinaryFn = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE),
        registry.getNativeType(JSTypeNative.STRING_TYPE)
    );
    constructorFn = registry.createConstructorType(
        "Foo", null, null, null
    );
    interfaceFn = registry.createInterfaceType("IFoo", null);
  }

  @Test
  public void testKindAndCapabilities() {
    Assert.assertTrue(ordinaryFn.isOrdinaryFunction());
    Assert.assertFalse(ordinaryFn.isConstructor());
    Assert.assertFalse(ordinaryFn.isInterface());
    Assert.assertTrue(ordinaryFn.isFunctionType());
    Assert.assertTrue(ordinaryFn.canBeCalled());
    Assert.assertFalse(ordinaryFn.hasInstanceType());

    Assert.assertFalse(constructorFn.isOrdinaryFunction());
    Assert.assertTrue(constructorFn.isConstructor());
    Assert.assertFalse(constructorFn.isInterface());
    Assert.assertTrue(constructorFn.hasInstanceType());

    Assert.assertFalse(interfaceFn.isOrdinaryFunction());
    Assert.assertFalse(interfaceFn.isConstructor());
    Assert.assertTrue(interfaceFn.isInterface());
    Assert.assertTrue(interfaceFn.hasInstanceType());
  }

  @Test
  public void testIsInstanceType() {
    Assert.assertFalse(ordinaryFn.isInstanceType());
    JSType u2u = registry.getNativeType(JSTypeNative.U2U_CONSTRUCTOR_TYPE);
    Assert.assertTrue(u2u.isInstanceType());
  }

  @Test
  public void testParametersAndArgumentsCount() {
    Assert.assertEquals(1, ordinaryFn.getMinArguments());
    Assert.assertEquals(1, ordinaryFn.getMaxArguments());

    FunctionParamBuilder paramBuilder = new FunctionParamBuilder(registry);
    paramBuilder.addRequiredParams(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    paramBuilder.addOptionalParams(registry.getNativeType(JSTypeNative.STRING_TYPE));
    paramBuilder.addVarArgs(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE));

    FunctionType complexFn = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.VOID_TYPE),
        paramBuilder.build()
    );

    Assert.assertEquals(1, complexFn.getMinArguments());
    Assert.assertEquals(Integer.MAX_VALUE, complexFn.getMaxArguments());

    Iterable<Node> params = complexFn.getParameters();
    Assert.assertEquals(3, Iterables.size(params));

    FunctionType noParamFn = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.VOID_TYPE)
    );
    Assert.assertEquals(0, noParamFn.getMinArguments());
    Assert.assertEquals(0, noParamFn.getMaxArguments());
  }

  @Test
  public void testReturnTypes() {
    Assert.assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), ordinaryFn.getReturnType());
    Assert.assertFalse(ordinaryFn.isReturnTypeInferred());

    FunctionType cloned = ordinaryFn.cloneWithNewReturnType(
        registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), true);
    Assert.assertEquals(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), cloned.getReturnType());
    Assert.assertTrue(cloned.isReturnTypeInferred());
  }

  @Test
  public void testPrototypesAndInheritance() {
    FunctionPrototypeType proto = constructorFn.getPrototype();
    Assert.assertNotNull(proto);
    Assert.assertTrue(constructorFn.hasCachedValues());

    Assert.assertFalse(constructorFn.setPrototype(null));
    Assert.assertFalse(constructorFn.setPrototype((FunctionPrototypeType) constructorFn.getInstanceType()));

    FunctionType subCtor = registry.createConstructorType("Bar", null, null, null);
    subCtor.setPrototypeBasedOn(constructorFn.getInstanceType());

    Assert.assertEquals(constructorFn, subCtor.getSuperClassConstructor());
    Assert.assertNotNull(constructorFn.getSubTypes());
    Assert.assertTrue(constructorFn.getSubTypes().contains(subCtor));
    Assert.assertFalse(subCtor.hasUnknownSupertype());

    FunctionPrototypeType customProto = new FunctionPrototypeType(
        registry, subCtor, constructorFn.getInstanceType());
    Assert.assertTrue(subCtor.setPrototype(customProto));
  }

  @Test
  public void testTopMostDefiningType() {
    constructorFn.getPrototype().defineDeclaredProperty(
        "propA", registry.getNativeType(JSTypeNative.NUMBER_TYPE), false);

    FunctionType subCtor = registry.createConstructorType("Bar", null, null, null);
    subCtor.setPrototypeBasedOn(constructorFn.getInstanceType());
    subCtor.getPrototype().defineDeclaredProperty(
        "propA", registry.getNativeType(JSTypeNative.NUMBER_TYPE), false);

    JSType definingType = subCtor.getTopMostDefiningType("propA");
    Assert.assertEquals(constructorFn.getInstanceType(), definingType);
  }

  @Test
  public void testImplementedInterfaces() {
    Assert.assertFalse(constructorFn.getAllImplementedInterfaces().iterator().hasNext());
    Assert.assertFalse(constructorFn.getImplementedInterfaces().iterator().hasNext());

    FunctionType superInterface = registry.createInterfaceType("ISuper", null);
    interfaceFn.setPrototypeBasedOn(superInterface.getInstanceType());

    constructorFn.setImplementedInterfaces(
        ImmutableList.of(interfaceFn.getInstanceType()));

    List<ObjectType> direct = ImmutableList.copyOf(constructorFn.getImplementedInterfaces());
    Assert.assertEquals(1, direct.size());
    Assert.assertEquals(interfaceFn.getInstanceType(), direct.get(0));

    List<ObjectType> all = ImmutableList.copyOf(constructorFn.getAllImplementedInterfaces());
    Assert.assertTrue(all.contains(interfaceFn.getInstanceType()));
    Assert.assertTrue(all.contains(superInterface.getInstanceType()));

    FunctionType subCtor = registry.createConstructorType("Bar", null, null, null);
    subCtor.setPrototypeBasedOn(constructorFn.getInstanceType());
    List<ObjectType> subAll = ImmutableList.copyOf(subCtor.getImplementedInterfaces());
    Assert.assertEquals(1, subAll.size());
  }

  @Test
  public void testPropertiesAndPredefinedMethods() {
    Assert.assertTrue(ordinaryFn.hasProperty("prototype"));
    Assert.assertTrue(ordinaryFn.hasOwnProperty("prototype"));
    Assert.assertTrue(ordinaryFn.isPropertyTypeInferred("prototype"));
    Assert.assertNotNull(ordinaryFn.getPropertyType("prototype"));

    JSType callProp = ordinaryFn.getPropertyType("call");
    Assert.assertNotNull(callProp);
    Assert.assertTrue(callProp.isFunctionType());

    JSType applyProp = ordinaryFn.getPropertyType("apply");
    Assert.assertNotNull(applyProp);
    Assert.assertTrue(applyProp.isFunctionType());

    FunctionType noParamFn = new FunctionType(
        registry, null, null,
        new ArrowType(registry, null, registry.getNativeType(JSTypeNative.NUMBER_TYPE)),
        null, null, false, false);
    JSType callNoParam = noParamFn.getPropertyType("call");
    Assert.assertNotNull(callNoParam);

    boolean definedProto = ordinaryFn.defineProperty(
        "prototype", registry.getNativeType(JSTypeNative.OBJECT_TYPE), false, false);
    Assert.assertTrue(definedProto);

    boolean definedProtoInvalid = ordinaryFn.defineProperty(
        "prototype", registry.getNativeType(JSTypeNative.NUMBER_TYPE), false, false);
    Assert.assertFalse(definedProtoInvalid);

    boolean reDefinedSame = ordinaryFn.defineProperty(
        "prototype", ordinaryFn.getPrototype(), false, false);
    Assert.assertTrue(reDefinedSame);
  }

  @Test
  public void testSubtypingAndEquivalence() {
    Assert.assertTrue(ordinaryFn.isSubtype(ordinaryFn));
    Assert.assertTrue(ordinaryFn.isEquivalentTo(ordinaryFn));
    Assert.assertFalse(ordinaryFn.isEquivalentTo(constructorFn));
    Assert.assertFalse(ordinaryFn.isEquivalentTo(interfaceFn));

    FunctionType iface2 = registry.createInterfaceType("IFoo", null);
    Assert.assertTrue(interfaceFn.isEquivalentTo(iface2));
    Assert.assertEquals(interfaceFn.hashCode(), iface2.hashCode());
    Assert.assertFalse(interfaceFn.isEquivalentTo(registry.createInterfaceType("IOther", null)));

    Assert.assertTrue(ordinaryFn.isSubtype(interfaceFn));
    Assert.assertFalse(interfaceFn.isSubtype(ordinaryFn));

    FunctionType ordinaryFn2 = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE),
        registry.getNativeType(JSTypeNative.STRING_TYPE)
    );
    Assert.assertTrue(ordinaryFn.isEquivalentTo(ordinaryFn2));
    Assert.assertEquals(ordinaryFn.hashCode(), ordinaryFn2.hashCode());
    Assert.assertTrue(ordinaryFn.hasEqualCallType(ordinaryFn2));

    UnionType union = (UnionType) registry.createUnionType(
        ordinaryFn, registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    Assert.assertTrue(ordinaryFn.isSubtype(union));
  }

  @Test
  public void testLeastSupertypeAndGreatestSubtype() {
    Assert.assertEquals(ordinaryFn, ordinaryFn.getLeastSupertype(ordinaryFn));
    Assert.assertEquals(ordinaryFn, ordinaryFn.getGreatestSubtype(ordinaryFn));

    FunctionType fnWithOtherReturn = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.STRING_TYPE),
        registry.getNativeType(JSTypeNative.STRING_TYPE)
    );

    JSType superType = ordinaryFn.getLeastSupertype(fnWithOtherReturn);
    Assert.assertTrue(superType.isFunctionType());
    Assert.assertTrue(((FunctionType) superType).getReturnType().isUnionType());

    JSType subType = ordinaryFn.getGreatestSubtype(fnWithOtherReturn);
    Assert.assertTrue(subType.isFunctionType());
    Assert.assertEquals(registry.getNativeType(JSTypeNative.NO_TYPE),
        ((FunctionType) subType).getReturnType());

    JSType fnInstance = registry.getNativeType(JSTypeNative.FUNCTION_INSTANCE_TYPE);
    Assert.assertEquals(fnInstance, ordinaryFn.getLeastSupertype(fnInstance));
    Assert.assertEquals(ordinaryFn, ordinaryFn.getGreatestSubtype(fnInstance));

    JSType numType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    Assert.assertNotNull(ordinaryFn.getLeastSupertype(numType));
    Assert.assertNotNull(ordinaryFn.getGreatestSubtype(numType));

    JSType ctorSuper = constructorFn.getLeastSupertype(interfaceFn);
    Assert.assertEquals(registry.getNativeType(JSTypeNative.U2U_CONSTRUCTOR_TYPE), ctorSuper);

    JSType ctorSub = constructorFn.getGreatestSubtype(interfaceFn);
    Assert.assertEquals(registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE), ctorSub);
  }

  @Test
  public void testToStringAndDebugHashCode() {
    Assert.assertEquals("Function",
        registry.getNativeType(JSTypeNative.FUNCTION_INSTANCE_TYPE).toString());

    String str = ordinaryFn.toString();
    Assert.assertTrue(str.startsWith("function ("));
    Assert.assertTrue(str.contains("string"));
    Assert.assertTrue(str.endsWith(": number"));

    FunctionParamBuilder paramBuilder = new FunctionParamBuilder(registry);
    paramBuilder.addVarArgs(registry.createNullableType(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE)));
    FunctionType varArgsFn = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.VOID_TYPE),
        paramBuilder.build()
    );
    String varArgsStr = varArgsFn.toString();
    Assert.assertTrue(varArgsStr.contains("...["));

    String debugStr = ordinaryFn.toDebugHashCodeString();
    Assert.assertTrue(debugStr.startsWith("function ("));

    String nativeDebug = registry.getNativeType(
        JSTypeNative.FUNCTION_INSTANCE_TYPE).toDebugHashCodeString();
    Assert.assertNotNull(nativeDebug);
  }

  @Test
  public void testVisitorAndResolving() {
    Visitor<String> visitor = new Visitor<String>() {
      @Override public String caseNoType() { return null; }
      @Override public String caseEnumElementType(EnumElementType type) { return null; }
      @Override public String caseAllType() { return null; }
      @Override public String caseBooleanType() { return null; }
      @Override public String caseNoObjectType() { return null; }
      @Override public String caseUnknownType() { return null; }
      @Override public String caseNullType() { return null; }
      @Override public String caseNamedType(NamedType type) { return null; }
      @Override public String caseNumberType() { return null; }
      @Override public String caseStringType() { return null; }
      @Override public String caseObjectType(ObjectType type) { return null; }
      @Override public String caseUnionType(UnionType type) { return null; }
      @Override public String caseRecordType(RecordType type) { return null; }
      @Override public String caseTemplateType(TemplateType templateType) { return null; }
      @Override public String caseVoidType() { return null; }
      @Override public String caseFunctionType(FunctionType type) { return "visited"; }
    };
    Assert.assertEquals("visited", ordinaryFn.visit(visitor));

    ErrorReporter reporter = null;
    StaticScope<JSType> scope = null;
    constructorFn.setImplementedInterfaces(
        ImmutableList.of(interfaceFn.getInstanceType()));
    constructorFn.resolve(reporter, scope);
    Assert.assertTrue(constructorFn.isResolved());
  }

  @Test
  public void testGetSourceAndSetSource() {
    Assert.assertNull(ordinaryFn.getSource());
    Node fnNode = new Node(Token.FUNCTION);
    ordinaryFn.setSource(fnNode);
    Assert.assertEquals(fnNode, ordinaryFn.getSource());
  }

  @Test
  public void testTypeOfThisHandling() {
    FunctionType ctorWithNoObj = new FunctionType(
        registry, "NoObjCtor", null,
        new ArrowType(registry, new Node(Token.LP), null),
        registry.getNativeObjectType(JSTypeNative.NO_OBJECT_TYPE),
        "T", true, false);

    Assert.assertEquals(registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE),
        ctorWithNoObj.getTypeOfThis());
    Assert.assertEquals("T", ctorWithNoObj.getTemplateTypeName());

    ObjectType customThis = registry.createAnonymousObjectType();
    ctorWithNoObj.setInstanceType(customThis);
    Assert.assertEquals(customThis, ctorWithNoObj.getInstanceType());
  }

  @Test(expected = IllegalStateException.class)
  public void testGetInstanceTypeThrowsOnOrdinary() {
    ordinaryFn.getInstanceType();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetSuperClassConstructorThrowsOnOrdinary() {
    ordinaryFn.getSuperClassConstructor();
  }
}
