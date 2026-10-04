package com.google.javascript.jscomp;

import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfoBuilder;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class NormalizeTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  private Node parse(String js) {
    return compiler.parseTestCode(js);
  }

  private void testNormalize(String js) {
    Node root = parse(js);
    Node externs = new Node(Token.BLOCK);
    Node main = new Node(Token.BLOCK, externs, root);
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);
  }

  @Test
  public void testBasicVarSplitting() {
    Node root = parse("var a = 1, b = 2;");
    Node externs = new Node(Token.BLOCK);
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);
    Assert.assertEquals(Token.SCRIPT, root.getType());
    Assert.assertEquals(2, root.getChildCount());
  }

  @Test
  public void testWhileToForConversion() {
    Node root = parse("while(true) { foo(); }");
    Node externs = new Node(Token.BLOCK);
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);
    Node first = root.getFirstChild();
    Assert.assertEquals(Token.FOR, first.getType());
    Assert.assertEquals(Token.EMPTY, first.getFirstChild().getType());
  }

  @Test
  public void testForInitializerExtraction() {
    Node root = parse("for (var x = 0; x < 10; x++) {}");
    Node externs = new Node(Token.BLOCK);
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);
    Assert.assertEquals(Token.VAR, root.getFirstChild().getType());
    Assert.assertEquals(Token.FOR, root.getChildAtIndex(1).getType());
  }

  @Test
  public void testForExprInitializerExtraction() {
    Node root = parse("var x; for (x = 0; x < 10; x++) {}");
    Node externs = new Node(Token.BLOCK);
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);
    Assert.assertEquals(3, root.getChildCount());
    Assert.assertEquals(Token.EXPR_RESULT, root.getChildAtIndex(1).getType());
  }

  @Test
  public void testForInVarExtraction() {
    Node root = parse("for (var a in obj) {}");
    Node externs = new Node(Token.BLOCK);
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);
    Assert.assertEquals(Token.VAR, root.getFirstChild().getType());
    Assert.assertEquals(Token.FOR, root.getChildAtIndex(1).getType());
    Assert.assertEquals(Token.NAME, root.getChildAtIndex(1).getFirstChild().getType());
  }

  @Test
  public void testLabelNormalization() {
    Node root = parse("label: a = 1;");
    Node externs = new Node(Token.BLOCK);
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);
    Node label = root.getFirstChild();
    Assert.assertEquals(Token.LABEL, label.getType());
    Assert.assertEquals(Token.BLOCK, label.getLastChild().getType());
  }

  @Test
  public void testLabelWithLoopsNotWrapped() {
    Node root = parse("label: while (true) {}");
    Node externs = new Node(Token.BLOCK);
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);
    Node label = root.getFirstChild();
    Assert.assertEquals(Token.LABEL, label.getType());
    Assert.assertEquals(Token.FOR, label.getLastChild().getType());
  }

  @Test
  public void testUnhoistedFunctionRewrite() {
    Node root = parse("if (true) { function f() {} }");
    Node externs = new Node(Token.BLOCK);
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);
    Node ifBlock = root.getFirstChild().getLastChild();
    Assert.assertEquals(Token.VAR, ifBlock.getFirstChild().getType());
  }

  @Test
  public void testMoveNamedFunctions() {
    Node root = parse("function outer() { var a = 1; function inner() {} }");
    Node externs = new Node(Token.BLOCK);
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);
    Node outerBody = root.getFirstChild().getLastChild();
    Assert.assertEquals(Token.FUNCTION, outerBody.getFirstChild().getType());
    Assert.assertEquals(Token.VAR, outerBody.getLastChild().getType());
  }

  @Test
  public void testDuplicateVarDeclarations() {
    Node root = parse("var a = 1; var a = 2;");
    Node externs = new Node(Token.BLOCK);
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);
    Assert.assertEquals(Token.VAR, root.getFirstChild().getType());
    Assert.assertEquals(Token.EXPR_RESULT, root.getLastChild().getType());
  }

  @Test
  public void testDuplicateEmptyVarDeclaration() {
    Node root = parse("var a = 1; var a;");
    Node externs = new Node(Token.BLOCK);
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);
    Assert.assertEquals(1, root.getChildCount());
  }

  @Test
  public void testDuplicateFunctionVarDeclaration() {
    Node root = parse("var f = 1; function f() {}");
    Node externs = new Node(Token.BLOCK);
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);
    Assert.assertEquals(Token.FUNCTION, root.getFirstChild().getType());
    Assert.assertEquals(Token.EXPR_RESULT, root.getLastChild().getType());
  }

  @Test
  public void testDuplicateCatchBlockVarError() {
    Node root = parse("try {} catch (e) { var e = 1; }");
    Node externs = new Node(Token.BLOCK);
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);
    Assert.assertTrue(compiler.getErrorCount() > 0);
  }

  @Test
  public void testParseAndNormalizeSyntheticCode() {
    Node node = Normalize.parseAndNormalizeSyntheticCode(compiler, "var a = 1, b = 2;", "prefix_");
    Assert.assertNotNull(node);
    Assert.assertEquals(Token.SCRIPT, node.getType());
  }

  @Test
  public void testParseAndNormalizeTestCode() {
    Node node = Normalize.parseAndNormalizeTestCode(compiler, "while(x) { x--; }", "test_");
    Assert.assertNotNull(node);
    Assert.assertEquals(Token.FOR, node.getFirstChild().getType());
  }

  @Test(expected = IllegalStateException.class)
  public void testAssertOnChangeThrowsOnUnnormalizedCode() {
    Node root = parse("var a = 1, b = 2;");
    Node externs = new Node(Token.BLOCK);
    Normalize normalize = new Normalize(compiler, true);
    normalize.process(externs, root);
  }

  @Test
  public void testPropagateConstantAnnotationsOverVars() {
    Node root = parse("var CONST = 1; var b = CONST;");
    Node externs = new Node(Token.BLOCK);
    Normalize.PropagateConstantAnnotationsOverVars pass =
        new Normalize.PropagateConstantAnnotationsOverVars(compiler, false);
    pass.process(externs, root);
    Node constNode = root.getFirstChild().getFirstChild();
    Assert.assertTrue(constNode.getBooleanProp(Node.IS_CONSTANT_NAME));
  }

  @Test(expected = IllegalStateException.class)
  public void testPropagateConstantAnnotationsThrowsWhenAssertOnChange() {
    Node root = parse("var CONST = 1;");
    Node externs = new Node(Token.BLOCK);
    Normalize.PropagateConstantAnnotationsOverVars pass =
        new Normalize.PropagateConstantAnnotationsOverVars(compiler, true);
    pass.process(externs, root);
  }

  @Test
  public void testVerifyConstantsPass() {
    Node root = parse("var CONST = 1;");
    Node externs = new Node(Token.BLOCK);
    Node top = new Node(Token.BLOCK, externs, root);
    root.getFirstChild().getFirstChild().putBooleanProp(Node.IS_CONSTANT_NAME, true);

    Normalize.VerifyConstants verifier = new Normalize.VerifyConstants(compiler, true);
    verifier.process(externs, root);
  }

  @Test(expected = IllegalStateException.class)
  public void testVerifyConstantsFailsWhenInconsistent() {
    Node root = parse("var a = CONST; var b = CONST;");
    Node externs = new Node(Token.BLOCK);
    Node top = new Node(Token.BLOCK, externs, root);
    Node name1 = root.getFirstChild().getFirstChild().getFirstChild();
    Node name2 = root.getLastChild().getFirstChild().getFirstChild();
    name1.putBooleanProp(Node.IS_CONSTANT_NAME, true);
    name2.putBooleanProp(Node.IS_CONSTANT_NAME, false);

    Normalize.VerifyConstants verifier = new Normalize.VerifyConstants(compiler, false);
    verifier.process(externs, root);
  }

  @Test
  public void testNormalizeStatementsObjectLiteralKey() {
    Node root = parse("var obj = { CONST_KEY: 1 };");
    Node externs = new Node(Token.BLOCK);
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);
    Assert.assertNotNull(root);
  }

  @Test
  public void testNormalizeStatementsGetProp() {
    Node root = parse("foo.CONST_PROP = 1;");
    Node externs = new Node(Token.BLOCK);
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);
    Assert.assertNotNull(root);
  }

  @Test
  public void testConstantWithJSDoc() {
    Node root = parse("/** @const */ var x = 1; var y = x;");
    Node externs = new Node(Token.BLOCK);
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);
    Assert.assertNotNull(root);
  }

  @Test
  public void testAlreadyNormalizedCodeWithAssertOnChange() {
    Node root = parse("var a = 1;");
    Node externs = new Node(Token.BLOCK);
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Normalize assertNormalize = new Normalize(compiler, true);
    assertNormalize.process(externs, root);
    Assert.assertTrue(compiler.isNormalized());
  }

  @Test
  public void testNestedLabels() {
    Node root = parse("label1: label2: a = 1;");
    Node externs = new Node(Token.BLOCK);
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);
    Assert.assertEquals(Token.LABEL, root.getFirstChild().getType());
  }

  @Test
  public void testForInLabelInitializer() {
    Node root = parse("label: for (var a in b) {}");
    Node externs = new Node(Token.BLOCK);
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);
    Assert.assertEquals(Token.VAR, root.getFirstChild().getType());
  }
}
