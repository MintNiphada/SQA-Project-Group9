package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.JSDocInfoBuilder;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for {@link CheckSideEffects}.
 */
public class CheckSideEffectsTest {

  private TestCompiler compiler;

  @Before
  public void setUp() {
    compiler = new TestCompiler();
  }

  private CheckSideEffects createPass(boolean protect) {
    return new CheckSideEffects(compiler, CheckLevel.WARNING, protect);
  }

  private void runProcess(Node root, boolean protect) {
    createPass(protect).process(null, root);
  }

  private void runHotSwap(Node root) {
    createPass(false).hotSwapScript(root, null);
  }

  private static Node createFor(Node init, Node cond, Node iter, Node body) {
    Node forNode = new Node(Token.FOR);
    forNode.addChildToBack(init);
    forNode.addChildToBack(cond);
    forNode.addChildToBack(iter);
    forNode.addChildToBack(body);
    return forNode;
  }

  private static class TestCompiler extends Compiler {
    private final List<JSError> reports = new ArrayList<>();
    private int codeChangeCount = 0;

    @Override
    public void report(JSError error) {
      reports.add(error);
    }

    @Override
    public void reportCodeChange() {
      codeChangeCount++;
    }
  }

  @Test
  public void testVisitEmptyNode() {
    Node root = IR.block();
    root.addChildToBack(new Node(Token.EMPTY));
    runProcess(root, false);
    assertTrue(compiler.reports.isEmpty());
  }

  @Test
  public void testVisitCommaNode() {
    Node root = IR.exprResult(new Node(Token.COMMA));
    runProcess(root, false);
    assertTrue(compiler.reports.isEmpty());
  }

  @Test
  public void testVisitParentNull() {
    Node root = IR.script();
    runProcess(root, false);
    assertTrue(compiler.reports.isEmpty());
  }

  @Test
  public void testVisitExprResultNode() {
    Node root = IR.exprResult(IR.call(IR.name("foo")));
    runProcess(root, false);
    assertTrue(compiler.reports.isEmpty());
  }

  @Test
  public void testVisitQualifiedNameWithJSDoc() {
    Node name = IR.name("foo");
    JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
    builder.recordNoAlias();
    name.setJSDocInfo(builder.build(name));
    Node root = IR.exprResult(name);
    runProcess(root, false);
    assertTrue(compiler.reports.isEmpty());
  }

  @Test
  public void testVisitCommaResultUsed() {
    Node comma = new Node(Token.COMMA);
    comma.addChildToBack(IR.number(0));
    comma.addChildToBack(IR.string("x"));
    Node var = IR.var(IR.name("a"), comma);
    runProcess(var, false);
    assertTrue(compiler.reports.isEmpty());
  }

  @Test
  public void testVisitCommaLastChildAllowedAncestor() {
    Node comma = new Node(Token.COMMA);
    comma.addChildToBack(IR.call(IR.name("foo")));
    comma.addChildToBack(IR.string("x"));
    Node root = IR.exprResult(comma);
    runProcess(root, false);
    assertEquals(1, compiler.reports.size());
    JSError error = compiler.reports.get(0);
    assertEquals("Is there a missing '+' on the previous line?", error.description);
  }

  @Test
  public void testVisitCommaLastChildDisallowedAncestor() {
    Node comma = new Node(Token.COMMA);
    comma.addChildToBack(IR.call(IR.name("foo")));
    comma.addChildToBack(IR.string("x"));
    Node forNode = createFor(comma, new Node(Token.EMPTY), new Node(Token.EMPTY), IR.block());
    runProcess(forNode, false);
    assertTrue(compiler.reports.isEmpty());
  }

  @Test
  public void testVisitParentNotExprResultOrBlockAllowedForInit() {
    Node init = IR.string("init");
    Node forNode = createFor(init, new Node(Token.EMPTY), new Node(Token.EMPTY), IR.block());
    runProcess(forNode, false);
    assertEquals(1, compiler.reports.size());
    JSError error = compiler.reports.get(0);
    assertEquals("Is there a missing '+' on the previous line?", error.description);
  }

  @Test
  public void testVisitParentNotExprResultOrBlockAllowedForIter() {
    Node iter = IR.string("iter");
    Node forNode = createFor(new Node(Token.EMPTY), new Node(Token.EMPTY), iter, IR.block());
    runProcess(forNode, false);
    assertEquals(1, compiler.reports.size());
    JSError error = compiler.reports.get(0);
    assertEquals("Is there a missing '+' on the previous line?", error.description);
  }

  @Test
  public void testVisitParentNotExprResultOrBlockDisallowedForBody() {
    Node body = IR.string("body");
    Node forNode = createFor(new Node(Token.EMPTY), new Node(Token.EMPTY), new Node(Token.EMPTY), body);
    runProcess(forNode, false);
    assertTrue(compiler.reports.isEmpty());
  }

  @Test
  public void testVisitParentNotExprResultOrBlockDisallowedForIf() {
    Node condition = IR.string("cond");
    Node ifNode = new Node(Token.IF, condition, IR.block());
    runProcess(ifNode, false);
    assertTrue(compiler.reports.isEmpty());
  }

  @Test
  public void testVisitSimpleOperator() {
    Node plus = new Node(Token.PLUS, IR.number(1), IR.number(2));
    Node root = IR.exprResult(plus);
    runProcess(root, false);
    assertEquals(1, compiler.reports.size());
    JSError error = compiler.reports.get(0);
    assertTrue(error.description.contains("plus"));
  }

  @Test
  public void testVisitString() {
    Node root = IR.exprResult(IR.string("hello"));
    runProcess(root, false);
    assertEquals(1, compiler.reports.size());
    JSError error = compiler.reports.get(0);
    assertEquals("Is there a missing '+' on the previous line?", error.description);
  }

  @Test
  public void testVisitNumber() {
    Node root = IR.exprResult(IR.number(42));
    runProcess(root, false);
    assertEquals(1, compiler.reports.size());
    JSError error = compiler.reports.get(0);
    assertEquals("This code lacks side-effects. Is there a bug?", error.description);
  }

  @Test
  public void testVisitStatementNotAddedToProblemNodes() {
    Node var = IR.var(IR.name("x"));
    Node block = IR.block();
    block.addChildToBack(var);
    runProcess(block, true);
    // A report is generated, but VAR is a statement so not added to problemNodes.
    assertEquals(1, compiler.reports.size());
    // Since problemNodes is empty, protectSideEffects should not change code.
    assertEquals(0, compiler.codeChangeCount);
    // Root should not be modified (no protector call).
    assertTrue(block.getFirstChild().isVar());
  }

  @Test
  public void testProcessWithProtect() {
    Node stringNode = IR.string("protected");
    Node root = IR.exprResult(stringNode);
    runProcess(root, true);
    assertEquals(1, compiler.reports.size());
    // Two code changes: one from addExtern, one from protectSideEffects.
    assertEquals(2, compiler.codeChangeCount);
    // The string node should now be wrapped in a call to JSCOMPILER_PRESERVE.
    Node exprResult = root.getFirstChild();
    Node call = exprResult.getFirstChild();
    assertNotNull(call);
    assertTrue(call.isCall());
    Node target = call.getFirstChild();
    assertTrue(target.isName());
    assertEquals(CheckSideEffects.PROTECTOR_FN, target.getString());
    // The last child of the call should be the original string.
    assertEquals(stringNode, call.getLastChild());
  }

  @Test
  public void testProcessWithoutProtect() {
    Node root = IR.exprResult(IR.string("unprotected"));
    runProcess(root, false);
    assertEquals(1, compiler.reports.size());
    assertEquals(0, compiler.codeChangeCount);
    // Root remains unchanged.
    assertTrue(root.getFirstChild().getFirstChild().isString());
  }

  @Test
  public void testHotSwapScript() {
    Node root = IR.exprResult(IR.string("hot"));
    runHotSwap(root);
    assertEquals(1, compiler.reports.size());
    assertEquals(0, compiler.codeChangeCount);
  }

  @Test
  public void testProtectSideEffectsEmptyProblemNodes() {
    Node root = IR.block();
    runProcess(root, true);
    assertEquals(0, compiler.codeChangeCount);
  }

  @Test
  public void testStripProtectionProcess() {
    Node call = IR.call(IR.name(CheckSideEffects.PROTECTOR_FN), IR.string("value"));
    Node root = IR.exprResult(call);
    CheckSideEffects.StripProtection strip = new CheckSideEffects.StripProtection(compiler);
    strip.process(null, root);
    // The call should be replaced by the string child.
    Node exprResult = root.getFirstChild();
    assertTrue(exprResult.getFirstChild().isString());
    assertEquals("value", exprResult.getFirstChild().getString());
    // The original call node should have no children (detached).
    assertEquals(0, call.getChildCount());
  }

  @Test
  public void testStripProtectionVisitNonCall() {
    Node name = IR.name("foo");
    CheckSideEffects.StripProtection strip = new CheckSideEffects.StripProtection(compiler);
    NodeTraversal.traverse(compiler, name, strip);
    // No exception, name unchanged.
    assertTrue(name.isName());
  }

  @Test
  public void testStripProtectionVisitCallNotProtector() {
    Node call = IR.call(IR.name("other"), IR.string("value"));
    Node root = IR.exprResult(call);
    CheckSideEffects.StripProtection strip = new CheckSideEffects.StripProtection(compiler);
    strip.process(null, root);
    // The call remains.
    assertTrue(root.getFirstChild().getFirstChild().isCall());
    assertEquals(call, root.getFirstChild().getFirstChild());
  }

  @Test
  public void testStripProtectionVisitCallProtector() {
    Node expr = IR.string("value");
    Node call = IR.call(IR.name(CheckSideEffects.PROTECTOR_FN), expr);
    Node root = IR.exprResult(call);
    CheckSideEffects.StripProtection strip = new CheckSideEffects.StripProtection(compiler);
    strip.process(null, root);
    // The call is replaced by expr.
    assertEquals(expr, root.getFirstChild().getFirstChild());
    // The call has no children.
    assertEquals(0, call.getChildCount());
  }
}
