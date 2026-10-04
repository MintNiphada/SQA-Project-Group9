package com.google.javascript.jscomp;

import com.google.javascript.rhino.JSDocInfo;
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
  private JSTypeRegistry registry;

  @Before
  public void setUp() {
    compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    registry = compiler.getTypeRegistry();
  }

  private Node parseAndTypeCheck(String js) {
    return parseAndTypeCheck("", js);
  }

  private Node parseAndTypeCheck(String externs, String js) {
    Node externsNode = compiler.parseTestCode(externs);
    Node jsNode = compiler.parseTestCode(js);
    Node root = new Node(Token.BLOCK, externsNode, jsNode);
    TypeCheck typeCheck = new TypeCheck(compiler, null, registry);
    typeCheck.processForTesting(externsNode, jsNode);
    return jsNode;
  }

  @Test
  public void testConstructorAndGetTypedPercentInitial() {
    TypeCheck tc = new TypeCheck(compiler, null, registry);
    Assert.assertEquals(0.0, tc.getTypedPercent(), 0.001);
  }

  @Test
  public void testReportMissingPropertiesChaining() {
    TypeCheck tc = new TypeCheck(compiler, null, registry);
    Assert.assertSame(tc, tc.reportMissingProperties(false));
    Assert.assertSame(tc, tc.reportMissingProperties(true));
  }

  @Test
  public void testBasicLiteralsAndTypes() {
    parseAndTypeCheck("var a = true; var b = false; var c = 123; var d = 'str'; var e = null; var f = [1, 2]; var g = /abc/;");
    Assert.assertEquals(0, compiler.getErrorCount());
    Assert.assertEquals(0, compiler.getWarningCount());
  }

  @Test
  public void testUnaryOperators() {
    parseAndTypeCheck("var x = 10; x++; x--; +x; -x; !x; typeof x; void x; ~x;");
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testBitwiseOperationWarning() {
    parseAndTypeCheck("var s = 'hello'; var res = ~s;");
    Assert.assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testArithmeticBinaryOperators() {
    parseAndTypeCheck("var a = 5 + 2; var b = 5 - 2; var c = 5 * 2; var d = 5 / 2; var e = 5 % 2;");
    Assert.assertEquals(0, compiler.getErrorCount());
    Assert.assertEquals(0, compiler.getWarningCount());
  }

  @Test
  public void testBitwiseBinaryOperators() {
    parseAndTypeCheck("var a = 1 << 2; var b = 1 >> 2; var c = 1 >>> 2; var d = 1 & 2; var e = 1 | 2; var f = 1 ^ 2;");
    Assert.assertEquals(0, compiler.getErrorCount());
    Assert.assertEquals(0, compiler.getWarningCount());
  }

  @Test
  public void testCompoundAssignments() {
    parseAndTypeCheck("var a = 1; a += 2; a -= 2; a *= 2; a /= 2; a %= 2; a <<= 1; a >>= 1; a >>>= 1; a &= 1; a |= 1; a ^= 1;");
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testComparisons() {
    parseAndTypeCheck("var a = 1 < 2; var b = 1 <= 2; var c = 1 > 2; var d = 1 >= 2; var s = 'a' < 'b';");
    Assert.assertEquals(0, compiler.getErrorCount());
    Assert.assertEquals(0, compiler.getWarningCount());
  }

  @Test
  public void testEqualityDeterministicWarning() {
    parseAndTypeCheck("var a = 1 === 'str';");
    Assert.assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testInOperator() {
    parseAndTypeCheck("var obj = {x: 1}; var res = 'x' in obj;");
    Assert.assertEquals(0, compiler.getErrorCount());
    Assert.assertEquals(0, compiler.getWarningCount());
  }

  @Test
  public void testInstanceofOperator() {
    parseAndTypeCheck("function Foo() {} var f = new Foo(); var res = f instanceof Foo;");
    Assert.assertEquals(0, compiler.getErrorCount());
    Assert.assertEquals(0, compiler.getWarningCount());
  }

  @Test
  public void testDeleteOperator() {
    parseAndTypeCheck("var obj = {a: 1}; delete obj.a; delete obj['a'];");
    Assert.assertEquals(0, compiler.getErrorCount());
    Assert.assertEquals(0, compiler.getWarningCount());
  }

  @Test
  public void testBadDeleteOperator() {
    parseAndTypeCheck("delete 1;");
    Assert.assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testFunctionCallAndNew() {
    parseAndTypeCheck("function bar(a, b) { return a + b; } bar(1, 2); var o = new Object();");
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testWrongArgumentCountWarning() {
    parseAndTypeCheck("function bar(a, b) { return a + b; } bar(1);");
    Assert.assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testNotCallableWarning() {
    parseAndTypeCheck("var num = 123; num();");
    Assert.assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testNotAConstructorWarning() {
    parseAndTypeCheck("var notCtor = 123; new notCtor();");
    Assert.assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testConstructorNotCallableWarning() {
    parseAndTypeCheck("/** @constructor */ function MyClass() {} MyClass();");
    Assert.assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testSwitchAndCase() {
    parseAndTypeCheck("var x = 1; switch (x) { case 1: break; case 2: break; default: break; }");
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testControlFlowStatements() {
    parseAndTypeCheck("var i = 0; while (i < 10) { i++; if (i == 5) continue; else break; } do { i--; } while (i > 0); for (var j = 0; j < 5; j++) {}");
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testTryCatchThrow() {
    parseAndTypeCheck("try { throw new Error('err'); } catch (e) {}");
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testWithStatement() {
    parseAndTypeCheck("var obj = {a: 1}; with (obj) { var x = a; }");
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testObjectLiteralKeyValidation() {
    parseAndTypeCheck("var obj = { get a() { return 1; }, set a(val) {} };");
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testNoTypeCheckAnnotation() {
    parseAndTypeCheck("/** @noalias */ function test() {}\n/** @notypecheck */ function bad() { var x = 1; x(); }");
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testGetTypedPercentNonZero() {
    Node jsNode = compiler.parseTestCode("var a = 1; var b = 'test';");
    Node externsNode = compiler.parseTestCode("");
    new Node(Token.BLOCK, externsNode, jsNode);
    TypeCheck tc = new TypeCheck(compiler, null, registry);
    tc.processForTesting(externsNode, jsNode);
    Assert.assertTrue(tc.getTypedPercent() > 0.0);
  }

  @Test
  public void testInterfaceAndImplementsChecks() {
    parseAndTypeCheck("/** @interface */ function Intf() {}\nIntf.prototype.draw = function() {};\n/** @constructor\n * @implements {Intf} */ function Impl() {}\nImpl.prototype.draw = function() {};");
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testBadInterfaceMemberWarning() {
    parseAndTypeCheck("/** @interface */ function Intf() {}\nIntf.prototype.foo = function() { return 1; };");
    Assert.assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testEnumDeclaration() {
    parseAndTypeCheck("/** @enum {number} */ var MyEnum = { A: 1, B: 2 }; var val = MyEnum.A;");
    Assert.assertEquals(0, compiler.getErrorCount());
    Assert.assertEquals(0, compiler.getWarningCount());
  }

  @Test(expected = RuntimeException.class)
  public void testProcessPreconditionFailureNullScope() {
    TypeCheck tc = new TypeCheck(compiler, null, registry);
    Node externs = new Node(Token.BLOCK);
    Node js = new Node(Token.BLOCK);
    new Node(Token.BLOCK, externs, js);
    tc.process(externs, js);
  }
}
