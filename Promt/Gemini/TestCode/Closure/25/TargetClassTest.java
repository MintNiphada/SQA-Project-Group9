package com.google.javascript.jscomp;

import com.google.javascript.rhino.jstype.BooleanLiteralSet;
import org.junit.Assert;
import org.junit.Test;

public class TargetClassTest {

  private Compiler compileJs(String js) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.checkTypes = true;
    compiler.initOptions(options);
    SourceFile externs = SourceFile.fromCode("externs.js", "function goog() {};");
    SourceFile input = SourceFile.fromCode("test.js", js);
    compiler.compile(externs, input, options);
    return compiler;
  }

  @Test
  public void testDiagnosticTypes() {
    Assert.assertNotNull(TypeInference.TEMPLATE_TYPE_NOT_OBJECT_TYPE);
    Assert.assertNotNull(TypeInference.TEMPLATE_TYPE_OF_THIS_EXPECTED);
    Assert.assertNotNull(TypeInference.FUNCTION_LITERAL_UNDEFINED_THIS);
    Assert.assertEquals("JSC_TEMPLATE_TYPE_NOT_OBJECT_TYPE", TypeInference.TEMPLATE_TYPE_NOT_OBJECT_TYPE.key);
    Assert.assertEquals("JSC_TEMPLATE_TYPE_OF_THIS_EXPECTED", TypeInference.TEMPLATE_TYPE_OF_THIS_EXPECTED.key);
    Assert.assertEquals("JSC_FUNCTION_LITERAL_UNDEFINED_THIS", TypeInference.FUNCTION_LITERAL_UNDEFINED_THIS.key);
  }

  @Test
  public void testGetBooleanOutcomes() {
    Assert.assertEquals(
        BooleanLiteralSet.TRUE,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.TRUE, BooleanLiteralSet.TRUE, true));
    Assert.assertEquals(
        BooleanLiteralSet.BOTH,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.TRUE, BooleanLiteralSet.FALSE, true));
    Assert.assertEquals(
        BooleanLiteralSet.FALSE,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.FALSE, BooleanLiteralSet.FALSE, false));
    Assert.assertEquals(
        BooleanLiteralSet.BOTH,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.FALSE, BooleanLiteralSet.TRUE, false));
    Assert.assertEquals(
        BooleanLiteralSet.EMPTY,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.EMPTY, BooleanLiteralSet.EMPTY, true));
    Assert.assertEquals(
        BooleanLiteralSet.EMPTY,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.EMPTY, BooleanLiteralSet.EMPTY, false));
  }

  @Test
  public void testVarDeclarationAndAssignmentInference() {
    Compiler compiler = compileJs("var x = 1; var y = 'hello'; var z = x + y;");
    Assert.assertEquals(0, compiler.getErrors().length);
  }

  @Test
  public void testArithmeticAndBitwiseOperations() {
    String js = "var a = 10;\n"
        + "var b = 2;\n"
        + "var c = a + b;\n"
        + "var d = a - b;\n"
        + "var e = a * b;\n"
        + "var f = a / b;\n"
        + "var g = a % b;\n"
        + "var h = a << b;\n"
        + "var i = a >> b;\n"
        + "var j = a >>> b;\n"
        + "var k = a & b;\n"
        + "var l = a | b;\n"
        + "var m = a ^ b;\n"
        + "var n = ~a;\n"
        + "var o = +a;\n"
        + "var p = -a;\n"
        + "a++;\n"
        + "b--;\n"
        + "a += 5;\n"
        + "b -= 1;\n"
        + "c *= 2;\n"
        + "d /= 2;\n"
        + "e %= 3;\n"
        + "f &= 1;\n"
        + "g |= 2;\n"
        + "h ^= 3;\n"
        + "i <<= 1;\n"
        + "j >>= 1;\n"
        + "k >>>= 1;\n";
    Compiler compiler = compileJs(js);
    Assert.assertEquals(0, compiler.getErrors().length);
  }

  @Test
  public void testLogicalAndHookExpressions() {
    String js = "var x = true && 'string';\n"
        + "var y = false || 123;\n"
        + "var z = true ? 1 : 'two';\n"
        + "var w = (x && y) ? z : null;\n";
    Compiler compiler = compileJs(js);
    Assert.assertEquals(0, compiler.getErrors().length);
  }

  @Test
  public void testObjectAndArrayLiterals() {
    String js = "var obj = { a: 1, b: 'str', c: true };\n"
        + "var valA = obj.a;\n"
        + "var valB = obj['b'];\n"
        + "var arr = [1, 2, 3];\n"
        + "var first = arr[0];\n";
    Compiler compiler = compileJs(js);
    Assert.assertEquals(0, compiler.getErrors().length);
  }

  @Test
  public void testComparisonsAndTypeof() {
    String js = "var a = 5;\n"
        + "var b = 10;\n"
        + "var c = a < b;\n"
        + "var d = a <= b;\n"
        + "var e = a > b;\n"
        + "var f = a >= b;\n"
        + "var g = a == b;\n"
        + "var h = a != b;\n"
        + "var i = a === b;\n"
        + "var j = a !== b;\n"
        + "var k = !a;\n"
        + "var l = typeof a;\n"
        + "var m = a instanceof Object;\n"
        + "var n = 'a' in {a: 1};\n"
        + "var o = (a, b);\n";
    Compiler compiler = compileJs(js);
    Assert.assertEquals(0, compiler.getErrors().length);
  }

  @Test
  public void testControlFlowStatements() {
    String js = "function testFlow(x) {\n"
        + "  if (x) {\n"
        + "    return 1;\n"
        + "  } else {\n"
        + "    return 0;\n"
        + "  }\n"
        + "}\n"
        + "function testSwitch(x) {\n"
        + "  switch (x) {\n"
        + "    case 1: return 'one';\n"
        + "    default: return 'other';\n"
        + "  }\n"
        + "}\n"
        + "function testForIn(obj) {\n"
        + "  for (var key in obj) {\n"
        + "    var k = key;\n"
        + "  }\n"
        + "}\n"
        + "function testTryCatch() {\n"
        + "  try {\n"
        + "    throw new Error('err');\n"
        + "  } catch (e) {\n"
        + "    var caught = e;\n"
        + "  }\n"
        + "}\n";
    Compiler compiler = compileJs(js);
    Assert.assertEquals(0, compiler.getErrors().length);
  }

  @Test
  public void testTypeCastingAndTypeAnnotation() {
    String js = "/** @type {number} */ var a = 10;\n"
        + "var b = /** @type {string} */ ('text');\n"
        + "function fn(/** number */ p1) {\n"
        + "  return p1 + 1;\n"
        + "}\n"
        + "var res = fn(5);\n";
    Compiler compiler = compileJs(js);
    Assert.assertEquals(0, compiler.getErrors().length);
  }

  @Test
  public void testConstructorAndNewInference() {
    String js = "/** @constructor */\n"
        + "function Foo(x) {\n"
        + "  this.x = x;\n"
        + "}\n"
        + "Foo.prototype.getX = function() { return this.x; };\n"
        + "var inst = new Foo(10);\n"
        + "var xVal = inst.getX();\n";
    Compiler compiler = compileJs(js);
    Assert.assertEquals(0, compiler.getErrors().length);
  }
}