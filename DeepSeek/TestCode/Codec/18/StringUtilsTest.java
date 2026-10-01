package org.apache.commons.codec.binary;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.apache.commons.codec.Charsets;
import org.junit.Test;

public class StringUtilsTest {

    // ---------- equals ----------

    @Test
    public void testEqualsBothNull() {
        assertTrue(StringUtils.equals(null, null));
    }

    @Test
    public void testEqualsSameReference() {
        StringBuilder sb = new StringBuilder("abc");
        assertTrue(StringUtils.equals(sb, sb));
    }

    @Test
    public void testEqualsFirstNull() {
        assertFalse(StringUtils.equals(null, "abc"));
    }

    @Test
    public void testEqualsSecondNull() {
        assertFalse(StringUtils.equals("abc", null));
    }

    @Test
    public void testEqualsBothStringEqual() {
        assertTrue(StringUtils.equals("abc", "abc"));
    }

    @Test
    public void testEqualsBothStringNotEqual() {
        assertFalse(StringUtils.equals("abc", "ABC"));
    }

    @Test
    public void testEqualsBothStringDifferentLength() {
        assertFalse(StringUtils.equals("abc", "ab"));
    }

    @Test
    public void testEqualsBothStringBuilderEqual() {
        assertTrue(StringUtils.equals(new StringBuilder("abc"), new StringBuilder("abc")));
    }

    @Test
    public void testEqualsBothStringBuilderNotEqual() {
        assertFalse(StringUtils.equals(new StringBuilder("abc"), new StringBuilder("abd")));
    }

    @Test
    public void testEqualsStringAndStringBuilderEqual() {
        assertTrue(StringUtils.equals("abc", new StringBuilder("abc")));
    }

    @Test
    public void testEqualsStringAndStringBuilderDifferentLength() {
        assertFalse(StringUtils.equals("abc", new StringBuilder("ab")));
    }

    @Test
    public void testEqualsStringBufferNotEqual() {
        assertFalse(StringUtils.equals(new StringBuffer("abc"), new StringBuffer("ABC")));
    }

    // ---------- getByteBufferUtf8 ----------

    @Test
    public void testGetByteBufferUtf8Null() {
        assertNull(StringUtils.getByteBufferUtf8(null));
    }

    @Test
    public void testGetByteBufferUtf8Empty() {
        ByteBuffer buf = StringUtils.getByteBufferUtf8("");
        assertEquals(0, buf.remaining());
    }

    @Test
    public void testGetByteBufferUtf8NonEmpty() {
        String input = "test";
        byte[] expected = input.getBytes(Charsets.UTF_8);
        ByteBuffer buf = StringUtils.getByteBufferUtf8(input);
        byte[] actual = new byte[buf.remaining()];
        buf.get(actual);
        assertArrayEquals(expected, actual);
    }

    // ---------- getBytesIso8859_1 ----------

    @Test
    public void testGetBytesIso8859_1Null() {
        assertNull(StringUtils.getBytesIso8859_1(null));
    }

    @Test
    public void testGetBytesIso8859_1Empty() {
        assertArrayEquals(new byte[0], StringUtils.getBytesIso8859_1(""));
    }

    @Test
    public void testGetBytesIso8859_1NonEmpty() {
        String input = "test";
        assertArrayEquals(input.getBytes(Charsets.ISO_8859_1), StringUtils.getBytesIso8859_1(input));
    }

    // ---------- getBytesUnchecked ----------

    @Test
    public void testGetBytesUncheckedNull() {
        assertNull(StringUtils.getBytesUnchecked(null, "UTF-8"));
    }

    @Test
    public void testGetBytesUncheckedValidCharset() {
        String input = "test";
        byte[] expected = input.getBytes(Charset.forName("UTF-8"));
        assertArrayEquals(expected, StringUtils.getBytesUnchecked(input, "UTF-8"));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetBytesUncheckedInvalidCharset() {
        StringUtils.getBytesUnchecked("test", "INVALID");
    }

    @Test
    public void testGetBytesUncheckedInvalidCharsetMessage() {
        try {
            StringUtils.getBytesUnchecked("test", "INVALID");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("INVALID"));        }
    }

    // ---------- getBytesUsAscii ----------

    @Test
    public void testGetBytesUsAsciiNull() {
        assertNull(StringUtils.getBytesUsAscii(null));
    }

    @Test
    public void testGetBytesUsAsciiEmpty() {
        assertArrayEquals(new byte[0], StringUtils.getBytesUsAscii(""));
    }

    @Test
    public void testGetBytesUsAsciiNonEmpty() {
        String input = "test";
        assertArrayEquals(input.getBytes(Charsets.US_ASCII), StringUtils.getBytesUsAscii(input));
    }

    // ---------- getBytesUtf16 ----------

    @Test
    public void testGetBytesUtf16Null() {
        assertNull(StringUtils.getBytesUtf16(null));
    }

    @Test
    public void testGetBytesUtf16Empty() {
        assertArrayEquals(new byte[0], StringUtils.getBytesUtf16(""));
    }

    @Test
    public void testGetBytesUtf16NonEmpty() {
        String input = "test";
        assertArrayEquals(input.getBytes(Charsets.UTF_16), StringUtils.getBytesUtf16(input));
    }

    // ---------- getBytesUtf16Be ----------

    @Test
    public void testGetBytesUtf16BeNull() {
        assertNull(StringUtils.getBytesUtf16Be(null));
    }

    @Test
    public void testGetBytesUtf16BeEmpty() {
        assertArrayEquals(new byte[0], StringUtils.getBytesUtf16Be(""));
    }

    @Test
    public void testGetBytesUtf16BeNonEmpty() {
        String input = "test";
        assertArrayEquals(input.getBytes(Charsets.UTF_16BE), StringUtils.getBytesUtf16Be(input));
    }

    // ---------- getBytesUtf16Le ----------

    @Test
    public void testGetBytesUtf16LeNull() {
        assertNull(StringUtils.getBytesUtf16Le(null));
    }

    @Test
    public void testGetBytesUtf16LeEmpty() {
        assertArrayEquals(new byte[0], StringUtils.getBytesUtf16Le(""));
    }

    @Test
    public void testGetBytesUtf16LeNonEmpty() {
        String input = "test";
        assertArrayEquals(input.getBytes(Charsets.UTF_16LE), StringUtils.getBytesUtf16Le(input));
    }

    // ---------- getBytesUtf8 ----------

    @Test
    public void testGetBytesUtf8Null() {
        assertNull(StringUtils.getBytesUtf8(null));
    }

    @Test
    public void testGetBytesUtf8Empty() {
        assertArrayEquals(new byte[0], StringUtils.getBytesUtf8(""));
    }

    @Test
    public void testGetBytesUtf8NonEmpty() {
        String input = "test";
        assertArrayEquals(input.getBytes(Charsets.UTF_8), StringUtils.getBytesUtf8(input));
    }

    // ---------- newString(bytes, String) ----------

    @Test
    public void testNewStringBytesNull() {
        assertNull(StringUtils.newString(null, "UTF-8"));
    }

    @Test
    public void testNewStringValidCharset() {
        byte[] bytes = "test".getBytes(Charset.forName("UTF-8"));
        assertEquals("test", StringUtils.newString(bytes, "UTF-8"));
    }

    @Test(expected = IllegalStateException.class)
    public void testNewStringInvalidCharset() {
        StringUtils.newString(new byte[]{}, "INVALID");
    }

    @Test
    public void testNewStringInvalidCharsetMessage() {
        try {
            StringUtils.newString(new byte[]{}, "INVALID");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("INVALID"));        }
    }

    // ---------- newStringIso8859_1 ----------

    @Test
    public void testNewStringIso8859_1Null() {
        assertNull(StringUtils.newStringIso8859_1(null));
    }

    @Test
    public void testNewStringIso8859_1NonEmpty() {
        byte[] bytes = "test".getBytes(Charsets.ISO_8859_1);
        assertEquals("test", StringUtils.newStringIso8859_1(bytes));
    }

    // ---------- newStringUsAscii ----------

    @Test
    public void testNewStringUsAsciiNull() {
        assertNull(StringUtils.newStringUsAscii(null));
    }

    @Test
    public void testNewStringUsAsciiNonEmpty() {
        byte[] bytes = "test".getBytes(Charsets.US_ASCII);
        assertEquals("test", StringUtils.newStringUsAscii(bytes));
    }

    // ---------- newStringUtf16 ----------

    @Test
    public void testNewStringUtf16Null() {
        assertNull(StringUtils.newStringUtf16(null));
    }

    @Test
    public void testNewStringUtf16NonEmpty() {
        byte[] bytes = "test".getBytes(Charsets.UTF_16);
        assertEquals("test", StringUtils.newStringUtf16(bytes));
    }

    // ---------- newStringUtf16Be ----------

    @Test
    public void testNewStringUtf16BeNull() {
        assertNull(StringUtils.newStringUtf16Be(null));
    }

    @Test
    public void testNewStringUtf16BeNonEmpty() {
        byte[] bytes = "test".getBytes(Charsets.UTF_16BE);
        assertEquals("test", StringUtils.newStringUtf16Be(bytes));
    }

    // ---------- newStringUtf16Le ----------

    @Test
    public void testNewStringUtf16LeNull() {
        assertNull(StringUtils.newStringUtf16Le(null));
    }

    @Test
    public void testNewStringUtf16LeNonEmpty() {
        byte[] bytes = "test".getBytes(Charsets.UTF_16LE);
        assertEquals("test", StringUtils.newStringUtf16Le(bytes));
    }

    // ---------- newStringUtf8 ----------

    @Test
    public void testNewStringUtf8Null() {
        assertNull(StringUtils.newStringUtf8(null));
    }

    @Test
    public void testNewStringUtf8NonEmpty() {
        byte[] bytes = "test".getBytes(Charsets.UTF_8);
        assertEquals("test", StringUtils.newStringUtf8(bytes));
    }

    // Roundtrip tests to verify encode/decode consistency

    @Test
    public void testRoundtripUtf8() {
        String original = "Hello, 世界!";
        byte[] encoded = StringUtils.getBytesUtf8(original);
        String decoded = StringUtils.newStringUtf8(encoded);
        assertEquals(original, decoded);
    }

    @Test
    public void testRoundtripUtf16() {
        String original = "Hello, 世界!";
        byte[] encoded = StringUtils.getBytesUtf16(original);
        String decoded = StringUtils.newStringUtf16(encoded);
        assertEquals(original, decoded);
    }

    @Test
    public void testRoundtripIso8859_1() {
        // only works for characters within ISO-8859-1
        String original = "Café";
        byte[] encoded = StringUtils.getBytesIso8859_1(original);
        String decoded = StringUtils.newStringIso8859_1(encoded);
        assertEquals(original, decoded);
    }

    @Test
    public void testRoundtripUsAscii() {
        String original = "test";
        byte[] encoded = StringUtils.getBytesUsAscii(original);
        String decoded = StringUtils.newStringUsAscii(encoded);
        assertEquals(original, decoded);
    }

    @Test
    public void testRoundtripUtf16Be() {
        String original = "Hello, World!";
        byte[] encoded = StringUtils.getBytesUtf16Be(original);
        String decoded = StringUtils.newStringUtf16Be(encoded);
        assertEquals(original, decoded);
    }

    @Test
    public void testRoundtripUtf16Le() {
        String original = "Hello, World!";
        byte[] encoded = StringUtils.getBytesUtf16Le(original);
        String decoded = StringUtils.newStringUtf16Le(encoded);
        assertEquals(original, decoded);
    }

    // Edge case: encoding of empty string yields empty byte arrays, decoding yields empty string

    @Test
    public void testEmptyEncodingUtf8() {
        assertArrayEquals(new byte[0], StringUtils.getBytesUtf8(""));
        assertEquals("", StringUtils.newStringUtf8(new byte[0]));
    }

    @Test
    public void testEmptyEncodingUtf16() {
        assertArrayEquals(new byte[0], StringUtils.getBytesUtf16(""));
        assertEquals("", StringUtils.newStringUtf16(new byte[0]));
    }

    @Test
    public void testEmptyEncodingIso8895_1() {
        assertArrayEquals(new byte[0], StringUtils.getBytesIso8895_1(""));
        assertEquals("", StringUtils.newStringIso8895_1(new byte[0]));
    }

    @Test
    public void testEmptyEncodingUsAscii() {
        assertArrayEquals(new byte[0], StringUtils.getBytesUsAscii(""));
        assertEquals("", StringUtils.newStringUsAscii(new byte[0]));
    }
}
