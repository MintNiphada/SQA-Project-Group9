package com.google.javascript.jscomp;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfoBuilder;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.StaticSourceFile;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GlobalNamespaceTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
  }

  private GlobalNamespace createNamespace(String js) {
    Node root = compiler.parseTestCode(js);
    return new GlobalNamespace(compiler, root);
  }

  private GlobalNamespace createNamespace(String externsJs, String js) {
    Node externsRoot = compiler.parseTestCode(externsJs);
    Node root = compiler.parseTestCode(js);
    return new GlobalNamespace(compiler, externsRoot, root);
  }

  @Test
  public void testBasicNamespaceCreation() {
    String js = "var a = 1; var b = {}; b.c = function() {};";
    GlobalNamespace gn = createNamespace(js);

    Assert.assertFalse(gn.hasExternsRoot());
    Assert.assertNull(gn.getParentScope());
    Assert.assertNotNull(gn.getTypeOfThis());

    Map<String, GlobalNamespace.Name> nameIndex = gn.getNameIndex();
    Assert.assertTrue(nameIndex.containsKey("a"));
    Assert.assertTrue(nameIndex.containsKey("b"));
    Assert.assertTrue(nameIndex.containsKey("b.c"));

    GlobalNamespace.Name aName = gn.getOwnSlot("a");
    Assert.assertNotNull(aName);
    Assert.assertEquals("a", aName.getBaseName());
    Assert.assertEquals("a", aName.getName());
    Assert.assertEquals("a", aName.getFullName());
    Assert.assertTrue(aName.isSimpleName());
    Assert.assertFalse(aName.isTypeInferred());
    Assert.assertNull(aName.getType());

    GlobalNamespace.Name bcName = gn.getSlot("b.c");
    Assert.assertNotNull(bcName);
    Assert.assertEquals("c", bcName.getBaseName());
    Assert.assertEquals("b.c", bcName.getFullName());
    Assert.assertFalse(bcName.isSimpleName());
    Assert.assertEquals(gn, gn.getScope(bcName));

    Iterable<GlobalNamespace.Ref> refs = gn.getReferences(aName);
    Assert.assertNotNull(refs);

    Iterable<GlobalNamespace.Name> allSymbols = gn.getAllSymbols();
    Assert.assertNotNull(allSymbols);

    List<GlobalNamespace.Name> forest = gn.getNameForest();
    Assert.assertNotNull(forest);
    Assert.assertTrue(forest.size() >= 2);
  }

  @Test
  public void testWithExterns() {
    String externs = "var extVar = {}; extVar.prop = 1;";
    String js = "var myVar = extVar.prop + 2;";
    GlobalNamespace gn = createNamespace(externs, js);

    Assert.assertTrue(gn.hasExternsRoot());
    Map<String, GlobalNamespace.Name> nameIndex = gn.getNameIndex();
    Assert.assertTrue(nameIndex.containsKey("extVar"));
    Assert.assertTrue(nameIndex.containsKey("extVar.prop"));
    Assert.assertTrue(nameIndex.containsKey("myVar"));

    GlobalNamespace.Name extVarName = gn.getSlot("extVar");
    Assert.assertNotNull(extVarName);
    Assert.assertTrue(extVarName.inExterns);
  }

  @Test
  public void testObjectLitKeys() {
    String js = "var obj = { x: 1, get y() { return 2; }, set y(val) {}, 'z': 3 };";
    GlobalNamespace gn = createNamespace(js);

    Map<String, GlobalNamespace.Name> nameIndex = gn.getNameIndex();
    Assert.assertTrue(nameIndex.containsKey("obj"));
    Assert.assertTrue(nameIndex.containsKey("obj.x"));
    Assert.assertTrue(nameIndex.containsKey("obj.y"));
    Assert.assertTrue(nameIndex.containsKey("obj.z"));

    GlobalNamespace.Name objY = nameIndex.get("obj.y");
    Assert.assertTrue(objY.isGetOrSetDefinition());
  }

  @Test
  public void testNestedObjectLiterals() {
    String js = "var root = { child: { leaf: 42 } };";
    GlobalNamespace gn = createNamespace(js);

    Map<String, GlobalNamespace.Name> nameIndex = gn.getNameIndex();
    Assert.assertTrue(nameIndex.containsKey("root"));
    Assert.assertTrue(nameIndex.containsKey("root.child"));
    Assert.assertTrue(nameIndex.containsKey("root.child.leaf"));
  }

  @Test
  public void testAssignedObjectLiterals() {
    String js = "var ns = {}; ns.sub = { key: 10 };";
    GlobalNamespace gn = createNamespace(js);

    Map<String, GlobalNamespace.Name> nameIndex = gn.getNameIndex();
    Assert.assertTrue(nameIndex.containsKey("ns.sub.key"));
  }

  @Test
  public void testVariousAssignmentsAndOps() {
    String js = "var a = 0; a++; a--; a += 5; a = a || 1; a = a ? a : 2;";
    GlobalNamespace gn = createNamespace(js);

    GlobalNamespace.Name aName = gn.getSlot("a");
    Assert.assertNotNull(aName);
    Assert.assertTrue(aName.globalSets > 1);
  }

  @Test
  public void testFunctionDeclarationsAndCalls() {
    String js = "function foo(x) { return x; } foo(1); new foo(2);";
    GlobalNamespace gn = createNamespace(js);

    GlobalNamespace.Name fooName = gn.getSlot("foo");
    Assert.assertNotNull(fooName);
    Assert.assertEquals(GlobalNamespace.Name.Type.FUNCTION, fooName.type);
    Assert.assertTrue(fooName.callGets > 0);
    Assert.assertTrue(fooName.totalGets > 0);
  }

  @Test
  public void testLocalScopeSets() {
    String js = "var g = 1; function f() { g = 2; var local = 3; }";
    GlobalNamespace gn = createNamespace(js);

    GlobalNamespace.Name gName = gn.getSlot("g");
    Assert.assertNotNull(gName);
    Assert.assertEquals(1, gName.globalSets);
    Assert.assertEquals(1, gName.localSets);
    Assert.assertNull(gn.getSlot("local"));
  }

  @Test
  public void testPrototypeHandling() {
    String js = "function Foo() {} Foo.prototype.bar = function() {}; Foo.prototype.bar.baz = 1;";
    GlobalNamespace gn = createNamespace(js);

    GlobalNamespace.Name fooName = gn.getSlot("Foo");
    Assert.assertNotNull(fooName);
    Assert.assertTrue(fooName.totalGets > 0);
  }

  @Test
  public void testAliasingAndTwinRefs() {
    String js = "var a = 1; var b = a = 2;";
    GlobalNamespace gn = createNamespace(js);

    GlobalNamespace.Name aName = gn.getSlot("a");
    Assert.assertNotNull(aName);
    boolean hasTwin = false;
    for (GlobalNamespace.Ref ref : aName.getRefs()) {
      if (ref.getTwin() != null) {
        hasTwin = true;
        break;
      }
    }
    Assert.assertTrue(hasTwin);
  }

  @Test
  public void testDeleteProp() {
    String js = "var obj = { a: 1 }; delete obj.a;";
    GlobalNamespace gn = createNamespace(js);

    GlobalNamespace.Name propName = gn.getSlot("obj.a");
    if (propName != null) {
      Assert.assertTrue(propName.deleteProps > 0);
      Assert.assertFalse(propName.canCollapse());
    }
  }

  @Test
  public void testScanNewNodes() {
    String js = "var a = 1;";
    Node root = compiler.parseTestCode(js);
    GlobalNamespace gn = new GlobalNamespace(compiler, root);
    gn.getNameIndex();

    Node newVar = new Node(Token.VAR, Node.newString(Token.NAME, "newVar"));
    Scope globalScope = new SyntacticScopeCreator(compiler).createScope(root, null);
    GlobalNamespace.AstChange change = new GlobalNamespace.AstChange(null, globalScope, newVar.getFirstChild());
    gn.scanNewNodes(Collections.singletonList(change));

    Assert.assertNotNull(gn.getSlot("newVar"));
  }

  @Test
  public void testNamePropertiesAndMethods() {
    GlobalNamespace.Name parent = new GlobalNamespace.Name("parent", null, false);
    GlobalNamespace.Name child = parent.addProperty("child", false);

    Assert.assertEquals("parent", parent.getBaseName());
    Assert.assertEquals("parent", parent.getFullName());
    Assert.assertEquals(parent, child.parent);
    Assert.assertEquals("child", child.getBaseName());
    Assert.assertEquals("parent.child", child.getFullName());
    Assert.assertFalse(child.isSimpleName());
    Assert.assertTrue(parent.isSimpleName());

    Assert.assertFalse(parent.isDeclaredType());
    child.setDeclaredType();
    Assert.assertTrue(child.isDeclaredType());
    parent.type = GlobalNamespace.Name.Type.OBJECTLIT;
    Assert.assertTrue(parent.isNamespace());

    Assert.assertNotNull(parent.toString());
  }

  @Test
  public void testNameAddAndRemoveRef() {
    GlobalNamespace.Name name = new GlobalNamespace.Name("test", null, false);
    GlobalNamespace.Ref refGlobalSet = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.SET_FROM_GLOBAL);
    GlobalNamespace.Ref refLocalSet = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.SET_FROM_LOCAL);
    GlobalNamespace.Ref refDirectGet = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.DIRECT_GET);
    GlobalNamespace.Ref refAliasGet = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.ALIASING_GET);
    GlobalNamespace.Ref refCallGet = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.CALL_GET);
    GlobalNamespace.Ref refDelete = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.DELETE_PROP);
    GlobalNamespace.Ref refProtoGet = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.PROTOTYPE_GET);

    name.addRef(refGlobalSet);
    name.addRef(refLocalSet);
    name.addRef(refDirectGet);
    name.addRef(refAliasGet);
    name.addRef(refCallGet);
    name.addRef(refDelete);
    name.addRef(refProtoGet);

    Assert.assertEquals(1, name.globalSets);
    Assert.assertEquals(1, name.localSets);
    Assert.assertEquals(1, name.aliasingGets);
    Assert.assertEquals(1, name.callGets);
    Assert.assertEquals(1, name.deleteProps);
    Assert.assertEquals(4, name.totalGets);
    Assert.assertEquals(refGlobalSet, name.getDeclaration());

    name.removeRef(refGlobalSet);
    Assert.assertEquals(0, name.globalSets);
    Assert.assertNull(name.getDeclaration());

    name.removeRef(refLocalSet);
    Assert.assertEquals(0, name.localSets);

    name.removeRef(refDirectGet);
    name.removeRef(refAliasGet);
    name.removeRef(refCallGet);
    name.removeRef(refProtoGet);
    name.removeRef(refDelete);

    Assert.assertEquals(0, name.totalGets);
    Assert.assertEquals(0, name.aliasingGets);
    Assert.assertEquals(0, name.callGets);
    Assert.assertEquals(0, name.deleteProps);
  }

  @Test
  public void testNameCanCollapseAndEliminate() {
    GlobalNamespace.Name name = new GlobalNamespace.Name("target", null, false);
    name.type = GlobalNamespace.Name.Type.OBJECTLIT;
    GlobalNamespace.Ref decl = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.SET_FROM_GLOBAL);
    name.addRef(decl);

    Assert.assertTrue(name.canCollapse());
    Assert.assertTrue(name.canCollapseUnannotatedChildNames());
    Assert.assertTrue(name.canEliminate());

    name.addRef(GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.DIRECT_GET));
    Assert.assertFalse(name.canEliminate());

    name.addRef(GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.ALIASING_GET));
    Assert.assertTrue(name.shouldKeepKeys());
    Assert.assertFalse(name.canCollapseUnannotatedChildNames());
  }

  @Test
  public void testNeedsToBeStubbed() {
    GlobalNamespace.Name name = new GlobalNamespace.Name("stubMe", null, false);
    Assert.assertFalse(name.needsToBeStubbed());
    name.addRef(GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.SET_FROM_LOCAL));
    Assert.assertTrue(name.needsToBeStubbed());
  }

  @Test
  public void testRefPropertiesAndTwin() {
    GlobalNamespace.Ref refSet = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.SET_FROM_GLOBAL);
    GlobalNamespace.Ref refAlias = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.ALIASING_GET);

    Assert.assertTrue(refSet.isSet());
    Assert.assertFalse(refAlias.isSet());
    Assert.assertEquals("", refSet.getSourceName());

    GlobalNamespace.Ref.markTwins(refSet, refAlias);
    Assert.assertEquals(refAlias, refSet.getTwin());
    Assert.assertEquals(refSet, refAlias.getTwin());

    GlobalNamespace.Ref cloned = refSet.cloneAndReclassify(GlobalNamespace.Ref.Type.SET_FROM_LOCAL);
    Assert.assertEquals(GlobalNamespace.Ref.Type.SET_FROM_LOCAL, cloned.type);
  }

  @Test
  public void testTrackerPass() {
    String js = "var trackedSymbol = 1;";
    Node root = compiler.parseTestCode(js);
    Node externs = compiler.parseTestCode("");

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(out);

    GlobalNamespace.Tracker tracker = new GlobalNamespace.Tracker(
        compiler, ps, Predicates.equalTo("trackedSymbol"));

    tracker.process(externs, root);
    String output = out.toString();
    Assert.assertTrue(output.contains("trackedSymbol: Added by"));

    out.reset();
    Node emptyRoot = compiler.parseTestCode("");
    tracker.process(externs, emptyRoot);
    String removedOutput = out.toString();
    Assert.assertTrue(removedOutput.contains("trackedSymbol: Removed by"));
  }

  @Test
  public void testJSDocInfoExtraction() {
    String js = "/** @constructor */ function MyClass() {}";
    GlobalNamespace gn = createNamespace(js);
    GlobalNamespace.Name name = gn.getSlot("MyClass");
    Assert.assertNotNull(name);
    Assert.assertTrue(name.isDeclaredType());
    Assert.assertNotNull(name.getJSDocInfo());
  }

  @Test
  public void testHookAndBooleanGetDeterminations() {
    String js = "var a = 1; var b = 2; var c = a || b; var d = a ? b : 0; if (a && b) {}";
    GlobalNamespace gn = createNamespace(js);

    GlobalNamespace.Name aName = gn.getSlot("a");
    Assert.assertNotNull(aName);
    Assert.assertTrue(aName.totalGets > 0);
  }

  @Test
  public void testSimpleStubDeclaration() {
    String js = "var ns; ns.stub;";
    GlobalNamespace gn = createNamespace(js);

    GlobalNamespace.Name stubName = gn.getSlot("ns.stub");
    if (stubName != null) {
      Assert.assertTrue(stubName.isSimpleStubDeclaration());
    }
  }
}