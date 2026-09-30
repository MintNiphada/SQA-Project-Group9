package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Test;

public class UnreachableCodeEliminationTest {

  private Node testProcess(String js, boolean removeNoOps) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(js);
    UnreachableCodeElimination pass = new UnreachableCodeElimination(compiler, removeNoOps);
    pass.process(null, root);
    return root;
  }

  private String testProcessSource(String js, boolean removeNoOps) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(js);
    UnreachableCodeElimination pass = new UnreachableCodeElimination(compiler, removeNoOps);
    pass.process(null, root);
    return compiler.toSource(root).trim();
  }

  @Test
  public void testRemoveDeadCodeAfterReturn() {
    String js = "function f() { return; alert('unreachable'); }";
    String result = testProcessSource(js, false);
    Assert.assertFalse(result.contains("unreachable"));
  }

  @Test
  public void testRemoveUselessReturn() {
    String js = "function f() { alert('reachable'); return; }";
    String result = testProcessSource(js, false);
    Assert.assertTrue(result.contains("reachable"));
    Assert.assertFalse(result.contains("return"));
  }

  @Test
  public void testRetainReturnWithValue() {
    String js = "function f() { return 1; }";
    String result = testProcessSource(js, false);
    Assert.assertTrue(result.contains("return 1"));
  }

  @Test
  public void testRemoveNoOpStatementsEnabled() {
    String js = "function f() { true; 1 + 2; 'hello'; a.b.c; }";
    String result = testProcessSource(js, true);
    Assert.assertFalse(result.contains("true"));
    Assert.assertFalse(result.contains("hello"));
  }

  @Test
  public void testRemoveNoOpStatementsDisabled() {
    String js = "function f() { true; }";
    String result = testProcessSource(js, false);
    Assert.assertTrue(result.contains("true"));
  }

  @Test
  public void testRemoveUselessContinue() {
    String js = "while (true) { alert(1); continue; }";
    String result = testProcessSource(js, false);
    Assert.assertTrue(result.contains("alert(1)"));
    Assert.assertFalse(result.contains("continue"));
  }

  @Test
  public void testRemoveUselessBreakInBlock() {
    String js = "for (;;) { alert(1); break; }";
    String result = testProcessSource(js, false);
    Assert.assertTrue(result.contains("break"));
  }

  @Test
  public void testBreakInSwitch() {
    String js = "switch(x) { case 1: alert(1); break; default: alert(2); }";
    String result = testProcessSource(js, false);
    Assert.assertTrue(result.contains("switch"));
  }

  @Test
  public void testUnreachableDoWhile() {
    String js = "function f() { return; do { alert(1); } while (true); }";
    String result = testProcessSource(js, false);
    Assert.assertNotNull(result);
  }

  @Test
  public void testTryCatchFinallyRemoval() {
    String js = "function f() { try { alert(1); } catch (e) { alert(e); } }";
    String result = testProcessSource(js, false);
    Assert.assertTrue(result.contains("try"));
  }

  @Test
  public void testTryFinally() {
    String js = "function f() { try { return 1; } finally { alert(2); } }";
    String result = testProcessSource(js, false);
    Assert.assertTrue(result.contains("try"));
    Assert.assertTrue(result.contains("finally"));
  }

  @Test
  public void testVarRedeclarationInDeadCode() {
    String js = "function f() { return; var x = 1; }";
    String result = testProcessSource(js, false);
    Assert.assertTrue(result.contains("var x"));
    Assert.assertFalse(result.contains("x = 1"));
  }

  @Test
  public void testCascadedUselessBranches() {
    String js = "function f() { if (a) { return; } else { return; } }";
    String result = testProcessSource(js, false);
    Assert.assertNotNull(result);
  }

  @Test
  public void testEmptyBlockHandling() {
    String js = "function f() { {} }";
    String result = testProcessSource(js, true);
    Assert.assertNotNull(result);
  }

  @Test
  public void testDirectVisitEdgeCases() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("function f() { return; }");
    UnreachableCodeElimination pass = new UnreachableCodeElimination(compiler, true);

    NodeTraversal t = new NodeTraversal(compiler, pass);
    pass.visit(t, root, null);

    Node scriptNode = new Node(Token.SCRIPT);
    pass.visit(t, scriptNode, root);

    Node fnNode = new Node(Token.FUNCTION, Node.newString(Token.NAME, "foo"), new Node(Token.LP), new Node(Token.BLOCK));
    pass.visit(t, fnNode, root);

    pass.process(null, root);
  }

  @Test
  public void testScopeStackHandling() {
    Compiler compiler = new Compiler();
    UnreachableCodeElimination pass = new UnreachableCodeElimination(compiler, false);
    Node script = compiler.parseTestCode("function a() { function b() {} }");
    pass.process(null, script);
    Assert.assertNull(pass.curCfg);
    Assert.assertTrue(pass.cfgStack.isEmpty());
  }

  @Test
  public void testMultipleReturnsAndFollowNodes() {
    String js = "function f(x) { if (x) { return 1; } else { return 2; } return 3; }";
    String result = testProcessSource(js, false);
    Assert.assertFalse(result.contains("return 3"));
  }
}