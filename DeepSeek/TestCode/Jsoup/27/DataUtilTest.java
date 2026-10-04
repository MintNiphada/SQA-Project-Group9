package org.jsoup.helper;

import org.junit.Test;
import static org.junit.Assert.*;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class DataUtilTest {

    @Test
    public void testLoadFileWithCharset() throws IOException {
        File tempFile = createTempFile("test content", "UTF-8");
        Document doc = DataUtil.load(tempFile, "UTF-8", "http://example.com");
        assertNotNull(doc);
        assertEquals("http://example.com", doc.baseUri());
        assertTrue(tempFile.delete());
    }

    @Test
    public void testLoadFileWithNullCharset() throws IOException {
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"></head><body></body></html>";
        File tempFile = createTempFile(html, "ISO-8859-1");
        Document doc = DataUtil.load(tempFile, null, "http://example.com");
        assertNotNull(doc);
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
        assertTrue(tempFile.delete());
    }

    @Test
    public void testLoadInputStreamWithCharset() throws IOException {
        String html = "<html><body>test</body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes("UTF-8"));
        Document doc = DataUtil.load(in, "UTF-8", "http://example.com");
        assertNotNull(doc);
        assertEquals("http://example.com", doc.baseUri());
    }

    @Test
    public void testLoadInputStreamWithNullCharset() throws IOException {
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body></body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes("ISO-8859-1"));
        Document doc = DataUtil.load(in, null, "http://example.com");
        assertNotNull(doc);
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    @Test
    public void testLoadInputStreamWithParser() throws IOException {
        String xml = "<root><item>value</item></root>";
        InputStream in = new ByteArrayInputStream(xml.getBytes("UTF-8"));
        Document doc = DataUtil.load(in, "UTF-8", "http://example.com", Parser.xmlParser());
        assertNotNull(doc);
        assertEquals("root", doc.child(0).tagName());
    }

    @Test
    public void testParseByteDataNullCharsetNoMeta() {
        String html = "<html><body>test</body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(Charset.forName("UTF-8")));
        Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    @Test
    public void testParseByteDataNullCharsetWithMetaHttpEquiv() {
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"></head><body></body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(Charset.forName("ISO-8859-1")));
        Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    @Test
    public void testParseByteDataNullCharsetWithMetaCharset() {
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body></body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(Charset.forName("ISO-8859-1")));
        Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    @Test
    public void testParseByteDataNullCharsetMetaFoundButEmpty() {
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=\"></head><body></body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(Charset.forName("UTF-8")));
        Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    @Test
    public void testParseByteDataNullCharsetMetaFoundButSameAsDefault() {
        String html = "<html><head><meta charset=\"UTF-8\"></head><body></body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(Charset.forName("UTF-8")));
        Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseByteDataWithEmptyCharset() {
        ByteBuffer byteData = ByteBuffer.wrap("test".getBytes());
        DataUtil.parseByteData(byteData, "", "http://example.com", Parser.htmlParser());
    }

    @Test
    public void testParseByteDataWithBOM() {
        String content = "\uFEFF<html><body>test</body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(content.getBytes(Charset.forName("UTF-8")));
        Document doc = DataUtil.parseByteData(byteData, "UTF-8", "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("test", doc.body().text());
    }

    @Test
    public void testReadToByteBufferEmptyStream() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        ByteBuffer result = DataUtil.readToByteBuffer(in);
        assertEquals(0, result.remaining());
    }

    @Test
    public void testReadToByteBufferNormalStream() throws IOException {
        byte[] data = "test data".getBytes();
        InputStream in = new ByteArrayInputStream(data);
        ByteBuffer result = DataUtil.readToByteBuffer(in);
        assertArrayEquals(data, result.array());
    }

    @Test
    public void testGetCharsetFromContentTypeNull() {
        assertNull(DataUtil.getCharsetFromContentType(null));
    }

    @Test
    public void testGetCharsetFromContentTypeWithCharset() {
        assertEquals("ISO-8859-1", DataUtil.getCharsetFromContentType("text/html; charset=ISO-8859-1"));
    }

    @Test
    public void testGetCharsetFromContentTypeWithoutCharset() {
        assertNull(DataUtil.getCharsetFromContentType("text/html"));
    }

    @Test
    public void testGetCharsetFromContentTypeWithQuotes() {
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=\"UTF-8\""));
    }

    @Test
    public void testGetCharsetFromContentTypeCaseInsensitive() {
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; Charset=utf-8"));
    }

    private File createTempFile(String content, String charset) throws IOException {
        File tempFile = File.createTempFile("jsoup", ".html");
        tempFile.deleteOnExit();
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(content.getBytes(charset));
        }
        return tempFile;
    }
}
