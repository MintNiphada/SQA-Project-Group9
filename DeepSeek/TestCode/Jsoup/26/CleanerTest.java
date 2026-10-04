package org.jsoup.safety;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
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
        Cleaner cleaner = new Cleaner(Whitelist.none());
        cleaner.clean(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsValidNullDocument() {
        Cleaner cleaner = new Cleaner(Whitelist.none());
        cleaner.isValid(null);
    }

    @Test
    public void testCleanWithNoneWhitelist() {
        Cleaner cleaner = new Cleaner(Whitelist.none());
        Document dirty = Jsoup.parse("<div>Hello <b>world</b></div>");
        Document clean = cleaner.clean(dirty);
        Assert.assertEquals("Hello world", clean.body().text());
        Assert.assertEquals(0, clean.body().children().size());
    }

    @Test
    public void testCleanWithBasicWhitelist() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        Document dirty = Jsoup.parse("<div><p>Safe</p><script>alert('xss')</script><b>bold</b></div>");
        Document clean = cleaner.clean(dirty);
        Assert.assertEquals("Safe bold", clean.body().text());
        Assert.assertEquals(2, clean.body().children().size());
        Assert.assertEquals("p", clean.body().child(0).tagName());
        Assert.assertEquals("b", clean.body().child(1).tagName());
    }

    @Test
    public void testIsValidTrue() {
        Whitelist whitelist = new Whitelist();
        whitelist.addTags("p", "b");
        Cleaner cleaner = new Cleaner(whitelist);
        Document doc = Jsoup.parse("<p><b>text</b></p>");
        Assert.assertTrue(cleaner.isValid(doc));
    }

    @Test
    public void testIsValidFalseDueToDisallowedTag() {
        Whitelist whitelist = new Whitelist();
        whitelist.addTags("p");
        Cleaner cleaner = new Cleaner(whitelist);
        Document doc = Jsoup.parse("<p><script>alert(1)</script></p>");
        Assert.assertFalse(cleaner.isValid(doc));
    }

    @Test
    public void testIsValidFalseDueToDisallowedAttribute() {
        Whitelist whitelist = new Whitelist();
        whitelist.addTags("p");
        Cleaner cleaner = new Cleaner(whitelist);
        Document doc = Jsoup.parse("<p onclick='alert(1)'>text</p>");
        Assert.assertFalse(cleaner.isValid(doc));
    }

    @Test
    public void testAttributeFiltering() {
        Whitelist whitelist = new Whitelist();
        whitelist.addTags("a");
        whitelist.addAttributes("a", "href");
        Cleaner cleaner = new Cleaner(whitelist);
        Document dirty = Jsoup.parse("<a href='http://example.com' onclick='steal()'>link</a>");
        Document clean = cleaner.clean(dirty);
        Element a = clean.body().child(0);
        Assert.assertEquals("a", a.tagName());
        Assert.assertTrue(a.hasAttr("href"));
        Assert.assertFalse(a.hasAttr("onclick"));
    }

    @Test
    public void testEnforcedAttributes() {
        Whitelist whitelist = new Whitelist();
        whitelist.addTags("a");
        whitelist.addEnforcedAttribute("a", "rel", "nofollow");
        Cleaner cleaner = new Cleaner(whitelist);
        Document dirty = Jsoup.parse("<a href='http://example.com'>link</a>");
        Document clean = cleaner.clean(dirty);
        Element a = clean.body().child(0);
        Assert.assertEquals("nofollow", a.attr("rel"));
    }

    @Test
    public void testNestedSafeAndUnsafeTags() {
        Whitelist whitelist = new Whitelist();
        whitelist.addTags("div", "p");
        Cleaner cleaner = new Cleaner(whitelist);
        Document dirty = Jsoup.parse("<div><p>safe</p><script>unsafe</script><p>also safe</p></div>");
        Document clean = cleaner.clean(dirty);
        Assert.assertEquals("safe unsafe also safe", clean.body().text());
        Assert.assertEquals(1, clean.body().children().size());
        Element div = clean.body().child(0);
        Assert.assertEquals("div", div.tagName());
        Assert.assertEquals(2, div.children().size());
        Assert.assertEquals("p", div.child(0).tagName());
        Assert.assertEquals("p", div.child(1).tagName());
    }

    @Test
    public void testTextNodeDirectlyUnderBody() {
        Cleaner cleaner = new Cleaner(Whitelist.none());
        Document dirty = Jsoup.parse("Hello <b>world</b>");
        Document clean = cleaner.clean(dirty);
        Assert.assertEquals("Hello world", clean.body().text());
    }

    @Test
    public void testCommentsIgnored() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        Document dirty = Jsoup.parse("<p>text<!-- comment --></p>");
        Document clean = cleaner.clean(dirty);
        Assert.assertEquals("text", clean.body().text());
        Assert.assertEquals(0, clean.body().child(0).childNodeSize());
    }

    @Test
    public void testBaseUriPreserved() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        Document dirty = Jsoup.parse("<p>text</p>");
        dirty.setBaseUri("http://example.com");
        Document clean = cleaner.clean(dirty);
        Assert.assertEquals("http://example.com", clean.baseUri());
    }

    @Test
    public void testEmptyBody() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        Document dirty = Jsoup.parse("");
        Document clean = cleaner.clean(dirty);
        Assert.assertEquals("", clean.body().text());
        Assert.assertEquals(0, clean.body().children().size());
    }

    @Test
    public void testIsValidWithEnforcedAttributesNotPresent() {
        Whitelist whitelist = new Whitelist();
        whitelist.addTags("a");
        whitelist.addEnforcedAttribute("a", "rel", "nofollow");
        Cleaner cleaner = new Cleaner(whitelist);
        Document doc = Jsoup.parse("<a href='http://example.com'>link</a>");
        Assert.assertFalse(cleaner.isValid(doc));
    }

    @Test
    public void testIsValidWithEnforcedAttributesAlreadyPresent() {
        Whitelist whitelist = new Whitelist();
        whitelist.addTags("a");
        whitelist.addEnforcedAttribute("a", "rel", "nofollow");
        Cleaner cleaner = new Cleaner(whitelist);
        Document doc = Jsoup.parse("<a href='http://example.com' rel='nofollow'>link</a>");
        Assert.assertTrue(cleaner.isValid(doc));
    }

    @Test
    public void testRecursiveDiscardingCount() {
        Whitelist whitelist = new Whitelist();
        whitelist.addTags("div");
        Cleaner cleaner = new Cleaner(whitelist);
        Document dirty = Jsoup.parse("<div><script>alert(1)</script><p>text</p></div>");
        Assert.assertFalse(cleaner.isValid(dirty));
    }

    @Test
    public void testMultipleAttributesDiscarded() {
        Whitelist whitelist = new Whitelist();
        whitelist.addTags("p");
        Cleaner cleaner = new Cleaner(whitelist);
        Document dirty = Jsoup.parse("<p class='foo' id='bar' style='color:red'>text</p>");
        Document clean = cleaner.clean(dirty);
        Element p = clean.body().child(0);
        Assert.assertEquals(0, p.attributes().size());
    }

    @Test
    public void testSafeAttributeWithProtocols() {
        Whitelist whitelist = new Whitelist();
        whitelist.addTags("a");
        whitelist.addAttributes("a", "href");
        whitelist.addProtocols("a", "href", "http", "https");
        Cleaner cleaner = new Cleaner(whitelist);
        Document dirty = Jsoup.parse("<a href='http://safe.com'>link</a><a href='javascript:alert(1)'>xss</a>");
        Document clean = cleaner.clean(dirty);
        Assert.assertEquals(2, clean.body().children().size());
        Assert.assertEquals("http://safe.com", clean.body().child(0).attr("href"));
        Assert.assertFalse(clean.body().child(1).hasAttr("href"));
    }

    @Test
    public void testDeeplyNestedUnsafeTags() {
        Whitelist whitelist = new Whitelist();
        whitelist.addTags("div");
        Cleaner cleaner = new Cleaner(whitelist);
        Document dirty = Jsoup.parse("<div><span><script>alert(1)</script></span></div>");
        Document clean = cleaner.clean(dirty);
        Assert.assertEquals("alert(1)", clean.body().text());
        Assert.assertEquals(1, clean.body().children().size());
        Assert.assertEquals("div", clean.body().child(0).tagName());
    }

    @Test
    public void testCleanDoesNotModifyOriginal() {
        Cleaner cleaner = new Cleaner(Whitelist.none());
        Document dirty = Jsoup.parse("<p>text</p>");
        Document clean = cleaner.clean(dirty);
        Assert.assertEquals("text", dirty.body().text());
        Assert.assertEquals(1, dirty.body().children().size());
    }

    @Test
    public void testIsValidWithTextNodesOnly() {
        Cleaner cleaner = new Cleaner(Whitelist.none());
        Document doc = Jsoup.parse("plain text");
        Assert.assertTrue(cleaner.isValid(doc));
    }

    @Test
    public void testIsValidWithComments() {
        Cleaner cleaner = new Cleaner(Whitelist.none());
        Document doc = Jsoup.parse("<!-- comment -->");
        Assert.assertTrue(cleaner.isValid(doc));
    }
}
