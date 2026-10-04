package org.apache.commons.math.complex;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import org.apache.commons.math.exception.NotPositiveException;
import org.apache.commons.math.exception.NullArgumentException;
import org.apache.commons.math.util.FastMath;
import org.junit.Assert;
import org.junit.Test;

public class ComplexTest {

    private static final double EPSILON = 1e-12;

    @Test
    public void testConstants() {
        Assert.assertEquals(0.0, Complex.I.getReal(), EPSILON);
        Assert.assertEquals(1.0, Complex.I.getImaginary(), EPSILON);
        Assert.assertEquals(1.0, Complex.ONE.getReal(), EPSILON);
        Assert.assertEquals(0.0, Complex.ONE.getImaginary(), EPSILON);
        Assert.assertEquals(0.0, Complex.ZERO.getReal(), EPSILON);
        Assert.assertEquals(0.0, Complex.ZERO.getImaginary(), EPSILON);
        Assert.assertTrue(Complex.NaN.isNaN());
        Assert.assertTrue(Complex.INF.isInfinite());
    }

    @Test
    public void testConstructorsAndGetters() {
        Complex c1 = new Complex(3.0);
        Assert.assertEquals(3.0, c1.getReal(), EPSILON);
        Assert.assertEquals(0.0, c1.getImaginary(), EPSILON);
        Assert.assertFalse(c1.isNaN());
        Assert.assertFalse(c1.isInfinite());

        Complex c2 = new Complex(3.0, -4.0);
        Assert.assertEquals(3.0, c2.getReal(), EPSILON);
        Assert.assertEquals(-4.0, c2.getImaginary(), EPSILON);

        Complex c3 = new Complex(Double.NaN, 1.0);
        Assert.assertTrue(c3.isNaN());
        Assert.assertFalse(c3.isInfinite());

        Complex c4 = new Complex(1.0, Double.NaN);
        Assert.assertTrue(c4.isNaN());
        Assert.assertFalse(c4.isInfinite());

        Complex c5 = new Complex(Double.POSITIVE_INFINITY, 1.0);
        Assert.assertFalse(c5.isNaN());
        Assert.assertTrue(c5.isInfinite());

        Complex c6 = new Complex(1.0, Double.NEGATIVE_INFINITY);
        Assert.assertFalse(c6.isNaN());
        Assert.assertTrue(c6.isInfinite());

        Complex c7 = new Complex(Double.POSITIVE_INFINITY, Double.NaN);
        Assert.assertTrue(c7.isNaN());
        Assert.assertFalse(c7.isInfinite());
    }

    @Test
    public void testAbs() {
        Complex z1 = new Complex(3.0, 4.0);
        Assert.assertEquals(5.0, z1.abs(), EPSILON);

        Complex z2 = new Complex(4.0, 3.0);
        Assert.assertEquals(5.0, z2.abs(), EPSILON);

        Complex z3 = new Complex(0.0, 5.0);
        Assert.assertEquals(5.0, z3.abs(), EPSILON);

        Complex z4 = new Complex(5.0, 0.0);
        Assert.assertEquals(5.0, z4.abs(), EPSILON);

        Complex z5 = new Complex(0.0, 0.0);
        Assert.assertEquals(0.0, z5.abs(), EPSILON);

        Assert.assertTrue(Double.isNaN(Complex.NaN.abs()));
        Assert.assertEquals(Double.POSITIVE_INFINITY, Complex.INF.abs(), EPSILON);
        Assert.assertEquals(Double.POSITIVE_INFINITY, new Complex(Double.NEGATIVE_INFINITY, 0.0).abs(), EPSILON);
        Assert.assertEquals(Double.POSITIVE_INFINITY, new Complex(0.0, Double.POSITIVE_INFINITY).abs(), EPSILON);
    }

    @Test
    public void testAdd() {
        Complex x = new Complex(3.0, 4.0);
        Complex y = new Complex(5.0, 6.0);
        Complex z = x.add(y);
        Assert.assertEquals(8.0, z.getReal(), EPSILON);
        Assert.assertEquals(10.0, z.getImaginary(), EPSILON);

        Assert.assertTrue(x.add(Complex.NaN).isNaN());
        Assert.assertTrue(Complex.NaN.add(x).isNaN());

        Complex zReal = x.add(2.0);
        Assert.assertEquals(5.0, zReal.getReal(), EPSILON);
        Assert.assertEquals(4.0, zReal.getImaginary(), EPSILON);

        Assert.assertTrue(x.add(Double.NaN).isNaN());
        Assert.assertTrue(Complex.NaN.add(2.0).isNaN());
    }

    @Test(expected = NullArgumentException.class)
    public void testAddNull() {
        Complex.ONE.add(null);
    }

    @Test
    public void testConjugate() {
        Complex x = new Complex(3.0, 4.0);
        Complex z = x.conjugate();
        Assert.assertEquals(3.0, z.getReal(), EPSILON);
        Assert.assertEquals(-4.0, z.getImaginary(), EPSILON);

        Assert.assertTrue(Complex.NaN.conjugate().isNaN());

        Complex inf = new Complex(1.0, Double.POSITIVE_INFINITY).conjugate();
        Assert.assertEquals(1.0, inf.getReal(), EPSILON);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, inf.getImaginary(), EPSILON);
    }

    @Test
    public void testDivide() {
        Complex x = new Complex(3.0, 4.0);
        Complex y = new Complex(2.0, 1.0);
        Complex z = x.divide(y);
        Assert.assertEquals(2.0, z.getReal(), EPSILON);
        Assert.assertEquals(1.0, z.getImaginary(), EPSILON);

        Complex y2 = new Complex(1.0, 2.0);
        Complex z2 = x.divide(y2);
        Assert.assertEquals(2.2, z2.getReal(), EPSILON);
        Assert.assertEquals(-0.4, z2.getImaginary(), EPSILON);

        Assert.assertTrue(x.divide(Complex.NaN).isNaN());
        Assert.assertTrue(Complex.NaN.divide(x).isNaN());
        Assert.assertTrue(x.divide(Complex.ZERO).isNaN());
        Assert.assertTrue(Complex.ZERO.divide(Complex.ZERO).isNaN());

        Complex infDiv = x.divide(Complex.INF);
        Assert.assertEquals(0.0, infDiv.getReal(), EPSILON);
        Assert.assertEquals(0.0, infDiv.getImaginary(), EPSILON);

        Assert.assertTrue(Complex.INF.divide(Complex.INF).isNaN());
    }

    @Test
    public void testDivideDouble() {
        Complex x = new Complex(3.0, 4.0);
        Complex z = x.divide(2.0);
        Assert.assertEquals(1.5, z.getReal(), EPSILON);
        Assert.assertEquals(2.0, z.getImaginary(), EPSILON);

        Assert.assertTrue(x.divide(Double.NaN).isNaN());
        Assert.assertTrue(Complex.NaN.divide(2.0).isNaN());
        Assert.assertTrue(x.divide(0.0).isNaN());

        Complex zInf = x.divide(Double.POSITIVE_INFINITY);
        Assert.assertEquals(0.0, zInf.getReal(), EPSILON);
        Assert.assertEquals(0.0, zInf.getImaginary(), EPSILON);

        Assert.assertTrue(Complex.INF.divide(Double.POSITIVE_INFINITY).isNaN());
    }

    @Test(expected = NullArgumentException.class)
    public void testDivideNull() {
        Complex.ONE.divide(null);
    }

    @Test
    public void testMultiply() {
        Complex x = new Complex(3.0, 4.0);
        Complex y = new Complex(-2.0, 3.0);
        Complex z = x.multiply(y);
        Assert.assertEquals(-18.0, z.getReal(), EPSILON);
        Assert.assertEquals(1.0, z.getImaginary(), EPSILON);

        Assert.assertTrue(x.multiply(Complex.NaN).isNaN());
        Assert.assertTrue(Complex.NaN.multiply(x).isNaN());

        Complex infMult = x.multiply(Complex.INF);
        Assert.assertTrue(infMult.isInfinite());

        Complex zReal = x.multiply(2.5);
        Assert.assertEquals(7.5, zReal.getReal(), EPSILON);
        Assert.assertEquals(10.0, zReal.getImaginary(), EPSILON);

        Assert.assertTrue(x.multiply(Double.NaN).isNaN());
        Assert.assertTrue(Complex.NaN.multiply(2.5).isNaN());
        Assert.assertTrue(x.multiply(Double.POSITIVE_INFINITY).isInfinite());
        Assert.assertTrue(Complex.INF.multiply(2.0).isInfinite());
    }

    @Test(expected = NullArgumentException.class)
    public void testMultiplyNull() {
        Complex.ONE.multiply(null);
    }

    @Test
    public void testNegate() {
        Complex x = new Complex(3.0, -4.0);
        Complex z = x.negate();
        Assert.assertEquals(-3.0, z.getReal(), EPSILON);
        Assert.assertEquals(4.0, z.getImaginary(), EPSILON);
        Assert.assertTrue(Complex.NaN.negate().isNaN());
    }

    @Test
    public void testSubtract() {
        Complex x = new Complex(3.0, 4.0);
        Complex y = new Complex(5.0, 2.0);
        Complex z = x.subtract(y);
        Assert.assertEquals(-2.0, z.getReal(), EPSILON);
        Assert.assertEquals(2.0, z.getImaginary(), EPSILON);

        Assert.assertTrue(x.subtract(Complex.NaN).isNaN());
        Assert.assertTrue(Complex.NaN.subtract(x).isNaN());

        Complex zReal = x.subtract(2.0);
        Assert.assertEquals(1.0, zReal.getReal(), EPSILON);
        Assert.assertEquals(4.0, zReal.getImaginary(), EPSILON);

        Assert.assertTrue(x.subtract(Double.NaN).isNaN());
        Assert.assertTrue(Complex.NaN.subtract(2.0).isNaN());
    }

    @Test(expected = NullArgumentException.class)
    public void testSubtractNull() {
        Complex.ONE.subtract(null);
    }

    @Test
    public void testEqualsAndHashCode() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex c2 = new Complex(1.0, 2.0);
        Complex c3 = new Complex(1.0, 3.0);
        Complex c4 = new Complex(2.0, 2.0);

        Assert.assertTrue(c1.equals(c1));
        Assert.assertTrue(c1.equals(c2));
        Assert.assertEquals(c1.hashCode(), c2.hashCode());

        Assert.assertFalse(c1.equals(null));
        Assert.assertFalse(c1.equals("Not a complex"));
        Assert.assertFalse(c1.equals(c3));
        Assert.assertFalse(c1.equals(c4));

        Complex nan1 = new Complex(Double.NaN, 1.0);
        Complex nan2 = new Complex(1.0, Double.NaN);
        Assert.assertTrue(nan1.equals(nan2));
        Assert.assertTrue(nan1.equals(Complex.NaN));
        Assert.assertEquals(nan1.hashCode(), Complex.NaN.hashCode());
        Assert.assertEquals(7, nan1.hashCode());
        Assert.assertFalse(c1.equals(nan1));
        Assert.assertFalse(nan1.equals(c1));
    }

    @Test
    public void testAcos() {
        Complex z = new Complex(0.5, 0.5).acos();
        Assert.assertEquals(1.118517879643706, z.getReal(), EPSILON);
        Assert.assertEquals(-0.5306375309525178, z.getImaginary(), EPSILON);
        Assert.assertTrue(Complex.NaN.acos().isNaN());
    }

    @Test
    public void testAsin() {
        Complex z = new Complex(0.5, 0.5).asin();
        Assert.assertEquals(0.4522784471511906, z.getReal(), EPSILON);
        Assert.assertEquals(0.5306375309525178, z.getImaginary(), EPSILON);
        Assert.assertTrue(Complex.NaN.asin().isNaN());
    }

    @Test
    public void testAtan() {
        Complex z = new Complex(0.5, 0.5).atan();
        Assert.assertEquals(0.4023594781085251, z.getReal(), EPSILON);
        Assert.assertEquals(0.5535743588970452, z.getImaginary(), EPSILON);
        Assert.assertTrue(Complex.NaN.atan().isNaN());
    }

    @Test
    public void testCosAndCosh() {
        Complex z = new Complex(1.0, 1.0);
        Complex cosZ = z.cos();
        Assert.assertEquals(FastMath.cos(1.0) * FastMath.cosh(1.0), cosZ.getReal(), EPSILON);
        Assert.assertEquals(-FastMath.sin(1.0) * FastMath.sinh(1.0), cosZ.getImaginary(), EPSILON);
        Assert.assertTrue(Complex.NaN.cos().isNaN());

        Complex coshZ = z.cosh();
        Assert.assertEquals(FastMath.cosh(1.0) * FastMath.cos(1.0), coshZ.getReal(), EPSILON);
        Assert.assertEquals(FastMath.sinh(1.0) * FastMath.sin(1.0), coshZ.getImaginary(), EPSILON);
        Assert.assertTrue(Complex.NaN.cosh().isNaN());
    }

    @Test
    public void testExp() {
        Complex z = new Complex(2.0, 3.0).exp();
        Assert.assertEquals(FastMath.exp(2.0) * FastMath.cos(3.0), z.getReal(), EPSILON);
        Assert.assertEquals(FastMath.exp(2.0) * FastMath.sin(3.0), z.getImaginary(), EPSILON);
        Assert.assertTrue(Complex.NaN.exp().isNaN());
    }

    @Test
    public void testLog() {
        Complex z = new Complex(3.0, 4.0).log();
        Assert.assertEquals(FastMath.log(5.0), z.getReal(), EPSILON);
        Assert.assertEquals(FastMath.atan2(4.0, 3.0), z.getImaginary(), EPSILON);
        Assert.assertTrue(Complex.NaN.log().isNaN());
    }

    @Test
    public void testPowComplex() {
        Complex base = new Complex(2.0, 1.0);
        Complex exp = new Complex(1.5, -0.5);
        Complex res = base.pow(exp);
        Assert.assertFalse(res.isNaN());

        Assert.assertTrue(base.pow(Complex.NaN).isNaN());
        Assert.assertTrue(Complex.NaN.pow(base).isNaN());
    }

    @Test(expected = NullArgumentException.class)
    public void testPowNull() {
        Complex.ONE.pow((Complex) null);
    }

    @Test
    public void testPowDouble() {
        Complex base = new Complex(2.0, 1.0);
        Complex res = base.pow(2.0);
        Complex exp = base.multiply(base);
        Assert.assertEquals(exp.getReal(), res.getReal(), EPSILON);
        Assert.assertEquals(exp.getImaginary(), res.getImaginary(), EPSILON);

        Assert.assertTrue(Complex.NaN.pow(2.0).isNaN());
    }

    @Test
    public void testSinAndSinh() {
        Complex z = new Complex(1.0, 1.0);
        Complex sinZ = z.sin();
        Assert.assertEquals(FastMath.sin(1.0) * FastMath.cosh(1.0), sinZ.getReal(), EPSILON);
        Assert.assertEquals(FastMath.cos(1.0) * FastMath.sinh(1.0), sinZ.getImaginary(), EPSILON);
        Assert.assertTrue(Complex.NaN.sin().isNaN());

        Complex sinhZ = z.sinh();
        Assert.assertEquals(FastMath.sinh(1.0) * FastMath.cos(1.0), sinhZ.getReal(), EPSILON);
        Assert.assertEquals(FastMath.cosh(1.0) * FastMath.sin(1.0), sinhZ.getImaginary(), EPSILON);
        Assert.assertTrue(Complex.NaN.sinh().isNaN());
    }

    @Test
    public void testSqrt() {
        Complex z1 = new Complex(3.0, 4.0).sqrt();
        Assert.assertEquals(2.0, z1.getReal(), EPSILON);
        Assert.assertEquals(1.0, z1.getImaginary(), EPSILON);

        Complex z2 = new Complex(-3.0, 4.0).sqrt();
        Assert.assertEquals(1.0, z2.getReal(), EPSILON);
        Assert.assertEquals(2.0, z2.getImaginary(), EPSILON);

        Complex z3 = new Complex(-3.0, -4.0).sqrt();
        Assert.assertEquals(1.0, z3.getReal(), EPSILON);
        Assert.assertEquals(-2.0, z3.getImaginary(), EPSILON);

        Complex zeroSqrt = Complex.ZERO.sqrt();
        Assert.assertEquals(0.0, zeroSqrt.getReal(), EPSILON);
        Assert.assertEquals(0.0, zeroSqrt.getImaginary(), EPSILON);

        Assert.assertTrue(Complex.NaN.sqrt().isNaN());
    }

    @Test
    public void testSqrt1z() {
        Complex z = new Complex(0.5, 0.5);
        Complex res = z.sqrt1z();
        Complex expected = Complex.ONE.subtract(z.multiply(z)).sqrt();
        Assert.assertEquals(expected.getReal(), res.getReal(), EPSILON);
        Assert.assertEquals(expected.getImaginary(), res.getImaginary(), EPSILON);
        Assert.assertTrue(Complex.NaN.sqrt1z().isNaN());
    }

    @Test
    public void testTanAndTanh() {
        Complex z = new Complex(1.0, 1.0);
        Complex tanZ = z.tan();
        Assert.assertEquals(0.27175258531951174, tanZ.getReal(), EPSILON);
        Assert.assertEquals(1.0839233273386946, tanZ.getImaginary(), EPSILON);
        Assert.assertTrue(Complex.NaN.tan().isNaN());

        Complex tanhZ = z.tanh();
        Assert.assertEquals(1.0839233273386946, tanhZ.getReal(), EPSILON);
        Assert.assertEquals(0.27175258531951174, tanhZ.getImaginary(), EPSILON);
        Assert.assertTrue(Complex.NaN.tanh().isNaN());
    }

    @Test
    public void testGetArgument() {
        Assert.assertEquals(0.0, new Complex(1.0, 0.0).getArgument(), EPSILON);
        Assert.assertEquals(FastMath.PI / 2.0, new Complex(0.0, 1.0).getArgument(), EPSILON);
        Assert.assertEquals(FastMath.PI, new Complex(-1.0, 0.0).getArgument(), EPSILON);
        Assert.assertEquals(-FastMath.PI / 2.0, new Complex(0.0, -1.0).getArgument(), EPSILON);
        Assert.assertTrue(Double.isNaN(Complex.NaN.getArgument()));
    }

    @Test
    public void testNthRoot() {
        Complex z = new Complex(0.0, 8.0);
        List<Complex> roots = z.nthRoot(3);
        Assert.assertEquals(3, roots.size());
        for (Complex root : roots) {
            Complex cubed = root.multiply(root).multiply(root);
            Assert.assertEquals(z.getReal(), cubed.getReal(), 1e-10);
            Assert.assertEquals(z.getImaginary(), cubed.getImaginary(), 1e-10);
        }

        List<Complex> nanRoots = Complex.NaN.nthRoot(2);
        Assert.assertEquals(1, nanRoots.size());
        Assert.assertTrue(nanRoots.get(0).isNaN());

        List<Complex> infRoots = Complex.INF.nthRoot(2);
        Assert.assertEquals(1, infRoots.size());
        Assert.assertTrue(infRoots.get(0).isInfinite());
    }

    @Test(expected = NotPositiveException.class)
    public void testNthRootZero() {
        Complex.ONE.nthRoot(0);
    }

    @Test(expected = NotPositiveException.class)
    public void testNthRootNegative() {
        Complex.ONE.nthRoot(-2);
    }

    @Test
    public void testValueOf() {
        Complex c1 = Complex.valueOf(1.0, 2.0);
        Assert.assertEquals(1.0, c1.getReal(), EPSILON);
        Assert.assertEquals(2.0, c1.getImaginary(), EPSILON);

        Assert.assertTrue(Complex.valueOf(Double.NaN, 2.0).isNaN());
        Assert.assertTrue(Complex.valueOf(1.0, Double.NaN).isNaN());
        Assert.assertTrue(Complex.valueOf(Double.NaN, Double.NaN).isNaN());

        Complex c2 = Complex.valueOf(3.0);
        Assert.assertEquals(3.0, c2.getReal(), EPSILON);
        Assert.assertEquals(0.0, c2.getImaginary(), EPSILON);

        Assert.assertTrue(Complex.valueOf(Double.NaN).isNaN());
    }

    @Test
    public void testFieldAndToString() {
        Complex c = new Complex(2.5, -3.5);
        Assert.assertNotNull(c.getField());
        Assert.assertEquals(ComplexField.getInstance(), c.getField());
        Assert.assertEquals("(2.5, -3.5)", c.toString());
    }

    @Test
    public void testSerialization() throws Exception {
        Complex c = new Complex(1.23, 4.56);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(c);
        oos.close();

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        Complex deserialized = (Complex) ois.readObject();
        ois.close();

        Assert.assertEquals(c, deserialized);
        Assert.assertFalse(deserialized.isNaN());
        Assert.assertFalse(deserialized.isInfinite());
    }
}
