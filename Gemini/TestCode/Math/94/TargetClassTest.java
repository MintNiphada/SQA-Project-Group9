package org.apache.commons.math.util;

import java.math.BigDecimal;
import org.junit.Assert;
import org.junit.Test;

public class MathUtilsTest {

    @Test
    public void testAddAndCheckInt() {
        Assert.assertEquals(5, MathUtils.addAndCheck(2, 3));
        Assert.assertEquals(-1, MathUtils.addAndCheck(2, -3));
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
        Assert.assertEquals(-1L, MathUtils.addAndCheck(2L, -3L));
        Assert.assertEquals(-1L, MathUtils.addAndCheck(-3L, 2L));
        Assert.assertEquals(-5L, MathUtils.addAndCheck(-2L, -3L));
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
    public void testSubAndCheckInt() {
        Assert.assertEquals(-1, MathUtils.subAndCheck(2, 3));
        Assert.assertEquals(5, MathUtils.subAndCheck(2, -3));
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
        Assert.assertEquals(5L, MathUtils.subAndCheck(2L, -3L));
        Assert.assertEquals(-1L, MathUtils.subAndCheck(-2L, Long.MIN_VALUE + 1L + Long.MAX_VALUE));
        Assert.assertEquals(0L, MathUtils.subAndCheck(Long.MIN_VALUE, Long.MIN_VALUE));
        Assert.assertEquals(Long.MIN_VALUE, MathUtils.subAndCheck(Long.MIN_VALUE + 1L, 1L));
    }

    @Test(expected = ArithmeticException.class)
    public void testSubAndCheckLongOverflowMin() {
        MathUtils.subAndCheck(0L, Long.MIN_VALUE);
    }

    @Test(expected = ArithmeticException.class)
    public void testSubAndCheckLongOverflowPositive() {
        MathUtils.subAndCheck(Long.MAX_VALUE, -1L);
    }

    @Test
    public void testMulAndCheckInt() {
        Assert.assertEquals(6, MathUtils.mulAndCheck(2, 3));
        Assert.assertEquals(-6, MathUtils.mulAndCheck(2, -3));
        Assert.assertEquals(0, MathUtils.mulAndCheck(0, 5));
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
        Assert.assertEquals(-6L, MathUtils.mulAndCheck(2L, -3L));
        Assert.assertEquals(-6L, MathUtils.mulAndCheck(-3L, 2L));
        Assert.assertEquals(6L, MathUtils.mulAndCheck(-2L, -3L));
        Assert.assertEquals(0L, MathUtils.mulAndCheck(0L, 5L));
        Assert.assertEquals(0L, MathUtils.mulAndCheck(5L, 0L));
        Assert.assertEquals(0L, MathUtils.mulAndCheck(0L, -5L));
        Assert.assertEquals(0L, MathUtils.mulAndCheck(-5L, 0L));
        Assert.assertEquals(0L, MathUtils.mulAndCheck(0L, 0L));
        Assert.assertEquals(Long.MIN_VALUE, MathUtils.mulAndCheck(Long.MIN_VALUE / 2L, 2L));
        Assert.assertEquals(Long.MAX_VALUE, MathUtils.mulAndCheck(Long.MAX_VALUE, 1L));
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
        Assert.assertEquals(67603900L, MathUtils.binomialCoefficient(67, 5));
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
    public void testBinomialCoefficientDoubleAndLog() {
        Assert.assertEquals(1.0, MathUtils.binomialCoefficientDouble(0, 0), 1e-10);
        Assert.assertEquals(0.0, MathUtils.binomialCoefficientLog(0, 0), 1e-10);
        Assert.assertEquals(5.0, MathUtils.binomialCoefficientDouble(5, 1), 1e-10);
        Assert.assertEquals(Math.log(5.0), MathUtils.binomialCoefficientLog(5, 1), 1e-10);
        Assert.assertEquals(10.0, MathUtils.binomialCoefficientDouble(5, 2), 1e-10);
        Assert.assertTrue(MathUtils.binomialCoefficientDouble(1030, 515) > 0);
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
    public void testCoshAndSinh() {
        Assert.assertEquals(1.0, MathUtils.cosh(0.0), 1e-10);
        Assert.assertEquals(0.0, MathUtils.sinh(0.0), 1e-10);
        Assert.assertEquals(MathUtils.cosh(2.0), MathUtils.cosh(-2.0), 1e-10);
        Assert.assertEquals(-MathUtils.sinh(2.0), MathUtils.sinh(-2.0), 1e-10);
    }

    @Test
    public void testEqualsDouble() {
        Assert.assertTrue(MathUtils.equals(Double.NaN, Double.NaN));
        Assert.assertTrue(MathUtils.equals(1.0, 1.0));
        Assert.assertFalse(MathUtils.equals(1.0, 2.0));
        Assert.assertFalse(MathUtils.equals(1.0, Double.NaN));
        Assert.assertFalse(MathUtils.equals(Double.NaN, 1.0));
    }

    @Test
    public void testEqualsDoubleArray() {
        double[] a1 = null;
        double[] a2 = null;
        double[] a3 = new double[] {1.0, Double.NaN};
        double[] a4 = new double[] {1.0, Double.NaN};
        double[] a5 = new double[] {1.0, 2.0};
        double[] a6 = new double[] {1.0};

        Assert.assertTrue(MathUtils.equals(a1, a2));
        Assert.assertFalse(MathUtils.equals(a1, a3));
        Assert.assertFalse(MathUtils.equals(a3, a1));
        Assert.assertTrue(MathUtils.equals(a3, a4));
        Assert.assertFalse(MathUtils.equals(a3, a5));
        Assert.assertFalse(MathUtils.equals(a3, a6));
    }

    @Test
    public void testFactorial() {
        Assert.assertEquals(1L, MathUtils.factorial(0));
        Assert.assertEquals(1L, MathUtils.factorial(1));
        Assert.assertEquals(2L, MathUtils.factorial(2));
        Assert.assertEquals(6L, MathUtils.factorial(3));
        Assert.assertEquals(2432902008176640000L, MathUtils.factorial(20));
        Assert.assertEquals(1.0, MathUtils.factorialDouble(0), 1e-10);
        Assert.assertEquals(0.0, MathUtils.factorialLog(0), 1e-10);
        Assert.assertEquals(0.0, MathUtils.factorialLog(1), 1e-10);
        Assert.assertEquals(Math.log(24.0), MathUtils.factorialLog(4), 1e-10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactorialNegative() {
        MathUtils.factorial(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactorialDoubleNegative() {
        MathUtils.factorialDouble(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactorialLogNegative() {
        MathUtils.factorialLog(-1);
    }

    @Test(expected = ArithmeticException.class)
    public void testFactorialOverflow() {
        MathUtils.factorial(21);
    }

    @Test
    public void testGcd() {
        Assert.assertEquals(4, MathUtils.gcd(0, 4));
        Assert.assertEquals(4, MathUtils.gcd(4, 0));
        Assert.assertEquals(0, MathUtils.gcd(0, 0));
        Assert.assertEquals(6, MathUtils.gcd(30, 24));
        Assert.assertEquals(6, MathUtils.gcd(-30, 24));
        Assert.assertEquals(6, MathUtils.gcd(30, -24));
        Assert.assertEquals(6, MathUtils.gcd(-30, -24));
        Assert.assertEquals(1, MathUtils.gcd(17, 19));
        Assert.assertEquals(1 << 30, MathUtils.gcd(1 << 30, 1 << 30));
    }

    @Test(expected = ArithmeticException.class)
    public void testGcdOverflow() {
        MathUtils.gcd(Integer.MIN_VALUE, 0);
    }

    @Test
    public void testLcm() {
        Assert.assertEquals(0, MathUtils.lcm(0, 4));
        Assert.assertEquals(12, MathUtils.lcm(4, 6));
        Assert.assertEquals(12, MathUtils.lcm(-4, 6));
        Assert.assertEquals(12, MathUtils.lcm(4, -6));
    }

    @Test(expected = ArithmeticException.class)
    public void testLcmOverflow() {
        MathUtils.lcm(Integer.MAX_VALUE, Integer.MAX_VALUE - 1);
    }

    @Test
    public void testHash() {
        Assert.assertEquals(Double.valueOf(1.23).hashCode(), MathUtils.hash(1.23));
        Assert.assertEquals(java.util.Arrays.hashCode(new double[] {1.0, 2.0}), MathUtils.hash(new double[] {1.0, 2.0}));
        Assert.assertEquals(0, MathUtils.hash((double[]) null));
    }

    @Test
    public void testIndicators() {
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
        Assert.assertEquals(3.0, MathUtils.log(2.0, 8.0), 1e-10);
    }

    @Test
    public void testNextAfter() {
        Assert.assertTrue(Double.isNaN(MathUtils.nextAfter(Double.NaN, 1.0)));
        Assert.assertTrue(Double.isInfinite(MathUtils.nextAfter(Double.POSITIVE_INFINITY, 1.0)));
        Assert.assertEquals(Double.MIN_VALUE, MathUtils.nextAfter(0.0, 1.0), 0.0);
        Assert.assertEquals(-Double.MIN_VALUE, MathUtils.nextAfter(0.0, -1.0), 0.0);

        double base = 1.0;
        double nextUp = MathUtils.nextAfter(base, 2.0);
        Assert.assertTrue(nextUp > base);
        double nextDown = MathUtils.nextAfter(base, 0.0);
        Assert.assertTrue(nextDown < base);

        double edgeMantissaMax = Double.longBitsToDouble(0x000fffffffffffffL);
        double steppedUp = MathUtils.nextAfter(edgeMantissaMax, 1.0);
        Assert.assertTrue(steppedUp > edgeMantissaMax);

        double edgePowerOfTwo = 2.0;
        double steppedDown = MathUtils.nextAfter(edgePowerOfTwo, 0.0);
        Assert.assertTrue(steppedDown < edgePowerOfTwo);
    }

    @Test
    public void testScalb() {
        Assert.assertEquals(0.0, MathUtils.scalb(0.0, 5), 0.0);
        Assert.assertTrue(Double.isNaN(MathUtils.scalb(Double.NaN, 5)));
        Assert.assertTrue(Double.isInfinite(MathUtils.scalb(Double.POSITIVE_INFINITY, 5)));
        Assert.assertEquals(8.0, MathUtils.scalb(1.0, 3), 1e-10);
        Assert.assertEquals(0.25, MathUtils.scalb(1.0, -2), 1e-10);
    }

    @Test
    public void testNormalizeAngle() {
        Assert.assertEquals(Math.PI / 2, MathUtils.normalizeAngle(Math.PI / 2, 0.0), 1e-10);
        Assert.assertEquals(Math.PI / 2, MathUtils.normalizeAngle(5 * Math.PI / 2, 0.0), 1e-10);
        Assert.assertEquals(Math.PI, MathUtils.normalizeAngle(Math.PI, Math.PI), 1e-10);
        Assert.assertEquals(0.0, MathUtils.normalizeAngle(2 * Math.PI, 0.0), 1e-10);
    }

    @Test
    public void testRoundDouble() {
        Assert.assertEquals(1.23, MathUtils.round(1.2345, 2), 1e-10);
        Assert.assertEquals(1.24, MathUtils.round(1.2355, 2), 1e-10);
        Assert.assertTrue(Double.isInfinite(MathUtils.round(Double.POSITIVE_INFINITY, 2)));
        Assert.assertTrue(Double.isNaN(MathUtils.round(Double.NaN, 2)));
    }

    @Test
    public void testRoundFloat() {
        Assert.assertEquals(1.23f, MathUtils.round(1.2345f, 2), 1e-5f);
        Assert.assertEquals(1.24f, MathUtils.round(1.2355f, 2), 1e-5f);
        Assert.assertEquals(1.24f, MathUtils.round(1.2355f, 2, BigDecimal.ROUND_HALF_UP), 1e-5f);
    }

    @Test
    public void testRoundRoundingModes() {
        Assert.assertEquals(2.0, MathUtils.round(1.2, 0, BigDecimal.ROUND_CEILING), 1e-10);
        Assert.assertEquals(-1.0, MathUtils.round(-1.2, 0, BigDecimal.ROUND_CEILING), 1e-10);
        Assert.assertEquals(1.0, MathUtils.round(1.8, 0, BigDecimal.ROUND_DOWN), 1e-10);
        Assert.assertEquals(1.0, MathUtils.round(1.8, 0, BigDecimal.ROUND_FLOOR), 1e-10);
        Assert.assertEquals(-2.0, MathUtils.round(-1.2, 0, BigDecimal.ROUND_FLOOR), 1e-10);
        Assert.assertEquals(1.0, MathUtils.round(1.5, 0, BigDecimal.ROUND_HALF_DOWN), 1e-10);
        Assert.assertEquals(2.0, MathUtils.round(1.51, 0, BigDecimal.ROUND_HALF_DOWN), 1e-10);
        Assert.assertEquals(2.0, MathUtils.round(2.5, 0, BigDecimal.ROUND_HALF_EVEN), 1e-10);
        Assert.assertEquals(2.0, MathUtils.round(1.5, 0, BigDecimal.ROUND_HALF_EVEN), 1e-10);
        Assert.assertEquals(2.0, MathUtils.round(1.6, 0, BigDecimal.ROUND_HALF_EVEN), 1e-10);
        Assert.assertEquals(1.0, MathUtils.round(1.4, 0, BigDecimal.ROUND_HALF_EVEN), 1e-10);
        Assert.assertEquals(2.0, MathUtils.round(1.5, 0, BigDecimal.ROUND_HALF_UP), 1e-10);
        Assert.assertEquals(1.0, MathUtils.round(1.4, 0, BigDecimal.ROUND_HALF_UP), 1e-10);
        Assert.assertEquals(2.0, MathUtils.round(1.1, 0, BigDecimal.ROUND_UP), 1e-10);
        Assert.assertEquals(1.0, MathUtils.round(1.0, 0, BigDecimal.ROUND_UNNECESSARY), 1e-10);
    }

    @Test(expected = ArithmeticException.class)
    public void testRoundUnnecessaryException() {
        MathUtils.round(1.5f, 0, BigDecimal.ROUND_UNNECESSARY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRoundInvalidMethod() {
        MathUtils.round(1.5f, 0, 9999);
    }
}
