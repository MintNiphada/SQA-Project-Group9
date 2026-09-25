package org.apache.commons.lang3.math;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class NumberUtilsTest {

    private static final double DELTA_DOUBLE = 0.0001d;
    private static final float DELTA_FLOAT = 0.0001f;

    @Test
    public void testConstructor() {
        assertNotNull(new NumberUtils());
    }

    @Test
    public void testConstants() {
        assertEquals(Long.valueOf(0L), NumberUtils.LONG_ZERO);
        assertEquals(Long.valueOf(1L), NumberUtils.LONG_ONE);
        assertEquals(Long.valueOf(-1L), NumberUtils.LONG_MINUS_ONE);
        assertEquals(Integer.valueOf(0), NumberUtils.INTEGER_ZERO);
        assertEquals(Integer.valueOf(1), NumberUtils.INTEGER_ONE);
        assertEquals(Integer.valueOf(2), NumberUtils.INTEGER_TWO);
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
        assertEquals(Long.valueOf(Integer.MAX_VALUE), NumberUtils.LONG_INT_MAX_VALUE);
        assertEquals(Long.valueOf(Integer.MIN_VALUE), NumberUtils.LONG_INT_MIN_VALUE);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testCompareByte() {
        assertEquals(0, NumberUtils.compare((byte) 5, (byte) 5));
        assertTrue(NumberUtils.compare((byte) 3, (byte) 5) < 0);
        assertTrue(NumberUtils.compare((byte) 5, (byte) 3) > 0);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testCompareInt() {
        assertEquals(0, NumberUtils.compare(10, 10));
        assertTrue(NumberUtils.compare(5, 10) < 0);
        assertTrue(NumberUtils.compare(15, 10) > 0);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testCompareLong() {
        assertEquals(0, NumberUtils.compare(100L, 100L));
        assertTrue(NumberUtils.compare(50L, 100L) < 0);
        assertTrue(NumberUtils.compare(150L, 100L) > 0);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testCompareShort() {
        assertEquals(0, NumberUtils.compare((short) 10, (short) 10));
        assertTrue(NumberUtils.compare((short) 5, (short) 10) < 0);
        assertTrue(NumberUtils.compare((short) 15, (short) 10) > 0);
    }

    @Test
    public void testCreateBigDecimal() {
        assertNull(NumberUtils.createBigDecimal(null));
        assertEquals(new BigDecimal("123.456"), NumberUtils.createBigDecimal("123.456"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigDecimalEmpty() {
        NumberUtils.createBigDecimal("");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigDecimalBlank() {
        NumberUtils.createBigDecimal("   ");
    }

    @Test
    public void testCreateBigInteger() {
        assertNull(NumberUtils.createBigInteger(null));
        assertEquals(new BigInteger("12345678901234567890"), NumberUtils.createBigInteger("12345678901234567890"));
        assertEquals(new BigInteger("+12345"), NumberUtils.createBigInteger("+12345"));
        assertEquals(new BigInteger("-12345"), NumberUtils.createBigInteger("-12345"));

        assertEquals(new BigInteger("ff", 16), NumberUtils.createBigInteger("0xff"));
        assertEquals(new BigInteger("FF", 16), NumberUtils.createBigInteger("0XFF"));
        assertEquals(new BigInteger("-ff", 16), NumberUtils.createBigInteger("-0xff"));
        assertEquals(new BigInteger("ff", 16), NumberUtils.createBigInteger("#ff"));
        assertEquals(new BigInteger("-ff", 16), NumberUtils.createBigInteger("-#ff"));

        assertEquals(new BigInteger("77", 8), NumberUtils.createBigInteger("077"));
        assertEquals(new BigInteger("-77", 8), NumberUtils.createBigInteger("-077"));
        assertEquals(BigInteger.ZERO, NumberUtils.createBigInteger("0"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigIntegerEmpty() {
        NumberUtils.createBigInteger("");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigIntegerDoubleSignNegative() {
        NumberUtils.createBigInteger("--1");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigIntegerDoubleSignPositive() {
        NumberUtils.createBigInteger("++1");
    }

    @Test
    public void testCreateDouble() {
        assertNull(NumberUtils.createDouble(null));
        assertEquals(Double.valueOf(1.234d), NumberUtils.createDouble("1.234"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateDoubleInvalid() {
        NumberUtils.createDouble("invalid");
    }

    @Test
    public void testCreateFloat() {
        assertNull(NumberUtils.createFloat(null));
        assertEquals(Float.valueOf(1.234f), NumberUtils.createFloat("1.234"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateFloatInvalid() {
        NumberUtils.createFloat("invalid");
    }

    @Test
    public void testCreateInteger() {
        assertNull(NumberUtils.createInteger(null));
        assertEquals(Integer.valueOf(1234), NumberUtils.createInteger("1234"));
        assertEquals(Integer.valueOf(0x1a), NumberUtils.createInteger("0x1a"));
        assertEquals(Integer.valueOf(012), NumberUtils.createInteger("012"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateIntegerInvalid() {
        NumberUtils.createInteger("abc");
    }

    @Test
    public void testCreateLong() {
        assertNull(NumberUtils.createLong(null));
        assertEquals(Long.valueOf(1234567890L), NumberUtils.createLong("1234567890"));
        assertEquals(Long.valueOf(0x1aL), NumberUtils.createLong("0X1a"));
        assertEquals(Long.valueOf(012L), NumberUtils.createLong("012"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateLongInvalid() {
        NumberUtils.createLong("invalid");
    }

    @Test
    public void testCreateNumber() {
        assertNull(NumberUtils.createNumber(null));

        // Hexadecimal
        assertEquals(Integer.valueOf(0x12), NumberUtils.createNumber("0x12"));
        assertEquals(Integer.valueOf(0x12), NumberUtils.createNumber("0X12"));
        assertEquals(Integer.valueOf(0x12), NumberUtils.createNumber("#12"));
        assertEquals(Integer.valueOf(-0x12), NumberUtils.createNumber("-0x12"));
        assertEquals(Integer.valueOf(-0x12), NumberUtils.createNumber("-#12"));
        assertEquals(Integer.valueOf(0x12), NumberUtils.createNumber("+0x12"));

        assertEquals(Integer.valueOf(0x00012), NumberUtils.createNumber("0x00012"));
        assertEquals(Long.valueOf(0x12L), NumberUtils.createNumber("0x12L"));
        assertEquals(Long.valueOf(0x12L), NumberUtils.createNumber("0x12l"));
        assertEquals(Long.valueOf(0x80000000L), NumberUtils.createNumber("0x80000000"));
        assertEquals(new BigInteger("8000000000000000", 16), NumberUtils.createNumber("0x8000000000000000"));
        assertEquals(new BigInteger("8000000000000000", 16), NumberUtils.createNumber("0x8000000000000000L"));

        // Type qualifiers: 'l' or 'L'
        assertEquals(Long.valueOf(1234L), NumberUtils.createNumber("1234l"));
        assertEquals(Long.valueOf(1234L), NumberUtils.createNumber("1234L"));
        assertEquals(Long.valueOf(-1234L), NumberUtils.createNumber("-1234L"));
        assertEquals(new BigInteger("123456789012345678901234567890"), NumberUtils.createNumber("123456789012345678901234567890L"));

        // Type qualifiers: 'f' or 'F'
        assertEquals(Float.valueOf(1.234f), NumberUtils.createNumber("1.234f"));
        assertEquals(Float.valueOf(1.234f), NumberUtils.createNumber("1.234F"));
        assertEquals(Float.valueOf(0.0f), NumberUtils.createNumber("0.0f"));
        assertEquals(Double.valueOf(1e-40d), (Double) NumberUtils.createNumber("1e-40f"), DELTA_DOUBLE);

        // Type qualifiers: 'd' or 'D'
        assertEquals(Double.valueOf(1.234d), NumberUtils.createNumber("1.234d"));
        assertEquals(Double.valueOf(1.234d), NumberUtils.createNumber("1.234D"));
        assertEquals(Double.valueOf(0.0d), NumberUtils.createNumber("0.0d"));
        assertEquals(new BigDecimal("1e-400"), NumberUtils.createNumber("1e-400d"));

        // No type qualifier
        assertEquals(Integer.valueOf(123), NumberUtils.createNumber("123"));
        assertEquals(Integer.valueOf(-123), NumberUtils.createNumber("-123"));
        assertEquals(Long.valueOf(2147483648L), NumberUtils.createNumber("2147483648"));
        assertEquals(new BigInteger("9223372036854775808"), NumberUtils.createNumber("9223372036854775808"));

        // Decimals and Exponents without qualifier
        assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23"));
        assertEquals(Double.valueOf(1.234567890123d), (Double) NumberUtils.createNumber("1.234567890123"), DELTA_DOUBLE);
        assertEquals(new BigDecimal("1.23456789012345678901234567890"), NumberUtils.createNumber("1.23456789012345678901234567890"));
        assertEquals(Float.valueOf(1.23e4f), NumberUtils.createNumber("1.23e4"));
        assertEquals(Float.valueOf(1.23e-4f), NumberUtils.createNumber("1.23e-4"));
        assertEquals(Double.valueOf(1.23e30d), NumberUtils.createNumber("1.23e30"));
        assertEquals(Float.valueOf(12e3f), NumberUtils.createNumber("12e3"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberBlank() {
        NumberUtils.createNumber("   ");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidSignOnly() {
        NumberUtils.createNumber("-");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidExponentOrder() {
        NumberUtils.createNumber("1e.2");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberDoubleExponent() {
        NumberUtils.createNumber("1e2e3");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberTrailingQualifierLongWithDecimal() {
        NumberUtils.createNumber("12.34L");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidQualifier() {
        NumberUtils.createNumber("123z");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberBadFloatFormat() {
        NumberUtils.createNumber("xyzF");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberBadDoubleFormat() {
        NumberUtils.createNumber("xyzD");
    }

    @Test
    public void testIsCreatable() {
        assertFalse(NumberUtils.isCreatable(null));
        assertFalse(NumberUtils.isCreatable(""));
        assertFalse(NumberUtils.isCreatable("   "));
        assertFalse(NumberUtils.isCreatable("abc"));
        assertFalse(NumberUtils.isCreatable("1.2.3"));

        assertTrue(NumberUtils.isCreatable("123"));
        assertTrue(NumberUtils.isCreatable("-123"));
        assertTrue(NumberUtils.isCreatable("+123"));
        assertTrue(NumberUtils.isCreatable("12.34"));
        assertTrue(NumberUtils.isCreatable("1.23e-4"));
        assertTrue(NumberUtils.isCreatable("0x12a"));
        assertTrue(NumberUtils.isCreatable("0X12A"));
        assertTrue(NumberUtils.isCreatable("#12A"));
        assertTrue(NumberUtils.isCreatable("1234L"));
        assertTrue(NumberUtils.isCreatable("12.34f"));
        assertTrue(NumberUtils.isCreatable("12.34d"));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testIsNumber() {
        assertEquals(NumberUtils.isCreatable("123"), NumberUtils.isNumber("123"));
        assertEquals(NumberUtils.isCreatable("abc"), NumberUtils.isNumber("abc"));
    }

    @Test
    public void testIsDigits() {
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits(""));
        assertFalse(NumberUtils.isDigits("12a3"));
        assertFalse(NumberUtils.isDigits("-123"));
        assertTrue(NumberUtils.isDigits("12345"));
    }

    @Test
    public void testIsParsable() {
        assertFalse(NumberUtils.isParsable(null));
        assertFalse(NumberUtils.isParsable(""));
        assertFalse(NumberUtils.isParsable("   "));
        assertFalse(NumberUtils.isParsable("abc"));
        assertFalse(NumberUtils.isParsable("12.34.56"));
        assertFalse(NumberUtils.isParsable("123L"));

        assertTrue(NumberUtils.isParsable("123"));
        assertTrue(NumberUtils.isParsable("-123"));
        assertTrue(NumberUtils.isParsable("12.34"));
        assertTrue(NumberUtils.isParsable("-12.34"));
        assertTrue(NumberUtils.isParsable("1.2e-5"));
        assertTrue(NumberUtils.isParsable("2.0f"));
        assertTrue(NumberUtils.isParsable("2.0d"));
        assertTrue(NumberUtils.isParsable("NaN"));
        assertTrue(NumberUtils.isParsable("Infinity"));
        assertTrue(NumberUtils.isParsable("-Infinity"));
        assertTrue(NumberUtils.isParsable("+Infinity"));
    }

    @Test
    public void testMaxByte() {
        assertEquals((byte) 5, NumberUtils.max((byte) 1, (byte) 5, (byte) 3));
        assertEquals((byte) 5, NumberUtils.max((byte) 5, (byte) 1, (byte) 3));
        assertEquals((byte) 5, NumberUtils.max((byte) 1, (byte) 3, (byte) 5));
        assertEquals((byte) 10, NumberUtils.max((byte) 1, (byte) 10, (byte) 3, (byte) 2));
    }

    @Test(expected = NullPointerException.class)
    public void testMaxByteArrayNull() {
        NumberUtils.max((byte[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxByteArrayEmpty() {
        NumberUtils.max(new byte[0]);
    }

    @Test
    public void testMinByte() {
        assertEquals((byte) 1, NumberUtils.min((byte) 5, (byte) 1, (byte) 3));
        assertEquals((byte) 1, NumberUtils.min((byte) 1, (byte) 5, (byte) 3));
        assertEquals((byte) 1, NumberUtils.min((byte) 5, (byte) 3, (byte) 1));
        assertEquals((byte) 0, NumberUtils.min((byte) 5, (byte) 0, (byte) 3, (byte) 2));
    }

    @Test(expected = NullPointerException.class)
    public void testMinByteArrayNull() {
        NumberUtils.min((byte[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinByteArrayEmpty() {
        NumberUtils.min(new byte[0]);
    }

    @Test
    public void testMaxShort() {
        assertEquals((short) 5, NumberUtils.max((short) 1, (short) 5, (short) 3));
        assertEquals((short) 5, NumberUtils.max((short) 5, (short) 1, (short) 3));
        assertEquals((short) 5, NumberUtils.max((short) 1, (short) 3, (short) 5));
        assertEquals((short) 10, NumberUtils.max((short) 1, (short) 10, (short) 3, (short) 2));
    }

    @Test(expected = NullPointerException.class)
    public void testMaxShortArrayNull() {
        NumberUtils.max((short[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxShortArrayEmpty() {
        NumberUtils.max(new short[0]);
    }

    @Test
    public void testMinShort() {
        assertEquals((short) 1, NumberUtils.min((short) 5, (short) 1, (short) 3));
        assertEquals((short) 1, NumberUtils.min((short) 1, (short) 5, (short) 3));
        assertEquals((short) 1, NumberUtils.min((short) 5, (short) 3, (short) 1));
        assertEquals((short) 0, NumberUtils.min((short) 5, (short) 0, (short) 3, (short) 2));
    }

    @Test(expected = NullPointerException.class)
    public void testMinShortArrayNull() {
        NumberUtils.min((short[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinShortArrayEmpty() {
        NumberUtils.min(new short[0]);
    }

    @Test
    public void testMaxInt() {
        assertEquals(5, NumberUtils.max(1, 5, 3));
        assertEquals(5, NumberUtils.max(5, 1, 3));
        assertEquals(5, NumberUtils.max(1, 3, 5));
        assertEquals(10, NumberUtils.max(1, 10, 3, 2));
    }

    @Test(expected = NullPointerException.class)
    public void testMaxIntArrayNull() {
        NumberUtils.max((int[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxIntArrayEmpty() {
        NumberUtils.max(new int[0]);
    }

    @Test
    public void testMinInt() {
        assertEquals(1, NumberUtils.min(5, 1, 3));
        assertEquals(1, NumberUtils.min(1, 5, 3));
        assertEquals(1, NumberUtils.min(5, 3, 1));
        assertEquals(0, NumberUtils.min(5, 0, 3, 2));
    }

    @Test(expected = NullPointerException.class)
    public void testMinIntArrayNull() {
        NumberUtils.min((int[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinIntArrayEmpty() {
        NumberUtils.min(new int[0]);
    }

    @Test
    public void testMaxLong() {
        assertEquals(5L, NumberUtils.max(1L, 5L, 3L));
        assertEquals(5L, NumberUtils.max(5L, 1L, 3L));
        assertEquals(5L, NumberUtils.max(1L, 3L, 5L));
        assertEquals(10L, NumberUtils.max(1L, 10L, 3L, 2L));
    }

    @Test(expected = NullPointerException.class)
    public void testMaxLongArrayNull() {
        NumberUtils.max((long[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxLongArrayEmpty() {
        NumberUtils.max(new long[0]);
    }

    @Test
    public void testMinLong() {
        assertEquals(1L, NumberUtils.min(5L, 1L, 3L));
        assertEquals(1L, NumberUtils.min(1L, 5L, 3L));
        assertEquals(1L, NumberUtils.min(5L, 3L, 1L));
        assertEquals(0L, NumberUtils.min(5L, 0L, 3L, 2L));
    }

    @Test(expected = NullPointerException.class)
    public void testMinLongArrayNull() {
        NumberUtils.min((long[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinLongArrayEmpty() {
        NumberUtils.min(new long[0]);
    }

    @Test
    public void testMaxFloat() {
        assertEquals(5.5f, NumberUtils.max(1.1f, 5.5f, 3.3f), DELTA_FLOAT);
        assertEquals(5.5f, NumberUtils.max(5.5f, 1.1f, 3.3f), DELTA_FLOAT);
        assertEquals(5.5f, NumberUtils.max(1.1f, 3.3f, 5.5f), DELTA_FLOAT);
        assertEquals(10.2f, NumberUtils.max(1.1f, 10.2f, 3.3f, 2.2f), DELTA_FLOAT);
        assertTrue(Float.isNaN(NumberUtils.max(1.0f, Float.NaN, 2.0f)));
    }

    @Test(expected = NullPointerException.class)
    public void testMaxFloatArrayNull() {
        NumberUtils.max((float[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxFloatArrayEmpty() {
        NumberUtils.max(new float[0]);
    }

    @Test
    public void testMinFloat() {
        assertEquals(1.1f, NumberUtils.min(5.5f, 1.1f, 3.3f), DELTA_FLOAT);
        assertEquals(1.1f, NumberUtils.min(1.1f, 5.5f, 3.3f), DELTA_FLOAT);
        assertEquals(1.1f, NumberUtils.min(5.5f, 3.3f, 1.1f), DELTA_FLOAT);
        assertEquals(0.5f, NumberUtils.min(5.5f, 0.5f, 3.3f, 2.2f), DELTA_FLOAT);
        assertTrue(Float.isNaN(NumberUtils.min(1.0f, Float.NaN, 2.0f)));
    }

    @Test(expected = NullPointerException.class)
    public void testMinFloatArrayNull() {
        NumberUtils.min((float[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinFloatArrayEmpty() {
        NumberUtils.min(new float[0]);
    }

    @Test
    public void testMaxDouble() {
        assertEquals(5.5d, NumberUtils.max(1.1d, 5.5d, 3.3d), DELTA_DOUBLE);
        assertEquals(5.5d, NumberUtils.max(5.5d, 1.1d, 3.3d), DELTA_DOUBLE);
        assertEquals(5.5d, NumberUtils.max(1.1d, 3.3d, 5.5d), DELTA_DOUBLE);
        assertEquals(10.2d, NumberUtils.max(1.1d, 10.2d, 3.3d, 2.2d), DELTA_DOUBLE);
        assertTrue(Double.isNaN(NumberUtils.max(1.0d, Double.NaN, 2.0d)));
    }

    @Test(expected = NullPointerException.class)
    public void testMaxDoubleArrayNull() {
        NumberUtils.max((double[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxDoubleArrayEmpty() {
        NumberUtils.max(new double[0]);
    }

    @Test
    public void testMinDouble() {
        assertEquals(1.1d, NumberUtils.min(5.5d, 1.1d, 3.3d), DELTA_DOUBLE);
        assertEquals(1.1d, NumberUtils.min(1.1d, 5.5d, 3.3d), DELTA_DOUBLE);
        assertEquals(1.1d, NumberUtils.min(5.5d, 3.3d, 1.1d), DELTA_DOUBLE);
        assertEquals(0.5d, NumberUtils.min(5.5d, 0.5d, 3.3d, 2.2d), DELTA_DOUBLE);
        assertTrue(Double.isNaN(NumberUtils.min(1.0d, Double.NaN, 2.0d)));
    }

    @Test(expected = NullPointerException.class)
    public void testMinDoubleArrayNull() {
        NumberUtils.min((double[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinDoubleArrayEmpty() {
        NumberUtils.min(new double[0]);
    }

    @Test
    public void testToByte() {
        assertEquals((byte) 0, NumberUtils.toByte(null));
        assertEquals((byte) 0, NumberUtils.toByte(""));
        assertEquals((byte) 0, NumberUtils.toByte("invalid"));
        assertEquals((byte) 12, NumberUtils.toByte("12"));

        assertEquals((byte) 5, NumberUtils.toByte(null, (byte) 5));
        assertEquals((byte) 5, NumberUtils.toByte("invalid", (byte) 5));
        assertEquals((byte) 12, NumberUtils.toByte("12", (byte) 5));
    }

    @Test
    public void testToShort() {
        assertEquals((short) 0, NumberUtils.toShort(null));
        assertEquals((short) 0, NumberUtils.toShort(""));
        assertEquals((short) 0, NumberUtils.toShort("invalid"));
        assertEquals((short) 123, NumberUtils.toShort("123"));

        assertEquals((short) 5, NumberUtils.toShort(null, (short) 5));
        assertEquals((short) 5, NumberUtils.toShort("invalid", (short) 5));
        assertEquals((short) 123, NumberUtils.toShort("123", (short) 5));
    }

    @Test
    public void testToInt() {
        assertEquals(0, NumberUtils.toInt(null));
        assertEquals(0, NumberUtils.toInt(""));
        assertEquals(0, NumberUtils.toInt("invalid"));
        assertEquals(1234, NumberUtils.toInt("1234"));

        assertEquals(5, NumberUtils.toInt(null, 5));
        assertEquals(5, NumberUtils.toInt("invalid", 5));
        assertEquals(1234, NumberUtils.toInt("1234", 5));
    }

    @Test
    public void testToLong() {
        assertEquals(0L, NumberUtils.toLong(null));
        assertEquals(0L, NumberUtils.toLong(""));
        assertEquals(0L, NumberUtils.toLong("invalid"));
        assertEquals(123456789L, NumberUtils.toLong("123456789"));

        assertEquals(5L, NumberUtils.toLong(null, 5L));
        assertEquals(5L, NumberUtils.toLong("invalid", 5L));
        assertEquals(123456789L, NumberUtils.toLong("123456789", 5L));
    }

    @Test
    public void testToFloat() {
        assertEquals(0.0f, NumberUtils.toFloat(null), DELTA_FLOAT);
        assertEquals(0.0f, NumberUtils.toFloat(""), DELTA_FLOAT);
        assertEquals(0.0f, NumberUtils.toFloat("invalid"), DELTA_FLOAT);
        assertEquals(12.34f, NumberUtils.toFloat("12.34"), DELTA_FLOAT);

        assertEquals(5.5f, NumberUtils.toFloat(null, 5.5f), DELTA_FLOAT);
        assertEquals(5.5f, NumberUtils.toFloat("invalid", 5.5f), DELTA_FLOAT);
        assertEquals(12.34f, NumberUtils.toFloat("12.34", 5.5f), DELTA_FLOAT);
    }

    @Test
    public void testToDouble() {
        assertEquals(0.0d, NumberUtils.toDouble((String) null), DELTA_DOUBLE);
        assertEquals(0.0d, NumberUtils.toDouble(""), DELTA_DOUBLE);
        assertEquals(0.0d, NumberUtils.toDouble("invalid"), DELTA_DOUBLE);
        assertEquals(12.34d, NumberUtils.toDouble("12.34"), DELTA_DOUBLE);

        assertEquals(5.5d, NumberUtils.toDouble((String) null, 5.5d), DELTA_DOUBLE);
        assertEquals(5.5d, NumberUtils.toDouble("invalid", 5.5d), DELTA_DOUBLE);
        assertEquals(12.34d, NumberUtils.toDouble("12.34", 5.5d), DELTA_DOUBLE);

        assertEquals(0.0d, NumberUtils.toDouble((BigDecimal) null), DELTA_DOUBLE);
        assertEquals(8.5d, NumberUtils.toDouble(BigDecimal.valueOf(8.5d)), DELTA_DOUBLE);
        assertEquals(1.1d, NumberUtils.toDouble((BigDecimal) null, 1.1d), DELTA_DOUBLE);
        assertEquals(8.5d, NumberUtils.toDouble(BigDecimal.valueOf(8.5d), 1.1d), DELTA_DOUBLE);
    }

    @Test
    public void testToScaledBigDecimal() {
        // BigDecimal
        assertEquals(BigDecimal.ZERO, NumberUtils.toScaledBigDecimal((BigDecimal) null));
        assertEquals(BigDecimal.ZERO, NumberUtils.toScaledBigDecimal((BigDecimal) null, 2, RoundingMode.HALF_UP));
        assertEquals(new BigDecimal("12.35"), NumberUtils.toScaledBigDecimal(new BigDecimal("12.345")));
        assertEquals(new BigDecimal("12.34"), NumberUtils.toScaledBigDecimal(new BigDecimal("12.344"), 2, RoundingMode.HALF_UP));
        assertEquals(new BigDecimal("12.34"), NumberUtils.toScaledBigDecimal(new BigDecimal("12.344"), 2, null));

        // Double
        assertEquals(BigDecimal.ZERO, NumberUtils.toScaledBigDecimal((Double) null));
        assertEquals(BigDecimal.ZERO, NumberUtils.toScaledBigDecimal((Double) null, 2, RoundingMode.HALF_UP));
        assertEquals(new BigDecimal("12.35"), NumberUtils.toScaledBigDecimal(Double.valueOf(12.345d)));
        assertEquals(new BigDecimal("12.34"), NumberUtils.toScaledBigDecimal(Double.valueOf(12.344d), 2, RoundingMode.HALF_UP));
        assertEquals(new BigDecimal("12.34"), NumberUtils.toScaledBigDecimal(Double.valueOf(12.344d), 2, null));

        // Float
        assertEquals(BigDecimal.ZERO, NumberUtils.toScaledBigDecimal((Float) null));
        assertEquals(BigDecimal.ZERO, NumberUtils.toScaledBigDecimal((Float) null, 2, RoundingMode.HALF_UP));
        assertEquals(new BigDecimal("12.35"), NumberUtils.toScaledBigDecimal(Float.valueOf(12.345f)));
        assertEquals(new BigDecimal("12.34"), NumberUtils.toScaledBigDecimal(Float.valueOf(12.344f), 2, RoundingMode.HALF_UP));
        assertEquals(new BigDecimal("12.34"), NumberUtils.toScaledBigDecimal(Float.valueOf(12.344f), 2, null));

        // String
        assertEquals(BigDecimal.ZERO, NumberUtils.toScaledBigDecimal((String) null));
        assertEquals(BigDecimal.ZERO, NumberUtils.toScaledBigDecimal((String) null, 2, RoundingMode.HALF_UP));
        assertEquals(new BigDecimal("12.35"), NumberUtils.toScaledBigDecimal("12.345"));
        assertEquals(new BigDecimal("12.34"), NumberUtils.toScaledBigDecimal("12.344", 2, RoundingMode.HALF_UP));
        assertEquals(new BigDecimal("12.34"), NumberUtils.toScaledBigDecimal("12.344", 2, null));
    }
}
