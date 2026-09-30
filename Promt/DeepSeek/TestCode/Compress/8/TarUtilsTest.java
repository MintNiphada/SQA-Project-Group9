package org.apache.commons.compress.archivers.tar;

import org.junit.Test;
import static org.junit.Assert.*;

public class TarUtilsTest {

    @Test
    public void testParseOctalBasic() {
        byte[] buffer = "0000000\0".getBytes();
        assertEquals(0L, TarUtils.parseOctal(buffer, 0, 8));
    }

    @Test
    public void testParseOctalLeadingSpaces() {
        byte[] buffer = "   0007\0".getBytes();
        assertEquals(7L, TarUtils.parseOctal(buffer, 0, 8));
    }

    @Test
    public void testParseOctalLeadingZeros() {
        byte[] buffer = "0000007\0".getBytes();
        assertEquals(7L, TarUtils.parseOctal(buffer, 0, 8));
    }

    @Test
    public void testParseOctalTrailingSpace() {
        byte[] buffer = "0000007 ".getBytes();
        assertEquals(7L, TarUtils.parseOctal(buffer, 0, 8));
    }

    @Test
    public void testParseOctalTrailingNul() {
        byte[] buffer = "0000007\0".getBytes();
        assertEquals(7L, TarUtils.parseOctal(buffer, 0, 8));
    }

    @Test
    public void testParseOctalAllNuls() {
        byte[] buffer = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};
        assertEquals(0L, TarUtils.parseOctal(buffer, 0, 8));
    }

    @Test
    public void testParseOctalMaxValue() {
        byte[] buffer = "7777777\0".getBytes();
        assertEquals(2097151L, TarUtils.parseOctal(buffer, 0, 8));
    }

    @Test
    public void testParseOctalMixedSpacesAndZeros() {
        byte[] buffer = "   0077\0".getBytes();
        assertEquals(63L, TarUtils.parseOctal(buffer, 0, 8));
    }

    @Test
    public void testParseOctalSpaceAfterPadding() {
        byte[] buffer = "   0  \0".getBytes();
        assertEquals(0L, TarUtils.parseOctal(buffer, 0, 6));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalInvalidByte() {
        byte[] buffer = "0000008\0".getBytes();
        TarUtils.parseOctal(buffer, 0, 8);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalInvalidLetter() {
        byte[] buffer = "000000a\0".getBytes();
        TarUtils.parseOctal(buffer, 0, 8);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalNoTrailingSpaceOrNul() {
        byte[] buffer = "00000077".getBytes();
        TarUtils.parseOctal(buffer, 0, 8);
    }

    @Test
    public void testParseOctalZeroValueWithSpace() {
        byte[] buffer = "0\0".getBytes();
        assertEquals(0L, TarUtils.parseOctal(buffer, 0, 2));
    }

    @Test
    public void testParseNameBasic() {
        byte[] buffer = "file.txt\0".getBytes();
        assertEquals("file.txt", TarUtils.parseName(buffer, 0, 9));
    }

    @Test
    public void testParseNameNoTrailingNul() {
        byte[] buffer = "file.txt".getBytes();
        assertEquals("file.txt", TarUtils.parseName(buffer, 0, 8));
    }

    @Test
    public void testParseNameWithNulInMiddle() {
        byte[] buffer = "file\0txt".getBytes();
        assertEquals("file", TarUtils.parseName(buffer, 0, 8));
    }

    @Test
    public void testParseNameEmpty() {
        byte[] buffer = new byte[0];
        assertEquals("", TarUtils.parseName(buffer, 0, 0));
    }

    @Test
    public void testParseNameAllNuls() {
        byte[] buffer = new byte[]{0, 0, 0};
        assertEquals("", TarUtils.parseName(buffer, 0, 3));
    }

    @Test
    public void testParseNameHighBitCharacters() {
        byte[] buffer = new byte[]{(byte) 0xE4, (byte) 0xB8, (byte) 0xAD, 0};
        assertEquals("\u4E2D", TarUtils.parseName(buffer, 0, 4));
    }

    @Test
    public void testFormatNameBytesExactLength() {
        byte[] buf = new byte[8];
        int result = TarUtils.formatNameBytes("file.txt", buf, 0, 8);
        assertEquals(8, result);
        assertEquals("file.txt", new String(buf, 0, 8));
    }

    @Test
    public void testFormatNameBytesShorterName() {
        byte[] buf = new byte[10];
        int result = TarUtils.formatNameBytes("file", buf, 0, 10);
        assertEquals(10, result);
        assertEquals("file", new String(buf, 0, 4));
        for (int i = 4; i < 10; i++) {
            assertEquals(0, buf[i]);
        }
    }

    @Test
    public void testFormatNameBytesLongerName() {
        byte[] buf = new byte[4];
        int result = TarUtils.formatNameBytes("file.txt", buf, 0, 4);
        assertEquals(4, result);
        assertEquals("file", new String(buf, 0, 4));
    }

    @Test
    public void testFormatNameBytesEmptyName() {
        byte[] buf = new byte[5];
        int result = TarUtils.formatNameBytes("", buf, 0, 5);
        assertEquals(5, result);
        for (int i = 0; i < 5; i++) {
            assertEquals(0, buf[i]);
        }
    }

    @Test
    public void testFormatNameBytesWithOffset() {
        byte[] buf = new byte[10];
        int result = TarUtils.formatNameBytes("ab", buf, 3, 4);
        assertEquals(7, result);
        assertEquals('a', buf[3]);
        assertEquals('b', buf[4]);
        assertEquals(0, buf[5]);
        assertEquals(0, buf[6]);
    }

    @Test
    public void testFormatUnsignedOctalStringZero() {
        byte[] buf = new byte[4];
        TarUtils.formatUnsignedOctalString(0L, buf, 0, 4);
        assertEquals("0000", new String(buf, 0, 4));
    }

    @Test
    public void testFormatUnsignedOctalStringSmallValue() {
        byte[] buf = new byte[4];
        TarUtils.formatUnsignedOctalString(7L, buf, 0, 4);
        assertEquals("0007", new String(buf, 0, 4));
    }

    @Test
    public void testFormatUnsignedOctalStringMaxFit() {
        byte[] buf = new byte[4];
        TarUtils.formatUnsignedOctalString(511L, buf, 0, 4);
        assertEquals("0777", new String(buf, 0, 4));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatUnsignedOctalStringOverflow() {
        byte[] buf = new byte[4];
        TarUtils.formatUnsignedOctalString(512L, buf, 0, 4);
    }

    @Test
    public void testFormatUnsignedOctalStringLargeValue() {
        byte[] buf = new byte[8];
        TarUtils.formatUnsignedOctalString(2097151L, buf, 0, 8);
        assertEquals("7777777", new String(buf, 0, 7));
        assertEquals('0', buf[7]);
    }

    @Test
    public void testFormatUnsignedOctalStringWithOffset() {
        byte[] buf = new byte[8];
        TarUtils.formatUnsignedOctalString(7L, buf, 2, 4);
        assertEquals('0', buf[2]);
        assertEquals('0', buf[3]);
        assertEquals('0', buf[4]);
        assertEquals('7', buf[5]);
    }

    @Test
    public void testFormatOctalBytesBasic() {
        byte[] buf = new byte[8];
        int result = TarUtils.formatOctalBytes(7L, buf, 0, 8);
        assertEquals(8, result);
        assertEquals("000007 \0", new String(buf, 0, 8));
    }

    @Test
    public void testFormatOctalBytesZero() {
        byte[] buf = new byte[4];
        int result = TarUtils.formatOctalBytes(0L, buf, 0, 4);
        assertEquals(4, result);
        assertEquals("00 \0", new String(buf, 0, 4));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatOctalBytesOverflow() {
        byte[] buf = new byte[4];
        TarUtils.formatOctalBytes(64L, buf, 0, 4);
    }

    @Test
    public void testFormatLongOctalBytesBasic() {
        byte[] buf = new byte[8];
        int result = TarUtils.formatLongOctalBytes(7L, buf, 0, 8);
        assertEquals(8, result);
        assertEquals("0000007 ", new String(buf, 0, 8));
    }

    @Test
    public void testFormatLongOctalBytesZero() {
        byte[] buf = new byte[4];
        int result = TarUtils.formatLongOctalBytes(0L, buf, 0, 4);
        assertEquals(4, result);
        assertEquals("000 ", new String(buf, 0, 4));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongOctalBytesOverflow() {
        byte[] buf = new byte[4];
        TarUtils.formatLongOctalBytes(512L, buf, 0, 4);
    }

    @Test
    public void testFormatCheckSumOctalBytesBasic() {
        byte[] buf = new byte[8];
        int result = TarUtils.formatCheckSumOctalBytes(7L, buf, 0, 8);
        assertEquals(8, result);
        assertEquals("000007\0 ", new String(buf, 0, 8));
    }

    @Test
    public void testFormatCheckSumOctalBytesZero() {
        byte[] buf = new byte[4];
        int result = TarUtils.formatCheckSumOctalBytes(0L, buf, 0, 4);
        assertEquals(4, result);
        assertEquals("00\0 ", new String(buf, 0, 4));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatCheckSumOctalBytesOverflow() {
        byte[] buf = new byte[4];
        TarUtils.formatCheckSumOctalBytes(64L, buf, 0, 4);
    }

    @Test
    public void testComputeCheckSumEmpty() {
        byte[] buf = new byte[0];
        assertEquals(0L, TarUtils.computeCheckSum(buf));
    }

    @Test
    public void testComputeCheckSumSingleByte() {
        byte[] buf = new byte[]{1};
        assertEquals(1L, TarUtils.computeCheckSum(buf));
    }

    @Test
    public void testComputeCheckSumMultipleBytes() {
        byte[] buf = new byte[]{1, 2, 3};
        assertEquals(6L, TarUtils.computeCheckSum(buf));
    }

    @Test
    public void testComputeCheckSumNegativeBytes() {
        byte[] buf = new byte[]{-1, -1};
        assertEquals(510L, TarUtils.computeCheckSum(buf));
    }

    @Test
    public void testComputeCheckSumAllZeros() {
        byte[] buf = new byte[512];
        assertEquals(0L, TarUtils.computeCheckSum(buf));
    }

    @Test
    public void testParseOctalEdgeCaseLengthTwo() {
        byte[] buffer = "0\0".getBytes();
        assertEquals(0L, TarUtils.parseOctal(buffer, 0, 2));
    }

    @Test
    public void testParseOctalSpaceThenNul() {
        byte[] buffer = "   \0".getBytes();
        assertEquals(0L, TarUtils.parseOctal(buffer, 0, 4));
    }

    @Test
    public void testParseOctalZeroThenSpace() {
        byte[] buffer = "0 ".getBytes();
        assertEquals(0L, TarUtils.parseOctal(buffer, 0, 2));
    }

    @Test
    public void testParseOctalLeadingSpacesThenValue() {
        byte[] buffer = "   7\0".getBytes();
        assertEquals(7L, TarUtils.parseOctal(buffer, 0, 5));
    }

    @Test
    public void testParseOctalLeadingZerosThenSpace() {
        byte[] buffer = "000 ".getBytes();
        assertEquals(0L, TarUtils.parseOctal(buffer, 0, 4));
    }

    @Test
    public void testParseNameWithOffset() {
        byte[] buffer = "prefix/file.txt\0".getBytes();
        assertEquals("file.txt", TarUtils.parseName(buffer, 7, 9));
    }

    @Test
    public void testParseNameLengthShorterThanBuffer() {
        byte[] buffer = "file.txt\0extra".getBytes();
        assertEquals("file.txt", TarUtils.parseName(buffer, 0, 8));
    }

    @Test
    public void testFormatNameBytesNullName() {
        byte[] buf = new byte[5];
        try {
            TarUtils.formatNameBytes(null, buf, 0, 5);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testFormatUnsignedOctalStringLengthOne() {
        byte[] buf = new byte[1];
        TarUtils.formatUnsignedOctalString(0L, buf, 0, 1);
        assertEquals('0', buf[0]);
    }

    @Test
    public void testFormatUnsignedOctalStringLengthOneNonZero() {
        byte[] buf = new byte[1];
        try {
            TarUtils.formatUnsignedOctalString(1L, buf, 0, 1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testFormatOctalBytesMinimumLength() {
        byte[] buf = new byte[3];
        int result = TarUtils.formatOctalBytes(0L, buf, 0, 3);
        assertEquals(3, result);
        assertEquals("0 \0", new String(buf, 0, 3));
    }

    @Test
    public void testFormatLongOctalBytesMinimumLength() {
        byte[] buf = new byte[2];
        int result = TarUtils.formatLongOctalBytes(0L, buf, 0, 2);
        assertEquals(2, result);
        assertEquals("0 ", new String(buf, 0, 2));
    }

    @Test
    public void testFormatCheckSumOctalBytesMinimumLength() {
        byte[] buf = new byte[3];
        int result = TarUtils.formatCheckSumOctalBytes(0L, buf, 0, 3);
        assertEquals(3, result);
        assertEquals("0\0 ", new String(buf, 0, 3));
    }

    @Test
    public void testParseOctalInvalidByteAfterPadding() {
        byte[] buffer = "   8\0".getBytes();
        try {
            TarUtils.parseOctal(buffer, 0, 5);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testParseOctalSpaceAfterZero() {
        byte[] buffer = "0 \0".getBytes();
        assertEquals(0L, TarUtils.parseOctal(buffer, 0, 3));
    }

    @Test
    public void testParseOctalMultipleSpaces() {
        byte[] buffer = "      \0".getBytes();
        assertEquals(0L, TarUtils.parseOctal(buffer, 0, 7));
    }

    @Test
    public void testParseOctalZeroFollowedByNul() {
        byte[] buffer = "0\0".getBytes();
        assertEquals(0L, TarUtils.parseOctal(buffer, 0, 2));
    }

    @Test
    public void testParseOctalLeadingSpacesOnly() {
        byte[] buffer = "   \0".getBytes();
        assertEquals(0L, TarUtils.parseOctal(buffer, 0, 4));
    }

    @Test
    public void testParseOctalLeadingZerosOnly() {
        byte[] buffer = "000\0".getBytes();
        assertEquals(0L, TarUtils.parseOctal(buffer, 0, 4));
    }

    @Test
    public void testParseOctalValueWithTrailingNulAndSpace() {
        byte[] buffer = "007\0 ".getBytes();
        assertEquals(7L, TarUtils.parseOctal(buffer, 0, 5));
    }

    @Test
    public void testParseOctalValueWithTrailingSpaceAndNul() {
        byte[] buffer = "007 \0".getBytes();
        assertEquals(7L, TarUtils.parseOctal(buffer, 0, 5));
    }

    @Test
    public void testFormatNameBytesZeroLength() {
        byte[] buf = new byte[5];
        int result = TarUtils.formatNameBytes("test", buf, 0, 0);
        assertEquals(0, result);
    }

    @Test
    public void testFormatUnsignedOctalStringExactFit() {
        byte[] buf = new byte[3];
        TarUtils.formatUnsignedOctalString(63L, buf, 0, 3);
        assertEquals("077", new String(buf, 0, 3));
    }

    @Test
    public void testFormatUnsignedOctalStringAlmostOverflow() {
        byte[] buf = new byte[3];
        TarUtils.formatUnsignedOctalString(63L, buf, 0, 3);
        assertEquals("077", new String(buf, 0, 3));
    }

    @Test
    public void testComputeCheckSumLargeArray() {
        byte[] buf = new byte[512];
        for (int i = 0; i < 512; i++) {
            buf[i] = (byte) i;
        }
        long expected = 0;
        for (int i = 0; i < 512; i++) {
            expected += (buf[i] & 0xFF);
        }
        assertEquals(expected, TarUtils.computeCheckSum(buf));
    }

    @Test
    public void testParseOctalBufferWithOffset() {
        byte[] buffer = "xx0007\0xx".getBytes();
        assertEquals(7L, TarUtils.parseOctal(buffer, 2, 5));
    }

    @Test
    public void testParseOctalInvalidByteBelowZero() {
        byte[] buffer = "000/00\0".getBytes();
        try {
            TarUtils.parseOctal(buffer, 0, 7);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testParseOctalInvalidByteAboveSeven() {
        byte[] buffer = "000:00\0".getBytes();
        try {
            TarUtils.parseOctal(buffer, 0, 7);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testParseOctalExceptionMessage() {
        byte[] buffer = "0008\0".getBytes();
        try {
            TarUtils.parseOctal(buffer, 0, 5);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Invalid byte 56"));
            assertTrue(e.getMessage().contains("offset 3"));
        }
    }

    @Test
    public void testParseOctalExceptionMessageWithNul() {
        byte[] buffer = "00\08\0".getBytes();
        try {
            TarUtils.parseOctal(buffer, 0, 5);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("{NUL}"));
        }
    }
}
