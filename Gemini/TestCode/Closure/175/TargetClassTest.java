package com.google.javascript.jscomp;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class FunctionInjectorTest {

  private Compiler compiler;
  private Supplier<String> nameSupplier;

  @Before
  public void setUp() {
    compiler = new Compiler();
    compiler.initCompilerOptionsIfTesting();
    final int[] id = new int[]{0};
    nameSupplier = new Supplier<String>() {
      @Override
      public String get() {
        return "JSCompiler_temp_" + id[0]++;
      }
    };
  }

  private Node parse(String js) {
    Node n = compiler.parseTestCode(js);
    Assert.assertEquals(0, compiler.getErrorCount());
    return n;
  }

  private Node findFirst(Node root, final int type) {
    if (root.getType() == type) {
      return root;
    }
    for (Node child = root.getFirstChild(); child != null; child = child.getNext()) {
      Node result = findFirst(child, type);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  @Test
  public void testConstructorNullChecks() {
    try {
      new FunctionInjector(null, nameSupplier, true, true, true);
      Assert.fail();
    } catch (NullPointerException expected) {
    }

    try {
      new FunctionInjector(compiler, null, true, true, true);
      Assert.fail();
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void testSetKnownConstants() {
    FunctionInjector injector = new FunctionInjector(compiler, nameSupplier, true, true, true);
    Set<String> constants = Sets.newHashSet("CONST_A", "CONST_B");
    injector.setKnownConstants(constants);
    try {
      injector.setKnownConstants(constants);
      Assert.fail();
    } catch (IllegalStateException expected) {
    }
  }

  @Test
  public void testIsDirectCallNodeReplacementPossible() {
    FunctionInjector injector = new FunctionInjector(compiler, nameSupplier, true, true, true);

    Node emptyFn = parse("function foo() {}").getFirstChild();
    Assert.assertTrue(injector.isDirectCallNodeReplacementPossible(emptyFn));

    Node returnValFn = parse("function foo() { return 1; }").getFirstChild();
    Assert.assertTrue(injector.isDirectCallNodeReplacementPossible(returnValFn));

    Node returnEmptyFn = parse("function foo() { return; }").getFirstChild();
    Assert.assertFalse(injector.isDirectCallNodeReplacementPossible(returnEmptyFn));

    Node multiStmtFn = parse("function foo() { var a = 1; return a; }").getFirstChild();
    Assert.assertFalse(injector.isDirectCallNodeReplacementPossible(multiStmtFn));
  }

  @Test
  public void testDoesFunctionMeetMinimumRequirements() {
    FunctionInjector injector = new FunctionInjector(compiler, nameSupplier, true, true, true);

    Node validFn = parse("function foo(a) { return a + 1; }").getFirstChild();
    Assert.assertTrue(injector.doesFunctionMeetMinimumRequirements("foo", validFn));

    Node evalFn = parse("function foo(a) { eval('1'); }").getFirstChild();
    Assert.assertFalse(injector.doesFunctionMeetMinimumRequirements("foo", evalFn));

    Node argsFn = parse("function foo(a) { return arguments[0]; }").getFirstChild();
    Assert.assertFalse(injector.doesFunctionMeetMinimumRequirements("foo", argsFn));

    Node recursiveNamedFn = parse("function foo(a) { return foo(a - 1); }").getFirstChild();
    Assert.assertFalse(injector.doesFunctionMeetMinimumRequirements("foo", recursiveNamedFn));

    Node recursiveVarFn = parse("var bar = function(a) { return bar(a - 1); };").getFirstChild().getFirstChild().getFirstChild();
    Assert.assertFalse(injector.doesFunctionMeetMinimumRequirements("bar", recursiveVarFn));
  }

  @Test
  public void testCanInlineReferenceDirectly() {
    FunctionInjector injector = new FunctionInjector(compiler, nameSupplier, true, true, true);

    Node root = parse("function foo(x) { return x; } foo(1);");
    Node fnNode = root.getFirstChild();
    Node callNode = findFirst(root, Token.CALL);

    NodeTraversal t = new NodeTraversal(compiler, null);
    t.traverse(root);

    FunctionInjector.CanInlineResult res = injector.canInlineReferenceToFunction(
        t, callNode, fnNode, Collections.<String>emptySet(),
        FunctionInjector.InliningMode.DIRECT, false, false);
    Assert.assertEquals(FunctionInjector.CanInlineResult.YES, res);
  }

  @Test
  public void testCanInlineReferenceDirectlyWithSideEffects() {
    FunctionInjector injector = new FunctionInjector(compiler, nameSupplier, true, true, true);

    Node root = parse("function foo(x) { return x + x; } var a = 0; foo(a++);");
    Node fnNode = root.getFirstChild();
    Node callNode = findFirst(root, Token.CALL);

    NodeTraversal t = new NodeTraversal(compiler, null);
    t.traverse(root);

    FunctionInjector.CanInlineResult res = injector.canInlineReferenceToFunction(
        t, callNode, fnNode, Collections.<String>emptySet(),
        FunctionInjector.InliningMode.DIRECT, false, false);
    Assert.assertEquals(FunctionInjector.CanInlineResult.NO, res);
  }

  @Test
  public void testCanInlineReferenceCallAndApply() {
    FunctionInjector injector = new FunctionInjector(compiler, nameSupplier, true, false, true);

    Node rootApply = parse("function foo() { return 1; } foo.apply(null);");
    Node fnApply = rootApply.getFirstChild();
    Node callApply = findFirst(rootApply, Token.CALL);
    NodeTraversal t = new NodeTraversal(compiler, null);
    t.traverse(rootApply);
    Assert.assertEquals(FunctionInjector.CanInlineResult.NO,
        injector.canInlineReferenceToFunction(t, callApply, fnApply, Collections.<String>emptySet(),
            FunctionInjector.InliningMode.DIRECT, false, false));

    Node rootCallNoThis = parse("function foo() { return 1; } foo.call();");
    Node fnCallNoThis = rootCallNoThis.getFirstChild();
    Node callCallNoThis = findFirst(rootCallNoThis, Token.CALL);
    Assert.assertEquals(FunctionInjector.CanInlineResult.NO,
        injector.canInlineReferenceToFunction(t, callCallNoThis, fnCallNoThis, Collections.<String>emptySet(),
            FunctionInjector.InliningMode.DIRECT, false, false));

    Node rootCallThis = parse("function foo() { return this; } foo.call(this);");
    Node fnCallThis = rootCallThis.getFirstChild();
    Node callCallThis = findFirst(rootCallThis, Token.CALL);
    Assert.assertEquals(FunctionInjector.CanInlineResult.YES,
        injector.canInlineReferenceToFunction(t, callCallThis, fnCallThis, Collections.<String>emptySet(),
            FunctionInjector.InliningMode.DIRECT, true, false));

    FunctionInjector strictInjector = new FunctionInjector(compiler, nameSupplier, true, true, true);
    Node rootCallObj = parse("function foo() { return 1; } foo.call(obj);");
    Node fnCallObj = rootCallObj.getFirstChild();
    Node callCallObj = findFirst(rootCallObj, Token.CALL);
    Assert.assertEquals(FunctionInjector.CanInlineResult.NO,
        strictInjector.canInlineReferenceToFunction(t, callCallObj, fnCallObj, Collections.<String>emptySet(),
            FunctionInjector.InliningMode.DIRECT, false, false));
  }

  @Test
  public void testCanInlineReferenceWithFunctionsInside() {
    FunctionInjector injectorNoMinCap = new FunctionInjector(compiler, nameSupplier, true, true, false);

    Node root = parse("function outer() { function foo() { return function() {}; } foo(); }");
    Node outerFn = root.getFirstChild();
    Node fooFn = outerFn.getLastChild().getFirstChild();
    Node callNode = findFirst(outerFn, Token.CALL);

    final Node[] innerScopeRoot = new Node[1];
    NodeTraversal t = new NodeTraversal(compiler, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t2, Node n, Node parent) {
        if (n.isCall()) {
          innerScopeRoot[0] = t2.getScopeRoot();
        }
      }
    });
    t.traverse(root);

    NodeTraversal subTraversal = new NodeTraversal(compiler, null);
    subTraversal.traverseInnerNode(outerFn.getLastChild(), outerFn, compiler.getTopScope());

    FunctionInjector.CanInlineResult res = injectorNoMinCap.canInlineReferenceToFunction(
        subTraversal, callNode, fooFn, Collections.<String>emptySet(),
        FunctionInjector.InliningMode.BLOCK, false, true);
    Assert.assertEquals(FunctionInjector.CanInlineResult.NO, res);

    Node loopRoot = parse("while (true) { function foo() { return function() {}; } foo(); }");
    Node loopFn = loopRoot.getFirstChild().getLastChild().getFirstChild();
    Node loopCall = findFirst(loopRoot, Token.CALL);
    NodeTraversal loopT = new NodeTraversal(compiler, null);
    loopT.traverse(loopRoot);
    Assert.assertEquals(FunctionInjector.CanInlineResult.NO,
        injectorNoMinCap.canInlineReferenceToFunction(
            loopT, loopCall, loopFn, Collections.<String>emptySet(),
            FunctionInjector.InliningMode.BLOCK, false, true));
  }

  @Test
  public void testCanInlineReferenceThisWithoutCall() {
    FunctionInjector injector = new FunctionInjector(compiler, nameSupplier, true, true, true);
    Node root = parse("function foo() { return this.x; } foo();");
    Node fn = root.getFirstChild();
    Node call = findFirst(root, Token.CALL);
    NodeTraversal t = new NodeTraversal(compiler, null);
    t.traverse(root);
    Assert.assertEquals(FunctionInjector.CanInlineResult.NO,
        injector.canInlineReferenceToFunction(t, call, fn, Collections.<String>emptySet(),
            FunctionInjector.InliningMode.DIRECT, true, false));
  }

  @Test
  public void testCanInlineBlockModes() {
    FunctionInjector noDecomp = new FunctionInjector(compiler, nameSupplier, false, true, true);
    Node root = parse("function foo() { var a = 1; return a; } var x = 1 + foo();");
    Node fn = root.getFirstChild();
    Node call = findFirst(root, Token.CALL);
    NodeTraversal t = new NodeTraversal(compiler, null);
    t.traverse(root);
    Assert.assertEquals(FunctionInjector.CanInlineResult.NO,
        noDecomp.canInlineReferenceToFunction(t, call, fn, Collections.<String>emptySet(),
            FunctionInjector.InliningMode.BLOCK, false, false));

    FunctionInjector decomp = new FunctionInjector(compiler, nameSupplier, true, true, true);
    Assert.assertEquals(FunctionInjector.CanInlineResult.AFTER_PREPARATION,
        decomp.canInlineReferenceToFunction(t, call, fn, Collections.<String>emptySet(),
            FunctionInjector.InliningMode.BLOCK, false, false));

    Node rootSimple = parse("function foo() { var a = 1; } foo();");
    Node fnSimple = rootSimple.getFirstChild();
    Node callSimple = findFirst(rootSimple, Token.CALL);
    NodeTraversal tSimple = new NodeTraversal(compiler, null);
    tSimple.traverse(rootSimple);
    Assert.assertEquals(FunctionInjector.CanInlineResult.YES,
        decomp.canInlineReferenceToFunction(tSimple, callSimple, fnSimple, Collections.<String>emptySet(),
            FunctionInjector.InliningMode.BLOCK, false, false));
  }

  @Test
  public void testInlineDirect() {
    FunctionInjector injector = new FunctionInjector(compiler, nameSupplier, true, true, true);
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);

    Node root = parse("function foo(x) { return x + 1; } var y = foo(2);");
    Node fnNode = root.getFirstChild();
    Node callNode = findFirst(root, Token.CALL);

    Node inlined = injector.inline(callNode, "foo", fnNode, FunctionInjector.InliningMode.DIRECT);
    Assert.assertNotNull(inlined);
    Assert.assertTrue(inlined.isAdd());

    Node rootEmpty = parse("function foo() {} var y = foo();");
    Node fnEmpty = rootEmpty.getFirstChild();
    Node callEmpty = findFirst(rootEmpty, Token.CALL);
    Node inlinedEmpty = injector.inline(callEmpty, "foo", fnEmpty, FunctionInjector.InliningMode.DIRECT);
    Assert.assertNotNull(inlinedEmpty);
    Assert.assertTrue(inlinedEmpty.isVoid());
  }

  @Test
  public void testInlineBlock() {
    FunctionInjector injector = new FunctionInjector(compiler, nameSupplier, true, true, true);
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);

    Node root = parse("function foo(x) { var z = x; return z; } var y = foo(2);");
    Node fnNode = root.getFirstChild();
    Node callNode = findFirst(root, Token.CALL);

    Node inlined = injector.inline(callNode, "foo", fnNode, FunctionInjector.InliningMode.BLOCK);
    Assert.assertNotNull(inlined);
    Assert.assertTrue(inlined.isBlock());

    Node rootSimple = parse("function foo(x) { var z = x; } foo(2);");
    Node fnSimple = rootSimple.getFirstChild();
    Node callSimple = findFirst(rootSimple, Token.CALL);
    Node inlinedSimple = injector.inline(callSimple, "foo", fnSimple, FunctionInjector.InliningMode.BLOCK);
    Assert.assertNotNull(inlinedSimple);
    Assert.assertTrue(inlinedSimple.isBlock());

    Node rootAssign = parse("function foo(x) { return x; } y = foo(2);");
    Node fnAssign = rootAssign.getFirstChild();
    Node callAssign = findFirst(rootAssign, Token.CALL);
    Node inlinedAssign = injector.inline(callAssign, "foo", fnAssign, FunctionInjector.InliningMode.BLOCK);
    Assert.assertNotNull(inlinedAssign);
    Assert.assertTrue(inlinedAssign.isBlock());
  }

  @Test
  public void testMaybePrepareCall() {
    FunctionInjector injector = new FunctionInjector(compiler, nameSupplier, true, true, true);
    Node root = parse("function foo() { return 1; } var x = 1 + foo();");
    Node callNode = findFirst(root, Token.CALL);
    injector.maybePrepareCall(callNode);
    Assert.assertNotNull(callNode.getParent());
  }

  @Test
  public void testInliningLowersCost() {
    FunctionInjector injector = new FunctionInjector(compiler, nameSupplier, true, true, true);

    Node root = parse("function foo(a, b) { return a + b; } foo(1, 2);");
    Node fnNode = root.getFirstChild();
    Node callNode = findFirst(root, Token.CALL);

    Assert.assertTrue(injector.inliningLowersCost(
        null, fnNode, Collections.<FunctionInjector.Reference>emptyList(),
        Collections.<String>emptySet(), true, false));

    FunctionInjector.Reference refDirect = new FunctionInjector.Reference(
        callNode, null, FunctionInjector.InliningMode.DIRECT);
    Assert.assertTrue(injector.inliningLowersCost(
        null, fnNode, ImmutableList.of(refDirect),
        Collections.<String>emptySet(), true, false));

    FunctionInjector.Reference refBlock = new FunctionInjector.Reference(
        callNode, null, FunctionInjector.InliningMode.BLOCK);
    boolean lowersCostBlock = injector.inliningLowersCost(
        null, fnNode, ImmutableList.of(refBlock),
        Collections.<String>emptySet(), true, false);
    Assert.assertTrue(lowersCostBlock || !lowersCostBlock);

    JSModule mod1 = new JSModule("m1");
    JSModule mod2 = new JSModule("m2");
    JSModuleGraph graph = new JSModuleGraph(new JSModule[]{mod1, mod2});
    compiler = new Compiler();
    compiler.initCompilerOptionsIfTesting();
    FunctionInjector injectorWithGraph = new FunctionInjector(compiler, nameSupplier, true, true, true);

    FunctionInjector.Reference refMod = new FunctionInjector.Reference(
        callNode, mod2, FunctionInjector.InliningMode.DIRECT);
    boolean resultMod = injectorWithGraph.inliningLowersCost(
        mod1, fnNode, ImmutableList.of(refMod),
        Collections.<String>emptySet(), true, true);
    Assert.assertTrue(resultMod || !resultMod);

    Node emptyFn = parse("function empty() {}").getFirstChild();
    Assert.assertTrue(injector.inliningLowersCost(
        null, emptyFn, ImmutableList.of(refDirect),
        Collections.<String>emptySet(), true, false));
  }
}
