package com.google.javascript.jscomp;

import com.google.javascript.jscomp.ControlFlowGraph.Branch;
import com.google.javascript.jscomp.LiveVariablesAnalysis.LiveVariableLattice;
import com.google.javascript.jscomp.Scope.Var;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LiveVariablesAnalysisTest {

  private static class AnalysisHelper {
    final Compiler compiler;
    final Node root;
    final ControlFlowGraph<Node> cfg;
    final Scope scope;
    final LiveVariablesAnalysis lva;

    AnalysisHelper(String js) {
      this.compiler = new Compiler();
      CompilerOptions options = new CompilerOptions();
      this.compiler.initOptions(options);

      this.root = compiler.parseTestCode(js);
      Node functionNode = findFirstFunction(root);
      Node scopeRoot = (functionNode != null) ? functionNode : root;

      ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, false, true);
      cfa.process(null, scopeRoot);
      this.cfg = cfa.getCfg();

      SyntacticScopeCreator scopeCreator = new SyntacticScopeCreator(compiler);
      this.scope = scopeCreator.createScope(scopeRoot, null);

      this.lva = new LiveVariablesAnalysis(cfg, scope, compiler);
    }

    private static Node findFirstFunction(Node n) {
      if (n.getType() == Token.FUNCTION) {
        return n;
      }
      for (Node c = n.getFirstChild(); c != null; c = c.getNext()) {
        Node result = findFirstFunction(c);
        if (result != null) {
          return result;
        }
      }
      return null;
    }
  }

  @Test
  public void testLatticeOperations() {
    AnalysisHelper helper = new AnalysisHelper("function f(a, b) { var c = 1; }");
    LiveVariableLattice entry = helper.lva.createEntryLattice();
    LiveVariableLattice estimate = helper.lva.createInitialEstimateLattice();

    Assert.assertNotNull(entry);
    Assert.assertNotNull(estimate);
    Assert.assertEquals(entry, estimate);
    Assert.assertEquals(entry.hashCode(), estimate.hashCode());
    Assert.assertEquals(entry.toString(), estimate.toString());

    Assert.assertFalse(entry.isLive(0));
    Assert.assertFalse(entry.isLive(1));

    Var varA = helper.scope.getVar("a");
    Var varB = helper.scope.getVar("b");
    Assert.assertNotNull(varA);
    Assert.assertNotNull(varB);
    Assert.assertFalse(entry.isLive(varA));
    Assert.assertFalse(entry.isLive(varB));

    Assert.assertFalse(entry.equals("notALattice"));
    Assert.assertFalse(entry.equals(new Object()));
  }

  @Test
  public void testJoinOp() {
    AnalysisHelper helper = new AnalysisHelper("function f(a, b, c) { var d = 1; }");
    LiveVariableLattice lat1 = helper.lva.createEntryLattice();
    LiveVariableLattice lat2 = helper.lva.createEntryLattice();

    Node nameA = Node.newString(Token.NAME, "a");
    LiveVariableLattice flowed1 = helper.lva.flowThrough(nameA, lat1);

    Node nameB = Node.newString(Token.NAME, "b");
    LiveVariableLattice flowed2 = helper.lva.flowThrough(nameB, lat2);

    List<LiveVariableLattice> lattices = new ArrayList<LiveVariableLattice>();
    lattices.add(flowed1);
    lattices.add(flowed2);

    LiveVariablesAnalysis.LiveVariableLattice joined = helper.lva.getJoinOp().apply(lattices);
    Assert.assertNotNull(joined);

    int idxA = helper.lva.getVarIndex("a");
    int idxB = helper.lva.getVarIndex("b");
    int idxC = helper.lva.getVarIndex("c");

    Assert.assertTrue(joined.isLive(idxA));
    Assert.assertTrue(joined.isLive(idxB));
    Assert.assertFalse(joined.isLive(idxC));
  }

  @Test
  public void testFlowThroughAssignAndUse() {
    AnalysisHelper helper = new AnalysisHelper("function f(x) { var y; y = x; return y; }");
    LiveVariableLattice lat = helper.lva.createEntryLattice();

    int idxX = helper.lva.getVarIndex("x");
    int idxY = helper.lva.getVarIndex("y");

    // return y -> uses y
    Node returnY = new Node(Token.RETURN, Node.newString(Token.NAME, "y"));
    LiveVariableLattice out1 = helper.lva.flowThrough(returnY, lat);
    Assert.assertTrue(out1.isLive(idxY));
    Assert.assertFalse(out1.isLive(idxX));

    // y = x -> kills y, gens x
    Node assignNode = new Node(Token.ASSIGN, Node.newString(Token.NAME, "y"), Node.newString(Token.NAME, "x"));
    LiveVariableLattice out2 = helper.lva.flowThrough(assignNode, out1);
    Assert.assertFalse(out2.isLive(idxY));
    Assert.assertTrue(out2.isLive(idxX));
  }

  @Test
  public void testFlowThroughCompoundAssignment() {
    AnalysisHelper helper = new AnalysisHelper("function f(x, y) { x += y; }");
    LiveVariableLattice lat = helper.lva.createEntryLattice();

    int idxX = helper.lva.getVarIndex("x");
    int idxY = helper.lva.getVarIndex("y");

    Node assignAdd = new Node(Token.ASSIGN_ADD, Node.newString(Token.NAME, "x"), Node.newString(Token.NAME, "y"));
    LiveVariableLattice out = helper.lva.flowThrough(assignAdd, lat);

    // Compound assignment x += y: kills x, but also gens x and y
    Assert.assertTrue(out.isLive(idxX));
    Assert.assertTrue(out.isLive(idxY));
  }

  @Test
  public void testFlowThroughLogicalAndHook() {
    AnalysisHelper helper = new AnalysisHelper("function f(a, b, c, d) { }");
    LiveVariableLattice lat = helper.lva.createEntryLattice();

    int idxA = helper.lva.getVarIndex("a");
    int idxB = helper.lva.getVarIndex("b");
    int idxC = helper.lva.getVarIndex("c");
    int idxD = helper.lva.getVarIndex("d");

    // AND node: a && b
    Node andNode = new Node(Token.AND, Node.newString(Token.NAME, "a"), Node.newString(Token.NAME, "b"));
    LiveVariableLattice outAnd = helper.lva.flowThrough(andNode, lat);
    Assert.assertTrue(outAnd.isLive(idxA));
    Assert.assertTrue(outAnd.isLive(idxB));

    // OR node: c || d
    Node orNode = new Node(Token.OR, Node.newString(Token.NAME, "c"), Node.newString(Token.NAME, "d"));
    LiveVariableLattice outOr = helper.lva.flowThrough(orNode, lat);
    Assert.assertTrue(outOr.isLive(idxC));
    Assert.assertTrue(outOr.isLive(idxD));

    // HOOK node: a ? b : c
    Node hookNode = new Node(Token.HOOK,
        Node.newString(Token.NAME, "a"),
        Node.newString(Token.NAME, "b"),
        Node.newString(Token.NAME, "c"));
    LiveVariableLattice outHook = helper.lva.flowThrough(hookNode, lat);
    Assert.assertTrue(outHook.isLive(idxA));
    Assert.assertTrue(outHook.isLive(idxB));
    Assert.assertTrue(outHook.isLive(idxC));
    Assert.assertFalse(outHook.isLive(idxD));
  }

  @Test
  public void testFlowThroughLoopsAndIf() {
    AnalysisHelper helper = new AnalysisHelper("function f(a, b, c) { }");
    LiveVariableLattice lat = helper.lva.createEntryLattice();

    int idxA = helper.lva.getVarIndex("a");
    int idxB = helper.lva.getVarIndex("b");
    int idxC = helper.lva.getVarIndex("c");

    // IF condition
    Node ifNode = new Node(Token.IF, Node.newString(Token.NAME, "a"), new Node(Token.BLOCK));
    LiveVariableLattice outIf = helper.lva.flowThrough(ifNode, lat);
    Assert.assertTrue(outIf.isLive(idxA));

    // WHILE loop
    Node whileNode = new Node(Token.WHILE, Node.newString(Token.NAME, "b"), new Node(Token.BLOCK));
    LiveVariableLattice outWhile = helper.lva.flowThrough(whileNode, lat);
    Assert.assertTrue(outWhile.isLive(idxB));

    // DO loop
    Node doNode = new Node(Token.DO, new Node(Token.BLOCK), Node.newString(Token.NAME, "c"));
    LiveVariableLattice outDo = helper.lva.flowThrough(doNode, lat);
    Assert.assertTrue(outDo.isLive(idxC));
  }

  @Test
  public void testFlowThroughForLoops() {
    AnalysisHelper helper = new AnalysisHelper("function f(x, y, arr) { var k; }");
    LiveVariableLattice lat = helper.lva.createEntryLattice();

    int idxX = helper.lva.getVarIndex("x");
    int idxY = helper.lva.getVarIndex("y");
    int idxArr = helper.lva.getVarIndex("arr");

    // Standard FOR loop: for (init; condition; increment)
    Node forStandard = new Node(Token.FOR,
        new Node(Token.EMPTY),
        Node.newString(Token.NAME, "x"),
        new Node(Token.EMPTY),
        new Node(Token.BLOCK));
    LiveVariableLattice outStandard = helper.lva.flowThrough(forStandard, lat);
    Assert.assertTrue(outStandard.isLive(idxX));

    // FOR-IN loop: for (y in arr)
    Node forIn = new Node(Token.FOR,
        Node.newString(Token.NAME, "y"),
        Node.newString(Token.NAME, "arr"),
        new Node(Token.BLOCK));
    LiveVariableLattice outForIn = helper.lva.flowThrough(forIn, lat);
    Assert.assertTrue(outForIn.isLive(idxY));
    Assert.assertTrue(outForIn.isLive(idxArr));

    // FOR-IN loop with var declaration: for (var k in arr)
    Node varK = new Node(Token.VAR, Node.newString(Token.NAME, "k"));
    Node forInVar = new Node(Token.FOR,
        varK,
        Node.newString(Token.NAME, "arr"),
        new Node(Token.BLOCK));
    LiveVariableLattice outForInVar = helper.lva.flowThrough(forInVar, lat);
    int idxK = helper.lva.getVarIndex("k");
    Assert.assertTrue(outForInVar.isLive(idxK));
    Assert.assertTrue(outForInVar.isLive(idxArr));
  }

  @Test
  public void testFlowThroughVarDeclaration() {
    AnalysisHelper helper = new AnalysisHelper("function f(a) { var b = a, c; }");
    LiveVariableLattice lat = helper.lva.createEntryLattice();

    int idxA = helper.lva.getVarIndex("a");
    int idxB = helper.lva.getVarIndex("b");

    // lat has b live
    Node nameB = Node.newString(Token.NAME, "b");
    LiveVariableLattice latWithB = helper.lva.flowThrough(nameB, lat);
    Assert.assertTrue(latWithB.isLive(idxB));

    // var b = a; (kills b, gens a)
    Node varDeclChildB = Node.newString(Token.NAME, "b");
    varDeclChildB.addChildToFront(Node.newString(Token.NAME, "a"));
    Node varDeclNode = new Node(Token.VAR, varDeclChildB);

    LiveVariableLattice outVar = helper.lva.flowThrough(varDeclNode, latWithB);
    Assert.assertFalse(outVar.isLive(idxB));
    Assert.assertTrue(outVar.isLive(idxA));
  }

  @Test
  public void testIgnoredConstructs() {
    AnalysisHelper helper = new AnalysisHelper("function f(a) { }");
    LiveVariableLattice lat = helper.lva.createEntryLattice();

    Node script = new Node(Token.SCRIPT);
    Node block = new Node(Token.BLOCK);
    Node function = new Node(Token.FUNCTION);

    Assert.assertEquals(lat, helper.lva.flowThrough(script, lat));
    Assert.assertEquals(lat, helper.lva.flowThrough(block, lat));
    Assert.assertEquals(lat, helper.lva.flowThrough(function, lat));
  }

  @Test
  public void testArgumentsEscapesAllParameters() {
    AnalysisHelper helper = new AnalysisHelper("function f(x, y) { return arguments[0]; }");
    LiveVariableLattice lat = helper.lva.createEntryLattice();

    Node argsNode = Node.newString(Token.NAME, "arguments");
    LiveVariableLattice out = helper.lva.flowThrough(argsNode, lat);
    Assert.assertNotNull(out);

    Set<Var> escaped = helper.lva.getEscapedLocals();
    Var varX = helper.scope.getVar("x");
    Var varY = helper.scope.getVar("y");

    Assert.assertTrue(escaped.contains(varX));
    Assert.assertTrue(escaped.contains(varY));

    // Once escaped, addToSetIfLocal should not add them to gen/kill
    Node nameX = Node.newString(Token.NAME, "x");
    LiveVariableLattice outX = helper.lva.flowThrough(nameX, lat);
    Assert.assertFalse(outX.isLive(varX));
  }

  @Test
  public void testDeclaredArgumentsNotEscaped() {
    AnalysisHelper helper = new AnalysisHelper("function f(arguments, x) { return arguments; }");
    LiveVariableLattice lat = helper.lva.createEntryLattice();

    Node argsNode = Node.newString(Token.NAME, "arguments");
    LiveVariableLattice out = helper.lva.flowThrough(argsNode, lat);

    int idxArgs = helper.lva.getVarIndex("arguments");
    Assert.assertTrue(out.isLive(idxArgs));
  }

  @Test
  public void testUndeclaredGlobalVarNotTracked() {
    AnalysisHelper helper = new AnalysisHelper("function f(a) { }");
    LiveVariableLattice lat = helper.lva.createEntryLattice();

    Node globalName = Node.newString(Token.NAME, "undeclaredGlobal");
    LiveVariableLattice out = helper.lva.flowThrough(globalName, lat);

    // undeclared variable should not throw and should not change anything
    Assert.assertEquals(lat, out);
  }

  @Test
  public void testAnalyzeDataFlowExecution() {
    AnalysisHelper helper = new AnalysisHelper(
        "function f(a, b) {\n" +
        "  var c = a + 1;\n" +
        "  if (c > 0) {\n" +
        "    b = 2;\n" +
        "  }\n" +
        "  return b + c;\n" +
        "}");

    helper.lva.analyze();
    Assert.assertFalse(helper.lva.isForward());
  }

  @Test
  public void testInnerFunctionEscapedVariables() {
    AnalysisHelper helper = new AnalysisHelper(
        "function f(a, b) {\n" +
        "  function inner() {\n" +
        "    return a;\n" +
        "  }\n" +
        "  return inner();\n" +
        "}");

    Set<Var> escaped = helper.lva.getEscapedLocals();
    Var varA = helper.scope.getVar("a");
    Var varB = helper.scope.getVar("b");

    Assert.assertTrue(escaped.contains(varA));
    Assert.assertFalse(escaped.contains(varB));
  }
}