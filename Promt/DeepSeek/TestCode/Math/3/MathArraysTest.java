package org.apache.commons.math3.util;

import org.junit.Assert;
import org.junit.Test;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.exception.NonMonotonicSequenceException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.Field;
import org.apache.commons.math3.util.MathArrays.OrderDirection;

public class MathArraysTest {

    @Test
    public void testScale() {
        double[] arr = {1.0, 2.0, 3.0};
        double[] scaled = MathArrays.scale(2.0, arr);
        Assert.assertArrayEquals(new double[]{2.0, 4.0, 6.0}, scaled, 0.0);
        Assert.assertArrayEquals(new double[]{1.0, 2.0, 3.0}, arr, 0.0);
    }

    @Test
    public void testScaleEmpty() {
        double[] arr = {};
        double[] scaled = MathArrays.scale(5.0, arr);
        Assert.assertEquals(0, scaled.length);
    }

    @Test
    public void testScaleInPlace() {
        double[] arr = {1.0, 2.0, 3.0};
        MathArrays.scaleInPlace(2.0, arr);
        Assert.assertArrayEquals(new double[]{2.0, 4.0, 6.0}, arr, 0.0);
    }

    @Test
    public void testScaleInPlaceEmpty() {
        double[] arr = {};
        MathArrays.scaleInPlace(5.0, arr);
        Assert.assertEquals(0, arr.length);
    }

    @Test
    public void testEbeAdd() {
        double[] a = {1.0, 2.0, 3.0};
        double[] b = {4.0, 5.0, 6.0};
        double[] result = MathArrays.ebeAdd(a, b);
        Assert.assertArrayEquals(new double[]{5.0, 7.0, 9.0}, result, 0.0);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testEbeAddDimensionMismatch() {
        MathArrays.ebeAdd(new double[]{1.0}, new double[]{1.0, 2.0});
    }

    @Test
    public void testEbeSubtract() {
        double[] a = {5.0, 7.0, 9.0};
        double[] b = {1.0, 2.0, 3.0};
        double[] result = MathArrays.ebeSubtract(a, b);
        Assert.assertArrayEquals(new double[]{4.0, 5.0, 6.0}, result, 0.0);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testEbeSubtractDimensionMismatch() {
        MathArrays.ebeSubtract(new double[]{1.0}, new double[]{1.0, 2.0});
    }

    @Test
    public void testEbeMultiply() {
        double[] a = {2.0, 3.0, 4.0};
        double[] b = {5.0, 6.0, 7.0};
        double[] result = MathArrays.ebeMultiply(a, b);
        Assert.assertArrayEquals(new double[]{10.0, 18.0, 28.0}, result, 0.0);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testEbeMultiplyDimensionMismatch() {
        MathArrays.ebeMultiply(new double[]{1.0}, new double[]{1.0, 2.0});
    }

    @Test
    public void testEbeDivide() {
        double[] a = {10.0, 20.0, 30.0};
        double[] b = {2.0, 4.0, 5.0};
        double[] result = MathArrays.ebeDivide(a, b);
        Assert.assertArrayEquals(new double[]{5.0, 5.0, 6.0}, result, 0.0);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testEbeDivideDimensionMismatch() {
        MathArrays.ebeDivide(new double[]{1.0}, new double[]{1.0, 2.0});
    }

    @Test
    public void testDistance1Double() {
        double[] p1 = {1.0, 2.0, 3.0};
        double[] p2 = {4.0, 5.0, 6.0};
        double dist = MathArrays.distance1(p1, p2);
        Assert.assertEquals(9.0, dist, 0.0);
    }

    @Test
    public void testDistance1Int() {
        int[] p1 = {1, 2, 3};
        int[] p2 = {4, 5, 6};
        int dist = MathArrays.distance1(p1, p2);
        Assert.assertEquals(9, dist);
    }

    @Test
    public void testDistanceDouble() {
        double[] p1 = {0.0, 0.0};
        double[] p2 = {3.0, 4.0};
        double dist = MathArrays.distance(p1, p2);
        Assert.assertEquals(5.0, dist, 0.0);
    }

    @Test
    public void testDistanceInt() {
        int[] p1 = {0, 0};
        int[] p2 = {3, 4};
        double dist = MathArrays.distance(p1, p2);
        Assert.assertEquals(5.0, dist, 0.0);
    }

    @Test
    public void testDistanceInfDouble() {
        double[] p1 = {1.0, 2.0, 3.0};
        double[] p2 = {4.0, 1.0, 6.0};
        double dist = MathArrays.distanceInf(p1, p2);
        Assert.assertEquals(3.0, dist, 0.0);
    }

    @Test
    public void testDistanceInfInt() {
        int[] p1 = {1, 2, 3};
        int[] p2 = {4, 1, 6};
        int dist = MathArrays.distanceInf(p1, p2);
        Assert.assertEquals(3, dist);
    }

    @Test
    public void testIsMonotonicComparableIncreasingStrict() {
        Integer[] arr = {1, 2, 3, 4};
        Assert.assertTrue(MathArrays.isMonotonic(arr, OrderDirection.INCREASING, true));
    }

    @Test
    public void testIsMonotonicComparableIncreasingNotStrict() {
        Integer[] arr = {1, 2, 2, 3};
        Assert.assertTrue(MathArrays.isMonotonic(arr, OrderDirection.INCREASING, false));
    }

    @Test
    public void testIsMonotonicComparableIncreasingStrictFalse() {
        Integer[] arr = {1, 2, 2, 3};
        Assert.assertFalse(MathArrays.isMonotonic(arr, OrderDirection.INCREASING, true));
    }

    @Test
    public void testIsMonotonicComparableDecreasingStrict() {
        Integer[] arr = {4, 3, 2, 1};
        Assert.assertTrue(MathArrays.isMonotonic(arr, OrderDirection.DECREASING, true));
    }

    @Test
    public void testIsMonotonicComparableDecreasingNotStrict() {
        Integer[] arr = {4, 3, 3, 1};
        Assert.assertTrue(MathArrays.isMonotonic(arr, OrderDirection.DECREASING, false));
    }

    @Test
    public void testIsMonotonicComparableDecreasingStrictFalse() {
        Integer[] arr = {4, 3, 3, 1};
        Assert.assertFalse(MathArrays.isMonotonic(arr, OrderDirection.DECREASING, true));
    }

    @Test
    public void testIsMonotonicDoubleIncreasingStrict() {
        double[] arr = {1.0, 2.0, 3.0};
        Assert.assertTrue(MathArrays.isMonotonic(arr, OrderDirection.INCREASING, true));
    }

    @Test
    public void testIsMonotonicDoubleDecreasingNotStrict() {
        double[] arr = {3.0, 2.0, 2.0, 1.0};
        Assert.assertTrue(MathArrays.isMonotonic(arr, OrderDirection.DECREASING, false));
    }

    @Test
    public void testCheckOrderDoubleIncreasingStrictAbort() {
        double[] arr = {1.0, 2.0, 3.0};
        Assert.assertTrue(MathArrays.checkOrder(arr, OrderDirection.INCREASING, true, true));
    }

    @Test(expected = NonMonotonicSequenceException.class)
    public void testCheckOrderDoubleIncreasingStrictAbortThrows() {
        double[] arr = {1.0, 2.0, 2.0};
        MathArrays.checkOrder(arr, OrderDirection.INCREASING, true, true);
    }

    @Test
    public void testCheckOrderDoubleIncreasingStrictNoAbort() {
        double[] arr = {1.0, 2.0, 2.0};
        Assert.assertFalse(MathArrays.checkOrder(arr, OrderDirection.INCREASING, true, false));
    }

    @Test
    public void testCheckOrderDoubleDecreasingNotStrictAbort() {
        double[] arr = {3.0, 2.0, 2.0, 1.0};
        Assert.assertTrue(MathArrays.checkOrder(arr, OrderDirection.DECREASING, false, true));
    }

    @Test(expected = NonMonotonicSequenceException.class)
    public void testCheckOrderDoubleDecreasingNotStrictAbortThrows() {
        double[] arr = {3.0, 2.0, 3.0};
        MathArrays.checkOrder(arr, OrderDirection.DECREASING, false, true);
    }

    @Test
    public void testCheckOrderDoubleDecreasingStrictNoAbort() {
        double[] arr = {3.0, 2.0, 2.0};
        Assert.assertFalse(MathArrays.checkOrder(arr, OrderDirection.DECREASING, true, false));
    }

    @Test(expected = NonMonotonicSequenceException.class)
    public void testCheckOrderDoubleDefault() {
        double[] arr = {3.0, 2.0, 1.0};
        MathArrays.checkOrder(arr, OrderDirection.INCREASING, true);
    }

    @Test(expected = NonMonotonicSequenceException.class)
    public void testCheckOrderDoubleStrictlyIncreasing() {
        double[] arr = {1.0, 1.0};
        MathArrays.checkOrder(arr);
    }

    @Test
    public void testCheckOrderDoubleValid() {
        double[] arr = {1.0, 2.0, 3.0};
        MathArrays.checkOrder(arr);
    }

    @Test
    public void testCheckRectangularValid() {
        long[][] arr = {{1, 2}, {3, 4}, {5, 6}};
        MathArrays.checkRectangular(arr);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testCheckRectangularInvalid() {
        long[][] arr = {{1, 2}, {3, 4, 5}};
        MathArrays.checkRectangular(arr);
    }

    @Test(expected = NullArgumentException.class)
    public void testCheckRectangularNull() {
        MathArrays.checkRectangular(null);
    }

    @Test
    public void testCheckPositiveValid() {
        double[] arr = {1.0, 2.0, 3.0};
        MathArrays.checkPositive(arr);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testCheckPositiveInvalidZero() {
        double[] arr = {1.0, 0.0, 3.0};
        MathArrays.checkPositive(arr);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testCheckPositiveInvalidNegative() {
        double[] arr = {1.0, -1.0, 3.0};
        MathArrays.checkPositive(arr);
    }

    @Test
    public void testCheckNonNegativeLongValid() {
        long[] arr = {0, 1, 2};
        MathArrays.checkNonNegative(arr);
    }

    @Test(expected = NotPositiveException.class)
    public void testCheckNonNegativeLongInvalid() {
        long[] arr = {0, -1, 2};
        MathArrays.checkNonNegative(arr);
    }

    @Test
    public void testCheckNonNegativeLong2DValid() {
        long[][] arr = {{0, 1}, {2, 3}};
        MathArrays.checkNonNegative(arr);
    }

    @Test(expected = NotPositiveException.class)
    public void testCheckNonNegativeLong2DInvalid() {
        long[][] arr = {{0, 1}, {-1, 3}};
        MathArrays.checkNonNegative(arr);
    }

    @Test
    public void testSafeNorm() {
        double[] v = {3.0, 4.0};
        double norm = MathArrays.safeNorm(v);
        Assert.assertEquals(5.0, norm, 0.0);
    }

    @Test
    public void testSafeNormLarge() {
        double[] v = {1e200, 1e200};
        double norm = MathArrays.safeNorm(v);
        Assert.assertTrue(Double.isFinite(norm));
    }

    @Test
    public void testSafeNormSmall() {
        double[] v = {1e-200, 1e-200};
        double norm = MathArrays.safeNorm(v);
        Assert.assertTrue(norm > 0);
    }

    @Test
    public void testSafeNormZero() {
        double[] v = {0.0, 0.0, 0.0};
        double norm = MathArrays.safeNorm(v);
        Assert.assertEquals(0.0, norm, 0.0);
    }

    @Test
    public void testSortInPlaceIncreasing() {
        double[] x = {3.0, 1.0, 2.0};
        double[] y = {1.0, 2.0, 3.0};
        double[] z = {0.0, 5.0, 7.0};
        MathArrays.sortInPlace(x, y, z);
        Assert.assertArrayEquals(new double[]{1.0, 2.0, 3.0}, x, 0.0);
        Assert.assertArrayEquals(new double[]{2.0, 3.0, 1.0}, y, 0.0);
        Assert.assertArrayEquals(new double[]{5.0, 7.0, 0.0}, z, 0.0);
    }

    @Test
    public void testSortInPlaceDecreasing() {
        double[] x = {1.0, 3.0, 2.0};
        double[] y = {1.0, 2.0, 3.0};
        MathArrays.sortInPlace(x, OrderDirection.DECREASING, y);
        Assert.assertArrayEquals(new double[]{3.0, 2.0, 1.0}, x, 0.0);
        Assert.assertArrayEquals(new double[]{2.0, 3.0, 1.0}, y, 0.0);
    }

    @Test(expected = NullArgumentException.class)
    public void testSortInPlaceNullX() {
        MathArrays.sortInPlace(null, new double[]{1.0});
    }

    @Test(expected = NullArgumentException.class)
    public void testSortInPlaceNullY() {
        MathArrays.sortInPlace(new double[]{1.0}, new double[]{null});
    }

    @Test(expected = DimensionMismatchException.class)
    public void testSortInPlaceDimensionMismatch() {
        MathArrays.sortInPlace(new double[]{1.0, 2.0}, new double[]{1.0});
    }

    @Test
    public void testCopyOfInt() {
        int[] source = {1, 2, 3};
        int[] copy = MathArrays.copyOf(source);
        Assert.assertArrayEquals(source, copy);
    }

    @Test
    public void testCopyOfIntTruncate() {
        int[] source = {1, 2, 3};
        int[] copy = MathArrays.copyOf(source, 2);
        Assert.assertArrayEquals(new int[]{1, 2}, copy);
    }

    @Test
    public void testCopyOfIntPad() {
        int[] source = {1, 2};
        int[] copy = MathArrays.copyOf(source, 4);
        Assert.assertArrayEquals(new int[]{1, 2, 0, 0}, copy);
    }

    @Test
    public void testCopyOfDouble() {
        double[] source = {1.0, 2.0, 3.0};
        double[] copy = MathArrays.copyOf(source);
        Assert.assertArrayEquals(source, copy, 0.0);
    }

    @Test
    public void testCopyOfDoubleTruncate() {
        double[] source = {1.0, 2.0, 3.0};
        double[] copy = MathArrays.copyOf(source, 2);
        Assert.assertArrayEquals(new double[]{1.0, 2.0}, copy, 0.0);
    }

    @Test
    public void testCopyOfDoublePad() {
        double[] source = {1.0, 2.0};
        double[] copy = MathArrays.copyOf(source, 4);
        Assert.assertArrayEquals(new double[]{1.0, 2.0, 0.0, 0.0}, copy, 0.0);
    }

    @Test
    public void testLinearCombinationArray() {
        double[] a = {1.0, 2.0, 3.0};
        double[] b = {4.0, 5.0, 6.0};
        double result = MathArrays.linearCombination(a, b);
        Assert.assertEquals(32.0, result, 0.0);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testLinearCombinationArrayDimensionMismatch() {
        MathArrays.linearCombination(new double[]{1.0}, new double[]{1.0, 2.0});
    }

    @Test
    public void testLinearCombinationTwo() {
        double result = MathArrays.linearCombination(1.0, 2.0, 3.0, 4.0);
        Assert.assertEquals(14.0, result, 0.0);
    }

    @Test
    public void testLinearCombinationThree() {
        double result = MathArrays.linearCombination(1.0, 2.0, 3.0, 4.0, 5.0, 6.0);
        Assert.assertEquals(44.0, result, 0.0);
    }

    @Test
    public void testLinearCombinationFour() {
        double result = MathArrays.linearCombination(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0);
        Assert.assertEquals(100.0, result, 0.0);
    }

    @Test
    public void testLinearCombinationNaN() {
        double result = MathArrays.linearCombination(Double.NaN, 1.0, 2.0, 3.0);
        Assert.assertTrue(Double.isNaN(result));
    }

    @Test
    public void testLinearCombinationInfinite() {
        double result = MathArrays.linearCombination(Double.POSITIVE_INFINITY, 1.0, 2.0, 3.0);
        Assert.assertTrue(Double.isInfinite(result));
    }

    @Test
    public void testEqualsFloat() {
        float[] x = {1.0f, 2.0f, 3.0f};
        float[] y = {1.0f, 2.0f, 3.0f};
        Assert.assertTrue(MathArrays.equals(x, y));
    }

    @Test
    public void testEqualsFloatFalse() {
        float[] x = {1.0f, 2.0f, 3.0f};
        float[] y = {1.0f, 2.0f, 4.0f};
        Assert.assertFalse(MathArrays.equals(x, y));
    }

    @Test
    public void testEqualsFloatNull() {
        Assert.assertTrue(MathArrays.equals(null, null));
        Assert.assertFalse(MathArrays.equals(null, new float[]{1.0f}));
        Assert.assertFalse(MathArrays.equals(new float[]{1.0f}, null));
    }

    @Test
    public void testEqualsFloatLengthMismatch() {
        Assert.assertFalse(MathArrays.equals(new float[]{1.0f}, new float[]{1.0f, 2.0f}));
    }

    @Test
    public void testEqualsIncludingNaNFloat() {
        float[] x = {1.0f, Float.NaN, 3.0f};
        float[] y = {1.0f, Float.NaN, 3.0f};
        Assert.assertTrue(MathArrays.equalsIncludingNaN(x, y));
    }

    @Test
    public void testEqualsIncludingNaNFloatFalse() {
        float[] x = {1.0f, Float.NaN, 3.0f};
        float[] y = {1.0f, 2.0f, 3.0f};
        Assert.assertFalse(MathArrays.equalsIncludingNaN(x, y));
    }

    @Test
    public void testEqualsDouble() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {1.0, 2.0, 3.0};
        Assert.assertTrue(MathArrays.equals(x, y));
    }

    @Test
    public void testEqualsDoubleFalse() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {1.0, 2.0, 4.0};
        Assert.assertFalse(MathArrays.equals(x, y));
    }

    @Test
    public void testEqualsDoubleNull() {
        Assert.assertTrue(MathArrays.equals(null, null));
        Assert.assertFalse(MathArrays.equals(null, new double[]{1.0}));
        Assert.assertFalse(MathArrays.equals(new double[]{1.0}, null));
    }

    @Test
    public void testEqualsDoubleLengthMismatch() {
        Assert.assertFalse(MathArrays.equals(new double[]{1.0}, new double[]{1.0, 2.0}));
    }

    @Test
    public void testEqualsIncludingNaNDouble() {
        double[] x = {1.0, Double.NaN, 3.0};
        double[] y = {1.0, Double.NaN, 3.0};
        Assert.assertTrue(MathArrays.equalsIncludingNaN(x, y));
    }

    @Test
    public void testEqualsIncludingNaNDoubleFalse() {
        double[] x = {1.0, Double.NaN, 3.0};
        double[] y = {1.0, 2.0, 3.0};
        Assert.assertFalse(MathArrays.equalsIncludingNaN(x, y));
    }

    @Test
    public void testNormalizeArray() {
        double[] values = {1.0, 2.0, 3.0};
        double[] normalized = MathArrays.normalizeArray(values, 1.0);
        double sum = 0;
        for (double v : normalized) sum += v;
        Assert.assertEquals(1.0, sum, 1e-15);
    }

    @Test
    public void testNormalizeArrayWithNaN() {
        double[] values = {1.0, Double.NaN, 3.0};
        double[] normalized = MathArrays.normalizeArray(values, 1.0);
        Assert.assertTrue(Double.isNaN(normalized[1]));
        double sum = normalized[0] + normalized[2];
        Assert.assertEquals(1.0, sum, 1e-15);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testNormalizeArrayInfiniteTarget() {
        MathArrays.normalizeArray(new double[]{1.0}, Double.POSITIVE_INFINITY);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testNormalizeArrayNaNTarget() {
        MathArrays.normalizeArray(new double[]{1.0}, Double.NaN);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testNormalizeArrayInfiniteElement() {
        MathArrays.normalizeArray(new double[]{Double.POSITIVE_INFINITY}, 1.0);
    }

    @Test(expected = MathArithmeticException.class)
    public void testNormalizeArraySumZero() {
        MathArrays.normalizeArray(new double[]{0.0, 0.0}, 1.0);
    }

    @Test
    public void testBuildArray1D() {
        Field<DummyFieldElement> field = new DummyField();
        DummyFieldElement[] array = MathArrays.buildArray(field, 3);
        Assert.assertEquals(3, array.length);
        for (DummyFieldElement e : array) {
            Assert.assertEquals(field.getZero(), e);
        }
    }

    @Test
    public void testBuildArray2D() {
        Field<DummyFieldElement> field = new DummyField();
        DummyFieldElement[][] array = MathArrays.buildArray(field, 2, 3);
        Assert.assertEquals(2, array.length);
        for (DummyFieldElement[] row : array) {
            Assert.assertEquals(3, row.length);
            for (DummyFieldElement e : row) {
                Assert.assertEquals(field.getZero(), e);
            }
        }
    }

    @Test
    public void testBuildArray2DNegativeColumns() {
        Field<DummyFieldElement> field = new DummyField();
        DummyFieldElement[][] array = MathArrays.buildArray(field, 2, -1);
        Assert.assertEquals(2, array.length);
        for (DummyFieldElement[] row : array) {
            Assert.assertNull(row);
        }
    }

    @Test
    public void testConvolve() {
        double[] x = {1.0, 2.0, 3.0};
        double[] h = {0.0, 1.0, 0.5};
        double[] result = MathArrays.convolve(x, h);
        double[] expected = {0.0, 1.0, 2.5, 4.0, 1.5};
        Assert.assertArrayEquals(expected, result, 1e-15);
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
        MathArrays.convolve(new double[]{}, new double[]{1.0});
    }

    @Test(expected = NoDataException.class)
    public void testConvolveEmptyH() {
        MathArrays.convolve(new double[]{1.0}, new double[]{});
    }

    private static class DummyFieldElement {
        private final double value;
        public DummyFieldElement(double value) { this.value = value; }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DummyFieldElement)) return false;
            return Double.compare(((DummyFieldElement) o).value, value) == 0;
        }
    }

    private static class DummyField implements Field<DummyFieldElement> {
        public DummyFieldElement getZero() { return new DummyFieldElement(0.0); }
        public DummyFieldElement getOne() { return new DummyFieldElement(1.0); }
        public Class<? extends DummyFieldElement> getRuntimeClass() { return DummyFieldElement.class; }
    }
}
