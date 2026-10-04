package org.jsoup.helper;

import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class DataUtilTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testGetCharsetFromContentType() {
        Assert.assertNull(DataUtil.getCharsetFromContentType(null));
        Assert.assertNull(DataUtil.getCharsetFromContentType("text/html"));
        Assert.assertNull(DataUtil.getCharsetFromContentType("text/html; charset="));
        Assert.assertNull(DataUtil.getCharsetFromContentType("text/html; charset=\"\""));
        Assert.assertNull(DataUtil.getCharsetFromContentType("text/html; charset=unsupported-charset-name-xyz"));
        Assert.assertNull(DataUtil.getCharsetFromContentType("text/html; charset=???badname???"));

        Assert.assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=utf-8"));
        Assert.assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=UTF-8"));
        Assert.assertEquals("ISO-8859-1", DataUtil.getCharsetFromContentType("text/html; charset=\"ISO-8859-1\""));
        Assert.assertEquals("ISO-8859-1", DataUtil.getCharsetFromContentType("text/html; charset='ISO-8859-1'"));
        Assert.assertEquals("US-ASCII", DataUtil.getCharsetFromContentType("text/html; charset=us-ascii; other=val"));
    }

    @Test
    public void testReadToByteBuffer() throws IOException {
        byte[] data = "Hello, World!".getBytes(StandardCharsets.UTF_8);
        InputStream stream = new ByteArrayInputStream(data);
        ByteBuffer buffer = DataUtil.readToByteBuffer(stream);
        Assert.assertEquals("Hello, World!", new String(buffer.array(), 0, buffer.remaining(), StandardCharsets.UTF_8));

        stream = new ByteArrayInputStream(data);
        buffer = DataUtil.readToByteBuffer(stream, 5);
        Assert.assertEquals(5, buffer.remaining());
        Assert.assertEquals("Hello", new String(buffer.array(), 0, buffer.remaining(), StandardCharsets.UTF_8));

        stream = new ByteArrayInputStream(data);
        buffer = DataUtil.readToByteBuffer(stream, 0);
        Assert.assertEquals(data.length, buffer.remaining());

        stream = new ByteArrayInputStream(data);
        buffer = DataUtil.readToByteBuffer(stream, 100);
        Assert.assertEquals(data.length, buffer.remaining());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadToByteBufferNegativeMaxSize() throws IOException {
        InputStream stream = new ByteArrayInputStream("Test".getBytes(StandardCharsets.UTF_8));
        DataUtil.readToByteBuffer(stream, -1);
    }

    @Test
    public void testParseByteDataExplicitCharset() {
        String html = "<p>Hello World</p>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.parseByteData(buffer, "UTF-8", "http://example.com", Parser.htmlParser());
        Assert.assertEquals("Hello World", doc.select("p").text());
        Assert.assertEquals(Charset.forName("UTF-8"), doc.outputSettings().charset());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseByteDataEmptyCharset() {
        ByteBuffer buffer = ByteBuffer.wrap("<p>Test</p>".getBytes(StandardCharsets.UTF_8));
        DataUtil.parseByteData(buffer, "", "http://example.com", Parser.htmlParser());
    }

    @Test
    public void testParseByteDataMetaCharsetHtml5() {
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body><p>Test</p></body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(StandardCharsets.ISO_8859_1));
        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        Assert.assertEquals("Test", doc.select("p").text());
        Assert.assertEquals(Charset.forName("ISO-8859-1"), doc.outputSettings().charset());
    }

    @Test
    public void testParseByteDataMetaHttpEquiv() {
        String html = "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=ISO-8859-1\"></head><body><p>Test</p></body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(StandardCharsets.ISO_8859_1));
        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        Assert.assertEquals("Test", doc.select("p").text());
        Assert.assertEquals(Charset.forName("ISO-8859-1"), doc.outputSettings().charset());
    }

    @Test
    public void testParseByteDataMetaHttpEquivWithFallbackCharsetAttr() {
        String html = "<html><head><meta http-equiv=\"content-type\" charset=\"ISO-8859-1\"></head><body><p>Test</p></body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(StandardCharsets.ISO_8859_1));
        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        Assert.assertEquals("Test", doc.select("p").text());
        Assert.assertEquals(Charset.forName("ISO-8859-1"), doc.outputSettings().charset());
    }

    @Test
    public void testParseByteDataMetaHttpEquivWithInvalidFallbackCharsetAttr() {
        String html = "<html><head><meta http-equiv=\"content-type\" charset=\"???invalid???\"></head><body><p>Test</p></body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        Assert.assertEquals("Test", doc.select("p").text());
    }

    @Test
    public void testParseByteDataMetaCharsetUtf8() {
        String html = "<html><head><meta charset=\"UTF-8\"></head><body><p>Test</p></body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        Assert.assertEquals("Test", doc.select("p").text());
    }

    @Test
    public void testParseByteDataNoMetaTag() {
        String html = "<html><head></head><body><p>Test</p></body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        Assert.assertEquals("Test", doc.select("p").text());
    }

    @Test
    public void testParseByteDataWithBom() {
        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] text = "<p>With BOM</p>".getBytes(StandardCharsets.UTF_8);
        byte[] all = new byte[bom.length + text.length];
        System.arraycopy(bom, 0, all, 0, bom.length);
        System.arraycopy(text, 0, all, bom.length, text.length);

        ByteBuffer buffer = ByteBuffer.wrap(all);
        Document doc = DataUtil.parseByteData(buffer, "UTF-8", "http://example.com", Parser.htmlParser());
        Assert.assertEquals("With BOM", doc.select("p").text());
        Assert.assertEquals(Charset.forName("UTF-8"), doc.outputSettings().charset());
    }

    @Test
    public void testLoadInputStream() throws IOException {
        String html = "<html><body><p>Stream Content</p></body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.load(in, "UTF-8", "http://example.com");
        Assert.assertEquals("Stream Content", doc.select("p").text());
    }

    @Test
    public void testLoadInputStreamWithParser() throws IOException {
        String xml = "<xml><item>Data</item></xml>";
        InputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.load(in, "UTF-8", "http://example.com", Parser.xmlParser());
        Assert.assertEquals("Data", doc.select("item").text());
    }

    @Test
    public void testLoadFile() throws IOException {
        File tempFile = tempFolder.newFile("test.html");
        String html = "<html><body><h1>File Header</h1></body></html>";
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            out.write(html.getBytes(StandardCharsets.UTF_8));
        }

        Document doc = DataUtil.load(tempFile, "UTF-8", "http://example.com");
        Assert.assertEquals("File Header", doc.select("h1").text());
    }
}
