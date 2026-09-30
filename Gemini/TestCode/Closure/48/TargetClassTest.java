package com.google.javascript.jscomp;

import com.google.javascript.jscomp.Scope.Var;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.EnumType;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.ObjectType;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

public class TypedScopeCreatorTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  private Scope createGlobalScope(String js) {
    return createGlobalScope("", js);
  }

  private Scope createGlobalScope(String externs, String js) {
    Node externsNode = compiler.parseTestCode(externs);
    Node mainNode = compiler.parseTestCode(js);
    Node root = new Node(Token.BLOCK, externsNode, mainNode);
    TypedScopeCreator tsc = new TypedScopeCreator(compiler);
    return tsc.createScope(root, null);
  }

  private Node findFunctionNode(Node node, String name) {
    if (node.isFunction()) {
      Node nameNode = node.getFirstChild();
      if (nameNode != null && name.equals(nameNode.getString())) {
        return node;
      }
    }
    for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
      Node fn = findFunctionNode(child, name);
      if (fn != null) {
        return fn;
      }
    }
    return null;
  }

  @Test
  public void testInitialScopeNativeTypes() {
    Scope scope = createGlobalScope("");
    assertNotNull(scope);
    assertTrue(scope.isGlobal());

    assertTrue(scope.isDeclared("Object", false));
    assertTrue(scope.isDeclared("Array", false));
    assertTrue(scope.isDeclared("Function", false));
    assertTrue(scope.isDeclared("Date", false));
    assertTrue(scope.isDeclared("RegExp", false));
    assertTrue(scope.isDeclared("String", false));
    assertTrue(scope.isDeclared("Number", false));
    assertTrue(scope.isDeclared("Boolean", false));
    assertTrue(scope.isDeclared("Error", false));
    assertTrue(scope.isDeclared("undefined", false));
    assertTrue(scope.isDeclared("ActiveXObject", false));

    Var objVar = scope.getVar("Object");
    assertNotNull(objVar);
    assertNotNull(objVar.getType());
    assertTrue(objVar.getType().isFunctionType());

    Var undefVar = scope.getVar("undefined");
    assertNotNull(undefVar);
    assertTrue(undefVar.getType().isVoidType());
  }

  @Test
  public void testSimpleVariableDeclaration() {
    Scope scope = createGlobalScope("var a = 10; var b = 'hello'; var c = true; var d = null; var e = void 0; var f = /abc/;");
    assertTrue(scope.isDeclared("a", false));
    assertTrue(scope.isDeclared("b", false));
    assertTrue(scope.isDeclared("c", false));
    assertTrue(scope.isDeclared("d", false));
    assertTrue(scope.isDeclared("e", false));
    assertTrue(scope.isDeclared("f", false));

    Var aVar = scope.getVar("a");
    assertNotNull(aVar);
    Var bVar = scope.getVar("b");
    assertNotNull(bVar);
    Var cVar = scope.getVar("c");
    assertNotNull(cVar);
  }

  @Test
  public void testTypedVariableDeclaration() {
    Scope scope = createGlobalScope("/** @type {number} */ var x = 5;");
    Var xVar = scope.getVar("x");
    assertNotNull(xVar);
    assertNotNull(xVar.getType());
    assertTrue(xVar.getType().isNumber());
    assertFalse(xVar.isTypeInferred());
  }

  @Test
  public void testFunctionDeclaration() {
    Scope scope = createGlobalScope("function foo(x, y) { return x + y; }");
    assertTrue(scope.isDeclared("foo", false));
    Var fooVar = scope.getVar("foo");
    assertNotNull(fooVar);
    assertNotNull(fooVar.getType());
    assertTrue(fooVar.getType().isFunctionType());
  }

  @Test
  public void testFunctionWithJSDoc() {
    Scope scope = createGlobalScope("/**\n"
        + " * @param {number} x\n"
        + " * @param {string} y\n"
        + " * @return {boolean}\n"
        + " */\n"
        + "function bar(x, y) { return true; }");
    Var barVar = scope.getVar("bar");
    assertNotNull(barVar);
    FunctionType fnType = barVar.getType().toMaybeFunctionType();
    assertNotNull(fnType);
    assertTrue(fnType.getReturnType().isBoolean());
  }

  @Test
  public void testConstructorAndPrototype() {
    Scope scope = createGlobalScope("/** @constructor */ function Person(name) { this.name = name; }");
    assertTrue(scope.isDeclared("Person", false));
    assertTrue(scope.isDeclared("Person.prototype", false));

    Var personVar = scope.getVar("Person");
    assertNotNull(personVar);
    assertTrue(personVar.getType().isConstructor());
  }

  @Test
  public void testInterfaceDeclaration() {
    Scope scope = createGlobalScope("/** @interface */ function Disposable() {}");
    assertTrue(scope.isDeclared("Disposable", false));
    Var dispVar = scope.getVar("Disposable");
    assertNotNull(dispVar);
    assertTrue(dispVar.getType().isInterface());
  }

  @Test
  public void testEnumTypeDeclaration() {
    Scope scope = createGlobalScope("/** @enum {number} */ var Colors = { RED: 1, GREEN: 2, BLUE: 3 };");
    assertTrue(scope.isDeclared("Colors", false));
    Var colorsVar = scope.getVar("Colors");
    assertNotNull(colorsVar);
    assertTrue(colorsVar.getType().isEnumType());
    EnumType enumType = (EnumType) colorsVar.getType();
    assertTrue(enumType.getElementsType().isNumber());
  }

  @Test
  public void testTypedefDeclaration() {
    Scope scope = createGlobalScope("/** @typedef {Array.<string>} */ var StringArray;");
    assertTrue(scope.isDeclared("StringArray", false));
    JSType type = compiler.getTypeRegistry().getType("StringArray");
    assertNotNull(type);
  }

  @Test
  public void testQualifiedNameAssignment() {
    Scope scope = createGlobalScope("var ns = {}; /** @type {number} */ ns.count = 0;");
    assertTrue(scope.isDeclared("ns", false));
    assertTrue(scope.isDeclared("ns.count", false));
    Var countVar = scope.getVar("ns.count");
    assertNotNull(countVar);
    assertTrue(countVar.getType().isNumber());
  }

  @Test
  public void testMethodDeclarationOnPrototype() {
    Scope scope = createGlobalScope(
        "/** @constructor */ function Greeter() {}\n"
        + "/** @param {string} name @return {string} */\n"
        + "Greeter.prototype.sayHello = function(name) { return 'Hello ' + name; };");
    assertTrue(scope.isDeclared("Greeter.prototype.sayHello", false));
    Var sayHelloVar = scope.getVar("Greeter.prototype.sayHello");
    assertNotNull(sayHelloVar);
    assertTrue(sayHelloVar.getType().isFunctionType());
  }

  @Test
  public void testLocalScopeCreation() {
    Node externsNode = compiler.parseTestCode("");
    Node mainNode = compiler.parseTestCode(
        "function outer(a) {\n"
        + "  var b = 2;\n"
        + "  function inner(c) {\n"
        + "    return a + b + c;\n"
        + "  }\n"
        + "  return inner;\n"
        + "}");
    Node root = new Node(Token.BLOCK, externsNode, mainNode);
    TypedScopeCreator tsc = new TypedScopeCreator(compiler);
    Scope globalScope = tsc.createScope(root, null);

    Node outerFn = findFunctionNode(mainNode, "outer");
    assertNotNull(outerFn);

    Scope localScope = tsc.createScope(outerFn, globalScope);
    assertNotNull(localScope);
    assertFalse(localScope.isGlobal());
    assertEquals(globalScope, localScope.getParent());
    assertTrue(localScope.isDeclared("a", false));
    assertTrue(localScope.isDeclared("b", false));
    assertTrue(localScope.isDeclared("inner", false));
  }

  @Test
  public void testCatchScope() {
    Scope scope = createGlobalScope("try { var x = 1; } catch (e) { var y = e; }");
    assertTrue(scope.isDeclared("x", false));
    assertTrue(scope.isDeclared("y", false));
  }

  @Test
  public void testObjectLiteralWithLends() {
    Scope scope = createGlobalScope(
        "/** @constructor */ function Foo() {}\n"
        + "var f = /** @lends {Foo.prototype} */ ({ bar: 123 });");
    assertTrue(scope.isDeclared("Foo", false));
  }

  @Test
  public void testMultipleVarDefWarning() {
    createGlobalScope("/** @type {number} */ var a = 1, b = 2;");
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeCheck.MULTIPLE_VAR_DEF.key, compiler.getWarnings()[0].getType().key);
  }

  @Test
  public void testEnumInitializerWarning() {
    createGlobalScope("/** @enum {number} */ var E = 123;");
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.ENUM_INITIALIZER.key, compiler.getWarnings()[0].getType().key);
  }

  @Test
  public void testConstructorInitializerWarning() {
    createGlobalScope("/** @constructor */ var Foo;");
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.CTOR_INITIALIZER.key, compiler.getWarnings()[0].getType().key);
  }

  @Test
  public void testInterfaceInitializerWarning() {
    createGlobalScope("/** @interface */ var Bar;");
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.IFACE_INITIALIZER.key, compiler.getWarnings()[0].getType().key);
  }

  @Test
  public void testPatchGlobalScope() {
    Node script1 = compiler.parseTestCode("var globalVar = 10; function testFn() {}");
    script1.setSourceName("script1.js");
    Node root = new Node(Token.BLOCK, compiler.parseTestCode(""), script1);

    TypedScopeCreator tsc = new TypedScopeCreator(compiler);
    Scope globalScope = tsc.createScope(root, null);
    assertTrue(globalScope.isDeclared("globalVar", false));
    assertTrue(globalScope.isDeclared("testFn", false));

    Node script1Modified = compiler.parseTestCode("var globalVar = 'updated';");
    script1Modified.setSourceName("script1.js");

    tsc.patchGlobalScope(globalScope, script1Modified);
    assertTrue(globalScope.isDeclared("globalVar", false));
    assertFalse(globalScope.isDeclared("testFn", false));
  }

  @Test
  public void testConstantOrIdiom() {
    Scope scope = createGlobalScope("var goog = goog || {};");
    assertTrue(scope.isDeclared("goog", false));
  }

  @Test
  public void testInheritanceHookup() {
    Scope scope = createGlobalScope(
        "var goog = {};\n"
        + "goog.inherits = function(child, parent) {};\n"
        + "/** @constructor */ function Super() {}\n"
        + "/** @constructor @extends {Super} */ function Sub() {}\n"
        + "goog.inherits(Sub, Super);");

    assertTrue(scope.isDeclared("Super", false));
    assertTrue(scope.isDeclared("Sub", false));
    Var subVar = scope.getVar("Sub");
    assertNotNull(subVar);
    FunctionType subCtor = subVar.getType().toMaybeFunctionType();
    assertNotNull(subCtor);
  }

  @Test
  public void testWindowConstructorUpdatesGlobalThis() {
    Scope scope = createGlobalScope(
        "/** @constructor */ function Window() {}\n"
        + "var x = 1;");
    assertTrue(scope.isDeclared("Window", false));
  }

  @Test
  public void testStubDeclarations() {
    Scope scope = createGlobalScope("var obj = {}; obj.foo;");
    assertTrue(scope.isDeclared("obj", false));
    assertTrue(scope.isDeclared("obj.foo", false));
    Var fooVar = scope.getVar("obj.foo");
    assertNotNull(fooVar);
    assertTrue(fooVar.isTypeInferred());
  }

  @Test
  public void testCodingConventionConstructor() {
    CodingConvention cc = new GoogleCodingConvention();
    TypedScopeCreator tsc = new TypedScopeCreator(compiler, cc);
    Node externsNode = compiler.parseTestCode("");
    Node mainNode = compiler.parseTestCode("var a = 1;");
    Node root = new Node(Token.BLOCK, externsNode, mainNode);
    Scope scope = tsc.createScope(root, null);
    assertNotNull(scope);
    assertTrue(scope.isDeclared("a", false));
  }
}