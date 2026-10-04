package org.apache.commons.math3.fraction;

import org.junit.Test;
import static org.junit.Assert.*;

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
        Fraction f = new Fraction(1, -2);
        assertEquals(-1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    @Test
    public void testIntIntConstructorBothNegative() {
        Fraction f = new Fraction(-1, -2);
        assertEquals(1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    @Test(expected = MathArithmeticException.class)
    public void testIntIntConstructorZeroDenominator() {
        new Fraction(1, 0);
    }

    @Test(expected = MathArithmeticException.class)
    public void testIntIntConstructorOverflowNegNumMin() {
        new Fraction(Integer.MIN_VALUE, -1);
    }

    @Test(expected = MathArithmeticException.class)
    public void testIntIntConstructorOverflowNegDenMin() {
        new Fraction(1, Integer.MIN_VALUE);
    }

    @Test
    public void testIntIntConstructorReducesGcd() {
        Fraction f = new Fraction(6, 9);
        assertEquals(2, f.getNumerator());
        assertEquals(3, f.getDenominator());
    }

    @Test
    public void testIntIntConstructorNegativeNumPositiveDen() {
        Fraction f = new Fraction(-3, 4);
        assertEquals(-3, f.getNumerator());
        assertEquals(4, f.getDenominator());
    }

    @Test
    public void testDoubleConstructorBasic() {
        Fraction f = new Fraction(0.5);
        assertEquals(1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    @Test
    public void testDoubleConstructorInteger() {
        Fraction f = new Fraction(3.0);
        assertEquals(3, f.getNumerator());
        assertEquals(1, f.getDenominator());
    }

    @Test
    public void testDoubleConstructorNegative() {
        Fraction f = new Fraction(-0.75);
        assertEquals(-3, f.getNumerator());
        assertEquals(4, f.getDenominator());
    }

    @Test
    public void testDoubleConstructorWithEpsilon() {
        Fraction f = new Fraction(0.33333333, 1e-9, 100);
        assertEquals(1, f.getNumerator());
        assertEquals(3, f.getDenominator());
    }

    @Test(expected = FractionConversionException.class)
    public void testDoubleConstructorEpsilonTooSmall() {
        new Fraction(0.1, 1e-20, 10);
    }

    @Test
    public void testDoubleConstructorMaxDenominator() {
        Fraction f = new Fraction(0.5, 2);
        assertEquals(1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    @Test
    public void testDoubleConstructorMaxDenominatorExact() {
        Fraction f = new Fraction(0.5, 1);
        assertEquals(1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    @Test(expected = FractionConversionException.class)
    public void testDoubleConstructorMaxDenominatorNoMatch() {
        new Fraction(0.1, 3);
    }

    @Test
    public void testAbsPositive() {
        Fraction f = new Fraction(1, 2);
        assertSame(f, f.abs());
    }

    @Test
    public void testAbsNegative() {
        Fraction f = new Fraction(-1, 2);
        Fraction abs = f.abs();
        assertEquals(1, abs.getNumerator());
        assertEquals(2, abs.getDenominator());
    }

    @Test
    public void testCompareToEqual() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = new Fraction(2, 4);
        assertEquals(0, f1.compareTo(f2));
    }

    @Test
    public void testCompareToLessThan() {
        Fraction f1 = new Fraction(1, 3);
        Fraction f2 = new Fraction(1, 2);
        assertEquals(-1, f1.compareTo(f2));
    }

    @Test
    public void testCompareToGreaterThan() {
        Fraction f1 = new Fraction(2, 3);
        Fraction f2 = new Fraction(1, 3);
        assertEquals(1, f1.compareTo(f2));
    }

    @Test
    public void testDoubleValue() {
        Fraction f = new Fraction(3, 4);
        assertEquals(0.75, f.doubleValue(), 0.0);
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
    public void testEqualsDifferentFraction() {
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
    public void testEqualsNonFractionObject() {
        Fraction f = new Fraction(1, 2);
        assertFalse(f.equals("1/2"));
    }

    @Test
    public void testFloatValue() {
        Fraction f = new Fraction(1, 4);
        assertEquals(0.25f, f.floatValue(), 0.0f);
    }

    @Test
    public void testGetDenominator() {
        Fraction f = new Fraction(2, 5);
        assertEquals(5, f.getDenominator());
    }

    @Test
    public void testGetNumerator() {
        Fraction f = new Fraction(2, 5);
        assertEquals(2, f.getNumerator());
    }

    @Test
    public void testHashCode() {
        Fraction f = new Fraction(3, 7);
        assertEquals(37 * (37 * 17 + 3) + 7, f.hashCode());
    }

    @Test
    public void testIntValue() {
        Fraction f = new Fraction(7, 2);
        assertEquals(3, f.intValue());
    }

    @Test
    public void testLongValue() {
        Fraction f = new Fraction(5, 2);
        assertEquals(2L, f.longValue());
    }

    @Test
    public void testNegate() {
        Fraction f = new Fraction(1, 2);
        Fraction neg = f.negate();
        assertEquals(-1, neg.getNumerator());
        assertEquals(2, neg.getDenominator());
    }

    @Test(expected = MathArithmeticException.class)
    public void testNegateOverflow() {
        Fraction f = new Fraction(Integer.MIN_VALUE, 1);
        f.negate();
    }

    @Test
    public void testReciprocal() {
        Fraction f = new Fraction(2, 3);
        Fraction recip = f.reciprocal();
        assertEquals(3, recip.getNumerator());
        assertEquals(2, recip.getDenominator());
    }

    @Test(expected = MathArithmeticException.class)
    public void testReciprocalZeroNumerator() {
        Fraction.ZERO.reciprocal();
    }

    @Test
    public void testAddFraction() {
        Fraction f1 = new Fraction(1, 3);
        Fraction f2 = new Fraction(1, 6);
        Fraction result = f1.add(f2);
        assertEquals(1, result.getNumerator());
        assertEquals(2, result.getDenominator());
    }

    @Test(expected = NullArgumentException.class)
    public void testAddNull() {
        new Fraction(1, 2).add(null);
    }

    @Test
    public void testAddFractionThisZero() {
        Fraction f1 = Fraction.ZERO;
        Fraction f2 = new Fraction(1, 3);
        Fraction result = f1.add(f2);
        assertEquals(f2, result);
    }

    @Test
    public void testAddFractionOtherZero() {
        Fraction f1 = new Fraction(1, 3);
        Fraction f2 = Fraction.ZERO;
        Fraction result = f1.add(f2);
        assertEquals(f1, result);
    }

    @Test(expected = MathArithmeticException.class)
    public void testAddFractionOverflow() {
        Fraction f1 = new Fraction(Integer.MAX_VALUE, 1);
        Fraction f2 = new Fraction(1, 1);
        f1.add(f2);
    }

    @Test
    public void testAddInt() {
        Fraction f = new Fraction(1, 3);
        Fraction result = f.add(1);
        assertEquals(4, result.getNumerator());
        assertEquals(3, result.getDenominator());
    }

    @Test
    public void testAddIntZero() {
        Fraction f = new Fraction(1, 3);
        Fraction result = f.add(0);
        assertEquals(f, result);
    }

    @Test
    public void testSubtractFraction() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = new Fraction(1, 3);
        Fraction result = f1.subtract(f2);
        assertEquals(1, result.getNumerator());
        assertEquals(6, result.getDenominator());
    }

    @Test(expected = NullArgumentException.class)
    public void testSubtractNull() {
        new Fraction(1, 2).subtract(null);
    }

    @Test
    public void testSubtractFractionThisZero() {
        Fraction f1 = Fraction.ZERO;
        Fraction f2 = new Fraction(1, 3);
        Fraction result = f1.subtract(f2);
        assertEquals(-1, result.getNumerator());
        assertEquals(3, result.getDenominator());
    }

    @Test
    public void testSubtractFractionOtherZero() {
        Fraction f1 = new Fraction(1, 3);
        Fraction f2 = Fraction.ZERO;
        Fraction result = f1.subtract(f2);
        assertEquals(f1, result);
    }

    @Test(expected = MathArithmeticException.class)
    public void testSubtractFractionOverflow() {
        Fraction f1 = new Fraction(Integer.MIN_VALUE, 1);
        Fraction f2 = new Fraction(1, 1);
        f1.subtract(f2);
    }

    @Test
    public void testSubtractInt() {
        Fraction f = new Fraction(7, 3);
        Fraction result = f.subtract(2);
        assertEquals(1, result.getNumerator());
        assertEquals(3, result.getDenominator());
    }

    @Test
    public void testMultiplyFraction() {
        Fraction f1 = new Fraction(2, 3);
        Fraction f2 = new Fraction(3, 4);
        Fraction result = f1.multiply(f2);
        assertEquals(1, result.getNumerator());
        assertEquals(2, result.getDenominator());
    }

    @Test(expected = NullArgumentException.class)
    public void testMultiplyNull() {
        new Fraction(1, 2).multiply(null);
    }

    @Test
    public void testMultiplyFractionZero() {
        Fraction f1 = Fraction.ZERO;
        Fraction f2 = new Fraction(1, 3);
        Fraction result = f1.multiply(f2);
        assertEquals(Fraction.ZERO, result);
    }

    @Test(expected = MathArithmeticException.class)
    public void testMultiplyFractionOverflow() {
        Fraction f1 = new Fraction(Integer.MAX_VALUE, 2);
        Fraction f2 = new Fraction(2, 1);
        f1.multiply(f2);
    }

    @Test
    public void testMultiplyInt() {
        Fraction f = new Fraction(2, 3);
        Fraction result = f.multiply(2);
        assertEquals(4, result.getNumerator());
        assertEquals(3, result.getDenominator());
    }

    @Test
    public void testMultiplyIntZero() {
        Fraction f = new Fraction(2, 3);
        Fraction result = f.multiply(0);
        assertEquals(Fraction.ZERO, result);
    }

    @Test
    public void testDivideFraction() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = new Fraction(1, 3);
        Fraction result = f1.divide(f2);
        assertEquals(3, result.getNumerator());
        assertEquals(2, result.getDenominator());
    }

    @Test(expected = NullArgumentException.class)
    public void testDivideNull() {
        new Fraction(1, 2).divide(null);
    }

    @Test(expected = MathArithmeticException.class)
    public void testDivideByZero() {
        new Fraction(1, 2).divide(Fraction.ZERO);
    }

    @Test
    public void testDivideInt() {
        Fraction f = new Fraction(1, 2);
        Fraction result = f.divide(2);
        assertEquals(1, result.getNumerator());
        assertEquals(4, result.getDenominator());
    }

    @Test(expected = MathArithmeticException.class)
    public void testDivideIntZero() {
        new Fraction(1, 2).divide(0);
    }

    @Test
    public void testPercentageValue() {
        Fraction f = new Fraction(1, 2);
        assertEquals(50.0, f.percentageValue(), 0.0);
    }

    @Test
    public void testGetReducedFraction() {
        Fraction f = Fraction.getReducedFraction(4, 8);
        assertEquals(1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    @Test(expected = MathArithmeticException.class)
    public void testGetReducedFractionZeroDenominator() {
        Fraction.getReducedFraction(1, 0);
    }

    @Test
    public void testGetReducedFractionZeroNumerator() {
        Fraction f = Fraction.getReducedFraction(0, 5);
        assertEquals(Fraction.ZERO, f);
    }

    @Test
    public void testGetReducedFractionMinValueEven() {
        Fraction f = Fraction.getReducedFraction(2, Integer.MIN_VALUE);
        assertEquals(1, f.getNumerator());
        assertEquals(Integer.MIN_VALUE / 2, f.getDenominator());
    }

    @Test(expected = MathArithmeticException.class)
    public void testGetReducedFractionMinValueOverflow() {
        Fraction.getReducedFraction(1, Integer.MIN_VALUE);
    }

    @Test
    public void testGetReducedFractionNegativeDenominator() {
        Fraction f = Fraction.getReducedFraction(1, -2);
        assertEquals(-1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    @Test
    public void testToStringDenominatorOne() {
        Fraction f = new Fraction(5, 1);
        assertEquals("5", f.toString());
    }

    @Test
    public void testToStringZeroNumerator() {
        Fraction f = Fraction.ZERO;
        assertEquals("0", f.toString());
    }

    @Test
    public void testToStringNormal() {
        Fraction f = new Fraction(2, 3);
        assertEquals("2 / 3", f.toString());
    }

    @Test
    public void testGetField() {
        assertNotNull(Fraction.ZERO.getField());
        assertSame(FractionField.getInstance(), Fraction.ZERO.getField());
    }
}
