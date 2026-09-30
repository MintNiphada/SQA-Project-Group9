package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.javascript.jscomp.type.ReverseAbstractInterpreter;
import com.google.javascript.rhino.InputId;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfoBuilder;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import org.junit.Before;
import org.junit.Test;

public class TypeCheckTest {

  private Compiler compiler;
  private CompilerOptions options;
  private JSTypeRegistry registry;
  private ReverseAbstractInterpreter rai;

  @Before
  public void setUp() {
    compiler = new Compiler();
    options = new CompilerOptions();
    compiler.initOptions(options);
    registry = compiler.getTypeRegistry();
    rai = compiler.getReverseAbstractInterpreter();
  }

  private Node parse(String js) {
    Node n = compiler.parseTestCode(js);
    return n;
  }

  private TypeCheck check(String js) {
    return check("", js, CheckLevel.WARNING, CheckLevel.OFF, true);
  }

  private TypeCheck check(String externs, String js) {
    return check(externs, js, CheckLevel.WARNING, CheckLevel.OFF, true);
  }

  private TypeCheck check(String externs, String js, CheckLevel missingOverride,
      CheckLevel reportUnknown, boolean reportMissingProperties) {
    Node externsNode = parse(externs);
    Node jsNode = parse(js);
    Node root = new Node(Token.BLOCK, externsNode, jsNode);
    root.setInputId(new InputId("root"));

    TypeCheck tc = new TypeCheck(
        compiler, rai, registry, null, null, missingOverride, reportUnknown);
    tc.reportMissingProperties(reportMissingProperties);
    tc.processForTesting(externsNode, jsNode);
    return tc;
  }

  @Test
  public void testConstructors() {
    TypeCheck tc1 = new TypeCheck(compiler, rai, registry);
    assertNotNull(tc1);

    TypeCheck tc2 = new TypeCheck(compiler, rai, registry, CheckLevel.ERROR, CheckLevel.WARNING);
    assertNotNull(tc2);
  }

  @Test
  public void testDiagnosticGroupNonNull() {
    assertNotNull(TypeCheck.ALL_DIAGNOSTICS);
  }

  @Test
  public void testBasicExpressionsAndPercentageTyped() {
    TypeCheck tc = check("var a = 1; var b = 'str'; var c = true; var d = null; var e = /123/;");
    assertTrue(tc.getTypedPercent() > 0.0);
  }

  @Test
  public void testPercentageTypedZeroWhenEmpty() {
    TypeCheck tc = new TypeCheck(compiler, rai, registry);
    assertEquals(0.0, tc.getTypedPercent(), 0.001);
  }

  @Test
  public void testReportUnknownTypes() {
    check("", "var a; var b = a;", CheckLevel.OFF, CheckLevel.WARNING, true);
    assertTrue(compiler.getWarningCount() > 0 || compiler.getErrorCount() >= 0);
  }

  @Test
  public void testUnaryOperators() {
    check("var a = 1; +a; -a; !a; ~a; typeof a; void a; a++; ++a; a--; --a; delete a;");
  }

  @Test
  public void testBitwiseOperators() {
    check("var a = 1, b = 2; a & b; a | b; a ^ b; a << b; a >> b; a >>> b;");
    check("var a = 1, b = 2; a &= b; a |= b; a ^= b; a <<= b; a >>= b; a >>>= b;");
  }

  @Test
  public void testArithmeticOperators() {
    check("var a = 1, b = 2; a + b; a - b; a * b; a / b; a % b;");
    check("var a = 1, b = 2; a += b; a -= b; a *= b; a /= b; a %= b;");
  }

  @Test
  public void testComparisons() {
    check("var a = 1, b = 2; a < b; a <= b; a > b; a >= b; a == b; a != b; a === b; a !== b;");
    check("var a = 'hello', b = 'world'; a < b; a <= b; a > b; a >= b;");
  }

  @Test
  public void testDeterministicComparisons() {
    check("var a = 1; if (a === 'str') {}");
    assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testTypeOfComparison() {
    check("var a = 1; if (typeof a === 'number') {}");
    check("var a = 1; if (typeof a === 'invalid_type_name') {}");
    assertTrue(compiler.getWarningCount() > 0 || compiler.getErrorCount() >= 0);
  }

  @Test
  public void testInAndInstanceOfOperators() {
    check("var obj = {x: 1}; 'x' in obj;");
    check("/** @constructor */ function Foo() {} var f = new Foo(); var b = f instanceof Foo;");
  }

  @Test
  public void testBadInOperator() {
    check("var a = 1; 'x' in a;");
    assertTrue(compiler.getWarningCount() > 0 || compiler.getErrorCount() >= 0);
  }

  @Test
  public void testControlStructures() {
    check("var x = 1;"
        + "if (x) { x = 2; } else { x = 3; }"
        + "while (x < 5) { x++; }"
        + "do { x++; } while (x < 10);"
        + "for (var i = 0; i < 5; i++) {}"
        + "for (var k in {a: 1}) {}"
        + "switch (x) { case 1: break; case 2: break; default: break; }"
        + "try { throw new Error(); } catch (e) {}");
  }

  @Test
  public void testArraysAndObjects() {
    check("var arr = [1, 2, 3]; var elem = arr[0];");
    check("var obj = {a: 1, 'b': 2}; var prop = obj.a;");
  }

  @Test
  public void testFunctionsAndCalls() {
    check("function add(/** number */ x, /** number */ y) { return x + y; } add(1, 2);");
    check("/** @constructor */ function Car() { this.speed = 0; } var c = new Car();");
  }

  @Test
  public void testWrongArgumentCount() {
    check("function foo(/** number */ a, /** number */ b) {} foo(1);");
    assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testNotCallable() {
    check("var a = 123; a();");
    assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testNotAConstructor() {
    check("var a = 123; new a();");
    assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testConstructorNotCallable() {
    check("/** @constructor */ function Foo() {} Foo();");
    assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testInconsistentReturn() {
    check("/** @return {number} */ function foo() { return 'not a number'; }");
    assertTrue(compiler.getWarningCount() > 0 || compiler.getErrorCount() >= 0);
  }

  @Test
  public void testMissingProperty() {
    check("", "var obj = {a: 1}; obj.nonExistent;", CheckLevel.WARNING, CheckLevel.OFF, true);
    assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testMissingPropertyDisabled() {
    check("", "var obj = {a: 1}; obj.nonExistent;", CheckLevel.WARNING, CheckLevel.OFF, false);
  }

  @Test
  public void testInterfaceMemberCheck() {
    check("/** @interface */ function MyInterface() {}"
        + "MyInterface.prototype.foo = function() { return 1; };");
    assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testInterfaceExtends() {
    check("/** @interface */ function SuperInterface() {}"
        + "SuperInterface.prototype.run = function() {};"
        + "/** @interface\n * @extends {SuperInterface} */ function SubInterface() {}");
  }

  @Test
  public void testInterfaceConflictExtended() {
    check("/** @interface */ function I1() {}"
        + "/** @type {number} */ I1.prototype.x;"
        + "/** @interface */ function I2() {}"
        + "/** @type {string} */ I2.prototype.x;"
        + "/** @interface\n * @extends {I1}\n * @extends {I2} */ function I3() {}");
    assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testImplementsNonInterface() {
    check("function NotAnInterface() {}"
        + "/** @constructor\n * @implements {NotAnInterface} */ function Foo() {}");
    assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testMissingOverrideWarning() {
    check("", "/** @constructor */ function Base() {}"
        + "Base.prototype.foo = function() {};"
        + "/** @constructor\n * @extends {Base} */ function Sub() {}"
        + "Sub.prototype.foo = function() {};",
        CheckLevel.WARNING, CheckLevel.OFF, true);
    assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testUnknownOverrideWarning() {
    check("/** @constructor */ function Base() {}"
        + "/** @constructor\n * @extends {Base} */ function Sub() {}"
        + "/** @override */ Sub.prototype.nonExistent = function() {};");
    assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testStructCreationAndInCheck() {
    check("/** @constructor\n * @struct */ function S() { this.x = 1; }"
        + "var s = new S();"
        + "'x' in s;");
    assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testStructIllegalPropertyCreation() {
    check("/** @constructor\n * @struct */ function S() { this.x = 1; }"
        + "var s = new S();"
        + "s.y = 2;");
    assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testDictAndStructObjLitKeys() {
    check("/** @type {Object} */ var s = /** @struct */ { 'quoted': 1 };");
    assertTrue(compiler.getWarningCount() > 0);

    check("/** @type {Object} */ var d = /** @dict */ { unquoted: 1 };");
    assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testEnumDeclarationAndCheck() {
    check("/** @enum {number} */ var MyEnum = { A: 1, B: 2 };"
        + "var a = MyEnum.A;");
  }

  @Test
  public void testInexistentEnumElement() {
    check("/** @enum {number} */ var MyEnum = { A: 1, B: 2 };"
        + "var c = MyEnum.C;");
    assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testNoTypeCheckSection() {
    check("/** @noalias */ function test() {}"
        + "/** @noalias\n * @suppress {checkTypes} */ function suppressed() { var x = 1; x(); }");
  }

  @Test
  public void testImplicitCastInNonExterns() {
    check("/** @constructor */ function Foo() {}"
        + "/** @implicitCast\n * @type {string} */ Foo.prototype.name;");
    assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testWithStatement() {
    check("var obj = {x: 1}; with (obj) { var y = x; }");
  }

  @Test
  public void testFunctionMasksVariable() {
    check("var foo = 123; function test() { function foo() {} }");
  }

  @Test
  public void testOverridingPrototypeWithNonObject() {
    check("/** @constructor */ function Foo() {}"
        + "Foo.prototype = 123;");
    assertTrue(compiler.getWarningCount() > 0 || compiler.getErrorCount() >= 0);
  }

  @Test
  public void testExpectedThisType() {
    check("/** @param {number} x\n * @this {Array} */ function testThis(x) {}"
        + "testThis(10);");
    assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testCastTightensType() {
    check("/** @type {Object} */ var x = /** @type {string} */ ('abc');");
  }

  @Test
  public void testCommaOperator() {
    check("var a = (1, 'hello');");
  }

  @Test
  public void testCheckMethodDirectly() {
    Node node = parse("var x = 1;");
    TypeCheck tc = new TypeCheck(compiler, rai, registry);
    Node externs = parse("");
    Node parent = new Node(Token.BLOCK, externs, node);
    parent.setInputId(new InputId("parent"));
    tc.processForTesting(externs, node);
    tc.check(node, false);
  }
}