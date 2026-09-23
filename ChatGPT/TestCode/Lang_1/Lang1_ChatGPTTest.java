package org.apache.commons.lang3.math;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Generated-by-hand pilot test for Defects4J Lang-1.
 * Bug: NumberUtils.createNumber("0x80000000") throws NumberFormatException
 * on the buggy version but returns a Long on the fixed version.
 * This test is used to validate the full benchmark pipeline end-to-end.
 */
public class Lang1_ChatGPTTest {

    @Test(timeout = 4000)
    public void testHexOverflowBecomesLong() {
        Number n = NumberUtils.createNumber("0x80000000");
        assertNotNull("createNumber returned null", n);
        assertTrue("expected a Long, got " + n.getClass().getSimpleName(),
                n instanceof Long);
        assertEquals(2147483648L, n.longValue());
    }
}
