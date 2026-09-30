package com.google.javascript.jscomp;

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TargetClassTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
  }

  private void test(String js, String expected) {
    test(js, expected, true);
  }

  private void test(String js, String expected, boolean removeUnreferenced) {
    Node externsNode = IR.block();
    Node rootNode = compiler.parseTestCode(js);
    Assert.assertEquals(0, compiler.getErrorCount());

    NameAnalyzer analyzer = new NameAnalyzer(compiler, removeUnreferenced);
    analyzer.process(externsNode, rootNode);

    if (expected != null) {
      String actual = compiler.toSource(rootNode);
      String expectedSource = compiler.toSource(compiler.parseTestCode(expected));
      Assert.assertEquals(expectedSource, actual);
    }
  }

  private void testWithExterns(String externs, String js, String expected) {
    Node externsNode = compiler.parseTestCode(externs);
    Node rootNode = compiler.parseTestCode(js);
    Assert.assertEquals(0, compiler.getErrorCount());

    NameAnalyzer analyzer = new NameAnalyzer(compiler, true);
    analyzer.process(externsNode, rootNode);

    if (expected != null) {
      String actual = compiler.toSource(rootNode);
      String expectedSource = compiler.toSource(compiler.parseTestCode(expected));
      Assert.assertEquals(expectedSource, actual);
    }
  }

  @Test
  public void testSimpleRemoval() {
    test("var x = 1;", "");
    test("var x = 1; window.x = x;", "var x = 1; window.x = x;");
  }

  @Test
  public void testFunctionDeclarations() {
    test("function foo() { return 1; }", "");
    test("function foo() { return 1; } window.foo = foo;", "function foo() { return 1; } window.foo = foo;");
    test("function foo() { var inner = 2; return inner; } window['foo'] = foo;", "function foo() { var inner = 2; return inner; } window['foo'] = foo;");
  }

  @Test
  public void testClassDefinitionsAndInherits() {
    test("function Super() {} function Sub() {} goog.inherits(Sub, Super); window.Sub = Sub;",
         "function Super() {} function Sub() {} goog.inherits(Sub, Super); window.Sub = Sub;");
    test("function Super() {} function Sub() {} goog.inherits(Sub, Super);", "");
  }

  @Test
  public void testPrototypeAssignments() {
    test("function Foo() {} Foo.prototype.bar = function() { return 42; }; window.Foo = Foo;",
         "function Foo() {} Foo.prototype.bar = function() { return 42; }; window.Foo = Foo;");
    test("function Foo() {} Foo.prototype.bar = function() { return 42; };", "");
    test("function Foo() {} Foo.prototype = { bar: function() {} };", "");
  }

  @Test
  public void testInstanceOfRemoval() {
    test("function Foo() {} var isFoo = x instanceof Foo;", "function Foo() {} var isFoo = false;");
    test("function Foo() {} if (x instanceof Foo) { alert(1); }", "function Foo() {} if (false) { alert(1); }");
  }

  @Test
  public void testHtmlReportGeneration() {
    Compiler comp = new Compiler();
    Node externs = comp.parseTestCode("var window;");
    Node root = comp.parseTestCode("function Foo() {} Foo.prototype.bar = function() {}; window.foo = new Foo();");

    NameAnalyzer analyzer = new NameAnalyzer(comp, true);
    analyzer.process(externs, root);

    String report = analyzer.getHtmlReport();
    Assert.assertNotNull(report);
    Assert.assertTrue(report.contains("<html><body>"));
    Assert.assertTrue(report.contains("OVERALL STATS"));
    Assert.assertTrue(report.contains("ALL NAMES"));
  }

  @Test
  public void testAliasing() {
    test("var a = {}; var b = a; a.foo = 3; window.alert(b.foo);",
         "var a = {}; var b = a; a.foo = 3; window.alert(b.foo);");
    test("var a = {}; var b = a; var c = b; window.alert(c);",
         "var a = {}; var b = a; var c = b; window.alert(c);");
  }

  @Test
  public void testComplexScopesAndBranches() {
    test("var x = 1; for (var i = 0; i < 10; i++) { x += i; }", "");
    test("var x = 1; for (var k in window) { x = window[k]; }", "");
    test("var a = 1; var b = 2; var c = a ? b : 3; window.c = c;",
         "var a = 1; var b = 2; var c = a ? b : 3; window.c = c;");
    test("var a = 1, b = 2; var c = (a, b); window.c = c;",
         "var a = 1, b = 2; var c = (a, b); window.c = c;");
  }

  @Test
  public void testExternals() {
    testWithExterns("var extVar = 10; function extFunc() {}",
                    "var a = extVar; window.a = a; extFunc();",
                    "var a = extVar; window.a = a; extFunc();");
    testWithExterns("var extVar = 10;",
                    "var a = extVar;",
                    "");
  }

  @Test
  public void testNestedAssignments() {
    test("var a; var b = a = 3; window.b = b;", "var a; var b = a = 3; window.b = b;");
    test("var a = {}; a.b = 1; a.b.c = 2; window.a = a;", "var a = {}; a.b = 1; a.b.c = 2; window.a = a;");
  }

  @Test
  public void testDoWhileAndConditions() {
    test("var x = 0; do { x++; } while(x < 10);", "");
    test("var x = 0; while(x < 10) { x++; }", "");
    test("var x = 0; switch(x) { case 1: break; default: break; }", "");
  }

  @Test
  public void testRemoveUnreferencedFalse() {
    test("var x = 1; function foo() {}", "var x = 1; function foo() {}", false);
  }
}