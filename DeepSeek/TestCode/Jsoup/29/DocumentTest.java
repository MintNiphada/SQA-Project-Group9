package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;
import org.jsoup.helper.Validate;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import java.nio.charset.Charset;
import java.util.List;

public class DocumentTest {

    @Test
    public void testConstructor() {
        Document doc = new Document("http://example.com");
        assertEquals("#document", doc.nodeName());
        assertEquals("http://example.com", doc.baseUri());
        assertNotNull(doc.outputSettings());
        assertEquals(Document.QuirksMode.noQuirks, doc.quirksMode());
    }

    @Test
    public void testCreateShell() {
        Document doc = Document.createShell("http://example.com");
        assertNotNull(doc);
        assertEquals("http://example.com", doc.baseUri());
        Element html = doc.child(0);
        assertEquals("html", html.tagName());
        assertEquals("head", html.child(0).tagName());
        assertEquals("body", html.child(1).tagName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateShellNullBaseUri() {
        Document.createShell(null);
    }

    @Test
    public void testHead() {
        Document doc = Document.createShell("http://example.com");
        Element head = doc.head();
        assertNotNull(head);
        assertEquals("head", head.tagName());
    }

    @Test
    public void testHeadMissing() {
        Document doc = new Document("http://example.com");
        assertNull(doc.head());
    }

    @Test
    public void testBody() {
        Document doc = Document.createShell("http://example.com");
        Element body = doc.body();
        assertNotNull(body);
        assertEquals("body", body.tagName());
    }

    @Test
    public void testBodyMissing() {
        Document doc = new Document("http://example.com");
        assertNull(doc.body());
    }

    @Test
    public void testTitleGetWhenExists() {
        Document doc = Document.createShell("http://example.com");
        Element head = doc.head();
        head.appendElement("title").text("Test Title");
        assertEquals("Test Title", doc.title());
    }

    @Test
    public void testTitleGetWhenNotExists() {
        Document doc = Document.createShell("http://example.com");
        assertEquals("", doc.title());
    }

    @Test
    public void testTitleSetWhenExists() {
        Document doc = Document.createShell("http://example.com");
        doc.head().appendElement("title").text("Old Title");
        doc.title("New Title");
        assertEquals("New Title", doc.title());
    }

    @Test
    public void testTitleSetWhenNotExists() {
        Document doc = Document.createShell("http://example.com");
        doc.title("New Title");
        assertEquals("New Title", doc.title());
        assertNotNull(doc.head().select("title").first());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTitleSetNull() {
        Document doc = Document.createShell("http://example.com");
        doc.title(null);
    }

    @Test
    public void testCreateElement() {
        Document doc = new Document("http://example.com");
        Element el = doc.createElement("div");
        assertEquals("div", el.tagName());
        assertEquals("http://example.com", el.baseUri());
        assertNull(el.parent());
    }

    @Test
    public void testNormaliseWithMissingHtmlHeadBody() {
        Document doc = new Document("http://example.com");
        doc.normalise();
        assertNotNull(doc.head());
        assertNotNull(doc.body());
        Element html = doc.child(0);
        assertEquals("html", html.tagName());
        assertEquals("head", html.child(0).tagName());
        assertEquals("body", html.child(1).tagName());
    }

    @Test
    public void testNormaliseMovesTextNodes() {
        Document doc = new Document("http://example.com");
        doc.appendText("Root text");
        Element html = doc.appendElement("html");
        html.appendText("Html text");
        Element head = html.appendElement("head");
        head.appendText("Head text");
        html.appendElement("body");
        doc.normalise();
        Element body = doc.body();
        List<Node> bodyChildren = body.childNodes();
        assertTrue(bodyChildren.size() >= 3);
        boolean foundRootText = false;
        boolean foundHtmlText = false;
        boolean foundHeadText = false;
        for (Node node : bodyChildren) {
            if (node instanceof TextNode) {
                String text = ((TextNode) node).getWholeText();
                if (text.contains("Root text")) foundRootText = true;
                if (text.contains("Html text")) foundHtmlText = true;
                if (text.contains("Head text")) foundHeadText = true;
            }
        }
        assertTrue(foundRootText);
        assertTrue(foundHtmlText);
        assertTrue(foundHeadText);
    }

    @Test
    public void testNormaliseTextNodesIgnoresBlank() {
        Document doc = Document.createShell("http://example.com");
        Element head = doc.head();
        head.appendText("   ");
        head.appendText("Non blank");
        doc.normalise();
        Element body = doc.body();
        boolean hasNonBlank = false;
        for (Node node : body.childNodes()) {
            if (node instanceof TextNode && ((TextNode) node).getWholeText().contains("Non blank")) {
                hasNonBlank = true;
                break;
            }
        }
        assertTrue(hasNonBlank);
    }

    @Test
    public void testNormaliseStructureMultipleHead() {
        Document doc = Document.createShell("http://example.com");
        Element html = doc.child(0);
        Element head1 = html.child(0);
        head1.appendElement("title").text("First");
        Element head2 = new Element(Tag.valueOf("head"), doc.baseUri());
        head2.appendElement("meta");
        html.appendChild(head2);
        doc.normalise();
        Elements heads = doc.getElementsByTag("head");
        assertEquals(1, heads.size());
        Element masterHead = heads.first();
        assertTrue(masterHead.getElementsByTag("title").size() > 0);
        assertTrue(masterHead.getElementsByTag("meta").size() > 0);
    }

    @Test
    public void testNormaliseStructureMultipleBody() {
        Document doc = Document.createShell("http://example.com");
        Element html = doc.child(0);
        Element body1 = html.child(1);
        body1.appendElement("p").text("First");
        Element body2 = new Element(Tag.valueOf("body"), doc.baseUri());
        body2.appendElement("div").text("Second");
        html.appendChild(body2);
        doc.normalise();
        Elements bodies = doc.getElementsByTag("body");
        assertEquals(1, bodies.size());
        Element masterBody = bodies.first();
        assertTrue(masterBody.getElementsByTag("p").size() > 0);
        assertTrue(masterBody.getElementsByTag("div").size() > 0);
    }

    @Test
    public void testNormaliseStructureParentNotHtml() {
        Document doc = Document.createShell("http://example.com");
        Element html = doc.child(0);
        Element head = html.child(0);
        html.removeChild(head);
        doc.appendChild(head);
        doc.normalise();
        assertEquals(html, head.parent());
    }

    @Test
    public void testOuterHtml() {
        Document doc = Document.createShell("http://example.com");
        String html = doc.outerHtml();
        assertTrue(html.contains("<html"));
        assertTrue(html.contains("<head"));
        assertTrue(html.contains("<body"));
    }

    @Test
    public void testTextSetter() {
        Document doc = Document.createShell("http://example.com");
        doc.text("Hello World");
        assertEquals("Hello World", doc.body().text());
    }

    @Test
    public void testNodeName() {
        Document doc = new Document("http://example.com");
        assertEquals("#document", doc.nodeName());
    }

    @Test
    public void testClone() {
        Document doc = Document.createShell("http://example.com");
        doc.outputSettings().prettyPrint(false);
        Document clone = doc.clone();
        assertNotSame(doc, clone);
        assertEquals(doc.baseUri(), clone.baseUri());
        assertNotSame(doc.outputSettings(), clone.outputSettings());
        assertEquals(doc.outputSettings().prettyPrint(), clone.outputSettings().prettyPrint());
    }

    @Test
    public void testOutputSettingsDefault() {
        Document doc = new Document("http://example.com");
        Document.OutputSettings settings = doc.outputSettings();
        assertEquals(Entities.EscapeMode.base, settings.escapeMode());
        assertEquals(Charset.forName("UTF-8"), settings.charset());
        assertTrue(settings.prettyPrint());
        assertEquals(1, settings.indentAmount());
    }

    @Test
    public void testOutputSettingsEscapeMode() {
        Document doc = new Document("http://example.com");
        Document.OutputSettings settings = doc.outputSettings();
        settings.escapeMode(Entities.EscapeMode.extended);
        assertEquals(Entities.EscapeMode.extended, settings.escapeMode());
    }

    @Test
    public void testOutputSettingsCharsetByCharset() {
        Document doc = new Document("http://example.com");
        Document.OutputSettings settings = doc.outputSettings();
        Charset iso = Charset.forName("ISO-8859-1");
        settings.charset(iso);
        assertEquals(iso, settings.charset());
    }

    @Test
    public void testOutputSettingsCharsetByName() {
        Document doc = new Document("http://example.com");
        Document.OutputSettings settings = doc.outputSettings();
        settings.charset("ISO-8859-1");
        assertEquals(Charset.forName("ISO-8859-1"), settings.charset());
    }

    @Test
    public void testOutputSettingsPrettyPrint() {
        Document doc = new Document("http://example.com");
        Document.OutputSettings settings = doc.outputSettings();
        settings.prettyPrint(false);
        assertFalse(settings.prettyPrint());
    }

    @Test
    public void testOutputSettingsIndentAmount() {
        Document doc = new Document("http://example.com");
        Document.OutputSettings settings = doc.outputSettings();
        settings.indentAmount(4);
        assertEquals(4, settings.indentAmount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOutputSettingsIndentAmountNegative() {
        Document doc = new Document("http://example.com");
        doc.outputSettings().indentAmount(-1);
    }

    @Test
    public void testOutputSettingsClone() {
        Document.OutputSettings original = new Document.OutputSettings();
        original.charset("ISO-8859-1");
        original.escapeMode(Entities.EscapeMode.extended);
        original.prettyPrint(false);
        original.indentAmount(2);
        Document.OutputSettings clone = original.clone();
        assertNotSame(original, clone);
        assertEquals(original.escapeMode(), clone.escapeMode());
        assertEquals(original.charset(), clone.charset());
        assertEquals(original.prettyPrint(), clone.prettyPrint());
        assertEquals(original.indentAmount(), clone.indentAmount());
        assertNotSame(original.encoder(), clone.encoder());
    }

    @Test
    public void testSetOutputSettings() {
        Document doc = new Document("http://example.com");
        Document.OutputSettings newSettings = new Document.OutputSettings();
        newSettings.prettyPrint(false);
        doc.outputSettings(newSettings);
        assertSame(newSettings, doc.outputSettings());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetOutputSettingsNull() {
        Document doc = new Document("http://example.com");
        doc.outputSettings(null);
    }

    @Test
    public void testQuirksMode() {
        Document doc = new Document("http://example.com");
        assertEquals(Document.QuirksMode.noQuirks, doc.quirksMode());
        doc.quirksMode(Document.QuirksMode.quirks);
        assertEquals(Document.QuirksMode.quirks, doc.quirksMode());
        doc.quirksMode(Document.QuirksMode.limitedQuirks);
        assertEquals(Document.QuirksMode.limitedQuirks, doc.quirksMode());
    }

    @Test
    public void testFindFirstElementByTagNameRecursive() {
        Document doc = new Document("http://example.com");
        Element div = doc.appendElement("div");
        Element span = div.appendElement("span");
        span.appendElement("p");
        Element found = doc.findFirstElementByTagName("p", doc);
        assertNotNull(found);
        assertEquals("p", found.tagName());
    }

    @Test
    public void testFindFirstElementByTagNameNotFound() {
        Document doc = new Document("http://example.com");
        assertNull(doc.findFirstElementByTagName("table", doc));
    }

    @Test
    public void testNormaliseTextNodesOrder() {
        Document doc = new Document("http://example.com");
        doc.appendText("First");
        Element html = doc.appendElement("html");
        html.appendText("Second");
        html.appendElement("head");
        html.appendElement("body");
        doc.normalise();
        Element body = doc.body();
        List<Node> children = body.childNodes();
        StringBuilder sb = new StringBuilder();
        for (Node child : children) {
            if (child instanceof TextNode) {
                sb.append(((TextNode) child).getWholeText());
            }
        }
        String text = sb.toString().replaceAll("\\s+", " ").trim();
        assertTrue(text.contains("First"));
        assertTrue(text.contains("Second"));
        assertTrue(text.indexOf("First") < text.indexOf("Second"));
    }
}
