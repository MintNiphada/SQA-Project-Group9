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
import org.junit.Before;
import org.junit.Test;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

public class XmlTreeBuilderTest {

    private XmlTreeBuilder treeBuilder;

    @Before
    public void setUp() {
        treeBuilder = new XmlTreeBuilder();
    }

    @Test
    public void testDefaultSettings() {
        ParseSettings settings = treeBuilder.defaultSettings();
        Assert.assertNotNull(settings);
        Assert.assertEquals(ParseSettings.preserveCase, settings);
    }

    @Test
    public void testParseReaderAndString() {
        String xml = "<root attr='val'><child>text</child></root>";
        Document doc1 = treeBuilder.parse(new StringReader(xml), "http://example.com/");
        Document doc2 = treeBuilder.parse(xml, "http://example.com/");

        Assert.assertNotNull(doc1);
        Assert.assertNotNull(doc2);
        Assert.assertEquals(Document.OutputSettings.Syntax.xml, doc1.outputSettings().syntax());
        Assert.assertEquals("root", doc1.child(0).tagName());
        Assert.assertEquals("val", doc1.child(0).attr("attr"));
        Assert.assertEquals("text", doc1.child(0).child(0).text());
        Assert.assertEquals(doc1.outerHtml(), doc2.outerHtml());
    }

    @Test
    public void testParseFragment() {
        String xml = "<one>One</one><two>Two</two>";
        List<Node> nodes = treeBuilder.parseFragment(xml, "http://example.com/", ParseErrorList.noTracking(), ParseSettings.preserveCase);
        Assert.assertEquals(2, nodes.size());
        Assert.assertTrue(nodes.get(0) instanceof Element);
        Assert.assertEquals("one", ((Element) nodes.get(0)).tagName());
        Assert.assertEquals("two", ((Element) nodes.get(1)).tagName());
    }

    @Test
    public void testSelfClosingUnknownTag() {
        String xml = "<custom self='true' />";
        Document doc = treeBuilder.parse(xml, "");
        Element el = doc.child(0);
        Assert.assertEquals("custom", el.tagName());
        Assert.assertTrue(el.tag().isSelfClosing());
    }

    @Test
    public void testSelfClosingKnownTag() {
        String xml = "<br />";
        Document doc = treeBuilder.parse(xml, "");
        Element el = doc.child(0);
        Assert.assertEquals("br", el.tagName());
    }

    @Test
    public void testCDataParsing() {
        String xml = "<data><![CDATA[some <cdata> & content]]></data>";
        Document doc = treeBuilder.parse(xml, "");
        Element dataEl = doc.child(0);
        Assert.assertEquals(1, dataEl.childNodeSize());
        Node child = dataEl.childNode(0);
        Assert.assertTrue(child instanceof CDataNode);
        Assert.assertEquals("some <cdata> & content", ((CDataNode) child).text());
    }

    @Test
    public void testTextNodeParsing() {
        String xml = "<data>Plain text content</data>";
        Document doc = treeBuilder.parse(xml, "");
        Element dataEl = doc.child(0);
        Assert.assertEquals(1, dataEl.childNodeSize());
        Node child = dataEl.childNode(0);
        Assert.assertTrue(child instanceof TextNode);
        Assert.assertEquals("Plain text content", ((TextNode) child).text());
    }

    @Test
    public void testDoctypeParsing() {
        String xml = "<!DOCTYPE html SYSTEM \"about:legacy-compat\"><root/>";
        Document doc = treeBuilder.parse(xml, "");
        List<Node> nodes = doc.childNodes();
        Assert.assertTrue(nodes.get(0) instanceof DocumentType);
        DocumentType doctype = (DocumentType) nodes.get(0);
        Assert.assertEquals("html", doctype.name());
        Assert.assertEquals("about:legacy-compat", doctype.attr("systemId"));
    }

    @Test
    public void testDoctypeWithPublicId() {
        String xml = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\"><root/>";
        Document doc = treeBuilder.parse(xml, "");
        DocumentType doctype = (DocumentType) doc.childNode(0);
        Assert.assertEquals("html", doctype.name());
        Assert.assertEquals("-//W3C//DTD XHTML 1.0 Strict//EN", doctype.attr("publicId"));
        Assert.assertEquals("http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd", doctype.attr("systemId"));
    }

    @Test
    public void testNormalComment() {
        String xml = "<root><!-- This is a comment --></root>";
        Document doc = treeBuilder.parse(xml, "");
        Element root = doc.child(0);
        Assert.assertEquals(1, root.childNodeSize());
        Assert.assertTrue(root.childNode(0) instanceof Comment);
        Assert.assertEquals(" This is a comment ", ((Comment) root.childNode(0)).getData());
    }

    @Test
    public void testBogusCommentXmlDeclarationQuestion() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root/>";
        Document doc = treeBuilder.parse(xml, "");
        Assert.assertTrue(doc.childNode(0) instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) doc.childNode(0);
        Assert.assertEquals("xml", decl.name());
        Assert.assertEquals("1.0", decl.attr("version"));
        Assert.assertEquals("UTF-8", decl.attr("encoding"));
        Assert.assertFalse(decl.isProcessingInstruction());
    }

    @Test
    public void testBogusCommentXmlDeclarationExclamation() {
        String xml = "<!xml version=\"1.0\"?><root/>";
        Document doc = treeBuilder.parse(xml, "");
        Assert.assertTrue(doc.childNode(0) instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) doc.childNode(0);
        Assert.assertEquals("xml", decl.name());
        Assert.assertEquals("1.0", decl.attr("version"));
        Assert.assertTrue(decl.isProcessingInstruction());
    }

    @Test
    public void testBogusCommentShortOrNoDeclarationPrefix() {
        Token.Comment commentToken = new Token.Comment();
        commentToken.bogus = true;
        commentToken.data.append("?");
        treeBuilder.initialiseParse(new StringReader(""), "", ParseErrorList.noTracking(), ParseSettings.preserveCase);
        treeBuilder.insert(commentToken);
        Assert.assertEquals(1, treeBuilder.doc.childNodeSize());
        Assert.assertTrue(treeBuilder.doc.childNode(0) instanceof Comment);

        Token.Comment commentToken2 = new Token.Comment();
        commentToken2.bogus = true;
        commentToken2.data.append("plain bogus");
        treeBuilder.insert(commentToken2);
        Assert.assertEquals(2, treeBuilder.doc.childNodeSize());
        Assert.assertTrue(treeBuilder.doc.childNode(1) instanceof Comment);
    }

    @Test
    public void testPopStackToCloseMatching() {
        String xml = "<a><b><c>test</c></b></a>";
        Document doc = treeBuilder.parse(xml, "");
        Assert.assertEquals("test", doc.select("a > b > c").text());
    }

    @Test
    public void testPopStackToCloseUnmatchedAndOutOfOrder() {
        String xml = "<a><b><c>test</b></a>";
        Document doc = treeBuilder.parse(xml, "");
        Assert.assertEquals("test", doc.select("a > b > c").text());
    }

    @Test
    public void testPopStackToCloseNonExistentTag() {
        String xml = "<a><z>test</z></nonexistent></a>";
        Document doc = treeBuilder.parse(xml, "");
        Assert.assertEquals(1, doc.children().size());
        Assert.assertEquals("a", doc.child(0).tagName());
    }

    @Test
    public void testProcessEofToken() {
        treeBuilder.initialiseParse(new StringReader(""), "", ParseErrorList.noTracking(), ParseSettings.preserveCase);
        Token.EOF eof = new Token.EOF();
        boolean processed = treeBuilder.process(eof);
        Assert.assertTrue(processed);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProcessUnexpectedTokenType() {
        treeBuilder.initialiseParse(new StringReader(""), "", ParseErrorList.noTracking(), ParseSettings.preserveCase);
        Token dummy = new Token() {
            @Override
            Token reset() {
                return this;
            }
        };
        treeBuilder.process(dummy);
    }

    @Test
    public void testCasePreservingSettings() {
        String xml = "<MyTag MyAttr=\"Val\">Text</MyTag>";
        Document doc = treeBuilder.parse(xml, "");
        Element el = doc.child(0);
        Assert.assertEquals("MyTag", el.tagName());
        Assert.assertTrue(el.hasAttr("MyAttr"));
        Assert.assertEquals("Val", el.attr("MyAttr"));
    }

    @Test
    public void testCaseInsensitiveSettings() {
        ParseSettings settings = new ParseSettings(false, false);
        List<Node> nodes = treeBuilder.parseFragment("<MyTag MyAttr=\"Val\" />", "", ParseErrorList.noTracking(), settings);
        Assert.assertEquals(1, nodes.size());
        Element el = (Element) nodes.get(0);
        Assert.assertEquals("mytag", el.tagName());
        Assert.assertTrue(el.hasAttr("myattr"));
    }

    @Test
    public void testNestedElementsStackState() {
        String xml = "<outer><inner1></inner1><inner2><inner3/></inner2></outer>";
        Document doc = treeBuilder.parse(xml, "");
        Assert.assertEquals(1, doc.children().size());
        Element outer = doc.child(0);
        Assert.assertEquals("outer", outer.tagName());
        Assert.assertEquals(2, outer.children().size());
        Assert.assertEquals("inner1", outer.child(0).tagName());
        Assert.assertEquals("inner2", outer.child(1).tagName());
        Assert.assertEquals("inner3", outer.child(1).child(0).tagName());
    }
}
