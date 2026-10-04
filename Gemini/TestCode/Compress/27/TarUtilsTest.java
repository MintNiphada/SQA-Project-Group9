package org.apache.commons.compress.archivers.tar;

import org.apache.commons.compress.archivers.zip.ZipEncoding;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class TarUtilsTest {

    @Test
    public void testParseOctalValidValues() {
        byte[] buffer = "000123 \0".getBytes();
        long val = TarUtils.parseOctal(buffer, 0, buffer.length);
        Assert.assertEquals(0123L, val);

        buffer = "   777\0".getBytes();
        val = TarUtils.parseOctal(buffer, 0, buffer.length);
        Assert.assertEquals(0777L, val);

        buffer = "123 ".getBytes();
        val = TarUtils.parseOctal(buffer, 0, buffer.length);
        Assert.assertEquals(0123L, val);

        buffer = "0 ".getBytes();
        val = TarUtils.parseOctal(buffer, 0, buffer.length);
        Assert.assertEquals(0L, val);

        byte[] allZeros = new byte[8];
        val = TarUtils.parseOctal(allZeros, 0, allZeros.length);
        Assert.assertEquals(0L, val);

        byte[] leadingNull = new byte[] {0, '1', '2', ' '};
        val = TarUtils.parseOctal(leadingNull, 0, leadingNull.length);
        Assert.assertEquals(0L, val);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalTooShort() {
        TarUtils.parseOctal(new byte[]{ '0' }, 0, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalAllSpaces() {
        byte[] buffer = "    ".getBytes();
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalInvalidChar() {
        byte[] buffer = "018 \0".getBytes();
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalNonOctalByte() {
        byte[] buffer = "01a \0".getBytes();
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    @Test
    public void testParseOctalOrBinary() {
        byte[] buffer = "0000755 \0".getBytes();
        long val = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        Assert.assertEquals(0755L, val);

        // Binary positive small length < 9
        byte[] binPositive = new byte[] {(byte) 0x80, 0x00, 0x01, 0x02};
        val = TarUtils.parseOctalOrBinary(binPositive, 0, binPositive.length);
        Assert.assertEquals(0x0102L, val);

        // Binary negative small length < 9
        byte[] binNegative = new byte[] {(byte) 0xff, (byte) 0xff, (byte) 0xfe};
        val = TarUtils.parseOctalOrBinary(binNegative, 0, binNegative.length);
        Assert.assertEquals(-2L, val);

        // Binary positive 9-byte or larger (length = 9)
        byte[] bin9Pos = new byte[9];
        bin9Pos[0] = (byte) 0x80;
        bin9Pos[8] = 0x05;
        val = TarUtils.parseOctalOrBinary(bin9Pos, 0, bin9Pos.length);
        Assert.assertEquals(5L, val);

        // Binary negative length >= 9 (12 bytes)
        byte[] bin12Neg = new byte[12];
        Arrays.fill(bin12Neg, (byte) 0xff);
        bin12Neg[11] = (byte) 0xfa; // -6 in 2's complement
        val = TarUtils.parseOctalOrBinary(bin12Neg, 0, bin12Neg.length);
        Assert.assertEquals(-6L, val);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseBinaryLongTooLarge() {
        byte[] buffer = new byte[10];
        buffer[0] = (byte) 0x80;
        TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseBinaryBigIntegerOverflow() {
        byte[] buffer = new byte[12];
        buffer[0] = (byte) 0x80;
        buffer[1] = (byte) 0x80; // sets bit 64 or higher
        TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
    }

    @Test
    public void testParseBoolean() {
        byte[] buffer = new byte[] {0, 1, 2};
        Assert.assertFalse(TarUtils.parseBoolean(buffer, 0));
        Assert.assertTrue(TarUtils.parseBoolean(buffer, 1));
        Assert.assertFalse(TarUtils.parseBoolean(buffer, 2));
    }

    @Test
    public void testParseName() throws IOException {
        byte[] buffer = "hello\0world".getBytes();
        String name = TarUtils.parseName(buffer, 0, buffer.length);
        Assert.assertEquals("hello", name);

        buffer = "nonull".getBytes();
        name = TarUtils.parseName(buffer, 0, buffer.length);
        Assert.assertEquals("nonull", name);

        buffer = new byte[10];
        name = TarUtils.parseName(buffer, 0, buffer.length);
        Assert.assertEquals("", name);
    }

    @Test
    public void testParseNameWithEncoding() throws IOException {
        byte[] buffer = "test\0extra".getBytes();
        String name = TarUtils.parseName(buffer, 0, buffer.length, TarUtils.DEFAULT_ENCODING);
        Assert.assertEquals("test", name);

        name = TarUtils.parseName(buffer, 0, buffer.length, TarUtils.FALLBACK_ENCODING);
        Assert.assertEquals("test", name);

        byte[] zeros = new byte[5];
        name = TarUtils.parseName(zeros, 0, zeros.length, TarUtils.FALLBACK_ENCODING);
        Assert.assertEquals("", name);
    }

    @Test
    public void testFormatNameBytes() throws IOException {
        byte[] buffer = new byte[10];
        int off = TarUtils.formatNameBytes("hello", buffer, 0, 10);
        Assert.assertEquals(10, off);
        Assert.assertEquals("hello\0\0\0\0\0", new String(buffer));

        Arrays.fill(buffer, (byte) 0);
        off = TarUtils.formatNameBytes("verylongnamehere", buffer, 0, 5);
        Assert.assertEquals(5, off);
        Assert.assertEquals("veryl", new String(buffer, 0, 5));

        Arrays.fill(buffer, (byte) 0);
        off = TarUtils.formatNameBytes("test", buffer, 2, 6, TarUtils.DEFAULT_ENCODING);
        Assert.assertEquals(8, off);
        Assert.assertEquals("test\0\0", new String(buffer, 2, 6));
    }

    @Test
    public void testFormatUnsignedOctalString() {
        byte[] buffer = new byte[6];
        TarUtils.formatUnsignedOctalString(0, buffer, 0, 6);
        Assert.assertEquals("000000", new String(buffer));

        TarUtils.formatUnsignedOctalString(0123, buffer, 0, 6);
        Assert.assertEquals("000123", new String(buffer));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatUnsignedOctalStringOverflow() {
        byte[] buffer = new byte[2];
        TarUtils.formatUnsignedOctalString(01000, buffer, 0, 2);
    }

    @Test
    public void testFormatOctalBytes() {
        byte[] buffer = new byte[8];
        int off = TarUtils.formatOctalBytes(0123, buffer, 0, 8);
        Assert.assertEquals(8, off);
        Assert.assertEquals(' ', buffer[6]);
        Assert.assertEquals(0, buffer[7]);
        Assert.assertEquals("000123 \0", new String(buffer));
    }

    @Test
    public void testFormatLongOctalBytes() {
        byte[] buffer = new byte[8];
        int off = TarUtils.formatLongOctalBytes(0123, buffer, 0, 8);
        Assert.assertEquals(8, off);
        Assert.assertEquals(' ', buffer[7]);
        Assert.assertEquals("0000123 ", new String(buffer));
    }

    @Test
    public void testFormatLongOctalOrBinaryBytes() {
        byte[] buffer = new byte[12];
        int off = TarUtils.formatLongOctalOrBinaryBytes(0123, buffer, 0, 12);
        Assert.assertEquals(12, off);
        Assert.assertEquals("00000000123 ", new String(buffer));

        // Binary positive (size > MAXSIZE)
        Arrays.fill(buffer, (byte) 0);
        long bigValue = TarConstants.MAXSIZE + 10L;
        off = TarUtils.formatLongOctalOrBinaryBytes(bigValue, buffer, 0, 12);
        Assert.assertEquals(12, off);
        long parsed = TarUtils.parseOctalOrBinary(buffer, 0, 12);
        Assert.assertEquals(bigValue, parsed);

        // Binary negative
        Arrays.fill(buffer, (byte) 0);
        long negValue = -55555L;
        off = TarUtils.formatLongOctalOrBinaryBytes(negValue, buffer, 0, 12);
        Assert.assertEquals(12, off);
        parsed = TarUtils.parseOctalOrBinary(buffer, 0, 12);
        Assert.assertEquals(negValue, parsed);

        // Small field < 9 length binary
        byte[] smallBuffer = new byte[8];
        long val = 0x10000000000L;
        off = TarUtils.formatLongOctalOrBinaryBytes(val, smallBuffer, 0, 8);
        Assert.assertEquals(8, off);
        parsed = TarUtils.parseOctalOrBinary(smallBuffer, 0, 8);
        Assert.assertEquals(val, parsed);

        // Small field negative binary
        Arrays.fill(smallBuffer, (byte) 0);
        long smallNeg = -123456L;
        off = TarUtils.formatLongOctalOrBinaryBytes(smallNeg, smallBuffer, 0, 8);
        Assert.assertEquals(8, off);
        parsed = TarUtils.parseOctalOrBinary(smallBuffer, 0, 8);
        Assert.assertEquals(smallNeg, parsed);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongBinaryTooLarge() {
        byte[] buffer = new byte[4];
        TarUtils.formatLongOctalOrBinaryBytes(0xFFFFFFFFFL, buffer, 0, 4);
    }

    @Test
    public void testFormatCheckSumOctalBytes() {
        byte[] buffer = new byte[8];
        int off = TarUtils.formatCheckSumOctalBytes(0123, buffer, 0, 8);
        Assert.assertEquals(8, off);
        Assert.assertEquals(0, buffer[6]);
        Assert.assertEquals((byte) ' ', buffer[7]);
        Assert.assertEquals("000123\0 ", new String(buffer));
    }

    @Test
    public void testComputeCheckSum() {
        byte[] buffer = new byte[] {1, 2, 3, (byte) 0xFF};
        long sum = TarUtils.computeCheckSum(buffer);
        Assert.assertEquals(1 + 2 + 3 + 255, sum);
    }

    @Test
    public void testVerifyCheckSum() {
        byte[] header = new byte[512];
        for (int i = 0; i < 512; i++) {
            header[i] = (byte) (i & 0x7F);
        }

        // Format valid checksum
        long sum = 0;
        for (int i = 0; i < 512; i++) {
            if (i >= TarConstants.CHKSUM_OFFSET && i < TarConstants.CHKSUM_OFFSET + TarConstants.CHKSUMLEN) {
                sum += ' ';
            } else {
                sum += (0xFF & header[i]);
            }
        }
        TarUtils.formatCheckSumOctalBytes(sum, header, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUMLEN);

        Assert.assertTrue(TarUtils.verifyCheckSum(header));

        // Invalid checksum - corrupted
        byte orig = header[TarConstants.CHKSUM_OFFSET];
        header[TarConstants.CHKSUM_OFFSET] = '0';
        header[TarConstants.CHKSUM_OFFSET + 1] = '0';
        header[TarConstants.CHKSUM_OFFSET + 2] = '0';
        header[TarConstants.CHKSUM_OFFSET + 3] = '0';
        header[TarConstants.CHKSUM_OFFSET + 4] = '0';
        header[TarConstants.CHKSUM_OFFSET + 5] = '1';
        header[TarConstants.CHKSUM_OFFSET + 6] = 0;
        header[TarConstants.CHKSUM_OFFSET + 7] = ' ';
        Assert.assertFalse(TarUtils.verifyCheckSum(header));

        // Heuristic: stored checksum > unsignedSum
        header[TarConstants.CHKSUM_OFFSET] = '7';
        header[TarConstants.CHKSUM_OFFSET + 1] = '7';
        header[TarConstants.CHKSUM_OFFSET + 2] = '7';
        header[TarConstants.CHKSUM_OFFSET + 3] = '7';
        header[TarConstants.CHKSUM_OFFSET + 4] = '7';
        header[TarConstants.CHKSUM_OFFSET + 5] = '7';
        Assert.assertTrue(TarUtils.verifyCheckSum(header));
    }

    @Test
    public void testFallbackEncoding() {
        ZipEncoding enc = TarUtils.FALLBACK_ENCODING;
        Assert.assertTrue(enc.canEncode("test"));

        ByteBuffer buf = enc.encode("abc");
        Assert.assertEquals(3, buf.remaining());
        Assert.assertEquals((byte) 'a', buf.get());
        Assert.assertEquals((byte) 'b', buf.get());
        Assert.assertEquals((byte) 'c', buf.get());

        byte[] raw = new byte[] {(byte) 'x', (byte) 'y', 0, (byte) 'z'};
        String decoded = enc.decode(raw);
        Assert.assertEquals("xy", decoded);
    }
}
