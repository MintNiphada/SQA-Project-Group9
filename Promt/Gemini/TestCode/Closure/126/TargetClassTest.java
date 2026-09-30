package com.google.javascript.jscomp;

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TargetClassTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  private void test(String js, String expected) {
    Compiler compActual = new Compiler();
    Node rootActual = compActual.parseTestCode(js);
    MinimizeExitPoints pass = new MinimizeExitPoints(compActual);
    pass.process(null, rootActual);

    Compiler compExpected = new Compiler();
    Node rootExpected = compExpected.parseTestCode(expected);

    String actualSource = compActual.toSource(rootActual);
    String expectedSource = compExpected.toSource(rootExpected);
    Assert.assertEquals(expectedSource, actualSource);
  }

  private void testSame(String js) {
    test(js, js);
  }

  @Test
  public void testSimpleFunctionReturn() {
    test("function f() { return; }", "function f() {}");
    test("function f() { var x = 1; return; }", "function f() { var x = 1; }");
    testSame("function f() { return 1; }");
  }

  @Test
  public void testIfReturn() {
    test("function f() { if (x) return; else return; }",
         "function f() { if (x); else; }");
    test("function f() { if (x) { return; } }",
         "function f() { if (x) {} }");
    test("function f() { if (x) { a(); return; } b(); }",
         "function f() { if (x) { a(); } else { b(); } }");
    test("function f() { if (x) return; a(); }",
         "function f() { if (x); else { a(); } }");
    test("function f() { if (x) { a(); } else { b(); return; } c(); }",
         "function f() { if (x) { a(); c(); } else { b(); } }");
    test("function f() { if (x) { a(); return; } else { b(); } c(); }",
         "function f() { if (x) { a(); } else { b(); c(); } }");
  }

  @Test
  public void testIfReturnWithFunctionDeclaration() {
    test("function f() { if (x) return; function g() {} a(); }",
         "function f() { if (x); else { function g() {} a(); } }");
  }

  @Test
  public void testMultipleIfReturns() {
    test("function f() { if (x) return; if (y) return; z(); }",
         "function f() { if (x); else { if (y); else { z(); } } }");
  }

  @Test
  public void testWhileLoopContinue() {
    test("while (x) { continue; }", "while (x) {}");
    test("while (x) { a(); continue; }", "while (x) { a(); }");
    test("while (x) { if (y) continue; a(); }",
         "while (x) { if (y); else { a(); } }");
    testSame("while (x) { continue foo; }");
  }

  @Test
  public void testForLoopContinue() {
    test("for (var i = 0; i < 10; i++) { continue; }",
         "for (var i = 0; i < 10; i++) {}");
    test("for (;;) { if (x) continue; a(); }",
         "for (;;) { if (x); else { a(); } }");
  }

  @Test
  public void testDoWhileLoop() {
    test("do { continue; } while (x);", "do {} while (x);");
    test("do { if (y) continue; a(); } while (x);",
         "do { if (y); else { a(); } } while (x);");
    test("do { break; } while (false);", "do {} while (false);");
    test("do { if (y) break; a(); } while (false);",
         "do { if (y); else { a(); } } while (false);");
    testSame("do { break; } while (true);");
    testSame("do { break; } while (x);");
  }

  @Test
  public void testLabelBreak() {
    test("foo: { break foo; }", "foo: {}");
    test("foo: { a(); break foo; }", "foo: { a(); }");
    test("foo: { if (x) break foo; a(); }",
         "foo: { if (x); else { a(); } }");
    testSame("foo: { break bar; }");
    testSame("foo: { break; }");
  }

  @Test
  public void testTryCatchFinally() {
    test("function f() { try { return; } catch (e) { return; } finally { return; } }",
         "function f() { try {} catch (e) {} finally {} }");
    test("function f() { try { if (x) return; a(); } catch (e) { if (y) return; b(); } finally { if (z) return; c(); } }",
         "function f() { try { if (x); else { a(); } } catch (e) { if (y); else { b(); } } finally { if (z); else { c(); } } }");
    test("function f() { try { return; } finally { return; } }",
         "function f() { try {} finally {} }");
  }

  @Test
  public void testNestedBlocksAndEmptyBlocks() {
    test("function f() { {} }", "function f() {}");
    test("function f() { ; }", "function f() {}");
    test("function f() { if (x) {} }", "function f() { if (x) {} }");
  }

  @Test
  public void testDirectTryMinimizeExitsEdgeCases() {
    MinimizeExitPoints pass = new MinimizeExitPoints(compiler);

    Node nonBlock = IR.exprResult(IR.number(1));
    pass.tryMinimizeExits(nonBlock, Token.RETURN, null);

    Node emptyBlock = IR.block();
    pass.tryMinimizeExits(emptyBlock, Token.RETURN, null);

    Node blockWithEmptyChild = IR.block(IR.empty());
    pass.tryMinimizeExits(blockWithEmptyChild, Token.RETURN, null);

    Node parent = IR.block();
    Node ifNode = IR.ifNode(IR.name("x"), IR.block());
    parent.addChildToBack(ifNode);
    pass.tryMinimizeExits(parent, Token.RETURN, null);
    Assert.assertEquals(1, parent.getChildCount());

    Node ifWithElse = IR.ifNode(IR.name("x"), IR.block(), IR.empty());
    pass.tryMinimizeExits(ifWithElse, Token.RETURN, null);
  }

  @Test
  public void testTryMinimizeIfBlockExitsVariants() {
    MinimizeExitPoints pass = new MinimizeExitPoints(compiler);

    Node parent = IR.block();
    Node ifNode = IR.ifNode(IR.name("cond"), IR.returnNode());
    Node sibling = IR.exprResult(IR.name("a"));
    parent.addChildToBack(ifNode);
    parent.addChildToBack(sibling);

    pass.tryMinimizeExits(parent, Token.RETURN, null);
    Assert.assertNotNull(ifNode.getNext());
  }

  @Test
  public void testTryMinimizeIfBlockExitsWithExistingEmptyDest() {
    MinimizeExitPoints pass = new MinimizeExitPoints(compiler);

    Node parent = IR.block();
    Node ifNode = IR.ifNode(IR.name("cond"), IR.block(IR.returnNode()), IR.empty());
    Node sibling = IR.exprResult(IR.name("a"));
    parent.addChildToBack(ifNode);
    parent.addChildToBack(sibling);

    pass.tryMinimizeExits(parent, Token.RETURN, null);
    Assert.assertNull(ifNode.getNext());
  }

  @Test
  public void testTryMinimizeIfBlockExitsWithExistingBlockDest() {
    MinimizeExitPoints pass = new MinimizeExitPoints(compiler);

    Node parent = IR.block();
    Node ifNode = IR.ifNode(IR.name("cond"), IR.block(IR.returnNode()), IR.block(IR.exprResult(IR.name("b"))));
    Node sibling = IR.exprResult(IR.name("a"));
    parent.addChildToBack(ifNode);
    parent.addChildToBack(sibling);

    pass.tryMinimizeExits(parent, Token.RETURN, null);
    Assert.assertNull(ifNode.getNext());
  }

  @Test
  public void testTryMinimizeIfBlockExitsWithExistingSingleStatementDest() {
    MinimizeExitPoints pass = new MinimizeExitPoints(compiler);

    Node parent = IR.block();
    Node ifNode = IR.ifNode(IR.name("cond"), IR.returnNode(), IR.exprResult(IR.name("b")));
    Node sibling = IR.exprResult(IR.name("a"));
    parent.addChildToBack(ifNode);
    parent.addChildToBack(sibling);

    pass.tryMinimizeExits(parent, Token.RETURN, null);
    Assert.assertNull(ifNode.getNext());
  }
}