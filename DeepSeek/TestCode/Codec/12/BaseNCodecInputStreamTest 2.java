package org.apache.commons.codec.binary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class BaseNCodecInputStreamTest {

    @Test
    public void testMarkSupported() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        BaseNCodecInputStream stream = new Base32InputStream(in, true);
        assertFalse(stream.markSupported());
        stream.close();
    }

    @Test
    public void testReadEOFEncoding() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        BaseNCodecInputStream stream = new Base32InputStream(in, true);
        assertEquals(-1, stream.read());
        stream.close();
    }

    @Test
    public void testReadEOFeDecoding() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        BaseNCodecInputStream stream = new Base32InputStream(in, false);
        assertEquals(-1, stream.read());
        stream.close();
    }

    @Test
    public void testReadSingleByteEncoding() throws IOException {
        // Encoding 'A' (byte 65) yields "IE======"
        byte[] raw = { 'A' };
        InputStream in = new ByteArrayInputStream(raw);
        BaseNCodecInputStream stream = new Base32InputStream(in, true);
        // First encoded byte is 'I'
        assertEquals('I', stream.read());
        stream.close();
    }

    @Test
    public void testReadSingleByteDecodingUnsigned() throws IOException {
        // Decoding "QA======" yields byte 0x80 (signed -128)
        byte[] encoded = "QA======".getBytes("US-ASCII");
        InputStream in = new ByteArrayInputStream(encoded);
        BaseNCodecInputStream stream = new Base32InputStream(in, false);
        // Expect unsigned value 256 + (-128) = 128
        assertEquals(128, stream.read());
        stream.close();
    }

    @Test
    public void testReadSingleByteDecodingValid() throws IOException {
        // Decoding "IE======" yields 'A' (65)
        byte[] encoded = "IE======".getBytes("US-ASCII");
        InputStream in = new ByteArrayInputStream(encoded);
        BaseNCodecInputStream stream = new Base32InputStream(in, false);
        assertEquals('A', stream.read());
        stream.close();
    }

    @Test(expected = NullPointerException.class)
    public void testReadByteArrayNull() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        BaseNCodecInputStream stream = new Base32InputStream(in, true);
        stream.read(null, 0, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadByteArrayOffsetNegative() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        BaseNCodecInputStream stream = new Base32InputStream(in, true);
        stream.read(new byte[5], -1, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadByteArrayLengthNegative() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        BaseNCodecInputStream stream = new Base32InputStream(in, true);
        stream.read(new byte[5], 0, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadByteArrayOffsetExceedsLength() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        BaseNCodecInputStream stream = new Base32InputStream(in, true);
        stream.read(new byte[5], 6, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadByteArrayOffsetPlusLenExceedsLength() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        BaseNCodecInputStream stream = new Base32InputStream(in, true);
        stream.read(new byte[5], 3, 3);
    }

    @Test
    public void testReadByteArrayLengthZero() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[]{ 1, 2, 3 });
        BaseNCodecInputStream stream = new Base32InputStream(in, true);
        byte[] buf = new byte[10];
        assertEquals(0, stream.read(buf, 0, 0));
        assertEquals(0, stream.read(buf, 5, 0)); // offset == length, still safe
        stream.close();
    }

    @Test
    public void testReadByteArrayDecoding() throws IOException {
        // Decode "IE======" -> 'A'
        byte[] encoded = "IE======".getBytes("US-ASCII");
        InputStream in = new ByteArrayInputStream(encoded);
        BaseNCodecInputStream stream = new Base32InputStream(in, false);
        byte[] buf = new byte[10];
        int len = stream.read(buf, 0, 10);
        assertEquals(1, len);
        assertEquals('A', buf[0]);
        stream.close();
    }

    @Test
    public void testReadByteArrayEncoding() throws IOException {
        // Encode "A" -> "IE======"
        byte[] raw = { 'A' };
        InputStream in = new ByteArrayInputStream(raw);
        BaseNCodecInputStream stream = new Base32InputStream(in, true);
        byte[] buf = new byte[10];
        int len = stream.read(buf, 0, 10);
        assertEquals(8, len);
        assertEquals("IE======", new String(buf, 0, len, "US-ASCII"));
        stream.close();
    }

    @Test
    public void testReadWhileLoopInternal() throws IOException {
        // Provide only invalid Base32 characters so that readResults returns 0 repeatedly
        // causing the while(readLen == 0) loop to iterate.
        byte[] invalid = "!!!!!".getBytes("US-ASCII");
        InputStream in = new ByteArrayInputStream(invalid);
        BaseNCodecInputStream stream = new Base32InputStream(in, false);
        // Eventually returns EOF after consuming all invalid input
        assertEquals(-1, stream.read());
        stream.close();
    }

    @Test
    public void testReadSingleByteReadZeroRetry() throws IOException {
        // Similar while loop in read(): provide enough invalid data to make read(singleByte,0,1)
        // return 0 at least once, then eventually -1.
        byte[] invalid = new byte[100];
        for (int i = 0; i < invalid.length; i++) {
            invalid[i] = '!';
        }
        InputStream in = new ByteArrayInputStream(invalid);
        BaseNCodecInputStream stream = new Base32InputStream(in, false);
        assertEquals(-1, stream.read());
        stream.close();
    }

    @Test
    public void testReadMultipleBytesEncodingLoop() throws IOException {
        // encoding a string that produces multiple lines of encoded data to exercise multiple reads
        String input = "HelloWorld12345"; // some length > 1
        byte[] raw = input.getBytes("US-ASCII");
        InputStream in = new ByteArrayInputStream(raw);
        BaseNCodecInputStream stream = new Base32InputStream(in, true);
        // Read all bytes and verify they are valid Base32 encoded chars (no implementation check)
        byte[] out = new byte[256];
        int total = 0;
        int r;
        while ((r = stream.read()) != -1) {
            assertTrue("Encoded byte should be printable ASCII", r >= 32 && r < 127);
            total++;
        }
        assertTrue("Should have read some encoded data", total > 0);
        stream.close();
    }

    @Test
    public void testReadLargeBufferDecoding() throws IOException {
        // Decode a valid encoded stream of multiple chunks to cover the internal buffer logic
        String encoded = "JBSWY3DPEBLW64TMMQ======"; // "Hello World" in Base32
        byte[] encBytes = encoded.getBytes("US-ASCII");
        InputStream in = new ByteArrayInputStream(encBytes);
        BaseNCodecInputStream stream = new Base32InputStream(in, false);
        byte[] buf = new byte[50];
        int total = stream.read(buf, 0, 50);
        assertEquals("Hello World", new String(buf, 0, total, "US-ASCII"));
        stream.close();
    }
}
