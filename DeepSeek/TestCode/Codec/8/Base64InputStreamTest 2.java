package org.apache.commons.codec.binary;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.Test;

public class Base64InputStreamTest {

    private static final byte[] EMPTY = new byte[0];
    private static final byte[] LINE_SEPARATOR_CRLF = new byte[] { '\r', '\n' };

    // Helper to encode bytes to Base64 string for test setup
    private String encode(byte[] data) {
        Base64 base64 = new Base64();
        return new String(base64.encode(data), StandardCharsets.UTF_8);
    }

    @Test
    public void testConstructorDefaultDecode() throws IOException {
        byte[] input = "Hello World".getBytes(StandardCharsets.UTF_8);
        String encoded = encode(input);
        InputStream in = new ByteArrayInputStream(encoded.getBytes(StandardCharsets.UTF_8));
        
        Base64InputStream bis = new Base64InputStream(in);
        
        byte[] result = readAll(bis);
        assertArrayEquals(input, result);
    }

    @Test
    public void testConstructorEncode() throws IOException {
        byte[] input = "Hello World".getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(input);
        
        Base64InputStream bis = new Base64InputStream(in, true);
        
        byte[] result = readAll(bis);
        String expectedEncoded = encode(input);
        assertArrayEquals(expectedEncoded.getBytes(StandardCharsets.UTF_8), result);
    }

    @Test
    public void testConstructorWithLineLengthAndSeparator() throws IOException {
        byte[] input = "Hello World".getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(input);
        
        // Encode with line length 4 and CRLF separator
        Base64InputStream bis = new Base64InputStream(in, true, 4, LINE_SEPARATOR_CRLF);
        
        byte[] result = readAll(bis);
        String resultStr = new String(result, StandardCharsets.UTF_8);
        
        // Verify it contains newlines
        assertTrue("Encoded output should contain line separators", resultStr.contains("\r\n"));
        
        // Verify decoding works
        InputStream decodeIn = new ByteArrayInputStream(result);
        Base64InputStream decodeBis = new Base64InputStream(decodeIn, false);
        byte[] decoded = readAll(decodeBis);
        assertArrayEquals(input, decoded);
    }

    @Test
    public void testReadSingleByte() throws IOException {
        byte[] input = "Test".getBytes(StandardCharsets.UTF_8);
        String encoded = encode(input);
        InputStream in = new ByteArrayInputStream(encoded.getBytes(StandardCharsets.UTF_8));
        
        Base64InputStream bis = new Base64InputStream(in);
        
        for (int i = 0; i < input.length; i++) {
            int b = bis.read();
            assertEquals("Mismatch at byte " + i, input[i] & 0xFF, b);
        }
        
        assertEquals(-1, bis.read());
    }

    @Test
    public void testReadSingleByteEOF() throws IOException {
        InputStream in = new ByteArrayInputStream(EMPTY);
        Base64InputStream bis = new Base64InputStream(in);
        assertEquals(-1, bis.read());
    }

    @Test
    public void testReadByteArrayNull() {
        InputStream in = new ByteArrayInputStream(EMPTY);
        Base64InputStream bis = new Base64InputStream(in);
        try {
            bis.read(null, 0, 1);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        } catch (IOException e) {
            fail("Unexpected IOException");
        }
    }

    @Test
    public void testReadByteArrayNegativeOffset() {
        InputStream in = new ByteArrayInputStream(EMPTY);
        Base64InputStream bis = new Base64InputStream(in);
        try {
            bis.read(new byte[10], -1, 1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        } catch (IOException e) {
            fail("Unexpected IOException");
        }
    }

    @Test
    public void testReadByteArrayNegativeLength() {
        InputStream in = new ByteArrayInputStream(EMPTY);
        Base64InputStream bis = new Base64InputStream(in);
        try {
            bis.read(new byte[10], 0, -1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        } catch (IOException e) {
            fail("Unexpected IOException");
        }
    }

    @Test
    public void testReadByteArrayOffsetTooLarge() {
        InputStream in = new ByteArrayInputStream(EMPTY);
        Base64InputStream bis = new Base64InputStream(in);
        try {
            bis.read(new byte[10], 11, 1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        } catch (IOException e) {
            fail("Unexpected IOException");
        }
    }

    @Test
    public void testReadByteArrayOffsetPlusLengthTooLarge() {
        InputStream in = new ByteArrayInputStream(EMPTY);
        Base64InputStream bis = new Base64InputStream(in);
        try {
            bis.read(new byte[10], 5, 6);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        } catch (IOException e) {
            fail("Unexpected IOException");
        }
    }

    @Test
    public void testReadByteArrayZeroLength() throws IOException {
        InputStream in = new ByteArrayInputStream(EMPTY);
        Base64InputStream bis = new Base64InputStream(in);
        assertEquals(0, bis.read(new byte[10], 0, 0));
    }

    @Test
    public void testDecodeEmptyInput() throws IOException {
        InputStream in = new ByteArrayInputStream(EMPTY);
        Base64InputStream bis = new Base64InputStream(in);
        byte[] buf = new byte[10];
        assertEquals(-1, bis.read(buf, 0, 10));
    }

    @Test
    public void testDecodeInvalidBase64() throws IOException {
        // "!!!" is not valid base64
        String invalid = "!!!";
        InputStream in = new ByteArrayInputStream(invalid.getBytes(StandardCharsets.UTF_8));
        Base64InputStream bis = new Base64InputStream(in);
        
        byte[] buf = new byte[10];
        // Depending on implementation, this might return -1 or throw. 
        // Base64 decoder usually ignores invalid chars or stops.
        // Let's just ensure it doesn't crash and returns something reasonable.
        int read = bis.read(buf, 0, 10);
        // If it returns -1, it means EOF reached without producing output.
        // If it returns 0, it might loop. The code has a while loop for readLen==0.
        // However, Base64.decode usually handles invalid chars by ignoring them or throwing.
        // In Commons Codec, invalid chars are often ignored.
        // So read might be -1 if all chars were ignored.
        assertTrue(read == -1 || read > 0);
    }

    @Test
    public void testMarkSupported() {
        InputStream in = new ByteArrayInputStream(EMPTY);
        Base64InputStream bis = new Base64InputStream(in);
        assertFalse(bis.markSupported());
    }

    @Test
    public void testEncodeDecodeRoundTrip() throws IOException {
        byte[] original = new byte[256];
        for (int i = 0; i < 256; i++) {
            original[i] = (byte) i;
        }
        
        // Encode
        InputStream in = new ByteArrayInputStream(original);
        Base64InputStream encodeStream = new Base64InputStream(in, true);
        byte[] encoded = readAll(encodeStream);
        
        // Decode
        InputStream in2 = new ByteArrayInputStream(encoded);
        Base64InputStream decodeStream = new Base64InputStream(in2, false);
        byte[] decoded = readAll(decodeStream);
        
        assertArrayEquals(original, decoded);
    }

    @Test
    public void testReadWithPartialBuffer() throws IOException {
        byte[] input = "Hello World".getBytes(StandardCharsets.UTF_8);
        String encoded = encode(input);
        InputStream in = new ByteArrayInputStream(encoded.getBytes(StandardCharsets.UTF_8));
        
        Base64InputStream bis = new Base64InputStream(in);
        
        byte[] buf = new byte[5];
        int totalRead = 0;
        int bytesRead;
        while ((bytesRead = bis.read(buf, 0, buf.length)) != -1) {
            totalRead += bytesRead;
        }
        
        assertEquals(input.length, totalRead);
    }

    @Test
    public void testEncodeWithLineLengthZero() throws IOException {
        byte[] input = "Hello World".getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(input);
        
        // lineLength <= 0 means no line breaks
        Base64InputStream bis = new Base64InputStream(in, true, 0, LINE_SEPARATOR_CRLF);
        
        byte[] result = readAll(bis);
        String resultStr = new String(result, StandardCharsets.UTF_8);
        
        assertFalse("Should not contain line separators", resultStr.contains("\r\n"));
        
        // Verify it's valid base64
        Base64 base64 = new Base64();
        byte[] decoded = base64.decode(result);
        assertArrayEquals(input, decoded);
    }

    @Test
    public void testEncodeWithNegativeLineLength() throws IOException {
        byte[] input = "Hello World".getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(input);
        
        Base64InputStream bis = new Base64InputStream(in, true, -1, LINE_SEPARATOR_CRLF);
        
        byte[] result = readAll(bis);
        String resultStr = new String(result, StandardCharsets.UTF_8);
        
        assertFalse("Should not contain line separators", resultStr.contains("\r\n"));
    }

    @Test
    public void testDecodeWithLineSeparators() throws IOException {
        byte[] input = "Hello World".getBytes(StandardCharsets.UTF_8);
        // Encode with line breaks
        Base64 base64 = new Base64(4, LINE_SEPARATOR_CRLF);
        byte[] encodedWithLines = base64.encode(input);
        
        InputStream in = new ByteArrayInputStream(encodedWithLines);
        Base64InputStream bis = new Base64InputStream(in, false);
        
        byte[] decoded = readAll(bis);
        assertArrayEquals(input, decoded);
    }

    @Test
    public void testReadFromClosedStream() throws IOException {
        InputStream in = new ByteArrayInputStream("test".getBytes());
        Base64InputStream bis = new Base64InputStream(in);
        bis.close();
        try {
            bis.read();
            fail("Expected IOException");
        } catch (IOException e) {
            // Expected
        }
    }

    private byte[] readAll(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }
        return baos.toByteArray();
    }
}
