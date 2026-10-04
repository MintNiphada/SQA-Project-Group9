package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class XmlTreeBuilderTest {

    @Test
    public void testSimpleXmlParse() {
        String xml = "<root><child id=\"1\">Test</child></root>";
        Document doc = Jsoup.parse(xml, "http://example.com/", Parser.xmlParser());
        assertEquals(1, doc.children().size());
        Element root = doc.child(0);
        assertEquals("root", root.tagName());
        assertEquals("child", root.child(0).tagName());
        assertEquals("1", root.child(0).attr("id"));
        assertEquals("Test", root.child(0).text());
    }

    @Test
    public void testSelfClosingUnknownTag() {
        String xml = "<custom-tag attr=\"val\" />";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        Element el = doc.child(0);
        assertEquals("custom-tag", el.tagName());
        assertEquals("val", el.attr("attr"));
        assertTrue(el.tag().isSelfClosing());
    }

    @Test
    public void testSelfClosingKnownTag() {
        String xml = "<img src=\"foo.jpg\" />";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        Element el = doc.child(0);
        assertEquals("img", el.tagName());
        assertEquals("foo.jpg", el.attr("src"));
    }

    @Test
    public void testCommentParse() {
        String xml = "<root><!-- This is a comment --></root>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        Element root = doc.child(0);
        assertEquals(1, root.childNodeSize());
        assertTrue(root.childNode(0) instanceof org.jsoup.nodes.Comment);
        assertEquals(" This is a comment ", ((org.jsoup.nodes.Comment) root.childNode(0)).getData());
    }

    @Test
    public void testTextAndCData() {
        String xml = "<root><![CDATA[Some <cdata> data]]></root>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        Element root = doc.child(0);
        assertEquals(1, root.childNodeSize());
        assertTrue(root.childNode(0) instanceof TextNode);
        assertEquals("Some <cdata> data", ((TextNode) root.childNode(0)).getWholeText());
    }

    @Test
    public void testDocTypeParse() {
        String xml = "<!DOCTYPE html SYSTEM \"about:legacy-compat\"><root/>";
        Document doc = Jsoup.parse(xml, "http://example.com/", Parser.xmlParser());
        assertTrue(doc.childNode(0) instanceof DocumentType);
        DocumentType doctype = (DocumentType) doc.childNode(0);
        assertEquals("html", doctype.attr("name"));
        assertEquals("about:legacy-compat", doctype.attr("systemId"));
        assertEquals("http://example.com/", doctype.baseUri());
    }

    @Test
    public void testDocTypeWithPublicAndSystem() {
        String xml = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\"><root/>";
        Document doc = Jsoup.parse(xml, "http://example.com/", Parser.xmlParser());
        assertTrue(doc.childNode(0) instanceof DocumentType);
        DocumentType doctype = (DocumentType) doc.childNode(0);
        assertEquals("html", doctype.attr("name"));
        assertEquals("-//W3C//DTD XHTML 1.0 Strict//EN", doctype.attr("publicId"));
        assertEquals("http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd", doctype.attr("systemId"));
    }

    @Test
    public void testPopStackToCloseUnopenedTag() {
        String xml = "<root></unopened><child>val</child></root>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        assertEquals("root", doc.child(0).tagName());
        assertEquals(1, doc.child(0).children().size());
        assertEquals("child", doc.child(0).child(0).tagName());
    }

    @Test
    public void testPopStackNestedElements() {
        String xml = "<a><b><c><d>text</d></b></c></a>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        assertEquals("<a><b><c><d>text</d></c></b></a>", doc.body().children().first().outerHtml().replaceAll("\\s+", ""));
    }

    @Test
    public void testDirectProcessMethods() {
        XmlTreeBuilder tb = new XmlTreeBuilder();
        tb.initialiseParse("<root/>", "http://example.com/", ParseErrorList.noTracking());

        Token.StartTag start = new Token.StartTag();
        start.name("parent");
        tb.process(start);

        Token.Character character = new Token.Character();
        character.data("text");
        tb.process(character);

        Token.Comment comment = new Token.Comment();
        comment.getData().append("comment data");
        tb.process(comment);

        Token.Doctype doctype = new Token.Doctype();
        doctype.name.append("custom-doc");
        doctype.publicIdentifier.append("pub");
        doctype.systemIdentifier.append("sys");
        tb.process(doctype);

        Token.EndTag end = new Token.EndTag();
        end.name("parent");
        tb.process(end);

        Token.EOF eof = new Token.EOF();
        assertTrue(tb.process(eof));

        Token.EndTag nonExistent = new Token.EndTag();
        nonExistent.name("nonExistent");
        assertTrue(tb.process(nonExistent));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProcessUnexpectedTokenType() {
        XmlTreeBuilder tb = new XmlTreeBuilder();
        tb.initialiseParse("<root/>", "", ParseErrorList.noTracking());
        Token invalidToken = new Token() {};
        tb.process(invalidToken);
    }

    @Test
    public void testParseFragment() {
        XmlTreeBuilder tb = new XmlTreeBuilder();
        List<org.jsoup.nodes.Node> nodes = tb.parseFragment("<one/><two>three</two>", "http://example.com/", ParseErrorList.noTracking());
        assertEquals(2, nodes.size());
        assertEquals("one", nodes.get(0).nodeName());
        assertEquals("two", nodes.get(1).nodeName());
    }

    @Test
    public void testPopStackDeepNesting() {
        String xml = "<a><b><c><d>target</d></c></b></a>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        assertEquals(1, doc.children().size());
        assertEquals("a", doc.child(0).tagName());
        assertEquals("b", doc.child(0).child(0).tagName());
        assertEquals("c", doc.child(0).child(0).child(0).tagName());
        assertEquals("d", doc.child(0).child(0).child(0).child(0).tagName());
        assertEquals("target", doc.child(0).child(0).child(0).child(0).text());
    }
}
