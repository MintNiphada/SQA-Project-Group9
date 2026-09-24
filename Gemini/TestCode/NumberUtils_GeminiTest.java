package org.apache.commons.lang3.math;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * References / Citations:
 * 1. Apache Commons Lang Bug Tracker: LANG-747 / LANG-834 (Hex numbers overflow handling in NumberUtils)
 *    URL: https://issues.apache.org/jira/browse/LANG-747
 * 2. Defects4J Bug Dataset: Bug Lang-1
 *    URL: https://github.com/rjust/defects4j/tree/master/framework/projects/Lang
 */
public class NumberUtils_GeminiTest {

    @Test(timeout = 4000)
    public void testCreateNumberHexIntegerOverflow() {
        // "0x80000000" equals 2147483648L, which exceeds Integer.MAX_VALUE (2147483647).
        // On buggy versions, this throws NumberFormatException via Integer.parseInt.
        // On fixed versions, this successfully parses into a Long.
        String hexStr = "0x80000000";
        Number result = NumberUtils.createNumber(hexStr);
        assertNotNull("Parsed number should not be null", result);
        assertEquals("Should parse hex string exceeding Integer.MAX_VALUE into a Long", 
                     Long.valueOf(2147483648L), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberHexNegativeIntegerOverflow() {
        // "-0x80000000" should be parsed as Integer.MIN_VALUE or Long depending on representation
        // Testing a string that exceeds Long capacity into BigInteger if needed, 
        // or standard hex boundary cases.
        String hexStr = "0x8000000000000000";
        Number result = NumberUtils.createNumber(hexStr);
        assertNotNull("Parsed number should not be null", result);
        assertEquals("Should parse hex string exceeding Long bounds properly", 
                     new java.math.BigInteger("8000000000000000", 16), result);
    }
}
