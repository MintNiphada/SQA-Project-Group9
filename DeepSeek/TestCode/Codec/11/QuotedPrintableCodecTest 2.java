package org.apache.commons.codec.net;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.util.BitSet;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.junit.Test;

public class QuotedPrintableCodecTest {

    private static final String UTF_8 = CharEncoding.UTF_8;
    private static final String ISO_8859_1 = "ISO-8859-1";

    @Test
    public void testDefaultConstructor() {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        assertEquals(UTF_8, codec.getDefaultCharset());
    }

    @Test
    public void testConstructorWithCharset() {
        QuotedPrintableCodec codec = new QuotedPrintableCodec(ISO_8859_1);
        assertEquals(ISO_8859_1, codec.getDefaultCharset());
    }

    @Test
    public void testEncodeByteArrayNull() {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        assertNull(codec.encode((byte[]) null));
    }

    @Test
    public void testEncodeByteArrayEmpty() {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        byte[] input = new byte[0];
        byte[] output = codec.encode(input);
        assertNotNull(output);
        assertEquals(0, output.length);
    }

    @Test
    public void testEncodeByteArrayAllPrintable() {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        // All bytes from 33 to 126 except 61 ('=') are printable, but '=' is printable? Actually '=' is 61, which is in range 33-60? 61 is not in 33-60, it's 61, so it's not printable. So '=' will be encoded.
        // Let's use only bytes that are in PRINTABLE_CHARS: 33-60, 62-126, TAB(9), SPACE(32)
        byte[] input = "abc ABC 123 !@#".getBytes(); // all printable
        byte[] output = codec.encode(input);
        assertArrayEquals(input, output);
    }

    @Test
    public void testEncodeByteArrayNonPrintable() {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        byte[] input = new byte[] { 0x00, 0x0A, 0x0D, (byte) 0xFF };
        byte[] output = codec.encode(input);
        // Expected: =00, =0A, =0D, =FF
        byte[] expected = new byte[] { '=', '0', '0', '=', '0', 'A', '=', '0', 'D', '=', 'F', 'F' };
        assertArrayEquals(expected, output);
    }

    @Test
    public void testEncodeByteArrayMixed() {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        byte[] input = new byte[] { 'A', 0x00, 'B', 0x0D, 'C' };
        byte[] output = codec.encode(input);
        byte[] expected = new byte[] { 'A', '=', '0', '0', 'B', '=', '0', 'D', 'C' };
        assertArrayEquals(expected, output);
    }

    @Test
    public void testEncodeByteArrayWithEqualsSign() {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        byte[] input = new byte[] { '=' };
        byte[] output = codec.encode(input);
        byte[] expected = new byte[] { '=', '3', 'D' };
        assertArrayEquals(expected, output);
    }

    @Test
    public void testEncodeByteArrayWithTabAndSpace() {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        byte[] input = new byte[] { 9, 32 };
        byte[] output = codec.encode(input);
        // TAB and SPACE are printable, so they should not be encoded
        assertArrayEquals(input, output);
    }

    @Test
    public void testDecodeByteArrayNull() throws DecoderException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        assertNull(codec.decode((byte[]) null));
    }

    @Test
    public void testDecodeByteArrayEmpty() throws DecoderException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        byte[] input = new byte[0];
        byte[] output = codec.decode(input);
        assertNotNull(output);
        assertEquals(0, output.length);
    }

    @Test
    public void testDecodeByteArrayNoEscape() throws DecoderException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        byte[] input = "Hello World".getBytes();
        byte[] output = codec.decode(input);
        assertArrayEquals(input, output);
    }

    @Test
    public void testDecodeByteArrayValidEscape() throws DecoderException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        byte[] input = "=41=42=43".getBytes(); // "ABC"
        byte[] output = codec.decode(input);
        assertArrayEquals("ABC".getBytes(), output);
    }

    @Test
    public void testDecodeByteArrayLowerCaseHex() throws DecoderException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        byte[] input = "=61=62=63".getBytes(); // "abc"
        byte[] output = codec.decode(input);
        assertArrayEquals("abc".getBytes(), output);
    }

    @Test
    public void testDecodeByteArrayMixed() throws DecoderException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        byte[] input = "A=42C".getBytes(); // "ABC"
        byte[] output = codec.decode(input);
        assertArrayEquals("ABC".getBytes(), output);
    }

    @Test(expected = DecoderException.class)
    public void testDecodeByteArrayIncompleteEscapeAtEnd() throws DecoderException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        byte[] input = "A=".getBytes(); // incomplete escape at end
        codec.decode(input);
    }

    @Test(expected = DecoderException.class)
    public void testDecodeByteArrayIncompleteEscapeOneChar() throws DecoderException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        byte[] input = "A=4".getBytes(); // only one hex digit
        codec.decode(input);
    }

    @Test
    public void testDecodeByteArrayInvalidHexDoesNotThrow() throws DecoderException {
        // The implementation does not validate hex digits; it uses Utils.digit16 which returns -1 for non-hex.
        // So it will produce some output without throwing.
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        byte[] input = "=XX".getBytes();
        byte[] output = codec.decode(input);
        // Expected: (char)((-1<<4)+-1) = (char)(-16-1) = (char)(-17) = 0xFFEF? Actually char is unsigned, so it's 65519.
        // But we just check that no exception is thrown and output length is 1.
        assertNotNull(output);
        assertEquals(1, output.length);
    }

    @Test
    public void testDecodeByteArraySoftLineBreakNotSupported() throws DecoderException {
        // Soft line break =CRLF is not handled; it will try to decode CR and LF as hex.
        // This does not throw because both bytes are present.
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        byte[] input = "=\r\n".getBytes();
        byte[] output = codec.decode(input);
        // It will decode \r (13) and \n (10) as hex digits, producing some character.
        assertNotNull(output);
        assertEquals(1, output.length);
    }

    @Test
    public void testEncodeStringNull() throws EncoderException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        assertNull(codec.encode((String) null));
    }

    @Test
    public void testEncodeStringEmpty() throws EncoderException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        assertEquals("", codec.encode(""));
    }

    @Test
    public void testEncodeStringPrintable() throws EncoderException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        String input = "Hello World!";
        String output = codec.encode(input);
        assertEquals(input, output);
    }

    @Test
    public void testEncodeStringNonPrintable() throws EncoderException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        String input = "a\u0000b"; // null char
        String output = codec.encode(input);
        assertEquals("a=00b", output);
    }

    @Test
    public void testEncodeStringWithCharset() throws UnsupportedEncodingException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec(ISO_8859_1);
        String input = "é"; // e acute in ISO-8859-1 is 0xE9
        String output = codec.encode(input, ISO_8859_1);
        assertEquals("=E9", output);
    }

    @Test
    public void testEncodeStringWithCharsetNullString() throws UnsupportedEncodingException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        assertNull(codec.encode(null, UTF_8));
    }

    @Test(expected = UnsupportedEncodingException.class)
    public void testEncodeStringWithInvalidCharset() throws UnsupportedEncodingException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        codec.encode("test", "INVALID_CHARSET");
    }

    @Test
    public void testDecodeStringNull() throws DecoderException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        assertNull(codec.decode((String) null));
    }

    @Test
    public void testDecodeStringEmpty() throws DecoderException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        assertEquals("", codec.decode(""));
    }

    @Test
    public void testDecodeStringNoEscape() throws DecoderException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        String input = "Hello World!";
        assertEquals(input, codec.decode(input));
    }

    @Test
    public void testDecodeStringValidEscape() throws DecoderException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        String input = "=48=65=6C=6C=6F"; // "Hello"
        assertEquals("Hello", codec.decode(input));
    }

    @Test
    public void testDecodeStringWithCharset() throws DecoderException, UnsupportedEncodingException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec(ISO_8859_1);
        String input = "=E9"; // e acute
        assertEquals("é", codec.decode(input, ISO_8859_1));
    }

    @Test
    public void testDecodeStringWithCharsetNullString() throws DecoderException, UnsupportedEncodingException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        assertNull(codec.decode(null, UTF_8));
    }

    @Test(expected = UnsupportedEncodingException.class)
    public void testDecodeStringWithInvalidCharset() throws DecoderException, UnsupportedEncodingException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        codec.decode("=41", "INVALID_CHARSET");
    }

    @Test
    public void testEncodeObjectNull() throws EncoderException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        assertNull(codec.encode((Object) null));
    }

    @Test
    public void testEncodeObjectByteArray() throws EncoderException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        byte[] input = new byte[] { 0x00, 0x41 };
        byte[] expected = new byte[] { '=', '0', '0', 'A' };
        assertArrayEquals(expected, (byte[]) codec.encode((Object) input));
    }

    @Test
    public void testEncodeObjectString() throws EncoderException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        String input = "Hello";
        assertEquals("Hello", codec.encode((Object) input));
    }

    @Test(expected = EncoderException.class)
    public void testEncodeObjectInvalidType() throws EncoderException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        codec.encode(new Integer(1));
    }

    @Test
    public void testDecodeObjectNull() throws DecoderException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        assertNull(codec.decode((Object) null));
    }

    @Test
    public void testDecodeObjectByteArray() throws DecoderException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        byte[] input = "=41=42".getBytes();
        byte[] expected = "AB".getBytes();
        assertArrayEquals(expected, (byte[]) codec.decode((Object) input));
    }

    @Test
    public void testDecodeObjectString() throws DecoderException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        String input = "=48=65=6C=6C=6F";
        assertEquals("Hello", codec.decode((Object) input));
    }

    @Test(expected = DecoderException.class)
    public void testDecodeObjectInvalidType() throws DecoderException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        codec.decode(new Integer(1));
    }

    @Test
    public void testStaticEncodeQuotedPrintableNullBytes() {
        assertNull(QuotedPrintableCodec.encodeQuotedPrintable(null, (byte[]) null));
    }

    @Test
    public void testStaticEncodeQuotedPrintableNullPrintable() {
        byte[] input = new byte[] { 0x00, 0x41 };
        byte[] output = QuotedPrintableCodec.encodeQuotedPrintable(null, input);
        // Should use default PRINTABLE_CHARS, so 0x00 encoded, 0x41 not
        byte[] expected = new byte[] { '=', '0', '0', 'A' };
        assertArrayEquals(expected, output);
    }

    @Test
    public void testStaticEncodeQuotedPrintableCustomBitSetAllPrintable() {
        BitSet all = new BitSet(256);
        for (int i = 0; i < 256; i++) {
            all.set(i);
        }
        byte[] input = new byte[] { 0x00, 0x41, (byte) 0xFF };
        byte[] output = QuotedPrintableCodec.encodeQuotedPrintable(all, input);
        // All bytes are printable, so output equals input
        assertArrayEquals(input, output);
    }

    @Test
    public void testStaticEncodeQuotedPrintableCustomBitSetNonePrintable() {
        BitSet none = new BitSet(256); // empty
        byte[] input = new byte[] { 0x41, 0x42 };
        byte[] output = QuotedPrintableCodec.encodeQuotedPrintable(none, input);
        // All bytes encoded
        byte[] expected = new byte[] { '=', '4', '1', '=', '4', '2' };
        assertArrayEquals(expected, output);
    }

    @Test
    public void testStaticDecodeQuotedPrintableNull() throws DecoderException {
        assertNull(QuotedPrintableCodec.decodeQuotedPrintable(null));
    }

    @Test
    public void testStaticDecodeQuotedPrintableValid() throws DecoderException {
        byte[] input = "=41=42".getBytes();
        byte[] expected = "AB".getBytes();
        assertArrayEquals(expected, QuotedPrintableCodec.decodeQuotedPrintable(input));
    }

    @Test(expected = DecoderException.class)
    public void testStaticDecodeQuotedPrintableIncomplete() throws DecoderException {
        QuotedPrintableCodec.decodeQuotedPrintable("=4".getBytes());
    }

    @Test
    public void testRoundTripByteArray() throws DecoderException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        byte[] original = new byte[] { 0x00, 0x41, 0x0D, 0x0A, (byte) 0xFF, 0x20, 0x09 };
        byte[] encoded = codec.encode(original);
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals(original, decoded);
    }

    @Test
    public void testRoundTripString() throws EncoderException, DecoderException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        String original = "Hello World!\r\n\u0000\u00FF";
        String encoded = codec.encode(original);
        String decoded = codec.decode(encoded);
        assertEquals(original, decoded);
    }

    @Test
    public void testRoundTripStringWithCharset() throws UnsupportedEncodingException, DecoderException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec(ISO_8859_1);
        String original = "éàç";
        String encoded = codec.encode(original, ISO_8859_1);
        String decoded = codec.decode(encoded, ISO_8859_1);
        assertEquals(original, decoded);
    }

    @Test
    public void testGetDefaultCharset() {
        QuotedPrintableCodec codec = new QuotedPrintableCodec("UTF-16");
        assertEquals("UTF-16", codec.getDefaultCharset());
    }
}
