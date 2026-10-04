package org.apache.commons.math.dfp;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DfpTest {

    private DfpField factory;
    private DfpField factory20;

    @Before
    public void setUp() {
        factory = new DfpField(6);
        factory20 = new DfpField(20);
    }

    @Test
    public void testConstructors() {
        Dfp zero = factory.newDfp();
        Assert.assertEquals(0, zero.intValue());
        Assert.assertEquals(Dfp.FINITE, zero.classify());

        Dfp fromByte = factory.newDfp((byte) -12);
        Assert.assertEquals(-12, fromByte.intValue());

        Dfp fromInt = factory.newDfp(123456);
        Assert.assertEquals(123456, fromInt.intValue());

        Dfp fromLong = factory.newDfp(-9876543210L);
        Assert.assertEquals("-9876543210.", fromLong.toString());

        Dfp minLong = factory.newDfp(Long.MIN_VALUE);
        Assert.assertEquals("-9223372036854775808.", minLong.toString());

        Dfp fromDouble = factory.newDfp(1.234);
        Assert.assertEquals(1.234, fromDouble.toDouble(), 1e-10);

        Dfp fromDoubleZero = factory.newDfp(0.0);
        Assert.assertTrue(fromDoubleZero.equals(zero));

        Dfp fromDoubleSubnormal = factory.newDfp(Double.MIN_VALUE);
        Assert.assertEquals(Double.MIN_VALUE, fromDoubleSubnormal.toDouble(), 0.0);

        Dfp fromDoubleNan = factory.newDfp(Double.NaN);
        Assert.assertTrue(fromDoubleNan.isNaN());

        Dfp fromDoublePosInf = factory.newDfp(Double.POSITIVE_INFINITY);
        Assert.assertTrue(fromDoublePosInf.isInfinite());
        Assert.assertEquals(1, fromDoublePosInf.sign);

        Dfp fromDoubleNegInf = factory.newDfp(Double.NEGATIVE_INFINITY);
        Assert.assertTrue(fromDoubleNegInf.isInfinite());
        Assert.assertEquals(-1, fromDoubleNegInf.sign);

        Dfp fromDoubleNeg = factory.newDfp(-1.5);
        Assert.assertEquals(-1.5, fromDoubleNeg.toDouble(), 1e-10);

        Dfp copy = new Dfp(fromInt);
        Assert.assertEquals(fromInt, copy);
        Assert.assertEquals(fromInt.hashCode(), copy.hashCode());
    }

    @Test
    public void testStringConstructor() {
        Dfp pInf = factory.newDfp("Infinity");
        Assert.assertTrue(pInf.isInfinite());
        Assert.assertEquals(1, pInf.sign);

        Dfp nInf = factory.newDfp("-Infinity");
        Assert.assertTrue(nInf.isInfinite());
        Assert.assertEquals(-1, nInf.sign);

        Dfp nan = factory.newDfp("NaN");
        Assert.assertTrue(nan.isNaN());

        Dfp sci1 = factory.newDfp("1.234e2");
        Assert.assertEquals(123.4, sci1.toDouble(), 1e-10);

        Dfp sci2 = factory.newDfp("-1.234E-2");
        Assert.assertEquals(-0.01234, sci2.toDouble(), 1e-10);

        Dfp leadingZeros = factory.newDfp("0.000123");
        Assert.assertEquals("0.000123", leadingZeros.toString());

        Dfp onlyZeroDec = factory.newDfp("0.00000");
        Assert.assertTrue(onlyZeroDec.equals(factory.getZero()));

        Dfp nonDigits = factory.newDfp(" 12a34.56b7 ");
        Assert.assertEquals(1234.567, nonDigits.toDouble(), 1e-10);

        Dfp longDec = factory.newDfp("123456789012345678901234567890");
        Assert.assertNotNull(longDec);
    }

    @Test
    public void testNewInstanceMethods() {
        Dfp a = factory.newDfp(10);
        Assert.assertEquals(0, a.newInstance().intValue());
        Assert.assertEquals((byte) 5, a.newInstance((byte) 5).intValue());
        Assert.assertEquals(7, a.newInstance(7).intValue());
        Assert.assertEquals(123456789L, a.newInstance(123456789L).intValue());
        Assert.assertEquals(1.5, a.newInstance(1.5).toDouble(), 1e-10);
        Assert.assertEquals(25, a.newInstance("25").intValue());
        Assert.assertTrue(a.newInstance((byte) 1, Dfp.QNAN).isNaN());

        Dfp b = factory20.newDfp(10);
        Dfp mismatched = a.newInstance(b);
        Assert.assertTrue(mismatched.isNaN());
    }

    @Test
    public void testGettersAndConstants() {
        Dfp a = factory.newDfp(10);
        Assert.assertSame(factory, a.getField());
        Assert.assertEquals(6, a.getRadixDigits());
        Assert.assertEquals(0, a.getZero().intValue());
        Assert.assertEquals(1, a.getOne().intValue());
        Assert.assertEquals(2, a.getTwo().intValue());
    }

    @Test
    public void testAlignAndShifts() {
        Dfp a = factory.newDfp(1);
        int lost1 = a.align(0);
        Assert.assertEquals(0, lost1);

        Dfp b = factory.newDfp(1);
        int lost2 = b.align(10);
        Assert.assertEquals(0, lost2);

        Dfp c = factory.newDfp(1);
        int lost3 = c.align(-3);
        Assert.assertEquals(0, lost3);
    }

    @Test
    public void testComparisons() {
        Dfp a = factory.newDfp(10);
        Dfp b = factory.newDfp(20);
        Dfp nan = factory.newDfp((byte) 1, Dfp.QNAN);
        Dfp pInf = factory.newDfp((byte) 1, Dfp.INFINITE);
        Dfp nInf = factory.newDfp((byte) -1, Dfp.INFINITE);

        Assert.assertTrue(a.lessThan(b));
        Assert.assertFalse(b.lessThan(a));
        Assert.assertFalse(a.lessThan(nan));

        Assert.assertTrue(b.greaterThan(a));
        Assert.assertFalse(a.greaterThan(b));
        Assert.assertFalse(b.greaterThan(nan));

        Assert.assertTrue(a.unequal(b));
        Assert.assertFalse(a.unequal(a));
        Assert.assertFalse(a.unequal(nan));

        Assert.assertTrue(a.equals(factory.newDfp(10)));
        Assert.assertFalse(a.equals(b));
        Assert.assertFalse(a.equals(nan));
        Assert.assertFalse(a.equals("string"));

        Assert.assertTrue(a.lessThan(pInf));
        Assert.assertTrue(nInf.lessThan(a));
        Assert.assertFalse(pInf.lessThan(pInf));

        Dfp aDiffPrec = factory20.newDfp(10);
        Assert.assertFalse(a.lessThan(aDiffPrec));
        Assert.assertFalse(a.greaterThan(aDiffPrec));
        Assert.assertFalse(a.unequal(aDiffPrec));
        Assert.assertFalse(a.equals(aDiffPrec));

        Dfp zero1 = factory.newDfp(0);
        Dfp zero2 = factory.newDfp(0).negate();
        Assert.assertTrue(zero1.equals(zero2));
    }

    @Test
    public void testRoundingModesAndTrunc() {
        Dfp val = factory.newDfp("1.5");
        Assert.assertEquals(2, val.rint().intValue());
        Assert.assertEquals(1, val.floor().intValue());
        Assert.assertEquals(2, val.ceil().intValue());

        Dfp nVal = factory.newDfp("-1.5");
        Assert.assertEquals(-2, nVal.rint().intValue());
        Assert.assertEquals(-2, nVal.floor().intValue());
        Assert.assertEquals(-1, nVal.ceil().intValue());

        Dfp evenVal = factory.newDfp("2.5");
        Assert.assertEquals(2, evenVal.rint().intValue());

        Dfp bigExp = factory.newDfp("1.0e30");
        Assert.assertEquals(bigExp, bigExp.rint());

        Dfp smallExp = factory.newDfp("1.0e-5");
        Assert.assertEquals(0, smallExp.rint().intValue());

        Dfp zero = factory.getZero();
        Assert.assertEquals(zero, zero.rint());

        Dfp nan = factory.newDfp((byte) 1, Dfp.QNAN);
        Assert.assertTrue(nan.rint().isNaN());

        Dfp inf = factory.newDfp((byte) 1, Dfp.INFINITE);
        Assert.assertTrue(inf.rint().isInfinite());
    }

    @Test
    public void testRoundVariants() {
        DfpField fDown = new DfpField(6);
        fDown.setRoundingMode(DfpField.RoundingMode.ROUND_DOWN);
        Assert.assertEquals(1, fDown.newDfp("1.9").rint().intValue());

        DfpField fUp = new DfpField(6);
        fUp.setRoundingMode(DfpField.RoundingMode.ROUND_UP);
        Assert.assertEquals(2, fUp.newDfp("1.1").rint().intValue());

        DfpField fHalfUp = new DfpField(6);
        fHalfUp.setRoundingMode(DfpField.RoundingMode.ROUND_HALF_UP);
        Assert.assertEquals(2, fHalfUp.newDfp("1.5").rint().intValue());
        Assert.assertEquals(1, fHalfUp.newDfp("1.49").rint().intValue());

        DfpField fHalfDown = new DfpField(6);
        fHalfDown.setRoundingMode(DfpField.RoundingMode.ROUND_HALF_DOWN);
        Assert.assertEquals(1, fHalfDown.newDfp("1.5").rint().intValue());
        Assert.assertEquals(2, fHalfDown.newDfp("1.51").rint().intValue());

        DfpField fHalfOdd = new DfpField(6);
        fHalfOdd.setRoundingMode(DfpField.RoundingMode.ROUND_HALF_ODD);
        Assert.assertEquals(1, fHalfOdd.newDfp("1.5").rint().intValue());
        Assert.assertEquals(3, fHalfOdd.newDfp("2.5").rint().intValue());
    }

    @Test
    public void testRemainder() {
        Dfp a = factory.newDfp(7);
        Dfp b = factory.newDfp(3);
        Assert.assertEquals(1, a.remainder(b).intValue());

        Dfp a2 = factory.newDfp(6);
        Assert.assertEquals(0, a2.remainder(b).intValue());
    }

    @Test
    public void testIntValueAndLogs() {
        Dfp a = factory.newDfp(123456);
        Assert.assertEquals(123456, a.intValue());

        Dfp overflow = factory.newDfp("3000000000");
        Assert.assertEquals(Integer.MAX_VALUE, overflow.intValue());

        Dfp underflow = factory.newDfp("-3000000000");
        Assert.assertEquals(Integer.MIN_VALUE, underflow.intValue());

        Dfp val = factory.newDfp(1000000);
        Assert.assertEquals(1, val.log10K());
        Assert.assertEquals(6, val.log10());

        Dfp val2 = factory.newDfp(5000);
        Assert.assertEquals(3, val2.log10());
        Dfp val3 = factory.newDfp(500);
        Assert.assertEquals(2, val3.log10());
        Dfp val4 = factory.newDfp(50);
        Assert.assertEquals(1, val4.log10());
        Dfp val5 = factory.newDfp(5);
        Assert.assertEquals(0, val5.log10());

        Assert.assertEquals(10000, factory.getOne().power10K(1).intValue());
        Assert.assertEquals(10, factory.getOne().power10(1).intValue());
        Assert.assertEquals(100, factory.getOne().power10(2).intValue());
        Assert.assertEquals(1000, factory.getOne().power10(3).intValue());
        Assert.assertEquals(10000, factory.getOne().power10(4).intValue());
        Assert.assertEquals("0.1", factory.getOne().power10(-1).toString());
    }

    @Test
    public void testAddAndSubtract() {
        Dfp a = factory.newDfp(1234);
        Dfp b = factory.newDfp(4321);
        Assert.assertEquals(5555, a.add(b).intValue());
        Assert.assertEquals(-3087, a.subtract(b).intValue());

        Dfp c = factory.newDfp(-1234);
        Assert.assertEquals(3087, b.add(c).intValue());

        Dfp diffPrec = factory20.newDfp(10);
        Assert.assertTrue(a.add(diffPrec).isNaN());

        Dfp nan = factory.newDfp((byte) 1, Dfp.QNAN);
        Assert.assertTrue(a.add(nan).isNaN());
        Assert.assertTrue(nan.add(a).isNaN());

        Dfp pInf = factory.newDfp((byte) 1, Dfp.INFINITE);
        Dfp nInf = factory.newDfp((byte) -1, Dfp.INFINITE);
        Assert.assertTrue(a.add(pInf).isInfinite());
        Assert.assertTrue(pInf.add(a).isInfinite());
        Assert.assertTrue(pInf.add(pInf).isInfinite());
        Assert.assertTrue(pInf.add(nInf).isNaN());

        Dfp zero = factory.getZero();
        Assert.assertEquals(1234, a.add(zero).intValue());
        Assert.assertEquals(1234, zero.add(a).intValue());
        Assert.assertEquals(0, zero.add(zero.negate()).intValue());
    }

    @Test
    public void testMultiply() {
        Dfp a = factory.newDfp(12);
        Dfp b = factory.newDfp(10);
        Assert.assertEquals(120, a.multiply(b).intValue());
        Assert.assertEquals(60, a.multiply(5).intValue());

        Dfp diffPrec = factory20.newDfp(10);
        Assert.assertTrue(a.multiply(diffPrec).isNaN());

        Dfp nan = factory.newDfp((byte) 1, Dfp.QNAN);
        Assert.assertTrue(a.multiply(nan).isNaN());
        Assert.assertTrue(nan.multiply(a).isNaN());
        Assert.assertTrue(nan.multiply(5).isNaN());

        Dfp pInf = factory.newDfp((byte) 1, Dfp.INFINITE);
        Assert.assertTrue(pInf.multiply(a).isInfinite());
        Assert.assertTrue(a.multiply(pInf).isInfinite());
        Assert.assertTrue(pInf.multiply(pInf).isInfinite());
        Assert.assertTrue(pInf.multiply(factory.getZero()).isNaN());
        Assert.assertTrue(factory.getZero().multiply(pInf).isNaN());

        Assert.assertTrue(pInf.multiply(2).isInfinite());
        Assert.assertTrue(pInf.multiply(0).isNaN());
        Assert.assertTrue(a.multiply(-1).isNaN());
        Assert.assertTrue(a.multiply(10001).isNaN());
    }

    @Test
    public void testDivide() {
        Dfp a = factory.newDfp(100);
        Dfp b = factory.newDfp(4);
        Assert.assertEquals(25, a.divide(b).intValue());
        Assert.assertEquals(20, a.divide(5).intValue());

        Dfp zero = factory.getZero();
        Assert.assertTrue(a.divide(zero).isInfinite());
        Assert.assertTrue(a.divide(0).isInfinite());

        Dfp diffPrec = factory20.newDfp(4);
        Assert.assertTrue(a.divide(diffPrec).isNaN());

        Dfp nan = factory.newDfp((byte) 1, Dfp.QNAN);
        Assert.assertTrue(a.divide(nan).isNaN());
        Assert.assertTrue(nan.divide(a).isNaN());
        Assert.assertTrue(nan.divide(5).isNaN());

        Dfp pInf = factory.newDfp((byte) 1, Dfp.INFINITE);
        Assert.assertTrue(pInf.divide(a).isInfinite());
        Assert.assertEquals(0, a.divide(pInf).intValue());
        Assert.assertTrue(pInf.divide(pInf).isNaN());

        Assert.assertTrue(pInf.divide(2).isInfinite());
        Assert.assertTrue(a.divide(-1).isNaN());
        Assert.assertTrue(a.divide(10001).isNaN());

        Dfp small = factory.newDfp(1);
        Assert.assertEquals(0.125, small.divide(8).toDouble(), 1e-10);
    }

    @Test
    public void testSqrt() {
        Dfp a = factory.newDfp(16);
        Assert.assertEquals(4, a.sqrt().intValue());

        Dfp zero = factory.getZero();
        Assert.assertEquals(0, zero.sqrt().intValue());

        Dfp neg = factory.newDfp(-4);
        Assert.assertTrue(neg.sqrt().isNaN());

        Dfp pInf = factory.newDfp((byte) 1, Dfp.INFINITE);
        Assert.assertTrue(pInf.sqrt().isInfinite());

        Dfp nan = factory.newDfp((byte) 1, Dfp.QNAN);
        Assert.assertTrue(nan.sqrt().isNaN());

        Dfp sNan = factory.newDfp((byte) 1, Dfp.SNAN);
        Assert.assertTrue(sNan.sqrt().isNaN());

        Dfp large = factory.newDfp("1.0e20");
        Assert.assertEquals("10000000000.", large.sqrt().toString());

        Dfp c1 = factory.newDfp(4500);
        Assert.assertTrue(c1.sqrt().toDouble() > 60);

        Dfp c2 = factory.newDfp(6500);
        Assert.assertTrue(c2.sqrt().toDouble() > 80);

        Dfp c3 = factory.newDfp(9500);
        Assert.assertTrue(c3.sqrt().toDouble() > 90);
    }

    @Test
    public void testToStringAndDfp2Sci() {
        Dfp val = factory.newDfp("123.456");
        Assert.assertEquals("123.456", val.toString());

        Dfp big = factory.newDfp("1.23456789e30");
        Assert.assertTrue(big.toString().contains("e"));

        Dfp zero = factory.getZero();
        Assert.assertEquals("0.", zero.toString());

        Dfp pInf = factory.newDfp((byte) 1, Dfp.INFINITE);
        Dfp nInf = factory.newDfp((byte) -1, Dfp.INFINITE);
        Dfp nan = factory.newDfp((byte) 1, Dfp.QNAN);

        Assert.assertEquals("Infinity", pInf.toString());
        Assert.assertEquals("-Infinity", nInf.toString());
        Assert.assertEquals("NaN", nan.toString());
    }

    @Test
    public void testNextAfterAndCopysign() {
        Dfp a = factory.newDfp(1);
        Dfp b = factory.newDfp(2);
        Dfp next = a.nextAfter(b);
        Assert.assertTrue(next.greaterThan(a));

        Dfp prev = b.nextAfter(a);
        Assert.assertTrue(prev.lessThan(b));

        Assert.assertEquals(a, a.nextAfter(a));

        Dfp zero = factory.getZero();
        Dfp nextZero = zero.nextAfter(b);
        Assert.assertTrue(nextZero.greaterThan(zero));

        Dfp neg = factory.newDfp(-1);
        Dfp nextNeg = neg.nextAfter(zero);
        Assert.assertTrue(nextNeg.greaterThan(neg));

        Dfp diffPrec = factory20.newDfp(1);
        Assert.assertTrue(a.nextAfter(diffPrec).isNaN());

        Dfp cs1 = Dfp.copysign(a, neg);
        Assert.assertEquals(-1, cs1.intValue());

        Dfp cs2 = Dfp.copysign(neg, a);
        Assert.assertEquals(1, cs2.intValue());
    }

    @Test
    public void testToDoubleAndSplitDouble() {
        Dfp val = factory.newDfp("12345.6789");
        Assert.assertEquals(12345.6789, val.toDouble(), 1e-10);

        Dfp nVal = factory.newDfp("-12345.6789");
        Assert.assertEquals(-12345.6789, nVal.toDouble(), 1e-10);

        Dfp pInf = factory.newDfp((byte) 1, Dfp.INFINITE);
        Dfp nInf = factory.newDfp((byte) -1, Dfp.INFINITE);
        Dfp nan = factory.newDfp((byte) 1, Dfp.QNAN);

        Assert.assertEquals(Double.POSITIVE_INFINITY, pInf.toDouble(), 0.0);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, nInf.toDouble(), 0.0);
        Assert.assertTrue(Double.isNaN(nan.toDouble()));

        Dfp tiny = factory.newDfp("1.0e-350");
        Assert.assertEquals(0.0, tiny.toDouble(), 0.0);

        Dfp huge = factory.newDfp("1.0e350");
        Assert.assertEquals(Double.POSITIVE_INFINITY, huge.toDouble(), 0.0);

        Dfp nHuge = factory.newDfp("-1.0e350");
        Assert.assertEquals(Double.NEGATIVE_INFINITY, nHuge.toDouble(), 0.0);

        double[] split = val.toSplitDouble();
        Assert.assertEquals(val.toDouble(), split[0] + split[1], 1e-10);
    }
}
