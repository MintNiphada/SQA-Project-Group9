package org.apache.commons.math3.util;

import static org.junit.Assert.*;
import org.junit.Test;

public class FastMathTest {

    @Test
    public void testSqrt() {
        assertEquals(2.0, FastMath.sqrt(4.0), 0.0);
        assertEquals(0.0, FastMath.sqrt(0.0), 0.0);
        assertTrue(Double.isNaN(FastMath.sqrt(-1.0)));
        assertTrue(Double.isNaN(FastMath.sqrt(Double.NaN)));
        assertEquals(Double.POSITIVE_INFINITY, FastMath.sqrt(Double.POSITIVE_INFINITY), 0.0);
    }

    @Test
    public void testCosh() {
        assertEquals(1.0, FastMath.cosh(0.0), 1e-15);
        assertTrue(Double.isNaN(FastMath.cosh(Double.NaN)));
        double x = 25.0;
        assertEquals(0.5 * FastMath.exp(x), FastMath.cosh(x), 1e-12);
        x = -25.0;
        assertEquals(0.5 * FastMath.exp(-x), FastMath.cosh(x), 1e-12);
        x = 0.5;
        assertEquals(Math.cosh(x), FastMath.cosh(x), 1e-14);
        x = -0.5;
        assertEquals(Math.cosh((-0.5)), FastMath.cosh(x), 1e-14);
    }

    @Test
    public void testSinh() {
        assertEquals(0.0, FastMath.sinh(0.0), 1e-15);
        assertTrue(Double.isNaN(FastMath.sinh(Double.NaN)));
        double x = 25.0;
        assertEquals(0.5 * FastMath.exp(x), FastMath.sinh(x), 1e-12);
        x = -25.0;
        assertEquals(-0.5 * FastMath.exp(-x), FastMath.sinh(x), 1e-12);
        x = 0.5;
        assertEquals(Math.sinh(x), FastMath.sinh(x), 1e-14);
        x = -0.5;
        assertEquals(Math.sinh((-0.5)), FastMath.sinh(x), 1e-14);
        x = 0.1;
        assertEquals(Math.sinh(x), FastMath.sinh(x), 1e-14);
    }

    @Test
    public void testTanh() {
        assertEquals(0.0, FastMath.tanh(0.0), 1e-15);
        assertTrue(Double.isNaN(FastMath.tanh(Double.NaN)));
        assertEquals(1.0, FastMath.tanh(25.0), 1e-15);
        assertEquals(-1.0, FastMath.tanh(-25.0), 1e-15);
        assertEquals(Math.tanh(0.5), FastMath.tanh(0.5), 1e-15);
        assertEquals(Math.tanh((-0.5)), FastMath.tanh(-0.5), 1e-15);
        assertEquals(Math.tanh(0.1), FastMath.tanh(0.1), 1e-15);
    }

    @Test
    public void testAcosh() {
        assertEquals(0.0, FastMath.acosh(1.0), 1e-15);
        assertEquals(Math.acosh(2.0), FastMath.acosh(2.0), 1e-14);
        assertTrue(Double.isNaN(FastMath.acosh(0.5)));
        assertTrue(Double.isNaN(FastMath.acosh(Double.NaN)));
    }

    @Test
    public void testAsinh() {
        assertEquals(0.0, FastMath.asinh(0.0), 1e-15);
        assertTrue(Double.isNaN(FastMath.asinh(Double.NaN)));
        assertEquals(Math.asinh(0.5), FastMath.asinh(0.5), 1e-14);
        assertEquals(Math.asinh((-0.5)), FastMath.asinh(-0.5), 1e-14);
        assertEquals(Math.asinh(0.05), FastMath.asinh(0.05), 1e-14);
        assertEquals(Math.asinh(0.005), FastMath.asinh(0.005), 1e-14);
        assertEquals(Math.asinh(0.0005), FastMath.asinh(0.0005), 1e-14);
    }

    @Test
    public void testAtanh() {
        assertEquals(0.0, FastMath.atanh(0.0), 1e-15);
        assertTrue(Double.isNaN(FastMath.atanh(Double.NaN)));
        assertEquals(Math.atanh(0.5), FastMath.atanh(0.5), 1e-14);
        assertEquals(Math.atanh((-0.5)), FastMath.atanh(-0.5), 1e-14);
        assertEquals(Math.atanh(0.1), FastMath.atanh(0.1), 1e-14);
        assertEquals(Math.atanh(0.01), FastMath.atanh(0.01), 1e-14);
        assertEquals(Math.atanh(0.001), FastMath.atanh(0.001), 1e-14);
    }

    @Test
    public void testSignumDouble() {
        assertEquals(1.0, FastMath.signum(5.0), 0.0);
        assertEquals(-1.0, FastMath.signum(-5.0), 0.0);
        assertEquals(0.0, FastMath.signum(0.0), 0.0);
        assertEquals(-0.0, FastMath.signum(-0.0), 0.0);
        assertTrue(Double.isNaN(FastMath.signum(Double.NaN)));
    }

    @Test
    public void testSignumFloat() {
        assertEquals(1.0f, FastMath.signum(5.0f), 0.0f);
        assertEquals(-1.0f, FastMath.signum(-5.0f), 0.0f);
        assertEquals(0.0f, FastMath.signum(0.0f), 0.0f);
        assertEquals(-0.0f, FastMath.signum(-0.0f), 0.0f);
        assertTrue(Float.isNaN(FastMath.signum(Float.NaN)));
    }

    @Test
    public void testNextUp() {
        assertEquals(Double.MIN_VALUE, FastMath.nextUp(0.0), 0.0);
        assertEquals(Double.MIN_VALUE, FastMath.nextUp(-0.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.nextUp(Double.MAX_VALUE), 0.0);
        assertEquals(Double.MAX_VALUE, FastMath.nextUp(Double.POSITIVE_INFINITY), 0.0);
        assertEquals(Float.MIN_VALUE, FastMath.nextUp(0.0f), 0.0f);
        assertEquals(Float.MIN_VALUE, FastMath.nextUp(-0.0f), 0.0f);
        assertEquals(Float.POSITIVE_INFINITY, FastMath.nextUp(Float.MAX_VALUE), 0.0f);
    }

    @Test
    public void testRandom() {
        double r = FastMath.random();
        assertTrue(r >= 0.0 && r < 1.0);
    }

    @Test
    public void testExp() {
        assertEquals(1.0, FastMath.exp(0.0), 1e-15);
        assertEquals(Math.E, FastMath.exp(1.0), 1e-14);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.exp(800.0), 0.0);
        assertEquals(0.0, FastMath.exp(-800.0), 0.0);
        assertTrue(Double.isNaN(FastMath.exp(Double.NaN)));
        assertEquals(Math.exp(2.0), FastMath.exp(2.0), 1e-14);
    }

    @Test
    public void testExpm1() {
        assertEquals(0.0, FastMath.expm1(0.0), 1e-15);
        assertTrue(Double.isNaN(FastMath.expm1(Double.NaN)));
        assertEquals(Math.expm1(0.5), FastMath.expm1(0.5), 1e-14);
        assertEquals(Math.expm1((-0.5)), FastMath.expm1(-0.5), 1e-14);
        assertEquals(Math.expm1(1.0), FastMath.expm1(1.0), 1e-14);
        assertEquals(Math.expm1((-1.0)), FastMath.expm1(-1.0), 1e-14);
        assertEquals(Math.expm1(20.0), FastMath.expm1(20.0), 1e-13);
        assertEquals(Math.expm1((-20.0)), FastMath.expm1(-20.0), 1e-13);
        double small = 1e-8;
        assertEquals(Math.expm1(small), FastMath.expm1(small), 1e-20);
    }

    @Test
    public void testLog() {
        assertEquals(0.0, FastMath.log(1.0), 1e-15);
        assertEquals(1.0, FastMath.log(Math.E), 1e-14);
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.log(0.0), 0.0);
        assertTrue(Double.isNaN(FastMath.log(-1.0)));
        assertTrue(Double.isNaN(FastMath.log(Double.NaN)));
        assertEquals(Double.POSITIVE_INFINITY, FastMath.log(Double.POSITIVE_INFINITY), 0.0);
    }

    @Test
    public void testLogSpecialValues() {
        assertEquals(1.0, FastMath.log(10.0) / FastMath.log(10.0), 1e-15);
    }

    @Test
    public void testLog1p() {
        assertEquals(0.0, FastMath.log1p(0.0), 1e-15);
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.log1p(-1.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.log1p(Double.POSITIVE_INFINITY), 0.0);
        assertEquals(Math.log1p(1e-7), FastMath.log1p(1e-7), 1e-20);
        assertEquals(Math.log1p((-1e-7)), FastMath.log1p(-1e-7), 1e-20);
        assertEquals(Math.log1p(2.0), FastMath.log1p(2.0), 1e-14);
    }

    @Test
    public void testLog10() {
        assertEquals(0.0, FastMath.log10(1.0), 1e-15);
        assertEquals(1.0, FastMath.log10(10.0), 1e-14);
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.log10(0.0), 0.0);
        assertTrue(Double.isNaN(FastMath.log10(-1.0)));
    }

    @Test
    public void testLogBase() {
        assertEquals(1.0, FastMath.log(2.0, 2.0), 1e-15);
        assertEquals(0.0, FastMath.log(2.0, 1.0), 1e-15);
        assertTrue(Double.isNaN(FastMath.log(0.0, 5.0)));
    }

    @Test
    public void testPowDoubleDouble() {
        assertEquals(1.0, FastMath.pow(0.0, 0.0), 0.0);
        assertEquals(1.0, FastMath.pow(1.0, 0.0), 0.0);
        assertEquals(8.0, FastMath.pow(2.0, 3.0), 1e-14);
        assertEquals(1.0/8.0, FastMath.pow(2.0, -3.0), 1e-16);
        assertTrue(Double.isNaN(FastMath.pow(Double.NaN, 2.0)));
        assertTrue(Double.isNaN(FastMath.pow(2.0, Double.NaN)));
        assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(0.0, -0.1), 0.0);
        assertEquals(0.0, FastMath.pow(0.0, 0.1), 0.0);
        assertEquals(0.0, FastMath.pow(Double.POSITIVE_INFINITY, -1.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(Double.POSITIVE_INFINITY, 1.0), 0.0);
        assertTrue(Double.isNaN(FastMath.pow(Double.POSITIVE_INFINITY, Double.NaN)));
        assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(0.5, Double.POSITIVE_INFINITY), 0.0);
        assertEquals(0.0, FastMath.pow(2.0, Double.POSITIVE_INFINITY), 0.0);
        assertEquals(0.0, FastMath.pow(Double.NEGATIVE_INFINITY, -2.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(Double.NEGATIVE_INFINITY, 2.0), 0.0);
        assertEquals(-0.0, FastMath.pow(Double.NEGATIVE_INFINITY, -1.0), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.pow(Double.NEGATIVE_INFINITY, 1.0), 0.0);
        assertEquals(0.0, FastMath.pow(-0.0, 0.5), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(-0.0, -0.5), 0.0);
        assertEquals(4.0, FastMath.pow(-2.0, 2.0), 1e-14);
        assertEquals(-8.0, FastMath.pow(-2.0, 3.0), 1e-14);
        assertTrue(Double.isNaN(FastMath.pow(-2.0, 0.5)));
        assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(2.0, 1000.0), 0.0);
    }

    @Test
    public void testPowDoubleInt() {
        assertEquals(1.0, FastMath.pow(5.0, 0), 0.0);
        assertEquals(8.0, FastMath.pow(2.0, 3), 1e-14);
        assertEquals(0.125, FastMath.pow(2.0, -3), 1e-16);
        assertEquals(1.0/9.0, FastMath.pow(3.0, -2), 1e-16);
        assertEquals(0.0, FastMath.pow(0.0, 5), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(0.0, -5), 0.0);
    }

    @Test
    public void testCeil() {
        assertEquals(3.0, FastMath.ceil(2.5), 0.0);
        assertEquals(-2.0, FastMath.ceil(-2.5), 0.0);
        assertEquals(0.0, FastMath.ceil(0.0), 0.0);
        assertEquals(-0.0, FastMath.ceil(-0.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.ceil(Double.POSITIVE_INFINITY), 0.0);
        assertTrue(Double.isNaN(FastMath.ceil(Double.NaN)));
        assertEquals(1.0, FastMath.ceil(1.0), 0.0);
        assertEquals(4.503599627370496E15, FastMath.ceil(4.503599627370496E15), 0.0);
    }

    @Test
    public void testFloor() {
        assertEquals(2.0, FastMath.floor(2.5), 0.0);
        assertEquals(-3.0, FastMath.floor(-2.5), 0.0);
        assertEquals(0.0, FastMath.floor(0.0), 0.0);
        assertEquals(-0.0, FastMath.floor(-0.0), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.floor(Double.NEGATIVE_INFINITY), 0.0);
        assertTrue(Double.isNaN(FastMath.floor(Double.NaN)));
    }

    @Test
    public void testRint() {
        assertEquals(2.0, FastMath.rint(2.5), 0.0);
        assertEquals(2.0, FastMath.rint(1.5), 0.0);
        assertEquals(0.0, FastMath.rint(0.5), 0.0);
        assertEquals(-0.0, FastMath.rint(-0.5), 0.0);
        assertEquals(-2.0, FastMath.rint(-1.5), 0.0);
        assertEquals(-2.0, FastMath.rint(-2.5), 0.0);
        assertEquals(2.0, FastMath.rint(2.0), 0.0);
        assertEquals(-2.0, FastMath.rint(-2.0), 0.0);
        assertEquals(3.0, FastMath.rint(2.5000000000001), 0.0);
    }

    @Test
    public void testRoundDouble() {
        assertEquals(1L, FastMath.round(0.5));
        assertEquals(0L, FastMath.round(0.4));
        assertEquals(-1L, FastMath.round(-0.5));
    }

    @Test
    public void testRoundFloat() {
        assertEquals(1, FastMath.round(0.5f));
        assertEquals(0, FastMath.round(0.4f));
        assertEquals(-1, FastMath.round(-0.5f));
    }

    @Test
    public void testMinInt() {
        assertEquals(1, FastMath.min(1, 2));
        assertEquals(1, FastMath.min(2, 1));
        assertEquals(1, FastMath.min(1, 1));
    }

    @Test
    public void testMinLong() {
        assertEquals(1L, FastMath.min(1L, 2L));
        assertEquals(1L, FastMath.min(2L, 1L));
        assertEquals(1L, FastMath.min(1L, 1L));
    }

    @Test
    public void testMinFloat() {
        assertEquals(-1.0f, FastMath.min(1.0f, -1.0f), 0.0f);
        assertEquals(-1.0f, FastMath.min(-1.0f, 1.0f), 0.0f);
        assertEquals(0.0f, FastMath.min(0.0f, -0.0f), 0.0f);
        assertTrue(Float.isNaN(FastMath.min(Float.NaN, 1.0f)));
        assertTrue(Float.isNaN(FastMath.min(1.0f, Float.NaN)));
    }

    @Test
    public void testMinDouble() {
        assertEquals(-1.0, FastMath.min(1.0, -1.0), 0.0);
        assertEquals(-1.0, FastMath.min(-1.0, 1.0), 0.0);
        assertEquals(-0.0, FastMath.min(0.0, -0.0), 0.0);
        assertTrue(Double.isNaN(FastMath.min(Double.NaN, 1.0)));
        assertTrue(Double.isNaN(FastMath.min(1.0, Double.NaN)));
    }

    @Test
    public void testMaxInt() {
        assertEquals(2, FastMath.max(1, 2));
        assertEquals(2, FastMath.max(2, 1));
    }

    @Test
    public void testMaxLong() {
        assertEquals(2L, FastMath.max(1L, 2L));
        assertEquals(2L, FastMath.max(2L, 1L));
    }

    @Test
    public void testMaxFloat() {
        assertEquals(1.0f, FastMath.max(1.0f, -1.0f), 0.0f);
        assertEquals(1.0f, FastMath.max(-1.0f, 1.0f), 0.0f);
        assertEquals(0.0f, FastMath.max(0.0f, -0.0f), 0.0f);
        assertTrue(Float.isNaN(FastMath.max(Float.NaN, 1.0f)));
        assertTrue(Float.isNaN(FastMath.max(1.0f, Float.NaN)));
    }

    @Test
    public void testMaxDouble() {
        assertEquals(1.0, FastMath.max(-1.0, 1.0), 0.0);
        assertEquals(1.0, FastMath.max(1.0, -1.0), 0.0);
        assertEquals(0.0, FastMath.max(0.0, -0.0), 0.0);
        assertTrue(Double.isNaN(FastMath.max(Double.NaN, 1.0)));
    }

    @Test
    public void testHypot() {
        assertEquals(5.0, FastMath.hypot(3.0, 4.0), 1e-14);
        assertEquals(0.0, FastMath.hypot(0.0, 0.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.hypot(Double.POSITIVE_INFINITY, 1.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.hypot(1.0, Double.NEGATIVE_INFINITY), 0.0);
        assertTrue(Double.isNaN(FastMath.hypot(Double.NaN, 1.0)));
        assertEquals(Math.abs(1e100), FastMath.hypot(1e100, 1e70), 1e85);
    }

    @Test
    public void testIEEEremainder() {
        assertEquals(0.0, FastMath.IEEEremainder(4.0, 2.0), 0.0);
        assertEquals(0.0, FastMath.IEEEremainder(5.0, 1.0), 1e-14);
        assertTrue(Double.isNaN(FastMath.IEEEremainder(Double.NaN, 1.0)));
    }

    @Test
    public void testCopySignDouble() {
        assertEquals(1.0, FastMath.copySign(1.0, 2.0), 0.0);
        assertEquals(-1.0, FastMath.copySign(1.0, -2.0), 0.0);
        assertEquals(1.0, FastMath.copySign(-1.0, 2.0), 0.0);
        assertEquals(-1.0, FastMath.copySign(-1.0, -2.0), 0.0);
        assertEquals(1.0, FastMath.copySign(1.0, Double.NaN), 0.0);
    }

    @Test
    public void testCopySignFloat() {
        assertEquals(1.0f, FastMath.copySign(1.0f, 2.0f), 0.0f);
        assertEquals(-1.0f, FastMath.copySign(1.0f, -2.0f), 0.0f);
        assertEquals(1.0f, FastMath.copySign(-1.0f, 2.0f), 0.0f);
        assertEquals(-1.0f, FastMath.copySign(-1.0f, -2.0f), 0.0f);
    }

    @Test
    public void testGetExponentDouble() {
        assertEquals(0, FastMath.getExponent(1.0));
        assertEquals(-1022, FastMath.getExponent(Double.MIN_NORMAL));
        assertEquals(1023, FastMath.getExponent(Double.MAX_VALUE));
        assertEquals(1024, FastMath.getExponent(Double.POSITIVE_INFINITY));
        assertEquals(-1023, FastMath.getExponent(Double.MIN_VALUE));
    }

    @Test
    public void testGetExponentFloat() {
        assertEquals(0, FastMath.getExponent(1.0f));
        assertEquals(-126, FastMath.getExponent(Float.MIN_NORMAL));
        assertEquals(127, FastMath.getExponent(Float.MAX_VALUE));
        assertEquals(128, FastMath.getExponent(Float.POSITIVE_INFINITY));
    }

    @Test
    public void testAbsInt() {
        assertEquals(5, FastMath.abs(-5));
        assertEquals(5, FastMath.abs(5));
        assertEquals(0, FastMath.abs(0));
        assertEquals(Integer.MIN_VALUE, FastMath.abs(Integer.MIN_VALUE));
    }

    @Test
    public void testAbsLong() {
        assertEquals(5L, FastMath.abs(-5L));
        assertEquals(5L, FastMath.abs(5L));
        assertEquals(0L, FastMath.abs(0L));
        assertEquals(Long.MIN_VALUE, FastMath.abs(Long.MIN_VALUE));
    }

    @Test
    public void testAbsFloat() {
        assertEquals(5.0f, FastMath.abs(-5.0f), 0.0f);
        assertEquals(5.0f, FastMath.abs(5.0f), 0.0f);
        assertEquals(0.0f, FastMath.abs(0.0f), 0.0f);
        assertEquals(0.0f, FastMath.abs(-0.0f), 0.0f);
    }

    @Test
    public void testAbsDouble() {
        assertEquals(5.0, FastMath.abs(-5.0), 0.0);
        assertEquals(5.0, FastMath.abs(5.0), 0.0);
        assertEquals(0.0, FastMath.abs(0.0), 0.0);
        assertEquals(0.0, FastMath.abs(-0.0), 0.0);
    }

    @Test
    public void testUlpDouble() {
        assertEquals(Double.POSITIVE_INFINITY, FastMath.ulp(Double.POSITIVE_INFINITY), 0.0);
        assertEquals(Double.MIN_VALUE, FastMath.ulp(0.0), 0.0);
        assertEquals(Double.longBitsToDouble(Double.doubleToLongBits(1.0) ^ 1) - 1.0, FastMath.ulp(1.0), 0.0);
        assertEquals(FastMath.ulp(0.0) * 1024, FastMath.ulp(Double.MIN_NORMAL), 1e-300);
    }

    @Test
    public void testUlpFloat() {
        assertEquals(Float.POSITIVE_INFINITY, FastMath.ulp(Float.POSITIVE_INFINITY), 0.0f);
        assertEquals(Float.MIN_VALUE, FastMath.ulp(0.0f), 0.0f);
        assertEquals(FastMath.abs(Float.intBitsToFloat(Float.floatToIntBits(1.0f) ^ 1) - 1.0f), FastMath.ulp(1.0f), 0.0f);
    }

    @Test
    public void testScalbDouble() {
        assertEquals(2.0, FastMath.scalb(1.0, 1), 0.0);
        assertEquals(0.5, FastMath.scalb(1.0, -1), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.scalb(Double.MAX_VALUE, 100), 0.0);
        assertEquals(0.0, FastMath.scalb(Double.MIN_VALUE, -100), 0.0);
        assertEquals(0.0, FastMath.scalb(1.0, -1075), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.scalb(1.0, 1024), 0.0);
        assertTrue(Double.isNaN(FastMath.scalb(Double.NaN, 1)));
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.scalb(Double.NEGATIVE_INFINITY, 100), 0.0);
        assertEquals(-0.0, FastMath.scalb(-1.0, -1074), 0.0);
    }

    @Test
    public void testScalbFloat() {
        assertEquals(2.0f, FastMath.scalb(1.0f, 1), 0.0f);
        assertEquals(0.5f, FastMath.scalb(1.0f, -1), 0.0f);
        assertEquals(Float.POSITIVE_INFINITY, FastMath.scalb(Float.MAX_VALUE, 100), 0.0f);
        assertEquals(0.0f, FastMath.scalb(Float.MIN_VALUE, -100), 0.0f);
        assertTrue(Float.isNaN(FastMath.scalb(Float.NaN, 1)));
    }

    @Test
    public void testNextAfterDouble() {
        assertEquals(Double.MIN_VALUE, FastMath.nextAfter(0.0, 1.0), 0.0);
        assertEquals(-Double.MIN_VALUE, FastMath.nextAfter(0.0, -1.0), 0.0);
        assertEquals(Double.MAX_VALUE, FastMath.nextAfter(Double.POSITIVE_INFINITY, -1.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.nextAfter(Double.MAX_VALUE, 1.0), 0.0);
        assertEquals(0.0, FastMath.nextAfter(Double.MIN_VALUE, -1.0), 0.0);
        assertEquals(Double.doubleToLongBits(1.0) == Double.doubleToLongBits(FastMath.nextAfter(1.0, 2.0)) ? 1.0 : 0.0, 0.0);
    }

    @Test
    public void testNextAfterFloat() {
        assertEquals(Float.MIN_VALUE, FastMath.nextAfter(0.0f, 1.0), 0.0f);
        assertEquals(-Float.MIN_VALUE, FastMath.nextAfter(0.0f, -1.0), 0.0f);
        assertEquals(Float.MAX_VALUE, FastMath.nextAfter(Float.POSITIVE_INFINITY, -1.0), 0.0f);
    }

    @Test
    public void testSin() {
        assertEquals(0.0, FastMath.sin(0.0), 1e-15);
        assertEquals(1.0, FastMath.sin(Math.PI / 2), 1e-15);
        assertEquals(-1.0, FastMath.sin(-Math.PI / 2), 1e-15);
        assertTrue(Double.isNaN(FastMath.sin(Double.NaN)));
        assertTrue(Double.isNaN(FastMath.sin(Double.POSITIVE_INFINITY)));
        // Large argument to test Payne-Hanek reduction
        assertEquals(Math.sin(1e10), FastMath.sin(1e10), 1e-8);
        // Negative zero
        assertEquals(-0.0, FastMath.sin(-0.0), 0.0);
        assertEquals(0.0, FastMath.sin(0.0), 0.0);
    }

    @Test
    public void testCos() {
        assertEquals(1.0, FastMath.cos(0.0), 1e-15);
        assertEquals(0.0, FastMath.cos(Math.PI / 2), 1e-15);
        assertTrue(Double.isNaN(FastMath.cos(Double.NaN)));
        assertTrue(Double.isNaN(FastMath.cos(Double.POSITIVE_INFINITY)));
        assertEquals(Math.cos(1e10), FastMath.cos(1e10), 1e-8);
    }

    @Test
    public void testTan() {
        assertEquals(0.0, FastMath.tan(0.0), 1e-15);
        assertEquals(1.0, FastMath.tan(Math.PI / 4), 1e-14);
        assertEquals(-0.0, FastMath.tan(-0.0), 0.0);
        assertTrue(Double.isNaN(FastMath.tan(Double.NaN)));
        assertTrue(Double.isNaN(FastMath.tan(Double.POSITIVE_INFINITY)));
        double x = 1e10;
        assertEquals(Math.tan(x), FastMath.tan(x), 1e-6);
        x = 1.0;
        assertEquals(Math.tan(x), FastMath.tan(x), 1e-14);
        x = -1.0;
        assertEquals(Math.tan(x), FastMath.tan(x), 1e-14);
    }

    @Test
    public void testAtan() {
        assertEquals(0.0, FastMath.atan(0.0), 1e-15);
        assertEquals(Math.atan(1.0), FastMath.atan(1.0), 1e-14);
        assertEquals(Math.atan(-1.0), FastMath.atan(-1.0), 1e-14);
        assertEquals(Math.atan(1e16), FastMath.atan(1e16), 1e-14);
        assertEquals(Math.atan(0.5), FastMath.atan(0.5), 1e-14);
        assertEquals(Math.atan((-0.5)), FastMath.atan(-0.5), 1e-14);
        assertEquals(Math.atan(5.0), FastMath.atan(5.0), 1e-14);
        assertEquals(Math.atan((-5.0)), FastMath.atan(-5.0), 1e-14);
        assertTrue(Double.isNaN(FastMath.atan(Double.NaN)));
    }

    @Test
    public void testAtan2() {
        assertEquals(0.0, FastMath.atan2(0.0, 1.0), 0.0);
        assertEquals(Math.PI / 2, FastMath.atan2(1.0, 0.0), 1e-14);
        assertEquals(Math.PI, FastMath.atan2(0.0, -1.0), 1e-14);
        assertEquals(-Math.PI / 2, FastMath.atan2(-1.0, 0.0), 1e-14);
        assertEquals(Math.PI / 4, FastMath.atan2(1.0, 1.0), 1e-14);
        assertTrue(Double.isNaN(FastMath.atan2(Double.NaN, 0.0)));
        assertTrue(Double.isNaN(FastMath.atan2(0.0, Double.NaN)));
        assertEquals(Math.PI / 2, FastMath.atan2(Double.POSITIVE_INFINITY, 1.0), 1e-14);
        assertEquals(-Math.PI / 2, FastMath.atan2(Double.NEGATIVE_INFINITY, 1.0), 1e-14);
        assertEquals(0.0, FastMath.atan2(0.0, Double.POSITIVE_INFINITY), 0.0);
        assertEquals(Math.PI, FastMath.atan2(0.0, Double.NEGATIVE_INFINITY), 1e-14);
        assertEquals(0.0, FastMath.atan2(0.0, 0.0), 0.0);
        assertEquals(-0.0, FastMath.atan2(-0.0, 0.0), 0.0);
        assertEquals(Math.PI, FastMath.atan2(0.0, -0.0), 1e-14);
        assertEquals(-Math.PI, FastMath.atan2(-0.0, -0.0), 1e-14);
    }

    @Test
    public void testAsin() {
        assertEquals(0.0, FastMath.asin(0.0), 1e-15);
        assertEquals(Math.PI / 2, FastMath.asin(1.0), 1e-14);
        assertEquals(-Math.PI / 2, FastMath.asin(-1.0), 1e-14);
        assertTrue(Double.isNaN(FastMath.asin(2.0)));
        assertTrue(Double.isNaN(FastMath.asin(Double.NaN)));
        assertEquals(Math.asin(0.5), FastMath.asin(0.5), 1e-14);
        assertEquals(Math.asin((-0.5)), FastMath.asin(-0.5), 1e-14);
    }

    @Test
    public void testAcos() {
        assertEquals(Math.PI / 2, FastMath.acos(0.0), 1e-14);
        assertEquals(0.0, FastMath.acos(1.0), 1e-14);
        assertEquals(Math.PI, FastMath.acos(-1.0), 1e-14);
        assertTrue(Double.isNaN(FastMath.acos(2.0)));
        assertTrue(Double.isNaN(FastMath.acos(Double.NaN)));
        assertEquals(Math.acos(0.5), FastMath.acos(0.5), 1e-14);
        assertEquals(Math.acos((-0.5)), FastMath.acos(-0.5), 1e-14);
    }

    @Test
    public void testCbrt() {
        assertEquals(0.0, FastMath.cbrt(0.0), 0.0);
        assertEquals(2.0, FastMath.cbrt(8.0), 1e-14);
        assertEquals(-2.0, FastMath.cbrt(-8.0), 1e-14);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.cbrt(Double.POSITIVE_INFINITY), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.cbrt(Double.NEGATIVE_INFINITY), 0.0);
        assertTrue(Double.isNaN(FastMath.cbrt(Double.NaN)));
        // subnormal
        double sub = Double.longBitsToDouble(0x0000000000000001L);
        assertEquals(Math.cbrt(sub), FastMath.cbrt(sub), 1e-300);
    }

    @Test
    public void testToRadians() {
        assertEquals(0.0, FastMath.toRadians(0.0), 0.0);
        assertEquals(Math.toRadians(90.0), FastMath.toRadians(90.0), 1e-14);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.toRadians(Double.POSITIVE_INFINITY), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.toRadians(Double.NEGATIVE_INFINITY), 0.0);
        assertEquals(-0.0, FastMath.toRadians(-0.0), 0.0);
    }

    @Test
    public void testToDegrees() {
        assertEquals(0.0, FastMath.toDegrees(0.0), 0.0);
        assertEquals(Math.toDegrees(Math.PI), FastMath.toDegrees(Math.PI), 1e-14);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.toDegrees(Double.POSITIVE_INFINITY), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.toDegrees(Double.NEGATIVE_INFINITY), 0.0);
        assertEquals(-0.0, FastMath.toDegrees(-0.0), 0.0);
    }

    @Test
    public void testMain() {
        // Ensure main runs without exception (coverage of code that prints arrays)
        FastMath.main(new String[0]);
    }
}
