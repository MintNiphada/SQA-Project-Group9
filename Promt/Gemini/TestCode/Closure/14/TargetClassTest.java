package com.google.javascript.jscomp;

import com.google.javascript.jscomp.ControlFlowGraph.Branch;
import com.google.javascript.jscomp.graph.DiGraph.DiGraphEdge;
import com.google.javascript.jscomp.graph.DiGraph.DiGraphNode;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.*;

public class ControlFlowAnalysisTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  private ControlFlowGraph<Node> createCfg(String js, boolean runTraverseFunctions) {
    Node root = compiler.parseTestCode(js);
    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, runTraverseFunctions, true);
    cfa.process(null, root);
    return cfa.getCfg();
  }

  private ControlFlowAnalysis createCfa(String js, boolean runTraverseFunctions) {
    Node root = compiler.parseTestCode(js);
    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, runTraverseFunctions, true);
    cfa.process(null, root);
    return cfa;
  }

  @Test
  public void testSimpleStatements() {
    String js = "var a = 1; var b = 2; a = a + b;";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
    assertNotNull(cfg.getEntry());
    assertNotNull(cfg.getImplicitReturn());
  }

  @Test
  public void testIfElse() {
    String js = "if (x) { a = 1; } else { a = 2; }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);

    String jsNoElse = "if (x) { a = 1; }";
    ControlFlowGraph<Node> cfgNoElse = createCfg(jsNoElse, true);
    assertNotNull(cfgNoElse);
  }

  @Test
  public void testWhileLoop() {
    String js = "while (x > 0) { x--; }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
  }

  @Test
  public void testDoWhileLoop() {
    String js = "do { x++; } while (x < 10);";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
  }

  @Test
  public void testForLoop() {
    String js = "for (var i = 0; i < 10; i++) { foo(i); }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
  }

  @Test
  public void testForInLoop() {
    String js = "for (var k in obj) { foo(k); }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
  }

  @Test
  public void testSwitchCaseDefault() {
    String js = "switch (x) {" +
        "  case 1: a = 1; break;" +
        "  case 2: a = 2;" +
        "  default: a = 3;" +
        "}";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
  }

  @Test
  public void testSwitchEmpty() {
    String js = "switch (x) {}";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
  }

  @Test
  public void testSwitchDefaultOnly() {
    String js = "switch (x) { default: a = 1; }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
  }

  @Test
  public void testSwitchCasesNoDefault() {
    String js = "switch (x) { case 1: a = 1; break; case 2: a = 2; break; }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
  }

  @Test
  public void testTryCatchFinally() {
    String js = "try { foo(); } catch (e) { bar(); } finally { baz(); }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
  }

  @Test
  public void testTryFinallyNoCatch() {
    String js = "try { foo(); } finally { baz(); }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
  }

  @Test
  public void testNestedTryFinallyBreak() {
    String js = "while (x) {" +
        "  try {" +
        "    try {" +
        "      break;" +
        "    } catch (a) {" +
        "    } finally {" +
        "      foo();" +
        "    }" +
        "    fooFollow();" +
        "  } catch (b) {" +
        "  } finally {" +
        "    bar();" +
        "  }" +
        "  barFollow();" +
        "}" +
        "END();";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
  }

  @Test
  public void testNestedTryFinallyContinue() {
    String js = "while (x) {" +
        "  try {" +
        "    try {" +
        "      continue;" +
        "    } finally {" +
        "      foo();" +
        "    }" +
        "  } finally {" +
        "    bar();" +
        "  }" +
        "}";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
  }

  @Test
  public void testReturnInsideTryFinally() {
    String js = "function f() {" +
        "  try {" +
        "    return 1;" +
        "  } finally {" +
        "    cleanup();" +
        "  }" +
        "}";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
  }

  @Test
  public void testLabeledBreakAndContinue() {
    String js = "OUTER: while (true) {" +
        "  INNER: for (var i = 0; i < 10; i++) {" +
        "    if (i == 1) continue INNER;" +
        "    if (i == 2) break INNER;" +
        "    if (i == 3) continue OUTER;" +
        "    if (i == 4) break OUTER;" +
        "  }" +
        "}";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
  }

  @Test
  public void testThrowStatement() {
    String js = "try { throw new Error(); } catch (e) { handle(e); }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
  }

  @Test
  public void testWithStatement() {
    String js = "with (o) { a = x; }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
  }

  @Test
  public void testFunctionDeclarations() {
    String js = "function a() { return 1; } function b() { return 2; } a();";
    ControlFlowGraph<Node> cfgTraverse = createCfg(js, true);
    assertNotNull(cfgTraverse);

    ControlFlowGraph<Node> cfgNoTraverse = createCfg(js, false);
    assertNotNull(cfgNoTraverse);
  }

  @Test
  public void testIsBreakTarget() {
    Node forNode = new Node(Token.FOR);
    Node blockNode = new Node(Token.BLOCK);

    assertTrue(ControlFlowAnalysis.isBreakTarget(forNode, null));
    assertFalse(ControlFlowAnalysis.isBreakTarget(blockNode, null));

    Node labelNode = new Node(Token.LABEL, Node.newString("lbl"), blockNode);
    assertTrue(ControlFlowAnalysis.isBreakTarget(blockNode, "lbl"));
    assertFalse(ControlFlowAnalysis.isBreakTarget(blockNode, "other"));
  }

  @Test
  public void testMayThrowException() {
    assertTrue(ControlFlowAnalysis.mayThrowException(new Node(Token.CALL)));
    assertTrue(ControlFlowAnalysis.mayThrowException(new Node(Token.GETPROP)));
    assertTrue(ControlFlowAnalysis.mayThrowException(new Node(Token.GETELEM)));
    assertTrue(ControlFlowAnalysis.mayThrowException(new Node(Token.THROW)));
    assertTrue(ControlFlowAnalysis.mayThrowException(new Node(Token.NEW)));
    assertTrue(ControlFlowAnalysis.mayThrowException(new Node(Token.ASSIGN)));
    assertTrue(ControlFlowAnalysis.mayThrowException(new Node(Token.INC)));
    assertTrue(ControlFlowAnalysis.mayThrowException(new Node(Token.DEC)));
    assertTrue(ControlFlowAnalysis.mayThrowException(new Node(Token.INSTANCEOF)));

    assertFalse(ControlFlowAnalysis.mayThrowException(new Node(Token.FUNCTION)));
    assertFalse(ControlFlowAnalysis.mayThrowException(new Node(Token.NUMBER)));
  }

  @Test
  public void testComputeFallThrough() {
    Node number = new Node(Token.NUMBER);
    assertEquals(number, ControlFlowAnalysis.computeFallThrough(number));

    Node doNode = new Node(Token.DO, number, new Node(Token.TRUE));
    assertEquals(number, ControlFlowAnalysis.computeFallThrough(doNode));

    Node labelNode = new Node(Token.LABEL, Node.newString("L"), number);
    assertEquals(number, ControlFlowAnalysis.computeFallThrough(labelNode));
  }

  @Test
  public void testComputeFollowNode() {
    Node n1 = new Node(Token.EXPR_RESULT, new Node(Token.NUMBER));
    Node n2 = new Node(Token.EXPR_RESULT, new Node(Token.NUMBER));
    Node block = new Node(Token.BLOCK, n1, n2);
    
    assertEquals(n2, ControlFlowAnalysis.computeFollowNode(n1));
    assertNull(ControlFlowAnalysis.computeFollowNode(n2));
  }

  @Test
  public void testGetExceptionHandler() {
    Node tryBlock = new Node(Token.BLOCK);
    Node catchBody = new Node(Token.BLOCK);
    Node catchNode = new Node(Token.CATCH, Node.newString(Token.NAME, "e"), catchBody);
    Node catchBlock = new Node(Token.BLOCK, catchNode);
    Node tryNode = new Node(Token.TRY, tryBlock, catchBlock);
    Node script = new Node(Token.SCRIPT, tryNode);

    Node stmt = new Node(Token.EXPR_RESULT, new Node(Token.NUMBER));
    tryBlock.addChildToFront(stmt);

    Node handler = ControlFlowAnalysis.getExceptionHandler(stmt);
    assertNotNull(handler);
    assertEquals(Token.CATCH, handler.getType());

    Node outsideStmt = new Node(Token.EXPR_RESULT, new Node(Token.NUMBER));
    script.addChildToBack(outsideStmt);
    assertNull(ControlFlowAnalysis.getExceptionHandler(outsideStmt));
  }

  @Test
  public void testNodeComparator() {
    String js = "var a = 1; var b = 2;";
    ControlFlowGraph<Node> cfg = createCfg(js, true);

    Comparator<DiGraphNode<Node, Branch>> fwdComp = cfg.getOptionalNodeComparator(true);
    assertNotNull(fwdComp);

    Comparator<DiGraphNode<Node, Branch>> bwdComp = cfg.getOptionalNodeComparator(false);
    assertNotNull(bwdComp);

    List<DiGraphNode<Node, Branch>> nodes = cfg.getDirectedGraphNodes();
    if (nodes.size() >= 2) {
      DiGraphNode<Node, Branch> n1 = nodes.get(0);
      DiGraphNode<Node, Branch> n2 = nodes.get(1);
      int fwdResult = fwdComp.compare(n1, n2);
      int bwdResult = bwdComp.compare(n1, n2);
      assertEquals(fwdResult, -bwdResult);
    }
  }

  @Test(expected = IllegalStateException.class)
  public void testBreakWithoutTargetThrows() {
    Node breakNode = new Node(Token.BREAK);
    Node root = new Node(Token.BLOCK, breakNode);
    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, true, true);
    cfa.process(null, root);
  }

  @Test
  public void testBreakWithoutTargetInIdeMode() {
    Compiler ideCompiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.ideMode = true;
    ideCompiler.initOptions(options);

    Node breakNode = new Node(Token.BREAK);
    Node root = new Node(Token.BLOCK, breakNode);
    ControlFlowAnalysis cfa = new ControlFlowAnalysis(ideCompiler, true, true);
    cfa.process(null, root);
    assertNotNull(cfa.getCfg());
  }
}