package org.jsoup.parser;

import org.jsoup.nodes.Attributes;
import org.junit.Assert;
import org.junit.Test;

public class TokenTest {

    @Test
    public void testResetNullStringBuilder() {
        Token.reset((StringBuilder) null);
    }

    @Test
    public void testResetValidStringBuilder() {
        StringBuilder sb = new StringBuilder("content");
        Token.reset(sb);
        Assert.assertEquals(0, sb.length());
    }

    @Test
    public void testDoctypeToken() {
        Token.Doctype doctype = new Token.Doctype();
        Assert.assertEquals(Token.TokenType.Doctype, doctype.type);
        Assert.assertEquals("Doctype", doctype.tokenType());
        Assert.assertTrue(doctype.isDoctype());
        Assert.assertFalse(doctype.isStartTag());
        Assert.assertFalse(doctype.isEndTag());
        Assert.assertFalse(doctype.isComment());
        Assert.assertFalse(doctype.isCharacter());
        Assert.assertFalse(doctype.isCData());
        Assert.assertFalse(doctype.isEOF());
        Assert.assertSame(doctype, doctype.asDoctype());

        doctype.name.append("html");
        doctype.pubSysKey = "PUBLIC";
        doctype.publicIdentifier.append("pubId");
        doctype.systemIdentifier.append("sysId");
        doctype.forceQuirks = true;

        Assert.assertEquals("html", doctype.getName());
        Assert.assertEquals("PUBLIC", doctype.getPubSysKey());
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
    public void testStartTagToken() {
        Token.StartTag startTag = new Token.StartTag();
        Assert.assertEquals(Token.TokenType.StartTag, startTag.type);
        Assert.assertEquals("StartTag", startTag.tokenType());
        Assert.assertTrue(startTag.isStartTag());
        Assert.assertFalse(startTag.isDoctype());
        Assert.assertSame(startTag, startTag.asStartTag());

        startTag.name("DIV");
        Assert.assertEquals("DIV", startTag.name());
        Assert.assertEquals("div", startTag.normalName());
        Assert.assertFalse(startTag.isSelfClosing());
        Assert.assertNotNull(startTag.getAttributes());
        Assert.assertEquals("<DIV>", startTag.toString());

        startTag.selfClosing = true;
        Assert.assertTrue(startTag.isSelfClosing());

        Attributes attrs = new Attributes();
        attrs.put("id", "main");
        startTag.nameAttr("SPAN", attrs);
        Assert.assertEquals("SPAN", startTag.name());
        Assert.assertEquals("span", startTag.normalName());
        Assert.assertEquals("<SPAN id=\"main\">", startTag.toString());

        startTag.reset();
        Assert.assertNotNull(startTag.attributes);
        Assert.assertNull(startTag.normalName);
        Assert.assertFalse(startTag.isSelfClosing());
    }

    @Test
    public void testEndTagToken() {
        Token.EndTag endTag = new Token.EndTag();
        Assert.assertEquals(Token.TokenType.EndTag, endTag.type);
        Assert.assertEquals("EndTag", endTag.tokenType());
        Assert.assertTrue(endTag.isEndTag());
        Assert.assertFalse(endTag.isStartTag());
        Assert.assertSame(endTag, endTag.asEndTag());

        endTag.name("p");
        Assert.assertEquals("</p>", endTag.toString());

        endTag.reset();
        Assert.assertNull(endTag.tagName);
    }

    @Test
    public void testTagAttributeHandling() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("input");

        tag.appendAttributeName("type");
        tag.appendAttributeValue("text");
        tag.newAttribute();

        tag.appendAttributeName("disabled");
        tag.setEmptyAttributeValue();
        tag.newAttribute();

        tag.appendAttributeName("required");
        tag.newAttribute();

        tag.appendAttributeName("   ");
        tag.newAttribute();

        Attributes attrs = tag.getAttributes();
        Assert.assertEquals("text", attrs.get("type"));
        Assert.assertEquals("", attrs.get("disabled"));
        Assert.assertEquals("", attrs.get("required"));

        tag.appendAttributeName("autofocus");
        tag.finaliseTag();
        Assert.assertTrue(tag.getAttributes().hasKey("autofocus"));
    }

    @Test
    public void testTagAppendMethods() {
        Token.StartTag tag = new Token.StartTag();
        tag.appendTagName("d");
        tag.appendTagName('i');
        tag.appendTagName("v");
        Assert.assertEquals("div", tag.name());
        Assert.assertEquals("div", tag.normalName());

        tag.appendAttributeName('c');
        tag.appendAttributeName("lass");
        tag.appendAttributeValue("btn");
        tag.appendAttributeValue(" primary");
        tag.appendAttributeValue(' ');
        tag.appendAttributeValue(new char[]{'a', 'c'});
        tag.appendAttributeValue(new int[]{0x74, 0x69, 0x76, 0x65});
        tag.newAttribute();

        Assert.assertEquals("btn primary active", tag.getAttributes().get("class"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTagNameValidationNull() {
        Token.StartTag tag = new Token.StartTag();
        tag.name();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTagNameValidationEmpty() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("");
        tag.name();
    }

    @Test
    public void testCommentToken() {
        Token.Comment comment = new Token.Comment();
        Assert.assertEquals(Token.TokenType.Comment, comment.type);
        Assert.assertEquals("Comment", comment.tokenType());
        Assert.assertTrue(comment.isComment());
        Assert.assertFalse(comment.isCharacter());
        Assert.assertSame(comment, comment.asComment());

        comment.data.append("sample comment");
        comment.bogus = true;
        Assert.assertEquals("sample comment", comment.getData());
        Assert.assertEquals("<!--sample comment-->", comment.toString());
        Assert.assertTrue(comment.bogus);

        comment.reset();
        Assert.assertEquals("", comment.getData());
        Assert.assertFalse(comment.bogus);
    }

    @Test
    public void testCharacterToken() {
        Token.Character character = new Token.Character();
        Assert.assertEquals(Token.TokenType.Character, character.type);
        Assert.assertEquals("Character", character.tokenType());
        Assert.assertTrue(character.isCharacter());
        Assert.assertFalse(character.isCData());
        Assert.assertFalse(commentCheck(character));
        Assert.assertSame(character, character.asCharacter());

        character.data("text data");
        Assert.assertEquals("text data", character.getData());
        Assert.assertEquals("text data", character.toString());

        character.reset();
        Assert.assertNull(character.getData());
    }

    private boolean commentCheck(Token token) {
        return token.isComment();
    }

    @Test
    public void testCDataToken() {
        Token.CData cdata = new Token.CData("data content");
        Assert.assertEquals(Token.TokenType.Character, cdata.type);
        Assert.assertTrue(cdata.isCharacter());
        Assert.assertTrue(cdata.isCData());
        Assert.assertEquals("<![CDATA[data content]]>", cdata.toString());
        Assert.assertEquals("data content", cdata.getData());
    }

    @Test
    public void testEOFToken() {
        Token.EOF eof = new Token.EOF();
        Assert.assertEquals(Token.TokenType.EOF, eof.type);
        Assert.assertEquals("EOF", eof.tokenType());
        Assert.assertTrue(eof.isEOF());
        Assert.assertFalse(eof.isStartTag());
        Assert.assertSame(eof, eof.reset());
    }

    @Test
    public void testTokenTypeEnum() {
        Token.TokenType[] types = Token.TokenType.values();
        Assert.assertEquals(6, types.length);
        Assert.assertEquals(Token.TokenType.Doctype, Token.TokenType.valueOf("Doctype"));
        Assert.assertEquals(Token.TokenType.StartTag, Token.TokenType.valueOf("StartTag"));
        Assert.assertEquals(Token.TokenType.EndTag, Token.TokenType.valueOf("EndTag"));
        Assert.assertEquals(Token.TokenType.Comment, Token.TokenType.valueOf("Comment"));
        Assert.assertEquals(Token.TokenType.Character, Token.TokenType.valueOf("Character"));
        Assert.assertEquals(Token.TokenType.EOF, Token.TokenType.valueOf("EOF"));
    }
}
