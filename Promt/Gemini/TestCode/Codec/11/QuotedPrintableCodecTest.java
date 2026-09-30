package org.apache.commons.codec.net;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.junit.Assert;
import org.junit.Test;

public class QuotedPrintableCodecTest {

    private static final String PLAIN_TEXT = "Hello World! This is a simple test string with = and other chars: \t\r\n~";
    private static final String UTF8_TEXT = "Th\u00e9 qu\u00efck br\u00f6wn f\u00f6x";

    @Test
    public void testDefaultConstructor() {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        Assert.assertEquals(CharEncoding.UTF_8, codec.getDefaultCharset());
    }

    @Test
    public void testCustomCharsetConstructor() {
        QuotedPrintableCodec codec = new QuotedPrintableCodec(CharEncoding.ISO_8859_1);
        Assert.assertEquals(CharEncoding.ISO_8859_1, codec.getDefaultCharset());
    }

    @Test
    public void testEncodeNullByteArray() {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        Assert.assertNull(codec.encode((byte[]) null));
        Assert.assertNull(QuotedPrintableCodec.encodeQuotedPrintable(null, null));
    }

    @Test
    public void testDecodeNullByteArray() throws Exception {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        Assert.assertNull(codec.decode((byte[]) null));
        Assert.assertNull(QuotedPrintableCodec.decodeQuotedPrintable(null));
    }

    @Test
    public void testEncodeEmptyByteArray() {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        byte[] empty = new byte[0];
        byte[] encoded = codec.encode(empty);
        Assert.assertNotNull(encoded);
        Assert.assertEquals(0, encoded.length);
    }

    @Test
    public void testDecodeEmptyByteArray() throws Exception {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        byte[] empty = new byte[0];
        byte[] decoded = codec.decode(empty);
        Assert.assertNotNull(decoded);
        Assert.assertEquals(0, decoded.length);
    }

    @Test
    public void testEncodeQuotedPrintableWithNullBitSet() {
        byte[] input = "=Hello \tWorld=".getBytes(StandardCharsets.US_ASCII);
        byte[] encoded = QuotedPrintableCodec.encodeQuotedPrintable(null, input);
        String result = new String(encoded, StandardCharsets.US_ASCII);
        Assert.assertEquals("=3DHello \tWorld=3D", result);
    }

    @Test
    public void testEncodeQuotedPrintableWithCustomBitSet() {
        BitSet custom = new BitSet(256);
        custom.set('a');
        custom.set('b');
        byte[] input = "abc".getBytes(StandardCharsets.US_ASCII);
        byte[] encoded = QuotedPrintableCodec.encodeQuotedPrintable(custom, input);
        String result = new String(encoded, StandardCharsets.US_ASCII);
        Assert.assertEquals("ab=63", result);
    }

    @Test
    public void testEncodeNegativeBytes() throws Exception {
        byte[] input = new byte[] { (byte) -1, (byte) -128, (byte) 127, 0, (byte) '=' };
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        byte[] encoded = codec.encode(input);
        byte[] decoded = codec.decode(encoded);
        Assert.assertArrayEquals(input, decoded);
    }

    @Test
    public void testEncodeDecodeAsciiPrintable() throws Exception {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        String plain = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!\"#$%&'()*+,-./:;<>?@[\\]^_`{|}~ \t";
        String encoded = codec.encode(plain);
        String decoded = codec.decode(encoded);
        Assert.assertEquals(plain, decoded);
    }

    @Test
    public void testDecodeHexLowercaseAndUppercase() throws Exception {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        byte[] lowerEncoded = "=3dhello=3d".getBytes(StandardCharsets.US_ASCII);
        byte[] upperEncoded = "=3Dhello=3D".getBytes(StandardCharsets.US_ASCII);

        byte[] lowerDecoded = codec.decode(lowerEncoded);
        byte[] upperDecoded = codec.decode(upperEncoded);

        Assert.assertArrayEquals("=hello=".getBytes(StandardCharsets.US_ASCII), lowerDecoded);
        Assert.assertArrayEquals("=hello=".getBytes(StandardCharsets.US_ASCII), upperDecoded);
    }

    @Test
    public void testDecodeInvalidQuotedPrintableTrailingEquals() {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        try {
            codec.decode("test=".getBytes(StandardCharsets.US_ASCII));
            Assert.fail("Expected DecoderException for trailing '='");
        } catch (DecoderException expected) {
            Assert.assertTrue(expected.getMessage().contains("Invalid quoted-printable encoding"));
        }
    }

    @Test
    public void testDecodeInvalidQuotedPrintableSingleHexChar() {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        try {
            codec.decode("test=3".getBytes(StandardCharsets.US_ASCII));
            Assert.fail("Expected DecoderException for truncated sequence '=3'");
        } catch (DecoderException expected) {
            Assert.assertTrue(expected.getMessage().contains("Invalid quoted-printable encoding"));
        }
    }

    @Test
    public void testDecodeInvalidQuotedPrintableNonHexChar() {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        try {
            codec.decode("test=3Z".getBytes(StandardCharsets.US_ASCII));
            Assert.fail("Expected DecoderException for invalid hex character");
        } catch (DecoderException expected) {
            // expected from Utils.digit16
        }
    }

    @Test
    public void testEncodeStringDefaultCharset() throws Exception {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        String encoded = codec.encode(UTF8_TEXT);
        String decoded = codec.decode(encoded);
        Assert.assertEquals(UTF8_TEXT, decoded);
    }

    @Test
    public void testEncodeStringCustomCharset() throws Exception {
        QuotedPrintableCodec codec = new QuotedPrintableCodec(CharEncoding.ISO_8859_1);
        String encoded = codec.encode(UTF8_TEXT, CharEncoding.ISO_8859_1);
        String decoded = codec.decode(encoded, CharEncoding.ISO_8859_1);
        Assert.assertEquals(UTF8_TEXT, decoded);
    }

    @Test
    public void testEncodeDecodeNullString() throws Exception {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        Assert.assertNull(codec.encode((String) null));
        Assert.assertNull(codec.encode(null, CharEncoding.UTF_8));
        Assert.assertNull(codec.decode((String) null));
        Assert.assertNull(codec.decode(null, CharEncoding.UTF_8));
    }

    @Test
    public void testEncodeStringUnsupportedCharset() {
        QuotedPrintableCodec codec = new QuotedPrintableCodec("UNSUPPORTED-CHARSET");
        try {
            codec.encode("test");
            Assert.fail("Expected EncoderException for unsupported default charset");
        } catch (EncoderException expected) {
            Assert.assertTrue(expected.getCause() instanceof UnsupportedEncodingException);
        }

        try {
            codec.encode("test", "UNSUPPORTED-CHARSET");
            Assert.fail("Expected UnsupportedEncodingException");
        } catch (UnsupportedEncodingException expected) {
            // expected
        }
    }

    @Test
    public void testDecodeStringUnsupportedCharset() {
        QuotedPrintableCodec codec = new QuotedPrintableCodec("UNSUPPORTED-CHARSET");
        try {
            codec.decode("test");
            Assert.fail("Expected DecoderException for unsupported default charset");
        } catch (DecoderException expected) {
            Assert.assertTrue(expected.getCause() instanceof UnsupportedEncodingException);
        }

        try {
            codec.decode("test", "UNSUPPORTED-CHARSET");
            Assert.fail("Expected UnsupportedEncodingException");
        } catch (UnsupportedEncodingException expected) {
            // expected
        }
    }

    @Test
    public void testEncodeObject() throws Exception {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        Assert.assertNull(codec.encode((Object) null));

        byte[] inputBytes = "Hello".getBytes(StandardCharsets.UTF_8);
        Object encodedBytes = codec.encode((Object) inputBytes);
        Assert.assertTrue(encodedBytes instanceof byte[]);
        Assert.assertArrayEquals(codec.encode(inputBytes), (byte[]) encodedBytes);

        String inputString = "Hello World!";
        Object encodedString = codec.encode((Object) inputString);
        Assert.assertTrue(encodedString instanceof String);
        Assert.assertEquals(codec.encode(inputString), encodedString);
    }

    @Test(expected = EncoderException.class)
    public void testEncodeObjectInvalidType() throws Exception {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        codec.encode(12345);
    }

    @Test
    public void testDecodeObject() throws Exception {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        Assert.assertNull(codec.decode((Object) null));

        byte[] encodedBytes = codec.encode("Hello".getBytes(StandardCharsets.UTF_8));
        Object decodedBytes = codec.decode((Object) encodedBytes);
        Assert.assertTrue(decodedBytes instanceof byte[]);
        Assert.assertArrayEquals("Hello".getBytes(StandardCharsets.UTF_8), (byte[]) decodedBytes);

        String encodedString = codec.encode("Hello World!");
        Object decodedString = codec.decode((Object) encodedString);
        Assert.assertTrue(decodedString instanceof String);
        Assert.assertEquals("Hello World!", decodedString);
    }

    @Test(expected = DecoderException.class)
    public void testDecodeObjectInvalidType() throws Exception {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        codec.decode(Double.valueOf(3.14159));
    }

    @Test
    public void testAllByteValuesRoundTrip() throws Exception {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        byte[] allBytes = new byte[256];
        for (int i = 0; i < 256; i++) {
            allBytes[i] = (byte) i;
        }

        byte[] encoded = codec.encode(allBytes);
        byte[] decoded = codec.decode(encoded);

        Assert.assertArrayEquals(allBytes, decoded);
    }

    @Test
    public void testSpecialCharacterEncoding() throws Exception {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        String input = "= \t\r\n";
        String encoded = codec.encode(input);
        Assert.assertEquals("=3D =09=0D=0A", encoded);
        Assert.assertEquals(input, codec.decode(encoded));
    }

    @Test
    public void testGetDefaultCharset() {
        QuotedPrintableCodec codec1 = new QuotedPrintableCodec();
        Assert.assertEquals(CharEncoding.UTF_8, codec1.getDefaultCharset());

        QuotedPrintableCodec codec2 = new QuotedPrintableCodec(CharEncoding.US_ASCII);
        Assert.assertEquals(CharEncoding.US_ASCII, codec2.getDefaultCharset());

        QuotedPrintableCodec codec3 = new QuotedPrintableCodec(null);
        Assert.assertNull(codec3.getDefaultCharset());
    }
}
