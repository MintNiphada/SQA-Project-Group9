package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ParserTest {

    @Test
    public void testHtmlParserCreation() {
        Parser parser = Parser.htmlParser();
        assertNotNull(parser);
        assertTrue(parser.getTreeBuilder() instanceof HtmlTreeBuilder);
        assertFalse(parser.isTrackErrors());
    }

    @Test
    public void testXmlParserCreation() {
        Parser parser = Parser.xmlParser();
        assertNotNull(parser);
        assertTrue(parser.getTreeBuilder() instanceof XmlTreeBuilder);
        assertFalse(parser.isTrackErrors());
    }

    @Test
    public void testGetAndSetTreeBuilder() {
        TreeBuilder htmlBuilder = new HtmlTreeBuilder();
        Parser parser = new Parser(htmlBuilder);
        assertSame(htmlBuilder, parser.getTreeBuilder());

        TreeBuilder xmlBuilder = new XmlTreeBuilder();
        Parser returnedParser = parser.setTreeBuilder(xmlBuilder);
        assertSame(parser, returnedParser);
        assertSame(xmlBuilder, parser.getTreeBuilder());
    }

    @Test
    public void testTrackErrorsFlagAndSettings() {
        Parser parser = Parser.htmlParser();
        assertFalse(parser.isTrackErrors());

        Parser returnedParser = parser.setTrackErrors(10);
        assertSame(parser, returnedParser);
        assertTrue(parser.isTrackErrors());

        parser.setTrackErrors(0);
        assertFalse(parser.isTrackErrors());

        parser.setTrackErrors(-1);
        assertFalse(parser.isTrackErrors());
    }

    @Test
    public void testParseInputWithoutErrorTracking() {
        Parser parser = Parser.htmlParser();
        parser.setTrackErrors(0);
        Document doc = parser.parseInput("<div><p>Hello</div>", "http://example.com");

        assertNotNull(doc);
        assertEquals("http://example.com", doc.baseUri());
        assertEquals("Hello", doc.select("p").text());

        List<ParseError> errors = parser.getErrors();
        assertNotNull(errors);
        assertEquals(0, errors.size());
    }

    @Test
    public void testParseInputWithErrorTracking() {
        Parser parser = Parser.htmlParser();
        parser.setTrackErrors(10);
        assertTrue(parser.isTrackErrors());

        // Malformed HTML designed to trigger parse errors
        Document doc = parser.parseInput("<html><head></head><body><div><p><span>foo</div></span></body></html>", "http://example.com");
        assertNotNull(doc);

        List<ParseError> errors = parser.getErrors();
        assertNotNull(errors);
        assertTrue(errors.size() > 0);
        assertTrue(errors.size() <= 10);
    }

    @Test
    public void testParseInputXmlTreeBuilder() {
        Parser parser = Parser.xmlParser();
        Document doc = parser.parseInput("<root><child attr=\"val\">Text</child></root>", "http://example.com");

        assertNotNull(doc);
        assertEquals("Text", doc.select("child").text());
        assertEquals("val", doc.select("child").attr("attr"));
    }

    @Test
    public void testStaticParse() {
        Document doc = Parser.parse("<title>Test</title><p>Paragraph</p>", "http://example.com/test");

        assertNotNull(doc);
        assertEquals("http://example.com/test", doc.baseUri());
        assertEquals("Test", doc.title());
        assertEquals("Paragraph", doc.select("p").text());
    }

    @Test
    public void testStaticParseFragmentWithContext() {
        Element context = new Element(Tag.valueOf("div"), "http://example.com");
        List<Node> nodes = Parser.parseFragment("<p>Fragment</p><span>Span</span>", context, "http://example.com");

        assertNotNull(nodes);
        assertEquals(2, nodes.size());
        assertTrue(nodes.get(0) instanceof Element);
        assertEquals("p", ((Element) nodes.get(0)).tagName());
        assertEquals("Fragment", ((Element) nodes.get(0)).text());
        assertTrue(nodes.get(1) instanceof Element);
        assertEquals("span", ((Element) nodes.get(1)).tagName());
        assertEquals("Span", ((Element) nodes.get(1)).text());
    }

    @Test
    public void testStaticParseFragmentWithoutContext() {
        List<Node> nodes = Parser.parseFragment("<p>Fragment</p>", null, "http://example.com");

        assertNotNull(nodes);
        assertTrue(nodes.size() > 0);
        boolean foundP = false;
        for (Node node : nodes) {
            if (node instanceof Element && "p".equals(((Element) node).tagName())) {
                foundP = true;
                assertEquals("Fragment", ((Element) node).text());
            }
        }
        assertTrue(foundP);
    }

    @Test
    public void testStaticParseBodyFragment() {
        Document doc = Parser.parseBodyFragment("<p>Paragraph 1</p><p>Paragraph 2</p>", "http://example.com");

        assertNotNull(doc);
        assertEquals("http://example.com", doc.baseUri());
        assertNotNull(doc.body());
        assertEquals(2, doc.body().children().size());
        assertEquals("Paragraph 1", doc.body().child(0).text());
        assertEquals("Paragraph 2", doc.body().child(1).text());
    }

    @Test
    public void testStaticParseBodyFragmentWithTextAndTags() {
        Document doc = Parser.parseBodyFragment("Just text <b>bold</b>", "http://example.com");

        assertNotNull(doc);
        assertEquals("Just text bold", doc.body().text());
        assertTrue(doc.body().childNode(0) instanceof TextNode);
        assertEquals("Just text ", ((TextNode) doc.body().childNode(0)).getWholeText());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testStaticParseBodyFragmentRelaxed() {
        Document doc = Parser.parseBodyFragmentRelaxed("<div>Relaxed parse</div>", "http://example.com");

        assertNotNull(doc);
        assertEquals("http://example.com", doc.baseUri());
        assertEquals("Relaxed parse", doc.select("div").text());
    }
}
