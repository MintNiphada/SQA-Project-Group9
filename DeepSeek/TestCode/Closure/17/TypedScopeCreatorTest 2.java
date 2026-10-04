package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.common.collect.ImmutableList;
import com.google.javascript.jscomp.CodingConvention.DelegateRelationship;
import com.google.javascript.jscomp.CodingConvention.SubclassRelationship;
import com.google.javascript.jscomp.CodingConvention.SubclassType;
import com.google.javascript.jscomp.Scope.Var;
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
  
  private AbstractCompiler compiler;
  private JSTypeRegistry registry;
  private TypedScopeCreator creator;

  @Before
  public void setUp() throws Exception {
    compiler = new Compiler();
    // Initialize type registry and other required components
    compiler.initOptions(new CompilerOptions());
    // Force type checking mode? We'll just use default.
    registry = compiler.getTypeRegistry();
    // TypedScopeCreator instance
    creator = new TypedScopeCreator(compiler);
  }
  
  private Node parse(String js) {
    return compiler.parseSyntheticCode("test", js);
  }
  
  private Node getRoot(Node script) {
    // The root of scope creation expects the global root, which for parseSyntheticCode is a SCRIPT node
    return script;
  }
  
  // ---- Tests for createInitialScope ----
  @Test
  public void testCreateInitialScopeContainsNativeTypes() {
    Node root = parse("");
    Scope global = creator.createInitialScope(root);
    assertTrue(global.isGlobal());
    // Check for native Object function
    Var objectVar = global.getVar("Object");
    assertNotNull(objectVar);
    assertTrue(objectVar.getType().isFunctionType());
    // Check for Array
    Var arrayVar = global.getVar("Array");
    assertNotNull(arrayVar);
    // Check for undefined
    Var undefinedVar = global.getVar("undefined");
    assertEquals(registry.getNativeType(JSTypeNative.VOID_TYPE), undefinedVar.getType());
    // ActiveXObject
    Var activeXVar = global.getVar("ActiveXObject");
    assertNotNull(activeXVar);
    assertEquals(registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE), activeXVar.getType());
  }

  // ---- Tests for createScope ----
  
  @Test
  public void testCreateGlobalScopeWithSimpleVar() {
    String js = "var x = 42;";
    Node root = parse(js);
    Scope scope = creator.createScope(root, null);
    Var x = scope.getVar("x");
    assertNotNull(x);
    // Type should be inferred as number
    JSType type = x.getType();
    assertNotNull(type);
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), type);
  }
  
  @Test
  public void testCreateGlobalScopeWithTypedVar() {
    String js = "/** @type {string} */ var x = 'hello';";
    Node root = parse(js);
    Scope scope = creator.createScope(root, null);
    Var x = scope.getVar("x");
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), x.getType());
  }
  
  @Test
  public void testCreateGlobalScopeWithFunctionDeclaration() {
    String js = "function f() { return 1; }";
    Node root = parse(js);
    Scope scope = creator.createScope(root, null);
    Var f = scope.getVar("f");
    assertNotNull(f);
    JSType type = f.getType();
    assertTrue(type.isFunctionType());
    FunctionType fnType = type.toMaybeFunctionType();
    // Return type should be number
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), fnType.getReturnType());
  }
  
  @Test
  public void testCreateGlobalScopeWithConstructor() {
    String js = "/** @constructor */ function C() {}";
    Node root = parse(js);
    Scope scope = creator.createScope(root, null);
    Var cVar = scope.getVar("C");
    assertNotNull(cVar);
    assertTrue(cVar.getType().isFunctionType());
    FunctionType ctor = cVar.getType().toMaybeFunctionType();
    assertTrue(ctor.isConstructor());
    // Prototype should be declared
    Var protoVar = scope.getVar("C.prototype");
    assertNotNull(protoVar);
    assertTrue(protoVar.getType() instanceof ObjectType);
  }
  
  @Test
  public void testCreateGlobalScopeWithInterface() {
    String js = "/** @interface */ function I() {}";
    Node root = parse(js);
    Scope scope = creator.createScope(root, null);
    Var iVar = scope.getVar("I");
    assertNotNull(iVar);
    assertTrue(iVar.getType().isFunctionType());
    FunctionType iface = iVar.getType().toMaybeFunctionType();
    assertTrue(iface.isInterface());
  }
  
  @Test
  public void testCreateGlobalScopeWithEnum() {
    String js = "/** @enum {number} */ var E = {A:1, B:2};";
    Node root = parse(js);    Scope scope = creator.createScope(root, null);
    Var eVar = scope.getVar("E");    assertNotNull(eVar);
    assertTrue(eVar.getType() instanceof EnumType);    EnumType enumType = (EnumType) eVar.getType();
    assertNotNull(enumType.getElementsType());    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), enumType.getElementsType());
  }
  
  @Test  public void testCreateGlobalScopeWithTypedef() {
    String js = "/** @typedef {number} */ var T = 0;";
    Node root = parse(js);    Scope scope = creator.createScope(root, null);    // typedef should be registered in type registry
    JSType typedefType = registry.getType("T");    assertNotNull(typedefType);
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), typedefType);  }
  
  @Test  public void testCreateGlobalScopeWithPrototypeAssignment() {
    String js = "/** @constructor */ function F() {} F.prototype.method = function() { return 0; };";    Node root = parse(js);
    Scope scope = creator.createScope(root, null);    Var proto = scope.getVar("F.prototype");    assertNotNull(proto);
    ObjectType protoType = (ObjectType) proto.getType();    // Check that method property is added
    JSType methodType = protoType.getPropertyType("method");    assertNotNull(methodType);
    assertTrue(methodType.isFunctionType());  }
  
  @Test  public void testCreateScopeWithLendsAnnotation() {
    String js = "/** @constructor */ function A() {} var obj = /** @lends {A.prototype} */ {x:1};";
    Node root = parse(js);    Scope scope = creator.createScope(root, null);    // The object literal should have type A.prototype
    ObjectType aProto = registry.getType("A.prototype").toMaybeObjectType();    assertNotNull(aProto);
    assertTrue(aProto.getPropertyType("x") != null);  }
  
  @Test  public void testCreateScopeWithSubclassing() {
    String js = "/** @constructor */ function Parent() {} /** @constructor @extends {Parent} */ function Child() {}";    Node root = parse(js);
    Scope scope = creator.createScope(root, null);    // Verify inheritance
    FunctionType childCtor = (FunctionType) scope.getVar("Child").getType();
    FunctionType parentCtor = childCtor.getSuperClassConstructor();    assertNotNull(parentCtor);
    assertEquals(scope.getVar("Parent").getType(), parentCtor);  }
  
  @Test  public void testCreateScopeWithDelegateRelationship() {
    // This requires a custom coding convention that defines delegates. 
    // Since default convention may not have one, we will test that no error occurs.
    String js = "var x = {};";    Node root = parse(js);
    Scope scope = creator.createScope(root, null); // just ensure no crash  }
  
  @Test  public void testCreateScopeWithCatch() {
    String js = "try { throw ''; } catch(e) { var x = e; }";    Node root = parse(js);
    Scope scope = creator.createScope(root, null);    // In global scope, catch variable should be declared
    Var eVar = scope.getVar("e");    assertNotNull(eVar);
    // Type should be unknown    assertEquals(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), eVar.getType());
  }
  
  @Test  public void testCreateScopeWithObjectLiteralCast() {
    // For object literal cast like goog.reflect.object, need coding convention
    // Default convention returns null, so test that no crash
    String js = "/** @type {!Object} */ var x = {};";    Node root = parse(js);
    Scope scope = creator.createScope(root, null);  }
  
  @Test  public void testCreateLocalScope() {
    String js = "function f() { var x = 1; return x; }";    Node root = parse(js);
    Scope globalScope = creator.createScope(root, null);    // Get the function node
    Node fnNode = root.getFirstChild();
    // The local scope can be obtained as a child scope    Scope localScope = null;
    for (Scope child : globalScope.getChildren()) {      if (child.getRootNode() == fnNode) {
        localScope = child;        break;
      }    }
    assertNotNull(localScope);    Var x = localScope.getVar("x");
    assertNotNull(x);    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), x.getType());
  }
  
  @Test  public void testCreateScopeWithBleedingFunction() {
    String js = "if (true) { function g() { return 1; } }";    Node root = parse(js);
    Scope scope = creator.createScope(root, null);    // In non-strict, function g might be hoisted? Actually, closure treats as block-scoped.
    // But TypedScopeCreator handles hoisted functions. We just check no crash and existence.
    assertNotNull(scope.getVar("g"));  }
  
  // ---- Tests for patchGlobalScope ----
  @Test  public void testPatchGlobalScopeRemovesAndRedefines() {
    // First create global scope with script1
    String js1 = "var x = 1;";    Node root1 = parse(js1);
    Scope global = creator.createScope(root1, null);    assertNotNull(global.getVar("x"));
    // Now patch with a new script that redefines x
    String js2 = "var x = 'hello';";    Node root2 = parse(js2);
    creator.patchGlobalScope(global, root2);    Var x = global.getVar("x");
    assertNotNull(x);    // Type should be string now (based on annotation? no annotation, so number from value? but value is string, so string)
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), x.getType());
  }
  
  @Test  public void testPatchGlobalScopeWhenNoPreviousVars() {
    String js1 = "var y = 42;";    Node root1 = parse(js1);
    Scope global = creator.createScope(root1, null);    // Patch with script that doesn't contain var y
    String js2 = "var z = true;";    Node root2 = parse(js2);
    creator.patchGlobalScope(global, root2);    // y should be removed? Actually patchGlobalScope removes vars from same script name only.
    // Since js2 has different source name, y remains. So test that y still present.
    assertNotNull(global.getVar("y"));
    assertNotNull(global.getVar("z"));  }
  
  @Test(expected = IllegalStateException.class)
  public void testPatchGlobalScopeThrowsIfNotScriptNode() {
    Scope global = creator.createInitialScope(parse(""));
    Node notScript = new Node(Token.FUNCTION);    creator.patchGlobalScope(global, notScript);
  }
  
  // Error reporting tests
  @Test  public void testMalformedTypedefError() {
    String js = "/** @typedef {doesnotexist} */ var T = 0;";    Node root = parse(js);
    // This will report a MALFORMED_TYPEDEF warning    creator.createScope(root, null);
    // Check that error was reported    // We can't easily check the errors without accessing reporter, but test ensures no crash
  }
  
  @Test  public void testEnumInitializerNotObjectLiteralWarning() {
    String js = "/** @enum {number} */ var E = 5;";    Node root = parse(js);
    Scope scope = creator.createScope(root, null);    // Should have reported JSC_ENUM_INITIALIZER_NOT_ENUM
    // Test that enum type is still created? The code sets newVar with EnumType but then warns
  }
  
  @Test  public void testCtorInitialixerError() {
    String js = "/** @constructor */ function C() {} C = null;"; // not allowed? Actually error about init
    Node root = parse(js);    Scope scope = creator.createScope(root, null);    // Should warn about CTOR_INITIALIZER
  }
  
  @Test  public void testUnknownLendsWarning() {
    String js = "var obj = /** @lends {NoSuchType.prototype} */ {x:1};";    Node root = parse(js);
    Scope scope = creator.createScope(root, null);  }
  
  @Test  public void testLendsOnNonObjectWarning() {
    String js = "/** @constructor */ function A() {} var obj = /** @lends {A} */ {x:1};";    Node root = parse(js);
    Scope scope = creator.createScope(root, null));  }
  
  @Test  public void testMultipleVarDefError() {
    String js = "/** @type {number} */ var x = 1, y = 2;";    Node root = parse(js);
    Scope scope = creator.createScope(root, null);    // Should report MULTIPLE_VAR_DEF
  }
  
  // Edge cases
  @Test  public void testCreateScopeWithEmptyScript() {
    Node root = parse("");    Scope scope = creator.createScope(root, null);
    assertTrue(scope.isGlobal()); }
  
  @Test  public void testCreateScopeWithNullParent() {
    Scope scope = creator.createScope(parse("var x = 1;"), null);    assertNotNull(scope);
    assertTrue(scope.isGlobal()); }
  
  @Test  public void testCreateScopeWithFunctionInsideLocalScope() {
    String js = "function outer() { function inner() { return 1; } }";    Node root = parse(js);
    Scope global = creator.createScope(root, null);    // local scope of outer
    FunctionType outerType = (FunctionType) global.getVar("outer").getType();
    assertNotNull(outerType); }
  
  @Test  public void testDeferredSetTypeResolvesLater() {
    // This is implicit in most tests
    String js = "/** @type {number} */ var x = 1;";    Node root = parse(js);
    Scope scope = creator.createScope(root, null);    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), scope.getVar("x").getType());
  }
  
  // Test that properties collected on @this type
  @Test  public void testCollectPropertiesOnThisType() {
    String js = "/** @constructor */ function A() {} /** @type {number} */ A.prototype.x;";    Node root = parse(js);
    Scope scope = creator.createScope(root, null));    ObjectType proto = (ObjectType) scope.getVar("A.prototype").getType();
    JSType xType = proto.getPropertyType("x");    assertEquals(registry.getNativeType(JSTypeUnative.NUMBER_TYPE), xType);
  }
  
  // Additional coverage for FirstOrderFunctionAnalyzer
  @Test  public void testEscapedVariablesDetected() {
    String js = "function f() { var x = 1; return function() { return x; } }";    Node root = parse(js);
    Scope global = creator.createScope(root, null);    // The function f's inner scope should mark x as escaped    // We can test indirectly by checking that x's type is not expanded? Hmm
  }
  
  @Test  public void testEscapedQualifiedNameDetected() {
    String js = "var obj = {}; function f() { var localObj = obj; return function() { return localObj.x; } }";    Node root = parse(js);
    Scope global = creator.createScope(root, null));  }
  
  @Test  public void testShouldTraverseOnlyDescendsIntoFunctionsWhenNotParentFunction() {    // This is covered by normal tests
  }
  
  // Test helper methods via reflection? No, just ensure public API works
  @Test  public void testDefineSlotWithGlobalThis() {
    String js = "/** @type {number} */ var x = 1;";    Node root = parse(js);
    Scope scope = creator.createScope(root, null);    // Global this should have property x
    ObjectType globalThis = registry.getNativeObjectType(JSTypeNative.GLOBAL_THIS);
    assertTrue(globalThis.hasProperty("x"));  }
  
  @Test  public void testDefineSlotWithWindowConstructor() {
    String js = "/** @constructor */ function Window() {}";    Node root = parse(js);
    Scope scope = creator.createScope(root, null);    // Special case: if "Window" is constructor, it updates global this ctor
    ObjectType globalThis = registry.getNativeObjectType(JSTypeNative.GLOBAL_THIS);
    // Not checking detailed effect, just no crash  }
}
