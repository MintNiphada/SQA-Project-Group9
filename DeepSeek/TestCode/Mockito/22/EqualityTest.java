package org.mockito.internal.matchers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class EqualityTest {

    @Test
    public void areEqualBothNull() {
        assertTrue(Equality.areEqual(null, null));
    }

    @Test
    public void areEqualFirstNullSecondNotNull() {
        assertFalse(Equality.areEqual(null, "a"));
    }

    @Test
    public void areEqualFirstNotNullSecondNull() {
        assertFalse(Equality.areEqual("a", null));
    }

    @Test
    public void areEqualBothNonNullNonArrayEqual() {
        assertTrue(Equality.areEqual("abc", "abc"));
    }

    @Test
    public void areEqualBothNonNullNonArrayNotEqual() {
        assertFalse(Equality.areEqual("abc", "def"));
    }

    @Test
    public void areEqualBothArraysEqual() {
        int[] a = {1, 2};
        int[] b = {1, 2};
        assertTrue(Equality.areEqual(a, b));
    }

    @Test
    public void areEqualBothArraysDifferentLength() {
        int[] a = {1, 2};
        int[] b = {1, 2, 3};
        assertFalse(Equality.areEqual(a, b));
    }

    @Test
    public void areEqualBothArraysSameLengthDifferentElements() {
        int[] a = {1, 2};
        int[] b = {1, 3};
        assertFalse(Equality.areEqual(a, b));
    }

    @Test
    public void areEqualOneArrayOneNonArray() {
        assertFalse(Equality.areEqual(new int[]{1}, "a"));
    }

    @Test
    public void areEqualFirstNonArraySecondArray() {
        assertFalse(Equality.areEqual("a", new int[]{1}));
    }

    @Test
    public void areEqualNestedArraysEqual() {
        int[][] a = {{1}, {2}};
        int[][] b = {{1}, {2}};
        assertTrue(Equality.areEqual(a, b));
    }

    @Test
    public void areEqualNestedArraysDifferent() {
        int[][] a = {{1}, {2}};
        int[][] b = {{1}, {3}};
        assertFalse(Equality.areEqual(a, b));
    }

    @Test
    public void areEqualArraysWithNullElements() {
        Object[] a = {null, "a"};
        Object[] b = {null, "a"};
        assertTrue(Equality.areEqual(a, b));
    }

    @Test
    public void areEqualArraysWithNullElementsDifferent() {
        Object[] a = {null, "a"};
        Object[] b = {"b", "a"};
        assertFalse(Equality.areEqual(a, b));
    }

    @Test
    public void areEqualEmptyArrays() {
        int[] a = {};
        int[] b = {};
        assertTrue(Equality.areEqual(a, b));
    }

    @Test
    public void areEqualPrimitiveArraysDifferentTypes() {
        int[] a = {1};
        double[] b = {1.0};
        assertFalse(Equality.areEqual(a, b));
    }

    @Test
    public void areEqualBooleanArrays() {
        boolean[] a = {true, false};
        boolean[] b = {true, false};
        assertTrue(Equality.areEqual(a, b));
    }

    @Test
    public void areEqualCharArray() {
        char[] a = {'a', 'b'};
        char[] b = {'a', 'b'};
        assertTrue(Equality.areEqual(a, b));
    }

    @Test
    public void areEqualIntArrayVsIntegerArray() {
        int[] a = {1};
        Integer[] b = {1};
        assertTrue(Equality.areEqual(a, b));
    }

    @Test
    public void isArrayWithArray() {
        assertTrue(Equality.isArray(new int[0]));
    }

    @Test
    public void isArrayWithNonArray() {
        assertFalse(Equality.isArray("string"));
    }

    @Test(expected = NullPointerException.class)
    public void isArrayWithNull() {
        Equality.isArray(null);
    }

    @Test
    public void areArrayLengthsEqualSameLength() {
        int[] a = {1, 2};
        int[] b = {3, 4};
        assertTrue(Equality.areArrayLengthsEqual(a, b));
    }

    @Test
    public void areArrayLengthsEqualDifferentLength() {
        int[] a = {1};
        int[] b = {1, 2};
        assertFalse(Equality.areArrayLengthsEqual(a, b));
    }

    @Test
    public void areArrayLengthsEqualEmptyArrays() {
        int[] a = {};
        int[] b = {};
        assertTrue(Equality.areArrayLengthsEqual(a, b));
    }

    @Test
    public void areArrayElementsEqualSameElements() {
        int[] a = {1, 2};
        int[] b = {1, 2};
        assertTrue(Equality.areArrayElementsEqual(a, b));
    }

    @Test
    public void areArrayElementsEqualDifferentElements() {
        int[] a = {1, 2};
        int[] b = {1, 3};
        assertFalse(Equality.areArrayElementsEqual(a, b));
    }

    @Test
    public void areArrayElementsEqualEmptyArrays() {
        int[] a = {};
        int[] b = {};
        assertTrue(Equality.areArrayElementsEqual(a, b));
    }

    @Test
    public void areArrayElementsEqualWithNulls() {
        Object[] a = {null, "a"};
        Object[] b = {null, "a"};
        assertTrue(Equality.areArrayElementsEqual(a, b));
    }

    @Test
    public void areArrayElementsEqualWithNullsDifferent() {
        Object[] a = {null, "a"};
        Object[] b = {"b", "a"};
        assertFalse(Equality.areArrayElementsEqual(a, b));
    }

    @Test
    public void areArraysEqualSameLengthAndElements() {
        int[] a = {1, 2};
        int[] b = {1, 2};
        assertTrue(Equality.areArraysEqual(a, b));
    }

    @Test
    public void areArraysEqualDifferentLength() {
        int[] a = {1};
        int[] b = {1, 2};
        assertFalse(Equality.areArraysEqual(a, b));
    }

    @Test
    public void areArraysEqualSameLengthDifferentElements() {
        int[] a = {1, 2};
        int[] b = {1, 3};
        assertFalse(Equality.areArraysEqual(a, b));
    }
}
