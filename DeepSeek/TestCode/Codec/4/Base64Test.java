package org.apache.commons.codec.binary;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigInteger;

import org.junit.Test;

public class Base64Test {

    // --- Static encodeBase64 / decodeBase64 ---

    @Test
    public void testEncodeBase64Basic() {
        byte[] input = "Hello World".getBytes();
        byte[] encoded = Base64.encodeBase64(input);
        assertNotNull(encoded);
        // Check standard encoding
        byte[] expected = java.util.Base64.getEncoder().encode(input);
        assertArrayEquals(expected, encoded);
    }

    @Test
    public void testEncodeBase64Empty() {
        byte[] input = new byte[0];
        byte[] encoded = Base64.encodeBase64(input);
        assertArrayEquals(input, encoded);
    }

    @Test
    public void testEncodeBase64Null() {
        byte[] encoded = Base64.encodeBase64(null);
        assertEquals(null, encoded);
    }

    @Test
    public void testEncodeBase64ChunkedFalse() {
        byte[] input = "The quick brown fox jumps over the lazy dog".getBytes();
        byte[] encoded = Base64.encodeBase64(input, false);
        assertNotNull(encoded);
    }

    @Test
    public void testEncodeBase64ChunkedTrue() {
        byte[] input = new byte[100]; // large enough to produce multiple lines
        for (int i = 0; i < input.length; i++) input[i] = (byte) i;
        byte[] encoded = Base64.encodeBase64(input, true);
        assertTrue(encoded.length > input.length);
        // Check CRLF presence
        String s = new String(encoded, java.nio.charset.StandardCharsets.US_ASCII);
        assertTrue(s.contains("\r\n"));
    }

    @Test
    public void testEncodeBase64UrlSafe() {
        byte[] input = "test".getBytes();
        byte[] encoded = Base64.encodeBase64(input, false, true);
        String s = new String(encoded, java.nio.charset.StandardCharsets.US_ASCII);
        assertFalse(s.contains("+") && !s.contains("/"));
        assertTrue(s.contains("-") || s.contains("_"));
    }

    @Test
    public void testEncodeBase64UrlSafeChunked() {
        byte[] input = new byte[100];
        for (int i = 0; i < input.length; i++) input[i] = (byte) i;
        byte[] encoded = Base64.encodeBase64(input, true, true);
        assertTrue(encoded.length > 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEncodeBase64MaxResultSizeExceeded() {
        byte[] large = new byte[1000];
        Base64.encodeBase64(large, false, false, 10); // max size too small
    }

    @Test
    public void testEncodeBase64String() {
        String result = Base64.encodeBase64String("input".getBytes());
        assertNotNull(result);
        assertTrue(result.length() > 0);
    }

    @Test
    public void testEncodeBase64URLSafe() {
        byte[] result = Base64.encodeBase64URLSafe("input".getBytes());
        assertNotNull(result);
        String s = new String(result, java.nio.charset.StandardCharsets.US_ASCII);
        assertFalse(s.contains("+") && !s.contains("/"));
    }

    @Test
    public void testEncodeBase64URLSafeString() {
        String result = Base64.encodeBase64URLSafeString("input".getBytes());
        assertNotNull(result);
    }

    @Test
    public void testEncodeBase64ChunkedMethod() {
        byte[] input = new byte[100];
        byte[] encoded = Base64.encodeBase64Chunked(input);
        assertTrue(encoded.length > input.length);
    }

    @Test
    public void testDecodeBase64ByteArray() {
        byte[] input = "Hello World".getBytes();
        byte[] encoded = Base64.encodeBase64(input);
        byte[] decoded = Base64.decodeBase64(encoded);
        assertArrayEquals(input, decoded);
    }

    @Test
    public void testDecodeBase64String() {
        String encoded = Base64.encodeBase64String("Hello".getBytes());
        byte[] decoded = Base64.decodeBase64(encoded);
        assertArrayEquals("Hello".getBytes(), decoded);
    }

    @Test
    public void testDecodeBase64Empty() {
        byte[] decoded = Base64.decodeBase64(new byte[0]);
        assertEquals(0, decoded.length);
    }

    @Test
    public void testDecodeBase64Null() {
        byte[] decoded = Base64.decodeBase64(null);
        assertEquals(null, decoded);
    }

    // --- isBase64, isArrayByteBase64 ---

    @Test
    public void testIsBase64Valid() {
        assertTrue(Base64.isBase64((byte) 'A'));
        assertTrue(Base64.isBase64((byte) 'Z'));
        assertTrue(Base64.isBase64((byte) 'a'));
        assertTrue(Base64.isBase64((byte) 'z'));
        assertTrue(Base64.isBase64((byte) '0'));
        assertTrue(Base64.isBase64((byte) '9'));
        assertTrue(Base64.isBase64((byte) '+'));
        assertTrue(Base64.isBase64((byte) '/'));
        assertTrue(Base64.isBase64((byte) '=')); // PAD
    }

    @Test
    public void testIsBase64Invalid() {
        assertFalse(Base64.isBase64((byte) '~'));
        assertFalse(Base64.isBase64((byte) '\n'));
        assertFalse(Base64.isBase64((byte) ';'));
        assertFalse(Base64.isBase64((byte) -1)); // negative
    }

    @Test
    public void testIsArrayByteBase64AllValid() {
        byte[] valid = "ABC+/=". getBytes();
        assertTrue(Base64.isArrayByteBase64(valid));
    }

    @Test
    public void testIsArrayByteBase64WithWhitespace() {
        byte[] mixed = "AB C\n\r\t+".getBytes();
        assertTrue(Base64.isArrayByteBase64(mixed));
    }

    @Test
    public void testIsArrayByteBase64Invalid() {
        byte[] invalid = "ABC~".getBytes();
        assertFalse(Base64.isArrayByteBase64(invalid));
    }

    @Test
    public void testIsArrayByteBase64Empty() {
        assertTrue(Base64.isArrayByteBase64(new byte[0]));
    }

    // --- encodeInteger / decodeInteger ---

    @Test(expected = NullPointerException.class)
    public void testEncodeIntegerNull() {
        Base64.encodeInteger(null);
    }

    @Test
    public void testEncodeDecodeIntegerRoundtrip() {
        BigInteger bigInt = new BigInteger("12345678901234567890");
        byte[] encoded = Base64.encodeInteger(bigInt);
        assertNotNull(encoded);
        BigInteger decoded = Base64.decodeInteger(encoded);
        assertEquals(bigInt, decoded);
    }

    @Test
    public void testEncodeIntegerZero() {
        BigInteger zero = BigInteger.ZERO;
        byte[] encoded = Base64.encodeInteger(zero);
        BigInteger decoded = Base64.decodeInteger(encoded);
        assertEquals(zero, decoded);
    }

    @Test
    public void testEncodeIntegerNegative() {
        BigInteger neg = new BigInteger("-987654321");
        byte[] encoded = Base64.encodeInteger(neg);
        BigInteger decoded = Base64.decodeInteger(encoded);
        assertEquals(neg, decoded);
    }

    // --- Instance constructors and encode/decode ---

    @Test
    public void testDefaultConstructor() {
        Base64 b64 = new Base64();
        assertFalse(b64.isUrlSafe());
    }

    @Test
    public void testUrlSafeConstructor() {
        Base64 b64 = new Base64(true);
        assertTrue(b64.isUrlSafe());
        b64 = new Base64(false);
        assertFalse(b64.isUrlSafe());
    }

    @Test
    public void testLineLengthConstructor() {
        Base64 b64 = new Base64(76);
        // Encode with chunking
        byte[] input = new byte[100];
        byte[] encoded = b64.encode(input);
        String s = new String(encoded, java.nio.charset.StandardCharsets.US_ASCII);
        assertTrue(s.contains("\r\n"));
    }

    @Test
    public void testLineLengthZeroConstructor() {
        Base64 b64 = new Base64(0);
        byte[] input = new byte[100];
        byte[] encoded = b64.encode(input);
        String s = new String(encoded, java.nio.charset.StandardCharsets.US_ASCII);
        assertFalse(s.contains("\r\n"));
    }

    @Test
    public void testLineLengthAndSeparatorConstructor() {
        byte[] sep = {'\r', '\n'};
        Base64 b64 = new Base64(76, sep);
        byte[] input = new byte[100];
        byte[] encoded = b64.encode(input);
        assertTrue(encoded.length > 0);
    }

    @Test
    public void testLineSeparatorContainsBase64CharThrows() {
        byte[] badSep = {'A', 'B'}; // 'A' is base64 char
        try {
            new Base64(76, badSep, false);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testConstructorFullParams() {
        Base64 b64 = new Base64(0, null, true);
        assertTrue(b64.isUrlSafe());
        // null separator triggers lineLength=0?
    }

    @Test
    public void testEncodeInstanceByteArray() {
        Base64 b64 = new Base64();
        byte[] input = "input".getBytes();
        byte[] encoded = b64.encode(input);
        assertNotNull(encoded);
        // roundtrip via static method
        byte[] decoded = Base64.decodeBase64(encoded);
        assertArrayEquals(input, decoded);
    }

    @Test
    public void testEncodeInstanceEmpty() {
        Base64 b64 = new Base64();
        byte[] encoded = b64.encode(new byte[0]);
        assertEquals(0, encoded.length);
    }

    @Test
    public void testEncodeInstanceNull() {
        Base64 b64 = new Base64();
        assertEquals(null, b64.encode(null));
    }

    @Test
    public void testEncodeToString() {
        Base64 b64 = new Base64();
        String s = b64.encodeToString("test".getBytes());
        assertNotNull(s);
        assertTrue(s.length() > 0);
    }

    @Test
    public void testDecodeInstanceByteArray() {
        Base64 b64 = new Base64();
        byte[] encoded = Base64.encodeBase64("decode".getBytes());
        byte[] decoded = b64.decode(encoded);
        assertArrayEquals("decode".getBytes(), decoded);
    }

    @Test
    public void testDecodeInstanceString() {
        Base64 b64 = new Base64();
        String encoded = Base64.encodeBase64String("hello".getBytes());
        byte[] decoded = b64.decode(encoded);
        assertArrayEquals("hello".getBytes(), decoded);
    }

    @Test
    public void testDecodeInstanceEmpty() {
        Base64 b64 = new Base64();
        assertArrayEquals(new byte[0], b64.decode(new byte[0]));
    }

    @Test
    public void testDecodeInstanceNull() {
        Base64 b64 = new Base64();
        assertEquals(null, b64.decode((byte[]) null));
    }

    @Test(expected = DecoderException.class)
    public void testDecodeObjectInvalidType() throws DecoderException {
        Base64 b64 = new Base64();
        b64.decode(new Integer(5));
    }

    @Test
    public void testDecodeObjectByteArray() throws DecoderException {
        Base64 b64 = new Base64();
        byte[] input = "xyz".getBytes();
        byte[] encoded = Base64.encodeBase64(input);
        Object result = b64.decode((Object) encoded);
        assertTrue(result instanceof byte[]);
        assertArrayEquals(input, (byte[]) result);
    }

    @Test
    public void testDecodeObjectString() throws DecoderException {
        Base64 b64 = new Base64();
        String encoded = Base64.encodeBase64String("abc".getBytes());
        Object result = b64.decode((Object) encoded);
        assertTrue(result instanceof byte[]);
        assertArrayEquals("abc".getBytes(), (byte[]) result);
    }

    @Test(expected = EncoderException.class)
    public void testEncodeObjectInvalidType() throws EncoderException {
        Base64 b64 = new Base64();
        b64.encode(new Object());
    }

    @Test
    public void testEncodeObjectByteArray() throws EncoderException {
        Base64 b64 = new Base64();
        byte[] input = "data".getBytes();
        Object result = b64.encode((Object) input);
        assertTrue(result instanceof byte[]);
        assertArrayEquals(Base64.encodeBase64(input), (byte[]) result);
    }

    @Test
    public void testHasDataInitiallyFalse() {
        Base64 b64 = new Base64();
        assertFalse(b64.hasData());
    }

    @Test
    public void testHasDataAfterEncode() {
        Base64 b64 = new Base64();
        b64.encode("abc".getBytes());
        assertTrue(b64.hasData());
    }

    @Test
    public void testAvailInitiallyZero() {
        Base64 b64 = new Base64();
        assertEquals(0, b64.avail());
    }

    @Test
    public void testAvailAfterEncode() {
        Base64 b64 = new Base64();
        b64.encode("data".getBytes());
        assertTrue(b64.avail() > 0);
    }

    @Test
    public void testEncodeMultipleCallsSameInstance() {
        Base64 b64 = new Base64();
        byte[] first = b64.encode("first".getBytes());
        byte[] second = b64.encode("second".getBytes());
        assertNotNull(first);
        assertNotNull(second);
        assertFalse(java.util.Arrays.equals(first, second));
    }

    @Test
    public void testUrlSafeModePaddingOmitted() {
        Base64 b64 = new Base64(true);
        byte[] input = {1}; // one byte
        byte[] encoded = b64.encode(input);
        String s = new String(encoded, java.nio.charset.StandardCharsets.US_ASCII);
        assertFalse(s.endsWith("=")); 
    }

    @Test
    public void testStandardModePaddingPresent() {
        Base64 b64 = new Base64(false);
        byte[] input = {1};
        byte[] encoded = b64.encode(input);
        String s = new String(encoded, java.nio.charset.StandardCharsets.US_ASCII);
        assertTrue(s.endsWith("=="));
    }

    @Test
    public void testDecodeWithPaddingAndLineSeparators() {
        // chunked decode with CRLF
        byte[] input = new byte[100];
        for (int i = 0; i < input.length; i++) input[i] = (byte) i;
        Base64 b64 = new Base64(76); // chunked
        byte[] encoded = b64.encode(input);
        Base64 decoder = new Base64();
        byte[] decoded = decoder.decode(encoded);
        assertArrayEquals(input, decoded);
    }

    @Test
    public void testDiscardWhitespaceDeprecated() {
        byte[] data = " A B\nC\t\r".getBytes();
        byte[] groomed = Base64.discardWhitespace(data);
        assertEquals("ABC", new String(groomed));
    }

    // Test lineLength constructor ensures multiple of 4
    @Test
    public void testLineLengthRounding() {
        Base64 b64 = new Base64(77); // should become 76
        byte[] input = new byte[100];
        byte[] encoded = b64.encode(input);
        String s = new String(encoded, java.nio.charset.StandardCharsets.US_ASCII);
        // line length 76 -> lines of 76 chars
        String[] lines = s.split("\r\n");
        assertTrue(lines[0].length() <= 76);
}
```
