package org.jsoup.helper;

import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DataUtilTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testPrivateConstructor() throws Exception {
        Constructor<DataUtil> constructor = DataUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        DataUtil instance = constructor.newInstance();
        assertNotNull(instance);
    }

    @Test
    public void testGetCharsetFromContentType() {
        assertNull(DataUtil.getCharsetFromContentType(null));
        assertNull(DataUtil.getCharsetFromContentType("text/html"));
        assertNull(DataUtil.getCharsetFromContentType("text/html; no-charset=utf-8"));

        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=utf-8"));
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=\"utf-8\""));
        assertEquals("ISO-8859-1", DataUtil.getCharsetFromContentType("text/html; charset=ISO-8859-1"));
        assertEquals("GB2312", DataUtil.getCharsetFromContentType("text/html; charset=\"GB2312\";"));
        assertEquals("EUC-JP", DataUtil.getCharsetFromContentType("text/html; CHARSET=EUC-JP"));
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset = \"UTF-8\""));
        assertEquals("", DataUtil.getCharsetFromContentType("text/html; charset="));
    }

    @Test
    public void testReadToByteBufferSmall() throws IOException {
        byte[] data = "Hello, World!".getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(data);
        ByteBuffer buffer = DataUtil.readToByteBuffer(in);

        assertNotNull(buffer);
        assertEquals(data.length, buffer.remaining());
        byte[] readBytes = new byte[buffer.remaining()];
        buffer.get(readBytes);
        assertEquals("Hello, World!", new String(readBytes, StandardCharsets.UTF_8));
    }

    @Test
    public void testReadToByteBufferLarge() throws IOException {
        int size = 0x20000 * 2 + 500; // Over 2 buffer chunks
        byte[] data = new byte[size];
        for (int i = 0; i < size; i++) {
            data[i] = (byte) (i % 128);
        }
        InputStream in = new ByteArrayInputStream(data);
        ByteBuffer buffer = DataUtil.readToByteBuffer(in);

        assertNotNull(buffer);
        assertEquals(size, buffer.remaining());
        byte[] readBytes = new byte[buffer.remaining()];
        buffer.get(readBytes);
        for (int i = 0; i < size; i++) {
            assertEquals(data[i], readBytes[i]);
        }
    }

    @Test
    public void testParseByteDataWithExplicitCharset() {
        String html = "<html><head><title>Test</title></head><body><p>Hello</p></body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(StandardCharsets.UTF_8));

        Document doc = DataUtil.parseByteData(buffer, "UTF-8", "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Test", doc.title());
        assertEquals("http://example.com", doc.baseUri());
        assertEquals(Charset.forName("UTF-8"), doc.outputSettings().charset());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseByteDataWithEmptyCharsetThrowsException() {
        String html = "<html><head><title>Test</title></head></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(StandardCharsets.UTF_8));
        DataUtil.parseByteData(buffer, "", "http://example.com", Parser.htmlParser());
    }

    @Test
    public void testParseByteDataNullCharsetNoMeta() {
        String html = "<html><head><title>Default UTF-8</title></head><body><p>Text</p></body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(StandardCharsets.UTF_8));

        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Default UTF-8", doc.title());
        assertEquals(Charset.forName("UTF-8"), doc.outputSettings().charset());
    }

    @Test
    public void testParseByteDataNullCharsetWithMetaHttpEquiv() {
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"><title>IsoDoc</title></head><body><p>Text</p></body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(StandardCharsets.ISO_8859_1));

        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("IsoDoc", doc.title());
        assertEquals(Charset.forName("ISO-8859-1"), doc.outputSettings().charset());
    }

    @Test
    public void testParseByteDataNullCharsetWithMetaCharsetHtml5() {
        String html = "<html><head><meta charset=\"ISO-8859-1\"><title>Html5 Meta</title></head><body><p>Text</p></body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(StandardCharsets.ISO_8859_1));

        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Html5 Meta", doc.title());
        assertEquals(Charset.forName("ISO-8859-1"), doc.outputSettings().charset());
    }

    @Test
    public void testParseByteDataNullCharsetMetaSameAsDefault() {
        String html = "<html><head><meta charset=\"UTF-8\"><title>UTF8 Meta</title></head><body><p>Text</p></body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(StandardCharsets.UTF_8));

        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("UTF8 Meta", doc.title());
        assertEquals(Charset.forName("UTF-8"), doc.outputSettings().charset());
    }

    @Test
    public void testParseByteDataNullCharsetMetaEmpty() {
        String html = "<html><head><meta charset=\"\"><title>Empty Charset Meta</title></head><body><p>Text</p></body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(StandardCharsets.UTF_8));

        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Empty Charset Meta", doc.title());
        assertEquals(Charset.forName("UTF-8"), doc.outputSettings().charset());
    }

    @Test
    public void testParseByteDataNullCharsetMetaHttpEquivNoCharset() {
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html\"><title>No Charset Meta</title></head><body><p>Text</p></body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(StandardCharsets.UTF_8));

        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("No Charset Meta", doc.title());
        assertEquals(Charset.forName("UTF-8"), doc.outputSettings().charset());
    }

    @Test
    public void testLoadFromInputStream() throws IOException {
        String html = "<html><head><title>Stream Test</title></head><body>Hello Stream</body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));

        Document doc = DataUtil.load(in, "UTF-8", "http://example.com");
        assertNotNull(doc);
        assertEquals("Stream Test", doc.title());
        assertEquals("http://example.com", doc.baseUri());
    }

    @Test
    public void testLoadFromInputStreamWithCustomParser() throws IOException {
        String xml = "<xml><title>XML Test</title></xml>";
        InputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));

        Document doc = DataUtil.load(in, "UTF-8", "http://example.com", Parser.xmlParser());
        assertNotNull(doc);
        assertEquals("XML Test", doc.select("title").first().text());
    }

    @Test
    public void testLoadFromFile() throws IOException {
        File file = tempFolder.newFile("test.html");
        String html = "<html><head><title>File Test</title></head><body>Hello File</body></html>";
        FileOutputStream out = new FileOutputStream(file);
        out.write(html.getBytes(StandardCharsets.UTF_8));
        out.close();

        Document doc = DataUtil.load(file, "UTF-8", "http://example.com");
        assertNotNull(doc);
        assertEquals("File Test", doc.title());
        assertEquals("http://example.com", doc.baseUri());
    }

    @Test
    public void testLoadFromNonExistentFileThrowsException() {
        File nonExistentFile = new File(tempFolder.getRoot(), "non_existent.html");
        try {
            DataUtil.load(nonExistentFile, "UTF-8", "http://example.com");
            fail("Expected IOException on non-existent file");
        } catch (IOException e) {
            assertTrue(true);
        }
    }
}
