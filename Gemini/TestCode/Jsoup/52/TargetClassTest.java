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
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testEmptyByteBuffer() {
        ByteBuffer buffer = DataUtil.emptyByteBuffer();
        Assert.assertNotNull(buffer);
        Assert.assertEquals(0, buffer.capacity());
        Assert.assertEquals(0, buffer.remaining());
    }

    @Test
    public void testMimeBoundary() {
        String boundary1 = DataUtil.mimeBoundary();
        String boundary2 = DataUtil.mimeBoundary();
        Assert.assertNotNull(boundary1);
        Assert.assertEquals(DataUtil.boundaryLength, boundary1.length());
        Assert.assertNotNull(boundary2);
        Assert.assertEquals(DataUtil.boundaryLength, boundary2.length());
    }

    @Test
    public void testGetCharsetFromContentType() {
        Assert.assertNull(DataUtil.getCharsetFromContentType(null));
        Assert.assertNull(DataUtil.getCharsetFromContentType("text/html"));
        Assert.assertNull(DataUtil.getCharsetFromContentType("text/html; charset="));
        Assert.assertNull(DataUtil.getCharsetFromContentType("text/html; charset=unsupported_charset_xyz"));
        Assert.assertNull(DataUtil.getCharsetFromContentType("text/html; charset=\"\""));
        Assert.assertNull(DataUtil.getCharsetFromContentType("text/html; charset=invalid charset with spaces"));

        Assert.assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=utf-8"));
        Assert.assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=\"UTF-8\""));
        Assert.assertEquals("ISO-8859-1", DataUtil.getCharsetFromContentType("text/html; charset='ISO-8859-1'"));
        Assert.assertEquals("US-ASCII", DataUtil.getCharsetFromContentType("text/html;charset=us-ascii;charset=other"));
    }

    @Test
    public void testCrossStreams() throws IOException {
        byte[] inputData = "Testing cross streams functionality".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(inputData);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        DataUtil.crossStreams(in, out);

        Assert.assertArrayEquals(inputData, out.toByteArray());
    }

    @Test
    public void testReadToByteBufferUnlimited() throws IOException {
        byte[] data = "Hello, World! Jsoup DataUtil test.".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(data);

        ByteBuffer byteBuffer = DataUtil.readToByteBuffer(in);
        Assert.assertArrayEquals(data, byteBuffer.array());
    }

    @Test
    public void testReadToByteBufferCapped() throws IOException {
        byte[] data = "1234567890abcdefghijklmnopqrstuvwxyz".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(data);

        ByteBuffer byteBuffer = DataUtil.readToByteBuffer(in, 10);
        Assert.assertEquals(10, byteBuffer.remaining());
        byte[] result = new byte[10];
        byteBuffer.get(result);
        Assert.assertArrayEquals("1234567890".getBytes(StandardCharsets.UTF_8), result);
    }

    @Test
    public void testReadToByteBufferCappedExactOrLarger() throws IOException {
        byte[] data = "12345".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(data);

        ByteBuffer byteBuffer = DataUtil.readToByteBuffer(in, 10);
        Assert.assertEquals(5, byteBuffer.remaining());
        byte[] result = new byte[5];
        byteBuffer.get(result);
        Assert.assertArrayEquals(data, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadToByteBufferNegativeMaxSize() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        DataUtil.readToByteBuffer(in, -1);
    }

    @Test
    public void testReadFileToByteBufferAndLoadFile() throws IOException {
        File file = temporaryFolder.newFile("test.html");
        String html = "<html><head><title>File Test</title></head><body><p>Hello File</p></body></html>";
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(html.getBytes(StandardCharsets.UTF_8));
        }

        ByteBuffer byteBuffer = DataUtil.readFileToByteBuffer(file);
        Assert.assertEquals(html, new String(byteBuffer.array(), StandardCharsets.UTF_8));

        Document doc = DataUtil.load(file, "UTF-8", "http://example.com");
        Assert.assertEquals("File Test", doc.title());
        Assert.assertEquals("Hello File", doc.select("p").text());
        Assert.assertEquals("http://example.com", doc.baseUri());
    }

    @Test
    public void testLoadInputStreamDefaultParser() throws IOException {
        String html = "<html><head><title>Stream Test</title></head><body><p>Hello Stream</p></body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));

        Document doc = DataUtil.load(in, "UTF-8", "http://example.com");
        Assert.assertEquals("Stream Test", doc.title());
        Assert.assertEquals("Hello Stream", doc.select("p").text());
    }

    @Test
    public void testLoadInputStreamXmlParser() throws IOException {
        String xml = "<root><child id='1'>Value</child></root>";
        InputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));

        Document doc = DataUtil.load(in, "UTF-8", "http://example.com", Parser.xmlParser());
        Assert.assertEquals("Value", doc.select("child").text());
    }

    @Test
    public void testParseByteDataWithBomUtf8() {
        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] content = "<p>BOM UTF8</p>".getBytes(StandardCharsets.UTF_8);
        ByteBuffer byteBuffer = ByteBuffer.allocate(bom.length + content.length);
        byteBuffer.put(bom);
        byteBuffer.put(content);
        byteBuffer.flip();

        Document doc = DataUtil.parseByteData(byteBuffer, null, "http://example.com", Parser.htmlParser());
        Assert.assertEquals("BOM UTF8", doc.select("p").text());
        Assert.assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    @Test
    public void testParseByteDataWithBomUtf16Be() {
        byte[] bom = new byte[]{(byte) 0xFE, (byte) 0xFF};
        byte[] content = "<p>BOM UTF16BE</p>".getBytes(StandardCharsets.UTF_16BE);
        ByteBuffer byteBuffer = ByteBuffer.allocate(bom.length + content.length);
        byteBuffer.put(bom);
        byteBuffer.put(content);
        byteBuffer.flip();

        Document doc = DataUtil.parseByteData(byteBuffer, null, "http://example.com", Parser.htmlParser());
        Assert.assertEquals("BOM UTF16BE", doc.select("p").text());
    }

    @Test
    public void testParseByteDataWithBomUtf16Le() {
        byte[] bom = new byte[]{(byte) 0xFF, (byte) 0xFE};
        byte[] content = "<p>BOM UTF16LE</p>".getBytes(StandardCharsets.UTF_16LE);
        ByteBuffer byteBuffer = ByteBuffer.allocate(bom.length + content.length);
        byteBuffer.put(bom);
        byteBuffer.put(content);
        byteBuffer.flip();

        Document doc = DataUtil.parseByteData(byteBuffer, null, "http://example.com", Parser.htmlParser());
        Assert.assertEquals("BOM UTF16LE", doc.select("p").text());
    }

    @Test
    public void testParseByteDataWithBomUtf32Be() {
        byte[] bom = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0xFE, (byte) 0xFF};
        ByteBuffer byteBuffer = ByteBuffer.wrap(bom);

        if (Charset.isSupported("UTF-32")) {
            Document doc = DataUtil.parseByteData(byteBuffer, null, "http://example.com", Parser.htmlParser());
            Assert.assertNotNull(doc);
        }
    }

    @Test
    public void testParseByteDataWithBomUtf32Le() {
        byte[] bom = new byte[]{(byte) 0xFF, (byte) 0xFE, (byte) 0x00, (byte) 0x00};
        ByteBuffer byteBuffer = ByteBuffer.wrap(bom);

        if (Charset.isSupported("UTF-32")) {
            Document doc = DataUtil.parseByteData(byteBuffer, null, "http://example.com", Parser.htmlParser());
            Assert.assertNotNull(doc);
        }
    }

    @Test
    public void testParseByteDataShortBufferNoBom() {
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[]{(byte) 0x3C, (byte) 0x70});
        Document doc = DataUtil.parseByteData(byteBuffer, "UTF-8", "http://example.com", Parser.htmlParser());
        Assert.assertNotNull(doc);
    }

    @Test
    public void testParseByteDataMetaHttpEquiv() {
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"></head><body><p>Hell\u00f3</p></body></html>";
        ByteBuffer byteBuffer = ByteBuffer.wrap(html.getBytes(StandardCharsets.ISO_8859_1));

        Document doc = DataUtil.parseByteData(byteBuffer, null, "http://example.com", Parser.htmlParser());
        Assert.assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
        Assert.assertEquals("Hell\u00f3", doc.select("p").text());
    }

    @Test
    public void testParseByteDataMetaCharset() {
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body><p>Hell\u00f3</p></body></html>";
        ByteBuffer byteBuffer = ByteBuffer.wrap(html.getBytes(StandardCharsets.ISO_8859_1));

        Document doc = DataUtil.parseByteData(byteBuffer, null, "http://example.com", Parser.htmlParser());
        Assert.assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
        Assert.assertEquals("Hell\u00f3", doc.select("p").text());
    }

    @Test
    public void testParseByteDataMetaCharsetUtf8Already() {
        String html = "<html><head><meta charset=\"UTF-8\"></head><body><p>Hello UTF8</p></body></html>";
        ByteBuffer byteBuffer = ByteBuffer.wrap(html.getBytes(StandardCharsets.UTF_8));

        Document doc = DataUtil.parseByteData(byteBuffer, null, "http://example.com", Parser.htmlParser());
        Assert.assertEquals("UTF-8", doc.outputSettings().charset().name());
        Assert.assertEquals("Hello UTF8", doc.select("p").text());
    }

    @Test
    public void testParseByteDataMetaInvalidCharsetFallback() {
        String html = "<html><head><meta charset=\"unsupported_123456\"></head><body><p>Fallback</p></body></html>";
        ByteBuffer byteBuffer = ByteBuffer.wrap(html.getBytes(StandardCharsets.UTF_8));

        Document doc = DataUtil.parseByteData(byteBuffer, null, "http://example.com", Parser.htmlParser());
        Assert.assertEquals("UTF-8", doc.outputSettings().charset().name());
        Assert.assertEquals("Fallback", doc.select("p").text());
    }

    @Test
    public void testParseByteDataXmlProlog() {
        String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><root><p>Hell\u00f3</p></root>";
        ByteBuffer byteBuffer = ByteBuffer.wrap(xml.getBytes(StandardCharsets.ISO_8859_1));

        Document doc = DataUtil.parseByteData(byteBuffer, null, "http://example.com", Parser.xmlParser());
        Assert.assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
        Assert.assertEquals("Hell\u00f3", doc.select("p").text());
    }

    @Test
    public void testParseByteDataXmlPrologNotXml() {
        String xml = "<root><p>Test</p></root>";
        ByteBuffer byteBuffer = ByteBuffer.wrap(xml.getBytes(StandardCharsets.UTF_8));

        Document doc = DataUtil.parseByteData(byteBuffer, null, "http://example.com", Parser.xmlParser());
        Assert.assertEquals("UTF-8", doc.outputSettings().charset().name());
        Assert.assertEquals("Test", doc.select("p").text());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseByteDataEmptyCharsetNameThrows() {
        ByteBuffer byteBuffer = ByteBuffer.wrap("<p>Test</p>".getBytes(StandardCharsets.UTF_8));
        DataUtil.parseByteData(byteBuffer, "", "http://example.com", Parser.htmlParser());
    }
}
