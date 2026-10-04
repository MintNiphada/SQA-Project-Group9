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
        byte[] buffer = " 0755 \0".getBytes();
        long val = TarUtils.parseOctal(buffer, 0, buffer.length);
        Assert.assertEquals(0755L, val);

        byte[] bufferAllZeros = new byte[]{0, 0, 0};
        Assert.assertEquals(0L, TarUtils.parseOctal(bufferAllZeros, 0, bufferAllZeros.length));

        byte[] bufferLeadingZero = new byte[]{0, '1', '2', ' ', 0};
        Assert.assertEquals(0L, TarUtils.parseOctal(bufferLeadingZero, 0, bufferLeadingZero.length));

        byte[] bufferEmbeddedNull = new byte[]{'1', '2', 0, '5', ' '};
        Assert.assertEquals(10L, TarUtils.parseOctal(bufferEmbeddedNull, 0, bufferEmbeddedNull.length));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalTooShort() {
        byte[] buffer = new byte[]{'0'};
        TarUtils.parseOctal(buffer, 0, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalInvalidByte() {
        byte[] buffer = " 0785 ".getBytes();
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalInvalidByteLow() {
        byte[] buffer = " 0/55 ".getBytes();
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    @Test
    public void testParseOctalOrBinaryOctal() {
        byte[] buffer = " 0755 \0".getBytes();
        Assert.assertEquals(0755L, TarUtils.parseOctalOrBinary(buffer, 0, buffer.length));
    }

    @Test
    public void testParseOctalOrBinaryPositiveBinarySmall() {
        byte[] buffer = new byte[8];
        buffer[0] = (byte) 0x80;
        buffer[6] = 1;
        buffer[7] = 2;
        long val = TarUtils.parseOctalOrBinary(buffer, 0, 8);
        Assert.assertEquals(258L, val);
    }

    @Test
    public void testParseOctalOrBinaryNegativeBinarySmall() {
        byte[] buffer = new byte[8];
        TarUtils.formatLongOctalOrBinaryBytes(-100L, buffer, 0, 8);
        long val = TarUtils.parseOctalOrBinary(buffer, 0, 8);
        Assert.assertEquals(-100L, val);
    }

    @Test
    public void testParseOctalOrBinaryPositiveBinaryBigInteger() {
        byte[] buffer = new byte[12];
        TarUtils.formatLongOctalOrBinaryBytes(0x1ffffffffL, buffer, 0, 12);
        long val = TarUtils.parseOctalOrBinary(buffer, 0, 12);
        Assert.assertEquals(0x1ffffffffL, val);
    }

    @Test
    public void testParseOctalOrBinaryNegativeBinaryBigInteger() {
        byte[] buffer = new byte[12];
        TarUtils.formatLongOctalOrBinaryBytes(-1234567890123L, buffer, 0, 12);
        long val = TarUtils.parseOctalOrBinary(buffer, 0, 12);
        Assert.assertEquals(-1234567890123L, val);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseBinaryBigIntegerOverflow() {
        byte[] buffer = new byte[12];
        buffer[0] = (byte) 0x80;
        Arrays.fill(buffer, 1, 12, (byte) 0xFF);
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
    public void testParseAndFormatName() throws IOException {
        byte[] buffer = new byte[20];
        int nextOffset = TarUtils.formatNameBytes("hello_world", buffer, 0, 20);
        Assert.assertEquals(20, nextOffset);
        String name = TarUtils.parseName(buffer, 0, 20);
        Assert.assertEquals("hello_world", name);

        String empty = TarUtils.parseName(new byte[10], 0, 10);
        Assert.assertEquals("", empty);
    }

    @Test
    public void testFormatNameBytesTruncation() {
        byte[] buffer = new byte[5];
        TarUtils.formatNameBytes("123456789", buffer, 0, 5);
        String name = TarUtils.parseName(buffer, 0, 5);
        Assert.assertEquals("12345", name);
    }

    @Test
    public void testFallbackEncoding() {
        ZipEncoding enc = TarUtils.FALLBACK_ENCODING;
        Assert.assertTrue(enc.canEncode("test"));
        ByteBuffer bb = enc.encode("test\0rest");
        Assert.assertEquals(9, bb.limit());
        String decoded = enc.decode(bb.array());
        Assert.assertEquals("test", decoded);

        byte[] signedBytes = new byte[]{(byte) 0x80, (byte) 0xFF, 0};
        String decodedSigned = enc.decode(signedBytes);
        Assert.assertEquals(2, decodedSigned.length());
        Assert.assertEquals(128, (int) decodedSigned.charAt(0));
        Assert.assertEquals(255, (int) decodedSigned.charAt(1));
    }

    @Test
    public void testFormatUnsignedOctalString() {
        byte[] buffer = new byte[6];
        TarUtils.formatUnsignedOctalString(0L, buffer, 0, 6);
        Assert.assertEquals("000000", new String(buffer));

        TarUtils.formatUnsignedOctalString(755L, buffer, 0, 6);
        Assert.assertEquals("001363", new String(buffer));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatUnsignedOctalStringOverflow() {
        byte[] buffer = new byte[2];
        TarUtils.formatUnsignedOctalString(100L, buffer, 0, 2);
    }

    @Test
    public void testFormatOctalBytes() {
        byte[] buffer = new byte[8];
        int res = TarUtils.formatOctalBytes(0755L, buffer, 0, 8);
        Assert.assertEquals(8, res);
        Assert.assertEquals("0000755 ", new String(buffer, 0, 7));
        Assert.assertEquals(0, buffer[7]);
    }

    @Test
    public void testFormatLongOctalBytes() {
        byte[] buffer = new byte[8];
        int res = TarUtils.formatLongOctalBytes(0755L, buffer, 0, 8);
        Assert.assertEquals(8, res);
        Assert.assertEquals("0000755 ", new String(buffer));
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesWithinOctalLimit() {
        byte[] buffer = new byte[8];
        int res = TarUtils.formatLongOctalOrBinaryBytes(TarConstants.MAXID, buffer, 0, TarConstants.UIDLEN);
        Assert.assertEquals(8, res);
        Assert.assertEquals(0, buffer[0] & 0x80);

        byte[] bufferSize = new byte[12];
        res = TarUtils.formatLongOctalOrBinaryBytes(TarConstants.MAXSIZE, bufferSize, 0, TarConstants.SIZELEN);
        Assert.assertEquals(12, res);
        Assert.assertEquals(0, bufferSize[0] & 0x80);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesBinarySmall() {
        byte[] buffer = new byte[8];
        long val = TarConstants.MAXID + 10;
        int res = TarUtils.formatLongOctalOrBinaryBytes(val, buffer, 0, 8);
        Assert.assertEquals(8, res);
        Assert.assertEquals((byte) 0x80, buffer[0]);
        Assert.assertEquals(val, TarUtils.parseOctalOrBinary(buffer, 0, 8));

        res = TarUtils.formatLongOctalOrBinaryBytes(-15L, buffer, 0, 8);
        Assert.assertEquals(8, res);
        Assert.assertEquals((byte) 0xff, buffer[0]);
        Assert.assertEquals(-15L, TarUtils.parseOctalOrBinary(buffer, 0, 8));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongBinaryOverflow() {
        byte[] buffer = new byte[2];
        TarUtils.formatLongOctalOrBinaryBytes(10000L, buffer, 0, 2);
    }

    @Test
    public void testFormatCheckSumOctalBytes() {
        byte[] buffer = new byte[8];
        int res = TarUtils.formatCheckSumOctalBytes(0755L, buffer, 0, 8);
        Assert.assertEquals(8, res);
        Assert.assertEquals("000755\0 ", new String(buffer));
    }

    @Test
    public void testComputeCheckSum() {
        byte[] buffer = new byte[]{1, 2, (byte) 255};
        long sum = TarUtils.computeCheckSum(buffer);
        Assert.assertEquals(1 + 2 + 255, sum);
    }

    @Test
    public void testVerifyCheckSum() {
        byte[] header = new byte[512];
        Arrays.fill(header, (byte) 'a');
        TarUtils.formatCheckSumOctalBytes(0, header, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUMLEN);
        long unsignedSum = 0;
        for (int i = 0; i < 512; i++) {
            if (i >= TarConstants.CHKSUM_OFFSET && i < TarConstants.CHKSUM_OFFSET + TarConstants.CHKSUMLEN) {
                unsignedSum += ' ';
            } else {
                unsignedSum += (header[i] & 0xff);
            }
        }
        TarUtils.formatCheckSumOctalBytes(unsignedSum, header, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUMLEN);
        Assert.assertTrue(TarUtils.verifyCheckSum(header));

        header[TarConstants.CHKSUM_OFFSET] = '9';
        header[TarConstants.CHKSUM_OFFSET + 1] = '0';
        header[TarConstants.CHKSUM_OFFSET + 2] = '0';
        Assert.assertFalse(TarUtils.verifyCheckSum(header));
    }

    @Test
    public void testVerifyCheckSumGreaterOrSigned() {
        byte[] header = new byte[512];
        TarUtils.formatCheckSumOctalBytes(999999L, header, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUMLEN);
        Assert.assertTrue(TarUtils.verifyCheckSum(header));
    }

    @Test
    public void testVerifyCheckSumDigitLimitBranch() {
        byte[] header = new byte[512];
        Arrays.fill(header, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUM_OFFSET + 8, (byte) '0');
        header[TarConstants.CHKSUM_OFFSET + 1] = ' ';
        header[TarConstants.CHKSUM_OFFSET + 2] = '7';
        Assert.assertTrue(TarUtils.verifyCheckSum(header));
    }
}
