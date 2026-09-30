package org.apache.commons.codec.binary;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class Base64InputStreamTest {

    private byte[] readAllBytes(InputStream in) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        byte[] tmp = new byte[1024];
        int n;
        while ((n = in.read(tmp)) != -1) {
            buf.write(tmp, 0, n);
        }
        return buf.toByteArray();
    }

    @Test
    public void testConstructorDefaultIsDecode() throws Exception {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        Base64InputStream stream = new Base64InputStream(in);
        assertNotNull(stream);
        // Verify it is decode by reading encoded data
        byte[] encoded = Base64.encodeBase64("test".getBytes("UTF-8"));
        stream = new Base64InputStream(new ByteArrayInputStream(encoded));
        byte[] result = readAllBytes(stream);
        assertArrayEquals("test".getBytes("UTF-8"), result);
    }

    @Test
    public void testConstructorEncodeFlag() throws Exception {
        InputStream in = new ByteArrayInputStream("input".getBytes("UTF-8"));
        Base64InputStream stream = new Base64InputStream(in, true);
        byte[] encoded = readAllBytes(stream);
        byte[] expected = Base64.encodeBase64("input".getBytes("UTF-8"));
        assertArrayEquals(expected, encoded);
    }

    @Test
    public void testConstructorWithLineLengthAndSeparator() throws Exception {
        byte[] raw = "Hello World".getBytes("UTF-8");
        Base64 base64Line = new Base64(10, new byte[]{'\r', '\n'});
        byte[] expected = base64Line.encodeBase64(raw);
        InputStream in = new ByteArrayInputStream(raw);
        Base64InputStream stream = new Base64InputStream(in, true, 10, new byte[]{'\r', '\n'});
        byte[] result = readAllBytes(stream);
        assertArrayEquals(expected, result);
    }

    @Test
    public void testConstructorLineLengthZeroNoLines() throws Exception {
        byte[] raw = "Test".getBytes("UTF-8");
        Base64 base64 = new Base64(false); // default no line breaks
        byte[] expected = base64.encodeBase64(raw);
        InputStream in = new ByteArrayInputStream(raw);
        Base64InputStream stream = new Base64InputStream(in, true, 0, new byte[]{'\r', '\n'});
        byte[] result = readAllBytes(stream);
        assertArrayEquals(expected, result);
    }

    @Test
    public void testReadSingleByteDecode() throws Exception {
        byte[] data = "Hello".getBytes("UTF-8");
        byte[] encoded = Base64.encodeBase64(data);
        Base64InputStream stream = new Base64InputStream(new ByteArrayInputStream(encoded));
        for (byte b : data) {
            int value = stream.read();
            assertTrue("Expected single byte >=0", value >= 0);
            assertEquals(b & 0xFF, value);
        }
        assertEquals(-1, stream.read());
    }

    @Test
    public void testReadSingleByteEncode() throws Exception {
        byte[] raw = "World".getBytes("UTF-8");
        byte[] expected = Base64.encodeBase64(raw);
        Base64InputStream stream = new Base64InputStream(new ByteArrayInputStream(raw), true);
        for (byte b : expected) {
            assertEquals(b & 0xFF, stream.read());
        }
        assertEquals(-1, stream.read());
    }

    @Test
    public void testReadSingleByteAfterDecodingToByteWithHighBit() throws Exception {
        // A byte with value 0x80 (128) signed is -128, read() should return 128
        byte[] raw = {(byte) 0x80, (byte) 0xFF};
        byte[] encoded = Base64.encodeBase64(raw);
        Base64InputStream stream = new Base64InputStream(new ByteArrayInputStream(encoded));
        assertEquals(128, stream.read()); // 0x80
        assertEquals(255, stream.read()); // 0xFF
        assertEquals(-1, stream.read());
    }

    @Test
    public void testReadSingleByteOnlyInvalidCharacters() throws Exception {
        // Stream of invalid base64 characters, decoding produces nothing, read() must eventually return -1
        byte[] invalid = "!@#$%^&*()".getBytes("UTF-8");
        ByteArrayInputStream bais = new ByteArrayInputStream(invalid);
        Base64InputStream stream = new Base64InputStream(bais);
        int result = stream.read();
        // It should loop over zero-returns and finally -1 when EOF
        assertEquals(-1, result);
    }

    @Test
    public void testReadArrayDecode() throws Exception {
        byte[] data = "Testing read(byte[],off,len)".getBytes("UTF-8");
        byte[] encoded = Base64.encodeBase64(data);
        Base64InputStream stream = new Base64InputStream(new ByteArrayInputStream(encoded));
        byte[] buf = new byte[data.length];
        int read = stream.read(buf, 0, buf.length);
        assertEquals(data.length, read);
        assertArrayEquals(data, buf);
        assertEquals(-1, stream.read());
    }

    @Test
    public void testReadArrayEncode() throws Exception {
        byte[] raw = "Encoding test".getBytes("UTF-8");
        byte[] expectedEncoded = Base64.encodeBase64(raw);
        Base64InputStream stream = new Base64InputStream(new ByteArrayInputStream(raw), true);
        byte[] buf = new byte[expectedEncoded.length];
        int read = stream.read(buf, 0, buf.length);
        assertEquals(expectedEncoded.length, read);
        assertArrayEquals(expectedEncoded, buf);
        assertEquals(-1, stream.read());
    }

    @Test(expected = NullPointerException.class)
    public void testReadNullBufferThrowsNullPointerException() throws IOException {
        Base64InputStream stream = new Base64InputStream(new ByteArrayInputStream(new byte[0]));
        stream.read(null, 0, 1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadNegativeOffsetThrowsIndexOutOfBounds() throws IOException {
        Base64InputStream stream = new Base64InputStream(new ByteArrayInputStream(new byte[0]));
        stream.read(new byte[1], -1, 1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadNegativeLengthThrowsIndexOutOfBounds() throws IOException {
        Base64InputStream stream = new Base64InputStream(new ByteArrayInputStream(new byte[0]));
        stream.read(new byte[1], 0, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadOffsetPlusLengthBeyondArrayThrowsIndexOutOfBounds() throws IOException {
        Base64InputStream stream = new Base64InputStream(new ByteArrayInputStream(new byte[0]));
        stream.read(new byte[5], 3, 3);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadOffsetBeyondArrayThrowsIndexOutOfBounds() throws IOException {
        Base64InputStream stream = new Base64InputStream(new ByteArrayInputStream(new byte[0]));
        stream.read(new byte[5], 6, 0);
    }

    @Test
    public void testReadLengthZeroReturnsZero() throws IOException {
        Base64InputStream stream = new Base64InputStream(new ByteArrayInputStream("data".getBytes()), true);
        assertEquals(0, stream.read(new byte[10], 5, 0));
    }

    @Test
    public void testReadPartialBufferAndSubsequentReadUsesInternalBuffer() throws Exception {
        // Provide encoded data that decodes to 10 bytes, read 5 then 5
        byte[] raw = new byte[10];
        for (int i = 0; i < raw.length; i++) raw[i] = (byte) i;
        byte[] encoded = Base64.encodeBase64(raw);
        Base64InputStream stream = new Base64InputStream(new ByteArrayInputStream(encoded));
        byte[] first = new byte[5];
        int r1 = stream.read(first, 0, 5);
        assertEquals(5, r1);
        byte[] second = new byte[5];
        int r2 = stream.read(second, 0, 5);
        assertEquals(5, r2);
        byte[] combined = new byte[10];
        System.arraycopy(first, 0, combined, 0, 5);
        System.arraycopy(second, 0, combined, 5, 5);
        assertArrayEquals(raw, combined);
        assertEquals(-1, stream.read());
    }

    @Test
    public void testOptimizationSetInitialBufferActivated() throws Exception {
        // len == b.length and underlying read returns > 0
        byte[] raw = "Optimize test".getBytes("UTF-8");
        byte[] encoded = Base64.encodeBase64(raw);
        Base64InputStream stream = new Base64InputStream(new ByteArrayInputStream(encoded));
        byte[] buf = new byte[raw.length]; // b.length == len (when we call read with len = raw.length)
        int read = stream.read(buf, 0, raw.length);
        assertEquals(raw.length, read);
        assertArrayEquals(raw, buf);
    }

    @Test
    public void testUnderlyingStreamThrowsIOExceptionOnRead() throws Exception {
        InputStream badStream = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("fail");
            }
            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                throw new IOException("fail");
            }
        };
        Base64InputStream stream = new Base64InputStream(badStream, false);
        try {
            stream.read();
            fail("Expected IOException");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("fail"));
        }
        try {
            stream.read(new byte[10], 0, 10);
            fail("Expected IOException");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("fail"));
        }
    }

    @Test
    public void testMarkSupportedReturnsFalse() {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        Base64InputStream stream = new Base64InputStream(in);
        assertFalse(stream.markSupported());
    }

    @Test
    public void testReadEndOfStreamImmediately() throws Exception {
        Base64InputStream stream = new Base64InputStream(new ByteArrayInputStream(new byte[0]));
        assertEquals(-1, stream.read());
        byte[] buf = new byte[10];
        assertEquals(-1, stream.read(buf, 0, 10));
    }

    @Test
    public void testEncodeWithEmptyRawData() throws Exception {
        byte[] raw = new byte[0];
        Base64InputStream stream = new Base64InputStream(new ByteArrayInputStream(raw), true);
        byte[] result = readAllBytes(stream);
        assertEquals(0, result.length);
    }

    @Test
    public void testDecodeWithEmptyEncodedData() throws Exception {
        byte[] encoded = new byte[0];
        Base64InputStream stream = new Base64InputStream(new ByteArrayInputStream(encoded), false);
        byte[] result = readAllBytes(stream);
        assertEquals(0, result.length);
    }
}
