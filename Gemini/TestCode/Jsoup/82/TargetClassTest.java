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
    public void testEmptyByteBuffer() {
        ByteBuffer buffer = DataUtil.emptyByteBuffer();
        Assert.assertNotNull(buffer);
        Assert.assertEquals(0, buffer.capacity());
        Assert.assertEquals(0, buffer.remaining());
    }

    @Test
    public void testMimeBoundary() {
        String boundary = DataUtil.mimeBoundary();
        Assert.assertNotNull(boundary);
        Assert.assertEquals(DataUtil.boundaryLength, boundary.length());
        Assert.assertTrue(boundary.matches("[-_a-zA-Z0-9]{32}"));
    }

    @Test
    public void testCrossStreams() throws IOException {
        byte[] inputData = "Hello World across streams".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(inputData);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        DataUtil.crossStreams(in, out);
        Assert.assertArrayEquals(inputData, out.toByteArray());
    }

    @Test
    public void testReadToByteBufferUnlimited() throws IOException {
        byte[] inputData = "Read unlimited byte buffer test data".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(inputData);
        ByteBuffer buffer = DataUtil.readToByteBuffer(in, 0);

        byte[] result = new byte[buffer.remaining()];
        buffer.get(result);
        Assert.assertArrayEquals(inputData, result);
    }

    @Test
    public void testReadToByteBufferWithLimit() throws IOException {
        byte[] inputData = "Limited byte buffer test data".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(inputData);
        ByteBuffer buffer = DataUtil.readToByteBuffer(in, 7);

        Assert.assertEquals(7, buffer.remaining());
        byte[] result = new byte[buffer.remaining()];
        buffer.get(result);
        Assert.assertArrayEquals("Limited".getBytes(StandardCharsets.UTF_8), result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadToByteBufferNegativeLimit() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        DataUtil.readToByteBuffer(in, -1);
    }

    @Test
    public void testGetCharsetFromContentType() {
        Assert.assertNull(DataUtil.getCharsetFromContentType(null));
        Assert.assertNull(DataUtil.getCharsetFromContentType("text/html"));
        Assert.assertNull(DataUtil.getCharsetFromContentType("text/html; charset="));
        Assert.assertNull(DataUtil.getCharsetFromContentType("text/html; charset=unsupported-charset-12345"));
        Assert.assertNull(DataUtil.getCharsetFromContentType("text/html; charset=??illegal"));

        Assert.assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=UTF-8"));
        Assert.assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=\"UTF-8\""));
        Assert.assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset='UTF-8'"));
        Assert.assertEquals("ISO-8859-1", DataUtil.getCharsetFromContentType("text/html; charset=iso-8859-1; boundary=something"));
    }

    @Test
    public void testParseInputStreamNullInput() throws IOException {
        Document doc = DataUtil.parseInputStream(null, "UTF-8", "http://example.com", Parser.htmlParser());
        Assert.assertNotNull(doc);
        Assert.assertEquals("http://example.com", doc.baseUri());
        Assert.assertEquals(0, doc.children().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseInputStreamEmptyCharsetNameThrows() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream("<p>Hello</p>".getBytes(StandardCharsets.UTF_8));
        DataUtil.parseInputStream(in, "", "http://example.com", Parser.htmlParser());
    }

    @Test
    public void testParseInputStreamWithExplicitCharset() throws IOException {
        String html = "<html><body><p>Test</p></body></html>";
        ByteArrayInputStream in = new ByteArrayInputStream(html.getBytes(StandardCharsets.ISO_8859_1));
        Document doc = DataUtil.parseInputStream(in, "ISO-8859-1", "http://example.com", Parser.htmlParser());

        Assert.assertNotNull(doc);
        Assert.assertEquals("Test", doc.select("p").text());
        Assert.assertEquals(StandardCharsets.ISO_8859_1, doc.outputSettings().charset());
    }

    @Test
    public void testParseInputStreamDetectMetaHttpEquiv() throws IOException {
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"></head><body><p>Caf\u00e9</p></body></html>";
        ByteArrayInputStream in = new ByteArrayInputStream(html.getBytes(StandardCharsets.ISO_8859_1));
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());

        Assert.assertNotNull(doc);
        Assert.assertEquals("Caf\u00e9", doc.select("p").text());
        Assert.assertEquals(StandardCharsets.ISO_8859_1, doc.outputSettings().charset());
    }

    @Test
    public void testParseInputStreamDetectMetaCharset() throws IOException {
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body><p>Caf\u00e9</p></body></html>";
        ByteArrayInputStream in = new ByteArrayInputStream(html.getBytes(StandardCharsets.ISO_8859_1));
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());

        Assert.assertNotNull(doc);
        Assert.assertEquals("Caf\u00e9", doc.select("p").text());
        Assert.assertEquals(StandardCharsets.ISO_8859_1, doc.outputSettings().charset());
    }

    @Test
    public void testParseInputStreamDetectXmlDeclaration() throws IOException {
        String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><root><item>Caf\u00e9</item></root>";
        ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.ISO_8859_1));
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.xmlParser());

        Assert.assertNotNull(doc);
        Assert.assertEquals("Caf\u00e9", doc.select("item").text());
        Assert.assertEquals(StandardCharsets.ISO_8859_1, doc.outputSettings().charset());
    }

    @Test
    public void testParseInputStreamDetectXmlDeclarationInComment() throws IOException {
        String xmlComment = "<!--?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><root><item>Caf\u00e9</item></root>";
        ByteArrayInputStream in = new ByteArrayInputStream(xmlComment.getBytes(StandardCharsets.ISO_8859_1));
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());

        Assert.assertNotNull(doc);
        Assert.assertEquals("Caf\u00e9", doc.select("item").text());
        Assert.assertEquals(StandardCharsets.ISO_8859_1, doc.outputSettings().charset());
    }

    @Test
    public void testParseInputStreamDetectUtf8Bom() throws IOException {
        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] content = "<p>Hello BOM</p>".getBytes(StandardCharsets.UTF_8);
        byte[] total = new byte[bom.length + content.length];
        System.arraycopy(bom, 0, total, 0, bom.length);
        System.arraycopy(content, 0, total, bom.length, content.length);

        ByteArrayInputStream in = new ByteArrayInputStream(total);
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());

        Assert.assertNotNull(doc);
        Assert.assertEquals("Hello BOM", doc.select("p").text());
        Assert.assertEquals(StandardCharsets.UTF_8, doc.outputSettings().charset());
    }

    @Test
    public void testParseInputStreamDetectUtf16BeBom() throws IOException {
        byte[] bom = new byte[]{(byte) 0xFE, (byte) 0xFF};
        byte[] content = "<p>UTF-16BE</p>".getBytes(StandardCharsets.UTF_16BE);
        byte[] total = new byte[bom.length + content.length];
        System.arraycopy(bom, 0, total, 0, bom.length);
        System.arraycopy(content, 0, total, bom.length, content.length);

        ByteArrayInputStream in = new ByteArrayInputStream(total);
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());

        Assert.assertNotNull(doc);
        Assert.assertEquals("UTF-16BE", doc.select("p").text());
    }

    @Test
    public void testParseInputStreamDetectUtf16LeBom() throws IOException {
        byte[] bom = new byte[]{(byte) 0xFF, (byte) 0xFE};
        byte[] content = "<p>UTF-16LE</p>".getBytes(StandardCharsets.UTF_16LE);
        byte[] total = new byte[bom.length + content.length];
        System.arraycopy(bom, 0, total, 0, bom.length);
        System.arraycopy(content, 0, total, bom.length, content.length);

        ByteArrayInputStream in = new ByteArrayInputStream(total);
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());

        Assert.assertNotNull(doc);
        Assert.assertEquals("UTF-16LE", doc.select("p").text());
    }

    @Test
    public void testParseInputStreamDetectUtf32BeBom() throws IOException {
        if (!Charset.isSupported("UTF-32")) {
            return;
        }
        byte[] bom = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0xFE, (byte) 0xFF};
        byte[] content = "<p>UTF-32BE</p>".getBytes(Charset.forName("UTF-32BE"));
        byte[] total = new byte[bom.length + content.length];
        System.arraycopy(bom, 0, total, 0, bom.length);
        System.arraycopy(content, 0, total, bom.length, content.length);

        ByteArrayInputStream in = new ByteArrayInputStream(total);
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());

        Assert.assertNotNull(doc);
        Assert.assertEquals("UTF-32BE", doc.select("p").text());
    }

    @Test
    public void testParseInputStreamDetectUtf32LeBom() throws IOException {
        if (!Charset.isSupported("UTF-32")) {
            return;
        }
        byte[] bom = new byte[]{(byte) 0xFF, (byte) 0xFE, (byte) 0x00, (byte) 0x00};
        byte[] content = "<p>UTF-32LE</p>".getBytes(Charset.forName("UTF-32LE"));
        byte[] total = new byte[bom.length + content.length];
        System.arraycopy(bom, 0, total, 0, bom.length);
        System.arraycopy(content, 0, total, bom.length, content.length);

        ByteArrayInputStream in = new ByteArrayInputStream(total);
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());

        Assert.assertNotNull(doc);
        Assert.assertEquals("UTF-32LE", doc.select("p").text());
    }

    @Test
    public void testParseInputStreamLargeStreamNotFullyReadInitial() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body><p>");
        for (int i = 0; i < 6000; i++) {
            sb.append("A");
        }
        sb.append("</p></body></html>");
        String content = sb.toString();

        ByteArrayInputStream in = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());

        Assert.assertNotNull(doc);
        Assert.assertEquals(6000, doc.select("p").text().length());
    }

    @Test
    public void testLoadFromFile() throws IOException {
        File tempFile = tempFolder.newFile("test.html");
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            out.write("<html><body><p>From File</p></body></html>".getBytes(StandardCharsets.UTF_8));
        }

        Document doc = DataUtil.load(tempFile, "UTF-8", "http://example.com");
        Assert.assertNotNull(doc);
        Assert.assertEquals("From File", doc.select("p").text());
    }

    @Test
    public void testLoadFromInputStream() throws IOException {
        InputStream in = new ByteArrayInputStream("<p>Stream</p>".getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.load(in, "UTF-8", "http://example.com");
        Assert.assertNotNull(doc);
        Assert.assertEquals("Stream", doc.select("p").text());
    }

    @Test
    public void testLoadFromInputStreamWithParser() throws IOException {
        InputStream in = new ByteArrayInputStream("<root><child>Value</child></root>".getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.load(in, "UTF-8", "http://example.com", Parser.xmlParser());
        Assert.assertNotNull(doc);
        Assert.assertEquals("Value", doc.select("child").text());
    }
}
