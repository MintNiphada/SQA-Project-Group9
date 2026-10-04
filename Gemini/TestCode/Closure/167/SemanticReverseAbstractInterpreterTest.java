package com.google.javascript.jscomp.type;

import com.google.javascript.jscomp.CodingConvention;
import com.google.javascript.jscomp.GoogleCodingConvention;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.StaticSlot;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class SemanticReverseAbstractInterpreterTest {

  private JSTypeRegistry registry;
  private CodingConvention convention;
  private SemanticReverseAbstractInterpreter interpreter;
  private DummyFlowScope blindScope;

  private static class DummyFlowScope implements FlowScope {
    private final Map<String, JSType> slots = new HashMap<>();
    private final DummyFlowScope parent;
    private String lastInferSlot = null;

    DummyFlowScope() {
      this.parent = null;
    }

    DummyFlowScope(DummyFlowScope parent) {
      this.parent = parent;
      if (parent != null) {
        this.slots.putAll(parent.slots);
      }
    }

    @Override
    public FlowScope createChildFlowScope() {
      return new DummyFlowScope(this);
    }

    @Override
    public FlowScope optimize() {
      return this;
    }

    @Override
    public void inferSlotType(String symbol, JSType type) {
      this.slots.put(symbol, type);
      this.lastInferSlot = symbol;
    }

    @Override
    public void inferQualifiedSlot(Node node, String symbol, JSType bottomType, JSType inferredType) {
      this.slots.put(symbol, inferredType);
      this.lastInferSlot = symbol;
    }

    @Override
    public JSType getTypeOfThis() {
      return null;
    }

    @Override
    public Node getRootNode() {
      return null;
    }

    @Override
    public StaticSlot<JSType> getSlot(String name) {
      if (slots.containsKey(name)) {
        return new SimpleSlot(name, slots.get(name));
      }
      return null;
    }

    @Override
    public StaticSlot<JSType> getOwnSlot(String name) {
      return getSlot(name);
    }

    @Override
    public StaticSlot<JSType> findUniqueRefinedSlot(FlowScope blindScope) {
      if (lastInferSlot != null) {
        return getSlot(lastInferSlot);
      }
      return null;
    }

    @Override
    public void completeScope(com.google.javascript.rhino.jstype.StaticScope<JSType> staticScope) {
    }
  }

  private static class SimpleSlot implements StaticSlot<JSType> {
    private final String name;
    private final JSType type;

    SimpleSlot(String name, JSType type) {
      this.name = name;
      this.type = type;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public JSType getType() {
      return type;
    }

    @Override
    public boolean isTypeInferred() {
      return true;
    }

    @Override
    public StaticSlot<JSType> getDeclaration() {
      return this;
    }

    @Override
    public JSType getJSType() {
      return type;
    }
  }

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(null);
    convention = new GoogleCodingConvention();
    interpreter = new SemanticReverseAbstractInterpreter(convention, registry);
    blindScope = new DummyFlowScope();
  }

  @Test
  public void testTypeOfEqOutcomeTrueAndFalse() {
    JSType union = registry.createUnionType(JSTypeNative.STRING_TYPE, JSTypeNative.NUMBER_TYPE);
    blindScope.inferSlotType("x", union);

    Node typeOf = new Node(Token.TYPEOF, Node.newString(Token.NAME, "x"));
    Node str = Node.newString("string");
    Node eq = new Node(Token.EQ, typeOf, str);

    FlowScope trueScope = interpreter.getPreciserScopeKnowingConditionOutcome(eq, blindScope, true);
    Assert.assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), trueScope.getSlot("x").getType());

    FlowScope falseScope = interpreter.getPreciserScopeKnowingConditionOutcome(eq, blindScope, false);
    Assert.assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), falseScope.getSlot("x").getType());
  }

  @Test
  public void testTypeOfRightChildStringLeftChildTypeOf() {
    JSType union = registry.createUnionType(JSTypeNative.STRING_TYPE, JSTypeNative.NUMBER_TYPE);
    blindScope.inferSlotType("x", union);

    Node str = Node.newString("string");
    Node typeOf = new Node(Token.TYPEOF, Node.newString(Token.NAME, "x"));
    Node eq = new Node(Token.EQ, str, typeOf);

    FlowScope trueScope = interpreter.getPreciserScopeKnowingConditionOutcome(eq, blindScope, true);
    Assert.assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), trueScope.getSlot("x").getType());
  }

  @Test
  public void testTypeOfInCaseStatement() {
    JSType union = registry.createUnionType(JSTypeNative.STRING_TYPE, JSTypeNative.NUMBER_TYPE);
    blindScope.inferSlotType("x", union);

    Node switchCond = new Node(Token.TYPEOF, Node.newString(Token.NAME, "x"));
    Node caseExpr = Node.newString("string");
    Node switchNode = new Node(Token.SWITCH, switchCond);
    Node caseNode = new Node(Token.CASE, caseExpr);
    switchNode.addChildToBack(caseNode);

    FlowScope scope = interpreter.getPreciserScopeKnowingConditionOutcome(caseNode, blindScope, true);
    Assert.assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), scope.getSlot("x").getType());

    FlowScope falseScope = interpreter.getPreciserScopeKnowingConditionOutcome(caseNode, blindScope, false);
    Assert.assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), falseScope.getSlot("x").getType());
  }

  @Test
  public void testAndBranches() {
    JSType union = registry.createNullableType(registry.getNativeType(JSTypeNative.STRING_TYPE));
    blindScope.inferSlotType("a", union);
    blindScope.inferSlotType("b", union);

    Node left = Node.newString(Token.NAME, "a");
    Node right = Node.newString(Token.NAME, "b");
    Node and = new Node(Token.AND, left, right);

    FlowScope trueScope = interpreter.getPreciserScopeKnowingConditionOutcome(and, blindScope, true);
    Assert.assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), trueScope.getSlot("a").getType());
    Assert.assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), trueScope.getSlot("b").getType());

    FlowScope falseScope = interpreter.getPreciserScopeKnowingConditionOutcome(and, blindScope, false);
    Assert.assertNotNull(falseScope);
  }

  @Test
  public void testOrBranches() {
    JSType union = registry.createNullableType(registry.getNativeType(JSTypeNative.STRING_TYPE));
    blindScope.inferSlotType("a", union);
    blindScope.inferSlotType("b", union);

    Node left = Node.newString(Token.NAME, "a");
    Node right = Node.newString(Token.NAME, "b");
    Node or = new Node(Token.OR, left, right);

    FlowScope falseScope = interpreter.getPreciserScopeKnowingConditionOutcome(or, blindScope, false);
    Assert.assertEquals(registry.getNativeType(JSTypeNative.NULL_TYPE), falseScope.getSlot("a").getType());
    Assert.assertEquals(registry.getNativeType(JSTypeNative.NULL_TYPE), falseScope.getSlot("b").getType());

    FlowScope trueScope = interpreter.getPreciserScopeKnowingConditionOutcome(or, blindScope, true);
    Assert.assertNotNull(trueScope);
  }

  @Test
  public void testEqualityOperators() {
    blindScope.inferSlotType("a", registry.createNullableType(registry.getNativeType(JSTypeNative.NUMBER_TYPE)));
    blindScope.inferSlotType("b", registry.getNativeType(JSTypeNative.NUMBER_TYPE));

    Node a = Node.newString(Token.NAME, "a");
    Node b = Node.newString(Token.NAME, "b");

    Node eq = new Node(Token.EQ, a, b);
    FlowScope scopeEqTrue = interpreter.getPreciserScopeKnowingConditionOutcome(eq, blindScope, true);
    Assert.assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), scopeEqTrue.getSlot("a").getType());

    FlowScope scopeEqFalse = interpreter.getPreciserScopeKnowingConditionOutcome(eq, blindScope, false);
    Assert.assertNotNull(scopeEqFalse);

    Node ne = new Node(Token.NE, a.cloneNode(), b.cloneNode());
    FlowScope scopeNeTrue = interpreter.getPreciserScopeKnowingConditionOutcome(ne, blindScope, true);
    Assert.assertNotNull(scopeNeTrue);

    FlowScope scopeNeFalse = interpreter.getPreciserScopeKnowingConditionOutcome(ne, blindScope, false);
    Assert.assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), scopeNeFalse.getSlot("a").getType());

    Node sheq = new Node(Token.SHEQ, a.cloneNode(), b.cloneNode());
    FlowScope scopeSheqTrue = interpreter.getPreciserScopeKnowingConditionOutcome(sheq, blindScope, true);
    Assert.assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), scopeSheqTrue.getSlot("a").getType());

    FlowScope scopeSheqFalse = interpreter.getPreciserScopeKnowingConditionOutcome(sheq, blindScope, false);
    Assert.assertNotNull(scopeSheqFalse);

    Node shne = new Node(Token.SHNE, a.cloneNode(), b.cloneNode());
    FlowScope scopeShneTrue = interpreter.getPreciserScopeKnowingConditionOutcome(shne, blindScope, true);
    Assert.assertNotNull(scopeShneTrue);

    FlowScope scopeShneFalse = interpreter.getPreciserScopeKnowingConditionOutcome(shne, blindScope, false);
    Assert.assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), scopeShneFalse.getSlot("a").getType());
  }

  @Test
  public void testRelationalInequalities() {
    blindScope.inferSlotType("a", registry.createUnionType(JSTypeNative.NUMBER_TYPE, JSTypeNative.VOID_TYPE));
    blindScope.inferSlotType("b", registry.getNativeType(JSTypeNative.NUMBER_TYPE));

    Node a = Node.newString(Token.NAME, "a");
    Node b = Node.newString(Token.NAME, "b");

    for (int token : new int[]{Token.LT, Token.LE, Token.GT, Token.GE}) {
      Node rel = new Node(token, a.cloneNode(), b.cloneNode());
      FlowScope res = interpreter.getPreciserScopeKnowingConditionOutcome(rel, blindScope, true);
      Assert.assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), res.getSlot("a").getType());

      FlowScope resFalse = interpreter.getPreciserScopeKnowingConditionOutcome(rel, blindScope, false);
      Assert.assertNotNull(resFalse);
    }
  }

  @Test
  public void testAssignNotAndName() {
    blindScope.inferSlotType("a", registry.createNullableType(registry.getNativeType(JSTypeNative.STRING_TYPE)));
    blindScope.inferSlotType("b", registry.createNullableType(registry.getNativeType(JSTypeNative.STRING_TYPE)));

    Node a = Node.newString(Token.NAME, "a");
    Node b = Node.newString(Token.NAME, "b");
    Node assign = new Node(Token.ASSIGN, a, b);

    FlowScope assignScope = interpreter.getPreciserScopeKnowingConditionOutcome(assign, blindScope, true);
    Assert.assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), assignScope.getSlot("a").getType());

    Node not = new Node(Token.NOT, Node.newString(Token.NAME, "a"));
    FlowScope notScope = interpreter.getPreciserScopeKnowingConditionOutcome(not, blindScope, true);
    Assert.assertEquals(registry.getNativeType(JSTypeNative.NULL_TYPE), notScope.getSlot("a").getType());

    Node name = Node.newString(Token.NAME, "a");
    FlowScope nameScope = interpreter.getPreciserScopeKnowingConditionOutcome(name, blindScope, true);
    Assert.assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), nameScope.getSlot("a").getType());
  }

  @Test
  public void testCaseNodeWithoutSwitchCondition() {
    blindScope.inferSlotType("x", registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    Node switchCond = Node.newString(Token.NAME, "x");
    Node caseExpr = Node.newNumber(5);
    caseExpr.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    Node switchNode = new Node(Token.SWITCH, switchCond);
    Node caseNode = new Node(Token.CASE, caseExpr);
    switchNode.addChildToBack(caseNode);

    FlowScope trueScope = interpreter.getPreciserScopeKnowingConditionOutcome(caseNode, blindScope, true);
    Assert.assertNotNull(trueScope);

    FlowScope falseScope = interpreter.getPreciserScopeKnowingConditionOutcome(caseNode, blindScope, false);
    Assert.assertNotNull(falseScope);
  }

  @Test
  public void testInstanceOfOperator() {
    FunctionType constructor = registry.createConstructorType("MyClass", null, null, null);
    ObjectType instance = constructor.getInstanceType();
    blindScope.inferSlotType("obj", registry.getNativeType(JSTypeNative.OBJECT_TYPE));

    Node objNode = Node.newString(Token.NAME, "obj");
    Node ctorNode = Node.newString(Token.NAME, "MyClass");
    ctorNode.setJSType(constructor);

    Node instanceOf = new Node(Token.INSTANCEOF, objNode, ctorNode);

    FlowScope trueScope = interpreter.getPreciserScopeKnowingConditionOutcome(instanceOf, blindScope, true);
    Assert.assertEquals(instance, trueScope.getSlot("obj").getType());

    FlowScope falseScope = interpreter.getPreciserScopeKnowingConditionOutcome(instanceOf, blindScope, false);
    Assert.assertNotNull(falseScope);
  }

  @Test
  public void testInOperator() {
    ObjectType objType = registry.createAnonymousObjectType();
    Node objNode = Node.newString(Token.NAME, "obj");
    objNode.setJSType(objType);

    Node propNode = Node.newString("prop");
    Node inNode = new Node(Token.IN, propNode, objNode);

    FlowScope scope = interpreter.getPreciserScopeKnowingConditionOutcome(inNode, blindScope, true);
    Assert.assertNotNull(scope.getSlot("obj.prop"));
    Assert.assertEquals(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), scope.getSlot("obj.prop").getType());

    FlowScope falseScope = interpreter.getPreciserScopeKnowingConditionOutcome(inNode, blindScope, false);
    Assert.assertNull(falseScope.getSlot("obj.prop"));
  }

  @Test
  public void testDefaultFallthrough() {
    Node node = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    FlowScope scope = interpreter.getPreciserScopeKnowingConditionOutcome(node, blindScope, true);
    Assert.assertSame(blindScope, scope);
  }
}
