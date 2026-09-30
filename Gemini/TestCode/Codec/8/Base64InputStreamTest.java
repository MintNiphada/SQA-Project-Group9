package org.apache.commons.codec.binary;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class Base64InputStreamTest {

    private static final String STRING_TO_ENCODE = "Hello World! Testing Base64InputStream with various inputs.";
    private static final String ENCODED_STRING = "SGVsbG8gV29ybGQhIFRlc3RpbmcgQmFzZTY0SW5wdXRTdHJlYW0gd2l0aCB2YXJpb3VzIGlucHV0cy4=";

    @Test
    public void testConstructors() {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        Base64InputStream in1 = new Base64InputStream(bais);
        Assert.assertFalse(in1.markSupported());

        Base64InputStream in2 = new Base64InputStream(bais, true);
        Assert.assertFalse(in2.markSupported());

        Base64InputStream in3 = new Base64InputStream(bais, true, 76, new byte[]{'\r', '\n'});
        Assert.assertFalse(in3.markSupported());
    }

    @Test
    public void testDecodeSingleByte() throws IOException {
        byte[] encodedBytes = StringUtils.getBytesUtf8(ENCODED_STRING);
        ByteArrayInputStream bais = new ByteArrayInputStream(encodedBytes);
        Base64InputStream in = new Base64InputStream(bais);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int b;
        while ((b = in.read()) != -1) {
            out.write(b);
        }
        in.close();

        String decoded = StringUtils.newStringUtf8(out.toByteArray());
        Assert.assertEquals(STRING_TO_ENCODE, decoded);
    }

    @Test
    public void testEncodeSingleByte() throws IOException {
        byte[] rawBytes = StringUtils.getBytesUtf8(STRING_TO_ENCODE);
        ByteArrayInputStream bais = new ByteArrayInputStream(rawBytes);
        Base64InputStream in = new Base64InputStream(bais, true, 0, null);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int b;
        while ((b = in.read()) != -1) {
            out.write(b);
        }
        in.close();

        String encoded = StringUtils.newStringUtf8(out.toByteArray());
        Assert.assertEquals(ENCODED_STRING, encoded);
    }

    @Test
    public void testDecodeBuffer() throws IOException {
        byte[] encodedBytes = StringUtils.getBytesUtf8(ENCODED_STRING);
        ByteArrayInputStream bais = new ByteArrayInputStream(encodedBytes);
        Base64InputStream in = new Base64InputStream(bais);

        byte[] buf = new byte[16];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int bytesRead;
        while ((bytesRead = in.read(buf, 0, buf.length)) != -1) {
            out.write(buf, 0, bytesRead);
        }
        in.close();

        String decoded = StringUtils.newStringUtf8(out.toByteArray());
        Assert.assertEquals(STRING_TO_ENCODE, decoded);
    }

    @Test
    public void testEncodeBuffer() throws IOException {
        byte[] rawBytes = StringUtils.getBytesUtf8(STRING_TO_ENCODE);
        ByteArrayInputStream bais = new ByteArrayInputStream(rawBytes);
        Base64InputStream in = new Base64InputStream(bais, true, 0, null);

        byte[] buf = new byte[16];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int bytesRead;
        while ((bytesRead = in.read(buf, 0, buf.length)) != -1) {
            out.write(buf, 0, bytesRead);
        }
        in.close();

        String encoded = StringUtils.newStringUtf8(out.toByteArray());
        Assert.assertEquals(ENCODED_STRING, encoded);
    }

    @Test
    public void testEncodeWithLineBreaks() throws IOException {
        byte[] rawBytes = new byte[100];
        for (int i = 0; i < rawBytes.length; i++) {
            rawBytes[i] = (byte) i;
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(rawBytes);
        Base64InputStream in = new Base64InputStream(bais, true, 64, new byte[]{'\n'});

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[32];
        int bytesRead;
        while ((bytesRead = in.read(buf)) != -1) {
            out.write(buf, 0, bytesRead);
        }
        in.close();

        byte[] expected = Base64.encodeBase64(rawBytes, true);
        // Base64.encodeBase64(chunked=true) uses 76 and \r\n, so let's compare with customized Base64 instance
        Base64 b64 = new Base64(64, new byte[]{'\n'});
        byte[] customExpected = b64.encode(rawBytes);
        Assert.assertArrayEquals(customExpected, out.toByteArray());
    }

    @Test
    public void testReadReturnsNegativeSingleByte() throws IOException {
        // Test byte value > 127 (signed negative byte value) to ensure range 0-255 is returned correctly
        byte[] binaryData = new byte[]{(byte) 0xFF, (byte) 0x80, (byte) 0xFE};
        byte[] encodedData = Base64.encodeBase64(binaryData);

        ByteArrayInputStream bais = new ByteArrayInputStream(encodedData);
        Base64InputStream in = new Base64InputStream(bais);

        int b1 = in.read();
        int b2 = in.read();
        int b3 = in.read();
        int b4 = in.read();
        in.close();

        Assert.assertEquals(255, b1);
        Assert.assertEquals(128, b2);
        Assert.assertEquals(254, b3);
        Assert.assertEquals(-1, b4);
    }

    @Test
    public void testReadZeroBytes() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[]{1, 2, 3});
        Base64InputStream in = new Base64InputStream(bais);

        byte[] buf = new byte[10];
        int bytesRead = in.read(buf, 0, 0);
        Assert.assertEquals(0, bytesRead);
        in.close();
    }

    @Test(expected = NullPointerException.class)
    public void testReadNullBuffer() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[]{1, 2, 3});
        Base64InputStream in = new Base64InputStream(bais);
        try {
            in.read(null, 0, 1);
        } finally {
            in.close();
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadNegativeOffset() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[]{1, 2, 3});
        Base64InputStream in = new Base64InputStream(bais);
        try {
            in.read(new byte[10], -1, 1);
        } finally {
            in.close();
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadNegativeLength() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[]{1, 2, 3});
        Base64InputStream in = new Base64InputStream(bais);
        try {
            in.read(new byte[10], 0, -1);
        } finally {
            in.close();
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadOffsetGreaterThanLength() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[]{1, 2, 3});
        Base64InputStream in = new Base64InputStream(bais);
        try {
            in.read(new byte[10], 11, 0);
        } finally {
            in.close();
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadOffsetPlusLenGreaterThanLength() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[]{1, 2, 3});
        Base64InputStream in = new Base64InputStream(bais);
        try {
            in.read(new byte[10], 5, 6);
        } finally {
            in.close();
        }
    }

    @Test
    public void testReadEmptyStream() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        Base64InputStream in = new Base64InputStream(bais);

        Assert.assertEquals(-1, in.read());
        byte[] buf = new byte[10];
        Assert.assertEquals(-1, in.read(buf, 0, 10));
        in.close();
    }

    @Test
    public void testReadEmptyStreamEncode() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        Base64InputStream in = new Base64InputStream(bais, true);

        Assert.assertEquals(-1, in.read());
        byte[] buf = new byte[10];
        Assert.assertEquals(-1, in.read(buf, 0, 10));
        in.close();
    }

    @Test
    public void testInitialBufferOptimizationDecode() throws IOException {
        byte[] encodedBytes = StringUtils.getBytesUtf8("AAAA");
        ByteArrayInputStream bais = new ByteArrayInputStream(encodedBytes);
        Base64InputStream in = new Base64InputStream(bais);

        // len == b.length triggers base64.setInitialBuffer(b, offset, len)
        byte[] buf = new byte[3];
        int readLen = in.read(buf, 0, 3);
        Assert.assertEquals(3, readLen);
        Assert.assertArrayEquals(new byte[]{0, 0, 0}, buf);
        in.close();
    }

    @Test
    public void testInitialBufferOptimizationEncode() throws IOException {
        byte[] rawBytes = new byte[]{0, 0, 0};
        ByteArrayInputStream bais = new ByteArrayInputStream(rawBytes);
        Base64InputStream in = new Base64InputStream(bais, true, 0, null);

        // len == b.length triggers base64.setInitialBuffer(b, offset, len)
        byte[] buf = new byte[4];
        int readLen = in.read(buf, 0, 4);
        Assert.assertEquals(4, readLen);
        Assert.assertArrayEquals(StringUtils.getBytesUtf8("AAAA"), buf);
        in.close();
    }

    @Test
    public void testDecodeWithIgnoredCharacters() throws IOException {
        // CODEC-101 scenario: Ignored characters / spaces
        String inputWithSpaces = "   \n\r\t  SGVs   \n\r  bG8=   \r\n  ";
        ByteArrayInputStream bais = new ByteArrayInputStream(StringUtils.getBytesUtf8(inputWithSpaces));
        Base64InputStream in = new Base64InputStream(bais);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[2];
        int c;
        while ((c = in.read(buf, 0, buf.length)) != -1) {
            out.write(buf, 0, c);
        }
        in.close();

        Assert.assertEquals("Hello", StringUtils.newStringUtf8(out.toByteArray()));
    }

    @Test
    public void testLargeStreamEncodeAndDecode() throws IOException {
        byte[] largeData = new byte[16384];
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte) (i % 256);
        }

        ByteArrayInputStream rawIn = new ByteArrayInputStream(largeData);
        Base64InputStream encodeIn = new Base64InputStream(rawIn, true);

        ByteArrayOutputStream encodedOut = new ByteArrayOutputStream();
        byte[] buf = new byte[512];
        int r;
        while ((r = encodeIn.read(buf)) != -1) {
            encodedOut.write(buf, 0, r);
        }
        encodeIn.close();

        ByteArrayInputStream encodedIn = new ByteArrayInputStream(encodedOut.toByteArray());
        Base64InputStream decodeIn = new Base64InputStream(encodedIn, false);

        ByteArrayOutputStream decodedOut = new ByteArrayOutputStream();
        while ((r = decodeIn.read(buf)) != -1) {
            decodedOut.write(buf, 0, r);
        }
        decodeIn.close();

        Assert.assertArrayEquals(largeData, decodedOut.toByteArray());
    }
}
