package org.apache.commons.math3.util;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class FastMathTest {

    private static final double EPSILON = 1e-12;

    @Test
    public void testPrivateConstructor() throws Exception {
        Constructor<FastMath> constructor = FastMath.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        FastMath instance = constructor.newInstance();
        Assert.assertNotNull(instance);
    }

    @Test
    public void testConstants() {
        Assert.assertEquals(Math.PI, FastMath.PI, 1e-15);
        Assert.assertEquals(Math.E, FastMath.E, 1e-15);
    }

    @Test
    public void testSqrt() {
        Assert.assertEquals(2.0, FastMath.sqrt(4.0), EPSILON);
        Assert.assertEquals(0.0, FastMath.sqrt(0.0), EPSILON);
        Assert.assertTrue(Double.isNaN(FastMath.sqrt(-1.0)));
        Assert.assertTrue(Double.isNaN(FastMath.sqrt(Double.NaN)));
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.sqrt(Double.POSITIVE_INFINITY), EPSILON);
    }

    @Test
    public void testCosh() {
        Assert.assertTrue(Double.isNaN(FastMath.cosh(Double.NaN)));
        Assert.assertEquals(1.0, FastMath.cosh(0.0), EPSILON);
        Assert.assertEquals(FastMath.cosh(1.5), FastMath.cosh(-1.5), EPSILON);
        Assert.assertEquals(StrictMath.cosh(2.0), FastMath.cosh(2.0), 1e-10);
        Assert.assertEquals(StrictMath.cosh(25.0), FastMath.cosh(25.0), 1e-5);
        Assert.assertEquals(StrictMath.cosh(-25.0), FastMath.cosh(-25.0), 1e-5);
        Assert.assertTrue(Double.isInfinite(FastMath.cosh(800.0)));
        Assert.assertTrue(Double.isInfinite(FastMath.cosh(-800.0)));
    }

    @Test
    public void testSinh() {
        Assert.assertTrue(Double.isNaN(FastMath.sinh(Double.NaN)));
        Assert.assertEquals(0.0, FastMath.sinh(0.0), 0.0);
        Assert.assertEquals(-0.0, FastMath.sinh(-0.0), 0.0);
        Assert.assertEquals(StrictMath.sinh(0.1), FastMath.sinh(0.1), EPSILON);
        Assert.assertEquals(StrictMath.sinh(-0.1), FastMath.sinh(-0.1), EPSILON);
        Assert.assertEquals(StrictMath.sinh(2.0), FastMath.sinh(2.0), 1e-10);
        Assert.assertEquals(StrictMath.sinh(-2.0), FastMath.sinh(-2.0), 1e-10);
        Assert.assertEquals(StrictMath.sinh(25.0), FastMath.sinh(25.0), 1e-5);
        Assert.assertEquals(StrictMath.sinh(-25.0), FastMath.sinh(-25.0), 1e-5);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.sinh(800.0), EPSILON);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, FastMath.sinh(-800.0), EPSILON);
    }

    @Test
    public void testTanh() {
        Assert.assertTrue(Double.isNaN(FastMath.tanh(Double.NaN)));
        Assert.assertEquals(0.0, FastMath.tanh(0.0), 0.0);
        Assert.assertEquals(-0.0, FastMath.tanh(-0.0), 0.0);
        Assert.assertEquals(1.0, FastMath.tanh(30.0), EPSILON);
        Assert.assertEquals(-1.0, FastMath.tanh(-30.0), EPSILON);
        Assert.assertEquals(StrictMath.tanh(0.2), FastMath.tanh(0.2), EPSILON);
        Assert.assertEquals(StrictMath.tanh(-0.2), FastMath.tanh(-0.2), EPSILON);
        Assert.assertEquals(StrictMath.tanh(1.5), FastMath.tanh(1.5), EPSILON);
        Assert.assertEquals(StrictMath.tanh(-1.5), FastMath.tanh(-1.5), EPSILON);
    }

    @Test
    public void testAcosh() {
        Assert.assertEquals(0.0, FastMath.acosh(1.0), EPSILON);
        Assert.assertEquals(1.3169578969248166, FastMath.acosh(2.0), EPSILON);
        Assert.assertTrue(Double.isNaN(FastMath.acosh(0.5)));
        Assert.assertTrue(Double.isNaN(FastMath.acosh(Double.NaN)));
    }

    @Test
    public void testAsinh() {
        Assert.assertTrue(Double.isNaN(FastMath.asinh(Double.NaN)));
        Assert.assertEquals(0.0, FastMath.asinh(0.0), EPSILON);
        Assert.assertEquals(0.001, FastMath.asinh(0.001), 1e-6);
        Assert.assertEquals(-0.001, FastMath.asinh(-0.001), 1e-6);
        Assert.assertEquals(0.01, FastMath.asinh(0.01), 1e-5);
        Assert.assertEquals(0.05, FastMath.asinh(0.05), 1e-4);
        Assert.assertEquals(0.12, FastMath.asinh(0.12), 1e-3);
        Assert.assertEquals(2.0, FastMath.asinh(FastMath.sinh(2.0)), EPSILON);
        Assert.assertEquals(-2.0, FastMath.asinh(FastMath.sinh(-2.0)), EPSILON);
    }

    @Test
    public void testAtanh() {
        Assert.assertTrue(Double.isNaN(FastMath.atanh(Double.NaN)));
        Assert.assertTrue(Double.isNaN(FastMath.atanh(2.0)));
        Assert.assertTrue(Double.isNaN(FastMath.atanh(-2.0)));
        Assert.assertEquals(0.0, FastMath.atanh(0.0), EPSILON);
        Assert.assertEquals(0.001, FastMath.atanh(0.001), 1e-6);
        Assert.assertEquals(-0.001, FastMath.atanh(-0.001), 1e-6);
        Assert.assertEquals(0.01, FastMath.atanh(0.01), 1e-5);
        Assert.assertEquals(0.05, FastMath.atanh(0.05), 1e-4);
        Assert.assertEquals(0.1, FastMath.atanh(0.1), 1e-3);
        Assert.assertEquals(0.5, FastMath.atanh(FastMath.tanh(0.5)), EPSILON);
        Assert.assertEquals(-0.5, FastMath.atanh(FastMath.tanh(-0.5)), EPSILON);
    }

    @Test
    public void testSignumDouble() {
        Assert.assertEquals(1.0, FastMath.signum(50.0), EPSILON);
        Assert.assertEquals(-1.0, FastMath.signum(-50.0), EPSILON);
        Assert.assertEquals(0.0, FastMath.signum(0.0), 0.0);
        Assert.assertEquals(-0.0, FastMath.signum(-0.0), 0.0);
        Assert.assertTrue(Double.isNaN(FastMath.signum(Double.NaN)));
    }

    @Test
    public void testSignumFloat() {
        Assert.assertEquals(1.0f, FastMath.signum(50.0f), 1e-6f);
        Assert.assertEquals(-1.0f, FastMath.signum(-50.0f), 1e-6f);
        Assert.assertEquals(0.0f, FastMath.signum(0.0f), 0.0f);
        Assert.assertEquals(-0.0f, FastMath.signum(-0.0f), 0.0f);
        Assert.assertTrue(Float.isNaN(FastMath.signum(Float.NaN)));
    }

    @Test
    public void testNextUp() {
        Assert.assertEquals(Double.MIN_VALUE, FastMath.nextUp(0.0), 0.0);
        Assert.assertEquals(0.0, FastMath.nextUp(-Double.MIN_VALUE), 0.0);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.nextUp(Double.POSITIVE_INFINITY), 0.0);
        Assert.assertTrue(Double.isNaN(FastMath.nextUp(Double.NaN)));

        Assert.assertEquals(Float.MIN_VALUE, FastMath.nextUp(0.0f), 0.0f);
        Assert.assertEquals(0.0f, FastMath.nextUp(-Float.MIN_VALUE), 0.0f);
        Assert.assertEquals(Float.POSITIVE_INFINITY, FastMath.nextUp(Float.POSITIVE_INFINITY), 0.0f);
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
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.exp(750.0), EPSILON);
        Assert.assertEquals(0.0, FastMath.exp(-800.0), EPSILON);
        Assert.assertEquals(StrictMath.exp(-720.0), FastMath.exp(-720.0), EPSILON);
        Assert.assertEquals(StrictMath.exp(-709.5), FastMath.exp(-709.5), EPSILON);
        Assert.assertEquals(StrictMath.exp(2.5), FastMath.exp(2.5), EPSILON);
    }

    @Test
    public void testExpm1() {
        Assert.assertEquals(0.0, FastMath.expm1(0.0), 0.0);
        Assert.assertEquals(-0.0, FastMath.expm1(-0.0), 0.0);
        Assert.assertTrue(Double.isNaN(FastMath.expm1(Double.NaN)));
        Assert.assertEquals(StrictMath.expm1(2.0), FastMath.expm1(2.0), EPSILON);
        Assert.assertEquals(StrictMath.expm1(-2.0), FastMath.expm1(-2.0), EPSILON);
        Assert.assertEquals(StrictMath.expm1(0.5), FastMath.expm1(0.5), EPSILON);
        Assert.assertEquals(StrictMath.expm1(-0.5), FastMath.expm1(-0.5), EPSILON);
    }

    @Test
    public void testLog() {
        Assert.assertEquals(Double.NEGATIVE_INFINITY, FastMath.log(0.0), EPSILON);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, FastMath.log(-0.0), EPSILON);
        Assert.assertTrue(Double.isNaN(FastMath.log(-1.0)));
        Assert.assertTrue(Double.isNaN(FastMath.log(Double.NaN)));
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.log(Double.POSITIVE_INFINITY), EPSILON);
        Assert.assertEquals(0.0, FastMath.log(1.0), EPSILON);
        Assert.assertEquals(StrictMath.log(0.995), FastMath.log(0.995), EPSILON);
        Assert.assertEquals(StrictMath.log(1.005), FastMath.log(1.005), EPSILON);
        Assert.assertEquals(StrictMath.log(2.5), FastMath.log(2.5), EPSILON);
        Assert.assertEquals(StrictMath.log(1e-310), FastMath.log(1e-310), EPSILON);
    }

    @Test
    public void testLog1p() {
        Assert.assertEquals(Double.NEGATIVE_INFINITY, FastMath.log1p(-1.0), EPSILON);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.log1p(Double.POSITIVE_INFINITY), EPSILON);
        Assert.assertTrue(Double.isNaN(FastMath.log1p(-2.0)));
        Assert.assertEquals(0.0, FastMath.log1p(0.0), EPSILON);
        Assert.assertEquals(StrictMath.log1p(1e-8), FastMath.log1p(1e-8), 1e-15);
        Assert.assertEquals(StrictMath.log1p(0.5), FastMath.log1p(0.5), EPSILON);
        Assert.assertEquals(StrictMath.log1p(-0.5), FastMath.log1p(-0.5), EPSILON);
    }

    @Test
    public void testLog10() {
        Assert.assertEquals(1.0, FastMath.log10(10.0), EPSILON);
        Assert.assertEquals(2.0, FastMath.log10(100.0), EPSILON);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, FastMath.log10(0.0), EPSILON);
        Assert.assertTrue(Double.isNaN(FastMath.log10(-10.0)));
    }

    @Test
    public void testLogBase() {
        Assert.assertEquals(2.0, FastMath.log(10.0, 100.0), EPSILON);
        Assert.assertEquals(3.0, FastMath.log(2.0, 8.0), EPSILON);
        Assert.assertTrue(Double.isNaN(FastMath.log(-2.0, 8.0)));
        Assert.assertTrue(Double.isNaN(FastMath.log(2.0, -8.0)));
    }

    @Test
    public void testPowDoubleDouble() {
        Assert.assertEquals(1.0, FastMath.pow(5.0, 0.0), EPSILON);
        Assert.assertEquals(1.0, FastMath.pow(0.0, 0.0), EPSILON);
        Assert.assertTrue(Double.isNaN(FastMath.pow(Double.NaN, 2.0)));
        Assert.assertTrue(Double.isNaN(FastMath.pow(2.0, Double.NaN)));

        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(0.0, -2.0), EPSILON);
        Assert.assertEquals(0.0, FastMath.pow(0.0, 2.0), EPSILON);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, FastMath.pow(-0.0, -3.0), EPSILON);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(-0.0, -2.0), EPSILON);
        Assert.assertEquals(-0.0, FastMath.pow(-0.0, 3.0), 0.0);
        Assert.assertEquals(0.0, FastMath.pow(-0.0, 2.0), 0.0);

        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(Double.POSITIVE_INFINITY, 2.0), EPSILON);
        Assert.assertEquals(0.0, FastMath.pow(Double.POSITIVE_INFINITY, -2.0), EPSILON);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(2.0, Double.POSITIVE_INFINITY), EPSILON);
        Assert.assertEquals(0.0, FastMath.pow(0.5, Double.POSITIVE_INFINITY), EPSILON);
        Assert.assertTrue(Double.isNaN(FastMath.pow(1.0, Double.POSITIVE_INFINITY)));

        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(Double.NEGATIVE_INFINITY, 2.0), EPSILON);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, FastMath.pow(Double.NEGATIVE_INFINITY, 3.0), EPSILON);
        Assert.assertEquals(0.0, FastMath.pow(Double.NEGATIVE_INFINITY, -2.0), EPSILON);
        Assert.assertEquals(-0.0, FastMath.pow(Double.NEGATIVE_INFINITY, -3.0), 0.0);

        Assert.assertEquals(0.0, FastMath.pow(2.0, Double.NEGATIVE_INFINITY), EPSILON);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(0.5, Double.NEGATIVE_INFINITY), EPSILON);
        Assert.assertTrue(Double.isNaN(FastMath.pow(1.0, Double.NEGATIVE_INFINITY)));

        Assert.assertEquals(4.0, FastMath.pow(-2.0, 2.0), EPSILON);
        Assert.assertEquals(-8.0, FastMath.pow(-2.0, 3.0), EPSILON);
        Assert.assertTrue(Double.isNaN(FastMath.pow(-2.0, 2.5)));
        Assert.assertEquals(StrictMath.pow(2.0, 1e-12), FastMath.pow(2.0, 1e-12), EPSILON);
        Assert.assertEquals(StrictMath.pow(2.0, 1e300), FastMath.pow(2.0, 1e300), EPSILON);
        Assert.assertEquals(8.0, FastMath.pow(2.0, 3.0), EPSILON);
    }

    @Test
    public void testPowDoubleInt() {
        Assert.assertEquals(1.0, FastMath.pow(5.0, 0), EPSILON);
        Assert.assertEquals(8.0, FastMath.pow(2.0, 3), EPSILON);
        Assert.assertEquals(0.125, FastMath.pow(2.0, -3), EPSILON);
        Assert.assertEquals(16.0, FastMath.pow(-2.0, 4), EPSILON);
        Assert.assertEquals(-32.0, FastMath.pow(-2.0, 5), EPSILON);
    }

    @Test
    public void testTrigonometry() {
        Assert.assertTrue(Double.isNaN(FastMath.sin(Double.NaN)));
        Assert.assertTrue(Double.isNaN(FastMath.sin(Double.POSITIVE_INFINITY)));
        Assert.assertEquals(0.0, FastMath.sin(0.0), 0.0);
        Assert.assertEquals(-0.0, FastMath.sin(-0.0), 0.0);
        Assert.assertEquals(StrictMath.sin(1.0), FastMath.sin(1.0), EPSILON);
        Assert.assertEquals(StrictMath.sin(3.0), FastMath.sin(3.0), EPSILON);
        Assert.assertEquals(StrictMath.sin(-3.0), FastMath.sin(-3.0), EPSILON);
        Assert.assertEquals(StrictMath.sin(100.0), FastMath.sin(100.0), EPSILON);
        Assert.assertEquals(StrictMath.sin(4000000.0), FastMath.sin(4000000.0), EPSILON);

        Assert.assertTrue(Double.isNaN(FastMath.cos(Double.NaN)));
        Assert.assertTrue(Double.isNaN(FastMath.cos(Double.POSITIVE_INFINITY)));
        Assert.assertEquals(1.0, FastMath.cos(0.0), EPSILON);
        Assert.assertEquals(StrictMath.cos(1.0), FastMath.cos(1.0), EPSILON);
        Assert.assertEquals(StrictMath.cos(3.0), FastMath.cos(3.0), EPSILON);
        Assert.assertEquals(StrictMath.cos(-3.0), FastMath.cos(-3.0), EPSILON);
        Assert.assertEquals(StrictMath.cos(100.0), FastMath.cos(100.0), EPSILON);
        Assert.assertEquals(StrictMath.cos(4000000.0), FastMath.cos(4000000.0), EPSILON);

        Assert.assertTrue(Double.isNaN(FastMath.tan(Double.NaN)));
        Assert.assertTrue(Double.isNaN(FastMath.tan(Double.POSITIVE_INFINITY)));
        Assert.assertEquals(0.0, FastMath.tan(0.0), 0.0);
        Assert.assertEquals(-0.0, FastMath.tan(-0.0), 0.0);
        Assert.assertEquals(StrictMath.tan(1.0), FastMath.tan(1.0), EPSILON);
        Assert.assertEquals(StrictMath.tan(1.55), FastMath.tan(1.55), EPSILON);
        Assert.assertEquals(StrictMath.tan(3.0), FastMath.tan(3.0), EPSILON);
        Assert.assertEquals(StrictMath.tan(-3.0), FastMath.tan(-3.0), EPSILON);
        Assert.assertEquals(StrictMath.tan(100.0), FastMath.tan(100.0), EPSILON);
        Assert.assertEquals(StrictMath.tan(4000000.0), FastMath.tan(4000000.0), EPSILON);
    }

    @Test
    public void testInverseTrigonometry() {
        Assert.assertTrue(Double.isNaN(FastMath.atan(Double.NaN)));
        Assert.assertEquals(0.0, FastMath.atan(0.0), 0.0);
        Assert.assertEquals(-0.0, FastMath.atan(-0.0), 0.0);
        Assert.assertEquals(Math.PI / 2.0, FastMath.atan(1e20), EPSILON);
        Assert.assertEquals(-Math.PI / 2.0, FastMath.atan(-1e20), EPSILON);
        Assert.assertEquals(StrictMath.atan(0.5), FastMath.atan(0.5), EPSILON);
        Assert.assertEquals(StrictMath.atan(2.0), FastMath.atan(2.0), EPSILON);
        Assert.assertEquals(StrictMath.atan(-2.0), FastMath.atan(-2.0), EPSILON);

        Assert.assertTrue(Double.isNaN(FastMath.asin(Double.NaN)));
        Assert.assertTrue(Double.isNaN(FastMath.asin(2.0)));
        Assert.assertTrue(Double.isNaN(FastMath.asin(-2.0)));
        Assert.assertEquals(0.0, FastMath.asin(0.0), 0.0);
        Assert.assertEquals(Math.PI / 2.0, FastMath.asin(1.0), EPSILON);
        Assert.assertEquals(-Math.PI / 2.0, FastMath.asin(-1.0), EPSILON);
        Assert.assertEquals(StrictMath.asin(0.5), FastMath.asin(0.5), EPSILON);
        Assert.assertEquals(StrictMath.asin(-0.5), FastMath.asin(-0.5), EPSILON);

        Assert.assertTrue(Double.isNaN(FastMath.acos(Double.NaN)));
        Assert.assertTrue(Double.isNaN(FastMath.acos(2.0)));
        Assert.assertTrue(Double.isNaN(FastMath.acos(-2.0)));
        Assert.assertEquals(Math.PI / 2.0, FastMath.acos(0.0), EPSILON);
        Assert.assertEquals(0.0, FastMath.acos(1.0), EPSILON);
        Assert.assertEquals(Math.PI, FastMath.acos(-1.0), EPSILON);
        Assert.assertEquals(StrictMath.acos(0.5), FastMath.acos(0.5), EPSILON);
        Assert.assertEquals(StrictMath.acos(-0.5), FastMath.acos(-0.5), EPSILON);
    }

    @Test
    public void testAtan2() {
        Assert.assertTrue(Double.isNaN(FastMath.atan2(Double.NaN, 1.0)));
        Assert.assertTrue(Double.isNaN(FastMath.atan2(1.0, Double.NaN)));
        Assert.assertEquals(0.0, FastMath.atan2(0.0, 2.0), 0.0);
        Assert.assertEquals(-0.0, FastMath.atan2(-0.0, 2.0), 0.0);
        Assert.assertEquals(Math.PI, FastMath.atan2(0.0, -2.0), EPSILON);
        Assert.assertEquals(-Math.PI, FastMath.atan2(-0.0, -2.0), EPSILON);
        Assert.assertEquals(0.0, FastMath.atan2(0.0, Double.POSITIVE_INFINITY), 0.0);
        Assert.assertEquals(-0.0, FastMath.atan2(-0.0, Double.POSITIVE_INFINITY), 0.0);
        Assert.assertEquals(Math.PI, FastMath.atan2(0.0, Double.NEGATIVE_INFINITY), EPSILON);
        Assert.assertEquals(-Math.PI, FastMath.atan2(-0.0, Double.NEGATIVE_INFINITY), EPSILON);

        Assert.assertEquals(Math.PI / 4.0, FastMath.atan2(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY), EPSILON);
        Assert.assertEquals(3.0 * Math.PI / 4.0, FastMath.atan2(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY), EPSILON);
        Assert.assertEquals(Math.PI / 2.0, FastMath.atan2(Double.POSITIVE_INFINITY, 1.0), EPSILON);

        Assert.assertEquals(-Math.PI / 4.0, FastMath.atan2(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY), EPSILON);
        Assert.assertEquals(-3.0 * Math.PI / 4.0, FastMath.atan2(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY), EPSILON);
        Assert.assertEquals(-Math.PI / 2.0, FastMath.atan2(Double.NEGATIVE_INFINITY, 1.0), EPSILON);

        Assert.assertEquals(0.0, FastMath.atan2(1.0, Double.POSITIVE_INFINITY), EPSILON);
        Assert.assertEquals(-0.0, FastMath.atan2(-1.0, Double.POSITIVE_INFINITY), EPSILON);
        Assert.assertEquals(Math.PI, FastMath.atan2(1.0, Double.NEGATIVE_INFINITY), EPSILON);
        Assert.assertEquals(-Math.PI, FastMath.atan2(-1.0, Double.NEGATIVE_INFINITY), EPSILON);

        Assert.assertEquals(Math.PI / 2.0, FastMath.atan2(1.0, 0.0), EPSILON);
        Assert.assertEquals(-Math.PI / 2.0, FastMath.atan2(-1.0, 0.0), EPSILON);

        Assert.assertEquals(StrictMath.atan2(1.0, 1.0), FastMath.atan2(1.0, 1.0), EPSILON);
        Assert.assertEquals(StrictMath.atan2(1.0, -1.0), FastMath.atan2(1.0, -1.0), EPSILON);
        Assert.assertEquals(StrictMath.atan2(-1.0, 1.0), FastMath.atan2(-1.0, 1.0), EPSILON);
        Assert.assertEquals(StrictMath.atan2(-1.0, -1.0), FastMath.atan2(-1.0, -1.0), EPSILON);
    }

    @Test
    public void testCbrt() {
        Assert.assertEquals(0.0, FastMath.cbrt(0.0), 0.0);
        Assert.assertEquals(-0.0, FastMath.cbrt(-0.0), 0.0);
        Assert.assertTrue(Double.isNaN(FastMath.cbrt(Double.NaN)));
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.cbrt(Double.POSITIVE_INFINITY), EPSILON);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, FastMath.cbrt(Double.NEGATIVE_INFINITY), EPSILON);
        Assert.assertEquals(2.0, FastMath.cbrt(8.0), EPSILON);
        Assert.assertEquals(-2.0, FastMath.cbrt(-8.0), EPSILON);
        Assert.assertEquals(StrictMath.cbrt(1e-310), FastMath.cbrt(1e-310), 1e-105);
    }

    @Test
    public void testDegreesRadians() {
        Assert.assertEquals(0.0, FastMath.toRadians(0.0), 0.0);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.toRadians(Double.POSITIVE_INFINITY), EPSILON);
        Assert.assertEquals(Math.PI, FastMath.toRadians(180.0), EPSILON);
        Assert.assertEquals(Math.PI / 2.0, FastMath.toRadians(90.0), EPSILON);

        Assert.assertEquals(0.0, FastMath.toDegrees(0.0), 0.0);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.toDegrees(Double.POSITIVE_INFINITY), EPSILON);
        Assert.assertEquals(180.0, FastMath.toDegrees(Math.PI), EPSILON);
        Assert.assertEquals(90.0, FastMath.toDegrees(Math.PI / 2.0), EPSILON);
    }

    @Test
    public void testAbs() {
        Assert.assertEquals(5, FastMath.abs(5));
        Assert.assertEquals(5, FastMath.abs(-5));
        Assert.assertEquals(5L, FastMath.abs(5L));
        Assert.assertEquals(5L, FastMath.abs(-5L));
        Assert.assertEquals(5.5f, FastMath.abs(5.5f), 1e-6f);
        Assert.assertEquals(5.5f, FastMath.abs(-5.5f), 1e-6f);
        Assert.assertEquals(0.0f, FastMath.abs(-0.0f), 0.0f);
        Assert.assertEquals(5.5, FastMath.abs(5.5), EPSILON);
        Assert.assertEquals(5.5, FastMath.abs(-5.5), EPSILON);
        Assert.assertEquals(0.0, FastMath.abs(-0.0), 0.0);
    }

    @Test
    public void testUlp() {
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.ulp(Double.POSITIVE_INFINITY), EPSILON);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.ulp(Double.NEGATIVE_INFINITY), EPSILON);
        Assert.assertEquals(Math.ulp(1.0), FastMath.ulp(1.0), EPSILON);
        Assert.assertEquals(Math.ulp(0.0), FastMath.ulp(0.0), EPSILON);

        Assert.assertEquals(Float.POSITIVE_INFINITY, FastMath.ulp(Float.POSITIVE_INFINITY), 0.0f);
        Assert.assertEquals(Float.POSITIVE_INFINITY, FastMath.ulp(Float.NEGATIVE_INFINITY), 0.0f);
        Assert.assertEquals(Math.ulp(1.0f), FastMath.ulp(1.0f), 0.0f);
        Assert.assertEquals(Math.ulp(0.0f), FastMath.ulp(0.0f), 0.0f);
    }

    @Test
    public void testScalbDouble() {
        Assert.assertEquals(4.0, FastMath.scalb(1.0, 2), EPSILON);
        Assert.assertEquals(0.25, FastMath.scalb(1.0, -2), EPSILON);
        Assert.assertTrue(Double.isNaN(FastMath.scalb(Double.NaN, 2)));
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.scalb(Double.POSITIVE_INFINITY, 2), EPSILON);
        Assert.assertEquals(0.0, FastMath.scalb(0.0, 2), 0.0);
        Assert.assertEquals(0.0, FastMath.scalb(1.0, -3000), 0.0);
        Assert.assertEquals(-0.0, FastMath.scalb(-1.0, -3000), 0.0);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.scalb(1.0, 3000), EPSILON);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, FastMath.scalb(-1.0, 3000), EPSILON);

        Assert.assertEquals(StrictMath.scalb(1.0, -1030), FastMath.scalb(1.0, -1030), EPSILON);
        Assert.assertEquals(StrictMath.scalb(1.0, -1080), FastMath.scalb(1.0, -1080), EPSILON);
        Assert.assertEquals(StrictMath.scalb(1e-310, 1030), FastMath.scalb(1e-310, 1030), EPSILON);
        Assert.assertEquals(StrictMath.scalb(1.0, 1030), FastMath.scalb(1.0, 1030), EPSILON);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.scalb(1.0, 2048), EPSILON);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, FastMath.scalb(-1.0, 2048), EPSILON);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.scalb(Double.MIN_VALUE, 2100), EPSILON);
    }

    @Test
    public void testScalbFloat() {
        Assert.assertEquals(4.0f, FastMath.scalb(1.0f, 2), 1e-6f);
        Assert.assertEquals(0.25f, FastMath.scalb(1.0f, -2), 1e-6f);
        Assert.assertTrue(Float.isNaN(FastMath.scalb(Float.NaN, 2)));
        Assert.assertEquals(Float.POSITIVE_INFINITY, FastMath.scalb(Float.POSITIVE_INFINITY, 2), 0.0f);
        Assert.assertEquals(0.0f, FastMath.scalb(0.0f, 2), 0.0f);
        Assert.assertEquals(0.0f, FastMath.scalb(1.0f, -300), 0.0f);
        Assert.assertEquals(-0.0f, FastMath.scalb(-1.0f, -300), 0.0f);
        Assert.assertEquals(Float.POSITIVE_INFINITY, FastMath.scalb(1.0f, 300), 0.0f);
        Assert.assertEquals(Float.NEGATIVE_INFINITY, FastMath.scalb(-1.0f, 300), 0.0f);

        Assert.assertEquals(StrictMath.scalb(1.0f, -130), FastMath.scalb(1.0f, -130), 1e-6f);
        Assert.assertEquals(StrictMath.scalb(1.0f, -145), FastMath.scalb(1.0f, -145), 1e-6f);
        Assert.assertEquals(StrictMath.scalb(1e-40f, 130), FastMath.scalb(1e-40f, 130), 1e-6f);
        Assert.assertEquals(StrictMath.scalb(1.0f, 130), FastMath.scalb(1.0f, 130), 1e-6f);
        Assert.assertEquals(Float.POSITIVE_INFINITY, FastMath.scalb(1.0f, 256), 0.0f);
        Assert.assertEquals(Float.NEGATIVE_INFINITY, FastMath.scalb(-1.0f, 256), 0.0f);
        Assert.assertEquals(Float.POSITIVE_INFINITY, FastMath.scalb(Float.MIN_VALUE, 300), 0.0f);
    }

    @Test
    public void testNextAfterDouble() {
        Assert.assertTrue(Double.isNaN(FastMath.nextAfter(Double.NaN, 1.0)));
        Assert.assertTrue(Double.isNaN(FastMath.nextAfter(1.0, Double.NaN)));
        Assert.assertEquals(2.0, FastMath.nextAfter(2.0, 2.0), 0.0);
        Assert.assertEquals(Double.MAX_VALUE, FastMath.nextAfter(Double.POSITIVE_INFINITY, 0.0), 0.0);
        Assert.assertEquals(-Double.MAX_VALUE, FastMath.nextAfter(Double.NEGATIVE_INFINITY, 0.0), 0.0);
        Assert.assertEquals(Double.MIN_VALUE, FastMath.nextAfter(0.0, 1.0), 0.0);
        Assert.assertEquals(-Double.MIN_VALUE, FastMath.nextAfter(0.0, -1.0), 0.0);
        Assert.assertEquals(Math.nextAfter(1.0, 2.0), FastMath.nextAfter(1.0, 2.0), 0.0);
        Assert.assertEquals(Math.nextAfter(1.0, 0.0), FastMath.nextAfter(1.0, 0.0), 0.0);
        Assert.assertEquals(Math.nextAfter(-1.0, -2.0), FastMath.nextAfter(-1.0, -2.0), 0.0);
        Assert.assertEquals(Math.nextAfter(-1.0, 0.0), FastMath.nextAfter(-1.0, 0.0), 0.0);
    }

    @Test
    public void testNextAfterFloat() {
        Assert.assertTrue(Float.isNaN(FastMath.nextAfter(Float.NaN, 1.0)));
        Assert.assertTrue(Float.isNaN(FastMath.nextAfter(1.0f, Double.NaN)));
        Assert.assertEquals(2.0f, FastMath.nextAfter(2.0f, 2.0), 0.0f);
        Assert.assertEquals(Float.MAX_VALUE, FastMath.nextAfter(Float.POSITIVE_INFINITY, 0.0), 0.0f);
        Assert.assertEquals(-Float.MAX_VALUE, FastMath.nextAfter(Float.NEGATIVE_INFINITY, 0.0), 0.0f);
        Assert.assertEquals(Float.MIN_VALUE, FastMath.nextAfter(0.0f, 1.0), 0.0f);
        Assert.assertEquals(-Float.MIN_VALUE, FastMath.nextAfter(0.0f, -1.0), 0.0f);
        Assert.assertEquals(Math.nextAfter(1.0f, 2.0), FastMath.nextAfter(1.0f, 2.0), 0.0f);
        Assert.assertEquals(Math.nextAfter(1.0f, 0.0), FastMath.nextAfter(1.0f, 0.0), 0.0f);
        Assert.assertEquals(Math.nextAfter(-1.0f, -2.0), FastMath.nextAfter(-1.0f, -2.0), 0.0f);
        Assert.assertEquals(Math.nextAfter(-1.0f, 0.0), FastMath.nextAfter(-1.0f, 0.0), 0.0f);
    }

    @Test
    public void testFloorCeilRintRound() {
        Assert.assertTrue(Double.isNaN(FastMath.floor(Double.NaN)));
        Assert.assertEquals(1e16, FastMath.floor(1e16), 0.0);
        Assert.assertEquals(-1e16, FastMath.floor(-1e16), 0.0);
        Assert.assertEquals(2.0, FastMath.floor(2.8), 0.0);
        Assert.assertEquals(-3.0, FastMath.floor(-2.3), 0.0);
        Assert.assertEquals(0.0, FastMath.floor(0.5), 0.0);
        Assert.assertEquals(-0.0, FastMath.floor(-0.0), 0.0);

        Assert.assertTrue(Double.isNaN(FastMath.ceil(Double.NaN)));
        Assert.assertEquals(2.0, FastMath.ceil(2.0), 0.0);
        Assert.assertEquals(3.0, FastMath.ceil(2.3), 0.0);
        Assert.assertEquals(-2.0, FastMath.ceil(-2.8), 0.0);
        Assert.assertEquals(-0.0, FastMath.ceil(-0.5), 0.0);

        Assert.assertEquals(2.0, FastMath.rint(2.3), 0.0);
        Assert.assertEquals(3.0, FastMath.rint(2.8), 0.0);
        Assert.assertEquals(2.0, FastMath.rint(2.5), 0.0);
        Assert.assertEquals(4.0, FastMath.rint(3.5), 0.0);
        Assert.assertEquals(-0.0, FastMath.rint(-0.2), 0.0);

        Assert.assertEquals(3L, FastMath.round(2.6));
        Assert.assertEquals(2L, FastMath.round(2.4));
        Assert.assertEquals(-2L, FastMath.round(-2.4));
        Assert.assertEquals(-3L, FastMath.round(-2.6));

        Assert.assertEquals(3, FastMath.round(2.6f));
        Assert.assertEquals(2, FastMath.round(2.4f));
        Assert.assertEquals(-2, FastMath.round(-2.4f));
        Assert.assertEquals(-3, FastMath.round(-2.6f));
    }

    @Test
    public void testMinMax() {
        Assert.assertEquals(2, FastMath.min(2, 5));
        Assert.assertEquals(2, FastMath.min(5, 2));
        Assert.assertEquals(2L, FastMath.min(2L, 5L));
        Assert.assertEquals(2L, FastMath.min(5L, 2L));

        Assert.assertEquals(2.0f, FastMath.min(2.0f, 5.0f), 0.0f);
        Assert.assertEquals(2.0f, FastMath.min(5.0f, 2.0f), 0.0f);
        Assert.assertTrue(Float.isNaN(FastMath.min(Float.NaN, 2.0f)));
        Assert.assertTrue(Float.isNaN(FastMath.min(2.0f, Float.NaN)));
        Assert.assertEquals(-0.0f, FastMath.min(-0.0f, 0.0f), 0.0f);
        Assert.assertEquals(-0.0f, FastMath.min(0.0f, -0.0f), 0.0f);

        Assert.assertEquals(2.0, FastMath.min(2.0, 5.0), 0.0);
        Assert.assertEquals(2.0, FastMath.min(5.0, 2.0), 0.0);
        Assert.assertTrue(Double.isNaN(FastMath.min(Double.NaN, 2.0)));
        Assert.assertTrue(Double.isNaN(FastMath.min(2.0, Double.NaN)));
        Assert.assertEquals(-0.0, FastMath.min(-0.0, 0.0), 0.0);
        Assert.assertEquals(-0.0, FastMath.min(0.0, -0.0), 0.0);

        Assert.assertEquals(5, FastMath.max(2, 5));
        Assert.assertEquals(5, FastMath.max(5, 2));
        Assert.assertEquals(5L, FastMath.max(2L, 5L));
        Assert.assertEquals(5L, FastMath.max(5L, 2L));

        Assert.assertEquals(5.0f, FastMath.max(2.0f, 5.0f), 0.0f);
        Assert.assertEquals(5.0f, FastMath.max(5.0f, 2.0f), 0.0f);
        Assert.assertTrue(Float.isNaN(FastMath.max(Float.NaN, 2.0f)));
        Assert.assertTrue(Float.isNaN(FastMath.max(2.0f, Float.NaN)));
        Assert.assertEquals(0.0f, FastMath.max(-0.0f, 0.0f), 0.0f);
        Assert.assertEquals(0.0f, FastMath.max(0.0f, -0.0f), 0.0f);

        Assert.assertEquals(5.0, FastMath.max(2.0, 5.0), 0.0);
        Assert.assertEquals(5.0, FastMath.max(5.0, 2.0), 0.0);
        Assert.assertTrue(Double.isNaN(FastMath.max(Double.NaN, 2.0)));
        Assert.assertTrue(Double.isNaN(FastMath.max(2.0, Double.NaN)));
        Assert.assertEquals(0.0, FastMath.max(-0.0, 0.0), 0.0);
        Assert.assertEquals(0.0, FastMath.max(0.0, -0.0), 0.0);
    }

    @Test
    public void testHypot() {
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.hypot(Double.POSITIVE_INFINITY, 1.0), EPSILON);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.hypot(1.0, Double.NEGATIVE_INFINITY), EPSILON);
        Assert.assertTrue(Double.isNaN(FastMath.hypot(Double.NaN, 1.0)));
        Assert.assertTrue(Double.isNaN(FastMath.hypot(1.0, Double.NaN)));
        Assert.assertEquals(1e30, FastMath.hypot(1e30, 1.0), 1e15);
        Assert.assertEquals(1e30, FastMath.hypot(1.0, 1e30), 1e15);
        Assert.assertEquals(5.0, FastMath.hypot(3.0, 4.0), EPSILON);
        Assert.assertEquals(5e200, FastMath.hypot(3e200, 4e200), 1e190);
        Assert.assertEquals(5e-200, FastMath.hypot(3e-200, 4e-200), 1e-210);
    }

    @Test
    public void testIEEEremainder() {
        Assert.assertEquals(StrictMath.IEEEremainder(5.0, 3.0), FastMath.IEEEremainder(5.0, 3.0), EPSILON);
        Assert.assertEquals(StrictMath.IEEEremainder(5.0, 2.5), FastMath.IEEEremainder(5.0, 2.5), EPSILON);
    }

    @Test
    public void testCopySign() {
        Assert.assertEquals(2.0, FastMath.copySign(2.0, 1.0), 0.0);
        Assert.assertEquals(-2.0, FastMath.copySign(2.0, -1.0), 0.0);
        Assert.assertEquals(2.0, FastMath.copySign(-2.0, 1.0), 0.0);
        Assert.assertEquals(-2.0, FastMath.copySign(-2.0, -1.0), 0.0);
        Assert.assertEquals(2.0, FastMath.copySign(2.0, Double.NaN), 0.0);

        Assert.assertEquals(2.0f, FastMath.copySign(2.0f, 1.0f), 0.0f);
        Assert.assertEquals(-2.0f, FastMath.copySign(2.0f, -1.0f), 0.0f);
        Assert.assertEquals(2.0f, FastMath.copySign(-2.0f, 1.0f), 0.0f);
        Assert.assertEquals(-2.0f, FastMath.copySign(-2.0f, -1.0f), 0.0f);
        Assert.assertEquals(2.0f, FastMath.copySign(2.0f, Float.NaN), 0.0f);
    }

    @Test
    public void testGetExponent() {
        Assert.assertEquals(3, FastMath.getExponent(8.0));
        Assert.assertEquals(-3, FastMath.getExponent(0.125));
        Assert.assertEquals(0, FastMath.getExponent(1.0));
        Assert.assertEquals(1024, FastMath.getExponent(Double.POSITIVE_INFINITY));

        Assert.assertEquals(3, FastMath.getExponent(8.0f));
        Assert.assertEquals(-3, FastMath.getExponent(0.125f));
        Assert.assertEquals(0, FastMath.getExponent(1.0f));
        Assert.assertEquals(128, FastMath.getExponent(Float.POSITIVE_INFINITY));
    }

    @Test
    public void testMainMethod() {
        PrintStream originalOut = System.out;
        try {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outContent));
            FastMath.main(new String[0]);
            Assert.assertTrue(outContent.toString().length() > 0);
        } finally {
            System.setOut(originalOut);
        }
    }
}
