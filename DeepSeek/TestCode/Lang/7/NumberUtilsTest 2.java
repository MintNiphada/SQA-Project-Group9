package org.apache.commons.lang3.math;

import static org.junit.Assert.*;
import org.junit.Test;

public class NumberUtilsTest {

    // Test constants
    @Test
    public void testConstants() {
        assertEquals(0L, NumberUtils.LONG_ZERO.longValue());
        assertEquals(1L, NumberUtils.LONG_ONE.longValue());
        assertEquals(-1L, NumberUtils.LONG_MINUS_ONE.longValue());
        assertEquals(0, NumberUtils.INTEGER_ZERO.intValue());
        assertEquals(1, NumberUtils.INTEGER_ONE.intValue());
        assertEquals(-1, NumberUtils.INTEGER_MINUS_ONE.intValue());
        assertEquals((short)0, NumberUtils.SHORT_ZERO.shortValue());
        assertEquals((short)1, NumberUtils.SHORT_ONE.shortValue());
        assertEquals((short)-1, NumberUtils.SHORT_MINUS_ONE.shortValue());
        assertEquals((byte)0, NumberUtils.BYTE_ZERO.byteValue());
        assertEquals((byte)1, NumberUtils.BYTE_ONE.byteValue());
        assertEquals((byte)-1, NumberUtils.BYTE_MINUS_ONE.byteValue());
        assertEquals(0.0d, NumberUtils.DOUBLE_ZERO.doubleValue(), 0.0);
        assertEquals(1.0d, NumberUtils.DOUBLE_ONE.doubleValue(), 0.0);
        assertEquals(-1.0d, NumberUtils.DOUBLE_MINUS_ONE.doubleValue(), 0.0);
        assertEquals(0.0f, NumberUtils.FLOAT_ZERO.floatValue(), 0.0f);
        assertEquals(1.0f, NumberUtils.FLOAT_ONE.floatValue(), 0.0f);
        assertEquals(-1.0f, NumberUtils.FLOAT_MINUS_ONE.floatValue(), 0.0f);
    }

    // toInt tests
    @Test
    public void testToInt_String() {
        assertEquals(0, NumberUtils.toInt(null));
        assertEquals(0, NumberUtils.toInt(""));
        assertEquals(1, NumberUtils.toInt("1"));
        assertEquals(0, NumberUtils.toInt("abc"));
        // leading/trailing spaces
        assertEquals(0, NumberUtils.toInt(" 1"));
        assertEquals(0, NumberUtils.toInt("1 "));
        assertEquals(0, NumberUtils.toInt(" 1 "));
    }

    @Test
    public void testToInt_String_Int() {
        assertEquals(1, NumberUtils.toInt(null, 1));
        assertEquals(2, NumberUtils.toInt("", 2));
        assertEquals(1, NumberUtils.toInt("1", 0));
        assertEquals(5, NumberUtils.toInt("abc", 5));
        assertEquals(-1, NumberUtils.toInt("-1", 0));
    }

    // toLong tests
    @Test
    public void testToLong_String() {
        assertEquals(0L, NumberUtils.toLong(null));
        assertEquals(0L, NumberUtils.toLong(""));
        assertEquals(1L, NumberUtils.toLong("1"));
        assertEquals(0L, NumberUtils.toLong("abc"));
    }

    @Test
    public void testToLong_String_Long() {
        assertEquals(1L, NumberUtils.toLong(null, 1L));
        assertEquals(2L, NumberUtils.toLong("", 2L));
        assertEquals(1L, NumberUtils.toLong("1", 0L));
        assertEquals(5L, NumberUtils.toLong("abc", 5L));
        assertEquals(-1L, NumberUtils.toLong("-1", 0L));
    }

    // toFloat tests
    @Test
    public void testToFloat_String() {
        assertEquals(0.0f, NumberUtils.toFloat(null), 0.0f);
        assertEquals(0.0f, NumberUtils.toFloat(""), 0.0f);
        assertEquals(1.5f, NumberUtils.toFloat("1.5"), 0.0f);
        assertEquals(0.0f, NumberUtils.toFloat("abc"), 0.0f);
    }

    @Test
    public void testToFloat_String_Float() {
        assertEquals(1.1f, NumberUtils.toFloat(null, 1.1f), 0.0f);
        assertEquals(1.1f, NumberUtils.toFloat("", 1.1f), 0.0f);
        assertEquals(1.5f, NumberUtils.toFloat("1.5", 0.0f), 0.0f);
        assertEquals(2.2f, NumberUtils.toFloat("abc", 2.2f), 0.0f);
    }

    // toDouble tests
    @Test
    public void testToDouble_String() {
        assertEquals(0.0d, NumberUtils.toDouble(null), 0.0d);
        assertEquals(0.0d, NumberUtils.toDouble(""), 0.0d);
        assertEquals(1.5d, NumberUtils.toDouble("1.5"), 0.0d);
        assertEquals(0.0d, NumberUtils.toDouble("abc"), 0.0d);
    }

    @Test
    public void testToDouble_String_Double() {
        assertEquals(1.1d, NumberUtils.toDouble(null, 1.1d), 0.0d);
        assertEquals(1.1d, NumberUtils.toDouble("", 1.1d), 0.0d);
        assertEquals(1.5d, NumberUtils.toDouble("1.5", 0.0d), 0.0d);
        assertEquals(2.2d, NumberUtils.toDouble("abc", 2.2d), 0.0d);
    }

    // toByte tests
    @Test
    public void testToByte_String() {
        assertEquals(0, NumberUtils.toByte(null));
        assertEquals(0, NumberUtils.toByte(""));
        assertEquals(1, NumberUtils.toByte("1"));
        assertEquals(0, NumberUtils.toByte("abc"));
        assertEquals(0, NumberUtils.toByte("128")); // out of range
    }

    @Test
    public void testToByte_String_Byte() {
        assertEquals(1, NumberUtils.toByte(null, (byte)1));
        assertEquals(2, NumberUtils.toByte("", (byte)2));
        assertEquals(1, NumberUtils.toByte("1", (byte)0));
        assertEquals(5, NumberUtils.toByte("abc", (byte)5));
    }

    // toShort tests
    @Test
    public void testToShort_String() {
        assertEquals(0, NumberUtils.toShort(null));
        assertEquals(0, NumberUtils.toShort(""));
        assertEquals(1, NumberUtils.toShort("1"));
        assertEquals(0, NumberUtils.toShort("abc"));
    }

    @Test
    public void testToShort_String_Short() {
        assertEquals(1, NumberUtils.toShort(null, (short)1));
        assertEquals(2, NumberUtils.toShort("", (short)2));
        assertEquals(1, NumberUtils.toShort("1", (short)0));
        assertEquals(5, NumberUtils.toShort("abc", (short)5));
    }

    // createFloat tests
    @Test
    public void testCreateFloat() {
        assertNull(NumberUtils.createFloat(null));
        assertEquals(1.0f, NumberUtils.createFloat("1"), 0.0f);
        assertEquals(1.5f, NumberUtils.createFloat("1.5"), 0.0f);
        try {
            NumberUtils.createFloat("abc");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    // createDouble tests
    @Test
    public void testCreateDouble() {
        assertNull(NumberUtils.createDouble(null));
        assertEquals(1.0d, NumberUtils.createDouble("1"), 0.0d);
        assertEquals(1.5d, NumberUtils.createDouble("1.5"), 0.0d);
        try {
            NumberUtils.createDouble("abc");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
        }
    }

    // createInteger tests
    @Test
    public void testCreateInteger() {
        assertNull(NumberUtils.createInteger(null));
        assertEquals(0, NumberUtils.createInteger("0").intValue());
        assertEquals(1, NumberUtils.createInteger("1").intValue());
        // hex
        assertEquals(0xFF, NumberUtils.createInteger("0xFF").intValue());
        assertEquals(0x10, NumberUtils.createInteger("0x10").intValue());
        // octal
        assertEquals(010, NumberUtils.createInteger("010").intValue());
        try {
            NumberUtils.createInteger("abc");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
        }
    }

    // createLong tests
    @Test
    public void testCreateLong() {
        assertNull(NumberUtils.createLong(null));
        assertEquals(0L, NumberUtils.createLong("0").longValue());
        assertEquals(1L, NumberUtils.createLong("1").longValue());
        assertEquals(0xFFL, NumberUtils.createLong("0xFF").longValue());
        assertEquals(010L, NumberUtils.createLong("010").longValue());
        try {
            NumberUtils.createLong("abc");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
        }
    }

    // createBigInteger tests
    @Test
    public void testCreateBigInteger() {
        assertNull(NumberUtils.createBigInteger(null));
        assertEquals(BigInteger.ZERO, NumberUtils.createBigInteger("0"));
        assertEquals(BigInteger.ONE, NumberUtils.createBigInteger("1"));
        assertEquals(new BigInteger("12345678901234567890"), NumberUtils.createBigInteger("12345678901234567890"));
        try {
            NumberUtils.createBigInteger("abc");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
        }
    }

    // createBigDecimal tests
    @Test
    public void testCreateBigDecimal() {
        assertNull(NumberUtils.createBigDecimal(null));
        assertEquals(BigDecimal.ZERO, NumberUtils.createBigDecimal("0"));
        assertEquals(BigDecimal.ONE, NumberUtils.createBigDecimal("1"));
        assertEquals(new BigDecimal("1.5"), NumberUtils.createBigDecimal("1.5"));
        try {
            NumberUtils.createBigDecimal("");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
        }
    }

    // createNumber tests (complex)
    @Test
    public void testCreateNumber_Null() {
        assertNull(NumberUtils.createNumber(null));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_Blank() {
        NumberUtils.createNumber("   ");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_EmptyString() {
        NumberUtils.createNumber("");
    }

    @Test
    public void testCreateNumber_LeadingTrailingSpaces() {
        try {
            NumberUtils.createNumber(" 1");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
        }
        try {
            NumberUtils.createNumber("1 ");
            fail();
        } catch (NumberFormatException e) {
        }
    }

    @Test
    public void testCreateNumber_DoubleDash() {
        assertNull(NumberUtils.createNumber("--1"));
    }

    @Test
    public void testCreateNumber_Hex() {
        assertEquals(0xFFL, NumberUtils.createNumber("0xFF"));
        assertEquals(0x1L, NumberUtils.createNumber("0x1"));
        assertEquals(0xABCDEF12L, NumberUtils.createNumber("0xABCDEF12"));
        // negative hex
        assertEquals(-0xFFL, NumberUtils.createNumber("-0xFF"));
        // hex with more than 8 digits -> Long
        assertEquals(0x123456789AB46L, NumberUtils.createNumber("0x123456789AB46"));
        // uppercase X
        assertEquals(0x1L, NumberUtils.createNumber("0X1"));
        assertEquals(-0x1L, NumberUtils.createNumber("-0X1"));
    }

    @Test
    public void testCreateNumber_HexTooLarge() {
        // longer than 8 hex digits
        Number n = NumberUtils.createNumber("0x123456789AB");
        assertTrue(n instanceof Long);
        assertEquals(0x123456789ABL, n.longValue());
    }

    @Test
    public void testCreateNumber_FloatType() {
        assertEquals(1.0f, NumberUtils.createNumber("1f"));
        assertEquals(1.0f, NumberUtils.createNumber("1F"));
        assertEquals(1.5f, NumberUtils.createNumber("1.5f"));
        assertEquals(-1.5f, NumberUtils.createNumber("-1.5F"));
    }

    @Test
    public void testCreateNumber_DoubleType() {
        assertEquals(1.0d, NumberUtils.createNumber("1d"));
        assertEquals(1.0d, NumberUtils.createNumber("1D"));
        assertEquals(1.5d, NumberUtils.createNumber("1.5d"));
        assertEquals(-1.5d, NumberUtils.createNumber("-1.5D"));
    }

    @Test
    public void testCreateNumber_LongType() {
        assertEquals(1L, NumberUtils.createNumber("1L"));
        assertEquals(-1L, NumberUtils.createNumber("-1L"));
        assertEquals(1L, NumberUtils.createNumber("1l"));
        assertEquals(-1L, NumberUtils.createNumber("-1l"));
    }

    @Test
    public void testCreateNumber_LargeLongFallsBackToBigInteger() {
        // too large for Long
        Number n = NumberUtils.createNumber("9223372036854775808L"); // > Long.MAX_VALUE
        assertTrue(n instanceof BigInteger);
        assertEquals(new BigInteger("9223372036854775808"), n);
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_LongWithDecimal() {
        NumberUtils.createNumber("1.5L");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_LongWithExponent() {
        NumberUtils.createNumber("1E2L");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_FloatWithLongSuffix() {
        NumberUtils.createNumber("1fL");
    }

    @Test
    public void testCreateNumber_ScientificNotation() {
        assertEquals(1.0E2d, NumberUtils.createNumber("1E2"));
        assertEquals(1.0E2d, NumberUtils.createNumber("1e2"));
        assertEquals(1.5E2d, NumberUtils.createNumber("1.5E2"));
        assertEquals(-1.5E-2d, NumberUtils.createNumber("-1.5e-2"));
    }

    @Test
    public void testCreateNumber_ScientificWithTypeSuffix() {
        assertEquals(1.0E2d, NumberUtils.createNumber("1E2d"));
        assertEquals(1.0E2f, NumberUtils.createNumber("1E2f"));
    }

    @Test
    public void testCreateNumber_DecimalOnly() {
        assertEquals(0.5d, NumberUtils.createNumber(".5"));
        assertEquals(-0.5d, NumberUtils.createNumber("-.5"));
    }

    @Test
    public void testCreateNumber_IntegerNumber() {
        assertEquals(123, NumberUtils.createNumber("123"));
        assertEquals(-456, NumberUtils.createNumber("-456"));
        // out of integer range
        assertEquals(2147483648L, NumberUtils.createNumber("2147483648")); // > Integer.MAX_VALUE
    }

    @Test
    public void testCreateNumber_VeryLargeInteger() {
        // larger than Long
        Number n = NumberUtils.createNumber("12345678901234567890");
        assertTrue(n instanceof BigInteger);
    }

    @Test
    public void testCreateNumber_AllZerosCheck() {
        // Float with all zeros but non-zero string? e.g. "0.0" -> all zeros? mantissa "0", exp null -> allZeros true. So float should be fine.
        assertEquals(0.0f, NumberUtils.createNumber("0.0f"));
        // but "0.000000000000000000000000000001f" -> float parse might return 0.0f but not all zeros, so should fall through to Double or BigDecimal
        Number n = NumberUtils.createNumber("0.000000000000000000000000000001f");
        assertTrue(n instanceof Double || n instanceof BigDecimal);
        assertTrue(n.doubleValue() > 0);
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_InvalidFormat() {
        NumberUtils.createNumber("abc");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_InvalidDecimalPointPlacement() {
        NumberUtils.createNumber("1.2.3");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_InvalidExponentWithDecimal() {
        NumberUtils.createNumber("1e2.3");
    }

    // isDigits tests
    @Test
    public void testIsDigits() {
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits(""));
        assertTrue(NumberUtils.isDigits("12345"));
        assertFalse(NumberUtils.isDigits("12a45"));
        assertFalse(NumberUtils.isDigits("12.45"));
        assertFalse(NumberUtils.isDigits("-123"));
    }

    // isNumber tests (complex)
    @Test
    public void testIsNumber() {
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));
        // simple integer
        assertTrue(NumberUtils.isNumber("123"));
        assertTrue(NumberUtils.isNumber("-123"));
        // hex
        assertTrue(NumberUtils.isNumber("0xABCDEF"));
        assertTrue(NumberUtils.isNumber("0x123"));
        assertFalse(NumberUtils.isNumber("0x"));
        assertFalse(NumberUtils.isNumber("--123"));
        // scientific
        assertTrue(NumberUtils.isNumber("1E2"));
        assertTrue(NumberUtils.isNumber("1e2"));
        assertTrue(NumberUtils.isNumber("-1E-2"));
        assertTrue(NumberUtils.isNumber("1.5E2"));
        assertFalse(NumberUtils.isNumber("1E"));
        assertFalse(NumberUtils.isNumber("1E-"));
        assertFalse(NumberUtils.isNumber("1E2E3"));
        // decimal
        assertTrue(NumberUtils.isNumber("1.5"));
        assertTrue(NumberUtils.isNumber(".5"));
        assertTrue(NumberUtils.isNumber("5."));
        assertFalse(NumberUtils.isNumber("1.5.5"));
        assertFalse(NumberUtils.isNumber("1.5E2.3"));
        // type qualifiers
        assertTrue(NumberUtils.isNumber("123L"));
        assertTrue(NumberUtils.isNumber("123l"));
        assertFalse(NumberUtils.isNumber("123.5L"));
        assertFalse(NumberUtils.isNumber("123E2L"));
        assertTrue(NumberUtils.isNumber("123f"));
        assertTrue(NumberUtils.isNumber("123F"));
        assertTrue(NumberUtils.isNumber("123D"));
        assertTrue(NumberUtils.isNumber("123d"));
        // trailing dot without exponent
        assertTrue(NumberUtils.isNumber("123."));
        // leading zero but not hex
        assertTrue(NumberUtils.isNumber("0123"));
        // sign after exponent required digit
        assertFalse(NumberUtils.isNumber("1E-"));
        assertFalse(NumberUtils.isNumber("1E+"));
        // two signs
        assertFalse(NumberUtils.isNumber("--123"));
        assertFalse(NumberUtils.isNumber("+-123"));
        // sign after decimal
        assertFalse(NumberUtils.isNumber("1.-5"));
        // more cases
        assertFalse(NumberUtils.isNumber("0xG"));
        assertFalse(NumberUtils.isNumber("0x-1"));
        assertFalse(NumberUtils.isNumber("\n"));
    }

    // min array tests
    @Test(expected = IllegalArgumentException.class)
    public void testMin_LongArray_Null() {
        NumberUtils.min((long[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMin_LongArray_Empty() {
        NumberUtils.min(new long[0]);
    }

    @Test
    public void testMin_LongArray() {
        assertEquals(1L, NumberUtils.min(new long[]{1L, 2L, 3L}));
        assertEquals(-5L, NumberUtils.min(new long[]{-5L, 0L, 3L}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMin_IntArray_Null() {
        NumberUtils.min((int[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMin_IntArray_Empty() {
        NumberUtils.min(new int[0]);
    }

    @Test
    public void testMin_IntArray() {
        assertEquals(1, NumberUtils.min(new int[]{1, 2, 3));
        assertEquals(-5, NumberUtils.min(new int[]{-5, 0, 3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMin_ShortArray_Null() {
        NumberUtils.min((short[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMin_ShortArray_Empty() {
        NumberUtils.min(new short[0]);
    }

    @Test
    public void testMin_ShortArray() {
        assertEquals(1, NumberUtils.min(new short[]{1, 2, 3));
        assertEquals(-5, NumberUtils.min(new short[]{-5, 0, 3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMin_ByteArray_Null() {
        NumberUtils.min((byte[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMin_ByteArray_Empty() {
        NumberUtils.min(new byte[0]);
    }

    @Test
    public void testMin_ByteArray() {
        assertEquals(1, NumberUtils.min(new byte[]{1, 2, 3));
        assertEquals(-5, NumberUtils.min(new byte[]{-5, 0, 3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMin_DoubleArray_Null() {
        NumberUtils.min((double[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMin_DoubleArray_Empty() {
        NumberUtils.min(new double[0]);
    }

    @Test
    public void testMin_DoubleArray() {
        assertEquals(1.2, NumberUtils.min(new double[]{1.2, 2.5, 3.1}), 0.0);
        assertEquals(-5.5, NumberUtils.min(new double[]{-5.5, 0.0, 3.3}), 0.0);
    }

    @Test
    public void testMin_DoubleArray_NaN() {
        assertTrue(Double.isNaN(NumberUtils.min(new double[]{Double.NaN, 2.0, 3.0})));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMin_FloatArray_Null() {
        NumberUtils.min((float[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMin_FloatArray_Empty() {
        NumberUtils.min(new float[0]);
    }

    @Test
    public void testMin_FloatArray() {
        assertEquals(1.2f, NumberUtils.min(new float[]{1.2f, 2.5f, 3.1f}), 0.0f);
        assertEquals(-5.5f, NumberUtils.min(new float[]{-5.5f, 0.0f, 3.3f}), 0.0f);
    }

    @Test
    public void testMin_FloatArray_NaN() {
        assertTrue(Float.isNaN(NumberUtils.min(new float[]{Float.NaN, 2.0f, 3.0f})));
    }

    // max array tests
    @Test(expected = IllegalArgumentException.class)
    public void testMax_LongArray_Null() {
        NumberUtils.max((long[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMax_LongArray_Empty() {
        NumberUtils.max(new long[0]);
    }

    @Test
    public void testMax_LongArray() {
        assertEquals(3L, NumberUtils.max(new long[]{1L, 3L, 2L}));
        assertEquals(3L, NumberUtils.max(new long[]{-5L, 0L, 3L}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMax_IntArray_Null() {
        NumberUtils.max((int[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMax_IntArray_Empty() {
        NumberUtils.max(new int[0]);
    }

    @Test
    public void testMax_IntArray() {
        assertEquals(3, NumberUtils.max(new int[]{1, 3, 2));
        assertEquals(3, NumberUtils.max(new int[]{-5, 0, 3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMax_ShortArray_Null() {
        NumberUtils.max((short[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMax_ShortArray_Empty() {
        NumberUtils.max(new short[0]);
    }

    @Test
    public void testMax_ShortArray() {
        assertEquals(3, NumberUtils.max(new short[]{1, 3, 2));
        assertEquals(3, NumberUtils.max(new short[]{-5, 0, 3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMax_ByteArray_Null() {
        NumberUtils.max((byte[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMax_ByteArray_Empty() {
        NumberUtils.max(new byte[0]);
    }

    @Test
    public void testMax_ByteArray() {
        assertEquals(3, NumberUtils.max(new byte[]{1, 3, 2));
        assertEquals(3, NumberUtils.max(new byte[]{-5, 0, 3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMax_DoubleArray_Null() {
        NumberUtils.max((double[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMax_DoubleArray_Empty() {
        NumberUtils.max(new double[0]);
    }

    @Test
    public void testMax_DoubleArray() {
        assertEquals(3.1, NumberUtils.max(new double[]{1.2, 3.1, 2.5}), 0.0);
        assertEquals(3.3, NumberUtils.max(new double[]{-5.5, 0.0, 3.3}), 0.0);
    }

    @Test
    public void testMax_DoubleArray_NaN() {
        assertTrue(Double.isNaN(NumberUtils.max(new double[]{Double.NaN, 2.0, 3.0})));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMax_FloatArray_Null() {
        NumberUtils.max((float[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMax_FloatArray_Empty() {
        NumberUtils.max(new float[0]);
    }

    @Test
    public void testMax_FloatArray() {
        assertEquals(3.1f, NumberUtils.max(new float[]{1.2f, 3.1f, 2.5f}), 0.0f);
        assertEquals(3.3f, NumberUtils.max(new float[]{-5.5f, 0.0f, 3.3f}), 0.0f);
    }

    @Test
    public void testMax_FloatArray_NaN() {
        assertTrue(Float.isNaN(NumberUtils.max(new float[]{Float.NaN, 2.0f, 3.0f})));
    }

    // 3 param min tests
    @Test
    public void testMin_Long() {
        assertEquals(1L, NumberUtils.min(1L, 2L, 3L));
        assertEquals(-5L, NumberUtils.min(-5L, 0L, 3L));
        assertEquals(3L, NumberUtils.min(3L, 3L, 3L));
        assertEquals(Long.MIN_VALUE, NumberUtils.min(Long.MIN_VALUE, 0L, Long.MAX_VALUE));
    }

    @Test
    public void testMin_Int() {
        assertEquals(1, NumberUtils.min(1, 2, 3));
        assertEquals(-5, NumberUtils.min(-5, 0, 3));
    }

    @Test
    public void testMin_Short() {
        assertEquals(1, NumberUtils.min((short)1, (short)2, (short)3));
        assertEquals(-5, NumberUtils.min((short)-5, (short)0, (short)3));
    }

    @Test
    public void testMin_Byte() {
        assertEquals(1, NumberUtils.min((byte)1, (byte)2, (byte)3));
        assertEquals(-5, NumberUtils.min((byte)-5, (byte)0, (byte)3));
    }

    @Test
    public void testMin_Double() {
        assertEquals(1.2, NumberUtils.min(1.2, 2.5, 3.1), 0.0);
        assertEquals(-5.5, NumberUtils.min(-5.5, 0.0, 3.3), 0.0);
        assertTrue(Double.isNaN(NumberUtils.min(Double.NaN, 1.0, 2.0)));
        assertEquals(Double.NEGATIVE_INFINITY, NumberUtils.min(1.0, Double.NEGATIVE_INFINITY, 2.0), 0.0);
    }

    @Test
    public void testMin_Float() {
        assertEquals(1.2f, NumberUtils.min(1.2f, 2.5f, 3.1f), 0.0f);
        assertEquals(-5.5f, NumberUtils.min(-5.5f, 0.0f, 3.3f), 0.0f);
        assertTrue(Float.isNaN(NumberUtils.min(Float.NaN, 1.0f, 2.0f)));
        assertEquals(Float.NEGATIVE_INFINITY, NumberUtils.min(1.0f, Float.NEGATIVE_INFINITY, 2.0f), 0.0f);
    }

    // 3 param max tests
    @Test
    public void testMax_Long() {
        assertEquals(3L, NumberUtils.max(1L, 2L, 3L));
        assertEquals(3L, NumberUtils.max(-5L, 0L, 3L));
        assertEquals(Long.MAX_VALUE, NumberUtils.max(Long.MIN_VALUE, 0L, Long.MAX_VALUE));
    }

    @Test
    public void testMax_Int() {
        assertEquals(3, NumberUtils.max(1, 2, 3));
        assertEquals(3, NumberUtils.max(-5, 0, 3));
    }

    @Test
    public void testMax_Short() {
        assertEquals(3, NumberUtils.max((short)1, (short)2, (short)3));
        assertEquals(3, NumberUtils.max((short)-5, (short)0, (short)3));
    }

    @Test
    public void testMax_Byte() {
        assertEquals(3, NumberUtils.max((byte)1, (byte)2, (byte)3));
        assertEquals(3, NumberUtils.max((byte)-5, (byte)0, (byte)3));
    }

    @Test
    public void testMax_Double() {
        assertEquals(3.1, NumberUtils.max(1.2, 2.5, 3.1), 0.0);
        assertEquals(3.3, NumberUtils.max(-5.5, 0.0, 3.3), 0.0);
        assertTrue(Double.isNaN(NumberUtils.max(Double.NaN, 1.0, 2.0)));
        assertEquals(Double.POSITIVE_INFINITY, NumberUtils.max(1.0, Double.POSITIVE_INFINITY, 2.0), 0.0);
    }

    @Test
    public void testMax_Float() {
        assertEquals(3.1f, NumberUtils.max(1.2f, 2.5f, 3.1f), 0.0f);
        assertEquals(3.3f, NumberUtils.max(-5.5f, 0.0f, 3.3f), 0.0f);
        assertTrue(Float.isNaN(NumberUtils.max(Float.NaN, 1.0f, 2.0f)));
        assertEquals(Float.POSITIVE_INFINITY, NumberUtils.max(1.0f, Float.POSITIVE_INFINITY, 2.0f), 0.0f);
    }

    // Additional edge cases for createNumber to improve coverage
    @Test
    public void testCreateNumber_FloatTooBig() {
        // "1e40f" too large for float, should fallback to Double or BigDecimal
        Number n = NumberUtils.createNumber("1e40f");
        assertTrue(n instanceof Double || n instanceof BigDecimal);
    }

    @Test
    public void testCreateNumber_DoubleTooBig() {
        Number n = NumberUtils.createNumber("1e400d");
        // Double too large, should fallback to BigDecimal
        assertTrue(n instanceof BigDecimal);
    }

    @Test
    public void testCreateNumber_NegativeZeroFloat() {
        assertEquals(-0.0f, NumberUtils.createNumber("-0.0f"));
    }

    @Test
    public void testCreateNumber_AllZerosFalse() {
        // "0.0000000000000000000000000000001f" should not be float because of allZeros check
        Number n = NumberUtils.createNumber("0.0000000000000000000000000000001f");
        assertTrue(n instanceof Double || n instanceof BigDecimal);
        assertTrue(n.doubleValue() > 0);
    }

    @Test
    public void testCreateNumber_ScientificWithNoSign() {
        assertEquals(1E3d, NumberUtils.createNumber("1E3"));
    }

    @Test
    public void testCreateNumber_DecimalWithE() {
        assertEquals(1.5E2d, NumberUtils.createNumber("1.5E2"));
    }

    @Test
    public void testCreateNumber_EndingWithDot() {
        // "1." -> should be integer? Actually no type qualifier, will try integer/long/bigint. "1." decimal point but nothing after -> dec = null? Actually code: if decPos > -1 then dec = substring after decPos up to expPos or end. So "1." -> decPos=1, substring after -> "" empty. dec = "" (empty string). Then dec is not null, exp not null? no exp, so dec != null. Then else branch: Must be a float,double,BigDec. allZeros on mant? mant="1", exp=null. float/double parse "1." as 1.0. So it should work.
        assertEquals(1.0d, NumberUtils.createNumber("1."));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_BadHex() {
        NumberUtils.createNumber("0xGHI");
    }

    @Test
    public void testCreateNumber_NegativeHex() {
        assertEquals(-0xFFL, NumberUtils.createNumber("-0xFF"));
    }

    @Test
    public void testCreateNumber_HexExactly8Digits() {
        // 8 hex digits -> can be int (if value within int range) but less than 8 digits? Actually condition: if hexDigits > 8 -> createLong else createInteger. So exactly 8 digits, if value > Integer.MAX_VALUE will throw exception? createInteger might fail, but the method does not catch it. That's a bug in the method? Actually it's as design. test if it throws.
        try {
            NumberUtils.createNumber("0xFFFFFFFF"); // 0xFFFFFFFF is -1 as int, valid.
            assertTrue(true);
        } catch (NumberFormatException e) {
            fail("Should be valid");
        }
        try {
            NumberUtils.createNumber("0x80000000"); // >Integer.MAX_VALUE, will throw NumberFormatException
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
        }
    }

}
