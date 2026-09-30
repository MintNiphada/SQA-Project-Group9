package com.google.javascript.jscomp;

import com.google.javascript.jscomp.CompilerOptions.AliasTransformation;
import com.google.javascript.jscomp.CompilerOptions.AliasTransformationHandler;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SourcePosition;
import org.junit.Test;

public class ScopedAliasesTest extends CompilerTestCase {

  private static final AliasTransformationHandler NULL_ALIAS_TRANSFORMATION_HANDLER =
      new AliasTransformationHandler() {
        @Override
        public AliasTransformation logAliasTransformation(
            String sourceFile, SourcePosition<AliasTransformation> position) {
          return new AliasTransformation() {
            @Override
            public void addAlias(String alias, String qualifiedName) {}
          };
        }
      };

  private PreprocessorSymbolTable preprocessorSymbolTable;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    preprocessorSymbolTable = null;
  }

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new ScopedAliases(compiler, preprocessorSymbolTable, NULL_ALIAS_TRANSFORMATION_HANDLER);
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  @Test
  public void testBasicAliasing() {
    test(
        "goog.scope(function() {\n" +
        "  var dom = goog.dom;\n" +
        "  dom.createElement('div');\n" +
        "});",
        "goog.dom.createElement('div');");
  }

  @Test
  public void testMultipleAliases() {
    test(
        "goog.scope(function() {\n" +
        "  var dom = goog.dom;\n" +
        "  var events = goog.events;\n" +
        "  dom.createElement('div');\n" +
        "  events.listen();\n" +
        "});",
        "goog.dom.createElement('div');\n" +
        "goog.events.listen();");
  }

  @Test
  public void testChainedAliases() {
    test(
        "goog.scope(function() {\n" +
        "  var g = goog;\n" +
        "  var d = g.dom;\n" +
        "  var el = d.createElement;\n" +
        "  el('div');\n" +
        "});",
        "goog.dom.createElement('div');");
  }

  @Test
  public void testMultipleDeclarationsInSingleVar() {
    test(
        "goog.scope(function() {\n" +
        "  var d = goog.dom, e = goog.events;\n" +
        "  d.createElement('div');\n" +
        "  e.listen();\n" +
        "});",
        "goog.dom.createElement('div');\n" +
        "goog.events.listen();");
  }

  @Test
  public void testNonAliasLocalVariable() {
    test(
        "goog.scope(function() {\n" +
        "  var x = 1;\n" +
        "  var y = function() { return x; };\n" +
        "});",
        "$jscomp.scope.x = 1;\n" +
        "var x = $jscomp.scope.x;\n" +
        "$jscomp.scope.y = function() { return $jscomp.scope.x; };\n" +
        "var y = $jscomp.scope.y;");
  }

  @Test
  public void testNonAliasLocalFunction() {
    test(
        "goog.scope(function() {\n" +
        "  var x = 1;\n" +
        "  function f() { return x; }\n" +
        "  f();\n" +
        "});",
        "$jscomp.scope.x = 1;\n" +
        "var x = $jscomp.scope.x;\n" +
        "function f() { return $jscomp.scope.x; }\n" +
        "f();");
  }

  @Test
  public void testNonAliasLocalsRepeatedNames() {
    test(
        "goog.scope(function() {\n" +
        "  var x = 10;\n" +
        "});\n" +
        "goog.scope(function() {\n" +
        "  var x = 20;\n" +
        "});",
        "$jscomp.scope.x = 10;\n" +
        "var x = $jscomp.scope.x;\n" +
        "$jscomp.scope.x$1 = 20;\n" +
        "var x$1 = $jscomp.scope.x$1;");
  }

  @Test
  public void testAliasCycle() {
    testError(
        "goog.scope(function() {\n" +
        "  var a = b;\n" +
        "  var b = a;\n" +
        "});",
        ScopedAliases.GOOG_SCOPE_ALIAS_CYCLE);
  }

  @Test
  public void testImproperScopeUse() {
    testError("var x = goog.scope(function() {});", ScopedAliases.GOOG_SCOPE_USED_IMPROPERLY);
    testError("if (true) goog.scope(function() {});", ScopedAliases.GOOG_SCOPE_USED_IMPROPERLY);
  }

  @Test
  public void testBadParameters() {
    testError("goog.scope();", ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
    testError("goog.scope(1);", ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
    testError("goog.scope(function(a) {});", ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
    testError("goog.scope(function foo() {});", ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
    testError("goog.scope(function() {}, 1);", ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  @Test
  public void testReturnInScope() {
    testError(
        "goog.scope(function() {\n" +
        "  return;\n" +
        "});",
        ScopedAliases.GOOG_SCOPE_USES_RETURN);
  }

  @Test
  public void testThisInScope() {
    testError(
        "goog.scope(function() {\n" +
        "  this.foo = 1;\n" +
        "});",
        ScopedAliases.GOOG_SCOPE_REFERENCES_THIS);
  }

  @Test
  public void testThrowInScope() {
    testError(
        "goog.scope(function() {\n" +
        "  throw 'error';\n" +
        "});",
        ScopedAliases.GOOG_SCOPE_USES_THROW);
  }

  @Test
  public void testAliasRedefined() {
    testError(
        "goog.scope(function() {\n" +
        "  var dom = goog.dom;\n" +
        "  dom = goog.events;\n" +
        "});",
        ScopedAliases.GOOG_SCOPE_ALIAS_REDEFINED);
  }

  @Test
  public void testCatchParamReported() {
    testError(
        "goog.scope(function() {\n" +
        "  try {} catch (e) {}\n" +
        "});",
        ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
  }

  @Test
  public void testJsDocTypeAliasing() {
    test(
        "goog.scope(function() {\n" +
        "  var Button = goog.ui.Button;\n" +
        "  /** @type {Button} */ var b;\n" +
        "});",
        "/** @type {goog.ui.Button} */ var b;");
  }

  @Test
  public void testJsDocSubtypeAliasing() {
    test(
        "goog.scope(function() {\n" +
        "  var ui = goog.ui;\n" +
        "  /** @type {ui.Button} */ var b;\n" +
        "});",
        "/** @type {goog.ui.Button} */ var b;");
  }

  @Test
  public void testNamespaceShadowing() {
    test(
        "goog.scope(function() {\n" +
        "  var dom = goog.dom;\n" +
        "  function f() {\n" +
        "    var goog = 1;\n" +
        "    dom.createElement('div');\n" +
        "  }\n" +
        "});",
        "function f() {\n" +
        "  var goog$$1 = 1;\n" +
        "  goog.dom.createElement('div');\n" +
        "}");
  }

  @Test
  public void testWithPreprocessorSymbolTable() {
    preprocessorSymbolTable = new PreprocessorSymbolTable(new Node(0));
    test(
        "goog.scope(function() {\n" +
        "  var dom = goog.dom;\n" +
        "  dom.createElement('div');\n" +
        "});",
        "goog.dom.createElement('div');");
  }

  @Test
  public void testHotSwapScript() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    Node root = compiler.parseTestCode(
        "goog.scope(function() {\n" +
        "  var dom = goog.dom;\n" +
        "  dom.createElement('div');\n" +
        "});");

    ScopedAliases pass = new ScopedAliases(compiler, null, NULL_ALIAS_TRANSFORMATION_HANDLER);
    pass.hotSwapScript(root, null);
    assertEquals(0, compiler.getErrorCount());
  }
}