package org.apache.commons.codec.binary;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;

public class Base64Test {

    @Test
    public void testDefaultConstructor() {
        Base64 base64 = new Base64();
        Assert.assertFalse(base64.isUrlSafe());
        Assert.assertFalse(base64.hasData());
        Assert.assertEquals(0, base64.avail());
    }

    @Test
    public void testBooleanConstructor() {
        Base64 base64UrlSafe = new Base64(true);
        Assert.assertTrue(base64UrlSafe.isUrlSafe());

        Base64 base64Standard = new Base64(false);
        Assert.assertFalse(base64Standard.isUrlSafe());
    }

    @Test
    public void testLineLengthConstructor() {
        Base64 base64 = new Base64(64);
        Assert.assertFalse(base64.isUrlSafe());

        Base64 base64Zero = new Base64(0);
        Assert.assertFalse(base64Zero.isUrlSafe());

        Base64 base64Negative = new Base64(-1);
        Assert.assertFalse(base64Negative.isUrlSafe());
    }

    @Test
    public void testLineLengthAndSeparatorConstructor() {
        byte[] customSeparator = new byte[]{'\n'};
        Base64 base64 = new Base64(64, customSeparator);
        Assert.assertFalse(base64.isUrlSafe());

        Base64 base64NullSep = new Base64(64, null);
        Assert.assertFalse(base64NullSep.isUrlSafe());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLineSeparatorContainingBase64ByteThrows() {
        byte[] invalidSeparator = new byte[]{'A', '\n'};
        new Base64(64, invalidSeparator);
    }

    @Test
    public void testIsBase64() {
        Assert.assertTrue(Base64.isBase64((byte) '='));
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

        Assert.assertFalse(Base64.isBase64((byte) -1));
        Assert.assertFalse(Base64.isBase64((byte) 127));
        Assert.assertFalse(Base64.isBase64((byte) ' '));
        Assert.assertFalse(Base64.isBase64((byte) '\n'));
        Assert.assertFalse(Base64.isBase64((byte) '\r'));
        Assert.assertFalse(Base64.isBase64((byte) '\t'));
        Assert.assertFalse(Base64.isBase64((byte) '$'));
    }

    @Test
    public void testIsArrayByteBase64() {
        Assert.assertTrue(Base64.isArrayByteBase64(new byte[0]));
        Assert.assertTrue(Base64.isArrayByteBase64(StringUtils.getBytesUtf8("YWJj\r\n\t ")));
        Assert.assertFalse(Base64.isArrayByteBase64(StringUtils.getBytesUtf8("YWJj!")));
    }

    @Test
    public void testDiscardWhitespace() {
        byte[] withWhitespace = StringUtils.getBytesUtf8(" Y\nW\rB\tj ");
        byte[] expected = StringUtils.getBytesUtf8("YWBj");
        byte[] groomed = Base64.discardWhitespace(withWhitespace);
        Assert.assertArrayEquals(expected, groomed);
    }

    @Test
    public void testEncodeEmptyAndNull() {
        Base64 base64 = new Base64();
        Assert.assertNull(base64.encode(null));
        Assert.assertArrayEquals(new byte[0], base64.encode(new byte[0]));
        Assert.assertNull(Base64.encodeBase64(null));
        Assert.assertArrayEquals(new byte[0], Base64.encodeBase64(new byte[0]));
    }

    @Test
    public void testDecodeEmptyAndNull() {
        Base64 base64 = new Base64();
        Assert.assertNull(base64.decode((byte[]) null));
        Assert.assertArrayEquals(new byte[0], base64.decode(new byte[0]));
        Assert.assertNull(Base64.decodeBase64((byte[]) null));
        Assert.assertArrayEquals(new byte[0], Base64.decodeBase64(new byte[0]));
        Assert.assertNull(Base64.decodeBase64((String) null));
    }

    @Test
    public void testEncodeModulusCases() {
        Base64 base64 = new Base64();

        // Length % 3 == 1 (modulus 1 -> 2 pad chars)
        byte[] input1 = new byte[]{'a'};
        String encoded1 = base64.encodeToString(input1);
        Assert.assertEquals("YQ==", encoded1);

        // Length % 3 == 2 (modulus 2 -> 1 pad char)
        byte[] input2 = new byte[]{'a', 'b'};
        String encoded2 = base64.encodeToString(input2);
        Assert.assertEquals("YWI=", encoded2);

        // Length % 3 == 0 (modulus 0 -> 0 pad chars)
        byte[] input3 = new byte[]{'a', 'b', 'c'};
        String encoded3 = base64.encodeToString(input3);
        Assert.assertEquals("YWJj", encoded3);

        // Negative byte value handling
        byte[] inputNegative = new byte[]{-1, -2, -3};
        byte[] encodedNeg = base64.encode(inputNegative);
        Assert.assertArrayEquals(inputNegative, base64.decode(encodedNeg));
    }

    @Test
    public void testEncodeDecodeUrlSafe() {
        byte[] binary = new byte[]{(byte) 0xfb, (byte) 0xff, (byte) 0xbf, (byte) 0xfe};

        // Standard encoding produces '+' and '/' and '='
        byte[] standard = Base64.encodeBase64(binary, false, false);
        String standardStr = StringUtils.newStringUtf8(standard);
        Assert.assertTrue(standardStr.contains("+") || standardStr.contains("/"));
        Assert.assertTrue(standardStr.endsWith("="));

        // URL-Safe encoding produces '-' and '_' and skips padding
        byte[] urlSafe = Base64.encodeBase64URLSafe(binary);
        String urlSafeStr = Base64.encodeBase64URLSafeString(binary);
        Assert.assertEquals(StringUtils.newStringUtf8(urlSafe), urlSafeStr);
        Assert.assertFalse(urlSafeStr.contains("+"));
        Assert.assertFalse(urlSafeStr.contains("/"));
        Assert.assertFalse(urlSafeStr.contains("="));
        Assert.assertTrue(urlSafeStr.contains("-") || urlSafeStr.contains("_"));

        // Both decode back to same original binary
        Assert.assertArrayEquals(binary, Base64.decodeBase64(standard));
        Assert.assertArrayEquals(binary, Base64.decodeBase64(urlSafe));
        Assert.assertArrayEquals(binary, Base64.decodeBase64(urlSafeStr));
    }

    @Test
    public void testEncodeDecodeChunked() {
        byte[] largeData = new byte[150];
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte) (i % 256);
        }

        byte[] chunkedBytes = Base64.encodeBase64Chunked(largeData);
        String chunkedStr = Base64.encodeBase64String(largeData);
        Assert.assertEquals(StringUtils.newStringUtf8(chunkedBytes), chunkedStr);
        Assert.assertTrue(chunkedStr.contains("\r\n"));

        byte[] decoded = Base64.decodeBase64(chunkedBytes);
        Assert.assertArrayEquals(largeData, decoded);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEncodeMaxResultSizeExceeded() {
        byte[] data = new byte[100];
        Base64.encodeBase64(data, false, false, 10);
    }

    @Test
    public void testDecodeWithPaddingAndWhitespaceAndNoise() {
        Base64 base64 = new Base64();

        // 1 Pad
        Assert.assertEquals("ab", StringUtils.newStringUtf8(base64.decode("YWI=")));
        // 2 Pads
        Assert.assertEquals("a", StringUtils.newStringUtf8(base64.decode("YQ==")));
        // Optional pads omitted
        Assert.assertEquals("ab", StringUtils.newStringUtf8(base64.decode("YWI")));
        Assert.assertEquals("a", StringUtils.newStringUtf8(base64.decode("YQ")));

        // Embedded whitespace & non-base64 characters
        byte[] decodedWithNoise = base64.decode(StringUtils.getBytesUtf8(" \t\r\n Y \n W !@#$ B j \r\n"));
        Assert.assertEquals("abc", StringUtils.newStringUtf8(decodedWithNoise));
    }

    @Test
    public void testObjectEncodeDecode() throws Exception {
        Base64 base64 = new Base64();

        byte[] raw = StringUtils.getBytesUtf8("Hello World");
        Object encodedObj = base64.encode((Object) raw);
        Assert.assertTrue(encodedObj instanceof byte[]);
        Assert.assertArrayEquals(base64.encode(raw), (byte[]) encodedObj);

        Object decodedFromBytes = base64.decode(encodedObj);
        Assert.assertTrue(decodedFromBytes instanceof byte[]);
        Assert.assertArrayEquals(raw, (byte[]) decodedFromBytes);

        String base64Str = base64.encodeToString(raw);
        Object decodedFromString = base64.decode((Object) base64Str);
        Assert.assertTrue(decodedFromString instanceof byte[]);
        Assert.assertArrayEquals(raw, (byte[]) decodedFromString);
    }

    @Test(expected = EncoderException.class)
    public void testObjectEncodeInvalidType() throws Exception {
        Base64 base64 = new Base64();
        base64.encode("A String Cannot Be Encoded With encode(Object)");
    }

    @Test(expected = DecoderException.class)
    public void testObjectDecodeInvalidType() throws Exception {
        Base64 base64 = new Base64();
        base64.decode(12345);
    }

    @Test
    public void testBigIntegerEncodingDecoding() {
        BigInteger bigInt = new BigInteger("123456789012345678901234567890");
        byte[] encodedBigInt = Base64.encodeInteger(bigInt);
        BigInteger decodedBigInt = Base64.decodeInteger(encodedBigInt);
        Assert.assertEquals(bigInt, decodedBigInt);

        // BigInteger exactly byte-aligned (e.g., 255 where sign bit would add a leading 0)
        BigInteger alignedBigInt = BigInteger.valueOf(255);
        byte[] encodedAligned = Base64.encodeInteger(alignedBigInt);
        Assert.assertEquals(alignedBigInt, Base64.decodeInteger(encodedAligned));

        // Zero
        BigInteger zero = BigInteger.ZERO;
        byte[] encodedZero = Base64.encodeInteger(zero);
        Assert.assertEquals(zero, Base64.decodeInteger(encodedZero));
    }

    @Test(expected = NullPointerException.class)
    public void testEncodeIntegerNullThrows() {
        Base64.encodeInteger(null);
    }

    @Test
    public void testToIntegerBytesDirect() {
        BigInteger bigIntAligned = new BigInteger(new byte[]{0x00, (byte) 0x80});
        byte[] resultAligned = Base64.toIntegerBytes(bigIntAligned);
        Assert.assertArrayEquals(new byte[]{(byte) 0x80}, resultAligned);

        BigInteger bigIntUnaligned = new BigInteger(new byte[]{0x01, 0x02});
        byte[] resultUnaligned = Base64.toIntegerBytes(bigIntUnaligned);
        Assert.assertArrayEquals(new byte[]{0x01, 0x02}, resultUnaligned);
    }

    @Test
    public void testStreamingInternalBuffersAndResize() {
        Base64 base64 = new Base64();

        // Trigger resizeBuffer via a very large input array
        byte[] massive = new byte[16384];
        Arrays.fill(massive, (byte) 'A');
        byte[] encoded = base64.encode(massive);
        Assert.assertNotNull(encoded);
        byte[] decoded = base64.decode(encoded);
        Assert.assertArrayEquals(massive, decoded);

        // Direct testing of internal methods for full branch coverage
        base64 = new Base64();
        Assert.assertEquals(0, base64.readResults(new byte[10], 0, 10));

        byte[] directBuf = new byte[10];
        base64.setInitialBuffer(directBuf, 0, 10);
        Assert.assertTrue(base64.hasData());
        Assert.assertEquals(0, base64.avail());

        // Decode EOF after EOF has been reached
        base64.decode(new byte[]{'A', 'A'}, 0, -1);
        Assert.assertEquals(-1, base64.readResults(new byte[10], 0, 10));

        // When eof is true, subsequent encode/decode calls do nothing
        base64.encode(new byte[]{1, 2, 3}, 0, 3);
        base64.decode(new byte[]{1, 2, 3}, 0, 3);
    }

    @Test
    public void testReadResultsBufferReusedSameArray() {
        Base64 base64 = new Base64();
        byte[] target = new byte[4];
        base64.setInitialBuffer(target, 0, 4);
        // Reading with identical buffer reference
        int read = base64.readResults(target, 0, 4);
        Assert.assertEquals(0, read);
        Assert.assertFalse(base64.hasData());
    }

    @Test
    public void testChunkingEdgeCases() {
        // Line length that triggers lineSeparator appending
        byte[] lineSep = new byte[]{'-', '-'};
        Base64 base64 = new Base64(4, lineSep, false);
        byte[] input = new byte[]{1, 2, 3, 4, 5, 6};
        byte[] encoded = base64.encode(input);

        // First 4 encoded characters, lineSep, next 4 encoded characters, lineSep
        Assert.assertEquals(8 + lineSep.length * 2, encoded.length);

        // Decode chunked with custom separator ignored as non-base64 if not in table
        byte[] decoded = base64.decode(encoded);
        Assert.assertArrayEquals(input, decoded);
    }
}
