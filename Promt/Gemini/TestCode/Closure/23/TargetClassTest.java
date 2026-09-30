package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;

public class PeepholeFoldConstantsTest extends CompilerTestCase {

  private boolean late = false;

  public PeepholeFoldConstantsTest() {
    super();
  }

  @Override
  protected CompilerPass getProcessor(final Compiler compiler) {
    return new PeepholeOptimizationsPass(compiler, new PeepholeFoldConstants(late));
  }

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    enableLineNumberCheck(true);
    late = false;
  }

  private void foldSame(String js) {
    testSame(js);
  }

  private void fold(String js, String expected) {
    test(js, expected);
  }

  private void fold(String js, String expected, DiagnosticType warning) {
    test(js, expected, warning);
  }

  @Test
  public void testTypeof() {
    fold("typeof 1", "'number'");
    fold("typeof 'a'", "'string'");
    fold("typeof true", "'boolean'");
    fold("typeof false", "'boolean'");
    fold("typeof null", "'object'");
    fold("typeof undefined", "'undefined'");
    fold("typeof void 0", "'undefined'");
    fold("typeof []", "'object'");
    fold("typeof {}", "'object'");
    fold("typeof function() {}", "'function'");
    foldSame("typeof x");
  }

  @Test
  public void testUnaryNot() {
    fold("!true", "false");
    fold("!false", "true");
    fold("!1", "false");
    fold("!0", "true");
    fold("!null", "true");
    fold("!void 0", "true");
    fold("!'hello'", "false");
    fold("!''", "true");
    foldSame("!x");

    late = true;
    foldSame("!0");
    foldSame("!1");
    fold("!2", "false");
  }

  @Test
  public void testUnaryPos() {
    fold("+1", "1");
    fold("+(1 + 2)", "3");
    foldSame("+x");
  }

  @Test
  public void testUnaryNeg() {
    fold("-1", "-1");
    fold("- -1", "1");
    foldSame("-Infinity");
    fold("-NaN", "NaN");
    foldSame("-'abc'", PeepholeFoldConstants.NEGATING_A_NON_NUMBER_ERROR);
  }

  @Test
  public void testUnaryBitNot() {
    fold("~0", "-1");
    fold("~1", "-2");
    fold("~-1", "0");
    foldSame("~1.5", PeepholeFoldConstants.FRACTIONAL_BITWISE_OPERAND);
    foldSame("~99999999999999", PeepholeFoldConstants.BITWISE_OPERAND_OUT_OF_RANGE);
    foldSame("~'abc'", PeepholeFoldConstants.NEGATING_A_NON_NUMBER_ERROR);
  }

  @Test
  public void testReduceVoid() {
    fold("void 1", "void 0");
    fold("void 'hello'", "void 0");
    fold("void true", "void 0");
    foldSame("void 0");
    foldSame("void foo()");
  }

  @Test
  public void testFoldInstanceof() {
    fold("1 instanceof Object", "false");
    fold("'abc' instanceof Object", "false");
    fold("true instanceof Object", "false");
    fold("({}) instanceof Object", "true");
    fold("[] instanceof Object", "true");
    foldSame("x instanceof Object");
    foldSame("({}) instanceof foo()");
  }

  @Test
  public void testFoldAssign() {
    late = false;
    fold("x += 1", "x = x + 1");
    fold("x -= 1", "x = x - 1");
    fold("x *= 1", "x = x * 1");
    fold("x /= 1", "x = x / 1");
    fold("x %= 1", "x = x % 1");
    fold("x &= 1", "x = x & 1");
    fold("x |= 1", "x = x | 1");
    fold("x ^= 1", "x = x ^ 1");
    fold("x <<= 1", "x = x << 1");
    fold("x >>= 1", "x = x >> 1");
    fold("x >>>= 1", "x = x >>> 1");

    late = true;
    fold("x = x + 1", "x += 1");
    fold("x = 1 + x", "x += 1");
    fold("x = x - 1", "x -= 1");
    fold("x = x * 1", "x *= 1");
    fold("x = 1 * x", "x *= 1");
    fold("x = x / 1", "x /= 1");
    fold("x = x % 1", "x %= 1");
    fold("x = x & 1", "x &= 1");
    fold("x = 1 & x", "x &= 1");
    fold("x = x | 1", "x |= 1");
    fold("x = 1 | x", "x |= 1");
    fold("x = x ^ 1", "x ^= 1");
    fold("x = 1 ^ x", "x ^= 1");
    fold("x = x << 1", "x <<= 1");
    fold("x = x >> 1", "x >>= 1");
    fold("x = x >>> 1", "x >>>= 1");
  }

  @Test
  public void testFoldAndOr() {
    fold("true && x", "x");
    fold("false && x", "false");
    fold("1 && x", "x");
    fold("0 && x", "0");

    fold("true || x", "true");
    fold("false || x", "x");
    fold("1 || x", "1");
    fold("0 || x", "x");

    foldSame("foo() && x");
    foldSame("foo() || x");
  }

  @Test
  public void testFoldAddStrings() {
    fold("'a' + 'b'", "'ab'");
    fold("'a' + 'b' + 'c'", "'abc'");
    fold("x + 'a' + 'b'", "x + 'ab'");
    fold("'a' + ( 'b' + x )", "'ab' + x");
    fold("1 + 'a'", "'1a'");
    fold("'a' + 1", "'a1'");
    fold("true + 'a'", "'truea'");
  }

  @Test
  public void testFoldArithmetic() {
    fold("1 + 2", "3");
    fold("5 - 2", "3");
    fold("2 * 3", "6");
    fold("6 / 2", "3");
    fold("7 % 3", "1");
    foldSame("1 / 0");
    foldSame("1 % 0");

    fold("1 & 3", "1");
    fold("1 | 2", "3");
    fold("1 ^ 3", "2");

    fold("x * 2 * 3", "x * 6");
    fold("x + 2 + 3", "x + 5");
    fold("x & 2 & 3", "x & 2");
    fold("x | 1 | 2", "x | 3");
    fold("x ^ 1 ^ 3", "x ^ 2");
  }

  @Test
  public void testFoldShift() {
    fold("1 << 2", "4");
    fold("8 >> 1", "4");
    fold("-1 >>> 1", "2147483647");
    fold("1 << 0", "1");

    foldSame("1 << 32", PeepholeFoldConstants.SHIFT_AMOUNT_OUT_OF_BOUNDS);
    foldSame("1 << -1", PeepholeFoldConstants.SHIFT_AMOUNT_OUT_OF_BOUNDS);
    foldSame("1.5 << 2", PeepholeFoldConstants.FRACTIONAL_BITWISE_OPERAND);
    foldSame("1 << 2.5", PeepholeFoldConstants.FRACTIONAL_BITWISE_OPERAND);
    foldSame("99999999999999 << 1", PeepholeFoldConstants.BITWISE_OPERAND_OUT_OF_RANGE);
  }

  @Test
  public void testFoldComparison() {
    fold("1 < 2", "true");
    fold("2 < 1", "false");
    fold("1 <= 1", "true");
    fold("1 <= 0", "false");
    fold("2 > 1", "true");
    fold("1 > 2", "false");
    fold("1 >= 1", "true");
    fold("0 >= 1", "false");

    fold("1 == 1", "true");
    fold("1 == 2", "false");
    fold("1 != 2", "true");
    fold("1 != 1", "false");
    fold("1 === 1", "true");
    fold("1 === 2", "false");
    fold("1 !== 2", "true");
    fold("1 !== 1", "false");

    fold("'a' == 'a'", "true");
    fold("'a' == 'b'", "false");
    fold("'a' != 'b'", "true");
    fold("'a' != 'a'", "false");
    fold("'a' === 'a'", "true");
    fold("'a' === 'b'", "false");
    fold("'a' !== 'b'", "true");
    fold("'a' !== 'a'", "false");
    foldSame("'\u000B' == '\u000B'");

    fold("null == null", "true");
    fold("null == undefined", "true");
    fold("undefined == null", "true");
    fold("undefined == undefined", "true");
    fold("null === null", "true");
    fold("null === undefined", "false");
    fold("undefined === null", "false");
    fold("undefined === undefined", "true");

    fold("null == 1", "false");
    fold("1 == null", "false");
    fold("undefined == 1", "false");
    fold("1 == undefined", "false");

    fold("true == true", "true");
    fold("true == false", "false");
    fold("false == false", "true");
    fold("true === true", "true");
    fold("true === false", "false");
    fold("true < false", "false");
    fold("false < true", "true");

    fold("this == this", "true");
    fold("this === this", "true");
    fold("this != this", "false");
    fold("this !== this", "false");

    fold("x < x", "false");
    fold("x > x", "false");
  }

  @Test
  public void testFoldGetProp() {
    fold("[1, 2, 3].length", "3");
    fold("[].length", "0");
    fold("'hello'.length", "5");
    fold("({a: 1}).a", "1");
    fold("({a: 1, b: 2}).b", "2");
    fold("({get a() { return 1; }}).a", "(function() { return 1; })()");

    foldSame("[foo()].length");
    foldSame("({a: foo()}).b");
    foldSame("({a: 1}).a += 1");
    foldSame("({a: 1}).a++");
  }

  @Test
  public void testFoldGetElem() {
    fold("[1, 2, 3][0]", "1");
    fold("[1, 2, 3][1]", "2");
    fold("[1, 2, 3][2]", "3");
    fold("([1, 2, 3])[1]", "2");

    foldSame("[1, 2, 3][3]", PeepholeFoldConstants.INDEX_OUT_OF_BOUNDS_ERROR);
    foldSame("[1, 2, 3][-1]", PeepholeFoldConstants.INDEX_OUT_OF_BOUNDS_ERROR);
    foldSame("[1, 2, 3][1.5]", PeepholeFoldConstants.INVALID_GETELEM_INDEX_ERROR);
    foldSame("[1, 2, 3]['foo']");
    foldSame("[1, 2, 3][0] = 5");

    fold("({'a': 1})['a']", "1");
  }

  @Test
  public void testFoldCtorCall() {
    fold("'' + new String('hello')", "'' + 'hello'");
    fold("x[new String('abc')]", "x['abc']");
    fold("x[new String()]", "x['']");
    foldSame("new String('hello')");
  }

  @Test
  public void testReduceOperands() {
    fold("+ '123'", "123");
    fold("- '123'", "-123");
    fold("~ '123'", "-124");
    fold("'123' - 1", "122");
    fold("'123' * 2", "246");
    fold("'123' / 3", "41");
    fold("'123' % 10", "3");
    fold("'123' & 1", "1");
    fold("'123' | 0", "123");
    fold("'123' ^ 0", "123");
    fold("'123' << 1", "246");
    fold("'123' >> 1", "61");
    fold("'123' >>> 1", "61");
  }
}