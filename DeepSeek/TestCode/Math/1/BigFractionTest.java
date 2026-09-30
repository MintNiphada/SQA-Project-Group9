package org.apache.commons.math3.fraction;

import static org.junit.Assert.*;
import org.junit.Test;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.ZeroException;

public class BigFractionTest {

    @Test
    public void testConstructorBigInteger() {
        BigFraction f = new BigFraction(BigInteger.valueOf(5));
        assertEquals(BigInteger.valueOf(5), f.getNumerator());
        assertEquals(BigInteger.ONE, f.getDenominator());
    }

    @Test
    public void testConstructorBigIntegerNull() {
        try {
            new BigFraction(null);
            fail("Expected NullArgumentException");
        } catch (NullArgumentException e) {
        }
    }

    @Test
    public void testConstructorBigIntegerBigIntegerBasic() {
        BigFraction f = new BigFraction(BigInteger.valueOf(2), BigInteger.valueOf(4));
        assertEquals(BigInteger.ONE, f.getNumerator());
        assertEquals(BigInteger.valueOf(2), f.getDenominator());
    }

    @Test
    public void testConstructorBigIntegerBigIntegerZeroDenominator() {
        try {
            new BigFraction(BigInteger.ONE, BigInteger.ZERO);
            fail("Expected ZeroException");
        } catch (ZeroException e) {
        }
    }

    @Test
    public void testConstructorBigIntegerBigIntegerZeroNumerator() {
        BigFraction f = new BigFraction(BigInteger.ZERO, BigInteger.valueOf(5));
        assertEquals(BigInteger.ZERO, f.getNumerator());
        assertEquals(BigInteger.ONE, f.getDenominator());
    }

    @Test
    public void testConstructorBigIntegerBigIntegerNegativeDenominator() {
        BigFraction f = new BigFraction(BigInteger.ONE, BigInteger.valueOf(-2));
        assertEquals(BigInteger.valueOf(-1), f.getNumerator());
        assertEquals(BigInteger.valueOf(2), f.getDenominator());
    }

    @Test
    public void testConstructorBigIntegerBigIntegerBothNegative() {
        BigFraction f = new BigFraction(BigInteger.valueOf(-3), BigInteger.valueOf(-6));
        assertEquals(BigInteger.ONE, f.getNumerator());
        assertEquals(BigInteger.valueOf(2), f.getDenominator());
    }

    @Test
    public void testConstructorBigIntegerBigIntegerNullNum() {
        try {
            new BigFraction(null, BigInteger.ONE);
            fail("Expected NullArgumentException");
        } catch (NullArgumentException e) {
        }
    }

    @Test
    public void testConstructorBigIntegerBigIntegerNullDen() {
        try {
            new BigFraction(BigInteger.ONE, null);
            fail("Expected NullArgumentException");
        } catch (NullArgumentException e) {
        }
    }

    @Test
    public void testConstructorDoubleNormal() {
        BigFraction f = new BigFraction(0.5);
        assertEquals(BigInteger.ONE, f.getNumerator());
        assertEquals(BigInteger.valueOf(2), f.getDenominator());
    }

    @Test
    public void testConstructorDoubleNegative() {
        BigFraction f = new BigFraction(-0.5);
        assertEquals(BigInteger.valueOf(-1), f.getNumerator());
        assertEquals(BigInteger.valueOf(2), f.getDenominator());
    }

    @Test
    public void testConstructorDoubleZero() {
        BigFraction f = new BigFraction(0.0);
        assertEquals(BigInteger.ZERO, f.getNumerator());
        assertEquals(BigInteger.ONE, f.getDenominator());
    }

    @Test
    public void testConstructorDoubleNaN() {
        try {
            new BigFraction(Double.NaN);
            fail("Expected MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
        }
    }

    @Test
    public void testConstructorDoublePositiveInfinity() {
        try {
            new BigFraction(Double.POSITIVE_INFINITY);
            fail("Expected MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
        }
    }

    @Test
    public void testConstructorDoubleNegativeInfinity() {
        try {
            new BigFraction(Double.NEGATIVE_INFINITY);
            fail("Expected MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
        }
    }

    @Test
    public void testConstructorDoubleSubnormal() {
        double small = Double.longBitsToDouble(0x0000000000000001L);
        BigFraction f = new BigFraction(small);
        assertEquals(BigInteger.ONE, f.getNumerator());
        assertTrue(f.getDenominator().compareTo(BigInteger.ONE) > 0);
    }

    @Test
    public void testConstructorDoubleEpsilonMaxIterationsExact() throws FractionConversionException {
        BigFraction f = new BigFraction(0.5, 1e-10, 100);
        assertEquals(BigInteger.ONE, f.getNumerator());
        assertEquals(BigInteger.valueOf(2), f.getDenominator());
    }

    @Test
    public void testConstructorDoubleEpsilonMaxIterationsConvergence() throws FractionConversionException {
        BigFraction f = new BigFraction(1.0/3.0, 1e-6, 100);
        double value = f.doubleValue();
        assertTrue(Math.abs(value - 1.0/3.0) < 1e-6);
    }

    @Test(expected = FractionConversionException.class)
    public void testConstructorDoubleEpsilonMaxIterationsOverflow() throws FractionConversionException {
        new BigFraction(0.5, 0.0, 1); // maxIterations=1, no convergence
    }

    @Test(expected = FractionConversionException.class)
    public void testConstructorDoubleEpsilonMaxIterationsFail() throws FractionConversionException {
        new BigFraction(1e100, 0.0, 10);
    }

    @Test
    public void testConstructorDoubleEpsilonIntegerAlmostInteger() throws FractionConversionException {
        BigFraction f = new BigFraction(2.0, 1e-10, 100);
        assertEquals(BigInteger.valueOf(2), f.getNumerator());
        assertEquals(BigInteger.ONE, f.getDenominator());
    }

    @Test
    public void testConstructorDoubleMaxDenominator() throws FractionConversionException {
        BigFraction f = new BigFraction(Math.PI, 10);
        assertTrue(f.getDenominator().intValue() <= 10);
    }

    @Test
    public void testConstructorDoubleMaxDenominatorExact() throws FractionConversionException {
        BigFraction f = new BigFraction(0.5, 10);
        assertEquals(BigInteger.ONE, f.getNumerator());
        assertEquals(BigInteger.valueOf(2), f.getDenominator());
    }

    @Test(expected = FractionConversionException.class)
    public void testConstructorDoubleMaxDenominatorOverflow() throws FractionConversionException {
        new BigFraction(1e100, 100);
    }

    @Test
    public void testConstructorInt() {
        BigFraction f = new BigFraction(5);
        assertEquals(BigInteger.valueOf(5), f.getNumerator());
        assertEquals(BigInteger.ONE, f.getDenominator());
    }

    @Test
    public void testConstructorIntInt() {
        BigFraction f = new BigFraction(2, 4);
        assertEquals(BigInteger.ONE, f.getNumerator());
        assertEquals(BigInteger.valueOf(2), f.getDenominator());
    }

    @Test
    public void testConstructorIntIntZeroDen() {
        try {
            new BigFraction(1, 0);
            fail("Should have thrown");
        } catch (ZeroException e) {
        }
    }

    @Test
    public void testConstructorLong() {
        BigFraction f = new BigFraction(10L);
        assertEquals(BigInteger.TEN, f.getNumerator());
        assertEquals(BigInteger.ONE, f.getDenominator());
    }

    @Test
    public void testConstructorLongLong() {
        BigFraction f = new BigFraction(10L, 20L);
        assertEquals(BigInteger.ONE, f.getNumerator());
        assertEquals(BigInteger.valueOf(2), f.getDenominator());
    }

    @Test
    public void testGetReducedFraction() {
        BigFraction f = BigFraction.getReducedFraction(2, 4);
        assertEquals(BigInteger.ONE, f.getNumerator());
        assertEquals(BigInteger.valueOf(2), f.getDenominator());
    }

    @Test
    public void testGetReducedFractionZeroNumerator() {
        BigFraction f = BigFraction.getReducedFraction(0, 5);
        assertSame(BigFraction.ZERO, f);
    }

    @Test
    public void testAbsPositive() {
        BigFraction f = new BigFraction(3, 4);
        assertEquals(f, f.abs());
    }

    @Test
    public void testAbsNegative() {
        BigFraction f = new BigFraction(-3, 4);
        assertEquals(new BigFraction(3, 4), f.abs());
    }

    @Test
    public void testAddBigInteger() {
        BigFraction f = new BigFraction(1, 2);
        BigFraction sum = f.add(BigInteger.valueOf(3));
        assertEquals(new BigFraction(7, 2), sum);
    }

    @Test(expected = NullArgumentException.class)
    public void testAddBigIntegerNull() {
        new BigFraction(1, 2).add((BigInteger) null);
    }

    @Test
    public void testAddInt() {
        BigFraction f = new BigFraction(1, 2);
        assertEquals(new BigFraction(5, 2), f.add(2));
    }

    @Test
    public void testAddLong() {
        BigFraction f = new BigFraction(1, 2);
        assertEquals(new BigFraction(5, 2), f.add(2L));
    }

    @Test
    public void testAddFraction() {
        BigFraction f1 = new BigFraction(1, 2);
        BigFraction f2 = new BigFraction(1, 3);
        BigFraction sum = f1.add(f2);
        assertEquals(new BigFraction(5, 6), sum);
    }

    @Test
    public void testAddFractionSameDenominator() {
        BigFraction f1 = new BigFraction(1, 5);
        BigFraction f2 = new BigFraction(2, 5);
        assertEquals(new BigFraction(3, 5), f1.add(f2));
    }

    @Test(expected = NullArgumentException.class)
    public void testAddFractionNull() {
        new BigFraction(1, 2).add((BigFraction) null);
    }

    @Test
    public void testAddFractionZero() {
        BigFraction f = new BigFraction(1, 3);
        assertEquals(f, f.add(BigFraction.ZERO));
    }

    @Test
    public void testBigDecimalValue() {
        BigFraction f = new BigFraction(1, 2);
        assertEquals(new BigDecimal("0.5"), f.bigDecimalValue());
    }

    @Test
    public void testBigDecimalValueRoundingMode() {
        BigFraction f = new BigFraction(1, 3);
        BigDecimal bd = f.bigDecimalValue(BigDecimal.ROUND_HALF_UP);
        assertEquals(new BigDecimal("0.33"), bd);
    }

    @Test
    public void testBigDecimalValueScaleRoundingMode() {
        BigFraction f = new BigFraction(1, 3);
        BigDecimal bd = f.bigDecimalValue(5, BigDecimal.ROUND_HALF_UP);
        assertEquals(new BigDecimal("0.33333"), bd);
    }

    @Test
    public void testCompareToEqual() {
        BigFraction f1 = new BigFraction(1, 2);
        BigFraction f2 = new BigFraction(2, 4);
        assertEquals(0, f1.compareTo(f2));
    }

    @Test
    public void testCompareToLess() {
        BigFraction f1 = new BigFraction(1, 3);
        BigFraction f2 = new BigFraction(1, 2);
        assertTrue(f1.compareTo(f2) < 0);
    }

    @Test
    public void testCompareToGreater() {
        BigFraction f1 = new BigFraction(2, 3);
        BigFraction f2 = new BigFraction(1, 3);
        assertTrue(f1.compareTo(f2) > 0);
    }

    @Test
    public void testDivideBigInteger() {
        BigFraction f = new BigFraction(1, 2);
        BigFraction result = f.divide(BigInteger.valueOf(3));
        assertEquals(new BigFraction(1, 6), result);
    }

    @Test(expected = NullArgumentException.class)
    public void testDivideBigIntegerNull() {
        new BigFraction(1, 2).divide((BigInteger) null);
    }

    @Test(expected = MathArithmeticException.class)
    public void testDivideBigIntegerZero() {
        new BigFraction(1, 2).divide(BigInteger.ZERO);
    }

    @Test
    public void testDivideInt() {
        BigFraction f = new BigFraction(1, 2);
        assertEquals(new BigFraction(1, 6), f.divide(3));
    }

    @Test(expected = MathArithmeticException.class)
    public void testDivideIntZero() {
        new BigFraction(1, 2).divide(0);
    }

    @Test
    public void testDivideLong() {
        BigFraction f = new BigFraction(1, 2);
        assertEquals(new BigFraction(1, 6), f.divide(3L));
    }

    @Test(expected = MathArithmeticException.class)
    public void testDivideLongZero() {
        new BigFraction(1, 2).divide(0L);
    }

    @Test
    public void testDivideFraction() {
        BigFraction f1 = new BigFraction(1, 2);
        BigFraction f2 = new BigFraction(3, 4);
        assertEquals(new BigFraction(2, 3), f1.divide(f2));
    }

    @Test(expected = NullArgumentException.class)
    public void testDivideFractionNull() {
        new BigFraction(1, 2).divide((BigFraction) null);
    }

    @Test(expected = MathArithmeticException.class)
    public void testDivideFractionZeroNumerator() {
        new BigFraction(1, 2).divide(BigFraction.ZERO);
    }

    @Test
    public void testDoubleValueNormal() {
        BigFraction f = new BigFraction(1, 2);
        assertEquals(0.5, f.doubleValue(), 1e-15);
    }

    @Test
    public void testDoubleValueLargeFraction() {
        BigFraction f = new BigFraction(BigInteger.valueOf(1).shiftLeft(1000), BigInteger.ONE);
        assertFalse(Double.isNaN(f.doubleValue()));
    }

    @Test
    public void testEqualsSameObject() {
        BigFraction f = new BigFraction(1, 2);
        assertTrue(f.equals(f));
    }

    @Test
    public void testEqualsEqual() {
        BigFraction f1 = new BigFraction(1, 2);
        BigFraction f2 = new BigFraction(2, 4);
        assertTrue(f1.equals(f2));
    }

    @Test
    public void testEqualsNotEqual() {
        BigFraction f1 = new BigFraction(1, 2);
        BigFraction f2 = new BigFraction(1, 3);
        assertFalse(f1.equals(f2));
    }

    @Test
    public void testEqualsNull() {
        assertFalse(new BigFraction(1, 2).equals(null));
    }

    @Test
    public void testEqualsOtherType() {
        assertFalse(new BigFraction(1, 2).equals("hello"));
    }

    @Test
    public void testFloatValueNormal() {
        BigFraction f = new BigFraction(1, 2);
        assertEquals(0.5f, f.floatValue(), 1e-8);
    }

    @Test
    public void testFloatValueLarge() {
        BigFraction f = new BigFraction(BigInteger.valueOf(1).shiftLeft(1000), BigInteger.ONE);
        assertFalse(Float.isNaN(f.floatValue()));
    }

    @Test
    public void testGetDenominator() {
        BigFraction f = new BigFraction(2, 4);
        assertEquals(BigInteger.valueOf(2), f.getDenominator());
    }

    @Test
    public void testGetDenominatorAsInt() {
        BigFraction f = new BigFraction(2, 4);
        assertEquals(2, f.getDenominatorAsInt());
    }

    @Test
    public void testGetDenominatorAsLong() {
        BigFraction f = new BigFraction(2, 4);
        assertEquals(2L, f.getDenominatorAsLong());
    }

    @Test
    public void testGetNumerator() {
        BigFraction f = new BigFraction(2, 4);
        assertEquals(BigInteger.ONE, f.getNumerator());
    }

    @Test
    public void testGetNumeratorAsInt() {
        BigFraction f = new BigFraction(2, 4);
        assertEquals(1, f.getNumeratorAsInt());
    }

    @Test
    public void testGetNumeratorAsLong() {
        BigFraction f = new BigFraction(2, 4);
        assertEquals(1L, f.getNumeratorAsLong());
    }

    @Test
    public void testHashCodeEqual() {
        BigFraction f1 = new BigFraction(1, 2);
        BigFraction f2 = new BigFraction(2, 4);
        assertEquals(f1.hashCode(), f2.hashCode());
    }

    @Test
    public void testIntValue() {
        BigFraction f = new BigFraction(5, 2);
        assertEquals(2, f.intValue());
    }

    @Test
    public void testLongValue() {
        BigFraction f = new BigFraction(5, 2);
        assertEquals(2L, f.longValue());
    }

    @Test
    public void testMultiplyBigInteger() {
        BigFraction f = new BigFraction(1, 2);
        assertEquals(new BigFraction(3, 2), f.multiply(BigInteger.valueOf(3)));
    }

    @Test(expected = NullArgumentException.class)
    public void testMultiplyBigIntegerNull() {
        new BigFraction(1, 2).multiply((BigInteger) null);
    }

    @Test
    public void testMultiplyInt() {
        BigFraction f = new BigFraction(1, 2);
        assertEquals(new BigFraction(3, 2), f.multiply(3));
    }

    @Test
    public void testMultiplyLong() {
        BigFraction f = new BigFraction(1, 2);
        assertEquals(new BigFraction(3, 2), f.multiply(3L));
    }

    @Test
    public void testMultiplyFraction() {
        BigFraction f1 = new BigFraction(1, 2);
        BigFraction f2 = new BigFraction(2, 3);
        assertEquals(new BigFraction(1, 3), f1.multiply(f2));
    }

    @Test
    public void testMultiplyFractionZero() {
        BigFraction f = new BigFraction(1, 3);
        assertEquals(BigFraction.ZERO, f.multiply(BigFraction.ZERO));
    }

    @Test
    public void testMultiplyFractionFromZero() {
        assertEquals(BigFraction.ZERO, BigFraction.ZERO.multiply(new BigFraction(1, 2)));
    }

    @Test(expected = NullArgumentException.class)
    public void testMultiplyFractionNull() {
        new BigFraction(1, 2).multiply((BigFraction) null);
    }

    @Test
    public void testNegatePositive() {
        BigFraction f = new BigFraction(3, 4);
        assertEquals(new BigFraction(-3, 4), f.negate());
    }

    @Test
    public void testNegateNegative() {
        BigFraction f = new BigFraction(-3, 4);
        assertEquals(new BigFraction(3, 4), f.negate());
    }

    @Test
    public void testPercentageValue() {
        BigFraction f = new BigFraction(1, 2);
        assertEquals(50.0, f.percentageValue(), 1e-15);
    }

    @Test
    public void testPowIntPositive() {
        BigFraction f = new BigFraction(2, 3);
        assertEquals(new BigFraction(4, 9), f.pow(2));
    }

    @Test
    public void testPowIntNegative() {
        BigFraction f = new BigFraction(2, 3);
        assertEquals(new BigFraction(9, 4), f.pow(-2));
    }

    @Test
    public void testPowIntZero() {
        BigFraction f = new BigFraction(2, 3);
        assertEquals(BigFraction.ONE, f.pow(0));
    }

    @Test
    public void testPowLongPositive() {
        BigFraction f = new BigFraction(2, 3);
        assertEquals(new BigFraction(4, 9), f.pow(2L));
    }

    @Test
    public void testPowLongNegative() {
        BigFraction f = new BigFraction(2, 3);
        assertEquals(new BigFraction(9, 4), f.pow(-2L));
    }

    @Test
    public void testPowBigIntegerPositive() {
        BigFraction f = new BigFraction(2, 3);
        assertEquals(new BigFraction(4, 9), f.pow(BigInteger.valueOf(2)));
    }

    @Test
    public void testPowBigIntegerNegative() {
        BigFraction f = new BigFraction(2, 3);
        assertEquals(new BigFraction(9, 4), f.pow(BigInteger.valueOf(-2)));
    }

    @Test
    public void testPowDouble() {
        BigFraction f = new BigFraction(2, 3);
        double expected = Math.pow(2.0/3.0, 2.0);
        assertEquals(expected, f.pow(2.0), 1e-15);
    }

    @Test
    public void testReciprocal() {
        BigFraction f = new BigFraction(2, 3);
        assertEquals(new BigFraction(3, 2), f.reciprocal());
    }

    @Test
    public void testReduceAlreadyReduced() {
        BigFraction f = new BigFraction(2, 3);
        assertSame(f, f.reduce()); // same instance? Actually reduce returns new BigFraction if not reducible? It returns new every time, but test equality.
        assertEquals(new BigFraction(2, 3), f.reduce());
    }

    @Test
    public void testReduceNotReduced() {
        BigFraction f = new BigFraction(4, 6);
        BigFraction reduced = f.reduce();
        assertEquals(new BigFraction(2, 3), reduced);
    }

    @Test
    public void testSubtractBigInteger() {
        BigFraction f = new BigFraction(3, 2);
        assertEquals(new BigFraction(-1, 2), f.subtract(BigInteger.valueOf(2)));
    }

    @Test(expected = NullArgumentException.class)
    public void testSubtractBigIntegerNull() {
        new BigFraction(1, 2).subtract((BigInteger) null);
    }

    @Test
    public void testSubtractInt() {
        BigFraction f = new BigFraction(3, 2);
        assertEquals(new BigFraction(-1, 2), f.subtract(2));
    }

    @Test
    public void testSubtractLong() {
        BigFraction f = new BigFraction(3, 2);
        assertEquals(new BigFraction(-1, 2), f.subtract(2L));
    }

    @Test
    public void testSubtractFraction() {
        BigFraction f1 = new BigFraction(3, 4);
        BigFraction f2 = new BigFraction(1, 2);
        assertEquals(new BigFraction(1, 4), f1.subtract(f2));
    }

    @Test
    public void testSubtractFractionSameDenominator() {
        BigFraction f1 = new BigFraction(3, 5);
        BigFraction f2 = new BigFraction(1, 5);
        assertEquals(new BigFraction(2, 5), f1.subtract(f2));
    }

    @Test(expected = NullArgumentException.class)
    public void testSubtractFractionNull() {
        new BigFraction(1, 2).subtract((BigFraction) null);
    }

    @Test
    public void testSubtractFractionZero() {
        BigFraction f = new BigFraction(1, 2);
        assertEquals(f, f.subtract(BigFraction.ZERO));
    }

    @Test
    public void testToStringInteger() {
        BigFraction f = new BigFraction(3, 1);
        assertEquals("3", f.toString());
    }

    @Test
    public void testToStringZero() {
        BigFraction f = BigFraction.ZERO;
        assertEquals("0", f.toString());
    }

    @Test
    public void testToStringFraction() {
        BigFraction f = new BigFraction(3, 4);
        assertEquals("3 / 4", f.toString());
    }

    @Test
    public void testGetField() {
        assertNotNull(BigFraction.ZERO.getField());
    }

    // Additional edge cases for double constructor (bits manipulation)
    @Test
    public void testConstructorDoublePowerOfTwo() {
        double d = 8.0;
        BigFraction f = new BigFraction(d);
        assertEquals(BigInteger.valueOf(8), f.getNumerator());
        assertEquals(BigInteger.ONE, f.getDenominator());
    }

    // Test for double value of a very small number (subnormal)
    @Test
    public void testConstructorDoubleTiny() {
        double tiny = Double.MIN_VALUE;
        BigFraction f = new BigFraction(tiny);
        assertFalse(Double.isNaN(f.doubleValue()));
    }

    // Test for division by int/long zero using divide(BigInteger)
    @Test(expected = MathArithmeticException.class)
    public void testDivideBigIntegerZeroExplicit() {
        new BigFraction(1).divide(BigInteger.ZERO);
    }

    // Test for pow with large BigInteger
    @Test
    public void testPowBigIntegerLarge() {
        BigFraction f = new BigFraction(2, 3);
        BigFraction pow = f.pow(BigInteger.valueOf(50));
        assertEquals(new BigFraction(BigInteger.valueOf(2).pow(50), BigInteger.valueOf(3).pow(50)), pow);
    }
}
