package org.apache.commons.lang3.math;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;

public class NumberUtilsTest {

    @Test
    public void testConstructor() {
        Assert.assertNotNull(new NumberUtils());
        final Constructor<?>[] cons = NumberUtils.class.getDeclaredConstructors();
        Assert.assertEquals(1, cons.length);
        Assert.assertTrue(Modifier.isPublic(cons[0].getModifiers()));
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
        Assert.assertEquals(5, NumberUtils.toInt(null, 5));
        Assert.assertEquals(5, NumberUtils.toInt("invalid", 5));
        Assert.assertEquals(123, NumberUtils.toInt("123", 5));
    }

    @Test
    public void testToLong() {
        Assert.assertEquals(0L, NumberUtils.toLong(null));
        Assert.assertEquals(0L, NumberUtils.toLong(""));
        Assert.assertEquals(0L, NumberUtils.toLong("abc"));
        Assert.assertEquals(1234567890123L, NumberUtils.toLong("1234567890123"));
        Assert.assertEquals(5L, NumberUtils.toLong(null, 5L));
        Assert.assertEquals(5L, NumberUtils.toLong("invalid", 5L));
        Assert.assertEquals(123L, NumberUtils.toLong("123", 5L));
    }

    @Test
    public void testToFloat() {
        Assert.assertEquals(0.0f, NumberUtils.toFloat(null), 0.0001f);
        Assert.assertEquals(0.0f, NumberUtils.toFloat(""), 0.0001f);
        Assert.assertEquals(0.0f, NumberUtils.toFloat("abc"), 0.0001f);
        Assert.assertEquals(1.23f, NumberUtils.toFloat("1.23"), 0.0001f);
        Assert.assertEquals(5.5f, NumberUtils.toFloat(null, 5.5f), 0.0001f);
        Assert.assertEquals(5.5f, NumberUtils.toFloat("invalid", 5.5f), 0.0001f);
        Assert.assertEquals(1.23f, NumberUtils.toFloat("1.23", 5.5f), 0.0001f);
    }

    @Test
    public void testToDouble() {
        Assert.assertEquals(0.0d, NumberUtils.toDouble(null), 0.0001d);
        Assert.assertEquals(0.0d, NumberUtils.toDouble(""), 0.0001d);
        Assert.assertEquals(0.0d, NumberUtils.toDouble("abc"), 0.0001d);
        Assert.assertEquals(1.23456d, NumberUtils.toDouble("1.23456"), 0.0001d);
        Assert.assertEquals(5.5d, NumberUtils.toDouble(null, 5.5d), 0.0001d);
        Assert.assertEquals(5.5d, NumberUtils.toDouble("invalid", 5.5d), 0.0001d);
        Assert.assertEquals(1.23456d, NumberUtils.toDouble("1.23456", 5.5d), 0.0001d);
    }

    @Test
    public void testToByte() {
        Assert.assertEquals((byte) 0, NumberUtils.toByte(null));
        Assert.assertEquals((byte) 0, NumberUtils.toByte(""));
        Assert.assertEquals((byte) 0, NumberUtils.toByte("abc"));
        Assert.assertEquals((byte) 123, NumberUtils.toByte("123"));
        Assert.assertEquals((byte) 5, NumberUtils.toByte(null, (byte) 5));
        Assert.assertEquals((byte) 5, NumberUtils.toByte("invalid", (byte) 5));
        Assert.assertEquals((byte) 123, NumberUtils.toByte("123", (byte) 5));
    }

    @Test
    public void testToShort() {
        Assert.assertEquals((short) 0, NumberUtils.toShort(null));
        Assert.assertEquals((short) 0, NumberUtils.toShort(""));
        Assert.assertEquals((short) 0, NumberUtils.toShort("abc"));
        Assert.assertEquals((short) 1234, NumberUtils.toShort("1234"));
        Assert.assertEquals((short) 5, NumberUtils.toShort(null, (short) 5));
        Assert.assertEquals((short) 5, NumberUtils.toShort("invalid", (short) 5));
        Assert.assertEquals((short) 1234, NumberUtils.toShort("1234", (short) 5));
    }

    @Test
    public void testCreateFloat() {
        Assert.assertNull(NumberUtils.createFloat(null));
        Assert.assertEquals(Float.valueOf(1.23f), NumberUtils.createFloat("1.23"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateFloatFailure() {
        NumberUtils.createFloat("invalid");
    }

    @Test
    public void testCreateDouble() {
        Assert.assertNull(NumberUtils.createDouble(null));
        Assert.assertEquals(Double.valueOf(1.2345), NumberUtils.createDouble("1.2345"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateDoubleFailure() {
        NumberUtils.createDouble("invalid");
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
        NumberUtils.createInteger("invalid");
    }

    @Test
    public void testCreateLong() {
        Assert.assertNull(NumberUtils.createLong(null));
        Assert.assertEquals(Long.valueOf(1234567890123L), NumberUtils.createLong("1234567890123"));
        Assert.assertEquals(Long.valueOf(0x1234L), NumberUtils.createLong("0x1234"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateLongFailure() {
        NumberUtils.createLong("invalid");
    }

    @Test
    public void testCreateBigInteger() {
        Assert.assertNull(NumberUtils.createBigInteger(null));
        Assert.assertEquals(new BigInteger("12345678901234567890"), NumberUtils.createBigInteger("12345678901234567890"));
        Assert.assertEquals(new BigInteger("-12345678901234567890"), NumberUtils.createBigInteger("-12345678901234567890"));
        Assert.assertEquals(new BigInteger("10", 16), NumberUtils.createBigInteger("0x10"));
        Assert.assertEquals(new BigInteger("-10", 16), NumberUtils.createBigInteger("-0x10"));
        Assert.assertEquals(new BigInteger("10", 16), NumberUtils.createBigInteger("#10"));
        Assert.assertEquals(new BigInteger("-10", 16), NumberUtils.createBigInteger("-#10"));
        Assert.assertEquals(new BigInteger("77", 8), NumberUtils.createBigInteger("077"));
        Assert.assertEquals(new BigInteger("-77", 8), NumberUtils.createBigInteger("-077"));
        Assert.assertEquals(BigInteger.ZERO, NumberUtils.createBigInteger("0"));
        Assert.assertEquals(BigInteger.ZERO, NumberUtils.createBigInteger("-0"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigIntegerFailure() {
        NumberUtils.createBigInteger("invalid");
    }

    @Test
    public void testCreateBigDecimal() {
        Assert.assertNull(NumberUtils.createBigDecimal(null));
        Assert.assertEquals(new BigDecimal("1234.5678"), NumberUtils.createBigDecimal("1234.5678"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigDecimalBlank() {
        NumberUtils.createBigDecimal("   ");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigDecimalDoubleDash() {
        NumberUtils.createBigDecimal("--123");
    }

    @Test
    public void testCreateNumber() {
        Assert.assertNull(NumberUtils.createNumber(null));
        
        Assert.assertEquals(Integer.valueOf(123), NumberUtils.createNumber("123"));
        Assert.assertEquals(Long.valueOf(12345678901L), NumberUtils.createNumber("12345678901"));
        Assert.assertEquals(new BigInteger("123456789012345678901234567890"), NumberUtils.createNumber("123456789012345678901234567890"));

        Assert.assertEquals(Integer.valueOf(0x12), NumberUtils.createNumber("0x12"));
        Assert.assertEquals(Integer.valueOf(0x12), NumberUtils.createNumber("0X12"));
        Assert.assertEquals(Integer.valueOf(-0x12), NumberUtils.createNumber("-0x12"));
        Assert.assertEquals(Integer.valueOf(-0x12), NumberUtils.createNumber("-0X12"));
        Assert.assertEquals(Integer.valueOf(0x12), NumberUtils.createNumber("#12"));
        Assert.assertEquals(Integer.valueOf(-0x12), NumberUtils.createNumber("-#12"));
        
        Assert.assertEquals(Long.valueOf(0x123456789L), NumberUtils.createNumber("0x123456789"));
        Assert.assertEquals(new BigInteger("123456789012345678", 16), NumberUtils.createNumber("0x123456789012345678"));

        Assert.assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123L"));
        Assert.assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123l"));
        Assert.assertEquals(Long.valueOf(-123L), NumberUtils.createNumber("-123L"));
        Assert.assertEquals(new BigInteger("123456789012345678901234567890"), NumberUtils.createNumber("123456789012345678901234567890L"));

        Assert.assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23f"));
        Assert.assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23F"));
        Assert.assertEquals(Float.valueOf(0.0f), NumberUtils.createNumber("0.0f"));
        Assert.assertEquals(Double.valueOf(1e300d), NumberUtils.createNumber("1e300f"));
        Assert.assertEquals(Double.valueOf(1.23d), NumberUtils.createNumber("1.23d"));
        Assert.assertEquals(Double.valueOf(1.23d), NumberUtils.createNumber("1.23D"));
        Assert.assertEquals(new BigDecimal("1e350"), NumberUtils.createNumber("1e350d"));

        Assert.assertEquals(Float.valueOf(1.2345f), NumberUtils.createNumber("1.2345"));
        Assert.assertEquals(Float.valueOf(0.0f), NumberUtils.createNumber("0.0"));
        Assert.assertEquals(Float.valueOf(1e10f), NumberUtils.createNumber("1e10"));
        Assert.assertEquals(Double.valueOf(1.234567890123d), NumberUtils.createNumber("1.234567890123"));
        Assert.assertEquals(Double.valueOf(1e-100d), NumberUtils.createNumber("1e-100"));
        Assert.assertEquals(new BigDecimal("1.23456789012345678901234567890"), NumberUtils.createNumber("1.23456789012345678901234567890"));
        Assert.assertEquals(new BigDecimal("1e-400"), NumberUtils.createNumber("1e-400"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberBlank() {
        NumberUtils.createNumber(" ");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidExponentPosition() {
        NumberUtils.createNumber("1.2e");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberExpBeforeDec() {
        NumberUtils.createNumber("1e2.3");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberExpTooLong() {
        NumberUtils.createNumber("1e");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidQualifier() {
        NumberUtils.createNumber("123q");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidLongQualifierWithDec() {
        NumberUtils.createNumber("12.3L");
    }

    @Test
    public void testMinLongArray() {
        Assert.assertEquals(1L, NumberUtils.min(new long[]{3L, 1L, 2L}));
        Assert.assertEquals(-5L, NumberUtils.min(new long[]{-1L, -5L, 0L}));
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
        Assert.assertEquals(-5, NumberUtils.min(new int[]{-1, -5, 0}));
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
        Assert.assertEquals((short) -5, NumberUtils.min(new short[]{-1, -5, 0}));
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
        Assert.assertEquals((byte) -5, NumberUtils.min(new byte[]{-1, -5, 0}));
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
        Assert.assertEquals(1.1d, NumberUtils.min(new double[]{3.3d, 1.1d, 2.2d}), 0.0001d);
        Assert.assertTrue(Double.isNaN(NumberUtils.min(new double[]{1.1d, Double.NaN, 2.2d})));
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
        Assert.assertEquals(1.1f, NumberUtils.min(new float[]{3.3f, 1.1f, 2.2f}), 0.0001f);
        Assert.assertTrue(Float.isNaN(NumberUtils.min(new float[]{1.1f, Float.NaN, 2.2f})));
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
        Assert.assertEquals(0L, NumberUtils.max(new long[]{-1L, -5L, 0L}));
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
        Assert.assertEquals(0, NumberUtils.max(new int[]{-1, -5, 0}));
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
        Assert.assertEquals((short) 0, NumberUtils.max(new short[]{-1, -5, 0}));
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
        Assert.assertEquals((byte) 0, NumberUtils.max(new byte[]{-1, -5, 0}));
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
        Assert.assertEquals(3.3d, NumberUtils.max(new double[]{1.1d, 3.3d, 2.2d}), 0.0001d);
        Assert.assertTrue(Double.isNaN(NumberUtils.max(new double[]{1.1d, Double.NaN, 2.2d})));
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
        Assert.assertEquals(3.3f, NumberUtils.max(new float[]{1.1f, 3.3f, 2.2f}), 0.0001f);
        Assert.assertTrue(Float.isNaN(NumberUtils.max(new float[]{1.1f, Float.NaN, 2.2f})));
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
    public void testMinLongThreeArgs() {
        Assert.assertEquals(1L, NumberUtils.min(1L, 2L, 3L));
        Assert.assertEquals(1L, NumberUtils.min(2L, 1L, 3L));
        Assert.assertEquals(1L, NumberUtils.min(3L, 2L, 1L));
    }

    @Test
    public void testMinIntThreeArgs() {
        Assert.assertEquals(1, NumberUtils.min(1, 2, 3));
        Assert.assertEquals(1, NumberUtils.min(2, 1, 3));
        Assert.assertEquals(1, NumberUtils.min(3, 2, 1));
    }

    @Test
    public void testMinShortThreeArgs() {
        Assert.assertEquals((short) 1, NumberUtils.min((short) 1, (short) 2, (short) 3));
        Assert.assertEquals((short) 1, NumberUtils.min((short) 2, (short) 1, (short) 3));
        Assert.assertEquals((short) 1, NumberUtils.min((short) 3, (short) 2, (short) 1));
    }

    @Test
    public void testMinByteThreeArgs() {
        Assert.assertEquals((byte) 1, NumberUtils.min((byte) 1, (byte) 2, (byte) 3));
        Assert.assertEquals((byte) 1, NumberUtils.min((byte) 2, (byte) 1, (byte) 3));
        Assert.assertEquals((byte) 1, NumberUtils.min((byte) 3, (byte) 2, (byte) 1));
    }

    @Test
    public void testMinDoubleThreeArgs() {
        Assert.assertEquals(1.1d, NumberUtils.min(1.1d, 2.2d, 3.3d), 0.0001d);
        Assert.assertEquals(1.1d, NumberUtils.min(2.2d, 1.1d, 3.3d), 0.0001d);
        Assert.assertEquals(1.1d, NumberUtils.min(3.3d, 2.2d, 1.1d), 0.0001d);
        Assert.assertTrue(Double.isNaN(NumberUtils.min(Double.NaN, 2.2d, 1.1d)));
    }

    @Test
    public void testMinFloatThreeArgs() {
        Assert.assertEquals(1.1f, NumberUtils.min(1.1f, 2.2f, 3.3f), 0.0001f);
        Assert.assertEquals(1.1f, NumberUtils.min(2.2f, 1.1f, 3.3f), 0.0001f);
        Assert.assertEquals(1.1f, NumberUtils.min(3.3f, 2.2f, 1.1f), 0.0001f);
        Assert.assertTrue(Float.isNaN(NumberUtils.min(Float.NaN, 2.2f, 1.1f)));
    }

    @Test
    public void testMaxLongThreeArgs() {
        Assert.assertEquals(3L, NumberUtils.max(1L, 2L, 3L));
        Assert.assertEquals(3L, NumberUtils.max(1L, 3L, 2L));
        Assert.assertEquals(3L, NumberUtils.max(3L, 2L, 1L));
    }

    @Test
    public void testMaxIntThreeArgs() {
        Assert.assertEquals(3, NumberUtils.max(1, 2, 3));
        Assert.assertEquals(3, NumberUtils.max(1, 3, 2));
        Assert.assertEquals(3, NumberUtils.max(3, 2, 1));
    }

    @Test
    public void testMaxShortThreeArgs() {
        Assert.assertEquals((short) 3, NumberUtils.max((short) 1, (short) 2, (short) 3));
        Assert.assertEquals((short) 3, NumberUtils.max((short) 1, (short) 3, (short) 2));
        Assert.assertEquals((short) 3, NumberUtils.max((short) 3, (short) 2, (short) 1));
    }

    @Test
    public void testMaxByteThreeArgs() {
        Assert.assertEquals((byte) 3, NumberUtils.max((byte) 1, (byte) 2, (byte) 3));
        Assert.assertEquals((byte) 3, NumberUtils.max((byte) 1, (byte) 3, (byte) 2));
        Assert.assertEquals((byte) 3, NumberUtils.max((byte) 3, (byte) 2, (byte) 1));
    }

    @Test
    public void testMaxDoubleThreeArgs() {
        Assert.assertEquals(3.3d, NumberUtils.max(1.1d, 2.2d, 3.3d), 0.0001d);
        Assert.assertEquals(3.3d, NumberUtils.max(1.1d, 3.3d, 2.2d), 0.0001d);
        Assert.assertEquals(3.3d, NumberUtils.max(3.3d, 2.2d, 1.1d), 0.0001d);
        Assert.assertTrue(Double.isNaN(NumberUtils.max(Double.NaN, 2.2d, 1.1d)));
    }

    @Test
    public void testMaxFloatThreeArgs() {
        Assert.assertEquals(3.3f, NumberUtils.max(1.1f, 2.2f, 3.3f), 0.0001f);
        Assert.assertEquals(3.3f, NumberUtils.max(1.1f, 3.3f, 2.2f), 0.0001f);
        Assert.assertEquals(3.3f, NumberUtils.max(3.3f, 2.2f, 1.1f), 0.0001f);
        Assert.assertTrue(Float.isNaN(NumberUtils.max(Float.NaN, 2.2f, 1.1f)));
    }

    @Test
    public void testIsDigits() {
        Assert.assertFalse(NumberUtils.isDigits(null));
        Assert.assertFalse(NumberUtils.isDigits(""));
        Assert.assertFalse(NumberUtils.isDigits("123a"));
        Assert.assertFalse(NumberUtils.isDigits("-123"));
        Assert.assertTrue(NumberUtils.isDigits("12345"));
    }

    @Test
    public void testIsNumber() {
        Assert.assertFalse(NumberUtils.isNumber(null));
        Assert.assertFalse(NumberUtils.isNumber(""));
        Assert.assertFalse(NumberUtils.isNumber(" "));
        Assert.assertFalse(NumberUtils.isNumber("abc"));
        
        Assert.assertTrue(NumberUtils.isNumber("0x123"));
        Assert.assertTrue(NumberUtils.isNumber("-0x123"));
        Assert.assertTrue(NumberUtils.isNumber("0xabcdef"));
        Assert.assertTrue(NumberUtils.isNumber("0xABCDEF"));
        Assert.assertFalse(NumberUtils.isNumber("0x"));
        Assert.assertFalse(NumberUtils.isNumber("0x123g"));

        Assert.assertTrue(NumberUtils.isNumber("123"));
        Assert.assertTrue(NumberUtils.isNumber("-123"));
        Assert.assertTrue(NumberUtils.isNumber("12.3"));
        Assert.assertTrue(NumberUtils.isNumber("-12.3"));
        Assert.assertTrue(NumberUtils.isNumber(".3"));
        Assert.assertTrue(NumberUtils.isNumber("12."));
        Assert.assertFalse(NumberUtils.isNumber("."));
        Assert.assertFalse(NumberUtils.isNumber("1.2.3"));

        Assert.assertTrue(NumberUtils.isNumber("123e4"));
        Assert.assertTrue(NumberUtils.isNumber("123E4"));
        Assert.assertTrue(NumberUtils.isNumber("123e+4"));
        Assert.assertTrue(NumberUtils.isNumber("123e-4"));
        Assert.assertFalse(NumberUtils.isNumber("123e"));
        Assert.assertFalse(NumberUtils.isNumber("123e+"));
        Assert.assertFalse(NumberUtils.isNumber("123e-"));
        Assert.assertFalse(NumberUtils.isNumber("123e4e5"));
        Assert.assertFalse(NumberUtils.isNumber("123e4.5"));
        Assert.assertFalse(NumberUtils.isNumber("e4"));

        Assert.assertTrue(NumberUtils.isNumber("123f"));
        Assert.assertTrue(NumberUtils.isNumber("123F"));
        Assert.assertTrue(NumberUtils.isNumber("123d"));
        Assert.assertTrue(NumberUtils.isNumber("123D"));
        Assert.assertTrue(NumberUtils.isNumber("123l"));
        Assert.assertTrue(NumberUtils.isNumber("123L"));
        Assert.assertFalse(NumberUtils.isNumber("12.3L"));
        Assert.assertFalse(NumberUtils.isNumber("12e3L"));
        Assert.assertFalse(NumberUtils.isNumber("123a"));
    }
}
