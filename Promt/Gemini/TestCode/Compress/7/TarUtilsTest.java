package org.apache.commons.compress.archivers.tar;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

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
    public void testParseOctalSimple() {
        byte[] buffer = "0000755 \0".getBytes();
        long val = TarUtils.parseOctal(buffer, 0, buffer.length);
        Assert.assertEquals(0755L, val);
    }

    @Test
    public void testParseOctalZero() {
        byte[] buffer = "0000000 \0".getBytes();
        long val = TarUtils.parseOctal(buffer, 0, buffer.length);
        Assert.assertEquals(0L, val);
    }

    @Test
    public void testParseOctalWithLeadingSpaces() {
        byte[] buffer = "   755 \0".getBytes();
        long val = TarUtils.parseOctal(buffer, 0, buffer.length);
        Assert.assertEquals(0755L, val);
    }

    @Test
    public void testParseOctalWithLeadingZerosAndSpaces() {
        byte[] buffer = " 000755 \0".getBytes();
        long val = TarUtils.parseOctal(buffer, 0, buffer.length);
        Assert.assertEquals(0755L, val);
    }

    @Test
    public void testParseOctalEmptyStringAllSpaces() {
        byte[] buffer = "        ".getBytes();
        long val = TarUtils.parseOctal(buffer, 0, buffer.length);
        Assert.assertEquals(0L, val);
    }

    @Test
    public void testParseOctalEmptyStringAllZeros() {
        byte[] buffer = "00000000".getBytes();
        long val = TarUtils.parseOctal(buffer, 0, buffer.length);
        Assert.assertEquals(0L, val);
    }

    @Test
    public void testParseOctalTerminatedByNull() {
        byte[] buffer = new byte[]{'0', '7', '5', '5', 0, '1', '2', '3'};
        long val = TarUtils.parseOctal(buffer, 0, buffer.length);
        Assert.assertEquals(0755L, val);
    }

    @Test
    public void testParseOctalTerminatedBySpace() {
        byte[] buffer = "755 123".getBytes();
        long val = TarUtils.parseOctal(buffer, 0, buffer.length);
        Assert.assertEquals(0755L, val);
    }

    @Test
    public void testParseOctalFullBufferNoTrailingDelimiters() {
        byte[] buffer = "755".getBytes();
        long val = TarUtils.parseOctal(buffer, 0, buffer.length);
        Assert.assertEquals(0755L, val);
    }

    @Test
    public void testParseOctalWithOffset() {
        byte[] buffer = "padding000755 \0extra".getBytes();
        long val = TarUtils.parseOctal(buffer, 7, 8);
        Assert.assertEquals(0755L, val);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalInvalidDigitHigh() {
        byte[] buffer = "0000855 \0".getBytes();
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalInvalidDigitLow() {
        byte[] buffer = new byte[]{'0', '0', (byte) ('0' - 1), '5', '5', ' '};
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalInvalidCharNonDigit() {
        byte[] buffer = "0000abc \0".getBytes();
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    @Test
    public void testParseOctalLargeValue() {
        byte[] buffer = "77777777777 \0".getBytes();
        long expected = 077777777777L;
        long val = TarUtils.parseOctal(buffer, 0, buffer.length);
        Assert.assertEquals(expected, val);
    }

    @Test
    public void testParseName() {
        byte[] buffer = "test-file.txt\0remaining garbage".getBytes();
        String name = TarUtils.parseName(buffer, 0, buffer.length);
        Assert.assertEquals("test-file.txt", name);
    }

    @Test
    public void testParseNameWithoutNullTermination() {
        byte[] buffer = "exactlength".getBytes();
        String name = TarUtils.parseName(buffer, 0, buffer.length);
        Assert.assertEquals("exactlength", name);
    }

    @Test
    public void testParseNameWithOffset() {
        byte[] buffer = "prefix_my-file.tar\0extra".getBytes();
        String name = TarUtils.parseName(buffer, 7, 15);
        Assert.assertEquals("my-file.tar", name);
    }

    @Test
    public void testParseNameEmpty() {
        byte[] buffer = new byte[]{0, 'a', 'b', 'c'};
        String name = TarUtils.parseName(buffer, 0, buffer.length);
        Assert.assertEquals("", name);
    }

    @Test
    public void testFormatNameBytesFitExact() {
        byte[] buf = new byte[8];
        int nextOffset = TarUtils.formatNameBytes("12345678", buf, 0, 8);
        Assert.assertEquals(8, nextOffset);
        Assert.assertEquals("12345678", new String(buf));
    }

    @Test
    public void testFormatNameBytesTruncated() {
        byte[] buf = new byte[5];
        int nextOffset = TarUtils.formatNameBytes("1234567890", buf, 0, 5);
        Assert.assertEquals(5, nextOffset);
        Assert.assertEquals("12345", new String(buf));
    }

    @Test
    public void testFormatNameBytesPaddedWithNulls() {
        byte[] buf = new byte[8];
        int nextOffset = TarUtils.formatNameBytes("1234", buf, 0, 8);
        Assert.assertEquals(8, nextOffset);
        Assert.assertEquals('1', buf[0]);
        Assert.assertEquals('2', buf[1]);
        Assert.assertEquals('3', buf[2]);
        Assert.assertEquals('4', buf[3]);
        Assert.assertEquals(0, buf[4]);
        Assert.assertEquals(0, buf[5]);
        Assert.assertEquals(0, buf[6]);
        Assert.assertEquals(0, buf[7]);
    }

    @Test
    public void testFormatNameBytesWithOffset() {
        byte[] buf = new byte[12];
        int nextOffset = TarUtils.formatNameBytes("abc", buf, 4, 6);
        Assert.assertEquals(10, nextOffset);
        Assert.assertEquals(0, buf[3]);
        Assert.assertEquals('a', buf[4]);
        Assert.assertEquals('b', buf[5]);
        Assert.assertEquals('c', buf[6]);
        Assert.assertEquals(0, buf[7]);
        Assert.assertEquals(0, buf[8]);
        Assert.assertEquals(0, buf[9]);
        Assert.assertEquals(0, buf[10]);
    }

    @Test
    public void testFormatUnsignedOctalStringZero() {
        byte[] buf = new byte[4];
        TarUtils.formatUnsignedOctalString(0L, buf, 0, 4);
        Assert.assertEquals("0000", new String(buf));
    }

    @Test
    public void testFormatUnsignedOctalStringValue() {
        byte[] buf = new byte[6];
        TarUtils.formatUnsignedOctalString(0755L, buf, 0, 6);
        Assert.assertEquals("000755", new String(buf));
    }

    @Test
    public void testFormatUnsignedOctalStringExactLength() {
        byte[] buf = new byte[3];
        TarUtils.formatUnsignedOctalString(0755L, buf, 0, 3);
        Assert.assertEquals("755", new String(buf));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatUnsignedOctalStringOverflow() {
        byte[] buf = new byte[2];
        TarUtils.formatUnsignedOctalString(0755L, buf, 0, 2);
    }

    @Test
    public void testFormatUnsignedOctalStringLargeValue() {
        byte[] buf = new byte[12];
        TarUtils.formatUnsignedOctalString(077777777777L, buf, 0, 12);
        Assert.assertEquals("077777777777", new String(buf));
    }

    @Test
    public void testFormatOctalBytes() {
        byte[] buf = new byte[8];
        int nextOffset = TarUtils.formatOctalBytes(0755L, buf, 0, 8);
        Assert.assertEquals(8, nextOffset);
        Assert.assertEquals('0', buf[0]);
        Assert.assertEquals('0', buf[1]);
        Assert.assertEquals('0', buf[2]);
        Assert.assertEquals('7', buf[3]);
        Assert.assertEquals('5', buf[4]);
        Assert.assertEquals('5', buf[5]);
        Assert.assertEquals(' ', buf[6]);
        Assert.assertEquals(0, buf[7]);
    }

    @Test
    public void testFormatOctalBytesWithOffset() {
        byte[] buf = new byte[12];
        int nextOffset = TarUtils.formatOctalBytes(0755L, buf, 2, 8);
        Assert.assertEquals(10, nextOffset);
        Assert.assertEquals('0', buf[2]);
        Assert.assertEquals('0', buf[3]);
        Assert.assertEquals('0', buf[4]);
        Assert.assertEquals('7', buf[5]);
        Assert.assertEquals('5', buf[6]);
        Assert.assertEquals('5', buf[7]);
        Assert.assertEquals(' ', buf[8]);
        Assert.assertEquals(0, buf[9]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatOctalBytesOverflow() {
        byte[] buf = new byte[4];
        TarUtils.formatOctalBytes(077777L, buf, 0, 4);
    }

    @Test
    public void testFormatLongOctalBytes() {
        byte[] buf = new byte[8];
        int nextOffset = TarUtils.formatLongOctalBytes(0755L, buf, 0, 8);
        Assert.assertEquals(8, nextOffset);
        Assert.assertEquals('0', buf[0]);
        Assert.assertEquals('0', buf[1]);
        Assert.assertEquals('0', buf[2]);
        Assert.assertEquals('0', buf[3]);
        Assert.assertEquals('7', buf[4]);
        Assert.assertEquals('5', buf[5]);
        Assert.assertEquals('5', buf[6]);
        Assert.assertEquals(' ', buf[7]);
    }

    @Test
    public void testFormatLongOctalBytesWithOffset() {
        byte[] buf = new byte[10];
        int nextOffset = TarUtils.formatLongOctalBytes(0755L, buf, 2, 8);
        Assert.assertEquals(10, nextOffset);
        Assert.assertEquals('0', buf[2]);
        Assert.assertEquals('0', buf[3]);
        Assert.assertEquals('0', buf[4]);
        Assert.assertEquals('0', buf[5]);
        Assert.assertEquals('7', buf[6]);
        Assert.assertEquals('5', buf[7]);
        Assert.assertEquals('5', buf[8]);
        Assert.assertEquals(' ', buf[9]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongOctalBytesOverflow() {
        byte[] buf = new byte[4];
        TarUtils.formatLongOctalBytes(077777L, buf, 0, 4);
    }

    @Test
    public void testFormatCheckSumOctalBytes() {
        byte[] buf = new byte[8];
        int nextOffset = TarUtils.formatCheckSumOctalBytes(0755L, buf, 0, 8);
        Assert.assertEquals(8, nextOffset);
        Assert.assertEquals('0', buf[0]);
        Assert.assertEquals('0', buf[1]);
        Assert.assertEquals('0', buf[2]);
        Assert.assertEquals('7', buf[3]);
        Assert.assertEquals('5', buf[4]);
        Assert.assertEquals('5', buf[5]);
        Assert.assertEquals(0, buf[6]);
        Assert.assertEquals(' ', buf[7]);
    }

    @Test
    public void testFormatCheckSumOctalBytesWithOffset() {
        byte[] buf = new byte[12];
        int nextOffset = TarUtils.formatCheckSumOctalBytes(0755L, buf, 2, 8);
        Assert.assertEquals(10, nextOffset);
        Assert.assertEquals('0', buf[2]);
        Assert.assertEquals('0', buf[3]);
        Assert.assertEquals('0', buf[4]);
        Assert.assertEquals('7', buf[5]);
        Assert.assertEquals('5', buf[6]);
        Assert.assertEquals('5', buf[7]);
        Assert.assertEquals(0, buf[8]);
        Assert.assertEquals(' ', buf[9]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatCheckSumOctalBytesOverflow() {
        byte[] buf = new byte[4];
        TarUtils.formatCheckSumOctalBytes(077777L, buf, 0, 4);
    }

    @Test
    public void testComputeCheckSum() {
        byte[] buf = new byte[]{1, 2, 3, 4, 5};
        long sum = TarUtils.computeCheckSum(buf);
        Assert.assertEquals(15L, sum);
    }

    @Test
    public void testComputeCheckSumEmptyBuffer() {
        byte[] buf = new byte[0];
        long sum = TarUtils.computeCheckSum(buf);
        Assert.assertEquals(0L, sum);
    }

    @Test
    public void testComputeCheckSumWithNegativeBytes() {
        byte[] buf = new byte[]{(byte) 0xFF, (byte) 0x80, 0x01};
        long sum = TarUtils.computeCheckSum(buf);
        Assert.assertEquals(255L + 128L + 1L, sum);
    }

    @Test
    public void testRoundTripParseAndFormatOctal() {
        byte[] buf = new byte[12];
        long original = 012345670L;
        TarUtils.formatOctalBytes(original, buf, 0, buf.length);
        long parsed = TarUtils.parseOctal(buf, 0, buf.length);
        Assert.assertEquals(original, parsed);
    }

    @Test
    public void testRoundTripParseAndFormatCheckSumOctal() {
        byte[] buf = new byte[8];
        long original = 0755L;
        TarUtils.formatCheckSumOctalBytes(original, buf, 0, buf.length);
        long parsed = TarUtils.parseOctal(buf, 0, buf.length);
        Assert.assertEquals(original, parsed);
    }

    @Test
    public void testRoundTripParseAndFormatLongOctal() {
        byte[] buf = new byte[12];
        long original = 076543210L;
        TarUtils.formatLongOctalBytes(original, buf, 0, buf.length);
        long parsed = TarUtils.parseOctal(buf, 0, buf.length);
        Assert.assertEquals(original, parsed);
    }

    @Test
    public void testRoundTripParseAndFormatName() {
        byte[] buf = new byte[32];
        String original = "archive/subfolder/file.txt";
        TarUtils.formatNameBytes(original, buf, 0, buf.length);
        String parsed = TarUtils.parseName(buf, 0, buf.length);
        Assert.assertEquals(original, parsed);
    }
}
