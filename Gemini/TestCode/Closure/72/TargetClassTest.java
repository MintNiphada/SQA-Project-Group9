package com.google.javascript.jscomp;

import com.google.common.base.Supplier;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FunctionToBlockMutatorTest {

  private Compiler compiler;
  private Supplier<String> idSupplier;

  @Before
  public void setUp() {
    compiler = new Compiler();
    idSupplier = new Supplier<String>() {
      private int count = 0;
      @Override
      public String get() {
        return String.valueOf(count++);
      }
    };
  }

  private Node parseFunction(String js) {
    Node script = compiler.parseTestCode(js);
    return script.getFirstChild();
  }

  private Node parseCall(String js) {
    Node script = compiler.parseTestCode(js);
    return script.getFirstChild().getFirstChild();
  }

  @Test
  public void testLabelNameSupplier() {
    Supplier<String> baseSupplier = new Supplier<String>() {
      private int count = 0;
      @Override
      public String get() {
        return String.valueOf(count++);
      }
    };
    FunctionToBlockMutator.LabelNameSupplier supplier =
        new FunctionToBlockMutator.LabelNameSupplier(baseSupplier);
    Assert.assertEquals("JSCompiler_inline_label_0", supplier.get());
    Assert.assertEquals("JSCompiler_inline_label_1", supplier.get());
  }

  @Test
  public void testSimpleFunctionWithoutReturn() {
    FunctionToBlockMutator mutator = new FunctionToBlockMutator(compiler, idSupplier);
    Node fnNode = parseFunction("function foo() { var a = 1; }");
    Node callNode = parseCall("foo();");

    Node result = mutator.mutate("foo", fnNode, callNode, null, false, false);
    Assert.assertNotNull(result);
    Assert.assertEquals(Token.BLOCK, result.getType());
  }

  @Test
  public void testSimpleFunctionWithResultNeeded() {
    FunctionToBlockMutator mutator = new FunctionToBlockMutator(compiler, idSupplier);
    Node fnNode = parseFunction("function foo() { var a = 1; }");
    Node callNode = parseCall("foo();");

    Node result = mutator.mutate("foo", fnNode, callNode, "res", true, false);
    Assert.assertNotNull(result);
    Assert.assertEquals(Token.BLOCK, result.getType());
    Node last = result.getLastChild();
    Assert.assertEquals(Token.EXPR_RESULT, last.getType());
    Assert.assertEquals(Token.ASSIGN, last.getFirstChild().getType());
    Assert.assertEquals("res", last.getFirstChild().getFirstChild().getString());
  }

  @Test
  public void testFunctionWithSingleReturnExpression() {
    FunctionToBlockMutator mutator = new FunctionToBlockMutator(compiler, idSupplier);
    Node fnNode = parseFunction("function foo(x) { return x; }");
    Node callNode = parseCall("foo(5);");

    Node result = mutator.mutate("foo", fnNode, callNode, "res", false, false);
    Assert.assertNotNull(result);
    Assert.assertEquals(Token.BLOCK, result.getType());
    Node last = result.getLastChild();
    Assert.assertEquals(Token.EXPR_RESULT, last.getType());
    Assert.assertEquals(Token.ASSIGN, last.getFirstChild().getType());
  }

  @Test
  public void testFunctionWithSingleReturnNoResultNeeded() {
    FunctionToBlockMutator mutator = new FunctionToBlockMutator(compiler, idSupplier);
    Node fnNode = parseFunction("function foo(x) { return x; }");
    Node callNode = parseCall("foo(5);");

    Node result = mutator.mutate("foo", fnNode, callNode, null, false, false);
    Assert.assertNotNull(result);
    Assert.assertEquals(Token.BLOCK, result.getType());
    Node last = result.getLastChild();
    Assert.assertEquals(Token.EXPR_RESULT, last.getType());
    Assert.assertEquals(Token.NUMBER, last.getFirstChild().getType());
  }

  @Test
  public void testFunctionWithEmptyReturn() {
    FunctionToBlockMutator mutator = new FunctionToBlockMutator(compiler, idSupplier);
    Node fnNode = parseFunction("function foo() { return; }");
    Node callNode = parseCall("foo();");

    Node result = mutator.mutate("foo", fnNode, callNode, null, false, false);
    Assert.assertNotNull(result);
    Assert.assertEquals(Token.BLOCK, result.getType());
    Assert.assertFalse(result.hasChildren());
  }

  @Test
  public void testFunctionWithEmptyReturnAndResultNeeded() {
    FunctionToBlockMutator mutator = new FunctionToBlockMutator(compiler, idSupplier);
    Node fnNode = parseFunction("function foo() { return; }");
    Node callNode = parseCall("foo();");

    Node result = mutator.mutate("foo", fnNode, callNode, "res", false, false);
    Assert.assertNotNull(result);
    Assert.assertEquals(Token.BLOCK, result.getType());
    Node last = result.getLastChild();
    Assert.assertEquals(Token.EXPR_RESULT, last.getType());
    Assert.assertEquals(Token.ASSIGN, last.getFirstChild().getType());
  }

  @Test
  public void testMultipleReturnsNeedLabelAndBreak() {
    FunctionToBlockMutator mutator = new FunctionToBlockMutator(compiler, idSupplier);
    Node fnNode = parseFunction("function foo(x) { if (x) { return 1; } return 2; }");
    Node callNode = parseCall("foo(1);");

    Node result = mutator.mutate("foo", fnNode, callNode, "res", false, false);
    Assert.assertNotNull(result);
    Assert.assertEquals(Token.BLOCK, result.getType());
    Node label = result.getFirstChild();
    Assert.assertEquals(Token.LABEL, label.getType());
    Node labelName = label.getFirstChild();
    Assert.assertEquals(Token.LABEL_NAME, labelName.getType());
    Assert.assertTrue(labelName.getString().startsWith("JSCompiler_inline_label_foo_"));
  }

  @Test
  public void testMultipleReturnsNoResultNeeded() {
    FunctionToBlockMutator mutator = new FunctionToBlockMutator(compiler, idSupplier);
    Node fnNode = parseFunction("function foo(x) { if (x) { return 1; } return; }");
    Node callNode = parseCall("foo(1);");

    Node result = mutator.mutate("foo", fnNode, callNode, null, false, false);
    Assert.assertNotNull(result);
    Assert.assertEquals(Token.BLOCK, result.getType());
    Node label = result.getFirstChild();
    Assert.assertEquals(Token.LABEL, label.getType());
  }

  @Test
  public void testAnonymousFunctionLabelName() {
    FunctionToBlockMutator mutator = new FunctionToBlockMutator(compiler, idSupplier);
    Node fnNode = parseFunction("function (x) { if (x) return 1; return 2; }");
    Node callNode = parseCall("foo(1);");

    Node result = mutator.mutate(null, fnNode, callNode, "res", false, false);
    Assert.assertNotNull(result);
    Node label = result.getFirstChild();
    Assert.assertEquals(Token.LABEL, label.getType());
    Node labelName = label.getFirstChild();
    Assert.assertTrue(labelName.getString().startsWith("JSCompiler_inline_label_anon_"));

    Node result2 = mutator.mutate("", fnNode, callNode, "res", false, false);
    Assert.assertNotNull(result2);
    Node label2 = result2.getFirstChild();
    Assert.assertEquals(Token.LABEL, label2.getType());
    Node labelName2 = label2.getFirstChild();
    Assert.assertTrue(labelName2.getString().startsWith("JSCompiler_inline_label_anon_"));
  }

  @Test
  public void testCallInLoopFixesUninitializedVars() {
    FunctionToBlockMutator mutator = new FunctionToBlockMutator(compiler, idSupplier);
    Node fnNode = parseFunction("function foo() { var a; var b = 1; for (var x in y) { var z; } }");
    Node callNode = parseCall("foo();");

    Node result = mutator.mutate("foo", fnNode, callNode, null, false, true);
    Assert.assertNotNull(result);

    Node firstVar = result.getFirstChild();
    Assert.assertEquals(Token.VAR, firstVar.getType());
    Node varName = firstVar.getFirstChild();
    Assert.assertTrue(varName.hasChildren());
    Assert.assertEquals(Token.VOID, varName.getFirstChild().getType());
  }

  @Test
  public void testModifiedParametersAliasing() {
    FunctionToBlockMutator mutator = new FunctionToBlockMutator(compiler, idSupplier);
    Node fnNode = parseFunction("function foo(x, y) { x = x + 1; return x + y; }");
    Node callNode = parseCall("foo(a + b, 2);");

    Node result = mutator.mutate("foo", fnNode, callNode, "res", false, false);
    Assert.assertNotNull(result);
    Assert.assertEquals(Token.BLOCK, result.getType());
  }

  @Test
  public void testNestedFunctionDoesNotReplaceInnerReturn() {
    FunctionToBlockMutator mutator = new FunctionToBlockMutator(compiler, idSupplier);
    Node fnNode = parseFunction("function foo() { function bar() { return 1; } return 2; }");
    Node callNode = parseCall("foo();");

    Node result = mutator.mutate("foo", fnNode, callNode, "res", false, false);
    Assert.assertNotNull(result);
    Assert.assertEquals(Token.BLOCK, result.getType());

    Node innerFn = result.getFirstChild();
    Assert.assertEquals(Token.FUNCTION, innerFn.getType());
  }
}