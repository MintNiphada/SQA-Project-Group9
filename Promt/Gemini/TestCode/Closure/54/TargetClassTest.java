package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.EnumType;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import org.junit.Before;
import org.junit.Test;

public class TypedScopeCreatorTest {

  private Compiler compiler;
  private JSTypeRegistry registry;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    registry = compiler.getTypeRegistry();
  }

  private Node parse(String js) {
    Node n = compiler.parseTestCode(js);
    assertEquals(0, compiler.getErrorCount());
    return n;
  }

  private Scope createGlobalScope(Node root) {
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    return creator.createScope(root, null);
  }

  @Test
  public void testInitialScopeCreation() {
    Node root = parse("");
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope scope = creator.createInitialScope(root);

    assertTrue(scope.isGlobal());
    assertNotNull(scope.getVar("Object"));
    assertNotNull(scope.getVar("Array"));
    assertNotNull(scope.getVar("Function"));
    assertNotNull(scope.getVar("Date"));
    assertNotNull(scope.getVar("RegExp"));
    assertNotNull(scope.getVar("String"));
    assertNotNull(scope.getVar("Number"));
    assertNotNull(scope.getVar("Boolean"));
    assertNotNull(scope.getVar("Error"));
    assertNotNull(scope.getVar("EvalError"));
    assertNotNull(scope.getVar("RangeError"));
    assertNotNull(scope.getVar("ReferenceError"));
    assertNotNull(scope.getVar("SyntaxError"));
    assertNotNull(scope.getVar("TypeError"));
    assertNotNull(scope.getVar("URIError"));
    assertNotNull(scope.getVar("undefined"));
    assertNotNull(scope.getVar("ActiveXObject"));
  }

  @Test
  public void testBasicVarDeclaration() {
    Node root = parse("var x = 1; var y = 'hello'; var z = true; var n = null; var u = undefined;");
    Scope scope = createGlobalScope(root);

    assertNotNull(scope.getVar("x"));
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), scope.getVar("x").getType());
    assertNotNull(scope.getVar("y"));
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), scope.getVar("y").getType());
    assertNotNull(scope.getVar("z"));
    assertEquals(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), scope.getVar("z").getType());
    assertNotNull(scope.getVar("n"));
    assertEquals(registry.getNativeType(JSTypeNative.NULL_TYPE), scope.getVar("n").getType());
    assertNotNull(scope.getVar("u"));
  }

  @Test
  public void testMultipleVarDeclaration() {
    Node root = parse("/** @type {number} */ var a = 1, b = 2;");
    Scope scope = createGlobalScope(root);

    assertEquals(1, compiler.getWarningCount());
    assertNotNull(scope.getVar("a"));
    assertNotNull(scope.getVar("b"));
  }

  @Test
  public void testFunctionDeclarationGlobalAndLocal() {
    Node root = parse("function foo(a, b) { var c = a; return c; }");
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    Scope.Var fooVar = globalScope.getVar("foo");
    assertNotNull(fooVar);
    assertTrue(fooVar.getType().isFunctionType());

    Node fnNode = root.getFirstChild();
    assertEquals(Token.FUNCTION, fnNode.getType());
    Scope localScope = creator.createScope(fnNode, globalScope);

    assertNotNull(localScope.getVar("a"));
    assertNotNull(localScope.getVar("b"));
    assertNotNull(localScope.getVar("c"));
    assertEquals(globalScope, localScope.getParent());
  }

  @Test
  public void testHoistedFunctionDeclaration() {
    Node root = parse("function bar() { foo(); function foo() {} }");
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    Node barFn = root.getFirstChild();
    Scope localScope = creator.createScope(barFn, globalScope);
    assertNotNull(localScope.getVar("foo"));
    assertTrue(localScope.getVar("foo").getType().isFunctionType());
  }

  @Test
  public void testBleedingFunctionName() {
    Node root = parse("var f = function myFunc(x) { return myFunc; };");
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    Node varNode = root.getFirstChild();
    Node nameNode = varNode.getFirstChild();
    Node fnNode = nameNode.getFirstChild();
    assertEquals(Token.FUNCTION, fnNode.getType());

    Scope localScope = creator.createScope(fnNode, globalScope);
    assertNotNull(localScope.getVar("myFunc"));
    assertNotNull(localScope.getVar("x"));
  }

  @Test
  public void testCatchParameter() {
    Node root = parse("function f() { try { } catch (e) { var x = e; } }");
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    Node fnNode = root.getFirstChild();
    Scope localScope = creator.createScope(fnNode, globalScope);

    assertNotNull(localScope.getVar("e"));
    assertNotNull(localScope.getVar("x"));
  }

  @Test
  public void testConstructorAndPrototypeDeclaration() {
    Node root = parse("/** @constructor */ function Person(name) { this.name = name; }\n"
        + "Person.prototype.sayHi = function() {};");
    Scope scope = createGlobalScope(root);

    Scope.Var personVar = scope.getVar("Person");
    assertNotNull(personVar);
    assertTrue(personVar.getType().isConstructor());

    Scope.Var protoVar = scope.getVar("Person.prototype");
    assertNotNull(protoVar);

    Scope.Var sayHiVar = scope.getVar("Person.prototype.sayHi");
    assertNotNull(sayHiVar);
  }

  @Test
  public void testInterfaceDeclaration() {
    Node root = parse("/** @interface */ function Disposable() {}\n"
        + "Disposable.prototype.dispose = function() {};");
    Scope scope = createGlobalScope(root);

    Scope.Var ifaceVar = scope.getVar("Disposable");
    assertNotNull(ifaceVar);
    assertTrue(ifaceVar.getType().isInterface());
  }

  @Test
  public void testUninitializedConstructorWarning() {
    Node root = parse("/** @constructor */ var Foo;");
    createGlobalScope(root);
    assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testUninitializedInterfaceWarning() {
    Node root = parse("/** @interface */ var IFoo;");
    createGlobalScope(root);
    assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testEnumTypeDeclaration() {
    Node root = parse("/** @enum {number} */ var Numbers = { ONE: 1, TWO: 2 };");
    Scope scope = createGlobalScope(root);

    Scope.Var enumVar = scope.getVar("Numbers");
    assertNotNull(enumVar);
    assertTrue(enumVar.getType() instanceof EnumType);

    EnumType enumType = (EnumType) enumVar.getType();
    assertTrue(enumType.hasOwnProperty("ONE"));
    assertTrue(enumType.hasOwnProperty("TWO"));
  }

  @Test
  public void testEnumDuplicateKeyWarning() {
    Node root = parse("/** @enum {number} */ var Dup = { ONE: 1, ONE: 2 };");
    createGlobalScope(root);
    assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testEnumInvalidInitializerWarning() {
    Node root = parse("/** @enum {number} */ var BadEnum = 5;");
    createGlobalScope(root);
    assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testTypedef() {
    Node root = parse("/** @typedef {{name: string, age: number}} */ var PersonRecord;\n"
        + "/** @type {PersonRecord} */ var p;");
    Scope scope = createGlobalScope(root);

    assertNotNull(registry.getType("PersonRecord"));
    Scope.Var pVar = scope.getVar("p");
    assertNotNull(pVar);
    assertEquals(registry.getType("PersonRecord"), pVar.getType());
  }

  @Test
  public void testLendsAnnotationValid() {
    Node root = parse("/** @constructor */ function Foo() {}\n"
        + "var obj = /** @lends {Foo.prototype} */ ({ bar: function() {} });");
    Scope scope = createGlobalScope(root);

    Scope.Var fooVar = scope.getVar("Foo");
    assertNotNull(fooVar);
    ObjectType instanceType = ((FunctionType) fooVar.getType()).getInstanceType();
    assertTrue(instanceType.hasProperty("bar"));
  }

  @Test
  public void testLendsAnnotationUnknownTarget() {
    Node root = parse("var obj = /** @lends {NonExistent.prototype} */ ({ bar: function() {} });");
    createGlobalScope(root);
    assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testLendsAnnotationNonObject() {
    Node root = parse("var num = 123;\n"
        + "var obj = /** @lends {num} */ ({ bar: function() {} });");
    createGlobalScope(root);
    assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testGoogInherits() {
    Node root = parse("var goog = {}; goog.inherits = function(child, parent) {};\n"
        + "/** @constructor */ function SuperClass() {}\n"
        + "/** @constructor \n * @extends {SuperClass} */ function SubClass() {}\n"
        + "goog.inherits(SubClass, SuperClass);");
    Scope scope = createGlobalScope(root);

    Scope.Var subClassVar = scope.getVar("SubClass");
    assertNotNull(subClassVar);
    FunctionType subClassType = (FunctionType) subClassVar.getType();
    assertEquals("SuperClass", subClassType.getSuperClassConstructor().getInstanceType().getReferenceName());
  }

  @Test
  public void testGoogAddSingletonGetter() {
    Node root = parse("var goog = {}; goog.addSingletonGetter = function(cls) {};\n"
        + "/** @constructor */ function MyService() {}\n"
        + "goog.addSingletonGetter(MyService);");
    Scope scope = createGlobalScope(root);

    Scope.Var serviceVar = scope.getVar("MyService");
    assertNotNull(serviceVar);
    ObjectType serviceType = ObjectType.cast(serviceVar.getType());
    assertNotNull(serviceType);
    assertTrue(serviceType.hasProperty("getInstance"));
  }

  @Test
  public void testObjectLiteralCast() {
    Node root = parse("var goog = {}; goog.reflect = {}; goog.reflect.object = function(type, obj) { return obj; };\n"
        + "/** @constructor */ function Foo() {}\n"
        + "var casted = goog.reflect.object(Foo, { prop: 1 });");
    Scope scope = createGlobalScope(root);

    Scope.Var castedVar = scope.getVar("casted");
    assertNotNull(castedVar);
  }

  @Test
  public void testObjectLiteralCastInvalidCtor() {
    Node root = parse("var goog = {}; goog.reflect = {}; goog.reflect.object = function(type, obj) { return obj; };\n"
        + "var casted = goog.reflect.object(NonExistentType, { prop: 1 });");
    createGlobalScope(root);
    assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testWindowConstructorGlobalThisRedefinition() {
    Node root = parse("/** @constructor */ function Window() {}\n"
        + "var w = new Window();");
    Scope scope = createGlobalScope(root);

    Scope.Var windowVar = scope.getVar("Window");
    assertNotNull(windowVar);
    assertTrue(windowVar.getType().isConstructor());
  }

  @Test
  public void testPatchGlobalScope() {
    Node script1 = parse("var a = 1; var b = 2;");
    script1.setInputId(new com.google.javascript.rhino.InputId("script1.js"));
    script1.setSourceName("script1.js");

    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(script1, null);

    assertTrue(globalScope.isDeclared("a", false));
    assertTrue(globalScope.isDeclared("b", false));

    Node script1Modified = parse("var a = 10; var c = 3;");
    script1Modified.setInputId(new com.google.javascript.rhino.InputId("script1.js"));
    script1Modified.setSourceName("script1.js");

    creator.patchGlobalScope(globalScope, script1Modified);

    assertTrue(globalScope.isDeclared("a", false));
    assertFalse(globalScope.isDeclared("b", false));
    assertTrue(globalScope.isDeclared("c", false));
  }

  @Test
  public void testCollectPropertiesOnThis() {
    Node root = parse("/** @constructor */ function Point(x, y) {\n"
        + "  /** @type {number} */ this.x = x;\n"
        + "  /** @type {number} */ this.y = y;\n"
        + "}");
    Scope scope = createGlobalScope(root);

    Scope.Var pointVar = scope.getVar("Point");
    assertNotNull(pointVar);
    FunctionType fnType = (FunctionType) pointVar.getType();
    ObjectType instanceType = fnType.getInstanceType();

    assertTrue(instanceType.hasProperty("x"));
    assertTrue(instanceType.hasProperty("y"));
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), instanceType.getPropertyType("x"));
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), instanceType.getPropertyType("y"));
  }

  @Test
  public void testConstantDeclarationInference() {
    Node root = parse("/** @const */ var MY_CONST = 42;\n"
        + "/** @const */ var MY_OBJ = { key: 'val' };");
    Scope scope = createGlobalScope(root);

    Scope.Var constVar = scope.getVar("MY_CONST");
    assertNotNull(constVar);
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), constVar.getType());

    Scope.Var constObjVar = scope.getVar("MY_OBJ");
    assertNotNull(constObjVar);
    assertNotNull(constObjVar.getType());
  }

  @Test
  public void testStubDeclarations() {
    Node root = parse("var ns = {};\n"
        + "ns.prop;\n"
        + "ns.method = function() {};");
    Scope scope = createGlobalScope(root);

    Scope.Var nsProp = scope.getVar("ns.prop");
    assertNotNull(nsProp);
    assertEquals(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), nsProp.getType());

    Scope.Var nsMethod = scope.getVar("ns.method");
    assertNotNull(nsMethod);
    assertTrue(nsMethod.getType().isFunctionType());
  }

  @Test
  public void testUndeclaredVariableRedeclarationWarning() {
    Node root = parse("var x = 1; var x = 2;");
    createGlobalScope(root);
    assertTrue(compiler.getWarningCount() > 0 || compiler.getErrorCount() > 0);
  }

  @Test
  public void testFunctionWithJsDocParams() {
    Node root = parse("/**\n"
        + " * @param {string} name\n"
        + " * @param {number=} opt_age\n"
        + " * @return {boolean}\n"
        + " */\n"
        + "function greet(name, opt_age) {\n"
        + "  return true;\n"
        + "}");
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    Node fnNode = root.getFirstChild();
    Scope localScope = creator.createScope(fnNode, globalScope);

    Scope.Var nameVar = localScope.getVar("name");
    assertNotNull(nameVar);
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), nameVar.getType());

    Scope.Var ageVar = localScope.getVar("opt_age");
    assertNotNull(ageVar);
  }

  @Test
  public void testAliasedConstructor() {
    Node root = parse("/** @constructor */ function Original() {}\n"
        + "/** @constructor */ var Alias = Original;\n"
        + "var a = new Alias();");
    Scope scope = createGlobalScope(root);

    Scope.Var aliasVar = scope.getVar("Alias");
    assertNotNull(aliasVar);
    assertTrue(aliasVar.getType().isConstructor());
  }

  @Test
  public void testDiagnosticTypesInitialized() {
    assertNotNull(TypedScopeCreator.MALFORMED_TYPEDEF);
    assertNotNull(TypedScopeCreator.ENUM_INITIALIZER);
    assertNotNull(TypedScopeCreator.CTOR_INITIALIZER);
    assertNotNull(TypedScopeCreator.IFACE_INITIALIZER);
    assertNotNull(TypedScopeCreator.CONSTRUCTOR_EXPECTED);
    assertNotNull(TypedScopeCreator.UNKNOWN_LENDS);
    assertNotNull(TypedScopeCreator.LENDS_ON_NON_OBJECT);
    assertNotNull(TypedScopeCreator.DELEGATE_PROXY_SUFFIX);
  }
}