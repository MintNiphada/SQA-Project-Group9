package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.*;
import org.junit.Test;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.commons.compress.archivers.zip.ZipEncoding;

public class TarUtilsTest {

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalLengthLessThan2() {
        TarUtils.parseOctal(new byte[1], 0, 1);
    }

    @Test
    public void testParseOctalLeadingZeroByteReturnsZero() {
        byte[] buffer = new byte[]{0, ' ', '0'};
        assertEquals(0L, TarUtils.parseOctal(buffer, 0, 3));
    }

    @Test
    public void testParseOctalLeadingSpaces() {
        byte[] buffer = new byte[]{' ', ' ', '1', '2', ' ', 0};
        assertEquals(10L, TarUtils.parseOctal(buffer, 0, 6));
    }

    @Test
    public void testParseOctalTrailingSpacesAndNuls() {
        byte[] buffer = new byte[]{'7', '7', ' ', 0, ' '};
        assertEquals(63L, TarUtils.parseOctal(buffer, 0, 5));
    }

    @Test
    public void testParseOctalAllSpacesAndNuls() {
        byte[] buffer = new byte[]{' ', 0, ' '};
        try {
            TarUtils.parseOctal(buffer, 0, 3);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testParseOctalInvalidByte() {
        byte[] buffer = new byte[]{'1', '8', ' '};
        try {
            TarUtils.parseOctal(buffer, 0, 3);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testParseOctalMaxLong() {
        byte[] buffer = new byte[]{'1', '7', '7', '7', '7', '7', '7', '7', '7', '7', '7', '7', '7', '7', '7', '7', '7', '7', '7', '7', '7', '7', ' ', 0};
        assertEquals(Long.MAX_VALUE, TarUtils.parseOctal(buffer, 0, 24));
    }

    @Test
    public void testParseOctalZero() {
        byte[] buffer = new byte[]{'0', ' '};
        assertEquals(0L, TarUtils.parseOctal(buffer, 0, 2));
    }

    @Test
    public void testParseOctalOrBinaryHighBitNotSet() {
        byte[] buffer = new byte[]{'1', '2', ' ', 0};
        assertEquals(10L, TarUtils.parseOctalOrBinary(buffer, 0, 4));
    }

    @Test
    public void testParseOctalOrBinaryHighBitSetPositiveShort() {
        byte[] buffer = new byte[]{(byte) 0x80, 0x00, 0x01};
        assertEquals(1L, TarUtils.parseOctalOrBinary(buffer, 0, 3));
    }

    @Test
    public void testParseOctalOrBinaryHighBitSetNegativeShort() {
        byte[] buffer = new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xfe};
        assertEquals(-2L, TarUtils.parseOctalOrBinary(buffer, 0, 3));
    }

    @Test
    public void testParseOctalOrBinaryHighBitSetPositiveLong() {
        byte[] buffer = new byte[]{(byte) 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01};
        assertEquals(1L, TarUtils.parseOctalOrBinary(buffer, 0, 9));
    }

    @Test
    public void testParseOctalOrBinaryHighBitSetNegativeLong() {
        byte[] buffer = new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xfe};
        assertEquals(-2L, TarUtils.parseOctalOrBinary(buffer, 0, 9));
    }

    @Test
    public void testParseOctalOrBinaryHighBitSetTooLarge() {
        byte[] buffer = new byte[]{(byte) 0x80, 0x7f, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
        try {
            TarUtils.parseOctalOrBinary(buffer, 0, 9);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testParseBooleanTrue() {
        byte[] buffer = new byte[]{1};
        assertTrue(TarUtils.parseBoolean(buffer, 0));
    }

    @Test
    public void testParseBooleanFalse() {
        byte[] buffer = new byte[]{0};
        assertFalse(TarUtils.parseBoolean(buffer, 0));
    }

    @Test
    public void testParseBooleanOtherValue() {
        byte[] buffer = new byte[]{2};
        assertFalse(TarUtils.parseBoolean(buffer, 0));
    }

    @Test
    public void testParseNameNullTerminated() throws IOException {
        byte[] buffer = new byte[]{'a', 'b', 0, 'c'};
        assertEquals("ab", TarUtils.parseName(buffer, 0, 4));
    }

    @Test
    public void testParseNameFullLength() throws IOException {
        byte[] buffer = new byte[]{'a', 'b', 'c'};
        assertEquals("abc", TarUtils.parseName(buffer, 0, 3));
    }

    @Test
    public void testParseNameEmpty() throws IOException {
        byte[] buffer = new byte[]{0, 0};
        assertEquals("", TarUtils.parseName(buffer, 0, 2));
    }

    @Test
    public void testParseNameWithEncoding() throws IOException {
        byte[] buffer = new byte[]{'x', 'y', 0};
        ZipEncoding encoding = new ZipEncoding() {
            public boolean canEncode(String name) { return true; }
            public ByteBuffer encode(String name) { return null; }
            public String decode(byte[] data) { return new String(data); }
        };
        assertEquals("xy", TarUtils.parseName(buffer, 0, 3, encoding));
    }

    @Test
    public void testParseNameWithEncodingEmpty() throws IOException {
        byte[] buffer = new byte[]{0};
        ZipEncoding encoding = new ZipEncoding() {
            public boolean canEncode(String name) { return true; }
            public ByteBuffer encode(String name) { return null; }
            public String decode(byte[] data) { return ""; }
        };
        assertEquals("", TarUtils.parseName(buffer, 0, 1, encoding));
    }

    @Test
    public void testFormatNameBytesShorterThanBuffer() throws IOException {
        byte[] buf = new byte[10];
        int result = TarUtils.formatNameBytes("abc", buf, 0, 10);
        assertEquals(10, result);
        assertEquals('a', buf[0]);
        assertEquals('b', buf[1]);
        assertEquals('c', buf[2]);
        assertEquals(0, buf[3]);
    }

    @Test
    public void testFormatNameBytesLongerThanBuffer() throws IOException {
        byte[] buf = new byte[3];
        int result = TarUtils.formatNameBytes("abcdef", buf, 0, 3);
        assertEquals(3, result);
        assertEquals('a', buf[0]);
        assertEquals('b', buf[1]);
        assertEquals('c', buf[2]);
    }

    @Test
    public void testFormatNameBytesExactFit() throws IOException {
        byte[] buf = new byte[3];
        int result = TarUtils.formatNameBytes("abc", buf, 0, 3);
        assertEquals(3, result);
        assertEquals('a', buf[0]);
        assertEquals('b', buf[1]);
        assertEquals('c', buf[2]);
    }

    @Test
    public void testFormatNameBytesEmpty() throws IOException {
        byte[] buf = new byte[3];
        int result = TarUtils.formatNameBytes("", buf, 0, 3);
        assertEquals(3, result);
        assertEquals(0, buf[0]);
        assertEquals(0, buf[1]);
        assertEquals(0, buf[2]);
    }

    @Test
    public void testFormatNameBytesWithEncodingTruncation() throws IOException {
        byte[] buf = new byte[2];
        ZipEncoding encoding = new ZipEncoding() {
            public boolean canEncode(String name) { return true; }
            public ByteBuffer encode(String name) {
                byte[] b = new byte[name.length() * 2];
                for (int i = 0; i < name.length(); i++) {
                    b[i*2] = (byte) name.charAt(i);
                    b[i*2+1] = (byte) name.charAt(i);
                }
                return ByteBuffer.wrap(b);
            }
            public String decode(byte[] data) { return null; }
        };
        int result = TarUtils.formatNameBytes("abc", buf, 0, 2, encoding);
        assertEquals(2, result);
        assertEquals('a', buf[0]);
        assertEquals('a', buf[1]);
    }

    @Test
    public void testFormatUnsignedOctalStringZero() {
        byte[] buf = new byte[3];
        TarUtils.formatUnsignedOctalString(0L, buf, 0, 3);
        assertEquals('0', buf[0]);
        assertEquals('0', buf[1]);
        assertEquals('0', buf[2]);
    }

    @Test
    public void testFormatUnsignedOctalStringValueFits() {
        byte[] buf = new byte[3];
        TarUtils.formatUnsignedOctalString(8L, buf, 0, 3);
        assertEquals('0', buf[0]);
        assertEquals('1', buf[1]);
        assertEquals('0', buf[2]);
    }

    @Test
    public void testFormatUnsignedOctalStringValueTooLarge() {
        byte[] buf = new byte[2];
        try {
            TarUtils.formatUnsignedOctalString(8L, buf, 0, 2);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
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
        assertEquals(0, buf[5]? 0:0);
    }

    @Test
    public void testFormatLongOctalBytes() {
        byte[] buf = new byte[4];
        int result = TarUtils.formatLongOctalBytes(10L, buf, 0, 4);
        assertEquals(4, result);
        assertEquals('0', buf[0]);
        assertEquals('1', buf[1]);
        assertEquals('2', buf[2]);
        assertEquals(' ', buf[3]);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesFitsOctal() {
        byte[] buf = new byte[4];
        int result = TarUtils.formatLongOctalOrBinaryBytes(10L, buf, 0, 4);
        assertEquals(4, result);
        assertEquals('0', buf[0]);
        assertEquals('1', buf[1]);
        assertEquals('2', buf[2]);
        assertEquals(' ', buf[3]);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesNegativeShort() {
        byte[] buf = new byte[3];
        int result = TarUtils.formatLongOctalOrBinaryBytes(-1L, buf, 0, 3);
        assertEquals(3, result);
        assertEquals((byte) 0xff, buf[0]);
        assertEquals((byte) 0xff, buf[1]);
        assertEquals((byte) 0xff, buf[2]);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesPositiveLong() {
        byte[] buf = new byte[9];
        int result = TarUtils.formatLongOctalOrBinaryBytes(0x8000000000000000L, buf, 0, 9);
        assertEquals(9, result);
        assertEquals((byte) 0x80, buf[0]);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesNegativeLong() {
        byte[] buf = new byte[9];
        int result = TarUtils.formatLongOctalOrBinaryBytes(-0x8000000000000000L, buf, 0, 9);
        assertEquals(9, result);
        assertEquals((byte) 0xff, buf[0]);
    }

    @Test
    public void testFormatCheckSumOctalBytes() {
        byte[] buf = new byte[5];
        int result = TarUtils.formatCheckSumOctalBytes(10L, buf, 0, 5);
        assertEquals(5, result);
        assertEquals('0', buf[0]);
        assertEquals('0', buf[1]);
        assertEquals('1', buf[2]);
        assertEquals('2', buf[3]);
        assertEquals(0, buf[4]);
        assertEquals(' ', buf[5]? ' ':0);
    }

    @Test
    public void testComputeCheckSum() {
        byte[] buf = new byte[]{1, 2, 3};
        assertEquals(6L, TarUtils.computeCheckSum(buf));
    }

    @Test
    public void testVerifyCheckSumMatchesUnsigned() {
        byte[] header = new byte[512];
        for (int i = 0; i < 512; i++) header[i] = 0;
        header[148] = '0';
        header[149] = '0';
        header[150] = '0';
        header[151] = '0';
        header[152] = '0';
        header[153] = '0';
        header[154] = ' ';
        header[155] = 0;
        long sum = TarUtils.computeCheckSum(header);
        assertTrue(TarUtils.verifyCheckSum(header));
    }

    @Test
    public void testVerifyCheckSumMatchesSigned() {
        byte[] header = new byte[512];
        for (int i = 0; i < 512; i++) header[i] = (byte) 0xff;
        header[148] = '1';
        header[149] = '7';
        header[150] = '7';
        header[151] = '7';
        header[152] = '7';
        header[153] = '7';
        header[154] = ' ';
        header[155] = 0;
        assertTrue(TarUtils.verifyCheckSum(header));
    }

    @Test
    public void testVerifyCheckSumGreaterThanUnsigned() {
        byte[] header = new byte[512];
        for (int i = 0; i < 512; i++) header[i] = 0;
        header[148] = '0';
        header[149] = '0';
        header[150] = '0';
        header[151] = '0';
        header[152] = '0';
        header[153] = '1';
        header[154] = ' ';
        header[155] = 0;
        assertTrue(TarUtils.verifyCheckSum(header));
    }

    @Test
    public void testVerifyCheckSumNoMatch() {
        byte[] header = new byte[512];
        for (int i = 0; i < 512; i++) header[i] = 1;
        header[148] = '0';
        header[149] = '0';
        header[150] = '0';
        header[151] = '0';
        header[152] = '0';
        header[153] = '0';
        header[154] = ' ';
        header[155] = 0;
        assertFalse(TarUtils.verifyCheckSum(header));
    }

    @Test
    public void testVerifyCheckSumOnlyFirstSixDigits() {
        byte[] header = new byte[512];
        for (int i = 0; i < 512; i++) header[i] = 0;
        header[148] = '0';
        header[149] = '0';
        header[150] = '0';
        header[151] = '0';
        header[152] = '0';
        header[153] = '0';
        header[154] = '7';
        header[155] = 0;
        assertTrue(TarUtils.verifyCheckSum(header));
    }

    @Test
    public void testVerifyCheckSumNonOctalAfterDigits() {
        byte[] header = new byte[512];
        for (int i = 0; i < 512; i++) header[i] = 0;
        header[148] = '0';
        header[149] = '0';
        header[150] = '0';
        header[151] = '0';
        header[152] = '0';
        header[153] = '0';
        header[154] = '8';
        header[155] = 0;
        assertTrue(TarUtils.verifyCheckSum(header));
    }
}
