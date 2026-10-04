package org.apache.commons.codec.binary;

import org.apache.commons.codec.CharEncoding;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class StringUtilsTest {

    private static final String TEST_STRING = "Hello World! \u00e9\u00f1\u4e16\u754c";
    private static final String ASCII_STRING = "Hello World!";

    @Test
    public void testConstructor() {
        Assert.assertNotNull(new StringUtils());
    }

    @Test
    public void testEquals() {
        Assert.assertTrue(StringUtils.equals(null, null));
        Assert.assertFalse(StringUtils.equals(null, "abc"));
        Assert.assertFalse(StringUtils.equals("abc", null));
        Assert.assertTrue(StringUtils.equals("abc", "abc"));
        Assert.assertFalse(StringUtils.equals("abc", "ABC"));
        Assert.assertFalse(StringUtils.equals("abc", "abcd"));
        Assert.assertFalse(StringUtils.equals("abcd", "abc"));

        final StringBuilder sb1 = new StringBuilder("abc");
        final StringBuilder sb2 = new StringBuilder("abc");
        final StringBuilder sb3 = new StringBuilder("abd");
        final StringBuilder sb4 = new StringBuilder("abcd");

        Assert.assertTrue(StringUtils.equals(sb1, sb1));
        Assert.assertTrue(StringUtils.equals(sb1, sb2));
        Assert.assertTrue(StringUtils.equals(sb1, "abc"));
        Assert.assertTrue(StringUtils.equals("abc", sb1));
        Assert.assertFalse(StringUtils.equals(sb1, sb3));
        Assert.assertFalse(StringUtils.equals(sb1, sb4));
        Assert.assertFalse(StringUtils.equals(sb4, sb1));
        Assert.assertFalse(StringUtils.equals(sb1, "ABC"));
    }

    @Test
    public void testGetByteBufferUtf8() {
        Assert.assertNull(StringUtils.getByteBufferUtf8(null));

        final ByteBuffer buffer = StringUtils.getByteBufferUtf8(TEST_STRING);
        Assert.assertNotNull(buffer);
        final byte[] expectedBytes = TEST_STRING.getBytes(StandardCharsets.UTF_8);
        final byte[] actualBytes = new byte[buffer.remaining()];
        buffer.get(actualBytes);
        Assert.assertArrayEquals(expectedBytes, actualBytes);
    }

    @Test
    public void testGetBytesIso8859_1() {
        Assert.assertNull(StringUtils.getBytesIso8859_1(null));

        final byte[] expected = ASCII_STRING.getBytes(StandardCharsets.ISO_8859_1);
        final byte[] actual = StringUtils.getBytesIso8859_1(ASCII_STRING);
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void testGetBytesUsAscii() {
        Assert.assertNull(StringUtils.getBytesUsAscii(null));

        final byte[] expected = ASCII_STRING.getBytes(StandardCharsets.US_ASCII);
        final byte[] actual = StringUtils.getBytesUsAscii(ASCII_STRING);
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void testGetBytesUtf16() {
        Assert.assertNull(StringUtils.getBytesUtf16(null));

        final byte[] expected = TEST_STRING.getBytes(StandardCharsets.UTF_16);
        final byte[] actual = StringUtils.getBytesUtf16(TEST_STRING);
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void testGetBytesUtf16Be() {
        Assert.assertNull(StringUtils.getBytesUtf16Be(null));

        final byte[] expected = TEST_STRING.getBytes(StandardCharsets.UTF_16BE);
        final byte[] actual = StringUtils.getBytesUtf16Be(TEST_STRING);
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void testGetBytesUtf16Le() {
        Assert.assertNull(StringUtils.getBytesUtf16Le(null));

        final byte[] expected = TEST_STRING.getBytes(StandardCharsets.UTF_16LE);
        final byte[] actual = StringUtils.getBytesUtf16Le(TEST_STRING);
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void testGetBytesUtf8() {
        Assert.assertNull(StringUtils.getBytesUtf8(null));

        final byte[] expected = TEST_STRING.getBytes(StandardCharsets.UTF_8);
        final byte[] actual = StringUtils.getBytesUtf8(TEST_STRING);
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void testGetBytesUnchecked() {
        Assert.assertNull(StringUtils.getBytesUnchecked(null, CharEncoding.UTF_8));

        final byte[] expected = TEST_STRING.getBytes(StandardCharsets.UTF_8);
        final byte[] actual = StringUtils.getBytesUnchecked(TEST_STRING, CharEncoding.UTF_8);
        Assert.assertArrayEquals(expected, actual);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetBytesUncheckedUnsupportedCharset() {
        StringUtils.getBytesUnchecked(TEST_STRING, "INVALID_CHARSET_NAME_XYZ");
    }

    @Test
    public void testNewString() {
        Assert.assertNull(StringUtils.newString(null, CharEncoding.UTF_8));

        final byte[] bytes = TEST_STRING.getBytes(StandardCharsets.UTF_8);
        final String actual = StringUtils.newString(bytes, CharEncoding.UTF_8);
        Assert.assertEquals(TEST_STRING, actual);
    }

    @Test(expected = IllegalStateException.class)
    public void testNewStringUnsupportedCharset() {
        final byte[] bytes = TEST_STRING.getBytes(StandardCharsets.UTF_8);
        StringUtils.newString(bytes, "INVALID_CHARSET_NAME_XYZ");
    }

    @Test
    public void testNewStringIso8859_1() {
        Assert.assertNull(StringUtils.newStringIso8859_1(null));

        final byte[] bytes = ASCII_STRING.getBytes(StandardCharsets.ISO_8859_1);
        final String actual = StringUtils.newStringIso8859_1(bytes);
        Assert.assertEquals(ASCII_STRING, actual);
    }

    @Test
    public void testNewStringUsAscii() {
        Assert.assertNull(StringUtils.newStringUsAscii(null));

        final byte[] bytes = ASCII_STRING.getBytes(StandardCharsets.US_ASCII);
        final String actual = StringUtils.newStringUsAscii(bytes);
        Assert.assertEquals(ASCII_STRING, actual);
    }

    @Test
    public void testNewStringUtf16() {
        Assert.assertNull(StringUtils.newStringUtf16(null));

        final byte[] bytes = TEST_STRING.getBytes(StandardCharsets.UTF_16);
        final String actual = StringUtils.newStringUtf16(bytes);
        Assert.assertEquals(TEST_STRING, actual);
    }

    @Test
    public void testNewStringUtf16Be() {
        Assert.assertNull(StringUtils.newStringUtf16Be(null));

        final byte[] bytes = TEST_STRING.getBytes(StandardCharsets.UTF_16BE);
        final String actual = StringUtils.newStringUtf16Be(bytes);
        Assert.assertEquals(TEST_STRING, actual);
    }

    @Test
    public void testNewStringUtf16Le() {
        Assert.assertNull(StringUtils.newStringUtf16Le(null));

        final byte[] bytes = TEST_STRING.getBytes(StandardCharsets.UTF_16LE);
        final String actual = StringUtils.newStringUtf16Le(bytes);
        Assert.assertEquals(TEST_STRING, actual);
    }

    @Test
    public void testNewStringUtf8() {
        Assert.assertNull(StringUtils.newStringUtf8(null));

        final byte[] bytes = TEST_STRING.getBytes(StandardCharsets.UTF_8);
        final String actual = StringUtils.newStringUtf8(bytes);
        Assert.assertEquals(TEST_STRING, actual);
    }
}
