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
    public void testCrossStreams() throws IOException {
        byte[] inputData = "Hello World across streams!".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(inputData);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataUtil.crossStreams(in, out);
        Assert.assertArrayEquals(inputData, out.toByteArray());
    }

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
        Assert.assertEquals(32, boundary.length());
        String boundary2 = DataUtil.mimeBoundary();
        Assert.assertNotEquals(boundary, boundary2);
    }

    @Test
    public void testGetCharsetFromContentType() {
        Assert.assertNull(DataUtil.getCharsetFromContentType(null));
        Assert.assertNull(DataUtil.getCharsetFromContentType("text/html"));
        Assert.assertNull(DataUtil.getCharsetFromContentType("text/html; boundary=something"));
        Assert.assertNull(DataUtil.getCharsetFromContentType("text/html; charset=unsupported_charset_xyz"));
        Assert.assertNull(DataUtil.getCharsetFromContentType("text/html; charset=???invalid"));

        Assert.assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=UTF-8"));
        Assert.assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=\"UTF-8\""));
        Assert.assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset='UTF-8'"));
        Assert.assertEquals("GB2312", DataUtil.getCharsetFromContentType("text/html; charset=gb2312"));
        Assert.assertEquals("ISO-8859-1", DataUtil.getCharsetFromContentType("text/html;charset=iso-8859-1"));
        Assert.assertEquals("US-ASCII", DataUtil.getCharsetFromContentType("text/html; charset=US-ASCII; header=foo"));
    }

    @Test
    public void testReadToByteBuffer() throws IOException {
        byte[] data = "Sample byte buffer test string".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ByteBuffer buffer = DataUtil.readToByteBuffer(in, 10);
        Assert.assertEquals(10, buffer.remaining());

        in = new ByteArrayInputStream(data);
        ByteBuffer fullBuffer = DataUtil.readToByteBuffer(in);
        Assert.assertEquals(data.length, fullBuffer.remaining());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadToByteBufferNegativeMaxSize() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[]{1, 2, 3});
        DataUtil.readToByteBuffer(in, -1);
    }

    @Test
    public void testReadFileToByteBuffer() throws IOException {
        File tempFile = tempFolder.newFile("testRead.txt");
        byte[] expected = "File content to buffer".getBytes(StandardCharsets.UTF_8);
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(expected);
        }
        ByteBuffer buf = DataUtil.readFileToByteBuffer(tempFile);
        byte[] actual = new byte[buf.remaining()];
        buf.get(actual);
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void testParseInputStreamNull() throws IOException {
        Document doc = DataUtil.parseInputStream(null, "UTF-8", "http://example.com", Parser.htmlParser());
        Assert.assertNotNull(doc);
        Assert.assertEquals("http://example.com", doc.baseUri());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseInputStreamEmptyCharset() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream("<p>test</p>".getBytes(StandardCharsets.UTF_8));
        DataUtil.parseInputStream(in, "", "http://example.com", Parser.htmlParser());
    }

    @Test
    public void testParseInputStreamWithExplicitCharset() throws IOException {
        String html = "<html><body><p>Hello World</p></body></html>";
        ByteArrayInputStream in = new ByteArrayInputStream(html.getBytes(StandardCharsets.ISO_8859_1));
        Document doc = DataUtil.parseInputStream(in, "ISO-8859-1", "http://example.com", Parser.htmlParser());
        Assert.assertEquals("Hello World", doc.select("p").text());
        Assert.assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    @Test
    public void testParseInputStreamDetectMetaHttpEquiv() throws IOException {
        String html = "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=ISO-8859-1\"></head><body><p>Café</p></body></html>";
        ByteArrayInputStream in = new ByteArrayInputStream(html.getBytes(StandardCharsets.ISO_8859_1));
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());
        Assert.assertEquals("Café", doc.select("p").text());
        Assert.assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    @Test
    public void testParseInputStreamDetectMetaCharset() throws IOException {
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body><p>Café</p></body></html>";
        ByteArrayInputStream in = new ByteArrayInputStream(html.getBytes(StandardCharsets.ISO_8859_1));
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());
        Assert.assertEquals("Café", doc.select("p").text());
        Assert.assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    @Test
    public void testParseInputStreamDetectXmlDeclaration() throws IOException {
        String xml = "<?xml encoding=\"ISO-8859-1\"?><root><data>Café</data></root>";
        ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.ISO_8859_1));
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.xmlParser());
        Assert.assertEquals("Café", doc.select("data").text());
        Assert.assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    @Test
    public void testParseInputStreamLargeStreamDefaultCharset() throws IOException {
        StringBuilder sb = new StringBuilder("<html><body>");
        for (int i = 0; i < 6000; i++) {
            sb.append("a");
        }
        sb.append("</body></html>");
        ByteArrayInputStream in = new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());
        Assert.assertEquals("UTF-8", doc.outputSettings().charset().name());
        Assert.assertTrue(doc.body().text().length() >= 6000);
    }

    @Test
    public void testParseInputStreamUtf8Bom() throws IOException {
        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] text = "<p>With BOM</p>".getBytes(StandardCharsets.UTF_8);
        byte[] all = new byte[bom.length + text.length];
        System.arraycopy(bom, 0, all, 0, bom.length);
        System.arraycopy(text, 0, all, bom.length, text.length);

        ByteArrayInputStream in = new ByteArrayInputStream(all);
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());
        Assert.assertEquals("With BOM", doc.select("p").text());
        Assert.assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    @Test
    public void testParseInputStreamUtf16BeBom() throws IOException {
        byte[] bom = new byte[]{(byte) 0xFE, (byte) 0xFF};
        byte[] text = "<p>UTF16 BE</p>".getBytes(StandardCharsets.UTF_16BE);
        byte[] all = new byte[bom.length + text.length];
        System.arraycopy(bom, 0, all, 0, bom.length);
        System.arraycopy(text, 0, all, bom.length, text.length);

        ByteArrayInputStream in = new ByteArrayInputStream(all);
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());
        Assert.assertEquals("UTF16 BE", doc.select("p").text());
        Assert.assertEquals("UTF-16", doc.outputSettings().charset().name());
    }

    @Test
    public void testParseInputStreamUtf16LeBom() throws IOException {
        byte[] bom = new byte[]{(byte) 0xFF, (byte) 0xFE};
        byte[] text = "<p>UTF16 LE</p>".getBytes(StandardCharsets.UTF_16LE);
        byte[] all = new byte[bom.length + text.length];
        System.arraycopy(bom, 0, all, 0, bom.length);
        System.arraycopy(text, 0, all, bom.length, text.length);

        ByteArrayInputStream in = new ByteArrayInputStream(all);
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());
        Assert.assertEquals("UTF16 LE", doc.select("p").text());
        Assert.assertEquals("UTF-16", doc.outputSettings().charset().name());
    }

    @Test
    public void testParseInputStreamUtf32BeBom() throws IOException {
        if (!Charset.isSupported("UTF-32")) return;
        byte[] bom = new byte[]{0x00, 0x00, (byte) 0xFE, (byte) 0xFF};
        byte[] text = "<p>UTF32 BE</p>".getBytes(Charset.forName("UTF-32BE"));
        byte[] all = new byte[bom.length + text.length];
        System.arraycopy(bom, 0, all, 0, bom.length);
        System.arraycopy(text, 0, all, bom.length, text.length);

        ByteArrayInputStream in = new ByteArrayInputStream(all);
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());
        Assert.assertEquals("UTF32 BE", doc.select("p").text());
        Assert.assertEquals("UTF-32", doc.outputSettings().charset().name());
    }

    @Test
    public void testParseInputStreamUtf32LeBom() throws IOException {
        if (!Charset.isSupported("UTF-32")) return;
        byte[] bom = new byte[]{(byte) 0xFF, (byte) 0xFE, 0x00, 0x00};
        byte[] text = "<p>UTF32 LE</p>".getBytes(Charset.forName("UTF-32LE"));
        byte[] all = new byte[bom.length + text.length];
        System.arraycopy(bom, 0, all, 0, bom.length);
        System.arraycopy(text, 0, all, bom.length, text.length);

        ByteArrayInputStream in = new ByteArrayInputStream(all);
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());
        Assert.assertEquals("UTF32 LE", doc.select("p").text());
        Assert.assertEquals("UTF-32", doc.outputSettings().charset().name());
    }

    @Test
    public void testLoadFromFile() throws IOException {
        File tempFile = tempFolder.newFile("loadTest.html");
        String html = "<html><body><p>File Load</p></body></html>";
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(html.getBytes(StandardCharsets.UTF_8));
        }
        Document doc = DataUtil.load(tempFile, "UTF-8", "http://example.com");
        Assert.assertEquals("File Load", doc.select("p").text());
    }

    @Test
    public void testLoadFromInputStream() throws IOException {
        InputStream in = new ByteArrayInputStream("<p>Stream Load</p>".getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.load(in, "UTF-8", "http://example.com");
        Assert.assertEquals("Stream Load", doc.select("p").text());

        in = new ByteArrayInputStream("<xml><p>XML Stream</p></xml>".getBytes(StandardCharsets.UTF_8));
        Document docXml = DataUtil.load(in, "UTF-8", "http://example.com", Parser.xmlParser());
        Assert.assertEquals("XML Stream", docXml.select("p").text());
    }
}
