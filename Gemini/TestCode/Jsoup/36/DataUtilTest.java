package org.jsoup.helper;

import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class DataUtilTest {

    @Test
    public void testPrivateConstructor() throws Exception {
        Constructor<DataUtil> constructor = DataUtil.class.getDeclaredConstructor();
        Assert.assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        DataUtil instance = constructor.newInstance();
        Assert.assertNotNull(instance);
    }

    @Test
    public void testGetCharsetFromContentTypeValid() {
        Assert.assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=UTF-8"));
        Assert.assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=\"UTF-8\""));
        Assert.assertEquals("ISO-8859-1", DataUtil.getCharsetFromContentType("text/html; charset=iso-8859-1"));
        Assert.assertEquals("US-ASCII", DataUtil.getCharsetFromContentType("text/html; charset=US-ASCII; extra=param"));
    }

    @Test
    public void testGetCharsetFromContentTypeInvalidAndNull() {
        Assert.assertNull(DataUtil.getCharsetFromContentType(null));
        Assert.assertNull(DataUtil.getCharsetFromContentType("text/html"));
        Assert.assertNull(DataUtil.getCharsetFromContentType("text/html; charset=UNSUPPORTED_CHARSET_XYZ"));
        Assert.assertNull(DataUtil.getCharsetFromContentType("text/html; charset="));
    }

    @Test
    public void testReadToByteBufferUnlimited() throws IOException {
        byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(data);
        ByteBuffer buffer = DataUtil.readToByteBuffer(in);
        Assert.assertEquals(data.length, buffer.remaining());
        Assert.assertEquals("Hello World", StandardCharsets.UTF_8.decode(buffer).toString());
    }

    @Test
    public void testReadToByteBufferCapped() throws IOException {
        byte[] data = "Hello Beautiful World".getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(data);
        ByteBuffer buffer = DataUtil.readToByteBuffer(in, 5);
        Assert.assertEquals(5, buffer.remaining());
        Assert.assertEquals("Hello", StandardCharsets.UTF_8.decode(buffer).toString());
    }

    @Test
    public void testReadToByteBufferExactSize() throws IOException {
        byte[] data = "Hello".getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(data);
        ByteBuffer buffer = DataUtil.readToByteBuffer(in, 5);
        Assert.assertEquals(5, buffer.remaining());
        Assert.assertEquals("Hello", StandardCharsets.UTF_8.decode(buffer).toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadToByteBufferNegativeMaxSize() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        DataUtil.readToByteBuffer(in, -1);
    }

    @Test
    public void testParseByteDataExplicitCharset() {
        String html = "<p>Test Paragraph</p>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(StandardCharsets.ISO_8859_1));
        Document doc = DataUtil.parseByteData(buffer, "ISO-8859-1", "http://example.com", Parser.htmlParser());
        Assert.assertEquals("Test Paragraph", doc.select("p").text());
        Assert.assertEquals("http://example.com", doc.baseUri());
        Assert.assertEquals(Charset.forName("ISO-8859-1"), doc.outputSettings().charset());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseByteDataEmptyCharsetThrows() {
        ByteBuffer buffer = ByteBuffer.wrap("<p>Test</p>".getBytes(StandardCharsets.UTF_8));
        DataUtil.parseByteData(buffer, "", "http://example.com", Parser.htmlParser());
    }

    @Test
    public void testParseByteDataNullCharsetDetectHttpEquiv() {
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"></head><body><p>\u00e9</p></body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(StandardCharsets.ISO_8859_1));
        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        Assert.assertEquals("\u00e9", doc.select("p").text());
        Assert.assertEquals(Charset.forName("ISO-8859-1"), doc.outputSettings().charset());
    }

    @Test
    public void testParseByteDataNullCharsetDetectHtml5MetaCharset() {
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body><p>\u00e9</p></body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(StandardCharsets.ISO_8859_1));
        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        Assert.assertEquals("\u00e9", doc.select("p").text());
        Assert.assertEquals(Charset.forName("ISO-8859-1"), doc.outputSettings().charset());
    }

    @Test
    public void testParseByteDataNullCharsetMetaUtf8() {
        String html = "<html><head><meta charset=\"UTF-8\"></head><body><p>Hello</p></body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        Assert.assertEquals("Hello", doc.select("p").text());
        Assert.assertEquals(Charset.forName("UTF-8"), doc.outputSettings().charset());
    }

    @Test
    public void testParseByteDataNullCharsetNoMeta() {
        String html = "<html><body><p>Hello World</p></body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        Assert.assertEquals("Hello World", doc.select("p").text());
        Assert.assertEquals(Charset.forName("UTF-8"), doc.outputSettings().charset());
    }

    @Test
    public void testParseByteDataWithBom() {
        String html = "\uFEFF<html><head><meta charset=\"ISO-8859-1\"></head><body><p>Test</p></body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(StandardCharsets.ISO_8859_1));
        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        Assert.assertEquals("Test", doc.select("p").text());
    }

    @Test
    public void testLoadInputStream() throws IOException {
        String html = "<title>Test Title</title>";
        InputStream in = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.load(in, "UTF-8", "http://example.com");
        Assert.assertEquals("Test Title", doc.title());
    }

    @Test
    public void testLoadInputStreamWithParser() throws IOException {
        String xml = "<xml><item>value</item></xml>";
        InputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.load(in, "UTF-8", "http://example.com", Parser.xmlParser());
        Assert.assertEquals("value", doc.select("item").text());
    }

    @Test
    public void testLoadFile() throws IOException {
        File tempFile = File.createTempFile("jsoup_test", ".html");
        tempFile.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(tempFile);
        fos.write("<title>File Test</title>".getBytes(StandardCharsets.UTF_8));
        fos.close();

        Document doc = DataUtil.load(tempFile, "UTF-8", "http://example.com");
        Assert.assertEquals("File Test", doc.title());
    }
}
