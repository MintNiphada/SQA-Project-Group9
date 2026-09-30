package org.apache.commons.codec.binary;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

public class Base64Test {

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

        byte[] input = new byte[100];
        Arrays.fill(input, (byte) 'A');
        byte[] encoded = b64.encode(input);
        String encodedStr = StringUtils.newStringUtf8(encoded);
        Assert.assertTrue(encodedStr.contains("\r\n"));
    }

    @Test
    public void testLineLengthAndSeparatorConstructor() {
        byte[] customSep = new byte[]{';', ':'};
        Base64 b64 = new Base64(8, customSep);
        Assert.assertFalse(b64.isUrlSafe());

        byte[] input = "1234567890".getBytes();
        byte[] encoded = b64.encode(input);
        String encodedStr = StringUtils.newStringUtf8(encoded);
        Assert.assertTrue(encodedStr.contains(";:"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithBase64SeparatorThrows() {
        byte[] invalidSep = new byte[]{'A', '\n'};
        new Base64(76, invalidSep);
    }

    @Test
    public void testConstructorWithNullSeparator() {
        Base64 b64 = new Base64(76, null, true);
        Assert.assertTrue(b64.isUrlSafe());
        byte[] input = "Hello World".getBytes();
        byte[] encoded = b64.encode(input);
        String encodedStr = StringUtils.newStringUtf8(encoded);
        Assert.assertFalse(encodedStr.contains("\r\n"));
    }

    @Test
    public void testEncodeEmptyAndNull() {
        Base64 b64 = new Base64();
        Assert.assertNull(b64.encode((byte[]) null));
        Assert.assertEquals(0, b64.encode(new byte[0]).length);
        Assert.assertNull(Base64.encodeBase64(null));
        Assert.assertEquals(0, Base64.encodeBase64(new byte[0]).length);
        Assert.assertNull(Base64.encodeBase64String(null));
    }

    @Test
    public void testDecodeEmptyAndNull() {
        Base64 b64 = new Base64();
        Assert.assertNull(b64.decode((byte[]) null));
        Assert.assertEquals(0, b64.decode(new byte[0]).length);
        Assert.assertNull(Base64.decodeBase64((byte[]) null));
        Assert.assertEquals(0, Base64.decodeBase64(new byte[0]).length);
        Assert.assertNull(Base64.decodeBase64((String) null));
    }

    @Test
    public void testBasicEncodeDecodeStandard() {
        String original = "Hello World!";
        byte[] origBytes = StringUtils.getBytesUtf8(original);

        byte[] encoded = Base64.encodeBase64(origBytes);
        Assert.assertEquals("SGVsbG8gV29ybGQh", StringUtils.newStringUtf8(encoded));

        byte[] decoded = Base64.decodeBase64(encoded);
        Assert.assertEquals(original, StringUtils.newStringUtf8(decoded));

        String encodedStr = Base64.encodeBase64String(origBytes);
        Assert.assertEquals("SGVsbG8gV29ybGQh", encodedStr);
        Assert.assertEquals(original, StringUtils.newStringUtf8(Base64.decodeBase64(encodedStr)));
    }

    @Test
    public void testModulusPaddingBranches() {
        // 1 byte input -> modulus 1 in encode EOF -> 2 base64 chars + 2 pads
        byte[] oneByte = new byte[]{'a'};
        byte[] encOne = Base64.encodeBase64(oneByte);
        Assert.assertEquals("YQ==", StringUtils.newStringUtf8(encOne));
        Assert.assertArrayEquals(oneByte, Base64.decodeBase64(encOne));

        // 2 bytes input -> modulus 2 in encode EOF -> 3 base64 chars + 1 pad
        byte[] twoBytes = new byte[]{'a', 'b'};
        byte[] encTwo = Base64.encodeBase64(twoBytes);
        Assert.assertEquals("YWI=", StringUtils.newStringUtf8(encTwo));
        Assert.assertArrayEquals(twoBytes, Base64.decodeBase64(encTwo));

        // 3 bytes input -> modulus 0 in encode EOF -> 4 base64 chars + 0 pad
        byte[] threeBytes = new byte[]{'a', 'b', 'c'};
        byte[] encThree = Base64.encodeBase64(threeBytes);
        Assert.assertEquals("YWJj", StringUtils.newStringUtf8(encThree));
        Assert.assertArrayEquals(threeBytes, Base64.decodeBase64(encThree));
    }

    @Test
    public void testUrlSafeEncoding() {
        // Bytes that produce + and / in standard base64 (indices 62 and 63)
        // 0xfb, 0xff, 0xbf -> +/+/
        byte[] binaryData = new byte[]{(byte) 0xfb, (byte) 0xff, (byte) 0xbf};
        byte[] standardEncoded = Base64.encodeBase64(binaryData, false, false);
        Assert.assertEquals("+/+/ ", StringUtils.newStringUtf8(standardEncoded).trim());

        byte[] urlSafeEncoded = Base64.encodeBase64URLSafe(binaryData);
        Assert.assertEquals("-_-_", StringUtils.newStringUtf8(urlSafeEncoded));

        String urlSafeString = Base64.encodeBase64URLSafeString(binaryData);
        Assert.assertEquals("-_-_", urlSafeString);

        // URL safe skips padding on EOF
        byte[] oneByte = new byte[]{(byte) 0xfb};
        byte[] encUrlOne = Base64.encodeBase64URLSafe(oneByte);
        Assert.assertEquals("-w", StringUtils.newStringUtf8(encUrlOne)); // No '=='

        byte[] twoBytes = new byte[]{(byte) 0xfb, (byte) 0xff};
        byte[] encUrlTwo = Base64.encodeBase64URLSafe(twoBytes);
        Assert.assertEquals("-_8", StringUtils.newStringUtf8(encUrlTwo)); // No '='

        // Decoder should seamlessly decode URL-safe without padding
        Assert.assertArrayEquals(oneByte, Base64.decodeBase64(encUrlOne));
        Assert.assertArrayEquals(twoBytes, Base64.decodeBase64(encUrlTwo));
        Assert.assertArrayEquals(binaryData, Base64.decodeBase64(urlSafeEncoded));
    }

    @Test
    public void testChunkedEncoding() {
        byte[] longInput = new byte[100];
        for (int i = 0; i < longInput.length; i++) {
            longInput[i] = (byte) (i & 0xFF);
        }
        byte[] chunked = Base64.encodeBase64Chunked(longInput);
        String chunkedStr = StringUtils.newStringUtf8(chunked);
        Assert.assertTrue(chunkedStr.contains("\r\n"));
        Assert.assertTrue(chunkedStr.endsWith("\r\n"));

        byte[] decoded = Base64.decodeBase64(chunked);
        Assert.assertArrayEquals(longInput, decoded);
    }

    @Test
    public void testDecodeWithWhitespaceAndIgnoredChars() {
        String withWhitespace = " S G V s \n\r\t b G 8 g V 2 9 y b G Q h ";
        byte[] decoded = Base64.decodeBase64(withWhitespace);
        Assert.assertEquals("Hello World!", StringUtils.newStringUtf8(decoded));

        // String with non-base64 characters that should be ignored
        String withGarbage = "SGVsb!@#$%^&*()_+G8gV29ybGQh";
        byte[] decodedGarbage = Base64.decodeBase64(withGarbage);
        Assert.assertEquals("Hello World!", StringUtils.newStringUtf8(decodedGarbage));
    }

    @Test
    public void testDecodeModulusBranchesWithoutPadding() {
        // Base64 without '=' padding
        // 2 chars (12 bits) -> 1 byte decoded (modulus 2)
        byte[] decMod2 = Base64.decodeBase64("YQ");
        Assert.assertArrayEquals(new byte[]{'a'}, decMod2);

        // 3 chars (18 bits) -> 2 bytes decoded (modulus 3)
        byte[] decMod3 = Base64.decodeBase64("YWI");
        Assert.assertArrayEquals(new byte[]{'a', 'b'}, decMod3);

        // 1 char (6 bits) -> 0 bytes decoded (modulus 1, ignored)
        byte[] decMod1 = Base64.decodeBase64("Y");
        Assert.assertEquals(0, decMod1.length);
    }

    @Test
    public void testDecodeStopsAtPadCharacter() {
        // PAD character '=' terminates decoding loop
        byte[] decoded = Base64.decodeBase64("YQ==SGVsbG8=");
        Assert.assertArrayEquals(new byte[]{'a'}, decoded);
    }

    @Test
    public void testIsBase64Byte() {
        Assert.assertTrue(Base64.isBase64((byte) 'A'));
        Assert.assertTrue(Base64.isBase64((byte) 'Z'));
        Assert.assertTrue(Base64.isBase64((byte) 'a'));
        Assert.assertTrue(Base64.isBase64((byte) 'z'));
        Assert.assertTrue(Base64.isBase64((byte) '0'));
        Assert.assertTrue(Base64.isBase64((byte) '9'));
        Assert.assertTrue(Base64.isBase64((byte) '+'));
        Assert.assertTrue(Base64.isBase64((byte) '/'));
        Assert.assertTrue(Base64.isBase64((byte) '-'));
        Assert.assertTrue(Base64.isBase64((byte) '_'));
        Assert.assertTrue(Base64.isBase64((byte) '='));

        Assert.assertFalse(Base64.isBase64((byte) ' '));
        Assert.assertFalse(Base64.isBase64((byte) '$'));
        Assert.assertFalse(Base64.isBase64((byte) -1));
        Assert.assertFalse(Base64.isBase64((byte) 127));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testIsBase64StringAndArray() {
        Assert.assertTrue(Base64.isBase64("SGVsbG8gV29ybGQ="));
        Assert.assertTrue(Base64.isBase64(" SGVsbG8g \r\n V29ybGQ= "));
        Assert.assertFalse(Base64.isBase64("SGVsbG8gV29ybGQ=@"));

        byte[] valid = StringUtils.getBytesUtf8("SGVsbG8g\n\r\t ");
        Assert.assertTrue(Base64.isBase64(valid));
        Assert.assertTrue(Base64.isArrayByteBase64(valid));

        byte[] invalid = StringUtils.getBytesUtf8("SGVsbG8*");
        Assert.assertFalse(Base64.isBase64(invalid));
        Assert.assertFalse(Base64.isArrayByteBase64(invalid));

        Assert.assertTrue(Base64.isBase64(new byte[0]));
        Assert.assertTrue(Base64.isBase64(""));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testDiscardWhitespace() {
        byte[] input = StringUtils.getBytesUtf8(" A \n B \r C \t D ");
        byte[] groomed = Base64.discardWhitespace(input);
        Assert.assertEquals("ABCD", StringUtils.newStringUtf8(groomed));
    }

    @Test
    public void testEncodeDecodeObjectInterface() throws Exception {
        Base64 b64 = new Base64();

        byte[] bytes = "Test String".getBytes();
        Object encodedObj = b64.encode((Object) bytes);
        Assert.assertTrue(encodedObj instanceof byte[]);

        Object decodedObj = b64.decode(encodedObj);
        Assert.assertTrue(decodedObj instanceof byte[]);
        Assert.assertArrayEquals(bytes, (byte[]) decodedObj);

        // decode with String object
        String encodedStr = StringUtils.newStringUtf8((byte[]) encodedObj);
        Object decodedFromStr = b64.decode((Object) encodedStr);
        Assert.assertTrue(decodedFromStr instanceof byte[]);
        Assert.assertArrayEquals(bytes, (byte[]) decodedFromStr);
    }

    @Test(expected = EncoderException.class)
    public void testEncodeObjectInvalidTypeThrows() throws Exception {
        Base64 b64 = new Base64();
        b64.encode("Not a byte array");
    }

    @Test(expected = DecoderException.class)
    public void testDecodeObjectInvalidTypeThrows() throws Exception {
        Base64 b64 = new Base64();
        b64.decode(12345);
    }

    @Test
    public void testEncodeToString() {
        Base64 b64 = new Base64();
        String result = b64.encodeToString("Hello".getBytes());
        Assert.assertEquals("SGVsbG8=", result);
        Assert.assertNull(b64.encodeToString(null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEncodeBase64MaxResultSizeExceeded() {
        byte[] data = new byte[100];
        Base64.encodeBase64(data, false, false, 10);
    }

    @Test
    public void testEncodeBase64MaxResultSizeWithinLimit() {
        byte[] data = new byte[10];
        byte[] res = Base64.encodeBase64(data, false, false, 100);
        Assert.assertNotNull(res);
    }

    @Test
    public void testLargeBufferExpansion() {
        // Buffer larger than DEFAULT_BUFFER_SIZE (8192) to trigger resizeBuffer multiple times
        byte[] largeData = new byte[20000];
        new Random(42).nextBytes(largeData);

        Base64 b64 = new Base64();
        byte[] encoded = b64.encode(largeData);
        byte[] decoded = b64.decode(encoded);

        Assert.assertArrayEquals(largeData, decoded);
    }

    @Test
    public void testStreamingEncodeAndReadResults() {
        Base64 b64 = new Base64();
        byte[] input = "Stream test 1234567890".getBytes();

        b64.encode(input, 0, input.length);
        Assert.assertTrue(b64.hasData());
        Assert.assertTrue(b64.avail() > 0);

        b64.encode(input, 0, -1); // EOF

        // Calling encode after EOF is a no-op
        b64.encode(input, 0, 10);

        byte[] out = new byte[b64.avail()];
        int read1 = b64.readResults(out, 0, 5);
        Assert.assertEquals(5, read1);
        Assert.assertTrue(b64.hasData());

        int remaining = out.length - 5;
        int read2 = b64.readResults(out, 5, remaining);
        Assert.assertEquals(remaining, read2);
        Assert.assertFalse(b64.hasData());
        Assert.assertEquals(0, b64.avail());

        // Further read on EOF returns -1
        int readAfterEof = b64.readResults(out, 0, out.length);
        Assert.assertEquals(-1, readAfterEof);

        byte[] decoded = Base64.decodeBase64(out);
        Assert.assertArrayEquals(input, decoded);
    }

    @Test
    public void testStreamingDecodeAfterEofIsNoop() {
        Base64 b64 = new Base64();
        byte[] encoded = Base64.encodeBase64("Hello".getBytes());

        b64.decode(encoded, 0, encoded.length);
        b64.decode(encoded, 0, -1); // EOF

        // Calling decode after EOF should return immediately
        b64.decode(encoded, 0, 2);

        byte[] out = new byte[b64.avail()];
        b64.readResults(out, 0, out.length);
        Assert.assertEquals("Hello", StringUtils.newStringUtf8(out));
    }

    @Test
    public void testReadResultsWhenNoBuffer() {
        Base64 b64 = new Base64();
        byte[] buf = new byte[10];
        int read = b64.readResults(buf, 0, 10);
        Assert.assertEquals(0, read);
    }

    @Test(expected = NullPointerException.class)
    public void testEncodeIntegerNullThrows() {
        Base64.encodeInteger(null);
    }

    @Test
    public void testBigIntegerEncodingDecoding() {
        BigInteger[] testInts = new BigInteger[]{
                BigInteger.ZERO,
                BigInteger.ONE,
                BigInteger.valueOf(127),
                BigInteger.valueOf(128),
                BigInteger.valueOf(255),
                BigInteger.valueOf(256),
                BigInteger.valueOf(65535),
                BigInteger.valueOf(65536),
                new BigInteger("123456789012345678901234567890"),
                new BigInteger(256, new Random(1))
        };

        for (BigInteger bi : testInts) {
            byte[] encoded = Base64.encodeInteger(bi);
            BigInteger decoded = Base64.decodeInteger(encoded);
            Assert.assertEquals("Failed for BigInteger: " + bi, bi, decoded);
        }
    }

    @Test
    public void testToIntegerBytesByteAligned() {
        // bitLength % 8 == 0 (e.g., bit length 16)
        BigInteger biAligned = new BigInteger("8000", 16);
        Assert.assertEquals(16, biAligned.bitLength());
        byte[] bytesAligned = Base64.toIntegerBytes(biAligned);
        Assert.assertEquals(2, bytesAligned.length);
        Assert.assertEquals((byte) 0x80, bytesAligned[0]);
        Assert.assertEquals((byte) 0x00, bytesAligned[1]);

        // bitLength % 8 != 0 and (bitLength / 8 + 1 == bitlen / 8)
        BigInteger biNotAligned = new BigInteger("7fff", 16);
        Assert.assertEquals(15, biNotAligned.bitLength());
        byte[] bytesNotAligned = Base64.toIntegerBytes(biNotAligned);
        Assert.assertEquals(2, bytesNotAligned.length);
    }

    @Test
    public void testNegativeByteValuesEncoding() {
        byte[] negativeBytes = new byte[]{-1, -128, -64, -2};
        byte[] encoded = Base64.encodeBase64(negativeBytes);
        byte[] decoded = Base64.decodeBase64(encoded);
        Assert.assertArrayEquals(negativeBytes, decoded);
    }

    @Test
    public void testPemChunkSizeConstant() {
        Assert.assertEquals(64, Base64.PEM_CHUNK_SIZE);
        Assert.assertEquals(76, Base64.MIME_CHUNK_SIZE);
        Assert.assertArrayEquals(new byte[]{'\r', '\n'}, Base64.CHUNK_SEPARATOR);
    }
}
