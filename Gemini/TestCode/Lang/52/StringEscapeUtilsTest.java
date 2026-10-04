package org.apache.commons.lang;

import org.apache.commons.lang.exception.NestableRuntimeException;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

public class StringEscapeUtilsTest {

    @Test
    public void testConstructor() {
        Assert.assertNotNull(new StringEscapeUtils());
    }

    @Test
    public void testEscapeJavaNull() throws IOException {
        Assert.assertNull(StringEscapeUtils.escapeJava(null));
        StringWriter writer = new StringWriter();
        StringEscapeUtils.escapeJava(writer, null);
        Assert.assertEquals("", writer.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEscapeJavaNullWriter() throws IOException {
        StringEscapeUtils.escapeJava(null, "test");
    }

    @Test
    public void testEscapeJavaBasic() {
        Assert.assertEquals("He didn't say, \\\"Stop!\\\"", StringEscapeUtils.escapeJava("He didn't say, \"Stop!\""));
        Assert.assertEquals("foo\\\\bar", StringEscapeUtils.escapeJava("foo\\bar"));
        Assert.assertEquals("foo\\r\\n\\t\\b\\fbar", StringEscapeUtils.escapeJava("foo\r\n\t\b\fbar"));
    }

    @Test
    public void testEscapeJavaUnicodeAndControlChars() {
        Assert.assertEquals("\\u0001", StringEscapeUtils.escapeJava("\u0001"));
        Assert.assertEquals("\\u0010", StringEscapeUtils.escapeJava("\u0010"));
        Assert.assertEquals("\\u0080", StringEscapeUtils.escapeJava("\u0080"));
        Assert.assertEquals("\\u0100", StringEscapeUtils.escapeJava("\u0100"));
        Assert.assertEquals("\\u1000", StringEscapeUtils.escapeJava("\u1000"));
    }

    @Test
    public void testEscapeJavaScript() throws IOException {
        Assert.assertNull(StringEscapeUtils.escapeJavaScript(null));
        StringWriter writer = new StringWriter();
        StringEscapeUtils.escapeJavaScript(writer, null);
        Assert.assertEquals("", writer.toString());

        Assert.assertEquals("He didn\\'t say, \\\"Stop!\\\"", StringEscapeUtils.escapeJavaScript("He didn't say, \"Stop!\""));

        writer = new StringWriter();
        StringEscapeUtils.escapeJavaScript(writer, "He didn't say, \"Stop!\"");
        Assert.assertEquals("He didn\\'t say, \\\"Stop!\\\"", writer.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEscapeJavaScriptNullWriter() throws IOException {
        StringEscapeUtils.escapeJavaScript(null, "test");
    }

    @Test
    public void testUnescapeJavaNull() throws IOException {
        Assert.assertNull(StringEscapeUtils.unescapeJava(null));
        StringWriter writer = new StringWriter();
        StringEscapeUtils.unescapeJava(writer, null);
        Assert.assertEquals("", writer.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnescapeJavaNullWriter() throws IOException {
        StringEscapeUtils.unescapeJava(null, "test");
    }

    @Test
    public void testUnescapeJavaBasic() {
        Assert.assertEquals("He didn't say, \"Stop!\"", StringEscapeUtils.unescapeJava("He didn't say, \\\"Stop!\\\""));
        Assert.assertEquals("foo\\bar", StringEscapeUtils.unescapeJava("foo\\\\bar"));
        Assert.assertEquals("foo'bar", StringEscapeUtils.unescapeJava("foo\\'bar"));
        Assert.assertEquals("foo\r\n\t\b\fbar", StringEscapeUtils.unescapeJava("foo\\r\\n\\t\\b\\fbar"));
        Assert.assertEquals("foo\\kbar", StringEscapeUtils.unescapeJava("foo\\kbar"));
        Assert.assertEquals("foo\\", StringEscapeUtils.unescapeJava("foo\\"));
    }

    @Test
    public void testUnescapeJavaUnicode() {
        Assert.assertEquals("A", StringEscapeUtils.unescapeJava("\\u0041"));
        Assert.assertEquals("Hello World", StringEscapeUtils.unescapeJava("\\u0048\\u0065\\u006c\\u006c\\u006f\\u0020\\u0057\\u006f\\u0072\\u006c\\u0064"));
    }

    @Test(expected = NestableRuntimeException.class)
    public void testUnescapeJavaInvalidUnicode() {
        StringEscapeUtils.unescapeJava("\\uZZZZ");
    }

    @Test
    public void testUnescapeJavaScript() throws IOException {
        Assert.assertNull(StringEscapeUtils.unescapeJavaScript(null));
        StringWriter writer = new StringWriter();
        StringEscapeUtils.unescapeJavaScript(writer, null);
        Assert.assertEquals("", writer.toString());

        Assert.assertEquals("He didn't say, \"Stop!\"", StringEscapeUtils.unescapeJavaScript("He didn\\'t say, \\\"Stop!\\\""));

        writer = new StringWriter();
        StringEscapeUtils.unescapeJavaScript(writer, "He didn\\'t say, \\\"Stop!\\\"");
        Assert.assertEquals("He didn't say, \"Stop!\"", writer.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnescapeJavaScriptNullWriter() throws IOException {
        StringEscapeUtils.unescapeJavaScript(null, "test");
    }

    @Test
    public void testEscapeHtmlNull() throws IOException {
        Assert.assertNull(StringEscapeUtils.escapeHtml(null));
        StringWriter writer = new StringWriter();
        StringEscapeUtils.escapeHtml(writer, null);
        Assert.assertEquals("", writer.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEscapeHtmlNullWriter() throws IOException {
        StringEscapeUtils.escapeHtml(null, "test");
    }

    @Test
    public void testEscapeHtml() throws IOException {
        Assert.assertEquals("&quot;bread&quot; &amp; &quot;butter&quot;", StringEscapeUtils.escapeHtml("\"bread\" & \"butter\""));
        StringWriter writer = new StringWriter();
        StringEscapeUtils.escapeHtml(writer, "<script>alert('xss');</script>");
        Assert.assertEquals("&lt;script&gt;alert('xss');&lt;/script&gt;", writer.toString());
    }

    @Test
    public void testUnescapeHtmlNull() throws IOException {
        Assert.assertNull(StringEscapeUtils.unescapeHtml(null));
        StringWriter writer = new StringWriter();
        StringEscapeUtils.unescapeHtml(writer, null);
        Assert.assertEquals("", writer.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnescapeHtmlNullWriter() throws IOException {
        StringEscapeUtils.unescapeHtml(null, "test");
    }

    @Test
    public void testUnescapeHtml() throws IOException {
        Assert.assertEquals("\"bread\" & \"butter\"", StringEscapeUtils.unescapeHtml("&quot;bread&quot; &amp; &quot;butter&quot;"));
        Assert.assertEquals("<Français>", StringEscapeUtils.unescapeHtml("&lt;Fran&ccedil;ais&gt;"));
        Assert.assertEquals("&zzzz;x", StringEscapeUtils.unescapeHtml("&zzzz;x"));

        StringWriter writer = new StringWriter();
        StringEscapeUtils.unescapeHtml(writer, "&lt;b&gt;bold&lt;/b&gt;");
        Assert.assertEquals("<b>bold</b>", writer.toString());
    }

    @Test
    public void testEscapeXmlNull() throws IOException {
        Assert.assertNull(StringEscapeUtils.escapeXml(null));
        StringWriter writer = new StringWriter();
        StringEscapeUtils.escapeXml(writer, null);
        Assert.assertEquals("", writer.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEscapeXmlNullWriter() throws IOException {
        StringEscapeUtils.escapeXml(null, "test");
    }

    @Test
    public void testEscapeXml() throws IOException {
        Assert.assertEquals("&quot;bread&quot; &amp; &apos;butter&apos; &lt; &gt;", StringEscapeUtils.escapeXml("\"bread\" & 'butter' < >"));
        StringWriter writer = new StringWriter();
        StringEscapeUtils.escapeXml(writer, "<foo & bar>");
        Assert.assertEquals("&lt;foo &amp; bar&gt;", writer.toString());
    }

    @Test
    public void testUnescapeXmlNull() throws IOException {
        Assert.assertNull(StringEscapeUtils.unescapeXml(null));
        StringWriter writer = new StringWriter();
        StringEscapeUtils.unescapeXml(writer, null);
        Assert.assertEquals("", writer.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnescapeXmlNullWriter() throws IOException {
        StringEscapeUtils.unescapeXml(null, "test");
    }

    @Test
    public void testUnescapeXml() throws IOException {
        Assert.assertEquals("\"bread\" & 'butter' < >", StringEscapeUtils.unescapeXml("&quot;bread&quot; &amp; &apos;butter&apos; &lt; &gt;"));
        StringWriter writer = new StringWriter();
        StringEscapeUtils.unescapeXml(writer, "&lt;tag&gt;value&lt;/tag&gt;");
        Assert.assertEquals("<tag>value</tag>", writer.toString());
    }

    @Test
    public void testEscapeSql() {
        Assert.assertNull(StringEscapeUtils.escapeSql(null));
        Assert.assertEquals("", StringEscapeUtils.escapeSql(""));
        Assert.assertEquals("McHale''s Navy", StringEscapeUtils.escapeSql("McHale's Navy"));
        Assert.assertEquals("No single quote", StringEscapeUtils.escapeSql("No single quote"));
        Assert.assertEquals("''''", StringEscapeUtils.escapeSql("''"));
    }
}
