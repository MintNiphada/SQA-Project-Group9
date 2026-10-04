package org.apache.commons.math.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.NonMonotonousSequenceException;
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
    public void testSubAndCheckInt() {
        Assert.assertEquals(-1, MathUtils.subAndCheck(2, 3));
        Assert.assertEquals(1, MathUtils.subAndCheck(-2, -3));
        Assert.assertEquals(Integer.MAX_VALUE, MathUtils.subAndCheck(Integer.MAX_VALUE - 1, -1));
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
        Assert.assertEquals(1L, MathUtils.subAndCheck(-2L, -3L));
        Assert.assertEquals(Long.MIN_VALUE, MathUtils.subAndCheck(Long.MIN_VALUE + 1L, 1L));
        Assert.assertEquals(0L, MathUtils.subAndCheck(Long.MIN_VALUE, Long.MIN_VALUE));
    }

    @Test(expected = ArithmeticException.class)
    public void testSubAndCheckLongOverflowMin() {
        MathUtils.subAndCheck(0L, Long.MIN_VALUE);
    }

    @Test(expected = ArithmeticException.class)
    public void testSubAndCheckLongOverflow() {
        MathUtils.subAndCheck(Long.MIN_VALUE, 1L);
    }

    @Test
    public void testMulAndCheckInt() {
        Assert.assertEquals(6, MathUtils.mulAndCheck(2, 3));
        Assert.assertEquals(-6, MathUtils.mulAndCheck(-2, 3));
        Assert.assertEquals(0, MathUtils.mulAndCheck(0, 5));
    }

    @Test(expected = ArithmeticException.class)
    public void testMulAndCheckIntOverflow() {
        MathUtils.mulAndCheck(Integer.MAX_VALUE, 2);
    }

    @Test(expected = ArithmeticException.class)
    public void testMulAndCheckIntUnderflow() {
        MathUtils.mulAndCheck(Integer.MIN_VALUE, 2);
    }

    @Test
    public void testMulAndCheckLong() {
        Assert.assertEquals(6L, MathUtils.mulAndCheck(2L, 3L));
        Assert.assertEquals(6L, MathUtils.mulAndCheck(3L, 2L));
        Assert.assertEquals(-6L, MathUtils.mulAndCheck(-2L, 3L));
        Assert.assertEquals(-6L, MathUtils.mulAndCheck(3L, -2L));
        Assert.assertEquals(6L, MathUtils.mulAndCheck(-2L, -3L));
        Assert.assertEquals(0L, MathUtils.mulAndCheck(0L, 5L));
        Assert.assertEquals(0L, MathUtils.mulAndCheck(5L, 0L));
        Assert.assertEquals(0L, MathUtils.mulAndCheck(0L, -5L));
        Assert.assertEquals(0L, MathUtils.mulAndCheck(-5L, 0L));
        Assert.assertEquals(0L, MathUtils.mulAndCheck(0L, 0L));
        Assert.assertEquals(Long.MIN_VALUE, MathUtils.mulAndCheck(Long.MIN_VALUE, 1L));
        Assert.assertEquals(Long.MAX_VALUE, MathUtils.mulAndCheck(Long.MAX_VALUE, 1L));
    }

    @Test(expected = ArithmeticException.class)
    public void testMulAndCheckLongOverflowBothPos() {
        MathUtils.mulAndCheck(Long.MAX_VALUE, 2L);
    }

    @Test(expected = ArithmeticException.class)
    public void testMulAndCheckLongOverflowBothNeg() {
        MathUtils.mulAndCheck(Long.MIN_VALUE, -2L);
    }

    @Test(expected = ArithmeticException.class)
    public void testMulAndCheckLongOverflowPosNeg() {
        MathUtils.mulAndCheck(Long.MAX_VALUE, -2L);
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
        Assert.assertEquals(72193255998188150L, MathUtils.binomialCoefficient(65, 30));
        Assert.assertEquals(1166803110L, MathUtils.binomialCoefficient(67, 6));
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
        Assert.assertEquals(1.0, MathUtils.binomialCoefficientDouble(0, 0), 1e-10);
        Assert.assertEquals(1.0, MathUtils.binomialCoefficientDouble(5, 0), 1e-10);
        Assert.assertEquals(1.0, MathUtils.binomialCoefficientDouble(5, 5), 1e-10);
        Assert.assertEquals(5.0, MathUtils.binomialCoefficientDouble(5, 1), 1e-10);
        Assert.assertEquals(5.0, MathUtils.binomialCoefficientDouble(5, 4), 1e-10);
        Assert.assertEquals(10.0, MathUtils.binomialCoefficientDouble(5, 2), 1e-10);
        Assert.assertEquals(10.0, MathUtils.binomialCoefficientDouble(5, 3), 1e-10);
        Assert.assertEquals(MathUtils.binomialCoefficient(60, 10), MathUtils.binomialCoefficientDouble(60, 10), 1e-10);
        Assert.assertTrue(MathUtils.binomialCoefficientDouble(100, 50) > 0);
    }

    @Test
    public void testBinomialCoefficientLog() {
        Assert.assertEquals(0.0, MathUtils.binomialCoefficientLog(0, 0), 1e-10);
        Assert.assertEquals(0.0, MathUtils.binomialCoefficientLog(5, 0), 1e-10);
        Assert.assertEquals(0.0, MathUtils.binomialCoefficientLog(5, 5), 1e-10);
        Assert.assertEquals(FastMath.log(5), MathUtils.binomialCoefficientLog(5, 1), 1e-10);
        Assert.assertEquals(FastMath.log(5), MathUtils.binomialCoefficientLog(5, 4), 1e-10);
        Assert.assertEquals(FastMath.log(10), MathUtils.binomialCoefficientLog(5, 2), 1e-10);
        Assert.assertEquals(FastMath.log(MathUtils.binomialCoefficientDouble(100, 10)), MathUtils.binomialCoefficientLog(100, 10), 1e-5);
        Assert.assertTrue(MathUtils.binomialCoefficientLog(1100, 500) > 0);
        Assert.assertTrue(MathUtils.binomialCoefficientLog(1100, 600) > 0);
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
        Assert.assertTrue(MathUtils.factorialDouble(25) > 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactorialDoubleNegative() {
        MathUtils.factorialDouble(-1);
    }

    @Test
    public void testFactorialLog() {
        Assert.assertEquals(0.0, MathUtils.factorialLog(0), 1e-10);
        Assert.assertEquals(0.0, MathUtils.factorialLog(1), 1e-10);
        Assert.assertEquals(FastMath.log(120.0), MathUtils.factorialLog(5), 1e-10);
        Assert.assertTrue(MathUtils.factorialLog(25) > 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactorialLogNegative() {
        MathUtils.factorialLog(-1);
    }

    @Test
    public void testCoshSinh() {
        Assert.assertEquals(1.0, MathUtils.cosh(0.0), 1e-10);
        Assert.assertEquals(0.0, MathUtils.sinh(0.0), 1e-10);
        Assert.assertEquals(1.5430806348152437, MathUtils.cosh(1.0), 1e-10);
        Assert.assertEquals(1.1752011936438014, MathUtils.sinh(1.0), 1e-10);
    }

    @Test
    public void testCompareTo() {
        Assert.assertEquals(0, MathUtils.compareTo(1.0, 1.0, 0.0));
        Assert.assertEquals(0, MathUtils.compareTo(1.0, 1.05, 0.1));
        Assert.assertEquals(-1, MathUtils.compareTo(1.0, 2.0, 0.1));
        Assert.assertEquals(1, MathUtils.compareTo(2.0, 1.0, 0.1));
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
    public void testEqualsIncludingNaN() {
        Assert.assertTrue(MathUtils.equalsIncludingNaN(Double.NaN, Double.NaN));
        Assert.assertTrue(MathUtils.equalsIncludingNaN(1.0, 1.0));
        Assert.assertFalse(MathUtils.equalsIncludingNaN(Double.NaN, 1.0));
        Assert.assertFalse(MathUtils.equalsIncludingNaN(1.0, Double.NaN));
    }

    @Test
    public void testEqualsDoubleEps() {
        Assert.assertTrue(MathUtils.equals(1.0, 1.05, 0.1));
        Assert.assertFalse(MathUtils.equals(1.0, 1.2, 0.1));
        Assert.assertFalse(MathUtils.equals(Double.NaN, 1.0, 0.1));
    }

    @Test
    public void testEqualsIncludingNaNEps() {
        Assert.assertTrue(MathUtils.equalsIncludingNaN(Double.NaN, Double.NaN, 0.1));
        Assert.assertTrue(MathUtils.equalsIncludingNaN(1.0, 1.05, 0.1));
        Assert.assertFalse(MathUtils.equalsIncludingNaN(1.0, 1.2, 0.1));
    }

    @Test
    public void testEqualsDoubleMaxUlps() {
        Assert.assertTrue(MathUtils.equals(1.0, 1.0, 1));
        Assert.assertTrue(MathUtils.equals(-1.0, -1.0, 1));
        Assert.assertTrue(MathUtils.equals(0.0, -0.0, 1));
        Assert.assertFalse(MathUtils.equals(Double.NaN, 1.0, 1));
        Assert.assertFalse(MathUtils.equals(1.0, Double.NaN, 1));
        Assert.assertFalse(MathUtils.equals(Double.NaN, Double.NaN, 1));
        Assert.assertTrue(MathUtils.equalsIncludingNaN(Double.NaN, Double.NaN, 1));
    }

    @Test
    public void testEqualsArray() {
        Assert.assertTrue(MathUtils.equals((double[]) null, (double[]) null));
        Assert.assertFalse(MathUtils.equals(new double[]{1.0}, null));
        Assert.assertFalse(MathUtils.equals(null, new double[]{1.0}));
        Assert.assertFalse(MathUtils.equals(new double[]{1.0}, new double[]{1.0, 2.0}));
        Assert.assertTrue(MathUtils.equals(new double[]{1.0, 2.0}, new double[]{1.0, 2.0}));
        Assert.assertFalse(MathUtils.equals(new double[]{1.0, 2.0}, new double[]{1.0, 3.0}));
    }

    @Test
    public void testEqualsIncludingNaNArray() {
        Assert.assertTrue(MathUtils.equalsIncludingNaN((double[]) null, (double[]) null));
        Assert.assertFalse(MathUtils.equalsIncludingNaN(new double[]{1.0}, null));
        Assert.assertFalse(MathUtils.equalsIncludingNaN(null, new double[]{1.0}));
        Assert.assertFalse(MathUtils.equalsIncludingNaN(new double[]{1.0}, new double[]{1.0, 2.0}));
        Assert.assertTrue(MathUtils.equalsIncludingNaN(new double[]{Double.NaN, 2.0}, new double[]{Double.NaN, 2.0}));
        Assert.assertFalse(MathUtils.equalsIncludingNaN(new double[]{Double.NaN, 2.0}, new double[]{1.0, 2.0}));
    }

    @Test
    public void testGcdInt() {
        Assert.assertEquals(0, MathUtils.gcd(0, 0));
        Assert.assertEquals(5, MathUtils.gcd(0, 5));
        Assert.assertEquals(5, MathUtils.gcd(5, 0));
        Assert.assertEquals(5, MathUtils.gcd(0, -5));
        Assert.assertEquals(5, MathUtils.gcd(-5, 0));
        Assert.assertEquals(6, MathUtils.gcd(12, 18));
        Assert.assertEquals(6, MathUtils.gcd(-12, 18));
        Assert.assertEquals(6, MathUtils.gcd(12, -18));
        Assert.assertEquals(6, MathUtils.gcd(-12, -18));
        Assert.assertEquals(1, MathUtils.gcd(13, 7));
        Assert.assertEquals(4, MathUtils.gcd(8, 12));
    }

    @Test(expected = ArithmeticException.class)
    public void testGcdIntOverflow1() {
        MathUtils.gcd(Integer.MIN_VALUE, 0);
    }

    @Test(expected = ArithmeticException.class)
    public void testGcdIntOverflow2() {
        MathUtils.gcd(0, Integer.MIN_VALUE);
    }

    @Test(expected = ArithmeticException.class)
    public void testGcdIntOverflowBoth() {
        MathUtils.gcd(Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    @Test
    public void testGcdLong() {
        Assert.assertEquals(0L, MathUtils.gcd(0L, 0L));
        Assert.assertEquals(5L, MathUtils.gcd(0L, 5L));
        Assert.assertEquals(5L, MathUtils.gcd(5L, 0L));
        Assert.assertEquals(5L, MathUtils.gcd(0L, -5L));
        Assert.assertEquals(5L, MathUtils.gcd(-5L, 0L));
        Assert.assertEquals(6L, MathUtils.gcd(12L, 18L));
        Assert.assertEquals(6L, MathUtils.gcd(-12L, 18L));
        Assert.assertEquals(6L, MathUtils.gcd(12L, -18L));
        Assert.assertEquals(6L, MathUtils.gcd(-12L, -18L));
        Assert.assertEquals(1L, MathUtils.gcd(13L, 7L));
        Assert.assertEquals(4L, MathUtils.gcd(8L, 12L));
    }

    @Test(expected = ArithmeticException.class)
    public void testGcdLongOverflow1() {
        MathUtils.gcd(Long.MIN_VALUE, 0L);
    }

    @Test(expected = ArithmeticException.class)
    public void testGcdLongOverflow2() {
        MathUtils.gcd(0L, Long.MIN_VALUE);
    }

    @Test(expected = ArithmeticException.class)
    public void testGcdLongOverflowBoth() {
        MathUtils.gcd(Long.MIN_VALUE, Long.MIN_VALUE);
    }

    @Test
    public void testLcmInt() {
        Assert.assertEquals(0, MathUtils.lcm(0, 5));
        Assert.assertEquals(0, MathUtils.lcm(5, 0));
        Assert.assertEquals(36, MathUtils.lcm(12, 18));
        Assert.assertEquals(36, MathUtils.lcm(-12, 18));
        Assert.assertEquals(36, MathUtils.lcm(12, -18));
        Assert.assertEquals(36, MathUtils.lcm(-12, -18));
    }

    @Test(expected = ArithmeticException.class)
    public void testLcmIntOverflow() {
        MathUtils.lcm(Integer.MAX_VALUE, Integer.MAX_VALUE - 1);
    }

    @Test
    public void testLcmLong() {
        Assert.assertEquals(0L, MathUtils.lcm(0L, 5L));
        Assert.assertEquals(0L, MathUtils.lcm(5L, 0L));
        Assert.assertEquals(36L, MathUtils.lcm(12L, 18L));
        Assert.assertEquals(36L, MathUtils.lcm(-12L, 18L));
        Assert.assertEquals(36L, MathUtils.lcm(12L, -18L));
        Assert.assertEquals(36L, MathUtils.lcm(-12L, -18L));
    }

    @Test(expected = ArithmeticException.class)
    public void testLcmLongOverflow() {
        MathUtils.lcm(Long.MAX_VALUE, Long.MAX_VALUE - 1L);
    }

    @Test
    public void testHash() {
        Assert.assertEquals(new Double(1.5).hashCode(), MathUtils.hash(1.5));
        double[] arr = new double[]{1.0, 2.0};
        Assert.assertEquals(java.util.Arrays.hashCode(arr), MathUtils.hash(arr));
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

        Assert.assertEquals(1.0f, MathUtils.indicator(0.0f), 1e-6f);
        Assert.assertEquals(1.0f, MathUtils.indicator(5.0f), 1e-6f);
        Assert.assertEquals(-1.0f, MathUtils.indicator(-5.0f), 1e-6f);
        Assert.assertTrue(Float.isNaN(MathUtils.indicator(Float.NaN)));

        Assert.assertEquals(1.0, MathUtils.indicator(0.0), 1e-10);
        Assert.assertEquals(1.0, MathUtils.indicator(5.0), 1e-10);
        Assert.assertEquals(-1.0, MathUtils.indicator(-5.0), 1e-10);
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

        Assert.assertEquals(0.0f, MathUtils.sign(0.0f), 1e-6f);
        Assert.assertEquals(1.0f, MathUtils.sign(5.0f), 1e-6f);
        Assert.assertEquals(-1.0f, MathUtils.sign(-5.0f), 1e-6f);
        Assert.assertTrue(Float.isNaN(MathUtils.sign(Float.NaN)));

        Assert.assertEquals(0.0, MathUtils.sign(0.0), 1e-10);
        Assert.assertEquals(1.0, MathUtils.sign(5.0), 1e-10);
        Assert.assertEquals(-1.0, MathUtils.sign(-5.0), 1e-10);
        Assert.assertTrue(Double.isNaN(MathUtils.sign(Double.NaN)));
    }

    @Test
    public void testLog() {
        Assert.assertEquals(2.0, MathUtils.log(2.0, 4.0), 1e-10);
        Assert.assertEquals(3.0, MathUtils.log(10.0, 1000.0), 1e-10);
    }

    @Test
    public void testScalb() {
        Assert.assertEquals(0.0, MathUtils.scalb(0.0, 5), 1e-10);
        Assert.assertTrue(Double.isNaN(MathUtils.scalb(Double.NaN, 5)));
        Assert.assertTrue(Double.isInfinite(MathUtils.scalb(Double.POSITIVE_INFINITY, 5)));
        Assert.assertEquals(8.0, MathUtils.scalb(2.0, 2), 1e-10);
        Assert.assertEquals(0.5, MathUtils.scalb(2.0, -2), 1e-10);
    }

    @Test
    public void testNormalizeAngle() {
        Assert.assertEquals(0.0, MathUtils.normalizeAngle(0.0, 0.0), 1e-10);
        Assert.assertEquals(0.0, MathUtils.normalizeAngle(MathUtils.TWO_PI, 0.0), 1e-10);
        Assert.assertEquals(FastMath.PI, MathUtils.normalizeAngle(3 * FastMath.PI, FastMath.PI), 1e-10);
    }

    @Test
    public void testNormalizeArray() {
        double[] values = new double[]{1.0, 2.0, 3.0, Double.NaN};
        double[] result = MathUtils.normalizeArray(values, 12.0);
        Assert.assertEquals(2.0, result[0], 1e-10);
        Assert.assertEquals(4.0, result[1], 1e-10);
        Assert.assertEquals(6.0, result[2], 1e-10);
        Assert.assertTrue(Double.isNaN(result[3]));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNormalizeArrayInfiniteSum() {
        MathUtils.normalizeArray(new double[]{1.0, 2.0}, Double.POSITIVE_INFINITY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNormalizeArrayNaNSum() {
        MathUtils.normalizeArray(new double[]{1.0, 2.0}, Double.NaN);
    }

    @Test(expected = ArithmeticException.class)
    public void testNormalizeArrayInfiniteElement() {
        MathUtils.normalizeArray(new double[]{1.0, Double.POSITIVE_INFINITY}, 5.0);
    }

    @Test(expected = ArithmeticException.class)
    public void testNormalizeArrayZeroSum() {
        MathUtils.normalizeArray(new double[]{1.0, -1.0}, 5.0);
    }

    @Test
    public void testRoundDouble() {
        Assert.assertEquals(1.23, MathUtils.round(1.234, 2), 1e-10);
        Assert.assertEquals(1.24, MathUtils.round(1.235, 2), 1e-10);
        Assert.assertEquals(1.24, MathUtils.round(1.234, 2, BigDecimal.ROUND_UP), 1e-10);
        Assert.assertTrue(Double.isInfinite(MathUtils.round(Double.POSITIVE_INFINITY, 2)));
        Assert.assertTrue(Double.isNaN(MathUtils.round(Double.NaN, 2)));
    }

    @Test
    public void testRoundFloat() {
        Assert.assertEquals(1.23f, MathUtils.round(1.234f, 2), 1e-5f);
        Assert.assertEquals(1.24f, MathUtils.round(1.235f, 2), 1e-5f);
        Assert.assertEquals(1.24f, MathUtils.round(1.234f, 2, BigDecimal.ROUND_UP), 1e-5f);
        Assert.assertEquals(1.23f, MathUtils.round(1.239f, 2, BigDecimal.ROUND_DOWN), 1e-5f);
        Assert.assertEquals(1.24f, MathUtils.round(1.234f, 2, BigDecimal.ROUND_CEILING), 1e-5f);
        Assert.assertEquals(-1.23f, MathUtils.round(-1.234f, 2, BigDecimal.ROUND_CEILING), 1e-5f);
        Assert.assertEquals(1.23f, MathUtils.round(1.239f, 2, BigDecimal.ROUND_FLOOR), 1e-5f);
        Assert.assertEquals(-1.24f, MathUtils.round(-1.234f, 2, BigDecimal.ROUND_FLOOR), 1e-5f);
        Assert.assertEquals(1.23f, MathUtils.round(1.235f, 2, BigDecimal.ROUND_HALF_DOWN), 1e-5f);
        Assert.assertEquals(1.24f, MathUtils.round(1.236f, 2, BigDecimal.ROUND_HALF_DOWN), 1e-5f);
        Assert.assertEquals(1.24f, MathUtils.round(1.235f, 2, BigDecimal.ROUND_HALF_EVEN), 1e-5f);
        Assert.assertEquals(1.24f, MathUtils.round(1.245f, 2, BigDecimal.ROUND_HALF_EVEN), 1e-5f);
        Assert.assertEquals(1.24f, MathUtils.round(1.244f, 2, BigDecimal.ROUND_HALF_EVEN), 1e-5f);
        Assert.assertEquals(1.25f, MathUtils.round(1.246f, 2, BigDecimal.ROUND_HALF_EVEN), 1e-5f);
        Assert.assertEquals(1.23f, MathUtils.round(1.230f, 2, BigDecimal.ROUND_UNNECESSARY), 1e-5f);
    }

    @Test(expected = ArithmeticException.class)
    public void testRoundFloatUnnecessaryException() {
        MathUtils.round(1.234f, 2, BigDecimal.ROUND_UNNECESSARY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRoundFloatInvalidMethod() {
        MathUtils.round(1.234f, 2, 999);
    }

    @Test
    public void testPow() {
        Assert.assertEquals(8, MathUtils.pow(2, 3));
        Assert.assertEquals(1, MathUtils.pow(2, 0));
        Assert.assertEquals(8, MathUtils.pow(2, 3L));
        Assert.assertEquals(8L, MathUtils.pow(2L, 3));
        Assert.assertEquals(8L, MathUtils.pow(2L, 3L));

        BigInteger b2 = BigInteger.valueOf(2);
        Assert.assertEquals(BigInteger.valueOf(8), MathUtils.pow(b2, 3));
        Assert.assertEquals(BigInteger.valueOf(8), MathUtils.pow(b2, 3L));
        Assert.assertEquals(BigInteger.valueOf(8), MathUtils.pow(b2, BigInteger.valueOf(3)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPowIntIntNegative() {
        MathUtils.pow(2, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPowIntLongNegative() {
        MathUtils.pow(2, -1L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPowLongIntNegative() {
        MathUtils.pow(2L, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPowLongLongNegative() {
        MathUtils.pow(2L, -1L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPowBigIntegerIntNegative() {
        MathUtils.pow(BigInteger.valueOf(2), -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPowBigIntegerLongNegative() {
        MathUtils.pow(BigInteger.valueOf(2), -1L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPowBigIntegerBigIntegerNegative() {
        MathUtils.pow(BigInteger.valueOf(2), BigInteger.valueOf(-1));
    }

    @Test
    public void testDistance() {
        double[] p1 = new double[]{1.0, 2.0};
        double[] p2 = new double[]{4.0, 6.0};
        Assert.assertEquals(7.0, MathUtils.distance1(p1, p2), 1e-10);
        Assert.assertEquals(5.0, MathUtils.distance(p1, p2), 1e-10);
        Assert.assertEquals(4.0, MathUtils.distanceInf(p1, p2), 1e-10);

        int[] ip1 = new int[]{1, 2};
        int[] ip2 = new int[]{4, 6};
        Assert.assertEquals(7, MathUtils.distance1(ip1, ip2));
        Assert.assertEquals(5.0, MathUtils.distance(ip1, ip2), 1e-10);
        Assert.assertEquals(4, MathUtils.distanceInf(ip1, ip2));
    }

    @Test
    public void testCheckOrder() {
        MathUtils.checkOrder(new double[]{1.0, 2.0, 3.0});
        MathUtils.checkOrder(new double[]{1.0, 2.0, 2.0}, MathUtils.OrderDirection.INCREASING, false);
        MathUtils.checkOrder(new double[]{3.0, 2.0, 1.0}, MathUtils.OrderDirection.DECREASING, true);
        MathUtils.checkOrder(new double[]{3.0, 2.0, 2.0}, MathUtils.OrderDirection.DECREASING, false);
    }

    @Test(expected = NonMonotonousSequenceException.class)
    public void testCheckOrderStrictIncreasingFail() {
        MathUtils.checkOrder(new double[]{1.0, 2.0, 2.0});
    }

    @Test(expected = NonMonotonousSequenceException.class)
    public void testCheckOrderIncreasingFail() {
        MathUtils.checkOrder(new double[]{1.0, 3.0, 2.0}, MathUtils.OrderDirection.INCREASING, false);
    }

    @Test(expected = NonMonotonousSequenceException.class)
    public void testCheckOrderStrictDecreasingFail() {
        MathUtils.checkOrder(new double[]{3.0, 2.0, 2.0}, MathUtils.OrderDirection.DECREASING, true);
    }

    @Test(expected = NonMonotonousSequenceException.class)
    public void testCheckOrderDecreasingFail() {
        MathUtils.checkOrder(new double[]{3.0, 1.0, 2.0}, MathUtils.OrderDirection.DECREASING, false);
    }

    @Test
    public void testSafeNorm() {
        Assert.assertEquals(0.0, MathUtils.safeNorm(new double[]{0.0, 0.0}), 1e-10);
        Assert.assertEquals(5.0, MathUtils.safeNorm(new double[]{3.0, 4.0}), 1e-10);
        Assert.assertEquals(5.0e-25, MathUtils.safeNorm(new double[]{3.0e-25, 4.0e-25}), 1e-35);
        Assert.assertEquals(5.0e25, MathUtils.safeNorm(new double[]{3.0e25, 4.0e25}), 1e15);
        Assert.assertTrue(MathUtils.safeNorm(new double[]{3.0e25, 4.0, 5.0e-25}) > 0);
        Assert.assertTrue(MathUtils.safeNorm(new double[]{3.0e-25, 4.0, 0.0}) > 0);
        Assert.assertTrue(MathUtils.safeNorm(new double[]{1.0e-25, 1.0e-24, 1.0}) > 0);
        Assert.assertTrue(MathUtils.safeNorm(new double[]{1.0e25, 2.0e25}) > 0);
        Assert.assertTrue(MathUtils.safeNorm(new double[]{1.0e-26, 2.0e-25}) > 0);
        Assert.assertTrue(MathUtils.safeNorm(new double[]{1.0e-5, 1.0e-25}) > 0);
        Assert.assertTrue(MathUtils.safeNorm(new double[]{1.0e-25, 1.0e-5}) > 0);
    }
}
