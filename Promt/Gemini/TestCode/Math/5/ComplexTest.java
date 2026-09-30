package org.apache.commons.math3.complex;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import org.apache.commons.math3.TestUtils;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.util.FastMath;
import org.junit.Assert;
import org.junit.Test;

public class ComplexTest {

    private static final double EPS = 1e-14;

    @Test
    public void testConstants() {
        Assert.assertEquals(0.0, Complex.I.getReal(), 0.0);
        Assert.assertEquals(1.0, Complex.I.getImaginary(), 0.0);

        Assert.assertTrue(Complex.NaN.isNaN());
        Assert.assertTrue(Double.isNaN(Complex.NaN.getReal()));
        Assert.assertTrue(Double.isNaN(Complex.NaN.getImaginary()));

        Assert.assertTrue(Complex.INF.isInfinite());
        Assert.assertEquals(Double.POSITIVE_INFINITY, Complex.INF.getReal(), 0.0);
        Assert.assertEquals(Double.POSITIVE_INFINITY, Complex.INF.getImaginary(), 0.0);

        Assert.assertEquals(1.0, Complex.ONE.getReal(), 0.0);
        Assert.assertEquals(0.0, Complex.ONE.getImaginary(), 0.0);

        Assert.assertEquals(0.0, Complex.ZERO.getReal(), 0.0);
        Assert.assertEquals(0.0, Complex.ZERO.getImaginary(), 0.0);
    }

    @Test
    public void testConstructorsAndAccessors() {
        Complex c1 = new Complex(3.0);
        Assert.assertEquals(3.0, c1.getReal(), 0.0);
        Assert.assertEquals(0.0, c1.getImaginary(), 0.0);
        Assert.assertFalse(c1.isNaN());
        Assert.assertFalse(c1.isInfinite());

        Complex c2 = new Complex(3.0, -4.0);
        Assert.assertEquals(3.0, c2.getReal(), 0.0);
        Assert.assertEquals(-4.0, c2.getImaginary(), 0.0);
        Assert.assertFalse(c2.isNaN());
        Assert.assertFalse(c2.isInfinite());

        Complex cNan1 = new Complex(Double.NaN, 1.0);
        Assert.assertTrue(cNan1.isNaN());
        Assert.assertFalse(cNan1.isInfinite());

        Complex cNan2 = new Complex(1.0, Double.NaN);
        Assert.assertTrue(cNan2.isNaN());
        Assert.assertFalse(cNan2.isInfinite());

        Complex cInf1 = new Complex(Double.POSITIVE_INFINITY, 1.0);
        Assert.assertTrue(cInf1.isInfinite());
        Assert.assertFalse(cInf1.isNaN());

        Complex cInf2 = new Complex(1.0, Double.NEGATIVE_INFINITY);
        Assert.assertTrue(cInf2.isInfinite());
        Assert.assertFalse(cInf2.isNaN());

        Complex cInfNan = new Complex(Double.POSITIVE_INFINITY, Double.NaN);
        Assert.assertTrue(cInfNan.isNaN());
        Assert.assertFalse(cInfNan.isInfinite());
    }

    @Test
    public void testAbs() {
        Assert.assertTrue(Double.isNaN(Complex.NaN.abs()));
        Assert.assertTrue(Double.isNaN(new Complex(Double.NaN, 1.0).abs()));
        Assert.assertTrue(Double.isNaN(new Complex(1.0, Double.NaN).abs()));

        Assert.assertEquals(Double.POSITIVE_INFINITY, Complex.INF.abs(), 0.0);
        Assert.assertEquals(Double.POSITIVE_INFINITY, new Complex(Double.NEGATIVE_INFINITY, 0.0).abs(), 0.0);
        Assert.assertEquals(Double.POSITIVE_INFINITY, new Complex(0.0, Double.POSITIVE_INFINITY).abs(), 0.0);

        Complex c1 = new Complex(3.0, 4.0);
        Assert.assertEquals(5.0, c1.abs(), EPS);

        Complex c2 = new Complex(4.0, 3.0);
        Assert.assertEquals(5.0, c2.abs(), EPS);

        Complex c3 = new Complex(0.0, 5.0);
        Assert.assertEquals(5.0, c3.abs(), EPS);

        Complex c4 = new Complex(5.0, 0.0);
        Assert.assertEquals(5.0, c4.abs(), EPS);

        Complex c5 = new Complex(0.0, 0.0);
        Assert.assertEquals(0.0, c5.abs(), EPS);

        Complex c6 = new Complex(-3.0, -4.0);
        Assert.assertEquals(5.0, c6.abs(), EPS);
    }

    @Test
    public void testAdd() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex c2 = new Complex(3.0, -4.0);
        Complex sum = c1.add(c2);
        Assert.assertEquals(4.0, sum.getReal(), EPS);
        Assert.assertEquals(-2.0, sum.getImaginary(), EPS);

        Assert.assertTrue(c1.add(Complex.NaN).isNaN());
        Assert.assertTrue(Complex.NaN.add(c1).isNaN());
        Assert.assertTrue(Complex.NaN.add(Complex.NaN).isNaN());

        Complex sumReal = c1.add(5.0);
        Assert.assertEquals(6.0, sumReal.getReal(), EPS);
        Assert.assertEquals(2.0, sumReal.getImaginary(), EPS);

        Assert.assertTrue(c1.add(Double.NaN).isNaN());
        Assert.assertTrue(Complex.NaN.add(5.0).isNaN());
    }

    @Test(expected = NullArgumentException.class)
    public void testAddNull() {
        new Complex(1.0, 1.0).add(null);
    }

    @Test
    public void testConjugate() {
        Complex c = new Complex(3.0, 4.0);
        Complex conj = c.conjugate();
        Assert.assertEquals(3.0, conj.getReal(), EPS);
        Assert.assertEquals(-4.0, conj.getImaginary(), EPS);

        Complex cZeroImag = new Complex(3.0, 0.0);
        Assert.assertEquals(3.0, cZeroImag.conjugate().getReal(), EPS);
        Assert.assertEquals(-0.0, cZeroImag.conjugate().getImaginary(), EPS);

        Assert.assertTrue(Complex.NaN.conjugate().isNaN());

        Complex infConj = new Complex(1.0, Double.POSITIVE_INFINITY).conjugate();
        Assert.assertEquals(1.0, infConj.getReal(), EPS);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, infConj.getImaginary(), EPS);
    }

    @Test
    public void testDivide() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex c2 = new Complex(3.0, 4.0);
        Complex res = c1.divide(c2);
        Assert.assertEquals(11.0 / 25.0, res.getReal(), EPS);
        Assert.assertEquals(2.0 / 25.0, res.getImaginary(), EPS);

        Complex c3 = new Complex(4.0, 3.0);
        Complex res2 = c1.divide(c3);
        Assert.assertEquals(10.0 / 25.0, res2.getReal(), EPS);
        Assert.assertEquals(5.0 / 25.0, res2.getImaginary(), EPS);

        Assert.assertTrue(c1.divide(Complex.ZERO).isNaN());
        Assert.assertTrue(c1.divide(Complex.NaN).isNaN());
        Assert.assertTrue(Complex.NaN.divide(c1).isNaN());
        Assert.assertTrue(Complex.INF.divide(Complex.INF).isNaN());

        Complex finiteDivInf = c1.divide(Complex.INF);
        Assert.assertEquals(Complex.ZERO, finiteDivInf);

        Complex infDivFinite = Complex.INF.divide(c1);
        Assert.assertTrue(infDivFinite.isInfinite() || infDivFinite.isNaN());

        Complex resDouble = c1.divide(2.0);
        Assert.assertEquals(0.5, resDouble.getReal(), EPS);
        Assert.assertEquals(1.0, resDouble.getImaginary(), EPS);

        Assert.assertTrue(c1.divide(0.0).isNaN());
        Assert.assertTrue(c1.divide(Double.NaN).isNaN());
        Assert.assertTrue(Complex.NaN.divide(2.0).isNaN());

        Assert.assertEquals(Complex.ZERO, c1.divide(Double.POSITIVE_INFINITY));
        Assert.assertEquals(Complex.ZERO, c1.divide(Double.NEGATIVE_INFINITY));
        Assert.assertTrue(Complex.INF.divide(Double.POSITIVE_INFINITY).isNaN());
    }

    @Test(expected = NullArgumentException.class)
    public void testDivideNull() {
        new Complex(1.0, 1.0).divide(null);
    }

    @Test
    public void testReciprocal() {
        Assert.assertTrue(Complex.NaN.reciprocal().isNaN());
        Assert.assertTrue(Complex.ZERO.reciprocal().isNaN());
        Assert.assertEquals(Complex.ZERO, Complex.INF.reciprocal());

        Complex c1 = new Complex(3.0, 4.0);
        Complex rec1 = c1.reciprocal();
        Assert.assertEquals(3.0 / 25.0, rec1.getReal(), EPS);
        Assert.assertEquals(-4.0 / 25.0, rec1.getImaginary(), EPS);

        Complex c2 = new Complex(4.0, 3.0);
        Complex rec2 = c2.reciprocal();
        Assert.assertEquals(4.0 / 25.0, rec2.getReal(), EPS);
        Assert.assertEquals(-3.0 / 25.0, rec2.getImaginary(), EPS);
    }

    @Test
    public void testMultiply() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex c2 = new Complex(3.0, 4.0);
        Complex prod = c1.multiply(c2);
        Assert.assertEquals(-5.0, prod.getReal(), EPS);
        Assert.assertEquals(10.0, prod.getImaginary(), EPS);

        Assert.assertTrue(c1.multiply(Complex.NaN).isNaN());
        Assert.assertTrue(Complex.NaN.multiply(c1).isNaN());

        Assert.assertEquals(Complex.INF, c1.multiply(Complex.INF));
        Assert.assertEquals(Complex.INF, Complex.INF.multiply(c1));
        Assert.assertEquals(Complex.INF, Complex.INF.multiply(Complex.INF));
        Assert.assertEquals(Complex.INF, new Complex(Double.POSITIVE_INFINITY, 1.0).multiply(c1));
        Assert.assertEquals(Complex.INF, new Complex(1.0, Double.POSITIVE_INFINITY).multiply(c1));

        Complex prodInt = c1.multiply(3);
        Assert.assertEquals(3.0, prodInt.getReal(), EPS);
        Assert.assertEquals(6.0, prodInt.getImaginary(), EPS);

        Assert.assertTrue(Complex.NaN.multiply(3).isNaN());
        Assert.assertEquals(Complex.INF, Complex.INF.multiply(3));
        Assert.assertEquals(Complex.INF, new Complex(1.0, Double.POSITIVE_INFINITY).multiply(3));

        Complex prodDouble = c1.multiply(2.5);
        Assert.assertEquals(2.5, prodDouble.getReal(), EPS);
        Assert.assertEquals(5.0, prodDouble.getImaginary(), EPS);

        Assert.assertTrue(c1.multiply(Double.NaN).isNaN());
        Assert.assertTrue(Complex.NaN.multiply(2.5).isNaN());
        Assert.assertEquals(Complex.INF, c1.multiply(Double.POSITIVE_INFINITY));
        Assert.assertEquals(Complex.INF, Complex.INF.multiply(2.5));
        Assert.assertEquals(Complex.INF, new Complex(1.0, Double.POSITIVE_INFINITY).multiply(2.5));
    }

    @Test(expected = NullArgumentException.class)
    public void testMultiplyNull() {
        new Complex(1.0, 1.0).multiply((Complex) null);
    }

    @Test
    public void testNegate() {
        Complex c = new Complex(3.0, -4.0);
        Complex neg = c.negate();
        Assert.assertEquals(-3.0, neg.getReal(), EPS);
        Assert.assertEquals(4.0, neg.getImaginary(), EPS);

        Assert.assertTrue(Complex.NaN.negate().isNaN());
    }

    @Test
    public void testSubtract() {
        Complex c1 = new Complex(5.0, 7.0);
        Complex c2 = new Complex(2.0, 3.0);
        Complex diff = c1.subtract(c2);
        Assert.assertEquals(3.0, diff.getReal(), EPS);
        Assert.assertEquals(4.0, diff.getImaginary(), EPS);

        Assert.assertTrue(c1.subtract(Complex.NaN).isNaN());
        Assert.assertTrue(Complex.NaN.subtract(c1).isNaN());

        Complex diffDouble = c1.subtract(2.0);
        Assert.assertEquals(3.0, diffDouble.getReal(), EPS);
        Assert.assertEquals(7.0, diffDouble.getImaginary(), EPS);

        Assert.assertTrue(c1.subtract(Double.NaN).isNaN());
        Assert.assertTrue(Complex.NaN.subtract(2.0).isNaN());
    }

    @Test(expected = NullArgumentException.class)
    public void testSubtractNull() {
        new Complex(1.0, 1.0).subtract(null);
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
        Assert.assertTrue(Complex.NaN.equals(nan1));
        Assert.assertFalse(c1.equals(nan1));
        Assert.assertFalse(nan1.equals(c1));

        Assert.assertEquals(7, Complex.NaN.hashCode());
        Assert.assertEquals(7, nan1.hashCode());
        Assert.assertEquals(7, nan2.hashCode());
    }

    @Test
    public void testTrigonometricFunctions() {
        Complex z = new Complex(1.0, 2.0);

        Assert.assertTrue(Complex.NaN.acos().isNaN());
        Assert.assertTrue(Complex.NaN.asin().isNaN());
        Assert.assertTrue(Complex.NaN.atan().isNaN());
        Assert.assertTrue(Complex.NaN.cos().isNaN());
        Assert.assertTrue(Complex.NaN.sin().isNaN());
        Assert.assertTrue(Complex.NaN.tan().isNaN());

        Assert.assertTrue(new Complex(Double.POSITIVE_INFINITY, 1.0).tan().isNaN());

        Complex tanLargePosImag = new Complex(1.0, 25.0).tan();
        Assert.assertEquals(0.0, tanLargePosImag.getReal(), EPS);
        Assert.assertEquals(1.0, tanLargePosImag.getImaginary(), EPS);

        Complex tanLargeNegImag = new Complex(1.0, -25.0).tan();
        Assert.assertEquals(0.0, tanLargeNegImag.getReal(), EPS);
        Assert.assertEquals(-1.0, tanLargeNegImag.getImaginary(), EPS);

        Complex acosZ = z.acos();
        Complex cosAcosZ = acosZ.cos();
        Assert.assertEquals(z.getReal(), cosAcosZ.getReal(), 1e-12);
        Assert.assertEquals(z.getImaginary(), cosAcosZ.getImaginary(), 1e-12);

        Complex asinZ = z.asin();
        Complex sinAsinZ = asinZ.sin();
        Assert.assertEquals(z.getReal(), sinAsinZ.getReal(), 1e-12);
        Assert.assertEquals(z.getImaginary(), sinAsinZ.getImaginary(), 1e-12);

        Complex atanZ = z.atan();
        Complex tanAtanZ = atanZ.tan();
        Assert.assertEquals(z.getReal(), tanAtanZ.getReal(), 1e-12);
        Assert.assertEquals(z.getImaginary(), tanAtanZ.getImaginary(), 1e-12);
    }

    @Test
    public void testHyperbolicFunctions() {
        Complex z = new Complex(1.0, 2.0);

        Assert.assertTrue(Complex.NaN.cosh().isNaN());
        Assert.assertTrue(Complex.NaN.sinh().isNaN());
        Assert.assertTrue(Complex.NaN.tanh().isNaN());

        Assert.assertTrue(new Complex(1.0, Double.POSITIVE_INFINITY).tanh().isNaN());

        Complex tanhLargePosReal = new Complex(25.0, 1.0).tanh();
        Assert.assertEquals(1.0, tanhLargePosReal.getReal(), EPS);
        Assert.assertEquals(0.0, tanhLargePosReal.getImaginary(), EPS);

        Complex tanhLargeNegReal = new Complex(-25.0, 1.0).tanh();
        Assert.assertEquals(-1.0, tanhLargeNegReal.getReal(), EPS);
        Assert.assertEquals(0.0, tanhLargeNegReal.getImaginary(), EPS);

        Complex regularTanh = z.tanh();
        Complex expectedTanh = z.sinh().divide(z.cosh());
        Assert.assertEquals(expectedTanh.getReal(), regularTanh.getReal(), 1e-12);
        Assert.assertEquals(expectedTanh.getImaginary(), regularTanh.getImaginary(), 1e-12);

        Complex coshVal = z.cosh();
        Complex expZ = z.exp();
        Complex expNegZ = z.negate().exp();
        Complex expectedCosh = expZ.add(expNegZ).divide(2.0);
        Assert.assertEquals(expectedCosh.getReal(), coshVal.getReal(), 1e-12);
        Assert.assertEquals(expectedCosh.getImaginary(), coshVal.getImaginary(), 1e-12);

        Complex sinhVal = z.sinh();
        Complex expectedSinh = expZ.subtract(expNegZ).divide(2.0);
        Assert.assertEquals(expectedSinh.getReal(), sinhVal.getReal(), 1e-12);
        Assert.assertEquals(expectedSinh.getImaginary(), sinhVal.getImaginary(), 1e-12);
    }

    @Test
    public void testExpAndLog() {
        Assert.assertTrue(Complex.NaN.exp().isNaN());
        Assert.assertTrue(Complex.NaN.log().isNaN());

        Complex z = new Complex(2.0, 3.0);
        Complex expLog = z.log().exp();
        Assert.assertEquals(z.getReal(), expLog.getReal(), 1e-12);
        Assert.assertEquals(z.getImaginary(), expLog.getImaginary(), 1e-12);

        Complex zeroExp = Complex.ZERO.exp();
        Assert.assertEquals(1.0, zeroExp.getReal(), EPS);
        Assert.assertEquals(0.0, zeroExp.getImaginary(), EPS);
    }

    @Test
    public void testPow() {
        Complex base = new Complex(2.0, 1.0);
        Complex exp = new Complex(3.0, -1.0);

        Complex powComplex = base.pow(exp);
        Complex expected = base.log().multiply(exp).exp();
        Assert.assertEquals(expected.getReal(), powComplex.getReal(), 1e-12);
        Assert.assertEquals(expected.getImaginary(), powComplex.getImaginary(), 1e-12);

        Complex powDouble = base.pow(2.0);
        Complex expectedDouble = base.multiply(base);
        Assert.assertEquals(expectedDouble.getReal(), powDouble.getReal(), 1e-12);
        Assert.assertEquals(expectedDouble.getImaginary(), powDouble.getImaginary(), 1e-12);
    }

    @Test(expected = NullArgumentException.class)
    public void testPowNull() {
        new Complex(2.0, 1.0).pow((Complex) null);
    }

    @Test
    public void testSqrt() {
        Assert.assertTrue(Complex.NaN.sqrt().isNaN());

        Complex zeroSqrt = Complex.ZERO.sqrt();
        Assert.assertEquals(0.0, zeroSqrt.getReal(), EPS);
        Assert.assertEquals(0.0, zeroSqrt.getImaginary(), EPS);

        Complex cPosReal = new Complex(3.0, 4.0);
        Complex sqrtPosReal = cPosReal.sqrt();
        Assert.assertEquals(2.0, sqrtPosReal.getReal(), EPS);
        Assert.assertEquals(1.0, sqrtPosReal.getImaginary(), EPS);

        Complex cNegRealPosImag = new Complex(-3.0, 4.0);
        Complex sqrtNegRealPosImag = cNegRealPosImag.sqrt();
        Assert.assertEquals(1.0, sqrtNegRealPosImag.getReal(), EPS);
        Assert.assertEquals(2.0, sqrtNegRealPosImag.getImaginary(), EPS);

        Complex cNegRealNegImag = new Complex(-3.0, -4.0);
        Complex sqrtNegRealNegImag = cNegRealNegImag.sqrt();
        Assert.assertEquals(1.0, sqrtNegRealNegImag.getReal(), EPS);
        Assert.assertEquals(-2.0, sqrtNegRealNegImag.getImaginary(), EPS);

        Assert.assertTrue(Complex.NaN.sqrt1z().isNaN());
        Complex sqrt1zVal = new Complex(0.5, 0.5).sqrt1z();
        Complex expectedSqrt1z = Complex.ONE.subtract(new Complex(0.5, 0.5).multiply(new Complex(0.5, 0.5))).sqrt();
        Assert.assertEquals(expectedSqrt1z.getReal(), sqrt1zVal.getReal(), EPS);
        Assert.assertEquals(expectedSqrt1z.getImaginary(), sqrt1zVal.getImaginary(), EPS);
    }

    @Test
    public void testGetArgument() {
        Assert.assertEquals(0.0, new Complex(1.0, 0.0).getArgument(), EPS);
        Assert.assertEquals(FastMath.PI / 2.0, new Complex(0.0, 1.0).getArgument(), EPS);
        Assert.assertEquals(FastMath.PI, new Complex(-1.0, 0.0).getArgument(), EPS);
        Assert.assertEquals(-FastMath.PI / 2.0, new Complex(0.0, -1.0).getArgument(), EPS);
        Assert.assertTrue(Double.isNaN(Complex.NaN.getArgument()));
    }

    @Test
    public void testNthRoot() {
        Complex z = new Complex(0.0, 8.0);
        List<Complex> roots = z.nthRoot(3);
        Assert.assertEquals(3, roots.size());
        for (Complex root : roots) {
            Complex cubed = root.multiply(root).multiply(root);
            Assert.assertEquals(z.getReal(), cubed.getReal(), 1e-12);
            Assert.assertEquals(z.getImaginary(), cubed.getImaginary(), 1e-12);
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
        new Complex(1.0, 1.0).nthRoot(0);
    }

    @Test(expected = NotPositiveException.class)
    public void testNthRootNegative() {
        new Complex(1.0, 1.0).nthRoot(-2);
    }

    @Test
    public void testValueOf() {
        Complex c1 = Complex.valueOf(3.0, 4.0);
        Assert.assertEquals(3.0, c1.getReal(), EPS);
        Assert.assertEquals(4.0, c1.getImaginary(), EPS);

        Assert.assertTrue(Complex.valueOf(Double.NaN, 1.0).isNaN());
        Assert.assertTrue(Complex.valueOf(1.0, Double.NaN).isNaN());
        Assert.assertTrue(Complex.valueOf(Double.NaN, Double.NaN).isNaN());

        Complex c2 = Complex.valueOf(5.0);
        Assert.assertEquals(5.0, c2.getReal(), EPS);
        Assert.assertEquals(0.0, c2.getImaginary(), EPS);

        Assert.assertTrue(Complex.valueOf(Double.NaN).isNaN());
    }

    @Test
    public void testCreateComplex() {
        Complex c = new Complex(1.0, 1.0) {
            private static final long serialVersionUID = 1L;
            @Override
            public Complex createComplex(double realPart, double imaginaryPart) {
                return super.createComplex(realPart, imaginaryPart);
            }
        };
        Complex created = c.createComplex(2.0, 3.0);
        Assert.assertEquals(2.0, created.getReal(), EPS);
        Assert.assertEquals(3.0, created.getImaginary(), EPS);
    }

    @Test
    public void testGetField() {
        Complex c = new Complex(1.0, 1.0);
        Assert.assertNotNull(c.getField());
        Assert.assertSame(ComplexField.getInstance(), c.getField());
    }

    @Test
    public void testToString() {
        Complex c = new Complex(1.5, -2.5);
        Assert.assertEquals("(1.5, -2.5)", c.toString());
    }

    @Test
    public void testSerialization() throws Exception {
        Complex c = new Complex(3.0, 4.0);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(c);
        oos.flush();
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Complex deserialized = (Complex) ois.readObject();
        ois.close();

        Assert.assertEquals(c, deserialized);
        Assert.assertFalse(deserialized.isNaN());
        Assert.assertFalse(deserialized.isInfinite());
    }

    @Test
    public void testSerializationNaNAndInf() throws Exception {
        Complex nan = Complex.NaN;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(nan);
        oos.writeObject(Complex.INF);
        oos.flush();
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Complex desNaN = (Complex) ois.readObject();
        Complex desInf = (Complex) ois.readObject();
        ois.close();

        Assert.assertTrue(desNaN.isNaN());
        Assert.assertTrue(desInf.isInfinite());
    }
}
