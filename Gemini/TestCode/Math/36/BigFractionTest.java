package org.apache.commons.math.fraction;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.apache.commons.math.TestUtils;
import org.apache.commons.math.exception.MathIllegalArgumentException;
import org.apache.commons.math.exception.NullArgumentException;
import org.apache.commons.math.exception.ZeroException;
import org.junit.Assert;
import org.junit.Test;

public class BigFractionTest {

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
        Assert.assertEquals(BigInteger.valueOf(4), BigFraction.FOUR_FIFTHS.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(5), BigFraction.FOUR_FIFTHS.getDenominator());
        Assert.assertEquals(BigInteger.valueOf(1), BigFraction.ONE_FIFTH.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(5), BigFraction.ONE_FIFTH.getDenominator());
        Assert.assertEquals(BigInteger.valueOf(1), BigFraction.ONE_HALF.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(2), BigFraction.ONE_HALF.getDenominator());
        Assert.assertEquals(BigInteger.valueOf(1), BigFraction.ONE_QUARTER.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(4), BigFraction.ONE_QUARTER.getDenominator());
        Assert.assertEquals(BigInteger.valueOf(1), BigFraction.ONE_THIRD.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(3), BigFraction.ONE_THIRD.getDenominator());
        Assert.assertEquals(BigInteger.valueOf(3), BigFraction.THREE_FIFTHS.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(5), BigFraction.THREE_FIFTHS.getDenominator());
        Assert.assertEquals(BigInteger.valueOf(3), BigFraction.THREE_QUARTERS.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(4), BigFraction.THREE_QUARTERS.getDenominator());
        Assert.assertEquals(BigInteger.valueOf(2), BigFraction.TWO_FIFTHS.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(5), BigFraction.TWO_FIFTHS.getDenominator());
        Assert.assertEquals(BigInteger.valueOf(1), BigFraction.TWO_QUARTERS.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(2), BigFraction.TWO_QUARTERS.getDenominator());
        Assert.assertEquals(BigInteger.valueOf(2), BigFraction.TWO_THIRDS.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(3), BigFraction.TWO_THIRDS.getDenominator());
    }

    @Test
    public void testConstructorBigInteger() {
        BigFraction f = new BigFraction(BigInteger.valueOf(5));
        Assert.assertEquals(BigInteger.valueOf(5), f.getNumerator());
        Assert.assertEquals(BigInteger.ONE, f.getDenominator());
    }

    @Test
    public void testConstructorBigIntegerBigInteger() {
        BigFraction f1 = new BigFraction(BigInteger.valueOf(6), BigInteger.valueOf(8));
        Assert.assertEquals(BigInteger.valueOf(3), f1.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(4), f1.getDenominator());

        BigFraction f2 = new BigFraction(BigInteger.valueOf(3), BigInteger.valueOf(-4));
        Assert.assertEquals(BigInteger.valueOf(-3), f2.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(4), f2.getDenominator());

        BigFraction f3 = new BigFraction(BigInteger.valueOf(0), BigInteger.valueOf(5));
        Assert.assertEquals(BigInteger.ZERO, f3.getNumerator());
        Assert.assertEquals(BigInteger.ONE, f3.getDenominator());

        BigFraction f4 = new BigFraction(BigInteger.valueOf(-3), BigInteger.valueOf(-4));
        Assert.assertEquals(BigInteger.valueOf(3), f4.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(4), f4.getDenominator());
    }

    @Test(expected = NullArgumentException.class)
    public void testConstructorBigIntegerNullNumerator() {
        new BigFraction(null, BigInteger.ONE);
    }

    @Test(expected = NullArgumentException.class)
    public void testConstructorBigIntegerNullDenominator() {
        new BigFraction(BigInteger.ONE, null);
    }

    @Test(expected = ZeroException.class)
    public void testConstructorBigIntegerZeroDenominator() {
        new BigFraction(BigInteger.ONE, BigInteger.ZERO);
    }

    @Test
    public void testConstructorDouble() {
        BigFraction f1 = new BigFraction(0.5);
        Assert.assertEquals(BigInteger.ONE, f1.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(2), f1.getDenominator());

        BigFraction f2 = new BigFraction(-0.5);
        Assert.assertEquals(BigInteger.valueOf(-1), f2.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(2), f2.getDenominator());

        BigFraction f3 = new BigFraction(0.0);
        Assert.assertEquals(BigInteger.ZERO, f3.getNumerator());
        Assert.assertEquals(BigInteger.ONE, f3.getDenominator());

        BigFraction f4 = new BigFraction(4.0);
        Assert.assertEquals(BigInteger.valueOf(4), f4.getNumerator());
        Assert.assertEquals(BigInteger.ONE, f4.getDenominator());

        BigFraction f5 = new BigFraction(Double.MIN_VALUE);
        Assert.assertEquals(BigInteger.ONE, f5.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(2).pow(1074), f5.getDenominator());
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testConstructorDoubleNaN() {
        new BigFraction(Double.NaN);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testConstructorDoublePositiveInfinity() {
        new BigFraction(Double.POSITIVE_INFINITY);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testConstructorDoubleNegativeInfinity() {
        new BigFraction(Double.NEGATIVE_INFINITY);
    }

    @Test
    public void testConstructorDoubleEpsilon() {
        BigFraction f1 = new BigFraction(0.3333333333333333, 1.0e-10, 100);
        Assert.assertEquals(BigInteger.ONE, f1.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(3), f1.getDenominator());

        BigFraction f2 = new BigFraction(2.0, 1.0e-5, 10);
        Assert.assertEquals(BigInteger.valueOf(2), f2.getNumerator());
        Assert.assertEquals(BigInteger.ONE, f2.getDenominator());

        BigFraction f3 = new BigFraction(0.4, 1.0e-10, 100);
        Assert.assertEquals(BigInteger.valueOf(2), f3.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(5), f3.getDenominator());
    }

    @Test(expected = FractionConversionException.class)
    public void testConstructorDoubleEpsilonOverflow() {
        new BigFraction(1e12, 1e-10, 10);
    }

    @Test(expected = FractionConversionException.class)
    public void testConstructorDoubleEpsilonMaxIterations() {
        new BigFraction(0.3333333333, 1.0e-15, 1);
    }

    @Test
    public void testConstructorDoubleMaxDenominator() {
        BigFraction f1 = new BigFraction(0.3333333333333333, 10);
        Assert.assertEquals(BigInteger.ONE, f1.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(3), f1.getDenominator());

        BigFraction f2 = new BigFraction(0.2857142857142857, 10);
        Assert.assertEquals(BigInteger.valueOf(2), f2.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(7), f2.getDenominator());
    }

    @Test
    public void testPrimitiveConstructors() {
        BigFraction f1 = new BigFraction(3);
        Assert.assertEquals(BigInteger.valueOf(3), f1.getNumerator());
        Assert.assertEquals(BigInteger.ONE, f1.getDenominator());

        BigFraction f2 = new BigFraction(3, 4);
        Assert.assertEquals(BigInteger.valueOf(3), f2.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(4), f2.getDenominator());

        BigFraction f3 = new BigFraction(3L);
        Assert.assertEquals(BigInteger.valueOf(3), f3.getNumerator());
        Assert.assertEquals(BigInteger.ONE, f3.getDenominator());

        BigFraction f4 = new BigFraction(3L, 4L);
        Assert.assertEquals(BigInteger.valueOf(3), f4.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(4), f4.getDenominator());
    }

    @Test
    public void testGetReducedFraction() {
        BigFraction f1 = BigFraction.getReducedFraction(0, 5);
        Assert.assertSame(BigFraction.ZERO, f1);

        BigFraction f2 = BigFraction.getReducedFraction(2, 4);
        Assert.assertEquals(BigInteger.ONE, f2.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(2), f2.getDenominator());
    }

    @Test
    public void testAbs() {
        BigFraction f1 = new BigFraction(-3, 4);
        BigFraction f2 = new BigFraction(3, 4);
        Assert.assertEquals(f2, f1.abs());
        Assert.assertSame(f2, f2.abs());
    }

    @Test
    public void testAdd() {
        BigFraction f1 = new BigFraction(1, 3);
        BigFraction f2 = new BigFraction(2, 3);
        BigFraction f3 = new BigFraction(1, 2);

        Assert.assertEquals(BigFraction.ONE, f1.add(f2));
        Assert.assertEquals(new BigFraction(5, 6), f1.add(f3));
        Assert.assertSame(f1, f1.add(BigFraction.ZERO));

        Assert.assertEquals(new BigFraction(4, 3), f1.add(BigInteger.ONE));
        Assert.assertEquals(new BigFraction(4, 3), f1.add(1));
        Assert.assertEquals(new BigFraction(4, 3), f1.add(1L));
    }

    @Test(expected = NullArgumentException.class)
    public void testAddNullBigFraction() {
        new BigFraction(1, 2).add((BigFraction) null);
    }

    @Test(expected = NullArgumentException.class)
    public void testAddNullBigInteger() {
        new BigFraction(1, 2).add((BigInteger) null);
    }

    @Test
    public void testSubtract() {
        BigFraction f1 = new BigFraction(2, 3);
        BigFraction f2 = new BigFraction(1, 3);
        BigFraction f3 = new BigFraction(1, 2);

        Assert.assertEquals(f2, f1.subtract(f2));
        Assert.assertEquals(new BigFraction(1, 6), f1.subtract(f3));
        Assert.assertSame(f1, f1.subtract(BigFraction.ZERO));

        Assert.assertEquals(new BigFraction(-1, 3), f1.subtract(BigInteger.ONE));
        Assert.assertEquals(new BigFraction(-1, 3), f1.subtract(1));
        Assert.assertEquals(new BigFraction(-1, 3), f1.subtract(1L));
    }

    @Test(expected = NullArgumentException.class)
    public void testSubtractNullBigFraction() {
        new BigFraction(1, 2).subtract((BigFraction) null);
    }

    @Test(expected = NullArgumentException.class)
    public void testSubtractNullBigInteger() {
        new BigFraction(1, 2).subtract((BigInteger) null);
    }

    @Test
    public void testMultiply() {
        BigFraction f1 = new BigFraction(2, 3);
        BigFraction f2 = new BigFraction(3, 4);

        Assert.assertEquals(BigFraction.ONE_HALF, f1.multiply(f2));
        Assert.assertSame(BigFraction.ZERO, f1.multiply(BigFraction.ZERO));
        Assert.assertSame(BigFraction.ZERO, BigFraction.ZERO.multiply(f1));

        Assert.assertEquals(new BigFraction(4, 3), f1.multiply(BigInteger.valueOf(2)));
        Assert.assertEquals(new BigFraction(4, 3), f1.multiply(2));
        Assert.assertEquals(new BigFraction(4, 3), f1.multiply(2L));
    }

    @Test(expected = NullArgumentException.class)
    public void testMultiplyNullBigFraction() {
        new BigFraction(1, 2).multiply((BigFraction) null);
    }

    @Test(expected = NullArgumentException.class)
    public void testMultiplyNullBigInteger() {
        new BigFraction(1, 2).multiply((BigInteger) null);
    }

    @Test
    public void testDivide() {
        BigFraction f1 = new BigFraction(1, 2);
        BigFraction f2 = new BigFraction(1, 4);

        Assert.assertEquals(BigFraction.TWO, f1.divide(f2));
        Assert.assertEquals(BigFraction.ONE_QUARTER, f1.divide(BigInteger.valueOf(2)));
        Assert.assertEquals(BigFraction.ONE_QUARTER, f1.divide(2));
        Assert.assertEquals(BigFraction.ONE_QUARTER, f1.divide(2L));
    }

    @Test(expected = NullArgumentException.class)
    public void testDivideNullBigFraction() {
        new BigFraction(1, 2).divide((BigFraction) null);
    }

    @Test(expected = ZeroException.class)
    public void testDivideZeroBigFraction() {
        new BigFraction(1, 2).divide(BigFraction.ZERO);
    }

    @Test(expected = ZeroException.class)
    public void testDivideZeroBigInteger() {
        new BigFraction(1, 2).divide(BigInteger.ZERO);
    }

    @Test(expected = ZeroException.class)
    public void testDivideZeroInt() {
        new BigFraction(1, 2).divide(0);
    }

    @Test(expected = ZeroException.class)
    public void testDivideZeroLong() {
        new BigFraction(1, 2).divide(0L);
    }

    @Test
    public void testNegate() {
        BigFraction f = new BigFraction(2, 3);
        Assert.assertEquals(new BigFraction(-2, 3), f.negate());
        Assert.assertEquals(f, f.negate().negate());
    }

    @Test
    public void testReciprocal() {
        BigFraction f = new BigFraction(2, 3);
        Assert.assertEquals(new BigFraction(3, 2), f.reciprocal());
    }

    @Test
    public void testPow() {
        BigFraction f = new BigFraction(2, 3);

        Assert.assertEquals(new BigFraction(4, 9), f.pow(2));
        Assert.assertEquals(new BigFraction(9, 4), f.pow(-2));
        Assert.assertEquals(BigFraction.ONE, f.pow(0));

        Assert.assertEquals(new BigFraction(4, 9), f.pow(2L));
        Assert.assertEquals(new BigFraction(9, 4), f.pow(-2L));
        Assert.assertEquals(BigFraction.ONE, f.pow(0L));

        Assert.assertEquals(new BigFraction(4, 9), f.pow(BigInteger.valueOf(2)));
        Assert.assertEquals(new BigFraction(9, 4), f.pow(BigInteger.valueOf(-2)));
        Assert.assertEquals(BigFraction.ONE, f.pow(BigInteger.ZERO));

        Assert.assertEquals(0.4444444444444444, f.pow(2.0), 1.0e-10);
    }

    @Test
    public void testConversions() {
        BigFraction f = new BigFraction(1, 2);
        Assert.assertEquals(0.5, f.doubleValue(), 1.0e-10);
        Assert.assertEquals(0.5f, f.floatValue(), 1.0e-10f);
        Assert.assertEquals(0, f.intValue());
        Assert.assertEquals(0L, f.longValue());

        BigFraction f2 = new BigFraction(5, 2);
        Assert.assertEquals(2, f2.intValue());
        Assert.assertEquals(2L, f2.longValue());

        Assert.assertEquals(50.0, f.percentageValue(), 1.0e-10);

        Assert.assertEquals(new BigDecimal("0.5"), f.bigDecimalValue());
        Assert.assertEquals(new BigDecimal("0.50"), f.bigDecimalValue(2, BigDecimal.ROUND_HALF_UP));
        Assert.assertEquals(new BigDecimal("0.5"), f.bigDecimalValue(BigDecimal.ROUND_HALF_UP));
    }

    @Test
    public void testGetParts() {
        BigFraction f = new BigFraction(3, 4);
        Assert.assertEquals(BigInteger.valueOf(3), f.getNumerator());
        Assert.assertEquals(3, f.getNumeratorAsInt());
        Assert.assertEquals(3L, f.getNumeratorAsLong());

        Assert.assertEquals(BigInteger.valueOf(4), f.getDenominator());
        Assert.assertEquals(4, f.getDenominatorAsInt());
        Assert.assertEquals(4L, f.getDenominatorAsLong());
    }

    @Test
    public void testCompareTo() {
        BigFraction f1 = new BigFraction(1, 2);
        BigFraction f2 = new BigFraction(2, 4);
        BigFraction f3 = new BigFraction(1, 3);
        BigFraction f4 = new BigFraction(2, 3);

        Assert.assertEquals(0, f1.compareTo(f2));
        Assert.assertTrue(f1.compareTo(f3) > 0);
        Assert.assertTrue(f1.compareTo(f4) < 0);
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
        Assert.assertFalse(f1.equals("string"));

        Assert.assertEquals(f1.hashCode(), f2.hashCode());
    }

    @Test
    public void testToString() {
        Assert.assertEquals("1 / 2", new BigFraction(1, 2).toString());
        Assert.assertEquals("2", new BigFraction(2).toString());
        Assert.assertEquals("0", new BigFraction(0).toString());
    }

    @Test
    public void testGetField() {
        Assert.assertNotNull(BigFraction.ONE.getField());
        Assert.assertSame(BigFractionField.getInstance(), BigFraction.ONE.getField());
    }

    @Test
    public void testSerial() {
        BigFraction f = new BigFraction(3, 4);
        Assert.assertEquals(f, TestUtils.serializedCopy(f));
    }
}
