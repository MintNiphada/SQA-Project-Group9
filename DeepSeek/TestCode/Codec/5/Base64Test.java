package org.apache.commons.codec.binary;

import static org.junit.Assert.*;
import org.junit.Test;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.binary.StringUtils;

public class Base64Test {

    @Test
    public void testConstructorDefault() {
        Base64 b64 = new Base64();
        assertFalse(b64.isUrlSafe());
        byte[] encoded = b64.encode("Hello".getBytes());
        assertNotNull(encoded);
        assertTrue(encoded.length > 0);
    }

    @Test
    public void testConstructorBooleanTrue() {
        Base64 b64 = new Base64(true);
        assertTrue(b64.isUrlSafe());
        assertEquals(76, b64.lineLength);
    }

    @Test
    public void testConstructorBooleanFalse() {
        Base64 b64 = new Base64(false);
        assertFalse(b64.isUrlSafe());
        assertEquals(76, b64.lineLength);
    }

    @Test
    public void testConstructorIntZero() {
        Base64 b64 = new Base64(0);
        assertFalse(b64.isUrlSafe());
        assertEquals(0, b64.lineLength);
    }

    @Test
    public void testConstructorIntPositive() {
        Base64 b64 = new Base64(10);
        assertEquals(8, b64.lineLength); // rounded down to multiple of 4
    }

    @Test
    public void testConstructorIntByteArrayNullSeparator() {
        Base64 b64 = new Base64(76, null);
        assertEquals(0, b64.lineLength);
    }

    @Test
    public void testConstructorIntByteArrayBoolean() {
        byte[] sep = {'\n'};
        Base64 b64 = new Base64(76, sep, true);
        assertTrue(b64.isUrlSafe());
        assertEquals(76, b64.lineLength);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorLineSeparatorContainsBase64Char() {
        byte[] sep = {'A'};
        new Base64(76, sep);
    }

    @Test
    public void testIsUrlSafe() {
        Base64 standard = new Base64();
        assertFalse(standard.isUrlSafe());
        Base64 urlSafe = new Base64(true);
        assertTrue(urlSafe.isUrlSafe());
    }

    @Test
    public void testIsBase64() {
        assertTrue(Base64.isBase64((byte) 'A'));
        assertTrue(Base64.isBase64((byte) '='));
        assertTrue(Base64.isBase64((byte) '+'));
        assertTrue(Base64.isBase64((byte) '/'));
        assertTrue(Base64.isBase64((byte) '-'));
        assertTrue(Base64.isBase64((byte) '_'));
        assertFalse(Base64.isBase64((byte) '#'));
        assertFalse(Base64.isBase64((byte) -1));
        assertFalse(Base64.isBase64((byte) 128));
    }

    @Test
    public void testIsArrayByteBase64Valid() {
        byte[] valid = {'A', 'B', 'C'};
        assertTrue(Base64.isArrayByteBase64(valid));
    }

    @Test
    public void testIsArrayByteBase64Whitespace() {
        byte[] withSpace = {'A', ' ', 'B', '\n', 'C'};
        assertTrue(Base64.isArrayByteBase64(withSpace));
    }

    @Test
    public void testIsArrayByteBase64Invalid() {
        byte[] invalid = {'A', 'B', 'C', '#'};
        assertFalse(Base64.isArrayByteBase64(invalid));
    }

    @Test
    public void testIsArrayByteBase64Empty() {
        assertTrue(Base64.isArrayByteBase64(new byte[0]));
    }

    @Test
    public void testEncodeBase64Static() {
        byte[] input = "test".getBytes();
        byte[] expected = new Base64().encode(input);
        assertArrayEquals(expected, Base64.encodeBase64(input));
    }

    @Test
    public void testEncodeBase64StaticNull() {
        assertNull(Base64.encodeBase64(null));
    }

    @Test
    public void testEncodeBase64StaticEmpty() {
        byte[] empty = new byte[0];
        assertArrayEquals(empty, Base64.encodeBase64(empty));
    }

    @Test
    public void testEncodeBase64String() {
        byte[] input = "test".getBytes();
        String encoded = Base64.encodeBase64String(input);
        assertNotNull(encoded);
        assertEquals(new String(Base64.encodeBase64(input)), encoded);
    }

    @Test
    public void testEncodeBase64Chunked() {
        byte[] input = new byte[200];
        new Random().nextBytes(input);
        byte[] chunked = Base64.encodeBase64Chunked(input);
        Base64 chunkEncoder = new Base64(true);
        byte[] expected = chunkEncoder.encode(input);
        assertArrayEquals(expected, chunked);
    }

    @Test
    public void testEncodeBase64URLSafe() {
        byte[] input = "test".getBytes();
        byte[] encoded = Base64.encodeBase64URLSafe(input);
        Base64 urlSafeEncoder = new Base64(true);
        assertEquals(new String(urlSafeEncoder.encode(input)), new String(encoded));
    }

    @Test
    public void testEncodeBase64URLSafeString() {
        byte[] input = "test".getBytes();
        String encoded = Base64.encodeBase64URLSafeString(input);
        assertNotNull(encoded);
        Base64 urlSafe = new Base64(true);
        assertEquals(StringUtils.newStringUtf8(urlSafe.encode(input)), encoded);
    }

    @Test
    public void testEncodeBase64MaxResultSizeExceeded() {
        byte[] input = new byte[1000];
        new Random().nextBytes(input);
        long len = (input.length * 4) / 3;
        try {
            Base64.encodeBase64(input, false, false, (int) len - 1);
            fail("Expected IlegalArgumentException");
        } catch (IllegalArgumentException e) { }
    }

    @Test
    public void testEncodeBase64MaxResultSizeWithChunking() {
        byte[] input = new byte[300];
        new Random().nextBytes(input);
        long len = Base64.getEncodeLength(input, 76, Base64.CHUNK_SEPARATOR);
        try {
            Base64.encodeBase64(input, true, false, (int) len - 1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) { }
    }

    @Test
    public void testDecodeBase64StaticByteArray() {
        byte[] input = "test".getBytes();
        byte[] encoded = Base64.encodeBase64(input);
        byte[] decoded = Base64.decodeBase64(encoded);
        assertArrayEquals(input, decoded);
    }

    @Test
    public void testDecodeBase64StaticString() {
        byte[] input = "test".getBytes();
        String encodedString = Base64.encodeBase64String(input);
        byte[] decoded = Base64.decodeBase64(encodedString);
        assertArrayEquals(input, decoded);
    }

    @Test
    public void testDecodeBase64StaticNullByteArray() {
        assertNull(Base64.decodeBase64((byte[]) null));
    }

    @Test
    public void testDecodeBase64StaticEmptyByteArray() {
        byte[] empty = new byte[0];
        assertArrayEquals(empty, Base64.decodeBase64(empty));
    }

    @Test
    public void testDecodeBase64WithPadding() {
        byte[] input = "test".getBytes();
        Base64 b64 = new Base64();
        byte[] encoded = b64.encode(input);
        byte[] decoded = b64.decode(encoded);
        assertArrayEquals(input, decoded);
    }

    @Test
    public void testDecodeBase64WithoutPadding() {
        byte[] input = "te".getBytes();
        Base64 b64 = new Base64(true); // URL SAFE omits padding
        byte[] encoded = b64.encode(input);
        byte[] decoded = b64.decode(encoded);
        assertArrayEquals(input, decoded);
    }

    @Test
    public void testDecodeIncludesWhiteSpace() {
        Base64 b64 = new Base64();
        byte[] input = "Hello".getBytes();
        byte[] encoded = b64.encode(input);
        // insert whitespace
        byte[] withSpace = new byte[encoded.length + 4];
        System.arraycopy(encoded, 0, withSpace, 0, encoded.length);
        withSpace[2] = (byte) '\n';
        withSpace[withSpace.length -1] = (byte) '\r';
        byte[] decoded = b64.decode(withSpace);
        assertArrayEquals(input, decoded);
    }

    @Test
    public void testDecodeInvalidCharacterIgnored() {
        Base64 b64 = new Base64();
        byte[] input = "Hello".getBytes();
        byte[] encoded = b64.encode(input);
        // Replace a char with #
        encoded[2] = '#';
        byte[] decoded = b64.decode(encoded);
        assertArrayEquals(input, decoded);
    }

    @Test
    public void testEncodeDecodeByteArray() {
        byte[] input = new byte[256];
        for (int i =0; i<256; i++) input[i] = (byte) i;
        Base64 b64 = new Base64();
        byte[] encoded = b64.encode(input);
        byte[] decoded = b64.decode(encoded);
        assertArrayEquals(input, decoded);
    }

    @Test
    public void testEncodeDecodeWithChunking() {
        Base64 b64 = new Base64(20, new byte[]{'\n'});
        byte[] input = "Hello World".getBytes();
        byte[] encoded = b64.encode(input);
        byte[] decoded = b64.decode(encoded);
        assertArrayEquals(input, decoded);
    }

    @Test
    public void testEncodeDecodeURLSafe() {
        Base64 b64 = new Base64(true);
        byte[] input = "Hello World".getBytes();
        byte[] encoded = b64.encode(input);
        byte[] decoded = b64.decode(encoded);
        assertArrayEquals(input, decoded);
    }

    @Test
    public void testEncodeNullReturnsNull() {
        Base64 b64 = new Base64();
        assertNull(b64.encode((byte[]) null));
    }

    @Test
    public void testEncodeEmptyReturnsEmpty() {
        Base64 b64 = new Base64();
        byte[] empty = new byte[0];
        assertArrayEquals(empty, b64.encode(empty));
    }

    @Test
    public void testEncodeObject() throws EncoderException {
        Base64 b64 = new Base64();
        byte[] input = "Test".getBytes();
        Object encodedObj = b64.encode((Object) input);
        assertTrue(encodedObj instanceof byte[]);
        assertArrayEquals(b64.encode(input), (byte[]) encodedObj);
    }

    @Test(expected = EncoderException.class)
    public void testEncodeObjectInvalid() throws EncoderException {
        new Base64().encode("Not a byte array");
    }

    @Test
    public void testEncodeToString() {
        Base64 b64 = new Base64();
        byte[] input = "test".getBytes();
        String encoded = b64.encodeToString(input);
        assertNotNull(encoded);
        assertEquals(StringUtils.newStringUtf8(b64.encode(input)), encoded);
    }

    @Test
    public void testDecodeObjectByteArray() throws DecoderException {
        Base64 b64 = new Base64();
        byte[] input = "test".getBytes();
        byte[] encoded = b64.encode(input);
        Object decodedObj = b64.decode((Object) encoded);
        assertTrue(decodedObj instanceof byte[]);
        assertArrayEquals(input, (byte[]) decodedObj);
    }

    @Test
    public void testDecodeObjectString() throws DecoderException {
        Base64 b64 = new Base64();
        byte[] input = "test".getBytes();
        String encodedString = b64.encodeToString(input);
        Object decodedObj = b64.decode((Object) encodedString);
        assertTrue(decodedObj instanceof byte[]);
        assertArrayEquals(input, (byte[]) decodedObj);
    }

    @Test(expected = DecoderException.class)
    public void testDecodeObjectInvalid() throws DecoderException {
        new Base64().decode(new Integer(1));
    }

    @Test
    public void testDecodeString() {
        Base64 b64 = new Base64();
        byte[] input = "test".getBytes();
        String encoded = b64.encodeToString(input);
        byte[] decoded = b64.decode(encoded);
        assertArrayEquals(input, decoded);
    }

    @Test
    public void testDecodeNull() {
        assertNull(new Base64().decode((byte[]) null));
    }

    @Test
    public void testDecodeEmpty() {
        byte[] empty = new byte[0];
        assertArrayEquals(empty, new Base64().decode(empty));
    }

    @Test
    public void testEncodeInteger() {
        BigInteger bigInt = new BigInteger("12345678901234567890");
        byte[] encoded = Base64.encodeInteger(bigInt);
        assertNotNull(encoded);
        BigInteger decoded = Base64.decodeInteger(encoded);
        assertEquals(bigInt, decoded);
    }

    @Test(expected = NullPointerException.class)
    public void testEncodeIntegerNull() {
        Base64.encodeInteger(null);
    }

    @Test
    public void testDecodeInteger() {
        byte[] input = Base64.encodeBase64("test".getBytes());
        BigInteger bI = Base64.decodeInteger(input);
        assertNotNull(bI);
        byte[] decoded = Base64.decodeBase64(input);
        BigInteger expected = new BigInteger(1, decoded);
        assertEquals(expected, bI);
    }

    @Test
    public void testToIntegerBytes() {
        BigInteger bigInt = new BigInteger("1234567890");
        byte[] bytes = Base64.toIntegerBytes(bigInt);
        assertNotNull(bytes);
        assertEquals(bytes.length, (bigInt.bitLength() + 7) / 8);
        // Reconstruct to confirm
        BigInteger reconstructed = new BigInteger(1, bytes);
        assertEquals(bigInt, reconstructed);
    }

    @Test
    public void testToIntegerBytesPowerOfTwo() {
        BigInteger bigInt = new BigInteger("256");
        byte[] bytes = Base64.toIntegerBytes(bigInt);
        assertEquals(2, bytes.length);
        assertEquals(bigInt, new BigInteger(1, bytes));
    }

    @Test
    public void testDiscardWhitespace() {
        byte[] data = "  A\r\nB\t".getBytes();
        byte[] cleaned = Base64.discardWhitespace(data);
        assertEquals("AB", new String(cleaned));
    }

    @Test
    public void testResizeBuffer() {
        Base64 b64 = new Base64();
        byte[] largeInput = new byte[10000];
        new Random().nextBytes(largeInput);
        byte[] encoded = b64.encode(largeInput);
        assertTrue(encoded.length > 0);
        byte[] decoded = b64.decode(encoded);
        assertArrayEquals(largeInput, decoded);
    }

    @Test
    public void testEncodeDecodeBufferReuse() {
        Base64 b64 = new Base64();
        byte[] input = "Hello World".getBytes();
        byte[] out = new byte[b64.encode(input).length];
        byte[] encoded = b64.encode(input);
        System.arraycopy(encoded, 0, out, 0, encoded.length);
        // encode again should reset
        byte[] input2 = "Another".getBytes();
        byte[] encoded2 = b64.encode(input2);
        assertNotNull(encoded2);
    }

    @Test
    public void testHasDataAndAvail() {
        Base64 b64 = new Base64();
        byte[] input = "test".getBytes();
        b64.encode(input); // data in buffer
        assertTrue(b64.hasData());
        assertTrue(b64.avail() > 0);
        byte[] out = new byte[100];
        int read = b64.readResults(out, 0, out.length);
        assertTrue(read > 0);
        assertTrue(b64.hasData() == (b64.avail() > 0));
    }

    @Test
    public void testReadResultsEmpty() {
        Base64 b64 = new Base64();
        assertEquals(0, b64.avail());
        byte[] out = new byte[10];
        assertEquals(0, b64.readResults(out, 0, 10));
    }

    @Test
    public void testSetInitialBufferThenEncode() {
        Base64 b64 = new Base64();
        byte[] input = "test".getBytes();
        byte[] out = new byte[100];
        b64.setInitialBuffer(out, 0, 100);
        b64.encode(input, 0, input.length);
        b64.encode(input, 0, -1); // flush
        int len = b64.avail();
        byte[] result = new byte[len];
        b64.readResults(result, 0, len);
        assertEquals(new String(b64.encode(input)), new String(result));
    }

    @Test
    public void testGetEncodeLength() {
        byte[] input = new byte[100];
        long len = Base64.getEncodeLength(input, 0, Base64.CHUNK_SEPARATOR);
        assertEquals((100 * 4 + 2) / 3, len);
        long chunked = Base64.getEncodeLength(input, 76, Base64.CHUNK_SEPARATOR);
        assertTrue(chunked > len);
    }

}
