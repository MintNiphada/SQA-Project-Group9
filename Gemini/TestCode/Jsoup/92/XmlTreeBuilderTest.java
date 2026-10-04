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
        Assert.assertEquals("Tag", settings.normalizeTag("Tag"));
        Assert.assertEquals("Attr", settings.normalizeAttribute("Attr"));
    }

    @Test
    public void testParseReaderBaseUri() {
        String xml = "<root><child id=\"1\">test</child></root>";
        Document doc = treeBuilder.parse(new StringReader(xml), "http://example.com");
        Assert.assertEquals("http://example.com", doc.baseUri());
        Assert.assertEquals(1, doc.children().size());
        Element root = doc.child(0);
        Assert.assertEquals("root", root.tagName());
        Assert.assertEquals(1, root.children().size());
        Element child = root.child(0);
        Assert.assertEquals("child", child.tagName());
        Assert.assertEquals("1", child.attr("id"));
        Assert.assertEquals("test", child.text());
        Assert.assertEquals(Document.OutputSettings.Syntax.xml, doc.outputSettings().syntax());
    }

    @Test
    public void testParseStringBaseUri() {
        String xml = "<doc attr=\"val\"><inner/></doc>";
        Document doc = treeBuilder.parse(xml, "http://test.com");
        Assert.assertEquals("http://test.com", doc.baseUri());
        Assert.assertEquals("doc", doc.child(0).tagName());
        Assert.assertEquals("val", doc.child(0).attr("attr"));
        Assert.assertEquals("inner", doc.child(0).child(0).tagName());
    }

    @Test
    public void testParseSelfClosingKnownAndUnknownTags() {
        String xml = "<custom unknown=\"true\"/><br/><img src=\"foo.jpg\"/>";
        Document doc = treeBuilder.parse(xml, "");
        Assert.assertEquals(3, doc.children().size());
        Element custom = doc.child(0);
        Element br = doc.child(1);
        Element img = doc.child(2);
        Assert.assertEquals("custom", custom.tagName());
        Assert.assertTrue(custom.tag().isSelfClosing());
        Assert.assertEquals("br", br.tagName());
        Assert.assertEquals("img", img.tagName());
    }

    @Test
    public void testParseCommentsAndDeclarations() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!-- regular comment --><!bogus declaration><root/>";
        Document doc = treeBuilder.parse(xml, "");
        Assert.assertEquals(4, doc.childNodes().size());
        
        Node declNode = doc.childNode(0);
        Assert.assertTrue(declNode instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) declNode;
        Assert.assertEquals("xml", decl.name());
        Assert.assertEquals("1.0", decl.attr("version"));
        Assert.assertEquals("UTF-8", decl.attr("encoding"));

        Node commentNode = doc.childNode(1);
        Assert.assertTrue(commentNode instanceof Comment);
        Assert.assertEquals(" regular comment ", ((Comment) commentNode).getData());

        Node bogusNode = doc.childNode(2);
        Assert.assertTrue(bogusNode instanceof XmlDeclaration || bogusNode instanceof Comment);
    }

    @Test
    public void testParseDoctype() {
        String xml = "<!DOCTYPE html SYSTEM \"about:legacy-compat\"><root/>";
        Document doc = treeBuilder.parse(xml, "");
        Assert.assertEquals(2, doc.childNodes().size());
        Assert.assertTrue(doc.childNode(0) instanceof DocumentType);
        DocumentType dt = (DocumentType) doc.childNode(0);
        Assert.assertEquals("html", dt.name());
        Assert.assertEquals("about:legacy-compat", dt.attr("systemId"));
    }

    @Test
    public void testParseCDataAndText() {
        String xml = "<root><![CDATA[<foo>&bar</foo>]]>Normal Text</root>";
        Document doc = treeBuilder.parse(xml, "");
        Element root = doc.child(0);
        Assert.assertEquals(2, root.childNodes().size());
        Assert.assertTrue(root.childNode(0) instanceof CDataNode);
        Assert.assertEquals("<foo>&bar</foo>", ((CDataNode) root.childNode(0)).text());
        Assert.assertTrue(root.childNode(1) instanceof TextNode);
        Assert.assertEquals("Normal Text", ((TextNode) root.childNode(1)).text());
    }

    @Test
    public void testUnclosedAndMismatchedTags() {
        String xml = "<a><b><c>text</a>";
        Document doc = treeBuilder.parse(xml, "");
        Assert.assertEquals(1, doc.children().size());
        Element a = doc.child(0);
        Assert.assertEquals("a", a.tagName());
        Element b = a.child(0);
        Assert.assertEquals("b", b.tagName());
        Element c = b.child(0);
        Assert.assertEquals("c", c.tagName());
        Assert.assertEquals("text", c.text());
    }

    @Test
    public void testPopStackToCloseNotFound() {
        String xml = "<root></nonexistent><child>data</child></root>";
        Document doc = treeBuilder.parse(xml, "");
        Assert.assertEquals(1, doc.children().size());
        Element root = doc.child(0);
        Assert.assertEquals("root", root.tagName());
        Assert.assertEquals(1, root.children().size());
        Assert.assertEquals("child", root.child(0).tagName());
    }

    @Test
    public void testParseFragment() {
        Parser parser = Parser.xmlParser();
        List<Node> nodes = treeBuilder.parseFragment("<one/><two>text</two>", "http://example.com", parser);
        Assert.assertEquals(2, nodes.size());
        Assert.assertTrue(nodes.get(0) instanceof Element);
        Assert.assertEquals("one", ((Element) nodes.get(0)).tagName());
        Assert.assertTrue(nodes.get(1) instanceof Element);
        Assert.assertEquals("two", ((Element) nodes.get(1)).tagName());
        Assert.assertEquals("text", ((Element) nodes.get(1)).text());
    }

    @Test
    public void testParseFragmentWithContext() {
        Parser parser = Parser.xmlParser();
        Element context = new Element(Tag.valueOf("div"), "");
        List<Node> nodes = treeBuilder.parseFragment("<foo>bar</foo>", context, "http://example.com", parser);
        Assert.assertEquals(1, nodes.size());
        Assert.assertEquals("foo", ((Element) nodes.get(0)).tagName());
        Assert.assertEquals("bar", ((Element) nodes.get(0)).text());
    }

    @Test
    public void testCasePreservation() {
        String xml = "<CaseSensitive TagName=\"Value\" ATTR=\"CAPS\">Text</CaseSensitive>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        Element el = doc.child(0);
        Assert.assertEquals("CaseSensitive", el.tagName());
        Assert.assertEquals("Value", el.attr("TagName"));
        Assert.assertEquals("CAPS", el.attr("ATTR"));
    }

    @Test
    public void testProcessEOF() {
        treeBuilder.initialiseParse(new StringReader(""), "", new Parser(treeBuilder));
        Token.EOF eof = new Token.EOF();
        Assert.assertTrue(treeBuilder.process(eof));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProcessInvalidTokenThrowsException() {
        treeBuilder.initialiseParse(new StringReader(""), "", new Parser(treeBuilder));
        Token token = new Token() {
            @Override
            Token reset() {
                return this;
            }
        };
        treeBuilder.process(token);
    }
}
