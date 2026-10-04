package org.jsoup.parser;

import org.jsoup.nodes.*;
import org.jsoup.helper.Validate;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

public class XmlTreeBuilderTest {

    private XmlTreeBuilder treeBuilder;
    private Document doc;
    private ParseErrorList errors;

    @Before
    public void setUp() {
        treeBuilder = new XmlTreeBuilder();
        errors = new ParseErrorList(0, 0);
        treeBuilder.initialiseParse("", "http://example.com", errors);
        doc = treeBuilder.doc;
    }

    @Test
    public void testInitialiseParseAddsDocToStack() {
        assertEquals(1, treeBuilder.stack.size());
        assertSame(doc, treeBuilder.stack.peek());
    }

    @Test
    public void testProcessStartTag() {
        Token.StartTag startTag = new Token.StartTag("root");
        treeBuilder.process(startTag);

        Element root = doc.child(0);
        assertNotNull(root);
        assertEquals("root", root.tagName());
        assertEquals(2, treeBuilder.stack.size());
        assertSame(root, treeBuilder.stack.peek());
    }

    @Test
    public void testProcessStartTagSelfClosingKnownTag() {
        Token.StartTag startTag = new Token.StartTag("br");
        startTag.setSelfClosing();
        treeBuilder.process(startTag);

        Element br = doc.child(0);
        assertNotNull(br);
        assertEquals("br", br.tagName());
        assertFalse(Tag.valueOf("br").isSelfClosing());
        assertEquals(1, treeBuilder.stack.size());
    }

    @Test
    public void testProcessStartTagSelfClosingUnknownTag() {
        Token.StartTag startTag = new Token.StartTag("custom");
        startTag.setSelfClosing();
        treeBuilder.process(startTag);

        Element custom = doc.child(0);
        assertNotNull(custom);
        assertEquals("custom", custom.tagName());
        assertTrue(Tag.valueOf("custom").isSelfClosing());
        assertEquals(1, treeBuilder.stack.size());
    }

    @Test
    public void testProcessEndTagMatching() {
        Token.StartTag startTag = new Token.StartTag("root");
        treeBuilder.process(startTag);

        Token.StartTag childTag = new Token.StartTag("child");
        treeBuilder.process(childTag);

        Token.EndTag endTag = new Token.EndTag("child");
        treeBuilder.process(endTag);

        assertEquals(2, treeBuilder.stack.size());
        assertEquals("root", treeBuilder.stack.peek().tagName());
    }

    @Test
    public void testProcessEndTagNonMatching() {
        Token.StartTag startTag = new Token.StartTag("root");
        treeBuilder.process(startTag);

        Token.EndTag endTag = new Token.EndTag("nonexistent");
        treeBuilder.process(endTag);

        assertEquals(2, treeBuilder.stack.size());
        assertEquals("root", treeBuilder.stack.peek().tagName());
    }

    @Test
    public void testProcessEndTagNoStack() {
        Token.EndTag endTag = new Token.EndTag("root");
        treeBuilder.process(endTag);

        assertEquals(1, treeBuilder.stack.size());
    }

    @Test
    public void testProcessEndTagNestedMatching() {
        Token.StartTag root = new Token.StartTag("root");
        treeBuilder.process(root);
        Token.StartTag child1 = new Token.StartTag("child1");
        treeBuilder.process(child1);
        Token.StartTag child2 = new Token.StartTag("child2");
        treeBuilder.process(child2);

        Token.EndTag endRoot = new Token.EndTag("root");
        treeBuilder.process(endRoot);

        assertEquals(1, treeBuilder.stack.size());
        assertSame(doc, treeBuilder.stack.peek());
    }

    @Test
    public void testProcessComment() {
        Token.Comment commentToken = new Token.Comment("This is a comment");
        treeBuilder.process(commentToken);

        Node node = doc.childNode(0);
        assertTrue(node instanceof Comment);
        Comment comment = (Comment) node;
        assertEquals("This is a comment", comment.getData());
    }

    @Test
    public void testProcessCharacter() {
        Token.Character characterToken = new Token.Character("text");
        treeBuilder.process(characterToken);

        Node node = doc.childNode(0);
        assertTrue(node instanceof TextNode);
        TextNode textNode = (TextNode) node;
        assertEquals("text", textNode.getWholeText());
    }

    @Test
    public void testProcessDoctype() {
        Token.Doctype doctypeToken = new Token.Doctype("html", "-//W3C//DTD XHTML 1.0 Transitional//EN", "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd");
        treeBuilder.process(doctypeToken);

        Node node = doc.childNode(0);
        assertTrue(node instanceof DocumentType);
        DocumentType doctype = (DocumentType) node;
        assertEquals("html", doctype.attr("name"));
        assertEquals("-//W3C//DTD XHTML 1.0 Transitional//EN", doctype.attr("publicId"));
        assertEquals("http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd", doctype.attr("systemId"));
    }

    @Test
    public void testProcessEOF() {
        Token.EOF eof = new Token.EOF();
        assertTrue(treeBuilder.process(eof));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProcessUnexpectedToken() {
        Token token = new Token() {
            @Override
            public TokenType type() {
                return TokenType.Tag;
            }
        };
        treeBuilder.process(token);
    }

    @Test
    public void testInsertStartTagWithAttributes() {
        Attributes attrs = new Attributes();
        attrs.put("id", "test");
        Token.StartTag startTag = new Token.StartTag("div", attrs);
        Element el = treeBuilder.insert(startTag);

        assertNotNull(el);
        assertEquals("div", el.tagName());
        assertTrue(el.hasAttr("id"));
        assertEquals("test", el.attr("id"));
        assertEquals(doc, el.parent());
        assertSame(el, treeBuilder.stack.peek());
        assertFalse(el.tag().isSelfClosing());
    }

    @Test
    public void testInsertStartTagSelfClosingWithKnownTag() {
        Token.StartTag startTag = new Token.StartTag("br");
        startTag.setSelfClosing();
        Element el = treeBuilder.insert(startTag);

        assertNotNull(el);
        assertEquals("br", el.tagName());
        assertFalse(el.tag().isSelfClosing());
        assertEquals(1, treeBuilder.stack.size());
        assertFalse(treeBuilder.stack.contains(el));
    }

    @Test
    public void testInsertStartTagSelfClosingWithUnknownTag() {
        Token.StartTag startTag = new Token.StartTag("custom");
        startTag.setSelfClosing();
        Element el = treeBuilder.insert(startTag);

        assertNotNull(el);
        assertEquals("custom", el.tagName());
        assertTrue(el.tag().isSelfClosing());
        assertEquals(1, treeBuilder.stack.size());
        assertFalse(treeBuilder.stack.contains(el));
    }

    @Test
    public void testInsertComment() {
        Token.Comment commentToken = new Token.Comment("content");
        treeBuilder.insert(commentToken);

        Node node = doc.childNode(0);
        assertTrue(node instanceof Comment);
        assertEquals("content", ((Comment) node).getData());
    }

    @Test
    public void testInsertCharacter() {
        Token.Character characterToken = new Token.Character("data");
        treeBuilder.insert(characterToken);

        Node node = doc.childNode(0);
        assertTrue(node instanceof TextNode);
        assertEquals("data", ((TextNode) node).getWholeText());
    }

    @Test
    public void testInsertDoctype() {
        Token.Doctype doctypeToken = new Token.Doctype("html", "pub", "sys");
        treeBuilder.insert(doctypeToken);

        Node node = doc.childNode(0);
        assertTrue(node instanceof DocumentType);
        DocumentType doctype = (DocumentType) node;
        assertEquals("html", doctype.attr("name"));
        assertEquals("pub", doctype.attr("publicId"));
        assertEquals("sys", doctype.attr("systemId"));
    }

    @Test
    public void testPopStackToCloseWhenFound() {
        treeBuilder.stack.clear();
        Element root = new Element(Tag.valueOf("root"), "");
        Element child1 = new Element(Tag.valueOf("child1"), "");
        Element child2 = new Element(Tag.valueOf("child2"), "");
        treeBuilder.stack.add(doc);
        treeBuilder.stack.add(root);
        treeBuilder.stack.add(child1);
        treeBuilder.stack.add(child2);

        Token.EndTag endTag = new Token.EndTag("root");
        treeBuilder.popStackToClose(endTag);

        assertEquals(1, treeBuilder.stack.size());
        assertSame(doc, treeBuilder.stack.peek());
    }

    @Test
    public void testPopStackToCloseWhenNotFound() {
        treeBuilder.stack.clear();
        Element root = new Element(Tag.valueOf("root"), "");
        treeBuilder.stack.add(doc);
        treeBuilder.stack.add(root);

        Token.EndTag endTag = new Token.EndTag("nonexistent");
        treeBuilder.popStackToClose(endTag);

        assertEquals(2, treeBuilder.stack.size());
        assertSame(doc, treeBuilder.stack.get(0));
        assertSame(root, treeBuilder.stack.get(1));
    }

    @Test
    public void testPopStackToCloseEmptyStack() {
        treeBuilder.stack.clear();
        Token.EndTag endTag = new Token.EndTag("any");
        treeBuilder.popStackToClose(endTag);
        assertEquals(0, treeBuilder.stack.size());
    }

    @Test
    public void testPopStackToCloseMatchingTopStack() {
        treeBuilder.stack.clear();
        Element root = new Element(Tag.valueOf("root"), "");
        treeBuilder.stack.add(doc);
        treeBuilder.stack.add(root);

        Token.EndTag endTag = new Token.EndTag("root");
        treeBuilder.popStackToClose(endTag);

        assertEquals(1, treeBuilder.stack.size());
        assertSame(doc, treeBuilder.stack.peek());
    }

    @Test
    public void testPopStackToCloseMultipleRemovals() {
        treeBuilder.stack.clear();
        Element root = new Element(Tag.valueOf("root"), "");
        Element child1 = new Element(Tag.valueOf("child1"), "");
        Element child2 = new Element(Tag.valueOf("child2"), "");
        Element child3 = new Element(Tag.valueOf("child3"), "");
        treeBuilder.stack.add(doc);
        treeBuilder.stack.add(root);
        treeBuilder.stack.add(child1);
        treeBuilder.stack.add(child2);
        treeBuilder.stack.add(child3);

        Token.EndTag endTag = new Token.EndTag("child2");
        treeBuilder.popStackToClose(endTag);

        assertEquals(3, treeBuilder.stack.size());
        assertSame(doc, treeBuilder.stack.peek());
        assertSame(root, treeBuilder.stack.get(1));
        assertSame(child1, treeBuilder.stack.get(2));
    }
}
