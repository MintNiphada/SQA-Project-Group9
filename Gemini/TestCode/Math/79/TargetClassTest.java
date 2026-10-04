package org.apache.commons.math.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.Assert;
import org.junit.Test;

public class MathUtilsTest {

    @Test
    public void testAddAndCheckInt() {
        Assert.assertEquals(5, MathUtils.addAndCheck(2, 3));
        Assert.assertEquals(-5, MathUtils.addAndCheck(-2, -3));
        Assert.assertEquals(Integer.MAX_VALUE, MathUtils.addAndCheck(Integer.MAX_VALUE - 1, 1));
        Assert.assertEquals(Integer.MIN_VALUE, MathUtils.addAndCheck(Integer.MIN_VALUE + 1, -1));
    }

    @Test(expected = ArithmeticException.class)
    public void testAddAndCheckIntOverflowPositive() {
        MathUtils.addAndCheck(Integer.MAX_VALUE, 1);
    }

    @Test(expected = ArithmeticException.class)
    public void testAddAndCheckIntOverflowNegative() {
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
    public void testAddAndCheckLongOverflowPositive() {
        MathUtils.addAndCheck(Long.MAX_VALUE, 1L);
    }

    @Test(expected = ArithmeticException.class)
    public void testAddAndCheckLongOverflowNegative() {
        MathUtils.addAndCheck(Long.MIN_VALUE, -1L);
    }

    @Test
    public void testBinomialCoefficient() {
        Assert.assertEquals(1L, MathUtils.binomialCoefficient(5, 0));
        Assert.assertEquals(1L, MathUtils.binomialCoefficient(5, 5));
        Assert.assertEquals(5L, MathUtils.binomialCoefficient(5, 1));
        Assert.assertEquals(5L, MathUtils.binomialCoefficient(5, 4));
        Assert.assertEquals(10L, MathUtils.binomialCoefficient(5, 2));
        Assert.assertEquals(10L, MathUtils.binomialCoefficient(5, 3));
        Assert.assertEquals(184756L, MathUtils.binomialCoefficient(20, 10));
        Assert.assertEquals(278256L, MathUtils.binomialCoefficient(64, 3));
        Assert.assertEquals(67L, MathUtils.binomialCoefficient(67, 1));
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
        MathUtils.binomialCoefficient(67, 30);
    }

    @Test
    public void testBinomialCoefficientDouble() {
        Assert.assertEquals(1.0, MathUtils.binomialCoefficientDouble(5, 0), 1e-10);
        Assert.assertEquals(1.0, MathUtils.binomialCoefficientDouble(5, 5), 1e-10);
        Assert.assertEquals(5.0, MathUtils.binomialCoefficientDouble(5, 1), 1e-10);
        Assert.assertEquals(5.0, MathUtils.binomialCoefficientDouble(5, 4), 1e-10);
        Assert.assertEquals(10.0, MathUtils.binomialCoefficientDouble(5, 2), 1e-10);
        Assert.assertEquals(10.0, MathUtils.binomialCoefficientDouble(5, 3), 1e-10);
        Assert.assertEquals(184756.0, MathUtils.binomialCoefficientDouble(20, 10), 1e-10);
        Assert.assertTrue(MathUtils.binomialCoefficientDouble(70, 35) > 0);
        Assert.assertEquals(Double.POSITIVE_INFINITY, MathUtils.binomialCoefficientDouble(1030, 500), 1e-10);
    }

    @Test
    public void testBinomialCoefficientLog() {
        Assert.assertEquals(0.0, MathUtils.binomialCoefficientLog(5, 0), 1e-10);
        Assert.assertEquals(0.0, MathUtils.binomialCoefficientLog(5, 5), 1e-10);
        Assert.assertEquals(Math.log(5), MathUtils.binomialCoefficientLog(5, 1), 1e-10);
        Assert.assertEquals(Math.log(5), MathUtils.binomialCoefficientLog(5, 4), 1e-10);
        Assert.assertEquals(Math.log(10), MathUtils.binomialCoefficientLog(5, 2), 1e-10);
        Assert.assertTrue(MathUtils.binomialCoefficientLog(70, 30) > 0);
        Assert.assertTrue(MathUtils.binomialCoefficientLog(1030, 20) > 0);
        Assert.assertTrue(MathUtils.binomialCoefficientLog(1030, 1010) > 0);
    }

    @Test
    public void testCompareTo() {
        Assert.assertEquals(0, MathUtils.compareTo(1.0, 1.0, 0.0));
        Assert.assertEquals(0, MathUtils.compareTo(1.0, 1.05, 0.1));
        Assert.assertEquals(-1, MathUtils.compareTo(1.0, 2.0, 0.1));
        Assert.assertEquals(1, MathUtils.compareTo(2.0, 1.0, 0.1));
    }

    @Test
    public void testCoshAndSinh() {
        Assert.assertEquals(1.0, MathUtils.cosh(0.0), 1e-10);
        Assert.assertEquals(0.0, MathUtils.sinh(0.0), 1e-10);
        Assert.assertEquals(MathUtils.cosh(1.5), MathUtils.cosh(-1.5), 1e-10);
        Assert.assertEquals(-MathUtils.sinh(1.5), MathUtils.sinh(-1.5), 1e-10);
    }

    @Test
    public void testEqualsDouble() {
        Assert.assertTrue(MathUtils.equals(Double.NaN, Double.NaN));
        Assert.assertFalse(MathUtils.equals(Double.NaN, 1.0));
        Assert.assertFalse(MathUtils.equals(1.0, Double.NaN));
        Assert.assertTrue(MathUtils.equals(1.0, 1.0));
        Assert.assertFalse(MathUtils.equals(1.0, 2.0));
    }

    @Test
    public void testEqualsDoubleWithEps() {
        Assert.assertTrue(MathUtils.equals(1.0, 1.05, 0.1));
        Assert.assertFalse(MathUtils.equals(1.0, 1.2, 0.1));
        Assert.assertTrue(MathUtils.equals(Double.NaN, Double.NaN, 0.1));
        Assert.assertTrue(MathUtils.equals(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 0.1));
        Assert.assertFalse(MathUtils.equals(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 0.1));
    }

    @Test
    public void testEqualsDoubleWithMaxUlps() {
        Assert.assertTrue(MathUtils.equals(0.0, 0.0, 1));
        Assert.assertTrue(MathUtils.equals(0.0, -0.0, 1));
        Assert.assertTrue(MathUtils.equals(-1.0, -1.0, 1));
        Assert.assertTrue(MathUtils.equals(1.0, Math.nextUp(1.0), 1));
        Assert.assertFalse(MathUtils.equals(1.0, 2.0, 1));
        Assert.assertTrue(MathUtils.equals(-1.0, -Math.nextUp(1.0), 2));
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
        Assert.assertEquals(6L, MathUtils.factorial(3));
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
        Assert.assertEquals(MathUtils.factorial(20), MathUtils.factorialDouble(20), 1e-10);
        Assert.assertTrue(MathUtils.factorialDouble(25) > 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactorialDoubleNegative() {
        MathUtils.factorialDouble(-1);
    }

    @Test
    public void testFactorialLog() {
        Assert.assertEquals(0.0, MathUtils.factorialLog(0), 1e-10);
        Assert.assertEquals(Math.log(120), MathUtils.factorialLog(5), 1e-10);
        Assert.assertEquals(Math.log(MathUtils.factorial(20)), MathUtils.factorialLog(20), 1e-10);
        Assert.assertTrue(MathUtils.factorialLog(25) > 0);
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
        Assert.assertEquals(5, MathUtils.gcd(0, -5));
        Assert.assertEquals(5, MathUtils.gcd(-5, 0));
        Assert.assertEquals(6, MathUtils.gcd(30, 24));
        Assert.assertEquals(6, MathUtils.gcd(-30, 24));
        Assert.assertEquals(6, MathUtils.gcd(30, -24));
        Assert.assertEquals(6, MathUtils.gcd(-30, -24));
        Assert.assertEquals(1, MathUtils.gcd(17, 19));
        Assert.assertEquals(1 << 10, MathUtils.gcd(1 << 10, 1 << 12));
    }

    @Test(expected = ArithmeticException.class)
    public void testGcdOverflowMinMin() {
        MathUtils.gcd(Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    @Test(expected = ArithmeticException.class)
    public void testGcdOverflowMinZero() {
        MathUtils.gcd(Integer.MIN_VALUE, 0);
    }

    @Test(expected = ArithmeticException.class)
    public void testGcdOverflowZeroMin() {
        MathUtils.gcd(0, Integer.MIN_VALUE);
    }

    @Test
    public void testHash() {
        Assert.assertEquals(Double.valueOf(1.23).hashCode(), MathUtils.hash(1.23));
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

        Assert.assertEquals(1.0F, MathUtils.indicator(5.0F), 1e-10F);
        Assert.assertEquals(1.0F, MathUtils.indicator(0.0F), 1e-10F);
        Assert.assertEquals(-1.0F, MathUtils.indicator(-5.0F), 1e-10F);
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
    public void testLcm() {
        Assert.assertEquals(0, MathUtils.lcm(0, 5));
        Assert.assertEquals(0, MathUtils.lcm(5, 0));
        Assert.assertEquals(12, MathUtils.lcm(4, 6));
        Assert.assertEquals(12, MathUtils.lcm(-4, 6));
        Assert.assertEquals(12, MathUtils.lcm(4, -6));
        Assert.assertEquals(12, MathUtils.lcm(-4, -6));
    }

    @Test(expected = ArithmeticException.class)
    public void testLcmOverflow() {
        MathUtils.lcm(Integer.MIN_VALUE, 1);
    }

    @Test
    public void testLog() {
        Assert.assertEquals(3.0, MathUtils.log(2.0, 8.0), 1e-10);
        Assert.assertEquals(2.0, MathUtils.log(10.0, 100.0), 1e-10);
    }

    @Test
    public void testMulAndCheckInt() {
        Assert.assertEquals(6, MathUtils.mulAndCheck(2, 3));
        Assert.assertEquals(-6, MathUtils.mulAndCheck(-2, 3));
        Assert.assertEquals(6, MathUtils.mulAndCheck(-2, -3));
        Assert.assertEquals(0, MathUtils.mulAndCheck(0, 3));
    }

    @Test(expected = ArithmeticException.class)
    public void testMulAndCheckIntOverflowPositive() {
        MathUtils.mulAndCheck(Integer.MAX_VALUE, 2);
    }

    @Test(expected = ArithmeticException.class)
    public void testMulAndCheckIntOverflowNegative() {
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
        Assert.assertEquals(0L, MathUtils.mulAndCheck(0L, 3L));
        Assert.assertEquals(0L, MathUtils.mulAndCheck(3L, 0L));
        Assert.assertEquals(0L, MathUtils.mulAndCheck(0L, -3L));
        Assert.assertEquals(0L, MathUtils.mulAndCheck(-3L, 0L));
        Assert.assertEquals(0L, MathUtils.mulAndCheck(0L, 0L));
    }

    @Test(expected = ArithmeticException.class)
    public void testMulAndCheckLongOverflowPositive() {
        MathUtils.mulAndCheck(Long.MAX_VALUE, 2L);
    }

    @Test(expected = ArithmeticException.class)
    public void testMulAndCheckLongOverflowNegativePos() {
        MathUtils.mulAndCheck(Long.MIN_VALUE, 2L);
    }

    @Test(expected = ArithmeticException.class)
    public void testMulAndCheckLongOverflowNegNeg() {
        MathUtils.mulAndCheck(Long.MIN_VALUE, -2L);
    }

    @Test
    public void testNextAfter() {
        Assert.assertTrue(Double.isNaN(MathUtils.nextAfter(Double.NaN, 1.0)));
        Assert.assertEquals(Double.POSITIVE_INFINITY, MathUtils.nextAfter(Double.POSITIVE_INFINITY, 1.0), 0.0);
        Assert.assertEquals(Double.MIN_VALUE, MathUtils.nextAfter(0.0, 1.0), 0.0);
        Assert.assertEquals(-Double.MIN_VALUE, MathUtils.nextAfter(0.0, -1.0), 0.0);
        Assert.assertTrue(MathUtils.nextAfter(1.0, 2.0) > 1.0);
        Assert.assertTrue(MathUtils.nextAfter(1.0, 0.0) < 1.0);
        Assert.assertTrue(MathUtils.nextAfter(-1.0, -2.0) < -1.0);
        Assert.assertTrue(MathUtils.nextAfter(-1.0, 0.0) > -1.0);
        Assert.assertEquals(2.0, MathUtils.nextAfter(Math.nextAfter(2.0, 1.0), 3.0), 0.0);
    }

    @Test
    public void testScalb() {
        Assert.assertEquals(0.0, MathUtils.scalb(0.0, 5), 0.0);
        Assert.assertTrue(Double.isNaN(MathUtils.scalb(Double.NaN, 5)));
        Assert.assertEquals(Double.POSITIVE_INFINITY, MathUtils.scalb(Double.POSITIVE_INFINITY, 5), 0.0);
        Assert.assertEquals(8.0, MathUtils.scalb(1.0, 3), 1e-10);
        Assert.assertEquals(0.25, MathUtils.scalb(1.0, -2), 1e-10);
    }

    @Test
    public void testNormalizeAngle() {
        Assert.assertEquals(0.0, MathUtils.normalizeAngle(0.0, 0.0), 1e-10);
        Assert.assertEquals(0.0, MathUtils.normalizeAngle(2 * Math.PI, 0.0), 1e-10);
        Assert.assertEquals(Math.PI, MathUtils.normalizeAngle(-Math.PI, Math.PI), 1e-10);
    }

    @Test
    public void testNormalizeArray() {
        double[] in = new double[]{1.0, 2.0, 3.0, Double.NaN};
        double[] out = MathUtils.normalizeArray(in, 12.0);
        Assert.assertEquals(2.0, out[0], 1e-10);
        Assert.assertEquals(4.0, out[1], 1e-10);
        Assert.assertEquals(6.0, out[2], 1e-10);
        Assert.assertTrue(Double.isNaN(out[3]));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNormalizeArrayTargetInfinite() {
        MathUtils.normalizeArray(new double[]{1.0}, Double.POSITIVE_INFINITY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNormalizeArrayTargetNaN() {
        MathUtils.normalizeArray(new double[]{1.0}, Double.NaN);
    }

    @Test(expected = ArithmeticException.class)
    public void testNormalizeArrayWithInfiniteElement() {
        MathUtils.normalizeArray(new double[]{1.0, Double.POSITIVE_INFINITY}, 1.0);
    }

    @Test(expected = ArithmeticException.class)
    public void testNormalizeArrayZeroSum() {
        MathUtils.normalizeArray(new double[]{1.0, -1.0}, 1.0);
    }

    @Test
    public void testRoundDouble() {
        Assert.assertEquals(1.23, MathUtils.round(1.234, 2), 1e-10);
        Assert.assertEquals(1.24, MathUtils.round(1.235, 2), 1e-10);
        Assert.assertEquals(Double.POSITIVE_INFINITY, MathUtils.round(Double.POSITIVE_INFINITY, 2), 0.0);
        Assert.assertTrue(Double.isNaN(MathUtils.round(Double.NaN, 2)));
    }

    @Test
    public void testRoundFloat() {
        Assert.assertEquals(1.23f, MathUtils.round(1.234f, 2), 1e-5f);
        Assert.assertEquals(1.24f, MathUtils.round(1.235f, 2), 1e-5f);
        Assert.assertEquals(2.0f, MathUtils.round(1.2f, 0, BigDecimal.ROUND_UP), 1e-5f);
        Assert.assertEquals(-2.0f, MathUtils.round(-1.2f, 0, BigDecimal.ROUND_UP), 1e-5f);
        Assert.assertEquals(1.0f, MathUtils.round(1.8f, 0, BigDecimal.ROUND_DOWN), 1e-5f);
        Assert.assertEquals(2.0f, MathUtils.round(1.2f, 0, BigDecimal.ROUND_CEILING), 1e-5f);
        Assert.assertEquals(-1.0f, MathUtils.round(-1.2f, 0, BigDecimal.ROUND_CEILING), 1e-5f);
        Assert.assertEquals(1.0f, MathUtils.round(1.8f, 0, BigDecimal.ROUND_FLOOR), 1e-5f);
        Assert.assertEquals(-2.0f, MathUtils.round(-1.2f, 0, BigDecimal.ROUND_FLOOR), 1e-5f);
        Assert.assertEquals(1.0f, MathUtils.round(1.5f, 0, BigDecimal.ROUND_HALF_DOWN), 1e-5f);
        Assert.assertEquals(2.0f, MathUtils.round(1.6f, 0, BigDecimal.ROUND_HALF_DOWN), 1e-5f);
        Assert.assertEquals(2.0f, MathUtils.round(1.5f, 0, BigDecimal.ROUND_HALF_EVEN), 1e-5f);
        Assert.assertEquals(2.0f, MathUtils.round(2.5f, 0, BigDecimal.ROUND_HALF_EVEN), 1e-5f);
        Assert.assertEquals(2.0f, MathUtils.round(2.3f, 0, BigDecimal.ROUND_HALF_EVEN), 1e-5f);
        Assert.assertEquals(3.0f, MathUtils.round(2.7f, 0, BigDecimal.ROUND_HALF_EVEN), 1e-5f);
        Assert.assertEquals(2.0f, MathUtils.round(2.0f, 0, BigDecimal.ROUND_UNNECESSARY), 1e-5f);
    }

    @Test(expected = ArithmeticException.class)
    public void testRoundFloatUnnecessaryException() {
        MathUtils.round(1.5f, 0, BigDecimal.ROUND_UNNECESSARY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRoundFloatInvalidRoundingMethod() {
        MathUtils.round(1.5f, 0, 999);
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

        Assert.assertEquals(1.0f, MathUtils.sign(5.0f), 1e-10f);
        Assert.assertEquals(0.0f, MathUtils.sign(0.0f), 1e-10f);
        Assert.assertEquals(-1.0f, MathUtils.sign(-5.0f), 1e-10f);
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

    @Test
    public void testSubAndCheckInt() {
        Assert.assertEquals(-1, MathUtils.subAndCheck(2, 3));
        Assert.assertEquals(1, MathUtils.subAndCheck(3, 2));
        Assert.assertEquals(Integer.MIN_VALUE, MathUtils.subAndCheck(Integer.MIN_VALUE + 1, 1));
    }

    @Test(expected = ArithmeticException.class)
    public void testSubAndCheckIntOverflowPositive() {
        MathUtils.subAndCheck(Integer.MAX_VALUE, -1);
    }

    @Test(expected = ArithmeticException.class)
    public void testSubAndCheckIntOverflowNegative() {
        MathUtils.subAndCheck(Integer.MIN_VALUE, 1);
    }

    @Test
    public void testSubAndCheckLong() {
        Assert.assertEquals(-1L, MathUtils.subAndCheck(2L, 3L));
        Assert.assertEquals(1L, MathUtils.subAndCheck(3L, 2L));
        Assert.assertEquals(-1L, MathUtils.subAndCheck(-2L, Long.MIN_VALUE + 1L));
        Assert.assertEquals(0L, MathUtils.subAndCheck(Long.MIN_VALUE, Long.MIN_VALUE));
        Assert.assertEquals(Long.MAX_VALUE, MathUtils.subAndCheck(Long.MAX_VALUE - 1L, -1L));
    }

    @Test(expected = ArithmeticException.class)
    public void testSubAndCheckLongOverflow() {
        MathUtils.subAndCheck(Long.MAX_VALUE, -1L);
    }

    @Test(expected = ArithmeticException.class)
    public void testSubAndCheckLongMinValuePositive() {
        MathUtils.subAndCheck(0L, Long.MIN_VALUE);
    }

    @Test
    public void testPowIntInt() {
        Assert.assertEquals(1, MathUtils.pow(5, 0));
        Assert.assertEquals(8, MathUtils.pow(2, 3));
        Assert.assertEquals(81, MathUtils.pow(3, 4));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPowIntIntNegative() {
        MathUtils.pow(2, -1);
    }

    @Test
    public void testPowIntLong() {
        Assert.assertEquals(1, MathUtils.pow(5, 0L));
        Assert.assertEquals(8, MathUtils.pow(2, 3L));
        Assert.assertEquals(81, MathUtils.pow(3, 4L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPowIntLongNegative() {
        MathUtils.pow(2, -1L);
    }

    @Test
    public void testPowLongInt() {
        Assert.assertEquals(1L, MathUtils.pow(5L, 0));
        Assert.assertEquals(8L, MathUtils.pow(2L, 3));
        Assert.assertEquals(81L, MathUtils.pow(3L, 4));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPowLongIntNegative() {
        MathUtils.pow(2L, -1);
    }

    @Test
    public void testPowLongLong() {
        Assert.assertEquals(1L, MathUtils.pow(5L, 0L));
        Assert.assertEquals(8L, MathUtils.pow(2L, 3L));
        Assert.assertEquals(81L, MathUtils.pow(3L, 4L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPowLongLongNegative() {
        MathUtils.pow(2L, -1L);
    }

    @Test
    public void testPowBigIntegerInt() {
        Assert.assertEquals(BigInteger.ONE, MathUtils.pow(BigInteger.valueOf(5), 0));
        Assert.assertEquals(BigInteger.valueOf(8), MathUtils.pow(BigInteger.valueOf(2), 3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPowBigIntegerIntNegative() {
        MathUtils.pow(BigInteger.valueOf(2), -1);
    }

    @Test
    public void testPowBigIntegerLong() {
        Assert.assertEquals(BigInteger.ONE, MathUtils.pow(BigInteger.valueOf(5), 0L));
        Assert.assertEquals(BigInteger.valueOf(8), MathUtils.pow(BigInteger.valueOf(2), 3L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPowBigIntegerLongNegative() {
        MathUtils.pow(BigInteger.valueOf(2), -1L);
    }

    @Test
    public void testPowBigIntegerBigInteger() {
        Assert.assertEquals(BigInteger.ONE, MathUtils.pow(BigInteger.valueOf(5), BigInteger.ZERO));
        Assert.assertEquals(BigInteger.valueOf(8), MathUtils.pow(BigInteger.valueOf(2), BigInteger.valueOf(3)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPowBigIntegerBigIntegerNegative() {
        MathUtils.pow(BigInteger.valueOf(2), BigInteger.valueOf(-1));
    }

    @Test
    public void testDistances() {
        double[] p1 = new double[]{1.0, 2.0};
        double[] p2 = new double[]{4.0, 6.0};
        int[] ip1 = new int[]{1, 2};
        int[] ip2 = new int[]{4, 6};

        Assert.assertEquals(7.0, MathUtils.distance1(p1, p2), 1e-10);
        Assert.assertEquals(7, MathUtils.distance1(ip1, ip2));

        Assert.assertEquals(5.0, MathUtils.distance(p1, p2), 1e-10);
        Assert.assertEquals(5.0, MathUtils.distance(ip1, ip2), 1e-10);

        Assert.assertEquals(4.0, MathUtils.distanceInf(p1, p2), 1e-10);
        Assert.assertEquals(4, MathUtils.distanceInf(ip1, ip2));
    }
}
