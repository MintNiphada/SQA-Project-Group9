package org.apache.commons.math3.analysis.differentiation;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

public class DSCompilerTest {

    @Before
    public void resetCompilerCache() throws Exception {
        Field field = DSCompiler.class.getDeclaredField("compilers");
        field.setAccessible(true);
        field.set(null, new java.util.concurrent.atomic.AtomicReference<DSCompiler[][]>(null));
    }

    @Test
    public void testGetCompiler() {
        DSCompiler c00 = DSCompiler.getCompiler(0, 0);
        assertNotNull(c00);
        assertEquals(0, c00.getFreeParameters());
        assertEquals(0, c00.getOrder());
        assertEquals(1, c00.getSize());

        DSCompiler c10 = DSCompiler.getCompiler(1, 0);
        assertNotNull(c10);
        assertEquals(1, c10.getFreeParameters());
        assertEquals(0, c10.getOrder());
        assertEquals(1, c10.getSize());

        DSCompiler c01 = DSCompiler.getCompiler(0, 1);
        assertNotNull(c01);
        assertEquals(0, c01.getFreeParameters());
        assertEquals(1, c01.getOrder());
        assertEquals(1, c01.getSize());

        DSCompiler c11 = DSCompiler.getCompiler(1, 1);
        assertNotNull(c11);
        assertEquals(1, c11.getFreeParameters());
        assertEquals(1, c11.getOrder());
        assertEquals(2, c11.getSize());

        DSCompiler c22 = DSCompiler.getCompiler(2, 2);
        assertNotNull(c22);
        assertEquals(2, c22.getFreeParameters());
        assertEquals(2, c22.getOrder());
        assertEquals(6, c22.getSize());

        // same parameters should return same instance
        assertSame(c00, DSCompiler.getCompiler(0, 0));
        assertSame(c11, DSCompiler.getCompiler(1, 1));
        assertSame(c22, DSCompiler.getCompiler(2, 2));
    }

    @Test
    public void testGetFreeParameters() {
        DSCompiler c = DSCompiler.getCompiler(3, 2);
        assertEquals(3, c.getFreeParameters());
    }

    @Test
    public void testGetOrder() {
        DSCompiler c = DSCompiler.getCompiler(3, 2);
        assertEquals(2, c.getOrder());
    }

    @Test
    public void testGetSize() {
        assertEquals(1, DSCompiler.getCompiler(0, 0).getSize());
        assertEquals(1, DSCompiler.getCompiler(1, 0).getSize());
        assertEquals(1, DSCompiler.getCompiler(0, 1).getSize());
        assertEquals(2, DSCompiler.getCompiler(1, 1).getSize());
        assertEquals(3, DSCompiler.getCompiler(2, 1).getSize());
        assertEquals(6, DSCompiler.getCompiler(2, 2).getSize());
        assertEquals(10, DSCompiler.getCompiler(3, 2).getSize());
        assertEquals(4, DSCompiler.getCompiler(3, 1).getSize());
    }

    @Test
    public void testGetPartialDerivativeIndexValid() {
        DSCompiler c = DSCompiler.getCompiler(2, 2);
        // index 0 is value
        assertEquals(0, c.getPartialDerivativeIndex(0, 0));
        // first derivatives: df/dx at index 1, df/dy at index 2? Actually for (2,1) order 1: indices: 0: f, 1: df/dx, 2: df/dy.
        // For order 2, the ordering is: 0: f, 1: df/dx, 2: d2f/dx2, 3: df/dy, 4: d2f/dxdy, 5: d2f/dy2.
        // So df/dx (1,0) -> index 1; df/dy (0,1) -> index 3; d2f/dx2 (2,0) -> index 2; d2f/dxdy (1,1) -> index 4; d2f/dy2 (0,2) -> index 5.
        assertEquals(1, c.getPartialDerivativeIndex(1, 0));
        assertEquals(3, c.getPartialDerivativeIndex(0, 1));
        assertEquals(2, c.getPartialDerivativeIndex(2, 0));
        assertEquals(4, c.getPartialDerivativeIndex(1, 1));
        assertEquals(5, c.getPartialDerivativeIndex(0, 2));
    }

    @Test(expected = org.apache.commons.math3.exception.DimensionMismatchException.class)
    public void testGetPartialDerivativeIndexWrongLength() {
        DSCompiler c = DSCompiler.getCompiler(2, 2);
        c.getPartialDerivativeIndex(1); // only one order, but 2 parameters
    }

    @Test(expected = org.apache.commons.math3.exception.NumberIsTooLargeException.class)
    public void testGetPartialDerivativeIndexSumTooLarge() {
        DSCompiler c = DSCompiler.getCompiler(2, 2);
        c.getPartialDerivativeIndex(1, 2); // sum = 3 > order 2
    }

    @Test
    public void testGetPartialDerivativeOrders() {
        DSCompiler c = DSCompiler.getCompiler(2, 2);
        assertArrayEquals(new int[]{0, 0}, c.getPartialDerivativeOrders(0));
        assertArrayEquals(new int[]{1, 0}, c.getPartialDerivativeOrders(1));
        assertArrayEquals(new int[]{2, 0}, c.getPartialDerivativeOrders(2));
        assertArrayEquals(new int[]{0, 1}, c.getPartialDerivativeOrders(3));
        assertArrayEquals(new int[]{1, 1}, c.getPartialDerivativeOrders(4));
        assertArrayEquals(new int[]{0, 2}, c.getPartialDerivativeOrders(5));
    }

    @Test
    public void testLinearCombination2() {
        DSCompiler c = DSCompiler.getCompiler(1, 1);
        int size = c.getSize();
        double[] c1 = new double[size];
        double[] c2 = new double[size];
        double[] result = new double[size];
        Arrays.fill(c1, 2.0);
        Arrays.fill(c2, 3.0);
        c.linearCombination(1.5, c1, 0, 2.5, c2, 0, result, 0);
        // result[i] = 1.5*2 + 2.5*3 = 3 + 7.5 = 10.5
        for (int i = 0; i < size; i++) {
            assertEquals(10.5, result[i], 1e-15);
        }
    }

    @Test
    public void testLinearCombination3() {
        DSCompiler c = DSCompiler.getCompiler(1, 1);
        int size = c.getSize();
        double[] c1 = new double[size];
        double[] c2 = new double[size];
        double[] c3 = new double[size];
        double[] result = new double[size];
        Arrays.fill(c1, 1.0);
        Arrays.fill(c2, 2.0);
        Arrays.fill(c3, 3.0);
        c.linearCombination(1.0, c1, 0, 2.0, c2, 0, 3.0, c3, 0, result, 0);
        // 1*1 + 2*2 + 3*3 = 1+4+9=14
        for (int i = 0; i < size; i++) {
            assertEquals(14.0, result[i], 1e-15);
        }
    }

    @Test
    public void testLinearCombination4() {
        DSCompiler c = DSCompiler.getCompiler(1, 1);
        int size = c.getSize();
        double[] c1 = new double[size];
        double[] c2 = new double[size];
        double[] c3 = new double[size];
        double[] c4 = new double[size];
        double[] result = new double[size];
        Arrays.fill(c1, 1.0);
        Arrays.fill(c2, 2.0);
        Arrays.fill(c3, 3.0);
        Arrays.fill(c4, 4.0);
        c.linearCombination(1.0, c1, 0, 2.0, c2, 0, 3.0, c3, 0, 4.0, c4, 0, result, 0);
        // 1*1 + 2*2 + 3*3 + 4*4 = 1+4+9+16=30
        for (int i = 0; i < size; i++) {
            assertEquals(30.0, result[i], 1e-15);
        }
    }

    @Test
    public void testAdd() {
        DSCompiler c = DSCompiler.getCompiler(1, 1);
        int size = c.getSize();
        double[] lhs = {2.0, 3.0};
        double[] rhs = {4.0, 5.0};
        double[] result = new double[size];
        c.add(lhs, 0, rhs, 0, result, 0);
        assertEquals(6.0, result[0], 1e-15);
        assertEquals(8.0, result[1], 1e-15);
    }

    @Test
    public void testSubtract() {
        DSCompiler c = DSCompiler.getCompiler(1, 1);
        int size = c.getSize();
        double[] lhs = {5.0, 7.0};
        double[] rhs = {2.0, 3.0};
        double[] result = new double[size];
        c.subtract(lhs, 0, rhs, 0, result, 0);
        assertEquals(3.0, result[0], 1e-15);
        assertEquals(4.0, result[1], 1e-15);
    }

    @Test
    public void testMultiply() {
        DSCompiler c = DSCompiler.getCompiler(1, 1);
        int size = c.getSize();
        double[] lhs = {2.0, 3.0};
        double[] rhs = {4.0, 5.0};
        double[] result = new double[size];
        c.multiply(lhs, 0, rhs, 0, result, 0);
        // f*g = 2*4=8, (f*g)' = f'*g + f*g' = 3*4 + 2*5 = 12+10=22
        assertEquals(8.0, result[0], 1e-15);
        assertEquals(22.0, result[1], 1e-15);
    }

    @Test
    public void testMultiplyZeroParameters() {
        DSCompiler c = DSCompiler.getCompiler(0, 0);
        double[] lhs = {2.0};
        double[] rhs = {3.0};
        double[] result = new double[1];
        c.multiply(lhs, 0, rhs, 0, result, 0);
        assertEquals(6.0, result[0], 1e-15);
    }

    @Test
    public void testDivide() {
        DSCompiler c = DSCompiler.getCompiler(1, 1);
        int size = c.getSize();
        double[] lhs = {6.0, 1.0}; // f=6, f'=1
        double[] rhs = {2.0, 0.5}; // g=2, g'=0.5
        double[] result = new double[size];
        c.divide(lhs, 0, rhs, 0, result, 0);
        // f/g = 6/2=3, (f/g)' = (f'*g - f*g')/g^2 = (1*2 - 6*0.5)/4 = (2-3)/4 = -0.25
        assertEquals(3.0, result[0], 1e-15);
        assertEquals(-0.25, result[1], 1e-15);
    }

    @Test
    public void testRemainder() {
        DSCompiler c = DSCompiler.getCompiler(1, 1);
        int size = c.getSize();
        double[] lhs = {7.0, 1.0};
        double[] rhs = {3.0, 0.5};
        double[] result = new double[size];
        c.remainder(lhs, 0, rhs, 0, result, 0);
        // 7 % 3 = 1, k = rint((7-1)/3)=2, derivative: 1 - 2*0.5 = 0
        assertEquals(1.0, result[0], 1e-15);
        assertEquals(0.0, result[1], 1e-15);
    }

    @Test
    public void testPowDouble() {
        DSCompiler c = DSCompiler.getCompiler(1, 1);
        int size = c.getSize();
        double[] operand = {2.0, 1.0}; // x=2, x'=1
        double[] result = new double[size];
        c.pow(operand, 0, 3.0, result, 0);
        // x^3 = 8, derivative: 3*x^2 * x' = 3*4*1=12
        assertEquals(8.0, result[0], 1e-15);
        assertEquals(12.0, result[1], 1e-15);
    }

    @Test
    public void testPowIntPositive() {
        DSCompiler c = DSCompiler.getCompiler(1, 1);
        int size = c.getSize();
        double[] operand = {2.0, 1.0};
        double[] result = new double[size];
        c.pow(operand, 0, 3, result, 0);
        assertEquals(8.0, result[0], 1e-15);
        assertEquals(12.0, result[1], 1e-15);
    }

    @Test
    public void testPowIntZero() {
        DSCompiler c = DSCompiler.getCompiler(1, 1);
        int size = c.getSize();
        double[] operand = {2.0, 1.0};
        double[] result = new double[size];
        c.pow(operand, 0, 0, result, 0);
        assertEquals(1.0, result[0], 1e-15);
        assertEquals(0.0, result[1], 1e-15);
    }

    @Test
    public void testPowIntNegative() {
        DSCompiler c = DSCompiler.getCompiler(1, 1);
        int size = c.getSize();
        double[] operand = {2.0, 1.0};
        double[] result = new double[size];
        c.pow(operand, 0, -1, result, 0);
        // x^-1 = 0.5, derivative: -1*x^{-2}*x' = -1/4 = -0.25
        assertEquals(0.5, result[0], 1e-15);
        assertEquals(-0.25, result[1], 1e-15);
    }

    @Test
    public void testPowArray() {
        DSCompiler c = DSCompiler.getCompiler(1, 1);
        int size = c.getSize();
        double[] x = {2.0, 1.0}; // x=2, x'=1
        double[] y = {3.0, 0.5}; // y=3, y'=0.5
        double[] result = new double[size];
        c.pow(x, 0, y, 0, result, 0);
        // x^y = exp(y*ln(x)) = exp(3*ln2) = 2^3 = 8
        // derivative: x^y * (y'*ln(x) + y*x'/x) = 8 * (0.5*ln2 + 3*1/2) = 8*(0.5*0.693147 + 1.5) = 8*(0.3465735+1.5)=8*1.8465735=14.772588
        double expectedVal = 8.0;
        double expectedDer = 8.0 * (0.5 * Math.log(2.0) + 3.0 * 1.0 / 2.0);
        assertEquals(expectedVal, result[0], 1e-12);
        assertEquals(expectedDer, result[1], 1e-12);
    }

    @Test
    public void testRootN() {
        DSCompiler c = DSCompiler.getCompiler(1, 1);
        int size = c.getSize();
        double[] operand = {8.0, 1.0};
        double[] result = new double[size];
        c.rootN(operand, 0, 3, result, 0);
        // cube root of 8 = 2, derivative: (1/3)*x^{-2/3} * x' = (1/3)*8^{-2/3} = (1/3)*(1/4)=1/12 ≈0.0833333
        assertEquals(2.0, result[0], 1e-15);
        assertEquals(1.0 / 12.0, result[1], 1e-12);
    }

    @Test
    public void testExp() {
        DSCompiler c = DSCompiler.getCompiler(1, 1);
        int size = c.getSize();
        double[] operand = {1.0, 1.0};
        double[] result = new double[size];
        c.exp(operand, 0, result, 0);
        double exp1 = Math.exp(1.0);
        assertEquals(exp1, result[0], 1e-15);
        assertEquals(exp1, result[1], 1e-15); // derivative of exp(x) is exp(x)*x'
    }

    @Test
    public void testExpm1() {
        DSCompiler c = DSCompiler.getCompiler(1, 1);
        int size = c.getSize();
        double[] operand = {1.0, 1.0};
        double[] result = new double[size];
        c.expm1(operand, 0, result, 0);
        double exp1 = Math.exp(1.0);
        assertEquals(exp1 - 1.0, result[0], 1e-15);
        assertEquals(exp1, result[1], 1e-15);
    }

    @Test
    public void testLog() {
        DSCompiler c = DSCompiler.getCompiler(1, 1);
        int size = c.getSize();
        double[] operand = {2.0, 1.0};
        double[] result = new double[size];
        c.log(operand, 0, result, 0);
        assertEquals(Math.log(2.0), result[0], 1e-15);
        assertEquals(1.0 / 2.0, result[1], 1e-15);
    }

    @Test
    public void testLog1p() {
        DSCompiler c = DSCompiler.getCompiler(1, 1);
        int size = c.getSize();
        double[] operand = {1.0, 1.0}; // log1p(1)=ln2
        double[] result = new double[size];
        c.log1p(operand, 0, result, 0);
        assertEquals(Math.log1p(1.0), result[0], 1e-15);
        assertEquals(1.0 / (1.0 + 1.0), result[1], 1e-15);
    }

    @Test
    public void testLog10() {
        DSCompiler c = DSCompiler.getCompiler(1, 1);
        int size = c.getSize();
        double[] operand = {10.0, 1.0};
        double[] result = new double[size];
        c.log10(operand, 0, result, 0);
        assertEquals(1.0, result[0], 1e-15);
        assertEquals(1.0 / (10.0 * Math.log(10.0)), result[1], 1e-15);
    }

    @Test
    public void testCos() {
        DSCompiler c = DSCompiler.getCompiler(1, 1);
        int size = c.getSize();
        double[] operand = {0.0, 1.0};
        double[] result = new double[size];
        c.cos(operand, 0, result, 0);
        assertEquals(1.0, result[0], 1e-15);
        assertEquals(0.0, result[1], 1e-15); // derivative -sin(0)*1 = 0
    }

    @Test
    public void testSin() {
        DSCompiler c = DSCompiler.getCompiler(1, 1);
        int size = c.getSize();
        double[] operand = {0.0, 1.0};
        double[] result = new double[size];
        c.sin(operand, 0, result, 0);
        assertEquals(0.0, result[0], 1e-15);
        assertEquals(1.0, result[1], 1e-15); // cos(0)*1 = 1
    }

    @Test
    public void testTan() {
        DSCompiler c = DSCompiler.getCompiler(1, 1);
        int size = c.getSize();
        double[] operand = {0.0, 1.0};
        double[] result = new double[size];
        c.tan(operand, 0, result, 0);
        assertEquals(0.0, result[0], 1e-15);
        assertEquals(1.0, result[1], 1e-15); // derivative sec^2(0)=1
    }

    @Test
    public void testAcos() {
        DSCompiler c = DSCompiler.getCompiler(1, 1);
        int size = c.getSize();
        double[] operand = {0.0, 1.0};
        double[] result = new double[size];
        c.acos(operand, 0, result, 0);
        assertEquals(Math.acos(0.0), result[0], 1e-15);
        assertEquals(-1.0, result[1], 1e-15); // derivative -1/sqrt(1-0) = -1
    }

    @Test
    public void testAsin() {
        DSCompiler c = DSCompiler.getCompiler(1, 1);
        int size = c.getSize();
        double[] operand = {0.0, 1.0};
        double[] result = new double[size];
        c.asin(operand, 0, result, 0);
        assertEquals(0.0, result[0], 1e-15);
        assertEquals(1.0, result[1], 1e-15); // derivative 1/sqrt(1-0)=1
    }

    @Test
    public void testAtan() {
        DSCompiler c = DSCompiler.getCompiler(1, 1);
        int size = c.getSize();
        double[] operand = {0.0, 1.0};
        double[] result = new double[size];
        c.atan(operand, 0, result, 0);
        assertEquals(0.0, result[0], 1e-15);
        assertEquals(1.0, result[1], 1e-15); // derivative 1/(1+0)=1
    }

    @Test
    public void testAtan2PositiveX() {
        DSCompiler c = DSCompiler.getCompiler(1, 1);
        int size = c.getSize();
        double[] y = {1.0, 1.0};
        double[] x = {1.0, 0.5};
        double[] result = new double[size];
        c.atan2(y, 0, x, 0, result, 0);
        // atan2(1,1) = pi/4 ≈0.785398, derivative? We'll just check value.
        assertEquals(Math.atan2(1.0, 1.0), result[0], 1e-12);
        // derivative not trivial, but we can check it's computed.
    }

    @Test
    public void testAtan2NegativeX() {
        DSCompiler c = DSCompiler.getCompiler(1, 1);
        int size = c.getSize();
        double[] y = {1.0, 1.0};
        double[] x = {-1.0, 0.5};
        double[] result = new double[size];
        c.atan2(y, 0, x, 0, result, 0);
        assertEquals(Math.atan2(1.0, -1.0), result[0], 1e-12);
    }

    @Test
    public void testCosh() {
        DSCompiler c = DSCompiler.getCompiler(1, 1);
        int size = c.getSize();
        double[] operand = {0.0, 1.0};
        double[] result = new double[size];
        c.cosh(operand, 0, result, 0);
        assertEquals(1.0, result[0], 1e-15);
        assertEquals(0.0, result[1], 1e-15); // sinh(0)=0
    }

    @Test
    public void testSinh() {
        DSCompiler c = DSCompiler.getCompiler(1, 1);
        int size = c.getSize();
        double[] operand = {0.0, 1.0};
        double[] result = new double[size];
        c.sinh(operand, 0, result, 0);
        assertEquals(0.0, result[0], 1e-15);
        assertEquals(1.0, result[1], 1e-15); // cosh(0)=1
    }

    @Test
    public void testTanh() {
        DSCompiler c = DSCompiler.getCompiler(1, 1);
        int size = c.getSize();
        double[] operand = {0.0, 1.0};
        double[] result = new double[size];
        c.tanh(operand, 0, result, 0);
        assertEquals(0.0, result[0], 1e-15);
        assertEquals(1.0, result[1], 1e-15); // derivative sech^2(0)=1
    }

    @Test
    public void testAcosh() {
        DSCompiler c = DSCompiler.getCompiler(1, 1);
        int size = c.getSize();
        double[] operand = {2.0, 1.0};
        double[] result = new double[size];
        c.acosh(operand, 0, result, 0);
        assertEquals(Math.acosh(2.0), result[0], 1e-12);
        // derivative 1/sqrt(x^2-1) = 1/sqrt(3) ≈0.57735
        assertEquals(1.0 / Math.sqrt(3.0), result[1], 1e-12);
    }

    @Test
    public void testAsinh() {
        DSCompiler c = DSCompiler.getCompiler(1, 1);
        int size = c.getSize();
        double[] operand = {0.0, 1.0};
        double[] result = new double[size];
        c.asinh(operand, 0, result, 0);
        assertEquals(0.0, result[0], 1e-15);
        assertEquals(1.0, result[1], 1e-15); // derivative 1/sqrt(1+0)=1
    }

    @Test
    public void testAtanh() {
        DSCompiler c = DSCompiler.getCompiler(1, 1);
        int size = c.getSize();
        double[] operand = {0.0, 1.0};
        double[] result = new double[size];
        c.atanh(operand, 0, result, 0);
        assertEquals(0.0, result[0], 1e-15);
        assertEquals(1.0, result[1], 1e-15); // derivative 1/(1-0)=1
    }

    @Test
    public void testCompose() {
        DSCompiler c = DSCompiler.getCompiler(1, 1);
        int size = c.getSize();
        double[] operand = {2.0, 1.0};
        double[] f = {3.0, 4.0}; // f0 = f(x), f1 = f'(x)
        double[] result = new double[size];
        c.compose(operand, 0, f, result, 0);
        // compose: result[0] = f0 = 3.0; result[1] = f1 * operand' = 4*1 = 4
        assertEquals(3.0, result[0], 1e-15);
        assertEquals(4.0, result[1], 1e-15);
    }

    @Test
    public void testTaylor() {
        DSCompiler c = DSCompiler.getCompiler(1, 1);
        int size = c.getSize();
        double[] ds = {2.0, 3.0}; // f=2, f'=3
        double delta = 0.5;
        double value = c.taylor(ds, 0, delta);
        // Taylor: f(x+dx) ≈ f + f'*dx = 2 + 3*0.5 = 3.5
        assertEquals(3.5, value, 1e-15);
    }

    @Test
    public void testCheckCompatibility() {
        DSCompiler c1 = DSCompiler.getCompiler(2, 2);
        DSCompiler c2 = DSCompiler.getCompiler(2, 2);
        c1.checkCompatibility(c2); // should not throw

        DSCompiler c3 = DSCompiler.getCompiler(2, 1);
        try {
            c1.checkCompatibility(c3);
            fail("Expected DimensionMismatchException");
        } catch (org.apache.commons.math3.exception.DimensionMismatchException e) {
            // expected
        }

        DSCompiler c4 = DSCompiler.getCompiler(3, 2);
        try {
            c1.checkCompatibility(c4);
            fail("Expected DimensionMismatchException");
        } catch (org.apache.commons.math3.exception.DimensionMismatchException e) {
            // expected
        }
    }

    @Test
    public void testEdgeCaseZeroParametersZeroOrder() {
        DSCompiler c = DSCompiler.getCompiler(0, 0);
        assertEquals(1, c.getSize());
        double[] lhs = {5.0};
        double[] rhs = {3.0};
        double[] result = new double[1];
        c.add(lhs, 0, rhs, 0, result, 0);
        assertEquals(8.0, result[0], 1e-15);
        c.multiply(lhs, 0, rhs, 0, result, 0);
        assertEquals(15.0, result[0], 1e-15);
        c.pow(lhs, 0, 2.0, result, 0);
        assertEquals(25.0, result[0], 1e-15);
    }

    @Test
    public void testGetPartialDerivativeIndexWithZeroParameters() {
        DSCompiler c = DSCompiler.getCompiler(0, 0);
        assertEquals(0, c.getPartialDerivativeIndex()); // empty orders
    }

    @Test(expected = org.apache.commons.math3.exception.DimensionMismatchException.class)
    public void testGetPartialDerivativeIndexWithZeroParametersButOrdersGiven() {
        DSCompiler c = DSCompiler.getCompiler(0, 0);
        c.getPartialDerivativeIndex(0); // length 1, but free parameters 0
    }
}
