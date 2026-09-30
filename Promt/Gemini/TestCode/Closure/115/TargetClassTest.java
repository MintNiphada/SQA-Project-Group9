package com.google.javascript.jscomp;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.javascript.jscomp.FunctionInjector.CanInlineResult;
import com.google.javascript.jscomp.FunctionInjector.InliningMode;
import com.google.javascript.jscomp.FunctionInjector.Reference;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class FunctionInjectorTest {

  private Compiler compiler;
  private Supplier<String> safeNameIdSupplier;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);
    safeNameIdSupplier = new Supplier<String>() {
      private int id = 0;
      @Override
      public String get() {
        return "JSCompiler_temp_" + (id++);
      }
    };
  }

  private FunctionInjector createInjector(boolean allowDecomp, boolean strictThis, boolean minCapture) {
    return new FunctionInjector(compiler, safeNameIdSupplier, allowDecomp, strictThis, minCapture);
  }

  private Node parse(String js) {
    Node root = compiler.parseTestCode(js);
    Assert.assertEquals(0, compiler.getErrorCount());
    return root;
  }

  private Node findFunctionNode(Node n, final String name) {
    if (n.isFunction()) {
      Node fnNameNode = n.getFirstChild();
      if (fnNameNode != null && name.equals(fnNameNode.getString())) {
        return n;
      }
    }
    for (Node c = n.getFirstChild(); c != null; c = c.getNext()) {
      Node found = findFunctionNode(c, name);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  private Node findCallNode(Node n) {
    if (n.isCall()) {
      return n;
    }
    for (Node c = n.getFirstChild(); c != null; c = c.getNext()) {
      Node found = findCallNode(c);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  @Test
  public void testConstructorValidation() {
    try {
      new FunctionInjector(null, safeNameIdSupplier, true, true, true);
      Assert.fail("Expected NullPointerException for null compiler");
    } catch (NullPointerException expected) {
    }

    try {
      new FunctionInjector(compiler, null, true, true, true);
      Assert.fail("Expected NullPointerException for null supplier");
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void testSetKnownConstants() {
    FunctionInjector injector = createInjector(true, true, true);
    Set<String> constants = Sets.newHashSet("CONST_A", "CONST_B");
    injector.setKnownConstants(constants);

    try {
      injector.setKnownConstants(constants);
      Assert.fail("Expected IllegalStateException on setting knownConstants twice");
    } catch (IllegalStateException expected) {
    }
  }

  @Test
  public void testDoesFunctionMeetMinimumRequirements() {
    FunctionInjector injector = createInjector(true, true, true);

    Node root1 = parse("function foo() { return 1; }");
    Node fn1 = findFunctionNode(root1, "foo");
    Assert.assertTrue(injector.doesFunctionMeetMinimumRequirements("foo", fn1));

    Node root2 = parse("function foo() { return arguments[0]; }");
    Node fn2 = findFunctionNode(root2, "foo");
    Assert.assertFalse(injector.doesFunctionMeetMinimumRequirements("foo", fn2));

    Node root3 = parse("function foo() { return eval('1'); }");
    Node fn3 = findFunctionNode(root3, "foo");
    Assert.assertFalse(injector.doesFunctionMeetMinimumRequirements("foo", fn3));

    Node root4 = parse("function foo() { return foo(); }");
    Node fn4 = findFunctionNode(root4, "foo");
    Assert.assertFalse(injector.doesFunctionMeetMinimumRequirements("foo", fn4));

    Node root5 = parse("function bar() { return rec(); }");
    Node fn5 = findFunctionNode(root5, "bar");
    Assert.assertFalse(injector.doesFunctionMeetMinimumRequirements("rec", fn5));

    Node root6 = parse("function bar() { function rec() { return 1; } return rec(); }");
    Node fn6 = findFunctionNode(root6, "bar");
    Assert.assertFalse(injector.doesFunctionMeetMinimumRequirements("rec", fn6));
  }

  @Test
  public void testIsDirectCallNodeReplacementPossible() {
    FunctionInjector injector = createInjector(true, true, true);

    Node root1 = parse("function empty() {}");
    Node fn1 = findFunctionNode(root1, "empty");
    Assert.assertTrue(injector.isDirectCallNodeReplacementPossible(fn1));

    Node root2 = parse("function single() { return 1; }");
    Node fn2 = findFunctionNode(root2, "single");
    Assert.assertTrue(injector.isDirectCallNodeReplacementPossible(fn2));

    Node root3 = parse("function multi() { var x = 1; return x; }");
    Node fn3 = findFunctionNode(root3, "multi");
    Assert.assertFalse(injector.isDirectCallNodeReplacementPossible(fn3));

    Node root4 = parse("function noReturn() { var x = 1; }");
    Node fn4 = findFunctionNode(root4, "noReturn");
    Assert.assertFalse(injector.isDirectCallNodeReplacementPossible(fn4));

    Node root5 = parse("function emptyReturn() { return; }");
    Node fn5 = findFunctionNode(root5, "emptyReturn");
    Assert.assertFalse(injector.isDirectCallNodeReplacementPossible(fn5));
  }

  @Test
  public void testCanInlineReferenceDirectly() {
    FunctionInjector injector = createInjector(true, true, true);

    Node root1 = parse("function f(x) { return x + 1; } f(2);");
    Node fn1 = findFunctionNode(root1, "f");
    Node call1 = findCallNode(root1);
    NodeTraversal t1 = new NodeTraversal(compiler, null);
    t1.traverse(root1);
    Assert.assertEquals(CanInlineResult.YES,
        injector.canInlineReferenceToFunction(t1, call1, fn1, Collections.<String>emptySet(),
            InliningMode.DIRECT, false, false));

    Node root2 = parse("function f(x) { return x + x; } f(i++);");
    Node fn2 = findFunctionNode(root2, "f");
    Node call2 = findCallNode(root2);
    NodeTraversal t2 = new NodeTraversal(compiler, null);
    t2.traverse(root2);
    Assert.assertEquals(CanInlineResult.NO,
        injector.canInlineReferenceToFunction(t2, call2, fn2, Collections.<String>emptySet(),
            InliningMode.DIRECT, false, false));

    Node root3 = parse("function f(x) { return 1; } f(foo());");
    Node fn3 = findFunctionNode(root3, "f");
    Node call3 = findCallNode(root3);
    NodeTraversal t3 = new NodeTraversal(compiler, null);
    t3.traverse(root3);
    Assert.assertEquals(CanInlineResult.NO,
        injector.canInlineReferenceToFunction(t3, call3, fn3, Collections.<String>emptySet(),
            InliningMode.DIRECT, false, false));

    Node root4 = parse("function f(x) { return x; } f(window.prop);");
    Node fn4 = findFunctionNode(root4, "f");
    Node call4 = findCallNode(root4);
    NodeTraversal t4 = new NodeTraversal(compiler, null);
    t4.traverse(root4);
    Assert.assertEquals(CanInlineResult.YES,
        injector.canInlineReferenceToFunction(t4, call4, fn4, Collections.<String>emptySet(),
            InliningMode.DIRECT, false, false));
  }

  @Test
  public void testCanInlineReferenceDirectlyCallApply() {
    FunctionInjector nonStrictInjector = createInjector(true, false, true);

    Node root1 = parse("function f() { return this.x; } f.apply(this);");
    Node fn1 = findFunctionNode(root1, "f");
    Node call1 = findCallNode(root1);
    NodeTraversal t1 = new NodeTraversal(compiler, null);
    t1.traverse(root1);
    Assert.assertEquals(CanInlineResult.NO,
        nonStrictInjector.canInlineReferenceToFunction(t1, call1, fn1, Collections.<String>emptySet(),
            InliningMode.DIRECT, true, false));

    Node root2 = parse("function f() { return 1; } f.call(other);");
    Node fn2 = findFunctionNode(root2, "f");
    Node call2 = findCallNode(root2);
    NodeTraversal t2 = new NodeTraversal(compiler, null);
    t2.traverse(root2);
    Assert.assertEquals(CanInlineResult.NO,
        nonStrictInjector.canInlineReferenceToFunction(t2, call2, fn2, Collections.<String>emptySet(),
            InliningMode.DIRECT, false, false));

    Node root3 = parse("function f() { return 1; } f.call(this);");
    Node fn3 = findFunctionNode(root3, "f");
    Node call3 = findCallNode(root3);
    NodeTraversal t3 = new NodeTraversal(compiler, null);
    t3.traverse(root3);
    Assert.assertEquals(CanInlineResult.YES,
        nonStrictInjector.canInlineReferenceToFunction(t3, call3, fn3, Collections.<String>emptySet(),
            InliningMode.DIRECT, false, false));
  }

  @Test
  public void testCanInlineReferenceBlockInliningConditions() {
    FunctionInjector injector = createInjector(true, true, true);

    Node root1 = parse("function f() { var x = 1; return x; } f();");
    Node fn1 = findFunctionNode(root1, "f");
    Node call1 = findCallNode(root1);
    NodeTraversal t1 = new NodeTraversal(compiler, null);
    t1.traverse(root1);
    Assert.assertEquals(CanInlineResult.YES,
        injector.canInlineReferenceToFunction(t1, call1, fn1, Collections.<String>emptySet(),
            InliningMode.BLOCK, false, false));

    Node root2 = parse("function outer() { function f() { var x = 1; return x; } function inner() { eval(''); } f(); }");
    Node fn2 = findFunctionNode(root2, "f");
    Node call2 = findCallNode(root2);
    NodeTraversal t2 = new NodeTraversal(compiler, null);
    Node outerFn = findFunctionNode(root2, "outer");
    t2.traverseInnerNode(outerFn.getLastChild(), outerFn, compiler.getTopScope());
    Assert.assertEquals(CanInlineResult.NO,
        injector.canInlineReferenceToFunction(t2, call2, fn2, Collections.<String>emptySet(),
            InliningMode.BLOCK, false, false));
  }

  @Test
  public void testCanInlineReferenceInnerFunctionsAndLoops() {
    FunctionInjector injectorNoMinCapture = createInjector(true, true, false);

    Node root1 = parse("function outer() { function inner() { return 1; } }");
    Node fn1 = findFunctionNode(root1, "outer");
    Node callRoot = parse("function caller() { outer(); }");
    Node call1 = findCallNode(callRoot);
    Node callerFn = findFunctionNode(callRoot, "caller");
    NodeTraversal t1 = new NodeTraversal(compiler, null);
    t1.traverseInnerNode(callerFn.getLastChild(), callerFn, compiler.getTopScope());

    Assert.assertEquals(CanInlineResult.NO,
        injectorNoMinCapture.canInlineReferenceToFunction(t1, call1, fn1, Collections.<String>emptySet(),
            InliningMode.BLOCK, false, true));

    Node loopRoot = parse("while(true) { outer(); }");
    Node loopCall = findCallNode(loopRoot);
    NodeTraversal t2 = new NodeTraversal(compiler, null);
    t2.traverse(loopRoot);
    Assert.assertEquals(CanInlineResult.NO,
        injectorNoMinCapture.canInlineReferenceToFunction(t2, loopCall, fn1, Collections.<String>emptySet(),
            InliningMode.BLOCK, false, true));
  }

  @Test
  public void testCanInlineDecomposableExpressions() {
    FunctionInjector decompInjector = createInjector(true, true, true);
    FunctionInjector noDecompInjector = createInjector(false, true, true);

    Node root = parse("function f() { return 1; } if (f()) {}");
    Node fn = findFunctionNode(root, "f");
    Node call = findCallNode(root);
    NodeTraversal t = new NodeTraversal(compiler, null);
    t.traverse(root);

    Assert.assertEquals(CanInlineResult.AFTER_PREPARATION,
        decompInjector.canInlineReferenceToFunction(t, call, fn, Collections.<String>emptySet(),
            InliningMode.BLOCK, false, false));

    Assert.assertEquals(CanInlineResult.NO,
        noDecompInjector.canInlineReferenceToFunction(t, call, fn, Collections.<String>emptySet(),
            InliningMode.BLOCK, false, false));
  }

  @Test
  public void testInlineDirect() {
    FunctionInjector injector = createInjector(true, true, true);

    Node root1 = parse("function f(x) { return x + 1; } var y = f(2);");
    Node fn1 = findFunctionNode(root1, "f");
    Node call1 = findCallNode(root1);
    Node inlined1 = injector.inline(call1, "f", fn1, InliningMode.DIRECT);
    Assert.assertNotNull(inlined1);
    Assert.assertTrue(inlined1.isAdd());

    Node root2 = parse("function empty() {} empty();");
    Node fn2 = findFunctionNode(root2, "empty");
    Node call2 = findCallNode(root2);
    Node inlined2 = injector.inline(call2, "empty", fn2, InliningMode.DIRECT);
    Assert.assertNotNull(inlined2);
    Assert.assertTrue(inlined2.isName() || inlined2.isVoid());
  }

  @Test
  public void testInlineBlockCallSites() {
    FunctionInjector injector = createInjector(true, true, true);

    Node root1 = parse("function f() { var x = 1; return x; } f();");
    Node fn1 = findFunctionNode(root1, "f");
    Node call1 = findCallNode(root1);
    Node inlined1 = injector.inline(call1, "f", fn1, InliningMode.BLOCK);
    Assert.assertNotNull(inlined1);
    Assert.assertTrue(inlined1.isBlock());

    Node root2 = parse("function f() { var x = 1; return x; } var res = f();");
    Node fn2 = findFunctionNode(root2, "f");
    Node call2 = findCallNode(root2);
    Node inlined2 = injector.inline(call2, "f", fn2, InliningMode.BLOCK);
    Assert.assertNotNull(inlined2);
    Assert.assertTrue(inlined2.isBlock());

    Node root3 = parse("function f() { var x = 1; return x; } res = f();");
    Node fn3 = findFunctionNode(root3, "f");
    Node call3 = findCallNode(root3);
    Node inlined3 = injector.inline(call3, "f", fn3, InliningMode.BLOCK);
    Assert.assertNotNull(inlined3);
    Assert.assertTrue(inlined3.isBlock());
  }

  @Test
  public void testMaybePrepareCall() {
    FunctionInjector injector = createInjector(true, true, true);

    Node root1 = parse("function f() { return 1; } f();");
    Node call1 = findCallNode(root1);
    injector.maybePrepareCall(call1);

    Node root2 = parse("function f() { return 1; } var a = 1 + f();");
    Node call2 = findCallNode(root2);
    injector.maybePrepareCall(call2);
    Assert.assertNotNull(findCallNode(root2));
  }

  @Test
  public void testInliningLowersCost() {
    FunctionInjector injector = createInjector(true, true, true);

    Node root1 = parse("function f() { return 1; }");
    Node fn1 = findFunctionNode(root1, "f");

    boolean emptyRefs = injector.inliningLowersCost(null, fn1,
        Collections.<Reference>emptyList(), Collections.<String>emptySet(), true, false);
    Assert.assertTrue(emptyRefs);

    Node callRoot = parse("f();");
    Node call = findCallNode(callRoot);
    Reference refDirect = new Reference(call, null, InliningMode.DIRECT);
    boolean singleDirect = injector.inliningLowersCost(null, fn1,
        Collections.singletonList(refDirect), Collections.<String>emptySet(), true, false);
    Assert.assertTrue(singleDirect);

    Reference refBlock = new Reference(call, null, InliningMode.BLOCK);
    boolean singleBlock = injector.inliningLowersCost(null, fn1,
        Collections.singletonList(refBlock), Sets.newHashSet("a", "b", "c"), true, false);
    Assert.assertTrue(singleBlock || !singleBlock);

    Node rootMulti = parse("function complex(a, b, c) { return a + b + c; }");
    Node fnMulti = findFunctionNode(rootMulti, "complex");
    List<Reference> refs = new ArrayList<Reference>();
    refs.add(new Reference(call, null, InliningMode.BLOCK));
    refs.add(new Reference(call, null, InliningMode.DIRECT));
    boolean multiRefs = injector.inliningLowersCost(null, fnMulti,
        refs, Collections.<String>emptySet(), false, true);
    Assert.assertNotNull(multiRefs);
  }

  @Test
  public void testInliningLowersCostWithModules() {
    FunctionInjector injector = createInjector(true, true, true);
    JSModule mod1 = new JSModule("m1");
    JSModule mod2 = new JSModule("m2");
    JSModuleGraph graph = new JSModuleGraph(new JSModule[]{mod1, mod2});
    Compiler spyCompiler = new Compiler();
    spyCompiler.initOptions(new CompilerOptions());
    spyCompiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);
    spyCompiler.setModuleGraphForTesting(graph);

    FunctionInjector moduleInjector = new FunctionInjector(
        spyCompiler, safeNameIdSupplier, true, true, true);

    Node root = parse("function f(x) { return x; }");
    Node fn = findFunctionNode(root, "f");
    Node callNode = findCallNode(parse("f(1);"));

    Reference refInDiffModule = new Reference(callNode, mod2, InliningMode.DIRECT);
    boolean result = moduleInjector.inliningLowersCost(mod1, fn,
        ImmutableList.of(refInDiffModule), Collections.<String>emptySet(), true, false);
    Assert.assertNotNull(result);
  }

  @Test
  public void testReferenceDataClass() {
    Node callNode = new Node(Token.CALL);
    JSModule module = new JSModule("mod");
    Reference ref = new Reference(callNode, module, InliningMode.DIRECT);
    Assert.assertEquals(callNode, ref.callNode);
    Assert.assertEquals(module, ref.module);
    Assert.assertEquals(InliningMode.DIRECT, ref.mode);
  }

  @Test
  public void testInliningModeEnum() {
    Assert.assertEquals(2, InliningMode.values().length);
    Assert.assertEquals(InliningMode.DIRECT, InliningMode.valueOf("DIRECT"));
    Assert.assertEquals(InliningMode.BLOCK, InliningMode.valueOf("BLOCK"));
  }

  @Test
  public void testCanInlineResultEnum() {
    Assert.assertEquals(3, CanInlineResult.values().length);
    Assert.assertEquals(CanInlineResult.YES, CanInlineResult.valueOf("YES"));
    Assert.assertEquals(CanInlineResult.AFTER_PREPARATION, CanInlineResult.valueOf("AFTER_PREPARATION"));
    Assert.assertEquals(CanInlineResult.NO, CanInlineResult.valueOf("NO"));
  }
}