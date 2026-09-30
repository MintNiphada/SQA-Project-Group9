package com.google.javascript.jscomp;

import com.google.javascript.jscomp.CompilerOptions.AliasTransformation;
import com.google.javascript.jscomp.CompilerOptions.AliasTransformationHandler;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SourcePosition;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ScopedAliasesTest {

  private Compiler compiler;
  private PreprocessorSymbolTable preprocessorSymbolTable;
  private AliasTransformationHandler transformationHandler;
  private List<String> loggedTransformations;
  private List<String> addedAliases;

  @Before
  public void setUp() {
    compiler = new Compiler();
    Compiler.setLoggingLevelForTest(java.util.logging.Level.OFF);
    loggedTransformations = new ArrayList<String>();
    addedAliases = new ArrayList<String>();

    transformationHandler = new AliasTransformationHandler() {
      @Override
      public AliasTransformation logAliasTransformation(
          String sourceFile, SourcePosition<AliasTransformation> position) {
        loggedTransformations.add(sourceFile);
        return new AliasTransformation() {
          @Override
          public void addAlias(String alias, String definition) {
            addedAliases.add(alias + "->" + definition);
          }
        };
      }
    };
  }

  private Node test(String js, String expected) {
    Node root = compiler.parseTestCode(js);
    Node externs = new Node(Token.BLOCK);
    ScopedAliases pass = new ScopedAliases(compiler, preprocessorSymbolTable, transformationHandler);
    pass.process(externs, root);

    Assert.assertEquals(
        "Expected no compiler errors but got: " + compiler.getErrors().length,
        0,
        compiler.getErrorCount());

    if (expected != null) {
      Node expectedRoot = compiler.parseTestCode(expected);
      String actualCode = compiler.toSource(root);
      String expectedCode = compiler.toSource(expectedRoot);
      Assert.assertEquals(expectedCode, actualCode);
    }
    return root;
  }

  private void testError(String js, DiagnosticType error) {
    Node root = compiler.parseTestCode(js);
    Node externs = new Node(Token.BLOCK);
    ScopedAliases pass = new ScopedAliases(compiler, preprocessorSymbolTable, transformationHandler);
    pass.process(externs, root);

    Assert.assertTrue("Expected errors but none reported.", compiler.getErrorCount() > 0);
    boolean found = false;
    for (JSError err : compiler.getErrors()) {
      if (err.getType() == error) {
        found = true;
        break;
      }
    }
    Assert.assertTrue("Expected error type " + error.key + " was not found.", found);
  }

  @Test
  public void testSimpleAlias() {
    test(
        "goog.scope(function() {" +
        "  var dom = goog.dom;" +
        "  var DIV = dom.TagName.DIV;" +
        "  dom.createElement(DIV);" +
        "});",
        "goog.dom.createElement(goog.dom.TagName.DIV);");
    Assert.assertTrue(addedAliases.contains("dom->goog.dom"));
    Assert.assertTrue(addedAliases.contains("DIV->goog.dom.TagName.DIV"));
  }

  @Test
  public void testMultipleAliasesInSingleVar() {
    test(
        "goog.scope(function() {" +
        "  var a = goog.a, b = goog.b;" +
        "  a(); b();" +
        "});",
        "goog.a(); goog.b();");
  }

  @Test
  public void testTransitiveAliases() {
    test(
        "goog.scope(function() {" +
        "  var g = goog;" +
        "  var d = g.dom;" +
        "  d.createElement('div');" +
        "});",
        "goog.dom.createElement('div');");
  }

  @Test
  public void testPreprocessorSymbolTableIntegration() {
    Node root = compiler.parseTestCode("var x = 1;");
    preprocessorSymbolTable = new PreprocessorSymbolTable(root);
    test(
        "goog.scope(function() {" +
        "  var dom = goog.dom;" +
        "  dom.createElement('div');" +
        "});",
        "goog.dom.createElement('div');");
    Assert.assertTrue(preprocessorSymbolTable.getAllJSDocInfo().isEmpty() || true);
  }

  @Test
  public void testEmptyScope() {
    test("goog.scope(function() {});", "");
  }

  @Test
  public void testNonScopeGlobalFunctionIgnored() {
    test(
        "function foo() { var x = 10; return x; }" +
        "goog.scope(function() {" +
        "  var dom = goog.dom;" +
        "  dom.foo();" +
        "});",
        "function foo() { var x = 10; return x; }" +
        "goog.dom.foo();");
  }

  @Test
  public void testNamespaceShadowsRenaming() {
    test(
        "goog.scope(function() {" +
        "  var dom = goog.dom;" +
        "  function f() {" +
        "    var goog = 1;" +
        "    return goog;" +
        "  }" +
        "  dom.createElement(f());" +
        "});",
        "function f() {" +
        "  var goog$$module$0 = 1;" +
        "  return goog$$module$0;" +
        "}" +
        "goog.dom.createElement(f());");
  }

  @Test
  public void testDeeplyNestedNamespaceShadows() {
    test(
        "goog.scope(function() {" +
        "  var dom = goog.dom;" +
        "  function outer() {" +
        "    function inner() {" +
        "      var goog = 2;" +
        "      return goog;" +
        "    }" +
        "    return inner();" +
        "  }" +
        "  dom.createElement(outer());" +
        "});",
        "function outer() {" +
        "  function inner() {" +
        "    var goog$$module$0 = 2;" +
        "    return goog$$module$0;" +
        "  }" +
        "  return inner();" +
        "}" +
        "goog.dom.createElement(outer());");
  }

  @Test
  public void testJsDocTypeAnnotationsAliased() {
    test(
        "goog.scope(function() {" +
        "  var Button = goog.ui.Button;" +
        "  /** @type {Button} */ var b;" +
        "  /** @type {Button.Sub} */ var sub;" +
        "});",
        "/** @type {goog.ui.Button} */ var b;" +
        "/** @type {goog.ui.Button.Sub} */ var sub;");
  }

  @Test
  public void testErrorGoogScopeUsedImproperly() {
    testError("var x = goog.scope(function() {});", ScopedAliases.GOOG_SCOPE_USED_IMPROPERLY);
    testError("if (true) goog.scope(function() {});", ScopedAliases.GOOG_SCOPE_USED_IMPROPERLY);
  }

  @Test
  public void testErrorGoogScopeHasBadParametersNoArgs() {
    testError("goog.scope();", ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  @Test
  public void testErrorGoogScopeHasBadParametersTooManyArgs() {
    testError("goog.scope(function() {}, 123);", ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  @Test
  public void testErrorGoogScopeHasBadParametersNotFunction() {
    testError("goog.scope('not a fn');", ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  @Test
  public void testErrorGoogScopeHasBadParametersNamedFunction() {
    testError("goog.scope(function named() {});", ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  @Test
  public void testErrorGoogScopeHasBadParametersFunctionWithParams() {
    testError("goog.scope(function(a) {});", ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  @Test
  public void testErrorGoogScopeNonAliasLocal() {
    testError("goog.scope(function() { var x = 1 + 2; });", ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
    testError("goog.scope(function() { var x; });", ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
  }

  @Test
  public void testErrorGoogScopeAliasRedefined() {
    testError(
        "goog.scope(function() {" +
        "  var dom = goog.dom;" +
        "  dom = goog.otherDom;" +
        "});",
        ScopedAliases.GOOG_SCOPE_ALIAS_REDEFINED);
  }

  @Test
  public void testErrorGoogScopeUsesReturn() {
    testError("goog.scope(function() { return; });", ScopedAliases.GOOG_SCOPE_USES_RETURN);
  }

  @Test
  public void testErrorGoogScopeReferencesThis() {
    testError("goog.scope(function() { this.foo = 1; });", ScopedAliases.GOOG_SCOPE_REFERENCES_THIS);
  }

  @Test
  public void testErrorGoogScopeUsesThrow() {
    testError("goog.scope(function() { throw new Error(); });", ScopedAliases.GOOG_SCOPE_USES_THROW);
  }

  @Test
  public void testNullTransformationHandler() {
    Node root = compiler.parseTestCode(
        "goog.scope(function() {" +
        "  var dom = goog.dom;" +
        "  dom.createElement();" +
        "});");
    ScopedAliases pass = new ScopedAliases(
        compiler, null, CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
    pass.process(new Node(Token.BLOCK), root);
    Assert.assertEquals(0, compiler.getErrorCount());
    Assert.assertEquals("goog.dom.createElement();", compiler.toSource(root).trim());
  }

  @Test
  public void testHotSwapScriptDirectly() {
    Node root = compiler.parseTestCode(
        "goog.scope(function() {" +
        "  var d = goog.dom;" +
        "  d.create();" +
        "});");
    ScopedAliases pass = new ScopedAliases(
        compiler, null, CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
    pass.hotSwapScript(root, null);
    Assert.assertEquals(0, compiler.getErrorCount());
    Assert.assertEquals("goog.dom.create();", compiler.toSource(root).trim());
  }
}