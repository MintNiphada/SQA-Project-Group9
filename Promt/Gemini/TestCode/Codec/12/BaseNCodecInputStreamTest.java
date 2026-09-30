package org.apache.commons.codec.binary;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class BaseNCodecInputStreamTest {

    private static class TestBaseNCodecInputStream extends BaseNCodecInputStream {
        protected TestBaseNCodecInputStream(InputStream in, BaseNCodec baseNCodec, boolean doEncode) {
            super(in, baseNCodec, doEncode);
        }
    }

    @Test
    public void testMarkSupported() {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        BaseNCodec codec = new Base64();
        BaseNCodecInputStream stream = new TestBaseNCodecInputStream(in, codec, true);
        Assert.assertFalse(stream.markSupported());
    }

    @Test(expected = NullPointerException.class)
    public void testReadNullBuffer() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[10]);
        BaseNCodec codec = new Base64();
        BaseNCodecInputStream stream = new TestBaseNCodecInputStream(in, codec, true);
        stream.read(null, 0, 1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadNegativeOffset() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[10]);
        BaseNCodec codec = new Base64();
        BaseNCodecInputStream stream = new TestBaseNCodecInputStream(in, codec, true);
        stream.read(new byte[10], -1, 1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadNegativeLength() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[10]);
        BaseNCodec codec = new Base64();
        BaseNCodecInputStream stream = new TestBaseNCodecInputStream(in, codec, true);
        stream.read(new byte[10], 0, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadOffsetGreaterThanArrayLength() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[10]);
        BaseNCodec codec = new Base64();
        BaseNCodecInputStream stream = new TestBaseNCodecInputStream(in, codec, true);
        stream.read(new byte[10], 11, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadOffsetPlusLengthGreaterThanArrayLength() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[10]);
        BaseNCodec codec = new Base64();
        BaseNCodecInputStream stream = new TestBaseNCodecInputStream(in, codec, true);
        stream.read(new byte[10], 5, 6);
    }

    @Test
    public void testReadZeroLength() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[10]);
        BaseNCodec codec = new Base64();
        BaseNCodecInputStream stream = new TestBaseNCodecInputStream(in, codec, true);
        byte[] buf = new byte[10];
        int bytesRead = stream.read(buf, 0, 0);
        Assert.assertEquals(0, bytesRead);
    }

    @Test
    public void testEncodeEmptyStream() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        BaseNCodec codec = new Base64();
        BaseNCodecInputStream stream = new TestBaseNCodecInputStream(in, codec, true);
        byte[] buf = new byte[10];
        int bytesRead = stream.read(buf, 0, buf.length);
        Assert.assertEquals(-1, bytesRead);
        Assert.assertEquals(-1, stream.read());
    }

    @Test
    public void testDecodeEmptyStream() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        BaseNCodec codec = new Base64();
        BaseNCodecInputStream stream = new TestBaseNCodecInputStream(in, codec, false);
        byte[] buf = new byte[10];
        int bytesRead = stream.read(buf, 0, buf.length);
        Assert.assertEquals(-1, bytesRead);
        Assert.assertEquals(-1, stream.read());
    }

    @Test
    public void testEncodeReadArray() throws IOException {
        byte[] input = "Hello World".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(input);
        BaseNCodec codec = new Base64(0); // no chunking
        BaseNCodecInputStream stream = new TestBaseNCodecInputStream(in, codec, true);

        byte[] out = new byte[100];
        int totalRead = 0;
        int read;
        while ((read = stream.read(out, totalRead, out.length - totalRead)) != -1) {
            totalRead += read;
        }

        byte[] actual = Arrays.copyOf(out, totalRead);
        byte[] expected = Base64.encodeBase64(input, false);
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void testDecodeReadArray() throws IOException {
        byte[] input = "SGVsbG8gV29ybGQ=".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(input);
        BaseNCodec codec = new Base64();
        BaseNCodecInputStream stream = new TestBaseNCodecInputStream(in, codec, false);

        byte[] out = new byte[100];
        int totalRead = 0;
        int read;
        while ((read = stream.read(out, totalRead, out.length - totalRead)) != -1) {
            totalRead += read;
        }

        byte[] actual = Arrays.copyOf(out, totalRead);
        byte[] expected = "Hello World".getBytes("UTF-8");
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void testEncodeSingleByteRead() throws IOException {
        byte[] input = new byte[]{(byte) 0xFF, (byte) 0xFE, (byte) 0xFD};
        InputStream in = new ByteArrayInputStream(input);
        BaseNCodec codec = new Base64(0);
        BaseNCodecInputStream stream = new TestBaseNCodecInputStream(in, codec, true);

        byte[] expected = Base64.encodeBase64(input, false);
        byte[] actual = new byte[expected.length];

        for (int i = 0; i < expected.length; i++) {
            int b = stream.read();
            Assert.assertTrue(b >= 0 && b <= 255);
            actual[i] = (byte) b;
        }
        Assert.assertEquals(-1, stream.read());
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void testDecodeSingleByteRead() throws IOException {
        byte[] input = Base64.encodeBase64(new byte[]{(byte) 0x80, (byte) 0x90, (byte) 0xA0}, false);
        InputStream in = new ByteArrayInputStream(input);
        BaseNCodec codec = new Base64();
        BaseNCodecInputStream stream = new TestBaseNCodecInputStream(in, codec, false);

        int b1 = stream.read();
        int b2 = stream.read();
        int b3 = stream.read();
        int eof = stream.read();

        Assert.assertEquals(128, b1);
        Assert.assertEquals(144, b2);
        Assert.assertEquals(160, b3);
        Assert.assertEquals(-1, eof);
    }

    @Test
    public void testLargeDataEncodeAndDecode() throws IOException {
        byte[] largeData = new byte[10000];
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte) (i % 256);
        }

        // Test Encode large data (exercises 4096 byte buffer branch)
        InputStream inEncode = new ByteArrayInputStream(largeData);
        BaseNCodec codecEncode = new Base64(0);
        BaseNCodecInputStream streamEncode = new TestBaseNCodecInputStream(inEncode, codecEncode, true);

        byte[] encodedBuffer = new byte[20000];
        int encodedLen = 0;
        int r;
        while ((r = streamEncode.read(encodedBuffer, encodedLen, 512)) != -1) {
            encodedLen += r;
        }
        byte[] encoded = Arrays.copyOf(encodedBuffer, encodedLen);
        Assert.assertArrayEquals(Base64.encodeBase64(largeData, false), encoded);

        // Test Decode large data (exercises 8192 byte buffer branch)
        InputStream inDecode = new ByteArrayInputStream(encoded);
        BaseNCodec codecDecode = new Base64(0);
        BaseNCodecInputStream streamDecode = new TestBaseNCodecInputStream(inDecode, codecDecode, false);

        byte[] decodedBuffer = new byte[20000];
        int decodedLen = 0;
        while ((r = streamDecode.read(decodedBuffer, decodedLen, 1024)) != -1) {
            decodedLen += r;
        }
        byte[] decoded = Arrays.copyOf(decodedBuffer, decodedLen);
        Assert.assertArrayEquals(largeData, decoded);
    }

    @Test
    public void testBase32DecodingWithIgnoredCharacters() throws IOException {
        // String with Base32 data containing non-Base32 characters like spaces or newlines
        // which causes readResults to return 0 initially, testing the readLen == 0 loop.
        String base32WithSpaces = "   JBSWY3DPEBLW64TMMQ======   ";
        InputStream in = new ByteArrayInputStream(base32WithSpaces.getBytes("UTF-8"));
        BaseNCodec codec = new Base32();
        BaseNCodecInputStream stream = new TestBaseNCodecInputStream(in, codec, false);

        byte[] out = new byte[100];
        int totalRead = 0;
        int read;
        while ((read = stream.read(out, totalRead, out.length - totalRead)) != -1) {
            totalRead += read;
        }

        byte[] actual = Arrays.copyOf(out, totalRead);
        byte[] expected = "Hello World".getBytes("UTF-8");
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void testCustomCodecWithZeroReadResultsLoop() throws IOException {
        // Mock-like BaseNCodec to explicitly trigger readResults() == 0 and hasData() transitions
        BaseNCodec customCodec = new BaseNCodec(3, 4, 0, 0) {
            private int callCount = 0;
            private boolean eof = false;

            @Override
            void encode(byte[] in, int inPos, int inAvail) {
                if (inAvail < 0) {
                    eof = true;
                }
            }

            @Override
            void decode(byte[] in, int inPos, int inAvail) {
                if (inAvail < 0) {
                    eof = true;
                }
            }

            @Override
            protected boolean isInAlphabet(byte value) {
                return true;
            }

            @Override
            public boolean hasData() {
                return callCount < 2 && !eof;
            }

            @Override
            int readResults(byte[] b, int bAvail, int bPos) {
                callCount++;
                if (callCount == 1) {
                    return 0; // Trigger the while (readLen == 0) loop
                } else if (callCount == 2) {
                    b[bAvail] = 42;
                    return 1;
                }
                return EOF;
            }
        };

        InputStream in = new ByteArrayInputStream(new byte[]{1, 2, 3});
        BaseNCodecInputStream stream = new TestBaseNCodecInputStream(in, customCodec, true);

        int firstByte = stream.read();
        Assert.assertEquals(42, firstByte);

        int secondByte = stream.read();
        Assert.assertEquals(-1, secondByte);
    }
}
