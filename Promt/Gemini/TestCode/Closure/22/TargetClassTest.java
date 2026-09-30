package com.google.javascript.jscomp;

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.JSDocInfoBuilder;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Test;

public class CheckSideEffectsTest {

  private Compiler compile(String js) {
    Compiler compiler = new Compiler();
    compiler.initCompilerOptionsIfTesting();
    Node externs = IR.root();
    Node main = compiler.parseTestCode(js);
    return compiler;
  }

  private void testCheck(String js, boolean protect, int expectedWarnings) {
    Compiler compiler = new Compiler();
    compiler.initCompilerOptionsIfTesting();
    Node main = compiler.parseTestCode(js);
    Node externs = IR.root();
    Node root = IR.root(externs, main);

    CheckSideEffects pass = new CheckSideEffects(compiler, CheckLevel.WARNING, protect);
    pass.process(externs, main);

    Assert.assertEquals("Warning count mismatch for: " + js, expectedWarnings, compiler.getWarningCount());
  }

  @Test
  public void testSimpleOperatorWarning() {
    Compiler compiler = new Compiler();
    compiler.initCompilerOptionsIfTesting();
    Node main = compiler.parseTestCode("x == 3;");
    Node externs = IR.root();

    CheckSideEffects pass = new CheckSideEffects(compiler, CheckLevel.WARNING, false);
    pass.process(externs, main);

    Assert.assertEquals(1, compiler.getWarningCount());
    JSError warning = compiler.getWarnings()[0];
    Assert.assertTrue(warning.description.contains("The result of the 'eq' operator is not being used."));
  }

  @Test
  public void testStringWarning() {
    Compiler compiler = new Compiler();
    compiler.initCompilerOptionsIfTesting();
    Node main = compiler.parseTestCode("\"some string\";");
    Node externs = IR.root();

    CheckSideEffects pass = new CheckSideEffects(compiler, CheckLevel.WARNING, false);
    pass.process(externs, main);

    Assert.assertEquals(1, compiler.getWarningCount());
    JSError warning = compiler.getWarnings()[0];
    Assert.assertTrue(warning.description.contains("Is there a missing '+' on the previous line?"));
  }

  @Test
  public void testCodeLacksSideEffectsWarning() {
    Compiler compiler = new Compiler();
    compiler.initCompilerOptionsIfTesting();
    Node main = compiler.parseTestCode("var a = 1; a;");
    Node externs = IR.root();

    CheckSideEffects pass = new CheckSideEffects(compiler, CheckLevel.WARNING, false);
    pass.process(externs, main);

    Assert.assertEquals(1, compiler.getWarningCount());
    JSError warning = compiler.getWarnings()[0];
    Assert.assertTrue(warning.description.contains("This code lacks side-effects. Is there a bug?"));
  }

  @Test
  public void testIndirectEvalCallIgnored() {
    testCheck("(0, eval)('var x = 1;');", false, 0);
  }

  @Test
  public void testCommaExpressionWarning() {
    testCheck("(a, b);", false, 2);
  }

  @Test
  public void testForLoopClauses() {
    testCheck("for (a; b; c) {}", false, 2);
    testCheck("for (var i = 0; i < 10; i++) {}", false, 0);
  }

  @Test
  public void testQualifiedNameWithJSDocIgnored() {
    Compiler compiler = new Compiler();
    compiler.initCompilerOptionsIfTesting();
    Node main = compiler.parseTestCode("/** @type {number} */ var x; /** @type {number} */ x.y;");
    Node externs = IR.root();

    CheckSideEffects pass = new CheckSideEffects(compiler, CheckLevel.WARNING, false);
    pass.process(externs, main);

    Assert.assertEquals(0, compiler.getWarningCount());
  }

  @Test
  public void testHotSwapScript() {
    Compiler compiler = new Compiler();
    compiler.initCompilerOptionsIfTesting();
    Node scriptRoot = compiler.parseTestCode("1 + 1;");

    CheckSideEffects pass = new CheckSideEffects(compiler, CheckLevel.WARNING, false);
    pass.hotSwapScript(scriptRoot, null);

    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testProtectSideEffects() {
    Compiler compiler = new Compiler();
    compiler.initCompilerOptionsIfTesting();
    Node main = compiler.parseTestCode("o.offsetWidth;");
    Node externs = IR.root();

    CheckSideEffects pass = new CheckSideEffects(compiler, CheckLevel.WARNING, true);
    pass.process(externs, main);

    Assert.assertEquals(1, compiler.getWarningCount());

    boolean foundPreserve = false;
    for (Node n : main.getNodes()) {
      if (n.isCall() && n.getFirstChild().isName() &&
          CheckSideEffects.PROTECTOR_FN.equals(n.getFirstChild().getString())) {
        foundPreserve = true;
        break;
      }
    }
    Assert.assertTrue("Expected JSCOMPILER_PRESERVE call node in AST", foundPreserve);
  }

  @Test
  public void testStripProtection() {
    Compiler compiler = new Compiler();
    compiler.initCompilerOptionsIfTesting();
    Node main = compiler.parseTestCode("o.offsetWidth;");
    Node externs = IR.root();

    CheckSideEffects pass = new CheckSideEffects(compiler, CheckLevel.WARNING, true);
    pass.process(externs, main);

    CheckSideEffects.StripProtection stripPass = new CheckSideEffects.StripProtection(compiler);
    stripPass.process(externs, main);

    boolean foundPreserve = false;
    for (Node n : main.getNodes()) {
      if (n.isCall() && n.getFirstChild().isName() &&
          CheckSideEffects.PROTECTOR_FN.equals(n.getFirstChild().getString())) {
        foundPreserve = true;
        break;
      }
    }
    Assert.assertFalse("JSCOMPILER_PRESERVE should have been stripped", foundPreserve);
  }

  @Test
  public void testVisitEdgeCases() {
    Compiler compiler = new Compiler();
    compiler.initCompilerOptionsIfTesting();
    CheckSideEffects pass = new CheckSideEffects(compiler, CheckLevel.WARNING, false);
    NodeTraversal t = new NodeTraversal(compiler, pass);

    // parent == null
    Node root = IR.root();
    pass.visit(t, root, null);

    // empty node
    Node empty = IR.empty();
    pass.visit(t, empty, root);

    // comma node
    Node comma = new Node(Token.COMMA);
    pass.visit(t, comma, root);

    Assert.assertEquals(0, compiler.getWarningCount());
  }

  @Test
  public void testEmptyBlockAndSideEffectFreeCalls() {
    testCheck("function foo() { ;;; }", false, 0);
    testCheck("function bar() { return 1; } foo();", false, 0);
  }
}