package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

class ProcessClosurePrimitivesTest extends CompilerTestCase {
  private CheckLevel requiresLevel = CheckLevel.ERROR;
  private boolean rewriteNewDateGoogNow = true;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    requiresLevel = CheckLevel.ERROR;
    rewriteNewDateGoogNow = true;
    enableTypeCheck(CheckLevel.OFF);
  }

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new ProcessClosurePrimitives(compiler, requiresLevel, rewriteNewDateGoogNow);
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  @Test
  public void testProvideSimple() {
    test("goog.provide('foo');", "var foo={};");
  }

  @Test
  public void testProvideMultiple() {
    test("goog.provide('foo.bar');", "var foo={}; foo.bar={};");
  }

  @Test
  public void testProvideHierarchy() {
    test("goog.provide('foo.bar.baz');", "var foo={}; foo.bar={}; foo.bar.baz={};");
  }

  @Test
  public void testProvideWithDefinitionAssignment() {
    test("goog.provide('foo.bar'); foo.bar = function() {};",
         "var foo = {}; foo.bar = function() {};");
  }

  @Test
  public void testProvideWithDefinitionVar() {
    test("goog.provide('foo'); var foo = 10;",
         "var foo = 10;");
  }

  @Test
  public void testProvideWithAssignConvertedToVar() {
    test("goog.provide('foo'); foo = 10;",
         "var foo = 10;");
  }

  @Test
  public void testRequireSimple() {
    test("goog.provide('foo'); goog.require('foo');", "var foo={};");
  }

  @Test
  public void testMissingProvide() {
    testError("goog.require('foo');", ProcessClosurePrimitives.MISSING_PROVIDE_ERROR);
  }

  @Test
  public void testLateProvide() {
    testError("goog.require('foo'); goog.provide('foo');", ProcessClosurePrimitives.LATE_PROVIDE_ERROR);
  }

  @Test
  public void testDuplicateProvide() {
    testError("goog.provide('foo'); goog.provide('foo');", ProcessClosurePrimitives.DUPLICATE_NAMESPACE_ERROR);
  }

  @Test
  public void testInvalidProvideIdentifier() {
    testError("goog.provide('foo.123');", ProcessClosurePrimitives.INVALID_PROVIDE_ERROR);
  }

  @Test
  public void testNullArgumentProvide() {
    testError("goog.provide();", ProcessClosurePrimitives.NULL_ARGUMENT_ERROR);
  }

  @Test
  public void testTooManyArgumentsProvide() {
    testError("goog.provide('foo', 'bar');", ProcessClosurePrimitives.TOO_MANY_ARGUMENTS_ERROR);
  }

  @Test
  public void testNonStringArgumentProvide() {
    testError("goog.provide(123);", ProcessClosurePrimitives.INVALID_ARGUMENT_ERROR);
  }

  @Test
  public void testNullArgumentRequire() {
    testError("goog.require();", ProcessClosurePrimitives.NULL_ARGUMENT_ERROR);
  }

  @Test
  public void testTooManyArgumentsRequire() {
    testError("goog.require('foo', 'bar');", ProcessClosurePrimitives.TOO_MANY_ARGUMENTS_ERROR);
  }

  @Test
  public void testNonStringArgumentRequire() {
    testError("goog.require(123);", ProcessClosurePrimitives.INVALID_ARGUMENT_ERROR);
  }

  @Test
  public void testFunctionNamespaceError() {
    testError("goog.provide('foo'); function foo() {}", ProcessClosurePrimitives.FUNCTION_NAMESPACE_ERROR);
  }

  @Test
  public void testExportSymbol() {
    Compiler compiler = new Compiler();
    ProcessClosurePrimitives pass = new ProcessClosurePrimitives(compiler, CheckLevel.ERROR, true);
    Node root = compiler.parseTestCode("goog.exportSymbol('a.b.c', x); goog.exportSymbol('d', y);");
    pass.process(null, root);
    Set<String> exported = pass.getExportedVariableNames();
    Assert.assertTrue(exported.contains("a"));
    Assert.assertTrue(exported.contains("d"));
    Assert.assertFalse(exported.contains("b"));
  }

  @Test
  public void testAddDependency() {
    test("goog.addDependency('x.js', ['a'], ['b']);", "0;");
  }

  @Test
  public void testSetCssNameMappingValid() {
    test("goog.setCssNameMapping({foo: 'bar'});", "");
    Assert.assertNotNull(getLastCompiler().getCssRenamingMap());
    Assert.assertEquals("bar", getLastCompiler().getCssRenamingMap().get("foo"));
    Assert.assertEquals("unknown", getLastCompiler().getCssRenamingMap().get("unknown"));
  }

  @Test
  public void testSetCssNameMappingInvalidObject() {
    testError("goog.setCssNameMapping(123);", ProcessClosurePrimitives.INVALID_ARGUMENT_ERROR);
  }

  @Test
  public void testSetCssNameMappingNonStringValues() {
    testError("goog.setCssNameMapping({foo: 123});", ProcessClosurePrimitives.NON_STRING_PASSED_TO_SET_CSS_NAME_MAPPING_ERROR);
  }

  @Test
  public void testRewriteNewDateGoogNow() {
    rewriteNewDateGoogNow = true;
    test("var d = new Date(goog.now());", "var d = new Date();");
  }

  @Test
  public void testNoRewriteNewDateWhenDisabled() {
    rewriteNewDateGoogNow = false;
    testSame("var d = new Date(goog.now());");
  }

  @Test
  public void testRewriteNewDateGoogNowIgnoredWithArgs() {
    rewriteNewDateGoogNow = true;
    testSame("var d = new Date(goog.now(), 1);");
  }

  @Test
  public void testRewriteNewDateGoogNowIgnoredOtherFn() {
    rewriteNewDateGoogNow = true;
    testSame("var d = new Date(foo());");
  }

  @Test
  public void testBaseClassInConstructor() {
    test("function Foo() { goog.base(this); } goog.inherits(Foo, BaseFoo);",
         "function Foo() { BaseFoo.call(this); } goog.inherits(Foo, BaseFoo);");
  }

  @Test
  public void testBaseClassInMethod() {
    test("function Foo() {} goog.inherits(Foo, BaseFoo);" +
         "Foo.prototype.bar = function() { goog.base(this, 'bar', 1); };",
         "function Foo() {} goog.inherits(Foo, BaseFoo);" +
         "Foo.prototype.bar = function() { Foo.superClass_.bar.call(this, 1); };");
  }

  @Test
  public void testBaseClassNoThis() {
    testError("function Foo() { goog.base(); }", ProcessClosurePrimitives.BASE_CLASS_ERROR);
  }

  @Test
  public void testBaseClassNotInMethod() {
    testError("goog.base(this);", ProcessClosurePrimitives.BASE_CLASS_ERROR);
  }

  @Test
  public void testBaseClassConstructorMissingInherits() {
    testError("function Foo() { goog.base(this); }", ProcessClosurePrimitives.BASE_CLASS_ERROR);
  }

  @Test
  public void testBaseClassMethodMissingMethodArg() {
    testError("Foo.prototype.bar = function() { goog.base(this); };", ProcessClosurePrimitives.BASE_CLASS_ERROR);
  }

  @Test
  public void testBaseClassMethodMismatchedMethodArg() {
    testError("Foo.prototype.bar = function() { goog.base(this, 'baz'); };", ProcessClosurePrimitives.BASE_CLASS_ERROR);
  }

  @Test
  public void testBaseClassInvalidPropertyUse() {
    testError("var x = goog.base;", ProcessClosurePrimitives.BASE_CLASS_ERROR);
  }

  @Test
  public void testRequireOffLevel() {
    requiresLevel = CheckLevel.OFF;
    testSame("goog.require('missing.namespace');");
  }

  @Test
  public void testModulesAcrossModuleGraph() {
    JSModule[] modules = new JSModule[2];
    modules[0] = new JSModule("m0");
    modules[0].add(SourceFile.fromCode("m0.js", "goog.provide('foo.bar');"));
    modules[1] = new JSModule("m1");
    modules[1].add(SourceFile.fromCode("m1.js", "goog.require('foo.bar');"));
    modules[1].addDependency(modules[0]);

    test(modules, new String[] {
      "var foo = {}; foo.bar = {};",
      ""
    });
  }

  @Test
  public void testModulesAcrossModuleGraphError() {
    JSModule[] modules = new JSModule[2];
    modules[0] = new JSModule("m0");
    modules[0].add(SourceFile.fromCode("m0.js", "goog.require('foo.bar');"));
    modules[1] = new JSModule("m1");
    modules[1].add(SourceFile.fromCode("m1.js", "goog.provide('foo.bar');"));

    testWarning(modules, ProcessClosurePrimitives.XMODULE_REQUIRE_ERROR);
  }

  @Test
  public void testImplicitProvideAcrossModules() {
    JSModule[] modules = new JSModule[3];
    modules[0] = new JSModule("base");
    modules[0].add(SourceFile.fromCode("base.js", ""));
    modules[1] = new JSModule("m1");
    modules[1].add(SourceFile.fromCode("m1.js", "goog.provide('ns.a');"));
    modules[1].addDependency(modules[0]);
    modules[2] = new JSModule("m2");
    modules[2].add(SourceFile.fromCode("m2.js", "goog.provide('ns.b');"));
    modules[2].addDependency(modules[0]);

    test(modules, new String[] {
      "var ns = {};",
      "ns.a = {};",
      "ns.b = {};"
    });
  }
}