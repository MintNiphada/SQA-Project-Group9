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
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import org.junit.Before;
import org.junit.Test;

public class TypedScopeCreatorTest {

  private Compiler compiler;
  private TypedScopeCreator scopeCreator;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    scopeCreator = new TypedScopeCreator(compiler);
  }

  private Scope buildGlobalScope(String js) {
    Node root = compiler.parseTestCode(js);
    assertEquals(0, compiler.getErrorCount());
    return scopeCreator.createScope(root, null);
  }

  private Scope buildLocalScope(Node fnNode, Scope parentScope) {
    return scopeCreator.createScope(fnNode, parentScope);
  }

  @Test
  public void testInitialScopeCreation() {
    Node root = new Node(Token.BLOCK);
    Scope scope = scopeCreator.createInitialScope(root);
    assertNotNull(scope);
    assertTrue(scope.isGlobal());
    assertNotNull(scope.getVar("Object"));
    assertNotNull(scope.getVar("Function"));
    assertNotNull(scope.getVar("Array"));
    assertNotNull(scope.getVar("String"));
    assertNotNull(scope.getVar("Number"));
    assertNotNull(scope.getVar("Boolean"));
    assertNotNull(scope.getVar("Date"));
    assertNotNull(scope.getVar("RegExp"));
    assertNotNull(scope.getVar("Error"));
    assertNotNull(scope.getVar("undefined"));
    assertNotNull(scope.getVar("goog.typedef"));
    assertNotNull(scope.getVar("ActiveXObject"));
  }

  @Test
  public void testCustomCodingConvention() {
    CodingConvention customConvention = new GoogleCodingConvention();
    TypedScopeCreator customCreator = new TypedScopeCreator(compiler, customConvention);
    Node root = compiler.parseTestCode("var x = 1;");
    Scope scope = customCreator.createScope(root, null);
    assertNotNull(scope);
    assertNotNull(scope.getVar("x"));
  }

  @Test
  public void testVarDeclarations() {
    String js = "var a = 1; var b = 'str', c = true; var d = null, e = void 0, f = /abc/;";
    Scope scope = buildGlobalScope(js);
    assertTrue(scope.isDeclared("a", false));
    assertTrue(scope.isDeclared("b", false));
    assertTrue(scope.isDeclared("c", false));
    assertTrue(scope.isDeclared("d", false));
    assertTrue(scope.isDeclared("e", false));
    assertTrue(scope.isDeclared("f", false));
  }

  @Test
  public void testLiteralTypesAttachment() {
    String js = "var a = 10, b = 'hello', c = false, d = null, e = void 0, f = /test/, g = {};";
    Scope scope = buildGlobalScope(js);
    assertNotNull(scope.getVar("a"));
    assertNotNull(scope.getVar("b"));
    assertNotNull(scope.getVar("c"));
    assertNotNull(scope.getVar("d"));
    assertNotNull(scope.getVar("e"));
    assertNotNull(scope.getVar("f"));
    assertNotNull(scope.getVar("g"));
  }

  @Test
  public void testFunctionDeclarations() {
    String js = "function foo(x, y) { return x + y; }";
    Scope scope = buildGlobalScope(js);
    assertTrue(scope.isDeclared("foo", false));
    Scope.Var fooVar = scope.getVar("foo");
    assertNotNull(fooVar);
    assertTrue(fooVar.getType() instanceof FunctionType);
  }

  @Test
  public void testConstructorAndPrototypeDeclaration() {
    String js = "/** @constructor */ function Foo() {} Foo.prototype.bar = function() {};";
    Scope scope = buildGlobalScope(js);
    assertTrue(scope.isDeclared("Foo", false));
    assertTrue(scope.isDeclared("Foo.prototype", false));
    assertTrue(scope.isDeclared("Foo.prototype.bar", false));
  }

  @Test
  public void testLocalScopeCreation() {
    String js = "function parent(a) { var b = 2; function child(c) { var d = 4; return a + b + c + d; } return child; }";
    Node root = compiler.parseTestCode(js);
    Scope globalScope = scopeCreator.createScope(root, null);
    
    Node fnNode = root.getFirstChild();
    assertEquals(Token.FUNCTION, fnNode.getType());
    
    Scope localScope = buildLocalScope(fnNode, globalScope);
    assertTrue(localScope.isLocal());
    assertEquals(globalScope, localScope.getParent());
    assertTrue(localScope.isDeclared("a", false));
    assertTrue(localScope.isDeclared("b", false));
    assertTrue(localScope.isDeclared("child", false));
    assertFalse(localScope.isDeclared("d", false));
  }

  @Test
  public void testCatchScopeDeclaration() {
    String js = "function testCatch() { try { var x = 1; } catch (err) { var y = err; } }";
    Node root = compiler.parseTestCode(js);
    Scope globalScope = scopeCreator.createScope(root, null);
    Node fnNode = root.getFirstChild();
    Scope localScope = buildLocalScope(fnNode, globalScope);
    assertNotNull(localScope);
  }

  @Test
  public void testEnumDeclaration() {
    String js = "/** @enum {number} */ var MyEnum = { A: 1, B: 2 };";
    Scope scope = buildGlobalScope(js);
    assertTrue(scope.isDeclared("MyEnum", false));
    Scope.Var enumVar = scope.getVar("MyEnum");
    assertNotNull(enumVar);
    assertTrue(enumVar.getType() instanceof EnumType);
    EnumType enumType = (EnumType) enumVar.getType();
    assertTrue(enumType.hasOwnProperty("A"));
    assertTrue(enumType.hasOwnProperty("B"));
  }

  @Test
  public void testEnumDuplicateKeyWarning() {
    String js = "/** @enum {number} */ var DupEnum = { A: 1, A: 2 };";
    Node root = compiler.parseTestCode(js);
    scopeCreator.createScope(root, null);
    assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testEnumNonConstantKeyWarning() {
    String js = "/** @enum {number} */ var BadEnum = { valid: 1 };";
    Node root = compiler.parseTestCode(js);
    scopeCreator.createScope(root, null);
    assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testInvalidEnumInitializerWarning() {
    String js = "/** @enum {number} */ var InvalidEnum = 123;";
    Node root = compiler.parseTestCode(js);
    scopeCreator.createScope(root, null);
    assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testTypedefDeclaration() {
    String js = "/** @typedef {(string|number)} */ var MyType;";
    Scope scope = buildGlobalScope(js);
    assertTrue(scope.isDeclared("MyType", false));
    JSType registered = compiler.getTypeRegistry().getType("MyType");
    assertNotNull(registered);
  }

  @Test
  public void testQualifiedTypedefDeclaration() {
    String js = "var ns = {}; /** @typedef {boolean} */ ns.BoolType;";
    Scope scope = buildGlobalScope(js);
    assertTrue(scope.isDeclared("ns", false));
    JSType registered = compiler.getTypeRegistry().getType("ns.BoolType");
    assertNotNull(registered);
  }

  @Test
  public void testMalformedTypedefWarning() {
    String js = "/** @typedef */ var BadTypeDef;";
    Node root = compiler.parseTestCode(js);
    scopeCreator.createScope(root, null);
    assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testInheritanceAndOverriddenFunction() {
    String js = "/** @constructor */ function SuperClass() {}\n"
        + "SuperClass.prototype.foo = function(x) {};\n"
        + "/** @constructor @extends {SuperClass} */ function SubClass() {}\n"
        + "SubClass.prototype.foo = function(x) {};";
    Scope scope = buildGlobalScope(js);
    assertTrue(scope.isDeclared("SuperClass", false));
    assertTrue(scope.isDeclared("SubClass", false));
    assertTrue(scope.isDeclared("SubClass.prototype.foo", false));
  }

  @Test
  public void testInheritanceViaInherits() {
    String js = "var goog = {};\n"
        + "goog.inherits = function(child, parent) {};\n"
        + "/** @constructor */ function Base() {}\n"
        + "/** @constructor */ function Derived() {}\n"
        + "goog.inherits(Derived, Base);";
    Scope scope = buildGlobalScope(js);
    assertTrue(scope.isDeclared("Base", false));
    assertTrue(scope.isDeclared("Derived", false));
  }

  @Test
  public void testStubDeclarations() {
    String js = "var obj = {}; obj.stubProp;";
    Scope scope = buildGlobalScope(js);
    assertTrue(scope.isDeclared("obj", false));
    assertTrue(scope.isDeclared("obj.stubProp", false));
  }

  @Test
  public void testPrototypeReassignment() {
    String js = "/** @constructor */ function Base() {}\n"
        + "Base.prototype = { a: function() {}, b: function() {} };";
    Scope scope = buildGlobalScope(js);
    assertTrue(scope.isDeclared("Base", false));
    assertNotNull(scope.getVar("Base.prototype"));
  }

  @Test
  public void testBleedingFunctionName() {
    String js = "var f = function bleeding(x) { return bleeding(x - 1); };";
    Node root = compiler.parseTestCode(js);
    Scope globalScope = scopeCreator.createScope(root, null);
    Node assignNode = root.getFirstChild().getFirstChild();
    Node fnNode = assignNode.getFirstChild();
    Scope localScope = buildLocalScope(fnNode, globalScope);
    assertTrue(localScope.isDeclared("bleeding", false));
  }

  @Test
  public void testCollectPropertiesInConstructor() {
    String js = "/** @constructor */ function Person() {\n"
        + "  /** @type {string} */ this.name = 'Alice';\n"
        + "  /** @type {number} */ this.age = 30;\n"
        + "}";
    Scope scope = buildGlobalScope(js);
    Scope.Var personVar = scope.getVar("Person");
    assertNotNull(personVar);
    FunctionType ctor = (FunctionType) personVar.getType();
    ObjectType instance = ctor.getInstanceType();
    assertTrue(instance.hasOwnProperty("name"));
    assertTrue(instance.hasOwnProperty("age"));
  }

  @Test
  public void testFunctionTypeAnnotation() {
    String js = "/** @type {function(number): string} */ var intToStr = function(x) { return '' + x; };";
    Scope scope = buildGlobalScope(js);
    Scope.Var varSlot = scope.getVar("intToStr");
    assertNotNull(varSlot);
    assertTrue(varSlot.getType() instanceof FunctionType);
  }

  @Test
  public void testMultipleVarDefWarningWithJsDoc() {
    String js = "/** @type {number} */ var x = 1, y = 2;";
    Node root = compiler.parseTestCode(js);
    scopeCreator.createScope(root, null);
    assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testObjectLiteralCast() {
    String js = "var goog = {}; goog.reflect = {}; goog.reflect.object = function(type, obj) { return obj; };\n"
        + "/** @constructor */ function Target() {}\n"
        + "var casted = goog.reflect.object(Target, { x: 1 });";
    Scope scope = buildGlobalScope(js);
    assertNotNull(scope.getVar("casted"));
  }

  @Test
  public void testSingletonGetter() {
    String js = "var goog = {}; goog.addSingletonGetter = function(ctor) {};\n"
        + "/** @constructor */ function Single() {}\n"
        + "goog.addSingletonGetter(Single);";
    Scope scope = buildGlobalScope(js);
    assertNotNull(scope.getVar("Single"));
  }

  @Test
  public void testConstantsAndDiagnosticTypes() {
    assertNotNull(TypedScopeCreator.DELEGATE_PROXY_SUFFIX);
    assertNotNull(TypedScopeCreator.MALFORMED_TYPEDEF);
    assertNotNull(TypedScopeCreator.ENUM_INITIALIZER);
    assertNotNull(TypedScopeCreator.CONSTRUCTOR_EXPECTED);
    assertEquals("(Proxy)", TypedScopeCreator.DELEGATE_PROXY_SUFFIX);
  }
}
