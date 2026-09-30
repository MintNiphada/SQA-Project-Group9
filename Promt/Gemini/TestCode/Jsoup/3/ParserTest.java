package org.jsoup.parser;

import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

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

    @Test
    public void testBasicDocumentStructure() {
        Document doc = Parser.parse("<html><head><title>Test Title</title></head><body><p>Hello World</p></body></html>", "http://example.com");
        Assert.assertNotNull(doc);
        Assert.assertEquals("Test Title", doc.title());
        Assert.assertEquals(1, doc.select("p").size());
        Assert.assertEquals("Hello World", doc.select("p").first().text());
    }

    @Test
    public void testParseBodyFragment() {
        Document doc = Parser.parseBodyFragment("<p>Fragment text</p><span>Extra</span>", "http://example.com");
        Assert.assertNotNull(doc);
        Assert.assertNotNull(doc.body());
        Assert.assertEquals(2, doc.body().children().size());
        Assert.assertEquals("Fragment text", doc.body().child(0).text());
        Assert.assertEquals("Extra", doc.body().child(1).text());
    }

    @Test
    public void testCommentsParsing() {
        String html = "<div><!-- Standard comment --></div><div><!-- Comment with single dash -></div>";
        Document doc = Parser.parse(html, "http://example.com");
        List<Node> childNodes1 = doc.select("div").get(0).childNodes();
        Assert.assertEquals(1, childNodes1.size());
        Assert.assertTrue(childNodes1.get(0) instanceof Comment);
        Assert.assertEquals(" Standard comment ", ((Comment) childNodes1.get(0)).getData());

        List<Node> childNodes2 = doc.select("div").get(1).childNodes();
        Assert.assertEquals(1, childNodes2.size());
        Assert.assertTrue(childNodes2.get(0) instanceof Comment);
        Assert.assertEquals(" Comment with single dash ", ((Comment) childNodes2.get(0)).getData());
    }

    @Test
    public void testXmlDeclarationAndDocType() {
        String html = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE html><html><head></head><body>Content</body></html>";
        Document doc = Parser.parse(html, "http://example.com");
        
        boolean foundXmlDecl = false;
        boolean foundDocType = false;
        for (Node node : doc.childNodes()) {
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
        Assert.assertTrue(foundXmlDecl);
        Assert.assertTrue(foundDocType);
    }

    @Test
    public void testCdataParsing() {
        String html = "<p><![CDATA[Some <unescaped> & raw text]]></p>";
        Document doc = Parser.parse(html, "http://example.com");
        Element p = doc.select("p").first();
        Assert.assertNotNull(p);
        Assert.assertEquals(1, p.childNodes().size());
        Assert.assertTrue(p.childNode(0) instanceof TextNode);
        Assert.assertEquals("Some <unescaped> & raw text", ((TextNode) p.childNode(0)).getWholeText());
    }

    @Test
    public void testEndTagHandling() {
        String html = "<div><p>Paragraph 1</p></p><p>Paragraph 2</p></div></>";
        Document doc = Parser.parse(html, "http://example.com");
        Elements ps = doc.select("p");
        Assert.assertEquals(2, ps.size());
        Assert.assertEquals("Paragraph 1", ps.get(0).text());
        Assert.assertEquals("Paragraph 2", ps.get(1).text());
    }

    @Test
    public void testStartTagNotValidWord() {
        String html = "< 5 and <";
        Document doc = Parser.parse(html, "http://example.com");
        Assert.assertTrue(doc.body().text().contains("< 5 and <"));
    }

    @Test
    public void testAttributeParsingVariations() {
        String html = "<div id='single' class=\"double\" name=unquoted disabled data-empty=\"\" key = 'spaced' ></div>";
        Document doc = Parser.parse(html, "http://example.com");
        Element div = doc.select("div").first();
        Assert.assertNotNull(div);
        Assert.assertEquals("single", div.attr("id"));
        Assert.assertEquals("double", div.attr("class"));
        Assert.assertEquals("unquoted", div.attr("name"));
        Assert.assertTrue(div.hasAttr("disabled"));
        Assert.assertEquals("", div.attr("data-empty"));
        Assert.assertEquals("spaced", div.attr("key"));
    }

    @Test
    public void testAttributeWithCorruptedKey() {
        String html = "<div =invalid class=\"valid\">Test</div>";
        Document doc = Parser.parse(html, "http://example.com");
        Element div = doc.select("div").first();
        Assert.assertNotNull(div);
        Assert.assertEquals("valid", div.attr("class"));
    }

    @Test
    public void testSelfClosingAndEmptyTags() {
        String html = "<div><img src='test.png'/><br><hr/><span>Hello</span></div>";
        Document doc = Parser.parse(html, "http://example.com");
        Element div = doc.select("div").first();
        Assert.assertNotNull(div);
        Assert.assertEquals(4, div.children().size());
        Assert.assertEquals("img", div.child(0).tagName());
        Assert.assertEquals("br", div.child(1).tagName());
        Assert.assertEquals("hr", div.child(2).tagName());
        Assert.assertEquals("span", div.child(3).tagName());
    }

    @Test
    public void testDataTagsHandling() {
        String html = "<title>Page &amp; Title</title><textarea>Text &amp; <b>area</b></textarea><script>var x = \"<b>test</b>\";</script><style>body { color: red; }</style>";
        Document doc = Parser.parse(html, "http://example.com");
        
        Element title = doc.select("title").first();
        Assert.assertNotNull(title);
        Assert.assertEquals("Page & Title", title.text());

        Element textarea = doc.select("textarea").first();
        Assert.assertNotNull(textarea);
        Assert.assertEquals("Text & <b>area</b>", textarea.text());

        Element script = doc.select("script").first();
        Assert.assertNotNull(script);
        Assert.assertEquals(1, script.childNodes().size());
        Assert.assertTrue(script.childNode(0) instanceof DataNode);
        Assert.assertEquals("var x = \"<b>test</b>\";", ((DataNode) script.childNode(0)).getWholeData());

        Element style = doc.select("style").first();
        Assert.assertNotNull(style);
        Assert.assertEquals(1, style.childNodes().size());
        Assert.assertTrue(style.childNode(0) instanceof DataNode);
        Assert.assertEquals("body { color: red; }", ((DataNode) style.childNode(0)).getWholeData());
    }

    @Test
    public void testBaseTagUrlUpdate() {
        String html = "<html><head><base href=\"http://example.com/dir/\"><base target=\"_blank\"></head><body><a href=\"sub.html\">Link</a></body></html>";
        Document doc = Parser.parse(html, "http://example.com");
        
        Assert.assertEquals("http://example.com/dir/", doc.baseUri());
        Element a = doc.select("a").first();
        Assert.assertNotNull(a);
        Assert.assertEquals("http://example.com/dir/sub.html", a.absUrl("href"));
    }

    @Test
    public void testBaseTagWithEmptyHrefIgnored() {
        String html = "<html><head><base target=\"_blank\"></head><body><a href=\"sub.html\">Link</a></body></html>";
        Document doc = Parser.parse(html, "http://example.com/base/");
        
        Assert.assertEquals("http://example.com/base/", doc.baseUri());
        Element a = doc.select("a").first();
        Assert.assertNotNull(a);
        Assert.assertEquals("http://example.com/base/sub.html", a.absUrl("href"));
    }

    @Test
    public void testImplicitParentCreationTable() {
        String html = "<td>Lone Cell</td>";
        Document doc = Parser.parse(html, "http://example.com");
        
        Element td = doc.select("td").first();
        Assert.assertNotNull(td);
        Assert.assertEquals("Lone Cell", td.text());
        Assert.assertEquals("tr", td.parent().tagName());
        Assert.assertEquals("tbody", td.parent().parent().tagName());
        Assert.assertEquals("table", td.parent().parent().parent().tagName());
    }

    @Test
    public void testImplicitBodyTagCreation() {
        String html = "<body>Hello Body</body>";
        Document doc = Parser.parse(html, "http://example.com");
        
        Assert.assertNotNull(doc.body());
        Assert.assertNotNull(doc.head());
        Assert.assertEquals("Hello Body", doc.body().text());
    }

    @Test
    public void testNestedElementsAndStackPopping() {
        String html = "<div><p><span><b>Text</b></span></p></div>";
        Document doc = Parser.parse(html, "http://example.com");
        
        Element b = doc.select("b").first();
        Assert.assertNotNull(b);
        Assert.assertEquals("Text", b.text());
        Assert.assertEquals("span", b.parent().tagName());
        Assert.assertEquals("p", b.parent().parent().tagName());
        Assert.assertEquals("div", b.parent().parent().parent().tagName());
    }

    @Test
    public void testClosingPastBodyOrHtmlIsPrevented() {
        String html = "<html><head></head><body><div><p>Hello</div></p></body></html>";
        Document doc = Parser.parse(html, "http://example.com");
        
        Element p = doc.select("p").first();
        Assert.assertNotNull(p);
        Assert.assertEquals("Hello", p.text());
    }

    @Test
    public void testUnclosedTagsAutoClosedAtEnd() {
        String html = "<div><p>Unclosed paragraph<div>New block</div>";
        Document doc = Parser.parse(html, "http://example.com");
        
        Element div = doc.select("div").first();
        Assert.assertNotNull(div);
        Assert.assertEquals(2, div.children().size());
        Assert.assertEquals("p", div.child(0).tagName());
        Assert.assertEquals("div", div.child(1).tagName());
    }

    @Test
    public void testRelaxedTagNestingAndFormatting() {
        String html = "<ul><li>Item 1<li>Item 2<li>Item 3</ul>";
        Document doc = Parser.parse(html, "http://example.com");
        
        Elements lis = doc.select("li");
        Assert.assertEquals(3, lis.size());
        Assert.assertEquals("Item 1", lis.get(0).text());
        Assert.assertEquals("Item 2", lis.get(1).text());
        Assert.assertEquals("Item 3", lis.get(2).text());
    }

    @Test
    public void testSpecialXmlAndCommentCharacters() {
        String html = "<!----><!><?>";
        Document doc = Parser.parse(html, "http://example.com");
        Assert.assertNotNull(doc);
    }
}
