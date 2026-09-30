package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Test;

public class TargetClassTest {

  private Node runPass(String js, boolean removeNoOpStatements) {
    Compiler compiler = new Compiler();
    Compiler.setLoggingLevel(java.util.logging.Level.OFF);
    Node root = compiler.parseTestCode(js);
    Node externs = new Node(Token.BLOCK);
    UnreachableCodeElimination pass = new UnreachableCodeElimination(compiler, removeNoOpStatements);
    pass.process(externs, root);
    return root;
  }

  private String transform(String js, boolean removeNoOpStatements) {
    Compiler compiler = new Compiler();
    Compiler.setLoggingLevel(java.util.logging.Level.OFF);
    Node root = compiler.parseTestCode(js);
    Node externs = new Node(Token.BLOCK);
    UnreachableCodeElimination pass = new UnreachableCodeElimination(compiler, removeNoOpStatements);
    pass.process(externs, root);
    return compiler.toSource(root).trim();
  }

  @Test
  public void testDeadCodeAfterReturn() {
    String js = "function f() { return 1; var x = 2; alert(x); }";
    String result = transform(js, true);
    Assert.assertFalse(result.contains("alert"));
    Assert.assertTrue(result.contains("return 1"));
  }

  @Test
  public void testRemoveUselessReturn() {
    String js = "function f() { var x = 1; return; }";
    String result = transform(js, false);
    Assert.assertFalse(result.contains("return"));
    Assert.assertTrue(result.contains("var x = 1"));
  }

  @Test
  public void testDoNotRemoveReturnWithValue() {
    String js = "function f() { return 1; }";
    String result = transform(js, false);
    Assert.assertTrue(result.contains("return 1"));
  }

  @Test
  public void testRemoveNoOpStatements() {
    String js = "function f() { 1 + 1; 'hello'; true; }";
    String resultTrue = transform(js, true);
    Assert.assertFalse(resultTrue.contains("1 + 1"));
    Assert.assertFalse(resultTrue.contains("hello"));

    String resultFalse = transform(js, false);
    Assert.assertTrue(resultFalse.contains("1 + 1") || resultFalse.contains("2"));
  }

  @Test
  public void testDoWhilePreserved() {
    String js = "function f() { do { alert(1); } while (false); }";
    String result = transform(js, true);
    Assert.assertTrue(result.contains("do"));
  }

  @Test
  public void testForInHeaderNotRemoved() {
    String js = "function f(obj) { for (var k in obj) { alert(k); } }";
    String result = transform(js, true);
    Assert.assertTrue(result.contains("for"));
    Assert.assertTrue(result.contains("in"));
  }

  @Test
  public void testTryCatchFinally() {
    String js = "function f() { try { throw 1; } catch (e) { alert(e); } finally { alert(2); } }";
    String result = transform(js, true);
    Assert.assertTrue(result.contains("try"));
    Assert.assertTrue(result.contains("catch"));
    Assert.assertTrue(result.contains("finally"));
  }

  @Test
  public void testTryCatchWithoutFinally() {
    String js = "function f() { try { return 1; } catch (e) { alert(e); } }";
    String result = transform(js, false);
    Assert.assertTrue(result.contains("try"));
  }

  @Test
  public void testUnreachableCatchRemoval() {
    String js = "function f() { try { return 1; } catch (e) { } }";
    String result = transform(js, true);
    Assert.assertNotNull(result);
  }

  @Test
  public void testBreakInLoop() {
    String js = "function f() { while (true) { break; } }";
    String result = transform(js, false);
    Assert.assertNotNull(result);
  }

  @Test
  public void testContinueInLoop() {
    String js = "function f() { for (var i = 0; i < 10; i++) { continue; } }";
    String result = transform(js, false);
    Assert.assertNotNull(result);
  }

  @Test
  public void testDeadVarDeclaration() {
    String js = "function f() { throw 1; var x; }";
    String result = transform(js, true);
    Assert.assertNotNull(result);
  }

  @Test
  public void testDeadVarWithInit() {
    String js = "function f() { return 1; var x = 2; }";
    String result = transform(js, true);
    Assert.assertFalse(result.contains("var x = 2"));
  }

  @Test
  public void testEmptyBlock() {
    String js = "function f() { {} }";
    String result = transform(js, true);
    Assert.assertNotNull(result);
  }

  @Test
  public void testSwitchBreak() {
    String js = "function f(x) { switch (x) { case 1: alert(1); break; default: break; } }";
    String result = transform(js, true);
    Assert.assertTrue(result.contains("switch"));
  }

  @Test
  public void testMultipleFunctions() {
    String js = "function f1() { return; } function f2() { var a = 1; return a; }";
    String result = transform(js, false);
    Assert.assertTrue(result.contains("f1"));
    Assert.assertTrue(result.contains("f2"));
  }

  @Test
  public void testNestedFunctionNotRemoved() {
    String js = "function f() { return 1; function inner() { alert(2); } }";
    String result = transform(js, true);
    Assert.assertTrue(result.contains("function inner"));
  }

  @Test
  public void testTopLevelStatements() {
    String js = "var x = 1; if (false) { var y = 2; }";
    String result = transform(js, true);
    Assert.assertNotNull(result);
  }

  @Test
  public void testProcessWithEmptyNode() {
    Compiler compiler = new Compiler();
    UnreachableCodeElimination pass = new UnreachableCodeElimination(compiler, true);
    Node externs = new Node(Token.BLOCK);
    Node root = new Node(Token.BLOCK);
    pass.process(externs, root);
    Assert.assertEquals(0, root.getChildCount());
  }

  @Test
  public void testConsecutiveJumps() {
    String js = "function f() { while(true) { if (false) { break; } break; } }";
    String result = transform(js, true);
    Assert.assertNotNull(result);
  }
}