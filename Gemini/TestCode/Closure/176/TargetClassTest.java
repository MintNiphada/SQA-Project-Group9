package com.google.javascript.jscomp;

import com.google.javascript.jscomp.CodingConvention.AssertionFunctionSpec;
import com.google.javascript.jscomp.type.FlowScope;
import com.google.javascript.jscomp.type.ReverseAbstractInterpreter;
import com.google.javascript.jscomp.type.SemanticReverseAbstractInterpreter;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.BooleanLiteralSet;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TypeInferenceTest {

  private Compiler compiler;
  private JSTypeRegistry registry;
  private ReverseAbstractInterpreter rai;

  @Before
  public void setUp() {
    compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    registry = compiler.getTypeRegistry();
    rai = new SemanticReverseAbstractInterpreter(compiler.getCodingConvention(), registry);
  }

  private TypeInference createTypeInference(Node root, Scope scope, Map<String, AssertionFunctionSpec> assertions) {
    ControlFlowGraph<Node> cfg = ControlFlowAnalysis.computeCfg(root, true);
    return new TypeInference(compiler, cfg, rai, scope, assertions);
  }

  private Scope createScope(Node root) {
    return new SyntacticScopeCreator(compiler).createScope(root, null);
  }

  private FlowScope processScript(String js) {
    Node root = compiler.parseTestCode(js);
    Scope scope = createScope(root);
    TypeInference ti = createTypeInference(root, scope, Collections.emptyMap());
    FlowScope entry = ti.createEntryLattice();
    return ti.flowThrough(root, entry);
  }

  @Test
  public void testGetBooleanOutcomes() {
    BooleanLiteralSet res1 = TypeInference.getBooleanOutcomes(BooleanLiteralSet.TRUE, BooleanLiteralSet.FALSE, true);
    Assert.assertEquals(BooleanLiteralSet.FALSE, res1);

    BooleanLiteralSet res2 = TypeInference.getBooleanOutcomes(BooleanLiteralSet.BOTH, BooleanLiteralSet.TRUE, false);
    Assert.assertEquals(BooleanLiteralSet.TRUE, res2);

    BooleanLiteralSet res3 = TypeInference.getBooleanOutcomes(BooleanLiteralSet.EMPTY, BooleanLiteralSet.EMPTY, true);
    Assert.assertEquals(BooleanLiteralSet.EMPTY, res3);
  }

  @Test
  public void testLatticesAndBottomScope() {
    Node root = compiler.parseTestCode("var a = 1;");
    Scope scope = createScope(root);
    TypeInference ti = createTypeInference(root, scope, Collections.emptyMap());

    FlowScope initial = ti.createInitialEstimateLattice();
    FlowScope entry = ti.createEntryLattice();
    Assert.assertNotNull(initial);
    Assert.assertNotNull(entry);

    FlowScope flowed = ti.flowThrough(root, initial);
    Assert.assertSame(initial, flowed);
  }

  @Test
  public void testArithmeticAndBitwiseOperations() {
    String js = "var a = 1 + 2; var b = 'x' + 2; var c = 1 - 2; var d = 1 * 2; var e = 1 / 2; var f = 1 % 2; " +
                "var g = 1 << 2; var h = 1 >> 2; var i = 1 >>> 2; var j = 1 & 2; var k = 1 | 2; var l = 1 ^ 2; " +
                "var m = +1; var n = -1; var o = ~1; var p = ++a; var q = --b; a += 1; a -= 1; a *= 1; a /= 1; a %= 1; " +
                "a &= 1; a |= 1; a ^= 1; a <<= 1; a >>= 1; a >>>= 1;";
    FlowScope scope = processScript(js);
    Assert.assertNotNull(scope);
  }

  @Test
  public void testRelationalAndLogicalOps() {
    String js = "var a = 1 < 2; var b = 1 <= 2; var c = 1 > 2; var d = 1 >= 2; var e = 1 == 2; var f = 1 != 2; " +
                "var g = 1 === 2; var h = 1 !== 2; var i = !1; var j = (1 in {}); var k = ({} instanceof Object); " +
                "var l = typeof 1; var m = delete ({}).x;";
    FlowScope scope = processScript(js);
    Assert.assertNotNull(scope);
  }

  @Test
  public void testShortCircuitingAndHook() {
    String js = "var a = true && false; var b = false || true; var c = true ? 1 : 'x'; var d = false ? null : 2;";
    FlowScope scope = processScript(js);
    Assert.assertNotNull(scope);
  }

  @Test
  public void testArrayAndObjectLiterals() {
    String js = "var arr = [1, 'two', 3]; var obj = {a: 1, 'b': 'val', c: true}; var el = arr[0]; var prop = obj.a;";
    FlowScope scope = processScript(js);
    Assert.assertNotNull(scope);
  }

  @Test
  public void testControlStructures() {
    String js = "var x = 1; switch (x) { case 1: x = 2; break; default: x = 3; } " +
                "try { throw new Error('err'); } catch (e) { x = e; }";
    FlowScope scope = processScript(js);
    Assert.assertNotNull(scope);
  }

  @Test
  public void testFunctionCallsAndNew() {
    String js = "function Foo(x) { this.x = x; } var f = new Foo(1); " +
                "function bar(a, b) { return a + b; } var res = bar(1, 2); " +
                "var bound = bar.bind(null, 1);";
    FlowScope scope = processScript(js);
    Assert.assertNotNull(scope);
  }

  @Test
  public void testBranchedFlowThroughForIn() {
    Node root = compiler.parseTestCode("for (var k in {a: 1, b: 2}) { var y = k; }");
    Scope scope = createScope(root);
    TypeInference ti = createTypeInference(root, scope, Collections.emptyMap());
    FlowScope entry = ti.createEntryLattice();
    Node forNode = root.getFirstChild();
    List<FlowScope> branched = ti.branchedFlowThrough(forNode, entry);
    Assert.assertNotNull(branched);
  }

  @Test
  public void testBranchedFlowThroughConditionals() {
    Node root = compiler.parseTestCode("var a = true; if (a && false) { var x = 1; } else if (a || true) { var y = 2; }");
    Scope scope = createScope(root);
    TypeInference ti = createTypeInference(root, scope, Collections.emptyMap());
    FlowScope entry = ti.createEntryLattice();
    List<FlowScope> branched = ti.branchedFlowThrough(root.getFirstChild(), entry);
    Assert.assertNotNull(branched);
  }

  @Test
  public void testBranchedFlowThroughSwitchCase() {
    Node root = compiler.parseTestCode("switch (1) { case 1: var a = 1; break; }");
    Scope scope = createScope(root);
    TypeInference ti = createTypeInference(root, scope, Collections.emptyMap());
    FlowScope entry = ti.createEntryLattice();
    Node switchNode = root.getFirstChild();
    Node caseNode = switchNode.getFirstChild().getNext();
    List<FlowScope> branched = ti.branchedFlowThrough(caseNode, entry);
    Assert.assertNotNull(branched);
  }

  @Test
  public void testAssertionFunctionsMapTightenTypes() {
    Node root = compiler.parseTestCode("function assert(condition) {} assert(x != null);");
    Scope scope = createScope(root);
    Map<String, AssertionFunctionSpec> assertions = new HashMap<String, AssertionFunctionSpec>();
    assertions.put("assert", new AssertionFunctionSpec("assert"));
    TypeInference ti = createTypeInference(root, scope, assertions);
    FlowScope entry = ti.createEntryLattice();
    FlowScope result = ti.flowThrough(root, entry);
    Assert.assertNotNull(result);
  }

  @Test
  public void testCommaAndParamExpressions() {
    String js = "var a = (1, 2, 3);";
    FlowScope scope = processScript(js);
    Assert.assertNotNull(scope);
  }

  @Test
  public void testCastAnnotation() {
    String js = "var x = /** @type {number} */ ('5');";
    FlowScope scope = processScript(js);
    Assert.assertNotNull(scope);
  }

  @Test
  public void testPropertyInferenceOnConstructor() {
    String js = "function Ctor() { this.prop = 42; } var c = new Ctor(); var p = c.prop;";
    FlowScope scope = processScript(js);
    Assert.assertNotNull(scope);
  }
}
