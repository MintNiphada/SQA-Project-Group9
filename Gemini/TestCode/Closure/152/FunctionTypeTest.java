package com.google.javascript.rhino.jstype;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.testing.TestErrorReporter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class FunctionTypeTest {
  private JSTypeRegistry registry;
  private Node fnNode;
  private Node lpNode;
  private ArrowType arrowType;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(new TestErrorReporter(null, null));
    fnNode = new Node(Token.FUNCTION);
    lpNode = new Node(Token.LP);
    arrowType = new ArrowType(registry, lpNode, registry.getNativeType(JSTypeNative.NUMBER_TYPE));
  }

  @Test
  public void testConstructorAndKindPredicates() {
    FunctionType ctor = new FunctionType(
        registry, "Ctor", fnNode, arrowType, null, null, true, false);
    Assert.assertTrue(ctor.isConstructor());
    Assert.assertFalse(ctor.isInterface());
    Assert.assertFalse(ctor.isOrdinaryFunction());
    Assert.assertTrue(ctor.isFunctionType());
    Assert.assertTrue(ctor.canBeCalled());
    Assert.assertTrue(ctor.hasInstanceType());
    Assert.assertNotNull(ctor.getInstanceType());

    FunctionType ordinary = new FunctionType(
        registry, "Ord", null, arrowType, null, "T", false, false);
    Assert.assertFalse(ordinary.isConstructor());
    Assert.assertFalse(ordinary.isInterface());
    Assert.assertTrue(ordinary.isOrdinaryFunction());
    Assert.assertFalse(ordinary.hasInstanceType());
    Assert.assertEquals("T", ordinary.getTemplateTypeName());

    FunctionType iface = FunctionType.forInterface(registry, "Iface", fnNode);
    Assert.assertFalse(iface.isConstructor());
    Assert.assertTrue(iface.isInterface());
    Assert.assertFalse(iface.isOrdinaryFunction());
    Assert.assertTrue(iface.hasInstanceType());
  }

  @Test
  public void testIsInstanceType() {
    FunctionType u2u = registry.getNativeFunctionType(JSTypeNative.U2U_CONSTRUCTOR_TYPE);
    Assert.assertTrue(u2u.isInstanceType());

    FunctionType ordinary = new FunctionType(
        registry, "Fn", null, arrowType, null, null, false, false);
    Assert.assertFalse(ordinary.isInstanceType());
  }

  @Test
  public void testParametersAndArgumentCounts() {
    Node p1 = Node.newString(Token.NAME, "a");
    p1.setJSType(registry.getNativeType(JSTypeNative.STRING_TYPE));
    Node p2 = Node.newString(Token.NAME, "b");
    p2.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    p2.setOptionalArg(true);
    Node p3 = Node.newString(Token.NAME, "c");
    p3.setJSType(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE));
    p3.setVarArgs(true);

    lpNode.addChildToBack(p1);
    lpNode.addChildToBack(p2);
    lpNode.addChildToBack(p3);

    FunctionType fn = new FunctionType(
        registry, "Fn", null, arrowType, null, null, false, false);

    Assert.assertEquals(1, fn.getMinArguments());
    Assert.assertEquals(Integer.MAX_VALUE, fn.getMaxArguments());
    Assert.assertEquals(3, Lists.newArrayList(fn.getParameters()).size());

    Node simpleLp = new Node(Token.LP);
    simpleLp.addChildToBack(Node.newString(Token.NAME, "x"));
    ArrowType simpleArrow = new ArrowType(registry, simpleLp, registry.getNativeType(JSTypeNative.STRING_TYPE));
    FunctionType simpleFn = new FunctionType(
        registry, "Fn2", null, simpleArrow, null, null, false, false);
    Assert.assertEquals(1, simpleFn.getMaxArguments());
    Assert.assertEquals(1, simpleFn.getMinArguments());

    ArrowType emptyArrow = new ArrowType(registry, null, registry.getNativeType(JSTypeNative.VOID_TYPE));
    FunctionType emptyFn = new FunctionType(
        registry, "Empty", null, emptyArrow, null, null, false, false);
    Assert.assertFalse(emptyFn.getParameters().iterator().hasNext());
    Assert.assertEquals(Integer.MAX_VALUE, emptyFn.getMaxArguments());
    Assert.assertEquals(0, emptyFn.getMinArguments());
  }

  @Test
  public void testReturnTypeAndArrowType() {
    ArrowType inferredArrow = new ArrowType(
        registry, lpNode, registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), true);
    FunctionType fn = new FunctionType(
        registry, "Fn", null, inferredArrow, null, null, false, false);

    Assert.assertEquals(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), fn.getReturnType());
    Assert.assertTrue(fn.isReturnTypeInferred());
    Assert.assertSame(inferredArrow, fn.getInternalArrowType());
  }

  @Test
  public void testPrototypeOperations() {
    FunctionType ctor = new FunctionType(
        registry, "Ctor", null, arrowType, null, null, true, false);
    FunctionPrototypeType proto = ctor.getPrototype();
    Assert.assertNotNull(proto);
    Assert.assertTrue(ctor.hasCachedValues());

    Assert.assertFalse(ctor.setPrototype(null));
    Assert.assertFalse(ctor.setPrototype((FunctionPrototypeType) ctor.getInstanceType()));

    FunctionType subCtor = new FunctionType(
        registry, "SubCtor", null, arrowType, null, null, true, false);
    subCtor.setPrototypeBasedOn(ctor.getInstanceType());
    Assert.assertEquals(ctor, subCtor.getSuperClassConstructor());
    Assert.assertEquals(1, ctor.getSubTypes().size());
    Assert.assertSame(subCtor, ctor.getSubTypes().get(0));

    subCtor.setPrototypeBasedOn(registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE));
    Assert.assertEquals(registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE),
        subCtor.getPrototype().getImplicitPrototype());
  }

  @Test
  public void testInterfacesAndHierarchy() {
    FunctionType iface1 = FunctionType.forInterface(registry, "Iface1", fnNode);
    FunctionType iface2 = FunctionType.forInterface(registry, "Iface2", fnNode);
    iface2.getPrototype().setImplicitPrototype(iface1.getInstanceType());

    FunctionType ctor = new FunctionType(
        registry, "Ctor", null, arrowType, null, null, true, false);
    ctor.setImplementedInterfaces(ImmutableList.of(iface2.getInstanceType()));

    List<ObjectType> direct = Lists.newArrayList(ctor.getImplementedInterfaces());
    Assert.assertEquals(1, direct.size());

    List<ObjectType> all = Lists.newArrayList(ctor.getAllImplementedInterfaces());
    Assert.assertEquals(2, all.size());
    Assert.assertTrue(all.contains(iface1.getInstanceType()));
    Assert.assertTrue(all.contains(iface2.getInstanceType()));

    FunctionType subCtor = new FunctionType(
        registry, "SubCtor", null, arrowType, null, null, true, false);
    subCtor.setPrototypeBasedOn(ctor.getInstanceType());
    List<ObjectType> subAll = Lists.newArrayList(subCtor.getImplementedInterfaces());
    Assert.assertEquals(1, subAll.size());
  }

  @Test
  public void testPropertiesAndCallApply() {
    FunctionType fn = new FunctionType(
        registry, "Fn", null, arrowType, null, null, false, false);

    Assert.assertTrue(fn.hasProperty("prototype"));
    Assert.assertTrue(fn.hasOwnProperty("prototype"));
    Assert.assertTrue(fn.isPropertyTypeInferred("prototype"));
    Assert.assertNotNull(fn.getPropertyType("prototype"));

    JSType callProp = fn.getPropertyType("call");
    Assert.assertNotNull(callProp);
    Assert.assertTrue(callProp.isFunctionType());

    JSType applyProp = fn.getPropertyType("apply");
    Assert.assertNotNull(applyProp);
    Assert.assertTrue(applyProp.isFunctionType());

    ArrowType noParamsArrow = new ArrowType(
        registry, null, registry.getNativeType(JSTypeNative.STRING_TYPE));
    FunctionType noParamsFn = new FunctionType(
        registry, "NoParams", null, noParamsArrow, null, null, false, false);
    Assert.assertNotNull(noParamsFn.getPropertyType("call"));
  }

  @Test
  public void testDefineProperty() {
    FunctionType fn = new FunctionType(
        registry, "Fn", null, arrowType, null, null, false, false);

    Assert.assertFalse(fn.defineProperty("prototype", registry.getNativeType(JSTypeNative.NUMBER_TYPE), false, false));
    ObjectType objType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    Assert.assertTrue(fn.defineProperty("prototype", objType, false, false));
    Assert.assertTrue(fn.defineProperty("prototype", fn.getPrototype(), false, false));
    Assert.assertTrue(fn.defineProperty("custom", registry.getNativeType(JSTypeNative.NUMBER_TYPE), false, false));
    Assert.assertTrue(fn.hasProperty("custom"));
  }

  @Test
  public void testSuperClassConstructorAndUnknownSupertype() {
    FunctionType parent = new FunctionType(
        registry, "Parent", null, arrowType, null, null, true, false);
    FunctionType child = new FunctionType(
        registry, "Child", null, arrowType, null, null, true, false);
    child.setPrototypeBasedOn(parent.getInstanceType());

    Assert.assertEquals(parent, child.getSuperClassConstructor());
    Assert.assertFalse(child.hasUnknownSupertype());

    FunctionType unknownParent = new FunctionType(
        registry, "UnkParent", null, arrowType, null, null, true, false);
    unknownParent.getPrototype().setImplicitPrototype(
        registry.getNativeObjectType(JSTypeNative.UNKNOWN_TYPE));
    Assert.assertTrue(unknownParent.hasUnknownSupertype());
  }

  @Test
  public void testTopMostDefiningType() {
    FunctionType grandParent = new FunctionType(
        registry, "GP", null, arrowType, null, null, true, false);
    grandParent.getPrototype().defineProperty("prop", registry.getNativeType(JSTypeNative.STRING_TYPE), false, false);

    FunctionType parent = new FunctionType(
        registry, "P", null, arrowType, null, null, true, false);
    parent.setPrototypeBasedOn(grandParent.getInstanceType());
    parent.getPrototype().defineProperty("prop", registry.getNativeType(JSTypeNative.STRING_TYPE), false, false);

    FunctionType child = new FunctionType(
        registry, "C", null, arrowType, null, null, true, false);
    child.setPrototypeBasedOn(parent.getInstanceType());
    child.getPrototype().defineProperty("prop", registry.getNativeType(JSTypeNative.STRING_TYPE), false, false);

    Assert.assertEquals(grandParent.getInstanceType(), child.getTopMostDefiningType("prop"));
  }

  @Test
  public void testEquivalenceAndHashCode() {
    FunctionType fn1 = new FunctionType(
        registry, "Fn", null, arrowType, null, null, false, false);
    FunctionType fn2 = new FunctionType(
        registry, "Fn", null, arrowType, null, null, false, false);
    Assert.assertTrue(fn1.isEquivalentTo(fn2));
    Assert.assertTrue(fn1.hasEqualCallType(fn2));
    Assert.assertEquals(fn1.hashCode(), fn2.hashCode());

    FunctionType iface1 = FunctionType.forInterface(registry, "Iface", fnNode);
    FunctionType iface2 = FunctionType.forInterface(registry, "Iface", fnNode);
    FunctionType iface3 = FunctionType.forInterface(registry, "OtherIface", fnNode);
    Assert.assertTrue(iface1.isEquivalentTo(iface2));
    Assert.assertFalse(iface1.isEquivalentTo(iface3));
    Assert.assertFalse(iface1.isEquivalentTo(fn1));
    Assert.assertEquals(iface1.hashCode(), iface2.hashCode());

    FunctionType ctor1 = new FunctionType(
        registry, "Ctor", null, arrowType, null, null, true, false);
    FunctionType ctor2 = new FunctionType(
        registry, "Ctor", null, arrowType, null, null, true, false);
    Assert.assertFalse(ctor1.isEquivalentTo(ctor2));
    Assert.assertTrue(ctor1.isEquivalentTo(ctor1));
    Assert.assertFalse(ctor1.isEquivalentTo(fn1));
  }

  @Test
  public void testSubtyping() {
    FunctionType fn1 = new FunctionType(
        registry, "Fn1", null, arrowType, null, null, false, false);
    FunctionType fn2 = new FunctionType(
        registry, "Fn2", null, arrowType, null, null, false, false);
    Assert.assertTrue(fn1.isSubtype(fn2));

    FunctionType iface = FunctionType.forInterface(registry, "Iface", fnNode);
    Assert.assertTrue(fn1.isSubtype(iface));
    Assert.assertFalse(iface.isSubtype(fn1));

    Assert.assertTrue(fn1.isSubtype(registry.getNativeType(JSTypeNative.FUNCTION_PROTOTYPE)));
    Assert.assertFalse(fn1.isSubtype(registry.getNativeType(JSTypeNative.NUMBER_TYPE)));
  }

  @Test
  public void testSupAndInfHelper() {
    FunctionType fn1 = new FunctionType(
        registry, "Fn1", null, arrowType, null, null, false, false);
    ArrowType strArrow = new ArrowType(
        registry, lpNode, registry.getNativeType(JSTypeNative.STRING_TYPE));
    FunctionType fn2 = new FunctionType(
        registry, "Fn2", null, strArrow, null, null, false, false);

    JSType sup = fn1.getLeastSupertype(fn2);
    Assert.assertTrue(sup.isFunctionType());

    JSType inf = fn1.getGreatestSubtype(fn2);
    Assert.assertTrue(inf.isFunctionType());

    JSType fnInst = registry.getNativeType(JSTypeNative.FUNCTION_INSTANCE_TYPE);
    Assert.assertSame(fnInst, fn1.getLeastSupertype(fnInst));
    Assert.assertSame(fn1, fn1.getGreatestSubtype(fnInst));

    JSType numType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    Assert.assertTrue(fn1.getLeastSupertype(numType).isUnionType());
    Assert.assertTrue(fn1.getGreatestSubtype(numType).isNoType());
  }

  @Test
  public void testToStringAndDebugHashCodeString() {
    Node p1 = Node.newString(Token.NAME, "a");
    p1.setJSType(registry.getNativeType(JSTypeNative.STRING_TYPE));
    Node p2 = Node.newString(Token.NAME, "b");
    p2.setJSType(registry.createUnionType(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE),
        registry.getNativeType(JSTypeNative.VOID_TYPE)));
    p2.setVarArgs(true);

    lpNode.addChildToBack(p1);
    lpNode.addChildToBack(p2);

    FunctionType fn = new FunctionType(
        registry, "Fn", null, arrowType, registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE), null, false, false);

    String str = fn.toString();
    Assert.assertTrue(str.startsWith("function (this:Object, string, ...[number]): number"));
    Assert.assertNotNull(fn.toDebugHashCodeString());

    FunctionType fnInst = registry.getNativeFunctionType(JSTypeNative.FUNCTION_INSTANCE_TYPE);
    Assert.assertEquals("Function", fnInst.toString());
    Assert.assertNotNull(fnInst.toDebugHashCodeString());
  }

  @Test
  public void testSourceAndVisitorAndResolution() {
    FunctionType fn = new FunctionType(
        registry, "Fn", fnNode, arrowType, null, null, false, false);
    Assert.assertSame(fnNode, fn.getSource());

    Node newSource = new Node(Token.FUNCTION);
    fn.setSource(newSource);
    Assert.assertSame(newSource, fn.getSource());

    String visited = fn.visit(new Visitor<String>() {
      @Override public String caseFunctionType(FunctionType type) { return "function"; }
      @Override public String caseObjectType(ObjectType type) { return "object"; }
      @Override public String caseUnknownType() { return "unknown"; }
      @Override public String caseNoType() { return "no"; }
      @Override public String caseNoObjectType() { return "no_object"; }
      @Override public String caseAllType() { return "all"; }
      @Override public String caseValueType(ValueType type) { return "value"; }
      @Override public String caseBooleanType() { return "boolean"; }
      @Override public String caseNullType() { return "null"; }
      @Override public String caseNumberType() { return "number"; }
      @Override public String caseStringType() { return "string"; }
      @Override public String caseVoidType() { return "void"; }
      @Override public String caseUnionType(UnionType type) { return "union"; }
      @Override public String caseParameterizedType(ParameterizedType type) { return "param"; }
      @Override public String caseTemplateType(TemplateType type) { return "template"; }
    });
    Assert.assertEquals("function", visited);

    FunctionType resolved = (FunctionType) fn.resolve(new TestErrorReporter(null, null), null);
    Assert.assertNotNull(resolved);
  }

  @Test
  public void testTypeOfThisAndInstanceType() {
    FunctionType ctorWithNoObj = new FunctionType(
        registry, "CtorNoObj", null, arrowType, registry.getNativeObjectType(JSTypeNative.NO_OBJECT_TYPE), null, true, false);
    Assert.assertEquals(registry.getNativeObjectType(JSTypeNative.NO_OBJECT_TYPE), ctorWithNoObj.getInstanceType());

    FunctionType ord = new FunctionType(
        registry, "Ord", null, arrowType, null, null, false, false);
    Assert.assertTrue(ord.getTypeOfThis().isUnknownType());

    ord.setInstanceType(registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE));
    Assert.assertEquals(registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE), ord.getTypeOfThis());
  }

  @Test(expected = IllegalStateException.class)
  public void testGetInstanceTypeOnOrdinaryFunctionThrows() {
    FunctionType ord = new FunctionType(
        registry, "Ord", null, arrowType, null, null, false, false);
    ord.getInstanceType();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidSourceNodeThrows() {
    new FunctionType(
        registry, "Invalid", new Node(Token.NAME), arrowType, null, null, false, false);
  }
}
