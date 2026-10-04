package org.jsoup.safety;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Tag;
import org.junit.Test;
import static org.junit.Assert.*;

public class CleanerTest {

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullWhitelist() {
        new Cleaner(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCleanWithNullDocument() {
        Cleaner cleaner = new Cleaner(Whitelist.none());
        cleaner.clean(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsValidWithNullDocument() {
        Cleaner cleaner = new Cleaner(Whitelist.none());
        cleaner.isValid(null);
    }

    @Test
    public void testCleanEmptyDocument() {
        Cleaner cleaner = new Cleaner(Whitelist.none());
        Document dirty = Document.createShell("");
        Document clean = cleaner.clean(dirty);
        assertNotNull(clean);
        assertNotNull(clean.body());
        assertEquals(0, clean.body().childNodes().size());
    }

    @Test
    public void testCleanFramesetDocumentNoBody() {
        Cleaner cleaner = new Cleaner(Whitelist.none());
        Document dirty = new Document("");
        dirty.appendChild(new Element(Tag.valueOf("frameset"), ""));
        Document clean = cleaner.clean(dirty);
        assertNotNull(clean);
        assertNotNull(clean.body());
        assertEquals(0, clean.body().childNodes().size());
    }

    @Test
    public void testCleanAllTagsRemovedWithNoneWhitelist() {
        Cleaner cleaner = new Cleaner(Whitelist.none());
        Document dirty = Document.createShell("");
        dirty.body().appendElement("p").text("Hello");
        Document clean = cleaner.clean(dirty);
        assertEquals(0, clean.body().childNodes().size());
    }

    @Test
    public void testCleanKeepsAllowedTag() {
        Whitelist wl = new Whitelist();
        wl.addTags("p");
        Cleaner cleaner = new Cleaner(wl);
        Document dirty = Document.createShell("");
        dirty.body().appendElement("p").text("Hello");
        Document clean = cleaner.clean(dirty);
        assertEquals(1, clean.body().children().size());
        assertEquals("p", clean.body().child(0).nodeName());
        assertEquals("Hello", clean.body().child(0).text());
    }

    @Test
    public void testCleanKeepsNestedAllowedTags() {
        Whitelist wl = new Whitelist();
        wl.addTags("div", "p");
        Cleaner cleaner = new Cleaner(wl);
        Document dirty = Document.createShell("");
        Element div = dirty.body().appendElement("div");
        div.appendElement("p").text("Hello");
        Document clean = cleaner.clean(dirty);
        assertEquals(1, clean.body().children().size());
        Element cleanDiv = clean.body().child(0);
        assertEquals("div", cleanDiv.nodeName());
        assertEquals(1, cleanDiv.children().size());
        assertEquals("p", cleanDiv.child(0).nodeName());
        assertEquals("Hello", cleanDiv.child(0).text());
    }

    @Test
    public void testCleanRemovesDisallowedTagButKeepsText() {
        Whitelist wl = new Whitelist();
        wl.addTags("p");
        Cleaner cleaner = new Cleaner(wl);
        Document dirty = Document.createShell("");
        Element p = dirty.body().appendElement("p");
        p.appendElement("span").text("Hello");
        Document clean = cleaner.clean(dirty);
        assertEquals(1, clean.body().children().size());
        Element cleanP = clean.body().child(0);
        assertEquals(1, cleanP.childNodes().size());
        assertTrue(cleanP.childNode(0) instanceof TextNode);
        assertEquals("Hello", ((TextNode) cleanP.childNode(0)).getWholeText());
    }

    @Test
    public void testCleanAllowsAllowedAttributes() {
        Whitelist wl = new Whitelist();
        wl.addTags("a");
        wl.addAttributes("a", "href");
        Cleaner cleaner = new Cleaner(wl);
        Document dirty = Document.createShell("");
        dirty.body().appendElement("a").attr("href", "http://example.com").text("link");
        Document clean = cleaner.clean(dirty);
        Element a = clean.body().child(0);
        assertEquals("http://example.com", a.attr("href"));
    }

    @Test
    public void testCleanRemovesDisallowedAttributes() {
        Whitelist wl = new Whitelist();
        wl.addTags("a");
        wl.addAttributes("a", "href");
        Cleaner cleaner = new Cleaner(wl);
        Document dirty = Document.createShell("");
        dirty.body().appendElement("a").attr("href", "http://example.com").attr("onclick", "foo()").text("click");
        Document clean = cleaner.clean(dirty);
        Element a = clean.body().child(0);
        assertTrue(a.hasAttr("href"));
        assertFalse(a.hasAttr("onclick"));
    }

    @Test
    public void testCleanAddsEnforcedAttributes() {
        Whitelist wl = new Whitelist();
        wl.addTags("a");
        wl.addEnforcedAttribute("a", "rel", "nofollow");
        Cleaner cleaner = new Cleaner(wl);
        Document dirty = Document.createShell("");
        dirty.body().appendElement("a").attr("href", "http://example.com").text("link");
        Document clean = cleaner.clean(dirty);
        Element a = clean.body().child(0);
        assertEquals("nofollow", a.attr("rel"));
    }

    @Test
    public void testCleanTextNodePreservation() {
        Whitelist wl = new Whitelist();
        wl.addTags("p");
        Cleaner cleaner = new Cleaner(wl);
        Document dirty = Document.createShell("");
        Element p = dirty.body().appendElement("p");
        p.appendText("Hello ");
        p.appendElement("b").text("World");
        Document clean = cleaner.clean(dirty);
        Element cleanP = clean.body().child(0);
        assertEquals(2, cleanP.childNodes().size());
        assertTrue(cleanP.childNode(0) instanceof TextNode);
        assertTrue(cleanP.childNode(1) instanceof TextNode);
        assertEquals("Hello World", cleanP.text());
    }

    @Test
    public void testIsValidReturnsTrueForCleanDocument() {
        Whitelist wl = new Whitelist();
        wl.addTags("p");
        Cleaner cleaner = new Cleaner(wl);
        Document dirty = Document.createShell("");
        dirty.body().appendElement("p").text("valid");
        assertTrue(cleaner.isValid(dirty));
    }

    @Test
    public void testIsValidReturnsFalseForDocumentWithDisallowedTag() {
        Whitelist wl = new Whitelist();
        wl.addTags("p");
        Cleaner cleaner = new Cleaner(wl);
        Document dirty = Document.createShell("");
        dirty.body().appendElement("p").text("valid");
        dirty.body().appendElement("script").text("bad");
        assertFalse(cleaner.isValid(dirty));
    }

    @Test
    public void testIsValidReturnsFalseForDocumentWithDisallowedAttribute() {
        Whitelist wl = new Whitelist();
        wl.addTags("a");
        wl.addAttributes("a", "href");
        Cleaner cleaner = new Cleaner(wl);
        Document dirty = Document.createShell("");
        dirty.body().appendElement("a").attr("href", "http://example.com").attr("onclick", "foo()").text("link");
        assertFalse(cleaner.isValid(dirty));
    }

    @Test
    public void testIsValidWithFramesetDocument() {
        Whitelist wl = new Whitelist();
        wl.addTags("body");
        Cleaner cleaner = new Cleaner(wl);
        Document dirty = new Document("");
        dirty.appendChild(new Element(Tag.valueOf("frameset"), ""));
        assertTrue(cleaner.isValid(dirty));
    }

    @Test
    public void testCleanDocumentBaseUriPreserved() {
        Whitelist wl = new Whitelist();
        wl.addTags("p");
        Cleaner cleaner = new Cleaner(wl);
        Document dirty = Document.createShell("http://example.com");
        dirty.body().appendElement("p").text("test");
        Document clean = cleaner.clean(dirty);
        assertEquals("http://example.com", clean.baseUri());
    }

    @Test
    public void testCleanElementMetaDiscardsCount() {
        Whitelist wl = new Whitelist();
        wl.addTags("a");
        wl.addAttributes("a", "href");
        Cleaner cleaner = new Cleaner(wl);
        Document dirty = Document.createShell("");
        dirty.body().appendElement("a").attr("href", "http://example.com").attr("title", "extra").text("link");
        assertFalse(cleaner.isValid(dirty));
    }

    @Test
    public void testCopySafeNodesWithUnsafeTagCountsDiscard() {
        Whitelist wl = new Whitelist();
        Cleaner cleaner = new Cleaner(wl);
        Document dirty = Document.createShell("");
        dirty.body().appendElement("script").text("alert(1)");
        assertFalse(cleaner.isValid(dirty));
    }

    @Test
    public void testEnforcedAttributesOverrideExisting() {
        Whitelist wl = new Whitelist();
        wl.addTags("a");
        wl.addAttributes("a", "rel");
        wl.addEnforcedAttribute("a", "rel", "nofollow");
        Cleaner cleaner = new Cleaner(wl);
        Document dirty = Document.createShell("");
        dirty.body().appendElement("a").attr("rel", "follow").attr("href", "http://example.com").text("link");
        Document clean = cleaner.clean(dirty);
        Element a = clean.body().child(0);
        assertEquals("nofollow", a.attr("rel"));
        assertEquals("http://example.com", a.attr("href"));
    }

    @Test
    public void testCleanWithTokenParentChildCombinations() {
        Whitelist wl = new Whitelist();
        wl.addTags("ul", "li");
        Cleaner cleaner = new Cleaner(wl);
        Document dirty = Document.createShell("");
        Element ul = dirty.body().appendElement("ul");
        ul.appendElement("li").text("Item 1");
        ul.appendElement("script").text("bad");
        ul.appendElement("li").text("Item 2");
        Document clean = cleaner.clean(dirty);
        Element cleanUl = clean.body().child(0);
        assertEquals(2, cleanUl.children().size());
        assertEquals("Item 1", cleanUl.child(0).text());
        assertEquals("Item 2", cleanUl.child(1).text());
    }
}
