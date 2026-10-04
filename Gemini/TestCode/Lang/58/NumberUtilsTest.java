package org.apache.commons.lang.math;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

public class NumberUtilsTest {

    @Test
    public void testConstants() {
        Assert.assertEquals(Long.valueOf(0L), NumberUtils.LONG_ZERO);
        Assert.assertEquals(Long.valueOf(1L), NumberUtils.LONG_ONE);
        Assert.assertEquals(Long.valueOf(-1L), NumberUtils.LONG_MINUS_ONE);
        Assert.assertEquals(Integer.valueOf(0), NumberUtils.INTEGER_ZERO);
        Assert.assertEquals(Integer.valueOf(1), NumberUtils.INTEGER_ONE);
        Assert.assertEquals(Integer.valueOf(-1), NumberUtils.INTEGER_MINUS_ONE);
        Assert.assertEquals(Short.valueOf((short) 0), NumberUtils.SHORT_ZERO);
        Assert.assertEquals(Short.valueOf((short) 1), NumberUtils.SHORT_ONE);
        Assert.assertEquals(Short.valueOf((short) -1), NumberUtils.SHORT_MINUS_ONE);
        Assert.assertEquals(Byte.valueOf((byte) 0), NumberUtils.BYTE_ZERO);
        Assert.assertEquals(Byte.valueOf((byte) 1), NumberUtils.BYTE_ONE);
        Assert.assertEquals(Byte.valueOf((byte) -1), NumberUtils.BYTE_MINUS_ONE);
        Assert.assertEquals(Double.valueOf(0.0d), NumberUtils.DOUBLE_ZERO);
        Assert.assertEquals(Double.valueOf(1.0d), NumberUtils.DOUBLE_ONE);
        Assert.assertEquals(Double.valueOf(-1.0d), NumberUtils.DOUBLE_MINUS_ONE);
        Assert.assertEquals(Float.valueOf(0.0f), NumberUtils.FLOAT_ZERO);
        Assert.assertEquals(Float.valueOf(1.0f), NumberUtils.FLOAT_ONE);
        Assert.assertEquals(Float.valueOf(-1.0f), NumberUtils.FLOAT_MINUS_ONE);
    }

    @Test
    public void testConstructor() {
        Assert.assertNotNull(new NumberUtils());
    }

    @Test
    public void testStringToInt() {
        Assert.assertEquals(0, NumberUtils.stringToInt(null));
        Assert.assertEquals(0, NumberUtils.stringToInt(""));
        Assert.assertEquals(123, NumberUtils.stringToInt("123"));
        Assert.assertEquals(0, NumberUtils.stringToInt("abc"));
        Assert.assertEquals(5, NumberUtils.stringToInt(null, 5));
        Assert.assertEquals(5, NumberUtils.stringToInt("abc", 5));
        Assert.assertEquals(123, NumberUtils.stringToInt("123", 5));
    }

    @Test
    public void testToInt() {
        Assert.assertEquals(0, NumberUtils.toInt(null));
        Assert.assertEquals(0, NumberUtils.toInt(""));
        Assert.assertEquals(123, NumberUtils.toInt("123"));
        Assert.assertEquals(0, NumberUtils.toInt("abc"));
        Assert.assertEquals(5, NumberUtils.toInt(null, 5));
        Assert.assertEquals(5, NumberUtils.toInt("abc", 5));
        Assert.assertEquals(123, NumberUtils.toInt("123", 5));
    }

    @Test
    public void testToLong() {
        Assert.assertEquals(0L, NumberUtils.toLong(null));
        Assert.assertEquals(0L, NumberUtils.toLong(""));
        Assert.assertEquals(123L, NumberUtils.toLong("123"));
        Assert.assertEquals(0L, NumberUtils.toLong("abc"));
        Assert.assertEquals(5L, NumberUtils.toLong(null, 5L));
        Assert.assertEquals(5L, NumberUtils.toLong("abc", 5L));
        Assert.assertEquals(123L, NumberUtils.toLong("123", 5L));
    }

    @Test
    public void testToFloat() {
        Assert.assertEquals(0.0f, NumberUtils.toFloat(null), 0.0001f);
        Assert.assertEquals(0.0f, NumberUtils.toFloat(""), 0.0001f);
        Assert.assertEquals(1.23f, NumberUtils.toFloat("1.23"), 0.0001f);
        Assert.assertEquals(0.0f, NumberUtils.toFloat("abc"), 0.0001f);
        Assert.assertEquals(5.5f, NumberUtils.toFloat(null, 5.5f), 0.0001f);
        Assert.assertEquals(5.5f, NumberUtils.toFloat("abc", 5.5f), 0.0001f);
        Assert.assertEquals(1.23f, NumberUtils.toFloat("1.23", 5.5f), 0.0001f);
    }

    @Test
    public void testToDouble() {
        Assert.assertEquals(0.0d, NumberUtils.toDouble(null), 0.0001d);
        Assert.assertEquals(0.0d, NumberUtils.toDouble(""), 0.0001d);
        Assert.assertEquals(1.23d, NumberUtils.toDouble("1.23"), 0.0001d);
        Assert.assertEquals(0.0d, NumberUtils.toDouble("abc"), 0.0001d);
        Assert.assertEquals(5.5d, NumberUtils.toDouble(null, 5.5d), 0.0001d);
        Assert.assertEquals(5.5d, NumberUtils.toDouble("abc", 5.5d), 0.0001d);
        Assert.assertEquals(1.23d, NumberUtils.toDouble("1.23", 5.5d), 0.0001d);
    }

    @Test
    public void testCreateNumber() {
        Assert.assertNull(NumberUtils.createNumber(null));
        Assert.assertNull(NumberUtils.createNumber("--123"));
        Assert.assertEquals(Integer.valueOf(123), NumberUtils.createNumber("123"));
        Assert.assertEquals(Integer.valueOf(-123), NumberUtils.createNumber("-123"));
        Assert.assertEquals(Integer.valueOf(0x1a), NumberUtils.createNumber("0x1a"));
        Assert.assertEquals(Integer.valueOf(-0x1a), NumberUtils.createNumber("-0x1a"));
        Assert.assertEquals(Long.valueOf(2147483648L), NumberUtils.createNumber("2147483648"));
        Assert.assertEquals(new BigInteger("9223372036854775808"), NumberUtils.createNumber("9223372036854775808"));
        Assert.assertEquals(Float.valueOf("1.23"), NumberUtils.createNumber("1.23"));
        Assert.assertEquals(Double.valueOf("1.234567890123456"), NumberUtils.createNumber("1.234567890123456"));
        Assert.assertEquals(new BigDecimal("1.23456789012345678901234567890123456789012345678901234567890"), NumberUtils.createNumber("1.23456789012345678901234567890123456789012345678901234567890"));
        Assert.assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123l"));
        Assert.assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123L"));
        Assert.assertEquals(Long.valueOf(-123L), NumberUtils.createNumber("-123L"));
        Assert.assertEquals(new BigInteger("9223372036854775808"), NumberUtils.createNumber("9223372036854775808L"));
        Assert.assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23f"));
        Assert.assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23F"));
        Assert.assertEquals(Float.valueOf(0.0f), NumberUtils.createNumber("0.0f"));
        Assert.assertEquals(Double.valueOf(1.23d), NumberUtils.createNumber("1.23d"));
        Assert.assertEquals(Double.valueOf(1.23d), NumberUtils.createNumber("1.23D"));
        Assert.assertEquals(Double.valueOf(0.0d), NumberUtils.createNumber("0.0d"));
        Assert.assertEquals(new BigDecimal("1.23456789012345678901234567890"), NumberUtils.createNumber("1.23456789012345678901234567890D"));
        Assert.assertEquals(Double.valueOf("1.23e4"), NumberUtils.createNumber("1.23e4"));
        Assert.assertEquals(Double.valueOf("1.23E4"), NumberUtils.createNumber("1.23E4"));
        Assert.assertEquals(Float.valueOf("1.23e4f"), NumberUtils.createNumber("1.23e4f"));
        Assert.assertEquals(Double.valueOf("1.23e4d"), NumberUtils.createNumber("1.23e4d"));
        Assert.assertEquals(Double.valueOf("1e4"), NumberUtils.createNumber("1e4"));
        Assert.assertEquals(Float.valueOf("1e4f"), NumberUtils.createNumber("1e4f"));
        Assert.assertEquals(Double.valueOf("1e4d"), NumberUtils.createNumber("1e4d"));
        Assert.assertEquals(Float.valueOf("0.0"), NumberUtils.createNumber("0.0"));
        Assert.assertEquals(Float.valueOf("0."), NumberUtils.createNumber("0."));
        Assert.assertEquals(Float.valueOf(".0"), NumberUtils.createNumber(".0"));
        Assert.assertEquals(Double.valueOf("1e-4"), NumberUtils.createNumber("1e-4"));
        Assert.assertEquals(Float.valueOf("1e-4f"), NumberUtils.createNumber("1e-4f"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberBlank() {
        NumberUtils.createNumber(" ");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberEmpty() {
        NumberUtils.createNumber("");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberExpPosLessThanDecPos() {
        NumberUtils.createNumber("1e2.3");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidLongQualifierDec() {
        NumberUtils.createNumber("1.2L");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidLongQualifierExp() {
        NumberUtils.createNumber("1e2L");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidLongQualifierNonDigits() {
        NumberUtils.createNumber("1a2L");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidLongQualifierEmpty() {
        NumberUtils.createNumber("L");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidQualifier() {
        NumberUtils.createNumber("123q");
    }

    @Test
    public void testCreateFloat() {
        Assert.assertNull(NumberUtils.createFloat(null));
        Assert.assertEquals(Float.valueOf(1.23f), NumberUtils.createFloat("1.23"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateFloatFailure() {
        NumberUtils.createFloat("abc");
    }

    @Test
    public void testCreateDouble() {
        Assert.assertNull(NumberUtils.createDouble(null));
        Assert.assertEquals(Double.valueOf(1.23d), NumberUtils.createDouble("1.23"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateDoubleFailure() {
        NumberUtils.createDouble("abc");
    }

    @Test
    public void testCreateInteger() {
        Assert.assertNull(NumberUtils.createInteger(null));
        Assert.assertEquals(Integer.valueOf(123), NumberUtils.createInteger("123"));
        Assert.assertEquals(Integer.valueOf(0x12), NumberUtils.createInteger("0x12"));
        Assert.assertEquals(Integer.valueOf(012), NumberUtils.createInteger("012"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateIntegerFailure() {
        NumberUtils.createInteger("abc");
    }

    @Test
    public void testCreateLong() {
        Assert.assertNull(NumberUtils.createLong(null));
        Assert.assertEquals(Long.valueOf(123L), NumberUtils.createLong("123"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateLongFailure() {
        NumberUtils.createLong("abc");
    }

    @Test
    public void testCreateBigInteger() {
        Assert.assertNull(NumberUtils.createBigInteger(null));
        Assert.assertEquals(new BigInteger("12345678901234567890"), NumberUtils.createBigInteger("12345678901234567890"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigIntegerFailure() {
        NumberUtils.createBigInteger("abc");
    }

    @Test
    public void testCreateBigDecimal() {
        Assert.assertNull(NumberUtils.createBigDecimal(null));
        Assert.assertEquals(new BigDecimal("123.456"), NumberUtils.createBigDecimal("123.456"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigDecimalBlank() {
        NumberUtils.createBigDecimal("   ");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigDecimalFailure() {
        NumberUtils.createBigDecimal("abc");
    }

    @Test
    public void testEqualsByteArray() {
        byte[] b1 = new byte[]{1, 2, 3};
        byte[] b2 = new byte[]{1, 2, 3};
        byte[] b3 = new byte[]{1, 2, 4};
        byte[] b4 = new byte[]{1, 2};
        Assert.assertTrue(NumberUtils.equals(b1, b1));
        Assert.assertTrue(NumberUtils.equals(b1, b2));
        Assert.assertFalse(NumberUtils.equals(b1, null));
        Assert.assertFalse(NumberUtils.equals(null, b1));
        Assert.assertFalse(NumberUtils.equals(b1, b3));
        Assert.assertFalse(NumberUtils.equals(b1, b4));
    }

    @Test
    public void testEqualsShortArray() {
        short[] s1 = new short[]{1, 2, 3};
        short[] s2 = new short[]{1, 2, 3};
        short[] s3 = new short[]{1, 2, 4};
        short[] s4 = new short[]{1, 2};
        Assert.assertTrue(NumberUtils.equals(s1, s1));
        Assert.assertTrue(NumberUtils.equals(s1, s2));
        Assert.assertFalse(NumberUtils.equals(s1, null));
        Assert.assertFalse(NumberUtils.equals(null, s1));
        Assert.assertFalse(NumberUtils.equals(s1, s3));
        Assert.assertFalse(NumberUtils.equals(s1, s4));
    }

    @Test
    public void testEqualsIntArray() {
        int[] i1 = new int[]{1, 2, 3};
        int[] i2 = new int[]{1, 2, 3};
        int[] i3 = new int[]{1, 2, 4};
        int[] i4 = new int[]{1, 2};
        Assert.assertTrue(NumberUtils.equals(i1, i1));
        Assert.assertTrue(NumberUtils.equals(i1, i2));
        Assert.assertFalse(NumberUtils.equals(i1, null));
        Assert.assertFalse(NumberUtils.equals(null, i1));
        Assert.assertFalse(NumberUtils.equals(i1, i3));
        Assert.assertFalse(NumberUtils.equals(i1, i4));
    }

    @Test
    public void testEqualsLongArray() {
        long[] l1 = new long[]{1L, 2L, 3L};
        long[] l2 = new long[]{1L, 2L, 3L};
        long[] l3 = new long[]{1L, 2L, 4L};
        long[] l4 = new long[]{1L, 2L};
        Assert.assertTrue(NumberUtils.equals(l1, l1));
        Assert.assertTrue(NumberUtils.equals(l1, l2));
        Assert.assertFalse(NumberUtils.equals(l1, null));
        Assert.assertFalse(NumberUtils.equals(null, l1));
        Assert.assertFalse(NumberUtils.equals(l1, l3));
        Assert.assertFalse(NumberUtils.equals(l1, l4));
    }

    @Test
    public void testEqualsFloatArray() {
        float[] f1 = new float[]{1.0f, Float.NaN, -0.0f};
        float[] f2 = new float[]{1.0f, Float.NaN, -0.0f};
        float[] f3 = new float[]{1.0f, Float.NaN, 0.0f};
        float[] f4 = new float[]{1.0f, Float.NaN};
        Assert.assertTrue(NumberUtils.equals(f1, f1));
        Assert.assertTrue(NumberUtils.equals(f1, f2));
        Assert.assertFalse(NumberUtils.equals(f1, null));
        Assert.assertFalse(NumberUtils.equals(null, f1));
        Assert.assertFalse(NumberUtils.equals(f1, f3));
        Assert.assertFalse(NumberUtils.equals(f1, f4));
    }

    @Test
    public void testEqualsDoubleArray() {
        double[] d1 = new double[]{1.0d, Double.NaN, -0.0d};
        double[] d2 = new double[]{1.0d, Double.NaN, -0.0d};
        double[] d3 = new double[]{1.0d, Double.NaN, 0.0d};
        double[] d4 = new double[]{1.0d, Double.NaN};
        Assert.assertTrue(NumberUtils.equals(d1, d1));
        Assert.assertTrue(NumberUtils.equals(d1, d2));
        Assert.assertFalse(NumberUtils.equals(d1, null));
        Assert.assertFalse(NumberUtils.equals(null, d1));
        Assert.assertFalse(NumberUtils.equals(d1, d3));
        Assert.assertFalse(NumberUtils.equals(d1, d4));
    }

    @Test
    public void testMinLongArray() {
        Assert.assertEquals(1L, NumberUtils.min(new long[]{3L, 1L, 2L}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinLongArrayNull() {
        NumberUtils.min((long[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinLongArrayEmpty() {
        NumberUtils.min(new long[]{});
    }

    @Test
    public void testMinIntArray() {
        Assert.assertEquals(1, NumberUtils.min(new int[]{3, 1, 2}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinIntArrayNull() {
        NumberUtils.min((int[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinIntArrayEmpty() {
        NumberUtils.min(new int[]{});
    }

    @Test
    public void testMinShortArray() {
        Assert.assertEquals((short) 1, NumberUtils.min(new short[]{3, 1, 2}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinShortArrayNull() {
        NumberUtils.min((short[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinShortArrayEmpty() {
        NumberUtils.min(new short[]{});
    }

    @Test
    public void testMinByteArray() {
        Assert.assertEquals((byte) 1, NumberUtils.min(new byte[]{3, 1, 2}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinByteArrayNull() {
        NumberUtils.min((byte[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinByteArrayEmpty() {
        NumberUtils.min(new byte[]{});
    }

    @Test
    public void testMinDoubleArray() {
        Assert.assertEquals(1.1d, NumberUtils.min(new double[]{3.3d, 1.1d, 2.2d}), 0.0001d);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinDoubleArrayNull() {
        NumberUtils.min((double[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinDoubleArrayEmpty() {
        NumberUtils.min(new double[]{});
    }

    @Test
    public void testMinFloatArray() {
        Assert.assertEquals(1.1f, NumberUtils.min(new float[]{3.3f, 1.1f, 2.2f}), 0.0001f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinFloatArrayNull() {
        NumberUtils.min((float[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinFloatArrayEmpty() {
        NumberUtils.min(new float[]{});
    }

    @Test
    public void testMaxLongArray() {
        Assert.assertEquals(3L, NumberUtils.max(new long[]{1L, 3L, 2L}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxLongArrayNull() {
        NumberUtils.max((long[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxLongArrayEmpty() {
        NumberUtils.max(new long[]{});
    }

    @Test
    public void testMaxIntArray() {
        Assert.assertEquals(3, NumberUtils.max(new int[]{1, 3, 2}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxIntArrayNull() {
        NumberUtils.max((int[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxIntArrayEmpty() {
        NumberUtils.max(new int[]{});
    }

    @Test
    public void testMaxShortArray() {
        Assert.assertEquals((short) 3, NumberUtils.max(new short[]{1, 3, 2}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxShortArrayNull() {
        NumberUtils.max((short[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxShortArrayEmpty() {
        NumberUtils.max(new short[]{});
    }

    @Test
    public void testMaxByteArray() {
        Assert.assertEquals((byte) 3, NumberUtils.max(new byte[]{1, 3, 2}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxByteArrayNull() {
        NumberUtils.max((byte[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxByteArrayEmpty() {
        NumberUtils.max(new byte[]{});
    }

    @Test
    public void testMaxDoubleArray() {
        Assert.assertEquals(3.3d, NumberUtils.max(new double[]{1.1d, 3.3d, 2.2d}), 0.0001d);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxDoubleArrayNull() {
        NumberUtils.max((double[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxDoubleArrayEmpty() {
        NumberUtils.max(new double[]{});
    }

    @Test
    public void testMaxFloatArray() {
        Assert.assertEquals(3.3f, NumberUtils.max(new float[]{1.1f, 3.3f, 2.2f}), 0.0001f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxFloatArrayNull() {
        NumberUtils.max((float[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxFloatArrayEmpty() {
        NumberUtils.max(new float[]{});
    }

    @Test
    public void testMinThreeValues() {
        Assert.assertEquals(1L, NumberUtils.min(1L, 2L, 3L));
        Assert.assertEquals(1L, NumberUtils.min(2L, 1L, 3L));
        Assert.assertEquals(1L, NumberUtils.min(3L, 2L, 1L));

        Assert.assertEquals(1, NumberUtils.min(1, 2, 3));
        Assert.assertEquals(1, NumberUtils.min(2, 1, 3));
        Assert.assertEquals(1, NumberUtils.min(3, 2, 1));

        Assert.assertEquals((short) 1, NumberUtils.min((short) 1, (short) 2, (short) 3));
        Assert.assertEquals((short) 1, NumberUtils.min((short) 2, (short) 1, (short) 3));
        Assert.assertEquals((short) 1, NumberUtils.min((short) 3, (short) 2, (short) 1));

        Assert.assertEquals((byte) 1, NumberUtils.min((byte) 1, (byte) 2, (byte) 3));
        Assert.assertEquals((byte) 1, NumberUtils.min((byte) 2, (byte) 1, (byte) 3));
        Assert.assertEquals((byte) 1, NumberUtils.min((byte) 3, (byte) 2, (byte) 1));

        Assert.assertEquals(1.0d, NumberUtils.min(1.0d, 2.0d, 3.0d), 0.0001d);
        Assert.assertEquals(1.0d, NumberUtils.min(2.0d, 1.0d, 3.0d), 0.0001d);
        Assert.assertEquals(1.0d, NumberUtils.min(3.0d, 2.0d, 1.0d), 0.0001d);
        Assert.assertTrue(Double.isNaN(NumberUtils.min(Double.NaN, 1.0d, 2.0d)));

        Assert.assertEquals(1.0f, NumberUtils.min(1.0f, 2.0f, 3.0f), 0.0001f);
        Assert.assertEquals(1.0f, NumberUtils.min(2.0f, 1.0f, 3.0f), 0.0001f);
        Assert.assertEquals(1.0f, NumberUtils.min(3.0f, 2.0f, 1.0f), 0.0001f);
        Assert.assertTrue(Float.isNaN(NumberUtils.min(Float.NaN, 1.0f, 2.0f)));
    }

    @Test
    public void testMaxThreeValues() {
        Assert.assertEquals(3L, NumberUtils.max(1L, 2L, 3L));
        Assert.assertEquals(3L, NumberUtils.max(1L, 3L, 2L));
        Assert.assertEquals(3L, NumberUtils.max(3L, 2L, 1L));

        Assert.assertEquals(3, NumberUtils.max(1, 2, 3));
        Assert.assertEquals(3, NumberUtils.max(1, 3, 2));
        Assert.assertEquals(3, NumberUtils.max(3, 2, 1));

        Assert.assertEquals((short) 3, NumberUtils.max((short) 1, (short) 2, (short) 3));
        Assert.assertEquals((short) 3, NumberUtils.max((short) 1, (short) 3, (short) 2));
        Assert.assertEquals((short) 3, NumberUtils.max((short) 3, (short) 2, (short) 1));

        Assert.assertEquals((byte) 3, NumberUtils.max((byte) 1, (byte) 2, (byte) 3));
        Assert.assertEquals((byte) 3, NumberUtils.max((byte) 1, (byte) 3, (byte) 2));
        Assert.assertEquals((byte) 3, NumberUtils.max((byte) 3, (byte) 2, (byte) 1));

        Assert.assertEquals(3.0d, NumberUtils.max(1.0d, 2.0d, 3.0d), 0.0001d);
        Assert.assertEquals(3.0d, NumberUtils.max(1.0d, 3.0d, 2.0d), 0.0001d);
        Assert.assertEquals(3.0d, NumberUtils.max(3.0d, 2.0d, 1.0d), 0.0001d);
        Assert.assertTrue(Double.isNaN(NumberUtils.max(Double.NaN, 1.0d, 2.0d)));

        Assert.assertEquals(3.0f, NumberUtils.max(1.0f, 2.0f, 3.0f), 0.0001f);
        Assert.assertEquals(3.0f, NumberUtils.max(1.0f, 3.0f, 2.0f), 0.0001f);
        Assert.assertEquals(3.0f, NumberUtils.max(3.0f, 2.0f, 1.0f), 0.0001f);
        Assert.assertTrue(Float.isNaN(NumberUtils.max(Float.NaN, 1.0f, 2.0f)));
    }

    @Test
    public void testCompareDouble() {
        Assert.assertEquals(-1, NumberUtils.compare(1.0d, 2.0d));
        Assert.assertEquals(1, NumberUtils.compare(2.0d, 1.0d));
        Assert.assertEquals(0, NumberUtils.compare(1.0d, 1.0d));
        Assert.assertEquals(0, NumberUtils.compare(Double.NaN, Double.NaN));
        Assert.assertEquals(-1, NumberUtils.compare(-0.0d, 0.0d));
        Assert.assertEquals(1, NumberUtils.compare(0.0d, -0.0d));
        Assert.assertEquals(1, NumberUtils.compare(Double.NaN, Double.POSITIVE_INFINITY));
        Assert.assertEquals(-1, NumberUtils.compare(Double.POSITIVE_INFINITY, Double.NaN));
    }

    @Test
    public void testCompareFloat() {
        Assert.assertEquals(-1, NumberUtils.compare(1.0f, 2.0f));
        Assert.assertEquals(1, NumberUtils.compare(2.0f, 1.0f));
        Assert.assertEquals(0, NumberUtils.compare(1.0f, 1.0f));
        Assert.assertEquals(0, NumberUtils.compare(Float.NaN, Float.NaN));
        Assert.assertEquals(-1, NumberUtils.compare(-0.0f, 0.0f));
        Assert.assertEquals(1, NumberUtils.compare(0.0f, -0.0f));
        Assert.assertEquals(1, NumberUtils.compare(Float.NaN, Float.POSITIVE_INFINITY));
        Assert.assertEquals(-1, NumberUtils.compare(Float.POSITIVE_INFINITY, Float.NaN));
    }

    @Test
    public void testIsDigits() {
        Assert.assertFalse(NumberUtils.isDigits(null));
        Assert.assertFalse(NumberUtils.isDigits(""));
        Assert.assertTrue(NumberUtils.isDigits("12345"));
        Assert.assertFalse(NumberUtils.isDigits("123a45"));
        Assert.assertFalse(NumberUtils.isDigits("-123"));
    }

    @Test
    public void testIsNumber() {
        Assert.assertFalse(NumberUtils.isNumber(null));
        Assert.assertFalse(NumberUtils.isNumber(""));
        Assert.assertFalse(NumberUtils.isNumber("   "));
        Assert.assertTrue(NumberUtils.isNumber("123"));
        Assert.assertTrue(NumberUtils.isNumber("-123"));
        Assert.assertTrue(NumberUtils.isNumber("123.45"));
        Assert.assertTrue(NumberUtils.isNumber("-123.45"));
        Assert.assertTrue(NumberUtils.isNumber(".45"));
        Assert.assertTrue(NumberUtils.isNumber("45."));
        Assert.assertTrue(NumberUtils.isNumber("1e5"));
        Assert.assertTrue(NumberUtils.isNumber("1E5"));
        Assert.assertTrue(NumberUtils.isNumber("1e+5"));
        Assert.assertTrue(NumberUtils.isNumber("1e-5"));
        Assert.assertTrue(NumberUtils.isNumber("1.2e5"));
        Assert.assertTrue(NumberUtils.isNumber("123L"));
        Assert.assertTrue(NumberUtils.isNumber("123l"));
        Assert.assertTrue(NumberUtils.isNumber("123.4f"));
        Assert.assertTrue(NumberUtils.isNumber("123.4F"));
        Assert.assertTrue(NumberUtils.isNumber("123.4d"));
        Assert.assertTrue(NumberUtils.isNumber("123.4D"));
        Assert.assertTrue(NumberUtils.isNumber("0x1234"));
        Assert.assertTrue(NumberUtils.isNumber("0xABCD"));
        Assert.assertTrue(NumberUtils.isNumber("0xabcd"));
        Assert.assertTrue(NumberUtils.isNumber("-0x1234"));
        Assert.assertFalse(NumberUtils.isNumber("0x"));
        Assert.assertFalse(NumberUtils.isNumber("-0x"));
        Assert.assertFalse(NumberUtils.isNumber("0x123G"));
        Assert.assertFalse(NumberUtils.isNumber("123e"));
        Assert.assertFalse(NumberUtils.isNumber("123E"));
        Assert.assertFalse(NumberUtils.isNumber("123e+"));
        Assert.assertFalse(NumberUtils.isNumber("123e-"));
        Assert.assertFalse(NumberUtils.isNumber("123ee"));
        Assert.assertFalse(NumberUtils.isNumber("123.4.5"));
        Assert.assertFalse(NumberUtils.isNumber("123.4e5.6"));
        Assert.assertFalse(NumberUtils.isNumber("123e5L"));
        Assert.assertFalse(NumberUtils.isNumber("123e5l"));
        Assert.assertTrue(NumberUtils.isNumber("123e5f"));
        Assert.assertTrue(NumberUtils.isNumber("123e5F"));
        Assert.assertTrue(NumberUtils.isNumber("123e5d"));
        Assert.assertTrue(NumberUtils.isNumber("123e5D"));
        Assert.assertFalse(NumberUtils.isNumber("."));
        Assert.assertFalse(NumberUtils.isNumber("1a2"));
        Assert.assertFalse(NumberUtils.isNumber("e1"));
        Assert.assertFalse(NumberUtils.isNumber("+123"));
        Assert.assertFalse(NumberUtils.isNumber("123-"));
        Assert.assertFalse(NumberUtils.isNumber("123+"));
        Assert.assertFalse(NumberUtils.isNumber("123z"));
    }
}
