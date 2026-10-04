package org.apache.commons.math.fraction;

import org.junit.Assert;
import org.junit.Test;

public class FractionTest {

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
    }

    @Test
    public void testConstructorDouble() throws FractionConversionException {
        Fraction f1 = new Fraction(0.5);
        Assert.assertEquals(1, f1.getNumerator());
        Assert.assertEquals(2, f1.getDenominator());

        Fraction f2 = new Fraction(1.0);
        Assert.assertEquals(1, f2.getNumerator());
        Assert.assertEquals(1, f2.getDenominator());

        Fraction f3 = new Fraction(-0.75);
        Assert.assertEquals(-3, f3.getNumerator());
        Assert.assertEquals(4, f3.getDenominator());

        Fraction f4 = new Fraction(0.3333333333, 1.0e-5, 100);
        Assert.assertEquals(1, f4.getNumerator());
        Assert.assertEquals(3, f4.getDenominator());

        Fraction f5 = new Fraction(0.666666, 10);
        Assert.assertEquals(2, f5.getNumerator());
        Assert.assertEquals(3, f5.getDenominator());

        Fraction f6 = new Fraction(0.0);
        Assert.assertEquals(0, f6.getNumerator());
        Assert.assertEquals(1, f6.getDenominator());
    }

    @Test(expected = FractionConversionException.class)
    public void testConstructorDoubleOverflow() throws FractionConversionException {
        new Fraction(1e12, 1.0e-5, 100);
    }

    @Test(expected = FractionConversionException.class)
    public void testConstructorDoubleMaxIterations() throws FractionConversionException {
        new Fraction(0.123456789, 1.0e-20, 2);
    }

    @Test(expected = FractionConversionException.class)
    public void testConstructorDoubleContinuedFractionOverflow() throws FractionConversionException {
        new Fraction(Double.MAX_VALUE, 1.0e-5, 100);
    }

    @Test
    public void testConstructorIntInt() {
        Fraction f = new Fraction(4, 6);
        Assert.assertEquals(2, f.getNumerator());
        Assert.assertEquals(3, f.getDenominator());

        Fraction fNegDenom = new Fraction(2, -3);
        Assert.assertEquals(-2, fNegDenom.getNumerator());
        Assert.assertEquals(3, fNegDenom.getDenominator());

        Fraction fBothNeg = new Fraction(-2, -4);
        Assert.assertEquals(1, fBothNeg.getNumerator());
        Assert.assertEquals(2, fBothNeg.getDenominator());

        Fraction fZero = new Fraction(0, 5);
        Assert.assertEquals(0, fZero.getNumerator());
        Assert.assertEquals(1, fZero.getDenominator());
    }

    @Test(expected = ArithmeticException.class)
    public void testConstructorZeroDenominator() {
        new Fraction(1, 0);
    }

    @Test(expected = ArithmeticException.class)
    public void testConstructorMinIntDenom() {
        new Fraction(1, Integer.MIN_VALUE);
    }

    @Test(expected = ArithmeticException.class)
    public void testConstructorMinIntNumNegDenom() {
        new Fraction(Integer.MIN_VALUE, -1);
    }

    @Test
    public void testAbs() {
        Fraction f1 = new Fraction(-2, 3);
        Fraction f2 = new Fraction(2, 3);
        Assert.assertEquals(2, f1.abs().getNumerator());
        Assert.assertEquals(3, f1.abs().getDenominator());
        Assert.assertEquals(2, f2.abs().getNumerator());
        Assert.assertEquals(3, f2.abs().getDenominator());
    }

    @Test
    public void testCompareTo() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = new Fraction(2, 3);
        Fraction f3 = new Fraction(2, 4);

        Assert.assertTrue(f1.compareTo(f2) < 0);
        Assert.assertTrue(f2.compareTo(f1) > 0);
        Assert.assertEquals(0, f1.compareTo(f3));
    }

    @Test
    public void testConversions() {
        Fraction f = new Fraction(3, 2);
        Assert.assertEquals(1.5, f.doubleValue(), 1e-15);
        Assert.assertEquals(1.5f, f.floatValue(), 1e-7f);
        Assert.assertEquals(1, f.intValue());
        Assert.assertEquals(1L, f.longValue());
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
        Assert.assertFalse(f1.equals("String"));

        Assert.assertEquals(f1.hashCode(), f2.hashCode());
    }

    @Test
    public void testNegate() {
        Fraction f = new Fraction(2, 3);
        Fraction neg = f.negate();
        Assert.assertEquals(-2, neg.getNumerator());
        Assert.assertEquals(3, neg.getDenominator());

        Fraction fNeg = new Fraction(-2, 3);
        Assert.assertEquals(2, fNeg.negate().getNumerator());
        Assert.assertEquals(3, fNeg.negate().getDenominator());
    }

    @Test(expected = ArithmeticException.class)
    public void testNegateOverflow() {
        new Fraction(Integer.MIN_VALUE, 1).negate();
    }

    @Test
    public void testReciprocal() {
        Fraction f = new Fraction(2, 3);
        Fraction rec = f.reciprocal();
        Assert.assertEquals(3, rec.getNumerator());
        Assert.assertEquals(2, rec.getDenominator());

        Fraction fNeg = new Fraction(-2, 3);
        Fraction recNeg = fNeg.reciprocal();
        Assert.assertEquals(-3, recNeg.getNumerator());
        Assert.assertEquals(2, recNeg.getDenominator());
    }

    @Test
    public void testAdd() {
        Fraction f1 = new Fraction(1, 3);
        Fraction f2 = new Fraction(2, 3);
        Fraction res = f1.add(f2);
        Assert.assertEquals(1, res.getNumerator());
        Assert.assertEquals(1, res.getDenominator());

        Fraction f3 = new Fraction(1, 2);
        Fraction f4 = new Fraction(1, 3);
        Fraction res2 = f3.add(f4);
        Assert.assertEquals(5, res2.getNumerator());
        Assert.assertEquals(6, res2.getDenominator());

        Fraction fZero = Fraction.ZERO;
        Assert.assertEquals(f1, fZero.add(f1));
        Assert.assertEquals(f1, f1.add(fZero));

        Fraction f5 = new Fraction(1, 6);
        Fraction f6 = new Fraction(1, 4);
        Fraction res3 = f5.add(f6);
        Assert.assertEquals(5, res3.getNumerator());
        Assert.assertEquals(12, res3.getDenominator());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNull() {
        Fraction.ONE.add(null);
    }

    @Test
    public void testSubtract() {
        Fraction f1 = new Fraction(2, 3);
        Fraction f2 = new Fraction(1, 3);
        Fraction res = f1.subtract(f2);
        Assert.assertEquals(1, res.getNumerator());
        Assert.assertEquals(3, res.getDenominator());

        Fraction fZero = Fraction.ZERO;
        Assert.assertEquals(new Fraction(-1, 3), fZero.subtract(f2));
        Assert.assertEquals(f1, f1.subtract(fZero));

        Fraction f3 = new Fraction(5, 6);
        Fraction f4 = new Fraction(1, 4);
        Fraction res2 = f3.subtract(f4);
        Assert.assertEquals(7, res2.getNumerator());
        Assert.assertEquals(12, res2.getDenominator());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubtractNull() {
        Fraction.ONE.subtract(null);
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
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMultiplyNull() {
        Fraction.ONE.multiply(null);
    }

    @Test
    public void testDivide() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = new Fraction(1, 4);
        Fraction res = f1.divide(f2);
        Assert.assertEquals(2, res.getNumerator());
        Assert.assertEquals(1, res.getDenominator());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDivideNull() {
        Fraction.ONE.divide(null);
    }

    @Test(expected = ArithmeticException.class)
    public void testDivideByZero() {
        Fraction.ONE.divide(Fraction.ZERO);
    }

    @Test
    public void testGetReducedFraction() {
        Fraction f1 = Fraction.getReducedFraction(0, 5);
        Assert.assertEquals(0, f1.getNumerator());
        Assert.assertEquals(1, f1.getDenominator());

        Fraction f2 = Fraction.getReducedFraction(2, 4);
        Assert.assertEquals(1, f2.getNumerator());
        Assert.assertEquals(2, f2.getDenominator());

        Fraction f3 = Fraction.getReducedFraction(-2, -4);
        Assert.assertEquals(1, f3.getNumerator());
        Assert.assertEquals(2, f3.getDenominator());

        Fraction f4 = Fraction.getReducedFraction(2, -4);
        Assert.assertEquals(-1, f4.getNumerator());
        Assert.assertEquals(2, f4.getDenominator());

        Fraction f5 = Fraction.getReducedFraction(2, Integer.MIN_VALUE);
        Assert.assertEquals(-1, f5.getNumerator());
        Assert.assertEquals(1073741824, f5.getDenominator());
    }

    @Test(expected = ArithmeticException.class)
    public void testGetReducedFractionZeroDenom() {
        Fraction.getReducedFraction(1, 0);
    }

    @Test(expected = ArithmeticException.class)
    public void testGetReducedFractionMinDenomOddNum() {
        Fraction.getReducedFraction(1, Integer.MIN_VALUE);
    }

    @Test(expected = ArithmeticException.class)
    public void testGetReducedFractionMinNumNegDenom() {
        Fraction.getReducedFraction(Integer.MIN_VALUE, -1);
    }

    @Test(expected = ArithmeticException.class)
    public void testAddSubOverflow() {
        Fraction f1 = new Fraction(Integer.MAX_VALUE - 1, 1);
        Fraction f2 = new Fraction(2, 1);
        f1.add(f2);
    }

    @Test(expected = ArithmeticException.class)
    public void testAddSubBigIntegerNumeratorOverflow() {
        Fraction f1 = new Fraction(Integer.MAX_VALUE, 2);
        Fraction f2 = new Fraction(Integer.MAX_VALUE, 2);
        f1.add(f2);
    }
}
