package com.google.javascript.jscomp;

import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;

public class ScopeTest {

  @Test
  public void testBottomScopeCreationAndProperties() {
    Node root = new Node(Token.BLOCK);
    Compiler compiler = new Compiler();
    JSTypeRegistry registry = compiler.getTypeRegistry();
    ObjectType thisType = registry.getNativeObjectType(JSTypeNative.GLOBAL_THIS);

    Scope bottomScope = new Scope(root, thisType);
    Assert.assertTrue(bottomScope.isBottom());
    Assert.assertEquals(0, bottomScope.getDepth());
    Assert.assertTrue(bottomScope.isGlobal());
    Assert.assertFalse(bottomScope.isLocal());
    Assert.assertNull(bottomScope.getParent());
    Assert.assertNull(bottomScope.getParentScope());
    Assert.assertSame(root, bottomScope.getRootNode());
    Assert.assertSame(thisType, bottomScope.getTypeOfThis());
    Assert.assertSame(bottomScope, bottomScope.getGlobalScope());
  }

  @Test
  public void testGlobalScopeWithCompiler() {
    Node root = new Node(Token.BLOCK);
    Compiler compiler = new Compiler();
    Scope globalScope = new Scope(root, compiler);

    Assert.assertFalse(globalScope.isBottom());
    Assert.assertEquals(0, globalScope.getDepth());
    Assert.assertTrue(globalScope.isGlobal());
    Assert.assertFalse(globalScope.isLocal());
    Assert.assertNull(globalScope.getParent());
    Assert.assertNotNull(globalScope.getTypeOfThis());
  }

  @Test
  public void testChildScopeHierarchyAndThisInheritance() {
    Node root = new Node(Token.BLOCK);
    Compiler compiler = new Compiler();
    Scope globalScope = new Scope(root, compiler);

    Node childRoot1 = new Node(Token.FUNCTION);
    Scope childScope1 = new Scope(globalScope, childRoot1);

    Assert.assertEquals(1, childScope1.getDepth());
    Assert.assertFalse(childScope1.isGlobal());
    Assert.assertTrue(childScope1.isLocal());
    Assert.assertSame(globalScope, childScope1.getParent());
    Assert.assertSame(globalScope, childScope1.getParentScope());
    Assert.assertSame(globalScope, childScope1.getGlobalScope());
    Assert.assertSame(globalScope.getTypeOfThis(), childScope1.getTypeOfThis());

    Node childRoot2 = new Node(Token.FUNCTION);
    Scope childScope2 = new Scope(childScope1, childRoot2);
    Assert.assertEquals(2, childScope2.getDepth());
    Assert.assertSame(globalScope, childScope2.getGlobalScope());
  }

  @Test
  public void testChildScopeWithFunctionType() {
    Node root = new Node(Token.BLOCK);
    Compiler compiler = new Compiler();
    JSTypeRegistry registry = compiler.getTypeRegistry();
    Scope globalScope = new Scope(root, compiler);

    ObjectType fnThisType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    FunctionType fnType = registry.createFunctionType(fnThisType);

    Node fnNode = new Node(Token.FUNCTION);
    fnNode.setJSType(fnType);

    Scope childScope = new Scope(globalScope, fnNode);
    Assert.assertSame(fnThisType, childScope.getTypeOfThis());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testChildScopeSameRootThrowsException() {
    Node root = new Node(Token.BLOCK);
    Compiler compiler = new Compiler();
    Scope globalScope = new Scope(root, compiler);
    new Scope(globalScope, root);
  }

  @Test(expected = NullPointerException.class)
  public void testChildScopeNullParentThrowsException() {
    Node root = new Node(Token.BLOCK);
    new Scope(null, root);
  }

  @Test
  public void testVarDeclarationAndLookup() {
    Node root = new Node(Token.BLOCK);
    Compiler compiler = new Compiler();
    Scope scope = new Scope(root, compiler);

    Assert.assertEquals(0, scope.getVarCount());
    Assert.assertFalse(scope.isDeclared("x", true));
    Assert.assertNull(scope.getVar("x"));
    Assert.assertNull(scope.getSlot("x"));
    Assert.assertNull(scope.getOwnSlot("x"));

    Node nameNode = Node.newString(Token.NAME, "x");
    CompilerInput input = new CompilerInput(SourceFile.fromCode("test.js", "var x;"));
    Scope.Var varX = scope.declare("x", nameNode, null, input);

    Assert.assertEquals(1, scope.getVarCount());
    Assert.assertTrue(scope.isDeclared("x", false));
    Assert.assertTrue(scope.isDeclared("x", true));
    Assert.assertSame(varX, scope.getVar("x"));
    Assert.assertSame(varX, scope.getSlot("x"));
    Assert.assertSame(varX, scope.getOwnSlot("x"));

    Iterator<Scope.Var> it = scope.getVars();
    Assert.assertTrue(it.hasNext());
    Assert.assertSame(varX, it.next());
    Assert.assertFalse(it.hasNext());
  }

  @Test
  public void testParentScopeLookup() {
    Node root = new Node(Token.BLOCK);
    Compiler compiler = new Compiler();
    Scope parent = new Scope(root, compiler);
    Node childRoot = new Node(Token.FUNCTION);
    Scope child = new Scope(parent, childRoot);

    Node nameNode = Node.newString(Token.NAME, "parentVar");
    Scope.Var v = parent.declare("parentVar", nameNode, null, null);

    Assert.assertNull(child.getOwnSlot("parentVar"));
    Assert.assertSame(v, child.getVar("parentVar"));
    Assert.assertSame(v, child.getSlot("parentVar"));
    Assert.assertFalse(child.isDeclared("parentVar", false));
    Assert.assertTrue(child.isDeclared("parentVar", true));
    Assert.assertNull(child.getVar("nonExistent"));
    Assert.assertFalse(child.isDeclared("nonExistent", true));
  }

  @Test(expected = IllegalStateException.class)
  public void testDuplicateDeclarationThrowsException() {
    Node root = new Node(Token.BLOCK);
    Compiler compiler = new Compiler();
    Scope scope = new Scope(root, compiler);
    Node nameNode1 = Node.newString(Token.NAME, "x");
    Node nameNode2 = Node.newString(Token.NAME, "x");
    scope.declare("x", nameNode1, null, null);
    scope.declare("x", nameNode2, null, null);
  }

  @Test(expected = IllegalStateException.class)
  public void testEmptyNameDeclarationThrowsException() {
    Node root = new Node(Token.BLOCK);
    Compiler compiler = new Compiler();
    Scope scope = new Scope(root, compiler);
    scope.declare("", Node.newString(Token.NAME, ""), null, null);
  }

  @Test(expected = IllegalStateException.class)
  public void testNullNameDeclarationThrowsException() {
    Node root = new Node(Token.BLOCK);
    Compiler compiler = new Compiler();
    Scope scope = new Scope(root, compiler);
    scope.declare(null, null, null, null);
  }

  @Test
  public void testUndeclareVar() {
    Node root = new Node(Token.BLOCK);
    Compiler compiler = new Compiler();
    Scope scope = new Scope(root, compiler);
    Node nameNode = Node.newString(Token.NAME, "y");
    Scope.Var varY = scope.declare("y", nameNode, null, null);

    Assert.assertEquals(1, scope.getVarCount());
    scope.undeclare(varY);
    Assert.assertEquals(0, scope.getVarCount());
    Assert.assertNull(scope.getVar("y"));
  }

  @Test(expected = IllegalStateException.class)
  public void testUndeclareVarFromDifferentScope() {
    Node root1 = new Node(Token.BLOCK);
    Node root2 = new Node(Token.BLOCK);
    Compiler compiler = new Compiler();
    Scope scope1 = new Scope(root1, compiler);
    Scope scope2 = new Scope(root2, compiler);

    Node nameNode = Node.newString(Token.NAME, "a");
    Scope.Var varA = scope1.declare("a", nameNode, null, null);
    scope2.undeclare(varA);
  }

  @Test
  public void testVarPropertiesAndMethods() {
    Node root = new Node(Token.BLOCK);
    Compiler compiler = new Compiler();
    JSTypeRegistry registry = compiler.getTypeRegistry();
    Scope scope = new Scope(root, compiler);

    Node nameNode = Node.newString(Token.NAME, "CONST_VAL");
    CompilerInput input = new CompilerInput(SourceFile.fromCode("source.js", "var CONST_VAL;"));
    JSType strType = registry.getNativeType(JSTypeNative.STRING_TYPE);

    Scope.Var var = scope.declare("CONST_VAL", nameNode, strType, input, true);

    Assert.assertEquals("CONST_VAL", var.getName());
    Assert.assertSame(nameNode, var.getNameNode());
    Assert.assertSame(scope, var.getScope());
    Assert.assertTrue(var.isGlobal());
    Assert.assertFalse(var.isLocal());
    Assert.assertEquals("source.js", var.getInputName());
    Assert.assertFalse(var.isExtern());
    Assert.assertTrue(var.isConst());
    Assert.assertFalse(var.isDefine());
    Assert.assertTrue(var.isTypeInferred());
    Assert.assertSame(strType, var.getType());
    Assert.assertNull(var.getJSDocInfo());
    Assert.assertFalse(var.isNoShadow());
    Assert.assertEquals("Scope.Var CONST_VAL", var.toString());

    JSType numType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    var.setType(numType);
    Assert.assertSame(numType, var.getType());

    ErrorReporter reporter = compiler.getErrorReporter();
    var.resolveType(reporter);
    Assert.assertNotNull(var.getType());
  }

  @Test
  public void testVarWithoutInputAndDeclaredType() {
    Node root = new Node(Token.BLOCK);
    Compiler compiler = new Compiler();
    Scope scope = new Scope(root, compiler);

    Node nameNode = Node.newString(Token.NAME, "normalVar");
    Scope.Var var = scope.declare("normalVar", nameNode, null, null, false);

    Assert.assertEquals("<non-file>", var.getInputName());
    Assert.assertTrue(var.isExtern());
    Assert.assertFalse(var.isTypeInferred());
    Assert.assertFalse(var.isConst());

    try {
      var.setType(compiler.getTypeRegistry().getNativeType(JSTypeNative.BOOLEAN_TYPE));
      Assert.fail("Expected IllegalStateException when setting type on non-inferred Var");
    } catch (IllegalStateException expected) {
    }

    var.resolveType(compiler.getErrorReporter());
    Assert.assertNull(var.getType());
  }

  @Test
  public void testVarWithJSDocInfo() {
    Node root = new Node(Token.BLOCK);
    Compiler compiler = new Compiler();
    Scope scope = new Scope(root, compiler);

    Node nameNode = Node.newString(Token.NAME, "defVar");
    JSDocInfo info = new JSDocInfo();
    info.setDefine(true);
    info.setNoShadow(true);
    nameNode.setJSDocInfo(info);

    Scope.Var var = scope.declare("defVar", nameNode, null, null);
    Assert.assertTrue(var.isDefine());
    Assert.assertTrue(var.isNoShadow());
    Assert.assertSame(info, var.getJSDocInfo());
  }

  @Test
  public void testVarInitialValueAndBleedingFunction() {
    Node root = new Node(Token.BLOCK);
    Compiler compiler = new Compiler();
    Scope scope = new Scope(root, compiler);

    Node fnName = Node.newString(Token.NAME, "fn");
    Node fn = new Node(Token.FUNCTION, fnName, new Node(Token.PARAM_LIST), new Node(Token.BLOCK));
    Scope.Var fnVar = scope.declare("fn", fnName, null, null);
    Assert.assertSame(fn, fnVar.getInitialValue());
    Assert.assertFalse(fnVar.isBleedingFunction());

    Node exprFn = NodeUtil.newFunctionNode("expr", java.util.Collections.<Node>emptyList(), new Node(Token.BLOCK), 0, 0);
    Node exprName = exprFn.getFirstChild();
    Node expr = new Node(Token.EXPR_RESULT, exprFn);
    Scope.Var exprFnVar = scope.declare("expr", exprName, null, null);
    Assert.assertTrue(exprFnVar.isBleedingFunction());

    Node varName = Node.newString(Token.NAME, "v");
    Node varInit = Node.newString("val");
    varName.addChildToFront(varInit);
    new Node(Token.VAR, varName);
    Scope.Var vVar = scope.declare("v", varName, null, null);
    Assert.assertSame(varInit, vVar.getInitialValue());

    Node assignName = Node.newString(Token.NAME, "a");
    Node assignVal = Node.newNumber(42);
    new Node(Token.ASSIGN, assignName, assignVal);
    Scope.Var assignVar = scope.declare("a", assignName, null, null);
    Assert.assertSame(assignVal, assignVar.getInitialValue());

    Node otherName = Node.newString(Token.NAME, "o");
    new Node(Token.EMPTY, otherName);
    Scope.Var otherVar = scope.declare("o", otherName, null, null);
    Assert.assertNull(otherVar.getInitialValue());

    Scope.Var nullNodeVar = scope.declare("nullNode", null, null, null);
    Assert.assertNull(nullNodeVar.getParentNode());
  }

  @Test
  public void testVarEqualsAndHashCode() {
    Node root = new Node(Token.BLOCK);
    Compiler compiler = new Compiler();
    Scope scope = new Scope(root, compiler);

    Node node1 = Node.newString(Token.NAME, "a");
    Node node2 = Node.newString(Token.NAME, "a");

    Scope.Var var1 = scope.declare("a", node1, null, null);

    Scope otherScope = new Scope(root, compiler);
    Scope.Var var2 = otherScope.declare("a", node1, null, null);
    Scope.Var var3 = otherScope.declare("b", node2, null, null);

    Assert.assertEquals(var1, var2);
    Assert.assertEquals(var1.hashCode(), var2.hashCode());
    Assert.assertNotEquals(var1, var3);
    Assert.assertNotEquals(var1, null);
    Assert.assertNotEquals(var1, "StringObject");
  }
}
