package org.apache.commons.codec.binary;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class Base64InputStreamTest {

    private static final String STRING_FIXTURE = "Hello World";
    private static final byte[] CRLF = new byte[] { '\r', '\n' };

    @Test
    public void testMarkSupported() {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        Base64InputStream in = new Base64InputStream(bais);
        Assert.assertFalse(in.markSupported());
    }

    @Test
    public void testCodec101() throws Exception {
        byte[] input = StringUtils.getBytesUtf8("SGVsbG8gV29ybGQ=");
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(input));
        byte[] result = new byte[input.length];
        int ret = in.read(result, 0, result.length);
        Assert.assertEquals(11, ret);
        Assert.assertEquals("Hello World", StringUtils.newStringUtf8(Arrays.copyOf(result, ret)));
        in.close();
    }

    @Test
    public void testInputStreamReaderInterop() throws Exception {
        byte[] encoded = Base64.encodeBase64(StringUtils.getBytesUtf8(STRING_FIXTURE));
        ByteArrayInputStream bais = new ByteArrayInputStream(encoded);
        Base64InputStream in = new Base64InputStream(bais);

        byte[] buf = new byte[1024];
        int bytesRead = in.read(buf, 0, buf.length);
        Assert.assertEquals(STRING_FIXTURE, StringUtils.newStringUtf8(Arrays.copyOf(buf, bytesRead)));
        in.close();
    }

    @Test
    public void testEncodeDecodeSingleByteRead() throws IOException {
        byte[] rawData = new byte[] { (byte) 0xFF, (byte) 0xFE, (byte) 0xFD, 0x00, 0x01, 0x02, (byte) 0x80 };
        ByteArrayInputStream bais = new ByteArrayInputStream(rawData);
        Base64InputStream encodeIn = new Base64InputStream(bais, true);

        // Read single bytes encoded
        byte[] encodedData = new byte[100];
        int encLen = 0;
        int b;
        while ((b = encodeIn.read()) != -1) {
            encodedData[encLen++] = (byte) b;
        }
        encodeIn.close();

        byte[] trimmedEncoded = Arrays.copyOf(encodedData, encLen);
        Assert.assertArrayEquals(Base64.encodeBase64(rawData), trimmedEncoded);

        // Decode single bytes
        Base64InputStream decodeIn = new Base64InputStream(new ByteArrayInputStream(trimmedEncoded), false);
        byte[] decodedData = new byte[100];
        int decLen = 0;
        while ((b = decodeIn.read()) != -1) {
            decodedData[decLen++] = (byte) b;
        }
        decodeIn.close();

        byte[] trimmedDecoded = Arrays.copyOf(decodedData, decLen);
        Assert.assertArrayEquals(rawData, trimmedDecoded);
    }

    @Test
    public void testEncodeDecodeChunked() throws IOException {
        byte[] rawData = StringUtils.getBytesUtf8("A very long string that should span multiple lines when encoded with line length configuration.");
        Base64InputStream encodeIn = new Base64InputStream(new ByteArrayInputStream(rawData), true, 16, CRLF);

        byte[] encoded = new byte[1024];
        int totalRead = 0;
        int read;
        while ((read = encodeIn.read(encoded, totalRead, encoded.length - totalRead)) > 0) {
            totalRead += read;
        }
        encodeIn.close();

        byte[] expectedEncoded = Base64.encodeBase64Chunked(rawData); // Note: chunked uses 76 by default, custom uses 16
        Base64 b64 = new Base64(16, CRLF);
        Assert.assertArrayEquals(b64.encode(rawData), Arrays.copyOf(encoded, totalRead));

        Base64InputStream decodeIn = new Base64InputStream(new ByteArrayInputStream(Arrays.copyOf(encoded, totalRead)), false, 16, CRLF);
        byte[] decoded = new byte[1024];
        int totalDecoded = 0;
        while ((read = decodeIn.read(decoded, totalDecoded, decoded.length - totalDecoded)) > 0) {
            totalDecoded += read;
        }
        decodeIn.close();

        Assert.assertArrayEquals(rawData, Arrays.copyOf(decoded, totalDecoded));
    }

    @Test
    public void testRead0Length() throws IOException {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(new byte[10]));
        byte[] b = new byte[10];
        int read = in.read(b, 0, 0);
        Assert.assertEquals(0, read);
        in.close();
    }

    @Test(expected = NullPointerException.class)
    public void testReadNullArray() throws IOException {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(new byte[10]));
        try {
            in.read(null, 0, 1);
        } finally {
            in.close();
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadNegativeOffset() throws IOException {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(new byte[10]));
        try {
            in.read(new byte[10], -1, 1);
        } finally {
            in.close();
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadNegativeLength() throws IOException {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(new byte[10]));
        try {
            in.read(new byte[10], 0, -1);
        } finally {
            in.close();
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadOffsetGreaterThanLength() throws IOException {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(new byte[10]));
        try {
            in.read(new byte[10], 11, 0);
        } finally {
            in.close();
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadOffsetPlusLengthGreaterThanLength() throws IOException {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(new byte[10]));
        try {
            in.read(new byte[10], 5, 6);
        } finally {
            in.close();
        }
    }

    @Test
    public void testReadEmptyStream() throws IOException {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(new byte[0]));
        Assert.assertEquals(-1, in.read());
        byte[] buf = new byte[10];
        Assert.assertEquals(-1, in.read(buf, 0, 10));
        in.close();
    }

    @Test
    public void testReadEmptyStreamEncode() throws IOException {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(new byte[0]), true);
        Assert.assertEquals(-1, in.read());
        byte[] buf = new byte[10];
        Assert.assertEquals(-1, in.read(buf, 0, 10));
        in.close();
    }

    @Test
    public void testReadWithOptimizationPath() throws IOException {
        // Optimization path: c > 0 && b.length == len
        byte[] raw = StringUtils.getBytesUtf8("Hello World");
        byte[] encoded = Base64.encodeBase64(raw);

        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(encoded), false);
        byte[] dest = new byte[raw.length];
        int count = 0;
        int r;
        while ((r = in.read(dest, count, dest.length - count)) > 0) {
            count += r;
        }
        Assert.assertEquals(raw.length, count);
        Assert.assertArrayEquals(raw, dest);
        in.close();
    }

    @Test
    public void testReadWithNonBase64Ignored() throws IOException {
        // Spaces and non-base64 characters in input stream should be ignored by decoder
        byte[] input = StringUtils.getBytesUtf8("  S  G  V  s  b  G  8  g  V  2  9  y  b  G  Q  = \r\n");
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(input));
        byte[] output = new byte[100];
        int count = 0;
        int r;
        while ((r = in.read(output, count, output.length - count)) > 0) {
            count += r;
        }
        Assert.assertEquals("Hello World", StringUtils.newStringUtf8(Arrays.copyOf(output, count)));
        in.close();
    }

    @Test
    public void testSingleByteReadLoopZeroHandling() throws IOException {
        // Stream returning non-base64 chars causing readResults to return 0 initially
        InputStream dummy = new InputStream() {
            private final byte[] data = new byte[] { ' ', ' ', ' ', 'A', 'A', '=', '=' };
            private int idx = 0;

            @Override
            public int read() {
                if (idx < data.length) {
                    return data[idx++];
                }
                return -1;
            }

            @Override
            public int read(byte[] b, int off, int len) {
                if (idx >= data.length) {
                    return -1;
                }
                int count = Math.min(len, data.length - idx);
                System.arraycopy(data, idx, b, off, count);
                idx += count;
                return count;
            }
        };

        Base64InputStream in = new Base64InputStream(dummy, false);
        int b = in.read();
        Assert.assertEquals(0, b);
        Assert.assertEquals(-1, in.read());
        in.close();
    }

    @Test
    public void testLargeDataEncodingDecoding() throws IOException {
        byte[] largeData = new byte[16384];
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte) (i % 256);
        }

        // Encode
        Base64InputStream encodeIn = new Base64InputStream(new ByteArrayInputStream(largeData), true);
        byte[] encoded = new byte[32768];
        int totalEncoded = 0;
        int read;
        while ((read = encodeIn.read(encoded, totalEncoded, encoded.length - totalEncoded)) > 0) {
            totalEncoded += read;
        }
        encodeIn.close();

        // Decode
        Base64InputStream decodeIn = new Base64InputStream(new ByteArrayInputStream(Arrays.copyOf(encoded, totalEncoded)), false);
        byte[] decoded = new byte[32768];
        int totalDecoded = 0;
        while ((read = decodeIn.read(decoded, totalDecoded, decoded.length - totalDecoded)) > 0) {
            totalDecoded += read;
        }
        decodeIn.close();

        Assert.assertEquals(largeData.length, totalDecoded);
        Assert.assertArrayEquals(largeData, Arrays.copyOf(decoded, totalDecoded));
    }
}
