package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfoBuilder;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

public class CheckGlobalThisTest {

  private CheckGlobalThis createCheck(Compiler compiler, CheckLevel level) {
    return new CheckGlobalThis(compiler, level);
  }

  private void test(String js, DiagnosticType expectedWarning) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(js);
    CheckGlobalThis check = new CheckGlobalThis(compiler, CheckLevel.WARNING);
    NodeTraversal.traverse(compiler, root, check);
    if (expectedWarning != null) {
      assertEquals(1, compiler.getWarningCount());
      assertEquals(expectedWarning, compiler.getWarnings()[0].getType());
    } else {
      assertEquals(0, compiler.getWarningCount());
      assertEquals(0, compiler.getErrorCount());
    }
  }

  private void testSame(String js) {
    test(js, null);
  }

  private void testWarning(String js) {
    test(js, CheckGlobalThis.GLOBAL_THIS);
  }

  @Test
  public void testGlobalThisReported() {
    testWarning("this.foo = 5;");
    testWarning("this['foo'] = 5;");
    testWarning("var x = this.foo;");
    testWarning("(this.foo)();");
    testWarning("function f() { this.a = 5; }");
    testWarning("var f = function() { this.a = 5; };");
    testWarning("x = function() { this.a = 5; };");
  }

  @Test
  public void testGlobalThisAllowedInConstructor() {
    testSame("/** @constructor */ function F() { this.a = 5; }");
    testSame("/** @constructor */ var F = function() { this.a = 5; };");
    testSame("var F = /** @constructor */ function() { this.a = 5; };");
    testSame("/** @constructor */ F = function() { this.a = 5; };");
    testSame("F = /** @constructor */ function() { this.a = 5; };");
  }

  @Test
  public void testGlobalThisAllowedInInterface() {
    testSame("/** @interface */ function F() { this.a = 5; }");
    testSame("/** @interface */ var F = function() { this.a = 5; };");
  }

  @Test
  public void testGlobalThisAllowedWithThisType() {
    testSame("/** @this {Object} */ function f() { this.a = 5; }");
    testSame("/** @this {Object} */ var f = function() { this.a = 5; };");
    testSame("var f = /** @this {Object} */ function() { this.a = 5; };");
    testSame("/** @this {Object} */ f = function() { this.a = 5; };");
  }

  @Test
  public void testGlobalThisAllowedWithOverride() {
    testSame("/** @override */ function f() { this.a = 5; }");
    testSame("/** @override */ var f = function() { this.a = 5; };");
    testSame("var f = /** @override */ function() { this.a = 5; };");
    testSame("/** @override */ f = function() { this.a = 5; };");
  }

  @Test
  public void testGlobalThisInPrototypeMethods() {
    testSame("Foo.prototype.method = function() { this.a = 5; };");
    testSame("Foo.prototype['method'] = function() { this.a = 5; };");
    testSame("Foo.prototype.bar.baz = function() { this.a = 5; };");
    testSame("Foo.prototype = { method: function() { this.a = 5; } };");
  }

  @Test
  public void testGlobalThisInsideUnsupportedFunctionLocations() {
    testSame("foo(function() { this.a = 5; });");
    testSame("var arr = [function() { this.a = 5; }];");
    testSame("if (true) { (function() { this.a = 5; })(); }");
  }

  @Test
  public void testNestedAssignments() {
    testWarning("(a = this).property = 5;");
    testWarning("a = this.property = 5;");
  }

  @Test
  public void testThisWithoutPropertyOrAssignLhs() {
    testSame("var a = this;");
    testSame("function f() { var a = this; }");
    testSame("return this;");
  }

  @Test
  public void testShouldTraverseDirectly() {
    Compiler compiler = new Compiler();
    CheckGlobalThis check = createCheck(compiler, CheckLevel.WARNING);
    NodeTraversal t = new NodeTraversal(compiler, check);

    Node script = new Node(Token.SCRIPT);
    Node block = new Node(Token.BLOCK);
    Node func = new Node(Token.FUNCTION, new Node(Token.NAME), new Node(Token.PARAM_LIST), block);
    script.addChildToBack(func);
    assertTrue(check.shouldTraverse(t, func, script));

    Node expr = new Node(Token.EXPR_RESULT);
    expr.addChildToBack(func);
    assertFalse(check.shouldTraverse(t, func, expr));

    JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
    builder.recordConstructor();
    JSDocInfo jsDoc = builder.build(func);
    func.setJSDocInfo(jsDoc);
    assertFalse(check.shouldTraverse(t, func, script));
  }

  @Test
  public void testAssignLhsAndRhsBranches() {
    Compiler compiler = new Compiler();
    CheckGlobalThis check = createCheck(compiler, CheckLevel.WARNING);
    NodeTraversal t = new NodeTraversal(compiler, check);

    Node assign = new Node(Token.ASSIGN);
    Node lhs = Node.newString(Token.NAME, "x");
    Node rhs = new Node(Token.THIS);
    assign.addChildToBack(lhs);
    assign.addChildToBack(rhs);

    assertTrue(check.shouldTraverse(t, lhs, assign));
    check.visit(t, lhs, assign);

    assertTrue(check.shouldTraverse(t, rhs, assign));
    check.visit(t, rhs, assign);
    assertEquals(0, compiler.getWarningCount());

    Node getPropProto = new Node(Token.GETPROP, Node.newString(Token.NAME, "Foo"), Node.newString("prototype"));
    Node assignProto = new Node(Token.ASSIGN, getPropProto, rhs);
    assertFalse(check.shouldTraverse(t, rhs, assignProto));

    Node getPropSub = new Node(Token.GETPROP, getPropProto.cloneTree(), Node.newString("bar"));
    Node assignSub = new Node(Token.ASSIGN, getPropSub, rhs);
    assertFalse(check.shouldTraverse(t, rhs, assignSub));

    Node getPropNormal = new Node(Token.GETPROP, Node.newString(Token.NAME, "Foo"), Node.newString("bar"));
    Node assignNormal = new Node(Token.ASSIGN, getPropNormal, rhs);
    assertTrue(check.shouldTraverse(t, rhs, assignNormal));
  }

  @Test
  public void testVisitLhsChildClearing() {
    Compiler compiler = new Compiler();
    CheckGlobalThis check = createCheck(compiler, CheckLevel.WARNING);
    NodeTraversal t = new NodeTraversal(compiler, check);

    Node assign = new Node(Token.ASSIGN);
    Node thisNode = new Node(Token.THIS);
    Node getProp = new Node(Token.GETPROP, thisNode, Node.newString("foo"));
    Node rhs = Node.newNumber(42);
    assign.addChildToBack(getProp);
    assign.addChildToBack(rhs);

    assertTrue(check.shouldTraverse(t, getProp, assign));
    check.visit(t, thisNode, getProp);
    assertEquals(1, compiler.getWarningCount());
    check.visit(t, getProp, assign);
  }

  @Test
  public void testJsDocOnVarParent() {
    Compiler compiler = new Compiler();
    CheckGlobalThis check = createCheck(compiler, CheckLevel.WARNING);
    NodeTraversal t = new NodeTraversal(compiler, check);

    Node varNode = new Node(Token.VAR);
    JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
    builder.recordConstructor();
    varNode.setJSDocInfo(builder.build(varNode));

    Node nameNode = Node.newString(Token.NAME, "F");
    varNode.addChildToBack(nameNode);

    Node func = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.PARAM_LIST), new Node(Token.BLOCK));
    nameNode.addChildToBack(func);

    assertFalse(check.shouldTraverse(t, func, nameNode));
  }
}
