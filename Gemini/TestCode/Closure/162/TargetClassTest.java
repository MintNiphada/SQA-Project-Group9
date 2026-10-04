package com.google.javascript.jscomp;

import com.google.javascript.jscomp.CompilerOptions.AliasTransformation;
import com.google.javascript.jscomp.CompilerOptions.AliasTransformationHandler;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SourcePosition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class ScopedAliasesTest {
  private Compiler compiler;
  private TestAliasTransformationHandler transformationHandler;
  private PreprocessorSymbolTable preprocessorSymbolTable;

  private static class TestAliasTransformation implements AliasTransformation {
    final Map<String, String> aliases = new HashMap<String, String>();

    @Override
    public void addAlias(String alias, String qualifiedName) {
      aliases.put(alias, qualifiedName);
    }
  }

  private static class TestAliasTransformationHandler implements AliasTransformationHandler {
    final Map<String, TestAliasTransformation> transformations =
        new HashMap<String, TestAliasTransformation>();

    @Override
    public AliasTransformation logAliasTransformation(
        String sourceFile, SourcePosition<AliasTransformation> position) {
      TestAliasTransformation trans = new TestAliasTransformation();
      transformations.put(sourceFile, trans);
      return trans;
    }
  }

  @Before
  public void setUp() {
    compiler = new Compiler();
    transformationHandler = new TestAliasTransformationHandler();
    preprocessorSymbolTable = null;
  }

  private Node parseAndRun(String js) {
    Node root = compiler.parseTestCode(js);
    ScopedAliases pass = new ScopedAliases(
        compiler, preprocessorSymbolTable, transformationHandler);
    pass.process(null, root);
    return root;
  }

  private void testScopedAliases(String js, String expectedJs) {
    Node root = parseAndRun(js);
    Assert.assertEquals(0, compiler.getErrorCount());
    Node expectedRoot = compiler.parseTestCode(expectedJs);
    String actual = compiler.toSource(root);
    String expected = compiler.toSource(expectedRoot);
    Assert.assertEquals(expected, actual);
  }

  private void testError(String js, DiagnosticType expectedError) {
    parseAndRun(js);
    Assert.assertTrue(compiler.getErrorCount() > 0);
    Assert.assertEquals(expectedError, compiler.getErrors()[0].getType());
  }

  @Test
  public void testSimpleAlias() {
    String js = "goog.scope(function() {\n"
        + "  var dom = goog.dom;\n"
        + "  dom.createElement('div');\n"
        + "});";
    String expected = "goog.dom.createElement('div');";
    testScopedAliases(js, expected);
  }

  @Test
  public void testMultipleAliasesAndChaining() {
    String js = "goog.scope(function() {\n"
        + "  var g = goog;\n"
        + "  var dom = g.dom;\n"
        + "  var create = dom.createElement;\n"
        + "  create('div');\n"
        + "});";
    String expected = "goog.dom.createElement('div');";
    testScopedAliases(js, expected);
  }

  @Test
  public void testMultipleVarsInSingleDeclaration() {
    String js = "goog.scope(function() {\n"
        + "  var a = goog.a, b = goog.b;\n"
        + "  a(); b();\n"
        + "});";
    String expected = "goog.a(); goog.b();";
    testScopedAliases(js, expected);
  }

  @Test
  public void testMultipleScopes() {
    String js = "goog.scope(function() {\n"
        + "  var dom = goog.dom;\n"
        + "  dom.createElement('div');\n"
        + "});\n"
        + "goog.scope(function() {\n"
        + "  var events = goog.events;\n"
        + "  events.listen();\n"
        + "});";
    String expected = "goog.dom.createElement('div'); goog.events.listen();";
    testScopedAliases(js, expected);
  }

  @Test
  public void testNestedFunctionUsingAlias() {
    String js = "goog.scope(function() {\n"
        + "  var dom = goog.dom;\n"
        + "  function foo() {\n"
        + "    return dom.createElement('div');\n"
        + "  }\n"
        + "  foo();\n"
        + "});";
    String expected = "function foo() {\n"
        + "  return goog.dom.createElement('div');\n"
        + "}\n"
        + "foo();";
    testScopedAliases(js, expected);
  }

  @Test
  public void testShadowedAliasNotReplaced() {
    String js = "goog.scope(function() {\n"
        + "  var dom = goog.dom;\n"
        + "  function foo(dom) {\n"
        + "    return dom.createElement('div');\n"
        + "  }\n"
        + "  foo(dom);\n"
        + "});";
    String expected = "function foo(dom) {\n"
        + "  return dom.createElement('div');\n"
        + "}\n"
        + "foo(goog.dom);";
    testScopedAliases(js, expected);
  }

  @Test
  public void testUnscopedFunctionIgnored() {
    String js = "function external() {\n"
        + "  var dom = 1;\n"
        + "  return dom;\n"
        + "}\n"
        + "goog.scope(function() {\n"
        + "  var dom = goog.dom;\n"
        + "  dom.createElement('div');\n"
        + "});";
    String expected = "function external() {\n"
        + "  var dom = 1;\n"
        + "  return dom;\n"
        + "}\n"
        + "goog.dom.createElement('div');";
    testScopedAliases(js, expected);
  }

  @Test
  public void testJsDocTypeTransformation() {
    String js = "goog.scope(function() {\n"
        + "  var Button = goog.ui.Button;\n"
        + "  /** @type {Button} */ var b;\n"
        + "  /** @type {Button.Sub} */ var bSub;\n"
        + "  /** @type {Array<Button>} */ var bArr;\n"
        + "});";
    Node root = parseAndRun(js);
    Assert.assertEquals(0, compiler.getErrorCount());
    Assert.assertNotNull(root);
  }

  @Test
  public void testPreprocessorSymbolTableReference() {
    Node root = compiler.parseTestCode("goog.scope(function() { var d = goog.dom; d(); });");
    preprocessorSymbolTable = new PreprocessorSymbolTable(root);
    ScopedAliases pass = new ScopedAliases(
        compiler, preprocessorSymbolTable, transformationHandler);
    pass.process(null, root);
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testErrorGoogScopeUsedImproperly() {
    testError("var x = goog.scope(function() {});", ScopedAliases.GOOG_SCOPE_USED_IMPROPERLY);
  }

  @Test
  public void testErrorGoogScopeNoParams() {
    testError("goog.scope();", ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  @Test
  public void testErrorGoogScopeTooManyParams() {
    testError("goog.scope(function() {}, 123);", ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  @Test
  public void testErrorGoogScopeNonFunctionParam() {
    testError("goog.scope('not a fn');", ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  @Test
  public void testErrorGoogScopeNamedFunction() {
    testError("goog.scope(function named() {});", ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  @Test
  public void testErrorGoogScopeFunctionWithParameters() {
    testError("goog.scope(function(a) {});", ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  @Test
  public void testErrorGoogScopeNonAliasLocalLiteral() {
    testError("goog.scope(function() { var x = 10; });", ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
  }

  @Test
  public void testErrorGoogScopeNonAliasLocalUninitialized() {
    testError("goog.scope(function() { var x; });", ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
  }

  @Test
  public void testErrorGoogScopeAliasRedefined() {
    testError("goog.scope(function() { var x = goog.dom; x = goog.events; });",
        ScopedAliases.GOOG_SCOPE_ALIAS_REDEFINED);
  }

  @Test
  public void testErrorGoogScopeReferencesThis() {
    testError("goog.scope(function() { this.foo(); });",
        ScopedAliases.GOOG_SCOPE_REFERENCES_THIS);
  }

  @Test
  public void testErrorGoogScopeUsesReturn() {
    testError("goog.scope(function() { return; });", ScopedAliases.GOOG_SCOPE_USES_RETURN);
  }

  @Test
  public void testErrorGoogScopeUsesThrow() {
    testError("goog.scope(function() { throw 'err'; });", ScopedAliases.GOOG_SCOPE_USES_THROW);
  }

  @Test
  public void testHotSwapScriptDirectCall() {
    Node root = compiler.parseTestCode("goog.scope(function() { var d = goog.dom; d(); });");
    ScopedAliases pass = new ScopedAliases(
        compiler, null, transformationHandler);
    pass.hotSwapScript(root, null);
    Assert.assertEquals(0, compiler.getErrorCount());
    Assert.assertEquals("goog.dom();", compiler.toSource(root));
  }

  @Test
  public void testEmptyScope() {
    String js = "goog.scope(function() {});";
    String expected = "";
    testScopedAliases(js, expected);
  }
}
