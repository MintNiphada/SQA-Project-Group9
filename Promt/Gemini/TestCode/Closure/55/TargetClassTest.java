package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Assert;
import org.junit.Test;

public class TargetClassTest {

  private Node parseAndProcess(String js) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    Node externs = new Node(com.google.javascript.rhino.Token.BLOCK);
    Node root = compiler.parseSyntheticCode("test.js", js);
    Assert.assertNotNull(root);

    FunctionRewriter rewriter = new FunctionRewriter(compiler);
    rewriter.process(externs, root);
    return root;
  }

  private String toSource(Node root) {
    Compiler compiler = new Compiler();
    return compiler.toSource(root);
  }

  @Test
  public void testEmptyFunctionReduction() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 20; i++) {
      sb.append("a.prototype.f").append(i).append(" = function() {};\n");
    }
    Node root = parseAndProcess(sb.toString());
    String output = toSource(root);
    Assert.assertTrue(output.contains("JSCompiler_emptyFn"));
    Assert.assertTrue(output.contains("JSCompiler_emptyFn()"));
  }

  @Test
  public void testIdentityFunctionReduction() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 20; i++) {
      sb.append("a.prototype.f").append(i).append(" = function(x) { return x; };\n");
    }
    Node root = parseAndProcess(sb.toString());
    String output = toSource(root);
    Assert.assertTrue(output.contains("JSCompiler_identityFn"));
    Assert.assertTrue(output.contains("JSCompiler_identityFn()"));
  }

  @Test
  public void testReturnConstantReduction() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 20; i++) {
      sb.append("a.prototype.f").append(i).append(" = function() { return 42; };\n");
    }
    Node root = parseAndProcess(sb.toString());
    String output = toSource(root);
    Assert.assertTrue(output.contains("JSCompiler_returnArg"));
    Assert.assertTrue(output.contains("JSCompiler_returnArg(42)"));
  }

  @Test
  public void testGetterReduction() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 20; i++) {
      sb.append("a.prototype.getFoo").append(i).append(" = function() { return this.foo_").append(i).append("; };\n");
    }
    Node root = parseAndProcess(sb.toString());
    String output = toSource(root);
    Assert.assertTrue(output.contains("JSCompiler_get"));
  }

  @Test
  public void testSetterReduction() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 20; i++) {
      sb.append("a.prototype.setFoo").append(i).append(" = function(val) { this.foo_").append(i).append(" = val; };\n");
    }
    Node root = parseAndProcess(sb.toString());
    String output = toSource(root);
    Assert.assertTrue(output.contains("JSCompiler_set"));
  }

  @Test
  public void testBelowSavingsThreshold() {
    String js = "a.prototype.f = function() {};\n";
    Node root = parseAndProcess(js);
    String output = toSource(root);
    Assert.assertFalse(output.contains("JSCompiler_emptyFn"));
  }

  @Test
  public void testNonReducibleFunctions() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 20; i++) {
      sb.append("a.prototype.f").append(i).append(" = function(x, y) { return x + y; };\n");
    }
    Node root = parseAndProcess(sb.toString());
    String output = toSource(root);
    Assert.assertFalse(output.contains("JSCompiler_"));
  }

  @Test
  public void testInvalidGettersAndSetters() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 10; i++) {
      sb.append("a.prototype.g").append(i).append(" = function() { return other.prop; };\n");
      sb.append("a.prototype.s").append(i).append(" = function(x) { other.prop = x; };\n");
      sb.append("a.prototype.h").append(i).append(" = function(x) { this.prop = other; };\n");
      sb.append("a.prototype.k").append(i).append(" = function(x, y) { return y; };\n");
      sb.append("a.prototype.m").append(i).append(" = function() { return function() {}; };\n");
    }
    Node root = parseAndProcess(sb.toString());
    String output = toSource(root);
    Assert.assertFalse(output.contains("JSCompiler_get"));
    Assert.assertFalse(output.contains("JSCompiler_set"));
  }

  @Test
  public void testParseHelperCode() {
    Compiler compiler = new Compiler();
    FunctionRewriter rewriter = new FunctionRewriter(compiler);
    FunctionRewriter.Reducer dummyReducer = new FunctionRewriter.Reducer() {
      @Override
      String getHelperSource() {
        return "function dummy() { return 1; }";
      }

      @Override
      Node reduce(Node node) {
        return node;
      }
    };

    Node helperNode = rewriter.parseHelperCode(dummyReducer);
    Assert.assertNotNull(helperNode);
    Assert.assertTrue(helperNode.isFunction());
  }

  @Test
  public void testParseHelperCodeInvalid() {
    Compiler compiler = new Compiler();
    FunctionRewriter rewriter = new FunctionRewriter(compiler);
    FunctionRewriter.Reducer invalidReducer = new FunctionRewriter.Reducer() {
      @Override
      String getHelperSource() {
        return "this is invalid js @@@ ###";
      }

      @Override
      Node reduce(Node node) {
        return node;
      }
    };

    Node helperNode = rewriter.parseHelperCode(invalidReducer);
    Assert.assertNull(helperNode);
  }

  @Test
  public void testMixedReductions() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 10; i++) {
      sb.append("a.prototype.empty").append(i).append(" = function() {};\n");
      sb.append("a.prototype.id").append(i).append(" = function(x) { return x; };\n");
      sb.append("a.prototype.get").append(i).append(" = function() { return this.val_").append(i).append("; };\n");
      sb.append("a.prototype.set").append(i).append(" = function(v) { this.val_").append(i).append(" = v; };\n");
      sb.append("a.prototype.c").append(i).append(" = function() { return \"constVal\"; };\n");
    }
    Node root = parseAndProcess(sb.toString());
    String output = toSource(root);
    Assert.assertTrue(output.contains("JSCompiler_emptyFn"));
    Assert.assertTrue(output.contains("JSCompiler_identityFn"));
    Assert.assertTrue(output.contains("JSCompiler_get"));
    Assert.assertTrue(output.contains("JSCompiler_set"));
    Assert.assertTrue(output.contains("JSCompiler_returnArg"));
  }
}