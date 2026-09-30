package org.apache.commons.lang3.math;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

public class NumberUtilsTest {

    @Test
    public void testConstructor() {
        NumberUtils utils = new NumberUtils();
        Assert.assertNotNull(utils);
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
        Assert.assertEquals(1, NumberUtils.toInt("1"));
        Assert.assertEquals(0, NumberUtils.toInt("invalid"));

        Assert.assertEquals(5, NumberUtils.toInt(null, 5));
        Assert.assertEquals(5, NumberUtils.toInt("", 5));
        Assert.assertEquals(5, NumberUtils.toInt("invalid", 5));
        Assert.assertEquals(123, NumberUtils.toInt("123", 5));
    }

    @Test
    public void testToLong() {
        Assert.assertEquals(0L, NumberUtils.toLong(null));
        Assert.assertEquals(0L, NumberUtils.toLong(""));
        Assert.assertEquals(1L, NumberUtils.toLong("1"));
        Assert.assertEquals(0L, NumberUtils.toLong("invalid"));

        Assert.assertEquals(5L, NumberUtils.toLong(null, 5L));
        Assert.assertEquals(5L, NumberUtils.toLong("", 5L));
        Assert.assertEquals(5L, NumberUtils.toLong("invalid", 5L));
        Assert.assertEquals(123L, NumberUtils.toLong("123", 5L));
    }

    @Test
    public void testToFloat() {
        Assert.assertEquals(0.0f, NumberUtils.toFloat(null), 0.0001f);
        Assert.assertEquals(0.0f, NumberUtils.toFloat(""), 0.0001f);
        Assert.assertEquals(1.5f, NumberUtils.toFloat("1.5"), 0.0001f);
        Assert.assertEquals(0.0f, NumberUtils.toFloat("invalid"), 0.0001f);

        Assert.assertEquals(5.5f, NumberUtils.toFloat(null, 5.5f), 0.0001f);
        Assert.assertEquals(5.5f, NumberUtils.toFloat("", 5.5f), 0.0001f);
        Assert.assertEquals(5.5f, NumberUtils.toFloat("invalid", 5.5f), 0.0001f);
        Assert.assertEquals(1.5f, NumberUtils.toFloat("1.5", 5.5f), 0.0001f);
    }

    @Test
    public void testToDouble() {
        Assert.assertEquals(0.0d, NumberUtils.toDouble(null), 0.0001d);
        Assert.assertEquals(0.0d, NumberUtils.toDouble(""), 0.0001d);
        Assert.assertEquals(1.5d, NumberUtils.toDouble("1.5"), 0.0001d);
        Assert.assertEquals(0.0d, NumberUtils.toDouble("invalid"), 0.0001d);

        Assert.assertEquals(5.5d, NumberUtils.toDouble(null, 5.5d), 0.0001d);
        Assert.assertEquals(5.5d, NumberUtils.toDouble("", 5.5d), 0.0001d);
        Assert.assertEquals(5.5d, NumberUtils.toDouble("invalid", 5.5d), 0.0001d);
        Assert.assertEquals(1.5d, NumberUtils.toDouble("1.5", 5.5d), 0.0001d);
    }

    @Test
    public void testToByte() {
        Assert.assertEquals((byte) 0, NumberUtils.toByte(null));
        Assert.assertEquals((byte) 0, NumberUtils.toByte(""));
        Assert.assertEquals((byte) 1, NumberUtils.toByte("1"));
        Assert.assertEquals((byte) 0, NumberUtils.toByte("invalid"));

        Assert.assertEquals((byte) 5, NumberUtils.toByte(null, (byte) 5));
        Assert.assertEquals((byte) 5, NumberUtils.toByte("", (byte) 5));
        Assert.assertEquals((byte) 5, NumberUtils.toByte("invalid", (byte) 5));
        Assert.assertEquals((byte) 123, NumberUtils.toByte("123", (byte) 5));
    }

    @Test
    public void testToShort() {
        Assert.assertEquals((short) 0, NumberUtils.toShort(null));
        Assert.assertEquals((short) 0, NumberUtils.toShort(""));
        Assert.assertEquals((short) 1, NumberUtils.toShort("1"));
        Assert.assertEquals((short) 0, NumberUtils.toShort("invalid"));

        Assert.assertEquals((short) 5, NumberUtils.toShort(null, (short) 5));
        Assert.assertEquals((short) 5, NumberUtils.toShort("", (short) 5));
        Assert.assertEquals((short) 5, NumberUtils.toShort("invalid", (short) 5));
        Assert.assertEquals((short) 123, NumberUtils.toShort("123", (short) 5));
    }

    @Test
    public void testCreateNumber() {
        Assert.assertNull(NumberUtils.createNumber(null));

        // Hex prefixes
        Assert.assertEquals(Integer.valueOf(0x12), NumberUtils.createNumber("0x12"));
        Assert.assertEquals(Integer.valueOf(0X12), NumberUtils.createNumber("0X12"));
        Assert.assertEquals(Integer.valueOf(-0x12), NumberUtils.createNumber("-0x12"));
        Assert.assertEquals(Integer.valueOf(-0X12), NumberUtils.createNumber("-0X12"));
        Assert.assertEquals(Integer.valueOf(0x12), NumberUtils.createNumber("#12"));
        Assert.assertEquals(Integer.valueOf(-0x12), NumberUtils.createNumber("-#12"));
        Assert.assertEquals(Long.valueOf(0x123456789L), NumberUtils.createNumber("0x123456789"));
        Assert.assertEquals(new BigInteger("12345678901234567", 16), NumberUtils.createNumber("0x12345678901234567"));

        // Type specifiers 'l', 'L'
        Assert.assertEquals(Long.valueOf(12345L), NumberUtils.createNumber("12345l"));
        Assert.assertEquals(Long.valueOf(-12345L), NumberUtils.createNumber("-12345L"));
        Assert.assertEquals(new BigInteger("9223372036854775808"), NumberUtils.createNumber("9223372036854775808L"));

        // Type specifiers 'f', 'F'
        Assert.assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23f"));
        Assert.assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23F"));
        Assert.assertEquals(Float.valueOf(0.0f), NumberUtils.createNumber("0.0f"));
        Assert.assertEquals(Float.valueOf(0.0f), NumberUtils.createNumber("0f"));
        Assert.assertEquals(Double.valueOf("1e-45"), (Double) NumberUtils.createNumber("1e-45f"), 0.0001);

        // Type specifiers 'd', 'D'
        Assert.assertEquals(Double.valueOf(1.23d), NumberUtils.createNumber("1.23d"));
        Assert.assertEquals(Double.valueOf(1.23d), NumberUtils.createNumber("1.23D"));
        Assert.assertEquals(Double.valueOf(0.0d), NumberUtils.createNumber("0.0d"));
        Assert.assertEquals(Double.valueOf(0.0d), NumberUtils.createNumber("0d"));
        Assert.assertEquals(new BigDecimal("1e-400"), NumberUtils.createNumber("1e-400d"));

        // Integers and Decimals without specifiers
        Assert.assertEquals(Integer.valueOf(123), NumberUtils.createNumber("123"));
        Assert.assertEquals(Long.valueOf(2147483648L), NumberUtils.createNumber("2147483648"));
        Assert.assertEquals(new BigInteger("9223372036854775808"), NumberUtils.createNumber("9223372036854775808"));

        // Floats, Doubles, BigDecimals without specifiers
        Assert.assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23"));
        Assert.assertEquals(Float.valueOf(0.0f), NumberUtils.createNumber("0.0"));
        Assert.assertEquals(Double.valueOf(1.23e30), (Double) NumberUtils.createNumber("1.23e30"), 0.0001);
        Assert.assertEquals(new BigDecimal("1.23e3000"), NumberUtils.createNumber("1.23e3000"));
        Assert.assertEquals(Float.valueOf(123e2f), NumberUtils.createNumber("123e2"));
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
    public void testCreateNumberInvalidHex() {
        NumberUtils.createNumber("0xGHIJ");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberDoubleExponent() {
        NumberUtils.createNumber("1.2.3e4");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidExponentPos() {
        NumberUtils.createNumber("1.2e");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidDecExpOrder() {
        NumberUtils.createNumber("1e2.3");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidLongQualifier1() {
        NumberUtils.createNumber("1.2L");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidLongQualifier2() {
        NumberUtils.createNumber("1e2L");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidLongQualifier3() {
        NumberUtils.createNumber("-abcL");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidCharacter() {
        NumberUtils.createNumber("123a");
    }

    @Test
    public void testCreateFloat() {
        Assert.assertNull(NumberUtils.createFloat(null));
        Assert.assertEquals(Float.valueOf(1.23f), NumberUtils.createFloat("1.23"));
    }

    @Test
    public void testCreateDouble() {
        Assert.assertNull(NumberUtils.createDouble(null));
        Assert.assertEquals(Double.valueOf(1.23d), NumberUtils.createDouble("1.23"));
    }

    @Test
    public void testCreateInteger() {
        Assert.assertNull(NumberUtils.createInteger(null));
        Assert.assertEquals(Integer.valueOf(123), NumberUtils.createInteger("123"));
        Assert.assertEquals(Integer.valueOf(0x12), NumberUtils.createInteger("0x12"));
        Assert.assertEquals(Integer.valueOf(8), NumberUtils.createInteger("010"));
    }

    @Test
    public void testCreateLong() {
        Assert.assertNull(NumberUtils.createLong(null));
        Assert.assertEquals(Long.valueOf(123L), NumberUtils.createLong("123"));
        Assert.assertEquals(Long.valueOf(0x12L), NumberUtils.createLong("0x12"));
        Assert.assertEquals(Long.valueOf(8L), NumberUtils.createLong("010"));
    }

    @Test
    public void testCreateBigInteger() {
        Assert.assertNull(NumberUtils.createBigInteger(null));
        Assert.assertEquals(new BigInteger("123"), NumberUtils.createBigInteger("123"));
        Assert.assertEquals(new BigInteger("-123"), NumberUtils.createBigInteger("-123"));
        Assert.assertEquals(new BigInteger("16"), NumberUtils.createBigInteger("0x10"));
        Assert.assertEquals(new BigInteger("-16"), NumberUtils.createBigInteger("-0x10"));
        Assert.assertEquals(new BigInteger("16"), NumberUtils.createBigInteger("#10"));
        Assert.assertEquals(new BigInteger("-16"), NumberUtils.createBigInteger("-#10"));
        Assert.assertEquals(new BigInteger("8"), NumberUtils.createBigInteger("010"));
        Assert.assertEquals(new BigInteger("-8"), NumberUtils.createBigInteger("-010"));
        Assert.assertEquals(new BigInteger("0"), NumberUtils.createBigInteger("0"));
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
    public void testCreateBigDecimalDoubleMinus() {
        NumberUtils.createBigDecimal("--123");
    }

    @Test
    public void testMinArrayLong() {
        Assert.assertEquals(1L, NumberUtils.min(new long[]{3L, 1L, 2L}));
        Assert.assertEquals(1L, NumberUtils.min(new long[]{1L}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinArrayLongNull() {
        NumberUtils.min((long[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinArrayLongEmpty() {
        NumberUtils.min(new long[0]);
    }

    @Test
    public void testMinArrayInt() {
        Assert.assertEquals(1, NumberUtils.min(new int[]{3, 1, 2}));
        Assert.assertEquals(1, NumberUtils.min(new int[]{1}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinArrayIntNull() {
        NumberUtils.min((int[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinArrayIntEmpty() {
        NumberUtils.min(new int[0]);
    }

    @Test
    public void testMinArrayShort() {
        Assert.assertEquals((short) 1, NumberUtils.min(new short[]{3, 1, 2}));
        Assert.assertEquals((short) 1, NumberUtils.min(new short[]{1}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinArrayShortNull() {
        NumberUtils.min((short[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinArrayShortEmpty() {
        NumberUtils.min(new short[0]);
    }

    @Test
    public void testMinArrayByte() {
        Assert.assertEquals((byte) 1, NumberUtils.min(new byte[]{3, 1, 2}));
        Assert.assertEquals((byte) 1, NumberUtils.min(new byte[]{1}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinArrayByteNull() {
        NumberUtils.min((byte[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinArrayByteEmpty() {
        NumberUtils.min(new byte[0]);
    }

    @Test
    public void testMinArrayDouble() {
        Assert.assertEquals(1.1d, NumberUtils.min(new double[]{3.3d, 1.1d, 2.2d}), 0.0001d);
        Assert.assertEquals(1.1d, NumberUtils.min(new double[]{1.1d}), 0.0001d);
        Assert.assertTrue(Double.isNaN(NumberUtils.min(new double[]{1.1d, Double.NaN, 2.2d})));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinArrayDoubleNull() {
        NumberUtils.min((double[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinArrayDoubleEmpty() {
        NumberUtils.min(new double[0]);
    }

    @Test
    public void testMinArrayFloat() {
        Assert.assertEquals(1.1f, NumberUtils.min(new float[]{3.3f, 1.1f, 2.2f}), 0.0001f);
        Assert.assertEquals(1.1f, NumberUtils.min(new float[]{1.1f}), 0.0001f);
        Assert.assertTrue(Float.isNaN(NumberUtils.min(new float[]{1.1f, Float.NaN, 2.2f})));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinArrayFloatNull() {
        NumberUtils.min((float[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinArrayFloatEmpty() {
        NumberUtils.min(new float[0]);
    }

    @Test
    public void testMaxArrayLong() {
        Assert.assertEquals(3L, NumberUtils.max(new long[]{1L, 3L, 2L}));
        Assert.assertEquals(1L, NumberUtils.max(new long[]{1L}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxArrayLongNull() {
        NumberUtils.max((long[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxArrayLongEmpty() {
        NumberUtils.max(new long[0]);
    }

    @Test
    public void testMaxArrayInt() {
        Assert.assertEquals(3, NumberUtils.max(new int[]{1, 3, 2}));
        Assert.assertEquals(1, NumberUtils.max(new int[]{1}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxArrayIntNull() {
        NumberUtils.max((int[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxArrayIntEmpty() {
        NumberUtils.max(new int[0]);
    }

    @Test
    public void testMaxArrayShort() {
        Assert.assertEquals((short) 3, NumberUtils.max(new short[]{1, 3, 2}));
        Assert.assertEquals((short) 1, NumberUtils.max(new short[]{1}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxArrayShortNull() {
        NumberUtils.max((short[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxArrayShortEmpty() {
        NumberUtils.max(new short[0]);
    }

    @Test
    public void testMaxArrayByte() {
        Assert.assertEquals((byte) 3, NumberUtils.max(new byte[]{1, 3, 2}));
        Assert.assertEquals((byte) 1, NumberUtils.max(new byte[]{1}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxArrayByteNull() {
        NumberUtils.max((byte[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxArrayByteEmpty() {
        NumberUtils.max(new byte[0]);
    }

    @Test
    public void testMaxArrayDouble() {
        Assert.assertEquals(3.3d, NumberUtils.max(new double[]{1.1d, 3.3d, 2.2d}), 0.0001d);
        Assert.assertEquals(1.1d, NumberUtils.max(new double[]{1.1d}), 0.0001d);
        Assert.assertTrue(Double.isNaN(NumberUtils.max(new double[]{1.1d, Double.NaN, 2.2d})));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxArrayDoubleNull() {
        NumberUtils.max((double[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxArrayDoubleEmpty() {
        NumberUtils.max(new double[0]);
    }

    @Test
    public void testMaxArrayFloat() {
        Assert.assertEquals(3.3f, NumberUtils.max(new float[]{1.1f, 3.3f, 2.2f}), 0.0001f);
        Assert.assertEquals(1.1f, NumberUtils.max(new float[]{1.1f}), 0.0001f);
        Assert.assertTrue(Float.isNaN(NumberUtils.max(new float[]{1.1f, Float.NaN, 2.2f})));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxArrayFloatNull() {
        NumberUtils.max((float[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxArrayFloatEmpty() {
        NumberUtils.max(new float[0]);
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
    public void testIsDigits() {
        Assert.assertFalse(NumberUtils.isDigits(null));
        Assert.assertFalse(NumberUtils.isDigits(""));
        Assert.assertTrue(NumberUtils.isDigits("12345"));
        Assert.assertFalse(NumberUtils.isDigits("123a45"));
        Assert.assertFalse(NumberUtils.isDigits("-123"));
        Assert.assertFalse(NumberUtils.isDigits("12.3"));
    }

    @Test
    public void testIsNumber() {
        Assert.assertFalse(NumberUtils.isNumber(null));
        Assert.assertFalse(NumberUtils.isNumber(""));
        Assert.assertFalse(NumberUtils.isNumber("   "));

        // Hex tests
        Assert.assertTrue(NumberUtils.isNumber("0x1234"));
        Assert.assertTrue(NumberUtils.isNumber("0xABCD"));
        Assert.assertTrue(NumberUtils.isNumber("0xabcd"));
        Assert.assertTrue(NumberUtils.isNumber("-0x1234"));
        Assert.assertTrue(NumberUtils.isNumber("-0xABCD"));
        Assert.assertFalse(NumberUtils.isNumber("0x"));
        Assert.assertFalse(NumberUtils.isNumber("-0x"));
        Assert.assertFalse(NumberUtils.isNumber("0x123G"));
        Assert.assertFalse(NumberUtils.isNumber("-0x123G"));

        // Regular integers and decimals
        Assert.assertTrue(NumberUtils.isNumber("123"));
        Assert.assertTrue(NumberUtils.isNumber("-123"));
        Assert.assertTrue(NumberUtils.isNumber("123.45"));
        Assert.assertTrue(NumberUtils.isNumber("-123.45"));
        Assert.assertTrue(NumberUtils.isNumber(".45"));
        Assert.assertTrue(NumberUtils.isNumber("-.45"));
        Assert.assertTrue(NumberUtils.isNumber("123."));
        Assert.assertFalse(NumberUtils.isNumber("."));
        Assert.assertFalse(NumberUtils.isNumber("-."));
        Assert.assertFalse(NumberUtils.isNumber("1.2.3"));
        Assert.assertFalse(NumberUtils.isNumber("123.."));

        // Exponents
        Assert.assertTrue(NumberUtils.isNumber("123e4"));
        Assert.assertTrue(NumberUtils.isNumber("123E4"));
        Assert.assertTrue(NumberUtils.isNumber("123e+4"));
        Assert.assertTrue(NumberUtils.isNumber("123e-4"));
        Assert.assertTrue(NumberUtils.isNumber("-123e4"));
        Assert.assertTrue(NumberUtils.isNumber("123.45e6"));
        Assert.assertFalse(NumberUtils.isNumber("123e"));
        Assert.assertFalse(NumberUtils.isNumber("123e+"));
        Assert.assertFalse(NumberUtils.isNumber("123e-"));
        Assert.assertFalse(NumberUtils.isNumber("e123"));
        Assert.assertFalse(NumberUtils.isNumber("1e2e3"));
        Assert.assertFalse(NumberUtils.isNumber("123e4.5"));

        // Qualifiers
        Assert.assertTrue(NumberUtils.isNumber("123L"));
        Assert.assertTrue(NumberUtils.isNumber("123l"));
        Assert.assertTrue(NumberUtils.isNumber("123f"));
        Assert.assertTrue(NumberUtils.isNumber("123F"));
        Assert.assertTrue(NumberUtils.isNumber("123d"));
        Assert.assertTrue(NumberUtils.isNumber("123D"));
        Assert.assertTrue(NumberUtils.isNumber("123.45f"));
        Assert.assertTrue(NumberUtils.isNumber("123.45d"));
        Assert.assertTrue(NumberUtils.isNumber("123e4f"));
        Assert.assertTrue(NumberUtils.isNumber("123e4d"));

        // Invalid qualifier scenarios
        Assert.assertFalse(NumberUtils.isNumber("123.45L"));
        Assert.assertFalse(NumberUtils.isNumber("123e4L"));
        Assert.assertFalse(NumberUtils.isNumber("L"));
        Assert.assertFalse(NumberUtils.isNumber("f"));
        Assert.assertFalse(NumberUtils.isNumber("123a"));
        Assert.assertFalse(NumberUtils.isNumber("123+45"));
        Assert.assertFalse(NumberUtils.isNumber("123-45"));
    }
}
