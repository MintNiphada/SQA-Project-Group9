package com.google.javascript.jscomp;

import com.google.common.base.Supplier;
import com.google.common.collect.Sets;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

public class InlineFunctionsTest {

  private Compiler compiler;
  private Supplier<String> idSupplier;

  @Before
  public void setUp() {
    compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);
    idSupplier = compiler.getUniqueNameIdSupplier();
  }

  private void testInline(String js, boolean inlineGlobal, boolean inlineLocal, boolean blockInlining) {
    Node root = compiler.parseTestCode(js);
    Node externs = new Node(Token.BLOCK);
    Node mainRoot = new Node(Token.BLOCK, externs, root);
    InlineFunctions inliner = new InlineFunctions(compiler, idSupplier, inlineGlobal, inlineLocal, blockInlining);
    inliner.process(externs, root);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorNullCompiler() {
    new InlineFunctions(null, idSupplier, true, true, true);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorNullSupplier() {
    new InlineFunctions(compiler, null, true, true, true);
  }

  @Test(expected = IllegalStateException.class)
  public void testProcessUnnormalizedThrows() {
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.RAW);
    InlineFunctions inliner = new InlineFunctions(compiler, idSupplier, true, true, true);
    Node root = compiler.parseTestCode("function foo() {}");
    inliner.process(new Node(Token.BLOCK), root);
  }

  @Test
  public void testProcessEmptyCode() {
    testInline("", true, true, true);
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testDirectInliningNamedFunction() {
    testInline("function foo() { return 42; } var x = foo();", true, true, true);
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testDirectInliningFunctionVar() {
    testInline("var foo = function() { return 42; }; var x = foo();", true, true, true);
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testDirectInliningFunctionExpression() {
    testInline("(function() { return 42; })();", true, true, true);
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testDirectInliningFunctionCallMethod() {
    testInline("(function() { return 42; }).call(this);", true, true, true);
    testInline("function foo() { return 42; } foo.call(this);", true, true, true);
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testBlockInliningEnabled() {
    testInline("function foo(a) { var b = a + 1; return b; } var x = foo(2);", true, true, true);
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testBlockInliningDisabled() {
    testInline("function foo(a) { var b = a + 1; return b; } var x = foo(2);", true, true, false);
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testInlineLocalFunctionsOnly() {
    testInline("function outer() { function inner() { return 1; } var x = inner(); }", false, true, true);
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testInlineGlobalFunctionsOnly() {
    testInline("function outer() { function inner() { return 1; } var x = inner(); }", true, false, true);
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testFunctionWithThis() {
    testInline("function foo() { return this.x; } foo();", true, true, true);
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testFunctionWithInnerFunctions() {
    testInline("function foo() { function bar() { return 1; } return bar(); } foo();", true, true, true);
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testFunctionWithModifiedParameters() {
    testInline("function foo(x) { x = x + 1; return x; } foo(5);", true, true, true);
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testDuplicateFunctionDefinitions() {
    testInline("function foo() { return 1; } function foo() { return 2; } foo();", true, true, true);
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testRecursiveFunction() {
    testInline("function foo(n) { if (n <= 0) return 0; return foo(n - 1); } foo(3);", true, true, true);
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testSpecialFunctionNotCandidate() {
    testInline("function JSCompiler_renameProperty(a) { return a; } JSCompiler_renameProperty('x');", true, true, true);
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testObjectPropertyStringReference() {
    testInline("function foo() { return 1; } new JSCompiler_ObjectPropertyString(window, foo); foo();", true, true, true);
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testFunctionAssigned() {
    testInline("function foo() { return 1; } foo = function() { return 2; }; foo();", true, true, true);
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testFunctionAliased() {
    testInline("function foo() { return 1; } var alias = foo; alias(); foo();", true, true, true);
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testNestedFunctionCallsResolution() {
    testInline("function a() { return 1; } function b() { return a(); } var x = b();", true, true, true);
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testSpecializationEnabled() {
    SpecializeModule.SpecializationState specState = new SpecializeModule.SpecializationState() {
      @Override
      public boolean canFixupFunction(Node n) {
        return true;
      }
      @Override
      public void reportSpecializedFunction(Node n) {}
      @Override
      public void reportRemovedFunction(Node n, Node b) {}
    };

    InlineFunctions inliner = new InlineFunctions(compiler, idSupplier, true, true, true);
    inliner.enableSpecialization(specState);
    Node root = compiler.parseTestCode("function foo() { return 1; } var x = foo();");
    inliner.process(new Node(Token.BLOCK), root);
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testSpecializationCannotFixupFunction() {
    SpecializeModule.SpecializationState specState = new SpecializeModule.SpecializationState() {
      @Override
      public boolean canFixupFunction(Node n) {
        return false;
      }
      @Override
      public void reportSpecializedFunction(Node n) {}
      @Override
      public void reportRemovedFunction(Node n, Node b) {}
    };

    InlineFunctions inliner = new InlineFunctions(compiler, idSupplier, true, true, true);
    inliner.enableSpecialization(specState);
    Node root = compiler.parseTestCode("function foo() { return 1; } var x = foo();");
    inliner.process(new Node(Token.BLOCK), root);
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testIsCandidateUsage() {
    Node varNode = new Node(Token.VAR);
    Node nameNode = Node.newString(Token.NAME, "foo");
    varNode.addChildToBack(nameNode);
    Assert.assertTrue(InlineFunctions.isCandidateUsage(nameNode));

    Node callNode = new Node(Token.CALL);
    Node calleeName = Node.newString(Token.NAME, "foo");
    callNode.addChildToBack(calleeName);
    Assert.assertTrue(InlineFunctions.isCandidateUsage(calleeName));

    Node getprop = new Node(Token.GETPROP, Node.newString(Token.NAME, "foo"), Node.newString(Token.STRING, "call"));
    Node callProp = new Node(Token.CALL, getprop);
    Assert.assertTrue(InlineFunctions.isCandidateUsage(getprop.getFirstChild()));

    Node assign = new Node(Token.ASSIGN, Node.newString(Token.NAME, "foo"), Node.newNumber(1));
    Assert.assertFalse(InlineFunctions.isCandidateUsage(assign.getFirstChild()));
  }

  @Test
  public void testFunctionStateAccessorsAndFlags() {
    InlineFunctions inliner = new InlineFunctions(compiler, idSupplier, true, true, true);
    InlineFunctions.FunctionState fs = inliner.getOrCreateFunctionState("testFn");
    Assert.assertNotNull(fs);
    Assert.assertSame(fs, inliner.getOrCreateFunctionState("testFn"));

    Assert.assertFalse(fs.hasExistingFunctionDefinition());
    Assert.assertNull(fs.getFn());
    Assert.assertTrue(fs.canInline());
    Assert.assertTrue(fs.canRemove());
    Assert.assertFalse(fs.canInlineDirectly());
    Assert.assertFalse(fs.hasReferences());
    Assert.assertFalse(fs.getReferencesThis());
    Assert.assertFalse(fs.hasInnerFunctions());
    Assert.assertNull(fs.getModule());
    Assert.assertTrue(fs.getNamesToAlias().isEmpty());

    fs.setReferencesThis(true);
    Assert.assertTrue(fs.getReferencesThis());

    fs.setHasInnerFunctions(true);
    Assert.assertTrue(fs.hasInnerFunctions());

    JSModule mod = new JSModule("m1");
    fs.setModule(mod);
    Assert.assertSame(mod, fs.getModule());

    Set<String> aliases = Sets.newHashSet("a", "b");
    fs.setNamesToAlias(aliases);
    Assert.assertEquals(aliases, fs.getNamesToAlias());

    Node call = new Node(Token.CALL, Node.newString(Token.NAME, "testFn"));
    InlineFunctions.Reference refBlock = inliner.new Reference(call, mod, FunctionInjector.InliningMode.BLOCK, false);
    fs.addReference(refBlock);
    Assert.assertTrue(fs.hasReferences());
    Assert.assertTrue(fs.hasBlockInliningReferences());
    Assert.assertEquals(1, fs.getReferences().size());
    Assert.assertSame(refBlock, fs.getReference(call));

    fs.removeBlockInliningReferences();
    Assert.assertFalse(fs.hasBlockInliningReferences());
    Assert.assertFalse(fs.hasReferences());

    fs.setInline(false);
    Assert.assertFalse(fs.canInline());
    Assert.assertFalse(fs.canRemove());

    fs.setRemove(true);
    Assert.assertTrue(fs.canRemove());
    fs.setRemove(false);
    Assert.assertFalse(fs.canRemove());

    fs.inlineDirectly(true);
    Assert.assertTrue(fs.canInlineDirectly());

    Node fnNode = new Node(Token.FUNCTION);
    fs.setSafeFnNode(fnNode);
    Assert.assertSame(fnNode, fs.getSafeFnNode());
  }

  @Test(expected = IllegalStateException.class)
  public void testVerifyAllReferencesInlinedThrowsWhenMissed() {
    InlineFunctions inliner = new InlineFunctions(compiler, idSupplier, true, true, true);
    InlineFunctions.FunctionState fs = inliner.getOrCreateFunctionState("foo");
    Node call = new Node(Token.CALL, Node.newString(Token.NAME, "foo"));
    Node parent = new Node(Token.EXPR_RESULT, call);
    InlineFunctions.Reference ref = inliner.new Reference(call, null, FunctionInjector.InliningMode.DIRECT, false);
    ref.inlined = false;
    fs.addReference(ref);
    inliner.verifyAllReferencesInlined(fs);
  }

  @Test
  public void testVerifyAllReferencesInlinedPasses() {
    InlineFunctions inliner = new InlineFunctions(compiler, idSupplier, true, true, true);
    InlineFunctions.FunctionState fs = inliner.getOrCreateFunctionState("foo");
    Node call = new Node(Token.CALL, Node.newString(Token.NAME, "foo"));
    Node parent = new Node(Token.EXPR_RESULT, call);
    InlineFunctions.Reference ref = inliner.new Reference(call, null, FunctionInjector.InliningMode.DIRECT, false);
    ref.inlined = true;
    fs.addReference(ref);
    inliner.verifyAllReferencesInlined(fs);
  }
}
