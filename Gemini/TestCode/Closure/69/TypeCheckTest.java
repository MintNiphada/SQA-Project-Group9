package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TypeCheckTest {

  private Compiler compiler;
  private TypeCheck typeCheck;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.checkTypes = true;
    compiler.initOptions(options);
  }

  private TypeCheck createTypeCheck(CheckLevel missingOverride, CheckLevel unknownTypes) {
    JSTypeRegistry registry = compiler.getTypeRegistry();
    SemanticReverseAbstractInterpreter rai =
        new SemanticReverseAbstractInterpreter(compiler.getCodingConvention(), registry);
    return new TypeCheck(compiler, rai, registry, missingOverride, unknownTypes);
  }

  private Scope testTypes(String js) {
    return testTypes("", js, CheckLevel.WARNING, CheckLevel.OFF);
  }

  private Scope testTypes(String externs, String js) {
    return testTypes(externs, js, CheckLevel.WARNING, CheckLevel.OFF);
  }

  private Scope testTypes(String externs, String js, CheckLevel missingOverride, CheckLevel unknownTypes) {
    typeCheck = createTypeCheck(missingOverride, unknownTypes);
    Node externsNode = compiler.parseSyntheticCode("externs", externs);
    Node jsNode = compiler.parseSyntheticCode("testcode", js);
    Node parent = new Node(Token.BLOCK, externsNode, jsNode);
    return typeCheck.processForTesting(externsNode, jsNode);
  }

  private void assertWarning(DiagnosticType type) {
    JSError[] warnings = compiler.getWarnings();
    for (JSError warning : warnings) {
      if (warning.getType() == type) {
        return;
      }
    }
    Assert.fail("Expected warning of type " + type + " but found: " + java.util.Arrays.toString(warnings));
  }

  private void assertError(DiagnosticType type) {
    JSError[] errors = compiler.getErrors();
    for (JSError error : errors) {
      if (error.getType() == type) {
        return;
      }
    }
    Assert.fail("Expected error of type " + type + " but found: " + java.util.Arrays.toString(errors));
  }

  private void assertNoWarningsOrErrors() {
    Assert.assertEquals("Expected no errors, got: " + java.util.Arrays.toString(compiler.getErrors()),
        0, compiler.getErrorCount());
    Assert.assertEquals("Expected no warnings, got: " + java.util.Arrays.toString(compiler.getWarnings()),
        0, compiler.getWarningCount());
  }

  @Test
  public void testBasicTypesAndLiterals() {
    testTypes(
        "var a = true;\n" +
        "var b = false;\n" +
        "var c = null;\n" +
        "var d = 42;\n" +
        "var e = 'hello';\n" +
        "var f = [1, 2, 3];\n" +
        "var g = /abc/;\n" +
        "var h = void 0;\n" +
        "var i = typeof d;\n" +
        "var j = !a;\n");
    assertNoWarningsOrErrors();
    Assert.assertTrue(typeCheck.getTypedPercent() > 0.0);
  }

  @Test
  public void testBinaryArithmeticAndBitwise() {
    testTypes(
        "var a = 10 + 20;\n" +
        "var b = 10 - 5;\n" +
        "var c = 10 * 2;\n" +
        "var d = 10 / 2;\n" +
        "var e = 10 % 3;\n" +
        "var f = 10 & 2;\n" +
        "var g = 10 | 2;\n" +
        "var h = 10 ^ 2;\n" +
        "var i = 10 << 2;\n" +
        "var j = 10 >> 2;\n" +
        "var k = 10 >>> 2;\n" +
        "var l = ~10;\n" +
        "var m = +10;\n" +
        "var n = -10;\n" +
        "a++;\n" +
        "b--;\n" +
        "a += 1;\n" +
        "a -= 1;\n" +
        "a *= 2;\n" +
        "a /= 2;\n" +
        "a %= 2;\n" +
        "a &= 2;\n" +
        "a |= 2;\n" +
        "a ^= 2;\n" +
        "a <<= 1;\n" +
        "a >>= 1;\n" +
        "a >>>= 1;\n");
    assertNoWarningsOrErrors();
  }

  @Test
  public void testBitwiseOperationWarning() {
    testTypes("var a = 'str'; var b = ~a;");
    assertWarning(TypeCheck.BIT_OPERATION);
  }

  @Test
  public void testShiftOperationWarning() {
    testTypes("var a = 'str'; var b = a << 2;");
    assertWarning(TypeCheck.BIT_OPERATION);

    setUp();
    testTypes("var a = 'str'; var b = 2 >> a;");
    assertWarning(TypeCheck.BIT_OPERATION);
  }

  @Test
  public void testComparisons() {
    testTypes(
        "var a = 1 < 2;\n" +
        "var b = 1 <= 2;\n" +
        "var c = 1 > 2;\n" +
        "var d = 1 >= 2;\n" +
        "var e = 'a' < 'b';\n");
    assertNoWarningsOrErrors();
  }

  @Test
  public void testEqualityDeterministicWarning() {
    testTypes("var a = 1 == 'str';");
    assertWarning(TypeCheck.DETERMINISTIC_TEST);

    setUp();
    testTypes("var a = 1 != 'str';");
    assertWarning(TypeCheck.DETERMINISTIC_TEST);
  }

  @Test
  public void testStrictEqualityDeterministicWarning() {
    testTypes("var a = 1 === 'str';");
    assertWarning(TypeCheck.DETERMINISTIC_TEST_NO_RESULT);
  }

  @Test
  public void testInAndInstanceof() {
    testTypes(
        "var obj = {x: 1};\n" +
        "var res = 'x' in obj;\n" +
        "/** @constructor */ function Foo() {}\n" +
        "var inst = new Foo();\n" +
        "var isInst = inst instanceof Foo;\n");
    assertNoWarningsOrErrors();
  }

  @Test
  public void testDeleteOperator() {
    testTypes(
        "var obj = {x: 1};\n" +
        "delete obj.x;\n" +
        "delete obj['x'];\n");
    assertNoWarningsOrErrors();
  }

  @Test
  public void testBadDeleteOperator() {
    testTypes("delete (1 + 2);");
    assertWarning(TypeCheck.BAD_DELETE);
  }

  @Test
  public void testCommaAndHookAndLogical() {
    testTypes(
        "var a = (1, 2);\n" +
        "var b = true ? 1 : 2;\n" +
        "var c = true && false;\n" +
        "var d = true || false;\n");
    assertNoWarningsOrErrors();
  }

  @Test
  public void testObjectLitKeysAndProperties() {
    testTypes(
        "/** @type {{a: number, b: string}} */\n" +
        "var obj = {a: 1, b: 'two'};\n" +
        "var x = obj.a;\n" +
        "var y = obj['b'];\n");
    assertNoWarningsOrErrors();
  }

  @Test
  public void testFunctionCallsAndConstructors() {
    testTypes(
        "/** @param {number} x \n @return {number} */\n" +
        "function f(x) { return x + 1; }\n" +
        "var res = f(10);\n" +
        "/** @constructor \n @param {string} msg */\n" +
        "function MyClass(msg) { this.msg = msg; }\n" +
        "var inst = new MyClass('hello');\n");
    assertNoWarningsOrErrors();
  }

  @Test
  public void testWrongArgumentCountWarning() {
    testTypes(
        "/** @param {number} x \n @param {number} y */\n" +
        "function f(x, y) {}\n" +
        "f(1);\n");
    assertWarning(TypeCheck.WRONG_ARGUMENT_COUNT);
  }

  @Test
  public void testNotCallableWarning() {
    testTypes("var x = 123; x();");
    assertWarning(TypeCheck.NOT_CALLABLE);
  }

  @Test
  public void testConstructorNotCallableWarning() {
    testTypes(
        "/** @constructor */\n" +
        "function Foo() {}\n" +
        "Foo();\n");
    assertWarning(TypeCheck.CONSTRUCTOR_NOT_CALLABLE);
  }

  @Test
  public void testNotAConstructorWarning() {
    testTypes(
        "var notCtor = 123;\n" +
        "var x = new notCtor();\n");
    assertWarning(TypeCheck.NOT_A_CONSTRUCTOR);
  }

  @Test
  public void testFunctionMasksVariable() {
    testTypes(
        "var x = 10;\n" +
        "function f() {\n" +
        "  function x() {}\n" +
        "}\n");
    assertWarning(TypeCheck.FUNCTION_MASKS_VARIABLE);
  }

  @Test
  public void testEnumChecking() {
    testTypes(
        "/** @enum {number} */\n" +
        "var MyEnum = {\n" +
        "  A: 1,\n" +
        "  B: 2\n" +
        "};\n" +
        "var val = MyEnum.A;\n");
    assertNoWarningsOrErrors();
  }

  @Test
  public void testInexistentEnumElement() {
    testTypes(
        "/** @enum {number} */\n" +
        "var MyEnum = { A: 1 };\n" +
        "var x = MyEnum.NON_EXISTENT;\n");
    assertWarning(TypeCheck.INEXISTENT_ENUM_ELEMENT);
  }

  @Test
  public void testEnumCopy() {
    testTypes(
        "/** @enum {number} */\n" +
        "var Enum1 = { A: 1 };\n" +
        "/** @enum {number} */\n" +
        "var Enum2 = Enum1;\n");
    assertNoWarningsOrErrors();
  }

  @Test
  public void testInterfaceDeclarationAndImplementation() {
    testTypes(
        "/** @interface */\n" +
        "function AnInterface() {}\n" +
        "AnInterface.prototype.doSomething = function() {};\n" +
        "/** @constructor \n @implements {AnInterface} */\n" +
        "function AnImpl() {}\n" +
        "AnImpl.prototype.doSomething = function() {};\n");
    assertNoWarningsOrErrors();
  }

  @Test
  public void testInterfaceFunctionNotEmpty() {
    testTypes(
        "/** @interface */\n" +
        "function AnInterface() {}\n" +
        "AnInterface.prototype.foo = function() { return 1; };\n");
    assertWarning(TypeCheck.INTERFACE_FUNCTION_NOT_EMPTY);
  }

  @Test
  public void testConflictingExtendedType() {
    testTypes(
        "/** @interface */\n" +
        "function Inter() {}\n" +
        "/** @constructor \n @extends {Inter} */\n" +
        "function Clazz() {}\n");
    assertWarning(TypeCheck.CONFLICTING_EXTENDED_TYPE);
  }

  @Test
  public void testConflictingImplementedType() {
    testTypes(
        "/** @interface */\n" +
        "function Inter1() {}\n" +
        "/** @interface \n @implements {Inter1} */\n" +
        "function Inter2() {}\n");
    assertWarning(TypeCheck.CONFLICTING_IMPLEMENTED_TYPE);
  }

  @Test
  public void testBadImplementedType() {
    testTypes(
        "/** @constructor */\n" +
        "function NotAnInterface() {}\n" +
        "/** @constructor \n @implements {NotAnInterface} */\n" +
        "function Clazz() {}\n");
    assertWarning(TypeCheck.BAD_IMPLEMENTED_TYPE);
  }

  @Test
  public void testOverrideWarnings() {
    testTypes(
        "",
        "/** @constructor */ function Parent() {}\n" +
        "Parent.prototype.foo = function() {};\n" +
        "/** @constructor \n @extends {Parent} */ function Child() {}\n" +
        "Child.prototype.foo = function() {};\n",
        CheckLevel.WARNING,
        CheckLevel.OFF);
    assertWarning(TypeCheck.HIDDEN_SUPERCLASS_PROPERTY);
  }

  @Test
  public void testInterfaceOverrideWarning() {
    testTypes(
        "",
        "/** @interface */ function Intf() {}\n" +
        "Intf.prototype.bar = function() {};\n" +
        "/** @constructor \n @implements {Intf} */ function Impl() {}\n" +
        "Impl.prototype.bar = function() {};\n",
        CheckLevel.WARNING,
        CheckLevel.OFF);
    assertWarning(TypeCheck.HIDDEN_INTERFACE_PROPERTY);
  }

  @Test
  public void testUnknownOverride() {
    testTypes(
        "/** @constructor */ function Parent() {}\n" +
        "/** @constructor \n @extends {Parent} */ function Child() {}\n" +
        "/** @override */ Child.prototype.unknownProp = function() {};\n");
    assertWarning(TypeCheck.UNKNOWN_OVERRIDE);
  }

  @Test
  public void testHiddenSuperclassPropertyMismatch() {
    testTypes(
        "/** @constructor */ function Parent() {}\n" +
        "/** @type {number} */ Parent.prototype.x = 1;\n" +
        "/** @constructor \n @extends {Parent} */ function Child() {}\n" +
        "/** @override \n @type {string} */ Child.prototype.x = 'bad';\n");
    assertWarning(TypeCheck.HIDDEN_SUPERCLASS_PROPERTY_MISMATCH);
  }

  @Test
  public void testIllegalImplicitCast() {
    testTypes(
        "/** @constructor */ function Foo() {}\n" +
        "/** @implicitCast \n @type {number} */ Foo.prototype.bar = 0;\n");
    assertWarning(TypeCheck.ILLEGAL_IMPLICIT_CAST);
  }

  @Test
  public void testNoTypeCheckSection() {
    testTypes(
        "/** @notypecheck */\n" +
        "function uncheckable() {\n" +
        "  var x = 123;\n" +
        "  x();\n" +
        "  var y = ~'string';\n" +
        "}\n");
    assertNoWarningsOrErrors();
  }

  @Test
  public void testReportMissingPropertiesSetting() {
    typeCheck = createTypeCheck(CheckLevel.WARNING, CheckLevel.OFF);
    Assert.assertSame(typeCheck, typeCheck.reportMissingProperties(false));
  }

  @Test
  public void testGetTypedPercent() {
    typeCheck = createTypeCheck(CheckLevel.WARNING, CheckLevel.OFF);
    Assert.assertEquals(0.0, typeCheck.getTypedPercent(), 0.0001);

    testTypes("var x = 1; var y = 'abc';");
    Assert.assertTrue(typeCheck.getTypedPercent() > 0.0);
  }

  @Test
  public void testUnknownTypesReporting() {
    testTypes(
        "",
        "var unknownVar = someNonExistentName;\n",
        CheckLevel.OFF,
        CheckLevel.WARNING);
    assertWarning(TypeCheck.UNKNOWN_EXPR_TYPE);
  }

  @Test
  public void testOverridingPrototypeWithNonObject() {
    testTypes(
        "/** @constructor */ function Foo() {}\n" +
        "Foo.prototype = 123;\n");
    assertWarning(TypeValidator.TYPE_MISMATCH_WARNING);
  }

  @Test
  public void testInterfaceExtendingNonInterface() {
    testTypes(
        "/** @constructor */ function Clazz() {}\n" +
        "/** @interface \n @extends {Clazz} */ function BadInterface() {}\n");
    assertWarning(TypeCheck.CONFLICTING_EXTENDED_TYPE);
  }

  @Test
  public void testMultipleInterfaceConflictProperties() {
    testTypes(
        "/** @interface */ function Intf1() {}\n" +
        "/** @type {number} */ Intf1.prototype.x;\n" +
        "/** @interface */ function Intf2() {}\n" +
        "/** @type {string} */ Intf2.prototype.x;\n" +
        "/** @interface \n @extends {Intf1} \n @extends {Intf2} */ function Combined() {}\n");
    assertWarning(TypeCheck.INCOMPATIBLE_EXTENDED_PROPERTY_TYPE);
  }

  @Test
  public void testSwitchAndCaseStatement() {
    testTypes(
        "var x = 1;\n" +
        "switch (x) {\n" +
        "  case 1: break;\n" +
        "  case 2: break;\n" +
        "  default: break;\n" +
        "}\n");
    assertNoWarningsOrErrors();
  }

  @Test
  public void testWhileDoForLoops() {
    testTypes(
        "var i = 0;\n" +
        "while (i < 5) { i++; }\n" +
        "do { i--; } while (i > 0);\n" +
        "for (var j = 0; j < 5; j++) {}\n" +
        "for (var prop in {a: 1}) {}\n");
    assertNoWarningsOrErrors();
  }

  @Test
  public void testTryCatchFinallyThrow() {
    testTypes(
        "try {\n" +
        "  throw new Error('err');\n" +
        "} catch (e) {\n" +
        "  var msg = e;\n" +
        "} finally {}\n");
    assertNoWarningsOrErrors();
  }

  @Test
  public void testProcessExternsAndJs() {
    TypeCheck tc = createTypeCheck(CheckLevel.WARNING, CheckLevel.OFF);
    Node externsNode = compiler.parseSyntheticCode("externs", "/** @type {number} */ var ext;");
    Node jsNode = compiler.parseSyntheticCode("testcode", "var local = ext + 1;");
    Node root = new Node(Token.BLOCK, externsNode, jsNode);

    MemoizedScopeCreator scopeCreator = new MemoizedScopeCreator(new TypedScopeCreator(compiler));
    Scope topScope = scopeCreator.createScope(root, null);

    TypeInferencePass inference = new TypeInferencePass(compiler,
        new SemanticReverseAbstractInterpreter(compiler.getCodingConvention(), compiler.getTypeRegistry()),
        topScope, scopeCreator);
    inference.process(externsNode, jsNode);

    TypeCheck tc2 = new TypeCheck(compiler,
        new SemanticReverseAbstractInterpreter(compiler.getCodingConvention(), compiler.getTypeRegistry()),
        compiler.getTypeRegistry(),
        topScope,
        scopeCreator,
        CheckLevel.WARNING,
        CheckLevel.OFF);

    tc2.process(externsNode, jsNode);
    Assert.assertTrue(tc2.getTypedPercent() > 0.0);
  }
}