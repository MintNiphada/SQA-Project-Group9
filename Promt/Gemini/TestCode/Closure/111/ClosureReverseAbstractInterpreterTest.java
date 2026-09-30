package com.google.javascript.jscomp.type;

import static com.google.javascript.rhino.jstype.JSTypeNative.ALL_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.ARRAY_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.BOOLEAN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.CHECKED_UNKNOWN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NO_OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NO_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NULL_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NULL_VOID;
import static com.google.javascript.rhino.jstype.JSTypeNative.NUMBER_STRING_BOOLEAN;
import static com.google.javascript.rhino.jstype.JSTypeNative.NUMBER_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.STRING_OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.STRING_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.UNKNOWN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.VOID_TYPE;

import com.google.javascript.jscomp.CodingConventions;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.StaticSlot;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class ClosureReverseAbstractInterpreterTest {

  private JSTypeRegistry registry;
  private ClosureReverseAbstractInterpreter interpreter;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(new SimpleErrorReporter());
    interpreter = new ClosureReverseAbstractInterpreter(
        CodingConventions.getDefault(), registry);
  }

  private JSType getNative(JSTypeNative type) {
    return registry.getNativeType(type);
  }

  private FlowScope createFlowScope(final Map<String, JSType> slotTypes) {
    InvocationHandler handler = new InvocationHandler() {
      private final Map<String, JSType> slots = new HashMap<String, JSType>(slotTypes);

      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String name = method.getName();
        if ("getSlot".equals(name) || "findSlot".equals(name) || "getOwnSlot".equals(name)) {
          final String slotName = (String) args[0];
          if (!slots.containsKey(slotName)) {
            return null;
          }
          final JSType type = slots.get(slotName);
          return Proxy.newProxyInstance(
              StaticSlot.class.getClassLoader(),
              new Class<?>[] { StaticSlot.class },
              new InvocationHandler() {
                @Override
                public Object invoke(Object p, Method m, Object[] a) {
                  if ("getType".equals(m.getName())) {
                    return type;
                  }
                  if ("getName".equals(m.getName())) {
                    return slotName;
                  }
                  return null;
                }
              });
        } else if ("createChildFlowScope".equals(name)) {
          return createFlowScope(new HashMap<String, JSType>(slots));
        } else if ("inferSlotType".equals(name)) {
          slots.put((String) args[0], (JSType) args[1]);
          return null;
        } else if ("inferQualifiedSlot".equals(name)) {
          slots.put((String) args[1], (JSType) args[3]);
          return null;
        } else if ("getTypeRegistry".equals(name)) {
          return registry;
        } else if ("toString".equals(name)) {
          return "FlowScopeProxy" + slots;
        }
        return null;
      }
    };

    return (FlowScope) Proxy.newProxyInstance(
        FlowScope.class.getClassLoader(),
        new Class<?>[] { FlowScope.class },
        handler);
  }

  private JSType getRefinedType(Node callNode, JSType inputType, boolean outcome) {
    Map<String, JSType> initialSlots = new HashMap<String, JSType>();
    if (inputType != null) {
      initialSlots.put("x", inputType);
    }
    FlowScope blindScope = createFlowScope(initialSlots);
    FlowScope informedScope = interpreter.getPreciserScopeKnowingConditionOutcome(
        callNode, blindScope, outcome);

    if (informedScope == null) {
      return null;
    }
    StaticSlot<JSType> slot = informedScope.getSlot("x");
    return slot != null ? slot.getType() : null;
  }

  private Node createGoogCall(String fnName, String varName) {
    return IR.call(IR.getprop(IR.name("goog"), IR.string(fnName)), IR.name(varName));
  }

  @Test
  public void testIsDef() {
    Node call = createGoogCall("isDef", "x");
    JSType union = registry.createUnionType(getNative(STRING_TYPE), getNative(VOID_TYPE));

    JSType trueOutcome = getRefinedType(call, union, true);
    Assert.assertEquals(getNative(STRING_TYPE), trueOutcome);

    JSType falseOutcome = getRefinedType(call, union, false);
    Assert.assertEquals(getNative(VOID_TYPE), falseOutcome);

    JSType nullInputTrue = getRefinedType(call, null, true);
    Assert.assertNull(nullInputTrue);

    JSType nullInputFalse = getRefinedType(call, null, false);
    Assert.assertNull(nullInputFalse);
  }

  @Test
  public void testIsNull() {
    Node call = createGoogCall("isNull", "x");
    JSType union = registry.createUnionType(getNative(NUMBER_TYPE), getNative(NULL_TYPE));

    JSType trueOutcome = getRefinedType(call, union, true);
    Assert.assertEquals(getNative(NULL_TYPE), trueOutcome);

    JSType falseOutcome = getRefinedType(call, union, false);
    Assert.assertEquals(getNative(NUMBER_TYPE), falseOutcome);

    JSType nullInputTrue = getRefinedType(call, null, true);
    Assert.assertNull(nullInputTrue);

    JSType nullInputFalse = getRefinedType(call, null, false);
    Assert.assertNull(nullInputFalse);
  }

  @Test
  public void testIsDefAndNotNull() {
    Node call = createGoogCall("isDefAndNotNull", "x");
    JSType union = registry.createUnionType(
        getNative(BOOLEAN_TYPE), getNative(NULL_TYPE), getNative(VOID_TYPE));

    JSType trueOutcome = getRefinedType(call, union, true);
    Assert.assertEquals(getNative(BOOLEAN_TYPE), trueOutcome);

    JSType falseOutcome = getRefinedType(call, union, false);
    Assert.assertEquals(getNative(NULL_VOID), falseOutcome);

    JSType nullInputTrue = getRefinedType(call, null, true);
    Assert.assertNull(nullInputTrue);

    JSType nullInputFalse = getRefinedType(call, null, false);
    Assert.assertNull(nullInputFalse);
  }

  @Test
  public void testIsString() {
    Node call = createGoogCall("isString", "x");
    JSType union = registry.createUnionType(getNative(STRING_TYPE), getNative(NUMBER_TYPE));

    JSType trueOutcome = getRefinedType(call, union, true);
    Assert.assertEquals(getNative(STRING_TYPE), trueOutcome);

    JSType falseOutcome = getRefinedType(call, union, false);
    Assert.assertEquals(getNative(NUMBER_TYPE), falseOutcome);
  }

  @Test
  public void testIsBoolean() {
    Node call = createGoogCall("isBoolean", "x");
    JSType union = registry.createUnionType(getNative(BOOLEAN_TYPE), getNative(NUMBER_TYPE));

    JSType trueOutcome = getRefinedType(call, union, true);
    Assert.assertEquals(getNative(BOOLEAN_TYPE), trueOutcome);

    JSType falseOutcome = getRefinedType(call, union, false);
    Assert.assertEquals(getNative(NUMBER_TYPE), falseOutcome);
  }

  @Test
  public void testIsNumber() {
    Node call = createGoogCall("isNumber", "x");
    JSType union = registry.createUnionType(getNative(NUMBER_TYPE), getNative(STRING_TYPE));

    JSType trueOutcome = getRefinedType(call, union, true);
    Assert.assertEquals(getNative(NUMBER_TYPE), trueOutcome);

    JSType falseOutcome = getRefinedType(call, union, false);
    Assert.assertEquals(getNative(STRING_TYPE), falseOutcome);
  }

  @Test
  public void testIsFunction() {
    Node call = createGoogCall("isFunction", "x");
    FunctionType fnType = registry.createFunctionType(getNative(VOID_TYPE));
    JSType union = registry.createUnionType(fnType, getNative(STRING_TYPE));

    JSType trueOutcome = getRefinedType(call, union, true);
    Assert.assertEquals(fnType, trueOutcome);

    JSType falseOutcome = getRefinedType(call, union, false);
    Assert.assertEquals(getNative(STRING_TYPE), falseOutcome);
  }

  @Test
  public void testIsArray() {
    Node call = createGoogCall("isArray", "x");

    JSType nullInputTrue = getRefinedType(call, null, true);
    Assert.assertEquals(getNative(ARRAY_TYPE), nullInputTrue);

    JSType nullInputFalse = getRefinedType(call, null, false);
    Assert.assertNull(nullInputFalse);

    JSType topType = getNative(ALL_TYPE);
    JSType topTrue = getRefinedType(call, topType, true);
    Assert.assertEquals(getNative(ARRAY_TYPE), topTrue);

    JSType arrayType = getNative(ARRAY_TYPE);
    JSType objectType = getNative(OBJECT_TYPE);
    JSType union = registry.createUnionType(arrayType, getNative(NUMBER_TYPE));

    JSType trueOutcome = getRefinedType(call, union, true);
    Assert.assertEquals(arrayType, trueOutcome);

    JSType falseOutcome = getRefinedType(call, union, false);
    Assert.assertEquals(getNative(NUMBER_TYPE), falseOutcome);

    JSType nonArrayObj = getNative(STRING_OBJECT_TYPE);
    JSType nonArrayTrue = getRefinedType(call, nonArrayObj, true);
    Assert.assertNull(nonArrayTrue);

    JSType nonArrayFalse = getRefinedType(call, nonArrayObj, false);
    Assert.assertEquals(nonArrayObj, nonArrayFalse);

    JSType arrayObjTrue = getRefinedType(call, objectType, true);
    Assert.assertEquals(arrayType, arrayObjTrue);

    JSType arrayObjFalse = getRefinedType(call, arrayType, false);
    Assert.assertNull(arrayObjFalse);
  }

  @Test
  public void testIsObject() {
    Node call = createGoogCall("isObject", "x");

    JSType nullInputTrue = getRefinedType(call, null, true);
    Assert.assertEquals(getNative(OBJECT_TYPE), nullInputTrue);

    JSType nullInputFalse = getRefinedType(call, null, false);
    Assert.assertNull(nullInputFalse);

    JSType topType = getNative(UNKNOWN_TYPE);
    JSType topTrue = getRefinedType(call, topType, true);
    Assert.assertEquals(getNative(CHECKED_UNKNOWN_TYPE), topTrue);

    JSType allType = getNative(ALL_TYPE);
    JSType allFalse = getRefinedType(call, allType, false);
    JSType expectedAllFalse = registry.createUnionType(
        getNative(NUMBER_STRING_BOOLEAN), getNative(NULL_VOID));
    Assert.assertEquals(expectedAllFalse, allFalse);

    ObjectType objType = (ObjectType) getNative(OBJECT_TYPE);
    JSType objTrue = getRefinedType(call, objType, true);
    Assert.assertEquals(objType, objTrue);

    JSType objFalse = getRefinedType(call, objType, false);
    Assert.assertNull(objFalse);

    FunctionType fnType = registry.createFunctionType(getNative(VOID_TYPE));
    JSType fnTrue = getRefinedType(call, fnType, true);
    Assert.assertEquals(fnType, fnTrue);

    JSType fnFalse = getRefinedType(call, fnType, false);
    Assert.assertNull(fnFalse);
  }

  @Test
  public void testNonMatchingConditions() {
    FlowScope scope = createFlowScope(new HashMap<String, JSType>());

    Node notCall = IR.name("x");
    FlowScope result1 = interpreter.getPreciserScopeKnowingConditionOutcome(notCall, scope, true);
    Assert.assertNotNull(result1);

    Node zeroChildCall = IR.call(IR.name("goog"));
    zeroChildCall.removeChildren();
    FlowScope result2 = interpreter.getPreciserScopeKnowingConditionOutcome(zeroChildCall, scope, true);
    Assert.assertNotNull(result2);

    Node threeChildCall = IR.call(
        IR.getprop(IR.name("goog"), IR.string("isDef")),
        IR.name("x"),
        IR.name("y"));
    FlowScope result3 = interpreter.getPreciserScopeKnowingConditionOutcome(threeChildCall, scope, true);
    Assert.assertNotNull(result3);

    Node directCall = IR.call(IR.name("isDef"), IR.name("x"));
    FlowScope result4 = interpreter.getPreciserScopeKnowingConditionOutcome(directCall, scope, true);
    Assert.assertNotNull(result4);

    Node nonQNameParam = IR.call(
        IR.getprop(IR.name("goog"), IR.string("isDef")),
        IR.add(IR.number(1), IR.number(2)));
    FlowScope result5 = interpreter.getPreciserScopeKnowingConditionOutcome(nonQNameParam, scope, true);
    Assert.assertNotNull(result5);

    Node notGoogProp = IR.call(
        IR.getprop(IR.name("other"), IR.string("isDef")),
        IR.name("x"));
    FlowScope result6 = interpreter.getPreciserScopeKnowingConditionOutcome(notGoogProp, scope, true);
    Assert.assertNotNull(result6);

    Node nonNameLeft = IR.call(
        IR.getprop(IR.getprop(IR.name("a"), IR.string("b")), IR.string("isDef")),
        IR.name("x"));
    FlowScope result7 = interpreter.getPreciserScopeKnowingConditionOutcome(nonNameLeft, scope, true);
    Assert.assertNotNull(result7);

    Node unknownRestricter = IR.call(
        IR.getprop(IR.name("goog"), IR.string("unknownHelper")),
        IR.name("x"));
    FlowScope result8 = interpreter.getPreciserScopeKnowingConditionOutcome(unknownRestricter, scope, true);
    Assert.assertNotNull(result8);
  }

  @Test
  public void testRestrictionReturningNullLeavesScopeUnchanged() {
    Map<String, JSType> initialSlots = new HashMap<String, JSType>();
    initialSlots.put("x", getNative(NUMBER_TYPE));
    FlowScope blindScope = createFlowScope(initialSlots);

    Node call = createGoogCall("isArray", "x");
    FlowScope informedScope = interpreter.getPreciserScopeKnowingConditionOutcome(
        call, blindScope, true);

    Assert.assertSame(blindScope, informedScope);
  }
}