package org.apache.commons.codec.binary;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.apache.commons.codec.Charsets;
import org.junit.Test;

public class StringUtilsTest {

    // Helper CharSequence implementation to test equals with non-String types
    private static final class CustomCharSequence implements CharSequence {
        private final String value;

        CustomCharSequence(final String value) {
            this.value = value;
        }

        @Override
        public int length() {
            return value.length();
        }

        @Override
        public char charAt(final int index) {
            return value.charAt(index);
        }

        @Override
        public CharSequence subSequence(final int start, final int end) {
            return value.subSequence(start, end);
        }

        @Override
        public String toString() {
            return value;
        }
    }

    // ------------------- equals tests -------------------

    @Test
    public void testEqualsBothNull() {
        assertTrue(StringUtils.equals(null, null));
    }

    @Test
    public void testEqualsOneNullFirstNull() {
        assertFalse(StringUtils.equals(null, "abc"));
    }

    @Test
    public void testEqualsOneNullSecondNull() {
        assertFalse(StringUtils.equals("abc", null));
    }

    @Test
    public void testEqualsBothSameString() {
        assertTrue(StringUtils.equals("abc", "abc"));
    }

    @Test
    public void testEqualsDifferentStrings() {
        assertFalse(StringUtils.equals("abc", "def"));
    }

    @Test
    public void testEqualsSameReference() {
        final String ref = "hello";
        assertTrue(StringUtils.equals(ref, ref));
    }

    @Test
    public void testEqualsNonStringCharSequenceEqual() {
        final CharSequence a = new CustomCharSequence("test");
        final CharSequence b = new CustomCharSequence("test");
        assertTrue(StringUtils.equals(a, b));
    }

    @Test
    public void testEqualsNonStringCharSequenceUnequal() {
        final CharSequence a = new CustomCharSequence("test");
        final CharSequence b = new CustomCharSequence("other");
        assertFalse(StringUtils.equals(a, b));
    }

    @Test
    public void testEqualsStringVsCharSequenceEqual() {
        assertTrue(StringUtils.equals("test", new CustomCharSequence("test")));
    }

    @Test
    public void testEqualsStringVsCharSequenceDifferent() {
        assertFalse(StringUtils.equals("test", new CustomCharSequence("other")));
    }

    @Test
    public void testEqualsDifferentLength() {
        // regionMatches uses max length, should return false when lengths differ
        assertFalse(StringUtils.equals("abc", "abcd"));
        assertFalse(StringUtils.equals(new CustomCharSequence("abc"), new CustomCharSequence("abcd")));
    }

    // ------------------- getByteBufferUtf8 tests -------------------

    @Test
    public void testGetByteBufferUtf8Null() {
        assertNull(StringUtils.getByteBufferUtf8(null));
    }

    @Test
    public void testGetByteBufferUtf8EmptyString() {
        final ByteBuffer result = StringUtils.getByteBufferUtf8("");
        assertNotNull(result);
        assertEquals(0, result.remaining());
    }

    @Test
    public void testGetByteBufferUtf8Ascii() {
        final ByteBuffer result = StringUtils.getByteBufferUtf8("hello");
        final byte[] expected = "hello".getBytes(Charsets.UTF_8);
        final byte[] actual = new byte[result.remaining()];
        result.get(actual);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testGetByteBufferUtf8NonAscii() {
        final ByteBuffer result = StringUtils.getByteBufferUtf8("é∑");
        final byte[] expected = "é∑".getBytes(Charsets.UTF_8);
        final byte[] actual = new byte[result.remaining()];
        result.get(actual);
        assertArrayEquals(expected, actual);
    }

    // ------------------- getBytesIso8859_1 tests -------------------

    @Test
    public void testGetBytesIso8859_1Null() {
        assertNull(StringUtils.getBytesIso8859_1(null));
    }

    @Test
    public void testGetBytesIso8859_1Empty() {
        final byte[] result = StringUtils.getBytesIso8859_1("");
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    public void testGetBytesIso8859_1Text() {
        final String input = "hello";
        final byte[] expected = input.getBytes(Charsets.ISO_8859_1);
        assertArrayEquals(expected, StringUtils.getBytesIso8859_1(input));
    }

    @Test
    public void testGetBytesIso8859_1SpecialChars() {
        // characters that fit into ISO-8859-1
        final String input = "ÀÖÜ";
        final byte[] expected = input.getBytes(Charsets.ISO_8859_1);
        assertArrayEquals(expected, StringUtils.getBytesIso8859_1(input));
    }

    // ------------------- getBytesUsAscii tests -------------------

    @Test
    public void testGetBytesUsAsciiNull() {
        assertNull(StringUtils.getBytesUsAscii(null));
    }

    @Test
    public void testGetBytesUsAsciiEmpty() {
        assertArrayEquals(new byte[0], StringUtils.getBytesUsAscii(""));
    }

    @Test
    public void testGetBytesUsAsciiText() {
        final String input = "hello";
        final byte[] expected = input.getBytes(Charsets.US_ASCII);
        assertArrayEquals(expected, StringUtils.getBytesUsAscii(input));
    }

    // ------------------- getBytesUtf16 tests -------------------

    @Test
    public void testGetBytesUtf16Null() {
        assertNull(StringUtils.getBytesUtf16(null));
    }

    @Test
    public void testGetBytesUtf16Empty() {
        assertArrayEquals(new byte[0], StringUtils.getBytesUtf16(""));
    }

    @Test
    public void testGetBytesUtf16Text() {
        final String input = "hello";
        final byte[] expected = input.getBytes(Charsets.UTF_16);
        assertArrayEquals(expected, StringUtils.getBytesUtf16(input));
    }

    // ------------------- getBytesUtf16Be tests -------------------

    @Test
    public void testGetBytesUtf16BeNull() {
        assertNull(StringUtils.getBytesUtf16Be(null));
    }

    @Test
    public void testGetBytesUtf16BeEmpty() {
        assertArrayEquals(new byte[0], StringUtils.getBytesUtf16Be(""));
    }

    @Test
    public void testGetBytesUtf16BeText() {
        final String input = "hello";
        final byte[] expected = input.getBytes(Charsets.UTF_16BE);
        assertArrayEquals(expected, StringUtils.getBytesUtf16Be(input));
    }

    // ------------------- getBytesUtf16Le tests -------------------

    @Test
    public void testGetBytesUtf16LeNull() {
        assertNull(StringUtils.getBytesUtf16Le(null));
    }

    @Test
    public void testGetBytesUtf16LeEmpty() {
        assertArrayEquals(new byte[0], StringUtils.getBytesUtf16Le(""));
    }

    @Test
    public void testGetBytesUtf16LeText() {
        final String input = "hello";
        final byte[] expected = input.getBytes(Charsets.UTF_16LE);
        assertArrayEquals(expected, StringUtils.getBytesUtf16Le(input));
    }

    // ------------------- getBytesUtf8 tests -------------------

    @Test
    public void testGetBytesUtf8Null() {
        assertNull(StringUtils.getBytesUtf8(null));
    }

    @Test
    public void testGetBytesUtf8Empty() {
        assertArrayEquals(new byte[0], StringUtils.getBytesUtf8(""));
    }

    @Test
    public void testGetBytesUtf8Text() {
        final String input = "hello";
        final byte[] expected = input.getBytes(Charsets.UTF_8);
        assertArrayEquals(expected, StringUtils.getBytesUtf8(input));
    }

    @Test
    public void testGetBytesUtf8NonAscii() {
        final String input = "é∑";
        final byte[] expected = input.getBytes(Charsets.UTF_8);
        assertArrayEquals(expected, StringUtils.getBytesUtf8(input));
    }

    // ------------------- getBytesUnchecked tests -------------------

    @Test
    public void testGetBytesUncheckedNull() {
        assertNull(StringUtils.getBytesUnchecked(null, "UTF-8"));
    }

    @Test
    public void testGetBytesUncheckedValidCharset() {
        final String input = "test";
        final byte[] expected = input.getBytes(Charsets.UTF_8);
        assertArrayEquals(expected, StringUtils.getBytesUnchecked(input, "UTF-8"));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetBytesUncheckedInvalidCharset() {
        StringUtils.getBytesUnchecked("test", "INVALID-CHARSET-NAME");
    }

    @Test
    public void testGetBytesUncheckedEmptyString() {
        assertArrayEquals(new byte[0], StringUtils.getBytesUnchecked("", "UTF-8"));
    }

    // ------------------- newString(byte[], String) tests -------------------

    @Test
    public void testNewStringNullBytes() {
        assertNull(StringUtils.newString(null, "UTF-8"));
    }

    @Test
    public void testNewStringValid() {
        final byte[] bytes = "hello".getBytes(Charsets.UTF_8);
        assertEquals("hello", StringUtils.newString(bytes, "UTF-8"));
    }

    @Test(expected = IllegalStateException.class)
    public void testNewStringInvalidCharset() {
        StringUtils.newString(new byte[] { 65 }, "INVALID-CHARSET");
    }

    @Test
    public void testNewStringEmptyBytes() {
        assertEquals("", StringUtils.newString(new byte[0], "UTF-8"));
    }

    // ------------------- newStringIso8859_1 tests -------------------

    @Test
    public void testNewStringIso8859_1Null() {
        assertNull(StringUtils.newStringIso8859_1(null));
    }

    @Test
    public void testNewStringIso8859_1Valid() {
        final byte[] bytes = "ÀÖÜ".getBytes(Charsets.ISO_8859_1);
        assertEquals("ÀÖÜ", StringUtils.newStringIso8859_1(bytes));
    }

    @Test
    public void testNewStringIso8859_1Empty() {
        assertEquals("", StringUtils.newStringIso8859_1(new byte[0]));
    }

    // ------------------- newStringUsAscii tests -------------------

    @Test
    public void testNewStringUsAsciiNull() {
        assertNull(StringUtils.newStringUsAscii(null));
    }

    @Test
    public void testNewStringUsAsciiValid() {
        final byte[] bytes = "hello".getBytes(Charsets.US_ASCII);
        assertEquals("hello", StringUtils.newStringUsAscii(bytes));
    }

    @Test
    public void testNewStringUsAsciiEmpty() {
        assertEquals("", StringUtils.newStringUsAscii(new byte[0]));
    }

    // ------------------- newStringUtf16 tests -------------------

    @Test
    public void testNewStringUtf16Null() {
        assertNull(StringUtils.newStringUtf16(null));
    }

    @Test
    public void testNewStringUtf16Valid() {
        final byte[] bytes = "hello".getBytes(Charsets.UTF_16);
        assertEquals("hello", StringUtils.newStringUtf16(bytes));
    }

    @Test
    public void testNewStringUtf16Empty() {
        assertEquals("", StringUtils.newStringUtf16(new byte[0]));
    }

    // ------------------- newStringUtf16Be tests -------------------

    @Test
    public void testNewStringUtf16BeNull() {
        assertNull(StringUtils.newStringUtf16Be(null));
    }

    @Test
    public void testNewStringUtf16BeValid() {
        final byte[] bytes = "hello".getBytes(Charsets.UTF_16BE);
        assertEquals("hello", StringUtils.newStringUtf16Be(bytes));
    }

    @Test
    public void testNewStringUtf16BeEmpty() {
        assertEquals("", StringUtils.newStringUtf16Be(new byte[0]));
    }

    // ------------------- newStringUtf16Le tests -------------------

    @Test
    public void testNewStringUtf16LeNull() {
        assertNull(StringUtils.newStringUtf16Le(null));
    }

    @Test
    public void testNewStringUtf16LeValid() {
        final byte[] bytes = "hello".getBytes(Charsets.UTF_16LE);
        assertEquals("hello", StringUtils.newStringUtf16Le(bytes));
    }

    @Test
    public void testNewStringUtf16LeEmpty() {
        assertEquals("", StringUtils.newStringUtf16Le(new byte[0]));
    }

    // ------------------- newStringUtf8 tests -------------------

    @Test
    public void testNewStringUtf8Null() {
        assertNull(StringUtils.newStringUtf8(null));
    }

    @Test
    public void testNewStringUtf8Valid() {
        final byte[] bytes = "é∑".getBytes(Charsets.UTF_8);
        assertEquals("é∑", StringUtils.newStringUtf8(bytes));
    }

    @Test
    public void testNewStringUtf8Empty() {
        assertEquals("", StringUtils.newStringUtf8(new byte[0]));
    }

    // ------------------- Round-trip tests -------------------

    @Test
    public void testRoundtripUtf8() {
        final String original = "test roundtrip é";
        assertEquals(original, StringUtils.newStringUtf8(StringUtils.getBytesUtf8(original)));
    }

    @Test
    public void testRoundtripIso8859_1() {
        final String original = "ÀÖÜ hello";
        assertEquals(original, StringUtils.newStringIso8859_1(StringUtils.getBytesIso8859_1(original)));
    }

    @Test
    public void testRoundtripUsAscii() {
        final String original = "hello";
        assertEquals(original, StringUtils.newStringUsAscii(StringUtils.getBytesUsAscii(original)));
    }

    @Test
    public void testRoundtripUtf16() {
        final String original = "hello";
        assertEquals(original, StringUtils.newStringUtf16(StringUtils.getBytesUtf16(original)));
    }

    @Test
    public void testRoundtripUtf16Be() {
        final String original = "hello";
        assertEquals(original, StringUtils.newStringUtf16Be(StringUtils.getBytesUtf16Be(original)));
    }

    @Test
    public void testRoundtripUtf16Le() {
        final String original = "hello";
        assertEquals(original, StringUtils.newStringUtf16Le(StringUtils.getBytesUtf16Le(original)));
    }

    @Test
    public void testGetByteBufferUtf8Roundtrip() {
        final String original = "roundtrip";
        final ByteBuffer buffer = StringUtils.getByteBufferUtf8(original);
        final byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        assertEquals(original, StringUtils.newStringUtf8(bytes));
    }
}
