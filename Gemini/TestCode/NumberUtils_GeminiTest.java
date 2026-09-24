package org.apache.commons.lang3.math;

import org.junit.Test;
import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Benchmark Crash Reproduction and Concolic Path Exploration Test Suite.
 * 
 * Comparative Study:
 * Search-based Software Testing / Crash Reproduction (Botsing)
 * vs. Dynamic Symbolic Execution / Concolic Testing (CATG)
 * vs. Large Language Model Automated Test Generation (Gemini).
 *
 * Dataset and Bug Tracking Citations:
 * 1. Defects4J Dataset:
 *    - Project: Commons-Lang
 *    - Bug ID: Lang-1 (Affects NumberUtils.createNumber)
 *    - Source: https://github.com/rjust/defects4j/tree/master/framework/projects/Lang
 * 2. Apache Commons Lang JIRA Issues:
 *    - LANG-747: NumberUtils does not handle hex numbers correctly
 *      https://issues.apache.org/jira/browse/LANG-747
 *    - LANG-834: NumberUtils.createNumber should handle # and hex overflow
 *      https://issues.apache.org/jira/browse/LANG-834
 */
public class NumberUtils_GeminiTest {

    // =========================================================================
    // Target A: Crash Reproduction Paths (Botsing Alignment)
    // =========================================================================

    /**
     * Target: 32-bit positive integer overflow boundary via hex format ("0x80000000").
     *
     * Constraint & Flow Analysis:
     * - Buggy Version (Lang-1b):
     *   The hex length condition checks (hexDigits <= 8), which evaluates to true.
     *   The method delegates directly to createInteger("0x80000000") -> Integer.decode().
     *   Because 0x80000000 (2147483648L) exceeds Integer.MAX_VALUE (2147483647),
     *   an unhandled java.lang.NumberFormatException is thrown, reproducing the crash.
     * - Fixed Version:
     *   Catches the NumberFormatException during Integer decoding and widens
     *   the numerical representation to Long, successfully yielding 2147483648L.
     */
    @Test(timeout = 4000)
    public void testCreateNumber_Hex32BitPositiveOverflow() {
        final String input = "0x80000000";
        final Number result = NumberUtils.createNumber(input);

        assertNotNull("Assertion failed: Result must not be null for input: " + input, result);
        assertEquals("Assertion failed: Hex literal 0x80000000 exceeding Integer.MAX_VALUE must widen to Long",
                Long.valueOf(2147483648L), result);
    }

    /**
     * Target: 32-bit negative integer overflow boundary via hex format ("-0x80000001").
     *
     * Constraint & Flow Analysis:
     * - Buggy Version (Lang-1b):
     *   The hex digit count is 8, triggering the integer parsing branch createInteger("-0x80000001").
     *   Since -2147483649L is strictly less than Integer.MIN_VALUE (-2147483648),
     *   Integer.decode() fails and throws NumberFormatException without promoting to Long.
     * - Fixed Version:
     *   Correctly captures the underflow condition on signed 32-bit int and widens to Long (-2147483649L).
     */
    @Test(timeout = 4000)
    public void testCreateNumber_Hex32BitNegativeOverflow() {
        final String input = "-0x80000001";
        final Number result = NumberUtils.createNumber(input);

        assertNotNull("Assertion failed: Result must not be null for input: " + input, result);
        assertEquals("Assertion failed: Hex literal -0x80000001 below Integer.MIN_VALUE must widen to Long",
                Long.valueOf(-2147483649L), result);
    }

    /**
     * Target: 64-bit Long overflow boundary via 16 hex digits ("0x8000000000000000").
     *
     * Constraint & Flow Analysis:
     * - Buggy Version (Lang-1b):
     *   Passes constraint (hexDigits <= 16) and invokes createLong("0x8000000000000000").
     *   The parsed value 9223372036854775808L exceeds Long.MAX_VALUE (9223372036854775807L).
     *   Long.decode() throws an uncaught NumberFormatException.
     * - Fixed Version:
     *   Catches the Long overflow condition and widens representation to java.math.BigInteger.
     */
    @Test(timeout = 4000)
    public void testCreateNumber_Hex64BitOverflowToBigInteger() {
        final String input = "0x8000000000000000";
        final Number result = NumberUtils.createNumber(input);

        assertNotNull("Assertion failed: Result must not be null for input: " + input, result);
        assertEquals("Assertion failed: Hex literal exceeding Long.MAX_VALUE must widen to BigInteger",
                new BigInteger("8000000000000000", 16), result);
    }

    /**
     * Target: Alternative '#' radix prefix with 32-bit integer overflow ("#80000000").
     *
     * Constraint & Flow Analysis:
     * - Buggy Version (Lang-1b):
     *   Incomplete prefix normalization or absence of overflow promotion for '#' prefixes
     *   leads to NumberFormatException when the value exceeds Integer.MAX_VALUE.
     * - Fixed Version:
     *   Treats '#' identically to '0x'/'0X', detecting the overflow and promoting the result to Long.
     */
    @Test(timeout = 4000)
    public void testCreateNumber_HexHashPrefixOverflow() {
        final String input = "#80000000";
        final Number result = NumberUtils.createNumber(input);

        assertNotNull("Assertion failed: Result must not be null for input: " + input, result);
        assertEquals("Assertion failed: Hash-prefixed hex #80000000 exceeding Integer.MAX_VALUE must widen to Long",
                Long.valueOf(2147483648L), result);
    }

    // =========================================================================
    // Target B: Concolic Path Constraints & Boundaries (CATG Alignment)
    // =========================================================================

    /**
     * Target: Boundary constraint at exact Integer.MAX_VALUE ("0x7FFFFFFF").
     *
     * Constraint & Flow Analysis:
     * - Buggy & Fixed Versions:
     *   Input length satisfies hexDigits <= 8 and fits strictly within [Integer.MIN_VALUE, Integer.MAX_VALUE].
     *   Both versions must parse this directly as java.lang.Integer without widening.
     */
    @Test(timeout = 4000)
    public void testCreateNumber_ExactIntegerMaxValueBoundary() {
        final String input = "0x7FFFFFFF";
        final Number result = NumberUtils.createNumber(input);

        assertNotNull("Assertion failed: Result must not be null for input: " + input, result);
        assertEquals("Assertion failed: 0x7FFFFFFF must evaluate exactly to Integer.MAX_VALUE",
                Integer.valueOf(Integer.MAX_VALUE), result);
    }

    /**
     * Target: Boundary constraint at exact Integer.MIN_VALUE ("-0x80000000").
     *
     * Constraint & Flow Analysis:
     * - Buggy & Fixed Versions:
     *   "-0x80000000" is mathematically equal to -2147483648, which is the lower bound of signed 32-bit int.
     *   Both versions must parse this as java.lang.Integer without throwing an exception.
     */
    @Test(timeout = 4000)
    public void testCreateNumber_ExactIntegerMinValueBoundary() {
        final String input = "-0x80000000";
        final Number result = NumberUtils.createNumber(input);

        assertNotNull("Assertion failed: Result must not be null for input: " + input, result);
        assertEquals("Assertion failed: -0x80000000 must evaluate exactly to Integer.MIN_VALUE",
                Integer.valueOf(Integer.MIN_VALUE), result);
    }

    /**
     * Target: Boundary constraint at exact Long.MAX_VALUE ("0x7FFFFFFFFFFFFFFF").
     *
     * Constraint & Flow Analysis:
     * - Buggy & Fixed Versions:
     *   Constraint hexDigits <= 16 holds, and value equals Long.MAX_VALUE (9223372036854775807L).
     *   Must parse into java.lang.Long without triggering BigInteger promotion.
     */
    @Test(timeout = 4000)
    public void testCreateNumber_ExactLongMaxValueBoundary() {
        final String input = "0x7FFFFFFFFFFFFFFF";
        final Number result = NumberUtils.createNumber(input);

        assertNotNull("Assertion failed: Result must not be null for input: " + input, result);
        assertEquals("Assertion failed: 0x7FFFFFFFFFFFFFFF must evaluate exactly to Long.MAX_VALUE",
                Long.valueOf(Long.MAX_VALUE), result);
    }

    /**
     * Target: Path constraint variant with uppercase prefix ("0X80000000").
     *
     * Constraint & Flow Analysis:
     * - Buggy Version (Lang-1b):
     *   Prefix is matched case-insensitively, but overflow into Long is unhandled in Integer.decode().
     * - Fixed Version:
     *   Correctly parses '0X' prefix, captures 32-bit overflow, and safely promotes to Long.
     */
    @Test(timeout = 4000)
    public void testCreateNumber_UppercasePrefixOverflow() {
        final String input = "0X80000000";
        final Number result = NumberUtils.createNumber(input);

        assertNotNull("Assertion failed: Result must not be null for input: " + input, result);
        assertEquals("Assertion failed: Uppercase prefix 0X80000000 exceeding Integer.MAX_VALUE must widen to Long",
                Long.valueOf(2147483648L), result);
    }

    /**
     * Target: Path constraint variant with negative sign and hash prefix ("-#80000000").
     *
     * Constraint & Flow Analysis:
     * - Buggy Version (Lang-1b):
     *   "-#" was not consistently recognized across all decode branches, leading to NumberFormatException.
     * - Fixed Version:
     *   Normalizes the negative hash prefix and parses to Integer.MIN_VALUE (-2147483648).
     */
    @Test(timeout = 4000)
    public void testCreateNumber_NegativeHashPrefixBoundary() {
        final String input = "-#80000000";
        final Number result = NumberUtils.createNumber(input);

        assertNotNull("Assertion failed: Result must not be null for input: " + input, result);
        assertEquals("Assertion failed: -#80000000 must evaluate correctly to Integer.MIN_VALUE",
                Integer.valueOf(Integer.MIN_VALUE), result);
    }

    // =========================================================================
    // Target C: Exception Paths (Syntax & Radix Guards)
    // =========================================================================

    /**
     * Target: Illegal hexadecimal character rejection ("0xG123").
     *
     * Constraint & Flow Analysis:
     * - Buggy & Fixed Versions:
     *   Character 'G' is outside valid base-16 set [0-9A-Fa-f].
     *   The constraint solver must ensure that non-hex inputs reject immediately
     *   by throwing java.lang.NumberFormatException on all versions.
     */
    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumber_InvalidHexFormatThrowsException() {
        final String invalidInput = "0xG123";
        NumberUtils.createNumber(invalidInput);
    }
}
