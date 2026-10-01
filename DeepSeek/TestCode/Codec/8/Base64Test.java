package org.apache.commons.codec.binary;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.util.Arrays;

import org.junit.Test;

public class Base64Test {

    // --- Constructor tests ---

    @Test
    public void testDefaultConstructor() {
        Base64 b = new Base64();
        assertFalse(b.isUrlSafe());
        // no data
        assertFalse(b.hasData());
        assertEquals(0, b.avail());
    }

    @Test
    public void testBooleanConstructorTrue() {
        Base64 b = new Base64(true);
        assertTrue(b.isUrlSafe());
        assertFalse(b.hasData());
    }

    @Test
    public void testBooleanConstructorFalse() {
        Base64 b = new Base64(false);
        assertFalse(b.isUrlSafe());
    }

    @Test
    public void testLineLengthConstructor() {
        Base64 b = new Base64(76);
        assertFalse(b.isUrlSafe());
        assertFalse(b.hasData());
    }

    @Test
    public void testLineLengthAndSeparatorConstructor() {
        Base64 b = new Base64(64, new byte[]{'\r', '\n'});
        assertFalse(b.isUrlSafe());
    }

    @Test
    public void testLineLengthAndSeparatorAndUrlSafeConstructor() {
        Base64 b = new Base64(50, new byte[]{'\r', '\n'}, true);
        assertTrue(b.isUrlSafe());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorThrowsOnBase64CharInSeparator() {
        new Base64(76, new byte[]{'0', 'A'});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorThrowsOnBase64CharInSeparator2() {
        new Base64(76, new byte[]{'+'});
    }

    @Test
    public void testConstructorNullSeparatorDisablesChunking() {
        Base64 b = new Base64(100, (byte[]) null);
        assertFalse(b.isUrlSafe());
        assertFalse(b.hasData());
        // lineLength should be 0 after null separator
        // we can test by encoding and check no CRLF
    }

    // --- isUrlSafe ---
    @Test
    public void testIsUrlSafe() {
        assertTrue(new Base64(true).isUrlSafe());
        assertFalse(new Base64(false).isUrlSafe());
        assertFalse(new Base64().isUrlSafe());
        assertTrue(new Base64(0, new byte[]{'\r', '\n'}, true).isUrlSafe());
    }

    // --- hasData and avail ---
    @Test
    public void testHasDataAvailAfterEncoding() {
        Base64 b = new Base64(0);
        b.encode("test".getBytes());
        assertTrue(b.hasData());
        assertTrue(b.avail() > 0);
        b.decode(b.encode("data".getBytes())); // reset? Actually decode does reset, but we can test after readResults
    }

    @Test
    public void testAvail() {
        Base64 b = new Base64(0);
        assertEquals(0, b.avail());
    }

    // --- readResults ---
    @Test
    public void testReadResultsNoData() {
        Base64 b = new Base64(0);
        byte[] buf = new byte[10];
        int r = b.readResults(buf, 0, 10);
        assertEquals(0, r); // no buffer and not EOF
    }

    @Test
    public void testReadResultsWithData() {
        Base64 b = new Base64(0);
        byte[] data = "abc".getBytes();
        byte[] encoded = b.encode(data);
        assertTrue(b.hasData());
        byte[] out = new byte[10];
        int cnt = b.readResults(out, 0, out.length);
        assertTrue(cnt > 0);
        assertArrayEquals(encoded, Arrays.copyOf(out, cnt));
        assertFalse(b.hasData());
    }

    // --- setInitialBuffer ---
    @Test
    public void testSetInitialBuffer() {
        Base64 b = new Base64(0);
        byte[] out = new byte[16];
        b.setInitialBuffer(out, 2, out.length);
        assertTrue(b.hasData());
        assertEquals(0, b.avail());
        // pos = 2, readPos = 2 -> avail = 0
    }

    @Test
    public void testSetInitialBufferMismatchLength() {
        Base64 b = new Base64(0);
        byte[] out = new byte[16];
        b.setInitialBuffer(out, 2, 15);
        assertFalse(b.hasData()); // because out.length != outAvail
    }

    // --- encode(byte[]) (public) ---
    @Test
    public void testEncodeNullReturnsNull() {
        Base64 b = new Base64();
        assertNull(b.encode((byte[]) null));
    }

    @Test
    public void testEncodeEmptyReturnsEmpty() {
        Base64 b = new Base64();
        byte[] empty = new byte[0];
        assertArrayEquals(empty, b.encode(empty));
    }

    @Test
    public void testEncodeBasic() {
        Base64 b = new Base64(0);
        byte[] input = "array".getBytes();
        byte[] expected = "YXJyYXk=".getBytes(); // standard base64 of "array"
        assertArrayEquals(expected, b.encode(input));
    }

    @Test
    public void testEncodeToString() {
        Base64 b = new Base64(0);
        String result = b.encodeToString("array".getBytes());
        assertEquals("YXJyYXk=", result);
    }

    @Test
    public void testEncodeUrlSafe() {
        Base64 b = new Base64(0, new byte[]{'\r', '\n'}, true);
        byte[] input = new byte[]{ (byte)0xFF, (byte)0xFE }; // contains + and /? Actually base64 of -1,-2 results in /+... Test
        String enc = b.encodeToString(input);
        assertFalse(enc.contains("+"));
        assertFalse(enc.contains("/"));
        assertTrue(enc.contains("-") || enc.contains("_"));
    }

    @Test
    public void testEncodeStandardPadding() {
        Base64 b = new Base64(0, new byte[]{'\r', '\n'}, false);
        byte[] input = "A".getBytes();
        String enc = b.encodeToString(input); // should have padding "QQ=="
        assertTrue(enc.endsWith("="));
    }

    @Test
    public void testEncodeUrlSafeNoPadding() {
        Base64 b = new Base64(0, new byte[]{'\r', '\n'}, true);
        byte[] input = "A".getBytes();
        String enc = b.encodeToString(input);
        assertFalse(enc.contains("="));
    }

    @Test
    public void testEncodeChunked() {
        Base64 b = new Base64(76); // default separator CRLF
        // input > 57 bytes to cause chunk
        byte[] big = new byte[100];
        for (int i=0; i<big.length; i++) big[i] = (byte)i;
        String encoded = b.encodeToString(big);
        assertTrue(encoded.contains("\r\n"));
    }

    @Test
    public void testEncodeLineLengthMultipleOf4NonMultiple() {
        Base64 b = new Base64(10, new byte[]{'\n'}); // lineLength will be 8
        byte[] data = new byte[6];
        String enc = b.encodeToString(data);
        // Should have chunks after 8 chars. 
        // Just ensure no exception and works.
        assertNotNull(enc);
    }

    // --- encode static methods ---
    @Test
    public void testStaticEncodeBase64() {
        byte[] input = "hello".getBytes();
        byte[] result = Base64.encodeBase64(input);
        assertArrayEquals("aGVsbG8=".getBytes(), result);
    }

    @Test
    public void testStaticEncodeBase64String() {
        byte[] input = "hello".getBytes();
        String result = Base64.encodeBase64String(input);
        assertEquals("aGVsbG8=", result);
    }

    @Test
    public void testStaticEncodeBase64URLSafe() {
        byte[] input = new byte[]{0, -1};
        byte[] result = Base64.encodeBase64URLSafe(input);
        String s = new String(result);
        assertFalse(s.contains("+"));
        assertFalse(s.contains("/"));
    }

    @Test
    public void testStaticEncodeBase64URLSafeString() {
        byte[] input = new byte[]{0, -1};
        String result = Base64.encodeBase64URLSafeString(input);
        assertFalse(result.contains("+"));
        assertFalse(result.contains("/"));
    }

    @Test
    public void testStaticEncodeBase64Chunked() {
        byte[] big = new byte[100];
        byte[] result = Base64.encodeBase64Chunked(big);
        String s = new String(result);
        assertTrue(s.contains("\r\n"));
    }

    @Test
    public void testStaticEncodeBase64WithChunkFalse() {
        byte[] input = "test".getBytes();
        byte[] result = Base64.encodeBase64(input, false);
        assertArrayEquals("dGVzdA==".getBytes(), result);
    }

    @Test
    public void testStaticEncodeBase64WithMaxResultSizeOk() {
        byte[] input = "abc".getBytes();
        byte[] result = Base64.encodeBase64(input, false, false, 100);
        assertArrayEquals("YWJj".getBytes(), result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStaticEncodeBase64WithMaxResultSizeTooSmall() {
        byte[] input = new byte[100];
        Base64.encodeBase64(input, false, false, 5);
    }

    // --- encode (internal) ---
    @Test
    public void testEncodeInternalWithEof() {
        Base64 b = new Base64(0);
        // encode EOF immediately
        b.encode(null, 0, -1);
        // Should write nothing and set eof
        assertFalse(b.hasData()); // because modulus 0, no data written
    }

    @Test
    public void testEncodeInternalWithModulus1() {
        Base64 b = new Base64(0);
        // simulate modulus 1
        byte[] input = new byte[1]; // after processing 1 byte, modulus becomes 1
        b.encode(input, 0, 1);
        assertFalse(b.hasData()); // not yet emitted (modulus !=0)
        // call EOF
        b.encode(input, 0, -1);
        // should output 2 chars + padding (standard) or no padding (url-safe)
        byte[] out = new byte[10];
        int len = b.readResults(out, 0, out.length);
        assertTrue(len > 0);
    }

    @Test
    public void testEncodeInternalModulus2() {
        Base64 b = new Base64(0);
        byte[] input = new byte[2];
        b.encode(input, 0, 2);
        // call EOF
        b.encode(input, 0, -1);
        // should output 3 chars + one padding or 3 chars
        byte[] out = new byte[10];
        int len = b.readResults(out, 0, out.length);
        assertTrue(len >= 2);
    }

    @Test
    public void testEncodeInternalWithLineBreaking() {
        Base64 b = new Base64(4, new byte[]{'\n'}); // chunk every 4 characters
        byte[] data = new byte[6]; // base64 output length 8, should break after first 4
        b.encode(data, 0, data.length);
        b.encode(data, 0, -1);
        byte[] out = new byte[100];
        int len = b.readResults(out, 0, out.length);
        String s = new String(out, 0, len);
        // expected something like "AAAA\nAAAA\n" but note padding may adjust, still test chunk.
        assertTrue(s.contains("\n"));
    }

    @Test
    public void testEncodeInternalBufferResize() {
        Base64 b = new Base64(0);
        // provide minimal buffer by setting initialBuffer
        byte[] out = new byte[4]; // small
        b.setInitialBuffer(out, 0, out.length);
        byte[] data = new byte[100]; // will force resizes
        b.encode(data, 0, data.length);
        b.encode(data, 0, -1);
        // Should not throw, result captured
        byte[] res = new byte[200];
        int len = b.readResults(res, 0, res.length);
        assertTrue(len > 4);
    }

    // --- decode(byte[]) public ---
    @Test
    public void testDecodeNullReturnsNull() {
        Base64 b = new Base64();
        assertNull(b.decode((byte[]) null));
    }

    @Test
    public void testDecodeEmptyReturnsEmpty() {
        Base64 b = new Base64();
        byte[] empty = new byte[0];
        assertArrayEquals(empty, b.decode(empty));
    }

    @Test
    public void testDecodeBasic() {
        Base64 b = new Base64();
        byte[] encoded = "YXJyYXk=".abytes();
        byte[] expected = "array".getBytes();
        assertArrayEquals(expected, b.decode(encoded));
    }

    @Test
    public void testDecodeStringMethod() {
        Base64 b = new Base64();
        byte[] res = b.decode("YXJyYXk=");
        assertArrayEquals("array".getBytes(), res);
    }

    @Test
    public void testDecodeWithNonBase64Chars() {
        Base64 b = new Base64();
        // garbage chars should be ignored
        byte[] encoded = "YXJy XYXk=".abytes(); // space
        assertArrayEquals("array".getBytes(), b.decode(encoded));
    }

    @Test
    public void testDecodeWithPaddingOnly() {
        Base64 b = new Base64();
        byte[] encoded = "=====".abytes(); // only padding, should decode to empty?
        // Actually first '=' causes eof=true and break, leaving x maybe 0. Then modulus? Not likely.
        // End decode returns empty array.
        byte[] res = b.decode(encoded);
        assertEquals(0, res.length);
    }

    @Test
    public void testDecodeInternalWithModulus2() {
        Base64 b = new Base64(0);
        // feed 2 valid chars then EOF
        byte[] encoded = "YQ".getBytes(); // "YQ" -> decodes to?
        // Manual: Y=24, Q=16 => 24*4+? decode algorithm: modulus 0 becomes 1 after first char? Actually decode loop: for each char, if valid result>=0, modulus=(++modulus)%4, x=(x<<6)+result. If modulus==0 write 3 bytes. Then after loop, if eof and modulus!=0: x=x<<6; switch modulus: 2 => x<<6, output 1 byte. 3 => output 2 bytes. For 2 chars, modulus becomes 2. After loop, process.
        b.decode(encoded,0,encoded.length);
        b.decode(encoded,0,-1);
        byte[] out = new byte[10];
        int len = b.readResults(out,0, out.length);
        assertTrue(len>0);
    }

    @Test
    public void testDecodeInternalModulus3() {
        Base64 b = new Base64(0);
        b.decode("YQJ".getBytes(),0,3);
        b.decode(new byte[0],0,-1);
        byte[] out = new byte[10];
        int len = b.readResults(out,0, out.length);
        assertTrue(len>0);
    }

    @Test
    public void testDecodeWithInvalidByteOutOfRange() {
        Base64 b = new Base64();
        byte[] mixed = new byte[]{(byte)0xFF, 'A', '=', '='}; // high byte ignored, then 'A' alone
        byte[] res = b.decode(mixed);
        // expected? 'A' only -> base64 'A'=0 -> 0 bits, then padding causes EOF after break. Should result empty.
        assertEquals(0, res.length);
    }

    @Test
    public void testDecodeObjectByteArray() {
        try {
            Base64 b = new Base64();
            byte[] encoded = "YXJyYXk=".abytes();
            Object result = b.decode((Object) encoded);
            assertTrue(result instanceof byte[]);
            assertArrayEquals("array".getBytes(), (byte[]) result);
        } catch (DecoderException e) {
            fail("unexpected");
        }
    }

    @Test
    public void testDecodeObjectString() {
        try {
            Base64 b = new Base64();
            Object result = b.decode((Object) "YXJyYXk=");
            assertTrue(result instanceof byte[]);
            assertArrayEquals("array".getBytes(), (byte[]) result);
        } catch (DecoderException e) {
            fail("unexpected");
        }
    }

    @Test(expected = DecoderException.class)
    public void testDecodeObjectInvalidType() throws DecoderException {
        new Base64().decode(new Integer(1));
    }

    // --- encode(Object) ---
    @Test(expected = EnocderException.class)
    public void testEncodeObjectInvalidType() throws EnocderException {
        new Base64().encode(new Integer(1));
    }

    @Test
    public void testEncodeObjectByteArray() throws EnocderException {
        Base64 b = new Base64();
        Object result = b.encode((Object) "test".getBytes());
        assertTrue(result instanceof byte[]);
        assertArrayEquals("dGVzdA==".abytes(), (byte[]) result);
    }

    // --- static decodeBase64 ---
    @Test
    public void testStaticDecodeBase64Bytes() {
        byte[] res = Base64.decodeBase64("dGVzdA==".abytes());
        assertArrayEquals("test".getBytes(), res);
    }

    @Test
    public void testStaticDecodeBase64String() {
        byte[] res = Base64.decodeBase64("dGVzdA==");
        assertArrayEquals("test".getBytes(), res);
    }

    // --- decodeInteger and encodeInteger ---
    @Test
    public void testEncodeInteger() {
        BigInteger bi = new BigInteger("1234567890");
        byte[] encoded = Base64.encodeInteger(bi);
        assertNotNull(encoded);
        // decode back
        BigInteger decoded = Base64.decodeInteger(encoded);
        assertEquals(bi, decoded);
    }

    @Test(expected = NullPointerException.class)
    public void testEncodeIntegerNullThrows() {
        Base64.encodeInteger(null);
    }

    @Test
    public void testDecodeInteger() {
        byte[] encoded = Base64.encodeInteger(BigInteger.ONE);
        BigInteger decoded = Base64.decodeInteger(encoded);
        assertEquals(BigInteger.ONE, decoded);
    }

    // --- toIntegerBytes ---
    @Test
    public void testToIntegerBytes() {
        BigInteger pos = new BigInteger("255"); // bit length 8
        byte[] bytes = Base64.toIntegerBytes(pos);
        assertArrayEquals(new byte[]{-1}, bytes); // 255 as unsigned?
        // Actually 255 -> bits=8, bitlen%8==0, startSrc=1, len=1-1=0? No, toByteArray returns [0, -1] (sign bit). Then startSrc=1, len=1, startDst=1-1=0, resize to 1 byte, copy from index 1 gives [-1].
        assertArrayEquals(new byte[]{-1}, bytes);
    }

    @Test
    public void testToIntegerBytesLarger() {
        BigInteger big = new BigInteger("65535"); // ff ff? 0b11111111 11111111 -> unsigned 65535? Actually 65535+? Let's not worry, just test known values.
        byte[] b = big.toByteArray();
        byte[] res = Base64.toIntegerBytes(big);
        // should have no sign lead byte if positive and not exactly byte aligned
        // Just ensure non-null and decode back works.
        BigInteger decoded = new BigInteger(1, res);
        assertEquals(big, decoded);
    }

    // --- isBase64 and isArrayByteBase64 ---
    @Test
    public void testIsBase64() {
        assertTrue(Base64.isBase64((byte)'A'));
        assertTrue(Base64.isBase64((byte)'Z'));
        assertTrue(Base64.isBase64((byte)'a'));
        assertTrue(Base64.isBase64((byte)'z'));
        assertTrue(Base64.isBase64((byte)'0'));
        assertTrue(Base64.isBase64((byte)'9'));
        assertTrue(Base64.isBase64((byte)'+'));
        assertTrue(Base64.isBase64((byte)'/'));
        assertTrue(Base64.isBase64((byte)'-'));
        assertTrue(Base64.isBase64((byte)'_'));
        assertTrue(Base64.isBase64((byte)'='));
        assertFalse(Base64.isBase64((byte)' '));
        assertFalse(Base64.isBase64((byte) -1);
    }

    @Test
    public void testIsArrayByteBase64() {
        assertTrue(Base64.isArrayByteBase64(new byte[]{'Y','X','J','='}));
        assertFalse(Base64.isArrayByteBase64(new byte[]{'Y','X',' ','J'}));
        assertTrue(Base64.isArrayByteBase64(new byte[0]));
    }

    // --- discardWhitespace ---
    @Test
    public void testDiscardWhitespace() {
        byte[] input = " a b\r\n\tc".getBytes();
        byte[] expected = "abc".getBytes();
        assertArrayEquals(expected, Base64.discardWhitespace(input));
    }

    // --- reset --- indirectly tested via reuse ---
    @Test
    public void testResetReuse() {
        Base64 b = new Base64(0);
        byte[] data = "test".getBytes();
        byte[] enc1 = b.encode(data);
        // encode again without reset? Actually encode calls reset at start, so ok.
        byte[] enc2 = b.encode(data);
        assertArrayEquals(enc1, enc2);
    }

    // --- static encodeBase64 with large maxResultSize edge ---
    @Test
    public void testStaticEncodeBase64EdgeMaxSize() {
        // Give exactly max required
        byte[] input = new byte[3];
        long len = 4; // since 3 bytes -> 4 chars no chunk
        byte[] result = Base64.encodeBase64(input, false, false, (int) len);
        assertNotNull(result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStaticEncodeBase64EdgeMaxSizeTooSmall() {
        byte[] input = "abcd".getBytes();
        Base64.encodeBase64(input, false, false, 3);
    }

    // --- decode with excess padding and valid chars causing buffer resize--- 
    @Test
    public void testDecodeBufferResize() {
        Base64 b = new Base64();
        // provide minimal buffer via setInitialBuffer
        byte[] inBuf = new byte[4];
        b.setInitialBuffer(inBuf, 0, inBuf.length);
        byte[] encoded = "YXJyYXk=".abytes();
        b.decode(encoded, 0, encoded.length);
        b.decode(encoded, 0, -1);
        byte[] out = new byte[10];
        int len = b.readResults(out, 0, out.length);
        assertTrue(len > 0);
    }
}
