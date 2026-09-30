package org.apache.commons.codec.binary;

import java.math.BigInteger;
import java.util.Arrays;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.junit.Assert;
import org.junit.Test;

public class Base64Test {

    @Test
    public void testConstructors() {
        Base64 b64Default = new Base64();
        Assert.assertFalse(b64Default.isUrlSafe());

        Base64 b64UrlSafe = new Base64(true);
        Assert.assertTrue(b64UrlSafe.isUrlSafe());

        Base64 b64Chunked = new Base64(64);
        Assert.assertFalse(b64Chunked.isUrlSafe());

        Base64 b64CustomSep = new Base64(64, new byte[]{'\n'});
        Assert.assertFalse(b64CustomSep.isUrlSafe());

        Base64 b64NullSep = new Base64(64, null, true);
        Assert.assertTrue(b64NullSep.isUrlSafe());

        Base64 b64NegativeLength = new Base64(-1, new byte[]{'\n'}, false);
        Assert.assertFalse(b64NegativeLength.isUrlSafe());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithBase64SeparatorThrows() {
        new Base64(76, new byte[]{'A', '\n'});
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

        Assert.assertFalse(Base64.isBase64((byte) ' '));
        Assert.assertFalse(Base64.isBase64((byte) '\n'));
        Assert.assertFalse(Base64.isBase64((byte) -1));
        Assert.assertFalse(Base64.isBase64((byte) -128));
        Assert.assertFalse(Base64.isBase64((byte) 127));
    }

    @Test
    public void testIsArrayByteBase64() {
        Assert.assertTrue(Base64.isArrayByteBase64(new byte[0]));
        Assert.assertTrue(Base64.isArrayByteBase64(StringUtils.getBytesUtf8("YWJj\r\n\t ")));
        Assert.assertFalse(Base64.isArrayByteBase64(new byte[]{(byte) 0xFF}));
        Assert.assertFalse(Base64.isArrayByteBase64(new byte[]{'a', 'b', 'c', (byte) 0x80}));
    }

    @Test
    public void testDiscardWhitespace() {
        byte[] input = StringUtils.getBytesUtf8(" a \n b \r c \t d ");
        byte[] expected = StringUtils.getBytesUtf8("abcd");
        Assert.assertArrayEquals(expected, Base64.discardWhitespace(input));
    }

    @Test
    public void testEncodeEmptyAndNull() {
        Base64 b64 = new Base64();
        Assert.assertNull(b64.encode((byte[]) null));
        Assert.assertArrayEquals(new byte[0], b64.encode(new byte[0]));
        Assert.assertNull(Base64.encodeBase64(null));
        Assert.assertArrayEquals(new byte[0], Base64.encodeBase64(new byte[0]));
        Assert.assertEquals("", Base64.encodeBase64String(new byte[0]));
    }

    @Test
    public void testDecodeEmptyAndNull() {
        Base64 b64 = new Base64();
        Assert.assertNull(b64.decode((byte[]) null));
        Assert.assertArrayEquals(new byte[0], b64.decode(new byte[0]));
        Assert.assertNull(Base64.decodeBase64((byte[]) null));
        Assert.assertArrayEquals(new byte[0], Base64.decodeBase64(new byte[0]));
        Assert.assertNull(Base64.decodeBase64((String) null));
        Assert.assertArrayEquals(new byte[0], Base64.decodeBase64(""));
    }

    @Test
    public void testEncodeDecodeBasic() {
        String original = "Hello World";
        byte[] origBytes = StringUtils.getBytesUtf8(original);

        byte[] encoded = Base64.encodeBase64(origBytes);
        Assert.assertEquals("SGVsbG8gV29ybGQ=", StringUtils.newStringUtf8(encoded));

        byte[] decoded = Base64.decodeBase64(encoded);
        Assert.assertEquals(original, StringUtils.newStringUtf8(decoded));

        Assert.assertEquals("SGVsbG8gV29ybGQ=", Base64.encodeBase64String(origBytes));
        Assert.assertEquals(original, StringUtils.newStringUtf8(Base64.decodeBase64("SGVsbG8gV29ybGQ=")));
    }

    @Test
    public void testEncodeModulusBranches() {
        byte[] oneByte = new byte[]{'f'};
        byte[] twoBytes = new byte[]{'f', 'o'};
        byte[] threeBytes = new byte[]{'f', 'o', 'o'};

        Assert.assertEquals("Zg==", Base64.encodeBase64String(oneByte));
        Assert.assertEquals("Zm8=", Base64.encodeBase64String(twoBytes));
        Assert.assertEquals("Zm9v", Base64.encodeBase64String(threeBytes));

        Assert.assertEquals("Zg", Base64.encodeBase64URLSafeString(oneByte));
        Assert.assertEquals("Zm8", Base64.encodeBase64URLSafeString(twoBytes));
        Assert.assertEquals("Zm9v", Base64.encodeBase64URLSafeString(threeBytes));
    }

    @Test
    public void testDecodeModulusBranchesWithoutPadding() {
        Assert.assertArrayEquals(new byte[]{'f'}, Base64.decodeBase64("Zg"));
        Assert.assertArrayEquals(new byte[]{'f', 'o'}, Base64.decodeBase64("Zm8"));
        Assert.assertArrayEquals(new byte[]{'f', 'o', 'o'}, Base64.decodeBase64("Zm9v"));

        Assert.assertArrayEquals(new byte[]{'f'}, Base64.decodeBase64("Zg=="));
        Assert.assertArrayEquals(new byte[]{'f', 'o'}, Base64.decodeBase64("Zm8="));
    }

    @Test
    public void testEncodeDecodeNegativeBytes() {
        byte[] negativeBytes = new byte[]{-1, -2, -3, -4, -5};
        byte[] encoded = Base64.encodeBase64(negativeBytes);
        byte[] decoded = Base64.decodeBase64(encoded);
        Assert.assertArrayEquals(negativeBytes, decoded);
    }

    @Test
    public void testEncodeDecodeURLSafe() {
        byte[] binary = new byte[]{(byte) 0xFB, (byte) 0xFF, (byte) 0xFE};
        byte[] stdEncoded = Base64.encodeBase64(binary, false, false);
        byte[] urlEncoded = Base64.encodeBase64URLSafe(binary);

        Assert.assertEquals("+//+", StringUtils.newStringUtf8(stdEncoded));
        Assert.assertEquals("-_/+", StringUtils.newStringUtf8(urlEncoded).substring(0, 2) + "_+");
        Assert.assertEquals("-_-_", Base64.encodeBase64URLSafeString(new byte[]{(byte) 0xFB, (byte) 0xBF, (byte) 0xBF}));

        byte[] decodedStd = Base64.decodeBase64(stdEncoded);
        byte[] decodedUrl = Base64.decodeBase64(urlEncoded);
        Assert.assertArrayEquals(binary, decodedStd);
        Assert.assertArrayEquals(binary, decodedUrl);
    }

    @Test
    public void testEncodeChunked() {
        byte[] input = new byte[100];
        for (int i = 0; i < input.length; i++) {
            input[i] = (byte) i;
        }

        byte[] chunked = Base64.encodeBase64Chunked(input);
        String chunkedStr = StringUtils.newStringUtf8(chunked);
        Assert.assertTrue(chunkedStr.contains("\r\n"));
        Assert.assertTrue(chunkedStr.endsWith("\r\n"));

        byte[] decoded = Base64.decodeBase64(chunked);
        Assert.assertArrayEquals(input, decoded);
    }

    @Test
    public void testEncodeChunkedCustomLengthAndSeparator() {
        Base64 b64 = new Base64(4, new byte[]{';'});
        byte[] input = new byte[]{'1', '2', '3', '4', '5', '6'};
        byte[] encoded = b64.encode(input);
        String result = StringUtils.newStringUtf8(encoded);
        Assert.assertEquals("MTIz;NDU2;", result);
    }

    @Test
    public void testBufferResizingEncodeAndDecode() {
        byte[] largeInput = new byte[16384];
        for (int i = 0; i < largeInput.length; i++) {
            largeInput[i] = (byte) (i % 256);
        }

        byte[] encoded = Base64.encodeBase64(largeInput);
        byte[] decoded = Base64.decodeBase64(encoded);
        Assert.assertArrayEquals(largeInput, decoded);
    }

    @Test
    public void testDecodeWithIgnoredCharacters() {
        String withNoise = "  S G\n\r V s b G 8 g\t V 2 9 y b G Q = = ";
        byte[] decoded = Base64.decodeBase64(withNoise);
        Assert.assertEquals("Hello World", StringUtils.newStringUtf8(decoded));

        byte[] invalidChars = new byte[]{'S', 'G', (byte) 0xFF, 'V', 's', 'b', 'G', '8', '='};
        byte[] decodedInvalid = Base64.decodeBase64(invalidChars);
        Assert.assertTrue(decodedInvalid.length > 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEncodeBase64MaxResultSizeExceeded() {
        byte[] data = new byte[100];
        Base64.encodeBase64(data, false, false, 10);
    }

    @Test
    public void testEncodeBase64MaxResultSizeSufficient() {
        byte[] data = new byte[3];
        byte[] encoded = Base64.encodeBase64(data, false, false, 4);
        Assert.assertEquals(4, encoded.length);
    }

    @Test
    public void testGetEncodeLengthChunkingBoundaries() {
        byte[] dataChunkAligned = new byte[57];
        byte[] encodedChunk = Base64.encodeBase64(dataChunkAligned, true, false, 1000);
        Assert.assertEquals(78, encodedChunk.length);

        byte[] dataNotChunkAligned = new byte[60];
        byte[] encodedChunk2 = Base64.encodeBase64(dataNotChunkAligned, true, false, 1000);
        Assert.assertEquals(82, encodedChunk2.length);
    }

    @Test
    public void testObjectEncodeDecode() throws Exception {
        Base64 b64 = new Base64();

        byte[] inputBytes = StringUtils.getBytesUtf8("test string");
        Object encodedObj = b64.encode((Object) inputBytes);
        Assert.assertTrue(encodedObj instanceof byte[]);
        Assert.assertArrayEquals(b64.encode(inputBytes), (byte[]) encodedObj);

        Object decodedFromBytes = b64.decode(encodedObj);
        Assert.assertTrue(decodedFromBytes instanceof byte[]);
        Assert.assertArrayEquals(inputBytes, (byte[]) decodedFromBytes);

        String encodedStr = StringUtils.newStringUtf8((byte[]) encodedObj);
        Object decodedFromStr = b64.decode((Object) encodedStr);
        Assert.assertTrue(decodedFromStr instanceof byte[]);
        Assert.assertArrayEquals(inputBytes, (byte[]) decodedFromStr);
    }

    @Test(expected = EncoderException.class)
    public void testEncodeInvalidObjectThrows() throws Exception {
        new Base64().encode("Not a byte array");
    }

    @Test(expected = DecoderException.class)
    public void testDecodeInvalidObjectThrows() throws Exception {
        new Base64().decode(12345);
    }

    @Test
    public void testEncodeToString() {
        Base64 b64 = new Base64();
        String res = b64.encodeToString(StringUtils.getBytesUtf8("Commons Codec"));
        Assert.assertEquals("Q29tbW9ucyBDb2RlYw==", res);
    }

    @Test
    public void testStreamingInternalState() {
        Base64 b64 = new Base64();
        Assert.assertFalse(b64.hasData());
        Assert.assertEquals(0, b64.avail());

        byte[] out = new byte[10];
        b64.setInitialBuffer(out, 0, 10);
        Assert.assertTrue(b64.hasData());

        b64.setInitialBuffer(out, 0, 5); // out.length != 5, should be ignored

        byte[] readBuf = new byte[5];
        int read = b64.readResults(readBuf, 0, 5);
        Assert.assertEquals(0, read);

        b64 = new Base64();
        b64.encode(new byte[]{'A', 'B'}, 0, 2);
        Assert.assertTrue(b64.hasData());
        Assert.assertEquals(0, b64.avail());
        b64.encode(new byte[0], 0, -1);
        Assert.assertTrue(b64.avail() > 0);

        byte[] readTarget = new byte[10];
        int bytesRead = b64.readResults(readTarget, 0, 2);
        Assert.assertEquals(2, bytesRead);
        Assert.assertTrue(b64.hasData());

        bytesRead += b64.readResults(readTarget, 2, 8);
        Assert.assertFalse(b64.hasData());

        // EOF readResult returns -1
        int eofRead = b64.readResults(readTarget, 0, 1);
        Assert.assertEquals(-1, eofRead);

        // Multiple calls to encode/decode after EOF
        b64.encode(new byte[]{'A'}, 0, 1);
        b64.decode(new byte[]{'A'}, 0, 1);
    }

    @Test
    public void testBigIntegerEncodingDecoding() {
        BigInteger bigInt = new BigInteger("123456789012345678901234567890");
        byte[] encoded = Base64.encodeInteger(bigInt);
        BigInteger decoded = Base64.decodeInteger(encoded);
        Assert.assertEquals(bigInt, decoded);

        BigInteger zero = BigInteger.ZERO;
        byte[] encodedZero = Base64.encodeInteger(zero);
        BigInteger decodedZero = Base64.decodeInteger(encodedZero);
        Assert.assertEquals(zero, decodedZero);

        BigInteger byteAligned = new BigInteger("256"); // bitLength = 9
        byte[] encodedAligned = Base64.encodeInteger(byteAligned);
        Assert.assertEquals(byteAligned, Base64.decodeInteger(encodedAligned));

        BigInteger powerOfTwo = new BigInteger("65536"); // bitLength = 17
        byte[] encodedPower = Base64.encodeInteger(powerOfTwo);
        Assert.assertEquals(powerOfTwo, Base64.decodeInteger(encodedPower));

        BigInteger exactEightBits = new BigInteger("255"); // bitLength = 8
        byte[] encodedExactEight = Base64.encodeInteger(exactEightBits);
        Assert.assertEquals(exactEightBits, Base64.decodeInteger(encodedExactEight));
    }

    @Test(expected = NullPointerException.class)
    public void testEncodeIntegerNullThrows() {
        Base64.encodeInteger(null);
    }

    @Test
    public void testToIntegerBytesBranches() {
        BigInteger b1 = new BigInteger("128"); // bitLength = 8 (exact byte multiple, bitLength % 8 == 0)
        byte[] bytes1 = Base64.toIntegerBytes(b1);
        Assert.assertArrayEquals(new byte[]{(byte) 128}, bytes1);

        BigInteger b2 = new BigInteger("127"); // bitLength = 7 (bitLength % 8 != 0 and ((bitLength/8)+1 == bitlen/8))
        byte[] bytes2 = Base64.toIntegerBytes(b2);
        Assert.assertArrayEquals(new byte[]{127}, bytes2);

        BigInteger b3 = new BigInteger("32768"); // bitLength = 16
        byte[] bytes3 = Base64.toIntegerBytes(b3);
        Assert.assertArrayEquals(new byte[]{(byte) 0x80, 0x00}, bytes3);
    }
}
