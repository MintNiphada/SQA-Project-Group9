package org.apache.commons.lang3.math;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.Assert;
import org.junit.Test;

public class NumberUtilsTest {

    @Test
    public void testConstructor() {
        Assert.assertNotNull(new NumberUtils());
    }

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
    public void testToInt() {
        Assert.assertEquals(0, NumberUtils.toInt(null));
        Assert.assertEquals(0, NumberUtils.toInt(""));
        Assert.assertEquals(0, NumberUtils.toInt("abc"));
        Assert.assertEquals(123, NumberUtils.toInt("123"));
        Assert.assertEquals(-123, NumberUtils.toInt("-123"));
        Assert.assertEquals(5, NumberUtils.toInt(null, 5));
        Assert.assertEquals(5, NumberUtils.toInt("invalid", 5));
        Assert.assertEquals(10, NumberUtils.toInt("10", 5));
    }

    @Test
    public void testToLong() {
        Assert.assertEquals(0L, NumberUtils.toLong(null));
        Assert.assertEquals(0L, NumberUtils.toLong(""));
        Assert.assertEquals(0L, NumberUtils.toLong("abc"));
        Assert.assertEquals(1234567890123L, NumberUtils.toLong("1234567890123"));
        Assert.assertEquals(-1234567890123L, NumberUtils.toLong("-1234567890123"));
        Assert.assertEquals(5L, NumberUtils.toLong(null, 5L));
        Assert.assertEquals(5L, NumberUtils.toLong("invalid", 5L));
        Assert.assertEquals(10L, NumberUtils.toLong("10", 5L));
    }

    @Test
    public void testToFloat() {
        Assert.assertEquals(0.0f, NumberUtils.toFloat(null), 0.0001f);
        Assert.assertEquals(0.0f, NumberUtils.toFloat(""), 0.0001f);
        Assert.assertEquals(0.0f, NumberUtils.toFloat("abc"), 0.0001f);
        Assert.assertEquals(1.23f, NumberUtils.toFloat("1.23"), 0.0001f);
        Assert.assertEquals(-1.23f, NumberUtils.toFloat("-1.23"), 0.0001f);
        Assert.assertEquals(5.5f, NumberUtils.toFloat(null, 5.5f), 0.0001f);
        Assert.assertEquals(5.5f, NumberUtils.toFloat("invalid", 5.5f), 0.0001f);
        Assert.assertEquals(1.5f, NumberUtils.toFloat("1.5", 5.5f), 0.0001f);
    }

    @Test
    public void testToDouble() {
        Assert.assertEquals(0.0d, NumberUtils.toDouble(null), 0.0001d);
        Assert.assertEquals(0.0d, NumberUtils.toDouble(""), 0.0001d);
        Assert.assertEquals(0.0d, NumberUtils.toDouble("abc"), 0.0001d);
        Assert.assertEquals(1.2345d, NumberUtils.toDouble("1.2345"), 0.0001d);
        Assert.assertEquals(-1.2345d, NumberUtils.toDouble("-1.2345"), 0.0001d);
        Assert.assertEquals(5.5d, NumberUtils.toDouble(null, 5.5d), 0.0001d);
        Assert.assertEquals(5.5d, NumberUtils.toDouble("invalid", 5.5d), 0.0001d);
        Assert.assertEquals(1.5d, NumberUtils.toDouble("1.5", 5.5d), 0.0001d);
    }

    @Test
    public void testToByte() {
        Assert.assertEquals((byte) 0, NumberUtils.toByte(null));
        Assert.assertEquals((byte) 0, NumberUtils.toByte(""));
        Assert.assertEquals((byte) 0, NumberUtils.toByte("abc"));
        Assert.assertEquals((byte) 123, NumberUtils.toByte("123"));
        Assert.assertEquals((byte) -123, NumberUtils.toByte("-123"));
        Assert.assertEquals((byte) 5, NumberUtils.toByte(null, (byte) 5));
        Assert.assertEquals((byte) 5, NumberUtils.toByte("invalid", (byte) 5));
        Assert.assertEquals((byte) 10, NumberUtils.toByte("10", (byte) 5));
    }

    @Test
    public void testToShort() {
        Assert.assertEquals((short) 0, NumberUtils.toShort(null));
        Assert.assertEquals((short) 0, NumberUtils.toShort(""));
        Assert.assertEquals((short) 0, NumberUtils.toShort("abc"));
        Assert.assertEquals((short) 12345, NumberUtils.toShort("12345"));
        Assert.assertEquals((short) -12345, NumberUtils.toShort("-12345"));
        Assert.assertEquals((short) 5, NumberUtils.toShort(null, (short) 5));
        Assert.assertEquals((short) 5, NumberUtils.toShort("invalid", (short) 5));
        Assert.assertEquals((short) 10, NumberUtils.toShort("10", (short) 5));
    }

    @Test
    public void testCreateFloat() {
        Assert.assertNull(NumberUtils.createFloat(null));
        Assert.assertEquals(Float.valueOf(1.23f), NumberUtils.createFloat("1.23"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateFloatInvalid() {
        NumberUtils.createFloat("abc");
    }

    @Test
    public void testCreateDouble() {
        Assert.assertNull(NumberUtils.createDouble(null));
        Assert.assertEquals(Double.valueOf(1.23d), NumberUtils.createDouble("1.23"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateDoubleInvalid() {
        NumberUtils.createDouble("abc");
    }

    @Test
    public void testCreateInteger() {
        Assert.assertNull(NumberUtils.createInteger(null));
        Assert.assertEquals(Integer.valueOf(123), NumberUtils.createInteger("123"));
        Assert.assertEquals(Integer.valueOf(123), NumberUtils.createInteger("0x7B"));
        Assert.assertEquals(Integer.valueOf(-123), NumberUtils.createInteger("-0x7B"));
        Assert.assertEquals(Integer.valueOf(8), NumberUtils.createInteger("010"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateIntegerInvalid() {
        NumberUtils.createInteger("abc");
    }

    @Test
    public void testCreateLong() {
        Assert.assertNull(NumberUtils.createLong(null));
        Assert.assertEquals(Long.valueOf(1234567890123L), NumberUtils.createLong("1234567890123"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateLongInvalid() {
        NumberUtils.createLong("abc");
    }

    @Test
    public void testCreateBigInteger() {
        Assert.assertNull(NumberUtils.createBigInteger(null));
        Assert.assertEquals(new BigInteger("123456789012345678901234567890"), NumberUtils.createBigInteger("123456789012345678901234567890"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigIntegerInvalid() {
        NumberUtils.createBigInteger("abc");
    }

    @Test
    public void testCreateBigDecimal() {
        Assert.assertNull(NumberUtils.createBigDecimal(null));
        Assert.assertEquals(new BigDecimal("12345678901234567890.123456789"), NumberUtils.createBigDecimal("12345678901234567890.123456789"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigDecimalBlank() {
        NumberUtils.createBigDecimal("   ");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigDecimalEmpty() {
        NumberUtils.createBigDecimal("");
    }

    @Test
    public void testCreateNumber() {
        Assert.assertNull(NumberUtils.createNumber(null));
        Assert.assertNull(NumberUtils.createNumber("--123"));

        Assert.assertEquals(Integer.valueOf(123), NumberUtils.createNumber("0x7B"));
        Assert.assertEquals(Integer.valueOf(-123), NumberUtils.createNumber("-0x7B"));
        Assert.assertEquals(Integer.valueOf(123), NumberUtils.createNumber("123"));
        Assert.assertEquals(Long.valueOf(1234567890123L), NumberUtils.createNumber("1234567890123"));
        Assert.assertEquals(new BigInteger("123456789012345678901234567890"), NumberUtils.createNumber("123456789012345678901234567890"));

        Assert.assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123l"));
        Assert.assertEquals(Long.valueOf(-123L), NumberUtils.createNumber("-123L"));
        Assert.assertEquals(new BigInteger("123456789012345678901234567890"), NumberUtils.createNumber("123456789012345678901234567890L"));

        Assert.assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23f"));
        Assert.assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23F"));
        Assert.assertEquals(Float.valueOf(0.0f), NumberUtils.createNumber("0.0f"));
        Assert.assertEquals(Float.valueOf(0.0f), NumberUtils.createNumber("00.00F"));
        Assert.assertEquals(Double.valueOf(1e200), (Double) NumberUtils.createNumber("1e200f"), 0.0001);
        Assert.assertEquals(Double.valueOf(1.23d), NumberUtils.createNumber("1.23d"));
        Assert.assertEquals(Double.valueOf(1.23d), NumberUtils.createNumber("1.23D"));
        Assert.assertEquals(Double.valueOf(0.0d), NumberUtils.createNumber("0.0d"));
        Assert.assertEquals(new BigDecimal("1e400"), NumberUtils.createNumber("1e400d"));

        Assert.assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23"));
        Assert.assertEquals(Float.valueOf(0.0f), NumberUtils.createNumber("0.0"));
        Assert.assertEquals(Float.valueOf(1.2e3f), NumberUtils.createNumber("1.2e3"));
        Assert.assertEquals(Float.valueOf(1.2E3f), NumberUtils.createNumber("1.2E3"));
        Assert.assertEquals(Double.valueOf(1.2e50), (Double) NumberUtils.createNumber("1.2e50"), 0.0001);
        Assert.assertEquals(new BigDecimal("1.2e500"), NumberUtils.createNumber("1.2e500"));
        Assert.assertEquals(new BigDecimal("0.00000000000000000000000000000000000000000000000001"), 
                NumberUtils.createNumber("0.00000000000000000000000000000000000000000000000001f"));
        Assert.assertEquals(new BigDecimal("0.000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001"), 
                NumberUtils.createNumber("0.000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001d"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberBlank() {
        NumberUtils.createNumber("   ");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberEmpty() {
        NumberUtils.createNumber("");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidExp1() {
        NumberUtils.createNumber("1.2e3e4");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidExp2() {
        NumberUtils.createNumber("1e2.3");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidExp3() {
        NumberUtils.createNumber("1e");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidLong() {
        NumberUtils.createNumber("1.2L");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidLong2() {
        NumberUtils.createNumber("1e2L");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidLong3() {
        NumberUtils.createNumber("abcL");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidChar() {
        NumberUtils.createNumber("123a");
    }

    @Test
    public void testMinLongArray() {
        Assert.assertEquals(1L, NumberUtils.min(new long[]{3L, 1L, 2L}));
        Assert.assertEquals(-5L, NumberUtils.min(new long[]{3L, -5L, 2L}));
        Assert.assertEquals(7L, NumberUtils.min(new long[]{7L}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinLongArrayNull() {
        NumberUtils.min((long[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinLongArrayEmpty() {
        NumberUtils.min(new long[0]);
    }

    @Test
    public void testMinIntArray() {
        Assert.assertEquals(1, NumberUtils.min(new int[]{3, 1, 2}));
        Assert.assertEquals(-5, NumberUtils.min(new int[]{3, -5, 2}));
        Assert.assertEquals(7, NumberUtils.min(new int[]{7}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinIntArrayNull() {
        NumberUtils.min((int[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinIntArrayEmpty() {
        NumberUtils.min(new int[0]);
    }

    @Test
    public void testMinShortArray() {
        Assert.assertEquals((short) 1, NumberUtils.min(new short[]{3, 1, 2}));
        Assert.assertEquals((short) -5, NumberUtils.min(new short[]{3, -5, 2}));
        Assert.assertEquals((short) 7, NumberUtils.min(new short[]{7}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinShortArrayNull() {
        NumberUtils.min((short[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinShortArrayEmpty() {
        NumberUtils.min(new short[0]);
    }

    @Test
    public void testMinByteArray() {
        Assert.assertEquals((byte) 1, NumberUtils.min(new byte[]{3, 1, 2}));
        Assert.assertEquals((byte) -5, NumberUtils.min(new byte[]{3, -5, 2}));
        Assert.assertEquals((byte) 7, NumberUtils.min(new byte[]{7}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinByteArrayNull() {
        NumberUtils.min((byte[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinByteArrayEmpty() {
        NumberUtils.min(new byte[0]);
    }

    @Test
    public void testMinDoubleArray() {
        Assert.assertEquals(1.2d, NumberUtils.min(new double[]{3.4d, 1.2d, 2.3d}), 0.0001d);
        Assert.assertEquals(-5.6d, NumberUtils.min(new double[]{3.4d, -5.6d, 2.3d}), 0.0001d);
        Assert.assertEquals(7.8d, NumberUtils.min(new double[]{7.8d}), 0.0001d);
        Assert.assertTrue(Double.isNaN(NumberUtils.min(new double[]{3.4d, Double.NaN, 2.3d})));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinDoubleArrayNull() {
        NumberUtils.min((double[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinDoubleArrayEmpty() {
        NumberUtils.min(new double[0]);
    }

    @Test
    public void testMinFloatArray() {
        Assert.assertEquals(1.2f, NumberUtils.min(new float[]{3.4f, 1.2f, 2.3f}), 0.0001f);
        Assert.assertEquals(-5.6f, NumberUtils.min(new float[]{3.4f, -5.6f, 2.3f}), 0.0001f);
        Assert.assertEquals(7.8f, NumberUtils.min(new float[]{7.8f}), 0.0001f);
        Assert.assertTrue(Float.isNaN(NumberUtils.min(new float[]{3.4f, Float.NaN, 2.3f})));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinFloatArrayNull() {
        NumberUtils.min((float[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinFloatArrayEmpty() {
        NumberUtils.min(new float[0]);
    }

    @Test
    public void testMaxLongArray() {
        Assert.assertEquals(3L, NumberUtils.max(new long[]{1L, 3L, 2L}));
        Assert.assertEquals(5L, NumberUtils.max(new long[]{-3L, 5L, 2L}));
        Assert.assertEquals(7L, NumberUtils.max(new long[]{7L}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxLongArrayNull() {
        NumberUtils.max((long[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxLongArrayEmpty() {
        NumberUtils.max(new long[0]);
    }

    @Test
    public void testMaxIntArray() {
        Assert.assertEquals(3, NumberUtils.max(new int[]{1, 3, 2}));
        Assert.assertEquals(5, NumberUtils.max(new int[]{-3, 5, 2}));
        Assert.assertEquals(7, NumberUtils.max(new int[]{7}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxIntArrayNull() {
        NumberUtils.max((int[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxIntArrayEmpty() {
        NumberUtils.max(new int[0]);
    }

    @Test
    public void testMaxShortArray() {
        Assert.assertEquals((short) 3, NumberUtils.max(new short[]{1, 3, 2}));
        Assert.assertEquals((short) 5, NumberUtils.max(new short[]{-3, 5, 2}));
        Assert.assertEquals((short) 7, NumberUtils.max(new short[]{7}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxShortArrayNull() {
        NumberUtils.max((short[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxShortArrayEmpty() {
        NumberUtils.max(new short[0]);
    }

    @Test
    public void testMaxByteArray() {
        Assert.assertEquals((byte) 3, NumberUtils.max(new byte[]{1, 3, 2}));
        Assert.assertEquals((byte) 5, NumberUtils.max(new byte[]{-3, 5, 2}));
        Assert.assertEquals((byte) 7, NumberUtils.max(new byte[]{7}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxByteArrayNull() {
        NumberUtils.max((byte[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxByteArrayEmpty() {
        NumberUtils.max(new byte[0]);
    }

    @Test
    public void testMaxDoubleArray() {
        Assert.assertEquals(3.4d, NumberUtils.max(new double[]{1.2d, 3.4d, 2.3d}), 0.0001d);
        Assert.assertEquals(5.6d, NumberUtils.max(new double[]{-3.4d, 5.6d, 2.3d}), 0.0001d);
        Assert.assertEquals(7.8d, NumberUtils.max(new double[]{7.8d}), 0.0001d);
        Assert.assertTrue(Double.isNaN(NumberUtils.max(new double[]{3.4d, Double.NaN, 2.3d})));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxDoubleArrayNull() {
        NumberUtils.max((double[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxDoubleArrayEmpty() {
        NumberUtils.max(new double[0]);
    }

    @Test
    public void testMaxFloatArray() {
        Assert.assertEquals(3.4f, NumberUtils.max(new float[]{1.2f, 3.4f, 2.3f}), 0.0001f);
        Assert.assertEquals(5.6f, NumberUtils.max(new float[]{-3.4f, 5.6f, 2.3f}), 0.0001f);
        Assert.assertEquals(7.8f, NumberUtils.max(new float[]{7.8f}), 0.0001f);
        Assert.assertTrue(Float.isNaN(NumberUtils.max(new float[]{3.4f, Float.NaN, 2.3f})));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxFloatArrayNull() {
        NumberUtils.max((float[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxFloatArrayEmpty() {
        NumberUtils.max(new float[0]);
    }

    @Test
    public void testMin3Values() {
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
        Assert.assertTrue(Double.isNaN(NumberUtils.min(1.0d, Double.NaN, 3.0d)));

        Assert.assertEquals(1.0f, NumberUtils.min(1.0f, 2.0f, 3.0f), 0.0001f);
        Assert.assertEquals(1.0f, NumberUtils.min(2.0f, 1.0f, 3.0f), 0.0001f);
        Assert.assertEquals(1.0f, NumberUtils.min(3.0f, 2.0f, 1.0f), 0.0001f);
        Assert.assertTrue(Float.isNaN(NumberUtils.min(1.0f, Float.NaN, 3.0f)));
    }

    @Test
    public void testMax3Values() {
        Assert.assertEquals(3L, NumberUtils.max(1L, 2L, 3L));
        Assert.assertEquals(3L, NumberUtils.max(2L, 3L, 1L));
        Assert.assertEquals(3L, NumberUtils.max(3L, 2L, 1L));

        Assert.assertEquals(3, NumberUtils.max(1, 2, 3));
        Assert.assertEquals(3, NumberUtils.max(2, 3, 1));
        Assert.assertEquals(3, NumberUtils.max(3, 2, 1));

        Assert.assertEquals((short) 3, NumberUtils.max((short) 1, (short) 2, (short) 3));
        Assert.assertEquals((short) 3, NumberUtils.max((short) 2, (short) 3, (short) 1));
        Assert.assertEquals((short) 3, NumberUtils.max((short) 3, (short) 2, (short) 1));

        Assert.assertEquals((byte) 3, NumberUtils.max((byte) 1, (byte) 2, (byte) 3));
        Assert.assertEquals((byte) 3, NumberUtils.max((byte) 2, (byte) 3, (byte) 1));
        Assert.assertEquals((byte) 3, NumberUtils.max((byte) 3, (byte) 2, (byte) 1));

        Assert.assertEquals(3.0d, NumberUtils.max(1.0d, 2.0d, 3.0d), 0.0001d);
        Assert.assertEquals(3.0d, NumberUtils.max(2.0d, 3.0d, 1.0d), 0.0001d);
        Assert.assertEquals(3.0d, NumberUtils.max(3.0d, 2.0d, 1.0d), 0.0001d);
        Assert.assertTrue(Double.isNaN(NumberUtils.max(1.0d, Double.NaN, 3.0d)));

        Assert.assertEquals(3.0f, NumberUtils.max(1.0f, 2.0f, 3.0f), 0.0001f);
        Assert.assertEquals(3.0f, NumberUtils.max(2.0f, 3.0f, 1.0f), 0.0001f);
        Assert.assertEquals(3.0f, NumberUtils.max(3.0f, 2.0f, 1.0f), 0.0001f);
        Assert.assertTrue(Float.isNaN(NumberUtils.max(1.0f, Float.NaN, 3.0f)));
    }

    @Test
    public void testIsDigits() {
        Assert.assertFalse(NumberUtils.isDigits(null));
        Assert.assertFalse(NumberUtils.isDigits(""));
        Assert.assertFalse(NumberUtils.isDigits("123a"));
        Assert.assertFalse(NumberUtils.isDigits("12.3"));
        Assert.assertFalse(NumberUtils.isDigits("-123"));
        Assert.assertTrue(NumberUtils.isDigits("12345"));
        Assert.assertTrue(NumberUtils.isDigits("0"));
    }

    @Test
    public void testIsNumber() {
        Assert.assertFalse(NumberUtils.isNumber(null));
        Assert.assertFalse(NumberUtils.isNumber(""));
        Assert.assertFalse(NumberUtils.isNumber("   "));
        Assert.assertFalse(NumberUtils.isNumber("0x"));
        Assert.assertFalse(NumberUtils.isNumber("-0x"));
        Assert.assertFalse(NumberUtils.isNumber("0xxyz"));
        Assert.assertTrue(NumberUtils.isNumber("0x1A2B"));
        Assert.assertTrue(NumberUtils.isNumber("-0x1a2b"));
        Assert.assertTrue(NumberUtils.isNumber("0x0"));

        Assert.assertTrue(NumberUtils.isNumber("12345"));
        Assert.assertTrue(NumberUtils.isNumber("-12345"));
        Assert.assertTrue(NumberUtils.isNumber("+12345") == false); // + not handled at beginning without e/E
        Assert.assertTrue(NumberUtils.isNumber("12.345"));
        Assert.assertTrue(NumberUtils.isNumber("-12.345"));
        Assert.assertTrue(NumberUtils.isNumber(".12345"));
        Assert.assertTrue(NumberUtils.isNumber("12345."));
        Assert.assertFalse(NumberUtils.isNumber("."));
        Assert.assertFalse(NumberUtils.isNumber("12.34.56"));

        Assert.assertTrue(NumberUtils.isNumber("123e4"));
        Assert.assertTrue(NumberUtils.isNumber("123E4"));
        Assert.assertTrue(NumberUtils.isNumber("123e+4"));
        Assert.assertTrue(NumberUtils.isNumber("123e-4"));
        Assert.assertTrue(NumberUtils.isNumber("-123.45e-6"));
        Assert.assertFalse(NumberUtils.isNumber("123e"));
        Assert.assertFalse(NumberUtils.isNumber("123e-"));
        Assert.assertFalse(NumberUtils.isNumber("123e+"));
        Assert.assertFalse(NumberUtils.isNumber("123e4e5"));
        Assert.assertFalse(NumberUtils.isNumber("123e4.5"));
        Assert.assertFalse(NumberUtils.isNumber("e123"));
        Assert.assertFalse(NumberUtils.isNumber("123+4"));

        Assert.assertTrue(NumberUtils.isNumber("12345L"));
        Assert.assertTrue(NumberUtils.isNumber("12345l"));
        Assert.assertTrue(NumberUtils.isNumber("-12345L"));
        Assert.assertFalse(NumberUtils.isNumber("123.45L"));
        Assert.assertFalse(NumberUtils.isNumber("123e4L"));

        Assert.assertTrue(NumberUtils.isNumber("12345f"));
        Assert.assertTrue(NumberUtils.isNumber("12345F"));
        Assert.assertTrue(NumberUtils.isNumber("123.45f"));
        Assert.assertTrue(NumberUtils.isNumber("123e4f"));

        Assert.assertTrue(NumberUtils.isNumber("12345d"));
        Assert.assertTrue(NumberUtils.isNumber("12345D"));
        Assert.assertTrue(NumberUtils.isNumber("123.45d"));
        Assert.assertTrue(NumberUtils.isNumber("123e4d"));

        Assert.assertFalse(NumberUtils.isNumber("12345a"));
        Assert.assertFalse(NumberUtils.isNumber("123e4a"));
        Assert.assertFalse(NumberUtils.isNumber("--123"));
    }
}
