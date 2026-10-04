package org.apache.commons.math3.dfp;

import org.junit.Test;
import static org.junit.Assert.*;

public class DfpTest {

    private DfpField field = new DfpField(10);

    @Test
    public void testConstructors() {
        Dfp zero = field.newDfp(0);
        assertEquals(0, zero.intValue());
        assertEquals(1, zero.sign);
        assertEquals(0, zero.exp);
        assertEquals(Dfp.FINITE, zero.nans);

        Dfp one = field.newDfp(1);
        assertEquals(1, one.intValue());

        Dfp neg = field.newDfp(-1);
        assertEquals(-1, neg.intValue());

        Dfp fromLong = field.newDfp(123456789L);
        assertEquals(123456789, fromLong.intValue());

        Dfp fromDouble = field.newDfp(3.14);
        assertTrue(fromDouble.toDouble() > 3.13 && fromDouble.toDouble() < 3.15);

        Dfp fromString = field.newDfp("123.456");
        assertEquals(123456, fromString.multiply(1000).intValue());

        Dfp copy = new Dfp(one);
        assertEquals(one, copy);
    }

    @Test
    public void testSpecialValues() {
        Dfp inf = field.newDfp(Dfp.INFINITE, (byte)1);
        assertTrue(inf.isInfinite());
        assertFalse(inf.isNaN());
        assertFalse(inf.isZero());

        Dfp negInf = field.newDfp(Dfp.INFINITE, (byte)-1);
        assertTrue(negInf.isInfinite());
        assertEquals(-1, negInf.sign);

        Dfp qnan = field.newDfp(Dfp.QNAN, (byte)1);
        assertTrue(qnan.isNaN());
        assertFalse(qnan.isInfinite());

        Dfp snan = field.newDfp(Dfp.SNAN, (byte)1);
        assertTrue(snan.isNaN());
    }

    @Test
    public void testAdd() {
        Dfp a = field.newDfp(5);
        Dfp b = field.newDfp(3);
        Dfp sum = a.add(b);
        assertEquals(8, sum.intValue());

        Dfp neg = field.newDfp(-3);
        sum = a.add(neg);
        assertEquals(2, sum.intValue());

        Dfp zero = field.newDfp(0);
        sum = a.add(zero);
        assertEquals(5, sum.intValue());

        Dfp inf = field.newDfp(Dfp.INFINITE, (byte)1);
        sum = inf.add(a);
        assertTrue(sum.isInfinite());

        Dfp negInf = field.newDfp(Dfp.INFINITE, (byte)-1);
        sum = inf.add(negInf);
        assertTrue(sum.isNaN());
    }

    @Test
    public void testSubtract() {
        Dfp a = field.newDfp(10);
        Dfp b = field.newDfp(3);
        Dfp diff = a.subtract(b);
        assertEquals(7, diff.intValue());

        diff = a.subtract(a);
        assertEquals(0, diff.intValue());
        assertTrue(diff.isZero());
    }

    @Test
    public void testMultiply() {
        Dfp a = field.newDfp(6);
        Dfp b = field.newDfp(7);
        Dfp prod = a.multiply(b);
        assertEquals(42, prod.intValue());

        Dfp zero = field.newDfp(0);
        prod = a.multiply(zero);
        assertEquals(0, prod.intValue());
        assertTrue(prod.isZero());

        Dfp inf = field.newDfp(Dfp.INFINITE, (byte)1);
        prod = inf.multiply(a);
        assertTrue(prod.isInfinite());

        prod = inf.multiply(zero);
        assertTrue(prod.isNaN());
    }

    @Test
    public void testMultiplyInt() {
        Dfp a = field.newDfp(5);
        Dfp prod = a.multiply(3);
        assertEquals(15, prod.intValue());

        prod = a.multiply(0);
        assertEquals(0, prod.intValue());

        Dfp inf = field.newDfp(Dfp.INFINITE, (byte)1);
        prod = inf.multiply(5);
        assertTrue(prod.isInfinite());

        prod = inf.multiply(0);
        assertTrue(prod.isNaN());
    }

    @Test
    public void testDivide() {
        Dfp a = field.newDfp(10);
        Dfp b = field.newDfp(2);
        Dfp quot = a.divide(b);
        assertEquals(5, quot.intValue());

        Dfp zero = field.newDfp(0);
        quot = zero.divide(a);
        assertEquals(0, quot.intValue());

        quot = a.divide(zero);
        assertTrue(quot.isInfinite());

        quot = zero.divide(zero);
        assertTrue(quot.isNaN());

        Dfp inf = field.newDfp(Dfp.INFINITE, (byte)1);
        quot = inf.divide(a);
        assertTrue(quot.isInfinite());

        quot = a.divide(inf);
        assertTrue(quot.isZero());
    }

    @Test
    public void testDivideInt() {
        Dfp a = field.newDfp(20);
        Dfp quot = a.divide(4);
        assertEquals(5, quot.intValue());

        quot = a.divide(0);
        assertTrue(quot.isInfinite());

        Dfp inf = field.newDfp(Dfp.INFINITE, (byte)1);
        quot = inf.divide(2);
        assertTrue(quot.isInfinite());
    }

    @Test
    public void testSqrt() {
        Dfp a = field.newDfp(16);
        Dfp sqrt = a.sqrt();
        assertEquals(4, sqrt.intValue());

        Dfp zero = field.newDfp(0);
        sqrt = zero.sqrt();
        assertEquals(0, sqrt.intValue());

        Dfp inf = field.newDfp(Dfp.INFINITE, (byte)1);
        sqrt = inf.sqrt();
        assertTrue(sqrt.isInfinite());

        Dfp neg = field.newDfp(-1);
        sqrt = neg.sqrt();
        assertTrue(sqrt.isNaN());

        Dfp qnan = field.newDfp(Dfp.QNAN, (byte)1);
        sqrt = qnan.sqrt();
        assertTrue(sqrt.isNaN());
    }

    @Test
    public void testReciprocal() {
        Dfp a = field.newDfp(4);
        Dfp rec = a.reciprocal();
        assertEquals(0, rec.multiply(4).rint().intValue() - 1);

        Dfp zero = field.newDfp(0);
        rec = zero.reciprocal();
        assertTrue(rec.isInfinite());
    }

    @Test
    public void testNegate() {
        Dfp a = field.newDfp(5);
        Dfp neg = a.negate();
        assertEquals(-5, neg.intValue());

        Dfp zero = field.newDfp(0);
        neg = zero.negate();
        assertEquals(0, neg.intValue());
        assertEquals(-1, neg.sign);
    }

    @Test
    public void testAbs() {
        Dfp a = field.newDfp(-5);
        Dfp abs = a.abs();
        assertEquals(5, abs.intValue());
        assertEquals(1, abs.sign);

        Dfp pos = field.newDfp(5);
        abs = pos.abs();
        assertEquals(5, abs.intValue());
        assertEquals(1, abs.sign);
    }

    @Test
    public void testRint() {
        Dfp a = field.newDfp("3.5");
        Dfp rint = a.rint();
        assertEquals(4, rint.intValue());

        a = field.newDfp("2.5");
        rint = a.rint();
        assertEquals(2, rint.intValue());

        a = field.newDfp("2.6");
        rint = a.rint();
        assertEquals(3, rint.intValue());
    }

    @Test
    public void testFloor() {
        Dfp a = field.newDfp("3.7");
        Dfp floor = a.floor();
        assertEquals(3, floor.intValue());

        a = field.newDfp("-3.7");
        floor = a.floor();
        assertEquals(-4, floor.intValue());
    }

    @Test
    public void testCeil() {
        Dfp a = field.newDfp("3.2");
        Dfp ceil = a.ceil();
        assertEquals(4, ceil.intValue());

        a = field.newDfp("-3.2");
        ceil = a.ceil();
        assertEquals(-3, ceil.intValue());
    }

    @Test
    public void testRemainder() {
        Dfp a = field.newDfp(10);
        Dfp b = field.newDfp(3);
        Dfp rem = a.remainder(b);
        assertEquals(1, rem.intValue());

        a = field.newDfp(-10);
        rem = a.remainder(b);
        assertEquals(-1, rem.intValue());
    }

    @Test
    public void testComparisons() {
        Dfp a = field.newDfp(5);
        Dfp b = field.newDfp(3);
        Dfp c = field.newDfp(5);

        assertTrue(a.greaterThan(b));
        assertFalse(a.lessThan(b));
        assertTrue(b.lessThan(a));
        assertFalse(b.greaterThan(a));
        assertTrue(a.equals(c));
        assertFalse(a.unequal(c));
        assertTrue(a.unequal(b));

        Dfp nan = field.newDfp(Dfp.QNAN, (byte)1);
        assertFalse(a.lessThan(nan));
        assertFalse(a.greaterThan(nan));
        assertFalse(a.equals(nan));
    }

    @Test
    public void testSignChecks() {
        Dfp pos = field.newDfp(5);
        assertTrue(pos.positiveOrNull());
        assertTrue(pos.strictlyPositive());
        assertFalse(pos.negativeOrNull());
        assertFalse(pos.strictlyNegative());

        Dfp neg = field.newDfp(-5);
        assertFalse(neg.positiveOrNull());
        assertFalse(neg.strictlyPositive());
        assertTrue(neg.negativeOrNull());
        assertTrue(neg.strictlyNegative());

        Dfp zero = field.newDfp(0);
        assertTrue(zero.positiveOrNull());
        assertFalse(zero.strictlyPositive());
        assertTrue(zero.negativeOrNull());
        assertFalse(zero.strictlyNegative());
    }

    @Test
    public void testIsZero() {
        Dfp zero = field.newDfp(0);
        assertTrue(zero.isZero());

        Dfp one = field.newDfp(1);
        assertFalse(one.isZero());

        Dfp nan = field.newDfp(Dfp.QNAN, (byte)1);
        assertFalse(nan.isZero());
    }

    @Test
    public void testIntValue() {
        Dfp a = field.newDfp(123);
        assertEquals(123, a.intValue());

        Dfp big = field.newDfp("2147483648");
        assertEquals(2147483647, big.intValue());

        Dfp small = field.newDfp("-2147483649");
        assertEquals(-2147483648, small.intValue());
    }

    @Test
    public void testLog10K() {
        Dfp a = field.newDfp(10000);
        assertEquals(0, a.log10K());

        Dfp b = field.newDfp(100000000);
        assertEquals(1, b.log10K());
    }

    @Test
    public void testPower10K() {
        Dfp a = field.newDfp(1);
        Dfp pow = a.power10K(2);
        assertEquals(100000000, pow.intValue());
    }

    @Test
    public void testLog10() {
        Dfp a = field.newDfp(1000);
        assertEquals(3, a.log10());

        Dfp b = field.newDfp(100);
        assertEquals(2, b.log10());

        Dfp c = field.newDfp(10);
        assertEquals(1, c.log10());

        Dfp d = field.newDfp(1);
        assertEquals(0, d.log10());
    }

    @Test
    public void testPower10() {
        Dfp a = field.newDfp(1);
        Dfp pow = a.power10(3);
        assertEquals(1000, pow.intValue());

        pow = a.power10(-2);
        assertEquals(0, pow.multiply(100).rint().intValue() - 1);
    }

    @Test
    public void testToString() {
        Dfp a = field.newDfp(123.456);
        assertNotNull(a.toString());

        Dfp inf = field.newDfp(Dfp.INFINITE, (byte)1);
        assertEquals("Infinity", inf.toString());

        Dfp negInf = field.newDfp(Dfp.INFINITE, (byte)-1);
        assertEquals("-Infinity", negInf.toString());

        Dfp nan = field.newDfp(Dfp.QNAN, (byte)1);
        assertEquals("NaN", nan.toString());
    }

    @Test
    public void testToDouble() {
        Dfp a = field.newDfp(3.14);
        assertEquals(3.14, a.toDouble(), 0.001);

        Dfp zero = field.newDfp(0);
        assertEquals(0.0, zero.toDouble(), 0.0);

        Dfp inf = field.newDfp(Dfp.INFINITE, (byte)1);
        assertEquals(Double.POSITIVE_INFINITY, inf.toDouble(), 0.0);

        Dfp negInf = field.newDfp(Dfp.INFINITE, (byte)-1);
        assertEquals(Double.NEGATIVE_INFINITY, negInf.toDouble(), 0.0);

        Dfp nan = field.newDfp(Dfp.QNAN, (byte)1);
        assertTrue(Double.isNaN(nan.toDouble()));
    }

    @Test
    public void testToSplitDouble() {
        Dfp a = field.newDfp(3.14);
        double[] split = a.toSplitDouble();
        assertEquals(2, split.length);
        assertEquals(3.14, split[0] + split[1], 0.001);
    }

    @Test
    public void testClassify() {
        Dfp a = field.newDfp(1);
        assertEquals(Dfp.FINITE, a.classify());

        Dfp inf = field.newDfp(Dfp.INFINITE, (byte)1);
        assertEquals(Dfp.INFINITE, inf.classify());

        Dfp qnan = field.newDfp(Dfp.QNAN, (byte)1);
        assertEquals(Dfp.QNAN, qnan.classify());

        Dfp snan = field.newDfp(Dfp.SNAN, (byte)1);
        assertEquals(Dfp.SNAN, snan.classify());
    }

    @Test
    public void testCopysign() {
        Dfp x = field.newDfp(5);
        Dfp y = field.newDfp(-1);
        Dfp result = Dfp.copysign(x, y);
        assertEquals(5, result.intValue());
        assertEquals(-1, result.sign);

        result = Dfp.copysign(x, x);
        assertEquals(1, result.sign);
    }

    @Test
    public void testNextAfter() {
        Dfp a = field.newDfp(5);
        Dfp b = field.newDfp(10);
        Dfp next = a.nextAfter(b);
        assertTrue(next.greaterThan(a));

        next = b.nextAfter(a);
        assertTrue(next.lessThan(b));

        Dfp same = a.nextAfter(a);
        assertEquals(a, same);

        Dfp zero = field.newDfp(0);
        next = zero.nextAfter(field.newDfp(1));
        assertTrue(next.greaterThan(zero));
    }

    @Test
    public void testNewInstance() {
        Dfp a = field.newDfp(5);
        Dfp copy = a.newInstance(a);
        assertEquals(a, copy);

        Dfp fromInt = a.newInstance(10);
        assertEquals(10, fromInt.intValue());

        Dfp fromLong = a.newInstance(100L);
        assertEquals(100, fromLong.intValue());

        Dfp fromDouble = a.newInstance(2.5);
        assertEquals(2.5, fromDouble.toDouble(), 0.001);

        Dfp fromString = a.newInstance("7.5");
        assertEquals(7.5, fromString.toDouble(), 0.001);

        Dfp fromByte = a.newInstance((byte)3);
        assertEquals(3, fromByte.intValue());

        Dfp special = a.newInstance((byte)1, Dfp.INFINITE);
        assertTrue(special.isInfinite());
    }

    @Test
    public void testHashCode() {
        Dfp a = field.newDfp(5);
        Dfp b = field.newDfp(5);
        assertEquals(a.hashCode(), b.hashCode());

        Dfp c = field.newDfp(6);
        assertNotEquals(a.hashCode(), c.hashCode());
    }

    @Test
    public void testEquals() {
        Dfp a = field.newDfp(5);
        Dfp b = field.newDfp(5);
        assertTrue(a.equals(b));

        Dfp c = field.newDfp(6);
        assertFalse(a.equals(c));

        assertFalse(a.equals(null));
        assertFalse(a.equals("string"));
    }

    @Test
    public void testLongMinValue() {
        Dfp minLong = field.newDfp(Long.MIN_VALUE);
        assertEquals(Long.MIN_VALUE, minLong.toDouble(), 1e10);
    }

    @Test
    public void testDoubleSubnormal() {
        Dfp subnormal = field.newDfp(Double.MIN_VALUE);
        assertTrue(subnormal.toDouble() > 0);
    }

    @Test
    public void testDoubleNaN() {
        Dfp nan = field.newDfp(Double.NaN);
        assertTrue(nan.isNaN());
    }

    @Test
    public void testDoubleInfinity() {
        Dfp inf = field.newDfp(Double.POSITIVE_INFINITY);
        assertTrue(inf.isInfinite());
        assertEquals(1, inf.sign);

        Dfp negInf = field.newDfp(Double.NEGATIVE_INFINITY);
        assertTrue(negInf.isInfinite());
        assertEquals(-1, negInf.sign);
    }

    @Test
    public void testStringScientific() {
        Dfp a = field.newDfp("1e10");
        assertEquals(10000000000L, a.toDouble(), 1e5);
    }

    @Test
    public void testStringNegativeScientific() {
        Dfp a = field.newDfp("-1.5e-3");
        assertEquals(-0.0015, a.toDouble(), 0.0001);
    }

    @Test
    public void testStringInfinity() {
        Dfp inf = field.newDfp("Infinity");
        assertTrue(inf.isInfinite());
        assertEquals(1, inf.sign);

        Dfp negInf = field.newDfp("-Infinity");
        assertTrue(negInf.isInfinite());
        assertEquals(-1, negInf.sign);
    }

    @Test
    public void testStringNaN() {
        Dfp nan = field.newDfp("NaN");
        assertTrue(nan.isNaN());
    }

    @Test
    public void testAlign() {
        Dfp a = field.newDfp(1);
        a.align(5);
        assertEquals(5, a.exp);
    }

    @Test
    public void testShiftLeft() {
        Dfp a = field.newDfp(1);
        int oldExp = a.exp;
        a.shiftLeft();
        assertEquals(oldExp - 1, a.exp);
    }

    @Test
    public void testShiftRight() {
        Dfp a = field.newDfp(1);
        int oldExp = a.exp;
        a.shiftRight();
        assertEquals(oldExp + 1, a.exp);
    }

    @Test
    public void testComplement() {
        Dfp a = field.newDfp(5);
        a.complement(0);
        // Just ensure no exception
    }

    @Test
    public void testRound() {
        Dfp a = field.newDfp(1);
        a.round(5000);
        // Just ensure no exception
    }

    @Test
    public void testTrap() {
        Dfp a = field.newDfp(1);
        Dfp result = a.dotrap(DfpField.FLAG_INVALID, "test", a, a);
        assertTrue(result.isNaN());
    }

    @Test
    public void testGetField() {
        Dfp a = field.newDfp(1);
        assertNotNull(a.getField());
        assertEquals(field, a.getField());
    }

    @Test
    public void testGetRadixDigits() {
        Dfp a = field.newDfp(1);
        assertEquals(10, a.getRadixDigits());
    }

    @Test
    public void testGetZero() {
        Dfp zero = field.newDfp(1).getZero();
        assertTrue(zero.isZero());
    }

    @Test
    public void testGetOne() {
        Dfp one = field.newDfp(1).getOne();
        assertEquals(1, one.intValue());
    }

    @Test
    public void testGetTwo() {
        Dfp two = field.newDfp(1).getTwo();
        assertEquals(2, two.intValue());
    }

    @Test
    public void testMultiplyFast() {
        Dfp a = field.newDfp(5);
        Dfp prod = a.multiply(3);
        assertEquals(15, prod.intValue());
    }

    @Test(expected = NullPointerException.class)
    public void testAddNull() {
        Dfp a = field.newDfp(1);
        a.add(null);
    }

    @Test
    public void testDifferentPrecision() {
        DfpField field2 = new DfpField(5);
        Dfp a = field.newDfp(1);
        Dfp b = field2.newDfp(1);
        Dfp sum = a.add(b);
        assertTrue(sum.isNaN());
    }
}
