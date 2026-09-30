package com.google.javascript.jscomp;

import org.junit.Test;

public class DevirtualizePrototypeMethodsTest extends CompilerTestCase {

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new DevirtualizePrototypeMethods(compiler);
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  @Test
  public void testSimpleMethodDevirtualization() {
    test("A.prototype.foo = function() { return this.x; };\n" +
         "var a = new A();\n" +
         "a.foo();",
         "var JSCompiler_StaticMethods_foo = function(JSCompiler_StaticMethods_foo$self) {\n" +
         "  return JSCompiler_StaticMethods_foo$self.x;\n" +
         "};\n" +
         "var a = new A();\n" +
         "JSCompiler_StaticMethods_foo(a);");
  }

  @Test
  public void testMethodWithArguments() {
    test("A.prototype.foo = function(a, b) { return this.x + a + b; };\n" +
         "var a = new A();\n" +
         "a.foo(1, 2);",
         "var JSCompiler_StaticMethods_foo = function(JSCompiler_StaticMethods_foo$self, a, b) {\n" +
         "  return JSCompiler_StaticMethods_foo$self.x + a + b;\n" +
         "};\n" +
         "var a = new A();\n" +
         "JSCompiler_StaticMethods_foo(a, 1, 2);");
  }

  @Test
  public void testMultipleCalls() {
    test("A.prototype.foo = function() { return this.x; };\n" +
         "var a = new A();\n" +
         "a.foo();\n" +
         "a.foo();",
         "var JSCompiler_StaticMethods_foo = function(JSCompiler_StaticMethods_foo$self) {\n" +
         "  return JSCompiler_StaticMethods_foo$self.x;\n" +
         "};\n" +
         "var a = new A();\n" +
         "JSCompiler_StaticMethods_foo(a);\n" +
         "JSCompiler_StaticMethods_foo(a);");
  }

  @Test
  public void testMultipleMethods() {
    test("A.prototype.foo = function() { return this.x; };\n" +
         "A.prototype.bar = function() { return this.y; };\n" +
         "var a = new A();\n" +
         "a.foo();\n" +
         "a.bar();",
         "var JSCompiler_StaticMethods_foo = function(JSCompiler_StaticMethods_foo$self) {\n" +
         "  return JSCompiler_StaticMethods_foo$self.x;\n" +
         "};\n" +
         "var JSCompiler_StaticMethods_bar = function(JSCompiler_StaticMethods_bar$self) {\n" +
         "  return JSCompiler_StaticMethods_bar$self.y;\n" +
         "};\n" +
         "var a = new A();\n" +
         "JSCompiler_StaticMethods_foo(a);\n" +
         "JSCompiler_StaticMethods_bar(a);");
  }

  @Test
  public void testMethodReturningThis() {
    test("A.prototype.foo = function() { return this; };\n" +
         "var a = new A();\n" +
         "a.foo();",
         "var JSCompiler_StaticMethods_foo = function(JSCompiler_StaticMethods_foo$self) {\n" +
         "  return JSCompiler_StaticMethods_foo$self;\n" +
         "};\n" +
         "var a = new A();\n" +
         "JSCompiler_StaticMethods_foo(a);");
  }

  @Test
  public void testMethodModifyingThis() {
    test("A.prototype.foo = function(val) { this.x = val; };\n" +
         "var a = new A();\n" +
         "a.foo(10);",
         "var JSCompiler_StaticMethods_foo = function(JSCompiler_StaticMethods_foo$self, val) {\n" +
         "  JSCompiler_StaticMethods_foo$self.x = val;\n" +
         "};\n" +
         "var a = new A();\n" +
         "JSCompiler_StaticMethods_foo(a, 10);");
  }

  @Test
  public void testNestedFunctionPreservesThis() {
    test("A.prototype.foo = function() {\n" +
         "  var self = this;\n" +
         "  function inner() { return this.y; }\n" +
         "  return self.x + inner();\n" +
         "};\n" +
         "var a = new A();\n" +
         "a.foo();",
         "var JSCompiler_StaticMethods_foo = function(JSCompiler_StaticMethods_foo$self) {\n" +
         "  var self = JSCompiler_StaticMethods_foo$self;\n" +
         "  function inner() { return this.y; }\n" +
         "  return self.x + inner();\n" +
         "};\n" +
         "var a = new A();\n" +
         "JSCompiler_StaticMethods_foo(a);");
  }

  @Test
  public void testVarArgsNotEligible() {
    testSame("A.prototype.foo = function() { return arguments[0]; };\n" +
             "var a = new A();\n" +
             "a.foo(1);");
  }

  @Test
  public void testUnusedMethodNotEligible() {
    testSame("A.prototype.foo = function() { return this.x; };");
  }

  @Test
  public void testDirectPropertyAccessNotEligible() {
    testSame("A.prototype.foo = function() { return this.x; };\n" +
             "var a = new A();\n" +
             "var fn = a.foo;");
  }

  @Test
  public void testMultipleDefinitionsNotEligible() {
    testSame("A.prototype.foo = function() { return this.x; };\n" +
             "B.prototype.foo = function() { return this.y; };\n" +
             "var a = new A();\n" +
             "a.foo();");
  }

  @Test
  public void testControlStructureIfBranchNotEligible() {
    testSame("if (true) {\n" +
             "  A.prototype.foo = function() { return this.x; };\n" +
             "}\n" +
             "var a = new A();\n" +
             "a.foo();");
  }

  @Test
  public void testControlStructureWhileBranchNotEligible() {
    testSame("while (true) {\n" +
             "  A.prototype.foo = function() { return this.x; };\n" +
             "}\n" +
             "var a = new A();\n" +
             "a.foo();");
  }

  @Test
  public void testControlStructureForBranchNotEligible() {
    testSame("for (;;) {\n" +
             "  A.prototype.foo = function() { return this.x; };\n" +
             "}\n" +
             "var a = new A();\n" +
             "a.foo();");
  }

  @Test
  public void testFunctionScopeNotEligible() {
    testSame("function init() {\n" +
             "  A.prototype.foo = function() { return this.x; };\n" +
             "}\n" +
             "var a = new A();\n" +
             "a.foo();");
  }

  @Test
  public void testNonPrototypeMethodNotEligible() {
    testSame("A.foo = function() { return 1; };\n" +
             "A.foo();");
  }

  @Test
  public void testStringKeyPrototypeNotEligible() {
    testSame("A.prototype['foo'] = function() { return 1; };\n" +
             "var a = new A();\n" +
             "a['foo']();");
  }

  @Test
  public void testNonFunctionPrototypePropertyNotEligible() {
    testSame("A.prototype.foo = 123;\n" +
             "var a = new A();\n" +
             "var x = a.foo;");
  }

  @Test
  public void testExternMethodNotEligible() {
    testSame("A.prototype.foo = function() {};",
             "var a = new A();\n" +
             "a.foo();",
             null);
  }

  @Test
  public void testModuleDependencyValid() {
    JSModule m1 = new JSModule("m1");
    m1.add(JSSourceFile.fromCode("m1.js",
        "A.prototype.foo = function() { return this.x; };"));

    JSModule m2 = new JSModule("m2");
    m2.add(JSSourceFile.fromCode("m2.js",
        "var a = new A(); a.foo();"));
    m2.addDependency(m1);

    test(new JSModule[] { m1, m2 }, new String[] {
        "var JSCompiler_StaticMethods_foo = function(JSCompiler_StaticMethods_foo$self) {\n" +
        "  return JSCompiler_StaticMethods_foo$self.x;\n" +
        "};",
        "var a = new A(); JSCompiler_StaticMethods_foo(a);"
    });
  }

  @Test
  public void testModuleDependencyInvalid() {
    JSModule m1 = new JSModule("m1");
    m1.add(JSSourceFile.fromCode("m1.js",
        "var a = new A(); a.foo();"));

    JSModule m2 = new JSModule("m2");
    m2.add(JSSourceFile.fromCode("m2.js",
        "A.prototype.foo = function() { return this.x; };"));

    testSame(new JSModule[] { m1, m2 });
  }

  @Test
  public void testTypeInformationPreserved() {
    enableTypeCheck(CheckLevel.WARNING);
    test("/** @constructor */ function A() { this.x = 1; }\n" +
         "/** @param {number} n\n" +
         " *  @return {number} */\n" +
         "A.prototype.foo = function(n) { return this.x + n; };\n" +
         "var a = new A();\n" +
         "a.foo(2);",
         "/** @constructor */ function A() { this.x = 1; }\n" +
         "var JSCompiler_StaticMethods_foo = function(JSCompiler_StaticMethods_foo$self, n) {\n" +
         "  return JSCompiler_StaticMethods_foo$self.x + n;\n" +
         "};\n" +
         "var a = new A();\n" +
         "JSCompiler_StaticMethods_foo(a, 2);");
  }
}