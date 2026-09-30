package org.apache.commons.codec.binary;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;

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

        Base64 b7 = new Base64(64, null, false);
        Assert.assertFalse(b7.isUrlSafe());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithInvalidSeparator() {
        new Base64(64, new byte[]{'A', 'B'});
    }

    @Test
    public void testIsBase64() {
        Assert.assertTrue(Base64.isBase64((byte) '='));
        Assert.assertTrue(Base64.isBase64((byte) 'A'));
        Assert.assertTrue(Base64.isBase64((byte) 'z'));
        Assert.assertTrue(Base64.isBase64((byte) '0'));
        Assert.assertTrue(Base64.isBase64((byte) '9'));
        Assert.assertTrue(Base64.isBase64((byte) '+'));
        Assert.assertTrue(Base64.isBase64((byte) '/'));
        Assert.assertTrue(Base64.isBase64((byte) '-'));
        Assert.assertTrue(Base64.isBase64((byte) '_'));

        Assert.assertFalse(Base64.isBase64((byte) ' '));
        Assert.assertFalse(Base64.isBase64((byte) '\n'));
        Assert.assertFalse(Base64.isBase64((byte) '\r'));
        Assert.assertFalse(Base64.isBase64((byte) '\t'));
        Assert.assertFalse(Base64.isBase64((byte) 0));
        Assert.assertFalse(Base64.isBase64((byte) -1));
        Assert.assertFalse(Base64.isBase64((byte) 127));
        Assert.assertFalse(Base64.isBase64((byte) 200));
    }

    @Test
    public void testIsArrayByteBase64() {
        Assert.assertTrue(Base64.isArrayByteBase64(new byte[0]));
        Assert.assertTrue(Base64.isArrayByteBase64(StringUtils.getBytesUtf8("ABCD\r\n \t")));
        Assert.assertFalse(Base64.isArrayByteBase64(new byte[]{'A', 'B', (byte) 0x80}));
        Assert.assertFalse(Base64.isArrayByteBase64(new byte[]{'~'}));
    }

    @Test
    public void testDiscardWhitespace() {
        byte[] input = StringUtils.getBytesUtf8("A B\r\nC\tD");
        byte[] expected = StringUtils.getBytesUtf8("ABCD");
        Assert.assertArrayEquals(expected, Base64.discardWhitespace(input));
    }

    @Test
    public void testBasicEncodeDecode() {
        Base64 b64 = new Base64();

        Assert.assertNull(b64.encode((byte[]) null));
        Assert.assertArrayEquals(new byte[0], b64.encode(new byte[0]));

        Assert.assertNull(b64.decode((byte[]) null));
        Assert.assertArrayEquals(new byte[0], b64.decode(new byte[0]));

        String[] testStrings = {"", "f", "fo", "foo", "foob", "fooba", "foobar"};
        String[] expectedBase64 = {"", "Zg==", "Zm8=", "Zm9v", "Zm9vYg==", "Zm9vYmE=", "Zm9vYmFy"};

        for (int i = 0; i < testStrings.length; i++) {
            byte[] raw = StringUtils.getBytesUtf8(testStrings[i]);
            byte[] encoded = b64.encode(raw);
            Assert.assertEquals(expectedBase64[i], StringUtils.newStringUtf8(encoded));
            byte[] decoded = b64.decode(encoded);
            Assert.assertArrayEquals(raw, decoded);
        }
    }

    @Test
    public void testStaticEncodeDecode() {
        Assert.assertNull(Base64.encodeBase64(null));
        Assert.assertArrayEquals(new byte[0], Base64.encodeBase64(new byte[0]));

        byte[] raw = StringUtils.getBytesUtf8("Hello World");
        byte[] encoded = Base64.encodeBase64(raw);
        byte[] decoded = Base64.decodeBase64(encoded);
        Assert.assertArrayEquals(raw, decoded);

        byte[] decodedFromString = Base64.decodeBase64(StringUtils.newStringUtf8(encoded));
        Assert.assertArrayEquals(raw, decodedFromString);
    }

    @Test
    public void testUrlSafe() {
        byte[] binaryData = new byte[]{(byte) 0xfb, (byte) 0xff, (byte) 0xbf}; // produces + / in standard
        byte[] standardEncoded = Base64.encodeBase64(binaryData, false, false);
        byte[] urlSafeEncoded = Base64.encodeBase64(binaryData, false, true);

        Assert.assertEquals("+/+/ ", StringUtils.newStringUtf8(standardEncoded).replace("\r\n", " ") + " ");
        Assert.assertEquals("----", StringUtils.newStringUtf8(Base64.encodeBase64(new byte[]{(byte) 0xfb, (byte) 0xef, (byte) 0xbe}, false, true)));

        String urlSafeStr = Base64.encodeBase64URLSafeString(binaryData);
        Assert.assertEquals(StringUtils.newStringUtf8(urlSafeEncoded), urlSafeStr);

        Assert.assertArrayEquals(binaryData, Base64.decodeBase64(urlSafeEncoded));
        Assert.assertArrayEquals(binaryData, Base64.decodeBase64(standardEncoded));

        byte[] singleByte = new byte[]{(byte) 0xfb};
        byte[] singleUrlSafe = Base64.encodeBase64URLSafe(singleByte);
        Assert.assertFalse(StringUtils.newStringUtf8(singleUrlSafe).contains("="));
        Assert.assertArrayEquals(singleByte, Base64.decodeBase64(singleUrlSafe));

        byte[] twoBytes = new byte[]{(byte) 0xfb, (byte) 0xf0};
        byte[] twoUrlSafe = Base64.encodeBase64URLSafe(twoBytes);
        Assert.assertFalse(StringUtils.newStringUtf8(twoUrlSafe).contains("="));
        Assert.assertArrayEquals(twoBytes, Base64.decodeBase64(twoUrlSafe));
    }

    @Test
    public void testChunkedEncoding() {
        byte[] data = new byte[100];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i & 0xFF);
        }

        byte[] chunked = Base64.encodeBase64Chunked(data);
        String chunkedStr = StringUtils.newStringUtf8(chunked);
        Assert.assertTrue(chunkedStr.contains("\r\n"));
        Assert.assertTrue(chunkedStr.endsWith("\r\n"));

        byte[] decoded = Base64.decodeBase64(chunked);
        Assert.assertArrayEquals(data, decoded);

        String chunkedString = Base64.encodeBase64String(data);
        Assert.assertEquals(chunkedStr, chunkedString);

        byte[] customSeparator = new byte[]{';', ';'};
        Base64 customCodec = new Base64(20, customSeparator, false);
        byte[] customChunked = customCodec.encode(data);
        String customStr = StringUtils.newStringUtf8(customChunked);
        Assert.assertTrue(customStr.contains(";;"));
        Assert.assertTrue(customStr.endsWith(";;"));

        Assert.assertArrayEquals(data, customCodec.decode(customChunked));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEncodeBase64MaxResultSizeExceeded() {
        byte[] data = new byte[100];
        Base64.encodeBase64(data, false, false, 50);
    }

    @Test
    public void testObjectEncodeDecode() throws Exception {
        Base64 b64 = new Base64();

        byte[] raw = StringUtils.getBytesUtf8("Test Object Codec");
        Object encodedObj = b64.encode((Object) raw);
        Assert.assertTrue(encodedObj instanceof byte[]);
        Assert.assertArrayEquals(b64.encode(raw), (byte[]) encodedObj);

        Object decodedObj1 = b64.decode(encodedObj);
        Assert.assertTrue(decodedObj1 instanceof byte[]);
        Assert.assertArrayEquals(raw, (byte[]) decodedObj1);

        String encodedStr = b64.encodeToString(raw);
        Object decodedObj2 = b64.decode((Object) encodedStr);
        Assert.assertTrue(decodedObj2 instanceof byte[]);
        Assert.assertArrayEquals(raw, (byte[]) decodedObj2);
    }

    @Test(expected = EncoderException.class)
    public void testObjectEncodeInvalidType() throws Exception {
        Base64 b64 = new Base64();
        b64.encode("Not A Byte Array");
    }

    @Test(expected = DecoderException.class)
    public void testObjectDecodeInvalidType() throws Exception {
        Base64 b64 = new Base64();
        b64.decode(Integer.valueOf(12345));
    }

    @Test
    public void testDecodeWithIgnoredCharacters() {
        String base64WithGarbage = "Zm 9\n\r v\tYmFy"; // "foobar" with whitespace
        byte[] decoded = Base64.decodeBase64(base64WithGarbage);
        Assert.assertEquals("foobar", StringUtils.newStringUtf8(decoded));

        String base64WithNonBase64 = "Zm9v!@#$%^&*()_+=YmFy"; // some non-base64 chars
        byte[] decodedNonB64 = Base64.decodeBase64(StringUtils.getBytesUtf8(base64WithNonBase64));
        Assert.assertNotNull(decodedNonB64);
    }

    @Test
    public void testDecodeModulusHandling() {
        // Test decoding strings without padding (EOF with modulus 2 and 3)
        byte[] dec2 = Base64.decodeBase64("Zg"); // 'f'
        Assert.assertEquals("f", StringUtils.newStringUtf8(dec2));

        byte[] dec3 = Base64.decodeBase64("Zm8"); // 'fo'
        Assert.assertEquals("fo", StringUtils.newStringUtf8(dec3));

        // Test with PAD encountered early
        byte[] decPad = Base64.decodeBase64("Zg==");
        Assert.assertEquals("f", StringUtils.newStringUtf8(decPad));
    }

    @Test
    public void testBufferResizingAndStreaming() {
        Base64 b64 = new Base64(0);
        Assert.assertFalse(b64.hasData());
        Assert.assertEquals(0, b64.avail());

        byte[] largeData = new byte[16384];
        Arrays.fill(largeData, (byte) 'A');
        byte[] encoded = b64.encode(largeData);
        Assert.assertNotNull(encoded);

        byte[] decoded = b64.decode(encoded);
        Assert.assertArrayEquals(largeData, decoded);

        byte[] out = new byte[10];
        int read = b64.readResults(out, 0, out.length);
        Assert.assertEquals(-1, read);
    }

    @Test
    public void testReadResultsAndSetInitialBuffer() {
        Base64 b64 = new Base64();
        byte[] out = new byte[100];
        b64.setInitialBuffer(out, 0, 100);
        b64.encode(new byte[]{1, 2, 3}, 0, 3);
        b64.encode(new byte[0], 0, -1);

        Assert.assertTrue(b64.hasData());
        Assert.assertTrue(b64.avail() > 0);

        byte[] extracted = new byte[100];
        int count = b64.readResults(extracted, 0, 100);
        Assert.assertTrue(count > 0);
        Assert.assertFalse(b64.hasData());
    }

    @Test
    public void testEncodeDecodeInteger() {
        BigInteger bi1 = new BigInteger("12345678901234567890");
        byte[] encoded = Base64.encodeInteger(bi1);
        BigInteger bi2 = Base64.decodeInteger(encoded);
        Assert.assertEquals(bi1, bi2);

        BigInteger biAligned = new BigInteger("256");
        byte[] encAligned = Base64.encodeInteger(biAligned);
        BigInteger decAligned = Base64.decodeInteger(encAligned);
        Assert.assertEquals(biAligned, decAligned);

        BigInteger biZero = BigInteger.ZERO;
        byte[] encZero = Base64.encodeInteger(biZero);
        BigInteger decZero = Base64.decodeInteger(encZero);
        Assert.assertEquals(biZero, decZero);
    }

    @Test(expected = NullPointerException.class)
    public void testEncodeIntegerNull() {
        Base64.encodeInteger(null);
    }

    @Test
    public void testNegativeByteValues() {
        byte[] negativeBytes = new byte[]{-1, -2, -3, -4, -128};
        byte[] encoded = Base64.encodeBase64(negativeBytes);
        byte[] decoded = Base64.decodeBase64(encoded);
        Assert.assertArrayEquals(negativeBytes, decoded);
    }

    @Test
    public void testLineLengthChunkEdgeCases() {
        byte[] data = new byte[76];
        Arrays.fill(data, (byte) 'k');
        Base64 b64 = new Base64(76, new byte[]{'\n'});
        byte[] encoded = b64.encode(data);
        String res = StringUtils.newStringUtf8(encoded);
        Assert.assertTrue(res.endsWith("\n"));

        // multiple chunks
        byte[] data2 = new byte[150];
        Arrays.fill(data2, (byte) 'k');
        byte[] encoded2 = b64.encode(data2);
        String res2 = StringUtils.newStringUtf8(encoded2);
        Assert.assertTrue(res2.contains("\n"));
    }

    @Test
    public void testEncodeDecodeAlreadyEOF() {
        Base64 b64 = new Base64();
        b64.encode(new byte[]{1, 2}, 0, -1);
        // Repeated calls when eof is true
        b64.encode(new byte[]{1, 2}, 0, 2);

        Base64 b64Dec = new Base64();
        b64Dec.decode(new byte[]{1, 2}, 0, -1);
        // Repeated calls when eof is true
        b64Dec.decode(new byte[]{1, 2}, 0, 2);
    }
}
