package org.apache.commons.math.util;

import java.math.BigDecimal;
import org.junit.Assert;
import org.junit.Test;

public class MathUtilsTest {

    @Test
    public void testAddAndCheckInt() {
        Assert.assertEquals(5, MathUtils.addAndCheck(2, 3));
        Assert.assertEquals(-5, MathUtils.addAndCheck(-2, -3));
        Assert.assertEquals(1, MathUtils.addAndCheck(3, -2));
        Assert.assertEquals(Integer.MAX_VALUE, MathUtils.addAndCheck(Integer.MAX_VALUE - 1, 1));
        Assert.assertEquals(Integer.MIN_VALUE, MathUtils.addAndCheck(Integer.MIN_VALUE + 1, -1));
    }

    @Test(expected = ArithmeticException.class)
    public void testAddAndCheckIntPositiveOverflow() {
        MathUtils.addAndCheck(Integer.MAX_VALUE, 1);
    }

    @Test(expected = ArithmeticException.class)
    public void testAddAndCheckIntNegativeOverflow() {
        MathUtils.addAndCheck(Integer.MIN_VALUE, -1);
    }

    @Test
    public void testAddAndCheckLong() {
        Assert.assertEquals(5L, MathUtils.addAndCheck(2L, 3L));
        Assert.assertEquals(5L, MathUtils.addAndCheck(3L, 2L));
        Assert.assertEquals(-5L, MathUtils.addAndCheck(-2L, -3L));
        Assert.assertEquals(-5L, MathUtils.addAndCheck(-3L, -2L));
        Assert.assertEquals(1L, MathUtils.addAndCheck(-2L, 3L));
        Assert.assertEquals(1L, MathUtils.addAndCheck(3L, -2L));
        Assert.assertEquals(Long.MAX_VALUE, MathUtils.addAndCheck(Long.MAX_VALUE - 1L, 1L));
        Assert.assertEquals(Long.MIN_VALUE, MathUtils.addAndCheck(Long.MIN_VALUE + 1L, -1L));
    }

    @Test(expected = ArithmeticException.class)
    public void testAddAndCheckLongPositiveOverflow() {
        MathUtils.addAndCheck(Long.MAX_VALUE, 1L);
    }

    @Test(expected = ArithmeticException.class)
    public void testAddAndCheckLongNegativeOverflow() {
        MathUtils.addAndCheck(Long.MIN_VALUE, -1L);
    }

    @Test
    public void testSubAndCheckInt() {
        Assert.assertEquals(-1, MathUtils.subAndCheck(2, 3));
        Assert.assertEquals(1, MathUtils.subAndCheck(3, 2));
        Assert.assertEquals(1, MathUtils.subAndCheck(-2, -3));
        Assert.assertEquals(Integer.MIN_VALUE, MathUtils.subAndCheck(Integer.MIN_VALUE + 1, 1));
        Assert.assertEquals(Integer.MAX_VALUE, MathUtils.subAndCheck(Integer.MAX_VALUE - 1, -1));
    }

    @Test(expected = ArithmeticException.class)
    public void testSubAndCheckIntPositiveOverflow() {
        MathUtils.subAndCheck(Integer.MAX_VALUE, -1);
    }

    @Test(expected = ArithmeticException.class)
    public void testSubAndCheckIntNegativeOverflow() {
        MathUtils.subAndCheck(Integer.MIN_VALUE, 1);
    }

    @Test
    public void testSubAndCheckLong() {
        Assert.assertEquals(-1L, MathUtils.subAndCheck(2L, 3L));
        Assert.assertEquals(1L, MathUtils.subAndCheck(3L, 2L));
        Assert.assertEquals(1L, MathUtils.subAndCheck(-2L, -3L));
        Assert.assertEquals(0L, MathUtils.subAndCheck(Long.MIN_VALUE, Long.MIN_VALUE));
        Assert.assertEquals(-1L, MathUtils.subAndCheck(-1L - Long.MIN_VALUE, Long.MIN_VALUE));
        Assert.assertEquals(Long.MAX_VALUE, MathUtils.subAndCheck(Long.MAX_VALUE - 1L, -1L));
    }

    @Test(expected = ArithmeticException.class)
    public void testSubAndCheckLongPositiveOverflow() {
        MathUtils.subAndCheck(Long.MAX_VALUE, -1L);
    }

    @Test(expected = ArithmeticException.class)
    public void testSubAndCheckLongNegativeOverflow() {
        MathUtils.subAndCheck(Long.MIN_VALUE, 1L);
    }

    @Test(expected = ArithmeticException.class)
    public void testSubAndCheckLongMinBoundaryOverflow() {
        MathUtils.subAndCheck(0L, Long.MIN_VALUE);
    }

    @Test
    public void testMulAndCheckInt() {
        Assert.assertEquals(6, MathUtils.mulAndCheck(2, 3));
        Assert.assertEquals(-6, MathUtils.mulAndCheck(2, -3));
        Assert.assertEquals(6, MathUtils.mulAndCheck(-2, -3));
        Assert.assertEquals(0, MathUtils.mulAndCheck(0, 5));
        Assert.assertEquals(Integer.MAX_VALUE, MathUtils.mulAndCheck(Integer.MAX_VALUE, 1));
        Assert.assertEquals(Integer.MIN_VALUE, MathUtils.mulAndCheck(Integer.MIN_VALUE, 1));
    }

    @Test(expected = ArithmeticException.class)
    public void testMulAndCheckIntPositiveOverflow() {
        MathUtils.mulAndCheck(Integer.MAX_VALUE, 2);
    }

    @Test(expected = ArithmeticException.class)
    public void testMulAndCheckIntNegativeOverflow() {
        MathUtils.mulAndCheck(Integer.MIN_VALUE, 2);
    }

    @Test
    public void testMulAndCheckLong() {
        Assert.assertEquals(6L, MathUtils.mulAndCheck(2L, 3L));
        Assert.assertEquals(6L, MathUtils.mulAndCheck(3L, 2L));
        Assert.assertEquals(-6L, MathUtils.mulAndCheck(-2L, 3L));
        Assert.assertEquals(-6L, MathUtils.mulAndCheck(3L, -2L));
        Assert.assertEquals(6L, MathUtils.mulAndCheck(-2L, -3L));
        Assert.assertEquals(6L, MathUtils.mulAndCheck(-3L, -2L));
        Assert.assertEquals(0L, MathUtils.mulAndCheck(0L, 5L));
        Assert.assertEquals(0L, MathUtils.mulAndCheck(5L, 0L));
        Assert.assertEquals(0L, MathUtils.mulAndCheck(-5L, 0L));
        Assert.assertEquals(0L, MathUtils.mulAndCheck(0L, -5L));
        Assert.assertEquals(0L, MathUtils.mulAndCheck(0L, 0L));
        Assert.assertEquals(Long.MAX_VALUE, MathUtils.mulAndCheck(Long.MAX_VALUE, 1L));
        Assert.assertEquals(Long.MIN_VALUE, MathUtils.mulAndCheck(Long.MIN_VALUE, 1L));
    }

    @Test(expected = ArithmeticException.class)
    public void testMulAndCheckLongPosPosOverflow() {
        MathUtils.mulAndCheck(Long.MAX_VALUE, 2L);
    }

    @Test(expected = ArithmeticException.class)
    public void testMulAndCheckLongNegNegOverflow() {
        MathUtils.mulAndCheck(Long.MIN_VALUE, -2L);
    }

    @Test(expected = ArithmeticException.class)
    public void testMulAndCheckLongNegPosOverflow() {
        MathUtils.mulAndCheck(Long.MIN_VALUE, 2L);
    }

    @Test
    public void testBinomialCoefficient() {
        Assert.assertEquals(1L, MathUtils.binomialCoefficient(0, 0));
        Assert.assertEquals(1L, MathUtils.binomialCoefficient(5, 0));
        Assert.assertEquals(1L, MathUtils.binomialCoefficient(5, 5));
        Assert.assertEquals(5L, MathUtils.binomialCoefficient(5, 1));
        Assert.assertEquals(5L, MathUtils.binomialCoefficient(5, 4));
        Assert.assertEquals(10L, MathUtils.binomialCoefficient(5, 2));
        Assert.assertEquals(10L, MathUtils.binomialCoefficient(5, 3));
        Assert.assertEquals(184756L, MathUtils.binomialCoefficient(20, 10));
        Assert.assertEquals(72194605345969446L, MathUtils.binomialCoefficient(66, 33));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficientNegativeN() {
        MathUtils.binomialCoefficient(-1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficientKGreaterThanN() {
        MathUtils.binomialCoefficient(4, 5);
    }

    @Test(expected = ArithmeticException.class)
    public void testBinomialCoefficientOverflow() {
        MathUtils.binomialCoefficient(1000, 500);
    }

    @Test
    public void testBinomialCoefficientDoubleAndLog() {
        Assert.assertEquals(1.0, MathUtils.binomialCoefficientDouble(0, 0), 1e-10);
        Assert.assertEquals(10.0, MathUtils.binomialCoefficientDouble(5, 2), 1e-10);
        Assert.assertEquals(0.0, MathUtils.binomialCoefficientLog(0, 0), 1e-10);
        Assert.assertEquals(0.0, MathUtils.binomialCoefficientLog(5, 0), 1e-10);
        Assert.assertEquals(Math.log(5), MathUtils.binomialCoefficientLog(5, 1), 1e-10);
        Assert.assertEquals(Math.log(5), MathUtils.binomialCoefficientLog(5, 4), 1e-10);
        Assert.assertEquals(Math.log(10), MathUtils.binomialCoefficientLog(5, 2), 1e-10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficientDoubleNegativeN() {
        MathUtils.binomialCoefficientDouble(-1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficientDoubleKGreaterThanN() {
        MathUtils.binomialCoefficientDouble(2, 3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficientLogNegativeN() {
        MathUtils.binomialCoefficientLog(-1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficientLogKGreaterThanN() {
        MathUtils.binomialCoefficientLog(2, 3);
    }

    @Test
    public void testHyperbolicFunctions() {
        Assert.assertEquals(1.0, MathUtils.cosh(0.0), 1e-10);
        Assert.assertEquals(1.5430806348152437, MathUtils.cosh(1.0), 1e-10);
        Assert.assertEquals(0.0, MathUtils.sinh(0.0), 1e-10);
        Assert.assertEquals(1.1752011936438014, MathUtils.sinh(1.0), 1e-10);
    }

    @Test
    public void testEqualsDouble() {
        Assert.assertTrue(MathUtils.equals(1.0, 1.0));
        Assert.assertFalse(MathUtils.equals(1.0, 2.0));
        Assert.assertTrue(MathUtils.equals(Double.NaN, Double.NaN));
        Assert.assertFalse(MathUtils.equals(Double.NaN, 1.0));
        Assert.assertFalse(MathUtils.equals(1.0, Double.NaN));
    }

    @Test
    public void testEqualsDoubleArray() {
        Assert.assertTrue(MathUtils.equals((double[]) null, (double[]) null));
        Assert.assertFalse(MathUtils.equals(new double[]{1.0}, null));
        Assert.assertFalse(MathUtils.equals(null, new double[]{1.0}));
        Assert.assertFalse(MathUtils.equals(new double[]{1.0}, new double[]{1.0, 2.0}));
        Assert.assertTrue(MathUtils.equals(new double[]{1.0, Double.NaN}, new double[]{1.0, Double.NaN}));
        Assert.assertFalse(MathUtils.equals(new double[]{1.0, 2.0}, new double[]{1.0, 3.0}));
    }

    @Test
    public void testFactorial() {
        Assert.assertEquals(1L, MathUtils.factorial(0));
        Assert.assertEquals(1L, MathUtils.factorial(1));
        Assert.assertEquals(2L, MathUtils.factorial(2));
        Assert.assertEquals(120L, MathUtils.factorial(5));
        Assert.assertEquals(2432902008176640000L, MathUtils.factorial(20));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactorialNegative() {
        MathUtils.factorial(-1);
    }

    @Test(expected = ArithmeticException.class)
    public void testFactorialOverflow() {
        MathUtils.factorial(21);
    }

    @Test
    public void testFactorialDouble() {
        Assert.assertEquals(1.0, MathUtils.factorialDouble(0), 1e-10);
        Assert.assertEquals(120.0, MathUtils.factorialDouble(5), 1e-10);
        Assert.assertEquals(2432902008176640000.0, MathUtils.factorialDouble(20), 1e-10);
        Assert.assertEquals(5.109094217170944e19, MathUtils.factorialDouble(21), 1e10);
        Assert.assertTrue(Double.isInfinite(MathUtils.factorialDouble(171)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactorialDoubleNegative() {
        MathUtils.factorialDouble(-1);
    }

    @Test
    public void testFactorialLog() {
        Assert.assertEquals(0.0, MathUtils.factorialLog(0), 1e-10);
        Assert.assertEquals(0.0, MathUtils.factorialLog(1), 1e-10);
        Assert.assertEquals(Math.log(120), MathUtils.factorialLog(5), 1e-10);
        Assert.assertEquals(Math.log(2432902008176640000.0), MathUtils.factorialLog(20), 1e-10);
        Assert.assertEquals(45.38013889847691, MathUtils.factorialLog(22), 1e-10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactorialLogNegative() {
        MathUtils.factorialLog(-1);
    }

    @Test
    public void testGcd() {
        Assert.assertEquals(0, MathUtils.gcd(0, 0));
        Assert.assertEquals(5, MathUtils.gcd(0, 5));
        Assert.assertEquals(5, MathUtils.gcd(5, 0));
        Assert.assertEquals(6, MathUtils.gcd(12, 18));
        Assert.assertEquals(6, MathUtils.gcd(-12, 18));
        Assert.assertEquals(6, MathUtils.gcd(12, -18));
        Assert.assertEquals(6, MathUtils.gcd(-12, -18));
        Assert.assertEquals(1, MathUtils.gcd(17, 13));
        Assert.assertEquals(1 << 15, MathUtils.gcd(1 << 15, 1 << 16));
    }

    @Test(expected = ArithmeticException.class)
    public void testGcdOverflow() {
        MathUtils.gcd(Integer.MIN_VALUE, 0);
    }

    @Test
    public void testLcm() {
        Assert.assertEquals(0, MathUtils.lcm(0, 5));
        Assert.assertEquals(0, MathUtils.lcm(5, 0));
        Assert.assertEquals(36, MathUtils.lcm(12, 18));
        Assert.assertEquals(36, MathUtils.lcm(-12, 18));
        Assert.assertEquals(36, MathUtils.lcm(12, -18));
        Assert.assertEquals(36, MathUtils.lcm(-12, -18));
    }

    @Test(expected = ArithmeticException.class)
    public void testLcmOverflow() {
        MathUtils.lcm(Integer.MAX_VALUE, 2);
    }

    @Test
    public void testHash() {
        Assert.assertEquals(Double.valueOf(1.5).hashCode(), MathUtils.hash(1.5));
        Assert.assertEquals(java.util.Arrays.hashCode(new double[]{1.0, 2.0}), MathUtils.hash(new double[]{1.0, 2.0}));
        Assert.assertEquals(0, MathUtils.hash((double[]) null));
    }

    @Test
    public void testIndicator() {
        Assert.assertEquals((byte) 1, MathUtils.indicator((byte) 5));
        Assert.assertEquals((byte) 1, MathUtils.indicator((byte) 0));
        Assert.assertEquals((byte) -1, MathUtils.indicator((byte) -5));

        Assert.assertEquals(1.0, MathUtils.indicator(5.0), 1e-10);
        Assert.assertEquals(1.0, MathUtils.indicator(0.0), 1e-10);
        Assert.assertEquals(-1.0, MathUtils.indicator(-5.0), 1e-10);
        Assert.assertTrue(Double.isNaN(MathUtils.indicator(Double.NaN)));

        Assert.assertEquals(1.0f, MathUtils.indicator(5.0f), 1e-6f);
        Assert.assertEquals(1.0f, MathUtils.indicator(0.0f), 1e-6f);
        Assert.assertEquals(-1.0f, MathUtils.indicator(-5.0f), 1e-6f);
        Assert.assertTrue(Float.isNaN(MathUtils.indicator(Float.NaN)));

        Assert.assertEquals(1, MathUtils.indicator(5));
        Assert.assertEquals(1, MathUtils.indicator(0));
        Assert.assertEquals(-1, MathUtils.indicator(-5));

        Assert.assertEquals(1L, MathUtils.indicator(5L));
        Assert.assertEquals(1L, MathUtils.indicator(0L));
        Assert.assertEquals(-1L, MathUtils.indicator(-5L));

        Assert.assertEquals((short) 1, MathUtils.indicator((short) 5));
        Assert.assertEquals((short) 1, MathUtils.indicator((short) 0));
        Assert.assertEquals((short) -1, MathUtils.indicator((short) -5));
    }

    @Test
    public void testLog() {
        Assert.assertEquals(2.0, MathUtils.log(2.0, 4.0), 1e-10);
        Assert.assertEquals(3.0, MathUtils.log(10.0, 1000.0), 1e-10);
        Assert.assertTrue(Double.isNaN(MathUtils.log(-2.0, 4.0)));
        Assert.assertTrue(Double.isNaN(MathUtils.log(2.0, -4.0)));
        Assert.assertTrue(Double.isInfinite(MathUtils.log(2.0, 0.0)));
    }

    @Test
    public void testNextAfter() {
        Assert.assertTrue(Double.isNaN(MathUtils.nextAfter(Double.NaN, 1.0)));
        Assert.assertEquals(Double.POSITIVE_INFINITY, MathUtils.nextAfter(Double.POSITIVE_INFINITY, 1.0), 0.0);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, MathUtils.nextAfter(Double.NEGATIVE_INFINITY, 1.0), 0.0);
        Assert.assertEquals(Double.MIN_VALUE, MathUtils.nextAfter(0.0, 1.0), 0.0);
        Assert.assertEquals(-Double.MIN_VALUE, MathUtils.nextAfter(0.0, -1.0), 0.0);

        double nextUp = MathUtils.nextAfter(1.0, 2.0);
        Assert.assertTrue(nextUp > 1.0);
        double nextDown = MathUtils.nextAfter(1.0, 0.0);
        Assert.assertTrue(nextDown < 1.0);

        double d1 = Double.longBitsToDouble(0x000fffffffffffffL);
        double d1Next = MathUtils.nextAfter(d1, 10.0);
        Assert.assertEquals(0x0010000000000000L, Double.doubleToLongBits(d1Next));

        double d2 = Double.longBitsToDouble(0x0010000000000000L);
        double d2Prev = MathUtils.nextAfter(d2, 0.0);
        Assert.assertEquals(0x000fffffffffffffL, Double.doubleToLongBits(d2Prev));
    }

    @Test
    public void testScalb() {
        Assert.assertEquals(0.0, MathUtils.scalb(0.0, 5), 0.0);
        Assert.assertTrue(Double.isNaN(MathUtils.scalb(Double.NaN, 5)));
        Assert.assertEquals(Double.POSITIVE_INFINITY, MathUtils.scalb(Double.POSITIVE_INFINITY, 5), 0.0);
        Assert.assertEquals(4.0, MathUtils.scalb(1.0, 2), 1e-10);
        Assert.assertEquals(0.25, MathUtils.scalb(1.0, -2), 1e-10);
    }

    @Test
    public void testNormalizeAngle() {
        Assert.assertEquals(0.0, MathUtils.normalizeAngle(0.0, 0.0), 1e-10);
        Assert.assertEquals(0.0, MathUtils.normalizeAngle(2 * Math.PI, 0.0), 1e-10);
        Assert.assertEquals(0.0, MathUtils.normalizeAngle(-2 * Math.PI, 0.0), 1e-10);
        Assert.assertEquals(Math.PI, MathUtils.normalizeAngle(3 * Math.PI, 0.0), 1e-10);
        Assert.assertEquals(Math.PI, MathUtils.normalizeAngle(Math.PI, Math.PI), 1e-10);
        Assert.assertEquals(0.5 * Math.PI, MathUtils.normalizeAngle(2.5 * Math.PI, Math.PI), 1e-10);
    }

    @Test
    public void testRoundDouble() {
        Assert.assertEquals(1.23, MathUtils.round(1.234, 2), 1e-10);
        Assert.assertEquals(1.24, MathUtils.round(1.235, 2), 1e-10);
        Assert.assertEquals(Double.POSITIVE_INFINITY, MathUtils.round(Double.POSITIVE_INFINITY, 2), 0.0);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, MathUtils.round(Double.NEGATIVE_INFINITY, 2), 0.0);
        Assert.assertTrue(Double.isNaN(MathUtils.round(Double.NaN, 2)));
    }

    @Test
    public void testRoundFloat() {
        Assert.assertEquals(1.23f, MathUtils.round(1.234f, 2), 1e-6f);
        Assert.assertEquals(1.24f, MathUtils.round(1.235f, 2), 1e-6f);
        Assert.assertEquals(1.23f, MathUtils.round(1.234f, 2, BigDecimal.ROUND_HALF_UP), 1e-6f);
        Assert.assertEquals(1.23f, MathUtils.round(1.236f, 2, BigDecimal.ROUND_DOWN), 1e-6f);
        Assert.assertEquals(1.24f, MathUtils.round(1.231f, 2, BigDecimal.ROUND_UP), 1e-6f);
        Assert.assertEquals(1.24f, MathUtils.round(1.231f, 2, BigDecimal.ROUND_CEILING), 1e-6f);
        Assert.assertEquals(-1.23f, MathUtils.round(-1.231f, 2, BigDecimal.ROUND_CEILING), 1e-6f);
        Assert.assertEquals(1.23f, MathUtils.round(1.239f, 2, BigDecimal.ROUND_FLOOR), 1e-6f);
        Assert.assertEquals(-1.24f, MathUtils.round(-1.231f, 2, BigDecimal.ROUND_FLOOR), 1e-6f);
        Assert.assertEquals(1.23f, MathUtils.round(1.235f, 2, BigDecimal.ROUND_HALF_DOWN), 1e-6f);
        Assert.assertEquals(1.24f, MathUtils.round(1.236f, 2, BigDecimal.ROUND_HALF_DOWN), 1e-6f);
        Assert.assertEquals(1.24f, MathUtils.round(1.235f, 2, BigDecimal.ROUND_HALF_EVEN), 1e-6f);
        Assert.assertEquals(1.24f, MathUtils.round(1.245f, 2, BigDecimal.ROUND_HALF_EVEN), 1e-6f);
        Assert.assertEquals(1.23f, MathUtils.round(1.234f, 2, BigDecimal.ROUND_HALF_EVEN), 1e-6f);
        Assert.assertEquals(1.24f, MathUtils.round(1.236f, 2, BigDecimal.ROUND_HALF_EVEN), 1e-6f);
        Assert.assertEquals(1.25f, MathUtils.round(1.25f, 2, BigDecimal.ROUND_UNNECESSARY), 1e-6f);
    }

    @Test(expected = ArithmeticException.class)
    public void testRoundFloatInexactUnnecessary() {
        MathUtils.round(1.234f, 2, BigDecimal.ROUND_UNNECESSARY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRoundFloatInvalidMethod() {
        MathUtils.round(1.234f, 2, -99);
    }

    @Test
    public void testSign() {
        Assert.assertEquals((byte) 1, MathUtils.sign((byte) 5));
        Assert.assertEquals((byte) 0, MathUtils.sign((byte) 0));
        Assert.assertEquals((byte) -1, MathUtils.sign((byte) -5));

        Assert.assertEquals(1.0, MathUtils.sign(5.0), 1e-10);
        Assert.assertEquals(0.0, MathUtils.sign(0.0), 1e-10);
        Assert.assertEquals(-1.0, MathUtils.sign(-5.0), 1e-10);
        Assert.assertTrue(Double.isNaN(MathUtils.sign(Double.NaN)));

        Assert.assertEquals(1.0f, MathUtils.sign(5.0f), 1e-6f);
        Assert.assertEquals(0.0f, MathUtils.sign(0.0f), 1e-6f);
        Assert.assertEquals(-1.0f, MathUtils.sign(-5.0f), 1e-6f);
        Assert.assertTrue(Float.isNaN(MathUtils.sign(Float.NaN)));

        Assert.assertEquals(1, MathUtils.sign(5));
        Assert.assertEquals(0, MathUtils.sign(0));
        Assert.assertEquals(-1, MathUtils.sign(-5));

        Assert.assertEquals(1L, MathUtils.sign(5L));
        Assert.assertEquals(0L, MathUtils.sign(0L));
        Assert.assertEquals(-1L, MathUtils.sign(-5L));

        Assert.assertEquals((short) 1, MathUtils.sign((short) 5));
        Assert.assertEquals((short) 0, MathUtils.sign((short) 0));
        Assert.assertEquals((short) -1, MathUtils.sign((short) -5));
    }
}
