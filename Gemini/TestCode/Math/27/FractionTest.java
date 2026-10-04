package org.apache.commons.math3.fraction;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.junit.Assert;
import org.junit.Test;

public class FractionTest {

    private static final double EPSILON = 1e-10;

    @Test
    public void testConstants() {
        Assert.assertEquals(0, Fraction.ZERO.getNumerator());
        Assert.assertEquals(1, Fraction.ZERO.getDenominator());
        Assert.assertEquals(1, Fraction.ONE.getNumerator());
        Assert.assertEquals(1, Fraction.ONE.getDenominator());
        Assert.assertEquals(2, Fraction.TWO.getNumerator());
        Assert.assertEquals(1, Fraction.TWO.getDenominator());
        Assert.assertEquals(-1, Fraction.MINUS_ONE.getNumerator());
        Assert.assertEquals(1, Fraction.MINUS_ONE.getDenominator());
        Assert.assertEquals(1, Fraction.ONE_HALF.getNumerator());
        Assert.assertEquals(2, Fraction.ONE_HALF.getDenominator());
        Assert.assertEquals(1, Fraction.ONE_THIRD.getNumerator());
        Assert.assertEquals(3, Fraction.ONE_THIRD.getDenominator());
        Assert.assertEquals(2, Fraction.TWO_THIRDS.getNumerator());
        Assert.assertEquals(3, Fraction.TWO_THIRDS.getDenominator());
        Assert.assertEquals(1, Fraction.ONE_QUARTER.getNumerator());
        Assert.assertEquals(4, Fraction.ONE_QUARTER.getDenominator());
        Assert.assertEquals(1, Fraction.TWO_QUARTERS.getNumerator());
        Assert.assertEquals(2, Fraction.TWO_QUARTERS.getDenominator());
        Assert.assertEquals(3, Fraction.THREE_QUARTERS.getNumerator());
        Assert.assertEquals(4, Fraction.THREE_QUARTERS.getDenominator());
        Assert.assertEquals(1, Fraction.ONE_FIFTH.getNumerator());
        Assert.assertEquals(5, Fraction.ONE_FIFTH.getDenominator());
        Assert.assertEquals(2, Fraction.TWO_FIFTHS.getNumerator());
        Assert.assertEquals(5, Fraction.TWO_FIFTHS.getDenominator());
        Assert.assertEquals(3, Fraction.THREE_FIFTHS.getNumerator());
        Assert.assertEquals(5, Fraction.THREE_FIFTHS.getDenominator());
        Assert.assertEquals(4, Fraction.FOUR_FIFTHS.getNumerator());
        Assert.assertEquals(5, Fraction.FOUR_FIFTHS.getDenominator());
    }

    @Test
    public void testConstructorInt() {
        Fraction f = new Fraction(5);
        Assert.assertEquals(5, f.getNumerator());
        Assert.assertEquals(1, f.getDenominator());

        f = new Fraction(-3);
        Assert.assertEquals(-3, f.getNumerator());
        Assert.assertEquals(1, f.getDenominator());

        f = new Fraction(0);
        Assert.assertEquals(0, f.getNumerator());
        Assert.assertEquals(1, f.getDenominator());
    }

    @Test
    public void testConstructorIntInt() {
        Fraction f = new Fraction(2, 4);
        Assert.assertEquals(1, f.getNumerator());
        Assert.assertEquals(2, f.getDenominator());

        f = new Fraction(2, -4);
        Assert.assertEquals(-1, f.getNumerator());
        Assert.assertEquals(2, f.getDenominator());

        f = new Fraction(-2, 4);
        Assert.assertEquals(-1, f.getNumerator());
        Assert.assertEquals(2, f.getDenominator());

        f = new Fraction(-2, -4);
        Assert.assertEquals(1, f.getNumerator());
        Assert.assertEquals(2, f.getDenominator());

        f = new Fraction(0, 5);
        Assert.assertEquals(0, f.getNumerator());
        Assert.assertEquals(1, f.getDenominator());
    }

    @Test(expected = MathArithmeticException.class)
    public void testConstructorIntIntZeroDenominator() {
        new Fraction(1, 0);
    }

    @Test(expected = MathArithmeticException.class)
    public void testConstructorIntIntOverflowDenominator() {
        new Fraction(1, Integer.MIN_VALUE);
    }

    @Test(expected = MathArithmeticException.class)
    public void testConstructorIntIntOverflowNumerator() {
        new Fraction(Integer.MIN_VALUE, -1);
    }

    @Test
    public void testConstructorDouble() {
        Fraction f = new Fraction(0.5);
        Assert.assertEquals(1, f.getNumerator());
        Assert.assertEquals(2, f.getDenominator());

        f = new Fraction(-0.75);
        Assert.assertEquals(-3, f.getNumerator());
        Assert.assertEquals(4, f.getDenominator());

        f = new Fraction(0.0);
        Assert.assertEquals(0, f.getNumerator());
        Assert.assertEquals(1, f.getDenominator());

        f = new Fraction(3.0);
        Assert.assertEquals(3, f.getNumerator());
        Assert.assertEquals(1, f.getDenominator());
    }

    @Test
    public void testConstructorDoubleEpsilonMaxIterations() {
        Fraction f = new Fraction(0.3333333333333333, 1.0e-5, 10);
        Assert.assertEquals(1, f.getNumerator());
        Assert.assertEquals(3, f.getDenominator());

        Fraction fInt = new Fraction(5.00000000001, 1.0e-5, 10);
        Assert.assertEquals(5, fInt.getNumerator());
        Assert.assertEquals(1, fInt.getDenominator());
    }

    @Test(expected = FractionConversionException.class)
    public void testConstructorDoubleOverflowA0() {
        new Fraction(1.0e20, 1.0e-5, 100);
    }

    @Test(expected = FractionConversionException.class)
    public void testConstructorDoubleMaxIterationsExceeded() {
        new Fraction(Math.PI, 1.0e-15, 2);
    }

    @Test(expected = FractionConversionException.class)
    public void testConstructorDoubleOverflowPQ() {
        new Fraction(2147483647.5, 1.0e-10, 100);
    }

    @Test
    public void testConstructorDoubleMaxDenominator() {
        Fraction f = new Fraction(0.333333, 10);
        Assert.assertEquals(1, f.getNumerator());
        Assert.assertEquals(3, f.getDenominator());

        Fraction f2 = new Fraction(0.333333, 100);
        Assert.assertTrue(f2.getDenominator() <= 100);

        Fraction f3 = new Fraction(0.8571428571, 7);
        Assert.assertEquals(6, f3.getNumerator());
        Assert.assertEquals(7, f3.getDenominator());
    }

    @Test
    public void testAbs() {
        Fraction f1 = new Fraction(-2, 3);
        Fraction f2 = new Fraction(2, 3);
        Fraction f3 = new Fraction(0);

        Assert.assertEquals(new Fraction(2, 3), f1.abs());
        Assert.assertSame(f2, f2.abs());
        Assert.assertSame(f3, f3.abs());
    }

    @Test
    public void testCompareTo() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = new Fraction(2, 4);
        Fraction f3 = new Fraction(1, 3);
        Fraction f4 = new Fraction(2, 3);

        Assert.assertEquals(0, f1.compareTo(f2));
        Assert.assertTrue(f1.compareTo(f3) > 0);
        Assert.assertTrue(f3.compareTo(f1) < 0);
        Assert.assertTrue(f1.compareTo(f4) < 0);
        Assert.assertTrue(f4.compareTo(f1) > 0);
    }

    @Test
    public void testConversions() {
        Fraction f = new Fraction(3, 2);

        Assert.assertEquals(1.5, f.doubleValue(), EPSILON);
        Assert.assertEquals(1.5f, f.floatValue(), EPSILON);
        Assert.assertEquals(1, f.intValue());
        Assert.assertEquals(1L, f.longValue());

        Fraction fNeg = new Fraction(-5, 2);
        Assert.assertEquals(-2.5, fNeg.doubleValue(), EPSILON);
        Assert.assertEquals(-2.5f, fNeg.floatValue(), EPSILON);
        Assert.assertEquals(-2, fNeg.intValue());
        Assert.assertEquals(-2L, fNeg.longValue());
    }

    @Test
    public void testEqualsAndHashCode() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = new Fraction(2, 4);
        Fraction f3 = new Fraction(1, 3);
        Fraction f4 = new Fraction(2, 3);

        Assert.assertTrue(f1.equals(f1));
        Assert.assertTrue(f1.equals(f2));
        Assert.assertEquals(f1.hashCode(), f2.hashCode());

        Assert.assertFalse(f1.equals(null));
        Assert.assertFalse(f1.equals("1/2"));
        Assert.assertFalse(f1.equals(f3));
        Assert.assertFalse(f1.equals(f4));

        Fraction zero1 = new Fraction(0, 1);
        Fraction zero2 = new Fraction(0, 2);
        Assert.assertTrue(zero1.equals(zero2));
        Assert.assertEquals(zero1.hashCode(), zero2.hashCode());
    }

    @Test
    public void testNegate() {
        Fraction f = new Fraction(2, 3);
        Fraction negated = f.negate();
        Assert.assertEquals(-2, negated.getNumerator());
        Assert.assertEquals(3, negated.getDenominator());

        Fraction fNeg = new Fraction(-2, 3);
        Assert.assertEquals(f, fNeg.negate());

        Fraction zero = new Fraction(0);
        Assert.assertEquals(0, zero.negate().getNumerator());
        Assert.assertEquals(1, zero.negate().getDenominator());
    }

    @Test(expected = MathArithmeticException.class)
    public void testNegateOverflow() {
        Fraction f = new Fraction(Integer.MIN_VALUE, 1);
        f.negate();
    }

    @Test
    public void testReciprocal() {
        Fraction f = new Fraction(2, 3);
        Fraction r = f.reciprocal();
        Assert.assertEquals(3, r.getNumerator());
        Assert.assertEquals(2, r.getDenominator());

        Fraction fNeg = new Fraction(-2, 3);
        Fraction rNeg = fNeg.reciprocal();
        Assert.assertEquals(-3, rNeg.getNumerator());
        Assert.assertEquals(2, rNeg.getDenominator());
    }

    @Test(expected = MathArithmeticException.class)
    public void testReciprocalZero() {
        Fraction zero = new Fraction(0);
        zero.reciprocal();
    }

    @Test
    public void testAdd() {
        Fraction f1 = new Fraction(1, 3);
        Fraction f2 = new Fraction(1, 6);
        Fraction sum = f1.add(f2);
        Assert.assertEquals(1, sum.getNumerator());
        Assert.assertEquals(2, sum.getDenominator());

        Fraction zero = Fraction.ZERO;
        Assert.assertEquals(f1, f1.add(zero));
        Assert.assertEquals(f1, zero.add(f1));

        Fraction f3 = new Fraction(1, 2);
        Fraction f4 = new Fraction(1, 3);
        Fraction sumCoprimeDenom = f3.add(f4);
        Assert.assertEquals(5, sumCoprimeDenom.getNumerator());
        Assert.assertEquals(6, sumCoprimeDenom.getDenominator());

        Fraction f5 = new Fraction(1, 6);
        Fraction f6 = new Fraction(1, 4);
        Fraction sumGcdDenom = f5.add(f6);
        Assert.assertEquals(5, sumGcdDenom.getNumerator());
        Assert.assertEquals(12, sumGcdDenom.getDenominator());
    }

    @Test(expected = NullArgumentException.class)
    public void testAddNull() {
        Fraction f = new Fraction(1, 2);
        f.add(null);
    }

    @Test
    public void testAddInt() {
        Fraction f = new Fraction(1, 3);
        Fraction res = f.add(2);
        Assert.assertEquals(7, res.getNumerator());
        Assert.assertEquals(3, res.getDenominator());

        res = f.add(-1);
        Assert.assertEquals(-2, res.getNumerator());
        Assert.assertEquals(3, res.getDenominator());
    }

    @Test
    public void testSubtract() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = new Fraction(1, 3);
        Fraction diff = f1.subtract(f2);
        Assert.assertEquals(1, diff.getNumerator());
        Assert.assertEquals(6, diff.getDenominator());

        Fraction zero = Fraction.ZERO;
        Assert.assertEquals(f1, f1.subtract(zero));
        Assert.assertEquals(f1.negate(), zero.subtract(f1));

        Fraction f3 = new Fraction(1, 4);
        Fraction f4 = new Fraction(1, 6);
        Fraction diffGcd = f3.subtract(f4);
        Assert.assertEquals(1, diffGcd.getNumerator());
        Assert.assertEquals(12, diffGcd.getDenominator());
    }

    @Test(expected = NullArgumentException.class)
    public void testSubtractNull() {
        Fraction f = new Fraction(1, 2);
        f.subtract(null);
    }

    @Test
    public void testSubtractInt() {
        Fraction f = new Fraction(7, 3);
        Fraction res = f.subtract(2);
        Assert.assertEquals(1, res.getNumerator());
        Assert.assertEquals(3, res.getDenominator());
    }

    @Test(expected = MathArithmeticException.class)
    public void testAddSubOverflowKnuth() {
        Fraction f1 = new Fraction(Integer.MAX_VALUE - 1, 1);
        Fraction f2 = new Fraction(2, 1);
        f1.add(f2);
    }

    @Test(expected = MathArithmeticException.class)
    public void testAddSubOverflowNumerator() {
        Fraction f1 = new Fraction(Integer.MAX_VALUE, 2);
        Fraction f2 = new Fraction(Integer.MAX_VALUE, 2);
        f1.add(f2);
    }

    @Test
    public void testMultiply() {
        Fraction f1 = new Fraction(2, 3);
        Fraction f2 = new Fraction(3, 4);
        Fraction prod = f1.multiply(f2);
        Assert.assertEquals(1, prod.getNumerator());
        Assert.assertEquals(2, prod.getDenominator());

        Assert.assertEquals(Fraction.ZERO, f1.multiply(Fraction.ZERO));
        Assert.assertEquals(Fraction.ZERO, Fraction.ZERO.multiply(f1));

        Fraction f3 = new Fraction(-1, 2);
        Fraction f4 = new Fraction(2, 3);
        Assert.assertEquals(new Fraction(-1, 3), f3.multiply(f4));
    }

    @Test(expected = NullArgumentException.class)
    public void testMultiplyNull() {
        Fraction f = new Fraction(1, 2);
        f.multiply(null);
    }

    @Test
    public void testMultiplyInt() {
        Fraction f = new Fraction(2, 3);
        Fraction res = f.multiply(3);
        Assert.assertEquals(2, res.getNumerator());
        Assert.assertEquals(1, res.getDenominator());

        res = f.multiply(0);
        Assert.assertEquals(0, res.getNumerator());
        Assert.assertEquals(1, res.getDenominator());
    }

    @Test(expected = MathArithmeticException.class)
    public void testMultiplyOverflow() {
        Fraction f1 = new Fraction(Integer.MAX_VALUE, 2);
        Fraction f2 = new Fraction(2, 1);
        f1.multiply(f2);
    }

    @Test
    public void testDivide() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = new Fraction(1, 4);
        Fraction quot = f1.divide(f2);
        Assert.assertEquals(2, quot.getNumerator());
        Assert.assertEquals(1, quot.getDenominator());

        Fraction zero = Fraction.ZERO;
        Assert.assertEquals(Fraction.ZERO, zero.divide(f1));
    }

    @Test(expected = NullArgumentException.class)
    public void testDivideNull() {
        Fraction f = new Fraction(1, 2);
        f.divide(null);
    }

    @Test(expected = MathArithmeticException.class)
    public void testDivideByZero() {
        Fraction f = new Fraction(1, 2);
        f.divide(Fraction.ZERO);
    }

    @Test
    public void testDivideInt() {
        Fraction f = new Fraction(2, 3);
        Fraction res = f.divide(2);
        Assert.assertEquals(1, res.getNumerator());
        Assert.assertEquals(3, res.getDenominator());
    }

    @Test(expected = MathArithmeticException.class)
    public void testDivideIntByZero() {
        Fraction f = new Fraction(2, 3);
        f.divide(0);
    }

    @Test
    public void testPercentageValue() {
        Fraction f = new Fraction(1, 4);
        Assert.assertEquals(25.0, f.percentageValue(), EPSILON);

        Fraction f2 = new Fraction(2, 3);
        Assert.assertEquals(66.66666666666667, f2.percentageValue(), EPSILON);
    }

    @Test
    public void testGetReducedFraction() {
        Fraction f = Fraction.getReducedFraction(0, 5);
        Assert.assertEquals(Fraction.ZERO, f);

        f = Fraction.getReducedFraction(2, 4);
        Assert.assertEquals(1, f.getNumerator());
        Assert.assertEquals(2, f.getDenominator());

        f = Fraction.getReducedFraction(2, -4);
        Assert.assertEquals(-1, f.getNumerator());
        Assert.assertEquals(2, f.getDenominator());

        f = Fraction.getReducedFraction(-2, -4);
        Assert.assertEquals(1, f.getNumerator());
        Assert.assertEquals(2, f.getDenominator());

        f = Fraction.getReducedFraction(4, Integer.MIN_VALUE);
        Assert.assertEquals(-1, f.getNumerator());
        Assert.assertEquals(Integer.MIN_VALUE / -4, f.getDenominator());
    }

    @Test(expected = MathArithmeticException.class)
    public void testGetReducedFractionZeroDenom() {
        Fraction.getReducedFraction(1, 0);
    }

    @Test(expected = MathArithmeticException.class)
    public void testGetReducedFractionOverflowDenom() {
        Fraction.getReducedFraction(1, Integer.MIN_VALUE);
    }

    @Test(expected = MathArithmeticException.class)
    public void testGetReducedFractionOverflowNum() {
        Fraction.getReducedFraction(Integer.MIN_VALUE, -1);
    }

    @Test
    public void testToString() {
        Assert.assertEquals("0", new Fraction(0, 5).toString());
        Assert.assertEquals("2", new Fraction(2, 1).toString());
        Assert.assertEquals("-3", new Fraction(-3, 1).toString());
        Assert.assertEquals("2 / 3", new Fraction(2, 3).toString());
        Assert.assertEquals("-1 / 2", new Fraction(-1, 2).toString());
    }

    @Test
    public void testGetField() {
        Fraction f = new Fraction(1, 2);
        FractionField field = f.getField();
        Assert.assertNotNull(field);
        Assert.assertEquals(FractionField.getInstance(), field);
        Assert.assertEquals(Fraction.ONE, field.getOne());
        Assert.assertEquals(Fraction.ZERO, field.getZero());
    }
}
