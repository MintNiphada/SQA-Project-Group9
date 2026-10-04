package org.jsoup.parser;

import org.jsoup.nodes.Attributes;
import org.junit.Assert;
import org.junit.Test;

public class TokenTest {

    @Test
    public void testResetStringBuilder() {
        Token.reset((StringBuilder) null);
        StringBuilder sb = new StringBuilder("test");
        Token.reset(sb);
        Assert.assertEquals(0, sb.length());
    }

    @Test
    public void testDoctype() {
        Token.Doctype doctype = new Token.Doctype();
        Assert.assertTrue(doctype.isDoctype());
        Assert.assertEquals(doctype, doctype.asDoctype());
        Assert.assertEquals("Doctype", doctype.tokenType());

        doctype.name.append("html");
        doctype.pubSysKey = "SYSTEM";
        doctype.publicIdentifier.append("pubId");
        doctype.systemIdentifier.append("sysId");
        doctype.forceQuirks = true;

        Assert.assertEquals("html", doctype.getName());
        Assert.assertEquals("SYSTEM", doctype.getPubSysKey());
        Assert.assertEquals("pubId", doctype.getPublicIdentifier());
        Assert.assertEquals("sysId", doctype.getSystemIdentifier());
        Assert.assertTrue(doctype.isForceQuirks());

        doctype.reset();
        Assert.assertEquals("", doctype.getName());
        Assert.assertNull(doctype.getPubSysKey());
        Assert.assertEquals("", doctype.getPublicIdentifier());
        Assert.assertEquals("", doctype.getSystemIdentifier());
        Assert.assertFalse(doctype.isForceQuirks());
    }

    @Test
    public void testStartTagBasicsAndReset() {
        Token.StartTag startTag = new Token.StartTag();
        Assert.assertTrue(startTag.isStartTag());
        Assert.assertEquals(startTag, startTag.asStartTag());
        Assert.assertEquals("StartTag", startTag.tokenType());

        startTag.name("DIV");
        Assert.assertEquals("DIV", startTag.name());
        Assert.assertEquals("div", startTag.normalName());

        startTag.selfClosing = true;
        Assert.assertTrue(startTag.isSelfClosing());

        startTag.reset();
        Assert.assertFalse(startTag.isSelfClosing());
        Assert.assertNull(startTag.normalName());
        Assert.assertNotNull(startTag.getAttributes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTagNameValidationNull() {
        Token.StartTag tag = new Token.StartTag();
        tag.name();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTagNameValidationEmpty() {
        Token.StartTag tag = new Token.StartTag();
        tag.tagName = "";
        tag.name();
    }

    @Test
    public void testTagAppendTagName() {
        Token.StartTag tag = new Token.StartTag();
        tag.appendTagName("di");
        tag.appendTagName('v');
        Assert.assertEquals("div", tag.name());
        Assert.assertEquals("div", tag.normalName());
    }

    @Test
    public void testTagAppendAttributeName() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("a");
        tag.appendAttributeName("hr");
        tag.appendAttributeName('e');
        tag.appendAttributeName("f");
        tag.setEmptyAttributeValue();
        tag.newAttribute();

        Assert.assertTrue(tag.getAttributes().hasKey("href"));
        Assert.assertEquals("", tag.getAttributes().get("href"));
    }

    @Test
    public void testTagAttributeSingleStringValue() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("a");
        tag.appendAttributeName("href");
        tag.appendAttributeValue("http://example.com");
        tag.finaliseTag();

        Assert.assertEquals("http://example.com", tag.getAttributes().get("href"));
    }

    @Test
    public void testTagAttributeMultipleValueAppends() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("a");
        tag.appendAttributeName("href");
        tag.appendAttributeValue("http://");
        tag.appendAttributeValue("example");
        tag.appendAttributeValue('.');
        tag.appendAttributeValue(new char[]{'c', 'o', 'm'});
        tag.appendAttributeValue(new int[]{47, 97});
        tag.newAttribute();

        Assert.assertEquals("http://example.com/a", tag.getAttributes().get("href"));
    }

    @Test
    public void testTagBooleanAttribute() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("input");
        tag.appendAttributeName("disabled");
        tag.newAttribute();

        Assert.assertTrue(tag.getAttributes().hasKey("disabled"));
    }

    @Test
    public void testTagNewAttributeWithNullAttributes() {
        Token.EndTag tag = new Token.EndTag();
        tag.name("div");
        Assert.assertNull(tag.getAttributes());
        tag.appendAttributeName("class");
        tag.appendAttributeValue("test");
        tag.newAttribute();
        Assert.assertNotNull(tag.getAttributes());
        Assert.assertEquals("test", tag.getAttributes().get("class"));
    }

    @Test
    public void testTagNewAttributeWithNullPendingAttributeName() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("div");
        tag.newAttribute();
        Assert.assertEquals(0, tag.getAttributes().size());
    }

    @Test
    public void testStartTagNameAttrAndToString() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("div");
        Assert.assertEquals("<div>", startTag.toString());

        Attributes attrs = new Attributes();
        attrs.put("id", "one");
        startTag.nameAttr("SPAN", attrs);
        Assert.assertEquals("SPAN", startTag.name());
        Assert.assertEquals("span", startTag.normalName());
        Assert.assertEquals("<SPAN id=\"one\">", startTag.toString());
    }

    @Test
    public void testEndTag() {
        Token.EndTag endTag = new Token.EndTag();
        Assert.assertTrue(endTag.isEndTag());
        Assert.assertEquals(endTag, endTag.asEndTag());
        Assert.assertEquals("EndTag", endTag.tokenType());

        endTag.name("DIV");
        Assert.assertEquals("</DIV>", endTag.toString());
    }

    @Test
    public void testComment() {
        Token.Comment comment = new Token.Comment();
        Assert.assertTrue(comment.isComment());
        Assert.assertEquals(comment, comment.asComment());
        Assert.assertEquals("Comment", comment.tokenType());

        comment.data.append("hello world");
        comment.bogus = true;
        Assert.assertEquals("hello world", comment.getData());
        Assert.assertEquals("<!--hello world-->", comment.toString());

        comment.reset();
        Assert.assertEquals("", comment.getData());
        Assert.assertFalse(comment.bogus);
    }

    @Test
    public void testCharacter() {
        Token.Character character = new Token.Character();
        Assert.assertTrue(character.isCharacter());
        Assert.assertEquals(character, character.asCharacter());
        Assert.assertEquals("Character", character.tokenType());

        character.data("sample text");
        Assert.assertEquals("sample text", character.getData());
        Assert.assertEquals("sample text", character.toString());

        character.reset();
        Assert.assertNull(character.getData());
    }

    @Test
    public void testEOF() {
        Token.EOF eof = new Token.EOF();
        Assert.assertTrue(eof.isEOF());
        Assert.assertEquals("EOF", eof.tokenType());
        Assert.assertSame(eof, eof.reset());
    }

    @Test
    public void testTokenTypeChecksFalse() {
        Token.Character token = new Token.Character();
        Assert.assertFalse(token.isDoctype());
        Assert.assertFalse(token.isStartTag());
        Assert.assertFalse(token.isEndTag());
        Assert.assertFalse(token.isComment());
        Assert.assertFalse(token.isEOF());
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
