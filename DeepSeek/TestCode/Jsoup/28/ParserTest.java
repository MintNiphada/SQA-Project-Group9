package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

public class ParserTest {

    @Test
    public void testDefaultConstructor() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        assertNotNull(parser.getTreeBuilder());
        assertFalse(parser.isTrackErrors());
        assertNull(parser.getErrors());
    }

    @Test
    public void testSetTreeBuilder() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        XmlTreeBuilder xmlBuilder = new XmlTreeBuilder();
        Parser returned = parser.setTreeBuilder(xmlBuilder);
        assertSame(parser, returned);
        assertSame(xmlBuilder, parser.getTreeBuilder());
    }

    @Test
    public void testIsTrackErrorsFalseByDefault() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        assertFalse(parser.isTrackErrors());
        parser.setTrackErrors(0);
        assertFalse(parser.isTrackErrors());
    }

    @Test
    public void testIsTrackErrorsTrueAfterSettingPositive() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        parser.setTrackErrors(5);
        assertTrue(parser.isTrackErrors());
    }

    @Test
    public void testIsTrackErrorsEdgeOne() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        parser.setTrackErrors(1);
        assertTrue(parser.isTrackErrors());
    }

    @Test
    public void testSetTrackErrorsReturnsSelf() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        assertSame(parser, parser.setTrackErrors(10));
    }

    @Test
    public void testGetErrorsBeforeParse() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        assertNull(parser.getErrors());
    }

    @Test
    public void testParseInputWithoutErrorTracking() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        parser.setTrackErrors(0);
        Document doc = parser.parseInput("<html></html>", "http://example.com");
        assertNotNull(doc);
        ParseErrorList errors = parser.getErrors();
        assertNotNull(errors);
        assertEquals(0, errors.size());
    }

    @Test
    public void testParseInputWithErrorTracking() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        parser.setTrackErrors(5);
        Document doc = parser.parseInput("<html><p>test</html>", "http://example.com");
        assertNotNull(doc);
        ParseErrorList errors = parser.getErrors();
        assertNotNull(errors);
    }

    @Test
    public void testParseInputEmptyString() {
        Parser parser = new Parser(new HtmlTreeBuilder());
        Document doc = parser.parseInput("", "http://example.com");
        assertNotNull(doc);
    }

    @Test
    public void testStaticParse() {
        Document doc = Parser.parse("<html><body>Hello</body></html>", "http://example.com");
        assertNotNull(doc);
    }

    @Test
    public void testStaticParseNullHtml() {
        try {
            Parser.parse(null, "http://example.com");
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testStaticParseFragment() {
        List<Node> nodes = Parser.parseFragment("<div>Hi</div>", null, "http://example.com");
        assertNotNull(nodes);
        assertTrue(nodes.size() > 0);
    }

    @Test
    public void testStaticParseFragmentWithContext() {
        Document doc = Parser.parse("<html><body><div id='ctx'></div></body></html>", "http://example.com");
        Element ctx = doc.getElementById("ctx");
        List<Node> nodes = Parser.parseFragment("<span>Inside</span>", ctx, "http://example.com");
        assertNotNull(nodes);
    }

    @Test
    public void testParseBodyFragment() {
        Document doc = Parser.parseBodyFragment("<p>Body text</p>", "http://example.com");
        assertNotNull(doc);
        assertNotNull(doc.body());
        assertTrue(doc.body().html().contains("<p>Body text</p>"));
    }

    @Test
    public void testParseBodyFragmentRelaxed() {
        Document doc = Parser.parseBodyFragmentRelaxed("<p>Relaxed</p>", "http://example.com");
        assertNotNull(doc);
    }

    @Test
    public void testHtmlParserFactory() {
        Parser parser = Parser.htmlParser();
        assertNotNull(parser);
        assertTrue(parser.getTreeBuilder() instanceof HtmlTreeBuilder);
    }

    @Test
    public void testXmlParserFactory() {
        Parser parser = Parser.xmlParser();
        assertNotNull(parser);
        assertTrue(parser.getTreeBuilder() instanceof XmlTreeBuilder);
    }

    @Test
    public void testGetErrorsAfterFailedParse() {
        Parser parser = Parser.htmlParser();
        parser.setTrackErrors(10);
        parser.parseInput("<</div>", "http://example.com");
        List<ParseError> errors = parser.getErrors();
        assertNotNull(errors);
    }

    @Test
    public void testParseInputReusesTreeBuilder() {
        Parser parser = Parser.htmlParser();
        TreeBuilder builder = parser.getTreeBuilder();
        parser.parseInput("<a>1</a>", "");
        assertSame(builder, parser.getTreeBuilder());
    }

    @Test
    public void testSetTrackErrorsNegativeIgnoresTracking() {
        Parser parser = Parser.htmlParser();
        parser.setTrackErrors(-1);
        assertFalse(parser.isTrackErrors());
    }
}
