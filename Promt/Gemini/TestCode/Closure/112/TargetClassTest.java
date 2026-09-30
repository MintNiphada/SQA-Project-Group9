package com.google.javascript.jscomp;

import com.google.common.collect.Maps;
import com.google.javascript.jscomp.CodingConvention.AssertionFunctionSpec;
import com.google.javascript.jscomp.type.ReverseAbstractInterpreter;
import com.google.javascript.jscomp.type.SemanticReverseAbstractInterpreter;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.BooleanLiteralSet;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class TypeInferenceTest {

  private void parseAndInfer(String js) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    Node root = compiler.parseTestCode(js);
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    Scope topScope = scopeCreator.createScope(root, null);
    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, false, true);
    cfa.process(null, root);
    ControlFlowGraph<Node> cfg = cfa.getCfg();
    ReverseAbstractInterpreter rai = new SemanticReverseAbstractInterpreter(
        compiler.getCodingConvention(), compiler.getTypeRegistry());
    Map<String, AssertionFunctionSpec> assertionMap = Maps.newHashMap();
    TypeInference inference = new TypeInference(compiler, cfg, rai, topScope, assertionMap);
    inference.analyze();
  }

  private void parseAndInferFunction(String js) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    Node root = compiler.parseTestCode("function fn() {" + js + "}");
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    Scope topScope = scopeCreator.createScope(root, null);
    Node fnNode = root.getFirstChild();
    Scope fnScope = scopeCreator.createScope(fnNode, topScope);
    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, false, true);
    cfa.process(null, fnNode.getLastChild());
    ControlFlowGraph<Node> cfg = cfa.getCfg();
    ReverseAbstractInterpreter rai = new SemanticReverseAbstractInterpreter(
        compiler.getCodingConvention(), compiler.getTypeRegistry());
    Map<String, AssertionFunctionSpec> assertionMap = Maps.newHashMap();
    TypeInference inference = new TypeInference(compiler, cfg, rai, fnScope, assertionMap);
    inference.analyze();
  }

  @Test
  public void testDiagnosticType() {
    Assert.assertNotNull(TypeInference.FUNCTION_LITERAL_UNDEFINED_THIS);
    Assert.assertEquals("JSC_FUNCTION_LITERAL_UNDEFINED_THIS",
        TypeInference.FUNCTION_LITERAL_UNDEFINED_THIS.key);
  }

  @Test
  public void testGetBooleanOutcomes() {
    Assert.assertEquals(BooleanLiteralSet.TRUE,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.TRUE, BooleanLiteralSet.TRUE, true));
    Assert.assertEquals(BooleanLiteralSet.FALSE,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.FALSE, BooleanLiteralSet.FALSE, true));
    Assert.assertEquals(BooleanLiteralSet.BOTH,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.BOTH, BooleanLiteralSet.BOTH, true));
    Assert.assertEquals(BooleanLiteralSet.EMPTY,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.EMPTY, BooleanLiteralSet.EMPTY, true));
    Assert.assertEquals(BooleanLiteralSet.BOTH,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.TRUE, BooleanLiteralSet.FALSE, false));
    Assert.assertEquals(BooleanLiteralSet.BOTH,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.FALSE, BooleanLiteralSet.TRUE, true));
  }

  @Test
  public void testVarDeclarationsAndAssignments() {
    parseAndInferFunction(
        "var a = 10;" +
        "var b = 'hello';" +
        "var c = true;" +
        "var d;" +
        "d = 42;" +
        "d = 'reassigned';" +
        "var e = null;" +
        "var f = undefined;"
    );
  }

  @Test
  public void testArithmeticAndBitwiseOperators() {
    parseAndInferFunction(
        "var x = 1;" +
        "var y = 2;" +
        "var a = x + y;" +
        "var b = x - y;" +
        "var c = x * y;" +
        "var d = x / y;" +
        "var e = x % y;" +
        "var f = x << y;" +
        "var g = x >> y;" +
        "var h = x >>> y;" +
        "var i = x & y;" +
        "var j = x | y;" +
        "var k = x ^ y;" +
        "var l = ~x;" +
        "var m = -x;" +
        "var n = +x;" +
        "x++;" +
        "y--;" +
        "++x;" +
        "--y;"
    );
  }

  @Test
  public void testCompoundAssignments() {
    parseAndInferFunction(
        "var x = 1;" +
        "x += 2;" +
        "x -= 1;" +
        "x *= 3;" +
        "x /= 2;" +
        "x %= 2;" +
        "x <<= 1;" +
        "x >>= 1;" +
        "x >>>= 1;" +
        "x &= 1;" +
        "x |= 2;" +
        "x ^= 3;" +
        "var s = 'a';" +
        "s += 'b';"
    );
  }

  @Test
  public void testLogicalAndShortCircuiting() {
    parseAndInferFunction(
        "var a = true;" +
        "var b = false;" +
        "var c = a && b;" +
        "var d = a || b;" +
        "var e = (a && b) || (c && d);" +
        "var f = (a || b) && (c || d);" +
        "var g = !a;"
    );
  }

  @Test
  public void testComparisons() {
    parseAndInferFunction(
        "var x = 1, y = 2;" +
        "var a = x < y;" +
        "var b = x <= y;" +
        "var c = x > y;" +
        "var d = x >= y;" +
        "var e = x == y;" +
        "var f = x != y;" +
        "var g = x === y;" +
        "var h = x !== y;" +
        "var i = x instanceof Object;" +
        "var j = 'p' in {};" +
        "var k = typeof x;" +
        "var l = delete x;"
    );
  }

  @Test
  public void testControlFlowStatements() {
    parseAndInferFunction(
        "var x = 10;" +
        "if (x > 5) {" +
        "  x = 1;" +
        "} else {" +
        "  x = 2;" +
        "}" +
        "while (x < 10) {" +
        "  x++;" +
        "}" +
        "for (var i = 0; i < 5; i++) {" +
        "  x += i;" +
        "}" +
        "var obj = {a: 1, b: 2};" +
        "for (var key in obj) {" +
        "  var val = obj[key];" +
        "}" +
        "switch (x) {" +
        "  case 1: x = 2; break;" +
        "  case 2: x = 3; break;" +
        "  default: x = 0;" +
        "}"
    );
  }

  @Test
  public void testTryCatchThrow() {
    parseAndInferFunction(
        "try {" +
        "  throw new Error('err');" +
        "} catch (e) {" +
        "  var msg = e;" +
        "}"
    );
  }

  @Test
  public void testObjectsAndArrays() {
    parseAndInferFunction(
        "var arr = [1, 2, 'three', true];" +
        "var el = arr[0];" +
        "var obj = {x: 1, y: 'str', z: false};" +
        "var prop = obj.x;" +
        "obj.y = 'new_str';" +
        "var hook = arr ? obj.x : obj.y;"
    );
  }

  @Test
  public void testFunctionsAndCalls() {
    parseAndInfer(
        "function add(a, b) { return a + b; }" +
        "var res = add(1, 2);" +
        "var bound = add.bind(null, 5);" +
        "var boundRes = bound(10);" +
        "var anon = (function(x) { return x * 2; })(4);"
    );
  }

  @Test
  public void testTypeCastingAndJSDoc() {
    parseAndInfer(
        "/** @type {number} */ var num = 10;" +
        "/** @type {string} */ var str = /** @type {string} */ ('hello');" +
        "function g(/** number */ x) { return /** @type {number} */ (x + 1); }" +
        "g(num);"
    );
  }

  @Test
  public void testTemplatedCalls() {
    parseAndInfer(
        "/**\n" +
        " * @param {Array.<T>} arr\n" +
        " * @param {function(T): R} fn\n" +
        " * @return {Array.<R>}\n" +
        " * @template T, R\n" +
        " */\n" +
        "function map(arr, fn) {}\n" +
        "var nums = [1, 2, 3];\n" +
        "var strings = map(nums, function(x) { return '' + x; });"
    );
  }

  @Test
  public void testConstructorAndNew() {
    parseAndInfer(
        "/** @constructor */ function Person(name) { this.name = name; }" +
        "Person.prototype.getName = function() { return this.name; };" +
        "var p = new Person('Alice');" +
        "var n = p.getName();"
    );
  }
}