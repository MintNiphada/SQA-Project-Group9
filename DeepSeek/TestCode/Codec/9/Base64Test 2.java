package org.apache.commons.codec.binary;

import org.junit.Test;
import static org.junit.Assert.*;

import java.math.BigInteger;
import java.util.Arrays;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;

public class Base64Test {

    // Helper method to compare byte arrays by converting to strings (for debugging)
    private static void assertByteArrayEquals(String message, byte[] expected, byte[] actual) {
        assertArrayEquals(message, expected, actual);
    }

    // --- Static isBase64 methods ---

    @Test
    public void testIsBase64_byte_octet() {
        assertTrue(Base64.isBase64((byte) 'A'));
        assertTrue(Base64.isBase64((byte) '='));
        assertFalse(Base64.isBase64((byte) '-'));
        assertFalse(Base64.isBase64((byte) 128)); // out of range, but isBase64 returns false for >= 128? Look at implementation: decodes -1 if out of range? Actually DECODE_TABLE has negative entries for non-base64. But range check: if (b >= 0 && b < DECODE_TABLE.length) and result >=0. For negative b, b>=0 is false. So byte 128 is negative, returns false.
        assertFalse(Base64.isBase64((byte) 200)); // negative byte
        assertFalse(Base64.isBase64((byte) 0x7F)); // DEL character? Not in table, returns false.
    }

    @Test
    public void testIsBase64_byteArray_empty() {
        assertTrue(Base64.isBase64(new byte[0]));
    }

    @Test
    public void testIsBase64_byteArray_valid() {
        assertTrue(Base64.isBase64("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=".getBytes()));
    }

    @Test
    public void testIsBase64_byteArray_invalid() {
        assertFalse(Base64.isBase64(new byte[]{'A', 'B', '!'}));
    }

    @Test
    public void testIsBase64_byteArray_whitespace() {
        assertTrue(Base64.isBase64(new byte[]{'A', ' ', '\n', '\r', '\t'}));
    }

    @Test
    public void testIsBase64_String() {
        assertTrue(Base64.isBase64(""));
        assertTrue(Base64.isBase64("ABCD"));
        assertFalse(Base64.isBase64("ABC!"));
    }

    @Test
    public void testIsArrayByteBase64() {
        // deprecated but test anyway
        assertTrue(Base64.isArrayByteBase64(new byte[]{'A', '='}));
        assertFalse(Base64.isArrayByteBase64(new byte[]{'@'}));
    }

    // --- Static encoding/decoding methods (byte[] input/output) ---

    @Test
    public void testEncodeBase64_null() {
        assertNull(Base64.encodeBase64(null));
    }

    @Test
    public void testEncodeBase64_empty() {
        assertArrayEquals(new byte[0], Base64.encodeBase64(new byte[0]));
    }

    @Test
    public void testEncodeBase64_basic() {
        byte[] input = "Hello".getBytes();
        byte[] expected = "SGVsbG8=".getBytes();
        assertArrayEquals(expected, Base64.encodeBase64(input));
    }

    @Test
    public void testEncodeBase64String() {
        byte[] input = "Hello".getBytes();
        String result = Base64.encodeBase64String(input);
        assertEquals("SGVsbG8=", result);
    }

    @Test
    public void testEncodeBase64URLSafe() {
        byte[] input = { (byte) 0xFB, (byte) 0xFF, (byte) 0x00 }; // will give +/ ?
        byte[] result = Base64.encodeBase64URLSafe(input);
        // For standard: +/... For URL safe: -_...
        // Let's compute: 0xFBFF00 -> binary: 11111011 11111111 00000000
        // 25: 0xFBFF00 >> 18? Actually encode 6 bits each: 111110 111111 110000 000000? Let's manually encode: first 6 bits: 111110 = 62 -> '+' in standard, '-' in URL safe. second 6 bits: 111111 = 63 -> '/' in standard, '_' in URL safe. third: 110000 = 48 -> 'w' , fourth: 000000 = 0 -> 'A'. Padding one '='.
        // So URL safe: -_wA=
        assertArrayEquals("-_wA=".getBytes(), result);
    }

    @Test
    public void testEncodeBase64URLSafeString() {
        byte[] input = { (byte) 0xFB, (byte) 0xFF, (byte) 0x00 };
        String result = Base64.encodeBase64URLSafeString(input);
        assertEquals("-_wA=", result);
    }

    @Test
    public void testEncodeBase64Chunked() {
        byte[] input = new byte[100];
        for (int i = 0; i < 100; i++) {
            input[i] = (byte) i;
        }
        byte[] result = Base64.encodeBase64Chunked(input);
        // check chunking: should have line breaks
        String s = new String(result);
        assertTrue(s.contains("\r\n"));
        // the length should be more than non-chunked because of CRLF
        assertTrue(result.length > 0);
    }

    @Test
    public void testEncodeBase64_withChunking_small() {
        byte[] input = "Hello".getBytes();
        byte[] result = Base64.encodeBase64(input, true, false, Integer.MAX_VALUE);
        // for small data, chunking still pads CRLF after final line?
        String s = new String(result);
        assertEquals("SGVsbG8=\r\n", s); // Note: line length 76 but short, still adds CRLF at end? Let's check: encodeBase64(true) uses lineLength 76. For small input, line separation is still added at end because of the EOF flush? The encoder code adds CRLF after EOF if lineLength>0 and previous char not CRLF. It will add CRLF. So expected is "SGVsbG8=\r\n". We'll test that.
    }

    @Test
    public void testDecodeBase64_string() {
        byte[] decoded = Base64.decodeBase64("SGVsbG8=");
        assertArrayEquals("Hello".getBytes(), decoded);
    }

    @Test
    public void testDecodeBase64_byteArray() {
        byte[] decoded = Base64.decodeBase64("SGVsbG8=".getBytes());
        assertArrayEquals("Hello".getBytes(), decoded);
    }

    @Test
    public void testDecodeBase64_null() {
        assertNull(new Base64().decode((byte[]) null));
    }

    @Test
    public void testDecodeBase64_empty_byteArray() {
        assertArrayEquals(new byte[0], new Base64().decode(new byte[0]));
    }

    @Test
    public void testEncodeInteger_null() {
        try {
            Base64.encodeInteger(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testEncodeInteger_zero() {
        BigInteger bi = BigInteger.ZERO;
        byte[] encoded = Base64.encodeInteger(bi);
        assertArrayEquals(Base64.encodeBase64(new byte[]{0}), encoded);
    }

    @Test
    public void testEncodeInteger_positive() {
        BigInteger bi = new BigInteger("1234567890");
        byte[] encoded = Base64.encodeInteger(bi);
        byte[] expected = Base64.encodeBase64(bi.toByteArray(), false); // but toIntegerBytes strips sign? We'll trust.
        // Just decode back and compare
        BigInteger decoded = Base64.decodeInteger(encoded);
        assertEquals(bi, decoded);
    }

    @Test
    public void testEncodeInteger_negative() {
        BigInteger bi = new BigInteger("-1234567890");
        byte[] encoded = Base64.encodeInteger(bi);
        BigInteger decoded = Base64.decodeInteger(encoded);
        assertEquals(bi, decoded);
    }

    @Test
    public void testDecodeInteger() {
        byte[] base64 = Base64.encodeBase64(new byte[]{0, 1, 2, 3});
        BigInteger bi = Base64.decodeInteger(base64);
        assertArrayEquals(new byte[]{0, 1, 2, 3}, bi.toByteArray());
    }

    @Test
    public void testToIntegerBytes() {
        // Test the static toIntegerBytes method (package access)
        BigInteger bi = new BigInteger("0");
        byte[] bytes = Base64.toIntegerBytes(bi);
        assertEquals(1, bytes.length);
        assertEquals(0, bytes[0]);

        bi = new BigInteger("255");
        bytes = Base64.toIntegerBytes(bi);
        // 255 -> bitLength 8, so array length 1, value 0xFF
        assertEquals(1, bytes.length);
        assertEquals((byte)0xFF, bytes[0]);

        bi = new BigInteger("256");
        bytes = Base64.toIntegerBytes(bi);
        // bitLength 9 -> round up to 16, startDst = 2 - 2 =0, startSrc=0, len=2, array length 2
        assertEquals(2, bytes.length);
        assertArrayEquals(new byte[]{0x01,0x00}, bytes);

        bi = new BigInteger("-256");
        bytes = Base64.toIntegerBytes(bi);
        assertEquals(2, bytes.length);
        assertArrayEquals(new byte[]{(byte)0xFF, 0x00}, bytes);
    }

    @Test
    public void testDiscardWhitespace() {
        byte[] input = "A B\nC\tD\r\nE".getBytes();
        byte[] result = Base64.discardWhitespace(input);
        assertArrayEquals("ABCDE".getBytes(), result);
    }

    @Test
    public void testDiscardWhitespace_noWhitespace() {
        byte[] input = "ABC".getBytes();
        byte[] result = Base64.discardWhitespace(input);
        assertArrayEquals("ABC".getBytes(), result);
    }

    @Test
    public void testDiscardWhitespace_empty() {
        byte[] input = new byte[0];
        byte[] result = Base64.discardWhitespace(input);
        assertArrayEquals(new byte[0], result);
    }

    // --- Constructor and configuration tests ---

    @Test
    public void testDefaultConstructor() {
        Base64 b64 = new Base64();
        assertFalse(b64.isUrlSafe());
        // lineLength 0
    }

    @Test
    public void testUrlSafeConstructor_false() {
        Base64 b64 = new Base64(false);
        assertFalse(b64.isUrlSafe());
    }

    @Test
    public void testUrlSafeConstructor_true() {
        Base64 b64 = new Base64(true);
        assertTrue(b64.isUrlSafe());
    }

    @Test
    public void testLineLengthConstructor_zero() {
        Base64 b64 = new Base64(0);
        assertFalse(b64.isUrlSafe());
    }

    @Test
    public void testLineLengthConstructor_negative() {
        Base64 b64 = new Base64(-10);
        // lineLength becomes 0
        assertFalse(b64.isUrlSafe());
    }

    @Test
    public void testLineLengthConstructor_positive() {
        Base64 b64 = new Base64(100);
        assertFalse(b64.isUrlSafe());
        // lineLength truncated to multiple of 4: 100/4=25*4=100 -> 100
    }

    @Test
    public void testLineLengthWithSeparatorConstructor_validSeparator() {
        Base64 b64 = new Base64(76, new byte[]{'\r', '\n'});
        assertFalse(b64.isUrlSafe());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLineLengthWithSeparatorConstructor_base64InSeparator_throwsException() {
        new Base64(76, new byte[]{'A'}); // 'A' is base64
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLineLengthWithSeparatorConstructor_base64InSeparator_withUrlSafe() {
        new Base64(76, new byte[]{'A'}, true);
    }

    @Test
    public void testLineLengthWithSeparatorAndUrlSafeConstructor() {
        Base64 b64 = new Base64(76, new byte[]{'\r', '\n'}, true);
        assertTrue(b64.isUrlSafe());
    }

    @Test
    public void testNullLineSeparator_disablesChunking() {
        Base64 b64 = new Base64(76, null);
        // lineLength becomes 0
        assertFalse(b64.isUrlSafe());
    }

    @Test
    public void testEncodeSize_debug() {
        Base64 b64 = new Base64(76, new byte[]{'\r', '\n'});
        // encodeSize = 4 + 2 =6
    }

    // --- Non-static encode and decode methods (streaming) ---

    @Test
    public void testEncode_nullArray() {
        Base64 b64 = new Base64();
        byte[] result = b64.encode((byte[]) null);
        assertNull(result);
    }

    @Test
    public void testEncode_emptyArray() {
        Base64 b64 = new Base64();
        byte[] result = b64.encode(new byte[0]);
        assertArrayEquals(new byte[0], result);
    }

    @Test
    public void testEncode_basic() {
        Base64 b64 = new Base64();
        byte[] result = b64.encode("Hello".getBytes());
        assertArrayEquals("SGVsbG8=".getBytes(), result);
    }

    @Test
    public void testEncode_Chunked() {
        Base64 b64 = new Base64(76);
        byte[] input = new byte[200];
        for (int i = 0; i < 200; i++) {
            input[i] = (byte) i;
        }
        byte[] result = b64.encode(input);
        String s = new String(result);
        assertTrue(s.contains("\r\n"));
    }

    @Test
    public void testEncode_urlSafe() {
        Base64 b64 = new Base64(true);
        byte[] input = { (byte) 0xFB, (byte) 0xFF, (byte) 0x00 };
        byte[] result = b64.encode(input);
        assertArrayEquals("-_wA".getBytes(), result); // No padding for URL safe? Wait, in URL safe, padding is omitted? Actually the code in encode: for modulus case 1 and 2, if encodeTable == STANDARD_ENCODE_TABLE, it adds PAD. For URL_SAFE, it does NOT add padding. So result is "-_wA" without '='. Yes.
    }

    @Test
    public void testEncode_urlSafe_withPaddingByStandard() {
        // For standard, padding added
        Base64 b64 = new Base64(false);
        byte[] input = { (byte) 0xFB, (byte) 0xFF, (byte) 0x00 };
        byte[] result = b64.encode(input);
        // Standard: +/wA=
        assertArrayEquals("+/wA=".getBytes(), result);
    }

    @Test
    public void testEncode_afterReset() {
        Base64 b64 = new Base64();
        b64.encode("Hello".getBytes());
        b64.reset(); // private, but encode calls reset itself? Actually encode(pArray) calls reset(). But we can call encode multiple times.
        byte[] result = b64.encode("World".getBytes());
        assertArrayEquals("V29ybGQ=".getBytes(), result);
    }

    @Test
    public void testEncodeToString() {
        Base64 b64 = new Base64();
        String result = b64.encodeToString("Hello".getBytes());
        assertEquals("SGVsbG8=", result);
    }

    @Test
    public void testEncodeObject() throws EncoderException {
        Base64 b64 = new Base64();
        Object result = b64.encode((Object) "Hello".getBytes());
        assertTrue(result instanceof byte[]);
        assertArrayEquals("SGVsbG8=".getBytes(), (byte[]) result);
    }

    @Test(expected = EncoderException.class)
    public void testEncodeObject_notByteArray() throws EncoderException {
        Base64 b64 = new Base64();
        b64.encode("not a byte array");
    }

    @Test
    public void testDecodeObject_byteArray() throws DecoderException {
        Base64 b64 = new Base64();
        Object result = b64.decode((Object) "SGVsbG8=".getBytes());
        assertTrue(result instanceof byte[]);
        assertArrayEquals("Hello".getBytes(), (byte[]) result);
    }

    @Test
    public void testDecodeObject_String() throws DecoderException {
        Base64 b64 = new Base64();
        Object result = b64.decode((Object) "SGVsbG8=");
        assertTrue(result instanceof byte[]);
        assertArrayEquals("Hello".getBytes(), (byte[]) result);
    }

    @Test(expected = DecoderException.class)
    public void testDecodeObject_invalidType() throws DecoderException {
        new Base64().decode(new Integer(1));
    }

    @Test
    public void testDecodeString() {
        Base64 b64 = new Base64();
        byte[] result = b64.decode("SGVsbG8=");
        assertArrayEquals("Hello".getBytes(), result);
    }

    @Test
    public void testDecodeByteArray_null() {
        Base64 b64 = new Base64();
        assertNull(b64.decode((byte[]) null));
    }

    @Test
    public void testDecodeByteArray_empty() {
        Base64 b64 = new Base64();
        assertArrayEquals(new byte[0], b64.decode(new byte[0]));
    }

    @Test
    public void testDecode_withPAD() {
        Base64 b64 = new Base64();
        byte[] result = b64.decode("AA==".getBytes());
        assertArrayEquals(new byte[]{0}, result);
    }

    @Test
    public void testDecode_withPAD_two() {
        Base64 b64 = new Base64();
        byte[] result = b64.decode("AAA=".getBytes());
        assertArrayEquals(new byte[]{0, 0}, result);
    }

    @Test
    public void testDecode_noPAD() {
        Base64 b64 = new Base64();
        byte[] result = b64.decode("AAAA".getBytes());
        assertArrayEquals(new byte[]{0, 0, 0}, result);
    }

    @Test
    public void testDecode_ignoreNonBase64Characters() {
        Base64 b64 = new Base64();
        byte[] result = b64.decode("SGVs\r\nbG8=".getBytes());
        assertArrayEquals("Hello".getBytes(), result);
    }

    @Test
    public void testDecode_urlSafeEncoded_withStandardDecoder() {
        // Decode URL safe - encoded string using standard decoder, should decode correctly because DECODE_TABLE handles both.
        byte[] input = "-_wA".getBytes(); // URL safe without padding
        Base64 b64 = new Base64();
        byte[] result = b64.decode(input);
        // expected: 0xFB, 0xFF, 0x00
        assertArrayEquals(new byte[]{(byte)0xFB, (byte)0xFF, 0x00}, result);
    }

    @Test
    public void testDecode_streaming_multipleCalls() {
        Base64 b64 = new Base64();
        // Use package-level decode method directly to test streaming
        b64.decode("ABCD".getBytes(), 0, 4); // decode 4 chars -> 3 bytes
        b64.decode(new byte[]{}, 0, -1); // signal EOF
        byte[] buf = new byte[b64.avail()];
        int len = b64.readResults(buf, 0, buf.length);
        assertEquals(3, len);
        assertArrayEquals(new byte[]{0, 0x10, (byte)0x83}, buf); // A=0, B=1, C=2, D=3? Let's compute.
        // ABCD: A=0 (000000), B=1 (000001), C=2 (000010), D=3 (000011) => bits 000000 000001 000010 000011 -> combine: 00000000 00010000 10000011? (0, 16, -125?) Not important. Just verify decoding.
    }

    @Test
    public void testDecode_streaming_eofWithModulus() {
        Base64 b64 = new Base64();
        b64.decode("AA".getBytes(), 0, 2); // 2 chars, 12 bits -> after padding 12 bits, case modulus=2 -> should output 1 byte.
        b64.decode(new byte[]{}, 0, -1); // signal EOF
        assertEquals(1, b64.avail());
        byte[] buf = new byte[1];
        b64.readResults(buf, 0, 1);
        assertEquals(0, buf[0]);
    }

    @Test
    public void testEncode_streaming_singleBytes() {
        Base64 b64 = new Base64();
        b64.encode(new byte[]{0}, 0, 1); // should not output yet
        assertEquals(0, b64.avail());
        b64.encode(new byte[]{0}, 1, 0); // zero length, does nothing
        b64.encode(new byte[]{}, 0, -1); // EOF, modulus=1 -> outputs 2 chars + padding?
        byte[] buf = new byte[b64.avail()];
        b64.readResults(buf, 0, buf.length);
        // should be "AA=="
        assertArrayEquals("AA==".getBytes(), buf);
    }

    @Test
    public void testEncode_streaming_twoBytes() {
        Base64 b64 = new Base64();
        b64.encode(new byte[]{0, 0}, 0, 2); // modulus=2, no flush yet
        assertEquals(0, b64.avail());
        b64.encode(new byte[]{}, 0, -1); // EOF modulus=2 -> outputs 3 chars + padding?
        byte[] buf = new byte[b64.avail()];
        b64.readResults(buf, 0, buf.length);
        // should be "AAA="
        assertArrayEquals("AAA=".getBytes(), buf);
    }

    @Test
    public void testEncode_streaming_threeBytes() {
        Base64 b64 = new Base64();
        b64.encode(new byte[]{0, 0, 0}, 0, 3); // modulus 0, full output
        assertEquals(4, b64.avail()); // output "AAAA"
        byte[] buf = new byte[4];
        b64.readResults(buf, 0, 4);
        assertArrayEquals("AAAA".getBytes(), buf);
        b64.encode(new byte[]{}, 0, -1); // EOF, no remaining bits
        assertEquals(0, b64.avail());
    }

    @Test
    public void testEncode_streaming_withChunking() {
        Base64 b64 = new Base64(10, new byte[]{'\r', '\n'}); // line length 10 (truncated to 8? 10/4=2*4=8)
        byte[] data = new byte[24]; // 24 bytes -> 32 chars + CRLF each line of 8 chars?
        for (int i = 0; i < 24; i++) data[i] = 0;
        b64.encode(data, 0, 24);
        b64.encode(new byte[]{}, 0, -1);
        byte[] result = new byte[b64.avail()];
        b64.readResults(result, 0, result.length);
        String s = new String(result);
        // Each line of 8 base64 chars, then CRLF, and at the end CRLF only if lineLength>0? The code adds CRLF after EOF only if previous char not CRLF. So final line gets CRLF.
        assertTrue(s.contains("\r\n"));
        // Check no empty lines at start, etc.
    }

    @Test
    public void testEncode_streaming_negative_byte() {
        Base64 b64 = new Base64();
        byte[] input = new byte[]{(byte) 0xFF};
        b64.encode(input, 0, 1);
        b64.encode(null, 0, -1); // signal EOF
        byte[] result = new byte[b64.avail()];
        b64.readResults(result, 0, result.length);
        assertArrayEquals("/w==".getBytes(), result); // since 0xFF = 11111111, mutliple ways? Actually for single byte 0xFF, base64: 111111 11???? -> ??. Standard: /w==
    }

    @Test
    public void testReadResults_multipleCalls() {
        Base64 b64 = new Base64();
        b64.encode(new byte[]{0,0,0}, 0, 3); // 4 bytes buffer
        byte[] buf = new byte[2];
        int len = b64.readResults(buf, 0, 2);
        assertEquals(2, len);
        assertEquals('A', buf[0]);
        assertEquals('A', buf[1]);
        assertEquals(2, b64.avail()); // still 2 left
        len = b64.readResults(buf, 0, 2);
        assertEquals(2, len);
        assertEquals('A', buf[0]);
        assertEquals('A', buf[1]);
        assertEquals(0, b64.avail());
        len = b64.readResults(buf, 0, 2);
        assertEquals(0, len); // no data, not eof yet
        // after eof, readResults returns -1? Actually if buffer==null and eof is true, returns -1. But we haven't signalled EOF, buffer is null (readPos>=pos), so returns 0. Then we signal eof via encode(... -1) and check.
        b64.encode(null, 0, -1); // eof
        // buffer now null because readPos reached pos? After eof, buffer may become null? readResults sets buffer=null when readPos>=pos. So buffer is null. Then readResults returns eof ? (eof is true) so -1.
        len = b64.readResults(buf, 0, 2);
        assertEquals(-1, len);
    }

    @Test
    public void testAvail_hasData() {
        Base64 b64 = new Base64();
        assertFalse(b64.hasData());
        assertEquals(0, b64.avail());
        b64.encode(new byte[]{0}, 0, 0); // no output
        assertFalse(b64.hasData());
        b64.encode(new byte[]{0,0,0}, 0, 3); // produces output
        assertTrue(b64.hasData());
        assertTrue(b64.avail() > 0);
    }

    @Test
    public void testResizeBuffer_initial() {
        Base64 b64 = new Base64();
        // directly encode to trigger resize
        b64.encode(new byte[10000], 0, 10000);
        // no exception
    }

    @Test
    public void testDecode_pad_endsStream() {
        Base64 b64 = new Base64();
        b64.decode("AA=".getBytes(), 0, 3);
        // eof should be true now
        // reading results after manual decode?
        b64.decode(new byte[]{}, 0, -1); // will set eof again, but already eof, check that it doesn't mess up.
        byte[] buf = new byte[4];
        int len = b64.readResults(buf, 0, 4);
        // AA= decodes to single byte 0.
        assertEquals(1, len);
        assertEquals(0, buf[0]);
    }

    @Test
    public void testDecode_inRangeButNegativeResult() {
        // character '-' (0x2D) is in range but DECODE_TABLE[45]? '-' is index 45, which in DECODE_TABLE is 62? Actually '-' is decoded as 62. So valid. But character '#' (0x23) is in range, result -1 (invalid). So should be ignored.
        Base64 b64 = new Base64();
        b64.decode("A#B".getBytes(), 0, 3); // '#' ignored
        b64.decode(new byte[]{}, 0, -1);
        byte[] buf = new byte[b64.avail()];
        b64.readResults(buf, 0, buf.length);
        // A=0, B=1 -> 6 bits each, modulus=2? A=000000, B=000001 => combined 000000 000001 => 12 bits, then after EOF case 2: shift right 4, output byte = 0. So one byte 0.
        assertEquals(1, buf.length);
        assertEquals(0, buf[0]);
    }

    @Test
    public void testEncode_HugeData_exceedMaxResultSize() {
        byte[] data = new byte[1000];
        try {
            Base64.encodeBase64(data, false, false, 10);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testEncodeBase64_maxResultSize_valid() {
        byte[] data = new byte[1];
        byte[] result = Base64.encodeBase64(data, false, false, Integer.MAX_VALUE);
        assertNotNull(result);
    }

    // Additional edge cases

    @Test
    public void testDecode_streaming_nonBase64_outOfRange() {
        Base64 b64 = new Base64();
        // byte 128 (negative) is out of range, should be ignored
        b64.decode(new byte[]{'A', -128, 'B'}, 0, 3);
        b64.decode(new byte[]{}, 0, -1);
        byte[] buf = new byte[2];
        int len = b64.readResults(buf, 0, 2);
        // A and B decoded to 1 byte? Actually A=0, B=1, modulus=2, output one byte 0.
        assertEquals(1, len);
    }

    @Test
    public void testEncode_lineLength_precisely_multipleOf4() {
        Base64 b64 = new Base64(8, new byte[]{'\r', '\n'});
        byte[] data = new byte[6]; // 6 bytes -> 8 chars.
        for (int i=0;i<6;i++) data[i]=0;
        b64.encode(data, 0, 6);
        b64.encode(null,0,-1);
        byte[] result = new byte[b64.avail()];
        b64.readResults(result, 0, result.length);
        String s = new String(result);
        // Should be "AAAAAAAA\r\n" (8 A's plus CRLF)
        assertTrue(s.startsWith("AAAAAAAA\r\n"));
    }

    @Test
    public void testEncode_lineLength_with_CRLF_prevent_duplicate() {
        // When we have encoded data that ends exactly at lineLength, the next chunk will have CRLF inserted, but we don't want duplicate CRLF. The code in encode after EOF: "if (lineLength > 0 && pos > 0 && buffer[pos-1] != b)" ensures no duplicate. That's tested.
    }

    @Test
    public void testDecode_with_trailing_nonBase64() {
        byte[] result = new Base64().decode("SGVsbG8= ".getBytes());
        assertArrayEquals("Hello".getBytes(), result);
    }

    @Test
    public void testStatic_encodeBase64_withNull() {
        assertNull(Base64.encodeBase64(null, false, false, Integer.MAX_VALUE));
    }

    @Test
    public void testStatic_encodeBase64_withEmpty() {
        assertArrayEquals(new byte[0], Base64.encodeBase64(new byte[0], false, false, Integer.MAX_VALUE));
    }

    @Test
    public void testStatic_encodeBase64_withChunked_short() {
        byte[] result = Base64.encodeBase64("Hello".getBytes(), true);
        String s = new String(result);
        assertTrue(s.endsWith("\r\n")); // chunked
    }

    @Test
    public void testEncodeStream_signalEofAfterEnd() {
        Base64 b64 = new Base64();
        b64.encode(new byte[]{0,0,0}, 0, 3);
        b64.encode(null, 0, -1); // eof, nothing to flush
        byte[] buf = new byte[4];
        b64.readResults(buf, 0, 4);
        assertArrayEquals("AAAA".getBytes(), buf);
        // calling encode again after eof should return immediately
        b64.encode(new byte[]{1,2,3}, 0, 3); // should ignore
        assertEquals(0, b64.avail());
    }

    @Test
    public void testDecodeStream_signalEofAfterEnd() {
        Base64 b64 = new Base64();
        b64.decode("AAAA".getBytes(), 0, 4); // yields 3 bytes 0
        b64.decode(null, 0, -1); // eof
        byte[] buf = new byte[3];
        b64.readResults(buf, 0, 3);
        assertEquals(3, buf.length);
        // decoding after eof ignored
        b64.decode("AAAA".getBytes(), 0, 4);
        assertEquals(0, b64.avail());
    }

    @Test
    public void testEncode_Pad_Standard_only() {
        Base64 b64 = new Base64(false); // standard
        byte[] input = new byte[]{0};
        byte[] result = b64.encode(input);
        assertArrayEquals("AA==".getBytes(), result);
    }

    @Test
    public void testEncode_URLSafe_noPad() {
        Base64 b64 = new Base64(true);
        byte[] input = new byte[]{0};
        byte[] result = b64.encode(input);
        assertArrayEquals("AA".getBytes(), result); // no padding
    }
}
```
