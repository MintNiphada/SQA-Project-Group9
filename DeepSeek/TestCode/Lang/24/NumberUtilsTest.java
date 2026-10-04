package org.apache.commons.lang3.math;

import org.junit.Test;
import static org.junit.Assert.*;
import java.math.BigDecimal;
import java.math.BigInteger;

public class NumberUtilsTest {

    @Test
    public void testToIntNull() {
        assertEquals(0, NumberUtils.toInt(null));
    }

    @Test
    public void testToIntEmpty() {
        assertEquals(0, NumberUtils.toInt(""));
    }

    @Test
    public void testToIntValid() {
        assertEquals(1, NumberUtils.toInt("1"));
    }

    @Test
    public void testToIntInvalid() {
        assertEquals(0, NumberUtils.toInt("abc"));
    }

    @Test
    public void testToIntWithDefaultNull() {
        assertEquals(5, NumberUtils.toInt(null, 5));
    }

    @Test
    public void testToIntWithDefaultEmpty() {
        assertEquals(5, NumberUtils.toInt("", 5));
    }

    @Test
    public void testToIntWithDefaultValid() {
        assertEquals(10, NumberUtils.toInt("10", 5));
    }

    @Test
    public void testToIntWithDefaultInvalid() {
        assertEquals(5, NumberUtils.toInt("abc", 5));
    }

    @Test
    public void testToLongNull() {
        assertEquals(0L, NumberUtils.toLong(null));
    }

    @Test
    public void testToLongEmpty() {
        assertEquals(0L, NumberUtils.toLong(""));
    }

    @Test
    public void testToLongValid() {
        assertEquals(123L, NumberUtils.toLong("123"));
    }

    @Test
    public void testToLongInvalid() {
        assertEquals(0L, NumberUtils.toLong("abc"));
    }

    @Test
    public void testToLongWithDefaultNull() {
        assertEquals(99L, NumberUtils.toLong(null, 99L));
    }

    @Test
    public void testToLongWithDefaultEmpty() {
        assertEquals(99L, NumberUtils.toLong("", 99L));
    }

    @Test
    public void testToLongWithDefaultValid() {
        assertEquals(456L, NumberUtils.toLong("456", 99L));
    }

    @Test
    public void testToLongWithDefaultInvalid() {
        assertEquals(99L, NumberUtils.toLong("abc", 99L));
    }

    @Test
    public void testToFloatNull() {
        assertEquals(0.0f, NumberUtils.toFloat(null), 0.0f);
    }

    @Test
    public void testToFloatEmpty() {
        assertEquals(0.0f, NumberUtils.toFloat(""), 0.0f);
    }

    @Test
    public void testToFloatValid() {
        assertEquals(1.5f, NumberUtils.toFloat("1.5"), 0.0f);
    }

    @Test
    public void testToFloatInvalid() {
        assertEquals(0.0f, NumberUtils.toFloat("abc"), 0.0f);
    }

    @Test
    public void testToFloatWithDefaultNull() {
        assertEquals(1.1f, NumberUtils.toFloat(null, 1.1f), 0.0f);
    }

    @Test
    public void testToFloatWithDefaultEmpty() {
        assertEquals(1.1f, NumberUtils.toFloat("", 1.1f), 0.0f);
    }

    @Test
    public void testToFloatWithDefaultValid() {
        assertEquals(1.5f, NumberUtils.toFloat("1.5", 0.0f), 0.0f);
    }

    @Test
    public void testToFloatWithDefaultInvalid() {
        assertEquals(1.1f, NumberUtils.toFloat("abc", 1.1f), 0.0f);
    }

    @Test
    public void testToDoubleNull() {
        assertEquals(0.0d, NumberUtils.toDouble(null), 0.0d);
    }

    @Test
    public void testToDoubleEmpty() {
        assertEquals(0.0d, NumberUtils.toDouble(""), 0.0d);
    }

    @Test
    public void testToDoubleValid() {
        assertEquals(1.5d, NumberUtils.toDouble("1.5"), 0.0d);
    }

    @Test
    public void testToDoubleInvalid() {
        assertEquals(0.0d, NumberUtils.toDouble("abc"), 0.0d);
    }

    @Test
    public void testToDoubleWithDefaultNull() {
        assertEquals(1.1d, NumberUtils.toDouble(null, 1.1d), 0.0d);
    }

    @Test
    public void testToDoubleWithDefaultEmpty() {
        assertEquals(1.1d, NumberUtils.toDouble("", 1.1d), 0.0d);
    }

    @Test
    public void testToDoubleWithDefaultValid() {
        assertEquals(1.5d, NumberUtils.toDouble("1.5", 0.0d), 0.0d);
    }

    @Test
    public void testToDoubleWithDefaultInvalid() {
        assertEquals(1.1d, NumberUtils.toDouble("abc", 1.1d), 0.0d);
    }

    @Test
    public void testToByteNull() {
        assertEquals(0, NumberUtils.toByte(null));
    }

    @Test
    public void testToByteEmpty() {
        assertEquals(0, NumberUtils.toByte(""));
    }

    @Test
    public void testToByteValid() {
        assertEquals(1, NumberUtils.toByte("1"));
    }

    @Test
    public void testToByteInvalid() {
        assertEquals(0, NumberUtils.toByte("abc"));
    }

    @Test
    public void testToByteWithDefaultNull() {
        assertEquals((byte)5, NumberUtils.toByte(null, (byte)5));
    }

    @Test
    public void testToByteWithDefaultEmpty() {
        assertEquals((byte)5, NumberUtils.toByte("", (byte)5));
    }

    @Test
    public void testToByteWithDefaultValid() {
        assertEquals((byte)10, NumberUtils.toByte("10", (byte)5));
    }

    @Test
    public void testToByteWithDefaultInvalid() {
        assertEquals((byte)5, NumberUtils.toByte("abc", (byte)5));
    }

    @Test
    public void testToShortNull() {
        assertEquals(0, NumberUtils.toShort(null));
    }

    @Test
    public void testToShortEmpty() {
        assertEquals(0, NumberUtils.toShort(""));
    }

    @Test
    public void testToShortValid() {
        assertEquals(1, NumberUtils.toShort("1"));
    }

    @Test
    public void testToShortInvalid() {
        assertEquals(0, NumberUtils.toShort("abc"));
    }

    @Test
    public void testToShortWithDefaultNull() {
        assertEquals((short)5, NumberUtils.toShort(null, (short)5));
    }

    @Test
    public void testToShortWithDefaultEmpty() {
        assertEquals((short)5, NumberUtils.toShort("", (short)5));
    }

    @Test
    public void testToShortWithDefaultValid() {
        assertEquals((short)10, NumberUtils.toShort("10", (short)5));
    }

    @Test
    public void testToShortWithDefaultInvalid() {
        assertEquals((short)5, NumberUtils.toShort("abc", (short)5));
    }

    @Test
    public void testCreateFloatNull() {
        assertNull(NumberUtils.createFloat(null));
    }

    @Test
    public void testCreateFloatValid() {
        assertEquals(Float.valueOf(1.5f), NumberUtils.createFloat("1.5"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateFloatInvalid() {
        NumberUtils.crateFloat("abc");
    }

    @Test
    public void testCreateDoubleNull() {
        assertNull(NumberUtils.createDouble(null));
    }

    @Test
    public void testCreateDoubleValid() {
        assertEquals(Double.valueOf(1.5d), NumberUtils.createDouble("1.5"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateDoubleInvalid() {
        NumberUtils.crateDouble("abc");
    }

    @Test
    public void testCreateIntegerNull() {
        assertNull(NumberUtils.createInteger(null));
    }

    @Test
    public void testCreateIntegerValid() {
        assertEquals(Integer.valueOf(10), NumberUtils.createInteger("10"));
    }

    @Test
    public void testCreateIntegerHex() {
        assertEquals(Integer.valueOf(10), NumberUtils.createInteger("0xA"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateIntegerInvalid() {
        NumberUtils.crateInteger("abc");
    }

    @Test
    public void testCreateLongNull() {
        assertNull(NumberUtils.createLong(null));
    }

    @Test
    public void testCreateLongValid() {
        assertEquals(Long.valueOf(123L), NumberUtils.createLong("123"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateLongInvalid() {
        NumberUtils.crateLong("abc");
    }

    @Test
    public void testCreateBigIntegerNull() {
        assertNull(NumberUtils.createBigInteger(null));
    }

    @Test
    public void testCreateBigIntegerValid() {
        assertEquals(new BigInteger("123"), NumberUtils.createBigInteger("123"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigIntegerInvalid() {
        NumberUtils.crateBigInteger("abc");
    }

    @Test
    public void testCreateBigDecimalNull() {
        assertNull(NumberUtils.createBigDecimal(null));
    }

    @Test
    public void testCreateBigDecimalValid() {
        assertEquals(new BigDecimal("1.5"), NumberUtils.createBigDecimal("1.5"));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigDecimalBlank() {
        NumberUtils.crateBigDecimal(" ");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateBigDecimalEmpty() {
        NumberUtils.crateBigDecimal("");
    }

    @Test
    public void testCreateNumberNull() {
        assertNull(NumberUtils.createNumber(null));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberBlank() {
        NumberUtils.crateNumber(" ");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberEmpty() {
        NumberUtils.crateNumber("");
    }

    @Test
    public void testCreateNumberWithDoubleDash() {
        assertNull(NumberUtils.createNumber("--"));
    }

    @Test
    public void testCreateNumberIntegerDec() {
        assertEquals(Integer.valueOf(10), NumberUtils.createNumber("10"));
    }

    @Test
    public void testCreateNumberHex() {
        assertEquals(Integer.valueOf(10), NumberUtils.createNumber("0xA"));
    }

    @Test
    public void testCreateNumberNegativeHex() {
        assertEquals(Integer.valueOf(-10), NumberUtils.createNumber("-0xA"));
    }

    @Test
    public void testCreateNumberLong() {
        assertEquals(Long.valueOf(1234567890123L), NumberUtils.createNumber("1234567890123"));
    }

    @Test
    public void testCreateNumberBigInteger() {
        assertEquals(new BigInteger("12345678901234567890"), NumberUtils.createNumber("12345678901234567890"));
    }

    @Test
    public void testCreateNumberFloat() {
        assertEquals(Float.valueOf(1.5f), NumberUtils.createNumber("1.5"));
    }

    @Test
    public void testCreateNumberScientificDouble() {
        assertEquals(Double.valueOf(1.5E2), NumberUtils.createNumber("1.5E2"));
    }

    @Test
    public void testCreateNumberScientificFloat() {
        assertEquals(Float.valueOf(1.5E2f), NumberUtils.createNumber("1.5E2f"));
    }
