package org.apache.commons.codec.binary;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.apache.commons.codec.CharEncoding;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests {@link StringUtils}.
 */
public class StringUtilsTest {

    private static final String TEST_STRING = "Hello World! \u00e9\u00e8\u00e0\u00f9";
    private static final String ASCII_STRING = "Hello World!";
    private static final String INVALID_CHARSET = "INVALID_CHARSET_NAME_12345";

    @Test
    public void testConstructor() {
        final StringUtils instance = new StringUtils();
        Assert.assertNotNull(instance);
    }

    @Test
    public void testEqualsBothNull() {
        Assert.assertTrue(StringUtils.equals(null, null));
    }

    @Test
    public void testEqualsFirstNull() {
        Assert.assertFalse(StringUtils.equals(null, "abc"));
        Assert.assertFalse(StringUtils.equals(null, new StringBuilder("abc")));
    }

    @Test
    public void testEqualsSecondNull() {
        Assert.assertFalse(StringUtils.equals("abc", null));
        Assert.assertFalse(StringUtils.equals(new StringBuilder("abc"), null));
    }

    @Test
    public void testEqualsSameInstance() {
        final String str = "abc";
        Assert.assertTrue(StringUtils.equals(str, str));

        final StringBuilder sb = new StringBuilder("abc");
        Assert.assertTrue(StringUtils.equals(sb, sb));
    }

    @Test
    public void testEqualsStrings() {
        Assert.assertTrue(StringUtils.equals("abc", "abc"));
        Assert.assertFalse(StringUtils.equals("abc", "ABC"));
        Assert.assertFalse(StringUtils.equals("abc", "def"));
        Assert.assertFalse(StringUtils.equals("abc", "abcd"));
        Assert.assertFalse(StringUtils.equals("abcd", "abc"));
    }

    @Test
    public void testEqualsCharSequences() {
        final StringBuilder sb1 = new StringBuilder("abc");
        final StringBuilder sb2 = new StringBuilder("abc");
        final StringBuilder sb3 = new StringBuilder("ABC");
        final StringBuilder sbLonger = new StringBuilder("abcd");
        final StringBuilder sbShorter = new StringBuilder("ab");
        final StringBuffer sbuf = new StringBuffer("abc");

        // StringBuilder vs StringBuilder
        Assert.assertTrue(StringUtils.equals(sb1, sb2));
        Assert.assertFalse(StringUtils.equals(sb1, sb3));
        Assert.assertFalse(StringUtils.equals(sb1, sbLonger));
        Assert.assertFalse(StringUtils.equals(sbLonger, sb1));
        Assert.assertFalse(StringUtils.equals(sb1, sbShorter));

        // String vs StringBuilder
        Assert.assertTrue(StringUtils.equals("abc", sb1));
        Assert.assertTrue(StringUtils.equals(sb1, "abc"));
        Assert.assertFalse(StringUtils.equals("ABC", sb1));
        Assert.assertFalse(StringUtils.equals(sb1, "ABC"));

        // StringBuffer vs StringBuilder
        Assert.assertTrue(StringUtils.equals(sb1, sbuf));
        Assert.assertTrue(StringUtils.equals(sbuf, sb1));
    }

    @Test
    public void testGetByteBufferUtf8() {
        Assert.assertNull(StringUtils.getByteBufferUtf8(null));

        final ByteBuffer buffer = StringUtils.getByteBufferUtf8(TEST_STRING);
        Assert.assertNotNull(buffer);
        final byte[] expected = TEST_STRING.getBytes(StandardCharsets.UTF_8);
        final byte[] actual = new byte[buffer.remaining()];
        buffer.get(actual);
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void testGetBytesIso8859_1() {
        Assert.assertNull(StringUtils.getBytesIso8859_1(null));

        final byte[] bytes = StringUtils.getBytesIso8859_1(ASCII_STRING);
        Assert.assertArrayEquals(ASCII_STRING.getBytes(StandardCharsets.ISO_8859_1), bytes);
    }

    @Test
    public void testGetBytesUsAscii() {
        Assert.assertNull(StringUtils.getBytesUsAscii(null));

        final byte[] bytes = StringUtils.getBytesUsAscii(ASCII_STRING);
        Assert.assertArrayEquals(ASCII_STRING.getBytes(StandardCharsets.US_ASCII), bytes);
    }

    @Test
    public void testGetBytesUtf16() {
        Assert.assertNull(StringUtils.getBytesUtf16(null));

        final byte[] bytes = StringUtils.getBytesUtf16(TEST_STRING);
        Assert.assertArrayEquals(TEST_STRING.getBytes(StandardCharsets.UTF_16), bytes);
    }

    @Test
    public void testGetBytesUtf16Be() {
        Assert.assertNull(StringUtils.getBytesUtf16Be(null));

        final byte[] bytes = StringUtils.getBytesUtf16Be(TEST_STRING);
        Assert.assertArrayEquals(TEST_STRING.getBytes(StandardCharsets.UTF_16BE), bytes);
    }

    @Test
    public void testGetBytesUtf16Le() {
        Assert.assertNull(StringUtils.getBytesUtf16Le(null));

        final byte[] bytes = StringUtils.getBytesUtf16Le(TEST_STRING);
        Assert.assertArrayEquals(TEST_STRING.getBytes(StandardCharsets.UTF_16LE), bytes);
    }

    @Test
    public void testGetBytesUtf8() {
        Assert.assertNull(StringUtils.getBytesUtf8(null));

        final byte[] bytes = StringUtils.getBytesUtf8(TEST_STRING);
        Assert.assertArrayEquals(TEST_STRING.getBytes(StandardCharsets.UTF_8), bytes);
    }

    @Test
    public void testGetBytesUnchecked() {
        Assert.assertNull(StringUtils.getBytesUnchecked(null, CharEncoding.UTF_8));

        final byte[] bytes = StringUtils.getBytesUnchecked(TEST_STRING, CharEncoding.UTF_8);
        Assert.assertArrayEquals(TEST_STRING.getBytes(StandardCharsets.UTF_8), bytes);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetBytesUncheckedUnsupportedEncoding() {
        StringUtils.getBytesUnchecked(TEST_STRING, INVALID_CHARSET);
    }

    @Test
    public void testNewStringNull() {
        Assert.assertNull(StringUtils.newString(null, CharEncoding.UTF_8));
        Assert.assertNull(StringUtils.newStringIso8859_1(null));
        Assert.assertNull(StringUtils.newStringUsAscii(null));
        Assert.assertNull(StringUtils.newStringUtf16(null));
        Assert.assertNull(StringUtils.newStringUtf16Be(null));
        Assert.assertNull(StringUtils.newStringUtf16Le(null));
        Assert.assertNull(StringUtils.newStringUtf8(null));
    }

    @Test
    public void testNewStringIso8859_1() {
        final byte[] bytes = ASCII_STRING.getBytes(StandardCharsets.ISO_8859_1);
        Assert.assertEquals(ASCII_STRING, StringUtils.newStringIso8859_1(bytes));
    }

    @Test
    public void testNewStringUsAscii() {
        final byte[] bytes = ASCII_STRING.getBytes(StandardCharsets.US_ASCII);
        Assert.assertEquals(ASCII_STRING, StringUtils.newStringUsAscii(bytes));
    }

    @Test
    public void testNewStringUtf16() {
        final byte[] bytes = TEST_STRING.getBytes(StandardCharsets.UTF_16);
        Assert.assertEquals(TEST_STRING, StringUtils.newStringUtf16(bytes));
    }

    @Test
    public void testNewStringUtf16Be() {
        final byte[] bytes = TEST_STRING.getBytes(StandardCharsets.UTF_16BE);
        Assert.assertEquals(TEST_STRING, StringUtils.newStringUtf16Be(bytes));
    }

    @Test
    public void testNewStringUtf16Le() {
        final byte[] bytes = TEST_STRING.getBytes(StandardCharsets.UTF_16LE);
        Assert.assertEquals(TEST_STRING, StringUtils.newStringUtf16Le(bytes));
    }

    @Test
    public void testNewStringUtf8() {
        final byte[] bytes = TEST_STRING.getBytes(StandardCharsets.UTF_8);
        Assert.assertEquals(TEST_STRING, StringUtils.newStringUtf8(bytes));
    }

    @Test
    public void testNewStringCustomCharset() {
        final byte[] bytes = TEST_STRING.getBytes(StandardCharsets.UTF_8);
        Assert.assertEquals(TEST_STRING, StringUtils.newString(bytes, CharEncoding.UTF_8));
    }

    @Test(expected = IllegalStateException.class)
    public void testNewStringUnsupportedEncoding() {
        final byte[] bytes = new byte[] { 65, 66, 67 };
        StringUtils.newString(bytes, INVALID_CHARSET);
    }

    @Test
    public void testRoundTrips() {
        // UTF-8
        Assert.assertEquals(TEST_STRING, StringUtils.newStringUtf8(StringUtils.getBytesUtf8(TEST_STRING)));

        // UTF-16
        Assert.assertEquals(TEST_STRING, StringUtils.newStringUtf16(StringUtils.getBytesUtf16(TEST_STRING)));

        // UTF-16BE
        Assert.assertEquals(TEST_STRING, StringUtils.newStringUtf16Be(StringUtils.getBytesUtf16Be(TEST_STRING)));

        // UTF-16LE
        Assert.assertEquals(TEST_STRING, StringUtils.newStringUtf16Le(StringUtils.getBytesUtf16Le(TEST_STRING)));

        // ISO-8859-1
        Assert.assertEquals(ASCII_STRING, StringUtils.newStringIso8859_1(StringUtils.getBytesIso8859_1(ASCII_STRING)));

        // US-ASCII
        Assert.assertEquals(ASCII_STRING, StringUtils.newStringUsAscii(StringUtils.getBytesUsAscii(ASCII_STRING)));

        // Empty string round-trips
        Assert.assertEquals("", StringUtils.newStringUtf8(StringUtils.getBytesUtf8("")));
        Assert.assertEquals("", StringUtils.newStringIso8859_1(StringUtils.getBytesIso8859_1("")));
        Assert.assertEquals("", StringUtils.newStringUsAscii(StringUtils.getBytesUsAscii("")));
        Assert.assertEquals("", StringUtils.newStringUtf16(StringUtils.getBytesUtf16("")));
        Assert.assertEquals("", StringUtils.newStringUtf16Be(StringUtils.getBytesUtf16Be("")));
        Assert.assertEquals("", StringUtils.newStringUtf16Le(StringUtils.getBytesUtf16Le("")));
    }
}
