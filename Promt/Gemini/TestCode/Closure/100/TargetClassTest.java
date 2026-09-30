package com.google.javascript.jscomp;

import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfoBuilder;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CheckGlobalThisTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  private void testTraverse(String js, int expectedWarnings, CheckLevel level) {
    Node root = compiler.parseTestCode(js);
    CheckGlobalThis callback = new CheckGlobalThis(compiler, level);
    NodeTraversal.traverse(compiler, root, callback);
    if (level == CheckLevel.ERROR) {
      Assert.assertEquals(expectedWarnings, compiler.getErrorCount());
    } else {
      Assert.assertEquals(expectedWarnings, compiler.getWarningCount());
    }
  }

  private void testTraverse(String js, int expectedWarnings) {
    testTraverse(js, expectedWarnings, CheckLevel.WARNING);
  }

  @Test
  public void testGlobalThisLhsAssignment() {
    testTraverse("this.a = 1;", 1);
  }

  @Test
  public void testGlobalThisLhsMultipleAssignments() {
    testTraverse("this.a = 1; this.b = 2;", 2);
  }

  @Test
  public void testGlobalThisRhsAssignmentNotReported() {
    testTraverse("var a = this;", 0);
  }

  @Test
  public void testConstructorFunctionNotTraversed() {
    testTraverse("/** @constructor */ function F() { this.a = 1; }", 0);
  }

  @Test
  public void testThisAnnotationFunctionNotTraversed() {
    testTraverse("/** @this {Object} */ function f() { this.a = 1; }", 0);
  }

  @Test
  public void testNormalFunctionTraversedAndReported() {
    testTraverse("function f() { this.a = 1; }", 1);
  }

  @Test
  public void testPrototypeAssignmentRhsFunctionNotTraversed() {
    testTraverse("Foo.prototype.bar = function() { this.a = 1; };", 0);
  }

  @Test
  public void testPrototypePropertySubpropertyRhsFunctionNotTraversed() {
    testTraverse("Foo.prototype.bar.baz = function() { this.a = 1; };", 0);
  }

  @Test
  public void testPrototypeDirectAssignmentNotTraversed() {
    testTraverse("Foo.prototype = function() { this.a = 1; };", 0);
  }

  @Test
  public void testObjectPropertyAssignmentFunctionTraversed() {
    testTraverse("Foo.bar = function() { this.a = 1; };", 1);
  }

  @Test
  public void testVarNameWithJsDocConstructor() {
    testTraverse("var /** @constructor */ F = function() { this.a = 1; };", 0);
  }

  @Test
  public void testVarWithJsDocConstructor() {
    testTraverse("/** @constructor */ var F = function() { this.a = 1; };", 0);
  }

  @Test
  public void testAssignWithJsDocConstructor() {
    testTraverse("/** @constructor */ F = function() { this.a = 1; };", 0);
  }

  @Test
  public void testNestedAssignInLhs() {
    testTraverse("(this.foo).bar = 1;", 1);
  }

  @Test
  public void testCheckLevelError() {
    testTraverse("this.a = 1;", 1, CheckLevel.ERROR);
  }

  @Test
  public void testCheckLevelOff() {
    testTraverse("this.a = 1;", 0, CheckLevel.OFF);
  }

  @Test
  public void testDiagnosticType() {
    Assert.assertEquals("JSC_USED_GLOBAL_THIS", CheckGlobalThis.GLOBAL_THIS.key);
  }

  @Test
  public void testManualAstShouldTraverseNonFunctionNonAssign() {
    CheckGlobalThis callback = new CheckGlobalThis(compiler, CheckLevel.WARNING);
    NodeTraversal t = new NodeTraversal(compiler, callback);
    Node numberNode = new Node(Token.NUMBER);
    Assert.assertTrue(callback.shouldTraverse(t, numberNode, null));
  }

  @Test
  public void testManualAstFunctionWithJsDocConstructorReturnsFalse() {
    CheckGlobalThis callback = new CheckGlobalThis(compiler, CheckLevel.WARNING);
    NodeTraversal t = new NodeTraversal(compiler, callback);

    JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
    builder.recordConstructor();
    JSDocInfo info = builder.build(null);

    Node fnNode = new Node(Token.FUNCTION);
    fnNode.setJSDocInfo(info);

    Assert.assertFalse(callback.shouldTraverse(t, fnNode, null));
  }

  @Test
  public void testManualAstFunctionWithJsDocThisTypeReturnsFalse() {
    CheckGlobalThis callback = new CheckGlobalThis(compiler, CheckLevel.WARNING);
    NodeTraversal t = new NodeTraversal(compiler, callback);

    JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
    builder.recordThisType(new Node(Token.STRING));
    JSDocInfo info = builder.build(null);

    Node fnNode = new Node(Token.FUNCTION);
    fnNode.setJSDocInfo(info);

    Assert.assertFalse(callback.shouldTraverse(t, fnNode, null));
  }

  @Test
  public void testManualAstFunctionWithOtherJsDocReturnsTrue() {
    CheckGlobalThis callback = new CheckGlobalThis(compiler, CheckLevel.WARNING);
    NodeTraversal t = new NodeTraversal(compiler, callback);

    JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
    builder.recordDescription("test function");
    JSDocInfo info = builder.build(null);

    Node fnNode = new Node(Token.FUNCTION);
    fnNode.setJSDocInfo(info);

    Assert.assertTrue(callback.shouldTraverse(t, fnNode, null));
  }

  @Test
  public void testManualAstFunctionJsDocFromAssignParent() {
    CheckGlobalThis callback = new CheckGlobalThis(compiler, CheckLevel.WARNING);
    NodeTraversal t = new NodeTraversal(compiler, callback);

    JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
    builder.recordConstructor();
    JSDocInfo info = builder.build(null);

    Node assign = new Node(Token.ASSIGN);
    assign.setJSDocInfo(info);
    Node lhs = Node.newString(Token.NAME, "x");
    Node fn = new Node(Token.FUNCTION);
    assign.addChildToBack(lhs);
    assign.addChildToBack(fn);

    Assert.assertFalse(callback.shouldTraverse(t, fn, assign));
  }

  @Test
  public void testManualAstFunctionJsDocFromVarGrandparent() {
    CheckGlobalThis callback = new CheckGlobalThis(compiler, CheckLevel.WARNING);
    NodeTraversal t = new NodeTraversal(compiler, callback);

    JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
    builder.recordConstructor();
    JSDocInfo info = builder.build(null);

    Node varNode = new Node(Token.VAR);
    varNode.setJSDocInfo(info);
    Node nameNode = Node.newString(Token.NAME, "F");
    Node fn = new Node(Token.FUNCTION);
    nameNode.addChildToBack(fn);
    varNode.addChildToBack(nameNode);

    Assert.assertFalse(callback.shouldTraverse(t, fn, nameNode));
  }

  @Test
  public void testManualAstAssignPrototypeLhsReturnsFalseOnRhs() {
    CheckGlobalThis callback = new CheckGlobalThis(compiler, CheckLevel.WARNING);
    NodeTraversal t = new NodeTraversal(compiler, callback);

    Node assign = new Node(Token.ASSIGN);
    Node getProp = new Node(Token.GETPROP, Node.newString(Token.NAME, "Foo"), Node.newString(Token.STRING, "prototype"));
    Node rhs = new Node(Token.OBJECTLIT);
    assign.addChildToBack(getProp);
    assign.addChildToBack(rhs);

    Assert.assertFalse(callback.shouldTraverse(t, rhs, assign));
  }

  @Test
  public void testManualAstAssignPrototypeSubpropertyLhsReturnsFalseOnRhs() {
    CheckGlobalThis callback = new CheckGlobalThis(compiler, CheckLevel.WARNING);
    NodeTraversal t = new NodeTraversal(compiler, callback);

    Node assign = new Node(Token.ASSIGN);
    Node getPropProto = new Node(Token.GETPROP, Node.newString(Token.NAME, "Foo"), Node.newString(Token.STRING, "prototype"));
    Node getPropBar = new Node(Token.GETPROP, getPropProto, Node.newString(Token.STRING, "bar"));
    Node rhs = new Node(Token.FUNCTION);
    assign.addChildToBack(getPropBar);
    assign.addChildToBack(rhs);

    Assert.assertFalse(callback.shouldTraverse(t, rhs, assign));
  }

  @Test
  public void testManualAstAssignNormalGetPropLhsReturnsTrueOnRhs() {
    CheckGlobalThis callback = new CheckGlobalThis(compiler, CheckLevel.WARNING);
    NodeTraversal t = new NodeTraversal(compiler, callback);

    Node assign = new Node(Token.ASSIGN);
    Node getProp = new Node(Token.GETPROP, Node.newString(Token.NAME, "Foo"), Node.newString(Token.STRING, "bar"));
    Node rhs = new Node(Token.FUNCTION);
    assign.addChildToBack(getProp);
    assign.addChildToBack(rhs);

    Assert.assertTrue(callback.shouldTraverse(t, rhs, assign));
  }

  @Test
  public void testManualAstAssignLhsChildResetOnVisit() {
    CheckGlobalThis callback = new CheckGlobalThis(compiler, CheckLevel.WARNING);
    NodeTraversal t = new NodeTraversal(compiler, callback);

    Node assign = new Node(Token.ASSIGN);
    Node lhs = Node.newString(Token.NAME, "x");
    Node rhs = new Node(Token.NUMBER);
    assign.addChildToBack(lhs);
    assign.addChildToBack(rhs);

    // shouldTraverse on LHS sets assignLhsChild
    Assert.assertTrue(callback.shouldTraverse(t, lhs, assign));

    // visit on LHS resets assignLhsChild
    callback.visit(t, lhs, assign);

    // After reset, visiting THIS outside assignLhsChild should not report
    Node thisNode = new Node(Token.THIS);
    callback.visit(t, thisNode, null);
    Assert.assertEquals(0, compiler.getWarningCount());
  }

  @Test
  public void testManualAstThisOnLhsReportsError() {
    CheckGlobalThis callback = new CheckGlobalThis(compiler, CheckLevel.WARNING);
    NodeTraversal t = new NodeTraversal(compiler, callback);

    Node assign = new Node(Token.ASSIGN);
    Node thisNode = new Node(Token.THIS);
    Node rhs = new Node(Token.NUMBER);
    assign.addChildToBack(thisNode);
    assign.addChildToBack(rhs);

    Assert.assertTrue(callback.shouldTraverse(t, thisNode, assign));
    callback.visit(t, thisNode, assign);
    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testManualAstGetFunctionJsDocInfoParentNotNameOrAssign() {
    CheckGlobalThis callback = new CheckGlobalThis(compiler, CheckLevel.WARNING);
    NodeTraversal t = new NodeTraversal(compiler, callback);

    Node block = new Node(Token.BLOCK);
    Node fn = new Node(Token.FUNCTION);
    block.addChildToBack(fn);

    Assert.assertTrue(callback.shouldTraverse(t, fn, block));
  }

  @Test
  public void testManualAstGetFunctionJsDocInfoParentNameGrandparentNotVar() {
    CheckGlobalThis callback = new CheckGlobalThis(compiler, CheckLevel.WARNING);
    NodeTraversal t = new NodeTraversal(compiler, callback);

    Node expr = new Node(Token.EXPR_RESULT);
    Node name = Node.newString(Token.NAME, "foo");
    Node fn = new Node(Token.FUNCTION);
    name.addChildToBack(fn);
    expr.addChildToBack(name);

    Assert.assertTrue(callback.shouldTraverse(t, fn, name));
  }
}