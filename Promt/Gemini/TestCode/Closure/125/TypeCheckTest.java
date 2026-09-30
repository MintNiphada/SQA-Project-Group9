package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TypeCheckTest {

  private Compiler compiler;
  private CompilerOptions options;

  @Before
  public void setUp() {
    compiler = new Compiler();
    options = new CompilerOptions();
    options.setWarningLevel(DiagnosticGroups.CHECK_TYPES, CheckLevel.WARNING);
    compiler.initOptions(options);
  }

  private TypeCheck createTypeCheck() {
    JSTypeRegistry registry = compiler.getTypeRegistry();
    return new TypeCheck(compiler, null, registry, CheckLevel.WARNING);
  }

  private void testTypes(String js) {
    testTypes("", js);
  }

  private void testTypes(String externs, String js) {
    Node externsNode = compiler.parseTestCode(externs);
    Node jsNode = compiler.parseTestCode(js);
    Node root = new Node(Token.BLOCK, externsNode, jsNode);
    TypeCheck check = createTypeCheck();
    check.processForTesting(externsNode, jsNode);
  }

  private void testTypesWarning(String js, DiagnosticType warning) {
    testTypesWarning("", js, warning);
  }

  private void testTypesWarning(String externs, String js, DiagnosticType warning) {
    Node externsNode = compiler.parseTestCode(externs);
    Node jsNode = compiler.parseTestCode(js);
    Node root = new Node(Token.BLOCK, externsNode, jsNode);
    TypeCheck check = createTypeCheck();
    check.processForTesting(externsNode, jsNode);
    Assert.assertTrue("Expected warning " + warning.key + ", but got errors: " +
        java.util.Arrays.toString(compiler.getErrors()) + " and warnings: " +
        java.util.Arrays.toString(compiler.getWarnings()),
        compiler.getWarningCount() > 0 || compiler.getErrorCount() > 0);
  }

  @Test
  public void testBasicExpressions() {
    testTypes("var x = 1; var y = true; var z = 'hello'; var n = null;");
    testTypes("var a = [1, 2, 3]; var r = /abc/;");
    testTypes("var c = (1, 2);");
    testTypes("var o = {a: 1, b: 'str'};");
  }

  @Test
  public void testUnaryOperators() {
    testTypes("var x = 1; x++; ++x; x--; --x; +x; -x; ~x; !x; void x; typeof x;");
    testTypes("delete ({a: 1}).a;");
  }

  @Test
  public void testBinaryOperators() {
    testTypes("var x = 1 + 2; var y = 2 * 3; var z = 4 / 2; var m = 5 % 2; var s = 1 - 2;");
    testTypes("var a = 1 << 2; var b = 4 >> 1; var c = 4 >>> 1;");
    testTypes("var d = 1 & 2; var e = 1 | 2; var f = 1 ^ 2;");
  }

  @Test
  public void testCompoundAssignmentOperators() {
    testTypes("var x = 1; x += 2; x -= 1; x *= 3; x /= 2; x %= 2;");
    testTypes("var x = 1; x <<= 1; x >>= 1; x >>>= 1; x &= 1; x |= 1; x ^= 1;");
  }

  @Test
  public void testComparisons() {
    testTypes("var a = 1 < 2; var b = 1 <= 2; var c = 1 > 2; var d = 1 >= 2;");
    testTypes("var s = 'a' < 'b'; var t = 'a' >= 'b';");
    testTypes("var eq = 1 == 2; var neq = 1 != 2; var seq = 1 === 2; var sneq = 1 !== 2;");
  }

  @Test
  public void testTypeofEvaluation() {
    testTypes("var x = 1; if (typeof x === 'number') {}");
    testTypes("var x = 'abc'; if (typeof x === 'string') {}");
    testTypes("var x = true; if (typeof x === 'boolean') {}");
    testTypes("var x = undefined; if (typeof x === 'undefined') {}");
    testTypes("var x = function() {}; if (typeof x === 'function') {}");
    testTypes("var x = {}; if (typeof x === 'object') {}");
    testTypes("var x = 1; if (typeof x === 'unknown') {}");
  }

  @Test
  public void testLogicalAndHookOperators() {
    testTypes("var a = true && false; var b = true || false; var c = true ? 1 : 2;");
  }

  @Test
  public void testControlStructures() {
    testTypes("if (true) { var a = 1; } else { var a = 2; }");
    testTypes("while (false) { break; continue; }");
    testTypes("do { break; } while (false);");
    testTypes("for (var i = 0; i < 10; i++) {}");
    testTypes("for (var k in {a: 1}) {}");
    testTypes("switch (1) { case 1: break; default: break; }");
    testTypes("try { throw new Error(); } catch (e) {}");
    testTypes("label: while (true) { break label; }");
  }

  @Test
  public void testFunctionCallAndReturn() {
    testTypes("function foo(a, b) { return a + b; } foo(1, 2);");
    testTypes("function bar() { return; } bar();");
  }

  @Test
  public void testConstructorAndNew() {
    testTypes("/** @constructor */ function Foo() { this.x = 1; } var f = new Foo();");
  }

  @Test
  public void testNewOnNonConstructor() {
    testTypesWarning("var x = 1; var y = new x();", TypeCheck.NOT_A_CONSTRUCTOR);
  }

  @Test
  public void testCallNonCallable() {
    testTypesWarning("var x = 1; x();", TypeCheck.NOT_CALLABLE);
  }

  @Test
  public void testWrongArgumentCount() {
    testTypesWarning("function foo(a, b) {} foo(1);", TypeCheck.WRONG_ARGUMENT_COUNT);
    testTypesWarning("function foo(a, b) {} foo(1, 2, 3);", TypeCheck.WRONG_ARGUMENT_COUNT);
  }

  @Test
  public void testInOperator() {
    testTypes("var obj = {a: 1}; var res = 'a' in obj;");
  }

  @Test
  public void testInstanceofOperator() {
    testTypes("/** @constructor */ function Foo() {} var f = new Foo(); var res = f instanceof Foo;");
  }

  @Test
  public void testBitwiseOnNonInteger() {
    testTypesWarning("var x = ~'str';", TypeCheck.BIT_OPERATION);
    testTypesWarning("var x = 'str' << 2;", TypeCheck.BIT_OPERATION);
  }

  @Test
  public void testDeterministicComparison() {
    testTypesWarning(
        "/** @type {number} */ var a = 1; /** @type {string} */ var b = 'str'; var c = (a === b);",
        TypeCheck.DETERMINISTIC_TEST);
  }

  @Test
  public void testGetElem() {
    testTypes("var arr = [1, 2, 3]; var x = arr[0]; arr[1] = 4;");
    testTypes("var obj = {'a': 1}; var x = obj['a'];");
  }

  @Test
  public void testTypeCast() {
    testTypes("var x = /** @type {number} */ (1);");
    testTypes("var obj = /** @type {Object} */ ({a: 1});");
  }

  @Test
  public void testNoTypeCheck() {
    testTypes("/** @noTypeCheck */ function foo() { var x = 1; x(); }");
  }

  @Test
  public void testGetTypedPercent() {
    Node externsNode = compiler.parseTestCode("");
    Node jsNode = compiler.parseTestCode("var a = 1; var b = 'hello';");
    new Node(Token.BLOCK, externsNode, jsNode);
    TypeCheck check = createTypeCheck();
    check.processForTesting(externsNode, jsNode);
    double percent = check.getTypedPercent();
    Assert.assertTrue(percent >= 0.0 && percent <= 100.0);
  }

  @Test
  public void testInterfaceDeclaration() {
    testTypes("/** @interface */ function MyInterface() {} MyInterface.prototype.foo = function() {};");
  }

  @Test
  public void testConstructorImplementsInterface() {
    testTypes(
        "/** @interface */ function MyInterface() {} MyInterface.prototype.foo = function() {};\n" +
        "/** @constructor\n * @implements {MyInterface} */ function MyClass() {}\n" +
        "MyClass.prototype.foo = function() {};");
  }

  @Test
  public void testGetterAndSetter() {
    testTypes("var obj = { get a() { return 1; }, set a(val) {} };");
  }

  @Test
  public void testReportMissingPropertiesConfiguration() {
    TypeCheck check = createTypeCheck();
    TypeCheck result = check.reportMissingProperties(false);
    Assert.assertSame(check, result);
    result = check.reportMissingProperties(true);
    Assert.assertSame(check, result);
  }

  @Test
  public void testProcessPreconditions() {
    TypeCheck check = createTypeCheck();
    Node externs = compiler.parseTestCode("");
    Node js = compiler.parseTestCode("var x = 1;");
    try {
      check.process(externs, js);
      Assert.fail("Expected exception when processing without scopes initialized");
    } catch (NullPointerException expected) {
      // Expected
    } catch (IllegalStateException expected) {
      // Expected
    }
  }

  @Test
  public void testAllDiagnosticsGroup() {
    Assert.assertNotNull(TypeCheck.ALL_DIAGNOSTICS);
    Assert.assertNotNull(TypeCheck.UNEXPECTED_TOKEN);
    Assert.assertNotNull(TypeCheck.DETERMINISTIC_TEST);
    Assert.assertNotNull(TypeCheck.NOT_A_CONSTRUCTOR);
    Assert.assertNotNull(TypeCheck.NOT_CALLABLE);
    Assert.assertNotNull(TypeCheck.WRONG_ARGUMENT_COUNT);
    Assert.assertNotNull(TypeCheck.BIT_OPERATION);
  }
}