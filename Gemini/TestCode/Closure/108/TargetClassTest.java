package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Assert;
import org.junit.Test;

public class ScopedAliasesTest {

  private Compiler compiler;

  private Node test(String js) {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    Node root = compiler.parseTestCode(js);
    ScopedAliases pass = new ScopedAliases(
        compiler,
        null,
        CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
    pass.process(null, root);
    return root;
  }

  private Node testWithPreprocessor(String js) {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    Node root = compiler.parseTestCode(js);
    PreprocessorSymbolTable table = new PreprocessorSymbolTable(root);
    ScopedAliases pass = new ScopedAliases(
        compiler,
        table,
        CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
    pass.process(null, root);
    return root;
  }

  private void testScopedAliases(String js, DiagnosticType expectedError) {
    test(js);
    if (expectedError != null) {
      Assert.assertTrue("Expected error: " + expectedError.key, compiler.getErrorCount() > 0);
      boolean found = false;
      for (JSError error : compiler.getErrors()) {
        if (error.getType() == expectedError) {
          found = true;
          break;
        }
      }
      Assert.assertTrue("Expected diagnostic: " + expectedError.key, found);
    } else {
      Assert.assertEquals("Expected 0 errors", 0, compiler.getErrorCount());
    }
  }

  @Test
  public void testSimpleAlias() {
    testScopedAliases(
        "goog.scope(function() {\n" +
        "  var dom = goog.dom;\n" +
        "  dom.createElement('div');\n" +
        "});",
        null);
  }

  @Test
  public void testChainedAlias() {
    testScopedAliases(
        "goog.scope(function() {\n" +
        "  var dom = goog.dom;\n" +
        "  var DIV = dom.TagName.DIV;\n" +
        "  dom.createElement(DIV);\n" +
        "});",
        null);
  }

  @Test
  public void testScopeUsedImproperlyNotExpr() {
    testScopedAliases(
        "var x = goog.scope(function() {});",
        ScopedAliases.GOOG_SCOPE_USED_IMPROPERLY);
  }

  @Test
  public void testScopeUsedImproperlyInIf() {
    testScopedAliases(
        "if (goog.scope(function() {})) {}",
        ScopedAliases.GOOG_SCOPE_USED_IMPROPERLY);
  }

  @Test
  public void testScopeBadParamsNoArgs() {
    testScopedAliases(
        "goog.scope();",
        ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  @Test
  public void testScopeBadParamsTooManyArgs() {
    testScopedAliases(
        "goog.scope(function() {}, 1);",
        ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  @Test
  public void testScopeBadParamsNonFunction() {
    testScopedAliases(
        "goog.scope(123);",
        ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  @Test
  public void testScopeBadParamsNamedFunction() {
    testScopedAliases(
        "goog.scope(function foo() {});",
        ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  @Test
  public void testScopeBadParamsWithArgs() {
    testScopedAliases(
        "goog.scope(function(a) {});",
        ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  @Test
  public void testScopeReferencesThis() {
    testScopedAliases(
        "goog.scope(function() {\n" +
        "  this.foo = 1;\n" +
        "});",
        ScopedAliases.GOOG_SCOPE_REFERENCES_THIS);
  }

  @Test
  public void testScopeUsesReturn() {
    testScopedAliases(
        "goog.scope(function() {\n" +
        "  return;\n" +
        "});",
        ScopedAliases.GOOG_SCOPE_USES_RETURN);
  }

  @Test
  public void testScopeUsesThrow() {
    testScopedAliases(
        "goog.scope(function() {\n" +
        "  throw 'error';\n" +
        "});",
        ScopedAliases.GOOG_SCOPE_USES_THROW);
  }

  @Test
  public void testScopeAliasRedefined() {
    testScopedAliases(
        "goog.scope(function() {\n" +
        "  var dom = goog.dom;\n" +
        "  dom = goog.otherDom;\n" +
        "});",
        ScopedAliases.GOOG_SCOPE_ALIAS_REDEFINED);
  }

  @Test
  public void testScopeAliasCycle() {
    testScopedAliases(
        "goog.scope(function() {\n" +
        "  var a = b.c;\n" +
        "  var b = a.c;\n" +
        "  a.foo();\n" +
        "});",
        ScopedAliases.GOOG_SCOPE_ALIAS_CYCLE);
  }

  @Test
  public void testScopeNonAliasLocalCatch() {
    testScopedAliases(
        "goog.scope(function() {\n" +
        "  try {} catch (e) {}\n" +
        "});",
        ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
  }

  @Test
  public void testFunctionDeclarationHoisted() {
    testScopedAliases(
        "goog.scope(function() {\n" +
        "  foo();\n" +
        "  function foo() {}\n" +
        "});",
        null);
  }

  @Test
  public void testFunctionDeclarationNonHoisted() {
    testScopedAliases(
        "goog.scope(function() {\n" +
        "  var x = 1;\n" +
        "  function foo() {}\n" +
        "  foo();\n" +
        "});",
        null);
  }

  @Test
  public void testMultipleFunctionsWithSameNameInMultipleScopes() {
    testScopedAliases(
        "goog.scope(function() {\n" +
        "  function foo() {}\n" +
        "});\n" +
        "goog.scope(function() {\n" +
        "  function foo() {}\n" +
        "});",
        null);
  }

  @Test
  public void testNamespaceShadowing() {
    testScopedAliases(
        "goog.scope(function() {\n" +
        "  var dom = goog.dom;\n" +
        "  function inner() {\n" +
        "    var goog = 1;\n" +
        "    return goog;\n" +
        "  }\n" +
        "  dom.createElement('div');\n" +
        "});",
        null);
  }

  @Test
  public void testJsDocTypeAliasing() {
    testScopedAliases(
        "goog.scope(function() {\n" +
        "  var Button = goog.ui.Button;\n" +
        "  /** @type {Button} */ var b;\n" +
        "  /** @type {Button.EventType} */ var e;\n" +
        "});",
        null);
  }

  @Test
  public void testJsDocTypeAliasingCompound() {
    testScopedAliases(
        "goog.scope(function() {\n" +
        "  var Button = goog.ui.Button;\n" +
        "  /** @type {Array.<Button>} */ var list;\n" +
        "});",
        null);
  }

  @Test
  public void testPreprocessorSymbolTableInteraction() {
    testWithPreprocessor(
        "goog.scope(function() {\n" +
        "  var dom = goog.dom;\n" +
        "  dom.createElement('div');\n" +
        "});");
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testMultipleAliasesInSingleVarStatement() {
    testScopedAliases(
        "goog.scope(function() {\n" +
        "  var a = goog.a, b = goog.b;\n" +
        "  a();\n" +
        "  b();\n" +
        "});",
        null);
  }

  @Test
  public void testHotSwapScriptDirectCall() {
    Compiler comp = new Compiler();
    comp.initOptions(new CompilerOptions());
    Node root = comp.parseTestCode(
        "goog.scope(function() {\n" +
        "  var dom = goog.dom;\n" +
        "  dom.createElement('div');\n" +
        "});");
    ScopedAliases pass = new ScopedAliases(
        comp,
        null,
        CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
    pass.hotSwapScript(root, null);
    Assert.assertEquals(0, comp.getErrorCount());
  }

  @Test
  public void testGlobalFunctionNotTraversed() {
    testScopedAliases(
        "function notInScope() {\n" +
        "  var x = 1;\n" +
        "}\n" +
        "goog.scope(function() {\n" +
        "  var dom = goog.dom;\n" +
        "  dom.createElement('div');\n" +
        "});",
        null);
  }

  @Test
  public void testInnerFunctionReferencingOuterAlias() {
    testScopedAliases(
        "goog.scope(function() {\n" +
        "  var dom = goog.dom;\n" +
        "  function helper() {\n" +
        "    dom.createElement('div');\n" +
        "  }\n" +
        "  helper();\n" +
        "});",
        null);
  }
}