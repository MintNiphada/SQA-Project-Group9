package org.jsoup.nodes;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class CommentTest {

    @Test
    public void testConstructorsAndGetData() {
        Comment comment1 = new Comment("test comment");
        Assert.assertEquals("test comment", comment1.getData());
        Assert.assertEquals("#comment", comment1.nodeName());

        Comment comment2 = new Comment("another comment", "http://example.com");
        Assert.assertEquals("another comment", comment2.getData());
        Assert.assertEquals("#comment", comment2.nodeName());
    }

    @Test
    public void testOuterHtml() {
        Comment comment = new Comment("hello world");
        Assert.assertEquals("<!--hello world-->", comment.outerHtml());
        Assert.assertEquals("<!--hello world-->", comment.toString());

        Document.OutputSettings settings = new Document.OutputSettings();
        settings.prettyPrint(false);
        StringBuilder accum = new StringBuilder();
        comment.outerHtmlTail(accum, 0, settings);
        Assert.assertEquals("", accum.toString());
    }

    @Test
    public void testOuterHtmlHeadWithPrettyPrint() throws IOException {
        Comment comment = new Comment("pretty comment");
        Document doc = new Document("http://example.com");
        doc.outputSettings().prettyPrint(true);

        StringBuilder accum = new StringBuilder();
        comment.outerHtmlHead(accum, 1, doc.outputSettings());
        Assert.assertEquals("  <!--pretty comment-->", accum.toString());
    }

    @Test
    public void testOuterHtmlHeadWithoutPrettyPrint() throws IOException {
        Comment comment = new Comment("raw comment");
        Document doc = new Document("http://example.com");
        doc.outputSettings().prettyPrint(false);

        StringBuilder accum = new StringBuilder();
        comment.outerHtmlHead(accum, 2, doc.outputSettings());
        Assert.assertEquals("<!--raw comment-->", accum.toString());
    }

    @Test
    public void testIsXmlDeclaration() {
        Assert.assertFalse(new Comment("").isXmlDeclaration());
        Assert.assertFalse(new Comment("!").isXmlDeclaration());
        Assert.assertFalse(new Comment("?").isXmlDeclaration());
        Assert.assertFalse(new Comment("regular comment").isXmlDeclaration());
        Assert.assertTrue(new Comment("!DOCTYPE html").isXmlDeclaration());
        Assert.assertTrue(new Comment("?xml version=\"1.0\" encoding=\"utf-8\"?").isXmlDeclaration());
    }

    @Test
    public void testAsXmlDeclarationProcessingInstruction() {
        Comment comment = new Comment("?xml version=\"1.0\" encoding=\"utf-8\"?");
        XmlDeclaration decl = comment.asXmlDeclaration();
        Assert.assertNotNull(decl);
        Assert.assertEquals("xml", decl.name());
        Assert.assertEquals("1.0", decl.attr("version"));
        Assert.assertEquals("utf-8", decl.attr("encoding"));
        Assert.assertFalse(decl.outerHtml().startsWith("<!"));
    }

    @Test
    public void testAsXmlDeclarationDocType() {
        Comment comment = new Comment("!DOCTYPE html");
        XmlDeclaration decl = comment.asXmlDeclaration();
        Assert.assertNotNull(decl);
        Assert.assertEquals("DOCTYPE", decl.name());
        Assert.assertTrue(decl.hasAttr("html"));
    }

    @Test
    public void testAsXmlDeclarationInvalid() {
        Comment comment = new Comment("?");
        try {
            comment.asXmlDeclaration();
            Assert.fail();
        } catch (StringIndexOutOfBoundsException e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testClone() {
        Comment comment = new Comment("data to clone");
        Comment clone = comment.clone();
        Assert.assertNotSame(comment, clone);
        Assert.assertEquals(comment.getData(), clone.getData());
        Assert.assertEquals(comment.nodeName(), clone.nodeName());
        Assert.assertEquals(comment.outerHtml(), clone.outerHtml());
    }
}
