package org.jsoup.parser;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.junit.Assert;
import org.junit.Test;

public class TokenTest {

    @Test
    public void testDoctype() {
        Token.Doctype doctype = new Token.Doctype();
        Assert.assertEquals(Token.TokenType.Doctype, doctype.type);
        Assert.assertTrue(doctype.isDoctype());
        Assert.assertFalse(doctype.isStartTag());
        Assert.assertFalse(doctype.isEndTag());
        Assert.assertFalse(doctype.isComment());
        Assert.assertFalse(doctype.isCharacter());
        Assert.assertFalse(doctype.isEOF());
        Assert.assertSame(doctype, doctype.asDoctype());

        Assert.assertFalse(doctype.isForceQuirks());
        doctype.forceQuirks = true;
        Assert.assertTrue(doctype.isForceQuirks());

        Assert.assertEquals("", doctype.getName());
        Assert.assertEquals("", doctype.getPublicIdentifier());
        Assert.assertEquals("", doctype.getSystemIdentifier());

        doctype.name.append("html");
        doctype.publicIdentifier.append("pubId");
        doctype.systemIdentifier.append("sysId");

        Assert.assertEquals("html", doctype.getName());
        Assert.assertEquals("pubId", doctype.getPublicIdentifier());
        Assert.assertEquals("sysId", doctype.getSystemIdentifier());
        Assert.assertEquals("Doctype", doctype.tokenType());
    }

    @Test(expected = ClassCastException.class)
    public void testInvalidDoctypeCast() {
        Token token = new Token.EOF();
        token.asDoctype();
    }

    @Test
    public void testStartTag() {
        Token.StartTag tag = new Token.StartTag();
        Assert.assertEquals(Token.TokenType.StartTag, tag.type);
        Assert.assertTrue(tag.isStartTag());
        Assert.assertFalse(tag.isDoctype());
        Assert.assertSame(tag, tag.asStartTag());
        Assert.assertNotNull(tag.getAttributes());
        Assert.assertEquals(0, tag.getAttributes().size());
        Assert.assertEquals("StartTag", tag.tokenType());

        tag.name("div");
        Assert.assertEquals("div", tag.name());
        Assert.assertEquals("<div>", tag.toString());

        tag.selfClosing = true;
        Assert.assertTrue(tag.isSelfClosing());

        Token.StartTag namedTag = new Token.StartTag("span");
        Assert.assertEquals("span", namedTag.name());
        Assert.assertEquals("<span>", namedTag.toString());

        Attributes attrs = new Attributes();
        attrs.put("id", "main");
        Token.StartTag attrTag = new Token.StartTag("p", attrs);
        Assert.assertEquals("p", attrTag.name());
        Assert.assertSame(attrs, attrTag.getAttributes());
        Assert.assertEquals("<p id=\"main\">", attrTag.toString());
    }

    @Test(expected = ClassCastException.class)
    public void testInvalidStartTagCast() {
        Token token = new Token.EOF();
        token.asStartTag();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStartTagEmptyNameValidation() {
        Token.StartTag tag = new Token.StartTag("");
        tag.name();
    }

    @Test
    public void testEndTag() {
        Token.EndTag tag = new Token.EndTag();
        Assert.assertEquals(Token.TokenType.EndTag, tag.type);
        Assert.assertTrue(tag.isEndTag());
        Assert.assertFalse(tag.isStartTag());
        Assert.assertSame(tag, tag.asEndTag());
        Assert.assertNull(tag.getAttributes());
        Assert.assertEquals("EndTag", tag.tokenType());

        tag.name("div");
        Assert.assertEquals("div", tag.name());
        Assert.assertEquals("</div>", tag.toString());

        Token.EndTag namedTag = new Token.EndTag("span");
        Assert.assertEquals("span", namedTag.name());
        Assert.assertEquals("</span>", namedTag.toString());
    }

    @Test(expected = ClassCastException.class)
    public void testInvalidEndTagCast() {
        Token token = new Token.EOF();
        token.asEndTag();
    }

    @Test
    public void testTagAppendersAndAttributes() {
        Token.EndTag tag = new Token.EndTag();
        tag.appendTagName("d");
        tag.appendTagName('i');
        tag.appendTagName("v");
        Assert.assertEquals("div", tag.name());

        tag.appendAttributeName("cl");
        tag.appendAttributeName('a');
        tag.appendAttributeName("ss");

        tag.appendAttributeValue("b");
        tag.appendAttributeValue('o');
        tag.appendAttributeValue("x");

        tag.newAttribute();
        Assert.assertNotNull(tag.getAttributes());
        Assert.assertTrue(tag.getAttributes().hasKey("class"));
        Assert.assertEquals("box", tag.getAttributes().get("class"));

        tag.appendAttributeName("disabled");
        tag.newAttribute();
        Assert.assertTrue(tag.getAttributes().hasKey("disabled"));
        Assert.assertEquals("", tag.getAttributes().get("disabled"));

        tag.appendAttributeName("title");
        tag.appendAttributeValue("tooltip");
        tag.finaliseTag();
        Assert.assertTrue(tag.getAttributes().hasKey("title"));
        Assert.assertEquals("tooltip", tag.getAttributes().get("title"));

        tag.finaliseTag();
    }

    @Test
    public void testComment() {
        Token.Comment comment = new Token.Comment();
        Assert.assertEquals(Token.TokenType.Comment, comment.type);
        Assert.assertTrue(comment.isComment());
        Assert.assertFalse(comment.isStartTag());
        Assert.assertSame(comment, comment.asComment());
        Assert.assertEquals("", comment.getData());
        Assert.assertEquals("<!---->", comment.toString());
        Assert.assertEquals("Comment", comment.tokenType());

        comment.data.append("sample comment");
        Assert.assertEquals("sample comment", comment.getData());
        Assert.assertEquals("<!--sample comment-->", comment.toString());
    }

    @Test(expected = ClassCastException.class)
    public void testInvalidCommentCast() {
        Token token = new Token.EOF();
        token.asComment();
    }

    @Test
    public void testCharacter() {
        Token.Character character = new Token.Character("hello world");
        Assert.assertEquals(Token.TokenType.Character, character.type);
        Assert.assertTrue(character.isCharacter());
        Assert.assertFalse(character.isComment());
        Assert.assertSame(character, character.asCharacter());
        Assert.assertEquals("hello world", character.getData());
        Assert.assertEquals("hello world", character.toString());
        Assert.assertEquals("Character", character.tokenType());
    }

    @Test(expected = ClassCastException.class)
    public void testInvalidCharacterCast() {
        Token token = new Token.EOF();
        token.asCharacter();
    }

    @Test
    public void testEOF() {
        Token.EOF eof = new Token.EOF();
        Assert.assertEquals(Token.TokenType.EOF, eof.type);
        Assert.assertTrue(eof.isEOF());
        Assert.assertFalse(eof.isDoctype());
        Assert.assertFalse(eof.isStartTag());
        Assert.assertFalse(eof.isEndTag());
        Assert.assertFalse(eof.isComment());
        Assert.assertFalse(eof.isCharacter());
        Assert.assertEquals("EOF", eof.tokenType());
    }

    @Test
    public void testTokenTypeEnum() {
        Assert.assertEquals(6, Token.TokenType.values().length);
        Assert.assertEquals(Token.TokenType.Doctype, Token.TokenType.valueOf("Doctype"));
        Assert.assertEquals(Token.TokenType.StartTag, Token.TokenType.valueOf("StartTag"));
        Assert.assertEquals(Token.TokenType.EndTag, Token.TokenType.valueOf("EndTag"));
        Assert.assertEquals(Token.TokenType.Comment, Token.TokenType.valueOf("Comment"));
        Assert.assertEquals(Token.TokenType.Character, Token.TokenType.valueOf("Character"));
        Assert.assertEquals(Token.TokenType.EOF, Token.TokenType.valueOf("EOF"));
    }
}
