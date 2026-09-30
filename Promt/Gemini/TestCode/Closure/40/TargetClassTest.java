package com.google.javascript.jscomp;

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Test;

public class NameAnalyzerTest {

  private Compiler createCompiler() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCodingConvention(new ClosureCodingConvention());
    compiler.initOptions(options);
    return compiler;
  }

  private String testTransform(String externsJs, String js, boolean removeUnreferenced) {
    Compiler compiler = createCompiler();
    Node externs = compiler.parseTestCode(externsJs);
    Node root = compiler.parseTestCode(js);
    NameAnalyzer analyzer = new NameAnalyzer(compiler, removeUnreferenced);
    analyzer.process(externs, root);
    return compiler.toSource(root);
  }

  private String testTransform(String js, boolean removeUnreferenced) {
    return testTransform("var window; function alert(x) {}", js, removeUnreferenced);
  }

  private NameAnalyzer processAndGetAnalyzer(String externsJs, String js, boolean removeUnreferenced) {
    Compiler compiler = createCompiler();
    Node externs = compiler.parseTestCode(externsJs);
    Node root = compiler.parseTestCode(js);
    NameAnalyzer analyzer = new NameAnalyzer(compiler, removeUnreferenced);
    analyzer.process(externs, root);
    return analyzer;
  }

  @Test
  public void testRemoveUnusedVariableAndFunction() {
    String js = "var unused = 1; function unusedFn() { return 2; }";
    String result = testTransform(js, true);
    Assert.assertFalse(result.contains("unused"));
    Assert.assertFalse(result.contains("unusedFn"));
  }

  @Test
  public void testKeepReferencedVariableAndFunction() {
    String js = "var used = 1; function usedFn() { return used; } window['out'] = usedFn();";
    String result = testTransform(js, true);
    Assert.assertTrue(result.contains("used"));
    Assert.assertTrue(result.contains("usedFn"));
  }

  @Test
  public void testRemoveUnreferencedPrototype() {
    String js = "function Foo() {} Foo.prototype.bar = function() { return 1; };";
    String result = testTransform(js, true);
    Assert.assertFalse(result.contains("Foo"));
    Assert.assertFalse(result.contains("bar"));
  }

  @Test
  public void testKeepReferencedPrototype() {
    String js = "function Foo() {} Foo.prototype.bar = function() { return 1; }; window['foo'] = new Foo().bar();";
    String result = testTransform(js, true);
    Assert.assertTrue(result.contains("Foo"));
    Assert.assertTrue(result.contains("bar"));
  }

  @Test
  public void testInheritanceRemoval() {
    String js = "function Super() {} function Sub() {} goog.inherits(Sub, Super);";
    String result = testTransform(js, true);
    Assert.assertFalse(result.contains("Super"));
    Assert.assertFalse(result.contains("Sub"));
    Assert.assertFalse(result.contains("inherits"));
  }

  @Test
  public void testInheritanceKeptWhenSubReferenced() {
    String js = "function Super() {} function Sub() {} goog.inherits(Sub, Super); window['Sub'] = new Sub();";
    String result = testTransform(js, true);
    Assert.assertTrue(result.contains("Super"));
    Assert.assertTrue(result.contains("Sub"));
    Assert.assertTrue(result.contains("inherits"));
  }

  @Test
  public void testSingletonGetterRemoved() {
    String js = "function Singleton() {} goog.addSingletonGetter(Singleton);";
    String result = testTransform(js, true);
    Assert.assertFalse(result.contains("Singleton"));
    Assert.assertFalse(result.contains("addSingletonGetter"));
  }

  @Test
  public void testSingletonGetterKept() {
    String js = "function Singleton() {} goog.addSingletonGetter(Singleton); window['inst'] = Singleton.getInstance();";
    String result = testTransform(js, true);
    Assert.assertTrue(result.contains("Singleton"));
    Assert.assertTrue(result.contains("addSingletonGetter"));
  }

  @Test
  public void testInstanceOfRemoval() {
    String js = "function Target() {} var result = obj instanceof Target; window['res'] = result;";
    String result = testTransform(js, true);
    Assert.assertFalse(result.contains("Target"));
    Assert.assertTrue(result.contains("false"));
  }

  @Test
  public void testAliasing() {
    String js = "var a = {}; var b = a; a.foo = 3; window['out'] = b.foo;";
    String result = testTransform(js, true);
    Assert.assertTrue(result.contains("foo"));
    Assert.assertTrue(result.contains("b"));
  }

  @Test
  public void testAliasingMultipleChains() {
    String js = "var a = {}; var b = a; var c = b; a.foo = 3; window['out'] = c.foo;";
    String result = testTransform(js, true);
    Assert.assertTrue(result.contains("foo"));
  }

  @Test
  public void testForLoopScope() {
    String js = "for (var i = 0; i < 10; i++) { window['out'] = i; }";
    String result = testTransform(js, true);
    Assert.assertTrue(result.contains("i"));
  }

  @Test
  public void testForInLoopScope() {
    String js = "for (var key in window) { window['out'] = key; }";
    String result = testTransform(js, true);
    Assert.assertTrue(result.contains("key"));
  }

  @Test
  public void testRemoveUnreferencedInForLoopInit() {
    String js = "for (var unused = 0; false; ) {}";
    String result = testTransform(js, true);
    Assert.assertFalse(result.contains("unused"));
  }

  @Test
  public void testControlStructures() {
    String js =
        "if (window['cond']) { var a = 1; window['a'] = a; } " +
        "while (window['cond2']) { window['b'] = 2; } " +
        "do { window['c'] = 3; } while (window['cond3']); " +
        "switch (window['cond4']) { case 1: window['d'] = 4; break; } " +
        "try { window['e'] = 5; } catch (ex) { throw ex; }";
    String result = testTransform(js, true);
    Assert.assertTrue(result.contains("window.a"));
    Assert.assertTrue(result.contains("window.b"));
    Assert.assertTrue(result.contains("window.c"));
    Assert.assertTrue(result.contains("window.d"));
    Assert.assertTrue(result.contains("window.e"));
  }

  @Test
  public void testGlobalThisProperty() {
    String js = "this.externalProp = 123;";
    String result = testTransform(js, true);
    Assert.assertTrue(result.contains("externalProp"));
  }

  @Test
  public void testExternallyDeclaredName() {
    String externs = "var extVar;";
    String js = "extVar = 10; window['res'] = extVar;";
    String result = testTransform(externs, js, true);
    Assert.assertTrue(result.contains("extVar"));
  }

  @Test
  public void testObjectLiteralKeySetter() {
    String js = "var ns = { prop1: 1, prop2: 2 }; window['out'] = ns.prop1;";
    String result = testTransform(js, true);
    Assert.assertTrue(result.contains("prop1"));
  }

  @Test
  public void testHtmlReportGeneration() {
    String js =
        "function SuperClass() {} " +
        "SuperClass.prototype.foo = function() {}; " +
        "function SubClass() {} " +
        "goog.inherits(SubClass, SuperClass); " +
        "window['SubClass'] = SubClass;";
    NameAnalyzer analyzer = processAndGetAnalyzer("var window;", js, false);
    String report = analyzer.getHtmlReport();
    Assert.assertNotNull(report);
    Assert.assertTrue(report.contains("<html>"));
    Assert.assertTrue(report.contains("OVERALL STATS"));
    Assert.assertTrue(report.contains("Total Names:"));
    Assert.assertTrue(report.contains("ALL NAMES"));
    Assert.assertTrue(report.contains("SuperClass"));
    Assert.assertTrue(report.contains("SubClass"));
  }

  @Test
  public void testProcessWithoutRemoval() {
    String js = "var unused = 1; function unusedFn() {}";
    String result = testTransform(js, false);
    Assert.assertTrue(result.contains("unused"));
    Assert.assertTrue(result.contains("unusedFn"));
  }

  @Test
  public void testSideEffectsPreservedInRemoval() {
    String js = "function sideEffect() { window['flag'] = true; } var unused = sideEffect();";
    String result = testTransform(js, true);
    Assert.assertTrue(result.contains("sideEffect()"));
    Assert.assertFalse(result.contains("unused"));
  }

  @Test
  public void testNestedPrototypeAssignment() {
    String js = "var A = {}; A.B = function() {}; A.B.prototype.m = function() {}; window['A'] = A;";
    String result = testTransform(js, true);
    Assert.assertTrue(result.contains("A.B"));
    Assert.assertTrue(result.contains("prototype.m"));
  }

  @Test
  public void testShortCircuitSideEffects() {
    String js = "var unused = window['cond'] && alert('keep');";
    String result = testTransform(js, true);
    Assert.assertFalse(result.contains("unused"));
    Assert.assertTrue(result.contains("alert"));
  }

  @Test
  public void testHookSideEffects() {
    String js = "var unused = window['cond'] ? alert('1') : alert('2');";
    String result = testTransform(js, true);
    Assert.assertFalse(result.contains("unused"));
    Assert.assertTrue(result.contains("alert(\"1\")"));
    Assert.assertTrue(result.contains("alert(\"2\")"));
  }

  @Test
  public void testCircularDependencyElimination() {
    String js =
        "function a() { b(); } " +
        "function b() { a(); } " +
        "var c = 123;";
    String result = testTransform(js, true);
    Assert.assertFalse(result.contains("function a"));
    Assert.assertFalse(result.contains("function b"));
    Assert.assertFalse(result.contains("c"));
  }

  @Test
  public void testGetElemPropertyWrite() {
    String js = "var a = {}; a['foo'] = 1; window['out'] = a;";
    String result = testTransform(js, true);
    Assert.assertTrue(result.contains("a"));
    Assert.assertTrue(result.contains("foo"));
  }
}