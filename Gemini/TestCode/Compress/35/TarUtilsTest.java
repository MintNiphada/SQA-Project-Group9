package org.apache.commons.compress.archivers.tar;

import org.apache.commons.compress.archivers.zip.ZipEncoding;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;

public class TarUtilsTest {

    @Test
    public void testParseOctalValid() {
        byte[] buffer = " 0755 \0".getBytes();
        long val = TarUtils.parseOctal(buffer, 0, buffer.length);
        Assert.assertEquals(493L, val);
    }

    @Test
    public void testParseOctalLeadingNull() {
        byte[] buffer = new byte[]{0, '7', '5', '5', ' '};
        long val = TarUtils.parseOctal(buffer, 0, buffer.length);
        Assert.assertEquals(0L, val);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalLengthTooShort() {
        byte[] buffer = new byte[]{'0'};
        TarUtils.parseOctal(buffer, 0, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalInvalidChar() {
        byte[] buffer = " 0785 \0".getBytes();
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    @Test
    public void testParseOctalOrBinaryPositiveBinarySmall() {
        byte[] buffer = new byte[8];
        TarUtils.formatLongOctalOrBinaryBytes(0x010203040506L, buffer, 0, 8);
        long val = TarUtils.parseOctalOrBinary(buffer, 0, 8);
        Assert.assertEquals(0x010203040506L, val);
    }

    @Test
    public void testParseOctalOrBinaryNegativeBinarySmall() {
        byte[] buffer = new byte[8];
        TarUtils.formatLongOctalOrBinaryBytes(-12345L, buffer, 0, 8);
        long val = TarUtils.parseOctalOrBinary(buffer, 0, 8);
        Assert.assertEquals(-12345L, val);
    }

    @Test
    public void testParseOctalOrBinaryPositiveBinaryLarge() {
        byte[] buffer = new byte[12];
        TarUtils.formatLongOctalOrBinaryBytes(0x0102030405060708L, buffer, 0, 12);
        long val = TarUtils.parseOctalOrBinary(buffer, 0, 12);
        Assert.assertEquals(0x0102030405060708L, val);
    }

    @Test
    public void testParseOctalOrBinaryNegativeBinaryLarge() {
        byte[] buffer = new byte[12];
        TarUtils.formatLongOctalOrBinaryBytes(-0x0102030405060708L, buffer, 0, 12);
        long val = TarUtils.parseOctalOrBinary(buffer, 0, 12);
        Assert.assertEquals(-0x0102030405060708L, val);
    }

    @Test
    public void testParseOctalOrBinaryOctalFallback() {
        byte[] buffer = " 0755 \0".getBytes();
        long val = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        Assert.assertEquals(493L, val);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalOrBinaryOverflow() {
        byte[] buffer = new byte[12];
        buffer[0] = (byte) 0x80;
        buffer[1] = 0x7f;
        TarUtils.parseOctalOrBinary(buffer, 0, 12);
    }

    @Test
    public void testParseBoolean() {
        Assert.assertTrue(TarUtils.parseBoolean(new byte[]{1}, 0));
        Assert.assertFalse(TarUtils.parseBoolean(new byte[]{0}, 0));
        Assert.assertFalse(TarUtils.parseBoolean(new byte[]{2}, 0));
    }

    @Test
    public void testParseName() throws IOException {
        byte[] buffer = new byte[]{'t', 'e', 's', 't', 0, 'x', 'y', 'z'};
        String name = TarUtils.parseName(buffer, 0, buffer.length);
        Assert.assertEquals("test", name);

        byte[] emptyBuffer = new byte[]{0, 0, 0};
        Assert.assertEquals("", TarUtils.parseName(emptyBuffer, 0, 3));
    }

    @Test
    public void testFormatNameBytes() throws IOException {
        byte[] buffer = new byte[10];
        int offset = TarUtils.formatNameBytes("hello", buffer, 0, 10);
        Assert.assertEquals(10, offset);
        Assert.assertEquals("hello", new String(buffer, 0, 5));
        for (int i = 5; i < 10; i++) {
            Assert.assertEquals(0, buffer[i]);
        }

        byte[] truncated = new byte[3];
        TarUtils.formatNameBytes("testing", truncated, 0, 3);
        Assert.assertEquals("tes", new String(truncated, 0, 3));
    }

    @Test
    public void testFallbackEncoding() {
        ZipEncoding enc = TarUtils.FALLBACK_ENCODING;
        Assert.assertTrue(enc.canEncode("abc"));
        ByteBuffer b = enc.encode("abc");
        Assert.assertEquals(3, b.limit());
        Assert.assertEquals("abc", enc.decode(new byte[]{'a', 'b', 'c', 0, 'd'}));
    }

    @Test
    public void testFormatUnsignedOctalString() {
        byte[] buffer = new byte[7];
        TarUtils.formatUnsignedOctalString(0, buffer, 0, 7);
        Assert.assertEquals("0000000", new String(buffer));

        TarUtils.formatUnsignedOctalString( TarConstants.MAXID, buffer, 0, 7);
        Assert.assertEquals("7777777", new String(buffer));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatUnsignedOctalStringOverflow() {
        byte[] buffer = new byte[2];
        TarUtils.formatUnsignedOctalString(100, buffer, 0, 2);
    }

    @Test
    public void testFormatOctalBytes() {
        byte[] buffer = new byte[8];
        int res = TarUtils.formatOctalBytes(0755, buffer, 0, 8);
        Assert.assertEquals(8, res);
        Assert.assertEquals("0000755 ", new String(buffer, 0, 7));
        Assert.assertEquals(0, buffer[7]);
    }

    @Test
    public void testFormatLongOctalBytes() {
        byte[] buffer = new byte[8];
        int res = TarUtils.formatLongOctalBytes(0755, buffer, 0, 8);
        Assert.assertEquals(8, res);
        Assert.assertEquals("0000755 ", new String(buffer, 0, 8));
    }

    @Test
    public void testFormatCheckSumOctalBytes() {
        byte[] buffer = new byte[8];
        int res = TarUtils.formatCheckSumOctalBytes(01234, buffer, 0, 8);
        Assert.assertEquals(8, res);
        Assert.assertEquals("0001234\0 ", new String(buffer, 0, 8));
    }

    @Test
    public void testComputeCheckSum() {
        byte[] buffer = new byte[]{1, 2, 3, (byte) 255};
        long sum = TarUtils.computeCheckSum(buffer);
        Assert.assertEquals(1 + 2 + 3 + 255, sum);
    }

    @Test
    public void testVerifyCheckSum() {
        byte[] header = new byte[512];
        for (int i = 0; i < 512; i++) {
            header[i] = (byte) (i & 0x7F);
        }
        for (int i = TarConstants.CHKSUM_OFFSET; i < TarConstants.CHKSUM_OFFSET + TarConstants.CHKSUMLEN; i++) {
            header[i] = ' ';
        }
        long sum = TarUtils.computeCheckSum(header);
        TarUtils.formatCheckSumOctalBytes(sum, header, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUMLEN);

        Assert.assertTrue(TarUtils.verifyCheckSum(header));

        header[0] = (byte) (header[0] + 1);
        Assert.assertFalse(TarUtils.verifyCheckSum(header));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongBinaryOverflow() {
        byte[] buffer = new byte[4];
        TarUtils.formatLongOctalOrBinaryBytes(0xFFFFFFFFFEL, buffer, 0, 4);
    }
}
