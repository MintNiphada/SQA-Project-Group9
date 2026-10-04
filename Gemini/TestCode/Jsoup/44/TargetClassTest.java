package org.jsoup.parser;

import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TreeBuilderTest {

    private static class TestTreeBuilder extends TreeBuilder {
        final List<Token> processedTokens = new ArrayList<Token>();

        @Override
        protected boolean process(Token token) {
            processedTokens.add(token);
            if (token.isStartTag()) {
                Token.StartTag startTag = token.asStartTag();
                Element el = new Element(Tag.valueOf(startTag.name()), baseUri, startTag.attributes);
                stack.add(el);
                doc.appendChild(el);
            } else if (token.isEndTag()) {
                if (!stack.isEmpty()) {
                    stack.remove(stack.size() - 1);
                }
            }
            return true;
        }
    }

    private TestTreeBuilder treeBuilder;

    @Before
    public void setUp() {
        treeBuilder = new TestTreeBuilder();
    }

    @Test
    public void testInitialiseParseSuccess() {
        ParseErrorList errors = ParseErrorList.tracking(10);
        treeBuilder.initialiseParse("<div>test</div>", "http://example.com", errors);
        Assert.assertNotNull(treeBuilder.doc);
        Assert.assertEquals("http://example.com", treeBuilder.baseUri);
        Assert.assertNotNull(treeBuilder.reader);
        Assert.assertNotNull(treeBuilder.tokeniser);
        Assert.assertNotNull(treeBuilder.stack);
        Assert.assertTrue(treeBuilder.stack.isEmpty());
        Assert.assertSame(errors, treeBuilder.errors);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInitialiseParseNullInput() {
        treeBuilder.initialiseParse(null, "http://example.com", ParseErrorList.noTracking());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInitialiseParseNullBaseUri() {
        treeBuilder.initialiseParse("<div>", null, ParseErrorList.noTracking());
    }

    @Test
    public void testParseWithBaseUri() {
        Document doc = treeBuilder.parse("<p>Hello</p>", "http://example.com");
        Assert.assertNotNull(doc);
        Assert.assertEquals("http://example.com", doc.baseUri());
        Assert.assertFalse(treeBuilder.processedTokens.isEmpty());
        Token lastToken = treeBuilder.processedTokens.get(treeBuilder.processedTokens.size() - 1);
        Assert.assertEquals(Token.TokenType.EOF, lastToken.type);
    }

    @Test
    public void testParseWithErrors() {
        ParseErrorList errors = ParseErrorList.tracking(5);
        Document doc = treeBuilder.parse("<span>World</span>", "http://example.com/test", errors);
        Assert.assertNotNull(doc);
        Assert.assertEquals("http://example.com/test", doc.baseUri());
        Assert.assertSame(errors, treeBuilder.errors);
    }

    @Test
    public void testProcessStartTagOnlyName() {
        treeBuilder.initialiseParse("", "http://example.com", ParseErrorList.noTracking());
        boolean result = treeBuilder.processStartTag("div");
        Assert.assertTrue(result);
        Assert.assertEquals(1, treeBuilder.processedTokens.size());
        Token token = treeBuilder.processedTokens.get(0);
        Assert.assertTrue(token.isStartTag());
        Assert.assertEquals("div", token.asStartTag().name());
    }

    @Test
    public void testProcessStartTagWithNameAndAttributes() {
        treeBuilder.initialiseParse("", "http://example.com", ParseErrorList.noTracking());
        Attributes attrs = new Attributes();
        attrs.put("id", "main");
        attrs.put("class", "content");
        boolean result = treeBuilder.processStartTag("div", attrs);
        Assert.assertTrue(result);
        Assert.assertEquals(1, treeBuilder.processedTokens.size());
        Token token = treeBuilder.processedTokens.get(0);
        Assert.assertTrue(token.isStartTag());
        Assert.assertEquals("div", token.asStartTag().name());
        Assert.assertEquals("main", token.asStartTag().attributes.get("id"));
        Assert.assertEquals("content", token.asStartTag().attributes.get("class"));
    }

    @Test
    public void testProcessEndTag() {
        treeBuilder.initialiseParse("", "http://example.com", ParseErrorList.noTracking());
        boolean result = treeBuilder.processEndTag("div");
        Assert.assertTrue(result);
        Assert.assertEquals(1, treeBuilder.processedTokens.size());
        Token token = treeBuilder.processedTokens.get(0);
        Assert.assertTrue(token.isEndTag());
        Assert.assertEquals("div", token.asEndTag().name());
    }

    @Test
    public void testCurrentElementWhenEmptyStack() {
        treeBuilder.initialiseParse("", "http://example.com", ParseErrorList.noTracking());
        Assert.assertNull(treeBuilder.currentElement());
    }

    @Test
    public void testCurrentElementWithStack() {
        treeBuilder.initialiseParse("", "http://example.com", ParseErrorList.noTracking());
        Element el1 = new Element(Tag.valueOf("div"), "http://example.com");
        Element el2 = new Element(Tag.valueOf("span"), "http://example.com");

        treeBuilder.stack.add(el1);
        Assert.assertSame(el1, treeBuilder.currentElement());

        treeBuilder.stack.add(el2);
        Assert.assertSame(el2, treeBuilder.currentElement());

        treeBuilder.stack.remove(treeBuilder.stack.size() - 1);
        Assert.assertSame(el1, treeBuilder.currentElement());
    }

    @Test
    public void testRunParserCompletesOnEOF() {
        treeBuilder.initialiseParse("", "http://example.com", ParseErrorList.noTracking());
        treeBuilder.runParser();
        Assert.assertEquals(1, treeBuilder.processedTokens.size());
        Assert.assertEquals(Token.TokenType.EOF, treeBuilder.processedTokens.get(0).type);
    }
}
