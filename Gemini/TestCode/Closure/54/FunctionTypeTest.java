package com.google.javascript.rhino.jstype;

import static com.google.javascript.rhino.jstype.JSTypeNative.BOOLEAN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.FUNCTION_INSTANCE_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.LEAST_FUNCTION_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NO_OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NUMBER_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.STRING_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.U2U_CONSTRUCTOR_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.UNKNOWN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.VOID_TYPE;

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
import java.util.Set;

public class FunctionTypeTest {

  private JSTypeRegistry registry;
  private JSType numberType;
  private JSType stringType;
  private JSType booleanType;
  private ObjectType objectType;
  private FunctionType fnInstanceType;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(null);
    numberType = registry.getNativeType(NUMBER_TYPE);
    stringType = registry.getNativeType(STRING_TYPE);
    booleanType = registry.getNativeType(BOOLEAN_TYPE);
    objectType = registry.getNativeObjectType(OBJECT_TYPE);
    fnInstanceType = registry.getNativeFunctionType(FUNCTION_INSTANCE_TYPE);
  }

  @Test
  public void testOrdinaryFunctionCreationAndProperties() {
    Node fnNode = new Node(Token.FUNCTION);
    Node paramNode = new Node(Token.LP);
    Node param1 = Node.newString(Token.NAME, "a");
    param1.setJSType(numberType);
    paramNode.addChildToBack(param1);

    ArrowType arrow = new ArrowType(registry, paramNode, stringType);
    FunctionType fn = new FunctionType(
        registry, "foo", fnNode, arrow, null, null, false, false);

    Assert.assertFalse(fn.isConstructor());
    Assert.assertFalse(fn.isInterface());
    Assert.assertTrue(fn.isOrdinaryFunction());
    Assert.assertTrue(fn.canBeCalled());
    Assert.assertSame(fn, fn.toMaybeFunctionType());
    Assert.assertSame(stringType, fn.getReturnType());
    Assert.assertFalse(fn.isReturnTypeInferred());
    Assert.assertSame(arrow, fn.getInternalArrowType());
    Assert.assertSame(fnNode, fn.getSource());
    Assert.assertEquals(1, fn.getMinArguments());
    Assert.assertEquals(1, fn.getMaxArguments());
    Assert.assertEquals("foo", fn.getReferenceName());
    Assert.assertNull(fn.getTemplateTypeName());
    Assert.assertFalse(fn.hasInstanceType());

    Node newSource = new Node(Token.FUNCTION);
    fn.setSource(newSource);
    Assert.assertSame(newSource, fn.getSource());
  }

  @Test
  public void testConstructorCreationAndInstanceType() {
    Node fnNode = new Node(Token.FUNCTION);
    Node paramNode = new Node(Token.LP);
    ArrowType arrow = new ArrowType(registry, paramNode, null);
    FunctionType ctor = new FunctionType(
        registry, "MyClass", fnNode, arrow, null, "T", true, false);

    Assert.assertTrue(ctor.isConstructor());
    Assert.assertFalse(ctor.isInterface());
    Assert.assertFalse(ctor.isOrdinaryFunction());
    Assert.assertTrue(ctor.hasInstanceType());
    Assert.assertEquals("T", ctor.getTemplateTypeName());

    ObjectType instance = ctor.getInstanceType();
    Assert.assertNotNull(instance);
    Assert.assertSame(instance, ctor.getTypeOfThis());

    ObjectType customInstance = new InstanceObjectType(registry, ctor);
    ctor.setInstanceType(customInstance);
    Assert.assertSame(customInstance, ctor.getInstanceType());
  }

  @Test
  public void testInterfaceCreation() {
    Node fnNode = new Node(Token.FUNCTION);
    FunctionType iface = FunctionType.forInterface(registry, "MyInterface", fnNode);

    Assert.assertTrue(iface.isInterface());
    Assert.assertFalse(iface.isConstructor());
    Assert.assertFalse(iface.isOrdinaryFunction());
    Assert.assertTrue(iface.hasInstanceType());
    Assert.assertEquals("MyInterface", iface.getReferenceName());
    Assert.assertNotNull(iface.getInstanceType());
  }

  @Test(expected = IllegalStateException.class)
  public void testGetInstanceTypeOnOrdinaryFunctionThrows() {
    Node paramNode = new Node(Token.LP);
    ArrowType arrow = new ArrowType(registry, paramNode, stringType);
    FunctionType fn = new FunctionType(
        registry, null, null, arrow, null, null, false, false);
    fn.getInstanceType();
  }

  @Test
  public void testMinMaxArgumentsWithOptionalAndVarArgs() {
    Node paramNode = new Node(Token.LP);

    Node reqParam = Node.newString(Token.NAME, "req");
    reqParam.setJSType(numberType);

    Node optParam = Node.newString(Token.NAME, "opt");
    optParam.setJSType(stringType);
    optParam.setOptionalArg(true);

    Node varParam = Node.newString(Token.NAME, "rest");
    varParam.setJSType(booleanType);
    varParam.setVarArgs(true);

    paramNode.addChildToBack(reqParam);
    paramNode.addChildToBack(optParam);
    paramNode.addChildToBack(varParam);

    ArrowType arrow = new ArrowType(registry, paramNode, null);
    FunctionType fn = new FunctionType(
        registry, null, null, arrow, null, null, false, false);

    Assert.assertEquals(1, fn.getMinArguments());
    Assert.assertEquals(Integer.MAX_VALUE, fn.getMaxArguments());
    Assert.assertEquals(3, Iterables.size(fn.getParameters()));

    // Test with no varargs
    Node paramNode2 = new Node(Token.LP);
    paramNode2.addChildToBack(Node.newString(Token.NAME, "p1"));
    paramNode2.addChildToBack(Node.newString(Token.NAME, "p2"));
    ArrowType arrow2 = new ArrowType(registry, paramNode2, null);
    FunctionType fn2 = new FunctionType(
        registry, null, null, arrow2, null, null, false, false);
    Assert.assertEquals(2, fn2.getMaxArguments());
    Assert.assertEquals(2, fn2.getMinArguments());

    // Test with empty parameters
    Node emptyParamNode = new Node(Token.LP);
    ArrowType arrow3 = new ArrowType(registry, emptyParamNode, null);
    FunctionType fn3 = new FunctionType(
        registry, null, null, arrow3, null, null, false, false);
    Assert.assertEquals(0, fn3.getMinArguments());
    Assert.assertEquals(0, fn3.getMaxArguments());
  }

  @Test
  public void testGetParametersWhenNull() {
    ArrowType arrow = new ArrowType(registry, null, null);
    FunctionType fn = new FunctionType(
        registry, null, null, arrow, null, null, false, false);
    Assert.assertNull(fn.getParametersNode());
    Assert.assertFalse(fn.getParameters().iterator().hasNext());
  }

  @Test
  public void testPrototypeHandlingAndSlots() {
    Node paramNode = new Node(Token.LP);
    ArrowType arrow = new ArrowType(registry, paramNode, null);
    FunctionType fn = new FunctionType(
        registry, "Base", null, arrow, null, null, false, false);

    Assert.assertFalse(fn.hasCachedValues());
    ObjectType proto = fn.getPrototype();
    Assert.assertNotNull(proto);
    Assert.assertTrue(fn.hasCachedValues());

    StaticSlot<JSType> slot = fn.getSlot("prototype");
    Assert.assertNotNull(slot);
    Assert.assertSame(proto, slot.getType());

    Set<String> propNames = fn.getOwnPropertyNames();
    Assert.assertTrue(propNames.contains("prototype"));

    // Setting prototype to null should return false
    Assert.assertFalse(fn.setPrototype(null));

    // Setting prototype based on another object
    PrototypeObjectType customProto =
        new PrototypeObjectType(registry, "CustomProto", objectType);
    fn.setPrototypeBasedOn(customProto);
    Assert.assertSame(customProto, fn.getPrototype());

    // Setting prototype based on non-prototype object
    fn.setPrototypeBasedOn(registry.getNativeObjectType(UNKNOWN_TYPE));
    Assert.assertNotNull(fn.getPrototype());

    // defineProperty for prototype
    Assert.assertTrue(fn.defineProperty("prototype", customProto, false, null));
    Assert.assertFalse(fn.defineProperty("prototype", numberType, false, null));
  }

  @Test
  public void testConstructorCannotSetPrototypeToInstanceType() {
    FunctionType ctor = registry.createConstructorType(
        "Ctor", null, null, null);
    Assert.assertFalse(ctor.setPrototype((PrototypeObjectType) ctor.getInstanceType()));
  }

  @Test
  public void testSuperClassConstructorAndInheritance() {
    FunctionType superCtor = registry.createConstructorType(
        "SuperClass", null, null, null);
    FunctionType subCtor = registry.createConstructorType(
        "SubClass", null, null, null);

    subCtor.getPrototype().setImplicitPrototype(superCtor.getInstanceType());
    subCtor.setPrototype(subCtor.getPrototype());

    Assert.assertSame(superCtor, subCtor.getSuperClassConstructor());
    Assert.assertNotNull(superCtor.getSubTypes());
    Assert.assertTrue(superCtor.getSubTypes().contains(subCtor));

    // Top most defining type
    superCtor.getPrototype().defineDeclaredProperty("superProp", stringType, null);
    subCtor.getPrototype().defineDeclaredProperty("subProp", numberType, null);

    Assert.assertSame(superCtor.getInstanceType(),
        subCtor.getTopMostDefiningType("superProp"));
    Assert.assertSame(subCtor.getInstanceType(),
        subCtor.getTopMostDefiningType("subProp"));
  }

  @Test
  public void testImplementedInterfaces() {
    FunctionType iface1 = FunctionType.forInterface(registry, "I1", null);
    FunctionType iface2 = FunctionType.forInterface(registry, "I2", null);
    FunctionType ctor = registry.createConstructorType("ClassA", null, null, null);

    Assert.assertFalse(ctor.hasImplementedInterfaces());
    ctor.setImplementedInterfaces(ImmutableList.of(iface1.getInstanceType()));
    Assert.assertTrue(ctor.hasImplementedInterfaces());

    List<ObjectType> directIfaces = ImmutableList.copyOf(ctor.getImplementedInterfaces());
    Assert.assertEquals(1, directIfaces.size());
    Assert.assertSame(iface1.getInstanceType(), directIfaces.get(0));

    Iterable<ObjectType> allIfaces = ctor.getAllImplementedInterfaces();
    Assert.assertTrue(Iterables.contains(allIfaces, iface1.getInstanceType()));

    // Subclass inheriting interfaces
    FunctionType subCtor = registry.createConstructorType("ClassB", null, null, null);
    subCtor.getPrototype().setImplicitPrototype(ctor.getInstanceType());
    subCtor.setPrototype(subCtor.getPrototype());
    subCtor.setImplementedInterfaces(ImmutableList.of(iface2.getInstanceType()));

    Assert.assertTrue(subCtor.hasImplementedInterfaces());
    List<ObjectType> subAllIfaces = ImmutableList.copyOf(subCtor.getAllImplementedInterfaces());
    Assert.assertTrue(subAllIfaces.contains(iface1.getInstanceType()));
    Assert.assertTrue(subAllIfaces.contains(iface2.getInstanceType()));
  }

  @Test
  public void testExtendedInterfaces() {
    FunctionType superIface = FunctionType.forInterface(registry, "SuperIface", null);
    FunctionType subIface = FunctionType.forInterface(registry, "SubIface", null);

    Assert.assertEquals(0, subIface.getExtendedInterfacesCount());
    subIface.setExtendedInterfaces(ImmutableList.of(superIface.getInstanceType()));
    Assert.assertEquals(1, subIface.getExtendedInterfacesCount());

    Iterable<ObjectType> allExt = subIface.getAllExtendedInterfaces();
    Assert.assertTrue(Iterables.contains(allExt, superIface.getInstanceType()));

    // Top defining interface
    superIface.getInstanceType().defineDeclaredProperty("propA", stringType, null);
    ObjectType topIface = FunctionType.getTopDefiningInterface(
        subIface.getInstanceType(), "propA");
    Assert.assertSame(superIface.getInstanceType(), topIface);

    Assert.assertSame(superIface.getInstanceType(),
        subIface.getTopMostDefiningType("propA"));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testSetExtendedInterfacesOnNonInterfaceThrows() {
    FunctionType ctor = registry.createConstructorType("NonIface", null, null, null);
    ctor.setExtendedInterfaces(Collections.<ObjectType>emptyList());
  }

  @Test
  public void testGetPropertyTypeCallAndApply() {
    Node paramNode = new Node(Token.LP);
    Node param1 = Node.newString(Token.NAME, "arg");
    param1.setJSType(numberType);
    paramNode.addChildToBack(param1);

    ArrowType arrow = new ArrowType(registry, paramNode, stringType);
    FunctionType fn = new FunctionType(
        registry, "fn", null, arrow, objectType, null, false, false);

    JSType callProp = fn.getPropertyType("call");
    Assert.assertTrue(callProp.isFunctionType());
    FunctionType callFn = callProp.toMaybeFunctionType();
    Assert.assertSame(stringType, callFn.getReturnType());

    JSType applyProp = fn.getPropertyType("apply");
    Assert.assertTrue(applyProp.isFunctionType());
    FunctionType applyFn = applyProp.toMaybeFunctionType();
    Assert.assertSame(stringType, applyFn.getReturnType());

    // Null parameters node lazy call property
    ArrowType nullParamsArrow = new ArrowType(registry, null, booleanType);
    FunctionType nullParamsFn = new FunctionType(
        registry, "nullParamsFn", null, nullParamsArrow, null, null, false, false);
    JSType nullParamsCall = nullParamsFn.getPropertyType("call");
    Assert.assertTrue(nullParamsCall.isFunctionType());
  }

  @Test
  public void testIsEquivalentToAndHashCode() {
    Node p1 = new Node(Token.LP);
    ArrowType a1 = new ArrowType(registry, p1, numberType);
    FunctionType fn1 = new FunctionType(
        registry, "fn1", null, a1, objectType, null, false, false);

    Node p2 = new Node(Token.LP);
    ArrowType a2 = new ArrowType(registry, p2, numberType);
    FunctionType fn2 = new FunctionType(
        registry, "fn2", null, a2, objectType, null, false, false);

    Assert.assertTrue(fn1.isEquivalentTo(fn2));
    Assert.assertTrue(fn1.hasEqualCallType(fn2));
    Assert.assertEquals(fn1.hashCode(), fn2.hashCode());
    Assert.assertFalse(fn1.isEquivalentTo(numberType));

    // Constructors equality
    FunctionType ctor1 = registry.createConstructorType("C1", null, null, null);
    FunctionType ctor2 = registry.createConstructorType("C2", null, null, null);
    Assert.assertFalse(ctor1.isEquivalentTo(ctor2));
    Assert.assertTrue(ctor1.isEquivalentTo(ctor1));
    Assert.assertFalse(ctor1.isEquivalentTo(fn1));

    // Interface equality
    FunctionType iface1 = FunctionType.forInterface(registry, "I", null);
    FunctionType iface2 = FunctionType.forInterface(registry, "I", null);
    FunctionType iface3 = FunctionType.forInterface(registry, "I3", null);
    Assert.assertTrue(iface1.isEquivalentTo(iface2));
    Assert.assertFalse(iface1.isEquivalentTo(iface3));
    Assert.assertEquals(iface1.hashCode(), iface2.hashCode());
    Assert.assertFalse(fn1.isEquivalentTo(iface1));
  }

  @Test
  public void testIsSubtype() {
    FunctionType iface = FunctionType.forInterface(registry, "I", null);
    FunctionType ctor = registry.createConstructorType("C", null, null, null);
    FunctionType fn = registry.createFunctionType(stringType);

    // Any function is subtype of interface function
    Assert.assertTrue(fn.isSubtype(iface));
    Assert.assertTrue(ctor.isSubtype(iface));

    // Interface function cannot be assigned to anything except itself or Object/Function
    Assert.assertFalse(iface.isSubtype(ctor));
    Assert.assertFalse(iface.isSubtype(fn));

    // Covariant 'this' check
    FunctionType fnWithObjThis = registry.createFunctionTypeWithThisType(
        null, objectType, Collections.<JSType>emptyList());
    FunctionType fnWithUnknownThis = registry.createFunctionType(stringType);
    Assert.assertTrue(fnWithObjThis.isSubtype(fnWithUnknownThis));
  }

  @Test
  public void testToStringAndDebugHashCodeString() {
    Assert.assertEquals("Function", fnInstanceType.toString());

    Node paramNode = new Node(Token.LP);
    Node reqP = Node.newString(Token.NAME, "r");
    reqP.setJSType(numberType);
    paramNode.addChildToBack(reqP);

    Node varP = Node.newString(Token.NAME, "rest");
    JSType unionType = registry.createUnionType(stringType, registry.getNativeType(VOID_TYPE));
    varP.setJSType(unionType);
    varP.setVarArgs(true);
    paramNode.addChildToBack(varP);

    ArrowType arrow = new ArrowType(registry, paramNode, booleanType);
    FunctionType fn = new FunctionType(
        registry, null, null, arrow, objectType, null, false, false);

    String str = fn.toString();
    Assert.assertTrue(str.contains("this:Object"));
    Assert.assertTrue(str.contains("number"));
    Assert.assertTrue(str.contains("...[string]"));
    Assert.assertTrue(str.endsWith(": boolean"));

    FunctionType ctor = new FunctionType(
        registry, "Ctor", null, arrow, objectType, null, true, false);
    Assert.assertTrue(ctor.toString().contains("new:Object"));

    String debugStr = fn.toDebugHashCodeString();
    Assert.assertNotNull(debugStr);
    Assert.assertTrue(debugStr.startsWith("function ("));
    Assert.assertNotNull(fnInstanceType.toDebugHashCodeString());
  }

  @Test
  public void testLeastSupertypeAndGreatestSubtype() {
    FunctionType fn1 = registry.createFunctionType(numberType, numberType);
    FunctionType fn2 = registry.createFunctionType(stringType, numberType);

    JSType sup = fn1.getLeastSupertype(fn2);
    Assert.assertTrue(sup.isFunctionType());
    Assert.assertEquals(
        registry.createUnionType(numberType, stringType),
        sup.toMaybeFunctionType().getReturnType());

    JSType inf = fn1.getGreatestSubtype(fn2);
    Assert.assertTrue(inf.isFunctionType());

    // Sup with non-function type
    JSType nonFnSup = fn1.getLeastSupertype(numberType);
    Assert.assertNotNull(nonFnSup);

    // Sup with self
    Assert.assertSame(fn1, fn1.getLeastSupertype(fn1));

    // Sup with Function instance type
    Assert.assertSame(fnInstanceType, fn1.getLeastSupertype(fnInstanceType));
    Assert.assertSame(fnInstanceType, fnInstanceType.getLeastSupertype(fn1));
    Assert.assertSame(fn1, fn1.getGreatestSubtype(fnInstanceType));

    // Sup with different parameters fallback to U2U_CONSTRUCTOR_TYPE
    FunctionType fnDifferentParams = registry.createFunctionType(numberType, stringType);
    JSType supDiff = fn1.getLeastSupertype(fnDifferentParams);
    Assert.assertSame(registry.getNativeFunctionType(U2U_CONSTRUCTOR_TYPE), supDiff);
    JSType infDiff = fn1.getGreatestSubtype(fnDifferentParams);
    Assert.assertSame(registry.getNativeFunctionType(LEAST_FUNCTION_TYPE), infDiff);
  }

  @Test
  public void testGetTypeOfThisWithNoObjectType() {
    Node paramNode = new Node(Token.LP);
    ArrowType arrow = new ArrowType(registry, paramNode, stringType);
    ObjectType noObj = registry.getNativeObjectType(NO_OBJECT_TYPE);
    FunctionType fn = new FunctionType(
        registry, null, null, arrow, noObj, null, false, false);

    Assert.assertSame(objectType, fn.getTypeOfThis());
  }

  @Test
  public void testClearCachedValuesAndResolveInternal() {
    FunctionType superCtor = registry.createConstructorType("Super", null, null, null);
    FunctionType subCtor = registry.createConstructorType("Sub", null, null, null);
    subCtor.getPrototype().setImplicitPrototype(superCtor.getInstanceType());
    subCtor.setPrototype(subCtor.getPrototype());

    FunctionType iface = FunctionType.forInterface(registry, "Iface", null);
    subCtor.setImplementedInterfaces(ImmutableList.of(iface.getInstanceType()));

    subCtor.clearCachedValues();

    ErrorReporter reporter = null;
    StaticScope<JSType> scope = null;
    JSType resolved = subCtor.resolveInternal(reporter, scope);
    Assert.assertSame(subCtor, resolved);
    Assert.assertSame(superCtor, subCtor.getSuperClassConstructor());
  }

  @Test
  public void testIsInstanceType() {
    FunctionType u2u = registry.getNativeFunctionType(U2U_CONSTRUCTOR_TYPE);
    Assert.assertTrue(u2u.isInstanceType());

    FunctionType normalFn = registry.createFunctionType(numberType);
    Assert.assertFalse(normalFn.isInstanceType());
  }

  @Test
  public void testVisit() {
    FunctionType fn = registry.createFunctionType(numberType);
    Visitor<String> visitor = new Visitor<String>() {
      @Override public String caseNoType() { return "no"; }
      @Override public String caseEnumElementType(EnumElementType type) { return "enum"; }
      @Override public String caseAllType() { return "all"; }
      @Override public String caseBooleanType() { return "bool"; }
      @Override public String caseNoObjectType() { return "noObj"; }
      @Override public String caseFunctionType(FunctionType type) { return "function"; }
      @Override public String caseObjectType(ObjectType type) { return "object"; }
      @Override public String caseUnknownType() { return "unknown"; }
      @Override public String caseNullType() { return "null"; }
      @Override public String caseNumberType() { return "number"; }
      @Override public String caseStringType() { return "string"; }
      @Override public String caseVoidType() { return "void"; }
      @Override public String caseUnionType(UnionType type) { return "union"; }
      @Override public String caseRecordType(RecordType type) { return "record"; }
    };

    Assert.assertEquals("function", fn.visit(visitor));
  }
}