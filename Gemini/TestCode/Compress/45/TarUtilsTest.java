package org.apache.commons.compress.archivers.tar;

import org.apache.commons.compress.archivers.zip.ZipEncoding;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class TarUtilsTest {

    @Test
    public void testParseOctal() {
        byte[] buffer = " 0755 \0".getBytes();
        long val = TarUtils.parseOctal(buffer, 0, buffer.length);
        Assert.assertEquals(493L, val);

        byte[] allZeros = new byte[]{0, 0, 0, 0};
        Assert.assertEquals(0L, TarUtils.parseOctal(allZeros, 0, allZeros.length));

        byte[] spaces = "     ".getBytes();
        Assert.assertEquals(0L, TarUtils.parseOctal(spaces, 0, spaces.length));

        byte[] leadingSpaces = "  123\0".getBytes();
        Assert.assertEquals(83L, TarUtils.parseOctal(leadingSpaces, 0, leadingSpaces.length));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalLengthLessThan2() {
        byte[] buffer = new byte[]{ '0' };
        TarUtils.parseOctal(buffer, 0, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalInvalidByte() {
        byte[] buffer = " 128 \0".getBytes();
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    @Test
    public void testParseOctalOrBinary() {
        byte[] octalBuf = " 0755 \0".getBytes();
        Assert.assertEquals(493L, TarUtils.parseOctalOrBinary(octalBuf, 0, octalBuf.length));

        byte[] posBinaryShort = new byte[]{(byte) 0x80, 0, 0, 1};
        Assert.assertEquals(1L, TarUtils.parseOctalOrBinary(posBinaryShort, 0, posBinaryShort.length));

        byte[] negBinaryShort = new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xfe};
        Assert.assertEquals(-2L, TarUtils.parseOctalOrBinary(negBinaryShort, 0, negBinaryShort.length));

        byte[] posBinaryLong = new byte[]{(byte) 0x80, 0, 0, 0, 0, 0, 0, 0, 0, 1};
        Assert.assertEquals(1L, TarUtils.parseOctalOrBinary(posBinaryLong, 0, posBinaryLong.length));

        byte[] negBinaryLong = new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xfe};
        Assert.assertEquals(-2L, TarUtils.parseOctalOrBinary(negBinaryLong, 0, negBinaryLong.length));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseBinaryLongOverflow() {
        byte[] buffer = new byte[]{(byte) 0x80, 0x7f, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
        TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
    }

    @Test
    public void testParseBoolean() {
        byte[] buffer = new byte[]{1, 0};
        Assert.assertTrue(TarUtils.parseBoolean(buffer, 0));
        Assert.assertFalse(TarUtils.parseBoolean(buffer, 1));
    }

    @Test
    public void testParseName() throws IOException {
        byte[] buffer = "hello\0world".getBytes();
        Assert.assertEquals("hello", TarUtils.parseName(buffer, 0, 5));
        Assert.assertEquals("hello", TarUtils.parseName(buffer, 0, buffer.length));

        byte[] empty = new byte[]{0, 0, 0};
        Assert.assertEquals("", TarUtils.parseName(empty, 0, 3));
        Assert.assertEquals("", TarUtils.parseName(empty, 0, 3, TarUtils.DEFAULT_ENCODING));
    }

    @Test
    public void testFallbackEncoding() {
        ZipEncoding enc = TarUtils.FALLBACK_ENCODING;
        Assert.assertTrue(enc.canEncode("test"));
        ByteBuffer bb = enc.encode("test");
        Assert.assertEquals(4, bb.limit());
        Assert.assertEquals("test", enc.decode(new byte[]{'t', 'e', 's', 't', 0, 'x'}));
    }

    @Test
    public void testFormatNameBytes() throws IOException {
        byte[] buf = new byte[10];
        int res = TarUtils.formatNameBytes("foo", buf, 0, 5);
        Assert.assertEquals(5, res);
        Assert.assertEquals('f', buf[0]);
        Assert.assertEquals('o', buf[1]);
        Assert.assertEquals('o', buf[2]);
        Assert.assertEquals(0, buf[3]);
        Assert.assertEquals(0, buf[4]);

        byte[] bufTruncated = new byte[3];
        TarUtils.formatNameBytes("foobar", bufTruncated, 0, 3);
        Assert.assertEquals('f', bufTruncated[0]);
        Assert.assertEquals('o', bufTruncated[1]);
        Assert.assertEquals('o', bufTruncated[2]);

        byte[] bufCustom = new byte[5];
        TarUtils.formatNameBytes("ab", bufCustom, 0, 5, TarUtils.DEFAULT_ENCODING);
        Assert.assertEquals('a', bufCustom[0]);
        Assert.assertEquals('b', bufCustom[1]);
        Assert.assertEquals(0, bufCustom[2]);
    }

    @Test
    public void testFormatUnsignedOctalString() {
        byte[] buf = new byte[6];
        TarUtils.formatUnsignedOctalString(0, buf, 0, 6);
        Assert.assertEquals("000000", new String(buf));

        TarUtils.formatUnsignedOctalString(7, buf, 0, 6);
        Assert.assertEquals("000007", new String(buf));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatUnsignedOctalStringOverflow() {
        byte[] buf = new byte[2];
        TarUtils.formatUnsignedOctalString(64, buf, 0, 2);
    }

    @Test
    public void testFormatOctalBytes() {
        byte[] buf = new byte[8];
        int end = TarUtils.formatOctalBytes(0755, buf, 0, 8);
        Assert.assertEquals(8, end);
        Assert.assertEquals("0000755 \0", new String(buf));
    }

    @Test
    public void testFormatLongOctalBytes() {
        byte[] buf = new byte[8];
        int end = TarUtils.formatLongOctalBytes(0755, buf, 0, 8);
        Assert.assertEquals(8, end);
        Assert.assertEquals("0000755 ", new String(buf));
    }

    @Test
    public void testFormatLongOctalOrBinaryBytes() {
        byte[] buf = new byte[8];
        int end = TarUtils.formatLongOctalOrBinaryBytes(0755, buf, 0, 8);
        Assert.assertEquals(8, end);
        Assert.assertEquals("0000755 ", new String(buf));

        byte[] bigBuf = new byte[8];
        TarUtils.formatLongOctalOrBinaryBytes(TarConstants.MAXID + 1L, bigBuf, 0, 8);
        Assert.assertEquals((byte) 0x80, bigBuf[0]);
        Assert.assertEquals(TarConstants.MAXID + 1L, TarUtils.parseOctalOrBinary(bigBuf, 0, 8));

        byte[] negBuf = new byte[8];
        TarUtils.formatLongOctalOrBinaryBytes(-1L, negBuf, 0, 8);
        Assert.assertEquals((byte) 0xff, negBuf[0]);
        Assert.assertEquals(-1L, TarUtils.parseOctalOrBinary(negBuf, 0, 8));

        byte[] largeBuf = new byte[12];
        TarUtils.formatLongOctalOrBinaryBytes(0x10000000000L, largeBuf, 0, 12);
        Assert.assertEquals((byte) 0x80, largeBuf[0]);
        Assert.assertEquals(0x10000000000L, TarUtils.parseOctalOrBinary(largeBuf, 0, 12));

        byte[] negLargeBuf = new byte[12];
        TarUtils.formatLongOctalOrBinaryBytes(-0x10000000000L, negLargeBuf, 0, 12);
        Assert.assertEquals((byte) 0xff, negLargeBuf[0]);
        Assert.assertEquals(-0x10000000000L, TarUtils.parseOctalOrBinary(negLargeBuf, 0, 12));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongBinaryOverflow() {
        byte[] buf = new byte[2];
        TarUtils.formatLongOctalOrBinaryBytes(0x10000, buf, 0, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatBigIntegerBinaryOverflow() {
        byte[] buf = new byte[9];
        TarUtils.formatLongOctalOrBinaryBytes(Long.MAX_VALUE, buf, 0, 8);
    }

    @Test
    public void testFormatCheckSumOctalBytes() {
        byte[] buf = new byte[8];
        int end = TarUtils.formatCheckSumOctalBytes(0755, buf, 0, 8);
        Assert.assertEquals(8, end);
        Assert.assertEquals("0000755\0 ", new String(buf));
    }

    @Test
    public void testComputeCheckSum() {
        byte[] buf = new byte[]{1, 2, 3, 4};
        Assert.assertEquals(10L, TarUtils.computeCheckSum(buf));
    }

    @Test
    public void testVerifyCheckSum() {
        byte[] header = new byte[512];
        Arrays.fill(header, (byte) 0);
        for (int i = 0; i < 100; i++) {
            header[i] = (byte) ('a' + (i % 26));
        }

        long unsignedSum = 0;
        for (int i = 0; i < header.length; i++) {
            byte b = header[i];
            if (TarConstants.CHKSUM_OFFSET <= i && i < TarConstants.CHKSUM_OFFSET + TarConstants.CHKSUMLEN) {
                b = ' ';
            }
            unsignedSum += 0xff & b;
        }

        TarUtils.formatCheckSumOctalBytes(unsignedSum, header, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUMLEN);
        Assert.assertTrue(TarUtils.verifyCheckSum(header));

        header[0] ^= 0x01;
        Assert.assertFalse(TarUtils.verifyCheckSum(header));

        Arrays.fill(header, (byte) -5);
        long signedSum = 0;
        for (int i = 0; i < header.length; i++) {
            byte b = header[i];
            if (TarConstants.CHKSUM_OFFSET <= i && i < TarConstants.CHKSUM_OFFSET + TarConstants.CHKSUMLEN) {
                b = ' ';
            }
            signedSum += b;
        }
        TarUtils.formatCheckSumOctalBytes(signedSum, header, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUMLEN);
        Assert.assertTrue(TarUtils.verifyCheckSum(header));
    }
}
