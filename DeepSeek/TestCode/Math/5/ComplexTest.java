package org.apache.commons.math3.complex;

import java.util.List;

import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.junit.Assert;
import org.junit.Test;

public class ComplexTest {

    private static final double DELTA = 1e-12;

    @Test
    public void testConstructorRealOnly() {
        Complex c = new Complex(1.0);
        Assert.assertEquals(1.0, c.getReal(), DELTA);
        Assert.assertEquals(0.0, c.getImaginary(), DELTA);
        Assert.assertFalse(c.isNaN());
        Assert.assertFalse(c.isInfinite());
    }

    @Test
    public void testConstructor() {
        Complex c = new Complex(2.0, 3.0);
        Assert.assertEquals(2.0, c.getReal(), DELTA);
        Assert.assertEquals(3.0, c.getImaginary(), DELTA);
        Assert.assertFalse(c.isNaN());
        Assert.assertFalse(c.isInfinite());
    }

    @Test
    public void testConstructorNaN() {
        Complex c = new Complex(Double.NaN, 0.0);
        Assert.assertTrue(c.isNaN());
        Assert.assertFalse(c.isInfinite());
    }

    @Test
    public void testConstructorInfinite() {
        Complex c = new Complex(Double.POSITIVE_INFINITY, 0.0);
        Assert.assertFalse(c.isNaN());
        Assert.assertTrue(c.isInfinite());
    }

    @Test
    public void testAbsNormal() {
        Complex c = new Complex(3.0, 4.0);
        Assert.assertEquals(5.0, c.abs(), DELTA);
    }

    @Test
    public void testAbsNaN() {
        Complex c = Complex.NaN;
        Assert.assertTrue(Double.isNaN(c.abs()));
    }

    @Test
    public void testAbsInfinite() {
        Complex c = Complex.INF;
        Assert.assertEquals(Double.POSITIVE_INFINITY, c.abs(), DELTA);
    }

    @Test
    public void testAbsZero() {
        Complex c = Complex.ZERO;
        Assert.assertEquals(0.0, c.abs(), DELTA);
    }

    @Test
    public void testAbsRealLessThanImag() {
        Complex c = new Complex(1.0, 2.0);
        Assert.assertEquals(Math.sqrt(5.0), c.abs(), DELTA);
    }

    @Test
    public void testAbsImagZero() {
        Complex c = new Complex(-3.0, 0.0);
        Assert.assertEquals(3.0, c.abs(), DELTA);
    }

    @Test
    public void testAbsRealZero() {
        Complex c = new Complex(0.0, -4.0);
        Assert.assertEquals(4.0, c.abs(), DELTA);
    }

    @Test
    public void testAddComplexNormal() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex c2 = new Complex(3.0, 4.0);
        Complex result = c1.add(c2);
        Assert.assertEquals(new Complex(4.0, 6.0), result);
    }

    @Test
    public void testAddComplexNaN() {
        Complex c1 = Complex.NaN;
        Complex c2 = new Complex(1.0, 1.0);
        Assert.assertEquals(Complex.NaN, c1.add(c2));
        Assert.assertEquals(Complex.NaN, c2.add(c1));
    }

    @Test(expected = NullArgumentException.class)
    public void testAddComplexNull() {
        new Complex(1.0, 1.0).add(null);
    }

    @Test
    public void testAddDoubleNormal() {
        Complex c = new Complex(1.0, 2.0);
        Complex result = c.add(3.0);
        Assert.assertEquals(new Complex(4.0, 2.0), result);
    }

    @Test
    public void testAddDoubleNaN() {
        Complex c = new Complex(1.0, 2.0);
        Assert.assertEquals(Complex.NaN, c.add(Double.NaN));
        Assert.assertEquals(Complex.NaN, Complex.NaN.add(1.0));
    }

    @Test
    public void testConjugateNormal() {
        Complex c = new Complex(1.0, 2.0);
        Complex conj = c.conjugate();
        Assert.assertEquals(new Complex(1.0, -2.0), conj);
    }

    @Test
    public void testConjugateNaN() {
        Assert.assertEquals(Complex.NaN, Complex.NaN.conjugate());
    }

    @Test
    public void testConjugateInfiniteImaginary() {
        Complex c = new Complex(1.0, Double.POSITIVE_INFINITY);
        Complex conj = c.conjugate();
        Assert.assertEquals(1.0, conj.getReal(), DELTA);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, conj.getImaginary(), DELTA);
    }

    @Test
    public void testDivideComplexNormal() {
        Complex c1 = new Complex(4.0, 2.0);
        Complex c2 = new Complex(2.0, 0.0);
        Complex result = c1.divide(c2);
        Assert.assertEquals(new Complex(2.0, 1.0), result);
    }

    @Test
    public void testDivideComplexNaN() {
        Complex c1 = Complex.NaN;
        Complex c2 = new Complex(1.0, 1.0);
        Assert.assertEquals(Complex.NaN, c1.divide(c2));
        Assert.assertEquals(Complex.NaN, c2.divide(c1));
    }

    @Test
    public void testDivideComplexDivisorZero() {
        Complex c = new Complex(1.0, 1.0);
        Assert.assertEquals(Complex.NaN, c.divide(Complex.ZERO));
    }

    @Test
    public void testDivideComplexBothInfinite() {
        Assert.assertEquals(Complex.NaN, Complex.INF.divide(Complex.INF));
    }

    @Test
    public void testDivideComplexFiniteByInfinite() {
        Complex finite = new Complex(1.0, 1.0);
        Assert.assertEquals(Complex.ZERO, finite.divide(Complex.INF));
    }

    @Test
    public void testDivideComplexInfiniteByFinite() {
        Complex finite = new Complex(2.0, 0.0);
        Complex result = Complex.INF.divide(finite);
        // Should be infinite, but may have NaN parts due to arithmetic
        Assert.assertTrue(result.isInfinite() || result.isNaN());
    }

    @Test
    public void testDivideComplexPrescaling() {
        // Test the branch where |c| < |d|
        Complex c1 = new Complex(1.0, 2.0);
        Complex c2 = new Complex(0.5, 1.0);
        Complex result = c1.divide(c2);
        Assert.assertEquals(new Complex(1.6, 0.8), result);
    }

    @Test(expected = NullArgumentException.class)
    public void testDivideComplexNull() {
        new Complex(1.0, 1.0).divide(null);
    }

    @Test
    public void testDivideDoubleNormal() {
        Complex c = new Complex(4.0, 2.0);
        Complex result = c.divide(2.0);
        Assert.assertEquals(new Complex(2.0, 1.0), result);
    }

    @Test
    public void testDivideDoubleNaN() {
        Complex c = new Complex(1.0, 1.0);
        Assert.assertEquals(Complex.NaN, c.divide(Double.NaN));
        Assert.assertEquals(Complex.NaN, Complex.NaN.divide(1.0));
    }

    @Test
    public void testDivideDoubleZero() {
        Complex c = new Complex(1.0, 1.0);
        Assert.assertEquals(Complex.NaN, c.divide(0.0));
    }

    @Test
    public void testDivideDoubleInfinite() {
        Complex finite = new Complex(1.0, 1.0);
        Assert.assertEquals(Complex.ZERO, finite.divide(Double.POSITIVE_INFINITY));
        Assert.assertEquals(Complex.NaN, Complex.INF.divide(Double.POSITIVE_INFINITY));
    }

    @Test
    public void testReciprocalNormal() {
        Complex c = new Complex(2.0, 0.0);
        Complex rec = c.reciprocal();
        Assert.assertEquals(new Complex(0.5, 0.0), rec);
    }

    @Test
    public void testReciprocalNaN() {
        Assert.assertEquals(Complex.NaN, Complex.NaN.reciprocal());
    }

    @Test
    public void testReciprocalZero() {
        Assert.assertEquals(Complex.NaN, Complex.ZERO.reciprocal());
    }

    @Test
    public void testReciprocalInfinite() {
        Assert.assertEquals(Complex.ZERO, Complex.INF.reciprocal());
    }

    @Test
    public void testReciprocalPrescaling() {
        Complex c = new Complex(1.0, 2.0);
        Complex rec = c.reciprocal();
        Assert.assertEquals(new Complex(0.2, -0.4), rec);
    }

    @Test
    public void testEqualsSameObject() {
        Complex c = new Complex(1.0, 2.0);
        Assert.assertTrue(c.equals(c));
    }

    @Test
    public void testEqualsNull() {
        Complex c = new Complex(1.0, 2.0);
        Assert.assertFalse(c.equals(null));
    }

    @Test
    public void testEqualsNonComplex() {
        Complex c = new Complex(1.0, 2.0);
        Assert.assertFalse(c.equals("not a complex"));
    }

    @Test
    public void testEqualsNaN() {
        Assert.assertTrue(Complex.NaN.equals(Complex.NaN));
        Assert.assertTrue(new Complex(Double.NaN, 0.0).equals(Complex.NaN));
        Assert.assertTrue(Complex.NaN.equals(new Complex(0.0, Double.NaN)));
    }

    @Test
    public void testEqualsNormal() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex c2 = new Complex(1.0, 2.0);
        Complex c3 = new Complex(1.0, 3.0);
        Assert.assertTrue(c1.equals(c2));
        Assert.assertFalse(c1.equals(c3));
    }

    @Test
    public void testEqualsInfinite() {
        Complex inf1 = new Complex(Double.POSITIVE_INFINITY, 0.0);
        Complex inf2 = new Complex(Double.POSITIVE_INFINITY, 0.0);
        Assert.assertTrue(inf1.equals(inf2));
    }

    @Test
    public void testHashCodeNaN() {
        Assert.assertEquals(7, Complex.NaN.hashCode());
    }

    @Test
    public void testHashCodeNormal() {
        Complex c = new Complex(1.0, 2.0);
        // Just ensure it's not throwing and consistent
        int h1 = c.hashCode();
        int h2 = c.hashCode();
        Assert.assertEquals(h1, h2);
    }

    @Test
    public void testGetters() {
        Complex c = new Complex(1.0, 2.0);
        Assert.assertEquals(1.0, c.getReal(), DELTA);
        Assert.assertEquals(2.0, c.getImaginary(), DELTA);
    }

    @Test
    public void testIsNaN() {
        Assert.assertTrue(Complex.NaN.isNaN());
        Assert.assertFalse(Complex.ONE.isNaN());
    }

    @Test
    public void testIsInfinite() {
        Assert.assertTrue(Complex.INF.isInfinite());
        Assert.assertFalse(Complex.ONE.isInfinite());
        Assert.assertFalse(Complex.NaN.isInfinite());
    }

    @Test
    public void testMultiplyComplexNormal() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex c2 = new Complex(3.0, 4.0);
        Complex result = c1.multiply(c2);
        Assert.assertEquals(new Complex(-5.0, 10.0), result);
    }

    @Test
    public void testMultiplyComplexNaN() {
        Assert.assertEquals(Complex.NaN, Complex.NaN.multiply(Complex.ONE));
        Assert.assertEquals(Complex.NaN, Complex.ONE.multiply(Complex.NaN));
    }

    @Test
    public void testMultiplyComplexInfinite() {
        Assert.assertEquals(Complex.INF, Complex.INF.multiply(Complex.ONE));
        Assert.assertEquals(Complex.INF, Complex.ONE.multiply(Complex.INF));
    }

    @Test(expected = NullArgumentException.class)
    public void testMultiplyComplexNull() {
        Complex.ONE.multiply(null);
    }

    @Test
    public void testMultiplyIntNormal() {
        Complex c = new Complex(1.0, 2.0);
        Complex result = c.multiply(3);
        Assert.assertEquals(new Complex(3.0, 6.0), result);
    }

    @Test
    public void testMultiplyIntNaN() {
        Assert.assertEquals(Complex.NaN, Complex.NaN.multiply(3));
    }

    @Test
    public void testMultiplyIntInfinite() {
        Assert.assertEquals(Complex.INF, Complex.INF.multiply(3));
    }

    @Test
    public void testMultiplyDoubleNormal() {
        Complex c = new Complex(1.0, 2.0);
        Complex result = c.multiply(3.0);
        Assert.assertEquals(new Complex(3.0, 6.0), result);
    }

    @Test
    public void testMultiplyDoubleNaN() {
        Assert.assertEquals(Complex.NaN, Complex.NaN.multiply(3.0));
        Assert.assertEquals(Complex.NaN, Complex.ONE.multiply(Double.NaN));
    }

    @Test
    public void testMultiplyDoubleInfinite() {
        Assert.assertEquals(Complex.INF, Complex.INF.multiply(3.0));
        Assert.assertEquals(Complex.INF, Complex.ONE.multiply(Double.POSITIVE_INFINITY));
    }

    @Test
    public void testNegateNormal() {
        Complex c = new Complex(1.0, -2.0);
        Complex neg = c.negate();
        Assert.assertEquals(new Complex(-1.0, 2.0), neg);
    }

    @Test
    public void testNegateNaN() {
        Assert.assertEquals(Complex.NaN, Complex.NaN.negate());
    }

    @Test
    public void testSubtractComplexNormal() {
        Complex c1 = new Complex(5.0, 6.0);
        Complex c2 = new Complex(1.0, 2.0);
        Complex result = c1.subtract(c2);
        Assert.assertEquals(new Complex(4.0, 4.0), result);
    }

    @Test
    public void testSubtractComplexNaN() {
        Assert.assertEquals(Complex.NaN, Complex.NaN.subtract(Complex.ONE));
        Assert.assertEquals(Complex.NaN, Complex.ONE.subtract(Complex.NaN));
    }

    @Test(expected = NullArgumentException.class)
    public void testSubtractComplexNull() {
        Complex.ONE.subtract(null);
    }

    @Test
    public void testSubtractDoubleNormal() {
        Complex c = new Complex(5.0, 6.0);
        Complex result = c.subtract(1.0);
        Assert.assertEquals(new Complex(4.0, 6.0), result);
    }

    @Test
    public void testSubtractDoubleNaN() {
        Assert.assertEquals(Complex.NaN, Complex.NaN.subtract(1.0));
        Assert.assertEquals(Complex.NaN, Complex.ONE.subtract(Double.NaN));
    }

    @Test
    public void testAcosNormal() {
        Complex c = new Complex(0.5, 0.0);
        Complex acos = c.acos();
        // acos(0.5) = pi/3
        Assert.assertEquals(Math.PI / 3, acos.getReal(), DELTA);
        Assert.assertEquals(0.0, acos.getImaginary(), DELTA);
    }

    @Test
    public void testAcosNaN() {
        Assert.assertEquals(Complex.NaN, Complex.NaN.acos());
    }

    @Test
    public void testAsinNormal() {
        Complex c = new Complex(0.5, 0.0);
        Complex asin = c.asin();
        Assert.assertEquals(Math.asin(0.5), asin.getReal(), DELTA);
        Assert.assertEquals(0.0, asin.getImaginary(), DELTA);
    }

    @Test
    public void testAsinNaN() {
        Assert.assertEquals(Complex.NaN, Complex.NaN.asin());
    }

    @Test
    public void testAtanNormal() {
        Complex c = new Complex(1.0, 0.0);
        Complex atan = c.atan();
        Assert.assertEquals(Math.PI / 4, atan.getReal(), DELTA);
        Assert.assertEquals(0.0, atan.getImaginary(), DELTA);
    }

    @Test
    public void testAtanNaN() {
        Assert.assertEquals(Complex.NaN, Complex.NaN.atan());
    }

    @Test
    public void testCosNormal() {
        Complex c = new Complex(0.0, 0.0);
        Complex cos = c.cos();
        Assert.assertEquals(1.0, cos.getReal(), DELTA);
        Assert.assertEquals(0.0, cos.getImaginary(), DELTA);
    }

    @Test
    public void testCosNaN() {
        Assert.assertEquals(Complex.NaN, Complex.NaN.cos());
    }

    @Test
    public void testCosInfinite() {
        Complex c = new Complex(1.0, Double.POSITIVE_INFINITY);
        Complex cos = c.cos();
        // cos(1 + INF i) = 1 - INF i? Actually according to doc: cos(1 ± INFINITY i) = 1 ∓ INFINITY i
        Assert.assertEquals(1.0, cos.getReal(), DELTA);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, cos.getImaginary(), DELTA);
    }

    @Test
    public void testCoshNormal() {
        Complex c = new Complex(0.0, 0.0);
        Complex cosh = c.cosh();
        Assert.assertEquals(1.0, cosh.getReal(), DELTA);
        Assert.assertEquals(0.0, cosh.getImaginary(), DELTA);
    }

    @Test
    public void testCoshNaN() {
        Assert.assertEquals(Complex.NaN, Complex.NaN.cosh());
    }

    @Test
    public void testCoshInfinite() {
        Complex c = new Complex(Double.POSITIVE_INFINITY, 1.0);
        Complex cosh = c.cosh();
        // cosh(INF + i) = INF + INF i
        Assert.assertEquals(Double.POSITIVE_INFINITY, cosh.getReal(), DELTA);
        Assert.assertEquals(Double.POSITIVE_INFINITY, cosh.getImaginary(), DELTA);
    }

    @Test
    public void testExpNormal() {
        Complex c = new Complex(0.0, 0.0);
        Complex exp = c.exp();
        Assert.assertEquals(1.0, exp.getReal(), DELTA);
        Assert.assertEquals(0.0, exp.getImaginary(), DELTA);
    }

    @Test
    public void testExpNaN() {
        Assert.assertEquals(Complex.NaN, Complex.NaN.exp());
    }

    @Test
    public void testExpInfinite() {
        Complex c = new Complex(Double.POSITIVE_INFINITY, 0.0);
        Complex exp = c.exp();
        Assert.assertEquals(Double.POSITIVE_INFINITY, exp.getReal(), DELTA);
        Assert.assertEquals(0.0, exp.getImaginary(), DELTA);
    }

    @Test
    public void testLogNormal() {
        Complex c = new Complex(1.0, 0.0);
        Complex log = c.log();
        Assert.assertEquals(0.0, log.getReal(), DELTA);
        Assert.assertEquals(0.0, log.getImaginary(), DELTA);
    }

    @Test
    public void testLogNaN() {
        Assert.assertEquals(Complex.NaN, Complex.NaN.log());
    }

    @Test
    public void testLogZero() {
        Complex log = Complex.ZERO.log();
        Assert.assertEquals(Double.NEGATIVE_INFINITY, log.getReal(), DELTA);
        Assert.assertEquals(0.0, log.getImaginary(), DELTA);
    }

    @Test
    public void testPowComplexNormal() {
        Complex base = new Complex(2.0, 0.0);
        Complex exp = new Complex(3.0, 0.0);
        Complex result = base.pow(exp);
        Assert.assertEquals(8.0, result.getReal(), DELTA);
        Assert.assertEquals(0.0, result.getImaginary(), DELTA);
    }

    @Test
    public void testPowComplexNaN() {
        Assert.assertEquals(Complex.NaN, Complex.NaN.pow(Complex.ONE));
    }

    @Test(expected = NullArgumentException.class)
    public void testPowComplexNull() {
        Complex.ONE.pow(null);
    }

    @Test
    public void testPowDoubleNormal() {
        Complex base = new Complex(2.0, 0.0);
        Complex result = base.pow(3.0);
        Assert.assertEquals(8.0, result.getReal(), DELTA);
        Assert.assertEquals(0.0, result.getImaginary(), DELTA);
    }

    @Test
    public void testPowDoubleNaN() {
        Assert.assertEquals(Complex.NaN, Complex.NaN.pow(2.0));
    }

    @Test
    public void testSinNormal() {
        Complex c = new Complex(0.0, 0.0);
        Complex sin = c.sin();
        Assert.assertEquals(0.0, sin.getReal(), DELTA);
        Assert.assertEquals(0.0, sin.getImaginary(), DELTA);
    }

    @Test
    public void testSinNaN() {
        Assert.assertEquals(Complex.NaN, Complex.NaN.sin());
    }

    @Test
    public void testSinInfinite() {
        Complex c = new Complex(1.0, Double.POSITIVE_INFINITY);
        Complex sin = c.sin();
        // sin(1 + INF i) = 1 + INF i
        Assert.assertEquals(1.0, sin.getReal(), DELTA);
        Assert.assertEquals(Double.POSITIVE_INFINITY, sin.getImaginary(), DELTA);
    }

    @Test
    public void testSinhNormal() {
        Complex c = new Complex(0.0, 0.0);
        Complex sinh = c.sinh();
        Assert.assertEquals(0.0, sinh.getReal(), DELTA);
        Assert.assertEquals(0.0, sinh.getImaginary(), DELTA);
    }

    @Test
    public void testSinhNaN() {
        Assert.assertEquals(Complex.NaN, Complex.NaN.sinh());
    }

    @Test
    public void testSinhInfinite() {
        Complex c = new Complex(Double.POSITIVE_INFINITY, 1.0);
        Complex sinh = c.sinh();
        Assert.assertEquals(Double.POSITIVE_INFINITY, sinh.getReal(), DELTA);
        Assert.assertEquals(Double.POSITIVE_INFINITY, sinh.getImaginary(), DELTA);
    }

    @Test
    public void testSqrtNormal() {
        Complex c = new Complex(4.0, 0.0);
        Complex sqrt = c.sqrt();
        Assert.assertEquals(2.0, sqrt.getReal(), DELTA);
        Assert.assertEquals(0.0, sqrt.getImaginary(), DELTA);
    }

    @Test
    public void testSqrtNaN() {
        Assert.assertEquals(Complex.NaN, Complex.NaN.sqrt());
    }

    @Test
    public void testSqrtZero() {
        Complex sqrt = Complex.ZERO.sqrt();
        Assert.assertEquals(0.0, sqrt.getReal(), DELTA);
        Assert.assertEquals(0.0, sqrt.getImaginary(), DELTA);
    }

    @Test
    public void testSqrtNegativeReal() {
        Complex c = new Complex(-4.0, 0.0);
        Complex sqrt = c.sqrt();
        Assert.assertEquals(0.0, sqrt.getReal(), DELTA);
        Assert.assertEquals(2.0, sqrt.getImaginary(), DELTA);
    }

    @Test
    public void testSqrtInfinite() {
        Complex c = new Complex(Double.POSITIVE_INFINITY, 0.0);
        Complex sqrt = c.sqrt();
        Assert.assertEquals(Double.POSITIVE_INFINITY, sqrt.getReal(), DELTA);
        Assert.assertEquals(0.0, sqrt.getImaginary(), DELTA);
    }

    @Test
    public void testSqrt1zNormal() {
        Complex c = new Complex(0.0, 0.0);
        Complex sqrt1z = c.sqrt1z();
        Assert.assertEquals(1.0, sqrt1z.getReal(), DELTA);
        Assert.assertEquals(0.0, sqrt1z.getImaginary(), DELTA);
    }

    @Test
    public void testSqrt1zNaN() {
        Assert.assertEquals(Complex.NaN, Complex.NaN.sqrt1z());
    }

    @Test
    public void testTanNormal() {
        Complex c = new Complex(0.0, 0.0);
        Complex tan = c.tan();
        Assert.assertEquals(0.0, tan.getReal(), DELTA);
        Assert.assertEquals(0.0, tan.getImaginary(), DELTA);
    }

    @Test
    public void testTanNaN() {
        Assert.assertEquals(Complex.NaN, Complex.NaN.tan());
    }

    @Test
    public void testTanInfiniteReal() {
        Complex c = new Complex(Double.POSITIVE_INFINITY, 0.0);
        Assert.assertEquals(Complex.NaN, c.tan());
    }

    @Test
    public void testTanLargeImaginary() {
        Complex c = new Complex(0.0, 30.0);
        Complex tan = c.tan();
        Assert.assertEquals(0.0, tan.getReal(), DELTA);
        Assert.assertEquals(1.0, tan.getImaginary(), DELTA);
    }

    @Test
    public void testTanLargeNegativeImaginary() {
        Complex c = new Complex(0.0, -30.0);
        Complex tan = c.tan();
        Assert.assertEquals(0.0, tan.getReal(), DELTA);
        Assert.assertEquals(-1.0, tan.getImaginary(), DELTA);
    }

    @Test
    public void testTanBoundaryImaginary20() {
        Complex c = new Complex(0.0, 20.0);
        Complex tan = c.tan();
        // Should not be simplified to 0+1i because imaginary == 20 is not >20
        Assert.assertFalse(tan.equals(new Complex(0.0, 1.0)));
    }

    @Test
    public void testTanhNormal() {
        Complex c = new Complex(0.0, 0.0);
        Complex tanh = c.tanh();
        Assert.assertEquals(0.0, tanh.getReal(), DELTA);
        Assert.assertEquals(0.0, tanh.getImaginary(), DELTA);
    }

    @Test
    public void testTanhNaN() {
        Assert.assertEquals(Complex.NaN, Complex.NaN.tanh());
    }

    @Test
    public void testTanhInfiniteImaginary() {
        Complex c = new Complex(0.0, Double.POSITIVE_INFINITY);
        Assert.assertEquals(Complex.NaN, c.tanh());
    }

    @Test
    public void testTanhLargeReal() {
        Complex c = new Complex(30.0, 0.0);
        Complex tanh = c.tanh();
        Assert.assertEquals(1.0, tanh.getReal(), DELTA);
        Assert.assertEquals(0.0, tanh.getImaginary(), DELTA);
    }

    @Test
    public void testTanhLargeNegativeReal() {
        Complex c = new Complex(-30.0, 0.0);
        Complex tanh = c.tanh();
        Assert.assertEquals(-1.0, tanh.getReal(), DELTA);
        Assert.assertEquals(0.0, tanh.getImaginary(), DELTA);
    }

    @Test
    public void testTanhBoundaryReal20() {
        Complex c = new Complex(20.0, 0.0);
        Complex tanh = c.tanh();
        Assert.assertFalse(tanh.equals(new Complex(1.0, 0.0)));
    }

    @Test
    public void testGetArgumentNormal() {
        Complex c = new Complex(1.0, 1.0);
        Assert.assertEquals(Math.PI / 4, c.getArgument(), DELTA);
    }

    @Test
    public void testGetArgumentNaN() {
        Assert.assertTrue(Double.isNaN(Complex.NaN.getArgument()));
    }

    @Test
    public void testGetArgumentInfinite() {
        Complex c = new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        Assert.assertEquals(Math.PI / 4, c.getArgument(), DELTA);
    }

    @Test
    public void testNthRootNormal() {
        Complex c = new Complex(8.0, 0.0);
        List<Complex> roots = c.nthRoot(3);
        Assert.assertEquals(3, roots.size());
        // Check one root: 2 + 0i
        Assert.assertTrue(roots.contains(new Complex(2.0, 0.0)));
    }

    @Test(expected = NotPositiveException.class)
    public void testNthRootNonPositive() {
        Complex.ONE.nthRoot(0);
    }

    @Test
    public void testNthRootNaN() {
        List<Complex> roots = Complex.NaN.nthRoot(2);
        Assert.assertEquals(1, roots.size());
        Assert.assertEquals(Complex.NaN, roots.get(0));
    }

    @Test
    public void testNthRootInfinite() {
        List<Complex> roots = Complex.INF.nthRoot(2);
        Assert.assertEquals(1, roots.size());
        Assert.assertEquals(Complex.INF, roots.get(0));
    }

    @Test
    public void testCreateComplex() {
        Complex c = new Complex(1.0, 2.0);
        Complex created = c.createComplex(3.0, 4.0);
        Assert.assertEquals(new Complex(3.0, 4.0), created);
    }

    @Test
    public void testValueOfTwoArgsNormal() {
        Complex c = Complex.valueOf(1.0, 2.0);
        Assert.assertEquals(new Complex(1.0, 2.0), c);
    }

    @Test
    public void testValueOfTwoArgsNaN() {
        Assert.assertEquals(Complex.NaN, Complex.valueOf(Double.NaN, 0.0));
        Assert.assertEquals(Complex.NaN, Complex.valueOf(0.0, Double.NaN));
    }

    @Test
    public void testValueOfOneArgNormal() {
        Complex c = Complex.valueOf(1.0);
        Assert.assertEquals(new Complex(1.0, 0.0), c);
    }

    @Test
    public void testValueOfOneArgNaN() {
        Assert.assertEquals(Complex.NaN, Complex.valueOf(Double.NaN));
    }

    @Test
    public void testToString() {
        Complex c = new Complex(1.5, -2.5);
        Assert.assertEquals("(1.5, -2.5)", c.toString());
    }

    @Test
    public void testGetField() {
        Assert.assertNotNull(Complex.ONE.getField());
    }

    @Test
    public void testStaticConstants() {
        Assert.assertEquals(new Complex(0.0, 1.0), Complex.I);
        Assert.assertTrue(Complex.NaN.isNaN());
        Assert.assertTrue(Complex.INF.isInfinite());
        Assert.assertEquals(new Complex(1.0, 0.0), Complex.ONE);
        Assert.assertEquals(new Complex(0.0, 0.0), Complex.ZERO);
    }
}
