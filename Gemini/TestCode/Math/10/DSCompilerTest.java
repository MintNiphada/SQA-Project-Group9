package org.apache.commons.math3.analysis.differentiation;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.util.FastMath;
import org.junit.Assert;
import org.junit.Test;

public class DSCompilerTest {

    private static final double EPS = 1e-12;

    @Test
    public void testGetCompilerCachingAndExpansion() {
        DSCompiler c1 = DSCompiler.getCompiler(0, 0);
        Assert.assertNotNull(c1);
        Assert.assertEquals(0, c1.getFreeParameters());
        Assert.assertEquals(0, c1.getOrder());
        Assert.assertEquals(1, c1.getSize());

        DSCompiler c1Bis = DSCompiler.getCompiler(0, 0);
        Assert.assertSame(c1, c1Bis);

        DSCompiler c2 = DSCompiler.getCompiler(3, 4);
        Assert.assertEquals(3, c2.getFreeParameters());
        Assert.assertEquals(4, c2.getOrder());
        Assert.assertEquals(35, c2.getSize());

        DSCompiler c3 = DSCompiler.getCompiler(2, 2);
        Assert.assertEquals(2, c3.getFreeParameters());
        Assert.assertEquals(2, c3.getOrder());
        Assert.assertEquals(6, c3.getSize());

        DSCompiler c4 = DSCompiler.getCompiler(5, 5);
        Assert.assertEquals(5, c4.getFreeParameters());
        Assert.assertEquals(5, c4.getOrder());
        Assert.assertNotNull(c4);
    }

    @Test
    public void testSizesAndDerivativeIndices() {
        DSCompiler c0 = DSCompiler.getCompiler(0, 3);
        Assert.assertEquals(1, c0.getSize());
        Assert.assertEquals(0, c0.getPartialDerivativeIndex());
        Assert.assertArrayEquals(new int[0], c0.getPartialDerivativeOrders(0));

        DSCompiler c1 = DSCompiler.getCompiler(1, 3);
        Assert.assertEquals(4, c1.getSize());
        Assert.assertEquals(0, c1.getPartialDerivativeIndex(0));
        Assert.assertEquals(1, c1.getPartialDerivativeIndex(1));
        Assert.assertEquals(2, c1.getPartialDerivativeIndex(2));
        Assert.assertEquals(3, c1.getPartialDerivativeIndex(3));
        Assert.assertArrayEquals(new int[] { 0 }, c1.getPartialDerivativeOrders(0));
        Assert.assertArrayEquals(new int[] { 1 }, c1.getPartialDerivativeOrders(1));
        Assert.assertArrayEquals(new int[] { 2 }, c1.getPartialDerivativeOrders(2));
        Assert.assertArrayEquals(new int[] { 3 }, c1.getPartialDerivativeOrders(3));

        DSCompiler c2 = DSCompiler.getCompiler(2, 2);
        Assert.assertEquals(0, c2.getPartialDerivativeIndex(0, 0));
        Assert.assertEquals(1, c2.getPartialDerivativeIndex(1, 0));
        Assert.assertEquals(2, c2.getPartialDerivativeIndex(2, 0));
        Assert.assertEquals(3, c2.getPartialDerivativeIndex(0, 1));
        Assert.assertEquals(4, c2.getPartialDerivativeIndex(1, 1));
        Assert.assertEquals(5, c2.getPartialDerivativeIndex(0, 2));

        for (int i = 0; i < c2.getSize(); ++i) {
            int[] orders = c2.getPartialDerivativeOrders(i);
            Assert.assertEquals(i, c2.getPartialDerivativeIndex(orders));
        }
    }

    @Test(expected = DimensionMismatchException.class)
    public void testGetPartialDerivativeIndexDimensionMismatch() {
        DSCompiler c = DSCompiler.getCompiler(2, 2);
        c.getPartialDerivativeIndex(1, 0, 0);
    }

    @Test(expected = NumberIsTooLargeException.class)
    public void testGetPartialDerivativeIndexTooLarge() {
        DSCompiler c = DSCompiler.getCompiler(2, 2);
        c.getPartialDerivativeIndex(2, 1);
    }

    @Test
    public void testCheckCompatibility() {
        DSCompiler c1 = DSCompiler.getCompiler(2, 3);
        DSCompiler c2 = DSCompiler.getCompiler(2, 3);
        DSCompiler c3 = DSCompiler.getCompiler(1, 3);
        DSCompiler c4 = DSCompiler.getCompiler(2, 2);

        c1.checkCompatibility(c2);

        try {
            c1.checkCompatibility(c3);
            Assert.fail("Expected DimensionMismatchException");
        } catch (DimensionMismatchException e) {
            Assert.assertEquals(2, e.getArgument());
            Assert.assertEquals(1, (int) (Integer) e.getDimension());
        }

        try {
            c1.checkCompatibility(c4);
            Assert.fail("Expected DimensionMismatchException");
        } catch (DimensionMismatchException e) {
            Assert.assertEquals(3, e.getArgument());
            Assert.assertEquals(2, (int) (Integer) e.getDimension());
        }
    }

    @Test
    public void testLinearCombinations() {
        DSCompiler c = DSCompiler.getCompiler(1, 2);
        double[] x = new double[] { 2.0, 1.0, 0.0 };
        double[] y = new double[] { 3.0, 0.0, 0.0 };
        double[] z = new double[] { 4.0, 2.0, 0.0 };
        double[] w = new double[] { 5.0, -1.0, 0.0 };

        double[] r2 = new double[3];
        c.linearCombination(2.0, x, 0, -1.0, y, 0, r2, 0);
        Assert.assertEquals(1.0, r2[0], EPS);
        Assert.assertEquals(2.0, r2[1], EPS);

        double[] r3 = new double[3];
        c.linearCombination(2.0, x, 0, 3.0, y, 0, -1.0, z, 0, r3, 0);
        Assert.assertEquals(9.0, r3[0], EPS);
        Assert.assertEquals(0.0, r3[1], EPS);

        double[] r4 = new double[3];
        c.linearCombination(1.0, x, 0, 2.0, y, 0, 3.0, z, 0, 4.0, w, 0, r4, 0);
        Assert.assertEquals(40.0, r4[0], EPS);
        Assert.assertEquals(3.0, r4[1], EPS);
    }

    @Test
    public void testAddSubtract() {
        DSCompiler c = DSCompiler.getCompiler(1, 2);
        double[] x = new double[] { 3.0, 2.0, 1.0 };
        double[] y = new double[] { 1.5, 0.5, 2.0 };
        double[] resAdd = new double[3];
        double[] resSub = new double[3];

        c.add(x, 0, y, 0, resAdd, 0);
        Assert.assertEquals(4.5, resAdd[0], EPS);
        Assert.assertEquals(2.5, resAdd[1], EPS);
        Assert.assertEquals(3.0, resAdd[2], EPS);

        c.subtract(x, 0, y, 0, resSub, 0);
        Assert.assertEquals(1.5, resSub[0], EPS);
        Assert.assertEquals(1.5, resSub[1], EPS);
        Assert.assertEquals(-1.0, resSub[2], EPS);
    }

    @Test
    public void testMultiplyDivide() {
        DSCompiler c = DSCompiler.getCompiler(1, 2);
        double[] x = new double[] { 3.0, 1.0, 0.0 }; // u(t) = t + 1 at t=2 -> u=3, u'=1, u''=0
        double[] y = new double[] { 2.0, 2.0, 0.0 }; // v(t) = 2t - 2 at t=2 -> v=2, v'=2, v''=0

        double[] prod = new double[3];
        c.multiply(x, 0, y, 0, prod, 0);
        Assert.assertEquals(6.0, prod[0], EPS); // u*v = 6
        Assert.assertEquals(8.0, prod[1], EPS); // u'v + uv' = 1*2 + 3*2 = 8
        Assert.assertEquals(4.0, prod[2], EPS); // u''v + 2u'v' + uv'' = 0 + 2*1*2 + 0 = 4

        double[] quot = new double[3];
        c.divide(x, 0, y, 0, quot, 0);
        Assert.assertEquals(1.5, quot[0], EPS); // 3 / 2 = 1.5
        Assert.assertEquals(-1.0, quot[1], EPS); // (1*2 - 3*2) / 4 = -1.0
        Assert.assertEquals(2.0, quot[2], EPS); // (0*4 - (-4)*2*(2))/16 ...
    }

    @Test
    public void testRemainder() {
        DSCompiler c = DSCompiler.getCompiler(1, 1);
        double[] x = new double[] { 7.5, 2.0 };
        double[] y = new double[] { 2.0, 0.5 };
        double[] res = new double[2];
        c.remainder(x, 0, y, 0, res, 0);
        Assert.assertEquals(1.5, res[0], EPS);
        double k = FastMath.rint((7.5 - 1.5) / 2.0); // k = 3
        Assert.assertEquals(2.0 - k * 0.5, res[1], EPS);
    }

    @Test
    public void testPowDoubleAndInt() {
        DSCompiler c = DSCompiler.getCompiler(1, 3);
        double[] x = new double[] { 2.0, 1.0, 0.0, 0.0 };

        double[] rDouble = new double[4];
        c.pow(x, 0, 3.5, rDouble, 0);
        Assert.assertEquals(FastMath.pow(2.0, 3.5), rDouble[0], EPS);
        Assert.assertEquals(3.5 * FastMath.pow(2.0, 2.5), rDouble[1], EPS);
        Assert.assertEquals(3.5 * 2.5 * FastMath.pow(2.0, 1.5), rDouble[2], EPS);
        Assert.assertEquals(3.5 * 2.5 * 1.5 * FastMath.pow(2.0, 0.5), rDouble[3], EPS);

        double[] rInt0 = new double[4];
        c.pow(x, 0, 0, rInt0, 0);
        Assert.assertEquals(1.0, rInt0[0], EPS);
        Assert.assertEquals(0.0, rInt0[1], EPS);
        Assert.assertEquals(0.0, rInt0[2], EPS);
        Assert.assertEquals(0.0, rInt0[3], EPS);

        double[] rIntPos = new double[4];
        c.pow(x, 0, 3, rIntPos, 0);
        Assert.assertEquals(8.0, rIntPos[0], EPS);
        Assert.assertEquals(12.0, rIntPos[1], EPS);
        Assert.assertEquals(12.0, rIntPos[2], EPS);
        Assert.assertEquals(6.0, rIntPos[3], EPS);

        double[] rIntNeg = new double[4];
        c.pow(x, 0, -2, rIntNeg, 0);
        Assert.assertEquals(0.25, rIntNeg[0], EPS);
        Assert.assertEquals(-0.25, rIntNeg[1], EPS);
        Assert.assertEquals(0.375, rIntNeg[2], EPS);
    }

    @Test
    public void testPowDS() {
        DSCompiler c = DSCompiler.getCompiler(1, 2);
        double[] x = new double[] { 2.0, 1.0, 0.0 };
        double[] y = new double[] { 3.0, 0.0, 0.0 };
        double[] res = new double[3];
        c.pow(x, 0, y, 0, res, 0);
        Assert.assertEquals(8.0, res[0], EPS);
        Assert.assertEquals(12.0, res[1], EPS);
        Assert.assertEquals(12.0, res[2], EPS);
    }

    @Test
    public void testRootN() {
        DSCompiler c = DSCompiler.getCompiler(1, 2);
        double[] x = new double[] { 4.0, 1.0, 0.0 };

        double[] rSqrt = new double[3];
        c.rootN(x, 0, 2, rSqrt, 0);
        Assert.assertEquals(2.0, rSqrt[0], EPS);
        Assert.assertEquals(0.25, rSqrt[1], EPS);
        Assert.assertEquals(-1.0 / 32.0, rSqrt[2], EPS);

        double[] y = new double[] { 8.0, 1.0, 0.0 };
        double[] rCbrt = new double[3];
        c.rootN(y, 0, 3, rCbrt, 0);
        Assert.assertEquals(2.0, rCbrt[0], EPS);
        Assert.assertEquals(1.0 / 12.0, rCbrt[1], EPS);

        double[] z = new double[] { 16.0, 1.0, 0.0 };
        double[] r4 = new double[3];
        c.rootN(z, 0, 4, r4, 0);
        Assert.assertEquals(2.0, r4[0], EPS);
        Assert.assertEquals(1.0 / 32.0, r4[1], EPS);
    }

    @Test
    public void testExponentialsAndLogarithms() {
        DSCompiler c = DSCompiler.getCompiler(1, 3);
        double[] x = new double[] { 0.5, 1.0, 0.0, 0.0 };

        double[] rExp = new double[4];
        c.exp(x, 0, rExp, 0);
        Assert.assertEquals(FastMath.exp(0.5), rExp[0], EPS);
        Assert.assertEquals(FastMath.exp(0.5), rExp[1], EPS);
        Assert.assertEquals(FastMath.exp(0.5), rExp[2], EPS);

        double[] rExpm1 = new double[4];
        c.expm1(x, 0, rExpm1, 0);
        Assert.assertEquals(FastMath.expm1(0.5), rExpm1[0], EPS);
        Assert.assertEquals(FastMath.exp(0.5), rExpm1[1], EPS);

        double[] rLog = new double[4];
        c.log(x, 0, rLog, 0);
        Assert.assertEquals(FastMath.log(0.5), rLog[0], EPS);
        Assert.assertEquals(2.0, rLog[1], EPS);
        Assert.assertEquals(-4.0, rLog[2], EPS);
        Assert.assertEquals(16.0, rLog[3], EPS);

        double[] rLog1p = new double[4];
        c.log1p(x, 0, rLog1p, 0);
        Assert.assertEquals(FastMath.log1p(0.5), rLog1p[0], EPS);
        Assert.assertEquals(1.0 / 1.5, rLog1p[1], EPS);
        Assert.assertEquals(-1.0 / 2.25, rLog1p[2], EPS);

        double[] rLog10 = new double[4];
        c.log10(x, 0, rLog10, 0);
        Assert.assertEquals(FastMath.log10(0.5), rLog10[0], EPS);
        Assert.assertEquals(2.0 / FastMath.log(10.0), rLog10[1], EPS);
    }

    @Test
    public void testTrigonometricHigherOrder() {
        DSCompiler c = DSCompiler.getCompiler(1, 5);
        double[] x = new double[] { 0.5, 1.0, 0.0, 0.0, 0.0, 0.0 };

        double[] rCos = new double[6];
        c.cos(x, 0, rCos, 0);
        Assert.assertEquals(FastMath.cos(0.5), rCos[0], EPS);
        Assert.assertEquals(-FastMath.sin(0.5), rCos[1], EPS);
        Assert.assertEquals(-FastMath.cos(0.5), rCos[2], EPS);
        Assert.assertEquals(FastMath.sin(0.5), rCos[3], EPS);

        double[] rSin = new double[6];
        c.sin(x, 0, rSin, 0);
        Assert.assertEquals(FastMath.sin(0.5), rSin[0], EPS);
        Assert.assertEquals(FastMath.cos(0.5), rSin[1], EPS);
        Assert.assertEquals(-FastMath.sin(0.5), rSin[2], EPS);

        double[] rTan = new double[6];
        c.tan(x, 0, rTan, 0);
        double t = FastMath.tan(0.5);
        Assert.assertEquals(t, rTan[0], EPS);
        Assert.assertEquals(1.0 + t * t, rTan[1], EPS);
        Assert.assertEquals(2.0 * t * (1.0 + t * t), rTan[2], EPS);
    }

    @Test
    public void testInverseTrigonometricHigherOrder() {
        DSCompiler c = DSCompiler.getCompiler(1, 5);
        double[] x = new double[] { 0.5, 1.0, 0.0, 0.0, 0.0, 0.0 };

        double[] rAcos = new double[6];
        c.acos(x, 0, rAcos, 0);
        Assert.assertEquals(FastMath.acos(0.5), rAcos[0], EPS);
        Assert.assertEquals(-1.0 / FastMath.sqrt(0.75), rAcos[1], EPS);
        Assert.assertEquals(-0.5 / FastMath.pow(0.75, 1.5), rAcos[2], EPS);

        double[] rAsin = new double[6];
        c.asin(x, 0, rAsin, 0);
        Assert.assertEquals(FastMath.asin(0.5), rAsin[0], EPS);
        Assert.assertEquals(1.0 / FastMath.sqrt(0.75), rAsin[1], EPS);
        Assert.assertEquals(0.5 / FastMath.pow(0.75, 1.5), rAsin[2], EPS);

        double[] rAtan = new double[6];
        c.atan(x, 0, rAtan, 0);
        Assert.assertEquals(FastMath.atan(0.5), rAtan[0], EPS);
        Assert.assertEquals(1.0 / 1.25, rAtan[1], EPS);
        Assert.assertEquals(-1.0 / (1.25 * 1.25), rAtan[2], EPS);
    }

    @Test
    public void testAtan2() {
        DSCompiler c = DSCompiler.getCompiler(1, 2);
        double[] yPos = new double[] { 1.0, 0.0, 0.0 };
        double[] yNeg = new double[] { -1.0, 0.0, 0.0 };
        double[] xPos = new double[] { 1.0, 1.0, 0.0 };
        double[] xNeg = new double[] { -1.0, 1.0, 0.0 };

        double[] resPos = new double[3];
        c.atan2(yPos, 0, xPos, 0, resPos, 0);
        Assert.assertEquals(FastMath.PI / 4.0, resPos[0], EPS);
        Assert.assertEquals(-0.5, resPos[1], EPS);

        double[] resNeg1 = new double[3];
        c.atan2(yPos, 0, xNeg, 0, resNeg1, 0);
        Assert.assertEquals(3.0 * FastMath.PI / 4.0, resNeg1[0], EPS);

        double[] resNeg2 = new double[3];
        c.atan2(yNeg, 0, xNeg, 0, resNeg2, 0);
        Assert.assertEquals(-3.0 * FastMath.PI / 4.0, resNeg2[0], EPS);
    }

    @Test
    public void testHyperbolicHigherOrder() {
        DSCompiler c = DSCompiler.getCompiler(1, 5);
        double[] x = new double[] { 0.5, 1.0, 0.0, 0.0, 0.0, 0.0 };

        double[] rCosh = new double[6];
        c.cosh(x, 0, rCosh, 0);
        Assert.assertEquals(FastMath.cosh(0.5), rCosh[0], EPS);
        Assert.assertEquals(FastMath.sinh(0.5), rCosh[1], EPS);
        Assert.assertEquals(FastMath.cosh(0.5), rCosh[2], EPS);

        double[] rSinh = new double[6];
        c.sinh(x, 0, rSinh, 0);
        Assert.assertEquals(FastMath.sinh(0.5), rSinh[0], EPS);
        Assert.assertEquals(FastMath.cosh(0.5), rSinh[1], EPS);
        Assert.assertEquals(FastMath.sinh(0.5), rSinh[2], EPS);

        double[] rTanh = new double[6];
        c.tanh(x, 0, rTanh, 0);
        double th = FastMath.tanh(0.5);
        Assert.assertEquals(th, rTanh[0], EPS);
        Assert.assertEquals(1.0 - th * th, rTanh[1], EPS);
        Assert.assertEquals(-2.0 * th * (1.0 - th * th), rTanh[2], EPS);
    }

    @Test
    public void testInverseHyperbolicHigherOrder() {
        DSCompiler c = DSCompiler.getCompiler(1, 5);
        double[] xAcosh = new double[] { 1.5, 1.0, 0.0, 0.0, 0.0, 0.0 };
        double[] rAcosh = new double[6];
        c.acosh(xAcosh, 0, rAcosh, 0);
        Assert.assertEquals(FastMath.acosh(1.5), rAcosh[0], EPS);
        Assert.assertEquals(1.0 / FastMath.sqrt(1.25), rAcosh[1], EPS);

        double[] xAsinh = new double[] { 0.5, 1.0, 0.0, 0.0, 0.0, 0.0 };
        double[] rAsinh = new double[6];
        c.asinh(xAsinh, 0, rAsinh, 0);
        Assert.assertEquals(FastMath.asinh(0.5), rAsinh[0], EPS);
        Assert.assertEquals(1.0 / FastMath.sqrt(1.25), rAsinh[1], EPS);

        double[] xAtanh = new double[] { 0.5, 1.0, 0.0, 0.0, 0.0, 0.0 };
        double[] rAtanh = new double[6];
        c.atanh(xAtanh, 0, rAtanh, 0);
        Assert.assertEquals(FastMath.atanh(0.5), rAtanh[0], EPS);
        Assert.assertEquals(1.0 / 0.75, rAtanh[1], EPS);
        Assert.assertEquals(2.0 * 0.5 / (0.75 * 0.75), rAtanh[2], EPS);
    }

    @Test
    public void testTaylorExpansion() {
        DSCompiler c = DSCompiler.getCompiler(2, 2);
        // f(x, y) = x^2 + 3xy + y^2 at (1, 2)
        // f(1, 2) = 1 + 6 + 4 = 11
        // df/dx = 2x + 3y = 8
        // d2f/dx2 = 2
        // df/dy = 3x + 2y = 7
        // d2f/dxdy = 3
        // d2f/dy2 = 2
        double[] ds = new double[c.getSize()];
        ds[c.getPartialDerivativeIndex(0, 0)] = 11.0;
        ds[c.getPartialDerivativeIndex(1, 0)] = 8.0;
        ds[c.getPartialDerivativeIndex(2, 0)] = 2.0;
        ds[c.getPartialDerivativeIndex(0, 1)] = 7.0;
        ds[c.getPartialDerivativeIndex(1, 1)] = 3.0;
        ds[c.getPartialDerivativeIndex(0, 2)] = 2.0;

        double deltaX = 0.5;
        double deltaY = -0.2;
        double taylorValue = c.taylor(ds, 0, deltaX, deltaY);

        double exactX = 1.0 + deltaX;
        double exactY = 2.0 + deltaY;
        double exactValue = exactX * exactX + 3.0 * exactX * exactY + exactY * exactY;
        Assert.assertEquals(exactValue, taylorValue, EPS);
    }

    @Test
    public void testZeroParameterZeroOrderOperations() {
        DSCompiler c = DSCompiler.getCompiler(0, 0);
        double[] op1 = new double[] { 3.0 };
        double[] op2 = new double[] { 2.0 };
        double[] res = new double[1];

        c.multiply(op1, 0, op2, 0, res, 0);
        Assert.assertEquals(6.0, res[0], EPS);

        c.divide(op1, 0, op2, 0, res, 0);
        Assert.assertEquals(1.5, res[0], EPS);

        c.pow(op1, 0, 2.0, res, 0);
        Assert.assertEquals(9.0, res[0], EPS);

        c.rootN(op1, 0, 2, res, 0);
        Assert.assertEquals(FastMath.sqrt(3.0), res[0], EPS);

        c.exp(op1, 0, res, 0);
        Assert.assertEquals(FastMath.exp(3.0), res[0], EPS);

        c.cos(op1, 0, res, 0);
        Assert.assertEquals(FastMath.cos(3.0), res[0], EPS);
    }
}
