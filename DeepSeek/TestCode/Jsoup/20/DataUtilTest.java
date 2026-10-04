package org.jsoup.helper;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class DataUtilTest {

    @Test
    public void testLoadFileWithCharset() throws IOException {
        File tempFile = File.createTempFile("test", ".html");
        tempFile.deleteOnExit();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile), "UTF-8"));
        writer.write("<html><head><meta charset=\"UTF-8\"></head><body>Test</body></html>");
        writer.close();

        Document doc = DataUtil.load(tempFile, "UTF-8", "http://example.com");
        assertNotNull(doc);
        assertEquals("Test", doc.body().text());
    }

    @Test
    public void testLoadFileNullCharset() throws IOException {
        File tempFile = File.createTempFile("test", ".html");
        tempFile.deleteOnExit();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile), "UTF-8"));
        writer.write("<html><head><meta charset=\"UTF-8\"></head><body>Test</body></html>");
        writer.close();

        Document doc = DataUtil.load(tempFile, null, "http://example.com");
        assertNotNull(doc);
        assertEquals("Test", doc.body().text());
    }

    @Test
    public void testLoadInputStreamWithCharset() throws IOException {
        String html = "<html><head><meta charset=\"UTF-8\"></head><body>Test</body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes("UTF-8"));
        Document doc = DataUtil.load(in, "UTF-8", "http://example.com");
        assertNotNull(doc);
        assertEquals("Test", doc.body().text());
    }

    @Test
    public void testLoadInputStreamNullCharset() throws IOException {
        String html = "<html><head><meta charset=\"UTF-8\"></head><body>Test</body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes("UTF-8"));
        Document doc = DataUtil.load(in, null, "http://example.com");
        assertNotNull(doc);
        assertEquals("Test", doc.body().text());
    }

    @Test
    public void testLoadInputStreamWithParser() throws IOException {
        String html = "<root><child>Test</child></root>";
        InputStream in = new ByteArrayInputStream(html.getBytes("UTF-8"));
        Document doc = DataUtil.load(in, "UTF-8", "http://example.com", Parser.xmlParser());
        assertNotNull(doc);
        assertEquals("Test", doc.select("child").text());
    }

    @Test
    public void testParseByteDataWithCharset() {
        String html = "<html><head><meta charset=\"UTF-8\"></head><body>Test</body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(Charset.forName("UTF-8")));
        Document doc = DataUtil.parseByteData(byteData, "UTF-8", "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Test", doc.body().text());
    }

    @Test
    public void testParseByteDataNullCharsetWithMetaHttpEquiv() {
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"></head><body>Test</body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(Charset.forName("ISO-8859-1")));
        Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Test", doc.body().text());
    }

    @Test
    public void testParseByteDataNullCharsetWithMetaCharset() {
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body>Test</body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(Charset.forName("ISO-8859-1")));
        Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Test", doc.body().text());
    }

    @Test
    public void testParseByteDataNullCharsetNoMeta() {
        String html = "<html><head></head><body>Test</body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(Charset.forName("UTF-8")));
        Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Test", doc.body().text());
    }

    @Test
    public void testParseByteDataNullCharsetMetaNotFound() {
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html\"></head><body>Test</body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(Charset.forName("UTF-8")));
        Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Test", doc.body().text());
    }

    @Test
    public void testParseByteDataNullCharsetMetaEmptyCharset() {
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=\"></head><body>Test</body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(Charset.forName("UTF-8")));
        Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Test", doc.body().text());
    }

    @Test
    public void testParseByteDataNullCharsetMetaDefaultCharset() {
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"></head><body>Test</body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(Charset.forName("UTF-8")));
        Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Test", doc.body().text());
    }

    @Test
    public void testParseByteDataWithBOM() {
        String html = "\uFEFF<html><head><meta charset=\"UTF-8\"></head><body>Test</body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(Charset.forName("UTF-8")));
        Document doc = DataUtil.parseByteData(byteData, "UTF-8", "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Test", doc.body().text());
    }

    @Test
    public void testReadToByteBuffer() throws IOException {
        String testString = "Test data for reading";
        InputStream in = new ByteArrayInputStream(testString.getBytes("UTF-8"));
        ByteBuffer result = DataUtil.readToByteBuffer(in);
        assertNotNull(result);
        String resultString = new String(result.array(), "UTF-8").trim();
        assertEquals(testString, resultString);
    }

    @Test
    public void testReadToByteBufferEmptyStream() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        ByteBuffer result = DataUtil.readToByteBuffer(in);
        assertNotNull(result);
        assertEquals(0, result.remaining());
    }

    @Test
    public void testGetCharsetFromContentTypeValid() {
        String charset = DataUtil.getCharsetFromContentType("text/html; charset=EUC-JP");
        assertEquals("EUC-JP", charset);
    }

    @Test
    public void testGetCharsetFromContentTypeWithQuotes() {
        String charset = DataUtil.getCharsetFromContentType("text/html; charset=\"Shift_JIS\"");
        assertEquals("SHIFT_JIS", charset);
    }

    @Test
    public void testGetCharsetFromContentTypeNull() {
        String charset = DataUtil.getCharsetFromContentType(null);
        assertNull(charset);
    }

    @Test
    public void testGetCharsetFromContentTypeNoCharset() {
        String charset = DataUtil.getCharsetFromContentType("text/html");
        assertNull(charset);
    }

    @Test
    public void testGetCharsetFromContentTypeCaseInsensitive() {
        String charset = DataUtil.getCharsetFromContentType("text/html; ChArSeT=utf-8");
        assertEquals("UTF-8", charset);
    }

    @Test
    public void testGetCharsetFromContentTypeWithSpaces() {
        String charset = DataUtil.getCharsetFromContentType("text/html; charset =   utf-8  ");
        assertEquals("UTF-8", charset);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseByteDataEmptyCharset() {
        ByteBuffer byteData = ByteBuffer.wrap("test".getBytes());
        DataUtil.parseByteData(byteData, "", "http://example.com", Parser.htmlParser());
    }

    @Test
    public void testParseByteDataNullCharsetMetaHttpEquivNoContent() {
        String html = "<html><head><meta http-equiv=\"Content-Type\"></head><body>Test</body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(Charset.forName("UTF-8")));
        Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Test", doc.body().text());
    }

    @Test
    public void testParseByteDataNullCharsetMetaCharsetEmpty() {
        String html = "<html><head><meta charset=\"\"></head><body>Test</body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(Charset.forName("UTF-8")));
        Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Test", doc.body().text());
    }
}
