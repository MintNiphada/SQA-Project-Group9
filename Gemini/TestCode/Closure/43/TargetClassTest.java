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
    compiler.initOptions(options);
  }

  private Scope createGlobalScope(String js) {
    return createGlobalScope("", js);
  }

  private Scope createGlobalScope(String externsJs, String js) {
    Node externsNode = compiler.parseTestCode(externsJs);
    Node mainNode = compiler.parseTestCode(js);
    Node root = new Node(Token.BLOCK, externsNode, mainNode);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    return creator.createScope(root, null);
  }

  private Scope createLocalScope(Node fnNode, Scope parentScope) {
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    return creator.createScope(fnNode, parentScope);
  }

  @Test
  public void testInitialScopeNatives() {
    Scope scope = createGlobalScope("");
    assertTrue(scope.isGlobal());
    assertNotNull(scope.getVar("Object"));
    assertNotNull(scope.getVar("Array"));
    assertNotNull(scope.getVar("Function"));
    assertNotNull(scope.getVar("Date"));
    assertNotNull(scope.getVar("Error"));
    assertNotNull(scope.getVar("RegExp"));
    assertNotNull(scope.getVar("String"));
    assertNotNull(scope.getVar("Number"));
    assertNotNull(scope.getVar("Boolean"));
    assertNotNull(scope.getVar("undefined"));
    assertNotNull(scope.getVar("ActiveXObject"));
    assertNotNull(scope.getVar("Object.prototype"));
    assertNotNull(scope.getVar("Function.prototype"));

    JSType undefinedType = scope.getVar("undefined").getType();
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.VOID_TYPE), undefinedType);
  }

  @Test
  public void testLiteralTypesInference() {
    String js =
        "var a = null;\n"
            + "var b = void 0;\n"
            + "var c = 'hello';\n"
            + "var d = 42;\n"
            + "var e = true;\n"
            + "var f = false;\n"
            + "var g = /abc/;\n"
            + "var h = {};\n";
    Scope scope = createGlobalScope(js);

    assertEquals(
        compiler.getTypeRegistry().getNativeType(JSTypeNative.NULL_TYPE),
        scope.getVar("a").getType());
    assertEquals(
        compiler.getTypeRegistry().getNativeType(JSTypeNative.VOID_TYPE),
        scope.getVar("b").getType());
    assertEquals(
        compiler.getTypeRegistry().getNativeType(JSTypeNative.STRING_TYPE),
        scope.getVar("c").getType());
    assertEquals(
        compiler.getTypeRegistry().getNativeType(JSTypeNative.NUMBER_TYPE),
        scope.getVar("d").getType());
    assertEquals(
        compiler.getTypeRegistry().getNativeType(JSTypeNative.BOOLEAN_TYPE),
        scope.getVar("e").getType());
    assertEquals(
        compiler.getTypeRegistry().getNativeType(JSTypeNative.BOOLEAN_TYPE),
        scope.getVar("f").getType());
    assertEquals(
        compiler.getTypeRegistry().getNativeType(JSTypeNative.REGEXP_TYPE),
        scope.getVar("g").getType());
    assertNotNull(scope.getVar("h").getType());
  }

  @Test
  public void testMultipleVarDefWarning() {
    String js = "/** @type {number} */ var x = 1, y = 2;";
    createGlobalScope(js);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeCheck.MULTIPLE_VAR_DEF, compiler.getWarnings()[0].getType());
  }

  @Test
  public void testFunctionDeclarationsAndLocalScope() {
    String js = "function foo(x, y) { var z = x + y; return z; }";
    Node mainNode = compiler.parseTestCode(js);
    Node externsNode = new Node(Token.BLOCK);
    Node root = new Node(Token.BLOCK, externsNode, mainNode);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    Scope.Var fooVar = globalScope.getVar("foo");
    assertNotNull(fooVar);
    assertTrue(fooVar.getType().isFunctionType());

    Node fnNode = mainNode.getFirstChild();
    Scope localScope = creator.createScope(fnNode, globalScope);
    assertTrue(localScope.isLocal());
    assertNotNull(localScope.getVar("x"));
    assertNotNull(localScope.getVar("y"));
    assertNotNull(localScope.getVar("z"));
  }

  @Test
  public void testBleedingFunction() {
    String js = "var f = function myFunc() { myFunc(); };";
    Node mainNode = compiler.parseTestCode(js);
    Node externsNode = new Node(Token.BLOCK);
    Node root = new Node(Token.BLOCK, externsNode, mainNode);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    Node varNode = mainNode.getFirstChild();
    Node nameNode = varNode.getFirstChild();
    Node fnNode = nameNode.getFirstChild();

    Scope localScope = creator.createScope(fnNode, globalScope);
    assertNotNull(localScope.getVar("myFunc"));
  }

  @Test
  public void testConstructorAndPrototypeDeclaration() {
    String js = "/** @constructor */ function MyClass() {}";
    Scope scope = createGlobalScope(js);
    Scope.Var classVar = scope.getVar("MyClass");
    assertNotNull(classVar);
    assertTrue(classVar.getType().isConstructor());

    Scope.Var protoVar = scope.getVar("MyClass.prototype");
    assertNotNull(protoVar);
  }

  @Test
  public void testInterfaceDeclaration() {
    String js = "/** @interface */ function MyIface() {}";
    Scope scope = createGlobalScope(js);
    Scope.Var ifaceVar = scope.getVar("MyIface");
    assertNotNull(ifaceVar);
    assertTrue(ifaceVar.getType().isInterface());
    assertNotNull(scope.getVar("MyIface.prototype"));
  }

  @Test
  public void testUninitializedConstructorWarning() {
    String js = "/** @constructor */ var UninitializedCtor;";
    createGlobalScope(js);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.CTOR_INITIALIZER, compiler.getWarnings()[0].getType());
  }

  @Test
  public void testUninitializedInterfaceWarning() {
    String js = "/** @interface */ var UninitializedIface;";
    createGlobalScope(js);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.IFACE_INITIALIZER, compiler.getWarnings()[0].getType());
  }

  @Test
  public void testEnumDeclaration() {
    String js = "/** @enum {number} */ var MyEnum = { A: 1, B: 2 };";
    Scope scope = createGlobalScope(js);
    Scope.Var enumVar = scope.getVar("MyEnum");
    assertNotNull(enumVar);
    assertTrue(enumVar.getType().isEnumType());
    EnumType enumType = (EnumType) enumVar.getType();
    assertTrue(enumType.getElements().contains("A"));
    assertTrue(enumType.getElements().contains("B"));
  }

  @Test
  public void testInvalidEnumInitializer() {
    String js = "/** @enum {number} */ var BadEnum = 123;";
    createGlobalScope(js);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.ENUM_INITIALIZER, compiler.getWarnings()[0].getType());
  }

  @Test
  public void testInvalidEnumKey() {
    String js = "/** @enum {number} */ var BadKeys = { 'invalid-key!': 1 };";
    createGlobalScope(js);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeCheck.ENUM_NOT_CONSTANT, compiler.getWarnings()[0].getType());
  }

  @Test
  public void testTypedefDeclaration() {
    String js = "/** @typedef {number|string} */ var NumOrStr;";
    Scope scope = createGlobalScope(js);
    JSType type = compiler.getTypeRegistry().getType("NumOrStr");
    assertNotNull(type);
    assertTrue(type.isUnionType());
  }

  @Test
  public void testMalformedTypedef() {
    String js = "/** @typedef */ var BadTypedef;";
    createGlobalScope(js);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.MALFORMED_TYPEDEF, compiler.getWarnings()[0].getType());
  }

  @Test
  public void testLendsAnnotation() {
    String js =
        "/** @constructor */ function Foo() {}\n"
            + "var obj = /** @lends {Foo.prototype} */ ({ bar: function() {} });";
    Scope scope = createGlobalScope(js);
    Scope.Var objVar = scope.getVar("obj");
    assertNotNull(objVar);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test
  public void testUnknownLendsWarning() {
    String js = "var obj = /** @lends {NonExistentClass.prototype} */ ({});";
    createGlobalScope(js);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.UNKNOWN_LENDS, compiler.getWarnings()[0].getType());
  }

  @Test
  public void testLendsOnNonObjectWarning() {
    String js =
        "var notAnObj = 123;\n"
            + "var obj = /** @lends {notAnObj} */ ({});";
    createGlobalScope(js);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.LENDS_ON_NON_OBJECT, compiler.getWarnings()[0].getType());
  }

  @Test
  public void testCatchScope() {
    String js = "function testCatch() { try {} catch (e) { var x = e; } }";
    Node mainNode = compiler.parseTestCode(js);
    Node externsNode = new Node(Token.BLOCK);
    Node root = new Node(Token.BLOCK, externsNode, mainNode);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    Node fnNode = mainNode.getFirstChild();
    Scope localScope = creator.createScope(fnNode, globalScope);
    assertNotNull(localScope.getVar("e"));
    assertNotNull(localScope.getVar("x"));
  }

  @Test
  public void testStubDeclarations() {
    String js = "var ns = {}; ns.prop;";
    Scope scope = createGlobalScope(js);
    assertNotNull(scope.getVar("ns.prop"));
    assertTrue(scope.getVar("ns.prop").getType().isUnknownType());
  }

  @Test
  public void testInheritanceSubclassRelationship() {
    String js =
        "/** @constructor */ function Super() {}\n"
            + "/** @constructor */ function Sub() {}\n"
            + "goog.inherits(Sub, Super);";
    Scope scope = createGlobalScope(js);
    Scope.Var subVar = scope.getVar("Sub");
    assertNotNull(subVar);
    FunctionType subCtor = subVar.getType().toMaybeFunctionType();
    assertNotNull(subCtor);
    assertEquals("Super", subCtor.getSuperClassConstructor().getInstanceType().getReferenceName());
  }

  @Test
  public void testSingletonGetter() {
    String js =
        "/** @constructor */ function Singleton() {}\n"
            + "goog.addSingletonGetter(Singleton);";
    Scope scope = createGlobalScope(js);
    Scope.Var var = scope.getVar("Singleton");
    assertNotNull(var);
    ObjectType type = var.getType().toMaybeFunctionType();
    assertTrue(type.hasProperty("getInstance"));
  }

  @Test
  public void testWindowConstructorRedefinition() {
    String js = "/** @constructor */ function Window() {}";
    Scope scope = createGlobalScope(js);
    assertNotNull(scope.getVar("Window"));
    assertTrue(scope.getVar("Window").getType().isConstructor());
  }

  @Test
  public void testFunctionPropertiesInMethodWithThis() {
    String js =
        "/** @constructor */ function Widget() { "
            + "  /** @type {number} */ this.counter = 0; "
            + "}";
    Scope scope = createGlobalScope(js);
    FunctionType ctor = scope.getVar("Widget").getType().toMaybeFunctionType();
    assertNotNull(ctor);
    ObjectType instanceType = ctor.getInstanceType();
    assertTrue(instanceType.hasProperty("counter"));
    assertEquals(
        compiler.getTypeRegistry().getNativeType(JSTypeNative.NUMBER_TYPE),
        instanceType.getPropertyType("counter"));
  }

  @Test
  public void testPatchGlobalScope() {
    String js1 = "var a = 1;";
    Node script1 = compiler.parseTestCode(js1);
    script1.setSourceName("file1.js");
    Node externsNode = new Node(Token.BLOCK);
    Node root = new Node(Token.BLOCK, externsNode, script1);

    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);
    assertNotNull(globalScope.getVar("a"));

    Node script1Updated = compiler.parseTestCode("var a = 'updated';");
    script1Updated.setSourceName("file1.js");
    creator.patchGlobalScope(globalScope, script1Updated);

    Scope.Var aVar = globalScope.getVar("a");
    assertNotNull(aVar);
    assertEquals(
        compiler.getTypeRegistry().getNativeType(JSTypeNative.STRING_TYPE),
        aVar.getType());
  }

  @Test
  public void testConstantWithLogicalOrPattern() {
    String js = "var ns = ns || {};";
    Scope scope = createGlobalScope(js);
    assertNotNull(scope.getVar("ns"));
  }

  @Test
  public void testConstructorAlias() {
    String js =
        "/** @constructor */ function Foo() {}\n"
            + "var Bar = Foo;";
    Scope scope = createGlobalScope(js);
    Scope.Var barVar = scope.getVar("Bar");
    assertNotNull(barVar);
    assertTrue(barVar.getType().isConstructor());
  }

  @Test
  public void testDelegateProxySuffixConstant() {
    assertNotNull(TypedScopeCreator.DELEGATE_PROXY_SUFFIX);
    assertFalse(TypedScopeCreator.DELEGATE_PROXY_SUFFIX.isEmpty());
  }
}