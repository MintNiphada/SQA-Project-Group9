package org.jsoup.helper;

import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StringUtilTest {

    @Test
    public void testJoinCollection() {
        List<String> list = Arrays.asList("a", "b", "c");
        Assert.assertEquals("a, b, c", StringUtil.join(list, ", "));
        Assert.assertEquals("", StringUtil.join(Collections.emptyList(), ", "));
        Assert.assertEquals("one", StringUtil.join(Collections.singletonList("one"), ", "));
    }

    @Test
    public void testJoinIterator() {
        List<String> list = new ArrayList<String>();
        list.add("x");
        list.add("y");
        Assert.assertEquals("x-y", StringUtil.join(list.iterator(), "-"));
        Assert.assertEquals("", StringUtil.join(Collections.emptyList().iterator(), "-"));
    }

    @Test
    public void testJoinArray() {
        String[] arr = new String[]{"one", "two", "three"};
        Assert.assertEquals("one|two|three", StringUtil.join(arr, "|"));
        Assert.assertEquals("", StringUtil.join(new String[0], "|"));
        Assert.assertEquals("solo", StringUtil.join(new String[]{"solo"}, "|"));
    }

    @Test
    public void testPadding() {
        Assert.assertEquals("", StringUtil.padding(0));
        Assert.assertEquals(" ", StringUtil.padding(1));
        Assert.assertEquals("  ", StringUtil.padding(2));
        Assert.assertEquals("                    ", StringUtil.padding(20));
        Assert.assertEquals("                     ", StringUtil.padding(21));
        Assert.assertEquals("                      ", StringUtil.padding(22));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPaddingNegative() {
        StringUtil.padding(-1);
    }

    @Test
    public void testIsBlank() {
        Assert.assertTrue(StringUtil.isBlank(null));
        Assert.assertTrue(StringUtil.isBlank(""));
        Assert.assertTrue(StringUtil.isBlank("   "));
        Assert.assertTrue(StringUtil.isBlank("\t\r\n\f "));
        Assert.assertFalse(StringUtil.isBlank("a"));
        Assert.assertFalse(StringUtil.isBlank("   a   "));
        Assert.assertFalse(StringUtil.isBlank(" \u00a0 "));
    }

    @Test
    public void testIsNumeric() {
        Assert.assertFalse(StringUtil.isNumeric(null));
        Assert.assertFalse(StringUtil.isNumeric(""));
        Assert.assertTrue(StringUtil.isNumeric("12345"));
        Assert.assertTrue(StringUtil.isNumeric("0"));
        Assert.assertFalse(StringUtil.isNumeric("123a45"));
        Assert.assertFalse(StringUtil.isNumeric(" 123 "));
        Assert.assertFalse(StringUtil.isNumeric("-123"));
    }

    @Test
    public void testIsWhitespace() {
        Assert.assertTrue(StringUtil.isWhitespace(' '));
        Assert.assertTrue(StringUtil.isWhitespace('\t'));
        Assert.assertTrue(StringUtil.isWhitespace('\n'));
        Assert.assertTrue(StringUtil.isWhitespace('\f'));
        Assert.assertTrue(StringUtil.isWhitespace('\r'));
        Assert.assertFalse(StringUtil.isWhitespace(160));
        Assert.assertFalse(StringUtil.isWhitespace('a'));
    }

    @Test
    public void testIsActuallyWhitespace() {
        Assert.assertTrue(StringUtil.isActuallyWhitespace(' '));
        Assert.assertTrue(StringUtil.isActuallyWhitespace('\t'));
        Assert.assertTrue(StringUtil.isActuallyWhitespace('\n'));
        Assert.assertTrue(StringUtil.isActuallyWhitespace('\f'));
        Assert.assertTrue(StringUtil.isActuallyWhitespace('\r'));
        Assert.assertTrue(StringUtil.isActuallyWhitespace(160));
        Assert.assertFalse(StringUtil.isActuallyWhitespace('a'));
    }

    @Test
    public void testNormaliseWhitespace() {
        Assert.assertEquals("a b c", StringUtil.normaliseWhitespace("a   b \n\t c"));
        Assert.assertEquals("a b", StringUtil.normaliseWhitespace("  a   b  "));
        Assert.assertEquals("a b", StringUtil.normaliseWhitespace("a\u00a0\u00a0b"));
        Assert.assertEquals("", StringUtil.normaliseWhitespace("   \t\n\r\f  "));
    }

    @Test
    public void testAppendNormalisedWhitespace() {
        StringBuilder sb = new StringBuilder();
        StringUtil.appendNormalisedWhitespace(sb, "   hello   world   ", true);
        Assert.assertEquals("hello world ", sb.toString());

        sb = new StringBuilder();
        StringUtil.appendNormalisedWhitespace(sb, "   hello   world   ", false);
        Assert.assertEquals(" hello world ", sb.toString());

        sb = new StringBuilder();
        StringUtil.appendNormalisedWhitespace(sb, "hello   world", false);
        Assert.assertEquals("hello world", sb.toString());

        sb = new StringBuilder();
        StringUtil.appendNormalisedWhitespace(sb, "\uD83D\uDE00   \uD83D\uDE00", false);
        Assert.assertEquals("\uD83D\uDE00 \uD83D\uDE00", sb.toString());
    }

    @Test
    public void testIn() {
        Assert.assertTrue(StringUtil.in("b", "a", "b", "c"));
        Assert.assertFalse(StringUtil.in("d", "a", "b", "c"));
        Assert.assertFalse(StringUtil.in("a"));
    }

    @Test
    public void testInSorted() {
        String[] sorted = new String[]{"apple", "banana", "cherry", "date"};
        Assert.assertTrue(StringUtil.inSorted("banana", sorted));
        Assert.assertTrue(StringUtil.inSorted("apple", sorted));
        Assert.assertTrue(StringUtil.inSorted("date", sorted));
        Assert.assertFalse(StringUtil.inSorted("fig", sorted));
        Assert.assertFalse(StringUtil.inSorted("aardvark", sorted));
    }

    @Test
    public void testResolveURL() throws MalformedURLException {
        URL base = new URL("http://example.com/path/file.html");
        Assert.assertEquals("http://example.com/path/other.html", StringUtil.resolve(base, "other.html").toExternalForm());
        Assert.assertEquals("http://example.com/path/file.html?query=1", StringUtil.resolve(base, "?query=1").toExternalForm());
        Assert.assertEquals("http://example.com/path/test.html", StringUtil.resolve(base, "./test.html").toExternalForm());
        Assert.assertEquals("http://example.com/foo", StringUtil.resolve(base, "/foo").toExternalForm());

        URL baseNoLeadingSlash = new URL("http", "example.com", 80, "path/file.html");
        Assert.assertEquals("http://example.com/path/./foo", StringUtil.resolve(baseNoLeadingSlash, "./foo").toExternalForm());
    }

    @Test
    public void testResolveString() {
        Assert.assertEquals("http://example.com/path/other.html", StringUtil.resolve("http://example.com/path/file.html", "other.html"));
        Assert.assertEquals("http://example.com/bar", StringUtil.resolve("invalid-url", "http://example.com/bar"));
        Assert.assertEquals("", StringUtil.resolve("invalid-url", "relative/path"));
        Assert.assertEquals("", StringUtil.resolve("http://example.com/", "http://::invalid::"));
    }

    @Test
    public void testStringBuilderRecycling() {
        StringBuilder sb1 = StringUtil.stringBuilder();
        sb1.append("test");
        Assert.assertEquals("test", sb1.toString());

        StringBuilder sb2 = StringUtil.stringBuilder();
        Assert.assertSame(sb1, sb2);
        Assert.assertEquals(0, sb2.length());

        char[] large = new char[8193];
        Arrays.fill(large, 'a');
        sb2.append(large);

        StringBuilder sb3 = StringUtil.stringBuilder();
        Assert.assertNotSame(sb2, sb3);
        Assert.assertEquals(0, sb3.length());
    }
}
