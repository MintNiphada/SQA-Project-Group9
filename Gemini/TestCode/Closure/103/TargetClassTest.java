package com.google.javascript.jscomp;

import com.google.javascript.jscomp.ControlFlowGraph.Branch;
import com.google.javascript.jscomp.graph.DiGraph.DiGraphNode;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Test;

import java.util.Comparator;

public class TargetClassTest {

  private ControlFlowGraph<Node> createAndProcessCfa(String js, boolean traverseFunctions) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(js);
    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, traverseFunctions);
    cfa.process(null, root);
    return cfa.getCfg();
  }

  @Test
  public void testIsBreakStructure() {
    Assert.assertTrue(ControlFlowAnalysis.isBreakStructure(new Node(Token.FOR), false));
    Assert.assertTrue(ControlFlowAnalysis.isBreakStructure(new Node(Token.DO), false));
    Assert.assertTrue(ControlFlowAnalysis.isBreakStructure(new Node(Token.WHILE), false));
    Assert.assertTrue(ControlFlowAnalysis.isBreakStructure(new Node(Token.SWITCH), false));

    Assert.assertFalse(ControlFlowAnalysis.isBreakStructure(new Node(Token.BLOCK), false));
    Assert.assertTrue(ControlFlowAnalysis.isBreakStructure(new Node(Token.BLOCK), true));
    Assert.assertFalse(ControlFlowAnalysis.isBreakStructure(new Node(Token.IF), false));
    Assert.assertTrue(ControlFlowAnalysis.isBreakStructure(new Node(Token.IF), true));
    Assert.assertFalse(ControlFlowAnalysis.isBreakStructure(new Node(Token.TRY), false));
    Assert.assertTrue(ControlFlowAnalysis.isBreakStructure(new Node(Token.TRY), true));

    Assert.assertFalse(ControlFlowAnalysis.isBreakStructure(new Node(Token.FUNCTION), true));
    Assert.assertFalse(ControlFlowAnalysis.isBreakStructure(new Node(Token.VAR), false));
  }

  @Test
  public void testIsContinueStructure() {
    Assert.assertTrue(ControlFlowAnalysis.isContinueStructure(new Node(Token.FOR)));
    Assert.assertTrue(ControlFlowAnalysis.isContinueStructure(new Node(Token.DO)));
    Assert.assertTrue(ControlFlowAnalysis.isContinueStructure(new Node(Token.WHILE)));

    Assert.assertFalse(ControlFlowAnalysis.isContinueStructure(new Node(Token.SWITCH)));
    Assert.assertFalse(ControlFlowAnalysis.isContinueStructure(new Node(Token.BLOCK)));
    Assert.assertFalse(ControlFlowAnalysis.isContinueStructure(new Node(Token.FUNCTION)));
  }

  @Test
  public void testSimpleIfElse() {
    String js = "if (x) { a(); } else { b(); }";
    ControlFlowGraph<Node> cfg = createAndProcessCfa(js, true);
    Assert.assertNotNull(cfg);
    Assert.assertNotNull(cfg.getEntry());
    Assert.assertNotNull(cfg.getImplicitReturn());
  }

  @Test
  public void testIfWithoutElse() {
    String js = "if (x) { a(); } c();";
    ControlFlowGraph<Node> cfg = createAndProcessCfa(js, true);
    Assert.assertNotNull(cfg);
  }

  @Test
  public void testWhileLoop() {
    String js = "while (x) { a(); }";
    ControlFlowGraph<Node> cfg = createAndProcessCfa(js, true);
    Assert.assertNotNull(cfg);
  }

  @Test
  public void testDoWhileLoop() {
    String js = "do { a(); } while (x);";
    ControlFlowGraph<Node> cfg = createAndProcessCfa(js, true);
    Assert.assertNotNull(cfg);
  }

  @Test
  public void testForLoopStandard() {
    String js = "for (var i = 0; i < 10; i++) { a(); }";
    ControlFlowGraph<Node> cfg = createAndProcessCfa(js, true);
    Assert.assertNotNull(cfg);
  }

  @Test
  public void testForInLoop() {
    String js = "for (var key in obj) { a(key); }";
    ControlFlowGraph<Node> cfg = createAndProcessCfa(js, true);
    Assert.assertNotNull(cfg);
  }

  @Test
  public void testSwitchStatement() {
    String js = "switch (x) {"
        + "  case 1: a(); break;"
        + "  case 2: b();"
        + "  default: c();"
        + "}";
    ControlFlowGraph<Node> cfg = createAndProcessCfa(js, true);
    Assert.assertNotNull(cfg);
  }

  @Test
  public void testSwitchNoDefault() {
    String js = "switch (x) {"
        + "  case 1: a();"
        + "  case 2: b();"
        + "}";
    ControlFlowGraph<Node> cfg = createAndProcessCfa(js, true);
    Assert.assertNotNull(cfg);
  }

  @Test
  public void testSwitchEmpty() {
    String js = "switch (x) {}";
    ControlFlowGraph<Node> cfg = createAndProcessCfa(js, true);
    Assert.assertNotNull(cfg);
  }

  @Test
  public void testSwitchDefaultOnly() {
    String js = "switch (x) { default: foo(); }";
    ControlFlowGraph<Node> cfg = createAndProcessCfa(js, true);
    Assert.assertNotNull(cfg);
  }

  @Test
  public void testTryCatchFinally() {
    String js = "try { a(); } catch (e) { b(e); } finally { c(); }";
    ControlFlowGraph<Node> cfg = createAndProcessCfa(js, true);
    Assert.assertNotNull(cfg);
  }

  @Test
  public void testTryFinallyOnly() {
    String js = "try { a(); } finally { c(); }";
    ControlFlowGraph<Node> cfg = createAndProcessCfa(js, true);
    Assert.assertNotNull(cfg);
  }

  @Test
  public void testNestedTryFinallyWithBreakAndContinue() {
    String js = "while(x) {\n"
        + "  try {\n"
        + "    try {\n"
        + "      break;\n"
        + "    } catch (a) {\n"
        + "    } finally {\n"
        + "      foo();\n"
        + "    }\n"
        + "    fooFollow();\n"
        + "  } catch (b) {\n"
        + "  } finally {\n"
        + "    bar();\n"
        + "  }\n"
        + "  barFollow();\n"
        + "}\n"
        + "END();";
    ControlFlowGraph<Node> cfg = createAndProcessCfa(js, true);
    Assert.assertNotNull(cfg);
  }

  @Test
  public void testNestedTryWithContinue() {
    String js = "for (var i = 0; i < 10; i++) {\n"
        + "  try {\n"
        + "    try {\n"
        + "      continue;\n"
        + "    } finally {\n"
        + "      foo();\n"
        + "    }\n"
        + "  } finally {\n"
        + "    bar();\n"
        + "  }\n"
        + "}";
    ControlFlowGraph<Node> cfg = createAndProcessCfa(js, true);
    Assert.assertNotNull(cfg);
  }

  @Test
  public void testLabeledBreakAndContinue() {
    String js = "OUTER: for (var i = 0; i < 10; i++) {\n"
        + "  INNER: while (true) {\n"
        + "    if (i == 1) continue OUTER;\n"
        + "    if (i == 2) break INNER;\n"
        + "    if (i == 3) break OUTER;\n"
        + "  }\n"
        + "}";
    ControlFlowGraph<Node> cfg = createAndProcessCfa(js, true);
    Assert.assertNotNull(cfg);
  }

  @Test
  public void testReturnInsideTryFinally() {
    String js = "function f() {\n"
        + "  try {\n"
        + "    return 1;\n"
        + "  } finally {\n"
        + "    cleanup();\n"
        + "  }\n"
        + "}";
    ControlFlowGraph<Node> cfg = createAndProcessCfa(js, true);
    Assert.assertNotNull(cfg);
  }

  @Test
  public void testReturnNoValue() {
    String js = "function f() { return; }";
    ControlFlowGraph<Node> cfg = createAndProcessCfa(js, true);
    Assert.assertNotNull(cfg);
  }

  @Test
  public void testThrowStatement() {
    String js = "try { throw new Error('msg'); } catch (e) { throw e; }";
    ControlFlowGraph<Node> cfg = createAndProcessCfa(js, true);
    Assert.assertNotNull(cfg);
  }

  @Test
  public void testWithStatement() {
    String js = "with (obj) { prop = 1; }";
    ControlFlowGraph<Node> cfg = createAndProcessCfa(js, true);
    Assert.assertNotNull(cfg);
  }

  @Test
  public void testFunctionTraverseTrueVsFalse() {
    String js = "var x = 1; function f() { var y = 2; return y; } f();";
    ControlFlowGraph<Node> cfgTrue = createAndProcessCfa(js, true);
    Assert.assertNotNull(cfgTrue);

    ControlFlowGraph<Node> cfgFalse = createAndProcessCfa(js, false);
    Assert.assertNotNull(cfgFalse);
  }

  @Test
  public void testExpressionExceptions() {
    String js = "try {\n"
        + "  a.b = 1;\n"
        + "  c[d] = 2;\n"
        + "  e++;\n"
        + "  --f;\n"
        + "  new Foo();\n"
        + "} catch(ex) {}\n";
    ControlFlowGraph<Node> cfg = createAndProcessCfa(js, true);
    Assert.assertNotNull(cfg);
  }

  @Test
  public void testSyntheticBlockAndComparator() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("var a = 1; var b = 2; var c = 3;");
    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, true);
    cfa.process(null, root);
    ControlFlowGraph<Node> cfg = cfa.getCfg();

    Comparator<DiGraphNode<Node, Branch>> fwdComp = cfg.getOptionalNodeComparator(true);
    Comparator<DiGraphNode<Node, Branch>> revComp = cfg.getOptionalNodeComparator(false);
    Assert.assertNotNull(fwdComp);
    Assert.assertNotNull(revComp);

    DiGraphNode<Node, Branch> entry = cfg.getEntry();
    DiGraphNode<Node, Branch> implicitReturn = cfg.getImplicitReturn();

    Assert.assertTrue(fwdComp.compare(entry, implicitReturn) <= 0);
    Assert.assertTrue(revComp.compare(entry, implicitReturn) >= 0);
  }

  @Test
  public void testUnreachableCodePrioritization() {
    String js = "function f() { return 1; var unreachable = 2; }";
    ControlFlowGraph<Node> cfg = createAndProcessCfa(js, true);
    Assert.assertNotNull(cfg);
  }
}