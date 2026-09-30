package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

public class TypeCheckTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    options.setWarningLevel(DiagnosticGroups.CHECK_TYPES, CheckLevel.WARNING);
    compiler.initOptions(options);
  }

  private TypeCheck createTypeChecker() {
    JSTypeRegistry registry = compiler.getTypeRegistry();
    return new TypeCheck(
        compiler,
        compiler.getReverseAbstractInterpreter(),
        registry,
        CheckLevel.WARNING,
        CheckLevel.WARNING);
  }

  private TypeCheck testAndCheck(String js) {
    return testAndCheck("", js);
  }

  private TypeCheck testAndCheck(String externs, String js) {
    Node jsRoot = compiler.parseSyntheticCode("test.js", js);
    Node externsRoot = compiler.parseSyntheticCode("externs.js", externs);
    Node parent = new Node(Token.BLOCK, externsRoot, jsRoot);

    TypeCheck tc = createTypeChecker();
    tc.processForTesting(externsRoot, jsRoot);
    return tc;
  }

  private void assertWarning(String js, DiagnosticType diag) {
    assertWarning("", js, diag);
  }

  private void assertWarning(String externs, String js, DiagnosticType diag) {
    testAndCheck(externs, js);
    boolean found = false;
    for (JSError warning : compiler.getWarnings()) {
      if (warning.getType().equals(diag)) {
        found = true;
        break;
      }
    }
    assertTrue("Expected warning " + diag.key + " but got: " + compiler.getWarnings(), found);
  }

  private void assertNoWarningsOrErrors(String js) {
    testAndCheck(js);
    assertEquals("Unexpected errors: " + compiler.getErrors(), 0, compiler.getErrorCount());
    assertEquals("Unexpected warnings: " + compiler.getWarnings(), 0, compiler.getWarningCount());
  }

  @Test
  public void testAllDiagnosticsNotNull() {
    assertNotNull(TypeCheck.ALL_DIAGNOSTICS);
    Iterator<DiagnosticType> it = TypeCheck.ALL_DIAGNOSTICS.getTypes().iterator();
    assertTrue(it.hasNext());
  }

  @Test
  public void testConstructors() {
    JSTypeRegistry registry = compiler.getTypeRegistry();
    TypeCheck tc1 = new TypeCheck(compiler, compiler.getReverseAbstractInterpreter(), registry);
    assertNotNull(tc1);

    TypeCheck tc2 = new TypeCheck(
        compiler,
        compiler.getReverseAbstractInterpreter(),
        registry,
        CheckLevel.WARNING,
        CheckLevel.WARNING);
    assertNotNull(tc2);
    tc2.reportMissingProperties(false);
  }

  @Test
  public void testBasicTypesAndLiterals() {
    TypeCheck tc = testAndCheck(
        "var a = true;\n" +
        "var b = false;\n" +
        "var c = null;\n" +
        "var d = 42;\n" +
        "var e = 'hello';\n" +
        "var f = [1, 2, 3];\n" +
        "var g = /abc/;\n" +
        "var h = undefined;\n" +
        "var i = typeof 'abc';\n" +
        "var j = void 0;\n" +
        "var k = !true;\n");
    assertTrue(tc.getTypedPercent() >= 0.0);
  }

  @Test
  public void testUnaryOperators() {
    assertNoWarningsOrErrors(
        "var x = 1;\n" +
        "x++;\n" +
        "++x;\n" +
        "x--;\n" +
        "--x;\n" +
        "var y = +x;\n" +
        "var z = -x;\n" +
        "var w = ~x;\n");
  }

  @Test
  public void testBitwiseOperationWarning() {
    assertWarning("var x = ~'hello';", TypeCheck.BIT_OPERATION);
    assertWarning("var x = 'hello' & 1;", TypeCheck.BIT_OPERATION);
    assertWarning("var x = 1 << 'hello';", TypeCheck.BIT_OPERATION);
    assertWarning("var x = 1 >> 'hello';", TypeCheck.BIT_OPERATION);
    assertWarning("var x = 1 >>> 'hello';", TypeCheck.BIT_OPERATION);
    assertWarning("var x = 1; x <<= 'hello';", TypeCheck.BIT_OPERATION);
    assertWarning("var x = 1; x >>= 'hello';", TypeCheck.BIT_OPERATION);
    assertWarning("var x = 1; x >>>= 'hello';", TypeCheck.BIT_OPERATION);
  }

  @Test
  public void testBinaryArithmeticOperators() {
    assertNoWarningsOrErrors(
        "var a = 1 + 2;\n" +
        "var b = 5 - 3;\n" +
        "var c = 2 * 4;\n" +
        "var d = 10 / 2;\n" +
        "var e = 10 % 3;\n" +
        "var f = 1; f += 2; f -= 1; f *= 3; f /= 2; f %= 2;\n" +
        "var g = 1; g &= 2; g |= 3; g ^= 1;\n" +
        "var h = 1 | 2;\n" +
        "var i = 1 ^ 2;\n");
  }

  @Test
  public void testComparisons() {
    assertNoWarningsOrErrors(
        "var a = 1 < 2;\n" +
        "var b = 1 <= 2;\n" +
        "var c = 1 > 2;\n" +
        "var d = 1 >= 2;\n" +
        "var s = 'a' < 'b';\n");
  }

  @Test
  public void testDeterministicEqualityTest() {
    assertWarning("var a = (1 === 'hello');", TypeCheck.DETERMINISTIC_TEST);
    assertWarning("var a = (1 !== 'hello');", TypeCheck.DETERMINISTIC_TEST);
    assertWarning("var a = true; var b = 'x'; var c = (a == b);", TypeCheck.DETERMINISTIC_TEST);
  }

  @Test
  public void testInstanceofAndIn() {
    assertNoWarningsOrErrors(
        "function Foo() {}\n" +
        "var x = new Foo();\n" +
        "var isFoo = x instanceof Foo;\n" +
        "var obj = {a: 1};\n" +
        "var hasA = 'a' in obj;\n");
  }

  @Test
  public void testDeleteAndComma() {
    assertNoWarningsOrErrors(
        "var obj = {a: 1};\n" +
        "delete obj.a;\n" +
        "var x = (1, 2, 3);\n");
  }

  @Test
  public void testCaseAndSwitch() {
    assertNoWarningsOrErrors(
        "var x = 1;\n" +
        "switch (x) {\n" +
        "  case 1:\n" +
        "    break;\n" +
        "  case 2:\n" +
        "    break;\n" +
        "  default:\n" +
        "    break;\n" +
        "}\n");
  }

  @Test
  public void testWithStatement() {
    assertNoWarningsOrErrors(
        "var obj = {a: 1};\n" +
        "with (obj) {\n" +
        "  var b = a;\n" +
        "}\n");
  }

  @Test
  public void testNotCallable() {
    assertWarning("var x = 123; x();", TypeCheck.NOT_CALLABLE);
  }

  @Test
  public void testNotAConstructor() {
    assertWarning("var x = 123; new x();", TypeCheck.NOT_A_CONSTRUCTOR);
  }

  @Test
  public void testConstructorNotCallable() {
    assertWarning(
        "/** @constructor */ function Foo() {}\n" +
        "Foo();\n",
        TypeCheck.CONSTRUCTOR_NOT_CALLABLE);
  }

  @Test
  public void testWrongArgumentCount() {
    assertWarning(
        "/** @param {number} x \n @param {number} y */ function add(x, y) {}\n" +
        "add(1);\n",
        TypeCheck.WRONG_ARGUMENT_COUNT);

    assertWarning(
        "/** @param {number} x */ function foo(x) {}\n" +
        "foo(1, 2, 3);\n",
        TypeCheck.WRONG_ARGUMENT_COUNT);
  }

  @Test
  public void testFunctionMasksVariable() {
    assertWarning(
        "var x = 1;\n" +
        "function foo() {\n" +
        "  function x() {}\n" +
        "}\n",
        TypeCheck.FUNCTION_MASKS_VARIABLE);
  }

  @Test
  public void testExpectedThisType() {
    assertWarning(
        "/** @this {{a: number}} */ function foo() {}\n" +
        "foo();\n",
        TypeCheck.EXPECTED_THIS_TYPE);
  }

  @Test
  public void testInexistentEnumElement() {
    assertWarning(
        "/** @enum {number} */ var MyEnum = { A: 1 };\n" +
        "var x = MyEnum.B;\n",
        TypeCheck.INEXISTENT_ENUM_ELEMENT);
  }

  @Test
  public void testOverridingPrototypeWithNonObject() {
    assertWarning(
        "/** @constructor */ function Foo() {}\n" +
        "Foo.prototype = 123;\n",
        TypeValidator.TYPE_MISMATCH_WARNING);
  }

  @Test
  public void testBadImplementedType() {
    assertWarning(
        "/** @constructor */ function Super() {}\n" +
        "/** @constructor\n @implements {Super} */ function Sub() {}\n",
        TypeCheck.BAD_IMPLEMENTED_TYPE);
  }

  @Test
  public void testConflictingExtendedType() {
    assertWarning(
        "/** @interface */ function Iface() {}\n" +
        "/** @constructor\n @extends {Iface} */ function Sub() {}\n",
        TypeCheck.CONFLICTING_EXTENDED_TYPE);

    assertWarning(
        "/** @constructor */ function Base() {}\n" +
        "/** @interface\n @extends {Base} */ function SubIface() {}\n",
        TypeCheck.CONFLICTING_EXTENDED_TYPE);
  }

  @Test
  public void testConflictingImplementedType() {
    assertWarning(
        "/** @interface */ function Iface1() {}\n" +
        "/** @interface\n @implements {Iface1} */ function Iface2() {}\n",
        TypeCheck.CONFLICTING_IMPLEMENTED_TYPE);
  }

  @Test
  public void testInvalidInterfaceMemberDeclaration() {
    assertWarning(
        "/** @interface */ function Iface() {}\n" +
        "Iface.prototype.foo = 123;\n",
        TypeCheck.INVALID_INTERFACE_MEMBER_DECLARATION);
  }

  @Test
  public void testInterfaceFunctionNotEmpty() {
    assertWarning(
        "/** @interface */ function Iface() {}\n" +
        "Iface.prototype.foo = function() { return 1; };\n",
        TypeCheck.INTERFACE_FUNCTION_NOT_EMPTY);
  }

  @Test
  public void testUnknownOverride() {
    assertWarning(
        "/** @constructor */ function Foo() {}\n" +
        "/** @override */ Foo.prototype.nonExistent = function() {};\n",
        TypeCheck.UNKNOWN_OVERRIDE);
  }

  @Test
  public void testHiddenSuperclassPropertyMismatch() {
    assertWarning(
        "/** @constructor */ function Base() {}\n" +
        "/** @type {number} */ Base.prototype.prop = 1;\n" +
        "/** @constructor\n @extends {Base} */ function Sub() {}\n" +
        "/** @override\n @type {string} */ Sub.prototype.prop = 'abc';\n",
        TypeCheck.HIDDEN_SUPERCLASS_PROPERTY_MISMATCH);
  }

  @Test
  public void testIncompatibleExtendedPropertyType() {
    assertWarning(
        "/** @interface */ function Iface1() {}\n" +
        "/** @type {number} */ Iface1.prototype.x;\n" +
        "/** @interface */ function Iface2() {}\n" +
        "/** @type {string} */ Iface2.prototype.x;\n" +
        "/** @interface\n @extends {Iface1}\n @extends {Iface2} */ function SubIface() {}\n",
        TypeCheck.INCOMPATIBLE_EXTENDED_PROPERTY_TYPE);
  }

  @Test
  public void testIllegalImplicitCast() {
    assertWarning(
        "/** @constructor */ function Foo() {}\n" +
        "/** @implicitCast\n @type {number} */ Foo.prototype.x = 1;\n",
        TypeCheck.ILLEGAL_IMPLICIT_CAST);
  }

  @Test
  public void testNoTypeCheckSection() {
    assertNoWarningsOrErrors(
        "/** @notypecheck */\n" +
        "function foo() {\n" +
        "  var x = 1;\n" +
        "  x = 'str';\n" +
        "  x.nonExistentProperty();\n" +
        "}\n");
  }

  @Test
  public void testTypeofStrings() {
    assertNoWarningsOrErrors(
        "var x = typeof 1 == 'number';\n" +
        "var y = typeof 'a' == 'string';\n" +
        "var z = typeof true == 'boolean';\n" +
        "var w = typeof {} == 'object';\n" +
        "var u = typeof undefined == 'undefined';\n" +
        "var v = typeof function(){} == 'function';\n");

    assertWarning("var x = typeof 1 == 'invalid_type_string';", TypeValidator.UNKNOWN_TYPEOF_VALUE);
  }

  @Test
  public void testObjectLiterals() {
    assertNoWarningsOrErrors(
        "var obj = {\n" +
        "  a: 1,\n" +
        "  b: 'hello',\n" +
        "  get c() { return 1; },\n" +
        "  set c(v) {}\n" +
        "};\n");
  }

  @Test
  public void testReturnStatements() {
    assertNoWarningsOrErrors(
        "/** @return {number} */ function f1() { return 1; }\n" +
        "/** @return {void} */ function f2() { return; }\n");

    assertWarning(
        "/** @return {number} */ function f3() { return 'not a number'; }\n",
        TypeValidator.TYPE_MISMATCH_WARNING);
  }

  @Test
  public void testControlFlowConstructs() {
    assertNoWarningsOrErrors(
        "var a = true;\n" +
        "if (a) { var x = 1; }\n" +
        "while (a) { break; }\n" +
        "do { continue; } while(false);\n" +
        "for (var i = 0; i < 10; i++) {}\n" +
        "try { throw 1; } catch (e) {} finally {}\n" +
        "label: for (;;) { break label; }\n");
  }

  @Test
  public void testLogicalAndHookOperators() {
    assertNoWarningsOrErrors(
        "var a = true && false;\n" +
        "var b = true || false;\n" +
        "var c = true ? 1 : 2;\n");
  }

  @Test
  public void testGetElem() {
    assertNoWarningsOrErrors(
        "var arr = [1, 2, 3];\n" +
        "var val = arr[0];\n" +
        "var map = {'key': 'value'};\n" +
        "var v = map['key'];\n");
  }
}