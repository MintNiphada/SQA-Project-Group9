package org.apache.commons.lang3.math;

import org.junit.Assert;
import org.junit.Test;

public class FractionTest {

    private static final double EPSILON = 1e-6;

    @Test
    public void testConstants() {
        Assert.assertEquals(0, Fraction.ZERO.getNumerator());
        Assert.assertEquals(1, Fraction.ZERO.getDenominator());

        Assert.assertEquals(1, Fraction.ONE.getNumerator());
        Assert.assertEquals(1, Fraction.ONE.getDenominator());

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
    public void testGetFraction_Int_Int() {
        Fraction f = Fraction.getFraction(3, 4);
        Assert.assertEquals(3, f.getNumerator());
        Assert.assertEquals(4, f.getDenominator());

        f = Fraction.getFraction(3, -4);
        Assert.assertEquals(-3, f.getNumerator());
        Assert.assertEquals(4, f.getDenominator());

        f = Fraction.getFraction(-3, -4);
        Assert.assertEquals(3, f.getNumerator());
        Assert.assertEquals(4, f.getDenominator());

        f = Fraction.getFraction(-3, 4);
        Assert.assertEquals(-3, f.getNumerator());
        Assert.assertEquals(4, f.getDenominator());

        f = Fraction.getFraction(Integer.MIN_VALUE, 1);
        Assert.assertEquals(Integer.MIN_VALUE, f.getNumerator());
        Assert.assertEquals(1, f.getDenominator());
    }

    @Test(expected = ArithmeticException.class)
    public void testGetFraction_Int_Int_ZeroDenominator() {
        Fraction.getFraction(1, 0);
    }

    @Test(expected = ArithmeticException.class)
    public void testGetFraction_Int_Int_OverflowNumerator() {
        Fraction.getFraction(Integer.MIN_VALUE, -1);
    }

    @Test(expected = ArithmeticException.class)
    public void testGetFraction_Int_Int_OverflowDenominator() {
        Fraction.getFraction(1, Integer.MIN_VALUE);
    }

    @Test
    public void testGetFraction_Whole_Int_Int() {
        Fraction f = Fraction.getFraction(1, 2, 3);
        Assert.assertEquals(5, f.getNumerator());
        Assert.assertEquals(3, f.getDenominator());

        f = Fraction.getFraction(-1, 2, 3);
        Assert.assertEquals(-5, f.getNumerator());
        Assert.assertEquals(3, f.getDenominator());

        f = Fraction.getFraction(0, 2, 3);
        Assert.assertEquals(2, f.getNumerator());
        Assert.assertEquals(3, f.getDenominator());

        f = Fraction.getFraction(0, 0, 3);
        Assert.assertEquals(0, f.getNumerator());
        Assert.assertEquals(3, f.getDenominator());
    }

    @Test(expected = ArithmeticException.class)
    public void testGetFraction_Whole_Int_Int_ZeroDenominator() {
        Fraction.getFraction(1, 1, 0);
    }

    @Test(expected = ArithmeticException.class)
    public void testGetFraction_Whole_Int_Int_NegativeDenominator() {
        Fraction.getFraction(1, 1, -1);
    }

    @Test(expected = ArithmeticException.class)
    public void testGetFraction_Whole_Int_Int_NegativeNumerator() {
        Fraction.getFraction(1, -1, 1);
    }

    @Test(expected = ArithmeticException.class)
    public void testGetFraction_Whole_Int_Int_OverflowPositive() {
        Fraction.getFraction(Integer.MAX_VALUE, 1, 2);
    }

    @Test(expected = ArithmeticException.class)
    public void testGetFraction_Whole_Int_Int_OverflowNegative() {
        Fraction.getFraction(Integer.MIN_VALUE, 1, 2);
    }

    @Test
    public void testGetReducedFraction() {
        Fraction f = Fraction.getReducedFraction(0, 5);
        Assert.assertEquals(0, f.getNumerator());
        Assert.assertEquals(1, f.getDenominator());

        f = Fraction.getReducedFraction(2, 4);
        Assert.assertEquals(1, f.getNumerator());
        Assert.assertEquals(2, f.getDenominator());

        f = Fraction.getReducedFraction(-2, 4);
        Assert.assertEquals(-1, f.getNumerator());
        Assert.assertEquals(2, f.getDenominator());

        f = Fraction.getReducedFraction(2, -4);
        Assert.assertEquals(-1, f.getNumerator());
        Assert.assertEquals(2, f.getDenominator());

        f = Fraction.getReducedFraction(-2, -4);
        Assert.assertEquals(1, f.getNumerator());
        Assert.assertEquals(2, f.getDenominator());

        f = Fraction.getReducedFraction(4, Integer.MIN_VALUE);
        Assert.assertEquals(1, f.getNumerator());
        Assert.assertEquals(-(Integer.MIN_VALUE / 4), f.getDenominator());
    }

    @Test(expected = ArithmeticException.class)
    public void testGetReducedFraction_ZeroDenominator() {
        Fraction.getReducedFraction(1, 0);
    }

    @Test(expected = ArithmeticException.class)
    public void testGetReducedFraction_OverflowNumerator() {
        Fraction.getReducedFraction(Integer.MIN_VALUE, -1);
    }

    @Test(expected = ArithmeticException.class)
    public void testGetReducedFraction_OverflowDenominator() {
        Fraction.getReducedFraction(1, Integer.MIN_VALUE);
    }

    @Test
    public void testGetFraction_Double() {
        Fraction f = Fraction.getFraction(0.0);
        Assert.assertEquals(0, f.getNumerator());
        Assert.assertEquals(1, f.getDenominator());

        f = Fraction.getFraction(0.5);
        Assert.assertEquals(1, f.getNumerator());
        Assert.assertEquals(2, f.getDenominator());

        f = Fraction.getFraction(-0.5);
        Assert.assertEquals(-1, f.getNumerator());
        Assert.assertEquals(2, f.getDenominator());

        f = Fraction.getFraction(1.0);
        Assert.assertEquals(1, f.getNumerator());
        Assert.assertEquals(1, f.getDenominator());

        f = Fraction.getFraction(0.75);
        Assert.assertEquals(3, f.getNumerator());
        Assert.assertEquals(4, f.getDenominator());

        f = Fraction.getFraction(0.3333333);
        Assert.assertEquals(1, f.getNumerator());
        Assert.assertEquals(3, f.getDenominator());

        f = Fraction.getFraction(2.5);
        Assert.assertEquals(5, f.getNumerator());
        Assert.assertEquals(2, f.getDenominator());

        f = Fraction.getFraction(-2.5);
        Assert.assertEquals(-5, f.getNumerator());
        Assert.assertEquals(2, f.getDenominator());
    }

    @Test(expected = ArithmeticException.class)
    public void testGetFraction_Double_GreaterThanMaxInt() {
        Fraction.getFraction((double) Integer.MAX_VALUE + 1000.0);
    }

    @Test(expected = ArithmeticException.class)
    public void testGetFraction_Double_NaN() {
        Fraction.getFraction(Double.NaN);
    }

    @Test(expected = ArithmeticException.class)
    public void testGetFraction_Double_NonConvergent() {
        Fraction.getFraction(0.1234567890123456789);
    }

    @Test
    public void testGetFraction_String() {
        Fraction f = Fraction.getFraction("3/4");
        Assert.assertEquals(3, f.getNumerator());
        Assert.assertEquals(4, f.getDenominator());

        f = Fraction.getFraction("-3/4");
        Assert.assertEquals(-3, f.getNumerator());
        Assert.assertEquals(4, f.getDenominator());

        f = Fraction.getFraction("1 3/4");
        Assert.assertEquals(7, f.getNumerator());
        Assert.assertEquals(4, f.getDenominator());

        f = Fraction.getFraction("-1 3/4");
        Assert.assertEquals(-7, f.getNumerator());
        Assert.assertEquals(4, f.getDenominator());

        f = Fraction.getFraction("5");
        Assert.assertEquals(5, f.getNumerator());
        Assert.assertEquals(1, f.getDenominator());

        f = Fraction.getFraction("-5");
        Assert.assertEquals(-5, f.getNumerator());
        Assert.assertEquals(1, f.getDenominator());

        f = Fraction.getFraction("0.75");
        Assert.assertEquals(3, f.getNumerator());
        Assert.assertEquals(4, f.getDenominator());

        f = Fraction.getFraction("-0.75");
        Assert.assertEquals(-3, f.getNumerator());
        Assert.assertEquals(4, f.getDenominator());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetFraction_String_Null() {
        Fraction.getFraction(null);
    }

    @Test(expected = NumberFormatException.class)
    public void testGetFraction_String_InvalidFormatSpaceWithoutSlash() {
        Fraction.getFraction("1 3");
    }

    @Test(expected = NumberFormatException.class)
    public void testGetFraction_String_Empty() {
        Fraction.getFraction("");
    }

    @Test(expected = NumberFormatException.class)
    public void testGetFraction_String_Invalid() {
        Fraction.getFraction("foo");
    }

    @Test
    public void testAccessorsAndNumberConversions() {
        Fraction f = Fraction.getFraction(7, 4);
        Assert.assertEquals(7, f.getNumerator());
        Assert.assertEquals(4, f.getDenominator());
        Assert.assertEquals(3, f.getProperNumerator());
        Assert.assertEquals(1, f.getProperWhole());
        Assert.assertEquals(1, f.intValue());
        Assert.assertEquals(1L, f.longValue());
        Assert.assertEquals(1.75f, f.floatValue(), EPSILON);
        Assert.assertEquals(1.75d, f.doubleValue(), EPSILON);

        Fraction fn = Fraction.getFraction(-7, 4);
        Assert.assertEquals(-7, fn.getNumerator());
        Assert.assertEquals(4, fn.getDenominator());
        Assert.assertEquals(3, fn.getProperNumerator());
        Assert.assertEquals(-1, fn.getProperWhole());
        Assert.assertEquals(-1, fn.intValue());
        Assert.assertEquals(-1L, fn.longValue());
        Assert.assertEquals(-1.75f, fn.floatValue(), EPSILON);
        Assert.assertEquals(-1.75d, fn.doubleValue(), EPSILON);
    }

    @Test
    public void testReduce() {
        Fraction f = Fraction.getFraction(2, 4).reduce();
        Assert.assertEquals(1, f.getNumerator());
        Assert.assertEquals(2, f.getDenominator());

        f = Fraction.getFraction(3, 5).reduce();
        Assert.assertEquals(3, f.getNumerator());
        Assert.assertEquals(5, f.getDenominator());

        f = Fraction.ZERO.reduce();
        Assert.assertSame(Fraction.ZERO, f);

        f = Fraction.getFraction(0, 4).reduce();
        Assert.assertEquals(Fraction.ZERO, f);

        f = Fraction.getFraction(Integer.MIN_VALUE, 2).reduce();
        Assert.assertEquals(Integer.MIN_VALUE / 2, f.getNumerator());
        Assert.assertEquals(1, f.getDenominator());
    }

    @Test
    public void testInvert() {
        Fraction f = Fraction.getFraction(3, 4).invert();
        Assert.assertEquals(4, f.getNumerator());
        Assert.assertEquals(3, f.getDenominator());

        f = Fraction.getFraction(-3, 4).invert();
        Assert.assertEquals(-4, f.getNumerator());
        Assert.assertEquals(3, f.getDenominator());
    }

    @Test(expected = ArithmeticException.class)
    public void testInvert_Zero() {
        Fraction.ZERO.invert();
    }

    @Test(expected = ArithmeticException.class)
    public void testInvert_OverflowNumerator() {
        Fraction.getFraction(Integer.MIN_VALUE, 1).invert();
    }

    @Test
    public void testNegate() {
        Fraction f = Fraction.getFraction(3, 4).negate();
        Assert.assertEquals(-3, f.getNumerator());
        Assert.assertEquals(4, f.getDenominator());

        f = Fraction.getFraction(-3, 4).negate();
        Assert.assertEquals(3, f.getNumerator());
        Assert.assertEquals(4, f.getDenominator());

        f = Fraction.ZERO.negate();
        Assert.assertEquals(0, f.getNumerator());
        Assert.assertEquals(1, f.getDenominator());
    }

    @Test(expected = ArithmeticException.class)
    public void testNegate_Overflow() {
        Fraction.getFraction(Integer.MIN_VALUE, 1).negate();
    }

    @Test
    public void testAbs() {
        Fraction f = Fraction.getFraction(3, 4).abs();
        Assert.assertEquals(3, f.getNumerator());
        Assert.assertEquals(4, f.getDenominator());

        f = Fraction.getFraction(-3, 4).abs();
        Assert.assertEquals(3, f.getNumerator());
        Assert.assertEquals(4, f.getDenominator());

        f = Fraction.ZERO.abs();
        Assert.assertEquals(0, f.getNumerator());
        Assert.assertEquals(1, f.getDenominator());
    }

    @Test
    public void testPow() {
        Fraction f = Fraction.getFraction(2, 3);
        Assert.assertEquals(Fraction.ONE, f.pow(0));
        Assert.assertEquals(Fraction.ONE, Fraction.ZERO.pow(0));
        Assert.assertEquals(f, f.pow(1));
        Assert.assertEquals(Fraction.getFraction(4, 9), f.pow(2));
        Assert.assertEquals(Fraction.getFraction(8, 27), f.pow(3));
        Assert.assertEquals(Fraction.getFraction(3, 2), f.pow(-1));
        Assert.assertEquals(Fraction.getFraction(9, 4), f.pow(-2));
        Assert.assertEquals(Fraction.getFraction(27, 8), f.pow(-3));
    }

    @Test(expected = ArithmeticException.class)
    public void testPow_NegativePowerZero() {
        Fraction.ZERO.pow(-1);
    }

    @Test(expected = ArithmeticException.class)
    public void testPow_Overflow() {
        Fraction.getFraction(Integer.MAX_VALUE, 1).pow(2);
    }

    @Test(expected = ArithmeticException.class)
    public void testPow_MinPower() {
        Fraction.getFraction(2, 3).pow(Integer.MIN_VALUE);
    }

    @Test
    public void testAdd() {
        Fraction f1 = Fraction.getFraction(1, 3);
        Fraction f2 = Fraction.getFraction(1, 6);
        Fraction result = f1.add(f2);
        Assert.assertEquals(1, result.getNumerator());
        Assert.assertEquals(2, result.getDenominator());

        Assert.assertEquals(f1, f1.add(Fraction.ZERO));
        Assert.assertEquals(f2, Fraction.ZERO.add(f2));

        f1 = Fraction.getFraction(1, 2);
        f2 = Fraction.getFraction(1, 3);
        result = f1.add(f2);
        Assert.assertEquals(5, result.getNumerator());
        Assert.assertEquals(6, result.getDenominator());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAdd_Null() {
        Fraction.ONE.add(null);
    }

    @Test(expected = ArithmeticException.class)
    public void testAdd_OverflowD1Equals1() {
        Fraction.getFraction(Integer.MAX_VALUE - 1, 1).add(Fraction.getFraction(2, 1));
    }

    @Test(expected = ArithmeticException.class)
    public void testAdd_OverflowBigIntNumerator() {
        Fraction.getFraction(Integer.MAX_VALUE, 2).add(Fraction.getFraction(Integer.MAX_VALUE, 2));
    }

    @Test
    public void testSubtract() {
        Fraction f1 = Fraction.getFraction(1, 2);
        Fraction f2 = Fraction.getFraction(1, 6);
        Fraction result = f1.subtract(f2);
        Assert.assertEquals(1, result.getNumerator());
        Assert.assertEquals(3, result.getDenominator());

        Assert.assertEquals(f1, f1.subtract(Fraction.ZERO));
        Assert.assertEquals(f2.negate(), Fraction.ZERO.subtract(f2));

        f1 = Fraction.getFraction(1, 2);
        f2 = Fraction.getFraction(1, 3);
        result = f1.subtract(f2);
        Assert.assertEquals(1, result.getNumerator());
        Assert.assertEquals(6, result.getDenominator());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubtract_Null() {
        Fraction.ONE.subtract(null);
    }

    @Test(expected = ArithmeticException.class)
    public void testSubtract_OverflowD1Equals1() {
        Fraction.getFraction(Integer.MIN_VALUE + 1, 1).subtract(Fraction.getFraction(2, 1));
    }

    @Test
    public void testMultiplyBy() {
        Fraction f1 = Fraction.getFraction(2, 3);
        Fraction f2 = Fraction.getFraction(3, 4);
        Fraction result = f1.multiplyBy(f2);
        Assert.assertEquals(1, result.getNumerator());
        Assert.assertEquals(2, result.getDenominator());

        Assert.assertSame(Fraction.ZERO, f1.multiplyBy(Fraction.ZERO));
        Assert.assertSame(Fraction.ZERO, Fraction.ZERO.multiplyBy(f2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMultiplyBy_Null() {
        Fraction.ONE.multiplyBy(null);
    }

    @Test(expected = ArithmeticException.class)
    public void testMultiplyBy_OverflowNumerator() {
        Fraction.getFraction(Integer.MAX_VALUE, 1).multiplyBy(Fraction.getFraction(2, 1));
    }

    @Test(expected = ArithmeticException.class)
    public void testMultiplyBy_OverflowDenominator() {
        Fraction.getFraction(1, Integer.MAX_VALUE).multiplyBy(Fraction.getFraction(1, 2));
    }

    @Test
    public void testDivideBy() {
        Fraction f1 = Fraction.getFraction(1, 2);
        Fraction f2 = Fraction.getFraction(3, 4);
        Fraction result = f1.divideBy(f2);
        Assert.assertEquals(2, result.getNumerator());
        Assert.assertEquals(3, result.getDenominator());

        Assert.assertEquals(Fraction.ZERO, Fraction.ZERO.divideBy(f1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDivideBy_Null() {
        Fraction.ONE.divideBy(null);
    }

    @Test(expected = ArithmeticException.class)
    public void testDivideBy_Zero() {
        Fraction.ONE.divideBy(Fraction.ZERO);
    }

    @Test
    public void testEqualsAndHashCode() {
        Fraction f1 = Fraction.getFraction(1, 2);
        Fraction f2 = Fraction.getFraction(1, 2);
        Fraction f3 = Fraction.getFraction(2, 4);
        Fraction f4 = Fraction.getFraction(1, 3);

        Assert.assertTrue(f1.equals(f1));
        Assert.assertTrue(f1.equals(f2));
        Assert.assertFalse(f1.equals(f3));
        Assert.assertFalse(f1.equals(f4));
        Assert.assertFalse(f1.equals(null));
        Assert.assertFalse(f1.equals("1/2"));

        Assert.assertEquals(f1.hashCode(), f2.hashCode());
        Assert.assertTrue(f1.hashCode() != f4.hashCode());
    }

    @Test
    public void testCompareTo() {
        Fraction f1 = Fraction.getFraction(1, 2);
        Fraction f2 = Fraction.getFraction(2, 4);
        Fraction f3 = Fraction.getFraction(1, 3);
        Fraction f4 = Fraction.getFraction(2, 3);

        Assert.assertEquals(0, f1.compareTo(f1));
        Assert.assertEquals(0, f1.compareTo(f2));
        Assert.assertTrue(f1.compareTo(f3) > 0);
        Assert.assertTrue(f1.compareTo(f4) < 0);

        Fraction fn1 = Fraction.getFraction(-1, 2);
        Fraction fn2 = Fraction.getFraction(-1, 3);
        Assert.assertTrue(fn1.compareTo(fn2) < 0);
    }

    @Test(expected = NullPointerException.class)
    public void testCompareTo_Null() {
        Fraction.ONE.compareTo(null);
    }

    @Test
    public void testToString() {
        Fraction f = Fraction.getFraction(3, 4);
        Assert.assertEquals("3/4", f.toString());
        // Verify caching path
        Assert.assertEquals("3/4", f.toString());

        f = Fraction.getFraction(-3, 4);
        Assert.assertEquals("-3/4", f.toString());
    }

    @Test
    public void testToProperString() {
        Fraction f = Fraction.ZERO;
        Assert.assertEquals("0", f.toProperString());

        f = Fraction.ONE;
        Assert.assertEquals("1", f.toProperString());

        f = Fraction.getFraction(-1, 1);
        Assert.assertEquals("-1", f.toProperString());

        f = Fraction.getFraction(4, 2);
        Assert.assertEquals("2", f.toProperString());

        f = Fraction.getFraction(-4, 2);
        Assert.assertEquals("-2", f.toProperString());

        f = Fraction.getFraction(7, 4);
        Assert.assertEquals("1 3/4", f.toProperString());

        f = Fraction.getFraction(-7, 4);
        Assert.assertEquals("-1 3/4", f.toProperString());

        f = Fraction.getFraction(3, 4);
        Assert.assertEquals("3/4", f.toProperString());

        f = Fraction.getFraction(-3, 4);
        Assert.assertEquals("-3/4", f.toProperString());

        // Verify caching path
        Assert.assertEquals("-3/4", f.toProperString());

        f = Fraction.getFraction(Integer.MIN_VALUE, 1);
        Assert.assertEquals(Integer.toString(Integer.MIN_VALUE), f.toProperString());
    }

    @Test
    public void testGreatestCommonDivisorBranches() {
        Fraction f1 = Fraction.getFraction(1, 3);
        Fraction f2 = Fraction.getFraction(2, 3);
        Fraction res = f1.add(f2);
        Assert.assertEquals(Fraction.ONE, res);

        f1 = Fraction.getFraction(7, 12);
        f2 = Fraction.getFraction(1, 18);
        res = f1.add(f2);
        Assert.assertEquals(Fraction.getFraction(23, 36), res);

        f1 = Fraction.getFraction(1, 1);
        f2 = Fraction.getFraction(1, 1);
        res = f1.add(f2);
        Assert.assertEquals(Fraction.getFraction(2, 1), res);
    }
}
