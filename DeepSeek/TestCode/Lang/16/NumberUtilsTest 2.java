package org.apache.commons.lang3.math;

import static org.junit.Assert.*;
import org.junit.Test;
import java.math.BigDecimal;
import java.math.BigInteger;

public class NumberUtilsTest {

    // Constants tests
    @Test
    public void testConstants() {
        assertEquals(0L, NumberUtils.LONG_ZERO.longValue());
        assertEquals(1L, NumberUtils.LONG_ONE.longValue());
        assertEquals(-1L, NumberUtils.LONG_MINUS_ONE.longValue());

        assertEquals(0, NumberUtils.INTEGER_ZERO.intValue());
        assertEquals(1, NumberUtils.INTEGER_ONE.intValue());
        assertEquals(-1, NumberUtils.INTEGER_MINUS_ONE.intValue());

        assertEquals((short) 0, NumberUtils.SHORT_ZERO.shortValue());
        assertEquals((short) 1, NumberUtils.SHORT_ONE.shortValue());
        assertEquals((short) -1, NumberUtils.SHORT_MINUS_ONE.shortValue());

        assertEquals((byte) 0, NumberUtils.BYTE_ZERO.byteValue());
        assertEquals((byte) 1, NumberUtils.BYTE_ONE.byteValue());
        assertEquals((byte) -1, NumberUtils.BYTE_MINUS_ONE.byteValue());

        assertEquals(0.0d, NumberUtils.DOUBLE_ZERO.doubleValue(), 0.0d);
        assertEquals(1.0d, NumberUtils.DOUBLE_ONE.doubleValue(), 0.0d);
        assertEquals(-1.0d, NumberUtils.DOUBLE_MINUS_ONE.doubleValue(), 0.0d);

        assertEquals(0.0f, NumberUtils.FLOAT_ZERO.floatValue(), 0.0f);
        assertEquals(1.0f, NumberUtils.FLOAT_ONE.floatValue(), 0.0f);
        assertEquals(-1.0f, NumberUtils.FLOAT_MINUS_ONE.floatValue(), 0.0f);
    }

    @Test
    public void testConstructor() {
        new NumberUtils(); // just for coverage
    }

    // toInt tests
    @Test
    public void testToIntString() {
        assertEquals(0, NumberUtils.toInt(null));
        assertEquals(0, NumberUtils.toInt(""));
        assertEquals(0, NumberUtils.toInt("  "));
        assertEquals(123, NumberUtils.toInt("123"));
        assertEquals(-456, NumberUtils.toInt("-456"));
        assertEquals(0, NumberUtils.toInt("+789"));
        assertEquals(0, NumberUtils.toInt("12.3"));
        assertEquals(0, NumberUtils.toInt("abc"));
        assertEquals(0, NumberUtils.toInt("2147483648")); // overflow
    }

    @Test
    public void testToIntStringDefault() {
        assertEquals(10, NumberUtils.toInt(null, 10));
        assertEquals(10, NumberUtils.toInt("", 10));
        assertEquals(10, NumberUtils.toInt("  ", 10));
        assertEquals(123, NumberUtils.toInt("123", 10));
        assertEquals(-456, NumberUtils.toInt("-456", 10));
        assertEquals(10, NumberUtils.toInt("abc", 10));
    }

    // toLong tests
    @Test
    public void testToLongString() {
        assertEquals(0L, NumberUtils.toLong(null));
        assertEquals(0L, NumberUtils.toLong(""));
        assertEquals(123L, NumberUtils.toLong("123"));
        assertEquals(-456L, NumberUtils.toLong("-456"));
        assertEquals(0L, NumberUtils.toLong("abc"));
        assertEquals(0L, NumberUtils.toLong("9223372036854775808")); // overflow
    }

    @Test
    public void testToLongStringDefault() {
        assertEquals(99L, NumberUtils.toLong(null, 99L));
        assertEquals(99L, NumberUtils.toLong("", 99L));
        assertEquals(123L, NumberUtils.toLong("123", 99L));
    }

    // toFloat tests
    @Test
    public void testToFloatString() {
        assertEquals(0.0f, NumberUtils.toFloat(null), 0.0f);
        assertEquals(0.0f, NumberUtils.toFloat(""), 0.0f);
        assertEquals(1.5f, NumberUtils.toFloat("1.5"), 0.0f);
        assertEquals(-2.3f, NumberUtils.toFloat("-2.3"), 0.0f);
        assertEquals(0.0f, NumberUtils.toFloat("abc"), 0.0f);
    }

    @Test
    public void testToFloatStringDefault() {
        assertEquals(1.1f, NumberUtils.toFloat(null, 1.1f), 0.0f);
        assertEquals(1.1f, NumberUtils.toFloat("", 1.1f), 0.0f);
        assertEquals(1.5f, NumberUtils.toFloat("1.5", 0.0f), 0.0f);
    }

    // toDouble tests
    @Test
    public void testToDoubleString() {
        assertEquals(0.0d, NumberUtils.toDouble(null), 0.0d);
        assertEquals(0.0d, NumberUtils.toDouble(""), 0.0d);
        assertEquals(1.5d, NumberUtils.toDouble("1.5"), 0.0d);
        assertEquals(-2.3d, NumberUtils.toDouble("-2.3"), 0.0d);
        assertEquals(0.0d, NumberUtils.toDouble("abc"), 0.0d);
    }

    @Test
    public void testToDoubleStringDefault() {
        assertEquals(1.1d, NumberUtils.toDouble(null, 1.1d), 0.0d);
        assertEquals(1.1d, NumberUtils.toDouble("", 1.1d), 0.0d);
        assertEquals(1.5d, NumberUtils.toDouble("1.5", 0.0d), 0.0d);
    }

    // toByte tests
    @Test
    public void testToByteString() {
        assertEquals(0, NumberUtils.toByte(null));
        assertEquals(0, NumberUtils.toByte(""));
        assertEquals(1, NumberUtils.toByte("1"));
        assertEquals(-1, NumberUtils.toByte("-1"));
        assertEquals(0, NumberUtils.toByte("128")); // overflow
        assertEquals(0, NumberUtils.toByte("abc"));
    }

    @Test
    public void testToByteStringDefault() {
        assertEquals((byte) 5, NumberUtils.toByte(null, (byte) 5));
        assertEquals((byte) 5, NumberUtils.toByte("", (byte) 5));
        assertEquals((byte) 10, NumberUtils.toByte("10", (byte) 5));
        assertEquals((byte) 5, NumberUtils.toByte("abc", (byte) 5));
    }

    // toShort tests
    @Test
    public void testToShortString() {
        assertEquals(0, NumberUtils.toShort(null));
        assertEquals(0, NumberUtils.toShort(""));
        assertEquals(1, NumberUtils.toShort("1"));
        assertEquals(-1, NumberUtils.toShort("-1"));
        assertEquals(0, NumberUtils.toShort("32768")); // overflow
        assertEquals(0, NumberUtils.toShort("abc"));
    }

    @Test
    public void testToShortStringDefault() {
        assertEquals((short) 5, NumberUtils.toShort(null, (short) 5));
        assertEquals((short) 5, NumberUtils.toShort("", (short) 5));
        assertEquals((short) 10, NumberUtils.toShort("10", (short) 5));
        assertEquals((short) 5, NumberUtils.toShort("abc", (short) 5));
    }

    // createNumber tests
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
        assertNull(NumberUtils.createNumber("--10"));
    }

    @Test
    public void testCreateNumberHex() {
        assertEquals(0xABC, NumberUtils.createNumber("0xABC").intValue());
        assertEquals(0xabc, NumberUtils.createNumber("0xabc").intValue());
        assertEquals(-0xABC, NumberUtils.createNumber("-0xABC").intValue());
        assertEquals(-0xabc, NumberUtils.createNumber("-0xabc").intValue());
        assertEquals(0x0, NumberUtils.createNumber("0x0").intValue());
        assertEquals(0xF, NumberUtils.createNumber("0xF").intValue());
    }

    @Test
    public void testCreateNumberIntegers() {
        assertEquals(Integer.valueOf(123), NumberUtils.createNumber("123"));
        assertEquals(Integer.valueOf(-456), NumberUtils.createNumber("-456"));
        assertEquals(Integer.valueOf(0), NumberUtils.createNumber("0"));
        // octal
        assertEquals(Integer.valueOf(8), NumberUtils.createNumber("010"));
        // hex via decode
        assertEquals(Integer.valueOf(0x1F), NumberUtils.createNumber("0x1F"));
    }

    @Test
    public void testCreateNumberLong() {
        assertEquals(Long.valueOf(2147483648L), NumberUtils.createNumber("2147483648"));
        assertEquals(Long.valueOf(-2147483649L), NumberUtils.createNumber("-2147483649"));
        assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123L"));
        assertEquals(Long.valueOf(-456L), NumberUtils.createNumber("-456L"));
        assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123l"));
    }

    @Test
    public void testCreateNumberFloat() {
        assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23f"));
        assertEquals(Float.valueOf(-4.56f), NumberUtils.createNumber("-4.56F"));
        assertTrue(NumberUtils.createNumber("1.23F") instanceof Float);
        // too big for float, but within double -> fall through to double
        Number n = NumberUtils.createNumber("3.4028235E38f");
        assertTrue("Expected Double for large float", n instanceof Double);
    }

    @Test
    public void testCreateNumberDouble() {
        assertEquals(Double.valueOf(1.23), NumberUtils.createNumber("1.23d"));
        assertEquals(Double.valueOf(-4.56), NumberUtils.createNumber("-4.56D"));
        assertEquals(Double.valueOf(1.23), NumberUtils.createNumber("1.23"));
        assertEquals(Double.valueOf(0.0), NumberUtils.createNumber("0.0"));
    }

    @Test
    public void testCreateNumberBigDecimal() {
        assertTrue(NumberUtils.createNumber("1.23E+25") instanceof BigDecimal);
        assertTrue(NumberUtils.createNumber("12345678901234567890.123") instanceof BigDecimal);
        assertTrue(NumberUtils.createNumber("1.23E25d") instanceof BigDecimal);
    }

    @Test
    public void testCreateNumberBigInteger() {
        Number n = NumberUtils.createNumber("123456789012345678901234567890");
        assertTrue(n instanceof BigInteger);
        assertEquals(new BigInteger("123456789012345678901234567890"), n);
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidLWithDecimal() {
        NumberUtils.createNumber("1.2L");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidLWithExponent() {
        NumberUtils.createNumber("1E2L");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidQualifier() {
        NumberUtils.createNumber("123X");
    }

    @Test
    public void testCreateNumberScientific() {
        Number n = NumberUtils.createNumber("1.23E10");
        assertEquals(Double.valueOf(1.23E10), n);
        n = NumberUtils.createNumber("-4.56e-2");
        assertEquals(Double.valueOf(-0.0456), n);
    }

    @Test
    public void testCreateNumberNegativeZero() {
        assertEquals(Float.valueOf(-0.0f), NumberUtils.createNumber("-0.0f"));
    }

    @Test
    public void testCreateNumberAllZerosCheck() {
        // 0F with all zeros should return Float
        Number n = NumberUtils.createNumber("0F");
        assertTrue(n instanceof Float);
        assertEquals(0.0f, n.floatValue(), 0.0f);
        // -0D
        n = NumberUtils.createNumber("-0.0D");
        assertTrue(n instanceof Double);
        assertEquals(0.0, n.doubleValue(), 0.0d);
    }

    // createFloat tests
    @Test
    public void testCreateFloat() {
        assertNull(NumberUtils.createFloat(null));
        assertEquals(Float.valueOf(1.5f), NumberUtils.createFloat("1.5"));
        assertEquals(Float.valueOf(-2.3f), NumberUtils.createFloat("-2.3"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateFloatInvalid() {
        NumberUtils.createFloat("abc");
    }

    // createDouble tests
    @Test
    public void testCreateDouble() {
        assertNull(NumberUtils.createDouble(null));
        assertEquals(Double.valueOf(1.5), NumberUtils.createDouble("1.5"));
        assertEquals(Double.valueOf(-2.3), NumberUtils.createDouble("-2.3"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateDoubleInvalid() {
        NumberUtils.createDouble("abc");
    }

    // createInteger tests
    @Test
    public void testCreateInteger() {
        assertNull(NumberUtils.createInteger(null));
        assertEquals(Integer.valueOf(123), NumberUtils.createInteger("123"));
        assertEquals(Integer.valueOf(-456), NumberUtils.createInteger("-456"));
        assertEquals(Integer.valueOf(0x1F), NumberUtils.createInteger("0x1F"));
        assertEquals(Integer.valueOf(8), NumberUtils.createInteger("010")); // octal
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateIntegerInvalid() {
        NumberUtils.createInteger("abc");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateIntegerBlank() {
        NumberUtils.createInteger("");
    }

    // createLong tests
    @Test
    public void testCreateLong() {
        assertNull(NumberUtils.createLong(null));
        assertEquals(Long.valueOf(123L), NumberUtils.createLong("123"));
        assertEquals(Long.valueOf(-456L), NumberUtils.createLong("-456"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateLongInvalid() {
        NumberUtils.createLong("abc");
    }

    // createBigInteger tests
    @Test
    public void testCreateBigInteger() {
        assertNull(NumberUtils.createBigInteger(null));
        assertEquals(new BigInteger("123"), NumberUtils.createBigInteger("123"));
        assertEquals(new BigInteger("-456"), NumberUtils.createBigInteger("-456"));
        assertEquals(BigInteger.ZERO, NumberUtils.createBigInteger("0"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigIntegerInvalid() {
        NumberUtils.createBigInteger("abc");
    }

    // createBigDecimal tests
    @Test
    public void testCreateBigDecimal() {
        assertNull(NumberUtils.createBigDecimal(null));
        assertEquals(new BigDecimal("123.45"), NumberUtils.createBigDecimal("123.45"));
        assertEquals(new BigDecimal("-456.78"), NumberUtils.createBigDecimal("-456.78"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigDecimalBlank() {
        NumberUtils.createBigDecimal("");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigDecimalInvalid() {
        NumberUtils.createBigDecimal("abc");
    }

    // isDigits tests
    @Test
    public void testIsDigits() {
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits(""));
        assertFalse(NumberUtils.isDigits(" "));
        assertTrue(NumberUtils.isDigits("12345"));
        assertFalse(NumberUtils.isDigits("1234a"));
        assertFalse(NumberUtils.isDigits("12.34"));
        assertFalse(NumberUtils.isDigits("-123"));
    }

    // isNumber tests
    @Test
    public void testIsNumber() {
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));
        assertFalse(NumberUtils.isNumber(" "));
        assertFalse(NumberUtils.isNumber("0x"));
        assertFalse(NumberUtils.isNumber("-0x"));
        assertTrue(NumberUtils.isNumber("0x1F"));
        assertTrue(NumberUtils.isNumber("-0x1F"));
        assertTrue(NumberUtils.isNumber("0xABCD"));
        assertTrue(NumberUtils.isNumber("0x0"));
        assertFalse(NumberUtils.isNumber("0xG")); // invalid hex
        assertFalse(NumberUtils.isNumber("0x123G")); // invalid char
        assertFalse(NumberUtils.isNumber("--10"));
        assertTrue(NumberUtils.isNumber("123"));
        assertTrue(NumberUtils.isNumber("-123"));
        assertTrue(NumberUtils.isNumber("0"));
        assertTrue(NumberUtils.isNumber("123.45"));
        assertTrue(NumberUtils.isNumber("-123.45"));
        assertTrue(NumberUtils.isNumber(".45"));
        assertTrue(NumberUtils.isNumber("123."));
        assertTrue(NumberUtils.isNumber("123e5"));
        assertTrue(NumberUtils.isNumber("123E5"));
        assertTrue(NumberUtils.isNumber("-123e-5"));
        assertTrue(NumberUtils.isNumber("123E+5"));
        assertTrue(NumberUtils.isNumber("123.45e10"));
        assertFalse(NumberUtils.isNumber("123.."));
        assertFalse(NumberUtils.isNumber("123e"));
        assertFalse(NumberUtils.isNumber("123e."));
        assertFalse(NumberUtils.isNumber("123e+"));
        assertFalse(NumberUtils.isNumber("e10"));
        assertFalse(NumberUtils.isNumber(".e10"));
        assertFalse(NumberUtils.isNumber("123e12.5")); // decimal in exponent invalid
        assertTrue(NumberUtils.isNumber("123L"));
        assertTrue(NumberUtils.isNumber("123l"));
        assertTrue(NumberUtils.isNumber("123D"));
        assertTrue(NumberUtils.isNumber("123d"));
        assertTrue(NumberUtils.isNumber("123F"));
        assertTrue(NumberUtils.isNumber("123f"));
        assertFalse(NumberUtils.isNumber("123Lf"));
        assertFalse(NumberUtils.isNumber("12.3L"));
        assertFalse(NumberUtils.isNumber("12e3L"));
        assertFalse(NumberUtils.isNumber("123X"));
        assertTrue(NumberUtils.isNumber("+123"));
        assertTrue(NumberUtils.isNumber("+123.45"));
        assertFalse(NumberUtils.isNumber("+123e"));
        assertFalse(NumberUtils.isNumber("+123e+"));
        assertTrue(NumberUtils.isNumber("+123e5"));
        assertFalse(NumberUtils.isNumber("+123e+5E")); // double E
        assertFalse(NumberUtils.isNumber("123.45e10.5")); // dot in exponent
        assertFalse(NumberUtils.isNumber("")); // empty
    }

    // Array min tests
    @Test
    public void testMinLongArray() {
        assertEquals(1L, NumberUtils.min(new long[]{5, 3, 1, 4}));
        assertEquals(-1L, NumberUtils.min(new long[]{-1, 0, 1}));
        assertEquals(1L, NumberUtils.min(new long[]{1}));
        assertEquals(Long.MIN_VALUE, NumberUtils.min(new long[]{Long.MIN_VALUE, 0}));
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
        assertEquals(1, NumberUtils.min(new int[]{5, 3, 1, 4}));
        assertEquals(-1, NumberUtils.min(new int[]{-1, 0, 1}));
        assertEquals(1, NumberUtils.min(new int[]{1}));
        assertEquals(Integer.MIN_VALUE, NumberUtils.min(new int[]{Integer.MIN_VALUE, 0}));
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
        assertEquals(1, NumberUtils.min(new short[]{5, 3, 1, 4}));
        assertEquals(-1, NumberUtils.min(new short[]{-1, 0, 1}));
        assertEquals(1, NumberUtils.min(new short[]{1}));
        assertEquals(Short.MIN_VALUE, NumberUtils.min(new short[]{Short.MIN_VALUE, 0}));
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
        assertEquals(1, NumberUtils.min(new byte[]{5, 3, 1, 4}));
        assertEquals(-1, NumberUtils.min(new byte[]{-1, 0, 1}));
        assertEquals(1, NumberUtils.min(new byte[]{1}));
        assertEquals(Byte.MIN_VALUE, NumberUtils.min(new byte[]{Byte.MIN_VALUE, 0}));
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
        assertEquals(1.5, NumberUtils.min(new double[]{5.5, 2.1, 1.5, 4.0}), 0.0));
        assertEquals(-1.2, NumberUtils.min(new double[]{-1.2, 0.0, 1.5}), 0.0));
        assertEquals(Double.NaN, NumberUtils.min(new double[]{1.0, Double.NaN, 2.0}), 0.0)); // NaN returns NaN
        double[] single = {1.1};
        assertEquals(1.1, NumberUtils.min(single), 0.0));
        assertEquals(Double.NEGative_INFINITY, NumberUtils.min(new double[]{Double.NEGative_INFINITY, 0.0}), 0.0));
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
        assertEquals(1.5f, NumberUtils.min(new float[]{5.5f, 2.1f, 1.5f, 4.0f}), 0.0f));
        assertEquals(-1.2f, NumberUtils.min(new float[]{-1.2f, 0.0f, 1.5f}), 0.0f));
        assertEquals(Float.NaN, NumberUtils.min(new float[]{1.0f, Float.NaN, 2.0f}), 0.0f));
        float[] single = {1.1f};
        assertEquals(1.1f, NumberUtils.min(single), 0.0f));
        assertEquals(Float.NEGative_INFINITY, NumberUtils.min(new float[]{Float.NEGative_INFINITY, 0.0f}), 0.0f));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinFloatArrayNull() {
        NumberUtils.min((float[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinFloatArrayEmpty() {
        NumberUtils.min(new float[0]);
    }

    // Array max tests
    @Test
    public void testMaxLongArray() {
        assertEquals(5L, NumberUtils.max(new long[]{5, 3, 1, 4}));
        assertEquals(1L, NumberUtils.max(new long[]{-1, 0, 1}));
        assertEquals(1L, NumberUtils.max(new long[]{1}));
        assertEquals(Long.MAX_VALUE, NumberUtils.max(new long[]{Long.MAX_VALUE, 0}));
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
        assertEquals(5, NumberUtils.max(new int[]{5, 3, 1, 4}));
        assertEquals(1, NumberUtils.max(new int[]{-1, 0, 1}));
        assertEquals(1, NumberUtils.max(new int[]{1}));
        assertEquals(Integer.MAX_VALUE, NumberUtils.max(new int[]{Integer.MAX_VALUE, 0}));
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
        assertEquals(5, NumberUtils.max(new short[]{5, 3, 1, 4}));
        assertEquals(1, NumberUtils.max(new short[]{-1, 0, 1}));
        assertEquals(1, NumberUtils.max(new short[]{1}));
        assertEquals(Short.MAX_VALUE, NumberUtils.max(new short[]{Short.MAX_VALUE, 0}));
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
        assertEquals(5, NumberUtils.max(new byte[]{5, 3, 1, 4}));
        assertEquals(1, NumberUtils.max(new byte[]{-1, 0, 1}));
        assertEquals(1, NumberUtils.max(new byte[]{1}));
        assertEquals(Byte.MAX_VALUE, NumberUtils.max(new byte[]{Byte.MAX_VALUE, 0}));
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
        assertEquals(5.5, NumberUtils.max(new double[]{5.5, 2.1, 1.5, 4.0}), 0.0));
        assertEquals(1.5, NumberUtils.max(new double[]{-1.2, 0.0, 1.5}), 0.0));
        assertEquals(Double.NaN, NumberUtils.max(new double[]{1.0, Double.NaN, 2.0}), 0.0));
        double[] single = {1.1};
        assertEquals(1.1, NumberUtils.max(single), 0.0));
        assertEquals(Double.POSITIVE_INFINITY, NumberUtils.max(new double[]{Double.POSITIVE_INFINITY, 0.0}), 0.0));
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
        assertEquals(5.5f, NumberUtils.max(new float[]{5.5f, 2.1f, 1.5f, 4.0f}), 0.0f));
        assertEquals(1.5f, NumberUtils.max(new float[]{-1.2f, 0.0f, 1.5f}), 0.0f));
        assertEquals(Float.NaN, NumberUtils.max(new float[]{1.0f, Float.NaN, 2.0f}), 0.0f));
        float[] single = {1.1f};
        assertEquals(1.1f, NumberUtils.max(single), 0.0f));
        assertEquals(Float.POSITIVE_INFINITY, NumberUtils.max(new float[]{Float.POSITIVE_INFINITY, 0.0f}), 0.0f));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxFloatArrayNull() {
        NumberUtils.max((float[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxFloatArrayEmpty() {
        NumberUtils.max(new float[0]);
    }

    // 3-param min tests
    @Test
    public void testMinLong() {
        assertEquals(1L, NumberUtils.min(5L, 1L, 4L));
        assertEquals(-5L, NumberUtils.min(-5L, 0L, 100L));
        assertEquals(Long.MIN_VALUE, NumberUtils.min(Long.MIN_VALUE, 0L, 100L));
    }

    @Test
    public void testMinInt() {
        assertEquals(1, NumberUtils.min(5, 1, 4));
        assertEquals(-5, NumberUtils.min(-5, 0, 100));
        assertEquals(Integer.MIN_VALUE, NumberUtils.min(Integer.MIN_VALUE, 0, 100));
    }

    @Test
    public void testMinShort() {
        assertEquals((short)1, NumberUtils.min((short)5, (short)1, (short)4));
        assertEquals((short)-5, NumberUtils.min((short)-5, (short)0, (short)100));
        assertEquals(Short.MIN_VALUE, NumberUtils.min(Short.MIN_VALUE, (short)0, (short)100));
    }

    @Test
    public void testMinByte() {
        assertEquals((byte)1, NumberUtils.min((byte)5, (byte)1, (byte)4));
        assertEquals((byte)-5, NumberUtils.min((byte)-5, (byte)0, (byte)100));
        assertEquals(Byte.MIN_VALUE, NumberUtils.min(Byte.MIN_VALUE, (byte)0, (byte)100));
    }

    @Test
    public void testMinDouble() {
        assertEquals(1.0, NumberUtils.min(5.0, 1.0, 4.0), 0.0));
        assertEquals(-5.0, NumberUtils.min(-5.0, 0.0, 100.0), 0.0));
        assertEquals(Double.NaN, NumberUtils.min(1.0, Double.NaN, 2.0), 0.0));
    }

    @Test
    public void testMinFloat() {
        assertEquals(1.0f, NumberUtils.min(5.0f, 1.0f, 4.0f), 0.0f));
        assertEquals(-5.0f, NumberUtils.min(-5.0f, 0.0f, 100.0f), 0.0f));
        assertEquals(Float.NaN, NumberUtils.min(1.0f, Float.NaN, 2.0f), 0.0f));
    }

    // 3-param max tests
    @Test
    public void testMaxLong() {
        assertEquals(5L, NumberUtils.max(5L, 1L, 4L));
        assertEquals(100L, NumberUtils.max(-5L, 0L, 100L));
        assertEquals(Long.MAX_VALUE, NumberUtils.max(Long.MAX_VALUE, 0L, 100L));
    }

    @Test
    public void testMaxInt() {
        assertEquals(5, NumberUtils.max(5, 1, 4));
        assertEquals(100, NumberUtils.max(-5, 0, 100));
        assertEquals(Integer.MAX_VALUE, NumberUtils.max(Integer.MAX_VALUE, 0, 100));
    }

    @Test
    public void testMaxShort() {
        assertEquals((short)5, NumberUtils.max((short)5, (short)1, (short)4));
        assertEquals((short)100, NumberUtils.max((short)-5, (short)0, (short)100));
        assertEquals(Short.MAX_VALUE, NumberUtils.max(Short.MAX_VALUE, (short)0, (short)100));
    }

    @Test
    public void testMaxByte() {
        assertEquals((byte)5, NumberUtils.max((byte)5, (byte)1, (byte)4));
        assertEquals((byte)100, NumberUtils.max((byte)-5, (byte)0, (byte)100));
        assertEquals(Byte.MAX_VALUE, NumberUtils.max(Byte.MAX_VALUE, (byte)0, (byte)100));
    }

    @Test
    public void testMaxDouble() {
        assertEquals(5.0, NumberUtils.max(5.0, 1.0, 4.0), 0.0));
        assertEquals(100.0, NumberUtils.max(-5.0, 0.0, 100.0), 0.0));
        assertEquals(Double.NaN, NumberUtils.max(1.0, Double.NaN, 2.0), 0.0));
    }

    @Test
    public void testMaxFloat() {
        assertEquals(5.0f, NumberUtils.max(5.0f, 1.0f, 4.0f), 0.0f));
        assertEquals(100.0f, NumberUtils.max(-5.0f, 0.0f, 100.0f), 0.0f));
        assertEquals(Float.NaN, NumberUtils.max(1.0f, Float.NaN, 2.0f), 0.0f));
    }
}
