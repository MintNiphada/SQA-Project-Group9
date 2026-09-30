package com.google.javascript.jscomp.type;

import static com.google.javascript.rhino.jstype.JSTypeNative.ALL_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.BOOLEAN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.CHECKED_UNKNOWN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NO_OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NO_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NULL_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NUMBER_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.STRING_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.U2U_CONSTRUCTOR_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.UNKNOWN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.VOID_TYPE;

import com.google.javascript.jscomp.ClosureCodingConvention;
import com.google.javascript.jscomp.CodingConvention;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.EnumElementType;
import com.google.javascript.rhino.jstype.EnumType;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.ParameterizedType;
import com.google.javascript.rhino.jstype.SimpleSlot;
import com.google.javascript.rhino.jstype.StaticSlot;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class ChainableReverseAbstractInterpreterTest {

  private JSTypeRegistry registry;
  private CodingConvention convention;
  private TestInterpreter interpreter;

  private static class SimpleFlowScope implements FlowScope {
    private final Map<String, StaticSlot<JSType>> slots = new HashMap<String, StaticSlot<JSType>>();

    @Override
    public StaticSlot<JSType> getSlot(String name) {
      return slots.get(name);
    }

    @Override
    public void inferSlotType(String name, JSType type) {
      slots.put(name, new SimpleSlot(name, type, false));
    }

    @Override
    public void inferQualifiedSlot(Node node, String qualifiedName, JSType blindType, JSType resultType) {
      slots.put(qualifiedName, new SimpleSlot(qualifiedName, resultType, false));
    }

    @Override
    public FlowScope createChildFlowScope() {
      SimpleFlowScope child = new SimpleFlowScope();
      child.slots.putAll(this.slots);
      return child;
    }

    @Override
    public FlowScope optimize() {
      return this;
    }

    @Override
    public Node getRootNode() {
      return null;
    }

    @Override
    public StaticSlot<JSType> getOwnSlot(String name) {
      return slots.get(name);
    }

    @Override
    public com.google.javascript.rhino.jstype.StaticScope<JSType> getParentScope() {
      return null;
    }

    @Override
    public com.google.javascript.rhino.jstype.StaticReference<JSType> getDeclaration() {
      return null;
    }

    @Override
    public boolean isResolved() {
      return true;
    }

    @Override
    public com.google.javascript.rhino.jstype.StaticSlot<JSType> getSlot(Node n) {
      return null;
    }

    @Override
    public JSType getTypeOfThis() {
      return null;
    }
  }

  private static class TestInterpreter extends ChainableReverseAbstractInterpreter {
    private FlowScope returnedScope;

    TestInterpreter(CodingConvention convention, JSTypeRegistry registry) {
      super(convention, registry);
    }

    public void setReturnedScope(FlowScope scope) {
      this.returnedScope = scope;
    }

    @Override
    public FlowScope getPreciserScopeKnowingConditionOutcome(Node condition, FlowScope blindScope, boolean outcome) {
      return returnedScope != null ? returnedScope : blindScope;
    }
  }

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(null);
    convention = new ClosureCodingConvention();
    interpreter = new TestInterpreter(convention, registry);
  }

  @Test(expected = NullPointerException.class)
  public void testConstructorNullConvention() {
    new TestInterpreter(null, registry);
  }

  @Test
  public void testAppendAndGetFirst() {
    TestInterpreter link1 = new TestInterpreter(convention, registry);
    TestInterpreter link2 = new TestInterpreter(convention, registry);
    TestInterpreter link3 = new TestInterpreter(convention, registry);

    Assert.assertSame(link1, link1.getFirst());

    ChainableReverseAbstractInterpreter res2 = link1.append(link2);
    Assert.assertSame(link2, res2);
    Assert.assertSame(link1, link2.getFirst());

    ChainableReverseAbstractInterpreter res3 = link2.append(link3);
    Assert.assertSame(link3, res3);
    Assert.assertSame(link1, link3.getFirst());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAppendAlreadyAppendedLinkFails() {
    TestInterpreter link1 = new TestInterpreter(convention, registry);
    TestInterpreter link2 = new TestInterpreter(convention, registry);
    TestInterpreter link3 = new TestInterpreter(convention, registry);

    link1.append(link2);
    link3.append(link1);
  }

  @Test
  public void testFirstAndNextPreciserScopeKnowingConditionOutcome() {
    TestInterpreter link1 = new TestInterpreter(convention, registry);
    TestInterpreter link2 = new TestInterpreter(convention, registry);
    link1.append(link2);

    SimpleFlowScope scope1 = new SimpleFlowScope();
    SimpleFlowScope scope2 = new SimpleFlowScope();
    SimpleFlowScope scope3 = new SimpleFlowScope();

    link1.setReturnedScope(scope2);
    link2.setReturnedScope(scope3);

    Node cond = new Node(Token.NAME);

    FlowScope firstResult = link2.firstPreciserScopeKnowingConditionOutcome(cond, scope1, true);
    Assert.assertSame(scope2, firstResult);

    FlowScope nextResult = link1.nextPreciserScopeKnowingConditionOutcome(cond, scope1, true);
    Assert.assertSame(scope3, nextResult);

    FlowScope endOfChainResult = link2.nextPreciserScopeKnowingConditionOutcome(cond, scope1, true);
    Assert.assertSame(scope1, endOfChainResult);
  }

  @Test
  public void testGetTypeIfRefinableName() {
    SimpleFlowScope scope = new SimpleFlowScope();
    Node nameNode = Node.newString(Token.NAME, "x");

    Assert.assertNull(interpreter.getTypeIfRefinable(nameNode, scope));

    scope.inferSlotType("x", registry.getNativeType(NUMBER_TYPE));
    Assert.assertSame(registry.getNativeType(NUMBER_TYPE), interpreter.getTypeIfRefinable(nameNode, scope));

    // Case where slot type is null, fallback to node type
    scope.slots.put("x", new SimpleSlot("x", null, false));
    nameNode.setJSType(registry.getNativeType(STRING_TYPE));
    Assert.assertSame(registry.getNativeType(STRING_TYPE), interpreter.getTypeIfRefinable(nameNode, scope));

    nameNode.setJSType(null);
    Assert.assertNull(interpreter.getTypeIfRefinable(nameNode, scope));
  }

  @Test
  public void testGetTypeIfRefinableGetProp() {
    SimpleFlowScope scope = new SimpleFlowScope();

    Node objNode = Node.newString(Token.NAME, "a");
    Node propNode = Node.newString("b");
    Node getPropNode = new Node(Token.GETPROP, objNode, propNode);

    // Unqualified getprop (empty string name)
    Node badObj = new Node(Token.FUNCTION);
    Node badGetProp = new Node(Token.GETPROP, badObj, Node.newString("c"));
    Assert.assertNull(interpreter.getTypeIfRefinable(badGetProp, scope));

    // Qualified getprop not in scope, no node type -> unknown
    Assert.assertSame(registry.getNativeType(UNKNOWN_TYPE), interpreter.getTypeIfRefinable(getPropNode, scope));

    // Qualified getprop with node type set
    getPropNode.setJSType(registry.getNativeType(NUMBER_TYPE));
    Assert.assertSame(registry.getNativeType(NUMBER_TYPE), interpreter.getTypeIfRefinable(getPropNode, scope));

    // Qualified getprop in scope
    scope.inferSlotType("a.b", registry.getNativeType(STRING_TYPE));
    Assert.assertSame(registry.getNativeType(STRING_TYPE), interpreter.getTypeIfRefinable(getPropNode, scope));

    // Other token type
    Node numberNode = Node.newNumber(42);
    Assert.assertNull(interpreter.getTypeIfRefinable(numberNode, scope));
  }

  @Test
  public void testDeclareNameInScope() {
    SimpleFlowScope scope = new SimpleFlowScope();

    Node nameNode = Node.newString(Token.NAME, "varName");
    interpreter.declareNameInScope(scope, nameNode, registry.getNativeType(BOOLEAN_TYPE));
    Assert.assertSame(registry.getNativeType(BOOLEAN_TYPE), scope.getSlot("varName").getType());

    Node objNode = Node.newString(Token.NAME, "a");
    Node propNode = Node.newString("b");
    Node getPropNode = new Node(Token.GETPROP, objNode, propNode);
    interpreter.declareNameInScope(scope, getPropNode, registry.getNativeType(NUMBER_TYPE));
    Assert.assertSame(registry.getNativeType(NUMBER_TYPE), scope.getSlot("a.b").getType());

    getPropNode.setJSType(registry.getNativeType(STRING_TYPE));
    interpreter.declareNameInScope(scope, getPropNode, registry.getNativeType(VOID_TYPE));
    Assert.assertSame(registry.getNativeType(VOID_TYPE), scope.getSlot("a.b").getType());

    Node thisNode = new Node(Token.THIS);
    interpreter.declareNameInScope(scope, thisNode, registry.getNativeType(OBJECT_TYPE));

    try {
      Node invalidNode = Node.newNumber(100);
      interpreter.declareNameInScope(scope, invalidNode, registry.getNativeType(NUMBER_TYPE));
      Assert.fail("Should throw IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
      // expected
    }
  }

  @Test
  public void testGetRestrictedWithoutUndefined() {
    Assert.assertNull(interpreter.getRestrictedWithoutUndefined(null));
    Assert.assertNull(interpreter.getRestrictedWithoutUndefined(registry.getNativeType(VOID_TYPE)));

    Assert.assertSame(registry.getNativeType(NUMBER_TYPE),
        interpreter.getRestrictedWithoutUndefined(registry.getNativeType(NUMBER_TYPE)));
    Assert.assertSame(registry.getNativeType(BOOLEAN_TYPE),
        interpreter.getRestrictedWithoutUndefined(registry.getNativeType(BOOLEAN_TYPE)));
    Assert.assertSame(registry.getNativeType(STRING_TYPE),
        interpreter.getRestrictedWithoutUndefined(registry.getNativeType(STRING_TYPE)));
    Assert.assertSame(registry.getNativeType(NULL_TYPE),
        interpreter.getRestrictedWithoutUndefined(registry.getNativeType(NULL_TYPE)));
    Assert.assertSame(registry.getNativeType(NO_OBJECT_TYPE),
        interpreter.getRestrictedWithoutUndefined(registry.getNativeType(NO_OBJECT_TYPE)));
    Assert.assertSame(registry.getNativeType(NO_TYPE),
        interpreter.getRestrictedWithoutUndefined(registry.getNativeType(NO_TYPE)));
    Assert.assertSame(registry.getNativeType(UNKNOWN_TYPE),
        interpreter.getRestrictedWithoutUndefined(registry.getNativeType(UNKNOWN_TYPE)));

    FunctionType fnType = registry.createFunctionType(registry.getNativeType(VOID_TYPE));
    Assert.assertSame(fnType, interpreter.getRestrictedWithoutUndefined(fnType));

    ObjectType objType = registry.getNativeObjectType(OBJECT_TYPE);
    Assert.assertSame(objType, interpreter.getRestrictedWithoutUndefined(objType));

    ParameterizedType paramType = registry.createParameterizedType(
        registry.getNativeObjectType(com.google.javascript.rhino.jstype.JSTypeNative.ARRAY_TYPE),
        registry.getNativeType(NUMBER_TYPE));
    Assert.assertSame(paramType, interpreter.getRestrictedWithoutUndefined(paramType));

    JSType allType = registry.getNativeType(ALL_TYPE);
    JSType restrictedAll = interpreter.getRestrictedWithoutUndefined(allType);
    Assert.assertNotNull(restrictedAll);
    Assert.assertFalse(restrictedAll.isAllType());

    JSType unionWithVoid = registry.createUnionType(
        registry.getNativeType(STRING_TYPE), registry.getNativeType(VOID_TYPE));
    Assert.assertSame(registry.getNativeType(STRING_TYPE),
        interpreter.getRestrictedWithoutUndefined(unionWithVoid));

    EnumType enumType = registry.createEnumType("MyEnum", null, registry.getNativeType(STRING_TYPE));
    EnumElementType enumElem = enumType.getElementsType();
    Assert.assertSame(enumElem, interpreter.getRestrictedWithoutUndefined(enumElem));

    EnumType enumTypeVoid = registry.createEnumType("VoidEnum", null, registry.getNativeType(VOID_TYPE));
    EnumElementType enumElemVoid = enumTypeVoid.getElementsType();
    Assert.assertNull(interpreter.getRestrictedWithoutUndefined(enumElemVoid));
  }

  @Test
  public void testGetRestrictedWithoutNull() {
    Assert.assertNull(interpreter.getRestrictedWithoutNull(null));
    Assert.assertNull(interpreter.getRestrictedWithoutNull(registry.getNativeType(NULL_TYPE)));

    Assert.assertSame(registry.getNativeType(NUMBER_TYPE),
        interpreter.getRestrictedWithoutNull(registry.getNativeType(NUMBER_TYPE)));
    Assert.assertSame(registry.getNativeType(BOOLEAN_TYPE),
        interpreter.getRestrictedWithoutNull(registry.getNativeType(BOOLEAN_TYPE)));
    Assert.assertSame(registry.getNativeType(STRING_TYPE),
        interpreter.getRestrictedWithoutNull(registry.getNativeType(STRING_TYPE)));
    Assert.assertSame(registry.getNativeType(VOID_TYPE),
        interpreter.getRestrictedWithoutNull(registry.getNativeType(VOID_TYPE)));
    Assert.assertSame(registry.getNativeType(NO_OBJECT_TYPE),
        interpreter.getRestrictedWithoutNull(registry.getNativeType(NO_OBJECT_TYPE)));
    Assert.assertSame(registry.getNativeType(NO_TYPE),
        interpreter.getRestrictedWithoutNull(registry.getNativeType(NO_TYPE)));
    Assert.assertSame(registry.getNativeType(UNKNOWN_TYPE),
        interpreter.getRestrictedWithoutNull(registry.getNativeType(UNKNOWN_TYPE)));

    FunctionType fnType = registry.createFunctionType(registry.getNativeType(VOID_TYPE));
    Assert.assertSame(fnType, interpreter.getRestrictedWithoutNull(fnType));

    ObjectType objType = registry.getNativeObjectType(OBJECT_TYPE);
    Assert.assertSame(objType, interpreter.getRestrictedWithoutNull(objType));

    ParameterizedType paramType = registry.createParameterizedType(
        registry.getNativeObjectType(com.google.javascript.rhino.jstype.JSTypeNative.ARRAY_TYPE),
        registry.getNativeType(NUMBER_TYPE));
    Assert.assertSame(paramType, interpreter.getRestrictedWithoutNull(paramType));

    JSType allType = registry.getNativeType(ALL_TYPE);
    JSType restrictedAll = interpreter.getRestrictedWithoutNull(allType);
    Assert.assertNotNull(restrictedAll);
    Assert.assertFalse(restrictedAll.isAllType());

    JSType unionWithNull = registry.createUnionType(
        registry.getNativeType(STRING_TYPE), registry.getNativeType(NULL_TYPE));
    Assert.assertSame(registry.getNativeType(STRING_TYPE),
        interpreter.getRestrictedWithoutNull(unionWithNull));

    EnumType enumType = registry.createEnumType("MyEnum2", null, registry.getNativeType(STRING_TYPE));
    EnumElementType enumElem = enumType.getElementsType();
    Assert.assertSame(enumElem, interpreter.getRestrictedWithoutNull(enumElem));

    EnumType enumTypeNull = registry.createEnumType("NullEnum", null, registry.getNativeType(NULL_TYPE));
    EnumElementType enumElemNull = enumTypeNull.getElementsType();
    Assert.assertNull(interpreter.getRestrictedWithoutNull(enumElemNull));
  }

  @Test
  public void testGetRestrictedByTypeOfResultNullTypeInput() {
    Assert.assertNull(interpreter.getRestrictedByTypeOfResult(null, "number", false));

    Assert.assertSame(registry.getNativeType(NUMBER_TYPE),
        interpreter.getRestrictedByTypeOfResult(null, "number", true));
    Assert.assertSame(registry.getNativeType(BOOLEAN_TYPE),
        interpreter.getRestrictedByTypeOfResult(null, "boolean", true));
    Assert.assertSame(registry.getNativeType(STRING_TYPE),
        interpreter.getRestrictedByTypeOfResult(null, "string", true));
    Assert.assertSame(registry.getNativeType(VOID_TYPE),
        interpreter.getRestrictedByTypeOfResult(null, "undefined", true));
    Assert.assertSame(registry.getNativeType(U2U_CONSTRUCTOR_TYPE),
        interpreter.getRestrictedByTypeOfResult(null, "function", true));
    Assert.assertSame(registry.getNativeType(CHECKED_UNKNOWN_TYPE),
        interpreter.getRestrictedByTypeOfResult(null, "unknown_value", true));
  }

  @Test
  public void testGetRestrictedByTypeOfResultMatches() {
    Assert.assertSame(registry.getNativeType(NUMBER_TYPE),
        interpreter.getRestrictedByTypeOfResult(registry.getNativeType(NUMBER_TYPE), "number", true));
    Assert.assertNull(interpreter.getRestrictedByTypeOfResult(registry.getNativeType(NUMBER_TYPE), "number", false));

    Assert.assertSame(registry.getNativeType(BOOLEAN_TYPE),
        interpreter.getRestrictedByTypeOfResult(registry.getNativeType(BOOLEAN_TYPE), "boolean", true));
    Assert.assertNull(interpreter.getRestrictedByTypeOfResult(registry.getNativeType(BOOLEAN_TYPE), "boolean", false));

    Assert.assertSame(registry.getNativeType(STRING_TYPE),
        interpreter.getRestrictedByTypeOfResult(registry.getNativeType(STRING_TYPE), "string", true));
    Assert.assertNull(interpreter.getRestrictedByTypeOfResult(registry.getNativeType(STRING_TYPE), "string", false));

    Assert.assertSame(registry.getNativeType(VOID_TYPE),
        interpreter.getRestrictedByTypeOfResult(registry.getNativeType(VOID_TYPE), "undefined", true));
    Assert.assertNull(interpreter.getRestrictedByTypeOfResult(registry.getNativeType(VOID_TYPE), "undefined", false));

    Assert.assertSame(registry.getNativeType(NULL_TYPE),
        interpreter.getRestrictedByTypeOfResult(registry.getNativeType(NULL_TYPE), "object", true));
    Assert.assertNull(interpreter.getRestrictedByTypeOfResult(registry.getNativeType(NULL_TYPE), "object", false));

    FunctionType fnType = registry.createFunctionType(registry.getNativeType(VOID_TYPE));
    Assert.assertSame(fnType, interpreter.getRestrictedByTypeOfResult(fnType, "function", true));
    Assert.assertNull(interpreter.getRestrictedByTypeOfResult(fnType, "function", false));

    ObjectType objType = registry.getNativeObjectType(OBJECT_TYPE);
    Assert.assertSame(objType, interpreter.getRestrictedByTypeOfResult(objType, "object", true));
    Assert.assertNull(interpreter.getRestrictedByTypeOfResult(objType, "object", false));

    Assert.assertSame(registry.getNativeType(U2U_CONSTRUCTOR_TYPE),
        interpreter.getRestrictedByTypeOfResult(objType, "function", true));
    Assert.assertNull(interpreter.getRestrictedByTypeOfResult(objType, "function", false));

    Assert.assertSame(registry.getNativeType(NO_OBJECT_TYPE),
        interpreter.getRestrictedByTypeOfResult(registry.getNativeType(NO_OBJECT_TYPE), "object", true));
    Assert.assertSame(registry.getNativeType(NO_OBJECT_TYPE),
        interpreter.getRestrictedByTypeOfResult(registry.getNativeType(NO_OBJECT_TYPE), "function", true));
    Assert.assertNull(interpreter.getRestrictedByTypeOfResult(registry.getNativeType(NO_OBJECT_TYPE), "number", true));

    Assert.assertSame(registry.getNativeType(NO_TYPE),
        interpreter.getRestrictedByTypeOfResult(registry.getNativeType(NO_TYPE), "number", true));

    Assert.assertSame(registry.getNativeType(NUMBER_TYPE),
        interpreter.getRestrictedByTypeOfResult(registry.getNativeType(ALL_TYPE), "number", true));
    Assert.assertSame(registry.getNativeType(ALL_TYPE),
        interpreter.getRestrictedByTypeOfResult(registry.getNativeType(ALL_TYPE), "unknown_val", true));
    Assert.assertSame(registry.getNativeType(ALL_TYPE),
        interpreter.getRestrictedByTypeOfResult(registry.getNativeType(ALL_TYPE), "number", false));

    Assert.assertSame(registry.getNativeType(NUMBER_TYPE),
        interpreter.getRestrictedByTypeOfResult(registry.getNativeType(UNKNOWN_TYPE), "number", true));
    Assert.assertSame(registry.getNativeType(CHECKED_UNKNOWN_TYPE),
        interpreter.getRestrictedByTypeOfResult(registry.getNativeType(UNKNOWN_TYPE), "unknown_val", true));
    Assert.assertSame(registry.getNativeType(CHECKED_UNKNOWN_TYPE),
        interpreter.getRestrictedByTypeOfResult(registry.getNativeType(UNKNOWN_TYPE), "number", false));

    ParameterizedType paramType = registry.createParameterizedType(
        registry.getNativeObjectType(com.google.javascript.rhino.jstype.JSTypeNative.ARRAY_TYPE),
        registry.getNativeType(NUMBER_TYPE));
    Assert.assertSame(paramType, interpreter.getRestrictedByTypeOfResult(paramType, "object", true));
    Assert.assertNull(interpreter.getRestrictedByTypeOfResult(paramType, "number", true));

    EnumType enumType = registry.createEnumType("Enum3", null, registry.getNativeType(NUMBER_TYPE));
    EnumElementType enumElem = enumType.getElementsType();
    Assert.assertSame(enumElem, interpreter.getRestrictedByTypeOfResult(enumElem, "number", true));
    Assert.assertNull(interpreter.getRestrictedByTypeOfResult(enumElem, "string", true));

    JSType union = registry.createUnionType(
        registry.getNativeType(NUMBER_TYPE), registry.getNativeType(STRING_TYPE));
    Assert.assertSame(registry.getNativeType(NUMBER_TYPE),
        interpreter.getRestrictedByTypeOfResult(union, "number", true));
    Assert.assertSame(registry.getNativeType(STRING_TYPE),
        interpreter.getRestrictedByTypeOfResult(union, "number", false));
  }

  @Test
  public void testAbstractVisitorsDirectCases() {
    ChainableReverseAbstractInterpreter.RestrictByTrueTypeOfResultVisitor trueVisitor =
        interpreter.new RestrictByTrueTypeOfResultVisitor() {
          @Override
          protected JSType caseTopType(JSType topType) {
            return topType;
          }
        };

    Assert.assertNull(trueVisitor.caseNoObjectType());
    Assert.assertNull(trueVisitor.caseBooleanType());
    Assert.assertNull(trueVisitor.caseFunctionType(registry.createFunctionType(registry.getNativeType(VOID_TYPE))));
    Assert.assertNull(trueVisitor.caseNullType());
    Assert.assertNull(trueVisitor.caseNumberType());
    Assert.assertNull(trueVisitor.caseObjectType(registry.getNativeObjectType(OBJECT_TYPE)));
    Assert.assertNull(trueVisitor.caseStringType());
    Assert.assertNull(trueVisitor.caseVoidType());
    Assert.assertSame(registry.getNativeType(NO_TYPE), trueVisitor.caseNoType());
    Assert.assertSame(registry.getNativeType(ALL_TYPE), trueVisitor.caseAllType());
    Assert.assertSame(registry.getNativeType(CHECKED_UNKNOWN_TYPE), trueVisitor.caseUnknownType());

    ParameterizedType paramType = registry.createParameterizedType(
        registry.getNativeObjectType(com.google.javascript.rhino.jstype.JSTypeNative.ARRAY_TYPE),
        registry.getNativeType(NUMBER_TYPE));
    Assert.assertNull(trueVisitor.caseParameterizedType(paramType));

    ChainableReverseAbstractInterpreter.RestrictByFalseTypeOfResultVisitor falseVisitor =
        interpreter.new RestrictByFalseTypeOfResultVisitor() {};

    Assert.assertSame(registry.getNativeType(NO_OBJECT_TYPE), falseVisitor.caseNoObjectType());
    Assert.assertSame(registry.getNativeType(BOOLEAN_TYPE), falseVisitor.caseBooleanType());
    FunctionType fnType = registry.createFunctionType(registry.getNativeType(VOID_TYPE));
    Assert.assertSame(fnType, falseVisitor.caseFunctionType(fnType));
    Assert.assertSame(registry.getNativeType(NULL_TYPE), falseVisitor.caseNullType());
    Assert.assertSame(registry.getNativeType(NUMBER_TYPE), falseVisitor.caseNumberType());
    ObjectType objType = registry.getNativeObjectType(OBJECT_TYPE);
    Assert.assertSame(objType, falseVisitor.caseObjectType(objType));
    Assert.assertSame(registry.getNativeType(STRING_TYPE), falseVisitor.caseStringType());
    Assert.assertSame(registry.getNativeType(VOID_TYPE), falseVisitor.caseVoidType());
    Assert.assertSame(registry.getNativeType(NO_TYPE), falseVisitor.caseNoType());
    Assert.assertSame(registry.getNativeType(ALL_TYPE), falseVisitor.caseAllType());
    Assert.assertSame(registry.getNativeType(CHECKED_UNKNOWN_TYPE), falseVisitor.caseUnknownType());
    Assert.assertSame(paramType, falseVisitor.caseParameterizedType(paramType));
  }
}