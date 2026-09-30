package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import org.junit.Before;
import org.junit.Test;

public class TypeCheckTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
  }

  private TypeCheck createTypeCheck() {
    JSTypeRegistry registry = compiler.getTypeRegistry();
    ReverseAbstractInterpreter rai = compiler.getReverseAbstractInterpreter();
    return new TypeCheck(compiler, rai, registry);
  }

  private TypeCheck createTypeCheck(CheckLevel missingOverride, CheckLevel unknownTypes) {
    JSTypeRegistry registry = compiler.getTypeRegistry();
    ReverseAbstractInterpreter rai = compiler.getReverseAbstractInterpreter();
    return new TypeCheck(compiler, rai, registry, missingOverride, unknownTypes);
  }

  private void check(String js) {
    TypeCheck typeCheck = createTypeCheck();
    check(typeCheck, js, "");
  }

  private void check(TypeCheck typeCheck, String js, String externsJs) {
    Node externsAndJsRoot = new Node(Token.BLOCK);
    Node externsRoot = compiler.parseTestCode(externsJs);
    Node jsRoot = compiler.parseTestCode(js);
    externsAndJsRoot.addChildToBack(externsRoot);
    externsAndJsRoot.addChildToBack(jsRoot);

    typeCheck.processForTesting(externsRoot, jsRoot);
  }

  @Test
  public void testBasicPrimitives() {
    check("var b = true; var f = false; var n = null; var num = 123; var str = 'test'; var v = void 0;");
    assertEquals(0, compiler.getWarningCount());
    assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testArrayAndRegExp() {
    check("var arr = [1, 2, 3]; var re = /ab+c/;");
    assertEquals(0, compiler.getWarningCount());
    assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testUnaryOperators() {
    check("var a = 5; a++; ++a; a--; --a; var b = +a; var c = -a; var d = !b; var e = typeof a; var f = ~a;");
    assertEquals(0, compiler.getWarningCount());
    assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testBinaryArithmeticAndBitwise() {
    check("var x = 1 + 2 - 3 * 4 / 5 % 6; var y = 1 << 2 >> 3 >>> 4; var z = 1 & 2 | 3 ^ 4;");
    assertEquals(0, compiler.getWarningCount());
    assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testCompoundAssignments() {
    check("var a = 1; a += 2; a -= 2; a *= 2; a /= 2; a %= 2; a <<= 1; a >>= 1; a >>>= 1; a &= 1; a |= 1; a ^= 1;");
    assertEquals(0, compiler.getWarningCount());
    assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testRelationalComparisons() {
    check("var b1 = 1 < 2; var b2 = 1 <= 2; var b3 = 2 > 1; var b4 = 2 >= 1; var b5 = 'a' < 'b';");
    assertEquals(0, compiler.getWarningCount());
    assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testEqualityComparisons() {
    check("var a = 1; var b = 2; var eq = (a == b); var ne = (a != b); var sheq = (a === b); var shne = (a !== b);");
    assertEquals(0, compiler.getWarningCount());
    assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testDeterministicEqualityWarning() {
    check("var x = 1 === 'string';");
    assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testInAndInstanceofOperators() {
    check("var obj = {a: 1}; var res = 'a' in obj; var isInst = obj instanceof Object;");
    assertEquals(0, compiler.getWarningCount());
    assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testObjectLiteralAndPropertyAccess() {
    TypeCheck tc = createTypeCheck();
    tc.reportMissingProperties(false);
    check(tc, "var obj = {x: 1, y: 'foo'}; var val = obj.x; var elem = obj['y'];", "");
    assertEquals(0, compiler.getWarningCount());
    assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testFunctionsAndCalls() {
    check("function add(a, b) { return a + b; } var res = add(1, 2);");
    assertEquals(0, compiler.getWarningCount());
    assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testConstructorAndNew() {
    check("/** @constructor */ function Foo() { this.x = 1; } var f = new Foo();");
    assertEquals(0, compiler.getWarningCount());
    assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testNewNonConstructor() {
    check("var notCtor = 123; var inst = new notCtor();");
    assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testDeleteOperator() {
    check("var obj = {p: 1}; delete obj.p;");
    assertEquals(0, compiler.getWarningCount());
    check("delete (1 + 2);");
    assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testSwitchAndCase() {
    check("var x = 10; switch (x) { case 10: break; default: break; }");
    assertEquals(0, compiler.getWarningCount());
    assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testWithStatement() {
    check("var obj = {a: 1}; with (obj) { var x = a; }");
    assertEquals(0, compiler.getWarningCount());
    assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testControlStructures() {
    check("var i = 0; if (i < 1) { i++; } while (i < 2) { i++; } do { i++; } while (i < 3); for (var j = 0; j < 5; j++) {}");
    assertEquals(0, compiler.getWarningCount());
    assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testLogicalExpressions() {
    check("var a = true && false; var b = true || false; var c = a ? 1 : 2;");
    assertEquals(0, compiler.getWarningCount());
    assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testCommaExpression() {
    check("var x = (1, 2, 3);");
    assertEquals(0, compiler.getWarningCount());
    assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testFunctionReturn() {
    check("/** @return {number} */ function f() { return 1; }");
    assertEquals(0, compiler.getWarningCount());
    assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testTypedPercentCalculation() {
    TypeCheck tc = createTypeCheck();
    check(tc, "var x = 1; var y = 'abc';", "");
    double pct = tc.getTypedPercent();
    assertTrue(pct >= 0.0 && pct <= 100.0);
  }

  @Test
  public void testInitialTypedPercentZero() {
    TypeCheck tc = createTypeCheck();
    assertEquals(0.0, tc.getTypedPercent(), 0.001);
  }

  @Test
  public void testEnumDeclaration() {
    check("/** @enum {number} */ var MyEnum = { A: 1, B: 2 }; var val = MyEnum.A;");
    assertEquals(0, compiler.getWarningCount());
    assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testInterfaceDefinition() {
    check("/** @interface */ function MyInterface() {} MyInterface.prototype.foo = function() {};");
    assertEquals(0, compiler.getWarningCount());
    assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testDiagnosticConstantsNotNull() {
    assertNotNull(TypeCheck.BAD_DELETE);
    assertNotNull(TypeCheck.DETERMINISTIC_TEST);
    assertNotNull(TypeCheck.DETERMINISTIC_TEST_NO_RESULT);
    assertNotNull(TypeCheck.INEXISTENT_ENUM_ELEMENT);
    assertNotNull(TypeCheck.INEXISTENT_PROPERTY);
    assertNotNull(TypeCheck.NOT_A_CONSTRUCTOR);
    assertNotNull(TypeCheck.BIT_OPERATION);
    assertNotNull(TypeCheck.NOT_CALLABLE);
    assertNotNull(TypeCheck.CONSTRUCTOR_NOT_CALLABLE);
    assertNotNull(TypeCheck.FUNCTION_MASKS_VARIABLE);
    assertNotNull(TypeCheck.MULTIPLE_VAR_DEF);
    assertNotNull(TypeCheck.ENUM_DUP);
    assertNotNull(TypeCheck.ENUM_NOT_CONSTANT);
    assertNotNull(TypeCheck.INVALID_INTERFACE_MEMBER_DECLARATION);
    assertNotNull(TypeCheck.INTERFACE_FUNCTION_NOT_EMPTY);
    assertNotNull(TypeCheck.CONFLICTING_EXTENDED_TYPE);
    assertNotNull(TypeCheck.CONFLICTING_IMPLEMENTED_TYPE);
    assertNotNull(TypeCheck.BAD_IMPLEMENTED_TYPE);
    assertNotNull(TypeCheck.HIDDEN_SUPERCLASS_PROPERTY);
    assertNotNull(TypeCheck.HIDDEN_INTERFACE_PROPERTY);
    assertNotNull(TypeCheck.HIDDEN_SUPERCLASS_PROPERTY_MISMATCH);
    assertNotNull(TypeCheck.UNKNOWN_OVERRIDE);
    assertNotNull(TypeCheck.INTERFACE_METHOD_OVERRIDE);
    assertNotNull(TypeCheck.UNKNOWN_EXPR_TYPE);
    assertNotNull(TypeCheck.UNRESOLVED_TYPE);
    assertNotNull(TypeCheck.WRONG_ARGUMENT_COUNT);
    assertNotNull(TypeCheck.ILLEGAL_IMPLICIT_CAST);
    assertNotNull(TypeCheck.INCOMPATIBLE_EXTENDED_PROPERTY_TYPE);
    assertNotNull(TypeCheck.EXPECTED_THIS_TYPE);
    assertNotNull(TypeCheck.ALL_DIAGNOSTICS);
  }

  @Test
  public void testNoTypeCheckAnnotation() {
    check("/** @noalias */ function test() {}\n/** @notypecheck */ function fn() { var x = 1; x(); }");
    assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testReportUnknownTypes() {
    TypeCheck tc = createTypeCheck(CheckLevel.WARNING, CheckLevel.WARNING);
    check(tc, "function f(unknownParam) { var x = unknownParam; }", "");
    assertTrue(tc.getTypedPercent() >= 0.0);
  }
}