package org.apache.commons.math.complex;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import org.apache.commons.math.exception.NotPositiveException;
import org.apache.commons.math.exception.NullArgumentException;
import org.junit.Assert;
import org.junit.Test;

public class ComplexTest {

    private static final double EPS = 1e-12;

    @Test
    public void testConstructorsAndAccessors() {
        Complex c1 = new Complex(3.0);
        Assert.assertEquals(3.0, c1.getReal(), EPS);
        Assert.assertEquals(0.0, c1.getImaginary(), EPS);
        Assert.assertFalse(c1.isNaN());
        Assert.assertFalse(c1.isInfinite());

        Complex c2 = new Complex(3.0, 4.0);
        Assert.assertEquals(3.0, c2.getReal(), EPS);
        Assert.assertEquals(4.0, c2.getImaginary(), EPS);

        Complex cNan = new Complex(Double.NaN, 1.0);
        Assert.assertTrue(cNan.isNaN());
        Assert.assertFalse(cNan.isInfinite());

        Complex cInf = new Complex(1.0, Double.POSITIVE_INFINITY);
        Assert.assertFalse(cInf.isNaN());
        Assert.assertTrue(cInf.isInfinite());
    }

    @Test
    public void testAbs() {
        Assert.assertTrue(Double.isNaN(Complex.NaN.abs()));
        Assert.assertEquals(Double.POSITIVE_INFINITY, Complex.INF.abs(), EPS);
        Assert.assertEquals(Double.POSITIVE_INFINITY, new Complex(Double.NEGATIVE_INFINITY, 0.0).abs(), EPS);

        Assert.assertEquals(5.0, new Complex(3.0, 4.0).abs(), EPS);
        Assert.assertEquals(5.0, new Complex(4.0, 3.0).abs(), EPS);
        Assert.assertEquals(0.0, Complex.ZERO.abs(), EPS);
        Assert.assertEquals(0.0, new Complex(0.0, 0.0).abs(), EPS);
        Assert.assertEquals(4.0, new Complex(0.0, 4.0).abs(), EPS);
        Assert.assertEquals(3.0, new Complex(3.0, 0.0).abs(), EPS);
    }

    @Test
    public void testAdd() {
        Complex x = new Complex(3.0, 4.0);
        Complex y = new Complex(1.0, 2.0);
        Complex res = x.add(y);
        Assert.assertEquals(4.0, res.getReal(), EPS);
        Assert.assertEquals(6.0, res.getImaginary(), EPS);

        Assert.assertEquals(Complex.NaN, x.add(Complex.NaN));
        Assert.assertEquals(Complex.NaN, Complex.NaN.add(x));

        Complex dRes = x.add(2.0);
        Assert.assertEquals(5.0, dRes.getReal(), EPS);
        Assert.assertEquals(4.0, dRes.getImaginary(), EPS);

        Assert.assertEquals(Complex.NaN, x.add(Double.NaN));
        Assert.assertEquals(Complex.NaN, Complex.NaN.add(2.0));
    }

    @Test(expected = NullArgumentException.class)
    public void testAddNull() {
        Complex.ONE.add(null);
    }

    @Test
    public void testConjugate() {
        Complex x = new Complex(3.0, 4.0);
        Complex conj = x.conjugate();
        Assert.assertEquals(3.0, conj.getReal(), EPS);
        Assert.assertEquals(-4.0, conj.getImaginary(), EPS);
        Assert.assertEquals(Complex.NaN, Complex.NaN.conjugate());
    }

    @Test
    public void testDivide() {
        Complex x = new Complex(3.0, 4.0);
        Complex y = new Complex(1.0, 2.0);
        Complex res1 = x.divide(y);
        Assert.assertEquals(2.2, res1.getReal(), EPS);
        Assert.assertEquals(-0.2, res1.getImaginary(), EPS);

        Complex z = new Complex(2.0, 1.0);
        Complex res2 = x.divide(z);
        Assert.assertEquals(2.0, res2.getReal(), EPS);
        Assert.assertEquals(1.0, res2.getImaginary(), EPS);

        Assert.assertEquals(Complex.NaN, x.divide(Complex.NaN));
        Assert.assertEquals(Complex.NaN, Complex.NaN.divide(x));
        Assert.assertEquals(Complex.NaN, x.divide(Complex.ZERO));

        Assert.assertEquals(Complex.ZERO, x.divide(Complex.INF));
        Assert.assertEquals(Complex.NaN, Complex.INF.divide(Complex.INF));
    }

    @Test(expected = NullArgumentException.class)
    public void testDivideNull() {
        Complex.ONE.divide(null);
    }

    @Test
    public void testDivideDouble() {
        Complex x = new Complex(3.0, 4.0);
        Complex res = x.divide(2.0);
        Assert.assertEquals(1.5, res.getReal(), EPS);
        Assert.assertEquals(2.0, res.getImaginary(), EPS);

        Assert.assertEquals(Complex.NaN, x.divide(0.0));
        Assert.assertEquals(Complex.NaN, x.divide(Double.NaN));
        Assert.assertEquals(Complex.NaN, Complex.NaN.divide(2.0));
        Assert.assertEquals(Complex.ZERO, x.divide(Double.POSITIVE_INFINITY));
        Assert.assertEquals(Complex.NaN, Complex.INF.divide(Double.POSITIVE_INFINITY));
    }

    @Test
    public void testReciprocal() {
        Assert.assertEquals(Complex.NaN, Complex.NaN.reciprocal());
        Assert.assertEquals(Complex.NaN, Complex.ZERO.reciprocal());
        Assert.assertEquals(Complex.ZERO, Complex.INF.reciprocal());

        Complex c1 = new Complex(1.0, 2.0).reciprocal();
        Assert.assertEquals(0.2, c1.getReal(), EPS);
        Assert.assertEquals(-0.4, c1.getImaginary(), EPS);

        Complex c2 = new Complex(2.0, 1.0).reciprocal();
        Assert.assertEquals(0.4, c2.getReal(), EPS);
        Assert.assertEquals(-0.2, c2.getImaginary(), EPS);
    }

    @Test
    public void testEqualsAndHashCode() {
        Complex c1 = new Complex(3.0, 4.0);
        Complex c2 = new Complex(3.0, 4.0);
        Complex c3 = new Complex(3.0, 5.0);
        Complex c4 = new Complex(4.0, 4.0);

        Assert.assertTrue(c1.equals(c1));
        Assert.assertTrue(c1.equals(c2));
        Assert.assertEquals(c1.hashCode(), c2.hashCode());

        Assert.assertFalse(c1.equals(null));
        Assert.assertFalse(c1.equals("test"));
        Assert.assertFalse(c1.equals(c3));
        Assert.assertFalse(c1.equals(c4));

        Complex nan1 = new Complex(Double.NaN, 0.0);
        Complex nan2 = new Complex(0.0, Double.NaN);
        Assert.assertTrue(nan1.equals(nan2));
        Assert.assertTrue(nan1.equals(Complex.NaN));
        Assert.assertFalse(c1.equals(nan1));
        Assert.assertFalse(nan1.equals(c1));
        Assert.assertEquals(Complex.NaN.hashCode(), nan1.hashCode());
        Assert.assertEquals(7, Complex.NaN.hashCode());
    }

    @Test
    public void testMultiply() {
        Complex x = new Complex(3.0, 4.0);
        Complex y = new Complex(1.0, 2.0);
        Complex res = x.multiply(y);
        Assert.assertEquals(-5.0, res.getReal(), EPS);
        Assert.assertEquals(10.0, res.getImaginary(), EPS);

        Assert.assertEquals(Complex.NaN, x.multiply(Complex.NaN));
        Assert.assertEquals(Complex.NaN, Complex.NaN.multiply(x));
        Assert.assertEquals(Complex.INF, x.multiply(Complex.INF));
        Assert.assertEquals(Complex.INF, Complex.INF.multiply(x));
        Assert.assertEquals(Complex.INF, new Complex(Double.POSITIVE_INFINITY, 0.0).multiply(new Complex(1.0, 0.0)));
        Assert.assertEquals(Complex.INF, new Complex(0.0, Double.POSITIVE_INFINITY).multiply(new Complex(1.0, 0.0)));
        Assert.assertEquals(Complex.INF, new Complex(1.0, 0.0).multiply(new Complex(Double.POSITIVE_INFINITY, 0.0)));
        Assert.assertEquals(Complex.INF, new Complex(1.0, 0.0).multiply(new Complex(0.0, Double.POSITIVE_INFINITY)));
    }

    @Test(expected = NullArgumentException.class)
    public void testMultiplyNull() {
        Complex.ONE.multiply((Complex) null);
    }

    @Test
    public void testMultiplyInt() {
        Complex x = new Complex(3.0, 4.0);
        Complex res = x.multiply(2);
        Assert.assertEquals(6.0, res.getReal(), EPS);
        Assert.assertEquals(8.0, res.getImaginary(), EPS);

        Assert.assertEquals(Complex.NaN, Complex.NaN.multiply(2));
        Assert.assertEquals(Complex.INF, Complex.INF.multiply(2));
        Assert.assertEquals(Complex.INF, new Complex(Double.POSITIVE_INFINITY, 0).multiply(2));
        Assert.assertEquals(Complex.INF, new Complex(0, Double.POSITIVE_INFINITY).multiply(2));
    }

    @Test
    public void testMultiplyDouble() {
        Complex x = new Complex(3.0, 4.0);
        Complex res = x.multiply(2.5);
        Assert.assertEquals(7.5, res.getReal(), EPS);
        Assert.assertEquals(10.0, res.getImaginary(), EPS);

        Assert.assertEquals(Complex.NaN, x.multiply(Double.NaN));
        Assert.assertEquals(Complex.NaN, Complex.NaN.multiply(2.5));
        Assert.assertEquals(Complex.INF, Complex.INF.multiply(2.5));
        Assert.assertEquals(Complex.INF, x.multiply(Double.POSITIVE_INFINITY));
        Assert.assertEquals(Complex.INF, new Complex(Double.POSITIVE_INFINITY, 0).multiply(1.0));
        Assert.assertEquals(Complex.INF, new Complex(0, Double.POSITIVE_INFINITY).multiply(1.0));
    }

    @Test
    public void testNegate() {
        Complex x = new Complex(3.0, -4.0);
        Complex neg = x.negate();
        Assert.assertEquals(-3.0, neg.getReal(), EPS);
        Assert.assertEquals(4.0, neg.getImaginary(), EPS);
        Assert.assertEquals(Complex.NaN, Complex.NaN.negate());
    }

    @Test
    public void testSubtract() {
        Complex x = new Complex(3.0, 4.0);
        Complex y = new Complex(1.0, 2.0);
        Complex res = x.subtract(y);
        Assert.assertEquals(2.0, res.getReal(), EPS);
        Assert.assertEquals(2.0, res.getImaginary(), EPS);

        Assert.assertEquals(Complex.NaN, x.subtract(Complex.NaN));
        Assert.assertEquals(Complex.NaN, Complex.NaN.subtract(x));

        Complex dRes = x.subtract(2.0);
        Assert.assertEquals(1.0, dRes.getReal(), EPS);
        Assert.assertEquals(4.0, dRes.getImaginary(), EPS);

        Assert.assertEquals(Complex.NaN, x.subtract(Double.NaN));
        Assert.assertEquals(Complex.NaN, Complex.NaN.subtract(2.0));
    }

    @Test(expected = NullArgumentException.class)
    public void testSubtractNull() {
        Complex.ONE.subtract(null);
    }

    @Test
    public void testAcos() {
        Assert.assertEquals(Complex.NaN, Complex.NaN.acos());
        Complex c = new Complex(0.5, 0.5);
        Complex acos = c.acos();
        Assert.assertEquals(c, acos.cos());
    }

    @Test
    public void testAsin() {
        Assert.assertEquals(Complex.NaN, Complex.NaN.asin());
        Complex c = new Complex(0.5, 0.5);
        Complex asin = c.asin();
        Assert.assertEquals(c, asin.sin());
    }

    @Test
    public void testAtan() {
        Assert.assertEquals(Complex.NaN, Complex.NaN.atan());
        Complex c = new Complex(0.5, 0.5);
        Complex atan = c.atan();
        Assert.assertEquals(c, atan.tan());
    }

    @Test
    public void testCosAndCosh() {
        Assert.assertEquals(Complex.NaN, Complex.NaN.cos());
        Assert.assertEquals(Complex.NaN, Complex.NaN.cosh());

        Complex c = new Complex(1.0, 2.0);
        Complex cos = c.cos();
        Assert.assertTrue(Math.abs(cos.getReal()) > 0);

        Complex cosh = c.cosh();
        Assert.assertTrue(Math.abs(cosh.getReal()) > 0);
    }

    @Test
    public void testExpAndLog() {
        Assert.assertEquals(Complex.NaN, Complex.NaN.exp());
        Assert.assertEquals(Complex.NaN, Complex.NaN.log());

        Complex c = new Complex(1.0, 2.0);
        Complex expLog = c.exp().log();
        Assert.assertEquals(c.getReal(), expLog.getReal(), 1e-10);
        Assert.assertEquals(c.getImaginary(), expLog.getImaginary(), 1e-10);
    }

    @Test
    public void testPow() {
        Complex base = new Complex(2.0, 1.0);
        Complex exp = new Complex(3.0, 2.0);
        Complex res = base.pow(exp);
        Assert.assertTrue(Math.abs(res.getReal()) > 0);

        Complex dRes = base.pow(2.0);
        Assert.assertEquals(base.multiply(base).getReal(), dRes.getReal(), EPS);
        Assert.assertEquals(base.multiply(base).getImaginary(), dRes.getImaginary(), EPS);
    }

    @Test(expected = NullArgumentException.class)
    public void testPowNull() {
        Complex.ONE.pow((Complex) null);
    }

    @Test
    public void testSinAndSinh() {
        Assert.assertEquals(Complex.NaN, Complex.NaN.sin());
        Assert.assertEquals(Complex.NaN, Complex.NaN.sinh());

        Complex c = new Complex(1.0, 2.0);
        Complex sin = c.sin();
        Assert.assertTrue(Math.abs(sin.getReal()) > 0);

        Complex sinh = c.sinh();
        Assert.assertTrue(Math.abs(sinh.getReal()) > 0);
    }

    @Test
    public void testSqrt() {
        Assert.assertEquals(Complex.NaN, Complex.NaN.sqrt());
        Assert.assertEquals(Complex.ZERO, Complex.ZERO.sqrt());

        Complex cPos = new Complex(3.0, 4.0);
        Complex sqrtPos = cPos.sqrt();
        Assert.assertEquals(2.0, sqrtPos.getReal(), EPS);
        Assert.assertEquals(1.0, sqrtPos.getImaginary(), EPS);

        Complex cNeg = new Complex(-3.0, 4.0);
        Complex sqrtNeg = cNeg.sqrt();
        Assert.assertEquals(1.0, sqrtNeg.getReal(), EPS);
        Assert.assertEquals(2.0, sqrtNeg.getImaginary(), EPS);

        Complex cNegImag = new Complex(-3.0, -4.0);
        Complex sqrtNegImag = cNegImag.sqrt();
        Assert.assertEquals(1.0, sqrtNegImag.getReal(), EPS);
        Assert.assertEquals(-2.0, sqrtNegImag.getImaginary(), EPS);
    }

    @Test
    public void testTanAndTanh() {
        Assert.assertEquals(Complex.NaN, Complex.NaN.tan());
        Assert.assertEquals(Complex.NaN, Complex.NaN.tanh());

        Complex c = new Complex(1.0, 2.0);
        Complex tan = c.tan();
        Assert.assertTrue(Math.abs(tan.getReal()) > 0);

        Complex tanh = c.tanh();
        Assert.assertTrue(Math.abs(tanh.getReal()) > 0);
    }

    @Test
    public void testGetArgument() {
        Complex c = new Complex(1.0, 1.0);
        Assert.assertEquals(Math.PI / 4.0, c.getArgument(), EPS);
        Assert.assertTrue(Double.isNaN(Complex.NaN.getArgument()));
    }

    @Test
    public void testNthRoot() {
        Complex c = new Complex(0.0, 8.0);
        List<Complex> roots = c.nthRoot(3);
        Assert.assertEquals(3, roots.size());

        List<Complex> nanRoots = Complex.NaN.nthRoot(2);
        Assert.assertEquals(1, nanRoots.size());
        Assert.assertEquals(Complex.NaN, nanRoots.get(0));

        List<Complex> infRoots = Complex.INF.nthRoot(2);
        Assert.assertEquals(1, infRoots.size());
        Assert.assertEquals(Complex.INF, infRoots.get(0));
    }

    @Test(expected = NotPositiveException.class)
    public void testNthRootZeroOrNegative() {
        Complex.ONE.nthRoot(0);
    }

    @Test
    public void testValueOf() {
        Complex c = Complex.valueOf(1.0, 2.0);
        Assert.assertEquals(1.0, c.getReal(), EPS);
        Assert.assertEquals(2.0, c.getImaginary(), EPS);

        Assert.assertEquals(Complex.NaN, Complex.valueOf(Double.NaN, 1.0));
        Assert.assertEquals(Complex.NaN, Complex.valueOf(1.0, Double.NaN));

        Complex cReal = Complex.valueOf(3.0);
        Assert.assertEquals(3.0, cReal.getReal(), EPS);
        Assert.assertEquals(0.0, cReal.getImaginary(), EPS);
        Assert.assertEquals(Complex.NaN, Complex.valueOf(Double.NaN));
    }

    @Test
    public void testToStringAndField() {
        Complex c = new Complex(1.0, 2.0);
        Assert.assertEquals("(1.0, 2.0)", c.toString());
        Assert.assertEquals(ComplexField.getInstance(), c.getField());
    }

    @Test
    public void testSerialization() throws Exception {
        Complex c = new Complex(3.0, 4.0);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(c);
        oos.close();

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        Complex deserialized = (Complex) ois.readObject();
        Assert.assertEquals(c, deserialized);
        Assert.assertFalse(deserialized.isNaN());
        Assert.assertFalse(deserialized.isInfinite());
    }
}
