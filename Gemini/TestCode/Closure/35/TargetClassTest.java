package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Maps;
import com.google.javascript.jscomp.CodingConvention.AssertionFunctionSpec;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.BooleanLiteralSet;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

public class TargetClassTest {

  private Compiler compiler;
  private JSTypeRegistry registry;
  private SemanticReverseAbstractInterpreter reverseInterpreter;
  private Map<String, AssertionFunctionSpec> assertionFunctionsMap;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    registry = compiler.getTypeRegistry();
    reverseInterpreter = new SemanticReverseAbstractInterpreter(
        new ClosureCodingConvention(), registry);
    assertionFunctionsMap = Maps.newHashMap();
  }

  private TypeInference createTypeInference(Node rootNode) {
    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, false, false);
    cfa.process(null, rootNode);
    ControlFlowGraph<Node> cfg = cfa.getCfg();
    Scope scope = new Scope(rootNode, (ObjectType) null);
    return new TypeInference(compiler, cfg, reverseInterpreter, scope, assertionFunctionsMap);
  }

  @Test
  public void testDiagnosticConstants() {
    assertEquals("JSC_TEMPLATE_TYPE_NOT_OBJECT_TYPE",
        TypeInference.TEMPLATE_TYPE_NOT_OBJECT_TYPE.key);
    assertEquals("JSC_TEMPLATE_TYPE_OF_THIS_EXPECTED",
        TypeInference.TEMPLATE_TYPE_OF_THIS_EXPECTED.key);
    assertEquals("JSC_FUNCTION_LITERAL_UNDEFINED_THIS",
        TypeInference.FUNCTION_LITERAL_UNDEFINED_THIS.key);
  }

  @Test
  public void testGetBooleanOutcomes() {
    assertEquals(BooleanLiteralSet.TRUE,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.TRUE, BooleanLiteralSet.TRUE, true));
    assertEquals(BooleanLiteralSet.FALSE,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.TRUE, BooleanLiteralSet.FALSE, true));
    assertEquals(BooleanLiteralSet.TRUE,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.FALSE, BooleanLiteralSet.TRUE, true));
    assertEquals(BooleanLiteralSet.FALSE,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.FALSE, BooleanLiteralSet.FALSE, true));

    assertEquals(BooleanLiteralSet.TRUE,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.TRUE, BooleanLiteralSet.TRUE, false));
    assertEquals(BooleanLiteralSet.TRUE,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.TRUE, BooleanLiteralSet.FALSE, false));
    assertEquals(BooleanLiteralSet.FALSE,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.FALSE, BooleanLiteralSet.FALSE, false));

    assertEquals(BooleanLiteralSet.BOTH,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.BOTH, BooleanLiteralSet.BOTH, true));
    assertEquals(BooleanLiteralSet.EMPTY,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.EMPTY, BooleanLiteralSet.EMPTY, true));
  }

  @Test
  public void testEntryAndInitialLattices() {
    Node script = new Node(Token.SCRIPT);
    TypeInference ti = createTypeInference(script);

    FlowScope entry = ti.createEntryLattice();
    FlowScope init = ti.createInitialEstimateLattice();

    assertNotNull(entry);
    assertNotNull(init);
    assertSame(init, ti.flowThrough(script, init));
  }

  @Test
  public void testFlowThroughBasicNodes() {
    Node block = new Node(Token.BLOCK);
    TypeInference ti = createTypeInference(block);
    FlowScope scope = ti.createEntryLattice();

    Node num = Node.newNumber(42);
    FlowScope s1 = ti.flowThrough(num, scope);
    assertNotNull(s1);

    Node str = Node.newString("hello");
    FlowScope s2 = ti.flowThrough(str, s1);
    assertNotNull(s2);

    Node thisNode = new Node(Token.THIS);
    FlowScope s3 = ti.flowThrough(thisNode, s2);
    assertNotNull(s3);

    Node arr = new Node(Token.ARRAYLIT);
    FlowScope s4 = ti.flowThrough(arr, s3);
    assertNotNull(s4);
    assertNotNull(arr.getJSType());
    assertTrue(arr.getJSType().isArrayType());

    Node obj = new Node(Token.OBJECTLIT);
    obj.setJSType(registry.getNativeType(com.google.javascript.rhino.jstype.JSTypeNative.OBJECT_TYPE));
    FlowScope s5 = ti.flowThrough(obj, s4);
    assertNotNull(s5);

    Node pos = new Node(Token.POS, Node.newNumber(1));
    FlowScope s6 = ti.flowThrough(pos, s5);
    assertNotNull(s6);
    assertNotNull(pos.getJSType());
    assertTrue(pos.getJSType().isNumber());

    Node neg = new Node(Token.NEG, Node.newNumber(2));
    FlowScope s7 = ti.flowThrough(neg, s6);
    assertNotNull(s7);
    assertNotNull(neg.getJSType());
    assertTrue(neg.getJSType().isNumber());

    Node typeof = new Node(Token.TYPEOF, Node.newString("foo"));
    FlowScope s8 = ti.flowThrough(typeof, s7);
    assertNotNull(s8);
    assertNotNull(typeof.getJSType());
    assertTrue(typeof.getJSType().isString());

    Node not = new Node(Token.NOT, Node.newNumber(0));
    FlowScope s9 = ti.flowThrough(not, s8);
    assertNotNull(s9);
    assertNotNull(not.getJSType());
    assertTrue(not.getJSType().isBooleanValueType());
  }

  @Test
  public void testFlowThroughArithmeticAndLogic() {
    Node block = new Node(Token.BLOCK);
    TypeInference ti = createTypeInference(block);
    FlowScope scope = ti.createEntryLattice();

    Node add = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    FlowScope s1 = ti.flowThrough(add, scope);
    assertNotNull(s1);
    assertNotNull(add.getJSType());

    Node addStr = new Node(Token.ADD, Node.newString("a"), Node.newNumber(2));
    FlowScope s2 = ti.flowThrough(addStr, s1);
    assertNotNull(s2);
    assertNotNull(addStr.getJSType());
    assertTrue(addStr.getJSType().isString());

    Node sub = new Node(Token.SUB, Node.newNumber(5), Node.newNumber(3));
    FlowScope s3 = ti.flowThrough(sub, s2);
    assertNotNull(s3);
    assertNotNull(sub.getJSType());
    assertTrue(sub.getJSType().isNumber());

    Node comma = new Node(Token.COMMA, Node.newNumber(1), Node.newString("val"));
    FlowScope s4 = ti.flowThrough(comma, s3);
    assertNotNull(s4);
    assertNotNull(comma.getJSType());

    Node and = new Node(Token.AND, Node.newNumber(1), Node.newNumber(2));
    FlowScope s5 = ti.flowThrough(and, s4);
    assertNotNull(s5);

    Node or = new Node(Token.OR, Node.newNumber(0), Node.newNumber(1));
    FlowScope s6 = ti.flowThrough(or, s5);
    assertNotNull(s6);

    Node hook = new Node(Token.HOOK, Node.newNumber(1), Node.newString("trueBranch"), Node.newString("falseBranch"));
    FlowScope s7 = ti.flowThrough(hook, s6);
    assertNotNull(s7);
  }

  @Test
  public void testFlowThroughStatements() {
    Node block = new Node(Token.BLOCK);
    TypeInference ti = createTypeInference(block);
    FlowScope scope = ti.createEntryLattice();

    Node returnNode = new Node(Token.RETURN, Node.newNumber(10));
    FlowScope s1 = ti.flowThrough(returnNode, scope);
    assertNotNull(s1);

    Node switchNode = new Node(Token.SWITCH, Node.newNumber(1));
    FlowScope s2 = ti.flowThrough(switchNode, s1);
    assertNotNull(s2);

    Node catchNode = new Node(Token.CATCH, Node.newString(Token.NAME, "err"), new Node(Token.BLOCK));
    FlowScope s3 = ti.flowThrough(catchNode, s2);
    assertNotNull(s3);

    Node varNode = new Node(Token.VAR, Node.newString(Token.NAME, "temp"));
    FlowScope s4 = ti.flowThrough(varNode, s3);
    assertNotNull(s4);

    Node exprResult = new Node(Token.EXPR_RESULT, Node.newNumber(100));
    FlowScope s5 = ti.flowThrough(exprResult, s4);
    assertNotNull(s5);
  }

  @Test
  public void testBranchedFlowThrough() {
    Node cond = Node.newNumber(1);
    Node block = new Node(Token.BLOCK, cond);
    TypeInference ti = createTypeInference(block);
    FlowScope scope = ti.createEntryLattice();

    java.util.List<FlowScope> branched = ti.branchedFlowThrough(cond, scope);
    assertNotNull(branched);
  }
}