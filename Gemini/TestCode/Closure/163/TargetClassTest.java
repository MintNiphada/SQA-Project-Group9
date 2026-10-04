package com.google.javascript.jscomp;

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;

public class AnalyzePrototypePropertiesTest {

  private Compiler createCompiler() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    return compiler;
  }

  private JSModuleGraph createSampleModuleGraph(JSModule root, JSModule m1, JSModule m2) {
    m1.addDependency(root);
    m2.addDependency(m1);
    return new JSModuleGraph(new JSModule[]{root, m1, m2});
  }

  @Test
  public void testProcessSimpleAssignment() {
    Compiler compiler = createCompiler();
    String js = "function Foo() {} Foo.prototype.bar = function() { return 1; }; var f = new Foo(); f.bar();";
    Node root = compiler.parseTestCode(js);
    Node externs = IR.root();

    AnalyzePrototypeProperties pass = new AnalyzePrototypeProperties(compiler, null, false, false);
    pass.process(externs, root);

    Collection<AnalyzePrototypeProperties.NameInfo> infos = pass.getAllNameInfo();
    Assert.assertNotNull(infos);
    Assert.assertFalse(infos.isEmpty());

    AnalyzePrototypeProperties.NameInfo barInfo = null;
    for (AnalyzePrototypeProperties.NameInfo info : infos) {
      if ("bar".equals(info.name)) {
        barInfo = info;
      }
    }
    Assert.assertNotNull(barInfo);
    Assert.assertTrue(barInfo.isReferenced());
    Assert.assertEquals("bar", barInfo.toString());
    Assert.assertFalse(barInfo.readsClosureVariables());
  }

  @Test
  public void testProcessObjectLiteralPrototype() {
    Compiler compiler = createCompiler();
    String js = "function Foo() {} Foo.prototype = { bar: function() {}, baz: 2 }; var f = new Foo(); f.bar();";
    Node root = compiler.parseTestCode(js);
    Node externs = IR.root();

    AnalyzePrototypeProperties pass = new AnalyzePrototypeProperties(compiler, null, true, false);
    pass.process(externs, root);

    Collection<AnalyzePrototypeProperties.NameInfo> infos = pass.getAllNameInfo();
    boolean foundBar = false;
    boolean foundBaz = false;
    for (AnalyzePrototypeProperties.NameInfo info : infos) {
      if ("bar".equals(info.name)) {
        foundBar = true;
        Assert.assertFalse(info.getDeclarations().isEmpty());
      }
      if ("baz".equals(info.name)) {
        foundBaz = true;
      }
    }
    Assert.assertTrue(foundBar);
    Assert.assertTrue(foundBaz);
  }

  @Test
  public void testProcessGlobalFunctionsAndAnchorUnused() {
    Compiler compiler = createCompiler();
    String js = "function globalFn() { return 1; } var globalFnVar = function() { return 2; };";
    Node root = compiler.parseTestCode(js);
    Node externs = IR.root();

    AnalyzePrototypeProperties pass = new AnalyzePrototypeProperties(compiler, null, false, true);
    pass.process(externs, root);

    Collection<AnalyzePrototypeProperties.NameInfo> infos = pass.getAllNameInfo();
    boolean foundGlobalFn = false;
    boolean foundGlobalFnVar = false;
    for (AnalyzePrototypeProperties.NameInfo info : infos) {
      if ("globalFn".equals(info.name)) {
        foundGlobalFn = true;
        Assert.assertTrue(info.isReferenced());
      }
      if ("globalFnVar".equals(info.name)) {
        foundGlobalFnVar = true;
        Assert.assertTrue(info.isReferenced());
      }
    }
    Assert.assertTrue(foundGlobalFn);
    Assert.assertTrue(foundGlobalFnVar);
  }

  @Test
  public void testClosureVariableReading() {
    Compiler compiler = createCompiler();
    String js = "function outer() { var x = 1; function Foo() {} Foo.prototype.get = function() { return x; }; }";
    Node root = compiler.parseTestCode(js);
    Node externs = IR.root();

    AnalyzePrototypeProperties pass = new AnalyzePrototypeProperties(compiler, null, false, false);
    pass.process(externs, root);

    Collection<AnalyzePrototypeProperties.NameInfo> infos = pass.getAllNameInfo();
    AnalyzePrototypeProperties.NameInfo getInfo = null;
    for (AnalyzePrototypeProperties.NameInfo info : infos) {
      if ("get".equals(info.name)) {
        getInfo = info;
      }
    }
    Assert.assertNotNull(getInfo);
    Assert.assertTrue(getInfo.readsClosureVariables());
  }

  @Test
  public void testExternPropertiesTraversal() {
    Compiler compiler = createCompiler();
    Node externs = compiler.parseTestCode("var window; window.customProp;");
    Node root = compiler.parseTestCode("function Foo() {} Foo.prototype.customProp = function() {};");

    AnalyzePrototypeProperties pass = new AnalyzePrototypeProperties(compiler, null, false, false);
    pass.process(externs, root);

    Collection<AnalyzePrototypeProperties.NameInfo> infos = pass.getAllNameInfo();
    AnalyzePrototypeProperties.NameInfo propInfo = null;
    for (AnalyzePrototypeProperties.NameInfo info : infos) {
      if ("customProp".equals(info.name)) {
        propInfo = info;
      }
    }
    Assert.assertNotNull(propInfo);
    Assert.assertTrue(propInfo.isReferenced());
  }

  @Test
  public void testModuleGraphPropagation() {
    Compiler compiler = createCompiler();
    JSModule m0 = new JSModule("m0");
    JSModule m1 = new JSModule("m1");
    JSModule m2 = new JSModule("m2");
    JSModuleGraph graph = createSampleModuleGraph(m0, m1, m2);

    AnalyzePrototypeProperties pass = new AnalyzePrototypeProperties(compiler, graph, false, false);

    AnalyzePrototypeProperties.NameInfo info = pass.new NameInfo("testProp");
    Assert.assertNull(info.getDeepestCommonModuleRef());

    boolean changed1 = info.markReference(m1);
    Assert.assertTrue(changed1);
    Assert.assertTrue(info.isReferenced());
    Assert.assertEquals(m1, info.getDeepestCommonModuleRef());

    boolean changed2 = info.markReference(m2);
    Assert.assertFalse(changed2);
    Assert.assertEquals(m1, info.getDeepestCommonModuleRef());

    boolean changed3 = info.markReference(m0);
    Assert.assertTrue(changed3);
    Assert.assertEquals(m0, info.getDeepestCommonModuleRef());
  }

  @Test
  public void testAssignmentPropertyGettersAndRemove() {
    Compiler compiler = createCompiler();
    String js = "Foo.prototype.bar = function() { return 42; };";
    Node root = compiler.parseTestCode(js);
    Node expr = root.getFirstChild();

    AnalyzePrototypeProperties.AssignmentProperty prop =
        new AnalyzePrototypeProperties.AssignmentProperty(expr, null);

    Assert.assertNull(prop.getModule());
    Assert.assertNotNull(prop.getPrototype());
    Assert.assertNotNull(prop.getValue());
    Assert.assertEquals(Token.FUNCTION, prop.getValue().getType());

    prop.remove();
    Assert.assertEquals(0, root.getChildCount());
  }

  @Test
  public void testLiteralPropertyGettersAndRemove() {
    Compiler compiler = createCompiler();
    String js = "Foo.prototype = { key1: 1, key2: 2 };";
    Node root = compiler.parseTestCode(js);
    Node assign = root.getFirstChild().getFirstChild();
    Node objLit = assign.getLastChild();
    Node key1 = objLit.getFirstChild();
    Node value1 = key1.getFirstChild();

    JSModule mod = new JSModule("mod");
    AnalyzePrototypeProperties.LiteralProperty prop =
        new AnalyzePrototypeProperties.LiteralProperty(key1, value1, objLit, assign, mod);

    Assert.assertEquals(mod, prop.getModule());
    Assert.assertEquals(assign.getFirstChild(), prop.getPrototype());
    Assert.assertEquals(value1, prop.getValue());

    prop.remove();
    Assert.assertEquals(1, objLit.getChildCount());
  }

  @Test
  public void testGlobalFunctionDeclarationSingleAndMultipleVar() {
    Compiler compiler = createCompiler();
    String js = "function named() {} var single = function() {}; var a = 1, multi = function() {};";
    Node root = compiler.parseTestCode(js);

    Node fnNode = root.getFirstChild();
    Node varSingle = fnNode.getNext();
    Node nameSingle = varSingle.getFirstChild();
    Node varMulti = varSingle.getNext();
    Node nameMulti = varMulti.getLastChild();

    AnalyzePrototypeProperties pass = new AnalyzePrototypeProperties(compiler, null, false, false);

    AnalyzePrototypeProperties.GlobalFunction gfNamed =
        pass.new GlobalFunction(fnNode.getFirstChild(), fnNode, root, null);
    Assert.assertEquals(fnNode, gfNamed.getFunctionNode());
    gfNamed.remove();
    Assert.assertEquals(2, root.getChildCount());

    AnalyzePrototypeProperties.GlobalFunction gfSingle =
        pass.new GlobalFunction(nameSingle, varSingle, root, null);
    gfSingle.remove();
    Assert.assertEquals(1, root.getChildCount());

    AnalyzePrototypeProperties.GlobalFunction gfMulti =
        pass.new GlobalFunction(nameMulti, varMulti, root, null);
    gfMulti.remove();
    Assert.assertEquals(1, root.getFirstChild().getChildCount());
  }

  @Test
  public void testObjectLiteralUsesProperties() {
    Compiler compiler = createCompiler();
    String js = "function run() { var obj = { propA: 1, propB: 2 }; return obj.propA; }";
    Node root = compiler.parseTestCode(js);
    Node externs = IR.root();

    AnalyzePrototypeProperties pass = new AnalyzePrototypeProperties(compiler, null, false, false);
    pass.process(externs, root);

    Collection<AnalyzePrototypeProperties.NameInfo> infos = pass.getAllNameInfo();
    boolean foundPropB = false;
    for (AnalyzePrototypeProperties.NameInfo info : infos) {
      if ("propB".equals(info.name)) {
        foundPropB = true;
      }
    }
    Assert.assertTrue(foundPropB);
  }

  @Test
  public void testNonPrototypeAssignmentIgnored() {
    Compiler compiler = createCompiler();
    String js = "var obj = {}; obj.bar = 123;";
    Node root = compiler.parseTestCode(js);
    Node externs = IR.root();

    AnalyzePrototypeProperties pass = new AnalyzePrototypeProperties(compiler, null, false, false);
    pass.process(externs, root);

    Collection<AnalyzePrototypeProperties.NameInfo> infos = pass.getAllNameInfo();
    for (AnalyzePrototypeProperties.NameInfo info : infos) {
      if ("bar".equals(info.name)) {
        Assert.assertTrue(info.getDeclarations().isEmpty());
      }
    }
  }
}
