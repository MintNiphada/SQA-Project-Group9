package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.testing.TestErrorReporter;
import org.junit.Before;
import org.junit.Test;

public class ClosureReverseAbstractInterpreterTest {

  private JSTypeRegistry registry;
  private Scope scope;
  private FlowScope blindScope;
  private ClosureReverseAbstractInterpreter rai;

  private JSType allType;
  private JSType unknownType;
  private JSType numberType;
  private JSType stringType;
  private JSType booleanType;
  private JSType nullType;
  private JSType voidType;
  private ObjectType objectType;
  private ObjectType arrayType;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(new TestErrorReporter(null, null));
    CodingConvention convention = new GoogleCodingConvention();
    rai = new ClosureReverseAbstractInterpreter(convention, registry);

    allType = registry.getNativeType(JSTypeNative.ALL_TYPE);
    unknownType = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);
    voidType = registry.getNativeType(JSTypeNative.VOID_TYPE);
    objectType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    arrayType = registry.getNativeObjectType(JSTypeNative.ARRAY_TYPE);

    Node root = new Node(Token.BLOCK);
    scope = new Scope(root, (Scope) null);
    blindScope = LinkedFlowScope.createEntryLattice(scope);
  }

  private FlowScope createScopeWithVar(String name, JSType type) {
    Node n = Node.newString(Token.NAME, name);
    scope.declare(name, n, type, null);
    FlowScope flowScope = LinkedFlowScope.createEntryLattice(scope);
    flowScope.inferSlotType(name, type);
    return flowScope;
  }

  private Node createCall(String methodName, String paramName) {
    Node callee = new Node(Token.GETPROP,
        Node.newString(Token.NAME, "goog"),
        Node.newString(Token.STRING, methodName));
    Node param = Node.newString(Token.NAME, paramName);
    return new Node(Token.CALL, callee, param);
  }

  @Test
  public void testIsDef() {
    JSType union = registry.createUnionType(stringType, voidType);
    FlowScope inputScope = createScopeWithVar("a", union);
    Node call = createCall("isDef", "a");

    FlowScope trueScope = rai.getPreciserScopeKnowingConditionOutcome(call, inputScope, true);
    assertEquals(stringType, trueScope.getSlot("a").getType());

    FlowScope falseScope = rai.getPreciserScopeKnowingConditionOutcome(call, inputScope, false);
    assertSame(inputScope, falseScope);
  }

  @Test
  public void testIsNull() {
    JSType union = registry.createUnionType(stringType, nullType);
    FlowScope inputScope = createScopeWithVar("a", union);
    Node call = createCall("isNull", "a");

    FlowScope trueScope = rai.getPreciserScopeKnowingConditionOutcome(call, inputScope, true);
    assertEquals(nullType, trueScope.getSlot("a").getType());

    FlowScope falseScope = rai.getPreciserScopeKnowingConditionOutcome(call, inputScope, false);
    assertEquals(stringType, falseScope.getSlot("a").getType());
  }

  @Test
  public void testIsDefAndNotNull() {
    JSType union = registry.createUnionType(stringType, nullType, voidType);
    FlowScope inputScope = createScopeWithVar("a", union);
    Node call = createCall("isDefAndNotNull", "a");

    FlowScope trueScope = rai.getPreciserScopeKnowingConditionOutcome(call, inputScope, true);
    assertEquals(stringType, trueScope.getSlot("a").getType());

    FlowScope falseScope = rai.getPreciserScopeKnowingConditionOutcome(call, inputScope, false);
    assertSame(inputScope, falseScope);
  }

  @Test
  public void testIsString() {
    JSType union = registry.createUnionType(stringType, numberType);
    FlowScope inputScope = createScopeWithVar("a", union);
    Node call = createCall("isString", "a");

    FlowScope trueScope = rai.getPreciserScopeKnowingConditionOutcome(call, inputScope, true);
    assertEquals(stringType, trueScope.getSlot("a").getType());

    FlowScope falseScope = rai.getPreciserScopeKnowingConditionOutcome(call, inputScope, false);
    assertEquals(numberType, falseScope.getSlot("a").getType());
  }

  @Test
  public void testIsBoolean() {
    JSType union = registry.createUnionType(booleanType, numberType);
    FlowScope inputScope = createScopeWithVar("a", union);
    Node call = createCall("isBoolean", "a");

    FlowScope trueScope = rai.getPreciserScopeKnowingConditionOutcome(call, inputScope, true);
    assertEquals(booleanType, trueScope.getSlot("a").getType());

    FlowScope falseScope = rai.getPreciserScopeKnowingConditionOutcome(call, inputScope, false);
    assertEquals(numberType, falseScope.getSlot("a").getType());
  }

  @Test
  public void testIsNumber() {
    JSType union = registry.createUnionType(stringType, numberType);
    FlowScope inputScope = createScopeWithVar("a", union);
    Node call = createCall("isNumber", "a");

    FlowScope trueScope = rai.getPreciserScopeKnowingConditionOutcome(call, inputScope, true);
    assertEquals(numberType, trueScope.getSlot("a").getType());

    FlowScope falseScope = rai.getPreciserScopeKnowingConditionOutcome(call, inputScope, false);
    assertEquals(stringType, falseScope.getSlot("a").getType());
  }

  @Test
  public void testIsFunction() {
    FunctionType functionType = registry.createFunctionType(stringType);
    JSType union = registry.createUnionType(functionType, numberType);
    FlowScope inputScope = createScopeWithVar("a", union);
    Node call = createCall("isFunction", "a");

    FlowScope trueScope = rai.getPreciserScopeKnowingConditionOutcome(call, inputScope, true);
    assertEquals(functionType, trueScope.getSlot("a").getType());

    FlowScope falseScope = rai.getPreciserScopeKnowingConditionOutcome(call, inputScope, false);
    assertEquals(numberType, falseScope.getSlot("a").getType());
  }

  @Test
  public void testIsArray() {
    JSType union = registry.createUnionType(arrayType, stringType);
    FlowScope inputScope = createScopeWithVar("a", union);
    Node call = createCall("isArray", "a");

    FlowScope trueScope = rai.getPreciserScopeKnowingConditionOutcome(call, inputScope, true);
    assertEquals(arrayType, trueScope.getSlot("a").getType());

    FlowScope falseScope = rai.getPreciserScopeKnowingConditionOutcome(call, inputScope, false);
    assertEquals(stringType, falseScope.getSlot("a").getType());
  }

  @Test
  public void testIsArrayOnTopTypeAndObjectType() {
    FlowScope topScope = createScopeWithVar("top", allType);
    Node callTop = createCall("isArray", "top");
    FlowScope trueTopScope = rai.getPreciserScopeKnowingConditionOutcome(callTop, topScope, true);
    assertEquals(allType, trueTopScope.getSlot("top").getType());

    FlowScope objScope = createScopeWithVar("obj", objectType);
    Node callObj = createCall("isArray", "obj");
    FlowScope trueObjScope = rai.getPreciserScopeKnowingConditionOutcome(callObj, objScope, true);
    assertEquals(arrayType, trueObjScope.getSlot("obj").getType());

    FlowScope falseObjScope = rai.getPreciserScopeKnowingConditionOutcome(callObj, objScope, false);
    assertEquals(objectType, falseObjScope.getSlot("obj").getType());
  }

  @Test
  public void testIsObject() {
    FunctionType fnType = registry.createFunctionType(stringType);
    JSType union = registry.createUnionType(objectType, fnType, numberType);
    FlowScope inputScope = createScopeWithVar("a", union);
    Node call = createCall("isObject", "a");

    FlowScope trueScope = rai.getPreciserScopeKnowingConditionOutcome(call, inputScope, true);
    assertEquals(registry.createUnionType(objectType, fnType), trueScope.getSlot("a").getType());

    FlowScope falseScope = rai.getPreciserScopeKnowingConditionOutcome(call, inputScope, false);
    assertEquals(numberType, falseScope.getSlot("a").getType());
  }

  @Test
  public void testIsObjectOnTopType() {
    FlowScope topScope = createScopeWithVar("top", allType);
    Node call = createCall("isObject", "top");

    FlowScope trueScope = rai.getPreciserScopeKnowingConditionOutcome(call, topScope, true);
    assertEquals(registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE), trueScope.getSlot("top").getType());
  }

  @Test
  public void testConditionsNotMatchingPatterns() {
    FlowScope inputScope = createScopeWithVar("a", stringType);

    // Non-call condition
    Node nonCall = Node.newString(Token.NAME, "a");
    FlowScope result1 = rai.getPreciserScopeKnowingConditionOutcome(nonCall, inputScope, true);
    assertSame(inputScope, result1);

    // Call condition with wrong child count
    Node callNoArgs = new Node(Token.CALL, new Node(Token.GETPROP,
        Node.newString(Token.NAME, "goog"),
        Node.newString(Token.STRING, "isString")));
    FlowScope result2 = rai.getPreciserScopeKnowingConditionOutcome(callNoArgs, inputScope, true);
    assertSame(inputScope, result2);

    // Callee is not GETPROP
    Node callDirectName = new Node(Token.CALL,
        Node.newString(Token.NAME, "isString"),
        Node.newString(Token.NAME, "a"));
    FlowScope result3 = rai.getPreciserScopeKnowingConditionOutcome(callDirectName, inputScope, true);
    assertSame(inputScope, result3);

    // Left child is not 'goog'
    Node callOtherNamespace = new Node(Token.CALL,
        new Node(Token.GETPROP, Node.newString(Token.NAME, "other"), Node.newString(Token.STRING, "isString")),
        Node.newString(Token.NAME, "a"));
    FlowScope result4 = rai.getPreciserScopeKnowingConditionOutcome(callOtherNamespace, inputScope, true);
    assertSame(inputScope, result4);

    // Left child is not NAME
    Node callNotNameNamespace = new Node(Token.CALL,
        new Node(Token.GETPROP, Node.newNumber(42), Node.newString(Token.STRING, "isString")),
        Node.newString(Token.NAME, "a"));
    FlowScope result5 = rai.getPreciserScopeKnowingConditionOutcome(callNotNameNamespace, inputScope, true);
    assertSame(inputScope, result5);

    // Right child is not STRING
    Node callNotStringProp = new Node(Token.CALL,
        new Node(Token.GETPROP, Node.newString(Token.NAME, "goog"), Node.newNumber(1)),
        Node.newString(Token.NAME, "a"));
    FlowScope result6 = rai.getPreciserScopeKnowingConditionOutcome(callNotStringProp, inputScope, true);
    assertSame(inputScope, result6);

    // Right child string is unrecognized method
    Node callUnknownMethod = createCall("unknownMethod", "a");
    FlowScope result7 = rai.getPreciserScopeKnowingConditionOutcome(callUnknownMethod, inputScope, true);
    assertSame(inputScope, result7);

    // Parameter is not in scope (paramType is null)
    Node callUnknownParam = createCall("isString", "nonExistentVar");
    FlowScope result8 = rai.getPreciserScopeKnowingConditionOutcome(callUnknownParam, inputScope, true);
    assertSame(inputScope, result8);
  }
}