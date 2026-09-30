package org.apache.commons.compress.archivers.tar;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;

public class TarUtilsTest {

    @Test
    public void testConstructorIsPrivate() throws Exception {
        Constructor<TarUtils> constructor = TarUtils.class.getDeclaredConstructor();
        Assert.assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        TarUtils instance = constructor.newInstance();
        Assert.assertNotNull(instance);
    }

    @Test
    public void testParseOctalAllZerosOrNulls() {
        byte[] buffer = new byte[10];
        long value = TarUtils.parseOctal(buffer, 0, buffer.length);
        Assert.assertEquals(0L, value);

        byte[] zeros = "0000000000".getBytes(StandardCharsets.US_ASCII);
        Assert.assertEquals(0L, TarUtils.parseOctal(zeros, 0, zeros.length));
    }

    @Test
    public void testParseOctalLeadingSpacesAndZeros() {
        byte[] buffer = "   000755\0".getBytes(StandardCharsets.US_ASCII);
        long value = TarUtils.parseOctal(buffer, 0, buffer.length);
        Assert.assertEquals(0755L, value);

        buffer = "   755 ".getBytes(StandardCharsets.US_ASCII);
        value = TarUtils.parseOctal(buffer, 0, buffer.length);
        Assert.assertEquals(0755L, value);

        buffer = "00755 ".getBytes(StandardCharsets.US_ASCII);
        value = TarUtils.parseOctal(buffer, 0, buffer.length);
        Assert.assertEquals(0755L, value);
    }

    @Test
    public void testParseOctalWithOffset() {
        byte[] buffer = "XXXX 0755\0YYYY".getBytes(StandardCharsets.US_ASCII);
        long value = TarUtils.parseOctal(buffer, 4, 6);
        Assert.assertEquals(0755L, value);
    }

    @Test
    public void testParseOctalTrailingSpace() {
        byte[] buffer = " 123 456 ".getBytes(StandardCharsets.US_ASCII);
        // Once non-zero/space is read, next space stops parsing
        long value = TarUtils.parseOctal(buffer, 0, buffer.length);
        Assert.assertEquals(0123L, value);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalInvalidByteHigh() {
        byte[] buffer = " 0789 ".getBytes(StandardCharsets.US_ASCII);
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalInvalidByteLow() {
        byte[] buffer = " 07/0 ".getBytes(StandardCharsets.US_ASCII);
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    @Test
    public void testParseOctalExceptionMessageContent() {
        byte[] buffer = new byte[]{' ', '1', '2', '8', 0};
        try {
            TarUtils.parseOctal(buffer, 0, buffer.length);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue(ex.getMessage().contains("Invalid byte 56"));
            Assert.assertTrue(ex.getMessage().contains("{NUL}"));
        }
    }

    @Test
    public void testParseName() {
        byte[] buffer = "hello\0world".getBytes(StandardCharsets.US_ASCII);
        String name = TarUtils.parseName(buffer, 0, buffer.length);
        Assert.assertEquals("hello", name);

        // Entire buffer consumed without NUL
        byte[] noNull = "abcdef".getBytes(StandardCharsets.US_ASCII);
        Assert.assertEquals("abcdef", TarUtils.parseName(noNull, 0, noNull.length));

        // Offset test
        Assert.assertEquals("world", TarUtils.parseName(buffer, 6, 5));

        // Empty buffer segment
        byte[] allNull = new byte[5];
        Assert.assertEquals("", TarUtils.parseName(allNull, 0, allNull.length));

        // Unsigned byte handling (> 127)
        byte[] highBytes = new byte[]{(byte) 0xE4, (byte) 0xF6, 0};
        String parsed = TarUtils.parseName(highBytes, 0, highBytes.length);
        Assert.assertEquals(2, parsed.length());
        Assert.assertEquals((char) 0xE4, parsed.charAt(0));
        Assert.assertEquals((char) 0xF6, parsed.charAt(1));
    }

    @Test
    public void testFormatNameBytes() {
        byte[] buffer = new byte[10];
        int nextOffset = TarUtils.formatNameBytes("test", buffer, 0, buffer.length);
        Assert.assertEquals(10, nextOffset);

        Assert.assertEquals('t', buffer[0]);
        Assert.assertEquals('e', buffer[1]);
        Assert.assertEquals('s', buffer[2]);
        Assert.assertEquals('t', buffer[3]);
        for (int i = 4; i < 10; i++) {
            Assert.assertEquals(0, buffer[i]);
        }

        // Test truncation
        byte[] shortBuf = new byte[4];
        nextOffset = TarUtils.formatNameBytes("testing", shortBuf, 0, shortBuf.length);
        Assert.assertEquals(4, nextOffset);
        Assert.assertEquals("test", new String(shortBuf, StandardCharsets.US_ASCII));

        // Test with offset
        byte[] offsetBuf = new byte[10];
        nextOffset = TarUtils.formatNameBytes("abc", offsetBuf, 2, 5);
        Assert.assertEquals(7, nextOffset);
        Assert.assertEquals(0, offsetBuf[0]);
        Assert.assertEquals(0, offsetBuf[1]);
        Assert.assertEquals('a', offsetBuf[2]);
        Assert.assertEquals('b', offsetBuf[3]);
        Assert.assertEquals('c', offsetBuf[4]);
        Assert.assertEquals(0, offsetBuf[5]);
        Assert.assertEquals(0, offsetBuf[6]);
        Assert.assertEquals(0, offsetBuf[7]);
    }

    @Test
    public void testFormatUnsignedOctalString() {
        byte[] buffer = new byte[6];
        TarUtils.formatUnsignedOctalString(0L, buffer, 0, buffer.length);
        Assert.assertEquals("000000", new String(buffer, StandardCharsets.US_ASCII));

        TarUtils.formatUnsignedOctalString(0755L, buffer, 0, buffer.length);
        Assert.assertEquals("000755", new String(buffer, StandardCharsets.US_ASCII));

        TarUtils.formatUnsignedOctalString(0777777L, buffer, 0, buffer.length);
        Assert.assertEquals("777777", new String(buffer, StandardCharsets.US_ASCII));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatUnsignedOctalStringOverflow() {
        byte[] buffer = new byte[3];
        // 01000 in octal requires 4 digits, will not fit in 3
        TarUtils.formatUnsignedOctalString(01000L, buffer, 0, buffer.length);
    }

    @Test
    public void testFormatOctalBytes() {
        byte[] buffer = new byte[8];
        int result = TarUtils.formatOctalBytes(0755L, buffer, 0, buffer.length);
        Assert.assertEquals(8, result);
        Assert.assertEquals("0000755 \0", new String(buffer, StandardCharsets.US_ASCII));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatOctalBytesOverflow() {
        byte[] buffer = new byte[4];
        // 4 bytes: 2 octal digits + space + null; 0755 needs 3 digits -> overflow
        TarUtils.formatOctalBytes(0755L, buffer, 0, buffer.length);
    }

    @Test
    public void testFormatLongOctalBytes() {
        byte[] buffer = new byte[8];
        int result = TarUtils.formatLongOctalBytes(0755L, buffer, 0, buffer.length);
        Assert.assertEquals(8, result);
        Assert.assertEquals("0000755 ", new String(buffer, StandardCharsets.US_ASCII));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongOctalBytesOverflow() {
        byte[] buffer = new byte[3];
        // 3 bytes: 2 octal digits + space; 0755 needs 3 digits -> overflow
        TarUtils.formatLongOctalBytes(0755L, buffer, 0, buffer.length);
    }

    @Test
    public void testFormatCheckSumOctalBytes() {
        byte[] buffer = new byte[8];
        int result = TarUtils.formatCheckSumOctalBytes(0755L, buffer, 0, buffer.length);
        Assert.assertEquals(8, result);
        Assert.assertEquals("0000755\0 ", new String(buffer, StandardCharsets.US_ASCII));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatCheckSumOctalBytesOverflow() {
        byte[] buffer = new byte[4];
        // 4 bytes: 2 octal digits + null + space; 0755 needs 3 digits -> overflow
        TarUtils.formatCheckSumOctalBytes(0755L, buffer, 0, buffer.length);
    }

    @Test
    public void testComputeCheckSum() {
        byte[] empty = new byte[0];
        Assert.assertEquals(0L, TarUtils.computeCheckSum(empty));

        byte[] allZeros = new byte[512];
        Assert.assertEquals(0L, TarUtils.computeCheckSum(allZeros));

        byte[] simple = new byte[]{'a', 'b', 'c'};
        Assert.assertEquals((long) ('a' + 'b' + 'c'), TarUtils.computeCheckSum(simple));

        // Negative signed byte: (byte) -1 == 0xFF == 255
        byte[] signedBytes = new byte[]{(byte) -1, (byte) -2};
        Assert.assertEquals(255L + 254L, TarUtils.computeCheckSum(signedBytes));
    }

    @Test
    public void testRoundTripParseAndFormatOctal() {
        byte[] buffer = new byte[12];
        long value = 012345670123L;
        TarUtils.formatOctalBytes(value, buffer, 0, buffer.length);
        long parsed = TarUtils.parseOctal(buffer, 0, buffer.length);
        Assert.assertEquals(value, parsed);
    }
}
