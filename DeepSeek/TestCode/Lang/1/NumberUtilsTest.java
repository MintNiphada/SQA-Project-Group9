package org.apache.commons.lang3.math;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;

public class NumberUtilsTest {

    // ---------------------------------------------------------------- toInt

    @Test
    public void testToInt_String() {
        assertEquals(0, NumberUtils.toInt(null));
        assertEquals(0, NumberUtils.toInt(""));
        assertEquals(1, NumberUtils.toInt("1"));
        assertEquals(-1, NumberUtils.toInt("-1"));
        assertEquals(0, NumberUtils.toInt("invalid"));
        assertEquals(Integer.MAX_VALUE, NumberUtils.toInt(Integer.toString(Integer.MAX_VALUE)));
        assertEquals(Integer.MIN_VALUE, NumberUtils.toInt(Integer.toString(Integer.MIN_VALUE)));
    }

    @Test
    public void testToInt_String_int() {
        assertEquals(1, NumberUtils.toInt(null, 1));
        assertEquals(1, NumberUtils.toInt("", 1));
        assertEquals(1, NumberUtils.toInt("1", 0));
        assertEquals(5, NumberUtils.toInt("abc", 5));
        assertEquals(Integer.MIN_VALUE, NumberUtils.toInt(Integer.toString(Integer.MIN_VALUE), 0));
        assertEquals(Integer.MAX_VALUE, NumberUtils.toInt(Integer.toString(Integer.MAX_VALUE), 0));
    }

    // --------------------------------------------------------------- toLong

    @Test
    public void testToLong_String() {
        assertEquals(0L, NumberUtils.toLong(null));
        assertEquals(0L, NumberUtils.toLong(""));
        assertEquals(1L, NumberUtils.toLong("1"));
        assertEquals(-1L, NumberUtils.toLong("-1"));
        assertEquals(0L, NumberUtils.toLong("invalid"));
        assertEquals(Long.MAX_VALUE, NumberUtils.toLong(Long.toString(Long.MAX_VALUE)));
        assertEquals(Long.MIN_VALUE, NumberUtils.toLong(Long.toString(Long.MIN_VALUE)));
    }

    @Test
    public void testToLong_String_long() {
        assertEquals(1L, NumberUtils.toLong(null, 1L));
        assertEquals(1L, NumberUtils.toLong("", 1L));
        assertEquals(1L, NumberUtils.toLong("1", 0L));
        assertEquals(5L, NumberUtils.toLong("abc", 5L));
        assertEquals(Long.MIN_VALUE, NumberUtils.toLong(Long.toString(Long.MIN_VALUE), 0L));
        assertEquals(Long.MAX_VALUE, NumberUtils.toLong(Long.toString(Long.MAX_VALUE), 0L));
    }

    // -------------------------------------------------------------- toFloat

    @Test
    public void testToFloat_String() {
        assertEquals(0.0f, NumberUtils.toFloat(null), 0f);
        assertEquals(0.0f, NumberUtils.toFloat(""), 0f);
        assertEquals(1.5f, NumberUtils.toFloat("1.5"), 0f);
        assertEquals(-2.3f, NumberUtils.toFloat("-2.3"), 0f);
        assertEquals(0.0f, NumberUtils.toFloat("abc"), 0f);
        assertEquals(Float.MAX_VALUE, NumberUtils.toFloat(Float.toString(Float.MAX_VALUE)), 0f);
    }

    @Test
    public void testToFloat_String_float() {
        assertEquals(1.1f, NumberUtils.toFloat(null, 1.1f), 0f);
        assertEquals(1.1f, NumberUtils.toFloat("", 1.1f), 0f);
        assertEquals(1.5f, NumberUtils.toFloat("1.5", 0.0f), 0f);
        assertEquals(2.5f, NumberUtils.toFloat("abc", 2.5f), 0f);
        assertEquals(Float.MIN_VALUE, NumberUtils.toFloat(Float.toString(Float.MIN_VALUE), 0f), 0f);
    }

    // ------------------------------------------------------------- toDouble

    @Test
    public void testToDouble_String() {
        assertEquals(0.0d, NumberUtils.toDouble(null), 0d);
        assertEquals(0.0d, NumberUtils.toDouble(""), 0d);
        assertEquals(1.5d, NumberUtils.toDouble("1.5"), 0d);
        assertEquals(-2.3d, NumberUtils.toDouble("-2.3"), 0d);
        assertEquals(0.0d, NumberUtils.toDouble("not a number"), 0d);
        assertEquals(Double.MAX_VALUE, NumberUtils.toDouble(Double.toString(Double.MAX_VALUE)), 0d);
    }

    @Test
    public void testToDouble_String_double() {
        assertEquals(1.1d, NumberUtils.toDouble(null, 1.1d), 0d);
        assertEquals(1.1d, NumberUtils.toDouble("", 1.1d), 0d);
        assertEquals(1.5d, NumberUtils.toDouble("1.5", 0.0d), 0d);
        assertEquals(2.5d, NumberUtils.toDouble("abc", 2.5d), 0d);
        assertEquals(Double.MIN_VALUE, NumberUtils.toDouble(Double.toString(Double.MIN_VALUE), 0d), 0d);
    }

    // --------------------------------------------------------------- toByte

    @Test
    public void testToByte_String() {
        assertEquals((byte) 0, NumberUtils.toByte(null));
        assertEquals((byte) 0, NumberUtils.toByte(""));
        assertEquals((byte) 1, NumberUtils.toByte("1"));
        assertEquals((byte) -1, NumberUtils.toByte("-1"));
        assertEquals((byte) 0, NumberUtils.toByte("invalid"));
        assertEquals(Byte.MAX_VALUE, NumberUtils.toByte(Byte.toString(Byte.MAX_VALUE)));
        assertEquals(Byte.MIN_VALUE, NumberUtils.toByte(Byte.toString(Byte.MIN_VALUE)));
    }

    @Test
    public void testToByte_String_byte() {
        assertEquals((byte) 1, NumberUtils.toByte(null, (byte) 1));
        assertEquals((byte) 1, NumberUtils.toByte("", (byte) 1));
        assertEquals((byte) 1, NumberUtils.toByte("1", (byte) 0));
        assertEquals((byte) 5, NumberUtils.toByte("abc", (byte) 5));
        assertEquals((byte) Byte.MIN_VALUE, NumberUtils.toByte(Byte.toString(Byte.MIN_VALUE), (byte) 0));
        assertEquals((byte) Byte.MAX_VALUE, NumberUtils.toByte(Byte.toString(Byte.MAX_VALUE), (byte) 0));
    }

    // -------------------------------------------------------------- toShort

    @Test
    public void testToShort_String() {
        assertEquals((short) 0, NumberUtils.toShort(null));
        assertEquals((short) 0, NumberUtils.toShort(""));
        assertEquals((short) 1, NumberUtils.toShort("1"));
        assertEquals((short) -1, NumberUtils.toShort("-1"));
        assertEquals((short) 0, NumberUtils.toShort("invalid"));
        assertEquals(Short.MAX_VALUE, NumberUtils.toShort(Short.toString(Short.MAX_VALUE)));
        assertEquals(Short.MIN_VALUE, NumberUtils.toShort(Short.toString(Short.MIN_VALUE)));
    }

    @Test
    public void testToShort_String_short() {
        assertEquals((short) 1, NumberUtils.toShort(null, (short) 1));
        assertEquals((short) 1, NumberUtils.toShort("", (short) 1));
        assertEquals((short) 1, NumberUtils.toShort("1", (short) 0));
        assertEquals((short) 5, NumberUtils.toShort("abc", (short) 5));
        assertEquals((short) Short.MIN_VALUE, NumberUtils.toShort(Short.toString(Short.MIN_VALUE), (short) 0));
        assertEquals((short) Short.MAX_VALUE, NumberUtils.toShort(Short.toString(Short.MAX_VALUE), (short) 0));
    }

    // --------------------------------------------------------- createNumber

    @Test
    public void testCreateNumber_null() {
        assertNull(NumberUtils.createNumber(null));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_blank_throwsException() {
        NumberUtils.createNumber("   ");
    }

    @Test
    public void testCreateNumber_simpleInteger() {
        assertEquals(Integer.valueOf(123), NumberUtils.createNumber("123"));
        assertEquals(Integer.valueOf(-456), NumberUtils.createNumber("-456"));
    }

    @Test
    public void testCreateNumber_decimalFloat() {
        assertEquals(Float.valueOf(1.5f), NumberUtils.createNumber("1.5"));
        assertEquals(Double.valueOf(0.12345678901234567890d), NumberUtils.createNumber("0.12345678901234567890"));
    }

    @Test
    public void testCreateNumber_exponent() {
        assertEquals(Double.valueOf(2e3), NumberUtils.createNumber("2e3"));
        assertEquals(Float.valueOf(1.5e2f), NumberUtils.createNumber("1.5e2f"));
    }

    @Test
    public void testCreateNumber_hex() {
        assertEquals(Integer.valueOf(26), NumberUtils.createNumber("0x1A"));
        assertEquals(Integer.valueOf(-26), NumberUtils.createNumber("-0x1A"));
        assertEquals(Long.valueOf(0x1FFFFFFFFL), NumberUtils.createNumber("0x1FFFFFFFF"));
        assertTrue(NumberUtils.createNumber("0x1234567890ABCDEF") instanceof Long);
        assertTrue(NumberUtils.createNumber("0x1234567890ABCDEF1234567890AB") instanceof BigInteger);
    }

    @Test
    public void testCreateNumber_hexHash() {
        assertEquals(Integer.valueOf(26), NumberUtils.createNumber("#1A"));
        assertEquals(Integer.valueOf(-26), NumberUtils.createNumber("-#1A"));
    }

    @Test
    public void testCreateNumber_longSuffix() {
        assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123L"));
        assertEquals(Long.valueOf(-567L), NumberUtils.createNumber("-567L"));
    }

    @Test
    public void testCreateNumber_floatSuffix() {
        assertEquals(Float.valueOf(1.5f), NumberUtils.createNumber("1.5F"));
        assertEquals(Float.valueOf(-2.3f), NumberUtils.createNumber("-2.3f"));
        assertEquals(Double.valueOf(1.5d), NumberUtils.createNumber("1.5D"));
        assertEquals(Double.valueOf(-2.3d), NumberUtils.createNumber("-2.3d"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_invalidSuffix() {
        NumberUtils.createNumber("12X");
    }

    @Test
    public void testCreateNumber_verySmallFloat() {
        // float underflow but allZeros logic might accept it
        Number n = NumberUtils.createNumber("0.0003");
        assertEquals(Float.valueOf(0.0f), n); // implementation returns Float 0.0
        assertTrue(n instanceof Float);
        // A very small non-zero number that float can represent exactly
        n = NumberUtils.createNumber("0.00000001");
        assertEquals(Float.valueOf(1e-8f), n);
    }

    @Test
    public void testCreateNumber_floatOverflowGoesToDouble() {
        Number n = NumberUtils.createNumber(Float.toString(Float.MAX_VALUE * 2));
        assertEquals(Double.valueOf(Float.MAX_VALUE * 2.0d), n);
    }

    @Test
    public void testCreateNumber_doubleOverflowGoesToBigDecimal() {
        Number n = NumberUtils.createNumber(Double.toString(Double.MAX_VALUE * 2));
        assertEquals(new BigDecimal(Double.MAX_VALUE).multiply(BigDecimal.valueOf(2)), n);
    }

    @Test
    public void testCreateNumber_allZerosLogic() {
        // mant all zeros, exp not null but all zeros, suffix F, acceptable
        assertEquals(Float.valueOf(0f), NumberUtils.createNumber("0.0e0F"));
        // mant non-zero, exp null, allZeros true (mant "0") but decimal part non-zero, still returns float 0.0
        assertEquals(Float.valueOf(0f), NumberUtils.createNumber("0.0003"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_invalidFormat_doubleExponent() {
        NumberUtils.createNumber("1e2e3");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_invalidFormat_doubleDecimal() {
        NumberUtils.createNumber("1.2.3");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_blankString() {
        NumberUtils.createNumber("");
    }

    // ------------------------------------------------------- createFloat

    @Test
    public void testCreateFloat() {
        assertNull(NumberUtils.createFloat(null));
        assertEquals(Float.valueOf(1.5f), NumberUtils.createFloat("1.5"));
        assertEquals(Float.valueOf(-2.3f), NumberUtils.createFloat("-2.3"));
        assertEquals(Float.valueOf(Float.MAX_VALUE), NumberUtils.createFloat(Float.toString(Float.MAX_VALUE)));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateFloat_invalid() {
        NumberUtils.createFloat("not a number");
    }

    // ------------------------------------------------------ createDouble

    @Test
    public void testCreateDouble() {
        assertNull(NumberUtils.createDouble(null));
        assertEquals(Double.valueOf(1.5d), NumberUtils.createDouble("1.5"));
        assertEquals(Double.valueOf(-2.3d), NumberUtils.createDouble("-2.3"));
        assertEquals(Double.valueOf(Double.MAX_VALUE), NumberUtils.createDouble(Double.toString(Double.MAX_VALUE)));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateDouble_invalid() {
        NumberUtils.createDouble("abc");
    }

    // ---------------------------------------------------- createInteger

    @Test
    public void testCreateInteger() {
        assertNull(NumberUtils.createInteger(null));
        assertEquals(Integer.valueOf(123), NumberUtils.createInteger("123"));
        assertEquals(Integer.valueOf(-456), NumberUtils.createInteger("-456"));
        assertEquals(Integer.valueOf(0x1A), NumberUtils.createInteger("0x1A"));
        assertEquals(Integer.valueOf(077), NumberUtils.createInteger("077")); // octal
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateInteger_invalid() {
        NumberUtils.createInteger("xyz");
    }

    // ------------------------------------------------------- createLong

    @Test
    public void testCreateLong() {
        assertNull(NumberUtils.createLong(null));
        assertEquals(Long.valueOf(123L), NumberUtils.createLong("123"));
        assertEquals(Long.valueOf(-456L), NumberUtils.createLong("-456"));
        assertEquals(Long.valueOf(0x1A), NumberUtils.createLong("0x1A"));
        assertEquals(Long.valueOf(077), NumberUtils.createLong("077"));
        assertEquals(Long.valueOf(Long.MAX_VALUE), NumberUtils.createLong(Long.toString(Long.MAX_VALUE)));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateLong_invalid() {
        NumberUtils.createLong("not a long");
    }

    // ------------------------------------------------- createBigInteger

    @Test
    public void testCreateBigInteger() {
        assertNull(NumberUtils.createBigInteger(null));
        assertEquals(new BigInteger("123"), NumberUtils.createBigInteger("123"));
        assertEquals(new BigInteger("-456"), NumberUtils.createBigInteger("-456"));
        assertEquals(new BigInteger("1A6", 16), NumberUtils.createBigInteger("0x1A6"));
        assertEquals(new BigInteger("1A6", 16), NumberUtils.createBigInteger("#1A6"));
        assertEquals(new BigInteger("777", 8), NumberUtils.createBigInteger("0777"));
        // very large number
        assertEquals(new BigInteger("123456789012345678901234567890"), NumberUtils.createBigInteger("123456789012345678901234567890"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigInteger_invalid() {
        NumberUtils.createBigInteger("---");
    }

    // ------------------------------------------------ createBigDecimal

    @Test
    public void testCreateBigDecimal() {
        assertNull(NumberUtils.createBigDecimal(null));
        assertEquals(new BigDecimal("123"), NumberUtils.createBigDecimal("123"));
        assertEquals(new BigDecimal("-456.78"), NumberUtils.createBigDecimal("-456.78"));
        assertEquals(new BigDecimal("1.5e3"), NumberUtils.createBigDecimal("1.5e3"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigDecimal_blank() {
        NumberUtils.createBigDecimal("   ");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigDecimal_doubleNegation() {
        NumberUtils.createBigDecimal("--123");
    }

    // ---------------------------------------------------- min array long[]

    @Test(expected = IllegalArgumentException.class)
    public void testMin_longArray_null() {
        NumberUtils.min((long[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMin_longArray_empty() {
        NumberUtils.min(new long[0]);
    }

    @Test
    public void testMin_longArray() {
        assertEquals(1L, NumberUtils.min(new long[]{1L, 5L, 3L}));
        assertEquals(-10L, NumberUtils.min(new long[]{-10L, 5L, -5L}));
        assertEquals(Long.MIN_VALUE, NumberUtils.min(new long[]{Long.MIN_VALUE, 0L, Long.MAX_VALUE}));
    }

    // ---------------------------------------------------- min array int[]

    @Test
    public void testMin_intArray() {
        assertEquals(2, NumberUtils.min(new int[]{5, 2, 9}));
        assertEquals(-100, NumberUtils.min(new int[]{-100, 0, -50}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMin_intArray_null() {
        NumberUtils.min((int[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMin_intArray_empty() {
        NumberUtils.min(new int[0]);
    }

    // --------------------------------------------------- min array short[]

    @Test
    public void testMin_shortArray() {
        assertEquals((short) 1, NumberUtils.min(new short[]{3, 1, 2}));
        assertEquals((short) -10, NumberUtils.min(new short[]{-10, 5, -5}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMin_shortArray_null() {
        NumberUtils.min((short[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMin_shortArray_empty() {
        NumberUtils.min(new short[0]);
    }

    // ---------------------------------------------------- min array byte[]

    @Test
    public void testMin_byteArray() {
        assertEquals((byte) 1, NumberUtils.min(new byte[]{3, 1, 2}));
        assertEquals((byte) -10, NumberUtils.min(new byte[]{-10, 5, -5}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMin_byteArray_null() {
        NumberUtils.min((byte[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMin_byteArray_empty() {
        NumberUtils.min(new byte[0]);
    }

    // -------------------------------------------------- min array double[]

    @Test
    public void testMin_doubleArray() {
        assertEquals(1.5d, NumberUtils.min(new double[]{3.5d, 1.5d, 2.5d}), 0d);
        assertEquals(Double.NaN, NumberUtils.min(new double[]{3.0d, Double.NaN, 1.0d}), 0d);
        assertEquals(-10.0d, NumberUtils.min(new double[]{-10.0d, 5.0d, -5.0d}), 0d);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMin_doubleArray_null() {
        NumberUtils.min((double[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMin_doubleArray_empty() {
        NumberUtils.min(new double[0]);
    }

    // --------------------------------------------------- min array float[]

    @Test
    public void testMin_floatArray() {
        assertEquals(1.5f, NumberUtils.min(new float[]{3.5f, 1.5f, 2.5f}), 0f);
        assertEquals(Float.NaN, NumberUtils.min(new float[]{3.0f, Float.NaN, 1.0f}), 0f);
        assertEquals(-10.0f, NumberUtils.min(new float[]{-10.0f, 5.0f, -5.0f}), 0f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMin_floatArray_null() {
        NumberUtils.min((float[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMin_floatArray_empty() {
        NumberUtils.min(new float[0]);
    }

    // ---------------------------------------------------- max array long[]

    @Test
    public void testMax_longArray() {
        assertEquals(9L, NumberUtils.max(new long[]{1L, 9L, 3L}));
        assertEquals(Long.MAX_VALUE, NumberUtils.max(new long[]{Long.MIN_VALUE, Long.MAX_VALUE, 0L}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMax_longArray_null() {
        NumberUtils.max((long[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMax_longArray_empty() {
        NumberUtils.max(new long[0]);
    }

    // ---------------------------------------------------- max array int[]

    @Test
    public void testMax_intArray() {
        assertEquals(9, NumberUtils.max(new int[]{1, 9, 3}));
    }

    // --------------------------------------------------- max array short[]

    @Test
    public void testMax_shortArray() {
        assertEquals((short) 9, NumberUtils.max(new short[]{1, 9, 3}));
    }

    // ---------------------------------------------------- max array byte[]

    @Test
    public void testMax_byteArray() {
        assertEquals((byte) 9, NumberUtils.max(new byte[]{1, 9, 3}));
    }

    // -------------------------------------------------- max array double[]

    @Test
    public void testMax_doubleArray() {
        assertEquals(9.5d, NumberUtils.max(new double[]{1.5d, 9.5d, 3.5d}), 0d);
        assertEquals(Double.NaN, NumberUtils.max(new double[]{3.0d, Double.NaN, 1.0d}), 0d);
    }

    // --------------------------------------------------- max array float[]

    @Test
    public void testMax_floatArray() {
        assertEquals(9.5f, NumberUtils.max(new float[]{1.5f, 9.5f, 3.5f}), 0f);
        assertEquals(Float.NaN, NumberUtils.max(new float[]{3.0f, Float.NaN, 1.0f}), 0f);
    }

    // ------------------------------------------------- three-param min long

    @Test
    public void testMin_long_threeArgs() {
        assertEquals(2L, NumberUtils.min(5L, 2L, 8L));
        assertEquals(-5L, NumberUtils.min(-5L, 10L, 0L));
        assertEquals(0L, NumberUtils.min(0L, 0L, 1L));
    }

    // ------------------------------------------------- three-param min int

    @Test
    public void testMin_int_threeArgs() {
        assertEquals(2, NumberUtils.min(5, 2, 8));
        assertEquals(-5, NumberUtils.min(-5, 10, 0));
    }

    // ------------------------------------------------- three-param min short

    @Test
    public void testMin_short_threeArgs() {
        assertEquals((short) 2, NumberUtils.min((short) 5, (short) 2, (short) 8));
        assertEquals((short) -5, NumberUtils.min((short) -5, (short) 10, (short) 0));
    }

    // ------------------------------------------------- three-param min byte

    @Test
    public void testMin_byte_threeArgs() {
        assertEquals((byte) 2, NumberUtils.min((byte) 5, (byte) 2, (byte) 8));
        assertEquals((byte) -5, NumberUtils.min((byte) -5, (byte) 10, (byte) 0));
    }

    // ------------------------------------------------- three-param min double

    @Test
    public void testMin_double_threeArgs() {
        assertEquals(2.5d, NumberUtils.min(5.5d, 2.5d, 8.0d), 0d);
        assertEquals(Double.NaN, NumberUtils.min(1.0d, Double.NaN, 3.0d), 0d);
        assertEquals(Double.NEGATIVE_INFINITY, NumberUtils.min(Double.NEGATIVE_INFINITY, 0d, Double.POSITIVE_INFINITY), 0d);
    }

    // ------------------------------------------------- three-param min float

    @Test
    public void testMin_float_threeArgs() {
        assertEquals(2.5f, NumberUtils.min(5.5f, 2.5f, 8.0f), 0f);
        assertEquals(Float.NaN, NumberUtils.min(1.0f, Float.NaN, 3.0f), 0f);
        assertEquals(Float.NEGATIVE_INFINITY, NumberUtils.min(Float.NEGATIVE_INFINITY, 0f, Float.POSITIVE_INFINITY), 0f);
    }

    // ------------------------------------------------- three-param max long

    @Test
    public void testMax_long_threeArgs() {
        assertEquals(8L, NumberUtils.max(5L, 2L, 8L));
        assertEquals(10L, NumberUtils.max(-5L, 10L, 0L));
    }

    // ------------------------------------------------- three-param max int

    @Test
    public void testMax_int_threeArgs() {
        assertEquals(8, NumberUtils.max(5, 2, 8));
        assertEquals(10, NumberUtils.max(-5, 10, 0));
    }

    // ------------------------------------------------- three-param max short

    @Test
    public void testMax_short_threeArgs() {
        assertEquals((short) 8, NumberUtils.max((short) 5, (short) 2, (short) 8));
        assertEquals((short) 10, NumberUtils.max((short) -5, (short) 10, (short) 0));
    }

    // ------------------------------------------------- three-param max byte

    @Test
    public void testMax_byte_threeArgs() {
        assertEquals((byte) 8, NumberUtils.max((byte) 5, (byte) 2, (byte) 8));
        assertEquals((byte) 10, NumberUtils.max((byte) -5, (byte) 10, (byte) 0));
    }

    // ------------------------------------------------- three-param max double

    @Test
    public void testMax_double_threeArgs() {
        assertEquals(8.5d, NumberUtils.max(5.5d, 2.5d, 8.5d), 0d);
        assertEquals(Double.NaN, NumberUtils.max(1.0d, Double.NaN, 3.0d), 0d);
        assertEquals(Double.POSITIVE_INFINITY, NumberUtils.max(Double.NEGATIVE_INFINITY, 0d, Double.POSITIVE_INFINITY), 0d);
    }

    // ------------------------------------------------- three-param max float

    @Test
    public void testMax_float_threeArgs() {
        assertEquals(8.5f, NumberUtils.max(5.5f, 2.5f, 8.5f), 0f);
        assertEquals(Float.NaN, NumberUtils.max(1.0f, Float.NaN, 3.0f), 0f);
        assertEquals(Float.POSITIVE_INFINITY, NumberUtils.max(Float.NEGATIVE_INFINITY, 0f, Float.POSITIVE_INFINITY), 0f);
    }

    // ----------------------------------------------------------- isDigits

    @Test
    public void testIsDigits() {
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits(""));
        assertTrue(NumberUtils.isDigits("12345"));
        assertFalse(NumberUtils.isDigits("12.34"));
        assertFalse(NumberUtils.isDigits("12a"));
        assertTrue(NumberUtils.isDigits("0000"));
    }

    // ----------------------------------------------------------- isNumber

    @Test
    public void testIsNumber() {
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));
        // standard numbers
        assertTrue(NumberUtils.isNumber("123"));
        assertTrue(NumberUtils.isNumber("-456"));
        // hex
        assertTrue(NumberUtils.isNumber("0x7f"));
        assertTrue(NumberUtils.isNumber("-0x7f"));
        assertFalse(NumberUtils.isNumber("0x"));
        // decimal
        assertTrue(NumberUtils.isNumber("1.5"));
        assertTrue(NumberUtils.isNumber("-2.3"));
        assertTrue(NumberUtils.isNumber(".5"));
        assertTrue(NumberUtils.isNumber("5."));
        assertFalse(NumberUtils.isNumber("..5"));
        assertFalse(NumberUtils.isNumber("1.5.6"));
        // exponent
        assertTrue(NumberUtils.isNumber("2e3"));
        assertTrue(NumberUtils.isNumber("-1.5e2"));
        assertTrue(NumberUtils.isNumber("1.5e-2"));
        assertFalse(NumberUtils.isNumber("e3")); // missing digit before e
        assertFalse(NumberUtils.isNumber("1e")); // missing digit after e
        assertFalse(NumberUtils.isNumber("1e-"));
        assertTrue(NumberUtils.isNumber("1e+2"));
        // type suffixes
        assertTrue(NumberUtils.isNumber("1.5F"));
        assertTrue(NumberUtils.isNumber("1D"));
        assertTrue(NumberUtils.isNumber("123L"));
        assertFalse(NumberUtils.isNumber("1.5L")); // L not allowed with decimal
        assertFalse(NumberUtils.isNumber("1.5e3L")); // L not allowed with exponent
        // hex edge cases
        assertTrue(NumberUtils.isNumber("-0x1A3F"));
        assertFalse(NumberUtils.isNumber("-0xGHI"));
        // some invalid
        assertFalse(NumberUtils.isNumber("--100"));
        assertFalse(NumberUtils.isNumber("12 34"));
    }

    // ------------------------------------------------- constant fields

    @Test
    public void testConstants() {
        assertEquals(Long.valueOf(0L), NumberUtils.LONG_ZERO);
        assertEquals(Long.valueOf(1L), NumberUtils.LONG_ONE);
        assertEquals(Long.valueOf(-1L), NumberUtils.LONG_MINUS_ONE);
        assertEquals(Integer.valueOf(0), NumberUtils.INTEGER_ZERO);
        assertEquals(Integer.valueOf(1), NumberUtils.INTEGER_ONE);
        assertEquals(Integer.valueOf(-1), NumberUtils.INTEGER_MINUS_ONE);
        assertEquals(Short.valueOf((short) 0), NumberUtils.SHORT_ZERO);
        assertEquals(Short.valueOf((short) 1), NumberUtils.SHORT_ONE);
        assertEquals(Short.valueOf((short) -1), NumberUtils.SHORT_MINUS_ONE);
        assertEquals(Byte.valueOf((byte) 0), NumberUtils.BYTE_ZERO);
        assertEquals(Byte.valueOf((byte) 1), NumberUtils.BYTE_ONE);
        assertEquals(Byte.valueOf((byte) -1), NumberUtils.BYTE_MINUS_ONE);
        assertEquals(Double.valueOf(0.0d), NumberUtils.DOUBLE_ZERO);
        assertEquals(Double.valueOf(1.0d), NumberUtils.DOUBLE_ONE);
        assertEquals(Double.valueOf(-1.0d), NumberUtils.DOUBLE_MINUS_ONE);
        assertEquals(Float.valueOf(0.0f), NumberUtils.FLOAT_ZERO);
        assertEquals(Float.valueOf(1.0f), NumberUtils.FLOAT_ONE);
        assertEquals(Float.valueOf(-1.0f), NumberUtils.FLOAT_MINUS_ONE);
    }
}
