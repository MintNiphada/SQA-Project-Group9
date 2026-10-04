package org.apache.commons.math3.fraction;

import org.junit.Test;
import static org.junit.Assert.*;
import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.FractionConversionException;

public class FractionTest {

    @Test
    public void testIntConstructor() {
        Fraction f = new Fraction(5);
        assertEquals(5, f.getNumerator());
        assertEquals(1, f.getDenominator());
    }

    @Test
    public void testIntIntConstructorPositive() {
        Fraction f = new Fraction(2, 4);
        assertEquals(1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    @Test
    public void testIntIntConstructorNegativeDenominator() {
        Fraction f = new Fraction(2, -4);
        assertEquals(-1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    @Test
    public void testIntIntConstructorBothNegative() {
        Fraction f = new Fraction(-2, -4);
        assertEquals(1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    @Test(expected = MathArithmeticException.class)
    public void testIntIntConstructorZeroDenominator() {
        new Fraction(1, 0);
    }

    @Test
    public void testIntIntConstructorZeroNumerator() {
        Fraction f = new Fraction(0, 5);
        assertEquals(0, f.getNumerator());
        assertEquals(1, f.getDenominator());
    }

    @Test(expected = MathArithmeticException.class)
    public void testIntIntConstructorOverflowNumMinValue() {
        new Fraction(Integer.MIN_VALUE, -1);
    }

    @Test(expected = MathArithmeticException.class)
    public void testIntIntConstructorOverflowDenMinValue() {
        new Fraction(1, Integer.MIN_VALUE);
    }

    @Test
    public void testDoubleConstructorExactInteger() throws FractionConversionException {
        Fraction f = new Fraction(5.0);
        assertEquals(5, f.getNumerator());
        assertEquals(1, f.getDenominator());
    }

    @Test
    public void testDoubleConstructorSimpleFraction() throws FractionConversionException {
        Fraction f = new Fraction(0.5);
        assertEquals(1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    @Test
    public void testDoubleConstructorNegative() throws FractionConversionException {
        Fraction f = new Fraction(-0.75);
        assertEquals(-3, f.getNumerator());
        assertEquals(4, f.getDenominator());
    }

    @Test(expected = FractionConversionException.class)
    public void testDoubleConstructorMaxIterationsExceeded() throws FractionConversionException {
        new Fraction(0.123456789, 1.0e-15, 2);
    }

    @Test
    public void testDoubleConstructorWithEpsilon() throws FractionConversionException {
        Fraction f = new Fraction(0.33333333, 1.0e-5, 100);
        assertEquals(1, f.getNumerator());
        assertEquals(3, f.getDenominator());
    }

    @Test
    public void testDoubleConstructorWithMaxDenominator() throws FractionConversionException {
        Fraction f = new Fraction(0.3333, 10);
        assertEquals(1, f.getNumerator());
        assertEquals(3, f.getDenominator());
    }

    @Test
    public void testDoubleConstructorAlmostInteger() throws FractionConversionException {
        Fraction f = new Fraction(2.0000001, 1.0e-5, 100);
        assertEquals(2, f.getNumerator());
        assertEquals(1, f.getDenominator());
    }

    @Test
    public void testAbsPositive() {
        Fraction f = new Fraction(3, 4);
        assertSame(f, f.abs());
    }

    @Test
    public void testAbsNegative() {
        Fraction f = new Fraction(-3, 4);
        Fraction abs = f.abs();
        assertEquals(3, abs.getNumerator());
        assertEquals(4, abs.getDenominator());
    }

    @Test
    public void testCompareToEqual() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = new Fraction(2, 4);
        assertEquals(0, f1.compareTo(f2));
    }

    @Test
    public void testCompareToLess() {
        Fraction f1 = new Fraction(1, 3);
        Fraction f2 = new Fraction(1, 2);
        assertEquals(-1, f1.compareTo(f2));
    }

    @Test
    public void testCompareToGreater() {
        Fraction f1 = new Fraction(2, 3);
        Fraction f2 = new Fraction(1, 2);
        assertEquals(1, f1.compareTo(f2));
    }

    @Test
    public void testDoubleValue() {
        Fraction f = new Fraction(1, 2);
        assertEquals(0.5, f.doubleValue(), 1.0e-15);
    }

    @Test
    public void testFloatValue() {
        Fraction f = new Fraction(1, 2);
        assertEquals(0.5f, f.floatValue(), 1.0e-15);
    }

    @Test
    public void testIntValue() {
        Fraction f = new Fraction(3, 2);
        assertEquals(1, f.intValue());
    }

    @Test
    public void testLongValue() {
        Fraction f = new Fraction(3, 2);
        assertEquals(1L, f.longValue());
    }

    @Test
    public void testEqualsSameObject() {
        Fraction f = new Fraction(1, 2);
        assertTrue(f.equals(f));
    }

    @Test
    public void testEqualsEqualFractions() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = new Fraction(2, 4);
        assertTrue(f1.equals(f2));
    }

    @Test
    public void testEqualsNotEqual() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = new Fraction(1, 3);
        assertFalse(f1.equals(f2));
    }

    @Test
    public void testEqualsNull() {
        Fraction f = new Fraction(1, 2);
        assertFalse(f.equals(null));
    }

    @Test
    public void testEqualsDifferentClass() {
        Fraction f = new Fraction(1, 2);
        assertFalse(f.equals("string"));
    }

    @Test
    public void testHashCodeConsistentWithEquals() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = new Fraction(2, 4);
        assertEquals(f1.hashCode(), f2.hashCode());
    }

    @Test
    public void testGetDenominator() {
        Fraction f = new Fraction(3, 4);
        assertEquals(4, f.getDenominator());
    }

    @Test
    public void testGetNumerator() {
        Fraction f = new Fraction(3, 4);
        assertEquals(3, f.getNumerator());
    }

    @Test
    public void testNegatePositive() {
        Fraction f = new Fraction(3, 4);
        Fraction neg = f.negate();
        assertEquals(-3, neg.getNumerator());
        assertEquals(4, neg.getDenominator());
    }

    @Test
    public void testNegateNegative() {
        Fraction f = new Fraction(-3, 4);
        Fraction neg = f.negate();
        assertEquals(3, neg.getNumerator());
        assertEquals(4, neg.getDenominator());
    }

    @Test(expected = MathArithmeticException.class)
    public void testNegateOverflow() {
        Fraction f = new Fraction(Integer.MIN_VALUE, 1);
        f.negate();
    }

    @Test
    public void testReciprocal() {
        Fraction f = new Fraction(3, 4);
        Fraction rec = f.reciprocal();
        assertEquals(4, rec.getNumerator());
        assertEquals(3, rec.getDenominator());
    }

    @Test
    public void testReciprocalOfZero() {
        Fraction f = new Fraction(0, 1);
        Fraction rec = f.reciprocal();
        assertEquals(1, rec.getNumerator());
        assertEquals(0, rec.getDenominator());
    }

    @Test
    public void testAddFraction() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = new Fraction(1, 3);
        Fraction sum = f1.add(f2);
        assertEquals(5, sum.getNumerator());
        assertEquals(6, sum.getDenominator());
    }

    @Test
    public void testAddFractionZero() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = Fraction.ZERO;
        Fraction sum = f1.add(f2);
        assertEquals(1, sum.getNumerator());
        assertEquals(2, sum.getDenominator());
    }

    @Test
    public void testAddFractionToZero() {
        Fraction f1 = Fraction.ZERO;
        Fraction f2 = new Fraction(1, 2);
        Fraction sum = f1.add(f2);
        assertEquals(1, sum.getNumerator());
        assertEquals(2, sum.getDenominator());
    }

    @Test(expected = NullArgumentException.class)
    public void testAddFractionNull() {
        Fraction f = new Fraction(1, 2);
        f.add(null);
    }

    @Test
    public void testAddInt() {
        Fraction f = new Fraction(1, 2);
        Fraction sum = f.add(2);
        assertEquals(5, sum.getNumerator());
        assertEquals(2, sum.getDenominator());
    }

    @Test
    public void testSubtractFraction() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = new Fraction(1, 3);
        Fraction diff = f1.subtract(f2);
        assertEquals(1, diff.getNumerator());
        assertEquals(6, diff.getDenominator());
    }

    @Test
    public void testSubtractFractionZero() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = Fraction.ZERO;
        Fraction diff = f1.subtract(f2);
        assertEquals(1, diff.getNumerator());
        assertEquals(2, diff.getDenominator());
    }

    @Test
    public void testSubtractFractionFromZero() {
        Fraction f1 = Fraction.ZERO;
        Fraction f2 = new Fraction(1, 2);
        Fraction diff = f1.subtract(f2);
        assertEquals(-1, diff.getNumerator());
        assertEquals(2, diff.getDenominator());
    }

    @Test(expected = NullArgumentException.class)
    public void testSubtractFractionNull() {
        Fraction f = new Fraction(1, 2);
        f.subtract(null);
    }

    @Test
    public void testSubtractInt() {
        Fraction f = new Fraction(3, 2);
        Fraction diff = f.subtract(1);
        assertEquals(1, diff.getNumerator());
        assertEquals(2, diff.getDenominator());
    }

    @Test
    public void testMultiplyFraction() {
        Fraction f1 = new Fraction(2, 3);
        Fraction f2 = new Fraction(3, 4);
        Fraction prod = f1.multiply(f2);
        assertEquals(1, prod.getNumerator());
        assertEquals(2, prod.getDenominator());
    }

    @Test
    public void testMultiplyFractionZero() {
        Fraction f1 = new Fraction(2, 3);
        Fraction f2 = Fraction.ZERO;
        Fraction prod = f1.multiply(f2);
        assertEquals(Fraction.ZERO, prod);
    }

    @Test(expected = NullArgumentException.class)
    public void testMultiplyFractionNull() {
        Fraction f = new Fraction(1, 2);
        f.multiply(null);
    }

    @Test
    public void testMultiplyInt() {
        Fraction f = new Fraction(2, 3);
        Fraction prod = f.multiply(3);
        assertEquals(2, prod.getNumerator());
        assertEquals(1, prod.getDenominator());
    }

    @Test
    public void testDivideFraction() {
        Fraction f1 = new Fraction(2, 3);
        Fraction f2 = new Fraction(3, 4);
        Fraction quot = f1.divide(f2);
        assertEquals(8, quot.getNumerator());
        assertEquals(9, quot.getDenominator());
    }

    @Test(expected = MathArithmeticException.class)
    public void testDivideFractionByZero() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = Fraction.ZERO;
        f1.divide(f2);
    }

    @Test(expected = NullArgumentException.class)
    public void testDivideFractionNull() {
        Fraction f = new Fraction(1, 2);
        f.divide(null);
    }

    @Test
    public void testDivideInt() {
        Fraction f = new Fraction(2, 3);
        Fraction quot = f.divide(2);
        assertEquals(2, quot.getNumerator());
        assertEquals(6, quot.getDenominator());
    }

    @Test
    public void testPercentageValue() {
        Fraction f = new Fraction(1, 2);
        assertEquals(50.0, f.percentageValue(), 1.0e-15);
    }

    @Test
    public void testGetReducedFractionNormal() {
        Fraction f = Fraction.getReducedFraction(2, 4);
        assertEquals(1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    @Test
    public void testGetReducedFractionZeroNumerator() {
        Fraction f = Fraction.getReducedFraction(0, 5);
        assertEquals(Fraction.ZERO, f);
    }

    @Test(expected = MathArithmeticException.class)
    public void testGetReducedFractionZeroDenominator() {
        Fraction.getReducedFraction(1, 0);
    }

    @Test
    public void testGetReducedFractionNegativeDenominator() {
        Fraction f = Fraction.getReducedFraction(1, -2);
        assertEquals(-1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    @Test
    public void testGetReducedFractionBothNegative() {
        Fraction f = Fraction.getReducedFraction(-1, -2);
        assertEquals(1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    @Test
    public void testGetReducedFractionMinValueDenominatorEvenNumerator() {
        Fraction f = Fraction.getReducedFraction(2, Integer.MIN_VALUE);
        assertEquals(1, f.getNumerator());
        assertEquals(Integer.MIN_VALUE / 2, f.getDenominator());
    }

    @Test(expected = MathArithmeticException.class)
    public void testGetReducedFractionMinValueDenominatorOddNumerator() {
        Fraction.getReducedFraction(1, Integer.MIN_VALUE);
    }

    @Test(expected = MathArithmeticException.class)
    public void testGetReducedFractionOverflowNumMinValue() {
        Fraction.getReducedFraction(Integer.MIN_VALUE, -1);
    }

    @Test
    public void testToStringDenominatorOne() {
        Fraction f = new Fraction(3, 1);
        assertEquals("3", f.toString());
    }

    @Test
    public void testToStringNumeratorZero() {
        Fraction f = new Fraction(0, 5);
        assertEquals("0", f.toString());
    }

    @Test
    public void testToStringNormal() {
        Fraction f = new Fraction(3, 4);
        assertEquals("3 / 4", f.toString());
    }

    @Test
    public void testGetField() {
        Fraction f = new Fraction(1, 2);
        assertNotNull(f.getField());
        assertEquals(FractionField.getInstance(), f.getField());
    }

    @Test
    public void testConstants() {
        assertEquals(2, Fraction.TWO.getNumerator());
        assertEquals(1, Fraction.TWO.getDenominator());
        assertEquals(1, Fraction.ONE.getNumerator());
        assertEquals(1, Fraction.ONE.getDenominator());
        assertEquals(0, Fraction.ZERO.getNumerator());
        assertEquals(1, Fraction.ZERO.getDenominator());
        assertEquals(4, Fraction.FOUR_FIFTHS.getNumerator());
        assertEquals(5, Fraction.FOUR_FIFTHS.getDenominator());
        assertEquals(1, Fraction.ONE_FIFTH.getNumerator());
        assertEquals(5, Fraction.ONE_FIFTH.getDenominator());
        assertEquals(1, Fraction.ONE_HALF.getNumerator());
        assertEquals(2, Fraction.ONE_HALF.getDenominator());
        assertEquals(1, Fraction.ONE_QUARTER.getNumerator());
        assertEquals(4, Fraction.ONE_QUARTER.getDenominator());
        assertEquals(1, Fraction.ONE_THIRD.getNumerator());
        assertEquals(3, Fraction.ONE_THIRD.getDenominator());
        assertEquals(3, Fraction.THREE_FIFTHS.getNumerator());
        assertEquals(5, Fraction.THREE_FIFTHS.getDenominator());
        assertEquals(3, Fraction.THREE_QUARTERS.getNumerator());
        assertEquals(4, Fraction.THREE_QUARTERS.getDenominator());
        assertEquals(2, Fraction.TWO_FIFTHS.getNumerator());
        assertEquals(5, Fraction.TWO_FIFTHS.getDenominator());
        assertEquals(1, Fraction.TWO_QUARTERS.getNumerator());
        assertEquals(2, Fraction.TWO_QUARTERS.getDenominator());
        assertEquals(2, Fraction.TWO_THIRDS.getNumerator());
        assertEquals(3, Fraction.TWO_THIRDS.getDenominator());
        assertEquals(-1, Fraction.MINUS_ONE.getNumerator());
        assertEquals(1, Fraction.MINUS_ONE.getDenominator());
    }

    @Test
    public void testAddSubOverflow() {
        Fraction f1 = new Fraction(Integer.MAX_VALUE, 1);
        Fraction f2 = new Fraction(1, 1);
        try {
            f1.add(f2);
            fail("Expected MathArithmeticException");
        } catch (MathArithmeticException e) {
        }
    }

    @Test
    public void testMultiplyOverflow() {
        Fraction f1 = new Fraction(Integer.MAX_VALUE, 1);
        Fraction f2 = new Fraction(2, 1);
        try {
            f1.multiply(f2);
            fail("Expected MathArithmeticException");
        } catch (MathArithmeticException e) {
        }
    }

    @Test
    public void testDivideOverflow() {
        Fraction f1 = new Fraction(Integer.MIN_VALUE, 1);
        Fraction f2 = new Fraction(1, Integer.MIN_VALUE);
        try {
            f1.divide(f2);
            fail("Expected MathArithmeticException");
        } catch (MathArithmeticException e) {
        }
    }

    @Test
    public void testAddSubWithGCDNotOne() {
        Fraction f1 = new Fraction(1, 6);
        Fraction f2 = new Fraction(1, 4);
        Fraction sum = f1.add(f2);
        assertEquals(5, sum.getNumerator());
        assertEquals(12, sum.getDenominator());
    }

    @Test
    public void testSubtractWithGCDNotOne() {
        Fraction f1 = new Fraction(1, 6);
        Fraction f2 = new Fraction(1, 4);
        Fraction diff = f1.subtract(f2);
        assertEquals(-1, diff.getNumerator());
        assertEquals(12, diff.getDenominator());
    }

    @Test
    public void testDoubleConstructorLargeValue() throws FractionConversionException {
        Fraction f = new Fraction(123456.789, 1.0e-5, 100);
        assertNotNull(f);
    }

    @Test(expected = FractionConversionException.class)
    public void testDoubleConstructorOverflow() throws FractionConversionException {
        new Fraction(1.0e10, 1.0e-5, 100);
    }
}
