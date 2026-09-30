package com.google.javascript.jscomp;

import com.google.common.collect.Sets;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfoBuilder;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

class GlobalNamespaceTest {

  private Compiler createCompiler() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    return compiler;
  }

  private GlobalNamespace createNamespace(String js) {
    return createNamespace("", js);
  }

  private GlobalNamespace createNamespace(String externs, String js) {
    Compiler compiler = createCompiler();
    Node externsNode = compiler.parseTestCode(externs);
    Node rootNode = compiler.parseTestCode(js);
    return new GlobalNamespace(compiler, externsNode, rootNode);
  }

  @Test
  public void testBasicVarDeclaration() {
    GlobalNamespace gn = createNamespace("var a = 1;");
    Map<String, GlobalNamespace.Name> index = gn.getNameIndex();
    Assert.assertTrue(index.containsKey("a"));
    GlobalNamespace.Name a = index.get("a");
    Assert.assertEquals("a", a.name);
    Assert.assertEquals("a", a.fullName());
    Assert.assertTrue(a.isSimpleName());
    Assert.assertEquals(1, a.globalSets);
    Assert.assertEquals(0, a.localSets);
    Assert.assertEquals(0, a.totalGets);
    Assert.assertNotNull(a.declaration);
    Assert.assertEquals(GlobalNamespace.Name.Type.OTHER, a.type);
    Assert.assertFalse(a.needsToBeStubbed());
  }

  @Test
  public void testFunctionDeclaration() {
    GlobalNamespace gn = createNamespace("function foo() {} foo();");
    Map<String, GlobalNamespace.Name> index = gn.getNameIndex();
    Assert.assertTrue(index.containsKey("foo"));
    GlobalNamespace.Name foo = index.get("foo");
    Assert.assertEquals(GlobalNamespace.Name.Type.FUNCTION, foo.type);
    Assert.assertEquals(1, foo.globalSets);
    Assert.assertEquals(1, foo.totalGets);
    Assert.assertEquals(1, foo.callGets);
    Assert.assertEquals(0, foo.aliasingGets);
    Assert.assertTrue(foo.canCollapse());
    Assert.assertTrue(foo.canCollapseUnannotatedChildNames());
  }

  @Test
  public void testObjectLiteralKeyResolution() {
    String js = "var w = {x: {y: {z: 0}}};";
    GlobalNamespace gn = createNamespace(js);
    Map<String, GlobalNamespace.Name> index = gn.getNameIndex();
    Assert.assertTrue(index.containsKey("w"));
    Assert.assertTrue(index.containsKey("w.x"));
    Assert.assertTrue(index.containsKey("w.x.y"));
    Assert.assertTrue(index.containsKey("w.x.y.z"));

    GlobalNamespace.Name w = index.get("w");
    Assert.assertEquals(GlobalNamespace.Name.Type.OBJECTLIT, w.type);
    GlobalNamespace.Name wx = index.get("w.x");
    Assert.assertEquals("w.x", wx.fullName());
    Assert.assertEquals(w, wx.parent);
    Assert.assertFalse(wx.isSimpleName());
  }

  @Test
  public void testObjectLiteralAssign() {
    String js = "var a = {}; a.b = {c: 123};";
    GlobalNamespace gn = createNamespace(js);
    Map<String, GlobalNamespace.Name> index = gn.getNameIndex();
    Assert.assertTrue(index.containsKey("a"));
    Assert.assertTrue(index.containsKey("a.b"));
    Assert.assertTrue(index.containsKey("a.b.c"));
    Assert.assertEquals(GlobalNamespace.Name.Type.OTHER, index.get("a.b.c").type);
  }

  @Test
  public void testConstructorAndEnumDeclarations() {
    Compiler compiler = createCompiler();
    Node script = compiler.parseTestCode(
        "/** @constructor */ function Bar() {}\n" +
        "/** @enum {number} */ var MyEnum = { A: 1, B: 2 };"
    );

    // Build GlobalNamespace with JSDoc attached correctly
    GlobalNamespace gn = new GlobalNamespace(compiler, script);
    Map<String, GlobalNamespace.Name> index = gn.getNameIndex();

    GlobalNamespace.Name bar = index.get("Bar");
    Assert.assertNotNull(bar);

    GlobalNamespace.Name myEnum = index.get("MyEnum");
    Assert.assertNotNull(myEnum);
    Assert.assertTrue(index.containsKey("MyEnum.A"));
    Assert.assertTrue(index.containsKey("MyEnum.B"));
  }

  @Test
  public void testPrototypePrefixHandling() {
    String js = "function Foo() {} Foo.prototype.bar = function() {}; Foo.prototype.bar.baz = 1;";
    GlobalNamespace gn = createNamespace(js);
    Map<String, GlobalNamespace.Name> index = gn.getNameIndex();
    Assert.assertTrue(index.containsKey("Foo"));
    GlobalNamespace.Name foo = index.get("Foo");
    Assert.assertTrue(foo.totalGets > 0);
  }

  @Test
  public void testLocalSetAndNeedsStubbed() {
    String js = "var a; function f() { a = 1; }";
    GlobalNamespace gn = createNamespace(js);
    Map<String, GlobalNamespace.Name> index = gn.getNameIndex();
    GlobalNamespace.Name a = index.get("a");
    Assert.assertNotNull(a);
    Assert.assertEquals(1, a.globalSets);
    Assert.assertEquals(1, a.localSets);
  }

  @Test
  public void testNeedsToBeStubbed() {
    GlobalNamespace.Name name = new GlobalNamespace.Name("foo", null, false);
    Assert.assertFalse(name.needsToBeStubbed());
    name.localSets = 1;
    name.globalSets = 0;
    Assert.assertTrue(name.needsToBeStubbed());
    name.globalSets = 1;
    Assert.assertFalse(name.needsToBeStubbed());
  }

  @Test
  public void testGetTypesInExpressions() {
    String js =
        "var a = 1;\n" +
        "var b = a;\n" +             // ALIASING_GET
        "var c = !a;\n" +            // DIRECT_GET (Unary)
        "var d = typeof a;\n" +      // DIRECT_GET (TYPEOF)
        "var e = void a;\n" +        // DIRECT_GET (VOID)
        "var f = ~a;\n" +            // DIRECT_GET (BITNOT)
        "var g = +a;\n" +            // DIRECT_GET (POS)
        "var h = -a;\n" +            // DIRECT_GET (NEG)
        "if (a) {}\n" +              // DIRECT_GET (IF)
        "var i = new a();\n" +       // DIRECT_GET on target
        "var j = new Object(a);\n" + // ALIASING_GET as argument
        "var k = a || 2;\n" +        // boolean expr
        "var l = a ? 1 : 2;\n" +     // hook condition
        "var m = 1 ? a : 2;\n";      // hook value
    GlobalNamespace gn = createNamespace(js);
    Map<String, GlobalNamespace.Name> index = gn.getNameIndex();
    GlobalNamespace.Name a = index.get("a");
    Assert.assertNotNull(a);
    Assert.assertTrue(a.totalGets >= 10);
  }

  @Test
  public void testDetermineGetTypeForHookOrBooleanExpr() {
    String js =
        "var a = 1;\n" +
        "var x = a || 2;\n" +
        "a = a || {};\n" +
        "while (a && true) {}\n" +
        "for (; a || false;) {}\n";
    GlobalNamespace gn = createNamespace(js);
    Map<String, GlobalNamespace.Name> index = gn.getNameIndex();
    GlobalNamespace.Name a = index.get("a");
    Assert.assertNotNull(a);
    Assert.assertTrue(a.totalGets > 0);
  }

  @Test
  public void testNestedAssign() {
    String js = "var a = 0; var b = (a = 1);";
    GlobalNamespace gn = createNamespace(js);
    Map<String, GlobalNamespace.Name> index = gn.getNameIndex();
    GlobalNamespace.Name a = index.get("a");
    Assert.assertNotNull(a);
    Assert.assertTrue(a.aliasingGets > 0);
  }

  @Test
  public void testExternsHandling() {
    String externs = "var ext = {};";
    String js = "ext.foo = 1; var local = ext.foo;";
    GlobalNamespace gn = createNamespace(externs, js);
    Map<String, GlobalNamespace.Name> index = gn.getNameIndex();
    Assert.assertTrue(index.containsKey("ext.foo"));
    GlobalNamespace.Name extFoo = index.get("ext.foo");
    Assert.assertNotNull(extFoo);
  }

  @Test
  public void testNameForest() {
    String js = "var a = {}; a.b = 1; var c = 2;";
    GlobalNamespace gn = createNamespace(js);
    List<GlobalNamespace.Name> forest = gn.getNameForest();
    Assert.assertEquals(2, forest.size());
    Assert.assertTrue(forest.get(0).name.equals("a") || forest.get(0).name.equals("c"));
  }

  @Test
  public void testScanNewNodes() {
    Compiler compiler = createCompiler();
    Node root = compiler.parseTestCode("var a = 1;");
    GlobalNamespace gn = new GlobalNamespace(compiler, root);
    gn.getNameIndex(); // trigger initial build

    Node newCode = compiler.parseTestCode("a = 2; a.b = 3;");
    Set<Node> newNodes = Sets.newHashSet(newCode.getFirstChild(), newCode.getLastChild());
    Scope globalScope = new SyntacticScopeCreator(compiler).createScope(root, null);
    gn.scanNewNodes(globalScope, newNodes);

    GlobalNamespace.Name a = gn.getNameIndex().get("a");
    Assert.assertNotNull(a);
  }

  @Test
  public void testNameAddAndRemoveRef() {
    GlobalNamespace.Name name = new GlobalNamespace.Name("test", null, false);
    GlobalNamespace.Ref decl = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.SET_FROM_GLOBAL);
    name.addRef(decl);
    Assert.assertEquals(decl, name.declaration);
    Assert.assertEquals(1, name.globalSets);

    GlobalNamespace.Ref setLocal = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.SET_FROM_LOCAL);
    name.addRef(setLocal);
    Assert.assertEquals(1, name.localSets);

    GlobalNamespace.Ref directGet = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.DIRECT_GET);
    name.addRef(directGet);
    Assert.assertEquals(1, name.totalGets);

    GlobalNamespace.Ref aliasGet = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.ALIASING_GET);
    name.addRef(aliasGet);
    Assert.assertEquals(2, name.totalGets);
    Assert.assertEquals(1, name.aliasingGets);

    GlobalNamespace.Ref callGet = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.CALL_GET);
    name.addRef(callGet);
    Assert.assertEquals(3, name.totalGets);
    Assert.assertEquals(1, name.callGets);

    GlobalNamespace.Ref protoGet = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.PROTOTYPE_GET);
    name.addRef(protoGet);
    Assert.assertEquals(4, name.totalGets);

    // Remove refs
    name.removeRef(protoGet);
    Assert.assertEquals(3, name.totalGets);

    name.removeRef(callGet);
    Assert.assertEquals(2, name.totalGets);
    Assert.assertEquals(0, name.callGets);

    name.removeRef(aliasGet);
    Assert.assertEquals(1, name.totalGets);
    Assert.assertEquals(0, name.aliasingGets);

    name.removeRef(directGet);
    Assert.assertEquals(0, name.totalGets);

    name.removeRef(setLocal);
    Assert.assertEquals(0, name.localSets);

    // Test removing declaration and fallback
    GlobalNamespace.Ref secondDecl = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.SET_FROM_GLOBAL);
    name.addRef(secondDecl);
    Assert.assertEquals(2, name.globalSets);
    name.removeRef(decl);
    Assert.assertEquals(1, name.globalSets);
    Assert.assertEquals(secondDecl, name.declaration);

    name.removeRef(secondDecl);
    Assert.assertEquals(0, name.globalSets);
    Assert.assertNull(name.declaration);
  }

  @Test
  public void testNameCanEliminateAndCanCollapse() {
    GlobalNamespace.Name parent = new GlobalNamespace.Name("Parent", null, false);
    parent.type = GlobalNamespace.Name.Type.OBJECTLIT;
    parent.globalSets = 1;
    parent.localSets = 0;

    Assert.assertTrue(parent.canCollapseUnannotatedChildNames());
    Assert.assertTrue(parent.canCollapse());
    Assert.assertTrue(parent.canEliminate());

    GlobalNamespace.Name child = parent.addProperty("child", false);
    child.type = GlobalNamespace.Name.Type.OTHER;
    child.globalSets = 1;
    child.localSets = 0;

    Assert.assertTrue(child.canCollapse());
    Assert.assertTrue(parent.canEliminate());

    // In externs
    GlobalNamespace.Name externName = new GlobalNamespace.Name("Ext", null, true);
    Assert.assertFalse(externName.canCollapse());

    // Is class or enum
    parent.setIsClassOrEnum();
    Assert.assertTrue(parent.isNamespace());

    // toString verification
    String str = parent.toString();
    Assert.assertTrue(str.contains("Parent"));
    Assert.assertTrue(str.contains("OBJECTLIT"));
  }

  @Test
  public void testRefPropertiesAndTwins() {
    GlobalNamespace.Ref setRef = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.SET_FROM_GLOBAL);
    GlobalNamespace.Ref aliasRef = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.ALIASING_GET);

    Assert.assertTrue(setRef.isSet());
    Assert.assertFalse(aliasRef.isSet());

    GlobalNamespace.Ref.markTwins(setRef, aliasRef);
    Assert.assertEquals(aliasRef, setRef.getTwin());
    Assert.assertEquals(setRef, aliasRef.getTwin());

    GlobalNamespace.Ref cloned = setRef.cloneAndReclassify(GlobalNamespace.Ref.Type.SET_FROM_LOCAL);
    Assert.assertEquals(GlobalNamespace.Ref.Type.SET_FROM_LOCAL, cloned.type);
    Assert.assertTrue(cloned.isSet());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMarkTwinsInvalid() {
    GlobalNamespace.Ref get1 = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.DIRECT_GET);
    GlobalNamespace.Ref get2 = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.CALL_GET);
    GlobalNamespace.Ref.markTwins(get1, get2);
  }

  @Test
  public void testValueTypeBranches() {
    String js =
        "var a = {k: 1};\n" +
        "var b = function() {};\n" +
        "var c = false || function() {};\n" +
        "var d = true ? {x: 1} : function() {};\n" +
        "var e = true ? 1 : function() {};\n";
    GlobalNamespace gn = createNamespace(js);
    Map<String, GlobalNamespace.Name> index = gn.getNameIndex();
    Assert.assertEquals(GlobalNamespace.Name.Type.OBJECTLIT, index.get("a").type);
    Assert.assertEquals(GlobalNamespace.Name.Type.FUNCTION, index.get("b").type);
    Assert.assertEquals(GlobalNamespace.Name.Type.FUNCTION, index.get("c").type);
    Assert.assertEquals(GlobalNamespace.Name.Type.OBJECTLIT, index.get("d").type);
    Assert.assertEquals(GlobalNamespace.Name.Type.FUNCTION, index.get("e").type);
  }
}