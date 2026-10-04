package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Attribute;

public class TokenTest {

    @Test
    public void testTokenType() {
        assertEquals("Doctype", new Token.Doctype().tokenType());
        assertEquals("StartTag", new Token.StartTag().tokenType());
        assertEquals("EndTag", new Token.EndTag().tokenType());
        assertEquals("Comment", new Token.Comment().tokenType());
        assertEquals("Character", new Token.Character("a").tokenType());
        assertEquals("EOF", new Token.EOF().tokenType());
    }

    @Test
    public void testDoctype() {
        Token.Doctype d = new Token.Doctype();
        assertEquals(Token.TokenType.Doctype, d.type);
        assertEquals("", d.getName());
        assertEquals("", d.getPublicIdentifier());
        assertEquals("", d.getSystemIdentifier());
        assertFalse(d.isForceQuirks());
        d.forceQuirks = true;
        assertTrue(d.isForceQuirks());
        d.name.append("html");
        d.publicIdentifier.append("-//W3C//DTD HTML 4.01//EN");
        d.systemIdentifier.append("http://www.w3.org/TR/html4/strict.dtd");
        assertEquals("html", d.getName());
        assertEquals("-//W3C//DTD HTML 4.01//EN", d.getPublicIdentifier());
        assertEquals("http://www.w3.org/TR/html4/strict.dtd", d.getSystemIdentifier());
    }

    @Test
    public void testTagNewAttributeWithNullAttributes() {
        Token.EndTag tag = new Token.EndTag();
        tag.pendingAttributeName = "href";
        tag.newAttribute();
        assertNotNull(tag.attributes);
        assertEquals(1, tag.attributes.size());
        assertEquals("", tag.attributes.get("href"));
    }

    @Test
    public void testTagNewAttributeWithPendingValue() {
        Token.StartTag tag = new Token.StartTag();
        tag.pendingAttributeName = "class";
        tag.pendingAttributeValue = new StringBuilder("foo");
        tag.newAttribute();
        assertEquals(1, tag.attributes.size());
        assertEquals("foo", tag.attributes.get("class"));
    }

    @Test
    public void testTagNewAttributeWithNullPendingName() {
        Token.StartTag tag = new Token.StartTag();
        tag.pendingAttributeName = null;
        tag.newAttribute();
        assertEquals(0, tag.attributes.size());
    }

    @Test
    public void testTagNewAttributeClearsPending() {
        Token.StartTag tag = new Token.StartTag();
        tag.pendingAttributeName = "id";
        tag.pendingAttributeValue = new StringBuilder("x");
        tag.newAttribute();
        assertNull(tag.pendingAttributeName);
        assertEquals(0, tag.pendingAttributeValue.length());
    }

    @Test
    public void testTagFinaliseTagWithPendingAttribute() {
        Token.StartTag tag = new Token.StartTag();
        tag.pendingAttributeName = "lang";
        tag.finaliseTag();
        assertEquals(1, tag.attributes.size());
        assertEquals("", tag.attributes.get("lang"));
    }

    @Test
    public void testTagFinaliseTagWithoutPendingAttribute() {
        Token.StartTag tag = new Token.StartTag();
        tag.finaliseTag();
        assertEquals(0, tag.attributes.size());
    }

    @Test(expected = NullPointerException.class)
    public void testTagNameWithNullTagName() {
        Token.StartTag tag = new Token.StartTag();
        tag.name();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTagNameWithEmptyTagName() {
        Token.StartTag tag = new Token.StartTag("");
        tag.name();
    }

    @Test
    public void testTagNameValid() {
        Token.StartTag tag = new Token.StartTag("div");
        assertEquals("div", tag.name());
    }

    @Test
    public void testTagNameSetter() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("span");
        assertEquals("span", tag.tagName);
    }

    @Test
    public void testTagIsSelfClosing() {
        Token.StartTag tag = new Token.StartTag();
        assertFalse(tag.isSelfClosing());
        tag.selfClosing = true;
        assertTrue(tag.isSelfClosing());
    }

    @Test
    public void testTagGetAttributes() {
        Token.StartTag tag = new Token.StartTag();
        assertNotNull(tag.getAttributes());
        assertEquals(0, tag.getAttributes().size());
    }

    @Test
    public void testAppendTagNameStringNull() {
        Token.StartTag tag = new Token.StartTag();
        tag.appendTagName("div");
        assertEquals("div", tag.tagName);
    }

    @Test
    public void testAppendTagNameStringNonNull() {
        Token.StartTag tag = new Token.StartTag("a");
        tag.appendTagName("b");
        assertEquals("ab", tag.tagName);
    }

    @Test
    public void testAppendTagNameChar() {
        Token.StartTag tag = new Token.StartTag();
        tag.appendTagName('x');
        assertEquals("x", tag.tagName);
    }

    @Test
    public void testAppendAttributeNameStringNull() {
        Token.StartTag tag = new Token.StartTag();
        tag.appendAttributeName("href");
        assertEquals("href", tag.pendingAttributeName);
    }

    @Test
    public void testAppendAttributeNameStringNonNull() {
        Token.StartTag tag = new Token.StartTag();
        tag.pendingAttributeName = "on";
        tag.appendAttributeName("click");
        assertEquals("onclick", tag.pendingAttributeName);
    }

    @Test
    public void testAppendAttributeNameChar() {
        Token.StartTag tag = new Token.StartTag();
        tag.appendAttributeName('a');
        assertEquals("a", tag.pendingAttributeName);
    }

    @Test
    public void testAppendAttributeValueStringNull() {
        Token.StartTag tag = new Token.StartTag();
        tag.appendAttributeValue("val");
        assertEquals("val", tag.pendingAttributeValue.toString());
    }

    @Test
    public void testAppendAttributeValueStringNonNull() {
        Token.StartTag tag = new Token.StartTag();
        tag.pendingAttributeValue = new StringBuilder("old");
        tag.appendAttributeValue("new");
        assertEquals("oldnew", tag.pendingAttributeValue.toString());
    }

    @Test
    public void testAppendAttributeValueChar() {
        Token.StartTag tag = new Token.StartTag();
        tag.appendAttributeValue('c');
        assertEquals("c", tag.pendingAttributeValue.toString());
    }

    @Test
    public void testStartTagDefaultConstructor() {
        Token.StartTag tag = new Token.StartTag();
        assertEquals(Token.TokenType.StartTag, tag.type);
        assertNotNull(tag.attributes);
        assertEquals(0, tag.attributes.size());
    }

    @Test
    public void testStartTagNameConstructor() {
        Token.StartTag tag = new Token.StartTag("p");
        assertEquals("p", tag.tagName);
        assertNotNull(tag.attributes);
    }

    @Test
    public void testStartTagNameAttributesConstructor() {
        Attributes attrs = new Attributes();
        attrs.put("id", "1");
        Token.StartTag tag = new Token.StartTag("div", attrs);
        assertEquals("div", tag.tagName);
        assertEquals(attrs, tag.attributes);
    }

    @Test
    public void testStartTagToStringNoAttributes() {
        Token.StartTag tag = new Token.StartTag("br");
        assertEquals("<br>", tag.toString());
    }

    @Test
    public void testStartTagToStringWithAttributes() {
        Token.StartTag tag = new Token.StartTag("a");
        tag.attributes.put("href", "http://example.com");
        assertEquals("<a href=\"http://example.com\">", tag.toString());
    }

    @Test
    public void testEndTagDefaultConstructor() {
        Token.EndTag tag = new Token.EndTag();
        assertEquals(Token.TokenType.EndTag, tag.type);
    }

    @Test
    public void testEndTagNameConstructor() {
        Token.EndTag tag = new Token.EndTag("div");
        assertEquals("div", tag.tagName);
    }

    @Test
    public void testEndTagToString() {
        Token.EndTag tag = new Token.EndTag("p");
        assertEquals("</p>", tag.toString());
    }

    @Test
    public void testComment() {
        Token.Comment comment = new Token.Comment();
        assertEquals(Token.TokenType.Comment, comment.type);
        assertEquals("", comment.getData());
        assertEquals("<!---->", comment.toString());
        comment.data.append("test");
        assertEquals("test", comment.getData());
        assertEquals("<!--test-->", comment.toString());
    }

    @Test
    public void testCharacter() {
        Token.Character c = new Token.Character("data");
        assertEquals(Token.TokenType.Character, c.type);
        assertEquals("data", c.getData());
        assertEquals("data", c.toString());
    }

    @Test
    public void testEOF() {
        Token.EOF eof = new Token.EOF();
        assertEquals(Token.TokenType.EOF, eof.type);
    }

    @Test
    public void testIsMethods() {
        Token.Doctype doctype = new Token.Doctype();
        assertTrue(doctype.isDoctype());
        assertFalse(doctype.isStartTag());
        assertFalse(doctype.isEndTag());
        assertFalse(doctype.isComment());
        assertFalse(doctype.isCharacter());
        assertFalse(doctype.isEOF());

        Token.StartTag startTag = new Token.StartTag();
        assertFalse(startTag.isDoctype());
        assertTrue(startTag.isStartTag());
        assertFalse(startTag.isEndTag());
        assertFalse(startTag.isComment());
        assertFalse(startTag.isCharacter());
        assertFalse(startTag.isEOF());

        Token.EndTag endTag = new Token.EndTag();
        assertFalse(endTag.isDoctype());
        assertFalse(endTag.isStartTag());
        assertTrue(endTag.isEndTag());
        assertFalse(endTag.isComment());
        assertFalse(endTag.isCharacter());
        assertFalse(endTag.isEOF());

        Token.Comment comment = new Token.Comment();
        assertFalse(comment.isDoctype());
        assertFalse(comment.isStartTag());
        assertFalse(comment.isEndTag());
        assertTrue(comment.isComment());
        assertFalse(comment.isCharacter());
        assertFalse(comment.isEOF());

        Token.Character character = new Token.Character("x");
        assertFalse(character.isDoctype());
        assertFalse(character.isStartTag());
        assertFalse(character.isEndTag());
        assertFalse(character.isComment());
        assertTrue(character.isCharacter());
        assertFalse(character.isEOF());

        Token.EOF eof = new Token.EOF();
        assertFalse(eof.isDoctype());
        assertFalse(eof.isStartTag());
        assertFalse(eof.isEndTag());
        assertFalse(eof.isComment());
        assertFalse(eof.isCharacter());
        assertTrue(eof.isEOF());
    }

    @Test
    public void testAsMethods() {
        Token.Doctype doctype = new Token.Doctype();
        assertSame(doctype, doctype.asDoctype());

        Token.StartTag startTag = new Token.StartTag();
        assertSame(startTag, startTag.asStartTag());

        Token.EndTag endTag = new Token.EndTag();
        assertSame(endTag, endTag.asEndTag());

        Token.Comment comment = new Token.Comment();
        assertSame(comment, comment.asComment());

        Token.Character character = new Token.Character("x");
        assertSame(character, character.asCharacter());
    }
}
