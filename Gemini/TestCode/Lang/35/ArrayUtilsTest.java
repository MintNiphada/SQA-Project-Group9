package org.apache.commons.lang3;

import org.junit.Assert;
import org.junit.Test;

import java.util.AbstractMap;
import java.util.Map;

public class ArrayUtilsTest {

    @Test
    public void testConstantsAndConstructor() {
        new ArrayUtils();
        Assert.assertEquals(0, ArrayUtils.EMPTY_OBJECT_ARRAY.length);
        Assert.assertEquals(0, ArrayUtils.EMPTY_CLASS_ARRAY.length);
        Assert.assertEquals(0, ArrayUtils.EMPTY_STRING_ARRAY.length);
        Assert.assertEquals(0, ArrayUtils.EMPTY_LONG_ARRAY.length);
        Assert.assertEquals(0, ArrayUtils.EMPTY_LONG_OBJECT_ARRAY.length);
        Assert.assertEquals(0, ArrayUtils.EMPTY_INT_ARRAY.length);
        Assert.assertEquals(0, ArrayUtils.EMPTY_INTEGER_OBJECT_ARRAY.length);
        Assert.assertEquals(0, ArrayUtils.EMPTY_SHORT_ARRAY.length);
        Assert.assertEquals(0, ArrayUtils.EMPTY_SHORT_OBJECT_ARRAY.length);
        Assert.assertEquals(0, ArrayUtils.EMPTY_BYTE_ARRAY.length);
        Assert.assertEquals(0, ArrayUtils.EMPTY_BYTE_OBJECT_ARRAY.length);
        Assert.assertEquals(0, ArrayUtils.EMPTY_DOUBLE_ARRAY.length);
        Assert.assertEquals(0, ArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY.length);
        Assert.assertEquals(0, ArrayUtils.EMPTY_FLOAT_ARRAY.length);
        Assert.assertEquals(0, ArrayUtils.EMPTY_FLOAT_OBJECT_ARRAY.length);
        Assert.assertEquals(0, ArrayUtils.EMPTY_BOOLEAN_ARRAY.length);
        Assert.assertEquals(0, ArrayUtils.EMPTY_BOOLEAN_OBJECT_ARRAY.length);
        Assert.assertEquals(0, ArrayUtils.EMPTY_CHAR_ARRAY.length);
        Assert.assertEquals(0, ArrayUtils.EMPTY_CHARACTER_OBJECT_ARRAY.length);
        Assert.assertEquals(-1, ArrayUtils.INDEX_NOT_FOUND);
    }

    @Test
    public void testToString() {
        Assert.assertEquals("{}", ArrayUtils.toString(null));
        Assert.assertEquals("nullVal", ArrayUtils.toString(null, "nullVal"));
        Assert.assertEquals("{1,2}", ArrayUtils.toString(new int[]{1, 2}));
        Assert.assertEquals("{1,2}", ArrayUtils.toString(new int[]{1, 2}, "nullVal"));
    }

    @Test
    public void testIsEquals() {
        Assert.assertTrue(ArrayUtils.isEquals(null, null));
        Assert.assertFalse(ArrayUtils.isEquals(new int[]{1}, null));
        Assert.assertTrue(ArrayUtils.isEquals(new int[]{1, 2}, new int[]{1, 2}));
        Assert.assertFalse(ArrayUtils.isEquals(new int[]{1, 2}, new int[]{1, 3}));
    }

    @Test
    public void testToMap() {
        Assert.assertNull(ArrayUtils.toMap(null));
        Map.Entry<String, String> entry = new AbstractMap.SimpleEntry<String, String>("A", "1");
        Object[] array = new Object[]{
            entry,
            new Object[]{"B", "2"}
        };
        Map<Object, Object> map = ArrayUtils.toMap(array);
        Assert.assertEquals(2, map.size());
        Assert.assertEquals("1", map.get("A"));
        Assert.assertEquals("2", map.get("B"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToMapShortArray() {
        ArrayUtils.toMap(new Object[]{new Object[]{"A"}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToMapInvalidType() {
        ArrayUtils.toMap(new Object[]{"invalid"});
    }

    @Test
    public void testToArray() {
        String[] arr = ArrayUtils.toArray("a", "b");
        Assert.assertArrayEquals(new String[]{"a", "b"}, arr);
    }

    @Test
    public void testCloneObject() {
        Assert.assertNull(ArrayUtils.clone((String[]) null));
        String[] arr = new String[]{"a", "b"};
        String[] clone = ArrayUtils.clone(arr);
        Assert.assertArrayEquals(arr, clone);
        Assert.assertNotSame(arr, clone);
    }

    @Test
    public void testClonePrimitives() {
        Assert.assertNull(ArrayUtils.clone((long[]) null));
        Assert.assertArrayEquals(new long[]{1L}, ArrayUtils.clone(new long[]{1L}));
        Assert.assertNull(ArrayUtils.clone((int[]) null));
        Assert.assertArrayEquals(new int[]{1}, ArrayUtils.clone(new int[]{1}));
        Assert.assertNull(ArrayUtils.clone((short[]) null));
        Assert.assertArrayEquals(new short[]{1}, ArrayUtils.clone(new short[]{1}));
        Assert.assertNull(ArrayUtils.clone((char[]) null));
        Assert.assertArrayEquals(new char[]{'a'}, ArrayUtils.clone(new char[]{'a'}));
        Assert.assertNull(ArrayUtils.clone((byte[]) null));
        Assert.assertArrayEquals(new byte[]{1}, ArrayUtils.clone(new byte[]{1}));
        Assert.assertNull(ArrayUtils.clone((double[]) null));
        Assert.assertArrayEquals(new double[]{1.0}, ArrayUtils.clone(new double[]{1.0}), 0.0);
        Assert.assertNull(ArrayUtils.clone((float[]) null));
        Assert.assertArrayEquals(new float[]{1.0f}, ArrayUtils.clone(new float[]{1.0f}), 0.0f);
        Assert.assertNull(ArrayUtils.clone((boolean[]) null));
        Assert.assertTrue(ArrayUtils.clone(new boolean[]{true})[0]);
    }

    @Test
    public void testSubarrayObject() {
        Assert.assertNull(ArrayUtils.subarray((String[]) null, 0, 1));
        String[] arr = new String[]{"a", "b", "c"};
        Assert.assertArrayEquals(new String[]{"a"}, ArrayUtils.subarray(arr, -1, 1));
        Assert.assertArrayEquals(new String[]{"b", "c"}, ArrayUtils.subarray(arr, 1, 5));
        Assert.assertArrayEquals(new String[0], ArrayUtils.subarray(arr, 2, 1));
        Assert.assertArrayEquals(new String[0], ArrayUtils.subarray(arr, 5, 6));
    }

    @Test
    public void testSubarrayLong() {
        Assert.assertNull(ArrayUtils.subarray((long[]) null, 0, 1));
        long[] arr = new long[]{1L, 2L, 3L};
        Assert.assertArrayEquals(new long[]{1L}, ArrayUtils.subarray(arr, -1, 1));
        Assert.assertArrayEquals(new long[]{2L, 3L}, ArrayUtils.subarray(arr, 1, 5));
        Assert.assertArrayEquals(new long[0], ArrayUtils.subarray(arr, 2, 1));
        Assert.assertArrayEquals(new long[0], ArrayUtils.subarray(arr, 5, 6));
    }

    @Test
    public void testSubarrayInt() {
        Assert.assertNull(ArrayUtils.subarray((int[]) null, 0, 1));
        int[] arr = new int[]{1, 2, 3};
        Assert.assertArrayEquals(new int[]{1}, ArrayUtils.subarray(arr, -1, 1));
        Assert.assertArrayEquals(new int[]{2, 3}, ArrayUtils.subarray(arr, 1, 5));
        Assert.assertArrayEquals(new int[0], ArrayUtils.subarray(arr, 2, 1));
    }

    @Test
    public void testSubarrayShort() {
        Assert.assertNull(ArrayUtils.subarray((short[]) null, 0, 1));
        short[] arr = new short[]{1, 2, 3};
        Assert.assertArrayEquals(new short[]{1}, ArrayUtils.subarray(arr, -1, 1));
        Assert.assertArrayEquals(new short[]{2, 3}, ArrayUtils.subarray(arr, 1, 5));
        Assert.assertArrayEquals(new short[0], ArrayUtils.subarray(arr, 2, 1));
    }

    @Test
    public void testSubarrayChar() {
        Assert.assertNull(ArrayUtils.subarray((char[]) null, 0, 1));
        char[] arr = new char[]{'a', 'b', 'c'};
        Assert.assertArrayEquals(new char[]{'a'}, ArrayUtils.subarray(arr, -1, 1));
        Assert.assertArrayEquals(new char[]{'b', 'c'}, ArrayUtils.subarray(arr, 1, 5));
        Assert.assertArrayEquals(new char[0], ArrayUtils.subarray(arr, 2, 1));
    }

    @Test
    public void testSubarrayByte() {
        Assert.assertNull(ArrayUtils.subarray((byte[]) null, 0, 1));
        byte[] arr = new byte[]{1, 2, 3};
        Assert.assertArrayEquals(new byte[]{1}, ArrayUtils.subarray(arr, -1, 1));
        Assert.assertArrayEquals(new byte[]{2, 3}, ArrayUtils.subarray(arr, 1, 5));
        Assert.assertArrayEquals(new byte[0], ArrayUtils.subarray(arr, 2, 1));
    }

    @Test
    public void testSubarrayDouble() {
        Assert.assertNull(ArrayUtils.subarray((double[]) null, 0, 1));
        double[] arr = new double[]{1.0, 2.0, 3.0};
        Assert.assertArrayEquals(new double[]{1.0}, ArrayUtils.subarray(arr, -1, 1), 0.0);
        Assert.assertArrayEquals(new double[]{2.0, 3.0}, ArrayUtils.subarray(arr, 1, 5), 0.0);
        Assert.assertArrayEquals(new double[0], ArrayUtils.subarray(arr, 2, 1), 0.0);
    }

    @Test
    public void testSubarrayFloat() {
        Assert.assertNull(ArrayUtils.subarray((float[]) null, 0, 1));
        float[] arr = new float[]{1.0f, 2.0f, 3.0f};
        Assert.assertArrayEquals(new float[]{1.0f}, ArrayUtils.subarray(arr, -1, 1), 0.0f);
        Assert.assertArrayEquals(new float[]{2.0f, 3.0f}, ArrayUtils.subarray(arr, 1, 5), 0.0f);
        Assert.assertArrayEquals(new float[0], ArrayUtils.subarray(arr, 2, 1), 0.0f);
    }

    @Test
    public void testSubarrayBoolean() {
        Assert.assertNull(ArrayUtils.subarray((boolean[]) null, 0, 1));
        boolean[] arr = new boolean[]{true, false, true};
        Assert.assertEquals(1, ArrayUtils.subarray(arr, -1, 1).length);
        Assert.assertEquals(2, ArrayUtils.subarray(arr, 1, 5).length);
        Assert.assertEquals(0, ArrayUtils.subarray(arr, 2, 1).length);
    }

    @Test
    public void testIsSameLength() {
        Assert.assertTrue(ArrayUtils.isSameLength((Object[]) null, (Object[]) null));
        Assert.assertFalse(ArrayUtils.isSameLength(new Object[1], (Object[]) null));
        Assert.assertFalse(ArrayUtils.isSameLength((Object[]) null, new Object[1]));
        Assert.assertTrue(ArrayUtils.isSameLength(new Object[1], new Object[1]));
        Assert.assertFalse(ArrayUtils.isSameLength(new Object[1], new Object[2]));

        Assert.assertTrue(ArrayUtils.isSameLength((long[]) null, (long[]) null));
        Assert.assertFalse(ArrayUtils.isSameLength(new long[1], (long[]) null));
        Assert.assertFalse(ArrayUtils.isSameLength((long[]) null, new long[1]));
        Assert.assertTrue(ArrayUtils.isSameLength(new long[1], new long[1]));
        Assert.assertFalse(ArrayUtils.isSameLength(new long[1], new long[2]));

        Assert.assertTrue(ArrayUtils.isSameLength((int[]) null, (int[]) null));
        Assert.assertFalse(ArrayUtils.isSameLength(new int[1], (int[]) null));
        Assert.assertFalse(ArrayUtils.isSameLength((int[]) null, new int[1]));
        Assert.assertTrue(ArrayUtils.isSameLength(new int[1], new int[1]));
        Assert.assertFalse(ArrayUtils.isSameLength(new int[1], new int[2]));

        Assert.assertTrue(ArrayUtils.isSameLength((short[]) null, (short[]) null));
        Assert.assertFalse(ArrayUtils.isSameLength(new short[1], (short[]) null));
        Assert.assertFalse(ArrayUtils.isSameLength((short[]) null, new short[1]));
        Assert.assertTrue(ArrayUtils.isSameLength(new short[1], new short[1]));
        Assert.assertFalse(ArrayUtils.isSameLength(new short[1], new short[2]));

        Assert.assertTrue(ArrayUtils.isSameLength((char[]) null, (char[]) null));
        Assert.assertFalse(ArrayUtils.isSameLength(new char[1], (char[]) null));
        Assert.assertFalse(ArrayUtils.isSameLength((char[]) null, new char[1]));
        Assert.assertTrue(ArrayUtils.isSameLength(new char[1], new char[1]));
        Assert.assertFalse(ArrayUtils.isSameLength(new char[1], new char[2]));

        Assert.assertTrue(ArrayUtils.isSameLength((byte[]) null, (byte[]) null));
        Assert.assertFalse(ArrayUtils.isSameLength(new byte[1], (byte[]) null));
        Assert.assertFalse(ArrayUtils.isSameLength((byte[]) null, new byte[1]));
        Assert.assertTrue(ArrayUtils.isSameLength(new byte[1], new byte[1]));
        Assert.assertFalse(ArrayUtils.isSameLength(new byte[1], new byte[2]));

        Assert.assertTrue(ArrayUtils.isSameLength((double[]) null, (double[]) null));
        Assert.assertFalse(ArrayUtils.isSameLength(new double[1], (double[]) null));
        Assert.assertFalse(ArrayUtils.isSameLength((double[]) null, new double[1]));
        Assert.assertTrue(ArrayUtils.isSameLength(new double[1], new double[1]));
        Assert.assertFalse(ArrayUtils.isSameLength(new double[1], new double[2]));

        Assert.assertTrue(ArrayUtils.isSameLength((float[]) null, (float[]) null));
        Assert.assertFalse(ArrayUtils.isSameLength(new float[1], (float[]) null));
        Assert.assertFalse(ArrayUtils.isSameLength((float[]) null, new float[1]));
        Assert.assertTrue(ArrayUtils.isSameLength(new float[1], new float[1]));
        Assert.assertFalse(ArrayUtils.isSameLength(new float[1], new float[2]));

        Assert.assertTrue(ArrayUtils.isSameLength((boolean[]) null, (boolean[]) null));
        Assert.assertFalse(ArrayUtils.isSameLength(new boolean[1], (boolean[]) null));
        Assert.assertFalse(ArrayUtils.isSameLength((boolean[]) null, new boolean[1]));
        Assert.assertTrue(ArrayUtils.isSameLength(new boolean[1], new boolean[1]));
        Assert.assertFalse(ArrayUtils.isSameLength(new boolean[1], new boolean[2]));
    }

    @Test
    public void testGetLengthAndIsSameType() {
        Assert.assertEquals(0, ArrayUtils.getLength(null));
        Assert.assertEquals(2, ArrayUtils.getLength(new int[]{1, 2}));
        Assert.assertTrue(ArrayUtils.isSameType(new int[]{1}, new int[]{2}));
        Assert.assertFalse(ArrayUtils.isSameType(new int[]{1}, new long[]{2}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetLengthNonArray() {
        ArrayUtils.getLength("string");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsSameTypeNullFirst() {
        ArrayUtils.isSameType(null, new int[]{1});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsSameTypeNullSecond() {
        ArrayUtils.isSameType(new int[]{1}, null);
    }

    @Test
    public void testReverse() {
        Object[] obj = new Object[]{"a", "b", "c"};
        ArrayUtils.reverse(obj);
        Assert.assertArrayEquals(new Object[]{"c", "b", "a"}, obj);
        ArrayUtils.reverse((Object[]) null);

        long[] l = new long[]{1L, 2L};
        ArrayUtils.reverse(l);
        Assert.assertArrayEquals(new long[]{2L, 1L}, l);
        ArrayUtils.reverse((long[]) null);

        int[] i = new int[]{1, 2};
        ArrayUtils.reverse(i);
        Assert.assertArrayEquals(new int[]{2, 1}, i);
        ArrayUtils.reverse((int[]) null);

        short[] s = new short[]{1, 2};
        ArrayUtils.reverse(s);
        Assert.assertArrayEquals(new short[]{2, 1}, s);
        ArrayUtils.reverse((short[]) null);

        char[] c = new char[]{'a', 'b'};
        ArrayUtils.reverse(c);
        Assert.assertArrayEquals(new char[]{'b', 'a'}, c);
        ArrayUtils.reverse((char[]) null);

        byte[] b = new byte[]{1, 2};
        ArrayUtils.reverse(b);
        Assert.assertArrayEquals(new byte[]{2, 1}, b);
        ArrayUtils.reverse((byte[]) null);

        double[] d = new double[]{1.0, 2.0};
        ArrayUtils.reverse(d);
        Assert.assertArrayEquals(new double[]{2.0, 1.0}, d, 0.0);
        ArrayUtils.reverse((double[]) null);

        float[] f = new float[]{1.0f, 2.0f};
        ArrayUtils.reverse(f);
        Assert.assertArrayEquals(new float[]{2.0f, 1.0f}, f, 0.0f);
        ArrayUtils.reverse((float[]) null);

        boolean[] bl = new boolean[]{true, false};
        ArrayUtils.reverse(bl);
        Assert.assertEquals(false, bl[0]);
        Assert.assertEquals(true, bl[1]);
        ArrayUtils.reverse((boolean[]) null);
    }

    @Test
    public void testIndexOfObject() {
        String[] arr = new String[]{"a", null, "b", "a"};
        Assert.assertEquals(0, ArrayUtils.indexOf(arr, "a"));
        Assert.assertEquals(3, ArrayUtils.indexOf(arr, "a", 1));
        Assert.assertEquals(0, ArrayUtils.indexOf(arr, "a", -1));
        Assert.assertEquals(1, ArrayUtils.indexOf(arr, null));
        Assert.assertEquals(1, ArrayUtils.indexOf(arr, null, 1));
        Assert.assertEquals(-1, ArrayUtils.indexOf(arr, "c"));
        Assert.assertEquals(-1, ArrayUtils.indexOf(arr, 123));
        Assert.assertEquals(-1, ArrayUtils.indexOf(null, "a"));
        Assert.assertEquals(-1, ArrayUtils.indexOf(arr, "a", 10));
        Assert.assertTrue(ArrayUtils.contains(arr, "a"));
        Assert.assertFalse(ArrayUtils.contains(arr, "c"));
    }

    @Test
    public void testLastIndexOfObject() {
        String[] arr = new String[]{"a", null, "b", "a"};
        Assert.assertEquals(3, ArrayUtils.lastIndexOf(arr, "a"));
        Assert.assertEquals(0, ArrayUtils.lastIndexOf(arr, "a", 2));
        Assert.assertEquals(1, ArrayUtils.lastIndexOf(arr, null));
        Assert.assertEquals(1, ArrayUtils.lastIndexOf(arr, null, 2));
        Assert.assertEquals(-1, ArrayUtils.lastIndexOf(arr, "a", -1));
        Assert.assertEquals(-1, ArrayUtils.lastIndexOf(arr, "c"));
        Assert.assertEquals(-1, ArrayUtils.lastIndexOf(arr, 123));
        Assert.assertEquals(-1, ArrayUtils.lastIndexOf(null, "a"));
    }

    @Test
    public void testIndexOfLong() {
        long[] arr = new long[]{1L, 2L, 1L};
        Assert.assertEquals(0, ArrayUtils.indexOf(arr, 1L));
        Assert.assertEquals(2, ArrayUtils.indexOf(arr, 1L, 1));
        Assert.assertEquals(0, ArrayUtils.indexOf(arr, 1L, -1));
        Assert.assertEquals(-1, ArrayUtils.indexOf(arr, 3L));
        Assert.assertEquals(-1, ArrayUtils.indexOf((long[]) null, 1L));
        Assert.assertEquals(2, ArrayUtils.lastIndexOf(arr, 1L));
        Assert.assertEquals(0, ArrayUtils.lastIndexOf(arr, 1L, 1));
        Assert.assertEquals(-1, ArrayUtils.lastIndexOf(arr, 1L, -1));
        Assert.assertEquals(2, ArrayUtils.lastIndexOf(arr, 1L, 5));
        Assert.assertEquals(-1, ArrayUtils.lastIndexOf(arr, 3L));
        Assert.assertEquals(-1, ArrayUtils.lastIndexOf((long[]) null, 1L));
        Assert.assertTrue(ArrayUtils.contains(arr, 1L));
        Assert.assertFalse(ArrayUtils.contains(arr, 3L));
    }

    @Test
    public void testIndexOfInt() {
        int[] arr = new int[]{1, 2, 1};
        Assert.assertEquals(0, ArrayUtils.indexOf(arr, 1));
        Assert.assertEquals(2, ArrayUtils.indexOf(arr, 1, 1));
        Assert.assertEquals(0, ArrayUtils.indexOf(arr, 1, -1));
        Assert.assertEquals(-1, ArrayUtils.indexOf(arr, 3));
        Assert.assertEquals(-1, ArrayUtils.indexOf((int[]) null, 1));
        Assert.assertEquals(2, ArrayUtils.lastIndexOf(arr, 1));
        Assert.assertEquals(0, ArrayUtils.lastIndexOf(arr, 1, 1));
        Assert.assertEquals(-1, ArrayUtils.lastIndexOf(arr, 1, -1));
        Assert.assertEquals(2, ArrayUtils.lastIndexOf(arr, 1, 5));
        Assert.assertEquals(-1, ArrayUtils.lastIndexOf(arr, 3));
        Assert.assertEquals(-1, ArrayUtils.lastIndexOf((int[]) null, 1));
        Assert.assertTrue(ArrayUtils.contains(arr, 1));
        Assert.assertFalse(ArrayUtils.contains(arr, 3));
    }

    @Test
    public void testIndexOfShort() {
        short[] arr = new short[]{1, 2, 1};
        Assert.assertEquals(0, ArrayUtils.indexOf(arr, (short) 1));
        Assert.assertEquals(2, ArrayUtils.indexOf(arr, (short) 1, 1));
        Assert.assertEquals(0, ArrayUtils.indexOf(arr, (short) 1, -1));
        Assert.assertEquals(-1, ArrayUtils.indexOf(arr, (short) 3));
        Assert.assertEquals(-1, ArrayUtils.indexOf((short[]) null, (short) 1));
        Assert.assertEquals(2, ArrayUtils.lastIndexOf(arr, (short) 1));
        Assert.assertEquals(0, ArrayUtils.lastIndexOf(arr, (short) 1, 1));
        Assert.assertEquals(-1, ArrayUtils.lastIndexOf(arr, (short) 1, -1));
        Assert.assertEquals(2, ArrayUtils.lastIndexOf(arr, (short) 1, 5));
        Assert.assertEquals(-1, ArrayUtils.lastIndexOf(arr, (short) 3));
        Assert.assertEquals(-1, ArrayUtils.lastIndexOf((short[]) null, (short) 1));
        Assert.assertTrue(ArrayUtils.contains(arr, (short) 1));
        Assert.assertFalse(ArrayUtils.contains(arr, (short) 3));
    }

    @Test
    public void testIndexOfChar() {
        char[] arr = new char[]{'a', 'b', 'a'};
        Assert.assertEquals(0, ArrayUtils.indexOf(arr, 'a'));
        Assert.assertEquals(2, ArrayUtils.indexOf(arr, 'a', 1));
        Assert.assertEquals(0, ArrayUtils.indexOf(arr, 'a', -1));
        Assert.assertEquals(-1, ArrayUtils.indexOf(arr, 'c'));
        Assert.assertEquals(-1, ArrayUtils.indexOf((char[]) null, 'a'));
        Assert.assertEquals(2, ArrayUtils.lastIndexOf(arr, 'a'));
        Assert.assertEquals(0, ArrayUtils.lastIndexOf(arr, 'a', 1));
        Assert.assertEquals(-1, ArrayUtils.lastIndexOf(arr, 'a', -1));
        Assert.assertEquals(2, ArrayUtils.lastIndexOf(arr, 'a', 5));
        Assert.assertEquals(-1, ArrayUtils.lastIndexOf(arr, 'c'));
        Assert.assertEquals(-1, ArrayUtils.lastIndexOf((char[]) null, 'a'));
        Assert.assertTrue(ArrayUtils.contains(arr, 'a'));
        Assert.assertFalse(ArrayUtils.contains(arr, 'c'));
    }

    @Test
    public void testIndexOfByte() {
        byte[] arr = new byte[]{1, 2, 1};
        Assert.assertEquals(0, ArrayUtils.indexOf(arr, (byte) 1));
        Assert.assertEquals(2, ArrayUtils.indexOf(arr, (byte) 1, 1));
        Assert.assertEquals(0, ArrayUtils.indexOf(arr, (byte) 1, -1));
        Assert.assertEquals(-1, ArrayUtils.indexOf(arr, (byte) 3));
        Assert.assertEquals(-1, ArrayUtils.indexOf((byte[]) null, (byte) 1));
        Assert.assertEquals(2, ArrayUtils.lastIndexOf(arr, (byte) 1));
        Assert.assertEquals(0, ArrayUtils.lastIndexOf(arr, (byte) 1, 1));
        Assert.assertEquals(-1, ArrayUtils.lastIndexOf(arr, (byte) 1, -1));
        Assert.assertEquals(2, ArrayUtils.lastIndexOf(arr, (byte) 1, 5));
        Assert.assertEquals(-1, ArrayUtils.lastIndexOf(arr, (byte) 3));
        Assert.assertEquals(-1, ArrayUtils.lastIndexOf((byte[]) null, (byte) 1));
        Assert.assertTrue(ArrayUtils.contains(arr, (byte) 1));
        Assert.assertFalse(ArrayUtils.contains(arr, (byte) 3));
    }

    @Test
    public void testIndexOfDouble() {
        double[] arr = new double[]{1.0, 2.0, 1.0};
        Assert.assertEquals(0, ArrayUtils.indexOf(arr, 1.0));
        Assert.assertEquals(2, ArrayUtils.indexOf(arr, 1.0, 1));
        Assert.assertEquals(0, ArrayUtils.indexOf(arr, 1.0, -1));
        Assert.assertEquals(-1, ArrayUtils.indexOf(arr, 3.0));
        Assert.assertEquals(-1, ArrayUtils.indexOf((double[]) null, 1.0));
        Assert.assertEquals(0, ArrayUtils.indexOf(arr, 1.05, 0.1));
        Assert.assertEquals(0, ArrayUtils.indexOf(arr, 1.05, -1, 0.1));
        Assert.assertEquals(-1, ArrayUtils.indexOf(arr, 1.05, 0.01));
        Assert.assertEquals(-1, ArrayUtils.indexOf((double[]) null, 1.05, 0.1));

        Assert.assertEquals(2, ArrayUtils.lastIndexOf(arr, 1.0));
        Assert.assertEquals(0, ArrayUtils.lastIndexOf(arr, 1.0, 1));
        Assert.assertEquals(-1, ArrayUtils.lastIndexOf(arr, 1.0, -1));
        Assert.assertEquals(2, ArrayUtils.lastIndexOf(arr, 1.0, 5));
        Assert.assertEquals(-1, ArrayUtils.lastIndexOf(arr, 3.0));
        Assert.assertEquals(-1, ArrayUtils.lastIndexOf((double[]) null, 1.0));

        Assert.assertEquals(2, ArrayUtils.lastIndexOf(arr, 1.05, 0.1));
        Assert.assertEquals(0, ArrayUtils.lastIndexOf(arr, 1.05, 1, 0.1));
        Assert.assertEquals(-1, ArrayUtils.lastIndexOf(arr, 1.05, -1, 0.1));
        Assert.assertEquals(2, ArrayUtils.lastIndexOf(arr, 1.05, 5, 0.1));
        Assert.assertEquals(-1, ArrayUtils.lastIndexOf(arr, 1.05, 0.01));
        Assert.assertEquals(-1, ArrayUtils.lastIndexOf((double[]) null, 1.05, 0.1));

        Assert.assertTrue(ArrayUtils.contains(arr, 1.0));
        Assert.assertFalse(ArrayUtils.contains(arr, 3.0));
        Assert.assertTrue(ArrayUtils.contains(arr, 1.05, 0.1));
        Assert.assertFalse(ArrayUtils.contains(arr, 1.05, 0.01));
    }

    @Test
    public void testIndexOfFloat() {
        float[] arr = new float[]{1.0f, 2.0f, 1.0f};
        Assert.assertEquals(0, ArrayUtils.indexOf(arr, 1.0f));
        Assert.assertEquals(2, ArrayUtils.indexOf(arr, 1.0f, 1));
        Assert.assertEquals(0, ArrayUtils.indexOf(arr, 1.0f, -1));
        Assert.assertEquals(-1, ArrayUtils.indexOf(arr, 3.0f));
        Assert.assertEquals(-1, ArrayUtils.indexOf((float[]) null, 1.0f));
        Assert.assertEquals(2, ArrayUtils.lastIndexOf(arr, 1.0f));
        Assert.assertEquals(0, ArrayUtils.lastIndexOf(arr, 1.0f, 1));
        Assert.assertEquals(-1, ArrayUtils.lastIndexOf(arr, 1.0f, -1));
        Assert.assertEquals(2, ArrayUtils.lastIndexOf(arr, 1.0f, 5));
        Assert.assertEquals(-1, ArrayUtils.lastIndexOf(arr, 3.0f));
        Assert.assertEquals(-1, ArrayUtils.lastIndexOf((float[]) null, 1.0f));
        Assert.assertTrue(ArrayUtils.contains(arr, 1.0f));
        Assert.assertFalse(ArrayUtils.contains(arr, 3.0f));
    }

    @Test
    public void testIndexOfBoolean() {
        boolean[] arr = new boolean[]{true, false, true};
        Assert.assertEquals(0, ArrayUtils.indexOf(arr, true));
        Assert.assertEquals(2, ArrayUtils.indexOf(arr, true, 1));
        Assert.assertEquals(0, ArrayUtils.indexOf(arr, true, -1));
        Assert.assertEquals(1, ArrayUtils.indexOf(arr, false));
        Assert.assertEquals(-1, ArrayUtils.indexOf((boolean[]) null, true));
        Assert.assertEquals(2, ArrayUtils.lastIndexOf(arr, true));
        Assert.assertEquals(0, ArrayUtils.lastIndexOf(arr, true, 1));
        Assert.assertEquals(-1, ArrayUtils.lastIndexOf(arr, true, -1));
        Assert.assertEquals(2, ArrayUtils.lastIndexOf(arr, true, 5));
        Assert.assertEquals(-1, ArrayUtils.lastIndexOf((boolean[]) null, true));
        Assert.assertTrue(ArrayUtils.contains(arr, true));
        Assert.assertFalse(ArrayUtils.contains(new boolean[]{false}, true));
    }

    @Test
    public void testConvertersChar() {
        Assert.assertNull(ArrayUtils.toPrimitive((Character[]) null));
        Assert.assertEquals(0, ArrayUtils.toPrimitive(new Character[0]).length);
        Assert.assertArrayEquals(new char[]{'a', 'b'}, ArrayUtils.toPrimitive(new Character[]{'a', 'b'}));
        Assert.assertNull(ArrayUtils.toPrimitive((Character[]) null, 'd'));
        Assert.assertEquals(0, ArrayUtils.toPrimitive(new Character[0], 'd').length);
        Assert.assertArrayEquals(new char[]{'a', 'd'}, ArrayUtils.toPrimitive(new Character[]{'a', null}, 'd'));
        Assert.assertNull(ArrayUtils.toObject((char[]) null));
        Assert.assertEquals(0, ArrayUtils.toObject(new char[0]).length);
        Assert.assertArrayEquals(new Character[]{'a'}, ArrayUtils.toObject(new char[]{'a'}));
    }

    @Test
    public void testConvertersLong() {
        Assert.assertNull(ArrayUtils.toPrimitive((Long[]) null));
        Assert.assertEquals(0, ArrayUtils.toPrimitive(new Long[0]).length);
        Assert.assertArrayEquals(new long[]{1L}, ArrayUtils.toPrimitive(new Long[]{1L}));
        Assert.assertNull(ArrayUtils.toPrimitive((Long[]) null, 0L));
        Assert.assertEquals(0, ArrayUtils.toPrimitive(new Long[0], 0L).length);
        Assert.assertArrayEquals(new long[]{1L, 0L}, ArrayUtils.toPrimitive(new Long[]{1L, null}, 0L));
        Assert.assertNull(ArrayUtils.toObject((long[]) null));
        Assert.assertEquals(0, ArrayUtils.toObject(new long[0]).length);
        Assert.assertArrayEquals(new Long[]{1L}, ArrayUtils.toObject(new long[]{1L}));
    }

    @Test
    public void testConvertersInt() {
        Assert.assertNull(ArrayUtils.toPrimitive((Integer[]) null));
        Assert.assertEquals(0, ArrayUtils.toPrimitive(new Integer[0]).length);
        Assert.assertArrayEquals(new int[]{1}, ArrayUtils.toPrimitive(new Integer[]{1}));
        Assert.assertNull(ArrayUtils.toPrimitive((Integer[]) null, 0));
        Assert.assertEquals(0, ArrayUtils.toPrimitive(new Integer[0], 0).length);
        Assert.assertArrayEquals(new int[]{1, 0}, ArrayUtils.toPrimitive(new Integer[]{1, null}, 0));
        Assert.assertNull(ArrayUtils.toObject((int[]) null));
        Assert.assertEquals(0, ArrayUtils.toObject(new int[0]).length);
        Assert.assertArrayEquals(new Integer[]{1}, ArrayUtils.toObject(new int[]{1}));
    }

    @Test
    public void testConvertersShort() {
        Assert.assertNull(ArrayUtils.toPrimitive((Short[]) null));
        Assert.assertEquals(0, ArrayUtils.toPrimitive(new Short[0]).length);
        Assert.assertArrayEquals(new short[]{1}, ArrayUtils.toPrimitive(new Short[]{1}));
        Assert.assertNull(ArrayUtils.toPrimitive((Short[]) null, (short) 0));
        Assert.assertEquals(0, ArrayUtils.toPrimitive(new Short[0], (short) 0).length);
        Assert.assertArrayEquals(new short[]{1, 0}, ArrayUtils.toPrimitive(new Short[]{1, null}, (short) 0));
        Assert.assertNull(ArrayUtils.toObject((short[]) null));
        Assert.assertEquals(0, ArrayUtils.toObject(new short[0]).length);
        Assert.assertArrayEquals(new Short[]{1}, ArrayUtils.toObject(new short[]{1}));
    }

    @Test
    public void testConvertersByte() {
        Assert.assertNull(ArrayUtils.toPrimitive((Byte[]) null));
        Assert.assertEquals(0, ArrayUtils.toPrimitive(new Byte[0]).length);
        Assert.assertArrayEquals(new byte[]{1}, ArrayUtils.toPrimitive(new Byte[]{1}));
        Assert.assertNull(ArrayUtils.toPrimitive((Byte[]) null, (byte) 0));
        Assert.assertEquals(0, ArrayUtils.toPrimitive(new Byte[0], (byte) 0).length);
        Assert.assertArrayEquals(new byte[]{1, 0}, ArrayUtils.toPrimitive(new Byte[]{1, null}, (byte) 0));
        Assert.assertNull(ArrayUtils.toObject((byte[]) null));
        Assert.assertEquals(0, ArrayUtils.toObject(new byte[0]).length);
        Assert.assertArrayEquals(new Byte[]{1}, ArrayUtils.toObject(new byte[]{1}));
    }

    @Test
    public void testConvertersDouble() {
        Assert.assertNull(ArrayUtils.toPrimitive((Double[]) null));
        Assert.assertEquals(0, ArrayUtils.toPrimitive(new Double[0]).length);
        Assert.assertArrayEquals(new double[]{1.0}, ArrayUtils.toPrimitive(new Double[]{1.0}), 0.0);
        Assert.assertNull(ArrayUtils.toPrimitive((Double[]) null, 0.0));
        Assert.assertEquals(0, ArrayUtils.toPrimitive(new Double[0], 0.0).length);
        Assert.assertArrayEquals(new double[]{1.0, 0.0}, ArrayUtils.toPrimitive(new Double[]{1.0, null}, 0.0), 0.0);
        Assert.assertNull(ArrayUtils.toObject((double[]) null));
        Assert.assertEquals(0, ArrayUtils.toObject(new double[0]).length);
        Assert.assertArrayEquals(new Double[]{1.0}, ArrayUtils.toObject(new double[]{1.0}));
    }

    @Test
    public void testConvertersFloat() {
        Assert.assertNull(ArrayUtils.toPrimitive((Float[]) null));
        Assert.assertEquals(0, ArrayUtils.toPrimitive(new Float[0]).length);
        Assert.assertArrayEquals(new float[]{1.0f}, ArrayUtils.toPrimitive(new Float[]{1.0f}), 0.0f);
        Assert.assertNull(ArrayUtils.toPrimitive((Float[]) null, 0.0f));
        Assert.assertEquals(0, ArrayUtils.toPrimitive(new Float[0], 0.0f).length);
        Assert.assertArrayEquals(new float[]{1.0f, 0.0f}, ArrayUtils.toPrimitive(new Float[]{1.0f, null}, 0.0f), 0.0f);
        Assert.assertNull(ArrayUtils.toObject((float[]) null));
        Assert.assertEquals(0, ArrayUtils.toObject(new float[0]).length);
        Assert.assertArrayEquals(new Float[]{1.0f}, ArrayUtils.toObject(new float[]{1.0f}));
    }

    @Test
    public void testConvertersBoolean() {
        Assert.assertNull(ArrayUtils.toPrimitive((Boolean[]) null));
        Assert.assertEquals(0, ArrayUtils.toPrimitive(new Boolean[0]).length);
        Assert.assertTrue(ArrayUtils.toPrimitive(new Boolean[]{true})[0]);
        Assert.assertNull(ArrayUtils.toPrimitive((Boolean[]) null, false));
        Assert.assertEquals(0, ArrayUtils.toPrimitive(new Boolean[0], false).length);
        Assert.assertFalse(ArrayUtils.toPrimitive(new Boolean[]{null}, false)[0]);
        Assert.assertNull(ArrayUtils.toObject((boolean[]) null));
        Assert.assertEquals(0, ArrayUtils.toObject(new boolean[0]).length);
        Assert.assertArrayEquals(new Boolean[]{Boolean.TRUE, Boolean.FALSE}, ArrayUtils.toObject(new boolean[]{true, false}));
    }

    @Test
    public void testIsEmpty() {
        Assert.assertTrue(ArrayUtils.isEmpty((String[]) null));
        Assert.assertTrue(ArrayUtils.isEmpty(new String[0]));
        Assert.assertFalse(ArrayUtils.isEmpty(new String[]{"a"}));

        Assert.assertTrue(ArrayUtils.isEmpty((long[]) null));
        Assert.assertTrue(ArrayUtils.isEmpty(new long[0]));
        Assert.assertFalse(ArrayUtils.isEmpty(new long[]{1L}));

        Assert.assertTrue(ArrayUtils.isEmpty((int[]) null));
        Assert.assertTrue(ArrayUtils.isEmpty(new int[0]));
        Assert.assertFalse(ArrayUtils.isEmpty(new int[]{1}));

        Assert.assertTrue(ArrayUtils.isEmpty((short[]) null));
        Assert.assertTrue(ArrayUtils.isEmpty(new short[0]));
        Assert.assertFalse(ArrayUtils.isEmpty(new short[]{1}));

        Assert.assertTrue(ArrayUtils.isEmpty((char[]) null));
        Assert.assertTrue(ArrayUtils.isEmpty(new char[0]));
        Assert.assertFalse(ArrayUtils.isEmpty(new char[]{'a'}));

        Assert.assertTrue(ArrayUtils.isEmpty((byte[]) null));
        Assert.assertTrue(ArrayUtils.isEmpty(new byte[0]));
        Assert.assertFalse(ArrayUtils.isEmpty(new byte[]{1}));

        Assert.assertTrue(ArrayUtils.isEmpty((double[]) null));
        Assert.assertTrue(ArrayUtils.isEmpty(new double[0]));
        Assert.assertFalse(ArrayUtils.isEmpty(new double[]{1.0}));

        Assert.assertTrue(ArrayUtils.isEmpty((float[]) null));
        Assert.assertTrue(ArrayUtils.isEmpty(new float[0]));
        Assert.assertFalse(ArrayUtils.isEmpty(new float[]{1.0f}));

        Assert.assertTrue(ArrayUtils.isEmpty((boolean[]) null));
        Assert.assertTrue(ArrayUtils.isEmpty(new boolean[0]));
        Assert.assertFalse(ArrayUtils.isEmpty(new boolean[]{true}));
    }

    @Test
    public void testAddAll() {
        Assert.assertNull(ArrayUtils.addAll((String[]) null, (String[]) null));
        Assert.assertArrayEquals(new String[]{"a"}, ArrayUtils.addAll(null, "a"));
        Assert.assertArrayEquals(new String[]{"a"}, ArrayUtils.addAll(new String[]{"a"}, (String[]) null));
        Assert.assertArrayEquals(new String[]{"a", "b"}, ArrayUtils.addAll(new String[]{"a"}, "b"));

        Assert.assertArrayEquals(new boolean[]{true, false}, ArrayUtils.addAll(new boolean[]{true}, false));
        Assert.assertArrayEquals(new boolean[]{true}, ArrayUtils.addAll(null, new boolean[]{true}));
        Assert.assertArrayEquals(new boolean[]{true}, ArrayUtils.addAll(new boolean[]{true}, (boolean[]) null));

        Assert.assertArrayEquals(new char[]{'a', 'b'}, ArrayUtils.addAll(new char[]{'a'}, 'b'));
        Assert.assertArrayEquals(new char[]{'a'}, ArrayUtils.addAll(null, new char[]{'a'}));
        Assert.assertArrayEquals(new char[]{'a'}, ArrayUtils.addAll(new char[]{'a'}, (char[]) null));

        Assert.assertArrayEquals(new byte[]{1, 2}, ArrayUtils.addAll(new byte[]{1}, (byte) 2));
        Assert.assertArrayEquals(new byte[]{1}, ArrayUtils.addAll(null, new byte[]{1}));
        Assert.assertArrayEquals(new byte[]{1}, ArrayUtils.addAll(new byte[]{1}, (byte[]) null));

        Assert.assertArrayEquals(new short[]{1, 2}, ArrayUtils.addAll(new short[]{1}, (short) 2));
        Assert.assertArrayEquals(new short[]{1}, ArrayUtils.addAll(null, new short[]{1}));
        Assert.assertArrayEquals(new short[]{1}, ArrayUtils.addAll(new short[]{1}, (short[]) null));

        Assert.assertArrayEquals(new int[]{1, 2}, ArrayUtils.addAll(new int[]{1}, 2));
        Assert.assertArrayEquals(new int[]{1}, ArrayUtils.addAll(null, new int[]{1}));
        Assert.assertArrayEquals(new int[]{1}, ArrayUtils.addAll(new int[]{1}, (int[]) null));

        Assert.assertArrayEquals(new long[]{1L, 2L}, ArrayUtils.addAll(new long[]{1L}, 2L));
        Assert.assertArrayEquals(new long[]{1L}, ArrayUtils.addAll(null, new long[]{1L}));
        Assert.assertArrayEquals(new long[]{1L}, ArrayUtils.addAll(new long[]{1L}, (long[]) null));

        Assert.assertArrayEquals(new float[]{1.0f, 2.0f}, ArrayUtils.addAll(new float[]{1.0f}, 2.0f), 0.0f);
        Assert.assertArrayEquals(new float[]{1.0f}, ArrayUtils.addAll(null, new float[]{1.0f}), 0.0f);
        Assert.assertArrayEquals(new float[]{1.0f}, ArrayUtils.addAll(new float[]{1.0f}, (float[]) null), 0.0f);

        Assert.assertArrayEquals(new double[]{1.0, 2.0}, ArrayUtils.addAll(new double[]{1.0}, 2.0), 0.0);
        Assert.assertArrayEquals(new double[]{1.0}, ArrayUtils.addAll(null, new double[]{1.0}), 0.0);
        Assert.assertArrayEquals(new double[]{1.0}, ArrayUtils.addAll(new double[]{1.0}, (double[]) null), 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddAllIncompatibleTypes() {
        String[] sArr = new String[]{"a"};
        Object[] oArr = new Object[]{new Integer(1)};
        ArrayUtils.addAll(sArr, (String[]) oArr);
    }

    @Test
    public void testAdd() {
        Assert.assertArrayEquals(new String[]{null}, ArrayUtils.add((String[]) null, (String) null));
        Assert.assertArrayEquals(new String[]{"a"}, ArrayUtils.add((String[]) null, "a"));
        Assert.assertArrayEquals(new String[]{"a", "b"}, ArrayUtils.add(new String[]{"a"}, "b"));

        Assert.assertArrayEquals(new boolean[]{true}, ArrayUtils.add((boolean[]) null, true));
        Assert.assertArrayEquals(new boolean[]{true, false}, ArrayUtils.add(new boolean[]{true}, false));

        Assert.assertArrayEquals(new byte[]{1}, ArrayUtils.add((byte[]) null, (byte) 1));
        Assert.assertArrayEquals(new byte[]{1, 2}, ArrayUtils.add(new byte[]{1}, (byte) 2));

        Assert.assertArrayEquals(new char[]{'a'}, ArrayUtils.add((char[]) null, 'a'));
        Assert.assertArrayEquals(new char[]{'a', 'b'}, ArrayUtils.add(new char[]{'a'}, 'b'));

        Assert.assertArrayEquals(new double[]{1.0}, ArrayUtils.add((double[]) null, 1.0), 0.0);
        Assert.assertArrayEquals(new double[]{1.0, 2.0}, ArrayUtils.add(new double[]{1.0}, 2.0), 0.0);

        Assert.assertArrayEquals(new float[]{1.0f}, ArrayUtils.add((float[]) null, 1.0f), 0.0f);
        Assert.assertArrayEquals(new float[]{1.0f, 2.0f}, ArrayUtils.add(new float[]{1.0f}, 2.0f), 0.0f);

        Assert.assertArrayEquals(new int[]{1}, ArrayUtils.add((int[]) null, 1));
        Assert.assertArrayEquals(new int[]{1, 2}, ArrayUtils.add(new int[]{1}, 2));

        Assert.assertArrayEquals(new long[]{1L}, ArrayUtils.add((long[]) null, 1L));
        Assert.assertArrayEquals(new long[]{1L, 2L}, ArrayUtils.add(new long[]{1L}, 2L));

        Assert.assertArrayEquals(new short[]{1}, ArrayUtils.add((short[]) null, (short) 1));
        Assert.assertArrayEquals(new short[]{1, 2}, ArrayUtils.add(new short[]{1}, (short) 2));
    }

    @Test
    public void testAddAtIndex() {
        Assert.assertArrayEquals(new Object[]{null}, ArrayUtils.add((Object[]) null, 0, (Object) null));
        Assert.assertArrayEquals(new String[]{"a"}, ArrayUtils.add((String[]) null, 0, "a"));
        Assert.assertArrayEquals(new String[]{"a", "c", "b"}, ArrayUtils.add(new String[]{"a", "b"}, 1, "c"));

        Assert.assertArrayEquals(new boolean[]{true}, ArrayUtils.add((boolean[]) null, 0, true));
        Assert.assertArrayEquals(new boolean[]{true, false, true}, ArrayUtils.add(new boolean[]{true, true}, 1, false));

        Assert.assertArrayEquals(new char[]{'a'}, ArrayUtils.add((char[]) null, 0, 'a'));
        Assert.assertArrayEquals(new char[]{'a', 'c', 'b'}, ArrayUtils.add(new char[]{'a', 'b'}, 1, 'c'));

        Assert.assertArrayEquals(new byte[]{1}, ArrayUtils.add((byte[]) null, 0, (byte) 1));
        Assert.assertArrayEquals(new byte[]{1, 3, 2}, ArrayUtils.add(new byte[]{1, 2}, 1, (byte) 3));

        Assert.assertArrayEquals(new short[]{1}, ArrayUtils.add((short[]) null, 0, (short) 1));
        Assert.assertArrayEquals(new short[]{1, 3, 2}, ArrayUtils.add(new short[]{1, 2}, 1, (short) 3));

        Assert.assertArrayEquals(new int[]{1}, ArrayUtils.add((int[]) null, 0, 1));
        Assert.assertArrayEquals(new int[]{1, 3, 2}, ArrayUtils.add(new int[]{1, 2}, 1, 3));

        Assert.assertArrayEquals(new long[]{1L}, ArrayUtils.add((long[]) null, 0, 1L));
        Assert.assertArrayEquals(new long[]{1L, 3L, 2L}, ArrayUtils.add(new long[]{1L, 2L}, 1, 3L));

        Assert.assertArrayEquals(new float[]{1.0f}, ArrayUtils.add((float[]) null, 0, 1.0f), 0.0f);
        Assert.assertArrayEquals(new float[]{1.0f, 3.0f, 2.0f}, ArrayUtils.add(new float[]{1.0f, 2.0f}, 1, 3.0f), 0.0f);

        Assert.assertArrayEquals(new double[]{1.0}, ArrayUtils.add((double[]) null, 0, 1.0), 0.0);
        Assert.assertArrayEquals(new double[]{1.0, 3.0, 2.0}, ArrayUtils.add(new double[]{1.0, 2.0}, 1, 3.0), 0.0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddAtIndexNullInvalidIndex() {
        ArrayUtils.add((String[]) null, 1, "a");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddAtIndexOutOfBoundsLow() {
        ArrayUtils.add(new String[]{"a"}, -1, "b");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddAtIndexOutOfBoundsHigh() {
        ArrayUtils.add(new String[]{"a"}, 2, "b");
    }

    @Test
    public void testRemove() {
        Assert.assertArrayEquals(new String[]{"a", "c"}, ArrayUtils.remove(new String[]{"a", "b", "c"}, 1));
        Assert.assertArrayEquals(new boolean[]{true, true}, ArrayUtils.remove(new boolean[]{true, false, true}, 1));
        Assert.assertArrayEquals(new byte[]{1, 3}, ArrayUtils.remove(new byte[]{1, 2, 3}, 1));
        Assert.assertArrayEquals(new char[]{'a', 'c'}, ArrayUtils.remove(new char[]{'a', 'b', 'c'}, 1));
        Assert.assertArrayEquals(new double[]{1.0, 3.0}, ArrayUtils.remove(new double[]{1.0, 2.0, 3.0}, 1), 0.0);
        Assert.assertArrayEquals(new float[]{1.0f, 3.0f}, ArrayUtils.remove(new float[]{1.0f, 2.0f, 3.0f}, 1), 0.0f);
        Assert.assertArrayEquals(new int[]{1, 3}, ArrayUtils.remove(new int[]{1, 2, 3}, 1));
        Assert.assertArrayEquals(new long[]{1L, 3L}, ArrayUtils.remove(new long[]{1L, 2L, 3L}, 1));
        Assert.assertArrayEquals(new short[]{1, 3}, ArrayUtils.remove(new short[]{1, 2, 3}, 1));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveNull() {
        ArrayUtils.remove((String[]) null, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveOutOfBounds() {
        ArrayUtils.remove(new String[]{"a"}, 1);
    }

    @Test
    public void testRemoveElement() {
        Assert.assertNull(ArrayUtils.removeElement((String[]) null, "a"));
        Assert.assertArrayEquals(new String[]{"a"}, ArrayUtils.removeElement(new String[]{"a"}, "b"));
        Assert.assertArrayEquals(new String[]{"b"}, ArrayUtils.removeElement(new String[]{"a", "b"}, "a"));

        Assert.assertNull(ArrayUtils.removeElement((boolean[]) null, true));
        Assert.assertArrayEquals(new boolean[]{true}, ArrayUtils.removeElement(new boolean[]{true}, false));
        Assert.assertArrayEquals(new boolean[]{false}, ArrayUtils.removeElement(new boolean[]{true, false}, true));

        Assert.assertNull(ArrayUtils.removeElement((byte[]) null, (byte) 1));
        Assert.assertArrayEquals(new byte[]{1}, ArrayUtils.removeElement(new byte[]{1}, (byte) 2));
        Assert.assertArrayEquals(new byte[]{2}, ArrayUtils.removeElement(new byte[]{1, 2}, (byte) 1));

        Assert.assertNull(ArrayUtils.removeElement((char[]) null, 'a'));
        Assert.assertArrayEquals(new char[]{'a'}, ArrayUtils.removeElement(new char[]{'a'}, 'b'));
        Assert.assertArrayEquals(new char[]{'b'}, ArrayUtils.removeElement(new char[]{'a', 'b'}, 'a'));

        Assert.assertNull(ArrayUtils.removeElement((double[]) null, 1.0));
        Assert.assertArrayEquals(new double[]{1.0}, ArrayUtils.removeElement(new double[]{1.0}, 2.0), 0.0);
        Assert.assertArrayEquals(new double[]{2.0}, ArrayUtils.removeElement(new double[]{1.0, 2.0}, 1.0), 0.0);

        Assert.assertNull(ArrayUtils.removeElement((float[]) null, 1.0f));
        Assert.assertArrayEquals(new float[]{1.0f}, ArrayUtils.removeElement(new float[]{1.0f}, 2.0f), 0.0f);
        Assert.assertArrayEquals(new float[]{2.0f}, ArrayUtils.removeElement(new float[]{1.0f, 2.0f}, 1.0f), 0.0f);

        Assert.assertNull(ArrayUtils.removeElement((int[]) null, 1));
        Assert.assertArrayEquals(new int[]{1}, ArrayUtils.removeElement(new int[]{1}, 2));
        Assert.assertArrayEquals(new int[]{2}, ArrayUtils.removeElement(new int[]{1, 2}, 1));

        Assert.assertNull(ArrayUtils.removeElement((long[]) null, 1L));
        Assert.assertArrayEquals(new long[]{1L}, ArrayUtils.removeElement(new long[]{1L}, 2L));
        Assert.assertArrayEquals(new long[]{2L}, ArrayUtils.removeElement(new long[]{1L, 2L}, 1L));

        Assert.assertNull(ArrayUtils.removeElement((short[]) null, (short) 1));
        Assert.assertArrayEquals(new short[]{1}, ArrayUtils.removeElement(new short[]{1}, (short) 2));
        Assert.assertArrayEquals(new short[]{2}, ArrayUtils.removeElement(new short[]{1, 2}, (short) 1));
    }
}
