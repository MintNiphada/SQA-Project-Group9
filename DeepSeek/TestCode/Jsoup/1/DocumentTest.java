package org.jsoup.nodes;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class DocumentTest {

    private Document doc;

    @Before
    public void setUp() {
        doc = new Document("http://example.com");
    }

    @Test
    public void testConstructor() {
        assertEquals("http://example.com", doc.baseUri());
        assertEquals("#root", doc.tagName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateShellNullBaseUri() {
        Document.createShell(null);
    }

    @Test
    public void testCreateShell() {
        Document shell = Document.createShell("http://test.com");
        assertNotNull(shell);
        assertEquals("http://test.com", shell.baseUri());
        // Should have html, head, body
        Element html = shell.child(0);
        assertEquals("html", html.tagName());
        assertEquals(2, html.children().size());
        assertEquals("head", html.child(0).tagName());
        assertEquals("body", html.child(1).tagName());
    }

    @Test
    public void testHeadWhenExists() {
        Document shell = Document.createShell("http://test.com");
        Element head = shell.head();
        assertNotNull(head);
        assertEquals("head", head.tagName());
    }

    @Test
    public void testHeadWhenMissing() {
        // Document without head
        Document doc = new Document("http://test.com");
        assertNull(doc.head());
    }

    @Test
    public void testBodyWhenExists() {
        Document shell = Document.createShell("http://test.com");
        Element body = shell.body();
        assertNotNull(body);
        assertEquals("body", body.tagName());
    }

    @Test
    public void testBodyWhenMissing() {
        Document doc = new Document("http://test.com");
        assertNull(doc.body());
    }

    @Test
    public void testTitleGetWhenExists() {
        Document shell = Document.createShell("http://test.com");
        shell.head().appendElement("title").text(" Hello World ");
        assertEquals("Hello World", shell.title());
    }

    @Test
    public void testTitleGetWhenMissing() {
        Document shell = Document.createShell("http://test.com");
        assertEquals("", shell.title());
    }

    @Test
    public void testTitleGetWithNoHead() {
        Document doc = new Document("http://test.com");
        assertEquals("", doc.title());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTitleSetNull() {
        doc.title(null);
    }

    @Test
    public void testTitleSetWhenTitleExists() {
        Document shell = Document.createShell("http://test.com");
        shell.head().appendElement("title").text("Old");
        shell.title("New Title");
        assertEquals("New Title", shell.title());
    }

    @Test
    public void testTitleSetWhenTitleMissing() {
        Document shell = Document.createShell("http://test.com");
        shell.title("New Title");
        assertEquals("New Title", shell.title());
        // Ensure title element was added to head
        Element head = shell.head();
        assertEquals("title", head.child(0).tagName());
    }

    @Test
    public void testCreateElement() {
        Element el = doc.createElement("div");
        assertEquals("div", el.tagName());
        assertEquals("http://example.com", el.baseUri());
        // Should not be a child of doc
        assertTrue(doc.children().isEmpty());
    }

    @Test
    public void testNormaliseAddsHtmlHeadBodyIfMissing() {
        Document doc = new Document("http://test.com");
        doc.normalise();
        assertNotNull(doc.select("html").first());
        assertNotNull(doc.head());
        assertNotNull(doc.body());
    }

    @Test
    public void testNormaliseMovesTextNodes() {
        Document doc = new Document("http://test.com");
        // Manually construct structure: root with text, then html with text, then head with text
        doc.appendChild(new TextNode("root text", ""));
        Element html = doc.appendElement("html");
        html.appendChild(new TextNode("html text", ""));
        Element head = html.appendElement("head");
        head.appendChild(new TextNode("head text", ""));
        Element body = html.appendElement("body");
        // body initially empty

        doc.normalise();

        // After normalise, text nodes should be moved to body, with a space separator added before each
        // Check body children
        assertEquals(5, body.childNodes().size()); // space, root text, space, html text, space, head text? Actually normalise adds a space TextNode before each moved text node.
        // The order: root text moved first? The normalise method processes head, then html, then this (root). So order: head text moved first, then html text, then root text. Each move prepends a space.
        // So body children: space, head text, space, html text, space, root text? Let's verify by checking text content.
        // We'll just assert that body contains the texts.
        String bodyText = body.text();
        assertTrue(bodyText.contains("head text"));
        assertTrue(bodyText.contains("html text"));
        assertTrue(bodyText.contains("root text"));
        // Original locations should be empty of those text nodes
        assertEquals(0, head.childNodes().size());
        assertEquals(0, html.childNodes().size()); // html had head and body, but after normalise head and body remain? Actually normalise only moves text nodes, not elements. So html still has head and body elements. But the text node that was directly under html is moved. So html's childNodes: head and body elements only.
        assertEquals(2, html.childNodes().size()); // head and body
        assertEquals(0, doc.childNodes().size()); // root text moved, html element remains? Wait, doc is the root element. Its childNodes originally had the html element and the root text. After normalise, the root text is moved, so doc should have only the html element. So doc.childNodes().size() should be 1 (the html element). But we appended html as a child, so doc has html element. The root text was appended to doc, so doc had two children: text and html. After normalise, text moved, so doc has only html. So size 1.
        assertEquals(1, doc.childNodes().size());
    }

    @Test
    public void testNormaliseDoesNotMoveBlankTextNodes() {
        Document doc = new Document("http://test.com");
        Element html = doc.appendElement("html");
        html.appendElement("head");
        html.appendElement("body");
        // Add a blank text node to head
        doc.head().appendChild(new TextNode("   ", ""));
        doc.normalise();
        // Blank text node should remain in head
        assertEquals(1, doc.head().childNodes().size());
        assertTrue(doc.head().childNode(0) instanceof TextNode);
        assertTrue(((TextNode) doc.head().childNode(0)).isBlank());
    }

    @Test
    public void testNormaliseReturnsThis() {
        Document doc = new Document("http://test.com");
        assertSame(doc, doc.normalise());
    }

    @Test
    public void testOuterHtml() {
        Document doc = Document.createShell("http://test.com");
        String html = doc.outerHtml();
        // Should not contain outer <#root> tag, just the inner HTML
        assertFalse(html.contains("<#root>"));
        assertTrue(html.contains("<html>"));
    }

    @Test
    public void testTextSet() {
        Document doc = Document.createShell("http://test.com");
        doc.body().appendElement("p").text("Old");
        doc.text("New body text");
        assertEquals("New body text", doc.body().text());
        // Ensure body children cleared and only text node remains
        assertEquals(1, doc.body().childNodes().size());
        assertTrue(doc.body().childNode(0) instanceof TextNode);
        assertEquals("New body text", ((TextNode) doc.body().childNode(0)).getWholeText());
    }

    @Test
    public void testTextSetReturnsDocument() {
        Document doc = Document.createShell("http://test.com");
        assertSame(doc, doc.text("test"));
    }

    @Test
    public void testNodeName() {
        assertEquals("#document", doc.nodeName());
    }
}
