package org.apache.commons.lang;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.junit.Assert;
import org.junit.Test;

public class StringEscapeUtilsTest {

    @Test
    public void testConstructor() {
        Assert.assertNotNull(new StringEscapeUtils());
    }

    @Test
    public void testEscapeJava() throws IOException {
        Assert.assertNull(StringEscapeUtils.escapeJava(null));
        Assert.assertEquals("", StringEscapeUtils.escapeJava(""));
        Assert.assertEquals("test", StringEscapeUtils.escapeJava("test"));
        Assert.assertEquals("foo\\\"bar", StringEscapeUtils.escapeJava("foo\"bar"));
        Assert.assertEquals("foo'bar", StringEscapeUtils.escapeJava("foo'bar"));
        Assert.assertEquals("foo\\\\bar", StringEscapeUtils.escapeJava("foo\\bar"));
        Assert.assertEquals("foo\\/bar", StringEscapeUtils.escapeJava("foo/bar"));
        Assert.assertEquals("\\b\\n\\t\\f\\r", StringEscapeUtils.escapeJava("\b\n\t\f\r"));
        Assert.assertEquals("\\u0001\\u0010", StringEscapeUtils.escapeJava("\u0001\u0010"));
        Assert.assertEquals("\\u0080", StringEscapeUtils.escapeJava("\u0080"));
        Assert.assertEquals("\\u0123", StringEscapeUtils.escapeJava("\u0123"));
        Assert.assertEquals("\\u1234", StringEscapeUtils.escapeJava("\u1234"));

        StringWriter sw = new StringWriter();
        StringEscapeUtils.escapeJava(sw, null);
        Assert.assertEquals("", sw.toString());

        sw = new StringWriter();
        StringEscapeUtils.escapeJava(sw, "hello\nworld");
        Assert.assertEquals("hello\\nworld", sw.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEscapeJavaNullWriter() throws IOException {
        StringEscapeUtils.escapeJava(null, "test");
    }

    @Test
    public void testEscapeJavaScript() throws IOException {
        Assert.assertNull(StringEscapeUtils.escapeJavaScript(null));
        Assert.assertEquals("", StringEscapeUtils.escapeJavaScript(""));
        Assert.assertEquals("He didn\\'t say, \\\"Stop!\\\"", StringEscapeUtils.escapeJavaScript("He didn't say, \"Stop!\""));

        StringWriter sw = new StringWriter();
        StringEscapeUtils.escapeJavaScript(sw, null);
        Assert.assertEquals("", sw.toString());

        sw = new StringWriter();
        StringEscapeUtils.escapeJavaScript(sw, "It's a test");
        Assert.assertEquals("It\\'s a test", sw.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEscapeJavaScriptNullWriter() throws IOException {
        StringEscapeUtils.escapeJavaScript(null, "test");
    }

    @Test
    public void testUnescapeJava() throws IOException {
        Assert.assertNull(StringEscapeUtils.unescapeJava(null));
        Assert.assertEquals("", StringEscapeUtils.unescapeJava(""));
        Assert.assertEquals("test", StringEscapeUtils.unescapeJava("test"));
        Assert.assertEquals("foo\"bar", StringEscapeUtils.unescapeJava("foo\\\"bar"));
        Assert.assertEquals("foo'bar", StringEscapeUtils.unescapeJava("foo\\'bar"));
        Assert.assertEquals("foo\\bar", StringEscapeUtils.unescapeJava("foo\\\\bar"));
        Assert.assertEquals("\b\n\t\f\r", StringEscapeUtils.unescapeJava("\\b\\n\\t\\f\\r"));
        Assert.assertEquals("\u0041\u1234", StringEscapeUtils.unescapeJava("\\u0041\\u1234"));
        Assert.assertEquals("foo\\", StringEscapeUtils.unescapeJava("foo\\"));
        Assert.assertEquals("foo\\xbar", StringEscapeUtils.unescapeJava("foo\\xbar"));

        StringWriter sw = new StringWriter();
        StringEscapeUtils.unescapeJava(sw, null);
        Assert.assertEquals("", sw.toString());

        sw = new StringWriter();
        StringEscapeUtils.unescapeJava(sw, "hello\\nworld");
        Assert.assertEquals("hello\nworld", sw.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnescapeJavaNullWriter() throws IOException {
        StringEscapeUtils.unescapeJava(null, "test");
    }

    @Test(expected = NestableRuntimeException.class)
    public void testUnescapeJavaInvalidUnicode() {
        StringEscapeUtils.unescapeJava("\\uZZZZ");
    }

    @Test
    public void testUnescapeJavaScript() throws IOException {
        Assert.assertNull(StringEscapeUtils.unescapeJavaScript(null));
        Assert.assertEquals("He didn't say, \"Stop!\"", StringEscapeUtils.unescapeJavaScript("He didn\\'t say, \\\"Stop!\\\""));

        StringWriter sw = new StringWriter();
        StringEscapeUtils.unescapeJavaScript(sw, "hello\\tworld");
        Assert.assertEquals("hello\tworld", sw.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnescapeJavaScriptNullWriter() throws IOException {
        StringEscapeUtils.unescapeJavaScript(null, "test");
    }

    @Test
    public void testEscapeHtml() throws IOException {
        Assert.assertNull(StringEscapeUtils.escapeHtml(null));
        Assert.assertEquals("", StringEscapeUtils.escapeHtml(""));
        Assert.assertEquals("&quot;bread&quot; &amp; &quot;butter&quot;", StringEscapeUtils.escapeHtml("\"bread\" & \"butter\""));
        Assert.assertEquals("&lt;&gt;", StringEscapeUtils.escapeHtml("<>"));

        StringWriter sw = new StringWriter();
        StringEscapeUtils.escapeHtml(sw, null);
        Assert.assertEquals("", sw.toString());

        sw = new StringWriter();
        StringEscapeUtils.escapeHtml(sw, "<b>test</b>");
        Assert.assertEquals("&lt;b&gt;test&lt;/b&gt;", sw.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEscapeHtmlNullWriter() throws IOException {
        StringEscapeUtils.escapeHtml(null, "test");
    }

    @Test
    public void testUnescapeHtml() throws IOException {
        Assert.assertNull(StringEscapeUtils.unescapeHtml(null));
        Assert.assertEquals("", StringEscapeUtils.unescapeHtml(""));
        Assert.assertEquals("<Fran\u00E7ais>", StringEscapeUtils.unescapeHtml("&lt;Fran&ccedil;ais&gt;"));
        Assert.assertEquals("&zzzz;x", StringEscapeUtils.unescapeHtml("&zzzz;x"));

        StringWriter sw = new StringWriter();
        StringEscapeUtils.unescapeHtml(sw, null);
        Assert.assertEquals("", sw.toString());

        sw = new StringWriter();
        StringEscapeUtils.unescapeHtml(sw, "&lt;b&gt;test&lt;/b&gt;");
        Assert.assertEquals("<b>test</b>", sw.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnescapeHtmlNullWriter() throws IOException {
        StringEscapeUtils.unescapeHtml(null, "test");
    }

    @Test
    public void testEscapeXml() throws IOException {
        Assert.assertNull(StringEscapeUtils.escapeXml(null));
        Assert.assertEquals("", StringEscapeUtils.escapeXml(""));
        Assert.assertEquals("&quot;bread&quot; &amp; &apos;butter&apos; &lt;&gt;", StringEscapeUtils.escapeXml("\"bread\" & 'butter' <>"));

        StringWriter sw = new StringWriter();
        StringEscapeUtils.escapeXml(sw, null);
        Assert.assertEquals("", sw.toString());

        sw = new StringWriter();
        StringEscapeUtils.escapeXml(sw, "<foo>");
        Assert.assertEquals("&lt;foo&gt;", sw.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEscapeXmlNullWriter() throws IOException {
        StringEscapeUtils.escapeXml(null, "test");
    }

    @Test
    public void testUnescapeXml() throws IOException {
        Assert.assertNull(StringEscapeUtils.unescapeXml(null));
        Assert.assertEquals("", StringEscapeUtils.unescapeXml(""));
        Assert.assertEquals("\"bread\" & 'butter' <>", StringEscapeUtils.unescapeXml("&quot;bread&quot; &amp; &apos;butter&apos; &lt;&gt;"));

        StringWriter sw = new StringWriter();
        StringEscapeUtils.unescapeXml(sw, null);
        Assert.assertEquals("", sw.toString());

        sw = new StringWriter();
        StringEscapeUtils.unescapeXml(sw, "&lt;foo&gt;");
        Assert.assertEquals("<foo>", sw.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnescapeXmlNullWriter() throws IOException {
        StringEscapeUtils.unescapeXml(null, "test");
    }

    @Test
    public void testEscapeSql() {
        Assert.assertNull(StringEscapeUtils.escapeSql(null));
        Assert.assertEquals("", StringEscapeUtils.escapeSql(""));
        Assert.assertEquals("McHale''s Navy", StringEscapeUtils.escapeSql("McHale's Navy"));
        Assert.assertEquals("no quotes", StringEscapeUtils.escapeSql("no quotes"));
    }

    @Test
    public void testEscapeCsv() throws IOException {
        Assert.assertNull(StringEscapeUtils.escapeCsv(null));
        Assert.assertEquals("", StringEscapeUtils.escapeCsv(""));
        Assert.assertEquals("simple", StringEscapeUtils.escapeCsv("simple"));
        Assert.assertEquals("\"hello, world\"", StringEscapeUtils.escapeCsv("hello, world"));
        Assert.assertEquals("\"hello\\nworld\"", StringEscapeUtils.escapeCsv("hello\nworld"));
        Assert.assertEquals("\"hello\\rworld\"", StringEscapeUtils.escapeCsv("hello\rworld"));
        Assert.assertEquals("\"\"\"quoted\"\"\"", StringEscapeUtils.escapeCsv("\"quoted\""));

        StringWriter sw = new StringWriter();
        StringEscapeUtils.escapeCsv(sw, null);
        Assert.assertEquals("", sw.toString());

        sw = new StringWriter();
        StringEscapeUtils.escapeCsv(sw, "simple");
        Assert.assertEquals("simple", sw.toString());

        sw = new StringWriter();
        StringEscapeUtils.escapeCsv(sw, "a,b");
        Assert.assertEquals("\"a,b\"", sw.toString());
    }

    @Test
    public void testUnescapeCsv() throws IOException {
        Assert.assertNull(StringEscapeUtils.unescapeCsv(null));
        Assert.assertEquals("", StringEscapeUtils.unescapeCsv(""));
        Assert.assertEquals("a", StringEscapeUtils.unescapeCsv("a"));
        Assert.assertEquals("simple", StringEscapeUtils.unescapeCsv("simple"));
        Assert.assertEquals("\"not closed", StringEscapeUtils.unescapeCsv("\"not closed"));
        Assert.assertEquals("not closed\"", StringEscapeUtils.unescapeCsv("not closed\""));
        Assert.assertEquals("plain", StringEscapeUtils.unescapeCsv("\"plain\""));
        Assert.assertEquals("hello, world", StringEscapeUtils.unescapeCsv("\"hello, world\""));
        Assert.assertEquals("line\nbreak", StringEscapeUtils.unescapeCsv("\"line\nbreak\""));
        Assert.assertEquals("line\rbreak", StringEscapeUtils.unescapeCsv("\"line\rbreak\""));
        Assert.assertEquals("\"quoted\"", StringEscapeUtils.unescapeCsv("\"\"\"quoted\"\"\""));

        StringWriter sw = new StringWriter();
        StringEscapeUtils.unescapeCsv(sw, null);
        Assert.assertEquals("", sw.toString());

        sw = new StringWriter();
        StringEscapeUtils.unescapeCsv(sw, "a");
        Assert.assertEquals("a", sw.toString());

        sw = new StringWriter();
        StringEscapeUtils.unescapeCsv(sw, "\"a,b\"");
        Assert.assertEquals("a,b", sw.toString());
    }
}
