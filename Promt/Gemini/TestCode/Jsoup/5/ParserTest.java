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
        Parser.parse("<p>Hello</p>", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseBodyFragmentNullHtml() {
        Parser.parseBodyFragment(null, "http://example.com");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseBodyFragmentNullBaseUri() {
        Parser.parseBodyFragment("<p>Hello</p>", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseBodyFragmentRelaxedNullHtml() {
        Parser.parseBodyFragmentRelaxed(null, "http://example.com");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseBodyFragmentRelaxedNullBaseUri() {
        Parser.parseBodyFragmentRelaxed("<p>Hello</p>", null);
    }

    @Test
    public void testBasicDocumentStructure() {
        String html = "<html><head><title>Test Title</title></head><body><p>Hello World</p></body></html>";
        Document doc = Parser.parse(html, "http://example.com");

        assertNotNull(doc);
        assertEquals("Test Title", doc.title());
        Element body = doc.body();
        assertNotNull(body);
        assertEquals(1, body.children().size());
        assertEquals("p", body.child(0).tagName());
        assertEquals("Hello World", body.child(0).text());
    }

    @Test
    public void testParseBodyFragment() {
        String html = "<div><span>Fragment</span></div>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com");

        assertNotNull(doc.body());
        assertEquals(1, doc.body().children().size());
        assertEquals("div", doc.body().child(0).tagName());
        assertEquals("Fragment", doc.body().child(0).text());
    }

    @Test
    public void testParseBodyFragmentRelaxed() {
        String html = "<li>Item 1</li><li>Item 2</li>";
        Document doc = Parser.parseBodyFragmentRelaxed(html, "http://example.com");

        assertNotNull(doc.body());
        Elements lis = doc.body().getElementsByTag("li");
        assertEquals(2, lis.size());
        assertEquals("Item 1", lis.get(0).text());
        assertEquals("Item 2", lis.get(1).text());
    }

    @Test
    public void testCommentsStandardAndMalformed() {
        String html = "<div><!-- Standard Comment -->Text<!-- Malformed Comment ->After</div>";
        Document doc = Parser.parse(html, "http://example.com");

        Element div = doc.body().child(0);
        List<Node> nodes = div.childNodes();

        boolean foundStdComment = false;
        boolean foundMalformedComment = false;

        for (Node node : nodes) {
            if (node instanceof Comment) {
                Comment comment = (Comment) node;
                if (" Standard Comment ".equals(comment.getData())) {
                    foundStdComment = true;
                } else if (" Malformed Comment ".equals(comment.getData())) {
                    foundMalformedComment = true;
                }
            }
        }

        assertTrue("Standard comment should be parsed", foundStdComment);
        assertTrue("Malformed comment ending with -> should be parsed without trailing dash", foundMalformedComment);
    }

    @Test
    public void testXmlDeclarationAndDocType() {
        String html = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE html><html><body>Test</body></html>";
        Document doc = Parser.parse(html, "http://example.com");

        boolean foundXmlDecl = false;
        boolean foundDocType = false;

        for (Node node : doc.childNodes()) {
            if (node instanceof XmlDeclaration) {
                XmlDeclaration decl = (XmlDeclaration) node;
                if (decl.getWholeDeclaration().startsWith("xml")) {
                    foundXmlDecl = true;
                    assertFalse(decl.toString().startsWith("<!"));
                } else if (decl.getWholeDeclaration().startsWith("DOCTYPE")) {
                    foundDocType = true;
                }
            }
        }

        assertTrue("XML declaration should be found", foundXmlDecl);
        assertTrue("DOCTYPE should be found as XmlDeclaration", foundDocType);
    }

    @Test
    public void testCdataSection() {
        String html = "<div><![CDATA[Some <raw> & <b>unescaped</b> content]]></div>";
        Document doc = Parser.parse(html, "http://example.com");

        Element div = doc.body().child(0);
        assertEquals(1, div.childNodeSize());
        Node childNode = div.childNode(0);
        assertTrue(childNode instanceof TextNode);
        assertEquals("Some <raw> & <b>unescaped</b> content", ((TextNode) childNode).getWholeText());
    }

    @Test
    public void testDataTagsScriptAndStyle() {
        String html = "<script type=\"text/javascript\">var x = \"<b>test</b>\"; if (x < 5) {}</script>" +
                      "<style type=\"text/css\">body > p { color: red; }</style>";
        Document doc = Parser.parse(html, "http://example.com");

        Element script = doc.head().getElementsByTag("script").first();
        assertNotNull(script);
        assertEquals(1, script.childNodeSize());
        assertTrue(script.childNode(0) instanceof DataNode);
        assertEquals("var x = \"<b>test</b>\"; if (x < 5) {}", ((DataNode) script.childNode(0)).getWholeData());

        Element style = doc.head().getElementsByTag("style").first();
        assertNotNull(style);
        assertEquals(1, style.childNodeSize());
        assertTrue(style.childNode(0) instanceof DataNode);
        assertEquals("body > p { color: red; }", ((DataNode) style.childNode(0)).getWholeData());
    }

    @Test
    public void testDataTagsTextareaAndTitle() {
        String html = "<title>Page &amp; <b>Title</b></title><textarea>Text &amp; <span>Area</span></textarea>";
        Document doc = Parser.parse(html, "http://example.com");

        Element title = doc.head().getElementsByTag("title").first();
        assertNotNull(title);
        assertEquals(1, title.childNodeSize());
        assertTrue(title.childNode(0) instanceof TextNode);
        assertEquals("Page & <b>Title</b>", ((TextNode) title.childNode(0)).getWholeText());

        Element textarea = doc.body().getElementsByTag("textarea").first();
        assertNotNull(textarea);
        assertEquals(1, textarea.childNodeSize());
        assertTrue(textarea.childNode(0) instanceof TextNode);
        assertEquals("Text & <span>Area</span>", ((TextNode) textarea.childNode(0)).getWholeText());
    }

    @Test
    public void testBaseTagResolution() {
        String html = "<html><head><base href=\"http://jsoup.org/path/\"><a href=\"sub/link.html\">Link</a></head><body></body></html>";
        Document doc = Parser.parse(html, "http://example.com");

        Element a = doc.head().getElementsByTag("a").first();
        assertNotNull(a);
        assertEquals("http://jsoup.org/path/sub/link.html", a.absUrl("href"));
        assertEquals("http://jsoup.org/path/", doc.baseUri());
    }

    @Test
    public void testBaseTagWithoutHref() {
        String html = "<html><head><base target=\"_blank\"><a href=\"sub/link.html\">Link</a></head><body></body></html>";
        Document doc = Parser.parse(html, "http://example.com/");

        Element a = doc.head().getElementsByTag("a").first();
        assertNotNull(a);
        assertEquals("http://example.com/sub/link.html", a.absUrl("href"));
        assertEquals("http://example.com/", doc.baseUri());
    }

    @Test
    public void testAttributesVariations() {
        String html = "<div id='single' class=\"double\" data-val=unquoted disabled  empty=\"\"  >Content</div>";
        Document doc = Parser.parse(html, "http://example.com");

        Element div = doc.body().child(0);
        assertEquals("single", div.attr("id"));
        assertEquals("double", div.attr("class"));
        assertEquals("unquoted", div.attr("data-val"));
        assertTrue(div.hasAttr("disabled"));
        assertEquals("", div.attr("disabled"));
        assertEquals("", div.attr("empty"));
    }

    @Test
    public void testInvalidAttributeCharacters() {
        String html = "<div =invalid \"weird\" normal=\"ok\">Content</div>";
        Document doc = Parser.parse(html, "http://example.com");

        Element div = doc.body().child(0);
        assertEquals("ok", div.attr("normal"));
    }

    @Test
    public void testSelfClosingTags() {
        String html = "<div><img src=\"foo.jpg\"/><custom-tag id=\"custom\"/><br></div>";
        Document doc = Parser.parse(html, "http://example.com");

        Element div = doc.body().child(0);
        assertEquals(3, div.children().size());

        Element img = div.child(0);
        assertEquals("img", img.tagName());
        assertEquals("foo.jpg", img.attr("src"));

        Element custom = div.child(1);
        assertEquals("custom-tag", custom.tagName());
        assertEquals("custom", custom.attr("id"));

        Element br = div.child(2);
        assertEquals("br", br.tagName());
    }

    @Test
    public void testTextNodeWithLessThanCharacter() {
        String html = "<p>5 < 10 and 10 > 5</p>";
        Document doc = Parser.parse(html, "http://example.com");

        Element p = doc.body().child(0);
        assertEquals("5 < 10 and 10 > 5", p.text());
    }

    @Test
    public void testImplicitParentCreationForBody() {
        String html = "<body><p>Direct Body</p></body>";
        Document doc = Parser.parse(html, "http://example.com");

        assertNotNull(doc.child(0));
        assertEquals("html", doc.child(0).tagName());
        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertEquals("Direct Body", doc.body().text());
    }

    @Test
    public void testImplicitParentCreationForTableTags() {
        String html = "<td>Cell 1</td><td>Cell 2</td>";
        Document doc = Parser.parse(html, "http://example.com");

        Elements tables = doc.body().getElementsByTag("table");
        assertEquals(1, tables.size());

        Elements cells = doc.body().getElementsByTag("td");
        assertEquals(2, cells.size());
        assertEquals("Cell 1", cells.get(0).text());
        assertEquals("Cell 2", cells.get(1).text());
    }

    @Test
    public void testImplicitParentCreationForListItems() {
        String html = "<li>Item 1</li><li>Item 2</li>";
        Document doc = Parser.parse(html, "http://example.com");

        Elements uls = doc.body().getElementsByTag("ul");
        assertEquals(1, uls.size());
        Elements lis = doc.body().getElementsByTag("li");
        assertEquals(2, lis.size());
    }

    @Test
    public void testPopStackToCloseUnclosedTags() {
        String html = "<div><p><span>First<p>Second</div>";
        Document doc = Parser.parse(html, "http://example.com");

        Element div = doc.body().child(0);
        assertEquals(2, div.children().size());

        Element p1 = div.child(0);
        assertEquals("p", p1.tagName());
        assertEquals("First", p1.text());

        Element p2 = div.child(1);
        assertEquals("p", p2.tagName());
        assertEquals("Second", p2.text());
    }

    @Test
    public void testEndTagNotOnStackAndEmptyEndTag() {
        String html = "<div>Hello</div></span></>";
        Document doc = Parser.parse(html, "http://example.com");

        Element div = doc.body().getElementsByTag("div").first();
        assertNotNull(div);
        assertEquals("Hello", div.text());
    }

    @Test
    public void testClosingBodyAndHtmlTagInsideBody() {
        String html = "<div>Hello</body>world</html>after</div>";
        Document doc = Parser.parse(html, "http://example.com");

        Element div = doc.body().getElementsByTag("div").first();
        assertNotNull(div);
        assertEquals("Helloworldafter", div.text().replaceAll("\\s+", ""));
    }

    @Test
    public void testNestedSameTags() {
        String html = "<div><div><span>Inner</span></div></div>";
        Document doc = Parser.parse(html, "http://example.com");

        Elements divs = doc.body().getElementsByTag("div");
        assertEquals(2, divs.size());
        Element outer = divs.get(0);
        Element inner = divs.get(1);
        assertTrue(outer.children().contains(inner));
        assertEquals("Inner", inner.text());
    }

    @Test
    public void testEmptyAndWhitespaceInput() {
        Document doc1 = Parser.parse("", "http://example.com");
        assertNotNull(doc1);
        assertNotNull(doc1.body());
        assertEquals(0, doc1.body().childNodeSize());

        Document doc2 = Parser.parse("   \n\t  ", "http://example.com");
        assertNotNull(doc2);
        assertNotNull(doc2.body());
    }
}
