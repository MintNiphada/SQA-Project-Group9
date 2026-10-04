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
        Assert.assertEquals("null", ArrayUtils.toString(null, "null"));
        Assert.assertEquals("{1,2}", ArrayUtils.toString(new int[]{1, 2}));
    }

    @Test
    public void testIsEquals() {
        Assert.assertTrue(ArrayUtils.isEquals(null, null));
        Assert.assertFalse(ArrayUtils.isEquals(new int[]{1}, new int[]{2}));
        Assert.assertTrue(ArrayUtils.isEquals(new int[]{1, 2}, new int[]{1, 2}));
    }

    @Test
    public void testToMap() {
        Assert.assertNull(ArrayUtils.toMap(null));
        Map<Object, Object> map = ArrayUtils.toMap(new Object[][]{{"key1", "val1"}, {"key2", "val2"}});
        Assert.assertEquals("val1", map.get("key1"));
        Assert.assertEquals("val2", map.get("key2"));

        Map.Entry<String, String> entry = new AbstractMap.SimpleEntry<String, String>("eK", "eV");
        Map<Object, Object> entryMap = ArrayUtils.toMap(new Object[]{entry});
        Assert.assertEquals("eV", entryMap.get("eK"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToMapInvalidElementLength() {
        ArrayUtils.toMap(new Object[][]{{"onlyOne"}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToMapInvalidElementType() {
        ArrayUtils.toMap(new Object[]{"invalidStringElement"});
    }

    @Test
    public void testClone() {
        Assert.assertNull(ArrayUtils.clone((Object[]) null));
        Assert.assertArrayEquals(new String[]{"a"}, ArrayUtils.clone(new String[]{"a"}));

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
        Assert.assertNull(ArrayUtils.subarray((Object[]) null, 0, 1));
        String[] array = new String[]{"a", "b", "c"};
        Assert.assertArrayEquals(new String[]{"a", "b"}, ArrayUtils.subarray(array, -1, 2));
        Assert.assertArrayEquals(new String[]{"b", "c"}, ArrayUtils.subarray(array, 1, 5));
        Assert.assertArrayEquals(new String[0], ArrayUtils.subarray(array, 2, 1));
        Assert.assertArrayEquals(new String[0], ArrayUtils.subarray(array, 5, 6));
    }

    @Test
    public void testSubarrayPrimitives() {
        Assert.assertNull(ArrayUtils.subarray((long[]) null, 0, 1));
        Assert.assertArrayEquals(new long[]{1L, 2L}, ArrayUtils.subarray(new long[]{1L, 2L, 3L}, -1, 2));
        Assert.assertArrayEquals(new long[]{2L, 3L}, ArrayUtils.subarray(new long[]{1L, 2L, 3L}, 1, 5));
        Assert.assertArrayEquals(new long[0], ArrayUtils.subarray(new long[]{1L, 2L}, 2, 1));

        Assert.assertNull(ArrayUtils.subarray((int[]) null, 0, 1));
        Assert.assertArrayEquals(new int[]{1, 2}, ArrayUtils.subarray(new int[]{1, 2, 3}, -1, 2));
        Assert.assertArrayEquals(new int[]{2, 3}, ArrayUtils.subarray(new int[]{1, 2, 3}, 1, 5));
        Assert.assertArrayEquals(new int[0], ArrayUtils.subarray(new int[]{1, 2}, 2, 1));

        Assert.assertNull(ArrayUtils.subarray((short[]) null, 0, 1));
        Assert.assertArrayEquals(new short[]{1, 2}, ArrayUtils.subarray(new short[]{1, 2, 3}, -1, 2));
        Assert.assertArrayEquals(new short[]{2, 3}, ArrayUtils.subarray(new short[]{1, 2, 3}, 1, 5));
        Assert.assertArrayEquals(new short[0], ArrayUtils.subarray(new short[]{1, 2}, 2, 1));

        Assert.assertNull(ArrayUtils.subarray((char[]) null, 0, 1));
        Assert.assertArrayEquals(new char[]{'a', 'b'}, ArrayUtils.subarray(new char[]{'a', 'b', 'c'}, -1, 2));
        Assert.assertArrayEquals(new char[]{'b', 'c'}, ArrayUtils.subarray(new char[]{'a', 'b', 'c'}, 1, 5));
        Assert.assertArrayEquals(new char[0], ArrayUtils.subarray(new char[]{'a', 'b'}, 2, 1));

        Assert.assertNull(ArrayUtils.subarray((byte[]) null, 0, 1));
        Assert.assertArrayEquals(new byte[]{1, 2}, ArrayUtils.subarray(new byte[]{1, 2, 3}, -1, 2));
        Assert.assertArrayEquals(new byte[]{2, 3}, ArrayUtils.subarray(new byte[]{1, 2, 3}, 1, 5));
        Assert.assertArrayEquals(new byte[0], ArrayUtils.subarray(new byte[]{1, 2}, 2, 1));

        Assert.assertNull(ArrayUtils.subarray((double[]) null, 0, 1));
        Assert.assertArrayEquals(new double[]{1.0, 2.0}, ArrayUtils.subarray(new double[]{1.0, 2.0, 3.0}, -1, 2), 0.0);
        Assert.assertArrayEquals(new double[]{2.0, 3.0}, ArrayUtils.subarray(new double[]{1.0, 2.0, 3.0}, 1, 5), 0.0);
        Assert.assertArrayEquals(new double[0], ArrayUtils.subarray(new double[]{1.0, 2.0}, 2, 1), 0.0);

        Assert.assertNull(ArrayUtils.subarray((float[]) null, 0, 1));
        Assert.assertArrayEquals(new float[]{1.0f, 2.0f}, ArrayUtils.subarray(new float[]{1.0f, 2.0f, 3.0f}, -1, 2), 0.0f);
        Assert.assertArrayEquals(new float[]{2.0f, 3.0f}, ArrayUtils.subarray(new float[]{1.0f, 2.0f, 3.0f}, 1, 5), 0.0f);
        Assert.assertArrayEquals(new float[0], ArrayUtils.subarray(new float[]{1.0f, 2.0f}, 2, 1), 0.0f);

        Assert.assertNull(ArrayUtils.subarray((boolean[]) null, 0, 1));
        Assert.assertArrayEquals(new boolean[]{true, false}, ArrayUtils.subarray(new boolean[]{true, false, true}, -1, 2));
        Assert.assertArrayEquals(new boolean[]{false, true}, ArrayUtils.subarray(new boolean[]{true, false, true}, 1, 5));
        Assert.assertArrayEquals(new boolean[0], ArrayUtils.subarray(new boolean[]{true, false}, 2, 1));
    }

    @Test
    public void testIsSameLength() {
        Assert.assertTrue(ArrayUtils.isSameLength((Object[]) null, (Object[]) null));
        Assert.assertFalse(ArrayUtils.isSameLength(new Object[]{"a"}, (Object[]) null));
        Assert.assertFalse(ArrayUtils.isSameLength((Object[]) null, new Object[]{"a"}));
        Assert.assertFalse(ArrayUtils.isSameLength(new Object[]{"a"}, new Object[]{"a", "b"}));
        Assert.assertTrue(ArrayUtils.isSameLength(new Object[]{"a"}, new Object[]{"b"}));

        Assert.assertTrue(ArrayUtils.isSameLength((long[]) null, (long[]) null));
        Assert.assertFalse(ArrayUtils.isSameLength(new long[]{1L}, (long[]) null));
        Assert.assertFalse(ArrayUtils.isSameLength((long[]) null, new long[]{1L}));
        Assert.assertFalse(ArrayUtils.isSameLength(new long[]{1L}, new long[]{1L, 2L}));
        Assert.assertTrue(ArrayUtils.isSameLength(new long[]{1L}, new long[]{2L}));

        Assert.assertTrue(ArrayUtils.isSameLength((int[]) null, (int[]) null));
        Assert.assertFalse(ArrayUtils.isSameLength(new int[]{1}, (int[]) null));
        Assert.assertFalse(ArrayUtils.isSameLength((int[]) null, new int[]{1}));
        Assert.assertFalse(ArrayUtils.isSameLength(new int[]{1}, new int[]{1, 2}));
        Assert.assertTrue(ArrayUtils.isSameLength(new int[]{1}, new int[]{2}));

        Assert.assertTrue(ArrayUtils.isSameLength((short[]) null, (short[]) null));
        Assert.assertFalse(ArrayUtils.isSameLength(new short[]{1}, (short[]) null));
        Assert.assertFalse(ArrayUtils.isSameLength((short[]) null, new short[]{1}));
        Assert.assertFalse(ArrayUtils.isSameLength(new short[]{1}, new short[]{1, 2}));
        Assert.assertTrue(ArrayUtils.isSameLength(new short[]{1}, new short[]{2}));

        Assert.assertTrue(ArrayUtils.isSameLength((char[]) null, (char[]) null));
        Assert.assertFalse(ArrayUtils.isSameLength(new char[]{'a'}, (char[]) null));
        Assert.assertFalse(ArrayUtils.isSameLength((char[]) null, new char[]{'a'}));
        Assert.assertFalse(ArrayUtils.isSameLength(new char[]{'a'}, new char[]{'a', 'b'}));
        Assert.assertTrue(ArrayUtils.isSameLength(new char[]{'a'}, new char[]{'b'}));

        Assert.assertTrue(ArrayUtils.isSameLength((byte[]) null, (byte[]) null));
        Assert.assertFalse(ArrayUtils.isSameLength(new byte[]{1}, (byte[]) null));
        Assert.assertFalse(ArrayUtils.isSameLength((byte[]) null, new byte[]{1}));
        Assert.assertFalse(ArrayUtils.isSameLength(new byte[]{1}, new byte[]{1, 2}));
        Assert.assertTrue(ArrayUtils.isSameLength(new byte[]{1}, new byte[]{2}));

        Assert.assertTrue(ArrayUtils.isSameLength((double[]) null, (double[]) null));
        Assert.assertFalse(ArrayUtils.isSameLength(new double[]{1.0}, (double[]) null));
        Assert.assertFalse(ArrayUtils.isSameLength((double[]) null, new double[]{1.0}));
        Assert.assertFalse(ArrayUtils.isSameLength(new double[]{1.0}, new double[]{1.0, 2.0}));
        Assert.assertTrue(ArrayUtils.isSameLength(new double[]{1.0}, new double[]{2.0}));

        Assert.assertTrue(ArrayUtils.isSameLength((float[]) null, (float[]) null));
        Assert.assertFalse(ArrayUtils.isSameLength(new float[]{1.0f}, (float[]) null));
        Assert.assertFalse(ArrayUtils.isSameLength((float[]) null, new float[]{1.0f}));
        Assert.assertFalse(ArrayUtils.isSameLength(new float[]{1.0f}, new float[]{1.0f, 2.0f}));
        Assert.assertTrue(ArrayUtils.isSameLength(new float[]{1.0f}, new float[]{2.0f}));

        Assert.assertTrue(ArrayUtils.isSameLength((boolean[]) null, (boolean[]) null));
        Assert.assertFalse(ArrayUtils.isSameLength(new boolean[]{true}, (boolean[]) null));
        Assert.assertFalse(ArrayUtils.isSameLength((boolean[]) null, new boolean[]{true}));
        Assert.assertFalse(ArrayUtils.isSameLength(new boolean[]{true}, new boolean[]{true, false}));
        Assert.assertTrue(ArrayUtils.isSameLength(new boolean[]{true}, new boolean[]{false}));
    }

    @Test
    public void testGetLengthAndIsSameType() {
        Assert.assertEquals(0, ArrayUtils.getLength(null));
        Assert.assertEquals(2, ArrayUtils.getLength(new int[]{1, 2}));
        Assert.assertTrue(ArrayUtils.isSameType(new int[]{1}, new int[]{2}));
        Assert.assertFalse(ArrayUtils.isSameType(new int[]{1}, new double[]{2.0}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetLengthNotAnArray() {
        ArrayUtils.getLength("not an array");
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
        ArrayUtils.reverse((Object[]) null);
        String[] sArr = new String[]{"a", "b", "c"};
        ArrayUtils.reverse(sArr);
        Assert.assertArrayEquals(new String[]{"c", "b", "a"}, sArr);

        ArrayUtils.reverse((long[]) null);
        long[] lArr = new long[]{1L, 2L, 3L};
        ArrayUtils.reverse(lArr);
        Assert.assertArrayEquals(new long[]{3L, 2L, 1L}, lArr);

        ArrayUtils.reverse((int[]) null);
        int[] iArr = new int[]{1, 2, 3};
        ArrayUtils.reverse(iArr);
        Assert.assertArrayEquals(new int[]{3, 2, 1}, iArr);

        ArrayUtils.reverse((short[]) null);
        short[] shArr = new short[]{1, 2, 3};
        ArrayUtils.reverse(shArr);
        Assert.assertArrayEquals(new short[]{3, 2, 1}, shArr);

        ArrayUtils.reverse((char[]) null);
        char[] cArr = new char[]{'a', 'b', 'c'};
        ArrayUtils.reverse(cArr);
        Assert.assertArrayEquals(new char[]{'c', 'b', 'a'}, cArr);

        ArrayUtils.reverse((byte[]) null);
        byte[] bArr = new byte[]{1, 2, 3};
        ArrayUtils.reverse(bArr);
        Assert.assertArrayEquals(new byte[]{3, 2, 1}, bArr);

        ArrayUtils.reverse((double[]) null);
        double[] dArr = new double[]{1.0, 2.0, 3.0};
        ArrayUtils.reverse(dArr);
        Assert.assertArrayEquals(new double[]{3.0, 2.0, 1.0}, dArr, 0.0);

        ArrayUtils.reverse((float[]) null);
        float[] fArr = new float[]{1.0f, 2.0f, 3.0f};
        ArrayUtils.reverse(fArr);
        Assert.assertArrayEquals(new float[]{3.0f, 2.0f, 1.0f}, fArr, 0.0f);

        ArrayUtils.reverse((boolean[]) null);
        boolean[] boolArr = new boolean[]{true, false, true};
        ArrayUtils.reverse(boolArr);
        Assert.assertArrayEquals(new boolean[]{true, false, true}, boolArr);
    }

    @Test
    public void testIndexOfAndLastIndexOfObject() {
        Assert.assertEquals(-1, ArrayUtils.indexOf((Object[]) null, "a"));
        Assert.assertEquals(-1, ArrayUtils.lastIndexOf((Object[]) null, "a"));
        Assert.assertFalse(ArrayUtils.contains((Object[]) null, "a"));

        String[] array = new String[]{"a", null, "b", "a", null};
        Assert.assertEquals(0, ArrayUtils.indexOf(array, "a"));
        Assert.assertEquals(3, ArrayUtils.indexOf(array, "a", 1));
        Assert.assertEquals(1, ArrayUtils.indexOf(array, null));
        Assert.assertEquals(4, ArrayUtils.indexOf(array, null, 2));
        Assert.assertEquals(-1, ArrayUtils.indexOf(array, "c"));
        Assert.assertEquals(-1, ArrayUtils.indexOf(array, 123));

        Assert.assertEquals(3, ArrayUtils.lastIndexOf(array, "a"));
        Assert.assertEquals(0, ArrayUtils.lastIndexOf(array, "a", 2));
        Assert.assertEquals(4, ArrayUtils.lastIndexOf(array, null));
        Assert.assertEquals(1, ArrayUtils.lastIndexOf(array, null, 3));
        Assert.assertEquals(-1, ArrayUtils.lastIndexOf(array, "a", -1));
        Assert.assertEquals(3, ArrayUtils.lastIndexOf(array, "a", 10));
        Assert.assertEquals(-1, ArrayUtils.lastIndexOf(array, "c"));
        Assert.assertEquals(-1, ArrayUtils.lastIndexOf(array, 123));

        Assert.assertTrue(ArrayUtils.contains(array, "b"));
        Assert.assertTrue(ArrayUtils.contains(array, null));
        Assert.assertFalse(ArrayUtils.contains(array, "z"));
    }

    @Test
    public void testIndexOfAndLastIndexOfPrimitives() {
        long[] lArr = new long[]{1L, 2L, 3L, 2L};
        Assert.assertEquals(-1, ArrayUtils.indexOf((long[]) null, 1L));
        Assert.assertEquals(1, ArrayUtils.indexOf(lArr, 2L, -1));
        Assert.assertEquals(3, ArrayUtils.lastIndexOf(lArr, 2L, 10));
        Assert.assertEquals(1, ArrayUtils.lastIndexOf(lArr, 2L, 2));
        Assert.assertEquals(-1, ArrayUtils.lastIndexOf(lArr, 2L, -1));
        Assert.assertTrue(ArrayUtils.contains(lArr, 3L));
        Assert.assertFalse(ArrayUtils.contains((long[]) null, 3L));

        int[] iArr = new int[]{1, 2, 3, 2};
        Assert.assertEquals(-1, ArrayUtils.indexOf((int[]) null, 1));
        Assert.assertEquals(1, ArrayUtils.indexOf(iArr, 2, -1));
        Assert.assertEquals(3, ArrayUtils.lastIndexOf(iArr, 2, 10));
        Assert.assertEquals(1, ArrayUtils.lastIndexOf(iArr, 2, 2));
        Assert.assertEquals(-1, ArrayUtils.lastIndexOf(iArr, 2, -1));
        Assert.assertTrue(ArrayUtils.contains(iArr, 3));
        Assert.assertFalse(ArrayUtils.contains((int[]) null, 3));

        short[] sArr = new short[]{1, 2, 3, 2};
        Assert.assertEquals(-1, ArrayUtils.indexOf((short[]) null, (short) 1));
        Assert.assertEquals(1, ArrayUtils.indexOf(sArr, (short) 2, -1));
        Assert.assertEquals(3, ArrayUtils.lastIndexOf(sArr, (short) 2, 10));
        Assert.assertEquals(1, ArrayUtils.lastIndexOf(sArr, (short) 2, 2));
        Assert.assertEquals(-1, ArrayUtils.lastIndexOf(sArr, (short) 2, -1));
        Assert.assertTrue(ArrayUtils.contains(sArr, (short) 3));
        Assert.assertFalse(ArrayUtils.contains((short[]) null, (short) 3));

        char[] cArr = new char[]{'a', 'b', 'c', 'b'};
        Assert.assertEquals(-1, ArrayUtils.indexOf((char[]) null, 'a'));
        Assert.assertEquals(1, ArrayUtils.indexOf(cArr, 'b', -1));
        Assert.assertEquals(3, ArrayUtils.lastIndexOf(cArr, 'b', 10));
        Assert.assertEquals(1, ArrayUtils.lastIndexOf(cArr, 'b', 2));
        Assert.assertEquals(-1, ArrayUtils.lastIndexOf(cArr, 'b', -1));
        Assert.assertTrue(ArrayUtils.contains(cArr, 'c'));
        Assert.assertFalse(ArrayUtils.contains((char[]) null, 'c'));

        byte[] bArr = new byte[]{1, 2, 3, 2};
        Assert.assertEquals(-1, ArrayUtils.indexOf((byte[]) null, (byte) 1));
        Assert.assertEquals(1, ArrayUtils.indexOf(bArr, (byte) 2, -1));
        Assert.assertEquals(3, ArrayUtils.lastIndexOf(bArr, (byte) 2, 10));
        Assert.assertEquals(1, ArrayUtils.lastIndexOf(bArr, (byte) 2, 2));
        Assert.assertEquals(-1, ArrayUtils.lastIndexOf(bArr, (byte) 2, -1));
        Assert.assertTrue(ArrayUtils.contains(bArr, (byte) 3));
        Assert.assertFalse(ArrayUtils.contains((byte[]) null, (byte) 3));

        double[] dArr = new double[]{1.0, 2.0, 3.0, 2.0};
        Assert.assertEquals(-1, ArrayUtils.indexOf((double[]) null, 1.0));
        Assert.assertEquals(1, ArrayUtils.indexOf(dArr, 2.0, -1));
        Assert.assertEquals(3, ArrayUtils.lastIndexOf(dArr, 2.0, 10));
        Assert.assertEquals(1, ArrayUtils.lastIndexOf(dArr, 2.0, 2));
        Assert.assertEquals(-1, ArrayUtils.lastIndexOf(dArr, 2.0, -1));
        Assert.assertTrue(ArrayUtils.contains(dArr, 3.0));
        Assert.assertFalse(ArrayUtils.contains((double[]) null, 3.0));
        Assert.assertEquals(1, ArrayUtils.indexOf(dArr, 2.05, 0.1));
        Assert.assertEquals(3, ArrayUtils.lastIndexOf(dArr, 2.05, 0.1));
        Assert.assertEquals(1, ArrayUtils.lastIndexOf(dArr, 2.05, 2, 0.1));
        Assert.assertEquals(-1, ArrayUtils.indexOf((double[]) null, 2.0, 0.1));
        Assert.assertEquals(-1, ArrayUtils.lastIndexOf((double[]) null, 2.0, 0.1));
        Assert.assertEquals(-1, ArrayUtils.lastIndexOf(dArr, 2.05, -1, 0.1));
        Assert.assertEquals(3, ArrayUtils.lastIndexOf(dArr, 2.05, 10, 0.1));
        Assert.assertTrue(ArrayUtils.contains(dArr, 2.05, 0.1));

        float[] fArr = new float[]{1.0f, 2.0f, 3.0f, 2.0f};
        Assert.assertEquals(-1, ArrayUtils.indexOf((float[]) null, 1.0f));
        Assert.assertEquals(1, ArrayUtils.indexOf(fArr, 2.0f, -1));
        Assert.assertEquals(3, ArrayUtils.lastIndexOf(fArr, 2.0f, 10));
        Assert.assertEquals(1, ArrayUtils.lastIndexOf(fArr, 2.0f, 2));
        Assert.assertEquals(-1, ArrayUtils.lastIndexOf(fArr, 2.0f, -1));
        Assert.assertTrue(ArrayUtils.contains(fArr, 3.0f));
        Assert.assertFalse(ArrayUtils.contains((float[]) null, 3.0f));

        boolean[] boolArr = new boolean[]{true, false, true};
        Assert.assertEquals(-1, ArrayUtils.indexOf((boolean[]) null, true));
        Assert.assertEquals(1, ArrayUtils.indexOf(boolArr, false, -1));
        Assert.assertEquals(2, ArrayUtils.lastIndexOf(boolArr, true, 10));
        Assert.assertEquals(0, ArrayUtils.lastIndexOf(boolArr, true, 1));
        Assert.assertEquals(-1, ArrayUtils.lastIndexOf(boolArr, true, -1));
        Assert.assertTrue(ArrayUtils.contains(boolArr, false));
        Assert.assertFalse(ArrayUtils.contains((boolean[]) null, false));
    }

    @Test
    public void testConvertersChar() {
        Assert.assertNull(ArrayUtils.toPrimitive((Character[]) null));
        Assert.assertArrayEquals(new char[0], ArrayUtils.toPrimitive(new Character[0]));
        Assert.assertArrayEquals(new char[]{'a', 'b'}, ArrayUtils.toPrimitive(new Character[]{Character.valueOf('a'), Character.valueOf('b')}));

        Assert.assertNull(ArrayUtils.toPrimitive((Character[]) null, 'c'));
        Assert.assertArrayEquals(new char[0], ArrayUtils.toPrimitive(new Character[0], 'c'));
        Assert.assertArrayEquals(new char[]{'a', 'c'}, ArrayUtils.toPrimitive(new Character[]{Character.valueOf('a'), null}, 'c'));

        Assert.assertNull(ArrayUtils.toObject((char[]) null));
        Assert.assertArrayEquals(new Character[0], ArrayUtils.toObject(new char[0]));
        Assert.assertArrayEquals(new Character[]{Character.valueOf('a')}, ArrayUtils.toObject(new char[]{'a'}));
    }

    @Test
    public void testConvertersLong() {
        Assert.assertNull(ArrayUtils.toPrimitive((Long[]) null));
        Assert.assertArrayEquals(new long[0], ArrayUtils.toPrimitive(new Long[0]));
        Assert.assertArrayEquals(new long[]{1L}, ArrayUtils.toPrimitive(new Long[]{Long.valueOf(1L)}));

        Assert.assertNull(ArrayUtils.toPrimitive((Long[]) null, 2L));
        Assert.assertArrayEquals(new long[0], ArrayUtils.toPrimitive(new Long[0], 2L));
        Assert.assertArrayEquals(new long[]{1L, 2L}, ArrayUtils.toPrimitive(new Long[]{Long.valueOf(1L), null}, 2L));

        Assert.assertNull(ArrayUtils.toObject((long[]) null));
        Assert.assertArrayEquals(new Long[0], ArrayUtils.toObject(new long[0]));
        Assert.assertArrayEquals(new Long[]{Long.valueOf(1L)}, ArrayUtils.toObject(new long[]{1L}));
    }

    @Test
    public void testConvertersInt() {
        Assert.assertNull(ArrayUtils.toPrimitive((Integer[]) null));
        Assert.assertArrayEquals(new int[0], ArrayUtils.toPrimitive(new Integer[0]));
        Assert.assertArrayEquals(new int[]{1}, ArrayUtils.toPrimitive(new Integer[]{Integer.valueOf(1)}));

        Assert.assertNull(ArrayUtils.toPrimitive((Integer[]) null, 2));
        Assert.assertArrayEquals(new int[0], ArrayUtils.toPrimitive(new Integer[0], 2));
        Assert.assertArrayEquals(new int[]{1, 2}, ArrayUtils.toPrimitive(new Integer[]{Integer.valueOf(1), null}, 2));

        Assert.assertNull(ArrayUtils.toObject((int[]) null));
        Assert.assertArrayEquals(new Integer[0], ArrayUtils.toObject(new int[0]));
        Assert.assertArrayEquals(new Integer[]{Integer.valueOf(1)}, ArrayUtils.toObject(new int[]{1}));
    }

    @Test
    public void testConvertersShort() {
        Assert.assertNull(ArrayUtils.toPrimitive((Short[]) null));
        Assert.assertArrayEquals(new short[0], ArrayUtils.toPrimitive(new Short[0]));
        Assert.assertArrayEquals(new short[]{1}, ArrayUtils.toPrimitive(new Short[]{Short.valueOf((short) 1)}));

        Assert.assertNull(ArrayUtils.toPrimitive((Short[]) null, (short) 2));
        Assert.assertArrayEquals(new short[0], ArrayUtils.toPrimitive(new Short[0], (short) 2));
        Assert.assertArrayEquals(new short[]{1, 2}, ArrayUtils.toPrimitive(new Short[]{Short.valueOf((short) 1), null}, (short) 2));

        Assert.assertNull(ArrayUtils.toObject((short[]) null));
        Assert.assertArrayEquals(new Short[0], ArrayUtils.toObject(new short[0]));
        Assert.assertArrayEquals(new Short[]{Short.valueOf((short) 1)}, ArrayUtils.toObject(new short[]{1}));
    }

    @Test
    public void testConvertersByte() {
        Assert.assertNull(ArrayUtils.toPrimitive((Byte[]) null));
        Assert.assertArrayEquals(new byte[0], ArrayUtils.toPrimitive(new Byte[0]));
        Assert.assertArrayEquals(new byte[]{1}, ArrayUtils.toPrimitive(new Byte[]{Byte.valueOf((byte) 1)}));

        Assert.assertNull(ArrayUtils.toPrimitive((Byte[]) null, (byte) 2));
        Assert.assertArrayEquals(new byte[0], ArrayUtils.toPrimitive(new Byte[0], (byte) 2));
        Assert.assertArrayEquals(new byte[]{1, 2}, ArrayUtils.toPrimitive(new Byte[]{Byte.valueOf((byte) 1), null}, (byte) 2));

        Assert.assertNull(ArrayUtils.toObject((byte[]) null));
        Assert.assertArrayEquals(new Byte[0], ArrayUtils.toObject(new byte[0]));
        Assert.assertArrayEquals(new Byte[]{Byte.valueOf((byte) 1)}, ArrayUtils.toObject(new byte[]{1}));
    }

    @Test
    public void testConvertersDouble() {
        Assert.assertNull(ArrayUtils.toPrimitive((Double[]) null));
        Assert.assertArrayEquals(new double[0], ArrayUtils.toPrimitive(new Double[0]), 0.0);
        Assert.assertArrayEquals(new double[]{1.0}, ArrayUtils.toPrimitive(new Double[]{Double.valueOf(1.0)}), 0.0);

        Assert.assertNull(ArrayUtils.toPrimitive((Double[]) null, 2.0));
        Assert.assertArrayEquals(new double[0], ArrayUtils.toPrimitive(new Double[0], 2.0), 0.0);
        Assert.assertArrayEquals(new double[]{1.0, 2.0}, ArrayUtils.toPrimitive(new Double[]{Double.valueOf(1.0), null}, 2.0), 0.0);

        Assert.assertNull(ArrayUtils.toObject((double[]) null));
        Assert.assertArrayEquals(new Double[0], ArrayUtils.toObject(new double[0]));
        Assert.assertArrayEquals(new Double[]{Double.valueOf(1.0)}, ArrayUtils.toObject(new double[]{1.0}));
    }

    @Test
    public void testConvertersFloat() {
        Assert.assertNull(ArrayUtils.toPrimitive((Float[]) null));
        Assert.assertArrayEquals(new float[0], ArrayUtils.toPrimitive(new Float[0]), 0.0f);
        Assert.assertArrayEquals(new float[]{1.0f}, ArrayUtils.toPrimitive(new Float[]{Float.valueOf(1.0f)}), 0.0f);

        Assert.assertNull(ArrayUtils.toPrimitive((Float[]) null, 2.0f));
        Assert.assertArrayEquals(new float[0], ArrayUtils.toPrimitive(new Float[0], 2.0f), 0.0f);
        Assert.assertArrayEquals(new float[]{1.0f, 2.0f}, ArrayUtils.toPrimitive(new Float[]{Float.valueOf(1.0f), null}, 2.0f), 0.0f);

        Assert.assertNull(ArrayUtils.toObject((float[]) null));
        Assert.assertArrayEquals(new Float[0], ArrayUtils.toObject(new float[0]));
        Assert.assertArrayEquals(new Float[]{Float.valueOf(1.0f)}, ArrayUtils.toObject(new float[]{1.0f}));
    }

    @Test
    public void testConvertersBoolean() {
        Assert.assertNull(ArrayUtils.toPrimitive((Boolean[]) null));
        Assert.assertArrayEquals(new boolean[0], ArrayUtils.toPrimitive(new Boolean[0]));
        Assert.assertArrayEquals(new boolean[]{true}, ArrayUtils.toPrimitive(new Boolean[]{Boolean.TRUE}));

        Assert.assertNull(ArrayUtils.toPrimitive((Boolean[]) null, false));
        Assert.assertArrayEquals(new boolean[0], ArrayUtils.toPrimitive(new Boolean[0], false));
        Assert.assertArrayEquals(new boolean[]{true, false}, ArrayUtils.toPrimitive(new Boolean[]{Boolean.TRUE, null}, false));

        Assert.assertNull(ArrayUtils.toObject((boolean[]) null));
        Assert.assertArrayEquals(new Boolean[0], ArrayUtils.toObject(new boolean[0]));
        Assert.assertArrayEquals(new Boolean[]{Boolean.TRUE, Boolean.FALSE}, ArrayUtils.toObject(new boolean[]{true, false}));
    }

    @Test
    public void testIsEmpty() {
        Assert.assertTrue(ArrayUtils.isEmpty((Object[]) null));
        Assert.assertTrue(ArrayUtils.isEmpty(new Object[0]));
        Assert.assertFalse(ArrayUtils.isEmpty(new Object[]{"a"}));

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
        Assert.assertNull(ArrayUtils.addAll((Object[]) null, (Object[]) null));
        Assert.assertArrayEquals(new String[]{"a"}, ArrayUtils.addAll(new String[]{"a"}, (String[]) null));
        Assert.assertArrayEquals(new String[]{"b"}, ArrayUtils.addAll((String[]) null, new String[]{"b"}));
        Assert.assertArrayEquals(new String[]{"a", "b"}, ArrayUtils.addAll(new String[]{"a"}, new String[]{"b"}));

        Assert.assertNull(ArrayUtils.addAll((boolean[]) null, (boolean[]) null));
        Assert.assertArrayEquals(new boolean[]{true}, ArrayUtils.addAll(new boolean[]{true}, (boolean[]) null));
        Assert.assertArrayEquals(new boolean[]{false}, ArrayUtils.addAll((boolean[]) null, new boolean[]{false}));
        Assert.assertArrayEquals(new boolean[]{true, false}, ArrayUtils.addAll(new boolean[]{true}, new boolean[]{false}));

        Assert.assertNull(ArrayUtils.addAll((char[]) null, (char[]) null));
        Assert.assertArrayEquals(new char[]{'a'}, ArrayUtils.addAll(new char[]{'a'}, (char[]) null));
        Assert.assertArrayEquals(new char[]{'b'}, ArrayUtils.addAll((char[]) null, new char[]{'b'}));
        Assert.assertArrayEquals(new char[]{'a', 'b'}, ArrayUtils.addAll(new char[]{'a'}, new char[]{'b'}));

        Assert.assertNull(ArrayUtils.addAll((byte[]) null, (byte[]) null));
        Assert.assertArrayEquals(new byte[]{1}, ArrayUtils.addAll(new byte[]{1}, (byte[]) null));
        Assert.assertArrayEquals(new byte[]{2}, ArrayUtils.addAll((byte[]) null, new byte[]{2}));
        Assert.assertArrayEquals(new byte[]{1, 2}, ArrayUtils.addAll(new byte[]{1}, new byte[]{2}));

        Assert.assertNull(ArrayUtils.addAll((short[]) null, (short[]) null));
        Assert.assertArrayEquals(new short[]{1}, ArrayUtils.addAll(new short[]{1}, (short[]) null));
        Assert.assertArrayEquals(new short[]{2}, ArrayUtils.addAll((short[]) null, new short[]{2}));
        Assert.assertArrayEquals(new short[]{1, 2}, ArrayUtils.addAll(new short[]{1}, new short[]{2}));

        Assert.assertNull(ArrayUtils.addAll((int[]) null, (int[]) null));
        Assert.assertArrayEquals(new int[]{1}, ArrayUtils.addAll(new int[]{1}, (int[]) null));
        Assert.assertArrayEquals(new int[]{2}, ArrayUtils.addAll((int[]) null, new int[]{2}));
        Assert.assertArrayEquals(new int[]{1, 2}, ArrayUtils.addAll(new int[]{1}, new int[]{2}));

        Assert.assertNull(ArrayUtils.addAll((long[]) null, (long[]) null));
        Assert.assertArrayEquals(new long[]{1L}, ArrayUtils.addAll(new long[]{1L}, (long[]) null));
        Assert.assertArrayEquals(new long[]{2L}, ArrayUtils.addAll((long[]) null, new long[]{2L}));
        Assert.assertArrayEquals(new long[]{1L, 2L}, ArrayUtils.addAll(new long[]{1L}, new long[]{2L}));

        Assert.assertNull(ArrayUtils.addAll((float[]) null, (float[]) null));
        Assert.assertArrayEquals(new float[]{1.0f}, ArrayUtils.addAll(new float[]{1.0f}, (float[]) null), 0.0f);
        Assert.assertArrayEquals(new float[]{2.0f}, ArrayUtils.addAll((float[]) null, new float[]{2.0f}), 0.0f);
        Assert.assertArrayEquals(new float[]{1.0f, 2.0f}, ArrayUtils.addAll(new float[]{1.0f}, new float[]{2.0f}), 0.0f);

        Assert.assertNull(ArrayUtils.addAll((double[]) null, (double[]) null));
        Assert.assertArrayEquals(new double[]{1.0}, ArrayUtils.addAll(new double[]{1.0}, (double[]) null), 0.0);
        Assert.assertArrayEquals(new double[]{2.0}, ArrayUtils.addAll((double[]) null, new double[]{2.0}), 0.0);
        Assert.assertArrayEquals(new double[]{1.0, 2.0}, ArrayUtils.addAll(new double[]{1.0}, new double[]{2.0}), 0.0);
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
        Assert.assertArrayEquals(new String[]{"a", "b", "c"}, ArrayUtils.add(new String[]{"a", "c"}, 1, "b"));

        Assert.assertArrayEquals(new boolean[]{true}, ArrayUtils.add((boolean[]) null, 0, true));
        Assert.assertArrayEquals(new boolean[]{true, false, true}, ArrayUtils.add(new boolean[]{true, true}, 1, false));

        Assert.assertArrayEquals(new char[]{'a'}, ArrayUtils.add((char[]) null, 0, 'a'));
        Assert.assertArrayEquals(new char[]{'a', 'b', 'c'}, ArrayUtils.add(new char[]{'a', 'c'}, 1, 'b'));

        Assert.assertArrayEquals(new byte[]{1}, ArrayUtils.add((byte[]) null, 0, (byte) 1));
        Assert.assertArrayEquals(new byte[]{1, 2, 3}, ArrayUtils.add(new byte[]{1, 3}, 1, (byte) 2));

        Assert.assertArrayEquals(new short[]{1}, ArrayUtils.add((short[]) null, 0, (short) 1));
        Assert.assertArrayEquals(new short[]{1, 2, 3}, ArrayUtils.add(new short[]{1, 3}, 1, (short) 2));

        Assert.assertArrayEquals(new int[]{1}, ArrayUtils.add((int[]) null, 0, 1));
        Assert.assertArrayEquals(new int[]{1, 2, 3}, ArrayUtils.add(new int[]{1, 3}, 1, 2));

        Assert.assertArrayEquals(new long[]{1L}, ArrayUtils.add((long[]) null, 0, 1L));
        Assert.assertArrayEquals(new long[]{1L, 2L, 3L}, ArrayUtils.add(new long[]{1L, 3L}, 1, 2L));

        Assert.assertArrayEquals(new float[]{1.0f}, ArrayUtils.add((float[]) null, 0, 1.0f), 0.0f);
        Assert.assertArrayEquals(new float[]{1.0f, 2.0f, 3.0f}, ArrayUtils.add(new float[]{1.0f, 3.0f}, 1, 2.0f), 0.0f);

        Assert.assertArrayEquals(new double[]{1.0}, ArrayUtils.add((double[]) null, 0, 1.0), 0.0);
        Assert.assertArrayEquals(new double[]{1.0, 2.0, 3.0}, ArrayUtils.add(new double[]{1.0, 3.0}, 1, 2.0), 0.0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddAtIndexOutOfBoundsNullArray() {
        ArrayUtils.add((String[]) null, 1, "a");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddAtIndexOutOfBoundsNegative() {
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
    public void testRemoveNullArray() {
        ArrayUtils.remove((Object[]) null, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveNegativeIndex() {
        ArrayUtils.remove(new int[]{1, 2}, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveHighIndex() {
        ArrayUtils.remove(new int[]{1, 2}, 2);
    }

    @Test
    public void testRemoveElement() {
        Assert.assertNull(ArrayUtils.removeElement((Object[]) null, "a"));
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
