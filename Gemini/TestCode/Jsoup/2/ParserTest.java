package org.jsoup.parser;

import org.jsoup.nodes.Comment;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.XmlDeclaration;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ParserTest {

    @Test(expected = IllegalArgumentException.class)
    public void testParseNullHtml() {
        Parser.parse(null, "http://example.com");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseNullBaseUri() {
        Parser.parse("<div></div>", null);
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
    public void testBasicHtmlDocument() {
        String html = "<html><head><title>Test Page</title></head><body><p>Hello World</p></body></html>";
        Document doc = Parser.parse(html, "http://example.com");

        assertNotNull(doc);
        assertEquals("Test Page", doc.title());
        Element p = doc.select("p").first();
        assertNotNull(p);
        assertEquals("Hello World", p.text());
        assertEquals("http://example.com", doc.baseUri());
    }

    @Test
    public void testParseBodyFragment() {
        String fragment = "<div><p>Fragment Paragraph</p></div>";
        Document doc = Parser.parseBodyFragment(fragment, "http://example.com/");

        assertNotNull(doc);
        assertNotNull(doc.body());
        Elements divs = doc.body().select("div");
        assertEquals(1, divs.size());
        assertEquals("Fragment Paragraph", divs.first().text());
    }

    @Test
    public void testCommentsParsing() {
        String html = "<div><!-- This is a regular comment --><span>Text</span><!-- Strange comment -></div>";
        Document doc = Parser.parse(html, "http://example.com");

        List<Node> childNodes = doc.body().child(0).childNodes();
        boolean foundNormalComment = false;
        boolean foundStrangeComment = false;

        for (Node node : childNodes) {
            if (node instanceof Comment) {
                Comment comment = (Comment) node;
                if (comment.getData().equals(" This is a regular comment ")) {
                    foundNormalComment = true;
                } else if (comment.getData().equals(" Strange comment ")) {
                    foundStrangeComment = true;
                }
            }
        }
        assertTrue("Expected standard comment to be parsed", foundNormalComment);
        assertTrue("Expected comment ending with -> to be parsed", foundStrangeComment);
    }

    @Test
    public void testCdataParsing() {
        String html = "<div><![CDATA[Some <unescaped> & raw CDATA content]]></div>";
        Document doc = Parser.parse(html, "http://example.com");

        Element div = doc.select("div").first();
        assertNotNull(div);
        assertEquals(1, div.childNodes().size());
        Node child = div.childNode(0);
        assertTrue(child instanceof TextNode);
        assertEquals("Some <unescaped> & raw CDATA content", ((TextNode) child).getWholeText());
    }

    @Test
    public void testXmlDeclarationAndDoctype() {
        String html = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE html><html><body><p>Test</p></body></html>";
        Document doc = Parser.parse(html, "http://example.com");

        assertNotNull(doc);
        List<Node> nodes = doc.childNodes();
        boolean foundXmlDecl = false;
        boolean foundDocType = false;

        for (Node node : nodes) {
            if (node instanceof XmlDeclaration) {
                XmlDeclaration decl = (XmlDeclaration) node;
                if (decl.getWholeDeclaration().contains("xml version=\"1.0\"")) {
                    foundXmlDecl = true;
                }
                if (decl.getWholeDeclaration().contains("DOCTYPE html")) {
                    foundDocType = true;
                }
            }
        }
        assertTrue("Xml declaration should be parsed", foundXmlDecl);
        assertTrue("DocType declaration should be parsed", foundDocType);
    }

    @Test
    public void testStartTagWithInvalidName() {
        String html = "< notatag <p>valid</p>";
        Document doc = Parser.parse(html, "http://example.com");

        assertNotNull(doc);
        assertTrue(doc.text().contains("< notatag") || doc.html().contains("&lt; notatag"));
        Element p = doc.select("p").first();
        assertNotNull(p);
        assertEquals("valid", p.text());
    }

    @Test
    public void testAttributesVariations() {
        String html = "<a href='http://single.com' title=\"double quoted\" target=_blank rel= rel2=nohref checked disabled=>Link</a>";
        Document doc = Parser.parse(html, "http://example.com");

        Element a = doc.select("a").first();
        assertNotNull(a);
        assertEquals("http://single.com", a.attr("href"));
        assertEquals("double quoted", a.attr("title"));
        assertEquals("_blank", a.attr("target"));
        assertEquals("", a.attr("rel"));
        assertEquals("nohref", a.attr("rel2"));
        assertTrue(a.hasAttr("checked"));
        assertTrue(a.hasAttr("disabled"));
    }

    @Test
    public void testAttributeKeyEmptyRecovery() {
        String html = "<div ==\"bad\" id=\"good\">Content</div>";
        Document doc = Parser.parse(html, "http://example.com");

        Element div = doc.select("div").first();
        assertNotNull(div);
        assertEquals("good", div.attr("id"));
    }

    @Test
    public void testSelfClosingAndEmptyTags() {
        String html = "<div><img src=\"test.jpg\" /><input type=\"text\"><hr/></div>";
        Document doc = Parser.parse(html, "http://example.com");

        Element div = doc.select("div").first();
        assertNotNull(div);
        assertEquals(3, div.children().size());
        assertEquals("img", div.child(0).tagName());
        assertEquals("test.jpg", div.child(0).attr("src"));
        assertEquals("input", div.child(1).tagName());
        assertEquals("hr", div.child(2).tagName());
    }

    @Test
    public void testDataTagsHandling() {
        String html = "<title>Page &amp; Title</title>" +
                      "<textarea>Inside &amp; <textarea> content</textarea>" +
                      "<script>var x = 1 < 2 ? \"<b>yes</b>\" : \"no\";</script>" +
                      "<style>body > div { color: red; }</style>";
        Document doc = Parser.parse(html, "http://example.com");

        Element title = doc.select("title").first();
        assertNotNull(title);
        assertEquals("Page & Title", title.text());

        Element textarea = doc.select("textarea").first();
        assertNotNull(textarea);
        assertEquals("Inside & <textarea> content", textarea.text());

        Element script = doc.select("script").first();
        assertNotNull(script);
        assertEquals(1, script.childNodes().size());
        assertTrue(script.childNode(0) instanceof DataNode);
        assertEquals("var x = 1 < 2 ? \"<b>yes</b>\" : \"no\";", ((DataNode) script.childNode(0)).getWholeData());

        Element style = doc.select("style").first();
        assertNotNull(style);
        assertEquals(1, style.childNodes().size());
        assertTrue(style.childNode(0) instanceof DataNode);
        assertEquals("body > div { color: red; }", ((DataNode) style.childNode(0)).getWholeData());
    }

    @Test
    public void testBaseUriHandling() {
        String html = "<head><base href=\"http://example.com/dir/sub/\"><base target=\"_blank\"></head>" +
                      "<body><a href=\"page.html\">Link</a></body>";
        Document doc = Parser.parse(html, "http://initial.com");

        assertEquals("http://example.com/dir/sub/", doc.baseUri());
        Element a = doc.select("a").first();
        assertNotNull(a);
        assertEquals("http://example.com/dir/sub/page.html", a.absUrl("href"));
    }

    @Test
    public void testImplicitParentCreation() {
        String html = "<td>Lone Cell</td>";
        Document doc = Parser.parse(html, "http://example.com");

        Element td = doc.select("td").first();
        assertNotNull(td);
        assertEquals("Lone Cell", td.text());
        Element tr = td.parent();
        assertNotNull(tr);
        assertEquals("tr", tr.tagName());
        Element table = doc.select("table").first();
        assertNotNull(table);
    }

    @Test
    public void testImplicitBodyAndHeadCreation() {
        String html = "<body><p>In Body</p></body>";
        Document doc = Parser.parse(html, "http://example.com");

        assertNotNull(doc.head());
        assertNotNull(doc.body());
        Element p = doc.select("p").first();
        assertNotNull(p);
        assertEquals("In Body", p.text());
    }

    @Test
    public void testEndTagEdgeCases() {
        // Empty end tag, end tag not matching stack, end tag attempting to close past body
        String html = "<div></><p>Text</unknown></p></div></body></html>";
        Document doc = Parser.parse(html, "http://example.com");

        Element p = doc.select("p").first();
        assertNotNull(p);
        assertEquals("Text", p.text());
    }

    @Test
    public void testUnclosedTagsAndStackRecovery() {
        String html = "<div><p>Paragraph 1<p>Paragraph 2</div>";
        Document doc = Parser.parse(html, "http://example.com");

        Elements ps = doc.select("div > p");
        assertEquals(2, ps.size());
        assertEquals("Paragraph 1", ps.get(0).text());
        assertEquals("Paragraph 2", ps.get(1).text());
    }

    @Test
    public void testNestedElementsAndFormatting() {
        String html = "<p><b>Bold <i>Italic</b> Still Italic?</i> Normal</p>";
        Document doc = Parser.parse(html, "http://example.com");

        Element p = doc.select("p").first();
        assertNotNull(p);
        assertTrue(p.text().contains("Bold"));
        assertTrue(p.text().contains("Italic"));
        assertTrue(p.text().contains("Normal"));
    }
}
