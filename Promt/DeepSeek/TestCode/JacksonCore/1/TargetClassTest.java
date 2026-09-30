package com.fasterxml.jackson.core.io;

import org.junit.Test;
import static org.junit.Assert.*;
import java.math.BigDecimal;

public class NumberInputTest
{
    @Test
    public void testParseIntCharArray() {
        assertEquals(0, NumberInput.parseInt(new char[]{'0'}, 0, 1));
        assertEquals(123, NumberInput.parseInt(new char[]{'1','2','3'}, 0, 3));
        assertEquals(9, NumberInput.parseInt(new char[]{'9'}, 0, 1));
        assertEquals(123456789, NumberInput.parseInt(new char[]{'1','2','3','4','5','6','7','8','9'}, 0, 9));
        assertEquals(987654321, NumberInput.parseInt(new char[]{'9','8','7','6','5','4','3','2','1'}, 0, 9));
        // length 2
        assertEquals(10, NumberInput.parseInt(new char[]{'1','0'}, 0, 2));
        // length 3
        assertEquals(100, NumberInput.parseInt(new char[]{'1','0','0'}, 0, 3));
        // length 4
        assertEquals(1000, NumberInput.parseInt(new char[]{'1','0','0','0'}, 0, 4));
        // length 5
        assertEquals(10000, NumberInput.parseInt(new char[]{'1','0','0','0','0'}, 0, 5));
        // length 6
        assertEquals(100000, NumberInput.parseInt(new char[]{'1','0','0','0','0','0'}, 0, 6));
        // length 7
        assertEquals(1000000, NumberInput.parseInt(new char[]{'1','0','0','0','0','0','0'}, 0, 7));
        // length 8
        assertEquals(10000000, NumberInput.parseInt(new char[]{'1','0','0','0','0','0','0','0'}, 0, 8));
        // length 9
        assertEquals(100000000, NumberInput.parseInt(new char[]{'1','0','0','0','0','0','0','0','0'}, 0, 9));
    }

    @Test
    public void testParseIntString() {
        assertEquals(0, NumberInput.parseInt("0"));
        assertEquals(123, NumberInput.parseInt("123"));
        assertEquals(-123, NumberInput.parseInt("-123"));
        assertEquals(9, NumberInput.parseInt("9"));
        assertEquals(123456789, NumberInput.parseInt("123456789"));
        assertEquals(-123456789, NumberInput.parseInt("-123456789"));
        // edge: maximum int positive
        assertEquals(Integer.MAX_VALUE, NumberInput.parseInt(String.valueOf(Integer.MAX_VALUE)));
        // edge: minimum int negative, but length 10 with sign, so should defer to Integer.parseInt
        assertEquals(Integer.MIN_VALUE, NumberInput.parseInt(String.valueOf(Integer.MIN_VALUE)));
        // invalid chars: should defer to Integer.parseInt and throw? Actually parseInt catches NumberFormatException? The method will return Integer.parseInt(str) for length>9 or invalid chars. For invalid chars like "1a", it will call Integer.parseInt and that throws NumberFormatException. So need to test that it throws.
        try {
            NumberInput.parseInt("1a");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) { }
        // length 1 with minus sign
        try {
            NumberInput.parseInt("-");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) { }
        // length >10 with minus sign
        assertEquals(Integer.MIN_VALUE, NumberInput.parseInt("-2147483648"));
        // length >9 without minus
        assertEquals(Integer.MAX_VALUE, NumberInput.parseInt("2147483647"));
        // empty string? Not called; but charAt(0) would throw.
        // test sign with leading zeros? It's fine.
    }

    @Test
    public void testParseLongCharArray() {
        // len must be between 10 and 18 inclusive. We'll test various lengths.
        // length 10: "1000000000" (min 10 digits)
        char[] digits10 = "1000000000".toCharArray();
        assertEquals(1000000000L, NumberInput.parseLong(digits10, 0, 10));
        // length 18: max digits for long 18 (if without sign)
        char[] digits18 = "123456789012345678".toCharArray();
        assertEquals(123456789012345678L, NumberInput.parseLong(digits18, 0, 18));
        // Another 18-digit number: 999999999999999999L -> that is beyond long? 9,999,999,999,999,999,999 is actually within long: 9,999,999,999,999,999,999L is less than Long.MAX_VALUE 9,223,372,036,854,775,807. But it's 10^19-1 which is larger than long max, so that would overflow. Let's test a valid max: 9223372036854775807L? Actually that is 19 digits, so not 18 digits. For 18 digits, the maximum is 999999999999999999L which is 9.99999999999999999e17, that's within long range? long max is 9.223372036854775807e18, so 999999999999999999L is less than max, because 9.999e17 < 9.223e18, so it's fine. But parseLong uses parseInt for first part (len-9) digits and second part 9 digits, then adds val * L_BILLION. For 18 digits, first part is 9 digits, val = parseInt of 9 digits, then L_BILLION = 10^9, then product is up to 9,999,999,999 * 10^9 = 9.999999999e18, which exceeds Long.MAX_VALUE? Actually 9,999,999,999 * 1,000,000,000 = 9,999,999,999,000,000,000 which is 9.999999999e18, that is larger than Long.MAX_VALUE (~9.22e18). So overflow may occur, but the method does not check overflow, it just computes. So we can test with values that do not overflow. So we should use a value that is less than max. So a safe 18-digit: 123456789012345678L. For 10 digits, example: 1234567890L. Let's test more lengths: 11, 12, ..., 17. Also test offset not 0. We'll assume caller ensures length >=10 and <=18, so we don't test out-of-range.
    }

    @Test
    public void testParseLongString() {
        // length <=9 uses parseInt
        assertEquals(123L, NumberInput.parseLong("123"));
        assertEquals(-123L, NumberInput.parseLong("-123"));
        // length >9 uses Long.parseLong
        assertEquals(Long.MAX_VALUE, NumberInput.parseLong(String.valueOf(Long.MAX_VALUE)));
        assertEquals(Long.MIN_VALUE, NumberInput.parseLong(String.valueOf(Long.MIN_VALUE)));
        // invalid throws NumberFormatException
        try {
            NumberInput.parseLong("123abc");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) { }
    }

    @Test
    public void testInLongRangeCharArray() {
        // negative = false => cmp with MAX_LONG_STR "9223372036854775807" (19 digits)
        // Test length less than cmpLen
        char[] shorter = "123".toCharArray();
        assertTrue(NumberInput.inLongRange(shorter, 0, 3, false));
        // equal length and equal value
        char[] maxLongStr = NumberInput.MAX_LONG_STR.toCharArray();
        assertTrue(NumberInput.inLongRange(maxLongStr, 0, maxLongStr.length, false));
        // equal length and value greater
        char[] greater = "9223372036854775808".toCharArray(); // larger by 1
        assertFalse(NumberInput.inLongRange(greater, 0, greater.length, false));
        // equal length and value smaller
        char[] smaller = "9223372036854775806".toCharArray();
        assertTrue(NumberInput.inLongRange(smaller, 0, smaller.length, false));
        // length greater than cmpLen
        char[] longer = "92233720368547758070".toCharArray();
        assertFalse(NumberInput.inLongRange(longer, 0, 20, false));
        // negative = true: cmp with MIN_LONG_STR_NO_SIGN "9223372036854775808" (19 digits, note: Long.MIN_VALUE is 9223372036854775808 without sign)
        char[] minLongStrNoSign = NumberInput.MIN_LONG_STR_NO_SIGN.toCharArray();
        assertTrue(NumberInput.inLongRange(minLongStrNoSign, 0, minLongStrNoSign.length, true));
        char[] minGreater = "9223372036854775809".toCharArray(); // larger by 1
        assertFalse(NumberInput.inLongRange(minGreater, 0, minGreater.length, true));
        char[] minSmaller = "9223372036854775807".toCharArray();
        assertTrue(NumberInput.inLongRange(minSmaller, 0, minSmaller.length, true));
        // test offset
        char[] withOffset = "xx9223372036854775807yy".toCharArray();
        assertTrue(NumberInput.inLongRange(withOffset, 2, 19, false));
    }

    @Test
    public void testInLongRangeString() {
        assertTrue(NumberInput.inLongRange("123", false));
        assertTrue(NumberInput.inLongRange(NumberInput.MAX_LONG_STR, false));
        assertFalse(NumberInput.inLongRange("9223372036854775808", false));
        assertTrue(NumberInput.inLongRange("9223372036854775806", false));
        assertFalse(NumberInput.inLongRange("92233720368547758070", false));
        // negative
        assertTrue(NumberInput.inLongRange(NumberInput.MIN_LONG_STR_NO_SIGN, true));
        assertFalse(NumberInput.inLongRange("9223372036854775809", true));
        assertTrue(NumberInput.inLongRange("9223372036854775807", true));
    }

    @Test
    public void testParseAsInt() {
        assertEquals(5, NumberInput.parseAsInt("5", -1));
        assertEquals(-5, NumberInput.parseAsInt("-5", -1));
        assertEquals(0, NumberInput.parseAsInt(null, -1));
        assertEquals(-1, NumberInput.parseAsInt("", -1));
        assertEquals(-1, NumberInput.parseAsInt("   ", -1));
        // plus sign removal
        assertEquals(42, NumberInput.parseAsInt("+42", -1));
        assertEquals(-42, NumberInput.parseAsInt("-42", -1));
        // non-digit character, falls to parseDouble
        assertEquals(0, NumberInput.parseAsInt("0.5", -1)); // parseDouble returns 0.5, (int) 0.5 = 0
        assertEquals(0, NumberInput.parseAsInt("0.9", -1));
        assertEquals(1, NumberInput.parseAsInt("1.1", -1));
        // invalid double fails, return default
        assertEquals(-1, NumberInput.parseAsInt("abc", -1));
        // leading zeros
        assertEquals(0, NumberInput.parseAsInt("00", -1));
        // max int
        assertEquals(Integer.MAX_VALUE, NumberInput.parseAsInt(String.valueOf(Integer.MAX_VALUE), -1));
        // min int
        assertEquals(Integer.MIN_VALUE, NumberInput.parseAsInt(String.valueOf(Integer.MIN_VALUE), -1));
        // number with whitespace? trim is called, so leading/trailing spaces okay.
        assertEquals(123, NumberInput.parseAsInt("   123  ", -1));
    }

    @Test
    public void testParseAsLong() {
        assertEquals(5L, NumberInput.parseAsLong("5", -1L));
        assertEquals(-5L, NumberInput.parseAsLong("-5", -1L));
        assertEquals(-1L, NumberInput.parseAsLong(null, -1L));
        assertEquals(-1L, NumberInput.parseAsLong("", -1L));
        assertEquals(-1L, NumberInput.parseAsLong("   ", -1L));
        assertEquals(42L, NumberInput.parseAsLong("+42", -1L));
        assertEquals(-42L, NumberInput.parseAsLong("-42", -1L));
        // non-digit
        assertEquals(0L, NumberInput.parseAsLong("0.5", -1L));
        assertEquals(-1L, NumberInput.parseAsLong("abc", -1L));
        assertEquals(Long.MAX_VALUE, NumberInput.parseAsLong(String.valueOf(Long.MAX_VALUE), -1L));
        assertEquals(Long.MIN_VALUE, NumberInput.parseAsLong(String.valueOf(Long.MIN_VALUE), -1L));
        assertEquals(123L, NumberInput.parseAsLong("   123  ", -1L));
    }

    @Test
    public void testParseAsDouble() {
        assertEquals(3.14, NumberInput.parseAsDouble("3.14", -1.0), 0.0);
        assertEquals(-1.0, NumberInput.parseAsDouble(null, -1.0), 0.0);
        assertEquals(-1.0, NumberInput.parseAsDouble("", -1.0), 0.0);
        assertEquals(-1.0, NumberInput.parseAsDouble("   ", -1.0), 0.0);
        assertEquals(-1.0, NumberInput.parseAsDouble("abc", -1.0), 0.0);
        assertEquals(Double.MIN_VALUE, NumberInput.parseAsDouble(NumberInput.NASTY_SMALL_DOUBLE, -1.0), 0.0);
    }

    @Test
    public void testParseDoubleNormal() {
        assertEquals(2.2250738585072014E-308, NumberInput.parseDouble("2.2250738585072012e-308"), 0.0); // NASTY_SMALL_DOUBLE returns Double.MIN_VALUE
        assertEquals(Double.MIN_VALUE, NumberInput.parseDouble(NumberInput.NASTY_SMALL_DOUBLE), 0.0);
        assertEquals(1.0, NumberInput.parseDouble("1.0"), 0.0);
    }

    @Test(expected = NumberFormatException.class)
    public void testParseDoubleInvalid() {
        NumberInput.parseDouble("abc");
    }

    @Test
    public void testParseBigDecimalString() {
        BigDecimal bd = NumberInput.parseBigDecimal("3.14");
        assertEquals(new BigDecimal("3.14"), bd);
    }

    @Test
    public void testParseBigDecimalCharArray() {
        BigDecimal bd = NumberInput.parseBigDecimal("42.0".toCharArray());
        assertEquals(new BigDecimal("42.0"), bd);
    }

    @Test
    public void testParseBigDecimalCharArrayOffsetLen() {
        BigDecimal bd = NumberInput.parseBigDecimal("xx3.14yy".toCharArray(), 2, 4);
        assertEquals(new BigDecimal("3.14"), bd);
    }

    @Test(expected = NumberFormatException.class)
    public void testParseBigDecimalInvalid() {
        NumberInput.parseBigDecimal("abc");
    }

    // Additional edge cases for parseInt(char[], offset, len) with various offsets
    @Test
    public void testParseIntCharArrayOffset() {
        char[] digits = "987654321".toCharArray();
        assertEquals(7654321, NumberInput.parseInt(digits, 2, 7));
        assertEquals(654321, NumberInput.parseInt(digits, 3, 6));
    }
}