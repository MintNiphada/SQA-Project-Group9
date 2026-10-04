package org.apache.commons.math.util;

import org.junit.Assert;
import org.junit.Test;

public class FastMathTest {

    private static final double EPSILON = 1e-12;

    @Test
    public void testSqrt() {
        Assert.assertEquals(2.0, FastMath.sqrt(4.0), EPSILON);
        Assert.assertEquals(0.0, FastMath.sqrt(0.0), EPSILON);
        Assert.assertTrue(Double.isNaN(FastMath.sqrt(-1.0)));
    }

    @Test
    public void testCosh() {
        Assert.assertTrue(Double.isNaN(FastMath.cosh(Double.NaN)));
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.cosh(750.0), EPSILON);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.cosh(-750.0), EPSILON);
        Assert.assertEquals(Math.cosh(25.0), FastMath.cosh(25.0), 1e-8);
        Assert.assertEquals(Math.cosh(-25.0), FastMath.cosh(-25.0), 1e-8);
        Assert.assertEquals(Math.cosh(2.0), FastMath.cosh(2.0), EPSILON);
        Assert.assertEquals(Math.cosh(-2.0), FastMath.cosh(-2.0), EPSILON);
        Assert.assertEquals(1.0, FastMath.cosh(0.0), EPSILON);
    }

    @Test
    public void testSinh() {
        Assert.assertTrue(Double.isNaN(FastMath.sinh(Double.NaN)));
        Assert.assertEquals(0.0, FastMath.sinh(0.0), EPSILON);
        Assert.assertEquals(-0.0, FastMath.sinh(-0.0), EPSILON);
        Assert.assertEquals(Math.sinh(25.0), FastMath.sinh(25.0), 1e-8);
        Assert.assertEquals(Math.sinh(-25.0), FastMath.sinh(-25.0), 1e-8);
        Assert.assertEquals(Math.sinh(2.0), FastMath.sinh(2.0), EPSILON);
        Assert.assertEquals(Math.sinh(-2.0), FastMath.sinh(-2.0), EPSILON);
        Assert.assertEquals(Math.sinh(0.1), FastMath.sinh(0.1), EPSILON);
        Assert.assertEquals(Math.sinh(-0.1), FastMath.sinh(-0.1), EPSILON);
    }

    @Test
    public void testTanh() {
        Assert.assertTrue(Double.isNaN(FastMath.tanh(Double.NaN)));
        Assert.assertEquals(1.0, FastMath.tanh(25.0), EPSILON);
        Assert.assertEquals(-1.0, FastMath.tanh(-25.0), EPSILON);
        Assert.assertEquals(0.0, FastMath.tanh(0.0), EPSILON);
        Assert.assertEquals(-0.0, FastMath.tanh(-0.0), EPSILON);
        Assert.assertEquals(Math.tanh(1.5), FastMath.tanh(1.5), EPSILON);
        Assert.assertEquals(Math.tanh(-1.5), FastMath.tanh(-1.5), EPSILON);
        Assert.assertEquals(Math.tanh(0.2), FastMath.tanh(0.2), EPSILON);
        Assert.assertEquals(Math.tanh(-0.2), FastMath.tanh(-0.2), EPSILON);
    }

    @Test
    public void testAcosh() {
        Assert.assertEquals(0.0, FastMath.acosh(1.0), EPSILON);
        Assert.assertEquals(1.3169578969248166, FastMath.acosh(2.0), EPSILON);
        Assert.assertTrue(Double.isNaN(FastMath.acosh(0.5)));
    }

    @Test
    public void testAsinh() {
        Assert.assertEquals(0.0, FastMath.asinh(0.0), EPSILON);
        Assert.assertEquals(-FastMath.asinh(2.0), FastMath.asinh(-2.0), EPSILON);
        Assert.assertEquals(1.4436354751788103, FastMath.asinh(2.0), EPSILON);
        Assert.assertEquals(0.14944811520779748, FastMath.asinh(0.15), EPSILON);
        Assert.assertEquals(0.04997919240455827, FastMath.asinh(0.05), EPSILON);
        Assert.assertEquals(0.0099998333399998, FastMath.asinh(0.01), EPSILON);
        Assert.assertEquals(0.0009999998333333467, FastMath.asinh(0.001), EPSILON);
    }

    @Test
    public void testAtanh() {
        Assert.assertEquals(0.0, FastMath.atanh(0.0), EPSILON);
        Assert.assertEquals(-FastMath.atanh(0.5), FastMath.atanh(-0.5), EPSILON);
        Assert.assertEquals(0.5493061443340549, FastMath.atanh(0.5), EPSILON);
        Assert.assertEquals(0.10033534773107558, FastMath.atanh(0.1), EPSILON);
        Assert.assertEquals(0.05004172927849162, FastMath.atanh(0.05), EPSILON);
        Assert.assertEquals(0.01000033335333476, FastMath.atanh(0.01), EPSILON);
        Assert.assertEquals(0.0010000003333335334, FastMath.atanh(0.001), EPSILON);
    }

    @Test
    public void testSignum() {
        Assert.assertEquals(1.0, FastMath.signum(50.0), EPSILON);
        Assert.assertEquals(-1.0, FastMath.signum(-50.0), EPSILON);
        Assert.assertEquals(0.0, FastMath.signum(0.0), EPSILON);
        Assert.assertTrue(Double.isNaN(FastMath.signum(Double.NaN)));
    }

    @Test
    public void testNextUpAndRandom() {
        Assert.assertEquals(Double.MIN_VALUE, FastMath.nextUp(0.0), 0.0);
        double r = FastMath.random();
        Assert.assertTrue(r >= 0.0 && r < 1.0);
    }

    @Test
    public void testExp() {
        Assert.assertEquals(Math.exp(1.0), FastMath.exp(1.0), EPSILON);
        Assert.assertEquals(0.0, FastMath.exp(-800.0), EPSILON);
        Assert.assertEquals(Math.exp(-715.0), FastMath.exp(-715.0), 1e-300);
        Assert.assertEquals(Math.exp(-709.2), FastMath.exp(-709.2), 1e-300);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.exp(750.0), 0.0);
        Assert.assertEquals(Math.exp(5.5), FastMath.exp(5.5), EPSILON);
        Assert.assertEquals(Math.exp(-5.5), FastMath.exp(-5.5), EPSILON);
    }

    @Test
    public void testExpm1() {
        Assert.assertTrue(Double.isNaN(FastMath.expm1(Double.NaN)));
        Assert.assertEquals(0.0, FastMath.expm1(0.0), EPSILON);
        Assert.assertEquals(Math.expm1(2.0), FastMath.expm1(2.0), EPSILON);
        Assert.assertEquals(Math.expm1(-2.0), FastMath.expm1(-2.0), EPSILON);
        Assert.assertEquals(Math.expm1(0.5), FastMath.expm1(0.5), EPSILON);
        Assert.assertEquals(Math.expm1(-0.5), FastMath.expm1(-0.5), EPSILON);
    }

    @Test
    public void testLog() {
        Assert.assertTrue(Double.isNaN(FastMath.log(-1.0)));
        Assert.assertTrue(Double.isNaN(FastMath.log(Double.NaN)));
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.log(Double.POSITIVE_INFINITY), 0.0);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, FastMath.log(0.0), 0.0);
        Assert.assertEquals(Math.log(1.005), FastMath.log(1.005), EPSILON);
        Assert.assertEquals(Math.log(0.995), FastMath.log(0.995), EPSILON);
        Assert.assertEquals(Math.log(1e-310), FastMath.log(1e-310), EPSILON);
        Assert.assertEquals(Math.log(10.0), FastMath.log(10.0), EPSILON);
    }

    @Test
    public void testLog1p() {
        Assert.assertEquals(Double.NEGATIVE_INFINITY, FastMath.log1p(-1.0), 0.0);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.log1p(Double.POSITIVE_INFINITY), 0.0);
        Assert.assertEquals(Math.log1p(0.5), FastMath.log1p(0.5), EPSILON);
        Assert.assertEquals(Math.log1p(1e-8), FastMath.log1p(1e-8), EPSILON);
    }

    @Test
    public void testLog10() {
        Assert.assertEquals(1.0, FastMath.log10(10.0), EPSILON);
        Assert.assertEquals(2.0, FastMath.log10(100.0), EPSILON);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, FastMath.log10(0.0), 0.0);
    }

    @Test
    public void testPow() {
        Assert.assertEquals(1.0, FastMath.pow(5.0, 0.0), EPSILON);
        Assert.assertTrue(Double.isNaN(FastMath.pow(Double.NaN, 2.0)));
        Assert.assertTrue(Double.isNaN(FastMath.pow(2.0, Double.NaN)));
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(0.0, -2.0), 0.0);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, FastMath.pow(-0.0, -3.0), 0.0);
        Assert.assertEquals(-0.0, FastMath.pow(-0.0, 3.0), 0.0);
        Assert.assertEquals(0.0, FastMath.pow(0.0, 2.0), 0.0);
        Assert.assertTrue(Double.isNaN(FastMath.pow(0.0, Double.NaN)));
        Assert.assertEquals(0.0, FastMath.pow(Double.POSITIVE_INFINITY, -2.0), 0.0);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(Double.POSITIVE_INFINITY, 2.0), 0.0);
        Assert.assertTrue(Double.isNaN(FastMath.pow(1.0, Double.POSITIVE_INFINITY)));
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(2.0, Double.POSITIVE_INFINITY), 0.0);
        Assert.assertEquals(0.0, FastMath.pow(0.5, Double.POSITIVE_INFINITY), 0.0);
        Assert.assertEquals(-0.0, FastMath.pow(Double.NEGATIVE_INFINITY, -3.0), 0.0);
        Assert.assertEquals(0.0, FastMath.pow(Double.NEGATIVE_INFINITY, -2.0), 0.0);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, FastMath.pow(Double.NEGATIVE_INFINITY, 3.0), 0.0);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(Double.NEGATIVE_INFINITY, 2.0), 0.0);
        Assert.assertTrue(Double.isNaN(FastMath.pow(1.0, Double.NEGATIVE_INFINITY)));
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(0.5, Double.NEGATIVE_INFINITY), 0.0);
        Assert.assertEquals(0.0, FastMath.pow(2.0, Double.NEGATIVE_INFINITY), 0.0);
        Assert.assertEquals(4.0, FastMath.pow(-2.0, 2.0), EPSILON);
        Assert.assertEquals(-8.0, FastMath.pow(-2.0, 3.0), EPSILON);
        Assert.assertTrue(Double.isNaN(FastMath.pow(-2.0, 2.5)));
        Assert.assertEquals(Math.pow(-2.0, 5e15), FastMath.pow(-2.0, 5e15), EPSILON);
        Assert.assertEquals(Math.pow(2.0, 1e300), FastMath.pow(2.0, 1e300), EPSILON);
        Assert.assertEquals(Math.pow(2.0, 3.5), FastMath.pow(2.0, 3.5), EPSILON);
    }

    @Test
    public void testSinCosTan() {
        Assert.assertEquals(0.0, FastMath.sin(0.0), 0.0);
        Assert.assertEquals(-0.0, FastMath.sin(-0.0), 0.0);
        Assert.assertTrue(Double.isNaN(FastMath.sin(Double.NaN)));
        Assert.assertTrue(Double.isNaN(FastMath.sin(Double.POSITIVE_INFINITY)));
        Assert.assertEquals(Math.sin(1.0), FastMath.sin(1.0), EPSILON);
        Assert.assertEquals(Math.sin(-1.0), FastMath.sin(-1.0), EPSILON);
        Assert.assertEquals(Math.sin(3.0), FastMath.sin(3.0), EPSILON);
        Assert.assertEquals(Math.sin(5.0), FastMath.sin(5.0), EPSILON);
        Assert.assertEquals(Math.sin(7.0), FastMath.sin(7.0), EPSILON);
        Assert.assertEquals(Math.sin(1e7), FastMath.sin(1e7), 1e-9);

        Assert.assertTrue(Double.isNaN(FastMath.cos(Double.NaN)));
        Assert.assertTrue(Double.isNaN(FastMath.cos(Double.POSITIVE_INFINITY)));
        Assert.assertEquals(Math.cos(1.0), FastMath.cos(1.0), EPSILON);
        Assert.assertEquals(Math.cos(-1.0), FastMath.cos(-1.0), EPSILON);
        Assert.assertEquals(Math.cos(3.0), FastMath.cos(3.0), EPSILON);
        Assert.assertEquals(Math.cos(5.0), FastMath.cos(5.0), EPSILON);
        Assert.assertEquals(Math.cos(7.0), FastMath.cos(7.0), EPSILON);
        Assert.assertEquals(Math.cos(1e7), FastMath.cos(1e7), 1e-9);

        Assert.assertEquals(0.0, FastMath.tan(0.0), 0.0);
        Assert.assertEquals(-0.0, FastMath.tan(-0.0), 0.0);
        Assert.assertTrue(Double.isNaN(FastMath.tan(Double.NaN)));
        Assert.assertTrue(Double.isNaN(FastMath.tan(Double.POSITIVE_INFINITY)));
        Assert.assertEquals(Math.tan(1.0), FastMath.tan(1.0), EPSILON);
        Assert.assertEquals(Math.tan(-1.0), FastMath.tan(-1.0), EPSILON);
        Assert.assertEquals(Math.tan(1.55), FastMath.tan(1.55), EPSILON);
        Assert.assertEquals(Math.tan(3.0), FastMath.tan(3.0), EPSILON);
        Assert.assertEquals(Math.tan(5.0), FastMath.tan(5.0), EPSILON);
        Assert.assertEquals(Math.tan(7.0), FastMath.tan(7.0), EPSILON);
        Assert.assertEquals(Math.tan(1e7), FastMath.tan(1e7), 1e-9);
    }

    @Test
    public void testAtan() {
        Assert.assertEquals(0.0, FastMath.atan(0.0), EPSILON);
        Assert.assertEquals(Math.atan(0.5), FastMath.atan(0.5), EPSILON);
        Assert.assertEquals(Math.atan(2.0), FastMath.atan(2.0), EPSILON);
        Assert.assertEquals(Math.atan(-2.0), FastMath.atan(-2.0), EPSILON);
        Assert.assertEquals(Math.PI / 2.0, FastMath.atan(2e16), EPSILON);
        Assert.assertEquals(-Math.PI / 2.0, FastMath.atan(-2e16), EPSILON);
    }

    @Test
    public void testAtan2() {
        Assert.assertTrue(Double.isNaN(FastMath.atan2(Double.NaN, 1.0)));
        Assert.assertTrue(Double.isNaN(FastMath.atan2(1.0, Double.NaN)));
        Assert.assertEquals(0.0, FastMath.atan2(0.0, 1.0), EPSILON);
        Assert.assertEquals(Math.PI, FastMath.atan2(0.0, -1.0), EPSILON);
        Assert.assertEquals(-Math.PI, FastMath.atan2(-0.0, -1.0), EPSILON);
        Assert.assertEquals(0.0, FastMath.atan2(0.0, Double.POSITIVE_INFINITY), EPSILON);
        Assert.assertEquals(Math.PI, FastMath.atan2(0.0, Double.NEGATIVE_INFINITY), EPSILON);
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
        Assert.assertEquals(Math.atan2(1e300, 2e300), FastMath.atan2(1e300, 2e300), EPSILON);
        Assert.assertEquals(Math.atan2(1.0, 2.0), FastMath.atan2(1.0, 2.0), EPSILON);
        Assert.assertEquals(Math.atan2(1.0, -2.0), FastMath.atan2(1.0, -2.0), EPSILON);
    }

    @Test
    public void testAsinAcos() {
        Assert.assertTrue(Double.isNaN(FastMath.asin(Double.NaN)));
        Assert.assertTrue(Double.isNaN(FastMath.asin(2.0)));
        Assert.assertTrue(Double.isNaN(FastMath.asin(-2.0)));
        Assert.assertEquals(Math.PI / 2.0, FastMath.asin(1.0), EPSILON);
        Assert.assertEquals(-Math.PI / 2.0, FastMath.asin(-1.0), EPSILON);
        Assert.assertEquals(Math.asin(0.5), FastMath.asin(0.5), EPSILON);

        Assert.assertTrue(Double.isNaN(FastMath.acos(Double.NaN)));
        Assert.assertTrue(Double.isNaN(FastMath.acos(2.0)));
        Assert.assertTrue(Double.isNaN(FastMath.acos(-2.0)));
        Assert.assertEquals(0.0, FastMath.acos(1.0), EPSILON);
        Assert.assertEquals(Math.PI, FastMath.acos(-1.0), EPSILON);
        Assert.assertEquals(Math.PI / 2.0, FastMath.acos(0.0), EPSILON);
        Assert.assertEquals(Math.acos(0.5), FastMath.acos(0.5), EPSILON);
        Assert.assertEquals(Math.acos(-0.5), FastMath.acos(-0.5), EPSILON);
    }

    @Test
    public void testCbrt() {
        Assert.assertEquals(0.0, FastMath.cbrt(0.0), 0.0);
        Assert.assertTrue(Double.isNaN(FastMath.cbrt(Double.NaN)));
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.cbrt(Double.POSITIVE_INFINITY), 0.0);
        Assert.assertEquals(2.0, FastMath.cbrt(8.0), EPSILON);
        Assert.assertEquals(-2.0, FastMath.cbrt(-8.0), EPSILON);
        Assert.assertEquals(Math.cbrt(1e-310), FastMath.cbrt(1e-310), 1e-105);
    }

    @Test
    public void testAngles() {
        Assert.assertEquals(Math.PI, FastMath.toRadians(180.0), EPSILON);
        Assert.assertEquals(180.0, FastMath.toDegrees(Math.PI), EPSILON);
    }

    @Test
    public void testAbs() {
        Assert.assertEquals(5, FastMath.abs(-5));
        Assert.assertEquals(5, FastMath.abs(5));
        Assert.assertEquals(5L, FastMath.abs(-5L));
        Assert.assertEquals(5L, FastMath.abs(5L));
        Assert.assertEquals(5.0f, FastMath.abs(-5.0f), 0.0f);
        Assert.assertEquals(5.0f, FastMath.abs(5.0f), 0.0f);
        Assert.assertEquals(5.0, FastMath.abs(-5.0), 0.0);
        Assert.assertEquals(5.0, FastMath.abs(5.0), 0.0);
    }

    @Test
    public void testUlpAndNextAfter() {
        Assert.assertEquals(Math.ulp(1.0), FastMath.ulp(1.0), 0.0);
        Assert.assertTrue(Double.isNaN(FastMath.nextAfter(Double.NaN, 1.0)));
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.nextAfter(Double.POSITIVE_INFINITY, 1.0), 0.0);
        Assert.assertEquals(Double.MIN_VALUE, FastMath.nextAfter(0.0, 1.0), 0.0);
        Assert.assertEquals(-Double.MIN_VALUE, FastMath.nextAfter(0.0, -1.0), 0.0);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.nextAfter(Double.MAX_VALUE, Double.POSITIVE_INFINITY), 0.0);
        Assert.assertEquals(1.0000000000000002, FastMath.nextAfter(1.0, 2.0), 0.0);
        Assert.assertEquals(Double.MAX_VALUE, FastMath.nextAfter(Double.POSITIVE_INFINITY, 0.0), 0.0);
        Assert.assertEquals(0.9999999999999999, FastMath.nextAfter(1.0, 0.0), 0.0);
        Assert.assertEquals(-0.9999999999999999, FastMath.nextAfter(-1.0, 0.0), 0.0);
    }

    @Test
    public void testFloorCeilRintRound() {
        Assert.assertTrue(Double.isNaN(FastMath.floor(Double.NaN)));
        Assert.assertEquals(5e15, FastMath.floor(5e15), 0.0);
        Assert.assertEquals(2.0, FastMath.floor(2.7), 0.0);
        Assert.assertEquals(-3.0, FastMath.floor(-2.7), 0.0);
        Assert.assertEquals(0.0, FastMath.floor(0.5), 0.0);
        Assert.assertEquals(-0.0, FastMath.floor(-0.5), 0.0);

        Assert.assertTrue(Double.isNaN(FastMath.ceil(Double.NaN)));
        Assert.assertEquals(2.0, FastMath.ceil(2.0), 0.0);
        Assert.assertEquals(3.0, FastMath.ceil(2.3), 0.0);
        Assert.assertEquals(-0.0, FastMath.ceil(-0.5), 0.0);

        Assert.assertEquals(2.0, FastMath.rint(2.3), 0.0);
        Assert.assertEquals(3.0, FastMath.rint(2.7), 0.0);
        Assert.assertEquals(2.0, FastMath.rint(2.5), 0.0);
        Assert.assertEquals(4.0, FastMath.rint(3.5), 0.0);

        Assert.assertEquals(3L, FastMath.round(2.6));
        Assert.assertEquals(-3L, FastMath.round(-2.6));
        Assert.assertEquals(3, FastMath.round(2.6f));
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
        Assert.assertEquals(2.0, FastMath.min(2.0, 5.0), 0.0);
        Assert.assertEquals(2.0, FastMath.min(5.0, 2.0), 0.0);
        Assert.assertTrue(Double.isNaN(FastMath.min(Double.NaN, 2.0)));

        Assert.assertEquals(5, FastMath.max(2, 5));
        Assert.assertEquals(5, FastMath.max(5, 2));
        Assert.assertEquals(5L, FastMath.max(2L, 5L));
        Assert.assertEquals(5L, FastMath.max(5L, 2L));
        Assert.assertEquals(5.0f, FastMath.max(2.0f, 5.0f), 0.0f);
        Assert.assertEquals(5.0f, FastMath.max(5.0f, 2.0f), 0.0f);
        Assert.assertTrue(Float.isNaN(FastMath.max(Float.NaN, 2.0f)));
        Assert.assertEquals(5.0, FastMath.max(2.0, 5.0), 0.0);
        Assert.assertEquals(5.0, FastMath.max(5.0, 2.0), 0.0);
        Assert.assertTrue(Double.isNaN(FastMath.max(Double.NaN, 2.0)));
    }
}
