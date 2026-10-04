package org.apache.commons.math.complex;

import org.apache.commons.math.TestUtils;
import org.apache.commons.math.exception.NullArgumentException;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class ComplexTest {

    private static final double EPS = 1e-12;

    @Test
    public void testConstants() {
        Assert.assertEquals(0.0, Complex.I.getReal(), EPS);
        Assert.assertEquals(1.0, Complex.I.getImaginary(), EPS);

        Assert.assertTrue(Complex.NaN.isNaN());
        Assert.assertTrue(Double.isNaN(Complex.NaN.getReal()));
        Assert.assertTrue(Double.isNaN(Complex.NaN.getImaginary()));

        Assert.assertTrue(Complex.INF.isInfinite());
        Assert.assertEquals(Double.POSITIVE_INFINITY, Complex.INF.getReal(), EPS);
        Assert.assertEquals(Double.POSITIVE_INFINITY, Complex.INF.getImaginary(), EPS);

        Assert.assertEquals(1.0, Complex.ONE.getReal(), EPS);
        Assert.assertEquals(0.0, Complex.ONE.getImaginary(), EPS);

        Assert.assertEquals(0.0, Complex.ZERO.getReal(), EPS);
        Assert.assertEquals(0.0, Complex.ZERO.getImaginary(), EPS);
    }

    @Test
    public void testConstructorAndPredicates() {
        Complex c1 = new Complex(2.0, 3.0);
        Assert.assertEquals(2.0, c1.getReal(), EPS);
        Assert.assertEquals(3.0, c1.getImaginary(), EPS);
        Assert.assertFalse(c1.isNaN());
        Assert.assertFalse(c1.isInfinite());

        Complex cNan1 = new Complex(Double.NaN, 1.0);
        Assert.assertTrue(cNan1.isNaN());
        Assert.assertFalse(cNan1.isInfinite());

        Complex cNan2 = new Complex(1.0, Double.NaN);
        Assert.assertTrue(cNan2.isNaN());
        Assert.assertFalse(cNan2.isInfinite());

        Complex cInf1 = new Complex(Double.POSITIVE_INFINITY, 1.0);
        Assert.assertFalse(cInf1.isNaN());
        Assert.assertTrue(cInf1.isInfinite());

        Complex cInf2 = new Complex(1.0, Double.NEGATIVE_INFINITY);
        Assert.assertFalse(cInf2.isNaN());
        Assert.assertTrue(cInf2.isInfinite());

        Complex cInfNan = new Complex(Double.POSITIVE_INFINITY, Double.NaN);
        Assert.assertTrue(cInfNan.isNaN());
        Assert.assertFalse(cInfNan.isInfinite());
    }

    @Test
    public void testAbs() {
        Assert.assertTrue(Double.isNaN(Complex.NaN.abs()));
        Assert.assertTrue(Double.isNaN(new Complex(Double.NaN, 0.0).abs()));

        Assert.assertEquals(Double.POSITIVE_INFINITY, Complex.INF.abs(), EPS);
        Assert.assertEquals(Double.POSITIVE_INFINITY, new Complex(Double.NEGATIVE_INFINITY, 0.0).abs(), EPS);
        Assert.assertEquals(Double.POSITIVE_INFINITY, new Complex(0.0, Double.POSITIVE_INFINITY).abs(), EPS);

        Assert.assertEquals(0.0, Complex.ZERO.abs(), EPS);
        Assert.assertEquals(3.0, new Complex(3.0, 0.0).abs(), EPS);
        Assert.assertEquals(3.0, new Complex(-3.0, 0.0).abs(), EPS);
        Assert.assertEquals(4.0, new Complex(0.0, 4.0).abs(), EPS);
        Assert.assertEquals(4.0, new Complex(0.0, -4.0).abs(), EPS);

        Assert.assertEquals(5.0, new Complex(3.0, 4.0).abs(), EPS);
        Assert.assertEquals(5.0, new Complex(4.0, 3.0).abs(), EPS);
    }

    @Test
    public void testAdd() {
        Complex x = new Complex(3.0, 4.0);
        Complex y = new Complex(5.0, 6.0);
        Complex z = x.add(y);
        Assert.assertEquals(8.0, z.getReal(), EPS);
        Assert.assertEquals(10.0, z.getImaginary(), EPS);

        Assert.assertTrue(x.add(Complex.NaN).isNaN());
        Assert.assertTrue(Complex.NaN.add(x).isNaN());
    }

    @Test(expected = NullArgumentException.class)
    public void testAddNull() {
        Complex.ONE.add(null);
    }

    @Test
    public void testConjugate() {
        Complex x = new Complex(3.0, 4.0);
        Complex z = x.conjugate();
        Assert.assertEquals(3.0, z.getReal(), EPS);
        Assert.assertEquals(-4.0, z.getImaginary(), EPS);

        Assert.assertTrue(Complex.NaN.conjugate().isNaN());

        Complex infConj = new Complex(1.0, Double.POSITIVE_INFINITY).conjugate();
        Assert.assertEquals(1.0, infConj.getReal(), EPS);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, infConj.getImaginary(), EPS);
    }

    @Test
    public void testDivide() {
        Complex x = new Complex(3.0, 2.0);
        Complex y = new Complex(1.0, -2.0);
        Complex z = x.divide(y);
        Assert.assertEquals(-0.2, z.getReal(), EPS);
        Assert.assertEquals(1.6, z.getImaginary(), EPS);

        Complex z2 = y.divide(x);
        Assert.assertEquals(-1.0 / 13.0, z2.getReal(), EPS);
        Assert.assertEquals(-8.0 / 13.0, z2.getImaginary(), EPS);

        Assert.assertTrue(x.divide(Complex.NaN).isNaN());
        Assert.assertTrue(Complex.NaN.divide(x).isNaN());
        Assert.assertTrue(x.divide(Complex.ZERO).isNaN());
        Assert.assertTrue(Complex.INF.divide(Complex.INF).isNaN());

        Complex finiteDividedByInf = x.divide(Complex.INF);
        Assert.assertEquals(0.0, finiteDividedByInf.getReal(), EPS);
        Assert.assertEquals(0.0, finiteDividedByInf.getImaginary(), EPS);

        Complex infDividedByFinite = Complex.INF.divide(x);
        Assert.assertTrue(infDividedByFinite.isNaN() || infDividedByFinite.isInfinite());
    }

    @Test(expected = NullArgumentException.class)
    public void testDivideNull() {
        Complex.ONE.divide(null);
    }

    @Test
    public void testEqualsAndHashCode() {
        Complex x = new Complex(3.0, 4.0);
        Complex y = new Complex(3.0, 4.0);
        Complex z = new Complex(3.0, 5.0);
        Complex w = new Complex(4.0, 4.0);

        Assert.assertTrue(x.equals(x));
        Assert.assertTrue(x.equals(y));
        Assert.assertEquals(x.hashCode(), y.hashCode());

        Assert.assertFalse(x.equals(null));
        Assert.assertFalse(x.equals("String"));
        Assert.assertFalse(x.equals(z));
        Assert.assertFalse(x.equals(w));

        Complex nan1 = new Complex(Double.NaN, 1.0);
        Complex nan2 = new Complex(1.0, Double.NaN);
        Assert.assertTrue(nan1.equals(nan2));
        Assert.assertTrue(nan1.equals(Complex.NaN));
        Assert.assertEquals(nan1.hashCode(), Complex.NaN.hashCode());
        Assert.assertFalse(x.equals(nan1));
        Assert.assertFalse(nan1.equals(x));
    }

    @Test
    public void testMultiplyComplex() {
        Complex x = new Complex(3.0, 4.0);
        Complex y = new Complex(-2.0, 5.0);
        Complex z = x.multiply(y);
        Assert.assertEquals(-26.0, z.getReal(), EPS);
        Assert.assertEquals(7.0, z.getImaginary(), EPS);

        Assert.assertTrue(x.multiply(Complex.NaN).isNaN());
        Assert.assertTrue(Complex.NaN.multiply(x).isNaN());

        Complex infResult = x.multiply(Complex.INF);
        Assert.assertTrue(infResult.isInfinite());
        Assert.assertEquals(Complex.INF, infResult);

        Complex infResult2 = Complex.INF.multiply(x);
        Assert.assertTrue(infResult2.isInfinite());
    }

    @Test(expected = NullArgumentException.class)
    public void testMultiplyComplexNull() {
        Complex.ONE.multiply((Complex) null);
    }

    @Test
    public void testMultiplyDouble() {
        Complex x = new Complex(3.0, 4.0);
        Complex z = x.multiply(2.5);
        Assert.assertEquals(7.5, z.getReal(), EPS);
        Assert.assertEquals(10.0, z.getImaginary(), EPS);

        Assert.assertTrue(x.multiply(Double.NaN).isNaN());
        Assert.assertTrue(Complex.NaN.multiply(2.5).isNaN());

        Assert.assertEquals(Complex.INF, x.multiply(Double.POSITIVE_INFINITY));
        Assert.assertEquals(Complex.INF, new Complex(Double.POSITIVE_INFINITY, 0).multiply(2.0));
        Assert.assertEquals(Complex.INF, new Complex(0, Double.NEGATIVE_INFINITY).multiply(2.0));
    }

    @Test
    public void testNegate() {
        Complex x = new Complex(3.0, -4.0);
        Complex z = x.negate();
        Assert.assertEquals(-3.0, z.getReal(), EPS);
        Assert.assertEquals(4.0, z.getImaginary(), EPS);

        Assert.assertTrue(Complex.NaN.negate().isNaN());
    }

    @Test
    public void testSubtract() {
        Complex x = new Complex(3.0, 4.0);
        Complex y = new Complex(5.0, 2.0);
        Complex z = x.subtract(y);
        Assert.assertEquals(-2.0, z.getReal(), EPS);
        Assert.assertEquals(2.0, z.getImaginary(), EPS);

        Assert.assertTrue(x.subtract(Complex.NaN).isNaN());
        Assert.assertTrue(Complex.NaN.subtract(x).isNaN());
    }

    @Test(expected = NullArgumentException.class)
    public void testSubtractNull() {
        Complex.ONE.subtract(null);
    }

    @Test
    public void testAcos() {
        Assert.assertTrue(Complex.NaN.acos().isNaN());
        Complex z = new Complex(0.5, 0.5).acos();
        Assert.assertEquals(1.118517879643706, z.getReal(), 1e-10);
        Assert.assertEquals(-0.5306375309525178, z.getImaginary(), 1e-10);
    }

    @Test
    public void testAsin() {
        Assert.assertTrue(Complex.NaN.asin().isNaN());
        Complex z = new Complex(0.5, 0.5).asin();
        Assert.assertEquals(0.4522784471511905, z.getReal(), 1e-10);
        Assert.assertEquals(0.5306375309525178, z.getImaginary(), 1e-10);
    }

    @Test
    public void testAtan() {
        Assert.assertTrue(Complex.NaN.atan().isNaN());
        Complex z = new Complex(0.5, 0.5).atan();
        Assert.assertEquals(0.4636476090008061, z.getReal(), 1e-10);
        Assert.assertEquals(0.4023594781085251, z.getImaginary(), 1e-10);
    }

    @Test
    public void testCos() {
        Assert.assertTrue(Complex.NaN.cos().isNaN());
        Complex z = new Complex(1.0, 1.0).cos();
        Assert.assertEquals(0.8337300251311491, z.getReal(), 1e-10);
        Assert.assertEquals(-0.9888977057628651, z.getImaginary(), 1e-10);
    }

    @Test
    public void testCosh() {
        Assert.assertTrue(Complex.NaN.cosh().isNaN());
        Complex z = new Complex(1.0, 1.0).cosh();
        Assert.assertEquals(0.8337300251311491, z.getReal(), 1e-10);
        Assert.assertEquals(0.9888977057628651, z.getImaginary(), 1e-10);
    }

    @Test
    public void testExp() {
        Assert.assertTrue(Complex.NaN.exp().isNaN());
        Complex z = new Complex(1.0, Math.PI).exp();
        Assert.assertEquals(-Math.E, z.getReal(), 1e-10);
        Assert.assertEquals(0.0, z.getImaginary(), 1e-10);
    }

    @Test
    public void testLog() {
        Assert.assertTrue(Complex.NaN.log().isNaN());
        Complex z = new Complex(0.0, Math.E).log();
        Assert.assertEquals(1.0, z.getReal(), 1e-10);
        Assert.assertEquals(Math.PI / 2.0, z.getImaginary(), 1e-10);
    }

    @Test
    public void testPow() {
        Complex x = new Complex(2.0, 0.0);
        Complex y = new Complex(3.0, 0.0);
        Complex z = x.pow(y);
        Assert.assertEquals(8.0, z.getReal(), 1e-10);
        Assert.assertEquals(0.0, z.getImaginary(), 1e-10);

        Assert.assertTrue(x.pow(Complex.NaN).isNaN());
        Assert.assertTrue(Complex.NaN.pow(x).isNaN());
    }

    @Test(expected = NullArgumentException.class)
    public void testPowNull() {
        Complex.ONE.pow(null);
    }

    @Test
    public void testSin() {
        Assert.assertTrue(Complex.NaN.sin().isNaN());
        Complex z = new Complex(1.0, 1.0).sin();
        Assert.assertEquals(1.2984575814159773, z.getReal(), 1e-10);
        Assert.assertEquals(0.6349639147847361, z.getImaginary(), 1e-10);
    }

    @Test
    public void testSinh() {
        Assert.assertTrue(Complex.NaN.sinh().isNaN());
        Complex z = new Complex(1.0, 1.0).sinh();
        Assert.assertEquals(0.6349639147847361, z.getReal(), 1e-10);
        Assert.assertEquals(1.2984575814159773, z.getImaginary(), 1e-10);
    }

    @Test
    public void testSqrt() {
        Assert.assertTrue(Complex.NaN.sqrt().isNaN());

        Complex zeroSqrt = Complex.ZERO.sqrt();
        Assert.assertEquals(0.0, zeroSqrt.getReal(), EPS);
        Assert.assertEquals(0.0, zeroSqrt.getImaginary(), EPS);

        Complex zPos = new Complex(3.0, 4.0).sqrt();
        Assert.assertEquals(2.0, zPos.getReal(), 1e-10);
        Assert.assertEquals(1.0, zPos.getImaginary(), 1e-10);

        Complex zNeg = new Complex(-3.0, 4.0).sqrt();
        Assert.assertEquals(1.0, zNeg.getReal(), 1e-10);
        Assert.assertEquals(2.0, zNeg.getImaginary(), 1e-10);

        Complex zNegImag = new Complex(-3.0, -4.0).sqrt();
        Assert.assertEquals(1.0, zNegImag.getReal(), 1e-10);
        Assert.assertEquals(-2.0, zNegImag.getImaginary(), 1e-10);
    }

    @Test
    public void testSqrt1z() {
        Complex z = new Complex(3.0, 4.0).sqrt1z();
        Complex expected = Complex.ONE.subtract(new Complex(3.0, 4.0).multiply(new Complex(3.0, 4.0))).sqrt();
        Assert.assertEquals(expected.getReal(), z.getReal(), 1e-10);
        Assert.assertEquals(expected.getImaginary(), z.getImaginary(), 1e-10);
    }

    @Test
    public void testTan() {
        Assert.assertTrue(Complex.NaN.tan().isNaN());
        Complex z = new Complex(1.0, 1.0).tan();
        Assert.assertEquals(0.27175258531951174, z.getReal(), 1e-10);
        Assert.assertEquals(1.0839233273386946, z.getImaginary(), 1e-10);
    }

    @Test
    public void testTanh() {
        Assert.assertTrue(Complex.NaN.tanh().isNaN());
        Complex z = new Complex(1.0, 1.0).tanh();
        Assert.assertEquals(1.0839233273386946, z.getReal(), 1e-10);
        Assert.assertEquals(0.27175258531951174, z.getImaginary(), 1e-10);
    }

    @Test
    public void testGetArgument() {
        Assert.assertEquals(0.0, new Complex(1.0, 0.0).getArgument(), EPS);
        Assert.assertEquals(Math.PI / 2.0, new Complex(0.0, 1.0).getArgument(), EPS);
        Assert.assertEquals(Math.PI, new Complex(-1.0, 0.0).getArgument(), EPS);
        Assert.assertEquals(-Math.PI / 2.0, new Complex(0.0, -1.0).getArgument(), EPS);
        Assert.assertTrue(Double.isNaN(Complex.NaN.getArgument()));
    }

    @Test
    public void testNthRoot() {
        Complex c = new Complex(0.0, 8.0);
        List<Complex> roots = c.nthRoot(3);
        Assert.assertEquals(3, roots.size());
        for (Complex root : roots) {
            Complex cubed = root.multiply(root).multiply(root);
            Assert.assertEquals(c.getReal(), cubed.getReal(), 1e-10);
            Assert.assertEquals(c.getImaginary(), cubed.getImaginary(), 1e-10);
        }

        List<Complex> nanRoots = Complex.NaN.nthRoot(2);
        Assert.assertEquals(1, nanRoots.size());
        Assert.assertTrue(nanRoots.get(0).isNaN());

        List<Complex> infRoots = Complex.INF.nthRoot(2);
        Assert.assertEquals(1, infRoots.size());
        Assert.assertTrue(infRoots.get(0).isInfinite());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNthRootZeroOrNegative() {
        Complex.ONE.nthRoot(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNthRootNegative() {
        Complex.ONE.nthRoot(-2);
    }

    @Test
    public void testGetField() {
        Complex c = new Complex(1.0, 2.0);
        Assert.assertEquals(ComplexField.getInstance(), c.getField());
    }

    @Test
    public void testToString() {
        Complex c = new Complex(1.5, -2.5);
        Assert.assertEquals("(1.5, -2.5)", c.toString());
    }

    @Test
    public void testSerialization() {
        Complex c = new Complex(3.0, 4.0);
        Complex deserialized = (Complex) TestUtils.serializeAndRecover(c);
        Assert.assertEquals(c, deserialized);
        Assert.assertEquals(c.hashCode(), deserialized.hashCode());
        Assert.assertFalse(deserialized.isNaN());
        Assert.assertFalse(deserialized.isInfinite());

        Complex nan = Complex.NaN;
        Complex deserializedNan = (Complex) TestUtils.serializeAndRecover(nan);
        Assert.assertTrue(deserializedNan.isNaN());
    }
}
