package org.apache.commons.math.complex;

import org.junit.Assert;
import org.junit.Test;

public class ComplexTest {

    private static final double EPS = 1e-10;

    @Test
    public void testConstants() {
        Assert.assertEquals(0.0, Complex.I.getReal(), EPS);
        Assert.assertEquals(1.0, Complex.I.getImaginary(), EPS);
        Assert.assertTrue(Complex.NaN.isNaN());
        Assert.assertTrue(Complex.INF.isInfinite());
        Assert.assertEquals(1.0, Complex.ONE.getReal(), EPS);
        Assert.assertEquals(0.0, Complex.ONE.getImaginary(), EPS);
        Assert.assertEquals(0.0, Complex.ZERO.getReal(), EPS);
        Assert.assertEquals(0.0, Complex.ZERO.getImaginary(), EPS);
    }

    @Test
    public void testGettersAndNaNInfinite() {
        Complex c = new Complex(3.0, 4.0);
        Assert.assertEquals(3.0, c.getReal(), EPS);
        Assert.assertEquals(4.0, c.getImaginary(), EPS);
        Assert.assertFalse(c.isNaN());
        Assert.assertFalse(c.isInfinite());

        Complex nanReal = new Complex(Double.NaN, 1.0);
        Assert.assertTrue(nanReal.isNaN());
        Assert.assertFalse(nanReal.isInfinite());

        Complex nanImag = new Complex(1.0, Double.NaN);
        Assert.assertTrue(nanImag.isNaN());
        Assert.assertFalse(nanImag.isInfinite());

        Complex infReal = new Complex(Double.POSITIVE_INFINITY, 1.0);
        Assert.assertFalse(infReal.isNaN());
        Assert.assertTrue(infReal.isInfinite());

        Complex infImag = new Complex(1.0, Double.NEGATIVE_INFINITY);
        Assert.assertFalse(infImag.isNaN());
        Assert.assertTrue(infImag.isInfinite());

        Complex infNan = new Complex(Double.POSITIVE_INFINITY, Double.NaN);
        Assert.assertTrue(infNan.isNaN());
        Assert.assertFalse(infNan.isInfinite());
    }

    @Test
    public void testAbs() {
        Assert.assertTrue(Double.isNaN(Complex.NaN.abs()));
        Assert.assertEquals(Double.POSITIVE_INFINITY, Complex.INF.abs(), EPS);
        Assert.assertEquals(5.0, new Complex(3.0, 4.0).abs(), EPS);
        Assert.assertEquals(5.0, new Complex(4.0, 3.0).abs(), EPS);
        Assert.assertEquals(0.0, new Complex(0.0, 0.0).abs(), EPS);
        Assert.assertEquals(3.0, new Complex(3.0, 0.0).abs(), EPS);
        Assert.assertEquals(4.0, new Complex(0.0, 4.0).abs(), EPS);
    }

    @Test
    public void testAdd() {
        Complex a = new Complex(1.0, 2.0);
        Complex b = new Complex(3.0, 4.0);
        Complex res = a.add(b);
        Assert.assertEquals(4.0, res.getReal(), EPS);
        Assert.assertEquals(6.0, res.getImaginary(), EPS);

        Assert.assertTrue(a.add(Complex.NaN).isNaN());
    }

    @Test(expected = NullPointerException.class)
    public void testAddNull() {
        new Complex(1.0, 1.0).add(null);
    }

    @Test
    public void testConjugate() {
        Complex c = new Complex(3.0, 4.0);
        Complex conj = c.conjugate();
        Assert.assertEquals(3.0, conj.getReal(), EPS);
        Assert.assertEquals(-4.0, conj.getImaginary(), EPS);

        Assert.assertTrue(Complex.NaN.conjugate().isNaN());
    }

    @Test
    public void testDivide() {
        Complex a = new Complex(3.0, 2.0);
        Complex b = new Complex(4.0, -3.0);
        Complex res = a.divide(b);
        Assert.assertEquals(6.0 / 25.0, res.getReal(), EPS);
        Assert.assertEquals(17.0 / 25.0, res.getImaginary(), EPS);

        Complex c = new Complex(1.0, 2.0);
        Complex d = new Complex(3.0, 4.0);
        Complex res2 = c.divide(d);
        Assert.assertEquals(11.0 / 25.0, res2.getReal(), EPS);
        Assert.assertEquals(2.0 / 25.0, res2.getImaginary(), EPS);

        Assert.assertTrue(a.divide(Complex.NaN).isNaN());
        Assert.assertTrue(Complex.NaN.divide(a).isNaN());
        Assert.assertTrue(a.divide(Complex.ZERO).isNaN());

        Assert.assertEquals(Complex.ZERO, a.divide(Complex.INF));
        Assert.assertTrue(Complex.INF.divide(Complex.INF).isNaN());
    }

    @Test(expected = NullPointerException.class)
    public void testDivideNull() {
        new Complex(1.0, 1.0).divide(null);
    }

    @Test
    public void testMultiply() {
        Complex a = new Complex(3.0, 2.0);
        Complex b = new Complex(4.0, -3.0);
        Complex res = a.multiply(b);
        Assert.assertEquals(18.0, res.getReal(), EPS);
        Assert.assertEquals(-1.0, res.getImaginary(), EPS);

        Assert.assertTrue(a.multiply(Complex.NaN).isNaN());
        Assert.assertTrue(Complex.NaN.multiply(a).isNaN());
        Assert.assertTrue(a.multiply(Complex.INF).isInfinite());
        Assert.assertTrue(Complex.INF.multiply(a).isInfinite());
        Assert.assertTrue(new Complex(Double.POSITIVE_INFINITY, 0.0).multiply(new Complex(1.0, 1.0)).isInfinite());
        Assert.assertTrue(new Complex(0.0, Double.POSITIVE_INFINITY).multiply(new Complex(1.0, 1.0)).isInfinite());
        Assert.assertTrue(new Complex(1.0, 1.0).multiply(new Complex(Double.POSITIVE_INFINITY, 0.0)).isInfinite());
        Assert.assertTrue(new Complex(1.0, 1.0).multiply(new Complex(0.0, Double.POSITIVE_INFINITY)).isInfinite());
    }

    @Test(expected = NullPointerException.class)
    public void testMultiplyNull() {
        new Complex(1.0, 1.0).multiply(null);
    }

    @Test
    public void testNegate() {
        Complex a = new Complex(3.0, -4.0);
        Complex neg = a.negate();
        Assert.assertEquals(-3.0, neg.getReal(), EPS);
        Assert.assertEquals(4.0, neg.getImaginary(), EPS);
        Assert.assertTrue(Complex.NaN.negate().isNaN());
    }

    @Test
    public void testSubtract() {
        Complex a = new Complex(3.0, 2.0);
        Complex b = new Complex(1.0, 4.0);
        Complex res = a.subtract(b);
        Assert.assertEquals(2.0, res.getReal(), EPS);
        Assert.assertEquals(-2.0, res.getImaginary(), EPS);

        Assert.assertTrue(a.subtract(Complex.NaN).isNaN());
        Assert.assertTrue(Complex.NaN.subtract(a).isNaN());
    }

    @Test(expected = NullPointerException.class)
    public void testSubtractNull() {
        new Complex(1.0, 1.0).subtract(null);
    }

    @Test
    public void testEqualsAndHashCode() {
        Complex a = new Complex(1.0, 2.0);
        Complex b = new Complex(1.0, 2.0);
        Complex c = new Complex(1.0, 3.0);
        Complex d = new Complex(2.0, 2.0);

        Assert.assertTrue(a.equals(a));
        Assert.assertTrue(a.equals(b));
        Assert.assertFalse(a.equals(c));
        Assert.assertFalse(a.equals(d));
        Assert.assertFalse(a.equals(null));
        Assert.assertFalse(a.equals("Not a complex"));

        Assert.assertEquals(a.hashCode(), b.hashCode());

        Complex nan1 = new Complex(Double.NaN, 1.0);
        Complex nan2 = new Complex(1.0, Double.NaN);
        Assert.assertTrue(nan1.equals(nan2));
        Assert.assertTrue(nan1.equals(Complex.NaN));
        Assert.assertFalse(a.equals(nan1));
        Assert.assertEquals(Complex.NaN.hashCode(), nan1.hashCode());
        Assert.assertEquals(7, Complex.NaN.hashCode());
    }

    @Test
    public void testAcos() {
        Assert.assertTrue(Complex.NaN.acos().isNaN());
        Complex c = new Complex(0.5, 0.5);
        Complex res = c.acos();
        Assert.assertEquals(1.118517879643706, res.getReal(), EPS);
        Assert.assertEquals(-0.5306375309525178, res.getImaginary(), EPS);
    }

    @Test
    public void testAsin() {
        Assert.assertTrue(Complex.NaN.asin().isNaN());
        Complex c = new Complex(0.5, 0.5);
        Complex res = c.asin();
        Assert.assertEquals(0.45227844715119045, res.getReal(), EPS);
        Assert.assertEquals(0.5306375309525178, res.getImaginary(), EPS);
    }

    @Test
    public void testAtan() {
        Assert.assertTrue(Complex.NaN.atan().isNaN());
        Complex c = new Complex(0.5, 0.5);
        Complex res = c.atan();
        Assert.assertEquals(0.4636476090008061, res.getReal(), EPS);
        Assert.assertEquals(0.40235947810852507, res.getImaginary(), EPS);
    }

    @Test
    public void testCosAndCosh() {
        Assert.assertTrue(Complex.NaN.cos().isNaN());
        Assert.assertTrue(Complex.NaN.cosh().isNaN());

        Complex c = new Complex(1.0, 2.0);
        Complex cosRes = c.cos();
        Assert.assertEquals(2.0327230070196656, cosRes.getReal(), EPS);
        Assert.assertEquals(-3.0518977991517997, cosRes.getImaginary(), EPS);

        Complex coshRes = c.cosh();
        Assert.assertEquals(-0.6421481235402096, coshRes.getReal(), EPS);
        Assert.assertEquals(1.0686074213827783, coshRes.getImaginary(), EPS);
    }

    @Test
    public void testExpAndLog() {
        Assert.assertTrue(Complex.NaN.exp().isNaN());
        Assert.assertTrue(Complex.NaN.log().isNaN());

        Complex c = new Complex(1.0, 2.0);
        Complex expRes = c.exp();
        Assert.assertEquals(Math.E * Math.cos(2.0), expRes.getReal(), EPS);
        Assert.assertEquals(Math.E * Math.sin(2.0), expRes.getImaginary(), EPS);

        Complex logRes = c.log();
        Assert.assertEquals(Math.log(Math.sqrt(5.0)), logRes.getReal(), EPS);
        Assert.assertEquals(Math.atan2(2.0, 1.0), logRes.getImaginary(), EPS);
    }

    @Test
    public void testPow() {
        Complex a = new Complex(2.0, 1.0);
        Complex b = new Complex(3.0, 4.0);
        Complex powRes = a.pow(b);
        Assert.assertFalse(powRes.isNaN());

        Assert.assertTrue(Complex.NaN.pow(a).isNaN());
        Assert.assertTrue(a.pow(Complex.NaN).isNaN());
    }

    @Test(expected = NullPointerException.class)
    public void testPowNull() {
        new Complex(1.0, 1.0).pow(null);
    }

    @Test
    public void testSinAndSinh() {
        Assert.assertTrue(Complex.NaN.sin().isNaN());
        Assert.assertTrue(Complex.NaN.sinh().isNaN());

        Complex c = new Complex(1.0, 2.0);
        Complex sinRes = c.sin();
        Assert.assertEquals(3.165778513216168, sinRes.getReal(), EPS);
        Assert.assertEquals(1.959601041421606, sinRes.getImaginary(), EPS);

        Complex sinhRes = c.sinh();
        Assert.assertEquals(-0.4890562590412937, sinhRes.getReal(), EPS);
        Assert.assertEquals(1.4031192506220405, sinhRes.getImaginary(), EPS);
    }

    @Test
    public void testSqrtAndSqrt1z() {
        Assert.assertTrue(Complex.NaN.sqrt().isNaN());
        Assert.assertTrue(Complex.NaN.sqrt1z().isNaN());

        Complex zeroSqrt = Complex.ZERO.sqrt();
        Assert.assertEquals(0.0, zeroSqrt.getReal(), EPS);
        Assert.assertEquals(0.0, zeroSqrt.getImaginary(), EPS);

        Complex posReal = new Complex(4.0, 3.0).sqrt();
        Assert.assertEquals(2.0, posReal.getReal(), 0.2);

        Complex negReal = new Complex(-4.0, 3.0).sqrt();
        Assert.assertTrue(negReal.getReal() > 0);

        Complex negRealNegImag = new Complex(-4.0, -3.0).sqrt();
        Assert.assertTrue(negRealNegImag.getImaginary() < 0);

        Complex sqrt1z = new Complex(0.5, 0.5).sqrt1z();
        Assert.assertFalse(sqrt1z.isNaN());
    }

    @Test
    public void testTanAndTanh() {
        Assert.assertTrue(Complex.NaN.tan().isNaN());
        Assert.assertTrue(Complex.NaN.tanh().isNaN());

        Complex c = new Complex(1.0, 2.0);
        Complex tanRes = c.tan();
        Assert.assertFalse(tanRes.isNaN());

        Complex tanhRes = c.tanh();
        Assert.assertFalse(tanhRes.isNaN());
    }

    @Test
    public void testCreateComplex() {
        Complex c = new Complex(1.0, 2.0);
        Complex created = c.createComplex(3.0, 4.0);
        Assert.assertEquals(3.0, created.getReal(), EPS);
        Assert.assertEquals(4.0, created.getImaginary(), EPS);
    }
}
