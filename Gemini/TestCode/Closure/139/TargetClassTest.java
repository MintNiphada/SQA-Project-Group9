package com.google.javascript.jscomp;

import com.google.javascript.rhino.JSDocInfo;
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

  private Node parseAndNormalize(String js, boolean assertOnChange) {
    Node root = compiler.parseTestCode(js);
    Node externs = new Node(Token.BLOCK);
    Normalize normalizer = new Normalize(compiler, assertOnChange);
    normalizer.process(externs, root);
    return root;
  }

  @Test
  public void testSplitVarDeclarations() {
    String js = "var a = 1, b = 2, c = 3;";
    Node root = parseAndNormalize(js, false);
    Assert.assertEquals(Token.SCRIPT, root.getType());
    Assert.assertEquals(3, root.getChildCount());
    Assert.assertEquals(Token.VAR, root.getFirstChild().getType());
    Assert.assertEquals(Token.VAR, root.getChildAtIndex(1).getType());
    Assert.assertEquals(Token.VAR, root.getLastChild().getType());
  }

  @Test
  public void testConvertWhileToFor() {
    String js = "while (x < 10) { x++; }";
    Node root = parseAndNormalize(js, false);
    Node forNode = root.getFirstChild();
    Assert.assertEquals(Token.FOR, forNode.getType());
    Assert.assertEquals(Token.EMPTY, forNode.getFirstChild().getType());
  }

  @Test
  public void testExtractForInitializer() {
    String js = "for (var i = 0; i < 10; i++) {}";
    Node root = parseAndNormalize(js, false);
    Assert.assertEquals(2, root.getChildCount());
    Assert.assertEquals(Token.VAR, root.getFirstChild().getType());
    Assert.assertEquals(Token.FOR, root.getLastChild().getType());
    Assert.assertEquals(Token.EMPTY, root.getLastChild().getFirstChild().getType());
  }

  @Test
  public void testExtractForExpressionInitializer() {
    String js = "var i; for (i = 0; i < 10; i++) {}";
    Node root = parseAndNormalize(js, false);
    Assert.assertEquals(3, root.getChildCount());
    Assert.assertEquals(Token.VAR, root.getFirstChild().getType());
    Assert.assertEquals(Token.EXPR_RESULT, root.getChildAtIndex(1).getType());
    Assert.assertEquals(Token.FOR, root.getLastChild().getType());
  }

  @Test
  public void testNormalizeLabels() {
    String js = "foo: x = 1;";
    Node root = parseAndNormalize(js, false);
    Node labelNode = root.getFirstChild();
    Assert.assertEquals(Token.LABEL, labelNode.getType());
    Assert.assertEquals(Token.BLOCK, labelNode.getLastChild().getType());
  }

  @Test
  public void testMoveNamedFunctions() {
    String js = "function outer() { var x = 1; function inner() {} }";
    Node root = parseAndNormalize(js, false);
    Node outerFunc = root.getFirstChild();
    Node body = outerFunc.getLastChild();
    Assert.assertEquals(Token.FUNCTION, body.getFirstChild().getType());
    Assert.assertEquals(Token.VAR, body.getLastChild().getType());
  }

  @Test
  public void testRemoveDuplicateVarDeclarations() {
    String js = "var a = 1; var a = 2;";
    Node root = parseAndNormalize(js, false);
    Assert.assertEquals(2, root.getChildCount());
    Assert.assertEquals(Token.VAR, root.getFirstChild().getType());
    Assert.assertEquals(Token.EXPR_RESULT, root.getLastChild().getType());
  }

  @Test
  public void testRemoveDuplicateVarWithoutInit() {
    String js = "var a = 1; var a;";
    Node root = parseAndNormalize(js, false);
    Assert.assertEquals(1, root.getChildCount());
    Assert.assertEquals(Token.VAR, root.getFirstChild().getType());
  }

  @Test
  public void testRemoveDuplicateVarInForIn() {
    String js = "var a; for (var a in b) {}";
    Node root = parseAndNormalize(js, false);
    Node forIn = root.getLastChild();
    Assert.assertEquals(Token.NAME, forIn.getFirstChild().getType());
  }

  @Test
  public void testRemoveDuplicateVarInLabel() {
    String js = "var a; label: var a;";
    Node root = parseAndNormalize(js, false);
    Node labelNode = root.getLastChild();
    Assert.assertEquals(Token.BLOCK, labelNode.getLastChild().getType());
    Assert.assertEquals(Token.EMPTY, labelNode.getLastChild().getFirstChild().getType());
  }

  @Test(expected = IllegalStateException.class)
  public void testAssertOnChangeFailsOnVarChange() {
    parseAndNormalize("var a = 1, b = 2;", true);
  }

  @Test(expected = IllegalStateException.class)
  public void testAssertOnChangeFailsOnWhileChange() {
    parseAndNormalize("while (true) {}", true);
  }

  @Test
  public void testPropagateConstantAnnotations() {
    Node root = compiler.parseTestCode("/** @const */ var CONST_VAL = 1; var use = CONST_VAL;");
    Node externs = new Node(Token.BLOCK);
    Normalize normalizer = new Normalize(compiler, false);
    normalizer.process(externs, root);

    Normalize.PropogateConstantAnnotations prop =
        new Normalize.PropogateConstantAnnotations(compiler, false);
    prop.process(externs, root);

    Node nameNode = root.getLastChild().getFirstChild().getFirstChild();
    Assert.assertTrue(nameNode.getBooleanProp(Node.IS_CONSTANT_NAME));
  }

  @Test
  public void testPropagateConstantAnnotationsEmptyName() {
    Node externs = new Node(Token.BLOCK);
    Node root = new Node(Token.SCRIPT, new Node(Token.NAME, ""));
    Normalize.PropogateConstantAnnotations prop =
        new Normalize.PropogateConstantAnnotations(compiler, false);
    prop.process(externs, root);
  }

  @Test(expected = IllegalStateException.class)
  public void testPropagateConstantAnnotationsAssertOnChange() {
    Node script = compiler.parseTestCode("/** @const */ var CONST_VAL = 1; var use = CONST_VAL;");
    Node externs = new Node(Token.BLOCK);
    Node root = new Node(Token.BLOCK, externs, script);
    NodeTraversal.traverse(compiler, script, new SyntacticScopeCreator(compiler));

    Normalize.PropogateConstantAnnotations prop =
        new Normalize.PropogateConstantAnnotations(compiler, true);
    prop.process(externs, script);
  }

  @Test
  public void testVerifyConstantsPass() {
    Node root = compiler.parseTestCode("var a = 1;");
    Node externs = new Node(Token.BLOCK);
    Node parent = new Node(Token.BLOCK, externs, root);

    Normalize.VerifyConstants verifier = new Normalize.VerifyConstants(compiler, false);
    verifier.process(externs, root);
  }

  @Test
  public void testVerifyConstantsWithUserDeclarations() {
    Node root = compiler.parseTestCode("var a = 1;");
    Node externs = new Node(Token.BLOCK);
    Node parent = new Node(Token.BLOCK, externs, root);

    Normalize.VerifyConstants verifier = new Normalize.VerifyConstants(compiler, true);
    verifier.process(externs, root);
  }

  @Test(expected = IllegalStateException.class)
  public void testVerifyConstantsInconsistent() {
    Node root = compiler.parseTestCode("var a = 1; var a = 2;");
    Node externs = new Node(Token.BLOCK);
    Node parent = new Node(Token.BLOCK, externs, root);

    root.getFirstChild().getFirstChild().putBooleanProp(Node.IS_CONSTANT_NAME, true);
    root.getLastChild().getFirstChild().putBooleanProp(Node.IS_CONSTANT_NAME, false);

    Normalize.VerifyConstants verifier = new Normalize.VerifyConstants(compiler, false);
    verifier.process(externs, root);
  }

  @Test
  public void testNormalizeStatementsCallback() {
    Normalize.NormalizeStatements ns = new Normalize.NormalizeStatements(compiler, false);
    NodeTraversal t = new NodeTraversal(compiler, ns);
    Node root = compiler.parseTestCode("while(false);");
    t.traverse(root);
    Assert.assertEquals(Token.FOR, root.getFirstChild().getType());
  }

  @Test
  public void testConsecutiveFunctionHoisting() {
    String js = "function test() { var a = 1; function f1(){} function f2(){} }";
    Node root = parseAndNormalize(js, false);
    Node body = root.getFirstChild().getLastChild();
    Assert.assertEquals(Token.FUNCTION, body.getFirstChild().getType());
    Assert.assertEquals(Token.FUNCTION, body.getChildAtIndex(1).getType());
    Assert.assertEquals(Token.VAR, body.getLastChild().getType());
  }

  @Test
  public void testCatchVarUniqueRenamingDoesNotRewriteVar() {
    String js = "function f() { try { throw 0; } catch (e) { e; } var e = 1; }";
    Node root = parseAndNormalize(js, false);
    Node body = root.getFirstChild().getLastChild();
    Node lastStatement = body.getLastChild();
    Assert.assertEquals(Token.VAR, lastStatement.getType());
  }

  @Test(expected = IllegalStateException.class)
  public void testEmptyVarNodeThrowsAssertOnChange() {
    Node script = new Node(Token.SCRIPT);
    Node emptyVar = new Node(Token.VAR);
    script.addChildToBack(emptyVar);
    Normalize.NormalizeStatements ns = new Normalize.NormalizeStatements(compiler, true);
    NodeTraversal.traverse(compiler, script, ns);
  }
}
