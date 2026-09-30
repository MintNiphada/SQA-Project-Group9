package org.apache.commons.lang3.math;

import static org.junit.Assert.*;
import org.junit.Test;

public class NumberUtilsTest {

    // Test constants (sanity)
    @Test
    public void testConstants() {
        assertEquals(0L, (long) NumberUtils.LONG_ZERO);
        assertEquals(1L, (long) NumberUtils.LONG_ONE);
        assertEquals(-1L, (long) NumberUtils.LONG_MINUS_ONE);
        assertEquals(0, (int) NumberUtils.INTEGER_ZERO);
        assertEquals(1, (int) NumberUtils.INTEGER_ONE);
        assertEquals(-1, (int) NumberUtils.INTEGER_MINUS_ONE);
        assertEquals(0, (short) NumberUtils.SHORT_ZERO);
        assertEquals(1, (short) NumberUtils.SHORT_ONE);
        assertEquals(-1, (short) NumberUtils.SHORT_MINUS_ONE);
        assertEquals(0, (byte) NumberUtils.BYTE_ZERO);
        assertEquals(1, (byte) NumberUtils.BYTE_ONE);
        assertEquals(-1, (byte) NumberUtils.BYTE_MINUS_ONE);
        assertEquals(0.0, NumberUtils.DOUBLE_ZERO, 0);
        assertEquals(1.0, NumberUtils.DOUBLE_ONE, 0);
        assertEquals(-1.0, NumberUtils.DOUBLE_MINUS_ONE, 0);
        assertEquals(0.0f, NumberUtils.FLOAT_ZERO, 0);
        assertEquals(1.0f, NumberUtils.FLOAT_ONE, 0);
        assertEquals(-1.0f, NumberUtils.FLOAT_MINUS_ONE, 0);
    }

    // Constructor test
    @Test
    public void testConstructor() {
        NumberUtils nu = new NumberUtils();
        assertNotNull(nu);
    }

    // toInt(String)
    @Test
    public void testToIntString() {
        assertEquals(0, NumberUtils.toInt(null));
        assertEquals(0, NumberUtils.toInt(""));
        assertEquals(0, NumberUtils.toInt(" "));
        assertEquals(1, NumberUtils.toInt("1"));
        assertEquals(-1, NumberUtils.toInt("-1"));
        assertEquals(123, NumberUtils.toInt("123"));
        assertEquals(0, NumberUtils.toInt("abc"));
        assertEquals(0, NumberUtils.toInt("123.4"));
    }

    @Test
    public void testToIntStringInt() {
        assertEquals(5, NumberUtils.toInt(null, 5));
        assertEquals(5, NumberUtils.toInt("", 5));
        assertEquals(5, NumberUtils.toInt(" ", 5));
        assertEquals(1, NumberUtils.toInt("1", 5));
        assertEquals(-1, NumberUtils.toInt("-1", 5));
        assertEquals(123, NumberUtils.toInt("123", 5));
        assertEquals(5, NumberUtils.toInt("abc", 5));
        assertEquals(5, NumberUtils.toInt("123.4", 5));
        assertEquals(Integer.MAX_VALUE, NumberUtils.toInt(String.valueOf(Integer.MAX_VALUE), 5));
        assertEquals(5, NumberUtils.toInt("2147483648", 5)); // overflow
        assertEquals(5, NumberUtils.toInt("-2147483649", 5));
    }

    // toLong(String)
    @Test
    public void testToLongString() {
        assertEquals(0L, NumberUtils.toLong(null));
        assertEquals(0L, NumberUtils.toLong(""));
        assertEquals(1L, NumberUtils.toLong("1"));
        assertEquals(-1L, NumberUtils.toLong("-1"));
        assertEquals(0L, NumberUtils.toLong("abc"));
        assertEquals(0L, NumberUtils.toLong("123.4"));
    }

    @Test
    public void testToLongStringLong() {
        assertEquals(5L, NumberUtils.toLong(null, 5L));
        assertEquals(5L, NumberUtils.toLong("", 5L));
        assertEquals(1L, NumberUtils.toLong("1", 5L));
        assertEquals(-1L, NumberUtils.toLong("-1", 5L));
        assertEquals(5L, NumberUtils.toLong("abc", 5L));
        assertEquals(Long.MAX_VALUE, NumberUtils.toLong(String.valueOf(Long.MAX_VALUE), 5L));
        assertEquals(5L, NumberUtils.toLong("9223372036854775808", 5L)); // overflow
    }

    // toFloat(String)
    @Test
    public void testToFloatString() {
        assertEquals(0.0f, NumberUtils.toFloat(null), 0);
        assertEquals(0.0f, NumberUtils.toFloat(""), 0);
        assertEquals(1.5f, NumberUtils.toFloat("1.5"), 0);
        assertEquals(0.0f, NumberUtils.toFloat("abc"), 0);
    }

    @Test
    public void testToFloatStringFloat() {
        assertEquals(1.1f, NumberUtils.toFloat(null, 1.1f), 0);
        assertEquals(1.1f, NumberUtils.toFloat("", 1.1f), 0);
        assertEquals(1.5f, NumberUtils.toFloat("1.5", 1.1f), 0);
        assertEquals(1.1f, NumberUtils.toFloat("abc", 1.1f), 0);
        assertEquals(Float.MAX_VALUE, NumberUtils.toFloat(String.valueOf(Float.MAX_VALUE), 1.1f), 0);
        assertEquals(1.1f, NumberUtils.toFloat("1e1000", 1.1f), 0); // overflow
    }

    // toDouble(String)
    @Test
    public void testToDoubleString() {
        assertEquals(0.0, NumberUtils.toDouble(null), 0);
        assertEquals(0.0, NumberUtils.toDouble(""), 0);
        assertEquals(1.5, NumberUtils.toDouble("1.5"), 0);
        assertEquals(0.0, NumberUtils.toDouble("abc"), 0);
    }

    @Test
    public void testToDoubleStringDouble() {
        assertEquals(1.1, NumberUtils.toDouble(null, 1.1), 0);
        assertEquals(1.1, NumberUtils.toDouble("", 1.1), 0);
        assertEquals(1.5, NumberUtils.toDouble("1.5", 1.1), 0);
        assertEquals(1.1, NumberUtils.toDouble("abc", 1.1), 0);
        assertEquals(Double.MAX_VALUE, NumberUtils.toDouble(String.valueOf(Double.MAX_VALUE), 1.1), 0);
        assertEquals(1.1, NumberUtils.toDouble("1e1000", 1.1), 0); // overflow
    }

    // toByte(String)
    @Test
    public void testToByteString() {
        assertEquals(0, NumberUtils.toByte(null));
        assertEquals(0, NumberUtils.toByte(""));
        assertEquals(1, NumberUtils.toByte("1"));
        assertEquals(-1, NumberUtils.toByte("-1"));
        assertEquals(0, NumberUtils.toByte("abc"));
        assertEquals(0, NumberUtils.toByte("200")); // overflow
    }

    @Test
    public void testToByteStringByte() {
        assertEquals(5, NumberUtils.toByte(null, (byte) 5));
        assertEquals(5, NumberUtils.toByte("", (byte) 5));
        assertEquals(1, NumberUtils.toByte("1", (byte) 5));
        assertEquals(-1, NumberUtils.toByte("-1", (byte) 5));
        assertEquals(5, NumberUtils.toByte("abc", (byte) 5));
        assertEquals(Byte.MAX_VALUE, NumberUtils.toByte(String.valueOf(Byte.MAX_VALUE), (byte) 5));
        assertEquals(5, NumberUtils.toByte("128", (byte) 5)); // overflow
    }

    // toShort(String)
    @Test
    public void testToShortString() {
        assertEquals(0, NumberUtils.toShort(null));
        assertEquals(0, NumberUtils.toShort(""));
        assertEquals(1, NumberUtils.toShort("1"));
        assertEquals(-1, NumberUtils.toShort("-1"));
        assertEquals(0, NumberUtils.toShort("abc"));
        assertEquals(0, NumberUtils.toShort("40000")); // overflow
    }

    @Test
    public void testToShortStringShort() {
        assertEquals(5, NumberUtils.toShort(null, (short) 5));
        assertEquals(5, NumberUtils.toShort("", (short) 5));
        assertEquals(1, NumberUtils.toShort("1", (short) 5));
        assertEquals(-1, NumberUtils.toShort("-1", (short) 5));
        assertEquals(5, NumberUtils.toShort("abc", (short) 5));
        assertEquals(Short.MAX_VALUE, NumberUtils.toShort(String.valueOf(Short.MAX_VALUE), (short) 5));
        assertEquals(5, NumberUtils.toShort("32768", (short) 5)); // overflow
    }

    // createNumber
    @Test
    public void testCreateNumberNull() {
        assertNull(NumberUtils.createNumber(null));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberBlank() {
        NumberUtils.createNumber("");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberWhitespace() {
        NumberUtils.createNumber("   ");
    }

    // simple integer
    @Test
    public void testCreateNumberSimpleInteger() {
        assertEquals(Integer.valueOf(123), NumberUtils.createNumber("123"));
        assertEquals(Integer.valueOf(-123), NumberUtils.createNumber("-123"));
    }

    // hex prefixes
    @Test
    public void testCreateNumberHex() {
        assertEquals(Integer.valueOf(0x1), NumberUtils.createNumber("0x1"));
        assertEquals(Integer.valueOf(0xF), NumberUtils.createNumber("0xF"));
        assertEquals(Long.valueOf(0x123456789L), NumberUtils.createNumber("0x123456789")); // 9 hex digits
        assertEquals(Long.valueOf(0x1234567890L), NumberUtils.createNumber("0x1234567890")); // borderline?
        // 17 hex digits -> BigInteger
        assertTrue(NumberUtils.createNumber("0x1234567890ABCDEF0") instanceof java.math.BigInteger);
        // negative hex
        assertEquals(Long.valueOf(-0x123456789L), NumberUtils.createNumber("-0x123456789"));
        // # prefix
        assertEquals(Integer.valueOf(0x1), NumberUtils.createNumber("#1"));
        assertEquals(Long.valueOf(0x123456789L), NumberUtils.createNumber("#123456789"));
        // -# prefix
        assertEquals(Integer.valueOf(-0x1), NumberUtils.createNumber("-#1"));
    }

    // hex with too many digits for Long -> BigInteger
    @Test
    public void testCreateNumberHexBigInteger() {
        Number n = NumberUtils.createNumber("0x1234567890ABCDEF0");
        assertTrue(n instanceof java.math.BigInteger);
        assertEquals(new java.math.BigInteger("0x1234567890ABCDEF0".substring(2), 16), n);
    }

    // test hex with trailing type qualifiers? The parser ignores them?
    // But the method first checks hex prefix, so it won't see lastChar. So "0x1L" would be hexDigits=1, return createInteger("0x1L") which will throw because decode can't parse 'L'.
    // We'll test that it throws NFE.
    @Test(expected = NumberFormatException.class)
    public void testCreateNumberHexWithQualifier() {
        NumberUtils.createNumber("0x1L");
    }

    // test type qualifiers l/L
    @Test
    public void testCreateNumberLongQualifier() {
        assertEquals(Long.valueOf(10L), NumberUtils.createNumber("10L"));
        assertEquals(Long.valueOf(10L), NumberUtils.createNumber("10l"));
        assertEquals(Long.valueOf(-10L), NumberUtils.createNumber("-10L"));
    }

    // test long qualifier with decimal or exp -> NFE
    @Test(expected = NumberFormatException.class)
    public void testCreateNumberLongQualifierWithDecimal() {
        NumberUtils.createNumber("10.0L");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberLongQualifierWithExp() {
        NumberUtils.createNumber("10E2L");
    }

    // test large number with L -> fallback to BigInteger
    @Test
    public void testCreateNumberBigIntegerFromLongQualifier() {
        Number n = NumberUtils.createNumber("12345678901234567890L");
        assertTrue(n instanceof java.math.BigInteger);
        assertEquals(new java.math.BigInteger("12345678901234567890"), n);
    }

    // test f/F qualifier
    @Test
    public void testCreateNumberFloatQualifier() {
        assertEquals(Float.valueOf(1.0f), NumberUtils.createNumber("1.0f"));
        assertEquals(Float.valueOf(1.0f), NumberUtils.createNumber("1.0F"));
        assertEquals(Float.valueOf(-1.0f), NumberUtils.createNumber("-1.0F"));
    }

    // test f qualifier with large value that exceeds float precision -> fallback to Double or BigDecimal
    @Test
    public void testCreateNumberFloatQualifierTooLarge() {
        // Use a string that would make Float overflow -> return Double or BigDecimal
        Number n = NumberUtils.createNumber("3.4028236E38f"); // close to max float, might be okay, but a bigger one
        // We can test with something like "3.4e38f" might be okay, but to trigger fallback we can add many digits
        // Actually, the code tries Float, if infinite or zero with non-all-zeros falls through. So if we pass "1e39f" that is inside Float? 1e39 > Float.MAX_VALUE -> Float returns Infinity, so it falls through to Double.
        n = NumberUtils.createNumber("1e39f");
        assertTrue(n instanceof Double); // or BigDecimal if Double also infinite
    }

    // test d/D qualifier
    @Test
    public void testCreateNumberDoubleQualifier() {
        assertEquals(Double.valueOf(1.0), NumberUtils.createNumber("1.0d"));
        assertEquals(Double.valueOf(1.0), NumberUtils.createNumber("1.0D"));
        assertEquals(Double.valueOf(-1.0), NumberUtils.createNumber("-1.0D"));
    }

    // test d qualifier with too large -> BigDecimal
    @Test
    public void testCreateNumberDoubleQualifierTooLarge() {
        // Force Double INF then fallback to BigDecimal
        Number n = NumberUtils.createNumber("1e309d"); // 1e309 > Double.MAX_VALUE
        assertTrue(n instanceof java.math.BigDecimal);
    }

    // test default qualifier
    @Test(expected = NumberFormatException.class)
    public void testCreateNumberBadQualifier() {
        NumberUtils.createNumber("123X");
    }

    // test decimal and no qualifier -> Float, Double, BigDecimal
    @Test
    public void testCreateNumberDecimal() {
        Number n = NumberUtils.createNumber("1.5");
        assertTrue(n instanceof Float);
        assertEquals(1.5f, n.floatValue(), 0);
    }

    @Test
    public void testCreateNumberDecimalSmall() {
        Number n = NumberUtils.createNumber("0.1");
        assertTrue(n instanceof Float);
    }

    @Test
    public void testCreateNumberDecimalLarge() {
        // A number that can't fit in Float -> Double
        Number n = NumberUtils.createNumber("1E39");
        assertTrue(n instanceof Double);
    }

    @Test
    public void testCreateNumberDecimalTooLargeForDouble() {
        Number n = NumberUtils.createNumber("1E309");
        assertTrue(n instanceof java.math.BigDecimal);
    }

    // scientific notation with E, positive exponent
    @Test
    public void testCreateNumberScientific() {
        Number n = NumberUtils.createNumber("1.2E3");
        assertTrue(n instanceof Float);
        assertEquals(1200.0f, n.floatValue(), 0);
    }

    // negative exponent
    @Test
    public void testCreateNumberNegativeExponent() {
        Number n = NumberUtils.createNumber("1E-3");
        assertTrue(n instanceof Float || n instanceof Double);
    }

    // test number with both decimal and exponent
    @Test
    public void testCreateNumberDecimalAndExponent() {
        Number n = NumberUtils.createNumber("12.34E10");
        assertTrue(n instanceof Double || n instanceof Float);
    }

    // tests for isAllZeros behavior: zero float but string contains non-zero digits -> fallback
    @Test
    public void testCreateNumberZeroFloatButNonZeroDigitsFQualifier() {
        // For example "0.0f" would return Float 0.0, allZeros true -> return Float. 
        // "0.1f" is fine.
        // But "0.0E1f"? The mantissa "0.0", exponent "1", allZeros true? Actually mantissa "0.0" not all zeros (has decimal), but isAllZeros checks each char; it would be false because '.' not zero. Wait, isAllZeros returns false if any char != '0', so ".1" or "0.0" would return false because '.'? The code is: if str == null true, else for i from length-1 to 0: if char != '0' return false; return str.length()>0. So for "0.0", it would check last char '0', ok, then '.' -> not '0' -> return false. So allZeros false, so the condition for Float zero with non-all-zeros is: f.floatValue() == 0.0F && !allZeros. If we have "0.0f", float value 0.0, allZeros false, so it would fall through to Double. But the algorithm checks: if (!(f.isInfinite() || (f.floatValue() == 0.0F && !allZeros))) return f; So if float value is 0.0 and allZeros false, the condition (f.floatValue() == 0.0F && !allZeros) is true, so the whole isInf... || true is true, so invert gives false, so it does NOT return f, falls through. So "0.0f" will try Double "0.0" (which is also 0.0) and allZeros with same string? "0.0" will have same allZeros false, so it will also fall through to BigDecimal. So "0.0f" returns BigDecimal, not Float. That's fine. We'll test that.
        Number n = NumberUtils.createNumber("0.0f");
        assertTrue(n instanceof java.math.BigDecimal);
    }

    // test integer no qualifier -> Integer, Long, BigInteger sequence
    @Test
    public void testCreateNumberIntegerSequence() {
        assertEquals(Integer.valueOf(123), NumberUtils.createNumber("123"));
        assertEquals(Long.valueOf(123L), NumberUtils.createNumber(String.valueOf(Integer.MAX_VALUE + 1L)));
        assertEquals(new java.math.BigInteger("12345678901234567890"), NumberUtils.createNumber("12345678901234567890"));
    }

    // test with leading zeros (octal) no qualifier -> Integer.decode handles octal
    @Test
    public void testCreateNumberOctal() {
        assertEquals(Integer.valueOf(8), NumberUtils.createNumber("010")); // octal 10 = 8
        assertEquals(Integer.valueOf(-8), NumberUtils.createNumber("-010"));
    }

    // test createNumber with both 'e' and 'E' present
    @Test(expected = NumberFormatException.class)
    public void testCreateNumberDoubleExponent() {
        // "1eE2" will cause expPos to be weird and lead to NFE
        NumberUtils.createNumber("1eE2");
    }

    // test exponent position before decimal point -> NFE
    @Test(expected = NumberFormatException.class)
    public void testCreateNumberExponentBeforeDecimal() {
        // "1E2.0" ? That's exp before decimal? Actually expPos=1, decPos=3, so decPos> -1, expPos < decPos -> NFE
        NumberUtils.createNumber("1E2.0");
    }

    // test exponent position beyond length
    @Test(expected = NumberFormatException.class)
    public void testCreateNumberExponentOutOfBounds() {
        // e.g., "1E" alone? Actually last char is 'E', it goes to type qualifier branch? Let's just use "1E2E3" maybe?
        // but the code: if (decPos > -1) { if expPos > -1) { if expPos < decPos || expPos > str.length()) throw... } else dec = ...}
        // If there's a decimal, we can make expPos > str.length() by having string like "1.2E"? That's length 4, indexOf('e')=3? Actually "1.2E", indexOf('e')=3, indexOf('E')=-1, sum=2? No, expPos = 3 + (-1) + 1 = 3. That's within. To get expPos > str.length(), we need a string like "1E2" where E at 1, but no other e, so expPos=1+(-1)+1=1. So that's not >. This tricky. Probably the check expPos > str.length() is only useful when both e and E are present, causing large expPos. So we can test "1.2eE" (length 5, e at 3, E at 4? Actually "1.2eE": indexOf('e')=3, indexOf('E')=4, sum=7, expPos=8 > length -> throws. So we'll test "1.2eE". That should be NFE.
        NumberUtils.createNumber("1.2eE");
    }

    // test negative case for createNumber with '-' but no digits after
    @Test(expected = NumberFormatException.class)
    public void testCreateNumberJustMinus() {
        NumberUtils.createNumber("-");
    }

    // createFloat, createDouble, createInteger, createLong, createBigInteger, createBigDecimal
    @Test
    public void testCreateFloat() {
        assertNull(NumberUtils.createFloat(null));
        assertEquals(Float.valueOf(1.0f), NumberUtils.createFloat("1.0"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateFloatInvalid() {
        NumberUtils.createFloat("abc");
    }

    @Test
    public void testCreateDouble() {
        assertNull(NumberUtils.createDouble(null));
        assertEquals(Double.valueOf(1.0), NumberUtils.createDouble("1.0"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateDoubleInvalid() {
        NumberUtils.createDouble("abc");
    }

    @Test
    public void testCreateInteger() {
        assertNull(NumberUtils.createInteger(null));
        assertEquals(Integer.valueOf(123), NumberUtils.createInteger("123"));
        assertEquals(Integer.valueOf(0xAABD), NumberUtils.createInteger("0xAABD"));
        assertEquals(Integer.valueOf(0777), NumberUtils.createInteger("0777"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateIntegerInvalid() {
        NumberUtils.createInteger("abc");
    }

    @Test
    public void testCreateLong() {
        assertNull(NumberUtils.createLong(null));
        assertEquals(Long.valueOf(123L), NumberUtils.createLong("123"));
        assertEquals(Long.valueOf(0xAABDL), NumberUtils.createLong("0xAABD"));
        assertEquals(Long.valueOf(0777L), NumberUtils.createLong("0777"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateLongInvalid() {
        NumberUtils.createLong("abc");
    }

    @Test
    public void testCreateBigInteger() {
        assertNull(NumberUtils.createBigInteger(null));
        assertEquals(new java.math.BigInteger("123"), NumberUtils.createBigInteger("123"));
        // hex
        assertEquals(new java.math.BigInteger("dead", 16), NumberUtils.createBigInteger("0xdead"));
        assertEquals(new java.math.BigInteger("dead", 16), NumberUtils.createBigInteger("#dead"));
        // negative hex
        assertEquals(new java.math.BigInteger("-dead", 16), NumberUtils.createBigInteger("-0xdead"));
        // octal
        assertEquals(new java.math.BigInteger("10", 8), NumberUtils.createBigInteger("010")); // octal 10 = 8
        assertEquals(new java.math.BigInteger("-10", 8), NumberUtils.createBigInteger("-010"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigIntegerEmpty() {
        NumberUtils.createBigInteger("");
    }

    @Test
    public void testCreateBigDecimal() {
        assertNull(NumberUtils.createBigDecimal(null));
        assertEquals(new java.math.BigDecimal("1.5"), NumberUtils.createBigDecimal("1.5"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigDecimalBlank() {
        NumberUtils.createBigDecimal("");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigDecimalDoubleDash() {
        NumberUtils.createBigDecimal("--1.5");
    }

    // min/max arrays
    // null and empty
    @Test(expected = IllegalArgumentException.class)
    public void testMinLongNull() { NumberUtils.min((long[]) null); }
    @Test(expected = IllegalArgumentException.class)
    public void testMinLongEmpty() { NumberUtils.min(new long[]{}); }
    @Test(expected = IllegalArgumentException.class)
    public void testMinIntNull() { NumberUtils.min((int[]) null); }
    @Test(expected = IllegalArgumentException.class)
    public void testMinIntEmpty() { NumberUtils.min(new int[]{}); }
    @Test(expected = IllegalArgumentException.class)
    public void testMinShortNull() { NumberUtils.min((short[]) null); }
    @Test(expected = IllegalArgumentException.class)
    public void testMinShortEmpty() { NumberUtils.min(new short[]{}); }
    @Test(expected = IllegalArgumentException.class)
    public void testMinByteNull() { NumberUtils.min((byte[]) null); }
    @Test(expected = IllegalArgumentException.class)
    public void testMinByteEmpty() { NumberUtils.min(new byte[]{}); }
    @Test(expected = IllegalArgumentException.class)
    public void testMinDoubleNull() { NumberUtils.min((double[]) null); }
    @Test(expected = IllegalArgumentException.class)
    public void testMinDoubleEmpty() { NumberUtils.min(new double[]{}); }
    @Test(expected = IllegalArgumentException.class)
    public void testMinFloatNull() { NumberUtils.min((float[]) null); }
    @Test(expected = IllegalArgumentException.class)
    public void testMinFloatEmpty() { NumberUtils.min(new float[]{}); }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxLongNull() { NumberUtils.max((long[]) null); }
    @Test(expected = IllegalArgumentException.class)
    public void testMaxLongEmpty() { NumberUtils.max(new long[]{}); }
    @Test(expected = IllegalArgumentException.class)
    public void testMaxIntNull() { NumberUtils.max((int[]) null); }
    @Test(expected = IllegalArgumentException.class)
    public void testMaxIntEmpty() { NumberUtils.max(new int[]{}); }
    @Test(expected = IllegalArgumentException.class)
    public void testMaxShortNull() { NumberUtils.max((short[]) null); }
    @Test(expected = IllegalArgumentException.class)
    public void testMaxShortEmpty() { NumberUtils.max(new short[]{}); }
    @Test(expected = IllegalArgumentException.class)
    public void testMaxByteNull() { NumberUtils.max((byte[]) null); }
    @Test(expected = IllegalArgumentException.class)
    public void testMaxByteEmpty() { NumberUtils.max(new byte[]{}); }
    @Test(expected = IllegalArgumentException.class)
    public void testMaxDoubleNull() { NumberUtils.max((double[]) null); }
    @Test(expected = IllegalArgumentException.class)
    public void testMaxDoubleEmpty() { NumberUtils.max(new double[]{}); }
    @Test(expected = IllegalArgumentException.class)
    public void testMaxFloatNull() { NumberUtils.max((float[]) null); }
    @Test(expected = IllegalArgumentException.class)
    public void testMaxFloatEmpty() { NumberUtils.max(new float[]{}); }

    // min array tests
    @Test
    public void testMinLongArray() {
        assertEquals(1L, NumberUtils.min(new long[]{5L, 1L, 10L}));
        assertEquals(1L, NumberUtils.min(new long[]{1L}));
    }
    @Test
    public void testMinIntArray() {
        assertEquals(1, NumberUtils.min(new int[]{5, 1, 10}));
    }
    @Test
    public void testMinShortArray() {
        assertEquals((short)1, NumberUtils.min(new short[]{5, 1, 10}));
    }
    @Test
    public void testMinByteArray() {
        assertEquals((byte)1, NumberUtils.min(new byte[]{5, 1, 10}));
    }
    @Test
    public void testMinDoubleArray() {
        assertEquals(1.0, NumberUtils.min(new double[]{5.0, 1.0, 10.0}), 0);
    }
    @Test
    public void testMinDoubleArrayNaN() {
        // NaN should be returned as per method behavior
        assertTrue(Double.isNaN(NumberUtils.min(new double[]{5.0, Double.NaN, 1.0})));
    }
    @Test
    public void testMinFloatArray() {
        assertEquals(1.0f, NumberUtils.min(new float[]{5.0f, 1.0f, 10.0f}), 0);
    }
    @Test
    public void testMinFloatArrayNaN() {
        assertTrue(Float.isNaN(NumberUtils.min(new float[]{5.0f, Float.NaN, 1.0f})));
    }

    // max array tests
    @Test
    public void testMaxLongArray() {
        assertEquals(10L, NumberUtils.max(new long[]{5L, 1L, 10L}));
    }
    @Test
    public void testMaxIntArray() {
        assertEquals(10, NumberUtils.max(new int[]{5, 1, 10}));
    }
    @Test
    public void testMaxShortArray() {
        assertEquals((short)10, NumberUtils.max(new short[]{5, 1, 10}));
    }
    @Test
    public void testMaxByteArray() {
        assertEquals((byte)10, NumberUtils.max(new byte[]{5, 1, 10}));
    }
    @Test
    public void testMaxDoubleArray() {
        assertEquals(10.0, NumberUtils.max(new double[]{5.0, 1.0, 10.0}), 0);
    }
    @Test
    public void testMaxDoubleArrayNaN() {
        assertTrue(Double.isNaN(NumberUtils.max(new double[]{5.0, Double.NaN, 1.0})));
    }
    @Test
    public void testMaxFloatArray() {
        assertEquals(10.0f, NumberUtils.max(new float[]{5.0f, 1.0f, 10.0f}), 0);
    }
    @Test
    public void testMaxFloatArrayNaN() {
        assertTrue(Float.isNaN(NumberUtils.max(new float[]{5.0f, Float.NaN, 1.0f})));
    }

    // 3-param min/max
    @Test
    public void testMinLong() {
        assertEquals(1L, NumberUtils.min(5L, 1L, 10L));
        assertEquals(1L, NumberUtils.min(1L, 5L, 10L));
        assertEquals(1L, NumberUtils.min(10L, 5L, 1L));
    }
    @Test
    public void testMinInt() {
        assertEquals(1, NumberUtils.min(5, 1, 10));
    }
    @Test
    public void testMinShort() {
        assertEquals((short)1, NumberUtils.min((short)5, (short)1, (short)10));
    }
    @Test
    public void testMinByte() {
        assertEquals((byte)1, NumberUtils.min((byte)5, (byte)1, (byte)10));
    }
    @Test
    public void testMinDouble() {
        assertEquals(1.0, NumberUtils.min(5.0, 1.0, 10.0), 0);
        assertTrue(Double.isNaN(NumberUtils.min(Double.NaN, 1.0, 2.0)));
    }
    @Test
    public void testMinFloat() {
        assertEquals(1.0f, NumberUtils.min(5.0f, 1.0f, 10.0f), 0);
        assertTrue(Float.isNaN(NumberUtils.min(Float.NaN, 1.0f, 2.0f)));
    }

    @Test
    public void testMaxLong() {
        assertEquals(10L, NumberUtils.max(5L, 1L, 10L));
    }
    @Test
    public void testMaxInt() {
        assertEquals(10, NumberUtils.max(5, 1, 10));
    }
    @Test
    public void testMaxShort() {
        assertEquals((short)10, NumberUtils.max((short)5, (short)1, (short)10));
    }
    @Test
    public void testMaxByte() {
        assertEquals((byte)10, NumberUtils.max((byte)5, (byte)1, (byte)10));
    }
    @Test
    public void testMaxDouble() {
        assertEquals(10.0, NumberUtils.max(5.0, 1.0, 10.0), 0);
        assertTrue(Double.isNaN(NumberUtils.max(Double.NaN, 1.0, 2.0)));
    }
    @Test
    public void testMaxFloat() {
        assertEquals(10.0f, NumberUtils.max(5.0f, 1.0f, 10.0f), 0);
        assertTrue(Float.isNaN(NumberUtils.max(Float.NaN, 1.0f, 2.0f)));
    }

    // isDigits
    @Test
    public void testIsDigits() {
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits(""));
        assertTrue(NumberUtils.isDigits("123"));
        assertFalse(NumberUtils.isDigits("12.3"));
        assertFalse(NumberUtils.isDigits("-123"));
    }

    // isNumber
    @Test
    public void testIsNumber() {
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));
        assertTrue(NumberUtils.isNumber("123"));
        assertTrue(NumberUtils.isNumber("-123"));
        assertTrue(NumberUtils.isNumber("0x1A"));
        assertFalse(NumberUtils.isNumber("0x")); // trailing empty after 0x
        assertTrue(NumberUtils.isNumber("1.23"));
        assertTrue(NumberUtils.isNumber("1.23E10"));
        assertTrue(NumberUtils.isNumber("1.23e-10"));
        assertTrue(NumberUtils.isNumber("1.23E+10"));
        assertTrue(NumberUtils.isNumber("1.23d"));
        assertTrue(NumberUtils.isNumber("1.23f"));
        assertTrue(NumberUtils.isNumber("123L"));
        assertTrue(NumberUtils.isNumber("123l"));
        // invalid cases
        assertFalse(NumberUtils.isNumber("."));
        assertFalse(NumberUtils.isNumber("1E"));
        assertFalse(NumberUtils.isNumber("1E-"));
        assertFalse(NumberUtils.isNumber("1.2.3"));
        assertFalse(NumberUtils.isNumber("1E2E3")); // two E
        assertFalse(NumberUtils.isNumber("0x1G")); // invalid hex
        assertFalse(NumberUtils.isNumber("12L2")); // digits after qualifier? The algorithm loops to last char, so "12L2" would find '2' as last char -> true? Actually it checks last char only for type qualifiers, earlier digits are fine. But is "12L2" a valid number? No, because type qualifier must be last. The loop ends before last char. For "12L2", length=4, last char '2', i < sz (sz=3 after decrement). The loop runs i=0 to i<3, i.e., i=0,1,2. chars = ['1','2','L','2']; i=0: '1' digit ok; i=1: '2' digit ok; i=2: 'L' -> in the elseif for 'L'/'l'? Actually the code for isNumber does not check L/l inside loop; it only checks for digits, decimal, e/E, signs, and else returns false. So 'L' will go to else and return false. So isNumber returns false for "12L2". We'll test that.
        assertFalse(NumberUtils.isNumber("12L2"));
        assertFalse(NumberUtils.isNumber("1.23L")); // L with decimal -> false
        assertFalse(NumberUtils.isNumber("1E10L")); // L with exponent -> false
        // test trailing decimal point
        assertTrue(NumberUtils.isNumber("1."));
        assertFalse(NumberUtils.isNumber("1..2"));
        // test leading sign
        assertTrue(NumberUtils.isNumber("-0.1"));
        // test hex with sign and valid
        assertTrue(NumberUtils.isNumber("-0x1A"));
        assertFalse(NumberUtils.isNumber("-0x")); // failing after 0x
    }

    // Additional edge case for isNumber: number with valid hex and no extra digits
    @Test
    public void testIsNumberHexValid() {
        assertTrue(NumberUtils.isNumber("0x1A"));
        assertTrue(NumberUtils.isNumber("-0x1A"));
    }

    // test isNumber with 'd'/'D'/'f'/'F' at end
    @Test
    public void testIsNumberWithQualifiers() {
        assertTrue(NumberUtils.isNumber("1.0d"));
        assertTrue(NumberUtils.isNumber("1.0D"));
        assertTrue(NumberUtils.isNumber("1.0f"));
        assertTrue(NumberUtils.isNumber("1.0F"));
    }

    // test isNumber for "0" octal type? isNumber doesn't special-case octal; "0" is digits, true.
    // isNumber for scientific notation with sign immediately after 'e'
    @Test
    public void testIsNumberScientificSign() {
        assertTrue(NumberUtils.isNumber("1e+2"));
        assertTrue(NumberUtils.isNumber("1E-2"));
        assertFalse(NumberUtils.isNumber("1e-"));
        assertFalse(NumberUtils.isNumber("1e+"));
    }
}
