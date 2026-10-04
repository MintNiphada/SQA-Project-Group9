package com.google.javascript.jscomp;

import com.google.javascript.rhino.ErrorReporter;
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
  private TypedScopeCreator creator;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    creator = new TypedScopeCreator(compiler);
  }

  private Scope buildGlobalScope(String js) {
    Node root = compiler.parseTestCode(js);
    return creator.createScope(root, null);
  }

  private Scope buildLocalScope(String js) {
    Node root = compiler.parseTestCode(js);
    Scope globalScope = creator.createScope(root, null);
    Node script = root.getFirstChild();
    Node firstStatement = script.getFirstChild();
    Node fnNode = null;
    if (firstStatement.isFunction()) {
      fnNode = firstStatement;
    } else if (firstStatement.isVar()) {
      fnNode = firstStatement.getFirstChild().getFirstChild();
    } else if (firstStatement.isExprResult()) {
      Node expr = firstStatement.getFirstChild();
      if (expr.isAssign()) {
        fnNode = expr.getLastChild();
      }
    }
    Assert.assertNotNull(fnNode);
    Assert.assertTrue(fnNode.isFunction());
    return creator.createScope(fnNode, globalScope);
  }

  @Test
  public void testCreateInitialScope() {
    Node root = new Node(Token.BLOCK);
    Scope scope = creator.createInitialScope(root);
    Assert.assertNotNull(scope);
    Assert.assertTrue(scope.isGlobal());
    Assert.assertNotNull(scope.getVar("Object"));
    Assert.assertNotNull(scope.getVar("Function"));
    Assert.assertNotNull(scope.getVar("Array"));
    Assert.assertNotNull(scope.getVar("String"));
    Assert.assertNotNull(scope.getVar("Boolean"));
    Assert.assertNotNull(scope.getVar("Number"));
    Assert.assertNotNull(scope.getVar("Date"));
    Assert.assertNotNull(scope.getVar("RegExp"));
    Assert.assertNotNull(scope.getVar("Error"));
    Assert.assertNotNull(scope.getVar("undefined"));
    Assert.assertNotNull(scope.getVar("ActiveXObject"));
  }

  @Test
  public void testSimpleVariableDeclarations() {
    Scope scope = buildGlobalScope("var a = 1; var b = 'str'; var c = true; var d = null; var e = void 0; var f = /abc/;");
    Assert.assertNotNull(scope.getVar("a"));
    Assert.assertEquals(JSTypeNative.NUMBER_TYPE, scope.getVar("a").getType().findPrimitivestType());
    Assert.assertNotNull(scope.getVar("b"));
    Assert.assertEquals(JSTypeNative.STRING_TYPE, scope.getVar("b").getType().findPrimitivestType());
    Assert.assertNotNull(scope.getVar("c"));
    Assert.assertEquals(JSTypeNative.BOOLEAN_TYPE, scope.getVar("c").getType().findPrimitivestType());
    Assert.assertNotNull(scope.getVar("d"));
    Assert.assertTrue(scope.getVar("d").getType().isNullType());
    Assert.assertNotNull(scope.getVar("e"));
    Assert.assertTrue(scope.getVar("e").getType().isVoidType());
    Assert.assertNotNull(scope.getVar("f"));
    Assert.assertTrue(scope.getVar("f").getType().isInstanceType());
  }

  @Test
  public void testFunctionDeclarations() {
    Scope scope = buildGlobalScope("function foo(x, y) { return x; }");
    Assert.assertNotNull(scope.getVar("foo"));
    Assert.assertTrue(scope.getVar("foo").getType().isFunctionType());
  }

  @Test
  public void testLocalScopeBuilding() {
    Scope localScope = buildLocalScope("function parent(a, b) { var c = 10; try {} catch (e) {} return a + b + c; }");
    Assert.assertNotNull(localScope);
    Assert.assertTrue(localScope.isLocal());
    Assert.assertNotNull(localScope.getVar("a"));
    Assert.assertNotNull(localScope.getVar("b"));
    Assert.assertNotNull(localScope.getVar("c"));
    Assert.assertNotNull(localScope.getVar("e"));
  }

  @Test
  public void testBleedingFunction() {
    Scope localScope = buildLocalScope("var outer = function inner(x) { return inner(x - 1); };");
    Assert.assertNotNull(localScope.getVar("inner"));
    Assert.assertNotNull(localScope.getVar("x"));
  }

  @Test
  public void testConstructorAndInterfaceDeclarations() {
    Scope scope = buildGlobalScope(
        "/** @constructor */ function MyClass() {}\n" +
        "/** @interface */ function MyInterface() {}\n"
    );
    Assert.assertNotNull(scope.getVar("MyClass"));
    Assert.assertTrue(scope.getVar("MyClass").getType().isConstructor());
    Assert.assertNotNull(scope.getVar("MyClass.prototype"));

    Assert.assertNotNull(scope.getVar("MyInterface"));
    Assert.assertTrue(scope.getVar("MyInterface").getType().isInterface());
    Assert.assertNotNull(scope.getVar("MyInterface.prototype"));
  }

  @Test
  public void testEnumTypeDeclaration() {
    Scope scope = buildGlobalScope(
        "/** @enum {number} */ var MyEnum = { A: 1, B: 2 };"
    );
    Assert.assertNotNull(scope.getVar("MyEnum"));
    Assert.assertTrue(scope.getVar("MyEnum").getType().isEnumType());
    EnumType enumType = scope.getVar("MyEnum").getType().toMaybeEnumType();
    Assert.assertTrue(enumType.getElementsType().isNumber());
    Assert.assertTrue(enumType.hasElement("A"));
    Assert.assertTrue(enumType.hasElement("B"));
  }

  @Test
  public void testTypedefDeclaration() {
    Scope scope = buildGlobalScope(
        "/** @typedef {number|string} */ var StringOrNumber;\n" +
        "/** @type {StringOrNumber} */ var x = 1;"
    );
    Assert.assertNotNull(scope.getVar("x"));
    Assert.assertTrue(scope.getVar("x").getType().isUnionType());
  }

  @Test
  public void testObjectLiteralLends() {
    Scope scope = buildGlobalScope(
        "/** @constructor */ function Foo() {}\n" +
        "var b = /** @lends {Foo.prototype} */ ({ bar: function() {} });"
    );
    Assert.assertNotNull(scope.getVar("Foo"));
    ObjectType fooProto = ObjectType.cast(scope.getVar("Foo.prototype").getType());
    Assert.assertTrue(fooProto.hasProperty("bar"));
  }

  @Test
  public void testUnknownLendsWarning() {
    buildGlobalScope("var b = /** @lends {NonExistent} */ ({ bar: 1 });");
    Assert.assertEquals(1, compiler.getWarningCount());
    Assert.assertEquals(TypedScopeCreator.UNKNOWN_LENDS.key, compiler.getWarnings()[0].getType().key);
  }

  @Test
  public void testLendsOnNonObjectWarning() {
    buildGlobalScope(
        "var num = 123;\n" +
        "var b = /** @lends {num} */ ({ bar: 1 });"
    );
    Assert.assertEquals(1, compiler.getWarningCount());
    Assert.assertEquals(TypedScopeCreator.LENDS_ON_NON_OBJECT.key, compiler.getWarnings()[0].getType().key);
  }

  @Test
  public void testMultipleVarDefWarning() {
    buildGlobalScope("/** @type {number} */ var a = 1, b = 2;");
    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testInheritanceAndProperties() {
    Scope scope = buildGlobalScope(
        "/** @constructor */ function Super() {}\n" +
        "Super.prototype.foo = function() {};\n" +
        "/** @constructor\n * @extends {Super} */ function Sub() {}\n" +
        "goog.inherits(Sub, Super);\n" +
        "Sub.prototype.foo = function() {};\n"
    );
    Assert.assertNotNull(scope.getVar("Super"));
    Assert.assertNotNull(scope.getVar("Sub"));
  }

  @Test
  public void testSingletonGetter() {
    Scope scope = buildGlobalScope(
        "/** @constructor */ function Singleton() {}\n" +
        "goog.addSingletonGetter(Singleton);\n"
    );
    Assert.assertNotNull(scope.getVar("Singleton"));
    ObjectType ctor = ObjectType.cast(scope.getVar("Singleton").getType());
    Assert.assertTrue(ctor.hasProperty("getInstance"));
  }

  @Test
  public void testObjectLiteralCast() {
    Scope scope = buildGlobalScope(
        "/** @constructor */ function SomeType() {}\n" +
        "var x = goog.reflect.object(SomeType, {});\n"
    );
    Assert.assertNotNull(scope.getVar("x"));
  }

  @Test
  public void testObjectLiteralCastMissingCtor() {
    buildGlobalScope("var x = goog.reflect.object(UnregisteredType, {});");
    Assert.assertEquals(1, compiler.getWarningCount());
    Assert.assertEquals(TypedScopeCreator.CONSTRUCTOR_EXPECTED.key, compiler.getWarnings()[0].getType().key);
  }

  @Test
  public void testWindowConstructor() {
    Scope scope = buildGlobalScope("/** @constructor */ function Window() {}");
    Assert.assertNotNull(scope.getVar("Window"));
    Assert.assertTrue(scope.getVar("Window").getType().isConstructor());
  }

  @Test
  public void testPatchGlobalScope() {
    String js = "var a = 1; var b = 2;";
    Node root = compiler.parseTestCode(js);
    Scope globalScope = creator.createScope(root, null);
    Assert.assertNotNull(globalScope.getVar("a"));
    Assert.assertNotNull(globalScope.getVar("b"));

    Node scriptNode = root.getFirstChild();
    creator.patchGlobalScope(globalScope, scriptNode);
    Assert.assertNotNull(globalScope.getVar("a"));
    Assert.assertNotNull(globalScope.getVar("b"));
  }

  @Test
  public void testStubDeclarations() {
    Scope scope = buildGlobalScope(
        "var ns = {};\n" +
        "ns.stubProp;\n"
    );
    Assert.assertNotNull(scope.getVar("ns"));
    ObjectType nsType = ObjectType.cast(scope.getVar("ns").getType());
    Assert.assertNotNull(nsType);
    Assert.assertTrue(nsType.hasProperty("stubProp"));
  }

  @Test
  public void testConstantInference() {
    Scope scope = buildGlobalScope(
        "/** @const */ var X = 42;\n" +
        "var Y = Y || 100;\n"
    );
    Assert.assertNotNull(scope.getVar("X"));
    Assert.assertTrue(scope.getVar("X").getType().isNumber());
    Assert.assertNotNull(scope.getVar("Y"));
  }

  @Test
  public void testConstructorAlias() {
    Scope scope = buildGlobalScope(
        "/** @constructor */ function Original() {}\n" +
        "var Alias = Original;\n" +
        "/** @type {Alias} */ var inst;\n"
    );
    Assert.assertNotNull(scope.getVar("Alias"));
    Assert.assertTrue(scope.getVar("Alias").getType().isConstructor());
    Assert.assertNotNull(scope.getVar("inst"));
    Assert.assertTrue(scope.getVar("inst").getType().isInstanceType());
  }

  @Test
  public void testPrototypeRedefinition() {
    Scope scope = buildGlobalScope(
        "/** @constructor */ function Base() {}\n" +
        "Base.prototype = { prop1: 1, prop2: 'two' };\n"
    );
    Assert.assertNotNull(scope.getVar("Base"));
    ObjectType proto = ObjectType.cast(scope.getVar("Base.prototype").getType());
    Assert.assertTrue(proto.hasProperty("prop1"));
    Assert.assertTrue(proto.hasProperty("prop2"));
  }

  @Test
  public void testFunctionPropertiesThisCollection() {
    Scope scope = buildGlobalScope(
        "/** @constructor */ function Person() {\n" +
        "  /** @type {string} */ this.name = 'John';\n" +
        "  /** @type {number} */ this.age = 30;\n" +
        "}\n"
    );
    Assert.assertNotNull(scope.getVar("Person"));
    FunctionType personCtor = scope.getVar("Person").getType().toMaybeFunctionType();
    ObjectType instanceType = personCtor.getInstanceType();
    Assert.assertTrue(instanceType.hasProperty("name"));
    Assert.assertTrue(instanceType.hasProperty("age"));
  }
}
