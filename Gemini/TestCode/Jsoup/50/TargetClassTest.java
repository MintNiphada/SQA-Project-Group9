package org.jsoup.helper;

import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
    public void testGetCharsetFromContentTypeValid() {
        Assert.assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=UTF-8"));
        Assert.assertEquals("ISO-8859-1", DataUtil.getCharsetFromContentType("text/html; charset=\"ISO-8859-1\""));
        Assert.assertEquals("GB2312", DataUtil.getCharsetFromContentType("text/html; charset='GB2312'"));
        Assert.assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html;charset=utf-8"));
    }

    @Test
    public void testGetCharsetFromContentTypeInvalidOrNull() {
        Assert.assertNull(DataUtil.getCharsetFromContentType(null));
        Assert.assertNull(DataUtil.getCharsetFromContentType("text/html"));
        Assert.assertNull(DataUtil.getCharsetFromContentType("text/html; charset="));
        Assert.assertNull(DataUtil.getCharsetFromContentType("text/html; charset=unsupported_charset_xyz"));
        Assert.assertNull(DataUtil.getCharsetFromContentType("text/html; charset=??invalid??"));
    }

    @Test
    public void testMimeBoundary() {
        String boundary = DataUtil.mimeBoundary();
        Assert.assertNotNull(boundary);
        Assert.assertEquals(DataUtil.boundaryLength, boundary.length());
        String boundary2 = DataUtil.mimeBoundary();
        Assert.assertNotEquals(boundary, boundary2);
    }

    @Test
    public void testEmptyByteBuffer() {
        ByteBuffer buffer = DataUtil.emptyByteBuffer();
        Assert.assertNotNull(buffer);
        Assert.assertEquals(0, buffer.capacity());
    }

    @Test
    public void testReadToByteBufferUnlimited() throws IOException {
        byte[] expected = "Hello World DataUtil".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(expected);
        ByteBuffer buffer = DataUtil.readToByteBuffer(in);
        Assert.assertEquals(expected.length, buffer.remaining());
        Assert.assertArrayEquals(expected, buffer.array());
    }

    @Test
    public void testReadToByteBufferCapped() throws IOException {
        byte[] expected = "Hello World DataUtil".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(expected);
        ByteBuffer buffer = DataUtil.readToByteBuffer(in, 5);
        Assert.assertEquals(5, buffer.remaining());
        byte[] actual = new byte[5];
        buffer.get(actual);
        Assert.assertArrayEquals("Hello".getBytes(StandardCharsets.UTF_8), actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadToByteBufferNegativeMaxSize() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[10]);
        DataUtil.readToByteBuffer(in, -1);
    }

    @Test
    public void testCrossStreams() throws IOException {
        byte[] expected = "Test Stream Data".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(expected);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataUtil.crossStreams(in, out);
        Assert.assertArrayEquals(expected, out.toByteArray());
    }

    @Test
    public void testReadFileToByteBuffer() throws IOException {
        File tempFile = tempFolder.newFile("test.html");
        byte[] data = "<p>File content</p>".getBytes(StandardCharsets.UTF_8);
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(data);
        }
        ByteBuffer buffer = DataUtil.readFileToByteBuffer(tempFile);
        Assert.assertEquals(data.length, buffer.remaining());
        Assert.assertArrayEquals(data, buffer.array());
    }

    @Test
    public void testParseByteDataExplicitCharset() {
        String html = "<p>Hello World</p>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(StandardCharsets.ISO_8859_1));
        Document doc = DataUtil.parseByteData(buffer, "ISO-8859-1", "http://example.com", Parser.htmlParser());
        Assert.assertEquals("Hello World", doc.select("p").text());
        Assert.assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseByteDataEmptyCharsetThrows() {
        ByteBuffer buffer = ByteBuffer.wrap("<p>Hello</p>".getBytes(StandardCharsets.UTF_8));
        DataUtil.parseByteData(buffer, "", "http://example.com", Parser.htmlParser());
    }

    @Test
    public void testParseByteDataMetaHttpEquiv() {
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"></head><body><p>Test</p></body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(StandardCharsets.ISO_8859_1));
        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        Assert.assertEquals("Test", doc.select("p").text());
        Assert.assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    @Test
    public void testParseByteDataMetaCharset() {
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body><p>Test</p></body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(StandardCharsets.ISO_8859_1));
        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        Assert.assertEquals("Test", doc.select("p").text());
        Assert.assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    @Test
    public void testParseByteDataMetaInvalidCharset() {
        String html = "<html><head><meta charset=\"invalid-charset-123\"></head><body><p>Test</p></body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        Assert.assertEquals("Test", doc.select("p").text());
        Assert.assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    @Test
    public void testParseByteDataWithBom() {
        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] text = "<p>BOM Test</p>".getBytes(StandardCharsets.UTF_8);
        byte[] combined = new byte[bom.length + text.length];
        System.arraycopy(bom, 0, combined, 0, bom.length);
        System.arraycopy(text, 0, combined, bom.length, text.length);

        ByteBuffer buffer = ByteBuffer.wrap(combined);
        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        Assert.assertEquals("BOM Test", doc.select("p").text());
        Assert.assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    @Test
    public void testParseByteDataWithBomAndExplicitCharset() {
        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] text = "<p>BOM Test 2</p>".getBytes(StandardCharsets.UTF_8);
        byte[] combined = new byte[bom.length + text.length];
        System.arraycopy(bom, 0, combined, 0, bom.length);
        System.arraycopy(text, 0, combined, bom.length, text.length);

        ByteBuffer buffer = ByteBuffer.wrap(combined);
        Document doc = DataUtil.parseByteData(buffer, "UTF-8", "http://example.com", Parser.htmlParser());
        Assert.assertEquals("BOM Test 2", doc.select("p").text());
        Assert.assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    @Test
    public void testLoadFile() throws IOException {
        File file = tempFolder.newFile("loadTest.html");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write("<title>Load File</title>".getBytes(StandardCharsets.UTF_8));
        }
        Document doc = DataUtil.load(file, "UTF-8", "http://example.com");
        Assert.assertEquals("Load File", doc.title());
    }

    @Test
    public void testLoadInputStream() throws IOException {
        InputStream in = new ByteArrayInputStream("<title>Stream Test</title>".getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.load(in, "UTF-8", "http://example.com");
        Assert.assertEquals("Stream Test", doc.title());
    }

    @Test
    public void testLoadInputStreamWithParser() throws IOException {
        InputStream in = new ByteArrayInputStream("<xml><node>Value</node></xml>".getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.load(in, "UTF-8", "http://example.com", Parser.xmlParser());
        Assert.assertEquals("Value", doc.select("node").text());
    }
}
