package org.apache.commons.math.complex;

import org.apache.commons.math.TestUtils;
import org.apache.commons.math.exception.NotPositiveException;
import org.apache.commons.math.exception.NullArgumentException;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class ComplexTest {

    @Test
    public void testConstantsAndConstructors() {
        Complex z1 = new Complex(3.0);
        Assert.assertEquals(3.0, z1.getReal(), 1.0e-15);
        Assert.assertEquals(0.0, z1.getImaginary(), 1.0e-15);
        Assert.assertFalse(z1.isNaN());
        Assert.assertFalse(z1.isInfinite());

        Complex z2 = new Complex(0.0, 1.0);
        Assert.assertEquals(Complex.I, z2);

        Complex zZero = new Complex(0.0, 0.0);
        Assert.assertEquals(Complex.ZERO, zZero);

        Complex zOne = new Complex(1.0, 0.0);
        Assert.assertEquals(Complex.ONE, zOne);

        Complex zNan = new Complex(Double.NaN, 0.0);
        Assert.assertTrue(zNan.isNaN());
        Assert.assertFalse(zNan.isInfinite());

        Complex zInf = new Complex(Double.POSITIVE_INFINITY, 0.0);
        Assert.assertFalse(zInf.isNaN());
        Assert.assertTrue(zInf.isInfinite());

        Assert.assertNotNull(z1.getField());
        Assert.assertEquals("(3.0, 0.0)", z1.toString());
    }

    @Test
    public void testValueOf() {
        Complex z = Complex.valueOf(2.0, -3.0);
        Assert.assertEquals(2.0, z.getReal(), 1.0e-15);
        Assert.assertEquals(-3.0, z.getImaginary(), 1.0e-15);

        Assert.assertTrue(Complex.valueOf(Double.NaN, 1.0).isNaN());
        Assert.assertTrue(Complex.valueOf(1.0, Double.NaN).isNaN());
        Assert.assertTrue(Complex.valueOf(Double.NaN).isNaN());
        Assert.assertEquals(new Complex(5.0, 0.0), Complex.valueOf(5.0));
    }

    @Test
    public void testAbs() {
        Assert.assertTrue(Double.isNaN(Complex.NaN.abs()));
        Assert.assertTrue(Double.isNaN(new Complex(Double.NaN, 1.0).abs()));
        Assert.assertTrue(Double.isNaN(new Complex(1.0, Double.NaN).abs()));

        Assert.assertEquals(Double.POSITIVE_INFINITY, Complex.INF.abs(), 1.0e-15);
        Assert.assertEquals(Double.POSITIVE_INFINITY, new Complex(Double.NEGATIVE_INFINITY, 0.0).abs(), 1.0e-15);
        Assert.assertEquals(Double.POSITIVE_INFINITY, new Complex(0.0, Double.POSITIVE_INFINITY).abs(), 1.0e-15);

        Assert.assertEquals(5.0, new Complex(3.0, 4.0).abs(), 1.0e-15);
        Assert.assertEquals(5.0, new Complex(4.0, 3.0).abs(), 1.0e-15);
        Assert.assertEquals(0.0, new Complex(0.0, 0.0).abs(), 1.0e-15);
        Assert.assertEquals(3.0, new Complex(3.0, 0.0).abs(), 1.0e-15);
        Assert.assertEquals(4.0, new Complex(0.0, 4.0).abs(), 1.0e-15);
    }

    @Test
    public void testAdd() {
        Complex z1 = new Complex(1.0, 2.0);
        Complex z2 = new Complex(3.0, -4.0);
        Complex res = z1.add(z2);
        Assert.assertEquals(4.0, res.getReal(), 1.0e-15);
        Assert.assertEquals(-2.0, res.getImaginary(), 1.0e-15);

        Assert.assertEquals(Complex.NaN, z1.add(Complex.NaN));
        Assert.assertEquals(Complex.NaN, Complex.NaN.add(z1));

        Complex resD = z1.add(3.0);
        Assert.assertEquals(4.0, resD.getReal(), 1.0e-15);
        Assert.assertEquals(2.0, resD.getImaginary(), 1.0e-15);

        Assert.assertEquals(Complex.NaN, z1.add(Double.NaN));
        Assert.assertEquals(Complex.NaN, Complex.NaN.add(1.0));
    }

    @Test(expected = NullArgumentException.class)
    public void testAddNull() {
        Complex.ONE.add((Complex) null);
    }

    @Test
    public void testSubtract() {
        Complex z1 = new Complex(1.0, 2.0);
        Complex z2 = new Complex(3.0, -4.0);
        Complex res = z1.subtract(z2);
        Assert.assertEquals(-2.0, res.getReal(), 1.0e-15);
        Assert.assertEquals(6.0, res.getImaginary(), 1.0e-15);

        Assert.assertEquals(Complex.NaN, z1.subtract(Complex.NaN));
        Assert.assertEquals(Complex.NaN, Complex.NaN.subtract(z1));

        Complex resD = z1.subtract(1.0);
        Assert.assertEquals(0.0, resD.getReal(), 1.0e-15);
        Assert.assertEquals(2.0, resD.getImaginary(), 1.0e-15);

        Assert.assertEquals(Complex.NaN, z1.subtract(Double.NaN));
        Assert.assertEquals(Complex.NaN, Complex.NaN.subtract(1.0));
    }

    @Test(expected = NullArgumentException.class)
    public void testSubtractNull() {
        Complex.ONE.subtract((Complex) null);
    }

    @Test
    public void testConjugateAndNegate() {
        Complex z = new Complex(1.0, -2.0);
        Complex conj = z.conjugate();
        Assert.assertEquals(1.0, conj.getReal(), 1.0e-15);
        Assert.assertEquals(2.0, conj.getImaginary(), 1.0e-15);
        Assert.assertTrue(Complex.NaN.conjugate().isNaN());

        Complex neg = z.negate();
        Assert.assertEquals(-1.0, neg.getReal(), 1.0e-15);
        Assert.assertEquals(2.0, neg.getImaginary(), 1.0e-15);
        Assert.assertTrue(Complex.NaN.negate().isNaN());
    }

    @Test
    public void testMultiply() {
        Complex z1 = new Complex(2.0, 3.0);
        Complex z2 = new Complex(4.0, -5.0);
        Complex res = z1.multiply(z2);
        Assert.assertEquals(23.0, res.getReal(), 1.0e-15);
        Assert.assertEquals(2.0, res.getImaginary(), 1.0e-15);

        Assert.assertEquals(Complex.NaN, z1.multiply(Complex.NaN));
        Assert.assertEquals(Complex.NaN, Complex.NaN.multiply(z1));

        Assert.assertEquals(Complex.INF, z1.multiply(Complex.INF));
        Assert.assertEquals(Complex.INF, Complex.INF.multiply(z1));
        Assert.assertEquals(Complex.INF, new Complex(Double.POSITIVE_INFINITY, 0).multiply(z1));
        Assert.assertEquals(Complex.INF, new Complex(0, Double.POSITIVE_INFINITY).multiply(z1));
        Assert.assertEquals(Complex.INF, z1.multiply(new Complex(Double.POSITIVE_INFINITY, 0)));
        Assert.assertEquals(Complex.INF, z1.multiply(new Complex(0, Double.POSITIVE_INFINITY)));

        Complex resD = z1.multiply(2.5);
        Assert.assertEquals(5.0, resD.getReal(), 1.0e-15);
        Assert.assertEquals(7.5, resD.getImaginary(), 1.0e-15);

        Assert.assertEquals(Complex.NaN, z1.multiply(Double.NaN));
        Assert.assertEquals(Complex.NaN, Complex.NaN.multiply(2.5));
        Assert.assertEquals(Complex.INF, z1.multiply(Double.POSITIVE_INFINITY));
        Assert.assertEquals(Complex.INF, new Complex(Double.POSITIVE_INFINITY, 0.0).multiply(2.0));
        Assert.assertEquals(Complex.INF, new Complex(0.0, Double.POSITIVE_INFINITY).multiply(2.0));
    }

    @Test(expected = NullArgumentException.class)
    public void testMultiplyNull() {
        Complex.ONE.multiply((Complex) null);
    }

    @Test
    public void testDivide() {
        Complex z1 = new Complex(2.0, 3.0);
        Complex z2 = new Complex(4.0, -5.0);
        Complex res = z1.divide(z2);
        Assert.assertEquals(-7.0 / 41.0, res.getReal(), 1.0e-15);
        Assert.assertEquals(22.0 / 41.0, res.getImaginary(), 1.0e-15);

        Complex z3 = new Complex(4.0, 2.0);
        Complex z4 = new Complex(1.0, 3.0);
        Complex res2 = z3.divide(z4);
        Assert.assertEquals(1.0, res2.getReal(), 1.0e-15);
        Assert.assertEquals(-1.0, res2.getImaginary(), 1.0e-15);

        Assert.assertEquals(Complex.NaN, z1.divide(Complex.NaN));
        Assert.assertEquals(Complex.NaN, Complex.NaN.divide(z1));
        Assert.assertEquals(Complex.NaN, Complex.ZERO.divide(Complex.ZERO));
        Assert.assertEquals(Complex.INF, z1.divide(Complex.ZERO));
        Assert.assertEquals(Complex.ZERO, z1.divide(Complex.INF));

        Assert.assertEquals(Complex.NaN, z1.divide(Double.NaN));
        Assert.assertEquals(Complex.NaN, Complex.NaN.divide(2.0));
        Assert.assertEquals(Complex.NaN, Complex.ZERO.divide(0.0));
        Assert.assertEquals(Complex.INF, z1.divide(0.0));
        Assert.assertEquals(Complex.ZERO, z1.divide(Double.POSITIVE_INFINITY));
        Assert.assertEquals(Complex.NaN, Complex.INF.divide(Double.POSITIVE_INFINITY));

        Complex resD = new Complex(4.0, -6.0).divide(2.0);
        Assert.assertEquals(2.0, resD.getReal(), 1.0e-15);
        Assert.assertEquals(-3.0, resD.getImaginary(), 1.0e-15);
    }

    @Test(expected = NullArgumentException.class)
    public void testDivideNull() {
        Complex.ONE.divide((Complex) null);
    }

    @Test
    public void testEqualsAndHashCode() {
        Complex z1 = new Complex(1.0, 2.0);
        Complex z2 = new Complex(1.0, 2.0);
        Complex z3 = new Complex(1.0, 3.0);
        Complex z4 = new Complex(3.0, 2.0);

        Assert.assertTrue(z1.equals(z1));
        Assert.assertTrue(z1.equals(z2));
        Assert.assertFalse(z1.equals(z3));
        Assert.assertFalse(z1.equals(z4));
        Assert.assertFalse(z1.equals(null));
        Assert.assertFalse(z1.equals("String"));

        Assert.assertEquals(z1.hashCode(), z2.hashCode());

        Complex nan1 = new Complex(Double.NaN, 1.0);
        Complex nan2 = new Complex(1.0, Double.NaN);
        Assert.assertTrue(nan1.equals(nan2));
        Assert.assertTrue(nan2.equals(Complex.NaN));
        Assert.assertFalse(z1.equals(nan1));
        Assert.assertFalse(nan1.equals(z1));
        Assert.assertEquals(7, nan1.hashCode());
        Assert.assertEquals(7, Complex.NaN.hashCode());
    }

    @Test
    public void testTrigonometric() {
        Assert.assertTrue(Complex.NaN.cos().isNaN());
        Assert.assertTrue(Complex.NaN.sin().isNaN());
        Assert.assertTrue(Complex.NaN.tan().isNaN());

        Complex z = new Complex(1.0, 2.0);
        Complex cosZ = z.cos();
        Complex sinZ = z.sin();
        Complex tanZ = z.tan();

        Assert.assertEquals(new Complex(0.0, 0.0), Complex.ZERO.sin());
        Assert.assertEquals(new Complex(1.0, 0.0), Complex.ZERO.cos());
        Assert.assertEquals(new Complex(0.0, 0.0), Complex.ZERO.tan());

        Assert.assertEquals(sinZ.divide(cosZ).getReal(), tanZ.getReal(), 1.0e-14);
        Assert.assertEquals(sinZ.divide(cosZ).getImaginary(), tanZ.getImaginary(), 1.0e-14);
    }

    @Test
    public void testHyperbolic() {
        Assert.assertTrue(Complex.NaN.cosh().isNaN());
        Assert.assertTrue(Complex.NaN.sinh().isNaN());
        Assert.assertTrue(Complex.NaN.tanh().isNaN());

        Complex z = new Complex(1.0, 2.0);
        Complex coshZ = z.cosh();
        Complex sinhZ = z.sinh();
        Complex tanhZ = z.tanh();

        Assert.assertEquals(new Complex(0.0, 0.0), Complex.ZERO.sinh());
        Assert.assertEquals(new Complex(1.0, 0.0), Complex.ZERO.cosh());
        Assert.assertEquals(new Complex(0.0, 0.0), Complex.ZERO.tanh());

        Assert.assertEquals(sinhZ.divide(coshZ).getReal(), tanhZ.getReal(), 1.0e-14);
        Assert.assertEquals(sinhZ.divide(coshZ).getImaginary(), tanhZ.getImaginary(), 1.0e-14);
    }

    @Test
    public void testInverseTrigonometric() {
        Assert.assertTrue(Complex.NaN.acos().isNaN());
        Assert.assertTrue(Complex.NaN.asin().isNaN());
        Assert.assertTrue(Complex.NaN.atan().isNaN());

        Complex z = new Complex(0.5, -0.5);
        Assert.assertEquals(z.getReal(), z.acos().cos().getReal(), 1.0e-14);
        Assert.assertEquals(z.getImaginary(), z.acos().cos().getImaginary(), 1.0e-14);

        Assert.assertEquals(z.getReal(), z.asin().sin().getReal(), 1.0e-14);
        Assert.assertEquals(z.getImaginary(), z.asin().sin().getImaginary(), 1.0e-14);

        Assert.assertEquals(z.getReal(), z.atan().tan().getReal(), 1.0e-14);
        Assert.assertEquals(z.getImaginary(), z.atan().tan().getImaginary(), 1.0e-14);
    }

    @Test
    public void testExpLogPow() {
        Assert.assertTrue(Complex.NaN.exp().isNaN());
        Assert.assertTrue(Complex.NaN.log().isNaN());
        Assert.assertTrue(Complex.NaN.pow(Complex.ONE).isNaN());
        Assert.assertTrue(Complex.ONE.pow(Complex.NaN).isNaN());
        Assert.assertTrue(Complex.NaN.pow(2.0).isNaN());

        Complex z = new Complex(2.0, 1.0);
        Complex expZ = z.exp();
        Complex logZ = expZ.log();
        Assert.assertEquals(z.getReal(), logZ.getReal(), 1.0e-14);
        Assert.assertEquals(z.getImaginary(), logZ.getImaginary(), 1.0e-14);

        Complex powComplex = z.pow(new Complex(2.0, 0.0));
        Complex powDouble = z.pow(2.0);
        Complex mult = z.multiply(z);
        Assert.assertEquals(mult.getReal(), powComplex.getReal(), 1.0e-14);
        Assert.assertEquals(mult.getImaginary(), powComplex.getImaginary(), 1.0e-14);
        Assert.assertEquals(mult.getReal(), powDouble.getReal(), 1.0e-14);
        Assert.assertEquals(mult.getImaginary(), powDouble.getImaginary(), 1.0e-14);
    }

    @Test(expected = NullArgumentException.class)
    public void testPowNull() {
        Complex.ONE.pow(null);
    }

    @Test
    public void testSqrt() {
        Assert.assertTrue(Complex.NaN.sqrt().isNaN());
        Assert.assertEquals(Complex.ZERO, Complex.ZERO.sqrt());

        Complex zPos = new Complex(3.0, 4.0);
        Complex sqrtPos = zPos.sqrt();
        Assert.assertEquals(2.0, sqrtPos.getReal(), 1.0e-15);
        Assert.assertEquals(1.0, sqrtPos.getImaginary(), 1.0e-15);

        Complex zNeg = new Complex(-3.0, 4.0);
        Complex sqrtNeg = zNeg.sqrt();
        Assert.assertEquals(1.0, sqrtNeg.getReal(), 1.0e-15);
        Assert.assertEquals(2.0, sqrtNeg.getImaginary(), 1.0e-15);

        Complex zNegIm = new Complex(-3.0, -4.0);
        Complex sqrtNegIm = zNegIm.sqrt();
        Assert.assertEquals(1.0, sqrtNegIm.getReal(), 1.0e-15);
        Assert.assertEquals(-2.0, sqrtNegIm.getImaginary(), 1.0e-15);

        Assert.assertTrue(Complex.NaN.sqrt1z().isNaN());
        Complex s1z = new Complex(0.5, 0.0).sqrt1z();
        Assert.assertEquals(Math.sqrt(0.75), s1z.getReal(), 1.0e-15);
    }

    @Test
    public void testGetArgument() {
        Assert.assertTrue(Double.isNaN(Complex.NaN.getArgument()));
        Assert.assertEquals(0.0, Complex.ONE.getArgument(), 1.0e-15);
        Assert.assertEquals(Math.PI / 2.0, Complex.I.getArgument(), 1.0e-15);
        Assert.assertEquals(Math.PI, new Complex(-1.0, 0.0).getArgument(), 1.0e-15);
        Assert.assertEquals(-Math.PI / 2.0, new Complex(0.0, -1.0).getArgument(), 1.0e-15);
    }

    @Test
    public void testNthRoot() {
        List<Complex> rootsNan = Complex.NaN.nthRoot(3);
        Assert.assertEquals(1, rootsNan.size());
        Assert.assertTrue(rootsNan.get(0).isNaN());

        List<Complex> rootsInf = Complex.INF.nthRoot(3);
        Assert.assertEquals(1, rootsInf.size());
        Assert.assertTrue(rootsInf.get(0).isInfinite());

        List<Complex> roots = Complex.ONE.nthRoot(4);
        Assert.assertEquals(4, roots.size());
        for (Complex root : roots) {
            Complex pow4 = root.multiply(root).multiply(root).multiply(root);
            Assert.assertEquals(1.0, pow4.getReal(), 1.0e-14);
            Assert.assertEquals(0.0, pow4.getImaginary(), 1.0e-14);
        }
    }

    @Test(expected = NotPositiveException.class)
    public void testNthRootZeroOrNegative() {
        Complex.ONE.nthRoot(0);
    }

    @Test(expected = NotPositiveException.class)
    public void testNthRootNegative() {
        Complex.ONE.nthRoot(-2);
    }

    @Test
    public void testSerialization() {
        Complex z = new Complex(3.0, 4.0);
        Complex deserialized = (Complex) TestUtils.serializeAndRecover(z);
        Assert.assertEquals(z, deserialized);
        Assert.assertEquals(z.hashCode(), deserialized.hashCode());
        Assert.assertEquals(z.abs(), deserialized.abs(), 1.0e-15);
    }
}
