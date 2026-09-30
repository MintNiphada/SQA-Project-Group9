package com.google.javascript.jscomp.type;

import static com.google.javascript.rhino.jstype.JSTypeNative.ALL_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.BOOLEAN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NO_OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NO_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NULL_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NUMBER_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.STRING_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.U2U_CONSTRUCTOR_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.UNKNOWN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.VOID_TYPE;

import com.google.javascript.jscomp.CodingConvention;
import com.google.javascript.jscomp.GoogleCodingConvention;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.EnumElementType;
import com.google.javascript.rhino.jstype.EnumType;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.ParameterizedType;
import com.google.javascript.rhino.jstype.StaticSlot;
import com.google.javascript.rhino.jstype.TemplateType;
import com.google.javascript.rhino.jstype.UnionType;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class ChainableReverseAbstractInterpreterTest {

  private JSTypeRegistry registry;
  private CodingConvention convention;
  private ConcreteInterpreter interpreter;

  private static class ConcreteInterpreter extends ChainableReverseAbstractInterpreter {
    private FlowScope returnedScope;

    ConcreteInterpreter(CodingConvention convention, JSTypeRegistry typeRegistry) {
      super(convention, typeRegistry);
    }

    void setPreciserScope(FlowScope scope) {
      this.returnedScope = scope;
    }

    @Override
    public FlowScope getPreciserScopeKnowingConditionOutcome(
        Node condition, FlowScope blindScope, boolean outcome) {
      return returnedScope != null ? returnedScope : blindScope;
    }
  }

  private static class DummyTrueTypeOfVisitor
      extends ChainableReverseAbstractInterpreter.RestrictByTrueTypeOfResultVisitor {
    DummyTrueTypeOfVisitor(ChainableReverseAbstractInterpreter interpreter) {
      interpreter.super();
    }

    @Override
    protected JSType caseTopType(JSType topType) {
      return topType;
    }
  }

  private static class DummyFalseTypeOfVisitor
      extends ChainableReverseAbstractInterpreter.RestrictByFalseTypeOfResultVisitor {
    DummyFalseTypeOfVisitor(ChainableReverseAbstractInterpreter interpreter) {
      interpreter.super();
    }
  }

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(null);
    convention = new GoogleCodingConvention();
    interpreter = new ConcreteInterpreter(convention, registry);
  }

  @SuppressWarnings("unchecked")
  private StaticSlot<JSType> createSlot(final String name, final JSType type) {
    return (StaticSlot<JSType>) Proxy.newProxyInstance(
        StaticSlot.class.getClassLoader(),
        new Class<?>[]{StaticSlot.class},
        new InvocationHandler() {
          @Override
          public Object invoke(Object proxy, Method method, Object[] args) {
            if ("getName".equals(method.getName())) {
              return name;
            }
            if ("getType".equals(method.getName())) {
              return type;
            }
            return null;
          }
        });
  }

  private FlowScope createMockFlowScope(
      final Map<String, StaticSlot<JSType>> slots,
      final Map<String, JSType> inferredSlots,
      final Map<String, JSType> qualifiedInferredSlots) {
    return (FlowScope) Proxy.newProxyInstance(
        FlowScope.class.getClassLoader(),
        new Class<?>[]{FlowScope.class},
        new InvocationHandler() {
          @Override
          public Object invoke(Object proxy, Method method, Object[] args) {
            String name = method.getName();
            if ("getSlot".equals(name)) {
              return slots.get(args[0]);
            } else if ("inferSlotType".equals(name)) {
              if (inferredSlots != null) {
                inferredSlots.put((String) args[0], (JSType) args[1]);
              }
              return null;
            } else if ("inferQualifiedSlot".equals(name)) {
              if (qualifiedInferredSlots != null) {
                qualifiedInferredSlots.put((String) args[1], (JSType) args[3]);
              }
              return null;
            }
            return null;
          }
        });
  }

  @Test(expected = NullPointerException.class)
  public void testConstructorNullConvention() {
    new ConcreteInterpreter(null, registry);
  }

  @Test
  public void testChainAppendingAndGetFirst() {
    ConcreteInterpreter link1 = new ConcreteInterpreter(convention, registry);
    ConcreteInterpreter link2 = new ConcreteInterpreter(convention, registry);
    ConcreteInterpreter link3 = new ConcreteInterpreter(convention, registry);

    Assert.assertSame(link1, link1.getFirst());

    ChainableReverseAbstractInterpreter appended = link1.append(link2);
    Assert.assertSame(link2, appended);
    Assert.assertSame(link1, link2.getFirst());

    link2.append(link3);
    Assert.assertSame(link1, link3.getFirst());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAppendAlreadyAppendedLinkThrows() {
    ConcreteInterpreter link1 = new ConcreteInterpreter(convention, registry);
    ConcreteInterpreter link2 = new ConcreteInterpreter(convention, registry);
    ConcreteInterpreter link3 = new ConcreteInterpreter(convention, registry);

    link2.append(link3);
    link1.append(link2);
  }

  @Test
  public void testFirstPreciserScopeKnowingConditionOutcome() {
    ConcreteInterpreter link1 = new ConcreteInterpreter(convention, registry);
    ConcreteInterpreter link2 = new ConcreteInterpreter(convention, registry);
    link1.append(link2);

    Map<String, StaticSlot<JSType>> slots = new HashMap<String, StaticSlot<JSType>>();
    FlowScope scope1 = createMockFlowScope(slots, null, null);
    FlowScope scope2 = createMockFlowScope(slots, null, null);

    link1.setPreciserScope(scope1);
    link2.setPreciserScope(scope2);

    Node cond = Node.newString(Token.NAME, "a");
    FlowScope result = link2.firstPreciserScopeKnowingConditionOutcome(cond, scope2, true);
    Assert.assertSame(scope1, result);
  }

  @Test
  public void testNextPreciserScopeKnowingConditionOutcome() {
    ConcreteInterpreter link1 = new ConcreteInterpreter(convention, registry);
    ConcreteInterpreter link2 = new ConcreteInterpreter(convention, registry);

    Map<String, StaticSlot<JSType>> slots = new HashMap<String, StaticSlot<JSType>>();
    FlowScope blind = createMockFlowScope(slots, null, null);
    FlowScope link2Scope = createMockFlowScope(slots, null, null);

    Node cond = Node.newString(Token.NAME, "a");

    FlowScope resWithoutNext = link1.nextPreciserScopeKnowingConditionOutcome(cond, blind, true);
    Assert.assertSame(blind, resWithoutNext);

    link1.append(link2);
    link2.setPreciserScope(link2Scope);

    FlowScope resWithNext = link1.nextPreciserScopeKnowingConditionOutcome(cond, blind, true);
    Assert.assertSame(link2Scope, resWithNext);
  }

  @Test
  public void testGetTypeIfRefinableNameNode() {
    Map<String, StaticSlot<JSType>> slots = new HashMap<String, StaticSlot<JSType>>();
    FlowScope scope = createMockFlowScope(slots, null, null);

    Node nameNode = Node.newString(Token.NAME, "foo");
    Assert.assertNull(interpreter.getTypeIfRefinable(nameNode, scope));

    slots.put("foo", createSlot("foo", registry.getNativeType(NUMBER_TYPE)));
    Assert.assertEquals(registry.getNativeType(NUMBER_TYPE),
        interpreter.getTypeIfRefinable(nameNode, scope));

    slots.put("foo", createSlot("foo", null));
    nameNode.setJSType(registry.getNativeType(STRING_TYPE));
    Assert.assertEquals(registry.getNativeType(STRING_TYPE),
        interpreter.getTypeIfRefinable(nameNode, scope));
  }

  @Test
  public void testGetTypeIfRefinableGetPropNode() {
    Map<String, StaticSlot<JSType>> slots = new HashMap<String, StaticSlot<JSType>>();
    FlowScope scope = createMockFlowScope(slots, null, null);

    Node getprop = new Node(Token.GETPROP,
        Node.newString(Token.NAME, "a"),
        Node.newString(Token.STRING, "b"));

    Assert.assertEquals(registry.getNativeType(UNKNOWN_TYPE),
        interpreter.getTypeIfRefinable(getprop, scope));

    getprop.setJSType(registry.getNativeType(BOOLEAN_TYPE));
    Assert.assertEquals(registry.getNativeType(BOOLEAN_TYPE),
        interpreter.getTypeIfRefinable(getprop, scope));

    slots.put("a.b", createSlot("a.b", registry.getNativeType(NUMBER_TYPE)));
    Assert.assertEquals(registry.getNativeType(NUMBER_TYPE),
        interpreter.getTypeIfRefinable(getprop, scope));

    slots.put("a.b", createSlot("a.b", null));
    Assert.assertEquals(registry.getNativeType(BOOLEAN_TYPE),
        interpreter.getTypeIfRefinable(getprop, scope));

    Node invalidProp = new Node(Token.GETPROP,
        new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2)),
        Node.newString(Token.STRING, "b"));
    Assert.assertNull(interpreter.getTypeIfRefinable(invalidProp, scope));
  }

  @Test
  public void testGetTypeIfRefinableOtherNodes() {
    Map<String, StaticSlot<JSType>> slots = new HashMap<String, StaticSlot<JSType>>();
    FlowScope scope = createMockFlowScope(slots, null, null);

    Node numberNode = Node.newNumber(42);
    Assert.assertNull(interpreter.getTypeIfRefinable(numberNode, scope));
  }

  @Test
  public void testDeclareNameInScope() {
    Map<String, JSType> inferred = new HashMap<String, JSType>();
    Map<String, JSType> qualified = new HashMap<String, JSType>();
    FlowScope scope = createMockFlowScope(new HashMap<String, StaticSlot<JSType>>(),
        inferred, qualified);

    Node nameNode = Node.newString(Token.NAME, "x");
    interpreter.declareNameInScope(scope, nameNode, registry.getNativeType(NUMBER_TYPE));
    Assert.assertEquals(registry.getNativeType(NUMBER_TYPE), inferred.get("x"));

    Node getprop = new Node(Token.GETPROP,
        Node.newString(Token.NAME, "x"),
        Node.newString(Token.STRING, "y"));
    interpreter.declareNameInScope(scope, getprop, registry.getNativeType(STRING_TYPE));
    Assert.assertEquals(registry.getNativeType(STRING_TYPE), qualified.get("x.y"));

    getprop.setJSType(registry.getNativeType(BOOLEAN_TYPE));
    interpreter.declareNameInScope(scope, getprop, registry.getNativeType(BOOLEAN_TYPE));
    Assert.assertEquals(registry.getNativeType(BOOLEAN_TYPE), qualified.get("x.y"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDeclareNameInScopeInvalidNodeThrows() {
    FlowScope scope = createMockFlowScope(new HashMap<String, StaticSlot<JSType>>(), null, null);
    Node numberNode = Node.newNumber(123);
    interpreter.declareNameInScope(scope, numberNode, registry.getNativeType(NUMBER_TYPE));
  }

  @Test
  public void testGetRestrictedWithoutUndefined() {
    Assert.assertNull(interpreter.getRestrictedWithoutUndefined(null));
    Assert.assertNull(interpreter.getRestrictedWithoutUndefined(registry.getNativeType(VOID_TYPE)));

    Assert.assertEquals(registry.getNativeType(NUMBER_TYPE),
        interpreter.getRestrictedWithoutUndefined(registry.getNativeType(NUMBER_TYPE)));
    Assert.assertEquals(registry.getNativeType(STRING_TYPE),
        interpreter.getRestrictedWithoutUndefined(registry.getNativeType(STRING_TYPE)));
    Assert.assertEquals(registry.getNativeType(BOOLEAN_TYPE),
        interpreter.getRestrictedWithoutUndefined(registry.getNativeType(BOOLEAN_TYPE)));
    Assert.assertEquals(registry.getNativeType(NULL_TYPE),
        interpreter.getRestrictedWithoutUndefined(registry.getNativeType(NULL_TYPE)));
    Assert.assertEquals(registry.getNativeType(NO_OBJECT_TYPE),
        interpreter.getRestrictedWithoutUndefined(registry.getNativeType(NO_OBJECT_TYPE)));
    Assert.assertEquals(registry.getNativeType(NO_TYPE),
        interpreter.getRestrictedWithoutUndefined(registry.getNativeType(NO_TYPE)));
    Assert.assertEquals(registry.getNativeType(UNKNOWN_TYPE),
        interpreter.getRestrictedWithoutUndefined(registry.getNativeType(UNKNOWN_TYPE)));

    FunctionType fnType = registry.createFunctionType(registry.getNativeType(NUMBER_TYPE));
    Assert.assertSame(fnType, interpreter.getRestrictedWithoutUndefined(fnType));

    ObjectType objType = registry.getNativeObjectType(OBJECT_TYPE);
    Assert.assertSame(objType, interpreter.getRestrictedWithoutUndefined(objType));

    JSType allType = registry.getNativeType(ALL_TYPE);
    JSType restAll = interpreter.getRestrictedWithoutUndefined(allType);
    Assert.assertTrue(restAll.isUnionType());

    JSType numOrVoid = registry.createUnionType(NUMBER_TYPE, VOID_TYPE);
    Assert.assertEquals(registry.getNativeType(NUMBER_TYPE),
        interpreter.getRestrictedWithoutUndefined(numOrVoid));

    EnumType enumType = registry.createEnumType("NumEnum", null, registry.getNativeType(NUMBER_TYPE));
    EnumElementType enumElem = enumType.getElementsType();
    Assert.assertSame(enumElem, interpreter.getRestrictedWithoutUndefined(enumElem));

    EnumType voidEnum = registry.createEnumType("VoidEnum", null, registry.getNativeType(VOID_TYPE));
    EnumElementType voidEnumElem = voidEnum.getElementsType();
    Assert.assertNull(interpreter.getRestrictedWithoutUndefined(voidEnumElem));

    TemplateType templateType = new TemplateType(registry, "T");
    Assert.assertSame(templateType, interpreter.getRestrictedWithoutUndefined(templateType));

    ParameterizedType paramType = new ParameterizedType(registry, objType, registry.getNativeType(STRING_TYPE));
    Assert.assertSame(paramType, interpreter.getRestrictedWithoutUndefined(paramType));
  }

  @Test
  public void testGetRestrictedWithoutNull() {
    Assert.assertNull(interpreter.getRestrictedWithoutNull(null));
    Assert.assertNull(interpreter.getRestrictedWithoutNull(registry.getNativeType(NULL_TYPE)));

    Assert.assertEquals(registry.getNativeType(NUMBER_TYPE),
        interpreter.getRestrictedWithoutNull(registry.getNativeType(NUMBER_TYPE)));
    Assert.assertEquals(registry.getNativeType(STRING_TYPE),
        interpreter.getRestrictedWithoutNull(registry.getNativeType(STRING_TYPE)));
    Assert.assertEquals(registry.getNativeType(BOOLEAN_TYPE),
        interpreter.getRestrictedWithoutNull(registry.getNativeType(BOOLEAN_TYPE)));
    Assert.assertEquals(registry.getNativeType(VOID_TYPE),
        interpreter.getRestrictedWithoutNull(registry.getNativeType(VOID_TYPE)));
    Assert.assertEquals(registry.getNativeType(NO_OBJECT_TYPE),
        interpreter.getRestrictedWithoutNull(registry.getNativeType(NO_OBJECT_TYPE)));
    Assert.assertEquals(registry.getNativeType(NO_TYPE),
        interpreter.getRestrictedWithoutNull(registry.getNativeType(NO_TYPE)));
    Assert.assertEquals(registry.getNativeType(UNKNOWN_TYPE),
        interpreter.getRestrictedWithoutNull(registry.getNativeType(UNKNOWN_TYPE)));

    FunctionType fnType = registry.createFunctionType(registry.getNativeType(NUMBER_TYPE));
    Assert.assertSame(fnType, interpreter.getRestrictedWithoutNull(fnType));

    ObjectType objType = registry.getNativeObjectType(OBJECT_TYPE);
    Assert.assertSame(objType, interpreter.getRestrictedWithoutNull(objType));

    JSType allType = registry.getNativeType(ALL_TYPE);
    JSType restAll = interpreter.getRestrictedWithoutNull(allType);
    Assert.assertTrue(restAll.isUnionType());

    JSType numOrNull = registry.createUnionType(NUMBER_TYPE, NULL_TYPE);
    Assert.assertEquals(registry.getNativeType(NUMBER_TYPE),
        interpreter.getRestrictedWithoutNull(numOrNull));

    EnumType enumType = registry.createEnumType("NumEnum2", null, registry.getNativeType(NUMBER_TYPE));
    EnumElementType enumElem = enumType.getElementsType();
    Assert.assertSame(enumElem, interpreter.getRestrictedWithoutNull(enumElem));

    EnumType nullEnum = registry.createEnumType("NullEnum", null, registry.getNativeType(NULL_TYPE));
    EnumElementType nullEnumElem = nullEnum.getElementsType();
    Assert.assertNull(interpreter.getRestrictedWithoutNull(nullEnumElem));

    TemplateType templateType = new TemplateType(registry, "T");
    Assert.assertSame(templateType, interpreter.getRestrictedWithoutNull(templateType));

    ParameterizedType paramType = new ParameterizedType(registry, objType, registry.getNativeType(STRING_TYPE));
    Assert.assertSame(paramType, interpreter.getRestrictedWithoutNull(paramType));
  }

  @Test
  public void testGetRestrictedByTypeOfResultNullType() {
    Assert.assertNull(interpreter.getRestrictedByTypeOfResult(null, "number", false));
    Assert.assertEquals(registry.getNativeType(NUMBER_TYPE),
        interpreter.getRestrictedByTypeOfResult(null, "number", true));
    Assert.assertEquals(registry.getNativeType(BOOLEAN_TYPE),
        interpreter.getRestrictedByTypeOfResult(null, "boolean", true));
    Assert.assertEquals(registry.getNativeType(STRING_TYPE),
        interpreter.getRestrictedByTypeOfResult(null, "string", true));
    Assert.assertEquals(registry.getNativeType(VOID_TYPE),
        interpreter.getRestrictedByTypeOfResult(null, "undefined", true));
    Assert.assertEquals(registry.getNativeType(U2U_CONSTRUCTOR_TYPE),
        interpreter.getRestrictedByTypeOfResult(null, "function", true));
    Assert.assertEquals(registry.getNativeType(UNKNOWN_TYPE),
        interpreter.getRestrictedByTypeOfResult(null, "custom_unknown", true));
  }

  @Test
  public void testGetRestrictedByTypeOfResultTopTypes() {
    JSType allType = registry.getNativeType(ALL_TYPE);
    JSType unkType = registry.getNativeType(UNKNOWN_TYPE);

    Assert.assertEquals(registry.getNativeType(NUMBER_TYPE),
        interpreter.getRestrictedByTypeOfResult(allType, "number", true));
    Assert.assertEquals(registry.getNativeType(NUMBER_TYPE),
        interpreter.getRestrictedByTypeOfResult(unkType, "number", true));
    Assert.assertEquals(allType,
        interpreter.getRestrictedByTypeOfResult(allType, "number", false));
    Assert.assertEquals(unkType,
        interpreter.getRestrictedByTypeOfResult(unkType, "number", false));
    Assert.assertEquals(unkType,
        interpreter.getRestrictedByTypeOfResult(unkType, "unknown_type", true));
  }

  @Test
  public void testGetRestrictedByTypeOfResultPrimitives() {
    JSType num = registry.getNativeType(NUMBER_TYPE);
    Assert.assertEquals(num, interpreter.getRestrictedByTypeOfResult(num, "number", true));
    Assert.assertNull(interpreter.getRestrictedByTypeOfResult(num, "number", false));
    Assert.assertNull(interpreter.getRestrictedByTypeOfResult(num, "string", true));
    Assert.assertEquals(num, interpreter.getRestrictedByTypeOfResult(num, "string", false));

    JSType str = registry.getNativeType(STRING_TYPE);
    Assert.assertEquals(str, interpreter.getRestrictedByTypeOfResult(str, "string", true));
    Assert.assertNull(interpreter.getRestrictedByTypeOfResult(str, "string", false));
    Assert.assertNull(interpreter.getRestrictedByTypeOfResult(str, "number", true));
    Assert.assertEquals(str, interpreter.getRestrictedByTypeOfResult(str, "number", false));

    JSType bool = registry.getNativeType(BOOLEAN_TYPE);
    Assert.assertEquals(bool, interpreter.getRestrictedByTypeOfResult(bool, "boolean", true));
    Assert.assertNull(interpreter.getRestrictedByTypeOfResult(bool, "boolean", false));
    Assert.assertNull(interpreter.getRestrictedByTypeOfResult(bool, "number", true));

    JSType voidType = registry.getNativeType(VOID_TYPE);
    Assert.assertEquals(voidType, interpreter.getRestrictedByTypeOfResult(voidType, "undefined", true));
    Assert.assertNull(interpreter.getRestrictedByTypeOfResult(voidType, "undefined", false));
    Assert.assertNull(interpreter.getRestrictedByTypeOfResult(voidType, "number", true));

    JSType nullType = registry.getNativeType(NULL_TYPE);
    Assert.assertEquals(nullType, interpreter.getRestrictedByTypeOfResult(nullType, "object", true));
    Assert.assertNull(interpreter.getRestrictedByTypeOfResult(nullType, "object", false));
    Assert.assertNull(interpreter.getRestrictedByTypeOfResult(nullType, "number", true));

    JSType noObj = registry.getNativeType(NO_OBJECT_TYPE);
    Assert.assertEquals(noObj, interpreter.getRestrictedByTypeOfResult(noObj, "object", true));
    Assert.assertEquals(noObj, interpreter.getRestrictedByTypeOfResult(noObj, "function", true));
    Assert.assertNull(interpreter.getRestrictedByTypeOfResult(noObj, "string", true));
    Assert.assertEquals(noObj, interpreter.getRestrictedByTypeOfResult(noObj, "string", false));

    JSType noType = registry.getNativeType(NO_TYPE);
    Assert.assertEquals(noType, interpreter.getRestrictedByTypeOfResult(noType, "number", true));
  }

  @Test
  public void testGetRestrictedByTypeOfResultObjectsAndFunctions() {
    FunctionType fnType = registry.createFunctionType(registry.getNativeType(NUMBER_TYPE));
    Assert.assertSame(fnType, interpreter.getRestrictedByTypeOfResult(fnType, "function", true));
    Assert.assertNull(interpreter.getRestrictedByTypeOfResult(fnType, "function", false));
    Assert.assertNull(interpreter.getRestrictedByTypeOfResult(fnType, "object", true));
    Assert.assertSame(fnType, interpreter.getRestrictedByTypeOfResult(fnType, "object", false));

    ObjectType objType = registry.getNativeObjectType(OBJECT_TYPE);
    Assert.assertSame(objType, interpreter.getRestrictedByTypeOfResult(objType, "object", true));
    Assert.assertNull(interpreter.getRestrictedByTypeOfResult(objType, "object", false));
    Assert.assertEquals(registry.getNativeType(U2U_CONSTRUCTOR_TYPE),
        interpreter.getRestrictedByTypeOfResult(objType, "function", true));
    Assert.assertNull(interpreter.getRestrictedByTypeOfResult(objType, "function", false));

    TemplateType templateType = new TemplateType(registry, "T");
    Assert.assertSame(templateType, interpreter.getRestrictedByTypeOfResult(templateType, "object", true));

    ParameterizedType paramType = new ParameterizedType(registry, objType, registry.getNativeType(STRING_TYPE));
    Assert.assertSame(paramType, interpreter.getRestrictedByTypeOfResult(paramType, "object", true));
  }

  @Test
  public void testGetRestrictedByTypeOfResultUnionsAndEnums() {
    UnionType union = (UnionType) registry.createUnionType(NUMBER_TYPE, STRING_TYPE, BOOLEAN_TYPE);
    Assert.assertEquals(registry.getNativeType(NUMBER_TYPE),
        interpreter.getRestrictedByTypeOfResult(union, "number", true));
    Assert.assertEquals(registry.createUnionType(STRING_TYPE, BOOLEAN_TYPE),
        interpreter.getRestrictedByTypeOfResult(union, "number", false));
    Assert.assertNull(interpreter.getRestrictedByTypeOfResult(union, "undefined", true));

    EnumType numEnum = registry.createEnumType("NumEnum3", null, registry.getNativeType(NUMBER_TYPE));
    EnumElementType numEnumElem = numEnum.getElementsType();
    Assert.assertSame(numEnumElem, interpreter.getRestrictedByTypeOfResult(numEnumElem, "number", true));
    Assert.assertNull(interpreter.getRestrictedByTypeOfResult(numEnumElem, "number", false));

    EnumType unionEnum = registry.createEnumType("UnionEnum", null, union);
    EnumElementType unionEnumElem = unionEnum.getElementsType();
    Assert.assertEquals(registry.getNativeType(NUMBER_TYPE),
        interpreter.getRestrictedByTypeOfResult(unionEnumElem, "number", true));
  }

  @Test
  public void testRestrictByTrueTypeOfResultVisitorDefaults() {
    DummyTrueTypeOfVisitor visitor = new DummyTrueTypeOfVisitor(interpreter);

    Assert.assertNull(visitor.caseNoObjectType());
    Assert.assertNull(visitor.caseBooleanType());
    Assert.assertNull(visitor.caseFunctionType(registry.createFunctionType(registry.getNativeType(NUMBER_TYPE))));
    Assert.assertNull(visitor.caseNullType());
    Assert.assertNull(visitor.caseNumberType());
    Assert.assertNull(visitor.caseObjectType(registry.getNativeObjectType(OBJECT_TYPE)));
    Assert.assertNull(visitor.caseStringType());
    Assert.assertNull(visitor.caseVoidType());
    Assert.assertEquals(registry.getNativeType(NO_TYPE), visitor.caseNoType());
  }

  @Test
  public void testRestrictByFalseTypeOfResultVisitorDefaults() {
    DummyFalseTypeOfVisitor visitor = new DummyFalseTypeOfVisitor(interpreter);

    Assert.assertEquals(registry.getNativeType(ALL_TYPE), visitor.caseAllType());
    Assert.assertEquals(registry.getNativeType(UNKNOWN_TYPE), visitor.caseUnknownType());
    Assert.assertEquals(registry.getNativeType(NO_OBJECT_TYPE), visitor.caseNoObjectType());
    Assert.assertEquals(registry.getNativeType(BOOLEAN_TYPE), visitor.caseBooleanType());

    FunctionType fnType = registry.createFunctionType(registry.getNativeType(NUMBER_TYPE));
    Assert.assertSame(fnType, visitor.caseFunctionType(fnType));

    Assert.assertEquals(registry.getNativeType(NULL_TYPE), visitor.caseNullType());
    Assert.assertEquals(registry.getNativeType(NUMBER_TYPE), visitor.caseNumberType());

    ObjectType objType = registry.getNativeObjectType(OBJECT_TYPE);
    Assert.assertSame(objType, visitor.caseObjectType(objType));

    Assert.assertEquals(registry.getNativeType(STRING_TYPE), visitor.caseStringType());
    Assert.assertEquals(registry.getNativeType(VOID_TYPE), visitor.caseVoidType());
    Assert.assertEquals(registry.getNativeType(NO_TYPE), visitor.caseNoType());

    TemplateType templateType = new TemplateType(registry, "T");
    Assert.assertSame(templateType, visitor.caseTemplateType(templateType));

    ParameterizedType paramType = new ParameterizedType(registry, objType, registry.getNativeType(STRING_TYPE));
    Assert.assertSame(paramType, visitor.caseParameterizedType(paramType));
  }
}