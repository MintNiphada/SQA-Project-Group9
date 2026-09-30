package com.google.javascript.jscomp;

import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfoBuilder;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Test;

public class NormalizeTest {

  private Node parse(Compiler compiler, String js) {
    Node n = compiler.parseTestCode(js);
    Assert.assertEquals(0, compiler.getErrorCount());
    return n;
  }

  private Compiler createCompiler() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    return compiler;
  }

  @Test
  public void testSplitVarDeclarations() {
    Compiler compiler = createCompiler();
    Node root = parse(compiler, "var a = 1, b = 2, c = 3;");
    Node externs = parse(compiler, "");
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    // root is a SCRIPT containing normalized statements
    // Var declarations should be split: var a = 1; var b = 2; var c = 3;
    int varCount = 0;
    for (Node c = root.getFirstChild(); c != null; c = c.getNext()) {
      if (c.getType() == Token.VAR) {
        varCount++;
        Assert.assertEquals(1, c.getChildCount());
      }
    }
    Assert.assertEquals(3, varCount);
  }

  @Test
  public void testConvertWhileToFor() {
    Compiler compiler = createCompiler();
    Node root = parse(compiler, "while (true) { foo(); }");
    Node externs = parse(compiler, "");
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Node first = root.getFirstChild();
    Assert.assertEquals(Token.FOR, first.getType());
    Assert.assertEquals(Token.EMPTY, first.getFirstChild().getType());
  }

  @Test
  public void testExtractForInitializer() {
    Compiler compiler = createCompiler();
    Node root = parse(compiler, "for (var i = 0; i < 10; i++) {}");
    Node externs = parse(compiler, "");
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Node first = root.getFirstChild();
    Assert.assertEquals(Token.VAR, first.getType());
    Node second = first.getNext();
    Assert.assertEquals(Token.FOR, second.getType());
    Assert.assertEquals(Token.EMPTY, second.getFirstChild().getType());
  }

  @Test
  public void testExtractForInVarInitializer() {
    Compiler compiler = createCompiler();
    Node root = parse(compiler, "for (var a in b) {}");
    Node externs = parse(compiler, "");
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Node first = root.getFirstChild();
    Assert.assertEquals(Token.VAR, first.getType());
    Node second = first.getNext();
    Assert.assertEquals(Token.FOR, second.getType());
    Assert.assertEquals(Token.NAME, second.getFirstChild().getType());
  }

  @Test
  public void testMoveFunctionsToTop() {
    Compiler compiler = createCompiler();
    Node root = parse(compiler, "function f() { var a = 1; function g() {} var b = 2; }");
    Node externs = parse(compiler, "");
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Node fn = root.getFirstChild();
    Assert.assertEquals(Token.FUNCTION, fn.getType());
    Node block = fn.getLastChild();
    Node firstInBlock = block.getFirstChild();
    Assert.assertEquals(Token.FUNCTION, firstInBlock.getType());
  }

  @Test
  public void testNormalizeFunctionDeclarationInBlock() {
    Compiler compiler = createCompiler();
    Node root = parse(compiler, "if (true) { function f() {} }");
    Node externs = parse(compiler, "");
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Node ifNode = root.getFirstChild();
    Node block = ifNode.getLastChild();
    Node child = block.getFirstChild();
    Assert.assertEquals(Token.VAR, child.getType());
  }

  @Test
  public void testNormalizeLabels() {
    Compiler compiler = createCompiler();
    Node root = parse(compiler, "label: a = 1;");
    Node externs = parse(compiler, "");
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Node labelNode = root.getFirstChild();
    Assert.assertEquals(Token.LABEL, labelNode.getType());
    Node labelChild = labelNode.getLastChild();
    Assert.assertEquals(Token.BLOCK, labelChild.getType());
  }

  @Test
  public void testDuplicateVarDeclarations() {
    Compiler compiler = createCompiler();
    Node root = parse(compiler, "var a = 1; var a = 2;");
    Node externs = parse(compiler, "");
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Node first = root.getFirstChild();
    Assert.assertEquals(Token.VAR, first.getType());
    Node second = first.getNext();
    Assert.assertEquals(Token.EXPR_RESULT, second.getType());
    Assert.assertEquals(Token.ASSIGN, second.getFirstChild().getType());
  }

  @Test
  public void testDuplicateVarDeclarationsWithoutInit() {
    Compiler compiler = createCompiler();
    Node root = parse(compiler, "var a = 1; var a;");
    Node externs = parse(compiler, "");
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Assert.assertNotNull(root.getFirstChild());
    Assert.assertNull(root.getFirstChild().getNext());
  }

  @Test
  public void testDuplicateVarAndFunction() {
    Compiler compiler = createCompiler();
    Node root = parse(compiler, "var f = 1; function f() {}");
    Node externs = parse(compiler, "");
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Assert.assertNotNull(root.getFirstChild());
  }

  @Test
  public void testDuplicateCatchVarError() {
    Compiler compiler = createCompiler();
    Node root = parse(compiler, "function f() { try {} catch (e) { var e = 1; } }");
    Node externs = parse(compiler, "");
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Assert.assertTrue(compiler.getErrorCount() > 0);
  }

  @Test
  public void testExternDuplicateVarAllowed() {
    Compiler compiler = createCompiler();
    Node externs = parse(compiler, "var a;");
    Node root = parse(compiler, "var a = 1;");
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Assert.assertEquals(0, compiler.getErrorCount());
    Assert.assertEquals(Token.VAR, root.getFirstChild().getType());
  }

  @Test
  public void testAssertOnChangeThrows() {
    Compiler compiler = createCompiler();
    Node root = parse(compiler, "var a = 1, b = 2;");
    Node externs = parse(compiler, "");
    Normalize normalize = new Normalize(compiler, true);
    try {
      normalize.process(externs, root);
      Assert.fail("Expected IllegalStateException due to assertOnChange");
    } catch (IllegalStateException e) {
      Assert.assertTrue(e.getMessage().contains("Normalize constraints violated"));
    }
  }

  @Test
  public void testParseAndNormalizeSyntheticCode() {
    Compiler compiler = createCompiler();
    Node result = Normalize.parseAndNormalizeSyntheticCode(compiler, "var x = 1;", "prefix_");
    Assert.assertNotNull(result);
    Assert.assertEquals(Token.SCRIPT, result.getType());
  }

  @Test
  public void testParseAndNormalizeTestCode() {
    Compiler compiler = createCompiler();
    Node result = Normalize.parseAndNormalizeTestCode(compiler, "var y = 2;", "prefix_");
    Assert.assertNotNull(result);
    Assert.assertEquals(Token.SCRIPT, result.getType());
  }

  @Test
  public void testPropagateConstantAnnotations() {
    Compiler compiler = createCompiler();
    Node root = parse(compiler, "var CONST_FOO = 1; function test() { var x = CONST_FOO; }");
    Node externs = parse(compiler, "");
    Normalize.PropagateConstantAnnotationsOverVars pass =
        new Normalize.PropagateConstantAnnotationsOverVars(compiler, false);
    pass.process(externs, root);

    Node varNode = root.getFirstChild();
    Node nameNode = varNode.getFirstChild();
    Assert.assertTrue(nameNode.getBooleanProp(Node.IS_CONSTANT_NAME));
  }

  @Test
  public void testPropagateConstantAnnotationsWithAssertOnChange() {
    Compiler compiler = createCompiler();
    Node root = parse(compiler, "var CONST_FOO = 1;");
    Node externs = parse(compiler, "");
    Normalize.PropagateConstantAnnotationsOverVars pass =
        new Normalize.PropagateConstantAnnotationsOverVars(compiler, true);
    try {
      pass.process(externs, root);
      Assert.fail("Expected IllegalStateException on unexpected const change");
    } catch (IllegalStateException e) {
      Assert.assertTrue(e.getMessage().contains("Unexpected const change"));
    }
  }

  @Test
  public void testVerifyConstantsPass() {
    Compiler compiler = createCompiler();
    Node root = parse(compiler, "var CONST_A = 1; var b = CONST_A;");
    Node externs = parse(compiler, "");
    Node parent = new Node(Token.BLOCK, externs, root);

    Normalize.PropagateConstantAnnotationsOverVars prop =
        new Normalize.PropagateConstantAnnotationsOverVars(compiler, false);
    prop.process(externs, root);

    Normalize.VerifyConstants verify = new Normalize.VerifyConstants(compiler, true);
    verify.process(externs, root);
  }

  @Test
  public void testVerifyConstantsInconsistentThrows() {
    Compiler compiler = createCompiler();
    Node root = parse(compiler, "var CONST_A = 1; var b = CONST_A;");
    Node externs = parse(compiler, "");
    Node parent = new Node(Token.BLOCK, externs, root);

    // Only mark the first CONST_A as constant
    Node varNode = root.getFirstChild();
    varNode.getFirstChild().putBooleanProp(Node.IS_CONSTANT_NAME, true);

    Normalize.VerifyConstants verify = new Normalize.VerifyConstants(compiler, false);
    try {
      verify.process(externs, root);
      Assert.fail("Expected exception for inconsistent constant annotations");
    } catch (IllegalStateException e) {
      Assert.assertTrue(e.getMessage().contains("is not consistently annotated"));
    }
  }

  @Test
  public void testVerifyConstantsExpectedMismatchThrows() {
    Compiler compiler = createCompiler();
    Node root = parse(compiler, "var not_const = 1;");
    Node externs = parse(compiler, "");
    Node parent = new Node(Token.BLOCK, externs, root);

    // Improperly mark not_const as constant
    Node varNode = root.getFirstChild();
    varNode.getFirstChild().putBooleanProp(Node.IS_CONSTANT_NAME, true);

    Normalize.VerifyConstants verify = new Normalize.VerifyConstants(compiler, true);
    try {
      verify.process(externs, root);
      Assert.fail("Expected exception for improperly marked constant");
    } catch (IllegalStateException e) {
      Assert.assertTrue(e.getMessage().contains("should not be annotated as constant"));
    }
  }

  @Test
  public void testObjectLitKeyConstantAnnotation() {
    Compiler compiler = createCompiler();
    Node root = parse(compiler, "var obj = { CONST_KEY: 123 };");
    Node externs = parse(compiler, "");
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);

    Node varNode = root.getFirstChild();
    Node objNode = varNode.getFirstChild().getFirstChild();
    Node keyNode = objNode.getFirstChild();
    Assert.assertTrue(keyNode.getBooleanProp(Node.IS_CONSTANT_NAME));
  }

  @Test
  public void testConstantWithJSDoc() {
    Compiler compiler = createCompiler();
    Node root = parse(compiler, "/** @const */ var myVar = 10; var z = myVar;");
    Node externs = parse(compiler, "");
    Node parent = new Node(Token.BLOCK, externs, root);

    Normalize.PropagateConstantAnnotationsOverVars prop =
        new Normalize.PropagateConstantAnnotationsOverVars(compiler, false);
    prop.process(externs, root);

    Normalize.VerifyConstants verify = new Normalize.VerifyConstants(compiler, true);
    verify.process(externs, root);
  }
}