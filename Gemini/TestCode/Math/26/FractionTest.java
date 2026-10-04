package org.apache.commons.math3.fraction;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.junit.Assert;
import org.junit.Test;

public class FractionTest {

    private static final double EPSILON = 10e-6;

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
    public void testConstructorDouble() throws FractionConversionException {
        Fraction f = new Fraction(0.5);
        Assert.assertEquals(1, f.getNumerator());
        Assert.assertEquals(2, f.getDenominator());

        f = new Fraction(0.0);
        Assert.assertEquals(0, f.getNumerator());
        Assert.assertEquals(1, f.getDenominator());

        f = new Fraction(1.0);
        Assert.assertEquals(1, f.getNumerator());
        Assert.assertEquals(1, f.getDenominator());

        f = new Fraction(-0.75);
        Assert.assertEquals(-3, f.getNumerator());
        Assert.assertEquals(4, f.getDenominator());

        f = new Fraction(2.5, 1.0e-5, 100);
        Assert.assertEquals(5, f.getNumerator());
        Assert.assertEquals(2, f.getDenominator());
    }

    @Test
    public void testConstructorDoubleMaxDenominator() throws FractionConversionException {
        Fraction f = new Fraction(0.3333333333, 10);
        Assert.assertEquals(1, f.getNumerator());
        Assert.assertEquals(3, f.getDenominator());

        f = new Fraction(0.6666666666, 10);
        Assert.assertEquals(2, f.getNumerator());
        Assert.assertEquals(3, f.getDenominator());
    }

    @Test(expected = FractionConversionException.class)
    public void testConstructorDoubleOverflowA0() throws FractionConversionException {
        new Fraction(1.0e20, 1.0e-5, 100);
    }

    @Test(expected = FractionConversionException.class)
    public void testConstructorDoubleOverflowP2Q2() throws FractionConversionException {
        new Fraction(1.0e-20, 1.0e-30, 2);
    }

    @Test(expected = FractionConversionException.class)
    public void testConstructorDoubleMaxIterationsExceeded() throws FractionConversionException {
        new Fraction(FastMath.PI, 1.0e-15, 2);
    }

    @Test
    public void testConstructorInt() {
        Fraction f = new Fraction(5);
        Assert.assertEquals(5, f.getNumerator());
        Assert.assertEquals(1, f.getDenominator());

        f = new Fraction(-3);
        Assert.assertEquals(-3, f.getNumerator());
        Assert.assertEquals(1, f.getDenominator());
    }

    @Test
    public void testConstructorIntInt() {
        Fraction f = new Fraction(6, 8);
        Assert.assertEquals(3, f.getNumerator());
        Assert.assertEquals(4, f.getDenominator());

        f = new Fraction(-6, 8);
        Assert.assertEquals(-3, f.getNumerator());
        Assert.assertEquals(4, f.getDenominator());

        f = new Fraction(6, -8);
        Assert.assertEquals(-3, f.getNumerator());
        Assert.assertEquals(4, f.getDenominator());

        f = new Fraction(-6, -8);
        Assert.assertEquals(3, f.getNumerator());
        Assert.assertEquals(4, f.getDenominator());

        f = new Fraction(0, 5);
        Assert.assertEquals(0, f.getNumerator());
        Assert.assertEquals(1, f.getDenominator());
    }

    @Test(expected = MathArithmeticException.class)
    public void testConstructorZeroDenominator() {
        new Fraction(1, 0);
    }

    @Test(expected = MathArithmeticException.class)
    public void testConstructorOverflowNumeratorMinVal() {
        new Fraction(Integer.MIN_VALUE, -1);
    }

    @Test(expected = MathArithmeticException.class)
    public void testConstructorOverflowDenominatorMinVal() {
        new Fraction(1, Integer.MIN_VALUE);
    }

    @Test
    public void testAbs() {
        Fraction f1 = new Fraction(3, 4);
        Fraction f2 = new Fraction(-3, 4);
        Fraction f3 = new Fraction(0, 1);

        Assert.assertSame(f1, f1.abs());
        Assert.assertEquals(f1, f2.abs());
        Assert.assertSame(f3, f3.abs());
    }

    @Test(expected = MathArithmeticException.class)
    public void testAbsOverflow() {
        new Fraction(Integer.MIN_VALUE, 1).abs();
    }

    @Test
    public void testCompareTo() {
        Fraction first = new Fraction(1, 2);
        Fraction second = new Fraction(1, 3);
        Fraction third = new Fraction(2, 4);
        Fraction fourth = new Fraction(2, 3);

        Assert.assertTrue(first.compareTo(first) == 0);
        Assert.assertTrue(first.compareTo(second) > 0);
        Assert.assertTrue(second.compareTo(first) < 0);
        Assert.assertTrue(first.compareTo(third) == 0);
        Assert.assertTrue(first.compareTo(fourth) < 0);

        Fraction largePos = new Fraction(Integer.MAX_VALUE, 1);
        Fraction largeNeg = new Fraction(Integer.MIN_VALUE + 1, 1);
        Assert.assertTrue(largePos.compareTo(largeNeg) > 0);
        Assert.assertTrue(largeNeg.compareTo(largePos) < 0);
    }

    @Test
    public void testConversions() {
        Fraction f = new Fraction(7, 2);
        Assert.assertEquals(3.5, f.doubleValue(), EPSILON);
        Assert.assertEquals(3.5f, f.floatValue(), EPSILON);
        Assert.assertEquals(3, f.intValue());
        Assert.assertEquals(3L, f.longValue());
        Assert.assertEquals(350.0, f.percentageValue(), EPSILON);

        Fraction neg = new Fraction(-7, 2);
        Assert.assertEquals(-3.5, neg.doubleValue(), EPSILON);
        Assert.assertEquals(-3, neg.intValue());
        Assert.assertEquals(-3L, neg.longValue());
        Assert.assertEquals(-350.0, neg.percentageValue(), EPSILON);
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
        Assert.assertFalse(f1.equals(new Object()));
        Assert.assertFalse(f1.equals(new Fraction(2, 3)));

        Assert.assertEquals(f1.hashCode(), f2.hashCode());
        Assert.assertFalse(f1.hashCode() == f3.hashCode());
    }

    @Test
    public void testNegate() {
        Fraction f = new Fraction(3, 4);
        Assert.assertEquals(new Fraction(-3, 4), f.negate());

        f = new Fraction(-3, 4);
        Assert.assertEquals(new Fraction(3, 4), f.negate());

        f = new Fraction(0, 1);
        Assert.assertEquals(new Fraction(0, 1), f.negate());
    }

    @Test(expected = MathArithmeticException.class)
    public void testNegateOverflow() {
        new Fraction(Integer.MIN_VALUE, 1).negate();
    }

    @Test
    public void testReciprocal() {
        Fraction f = new Fraction(3, 4);
        Assert.assertEquals(new Fraction(4, 3), f.reciprocal());

        f = new Fraction(-3, 4);
        Assert.assertEquals(new Fraction(-4, 3), f.reciprocal());
    }

    @Test(expected = MathArithmeticException.class)
    public void testReciprocalZero() {
        Fraction.ZERO.reciprocal();
    }

    @Test
    public void testAdd() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = new Fraction(1, 3);
        Fraction res = f1.add(f2);
        Assert.assertEquals(5, res.getNumerator());
        Assert.assertEquals(6, res.getDenominator());

        Fraction f3 = new Fraction(1, 4);
        Fraction f4 = new Fraction(3, 4);
        res = f3.add(f4);
        Assert.assertEquals(1, res.getNumerator());
        Assert.assertEquals(1, res.getDenominator());

        Assert.assertEquals(f1, f1.add(Fraction.ZERO));
        Assert.assertEquals(f1, Fraction.ZERO.add(f1));

        Fraction intAdd = f1.add(2);
        Assert.assertEquals(5, intAdd.getNumerator());
        Assert.assertEquals(2, intAdd.getDenominator());
    }

    @Test(expected = NullArgumentException.class)
    public void testAddNull() {
        Fraction.ONE.add((Fraction) null);
    }

    @Test(expected = MathArithmeticException.class)
    public void testAddOverflow() {
        Fraction f1 = new Fraction(Integer.MAX_VALUE - 1, 1);
        f1.add(new Fraction(2, 1));
    }

    @Test
    public void testSubtract() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = new Fraction(1, 3);
        Fraction res = f1.subtract(f2);
        Assert.assertEquals(1, res.getNumerator());
        Assert.assertEquals(6, res.getDenominator());

        Fraction f3 = new Fraction(3, 4);
        Fraction f4 = new Fraction(1, 4);
        res = f3.subtract(f4);
        Assert.assertEquals(1, res.getNumerator());
        Assert.assertEquals(2, res.getDenominator());

        Assert.assertEquals(f1, f1.subtract(Fraction.ZERO));
        Assert.assertEquals(f1.negate(), Fraction.ZERO.subtract(f1));

        Fraction intSub = f1.subtract(2);
        Assert.assertEquals(-3, intSub.getNumerator());
        Assert.assertEquals(2, intSub.getDenominator());
    }

    @Test(expected = NullArgumentException.class)
    public void testSubtractNull() {
        Fraction.ONE.subtract((Fraction) null);
    }

    @Test(expected = MathArithmeticException.class)
    public void testSubtractOverflow() {
        Fraction f1 = new Fraction(Integer.MIN_VALUE + 1, 1);
        f1.subtract(new Fraction(2, 1));
    }

    @Test
    public void testAddSubSpecialBranches() {
        Fraction f1 = new Fraction(2, 9);
        Fraction f2 = new Fraction(5, 6);
        Fraction res = f1.add(f2);
        Assert.assertEquals(19, res.getNumerator());
        Assert.assertEquals(18, res.getDenominator());

        res = f1.subtract(f2);
        Assert.assertEquals(-11, res.getNumerator());
        Assert.assertEquals(18, res.getDenominator());
    }

    @Test(expected = MathArithmeticException.class)
    public void testAddSubNumeratorOverflowAfterMultiply() {
        Fraction f1 = new Fraction(Integer.MAX_VALUE - 1, 2);
        Fraction f2 = new Fraction(Integer.MAX_VALUE - 1, 4);
        f1.add(f2);
    }

    @Test
    public void testMultiply() {
        Fraction f1 = new Fraction(2, 3);
        Fraction f2 = new Fraction(3, 4);
        Fraction res = f1.multiply(f2);
        Assert.assertEquals(1, res.getNumerator());
        Assert.assertEquals(2, res.getDenominator());

        Assert.assertEquals(Fraction.ZERO, f1.multiply(Fraction.ZERO));
        Assert.assertEquals(Fraction.ZERO, Fraction.ZERO.multiply(f1));

        Fraction intMul = f1.multiply(3);
        Assert.assertEquals(2, intMul.getNumerator());
        Assert.assertEquals(1, intMul.getDenominator());
    }

    @Test(expected = NullArgumentException.class)
    public void testMultiplyNull() {
        Fraction.ONE.multiply((Fraction) null);
    }

    @Test(expected = MathArithmeticException.class)
    public void testMultiplyOverflow() {
        Fraction f1 = new Fraction(Integer.MAX_VALUE, 1);
        Fraction f2 = new Fraction(2, 1);
        f1.multiply(f2);
    }

    @Test
    public void testDivide() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = new Fraction(1, 3);
        Fraction res = f1.divide(f2);
        Assert.assertEquals(3, res.getNumerator());
        Assert.assertEquals(2, res.getDenominator());

        Fraction intDiv = f1.divide(2);
        Assert.assertEquals(1, intDiv.getNumerator());
        Assert.assertEquals(4, intDiv.getDenominator());
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
    public void testDivideOverflow() {
        Fraction f1 = new Fraction(Integer.MAX_VALUE, 1);
        Fraction f2 = new Fraction(1, 2);
        f1.divide(f2);
    }

    @Test
    public void testGetReducedFraction() {
        Fraction f = Fraction.getReducedFraction(2, 4);
        Assert.assertEquals(1, f.getNumerator());
        Assert.assertEquals(2, f.getDenominator());

        f = Fraction.getReducedFraction(0, 5);
        Assert.assertSame(Fraction.ZERO, f);

        f = Fraction.getReducedFraction(2, -4);
        Assert.assertEquals(-1, f.getNumerator());
        Assert.assertEquals(2, f.getDenominator());

        f = Fraction.getReducedFraction(-2, -4);
        Assert.assertEquals(1, f.getNumerator());
        Assert.assertEquals(2, f.getDenominator());

        f = Fraction.getReducedFraction(4, Integer.MIN_VALUE);
        Assert.assertEquals(1, f.getNumerator());
        Assert.assertEquals(Integer.MIN_VALUE / 4, f.getDenominator());
    }

    @Test(expected = MathArithmeticException.class)
    public void testGetReducedFractionZeroDen() {
        Fraction.getReducedFraction(1, 0);
    }

    @Test(expected = MathArithmeticException.class)
    public void testGetReducedFractionOverflow() {
        Fraction.getReducedFraction(1, Integer.MIN_VALUE);
    }

    @Test(expected = MathArithmeticException.class)
    public void testGetReducedFractionOverflowNumerator() {
        Fraction.getReducedFraction(Integer.MIN_VALUE, -1);
    }

    @Test
    public void testToString() {
        Assert.assertEquals("0", new Fraction(0, 3).toString());
        Assert.assertEquals("3", new Fraction(3, 1).toString());
        Assert.assertEquals("3 / 4", new Fraction(3, 4).toString());
        Assert.assertEquals("-3 / 4", new Fraction(-3, 4).toString());
    }

    @Test
    public void testGetField() {
        Fraction f = new Fraction(1, 2);
        Assert.assertNotNull(f.getField());
        Assert.assertEquals(FractionField.getInstance(), f.getField());
    }
}
