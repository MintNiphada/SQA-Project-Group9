package org.apache.commons.math3.fraction;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.apache.commons.math3.TestUtils;
import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.ZeroException;
import org.junit.Assert;
import org.junit.Test;

public class BigFractionTest {

    private static final double EPSILON = 1e-15;

    @Test
    public void testConstants() {
        Assert.assertEquals(BigInteger.valueOf(2), BigFraction.TWO.getNumerator());
        Assert.assertEquals(BigInteger.ONE, BigFraction.TWO.getDenominator());

        Assert.assertEquals(BigInteger.ONE, BigFraction.ONE.getNumerator());
        Assert.assertEquals(BigInteger.ONE, BigFraction.ONE.getDenominator());

        Assert.assertEquals(BigInteger.ZERO, BigFraction.ZERO.getNumerator());
        Assert.assertEquals(BigInteger.ONE, BigFraction.ZERO.getDenominator());

        Assert.assertEquals(BigInteger.valueOf(-1), BigFraction.MINUS_ONE.getNumerator());
        Assert.assertEquals(BigInteger.ONE, BigFraction.MINUS_ONE.getDenominator());

        Assert.assertEquals(new BigFraction(4, 5), BigFraction.FOUR_FIFTHS);
        Assert.assertEquals(new BigFraction(1, 5), BigFraction.ONE_FIFTH);
        Assert.assertEquals(new BigFraction(1, 2), BigFraction.ONE_HALF);
        Assert.assertEquals(new BigFraction(1, 4), BigFraction.ONE_QUARTER);
        Assert.assertEquals(new BigFraction(1, 3), BigFraction.ONE_THIRD);
        Assert.assertEquals(new BigFraction(3, 5), BigFraction.THREE_FIFTHS);
        Assert.assertEquals(new BigFraction(3, 4), BigFraction.THREE_QUARTERS);
        Assert.assertEquals(new BigFraction(2, 5), BigFraction.TWO_FIFTHS);
        Assert.assertEquals(new BigFraction(2, 4), BigFraction.TWO_QUARTERS);
        Assert.assertEquals(new BigFraction(2, 3), BigFraction.TWO_THIRDS);
    }

    @Test
    public void testConstructorBigInteger() {
        BigFraction bf = new BigFraction(BigInteger.valueOf(10));
        Assert.assertEquals(BigInteger.valueOf(10), bf.getNumerator());
        Assert.assertEquals(BigInteger.ONE, bf.getDenominator());
    }

    @Test
    public void testConstructorBigIntegerBigInteger() {
        BigFraction bf1 = new BigFraction(BigInteger.valueOf(6), BigInteger.valueOf(8));
        Assert.assertEquals(BigInteger.valueOf(3), bf1.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(4), bf1.getDenominator());

        BigFraction bf2 = new BigFraction(BigInteger.valueOf(6), BigInteger.valueOf(-8));
        Assert.assertEquals(BigInteger.valueOf(-3), bf2.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(4), bf2.getDenominator());

        BigFraction bf3 = new BigFraction(BigInteger.valueOf(-6), BigInteger.valueOf(-8));
        Assert.assertEquals(BigInteger.valueOf(3), bf3.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(4), bf3.getDenominator());

        BigFraction bfZero = new BigFraction(BigInteger.ZERO, BigInteger.valueOf(5));
        Assert.assertEquals(BigInteger.ZERO, bfZero.getNumerator());
        Assert.assertEquals(BigInteger.ONE, bfZero.getDenominator());
    }

    @Test(expected = NullArgumentException.class)
    public void testConstructorNullNumerator() {
        new BigFraction(null, BigInteger.ONE);
    }

    @Test(expected = NullArgumentException.class)
    public void testConstructorNullDenominator() {
        new BigFraction(BigInteger.ONE, null);
    }

    @Test(expected = ZeroException.class)
    public void testConstructorZeroDenominator() {
        new BigFraction(BigInteger.ONE, BigInteger.ZERO);
    }

    @Test
    public void testConstructorInt() {
        BigFraction bf = new BigFraction(42);
        Assert.assertEquals(BigInteger.valueOf(42), bf.getNumerator());
        Assert.assertEquals(BigInteger.ONE, bf.getDenominator());
    }

    @Test
    public void testConstructorIntInt() {
        BigFraction bf = new BigFraction(6, -8);
        Assert.assertEquals(BigInteger.valueOf(-3), bf.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(4), bf.getDenominator());
    }

    @Test
    public void testConstructorLong() {
        BigFraction bf = new BigFraction(1234567890123L);
        Assert.assertEquals(BigInteger.valueOf(1234567890123L), bf.getNumerator());
        Assert.assertEquals(BigInteger.ONE, bf.getDenominator());
    }

    @Test
    public void testConstructorLongLong() {
        BigFraction bf = new BigFraction(1000000000000L, 2000000000000L);
        Assert.assertEquals(BigInteger.ONE, bf.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(2), bf.getDenominator());
    }

    @Test
    public void testConstructorDoubleExact() {
        BigFraction bf = new BigFraction(0.5);
        Assert.assertEquals(BigInteger.ONE, bf.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(2), bf.getDenominator());

        BigFraction bfNeg = new BigFraction(-0.75);
        Assert.assertEquals(BigInteger.valueOf(-3), bfNeg.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(4), bfNeg.getDenominator());

        BigFraction bfZero = new BigFraction(0.0);
        Assert.assertEquals(BigInteger.ZERO, bfZero.getNumerator());
        Assert.assertEquals(BigInteger.ONE, bfZero.getDenominator());

        BigFraction bfLarge = new BigFraction(4.0);
        Assert.assertEquals(BigInteger.valueOf(4), bfLarge.getNumerator());
        Assert.assertEquals(BigInteger.ONE, bfLarge.getDenominator());

        BigFraction bfSubnormal = new BigFraction(Double.MIN_VALUE);
        Assert.assertEquals(BigInteger.ONE, bfSubnormal.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(2).pow(1074), bfSubnormal.getDenominator());
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testConstructorDoubleNaN() {
        new BigFraction(Double.NaN);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testConstructorDoublePosInf() {
        new BigFraction(Double.POSITIVE_INFINITY);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testConstructorDoubleNegInf() {
        new BigFraction(Double.NEGATIVE_INFINITY);
    }

    @Test
    public void testConstructorDoubleEpsilonMaxIterations() {
        BigFraction bf = new BigFraction(1.0 / 3.0, 1e-10, 20);
        Assert.assertEquals(BigInteger.ONE, bf.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(3), bf.getDenominator());

        BigFraction bfInt = new BigFraction(5.0, 1e-5, 10);
        Assert.assertEquals(BigInteger.valueOf(5), bfInt.getNumerator());
        Assert.assertEquals(BigInteger.ONE, bfInt.getDenominator());
    }

    @Test(expected = FractionConversionException.class)
    public void testConstructorDoubleMaxIterationsExceeded() {
        new BigFraction(0.3333333333333, 1e-15, 2);
    }

    @Test(expected = FractionConversionException.class)
    public void testConstructorDoubleOverflow() {
        new BigFraction(1e20, 1e-5, 10);
    }

    @Test
    public void testConstructorDoubleMaxDenominator() {
        BigFraction bf = new BigFraction(0.3333333333333333, 10);
        Assert.assertEquals(BigInteger.ONE, bf.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(3), bf.getDenominator());
    }

    @Test
    public void testGetReducedFraction() {
        BigFraction bf = BigFraction.getReducedFraction(0, 5);
        Assert.assertSame(BigFraction.ZERO, bf);

        BigFraction bf2 = BigFraction.getReducedFraction(6, -8);
        Assert.assertEquals(BigInteger.valueOf(-3), bf2.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(4), bf2.getDenominator());
    }

    @Test
    public void testAbs() {
        BigFraction pos = new BigFraction(3, 4);
        BigFraction neg = new BigFraction(-3, 4);
        BigFraction zero = BigFraction.ZERO;

        Assert.assertSame(pos, pos.abs());
        Assert.assertEquals(pos, neg.abs());
        Assert.assertSame(zero, zero.abs());
    }

    @Test
    public void testAdd() {
        BigFraction bf = new BigFraction(1, 3);

        BigFraction resBigInt = bf.add(BigInteger.valueOf(2));
        Assert.assertEquals(new BigFraction(7, 3), resBigInt);

        BigFraction resInt = bf.add(2);
        Assert.assertEquals(new BigFraction(7, 3), resInt);

        BigFraction resLong = bf.add(2L);
        Assert.assertEquals(new BigFraction(7, 3), resLong);

        BigFraction sameDenom = bf.add(new BigFraction(4, 3));
        Assert.assertEquals(new BigFraction(5, 3), sameDenom);

        BigFraction diffDenom = bf.add(new BigFraction(1, 2));
        Assert.assertEquals(new BigFraction(5, 6), diffDenom);

        BigFraction withZero = bf.add(BigFraction.ZERO);
        Assert.assertSame(bf, withZero);
    }

    @Test(expected = NullArgumentException.class)
    public void testAddNullBigInteger() {
        BigFraction.ONE.add((BigInteger) null);
    }

    @Test(expected = NullArgumentException.class)
    public void testAddNullFraction() {
        BigFraction.ONE.add((BigFraction) null);
    }

    @Test
    public void testSubtract() {
        BigFraction bf = new BigFraction(5, 3);

        BigFraction resBigInt = bf.subtract(BigInteger.valueOf(1));
        Assert.assertEquals(new BigFraction(2, 3), resBigInt);

        BigFraction resInt = bf.subtract(1);
        Assert.assertEquals(new BigFraction(2, 3), resInt);

        BigFraction resLong = bf.subtract(1L);
        Assert.assertEquals(new BigFraction(2, 3), resLong);

        BigFraction sameDenom = bf.subtract(new BigFraction(2, 3));
        Assert.assertEquals(BigFraction.ONE, sameDenom);

        BigFraction diffDenom = bf.subtract(new BigFraction(1, 2));
        Assert.assertEquals(new BigFraction(7, 6), diffDenom);

        BigFraction withZero = bf.subtract(BigFraction.ZERO);
        Assert.assertSame(bf, withZero);
    }

    @Test(expected = NullArgumentException.class)
    public void testSubtractNullBigInteger() {
        BigFraction.ONE.subtract((BigInteger) null);
    }

    @Test(expected = NullArgumentException.class)
    public void testSubtractNullFraction() {
        BigFraction.ONE.subtract((BigFraction) null);
    }

    @Test
    public void testMultiply() {
        BigFraction bf = new BigFraction(2, 3);

        BigFraction resBigInt = bf.multiply(BigInteger.valueOf(3));
        Assert.assertEquals(BigFraction.TWO, resBigInt);

        BigFraction resInt = bf.multiply(3);
        Assert.assertEquals(BigFraction.TWO, resInt);

        BigFraction resLong = bf.multiply(3L);
        Assert.assertEquals(BigFraction.TWO, resLong);

        BigFraction resFrac = bf.multiply(new BigFraction(3, 4));
        Assert.assertEquals(BigFraction.ONE_HALF, resFrac);

        Assert.assertSame(BigFraction.ZERO, bf.multiply(BigFraction.ZERO));
        Assert.assertSame(BigFraction.ZERO, BigFraction.ZERO.multiply(bf));
    }

    @Test(expected = NullArgumentException.class)
    public void testMultiplyNullBigInteger() {
        BigFraction.ONE.multiply((BigInteger) null);
    }

    @Test(expected = NullArgumentException.class)
    public void testMultiplyNullFraction() {
        BigFraction.ONE.multiply((BigFraction) null);
    }

    @Test
    public void testDivide() {
        BigFraction bf = new BigFraction(2, 3);

        BigFraction resBigInt = bf.divide(BigInteger.valueOf(2));
        Assert.assertEquals(BigFraction.ONE_THIRD, resBigInt);

        BigFraction resInt = bf.divide(2);
        Assert.assertEquals(BigFraction.ONE_THIRD, resInt);

        BigFraction resLong = bf.divide(2L);
        Assert.assertEquals(BigFraction.ONE_THIRD, resLong);

        BigFraction resFrac = bf.divide(new BigFraction(4, 3));
        Assert.assertEquals(BigFraction.ONE_HALF, resFrac);
    }

    @Test(expected = NullArgumentException.class)
    public void testDivideNullBigInteger() {
        BigFraction.ONE.divide((BigInteger) null);
    }

    @Test(expected = MathArithmeticException.class)
    public void testDivideZeroBigInteger() {
        BigFraction.ONE.divide(BigInteger.ZERO);
    }

    @Test(expected = MathArithmeticException.class)
    public void testDivideZeroInt() {
        BigFraction.ONE.divide(0);
    }

    @Test(expected = MathArithmeticException.class)
    public void testDivideZeroLong() {
        BigFraction.ONE.divide(0L);
    }

    @Test(expected = NullArgumentException.class)
    public void testDivideNullFraction() {
        BigFraction.ONE.divide((BigFraction) null);
    }

    @Test(expected = MathArithmeticException.class)
    public void testDivideZeroFraction() {
        BigFraction.ONE.divide(BigFraction.ZERO);
    }

    @Test
    public void testNegate() {
        BigFraction bf = new BigFraction(3, 4);
        Assert.assertEquals(new BigFraction(-3, 4), bf.negate());
        Assert.assertEquals(bf, bf.negate().negate());
    }

    @Test
    public void testReciprocal() {
        BigFraction bf = new BigFraction(3, 4);
        Assert.assertEquals(new BigFraction(4, 3), bf.reciprocal());

        BigFraction negBf = new BigFraction(-3, 4);
        Assert.assertEquals(new BigFraction(-4, 3), negBf.reciprocal());
    }

    @Test(expected = ZeroException.class)
    public void testReciprocalZero() {
        BigFraction.ZERO.reciprocal();
    }

    @Test
    public void testPow() {
        BigFraction bf = new BigFraction(2, 3);

        Assert.assertEquals(new BigFraction(8, 27), bf.pow(3));
        Assert.assertEquals(new BigFraction(27, 8), bf.pow(-3));
        Assert.assertEquals(BigFraction.ONE, bf.pow(0));

        Assert.assertEquals(new BigFraction(8, 27), bf.pow(3L));
        Assert.assertEquals(new BigFraction(27, 8), bf.pow(-3L));
        Assert.assertEquals(BigFraction.ONE, bf.pow(0L));

        Assert.assertEquals(new BigFraction(8, 27), bf.pow(BigInteger.valueOf(3)));
        Assert.assertEquals(new BigFraction(27, 8), bf.pow(BigInteger.valueOf(-3)));
        Assert.assertEquals(BigFraction.ONE, bf.pow(BigInteger.ZERO));

        Assert.assertEquals(8.0 / 27.0, bf.pow(3.0), EPSILON);
    }

    @Test
    public void testBigDecimalValue() {
        BigFraction bf = new BigFraction(1, 2);
        Assert.assertEquals(new BigDecimal("0.5"), bf.bigDecimalValue());
        Assert.assertEquals(new BigDecimal("0.5"), bf.bigDecimalValue(BigDecimal.ROUND_HALF_UP));
        Assert.assertEquals(new BigDecimal("0.50"), bf.bigDecimalValue(2, BigDecimal.ROUND_HALF_UP));
    }

    @Test
    public void testPercentageValue() {
        BigFraction bf = new BigFraction(1, 4);
        Assert.assertEquals(25.0, bf.percentageValue(), EPSILON);
    }

    @Test
    public void testConversions() {
        BigFraction bf = new BigFraction(7, 2);
        Assert.assertEquals(3.5, bf.doubleValue(), EPSILON);
        Assert.assertEquals(3.5f, bf.floatValue(), 1e-6f);
        Assert.assertEquals(3, bf.intValue());
        Assert.assertEquals(3L, bf.longValue());

        BigInteger bigNum = BigInteger.ONE.shiftLeft(1100);
        BigInteger bigDen = BigInteger.ONE.shiftLeft(1050);
        BigFraction hugeBf = new BigFraction(bigNum, bigDen);
        Assert.assertEquals(Math.pow(2.0, 50.0), hugeBf.doubleValue(), 1e-3);
        Assert.assertEquals((float) Math.pow(2.0, 50.0), hugeBf.floatValue(), 1e-3f);
    }

    @Test
    public void testAccessors() {
        BigFraction bf = new BigFraction(5, 7);
        Assert.assertEquals(BigInteger.valueOf(5), bf.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(7), bf.getDenominator());
        Assert.assertEquals(5, bf.getNumeratorAsInt());
        Assert.assertEquals(7, bf.getDenominatorAsInt());
        Assert.assertEquals(5L, bf.getNumeratorAsLong());
        Assert.assertEquals(7L, bf.getDenominatorAsLong());
    }

    @Test
    public void testCompareTo() {
        BigFraction f1 = new BigFraction(1, 2);
        BigFraction f2 = new BigFraction(2, 3);
        BigFraction f3 = new BigFraction(2, 4);

        Assert.assertTrue(f1.compareTo(f2) < 0);
        Assert.assertTrue(f2.compareTo(f1) > 0);
        Assert.assertEquals(0, f1.compareTo(f3));
    }

    @Test
    public void testEqualsAndHashCode() {
        BigFraction f1 = new BigFraction(1, 2);
        BigFraction f2 = new BigFraction(2, 4);
        BigFraction f3 = new BigFraction(1, 3);

        Assert.assertTrue(f1.equals(f1));
        Assert.assertTrue(f1.equals(f2));
        Assert.assertFalse(f1.equals(f3));
        Assert.assertFalse(f1.equals(null));
        Assert.assertFalse(f1.equals("Not a fraction"));

        Assert.assertEquals(f1.hashCode(), f2.hashCode());
    }

    @Test
    public void testToString() {
        Assert.assertEquals("0", BigFraction.ZERO.toString());
        Assert.assertEquals("5", new BigFraction(5).toString());
        Assert.assertEquals("2 / 3", new BigFraction(2, 3).toString());
        Assert.assertEquals("-2 / 3", new BigFraction(-2, 3).toString());
    }

    @Test
    public void testReduce() {
        BigFraction bf = new BigFraction(6, 8);
        BigFraction reduced = bf.reduce();
        Assert.assertEquals(new BigFraction(3, 4), reduced);
    }

    @Test
    public void testGetField() {
        BigFraction bf = new BigFraction(1, 2);
        Assert.assertSame(BigFractionField.getInstance(), bf.getField());
    }

    @Test
    public void testSerialization() {
        BigFraction bf = new BigFraction(12345, 67890);
        BigFraction deserialized = (BigFraction) TestUtils.serializeAndRecover(bf);
        Assert.assertEquals(bf, deserialized);
    }
}
