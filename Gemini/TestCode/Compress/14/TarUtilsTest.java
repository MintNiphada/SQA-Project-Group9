package org.apache.commons.compress.archivers.tar;

import org.junit.Assert;
import org.junit.Test;

public class TarUtilsTest {

    @Test
    public void testParseOctalValidValues() {
        byte[] buffer = " 0755 \0".getBytes();
        long value = TarUtils.parseOctal(buffer, 0, buffer.length);
        Assert.assertEquals(0755L, value);

        byte[] buffer2 = "00000000000 \0".getBytes();
        Assert.assertEquals(0L, TarUtils.parseOctal(buffer2, 0, buffer2.length));

        byte[] buffer3 = "1234567 \0".getBytes();
        Assert.assertEquals(01234567L, TarUtils.parseOctal(buffer3, 0, buffer3.length));

        // Buffer with two trailing spaces
        byte[] buffer4 = " 123  ".getBytes();
        Assert.assertEquals(0123L, TarUtils.parseOctal(buffer4, 0, buffer4.length));

        // Buffer with single trailing space
        byte[] buffer5 = "123 ".getBytes();
        Assert.assertEquals(0123L, TarUtils.parseOctal(buffer5, 0, buffer5.length));

        // Buffer with single trailing NUL
        byte[] buffer6 = new byte[]{'1', '2', '3', 0};
        Assert.assertEquals(0123L, TarUtils.parseOctal(buffer6, 0, buffer6.length));

        // Buffer with two trailing NULs
        byte[] buffer7 = new byte[]{'1', '2', '3', 0, 0};
        Assert.assertEquals(0123L, TarUtils.parseOctal(buffer7, 0, buffer7.length));
    }

    @Test
    public void testParseOctalAllNul() {
        byte[] buffer = new byte[8];
        long value = TarUtils.parseOctal(buffer, 0, buffer.length);
        Assert.assertEquals(0L, value);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalLengthLessThan2() {
        byte[] buffer = new byte[]{'1'};
        TarUtils.parseOctal(buffer, 0, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalMissingTrailer() {
        byte[] buffer = "123456".getBytes();
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalInvalidDigit() {
        byte[] buffer = " 0855 \0".getBytes();
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalInvalidNonDigit() {
        byte[] buffer = " 0a55 \0".getBytes();
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    @Test
    public void testParseOctalOrBinary() {
        // Octal mode when MSB is not set
        byte[] octalBuffer = " 0755 \0".getBytes();
        Assert.assertEquals(0755L, TarUtils.parseOctalOrBinary(octalBuffer, 0, octalBuffer.length));

        // Binary mode when MSB is set
        byte[] binaryBuffer = new byte[8];
        binaryBuffer[0] = (byte) 0x80;
        binaryBuffer[7] = 0x01;
        Assert.assertEquals(1L, TarUtils.parseOctalOrBinary(binaryBuffer, 0, 8));

        byte[] binaryBuffer2 = new byte[8];
        binaryBuffer2[0] = (byte) 0x80;
        binaryBuffer2[6] = 0x01;
        binaryBuffer2[7] = 0x02;
        Assert.assertEquals(258L, TarUtils.parseOctalOrBinary(binaryBuffer2, 0, 8));

        // Binary with first byte non-zero lower 7 bits
        byte[] binaryBuffer3 = new byte[2];
        binaryBuffer3[0] = (byte) 0x81;
        binaryBuffer3[1] = 0x02;
        Assert.assertEquals(0x0102L, TarUtils.parseOctalOrBinary(binaryBuffer3, 0, 2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalOrBinaryOverflow() {
        // Binary value exceeding Long.MAX_VALUE
        byte[] binaryBuffer = new byte[10];
        binaryBuffer[0] = (byte) 0xFF;
        for (int i = 1; i < 10; i++) {
            binaryBuffer[i] = (byte) 0xFF;
        }
        TarUtils.parseOctalOrBinary(binaryBuffer, 0, 10);
    }

    @Test
    public void testParseBoolean() {
        byte[] buffer = new byte[]{1, 0, 2};
        Assert.assertTrue(TarUtils.parseBoolean(buffer, 0));
        Assert.assertFalse(TarUtils.parseBoolean(buffer, 1));
        Assert.assertFalse(TarUtils.parseBoolean(buffer, 2));
    }

    @Test
    public void testParseName() {
        byte[] buffer = new byte[]{'t', 'e', 's', 't', 0, 'f', 'i', 'l', 'e'};
        String name = TarUtils.parseName(buffer, 0, buffer.length);
        Assert.assertEquals("test", name);

        String nameNoNull = TarUtils.parseName(buffer, 0, 4);
        Assert.assertEquals("test", nameNoNull);

        String nameOffset = TarUtils.parseName(buffer, 5, 4);
        Assert.assertEquals("file", nameOffset);

        // Sign extension handling: byte > 127
        byte[] bufferExtended = new byte[]{(byte) 0xE4, (byte) 0xF6, 0};
        String extendedName = TarUtils.parseName(bufferExtended, 0, 3);
        Assert.assertEquals("\u00E4\u00F6", extendedName);
    }

    @Test
    public void testFormatNameBytes() {
        byte[] buffer = new byte[10];
        int nextOffset = TarUtils.formatNameBytes("test", buffer, 0, 10);
        Assert.assertEquals(10, nextOffset);
        Assert.assertEquals('t', buffer[0]);
        Assert.assertEquals('e', buffer[1]);
        Assert.assertEquals('s', buffer[2]);
        Assert.assertEquals('t', buffer[3]);
        for (int i = 4; i < 10; i++) {
            Assert.assertEquals(0, buffer[i]);
        }

        // Truncation test
        byte[] bufferTrunc = new byte[3];
        nextOffset = TarUtils.formatNameBytes("testing", bufferTrunc, 0, 3);
        Assert.assertEquals(3, nextOffset);
        Assert.assertEquals('t', bufferTrunc[0]);
        Assert.assertEquals('e', bufferTrunc[1]);
        Assert.assertEquals('s', bufferTrunc[2]);
    }

    @Test
    public void testFormatUnsignedOctalString() {
        byte[] buffer = new byte[6];
        TarUtils.formatUnsignedOctalString(0755L, buffer, 0, 6);
        Assert.assertEquals("000755", new String(buffer));

        TarUtils.formatUnsignedOctalString(0L, buffer, 0, 6);
        Assert.assertEquals("000000", new String(buffer));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatUnsignedOctalStringOverflow() {
        byte[] buffer = new byte[2];
        TarUtils.formatUnsignedOctalString(07777L, buffer, 0, 2);
    }

    @Test
    public void testFormatOctalBytes() {
        byte[] buffer = new byte[8];
        int nextOffset = TarUtils.formatOctalBytes(0755L, buffer, 0, 8);
        Assert.assertEquals(8, nextOffset);
        Assert.assertEquals("0000755 \0", new String(buffer));
    }

    @Test
    public void testFormatLongOctalBytes() {
        byte[] buffer = new byte[8];
        int nextOffset = TarUtils.formatLongOctalBytes(0755L, buffer, 0, 8);
        Assert.assertEquals(8, nextOffset);
        Assert.assertEquals("0000755 ", new String(buffer));
    }

    @Test
    public void testFormatLongOctalOrBinaryBytes() {
        // Fits in UIDLEN octal
        byte[] buffer = new byte[TarConstants.UIDLEN];
        int nextOffset = TarUtils.formatLongOctalOrBinaryBytes(0755L, buffer, 0, TarConstants.UIDLEN);
        Assert.assertEquals(TarConstants.UIDLEN, nextOffset);
        Assert.assertEquals("0000755 ", new String(buffer));

        // Binary format for value larger than TarConstants.MAXID
        byte[] bigBuffer = new byte[TarConstants.UIDLEN];
        long largeVal = TarConstants.MAXID + 10L;
        nextOffset = TarUtils.formatLongOctalOrBinaryBytes(largeVal, bigBuffer, 0, TarConstants.UIDLEN);
        Assert.assertEquals(TarConstants.UIDLEN, nextOffset);
        Assert.assertEquals((byte) 0x80, (byte) (bigBuffer[0] & 0x80));
        long parsed = TarUtils.parseOctalOrBinary(bigBuffer, 0, TarConstants.UIDLEN);
        Assert.assertEquals(largeVal, parsed);

        // Fits in SIZELEN octal
        byte[] sizeBuffer = new byte[TarConstants.SIZELEN];
        nextOffset = TarUtils.formatLongOctalOrBinaryBytes(012345L, sizeBuffer, 0, TarConstants.SIZELEN);
        Assert.assertEquals(TarConstants.SIZELEN, nextOffset);
        Assert.assertEquals("000000012345 ", new String(sizeBuffer));

        // Binary format for value larger than TarConstants.MAXSIZE
        byte[] bigSizeBuffer = new byte[TarConstants.SIZELEN];
        long largeSizeVal = TarConstants.MAXSIZE + 10L;
        nextOffset = TarUtils.formatLongOctalOrBinaryBytes(largeSizeVal, bigSizeBuffer, 0, TarConstants.SIZELEN);
        Assert.assertEquals(TarConstants.SIZELEN, nextOffset);
        Assert.assertEquals((byte) 0x80, (byte) (bigSizeBuffer[0] & 0x80));
        long parsedSize = TarUtils.parseOctalOrBinary(bigSizeBuffer, 0, TarConstants.SIZELEN);
        Assert.assertEquals(largeSizeVal, parsedSize);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongOctalOrBinaryBytesOverflow() {
        byte[] buffer = new byte[2];
        TarUtils.formatLongOctalOrBinaryBytes(0xFFFFFFFFFFFFL, buffer, 0, 2);
    }

    @Test
    public void testFormatCheckSumOctalBytes() {
        byte[] buffer = new byte[8];
        int nextOffset = TarUtils.formatCheckSumOctalBytes(0755L, buffer, 0, 8);
        Assert.assertEquals(8, nextOffset);
        Assert.assertEquals("0000755\0 ", new String(buffer));
    }

    @Test
    public void testComputeCheckSum() {
        byte[] buffer = new byte[]{1, 2, 3, (byte) 255};
        long sum = TarUtils.computeCheckSum(buffer);
        Assert.assertEquals(1 + 2 + 3 + 255, sum);
    }

    @Test
    public void testRoundTripOctal() {
        long[] values = {0L, 1L, 077L, 0755L, 0777777L};
        byte[] buffer = new byte[12];
        for (long val : values) {
            TarUtils.formatOctalBytes(val, buffer, 0, buffer.length);
            long res = TarUtils.parseOctal(buffer, 0, buffer.length);
            Assert.assertEquals(val, res);
        }
    }

    @Test
    public void testRoundTripCheckSum() {
        long[] values = {0L, 1L, 077L, 0755L, 0777777L};
        byte[] buffer = new byte[8];
        for (long val : values) {
            TarUtils.formatCheckSumOctalBytes(val, buffer, 0, buffer.length);
            long res = TarUtils.parseOctal(buffer, 0, buffer.length);
            Assert.assertEquals(val, res);
        }
    }
}
