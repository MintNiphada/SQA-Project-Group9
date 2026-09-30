package com.google.javascript.jscomp;

import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfoBuilder;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Test;

class CheckGlobalThisTest {

  private Compiler testCompiler(String js, CheckLevel level) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(js);
    CheckGlobalThis checker = new CheckGlobalThis(compiler, level);
    NodeTraversal.traverse(compiler, root, checker);
    return compiler;
  }

  private void testFailure(String js) {
    Compiler compiler = testCompiler(js, CheckLevel.WARNING);
    Assert.assertEquals(1, compiler.getWarningCount());
    Assert.assertEquals(0, compiler.getErrorCount());
    Assert.assertEquals(CheckGlobalThis.GLOBAL_THIS.key, compiler.getWarnings()[0].getType().key);
  }

  private void testFailureError(String js) {
    Compiler compiler = testCompiler(js, CheckLevel.ERROR);
    Assert.assertEquals(0, compiler.getWarningCount());
    Assert.assertEquals(1, compiler.getErrorCount());
    Assert.assertEquals(CheckGlobalThis.GLOBAL_THIS.key, compiler.getErrors()[0].getType().key);
  }

  private void testOk(String js) {
    Compiler compiler = testCompiler(js, CheckLevel.WARNING);
    Assert.assertEquals(0, compiler.getWarningCount());
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testGlobalThisLhsAssignment() {
    testFailure("this.foo = 1;");
    testFailure("this[0] = 1;");
    testFailure("this.a.b = 1;");
  }

  @Test
  public void testGlobalThisPropertyAccess() {
    testFailure("var x = this.foo;");
    testFailure("var x = this[0];");
    testFailure("this.foo();");
  }

  @Test
  public void testGlobalThisErrorLevel() {
    testFailureError("this.foo = 1;");
  }

  @Test
  public void testPureThisUsageNotReported() {
    testOk("var x = this;");
    testOk("foo(this);");
    testOk("return this;");
  }

  @Test
  public void testFunctionConstructorAnnotation() {
    testOk("/** @constructor */ function F() { this.foo = 1; }");
    testOk("/** @constructor */ var F = function() { this.foo = 1; };");
    testOk("var F; /** @constructor */ F = function() { this.foo = 1; };");
  }

  @Test
  public void testFunctionThisAnnotation() {
    testOk("/** @this {Object} */ function f() { this.foo = 1; }");
    testOk("/** @this {Object} */ var f = function() { this.foo = 1; };");
    testOk("var f; /** @this {Object} */ f = function() { this.foo = 1; };");
  }

  @Test
  public void testFunctionOverrideAnnotation() {
    testOk("/** @override */ function f() { this.foo = 1; }");
    testOk("/** @override */ var f = function() { this.foo = 1; };");
    testOk("var f; /** @override */ f = function() { this.foo = 1; };");
  }

  @Test
  public void testUnannotatedFunctionInValidScope() {
    testFailure("function f() { this.foo = 1; }");
    testFailure("var f = function() { this.foo = 1; };");
    testFailure("f = function() { this.foo = 1; };");
  }

  @Test
  public void testFunctionInNonTraversableScope() {
    testOk("foo(function() { this.foo = 1; });");
    testOk("var obj = { f: function() { this.foo = 1; } };");
    testOk("var arr = [function() { this.foo = 1; }];");
  }

  @Test
  public void testPrototypeAssignmentRhs() {
    testOk("A.prototype.foo = function() { this.x = 1; };");
    testOk("A.prototype = { foo: function() { this.x = 1; } };");
    testOk("A.prototype.foo.bar = function() { this.x = 1; };");
  }

  @Test
  public void testNestedAssignment() {
    testFailure("(a = this).property = 1;");
    testFailure("a = this.foo = 3;");
  }

  @Test
  public void testShouldTraverseDirectly() {
    Compiler compiler = new Compiler();
    CheckGlobalThis checker = new CheckGlobalThis(compiler, CheckLevel.WARNING);

    Node scriptNode = new Node(Token.SCRIPT);
    Node assignNode = new Node(Token.ASSIGN);
    Node lhs = Node.newString(Token.NAME, "a");
    Node rhs = new Node(Token.FUNCTION);

    assignNode.addChildToBack(lhs);
    assignNode.addChildToBack(rhs);
    scriptNode.addChildToBack(assignNode);

    NodeTraversal t = new NodeTraversal(compiler, checker);
    Assert.assertTrue(checker.shouldTraverse(t, scriptNode, null));
    Assert.assertTrue(checker.shouldTraverse(t, assignNode, scriptNode));
    Assert.assertTrue(checker.shouldTraverse(t, lhs, assignNode));
    Assert.assertTrue(checker.shouldTraverse(t, rhs, assignNode));

    checker.visit(t, lhs, assignNode);
    checker.visit(t, rhs, assignNode);
    checker.visit(t, assignNode, scriptNode);
  }

  @Test
  public void testGetFunctionJsDocInfoVariousStructures() {
    Compiler compiler = new Compiler();
    CheckGlobalThis checker = new CheckGlobalThis(compiler, CheckLevel.WARNING);

    // Case 1: JSDoc on Function Node itself
    Node fn = new Node(Token.FUNCTION);
    JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
    builder.recordConstructor();
    JSDocInfo info = builder.build(fn);
    fn.setJSDocInfo(info);
    Node block = new Node(Token.BLOCK, fn);
    Assert.assertFalse(checker.shouldTraverse(null, fn, block));

    // Case 2: JSDoc on NAME parent
    Node fn2 = new Node(Token.FUNCTION);
    Node nameNode = Node.newString(Token.NAME, "myFunc");
    nameNode.addChildToBack(fn2);
    nameNode.setJSDocInfo(info);
    Assert.assertFalse(checker.shouldTraverse(null, fn2, nameNode));

    // Case 3: JSDoc on VAR grandparent
    Node fn3 = new Node(Token.FUNCTION);
    Node nameNode2 = Node.newString(Token.NAME, "myVarFunc");
    nameNode2.addChildToBack(fn3);
    Node varNode = new Node(Token.VAR, nameNode2);
    varNode.setJSDocInfo(info);
    Assert.assertFalse(checker.shouldTraverse(null, fn3, nameNode2));

    // Case 4: No JSDoc and parent is not SCRIPT/BLOCK/NAME/ASSIGN
    Node fn4 = new Node(Token.FUNCTION);
    Node exprResult = new Node(Token.EXPR_RESULT, fn4);
    Assert.assertFalse(checker.shouldTraverse(null, fn4, exprResult));
  }
}