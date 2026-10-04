package org.jsoup.nodes;

import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.Charset;

public class DocumentTest {

    @Test
    public void testCreateShell() {
        Document doc = Document.createShell("http://example.com/");
        Assert.assertEquals("http://example.com/", doc.baseUri());
        Assert.assertEquals("#document", doc.nodeName());
        Assert.assertNotNull(doc.head());
        Assert.assertEquals("head", doc.head().tagName());
        Assert.assertNotNull(doc.body());
        Assert.assertEquals("body", doc.body().tagName());
        Assert.assertEquals("<html>\n <head></head>\n <body></body>\n</html>", doc.outerHtml());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateShellNullUri() {
        Document.createShell(null);
    }

    @Test
    public void testTitle() {
        Document doc = Document.createShell("http://example.com/");
        Assert.assertEquals("", doc.title());

        doc.title("Test Title");
        Assert.assertEquals("Test Title", doc.title());
        Assert.assertEquals("Test Title", doc.head().getElementsByTag("title").first().text());

        doc.title("Updated Title");
        Assert.assertEquals("Updated Title", doc.title());

        doc.head().getElementsByTag("title").first().text("  Trimmed Title  ");
        Assert.assertEquals("Trimmed Title", doc.title());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTitleNull() {
        Document doc = Document.createShell("http://example.com/");
        doc.title(null);
    }

    @Test
    public void testCreateElement() {
        Document doc = new Document("http://example.com/");
        Element div = doc.createElement("div");
        Assert.assertEquals("div", div.tagName());
        Assert.assertEquals("http://example.com/", div.baseUri());
        Assert.assertNull(div.parent());
    }

    @Test
    public void testNormaliseEmptyDoc() {
        Document doc = new Document("http://example.com/");
        doc.normalise();
        Assert.assertNotNull(doc.head());
        Assert.assertNotNull(doc.body());
        Assert.assertEquals("html", doc.child(0).tagName());
    }

    @Test
    public void testNormaliseWithTextNodes() {
        Document doc = new Document("http://example.com/");
        doc.appendText("Text in root");
        Element html = doc.appendElement("html");
        html.appendText("Text in html");
        Element head = html.appendElement("head");
        head.appendText("Text in head");

        doc.normalise();

        Assert.assertEquals("Text in head Text in html Text in root", doc.body().text());
        Assert.assertEquals(0, head.textNodes().size());
    }

    @Test
    public void testNormaliseDuplicateStructures() {
        Document doc = new Document("http://example.com/");
        Element html = doc.appendElement("html");
        Element head1 = html.appendElement("head");
        head1.appendElement("meta").attr("name", "first");
        Element head2 = html.appendElement("head");
        head2.appendElement("meta").attr("name", "second");

        Element body1 = html.appendElement("body");
        body1.appendElement("p").text("First body");
        Element body2 = html.appendElement("body");
        body2.appendElement("p").text("Second body");

        Element extraHead = doc.appendElement("head");
        extraHead.appendElement("meta").attr("name", "third");

        doc.normalise();

        Assert.assertEquals(1, doc.getElementsByTag("head").size());
        Assert.assertEquals(1, doc.getElementsByTag("body").size());
        Assert.assertEquals(3, doc.head().getElementsByTag("meta").size());
        Assert.assertEquals(2, doc.body().getElementsByTag("p").size());
        Assert.assertEquals(html, doc.head().parent());
        Assert.assertEquals(html, doc.body().parent());
    }

    @Test
    public void testTextSetter() {
        Document doc = Document.createShell("http://example.com/");
        doc.text("Hello World");
        Assert.assertEquals("Hello World", doc.body().text());
        Assert.assertEquals("Hello World", doc.text());
        Assert.assertNotNull(doc.head());
    }

    @Test
    public void testClone() {
        Document doc = Document.createShell("http://example.com/");
        doc.title("Original");
        doc.outputSettings().indentAmount(4).prettyPrint(false);
        doc.quirksMode(Document.QuirksMode.quirks);

        Document clone = doc.clone();
        Assert.assertNotSame(doc, clone);
        Assert.assertNotSame(doc.outputSettings(), clone.outputSettings());
        Assert.assertEquals(doc.title(), clone.title());
        Assert.assertEquals(4, clone.outputSettings().indentAmount());
        Assert.assertFalse(clone.outputSettings().prettyPrint());
        Assert.assertEquals(Document.QuirksMode.quirks, clone.quirksMode());

        clone.title("Modified");
        Assert.assertEquals("Original", doc.title());
        Assert.assertEquals("Modified", clone.title());
    }

    @Test
    public void testOutputSettings() {
        Document.OutputSettings settings = new Document.OutputSettings();

        Assert.assertEquals(Entities.EscapeMode.base, settings.escapeMode());
        settings.escapeMode(Entities.EscapeMode.extended);
        Assert.assertEquals(Entities.EscapeMode.extended, settings.escapeMode());

        Assert.assertEquals(Charset.forName("UTF-8"), settings.charset());
        settings.charset("ISO-8859-1");
        Assert.assertEquals(Charset.forName("ISO-8859-1"), settings.charset());
        Assert.assertEquals("ISO-8859-1", settings.encoder().charset().name());

        settings.charset(Charset.forName("US-ASCII"));
        Assert.assertEquals(Charset.forName("US-ASCII"), settings.charset());

        Assert.assertTrue(settings.prettyPrint());
        settings.prettyPrint(false);
        Assert.assertFalse(settings.prettyPrint());

        Assert.assertEquals(1, settings.indentAmount());
        settings.indentAmount(2);
        Assert.assertEquals(2, settings.indentAmount());

        Document.OutputSettings cloned = settings.clone();
        Assert.assertNotSame(settings, cloned);
        Assert.assertEquals(settings.escapeMode(), cloned.escapeMode());
        Assert.assertEquals(settings.charset(), cloned.charset());
        Assert.assertEquals(settings.prettyPrint(), cloned.prettyPrint());
        Assert.assertEquals(settings.indentAmount(), cloned.indentAmount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOutputSettingsNegativeIndent() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.indentAmount(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullOutputSettings() {
        Document doc = new Document("http://example.com/");
        doc.outputSettings(null);
    }

    @Test
    public void testQuirksMode() {
        Document doc = new Document("http://example.com/");
        Assert.assertEquals(Document.QuirksMode.noQuirks, doc.quirksMode());

        doc.quirksMode(Document.QuirksMode.limitedQuirks);
        Assert.assertEquals(Document.QuirksMode.limitedQuirks, doc.quirksMode());

        doc.quirksMode(Document.QuirksMode.quirks);
        Assert.assertEquals(Document.QuirksMode.quirks, doc.quirksMode());
    }

    @Test
    public void testOuterHtml() {
        Document doc = Document.createShell("http://example.com/");
        doc.outputSettings().prettyPrint(false);
        Assert.assertEquals("<html><head></head><body></body></html>", doc.outerHtml());
    }
}
