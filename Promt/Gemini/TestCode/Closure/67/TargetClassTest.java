package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.Deque;

public class AnalyzePrototypePropertiesTest {

  private AnalyzePrototypeProperties process(String js, boolean canModifyExterns, boolean anchorUnusedVars) {
    return process("", js, canModifyExterns, anchorUnusedVars);
  }

  private AnalyzePrototypeProperties process(String externsJs, String js, boolean canModifyExterns, boolean anchorUnusedVars) {
    Compiler compiler = new Compiler();
    Node externsRoot = compiler.parseTestCode(externsJs);
    Node mainRoot = compiler.parseTestCode(js);
    AnalyzePrototypeProperties pass = new AnalyzePrototypeProperties(compiler, null, canModifyExterns, anchorUnusedVars);
    pass.process(externsRoot, mainRoot);
    return pass;
  }

  private AnalyzePrototypeProperties.NameInfo findNameInfo(Collection<AnalyzePrototypeProperties.NameInfo> infos, String name) {
    for (AnalyzePrototypeProperties.NameInfo info : infos) {
      if (name.equals(info.name)) {
        return info;
      }
    }
    return null;
  }

  @Test
  public void testImplicitlyUsedProperties() {
    AnalyzePrototypeProperties pass = process("", false, false);
    Collection<AnalyzePrototypeProperties.NameInfo> allInfo = pass.getAllNameInfo();

    AnalyzePrototypeProperties.NameInfo toStringInfo = findNameInfo(allInfo, "toString");
    Assert.assertNotNull(toStringInfo);
    Assert.assertTrue(toStringInfo.isReferenced());
    Assert.assertEquals("toString", toStringInfo.toString());

    AnalyzePrototypeProperties.NameInfo lengthInfo = findNameInfo(allInfo, "length");
    Assert.assertNotNull(lengthInfo);
    Assert.assertTrue(lengthInfo.isReferenced());

    AnalyzePrototypeProperties.NameInfo valueOfInfo = findNameInfo(allInfo, "valueOf");
    Assert.assertNotNull(valueOfInfo);
    Assert.assertTrue(valueOfInfo.isReferenced());
  }

  @Test
  public void testPrototypePropertyAssignment() {
    String js = "function Foo() {}\n"
        + "Foo.prototype.bar = function() { return 1; };\n"
        + "var x = new Foo();\n"
        + "x.bar();";
    AnalyzePrototypeProperties pass = process(js, false, false);
    Collection<AnalyzePrototypeProperties.NameInfo> allInfo = pass.getAllNameInfo();

    AnalyzePrototypeProperties.NameInfo barInfo = findNameInfo(allInfo, "bar");
    Assert.assertNotNull(barInfo);
    Assert.assertTrue(barInfo.isReferenced());
    Deque<AnalyzePrototypeProperties.Symbol> decls = barInfo.getDeclarations();
    Assert.assertEquals(1, decls.size());

    AnalyzePrototypeProperties.Symbol symbol = decls.peek();
    Assert.assertTrue(symbol instanceof AnalyzePrototypeProperties.AssignmentProperty);
    AnalyzePrototypeProperties.AssignmentProperty assignProp = (AnalyzePrototypeProperties.AssignmentProperty) symbol;
    Assert.assertNotNull(assignProp.getPrototype());
    Assert.assertNotNull(assignProp.getValue());
    Assert.assertNull(assignProp.getModule());
  }

  @Test
  public void testPrototypeObjectLiteral() {
    String js = "function Foo() {}\n"
        + "Foo.prototype = {\n"
        + "  baz: function() { return 2; },\n"
        + "  qux: 42\n"
        + "};\n"
        + "var f = new Foo();\n"
        + "f.baz();";
    AnalyzePrototypeProperties pass = process(js, false, false);
    Collection<AnalyzePrototypeProperties.NameInfo> allInfo = pass.getAllNameInfo();

    AnalyzePrototypeProperties.NameInfo bazInfo = findNameInfo(allInfo, "baz");
    Assert.assertNotNull(bazInfo);
    Assert.assertTrue(bazInfo.isReferenced());
    Assert.assertEquals(1, bazInfo.getDeclarations().size());

    AnalyzePrototypeProperties.Symbol symbol = bazInfo.getDeclarations().peek();
    Assert.assertTrue(symbol instanceof AnalyzePrototypeProperties.LiteralProperty);
    AnalyzePrototypeProperties.LiteralProperty litProp = (AnalyzePrototypeProperties.LiteralProperty) symbol;
    Assert.assertNotNull(litProp.getPrototype());
    Assert.assertNotNull(litProp.getValue());
    Assert.assertNull(litProp.getModule());

    AnalyzePrototypeProperties.NameInfo quxInfo = findNameInfo(allInfo, "qux");
    Assert.assertNotNull(quxInfo);
    Assert.assertFalse(quxInfo.isReferenced());
  }

  @Test
  public void testGlobalFunctionDeclarationAndVar() {
    String js = "function globalFn() { return 1; }\n"
        + "var globalVarFn = function() { return 2; };\n"
        + "globalFn();\n"
        + "globalVarFn();";
    AnalyzePrototypeProperties pass = process(js, false, false);
    Collection<AnalyzePrototypeProperties.NameInfo> allInfo = pass.getAllNameInfo();

    AnalyzePrototypeProperties.NameInfo fnInfo = findNameInfo(allInfo, "globalFn");
    Assert.assertNotNull(fnInfo);
    Assert.assertTrue(fnInfo.isReferenced());
    Assert.assertFalse(fnInfo.getDeclarations().isEmpty());

    AnalyzePrototypeProperties.NameInfo varInfo = findNameInfo(allInfo, "globalVarFn");
    Assert.assertNotNull(varInfo);
    Assert.assertTrue(varInfo.isReferenced());
  }

  @Test
  public void testAnchorUnusedVars() {
    String js = "function unusedGlobalFn() {}\n"
        + "var unusedGlobalVar = function() {};";
    AnalyzePrototypeProperties pass = process(js, false, true);
    Collection<AnalyzePrototypeProperties.NameInfo> allInfo = pass.getAllNameInfo();

    AnalyzePrototypeProperties.NameInfo fnInfo = findNameInfo(allInfo, "unusedGlobalFn");
    Assert.assertNotNull(fnInfo);
    Assert.assertTrue(fnInfo.isReferenced());

    AnalyzePrototypeProperties.NameInfo varInfo = findNameInfo(allInfo, "unusedGlobalVar");
    Assert.assertNotNull(varInfo);
    Assert.assertTrue(varInfo.isReferenced());
  }

  @Test
  public void testClosureVariableReading() {
    String js = "function outer() {\n"
        + "  var captured = 10;\n"
        + "  function Foo() {}\n"
        + "  Foo.prototype.method = function() { return captured; };\n"
        + "}\n"
        + "outer();";
    AnalyzePrototypeProperties pass = process(js, false, false);
    Collection<AnalyzePrototypeProperties.NameInfo> allInfo = pass.getAllNameInfo();

    AnalyzePrototypeProperties.NameInfo methodInfo = findNameInfo(allInfo, "method");
    Assert.assertNotNull(methodInfo);
    Assert.assertTrue(methodInfo.readsClosureVariables());
  }

  @Test
  public void testObjectLiteralPropertiesUsage() {
    String js = "var obj = { propA: 1, 'propB': 2 };\n"
        + "function test() { return obj.propA; }\n"
        + "test();";
    AnalyzePrototypeProperties pass = process(js, false, false);
    Collection<AnalyzePrototypeProperties.NameInfo> allInfo = pass.getAllNameInfo();

    AnalyzePrototypeProperties.NameInfo propA = findNameInfo(allInfo, "propA");
    Assert.assertNotNull(propA);
    Assert.assertTrue(propA.isReferenced());
  }

  @Test
  public void testModulesAndReferencePropagation() {
    Compiler compiler = new Compiler();
    JSModule m1 = new JSModule("m1");
    JSModule m2 = new JSModule("m2");
    m2.addDependency(m1);
    JSModuleGraph moduleGraph = new JSModuleGraph(new JSModule[]{m1, m2});

    Node externs = compiler.parseTestCode("");
    Node root = new Node(Token.BLOCK);
    Node file1 = compiler.parseTestCode("function Foo() {} Foo.prototype.m = function() { this.n(); };");
    Node file2 = compiler.parseTestCode("Foo.prototype.n = function() {}; var f = new Foo(); f.m();");
    root.addChildToBack(file1);
    root.addChildToBack(file2);

    AnalyzePrototypeProperties pass = new AnalyzePrototypeProperties(compiler, moduleGraph, false, false);
    pass.process(externs, root);

    Collection<AnalyzePrototypeProperties.NameInfo> allInfo = pass.getAllNameInfo();
    AnalyzePrototypeProperties.NameInfo nInfo = findNameInfo(allInfo, "n");
    Assert.assertNotNull(nInfo);
    Assert.assertTrue(nInfo.isReferenced());
  }

  @Test
  public void testExternProperties() {
    String externs = "var ExtObj; ExtObj.prototype.extProp;";
    String js = "function test() {}";
    AnalyzePrototypeProperties passWithoutMod = process(externs, js, false, false);
    AnalyzePrototypeProperties.NameInfo extInfo = findNameInfo(passWithoutMod.getAllNameInfo(), "extProp");
    Assert.assertNotNull(extInfo);
    Assert.assertTrue(extInfo.isReferenced());

    AnalyzePrototypeProperties passWithMod = process(externs, js, true, false);
    AnalyzePrototypeProperties.NameInfo extInfo2 = findNameInfo(passWithMod.getAllNameInfo(), "extProp");
    Assert.assertNull(extInfo2);
  }

  @Test
  public void testAssignmentPropertyRemove() {
    Compiler compiler = new Compiler();
    Node script = compiler.parseTestCode("function Foo() {} Foo.prototype.bar = function() {};");
    Node expr = script.getChildAtIndex(1);
    Assert.assertEquals(Token.EXPR_RESULT, expr.getType());

    AnalyzePrototypeProperties.AssignmentProperty prop = new AnalyzePrototypeProperties.AssignmentProperty(expr, null);
    Assert.assertNotNull(prop.getPrototype());
    Assert.assertNotNull(prop.getValue());
    Assert.assertNull(prop.getModule());

    prop.remove();
    Assert.assertEquals(1, script.getChildCount());
  }

  @Test
  public void testLiteralPropertyRemove() {
    Compiler compiler = new Compiler();
    Node script = compiler.parseTestCode("Foo.prototype = { key1: 1, key2: 2 };");
    Node expr = script.getFirstChild();
    Node assign = expr.getFirstChild();
    Node objLit = assign.getLastChild();
    Node key1 = objLit.getFirstChild();

    AnalyzePrototypeProperties.LiteralProperty litProp =
        new AnalyzePrototypeProperties.LiteralProperty(key1, key1.getFirstChild(), objLit, assign, null);
    Assert.assertNotNull(litProp.getPrototype());
    Assert.assertEquals(key1.getFirstChild(), litProp.getValue());
    Assert.assertNull(litProp.getModule());

    litProp.remove();
    Assert.assertEquals(1, objLit.getChildCount());
    Assert.assertEquals("key2", objLit.getFirstChild().getString());
  }

  @Test
  public void testGlobalFunctionSymbolRemoval() {
    Compiler compiler = new Compiler();
    Node script = compiler.parseTestCode("function f() {} var g = function() {}, h = function() {};");

    Node fNode = script.getFirstChild();
    AnalyzePrototypeProperties.GlobalFunction globalF =
        new AnalyzePrototypeProperties(compiler, null, false, false).new GlobalFunction(
            fNode.getFirstChild(), fNode, script, null);
    Assert.assertEquals(fNode, globalF.getFunctionNode());
    globalF.remove();
    Assert.assertEquals(1, script.getChildCount());

    Node varNode = script.getFirstChild();
    Node gName = varNode.getFirstChild();
    AnalyzePrototypeProperties.GlobalFunction globalG =
        new AnalyzePrototypeProperties(compiler, null, false, false).new GlobalFunction(
            gName, varNode, script, null);
    Assert.assertNull(globalG.getModule());
    globalG.remove();
    Assert.assertEquals(1, varNode.getChildCount());
    Assert.assertEquals("h", varNode.getFirstChild().getString());
  }

  @Test
  public void testNameInfoMarkReference() {
    AnalyzePrototypeProperties pass = new AnalyzePrototypeProperties(new Compiler(), null, false, false);
    AnalyzePrototypeProperties.NameInfo info = pass.new NameInfo("customProp");
    Assert.assertFalse(info.isReferenced());
    Assert.assertEquals("customProp", info.toString());

    boolean changed = info.markReference(null);
    Assert.assertTrue(changed);
    Assert.assertTrue(info.isReferenced());

    boolean changedAgain = info.markReference(null);
    Assert.assertFalse(changedAgain);
    Assert.assertNull(info.getDeepestCommonModuleRef());
  }

  @Test
  public void testNameInfoWithModuleGraphMarkReference() {
    JSModule m1 = new JSModule("m1");
    JSModule m2 = new JSModule("m2");
    m2.addDependency(m1);
    JSModuleGraph graph = new JSModuleGraph(new JSModule[]{m1, m2});

    AnalyzePrototypeProperties pass = new AnalyzePrototypeProperties(new Compiler(), graph, false, false);
    AnalyzePrototypeProperties.NameInfo info = pass.new NameInfo("modProp");
    Assert.assertTrue(info.markReference(m2));
    Assert.assertEquals(m2, info.getDeepestCommonModuleRef());

    Assert.assertTrue(info.markReference(m1));
    Assert.assertEquals(m1, info.getDeepestCommonModuleRef());

    Assert.assertFalse(info.markReference(m2));
  }
}