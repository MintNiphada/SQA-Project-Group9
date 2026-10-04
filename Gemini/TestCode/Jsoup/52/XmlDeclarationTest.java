package org.jsoup.nodes;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class XmlDeclarationTest {

    @Test
    public void testConstructorAndGetters() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        Assert.assertEquals("#declaration", decl.nodeName());
        Assert.assertEquals("xml", decl.name());
        Assert.assertEquals("http://example.com", decl.baseUri());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullNameThrowsException() {
        new XmlDeclaration(null, "http://example.com", false);
    }

    @Test
    public void testWholeDeclarationNonXml() {
        XmlDeclaration decl = new XmlDeclaration("custom", "http://example.com", false);
        decl.attr("version", "1.0");
        decl.attr("encoding", "UTF-8");
        Assert.assertEquals("custom", decl.getWholeDeclaration());
    }

    @Test
    public void testWholeDeclarationXmlFewAttributes() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        Assert.assertEquals("xml", decl.getWholeDeclaration());

        decl.attr("version", "1.0");
        Assert.assertEquals("xml", decl.getWholeDeclaration());
    }

    @Test
    public void testWholeDeclarationXmlWithVersionAndEncoding() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        decl.attr("version", "1.0");
        decl.attr("encoding", "UTF-8");
        Assert.assertEquals("xml version=\"1.0\" encoding=\"UTF-8\"", decl.getWholeDeclaration());
    }

    @Test
    public void testWholeDeclarationXmlWithVersionOnlyAndOtherAttr() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        decl.attr("version", "1.0");
        decl.attr("standalone", "yes");
        Assert.assertEquals("xml version=\"1.0\"", decl.getWholeDeclaration());
    }

    @Test
    public void testWholeDeclarationXmlWithEncodingOnlyAndOtherAttr() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        decl.attr("encoding", "UTF-8");
        decl.attr("standalone", "yes");
        Assert.assertEquals("xml encoding=\"UTF-8\"", decl.getWholeDeclaration());
    }

    @Test
    public void testWholeDeclarationXmlWithNeitherVersionNorEncoding() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        decl.attr("foo", "bar");
        decl.attr("baz", "qux");
        Assert.assertEquals("xml", decl.getWholeDeclaration());
    }

    @Test
    public void testOuterHtmlDeclaration() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        decl.attr("version", "1.0");
        decl.attr("encoding", "UTF-8");
        Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\">", decl.outerHtml());
        Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\">", decl.toString());
    }

    @Test
    public void testOuterHtmlProcessingInstruction() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", true);
        decl.attr("version", "1.0");
        decl.attr("encoding", "UTF-8");
        Assert.assertEquals("<!xml version=\"1.0\" encoding=\"UTF-8\">", decl.outerHtml());
        Assert.assertEquals("<!xml version=\"1.0\" encoding=\"UTF-8\">", decl.toString());
    }

    @Test
    public void testOuterHtmlHeadAndTailDirectly() throws IOException {
        XmlDeclaration decl = new XmlDeclaration("DOCTYPE", "http://example.com", true);
        StringBuilder sb = new StringBuilder();
        Document.OutputSettings settings = new Document.OutputSettings();
        decl.outerHtmlHead(sb, 0, settings);
        decl.outerHtmlTail(sb, 0, settings);
        Assert.assertEquals("<!DOCTYPE>", sb.toString());
    }
}
