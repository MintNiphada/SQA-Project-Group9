package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class TokeniserTest {

    @Test
    public void testReadWithCharBuffer() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = new ParseErrorList(0, 0);
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        tokeniser.emit(new Token.StartTag());
        tokeniser.emit('a');
        Token result = tokeniser.read();
        assertTrue(result instanceof Token.Character);
        assertEquals("a", ((Token.Character) result).getData());
        assertTrue(tokeniser.isEmitPending);
    }

    @Test
    public void testReadWithEmitPendingNoCharBuffer() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = new ParseErrorList(0, 0);
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        Token.StartTag startTag = new Token.StartTag();
        tokeniser.emit(startTag);
        Token result = tokeniser.read();
        assertSame(startTag, result);
        assertFalse(tokeniser.isEmitPending);
    }

    @Test
    public void testReadSelfClosingFlagNotAcknowledged() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = new ParseErrorList(0, 10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        Token.StartTag startTag = new Token.StartTag();
        startTag.selfClosing = true;
        tokeniser.emit(startTag);
        tokeniser.read();
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getErrorMessage().contains("Self closing flag not acknowledged"));
    }

    @Test
    public void testEmitTokenStartTag() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = new ParseErrorList(0, 0);
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        Token.StartTag startTag = new Token.StartTag();
        startTag.tagName = "div";
        tokeniser.emit(startTag);
        assertSame(startTag, tokeniser.emitPending);
        assertTrue(tokeniser.isEmitPending);
        assertSame(startTag, tokeniser.lastStartTag);
        assertTrue(tokeniser.selfClosingFlagAcknowledged);
    }

    @Test
    public void testEmitTokenStartTagSelfClosing() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = new ParseErrorList(0, 0);
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        Token.StartTag startTag = new Token.StartTag();
        startTag.selfClosing = true;
        tokeniser.emit(startTag);
        assertFalse(tokeniser.selfClosingFlagAcknowledged);
    }

    @Test
    public void testEmitTokenEndTagWithAttributes() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = new ParseErrorList(0, 10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        Token.EndTag endTag = new Token.EndTag();
        endTag.attributes = new org.jsoup.nodes.Attributes();
        tokeniser.emit(endTag);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getErrorMessage().contains("Attributes incorrectly present on end tag"));
    }

    @Test
    public void testEmitString() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = new ParseErrorList(0, 0);
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        tokeniser.emit("hello");
        assertEquals("hello", tokeniser.charBuffer.toString());
    }

    @Test
    public void testEmitChar() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = new ParseErrorList(0, 0);
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        tokeniser.emit('x');
        assertEquals("x", tokeniser.charBuffer.toString());
    }

    @Test
    public void testGetState() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = new ParseErrorList(0, 0);
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        assertEquals(TokeniserState.Data, tokeniser.getState());
    }

    @Test
    public void testTransition() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = new ParseErrorList(0, 0);
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        tokeniser.transition(TokeniserState.TagOpen);
        assertEquals(TokeniserState.TagOpen, tokeniser.getState());
    }

    @Test
    public void testAdvanceTransition() {
        CharacterReader reader = new CharacterReader("abc");
        ParseErrorList errors = new ParseErrorList(0, 0);
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        tokeniser.advanceTransition(TokeniserState.TagOpen);
        assertEquals(TokeniserState.TagOpen, tokeniser.getState());
        assertEquals('b', reader.current());
    }

    @Test
    public void testAcknowledgeSelfClosingFlag() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = new ParseErrorList(0, 0);
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        tokeniser.selfClosingFlagAcknowledged = false;
        tokeniser.acknowledgeSelfClosingFlag();
        assertTrue(tokeniser.selfClosingFlagAcknowledged);
    }

    @Test
    public void testConsumeCharacterReferenceEmptyReader() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = new ParseErrorList(0, 0);
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        assertNull(tokeniser.consumeCharacterReference(null, false));
    }

    @Test
    public void testConsumeCharacterReferenceAdditionalAllowedCharacterMatches() {
        CharacterReader reader = new CharacterReader("a");
        ParseErrorList errors = new ParseErrorList(0, 0);
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        assertNull(tokeniser.consumeCharacterReference('a', false));
    }

    @Test
    public void testConsumeCharacterReferenceMatchesAny() {
        CharacterReader reader = new CharacterReader("<");
        ParseErrorList errors = new ParseErrorList(0, 0);
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        assertNull(tokeniser.consumeCharacterReference(null, false));
    }

    @Test
    public void testConsumeCharacterReferenceNumericNoNumerals() {
        CharacterReader reader = new CharacterReader("#");
        ParseErrorList errors = new ParseErrorList(0, 10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        assertNull(tokeniser.consumeCharacterReference(null, false));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getErrorMessage().contains("numeric reference with no numerals"));
    }

    @Test
    public void testConsumeCharacterReferenceNumericMissingSemicolon() {
        CharacterReader reader = new CharacterReader("#65");
        ParseErrorList errors = new ParseErrorList(0, 10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        Character result = tokeniser.consumeCharacterReference(null, false);
        assertEquals(Character.valueOf('A'), result);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getErrorMessage().contains("missing semicolon"));
    }

    @Test
    public void testConsumeCharacterReferenceNumericHex() {
        CharacterReader reader = new CharacterReader("#x41;");
        ParseErrorList errors = new ParseErrorList(0, 0);
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        Character result = tokeniser.consumeCharacterReference(null, false);
        assertEquals(Character.valueOf('A'), result);
    }

    @Test
    public void testConsumeCharacterReferenceNumericOutOfRangeHighSurrogate() {
        CharacterReader reader = new CharacterReader("#xD800;");
        ParseErrorList errors = new ParseErrorList(0, 10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        Character result = tokeniser.consumeCharacterReference(null, false);
        assertEquals(Character.valueOf('\uFFFD'), result);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getErrorMessage().contains("character outside of valid range"));
    }

    @Test
    public void testConsumeCharacterReferenceNumericOutOfRangeAboveMax() {
        CharacterReader reader = new CharacterReader("#110000;");
        ParseErrorList errors = new ParseErrorList(0, 10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        Character result = tokeniser.consumeCharacterReference(null, false);
        assertEquals(Character.valueOf('\uFFFD'), result);
    }

    @Test
    public void testConsumeCharacterReferenceNamedFound() {
        CharacterReader reader = new CharacterReader("amp;");
        ParseErrorList errors = new ParseErrorList(0, 0);
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        Character result = tokeniser.consumeCharacterReference(null, false);
        assertEquals(Character.valueOf('&'), result);
    }

    @Test
    public void testConsumeCharacterReferenceNamedNotFound() {
        CharacterReader reader = new CharacterReader("unknown;");
        ParseErrorList errors = new ParseErrorList(0, 10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        assertNull(tokeniser.consumeCharacterReference(null, false));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getErrorMessage().contains("invalid named referenece"));
    }

    @Test
    public void testConsumeCharacterReferenceNamedInAttributeWithFollowingChar() {
        CharacterReader reader = new CharacterReader("amp=abc");
        ParseErrorList errors = new ParseErrorList(0, 0);
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        assertNull(tokeniser.consumeCharacterReference(null, true));
    }

    @Test
    public void testConsumeCharacterReferenceNamedMissingSemicolon() {
        CharacterReader reader = new CharacterReader("amp");
        ParseErrorList errors = new ParseErrorList(0, 10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        Character result = tokeniser.consumeCharacterReference(null, false);
        assertEquals(Character.valueOf('&'), result);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getErrorMessage().contains("missing semicolon"));
    }

    @Test
    public void testCreateTagPendingStart() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = new ParseErrorList(0, 0);
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        Token.Tag tag = tokeniser.createTagPending(true);
        assertTrue(tag instanceof Token.StartTag);
        assertSame(tag, tokeniser.tagPending);
    }

    @Test
    public void testCreateTagPendingEnd() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = new ParseErrorList(0, 0);
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        Token.Tag tag = tokeniser.createTagPending(false);
        assertTrue(tag instanceof Token.EndTag);
        assertSame(tag, tokeniser.tagPending);
    }

    @Test
    public void testEmitTagPending() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = new ParseErrorList(0, 0);
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        Token.StartTag startTag = new Token.StartTag();
        tokeniser.tagPending = startTag;
        tokeniser.emitTagPending();
        assertTrue(tokeniser.isEmitPending);
        assertSame(startTag, tokeniser.emitPending);
    }

    @Test
    public void testCreateCommentPending() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = new ParseErrorList(0, 0);
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        tokeniser.createCommentPending();
        assertNotNull(tokeniser.commentPending);
        assertTrue(tokeniser.commentPending instanceof Token.Comment);
    }

    @Test
    public void testEmitCommentPending() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = new ParseErrorList(0, 0);
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        Token.Comment comment = new Token.Comment();
        tokeniser.commentPending = comment;
        tokeniser.emitCommentPending();
        assertTrue(tokeniser.isEmitPending);
        assertSame(comment, tokeniser.emitPending);
    }

    @Test
    public void testCreateDoctypePending() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = new ParseErrorList(0, 0);
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        tokeniser.createDoctypePending();
        assertNotNull(tokeniser.doctypePending);
        assertTrue(tokeniser.doctypePending instanceof Token.Doctype);
    }

    @Test
    public void testEmitDoctypePending() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = new ParseErrorList(0, 0);
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        Token.Doctype doctype = new Token.Doctype();
        tokeniser.doctypePending = doctype;
        tokeniser.emitDoctypePending();
        assertTrue(tokeniser.isEmitPending);
        assertSame(doctype, tokeniser.emitPending);
    }

    @Test
    public void testCreateTempBuffer() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = new ParseErrorList(0, 0);
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        tokeniser.createTempBuffer();
        assertNotNull(tokeniser.dataBuffer);
    }

    @Test
    public void testIsAppropriateEndTagTokenLastStartTagNull() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = new ParseErrorList(0, 0);
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        tokeniser.tagPending = new Token.EndTag();
        assertFalse(tokeniser.isAppropriateEndTagToken());
    }

    @Test
    public void testIsAppropriateEndTagTokenMatching() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = new ParseErrorList(0, 0);
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        Token.StartTag startTag = new Token.StartTag();
        startTag.tagName = "p";
        tokeniser.lastStartTag = startTag;
        Token.EndTag endTag = new Token.EndTag();
        endTag.tagName = "p";
        tokeniser.tagPending = endTag;
        assertTrue(tokeniser.isAppropriateEndTagToken());
    }

    @Test
    public void testIsAppropriateEndTagTokenNotMatching() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = new ParseErrorList(0, 0);
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        Token.StartTag startTag = new Token.StartTag();
        startTag.tagName = "p";
        tokeniser.lastStartTag = startTag;
        Token.EndTag endTag = new Token.EndTag();
        endTag.tagName = "div";
        tokeniser.tagPending = endTag;
        assertFalse(tokeniser.isAppropriateEndTagToken());
    }

    @Test
    public void testAppropriateEndTagName() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = new ParseErrorList(0, 0);
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        Token.StartTag startTag = new Token.StartTag();
        startTag.tagName = "span";
        tokeniser.lastStartTag = startTag;
        assertEquals("span", tokeniser.appropriateEndTagName());
    }

    @Test
    public void testErrorState() {
        CharacterReader reader = new CharacterReader("x");
        ParseErrorList errors = new ParseErrorList(0, 10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        tokeniser.error(TokeniserState.Data);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getErrorMessage().contains("Unexpected character"));
    }

    @Test
    public void testEofError() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = new ParseErrorList(0, 10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        tokeniser.eofError(TokeniserState.Data);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getErrorMessage().contains("Unexpectedly reached end of file"));
    }

    @Test
    public void testCharacterReferenceError() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = new ParseErrorList(0, 10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        tokeniser.characterReferenceError("test error");
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getErrorMessage().contains("Invalid character reference"));
    }

    @Test
    public void testErrorString() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = new ParseErrorList(0, 10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        tokeniser.error("custom error");
        assertEquals(1, errors.size());
        assertEquals("custom error", errors.get(0).getErrorMessage());
    }

    @Test
    public void testCurrentNodeInHtmlNS() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = new ParseErrorList(0, 0);
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        assertTrue(tokeniser.currentNodeInHtmlNS());
    }
}
