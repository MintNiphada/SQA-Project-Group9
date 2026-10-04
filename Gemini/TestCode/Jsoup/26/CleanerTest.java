package org.jsoup.safety;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.junit.Assert;
import org.junit.Test;

public class CleanerTest {

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullWhitelist() {
        new Cleaner(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCleanNullDocument() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        cleaner.clean(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsValidNullDocument() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        cleaner.isValid(null);
    }

    @Test
    public void testCleanSimpleValidHtml() {
        String html = "<p>Hello <b>world</b>!</p>";
        Document dirty = Jsoup.parse(html);
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        
        Document clean = cleaner.clean(dirty);
        
        Assert.assertEquals("<p>Hello <b>world</b>!</p>", clean.body().html());
        Assert.assertTrue(cleaner.isValid(dirty));
    }

    @Test
    public void testCleanDropsUnsafeTagsPreservesContent() {
        String html = "<div><custom>Safe text</custom> <b>bold</b></div>";
        Document dirty = Jsoup.parse(html);
        // Whitelist.simpleText only allows b, em, i, strong, u
        Cleaner cleaner = new Cleaner(Whitelist.simpleText());
        
        Document clean = cleaner.clean(dirty);
        
        Assert.assertEquals("Safe text <b>bold</b>", clean.body().html());
        Assert.assertFalse(cleaner.isValid(dirty));
    }

    @Test
    public void testCleanDropsUnsafeAttributes() {
        String html = "<p><a href=\"http://example.com/\" onclick=\"stealCookies()\" title=\"link\">Link</a></p>";
        Document dirty = Jsoup.parse(html);
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        
        Document clean = cleaner.clean(dirty);
        
        Assert.assertFalse(clean.body().html().contains("onclick"));
        Assert.assertTrue(clean.body().html().contains("href=\"http://example.com/\""));
        Assert.assertFalse(cleaner.isValid(dirty));
    }

    @Test
    public void testCleanAddsEnforcedAttributes() {
        String html = "<p><a href=\"http://example.com/\">Link</a></p>";
        Document dirty = Jsoup.parse(html);
        Whitelist whitelist = Whitelist.basic(); // basic enforces rel="nofollow" on <a>
        Cleaner cleaner = new Cleaner(whitelist);
        
        Document clean = cleaner.clean(dirty);
        
        Assert.assertEquals("<p><a href=\"http://example.com/\" rel=\"nofollow\">Link</a></p>", clean.body().html());
        // isValid should return true if no tags or attributes were discarded, even if enforced attrs are added
        Assert.assertTrue(cleaner.isValid(dirty));
    }

    @Test
    public void testCleanDiscardsCommentsAndNonTextNodes() {
        Document dirty = Jsoup.parse("<p>Hello <!-- comment -->world</p>");
        dirty.body().getElementsByTag("p").first().appendChild(new Comment("another comment", ""));
        dirty.body().getElementsByTag("p").first().appendChild(new DataNode("some data", ""));

        Cleaner cleaner = new Cleaner(Whitelist.relaxed());
        Document clean = cleaner.clean(dirty);

        Assert.assertEquals("<p>Hello world</p>", clean.body().html());
        Assert.assertTrue(cleaner.isValid(dirty));
    }

    @Test
    public void testPreservesBaseUri() {
        String baseUri = "http://example.com/path/";
        Document dirty = Jsoup.parse("<p><a href=\"test.html\">Link</a></p>", baseUri);
        Cleaner cleaner = new Cleaner(Whitelist.basic());

        Document clean = cleaner.clean(dirty);

        Assert.assertEquals(baseUri, clean.baseUri());
        Element anchor = clean.body().getElementsByTag("a").first();
        Assert.assertNotNull(anchor);
        Assert.assertEquals("http://example.com/path/test.html", anchor.absUrl("href"));
    }

    @Test
    public void testOriginalDocumentNotModified() {
        String html = "<p><script>alert('xss')</script>Hello</p>";
        Document dirty = Jsoup.parse(html);
        String dirtyOriginalHtml = dirty.html();

        Cleaner cleaner = new Cleaner(Whitelist.basic());
        cleaner.clean(dirty);

        Assert.assertEquals(dirtyOriginalHtml, dirty.html());
    }

    @Test
    public void testIsValidReturnsFalseOnDisallowedProtocols() {
        String html = "<p><a href=\"javascript:alert(1)\">Click</a></p>";
        Document dirty = Jsoup.parse(html);
        Cleaner cleaner = new Cleaner(Whitelist.basic());

        Assert.assertFalse(cleaner.isValid(dirty));
        Document clean = cleaner.clean(dirty);
        Assert.assertEquals("<p><a rel=\"nofollow\">Click</a></p>", clean.body().html());
    }

    @Test
    public void testCleanEmptyDocument() {
        Document dirty = Jsoup.parse("");
        Cleaner cleaner = new Cleaner(Whitelist.basic());

        Document clean = cleaner.clean(dirty);

        Assert.assertEquals("", clean.body().html());
        Assert.assertTrue(cleaner.isValid(dirty));
    }

    @Test
    public void testDeeplyNestedDiscardedTags() {
        String html = "<div><span><u><custom><b>Text</b></custom></u></span></div>";
        Document dirty = Jsoup.parse(html);
        Cleaner cleaner = new Cleaner(Whitelist.none().addTags("b"));

        Document clean = cleaner.clean(dirty);

        Assert.assertEquals("<b>Text</b>", clean.body().html());
        Assert.assertFalse(cleaner.isValid(dirty));
    }

    @Test
    public void testWhitespaceAndFormattingPreserved() {
        String html = "<p>Line 1\nLine 2   Line 3</p>";
        Document dirty = Jsoup.parse(html);
        Cleaner cleaner = new Cleaner(Whitelist.basic());

        Document clean = cleaner.clean(dirty);

        TextNode textNode = (TextNode) clean.body().child(0).childNode(0);
        Assert.assertEquals("Line 1\nLine 2   Line 3", textNode.getWholeText());
    }

    @Test
    public void testMultipleAttributesHandling() {
        Whitelist whitelist = new Whitelist()
                .addTags("a")
                .addAttributes("a", "href", "title")
                .addEnforcedAttribute("a", "target", "_blank");

        String html = "<a href=\"http://example.com\" title=\"Example\" class=\"bad\" onclick=\"bad()\">Link</a>";
        Document dirty = Jsoup.parse(html);
        Cleaner cleaner = new Cleaner(whitelist);

        Assert.assertFalse(cleaner.isValid(dirty));

        Document clean = cleaner.clean(dirty);
        Element a = clean.body().select("a").first();
        Assert.assertNotNull(a);
        Assert.assertEquals("http://example.com", a.attr("href"));
        Assert.assertEquals("Example", a.attr("title"));
        Assert.assertEquals("_blank", a.attr("target"));
        Assert.assertFalse(a.hasAttr("class"));
        Assert.assertFalse(a.hasAttr("onclick"));
    }

    @Test
    public void testCleanWithOnlyTextNodeInBody() {
        Document dirty = Jsoup.parse("Plain text without tags");
        Cleaner cleaner = new Cleaner(Whitelist.basic());

        Document clean = cleaner.clean(dirty);

        Assert.assertEquals("Plain text without tags", clean.body().html());
        Assert.assertTrue(cleaner.isValid(dirty));
    }
}
