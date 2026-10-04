package org.apache.commons.compress.archivers.tar;

import org.apache.commons.compress.archivers.zip.ZipEncoding;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class TarUtilsTest {

    @Test
    public void testParseOctalValid() {
        byte[] buffer = "0000755 \0".getBytes();
        long val = TarUtils.parseOctal(buffer, 0, buffer.length);
        Assert.assertEquals(0755L, val);

        buffer = "   755 \0".getBytes();
        val = TarUtils.parseOctal(buffer, 0, buffer.length);
        Assert.assertEquals(0755L, val);

        buffer = "   755  ".getBytes();
        val = TarUtils.parseOctal(buffer, 0, buffer.length);
        Assert.assertEquals(0755L, val);

        buffer = "0\0".getBytes();
        val = TarUtils.parseOctal(buffer, 0, 2);
        Assert.assertEquals(0L, val);

        buffer = new byte[]{0, 0, 0, 0};
        val = TarUtils.parseOctal(buffer, 0, buffer.length);
        Assert.assertEquals(0L, val);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalTooShort() {
        byte[] buffer = new byte[]{0};
        TarUtils.parseOctal(buffer, 0, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalInvalidTrailer() {
        byte[] buffer = "0000755x".getBytes();
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalInvalidByte() {
        byte[] buffer = "0000855 \0".getBytes();
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    @Test
    public void testParseOctalOrBinaryOctal() {
        byte[] buffer = "0000755 \0".getBytes();
        long val = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        Assert.assertEquals(0755L, val);
    }

    @Test
    public void testParseOctalOrBinaryBinaryPositiveSmall() {
        byte[] buffer = new byte[8];
        buffer[0] = (byte) 0x80;
        buffer[7] = 0x01;
        long val = TarUtils.parseOctalOrBinary(buffer, 0, 8);
        Assert.assertEquals(1L, val);

        buffer = new byte[4];
        buffer[0] = (byte) 0x80;
        buffer[1] = 0x01;
        buffer[2] = 0x02;
        buffer[3] = 0x03;
        val = TarUtils.parseOctalOrBinary(buffer, 0, 4);
        Assert.assertEquals(0x010203L, val);
    }

    @Test
    public void testParseOctalOrBinaryBinaryNegativeSmall() {
        byte[] buffer = new byte[8];
        buffer[0] = (byte) 0xff;
        for (int i = 1; i < 8; i++) {
            buffer[i] = (byte) 0xff;
        }
        long val = TarUtils.parseOctalOrBinary(buffer, 0, 8);
        Assert.assertEquals(-1L, val);

        buffer = new byte[4];
        buffer[0] = (byte) 0xff;
        buffer[1] = (byte) 0xff;
        buffer[2] = (byte) 0xff;
        buffer[3] = (byte) 0xfe;
        val = TarUtils.parseOctalOrBinary(buffer, 0, 4);
        Assert.assertEquals(-2L, val);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseBinaryLongTooLargeLength() throws Exception {
        java.lang.reflect.Method m = TarUtils.class.getDeclaredMethod("parseBinaryLong", byte[].class, int.class, int.class, boolean.class);
        m.setAccessible(true);
        try {
            m.invoke(null, new byte[9], 0, 9, false);
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw (Exception) e.getCause();
        }
    }

    @Test
    public void testParseOctalOrBinaryBinaryBigInteger() {
        byte[] buffer = new byte[12];
        buffer[0] = (byte) 0x80;
        buffer[11] = 0x01;
        long val = TarUtils.parseOctalOrBinary(buffer, 0, 12);
        Assert.assertEquals(1L, val);

        buffer = new byte[12];
        Arrays.fill(buffer, (byte) 0xff);
        val = TarUtils.parseOctalOrBinary(buffer, 0, 12);
        Assert.assertEquals(-1L, val);

        buffer = new byte[12];
        buffer[0] = (byte) 0x80;
        buffer[4] = 0x01;
        buffer[5] = 0x00;
        buffer[6] = 0x00;
        buffer[7] = 0x00;
        buffer[8] = 0x00;
        buffer[9] = 0x00;
        buffer[10] = 0x00;
        buffer[11] = 0x00;
        val = TarUtils.parseOctalOrBinary(buffer, 0, 12);
        Assert.assertEquals(0x0100000000000000L, val);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalOrBinaryBinaryBigIntegerOverflow() {
        byte[] buffer = new byte[12];
        buffer[0] = (byte) 0x80;
        buffer[1] = (byte) 0x7f;
        buffer[2] = (byte) 0xff;
        buffer[3] = (byte) 0xff;
        buffer[4] = (byte) 0xff;
        buffer[5] = (byte) 0xff;
        buffer[6] = (byte) 0xff;
        buffer[7] = (byte) 0xff;
        buffer[8] = (byte) 0xff;
        buffer[9] = (byte) 0xff;
        TarUtils.parseOctalOrBinary(buffer, 0, 12);
    }

    @Test
    public void testParseBoolean() {
        byte[] buffer = new byte[]{1, 0, 2};
        Assert.assertTrue(TarUtils.parseBoolean(buffer, 0));
        Assert.assertFalse(TarUtils.parseBoolean(buffer, 1));
        Assert.assertFalse(TarUtils.parseBoolean(buffer, 2));
    }

    @Test
    public void testParseName() throws IOException {
        byte[] buffer = "hello\0world".getBytes();
        String name = TarUtils.parseName(buffer, 0, buffer.length);
        Assert.assertEquals("hello", name);

        buffer = "hello".getBytes();
        name = TarUtils.parseName(buffer, 0, buffer.length);
        Assert.assertEquals("hello", name);

        buffer = new byte[]{0, 0, 0};
        name = TarUtils.parseName(buffer, 0, buffer.length);
        Assert.assertEquals("", name);
    }

    @Test
    public void testParseNameFallbackEncoding() {
        byte[] buffer = new byte[]{(byte) 0xc3, (byte) 0xa9, 0};
        String name = TarUtils.parseName(buffer, 0, buffer.length, TarUtils.FALLBACK_ENCODING);
        Assert.assertEquals("\u00c3\u00a9", name);
        Assert.assertTrue(TarUtils.FALLBACK_ENCODING.canEncode("test"));
    }

    @Test
    public void testFormatNameBytes() throws IOException {
        byte[] buffer = new byte[10];
        int off = TarUtils.formatNameBytes("hello", buffer, 0, 10);
        Assert.assertEquals(10, off);
        Assert.assertEquals("hello", new String(buffer, 0, 5));
        for (int i = 5; i < 10; i++) {
            Assert.assertEquals(0, buffer[i]);
        }

        buffer = new byte[4];
        off = TarUtils.formatNameBytes("toolongname", buffer, 0, 4);
        Assert.assertEquals(4, off);
        Assert.assertEquals("tool", new String(buffer, 0, 4));
    }

    @Test
    public void testFormatUnsignedOctalString() {
        byte[] buffer = new byte[6];
        TarUtils.formatUnsignedOctalString(0, buffer, 0, 6);
        Assert.assertEquals("000000", new String(buffer));

        TarUtils.formatUnsignedOctalString(0755, buffer, 0, 6);
        Assert.assertEquals("000755", new String(buffer));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatUnsignedOctalStringOverflow() {
        byte[] buffer = new byte[2];
        TarUtils.formatUnsignedOctalString(0755, buffer, 0, 2);
    }

    @Test
    public void testFormatOctalBytes() {
        byte[] buffer = new byte[8];
        int off = TarUtils.formatOctalBytes(0755, buffer, 0, 8);
        Assert.assertEquals(8, off);
        Assert.assertEquals("0000755 \0", new String(buffer));
    }

    @Test
    public void testFormatLongOctalBytes() {
        byte[] buffer = new byte[8];
        int off = TarUtils.formatLongOctalBytes(0755, buffer, 0, 8);
        Assert.assertEquals(8, off);
        Assert.assertEquals("0000755 ", new String(buffer));
    }

    @Test
    public void testFormatLongOctalOrBinaryBytes() {
        byte[] buffer = new byte[8];
        int off = TarUtils.formatLongOctalOrBinaryBytes(0755, buffer, 0, 8);
        Assert.assertEquals(8, off);
        Assert.assertEquals("0000755 ", new String(buffer));

        buffer = new byte[TarConstants.UIDLEN];
        off = TarUtils.formatLongOctalOrBinaryBytes(0x1FFFFFL, buffer, 0, TarConstants.UIDLEN);
        Assert.assertEquals(TarConstants.UIDLEN, off);
        Assert.assertEquals(0x80, buffer[0] & 0xFF);
        long val = TarUtils.parseOctalOrBinary(buffer, 0, TarConstants.UIDLEN);
        Assert.assertEquals(0x1FFFFFL, val);

        buffer = new byte[TarConstants.UIDLEN];
        off = TarUtils.formatLongOctalOrBinaryBytes(-1L, buffer, 0, TarConstants.UIDLEN);
        Assert.assertEquals(TarConstants.UIDLEN, off);
        Assert.assertEquals(0xFF, buffer[0] & 0xFF);
        val = TarUtils.parseOctalOrBinary(buffer, 0, TarConstants.UIDLEN);
        Assert.assertEquals(-1L, val);

        buffer = new byte[12];
        off = TarUtils.formatLongOctalOrBinaryBytes(0x1FFFFFFFFL, buffer, 0, 12);
        Assert.assertEquals(12, off);
        Assert.assertEquals(0x80, buffer[0] & 0xFF);
        val = TarUtils.parseOctalOrBinary(buffer, 0, 12);
        Assert.assertEquals(0x1FFFFFFFFL, val);

        buffer = new byte[12];
        off = TarUtils.formatLongOctalOrBinaryBytes(-100L, buffer, 0, 12);
        Assert.assertEquals(12, off);
        Assert.assertEquals(0xFF, buffer[0] & 0xFF);
        val = TarUtils.parseOctalOrBinary(buffer, 0, 12);
        Assert.assertEquals(-100L, val);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongBinaryOverflow() throws Exception {
        java.lang.reflect.Method m = TarUtils.class.getDeclaredMethod("formatLongBinary", long.class, byte[].class, int.class, int.class, boolean.class);
        m.setAccessible(true);
        try {
            m.invoke(null, 0x100000000L, new byte[4], 0, 4, false);
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw (Exception) e.getCause();
        }
    }

    @Test
    public void testFormatCheckSumOctalBytes() {
        byte[] buffer = new byte[8];
        int off = TarUtils.formatCheckSumOctalBytes(0755, buffer, 0, 8);
        Assert.assertEquals(8, off);
        Assert.assertEquals("000755\0 ", new String(buffer));
    }

    @Test
    public void testComputeAndVerifyCheckSum() {
        byte[] header = new byte[512];
        Arrays.fill(header, (byte) 'a');
        TarUtils.formatCheckSumOctalBytes(0, header, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUMLEN);

        long sum = 0;
        for (int i = 0; i < 512; i++) {
            if (i >= TarConstants.CHKSUM_OFFSET && i < TarConstants.CHKSUM_OFFSET + TarConstants.CHKSUMLEN) {
                sum += (byte) ' ';
            } else {
                sum += (byte) 'a';
            }
        }

        TarUtils.formatCheckSumOctalBytes(sum, header, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUMLEN);
        Assert.assertTrue(TarUtils.verifyCheckSum(header));

        header[TarConstants.CHKSUM_OFFSET] = '9';
        header[TarConstants.CHKSUM_OFFSET + 1] = '9';
        TarUtils.verifyCheckSum(header);

        byte[] allZero = new byte[512];
        TarUtils.formatCheckSumOctalBytes(1000, allZero, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUMLEN);
        Assert.assertTrue(TarUtils.verifyCheckSum(allZero));

        byte[] badHeader = new byte[512];
        Arrays.fill(badHeader, (byte) 0xff);
        TarUtils.formatCheckSumOctalBytes(0, badHeader, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUMLEN);
        Assert.assertFalse(TarUtils.verifyCheckSum(badHeader));
    }

    @Test
    public void testComputeCheckSum() {
        byte[] buf = new byte[]{1, 2, 3, (byte) 255};
        Assert.assertEquals(1 + 2 + 3 + 255, TarUtils.computeCheckSum(buf));
    }

    @Test
    public void testExceptionMessageCoverage() {
        byte[] buffer = new byte[]{0, (byte) '8', 0};
        try {
            TarUtils.parseOctal(buffer, 0, 3);
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("{NUL}"));
        }
    }
}
