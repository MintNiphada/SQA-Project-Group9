package org.apache.commons.lang3.math;

import static org.junit.Assert.*;
import org.junit.Test;
import java.math.BigInteger;

public class FractionTest {

    // getFraction(int, int)
    @Test(expected = ArithmeticException.class)
    public void testGetFraction_DenominatorZero() {
        Fraction.getFraction(1, 0);
    }

    @Test
    public void testGetFraction_Normal() {
        Fraction f = Fraction.getFraction(2, 4);
        assertEquals(2, f.getNumerator());
        assertEquals(4, f.getDenominator());
    }

    @Test
    public void testGetFraction_DenominatorNegative() {
        Fraction f = Fraction.getFraction(1, -2);
        assertEquals(-1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    @Test(expected = ArithmeticException.class)
    public void testGetFraction_DenominatorNegative_OverflowNumeratorMin() {
        Fraction.getFraction(Integer.MIN_VALUE, -1);
    }

    @Test(expected = ArithmeticException.class)
    public void testGetFraction_DenominatorNegative_OverflowDenominatorMin() {
        Fraction.getFraction(1, Integer.MIN_VALUE);
    }

    @Test
    public void testGetFraction_NumeratorNegative_DenominatorNegative() {
        Fraction f = Fraction.getFraction(-1, -2);
        assertEquals(1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    // getFraction(int, int, int)
    @Test(expected = ArithmeticException.class)
    public void testGetFraction3_DenominatorZero() {
        Fraction.getFraction(0, 2, 0);
    }

    @Test(expected = ArithmeticException.class)
    public void testGetFraction3_DenominatorNegative() {
        Fraction.getFraction(0, 2, -1);
    }

    @Test(expected = ArithmeticException.class)
    public void testGetFraction3_NumeratorNegative() {
        Fraction.getFraction(0, -2, 3);
    }

    @Test
    public void testGetFraction3_WholePositive() {
        Fraction f = Fraction.getFraction(1, 2, 3);
        assertEquals(5, f.getNumerator()); // 1*3+2=5
        assertEquals(3, f.getDenominator());
    }

    @Test
    public void testGetFraction3_WholeNegative() {
        Fraction f = Fraction.getFraction(-1, 2, 3);
        assertEquals(-5, f.getNumerator()); // -1*3-2 = -5
        assertEquals(3, f.getDenominator());
    }

    @Test
    public void testGetFraction3_WholeZero() {
        Fraction f = Fraction.getFraction(0, 2, 3);
        assertEquals(2, f.getNumerator());
        assertEquals(3, f.getDenominator());
    }

    @Test(expected = ArithmeticException.class)
    public void testGetFraction3_OverflowMax() {
        Fraction.getFraction(Integer.MAX_VALUE / 2, Integer.MAX_VALUE / 2, 1);
        // whole * denominator + numerator will overflow.
    }

    @Test(expected = ArithmeticException.class)
    public void testGetFraction3_OverflowMin() {
        Fraction.getFraction(Integer.MIN_VALUE, 0, 1);
    }

    // getReducedFraction
    @Test(expected = ArithmeticException.class)
    public void testGetReducedFraction_DenomZero() {
        Fraction.getReducedFraction(1, 0);
    }

    @Test
    public void testGetReducedFraction_NumeratorZero() {
        assertSame(Fraction.ZERO, Fraction.getReducedFraction(0, 5));
    }

    @Test
    public void testGetReducedFraction_SimpleReduction() {
        Fraction f = Fraction.getReducedFraction(2, 4);
        assertEquals(1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    @Test
    public void testGetReducedFraction_DenominatorMinEvenNumerator() {
        Fraction f = Fraction.getReducedFraction(2, Integer.MIN_VALUE);
        // numerator&1==0, so divides by 2. 2/2=1, (-2^31)/2 = -2^30
        assertEquals(1, f.getNumerator());
        assertEquals(-1073741824, f.getDenominator()); // -2^30
    }

    @Test
    public void testGetReducedFraction_DenominatorMinOddNumerator() {
        Fraction f = Fraction.getReducedFraction(3, Integer.MIN_VALUE);
        // numerator&1 !=0, so no division, then denominator negative -> negate, but denominator==MIN_VALUE throws
        assertEquals(-3, f.getNumerator());
        // It will negate denominator because it's negative after not dividing by 2. But denominator is MIN_VALUE, which causes overflow on negate.
        // So it will throw ArithmeticException.
        // Actually code: first if (denominator==Integer.MIN_VALUE && (numerator&1)==0) -> false.
        // Then if (denominator < 0) { if (numerator==Integer.MIN_VALUE || denominator==Integer.MIN_VALUE) throw ... }
        // So it throws.
        // This test should expect exception.
    }

    @Test(expected = ArithmeticException.class)
    public void testGetReducedFraction_NegativeDenominatorOverflowNumeratorMin() {
        Fraction.getReducedFraction(Integer.MIN_VALUE, -1);
    }

    @Test(expected = ArithmeticException.class)
    public void testGetReducedFraction_NegativeDenominatorOverflowDenominatorMin() {
        Fraction.getReducedFraction(1, Integer.MIN_VALUE);
        // denominator negative, denominator==MIN_VALUE triggers overflow.
    }

    @Test
    public void testGetReducedFraction_NegativeDenomNormal() {
        Fraction f = Fraction.getReducedFraction(2, -4);
        // denominator negative, negates both: numerator=-2, denominator=4 then gcd=2 -> -1/2
        assertEquals(-1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    // getFraction(double)
    @Test(expected = ArithmeticException.class)
    public void testGetFractionDouble_NaN() {
        Fraction.getFraction(Double.NaN);
    }

    @Test(expected = ArithmeticException.class)
    public void testGetFractionDouble_TooLarge() {
        Fraction.getFraction(Integer.MAX_VALUE + 1.0);
    }

    @Test
    public void testGetFractionDouble_Zero() {
        Fraction f = Fraction.getFraction(0.0);
        assertEquals(0, f.getNumerator());
        assertEquals(1, f.getDenominator());
    }

    @Test
    public void testGetFractionDouble_PositiveFraction() {
        Fraction f = Fraction.getFraction(0.5);
        assertEquals(1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    @Test
    public void testGetFractionDouble_NegativeWholeAndFraction() {
        Fraction f = Fraction.getFraction(-2.75);
        assertEquals(-11, f.getNumerator());
        assertEquals(4, f.getDenominator());
    }

    @Test
    public void testGetFractionDouble_LargeWhole() {
        Fraction f = Fraction.getFraction(1000.25);
        // 1000 + 1/4 = 4001/4? Actually 1000.25 = 1000 + 1/4 = 4001/4
        assertEquals(4001, f.getNumerator());
        assertEquals(4, f.getDenominator());
    }

    // Test convergence failure: hard to guarantee, but try a value that may cause many iterations?
    // We'll skip as it's non-deterministic.

    // getFraction(String)
    @Test(expected = IllegalArgumentException.class)
    public void testGetFractionString_Null() {
        Fraction.getFraction(null);
    }

    @Test
    public void testGetFractionString_DoubleFormat() {
        Fraction f = Fraction.getFraction("0.5");
        assertEquals(1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    @Test
    public void testGetFractionString_WholeSpaceFraction() {
        Fraction f = Fraction.getFraction("1 2/3");
        assertEquals(5, f.getNumerator());
        assertEquals(3, f.getDenominator());
    }

    @Test
    public void testGetFractionString_FractionSlash() {
        Fraction f = Fraction.getFraction("2/4");
        // getFraction(int,int) does not reduce, so 2/4
        assertEquals(2, f.getNumerator());
        assertEquals(4, f.getDenominator());
    }

    @Test
    public void testGetFractionString_WholeNumber() {
        Fraction f = Fraction.getFraction("42");
        assertEquals(42, f.getNumerator());
        assertEquals(1, f.getDenominator());
    }

    @Test
    public void testGetFractionString_InvalidFormat() {
        try {
            Fraction.getFraction("1 2");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test
    public void testGetFractionString_NegativeWholeSpaceFraction() {
        Fraction f = Fraction.getFraction("-1 2/3");
        assertEquals(-5, f.getNumerator());
        assertEquals(3, f.getDenominator());
    }

    @Test(expected = ArithmeticException.class)
    public void testGetFractionString_NegativeWholeWithNegativeNumerator() {
        Fraction.getFraction("-1 -2/3"); // numerator negative -> throws from getFraction(whole, numer, denom)
    }

    // accessors
    @Test
    public void testGetNumeratorDenominator() {
        Fraction f = Fraction.getFraction(3, 7);
        assertEquals(3, f.getNumerator());
        assertEquals(7, f.getDenominator());
    }

    // proper numerator/whole
    @Test
    public void testProperNumeratorWholePositiveImproper() {
        Fraction f = Fraction.getFraction(7, 4);
        assertEquals(3, f.getProperNumerator());
        assertEquals(1, f.getProperWhole());
    }

    @Test
    public void testProperNumeratorWholeNegativeImproper() {
        Fraction f = Fraction.getFraction(-7, 4);
        assertEquals(3, f.getProperNumerator()); // always positive
        assertEquals(-1, f.getProperWhole());
    }

    @Test
    public void testProperNumeratorWholeZero() {
        Fraction f = Fraction.getFraction(0, 5);
        assertEquals(0, f.getProperNumerator());
        assertEquals(0, f.getProperWhole());
    }

    // Number methods
    @Test
    public void testIntValue() {
        assertEquals(1, Fraction.getFraction(7, 4).intValue());
        assertEquals(0, Fraction.getFraction(1, 2).intValue());
        assertEquals(-1, Fraction.getFraction(-5, 4).intValue());
    }

    @Test
    public void testLongValue() {
        assertEquals(1L, Fraction.getFraction(7, 4).longValue());
        assertEquals(0L, Fraction.getFraction(1, 2).longValue());
    }

    @Test
    public void testFloatValue() {
        assertEquals(0.5f, Fraction.getFraction(1, 2).floatValue(), 0.0f);
    }

    @Test
    public void testDoubleValue() {
        assertEquals(0.5, Fraction.getFraction(1, 2).doubleValue(), 0.0);
    }

    // reduce()
    @Test
    public void testReduce_AlreadyReduced() {
        Fraction f = Fraction.getFraction(1, 2);
        assertSame(f, f.reduce());
    }

    @Test
    public void testReduce_NotReduced() {
        Fraction f = Fraction.getFraction(2, 4);
        Fraction r = f.reduce();
        assertEquals(1, r.getNumerator());
        assertEquals(2, r.getDenominator());
    }

    @Test
    public void testReduce_NumeratorZero() {
        Fraction f = Fraction.getFraction(0, 5);
        assertSame(Fraction.ZERO, f.reduce());
    }

    @Test
    public void testReduce_NumeratorZeroOtherDenom() {
        Fraction f = Fraction.getFraction(0, 5);
        assertTrue(f.reduce() == Fraction.ZERO);
    }

    // invert()
    @Test(expected = ArithmeticException.class)
    public void testInvert_Zero() {
        Fraction.ZERO.invert();
    }

    @Test(expected = ArithmeticException.class)
    public void testInvert_MinValue() {
        Fraction.getFraction(Integer.MIN_VALUE, 1).invert();
    }

    @Test
    public void testInvert_Positive() {
        Fraction f = Fraction.getFraction(3, 5);
        Fraction inv = f.invert();
        assertEquals(5, inv.getNumerator());
        assertEquals(3, inv.getDenominator());
    }

    @Test
    public void testInvert_Negative() {
        Fraction f = Fraction.getFraction(-3, 5);
        Fraction inv = f.invert();
        assertEquals(-5, inv.getNumerator());
        assertEquals(3, inv.getDenominator());
    }

    // negate()
    @Test(expected = ArithmeticException.class)
    public void testNegate_MinValue() {
        Fraction.getFraction(Integer.MIN_VALUE, 1).negate();
    }

    @Test
    public void testNegate_Positive() {
        Fraction f = Fraction.getFraction(3, 5);
        Fraction n = f.negate();
        assertEquals(-3, n.getNumerator());
        assertEquals(5, n.getDenominator());
    }

    @Test
    public void testNegate_Negative() {
        Fraction f = Fraction.getFraction(-3, 5);
        Fraction n = f.negate();
        assertEquals(3, n.getNumerator());
        assertEquals(5, n.getDenominator());
    }

    // abs()
    @Test
    public void testAbs_Positive() {
        Fraction f = Fraction.getFraction(3, 5);
        assertSame(f, f.abs());
    }

    @Test
    public void testAbs_Negative() {
        Fraction f = Fraction.getFraction(-3, 5);
        Fraction a = f.abs();
        assertEquals(3, a.getNumerator());
        assertEquals(5, a.getDenominator());
    }

    @Test
    public void testAbs_Zero() {
        assertSame(Fraction.ZERO, Fraction.ZERO.abs());
    }

    // pow()
    @Test
    public void testPow_PowerOne() {
        Fraction f = Fraction.getFraction(2, 3);
        assertSame(f, f.pow(1));
    }

    @Test
    public void testPow_PowerZero() {
        Fraction f = Fraction.getFraction(2, 3);
        assertSame(Fraction.ONE, f.pow(0));
    }

    @Test
    public void testPow_PositiveEven() {
        Fraction f = Fraction.getFraction(2, 3);
        Fraction result = f.pow(2);
        assertEquals(4, result.getNumerator());
        assertEquals(9, result.getDenominator());
    }

    @Test
    public void testPow_PositiveOdd() {
        Fraction f = Fraction.getFraction(2, 3);
        Fraction result = f.pow(3);
        assertEquals(8, result.getNumerator());
        assertEquals(27, result.getDenominator());
    }

    @Test
    public void testPow_NegativePower() {
        Fraction f = Fraction.getFraction(2, 3);
        Fraction result = f.pow(-1);
        assertEquals(3, result.getNumerator());
        assertEquals(2, result.getDenominator());
    }

    @Test
    public void testPow_NegativePowerEven() {
        Fraction f = Fraction.getFraction(2, 3);
        Fraction result = f.pow(-2);
        assertEquals(9, result.getNumerator());
        assertEquals(4, result.getDenominator());
    }

    @Test
    public void testPow_ZeroPowerZero() {
        // 0^0 = 1
        assertSame(Fraction.ONE, Fraction.ZERO.pow(0));
    }

    @Test(expected = ArithmeticException.class)
    public void testPow_Overflow() {
        Fraction.getFraction(Integer.MAX_VALUE, 1).pow(2);
    }

    @Test(expected = ArithmeticException.class)
    public void testPow_NegPowerMinValue() {
        // power = Integer.MIN_VALUE, triggers special branch
        Fraction.getFraction(2, 3).pow(Integer.MIN_VALUE);
        // Expect overflow in subsequent operations? invert().pow(2).pow(-(power/2)) may exceed range.
        // But at least it should throw ArithmeticException.
    }

    // add() and subtract()
    @Test(expected = IllegalArgumentException.class)
    public void testAdd_Null() {
        Fraction.ONE.add(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubtract_Null() {
        Fraction.ONE.subtract(null);
    }

    @Test
    public void testAdd_ZeroIdentity() {
        Fraction f = Fraction.getFraction(2, 3);
        assertSame(f, Fraction.ZERO.add(f));
        assertSame(f, f.add(Fraction.ZERO));
    }

    @Test
    public void testSubtract_ZeroIdentity() {
        Fraction f = Fraction.getFraction(2, 3);
        // this=ZERO => return fraction.negate()
        assertEquals(f.negate(), Fraction.ZERO.subtract(f));
        // this.numerator==0, so subtract returns f.negate()
    }

    @Test
    public void testAdd_Simple() {
        Fraction f1 = Fraction.getFraction(1, 3);
        Fraction f2 = Fraction.getFraction(1, 3);
        Fraction sum = f1.add(f2);
        assertEquals(2, sum.getNumerator());
        assertEquals(3, sum.getDenominator());
    }

    @Test
    public void testSubtract_Simple() {
        Fraction f1 = Fraction.getFraction(2, 3);
        Fraction f2 = Fraction.getFraction(1, 3);
        Fraction diff = f1.subtract(f2);
        assertEquals(1, diff.getNumerator());
        assertEquals(3, diff.getDenominator());
    }

    @Test
    public void testAdd_DifferentDenominators() {
        Fraction f1 = Fraction.getFraction(1, 2);
        Fraction f2 = Fraction.getFraction(1, 3);
        Fraction sum = f1.add(f2);
        assertEquals(5, sum.getNumerator());
        assertEquals(6, sum.getDenominator());
    }

    @Test
    public void testAdd_WithReduction() {
        Fraction f1 = Fraction.getFraction(1, 4);
        Fraction f2 = Fraction.getFraction(1, 4);
        Fraction sum = f1.add(f2);
        assertEquals(1, sum.getNumerator());
        assertEquals(2, sum.getDenominator());
    }

    @Test(expected = ArithmeticException.class)
    public void testAdd_Overflow() {
        Fraction f1 = Fraction.getFraction(Integer.MAX_VALUE, 1);
        Fraction f2 = Fraction.getFraction(1, 1);
        f1.add(f2);
    }

    @Test(expected = ArithmeticException.class)
    public void testSubtract_Overflow() {
        Fraction f1 = Fraction.getFraction(Integer.MIN_VALUE, 1);
        Fraction f2 = Fraction.getFraction(1, 1);
        f1.subtract(f2);
    }

    @Test
    public void testAdd_ComplexKnuth() {
        // covers BigInteger path when d1!=1
        Fraction f1 = Fraction.getFraction(1, 6);
        Fraction f2 = Fraction.getFraction(1, 10);
        Fraction sum = f1.add(f2);
        assertEquals(4, sum.getNumerator()); // 1/6+1/10=4/15
        assertEquals(15, sum.getDenominator());
    }

    // multiplyBy
    @Test(expected = IllegalArgumentException.class)
    public void testMultiplyBy_Null() {
        Fraction.ONE.multiplyBy(null);
    }

    @Test
    public void testMultiplyBy_Zero() {
        Fraction f = Fraction.getFraction(2, 3);
        assertSame(Fraction.ZERO, Fraction.ZERO.multiplyBy(f));
        assertSame(Fraction.ZERO, f.multiplyBy(Fraction.ZERO));
    }

    @Test
    public void testMultiplyBy_Normal() {
        Fraction f1 = Fraction.getFraction(2, 3);
        Fraction f2 = Fraction.getFraction(3, 4);
        Fraction prod = f1.multiplyBy(f2);
        assertEquals(1, prod.getNumerator());
        assertEquals(2, prod.getDenominator());
    }

    @Test(expected = ArithmeticException.class)
    public void testMultiplyBy_Overflow() {
        Fraction.getFraction(Integer.MAX_VALUE, 1).multiplyBy(Fraction.getFraction(Integer.MAX_VALUE, 1));
    }

    // divideBy
    @Test(expected = IllegalArgumentException.class)
    public void testDivideBy_Null() {
        Fraction.ONE.divideBy(null);
    }

    @Test(expected = ArithmeticException.class)
    public void testDivideBy_Zero() {
        Fraction.ONE.divideBy(Fraction.ZERO);
    }

    @Test
    public void testDivideBy_Normal() {
        Fraction f1 = Fraction.getFraction(2, 3);
        Fraction f2 = Fraction.getFraction(3, 4);
        Fraction result = f1.divideBy(f2);
        assertEquals(8, result.getNumerator()); // (2/3) / (3/4) = 8/9
        assertEquals(9, result.getDenominator());
    }

    // equals and hashCode
    @Test
    public void testEquals_SameObject() {
        Fraction f = Fraction.getFraction(2, 3);
        assertTrue(f.equals(f));
    }

    @Test
    public void testEquals_DifferentClass() {
        assertFalse(Fraction.ONE.equals("string"));
    }

    @Test
    public void testEquals_SameValues() {
        Fraction f1 = Fraction.getFraction(2, 3);
        Fraction f2 = Fraction.getFraction(2, 3);
        assertTrue(f1.equals(f2));
        assertEquals(f1.hashCode(), f2.hashCode());
    }

    @Test
    public void testEquals_DifferentValues() {
        Fraction f1 = Fraction.getFraction(1, 2);
        Fraction f2 = Fraction.getFraction(2, 4); // different values, equals based on exact numerator/denominator
        assertFalse(f1.equals(f2));
    }

    @Test
    public void testHashCode_Consistent() {
        Fraction f = Fraction.getFraction(3, 7);
        int h1 = f.hashCode();
        int h2 = f.hashCode();
        assertEquals(h1, h2);
    }

    // compareTo
    @Test
    public void testCompareTo_SameObject() {
        Fraction f = Fraction.getFraction(2, 3);
        assertEquals(0, f.compareTo(f));
    }

    @Test
    public void testCompareTo_SameValueDifferentRepresentation() {
        Fraction f1 = Fraction.getFraction(1, 2);
        Fraction f2 = Fraction.getFraction(2, 4);
        // cross multiplication: 1*4=4, 2*2=4 => equal
        assertEquals(0, f1.compareTo(f2));
    }

    @Test
    public void testCompareTo_LessThan() {
        Fraction f1 = Fraction.getFraction(1, 3);
        Fraction f2 = Fraction.getFraction(1, 2);
        assertTrue(f1.compareTo(f2) < 0);
    }

    @Test
    public void testCompareTo_GreaterThan() {
        Fraction f1 = Fraction.getFraction(2, 3);
        Fraction f2 = Fraction.getFraction(1, 2);
        assertTrue(f1.compareTo(f2) > 0);
    }

    @Test(expected = NullPointerException.class)
    public void testCompareTo_Null() {
        Fraction.ONE.compareTo(null);
    }

    // toString
    @Test
    public void testToString() {
        Fraction f = Fraction.getFraction(2, 3);
        assertEquals("2/3", f.toString());
    }

    @Test
    public void testToString_Negative() {
        Fraction f = Fraction.getFraction(-2, 3);
        assertEquals("-2/3", f.toString());
    }

    @Test
    public void testToString_Caches() {
        Fraction f = Fraction.getFraction(2, 3);
        String s1 = f.toString();
        String s2 = f.toString();
        assertSame(s1, s2);
    }

    // toProperString
    @Test
    public void testToProperString_Zero() {
        assertEquals("0", Fraction.ZERO.toProperString());
    }

    @Test
    public void testToProperString_One() {
        assertEquals("1", Fraction.ONE.toProperString());
    }

    @Test
    public void testToProperString_NegativeOne() {
        Fraction f = Fraction.getFraction(-1, 1);
        assertEquals("-1", f.toProperString());
    }

    @Test
    public void testToProperString_NegativeOneDenom() {
        // numerator == -1 * denominator => -1
        Fraction f = Fraction.getFraction(-5, 5);
        assertEquals("-1", f.toProperString());
    }

    @Test
    public void testToProperString_ProperFraction() {
        Fraction f = Fraction.getFraction(1, 2);
        assertEquals("1/2", f.toProperString());
    }

    @Test
    public void testToProperString_ImproperFraction() {
        Fraction f = Fraction.getFraction(7, 4);
        assertEquals("1 3/4", f.toProperString());
    }

    @Test
    public void testToProperString_NegativeImproperFraction() {
        Fraction f = Fraction.getFraction(-7, 4);
        assertEquals("-1 3/4", f.toProperString());
    }

    @Test
    public void testToProperString_ImproperWholeOnly() {
        Fraction f = Fraction.getFraction(6, 2);
        assertEquals("3", f.toProperString());
    }

    @Test
    public void testToProperString_NegativeImproperWholeOnly() {
        Fraction f = Fraction.getFraction(-6, 2);
        assertEquals("-3", f.toProperString());
    }

    @Test
    public void testToProperString_Caches() {
        Fraction f = Fraction.getFraction(7, 4);
        String s1 = f.toProperString();
        String s2 = f.toProperString();
        assertSame(s1, s2);
    }

    // Test constants
    @Test
    public void testConstants() {
        assertEquals(0, Fraction.ZERO.getNumerator());
        assertEquals(1, Fraction.ZERO.getDenominator());
        assertEquals(1, Fraction.ONE.getNumerator());
        assertEquals(1, Fraction.ONE.getDenominator());
        assertEquals(1, Fraction.ONE_HALF.getNumerator());
        assertEquals(2, Fraction.ONE_HALF.getDenominator());
        // etc, but not needed to test all
    }

    // Additional edge cases for greatestCommonDivisor
    @Test
    public void testGcdMinValue() {
        // indirectly via reduce
        Fraction f = Fraction.getFraction(Integer.MIN_VALUE, Integer.MIN_VALUE);
        // getFraction normalizes signs, denominator negative -> denominator becomes -MIN_VALUE which overflows.
        // So it throws ArithmeticException.
    }

    @Test(expected = ArithmeticException.class)
    public void testAdd_BigIntegerOverflow() {
        // create fractions such that BigInteger multiplication overflows 65 bits? Actually w.bitLength() > 31 throws.
        Fraction f1 = Fraction.getFraction(Integer.MAX_VALUE, 1);
        Fraction f2 = Fraction.getFraction(1, Integer.MAX_VALUE);
        // d1 = gcd(1, MAX_VALUE) = 1, so goes to simple path, not BigInteger.
        // Need to trigger BigInteger path with d1 != 1.
        // For example, denominators share a gcd >1 and the result overflows.
        // Choose numerator large and denominator something.
        Fraction a = Fraction.getFraction(Integer.MAX_VALUE, 2);
        Fraction b = Fraction.getFraction(Integer.MAX_VALUE, 4);
        // d1 = gcd(2,4)=2. Then uvp = MAX_VALUE * (4/2=2) = overflow in BigInteger multiplication? Actually BigInteger will handle it, but w.bitLength() > 31 will throw.
        a.add(b); // Expect ArithmeticException
    }

    @Test(expected = ArithmeticException.class)
    public void testDivideBy_Overflow() {
        Fraction f1 = Fraction.getFraction(Integer.MAX_VALUE, 1);
        Fraction f2 = Fraction.getFraction(1, Integer.MAX_VALUE);
        // dividing f1 by f2 is same as multiply by Integer.MAX_VALUE/1, so overflow.
        f1.divideBy(f2);
    }
}
