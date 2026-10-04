package org.apache.commons.lang3.math;

import org.junit.Test;
import static org.junit.Assert.*;

public class NumberUtilsTest {

    @Test
    public void testConstants() {
        assertEquals(Long.valueOf(0L), NumberUtils.LONG_ZERO);
        assertEquals(Long.valueOf(1L), NumberUtils.LONG_ONE);
        assertEquals(Long.valueOf(-1L), NumberUtils.LONG_MINUS_ONE);
        assertEquals(Integer.valueOf(0), NumberUtils.INTEGER_ZERO);
        assertEquals(Integer.valueOf(1), NumberUtils.INTEGER_ONE);
        assertEquals(Integer.valueOf(-1), NumberUtils.INTEGER_MINUS_ONE);
        assertEquals(Short.valueOf((short)0), NumberUtils.SHORT_ZERO);
        assertEquals(Short.valueOf((short)1), NumberUtils.SHORT_ONE);
        assertEquals(Short.valueOf((short)-1), NumberUtils.SHORT_MINUS_ONE);
        assertEquals(Byte.valueOf((byte)0), NumberUtils.BYTE_ZERO);
        assertEquals(Byte.valueOf((byte)1), NumberUtils.BYTE_ONE);
        assertEquals(Byte.valueOf((byte)-1), NumberUtils.BYTE_MINUS_ONE);
        assertEquals(Double.valueOf(0.0d), NumberUtils.DOUBLE_ZERO);
        assertEquals(Double.valueOf(1.0d), NumberUtils.DOUBLE_ONE);
        assertEquals(Double.valueOf(-1.0d), NumberUtils.DOUBLE_MINUS_ONE);
        assertEquals(Float.valueOf(0.0f), NumberUtils.FLOAT_ZERO);
        assertEquals(Float.valueOf(1.0f), NumberUtils.FLOAT_ONE);
        assertEquals(Float.valueOf(-1.0f), NumberUtils.FLOAT_MINUS_ONE);
    }

    @Test
    public void testConstructor() {
        new NumberUtils();
    }

    @Test
    public void testToIntString() {
        assertEquals(0, NumberUtils.toInt(null));
        assertEquals(0, NumberUtils.toInt(""));
        assertEquals(1, NumberUtils.toInt("1"));
        assertEquals(0, NumberUtils.toInt("abc"));
    }

    @Test
    public void testToIntStringInt() {
        assertEquals(5, NumberUtils.toInt(null, 5));
        assertEquals(5, NumberUtils.toInt("", 5));
        assertEquals(1, NumberUtils.toInt("1", 5));
        assertEquals(5, NumberUtils.toInt("abc", 5));
    }

    @Test
    public void testToLongString() {
        assertEquals(0L, NumberUtils.toLong(null));
        assertEquals(0L, NumberUtils.toLong(""));
        assertEquals(1L, NumberUtils.toLong("1"));
        assertEquals(0L, NumberUtils.toLong("abc"));
    }

    @Test
    public void testToLongStringLong() {
        assertEquals(5L, NumberUtils.toLong(null, 5L));
        assertEquals(5L, NumberUtils.toLong("", 5L));
        assertEquals(1L, NumberUtils.toLong("1", 5L));
        assertEquals(5L, NumberUtils.toLong("abc", 5L));
    }

    @Test
    public void testToFloatString() {
        assertEquals(0.0f, NumberUtils.toFloat(null), 0.0f);
        assertEquals(0.0f, NumberUtils.toFloat(""), 0.0f);
        assertEquals(1.5f, NumberUtils.toFloat("1.5"), 0.0f);
        assertEquals(0.0f, NumberUtils.toFloat("abc"), 0.0f);
    }

    @Test
    public void testToFloatStringFloat() {
        assertEquals(1.1f, NumberUtils.toFloat(null, 1.1f), 0.0f);
        assertEquals(1.1f, NumberUtils.toFloat("", 1.1f), 0.0f);
        assertEquals(1.5f, NumberUtils.toFloat("1.5", 0.0f), 0.0f);
        assertEquals(1.1f, NumberUtils.toFloat("abc", 1.1f), 0.0f);
    }

    @Test
    public void testToDoubleString() {
        assertEquals(0.0d, NumberUtils.toDouble(null), 0.0d);
        assertEquals(0.0d, NumberUtils.toDouble(""), 0.0d);
        assertEquals(1.5d, NumberUtils.toDouble("1.5"), 0.0d);
        assertEquals(0.0d, NumberUtils.toDouble("abc"), 0.0d);
    }

    @Test
    public void testToDoubleStringDouble() {
        assertEquals(1.1d, NumberUtils.toDouble(null, 1.1d), 0.0d);
        assertEquals(1.1d, NumberUtils.toDouble("", 1.1d), 0.0d);
        assertEquals(1.5d, NumberUtils.toDouble("1.5", 0.0d), 0.0d);
        assertEquals(1.1d, NumberUtils.toDouble("abc", 1.1d), 0.0d);
    }

    @Test
    public void testToByteString() {
        assertEquals(0, NumberUtils.toByte(null));
        assertEquals(0, NumberUtils.toByte(""));
        assertEquals(1, NumberUtils.toByte("1"));
        assertEquals(0, NumberUtils.toByte("abc"));
    }

    @Test
    public void testToByteStringByte() {
        assertEquals(1, NumberUtils.toByte(null, (byte)1));
        assertEquals(1, NumberUtils.toByte("", (byte)1));
        assertEquals(1, NumberUtils.toByte("1", (byte)0));
        assertEquals(1, NumberUtils.toByte("abc", (byte)1));
    }

    @Test
    public void testToShortString() {
        assertEquals(0, NumberUtils.toShort(null));
        assertEquals(0, NumberUtils.toShort(""));
        assertEquals(1, NumberUtils.toShort("1"));
        assertEquals(0, NumberUtils.toShort("abc"));
    }

    @Test
    public void testToShortStringShort() {
        assertEquals(1, NumberUtils.toShort(null, (short)1));
        assertEquals(1, NumberUtils.toShort("", (short)1));
        assertEquals(1, NumberUtils.toShort("1", (short)0));
        assertEquals(1, NumberUtils.toShort("abc", (short)1));
    }

    @Test
    public void testCreateNumberNull() {
        assertNull(NumberUtils.createNumber(null));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberBlank() {
        NumberUtils.createNumber("");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberBlankSpaces() {
        NumberUtils.createNumber("   ");
    }

    @Test
    public void testCreateNumberDoubleDash() {
        assertNull(NumberUtils.createNumber("--123"));
    }

    @Test
    public void testCreateNumberHex() {
        assertEquals(Integer.valueOf(0x1A), NumberUtils.createNumber("0x1A"));
        assertEquals(Integer.valueOf(-0x1A), NumberUtils.createNumber("-0x1A"));
    }

    @Test
    public void testCreateNumberInteger() {
        assertEquals(Integer.valueOf(123), NumberUtils.createNumber("123"));
        assertEquals(Integer.valueOf(-123), NumberUtils.createNumber("-123"));
    }

    @Test
    public void testCreateNumberLong() {
        assertEquals(Long.valueOf(1234567890123L), NumberUtils.createNumber("1234567890123"));
    }

    @Test
    public void testCreateNumberBigInteger() {
        assertEquals(new java.math.BigInteger("12345678901234567890"), NumberUtils.createNumber("12345678901234567890"));
    }

    @Test
    public void testCreateNumberFloat() {
        assertEquals(Float.valueOf(1.5f), NumberUtils.createNumber("1.5f"));
        assertEquals(Float.valueOf(1.5f), NumberUtils.createNumber("1.5F"));
    }

    @Test
    public void testCreateNumberDouble() {
        assertEquals(Double.valueOf(1.5d), NumberUtils.createNumber("1.5d"));
        assertEquals(Double.valueOf(1.5d), NumberUtils.createNumber("1.5D"));
    }

    @Test
    public void testCreateNumberBigDecimal() {
        assertEquals(new java.math.BigDecimal("1.5"), NumberUtils.createNumber("1.5"));
    }

    @Test
    public void testCreateNumberScientific() {
        assertEquals(Float.valueOf(1.5e2f), NumberUtils.createNumber("1.5e2f"));
        assertEquals(Double.valueOf(1.5e2), NumberUtils.createNumber("1.5e2"));
    }

    @Test
    public void testCreateNumberLongQualifier() {
        assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123L"));
        assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123l"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidLWithDecimal() {
        NumberUtils.createNumber("1.5L");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidLWithExponent() {
        NumberUtils.createNumber("1e2L");
    }

    @Test
    public void testCreateNumberFloatPrecisionLoss() {
        assertEquals(new java.math.BigDecimal("1.1"), NumberUtils.createNumber("1.1f"));
    }

    @Test
    public void testCreateNumberDoublePrecisionLoss() {
        assertEquals(new java.math.BigDecimal("1.1"), NumberUtils.createNumber("1.1d"));
    }

    @Test
    public void testCreateNumberAllZeros() {
        assertEquals(Float.valueOf(0.0f), NumberUtils.createNumber("0.0f"));
        assertEquals(Double.valueOf(0.0d), NumberUtils.createNumber("0.0d"));
    }

    @Test
    public void testCreateNumberInvalidExpPosition() {
        try {
            NumberUtils.createNumber("1e2.3");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
        }
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidTrailingChar() {
        NumberUtils.createNumber("123x");
    }

    @Test
    public void testCreateFloat() {
        assertNull(NumberUtils.createFloat(null));
        assertEquals(Float.valueOf(1.5f), NumberUtils.createFloat("1.5"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateFloatInvalid() {
        NumberUtils.createFloat("abc");
    }

    @Test
    public void testCreateDouble() {
        assertNull(NumberUtils.createDouble(null));
        assertEquals(Double.valueOf(1.5d), NumberUtils.createDouble("1.5"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateDoubleInvalid() {
        NumberUtils.createDouble("abc");
    }

    @Test
    public void testCreateInteger() {
        assertNull(NumberUtils.createInteger(null));
        assertEquals(Integer.valueOf(123), NumberUtils.createInteger("123"));
        assertEquals(Integer.valueOf(0x1A), NumberUtils.createInteger("0x1A"));
        assertEquals(Integer.valueOf(0777), NumberUtils.createInteger("0777"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateIntegerInvalid() {
        NumberUtils.createInteger("abc");
    }

    @Test
    public void testCreateLong() {
        assertNull(NumberUtils.createLong(null));
        assertEquals(Long.valueOf(123L), NumberUtils.createLong("123"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateLongInvalid() {
        NumberUtils.createLong("abc");
    }

    @Test
    public void testCreateBigInteger() {
        assertNull(NumberUtils.createBigInteger(null));
        assertEquals(new java.math.BigInteger("123"), NumberUtils.createBigInteger("123"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigIntegerInvalid() {
        NumberUtils.createBigInteger("abc");
    }

    @Test
    public void testCreateBigDecimal() {
        assertNull(NumberUtils.createBigDecimal(null));
        assertEquals(new java.math.BigDecimal("1.5"), NumberUtils.createBigDecimal("1.5"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigDecimalBlank() {
        NumberUtils.createBigDecimal("");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigDecimalInvalid() {
        NumberUtils.createBigDecimal("abc");
    }

    @Test
    public void testMinLongArray() {
        assertEquals(1L, NumberUtils.min(new long[]{1L, 2L, 3L}));
        assertEquals(-3L, NumberUtils.min(new long[]{-1L, -2L, -3L}));
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
        assertEquals(1, NumberUtils.min(new int[]{1, 2, 3}));
        assertEquals(-3, NumberUtils.min(new int[]{-1, -2, -3}));
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
        assertEquals((short)1, NumberUtils.min(new short[]{1, 2, 3}));
        assertEquals((short)-3, NumberUtils.min(new short[]{-1, -2, -3}));
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
        assertEquals((byte)1, NumberUtils.min(new byte[]{1, 2, 3}));
        assertEquals((byte)-3, NumberUtils.min(new byte[]{-1, -2, -3}));
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
        assertEquals(1.0, NumberUtils.min(new double[]{1.0, 2.0, 3.0}), 0.0);
        assertEquals(-3.0, NumberUtils.min(new double[]{-1.0, -2.0, -3.0}), 0.0);
        assertTrue(Double.isNaN(NumberUtils.min(new double[]{1.0, Double.NaN, 3.0})));
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
        assertEquals(1.0f, NumberUtils.min(new float[]{1.0f, 2.0f, 3.0f}), 0.0f);
        assertEquals(-3.0f, NumberUtils.min(new float[]{-1.0f, -2.0f, -3.0f}), 0.0f);
        assertTrue(Float.isNaN(NumberUtils.min(new float[]{1.0f, Float.NaN, 3.0f})));
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
        assertEquals(3L, NumberUtils.max(new long[]{1L, 2L, 3L}));
        assertEquals(-1L, NumberUtils.max(new long[]{-1L, -2L, -3L}));
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
        assertEquals(3, NumberUtils.max(new int[]{1, 2, 3}));
        assertEquals(-1, NumberUtils.max(new int[]{-1, -2, -3}));
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
        assertEquals((short)3, NumberUtils.max(new short[]{1, 2, 3}));
        assertEquals((short)-1, NumberUtils.max(new short[]{-1, -2, -3}));
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
        assertEquals((byte)3, NumberUtils.max(new byte[]{1, 2, 3}));
        assertEquals((byte)-1, NumberUtils.max(new byte[]{-1, -2, -3}));
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
        assertEquals(3.0, NumberUtils.max(new double[]{1.0, 2.0, 3.0}), 0.0);
        assertEquals(-1.0, NumberUtils.max(new double[]{-1.0, -2.0, -3.0}), 0.0);
        assertTrue(Double.isNaN(NumberUtils.max(new double[]{1.0, Double.NaN, 3.0})));
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
        assertEquals(3.0f, NumberUtils.max(new float[]{1.0f, 2.0f, 3.0f}), 0.0f);
        assertEquals(-1.0f, NumberUtils.max(new float[]{-1.0f, -2.0f, -3.0f}), 0.0f);
        assertTrue(Float.isNaN(NumberUtils.max(new float[]{1.0f, Float.NaN, 3.0f})));
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
    public void testMinThreeLong() {
        assertEquals(1L, NumberUtils.min(1L, 2L, 3L));
        assertEquals(1L, NumberUtils.min(2L, 1L, 3L));
        assertEquals(1L, NumberUtils.min(3L, 2L, 1L));
    }

    @Test
    public void testMinThreeInt() {
        assertEquals(1, NumberUtils.min(1, 2, 3));
        assertEquals(1, NumberUtils.min(2, 1, 3));
        assertEquals(1, NumberUtils.min(3, 2, 1));
    }

    @Test
    public void testMinThreeShort() {
        assertEquals((short)1, NumberUtils.min((short)1, (short)2, (short)3));
        assertEquals((short)1, NumberUtils.min((short)2, (short)1, (short)3));
        assertEquals((short)1, NumberUtils.min((short)3, (short)2, (short)1));
    }

    @Test
    public void testMinThreeByte() {
        assertEquals((byte)1, NumberUtils.min((byte)1, (byte)2, (byte)3));
        assertEquals((byte)1, NumberUtils.min((byte)2, (byte)1, (byte)3));
        assertEquals((byte)1, NumberUtils.min((byte)3, (byte)2, (byte)1));
    }

    @Test
    public void testMinThreeDouble() {
        assertEquals(1.0, NumberUtils.min(1.0, 2.0, 3.0), 0.0);
        assertTrue(Double.isNaN(NumberUtils.min(1.0, Double.NaN, 3.0)));
        assertEquals(Double.NEGATIVE_INFINITY, NumberUtils.min(Double.NEGATIVE_INFINITY, 1.0, 2.0), 0.0);
    }

    @Test
    public void testMinThreeFloat() {
        assertEquals(1.0f, NumberUtils.min(1.0f, 2.0f, 3.0f), 0.0f);
        assertTrue(Float.isNaN(NumberUtils.min(1.0f, Float.NaN, 3.0f)));
        assertEquals(Float.NEGATIVE_INFINITY, NumberUtils.min(Float.NEGATIVE_INFINITY, 1.0f, 2.0f), 0.0f);
    }

    @Test
    public void testMaxThreeLong() {
        assertEquals(3L, NumberUtils.max(1L, 2L, 3L));
        assertEquals(3L, NumberUtils.max(3L, 2L, 1L));
        assertEquals(3L, NumberUtils.max(2L, 3L, 1L));
    }

    @Test
    public void testMaxThreeInt() {
        assertEquals(3, NumberUtils.max(1, 2, 3));
        assertEquals(3, NumberUtils.max(3, 2, 1));
        assertEquals(3, NumberUtils.max(2, 3, 1));
    }

    @Test
    public void testMaxThreeShort() {
        assertEquals((short)3, NumberUtils.max((short)1, (short)2, (short)3));
        assertEquals((short)3, NumberUtils.max((short)3, (short)2, (short)1));
        assertEquals((short)3, NumberUtils.max((short)2, (short)3, (short)1));
    }

    @Test
    public void testMaxThreeByte() {
        assertEquals((byte)3, NumberUtils.max((byte)1, (byte)2, (byte)3));
        assertEquals((byte)3, NumberUtils.max((byte)3, (byte)2, (byte)1));
        assertEquals((byte)3, NumberUtils.max((byte)2, (byte)3, (byte)1));
    }

    @Test
    public void testMaxThreeDouble() {
        assertEquals(3.0, NumberUtils.max(1.0, 2.0, 3.0), 0.0);
        assertTrue(Double.isNaN(NumberUtils.max(1.0, Double.NaN, 3.0)));
        assertEquals(Double.POSITIVE_INFINITY, NumberUtils.max(Double.POSITIVE_INFINITY, 1.0, 2.0), 0.0);
    }

    @Test
    public void testMaxThreeFloat() {
        assertEquals(3.0f, NumberUtils.max(1.0f, 2.0f, 3.0f), 0.0f);
        assertTrue(Float.isNaN(NumberUtils.max(1.0f, Float.NaN, 3.0f)));
        assertEquals(Float.POSITIVE_INFINITY, NumberUtils.max(Float.POSITIVE_INFINITY, 1.0f, 2.0f), 0.0f);
    }

    @Test
    public void testIsDigits() {
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits(""));
        assertTrue(NumberUtils.isDigits("123"));
        assertFalse(NumberUtils.isDigits("12a3"));
    }

    @Test
    public void testIsNumber() {
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));
        assertTrue(NumberUtils.isNumber("123"));
        assertTrue(NumberUtils.isNumber("123.456"));
        assertTrue(NumberUtils.isNumber("1.2e3"));
        assertTrue(NumberUtils.isNumber("0x1A"));
        assertTrue(NumberUtils.isNumber("-0x1A"));
        assertFalse(NumberUtils.isNumber("0x"));
        assertTrue(NumberUtils.isNumber("123L"));
        assertTrue(NumberUtils.isNumber("123l"));
        assertFalse(NumberUtils.isNumber("123Lx"));
        assertTrue(NumberUtils.isNumber("1.2f"));
        assertTrue(NumberUtils.isNumber("1.2d"));
        assertFalse(NumberUtils.isNumber("1.2e"));
        assertFalse(NumberUtils.isNumber("1.2e-"));
        assertTrue(NumberUtils.isNumber("1.2e-3"));
        assertFalse(NumberUtils.isNumber("1.2e3.4"));
        assertFalse(NumberUtils.isNumber("1.2.3"));
        assertFalse(NumberUtils.isNumber("1.2e3e4"));
        assertFalse(NumberUtils.isNumber("--123"));
        assertTrue(NumberUtils.isNumber("."));
        assertFalse(NumberUtils.isNumber("1E"));
        assertTrue(NumberUtils.isNumber("1E2"));
        assertFalse(NumberUtils.isNumber("1E2E3"));
        assertTrue(NumberUtils.isNumber("1."));
        assertFalse(NumberUtils.isNumber("1.L"));
        assertFalse(NumberUtils.isNumber("1eL"));
        assertTrue(NumberUtils.isNumber("1e2"));
        assertTrue(NumberUtils.isNumber("-1"));
        assertTrue(NumberUtils.isNumber("+1"));
        assertFalse(NumberUtils.isNumber("+"));
        assertFalse(NumberUtils.isNumber("1e+"));
        assertTrue(NumberUtils.isNumber("1e+2"));
        assertFalse(NumberUtils.isNumber("1e-"));
        assertTrue(NumberUtils.isNumber("1e-2"));
    }
}
