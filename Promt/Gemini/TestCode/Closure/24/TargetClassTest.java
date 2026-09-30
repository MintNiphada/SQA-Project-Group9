package com.google.javascript.jscomp;

import com.google.javascript.jscomp.CompilerOptions.AliasTransformation;
import com.google.javascript.jscomp.CompilerOptions.AliasTransformationHandler;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SourcePosition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TargetClassTest {
  private Compiler compiler;
  private TestAliasTransformationHandler transformationHandler;

  private static class TestAliasTransformationHandler implements AliasTransformationHandler {
    final Map<String, List<String>> aliases = new HashMap<String, List<String>>();
    final List<SourcePosition<AliasTransformation>> positions =
        new ArrayList<SourcePosition<AliasTransformation>>();

    @Override
    public AliasTransformation logAliasTransformation(
        String sourceFile, final SourcePosition<AliasTransformation> position) {
      positions.add(position);
      return new AliasTransformation() {
        @Override
        public void addAlias(String alias, String definition) {
          if (!aliases.containsKey(alias)) {
            aliases.put(alias, new ArrayList<String>());
          }
          aliases.get(alias).add(definition);
        }
      };
    }
  }

  @Before
  public void setUp() {
    compiler = new Compiler();
    transformationHandler = new TestAliasTransformationHandler();
  }

  private Node test(String js, DiagnosticType expectedError) {
    Node root = compiler.parseTestCode(js);
    ScopedAliases pass = new ScopedAliases(compiler, null, transformationHandler);
    pass.process(null, root);

    if (expectedError != null) {
      Assert.assertTrue("Expected at least one error", compiler.getErrorCount() > 0);
      boolean found = false;
      for (JSError error : compiler.getErrors()) {
        if (error.getType().key.equals(expectedError.key)) {
          found = true;
          break;
        }
      }
      Assert.assertTrue("Expected error " + expectedError.key + " but got " +
          (compiler.getErrorCount() > 0 ? compiler.getErrors()[0].getType().key : "none"), found);
    } else {
      Assert.assertEquals("Expected no errors: " +
          (compiler.getErrorCount() > 0 ? compiler.getErrors()[0].getDescription() : ""),
          0, compiler.getErrorCount());
    }
    return root;
  }

  private Node test(String js) {
    return test(js, null);
  }

  @Test
  public void testSimpleAliasTransformation() {
    String js = "goog.scope(function() {\n" +
                "  var dom = goog.dom;\n" +
                "  var DIV = dom.TagName.DIV;\n" +
                "  dom.createElement(DIV);\n" +
                "});";
    Node root = test(js);
    String result = compiler.toSource(root);
    Assert.assertTrue(result.contains("goog.dom.createElement(goog.dom.TagName.DIV)"));
    Assert.assertTrue(transformationHandler.aliases.containsKey("dom"));
    Assert.assertTrue(transformationHandler.aliases.containsKey("DIV"));
  }

  @Test
  public void testMultipleVarDeclarationsInOneStatement() {
    String js = "goog.scope(function() {\n" +
                "  var dom = goog.dom, DIV = dom.TagName.DIV;\n" +
                "  dom.createElement(DIV);\n" +
                "});";
    Node root = test(js);
    String result = compiler.toSource(root);
    Assert.assertTrue(result.contains("goog.dom.createElement(goog.dom.TagName.DIV)"));
  }

  @Test
  public void testScopeWithPreprocessorSymbolTable() {
    String js = "goog.scope(function() {\n" +
                "  var dom = goog.dom;\n" +
                "  dom.createElement('div');\n" +
                "});";
    Node root = compiler.parseTestCode(js);
    PreprocessorSymbolTable symbolTable = new PreprocessorSymbolTable(root);
    ScopedAliases pass = new ScopedAliases(compiler, symbolTable, transformationHandler);
    pass.process(null, root);

    Assert.assertEquals(0, compiler.getErrorCount());
    String result = compiler.toSource(root);
    Assert.assertTrue(result.contains("goog.dom.createElement(\"div\")") ||
                      result.contains("goog.dom.createElement('div')"));
  }

  @Test
  public void testErrorScopeUsedImproperly() {
    String js = "var x = goog.scope(function() {});";
    test(js, ScopedAliases.GOOG_SCOPE_USED_IMPROPERLY);
  }

  @Test
  public void testErrorScopeHasBadParametersNoArgs() {
    String js = "goog.scope();";
    test(js, ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  @Test
  public void testErrorScopeHasBadParametersTooManyArgs() {
    String js = "goog.scope(function() {}, 123);";
    test(js, ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  @Test
  public void testErrorScopeHasBadParametersNonFunction() {
    String js = "goog.scope(123);";
    test(js, ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  @Test
  public void testErrorScopeHasBadParametersNamedFunction() {
    String js = "goog.scope(function myScope() {});";
    test(js, ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  @Test
  public void testErrorScopeHasBadParametersWithParams() {
    String js = "goog.scope(function(a) {});";
    test(js, ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  @Test
  public void testErrorScopeReferencesThis() {
    String js = "goog.scope(function() {\n" +
                "  this.foo = 1;\n" +
                "});";
    test(js, ScopedAliases.GOOG_SCOPE_REFERENCES_THIS);
  }

  @Test
  public void testErrorScopeUsesReturn() {
    String js = "goog.scope(function() {\n" +
                "  return 1;\n" +
                "});";
    test(js, ScopedAliases.GOOG_SCOPE_USES_RETURN);
  }

  @Test
  public void testErrorScopeUsesThrow() {
    String js = "goog.scope(function() {\n" +
                "  throw 'error';\n" +
                "});";
    test(js, ScopedAliases.GOOG_SCOPE_USES_THROW);
  }

  @Test
  public void testErrorScopeAliasRedefined() {
    String js = "goog.scope(function() {\n" +
                "  var dom = goog.dom;\n" +
                "  dom = goog.other;\n" +
                "});";
    test(js, ScopedAliases.GOOG_SCOPE_ALIAS_REDEFINED);
  }

  @Test
  public void testErrorScopeNonAliasLocalLiteral() {
    String js = "goog.scope(function() {\n" +
                "  var x = 123;\n" +
                "});";
    test(js, ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
  }

  @Test
  public void testErrorScopeNonAliasLocalUninitialized() {
    String js = "goog.scope(function() {\n" +
                "  var x;\n" +
                "});";
    test(js, ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
  }

  @Test
  public void testErrorScopeNonAliasLocalFunctionCall() {
    String js = "goog.scope(function() {\n" +
                "  var x = foo();\n" +
                "});";
    test(js, ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
  }

  @Test
  public void testTypeNodeAliasing() {
    String js = "goog.scope(function() {\n" +
                "  var Button = goog.ui.Button;\n" +
                "  /** @type {Button} */ var b;\n" +
                "  /** @type {Button.EventType} */ var e;\n" +
                "});";
    Node root = test(js);
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testNestedFunctionTraversedProperly() {
    String js = "function globalFn() { var y = 1; }\n" +
                "goog.scope(function() {\n" +
                "  var dom = goog.dom;\n" +
                "  function inner() {\n" +
                "    dom.createElement('div');\n" +
                "  }\n" +
                "});";
    Node root = test(js);
    String result = compiler.toSource(root);
    Assert.assertTrue(result.contains("goog.dom.createElement"));
  }

  @Test
  public void testEmptyGoogScopeBlock() {
    String js = "goog.scope(function() {});";
    Node root = test(js);
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testHotSwapScriptDirect() {
    String js = "goog.scope(function() {\n" +
                "  var dom = goog.dom;\n" +
                "  dom.createElement('div');\n" +
                "});";
    Node root = compiler.parseTestCode(js);
    ScopedAliases pass = new ScopedAliases(compiler, null, transformationHandler);
    pass.hotSwapScript(root, null);
    Assert.assertEquals(0, compiler.getErrorCount());
    String result = compiler.toSource(root);
    Assert.assertTrue(result.contains("goog.dom.createElement"));
  }
}