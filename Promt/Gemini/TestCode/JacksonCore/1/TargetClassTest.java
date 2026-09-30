package com.fasterxml.jackson.core.io;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

public class NumberInputTest {

    @Test
    public void testConstructor() {
        NumberInput input = new NumberInput();
        Assert.assertNotNull(input);
    }

    @Test
    public void testParseIntCharArray() {
        char[] chars = "1234567890".toCharArray();
        
        Assert.assertEquals(1, NumberInput.parseInt(chars, 0, 1));
        Assert.assertEquals(12, NumberInput.parseInt(chars, 0, 2));
        Assert.assertEquals(123, NumberInput.parseInt(chars, 0, 3));
        Assert.assertEquals(1234, NumberInput.parseInt(chars, 0, 4));
        Assert.assertEquals(12345, NumberInput.parseInt(chars, 0, 5));
        Assert.assertEquals(123456, NumberInput.parseInt(chars, 0, 6));
        Assert.assertEquals(1234567, NumberInput.parseInt(chars, 0, 7));
        Assert.assertEquals(12345678, NumberInput.parseInt(chars, 0, 8));
        Assert.assertEquals(123456789, NumberInput.parseInt(chars, 0, 9));

        Assert.assertEquals(5678, NumberInput.parseInt(chars, 4, 4));
        Assert.assertEquals(0, NumberInput.parseInt(chars, 9, 1));
    }

    @Test
    public void testParseIntStringPositive() {
        Assert.assertEquals(0, NumberInput.parseInt("0"));
        Assert.assertEquals(1, NumberInput.parseInt("1"));
        Assert.assertEquals(12, NumberInput.parseInt("12"));
        Assert.assertEquals(123, NumberInput.parseInt("123"));
        Assert.assertEquals(1234, NumberInput.parseInt("1234"));
        Assert.assertEquals(12345, NumberInput.parseInt("12345"));
        Assert.assertEquals(123456, NumberInput.parseInt("123456"));
        Assert.assertEquals(1234567, NumberInput.parseInt("1234567"));
        Assert.assertEquals(12345678, NumberInput.parseInt("12345678"));
        Assert.assertEquals(123456789, NumberInput.parseInt("123456789"));
        Assert.assertEquals(1000000000, NumberInput.parseInt("1000000000"));
        Assert.assertEquals(Integer.MAX_VALUE, NumberInput.parseInt(String.valueOf(Integer.MAX_VALUE)));
    }

    @Test
    public void testParseIntStringNegative() {
        Assert.assertEquals(-1, NumberInput.parseInt("-1"));
        Assert.assertEquals(-12, NumberInput.parseInt("-12"));
        Assert.assertEquals(-123, NumberInput.parseInt("-123"));
        Assert.assertEquals(-1234, NumberInput.parseInt("-1234"));
        Assert.assertEquals(-12345, NumberInput.parseInt("-12345"));
        Assert.assertEquals(-123456, NumberInput.parseInt("-123456"));
        Assert.assertEquals(-1234567, NumberInput.parseInt("-1234567"));
        Assert.assertEquals(-12345678, NumberInput.parseInt("-12345678"));
        Assert.assertEquals(-123456789, NumberInput.parseInt("-123456789"));
        Assert.assertEquals(-1000000000, NumberInput.parseInt("-1000000000"));
        Assert.assertEquals(Integer.MIN_VALUE, NumberInput.parseInt(String.valueOf(Integer.MIN_VALUE)));
    }

    @Test
    public void testParseIntStringNonDigitBranches() {
        // Non-digit on 1st char
        try {
            NumberInput.parseInt("a");
            Assert.fail("Expected NumberFormatException");
        } catch (NumberFormatException expected) {}

        // Non-digit on 2nd char
        try {
            NumberInput.parseInt("1a");
            Assert.fail("Expected NumberFormatException");
        } catch (NumberFormatException expected) {}

        // Non-digit on 3rd char
        try {
            NumberInput.parseInt("12a");
            Assert.fail("Expected NumberFormatException");
        } catch (NumberFormatException expected) {}

        // Non-digit on 4th char
        try {
            NumberInput.parseInt("123a");
            Assert.fail("Expected NumberFormatException");
        } catch (NumberFormatException expected) {}

        // Non-digit on 5th char
        try {
            NumberInput.parseInt("1234a");
            Assert.fail("Expected NumberFormatException");
        } catch (NumberFormatException expected) {}

        // Negative with non-digit
        try {
            NumberInput.parseInt("-a");
            Assert.fail("Expected NumberFormatException");
        } catch (NumberFormatException expected) {}

        try {
            NumberInput.parseInt("-1a");
            Assert.fail("Expected NumberFormatException");
        } catch (NumberFormatException expected) {}

        try {
            NumberInput.parseInt("-12a");
            Assert.fail("Expected NumberFormatException");
        } catch (NumberFormatException expected) {}

        try {
            NumberInput.parseInt("-123a");
            Assert.fail("Expected NumberFormatException");
        } catch (NumberFormatException expected) {}

        // Minus only
        try {
            NumberInput.parseInt("-");
            Assert.fail("Expected NumberFormatException");
        } catch (NumberFormatException expected) {}

        // Overflow negative string (> 10 chars)
        try {
            NumberInput.parseInt("-12345678901");
            Assert.fail("Expected NumberFormatException");
        } catch (NumberFormatException expected) {}

        // Character below '0'
        try {
            NumberInput.parseInt("/12");
            Assert.fail("Expected NumberFormatException");
        } catch (NumberFormatException expected) {}

        try {
            NumberInput.parseInt("1/2");
            Assert.fail("Expected NumberFormatException");
        } catch (NumberFormatException expected) {}

        try {
            NumberInput.parseInt("12/");
            Assert.fail("Expected NumberFormatException");
        } catch (NumberFormatException expected) {}

        try {
            NumberInput.parseInt("123/");
            Assert.fail("Expected NumberFormatException");
        } catch (NumberFormatException expected) {}
    }

    @Test
    public void testParseLongCharArray() {
        char[] chars = "123456789012345678".toCharArray();
        long result = NumberInput.parseLong(chars, 0, 18);
        Assert.assertEquals(123456789012345678L, result);

        char[] chars10 = "1234567890".toCharArray();
        Assert.assertEquals(1234567890L, NumberInput.parseLong(chars10, 0, 10));

        char[] offsetChars = "prefix12345678901234suffix".toCharArray();
        Assert.assertEquals(12345678901234L, NumberInput.parseLong(offsetChars, 6, 14));
    }

    @Test
    public void testParseLongString() {
        Assert.assertEquals(0L, NumberInput.parseLong("0"));
        Assert.assertEquals(123456789L, NumberInput.parseLong("123456789"));
        Assert.assertEquals(1234567890L, NumberInput.parseLong("1234567890"));
        Assert.assertEquals(Long.MAX_VALUE, NumberInput.parseLong(String.valueOf(Long.MAX_VALUE)));
        Assert.assertEquals(Long.MIN_VALUE, NumberInput.parseLong(String.valueOf(Long.MIN_VALUE)));
        Assert.assertEquals(-1234567890L, NumberInput.parseLong("-1234567890"));
    }

    @Test
    public void testInLongRangeCharArray() {
        char[] maxLong = NumberInput.MAX_LONG_STR.toCharArray();
        Assert.assertTrue(NumberInput.inLongRange(maxLong, 0, maxLong.length, false));

        char[] minLongNoSign = NumberInput.MIN_LONG_STR_NO_SIGN.toCharArray();
        Assert.assertTrue(NumberInput.inLongRange(minLongNoSign, 0, minLongNoSign.length, true));

        // Shorter length
        char[] shortDigits = "12345".toCharArray();
        Assert.assertTrue(NumberInput.inLongRange(shortDigits, 0, shortDigits.length, false));
        Assert.assertTrue(NumberInput.inLongRange(shortDigits, 0, shortDigits.length, true));

        // Longer length
        char[] longDigits = "12345678901234567890".toCharArray();
        Assert.assertFalse(NumberInput.inLongRange(longDigits, 0, longDigits.length, false));
        Assert.assertFalse(NumberInput.inLongRange(longDigits, 0, longDigits.length, true));

        // Same length, smaller values
        char[] smaller = "9223372036854775806".toCharArray();
        Assert.assertTrue(NumberInput.inLongRange(smaller, 0, smaller.length, false));
        Assert.assertTrue(NumberInput.inLongRange(smaller, 0, smaller.length, true));

        // Same length, larger values
        char[] larger = "9223372036854775808".toCharArray();
        Assert.assertFalse(NumberInput.inLongRange(larger, 0, larger.length, false));
        Assert.assertTrue(NumberInput.inLongRange(larger, 0, larger.length, true));

        char[] tooLargeNegative = "9223372036854775809".toCharArray();
        Assert.assertFalse(NumberInput.inLongRange(tooLargeNegative, 0, tooLargeNegative.length, true));

        // Difference in earlier digits
        char[] earlierDiffSmall = "8223372036854775807".toCharArray();
        Assert.assertTrue(NumberInput.inLongRange(earlierDiffSmall, 0, earlierDiffSmall.length, false));

        char[] earlierDiffLarge = "9323372036854775807".toCharArray();
        Assert.assertFalse(NumberInput.inLongRange(earlierDiffLarge, 0, earlierDiffLarge.length, false));
    }

    @Test
    public void testInLongRangeString() {
        Assert.assertTrue(NumberInput.inLongRange(NumberInput.MAX_LONG_STR, false));
        Assert.assertTrue(NumberInput.inLongRange(NumberInput.MIN_LONG_STR_NO_SIGN, true));

        // Shorter length
        Assert.assertTrue(NumberInput.inLongRange("12345", false));
        Assert.assertTrue(NumberInput.inLongRange("12345", true));

        // Longer length
        Assert.assertFalse(NumberInput.inLongRange("12345678901234567890", false));
        Assert.assertFalse(NumberInput.inLongRange("12345678901234567890", true));

        // Same length, smaller
        Assert.assertTrue(NumberInput.inLongRange("9223372036854775806", false));
        Assert.assertTrue(NumberInput.inLongRange("9223372036854775806", true));

        // Same length, larger for max long, exact for min long no sign
        Assert.assertFalse(NumberInput.inLongRange("9223372036854775808", false));
        Assert.assertTrue(NumberInput.inLongRange("9223372036854775808", true));

        // Same length, too large for min long
        Assert.assertFalse(NumberInput.inLongRange("9223372036854775809", true));

        // Difference at first digit
        Assert.assertTrue(NumberInput.inLongRange("1000000000000000000", false));
        Assert.assertFalse(NumberInput.inLongRange("9999999999999999999", false));
    }

    @Test
    public void testParseAsInt() {
        Assert.assertEquals(10, NumberInput.parseAsInt(null, 10));
        Assert.assertEquals(10, NumberInput.parseAsInt("", 10));
        Assert.assertEquals(10, NumberInput.parseAsInt("   ", 10));

        Assert.assertEquals(123, NumberInput.parseAsInt("123", 0));
        Assert.assertEquals(123, NumberInput.parseAsInt("+123", 0));
        Assert.assertEquals(-123, NumberInput.parseAsInt("-123", 0));

        // Double coercion
        Assert.assertEquals(123, NumberInput.parseAsInt("123.45", 0));
        Assert.assertEquals(-123, NumberInput.parseAsInt("-123.45", 0));
        Assert.assertEquals(120, NumberInput.parseAsInt("1.2e2", 0));

        // Malformed double coercion
        Assert.assertEquals(99, NumberInput.parseAsInt("123.45.67", 99));
        Assert.assertEquals(99, NumberInput.parseAsInt("abc", 99));
        Assert.assertEquals(99, NumberInput.parseAsInt("+", 99));
        Assert.assertEquals(99, NumberInput.parseAsInt("-", 99));

        // Overflow int but within int parse logic
        Assert.assertEquals(99, NumberInput.parseAsInt("999999999999999999999999", 99));
    }

    @Test
    public void testParseAsLong() {
        Assert.assertEquals(10L, NumberInput.parseAsLong(null, 10L));
        Assert.assertEquals(10L, NumberInput.parseAsLong("", 10L));
        Assert.assertEquals(10L, NumberInput.parseAsLong("   ", 10L));

        Assert.assertEquals(123L, NumberInput.parseAsLong("123", 0L));
        Assert.assertEquals(123L, NumberInput.parseAsLong("+123", 0L));
        Assert.assertEquals(-123L, NumberInput.parseAsLong("-123", 0L));
        Assert.assertEquals(123456789012345L, NumberInput.parseAsLong("123456789012345", 0L));

        // Double coercion
        Assert.assertEquals(123L, NumberInput.parseAsLong("123.45", 0L));
        Assert.assertEquals(-123L, NumberInput.parseAsLong("-123.45", 0L));
        Assert.assertEquals(120L, NumberInput.parseAsLong("1.2e2", 0L));

        // Malformed double coercion
        Assert.assertEquals(99L, NumberInput.parseAsLong("123.45.67", 99L));
        Assert.assertEquals(99L, NumberInput.parseAsLong("abc", 99L));
        Assert.assertEquals(99L, NumberInput.parseAsLong("+", 99L));
        Assert.assertEquals(99L, NumberInput.parseAsLong("-", 99L));

        // Overflow long but within long parse logic
        Assert.assertEquals(99L, NumberInput.parseAsLong("9999999999999999999999999999999999999", 99L));
    }

    @Test
    public void testParseAsDouble() {
        Assert.assertEquals(10.5, NumberInput.parseAsDouble(null, 10.5), 0.00001);
        Assert.assertEquals(10.5, NumberInput.parseAsDouble("", 10.5), 0.00001);
        Assert.assertEquals(10.5, NumberInput.parseAsDouble("   ", 10.5), 0.00001);

        Assert.assertEquals(123.45, NumberInput.parseAsDouble("123.45", 0.0), 0.00001);
        Assert.assertEquals(-123.45, NumberInput.parseAsDouble("-123.45", 0.0), 0.00001);
        Assert.assertEquals(123.0, NumberInput.parseAsDouble("123", 0.0), 0.00001);
        Assert.assertEquals(99.0, NumberInput.parseAsDouble("invalid", 99.0), 0.00001);
    }

    @Test
    public void testParseDouble() {
        Assert.assertEquals(Double.MIN_VALUE, NumberInput.parseDouble(NumberInput.NASTY_SMALL_DOUBLE), 0.0);
        Assert.assertEquals(123.456, NumberInput.parseDouble("123.456"), 0.00001);
        Assert.assertEquals(-0.5, NumberInput.parseDouble("-0.5"), 0.00001);
        Assert.assertEquals(0.0, NumberInput.parseDouble("0.0"), 0.00001);

        try {
            NumberInput.parseDouble("not-a-double");
            Assert.fail("Expected NumberFormatException");
        } catch (NumberFormatException expected) {}
    }

    @Test
    public void testParseBigDecimal() {
        BigDecimal bd1 = NumberInput.parseBigDecimal("12345.67890");
        Assert.assertEquals(new BigDecimal("12345.67890"), bd1);

        char[] chars = "prefix12345.67890suffix".toCharArray();
        BigDecimal bd2 = NumberInput.parseBigDecimal(chars, 6, 11);
        Assert.assertEquals(new BigDecimal("12345.67890"), bd2);

        char[] fullChars = "98765.4321".toCharArray();
        BigDecimal bd3 = NumberInput.parseBigDecimal(fullChars);
        Assert.assertEquals(new BigDecimal("98765.4321"), bd3);
    }

    @Test(expected = NumberFormatException.class)
    public void testParseBigDecimalInvalidString() {
        NumberInput.parseBigDecimal("invalid");
    }

    @Test(expected = NumberFormatException.class)
    public void testParseBigDecimalInvalidCharArray() {
        char[] invalid = "invalid".toCharArray();
        NumberInput.parseBigDecimal(invalid);
    }

    @Test(expected = NumberFormatException.class)
    public void testParseBigDecimalInvalidCharArrayOffset() {
        char[] invalid = "abc".toCharArray();
        NumberInput.parseBigDecimal(invalid, 0, 3);
    }
}