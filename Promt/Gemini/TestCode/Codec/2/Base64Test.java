package org.apache.commons.codec.binary;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Arrays;

public class Base64Test {

    private static final String UTF_8 = "UTF-8";

    @Test
    public void testDefaultConstructor() {
        Base64 b64 = new Base64();
        Assert.assertFalse(b64.isUrlSafe());
        Assert.assertFalse(b64.hasData());
        Assert.assertEquals(0, b64.avail());
    }

    @Test
    public void testUrlSafeConstructor() {
        Base64 b64Url = new Base64(true);
        Assert.assertTrue(b64Url.isUrlSafe());

        Base64 b64Std = new Base64(false);
        Assert.assertFalse(b64Std.isUrlSafe());
    }

    @Test
    public void testLineLengthConstructor() {
        Base64 b64 = new Base64(64);
        Assert.assertFalse(b64.isUrlSafe());

        Base64 b64NoChunk = new Base64(0);
        Assert.assertFalse(b64NoChunk.isUrlSafe());

        Base64 b64Negative = new Base64(-1);
        Assert.assertFalse(b64Negative.isUrlSafe());
    }

    @Test
    public void testLineLengthAndSeparatorConstructor() {
        byte[] customSep = new byte[]{'\n'};
        Base64 b64 = new Base64(32, customSep);
        Assert.assertFalse(b64.isUrlSafe());

        Base64 b64Url = new Base64(32, customSep, true);
        Assert.assertTrue(b64Url.isUrlSafe());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithInvalidSeparator() {
        byte[] invalidSep = new byte[]{'A'};
        new Base64(76, invalidSep, false);
    }

    @Test
    public void testIsBase64() {
        Assert.assertTrue(Base64.isBase64((byte) 'A'));
        Assert.assertTrue(Base64.isBase64((byte) 'Z'));
        Assert.assertTrue(Base64.isBase64((byte) 'a'));
        Assert.assertTrue(Base64.isBase64((byte) 'z'));
        Assert.assertTrue(Base64.isBase64((byte) '0'));
        Assert.assertTrue(Base64.isBase64((byte) '9'));
        Assert.assertTrue(Base64.isBase64((byte) '+'));
        Assert.assertTrue(Base64.isBase64((byte) '/'));
        Assert.assertTrue(Base64.isBase64((byte) '='));
        Assert.assertTrue(Base64.isBase64((byte) '-'));
        Assert.assertTrue(Base64.isBase64((byte) '_'));

        Assert.assertFalse(Base64.isBase64((byte) ' '));
        Assert.assertFalse(Base64.isBase64((byte) '\t'));
        Assert.assertFalse(Base64.isBase64((byte) '\r'));
        Assert.assertFalse(Base64.isBase64((byte) '\n'));
        Assert.assertFalse(Base64.isBase64((byte) -1));
        Assert.assertFalse(Base64.isBase64((byte) 127));
        Assert.assertFalse(Base64.isBase64((byte) '$'));
        Assert.assertFalse(Base64.isBase64((byte) '@'));
    }

    @Test
    public void testIsArrayByteBase64() {
        byte[] empty = new byte[0];
        Assert.assertTrue(Base64.isArrayByteBase64(empty));

        byte[] valid = "YWJjZGVmZ2hpams=".getBytes();
        Assert.assertTrue(Base64.isArrayByteBase64(valid));

        byte[] validWithWhitespace = "YWJj\r\n ZGVm\tZ2hpams=".getBytes();
        Assert.assertTrue(Base64.isArrayByteBase64(validWithWhitespace));

        byte[] invalid = "YWJj!@#$%".getBytes();
        Assert.assertFalse(Base64.isArrayByteBase64(invalid));

        byte[] invalidWithNegative = new byte[]{'A', -5, 'B'};
        Assert.assertFalse(Base64.isArrayByteBase64(invalidWithNegative));
    }

    @Test
    public void testEncodeBase64Basic() throws UnsupportedEncodingException {
        byte[] input1 = "1".getBytes(UTF_8);
        byte[] input2 = "12".getBytes(UTF_8);
        byte[] input3 = "123".getBytes(UTF_8);
        byte[] input4 = "1234".getBytes(UTF_8);

        Assert.assertEquals("MQ==", new String(Base64.encodeBase64(input1), UTF_8));
        Assert.assertEquals("MTI=", new String(Base64.encodeBase64(input2), UTF_8));
        Assert.assertEquals("MTIz", new String(Base64.encodeBase64(input3), UTF_8));
        Assert.assertEquals("MTIzNA==", new String(Base64.encodeBase64(input4), UTF_8));

        Assert.assertNull(Base64.encodeBase64(null));
        Assert.assertArrayEquals(new byte[0], Base64.encodeBase64(new byte[0]));
    }

    @Test
    public void testDecodeBase64Basic() throws UnsupportedEncodingException {
        Assert.assertEquals("1", new String(Base64.decodeBase64("MQ==".getBytes(UTF_8)), UTF_8));
        Assert.assertEquals("12", new String(Base64.decodeBase64("MTI=".getBytes(UTF_8)), UTF_8));
        Assert.assertEquals("123", new String(Base64.decodeBase64("MTIz".getBytes(UTF_8)), UTF_8));
        Assert.assertEquals("1234", new String(Base64.decodeBase64("MTIzNA==".getBytes(UTF_8)), UTF_8));

        // Missing padding characters
        Assert.assertEquals("1", new String(Base64.decodeBase64("MQ".getBytes(UTF_8)), UTF_8));
        Assert.assertEquals("12", new String(Base64.decodeBase64("MTI".getBytes(UTF_8)), UTF_8));

        Assert.assertNull(Base64.decodeBase64(null));
        Assert.assertArrayEquals(new byte[0], Base64.decodeBase64(new byte[0]));
    }

    @Test
    public void testUrlSafeEncodingDecoding() throws UnsupportedEncodingException {
        byte[] binary = new byte[]{(byte) 0xfb, (byte) 0xf0, (byte) 0xfe, (byte) 0xff};
        byte[] standardEncoded = Base64.encodeBase64(binary, false, false);
        byte[] urlSafeEncoded = Base64.encodeBase64URLSafe(binary);

        String standardStr = new String(standardEncoded, UTF_8);
        String urlSafeStr = new String(urlSafeEncoded, UTF_8);

        Assert.assertTrue(standardStr.contains("+") || standardStr.contains("/"));
        Assert.assertFalse(urlSafeStr.contains("+"));
        Assert.assertFalse(urlSafeStr.contains("/"));
        Assert.assertFalse(urlSafeStr.contains("="));

        byte[] decodedFromStandard = Base64.decodeBase64(standardEncoded);
        byte[] decodedFromUrlSafe = Base64.decodeBase64(urlSafeEncoded);

        Assert.assertArrayEquals(binary, decodedFromStandard);
        Assert.assertArrayEquals(binary, decodedFromUrlSafe);

        Base64 urlSafeInstance = new Base64(true);
        byte[] encodedInstance = urlSafeInstance.encode(binary);
        Assert.assertArrayEquals(urlSafeEncoded, encodedInstance);
    }

    @Test
    public void testChunkedEncoding() throws UnsupportedEncodingException {
        byte[] longData = new byte[100];
        for (int i = 0; i < longData.length; i++) {
            longData[i] = (byte) (i % 256);
        }

        byte[] chunked = Base64.encodeBase64Chunked(longData);
        String chunkedStr = new String(chunked, UTF_8);

        Assert.assertTrue(chunkedStr.contains("\r\n"));
        Assert.assertTrue(chunkedStr.endsWith("\r\n"));

        byte[] decoded = Base64.decodeBase64(chunked);
        Assert.assertArrayEquals(longData, decoded);

        byte[] chunkedViaParam = Base64.encodeBase64(longData, true);
        Assert.assertArrayEquals(chunked, chunkedViaParam);
    }

    @Test
    public void testInstanceEncodeDecodeMethods() throws Exception {
        Base64 b64 = new Base64();
        byte[] original = "Testing Base64 Instance Methods".getBytes(UTF_8);

        Object encodedObj = b64.encode((Object) original);
        Assert.assertTrue(encodedObj instanceof byte[]);
        byte[] encodedBytes = (byte[]) encodedObj;

        Object decodedObj = b64.decode(encodedObj);
        Assert.assertTrue(decodedObj instanceof byte[]);
        byte[] decodedBytes = (byte[]) decodedObj;

        Assert.assertArrayEquals(original, decodedBytes);
        Assert.assertArrayEquals(original, b64.decode(b64.encode(original)));
    }

    @Test(expected = EncoderException.class)
    public void testEncodeNonByteArrayThrowsException() throws EncoderException {
        Base64 b64 = new Base64();
        b64.encode("A String is not a byte[]");
    }

    @Test(expected = DecoderException.class)
    public void testDecodeNonByteArrayThrowsException() throws DecoderException {
        Base64 b64 = new Base64();
        b64.decode("A String is not a byte[]");
    }

    @Test
    public void testDiscardWhitespace() throws UnsupportedEncodingException {
        byte[] dataWithSpaces = " Y W J j \r\n \t Z G V m ".getBytes(UTF_8);
        byte[] groomed = Base64.discardWhitespace(dataWithSpaces);
        Assert.assertEquals("YWJjZGVm", new String(groomed, UTF_8));

        byte[] empty = new byte[0];
        Assert.assertArrayEquals(empty, Base64.discardWhitespace(empty));
    }

    @Test
    public void testDiscardNonBase64() throws UnsupportedEncodingException {
        byte[] dataWithNonBase64 = "YW#J$j%Z&G*V(m)".getBytes(UTF_8);
        byte[] groomed = Base64.discardNonBase64(dataWithNonBase64);
        Assert.assertEquals("YWJjZGVm", new String(groomed, UTF_8));

        byte[] empty = new byte[0];
        Assert.assertArrayEquals(empty, Base64.discardNonBase64(empty));
    }

    @Test
    public void testBigIntegerEncodingDecoding() {
        BigInteger bigInt = new BigInteger("123456789012345678901234567890");
        byte[] encoded = Base64.encodeInteger(bigInt);
        BigInteger decoded = Base64.decodeInteger(encoded);
        Assert.assertEquals(bigInt, decoded);

        // Test exact byte boundary alignment
        BigInteger alignedBigInt = new BigInteger("256");
        byte[] encodedAligned = Base64.encodeInteger(alignedBigInt);
        BigInteger decodedAligned = Base64.decodeInteger(encodedAligned);
        Assert.assertEquals(alignedBigInt, decodedAligned);

        // Test bitLength % 8 == 0 path in toIntegerBytes
        BigInteger powerOfTwo = BigInteger.valueOf(2).pow(16);
        byte[] encodedPower = Base64.encodeInteger(powerOfTwo);
        Assert.assertEquals(powerOfTwo, Base64.decodeInteger(encodedPower));

        // Test non-aligned BigInteger
        BigInteger small = BigInteger.valueOf(127);
        byte[] encodedSmall = Base64.encodeInteger(small);
        Assert.assertEquals(small, Base64.decodeInteger(encodedSmall));
    }

    @Test(expected = NullPointerException.class)
    public void testEncodeIntegerNullThrowsException() {
        Base64.encodeInteger(null);
    }

    @Test
    public void testStreamingEncodeDecodeLargeBuffer() {
        Base64 b64 = new Base64();
        byte[] largeData = new byte[16384];
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte) (i & 0xFF);
        }

        b64.encode(largeData, 0, largeData.length);
        b64.encode(largeData, 0, -1); // EOF

        Assert.assertTrue(b64.hasData());
        int avail = b64.avail();
        Assert.assertTrue(avail > 0);

        byte[] encoded = new byte[avail];
        int read = b64.readResults(encoded, 0, avail);
        Assert.assertEquals(avail, read);
        Assert.assertEquals(0, b64.avail());
        Assert.assertFalse(b64.hasData());

        // Test EOF repeated call
        b64.encode(largeData, 0, 10);
        Assert.assertEquals(0, b64.avail());

        // Decode large data
        Base64 decoder = new Base64();
        decoder.decode(encoded, 0, encoded.length);
        decoder.decode(encoded, 0, -1);

        byte[] decoded = new byte[decoder.avail()];
        decoder.readResults(decoded, 0, decoded.length);
        Assert.assertArrayEquals(largeData, decoded);

        // Test decoder EOF repeated call
        decoder.decode(encoded, 0, 10);
        Assert.assertEquals(0, decoder.avail());
    }

    @Test
    public void testStreamingPartialReads() {
        Base64 b64 = new Base64();
        byte[] data = "Hello World!".getBytes();
        b64.encode(data, 0, data.length);
        b64.encode(data, 0, -1);

        int total = b64.avail();
        byte[] part1 = new byte[4];
        int read1 = b64.readResults(part1, 0, 4);
        Assert.assertEquals(4, read1);
        Assert.assertEquals(total - 4, b64.avail());
        Assert.assertTrue(b64.hasData());

        byte[] part2 = new byte[total - 4];
        int read2 = b64.readResults(part2, 0, part2.length);
        Assert.assertEquals(total - 4, read2);
        Assert.assertEquals(0, b64.avail());
        Assert.assertFalse(b64.hasData());

        // Read when buffer is empty and EOF is true
        int readAfterEof = b64.readResults(new byte[10], 0, 10);
        Assert.assertEquals(-1, readAfterEof);
    }

    @Test
    public void testSetInitialBufferDirectBufferReuse() {
        Base64 b64 = new Base64(0, new byte[0], false);
        byte[] target = new byte[4];
        b64.setInitialBuffer(target, 0, 4);
        byte[] input = new byte[]{1, 2, 3};
        b64.encode(input, 0, 3);

        // readResults where buf == b
        int read = b64.readResults(target, 0, 4);
        Assert.assertEquals(4, read);
        Assert.assertFalse(b64.hasData());
    }

    @Test
    public void testNegativeByteHandling() {
        byte[] negativeBytes = new byte[]{-1, -2, -3, -4, -5};
        byte[] encoded = Base64.encodeBase64(negativeBytes);
        byte[] decoded = Base64.decodeBase64(encoded);
        Assert.assertArrayEquals(negativeBytes, decoded);
    }

    @Test
    public void testGarbageInGarbageOutDecoder() {
        byte[] garbage = "SGVs!@#$bG8g%^&*V29ybGQ=".getBytes();
        byte[] decoded = Base64.decodeBase64(garbage);
        Assert.assertEquals("Hello World", new String(decoded));
    }

    @Test
    public void testDecodeWithPadPrematurely() {
        Base64 decoder = new Base64();
        byte[] input = "MQ==AAAA".getBytes();
        decoder.decode(input, 0, input.length);
        byte[] out = new byte[decoder.avail()];
        decoder.readResults(out, 0, out.length);
        Assert.assertEquals("1", new String(out));
    }
}
