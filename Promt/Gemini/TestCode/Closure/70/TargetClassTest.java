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

public class TypedScopeCreatorTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT5);
    compiler.initOptions(options);
  }

  private Scope buildGlobalScope(String js) {
    Node root = compiler.parseTestCode(js);
    assertEquals(0, compiler.getErrorCount());
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    return creator.createScope(root, null);
  }

  private Node findFirstNode(Node n, int tokenType) {
    if (n.getType() == tokenType) {
      return n;
    }
    for (Node child = n.getFirstChild(); child != null; child = child.getNext()) {
      Node result = findFirstNode(child, tokenType);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  @Test
  public void testInitialScopeNativeTypes() {
    Scope scope = buildGlobalScope("");
    assertTrue(scope.isGlobal());

    assertNotNull(scope.getVar("Object"));
    assertNotNull(scope.getVar("Array"));
    assertNotNull(scope.getVar("Function"));
    assertNotNull(scope.getVar("Date"));
    assertNotNull(scope.getVar("RegExp"));
    assertNotNull(scope.getVar("Error"));
    assertNotNull(scope.getVar("undefined"));
    assertNotNull(scope.getVar("ActiveXObject"));
    assertNotNull(scope.getVar("goog.typedef"));

    JSType undefinedType = scope.getVar("undefined").getType();
    assertNotNull(undefinedType);
    assertTrue(undefinedType.isVoidType());
  }

  @Test
  public void testLiteralTypes() {
    String js = "var n = null;\n"
        + "var v = void 0;\n"
        + "var s = 'hello';\n"
        + "var num = 42;\n"
        + "var bTrue = true;\n"
        + "var bFalse = false;\n"
        + "var r = /test/;\n"
        + "var obj = {};\n";

    Scope scope = buildGlobalScope(js);

    assertTrue(scope.getVar("n").getType().isNullType());
    assertTrue(scope.getVar("v").getType().isVoidType());
    assertTrue(scope.getVar("s").getType().isString());
    assertTrue(scope.getVar("num").getType().isNumber());
    assertTrue(scope.getVar("bTrue").getType().isBooleanValueType());
    assertTrue(scope.getVar("bFalse").getType().isBooleanValueType());
    assertTrue(scope.getVar("r").getType().isObject());
    assertNotNull(scope.getVar("obj").getType());
    assertTrue(scope.getVar("obj").getType().isObject());
  }

  @Test
  public void testFunctionDeclarations() {
    String js = "function foo(x, y) { return x; }\n"
        + "var bar = function(z) { return z; };\n";

    Scope scope = buildGlobalScope(js);

    Scope.Var fooVar = scope.getVar("foo");
    assertNotNull(fooVar);
    assertTrue(fooVar.getType().isFunctionType());
    assertFalse(fooVar.isTypeInferred());

    Scope.Var barVar = scope.getVar("bar");
    assertNotNull(barVar);
    assertTrue(barVar.getType().isFunctionType());
  }

  @Test
  public void testLocalScopeCreation() {
    String js = "function outer(a, b) {\n"
        + "  var x = 10;\n"
        + "  function inner(c) {\n"
        + "    var y = 'test';\n"
        + "    return y;\n"
        + "  }\n"
        + "  return inner(x);\n"
        + "}\n";

    Node root = compiler.parseTestCode(js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    Node outerFnNode = findFirstNode(root, Token.FUNCTION);
    assertNotNull(outerFnNode);

    Scope outerScope = creator.createScope(outerFnNode, globalScope);
    assertTrue(outerScope.isLocal());
    assertNotNull(outerScope.getVar("a"));
    assertNotNull(outerScope.getVar("b"));
    assertNotNull(outerScope.getVar("x"));
    assertNotNull(outerScope.getVar("inner"));

    Node innerFnNode = findFirstNode(outerFnNode.getLastChild(), Token.FUNCTION);
    assertNotNull(innerFnNode);

    Scope innerScope = creator.createScope(innerFnNode, outerScope);
    assertTrue(innerScope.isLocal());
    assertNotNull(innerScope.getVar("c"));
    assertNotNull(innerScope.getVar("y"));
  }

  @Test
  public void testCatchParameter() {
    String js = "function testCatch() {\n"
        + "  try {\n"
        + "    throw 'err';\n"
        + "  } catch (e) {\n"
        + "    var z = e;\n"
        + "  }\n"
        + "}\n";

    Node root = compiler.parseTestCode(js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    Node fnNode = findFirstNode(root, Token.FUNCTION);
    Scope localScope = creator.createScope(fnNode, globalScope);

    assertNotNull(localScope.getVar("e"));
    assertNotNull(localScope.getVar("z"));
  }

  @Test
  public void testConstructorAndInterfaceDeclarations() {
    String js = "/** @constructor */\n"
        + "function MyClass() {\n"
        + "  /** @type {number} */\n"
        + "  this.foo = 1;\n"
        + "}\n"
        + "/** @interface */\n"
        + "function MyInterface() {}\n";

    Scope scope = buildGlobalScope(js);

    Scope.Var classVar = scope.getVar("MyClass");
    assertNotNull(classVar);
    assertTrue(classVar.getType().isConstructor());

    Scope.Var ifaceVar = scope.getVar("MyInterface");
    assertNotNull(ifaceVar);
    assertTrue(ifaceVar.getType().isInterface());

    assertNotNull(scope.getVar("MyClass.prototype"));
    assertNotNull(scope.getVar("MyInterface.prototype"));
  }

  @Test
  public void testEnumDeclaration() {
    String js = "/** @enum {string} */\n"
        + "var Color = {\n"
        + "  RED: 'red',\n"
        + "  GREEN: 'green',\n"
        + "  BLUE: 'blue'\n"
        + "};\n";

    Scope scope = buildGlobalScope(js);

    Scope.Var enumVar = scope.getVar("Color");
    assertNotNull(enumVar);
    assertTrue(enumVar.getType().isEnumType());

    EnumType enumType = (EnumType) enumVar.getType();
    assertTrue(enumType.getElementsType().isString());
    assertTrue(enumType.hasOwnProperty("RED"));
    assertTrue(enumType.hasOwnProperty("GREEN"));
    assertTrue(enumType.hasOwnProperty("BLUE"));
  }

  @Test
  public void testEnumInitializerWarning() {
    String js = "/** @enum {number} */\n"
        + "var BadEnum = 42;\n";

    Node root = compiler.parseTestCode(js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    creator.createScope(root, null);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.ENUM_INITIALIZER.key,
        compiler.getWarnings()[0].getType().key);
  }

  @Test
  public void testConstructorInitializerWarning() {
    String js = "/** @constructor */\n"
        + "var MyCtor;\n";

    Node root = compiler.parseTestCode(js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    creator.createScope(root, null);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.CTOR_INITIALIZER.key,
        compiler.getWarnings()[0].getType().key);
  }

  @Test
  public void testInterfaceInitializerWarning() {
    String js = "/** @interface */\n"
        + "var MyIface;\n";

    Node root = compiler.parseTestCode(js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    creator.createScope(root, null);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.IFACE_INITIALIZER.key,
        compiler.getWarnings()[0].getType().key);
  }

  @Test
  public void testMultipleVarDefWarning() {
    String js = "/** @type {number} */\n"
        + "var a = 1, b = 2;\n";

    Node root = compiler.parseTestCode(js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    creator.createScope(root, null);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeCheck.MULTIPLE_VAR_DEF.key,
        compiler.getWarnings()[0].getType().key);
  }

  @Test
  public void testTypedefDeclaration() {
    String js = "/** @typedef {(string|number)} */\n"
        + "var MyType;\n"
        + "/** @type {MyType} */\n"
        + "var val = 'test';\n";

    Scope scope = buildGlobalScope(js);
    JSType registryType = compiler.getTypeRegistry().getType("MyType");
    assertNotNull(registryType);
    assertTrue(registryType.isUnionType());
  }

  @Test
  public void testLendsAnnotation() {
    String js = "/** @constructor */\n"
        + "function Person() {}\n"
        + "var methods = /** @lends {Person.prototype} */ ({\n"
        + "  sayHello: function() {}\n"
        + "});\n";

    Scope scope = buildGlobalScope(js);
    assertNotNull(scope.getVar("Person"));
    assertNotNull(scope.getVar("methods"));
  }

  @Test
  public void testLendsUnknownWarning() {
    String js = "var methods = /** @lends {NonExistent.prototype} */ ({\n"
        + "  foo: function() {}\n"
        + "});\n";

    Node root = compiler.parseTestCode(js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    creator.createScope(root, null);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.UNKNOWN_LENDS.key,
        compiler.getWarnings()[0].getType().key);
  }

  @Test
  public void testLendsOnNonObjectWarning() {
    String js = "var notAnObj = 123;\n"
        + "var methods = /** @lends {notAnObj} */ ({\n"
        + "  foo: function() {}\n"
        + "});\n";

    Node root = compiler.parseTestCode(js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    creator.createScope(root, null);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.LENDS_ON_NON_OBJECT.key,
        compiler.getWarnings()[0].getType().key);
  }

  @Test
  public void testGlobalThisWindowRedefinition() {
    String js = "/** @constructor */\n"
        + "function Window() {}\n";

    Scope scope = buildGlobalScope(js);
    Scope.Var windowVar = scope.getVar("Window");
    assertNotNull(windowVar);
    assertTrue(windowVar.getType().isConstructor());

    ObjectType globalThis = compiler.getTypeRegistry()
        .getNativeObjectType(JSTypeNative.GLOBAL_THIS);
    assertNotNull(globalThis);
  }

  @Test
  public void testQualifiedNameAssignments() {
    String js = "var ns = {};\n"
        + "/** @type {number} */\n"
        + "ns.count = 10;\n"
        + "/** @param {string} msg */\n"
        + "ns.log = function(msg) {};\n";

    Scope scope = buildGlobalScope(js);
    assertNotNull(scope.getVar("ns"));
    assertNotNull(scope.getVar("ns.count"));
    assertNotNull(scope.getVar("ns.log"));
    assertTrue(scope.getVar("ns.count").getType().isNumber());
    assertTrue(scope.getVar("ns.log").getType().isFunctionType());
  }

  @Test
  public void testStubDeclarations() {
    String js = "var ns = {};\n"
        + "ns.stubProp;\n";

    Scope scope = buildGlobalScope(js);
    assertNotNull(scope.getVar("ns"));
    assertNotNull(scope.getVar("ns.stubProp"));
    assertTrue(scope.getVar("ns.stubProp").getType().isUnknownType());
  }

  @Test
  public void testObjectLitKeysWithTypes() {
    String js = "var obj = {\n"
        + "  /** @type {number} */\n"
        + "  x: 1,\n"
        + "  /** @type {string} */\n"
        + "  y: 'hello'\n"
        + "};\n";

    Scope scope = buildGlobalScope(js);
    assertNotNull(scope.getVar("obj"));
    assertNotNull(scope.getVar("obj.x"));
    assertNotNull(scope.getVar("obj.y"));
    assertTrue(scope.getVar("obj.x").getType().isNumber());
    assertTrue(scope.getVar("obj.y").getType().isString());
  }

  @Test
  public void testPrototypePropertyRedefinition() {
    String js = "function Foo() {}\n"
        + "Foo.prototype = { bar: function() {} };\n";

    Scope scope = buildGlobalScope(js);
    assertNotNull(scope.getVar("Foo"));
    assertNotNull(scope.getVar("Foo.prototype"));
  }

  @Test
  public void testFunctionWithThisTypeProperties() {
    String js = "/** @constructor */\n"
        + "function Widget() {\n"
        + "  /** @type {string} */\n"
        + "  this.name = 'default';\n"
        + "}\n";

    Scope scope = buildGlobalScope(js);
    FunctionType widgetType = (FunctionType) scope.getVar("Widget").getType();
    ObjectType instanceType = widgetType.getInstanceType();
    assertTrue(instanceType.hasProperty("name"));
    assertTrue(instanceType.getPropertyType("name").isString());
  }

  @Test
  public void testBleedingFunctionExpression() {
    String js = "var f = function myNamedFn() {\n"
        + "  return myNamedFn;\n"
        + "};\n";

    Node root = compiler.parseTestCode(js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    assertNotNull(globalScope.getVar("f"));
    assertNull(globalScope.getVar("myNamedFn"));

    Node fnNode = findFirstNode(root, Token.FUNCTION);
    Scope localScope = creator.createScope(fnNode, globalScope);
    assertNotNull(localScope.getVar("myNamedFn"));
  }

  @Test
  public void testConstantDeclarationInference() {
    String js = "/** @const */ var MY_CONST = 123;\n"
        + "/** @const */ var MY_STR = 'abc';\n";

    Scope scope = buildGlobalScope(js);
    Scope.Var constVar = scope.getVar("MY_CONST");
    assertNotNull(constVar);
    assertTrue(constVar.getType().isNumber());

    Scope.Var strVar = scope.getVar("MY_STR");
    assertNotNull(strVar);
    assertTrue(strVar.getType().isString());
  }
}