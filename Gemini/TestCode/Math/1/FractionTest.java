package org.apache.commons.math3.fraction;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.util.FastMath;
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

        Assert.assertEquals(2, Fraction.TWO_QUARTERS.getNumerator());
        Assert.assertEquals(4, Fraction.TWO_QUARTERS.getDenominator());

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
        Fraction f1 = new Fraction(0);
        Assert.assertEquals(0, f1.getNumerator());
        Assert.assertEquals(1, f1.getDenominator());

        Fraction f2 = new Fraction(42);
        Assert.assertEquals(42, f2.getNumerator());
        Assert.assertEquals(1, f2.getDenominator());

        Fraction f3 = new Fraction(-7);
        Assert.assertEquals(-7, f3.getNumerator());
        Assert.assertEquals(1, f3.getDenominator());
    }

    @Test
    public void testConstructorIntInt() {
        Fraction f1 = new Fraction(6, 8);
        Assert.assertEquals(3, f1.getNumerator());
        Assert.assertEquals(4, f1.getDenominator());

        Fraction f2 = new Fraction(-6, 8);
        Assert.assertEquals(-3, f2.getNumerator());
        Assert.assertEquals(4, f2.getDenominator());

        Fraction f3 = new Fraction(6, -8);
        Assert.assertEquals(-3, f3.getNumerator());
        Assert.assertEquals(4, f3.getDenominator());

        Fraction f4 = new Fraction(-6, -8);
        Assert.assertEquals(3, f4.getNumerator());
        Assert.assertEquals(4, f4.getDenominator());

        Fraction f5 = new Fraction(0, 5);
        Assert.assertEquals(0, f5.getNumerator());
        Assert.assertEquals(1, f5.getDenominator());
    }

    @Test(expected = MathArithmeticException.class)
    public void testConstructorZeroDenominator() {
        new Fraction(1, 0);
    }

    @Test(expected = MathArithmeticException.class)
    public void testConstructorOverflowMinNumerator() {
        new Fraction(Integer.MIN_VALUE, -1);
    }

    @Test(expected = MathArithmeticException.class)
    public void testConstructorOverflowMinDenominator() {
        new Fraction(1, Integer.MIN_VALUE);
    }

    @Test
    public void testConstructorDouble() {
        Fraction f1 = new Fraction(0.5);
        Assert.assertEquals(1, f1.getNumerator());
        Assert.assertEquals(2, f1.getDenominator());

        Fraction f2 = new Fraction(-0.75);
        Assert.assertEquals(-3, f2.getNumerator());
        Assert.assertEquals(4, f2.getDenominator());

        Fraction f3 = new Fraction(1.0);
        Assert.assertEquals(1, f3.getNumerator());
        Assert.assertEquals(1, f3.getDenominator());

        Fraction f4 = new Fraction(1.0 / 3.0);
        Assert.assertEquals(1, f4.getNumerator());
        Assert.assertEquals(3, f4.getDenominator());
    }

    @Test
    public void testConstructorDoubleWithEpsilon() {
        Fraction f = new Fraction(0.333333333333, 1e-4, 10);
        Assert.assertEquals(1, f.getNumerator());
        Assert.assertEquals(3, f.getDenominator());

        Fraction fExact = new Fraction(5.0, 1e-5, 10);
        Assert.assertEquals(5, fExact.getNumerator());
        Assert.assertEquals(1, fExact.getDenominator());
    }

    @Test
    public void testConstructorDoubleWithMaxDenominator() {
        Fraction f = new Fraction(0.666666, 10);
        Assert.assertEquals(2, f.getNumerator());
        Assert.assertEquals(3, f.getDenominator());

        Fraction f2 = new Fraction(0.666666, 2);
        Assert.assertEquals(1, f2.getNumerator());
        Assert.assertEquals(1, f2.getDenominator());
    }

    @Test(expected = FractionConversionException.class)
    public void testConstructorDoubleOverflowValue() {
        new Fraction(1e20, 1e-5, 100);
    }

    @Test(expected = FractionConversionException.class)
    public void testConstructorDoubleMaxIterations() {
        new Fraction(FastMath.PI, 1e-20, 2);
    }

    @Test(expected = FractionConversionException.class)
    public void testConstructorDoubleIntermediateOverflow() {
        new Fraction(2.0000000000000004e-10, 1e-20, 100);
    }

    @Test
    public void testAbs() {
        Fraction pos = new Fraction(3, 4);
        Fraction neg = new Fraction(-3, 4);

        Assert.assertSame(pos, pos.abs());
        Assert.assertEquals(new Fraction(3, 4), neg.abs());
        Assert.assertEquals(Fraction.ZERO, Fraction.ZERO.abs());
    }

    @Test(expected = MathArithmeticException.class)
    public void testAbsOverflow() {
        Fraction f = new Fraction(Integer.MIN_VALUE, 1);
        f.abs();
    }

    @Test
    public void testNegate() {
        Fraction f1 = new Fraction(3, 4);
        Assert.assertEquals(new Fraction(-3, 4), f1.negate());

        Fraction f2 = new Fraction(-3, 4);
        Assert.assertEquals(new Fraction(3, 4), f2.negate());

        Fraction f3 = new Fraction(0, 1);
        Assert.assertEquals(0, f3.negate().getNumerator());
    }

    @Test(expected = MathArithmeticException.class)
    public void testNegateOverflow() {
        Fraction f = new Fraction(Integer.MIN_VALUE, 1);
        f.negate();
    }

    @Test
    public void testReciprocal() {
        Fraction f1 = new Fraction(3, 4);
        Assert.assertEquals(new Fraction(4, 3), f1.reciprocal());

        Fraction f2 = new Fraction(-3, 4);
        Assert.assertEquals(new Fraction(-4, 3), f2.reciprocal());
    }

    @Test(expected = MathArithmeticException.class)
    public void testReciprocalZero() {
        Fraction.ZERO.reciprocal();
    }

    @Test
    public void testCompareTo() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = new Fraction(3, 4);
        Fraction f3 = new Fraction(2, 4);
        Fraction f4 = new Fraction(-1, 2);

        Assert.assertTrue(f1.compareTo(f2) < 0);
        Assert.assertTrue(f2.compareTo(f1) > 0);
        Assert.assertEquals(0, f1.compareTo(f3));
        Assert.assertTrue(f1.compareTo(f4) > 0);
        Assert.assertTrue(f4.compareTo(f1) < 0);
    }

    @Test
    public void testEqualsAndHashCode() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = new Fraction(2, 4);
        Fraction f3 = new Fraction(1, 3);

        Assert.assertTrue(f1.equals(f1));
        Assert.assertTrue(f1.equals(f2));
        Assert.assertFalse(f1.equals(f3));
        Assert.assertFalse(f1.equals(null));
        Assert.assertFalse(f1.equals("NotAFraction"));

        Assert.assertEquals(f1.hashCode(), f2.hashCode());
    }

    @Test
    public void testNumericValues() {
        Fraction f = new Fraction(3, 2);
        Assert.assertEquals(1.5, f.doubleValue(), EPSILON);
        Assert.assertEquals(1.5f, f.floatValue(), (float) EPSILON);
        Assert.assertEquals(1, f.intValue());
        Assert.assertEquals(1L, f.longValue());
        Assert.assertEquals(150.0, f.percentageValue(), EPSILON);

        Fraction neg = new Fraction(-7, 2);
        Assert.assertEquals(-3.5, neg.doubleValue(), EPSILON);
        Assert.assertEquals(-3, neg.intValue());
        Assert.assertEquals(-3L, neg.longValue());
        Assert.assertEquals(-350.0, neg.percentageValue(), EPSILON);
    }

    @Test
    public void testAdd() {
        Fraction f1 = new Fraction(1, 3);
        Fraction f2 = new Fraction(2, 3);
        Assert.assertEquals(Fraction.ONE, f1.add(f2));

        Fraction f3 = new Fraction(1, 4);
        Fraction f4 = new Fraction(1, 6);
        Assert.assertEquals(new Fraction(5, 12), f3.add(f4));

        Assert.assertEquals(f1, f1.add(Fraction.ZERO));
        Assert.assertEquals(f1, Fraction.ZERO.add(f1));

        Fraction f5 = new Fraction(1, 2);
        Assert.assertEquals(new Fraction(5, 2), f5.add(2));
        Assert.assertEquals(new Fraction(-3, 2), f5.add(-2));
    }

    @Test(expected = NullArgumentException.class)
    public void testAddNull() {
        Fraction.ONE.add((Fraction) null);
    }

    @Test(expected = MathArithmeticException.class)
    public void testAddOverflow() {
        Fraction f1 = new Fraction(Integer.MAX_VALUE - 1, 1);
        Fraction f2 = new Fraction(2, 1);
        f1.add(f2);
    }

    @Test
    public void testSubtract() {
        Fraction f1 = new Fraction(2, 3);
        Fraction f2 = new Fraction(1, 3);
        Assert.assertEquals(new Fraction(1, 3), f1.subtract(f2));

        Fraction f3 = new Fraction(1, 4);
        Fraction f4 = new Fraction(1, 6);
        Assert.assertEquals(new Fraction(1, 12), f3.subtract(f4));

        Assert.assertEquals(f1, f1.subtract(Fraction.ZERO));
        Assert.assertEquals(new Fraction(-2, 3), Fraction.ZERO.subtract(f1));

        Fraction f5 = new Fraction(5, 2);
        Assert.assertEquals(new Fraction(1, 2), f5.subtract(2));
        Assert.assertEquals(new Fraction(9, 2), f5.subtract(-2));
    }

    @Test(expected = NullArgumentException.class)
    public void testSubtractNull() {
        Fraction.ONE.subtract((Fraction) null);
    }

    @Test(expected = MathArithmeticException.class)
    public void testSubtractOverflow() {
        Fraction f1 = new Fraction(Integer.MIN_VALUE + 1, 1);
        Fraction f2 = new Fraction(2, 1);
        f1.subtract(f2);
    }

    @Test
    public void testMultiply() {
        Fraction f1 = new Fraction(2, 3);
        Fraction f2 = new Fraction(3, 4);
        Assert.assertEquals(Fraction.ONE_HALF, f1.multiply(f2));

        Assert.assertEquals(Fraction.ZERO, f1.multiply(Fraction.ZERO));
        Assert.assertEquals(Fraction.ZERO, Fraction.ZERO.multiply(f1));

        Fraction f3 = new Fraction(3, 5);
        Assert.assertEquals(new Fraction(6, 5), f3.multiply(2));
        Assert.assertEquals(new Fraction(-6, 5), f3.multiply(-2));
        Assert.assertEquals(Fraction.ZERO, f3.multiply(0));
    }

    @Test(expected = NullArgumentException.class)
    public void testMultiplyNull() {
        Fraction.ONE.multiply((Fraction) null);
    }

    @Test(expected = MathArithmeticException.class)
    public void testMultiplyOverflow() {
        Fraction f1 = new Fraction(Integer.MAX_VALUE, 2);
        Fraction f2 = new Fraction(3, 1);
        f1.multiply(f2);
    }

    @Test
    public void testDivide() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = new Fraction(3, 4);
        Assert.assertEquals(new Fraction(2, 3), f1.divide(f2));

        Fraction f3 = new Fraction(6, 5);
        Assert.assertEquals(new Fraction(3, 5), f3.divide(2));
        Assert.assertEquals(new Fraction(-3, 5), f3.divide(-2));
    }

    @Test(expected = NullArgumentException.class)
    public void testDivideNull() {
        Fraction.ONE.divide((Fraction) null);
    }

    @Test(expected = MathArithmeticException.class)
    public void testDivideByZeroFraction() {
        Fraction.ONE.divide(Fraction.ZERO);
    }

    @Test(expected = MathArithmeticException.class)
    public void testDivideByZeroInt() {
        Fraction.ONE.divide(0);
    }

    @Test(expected = MathArithmeticException.class)
    public void testDivideOverflow() {
        Fraction f1 = new Fraction(Integer.MIN_VALUE, 1);
        Fraction f2 = new Fraction(1, 1);
        f1.divide(f2).negate();
    }

    @Test
    public void testGetReducedFraction() {
        Fraction f1 = Fraction.getReducedFraction(2, 4);
        Assert.assertEquals(1, f1.getNumerator());
        Assert.assertEquals(2, f1.getDenominator());

        Fraction f2 = Fraction.getReducedFraction(0, 5);
        Assert.assertSame(Fraction.ZERO, f2);

        Fraction f3 = Fraction.getReducedFraction(2, -4);
        Assert.assertEquals(-1, f3.getNumerator());
        Assert.assertEquals(2, f3.getDenominator());

        Fraction f4 = Fraction.getReducedFraction(-2, -4);
        Assert.assertEquals(1, f4.getNumerator());
        Assert.assertEquals(2, f4.getDenominator());

        Fraction f5 = Fraction.getReducedFraction(2, Integer.MIN_VALUE);
        Assert.assertEquals(-1, f5.getNumerator());
        Assert.assertEquals(1073741824, f5.getDenominator());
    }

    @Test(expected = MathArithmeticException.class)
    public void testGetReducedFractionZeroDenominator() {
        Fraction.getReducedFraction(1, 0);
    }

    @Test(expected = MathArithmeticException.class)
    public void testGetReducedFractionOverflow1() {
        Fraction.getReducedFraction(Integer.MIN_VALUE, -1);
    }

    @Test(expected = MathArithmeticException.class)
    public void testGetReducedFractionOverflow2() {
        Fraction.getReducedFraction(1, Integer.MIN_VALUE);
    }

    @Test
    public void testToString() {
        Assert.assertEquals("0", Fraction.ZERO.toString());
        Assert.assertEquals("1", Fraction.ONE.toString());
        Assert.assertEquals("-5", new Fraction(-5, 1).toString());
        Assert.assertEquals("3 / 4", new Fraction(3, 4).toString());
        Assert.assertEquals("-3 / 4", new Fraction(-3, 4).toString());
    }

    @Test
    public void testGetField() {
        Fraction f = new Fraction(1, 2);
        Assert.assertEquals(FractionField.getInstance(), f.getField());
    }
}
