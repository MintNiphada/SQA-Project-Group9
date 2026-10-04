package org.jsoup.safety;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Assert;
import org.junit.Test;

public class CleanerTest {

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullWhitelist() {
        new Cleaner(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCleanNullDocument() {
        Cleaner cleaner = new Cleaner(Whitelist.simpleText());
        cleaner.clean(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsValidNullDocument() {
        Cleaner cleaner = new Cleaner(Whitelist.simpleText());
        cleaner.isValid(null);
    }

    @Test
    public void testSimpleTextCleaning() {
        String html = "<p>Hello <b>World</b></p>";
        Document dirty = Jsoup.parse(html);
        Cleaner cleaner = new Cleaner(Whitelist.simpleText());
        Document clean = cleaner.clean(dirty);

        Assert.assertEquals("Hello <b>World</b>", clean.body().html());
        Assert.assertFalse(cleaner.isValid(dirty));
    }

    @Test
    public void testIsValidTrue() {
        String html = "<b>Hello</b> <em>World</em>";
        Document dirty = Jsoup.parseBodyFragment(html);
        Cleaner cleaner = new Cleaner(Whitelist.simpleText());

        Assert.assertTrue(cleaner.isValid(dirty));
        Document clean = cleaner.clean(dirty);
        Assert.assertEquals("<b>Hello</b> <em>World</em>", clean.body().html());
    }

    @Test
    public void testDisallowedTagRemovalPreservesText() {
        String html = "<div>Test <script>alert('xss')</script> Content</div>";
        Document dirty = Jsoup.parseBodyFragment(html);
        Cleaner cleaner = new Cleaner(Whitelist.simpleText());

        Assert.assertFalse(cleaner.isValid(dirty));
        Document clean = cleaner.clean(dirty);
        Assert.assertEquals("Test alert('xss') Content", clean.body().html());
    }

    @Test
    public void testDisallowedAttributesRemoval() {
        String html = "<a href=\"http://example.com\" onclick=\"steal()\" style=\"color:red\">Link</a>";
        Document dirty = Jsoup.parseBodyFragment(html);
        Cleaner cleaner = new Cleaner(Whitelist.basic());

        Assert.assertFalse(cleaner.isValid(dirty));
        Document clean = cleaner.clean(dirty);
        Assert.assertEquals("<a href=\"http://example.com\" rel=\"nofollow\">Link</a>", clean.body().html());
    }

    @Test
    public void testEnforcedAttributesAdded() {
        String html = "<a href=\"http://example.com\">Link</a>";
        Document dirty = Jsoup.parseBodyFragment(html);
        Cleaner cleaner = new Cleaner(Whitelist.basic());

        Assert.assertTrue(cleaner.isValid(dirty));
        Document clean = cleaner.clean(dirty);
        Assert.assertEquals("<a href=\"http://example.com\" rel=\"nofollow\">Link</a>", clean.body().html());
    }

    @Test
    public void testFramesetDocumentCleaning() {
        Document dirty = Document.createShell("http://example.com");
        Element html = dirty.child(0);
        dirty.body().remove();
        html.appendElement("frameset").appendElement("frame").attr("src", "frame.html");

        Cleaner cleaner = new Cleaner(Whitelist.basic());
        Document clean = cleaner.clean(dirty);
        Assert.assertNotNull(clean.body());
        Assert.assertEquals("", clean.body().html());
    }

    @Test
    public void testNestedElements() {
        String html = "<blockquote><p>Quote with <a href=\"http://example.com\">link</a> and <b>bold</b></p></blockquote>";
        Document dirty = Jsoup.parseBodyFragment(html);
        Cleaner cleaner = new Cleaner(Whitelist.relaxed());

        Assert.assertTrue(cleaner.isValid(dirty));
        Document clean = cleaner.clean(dirty);
        Assert.assertEquals("<blockquote><p>Quote with <a href=\"http://example.com\">link</a> and <b>bold</b></p></blockquote>", clean.body().html());
    }

    @Test
    public void testNonElementNonTextNodesIgnored() {
        String html = "<p>Text <!-- Comment --> <b>Bold</b></p>";
        Document dirty = Jsoup.parseBodyFragment(html);
        Cleaner cleaner = new Cleaner(Whitelist.basic());

        Document clean = cleaner.clean(dirty);
        Assert.assertEquals("<p>Text  <b>Bold</b></p>", clean.body().html());
    }

    @Test
    public void testPreserveBaseUri() {
        String baseUri = "http://example.com/sub/";
        Document dirty = Jsoup.parse("<p><a href=\"relative.html\">Link</a></p>", baseUri);
        Cleaner cleaner = new Cleaner(Whitelist.basic());

        Document clean = cleaner.clean(dirty);
        Assert.assertEquals(baseUri, clean.baseUri());
        Element aTag = clean.body().selectFirst("a");
        Assert.assertNotNull(aTag);
        Assert.assertEquals("http://example.com/sub/relative.html", aTag.absUrl("href"));
    }

    @Test
    public void testCustomWhitelistRule() {
        Whitelist custom = new Whitelist()
                .addTags("custom")
                .addAttributes("custom", "attr")
                .addEnforcedAttribute("custom", "enforced", "val");

        Document dirty = Jsoup.parseBodyFragment("<custom attr=\"123\" bad=\"456\">Text</custom>");
        Cleaner cleaner = new Cleaner(custom);

        Assert.assertFalse(cleaner.isValid(dirty));
        Document clean = cleaner.clean(dirty);
        Assert.assertEquals("<custom attr=\"123\" enforced=\"val\">Text</custom>", clean.body().html());
    }
}
