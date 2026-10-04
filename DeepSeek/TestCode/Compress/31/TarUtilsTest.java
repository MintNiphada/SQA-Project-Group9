package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.apache.commons.compress.archivers.zip.ZipEncoding;
import org.apache.commons.compress.archivers.zip.ZipEncodingHelper;
import org.junit.Test;

public class TarUtilsTest {

    @Test
    public void testParseOctalValidSimple() {
        byte[] buffer = "0000000 \0".getBytes(StandardCharsets.US_ASCII);
        long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0L, result);
    }

    @Test
    public void testParseOctalValidNumber() {
        byte[] buffer = "0000644 \0".getBytes(StandardCharsets.US_ASCII);
        long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(420L, result);
    }

    @Test
    public void testParseOctalLeadingSpaces() {
        byte[] buffer = "   0644 \0".getBytes(StandardCharsets.US_ASCII);
        long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(420L, result);
    }

    @Test
    public void testParseOctalTrailingSpacesAndNuls() {
        byte[] buffer = "0644  \0".getBytes(StandardCharsets.US_ASCII);
        long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(420L, result);
    }

    @Test
    public void testParseOctalAllNuls() {
        byte[] buffer = new byte[]{0, 0, 0, 0, 0};
        long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0L, result);
    }

    @Test
    public void testParseOctalLeadingNul() {
        byte[] buffer = new byte[]{0, '0', '0', '0', '0', '6', '4', '4', ' ', 0};
        long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0L, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalLengthLessThanTwo() {
        byte[] buffer = new byte[]{'0'};
        TarUtils.parseOctal(buffer, 0, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalInvalidByte() {
        byte[] buffer = "000064a \0".getBytes(StandardCharsets.US_ASCII);
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalMissingTrailer() {
        byte[] buffer = "0000644".getBytes(StandardCharsets.US_ASCII);
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    @Test
    public void testParseOctalOrBinaryOctal() {
        byte[] buffer = "0000644 \0".getBytes(StandardCharsets.US_ASCII);
        long result = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        assertEquals(420L, result);
    }

    @Test
    public void testParseOctalOrBinaryBinaryPositive() {
        byte[] buffer = new byte[]{(byte) 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01};
        long result = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        assertEquals(1L, result);
    }

    @Test
    public void testParseOctalOrBinaryBinaryNegative() {
        byte[] buffer = new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xfe};
        long result = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        assertEquals(-2L, result);
    }

    @Test
    public void testParseOctalOrBinaryBinaryNegativeMinusOne() {
        byte[] buffer = new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
        long result = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        assertEquals(-1L, result);
    }

    @Test
    public void testParseOctalOrBinaryBinaryLongMax() {
        byte[] buffer = new byte[]{(byte) 0x80, 0x7f, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
        long result = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        assertEquals(Long.MAX_VALUE, result);
    }

    @Test
    public void testParseOctalOrBinaryBinaryLongMin() {
        byte[] buffer = new byte[]{(byte) 0xff, (byte) 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        long result = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        assertEquals(Long.MIN_VALUE, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalOrBinaryBinaryTooLarge() {
        byte[] buffer = new byte[]{(byte) 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01};
        TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
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
    public void testParseBooleanFalseNonZero() {
        byte[] buffer = new byte[]{2};
        assertFalse(TarUtils.parseBoolean(buffer, 0));
    }

    @Test
    public void testParseNameSimple() {
        byte[] buffer = "file.txt\0".getBytes(StandardCharsets.US_ASCII);
        String result = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals("file.txt", result);
    }

    @Test
    public void testParseNameTrailingNuls() {
        byte[] buffer = new byte[]{'f', 'i', 'l', 'e', 0, 0, 0};
        String result = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals("file", result);
    }

    @Test
    public void testParseNameAllNuls() {
        byte[] buffer = new byte[]{0, 0, 0};
        String result = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals("", result);
    }

    @Test
    public void testParseNameWithEncoding() throws IOException {
        byte[] buffer = "file.txt\0".getBytes(StandardCharsets.US_ASCII);
        ZipEncoding encoding = ZipEncodingHelper.getZipEncoding("US-ASCII");
        String result = TarUtils.parseName(buffer, 0, buffer.length, encoding);
        assertEquals("file.txt", result);
    }

    @Test
    public void testFormatNameBytesSimple() {
        byte[] buf = new byte[10];
        int result = TarUtils.formatNameBytes("file", buf, 0, 10);
        assertEquals(10, result);
        assertEquals('f', buf[0]);
        assertEquals('i', buf[1]);
        assertEquals('l', buf[2]);
        assertEquals('e', buf[3]);
        assertEquals(0, buf[4]);
    }

    @Test
    public void testFormatNameBytesTruncate() {
        byte[] buf = new byte[3];
        int result = TarUtils.formatNameBytes("longname", buf, 0, 3);
        assertEquals(3, result);
        assertEquals('l', buf[0]);
        assertEquals('o', buf[1]);
        assertEquals('n', buf[2]);
    }

    @Test
    public void testFormatNameBytesWithEncoding() throws IOException {
        byte[] buf = new byte[10];
        ZipEncoding encoding = ZipEncodingHelper.getZipEncoding("US-ASCII");
        int result = TarUtils.formatNameBytes("file", buf, 0, 10, encoding);
        assertEquals(10, result);
        assertEquals('f', buf[0]);
    }

    @Test
    public void testFormatUnsignedOctalStringZero() {
        byte[] buf = new byte[4];
        TarUtils.formatUnsignedOctalString(0L, buf, 0, 4);
        assertEquals('0', buf[0]);
        assertEquals('0', buf[1]);
        assertEquals('0', buf[2]);
        assertEquals('0', buf[3]);
    }

    @Test
    public void testFormatUnsignedOctalStringValue() {
        byte[] buf = new byte[4];
        TarUtils.formatUnsignedOctalString(8L, buf, 0, 4);
        assertEquals('0', buf[0]);
        assertEquals('0', buf[1]);
        assertEquals('1', buf[2]);
        assertEquals('0', buf[3]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatUnsignedOctalStringOverflow() {
        byte[] buf = new byte[2];
        TarUtils.formatUnsignedOctalString(64L, buf, 0, 2);
    }

    @Test
    public void testFormatOctalBytes() {
        byte[] buf = new byte[6];
        int result = TarUtils.formatOctalBytes(420L, buf, 0, 6);
        assertEquals(6, result);
        assertEquals('0', buf[0]);
        assertEquals('0', buf[1]);
        assertEquals('0', buf[2]);
        assertEquals('6', buf[3]);
        assertEquals('4', buf[4]);
        assertEquals('4', buf[5]);
    }

    @Test
    public void testFormatLongOctalBytes() {
        byte[] buf = new byte[5];
        int result = TarUtils.formatLongOctalBytes(420L, buf, 0, 5);
        assertEquals(5, result);
        assertEquals('0', buf[0]);
        assertEquals('0', buf[1]);
        assertEquals('6', buf[2]);
        assertEquals('4', buf[3]);
        assertEquals('4', buf[4]);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesOctal() {
        byte[] buf = new byte[8];
        int result = TarUtils.formatLongOctalOrBinaryBytes(100L, buf, 0, 8);
        assertEquals(8, result);
        assertEquals('0', buf[0]);
        assertEquals('0', buf[1]);
        assertEquals('0', buf[2]);
        assertEquals('0', buf[3]);
        assertEquals('0', buf[4]);
        assertEquals('1', buf[5]);
        assertEquals('4', buf[6]);
        assertEquals('4', buf[7]);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesBinaryPositive() {
        byte[] buf = new byte[8];
        int result = TarUtils.formatLongOctalOrBinaryBytes(Long.MAX_VALUE, buf, 0, 8);
        assertEquals(8, result);
        assertEquals((byte) 0x80, buf[0]);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesBinaryNegative() {
        byte[] buf = new byte[8];
        int result = TarUtils.formatLongOctalOrBinaryBytes(-1L, buf, 0, 8);
        assertEquals(8, result);
        assertEquals((byte) 0xff, buf[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongOctalOrBinaryBytesBinaryTooLarge() {
        byte[] buf = new byte[4];
        TarUtils.formatLongOctalOrBinaryBytes(Long.MAX_VALUE, buf, 0, 4);
    }

    @Test
    public void testFormatCheckSumOctalBytes() {
        byte[] buf = new byte[8];
        int result = TarUtils.formatCheckSumOctalBytes(12345L, buf, 0, 8);
        assertEquals(8, result);
        assertEquals('0', buf[0]);
        assertEquals('0', buf[1]);
        assertEquals('0', buf[2]);
        assertEquals('0', buf[3]);
        assertEquals('3', buf[4]);
        assertEquals('0', buf[5]);
        assertEquals('0', buf[6]);
        assertEquals('7', buf[7]);
    }

    @Test
    public void testComputeCheckSum() {
        byte[] header = new byte[512];
        for (int i = 0; i < 512; i++) {
            header[i] = (byte) i;
        }
        long sum = TarUtils.computeCheckSum(header);
        assertEquals(32640L, sum);
    }

    @Test
    public void testVerifyCheckSumValidUnsigned() {
        byte[] header = new byte[512];
        for (int i = 0; i < 512; i++) {
            header[i] = (byte) i;
        }
        long unsignedSum = 0;
        for (int i = 0; i < 512; i++) {
            unsignedSum += 0xff & header[i];
        }
        String octal = Long.toOctalString(unsignedSum);
        while (octal.length() < 6) {
            octal = "0" + octal;
        }
        octal = octal + "\0 ";
        byte[] octalBytes = octal.getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(octalBytes, 0, header, 148, octalBytes.length);
        assertTrue(TarUtils.verifyCheckSum(header));
    }

    @Test
    public void testVerifyCheckSumValidSigned() {
        byte[] header = new byte[512];
        for (int i = 0; i < 512; i++) {
            header[i] = (byte) i;
        }
        long signedSum = 0;
        for (int i = 0; i < 512; i++) {
            signedSum += header[i];
        }
        String octal = Long.toOctalString(signedSum);
        while (octal.length() < 6) {
            octal = "0" + octal;
        }
        octal = octal + "\0 ";
        byte[] octalBytes = octal.getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(octalBytes, 0, header, 148, octalBytes.length);
        assertTrue(TarUtils.verifyCheckSum(header));
    }

    @Test
    public void testVerifyCheckSumGreaterThanUnsigned() {
        byte[] header = new byte[512];
        for (int i = 0; i < 512; i++) {
            header[i] = (byte) i;
        }
        long unsignedSum = 0;
        for (int i = 0; i < 512; i++) {
            unsignedSum += 0xff & header[i];
        }
        String octal = Long.toOctalString(unsignedSum + 1);
        while (octal.length() < 6) {
            octal = "0" + octal;
        }
        octal = octal + "\0 ";
        byte[] octalBytes = octal.getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(octalBytes, 0, header, 148, octalBytes.length);
        assertTrue(TarUtils.verifyCheckSum(header));
    }

    @Test
    public void testVerifyCheckSumInvalid() {
        byte[] header = new byte[512];
        for (int i = 0; i < 512; i++) {
            header[i] = (byte) i;
        }
        String octal = "000000\0 ";
        byte[] octalBytes = octal.getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(octalBytes, 0, header, 148, octalBytes.length);
        assertFalse(TarUtils.verifyCheckSum(header));
    }

    @Test
    public void testParseOctalOrBinaryBinaryShortNegative() {
        byte[] buffer = new byte[]{(byte) 0xff, (byte) 0xfe};
        long result = TarUtils.parseOctalOrBinary(buffer, 0, 2);
        assertEquals(-2L, result);
    }

    @Test
    public void testParseOctalOrBinaryBinaryShortPositive() {
        byte[] buffer = new byte[]{(byte) 0x80, 0x01};
        long result = TarUtils.parseOctalOrBinary(buffer, 0, 2);
        assertEquals(1L, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalOrBinaryBinaryShortTooLarge() {
        byte[] buffer = new byte[]{(byte) 0x80, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
        TarUtils.parseOctalOrBinary(buffer, 0, 9);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesShortBinary() {
        byte[] buf = new byte[4];
        int result = TarUtils.formatLongOctalOrBinaryBytes(255L, buf, 0, 4);
        assertEquals(4, result);
        assertEquals((byte) 0x80, buf[0]);
        assertEquals(0x00, buf[1]);
        assertEquals(0x00, buf[2]);
        assertEquals((byte) 0xff, buf[3]);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesShortBinaryNegative() {
        byte[] buf = new byte[4];
        int result = TarUtils.formatLongOctalOrBinaryBytes(-1L, buf, 0, 4);
        assertEquals(4, result);
        assertEquals((byte) 0xff, buf[0]);
        assertEquals((byte) 0xff, buf[1]);
        assertEquals((byte) 0xff, buf[2]);
        assertEquals((byte) 0xff, buf[3]);
    }

    @Test
    public void testParseOctalOrBinaryBinaryBigIntegerNegative() {
        byte[] buffer = new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xfe};
        long result = TarUtils.parseOctalOrBinary(buffer, 0, 9);
        assertEquals(-2L, result);
    }

    @Test
    public void testParseOctalOrBinaryBinaryBigIntegerPositive() {
        byte[] buffer = new byte[]{(byte) 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01};
        long result = TarUtils.parseOctalOrBinary(buffer, 0, 9);
        assertEquals(1L, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalOrBinaryBinaryBigIntegerTooLarge() {
        byte[] buffer = new byte[]{(byte) 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01};
        TarUtils.parseOctalOrBinary(buffer, 0, 10);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesBigIntegerBinary() {
        byte[] buf = new byte[9];
        int result = TarUtils.formatLongOctalOrBinaryBytes(Long.MAX_VALUE, buf, 0, 9);
        assertEquals(9, result);
        assertEquals((byte) 0x80, buf[0]);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesBigIntegerBinaryNegative() {
        byte[] buf = new byte[9];
        int result = TarUtils.formatLongOctalOrBinaryBytes(Long.MIN_VALUE, buf, 0, 9);
        assertEquals(9, result);
        assertEquals((byte) 0xff, buf[0]);
    }

    @Test
    public void testParseOctalTrailingNulOnly() {
        byte[] buffer = new byte[]{'0', '0', '0', '0', '6', '4', '4', 0};
        long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(420L, result);
    }

    @Test
    public void testParseOctalTrailingSpaceOnly() {
        byte[] buffer = new byte[]{'0', '0', '0', '0', '6', '4', '4', ' '};
        long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(420L, result);
    }

    @Test
    public void testParseOctalBreakOnNul() {
        byte[] buffer = new byte[]{'0', '0', '0', '0', '6', 0, '4', '4', ' ', 0};
        long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(6L, result);
    }

    @Test
    public void testFormatNameBytesFallbackEncoding() {
        byte[] buf = new byte[10];
        int result = TarUtils.formatNameBytes("file", buf, 0, 10);
        assertEquals(10, result);
        assertEquals('f', buf[0]);
    }

    @Test
    public void testParseNameFallbackEncoding() {
        byte[] buffer = new byte[]{'f', 'i', 'l', 'e', 0};
        String result = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals("file", result);
    }

    @Test
    public void testVerifyCheckSumOnlyFirstSixDigits() {
        byte[] header = new byte[512];
        for (int i = 0; i < 512; i++) {
            header[i] = (byte) i;
        }
        long unsignedSum = 0;
        for (int i = 0; i < 512; i++) {
            unsignedSum += 0xff & header[i];
        }
        String octal = Long.toOctalString(unsignedSum);
        while (octal.length() < 6) {
            octal = "0" + octal;
        }
        octal = octal + "123\0 ";
        byte[] octalBytes = octal.getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(octalBytes, 0, header, 148, octalBytes.length);
        assertTrue(TarUtils.verifyCheckSum(header));
    }

    @Test
    public void testVerifyCheckSumNonOctalCharactersIgnored() {
        byte[] header = new byte[512];
        for (int i = 0; i < 512; i++) {
            header[i] = (byte) i;
        }
        long unsignedSum = 0;
        for (int i = 0; i < 512; i++) {
            unsignedSum += 0xff & header[i];
        }
        String octal = Long.toOctalString(unsignedSum);
        while (octal.length() < 6) {
            octal = "0" + octal;
        }
        octal = "a" + octal + "\0 ";
        byte[] octalBytes = octal.getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(octalBytes, 0, header, 148, octalBytes.length);
        assertFalse(TarUtils.verifyCheckSum(header));
    }

    @Test
    public void testFormatUnsignedOctalStringMaxValueForLength() {
        byte[] buf = new byte[3];
        TarUtils.formatUnsignedOctalString(63L, buf, 0, 3);
        assertEquals('0', buf[0]);
        assertEquals('7', buf[1]);
        assertEquals('7', buf[2]);
    }

    @Test
    public void testParseOctalOrBinaryBinaryNegativeShortMinusOne() {
        byte[] buffer = new byte[]{(byte) 0xff, (byte) 0xff};
        long result = TarUtils.parseOctalOrBinary(buffer, 0, 2);
        assertEquals(-1L, result);
    }

    @Test
    public void testParseOctalOrBinaryBinaryNegativeShortMinus128() {
        byte[] buffer = new byte[]{(byte) 0xff, (byte) 0x80};
        long result = TarUtils.parseOctalOrBinary(buffer, 0, 2);
        assertEquals(-128L, result);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesShortBinaryMax() {
        byte[] buf = new byte[4];
        int result = TarUtils.formatLongOctalOrBinaryBytes(8388607L, buf, 0, 4);
        assertEquals(4, result);
        assertEquals((byte) 0x80, buf[0]);
        assertEquals(0x7f, buf[1]);
        assertEquals((byte) 0xff, buf[2]);
        assertEquals((byte) 0xff, buf[3]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongOctalOrBinaryBytesShortBinaryOverflow() {
        byte[] buf = new byte[4];
        TarUtils.formatLongOctalOrBinaryBytes(8388608L, buf, 0, 4);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesShortBinaryNegativeMin() {
        byte[] buf = new byte[4];
        int result = TarUtils.formatLongOctalOrBinaryBytes(-8388608L, buf, 0, 4);
        assertEquals(4, result);
        assertEquals((byte) 0xff, buf[0]);
        assertEquals((byte) 0x80, buf[1]);
        assertEquals(0x00, buf[2]);
        assertEquals(0x00, buf[3]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongOctalOrBinaryBytesShortBinaryNegativeOverflow() {
        byte[] buf = new byte[4];
        TarUtils.formatLongOctalOrBinaryBytes(-8388609L, buf, 0, 4);
    }

    @Test
    public void testParseOctalOrBinaryBinaryBigIntegerNegativeMinusOne() {
        byte[] buffer = new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
        long result = TarUtils.parseOctalOrBinary(buffer, 0, 9);
        assertEquals(-1L, result);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesBigIntegerBinaryMax() {
        byte[] buf = new byte[9];
        int result = TarUtils.formatLongOctalOrBinaryBytes(Long.MAX_VALUE, buf, 0, 9);
        assertEquals(9, result);
        assertEquals((byte) 0x80, buf[0]);
        assertEquals(0x7f, buf[1]);
        assertEquals((byte) 0xff, buf[2]);
        assertEquals((byte) 0xff, buf[3]);
        assertEquals((byte) 0xff, buf[4]);
        assertEquals((byte) 0xff, buf[5]);
        assertEquals((byte) 0xff, buf[6]);
        assertEquals((byte) 0xff, buf[7]);
        assertEquals((byte) 0xff, buf[8]);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesBigIntegerBinaryMin() {
        byte[] buf = new byte[9];
        int result = TarUtils.formatLongOctalOrBinaryBytes(Long.MIN_VALUE, buf, 0, 9);
        assertEquals(9, result);
        assertEquals((byte) 0xff, buf[0]);
        assertEquals((byte) 0x80, buf[1]);
        assertEquals(0x00, buf[2]);
        assertEquals(0x00, buf[3]);
        assertEquals(0x00, buf[4]);
        assertEquals(0x00, buf[5]);
        assertEquals(0x00, buf[6]);
        assertEquals(0x00, buf[7]);
        assertEquals(0x00, buf[8]);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesOctalMaxId() {
        byte[] buf = new byte[8];
        int result = TarUtils.formatLongOctalOrBinaryBytes(2097151L, buf, 0, 8);
        assertEquals(8, result);
        assertEquals('0', buf[0]);
        assertEquals('0', buf[1]);
        assertEquals('0', buf[2]);
        assertEquals('0', buf[3]);
        assertEquals('0', buf[4]);
        assertEquals('0', buf[5]);
        assertEquals('0', buf[6]);
        assertEquals('0', buf[7]);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesOctalMaxSize() {
        byte[] buf = new byte[12];
        int result = TarUtils.formatLongOctalOrBinaryBytes(8589934591L, buf, 0, 12);
        assertEquals(12, result);
        assertEquals('0', buf[0]);
        assertEquals('0', buf[1]);
        assertEquals('0', buf[2]);
        assertEquals('0', buf[3]);
        assertEquals('0', buf[4]);
        assertEquals('0', buf[5]);
        assertEquals('0', buf[6]);
        assertEquals('0', buf[7]);
        assertEquals('0', buf[8]);
        assertEquals('0', buf[9]);
        assertEquals('0', buf[10]);
        assertEquals('0', buf[11]);
    }

    @Test
    public void testParseOctalOrBinaryBinaryNegativeBigIntegerMinusTwo() {
        byte[] buffer = new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xfe};
        long result = TarUtils.parseOctalOrBinary(buffer, 0, 9);
        assertEquals(-2L, result);
    }

    @Test
    public void testParseOctalOrBinaryBinaryPositiveBigIntegerOne() {
        byte[] buffer = new byte[]{(byte) 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01};
        long result = TarUtils.parseOctalOrBinary(buffer, 0, 9);
        assertEquals(1L, result);
    }

    @Test
    public void testFormatNameBytesEncodingTruncate() throws IOException {
        byte[] buf = new byte[3];
        ZipEncoding encoding = new ZipEncoding() {
            public boolean canEncode(String name) { return true; }
            public ByteBuffer encode(String name) {
                byte[] b = name.getBytes(StandardCharsets.UTF_16);
                return ByteBuffer.wrap(b);
            }
            public String decode(byte[] buffer) { return new String(buffer, StandardCharsets.UTF_16); }
        };
        int result = TarUtils.formatNameBytes("ab", buf, 0, 3, encoding);
        assertEquals(3, result);
    }

    @Test
    public void testParseNameEncodingFallback() throws IOException {
        byte[] buffer = new byte[]{'f', 'i', 'l', 'e', 0};
        ZipEncoding encoding = new ZipEncoding() {
            public boolean canEncode(String name) { return true; }
            public ByteBuffer encode(String name) throws IOException { throw new IOException("fail"); }
            public String decode(byte[] buffer) throws IOException { throw new IOException("fail"); }
        };
        try {
            TarUtils.parseName(buffer, 0, buffer.length, encoding);
            fail("Expected IOException");
        } catch (IOException e) {
        }
    }

    @Test
    public void testFormatNameBytesEncodingFallback() throws IOException {
        byte[] buf = new byte[10];
        ZipEncoding encoding = new ZipEncoding() {
            public boolean canEncode(String name) { return true; }
            public ByteBuffer encode(String name) throws IOException { throw new IOException("fail"); }
            public String decode(byte[] buffer) throws IOException { throw new IOException("fail"); }
        };
        try {
            TarUtils.formatNameBytes("file", buf, 0, 10, encoding);
            fail("Expected IOException");
        } catch (IOException e) {
        }
    }

    @Test
    public void testParseOctalOrBinaryBinaryNegativeBigIntegerMin() {
        byte[] buffer = new byte[]{(byte) 0xff, (byte) 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        long result = TarUtils.parseOctalOrBinary(buffer, 0, 9);
        assertEquals(Long.MIN_VALUE, result);
    }

    @Test
    public void testParseOctalOrBinaryBinaryPositiveBigIntegerMax() {
        byte[] buffer = new byte[]{(byte) 0x80, 0x7f, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
        long result = TarUtils.parseOctalOrBinary(buffer, 0, 9);
        assertEquals(Long.MAX_VALUE, result);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesBigIntegerBinaryNegativeMin() {
        byte[] buf = new byte[9];
        int result = TarUtils.formatLongOctalOrBinaryBytes(Long.MIN_VALUE, buf, 0, 9);
        assertEquals(9, result);
        assertEquals((byte) 0xff, buf[0]);
        assertEquals((byte) 0x80, buf[1]);
        assertEquals(0x00, buf[2]);
        assertEquals(0x00, buf[3]);
        assertEquals(0x00, buf[4]);
        assertEquals(0x00, buf[5]);
        assertEquals(0x00, buf[6]);
        assertEquals(0x00, buf[7]);
        assertEquals(0x00, buf[8]);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesBigIntegerBinaryPositiveMax() {
        byte[] buf = new byte[9];
        int result = TarUtils.formatLongOctalOrBinaryBytes(Long.MAX_VALUE, buf, 0, 9);
        assertEquals(9, result);
        assertEquals((byte) 0x80, buf[0]);
        assertEquals(0x7f, buf[1]);
        assertEquals((byte) 0xff, buf[2]);
        assertEquals((byte) 0xff, buf[3]);
        assertEquals((byte) 0xff, buf[4]);
        assertEquals((byte) 0xff, buf[5]);
        assertEquals((byte) 0xff, buf[6]);
        assertEquals((byte) 0xff, buf[7]);
        assertEquals((byte) 0xff, buf[8]);
    }

    @Test
    public void testParseOctalOrBinaryBinaryNegativeShortMin() {
        byte[] buffer = new byte[]{(byte) 0xff, (byte) 0x80};
        long result = TarUtils.parseOctalOrBinary(buffer, 0, 2);
        assertEquals(-128L, result);
    }

    @Test
    public void testParseOctalOrBinaryBinaryPositiveShortMax() {
        byte[] buffer = new byte[]{(byte) 0x80, 0x7f};
        long result = TarUtils.parseOctalOrBinary(buffer, 0, 2);
        assertEquals(127L, result);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesShortBinaryPositiveMax() {
        byte[] buf = new byte[4];
        int result = TarUtils.formatLongOctalOrBinaryBytes(8388607L, buf, 0, 4);
        assertEquals(4, result);
        assertEquals((byte) 0x80, buf[0]);
        assertEquals(0x7f, buf[1]);
        assertEquals((byte) 0xff, buf[2]);
        assertEquals((byte) 0xff, buf[3]);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesShortBinaryNegativeMin() {
        byte[] buf = new byte[4];
        int result = TarUtils.formatLongOctalOrBinaryBytes(-8388608L, buf, 0, 4);
        assertEquals(4, result);
        assertEquals((byte) 0xff, buf[0]);
        assertEquals((byte) 0x80, buf[1]);
        assertEquals(0x00, buf[2]);
        assertEquals(0x00, buf[3]);
    }

    @Test
    public void testVerifyCheckSumStoredSumGreaterThanUnsignedSum() {
        byte[] header = new byte[512];
        for (int i = 0; i < 512; i++) {
            header[i] = (byte) i;
        }
        long unsignedSum = 0;
        for (int i = 0; i < 512; i++) {
            unsignedSum += 0xff & header[i];
        }
        long storedSum = unsignedSum + 1;
        String octal = Long.toOctalString(storedSum);
        while (octal.length() < 6) {
            octal = "0" + octal;
        }
        octal = octal + "\0 ";
        byte[] octalBytes = octal.getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(octalBytes, 0, header, 148, octalBytes.length);
        assertTrue(TarUtils.verifyCheckSum(header));
    }

    @Test
    public void testVerifyCheckSumStoredSumLessThanUnsignedSum() {
        byte[] header = new byte[512];
        for (int i = 0; i < 512; i++) {
            header[i] = (byte) i;
        }
        long unsignedSum = 0;
        for (int i = 0; i < 512; i++) {
            unsignedSum += 0xff & header[i];
        }
        long storedSum = unsignedSum - 1;
        String octal = Long.toOctalString(storedSum);
        while (octal.length() < 6) {
            octal = "0" + octal;
        }
        octal = octal + "\0 ";
        byte[] octalBytes = octal.getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(octalBytes, 0, header, 148, octalBytes.length);
        assertFalse(TarUtils.verifyCheckSum(header));
    }

    @Test
    public void testVerifyCheckSumStoredSumEqualsSignedSum() {
        byte[] header = new byte[512];
        for (int i = 0; i < 512; i++) {
            header[i] = (byte) i;
        }
        long signedSum = 0;
        for (int i = 0; i < 512; i++) {
            signedSum += header[i];
        }
        String octal = Long.toOctalString(signedSum);
        while (octal.length() < 6) {
            octal = "0" + octal;
        }
        octal = octal + "\0 ";
        byte[] octalBytes = octal.getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(octalBytes, 0, header, 148, octalBytes.length);
        assertTrue(TarUtils.verifyCheckSum(header));
    }

    @Test
    public void testVerifyCheckSumStoredSumNotEqualsSignedSum() {
        byte[] header = new byte[512];
        for (int i = 0; i < 512; i++) {
            header[i] = (byte) i;
        }
        long signedSum = 0;
        for (int i = 0; i < 512; i++) {
            signedSum += header[i];
        }
        long storedSum = signedSum + 1;
        String octal = Long.toOctalString(storedSum);
        while (octal.length() < 6) {
            octal = "0" + octal;
        }
        octal = octal + "\0 ";
        byte[] octalBytes = octal.getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(octalBytes, 0, header, 148, octalBytes.length);
        assertFalse(TarUtils.verifyCheckSum(header));
    }

    @Test
    public void testParseOctalOrBinaryBinaryNegativeBigIntegerMinusOne() {
        byte[] buffer = new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
        long result = TarUtils.parseOctalOrBinary(buffer, 0, 9);
        assertEquals(-1L, result);
    }

    @Test
    public void testParseOctalOrBinaryBinaryPositiveBigIntegerOne() {
        byte[] buffer = new byte[]{(byte) 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01};
        long result = TarUtils.parseOctalOrBinary(buffer, 0, 9);
        assertEquals(1L, result);
    }

    @Test
    public void testParseOctalOrBinaryBinaryNegativeBigIntegerMin() {
        byte[] buffer = new byte[]{(byte) 0xff, (byte) 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        long result = TarUtils.parseOctalOrBinary(buffer, 0, 9);
        assertEquals(Long.MIN_VALUE, result);
    }

    @Test
    public void testParseOctalOrBinaryBinaryPositiveBigIntegerMax() {
        byte[] buffer = new byte[]{(byte) 0x80, 0x7f, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
        long result = TarUtils.parseOctalOrBinary(buffer, 0, 9);
        assertEquals(Long.MAX_VALUE, result);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesBigIntegerBinaryNegativeMin() {
        byte[] buf = new byte[9];
        int result = TarUtils.formatLongOctalOrBinaryBytes(Long.MIN_VALUE, buf, 0, 9);
        assertEquals(9, result);
        assertEquals((byte) 0xff, buf[0]);
        assertEquals((byte) 0x80, buf[1]);
        assertEquals(0x00, buf[2]);
        assertEquals(0x00, buf[3]);
        assertEquals(0x00, buf[4]);
        assertEquals(0x00, buf[5]);
        assertEquals(0x00, buf[6]);
        assertEquals(0x00, buf[7]);
        assertEquals(0x00, buf[8]);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesBigIntegerBinaryPositiveMax() {
        byte[] buf = new byte[9];
        int result = TarUtils.formatLongOctalOrBinaryBytes(Long.MAX_VALUE, buf, 0, 9);
        assertEquals(9, result);
        assertEquals((byte) 0x80, buf[0]);
        assertEquals(0x7f, buf[1]);
        assertEquals((byte) 0xff, buf[2]);
        assertEquals((byte) 0xff, buf[3]);
        assertEquals((byte) 0xff, buf[4]);
        assertEquals((byte) 0xff, buf[5]);
        assertEquals((byte) 0xff, buf[6]);
        assertEquals((byte) 0xff, buf[7]);
        assertEquals((byte) 0xff, buf[8]);
    }

    @Test
    public void testParseOctalOrBinaryBinaryNegativeShortMin() {
        byte[] buffer = new byte[]{(byte) 0xff, (byte) 0x80};
        long result = TarUtils.parseOctalOrBinary(buffer, 0, 2);
        assertEquals(-128L, result);
    }

    @Test
    public void testParseOctalOrBinaryBinaryPositiveShortMax() {
        byte[] buffer = new byte[]{(byte) 0x80, 0x7f};
        long result = TarUtils.parseOctalOrBinary(buffer, 0, 2);
        assertEquals(127L, result);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesShortBinaryPositiveMax() {
        byte[] buf = new byte[4];
        int result = TarUtils.formatLongOctalOrBinaryBytes(8388607L, buf, 0, 4);
        assertEquals(4, result);
        assertEquals((byte) 0x80, buf[0]);
        assertEquals(0x7f, buf[1]);
        assertEquals((byte) 0xff, buf[2]);
        assertEquals((byte) 0xff, buf[3]);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesShortBinaryNegativeMin() {
        byte[] buf = new byte[4];
        int result = TarUtils.formatLongOctalOrBinaryBytes(-8388608L, buf, 0, 4);
        assertEquals(4, result);
        assertEquals((byte) 0xff, buf[0]);
        assertEquals((byte) 0x80, buf[1]);
        assertEquals(0x00, buf[2]);
        assertEquals(0x00, buf[3]);
    }

    @Test
    public void testVerifyCheckSumStoredSumGreaterThanUnsignedSum() {
        byte[] header = new byte[512];
        for (int i = 0; i < 512; i++) {
            header[i] = (byte) i;
        }
        long unsignedSum = 0;
        for (int i = 0; i < 512; i++) {
            unsignedSum += 0xff & header[i];
        }
        long storedSum = unsignedSum + 1;
        String octal = Long.toOctalString(storedSum);
        while (octal.length() < 6) {
            octal = "0" + octal;
        }
        octal = octal + "\0 ";
        byte[] octalBytes = octal.getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(octalBytes, 0, header, 148, octalBytes.length);
        assertTrue(TarUtils.verifyCheckSum(header));
    }

    @Test
    public void testVerifyCheckSumStoredSumLessThanUnsignedSum() {
        byte[] header = new byte[512];
        for (int i = 0; i < 512; i++) {
            header[i] = (byte) i;
        }
        long unsignedSum = 0;
        for (int i = 0; i < 512; i++) {
            unsignedSum += 0xff & header[i];
        }
        long storedSum = unsignedSum - 1;
        String octal = Long.toOctalString(storedSum);
        while (octal.length() < 6) {
            octal = "0" + octal;
        }
        octal = octal + "\0 ";
        byte[] octalBytes = octal.getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(octalBytes, 0, header, 148, octalBytes.length);
        assertFalse(TarUtils.verifyCheckSum(header));
    }

    @Test
    public void testVerifyCheckSumStoredSumEqualsSignedSum() {
        byte[] header = new byte[512];
        for (int i = 0; i < 512; i++) {
            header[i] = (byte) i;
        }
        long signedSum = 0;
        for (int i = 0; i < 512; i++) {
            signedSum += header[i];
        }
        String octal = Long.toOctalString(signedSum);
        while (octal.length() < 6) {
            octal = "0" + octal;
        }
        octal = octal + "\0 ";
        byte[] octalBytes = octal.getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(octalBytes, 0, header, 148, octalBytes.length);
        assertTrue(TarUtils.verifyCheckSum(header));
    }

    @Test
    public void testVerifyCheckSumStoredSumNotEqualsSignedSum() {
        byte[] header = new byte[512];
        for (int i = 0; i < 512; i++) {
            header[i] = (byte) i;
        }
        long signedSum = 0;
        for (int i = 0; i < 512; i++) {
            signedSum += header[i];
        }
        long storedSum = signedSum + 1;
        String octal = Long.toOctalString(storedSum);
        while (octal.length() < 6) {
            octal = "0" + octal;
        }
        octal = octal + "\0 ";
        byte[] octalBytes = octal.getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(octalBytes, 0, header, 148, octalBytes.length);
        assertFalse(TarUtils.verifyCheckSum(header));
    }

    @Test
    public void testParseOctalOrBinaryBinaryNegativeBigIntegerMinusOne() {
        byte[] buffer = new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
        long result = TarUtils.parseOctalOrBinary(buffer, 0, 9);
        assertEquals(-1L, result);
    }

    @Test
    public void testParseOctalOrBinaryBinaryPositiveBigIntegerOne() {
        byte[] buffer = new byte[]{(byte) 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01};
        long result = TarUtils.parseOctalOrBinary(buffer, 0, 9);
        assertEquals(1L, result);
    }

    @Test
    public void testParseOctalOrBinaryBinaryNegativeBigIntegerMin() {
        byte[] buffer = new byte[]{(byte) 0xff, (byte) 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        long result = TarUtils.parseOctalOrBinary(buffer, 0, 9);
        assertEquals(Long.MIN_VALUE, result);
    }

    @Test
    public void testParseOctalOrBinaryBinaryPositiveBigIntegerMax() {
        byte[] buffer = new byte[]{(byte) 0x80, 0x7f, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
        long result = TarUtils.parseOctalOrBinary(buffer, 0, 9);
        assertEquals(Long.MAX_VALUE, result);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesBigIntegerBinaryNegativeMin() {
        byte[] buf = new byte[9];
        int result = TarUtils.formatLongOctalOrBinaryBytes(Long.MIN_VALUE, buf, 0, 9);
        assertEquals(9, result);
        assertEquals((byte) 0xff, buf[0]);
        assertEquals((byte) 0x80, buf[1]);
        assertEquals(0x00, buf[2]);
        assertEquals(0x00, buf[3]);
        assertEquals(0x00, buf[4]);
        assertEquals(0x00, buf[5]);
        assertEquals(0x00, buf[6]);
        assertEquals(0x00, buf[7]);
        assertEquals(0x00, buf[8]);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesBigIntegerBinaryPositiveMax() {
        byte[] buf = new byte[9];
        int result = TarUtils.formatLongOctalOrBinaryBytes(Long.MAX_VALUE, buf, 0, 9);
        assertEquals(9, result);
        assertEquals((byte) 0x80, buf[0]);
        assertEquals(0x7f, buf[1]);
        assertEquals((byte) 0xff, buf[2]);
        assertEquals((byte) 0xff, buf[3]);
        assertEquals((byte) 0xff, buf[4]);
        assertEquals((byte) 0xff, buf[5]);
        assertEquals((byte) 0xff, buf[6]);
        assertEquals((byte) 0xff, buf[7]);
        assertEquals((byte) 0xff, buf[8]);
    }

    @Test
    public void testParseOctalOrBinaryBinaryNegativeShortMin() {
        byte[] buffer = new byte[]{(byte) 0xff, (byte) 0x80};
        long result = TarUtils.parseOctalOrBinary(buffer, 0, 2);
        assertEquals(-128L, result);
    }

    @Test
    public void testParseOctalOrBinaryBinaryPositiveShortMax() {
        byte[] buffer = new byte[]{(byte) 0x80, 0x7f};
        long result = TarUtils.parseOctalOrBinary(buffer, 0, 2);
        assertEquals(127L, result);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesShortBinaryPositiveMax() {
        byte[] buf = new byte[4];
        int result = TarUtils.formatLongOctalOrBinaryBytes(8388607L, buf, 0, 4);
        assertEquals(4, result);
        assertEquals((byte) 0x80, buf[0]);
        assertEquals(0x7f, buf[1]);
        assertEquals((byte) 0xff, buf[2]);
        assertEquals((byte) 0xff, buf[3]);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesShortBinaryNegativeMin() {
        byte[] buf = new byte[4];
        int result = TarUtils.formatLongOctalOrBinaryBytes(-8388608L, buf, 0, 4);
        assertEquals(4, result);
        assertEquals((byte) 0xff, buf[0]);
        assertEquals((byte) 0x80, buf[1]);
        assertEquals(0x00, buf[2]);
        assertEquals(0x00, buf[3]);
    }

    @Test
    public void testVerifyCheckSumStoredSumGreaterThanUnsignedSum() {
        byte[] header = new byte[512];
        for (int i = 0; i < 512; i++) {
            header[i] = (byte) i;
        }
        long unsignedSum = 0;
        for (int i = 0; i < 512; i++) {
            unsignedSum += 0xff & header[i];
        }
        long storedSum = unsignedSum + 1;
        String octal = Long.toOctalString(storedSum);
        while (octal.length() < 6) {
            octal = "0" + octal;
        }
        octal = octal + "\0 ";
        byte[] octalBytes = octal.getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(octalBytes, 0, header, 148, octalBytes.length);
        assertTrue(TarUtils.verifyCheckSum(header));
    }

    @Test
    public void testVerifyCheckSumStoredSumLessThanUnsignedSum() {
        byte[] header = new byte[512];
        for (int i = 0; i < 512; i++) {
            header[i] = (byte) i;
        }
        long unsignedSum = 0;
        for (int i = 0; i < 512; i++) {
            unsignedSum += 0xff & header[i];
        }
        long storedSum = unsignedSum - 1;
        String octal = Long.toOctalString(storedSum);
        while (octal.length() < 6) {
            octal = "0" + octal;
        }
        octal = octal + "\0 ";
        byte[] octalBytes = octal.getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(octalBytes, 0, header, 148, octalBytes.length);
        assertFalse(TarUtils.verifyCheckSum(header));
    }

    @Test
    public void testVerifyCheckSumStoredSumEqualsSignedSum() {
        byte[] header = new byte[512];
        for (int i = 0; i < 512; i++) {
            header[i] = (byte) i;
        }
        long signedSum = 0;
        for (int i = 0; i < 512; i++) {
            signedSum += header[i];
        }
        String octal = Long.toOctalString(signedSum);
        while (octal.length() < 6) {
            octal = "0" + octal;
        }
        octal = octal + "\0 ";
        byte[] octalBytes = octal.getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(octalBytes, 0, header, 148, octalBytes.length);
        assertTrue(TarUtils.verifyCheckSum(header));
    }

    @Test
    public void testVerifyCheckSumStoredSumNotEqualsSignedSum() {
        byte[] header = new byte[512];
        for (int i = 0; i < 512; i++) {
            header[i] = (byte) i;
        }
        long signedSum = 0;
        for (int i = 0; i < 512; i++) {
            signedSum += header[i];
        }
        long storedSum = signedSum + 1;
        String octal = Long.toOctalString(storedSum);
        while (octal.length() < 6) {
            octal = "0" + octal;
        }
        octal = octal + "\0 ";
        byte[] octalBytes = octal.getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(octalBytes, 0, header, 148, octalBytes.length);
        assertFalse(TarUtils.verifyCheckSum(header));
    }

}
