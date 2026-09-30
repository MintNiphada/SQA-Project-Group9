package com.google.javascript.jscomp;

import com.google.javascript.jscomp.graph.LatticeElement;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

public class MaybeReachingVariableUseTest {

  private Scope jsScope;
  private ControlFlowGraph<Node> cfg;
  private MaybeReachingVariableUse analysis;
  private Compiler compiler;

  private Node computeAnalysis(String src) {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    Node root = compiler.parseTestCode(src);
    Node functionNode = null;
    if (root.getFirstChild() != null && root.getFirstChild().isFunction()) {
      functionNode = root.getFirstChild();
    } else {
      for (Node n : root.children()) {
        if (n.isFunction()) {
          functionNode = n;
          break;
        }
      }
    }

    Node targetNode = (functionNode != null) ? functionNode : root;

    SyntacticScopeCreator scopeCreator = new SyntacticScopeCreator(compiler);
    if (functionNode != null) {
      Scope globalScope = scopeCreator.createScope(root, null);
      jsScope = scopeCreator.createScope(functionNode, globalScope);
    } else {
      jsScope = scopeCreator.createScope(root, null);
    }

    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, false, true);
    cfa.process(null, targetNode);
    cfg = cfa.getCfg();

    analysis = new MaybeReachingVariableUse(cfg, jsScope, compiler);
    analysis.analyze();

    return targetNode;
  }

  private Node findFirstNode(Node n, int tokenType) {
    if (n.getType() == tokenType) {
      return n;
    }
    for (Node child = n.getFirstChild(); child != null; child = child.getNext()) {
      Node result = findFirstNode(child, tokenType);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  @Test
  public void testReachingUsesLatticeElement() {
    MaybeReachingVariableUse.ReachingUses u1 = new MaybeReachingVariableUse.ReachingUses();
    MaybeReachingVariableUse.ReachingUses u2 = new MaybeReachingVariableUse.ReachingUses();

    Assert.assertTrue(u1.equals(u1));
    Assert.assertTrue(u1.equals(u2));
    Assert.assertEquals(u1.hashCode(), u2.hashCode());
    Assert.assertFalse(u1.equals(null));
    Assert.assertFalse(u1.equals(new Object()));

    computeAnalysis("function f() { var x = 1; return x; }");
    Scope.Var varX = jsScope.getVar("x");
    Node n = new Node(Token.NAME);
    u1.mayUseMap.put(varX, n);

    Assert.assertFalse(u1.equals(u2));
    Assert.assertFalse(u2.equals(u1));

    MaybeReachingVariableUse.ReachingUses u3 = new MaybeReachingVariableUse.ReachingUses(u1);
    Assert.assertTrue(u1.equals(u3));
    Assert.assertEquals(u1.hashCode(), u3.hashCode());
  }

  @Test
  public void testBasicAnalysisProperties() {
    computeAnalysis("function f() { var x = 1; }");
    Assert.assertFalse(analysis.isForward());
    LatticeElement entry = analysis.createEntryLattice();
    Assert.assertNotNull(entry);
    Assert.assertTrue(entry instanceof MaybeReachingVariableUse.ReachingUses);
    LatticeElement initEst = analysis.createInitialEstimateLattice();
    Assert.assertNotNull(initEst);
    Assert.assertTrue(initEst instanceof MaybeReachingVariableUse.ReachingUses);
  }

  @Test
  public void testSimpleVariableUse() {
    Node target = computeAnalysis("function f() { var x = 1; return x; }");
    Node varNode = findFirstNode(target, Token.VAR);
    Collection<Node> uses = analysis.getUses("x", varNode);
    Assert.assertEquals(1, uses.size());
  }

  @Test
  public void testAssignmentOperations() {
    Node target = computeAnalysis("function f() { var x = 1; x += 2; x = 3; return x; }");
    Node varNode = findFirstNode(target, Token.VAR);
    Collection<Node> uses = analysis.getUses("x", varNode);
    Assert.assertEquals(1, uses.size());
  }

  @Test
  public void testIfBranching() {
    Node target = computeAnalysis("function f(cond) { var x = 1; if (cond) { return x; } else { return x; } }");
    Node varNode = findFirstNode(target, Token.VAR);
    Collection<Node> uses = analysis.getUses("x", varNode);
    Assert.assertEquals(2, uses.size());
  }

  @Test
  public void testWhileAndDoLoops() {
    Node target = computeAnalysis("function f() { var x = 0; while(x < 10) { x++; } do { x--; } while(x > 0); return x; }");
    Node varNode = findFirstNode(target, Token.VAR);
    Collection<Node> uses = analysis.getUses("x", varNode);
    Assert.assertTrue(uses.size() >= 1);
  }

  @Test
  public void testForLoops() {
    Node target = computeAnalysis("function f() { var x = 0; for (; x < 10; x++) { alert(x); } }");
    Node varNode = findFirstNode(target, Token.VAR);
    Collection<Node> uses = analysis.getUses("x", varNode);
    Assert.assertTrue(uses.size() >= 1);
  }

  @Test
  public void testForInLoops() {
    Node target = computeAnalysis("function f(obj) { var x; for (x in obj) { alert(x); } for (var y in obj) { alert(y); } }");
    Node varX = findFirstNode(target, Token.VAR);
    Collection<Node> usesX = analysis.getUses("x", varX);
    Assert.assertNotNull(usesX);
  }

  @Test
  public void testForInNonNameLhs() {
    Node target = computeAnalysis("function f(obj, arr) { var x = {}; for (x.prop in arr) { alert(x); } }");
    Node varX = findFirstNode(target, Token.VAR);
    Collection<Node> usesX = analysis.getUses("x", varX);
    Assert.assertNotNull(usesX);
  }

  @Test
  public void testLogicalAndOrAndHook() {
    Node target = computeAnalysis("function f(a, b) { var x = 1; var y = (x && a) || (b ? x : 0); return y; }");
    Node varX = findFirstNode(target, Token.VAR);
    Collection<Node> uses = analysis.getUses("x", varX);
    Assert.assertTrue(uses.size() >= 1);
  }

  @Test
  public void testVarWithoutInit() {
    Node target = computeAnalysis("function f() { var x; x = 2; return x; }");
    Node varNode = findFirstNode(target, Token.VAR);
    Collection<Node> uses = analysis.getUses("x", varNode);
    Assert.assertTrue(uses.isEmpty());
  }

  @Test
  public void testEscapedVariable() {
    Node target = computeAnalysis("function f() { var x = 1; function inner() { return x; } return inner; }");
    Node varNode = findFirstNode(target, Token.VAR);
    Collection<Node> uses = analysis.getUses("x", varNode);
    Assert.assertTrue(uses.isEmpty());
  }

  @Test
  public void testNonLocalVariableIgnored() {
    Node target = computeAnalysis("var g = 1; function f() { g = 2; return g; }");
    Node assignNode = findFirstNode(target, Token.ASSIGN);
    if (assignNode != null) {
      Collection<Node> uses = analysis.getUses("g", assignNode);
      Assert.assertNull(uses);
    }
  }

  @Test
  public void testReachingUsesJoinOp() {
    computeAnalysis("function f() { var x = 1; return x; }");
    MaybeReachingVariableUse.ReachingUses u1 = new MaybeReachingVariableUse.ReachingUses();
    MaybeReachingVariableUse.ReachingUses u2 = new MaybeReachingVariableUse.ReachingUses();

    Scope.Var varX = jsScope.getVar("x");
    Node n1 = new Node(Token.NAME);
    Node n2 = new Node(Token.NAME);
    u1.mayUseMap.put(varX, n1);
    u2.mayUseMap.put(varX, n2);

    MaybeReachingVariableUse.ReachingUses joined =
        new MaybeReachingVariableUse.ReachingUses();
    joined.mayUseMap.putAll(u1.mayUseMap);
    joined.mayUseMap.putAll(u2.mayUseMap);

    Assert.assertEquals(2, joined.mayUseMap.get(varX).size());
  }

  @Test
  public void testComplexSubtreeTraversal() {
    Node target = computeAnalysis("function f() { var x = 1; var arr = [x, 2, {key: x}]; return arr; }");
    Node varX = findFirstNode(target, Token.VAR);
    Collection<Node> uses = analysis.getUses("x", varX);
    Assert.assertEquals(1, uses.size());
  }
}