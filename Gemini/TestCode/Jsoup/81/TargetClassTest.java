package org.jsoup.helper;

import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.Assert;
import org.junit.Test;

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

    @Test
    public void testParseInputStreamNull() throws IOException {
        Document doc = DataUtil.parseInputStream(null, "UTF-8", "http://example.com", Parser.htmlParser());
        Assert.assertNotNull(doc);
        Assert.assertEquals("http://example.com", doc.baseUri());
        Assert.assertEquals(0, doc.childNodeSize());
    }

    @Test
    public void testLoadInputStreamDefault() throws IOException {
        String html = "<html><head><title>Test</title></head><body><p>Hello</p></body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.load(in, "UTF-8", "http://example.com");
        Assert.assertEquals("Test", doc.title());
        Assert.assertEquals("Hello", doc.select("p").text());
    }

    @Test
    public void testLoadInputStreamWithParser() throws IOException {
        String xml = "<xml><data>Value</data></xml>";
        InputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.load(in, "UTF-8", "http://example.com", Parser.xmlParser());
        Assert.assertEquals("Value", doc.select("data").text());
    }

    @Test
    public void testLoadFile() throws IOException {
        File temp = File.createTempFile("jsoup_test", ".html");
        temp.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(temp);
        fos.write("<html><head><title>FileTest</title></head><body>Body</body></html>".getBytes(StandardCharsets.UTF_8));
        fos.close();

        Document doc = DataUtil.load(temp, "UTF-8", "http://example.com");
        Assert.assertEquals("FileTest", doc.title());
    }

    @Test
    public void testCrossStreams() throws IOException {
        byte[] data = "CrossStreamContent".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataUtil.crossStreams(in, out);
        Assert.assertArrayEquals(data, out.toByteArray());
    }

    @Test
    public void testReadToByteBuffer() throws IOException {
        byte[] data = "ByteBufferContent".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ByteBuffer buffer = DataUtil.readToByteBuffer(in, 10);
        Assert.assertEquals(10, buffer.remaining());

        in = new ByteArrayInputStream(data);
        ByteBuffer fullBuffer = DataUtil.readToByteBuffer(in);
        Assert.assertEquals(data.length, fullBuffer.remaining());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadToByteBufferNegativeMaxSize() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[10]);
        DataUtil.readToByteBuffer(in, -1);
    }

    @Test
    public void testReadFileToByteBuffer() throws IOException {
        File temp = File.createTempFile("jsoup_buffer", ".bin");
        temp.deleteOnExit();
        byte[] data = new byte[]{1, 2, 3, 4, 5};
        FileOutputStream fos = new FileOutputStream(temp);
        fos.write(data);
        fos.close();

        ByteBuffer buffer = DataUtil.readFileToByteBuffer(temp);
        Assert.assertEquals(5, buffer.remaining());
        byte[] readBack = new byte[buffer.remaining()];
        buffer.get(readBack);
        Assert.assertArrayEquals(data, readBack);
    }

    @Test
    public void testEmptyByteBuffer() {
        ByteBuffer buffer = DataUtil.emptyByteBuffer();
        Assert.assertNotNull(buffer);
        Assert.assertEquals(0, buffer.remaining());
    }

    @Test
    public void testGetCharsetFromContentType() {
        Assert.assertNull(DataUtil.getCharsetFromContentType(null));
        Assert.assertNull(DataUtil.getCharsetFromContentType("text/html"));
        Assert.assertNull(DataUtil.getCharsetFromContentType("text/html; charset="));
        Assert.assertNull(DataUtil.getCharsetFromContentType("text/html; charset=unsupported_1234"));
        Assert.assertNull(DataUtil.getCharsetFromContentType("text/html; charset=???invalid???"));
        Assert.assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=UTF-8"));
        Assert.assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=\"UTF-8\""));
        Assert.assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset='UTF-8'"));
        Assert.assertEquals("ISO-8859-1", DataUtil.getCharsetFromContentType("text/html; charset=iso-8859-1"));
        Assert.assertEquals("GB2312", DataUtil.getCharsetFromContentType("text/html; charset=gb2312; boundary=something"));
    }

    @Test
    public void testMimeBoundary() {
        String boundary = DataUtil.mimeBoundary();
        Assert.assertNotNull(boundary);
        Assert.assertEquals(DataUtil.boundaryLength, boundary.length());
    }

    @Test
    public void testBomDetectionUtf8() throws IOException {
        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] content = "<p>BOM Test</p>".getBytes(StandardCharsets.UTF_8);
        byte[] all = new byte[bom.length + content.length];
        System.arraycopy(bom, 0, all, 0, bom.length);
        System.arraycopy(content, 0, all, bom.length, content.length);

        Document doc = DataUtil.parseInputStream(new ByteArrayInputStream(all), null, "", Parser.htmlParser());
        Assert.assertEquals("BOM Test", doc.select("p").text());
        Assert.assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    @Test
    public void testBomDetectionUtf16BE() throws IOException {
        byte[] bom = new byte[]{(byte) 0xFE, (byte) 0xFF};
        byte[] content = "<p>BOM16BE</p>".getBytes(StandardCharsets.UTF_16BE);
        byte[] all = new byte[bom.length + content.length];
        System.arraycopy(bom, 0, all, 0, bom.length);
        System.arraycopy(content, 0, all, bom.length, content.length);

        Document doc = DataUtil.parseInputStream(new ByteArrayInputStream(all), null, "", Parser.htmlParser());
        Assert.assertEquals("BOM16BE", doc.select("p").text());
        Assert.assertTrue(doc.outputSettings().charset().name().startsWith("UTF-16"));
    }

    @Test
    public void testBomDetectionUtf16LE() throws IOException {
        byte[] bom = new byte[]{(byte) 0xFF, (byte) 0xFE};
        byte[] content = "<p>BOM16LE</p>".getBytes(StandardCharsets.UTF_16LE);
        byte[] all = new byte[bom.length + content.length];
        System.arraycopy(bom, 0, all, 0, bom.length);
        System.arraycopy(content, 0, all, bom.length, content.length);

        Document doc = DataUtil.parseInputStream(new ByteArrayInputStream(all), null, "", Parser.htmlParser());
        Assert.assertEquals("BOM16LE", doc.select("p").text());
        Assert.assertTrue(doc.outputSettings().charset().name().startsWith("UTF-16"));
    }

    @Test
    public void testBomDetectionUtf32BE() throws IOException {
        if (!Charset.isSupported("UTF-32")) return;
        byte[] bom = new byte[]{0x00, 0x00, (byte) 0xFE, (byte) 0xFF};
        byte[] content = "<p>BOM32BE</p>".getBytes(Charset.forName("UTF-32BE"));
        byte[] all = new byte[bom.length + content.length];
        System.arraycopy(bom, 0, all, 0, bom.length);
        System.arraycopy(content, 0, all, bom.length, content.length);

        Document doc = DataUtil.parseInputStream(new ByteArrayInputStream(all), null, "", Parser.htmlParser());
        Assert.assertEquals("BOM32BE", doc.select("p").text());
    }

    @Test
    public void testBomDetectionUtf32LE() throws IOException {
        if (!Charset.isSupported("UTF-32")) return;
        byte[] bom = new byte[]{(byte) 0xFF, (byte) 0xFE, 0x00, 0x00};
        byte[] content = "<p>BOM32LE</p>".getBytes(Charset.forName("UTF-32LE"));
        byte[] all = new byte[bom.length + content.length];
        System.arraycopy(bom, 0, all, 0, bom.length);
        System.arraycopy(content, 0, all, bom.length, content.length);

        Document doc = DataUtil.parseInputStream(new ByteArrayInputStream(all), null, "", Parser.htmlParser());
        Assert.assertEquals("BOM32LE", doc.select("p").text());
    }

    @Test
    public void testMetaCharsetDetection() throws IOException {
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body><p>MetaCharset</p></body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes(StandardCharsets.ISO_8859_1));
        Document doc = DataUtil.parseInputStream(in, null, "", Parser.htmlParser());
        Assert.assertEquals("MetaCharset", doc.select("p").text());
        Assert.assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    @Test
    public void testMetaHttpEquivDetection() throws IOException {
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"></head><body><p>HttpEquiv</p></body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes(StandardCharsets.ISO_8859_1));
        Document doc = DataUtil.parseInputStream(in, null, "", Parser.htmlParser());
        Assert.assertEquals("HttpEquiv", doc.select("p").text());
        Assert.assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    @Test
    public void testXmlDeclarationDetection() throws IOException {
        String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><root><item>XmlDecl</item></root>";
        InputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.ISO_8859_1));
        Document doc = DataUtil.parseInputStream(in, null, "", Parser.xmlParser());
        Assert.assertEquals("XmlDecl", doc.select("item").text());
        Assert.assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    @Test
    public void testXmlDeclarationNonXmlNode() throws IOException {
        String xml = "<root><item>NotDecl</item></root>";
        InputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.parseInputStream(in, null, "", Parser.xmlParser());
        Assert.assertEquals("NotDecl", doc.select("item").text());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseInputStreamEmptyCharsetNameThrows() throws IOException {
        InputStream in = new ByteArrayInputStream("<p>test</p>".getBytes(StandardCharsets.UTF_8));
        DataUtil.parseInputStream(in, "", "", Parser.htmlParser());
    }

    @Test
    public void testStreamLargerThanFirstReadBuffer() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><meta charset=\"UTF-8\"></head><body>");
        for (int i = 0; i < 6000; i++) {
            sb.append("A");
        }
        sb.append("<p>End</p></body></html>");

        InputStream in = new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.parseInputStream(in, null, "", Parser.htmlParser());
        Assert.assertEquals("End", doc.select("p").text());
    }
}
