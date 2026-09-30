package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TargetClassTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
    Compiler.setLoggingLevelForTest(java.util.logging.Level.OFF);
  }

  private String optimizeAndPrint(String js) {
    Node root = compiler.parseTestCode(js);
    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(
        compiler, new PeepholeFoldConstants());
    pass.process(null, root);
    return compiler.toSource(root).trim();
  }

  private void test(String js, String expected) {
    String actual = optimizeAndPrint(js);
    String expectedNormalized = compiler.toSource(compiler.parseTestCode(expected)).trim();
    Assert.assertEquals(expectedNormalized, actual);
  }

  private void testSame(String js) {
    test(js, js);
  }

  @Test
  public void testTypeofFolding() {
    test("typeof 1", "'number'");
    test("typeof 'abc'", "'string'");
    test("typeof true", "'boolean'");
    test("typeof false", "'boolean'");
    test("typeof null", "'object'");
    test("typeof undefined", "'undefined'");
    test("typeof void 0", "'undefined'");
    test("typeof {}", "'object'");
    test("typeof []", "'object'");
    test("typeof function() {}", "'function'");
    testSame("typeof foo");
  }

  @Test
  public void testUnaryOperators() {
    test("!true", "false");
    test("!false", "true");
    test("!2", "false");
    test("!0", "!0");
    test("!1", "!1");

    test("+123", "123");
    test("+'123'", "123");
    test("+-0", "0");

    test("- -5", "5");
    test("-5", "-5");
    test("-Infinity", "-Infinity");
    test("-NaN", "NaN");

    test("~0", "-1");
    test("~-1", "0");
    test("~5", "-6");
  }

  @Test
  public void testUnaryOperatorErrors() {
    testSame("~1.5");
    testSame("~1e15");
    testSame("-('abc')");
  }

  @Test
  public void testVoidReduction() {
    test("void 1", "void 0");
    test("void 'abc'", "void 0");
    testSame("void 0");
    testSame("void foo()");
  }

  @Test
  public void testInstanceofFolding() {
    test("5 instanceof Object", "false");
    test("'str' instanceof Object", "false");
    test("true instanceof Object", "false");
    test("null instanceof Object", "false");
    test("({}) instanceof Object", "true");
    test("[] instanceof Object", "true");
    testSame("foo instanceof Object");
    testSame("({}) instanceof foo");
  }

  @Test
  public void testAssignFolding() {
    test("x = x + y", "x += y");
    test("x = y + x", "x += y");
    test("x = x - y", "x -= y");
    test("x = x * y", "x *= y");
    test("x = x / y", "x /= y");
    test("x = x % y", "x %= y");
    test("x = x & y", "x &= y");
    test("x = x | y", "x |= y");
    test("x = x ^ y", "x ^= y");
    test("x = x << y", "x <<= y");
    test("x = x >> y", "x >>= y");
    test("x = x >>> y", "x >>>= y");
    testSame("x = y - x");
    testSame("foo().x = foo().x + y");
  }

  @Test
  public void testLogicalAndOrFolding() {
    test("true && x", "x");
    test("false && x", "false");
    test("true || x", "true");
    test("false || x", "x");
    test("3 || x", "3");
    test("0 && x", "0");
    testSame("x && y");
    testSame("x || y");
  }

  @Test
  public void testStringConcatenation() {
    test("'a' + 'b'", "'ab'");
    test("'a' + 'b' + 'c'", "'abc'");
    test("foo() + 'a' + 'b'", "foo() + 'ab'");
    test("'a' + ('b' + foo())", "'ab' + foo()");
    test("'a' + 1", "'a1'");
    test("1 + 'a'", "'1a'");
  }

  @Test
  public void testArithmeticOperations() {
    test("1 + 2", "3");
    test("5 - 3", "2");
    test("4 * 2", "8");
    test("10 / 2", "5");
    test("10 % 3", "1");

    test("1 & 3", "1");
    test("1 | 2", "3");
    test("1 ^ 3", "2");

    test("foo() * 2 * 3", "foo() * 6");
    test("foo() + 2 + 3", "foo() + 5");
    test("2 * (foo() * 3)", "foo() * 6");

    testSame("10 / 0");
    testSame("10 % 0");
  }

  @Test
  public void testShiftOperations() {
    test("1 << 2", "4");
    test("8 >> 1", "4");
    test("-1 >>> 1", "2147483647");
    test("0 >>> 0", "0");

    testSame("1 << 32");
    testSame("1 << -1");
    testSame("1.5 << 2");
    testSame("1 << 2.5");
    testSame("1e15 << 2");
  }

  @Test
  public void testComparisons() {
    test("1 == 1", "true");
    test("1 == 2", "false");
    test("1 != 2", "true");
    test("1 != 1", "false");
    test("1 === 1", "true");
    test("1 === 2", "false");
    test("1 !== 2", "true");
    test("1 !== 1", "false");

    test("1 < 2", "true");
    test("2 < 1", "false");
    test("1 <= 1", "true");
    test("2 <= 1", "false");
    test("2 > 1", "true");
    test("1 > 2", "false");
    test("1 >= 1", "true");
    test("1 >= 2", "false");

    test("'a' == 'a'", "true");
    test("'a' == 'b'", "false");
    test("'a' === 'a'", "true");
    test("'a' !== 'b'", "true");

    test("true == true", "true");
    test("false == true", "false");
    test("null == null", "true");
    test("true == false", "false");

    test("undefined == undefined", "true");
    test("undefined == null", "true");
    test("null == undefined", "true");
    test("undefined === null", "false");
    test("undefined !== null", "true");
    test("void 0 == undefined", "true");
    test("void 0 === undefined", "true");
    test("void 0 < undefined", "false");

    test("this == this", "true");
    test("this != this", "false");
    test("this === this", "true");
    test("this !== this", "false");
    testSame("this == other");
    testSame("this < this");
  }

  @Test
  public void testConstructorFolding() {
    test("this[new String('eval')]", "this['eval']");
    test("this[new String()]", "this['']");
    testSame("new String('eval')");
    testSame("this[new Object()]");
  }

  @Test
  public void testStringMethods() {
    test("'ABC'.toLowerCase()", "'abc'");
    test("'abc'.toUpperCase()", "'ABC'");

    test("'abcdef'.indexOf('cd')", "2");
    test("'abcdef'.indexOf('gh')", "-1");
    test("'abcdefcd'.indexOf('cd', 3)", "6");
    test("'abcdefcd'.lastIndexOf('cd')", "6");
    test("'abcdefcd'.lastIndexOf('cd', 4)", "2");

    test("'abcdef'.substr(2)", "'cdef'");
    test("'abcdef'.substr(2, 3)", "'cde'");
    test("'abcdef'.substring(2)", "'cdef'");
    test("'abcdef'.substring(2, 4)", "'cd'");

    testSame("'abcdef'.substr(-1)");
    testSame("'abcdef'.substr(2, 10)");
    testSame("'abcdef'.substring(-1)");
    testSame("'abcdef'.substring(4, 2)");
    testSame("'abcdef'.indexOf(foo())");
  }

  @Test
  public void testArrayJoin() {
    test("['a', 'b', 'c'].join('')", "'abc'");
    test("['a', 'b', 'c'].join(',')", "'a,b,c'");
    test("['a', 'b', 'c'].join()", "'a,b,c'");
    test("[1, 2, 3].join('')", "'123'");
    test("[].join('')", "''");
    test("['a', b, 'c'].join('')", "['a', b, 'c'].join('')");
  }

  @Test
  public void testGetElemFolding() {
    test("[1, 2, 3][0]", "1");
    test("[1, 2, 3][1]", "2");
    test("[1, 2, 3][2]", "3");
    test("['a', 'b'][0]", "'a'");
    test("[, 'b'][0]", "void 0");

    testSame("[1, 2, 3][3]");
    testSame("[1, 2, 3][-1]");
    testSame("[1, 2, 3][1.5]");
    testSame("[1, 2, 3]['foo']");
  }

  @Test
  public void testGetPropFolding() {
    test("[1, 2, 3].length", "3");
    test("[].length", "0");
    test("'hello'.length", "5");
    test("''.length", "0");
    testSame("foo.length");
    testSame("[foo()].length");
  }

  @Test
  public void testOperandConversions() {
    test("1 + +'2'", "3");
    test("1 - '2'", "-1");
    test("2 * '3'", "6");
    test("6 / '2'", "3");
    test("7 % '4'", "3");
    test("~'5'", "-6");
  }

  @Test
  public void testDirectSubtreeOptimization() {
    PeepholeFoldConstants optimizer = new PeepholeFoldConstants();
    Node expr = new Node(Token.EXPR_RESULT, Node.newString("test"));
    Node result = optimizer.optimizeSubtree(expr);
    Assert.assertSame(expr, result);

    Node emptyAdd = new Node(Token.ADD);
    Node addResult = optimizer.optimizeSubtree(emptyAdd);
    Assert.assertSame(emptyAdd, addResult);

    Node singleChildAdd = new Node(Token.ADD, Node.newNumber(1));
    Node singleResult = optimizer.optimizeSubtree(singleChildAdd);
    Assert.assertSame(singleChildAdd, singleResult);
  }
}