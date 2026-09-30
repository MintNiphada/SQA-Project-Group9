package org.jsoup.nodes;

import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class DocumentTest {

    @Test
    public void testConstructor() {
        Document doc = new Document("http://example.com/");
        assertEquals("#document", doc.nodeName());
        assertEquals("http://example.com/", doc.baseUri());
        assertNull(doc.head());
        assertNull(doc.body());
        assertEquals("", doc.title());
        assertNotNull(doc.outputSettings());
    }

    @Test
    public void testCreateShell() {
        Document doc = Document.createShell("http://example.com/");
        assertEquals("http://example.com/", doc.baseUri());
        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertEquals("head", doc.head().nodeName());
        assertEquals("body", doc.body().nodeName());
        assertEquals("html", doc.head().parent().nodeName());
        assertEquals("html", doc.body().parent().nodeName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateShellNullUri() {
        Document.createShell(null);
    }

    @Test
    public void testHeadAndBodyAccessors() {
        Document doc = new Document("http://example.com/");
        assertNull(doc.head());
        assertNull(doc.body());

        Element html = doc.appendElement("html");
        Element head = html.appendElement("head");
        Element body = html.appendElement("body");

        assertSame(head, doc.head());
        assertSame(body, doc.body());
    }

    @Test
    public void testTitleGetAndSet() {
        Document doc = Document.createShell("http://example.com/");
        assertEquals("", doc.title());

        doc.title("Hello & World");
        assertEquals("Hello & World", doc.title());
        assertNotNull(doc.head().getElementsByTag("title").first());
        assertEquals("Hello & World", doc.head().getElementsByTag("title").first().text());

        // Update existing title
        doc.title("Updated Title");
        assertEquals("Updated Title", doc.title());
        assertEquals(1, doc.getElementsByTag("title").size());

        // Title with whitespaces is trimmed
        doc.title("   Trimmed Title   ");
        assertEquals("Trimmed Title", doc.title());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetTitleNull() {
        Document doc = Document.createShell("http://example.com/");
        doc.title(null);
    }

    @Test
    public void testCreateElement() {
        Document doc = new Document("http://example.com/");
        Element div = doc.createElement("div");
        assertNotNull(div);
        assertEquals("div", div.tagName());
        assertEquals("http://example.com/", div.baseUri());
        assertEquals(0, doc.children().size());
    }

    @Test
    public void testNormaliseEmptyDoc() {
        Document doc = new Document("http://example.com/");
        Document normalised = doc.normalise();
        assertSame(doc, normalised);
        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertEquals("html", doc.child(0).nodeName());
    }

    @Test
    public void testNormaliseStructure() {
        Document doc = new Document("http://example.com/");
        Element html = doc.appendElement("html");
        // html exists but head and body do not
        doc.normalise();
        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertEquals("head", html.child(0).nodeName());
        assertEquals("body", html.child(1).nodeName());
    }

    @Test
    public void testNormaliseTextNodesMovement() {
        Document doc = new Document("http://example.com/");
        // Add text nodes to root, html, and head
        doc.appendText("Root text");
        doc.appendText("   "); // blank text node should be ignored
        Element html = doc.appendElement("html");
        html.appendText("HTML text");
        Element head = html.appendElement("head");
        head.appendText("Head text");
        Element body = html.appendElement("body");
        body.text("Body text");

        doc.normalise();

        // Ensure text nodes were moved into body
        String bodyText = doc.body().text();
        assertTrue(bodyText.contains("Root text"));
        assertTrue(bodyText.contains("HTML text"));
        assertTrue(bodyText.contains("Head text"));
        assertTrue(bodyText.contains("Body text"));
    }

    @Test
    public void testOuterHtml() {
        Document doc = Document.createShell("http://example.com/");
        doc.body().appendElement("p").text("Test");
        String outerHtml = doc.outerHtml();
        assertFalse(outerHtml.startsWith("#document"));
        assertTrue(outerHtml.contains("<html>"));
        assertTrue(outerHtml.contains("<head>"));
        assertTrue(outerHtml.contains("<body>"));
        assertTrue(outerHtml.contains("<p>Test</p>"));
    }

    @Test
    public void testText() {
        Document doc = Document.createShell("http://example.com/");
        doc.body().appendElement("p").text("Old text");
        Document returned = (Document) doc.text("New body text");
        assertSame(doc, returned);
        assertEquals("New body text", doc.body().text());
        assertNotNull(doc.head());
    }

    @Test
    public void testNodeName() {
        Document doc = new Document("http://example.com/");
        assertEquals("#document", doc.nodeName());
    }

    @Test
    public void testOutputSettingsDefaults() {
        Document.OutputSettings settings = new Document.OutputSettings();
        assertEquals(Entities.EscapeMode.base, settings.escapeMode());
        assertEquals(StandardCharsets.UTF_8, settings.charset());
        assertTrue(settings.prettyPrint());
        assertEquals(1, settings.indentAmount());
        assertNotNull(settings.encoder());
    }

    @Test
    public void testOutputSettingsEscapeMode() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.escapeMode(Entities.EscapeMode.extended);
        assertEquals(Entities.EscapeMode.extended, settings.escapeMode());

        settings.escapeMode(Entities.EscapeMode.base);
        assertEquals(Entities.EscapeMode.base, settings.escapeMode());
    }

    @Test
    public void testOutputSettingsCharset() {
        Document.OutputSettings settings = new Document.OutputSettings();
        
        settings.charset(StandardCharsets.ISO_8859_1);
        assertEquals(StandardCharsets.ISO_8859_1, settings.charset());
        assertEquals(StandardCharsets.ISO_8859_1.name(), settings.encoder().charset().name());

        settings.charset("US-ASCII");
        assertEquals(StandardCharsets.US_ASCII, settings.charset());
        assertEquals(StandardCharsets.US_ASCII.name(), settings.encoder().charset().name());
    }

    @Test
    public void testOutputSettingsPrettyPrint() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.prettyPrint(false);
        assertFalse(settings.prettyPrint());

        settings.prettyPrint(true);
        assertTrue(settings.prettyPrint());
    }

    @Test
    public void testOutputSettingsIndentAmount() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.indentAmount(4);
        assertEquals(4, settings.indentAmount());

        settings.indentAmount(0);
        assertEquals(0, settings.indentAmount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOutputSettingsIndentAmountNegative() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.indentAmount(-1);
    }

    @Test
    public void testDocumentOutputSettingsChaining() {
        Document doc = new Document("http://example.com/");
        doc.outputSettings()
           .escapeMode(Entities.EscapeMode.xhtml)
           .charset(Charset.forName("UTF-8"))
           .prettyPrint(false)
           .indentAmount(2);

        assertEquals(Entities.EscapeMode.xhtml, doc.outputSettings().escapeMode());
        assertEquals(Charset.forName("UTF-8"), doc.outputSettings().charset());
        assertFalse(doc.outputSettings().prettyPrint());
        assertEquals(2, doc.outputSettings().indentAmount());
    }
}
