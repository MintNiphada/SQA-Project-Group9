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
    public void testSimpleClean() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        Document dirty = Jsoup.parse("<p>Hello <b>world</b>!</p>");
        Document clean = cleaner.clean(dirty);
        Assert.assertEquals("<p>Hello <b>world</b>!</p>", clean.body().html());
    }

    @Test
    public void testDisallowedTagRemoval() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        Document dirty = Jsoup.parse("<p>Hello <script>alert('xss');</script>world</p>");
        Document clean = cleaner.clean(dirty);
        Assert.assertEquals("<p>Hello world</p>", clean.body().html());
    }

    @Test
    public void testDisallowedAttributeRemoval() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        Document dirty = Jsoup.parse("<p onclick=\"exploit()\" style=\"color:red\">Text</p>");
        Document clean = cleaner.clean(dirty);
        Assert.assertEquals("<p>Text</p>", clean.body().html());
    }

    @Test
    public void testAllowedAttributePreserved() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        Document dirty = Jsoup.parse("<a href=\"http://example.com/\" title=\"Test\">Link</a>");
        Document clean = cleaner.clean(dirty);
        Assert.assertTrue(clean.body().html().contains("href=\"http://example.com/\""));
    }

    @Test
    public void testEnforcedAttributes() {
        Whitelist whitelist = Whitelist.basic().addEnforcedAttribute("a", "rel", "nofollow");
        Cleaner cleaner = new Cleaner(whitelist);
        Document dirty = Jsoup.parse("<a href=\"http://example.com/\">Link</a>");
        Document clean = cleaner.clean(dirty);
        Assert.assertEquals("<a href=\"http://example.com/\" rel=\"nofollow\">Link</a>", clean.body().html());
    }

    @Test
    public void testIsValidTrue() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        Document validDoc = Jsoup.parse("<p><a href=\"http://example.com/\">Link</a></p>");
        Assert.assertTrue(cleaner.isValid(validDoc));
    }

    @Test
    public void testIsValidFalseWithDisallowedTag() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        Document dirty = Jsoup.parse("<p>Text<script>alert(1)</script></p>");
        Assert.assertFalse(cleaner.isValid(dirty));
    }

    @Test
    public void testIsValidFalseWithDisallowedAttribute() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        Document dirty = Jsoup.parse("<p onclick=\"alert(1)\">Text</p>");
        Assert.assertFalse(cleaner.isValid(dirty));
    }

    @Test
    public void testIsValidFalseWithComment() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        Document dirty = Jsoup.parse("<p>Text<!-- comment --></p>");
        Assert.assertFalse(cleaner.isValid(dirty));
    }

    @Test
    public void testCommentsDroppedInClean() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        Document dirty = Jsoup.parse("<p>Hello <!-- comment -->World</p>");
        Document clean = cleaner.clean(dirty);
        Assert.assertEquals("<p>Hello World</p>", clean.body().html());
    }

    @Test
    public void testFramesetWithoutBody() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        Document framesetDoc = Document.createShell("");
        framesetDoc.body().remove();
        Document clean = cleaner.clean(framesetDoc);
        Assert.assertNotNull(clean.body());
        Assert.assertEquals("", clean.body().html());
    }

    @Test
    public void testDataNodeInSafeTag() {
        Whitelist whitelist = Whitelist.none().addTags("script");
        Cleaner cleaner = new Cleaner(whitelist);
        Document dirty = Document.createShell("http://example.com/");
        Element script = dirty.body().appendElement("script");
        script.appendChild(new DataNode("var x = 1;", "http://example.com/"));
        Document clean = cleaner.clean(dirty);
        Assert.assertEquals("<script>var x = 1;</script>", clean.body().html());
        Assert.assertTrue(cleaner.isValid(dirty));
    }

    @Test
    public void testDataNodeInUnsafeTag() {
        Whitelist whitelist = Whitelist.none();
        Cleaner cleaner = new Cleaner(whitelist);
        Document dirty = Document.createShell("http://example.com/");
        Element script = dirty.body().appendElement("script");
        script.appendChild(new DataNode("var x = 1;", "http://example.com/"));
        Document clean = cleaner.clean(dirty);
        Assert.assertEquals("", clean.body().html());
        Assert.assertFalse(cleaner.isValid(dirty));
    }

    @Test
    public void testBaseUriPreserved() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        Document dirty = Document.createShell("http://example.com/");
        Element link = dirty.body().appendElement("a");
        link.attr("href", "/relative");
        link.text("Link");
        Document clean = cleaner.clean(dirty);
        Assert.assertEquals("http://example.com/", clean.baseUri());
        Assert.assertEquals("http://example.com/relative", clean.body().select("a").first().absUrl("href"));
    }

    @Test
    public void testNestedElementsStructure() {
        Cleaner cleaner = new Cleaner(Whitelist.relaxed());
        Document dirty = Jsoup.parse("<div><table><tbody><tr><td>Cell 1</td><td>Cell 2</td></tr></tbody></table></div>");
        Document clean = cleaner.clean(dirty);
        Assert.assertEquals("<div>\n <table>\n  <tbody>\n   <tr>\n    <td>Cell 1</td>\n    <td>Cell 2</td>\n   </tr>\n  </tbody>\n </table>\n</div>", clean.body().html());
    }

    @Test
    public void testTextNodesPreserved() {
        Cleaner cleaner = new Cleaner(Whitelist.none());
        Document dirty = Jsoup.parse("Just plain text with <b>formatting</b> removed.");
        Document clean = cleaner.clean(dirty);
        Assert.assertEquals("Just plain text with formatting removed.", clean.body().html());
    }

    @Test
    public void testEmptyDocumentClean() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        Document dirty = Jsoup.parse("");
        Document clean = cleaner.clean(dirty);
        Assert.assertEquals("", clean.body().html());
        Assert.assertTrue(cleaner.isValid(dirty));
    }

    @Test
    public void testCustomTagsAndAttributes() {
        Whitelist whitelist = Whitelist.none()
                .addTags("custom-tag")
                .addAttributes("custom-tag", "custom-attr");
        Cleaner cleaner = new Cleaner(whitelist);
        Document dirty = Jsoup.parse("<custom-tag custom-attr=\"val\" dropped-attr=\"drop\">Content</custom-tag>");
        Assert.assertFalse(cleaner.isValid(dirty));
        Document clean = cleaner.clean(dirty);
        Assert.assertEquals("<custom-tag custom-attr=\"val\">\n Content\n</custom-tag>", clean.body().html());
    }

    @Test
    public void testNonElementNodeHandling() {
        Cleaner cleaner = new Cleaner(Whitelist.basic());
        Document dirty = Document.createShell("");
        dirty.body().appendChild(new Comment("a comment", ""));
        dirty.body().appendChild(new TextNode("some text", ""));
        Assert.assertFalse(cleaner.isValid(dirty));
        Document clean = cleaner.clean(dirty);
        Assert.assertEquals("some text", clean.body().html());
    }
}
