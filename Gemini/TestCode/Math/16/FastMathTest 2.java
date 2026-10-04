package org.apache.commons.math3.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.Assert;
import org.junit.Test;

public class FastMathTest {

    private static final double EPSILON = 1e-14;

    @Test
    public void testConstants() {
        Assert.assertEquals(Math.PI, FastMath.PI, 1e-15);
        Assert.assertEquals(Math.E, FastMath.E, 1e-15);
    }

    @Test
    public void testSqrt() {
        Assert.assertEquals(0.0, FastMath.sqrt(0.0), 0.0);
        Assert.assertEquals(2.0, FastMath.sqrt(4.0), EPSILON);
        Assert.assertTrue(Double.isNaN(FastMath.sqrt(-1.0)));
        Assert.assertTrue(Double.isNaN(FastMath.sqrt(Double.NaN)));
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.sqrt(Double.POSITIVE_INFINITY), 0.0);
    }

    @Test
    public void testCosh() {
        Assert.assertTrue(Double.isNaN(FastMath.cosh(Double.NaN)));
        Assert.assertEquals(1.0, FastMath.cosh(0.0), EPSILON);
        Assert.assertEquals(1.0, FastMath.cosh(-0.0), EPSILON);
        Assert.assertEquals(Math.cosh(1.5), FastMath.cosh(1.5), EPSILON);
        Assert.assertEquals(Math.cosh(-1.5), FastMath.cosh(-1.5), EPSILON);
        Assert.assertEquals(Math.cosh(25.0), FastMath.cosh(25.0), Math.cosh(25.0) * 1e-14);
        Assert.assertEquals(Math.cosh(-25.0), FastMath.cosh(-25.0), Math.cosh(-25.0) * 1e-14);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.cosh(1000.0), 0.0);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.cosh(-1000.0), 0.0);
    }

    @Test
    public void testSinh() {
        Assert.assertTrue(Double.isNaN(FastMath.sinh(Double.NaN)));
        Assert.assertEquals(0.0, FastMath.sinh(0.0), 0.0);
        Assert.assertEquals(-0.0, FastMath.sinh(-0.0), 0.0);
        Assert.assertEquals(Math.sinh(0.1), FastMath.sinh(0.1), EPSILON);
        Assert.assertEquals(Math.sinh(-0.1), FastMath.sinh(-0.1), EPSILON);
        Assert.assertEquals(Math.sinh(2.0), FastMath.sinh(2.0), EPSILON);
        Assert.assertEquals(Math.sinh(-2.0), FastMath.sinh(-2.0), EPSILON);
        Assert.assertEquals(Math.sinh(25.0), FastMath.sinh(25.0), Math.sinh(25.0) * 1e-14);
        Assert.assertEquals(Math.sinh(-25.0), FastMath.sinh(-25.0), Math.sinh(-25.0) * 1e-14);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.sinh(1000.0), 0.0);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, FastMath.sinh(-1000.0), 0.0);
    }

    @Test
    public void testTanh() {
        Assert.assertTrue(Double.isNaN(FastMath.tanh(Double.NaN)));
        Assert.assertEquals(0.0, FastMath.tanh(0.0), 0.0);
        Assert.assertEquals(-0.0, FastMath.tanh(-0.0), 0.0);
        Assert.assertEquals(1.0, FastMath.tanh(25.0), EPSILON);
        Assert.assertEquals(-1.0, FastMath.tanh(-25.0), EPSILON);
        Assert.assertEquals(Math.tanh(0.2), FastMath.tanh(0.2), EPSILON);
        Assert.assertEquals(Math.tanh(-0.2), FastMath.tanh(-0.2), EPSILON);
        Assert.assertEquals(Math.tanh(1.2), FastMath.tanh(1.2), EPSILON);
        Assert.assertEquals(Math.tanh(-1.2), FastMath.tanh(-1.2), EPSILON);
    }

    @Test
    public void testAcosh() {
        Assert.assertTrue(Double.isNaN(FastMath.acosh(0.5)));
        Assert.assertTrue(Double.isNaN(FastMath.acosh(-1.0)));
        Assert.assertEquals(0.0, FastMath.acosh(1.0), EPSILON);
        Assert.assertEquals(1.3169578969248166, FastMath.acosh(2.0), EPSILON);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.acosh(Double.POSITIVE_INFINITY), 0.0);
    }

    @Test
    public void testAsinh() {
        Assert.assertTrue(Double.isNaN(FastMath.asinh(Double.NaN)));
        Assert.assertEquals(0.0, FastMath.asinh(0.0), 0.0);
        Assert.assertEquals(-0.0, FastMath.asinh(-0.0), 0.0);

        Assert.assertEquals(0.001, FastMath.asinh(0.001), 1e-8);
        Assert.assertEquals(-0.001, FastMath.asinh(-0.001), 1e-8);
        Assert.assertEquals(0.01, FastMath.asinh(0.01), 1e-8);
        Assert.assertEquals(0.05, FastMath.asinh(0.05), 1e-8);
        Assert.assertEquals(0.12, FastMath.asinh(0.12), 1e-8);
        Assert.assertEquals(0.5, FastMath.asinh(0.5), 1e-8);
        Assert.assertEquals(2.0, FastMath.asinh(FastMath.sinh(2.0)), EPSILON);
        Assert.assertEquals(-2.0, FastMath.asinh(FastMath.sinh(-2.0)), EPSILON);
    }

    @Test
    public void testAtanh() {
        Assert.assertTrue(Double.isNaN(FastMath.atanh(Double.NaN)));
        Assert.assertTrue(Double.isNaN(FastMath.atanh(1.5)));
        Assert.assertTrue(Double.isNaN(FastMath.atanh(-1.5)));
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.atanh(1.0), 0.0);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, FastMath.atanh(-1.0), 0.0);
        Assert.assertEquals(0.0, FastMath.atanh(0.0), 0.0);
        Assert.assertEquals(-0.0, FastMath.atanh(-0.0), 0.0);

        Assert.assertEquals(0.001, FastMath.atanh(0.001), 1e-8);
        Assert.assertEquals(-0.001, FastMath.atanh(-0.001), 1e-8);
        Assert.assertEquals(0.01, FastMath.atanh(0.01), 1e-8);
        Assert.assertEquals(0.05, FastMath.atanh(0.05), 1e-8);
        Assert.assertEquals(0.1, FastMath.atanh(0.1), 1e-8);
        Assert.assertEquals(0.5, FastMath.atanh(0.5), 1e-8);
        Assert.assertEquals(-0.5, FastMath.atanh(-0.5), 1e-8);
    }

    @Test
    public void testSignumDouble() {
        Assert.assertTrue(Double.isNaN(FastMath.signum(Double.NaN)));
        Assert.assertEquals(1.0, FastMath.signum(100.5), 0.0);
        Assert.assertEquals(-1.0, FastMath.signum(-100.5), 0.0);
        Assert.assertEquals(0.0, FastMath.signum(0.0), 0.0);
        Assert.assertEquals(-0.0, FastMath.signum(-0.0), 0.0);
        Assert.assertEquals(1.0, FastMath.signum(Double.POSITIVE_INFINITY), 0.0);
        Assert.assertEquals(-1.0, FastMath.signum(Double.NEGATIVE_INFINITY), 0.0);
    }

    @Test
    public void testSignumFloat() {
        Assert.assertTrue(Float.isNaN(FastMath.signum(Float.NaN)));
        Assert.assertEquals(1.0f, FastMath.signum(100.5f), 0.0f);
        Assert.assertEquals(-1.0f, FastMath.signum(-100.5f), 0.0f);
        Assert.assertEquals(0.0f, FastMath.signum(0.0f), 0.0f);
        Assert.assertEquals(-0.0f, FastMath.signum(-0.0f), 0.0f);
        Assert.assertEquals(1.0f, FastMath.signum(Float.POSITIVE_INFINITY), 0.0f);
        Assert.assertEquals(-1.0f, FastMath.signum(Float.NEGATIVE_INFINITY), 0.0f);
    }

    @Test
    public void testNextUpDouble() {
        Assert.assertEquals(Double.MIN_VALUE, FastMath.nextUp(0.0), 0.0);
        Assert.assertEquals(0.0, FastMath.nextUp(-Double.MIN_VALUE), 0.0);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.nextUp(Double.MAX_VALUE), 0.0);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.nextUp(Double.POSITIVE_INFINITY), 0.0);
        Assert.assertEquals(-Double.MAX_VALUE, FastMath.nextUp(Double.NEGATIVE_INFINITY), 0.0);
        Assert.assertTrue(Double.isNaN(FastMath.nextUp(Double.NaN)));
    }

    @Test
    public void testNextUpFloat() {
        Assert.assertEquals(Float.MIN_VALUE, FastMath.nextUp(0.0f), 0.0f);
        Assert.assertEquals(0.0f, FastMath.nextUp(-Float.MIN_VALUE), 0.0f);
        Assert.assertEquals(Float.POSITIVE_INFINITY, FastMath.nextUp(Float.MAX_VALUE), 0.0f);
        Assert.assertEquals(Float.POSITIVE_INFINITY, FastMath.nextUp(Float.POSITIVE_INFINITY), 0.0f);
        Assert.assertEquals(-Float.MAX_VALUE, FastMath.nextUp(Float.NEGATIVE_INFINITY), 0.0f);
        Assert.assertTrue(Float.isNaN(FastMath.nextUp(Float.NaN)));
    }

    @Test
    public void testRandom() {
        double r = FastMath.random();
        Assert.assertTrue(r >= 0.0 && r < 1.0);
    }

    @Test
    public void testExp() {
        Assert.assertEquals(1.0, FastMath.exp(0.0), EPSILON);
        Assert.assertEquals(Math.E, FastMath.exp(1.0), EPSILON);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.exp(710.0), 0.0);
        Assert.assertEquals(0.0, FastMath.exp(-750.0), 0.0);
        Assert.assertTrue(FastMath.exp(-720.0) > 0.0);
        Assert.assertTrue(FastMath.exp(-709.5) > 0.0);
        Assert.assertEquals(Math.exp(-5.5), FastMath.exp(-5.5), EPSILON);
        Assert.assertEquals(Math.exp(5.5), FastMath.exp(5.5), EPSILON);
    }

    @Test
    public void testExpm1() {
        Assert.assertTrue(Double.isNaN(FastMath.expm1(Double.NaN)));
        Assert.assertEquals(0.0, FastMath.expm1(0.0), 0.0);
        Assert.assertEquals(-0.0, FastMath.expm1(-0.0), 0.0);
        Assert.assertEquals(Math.expm1(2.0), FastMath.expm1(2.0), EPSILON);
        Assert.assertEquals(Math.expm1(-2.0), FastMath.expm1(-2.0), EPSILON);
        Assert.assertEquals(Math.expm1(0.5), FastMath.expm1(0.5), EPSILON);
        Assert.assertEquals(Math.expm1(-0.5), FastMath.expm1(-0.5), EPSILON);
        Assert.assertEquals(Math.expm1(1e-5), FastMath.expm1(1e-5), 1e-15);
        Assert.assertEquals(Math.expm1(-1e-5), FastMath.expm1(-1e-5), 1e-15);
    }

    @Test
    public void testLog() {
        Assert.assertEquals(Double.NEGATIVE_INFINITY, FastMath.log(0.0), 0.0);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, FastMath.log(-0.0), 0.0);
        Assert.assertTrue(Double.isNaN(FastMath.log(-1.0)));
        Assert.assertTrue(Double.isNaN(FastMath.log(Double.NaN)));
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.log(Double.POSITIVE_INFINITY), 0.0);
        Assert.assertEquals(0.0, FastMath.log(1.0), EPSILON);
        Assert.assertEquals(1.0, FastMath.log(FastMath.E), EPSILON);
        Assert.assertEquals(Math.log(1.005), FastMath.log(1.005), EPSILON);
        Assert.assertEquals(Math.log(0.995), FastMath.log(0.995), EPSILON);
        Assert.assertEquals(Math.log(100.0), FastMath.log(100.0), EPSILON);
        Assert.assertEquals(Math.log(Double.MIN_NORMAL / 4.0), FastMath.log(Double.MIN_NORMAL / 4.0), 1e-10);
    }

    @Test
    public void testLog1p() {
        Assert.assertEquals(Double.NEGATIVE_INFINITY, FastMath.log1p(-1.0), 0.0);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.log1p(Double.POSITIVE_INFINITY), 0.0);
        Assert.assertTrue(Double.isNaN(FastMath.log1p(-2.0)));
        Assert.assertEquals(0.0, FastMath.log1p(0.0), 0.0);
        Assert.assertEquals(Math.log1p(1e-8), FastMath.log1p(1e-8), 1e-15);
        Assert.assertEquals(Math.log1p(-1e-8), FastMath.log1p(-1e-8), 1e-15);
        Assert.assertEquals(Math.log1p(0.5), FastMath.log1p(0.5), EPSILON);
    }

    @Test
    public void testLog10() {
        Assert.assertEquals(Double.NEGATIVE_INFINITY, FastMath.log10(0.0), 0.0);
        Assert.assertEquals(1.0, FastMath.log10(10.0), EPSILON);
        Assert.assertEquals(2.0, FastMath.log10(100.0), EPSILON);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.log10(Double.POSITIVE_INFINITY), 0.0);
        Assert.assertTrue(Double.isNaN(FastMath.log10(-10.0)));
    }

    @Test
    public void testLogBase() {
        Assert.assertEquals(3.0, FastMath.log(2.0, 8.0), EPSILON);
        Assert.assertTrue(Double.isNaN(FastMath.log(-2.0, 8.0)));
        Assert.assertTrue(Double.isNaN(FastMath.log(2.0, -8.0)));
        Assert.assertEquals(0.0, FastMath.log(0.0, 5.0), 0.0);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, FastMath.log(5.0, 0.0), 0.0);
        Assert.assertTrue(Double.isNaN(FastMath.log(0.0, 0.0)));
    }

    @Test
    public void testPowDoubleDouble() {
        Assert.assertEquals(1.0, FastMath.pow(123.456, 0.0), 0.0);
        Assert.assertEquals(1.0, FastMath.pow(Double.NaN, 0.0), 0.0);
        Assert.assertTrue(Double.isNaN(FastMath.pow(Double.NaN, 2.0)));

        Assert.assertEquals(Double.NEGATIVE_INFINITY, FastMath.pow(-0.0, -3.0), 0.0);
        Assert.assertEquals(-0.0, FastMath.pow(-0.0, 3.0), 0.0);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(-0.0, -2.0), 0.0);
        Assert.assertEquals(0.0, FastMath.pow(-0.0, 2.0), 0.0);

        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(0.0, -2.0), 0.0);
        Assert.assertEquals(0.0, FastMath.pow(0.0, 2.0), 0.0);

        Assert.assertTrue(Double.isNaN(FastMath.pow(Double.POSITIVE_INFINITY, Double.NaN)));
        Assert.assertEquals(0.0, FastMath.pow(Double.POSITIVE_INFINITY, -2.0), 0.0);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(Double.POSITIVE_INFINITY, 2.0), 0.0);

        Assert.assertTrue(Double.isNaN(FastMath.pow(1.0, Double.POSITIVE_INFINITY)));
        Assert.assertTrue(Double.isNaN(FastMath.pow(-1.0, Double.POSITIVE_INFINITY)));
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(2.0, Double.POSITIVE_INFINITY), 0.0);
        Assert.assertEquals(0.0, FastMath.pow(0.5, Double.POSITIVE_INFINITY), 0.0);

        Assert.assertTrue(Double.isNaN(FastMath.pow(Double.NEGATIVE_INFINITY, Double.NaN)));
        Assert.assertEquals(-0.0, FastMath.pow(Double.NEGATIVE_INFINITY, -3.0), 0.0);
        Assert.assertEquals(0.0, FastMath.pow(Double.NEGATIVE_INFINITY, -2.0), 0.0);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, FastMath.pow(Double.NEGATIVE_INFINITY, 3.0), 0.0);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(Double.NEGATIVE_INFINITY, 2.0), 0.0);

        Assert.assertTrue(Double.isNaN(FastMath.pow(1.0, Double.NEGATIVE_INFINITY)));
        Assert.assertTrue(Double.isNaN(FastMath.pow(-1.0, Double.NEGATIVE_INFINITY)));
        Assert.assertEquals(0.0, FastMath.pow(2.0, Double.NEGATIVE_INFINITY), 0.0);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(0.5, Double.NEGATIVE_INFINITY), 0.0);

        Assert.assertEquals(16.0, FastMath.pow(-2.0, 4.0), EPSILON);
        Assert.assertEquals(-8.0, FastMath.pow(-2.0, 3.0), EPSILON);
        Assert.assertEquals(FastMath.pow(2.0, 5000000000000000.0), FastMath.pow(-2.0, 5000000000000000.0), EPSILON);
        Assert.assertTrue(Double.isNaN(FastMath.pow(-2.0, 2.5)));

        Assert.assertEquals(8.0, FastMath.pow(2.0, 3.0), EPSILON);
        Assert.assertEquals(Math.pow(1.5, 300.0), FastMath.pow(1.5, 300.0), Math.pow(1.5, 300.0) * 1e-12);
        Assert.assertEquals(Math.pow(1.5, -300.0), FastMath.pow(1.5, -300.0), Math.pow(1.5, -300.0) * 1e-12);
    }

    @Test
    public void testPowDoubleInt() {
        Assert.assertEquals(1.0, FastMath.pow(5.5, 0), 0.0);
        Assert.assertEquals(8.0, FastMath.pow(2.0, 3), EPSILON);
        Assert.assertEquals(0.125, FastMath.pow(2.0, -3), EPSILON);
        Assert.assertEquals(1024.0, FastMath.pow(2.0, 10), EPSILON);
        Assert.assertEquals(1.0 / 1024.0, FastMath.pow(2.0, -10), EPSILON);
    }

    @Test
    public void testSin() {
        Assert.assertEquals(0.0, FastMath.sin(0.0), 0.0);
        Assert.assertEquals(-0.0, FastMath.sin(-0.0), 0.0);
        Assert.assertTrue(Double.isNaN(FastMath.sin(Double.NaN)));
        Assert.assertTrue(Double.isNaN(FastMath.sin(Double.POSITIVE_INFINITY)));
        Assert.assertTrue(Double.isNaN(FastMath.sin(Double.NEGATIVE_INFINITY)));

        Assert.assertEquals(1.0, FastMath.sin(FastMath.PI / 2.0), EPSILON);
        Assert.assertEquals(0.0, FastMath.sin(FastMath.PI), EPSILON);
        Assert.assertEquals(-1.0, FastMath.sin(3.0 * FastMath.PI / 2.0), EPSILON);
        Assert.assertEquals(0.0, FastMath.sin(2.0 * FastMath.PI), EPSILON);
        Assert.assertEquals(-1.0, FastMath.sin(-FastMath.PI / 2.0), EPSILON);

        Assert.assertEquals(Math.sin(4000000.0), FastMath.sin(4000000.0), 1e-9);
        Assert.assertEquals(Math.sin(-4000000.0), FastMath.sin(-4000000.0), 1e-9);
    }

    @Test
    public void testCos() {
        Assert.assertEquals(1.0, FastMath.cos(0.0), EPSILON);
        Assert.assertEquals(1.0, FastMath.cos(-0.0), EPSILON);
        Assert.assertTrue(Double.isNaN(FastMath.cos(Double.NaN)));
        Assert.assertTrue(Double.isNaN(FastMath.cos(Double.POSITIVE_INFINITY)));

        Assert.assertEquals(0.0, FastMath.cos(FastMath.PI / 2.0), EPSILON);
        Assert.assertEquals(-1.0, FastMath.cos(FastMath.PI), EPSILON);
        Assert.assertEquals(0.0, FastMath.cos(3.0 * FastMath.PI / 2.0), EPSILON);
        Assert.assertEquals(1.0, FastMath.cos(2.0 * FastMath.PI), EPSILON);
        Assert.assertEquals(Math.cos(-2.5), FastMath.cos(-2.5), EPSILON);

        Assert.assertEquals(Math.cos(4000000.0), FastMath.cos(4000000.0), 1e-9);
    }

    @Test
    public void testTan() {
        Assert.assertEquals(0.0, FastMath.tan(0.0), 0.0);
        Assert.assertEquals(-0.0, FastMath.tan(-0.0), 0.0);
        Assert.assertTrue(Double.isNaN(FastMath.tan(Double.NaN)));
        Assert.assertTrue(Double.isNaN(FastMath.tan(Double.POSITIVE_INFINITY)));

        Assert.assertEquals(Math.tan(0.5), FastMath.tan(0.5), EPSILON);
        Assert.assertEquals(Math.tan(-0.5), FastMath.tan(-0.5), EPSILON);
        Assert.assertEquals(Math.tan(1.55), FastMath.tan(1.55), 1e-10);
        Assert.assertEquals(Math.tan(-1.55), FastMath.tan(-1.55), 1e-10);
        Assert.assertEquals(Math.tan(2.5), FastMath.tan(2.5), EPSILON);
        Assert.assertEquals(Math.tan(4000000.0), FastMath.tan(4000000.0), 1e-9);
    }

    @Test
    public void testAtan() {
        Assert.assertEquals(0.0, FastMath.atan(0.0), 0.0);
        Assert.assertEquals(-0.0, FastMath.atan(-0.0), 0.0);
        Assert.assertEquals(FastMath.PI / 4.0, FastMath.atan(1.0), EPSILON);
        Assert.assertEquals(-FastMath.PI / 4.0, FastMath.atan(-1.0), EPSILON);
        Assert.assertEquals(Math.atan(0.5), FastMath.atan(0.5), EPSILON);
        Assert.assertEquals(Math.atan(2.5), FastMath.atan(2.5), EPSILON);
        Assert.assertEquals(FastMath.PI / 2.0, FastMath.atan(2e16), EPSILON);
        Assert.assertEquals(-FastMath.PI / 2.0, FastMath.atan(-2e16), EPSILON);
    }

    @Test
    public void testAtan2() {
        Assert.assertTrue(Double.isNaN(FastMath.atan2(Double.NaN, 1.0)));
        Assert.assertTrue(Double.isNaN(FastMath.atan2(1.0, Double.NaN)));

        Assert.assertEquals(0.0, FastMath.atan2(0.0, 1.0), 0.0);
        Assert.assertEquals(-0.0, FastMath.atan2(-0.0, 1.0), 0.0);
        Assert.assertEquals(FastMath.PI, FastMath.atan2(0.0, -1.0), EPSILON);
        Assert.assertEquals(-FastMath.PI, FastMath.atan2(-0.0, -1.0), EPSILON);

        Assert.assertEquals(0.0, FastMath.atan2(0.0, Double.POSITIVE_INFINITY), 0.0);
        Assert.assertEquals(FastMath.PI, FastMath.atan2(0.0, Double.NEGATIVE_INFINITY), EPSILON);

        Assert.assertEquals(FastMath.PI / 4.0, FastMath.atan2(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY), EPSILON);
        Assert.assertEquals(3.0 * FastMath.PI / 4.0, FastMath.atan2(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY), EPSILON);
        Assert.assertEquals(FastMath.PI / 2.0, FastMath.atan2(Double.POSITIVE_INFINITY, 1.0), EPSILON);

        Assert.assertEquals(-FastMath.PI / 4.0, FastMath.atan2(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY), EPSILON);
        Assert.assertEquals(-3.0 * FastMath.PI / 4.0, FastMath.atan2(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY), EPSILON);
        Assert.assertEquals(-FastMath.PI / 2.0, FastMath.atan2(Double.NEGATIVE_INFINITY, 1.0), EPSILON);

        Assert.assertEquals(0.0, FastMath.atan2(1.0, Double.POSITIVE_INFINITY), 0.0);
        Assert.assertEquals(-0.0, FastMath.atan2(-1.0, Double.POSITIVE_INFINITY), 0.0);
        Assert.assertEquals(FastMath.PI, FastMath.atan2(1.0, Double.NEGATIVE_INFINITY), EPSILON);
        Assert.assertEquals(-FastMath.PI, FastMath.atan2(-1.0, Double.NEGATIVE_INFINITY), EPSILON);

        Assert.assertEquals(FastMath.PI / 2.0, FastMath.atan2(1.0, 0.0), EPSILON);
        Assert.assertEquals(-FastMath.PI / 2.0, FastMath.atan2(-1.0, 0.0), EPSILON);

        Assert.assertEquals(Math.atan2(2.0, 3.0), FastMath.atan2(2.0, 3.0), EPSILON);
        Assert.assertEquals(Math.atan2(-2.0, 3.0), FastMath.atan2(-2.0, 3.0), EPSILON);
        Assert.assertEquals(Math.atan2(2.0, -3.0), FastMath.atan2(2.0, -3.0), EPSILON);
        Assert.assertEquals(Math.atan2(-2.0, -3.0), FastMath.atan2(-2.0, -3.0), EPSILON);
        Assert.assertEquals(Math.atan2(1e300, 1e-300), FastMath.atan2(1e300, 1e-300), EPSILON);
    }

    @Test
    public void testAsin() {
        Assert.assertTrue(Double.isNaN(FastMath.asin(Double.NaN)));
        Assert.assertTrue(Double.isNaN(FastMath.asin(1.5)));
        Assert.assertTrue(Double.isNaN(FastMath.asin(-1.5)));
        Assert.assertEquals(FastMath.PI / 2.0, FastMath.asin(1.0), EPSILON);
        Assert.assertEquals(-FastMath.PI / 2.0, FastMath.asin(-1.0), EPSILON);
        Assert.assertEquals(0.0, FastMath.asin(0.0), 0.0);
        Assert.assertEquals(-0.0, FastMath.asin(-0.0), 0.0);
        Assert.assertEquals(Math.asin(0.5), FastMath.asin(0.5), EPSILON);
        Assert.assertEquals(Math.asin(-0.5), FastMath.asin(-0.5), EPSILON);
    }

    @Test
    public void testAcos() {
        Assert.assertTrue(Double.isNaN(FastMath.acos(Double.NaN)));
        Assert.assertTrue(Double.isNaN(FastMath.acos(1.5)));
        Assert.assertTrue(Double.isNaN(FastMath.acos(-1.5)));
        Assert.assertEquals(FastMath.PI, FastMath.acos(-1.0), EPSILON);
        Assert.assertEquals(0.0, FastMath.acos(1.0), 0.0);
        Assert.assertEquals(FastMath.PI / 2.0, FastMath.acos(0.0), EPSILON);
        Assert.assertEquals(Math.acos(0.5), FastMath.acos(0.5), EPSILON);
        Assert.assertEquals(Math.acos(-0.5), FastMath.acos(-0.5), EPSILON);
    }

    @Test
    public void testCbrt() {
        Assert.assertEquals(0.0, FastMath.cbrt(0.0), 0.0);
        Assert.assertEquals(-0.0, FastMath.cbrt(-0.0), 0.0);
        Assert.assertTrue(Double.isNaN(FastMath.cbrt(Double.NaN)));
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.cbrt(Double.POSITIVE_INFINITY), 0.0);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, FastMath.cbrt(Double.NEGATIVE_INFINITY), 0.0);

        Assert.assertEquals(2.0, FastMath.cbrt(8.0), EPSILON);
        Assert.assertEquals(-2.0, FastMath.cbrt(-8.0), EPSILON);
        Assert.assertEquals(3.0, FastMath.cbrt(27.0), EPSILON);
        Assert.assertEquals(Math.cbrt(Double.MIN_NORMAL / 8.0), FastMath.cbrt(Double.MIN_NORMAL / 8.0), 1e-15);
        Assert.assertEquals(Math.cbrt(123.456), FastMath.cbrt(123.456), EPSILON);
    }

    @Test
    public void testDegreesRadians() {
        Assert.assertEquals(0.0, FastMath.toRadians(0.0), 0.0);
        Assert.assertEquals(-0.0, FastMath.toRadians(-0.0), 0.0);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.toRadians(Double.POSITIVE_INFINITY), 0.0);
        Assert.assertEquals(FastMath.PI, FastMath.toRadians(180.0), EPSILON);

        Assert.assertEquals(0.0, FastMath.toDegrees(0.0), 0.0);
        Assert.assertEquals(-0.0, FastMath.toDegrees(-0.0), 0.0);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.toDegrees(Double.POSITIVE_INFINITY), 0.0);
        Assert.assertEquals(180.0, FastMath.toDegrees(FastMath.PI), EPSILON);
    }

    @Test
    public void testAbs() {
        Assert.assertEquals(10, FastMath.abs(10));
        Assert.assertEquals(10, FastMath.abs(-10));
        Assert.assertEquals(10L, FastMath.abs(10L));
        Assert.assertEquals(10L, FastMath.abs(-10L));

        Assert.assertEquals(10.5f, FastMath.abs(10.5f), 0.0f);
        Assert.assertEquals(10.5f, FastMath.abs(-10.5f), 0.0f);
        Assert.assertEquals(0.0f, FastMath.abs(-0.0f), 0.0f);

        Assert.assertEquals(10.5, FastMath.abs(10.5), 0.0);
        Assert.assertEquals(10.5, FastMath.abs(-10.5), 0.0);
        Assert.assertEquals(0.0, FastMath.abs(-0.0), 0.0);
    }

    @Test
    public void testUlp() {
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.ulp(Double.POSITIVE_INFINITY), 0.0);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.ulp(Double.NEGATIVE_INFINITY), 0.0);
        Assert.assertEquals(Math.ulp(1.0), FastMath.ulp(1.0), 0.0);
        Assert.assertEquals(Math.ulp(123.456), FastMath.ulp(123.456), 0.0);

        Assert.assertEquals(Float.POSITIVE_INFINITY, FastMath.ulp(Float.POSITIVE_INFINITY), 0.0f);
        Assert.assertEquals(Float.POSITIVE_INFINITY, FastMath.ulp(Float.NEGATIVE_INFINITY), 0.0f);
        Assert.assertEquals(Math.ulp(1.0f), FastMath.ulp(1.0f), 0.0f);
        Assert.assertEquals(Math.ulp(123.456f), FastMath.ulp(123.456f), 0.0f);
    }

    @Test
    public void testScalbDouble() {
        Assert.assertEquals(8.0, FastMath.scalb(1.0, 3), 0.0);
        Assert.assertEquals(0.125, FastMath.scalb(1.0, -3), 0.0);
        Assert.assertTrue(Double.isNaN(FastMath.scalb(Double.NaN, 2)));
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.scalb(Double.POSITIVE_INFINITY, 2), 0.0);
        Assert.assertEquals(0.0, FastMath.scalb(0.0, 2), 0.0);

        Assert.assertEquals(0.0, FastMath.scalb(1.0, -2100), 0.0);
        Assert.assertEquals(-0.0, FastMath.scalb(-1.0, -2100), 0.0);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.scalb(1.0, 2100), 0.0);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, FastMath.scalb(-1.0, 2100), 0.0);

        Assert.assertEquals(Double.longBitsToDouble(1L), FastMath.scalb(1.0, -1074), 0.0);
        Assert.assertEquals(0.0, FastMath.scalb(1.0, -1075), 0.0);

        double subnormal = Double.longBitsToDouble(1L);
        Assert.assertEquals(1.0, FastMath.scalb(subnormal, 1074), 0.0);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.scalb(subnormal, 3000), 0.0);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.scalb(1.0, 1024), 0.0);
    }

    @Test
    public void testScalbFloat() {
        Assert.assertEquals(8.0f, FastMath.scalb(1.0f, 3), 0.0f);
        Assert.assertEquals(0.125f, FastMath.scalb(1.0f, -3), 0.0f);
        Assert.assertTrue(Float.isNaN(FastMath.scalb(Float.NaN, 2)));
        Assert.assertEquals(Float.POSITIVE_INFINITY, FastMath.scalb(Float.POSITIVE_INFINITY, 2), 0.0f);
        Assert.assertEquals(0.0f, FastMath.scalb(0.0f, 2), 0.0f);

        Assert.assertEquals(0.0f, FastMath.scalb(1.0f, -300), 0.0f);
        Assert.assertEquals(-0.0f, FastMath.scalb(-1.0f, -300), 0.0f);
        Assert.assertEquals(Float.POSITIVE_INFINITY, FastMath.scalb(1.0f, 300), 0.0f);
        Assert.assertEquals(Float.NEGATIVE_INFINITY, FastMath.scalb(-1.0f, 300), 0.0f);

        Assert.assertEquals(Float.intBitsToFloat(1), FastMath.scalb(1.0f, -149), 0.0f);
        Assert.assertEquals(0.0f, FastMath.scalb(1.0f, -150), 0.0f);

        float subnormal = Float.intBitsToFloat(1);
        Assert.assertEquals(1.0f, FastMath.scalb(subnormal, 149), 0.0f);
        Assert.assertEquals(Float.POSITIVE_INFINITY, FastMath.scalb(subnormal, 300), 0.0f);
        Assert.assertEquals(Float.POSITIVE_INFINITY, FastMath.scalb(1.0f, 128), 0.0f);
    }

    @Test
    public void testNextAfterDouble() {
        Assert.assertTrue(Double.isNaN(FastMath.nextAfter(Double.NaN, 1.0)));
        Assert.assertTrue(Double.isNaN(FastMath.nextAfter(1.0, Double.NaN)));
        Assert.assertEquals(1.0, FastMath.nextAfter(1.0, 1.0), 0.0);
        Assert.assertEquals(Double.MAX_VALUE, FastMath.nextAfter(Double.POSITIVE_INFINITY, 0.0), 0.0);
        Assert.assertEquals(-Double.MAX_VALUE, FastMath.nextAfter(Double.NEGATIVE_INFINITY, 0.0), 0.0);
        Assert.assertEquals(Double.MIN_VALUE, FastMath.nextAfter(0.0, 1.0), 0.0);
        Assert.assertEquals(-Double.MIN_VALUE, FastMath.nextAfter(0.0, -1.0), 0.0);
        Assert.assertTrue(FastMath.nextAfter(1.0, 2.0) > 1.0);
        Assert.assertTrue(FastMath.nextAfter(1.0, 0.0) < 1.0);
        Assert.assertTrue(FastMath.nextAfter(-1.0, -2.0) < -1.0);
        Assert.assertTrue(FastMath.nextAfter(-1.0, 0.0) > -1.0);
    }

    @Test
    public void testNextAfterFloat() {
        Assert.assertTrue(Float.isNaN(FastMath.nextAfter(Float.NaN, 1.0)));
        Assert.assertTrue(Float.isNaN(FastMath.nextAfter(1.0f, Double.NaN)));
        Assert.assertEquals(1.0f, FastMath.nextAfter(1.0f, 1.0), 0.0f);
        Assert.assertEquals(Float.MAX_VALUE, FastMath.nextAfter(Float.POSITIVE_INFINITY, 0.0), 0.0f);
        Assert.assertEquals(-Float.MAX_VALUE, FastMath.nextAfter(Float.NEGATIVE_INFINITY, 0.0), 0.0f);
        Assert.assertEquals(Float.MIN_VALUE, FastMath.nextAfter(0.0f, 1.0), 0.0f);
        Assert.assertEquals(-Float.MIN_VALUE, FastMath.nextAfter(0.0f, -1.0), 0.0f);
        Assert.assertTrue(FastMath.nextAfter(1.0f, 2.0) > 1.0f);
        Assert.assertTrue(FastMath.nextAfter(1.0f, 0.0) < 1.0f);
        Assert.assertTrue(FastMath.nextAfter(-1.0f, -2.0) < -1.0f);
        Assert.assertTrue(FastMath.nextAfter(-1.0f, 0.0) > -1.0f);
    }

    @Test
    public void testFloorCeilRintRound() {
        Assert.assertTrue(Double.isNaN(FastMath.floor(Double.NaN)));
        Assert.assertEquals(1e16, FastMath.floor(1e16), 0.0);
        Assert.assertEquals(2.0, FastMath.floor(2.8), 0.0);
        Assert.assertEquals(-3.0, FastMath.floor(-2.8), 0.0);
        Assert.assertEquals(0.0, FastMath.floor(0.5), 0.0);

        Assert.assertTrue(Double.isNaN(FastMath.ceil(Double.NaN)));
        Assert.assertEquals(3.0, FastMath.ceil(2.3), 0.0);
        Assert.assertEquals(-2.0, FastMath.ceil(-2.3), 0.0);
        Assert.assertEquals(0.0, FastMath.ceil(-0.5), 0.0);

        Assert.assertEquals(2.0, FastMath.rint(2.3), 0.0);
        Assert.assertEquals(3.0, FastMath.rint(2.8), 0.0);
        Assert.assertEquals(2.0, FastMath.rint(2.5), 0.0);
        Assert.assertEquals(4.0, FastMath.rint(3.5), 0.0);
        Assert.assertEquals(-0.0, FastMath.rint(-0.2), 0.0);

        Assert.assertEquals(3L, FastMath.round(2.6));
        Assert.assertEquals(2L, FastMath.round(2.4));
        Assert.assertEquals(-2L, FastMath.round(-2.4));

        Assert.assertEquals(3, FastMath.round(2.6f));
        Assert.assertEquals(2, FastMath.round(2.4f));
    }

    @Test
    public void testMinMax() {
        Assert.assertEquals(1, FastMath.min(1, 2));
        Assert.assertEquals(1, FastMath.min(2, 1));
        Assert.assertEquals(1L, FastMath.min(1L, 2L));
        Assert.assertEquals(1L, FastMath.min(2L, 1L));

        Assert.assertEquals(1.0f, FastMath.min(1.0f, 2.0f), 0.0f);
        Assert.assertEquals(1.0f, FastMath.min(2.0f, 1.0f), 0.0f);
        Assert.assertTrue(Float.isNaN(FastMath.min(Float.NaN, 1.0f)));
        Assert.assertTrue(Float.isNaN(FastMath.min(1.0f, Float.NaN)));
        Assert.assertEquals(-0.0f, FastMath.min(0.0f, -0.0f), 0.0f);
        Assert.assertEquals(-0.0f, FastMath.min(-0.0f, 0.0f), 0.0f);

        Assert.assertEquals(1.0, FastMath.min(1.0, 2.0), 0.0);
        Assert.assertEquals(1.0, FastMath.min(2.0, 1.0), 0.0);
        Assert.assertTrue(Double.isNaN(FastMath.min(Double.NaN, 1.0)));
        Assert.assertTrue(Double.isNaN(FastMath.min(1.0, Double.NaN)));
        Assert.assertEquals(-0.0, FastMath.min(0.0, -0.0), 0.0);
        Assert.assertEquals(-0.0, FastMath.min(-0.0, 0.0), 0.0);

        Assert.assertEquals(2, FastMath.max(1, 2));
        Assert.assertEquals(2, FastMath.max(2, 1));
        Assert.assertEquals(2L, FastMath.max(1L, 2L));
        Assert.assertEquals(2L, FastMath.max(2L, 1L));

        Assert.assertEquals(2.0f, FastMath.max(1.0f, 2.0f), 0.0f);
        Assert.assertEquals(2.0f, FastMath.max(2.0f, 1.0f), 0.0f);
        Assert.assertTrue(Float.isNaN(FastMath.max(Float.NaN, 1.0f)));
        Assert.assertTrue(Float.isNaN(FastMath.max(1.0f, Float.NaN)));
        Assert.assertEquals(0.0f, FastMath.max(0.0f, -0.0f), 0.0f);
        Assert.assertEquals(0.0f, FastMath.max(-0.0f, 0.0f), 0.0f);

        Assert.assertEquals(2.0, FastMath.max(1.0, 2.0), 0.0);
        Assert.assertEquals(2.0, FastMath.max(2.0, 1.0), 0.0);
        Assert.assertTrue(Double.isNaN(FastMath.max(Double.NaN, 1.0)));
        Assert.assertTrue(Double.isNaN(FastMath.max(1.0, Double.NaN)));
        Assert.assertEquals(0.0, FastMath.max(0.0, -0.0), 0.0);
        Assert.assertEquals(0.0, FastMath.max(-0.0, 0.0), 0.0);
    }

    @Test
    public void testHypot() {
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.hypot(Double.POSITIVE_INFINITY, 1.0), 0.0);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.hypot(1.0, Double.NEGATIVE_INFINITY), 0.0);
        Assert.assertTrue(Double.isNaN(FastMath.hypot(Double.NaN, 1.0)));
        Assert.assertTrue(Double.isNaN(FastMath.hypot(1.0, Double.NaN)));

        Assert.assertEquals(5.0, FastMath.hypot(3.0, 4.0), EPSILON);
        Assert.assertEquals(5.0, FastMath.hypot(-3.0, -4.0), EPSILON);
        Assert.assertEquals(1e10, FastMath.hypot(1e10, 1.0), EPSILON);
        Assert.assertEquals(1e10, FastMath.hypot(1.0, 1e10), EPSILON);
        Assert.assertEquals(5e200, FastMath.hypot(3e200, 4e200), 5e200 * 1e-15);
    }

    @Test
    public void testIEEEremainder() {
        Assert.assertEquals(StrictMath.IEEEremainder(5.0, 3.0), FastMath.IEEEremainder(5.0, 3.0), EPSILON);
        Assert.assertEquals(StrictMath.IEEEremainder(7.0, 2.5), FastMath.IEEEremainder(7.0, 2.5), EPSILON);
    }

    @Test
    public void testCopySign() {
        Assert.assertEquals(2.0, FastMath.copySign(2.0, 1.0), 0.0);
        Assert.assertEquals(-2.0, FastMath.copySign(2.0, -1.0), 0.0);
        Assert.assertEquals(2.0, FastMath.copySign(-2.0, 1.0), 0.0);
        Assert.assertEquals(-2.0, FastMath.copySign(-2.0, -1.0), 0.0);
        Assert.assertEquals(2.0, FastMath.copySign(-2.0, Double.NaN), 0.0);

        Assert.assertEquals(2.0f, FastMath.copySign(2.0f, 1.0f), 0.0f);
        Assert.assertEquals(-2.0f, FastMath.copySign(2.0f, -1.0f), 0.0f);
        Assert.assertEquals(2.0f, FastMath.copySign(-2.0f, 1.0f), 0.0f);
        Assert.assertEquals(-2.0f, FastMath.copySign(-2.0f, -1.0f), 0.0f);
        Assert.assertEquals(2.0f, FastMath.copySign(-2.0f, Float.NaN), 0.0f);
    }

    @Test
    public void testGetExponent() {
        Assert.assertEquals(0, FastMath.getExponent(1.0));
        Assert.assertEquals(1, FastMath.getExponent(2.0));
        Assert.assertEquals(-1, FastMath.getExponent(0.5));
        Assert.assertEquals(1023, FastMath.getExponent(Double.MAX_VALUE));

        Assert.assertEquals(0, FastMath.getExponent(1.0f));
        Assert.assertEquals(1, FastMath.getExponent(2.0f));
        Assert.assertEquals(-1, FastMath.getExponent(0.5f));
        Assert.assertEquals(127, FastMath.getExponent(Float.MAX_VALUE));
    }

    @Test
    public void testMain() {
        PrintStream originalOut = System.out;
        try {
            System.setOut(new PrintStream(new ByteArrayOutputStream()));
            FastMath.main(new String[0]);
        } finally {
            System.setOut(originalOut);
        }
    }
}
