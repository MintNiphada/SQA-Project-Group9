package org.apache.commons.math3.dfp;

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
    public void testConstructorsAndFactories() {
        Dfp d0 = new Dfp(factory);
        Assert.assertTrue(d0.isZero());
        Assert.assertEquals(1, d0.sign);

        Dfp db = new Dfp(factory, (byte) -12);
        Assert.assertEquals(-12, db.intValue());

        Dfp di = new Dfp(factory, 123456);
        Assert.assertEquals(123456, di.intValue());

        Dfp dl = new Dfp(factory, -9876543210123L);
        Assert.assertEquals(-1, dl.sign);

        Dfp dlMin = new Dfp(factory, Long.MIN_VALUE);
        Assert.assertEquals(-1, dlMin.sign);
        Assert.assertEquals(Dfp.FINITE, dlMin.classify());

        Dfp ddZero = new Dfp(factory, 0.0);
        Assert.assertTrue(ddZero.isZero());
        Assert.assertEquals(1, ddZero.sign);

        Dfp ddNegZero = new Dfp(factory, -0.0);
        Assert.assertTrue(ddNegZero.isZero());
        Assert.assertEquals(-1, ddNegZero.sign);

        Dfp ddPosInf = new Dfp(factory, Double.POSITIVE_INFINITY);
        Assert.assertTrue(ddPosInf.isInfinite());
        Assert.assertEquals(1, ddPosInf.sign);

        Dfp ddNegInf = new Dfp(factory, Double.NEGATIVE_INFINITY);
        Assert.assertTrue(ddNegInf.isInfinite());
        Assert.assertEquals(-1, ddNegInf.sign);

        Dfp ddNan = new Dfp(factory, Double.NaN);
        Assert.assertTrue(ddNan.isNaN());

        Dfp ddSub = new Dfp(factory, Double.MIN_VALUE);
        Assert.assertFalse(ddSub.isZero());
        Assert.assertTrue(ddSub.toDouble() > 0);

        Dfp ddNegNorm = new Dfp(factory, -4.5);
        Assert.assertEquals(-1, ddNegNorm.sign);

        Dfp copy = new Dfp(dl);
        Assert.assertEquals(dl, copy);

        Dfp customNan = factory.newDfp((byte) -1, Dfp.SNAN);
        Assert.assertTrue(customNan.isNaN());
        Assert.assertEquals(-1, customNan.sign);

        Assert.assertEquals(factory.getRadixDigits(), d0.getRadixDigits());
        Assert.assertEquals(factory, d0.getField());
        Assert.assertEquals(0, d0.getZero().intValue());
        Assert.assertEquals(1, d0.getOne().intValue());
        Assert.assertEquals(2, d0.getTwo().intValue());

        Assert.assertEquals(10, d0.newInstance((byte) 10).intValue());
        Assert.assertEquals(20, d0.newInstance(20).intValue());
        Assert.assertEquals(30L, d0.newInstance(30L).intValue());
        Assert.assertEquals(40.0, d0.newInstance(40.0).toDouble(), 1e-10);
        Assert.assertEquals(50, d0.newInstance("50").intValue());
        Assert.assertTrue(d0.newInstance((byte) 1, Dfp.INFINITE).isInfinite());
    }

    @Test
    public void testStringConstructorEdgeCases() {
        Dfp pinf = factory.newDfp("Infinity");
        Assert.assertTrue(pinf.isInfinite());
        Assert.assertEquals(1, pinf.sign);

        Dfp ninf = factory.newDfp("-Infinity");
        Assert.assertTrue(ninf.isInfinite());
        Assert.assertEquals(-1, ninf.sign);

        Dfp nan = factory.newDfp("NaN");
        Assert.assertTrue(nan.isNaN());

        Dfp sci1 = factory.newDfp("1.2345e2");
        Assert.assertEquals(123.45, sci1.toDouble(), 1e-5);

        Dfp sci2 = factory.newDfp("-1.2345E-2");
        Assert.assertEquals(-0.012345, sci2.toDouble(), 1e-7);

        Dfp sci3 = factory.newDfp("0.00000");
        Assert.assertTrue(sci3.isZero());

        Dfp sci4 = factory.newDfp("123456789012345678901234567890");
        Assert.assertFalse(sci4.isZero());

        Dfp sci5 = factory.newDfp(".00001234");
        Assert.assertEquals(0.00001234, sci5.toDouble(), 1e-10);

        Dfp sci6 = factory.newDfp("1234.");
        Assert.assertEquals(1234, sci6.intValue());

        Dfp sci7 = factory.newDfp("1234567890123456789012345678901234567890.123456789");
        Assert.assertFalse(sci7.isZero());
    }

    @Test
    public void testShiftsAndAlign() {
        Dfp a = factory.newDfp("1234.5678");
        a.shiftLeft();
        Assert.assertEquals(0, a.mant[0]);

        a.shiftRight();
        Assert.assertEquals(0, a.mant[a.mant.length - 1]);

        Dfp b = factory.newDfp("1.0");
        int lost = b.align(b.exp);
        Assert.assertEquals(0, lost);

        Dfp c = factory.newDfp("1.0");
        c.align(c.exp + 100);
        Assert.assertTrue(c.isZero());

        Dfp d = factory.newDfp("123456789");
        d.align(d.exp - 3);
        d.align(d.exp + 3);
    }

    @Test
    public void testComparisonsAndPredicates() {
        Dfp pZero = factory.getZero();
        Dfp nZero = factory.getZero().negate();
        Dfp one = factory.getOne();
        Dfp nOne = one.negate();
        Dfp pInf = factory.newDfp((byte) 1, Dfp.INFINITE);
        Dfp nInf = factory.newDfp((byte) -1, Dfp.INFINITE);
        Dfp nan = factory.newDfp((byte) 1, Dfp.QNAN);

        Assert.assertTrue(pZero.equals(nZero));
        Assert.assertTrue(nZero.equals(pZero));
        Assert.assertFalse(pZero.lessThan(nZero));
        Assert.assertFalse(pZero.greaterThan(nZero));

        Assert.assertTrue(nOne.lessThan(one));
        Assert.assertTrue(one.greaterThan(nOne));
        Assert.assertFalse(one.lessThan(nOne));
        Assert.assertFalse(nOne.greaterThan(one));

        Assert.assertTrue(one.lessThan(pInf));
        Assert.assertTrue(pInf.greaterThan(one));
        Assert.assertTrue(nInf.lessThan(nOne));
        Assert.assertTrue(nOne.greaterThan(nInf));
        Assert.assertTrue(nInf.lessThan(pInf));
        Assert.assertTrue(pInf.greaterThan(nInf));
        Assert.assertTrue(pInf.equals(pInf));
        Assert.assertTrue(nInf.equals(nInf));

        Assert.assertFalse(nan.lessThan(one));
        Assert.assertFalse(nan.greaterThan(one));
        Assert.assertFalse(one.lessThan(nan));
        Assert.assertFalse(one.greaterThan(nan));
        Assert.assertFalse(nan.equals(nan));
        Assert.assertFalse(nan.equals(one));
        Assert.assertFalse(nan.unequal(nan));

        Dfp diffPrec = factory20.getOne();
        Assert.assertFalse(one.lessThan(diffPrec));
        Assert.assertFalse(one.greaterThan(diffPrec));
        Assert.assertFalse(one.equals(diffPrec));
        Assert.assertFalse(one.unequal(diffPrec));
        Assert.assertFalse(one.equals(new Object()));

        Assert.assertTrue(one.unequal(nOne));
        Assert.assertFalse(one.unequal(one));

        Assert.assertTrue(pZero.negativeOrNull());
        Assert.assertTrue(nZero.negativeOrNull());
        Assert.assertTrue(nOne.negativeOrNull());
        Assert.assertFalse(one.negativeOrNull());
        Assert.assertFalse(nan.negativeOrNull());

        Assert.assertFalse(pZero.strictlyNegative());
        Assert.assertFalse(nZero.strictlyNegative());
        Assert.assertTrue(nOne.strictlyNegative());
        Assert.assertTrue(nInf.strictlyNegative());
        Assert.assertFalse(one.strictlyNegative());
        Assert.assertFalse(nan.strictlyNegative());

        Assert.assertTrue(pZero.positiveOrNull());
        Assert.assertTrue(nZero.positiveOrNull());
        Assert.assertTrue(one.positiveOrNull());
        Assert.assertFalse(nOne.positiveOrNull());
        Assert.assertFalse(nan.positiveOrNull());

        Assert.assertFalse(pZero.strictlyPositive());
        Assert.assertFalse(nZero.strictlyPositive());
        Assert.assertTrue(one.strictlyPositive());
        Assert.assertTrue(pInf.strictlyPositive());
        Assert.assertFalse(nOne.strictlyPositive());
        Assert.assertFalse(nan.strictlyPositive());

        Assert.assertTrue(pZero.isZero());
        Assert.assertTrue(nZero.isZero());
        Assert.assertFalse(one.isZero());
        Assert.assertFalse(pInf.isZero());
        Assert.assertFalse(nan.isZero());

        Assert.assertEquals(one.hashCode(), factory.getOne().hashCode());
        Assert.assertNotEquals(one.hashCode(), nOne.hashCode());
    }

    @Test
    public void testRoundingAndTrunc() {
        Dfp p2_5 = factory.newDfp("2.5");
        Dfp p3_5 = factory.newDfp("3.5");
        Dfp n2_5 = factory.newDfp("-2.5");
        Dfp n3_5 = factory.newDfp("-3.5");

        Assert.assertEquals(2, p2_5.rint().intValue());
        Assert.assertEquals(4, p3_5.rint().intValue());
        Assert.assertEquals(-2, n2_5.rint().intValue());
        Assert.assertEquals(-4, n3_5.rint().intValue());

        Assert.assertEquals(2, p2_5.floor().intValue());
        Assert.assertEquals(-3, n2_5.floor().intValue());

        Assert.assertEquals(3, p2_5.ceil().intValue());
        Assert.assertEquals(-2, n2_5.ceil().intValue());

        Dfp nan = factory.newDfp((byte) 1, Dfp.QNAN);
        Assert.assertTrue(nan.rint().isNaN());

        Dfp inf = factory.newDfp((byte) 1, Dfp.INFINITE);
        Assert.assertTrue(inf.rint().isInfinite());

        Dfp zero = factory.getZero();
        Assert.assertTrue(zero.rint().isZero());

        Dfp small = factory.newDfp("0.000000001");
        Assert.assertTrue(small.trunc(DfpField.RoundingMode.ROUND_HALF_EVEN).isZero());

        Dfp huge = factory.newDfp("123456789012345678901234567890");
        Assert.assertEquals(huge, huge.rint());

        Dfp f1 = factory.newDfp("1.2");
        Assert.assertEquals(1, f1.trunc(DfpField.RoundingMode.ROUND_DOWN).intValue());
        Assert.assertEquals(2, f1.trunc(DfpField.RoundingMode.ROUND_UP).intValue());
        Assert.assertEquals(1, f1.trunc(DfpField.RoundingMode.ROUND_HALF_UP).intValue());
        Assert.assertEquals(1, f1.trunc(DfpField.RoundingMode.ROUND_HALF_DOWN).intValue());
        Assert.assertEquals(1, f1.trunc(DfpField.RoundingMode.ROUND_HALF_ODD).intValue());

        Dfp f2 = factory.newDfp("1.8");
        Assert.assertEquals(2, f2.trunc(DfpField.RoundingMode.ROUND_HALF_ODD).intValue());

        Dfp halfEvenOddMant = factory.newDfp("1.5");
        Assert.assertEquals(2, halfEvenOddMant.rint().intValue());
    }

    @Test
    public void testIntValueAndRemainder() {
        Dfp maxInt = factory.newDfp("3000000000");
        Assert.assertEquals(2147483647, maxInt.intValue());

        Dfp minInt = factory.newDfp("-3000000000");
        Assert.assertEquals(-2147483648, minInt.intValue());

        Dfp val = factory.newDfp("-1234567");
        Assert.assertEquals(-1234567, val.intValue());

        Dfp d1 = factory.newDfp("5.5");
        Dfp d2 = factory.newDfp("2.0");
        Dfp rem = d1.remainder(d2);
        Assert.assertEquals(-0.5, rem.toDouble(), 1e-10);

        Dfp remZero = factory.newDfp("4.0").remainder(factory.newDfp("2.0"));
        Assert.assertTrue(remZero.isZero());
        Assert.assertEquals(1, remZero.sign);
    }

    @Test
    public void testLogsAndPowers() {
        Dfp a = factory.newDfp("1000000");
        Assert.assertEquals(1, a.log10K());

        Dfp p10k = a.power10K(3);
        Assert.assertEquals(4, p10k.exp);

        Dfp d4 = factory.newDfp("5000");
        Dfp d3 = factory.newDfp("500");
        Dfp d2 = factory.newDfp("50");
        Dfp d1 = factory.newDfp("5");

        Assert.assertEquals(3, d4.log10());
        Assert.assertEquals(2, d3.log10());
        Assert.assertEquals(1, d2.log10());
        Assert.assertEquals(0, d1.log10());

        Assert.assertEquals(1, factory.getOne().power10(0).intValue());
        Assert.assertEquals(10, factory.getOne().power10(1).intValue());
        Assert.assertEquals(100, factory.getOne().power10(2).intValue());
        Assert.assertEquals(1000, factory.getOne().power10(3).intValue());
        Assert.assertEquals(10000, factory.getOne().power10(4).intValue());
        Assert.assertEquals(0.1, factory.getOne().power10(-1).toDouble(), 1e-10);
        Assert.assertEquals(0.01, factory.getOne().power10(-2).toDouble(), 1e-10);
        Assert.assertEquals(0.001, factory.getOne().power10(-3).toDouble(), 1e-10);
        Assert.assertEquals(0.0001, factory.getOne().power10(-4).toDouble(), 1e-10);
    }

    @Test
    public void testComplement() {
        Dfp a = factory.newDfp("1234.5678");
        int extra = a.complement(1234);
        Assert.assertTrue(extra >= 0);
    }

    @Test
    public void testAddAndSubtract() {
        Dfp a = factory.newDfp("123.456");
        Dfp b = factory.newDfp("654.321");
        Dfp sum = a.add(b);
        Assert.assertEquals(777.777, sum.toDouble(), 1e-10);

        Dfp diff = a.subtract(b);
        Assert.assertEquals(-530.865, diff.toDouble(), 1e-10);

        Dfp pInf = factory.newDfp((byte) 1, Dfp.INFINITE);
        Dfp nInf = factory.newDfp((byte) -1, Dfp.INFINITE);
        Dfp nan = factory.newDfp((byte) 1, Dfp.QNAN);

        Assert.assertTrue(a.add(pInf).isInfinite());
        Assert.assertTrue(pInf.add(a).isInfinite());
        Assert.assertTrue(pInf.add(pInf).isInfinite());
        Assert.assertTrue(pInf.add(nInf).isNaN());
        Assert.assertTrue(nan.add(a).isNaN());
        Assert.assertTrue(a.add(nan).isNaN());

        Dfp diffField = factory20.getOne();
        Assert.assertTrue(a.add(diffField).isNaN());

        Dfp zero = factory.getZero();
        Assert.assertEquals(a, a.add(zero));
        Assert.assertEquals(a, zero.add(a));

        Dfp posOne = factory.getOne();
        Dfp negOne = posOne.negate();
        Dfp cancelled = posOne.add(negOne);
        Assert.assertTrue(cancelled.isZero());
        Assert.assertEquals(1, cancelled.sign);

        Dfp overflowAdd = factory.newDfp("999999999999999999999999").add(factory.newDfp("1"));
        Assert.assertFalse(overflowAdd.isZero());
    }

    @Test
    public void testMultiply() {
        Dfp a = factory.newDfp("12.34");
        Dfp b = factory.newDfp("5.6");
        Assert.assertEquals(69.104, a.multiply(b).toDouble(), 1e-10);

        Dfp pInf = factory.newDfp((byte) 1, Dfp.INFINITE);
        Dfp nInf = factory.newDfp((byte) -1, Dfp.INFINITE);
        Dfp nan = factory.newDfp((byte) 1, Dfp.QNAN);
        Dfp zero = factory.getZero();

        Assert.assertTrue(a.multiply(nan).isNaN());
        Assert.assertTrue(nan.multiply(a).isNaN());
        Assert.assertTrue(pInf.multiply(a).isInfinite());
        Assert.assertTrue(a.multiply(pInf).isInfinite());
        Assert.assertTrue(pInf.multiply(pInf).isInfinite());
        Assert.assertTrue(pInf.multiply(nInf).isInfinite());
        Assert.assertTrue(pInf.multiply(zero).isNaN());
        Assert.assertTrue(zero.multiply(pInf).isNaN());

        Dfp diffField = factory20.getOne();
        Assert.assertTrue(a.multiply(diffField).isNaN());

        Dfp mulFast = a.multiply(5);
        Assert.assertEquals(61.7, mulFast.toDouble(), 1e-10);

        Assert.assertTrue(a.multiply(-1).isNaN());
        Assert.assertTrue(a.multiply(10000).isNaN());
        Assert.assertTrue(pInf.multiply(5).isInfinite());
        Assert.assertTrue(pInf.multiply(0).isNaN());
        Assert.assertTrue(nan.multiply(5).isNaN());
        Assert.assertTrue(zero.multiply(5).isZero());
    }

    @Test
    public void testDivide() {
        Dfp a = factory.newDfp("100.0");
        Dfp b = factory.newDfp("4.0");
        Assert.assertEquals(25.0, a.divide(b).toDouble(), 1e-10);

        Dfp pInf = factory.newDfp((byte) 1, Dfp.INFINITE);
        Dfp nInf = factory.newDfp((byte) -1, Dfp.INFINITE);
        Dfp nan = factory.newDfp((byte) 1, Dfp.QNAN);
        Dfp zero = factory.getZero();

        Assert.assertTrue(a.divide(nan).isNaN());
        Assert.assertTrue(nan.divide(a).isNaN());
        Assert.assertTrue(pInf.divide(a).isInfinite());
        Assert.assertTrue(a.divide(pInf).isZero());
        Assert.assertTrue(pInf.divide(pInf).isNaN());
        Assert.assertTrue(a.divide(zero).isInfinite());

        Dfp diffField = factory20.getOne();
        Assert.assertTrue(a.divide(diffField).isNaN());

        Dfp divFast = a.divide(4);
        Assert.assertEquals(25.0, divFast.toDouble(), 1e-10);

        Assert.assertTrue(a.divide(0).isInfinite());
        Assert.assertTrue(a.divide(-1).isNaN());
        Assert.assertTrue(a.divide(10000).isNaN());
        Assert.assertTrue(nan.divide(4).isNaN());
        Assert.assertTrue(pInf.divide(4).isInfinite());

        Dfp smallDividend = factory.newDfp("0.0001");
        Dfp divNorm = smallDividend.divide(10);
        Assert.assertEquals(0.00001, divNorm.toDouble(), 1e-10);

        Dfp recip = factory.newDfp("4.0").reciprocal();
        Assert.assertEquals(0.25, recip.toDouble(), 1e-10);
    }

    @Test
    public void testSqrt() {
        Dfp four = factory.newDfp("4.0");
        Assert.assertEquals(2.0, four.sqrt().toDouble(), 1e-10);

        Dfp zero = factory.getZero();
        Assert.assertTrue(zero.sqrt().isZero());

        Dfp pInf = factory.newDfp((byte) 1, Dfp.INFINITE);
        Assert.assertTrue(pInf.sqrt().isInfinite());

        Dfp qnan = factory.newDfp((byte) 1, Dfp.QNAN);
        Assert.assertTrue(qnan.sqrt().isNaN());

        Dfp snan = factory.newDfp((byte) 1, Dfp.SNAN);
        Assert.assertTrue(snan.sqrt().isNaN());

        Dfp neg = factory.newDfp("-4.0");
        Assert.assertTrue(neg.sqrt().isNaN());

        Dfp s1 = factory.newDfp("1e-10").sqrt();
        Assert.assertEquals(1e-5, s1.toDouble(), 1e-10);

        Dfp s2 = factory.newDfp("4e10").sqrt();
        Assert.assertEquals(2e5, s2.toDouble(), 1e-1);

        Dfp s3 = factory.newDfp("0.4").sqrt();
        Assert.assertEquals(Math.sqrt(0.4), s3.toDouble(), 1e-8);

        Dfp s4 = factory.newDfp("0.006").sqrt();
        Assert.assertEquals(Math.sqrt(0.006), s4.toDouble(), 1e-8);

        Dfp s5 = factory.newDfp("0.00008").sqrt();
        Assert.assertEquals(Math.sqrt(0.00008), s5.toDouble(), 1e-8);
    }

    @Test
    public void testToStringAndDfp2Sci() {
        Dfp pInf = factory.newDfp((byte) 1, Dfp.INFINITE);
        Assert.assertEquals("Infinity", pInf.toString());

        Dfp nInf = factory.newDfp((byte) -1, Dfp.INFINITE);
        Assert.assertEquals("-Infinity", nInf.toString());

        Dfp nan = factory.newDfp((byte) 1, Dfp.QNAN);
        Assert.assertEquals("NaN", nan.toString());

        Dfp norm = factory.newDfp("123.456");
        Assert.assertTrue(norm.toString().contains("123.456"));

        Dfp negNorm = factory.newDfp("-123.456");
        Assert.assertTrue(negNorm.toString().contains("-123.456"));

        Dfp small = factory.newDfp("0.00000000000000000123");
        Assert.assertTrue(small.toString().contains("e"));

        Dfp huge = factory.newDfp("123456789012345678901234567890");
        Assert.assertTrue(huge.toString().contains("e"));

        Dfp negHuge = factory.newDfp("-123456789012345678901234567890");
        Assert.assertTrue(negHuge.toString().contains("-"));

        Dfp sciZero = factory.getZero();
        sciZero.exp = -10;
        Assert.assertEquals("0.0e0", sciZero.dfp2sci());
    }

    @Test
    public void testDotrapAndClassify() {
        Dfp zero = factory.getZero();
        Dfp one = factory.getOne();

        Dfp trapInv = zero.dotrap(DfpField.FLAG_INVALID, "test", one, one);
        Assert.assertTrue(trapInv.isNaN());

        Dfp trapDivZero = one.dotrap(DfpField.FLAG_DIV_ZERO, "test", one, one);
        Assert.assertTrue(trapDivZero.isInfinite());

        Dfp trapDivZero0 = zero.dotrap(DfpField.FLAG_DIV_ZERO, "test", zero, zero);
        Assert.assertTrue(trapDivZero0.isNaN());

        Dfp qnan = factory.newDfp((byte) 1, Dfp.QNAN);
        Dfp trapDivZeroNan = qnan.dotrap(DfpField.FLAG_DIV_ZERO, "test", one, one);
        Assert.assertTrue(trapDivZeroNan.isNaN());

        Dfp pInf = factory.newDfp((byte) 1, Dfp.INFINITE);
        Dfp trapDivZeroInf = pInf.dotrap(DfpField.FLAG_DIV_ZERO, "test", one, one);
        Assert.assertTrue(trapDivZeroInf.isNaN());

        Dfp underflowRes = factory.newDfp("1.0");
        underflowRes.exp = Dfp.MIN_EXP - 100;
        Dfp trapUnder = zero.dotrap(DfpField.FLAG_UNDERFLOW, "test", one, underflowRes);
        Assert.assertTrue(trapUnder.isZero());

        Dfp underflowGradual = factory.newDfp("1.0");
        underflowGradual.exp = Dfp.MIN_EXP + 2;
        Dfp trapUnderG = zero.dotrap(DfpField.FLAG_UNDERFLOW, "test", one, underflowGradual);
        Assert.assertFalse(trapUnderG.isZero());

        Dfp overflowRes = factory.newDfp("1.0");
        overflowRes.exp = Dfp.MAX_EXP + 10;
        Dfp trapOver = zero.dotrap(DfpField.FLAG_OVERFLOW, "test", one, overflowRes);
        Assert.assertTrue(trapOver.isInfinite());

        Dfp defTrap = zero.dotrap(9999, "test", one, one);
        Assert.assertEquals(one, defTrap);

        Assert.assertEquals(Dfp.FINITE, one.classify());
        Assert.assertEquals(Dfp.INFINITE, pInf.classify());
        Assert.assertEquals(Dfp.QNAN, qnan.classify());
    }

    @Test
    public void testCopysignAndNextAfter() {
        Dfp pos = factory.newDfp("5.0");
        Dfp neg = factory.newDfp("-3.0");
        Assert.assertEquals(-5.0, Dfp.copysign(pos, neg).toDouble(), 1e-10);
        Assert.assertEquals(3.0, Dfp.copysign(neg, pos).toDouble(), 1e-10);

        Dfp nextSame = pos.nextAfter(pos);
        Assert.assertEquals(pos, nextSame);

        Dfp nextUp = pos.nextAfter(factory.newDfp("10.0"));
        Assert.assertTrue(nextUp.greaterThan(pos));

        Dfp nextDown = pos.nextAfter(factory.newDfp("1.0"));
        Assert.assertTrue(nextDown.lessThan(pos));

        Dfp negNextUp = neg.nextAfter(factory.newDfp("0.0"));
        Assert.assertTrue(negNextUp.greaterThan(neg));

        Dfp zero = factory.getZero();
        Dfp nextAfterZero = zero.nextAfter(factory.getOne());
        Assert.assertTrue(nextAfterZero.greaterThan(zero));

        Dfp zeroNextDown = zero.nextAfter(factory.getOne().negate());
        Assert.assertTrue(zeroNextDown.lessThan(zero));

        Dfp one = factory.getOne();
        Dfp nextDownOne = one.nextAfter(zero);
        Assert.assertTrue(nextDownOne.lessThan(one));

        Dfp diffField = factory20.getOne();
        Assert.assertTrue(pos.nextAfter(diffField).isNaN());
    }

    @Test
    public void testToDoubleAndToSplitDouble() {
        Dfp pInf = factory.newDfp((byte) 1, Dfp.INFINITE);
        Assert.assertEquals(Double.POSITIVE_INFINITY, pInf.toDouble(), 0.0);

        Dfp nInf = factory.newDfp((byte) -1, Dfp.INFINITE);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, nInf.toDouble(), 0.0);

        Dfp nan = factory.newDfp((byte) 1, Dfp.QNAN);
        Assert.assertTrue(Double.isNaN(nan.toDouble()));

        Dfp pZero = factory.getZero();
        Assert.assertEquals(0.0, pZero.toDouble(), 0.0);

        Dfp nZero = factory.getZero().negate();
        Assert.assertEquals(-0.0, nZero.toDouble(), 0.0);

        Dfp val = factory.newDfp("123.4567890123");
        Assert.assertEquals(123.4567890123, val.toDouble(), 1e-10);

        Dfp negVal = factory.newDfp("-123.4567890123");
        Assert.assertEquals(-123.4567890123, negVal.toDouble(), 1e-10);

        Dfp subnormalDfp = factory.newDfp("1e-315");
        Assert.assertTrue(subnormalDfp.toDouble() > 0.0);

        Dfp hugeDfp = factory.newDfp("1e309");
        Assert.assertEquals(Double.POSITIVE_INFINITY, hugeDfp.toDouble(), 0.0);

        Dfp negHugeDfp = factory.newDfp("-1e309");
        Assert.assertEquals(Double.NEGATIVE_INFINITY, negHugeDfp.toDouble(), 0.0);

        Dfp underflowDfp = factory.newDfp("1e-350");
        Assert.assertEquals(0.0, underflowDfp.toDouble(), 0.0);

        double[] split = val.toSplitDouble();
        Assert.assertEquals(2, split.length);
        Assert.assertEquals(val.toDouble(), split[0] + split[1], 1e-15);
    }

    @Test
    public void testNewInstanceWithPrecisionMismatch() {
        Dfp d6 = factory.getOne();
        Dfp d20 = factory20.getOne();

        Dfp copyMismatch = d6.newInstance(d20);
        Assert.assertTrue(copyMismatch.isNaN());
    }

    @Test
    public void testAbsAndNegate() {
        Dfp pos = factory.newDfp("12.34");
        Dfp neg = factory.newDfp("-12.34");

        Assert.assertEquals(pos, pos.abs());
        Assert.assertEquals(pos, neg.abs());

        Assert.assertEquals(neg, pos.negate());
        Assert.assertEquals(pos, neg.negate());
    }
}
