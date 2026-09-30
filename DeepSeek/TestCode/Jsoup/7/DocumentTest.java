package org.jsoup.nodes;

import org.jsoup.helper.Validate;
import org.jsoup.parser.Tag;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.List;

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
        assertNotNull(doc.outputSettings());
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
        Element html = shell.child(0);
        assertEquals("html", html.tagName());
        assertEquals(2, html.children().size());
        assertEquals("head", html.child(0).tagName());
        assertEquals("body", html.child(1).tagName());
    }

    @Test
    public void testHeadWhenPresent() {
        Document shell = Document.createShell("http://test.com");
        Element head = shell.head();
        assertNotNull(head);
        assertEquals("head", head.tagName());
    }

    @Test
    public void testHeadWhenAbsent() {
        assertNull(doc.head());
    }

    @Test
    public void testBodyWhenPresent() {
        Document shell = Document.createShell("http://test.com");
        Element body = shell.body();
        assertNotNull(body);
        assertEquals("body", body.tagName());
    }

    @Test
    public void testBodyWhenAbsent() {
        assertNull(doc.body());
    }

    @Test
    public void testTitleGetterNoTitle() {
        assertEquals("", doc.title());
    }

    @Test
    public void testTitleGetterWithTitle() {
        Document shell = Document.createShell("http://test.com");
        shell.head().appendElement("title").text(" Hello World ");
        assertEquals("Hello World", shell.title());
    }

    @Test
    public void testTitleGetterEmptyTitle() {
        Document shell = Document.createShell("http://test.com");
        shell.head().appendElement("title").text("");
        assertEquals("", shell.title());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTitleSetterNull() {
        doc.title(null);
    }

    @Test
    public void testTitleSetterAddsTitleWhenAbsent() {
        Document shell = Document.createShell("http://test.com");
        shell.title("New Title");
        assertEquals("New Title", shell.title());
        Element titleEl = shell.head().getElementsByTag("title").first();
        assertNotNull(titleEl);
        assertEquals("New Title", titleEl.text());
    }

    @Test
    public void testTitleSetterUpdatesExistingTitle() {
        Document shell = Document.createShell("http://test.com");
        shell.head().appendElement("title").text("Old");
        shell.title("New");
        assertEquals("New", shell.title());
        Element titleEl = shell.head().getElementsByTag("title").first();
        assertEquals("New", titleEl.text());
    }

    @Test
    public void testCreateElement() {
        Element el = doc.createElement("div");
        assertEquals("div", el.tagName());
        assertEquals("http://example.com", el.baseUri());
        assertFalse(doc.children().contains(el)); // not added as child
    }

    @Test
    public void testNormaliseCreatesHtmlHeadBodyIfMissing() {
        Document d = new Document("http://test.com");
        d.normalise();
        Element html = d.child(0);
        assertNotNull(html);
        assertEquals("html", html.tagName());
        assertNotNull(d.head());
        assertNotNull(d.body());
    }

    @Test
    public void testNormaliseMovesTextNodesFromRoot() {
        Document d = new Document("http://test.com");
        d.appendChild(new TextNode("Root text", "http://test.com"));
        d.normalise();
        // root text should be moved to body
        Element body = d.body();
        List<Node> bodyChildren = body.childNodes();
        // body should have at least the text node, possibly with a space before it
        assertTrue(bodyChildren.size() >= 1);
        boolean found = false;
        for (Node child : bodyChildren) {
            if (child instanceof TextNode && ((TextNode) child).getWholeText().equals("Root text")) {
                found = true;
                break;
            }
        }
        assertTrue("Root text not moved to body", found);
        // root should no longer have that text node
        assertFalse(d.childNodes().contains(new TextNode("Root text", "http://test.com")));
    }

    @Test
    public void testNormaliseMovesTextNodesFromHead() {
        Document d = Document.createShell("http://test.com");
        d.head().appendChild(new TextNode("Head text", "http://test.com"));
        d.normalise();
        Element body = d.body();
        List<Node> bodyChildren = body.childNodes();
        boolean found = false;
        for (Node child : bodyChildren) {
            if (child instanceof TextNode && ((TextNode) child).getWholeText().equals("Head text")) {
                found = true;
                break;
            }
        }
        assertTrue("Head text not moved to body", found);
        // head should no longer have that text node
        assertFalse(d.head().childNodes().contains(new TextNode("Head text", "http://test.com")));
    }

    @Test
    public void testNormaliseMovesTextNodesFromHtml() {
        Document d = Document.createShell("http://test.com");
        Element html = d.child(0);
        html.appendChild(new TextNode("Html text", "http://test.com"));
        d.normalise();
        Element body = d.body();
        boolean found = false;
        for (Node child : body.childNodes()) {
            if (child instanceof TextNode && ((TextNode) child).getWholeText().equals("Html text")) {
                found = true;
                break;
            }
        }
        assertTrue("Html text not moved to body", found);
        assertFalse(html.childNodes().contains(new TextNode("Html text", "http://test.com")));
    }

    @Test
    public void testNormaliseDoesNotMoveBlankTextNodes() {
        Document d = Document.createShell("http://test.com");
        d.head().appendChild(new TextNode("   ", "http://test.com"));
        d.normalise();
        // blank text node should remain in head
        assertTrue(d.head().childNodes().stream().anyMatch(n -> n instanceof TextNode && ((TextNode) n).isBlank()));
        // body should not have that blank text node
        assertFalse(d.body().childNodes().stream().anyMatch(n -> n instanceof TextNode && ((TextNode) n).isBlank()));
    }

    @Test
    public void testNormaliseMaintainsTextOrder() {
        Document d = Document.createShell("http://test.com");
        d.head().appendChild(new TextNode("First", "http://test.com"));
        d.head().appendChild(new TextNode("Second", "http://test.com"));
        d.normalise();
        List<Node> bodyChildren = d.body().childNodes();
        // The text nodes are prepended in reverse order, each preceded by a space.
        // So body should have: space, "Second", space, "First" (and possibly other nodes)
        // We'll check that "First" appears after "Second" in the body.
        int indexFirst = -1, indexSecond = -1;
        for (int i = 0; i < bodyChildren.size(); i++) {
            Node n = bodyChildren.get(i);
            if (n instanceof TextNode) {
                String text = ((TextNode) n).getWholeText();
                if ("First".equals(text)) indexFirst = i;
                if ("Second".equals(text)) indexSecond = i;
            }
        }
        assertTrue(indexFirst > indexSecond);
    }

    @Test
    public void testNormaliseReturnsThis() {
        Document d = new Document("http://test.com");
        assertSame(d, d.normalise());
    }

    @Test
    public void testOuterHtml() {
        Document d = Document.createShell("http://test.com");
        d.body().appendElement("p").text("Hello");
        String html = d.outerHtml();
        assertTrue(html.contains("<html>"));
        assertTrue(html.contains("<head>"));
        assertTrue(html.contains("<body>"));
        assertTrue(html.contains("<p>Hello</p>"));
    }

    @Test
    public void testTextSetter() {
        Document d = Document.createShell("http://test.com");
        d.body().appendElement("p").text("Old");
        d.text("New text");
        assertEquals("New text", d.body().text());
        // body should have only the text node
        assertEquals(1, d.body().childNodes().size());
        assertTrue(d.body().childNode(0) instanceof TextNode);
        assertEquals("New text", ((TextNode) d.body().childNode(0)).getWholeText());
    }

    @Test
    public void testTextSetterReturnsDocument() {
        Document d = Document.createShell("http://test.com");
        assertSame(d, d.text("test"));
    }

    @Test
    public void testNodeName() {
        assertEquals("#document", doc.nodeName());
    }

    @Test
    public void testOutputSettings() {
        assertNotNull(doc.outputSettings());
        assertSame(doc.outputSettings(), doc.outputSettings());
    }

    // OutputSettings tests
    @Test
    public void testOutputSettingsDefaultValues() {
        Document.OutputSettings settings = doc.outputSettings();
        assertEquals(Entities.EscapeMode.base, settings.escapeMode());
        assertEquals(Charset.forName("UTF-8"), settings.charset());
        assertTrue(settings.prettyPrint());
        assertEquals(1, settings.indentAmount());
    }

    @Test
    public void testOutputSettingsEscapeMode() {
        Document.OutputSettings settings = doc.outputSettings();
        settings.escapeMode(Entities.EscapeMode.extended);
        assertEquals(Entities.EscapeMode.extended, settings.escapeMode());
        assertSame(settings, settings.escapeMode(Entities.EscapeMode.base)); // chaining
    }

    @Test
    public void testOutputSettingsCharsetByCharset() {
        Document.OutputSettings settings = doc.outputSettings();
        Charset iso = Charset.forName("ISO-8859-1");
        settings.charset(iso);
        assertEquals(iso, settings.charset());
        assertNotNull(settings.encoder());
        assertSame(settings, settings.charset(Charset.forName("UTF-8")));
    }

    @Test
    public void testOutputSettingsCharsetByName() {
        Document.OutputSettings settings = doc.outputSettings();
        settings.charset("ISO-8859-1");
        assertEquals(Charset.forName("ISO-8859-1"), settings.charset());
        assertSame(settings, settings.charset("UTF-8"));
    }

    @Test
    public void testOutputSettingsEncoder() {
        Document.OutputSettings settings = doc.outputSettings();
        CharsetEncoder encoder = settings.encoder();
        assertNotNull(encoder);
        assertEquals(Charset.forName("UTF-8").newEncoder().charset(), encoder.charset());
    }

    @Test
    public void testOutputSettingsPrettyPrint() {
        Document.OutputSettings settings = doc.outputSettings();
        settings.prettyPrint(false);
        assertFalse(settings.prettyPrint());
        assertSame(settings, settings.prettyPrint(true));
    }

    @Test
    public void testOutputSettingsIndentAmount() {
        Document.OutputSettings settings = doc.outputSettings();
        settings.indentAmount(4);
        assertEquals(4, settings.indentAmount());
        assertSame(settings, settings.indentAmount(2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOutputSettingsIndentAmountNegative() {
        doc.outputSettings().indentAmount(-1);
    }

    @Test
    public void testOutputSettingsIndentAmountZero() {
        Document.OutputSettings settings = doc.outputSettings();
        settings.indentAmount(0);
        assertEquals(0, settings.indentAmount());
    }
}
