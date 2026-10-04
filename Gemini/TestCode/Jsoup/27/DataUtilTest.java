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
import java.lang.reflect.Modifier;
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
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testPrivateConstructor() throws Exception {
        Constructor<DataUtil> constructor = DataUtil.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        DataUtil instance = constructor.newInstance();
        assertNotNull(instance);
    }

    @Test
    public void testGetCharsetFromContentType() {
        assertNull(DataUtil.getCharsetFromContentType(null));
        assertNull(DataUtil.getCharsetFromContentType("text/html"));
        assertNull(DataUtil.getCharsetFromContentType("text/html; nocharset"));

        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=utf-8"));
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=UTF-8"));
        assertEquals("ISO-8859-1", DataUtil.getCharsetFromContentType("text/html; charset=iso-8859-1"));
        assertEquals("GB2312", DataUtil.getCharsetFromContentType("text/html; charset=\"gb2312\""));
        assertEquals("GB2312", DataUtil.getCharsetFromContentType("text/html; charset= \"gb2312\""));
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=\"utf-8\"; other=val"));
        assertEquals("US-ASCII", DataUtil.getCharsetFromContentType("text/plain; CHARSET=us-ascii"));
    }

    @Test
    public void testReadToByteBuffer() throws IOException {
        byte[] original = "Hello World! This is test byte data.".getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(original);
        ByteBuffer byteBuffer = DataUtil.readToByteBuffer(in);

        assertNotNull(byteBuffer);
        assertEquals(original.length, byteBuffer.remaining());

        byte[] readBack = new byte[byteBuffer.remaining()];
        byteBuffer.get(readBack);
        assertEquals(new String(original, StandardCharsets.UTF_8), new String(readBack, StandardCharsets.UTF_8));
    }

    @Test
    public void testReadToByteBufferLargerThanBufferSize() throws IOException {
        // bufferSize is 0x20000 = 131072 bytes. Test larger stream to ensure loop iterations.
        int testSize = 0x20000 + 5000;
        byte[] largeData = new byte[testSize];
        for (int i = 0; i < testSize; i++) {
            largeData[i] = (byte) (i % 127);
        }

        InputStream in = new ByteArrayInputStream(largeData);
        ByteBuffer byteBuffer = DataUtil.readToByteBuffer(in);

        assertNotNull(byteBuffer);
        assertEquals(testSize, byteBuffer.remaining());

        byte[] readBack = new byte[byteBuffer.remaining()];
        byteBuffer.get(readBack);
        for (int i = 0; i < testSize; i++) {
            assertEquals(largeData[i], readBack[i]);
        }
    }

    @Test
    public void testParseByteDataWithSpecifiedCharset() {
        String html = "<html><head><title>Test Specified</title></head><body><p>Hello</p></body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(StandardCharsets.ISO_8859_1));

        Document doc = DataUtil.parseByteData(byteData, "ISO-8859-1", "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Test Specified", doc.title());
        assertEquals("http://example.com", doc.baseUri());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseByteDataEmptyCharsetThrowsException() {
        ByteBuffer byteData = ByteBuffer.wrap("<html></html>".getBytes(StandardCharsets.UTF_8));
        DataUtil.parseByteData(byteData, "", "http://example.com", Parser.htmlParser());
    }

    @Test
    public void testParseByteDataWithNullCharsetNoMetaTag() {
        String html = "<html><head><title>No Meta</title></head><body><p>Hello UTF-8</p></body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(StandardCharsets.UTF_8));

        Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("No Meta", doc.title());
    }

    @Test
    public void testParseByteDataDetectsMetaHttpEquivCharset() {
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\"><title>Iso Meta</title></head><body><p>\u00e9</p></body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(Charset.forName("ISO-8859-1")));

        Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Iso Meta", doc.title());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
        assertEquals("\u00e9", doc.select("p").first().text());
    }

    @Test
    public void testParseByteDataDetectsHtml5MetaCharset() {
        String html = "<html><head><meta charset=\"iso-8859-1\"><title>Html5 Charset</title></head><body><p>\u00e9</p></body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(Charset.forName("ISO-8859-1")));

        Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Html5 Charset", doc.title());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
        assertEquals("\u00e9", doc.select("p").first().text());
    }

    @Test
    public void testParseByteDataMetaCharsetIsDefaultUtf8() {
        String html = "<html><head><meta charset=\"UTF-8\"><title>UTF-8 Meta</title></head><body><p>Hello</p></body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(StandardCharsets.UTF_8));

        Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("UTF-8 Meta", doc.title());
    }

    @Test
    public void testParseByteDataMetaWithInvalidOrEmptyCharset() {
        String html = "<html><head><meta charset=\"\"><title>Empty Meta Charset</title></head><body><p>Hello</p></body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(StandardCharsets.UTF_8));

        Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Empty Meta Charset", doc.title());
    }

    @Test
    public void testParseByteDataMetaHttpEquivWithoutCharset() {
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html\"><title>No Charset In Meta</title></head><body><p>Hello</p></body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(StandardCharsets.UTF_8));

        Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("No Charset In Meta", doc.title());
    }

    @Test
    public void testParseByteDataStripsBom() {
        String html = "\uFEFF<html><head><meta charset=\"iso-8859-1\"><title>BOM Test</title></head><body><p>Text</p></body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(Charset.forName("ISO-8859-1")));

        Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("BOM Test", doc.title());
        // Verify BOM is stripped and does not prepend stray text node before head
        assertEquals("html", doc.child(0).nodeName());
    }

    @Test
    public void testLoadInputStreamDefaultParser() throws IOException {
        String html = "<html><head><title>Stream Test</title></head><body><p>Body text</p></body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));

        Document doc = DataUtil.load(in, "UTF-8", "http://example.com");
        assertNotNull(doc);
        assertEquals("Stream Test", doc.title());
        assertEquals("http://example.com", doc.baseUri());
    }

    @Test
    public void testLoadInputStreamWithXmlParser() throws IOException {
        String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?><root><child>value</child></root>";
        InputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));

        Document doc = DataUtil.load(in, "UTF-8", "http://example.com", Parser.xmlParser());
        assertNotNull(doc);
        assertEquals("value", doc.select("child").first().text());
    }

    @Test
    public void testLoadFile() throws IOException {
        File tempFile = temporaryFolder.newFile("test.html");
        String html = "<html><head><title>File Test</title></head><body><p>File content</p></body></html>";
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            out.write(html.getBytes(StandardCharsets.UTF_8));
        }

        Document doc = DataUtil.load(tempFile, "UTF-8", "http://example.com");
        assertNotNull(doc);
        assertEquals("File Test", doc.title());
        assertEquals("http://example.com", doc.baseUri());
    }

    @Test(expected = IOException.class)
    public void testLoadNonExistentFileThrowsException() throws IOException {
        File nonExistent = new File(temporaryFolder.getRoot(), "does_not_exist.html");
        DataUtil.load(nonExistent, "UTF-8", "http://example.com");
    }
}
