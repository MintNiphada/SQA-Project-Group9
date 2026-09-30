package org.jsoup.nodes;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DocumentTest {

    @Test
    public void testConstructor() {
        Document doc = new Document("http://example.com/");
        assertEquals("http://example.com/", doc.baseUri());
        assertEquals("#document", doc.nodeName());
        assertEquals(0, doc.childNodes.size());
    }

    @Test
    public void testCreateShell() {
        Document doc = Document.createShell("http://example.com/");
        assertEquals("http://example.com/", doc.baseUri());
        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertEquals("head", doc.head().tagName());
        assertEquals("body", doc.body().tagName());
        assertEquals("html", doc.select("html").first().tagName());
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

        doc.title("Test Title");
        assertEquals("Test Title", doc.title());
        assertEquals("Test Title", doc.head().getElementsByTag("title").first().text());

        // Update existing title
        doc.title("Updated Title");
        assertEquals("Updated Title", doc.title());
        assertEquals(1, doc.head().getElementsByTag("title").size());

        // Test title trimming
        doc.head().getElementsByTag("title").first().text("  Trimmed Title  ");
        assertEquals("Trimmed Title", doc.title());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTitleSetNull() {
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
        assertEquals(0, doc.childNodes.size());
    }

    @Test
    public void testNormaliseEmptyDoc() {
        Document doc = new Document("http://example.com/");
        Document normalised = doc.normalise();
        assertSame(doc, normalised);

        assertNotNull(doc.select("html").first());
        assertNotNull(doc.head());
        assertNotNull(doc.body());
    }

    @Test
    public void testNormaliseMissingHeadAndBody() {
        Document doc = new Document("http://example.com/");
        doc.appendElement("html");
        doc.normalise();

        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertEquals(2, doc.select("html").first().children().size());
    }

    @Test
    public void testNormaliseMissingBodyOnly() {
        Document doc = new Document("http://example.com/");
        Element html = doc.appendElement("html");
        html.appendElement("head");
        doc.normalise();

        assertNotNull(doc.head());
        assertNotNull(doc.body());
    }

    @Test
    public void testNormaliseMissingHeadOnly() {
        Document doc = new Document("http://example.com/");
        Element html = doc.appendElement("html");
        html.appendElement("body");
        doc.normalise();

        assertNotNull(doc.head());
        assertNotNull(doc.body());
    }

    @Test
    public void testNormaliseMovesTextNodesToBody() {
        Document doc = new Document("http://example.com/");
        doc.appendText("Root Text");
        Element html = doc.appendElement("html");
        html.appendText("HTML Text");
        Element head = html.appendElement("head");
        head.appendText("Head Text");
        Element body = html.appendElement("body");
        body.appendText("Body Text");

        doc.normalise();

        assertEquals(0, doc.head().textNodes().size());
        assertEquals(0, doc.select("html").first().textNodes().size());
        assertEquals(0, doc.textNodes().size());

        String bodyText = doc.body().text();
        assertTrue(bodyText.contains("Body Text"));
        assertTrue(bodyText.contains("Head Text"));
        assertTrue(bodyText.contains("HTML Text"));
        assertTrue(bodyText.contains("Root Text"));
    }

    @Test
    public void testNormaliseIgnoresBlankTextNodes() {
        Document doc = new Document("http://example.com/");
        doc.appendText("   ");
        Element html = doc.appendElement("html");
        html.appendText(" \n\t ");
        Element head = html.appendElement("head");
        head.appendText("   ");
        Element body = html.appendElement("body");

        doc.normalise();

        assertEquals("", body.text());
    }

    @Test
    public void testOuterHtml() {
        Document doc = Document.createShell("http://example.com/");
        doc.title("Hello");
        doc.body().appendElement("p").text("World");

        String expected = "<html>\n <head>\n  <title>Hello</title>\n </head>\n <body>\n  <p>World</p>\n </body>\n</html>";
        assertEquals(expected, doc.outerHtml());
    }

    @Test
    public void testText() {
        Document doc = Document.createShell("http://example.com/");
        doc.body().appendElement("p").text("Old Text");

        Element ret = doc.text("New Text");
        assertSame(doc, ret);
        assertEquals("New Text", doc.body().text());
        assertNotNull(doc.head());
        assertNotNull(doc.body());
    }

    @Test
    public void testNodeName() {
        Document doc = new Document("http://example.com/");
        assertEquals("#document", doc.nodeName());
    }
}
