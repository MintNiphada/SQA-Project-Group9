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
        Base64 b1 = new Base64();
        Assert.assertFalse(b1.isUrlSafe());

        Base64 b2 = new Base64(true);
        Assert.assertTrue(b2.isUrlSafe());

        Base64 b3 = new Base64(false);
        Assert.assertFalse(b3.isUrlSafe());

        Base64 b4 = new Base64(64);
        Assert.assertFalse(b4.isUrlSafe());

        Base64 b5 = new Base64(64, new byte[]{'\n'});
        Assert.assertFalse(b5.isUrlSafe());

        Base64 b6 = new Base64(64, new byte[]{'\n'}, true);
        Assert.assertTrue(b6.isUrlSafe());

        Base64 b7 = new Base64(-1, null, false);
        Assert.assertFalse(b7.isUrlSafe());

        Base64 b8 = new Base64(10, null, true);
        Assert.assertTrue(b8.isUrlSafe());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithBase64Separator() {
        new Base64(64, new byte[]{'A', '\n'});
    }

    @Test
    public void testEncodeDecodeEmptyAndNull() {
        Base64 b64 = new Base64();
        Assert.assertNull(b64.encode((byte[]) null));
        Assert.assertEquals(0, b64.encode(new byte[0]).length);
        Assert.assertNull(b64.decode((byte[]) null));
        Assert.assertEquals(0, b64.decode(new byte[0]).length);
        Assert.assertNull(Base64.encodeBase64(null));
        Assert.assertNull(Base64.decodeBase64((byte[]) null));
        Assert.assertNull(Base64.decodeBase64((String) null));
    }

    @Test
    public void testBasicEncodeDecode() {
        String original = "Hello World!";
        byte[] encoded = Base64.encodeBase64(StringUtils.getBytesUtf8(original));
        byte[] decoded = Base64.decodeBase64(encoded);
        Assert.assertEquals(original, StringUtils.newStringUtf8(decoded));

        String encodedString = Base64.encodeBase64String(StringUtils.getBytesUtf8(original));
        byte[] decodedFromString = Base64.decodeBase64(encodedString);
        Assert.assertEquals(original, StringUtils.newStringUtf8(decodedFromString));
    }

    @Test
    public void testEncodeModulusCases() {
        // modulus 1 (1 byte in -> 2 chars + 2 pad)
        byte[] in1 = new byte[]{'f'};
        Assert.assertEquals("Zg==", Base64.encodeBase64String(in1).trim());

        // modulus 2 (2 bytes in -> 3 chars + 1 pad)
        byte[] in2 = new byte[]{'f', 'o'};
        Assert.assertEquals("Zm8=", Base64.encodeBase64String(in2).trim());

        // modulus 0 (3 bytes in -> 4 chars + 0 pad)
        byte[] in3 = new byte[]{'f', 'o', 'o'};
        Assert.assertEquals("Zm9v", Base64.encodeBase64String(in3).trim());
    }

    @Test
    public void testNegativeByteEncoding() {
        byte[] in = new byte[]{-1, -2, -3};
        byte[] encoded = Base64.encodeBase64(in);
        byte[] decoded = Base64.decodeBase64(encoded);
        Assert.assertArrayEquals(in, decoded);
    }

    @Test
    public void testUrlSafeEncoding() {
        byte[] binary = new byte[]{(byte) 0xfb, (byte) 0xff, (byte) 0xbf};
        byte[] standard = Base64.encodeBase64(binary, false, false);
        byte[] urlSafe = Base64.encodeBase64(binary, false, true);

        Assert.assertEquals("+/+/".substring(0, 4), StringUtils.newStringUtf8(standard));
        Assert.assertEquals("-_-_".substring(0, 4), StringUtils.newStringUtf8(urlSafe));

        byte[] in1 = new byte[]{(byte) 0xfb};
        Base64 urlCodec = new Base64(true);
        byte[] urlSafeMod1 = urlCodec.encode(in1);
        Assert.assertEquals("+w==".replace('+', '-').replace("=", ""), StringUtils.newStringUtf8(urlSafeMod1));

        byte[] in2 = new byte[]{(byte) 0xfb, (byte) 0xff};
        byte[] urlSafeMod2 = urlCodec.encode(in2);
        Assert.assertEquals("+/8=".replace('+', '-').replace('/', '_').replace("=", ""), StringUtils.newStringUtf8(urlSafeMod2));

        Assert.assertArrayEquals(in1, urlCodec.decode(urlSafeMod1));
        Assert.assertArrayEquals(in2, urlCodec.decode(urlSafeMod2));
        Assert.assertEquals(StringUtils.newStringUtf8(urlSafe), Base64.encodeBase64URLSafeString(binary));
        Assert.assertArrayEquals(urlSafe, Base64.encodeBase64URLSafe(binary));
    }

    @Test
    public void testChunkedEncoding() {
        byte[] bytes = new byte[100];
        Arrays.fill(bytes, (byte) 'A');
        byte[] chunked = Base64.encodeBase64Chunked(bytes);
        String chunkedStr = StringUtils.newStringUtf8(chunked);
        Assert.assertTrue(chunkedStr.contains("\r\n"));

        byte[] decoded = Base64.decodeBase64(chunked);
        Assert.assertArrayEquals(bytes, decoded);
    }

    @Test
    public void testChunkedEncodingBoundary() {
        byte[] bytes = new byte[57];
        Arrays.fill(bytes, (byte) 'B');
        byte[] chunked = Base64.encodeBase64Chunked(bytes);
        Assert.assertTrue(StringUtils.newStringUtf8(chunked).endsWith("\r\n"));
    }

    @Test
    public void testDecodeWithPaddingAndNoPadding() {
        Assert.assertEquals("f", StringUtils.newStringUtf8(Base64.decodeBase64("Zg==")));
        Assert.assertEquals("f", StringUtils.newStringUtf8(Base64.decodeBase64("Zg")));
        Assert.assertEquals("fo", StringUtils.newStringUtf8(Base64.decodeBase64("Zm8=")));
        Assert.assertEquals("fo", StringUtils.newStringUtf8(Base64.decodeBase64("Zm8")));
        Assert.assertEquals("foo", StringUtils.newStringUtf8(Base64.decodeBase64("Zm9v")));
    }

    @Test
    public void testDecodeGarbageAndWhitespace() {
        String dirty = " Z m\r\n 9\tv = = ";
        byte[] decoded = Base64.decodeBase64(dirty);
        Assert.assertEquals("foo", StringUtils.newStringUtf8(decoded));

        byte[] outOfBounds = new byte[]{(byte) 0x80, (byte) 0xFF, 'Z', 'm', '9', 'v'};
        Assert.assertEquals("foo", StringUtils.newStringUtf8(Base64.decodeBase64(outOfBounds)));

        byte[] nonBase64Char = new byte[]{'~', 'Z', '!', 'm', '@', '9', '#', 'v'};
        Assert.assertEquals("foo", StringUtils.newStringUtf8(Base64.decodeBase64(nonBase64Char)));
    }

    @Test
    public void testIsBase64() {
        Assert.assertTrue(Base64.isBase64((byte) 'A'));
        Assert.assertTrue(Base64.isBase64((byte) 'z'));
        Assert.assertTrue(Base64.isBase64((byte) '0'));
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
    public void testIsArrayByteBase64() {
        Assert.assertTrue(Base64.isArrayByteBase64(new byte[0]));
        Assert.assertTrue(Base64.isArrayByteBase64(new byte[]{'A', 'B', ' ', '\t', '\r', '\n'}));
        Assert.assertFalse(Base64.isArrayByteBase64(new byte[]{'A', '%'}));
    }

    @Test
    public void testObjectEncodeDecode() throws Exception {
        Base64 b64 = new Base64();
        byte[] input = StringUtils.getBytesUtf8("Hello");
        Object encodedObj = b64.encode((Object) input);
        Assert.assertTrue(encodedObj instanceof byte[]);
        Assert.assertEquals("SGVsbG8=", StringUtils.newStringUtf8((byte[]) encodedObj));

        Object decodedFromBytes = b64.decode(encodedObj);
        Assert.assertTrue(decodedFromBytes instanceof byte[]);
        Assert.assertEquals("Hello", StringUtils.newStringUtf8((byte[]) decodedFromBytes));

        Object decodedFromString = b64.decode((Object) "SGVsbG8=");
        Assert.assertTrue(decodedFromString instanceof byte[]);
        Assert.assertEquals("Hello", StringUtils.newStringUtf8((byte[]) decodedFromString));
    }

    @Test(expected = EncoderException.class)
    public void testEncodeInvalidObject() throws Exception {
        new Base64().encode("Invalid Object");
    }

    @Test(expected = DecoderException.class)
    public void testDecodeInvalidObject() throws Exception {
        new Base64().decode(12345);
    }

    @Test
    public void testEncodeToString() {
        Base64 b64 = new Base64();
        Assert.assertEquals("SGVsbG8=", b64.encodeToString(StringUtils.getBytesUtf8("Hello")));
    }

    @Test
    public void testDecodeStringMethod() {
        Base64 b64 = new Base64();
        Assert.assertEquals("Hello", StringUtils.newStringUtf8(b64.decode("SGVsbG8=")));
    }

    @Test
    public void testBufferResizingOnEncodeAndDecode() {
        byte[] large = new byte[10000];
        for (int i = 0; i < large.length; i++) {
            large[i] = (byte) (i % 256);
        }
        Base64 b64 = new Base64();
        byte[] encoded = b64.encode(large);
        byte[] decoded = b64.decode(encoded);
        Assert.assertArrayEquals(large, decoded);
    }

    @Test
    public void testReadResultsAndAvailAndHasData() {
        Base64 b64 = new Base64();
        Assert.assertFalse(b64.hasData());
        Assert.assertEquals(0, b64.avail());
        byte[] out = new byte[10];
        Assert.assertEquals(0, b64.readResults(out, 0, out.length));

        b64.encode(new byte[]{'a', 'b', 'c'}, 0, 3);
        Assert.assertTrue(b64.hasData());
        Assert.assertTrue(b64.avail() > 0);

        byte[] dest = new byte[2];
        int read = b64.readResults(dest, 0, 2);
        Assert.assertEquals(2, read);
        Assert.assertTrue(b64.hasData());

        byte[] remaining = new byte[10];
        int readRemaining = b64.readResults(remaining, 0, 10);
        Assert.assertEquals(2, readRemaining);
        Assert.assertFalse(b64.hasData());
    }

    @Test
    public void testReadResultsBufferSameAsDest() {
        Base64 b64 = new Base64();
        byte[] buffer = new byte[10];
        b64.setInitialBuffer(buffer, 0, 10);
        b64.encode(new byte[]{'a', 'b', 'c'}, 0, 3);
        int read = b64.readResults(buffer, 0, 10);
        Assert.assertEquals(4, read);
        Assert.assertFalse(b64.hasData());
    }

    @Test
    public void testEncodeDecodeStreamingEof() {
        Base64 b64 = new Base64();
        b64.encode(new byte[]{'a'}, 0, -1);
        Assert.assertEquals(0, b64.avail());
        b64.encode(new byte[]{'a'}, 0, 1);
        Assert.assertEquals(0, b64.avail());

        Base64 decoder = new Base64();
        decoder.decode(new byte[]{'Z', 'g', '=', '='}, 0, 4);
        decoder.decode(new byte[0], 0, -1);
        decoder.decode(new byte[]{'A'}, 0, 1);
        byte[] res = new byte[10];
        int read = decoder.readResults(res, 0, 10);
        Assert.assertEquals(1, read);
        Assert.assertEquals('f', res[0]);
    }

    @Test
    public void testDiscardWhitespace() {
        byte[] src = StringUtils.getBytesUtf8(" A \r\n B \t C ");
        byte[] cleaned = Base64.discardWhitespace(src);
        Assert.assertEquals("ABC", StringUtils.newStringUtf8(cleaned));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEncodeBase64MaxSizeExceeded() {
        byte[] data = new byte[100];
        Base64.encodeBase64(data, false, false, 50);
    }

    @Test
    public void testBigIntegerEncodingDecoding() {
        BigInteger bigInt = new BigInteger("123456789012345678901234567890");
        byte[] encoded = Base64.encodeInteger(bigInt);
        BigInteger decoded = Base64.decodeInteger(encoded);
        Assert.assertEquals(bigInt, decoded);

        BigInteger aligned = new BigInteger("255");
        byte[] encAligned = Base64.encodeInteger(aligned);
        Assert.assertEquals(aligned, Base64.decodeInteger(encAligned));

        BigInteger zero = BigInteger.ZERO;
        byte[] encZero = Base64.encodeInteger(zero);
        Assert.assertEquals(zero, Base64.decodeInteger(encZero));
    }

    @Test(expected = NullPointerException.class)
    public void testEncodeIntegerNull() {
        Base64.encodeInteger(null);
    }

    @Test
    public void testToIntegerBytesBranches() {
        BigInteger biNonAligned = new BigInteger("65535"); // 16-bit
        byte[] bytesNonAligned = Base64.toIntegerBytes(biNonAligned);
        Assert.assertEquals(2, bytesNonAligned.length);

        BigInteger biPadded = new BigInteger("1"); // 1-bit -> 1 byte
        byte[] bytesPadded = Base64.toIntegerBytes(biPadded);
        Assert.assertEquals(1, bytesPadded.length);
        Assert.assertEquals(1, bytesPadded[0]);
    }

    @Test
    public void testEmptyArrayOperations() {
        Assert.assertArrayEquals(new byte[0], Base64.encodeBase64(new byte[0], true, true, 100));
        Assert.assertArrayEquals(new byte[0], Base64.decodeBase64(new byte[0]));
        Assert.assertEquals("", Base64.encodeBase64String(new byte[0]));
        Assert.assertEquals("", Base64.encodeBase64URLSafeString(new byte[0]));
    }
}
