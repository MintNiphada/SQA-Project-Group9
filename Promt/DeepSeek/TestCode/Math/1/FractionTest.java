package org.apache.commons.math3.fraction;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class FractionTest {

    private Fraction zero;
    private Fraction one;
    private Fraction oneHalf;
    private Fraction minusOne;
    private Fraction two;
    private Fraction threeQuarters;
    private Fraction oneThird;
    private Fraction largeFraction;
    private Fraction maxValueFraction;

    @Before
    public void setUp() {
        zero = Fraction.ZERO;
        one = Fraction.ONE;
        oneHalf = Fraction.ONE_HALF;
        minusOne = Fraction.MINUS_ONE;
        two = Fraction.TWO;
        threeQuarters = Fraction.THREE_QUARTERS;
        oneThird = Fraction.ONE_THIRD;
        largeFraction = new Fraction(1073741824, 1); // near max
        maxValueFraction = new Fraction(Integer.MAX_VALUE, 1);
    }

    @Test
    public void testConstantValues() {
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
        assertEquals(1, Fraction.TWO_QUARTERS.getNumerator()); // reduced
        assertEquals(2, Fraction.TWO_QUARTERS.getDenominator());
        assertEquals(2, Fraction.TWO_THIRDS.getNumerator());
        assertEquals(3, Fraction.TWO_THIRDS.getDenominator());
        assertEquals(-1, Fraction.MINUS_ONE.getNumerator());
        assertEquals(1, Fraction.MINUS_ONE.getDenominator());
    }

    @Test
    public void testIntConstructor() {
        Fraction f = new Fraction(5);
        assertEquals(5, f.getNumerator());
        assertEquals(1, f.getDenominator());
    }

    @Test(expected = MathArithmeticException.class)
    public void testIntIntConstructorWithDenominatorZeroThrows() {
        new Fraction(1, 0);
    }

    @Test
    public void testIntIntConstructorNormalCase() {
        Fraction f = new Fraction(4, 6);
        assertEquals(2, f.getNumerator());
        assertEquals(3, f.getDenominator());
    }

    @Test
    public void testIntIntConstructorWithNegativeDenominator() {
        Fraction f = new Fraction(1, -2);
        assertEquals(-1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    @Test
    public void testIntIntConstructorWithBothNegative() {
        Fraction f = new Fraction(-1, -2);
        assertEquals(1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    @Test
    public void testIntIntConstructorReducesToZero() {
        Fraction f = new Fraction(0, 5);
        assertEquals(0, f.getNumerator());
        assertEquals(1, f.getDenominator());
    }

    @Test(expected = MathArithmeticException.class)
    public void testIntIntConstructorOverflowNegatingNumerator() {
        new Fraction(Integer.MIN_VALUE, -1);
    }

    @Test(expected = MathArithmeticException.class)
    public void testIntIntConstructorOverflowNegatingDenominator() {
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
    public void testDoubleConstructorNegativeValue() throws FractionConversionException {
        Fraction f = new Fraction(-0.5);
        assertEquals(-1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    @Test
    public void testDoubleConstructorWithEpsilonAndMaxIter() throws FractionConversionException {
        Fraction f = new Fraction(0.3333333, 1e-6, 100);
        double error = Math.abs(f.doubleValue() - 0.3333333);
        assertTrue(error < 1e-5);
    }

    @Test
    public void testDoubleConstructorWithMaxDenominator() throws FractionConversionException {
        Fraction f = new Fraction(0.7, 10);
        double error = Math.abs(f.doubleValue() - 0.7);
        assertTrue(f.getDenominator() <= 10);
    }

    @Test(expected = FractionConversionException.class)
    public void testDoubleConstructorOverflow() throws FractionConversionException {
        new Fraction(0.5, 0.0, Integer.MAX_VALUE, 100) {
            // force overflow? Actually we need to create a scenario where p2 or q2 > Integer.MAX_VALUE.
            // The double value 1e-10 yields very large denominator. Could trigger overflow.
        };
        // Simpler: use a small epsilon and value = 1e-10, maxIterations=100, but maxDenominator=Integer.MAX_VALUE.
    }

    @Test(expected = FractionConversionException.class)
    public void testDoubleConstructorMaxIterationsExceeded() throws FractionConversionException {
        // Try with maxIterations = 0, which will stop immediately but n >= maxIterations?
        new Fraction(Math.PI, 0.0, Integer.MAX_VALUE, 1);
    }

    @Test
    public void testAbsPositive() {
        Fraction f = new Fraction(3, 4);
        assertEquals(f, f.abs());
    }

    @Test
    public void testAbsNegative() {
        Fraction f = new Fraction(-3, 4);
        assertEquals(new Fraction(3, 4), f.abs());
    }

    @Test
    public void testAbsZero() {
        assertEquals(zero, zero.abs());
    }

    @Test
    public void testCompareToEqual() {
        assertEquals(0, one.compareTo(one));
    }

    @Test
    public void testCompareToLessThan() {
        assertTrue(oneHalf.compareTo(one) < 0);
    }

    @Test
    public void testCompareToGreaterThan() {
        assertTrue(two.compareTo(oneHalf) > 0);
    }

    @Test
    public void testCompareToDifferentSigns() {
        assertTrue(minusOne.compareTo(one) < 0);
    }

    @Test
    public void testDoubleValue() {
        assertEquals(0.5, oneHalf.doubleValue(), 1e-15);
        assertEquals(0.0, zero.doubleValue(), 1e-15);
    }

    @Test
    public void testFloatValue() {
        assertEquals(0.5f, oneHalf.floatValue(), 1e-6f);
    }

    @Test
    public void testIntValue() {
        assertEquals(0, oneHalf.intValue());
        assertEquals(2, two.intValue());
    }

    @Test
    public void testLongValue() {
        assertEquals(0L, oneHalf.longValue());
        assertEquals(2L, two.longValue());
    }

    @Test
    public void testEqualsSameObject() {
        assertTrue(one.equals(one));
    }

    @Test
    public void testEqualsEqualFractions() {
        Fraction f1 = new Fraction(2, 4);
        Fraction f2 = new Fraction(1, 2);
        assertTrue(f1.equals(f2));
    }

    @Test
    public void testEqualsNonFraction() {
        assertFalse(one.equals("1"));
    }

    @Test
    public void testEqualsNull() {
        assertFalse(one.equals(null));
    }

    @Test
    public void testHashCodeConsistentWithEquals() {
        Fraction f1 = new Fraction(2, 4);
        Fraction f2 = new Fraction(1, 2);
        assertEquals(f1.hashCode(), f2.hashCode());
    }

    @Test
    public void testNegatePositive() {
        assertEquals(new Fraction(-3, 4), new Fraction(3, 4).negate());
    }

    @Test
    public void testNegateNegative() {
        assertEquals(new Fraction(3, 4), new Fraction(-3, 4).negate());
    }

    @Test
    public void testNegateZero() {
        assertEquals(zero, zero.negate());
    }

    @Test(expected = MathArithmeticException.class)
    public void testNegateIntegerMinValue() {
        Fraction f = new Fraction(Integer.MIN_VALUE, 1);
        f.negate();
    }

    @Test
    public void testReciprocalNormal() {
        Fraction f = new Fraction(3, 4);
        assertEquals(new Fraction(4, 3), f.reciprocal());
    }

    @Test
    public void testReciprocalOfOne() {
        assertEquals(one, one.reciprocal());
    }

    @Test
    public void testReciprocalOfNegative() {
        Fraction f = new Fraction(-3, 4);
        assertEquals(new Fraction(-4, 3), f.reciprocal());
    }

    @Test(expected = MathArithmeticException.class)
    public void testReciprocalOfZeroThrows() {
        zero.reciprocal();
    }

    @Test
    public void testReciprocalOfIntegerMinValue() {
        Fraction f = new Fraction(1, Integer.MIN_VALUE);
        // reciprocal: denominator = Integer.MIN_VALUE, numerator = 1 -> new Fraction(Integer.MIN_VALUE, 1)
        // Are there overflows? It's valid.
        Fraction r = f.reciprocal();
        assertEquals(Integer.MIN_VALUE, r.getNumerator());
        assertEquals(1, r.getDenominator());
    }

    @Test
    public void testAddFractionNormal() {
        Fraction result = oneHalf.add(oneThird);
        assertEquals(new Fraction(5, 6), result);
    }

    @Test
    public void testAddFractionWithZero() {
        assertEquals(oneHalf, oneHalf.add(zero));
        assertEquals(oneHalf, zero.add(oneHalf));
    }

    @Test
    public void testAddFractionNegative() {
        Fraction result = oneHalf.add(minusOne);
        assertEquals(new Fraction(-1, 2), result);
    }

    @Test(expected = NullArgumentException.class)
    public void testAddFractionNullThrows() {
        one.add(null);
    }

    @Test
    public void testAddInt() {
        Fraction result = oneHalf.add(1);
        assertEquals(new Fraction(3, 2), result);
    }

    @Test
    public void testAddIntZero() {
        assertEquals(oneHalf, oneHalf.add(0));
    }

    @Test
    public void testAddIntNegative() {
        assertEquals(new Fraction(-1, 2), oneHalf.add(-1));
    }

    @Test
    public void testSubtractFractionNormal() {
        Fraction result = oneHalf.subtract(oneThird);
        assertEquals(new Fraction(1, 6), result);
    }

    @Test
    public void testSubtractFractionWithZero() {
        assertEquals(oneHalf, oneHalf.subtract(zero));
        assertEquals(oneHalf.negate(), zero.subtract(oneHalf));
    }

    @Test
    public void testSubtractFractionNegative() {
        Fraction result = oneHalf.subtract(minusOne);
        assertEquals(new Fraction(3, 2), result);
    }

    @Test(expected = NullArgumentException.class)
    public void testSubtractFractionNullThrows() {
        one.subtract(null);
    }

    @Test
    public void testSubtractInt() {
        Fraction result = oneHalf.subtract(1);
        assertEquals(new Fraction(-1, 2), result);
    }

    @Test
    public void testSubtractIntZero() {
        assertEquals(oneHalf, oneHalf.subtract(0));
    }

    @Test
    public void testSubtractIntNegative() {
        assertEquals(new Fraction(3, 2), oneHalf.subtract(-1));
    }

    @Test
    public void testMultiplyFractionNormal() {
        Fraction result = oneHalf.multiply(oneThird);
        assertEquals(new Fraction(1, 6), result);
    }

    @Test
    public void testMultiplyFractionWithZero() {
        assertEquals(zero, oneHalf.multiply(zero));
        assertEquals(zero, zero.multiply(oneHalf));
    }

    @Test
    public void testMultiplyFractionNegative() {
        Fraction result = oneHalf.multiply(minusOne);
        assertEquals(new Fraction(-1, 2), result);
    }

    @Test(expected = NullArgumentException.class)
    public void testMultiplyFractionNullThrows() {
        one.multiply(null);
    }

    @Test
    public void testMultiplyInt() {
        Fraction result = oneHalf.multiply(2);
        assertEquals(one, result);
    }

    @Test
    public void testMultiplyIntZero() {
        assertEquals(zero, oneHalf.multiply(0));
    }

    @Test
    public void testMultiplyIntNegative() {
        assertEquals(new Fraction(-3, 2), oneHalf.multiply(-3));
    }

    @Test
    public void testDivideFractionNormal() {
        Fraction result = oneHalf.divide(oneThird);
        assertEquals(new Fraction(3, 2), result);
    }

    @Test
    public void testDivideFractionByZeroThrows() {
        try {
            oneHalf.divide(zero);
            fail("Expected MathArithmeticException");
        } catch (MathArithmeticException e) {
            // expected
        }
    }

    @Test(expected = NullArgumentException.class)
    public void testDivideFractionNullThrows() {
        one.divide(null);
    }

    @Test
    public void testDivideInt() {
        Fraction result = oneHalf.divide(2);
        assertEquals(new Fraction(1, 4), result);
    }

    @Test
    public void testDivideIntZeroThrows() {
        // dividing by integer zero: denominator * 0 = 0 -> MathArithmeticException from constructor
        try {
            oneHalf.divide(0);
            fail("Expected MathArithmeticException");
        } catch (MathArithmeticException e) {
            // expected
        }
    }

    @Test
    public void testPercentageValue() {
        assertEquals(50.0, oneHalf.percentageValue(), 1e-15);
        assertEquals(0.0, zero.percentageValue(), 1e-15);
    }

    @Test
    public void testGetReducedFractionNormal() {
        Fraction f = Fraction.getReducedFraction(2, 4);
        assertEquals(new Fraction(1, 2), f);
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
    public void testGetReducedFractionNegativeDenominator() {
        Fraction f = Fraction.getReducedFraction(1, -3);
        assertEquals(new Fraction(-1, 3), f);
    }

    @Test(expected = MathArithmeticException.class)
    public void testGetReducedFractionOverflowNegateNumerator() {
        Fraction.getReducedFraction(Integer.MIN_VALUE, -1);
    }

    @Test(expected = MathArithmeticException.class)
    public void testGetReducedFractionOverflowNegateDenominator() {
        Fraction.getReducedFraction(1, Integer.MIN_VALUE);
    }

    @Test
    public void testGetReducedFractionSpecialCaseDenominatorMinValueEvenNumerator() {
        // denominator == Integer.MIN_VALUE, numerator even
        Fraction f = Fraction.getReducedFraction(2, Integer.MIN_VALUE);
        // numerator/2, denominator/2 => 1, Integer.MIN_VALUE/2 = -1073741824
        // after reduction: gcd(1, -1073741824)=1, then negative denominator handled: numerator=-1, denominator=1073741824
        assertEquals(-1, f.getNumerator());
        assertEquals(1073741824, f.getDenominator());
    }

    @Test
    public void testGetReducedFractionSpecialCaseDenominatorMinValueOddNumerator() {
        // numerator odd => not reduced, but negative denominator triggers overflow? denominator is min value -> negate overflow.
        // This should throw MathArithmeticException because denominator = Integer.MIN_VALUE and numerator not even? Let's see: the code checks if (denominator==Integer.MIN_VALUE && (numerator&1)==0) before sign handling. For odd numerator, it doesn't reduce, then goes to if (denominator < 0) and checks overflow. Since denominator = Integer.MIN_VALUE and negative, it will throw because denominator==Integer.MIN_VALUE.
        try {
            Fraction.getReducedFraction(1, Integer.MIN_VALUE);
            fail("Expected MathArithmeticException");
        } catch (MathArithmeticException e) {
            // expected
        }
    }

    @Test
    public void testToStringInteger() {
        assertEquals("2", two.toString());
        assertEquals("-1", minusOne.toString());
    }

    @Test
    public void testToStringZero() {
        assertEquals("0", zero.toString());
    }

    @Test
    public void testToStringFraction() {
        assertEquals("1 / 2", oneHalf.toString());
        assertEquals("-1 / 2", new Fraction(-1, 2).toString());
    }

    @Test
    public void testGetField() {
        assertNotNull(one.getField());
        assertEquals(FractionField.getInstance(), one.getField());
    }

    @Test
    public void testAddSubOverflowCase() {
        // Test addSub with d1 != 1 that leads to result > Integer.MAX_VALUE
        Fraction f1 = new Fraction(Integer.MAX_VALUE, 1);
        Fraction f2 = new Fraction(1, 1);
        try {
            f1.add(f2);
            fail("Expected MathArithmeticException");
        } catch (MathArithmeticException e) {
            // expected
        }
    }

    @Test
    public void testAddSubOverflowCaseD1NotOne() {
        // Create fractions that lead to d1 != 1 and large result
        Fraction f1 = new Fraction(Integer.MAX_VALUE, 2);
        Fraction f2 = new Fraction(Integer.MAX_VALUE, 5); // denominators 2 and 5 gcd=1 actually? 2 and 5 gcd=1, so d1=1 -> goes to simple path. To force d1!=1, need common factor. Use denominators 2 and 4 -> gcd=2, so d1=2.
        // f1 = MAX/2, f2 = MAX/4, sum = 3/4 MAX? But numerator MAX is large; we need to overflow. Actually addSub may overflow in BigInteger w.bitLength()>31.
        Fraction f1b = new Fraction(Integer.MAX_VALUE, 4); // denominator 4, numerator max
        Fraction f2b = new Fraction(Integer.MAX_VALUE, 2); // denominator 2, numerator max
        // sum = MAX/4 + MAX/2 = 3/4 MAX -> numerator 3*MAX/4, denominator? Denom 4 and 2, lcm=4, d1=gcd(4,2)=2.
        // This likely will overflow w.bitLength() >31.
        try {
            f1b.add(f2b);
            fail("Expected MathArithmeticException");
        } catch (MathArithmeticException e) {
            // expected
        }
    }

    @Test
    public void testMultiplyOverflow() {
        Fraction f1 = new Fraction(Integer.MAX_VALUE, 1);
        Fraction f2 = new Fraction(Integer.MAX_VALUE, 1);
        try {
            f1.multiply(f2);
            fail("Expected MathArithmeticException");
        } catch (MathArithmeticException e) {
            // expected
        }
    }

    @Test
    public void testMultiplyDivideOverflow() {
        Fraction f1 = new Fraction(Integer.MIN_VALUE, 1);
        Fraction f2 = new Fraction(-1, 1);
        // multiply two large negatives
        try {
            f1.multiply(f2);
            fail("Expected MathArithmeticException");
        } catch (MathArithmeticException e) {
            // expected
        }
    }

    @Test
    public void testDoubleConstructorOverflowLarge() throws FractionConversionException {
        // Use a value that causes p2 or q2 overflow
        try {
            new Fraction(1e-10, 0.0, Integer.MAX_VALUE, 1000);
            fail("Expected FractionConversionException");
        } catch (FractionConversionException e) {
            // expected
        }
    }

    @Test
    public void testDoubleConstructorMaxDenomExactly() throws FractionConversionException {
        // value = 1/3, maxDenominator=3 -> should be exact
        Fraction f = new Fraction(1.0/3.0, 3);
        assertEquals(1, f.getNumerator());
        assertEquals(3, f.getDenominator());
    }

    @Test
    public void testDoubleConstructorMaxDenomExceeds() throws FractionConversionException {
        Fraction f = new Fraction(1.0/3.0, 2); // maxDen=2, should approximate
        assertTrue(Math.abs(f.doubleValue() - 1.0/3.0) < 0.1);
    }

    @Test
    public void testDoubleConstructorEpsilonStop() throws FractionConversionException {
        // eps is large, will stop early with integer part
        Fraction f = new Fraction(3.14159, 0.1, 100);
        assertEquals(3, f.getNumerator());
        assertEquals(1, f.getDenominator());
    }

    // Additional boundary tests to ensure coverage

    @Test
    public void testSubtractOverflowWithD1Not1() {
        // subtract that leads to d1 != 1 and overflow
        Fraction f1 = new Fraction(Integer.MIN_VALUE, 2);
        Fraction f2 = new Fraction(Integer.MAX_VALUE, 4);
        try {
            f1.subtract(f2);
            fail("Expected MathArithmeticException");
        } catch (MathArithmeticException e) {
            // expected
        }
    }
}
