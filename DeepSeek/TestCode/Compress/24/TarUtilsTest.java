package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.*;
import org.junit.Test;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.commons.compress.archivers.zip.ZipEncoding;

public class TarUtilsTest {

    @Test
    public void testParseOctalLengthLessThan2() {
        try {
            TarUtils.parseOctal(new byte[1], 0, 1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("must be at least 2"));
        }
    }

    @Test
    public void testParseOctalLeadingNul() {
        byte[] buffer = {0, '1', '2', ' '};
        assertEquals(0L, TarUtils.parseOctal(buffer, 0, 4));
    }

    @Test
    public void testParseOctalAllNuls() {
        byte[] buffer = {0, 0, 0};
        assertEquals(0L, TarUtils.parseOctal(buffer, 0, 3));
    }

    @Test
    public void testParseOctalLeadingSpaces() {
        byte[] buffer = {' ', ' ', '1', '2', ' ', 0};
        assertEquals(10L, TarUtils.parseOctal(buffer, 0, 6));
    }

    @Test
    public void testParseOctalTrailingNulOrSpace() {
        byte[] buffer = {'1', '2', 0};
        assertEquals(10L, TarUtils.parseOctal(buffer, 0, 3));
        buffer = new byte[]{'1', '2', ' '};
        assertEquals(10L, TarUtils.parseOctal(buffer, 0, 3));
    }

    @Test
    public void testParseOctalExtraTrailing() {
        byte[] buffer = {'1', '2', ' ', 0, ' '};
        assertEquals(10L, TarUtils.parseOctal(buffer, 0, 5));
    }

    @Test
    public void testParseOctalNoTrailingThrows() {
        byte[] buffer = {'1', '2'};
        try {
            TarUtils.parseOctal(buffer, 0, 2);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Invalid byte"));
        }
    }

    @Test
    public void testParseOctalInvalidByte() {
        byte[] buffer = {'1', '8', ' '};
        try {
            TarUtils.parseOctal(buffer, 0, 3);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Invalid byte"));
        }
    }

    @Test
    public void testParseOctalAllSpaces() {
        byte[] buffer = {' ', ' ', ' '};
        assertEquals(0L, TarUtils.parseOctal(buffer, 0, 3));
    }

    @Test
    public void testParseOctalMaxValue() {
        byte[] buffer = {'7', '7', '7', '7', '7', '7', '7', '7', '7', '7', ' ', 0};
        assertEquals(0177777777777L, TarUtils.parseOctal(buffer, 0, 12));
    }

    @Test
    public void testParseOctalOrBinaryOctalPath() {
        byte[] buffer = {'1', '2', ' '};
        assertEquals(10L, TarUtils.parseOctalOrBinary(buffer, 0, 3));
    }

    @Test
    public void testParseOctalOrBinaryPositiveBinaryShort() {
        byte[] buffer = {(byte) 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01};
        assertEquals(1L, TarUtils.parseOctalOrBinary(buffer, 0, 8));
    }

    @Test
    public void testParseOctalOrBinaryNegativeBinaryShort() {
        byte[] buffer = {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xfe};
        assertEquals(-2L, TarUtils.parseOctalOrBinary(buffer, 0, 8));
    }

    @Test
    public void testParseOctalOrBinaryBinaryLongExceedsSignedLong() {
        byte[] buffer = {(byte) 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01};
        try {
            TarUtils.parseOctalOrBinary(buffer, 0, 9);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("exceeds maximum signed long"));
        }
    }

    @Test
    public void testParseOctalOrBinaryPositiveBinaryBigInteger() {
        byte[] buffer = new byte[9];
        buffer[0] = (byte) 0x80;
        buffer[8] = 0x01;
        assertEquals(1L, TarUtils.parseOctalOrBinary(buffer, 0, 9));
    }

    @Test
    public void testParseOctalOrBinaryNegativeBinaryBigInteger() {
        byte[] buffer = new byte[9];
        buffer[0] = (byte) 0xff;
        for (int i = 1; i < 9; i++) buffer[i] = (byte) 0xff;
        buffer[8] = (byte) 0xfe;
        assertEquals(-2L, TarUtils.parseOctalOrBinary(buffer, 0, 9));
    }

    @Test
    public void testParseOctalOrBinaryBinaryBigIntegerOverflow() {
        byte[] buffer = new byte[9];
        buffer[0] = (byte) 0x80;
        buffer[1] = (byte) 0x80;
        try {
            TarUtils.parseOctalOrBinary(buffer, 0, 9);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("exceeds maximum signed long"));
        }
    }

    @Test
    public void testParseBooleanTrue() {
        byte[] buffer = {1};
        assertTrue(TarUtils.parseBoolean(buffer, 0));
    }

    @Test
    public void testParseBooleanFalse() {
        byte[] buffer = {0};
        assertFalse(TarUtils.parseBoolean(buffer, 0));
    }

    @Test
    public void testParseBooleanOtherValue() {
        byte[] buffer = {2};
        assertFalse(TarUtils.parseBoolean(buffer, 0));
    }

    @Test
    public void testParseNameNormal() {
        byte[] buffer = {'a', 'b', 'c', 0, 'd'};
        assertEquals("abc", TarUtils.parseName(buffer, 0, 5));
    }

    @Test
    public void testParseNameNoNul() {
        byte[] buffer = {'a', 'b', 'c'};
        assertEquals("abc", TarUtils.parseName(buffer, 0, 3));
    }

    @Test
    public void testParseNameEmpty() {
        byte[] buffer = {0, 0};
        assertEquals("", TarUtils.parseName(buffer, 0, 2));
    }

    @Test
    public void testParseNameWithEncoding() throws IOException {
        byte[] buffer = {'a', 'b', 0};
        ZipEncoding encoding = TarUtils.DEFAULT_ENCODING;
        assertEquals("ab", TarUtils.parseName(buffer, 0, 3, encoding));
    }

    @Test
    public void testParseNameEncodingFallback() {
        byte[] buffer = {'a', 'b', 0};
        assertEquals("ab", TarUtils.parseName(buffer, 0, 3));
    }

    @Test
    public void testFormatNameBytesShorter() {
        byte[] buf = new byte[10];
        int result = TarUtils.formatNameBytes("abc", buf, 0, 10);
        assertEquals(10, result);
        assertEquals('a', buf[0]);
        assertEquals('b', buf[1]);
        assertEquals('c', buf[2]);
        assertEquals(0, buf[3]);
        assertEquals(0, buf[9]);
    }

    @Test
    public void testFormatNameBytesLonger() {
        byte[] buf = new byte[3];
        int result = TarUtils.formatNameBytes("abcdef", buf, 0, 3);
        assertEquals(3, result);
        assertEquals('a', buf[0]);
        assertEquals('b', buf[1]);
        assertEquals('c', buf[2]);
    }

    @Test
    public void testFormatNameBytesExact() {
        byte[] buf = new byte[3];
        int result = TarUtils.formatNameBytes("abc", buf, 0, 3);
        assertEquals(3, result);
        assertEquals('a', buf[0]);
        assertEquals('b', buf[1]);
        assertEquals('c', buf[2]);
    }

    @Test
    public void testFormatNameBytesWithEncoding() throws IOException {
        byte[] buf = new byte[5];
        ZipEncoding encoding = TarUtils.DEFAULT_ENCODING;
        int result = TarUtils.formatNameBytes("ab", buf, 0, 5, encoding);
        assertEquals(5, result);
        assertEquals('a', buf[0]);
        assertEquals('b', buf[1]);
        assertEquals(0, buf[2]);
    }

    @Test
    public void testFormatUnsignedOctalStringZero() {
        byte[] buffer = new byte[5];
        TarUtils.formatUnsignedOctalString(0L, buffer, 0, 5);
        assertEquals('0', buffer[4]);
        assertEquals('0', buffer[0]);
    }

    @Test
    public void testFormatUnsignedOctalStringSmall() {
        byte[] buffer = new byte[5];
        TarUtils.formatUnsignedOctalString(8L, buffer, 0, 5);
        assertEquals('0', buffer[0]);
        assertEquals('0', buffer[1]);
        assertEquals('0', buffer[2]);
        assertEquals('1', buffer[3]);
        assertEquals('0', buffer[4]);
    }

    @Test
    public void testFormatUnsignedOctalStringMaxFit() {
        byte[] buffer = new byte[3];
        TarUtils.formatUnsignedOctalString(077L, buffer, 0, 3);
        assertEquals('0', buffer[0]);
        assertEquals('7', buffer[1]);
        assertEquals('7', buffer[2]);
    }

    @Test
    public void testFormatUnsignedOctalStringTooLarge() {
        byte[] buffer = new byte[2];
        try {
            TarUtils.formatUnsignedOctalString(8L, buffer, 0, 2);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("will not fit"));
        }
    }

    @Test
    public void testFormatOctalBytes() {
        byte[] buf = new byte[5];
        int result = TarUtils.formatOctalBytes(10L, buf, 0, 5);
        assertEquals(5, result);
        assertEquals('0', buf[0]);
        assertEquals('0', buf[1]);
        assertEquals('1', buf[2]);
        assertEquals('2', buf[3]);
        assertEquals(' ', buf[4]);
        assertEquals(0, buf[4]); // Actually buf[4] is space, then buf[5]? Wait length=5, offset=0, idx=length-2=3, formatUnsignedOctalString(value, buf, 0, 3) fills indices 0,1,2. Then buf[offset+idx++] = ' ' -> buf[3] = ' ', then buf[offset+idx] = 0 -> buf[4] = 0. So buf[3]=' ', buf[4]=0. So assert: buf[0]='0', buf[1]='0', buf[2]='1', buf[3]=' ', buf[4]=0.
        assertEquals(' ', buf[3]);
        assertEquals(0, buf[4]);
    }

    @Test
    public void testFormatLongOctalBytes() {
        byte[] buf = new byte[5];
        int result = TarUtils.formatLongOctalBytes(10L, buf, 0, 5);
        assertEquals(5, result);
        assertEquals('0', buf[0]);
        assertEquals('0', buf[1]);
        assertEquals('0', buf[2]);
        assertEquals('1', buf[3]);
        assertEquals(' ', buf[4]);
    }

    @Test
    public void testFormatCheckSumOctalBytes() {
        byte[] buf = new byte[5];
        int result = TarUtils.formatCheckSumOctalBytes(10L, buf, 0, 5);
        assertEquals(5, result);
        assertEquals('0', buf[0]);
        assertEquals('0', buf[1]);
        assertEquals('1', buf[2]);
        assertEquals(0, buf[3]);
        assertEquals(' ', buf[4]);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesOctalFit() {
        byte[] buf = new byte[8];
        int result = TarUtils.formatLongOctalOrBinaryBytes(100L, buf, 0, 8);
        assertEquals(8, result);
        assertEquals('0', buf[0]);
        assertEquals(' ', buf[7]);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesBinaryShortPositive() {
        byte[] buf = new byte[8];
        int result = TarUtils.formatLongOctalOrBinaryBytes(2097152L, buf, 0, 8);
        assertEquals(8, result);
        assertEquals((byte)0x80, buf[0]);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesBinaryShortNegative() {
        byte[] buf = new byte[8];
        int result = TarUtils.formatLongOctalOrBinaryBytes(-1L, buf, 0, 8);
        assertEquals(8, result);
        assertEquals((byte)0xff, buf[0]);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesBinaryLongPositive() {
        byte[] buf = new byte[9];
        int result = TarUtils.formatLongOctalOrBinaryBytes(8589934592L, buf, 0, 9);
        assertEquals(9, result);
        assertEquals((byte)0x80, buf[0]);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesBinaryLongNegative() {
        byte[] buf = new byte[9];
        int result = TarUtils.formatLongOctalOrBinaryBytes(-1L, buf, 0, 9);
        assertEquals(9, result);
        assertEquals((byte)0xff, buf[0]);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesTooLarge() {
        byte[] buf = new byte[8];
        try {
            TarUtils.formatLongOctalOrBinaryBytes(1L << 56, buf, 0, 8);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("too large"));
        }
    }

    @Test
    public void testComputeCheckSum() {
        byte[] header = new byte[512];
        for (int i = 0; i < 512; i++) header[i] = (byte) i;
        long sum = TarUtils.computeCheckSum(header);
        long expected = 0;
        for (int i = 0; i < 512; i++) expected += (header[i] & 0xff);
        assertEquals(expected, sum);
    }

    @Test
    public void testVerifyCheckSumValidUnsigned() {
        byte[] header = new byte[512];
        for (int i = 0; i < 512; i++) header[i] = (byte) i;
        long unsignedSum = 0;
        for (int i = 0; i < 512; i++) {
            if (i >= TarConstants.CHKSUM_OFFSET && i < TarConstants.CHKSUM_OFFSET + TarConstants.CHKSUMLEN) {
                header[i] = ' ';
            }
            unsignedSum += (header[i] & 0xff);
        }
        String octal = Long.toOctalString(unsignedSum);
        while (octal.length() < 6) octal = "0" + octal;
        for (int i = 0; i < 6; i++) {
            header[TarConstants.CHKSUM_OFFSET + i] = (byte) octal.charAt(i);
        }
        assertTrue(TarUtils.verifyCheckSum(header));
    }

    @Test
    public void testVerifyCheckSumValidSigned() {
        byte[] header = new byte[512];
        for (int i = 0; i < 512; i++) header[i] = (byte) i;
        long signedSum = 0;
        for (int i = 0; i < 512; i++) {
            if (i >= TarConstants.CHKSUM_OFFSET && i < TarConstants.CHKSUM_OFFSET + TarConstants.CHKSUMLEN) {
                header[i] = ' ';
            }
            signedSum += header[i];
        }
        String octal = Long.toOctalString(signedSum & 0xFFFFFFFFFFFFFFFFL);
        while (octal.length() < 6) octal = "0" + octal;
        for (int i = 0; i < 6; i++) {
            header[TarConstants.CHKSUM_OFFSET + i] = (byte) octal.charAt(i);
        }
        assertTrue(TarUtils.verifyCheckSum(header));
    }

    @Test
    public void testVerifyCheckSumStoredGreaterThanUnsigned() {
        byte[] header = new byte[512];
        for (int i = 0; i < 512; i++) header[i] = (byte) i;
        long unsignedSum = 0;
        for (int i = 0; i < 512; i++) {
            if (i >= TarConstants.CHKSUM_OFFSET && i < TarConstants.CHKSUM_OFFSET + TarConstants.CHKSUMLEN) {
                header[i] = ' ';
            }
            unsignedSum += (header[i] & 0xff);
        }
        long stored = unsignedSum + 1;
        String octal = Long.toOctalString(stored);
        while (octal.length() < 6) octal = "0" + octal;
        for (int i = 0; i < 6; i++) {
            header[TarConstants.CHKSUM_OFFSET + i] = (byte) octal.charAt(i);
        }
        assertTrue(TarUtils.verifyCheckSum(header));
    }

    @Test
    public void testVerifyCheckSumInvalid() {
        byte[] header = new byte[512];
        for (int i = 0; i < 512; i++) header[i] = (byte) i;
        long unsignedSum = 0;
        for (int i = 0; i < 512; i++) {
            if (i >= TarConstants.CHKSUM_OFFSET && i < TarConstants.CHKSUM_OFFSET + TarConstants.CHKSUMLEN) {
                header[i] = ' ';
            }
            unsignedSum += (header[i] & 0xff);
        }
        long stored = unsignedSum - 1;
        String octal = Long.toOctalString(stored);
        while (octal.length() < 6) octal = "0" + octal;
        for (int i = 0; i < 6; i++) {
            header[TarConstants.CHKSUM_OFFSET + i] = (byte) octal.charAt(i);
        }
        assertFalse(TarUtils.verifyCheckSum(header));
    }

    @Test
    public void testVerifyCheckSumDigitsMoreThan6() {
        byte[] header = new byte[512];
        for (int i = 0; i < 512; i++) header[i] = (byte) i;
        long unsignedSum = 0;
        for (int i = 0; i < 512; i++) {
            if (i >= TarConstants.CHKSUM_OFFSET && i < TarConstants.CHKSUM_OFFSET + TarConstants.CHKSUMLEN) {
                header[i] = ' ';
            }
            unsignedSum += (header[i] & 0xff);
        }
        String octal = "0000007"; // 7 digits
        for (int i = 0; i < 7; i++) {
            header[TarConstants.CHKSUM_OFFSET + i] = (byte) octal.charAt(i);
        }
        assertFalse(TarUtils.verifyCheckSum(header));
    }
}
