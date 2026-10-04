package org.mockito.internal.matchers;

import org.junit.Assert;
import org.junit.Test;

public class EqualityTest {

    @Test
    public void testBothNull() {
        Assert.assertTrue(Equality.areEqual(null, null));
    }

    @Test
    public void testFirstNullSecondNotNull() {
        Assert.assertFalse(Equality.areEqual(null, "test"));
        Assert.assertFalse(Equality.areEqual(null, new int[]{1, 2}));
    }

    @Test
    public void testFirstNotNullSecondNull() {
        Assert.assertFalse(Equality.areEqual("test", null));
        Assert.assertFalse(Equality.areEqual(new int[]{1, 2}, null));
    }

    @Test
    public void testNonArrayEqualObjects() {
        Assert.assertTrue(Equality.areEqual("hello", "hello"));
        Assert.assertTrue(Equality.areEqual(100, 100));
        Assert.assertTrue(Equality.areEqual(new java.math.BigDecimal("1.0"), new java.math.BigDecimal("1.0")));
    }

    @Test
    public void testNonArrayUnequalObjects() {
        Assert.assertFalse(Equality.areEqual("hello", "world"));
        Assert.assertFalse(Equality.areEqual(100, 200));
        Assert.assertFalse(Equality.areEqual(100, "100"));
    }

    @Test
    public void testFirstIsArraySecondIsNotArray() {
        Assert.assertFalse(Equality.areEqual(new int[]{1, 2}, "not an array"));
        Assert.assertFalse(Equality.areEqual(new String[]{"a"}, 123));
    }

    @Test
    public void testFirstIsNotArraySecondIsArray() {
        Assert.assertFalse(Equality.areEqual("not an array", new int[]{1, 2}));
        Assert.assertFalse(Equality.areEqual(123, new String[]{"a"}));
    }

    @Test
    public void testPrimitiveArraysEqual() {
        Assert.assertTrue(Equality.areEqual(new int[]{1, 2, 3}, new int[]{1, 2, 3}));
        Assert.assertTrue(Equality.areEqual(new boolean[]{true, false}, new boolean[]{true, false}));
        Assert.assertTrue(Equality.areEqual(new byte[]{1, 2}, new byte[]{1, 2}));
        Assert.assertTrue(Equality.areEqual(new char[]{'a', 'b'}, new char[]{'a', 'b'}));
        Assert.assertTrue(Equality.areEqual(new short[]{1, 2}, new short[]{1, 2}));
        Assert.assertTrue(Equality.areEqual(new long[]{1L, 2L}, new long[]{1L, 2L}));
        Assert.assertTrue(Equality.areEqual(new float[]{1.0f, 2.5f}, new float[]{1.0f, 2.5f}));
        Assert.assertTrue(Equality.areEqual(new double[]{1.0, 2.5}, new double[]{1.0, 2.5}));
    }

    @Test
    public void testPrimitiveArraysUnequalValues() {
        Assert.assertFalse(Equality.areEqual(new int[]{1, 2, 3}, new int[]{1, 2, 4}));
        Assert.assertFalse(Equality.areEqual(new boolean[]{true, false}, new boolean[]{true, true}));
        Assert.assertFalse(Equality.areEqual(new char[]{'a', 'b'}, new char[]{'a', 'c'}));
        Assert.assertFalse(Equality.areEqual(new double[]{1.0, 2.0}, new double[]{1.0, 2.1}));
    }

    @Test
    public void testPrimitiveArraysDifferentLengths() {
        Assert.assertFalse(Equality.areEqual(new int[]{1, 2}, new int[]{1, 2, 3}));
        Assert.assertFalse(Equality.areEqual(new int[]{1, 2, 3}, new int[]{1, 2}));
        Assert.assertFalse(Equality.areEqual(new boolean[]{}, new boolean[]{true}));
    }

    @Test
    public void testEmptyArraysEqual() {
        Assert.assertTrue(Equality.areEqual(new int[]{}, new int[]{}));
        Assert.assertTrue(Equality.areEqual(new Object[]{}, new Object[]{}));
        Assert.assertTrue(Equality.areEqual(new String[]{}, new String[]{}));
    }

    @Test
    public void testObjectArraysEqual() {
        Assert.assertTrue(Equality.areEqual(new String[]{"a", "b", "c"}, new String[]{"a", "b", "c"}));
        Assert.assertTrue(Equality.areEqual(new Object[]{1, "test", null}, new Object[]{1, "test", null}));
    }

    @Test
    public void testObjectArraysUnequal() {
        Assert.assertFalse(Equality.areEqual(new String[]{"a", "b"}, new String[]{"a", "c"}));
        Assert.assertFalse(Equality.areEqual(new Object[]{1, "test", null}, new Object[]{1, "test", "other"}));
    }

    @Test
    public void testObjectArraysDifferentLengths() {
        Assert.assertFalse(Equality.areEqual(new String[]{"a", "b"}, new String[]{"a"}));
        Assert.assertFalse(Equality.areEqual(new Object[]{}, new Object[]{null}));
    }

    @Test
    public void testMultiDimensionalArraysEqual() {
        int[][] arr1 = new int[][]{{1, 2}, {3, 4}};
        int[][] arr2 = new int[][]{{1, 2}, {3, 4}};
        Assert.assertTrue(Equality.areEqual(arr1, arr2));

        Object[][][] deepArr1 = new Object[][][]{{{1, "a"}, {2, "b"}}};
        Object[][][] deepArr2 = new Object[][][]{{{1, "a"}, {2, "b"}}};
        Assert.assertTrue(Equality.areEqual(deepArr1, deepArr2));
    }

    @Test
    public void testMultiDimensionalArraysUnequal() {
        int[][] arr1 = new int[][]{{1, 2}, {3, 4}};
        int[][] arr2 = new int[][]{{1, 2}, {3, 5}};
        Assert.assertFalse(Equality.areEqual(arr1, arr2));

        int[][] arr3 = new int[][]{{1, 2}, {3, 4, 5}};
        Assert.assertFalse(Equality.areEqual(arr1, arr3));
    }

    @Test
    public void testPackagePrivateMethodsDirectly() {
        Assert.assertTrue(Equality.isArray(new int[]{1}));
        Assert.assertTrue(Equality.isArray(new Object[0]));
        Assert.assertFalse(Equality.isArray("string"));
        Assert.assertFalse(Equality.isArray(new Object()));

        Assert.assertTrue(Equality.areArrayLengthsEqual(new int[]{1, 2}, new String[]{"a", "b"}));
        Assert.assertFalse(Equality.areArrayLengthsEqual(new int[]{1}, new String[]{"a", "b"}));

        Assert.assertTrue(Equality.areArrayElementsEqual(new int[]{1, 2}, new int[]{1, 2}));
        Assert.assertFalse(Equality.areArrayElementsEqual(new int[]{1, 2}, new int[]{1, 3}));

        Assert.assertTrue(Equality.areArraysEqual(new int[]{1, 2}, new int[]{1, 2}));
        Assert.assertFalse(Equality.areArraysEqual(new int[]{1, 2}, new int[]{1, 2, 3}));
        Assert.assertFalse(Equality.areArraysEqual(new int[]{1, 2}, new int[]{1, 3}));
    }

    @Test
    public void testInstantiable() {
        Equality equality = new Equality();
        Assert.assertNotNull(equality);
    }
}
