package com.google.javascript.jscomp;

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfoBuilder;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Test;

public class TargetClassTest {

  private Compiler createCompiler() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    return compiler;
  }

  private Node parse(Compiler compiler, String js) {
    Node root = compiler.parseTestCode(js);
    Assert.assertNotNull(root);
    return root;
  }

  @Test
  public void testNoWarningOnValidStatements() {
    Compiler compiler = createCompiler();
    Node root = parse(compiler, "var x = 1; function foo() { return x; } foo();");
    CheckSideEffects pass = new CheckSideEffects(compiler, CheckLevel.WARNING, false);
    pass.process(null, root);
    Assert.assertEquals(0, compiler.getWarningCount());
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testWarningOnUselessString() {
    Compiler compiler = createCompiler();
    Node root = parse(compiler, "'some useless string';");
    CheckSideEffects pass = new CheckSideEffects(compiler, CheckLevel.WARNING, false);
    pass.process(null, root);
    Assert.assertEquals(1, compiler.getWarningCount());
    JSError warning = compiler.getWarnings()[0];
    Assert.assertEquals(CheckSideEffects.USELESS_CODE_ERROR, warning.getType());
    Assert.assertTrue(warning.description.contains("missing '+'"));
  }

  @Test
  public void testWarningOnSimpleOperator() {
    Compiler compiler = createCompiler();
    Node root = parse(compiler, "1 + 2;");
    CheckSideEffects pass = new CheckSideEffects(compiler, CheckLevel.WARNING, false);
    pass.process(null, root);
    Assert.assertEquals(1, compiler.getWarningCount());
    JSError warning = compiler.getWarnings()[0];
    Assert.assertEquals(CheckSideEffects.USELESS_CODE_ERROR, warning.getType());
    Assert.assertTrue(warning.description.contains("operator is not being used"));
  }

  @Test
  public void testWarningOnVariableReferenceWithoutSideEffects() {
    Compiler compiler = createCompiler();
    Node root = parse(compiler, "var x = 1; x;");
    CheckSideEffects pass = new CheckSideEffects(compiler, CheckLevel.ERROR, false);
    pass.process(null, root);
    Assert.assertEquals(1, compiler.getErrorCount());
    JSError error = compiler.getErrors()[0];
    Assert.assertEquals(CheckSideEffects.USELESS_CODE_ERROR, error.getType());
    Assert.assertTrue(error.description.contains("This code lacks side-effects"));
  }

  @Test
  public void testEmptyAndCommaNodesIgnored() {
    Compiler compiler = createCompiler();
    Node root = parse(compiler, ";;;");
    CheckSideEffects pass = new CheckSideEffects(compiler, CheckLevel.WARNING, false);
    pass.process(null, root);
    Assert.assertEquals(0, compiler.getWarningCount());
  }

  @Test
  public void testJSDocOnQualifiedNameIgnored() {
    Compiler compiler = createCompiler();
    Node root = parse(compiler, "var ns = {}; /** @type {number} */ ns.prop;");
    CheckSideEffects pass = new CheckSideEffects(compiler, CheckLevel.WARNING, false);
    pass.process(null, root);
    Assert.assertEquals(0, compiler.getWarningCount());
  }

  @Test
  public void testForLoopInitAndIncrement() {
    Compiler compiler = createCompiler();
    Node root = parse(compiler, "for (1 + 1; ; 2 + 2) { break; }");
    CheckSideEffects pass = new CheckSideEffects(compiler, CheckLevel.WARNING, false);
    pass.process(null, root);
    Assert.assertEquals(2, compiler.getWarningCount());
  }

  @Test
  public void testCommaExpressionLacksSideEffects() {
    Compiler compiler = createCompiler();
    Node root = parse(compiler, "(1, 2);");
    CheckSideEffects pass = new CheckSideEffects(compiler, CheckLevel.WARNING, false);
    pass.process(null, root);
    Assert.assertTrue(compiler.getWarningCount() >= 1);
  }

  @Test
  public void testCommaExpressionResultUsed() {
    Compiler compiler = createCompiler();
    Node root = parse(compiler, "var a = (1, 2);");
    CheckSideEffects pass = new CheckSideEffects(compiler, CheckLevel.WARNING, false);
    pass.process(null, root);
    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testCommaExpressionInIfCondition() {
    Compiler compiler = createCompiler();
    Node root = parse(compiler, "if ((1, true)) { var x = 1; }");
    CheckSideEffects pass = new CheckSideEffects(compiler, CheckLevel.WARNING, false);
    pass.process(null, root);
    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testProtectSideEffects() {
    Compiler compiler = createCompiler();
    Node root = parse(compiler, "1 + 1;");
    CheckSideEffects pass = new CheckSideEffects(compiler, CheckLevel.WARNING, true);
    pass.process(null, root);
    Assert.assertEquals(1, compiler.getWarningCount());

    String output = compiler.toSource(root);
    Assert.assertTrue(output.contains(CheckSideEffects.PROTECTOR_FN));
  }

  @Test
  public void testStripProtection() {
    Compiler compiler = createCompiler();
    Node root = parse(compiler, "1 + 1;");
    CheckSideEffects pass = new CheckSideEffects(compiler, CheckLevel.WARNING, true);
    pass.process(null, root);

    CheckSideEffects.StripProtection stripPass = new CheckSideEffects.StripProtection(compiler);
    stripPass.process(null, root);

    String output = compiler.toSource(root);
    Assert.assertFalse(output.contains(CheckSideEffects.PROTECTOR_FN));
  }

  @Test
  public void testHotSwapScript() {
    Compiler compiler = createCompiler();
    Node scriptRoot = parse(compiler, "1 + 1;");
    CheckSideEffects pass = new CheckSideEffects(compiler, CheckLevel.WARNING, false);
    pass.hotSwapScript(scriptRoot, null);
    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testDirectVisitEdgeCases() {
    Compiler compiler = createCompiler();
    NodeTraversal t = new NodeTraversal(compiler, new CheckSideEffects(compiler, CheckLevel.WARNING, false));
    CheckSideEffects pass = new CheckSideEffects(compiler, CheckLevel.WARNING, false);

    Node emptyNode = IR.empty();
    pass.visit(t, emptyNode, null);

    Node commaNode = IR.comma(IR.number(1), IR.number(2));
    pass.visit(t, commaNode, null);

    Node exprResult = IR.exprResult(IR.number(1));
    pass.visit(t, exprResult, null);

    Node nameWithDoc = IR.name("x");
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(false);
    JSDocInfo info = docBuilder.build(nameWithDoc);
    nameWithDoc.setJSDocInfo(info);
    pass.visit(t, nameWithDoc, IR.exprResult(nameWithDoc));

    Assert.assertEquals(0, compiler.getWarningCount());
  }

  @Test
  public void testStripProtectionNonTargetCall() {
    Compiler compiler = createCompiler();
    Node root = parse(compiler, "foo(1);");
    CheckSideEffects.StripProtection stripPass = new CheckSideEffects.StripProtection(compiler);
    stripPass.process(null, root);
    String output = compiler.toSource(root);
    Assert.assertTrue(output.contains("foo(1)"));
  }
}