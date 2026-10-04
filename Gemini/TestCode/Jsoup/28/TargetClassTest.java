package org.jsoup.parser;

import org.junit.Assert;
import org.junit.Test;

public class TokeniserTest {

    @Test
    public void testStateTransitions() {
        CharacterReader reader = new CharacterReader("abc");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Assert.assertEquals(TokeniserState.Data, tokeniser.getState());

        tokeniser.transition(TokeniserState.TagOpen);
        Assert.assertEquals(TokeniserState.TagOpen, tokeniser.getState());
        Assert.assertEquals('a', reader.current());

        tokeniser.advanceTransition(TokeniserState.TagName);
        Assert.assertEquals(TokeniserState.TagName, tokeniser.getState());
        Assert.assertEquals('b', reader.current());
    }

    @Test
    public void testEmitCharacterAndString() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = ParseErrorList.noTracking();
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        tokeniser.emit('a');
        tokeniser.emit("bc");

        tokeniser.emit(new Token.EOF());
        Token token = tokeniser.read();
        Assert.assertTrue(token.isCharacter());
        Assert.assertEquals("abc", ((Token.Character) token).getData());

        Token nextToken = tokeniser.read();
        Assert.assertTrue(nextToken.isEOF());
    }

    @Test
    public void testCreateAndEmitTagPending() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = ParseErrorList.noTracking();
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Token.Tag startTag = tokeniser.createTagPending(true);
        Assert.assertTrue(startTag instanceof Token.StartTag);
        startTag.name("div");
        tokeniser.emitTagPending();

        Token token = tokeniser.read();
        Assert.assertTrue(token.isStartTag());
        Assert.assertEquals("div", token.asStartTag().name());

        Token.Tag endTag = tokeniser.createTagPending(false);
        Assert.assertTrue(endTag instanceof Token.EndTag);
        endTag.name("div");
        tokeniser.emitTagPending();

        token = tokeniser.read();
        Assert.assertTrue(token.isEndTag());
        Assert.assertEquals("div", token.asEndTag().name());
    }

    @Test
    public void testCreateAndEmitCommentPending() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = ParseErrorList.noTracking();
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        tokeniser.createCommentPending();
        tokeniser.commentPending.data.append("comment text");
        tokeniser.emitCommentPending();

        Token token = tokeniser.read();
        Assert.assertTrue(token.isComment());
        Assert.assertEquals("comment text", token.asComment().getData());
    }

    @Test
    public void testCreateAndEmitDoctypePending() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = ParseErrorList.noTracking();
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        tokeniser.createDoctypePending();
        tokeniser.doctypePending.name.append("html");
        tokeniser.emitDoctypePending();

        Token token = tokeniser.read();
        Assert.assertTrue(token.isDoctype());
        Assert.assertEquals("html", token.asDoctype().getName());
    }

    @Test
    public void testCreateTempBuffer() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = ParseErrorList.noTracking();
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Assert.assertNull(tokeniser.dataBuffer);
        tokeniser.createTempBuffer();
        Assert.assertNotNull(tokeniser.dataBuffer);
        Assert.assertEquals(0, tokeniser.dataBuffer.length());
    }

    @Test
    public void testIsAppropriateEndTagToken() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = ParseErrorList.noTracking();
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        tokeniser.createTagPending(false);
        tokeniser.tagPending.name("p");

        Assert.assertFalse(tokeniser.isAppropriateEndTagToken());

        Token.StartTag startTag = new Token.StartTag();
        startTag.name("div");
        tokeniser.emit(startTag);
        tokeniser.read();

        Assert.assertEquals("div", tokeniser.appropriateEndTagName());
        Assert.assertFalse(tokeniser.isAppropriateEndTagToken());

        tokeniser.tagPending.name("div");
        Assert.assertTrue(tokeniser.isAppropriateEndTagToken());
    }

    @Test
    public void testSelfClosingFlagTracking() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Token.StartTag startTag = new Token.StartTag();
        startTag.name("img");
        startTag.selfClosing = true;

        tokeniser.emit(startTag);
        tokeniser.read();

        tokeniser.emit(new Token.EOF());
        tokeniser.read();

        Assert.assertEquals(1, errors.size());
        Assert.assertTrue(errors.get(0).getErrorMessage().contains("Self closing flag not acknowledged"));

        tokeniser.acknowledgeSelfClosingFlag();
    }

    @Test
    public void testEndTagWithAttributesError() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Token.EndTag endTag = new Token.EndTag();
        endTag.name("div");
        endTag.attributes = new org.jsoup.nodes.Attributes();
        endTag.attributes.put("class", "foo");

        tokeniser.emit(endTag);
        Assert.assertEquals(1, errors.size());
        Assert.assertTrue(errors.get(0).getErrorMessage().contains("Attributes incorrectly present on end tag"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmitWithPendingThrowsException() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = ParseErrorList.noTracking();
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        tokeniser.emit(new Token.EOF());
        tokeniser.emit(new Token.EOF());
    }

    @Test
    public void testErrorTracking() {
        CharacterReader reader = new CharacterReader("test");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        tokeniser.error(TokeniserState.Data);
        Assert.assertEquals(1, errors.size());

        tokeniser.eofError(TokeniserState.TagOpen);
        Assert.assertEquals(2, errors.size());

        ParseErrorList noErrors = ParseErrorList.noTracking();
        Tokeniser noErrorTokeniser = new Tokeniser(reader, noErrors);
        noErrorTokeniser.error(TokeniserState.Data);
        noErrorTokeniser.eofError(TokeniserState.TagOpen);
        Assert.assertEquals(0, noErrors.size());
    }

    @Test
    public void testCurrentNodeInHtmlNS() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = ParseErrorList.noTracking();
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Assert.assertTrue(tokeniser.currentNodeInHtmlNS());
    }

    @Test
    public void testConsumeCharacterReferenceEmptyOrWhitespaceOrExcluded() {
        ParseErrorList errors = ParseErrorList.tracking(10);

        Tokeniser tokeniser = new Tokeniser(new CharacterReader(""), errors);
        Assert.assertNull(tokeniser.consumeCharacterReference(null, false));

        tokeniser = new Tokeniser(new CharacterReader("x"), errors);
        Assert.assertNull(tokeniser.consumeCharacterReference('x', false));

        for (char c : new char[]{'\t', '\n', '\r', '\f', ' ', '<', '&'}) {
            tokeniser = new Tokeniser(new CharacterReader(String.valueOf(c)), errors);
            Assert.assertNull(tokeniser.consumeCharacterReference(null, false));
        }
    }

    @Test
    public void testConsumeCharacterReferenceNumericDecimal() {
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(new CharacterReader("#65;"), errors);
        Character c = tokeniser.consumeCharacterReference(null, false);
        Assert.assertEquals(Character.valueOf('A'), c);
        Assert.assertEquals(0, errors.size());

        tokeniser = new Tokeniser(new CharacterReader("#65"), errors);
        c = tokeniser.consumeCharacterReference(null, false);
        Assert.assertEquals(Character.valueOf('A'), c);
        Assert.assertEquals(1, errors.size());
        Assert.assertTrue(errors.get(0).getErrorMessage().contains("missing semicolon"));
    }

    @Test
    public void testConsumeCharacterReferenceNumericHex() {
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(new CharacterReader("#x41;"), errors);
        Character c = tokeniser.consumeCharacterReference(null, false);
        Assert.assertEquals(Character.valueOf('A'), c);

        tokeniser = new Tokeniser(new CharacterReader("#X41;"), errors);
        c = tokeniser.consumeCharacterReference(null, false);
        Assert.assertEquals(Character.valueOf('A'), c);
    }

    @Test
    public void testConsumeCharacterReferenceNumericNoDigits() {
        ParseErrorList errors = ParseErrorList.tracking(10);
        CharacterReader reader = new CharacterReader("#;");
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        Character c = tokeniser.consumeCharacterReference(null, false);
        Assert.assertNull(c);
        Assert.assertEquals(1, errors.size());
        Assert.assertTrue(errors.get(0).getErrorMessage().contains("numeric reference with no numerals"));
        Assert.assertEquals('#', reader.current());
    }

    @Test
    public void testConsumeCharacterReferenceNumericOutOfRange() {
        ParseErrorList errors = ParseErrorList.tracking(10);

        Tokeniser tokeniser = new Tokeniser(new CharacterReader("#xD800;"), errors);
        Character c = tokeniser.consumeCharacterReference(null, false);
        Assert.assertEquals(Character.valueOf(Tokeniser.replacementChar), c);
        Assert.assertTrue(errors.get(0).getErrorMessage().contains("character outside of valid range"));

        errors.clear();
        tokeniser = new Tokeniser(new CharacterReader("#x110000;"), errors);
        c = tokeniser.consumeCharacterReference(null, false);
        Assert.assertEquals(Character.valueOf(Tokeniser.replacementChar), c);
        Assert.assertTrue(errors.get(0).getErrorMessage().contains("character outside of valid range"));

        errors.clear();
        tokeniser = new Tokeniser(new CharacterReader("#999999999999999999999999999999;"), errors);
        c = tokeniser.consumeCharacterReference(null, false);
        Assert.assertEquals(Character.valueOf(Tokeniser.replacementChar), c);
        Assert.assertTrue(errors.get(0).getErrorMessage().contains("character outside of valid range"));
    }

    @Test
    public void testConsumeCharacterReferenceNamed() {
        ParseErrorList errors = ParseErrorList.tracking(10);

        Tokeniser tokeniser = new Tokeniser(new CharacterReader("lt;"), errors);
        Character c = tokeniser.consumeCharacterReference(null, false);
        Assert.assertEquals(Character.valueOf('<'), c);
        Assert.assertEquals(0, errors.size());

        tokeniser = new Tokeniser(new CharacterReader("lt"), errors);
        c = tokeniser.consumeCharacterReference(null, false);
        Assert.assertEquals(Character.valueOf('<'), c);
        Assert.assertEquals(1, errors.size());
        Assert.assertTrue(errors.get(0).getErrorMessage().contains("missing semicolon"));
    }

    @Test
    public void testConsumeCharacterReferenceNamedInvalid() {
        ParseErrorList errors = ParseErrorList.tracking(10);

        CharacterReader reader = new CharacterReader("unknownNamedEntity;");
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        Character c = tokeniser.consumeCharacterReference(null, false);
        Assert.assertNull(c);
        Assert.assertEquals('u', reader.current());
        Assert.assertEquals(1, errors.size());
        Assert.assertTrue(errors.get(0).getErrorMessage().contains("invalid named referenece"));

        errors.clear();
        reader = new CharacterReader("unknownNamedEntity");
        tokeniser = new Tokeniser(reader, errors);
        c = tokeniser.consumeCharacterReference(null, false);
        Assert.assertNull(c);
        Assert.assertEquals('u', reader.current());
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testConsumeCharacterReferenceInAttributeWithDisallowedFollowingChar() {
        ParseErrorList errors = ParseErrorList.tracking(10);

        CharacterReader reader = new CharacterReader("amp=foo");
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        Character c = tokeniser.consumeCharacterReference(null, true);
        Assert.assertNull(c);
        Assert.assertEquals('a', reader.current());

        reader = new CharacterReader("amp-foo");
        tokeniser = new Tokeniser(reader, errors);
        c = tokeniser.consumeCharacterReference(null, true);
        Assert.assertNull(c);

        reader = new CharacterReader("amp_foo");
        tokeniser = new Tokeniser(reader, errors);
        c = tokeniser.consumeCharacterReference(null, true);
        Assert.assertNull(c);

        reader = new CharacterReader("amp1foo");
        tokeniser = new Tokeniser(reader, errors);
        c = tokeniser.consumeCharacterReference(null, true);
        Assert.assertNull(c);

        reader = new CharacterReader("amp;");
        tokeniser = new Tokeniser(reader, errors);
        c = tokeniser.consumeCharacterReference(null, true);
        Assert.assertEquals(Character.valueOf('&'), c);
    }

    @Test
    public void testFullTokenisationWorkflow() {
        CharacterReader reader = new CharacterReader("<p class=\"test\">Hello &amp; world</p>");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Token token = tokeniser.read();
        Assert.assertTrue(token.isStartTag());
        Assert.assertEquals("p", token.asStartTag().name());
        Assert.assertEquals("test", token.asStartTag().attributes.get("class"));

        token = tokeniser.read();
        Assert.assertTrue(token.isCharacter());
        Assert.assertEquals("Hello & world", token.asCharacter().getData());

        token = tokeniser.read();
        Assert.assertTrue(token.isEndTag());
        Assert.assertEquals("p", token.asEndTag().name());

        token = tokeniser.read();
        Assert.assertTrue(token.isEOF());
    }
}
