package org.jsoup.parser;

import org.jsoup.nodes.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class ParserTest {

    @Test(expected = IllegalArgumentException.class)
    public void testParseNullHtml() {
        Parser.parse(null, "http://example.com");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseNullBaseUri() {
        Parser.parse("<html></html>", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseBodyFragmentNullHtml() {
        Parser.parseBodyFragment(null, "http://example.com");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseBodyFragmentNullBaseUri() {
        Parser.parseBodyFragment("<p>test</p>", null);
    }

    @Test
    public void testParseEmptyString() {
        Document doc = Parser.parse("", "http://example.com");
        assertNotNull(doc);
        assertNotNull(doc.html());
    }

    @Test
    public void testParseSimpleHtml() {
        String html = "<html><head><title>Test</title></head><body><p>Hello</p></body></html>";
        Document doc = Parser.parse(html, "http://example.com");
        assertEquals("Test", doc.title());
        Element body = doc.body();
        assertEquals(1, body.children().size());
        Element p = body.child(0);
        assertEquals("p", p.tagName());
        assertEquals("Hello", p.text());
    }

    @Test
    public void testParseComment() {
        String html = "<!-- comment -->";
        Document doc = Parser.parse(html, "http://example.com");
        boolean found = false;
        for (Node node : doc.childNodes()) {
            if (node instanceof Comment) {
                Comment c = (Comment) node;
                assertEquals(" comment ", c.getData());
                found = true;
            }
        }
        assertTrue("Comment not found", found);
    }

    @Test
    public void testParseCommentWithDash() {
        String html = "<!-- comment -->";
        Document doc = Parser.parse(html, "http://example.com");
        Comment comment = (Comment) doc.childNode(0);
        assertEquals(" comment ", comment.getData());
    }

    @Test
    public void testParseCdata() {
        String html = "<![CDATA[ <p>raw text</p> ]]>";
        Document doc = Parser.parse(html, "http://example.com");
        Node node = doc.childNode(0);
        assertTrue(node instanceof TextNode);
        assertEquals(" <p>raw text</p> ", ((TextNode) node).getWholeText());
    }

    @Test
    public void testParseXmlDeclaration() {
        String html = "<?xml version='1.0'?>";
        Document doc = Parser.parse(html, "http://example.com");
        Node node = doc.childNode(0);
        assertTrue(node instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) node;
        assertEquals("xml version='1.0'", decl.getData());
        assertFalse(decl.isProcessingInstruction());
    }

    @Test
    public void testParseXmlDeclarationProcInstr() {
        String html = "<!DOCTYPE html>";
        Document doc = Parser.parse(html, "http://example.com");
        Node node = doc.childNode(0);
        assertTrue(node instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) node;
        assertEquals("DOCTYPE html", decl.getData());
        assertTrue(decl.isProcessingInstruction());
    }

    @Test
    public void testParseEndTag() {
        String html = "<div></div>";
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        assertEquals(1, body.children().size());
        Element div = body.child(0);
        assertEquals("div", div.tagName());
        assertTrue(div.children().isEmpty());
    }

    @Test
    public void testParseEndTagEmptyName() {
        String html = "</>";
        Document doc = Parser.parse(html, "http://example.com");
        assertNotNull(doc);
    }

    @Test
    public void testParseStartTagEmptyName() {
        String html = "<";
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        TextNode textNode = (TextNode) body.childNode(0);
        assertEquals("<", textNode.getWholeText());
    }

    @Test
    public void testParseSelfClosingTag() {
        String html = "<br/>";
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        assertEquals(1, body.children().size());
        Element br = body.child(0);
        assertEquals("br", br.tagName());
        assertTrue(br.children().isEmpty());
    }

    @Test
    public void testParseAttributes() {
        String html = "<a href='http://example.com' target=\"_blank\" id=test>link</a>";
        Document doc = Parser.parse(html, "http://example.com");
        Element a = doc.body().child(0);
        assertEquals("http://example.com", a.attr("href"));
        assertEquals("_blank", a.attr("target"));
        assertEquals("test", a.attr("id"));
    }

    @Test
    public void testParseAttributeNoValue() {
        String html = "<input disabled>";
        Document doc = Parser.parse(html, "http://example.com");
        Element input = doc.body().child(0);
        assertTrue(input.hasAttr("disabled"));
        assertEquals("", input.attr("disabled"));
    }

    @Test
    public void testParseAttributeSingleQuote() {
        String html = "<div attr='value'>";
        Document doc = Parser.parse(html, "http://example.com");
        Element div = doc.body().child(0);
        assertEquals("value", div.attr("attr"));
    }

    @Test
    public void testParseAttributeDoubleQuote() {
        String html = "<div attr=\"value\">";
        Document doc = Parser.parse(html, "http://example.com");
        Element div = doc.body().child(0);
        assertEquals("value", div.attr("attr"));
    }

    @Test
    public void testParseAttributeUnquoted() {
        String html = "<div attr=value>";
        Document doc = Parser.parse(html, "http://example.com");
        Element div = doc.body().child(0);
        assertEquals("value", div.attr("attr"));
    }

    @Test
    public void testParseAttributeEmptyKey() {
        String html = "<div =value>";
        Document doc = Parser.parse(html, "http://example.com");
        Element div = doc.body().child(0);
        assertTrue(div.attributes().asList().isEmpty());
    }

    @Test
    public void testParseAttributeEmptyValue() {
        String html = "<div key=>";
        Document doc = Parser.parse(html, "http://example.com");
        Element div = doc.body().child(0);
        assertEquals("", div.attr("key"));
    }

    @Test
    public void testParseAttributeSpaceAfterEquals() {
        String html = "<div key = value>";
        Document doc = Parser.parse(html, "http://example.com");
        Element div = doc.body().child(0);
        assertEquals("value", div.attr("key"));
    }

    @Test
    public void testParseDataTagScript() {
        String html = "<script>var x = '<test>';</script>";
        Document doc = Parser.parse(html, "http://example.com");
        Element script = doc.body().child(0);
        assertEquals("script", script.tagName());
        assertEquals(1, script.children().size());
        Node dataNode = script.childNode(0);
        assertTrue(dataNode instanceof DataNode);
        assertEquals("var x = '<test>';", ((DataNode) dataNode).getWholeData());
    }

    @Test
    public void testParseDataTagTextarea() {
        String html = "<textarea>some text</textarea>";
        Document doc = Parser.parse(html, "http://example.com");
        Element textarea = doc.body().child(0);
        assertEquals("textarea", textarea.tagName());
        Node textNode = textarea.childNode(0);
        assertTrue(textNode instanceof TextNode);
        assertEquals("some text", ((TextNode) textNode).getWholeText());
    }

    @Test
    public void testParseDataTagTitle() {
        String html = "<title>Page Title</title>";
        Document doc = Parser.parse(html, "http://example.com");
        Element head = doc.head();
        Element title = head.child(0);
        assertEquals("title", title.tagName());
        Node textNode = title.childNode(0);
        assertTrue(textNode instanceof TextNode);
        assertEquals("Page Title", ((TextNode) textNode).getWholeText());
    }

    @Test
    public void testParseBaseTagUpdatesBaseUri() {
        String html = "<base href='http://newbase.com/'><a href='page.html'>link</a>";
        Document doc = Parser.parse(html, "http://oldbase.com");
        assertEquals("http://newbase.com/", doc.baseUri());
        Element a = doc.body().child(0);
        assertEquals("http://newbase.com/page.html", a.absUrl("href"));
    }

    @Test
    public void testParseBaseTagNoHref() {
        String html = "<base target='_blank'>";
        Document doc = Parser.parse(html, "http://example.com");
        assertEquals("http://example.com", doc.baseUri());
    }

    @Test
    public void testParseImplicitParentCreation() {
        String html = "<p>text</p>";
        Document doc = Parser.parse(html, "http://example.com");
        assertNotNull(doc.html());
        assertNotNull(doc.head());
        assertNotNull(doc.body());
        Element p = doc.body().child(0);
        assertEquals("p", p.tagName());
        assertEquals("text", p.text());
    }

    @Test
    public void testParseBodyFragment() {
        String fragment = "<div><p>Hello</p></div>";
        Document doc = Parser.parseBodyFragment(fragment, "http://example.com");
        assertNotNull(doc.head());
        assertTrue(doc.head().children().isEmpty());
        Element body = doc.body();
        assertEquals(1, body.children().size());
        Element div = body.child(0);
        assertEquals("div", div.tagName());
        Element p = div.child(0);
        assertEquals("p", p.tagName());
        assertEquals("Hello", p.text());
    }

    @Test
    public void testParseBodyFragmentTextOnly() {
        String fragment = "Just text";
        Document doc = Parser.parseBodyFragment(fragment, "http://example.com");
        Element body = doc.body();
        assertEquals("Just text", body.text());
    }

    @Test
    public void testParseEndTagNotInStack() {
        String html = "<div></span></div>";
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        assertEquals(1, body.children().size());
        Element div = body.child(0);
        assertEquals("div", div.tagName());
    }

    @Test
    public void testParseEndTagPastBody() {
        String html = "<body><div></div></body></html>";
        Document doc = Parser.parse(html, "http://example.com");
        assertNotNull(doc);
    }

    @Test
    public void testStackHasValidParentRootHtml() {
        String html = "<html><head></head><body></body></html>";
        Document doc = Parser.parse(html, "http://example.com");
        assertNotNull(doc);
    }

    @Test
    public void testParseBodyTagCreatesImplicitHead() {
        String html = "<body><p>text</p></body>";
        Document doc = Parser.parse(html, "http://example.com");
        assertNotNull(doc.head());
        assertNotNull(doc.body());
    }

    @Test
    public void testParseTextNodeWithEntities() {
        String html = "&lt;div&gt;";
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        assertEquals("<div>", body.text());
    }

    @Test
    public void testParseImplicitTableCreation() {
        String html = "<tr><td>cell</td></tr>";
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        Element table = body.child(0);
        assertEquals("table", table.tagName());
        Element tr = table.child(0);
        assertEquals("tr", tr.tagName());
        Element td = tr.child(0);
        assertEquals("td", td.tagName());
        assertEquals("cell", td.text());
    }
}
