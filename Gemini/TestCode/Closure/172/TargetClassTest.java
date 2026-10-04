package com.google.javascript.jscomp;

import com.google.javascript.rhino.InputId;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.EnumType;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TypedScopeCreatorTest {
  private Compiler compiler;
  private JSTypeRegistry registry;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT5);
    compiler.initOptions(options);
    registry = compiler.getTypeRegistry();
  }

  private Scope parseAndCreateScope(String js, String externs) {
    Node externsNode = compiler.parseTestCode(externs);
    Node rootNode = compiler.parseTestCode(js);
    Node block = new Node(Token.BLOCK, externsNode, rootNode);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    return creator.createScope(block, null);
  }

  private Scope parseAndCreateScope(String js) {
    return parseAndCreateScope(js, "");
  }

  @Test
  public void testGlobalScopeCreation() {
    Scope scope = parseAndCreateScope("var a = 1; function foo(b) { return b; }");
    Assert.assertNotNull(scope);
    Assert.assertTrue(scope.isGlobal());
    Assert.assertTrue(scope.isDeclared("a", false));
    Assert.assertTrue(scope.isDeclared("foo", false));
    Assert.assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), scope.getVar("a").getType());
    Assert.assertNotNull(scope.getVar("foo").getType());
  }

  @Test
  public void testNativeTypesDeclaredInInitialScope() {
    Scope scope = parseAndCreateScope("");
    Assert.assertTrue(scope.isDeclared("Object", false));
    Assert.assertTrue(scope.isDeclared("Array", false));
    Assert.assertTrue(scope.isDeclared("Function", false));
    Assert.assertTrue(scope.isDeclared("Date", false));
    Assert.assertTrue(scope.isDeclared("RegExp", false));
    Assert.assertTrue(scope.isDeclared("Error", false));
    Assert.assertTrue(scope.isDeclared("undefined", false));
  }

  @Test
  public void testLocalScopeCreation() {
    Node externsNode = compiler.parseTestCode("");
    Node rootNode = compiler.parseTestCode("function foo(x) { var y = 2; return x + y; }");
    Node block = new Node(Token.BLOCK, externsNode, rootNode);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(block, null);

    Node fnNode = rootNode.getFirstChild();
    Scope localScope = creator.createScope(fnNode.getLastChild(), globalScope);
    Assert.assertNotNull(localScope);
    Assert.assertFalse(localScope.isGlobal());
    Assert.assertEquals(globalScope, localScope.getParent());
  }

  @Test
  public void testLiteralTypeInference() {
    Scope scope = parseAndCreateScope(
        "var n = null; var u = void 0; var b = true; var s = 'str'; var num = 42; var r = /abc/;");
    Assert.assertEquals(registry.getNativeType(JSTypeNative.NULL_TYPE), scope.getVar("n").getType());
    Assert.assertEquals(registry.getNativeType(JSTypeNative.VOID_TYPE), scope.getVar("u").getType());
    Assert.assertEquals(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), scope.getVar("b").getType());
    Assert.assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), scope.getVar("s").getType());
    Assert.assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), scope.getVar("num").getType());
    Assert.assertEquals(registry.getNativeType(JSTypeNative.REGEXP_TYPE), scope.getVar("r").getType());
  }

  @Test
  public void testConstructorDefinition() {
    Scope scope = parseAndCreateScope(
        "/** @constructor */ function MyClass() { this.x = 1; }");
    Assert.assertTrue(scope.isDeclared("MyClass", false));
    Assert.assertTrue(scope.isDeclared("MyClass.prototype", false));
    JSType type = scope.getVar("MyClass").getType();
    Assert.assertTrue(type.isConstructor());
    FunctionType fnType = type.toMaybeFunctionType();
    Assert.assertNotNull(fnType);
    Assert.assertNotNull(fnType.getInstanceType());
  }

  @Test
  public void testInterfaceDefinition() {
    Scope scope = parseAndCreateScope(
        "/** @interface */ function MyInterface() {}");
    Assert.assertTrue(scope.isDeclared("MyInterface", false));
    JSType type = scope.getVar("MyInterface").getType();
    Assert.assertTrue(type.isInterface());
  }

  @Test
  public void testTypedefDeclaration() {
    Scope scope = parseAndCreateScope(
        "/** @typedef {number|string} */ var NumOrStr;");
    Assert.assertNotNull(registry.getType("NumOrStr"));
  }

  @Test
  public void testEnumDefinition() {
    Scope scope = parseAndCreateScope(
        "/** @enum {number} */ var Color = { RED: 1, GREEN: 2, BLUE: 3 };");
    Assert.assertTrue(scope.isDeclared("Color", false));
    JSType type = scope.getVar("Color").getType();
    Assert.assertTrue(type.isEnumType());
    EnumType enumType = (EnumType) type;
    Assert.assertTrue(enumType.getElements().contains("RED"));
    Assert.assertTrue(enumType.getElements().contains("GREEN"));
    Assert.assertTrue(enumType.getElements().contains("BLUE"));
  }

  @Test
  public void testStubDeclarations() {
    Scope scope = parseAndCreateScope("var ns = {}; ns.prop;");
    Assert.assertTrue(scope.isDeclared("ns", false));
    Assert.assertTrue(scope.isDeclared("ns.prop", false));
  }

  @Test
  public void testCatchScope() {
    Node externsNode = compiler.parseTestCode("");
    Node rootNode = compiler.parseTestCode("try { } catch (e) { var x = 1; }");
    Node block = new Node(Token.BLOCK, externsNode, rootNode);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope scope = creator.createScope(block, null);
    Assert.assertNotNull(scope);
  }

  @Test
  public void testLendsAnnotation() {
    Scope scope = parseAndCreateScope(
        "/** @constructor */ function Foo() {}"
        + "Foo.prototype = /** @lends {Foo.prototype} */ ({ bar: function() {} });");
    Assert.assertTrue(scope.isDeclared("Foo", false));
    Assert.assertNotNull(scope.getVar("Foo.prototype"));
  }

  @Test
  public void testUnknownLendsWarning() {
    parseAndCreateScope("var x = /** @lends {NonExistent} */ ({ a: 1 });");
    Assert.assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testLendsOnNonObjectWarning() {
    parseAndCreateScope("var num = 123; var x = /** @lends {num} */ ({ a: 1 });");
    Assert.assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testInheritanceSubclassCall() {
    Scope scope = parseAndCreateScope(
        "/** @constructor */ function Super() {}\n"
        + "/** @constructor */ function Sub() {}\n"
        + "goog.inherits(Sub, Super);",
        "var goog = {}; goog.inherits = function(child, parent) {};");
    Assert.assertTrue(scope.isDeclared("Super", false));
    Assert.assertTrue(scope.isDeclared("Sub", false));
  }

  @Test
  public void testSingletonGetter() {
    Scope scope = parseAndCreateScope(
        "/** @constructor */ function Single() {}\n"
        + "goog.addSingletonGetter(Single);",
        "var goog = {}; goog.addSingletonGetter = function(ctor) {};");
    Assert.assertTrue(scope.isDeclared("Single", false));
    JSType type = scope.getVar("Single").getType();
    Assert.assertTrue(type.isConstructor());
  }

  @Test
  public void testObjectLiteralCast() {
    parseAndCreateScope(
        "/** @constructor */ function TargetType() {}\n"
        + "goog.reflect.object(TargetType, { x: 1 });",
        "var goog = { reflect: {} }; goog.reflect.object = function(ctor, obj) {};");
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testPatchGlobalScope() {
    Node scriptRoot1 = compiler.parseTestCode("var a = 1;");
    scriptRoot1.setInputId(new InputId("script1.js"));
    scriptRoot1.setStaticSourceFile(new SimpleSourceFile("script1.js", false));
    Node externsNode = compiler.parseTestCode("");
    Node block = new Node(Token.BLOCK, externsNode, scriptRoot1);

    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(block, null);
    Assert.assertTrue(globalScope.isDeclared("a", false));

    Node scriptRoot2 = compiler.parseTestCode("var a = 'updated'; var b = 2;");
    scriptRoot2.setInputId(new InputId("script1.js"));
    scriptRoot2.setStaticSourceFile(new SimpleSourceFile("script1.js", false));

    creator.patchGlobalScope(globalScope, scriptRoot2);
    Assert.assertTrue(globalScope.isDeclared("a", false));
    Assert.assertTrue(globalScope.isDeclared("b", false));
    Assert.assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), globalScope.getVar("a").getType());
  }

  @Test
  public void testDelegateRelationship() {
    Scope scope = parseAndCreateScope(
        "/** @constructor */ function DelegateSuper() {}\n"
        + "/** @constructor */ function DelegateBase() {}\n"
        + "/** @constructor */ function Delegator() {}\n"
        + "goog.exportSymbol('DelegateBase', DelegateBase);\n",
        "var goog = {}; goog.exportSymbol = function(a, b) {};");
    Assert.assertNotNull(scope);
  }

  @Test
  public void testConstantDeclarationInference() {
    Scope scope = parseAndCreateScope("/** @const */ var CONST_VAL = 100;");
    Assert.assertTrue(scope.isDeclared("CONST_VAL", false));
    Assert.assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), scope.getVar("CONST_VAL").getType());
  }

  @Test
  public void testFunctionWithThisTypeProperties() {
    Scope scope = parseAndCreateScope(
        "/** @constructor */ function Person() { /** @type {string} */ this.name = 'John'; }");
    Assert.assertTrue(scope.isDeclared("Person", false));
    FunctionType fn = scope.getVar("Person").getType().toMaybeFunctionType();
    Assert.assertNotNull(fn);
    ObjectType instanceType = fn.getInstanceType();
    Assert.assertTrue(instanceType.hasProperty("name"));
  }

  @Test
  public void testQualifiedNameInference() {
    Scope scope = parseAndCreateScope(
        "var ns = {};\n"
        + "/** @type {number} */ ns.count = 0;");
    Assert.assertTrue(scope.isDeclared("ns.count", false));
    Assert.assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), scope.getVar("ns.count").getType());
  }

  @Test
  public void testHoistedFunctionDeclaration() {
    Scope scope = parseAndCreateScope(
        "function outer() { foo(); function foo() {} }");
    Assert.assertTrue(scope.isDeclared("outer", false));
  }

  @Test
  public void testDelegateProxyPrototypeProperty() {
    Scope scope = parseAndCreateScope(
        "/** @constructor */ function Base() {}\n"
        + "/** @type {Base} */ var b;");
    Assert.assertNotNull(scope);
  }
}
