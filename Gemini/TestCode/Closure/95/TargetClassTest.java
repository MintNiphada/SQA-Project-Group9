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
  private TypedScopeCreator creator;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    creator = new TypedScopeCreator(compiler);
  }

  private Scope createGlobalScope(String js) {
    Node root = compiler.parseTestCode(js);
    return creator.createScope(root, null);
  }

  @Test
  public void testInitialScopeNativeTypes() {
    Node root = compiler.parseTestCode("");
    Scope scope = creator.createInitialScope(root);

    assertNotNull(scope.getVar("Object"));
    assertNotNull(scope.getVar("Function"));
    assertNotNull(scope.getVar("Array"));
    assertNotNull(scope.getVar("String"));
    assertNotNull(scope.getVar("Boolean"));
    assertNotNull(scope.getVar("Number"));
    assertNotNull(scope.getVar("Date"));
    assertNotNull(scope.getVar("RegExp"));
    assertNotNull(scope.getVar("Error"));
    assertNotNull(scope.getVar("EvalError"));
    assertNotNull(scope.getVar("RangeError"));
    assertNotNull(scope.getVar("ReferenceError"));
    assertNotNull(scope.getVar("SyntaxError"));
    assertNotNull(scope.getVar("TypeError"));
    assertNotNull(scope.getVar("URIError"));
    assertNotNull(scope.getVar("undefined"));
    assertNotNull(scope.getVar("goog.typedef"));
    assertNotNull(scope.getVar("ActiveXObject"));
  }

  @Test
  public void testVarDeclarationSimple() {
    String js = "var a = 1; var b = 'hello'; var c = true; var d = null; var e = void 0;";
    Scope scope = createGlobalScope(js);

    assertTrue(scope.isDeclared("a", false));
    assertTrue(scope.isDeclared("b", false));
    assertTrue(scope.isDeclared("c", false));
    assertTrue(scope.isDeclared("d", false));
    assertTrue(scope.isDeclared("e", false));
  }

  @Test
  public void testMultiVarDeclaration() {
    String js = "var x = 1, y = 2, z = 3;";
    Scope scope = createGlobalScope(js);

    assertTrue(scope.isDeclared("x", false));
    assertTrue(scope.isDeclared("y", false));
    assertTrue(scope.isDeclared("z", false));
  }

  @Test
  public void testMultiVarWithDocWarning() {
    String js = "/** @type {number} */ var x = 1, y = 2;";
    createGlobalScope(js);
    assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testFunctionDeclarationGlobalAndLocal() {
    String js = "function foo(x, y) { var z = x + y; return z; }";
    Node root = compiler.parseTestCode(js);
    Scope globalScope = creator.createScope(root, null);

    assertTrue(globalScope.isDeclared("foo", false));
    Scope.Var fooVar = globalScope.getVar("foo");
    assertNotNull(fooVar.getType());
    assertTrue(fooVar.getType().isFunctionType());

    Node fnNode = root.getFirstChild();
    Scope localScope = creator.createScope(fnNode, globalScope);

    assertTrue(localScope.isDeclared("x", false));
    assertTrue(localScope.isDeclared("y", false));
    assertTrue(localScope.isDeclared("z", false));
    assertFalse(localScope.isGlobal());
  }

  @Test
  public void testFunctionWithJSDoc() {
    String js = "/**\n"
        + " * @param {number} a\n"
        + " * @param {string} b\n"
        + " * @return {boolean}\n"
        + " */\n"
        + "function testFn(a, b) { return true; }";
    Scope scope = createGlobalScope(js);
    Scope.Var fnVar = scope.getVar("testFn");
    assertNotNull(fnVar);
    FunctionType fnType = (FunctionType) fnVar.getType();
    assertNotNull(fnType);
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.BOOLEAN_TYPE), fnType.getReturnType());
  }

  @Test
  public void testConstructorAndPrototypeProperties() {
    String js = "/** @constructor */ function MyClass() { /** @type {number} */ this.field = 42; }\n"
        + "MyClass.prototype.method = function() { return this.field; };";
    Scope scope = createGlobalScope(js);

    Scope.Var classVar = scope.getVar("MyClass");
    assertNotNull(classVar);
    assertTrue(classVar.getType().isConstructor());

    Scope.Var protoVar = scope.getVar("MyClass.prototype");
    assertNotNull(protoVar);

    Scope.Var methodVar = scope.getVar("MyClass.prototype.method");
    assertNotNull(methodVar);
  }

  @Test
  public void testEnumTypeValid() {
    String js = "/** @enum {number} */ var Status = { OK: 200, ERROR: 500 };";
    Scope scope = createGlobalScope(js);

    Scope.Var statusVar = scope.getVar("Status");
    assertNotNull(statusVar);
    assertTrue(statusVar.getType() instanceof EnumType);
    EnumType enumType = (EnumType) statusVar.getType();
    assertTrue(enumType.hasOwnProperty("OK"));
    assertTrue(enumType.hasOwnProperty("ERROR"));
  }

  @Test
  public void testEnumTypeDuplicateElement() {
    String js = "/** @enum {number} */ var Status = { OK: 200, OK: 200 };";
    createGlobalScope(js);
    assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testEnumTypeInvalidKey() {
    String js = "/** @enum {number} */ var Status = { 'lower_case': 1 };";
    createGlobalScope(js);
    assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testEnumTypeNotObjectLit() {
    String js = "/** @enum {number} */ var Status = 123;";
    createGlobalScope(js);
    assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testTypedefValid() {
    String js = "/** @typedef {(string|number)} */ var StringOrNum;";
    Scope scope = createGlobalScope(js);
    assertNotNull(scope.getVar("StringOrNum"));
    JSType declared = compiler.getTypeRegistry().getType("StringOrNum");
    assertNotNull(declared);
  }

  @Test
  public void testTypedefMalformed() {
    String js = "/** @typedef */ var Malformed;";
    createGlobalScope(js);
    assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testCatchScope() {
    String js = "function f() { try { var a = 1; } catch (err) { var b = err; } }";
    Node root = compiler.parseTestCode(js);
    Scope globalScope = creator.createScope(root, null);
    Node fnNode = root.getFirstChild();
    Scope localScope = creator.createScope(fnNode, globalScope);

    assertNotNull(localScope);
    assertTrue(localScope.isDeclared("a", false));
    assertTrue(localScope.isDeclared("b", false));
    assertTrue(localScope.isDeclared("err", false));
  }

  @Test
  public void testBleedingFunction() {
    String js = "var f = function bleeding(x) { return x; };";
    Node root = compiler.parseTestCode(js);
    Scope globalScope = creator.createScope(root, null);

    Node fnNode = root.getFirstChild().getFirstChild().getFirstChild();
    Scope localScope = creator.createScope(fnNode, globalScope);
    assertTrue(localScope.isDeclared("bleeding", false));
  }

  @Test
  public void testObjectLiteralCastValid() {
    String js = "/** @constructor */ function Foo() {}\n"
        + "goog.reflect.object(Foo, { a: 1 });";
    Scope scope = createGlobalScope(js);
    assertNotNull(scope.getVar("Foo"));
  }

  @Test
  public void testObjectLiteralCastInvalid() {
    String js = "var notACtor = 123;\n"
        + "goog.reflect.object(notACtor, { a: 1 });";
    createGlobalScope(js);
    assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testInheritanceHook() {
    String js = "/** @constructor */ function Parent() {}\n"
        + "/** @constructor */ function Child() {}\n"
        + "goog.inherits(Child, Parent);";
    Scope scope = createGlobalScope(js);
    assertNotNull(scope.getVar("Child"));
    assertNotNull(scope.getVar("Parent"));
  }

  @Test
  public void testSingletonGetterHook() {
    String js = "/** @constructor */ function Singleton() {}\n"
        + "goog.addSingletonGetter(Singleton);";
    Scope scope = createGlobalScope(js);
    assertNotNull(scope.getVar("Singleton"));
  }

  @Test
  public void testStubDeclarations() {
    String js = "var ns = {};\n"
        + "ns.prop;\n"
        + "ns.anotherProp;";
    Scope scope = createGlobalScope(js);
    assertTrue(scope.isDeclared("ns", false));
    assertTrue(scope.isDeclared("ns.prop", false));
    assertTrue(scope.isDeclared("ns.anotherProp", false));
  }

  @Test
  public void testRedeclarationWarning() {
    String js = "/** @type {number} */ var x = 1;\n"
        + "/** @type {string} */ var x = 'dup';";
    createGlobalScope(js);
    assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testCustomCodingConventionConstructor() {
    CodingConvention customConvention = new GoogleCodingConvention();
    TypedScopeCreator customCreator = new TypedScopeCreator(compiler, customConvention);
    Node root = compiler.parseTestCode("var a = 1;");
    Scope scope = customCreator.createScope(root, null);
    assertNotNull(scope);
    assertTrue(scope.isDeclared("a", false));
  }

  @Test
  public void testLiteralsInAST() {
    String js = "var n = 42; var s = 'test'; var b = false; var r = /abc/; var obj = {};";
    Scope scope = createGlobalScope(js);
    assertNotNull(scope.getVar("n"));
    assertNotNull(scope.getVar("s"));
    assertNotNull(scope.getVar("b"));
    assertNotNull(scope.getVar("r"));
    assertNotNull(scope.getVar("obj"));
  }
}