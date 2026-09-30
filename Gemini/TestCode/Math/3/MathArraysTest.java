package org.apache.commons.math3.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.math3.Field;
import org.apache.commons.math3.FieldElement;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.MathInternalError;
import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.exception.NonMonotonicSequenceException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.fraction.Fraction;
import org.apache.commons.math3.fraction.FractionField;
import org.junit.Assert;
import org.junit.Test;

public class MathArraysTest {

    @Test
    public void testPrivateConstructor() throws Exception {
        Constructor<MathArrays> constructor = MathArrays.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        MathArrays instance = constructor.newInstance();
        Assert.assertNotNull(instance);
    }

    @Test
    public void testScaleAndScaleInPlace() {
        double[] test = {1.0, 2.0, 3.5};
        double[] scaled = MathArrays.scale(2.0, test);
        Assert.assertArrayEquals(new double[]{2.0, 4.0, 7.0}, scaled, 1e-15);
        Assert.assertArrayEquals(new double[]{1.0, 2.0, 3.5}, test, 1e-15);

        MathArrays.scaleInPlace(3.0, test);
        Assert.assertArrayEquals(new double[]{3.0, 6.0, 10.5}, test, 1e-15);
    }

    @Test
    public void testEbeOperations() {
        double[] a = {1.0, 4.0, 9.0};
        double[] b = {2.0, 2.0, 3.0};

        Assert.assertArrayEquals(new double[]{3.0, 6.0, 12.0}, MathArrays.ebeAdd(a, b), 1e-15);
        Assert.assertArrayEquals(new double[]{-1.0, 2.0, 6.0}, MathArrays.ebeSubtract(a, b), 1e-15);
        Assert.assertArrayEquals(new double[]{2.0, 8.0, 27.0}, MathArrays.ebeMultiply(a, b), 1e-15);
        Assert.assertArrayEquals(new double[]{0.5, 2.0, 3.0}, MathArrays.ebeDivide(a, b), 1e-15);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testEbeAddDimensionMismatch() {
        MathArrays.ebeAdd(new double[]{1.0}, new double[]{1.0, 2.0});
    }

    @Test(expected = DimensionMismatchException.class)
    public void testEbeSubtractDimensionMismatch() {
        MathArrays.ebeSubtract(new double[]{1.0}, new double[]{1.0, 2.0});
    }

    @Test(expected = DimensionMismatchException.class)
    public void testEbeMultiplyDimensionMismatch() {
        MathArrays.ebeMultiply(new double[]{1.0}, new double[]{1.0, 2.0});
    }

    @Test(expected = DimensionMismatchException.class)
    public void testEbeDivideDimensionMismatch() {
        MathArrays.ebeDivide(new double[]{1.0}, new double[]{1.0, 2.0});
    }

    @Test
    public void testDistances() {
        double[] d1 = {1.0, 2.0, 5.0};
        double[] d2 = {4.0, -2.0, 5.0};

        Assert.assertEquals(7.0, MathArrays.distance1(d1, d2), 1e-15);
        Assert.assertEquals(5.0, MathArrays.distance(d1, d2), 1e-15);
        Assert.assertEquals(4.0, MathArrays.distanceInf(d1, d2), 1e-15);

        int[] i1 = {1, 2, 5};
        int[] i2 = {4, -2, 5};

        Assert.assertEquals(7, MathArrays.distance1(i1, i2));
        Assert.assertEquals(5.0, MathArrays.distance(i1, i2), 1e-15);
        Assert.assertEquals(4, MathArrays.distanceInf(i1, i2));
    }

    @Test
    public void testIsMonotonicComparable() {
        Integer[] incStrict = {1, 2, 3, 5};
        Integer[] incNonStrict = {1, 2, 2, 5};
        Integer[] decStrict = {5, 3, 2, 1};
        Integer[] decNonStrict = {5, 2, 2, 1};
        Integer[] notMonotonic = {1, 3, 2, 5};

        Assert.assertTrue(MathArrays.isMonotonic(incStrict, MathArrays.OrderDirection.INCREASING, true));
        Assert.assertTrue(MathArrays.isMonotonic(incStrict, MathArrays.OrderDirection.INCREASING, false));
        Assert.assertFalse(MathArrays.isMonotonic(incNonStrict, MathArrays.OrderDirection.INCREASING, true));
        Assert.assertTrue(MathArrays.isMonotonic(incNonStrict, MathArrays.OrderDirection.INCREASING, false));

        Assert.assertTrue(MathArrays.isMonotonic(decStrict, MathArrays.OrderDirection.DECREASING, true));
        Assert.assertTrue(MathArrays.isMonotonic(decStrict, MathArrays.OrderDirection.DECREASING, false));
        Assert.assertFalse(MathArrays.isMonotonic(decNonStrict, MathArrays.OrderDirection.DECREASING, true));
        Assert.assertTrue(MathArrays.isMonotonic(decNonStrict, MathArrays.OrderDirection.DECREASING, false));

        Assert.assertFalse(MathArrays.isMonotonic(notMonotonic, MathArrays.OrderDirection.INCREASING, false));
        Assert.assertFalse(MathArrays.isMonotonic(notMonotonic, MathArrays.OrderDirection.DECREASING, false));
    }

    @Test
    public void testCheckOrderDouble() {
        double[] incStrict = {1.0, 2.0, 3.0};
        double[] incNonStrict = {1.0, 2.0, 2.0};
        double[] decStrict = {3.0, 2.0, 1.0};
        double[] decNonStrict = {3.0, 2.0, 2.0};
        double[] unord = {1.0, 3.0, 2.0};

        Assert.assertTrue(MathArrays.isMonotonic(incStrict, MathArrays.OrderDirection.INCREASING, true));
        Assert.assertTrue(MathArrays.isMonotonic(incNonStrict, MathArrays.OrderDirection.INCREASING, false));
        Assert.assertFalse(MathArrays.isMonotonic(incNonStrict, MathArrays.OrderDirection.INCREASING, true));

        Assert.assertTrue(MathArrays.isMonotonic(decStrict, MathArrays.OrderDirection.DECREASING, true));
        Assert.assertTrue(MathArrays.isMonotonic(decNonStrict, MathArrays.OrderDirection.DECREASING, false));
        Assert.assertFalse(MathArrays.isMonotonic(decNonStrict, MathArrays.OrderDirection.DECREASING, true));

        Assert.assertFalse(MathArrays.checkOrder(unord, MathArrays.OrderDirection.INCREASING, true, false));
        Assert.assertFalse(MathArrays.checkOrder(unord, MathArrays.OrderDirection.DECREASING, false, false));

        MathArrays.checkOrder(incStrict);
        MathArrays.checkOrder(decStrict, MathArrays.OrderDirection.DECREASING, true);
    }

    @Test(expected = NonMonotonicSequenceException.class)
    public void testCheckOrderException1() {
        MathArrays.checkOrder(new double[]{1.0, 3.0, 2.0});
    }

    @Test(expected = NonMonotonicSequenceException.class)
    public void testCheckOrderException2() {
        MathArrays.checkOrder(new double[]{3.0, 1.0, 2.0}, MathArrays.OrderDirection.DECREASING, true);
    }

    @Test
    public void testCheckRectangular() {
        long[][] rect = {{1, 2}, {3, 4}, {5, 6}};
        MathArrays.checkRectangular(rect);
    }

    @Test(expected = NullArgumentException.class)
    public void testCheckRectangularNull() {
        MathArrays.checkRectangular(null);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testCheckRectangularMismatch() {
        long[][] nonRect = {{1, 2}, {3, 4, 5}};
        MathArrays.checkRectangular(nonRect);
    }

    @Test
    public void testCheckPositive() {
        MathArrays.checkPositive(new double[]{0.1, 1.0, 10.0});
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testCheckPositiveFail() {
        MathArrays.checkPositive(new double[]{0.1, 0.0, 10.0});
    }

    @Test
    public void testCheckNonNegative1D() {
        MathArrays.checkNonNegative(new long[]{0, 1, 10});
    }

    @Test(expected = NotPositiveException.class)
    public void testCheckNonNegative1DFail() {
        MathArrays.checkNonNegative(new long[]{0, -1, 10});
    }

    @Test
    public void testCheckNonNegative2D() {
        MathArrays.checkNonNegative(new long[][]{{0, 1}, {2, 3}});
    }

    @Test(expected = NotPositiveException.class)
    public void testCheckNonNegative2DFail() {
        MathArrays.checkNonNegative(new long[][]{{0, 1}, {-2, 3}});
    }

    @Test
    public void testSafeNorm() {
        Assert.assertEquals(5.0, MathArrays.safeNorm(new double[]{3.0, 4.0}), 1e-15);
        Assert.assertEquals(0.0, MathArrays.safeNorm(new double[]{0.0, 0.0}), 1e-15);

        double[] tiny = {1.0e-25, 2.0e-25};
        Assert.assertTrue(MathArrays.safeNorm(tiny) > 0);

        double[] tinyZero = {0.0, 1.0e-25, 0.0};
        Assert.assertTrue(MathArrays.safeNorm(tinyZero) > 0);

        double[] huge = {1.0e20, 2.0e20};
        Assert.assertTrue(MathArrays.safeNorm(huge) > 1.0e20);

        double[] mixedS2GeX3max = {1.0, 1.0e-25};
        Assert.assertTrue(MathArrays.safeNorm(mixedS2GeX3max) >= 1.0);

        double[] mixedS2LtX3max = {1.0e-22, 1.0e-25};
        Assert.assertTrue(MathArrays.safeNorm(mixedS2LtX3max) > 0);
    }

    @Test
    public void testSortInPlace() {
        double[] x = {3.0, 1.0, 2.0};
        double[] y = {30.0, 10.0, 20.0};
        double[] z = {300.0, 100.0, 200.0};

        MathArrays.sortInPlace(x, y, z);
        Assert.assertArrayEquals(new double[]{1.0, 2.0, 3.0}, x, 1e-15);
        Assert.assertArrayEquals(new double[]{10.0, 20.0, 30.0}, y, 1e-15);
        Assert.assertArrayEquals(new double[]{100.0, 200.0, 300.0}, z, 1e-15);

        MathArrays.sortInPlace(x, MathArrays.OrderDirection.DECREASING, y, z);
        Assert.assertArrayEquals(new double[]{3.0, 2.0, 1.0}, x, 1e-15);
        Assert.assertArrayEquals(new double[]{30.0, 20.0, 10.0}, y, 1e-15);
        Assert.assertArrayEquals(new double[]{300.0, 200.0, 100.0}, z, 1e-15);
    }

    @Test(expected = NullArgumentException.class)
    public void testSortInPlaceNullX() {
        MathArrays.sortInPlace(null, new double[]{1.0});
    }

    @Test(expected = NullArgumentException.class)
    public void testSortInPlaceNullY() {
        MathArrays.sortInPlace(new double[]{1.0}, (double[]) null);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testSortInPlaceMismatch() {
        MathArrays.sortInPlace(new double[]{1.0, 2.0}, new double[]{1.0});
    }

    @Test
    public void testCopyOf() {
        int[] srcInt = {1, 2, 3};
        Assert.assertArrayEquals(srcInt, MathArrays.copyOf(srcInt));
        Assert.assertArrayEquals(new int[]{1, 2}, MathArrays.copyOf(srcInt, 2));
        Assert.assertArrayEquals(new int[]{1, 2, 3, 0}, MathArrays.copyOf(srcInt, 4));

        double[] srcDouble = {1.0, 2.0, 3.0};
        Assert.assertArrayEquals(srcDouble, MathArrays.copyOf(srcDouble), 1e-15);
        Assert.assertArrayEquals(new double[]{1.0, 2.0}, MathArrays.copyOf(srcDouble, 2), 1e-15);
        Assert.assertArrayEquals(new double[]{1.0, 2.0, 3.0, 0.0}, MathArrays.copyOf(srcDouble, 4), 1e-15);
    }

    @Test
    public void testLinearCombinationArray() {
        double[] a = {1.0, 2.0, 3.0};
        double[] b = {4.0, 5.0, 6.0};
        Assert.assertEquals(32.0, MathArrays.linearCombination(a, b), 1e-15);

        double[] a2 = {1.0, 2.0};
        double[] b2 = {3.0, 4.0};
        Assert.assertEquals(11.0, MathArrays.linearCombination(a2, b2), 1e-15);

        double[] aNaN = {Double.NaN, 2.0};
        double[] bNaN = {1.0, 2.0};
        Assert.assertTrue(Double.isNaN(MathArrays.linearCombination(aNaN, bNaN)));
    }

    @Test(expected = DimensionMismatchException.class)
    public void testLinearCombinationArrayMismatch() {
        MathArrays.linearCombination(new double[]{1.0}, new double[]{1.0, 2.0});
    }

    @Test
    public void testLinearCombination2Terms() {
        Assert.assertEquals(11.0, MathArrays.linearCombination(1.0, 3.0, 2.0, 4.0), 1e-15);
        Assert.assertTrue(Double.isNaN(MathArrays.linearCombination(Double.NaN, 1.0, 2.0, 3.0)));
    }

    @Test
    public void testLinearCombination3Terms() {
        Assert.assertEquals(32.0, MathArrays.linearCombination(1.0, 4.0, 2.0, 5.0, 3.0, 6.0), 1e-15);
        Assert.assertTrue(Double.isNaN(MathArrays.linearCombination(Double.NaN, 1.0, 2.0, 3.0, 4.0, 5.0)));
    }

    @Test
    public void testLinearCombination4Terms() {
        Assert.assertEquals(70.0, MathArrays.linearCombination(1.0, 4.0, 2.0, 5.0, 3.0, 6.0, 4.0, 7.0), 1e-15);
        Assert.assertTrue(Double.isNaN(MathArrays.linearCombination(Double.NaN, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0)));
    }

    @Test
    public void testEqualsFloat() {
        float[] a = {1.0f, 2.0f, Float.NaN};
        float[] b = {1.0f, 2.0f, Float.NaN};
        float[] c = {1.0f, 2.0f, 3.0f};

        Assert.assertTrue(MathArrays.equals((float[]) null, (float[]) null));
        Assert.assertFalse(MathArrays.equals((float[]) null, a));
        Assert.assertFalse(MathArrays.equals(a, (float[]) null));
        Assert.assertFalse(MathArrays.equals(a, new float[]{1.0f}));
        Assert.assertFalse(MathArrays.equals(a, b));
        Assert.assertTrue(MathArrays.equals(c, new float[]{1.0f, 2.0f, 3.0f}));
        Assert.assertFalse(MathArrays.equals(c, new float[]{1.0f, 2.0f, 4.0f}));

        Assert.assertTrue(MathArrays.equalsIncludingNaN((float[]) null, (float[]) null));
        Assert.assertFalse(MathArrays.equalsIncludingNaN((float[]) null, a));
        Assert.assertFalse(MathArrays.equalsIncludingNaN(a, (float[]) null));
        Assert.assertFalse(MathArrays.equalsIncludingNaN(a, new float[]{1.0f}));
        Assert.assertTrue(MathArrays.equalsIncludingNaN(a, b));
        Assert.assertFalse(MathArrays.equalsIncludingNaN(a, c));
    }

    @Test
    public void testEqualsDouble() {
        double[] a = {1.0, 2.0, Double.NaN};
        double[] b = {1.0, 2.0, Double.NaN};
        double[] c = {1.0, 2.0, 3.0};

        Assert.assertTrue(MathArrays.equals((double[]) null, (double[]) null));
        Assert.assertFalse(MathArrays.equals((double[]) null, a));
        Assert.assertFalse(MathArrays.equals(a, (double[]) null));
        Assert.assertFalse(MathArrays.equals(a, new double[]{1.0}));
        Assert.assertFalse(MathArrays.equals(a, b));
        Assert.assertTrue(MathArrays.equals(c, new double[]{1.0, 2.0, 3.0}));
        Assert.assertFalse(MathArrays.equals(c, new double[]{1.0, 2.0, 4.0}));

        Assert.assertTrue(MathArrays.equalsIncludingNaN((double[]) null, (double[]) null));
        Assert.assertFalse(MathArrays.equalsIncludingNaN((double[]) null, a));
        Assert.assertFalse(MathArrays.equalsIncludingNaN(a, (double[]) null));
        Assert.assertFalse(MathArrays.equalsIncludingNaN(a, new double[]{1.0}));
        Assert.assertTrue(MathArrays.equalsIncludingNaN(a, b));
        Assert.assertFalse(MathArrays.equalsIncludingNaN(a, c));
    }

    @Test
    public void testNormalizeArray() {
        double[] values = {1.0, 2.0, Double.NaN, 3.0};
        double[] norm = MathArrays.normalizeArray(values, 12.0);
        Assert.assertEquals(2.0, norm[0], 1e-15);
        Assert.assertEquals(4.0, norm[1], 1e-15);
        Assert.assertTrue(Double.isNaN(norm[2]));
        Assert.assertEquals(6.0, norm[3], 1e-15);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testNormalizeArrayInfiniteTarget() {
        MathArrays.normalizeArray(new double[]{1.0}, Double.POSITIVE_INFINITY);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testNormalizeArrayNanTarget() {
        MathArrays.normalizeArray(new double[]{1.0}, Double.NaN);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testNormalizeArrayInfiniteElement() {
        MathArrays.normalizeArray(new double[]{1.0, Double.POSITIVE_INFINITY}, 1.0);
    }

    @Test(expected = MathArithmeticException.class)
    public void testNormalizeArrayZeroSum() {
        MathArrays.normalizeArray(new double[]{0.0, 0.0}, 1.0);
    }

    @Test
    public void testBuildArray() {
        Field<Fraction> field = FractionField.getInstance();
        Fraction[] arr1D = MathArrays.buildArray(field, 3);
        Assert.assertEquals(3, arr1D.length);
        Assert.assertEquals(Fraction.ZERO, arr1D[0]);
        Assert.assertEquals(Fraction.ZERO, arr1D[1]);
        Assert.assertEquals(Fraction.ZERO, arr1D[2]);

        Fraction[][] arr2D = MathArrays.buildArray(field, 2, 3);
        Assert.assertEquals(2, arr2D.length);
        Assert.assertEquals(3, arr2D[0].length);
        Assert.assertEquals(Fraction.ZERO, arr2D[0][0]);
        Assert.assertEquals(Fraction.ZERO, arr2D[1][2]);

        Fraction[][] partialArr = MathArrays.buildArray(field, 2, -1);
        Assert.assertEquals(2, partialArr.length);
        Assert.assertNull(partialArr[0]);
    }

    @Test
    public void testConvolve() {
        double[] x = {1.0, 2.0, 3.0};
        double[] h = {0.5, 1.0};
        double[] expected = {0.5, 2.0, 3.5, 3.0};
        Assert.assertArrayEquals(expected, MathArrays.convolve(x, h), 1e-15);
    }

    @Test(expected = NullArgumentException.class)
    public void testConvolveNullX() {
        MathArrays.convolve(null, new double[]{1.0});
    }

    @Test(expected = NullArgumentException.class)
    public void testConvolveNullH() {
        MathArrays.convolve(new double[]{1.0}, null);
    }

    @Test(expected = NoDataException.class)
    public void testConvolveEmptyX() {
        MathArrays.convolve(new double[0], new double[]{1.0});
    }

    @Test(expected = NoDataException.class)
    public void testConvolveEmptyH() {
        MathArrays.convolve(new double[]{1.0}, new double[0]);
    }

    @Test
    public void testFunctionInterface() {
        MathArrays.Function sumFunc = new MathArrays.Function() {
            public double evaluate(double[] array) {
                return evaluate(array, 0, array.length);
            }

            public double evaluate(double[] array, int startIndex, int numElements) {
                double s = 0;
                for (int i = startIndex; i < startIndex + numElements; i++) {
                    s += array[i];
                }
                return s;
            }
        };

        double[] data = {1.0, 2.0, 3.0, 4.0};
        Assert.assertEquals(10.0, sumFunc.evaluate(data), 1e-15);
        Assert.assertEquals(5.0, sumFunc.evaluate(data, 1, 2), 1e-15);
    }
}
