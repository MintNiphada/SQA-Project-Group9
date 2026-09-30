package com.google.javascript.jscomp;

import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfoBuilder;
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

import java.lang.reflect.Method;

public class TypeCheckTest {

  private Compiler compiler;
  private JSTypeRegistry registry;
  private ReverseAbstractInterpreter rai;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    registry = compiler.getTypeRegistry();
    rai = compiler.getReverseAbstractInterpreter();
  }

  private Scope check(String js) {
    return check("", js);
  }

  private Scope check(String externs, String js) {
    Node externsRoot = compiler.parseTestCode(externs);
    Node jsRoot = compiler.parseTestCode(js);
    Node parent = new Node(Token.BLOCK, externsRoot, jsRoot);

    TypeCheck typeCheck = new TypeCheck(compiler, rai, registry);
    return typeCheck.processForTesting(externsRoot, jsRoot);
  }

  private Scope checkWithOptions(String js, CheckLevel missingOverride, CheckLevel unknownTypes) {
    Node externsRoot = compiler.parseTestCode("");
    Node jsRoot = compiler.parseTestCode(js);
    Node parent = new Node(Token.BLOCK, externsRoot, jsRoot);

    TypeCheck typeCheck = new TypeCheck(compiler, rai, registry, missingOverride, unknownTypes);
    return typeCheck.processForTesting(externsRoot, jsRoot);
  }

  @Test
  public void testConstructorsAndConfig() {
    TypeCheck tc1 = new TypeCheck(compiler, rai, registry);
    Assert.assertNotNull(tc1);

    TypeCheck tc2 = new TypeCheck(compiler, rai, registry, CheckLevel.ERROR, CheckLevel.WARNING);
    Assert.assertNotNull(tc2);

    TypeCheck tc3 = new TypeCheck(compiler, rai, registry, null, null, CheckLevel.OFF, CheckLevel.OFF);
    Assert.assertSame(tc3, tc3.reportMissingProperties(false));
    Assert.assertSame(tc3, tc3.reportMissingProperties(true));
  }

  @Test
  public void testGetTypedPercent() {
    TypeCheck tc = new TypeCheck(compiler, rai, registry);
    Assert.assertEquals(0.0, tc.getTypedPercent(), 0.0001);

    check("var x = 1; var y = 2;");
    Assert.assertTrue(compiler.getErrorCount() == 0);
  }

  @Test
  public void testAllDiagnosticsGroup() {
    Assert.assertNotNull(TypeCheck.ALL_DIAGNOSTICS);
    Assert.assertTrue(TypeCheck.ALL_DIAGNOSTICS.getTypes().contains(TypeCheck.NOT_A_CONSTRUCTOR));
    Assert.assertTrue(TypeCheck.ALL_DIAGNOSTICS.getTypes().contains(TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test
  public void testBasicExpressions() {
    check("var a = true;\n" +
          "var b = false;\n" +
          "var c = null;\n" +
          "var d = 123;\n" +
          "var e = 'string';\n" +
          "var f = /abc/;\n" +
          "var g = [1, 2, 3];\n" +
          "var h = void 0;\n" +
          "var i = typeof 1;\n" +
          "var j = (1, 2);\n" +
          "var k = !true;");
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testArithmeticAndBitwise() {
    check("var x = 1 + 2;\n" +
          "var y = 1 - 2;\n" +
          "var z = 1 * 2;\n" +
          "var w = 1 / 2;\n" +
          "var m = 1 % 2;\n" +
          "var b1 = 1 & 2;\n" +
          "var b2 = 1 | 2;\n" +
          "var b3 = 1 ^ 2;\n" +
          "var b4 = ~1;\n" +
          "var s1 = 1 << 2;\n" +
          "var s2 = 1 >> 2;\n" +
          "var s3 = 1 >>> 2;");
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testIncrementDecrementAndSign() {
    check("var x = 1;\n" +
          "x++;\n" +
          "++x;\n" +
          "x--;\n" +
          "--x;\n" +
          "var p = +x;\n" +
          "var n = -x;");
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testInvalidBitwiseNot() {
    check("var x = ~'invalid';");
    Assert.assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testComparisons() {
    check("var a = 1 < 2;\n" +
          "var b = 1 <= 2;\n" +
          "var c = 1 > 2;\n" +
          "var d = 1 >= 2;\n" +
          "var s = 'a' < 'b';");
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testEqualityDeterministic() {
    check("var x = 1 == '1';");
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testStrictEqualityWarning() {
    check("var x = (1 === 'string');");
    Assert.assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testInAndInstanceofOperators() {
    check("var obj = {a: 1};\n" +
          "var inTest = 'a' in obj;\n" +
          "var isInst = obj instanceof Object;");
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testDeleteOperator() {
    check("var obj = {a: 1};\n" +
          "delete obj.a;\n" +
          "delete obj['a'];\n" +
          "var x = 1;\n" +
          "delete x;");
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testInvalidDelete() {
    check("delete 1;");
    Assert.assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testFunctionCallAndArguments() {
    check("function foo(a, b) { return a + b; }\n" +
          "foo(1, 2);");
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testWrongArgumentCount() {
    check("function foo(a, b) {}\n" +
          "foo(1);");
    Assert.assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testNonCallableCall() {
    check("var x = 1;\n" +
          "x();");
    Assert.assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testConstructorCallWithoutNew() {
    check("/** @constructor */ function Foo() {}\n" +
          "Foo();");
    Assert.assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testNewOperator() {
    check("/** @constructor */ function Foo() {}\n" +
          "var f = new Foo();");
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testNewNonConstructor() {
    check("var notCtor = 1;\n" +
          "var x = new notCtor();");
    Assert.assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testReturnStatements() {
    check("/** @return {number} */ function f() { return 1; }");
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testReturnMismatch() {
    check("/** @return {number} */ function f() { return 'str'; }");
    Assert.assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testSwitchAndCase() {
    check("switch(1) {\n" +
          "  case 1: break;\n" +
          "  case 2: break;\n" +
          "  default: break;\n" +
          "}");
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testSwitchCaseMismatch() {
    check("switch(1) {\n" +
          "  case 'a': break;\n" +
          "}");
    Assert.assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testWithStatement() {
    check("var obj = {a: 1};\n" +
          "with(obj) {\n" +
          "  var x = 1;\n" +
          "}");
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testEnumDeclarationAndAccess() {
    check("/** @enum {number} */ var MyEnum = { A: 1, B: 2 };\n" +
          "var x = MyEnum.A;");
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testInexistentEnumElement() {
    check("/** @enum {number} */ var MyEnum = { A: 1, B: 2 };\n" +
          "var x = MyEnum.C;");
    Assert.assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testInterfaceDefinitionAndImplementation() {
    check("/** @interface */ function MyInterface() {}\n" +
          "MyInterface.prototype.doSomething = function() {};\n" +
          "/** @constructor @implements {MyInterface} */ function MyClass() {}\n" +
          "MyClass.prototype.doSomething = function() {};");
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testInterfaceNonEmptyFunction() {
    check("/** @interface */ function MyInterface() {}\n" +
          "MyInterface.prototype.doSomething = function() { return 1; };");
    Assert.assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testConflictingExtendedType() {
    check("/** @constructor */ function SuperClass() {}\n" +
          "/** @interface @extends {SuperClass} */ function SubInterface() {}");
    Assert.assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testBadImplementedType() {
    check("/** @constructor */ function NotAnInterface() {}\n" +
          "/** @constructor @implements {NotAnInterface} */ function SubClass() {}");
    Assert.assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testOverrideAnnotationOnSuperclass() {
    checkWithOptions(
        "/** @constructor */ function Super() {}\n" +
        "Super.prototype.foo = function() {};\n" +
        "/** @constructor @extends {Super} */ function Sub() {}\n" +
        "/** @override */ Sub.prototype.foo = function() {};",
        CheckLevel.WARNING, CheckLevel.OFF);
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testMissingOverrideWarning() {
    checkWithOptions(
        "/** @constructor */ function Super() {}\n" +
        "Super.prototype.foo = function() {};\n" +
        "/** @constructor @extends {Super} */ function Sub() {}\n" +
        "Sub.prototype.foo = function() {};",
        CheckLevel.WARNING, CheckLevel.OFF);
    Assert.assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testUnknownOverrideWarning() {
    checkWithOptions(
        "/** @constructor */ function Super() {}\n" +
        "/** @constructor @extends {Super} */ function Sub() {}\n" +
        "/** @override */ Sub.prototype.nonExistent = function() {};",
        CheckLevel.WARNING, CheckLevel.OFF);
    Assert.assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testOverrideMismatchWarning() {
    checkWithOptions(
        "/** @constructor */ function Super() {}\n" +
        "/** @type {number} */ Super.prototype.foo = 1;\n" +
        "/** @constructor @extends {Super} */ function Sub() {}\n" +
        "/** @override \n @type {string} */ Sub.prototype.foo = 'str';",
        CheckLevel.WARNING, CheckLevel.OFF);
    Assert.assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testNoTypeCheckSection() {
    check("/** @noTypeCheck */ function uncheck() { var x = 1; x(); }");
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testControlFlowStatements() {
    check("var x = 0;\n" +
          "if (x > 0) { x = 1; } else { x = 2; }\n" +
          "while (x < 5) { x++; }\n" +
          "do { x++; } while(x < 10);\n" +
          "for (var i = 0; i < 5; i++) { if (i == 2) continue; else break; }\n" +
          "try { throw new Error(); } catch (e) {}");
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testGetElem() {
    check("var arr = [1, 2, 3];\n" +
          "var item = arr[0];\n" +
          "var obj = {'key': 'value'};\n" +
          "var val = obj['key'];");
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testLogicalExpressionsAndHooks() {
    check("var a = true && false;\n" +
          "var b = true || false;\n" +
          "var c = true ? 1 : 2;");
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testCompoundAssignments() {
    check("var x = 1;\n" +
          "x += 2;\n" +
          "x -= 1;\n" +
          "x *= 3;\n" +
          "x /= 2;\n" +
          "x %= 2;\n" +
          "x &= 1;\n" +
          "x |= 2;\n" +
          "x ^= 3;\n" +
          "x <<= 1;\n" +
          "x >>= 1;\n" +
          "x >>>= 1;");
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testOverridingPrototypeWithNonObject() {
    check("/** @constructor */ function Foo() {}\n" +
          "Foo.prototype = 123;");
    Assert.assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testDirectCheckInvocation() {
    Node externs = compiler.parseTestCode("");
    Node js = compiler.parseTestCode("var a = 1;");
    Node block = new Node(Token.BLOCK, externs, js);

    TypeCheck typeCheck = new TypeCheck(compiler, rai, registry);
    typeCheck.processForTesting(externs, js);

    typeCheck.check(js, false);
    typeCheck.check(externs, true);
    Assert.assertTrue(typeCheck.getTypedPercent() >= 0.0);
  }

  @Test
  public void testIsReferenceHelper() throws Exception {
    Method isRef = TypeCheck.class.getDeclaredMethod("isReference", Node.class);
    isRef.setAccessible(true);

    Node nameNode = Node.newString(Token.NAME, "foo");
    Node getPropNode = new Node(Token.GETPROP, nameNode, Node.newString("bar"));
    Node getElemNode = new Node(Token.GETELEM, nameNode, Node.newNumber(0));
    Node numNode = Node.newNumber(42);

    Assert.assertTrue((Boolean) isRef.invoke(null, nameNode));
    Assert.assertTrue((Boolean) isRef.invoke(null, getPropNode));
    Assert.assertTrue((Boolean) isRef.invoke(null, getElemNode));
    Assert.assertFalse((Boolean) isRef.invoke(null, numNode));
  }
}