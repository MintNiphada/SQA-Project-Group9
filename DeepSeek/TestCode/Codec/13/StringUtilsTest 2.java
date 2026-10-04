package org.apache.commons.codec.binary;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.junit.Test;

public class StringUtilsTest {

    // ------------------- getBytesIso8859_1 -------------------

    @Test
    public void testGetBytesIso8859_1_null() {
        assertNull(StringUtils.getBytesIso8859_1(null));
    }

    @Test
    public void testGetBytesIso8859_1_empty() {
        byte[] result = StringUtils.getBytesIso8859_1("");
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    public void testGetBytesIso8859_1_normal() {
        String input = "test";
        byte[] expected = input.getBytes(Charset.forName("ISO-8859-1"));
        byte[] result = StringUtils.getBytesIso8859_1(input);
        assertArrayEquals(expected, result);
    }

    @Test
    public void testGetBytesIso8859_1_specialChars() {
        String input = "éçüñ";
        byte[] expected = input.getBytes(Charset.forName("ISO-8859-1"));
        byte[] result = StringUtils.getBytesIso8859_1(input);
        assertArrayEquals(expected, result);
    }

    // ------------------- getBytesUsAscii -------------------

    @Test
    public void testGetBytesUsAscii_null() {
        assertNull(StringUtils.getBytesUsAscii(null));
    }

    @Test
    public void testGetBytesUsAscii_empty() {
        byte[] result = StringUtils.getBytesUsAscii("");
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    public void testGetBytesUsAscii_normal() {
        String input = "test";
        byte[] expected = input.getBytes(Charset.forName("US-ASCII"));
        byte[] result = StringUtils.getBytesUsAscii(input);
        assertArrayEquals(expected, result);
    }

    @Test
    public void testGetBytesUsAscii_specialChars() {
        // ASCII does not support these, but Java will replace them with '?'
        String input = "éçüñ";
        byte[] expected = input.getBytes(Charset.forName("US-ASCII"));
        byte[] result = StringUtils.getBytesUsAscii(input);
        assertArrayEquals(expected, result);
    }

    // ------------------- getBytesUtf8 -------------------

    @Test
    public void testGetBytesUtf8_null() {
        assertNull(StringUtils.getBytesUtf8(null));
    }

    @Test
    public void testGetBytesUtf8_empty() {
        byte[] result = StringUtils.getBytesUtf8("");
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    public void testGetBytesUtf8_normal() {
        String input = "test";
        byte[] expected = input.getBytes(Charset.forName("UTF-8"));
        byte[] result = StringUtils.getBytesUtf8(input);
        assertArrayEquals(expected, result);
    }

    @Test
    public void testGetBytesUtf8_specialChars() {
        String input = "éçüñ";
        byte[] expected = input.getBytes(Charset.forName("UTF-8"));
        byte[] result = StringUtils.getBytesUtf8(input);
        assertArrayEquals(expected, result);
    }

    // ------------------- getBytesUtf16 -------------------

    @Test
    public void testGetBytesUtf16_null() {
        assertNull(StringUtils.getBytesUtf16(null));
    }

    @Test
    public void testGetBytesUtf16_empty() {
        byte[] result = StringUtils.getBytesUtf16("");
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    public void testGetBytesUtf16_normal() {
        String input = "test";
        byte[] expected = input.getBytes(Charset.forName("UTF-16"));
        byte[] result = StringUtils.getBytesUtf16(input);
        assertArrayEquals(expected, result);
    }

    @Test
    public void testGetBytesUtf16_specialChars() {
        String input = "éçüñ";
        byte[] expected = input.getBytes(Charset.forName("UTF-16"));
        byte[] result = StringUtils.getBytesUtf16(input);
        assertArrayEquals(expected, result);
    }

    // ------------------- getBytesUtf16Be -------------------

    @Test
    public void testGetBytesUtf16Be_null() {
        assertNull(StringUtils.getBytesUtf16Be(null));
    }

    @Test
    public void testGetBytesUtf16Be_empty() {
        byte[] result = StringUtils.getBytesUtf16Be("");
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    public void testGetBytesUtf16Be_normal() {
        String input = "test";
        byte[] expected = input.getBytes(Charset.forName("UTF-16BE"));
        byte[] result = StringUtils.getBytesUtf16Be(input);
        assertArrayEquals(expected, result);
    }

    @Test
    public void testGetBytesUtf16Be_specialChars() {
        String input = "éçüñ";
        byte[] expected = input.getBytes(Charset.forName("UTF-16BE"));
        byte[] result = StringUtils.getBytesUtf16Be(input);
        assertArrayEquals(expected, result);
    }

    // ------------------- getBytesUtf16Le -------------------

    @Test
    public void testGetBytesUtf16Le_null() {
        assertNull(StringUtils.getBytesUtf16Le(null));
    }

    @Test
    public void testGetBytesUtf16Le_empty() {
        byte[] result = StringUtils.getBytesUtf16Le("");
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    public void testGetBytesUtf16Le_normal() {
        String input = "test";
        byte[] expected = input.getBytes(Charset.forName("UTF-16LE"));
        byte[] result = StringUtils.getBytesUtf16Le(input);
        assertArrayEquals(expected, result);
    }

    @Test
    public void testGetBytesUtf16Le_specialChars() {
        String input = "éçüñ";
        byte[] expected = input.getBytes(Charset.forName("UTF-16LE"));
        byte[] result = StringUtils.getBytesUtf16Le(input);
        assertArrayEquals(expected, result);
    }

    // ------------------- getBytesUnchecked -------------------

    @Test
    public void testGetBytesUnchecked_null() {
        assertNull(StringUtils.getBytesUnchecked(null, "UTF-8"));
    }

    @Test
    public void testGetBytesUnchecked_valid() {
        String input = "test";
        byte[] expected = input.getBytes(Charset.forName("UTF-8"));
        byte[] result = StringUtils.getBytesUnchecked(input, "UTF-8"));
        assertArrayEquals(expected, result);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetBytesUnchecked_invalidCharset() {
        StringUtils.getBytesUnchecked("test", "INVALID_CHARSET"));
    }

    @Test
    public void testGetBytesUnchecked_invalidCharsetExceptionMessage() {
        try {
            StringUtils.getBytesUnchecked("test", "INVALID");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("INVALID"));
        }
    }

    // ------------------- newString -------------------

    @Test
    public void testNewString_bytesNull() {
        assertNull(StringUtils.newString(null, "UTF-8"));
    }

    @Test
    public void testNewString_emptyBytes() {
        String result = StringUtils.newString(new byte[0], "UTF-8"));
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testNewString_valid() {
        byte[] bytes = "test".getBytes(Charset.forName("UTF-8"));
        String result = StringUtils.newString(bytes, "UTF-8"));
        assertEquals("test", result);
    }

    @Test(expected = IllegalStateException.class)
    public void testNewString_invalidCharset() {
        StringUtils.newString(new byte[0], "INVALID");
    }

    @Test
    public void testNewString_invalidCharsetExceptionMessage() {
        try {
            StringUtils.newString(new byte[0], "INVALID");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("INVALID"));
        }
    }

    // ------------------- newStringIso8859_1 -------------------

    // According to Javadoc, null input should return null, but actual code throws NPE.
    // We test the actual behavior.
    @Test(expected = NullPointerException.class)
    public void testNewStringIso8859_1_null() {
        StringUtils.newStringIso8859_1(null);
    }

    @Test
    public void testNewStringIso8859_1_empty() {
        String result = StringUtils.newStringIso8859_1(new byte[0]);
        assertNotNull(result);
        assertEquals("", result);
    }

    @Test
    public void testNewStringIso8859_1_valid() {
        String original = "test";
        byte[] bytes = original.getBytes(Charset.forName("ISO-8859-1"));
        String result = StringUtils.newStringIso8859_1(bytes);
        assertEquals(original, result);
    }

    @Test
    public void testNewStringIso8859_1_specialChars() {
        String original = "éçüñ";
        byte[] bytes = original.getBytes(Charset.forName("ISO-8859-1"));
        String result = StringUtils.newStringIso8859_1(bytes);
        assertEquals(original, result);
    }

    // ------------------- newStringUsAscii -------------------

    @Test(expected = NullPointerException.class)
    public void testNewStringUsAscii_null() {
        StringUtils.newStringUsAscii(null);
    }

    @Test
    public void testNewStringUsAscii_empty() {
        String result = StringUtils.newStringUsAscii(new byte[0]);
        assertEquals("", result);
    }

    @Test
    public void testNewStringUsAscii_valid() {
        String original = "test";
        byte[] bytes = original.getBytes(Charset.forName("US-ASCII"));
        String result = StringUtils.newStringUsAscii(bytes);
        assertEquals(original, result);
    }

    @Test
    public void testNewStringUsAscii_specialChars() {
        // ASCII will replace with ?
        String original = "éçüñ";
        byte[] bytes = original.getBytes(Charset.forName("US-ASCII"));
        String result = StringUtils.newStringUsAscii(bytes);
        assertEquals(new String(bytes, Charset.forName("US-ASCII")));
    }

    // ------------------- newStringUtf8 -------------------

    @Test
    public void testNewStringUtf8_null() {
        assertNull(StringUtils.newStringUtf8(null));
    }

    @Test
    public void testNewStringUtf8_empty() {
        String result = StringUtils.newStringUtf8(new byte[0]);
        assertEquals("", result);
    }

    @Test
    public void testNewStringUtf8_valid() {
        String original = "test";
        byte[] bytes = original.getBytes(Charset.forName("UTF-8"));
        String result = StringUtils.newStringUtf8(bytes);
        assertEquals(original, result);
    }

    @Test
    public void testNewStringUtf8_specialChars() {
        String original = "éçüñ";
        byte[] bytes = original.getBytes(Charset.forName("UTF-8"));
        String result = StringUtils.newStringUtf8(bytes);
        assertEquals(original, result);
    }

    // ------------------- newStringUtf16 -------------------

    @Test
    public void testNewStringUtf16_null() {
        assertNull(StringUtils.newStringUtf16(null));
    }

    @Test
    public void testNewStringUtf16_empty() {
        String result = StringUtils.newStringUtf16(new byte[0]);
        assertEquals("", result);
    }

    @Test
    public void testNewStringUtf16_valid() {
        String original = "test";
        byte[] bytes = original.getBytes(Charset.forName("UTF-16"));
        String result = StringUtils.newStringUtf16(bytes);
        assertEquals(original, result);
    }

    @Test
    public void testNewStringUtf16_specialChars() {
        String original = "éçüñ";
        byte[] bytes = original.getBytes(Charset.forName("UTF-16"));
        String result = StringUtils.newStringUtf16(bytes);
        assertEquals(original, result);
    }

    // ------------------- newStringUtf16Be -------------------

    @Test
    public void testNewStringUtf16Be_null() {
        assertNull(StringUtils.newStringUtf16Be(null));
    }

    @Test
    public void testNewStringUtf16Be_empty() {
        String result = StringUtils.newStringUtf16Be(new byte[0]);
        assertEquals("", result);
    }

    @Test
    public void testNewStringUtf16Be_valid() {
        String original = "test";
        byte[] bytes = original.getBytes(Charset.forName("UTF-16BE"));
        String result = StringUtils.newStringUtf16Be(bytes);
        assertEquals(original, result);
    }

    @Test
    public void testNewStringUtf16Be_specialChars() {
        String original = "éçüñ";
        byte[] bytes = original.getBytes(Charset.forName("UTF-16BE"));
        String result = StringUtils.newStringUtf16Be(bytes);
        assertEquals(original, result);
    }

    // ------------------- newStringUtf16Le -------------------

    @Test
    public void testNewStringUtf16Le_null() {
        assertNull(StringUtils.newStringUtf16Le(null));
    }

    @Test
    public void testNewStringUtf16Le_empty() {
        String result = StringUtils.newStringUtf16Le(new byte[0]);
        assertEquals("", result);
    }

    @Test
    public void testNewStringUtf16Le_valid() {
        String original = "test";
        byte[] bytes = original.getBytes(Charset.forName("UTF-16LE"));
        String result = StringUtils.newStringUtf16Le(bytes);
        assertEquals(original, result);
    }

    @Test
    public void testNewStringUtf16Le_specialChars() {
        String original = "éçüñ";
        byte[] bytes = original.getBytes(Charset.forName("UTF-16LE"));
        String result = StringUtils.newStringUtf16Le(bytes);
        assertEquals(original, result);
    }

    // ------------------- Roundtrip tests -------------------

    @Test
    public void testRoundtripUtf8() {
        String original = "test string";
        byte[] bytes = StringUtils.getBytesUtf8(original);
        String decoded = StringUtils.newStringUtf8(bytes);
        assertEquals(original, decoded);
    }

    @Test
    public void testRoundtripIso8859_1() {
        String original = "test string";
        byte[] bytes = StringUtils.getBytesIso8859_1(original);
        String decoded = StringUtils.newStringIso8859_1(bytes);
        assertEquals(original, decoded);
    }

    @Test
    public void testRoundtripUtf16() {
        String original = "test string";
        byte[] bytes = StringUtils.getBytesUtf16(original);
        String decoded = StringUtils.newStringUtf16(bytes);
        assertEquals(original, decoded);
    }

    @Test
    public void testRoundtripUtf16Be() {
        String original = "test string";
        byte[] bytes = StringUtils.getBytesUtf16Be(original);
        String decoded = StringUtils.newStringUtf16Be(bytes);
        assertEquals(original, decoded);
    }

    @Test
    public void testRoundtripUtf16Le() {
        String original = "test string";
        byte[] bytes = StringUtils.getBytesUtf16Le(original);
        String decoded = StringUtils.newStringUtf16Le(bytes);
        assertEquals(original, decoded);
    }

    // Note: newStringUsAscii might not be a perfect roundtrip for non-ASCII, but we test for ASCII-safe strings.
    @Test
    public void testRoundtripUsAscii() {
        String original = "test";
        byte[] bytes = StringUtils.getBytesUsAscii(original);
        String decoded = StringUtils.newStringUsAscii(bytes);
        assertEquals(original, decoded);
    }
}
