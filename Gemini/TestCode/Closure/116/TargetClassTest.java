package com.google.javascript.jscomp;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.javascript.jscomp.AbstractCompiler.LifeCycleStage;
import com.google.javascript.jscomp.FunctionInjector.CanInlineResult;
import com.google.javascript.jscomp.FunctionInjector.InliningMode;
import com.google.javascript.jscomp.FunctionInjector.Reference;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class FunctionInjectorTest {

  private Compiler compiler;
  private Supplier<String> safeNameIdSupplier;
  private int idCounter;

  @Before
  public void setUp() {
    compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    idCounter = 0;
    safeNameIdSupplier = new Supplier<String>() {
      @Override
      public String get() {
        return "JSCompiler_temp_" + (idCounter++);
      }
    };
  }

  private FunctionInjector createInjector(
      boolean allowDecomposition,
      boolean assumeStrictThis,
      boolean assumeMinimumCapture) {
    return new FunctionInjector(
        compiler,
        safeNameIdSupplier,
        allowDecomposition,
        assumeStrictThis,
        assumeMinimumCapture);
  }

  private Node parse(String js) {
    Node root = compiler.parseTestCode(js);
    Assert.assertEquals(0, compiler.getErrorCount());
    return root;
  }

  private Node findFunction(Node root, final String name) {
    final Node[] result = new Node[1];
    NodeTraversal.traverse(compiler, root, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n.isFunction()) {
          Node nameNode = n.getFirstChild();
          if (nameNode != null && name.equals(nameNode.getString())) {
            result[0] = n;
          } else if (parent != null && parent.isName() && name.equals(parent.getString())) {
            result[0] = n;
          } else if (parent != null && parent.isAssign() && parent.getFirstChild().isName()
              && name.equals(parent.getFirstChild().getString())) {
            result[0] = n;
          }
        }
      }
    });
    return result[0];
  }

  private Node findCall(Node root) {
    final Node[] result = new Node[1];
    NodeTraversal.traverse(compiler, root, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n.isCall() && result[0] == null) {
          result[0] = n;
        }
      }
    });
    return result[0];
  }

  @Test(expected = NullPointerException.class)
  public void testConstructorNullCompiler() {
    new FunctionInjector(null, safeNameIdSupplier, true, false, false);
  }

  @Test(expected = NullPointerException.class)
  public void testConstructorNullSupplier() {
    new FunctionInjector(compiler, null, true, false, false);
  }

  @Test
  public void testSetKnownConstants() {
    FunctionInjector injector = createInjector(true, false, false);
    Set<String> constants = Sets.newHashSet("CONST_A", "CONST_B");
    injector.setKnownConstants(constants);

    try {
      injector.setKnownConstants(constants);
      Assert.fail("Expected IllegalStateException on second setKnownConstants call");
    } catch (IllegalStateException expected) {
    }
  }

  @Test
  public void testDoesFunctionMeetMinimumRequirements() {
    FunctionInjector injector = createInjector(true, false, false);

    Node root1 = parse("function foo() { return 1; }");
    Node fn1 = findFunction(root1, "foo");
    Assert.assertTrue(injector.doesFunctionMeetMinimumRequirements("foo", fn1));

    Node root2 = parse("function foo() { return arguments[0]; }");
    Node fn2 = findFunction(root2, "foo");
    Assert.assertFalse(injector.doesFunctionMeetMinimumRequirements("foo", fn2));

    Node root3 = parse("function foo() { eval('1'); }");
    Node fn3 = findFunction(root3, "foo");
    Assert.assertFalse(injector.doesFunctionMeetMinimumRequirements("foo", fn3));

    Node root4 = parse("function foo() { return foo(); }");
    Node fn4 = findFunction(root4, "foo");
    Assert.assertFalse(injector.doesFunctionMeetMinimumRequirements("foo", fn4));

    Node root5 = parse("var bar = function foo() { return foo(); };");
    Node fn5 = findFunction(root5, "foo");
    Assert.assertFalse(injector.doesFunctionMeetMinimumRequirements("bar", fn5));

    Node root6 = parse("function foo() { return function() { return 1; }; }");
    Node fn6 = findFunction(root6, "foo");
    Assert.assertTrue(injector.doesFunctionMeetMinimumRequirements("foo", fn6));
  }

  @Test
  public void testIsDirectCallNodeReplacementPossible() {
    FunctionInjector injector = createInjector(true, false, false);

    Node root1 = parse("function foo() {}");
    Node fn1 = findFunction(root1, "foo");
    Assert.assertTrue(injector.isDirectCallNodeReplacementPossible(fn1));

    Node root2 = parse("function foo() { return 1; }");
    Node fn2 = findFunction(root2, "foo");
    Assert.assertTrue(injector.isDirectCallNodeReplacementPossible(fn2));

    Node root3 = parse("function foo() { return; }");
    Node fn3 = findFunction(root3, "foo");
    Assert.assertFalse(injector.isDirectCallNodeReplacementPossible(fn3));

    Node root4 = parse("function foo() { var x = 1; return x; }");
    Node fn4 = findFunction(root4, "foo");
    Assert.assertFalse(injector.isDirectCallNodeReplacementPossible(fn4));
  }

  @Test
  public void testCanInlineReferenceDirectlySimple() {
    FunctionInjector injector = createInjector(true, false, false);

    Node root = parse("function foo(a, b) { return a + b; } var x = foo(1, 2);");
    final Node fn = findFunction(root, "foo");
    final Node call = findCall(root);

    NodeTraversal.traverse(compiler, root, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n == call) {
          CanInlineResult res = injector.canInlineReferenceToFunction(
              t, call, fn, Collections.<String>emptySet(), InliningMode.DIRECT, false, false);
          Assert.assertEquals(CanInlineResult.YES, res);
        }
      }
    });
  }

  @Test
  public void testCanInlineReferenceDirectlySideEffects() {
    FunctionInjector injector = createInjector(true, false, false);

    Node root = parse("function foo(a) { return a + a; } var i = 0; var x = foo(i++);");
    final Node fn = findFunction(root, "foo");
    final Node call = findCall(root);

    NodeTraversal.traverse(compiler, root, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n == call) {
          CanInlineResult res = injector.canInlineReferenceToFunction(
              t, call, fn, Collections.<String>emptySet(), InliningMode.DIRECT, false, false);
          Assert.assertEquals(CanInlineResult.NO, res);
        }
      }
    });
  }

  @Test
  public void testCanInlineReferenceWithThis() {
    FunctionInjector injectorStrict = createInjector(true, true, false);
    FunctionInjector injectorNonStrict = createInjector(true, false, false);

    Node root = parse("function foo() { return this.x; } foo.call(this);");
    final Node fn = findFunction(root, "foo");
    final Node call = findCall(root);

    NodeTraversal.traverse(compiler, root, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n == call) {
          CanInlineResult resStrict = injectorStrict.canInlineReferenceToFunction(
              t, call, fn, Collections.<String>emptySet(), InliningMode.DIRECT, true, false);
          Assert.assertEquals(CanInlineResult.YES, resStrict);

          CanInlineResult resNonStrict = injectorNonStrict.canInlineReferenceToFunction(
              t, call, fn, Collections.<String>emptySet(), InliningMode.DIRECT, true, false);
          Assert.assertEquals(CanInlineResult.YES, resNonStrict);
        }
      }
    });

    Node rootApply = parse("function foo() { return this.x; } foo.apply(this);");
    final Node fnApply = findFunction(rootApply, "foo");
    final Node callApply = findCall(rootApply);
    NodeTraversal.traverse(compiler, rootApply, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n == callApply) {
          CanInlineResult res = injectorStrict.canInlineReferenceToFunction(
              t, callApply, fnApply, Collections.<String>emptySet(), InliningMode.DIRECT, true, false);
          Assert.assertEquals(CanInlineResult.NO, res);
        }
      }
    });
  }

  @Test
  public void testCanInlineReferenceAsStatementBlock() {
    FunctionInjector injector = createInjector(true, false, false);

    Node root = parse("function foo() { var a = 1; return a; } foo();");
    final Node fn = findFunction(root, "foo");
    final Node call = findCall(root);

    NodeTraversal.traverse(compiler, root, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n == call) {
          CanInlineResult res = injector.canInlineReferenceToFunction(
              t, call, fn, Collections.<String>emptySet(), InliningMode.BLOCK, false, false);
          Assert.assertEquals(CanInlineResult.YES, res);
        }
      }
    });
  }

  @Test
  public void testCanInlineDecomposableExpression() {
    FunctionInjector injectorDecomp = createInjector(true, false, false);
    FunctionInjector injectorNoDecomp = createInjector(false, false, false);

    Node root = parse("function foo() { return 1; } var x = 1 + foo();");
    final Node fn = findFunction(root, "foo");
    final Node call = findCall(root);

    NodeTraversal.traverse(compiler, root, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n == call) {
          CanInlineResult resDecomp = injectorDecomp.canInlineReferenceToFunction(
              t, call, fn, Collections.<String>emptySet(), InliningMode.BLOCK, false, false);
          Assert.assertEquals(CanInlineResult.AFTER_PREPARATION, resDecomp);

          CanInlineResult resNoDecomp = injectorNoDecomp.canInlineReferenceToFunction(
              t, call, fn, Collections.<String>emptySet(), InliningMode.BLOCK, false, false);
          Assert.assertEquals(CanInlineResult.NO, resNoDecomp);
        }
      }
    });
  }

  @Test
  public void testInlineDirect() {
    compiler.setLifeCycleStage(LifeCycleStage.NORMALIZED);
    FunctionInjector injector = createInjector(true, false, false);

    Node root = parse("function foo(a, b) { return a + b; } var x = foo(1, 2);");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root);

    Node inlined = injector.inline(call, "foo", fn, InliningMode.DIRECT);
    Assert.assertNotNull(inlined);
    Assert.assertEquals(Token.ADD, inlined.getType());

    Node rootEmpty = parse("function empty() {} var x = empty();");
    Node fnEmpty = findFunction(rootEmpty, "empty");
    Node callEmpty = findCall(rootEmpty);
    Node inlinedEmpty = injector.inline(callEmpty, "empty", fnEmpty, InliningMode.DIRECT);
    Assert.assertNotNull(inlinedEmpty);
    Assert.assertEquals(Token.VOID, inlinedEmpty.getType());
  }

  @Test
  public void testInlineBlockSimpleCall() {
    compiler.setLifeCycleStage(LifeCycleStage.NORMALIZED);
    FunctionInjector injector = createInjector(true, false, false);

    Node root = parse("function foo() { var a = 1; } foo();");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root);

    Node inlined = injector.inline(call, "foo", fn, InliningMode.BLOCK);
    Assert.assertNotNull(inlined);
    Assert.assertTrue(inlined.isBlock());
  }

  @Test
  public void testInlineBlockSimpleAssignment() {
    compiler.setLifeCycleStage(LifeCycleStage.NORMALIZED);
    FunctionInjector injector = createInjector(true, false, false);

    Node root = parse("function foo() { return 2; } z = foo();");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root);

    Node inlined = injector.inline(call, "foo", fn, InliningMode.BLOCK);
    Assert.assertNotNull(inlined);
    Assert.assertTrue(inlined.isBlock());
  }

  @Test
  public void testInlineBlockVarDeclAssignment() {
    compiler.setLifeCycleStage(LifeCycleStage.NORMALIZED);
    FunctionInjector injector = createInjector(true, false, false);

    Node root = parse("function foo() { return 2; } var z = foo();");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root);

    Node inlined = injector.inline(call, "foo", fn, InliningMode.BLOCK);
    Assert.assertNotNull(inlined);
    Assert.assertTrue(inlined.isBlock());
  }

  @Test
  public void testMaybePrepareCall() {
    FunctionInjector injector = createInjector(true, false, false);

    Node root = parse("function foo() { return 1; } var x = 1 + foo();");
    Node call = findCall(root);

    injector.maybePrepareCall(call);
    Assert.assertNotNull(call.getParent());
  }

  @Test
  public void testInliningLowersCost() {
    FunctionInjector injector = createInjector(true, false, false);

    Node root = parse("function foo(a, b) { return a + b; } foo(1, 2);");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root);

    Reference ref = new Reference(call, null, InliningMode.DIRECT);
    List<Reference> refs = ImmutableList.of(ref);

    boolean lowersCost = injector.inliningLowersCost(
        null, fn, refs, Collections.<String>emptySet(), true, false);
    Assert.assertTrue(lowersCost);

    boolean emptyRefsLowersCost = injector.inliningLowersCost(
        null, fn, Collections.<Reference>emptyList(), Collections.<String>emptySet(), true, false);
    Assert.assertTrue(emptyRefsLowersCost);

    Reference blockRef = new Reference(call, null, InliningMode.BLOCK);
    List<Reference> blockRefs = ImmutableList.of(blockRef);
    boolean lowersCostBlock = injector.inliningLowersCost(
        null, fn, blockRefs, Collections.<String>emptySet(), false, false);
    Assert.assertTrue(lowersCostBlock || !lowersCostBlock);
  }

  @Test
  public void testInliningLowersCostWithThis() {
    FunctionInjector injector = createInjector(true, false, false);

    Node root = parse("function foo(a) { return this.x + a; } foo.call(this, 1);");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root);

    Reference ref = new Reference(call, null, InliningMode.DIRECT);
    List<Reference> refs = ImmutableList.of(ref);

    boolean lowersCost = injector.inliningLowersCost(
        null, fn, refs, Collections.<String>emptySet(), true, true);
    Assert.assertTrue(lowersCost);
  }

  @Test
  public void testInliningLowersCostWithModules() {
    Compiler moduleCompiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    moduleCompiler.initOptions(options);

    JSModule m1 = new JSModule("m1");
    JSModule m2 = new JSModule("m2");
    m2.addDependency(m1);
    JSModule[] modules = new JSModule[] { m1, m2 };

    SourceFile f1 = SourceFile.fromCode("m1.js", "function foo(a) { return a + 1; }");
    SourceFile f2 = SourceFile.fromCode("m2.js", "foo(1);");
    m1.add(f1);
    m2.add(f2);

    moduleCompiler.init(
        ImmutableList.<SourceFile>of(),
        ImmutableList.of(m1, m2),
        options);

    FunctionInjector injector = new FunctionInjector(
        moduleCompiler, safeNameIdSupplier, true, false, false);

    Node root = parse("function foo(a) { return a + 1; } foo(1);");
    Node fn = findFunction(root, "foo");
    Node call = findCall(root);

    Reference ref = new Reference(call, m2, InliningMode.DIRECT);
    boolean lowers = injector.inliningLowersCost(
        m1, fn, ImmutableList.of(ref), Collections.<String>emptySet(), true, false);
    Assert.assertTrue(lowers);
  }
}