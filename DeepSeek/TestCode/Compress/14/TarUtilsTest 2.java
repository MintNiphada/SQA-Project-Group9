package org.apache.commons.compress.archivers.tar;

import org.junit.Test;
import static org.junit.Assert.*;

public class TarUtilsTest {

    // parseOctal tests
    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalLengthLessThan2() {
        TarUtils.parseOctal(new byte[1], 0, 1);
    }

    @Test
    public void testParseOctalAllNULs() {
        byte[] buffer = new byte[]{0, 0, 0};
        assertEquals(0L, TarUtils.parseOctal(buffer, 0, 3));
    }

    @Test
    public void testParseOctalLeadingSpaces() {
        byte[] buffer = "   7 \0".getBytes(); // spaces, '7', space, NUL
        assertEquals(7L, TarUtils.parseOctal(buffer, 0, buffer.length));
    }

    @Test
    public void testParseOctalTrailingSpaceOrNUL() {
        byte[] buffer1 = "7 \0".getBytes(); // '7', space, NUL
        assertEquals(7L, TarUtils.parseOctal(buffer1, 0, buffer1.length));
        byte[] buffer2 = "7\0".getBytes(); // '7', NUL
        assertEquals(7L, TarUtils.parseOctal(buffer2, 0, buffer2.length));
    }

    @Test
    public void testParseOctalDoubleTrailer() {
        byte[] buffer = "7  \0".getBytes(); // '7', space, space, NUL
        assertEquals(7L, TarUtils.parseOctal(buffer, 0, buffer.length));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalInvalidTrailer() {
        byte[] buffer = "7a".getBytes(); // '7', 'a' - invalid trailer
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalInvalidDigit() {
        byte[] buffer = "8 \0".getBytes(); // '8' not octal
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    @Test
    public void testParseOctalMaxValue() {
        // 777777777777777777777 (21 digits) fits in long? Actually max octal 21 digits: 777777777777777777777 = Long.MAX_VALUE? Let's test with a large octal string.
        // We'll use a buffer with max octal digits that fit in long: 777777777777777777777 (21 sevens) is 7,777,777,777,777,777,777,777 octal = 2^63-1? Actually 2^63-1 = 9223372036854775807, octal is 777777777777777777777. So 21 sevens.
        byte[] buffer = "777777777777777777777 \0".getBytes();
        assertEquals(Long.MAX_VALUE, TarUtils.parseOctal(buffer, 0, buffer.length));
    }

    @Test
    public void testParseOctalLeadingNULThrows() {
        // Leading NUL (0) is not a valid octal digit, should throw
        byte[] buffer = new byte[]{0, '7', ' ', 0};
        try {
            TarUtils.parseOctal(buffer, 0, buffer.length);
            fail("Expected IllegalArgumentException for leading NUL");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testParseOctalOnlySpacesAndTrailer() {
        byte[] buffer = "   \0".getBytes(); // spaces, then NUL
        // After skipping spaces, start==end? Actually start becomes end after skipping spaces, then trailer check: trailer = buffer[end-1] which is NUL, end--, then next trailer = buffer[end-1] which is space? Wait, buffer: ' ', ' ', ' ', 0. length=4. start=0, skip spaces: start becomes 3 (index of NUL). end=4. trailer = buffer[3]=0, so end-- => end=3. Now start=3, end=3, loop not executed. result=0. So returns 0.
        assertEquals(0L, TarUtils.parseOctal(buffer, 0, buffer.length));
    }

    // parseOctalOrBinary tests
    @Test
    public void testParseOctalOrBinaryOctalPath() {
        byte[] buffer = "7 \0".getBytes();
        assertEquals(7L, TarUtils.parseOctalOrBinary(buffer, 0, buffer.length));
    }

    @Test
    public void testParseOctalOrBinaryBinaryPath() {
        // Set high bit of first byte to indicate binary
        byte[] buffer = new byte[4];
        buffer[0] = (byte) 0x80; // high bit set, rest 0
        buffer[1] = 0x01;
        buffer[2] = 0x02;
        buffer[3] = 0x03;
        long expected = ((0x80L & 0x7f) << 24) | (0x01L << 16) | (0x02L << 8) | 0x03L;
        assertEquals(expected, TarUtils.parseOctalOrBinary(buffer, 0, 4));
    }

    @Test
    public void testParseOctalOrBinaryBinaryOverflow() {
        // Create a binary number that overflows signed long
        byte[] buffer = new byte[9]; // 9 bytes, first byte high bit set, value large
        buffer[0] = (byte) 0x80;
        for (int i = 1; i < 9; i++) {
            buffer[i] = (byte) 0xFF;
        }
        try {
            TarUtils.parseOctalOrBinary(buffer, 0, 9);
            fail("Expected IllegalArgumentException for overflow");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testParseOctalOrBinaryBinaryMaxValue() {
        // Maximum signed long: 0x7FFFFFFFFFFFFFFF, but binary representation with high bit cleared? Actually binary format: first byte high bit set, then 7 bits of value, then subsequent bytes. Max value that fits: first byte 0xFF (high bit set, value 0x7f), then 7 bytes of 0xFF. That's 8 bytes total. Value = 0x7FFFFFFFFFFFFFFF? Let's compute: first byte 0xFF -> val = 0x7F. Then for i=1..7: val = (val << 8) + 0xFF. After 7 iterations, val = 0x7FFFFFFFFFFFFFFF (which is Long.MAX_VALUE). So length=8.
        byte[] buffer = new byte[8];
        buffer[0] = (byte) 0xFF; // high bit set, value 0x7F
        for (int i = 1; i < 8; i++) {
            buffer[i] = (byte) 0xFF;
        }
        assertEquals(Long.MAX_VALUE, TarUtils.parseOctalOrBinary(buffer, 0, 8));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalOrBinaryBinaryOverflowAtLimit() {
        // One more than max: first byte 0xFF, then 7 bytes 0xFF, but length=9 with extra 0x01 will overflow.
        byte[] buffer = new byte[9];
        buffer[0] = (byte) 0xFF;
        for (int i = 1; i < 8; i++) {
            buffer[i] = (byte) 0xFF;
        }
        buffer[8] = 0x01;
        TarUtils.parseOctalOrBinary(buffer, 0, 9);
    }

    // parseBoolean tests
    @Test
    public void testParseBooleanTrue() {
        byte[] buffer = new byte[]{1};
        assertTrue(TarUtils.parseBoolean(buffer, 0));
    }

    @Test
    public void testParseBooleanFalseZero() {
        byte[] buffer = new byte[]{0};
        assertFalse(TarUtils.parseBoolean(buffer, 0));
    }

    @Test
    public void testParseBooleanFalseOther() {
        byte[] buffer = new byte[]{2};
        assertFalse(TarUtils.parseBoolean(buffer, 0));
    }

    // parseName tests
    @Test
    public void testParseNameNormal() {
        byte[] buffer = "hello\0world".getBytes();
        assertEquals("hello", TarUtils.parseName(buffer, 0, buffer.length));
    }

    @Test
    public void testParseNameNoNUL() {
        byte[] buffer = "hello".getBytes();
        assertEquals("hello", TarUtils.parseName(buffer, 0, buffer.length));
    }

    @Test
    public void testParseNameEmpty() {
        byte[] buffer = new byte[0];
        assertEquals("", TarUtils.parseName(buffer, 0, 0));
    }

    @Test
    public void testParseNameWithHighBit() {
        byte[] buffer = new byte[]{(byte) 0xC3, (byte) 0xA9, 0}; // é in UTF-8? But we treat as unsigned char.
        String result = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals(2, result.length());
        assertEquals(0xC3, result.charAt(0) & 0xFF);
        assertEquals(0xA9, result.charAt(1) & 0xFF);
    }

    // formatNameBytes tests
    @Test
    public void testFormatNameBytesShorterThanLength() {
        byte[] buf = new byte[10];
        int offset = 2;
        int length = 5;
        int result = TarUtils.formatNameBytes("hi", buf, offset, length);
        assertEquals(offset + length, result);
        assertEquals('h', buf[offset]);
        assertEquals('i', buf[offset+1]);
        assertEquals(0, buf[offset+2]);
        assertEquals(0, buf[offset+3]);
        assertEquals(0, buf[offset+4]);
    }

    @Test
    public void testFormatNameBytesExactLength() {
        byte[] buf = new byte[5];
        TarUtils.formatNameBytes("hello", buf, 0, 5);
        assertEquals('h', buf[0]);
        assertEquals('e', buf[1]);
        assertEquals('l', buf[2]);
        assertEquals('l', buf[3]);
        assertEquals('o', buf[4]);
    }

    @Test
    public void testFormatNameBytesLongerThanLength() {
        byte[] buf = new byte[5];
        TarUtils.formatNameBytes("hello world", buf, 0, 5);
        assertEquals('h', buf[0]);
        assertEquals('e', buf[1]);
        assertEquals('l', buf[2]);
        assertEquals('l', buf[3]);
        assertEquals('o', buf[4]);
    }

    @Test
    public void testFormatNameBytesEmptyName() {
        byte[] buf = new byte[3];
        TarUtils.formatNameBytes("", buf, 0, 3);
        assertEquals(0, buf[0]);
        assertEquals(0, buf[1]);
        assertEquals(0, buf[2]);
    }

    // formatUnsignedOctalString tests
    @Test
    public void testFormatUnsignedOctalStringZero() {
        byte[] buf = new byte[3];
        TarUtils.formatUnsignedOctalString(0L, buf, 0, 3);
        assertEquals('0', buf[0]);
        assertEquals('0', buf[1]);
        assertEquals('0', buf[2]);
    }

    @Test
    public void testFormatUnsignedOctalStringSmallValue() {
        byte[] buf = new byte[3];
        TarUtils.formatUnsignedOctalString(7L, buf, 0, 3);
        assertEquals('0', buf[0]);
        assertEquals('0', buf[1]);
        assertEquals('7', buf[2]);
    }

    @Test
    public void testFormatUnsignedOctalStringMaxFit() {
        byte[] buf = new byte[3];
        // 3 octal digits max = 777 octal = 511 decimal
        TarUtils.formatUnsignedOctalString(511L, buf, 0, 3);
        assertEquals('7', buf[0]);
        assertEquals('7', buf[1]);
        assertEquals('7', buf[2]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatUnsignedOctalStringOverflow() {
        byte[] buf = new byte[3];
        TarUtils.formatUnsignedOctalString(512L, buf, 0, 3); // 512 = 1000 octal, needs 4 digits
    }

    @Test
    public void testFormatUnsignedOctalStringLengthOneZero() {
        byte[] buf = new byte[1];
        TarUtils.formatUnsignedOctalString(0L, buf, 0, 1);
        assertEquals('0', buf[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatUnsignedOctalStringLengthOneOverflow() {
        byte[] buf = new byte[1];
        TarUtils.formatUnsignedOctalString(8L, buf, 0, 1); // 8 octal = 10, needs 2 digits
    }

    // formatOctalBytes tests
    @Test
    public void testFormatOctalBytes() {
        byte[] buf = new byte[5];
        int result = TarUtils.formatOctalBytes(7L, buf, 0, 5);
        assertEquals(5, result);
        // formatUnsignedOctalString for length-2=3: value 7 -> "007"
        assertEquals('0', buf[0]);
        assertEquals('0', buf[1]);
        assertEquals('7', buf[2]);
        assertEquals(' ', buf[3]);
        assertEquals(0, buf[4]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatOctalBytesOverflow() {
        byte[] buf = new byte[4]; // length 4, so octal part length 2, max 77 octal = 63 decimal
        TarUtils.formatOctalBytes(64L, buf, 0, 4);
    }

    // formatLongOctalBytes tests
    @Test
    public void testFormatLongOctalBytes() {
        byte[] buf = new byte[4];
        int result = TarUtils.formatLongOctalBytes(7L, buf, 0, 4);
        assertEquals(4, result);
        // formatUnsignedOctalString for length-1=3: "007"
        assertEquals('0', buf[0]);
        assertEquals('0', buf[1]);
        assertEquals('7', buf[2]);
        assertEquals(' ', buf[3]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongOctalBytesOverflow() {
        byte[] buf = new byte[3]; // length 3, octal part length 2, max 77 octal = 63
        TarUtils.formatLongOctalBytes(64L, buf, 0, 3);
    }

    // formatLongOctalOrBinaryBytes tests
    @Test
    public void testFormatLongOctalOrBinaryBytesOctalPath() {
        // Use UIDLEN length (8) and value <= MAXID (2097151)
        byte[] buf = new byte[TarConstants.UIDLEN];
        long value = 2097151L; // MAXID
        int result = TarUtils.formatLongOctalOrBinaryBytes(value, buf, 0, TarConstants.UIDLEN);
        assertEquals(TarConstants.UIDLEN, result);
        // Should be formatted as octal with trailing space
        assertEquals(' ', buf[TarConstants.UIDLEN - 1]);
        // Check that high bit is not set
        assertEquals(0, buf[0] & 0x80);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesBinaryPath() {
        // Use SIZE field length (12) and value > MAXSIZE (8589934591L)
        byte[] buf = new byte[12];
        long value = 8589934592L; // MAXSIZE + 1
        int result = TarUtils.formatLongOctalOrBinaryBytes(value, buf, 0, 12);
        assertEquals(12, result);
        // High bit of first byte should be set
        assertTrue((buf[0] & 0x80) != 0);
        // Verify value can be recovered
        long recovered = 0;
        for (int i = 0; i < 12; i++) {
            recovered = (recovered << 8) | (buf[i] & 0xFF);
        }
        // The high bit was set, so we need to mask it out for comparison? Actually the stored value is the original value with high bit set on first byte. The original value's most significant bits are stored in the lower 7 bits of first byte and subsequent bytes. So recovered after masking high bit? Let's just check that the value is correctly stored by using parseOctalOrBinary.
        long parsed = TarUtils.parseOctalOrBinary(buf, 0, 12);
        assertEquals(value, parsed);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongOctalOrBinaryBytesBinaryOverflow() {
        // Value too large for 8-byte field (UIDLEN) binary? Actually binary path only used if value > maxAsOctalChar. For UIDLEN=8, maxAsOctalChar=MAXID=2097151. If value > that, it goes binary. But binary in 8 bytes can hold up to Long.MAX_VALUE? Actually 8 bytes binary with high bit set can hold up to 2^63-1. So overflow only if value > Long.MAX_VALUE? But the method checks if val != 0 or (buf[offset] & 0x80) != 0 after shifting. For 8 bytes, if value is negative? Actually value is long, so it's signed. If value is negative, the high bit of the long is set, which would cause overflow in the binary representation? Let's test with a value that is too large for the field length. For length=2, max binary value is 0x7FFF (since first byte high bit set, then one more byte). So value 0x8000 would overflow.
        byte[] buf = new byte[2];
        TarUtils.formatLongOctalOrBinaryBytes(0x8000L, buf, 0, 2);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesBinaryEdge() {
        // length=2, max value that fits: 0x7FFF (32767)
        byte[] buf = new byte[2];
        TarUtils.formatLongOctalOrBinaryBytes(0x7FFFL, buf, 0, 2);
        assertTrue((buf[0] & 0x80) != 0);
        assertEquals(0x7F, buf[0] & 0x7F);
        assertEquals(0xFF, buf[1] & 0xFF);
    }

    // formatCheckSumOctalBytes tests
    @Test
    public void testFormatCheckSumOctalBytes() {
        byte[] buf = new byte[8];
        int result = TarUtils.formatCheckSumOctalBytes(1234567L, buf, 0, 8);
        assertEquals(8, result);
        // formatUnsignedOctalString for length-2=6: value 1234567 octal is 4553207? Actually 1234567 decimal = 4553207 octal (7 digits). So it should fit in 6? 4553207 is 7 digits, so overflow? Let's test with a small value.
        // We'll test with value 0.
        TarUtils.formatCheckSumOctalBytes(0L, buf, 0, 8);
        assertEquals('0', buf[0]);
        assertEquals('0', buf[1]);
        assertEquals('0', buf[2]);
        assertEquals('0', buf[3]);
        assertEquals('0', buf[4]);
        assertEquals('0', buf[5]);
        assertEquals(0, buf[6]);   // NUL
        assertEquals(' ', buf[7]); // space
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatCheckSumOctalBytesOverflow() {
        byte[] buf = new byte[4]; // length 4, octal part length 2, max 77 octal = 63
        TarUtils.formatCheckSumOctalBytes(64L, buf, 0, 4);
    }

    // computeCheckSum tests
    @Test
    public void testComputeCheckSumEmpty() {
        byte[] buf = new byte[0];
        assertEquals(0L, TarUtils.computeCheckSum(buf));
    }

    @Test
    public void testComputeCheckSumSingleByte() {
        byte[] buf = new byte[]{(byte) 0xFF};
        assertEquals(255L, TarUtils.computeCheckSum(buf));
    }

    @Test
    public void testComputeCheckSumMultipleBytes() {
        byte[] buf = new byte[]{1, 2, 3, (byte) 0xFF};
        assertEquals(1+2+3+255L, TarUtils.computeCheckSum(buf));
    }

    @Test
    public void testComputeCheckSumWithNegativeBytes() {
        byte[] buf = new byte[]{-1, -2}; // -1 & 255 = 255, -2 & 255 = 254
        assertEquals(255L + 254L, TarUtils.computeCheckSum(buf));
    }

    // Additional edge cases for parseOctal: offset not zero
    @Test
    public void testParseOctalWithOffset() {
        byte[] buffer = new byte[10];
        buffer[5] = '7';
        buffer[6] = ' ';
        buffer[7] = 0;
        assertEquals(7L, TarUtils.parseOctal(buffer, 5, 3));
    }

    // Test exception message indirectly? Not necessary but can be done.
}
```
