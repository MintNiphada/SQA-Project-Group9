package org.apache.commons.lang3.math;

import org.junit.Test;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Unit tests for {@link NumberUtils}.
 */
public class NumberUtilsAITest {

    // -------------------------------------------------------------------
    // 1. Instantiation Test (JavaBean requirement)
    // -------------------------------------------------------------------
    @Test
    public void testConstructor() {
        assertNotNull(new NumberUtils());
    }

    // -------------------------------------------------------------------
    // 2. Safe Conversions (toInt, toLong, etc.)
    // -------------------------------------------------------------------
    @Test
    public void testToInt() {
        assertEquals("Null check", 0, NumberUtils.toInt(null));
        assertEquals("Blank check", 0, NumberUtils.toInt(""));
        assertEquals("Valid int", 123, NumberUtils.toInt("123"));
        assertEquals("Default fallback on invalid", 5, NumberUtils.toInt("abc", 5));
        assertEquals("Default fallback on overflow", 5, NumberUtils.toInt("9999999999999999", 5));
    }

    // -------------------------------------------------------------------
    // 3. createNumber Branch & Boundary Tests
    // -------------------------------------------------------------------
    @Test
    public void testCreateNumber_NullAndBlank() {
        assertNull("Null input returns null", NumberUtils.createNumber(null));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumber_BlankString() {
        NumberUtils.createNumber("   ");
    }

    @Test
    public void testCreateNumber_Hexadecimal() {
        assertEquals("Standard hex", Integer.valueOf(0x1F), NumberUtils.createNumber("0x1F"));
        assertEquals("Hash prefix hex", Integer.valueOf(0x1F), NumberUtils.createNumber("#1F"));
        assertEquals("Negative hex", Integer.valueOf(-0x1F), NumberUtils.createNumber("-0x1F"));
        
        // Tests for upper-case hex notation (checks potential 0X bug)
        assertEquals("Upper-case hex prefix", Integer.valueOf(0xFF), NumberUtils.createNumber("0XFF"));
        
        // Hex exceeding int digits (> 8 digits -> Long)
        assertEquals(Long.valueOf(0x123456789L), NumberUtils.createNumber("0x123456789"));
        
        // Hex exceeding long digits (> 16 digits -> BigInteger)
        assertEquals(new BigInteger("1234567890ABCDEF1", 16), 
                     NumberUtils.createNumber("0x1234567890ABCDEF1"));
    }

    @Test
    public void testCreateNumber_TypeQualifiers() {
        // Long qualifier
        assertEquals(Long.valueOf(100L), NumberUtils.createNumber("100L"));
        assertEquals(Long.valueOf(100L), NumberUtils.createNumber("100l"));

        // Float qualifier
        assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23f"));
        assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23F"));

        // Double qualifier
        assertEquals(Double.valueOf(1.23d), NumberUtils.createNumber("1.23d"));
        assertEquals(Double.valueOf(1.23d), NumberUtils.createNumber("1.23D"));
    }

    @Test
    public void testCreateNumber_TypeInferenceByPrecision() {
        // <= 7 decimals -> Float
        Number numFloat = NumberUtils.createNumber("1.123456");
        assertTrue("Inferred Float", numFloat instanceof Float);

        // 8 to 16 decimals -> Double
        Number numDouble = NumberUtils.createNumber("1.12345678901");
        assertTrue("Inferred Double", numDouble instanceof Double);

        // > 16 decimals -> BigDecimal
        Number numBigDec = NumberUtils.createNumber("1.1234567890123456789");
        assertTrue("Inferred BigDecimal", numBigDec instanceof BigDecimal);
    }

    // -------------------------------------------------------------------
    // 4. Array Aggregations (min/max validation & NaN handling)
    // -------------------------------------------------------------------
    @Test(expected = IllegalArgumentException.class)
    public void testMin_NullArray() {
        NumberUtils.min((int[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMin_EmptyArray() {
        NumberUtils.min(new int[0]);
    }

    @Test
    public void testMin_IntArray() {
        assertEquals(1, NumberUtils.min(new int[] { 5, 3, 1, 4 }));
        assertEquals(-10, NumberUtils.min(new int[] { -10, 0, 10 }));
    }

    @Test
    public void testMin_DoubleArray_NaNHandling() {
        // According to the specification, if any element is NaN, NaN should be returned
        assertTrue(Double.isNaN(NumberUtils.min(new double[] { 1.2, Double.NaN, 0.5 })));
        assertEquals(0.5, NumberUtils.min(new double[] { 1.2, 3.4, 0.5 }), 0.0001);
    }

    // -------------------------------------------------------------------
    // 5. String Format Validation (isNumber)
    // -------------------------------------------------------------------
    @Test
    public void testIsNumber() {
        assertFalse("Empty string", NumberUtils.isNumber(""));
        assertFalse("Null string", NumberUtils.isNumber(null));
        assertTrue("Valid integer", NumberUtils.isNumber("123"));
        assertTrue("Valid negative float", NumberUtils.isNumber("-1.23f"));
        assertTrue("Valid scientific", NumberUtils.isNumber("1.23e-4"));
        assertFalse("Invalid dangling exponent", NumberUtils.isNumber("123e"));
        assertFalse("Invalid multiple decimals", NumberUtils.isNumber("1.2.3"));
        assertFalse("Trailing letter without type meaning", NumberUtils.isNumber("123a"));
    }
}