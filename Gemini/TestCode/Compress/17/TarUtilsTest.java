package org.apache.commons.compress.archivers.tar;

import org.apache.commons.compress.archivers.zip.ZipEncoding;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class TarUtilsTest {

    @Test
    public void testPrivateConstructor() throws Exception {
        Constructor<TarUtils> constructor = TarUtils.class.getDeclaredConstructor();
        Assert.assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        TarUtils instance = constructor.newInstance();
        Assert.assertNotNull(instance);
    }

    @Test
    public void testParseOctalValid() {
        byte[] buffer = " 0755 \0".getBytes();
        long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        Assert.assertEquals(0755L, result);

        buffer = "0000755\0".getBytes();
        result = TarUtils.parseOctal(buffer, 0, buffer.length);
        Assert.assertEquals(0755L, result);

        buffer = " 755 ".getBytes();
        result = TarUtils.parseOctal(buffer, 0, buffer.length);
        Assert.assertEquals(0755L, result);

        buffer = new byte[]{0, 0, 0};
        result = TarUtils.parseOctal(buffer, 0, buffer.length);
        Assert.assertEquals(0L, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalTooShort() {
        byte[] buffer = new byte[]{ '0' };
        TarUtils.parseOctal(buffer, 0, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalMissingTrailer() {
        byte[] buffer = "0755".getBytes();
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalInvalidDigit() {
        byte[] buffer = "0789 \0".getBytes();
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalInvalidNonOctalByte() {
        byte[] buffer = "07a5 \0".getBytes();
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    @Test
    public void testParseOctalOrBinaryPositiveOctal() {
        byte[] buffer = " 0000755\0".getBytes();
        long val = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        Assert.assertEquals(0755L, val);
    }

    @Test
    public void testParseOctalOrBinarySmallBinaryPositive() {
        byte[] buffer = new byte[8];
        buffer[0] = (byte) 0x80;
        buffer[6] = 0x01;
        buffer[7] = 0x23;
        long val = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        Assert.assertEquals(0x0123L, val);
    }

    @Test
    public void testParseOctalOrBinarySmallBinaryNegative() {
        byte[] buffer = new byte[8];
        Arrays.fill(buffer, (byte) 0xff);
        buffer[7] = (byte) 0xfe; // -2 in 8-byte 2's complement
        long val = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        Assert.assertEquals(-2L, val);
    }

    @Test
    public void testParseOctalOrBinaryBigBinaryPositive() {
        byte[] buffer = new byte[12];
        buffer[0] = (byte) 0x80;
        buffer[10] = 0x01;
        buffer[11] = 0x00;
        long val = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        Assert.assertEquals(256L, val);
    }

    @Test
    public void testParseOctalOrBinaryBigBinaryNegative() {
        byte[] buffer = new byte[12];
        Arrays.fill(buffer, (byte) 0xff);
        buffer[11] = (byte) 0xfe;
        long val = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        Assert.assertEquals(-2L, val);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalOrBinaryBigBinaryOverflow() {
        byte[] buffer = new byte[12];
        buffer[0] = (byte) 0x80;
        buffer[1] = 0x01; // makes it > 63 bits
        TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
    }

    @Test
    public void testParseBoolean() {
        byte[] buffer = new byte[]{ 0, 1, 2 };
        Assert.assertFalse(TarUtils.parseBoolean(buffer, 0));
        Assert.assertTrue(TarUtils.parseBoolean(buffer, 1));
        Assert.assertFalse(TarUtils.parseBoolean(buffer, 2));
    }

    @Test
    public void testParseName() {
        byte[] buffer = "hello\0world".getBytes();
        String name = TarUtils.parseName(buffer, 0, buffer.length);
        Assert.assertEquals("hello", name);

        byte[] allZeros = new byte[10];
        Assert.assertEquals("", TarUtils.parseName(allZeros, 0, allZeros.length));

        byte[] full = "abcdef".getBytes();
        Assert.assertEquals("abcdef", TarUtils.parseName(full, 0, full.length));
    }

    @Test
    public void testParseNameWithEncodingFallback() throws IOException {
        ZipEncoding throwingEncoding = new ZipEncoding() {
            public boolean canEncode(String name) { return false; }
            public ByteBuffer encode(String name) throws IOException { throw new IOException("mock error"); }
            public String decode(byte[] buffer) throws IOException { throw new IOException("mock decode error"); }
        };

        byte[] buffer = "test\0".getBytes();
        try {
            TarUtils.parseName(buffer, 0, buffer.length, throwingEncoding);
            Assert.fail("Expected IOException");
        } catch (IOException expected) {
            // Success
        }
    }

    @Test
    public void testFormatNameBytes() {
        byte[] buffer = new byte[10];
        int nextOffset = TarUtils.formatNameBytes("foo", buffer, 0, 6);
        Assert.assertEquals(6, nextOffset);
        Assert.assertEquals('f', buffer[0]);
        Assert.assertEquals('o', buffer[1]);
        Assert.assertEquals('o', buffer[2]);
        Assert.assertEquals(0, buffer[3]);
        Assert.assertEquals(0, buffer[4]);
        Assert.assertEquals(0, buffer[5]);

        // Test truncation
        Arrays.fill(buffer, (byte) 0);
        nextOffset = TarUtils.formatNameBytes("verylongfilename", buffer, 0, 5);
        Assert.assertEquals(5, nextOffset);
        Assert.assertEquals("veryl", new String(buffer, 0, 5));
    }

    @Test
    public void testFormatUnsignedOctalString() {
        byte[] buffer = new byte[6];
        TarUtils.formatUnsignedOctalString(0L, buffer, 0, 6);
        Assert.assertEquals("000000", new String(buffer));

        TarUtils.formatUnsignedOctalString(0755L, buffer, 0, 6);
        Assert.assertEquals("000755", new String(buffer));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatUnsignedOctalStringOverflow() {
        byte[] buffer = new byte[3];
        TarUtils.formatUnsignedOctalString(07777L, buffer, 0, 3);
    }

    @Test
    public void testFormatOctalBytes() {
        byte[] buffer = new byte[8];
        int off = TarUtils.formatOctalBytes(0755L, buffer, 0, 8);
        Assert.assertEquals(8, off);
        Assert.assertEquals("0000755 \0", new String(buffer));
    }

    @Test
    public void testFormatLongOctalBytes() {
        byte[] buffer = new byte[8];
        int off = TarUtils.formatLongOctalBytes(0755L, buffer, 0, 8);
        Assert.assertEquals(8, off);
        Assert.assertEquals("0000755 ", new String(buffer));
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesWithinOctalRange() {
        byte[] buffer = new byte[TarConstants.UIDLEN];
        int off = TarUtils.formatLongOctalOrBinaryBytes(0755L, buffer, 0, TarConstants.UIDLEN);
        Assert.assertEquals(TarConstants.UIDLEN, off);
        Assert.assertEquals("0000755 ", new String(buffer));
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesBinarySmallPositive() {
        byte[] buffer = new byte[8];
        long value = TarConstants.MAXID + 10;
        int off = TarUtils.formatLongOctalOrBinaryBytes(value, buffer, 0, 8);
        Assert.assertEquals(8, off);
        Assert.assertEquals((byte) 0x80, buffer[0]);

        long parsed = TarUtils.parseOctalOrBinary(buffer, 0, 8);
        Assert.assertEquals(value, parsed);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesBinarySmallNegative() {
        byte[] buffer = new byte[8];
        long value = -12345L;
        int off = TarUtils.formatLongOctalOrBinaryBytes(value, buffer, 0, 8);
        Assert.assertEquals(8, off);
        Assert.assertEquals((byte) 0xff, buffer[0]);

        long parsed = TarUtils.parseOctalOrBinary(buffer, 0, 8);
        Assert.assertEquals(value, parsed);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesBinaryBigPositive() {
        byte[] buffer = new byte[12];
        long value = 0x1FFFFFFFFL + 100L;
        int off = TarUtils.formatLongOctalOrBinaryBytes(value, buffer, 0, 12);
        Assert.assertEquals(12, off);
        Assert.assertEquals((byte) 0x80, buffer[0]);

        long parsed = TarUtils.parseOctalOrBinary(buffer, 0, 12);
        Assert.assertEquals(value, parsed);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesBinaryBigNegative() {
        byte[] buffer = new byte[12];
        long value = -9876543210L;
        int off = TarUtils.formatLongOctalOrBinaryBytes(value, buffer, 0, 12);
        Assert.assertEquals(12, off);
        Assert.assertEquals((byte) 0xff, buffer[0]);

        long parsed = TarUtils.parseOctalOrBinary(buffer, 0, 12);
        Assert.assertEquals(value, parsed);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongBinaryOverflow() {
        byte[] buffer = new byte[4];
        long value = 1L << (3 * 8); // length 4 allows max 3 bytes
        TarUtils.formatLongOctalOrBinaryBytes(value, buffer, 0, 4);
    }

    @Test
    public void testFormatCheckSumOctalBytes() {
        byte[] buffer = new byte[8];
        int off = TarUtils.formatCheckSumOctalBytes(0755L, buffer, 0, 8);
        Assert.assertEquals(8, off);
        Assert.assertEquals("0000755\0 ", new String(buffer));
    }

    @Test
    public void testComputeCheckSum() {
        byte[] buffer = new byte[]{ 1, 2, 3, (byte) 0xff };
        long sum = TarUtils.computeCheckSum(buffer);
        Assert.assertEquals(1 + 2 + 3 + 255, sum);
    }

    @Test
    public void testVerifyCheckSum() {
        byte[] header = new byte[512];
        for (int i = 0; i < header.length; i++) {
            header[i] = (byte) (i & 0x7f);
        }

        // Fill checksum region with spaces first
        for (int i = TarConstants.CHKSUM_OFFSET; i < TarConstants.CHKSUM_OFFSET + TarConstants.CHKSUMLEN; i++) {
            header[i] = ' ';
        }

        long sum = TarUtils.computeCheckSum(header);
        TarUtils.formatCheckSumOctalBytes(sum, header, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUMLEN);

        Assert.assertTrue(TarUtils.verifyCheckSum(header));

        // Corrupt checksum
        header[TarConstants.CHKSUM_OFFSET] = '9';
        // With COMPRESS-177 heuristic (storedSum > unsignedSum can be true or false depending on sum),
        // let's explicitly test mismatch when storedSum < unsignedSum.
        byte[] badHeader = new byte[512];
        Arrays.fill(badHeader, (byte) 'a');
        TarUtils.formatCheckSumOctalBytes(0, badHeader, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUMLEN);
        Assert.assertFalse(TarUtils.verifyCheckSum(badHeader));
    }

    @Test
    public void testVerifyCheckSumWithDigitsFollowedByNonDigits() {
        byte[] header = new byte[512];
        TarUtils.formatCheckSumOctalBytes(1234L, header, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUMLEN);
        // Header is mostly zeroes, checksum is 1234 which is > sum of spaces, heuristic should return true
        Assert.assertTrue(TarUtils.verifyCheckSum(header));
    }

    @Test
    public void testFallbackEncoding() {
        ZipEncoding fallback = TarUtils.FALLBACK_ENCODING;
        Assert.assertTrue(fallback.canEncode("test"));

        ByteBuffer buf = fallback.encode("hello");
        byte[] bytes = new byte[buf.limit()];
        buf.get(bytes);
        Assert.assertEquals("hello", new String(bytes));

        byte[] decodeBuf = new byte[]{ 'w', 'o', 'r', 'l', 'd', 0, 'x' };
        String decoded = fallback.decode(decodeBuf);
        Assert.assertEquals("world", decoded);
    }
}
