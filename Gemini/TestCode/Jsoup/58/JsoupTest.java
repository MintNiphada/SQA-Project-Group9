package org.jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Whitelist;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class JsoupTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testPrivateConstructor() throws Exception {
        Constructor<Jsoup> constructor = Jsoup.class.getDeclaredConstructor();
        Assert.assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        Jsoup instance = constructor.newInstance();
        Assert.assertNotNull(instance);
    }

    @Test
    public void testParseHtml() {
        Document doc = Jsoup.parse("<html><head><title>Test</title></head><body><p>Hello</p></body></html>");
        Assert.assertEquals("Test", doc.title());
        Assert.assertEquals("Hello", doc.select("p").first().text());
        Assert.assertEquals("", doc.baseUri());
    }

    @Test
    public void testParseHtmlWithBaseUri() {
        Document doc = Jsoup.parse("<a href='/link'>Link</a>", "http://example.com/");
        Element link = doc.select("a").first();
        Assert.assertEquals("http://example.com/link", link.attr("abs:href"));
        Assert.assertEquals("http://example.com/", doc.baseUri());
    }

    @Test
    public void testParseHtmlWithBaseUriAndParser() {
        String xml = "<root><child id='1'>Text</child></root>";
        Document doc = Jsoup.parse(xml, "http://example.com/", Parser.xmlParser());
        Assert.assertEquals("Text", doc.select("child").first().text());
        Assert.assertEquals("http://example.com/", doc.baseUri());
    }

    @Test
    public void testConnect() {
        Connection con = Jsoup.connect("http://example.com");
        Assert.assertNotNull(con);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConnectInvalidProtocol() {
        Jsoup.connect("ftp://example.com");
    }

    @Test
    public void testParseFileWithCharsetAndBaseUri() throws IOException {
        File file = tempFolder.newFile("test.html");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write("<html><body><a href='sub'>Test</a></body></html>".getBytes(StandardCharsets.UTF_8));
        }

        Document doc = Jsoup.parse(file, "UTF-8", "http://example.com/");
        Assert.assertEquals("http://example.com/sub", doc.select("a").first().attr("abs:href"));
    }

    @Test
    public void testParseFileWithCharset() throws IOException {
        File file = tempFolder.newFile("test_abs.html");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write("<html><body><p>File Content</p></body></html>".getBytes(StandardCharsets.UTF_8));
        }

        Document doc = Jsoup.parse(file, "UTF-8");
        Assert.assertEquals("File Content", doc.select("p").first().text());
        Assert.assertEquals(file.getAbsolutePath(), doc.baseUri());
    }

    @Test
    public void testParseInputStreamWithCharsetAndBaseUri() throws IOException {
        String html = "<html><body><a href='/path'>Stream</a></body></html>";
        InputStream stream = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));
        Document doc = Jsoup.parse(stream, "UTF-8", "http://example.com/");
        Assert.assertEquals("http://example.com/path", doc.select("a").first().attr("abs:href"));
    }

    @Test
    public void testParseInputStreamWithParser() throws IOException {
        String xml = "<xml><data>Value</data></xml>";
        InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        Document doc = Jsoup.parse(stream, "UTF-8", "http://example.com/", Parser.xmlParser());
        Assert.assertEquals("Value", doc.select("data").first().text());
    }

    @Test
    public void testParseBodyFragment() {
        Document doc = Jsoup.parseBodyFragment("<p>Fragment</p>");
        Assert.assertEquals("Fragment", doc.body().text());
        Assert.assertEquals("", doc.baseUri());
    }

    @Test
    public void testParseBodyFragmentWithBaseUri() {
        Document doc = Jsoup.parseBodyFragment("<a href='relative'>Fragment Link</a>", "http://example.com/dir/");
        Assert.assertEquals("http://example.com/dir/relative", doc.select("a").first().attr("abs:href"));
    }

    @Test
    public void testParseUrl() {
        try {
            URL url = new URL("http://localhost:1");
            Jsoup.parse(url, 100);
            Assert.fail();
        } catch (IOException e) {
            Assert.assertNotNull(e);
        }
    }

    @Test(expected = MalformedURLException.class)
    public void testParseMalformedUrl() throws IOException {
        URL url = new URL("ftp://localhost");
        Jsoup.parse(url, 1000);
    }

    @Test
    public void testCleanBodyHtmlAndWhitelist() {
        String unsafe = "<p><a href='http://example.com/' onclick='steal()'>Link</a><script>alert(1)</script></p>";
        String safe = Jsoup.clean(unsafe, Whitelist.basic());
        Assert.assertEquals("<p><a href=\"http://example.com/\" rel=\"nofollow\">Link</a></p>", safe);
    }

    @Test
    public void testCleanBodyHtmlBaseUriAndWhitelist() {
        String unsafe = "<a href='/contact'>Contact</a>";
        String safe = Jsoup.clean(unsafe, "http://example.com/", Whitelist.basic());
        Assert.assertEquals("<a href=\"http://example.com/contact\" rel=\"nofollow\">Contact</a>", safe);
    }

    @Test
    public void testCleanBodyHtmlBaseUriWhitelistAndOutputSettings() {
        String unsafe = "<p>Test &amp; Check</p>";
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.prettyPrint(false);
        String safe = Jsoup.clean(unsafe, "http://example.com/", Whitelist.basic(), settings);
        Assert.assertEquals("<p>Test &amp; Check</p>", safe);
    }

    @Test
    public void testIsValid() {
        Assert.assertTrue(Jsoup.isValid("<p>Safe <b>text</b></p>", Whitelist.basic()));
        Assert.assertFalse(Jsoup.isValid("<script>alert('bad');</script>", Whitelist.basic()));
        Assert.assertFalse(Jsoup.isValid("<p><a href='javascript:void(0)'>Link</a></p>", Whitelist.basic()));
    }
}
