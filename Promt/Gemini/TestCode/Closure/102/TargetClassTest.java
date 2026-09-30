package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Test;

public class NormalizeTest {

  @Test
  public void testWhileToForConversion() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("while (true) { alert(1); }");
    Node externs = compiler.parseTestCode("");
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Assert.assertNotNull(root);
    Node first = root.getFirstChild();
    Assert.assertNotNull(first);
    Assert.assertEquals(Token.FOR, first.getType());
    Assert.assertEquals(Token.EMPTY, first.getFirstChild().getType());
  }

  @Test
  public void testSplitVarDeclarations() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("var a = 1, b = 2, c = 3;");
    Node externs = compiler.parseTestCode("");
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Assert.assertEquals(3, root.getChildCount());
    for (Node child = root.getFirstChild(); child != null; child = child.getNext()) {
      Assert.assertEquals(Token.VAR, child.getType());
      Assert.assertTrue(child.hasOneChild());
    }
  }

  @Test
  public void testExtractForInitializerVar() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("for (var i = 0; i < 10; i++) {}");
    Node externs = compiler.parseTestCode("");
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Assert.assertEquals(2, root.getChildCount());
    Node first = root.getFirstChild();
    Assert.assertEquals(Token.VAR, first.getType());
    Node second = first.getNext();
    Assert.assertEquals(Token.FOR, second.getType());
    Assert.assertEquals(Token.EMPTY, second.getFirstChild().getType());
  }

  @Test
  public void testExtractForInitializerExpr() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("var i; for (i = 0; i < 10; i++) {}");
    Node externs = compiler.parseTestCode("");
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Node first = root.getFirstChild();
    Assert.assertEquals(Token.VAR, first.getType());
    Node second = first.getNext();
    Assert.assertEquals(Token.EXPR_RESULT, second.getType());
    Node third = second.getNext();
    Assert.assertEquals(Token.FOR, third.getType());
  }

  @Test
  public void testForInNotExtracted() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("for (var k in obj) {}");
    Node externs = compiler.parseTestCode("");
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Assert.assertEquals(1, root.getChildCount());
    Assert.assertEquals(Token.FOR, root.getFirstChild().getType());
  }

  @Test
  public void testNormalizeLabelsWrapsInBlock() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("lab: a = 1;");
    Node externs = compiler.parseTestCode("");
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Node labelNode = root.getFirstChild();
    Assert.assertEquals(Token.LABEL, labelNode.getType());
    Node last = labelNode.getLastChild();
    Assert.assertEquals(Token.BLOCK, last.getType());
  }

  @Test
  public void testNormalizeLabelsLoopsNotWrapped() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(
        "lab1: while(true) {}" +
        "lab2: for(;;) {}" +
        "lab3: do {} while(true);" +
        "lab4: { var x = 1; }" +
        "lab5: lab6: var y = 2;");
    Node externs = compiler.parseTestCode("");
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Assert.assertNotNull(root);
  }

  @Test
  public void testMoveNamedFunctions() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(
        "function outer() {" +
        "  var x = 1;" +
        "  function f1() {}" +
        "  var y = 2;" +
        "  function f2() {}" +
        "}");
    Node externs = compiler.parseTestCode("");
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Node fnNode = root.getFirstChild();
    Node fnBody = fnNode.getLastChild();
    Node first = fnBody.getFirstChild();
    Assert.assertEquals(Token.FUNCTION, first.getType());
    Assert.assertEquals("f1", first.getFirstChild().getString());
    Node second = first.getNext();
    Assert.assertEquals(Token.FUNCTION, second.getType());
    Assert.assertEquals("f2", second.getFirstChild().getString());
  }

  @Test
  public void testDuplicateDeclarationsWithInit() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("var a = 1; var a = 2;");
    Node externs = compiler.parseTestCode("");
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Node first = root.getFirstChild();
    Assert.assertEquals(Token.VAR, first.getType());
    Node second = first.getNext();
    Assert.assertEquals(Token.EXPR_RESULT, second.getType());
  }

  @Test
  public void testDuplicateDeclarationsWithoutInit() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("var a = 1; var a;");
    Node externs = compiler.parseTestCode("");
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Assert.assertEquals(1, root.getChildCount());
    Assert.assertEquals(Token.VAR, root.getFirstChild().getType());
  }

  @Test
  public void testDuplicateDeclarationsInForIn() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("var a; for (var a in b) {}");
    Node externs = compiler.parseTestCode("");
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Node first = root.getFirstChild();
    Assert.assertEquals(Token.VAR, first.getType());
    Node forNode = first.getNext();
    Assert.assertEquals(Token.FOR, forNode.getType());
    Assert.assertEquals(Token.NAME, forNode.getFirstChild().getType());
  }

  @Test
  public void testDuplicateDeclarationsInLabel() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("var a; lab: var a;");
    Node externs = compiler.parseTestCode("");
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Node first = root.getFirstChild();
    Assert.assertEquals(Token.VAR, first.getType());
    Node labelNode = first.getNext();
    Assert.assertEquals(Token.LABEL, labelNode.getType());
  }

  @Test(expected = IllegalStateException.class)
  public void testAssertOnChangeThrowsOnUnnormalizedCode() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("var a = 1, b = 2;");
    Node externs = compiler.parseTestCode("");
    Normalize normalize = new Normalize(compiler, true);
    normalize.process(externs, root);
  }

  @Test(expected = IllegalStateException.class)
  public void testEmptyVarNodeWithAssertOnChange() {
    Compiler compiler = new Compiler();
    Node emptyVar = new Node(Token.VAR);
    Node root = new Node(Token.SCRIPT, emptyVar);
    Node externs = new Node(Token.SCRIPT);
    Normalize normalize = new Normalize(compiler, true);
    normalize.process(externs, root);
  }

  @Test
  public void testPropogateConstantAnnotations() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("/** @const */ var CONST_VAL = 1; var b = CONST_VAL;");
    Node externs = compiler.parseTestCode("");
    Normalize.PropogateConstantAnnotations prop =
        new Normalize.PropogateConstantAnnotations(compiler, false);
    prop.process(externs, root);

    Node varB = root.getLastChild();
    Node nameB = varB.getFirstChild();
    Node init = nameB.getFirstChild();
    Assert.assertTrue(init.getBooleanProp(Node.IS_CONSTANT_NAME));
  }

  @Test(expected = IllegalStateException.class)
  public void testPropogateConstantAnnotationsAssertOnChangeThrows() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("/** @const */ var CONST_VAL = 1; var b = CONST_VAL;");
    Node externs = compiler.parseTestCode("");
    Normalize.PropogateConstantAnnotations prop =
        new Normalize.PropogateConstantAnnotations(compiler, true);
    prop.process(externs, root);
  }

  @Test
  public void testPropogateConstantAnnotationsEmptyName() {
    Compiler compiler = new Compiler();
    Node nameNode = Node.newString(Token.NAME, "");
    Node root = new Node(Token.SCRIPT, nameNode);
    Node externs = new Node(Token.SCRIPT);
    Normalize.PropogateConstantAnnotations prop =
        new Normalize.PropogateConstantAnnotations(compiler, false);
    prop.process(externs, root);
    Assert.assertFalse(nameNode.getBooleanProp(Node.IS_CONSTANT_NAME));
  }

  @Test
  public void testVerifyConstantsPasses() {
    Compiler compiler = new Compiler();
    Node externs = compiler.parseTestCode("");
    Node root = compiler.parseTestCode("var a = 1; var b = 2;");
    Node parent = new Node(Token.BLOCK, externs, root);
    Normalize.VerifyConstants verifier = new Normalize.VerifyConstants(compiler, false);
    verifier.process(externs, root);
  }

  @Test(expected = IllegalStateException.class)
  public void testVerifyConstantsFailsInconsistent() {
    Compiler compiler = new Compiler();
    Node externs = compiler.parseTestCode("");
    Node root = compiler.parseTestCode("var a = 1; a = 2;");
    Node name1 = root.getFirstChild().getFirstChild();
    name1.putBooleanProp(Node.IS_CONSTANT_NAME, true);

    Node parent = new Node(Token.BLOCK, externs, root);
    Normalize.VerifyConstants verifier = new Normalize.VerifyConstants(compiler, false);
    verifier.process(externs, root);
  }

  @Test
  public void testVerifyConstantsEmptyName() {
    Compiler compiler = new Compiler();
    Node externs = new Node(Token.SCRIPT);
    Node emptyName = Node.newString(Token.NAME, "");
    Node root = new Node(Token.SCRIPT, emptyName);
    Node parent = new Node(Token.BLOCK, externs, root);
    Normalize.VerifyConstants verifier = new Normalize.VerifyConstants(compiler, false);
    verifier.process(externs, root);
  }

  @Test
  public void testVerifyConstantsWithUserDeclarations() {
    Compiler compiler = new Compiler();
    Node externs = compiler.parseTestCode("");
    Node root = compiler.parseTestCode("var a = 1;");
    Node parent = new Node(Token.BLOCK, externs, root);
    Normalize.VerifyConstants verifier = new Normalize.VerifyConstants(compiler, true);
    verifier.process(externs, root);
  }

  @Test(expected = IllegalStateException.class)
  public void testVerifyConstantsWithUserDeclarationsFailsWhenExpectedConst() {
    Compiler compiler = new Compiler();
    Node externs = compiler.parseTestCode("");
    Node root = compiler.parseTestCode("/** @const */ var A = 1;");
    Node parent = new Node(Token.BLOCK, externs, root);
    Normalize.VerifyConstants verifier = new Normalize.VerifyConstants(compiler, true);
    verifier.process(externs, root);
  }

  @Test
  public void testExtractForInitializerNestedLabel() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("lab: for (var i = 0; i < 10; i++) {}");
    Node externs = compiler.parseTestCode("");
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Node first = root.getFirstChild();
    Assert.assertEquals(Token.VAR, first.getType());
    Node labelNode = first.getNext();
    Assert.assertEquals(Token.LABEL, labelNode.getType());
  }
}