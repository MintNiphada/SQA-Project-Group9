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
        Assert.assertEquals(1L, MathUtils.addAndCheck(3L, -2L));
        Assert.assertEquals(1L, MathUtils.addAndCheck(-2L, 3L));
        Assert.assertEquals(Long.MAX_VALUE, MathUtils.addAndCheck(Long.MAX_VALUE - 1L, 1L));
        Assert.assertEquals(Long.MIN_VALUE, MathUtils.addAndCheck(Long.MIN_VALUE + 1L, -1L));
    }

    @Test(expected = ArithmeticException.class)
    public void testAddAndCheckLongOverflowPositive() {
        MathUtils.addAndCheck(Long.MAX_VALUE, 1L);
    }

    @Test(expected = ArithmeticException.class)
    public void testAddAndCheckLongOverflowPositiveReverse() {
        MathUtils.addAndCheck(1L, Long.MAX_VALUE);
    }

    @Test(expected = ArithmeticException.class)
    public void testAddAndCheckLongOverflowNegative() {
        MathUtils.addAndCheck(Long.MIN_VALUE, -1L);
    }

    @Test(expected = ArithmeticException.class)
    public void testAddAndCheckLongOverflowNegativeReverse() {
        MathUtils.addAndCheck(-1L, Long.MIN_VALUE);
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
    public void testSubAndCheckIntOverflow() {
        MathUtils.subAndCheck(Integer.MAX_VALUE, -1);
    }

    @Test(expected = ArithmeticException.class)
    public void testSubAndCheckIntUnderflow() {
        MathUtils.subAndCheck(Integer.MIN_VALUE, 1);
    }

    @Test
    public void testSubAndCheckLong() {
        Assert.assertEquals(1L, MathUtils.subAndCheck(3L, 2L));
        Assert.assertEquals(-1L, MathUtils.subAndCheck(2L, 3L));
        Assert.assertEquals(0L, MathUtils.subAndCheck(-1L, -1L));
        Assert.assertEquals(Long.MIN_VALUE, MathUtils.subAndCheck(Long.MIN_VALUE + 1L, 1L));
        Assert.assertEquals(Long.MAX_VALUE, MathUtils.subAndCheck(Long.MAX_VALUE - 1L, -1L));
        Assert.assertEquals(1L, MathUtils.subAndCheck(-1L, Long.MIN_VALUE + 2L));
        Assert.assertEquals(0L, MathUtils.subAndCheck(Long.MIN_VALUE, Long.MIN_VALUE));
    }

    @Test(expected = ArithmeticException.class)
    public void testSubAndCheckLongOverflowPositive() {
        MathUtils.subAndCheck(0L, Long.MIN_VALUE);
    }

    @Test(expected = ArithmeticException.class)
    public void testSubAndCheckLongOverflowNegative() {
        MathUtils.subAndCheck(Long.MIN_VALUE, 1L);
    }

    @Test
    public void testMulAndCheckInt() {
        Assert.assertEquals(6, MathUtils.mulAndCheck(2, 3));
        Assert.assertEquals(-6, MathUtils.mulAndCheck(2, -3));
        Assert.assertEquals(6, MathUtils.mulAndCheck(-2, -3));
        Assert.assertEquals(0, MathUtils.mulAndCheck(0, 100));
        Assert.assertEquals(0, MathUtils.mulAndCheck(100, 0));
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
        Assert.assertEquals(0L, MathUtils.mulAndCheck(0L, 5L));
        Assert.assertEquals(0L, MathUtils.mulAndCheck(5L, 0L));
        Assert.assertEquals(0L, MathUtils.mulAndCheck(0L, -5L));
        Assert.assertEquals(0L, MathUtils.mulAndCheck(-5L, 0L));
        Assert.assertEquals(0L, MathUtils.mulAndCheck(0L, 0L));
    }

    @Test(expected = ArithmeticException.class)
    public void testMulAndCheckLongOverflowPosPos() {
        MathUtils.mulAndCheck(Long.MAX_VALUE, 2L);
    }

    @Test(expected = ArithmeticException.class)
    public void testMulAndCheckLongOverflowNegNeg() {
        MathUtils.mulAndCheck(Long.MIN_VALUE, -1L);
    }

    @Test(expected = ArithmeticException.class)
    public void testMulAndCheckLongOverflowNegPos() {
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
        Assert.assertEquals(2598960L, MathUtils.binomialCoefficient(52, 5));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficientKGreaterThanN() {
        MathUtils.binomialCoefficient(4, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficientNegativeN() {
        MathUtils.binomialCoefficient(-1, 0);
    }

    @Test(expected = ArithmeticException.class)
    public void testBinomialCoefficientOverflow() {
        MathUtils.binomialCoefficient(67, 30);
    }

    @Test
    public void testBinomialCoefficientDoubleAndLog() {
        Assert.assertEquals(1.0, MathUtils.binomialCoefficientDouble(0, 0), 1e-10);
        Assert.assertEquals(1.0, MathUtils.binomialCoefficientDouble(5, 0), 1e-10);
        Assert.assertEquals(1.0, MathUtils.binomialCoefficientDouble(5, 5), 1e-10);
        Assert.assertEquals(5.0, MathUtils.binomialCoefficientDouble(5, 1), 1e-10);
        Assert.assertEquals(5.0, MathUtils.binomialCoefficientDouble(5, 4), 1e-10);
        Assert.assertEquals(10.0, MathUtils.binomialCoefficientDouble(5, 2), 1e-10);
        Assert.assertEquals(0.0, MathUtils.binomialCoefficientLog(0, 0), 1e-10);
        Assert.assertEquals(0.0, MathUtils.binomialCoefficientLog(5, 0), 1e-10);
        Assert.assertEquals(0.0, MathUtils.binomialCoefficientLog(5, 5), 1e-10);
        Assert.assertEquals(Math.log(5.0), MathUtils.binomialCoefficientLog(5, 1), 1e-10);
        Assert.assertEquals(Math.log(5.0), MathUtils.binomialCoefficientLog(5, 4), 1e-10);
        Assert.assertEquals(Math.log(10.0), MathUtils.binomialCoefficientLog(5, 2), 1e-10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficientDoubleKGreaterThanN() {
        MathUtils.binomialCoefficientDouble(3, 4);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficientDoubleNegativeN() {
        MathUtils.binomialCoefficientDouble(-1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficientLogKGreaterThanN() {
        MathUtils.binomialCoefficientLog(3, 4);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBinomialCoefficientLogNegativeN() {
        MathUtils.binomialCoefficientLog(-1, 0);
    }

    @Test
    public void testFactorial() {
        Assert.assertEquals(1L, MathUtils.factorial(0));
        Assert.assertEquals(1L, MathUtils.factorial(1));
        Assert.assertEquals(2L, MathUtils.factorial(2));
        Assert.assertEquals(6L, MathUtils.factorial(3));
        Assert.assertEquals(24L, MathUtils.factorial(4));
        Assert.assertEquals(120L, MathUtils.factorial(5));
        Assert.assertEquals(2432902008176640000L, MathUtils.factorial(20));
        Assert.assertEquals(1.0, MathUtils.factorialDouble(0), 1e-10);
        Assert.assertEquals(1.0, MathUtils.factorialDouble(1), 1e-10);
        Assert.assertEquals(120.0, MathUtils.factorialDouble(5), 1e-10);
        Assert.assertEquals(0.0, MathUtils.factorialLog(0), 1e-10);
        Assert.assertEquals(0.0, MathUtils.factorialLog(1), 1e-10);
        Assert.assertEquals(Math.log(120.0), MathUtils.factorialLog(5), 1e-10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactorialNegative() {
        MathUtils.factorial(-1);
    }

    @Test(expected = ArithmeticException.class)
    public void testFactorialOverflow() {
        MathUtils.factorial(21);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactorialDoubleNegative() {
        MathUtils.factorialDouble(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactorialLogNegative() {
        MathUtils.factorialLog(-1);
    }

    @Test
    public void testCoshSinh() {
        Assert.assertEquals(1.0, MathUtils.cosh(0.0), 1e-10);
        Assert.assertEquals(0.0, MathUtils.sinh(0.0), 1e-10);
        Assert.assertEquals((Math.E + 1.0 / Math.E) / 2.0, MathUtils.cosh(1.0), 1e-10);
        Assert.assertEquals((Math.E - 1.0 / Math.E) / 2.0, MathUtils.sinh(1.0), 1e-10);
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
        Assert.assertTrue(MathUtils.equals(new double[]{1.0, Double.NaN, 3.0}, new double[]{1.0, Double.NaN, 3.0}));
        Assert.assertFalse(MathUtils.equals(new double[]{1.0, 2.0}, new double[]{1.0, 3.0}));
    }

    @Test
    public void testGcd() {
        Assert.assertEquals(6, MathUtils.gcd(30, 12));
        Assert.assertEquals(6, MathUtils.gcd(-30, 12));
        Assert.assertEquals(6, MathUtils.gcd(30, -12));
        Assert.assertEquals(6, MathUtils.gcd(-30, -12));
        Assert.assertEquals(5, MathUtils.gcd(0, 5));
        Assert.assertEquals(5, MathUtils.gcd(5, 0));
        Assert.assertEquals(0, MathUtils.gcd(0, 0));
        Assert.assertEquals(1, MathUtils.gcd(17, 19));
        Assert.assertEquals(1 << 15, MathUtils.gcd(1 << 15, 1 << 16));
    }

    @Test(expected = ArithmeticException.class)
    public void testGcdOverflow() {
        MathUtils.gcd(Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    @Test
    public void testLcm() {
        Assert.assertEquals(60, MathUtils.lcm(30, 12));
        Assert.assertEquals(60, MathUtils.lcm(-30, 12));
        Assert.assertEquals(60, MathUtils.lcm(30, -12));
        Assert.assertEquals(60, MathUtils.lcm(-30, -12));
        Assert.assertEquals(0, MathUtils.lcm(0, 5));
        Assert.assertEquals(0, MathUtils.lcm(5, 0));
    }

    @Test(expected = ArithmeticException.class)
    public void testLcmOverflow() {
        MathUtils.lcm(Integer.MAX_VALUE, Integer.MAX_VALUE - 1);
    }

    @Test
    public void testHash() {
        Assert.assertEquals(Double.valueOf(1.23).hashCode(), MathUtils.hash(1.23));
        Assert.assertEquals(java.util.Arrays.hashCode(new double[]{1.0, 2.0}), MathUtils.hash(new double[]{1.0, 2.0}));
        Assert.assertEquals(0, MathUtils.hash((double[]) null));
    }

    @Test
    public void testIndicator() {
        Assert.assertEquals((byte) 1, MathUtils.indicator((byte) 0));
        Assert.assertEquals((byte) 1, MathUtils.indicator((byte) 5));
        Assert.assertEquals((byte) -1, MathUtils.indicator((byte) -5));

        Assert.assertEquals((short) 1, MathUtils.indicator((short) 0));
        Assert.assertEquals((short) 1, MathUtils.indicator((short) 5));
        Assert.assertEquals((short) -1, MathUtils.indicator((short) -5));

        Assert.assertEquals(1, MathUtils.indicator(0));
        Assert.assertEquals(1, MathUtils.indicator(5));
        Assert.assertEquals(-1, MathUtils.indicator(-5));

        Assert.assertEquals(1L, MathUtils.indicator(0L));
        Assert.assertEquals(1L, MathUtils.indicator(5L));
        Assert.assertEquals(-1L, MathUtils.indicator(-5L));

        Assert.assertEquals(1.0f, MathUtils.indicator(0.0f), 0.0f);
        Assert.assertEquals(1.0f, MathUtils.indicator(5.0f), 0.0f);
        Assert.assertEquals(-1.0f, MathUtils.indicator(-5.0f), 0.0f);
        Assert.assertTrue(Float.isNaN(MathUtils.indicator(Float.NaN)));

        Assert.assertEquals(1.0, MathUtils.indicator(0.0), 0.0);
        Assert.assertEquals(1.0, MathUtils.indicator(5.0), 0.0);
        Assert.assertEquals(-1.0, MathUtils.indicator(-5.0), 0.0);
        Assert.assertTrue(Double.isNaN(MathUtils.indicator(Double.NaN)));
    }

    @Test
    public void testSign() {
        Assert.assertEquals((byte) 0, MathUtils.sign((byte) 0));
        Assert.assertEquals((byte) 1, MathUtils.sign((byte) 5));
        Assert.assertEquals((byte) -1, MathUtils.sign((byte) -5));

        Assert.assertEquals((short) 0, MathUtils.sign((short) 0));
        Assert.assertEquals((short) 1, MathUtils.sign((short) 5));
        Assert.assertEquals((short) -1, MathUtils.sign((short) -5));

        Assert.assertEquals(0, MathUtils.sign(0));
        Assert.assertEquals(1, MathUtils.sign(5));
        Assert.assertEquals(-1, MathUtils.sign(-5));

        Assert.assertEquals(0L, MathUtils.sign(0L));
        Assert.assertEquals(1L, MathUtils.sign(5L));
        Assert.assertEquals(-1L, MathUtils.sign(-5L));

        Assert.assertEquals(0.0f, MathUtils.sign(0.0f), 0.0f);
        Assert.assertEquals(1.0f, MathUtils.sign(5.0f), 0.0f);
        Assert.assertEquals(-1.0f, MathUtils.sign(-5.0f), 0.0f);
        Assert.assertTrue(Float.isNaN(MathUtils.sign(Float.NaN)));

        Assert.assertEquals(0.0, MathUtils.sign(0.0), 0.0);
        Assert.assertEquals(1.0, MathUtils.sign(5.0), 0.0);
        Assert.assertEquals(-1.0, MathUtils.sign(-5.0), 0.0);
        Assert.assertTrue(Double.isNaN(MathUtils.sign(Double.NaN)));
    }

    @Test
    public void testLog() {
        Assert.assertEquals(2.0, MathUtils.log(10.0, 100.0), 1e-10);
        Assert.assertEquals(3.0, MathUtils.log(2.0, 8.0), 1e-10);
    }

    @Test
    public void testNextAfter() {
        Assert.assertTrue(Double.isNaN(MathUtils.nextAfter(Double.NaN, 1.0)));
        Assert.assertTrue(Double.isInfinite(MathUtils.nextAfter(Double.POSITIVE_INFINITY, 1.0)));
        Assert.assertEquals(Double.MIN_VALUE, MathUtils.nextAfter(0.0, 1.0), 0.0);
        Assert.assertEquals(-Double.MIN_VALUE, MathUtils.nextAfter(0.0, -1.0), 0.0);

        double nextUp = MathUtils.nextAfter(1.0, 2.0);
        Assert.assertTrue(nextUp > 1.0);
        double nextDown = MathUtils.nextAfter(1.0, 0.0);
        Assert.assertTrue(nextDown < 1.0);

        double valMaxMantissa = Double.longBitsToDouble(0x000fffffffffffffL);
        double nextOfMaxMantissa = MathUtils.nextAfter(valMaxMantissa, 100.0);
        Assert.assertTrue(nextOfMaxMantissa > valMaxMantissa);

        double valZeroMantissa = Double.longBitsToDouble(0x3ff0000000000000L);
        double prevOfZeroMantissa = MathUtils.nextAfter(valZeroMantissa, 0.0);
        Assert.assertTrue(prevOfZeroMantissa < valZeroMantissa);
    }

    @Test
    public void testScalb() {
        Assert.assertEquals(0.0, MathUtils.scalb(0.0, 5), 0.0);
        Assert.assertTrue(Double.isNaN(MathUtils.scalb(Double.NaN, 5)));
        Assert.assertTrue(Double.isInfinite(MathUtils.scalb(Double.POSITIVE_INFINITY, 5)));
        Assert.assertEquals(8.0, MathUtils.scalb(2.0, 2), 1e-10);
        Assert.assertEquals(0.5, MathUtils.scalb(2.0, -2), 1e-10);
    }

    @Test
    public void testNormalizeAngle() {
        Assert.assertEquals(0.0, MathUtils.normalizeAngle(0.0, 0.0), 1e-10);
        Assert.assertEquals(Math.PI, MathUtils.normalizeAngle(3 * Math.PI, Math.PI), 1e-10);
        Assert.assertEquals(-Math.PI / 2.0, MathUtils.normalizeAngle(1.5 * Math.PI, 0.0), 1e-10);
    }

    @Test
    public void testRoundDouble() {
        Assert.assertEquals(1.23, MathUtils.round(1.234, 2), 1e-10);
        Assert.assertEquals(1.24, MathUtils.round(1.235, 2), 1e-10);
        Assert.assertEquals(1.23, MathUtils.round(1.234, 2, BigDecimal.ROUND_HALF_UP), 1e-10);
        Assert.assertTrue(Double.isInfinite(MathUtils.round(Double.POSITIVE_INFINITY, 2, BigDecimal.ROUND_HALF_UP)));
        Assert.assertTrue(Double.isNaN(MathUtils.round(Double.NaN, 2, BigDecimal.ROUND_HALF_UP)));
    }

    @Test
    public void testRoundFloat() {
        Assert.assertEquals(1.23f, MathUtils.round(1.234f, 2), 1e-5f);
        Assert.assertEquals(1.24f, MathUtils.round(1.235f, 2), 1e-5f);

        Assert.assertEquals(2.0f, MathUtils.round(1.2f, 0, BigDecimal.ROUND_CEILING), 1e-5f);
        Assert.assertEquals(-1.0f, MathUtils.round(-1.2f, 0, BigDecimal.ROUND_CEILING), 1e-5f);

        Assert.assertEquals(1.0f, MathUtils.round(1.8f, 0, BigDecimal.ROUND_DOWN), 1e-5f);
        Assert.assertEquals(-1.0f, MathUtils.round(-1.8f, 0, BigDecimal.ROUND_DOWN), 1e-5f);

        Assert.assertEquals(1.0f, MathUtils.round(1.8f, 0, BigDecimal.ROUND_FLOOR), 1e-5f);
        Assert.assertEquals(-2.0f, MathUtils.round(-1.2f, 0, BigDecimal.ROUND_FLOOR), 1e-5f);

        Assert.assertEquals(1.0f, MathUtils.round(1.5f, 0, BigDecimal.ROUND_HALF_DOWN), 1e-5f);
        Assert.assertEquals(2.0f, MathUtils.round(1.6f, 0, BigDecimal.ROUND_HALF_DOWN), 1e-5f);

        Assert.assertEquals(2.0f, MathUtils.round(2.5f, 0, BigDecimal.ROUND_HALF_EVEN), 1e-5f);
        Assert.assertEquals(4.0f, MathUtils.round(3.5f, 0, BigDecimal.ROUND_HALF_EVEN), 1e-5f);
        Assert.assertEquals(2.0f, MathUtils.round(2.2f, 0, BigDecimal.ROUND_HALF_EVEN), 1e-5f);
        Assert.assertEquals(3.0f, MathUtils.round(2.8f, 0, BigDecimal.ROUND_HALF_EVEN), 1e-5f);

        Assert.assertEquals(2.0f, MathUtils.round(1.5f, 0, BigDecimal.ROUND_HALF_UP), 1e-5f);
        Assert.assertEquals(1.0f, MathUtils.round(1.4f, 0, BigDecimal.ROUND_HALF_UP), 1e-5f);

        Assert.assertEquals(2.0f, MathUtils.round(1.1f, 0, BigDecimal.ROUND_UP), 1e-5f);
        Assert.assertEquals(1.0f, MathUtils.round(1.0f, 0, BigDecimal.ROUND_UNNECESSARY), 1e-5f);
    }

    @Test(expected = ArithmeticException.class)
    public void testRoundUnnecessaryInexact() {
        MathUtils.round(1.5f, 0, BigDecimal.ROUND_UNNECESSARY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRoundInvalidMethod() {
        MathUtils.round(1.5f, 0, 9999);
    }

    @Test
    public void testConstants() {
        Assert.assertEquals(0x1.0p-53, MathUtils.EPSILON, 0.0);
        Assert.assertEquals(0x1.0p-1022, MathUtils.SAFE_MIN, 0.0);
    }
}
