package org.apache.commons.codec.binary;

import org.apache.commons.codec.CharEncoding;
import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

/**
 * High-coverage unit tests for {@link StringUtils}.
 */
public class StringUtilsTest {

    private static final String TEST_STRING = "Hello, World! \u00e9\u00f1\u4e16\u754c";
    private static final String ASCII_STRING = "Hello, World!";

    @Test
    public void testConstructor() {
        // StringUtils has an implicit public default constructor
        final StringUtils instance = new StringUtils();
        Assert.assertNotNull(instance);
    }

    @Test
    public void testGetBytesIso8859_1() throws UnsupportedEncodingException {
        Assert.assertNull(StringUtils.getBytesIso8859_1(null));
        final byte[] expected = ASCII_STRING.getBytes("ISO-8859-1");
        final byte[] actual = StringUtils.getBytesIso8859_1(ASCII_STRING);
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void testGetBytesUsAscii() throws UnsupportedEncodingException {
        Assert.assertNull(StringUtils.getBytesUsAscii(null));
        final byte[] expected = ASCII_STRING.getBytes("US-ASCII");
        final byte[] actual = StringUtils.getBytesUsAscii(ASCII_STRING);
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void testGetBytesUtf8() throws UnsupportedEncodingException {
        Assert.assertNull(StringUtils.getBytesUtf8(null));
        final byte[] expected = TEST_STRING.getBytes("UTF-8");
        final byte[] actual = StringUtils.getBytesUtf8(TEST_STRING);
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void testGetBytesUtf16() throws UnsupportedEncodingException {
        Assert.assertNull(StringUtils.getBytesUtf16(null));
        final byte[] expected = TEST_STRING.getBytes("UTF-16");
        final byte[] actual = StringUtils.getBytesUtf16(TEST_STRING);
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void testGetBytesUtf16Be() throws UnsupportedEncodingException {
        Assert.assertNull(StringUtils.getBytesUtf16Be(null));
        final byte[] expected = TEST_STRING.getBytes("UTF-16BE");
        final byte[] actual = StringUtils.getBytesUtf16Be(TEST_STRING);
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void testGetBytesUtf16Le() throws UnsupportedEncodingException {
        Assert.assertNull(StringUtils.getBytesUtf16Le(null));
        final byte[] expected = TEST_STRING.getBytes("UTF-16LE");
        final byte[] actual = StringUtils.getBytesUtf16Le(TEST_STRING);
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void testGetBytesUnchecked() throws UnsupportedEncodingException {
        Assert.assertNull(StringUtils.getBytesUnchecked(null, "UTF-8"));
        Assert.assertNull(StringUtils.getBytesUnchecked(null, "INVALID_CHARSET_NAME"));

        final byte[] expected = TEST_STRING.getBytes("UTF-8");
        final byte[] actual = StringUtils.getBytesUnchecked(TEST_STRING, CharEncoding.UTF_8);
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void testGetBytesUncheckedInvalidCharset() {
        try {
            StringUtils.getBytesUnchecked(TEST_STRING, "INVALID_CHARSET_NAME");
            Assert.fail("Expected IllegalStateException for invalid charset");
        } catch (final IllegalStateException e) {
            Assert.assertTrue(e.getCause() instanceof UnsupportedEncodingException);
            Assert.assertTrue(e.getMessage().contains("INVALID_CHARSET_NAME"));
        }
    }

    @Test
    public void testNewStringIso8859_1() throws UnsupportedEncodingException {
        final byte[] bytes = ASCII_STRING.getBytes("ISO-8859-1");
        Assert.assertEquals(ASCII_STRING, StringUtils.newStringIso8859_1(bytes));
    }

    @Test
    public void testNewStringUsAscii() throws UnsupportedEncodingException {
        final byte[] bytes = ASCII_STRING.getBytes("US-ASCII");
        Assert.assertEquals(ASCII_STRING, StringUtils.newStringUsAscii(bytes));
    }

    @Test
    public void testNewStringUtf8() throws UnsupportedEncodingException {
        Assert.assertNull(StringUtils.newStringUtf8(null));
        final byte[] bytes = TEST_STRING.getBytes("UTF-8");
        Assert.assertEquals(TEST_STRING, StringUtils.newStringUtf8(bytes));
    }

    @Test
    public void testNewStringUtf16() throws UnsupportedEncodingException {
        final byte[] bytes = TEST_STRING.getBytes("UTF-16");
        Assert.assertEquals(TEST_STRING, StringUtils.newStringUtf16(bytes));
    }

    @Test
    public void testNewStringUtf16Be() throws UnsupportedEncodingException {
        final byte[] bytes = TEST_STRING.getBytes("UTF-16BE");
        Assert.assertEquals(TEST_STRING, StringUtils.newStringUtf16Be(bytes));
    }

    @Test
    public void testNewStringUtf16Le() throws UnsupportedEncodingException {
        final byte[] bytes = TEST_STRING.getBytes("UTF-16LE");
        Assert.assertEquals(TEST_STRING, StringUtils.newStringUtf16Le(bytes));
    }

    @Test
    public void testNewStringUnchecked() throws UnsupportedEncodingException {
        Assert.assertNull(StringUtils.newString(null, "UTF-8"));
        Assert.assertNull(StringUtils.newString(null, "INVALID_CHARSET_NAME"));

        final byte[] bytes = TEST_STRING.getBytes("UTF-8");
        Assert.assertEquals(TEST_STRING, StringUtils.newString(bytes, CharEncoding.UTF_8));
    }

    @Test
    public void testNewStringUncheckedInvalidCharset() {
        final byte[] bytes = new byte[]{65, 66, 67};
        try {
            StringUtils.newString(bytes, "INVALID_CHARSET_NAME");
            Assert.fail("Expected IllegalStateException for invalid charset");
        } catch (final IllegalStateException e) {
            Assert.assertTrue(e.getCause() instanceof UnsupportedEncodingException);
            Assert.assertTrue(e.getMessage().contains("INVALID_CHARSET_NAME"));
        }
    }

    @Test
    public void testRoundTrips() {
        Assert.assertEquals(TEST_STRING, StringUtils.newStringUtf8(StringUtils.getBytesUtf8(TEST_STRING)));
        Assert.assertEquals(TEST_STRING, StringUtils.newStringUtf16(StringUtils.getBytesUtf16(TEST_STRING)));
        Assert.assertEquals(TEST_STRING, StringUtils.newStringUtf16Be(StringUtils.getBytesUtf16Be(TEST_STRING)));
        Assert.assertEquals(TEST_STRING, StringUtils.newStringUtf16Le(StringUtils.getBytesUtf16Le(TEST_STRING)));

        Assert.assertEquals(ASCII_STRING, StringUtils.newStringUsAscii(StringUtils.getBytesUsAscii(ASCII_STRING)));
        Assert.assertEquals(ASCII_STRING, StringUtils.newStringIso8859_1(StringUtils.getBytesIso8859_1(ASCII_STRING)));

        Assert.assertEquals(TEST_STRING, StringUtils.newString(StringUtils.getBytesUnchecked(TEST_STRING, "UTF-8"), "UTF-8"));
    }

    @Test
    public void testEmptyStrings() {
        Assert.assertArrayEquals(new byte[0], StringUtils.getBytesIso8859_1(""));
        Assert.assertArrayEquals(new byte[0], StringUtils.getBytesUsAscii(""));
        Assert.assertArrayEquals(new byte[0], StringUtils.getBytesUtf8(""));
        Assert.assertArrayEquals(new byte[0], StringUtils.getBytesUtf16Be(""));
        Assert.assertArrayEquals(new byte[0], StringUtils.getBytesUtf16Le(""));
        Assert.assertArrayEquals(new byte[0], StringUtils.getBytesUnchecked("", "UTF-8"));

        Assert.assertEquals("", StringUtils.newStringIso8859_1(new byte[0]));
        Assert.assertEquals("", StringUtils.newStringUsAscii(new byte[0]));
        Assert.assertEquals("", StringUtils.newStringUtf8(new byte[0]));
        Assert.assertEquals("", StringUtils.newStringUtf16(new byte[0]));
        Assert.assertEquals("", StringUtils.newStringUtf16Be(new byte[0]));
        Assert.assertEquals("", StringUtils.newStringUtf16Le(new byte[0]));
        Assert.assertEquals("", StringUtils.newString(new byte[0], "UTF-8"));
    }
}
