package org.jsoup.parser;

import org.jsoup.nodes.*;
import org.jsoup.helper.Validate;
import org.junit.Test;
import static org.junit.Assert.*;

public class ParserTest {

    @Test
    public void testParseSimpleHtml() {
        String html = "<html><head><title>Test</title></head><body><p>Hello</p></body></html>";
        Document doc = Parser.parse(html, "http://example.com");
        assertNotNull(doc);
        assertEquals("Test", doc.title());
        assertEquals("Hello", doc.body().text());
    }

    @Test
    public void testParseBodyFragment() {
        String fragment = "<div>test</div>";
        Document doc = Parser.parseBodyFragment(fragment, "http://example.com");
        assertNotNull(doc);
        assertNotNull(doc.body());
        assertEquals("<div>test</div>", doc.body().html().trim());
    }

    @Test
    public void testParseBodyFragmentRelaxed() {
        String fragment = "<td>cell</td>";
        Document doc = Parser.parseBodyFragmentRelaxed(fragment, "http://example.com");
        assertNotNull(doc);
        // In relaxed mode, no implicit table/tr should be created
        Element body = doc.body();
        assertEquals(1, body.children().size());
        assertEquals("td", body.child(0).tagName());
    }

    @Test
    public void testParseComment() {
        String html = "<!-- comment -->";
        Document doc = Parser.parse(html, "http://example.com");
        assertNotNull(doc);
        // Comment should be in body
        Element body = doc.body();
        assertEquals(1, body.childNodeSize());
        assertTrue(body.childNode(0) instanceof Comment);
        assertEquals(" comment ", ((Comment) body.childNode(0)).getData());
    }

    @Test
    public void testParseCommentWithDashEnding() {
        String html = "<!-- comment -- -->";
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        Comment comment = (Comment) body.childNode(0);
        // The trailing dash before --> should be stripped
        assertEquals(" comment - ", comment.getData());
    }

    @Test
    public void testParseCdata() {
        String html = "<![CDATA[ raw data ]]>";
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        assertEquals(1, body.childNodeSize());
        assertTrue(body.childNode(0) instanceof TextNode);
        assertEquals(" raw data ", ((TextNode) body.childNode(0)).getWholeText());
    }

    @Test
    public void testParseXmlDeclaration() {
        String html = "<?xml version='1.0'?>";
        Document doc = Parser.parse(html, "http://example.com");
        // XmlDeclaration should be added to body
        Element body = doc.body();
        assertEquals(1, body.childNodeSize());
        assertTrue(body.childNode(0) instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) body.childNode(0);
        assertEquals("xml version='1.0'", decl.getData());
        assertFalse(decl.isProcessingInstruction());
    }

    @Test
    public void testParseXmlDeclarationProcInstr() {
        String html = "<!DOCTYPE html>";
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        XmlDeclaration decl = (XmlDeclaration) body.childNode(0);
        assertTrue(decl.isProcessingInstruction());
    }

    @Test
    public void testParseSelfClosingTag() {
        String html = "<br/>";
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        assertEquals(1, body.children().size());
        assertEquals("br", body.child(0).tagName());
        assertTrue(body.child(0).tag().isEmpty());
    }

    @Test
    public void testParseUnknownSelfClosingTag() {
        String html = "<foo/>";
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        Element foo = body.child(0);
        assertEquals("foo", foo.tagName());
        assertTrue(foo.tag().isSelfClosing());
    }

    @Test
    public void testParseAttributesDoubleQuoted() {
        String html = "<div id=\"main\" class=\"test\">content</div>";
        Document doc = Parser.parse(html, "http://example.com");
        Element div = doc.body().child(0);
        assertEquals("main", div.attr("id"));
        assertEquals("test", div.attr("class"));
    }

    @Test
    public void testParseAttributesSingleQuoted() {
        String html = "<div id='main' class='test'>content</div>";
        Document doc = Parser.parse(html, "http://example.com");
        Element div = doc.body().child(0);
        assertEquals("main", div.attr("id"));
        assertEquals("test", div.attr("class"));
    }

    @Test
    public void testParseAttributesUnquoted() {
        String html = "<div id=main class=test>content</div>";
        Document doc = Parser.parse(html, "http://example.com");
        Element div = doc.body().child(0);
        assertEquals("main", div.attr("id"));
        assertEquals("test", div.attr("class"));
    }

    @Test
    public void testParseBooleanAttribute() {
        String html = "<input disabled>";
        Document doc = Parser.parse(html, "http://example.com");
        Element input = doc.body().child(0);
        assertTrue(input.hasAttr("disabled"));
        assertEquals("", input.attr("disabled"));
    }

    @Test
    public void testParseDataTagScript() {
        String html = "<script>var x = 1;</script>";
        Document doc = Parser.parse(html, "http://example.com");
        Element script = doc.body().child(0);
        assertEquals(1, script.childNodeSize());
        assertTrue(script.childNode(0) instanceof DataNode);
        assertEquals("var x = 1;", ((DataNode) script.childNode(0)).getWholeData());
    }

    @Test
    public void testParseDataTagTitle() {
        String html = "<title>My Title</title>";
        Document doc = Parser.parse(html, "http://example.com");
        Element title = doc.head().child(0);
        assertEquals(1, title.childNodeSize());
        assertTrue(title.childNode(0) instanceof TextNode);
        assertEquals("My Title", ((TextNode) title.childNode(0)).getWholeText());
    }

    @Test
    public void testParseDataTagTextarea() {
        String html = "<textarea>some text</textarea>";
        Document doc = Parser.parse(html, "http://example.com");
        Element textarea = doc.body().child(0);
        assertEquals(1, textarea.childNodeSize());
        assertTrue(textarea.childNode(0) instanceof TextNode);
        assertEquals("some text", ((TextNode) textarea.childNode(0)).getWholeText());
    }

    @Test
    public void testParseBaseTagUpdatesBaseUri() {
        String html = "<base href=\"http://newbase.com/\"><a href=\"page.html\">link</a>";
        Document doc = Parser.parse(html, "http://example.com");
        Element a = doc.body().child(0);
        assertEquals("http://newbase.com/page.html", a.absUrl("href"));
    }

    @Test
    public void testParseEndTag() {
        String html = "<div><p>text</p></div>";
        Document doc = Parser.parse(html, "http://example.com");
        Element div = doc.body().child(0);
        assertEquals(1, div.children().size());
        assertEquals("p", div.child(0).tagName());
    }

    @Test
    public void testParseEndTagIgnoreNonOpen() {
        String html = "<div></p></div>";
        Document doc = Parser.parse(html, "http://example.com");
        // The </p> should be ignored because no <p> is open
        Element div = doc.body().child(0);
        assertEquals(0, div.children().size());
    }

    @Test
    public void testParseTextNode() {
        String html = "plain text";
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        assertEquals(1, body.childNodeSize());
        assertTrue(body.childNode(0) instanceof TextNode);
        assertEquals("plain text", ((TextNode) body.childNode(0)).getWholeText());
    }

    @Test
    public void testParseTextNodeWithLeadingLessThan() {
        String html = "hello < there";
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        // Should produce two text nodes: "hello " and "< there"
        assertEquals(2, body.childNodeSize());
        assertEquals("hello ", ((TextNode) body.childNode(0)).getWholeText());
        assertEquals("< there", ((TextNode) body.childNode(1)).getWholeText());
    }

    @Test
    public void testParseImplicitParentCreation() {
        String html = "<td>cell</td>";
        Document doc = Parser.parse(html, "http://example.com");
        // Should create implicit table > tbody > tr > td
        Element body = doc.body();
        Element table = body.child(0);
        assertEquals("table", table.tagName());
        Element tbody = table.child(0);
        assertEquals("tbody", tbody.tagName());
        Element tr = tbody.child(0);
        assertEquals("tr", tr.tagName());
        Element td = tr.child(0);
        assertEquals("td", td.tagName());
        assertEquals("cell", td.text());
    }

    @Test
    public void testParseImplicitParentBodyTag() {
        String html = "<body><p>text</p></body>";
        Document doc = Parser.parse(html, "http://example.com");
        // Should not create extra body, but ensure head exists
        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertEquals(1, doc.body().children().size());
    }

    @Test
    public void testParseRelaxedNoImplicitParent() {
        String html = "<td>cell</td>";
        Document doc = Parser.parseBodyFragmentRelaxed(html, "http://example.com");
        Element body = doc.body();
        assertEquals(1, body.children().size());
        assertEquals("td", body.child(0).tagName());
    }

    @Test
    public void testParseEmptyString() {
        Document doc = Parser.parse("", "http://example.com");
        assertNotNull(doc);
        assertNotNull(doc.body());
        assertEquals(0, doc.body().childNodeSize());
    }

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
        Parser.parseBodyFragment("<div></div>", null);
    }

    @Test
    public void testParseOnlyText() {
        String html = "just text";
        Document doc = Parser.parse(html, "http://example.com");
        assertEquals("just text", doc.body().text());
    }

    @Test
    public void testParseNestedTags() {
        String html = "<div><span><b>bold</b></span></div>";
        Document doc = Parser.parse(html, "http://example.com");
        Element div = doc.body().child(0);
        Element span = div.child(0);
        Element b = span.child(0);
        assertEquals("bold", b.text());
    }

    @Test
    public void testParseAttributeEmptyKey() {
        String html = "<div =value>content</div>";
        Document doc = Parser.parse(html, "http://example.com");
        Element div = doc.body().child(0);
        // The empty key attribute should be ignored (null returned)
        assertFalse(div.hasAttr(""));
    }

    @Test
    public void testParseEndTagEmptyName() {
        String html = "<div></>";
        Document doc = Parser.parse(html, "http://example.com");
        // The </> should be ignored because tagName is empty
        Element div = doc.body().child(0);
        assertEquals(0, div.children().size());
    }

    @Test
    public void testParseCommentNoEnd() {
        String html = "<!-- comment";
        Document doc = Parser.parse(html, "http://example.com");
        // Should treat as comment until end of stream
        Element body = doc.body();
        assertTrue(body.childNode(0) instanceof Comment);
    }

    @Test
    public void testParseCdataNoEnd() {
        String html = "<![CDATA[ data";
        Document doc = Parser.parse(html, "http://example.com");
        // Should treat as text until end of stream
        Element body = doc.body();
        assertTrue(body.childNode(0) instanceof TextNode);
    }

    @Test
    public void testParseXmlDeclNoEnd() {
        String html = "<?xml version='1.0'";
        Document doc = Parser.parse(html, "http://example.com");
        // Should treat as XmlDeclaration until end of stream
        Element body = doc.body();
        assertTrue(body.childNode(0) instanceof XmlDeclaration);
    }

    @Test
    public void testParseMultipleTags() {
        String html = "<p>first</p><p>second</p>";
        Document doc = Parser.parse(html, "http://example.com");
        Element body = doc.body();
        assertEquals(2, body.children().size());
        assertEquals("first", body.child(0).text());
        assertEquals("second", body.child(1).text());
    }
}
