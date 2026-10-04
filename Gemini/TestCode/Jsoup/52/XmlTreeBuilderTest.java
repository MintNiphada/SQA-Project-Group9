package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.XmlDeclaration;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class XmlTreeBuilderTest {

    @Test
    public void testParseXmlDeclaration() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root>text</root>";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://example.com/");
        
        assertEquals(Document.OutputSettings.Syntax.xml, doc.outputSettings().syntax());
        assertEquals(2, doc.childNodeSize());
        
        Node declNode = doc.childNode(0);
        assertTrue(declNode instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) declNode;
        assertEquals("xml version=\"1.0\" encoding=\"UTF-8\"", decl.getWholeDeclaration());
        assertFalse(decl.toString().startsWith("<!"));
        
        Element root = doc.select("root").first();
        assertNotNull(root);
        assertEquals("text", root.text());
    }

    @Test
    public void testParseExclamationDeclaration() {
        String xml = "<!DECL something><root/>";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://example.com/");
        
        assertTrue(doc.childNode(0) instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) doc.childNode(0);
        assertTrue(decl.toString().startsWith("<!"));
    }

    @Test
    public void testParseComment() {
        String xml = "<!-- standard comment --><root/>";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://example.com/");
        
        assertTrue(doc.childNode(0) instanceof Comment);
        Comment comment = (Comment) doc.childNode(0);
        assertEquals(" standard comment ", comment.getData());
    }

    @Test
    public void testBogusCommentVariations() {
        XmlTreeBuilder tb = new XmlTreeBuilder();
        tb.initialiseParse("<dummy/>", "http://example.com/", ParseErrorList.noTracking());

        Token.Comment bogusShort = new Token.Comment();
        bogusShort.bogus = true;
        bogusShort.data.append("?");
        tb.insert(bogusShort);
        assertTrue(tb.doc.childNode(0) instanceof Comment);

        Token.Comment bogusNoPrefix = new Token.Comment();
        bogusNoPrefix.bogus = true;
        bogusNoPrefix.data.append("abc");
        tb.insert(bogusNoPrefix);
        assertTrue(tb.doc.childNode(1) instanceof Comment);

        Token.Comment bogusExclamation = new Token.Comment();
        bogusExclamation.bogus = true;
        bogusExclamation.data.append("!custom decl");
        tb.insert(bogusExclamation);
        assertTrue(tb.doc.childNode(2) instanceof XmlDeclaration);

        Token.Comment bogusQuestion = new Token.Comment();
        bogusQuestion.bogus = true;
        bogusQuestion.data.append("?custom decl");
        tb.insert(bogusQuestion);
        assertTrue(tb.doc.childNode(3) instanceof XmlDeclaration);
    }

    @Test
    public void testParseDoctype() {
        String xml = "<!DOCTYPE html SYSTEM \"about:legacy-compat\"><root/>";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://example.com/");
        
        assertTrue(doc.childNode(0) instanceof DocumentType);
        DocumentType dt = (DocumentType) doc.childNode(0);
        assertEquals("html", dt.attr("name"));
        assertEquals("about:legacy-compat", dt.attr("systemId"));
    }

    @Test
    public void testSelfClosingTagsKnownAndUnknown() {
        String xml = "<root><unknown id=\"1\"/><br/><img src=\"foo.jpg\"/><custom>test</custom></root>";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://example.com/");
        
        Element root = doc.child(0);
        assertEquals(4, root.children().size());
        
        Element unknown = root.child(0);
        assertEquals("unknown", unknown.tagName());
        assertTrue(Tag.valueOf("unknown").isSelfClosing());
        
        Element br = root.child(1);
        assertEquals("br", br.tagName());
        
        Element custom = root.child(3);
        assertEquals("custom", custom.tagName());
        assertEquals("test", custom.text());
    }

    @Test
    public void testStackClosingNestedElements() {
        String xml = "<a><b><c><d>text</a>";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://example.com/");
        
        assertEquals(1, doc.children().size());
        Element a = doc.child(0);
        assertEquals("a", a.tagName());
        assertEquals("text", a.text());
        assertEquals(1, a.children().size());
        assertEquals("b", a.child(0).tagName());
    }

    @Test
    public void testPopStackWhenEndTagNotFound() {
        String xml = "<root><child>value</notfound></child></root>";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://example.com/");
        
        Element root = doc.select("root").first();
        assertNotNull(root);
        assertEquals("value", root.select("child").text());
    }

    @Test
    public void testParseFragment() {
        XmlTreeBuilder tb = new XmlTreeBuilder();
        ParseErrorList errors = ParseErrorList.tracking(10);
        List<Node> nodes = tb.parseFragment("<one>1</one><two>2</two>", "http://example.com/", errors);
        
        assertEquals(2, nodes.size());
        assertEquals("one", nodes.get(0).nodeName());
        assertEquals("two", nodes.get(1).nodeName());
    }

    @Test
    public void testCharacterInsertion() {
        String xml = "<root>Hello &amp; World</root>";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://example.com/");
        
        Element root = doc.child(0);
        assertEquals(1, root.childNodeSize());
        assertTrue(root.childNode(0) instanceof TextNode);
        assertEquals("Hello & World", ((TextNode) root.childNode(0)).getWholeText());
    }

    @Test
    public void testProcessEof() {
        XmlTreeBuilder tb = new XmlTreeBuilder();
        tb.initialiseParse("<root></root>", "http://example.com/", ParseErrorList.noTracking());
        Token.EOF eof = new Token.EOF();
        boolean processed = tb.process(eof);
        assertTrue(processed);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnexpectedTokenType() {
        XmlTreeBuilder tb = new XmlTreeBuilder();
        tb.initialiseParse("<root></root>", "http://example.com/", ParseErrorList.noTracking());
        Token token = new Token() {
            @Override
            Token reset() {
                return this;
            }
        };
        tb.process(token);
    }

    @Test
    public void testCaseSensitivity() {
        String xml = "<MixedCase>Test<CHILD>Content</CHILD></MixedCase>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        assertEquals("<MixedCase>Test<CHILD>Content</CHILD></MixedCase>", doc.outerHtml().replaceAll("\\r?\\n", ""));
    }
}
