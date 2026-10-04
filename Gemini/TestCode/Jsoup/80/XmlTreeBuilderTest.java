package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.CDataNode;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.XmlDeclaration;
import org.junit.Assert;
import org.junit.Test;

import java.io.StringReader;
import java.util.List;

public class XmlTreeBuilderTest {

    @Test
    public void testDefaultSettings() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        ParseSettings settings = builder.defaultSettings();
        Assert.assertEquals("TEST", settings.normalizeTag("TEST"));
        Assert.assertEquals("ATTR", settings.normalizeAttribute("ATTR"));
    }

    @Test
    public void testParseString() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("<root><child attr=\"val\">Text</child></root>", "http://example.com/");
        Assert.assertEquals(Document.OutputSettings.Syntax.xml, doc.outputSettings().syntax());
        Assert.assertEquals(1, doc.children().size());
        Element root = doc.child(0);
        Assert.assertEquals("root", root.tagName());
        Assert.assertEquals("child", root.child(0).tagName());
        Assert.assertEquals("val", root.child(0).attr("attr"));
        Assert.assertEquals("Text", root.child(0).text());
        Assert.assertEquals("http://example.com/", root.baseUri());
    }

    @Test
    public void testParseReader() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse(new StringReader("<root id=\"1\" />"), "http://example.com/");
        Assert.assertEquals(Document.OutputSettings.Syntax.xml, doc.outputSettings().syntax());
        Assert.assertEquals("root", doc.child(0).tagName());
        Assert.assertEquals("1", doc.child(0).attr("id"));
    }

    @Test
    public void testParseFragment() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        List<Node> nodes = builder.parseFragment("<one/><two>Text</two>", "http://example.com/", ParseErrorList.noTracking(), ParseSettings.preserveCase);
        Assert.assertEquals(2, nodes.size());
        Assert.assertEquals("one", ((Element) nodes.get(0)).tagName());
        Assert.assertEquals("two", ((Element) nodes.get(1)).tagName());
        Assert.assertEquals("Text", ((Element) nodes.get(1)).text());
    }

    @Test
    public void testSelfClosingKnownTag() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("<br/><img src=\"foo.jpg\"/>", "");
        Assert.assertEquals(2, doc.children().size());
        Assert.assertEquals("br", doc.child(0).tagName());
        Assert.assertEquals("img", doc.child(1).tagName());
    }

    @Test
    public void testSelfClosingUnknownTag() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("<custom-tag id=\"1\"/><another-tag/>", "");
        Assert.assertEquals(2, doc.children().size());
        Element el = doc.child(0);
        Assert.assertEquals("custom-tag", el.tagName());
        Assert.assertTrue(el.tag().isSelfClosing());
    }

    @Test
    public void testCommentNormal() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("<!-- This is a regular comment --><root/>", "");
        Assert.assertEquals(2, doc.childNodeSize());
        Assert.assertTrue(doc.childNode(0) instanceof Comment);
        Comment comment = (Comment) doc.childNode(0);
        Assert.assertEquals(" This is a regular comment ", comment.getData());
    }

    @Test
    public void testXmlDeclarationBogusComment() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("<?xml version=\"1.0\" encoding=\"UTF-8\"?><root/>", "");
        Assert.assertTrue(doc.childNode(0) instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) doc.childNode(0);
        Assert.assertEquals("xml", decl.name());
        Assert.assertEquals("1.0", decl.attr("version"));
        Assert.assertEquals("UTF-8", decl.attr("encoding"));
        Assert.assertFalse(decl.toString().startsWith("<!"));
    }

    @Test
    public void testXmlDeclarationExclamationBogusComment() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("<!xml version=\"1.0\"?><root/>", "");
        Assert.assertTrue(doc.childNode(0) instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) doc.childNode(0);
        Assert.assertEquals("xml", decl.name());
        Assert.assertEquals("1.0", decl.attr("version"));
    }

    @Test
    public void testBogusCommentShortOrNoSpecialPrefix() {
        Token.Comment commentToken = new Token.Comment();
        commentToken.bogus = true;
        commentToken.data.append("!");

        XmlTreeBuilder builder = new XmlTreeBuilder();
        builder.initialiseParse(new StringReader(""), "", ParseErrorList.noTracking(), ParseSettings.preserveCase);
        builder.insert(commentToken);

        Assert.assertEquals(1, builder.doc.childNodeSize());
        Assert.assertTrue(builder.doc.childNode(0) instanceof Comment);
        Assert.assertEquals("!", ((Comment) builder.doc.childNode(0)).getData());

        Token.Comment commentToken2 = new Token.Comment();
        commentToken2.bogus = true;
        commentToken2.data.append("regular bogus comment");

        builder.insert(commentToken2);
        Assert.assertEquals(2, builder.doc.childNodeSize());
        Assert.assertTrue(builder.doc.childNode(1) instanceof Comment);
    }

    @Test
    public void testCDataNode() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("<root><![CDATA[raw & unescaped <data>]]></root>", "");
        Element root = doc.child(0);
        Assert.assertEquals(1, root.childNodeSize());
        Assert.assertTrue(root.childNode(0) instanceof CDataNode);
        CDataNode cdata = (CDataNode) root.childNode(0);
        Assert.assertEquals("raw & unescaped <data>", cdata.text());
    }

    @Test
    public void testTextNode() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("<root>Simple text</root>", "");
        Element root = doc.child(0);
        Assert.assertEquals(1, root.childNodeSize());
        Assert.assertTrue(root.childNode(0) instanceof TextNode);
        Assert.assertEquals("Simple text", ((TextNode) root.childNode(0)).text());
    }

    @Test
    public void testDoctypeNode() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("<!DOCTYPE html SYSTEM \"about:legacy-compat\"><root/>", "");
        Assert.assertTrue(doc.childNode(0) instanceof DocumentType);
        DocumentType doctype = (DocumentType) doc.childNode(0);
        Assert.assertEquals("html", doctype.attr("name"));
        Assert.assertEquals("about:legacy-compat", doctype.attr("systemId"));
    }

    @Test
    public void testDoctypeWithPubSysKey() {
        Token.Doctype doctypeToken = new Token.Doctype();
        doctypeToken.name.append("html");
        doctypeToken.publicIdentifier.append("public_id");
        doctypeToken.systemIdentifier.append("system_id");
        doctypeToken.pubSysKey = "SYSTEM";

        XmlTreeBuilder builder = new XmlTreeBuilder();
        builder.initialiseParse(new StringReader(""), "", ParseErrorList.noTracking(), ParseSettings.preserveCase);
        builder.insert(doctypeToken);

        Assert.assertEquals(1, builder.doc.childNodeSize());
        DocumentType docTypeNode = (DocumentType) builder.doc.childNode(0);
        Assert.assertEquals("html", docTypeNode.attr("name"));
        Assert.assertEquals("public_id", docTypeNode.attr("publicId"));
        Assert.assertEquals("system_id", docTypeNode.attr("systemId"));
    }

    @Test
    public void testPopStackToCloseMatching() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("<outer><inner>content</inner></outer>", "");
        Assert.assertEquals(1, doc.children().size());
        Element outer = doc.child(0);
        Assert.assertEquals("outer", outer.tagName());
        Assert.assertEquals(1, outer.children().size());
        Element inner = outer.child(0);
        Assert.assertEquals("inner", inner.tagName());
        Assert.assertEquals("content", inner.text());
    }

    @Test
    public void testPopStackToCloseUnmatchedAndOutOfOrder() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("<outer><inner><nested>text</inner></outer>", "");
        Element outer = doc.child(0);
        Assert.assertEquals("outer", outer.tagName());
        Assert.assertEquals(1, outer.children().size());
        Element inner = outer.child(0);
        Assert.assertEquals("inner", inner.tagName());
        Assert.assertEquals(1, inner.children().size());
        Element nested = inner.child(0);
        Assert.assertEquals("nested", nested.tagName());
    }

    @Test
    public void testPopStackToCloseNonExistentTag() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("<outer>text</nonexistent></outer>", "");
        Element outer = doc.child(0);
        Assert.assertEquals("outer", outer.tagName());
        Assert.assertEquals("text", outer.text());
    }

    @Test
    public void testProcessEofToken() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        builder.initialiseParse(new StringReader(""), "", ParseErrorList.noTracking(), ParseSettings.preserveCase);
        Token.EOF eof = new Token.EOF();
        boolean result = builder.process(eof);
        Assert.assertTrue(result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProcessUnexpectedTokenType() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        builder.initialiseParse(new StringReader(""), "", ParseErrorList.noTracking(), ParseSettings.preserveCase);
        Token token = new Token() {
            {
                type = TokenType.Comment;
            }
        };
        token.type = null;
        builder.process(token);
    }

    @Test
    public void testCasePreservation() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("<CamelCase AttributeName=\"Value\" />", "");
        Element el = doc.child(0);
        Assert.assertEquals("CamelCase", el.tagName());
        Assert.assertTrue(el.hasAttr("AttributeName"));
        Assert.assertEquals("Value", el.attr("AttributeName"));
    }

    @Test
    public void testJsoupXmlParserIntegration() {
        String xml = "<CHECK><ITEM id=\"1\">Value</ITEM></CHECK>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        Assert.assertEquals("CHECK", doc.child(0).tagName());
        Assert.assertEquals("1", doc.select("ITEM").attr("id"));
        Assert.assertEquals("Value", doc.select("ITEM").text());
    }
}
