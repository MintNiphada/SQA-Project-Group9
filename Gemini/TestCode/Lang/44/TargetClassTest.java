package org.apache.commons.lang;

import org.junit.Assert;
import org.junit.Test;
import java.math.BigDecimal;
import java.math.BigInteger;

public class NumberUtilsTest {

    @Test
    public void testConstructor() {
        Assert.assertNotNull(new NumberUtils());
    }

    @Test
    public void testStringToInt() {
        Assert.assertEquals(0, NumberUtils.stringToInt(null));
        Assert.assertEquals(0, NumberUtils.stringToInt(""));
        Assert.assertEquals(0, NumberUtils.stringToInt("abc"));
        Assert.assertEquals(123, NumberUtils.stringToInt("123"));
        Assert.assertEquals(-123, NumberUtils.stringToInt("-123"));

        Assert.assertEquals(10, NumberUtils.stringToInt(null, 10));
        Assert.assertEquals(10, NumberUtils.stringToInt("", 10));
        Assert.assertEquals(10, NumberUtils.stringToInt("invalid", 10));
        Assert.assertEquals(42, NumberUtils.stringToInt("42", 10));
    }

    @Test
    public void testCreateNumberNullAndEmpty() {
        Assert.assertNull(NumberUtils.createNumber(null));
        Assert.assertNull(NumberUtils.createNumber("--123"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberEmptyString() {
        NumberUtils.createNumber("");
    }

    @Test
    public void testCreateNumberHex() {
        Assert.assertEquals(Integer.valueOf(255), NumberUtils.createNumber("0xFF"));
        Assert.assertEquals(Integer.valueOf(255), NumberUtils.createNumber("0xff"));
        Assert.assertEquals(Integer.valueOf(-255), NumberUtils.createNumber("-0xFF"));
    }

    @Test
    public void testCreateNumberIntegersAndLongs() {
        Assert.assertEquals(Integer.valueOf(1234), NumberUtils.createNumber("1234"));
        Assert.assertEquals(Integer.valueOf(-1234), NumberUtils.createNumber("-1234"));
        Assert.assertEquals(Long.valueOf(2147483648L), NumberUtils.createNumber("2147483648"));
        Assert.assertEquals(Long.valueOf(-2147483649L), NumberUtils.createNumber("-2147483649"));
        Assert.assertEquals(new BigInteger("9223372036854775808"), NumberUtils.createNumber("9223372036854775808"));
        Assert.assertEquals(new BigInteger("-9223372036854775809"), NumberUtils.createNumber("-9223372036854775809"));
    }

    @Test
    public void testCreateNumberDecimalsAndExponents() {
        Assert.assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23"));
        Assert.assertEquals(Double.valueOf(1.23456789012345e30), NumberUtils.createNumber("1.23456789012345e30"));
        Assert.assertEquals(new BigDecimal("1.23456789012345678901234567890e3000"), NumberUtils.createNumber("1.23456789012345678901234567890e3000"));
        Assert.assertEquals(Float.valueOf(0.0f), NumberUtils.createNumber("0.0"));
        Assert.assertEquals(Double.valueOf(0.0d), NumberUtils.createNumber("0.000000000000000000000000000000000000000000000000000000001e-300"));
        Assert.assertEquals(Float.valueOf(1e2f), NumberUtils.createNumber("1e2"));
    }

    @Test
    public void testCreateNumberTypeQualifiers() {
        Assert.assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123L"));
        Assert.assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123l"));
        Assert.assertEquals(Long.valueOf(-123L), NumberUtils.createNumber("-123L"));
        Assert.assertEquals(new BigInteger("9223372036854775808"), NumberUtils.createNumber("9223372036854775808L"));

        Assert.assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23f"));
        Assert.assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23F"));
        Assert.assertEquals(Float.valueOf(0.0f), NumberUtils.createNumber("0.0f"));
        Assert.assertEquals(Float.valueOf(0.0f), NumberUtils.createNumber("000f"));

        Assert.assertEquals(Double.valueOf(1.23d), NumberUtils.createNumber("1.23d"));
        Assert.assertEquals(Double.valueOf(1.23d), NumberUtils.createNumber("1.23D"));
        Assert.assertEquals(Double.valueOf(0.0d), NumberUtils.createNumber("0.0d"));
        Assert.assertEquals(Double.valueOf(1e200), NumberUtils.createNumber("1e200f"));
        Assert.assertEquals(new BigDecimal("1e400"), NumberUtils.createNumber("1e400f"));
        Assert.assertEquals(new BigDecimal("1e400"), NumberUtils.createNumber("1e400d"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberExpBeforeDec() {
        NumberUtils.createNumber("1e2.3");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidLong() {
        NumberUtils.createNumber("1.2L");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidLongWithExp() {
        NumberUtils.createNumber("1e2L");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidQualifier() {
        NumberUtils.createNumber("123a");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidFormat() {
        NumberUtils.createNumber("foo");
    }

    @Test
    public void testDirectCreationMethods() {
        Assert.assertEquals(Float.valueOf(3.14f), NumberUtils.createFloat("3.14"));
        Assert.assertEquals(Double.valueOf(3.14159), NumberUtils.createDouble("3.14159"));
        Assert.assertEquals(Integer.valueOf(123), NumberUtils.createInteger("123"));
        Assert.assertEquals(Integer.valueOf(123), NumberUtils.createInteger("0173"));
        Assert.assertEquals(Integer.valueOf(123), NumberUtils.createInteger("0x7b"));
        Assert.assertEquals(Long.valueOf(1234567890123L), NumberUtils.createLong("1234567890123"));
        Assert.assertEquals(new BigInteger("12345678901234567890"), NumberUtils.createBigInteger("12345678901234567890"));
        Assert.assertEquals(new BigDecimal("12345678901234567890.123456789"), NumberUtils.createBigDecimal("12345678901234567890.123456789"));
    }

    @Test
    public void testMinimumLong() {
        Assert.assertEquals(1L, NumberUtils.minimum(1L, 2L, 3L));
        Assert.assertEquals(1L, NumberUtils.minimum(2L, 1L, 3L));
        Assert.assertEquals(1L, NumberUtils.minimum(3L, 2L, 1L));
        Assert.assertEquals(1L, NumberUtils.minimum(1L, 1L, 1L));
    }

    @Test
    public void testMinimumInt() {
        Assert.assertEquals(1, NumberUtils.minimum(1, 2, 3));
        Assert.assertEquals(1, NumberUtils.minimum(2, 1, 3));
        Assert.assertEquals(1, NumberUtils.minimum(3, 2, 1));
        Assert.assertEquals(1, NumberUtils.minimum(1, 1, 1));
    }

    @Test
    public void testMaximumLong() {
        Assert.assertEquals(3L, NumberUtils.maximum(1L, 2L, 3L));
        Assert.assertEquals(3L, NumberUtils.maximum(1L, 3L, 2L));
        Assert.assertEquals(3L, NumberUtils.maximum(3L, 2L, 1L));
        Assert.assertEquals(3L, NumberUtils.maximum(3L, 3L, 3L));
    }

    @Test
    public void testMaximumInt() {
        Assert.assertEquals(3, NumberUtils.maximum(1, 2, 3));
        Assert.assertEquals(3, NumberUtils.maximum(1, 3, 2));
        Assert.assertEquals(3, NumberUtils.maximum(3, 2, 1));
        Assert.assertEquals(3, NumberUtils.maximum(3, 3, 3));
    }

    @Test
    public void testCompareDouble() {
        Assert.assertEquals(-1, NumberUtils.compare(-10.0d, 10.0d));
        Assert.assertEquals(+1, NumberUtils.compare(10.0d, -10.0d));
        Assert.assertEquals(0, NumberUtils.compare(10.0d, 10.0d));

        Assert.assertEquals(-1, NumberUtils.compare(-0.0d, +0.0d));
        Assert.assertEquals(+1, NumberUtils.compare(+0.0d, -0.0d));
        Assert.assertEquals(0, NumberUtils.compare(+0.0d, +0.0d));
        Assert.assertEquals(0, NumberUtils.compare(-0.0d, -0.0d));

        Assert.assertEquals(0, NumberUtils.compare(Double.NaN, Double.NaN));
        Assert.assertEquals(+1, NumberUtils.compare(Double.NaN, Double.POSITIVE_INFINITY));
        Assert.assertEquals(-1, NumberUtils.compare(Double.POSITIVE_INFINITY, Double.NaN));
        Assert.assertEquals(+1, NumberUtils.compare(Double.NaN, Double.MAX_VALUE));
        Assert.assertEquals(-1, NumberUtils.compare(Double.MAX_VALUE, Double.NaN));
    }

    @Test
    public void testCompareFloat() {
        Assert.assertEquals(-1, NumberUtils.compare(-10.0f, 10.0f));
        Assert.assertEquals(+1, NumberUtils.compare(10.0f, -10.0f));
        Assert.assertEquals(0, NumberUtils.compare(10.0f, 10.0f));

        Assert.assertEquals(-1, NumberUtils.compare(-0.0f, +0.0f));
        Assert.assertEquals(+1, NumberUtils.compare(+0.0f, -0.0f));
        Assert.assertEquals(0, NumberUtils.compare(+0.0f, +0.0f));
        Assert.assertEquals(0, NumberUtils.compare(-0.0f, -0.0f));

        Assert.assertEquals(0, NumberUtils.compare(Float.NaN, Float.NaN));
        Assert.assertEquals(+1, NumberUtils.compare(Float.NaN, Float.POSITIVE_INFINITY));
        Assert.assertEquals(-1, NumberUtils.compare(Float.POSITIVE_INFINITY, Float.NaN));
        Assert.assertEquals(+1, NumberUtils.compare(Float.NaN, Float.MAX_VALUE));
        Assert.assertEquals(-1, NumberUtils.compare(Float.MAX_VALUE, Float.NaN));
    }

    @Test
    public void testIsDigits() {
        Assert.assertFalse(NumberUtils.isDigits(null));
        Assert.assertFalse(NumberUtils.isDigits(""));
        Assert.assertFalse(NumberUtils.isDigits("  "));
        Assert.assertFalse(NumberUtils.isDigits("123a"));
        Assert.assertFalse(NumberUtils.isDigits("12.3"));
        Assert.assertFalse(NumberUtils.isDigits("-123"));
        Assert.assertTrue(NumberUtils.isDigits("0"));
        Assert.assertTrue(NumberUtils.isDigits("12345"));
    }

    @Test
    public void testIsNumber() {
        Assert.assertFalse(NumberUtils.isNumber(null));
        Assert.assertFalse(NumberUtils.isNumber(""));
        Assert.assertFalse(NumberUtils.isNumber("  "));
        Assert.assertFalse(NumberUtils.isNumber("foo"));
        Assert.assertFalse(NumberUtils.isNumber("0x"));
        Assert.assertFalse(NumberUtils.isNumber("-0x"));
        Assert.assertFalse(NumberUtils.isNumber("0xxyz"));
        Assert.assertTrue(NumberUtils.isNumber("0x1a"));
        Assert.assertTrue(NumberUtils.isNumber("0X1A"));
        Assert.assertTrue(NumberUtils.isNumber("-0x1a"));
        Assert.assertTrue(NumberUtils.isNumber("-0X1A"));

        Assert.assertTrue(NumberUtils.isNumber("123"));
        Assert.assertTrue(NumberUtils.isNumber("-123"));
        Assert.assertTrue(NumberUtils.isNumber("+123"));
        Assert.assertTrue(NumberUtils.isNumber("12.3"));
        Assert.assertTrue(NumberUtils.isNumber("-12.3"));
        Assert.assertTrue(NumberUtils.isNumber(".3"));
        Assert.assertTrue(NumberUtils.isNumber("-.3"));
        Assert.assertFalse(NumberUtils.isNumber("."));
        Assert.assertFalse(NumberUtils.isNumber("-."));

        Assert.assertTrue(NumberUtils.isNumber("123e1"));
        Assert.assertTrue(NumberUtils.isNumber("123E1"));
        Assert.assertTrue(NumberUtils.isNumber("123e+1"));
        Assert.assertTrue(NumberUtils.isNumber("123e-1"));
        Assert.assertFalse(NumberUtils.isNumber("123e"));
        Assert.assertFalse(NumberUtils.isNumber("123e+"));
        Assert.assertFalse(NumberUtils.isNumber("123e-"));
        Assert.assertFalse(NumberUtils.isNumber("e1"));
        Assert.assertFalse(NumberUtils.isNumber("1.2.3"));
        Assert.assertFalse(NumberUtils.isNumber("1e2e3"));
        Assert.assertFalse(NumberUtils.isNumber("1e2.3"));

        Assert.assertTrue(NumberUtils.isNumber("123L"));
        Assert.assertTrue(NumberUtils.isNumber("123l"));
        Assert.assertFalse(NumberUtils.isNumber("12.3L"));
        Assert.assertFalse(NumberUtils.isNumber("123e1L"));
        Assert.assertFalse(NumberUtils.isNumber("L"));

        Assert.assertTrue(NumberUtils.isNumber("123f"));
        Assert.assertTrue(NumberUtils.isNumber("123F"));
        Assert.assertTrue(NumberUtils.isNumber("12.3f"));
        Assert.assertTrue(NumberUtils.isNumber("123d"));
        Assert.assertTrue(NumberUtils.isNumber("123D"));
        Assert.assertTrue(NumberUtils.isNumber("12.3d"));
        Assert.assertFalse(NumberUtils.isNumber("f"));
        Assert.assertFalse(NumberUtils.isNumber("d"));
        Assert.assertFalse(NumberUtils.isNumber("123a"));
        Assert.assertFalse(NumberUtils.isNumber("123-"));
        Assert.assertFalse(NumberUtils.isNumber("123+"));
    }
}
