package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.EnumType;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.ObjectType;

import org.junit.Before;
import org.junit.Test;

public class TargetClassTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCodingConvention(new ClosureCodingConvention());
    compiler.initOptions(options);
  }

  private Scope createGlobalScope(String js) {
    return createGlobalScope("", js);
  }

  private Scope createGlobalScope(String externsJs, String js) {
    Node externsNode = compiler.parseTestCode(externsJs);
    externsNode.setIsSyntheticBlock(true);
    Node mainNode = compiler.parseTestCode(js);
    mainNode.setIsSyntheticBlock(true);
    Node root = new Node(Token.BLOCK, externsNode, mainNode);

    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    return creator.createScope(root, null);
  }

  @Test
  public void testInitialGlobalScopeNativeBindings() {
    Scope scope = createGlobalScope("");
    assertNotNull(scope);
    assertTrue(scope.isGlobal());

    assertNotNull(scope.getVar("Object"));
    assertNotNull(scope.getVar("Array"));
    assertNotNull(scope.getVar("Boolean"));
    assertNotNull(scope.getVar("Date"));
    assertNotNull(scope.getVar("Error"));
    assertNotNull(scope.getVar("EvalError"));
    assertNotNull(scope.getVar("Function"));
    assertNotNull(scope.getVar("Number"));
    assertNotNull(scope.getVar("RangeError"));
    assertNotNull(scope.getVar("ReferenceError"));
    assertNotNull(scope.getVar("RegExp"));
    assertNotNull(scope.getVar("String"));
    assertNotNull(scope.getVar("SyntaxError"));
    assertNotNull(scope.getVar("TypeError"));
    assertNotNull(scope.getVar("URIError"));
    assertNotNull(scope.getVar("undefined"));
    assertNotNull(scope.getVar("ActiveXObject"));
  }

  @Test
  public void testVarDeclarationsAndInferredTypes() {
    String js = "var a = 1;\n" +
                "var b = 'hello';\n" +
                "var c = true;\n" +
                "var d = null;\n" +
                "var e = void 0;\n" +
                "var f = /abc/;\n" +
                "var g = {};";
    Scope scope = createGlobalScope(js);

    assertNotNull(scope.getVar("a"));
    assertNotNull(scope.getVar("b"));
    assertNotNull(scope.getVar("c"));
    assertNotNull(scope.getVar("d"));
    assertNotNull(scope.getVar("e"));
    assertNotNull(scope.getVar("f"));
    assertNotNull(scope.getVar("g"));

    assertEquals("number", scope.getVar("a").getType().toString());
    assertEquals("string", scope.getVar("b").getType().toString());
    assertEquals("boolean", scope.getVar("c").getType().toString());
    assertEquals("null", scope.getVar("d").getType().toString());
    assertEquals("undefined", scope.getVar("e").getType().toString());
    assertEquals("RegExp", scope.getVar("f").getType().toString());
  }

  @Test
  public void testExplicitJSDocVarTypes() {
    String js = "/** @type {number} */ var x = 10;\n" +
                "/** @type {string} */ var y;\n" +
                "/** @type {?Object} */ var z = null;";
    Scope scope = createGlobalScope(js);

    assertEquals("number", scope.getVar("x").getType().toString());
    assertFalse(scope.getVar("x").isTypeInferred());

    assertEquals("string", scope.getVar("y").getType().toString());
    assertFalse(scope.getVar("y").isTypeInferred());

    assertTrue(scope.getVar("z").getType().isNullable());
  }

  @Test
  public void testConstructorAndPrototypeDeclaration() {
    String js = "/** @constructor */ function Foo() { this.x = 1; }\n" +
                "Foo.prototype.bar = function() { return this.x; };\n" +
                "var f = new Foo();";
    Scope scope = createGlobalScope(js);

    Scope.Var fooVar = scope.getVar("Foo");
    assertNotNull(fooVar);
    assertTrue(fooVar.getType().isConstructor());

    Scope.Var protoVar = scope.getVar("Foo.prototype");
    assertNotNull(protoVar);

    FunctionType fooCtor = fooVar.getType().toMaybeFunctionType();
    ObjectType instanceType = fooCtor.getInstanceType();
    assertNotNull(instanceType);
  }

  @Test
  public void testInterfaceDeclaration() {
    String js = "/** @interface */ function AnInterface() {}\n" +
                "AnInterface.prototype.doSomething = function() {};";
    Scope scope = createGlobalScope(js);

    Scope.Var ifaceVar = scope.getVar("AnInterface");
    assertNotNull(ifaceVar);
    assertTrue(ifaceVar.getType().isInterface());
  }

  @Test
  public void testEnumDeclaration() {
    String js = "/** @enum {number} */ var MyEnum = { A: 1, B: 2 };";
    Scope scope = createGlobalScope(js);

    Scope.Var enumVar = scope.getVar("MyEnum");
    assertNotNull(enumVar);
    assertTrue(enumVar.getType().isEnumType());

    EnumType enumType = (EnumType) enumVar.getType();
    assertEquals("number", enumType.getElementsType().toString());
    assertTrue(enumType.hasOwnProperty("A"));
    assertTrue(enumType.hasOwnProperty("B"));
  }

  @Test
  public void testEnumAliasing() {
    String js = "/** @enum {string} */ var EnumA = { X: 'x' };\n" +
                "/** @enum {string} */ var EnumB = EnumA;";
    Scope scope = createGlobalScope(js);

    Scope.Var enumB = scope.getVar("EnumB");
    assertNotNull(enumB);
    assertTrue(enumB.getType().isEnumType());
  }

  @Test
  public void testTypedefDeclaration() {
    String js = "/** @typedef {{x: number, y: string}} */ var Point;\n" +
                "/** @type {Point} */ var pt;";
    Scope scope = createGlobalScope(js);

    Scope.Var ptVar = scope.getVar("pt");
    assertNotNull(ptVar);
    assertNotNull(ptVar.getType());
    assertTrue(ptVar.getType().isRecordType());
  }

  @Test
  public void testFunctionScopingAndParameters() {
    String js = "/**\n" +
                " * @param {number} a\n" +
                " * @param {string} b\n" +
                " * @return {boolean}\n" +
                " */\n" +
                "function testFn(a, b) {\n" +
                "  var innerVar = a + 1;\n" +
                "  return innerVar > 0;\n" +
                "}";

    Node externsNode = compiler.parseTestCode("");
    Node mainNode = compiler.parseTestCode(js);
    Node root = new Node(Token.BLOCK, externsNode, mainNode);

    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    Node fnNode = mainNode.getFirstChild();
    Scope localScope = creator.createScope(fnNode, globalScope);

    assertNotNull(localScope);
    assertTrue(localScope.isLocal());

    Scope.Var aVar = localScope.getVar("a");
    assertNotNull(aVar);
    assertEquals("number", aVar.getType().toString());

    Scope.Var bVar = localScope.getVar("b");
    assertNotNull(bVar);
    assertEquals("string", bVar.getType().toString());

    Scope.Var inner = localScope.getVar("innerVar");
    assertNotNull(inner);
  }

  @Test
  public void testCatchScope() {
    String js = "function f() {\n" +
                "  try {\n" +
                "  } catch (err) {\n" +
                "    var local = err;\n" +
                "  }\n" +
                "}";

    Node externsNode = compiler.parseTestCode("");
    Node mainNode = compiler.parseTestCode(js);
    Node root = new Node(Token.BLOCK, externsNode, mainNode);

    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    Node fnNode = mainNode.getFirstChild();
    Scope localScope = creator.createScope(fnNode, globalScope);
    assertNotNull(localScope.getVar("err"));
    assertNotNull(localScope.getVar("local"));
  }

  @Test
  public void testLendsAnnotation() {
    String js = "/** @constructor */ function Person() {}\n" +
                "/** @lends {Person.prototype} */\n" +
                "var methods = {\n" +
                "  /** @return {string} */\n" +
                "  getName: function() { return ''; }\n" +
                "};";
    Scope scope = createGlobalScope(js);
    Scope.Var personVar = scope.getVar("Person");
    assertNotNull(personVar);
  }

  @Test
  public void testInheritanceSubclassRelationship() {
    String js = "/** @constructor */ function SuperClass() {}\n" +
                "/** @constructor \n @extends {SuperClass} */ function SubClass() {}\n" +
                "goog.inherits(SubClass, SuperClass);";
    Scope scope = createGlobalScope(js);

    Scope.Var subVar = scope.getVar("SubClass");
    assertNotNull(subVar);
    FunctionType subCtor = subVar.getType().toMaybeFunctionType();
    assertNotNull(subCtor.getSuperClassConstructor());
  }

  @Test
  public void testAddSingletonGetter() {
    String js = "/** @constructor */ function SingletonFoo() {}\n" +
                "goog.addSingletonGetter(SingletonFoo);";
    Scope scope = createGlobalScope(js);

    Scope.Var fooVar = scope.getVar("SingletonFoo");
    assertNotNull(fooVar);
    ObjectType objType = fooVar.getType().toMaybeFunctionType().getInstanceType();
    assertNotNull(objType);
  }

  @Test
  public void testConstantWithOrIdiom() {
    String js = "var ns = {};\n" +
                "/** @const */ ns.MY_CONST = ns.MY_CONST || 42;";
    Scope scope = createGlobalScope(js);

    Scope.Var constVar = scope.getVar("ns.MY_CONST");
    assertNotNull(constVar);
    assertEquals("number", constVar.getType().toString());
  }

  @Test
  public void testMultipleVarDefWarning() {
    String js = "/** @type {number} */ var a = 1, b = 2;";
    createGlobalScope(js);
    assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testStubDeclarations() {
    String js = "var ns = {};\n" +
                "ns.stubProp;\n" +
                "/** @constructor */ function Foo() {}\n" +
                "Foo.prototype.stubMethod;";
    Scope scope = createGlobalScope(js);

    assertNotNull(scope.getVar("ns.stubProp"));
    assertNotNull(scope.getVar("Foo.prototype.stubMethod"));
  }

  @Test
  public void testGlobalWindowConstructor() {
    String js = "/** @constructor */ function Window() {}";
    Scope scope = createGlobalScope(js);
    Scope.Var windowVar = scope.getVar("Window");
    assertNotNull(windowVar);
    assertTrue(windowVar.getType().isConstructor());
  }

  @Test
  public void testPatchGlobalScope() {
    String js1 = "var a = 10; var b = 'test';";
    Node externsNode = compiler.parseTestCode("");
    externsNode.setIsSyntheticBlock(true);
    Node scriptNode = compiler.parseTestCode(js1);
    scriptNode.setIsSyntheticBlock(true);
    scriptNode.setSourceFileForTesting("test.js");
    Node root = new Node(Token.BLOCK, externsNode, scriptNode);

    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);
    assertNotNull(globalScope.getVar("a"));
    assertNotNull(globalScope.getVar("b"));

    String js2 = "var a = 20; var c = true;";
    Node newScriptNode = compiler.parseTestCode(js2);
    newScriptNode.setIsSyntheticBlock(true);
    newScriptNode.setSourceFileForTesting("test.js");

    creator.patchGlobalScope(globalScope, newScriptNode);

    assertNotNull(globalScope.getVar("a"));
    assertNull(globalScope.getVar("b"));
    assertNotNull(globalScope.getVar("c"));
  }

  @Test
  public void testHoistedFunctionDeclaration() {
    String js = "function test() {\n" +
                "  return hoisted();\n" +
                "  function hoisted() { return 123; }\n" +
                "}";
    Scope scope = createGlobalScope(js);
    assertNotNull(scope.getVar("test"));
  }

  @Test
  public void testBleedingFunctionName() {
    String js = "var myFn = function innerName(x) {\n" +
                "  return innerName(x - 1);\n" +
                "};";

    Node externsNode = compiler.parseTestCode("");
    Node mainNode = compiler.parseTestCode(js);
    Node root = new Node(Token.BLOCK, externsNode, mainNode);

    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    Node varNode = mainNode.getFirstChild();
    Node nameNode = varNode.getFirstChild();
    Node fnNode = nameNode.getFirstChild();

    Scope localScope = creator.createScope(fnNode, globalScope);
    assertNotNull(localScope.getVar("innerName"));
    assertNotNull(localScope.getVar("x"));
  }

  @Test
  public void testMalformedTypedefReport() {
    String js = "/** @typedef */ var BadTypedef;";
    createGlobalScope(js);
    assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testLendsOnNonObjectReport() {
    String js = "var num = 42;\n" +
                "var obj = /** @lends {num} */ { a: 1 };";
    createGlobalScope(js);
    assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testUnknownLendsReport() {
    String js = "var obj = /** @lends {NonExistentClass} */ { a: 1 };";
    createGlobalScope(js);
    assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testObjectLiteralCast() {
    String js = "/** @constructor */ function CastTarget() {}\n" +
                "goog.reflect.object(CastTarget, { foo: 'bar' });";
    Scope scope = createGlobalScope(js);
    assertNotNull(scope.getVar("CastTarget"));
  }

  @Test
  public void testDelegateRelationship() {
    String js = "/** @constructor */ function SuperDelegate() {}\n" +
                "/** @constructor */ function BaseDelegate() {}\n" +
                "/** @constructor */ function Delegator() {}\n" +
                "goog.enableProvide();";
    Scope scope = createGlobalScope(js);
    assertNotNull(scope);
  }
}