package org.apache.commons.lang.math;

import org.junit.Assert;
import org.junit.Test;

public class FractionTest {

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
    public void testGetFraction_int_int() {
        Fraction f = Fraction.getFraction(3, 4);
        Assert.assertEquals(3, f.getNumerator());
        Assert.assertEquals(4, f.getDenominator());

        f = Fraction.getFraction(3, -4);
        Assert.assertEquals(-3, f.getNumerator());
        Assert.assertEquals(4, f.getDenominator());

        f = Fraction.getFraction(-3, -4);
        Assert.assertEquals(3, f.getNumerator());
        Assert.assertEquals(4, f.getDenominator());

        f = Fraction.getFraction(Integer.MIN_VALUE, 1);
        Assert.assertEquals(Integer.MIN_VALUE, f.getNumerator());
        Assert.assertEquals(1, f.getDenominator());
    }

    @Test(expected = ArithmeticException.class)
    public void testGetFraction_int_int_ZeroDenominator() {
        Fraction.getFraction(1, 0);
    }

    @Test(expected = ArithmeticException.class)
    public void testGetFraction_int_int_OverflowMinNum() {
        Fraction.getFraction(Integer.MIN_VALUE, -1);
    }

    @Test(expected = ArithmeticException.class)
    public void testGetFraction_int_int_OverflowMinDenom() {
        Fraction.getFraction(1, Integer.MIN_VALUE);
    }

    @Test
    public void testGetFraction_int_int_int() {
        Fraction f = Fraction.getFraction(1, 1, 2);
        Assert.assertEquals(3, f.getNumerator());
        Assert.assertEquals(2, f.getDenominator());

        f = Fraction.getFraction(-1, 1, 2);
        Assert.assertEquals(-3, f.getNumerator());
        Assert.assertEquals(2, f.getDenominator());

        f = Fraction.getFraction(0, 0, 1);
        Assert.assertEquals(0, f.getNumerator());
        Assert.assertEquals(1, f.getDenominator());
    }

    @Test(expected = ArithmeticException.class)
    public void testGetFraction_int_int_int_ZeroDenom() {
        Fraction.getFraction(1, 1, 0);
    }

    @Test(expected = ArithmeticException.class)
    public void testGetFraction_int_int_int_NegativeDenom() {
        Fraction.getFraction(1, 1, -2);
    }

    @Test(expected = ArithmeticException.class)
    public void testGetFraction_int_int_int_NegativeNum() {
        Fraction.getFraction(1, -1, 2);
    }

    @Test(expected = ArithmeticException.class)
    public void testGetFraction_int_int_int_OverflowPositive() {
        Fraction.getFraction(Integer.MAX_VALUE, 1, 2);
    }

    @Test(expected = ArithmeticException.class)
    public void testGetFraction_int_int_int_OverflowNegative() {
        Fraction.getFraction(Integer.MIN_VALUE, 1, 2);
    }

    @Test
    public void testGetReducedFraction() {
        Fraction f = Fraction.getReducedFraction(0, 5);
        Assert.assertSame(Fraction.ZERO, f);

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
        Assert.assertEquals(1, f.getNumerator());
        Assert.assertEquals(-(Integer.MIN_VALUE / 4), f.getDenominator());
    }

    @Test(expected = ArithmeticException.class)
    public void testGetReducedFraction_ZeroDenom() {
        Fraction.getReducedFraction(1, 0);
    }

    @Test(expected = ArithmeticException.class)
    public void testGetReducedFraction_OverflowMinNum() {
        Fraction.getReducedFraction(Integer.MIN_VALUE, -1);
    }

    @Test(expected = ArithmeticException.class)
    public void testGetReducedFraction_OverflowMinDenom() {
        Fraction.getReducedFraction(1, Integer.MIN_VALUE);
    }

    @Test
    public void testGetFraction_double() {
        Fraction f = Fraction.getFraction(0.0);
        Assert.assertEquals(0, f.getNumerator());
        Assert.assertEquals(1, f.getDenominator());

        f = Fraction.getFraction(0.5);
        Assert.assertEquals(1, f.getNumerator());
        Assert.assertEquals(2, f.getDenominator());

        f = Fraction.getFraction(-0.5);
        Assert.assertEquals(-1, f.getNumerator());
        Assert.assertEquals(2, f.getDenominator());

        f = Fraction.getFraction(1.75);
        Assert.assertEquals(7, f.getNumerator());
        Assert.assertEquals(4, f.getDenominator());

        f = Fraction.getFraction(12345.0);
        Assert.assertEquals(12345, f.getNumerator());
        Assert.assertEquals(1, f.getDenominator());
    }

    @Test(expected = ArithmeticException.class)
    public void testGetFraction_double_NaN() {
        Fraction.getFraction(Double.NaN);
    }

    @Test(expected = ArithmeticException.class)
    public void testGetFraction_double_TooLarge() {
        Fraction.getFraction((double) Integer.MAX_VALUE + 100.0);
    }

    @Test(expected = ArithmeticException.class)
    public void testGetFraction_double_NonConvergent() {
        Fraction.getFraction(0.4999999999999999);
    }

    @Test
    public void testGetFraction_String() {
        Fraction f = Fraction.getFraction("0.5");
        Assert.assertEquals(1, f.getNumerator());
        Assert.assertEquals(2, f.getDenominator());

        f = Fraction.getFraction("1 1/2");
        Assert.assertEquals(3, f.getNumerator());
        Assert.assertEquals(2, f.getDenominator());

        f = Fraction.getFraction("-1 1/2");
        Assert.assertEquals(-3, f.getNumerator());
        Assert.assertEquals(2, f.getDenominator());

        f = Fraction.getFraction("3/4");
        Assert.assertEquals(3, f.getNumerator());
        Assert.assertEquals(4, f.getDenominator());

        f = Fraction.getFraction("5");
        Assert.assertEquals(5, f.getNumerator());
        Assert.assertEquals(1, f.getDenominator());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetFraction_String_Null() {
        Fraction.getFraction(null);
    }

    @Test(expected = NumberFormatException.class)
    public void testGetFraction_String_InvalidSpaceFormat() {
        Fraction.getFraction("1 2");
    }

    @Test(expected = NumberFormatException.class)
    public void testGetFraction_String_InvalidFormat() {
        Fraction.getFraction("abc");
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
        Assert.assertEquals(1.75f, f.floatValue(), 0.0001f);
        Assert.assertEquals(1.75d, f.doubleValue(), 0.0001d);

        Fraction fn = Fraction.getFraction(-7, 4);
        Assert.assertEquals(-7, fn.getNumerator());
        Assert.assertEquals(4, fn.getDenominator());
        Assert.assertEquals(3, fn.getProperNumerator());
        Assert.assertEquals(-1, fn.getProperWhole());
    }

    @Test
    public void testReduce() {
        Fraction f = Fraction.getFraction(2, 4);
        Fraction red = f.reduce();
        Assert.assertEquals(1, red.getNumerator());
        Assert.assertEquals(2, red.getDenominator());

        Fraction alreadyReduced = Fraction.getFraction(1, 2);
        Assert.assertSame(alreadyReduced, alreadyReduced.reduce());
    }

    @Test
    public void testInvert() {
        Fraction f = Fraction.getFraction(3, 4);
        Fraction inv = f.invert();
        Assert.assertEquals(4, inv.getNumerator());
        Assert.assertEquals(3, inv.getDenominator());

        f = Fraction.getFraction(-3, 4);
        inv = f.invert();
        Assert.assertEquals(-4, inv.getNumerator());
        Assert.assertEquals(3, inv.getDenominator());
    }

    @Test(expected = ArithmeticException.class)
    public void testInvert_Zero() {
        Fraction.ZERO.invert();
    }

    @Test(expected = ArithmeticException.class)
    public void testInvert_MinNumerator() {
        Fraction.getFraction(Integer.MIN_VALUE, 1).invert();
    }

    @Test
    public void testNegate() {
        Fraction f = Fraction.getFraction(3, 4);
        Fraction neg = f.negate();
        Assert.assertEquals(-3, neg.getNumerator());
        Assert.assertEquals(4, neg.getDenominator());

        f = Fraction.getFraction(-3, 4);
        neg = f.negate();
        Assert.assertEquals(3, neg.getNumerator());
        Assert.assertEquals(4, neg.getDenominator());
    }

    @Test(expected = ArithmeticException.class)
    public void testNegate_MinNumerator() {
        Fraction.getFraction(Integer.MIN_VALUE, 1).negate();
    }

    @Test
    public void testAbs() {
        Fraction f = Fraction.getFraction(-3, 4);
        Fraction abs = f.abs();
        Assert.assertEquals(3, abs.getNumerator());
        Assert.assertEquals(4, abs.getDenominator());

        f = Fraction.getFraction(3, 4);
        Assert.assertSame(f, f.abs());
    }

    @Test
    public void testPow() {
        Fraction f = Fraction.getFraction(2, 3);
        Assert.assertSame(f, f.pow(1));
        Assert.assertEquals(Fraction.ONE, f.pow(0));
        Assert.assertEquals(Fraction.ONE, Fraction.ZERO.pow(0));

        Fraction pow2 = f.pow(2);
        Assert.assertEquals(4, pow2.getNumerator());
        Assert.assertEquals(9, pow2.getDenominator());

        Fraction pow3 = f.pow(3);
        Assert.assertEquals(8, pow3.getNumerator());
        Assert.assertEquals(27, pow3.getDenominator());

        Fraction powNeg2 = f.pow(-2);
        Assert.assertEquals(9, powNeg2.getNumerator());
        Assert.assertEquals(4, powNeg2.getDenominator());

        Fraction powNeg1 = f.pow(-1);
        Assert.assertEquals(3, powNeg1.getNumerator());
        Assert.assertEquals(2, powNeg1.getDenominator());

        Fraction one = Fraction.ONE.pow(Integer.MIN_VALUE);
        Assert.assertEquals(Fraction.ONE, one);
    }

    @Test(expected = ArithmeticException.class)
    public void testPow_Overflow() {
        Fraction.getFraction(Integer.MAX_VALUE, 1).pow(2);
    }

    @Test
    public void testAdd() {
        Fraction f1 = Fraction.getFraction(1, 3);
        Fraction f2 = Fraction.getFraction(2, 3);
        Fraction res = f1.add(f2);
        Assert.assertEquals(1, res.getNumerator());
        Assert.assertEquals(1, res.getDenominator());

        res = f1.add(Fraction.ZERO);
        Assert.assertSame(f1, res);

        res = Fraction.ZERO.add(f1);
        Assert.assertSame(f1, res);

        Fraction f3 = Fraction.getFraction(1, 2);
        Fraction f4 = Fraction.getFraction(1, 3);
        res = f3.add(f4);
        Assert.assertEquals(5, res.getNumerator());
        Assert.assertEquals(6, res.getDenominator());

        Fraction f5 = Fraction.getFraction(1, 4);
        Fraction f6 = Fraction.getFraction(1, 6);
        res = f5.add(f6);
        Assert.assertEquals(5, res.getNumerator());
        Assert.assertEquals(12, res.getDenominator());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAdd_Null() {
        Fraction.ONE.add(null);
    }

    @Test
    public void testSubtract() {
        Fraction f1 = Fraction.getFraction(2, 3);
        Fraction f2 = Fraction.getFraction(1, 3);
        Fraction res = f1.subtract(f2);
        Assert.assertEquals(1, res.getNumerator());
        Assert.assertEquals(3, res.getDenominator());

        res = f1.subtract(Fraction.ZERO);
        Assert.assertSame(f1, res);

        res = Fraction.ZERO.subtract(f1);
        Assert.assertEquals(-2, res.getNumerator());
        Assert.assertEquals(3, res.getDenominator());

        Fraction f3 = Fraction.getFraction(1, 2);
        Fraction f4 = Fraction.getFraction(1, 3);
        res = f3.subtract(f4);
        Assert.assertEquals(1, res.getNumerator());
        Assert.assertEquals(6, res.getDenominator());

        Fraction f5 = Fraction.getFraction(1, 4);
        Fraction f6 = Fraction.getFraction(1, 6);
        res = f5.subtract(f6);
        Assert.assertEquals(1, res.getNumerator());
        Assert.assertEquals(12, res.getDenominator());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubtract_Null() {
        Fraction.ONE.subtract(null);
    }

    @Test(expected = ArithmeticException.class)
    public void testAdd_OverflowD1Equals1() {
        Fraction f1 = Fraction.getFraction(Integer.MAX_VALUE - 1, 1);
        Fraction f2 = Fraction.getFraction(2, 1);
        f1.add(f2);
    }

    @Test(expected = ArithmeticException.class)
    public void testAdd_OverflowBigNumerator() {
        Fraction f1 = Fraction.getFraction(Integer.MAX_VALUE, 2);
        Fraction f2 = Fraction.getFraction(Integer.MAX_VALUE, 2);
        f1.add(f2);
    }

    @Test(expected = ArithmeticException.class)
    public void testSubtract_Overflow() {
        Fraction f1 = Fraction.getFraction(Integer.MIN_VALUE, 2);
        Fraction f2 = Fraction.getFraction(1, 2);
        f1.subtract(f2);
    }

    @Test
    public void testMultiplyBy() {
        Fraction f1 = Fraction.getFraction(2, 3);
        Fraction f2 = Fraction.getFraction(3, 4);
        Fraction res = f1.multiplyBy(f2);
        Assert.assertEquals(1, res.getNumerator());
        Assert.assertEquals(2, res.getDenominator());

        res = f1.multiplyBy(Fraction.ZERO);
        Assert.assertSame(Fraction.ZERO, res);

        res = Fraction.ZERO.multiplyBy(f1);
        Assert.assertSame(Fraction.ZERO, res);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMultiplyBy_Null() {
        Fraction.ONE.multiplyBy(null);
    }

    @Test(expected = ArithmeticException.class)
    public void testMultiplyBy_Overflow() {
        Fraction f1 = Fraction.getFraction(Integer.MAX_VALUE, 2);
        Fraction f2 = Fraction.getFraction(2, 1);
        f1.multiplyBy(f2);
    }

    @Test
    public void testDivideBy() {
        Fraction f1 = Fraction.getFraction(1, 2);
        Fraction f2 = Fraction.getFraction(3, 4);
        Fraction res = f1.divideBy(f2);
        Assert.assertEquals(2, res.getNumerator());
        Assert.assertEquals(3, res.getDenominator());

        res = Fraction.ZERO.divideBy(f1);
        Assert.assertSame(Fraction.ZERO, res);
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

        Assert.assertTrue(f1.equals(f1));
        Assert.assertTrue(f1.equals(f2));
        Assert.assertFalse(f1.equals(f3));
        Assert.assertFalse(f1.equals(null));
        Assert.assertFalse(f1.equals("1/2"));

        Assert.assertEquals(f1.hashCode(), f2.hashCode());
        Assert.assertEquals(f1.hashCode(), f1.hashCode());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCompareTo() {
        Fraction f1 = Fraction.getFraction(1, 2);
        Fraction f2 = Fraction.getFraction(2, 4);
        Fraction f3 = Fraction.getFraction(1, 3);
        Fraction f4 = Fraction.getFraction(2, 3);

        Assert.assertEquals(0, f1.compareTo(f1));
        Assert.assertEquals(0, f1.compareTo(f2));
        Assert.assertEquals(1, f1.compareTo(f3));
        Assert.assertEquals(-1, f1.compareTo(f4));
    }

    @Test(expected = NullPointerException.class)
    @SuppressWarnings("unchecked")
    public void testCompareTo_Null() {
        Fraction.ONE.compareTo(null);
    }

    @Test(expected = ClassCastException.class)
    @SuppressWarnings("unchecked")
    public void testCompareTo_InvalidObject() {
        Fraction.ONE.compareTo("string");
    }

    @Test
    public void testToString() {
        Fraction f = Fraction.getFraction(3, 4);
        Assert.assertEquals("3/4", f.toString());
        Assert.assertEquals("3/4", f.toString());
    }

    @Test
    public void testToProperString() {
        Assert.assertEquals("0", Fraction.ZERO.toProperString());
        Assert.assertEquals("1", Fraction.ONE.toProperString());
        Assert.assertEquals("-1", Fraction.getFraction(-1, 1).toProperString());
        Assert.assertEquals("2", Fraction.getFraction(4, 2).toProperString());
        Assert.assertEquals("-2", Fraction.getFraction(-4, 2).toProperString());
        Assert.assertEquals("1 1/2", Fraction.getFraction(3, 2).toProperString());
        Assert.assertEquals("-1 1/2", Fraction.getFraction(-3, 2).toProperString());
        Assert.assertEquals("1/2", Fraction.getFraction(1, 2).toProperString());
        Assert.assertEquals("-1/2", Fraction.getFraction(-1, 2).toProperString());
        Fraction f = Fraction.getFraction(3, 2);
        Assert.assertEquals("1 1/2", f.toProperString());
        Assert.assertEquals("1 1/2", f.toProperString());
    }
}
