package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Test;

public class CheckGlobalThisTest {

  private Compiler testCompiler(String js, int expectedWarnings) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.checkGlobalThisLevel = CheckLevel.WARNING;
    compiler.initOptions(options);
    Node root = compiler.parseTestCode(js);
    CheckGlobalThis callback = new CheckGlobalThis(compiler, CheckLevel.WARNING);
    NodeTraversal.traverse(compiler, root, callback);
    Assert.assertEquals("Warning count mismatch for: " + js,
        expectedWarnings, compiler.getWarningCount());
    if (expectedWarnings > 0) {
      Assert.assertEquals(CheckGlobalThis.GLOBAL_THIS,
          compiler.getWarnings()[0].getType());
    }
    return compiler;
  }

  private Compiler testCompilerError(String js, int expectedErrors) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    Node root = compiler.parseTestCode(js);
    CheckGlobalThis callback = new CheckGlobalThis(compiler, CheckLevel.ERROR);
    NodeTraversal.traverse(compiler, root, callback);
    Assert.assertEquals("Error count mismatch for: " + js,
        expectedErrors, compiler.getErrorCount());
    if (expectedErrors > 0) {
      Assert.assertEquals(CheckGlobalThis.GLOBAL_THIS,
          compiler.getErrors()[0].getType());
    }
    return compiler;
  }

  @Test
  public void testGlobalThisPropertyAccess() {
    testCompiler("this.foo = 1;", 1);
    testCompiler("this.foo;", 1);
    testCompiler("this['foo'] = 1;", 1);
    testCompiler("this['foo'];", 1);
    testCompiler("var x = this.foo;", 1);
    testCompiler("var x = this['foo'];", 1);
  }

  @Test
  public void testGlobalThisWithoutPropertyAccess() {
    testCompiler("var x = this;", 0);
    testCompiler("var x = [this];", 0);
    testCompiler("foo(this);", 0);
    testCompiler("return this;", 0);
  }

  @Test
  public void testGlobalThisOnLhsOfAssign() {
    testCompiler("this.x = 1;", 1);
    testCompiler("this = 1;", 1);
    testCompiler("(a = this).x = 1;", 1);
  }

  @Test
  public void testFunctionsWithoutDoc() {
    testCompiler("function f() { this.foo = 1; }", 1);
    testCompiler("var f = function() { this.foo = 1; };", 1);
    testCompiler("o.f = function() { this.foo = 1; };", 1);
    testCompiler("var o = { f: function() { this.foo = 1; } };", 1);
    testCompiler("var o = { 1: function() { this.foo = 1; } };", 1);
    testCompiler("var o = { 'f': function() { this.foo = 1; } };", 1);
  }

  @Test
  public void testConstructorFunction() {
    testCompiler("/** @constructor */ function F() { this.foo = 1; }", 0);
    testCompiler("/** @constructor */ var F = function() { this.foo = 1; };", 0);
    testCompiler("var /** @constructor */ F = function() { this.foo = 1; };", 0);
    testCompiler("/** @constructor */ F = function() { this.foo = 1; };", 0);
  }

  @Test
  public void testInterfaceFunction() {
    testCompiler("/** @interface */ function F() { this.foo = 1; }", 0);
    testCompiler("/** @interface */ var F = function() { this.foo = 1; };", 0);
    testCompiler("var /** @interface */ F = function() { this.foo = 1; };", 0);
    testCompiler("/** @interface */ F = function() { this.foo = 1; };", 0);
  }

  @Test
  public void testThisTypeFunction() {
    testCompiler("/** @this {Object} */ function f() { this.foo = 1; }", 0);
    testCompiler("/** @this {Object} */ var f = function() { this.foo = 1; };", 0);
    testCompiler("var /** @this {Object} */ f = function() { this.foo = 1; };", 0);
    testCompiler("/** @this {Object} */ f = function() { this.foo = 1; };", 0);
  }

  @Test
  public void testOverrideFunction() {
    testCompiler("/** @override */ function f() { this.foo = 1; }", 0);
    testCompiler("/** @override */ var f = function() { this.foo = 1; };", 0);
    testCompiler("var /** @override */ f = function() { this.foo = 1; };", 0);
    testCompiler("/** @override */ f = function() { this.foo = 1; };", 0);
  }

  @Test
  public void testFunctionWhereDocCannotBePlaced() {
    testCompiler("var arr = [function() { this.foo = 1; }];", 0);
    testCompiler("call(function() { this.foo = 1; });", 0);
    testCompiler("(function() { this.foo = 1; })();", 0);
    testCompiler("foo(1, function() { this.foo = 1; });", 0);
  }

  @Test
  public void testPrototypeAssignments() {
    testCompiler("Foo.prototype = { method: function() { this.foo = 1; } };", 0);
    testCompiler("Foo.prototype = function() { this.foo = 1; };", 0);
    testCompiler("Foo.prototype.method = function() { this.foo = 1; };", 0);
    testCompiler("Foo.bar.prototype.method = function() { this.foo = 1; };", 0);
    testCompiler("Foo.prototype['method'] = function() { this.foo = 1; };", 0);
    testCompiler("Foo.bar.prototype['method'] = function() { this.foo = 1; };", 0);
  }

  @Test
  public void testNonPrototypeAssignments() {
    testCompiler("Foo.bar = function() { this.foo = 1; };", 1);
    testCompiler("Foo.bar.baz = function() { this.foo = 1; };", 1);
  }

  @Test
  public void testCheckLevelError() {
    testCompilerError("this.foo = 1;", 1);
    testCompilerError("var x = this;", 0);
  }

  @Test
  public void testDirectCallbackMethods() {
    Compiler compiler = new Compiler();
    CheckGlobalThis callback = new CheckGlobalThis(compiler, CheckLevel.WARNING);
    NodeTraversal t = new NodeTraversal(compiler, callback);

    Node emptyScript = new Node(Token.SCRIPT);
    Node varNode = new Node(Token.VAR);
    Node nameNode = Node.newString(Token.NAME, "a");
    varNode.addChildToFront(nameNode);
    emptyScript.addChildToFront(varNode);

    Assert.assertTrue(callback.shouldTraverse(t, emptyScript, null));
    Assert.assertTrue(callback.shouldTraverse(t, varNode, emptyScript));

    Node thisNode = new Node(Token.THIS);
    callback.visit(t, thisNode, null);
    Assert.assertEquals(0, compiler.getWarningCount());

    Node getPropNode = new Node(Token.GETPROP, thisNode, Node.newString("foo"));
    callback.visit(t, thisNode, getPropNode);
    Assert.assertEquals(1, compiler.getWarningCount());

    Node assignNode = new Node(Token.ASSIGN, thisNode, Node.newNumber(1));
    boolean shouldTravLhs = callback.shouldTraverse(t, thisNode, assignNode);
    Assert.assertTrue(shouldTravLhs);
    callback.visit(t, thisNode, assignNode);
    Assert.assertEquals(2, compiler.getWarningCount());
  }
}