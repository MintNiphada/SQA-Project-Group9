package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class ParserTest {

    @Test
    public void testHtmlParserInitialization() {
        Parser parser = Parser.htmlParser();
        Assert.assertNotNull(parser);
        Assert.assertTrue(parser.getTreeBuilder() instanceof HtmlTreeBuilder);
        Assert.assertFalse(parser.isTrackErrors());
        Assert.assertNull(parser.getErrors());
        Assert.assertNotNull(parser.settings());
    }

    @Test
    public void testXmlParserInitialization() {
        Parser parser = Parser.xmlParser();
        Assert.assertNotNull(parser);
        Assert.assertTrue(parser.getTreeBuilder() instanceof XmlTreeBuilder);
        Assert.assertFalse(parser.isTrackErrors());
        Assert.assertNull(parser.getErrors());
        Assert.assertNotNull(parser.settings());
    }

    @Test
    public void testSetTreeBuilder() {
        Parser parser = Parser.htmlParser();
        XmlTreeBuilder xmlTreeBuilder = new XmlTreeBuilder();
        Parser chainedParser = parser.setTreeBuilder(xmlTreeBuilder);
        Assert.assertSame(parser, chainedParser);
        Assert.assertSame(xmlTreeBuilder, parser.getTreeBuilder());
    }

    @Test
    public void testTrackErrorsToggle() {
        Parser parser = Parser.htmlParser();
        Assert.assertFalse(parser.isTrackErrors());
        
        Parser chained = parser.setTrackErrors(10);
        Assert.assertSame(parser, chained);
        Assert.assertTrue(parser.isTrackErrors());

        parser.setTrackErrors(0);
        Assert.assertFalse(parser.isTrackErrors());

        parser.setTrackErrors(-1);
        Assert.assertFalse(parser.isTrackErrors());
    }

    @Test
    public void testSettingsGetSet() {
        Parser parser = Parser.htmlParser();
        ParseSettings customSettings = new ParseSettings(true, true);
        Parser chained = parser.settings(customSettings);
        Assert.assertSame(parser, chained);
        Assert.assertSame(customSettings, parser.settings());
    }

    @Test
    public void testParseInputWithoutTracking() {
        Parser parser = Parser.htmlParser();
        Document doc = parser.parseInput("<p>Test</p>", "http://example.com");
        Assert.assertNotNull(doc);
        Assert.assertEquals("http://example.com", doc.baseUri());
        Assert.assertEquals("Test", doc.select("p").text());
        Assert.assertNotNull(parser.getErrors());
        Assert.assertEquals(0, parser.getErrors().size());
    }

    @Test
    public void testParseInputWithTracking() {
        Parser parser = Parser.htmlParser();
        parser.setTrackErrors(10);
        Document doc = parser.parseInput("<html><p>Test", "http://example.com");
        Assert.assertNotNull(doc);
        Assert.assertNotNull(parser.getErrors());
        Assert.assertTrue(parser.getErrors().size() > 0);
    }

    @Test
    public void testStaticParse() {
        Document doc = Parser.parse("<title>Jsoup Test</title><p>Hello World</p>", "http://example.com/");
        Assert.assertNotNull(doc);
        Assert.assertEquals("Jsoup Test", doc.title());
        Assert.assertEquals("Hello World", doc.select("p").text());
        Assert.assertEquals("http://example.com/", doc.baseUri());
    }

    @Test
    public void testStaticParseFragment() {
        Document doc = Document.createShell("http://example.com/");
        Element body = doc.body();
        List<Node> nodes = Parser.parseFragment("<div><p>Paragraph 1</p><p>Paragraph 2</p></div>", body, "http://example.com/");
        Assert.assertNotNull(nodes);
        Assert.assertEquals(1, nodes.size());
        Assert.assertTrue(nodes.get(0) instanceof Element);
        Element div = (Element) nodes.get(0);
        Assert.assertEquals("div", div.tagName());
        Assert.assertEquals(2, div.children().size());
    }

    @Test
    public void testStaticParseXmlFragment() {
        List<Node> nodes = Parser.parseXmlFragment("<item id=\"1\">Value1</item><item id=\"2\">Value2</item>", "http://example.com/");
        Assert.assertNotNull(nodes);
        Assert.assertEquals(2, nodes.size());
        Assert.assertTrue(nodes.get(0) instanceof Element);
        Assert.assertTrue(nodes.get(1) instanceof Element);
        Element item1 = (Element) nodes.get(0);
        Element item2 = (Element) nodes.get(1);
        Assert.assertEquals("item", item1.tagName());
        Assert.assertEquals("1", item1.attr("id"));
        Assert.assertEquals("item", item2.tagName());
        Assert.assertEquals("2", item2.attr("id"));
    }

    @Test
    public void testParseBodyFragment() {
        Document doc = Parser.parseBodyFragment("<p>One</p><p>Two</p>", "http://example.com/");
        Assert.assertNotNull(doc);
        Element body = doc.body();
        Assert.assertNotNull(body);
        Assert.assertEquals(2, body.children().size());
        Assert.assertEquals("One", body.child(0).text());
        Assert.assertEquals("Two", body.child(1).text());
    }

    @Test
    public void testParseBodyFragmentSingleChild() {
        Document doc = Parser.parseBodyFragment("<p>Single</p>", "http://example.com/");
        Assert.assertNotNull(doc);
        Element body = doc.body();
        Assert.assertEquals(1, body.children().size());
        Assert.assertEquals("Single", body.child(0).text());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testParseBodyFragmentRelaxed() {
        Document doc = Parser.parseBodyFragmentRelaxed("<p>Relaxed</p>", "http://example.com/");
        Assert.assertNotNull(doc);
        Assert.assertEquals("Relaxed", doc.select("p").text());
    }

    @Test
    public void testUnescapeEntities() {
        String escaped = "&lt;div&gt;&amp;copy;&quot;&apos;&lt;/div&gt;";
        String unescaped = Parser.unescapeEntities(escaped, false);
        Assert.assertEquals("<div>&copy;\"'</div>", unescaped);

        String attributeUnescaped = Parser.unescapeEntities("&quot;test&amp;&quot;", true);
        Assert.assertEquals("\"test&\"", attributeUnescaped);
    }
}
