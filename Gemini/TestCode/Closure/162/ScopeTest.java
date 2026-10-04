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
import com.google.javascript.rhino.jstype.StaticSourceFile;
import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;

public class ScopeTest {

  @Test
  public void testGlobalScopeAndBasicProperties() {
    Node root = new Node(Token.BLOCK);
    ObjectType thisType = null;
    Scope scope = new Scope(root, thisType);

    Assert.assertEquals(0, scope.getDepth());
    Assert.assertTrue(scope.isBottom());
    Assert.assertTrue(scope.isGlobal());
    Assert.assertFalse(scope.isLocal());
    Assert.assertSame(root, scope.getRootNode());
    Assert.assertNull(scope.getParent());
    Assert.assertNull(scope.getParentScope());
    Assert.assertSame(scope, scope.getGlobalScope());
    Assert.assertNull(scope.getTypeOfThis());
    Assert.assertEquals(0, scope.getVarCount());
  }

  @Test
  public void testCompilerGlobalScope() {
    Compiler compiler = new Compiler();
    Node root = new Node(Token.BLOCK);
    Scope scope = new Scope(root, compiler);

    Assert.assertEquals(0, scope.getDepth());
    Assert.assertFalse(scope.isBottom());
    Assert.assertTrue(scope.isGlobal());
    Assert.assertNotNull(scope.getTypeOfThis());
    Assert.assertEquals(compiler.getTypeRegistry().getNativeObjectType(JSTypeNative.GLOBAL_THIS), scope.getTypeOfThis());
  }

  @Test
  public void testNestedScope() {
    Compiler compiler = new Compiler();
    Node globalRoot = new Node(Token.BLOCK);
    Scope globalScope = new Scope(globalRoot, compiler);

    Node fnNode = new Node(Token.FUNCTION);
    Scope childScope = new Scope(globalScope, fnNode);

    Assert.assertEquals(1, childScope.getDepth());
    Assert.assertFalse(childScope.isBottom());
    Assert.assertFalse(childScope.isGlobal());
    Assert.assertTrue(childScope.isLocal());
    Assert.assertSame(globalScope, childScope.getParent());
    Assert.assertSame(globalScope, childScope.getParentScope());
    Assert.assertSame(globalScope, childScope.getGlobalScope());
    Assert.assertSame(globalScope.getTypeOfThis(), childScope.getTypeOfThis());

    Node nestedFnNode = new Node(Token.FUNCTION);
    Scope grandchildScope = new Scope(childScope, nestedFnNode);
    Assert.assertEquals(2, grandchildScope.getDepth());
    Assert.assertSame(globalScope, grandchildScope.getGlobalScope());
  }

  @Test
  public void testNestedScopeWithFunctionType() {
    Compiler compiler = new Compiler();
    Node globalRoot = new Node(Token.BLOCK);
    Scope globalScope = new Scope(globalRoot, compiler);

    JSTypeRegistry registry = compiler.getTypeRegistry();
    ObjectType customThis = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    FunctionType fnType = registry.createFunctionType(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), false);
    fnType = registry.createFunctionTypeWithThisType(fnType, customThis);

    Node fnNode = new Node(Token.FUNCTION);
    fnNode.setJSType(fnType);

    Scope childScope = new Scope(globalScope, fnNode);
    Assert.assertSame(customThis, childScope.getTypeOfThis());
  }

  @Test(expected = NullPointerException.class)
  public void testNestedScopeNullParentThrows() {
    Node node = new Node(Token.FUNCTION);
    new Scope(null, node);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNestedScopeSameRootThrows() {
    Node node = new Node(Token.BLOCK);
    Scope globalScope = new Scope(node, (ObjectType) null);
    new Scope(globalScope, node);
  }

  @Test
  public void testDeclareAndUndeclareVar() {
    Node root = new Node(Token.BLOCK);
    Scope scope = new Scope(root, (ObjectType) null);

    Node nameNode = Node.newString(Token.NAME, "x");
    CompilerInput input = new CompilerInput(SourceFile.fromCode("test.js", "var x;"));

    Scope.Var varX = scope.declare("x", nameNode, null, input);

    Assert.assertEquals(1, scope.getVarCount());
    Assert.assertSame(varX, scope.getVar("x"));
    Assert.assertSame(varX, scope.getSlot("x"));
    Assert.assertSame(varX, scope.getOwnSlot("x"));
    Assert.assertTrue(scope.isDeclared("x", false));
    Assert.assertTrue(scope.isDeclared("x", true));
    Assert.assertFalse(scope.isDeclared("y", false));
    Assert.assertNull(scope.getVar("y"));
    Assert.assertNull(scope.getSlot("y"));
    Assert.assertNull(scope.getOwnSlot("y"));

    Assert.assertEquals("x", varX.getName());
    Assert.assertSame(nameNode, varX.getNode());
    Assert.assertSame(nameNode, varX.getNameNode());
    Assert.assertSame(input, varX.getInput());
    Assert.assertEquals("test.js", varX.getInputName());
    Assert.assertSame(varX, varX.getSymbol());
    Assert.assertSame(varX, varX.getDeclaration());
    Assert.assertSame(scope, varX.getScope());
    Assert.assertTrue(varX.isGlobal());
    Assert.assertFalse(varX.isLocal());
    Assert.assertFalse(varX.isDefine());
    Assert.assertFalse(varX.isNoShadow());
    Assert.assertTrue(varX.isTypeInferred());

    scope.undeclare(varX);
    Assert.assertEquals(0, scope.getVarCount());
    Assert.assertNull(scope.getVar("x"));
  }

  @Test(expected = IllegalStateException.class)
  public void testDeclareDuplicateThrows() {
    Node root = new Node(Token.BLOCK);
    Scope scope = new Scope(root, (ObjectType) null);
    Node nameNode = Node.newString(Token.NAME, "x");
    scope.declare("x", nameNode, null, null);
    scope.declare("x", nameNode, null, null);
  }

  @Test(expected = IllegalStateException.class)
  public void testDeclareEmptyNameThrows() {
    Node root = new Node(Token.BLOCK);
    Scope scope = new Scope(root, (ObjectType) null);
    scope.declare("", new Node(Token.NAME), null, null);
  }

  @Test(expected = IllegalStateException.class)
  public void testUndeclareWrongScopeThrows() {
    Node root1 = new Node(Token.BLOCK);
    Node root2 = new Node(Token.BLOCK);
    Scope scope1 = new Scope(root1, (ObjectType) null);
    Scope scope2 = new Scope(root2, (ObjectType) null);

    Node nameNode = Node.newString(Token.NAME, "x");
    Scope.Var varX = scope1.declare("x", nameNode, null, null);
    scope2.undeclare(varX);
  }

  @Test
  public void testVarGetInitialValue() {
    Node varNode = new Node(Token.VAR);
    Node nameNode1 = Node.newString(Token.NAME, "a");
    Node valueNode1 = Node.newString("init");
    nameNode1.addChildToBack(valueNode1);
    varNode.addChildToBack(nameNode1);

    Node assignNode = new Node(Token.ASSIGN);
    Node nameNode2 = Node.newString(Token.NAME, "b");
    Node valueNode2 = new Node(Token.NUMBER);
    assignNode.addChildToBack(nameNode2);
    assignNode.addChildToBack(valueNode2);

    Node fnNode = new Node(Token.FUNCTION);
    Node nameNode3 = Node.newString(Token.NAME, "c");
    fnNode.addChildToBack(nameNode3);

    Node otherParent = new Node(Token.PARAM_LIST);
    Node nameNode4 = Node.newString(Token.NAME, "d");
    otherParent.addChildToBack(nameNode4);

    Scope scope = new Scope(new Node(Token.BLOCK), (ObjectType) null);
    Scope.Var v1 = scope.declare("a", nameNode1, null, null);
    Scope.Var v2 = scope.declare("b", nameNode2, null, null);
    Scope.Var v3 = scope.declare("c", nameNode3, null, null);
    Scope.Var v4 = scope.declare("d", nameNode4, null, null);

    Assert.assertSame(valueNode1, v1.getInitialValue());
    Assert.assertSame(valueNode2, v2.getInitialValue());
    Assert.assertSame(fnNode, v3.getInitialValue());
    Assert.assertNull(v4.getInitialValue());
  }

  @Test
  public void testVarTypeHandling() {
    Compiler compiler = new Compiler();
    JSTypeRegistry registry = compiler.getTypeRegistry();
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);

    Node root = new Node(Token.BLOCK);
    Scope scope = new Scope(root, (ObjectType) null);
    Node nameNode = Node.newString(Token.NAME, "x");

    Scope.Var inferredVar = scope.declare("x", nameNode, numberType, null, true);
    Assert.assertSame(numberType, inferredVar.getType());
    Assert.assertTrue(inferredVar.isTypeInferred());
    inferredVar.setType(stringType);
    Assert.assertSame(stringType, inferredVar.getType());

    ErrorReporter reporter = compiler.getErrorManager();
    inferredVar.resolveType(reporter);
    Assert.assertNotNull(inferredVar.getType());

    Node nameNode2 = Node.newString(Token.NAME, "y");
    Scope.Var declaredVar = scope.declare("y", nameNode2, numberType, null, false);
    Assert.assertFalse(declaredVar.isTypeInferred());
    try {
      declaredVar.setType(stringType);
      Assert.fail("Expected IllegalStateException on setting type on declared variable");
    } catch (IllegalStateException expected) {
    }
  }

  @Test
  public void testVarInfoAndFlags() {
    Node root = new Node(Token.BLOCK);
    Scope scope = new Scope(root, (ObjectType) null);

    Node nameNode = Node.newString(Token.NAME, "CONST_VAL");
    JSDocInfo info = new JSDocInfo();
    nameNode.setJSDocInfo(info);

    Scope.Var varConst = scope.declare("CONST_VAL", nameNode, null, null);
    Assert.assertTrue(varConst.isConst());
    Assert.assertSame(info, varConst.getJSDocInfo());
    Assert.assertFalse(varConst.isNoShadow());
    Assert.assertEquals("<non-file>", varConst.getInputName());
    Assert.assertTrue(varConst.isExtern());

    Node fnParent = new Node(Token.FUNCTION);
    fnParent.addChildToBack(new Node(Token.NAME));
    Node nameBleed = Node.newString(Token.NAME, "bleed");
    fnParent.addChildToBack(nameBleed);
    Scope.Var varBleed = scope.declare("bleed", nameBleed, null, null);
    Assert.assertTrue(varBleed.isBleedingFunction());
  }

  @Test
  public void testVarEqualsHashCodeToString() {
    Node root = new Node(Token.BLOCK);
    Scope scope = new Scope(root, (ObjectType) null);
    Node n1 = Node.newString(Token.NAME, "a");
    Node n2 = Node.newString(Token.NAME, "a");

    Scope.Var v1 = scope.declare("a", n1, null, null);
    Scope.Var v1Clone = new Scope(new Node(Token.BLOCK), (ObjectType) null).declare("a", n1, null, null);
    Scope.Var v2 = new Scope(new Node(Token.BLOCK), (ObjectType) null).declare("a2", n2, null, null);

    Assert.assertEquals(v1, v1Clone);
    Assert.assertNotEquals(v1, v2);
    Assert.assertFalse(v1.equals("string"));
    Assert.assertFalse(v1.equals(null));
    Assert.assertEquals(n1.hashCode(), v1.hashCode());
    Assert.assertTrue(v1.toString().contains("Scope.Var a{"));
  }

  @Test
  public void testArgumentsVar() {
    Node root = new Node(Token.BLOCK);
    Scope scope = new Scope(root, (ObjectType) null);

    Scope.Var args1 = scope.getArgumentsVar();
    Scope.Var args2 = scope.getArgumentsVar();
    Assert.assertSame(args1, args2);
    Assert.assertEquals("arguments", args1.getName());
    Assert.assertNull(args1.getNode());
    Assert.assertNull(args1.getDeclaration());
    Assert.assertNull(args1.getParentNode());
    Assert.assertFalse(args1.isTypeInferred());
    Assert.assertEquals("<non-file>", args1.getInputName());

    Scope otherScopeSameRoot = new Scope(root, (ObjectType) null);
    Scope.Var otherArgs = otherScopeSameRoot.getArgumentsVar();
    Assert.assertEquals(args1, otherArgs);

    Scope diffScope = new Scope(new Node(Token.BLOCK), (ObjectType) null);
    Assert.assertNotEquals(args1, diffScope.getArgumentsVar());
    Assert.assertFalse(args1.equals("other"));
    Assert.assertFalse(args1.equals(null));
    Assert.assertEquals(System.identityHashCode(args1), args1.hashCode());
  }

  @Test
  public void testScopeInheritanceAndLookups() {
    Node parentRoot = new Node(Token.BLOCK);
    Scope parentScope = new Scope(parentRoot, (ObjectType) null);
    Node childRoot = new Node(Token.FUNCTION);
    Scope childScope = new Scope(parentScope, childRoot);

    Node n1 = Node.newString(Token.NAME, "x");
    Scope.Var v1 = parentScope.declare("x", n1, null, null);
    Node n2 = Node.newString(Token.NAME, "y");
    Scope.Var v2 = childScope.declare("y", n2, null, null);

    Assert.assertSame(v1, childScope.getVar("x"));
    Assert.assertNull(childScope.getOwnSlot("x"));
    Assert.assertSame(v1, childScope.getSlot("x"));
    Assert.assertTrue(childScope.isDeclared("x", true));
    Assert.assertFalse(childScope.isDeclared("x", false));

    Assert.assertSame(v2, childScope.getVar("y"));
    Assert.assertSame(v2, childScope.getOwnSlot("y"));
    Assert.assertNull(parentScope.getVar("y"));
    Assert.assertFalse(parentScope.isDeclared("y", true));

    Assert.assertNull(childScope.getVar("z"));

    Assert.assertEquals(1, childScope.getReferences(v2).iterator().next() != null ? 1 : 0);
    Assert.assertSame(childScope, childScope.getScope(v2));
    Assert.assertEquals(1, childScope.getAllSymbols().size());
  }

  @Test
  public void testDeclarativelyUnboundVarsWithoutTypes() {
    Node root = new Node(Token.BLOCK);
    Scope scope = new Scope(root, (ObjectType) null);

    Node varNode = new Node(Token.VAR);
    Node nameUnbound = Node.newString(Token.NAME, "unbound");
    varNode.addChildToBack(nameUnbound);
    CompilerInput inputNonExtern = new CompilerInput(SourceFile.fromCode("test.js", "var unbound;"));
    scope.declare("unbound", nameUnbound, null, inputNonExtern);

    Node nameBound = Node.newString(Token.NAME, "bound");
    varNode.addChildToBack(nameBound);
    Compiler compiler = new Compiler();
    JSType numberType = compiler.getTypeRegistry().getNativeType(JSTypeNative.NUMBER_TYPE);
    scope.declare("bound", nameBound, numberType, inputNonExtern);

    Node nameExtern = Node.newString(Token.NAME, "externVar");
    varNode.addChildToBack(nameExtern);
    CompilerInput inputExtern = new CompilerInput(SourceFile.fromCode("extern.js", "var externVar;"), true);
    scope.declare("externVar", nameExtern, null, inputExtern);

    Node letNode = new Node(Token.LET);
    Node nameLet = Node.newString(Token.NAME, "letVar");
    letNode.addChildToBack(nameLet);
    scope.declare("letVar", nameLet, null, inputNonExtern);

    Iterator<Scope.Var> unboundIter = scope.getDeclarativelyUnboundVarsWithoutTypes();
    Assert.assertTrue(unboundIter.hasNext());
    Scope.Var found = unboundIter.next();
    Assert.assertEquals("unbound", found.getName());
    Assert.assertFalse(unboundIter.hasNext());
  }

  @Test
  public void testSourceFileAccess() {
    Node root = new Node(Token.BLOCK);
    Scope scope = new Scope(root, (ObjectType) null);

    Node nameNode = Node.newString(Token.NAME, "x");
    SourceFile sf = SourceFile.fromCode("input.js", "var x;");
    nameNode.setStaticSourceFile(sf);

    Scope.Var varX = scope.declare("x", nameNode, null, null);
    StaticSourceFile retrievedFile = varX.getSourceFile();
    Assert.assertEquals("input.js", retrievedFile.getName());
  }
}
