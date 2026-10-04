package org.jsoup.parser;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TokeniserTest {

    @Mock
    private CharacterReader reader;
    @Mock
    private TokeniserState state;
    @Mock
    private Token.StartTag startTag;
    @Mock
    private Token.EndTag endTag;
    @Mock
    private Token.Comment comment;
    @Mock
    private Token.Doctype doctype;
    @Mock
    private Token.Character characterToken;

    private Tokeniser tokeniser;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        tokeniser = new Tokeniser(reader);
        // set state to a mock to avoid real state transitions during read tests
        setField(tokeniser, "state", state);
    }

    // Helper to set private field via reflection
    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Object getField(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    @Test
    public void testConstructor() {
        assertNotNull(tokeniser);
        // reader is set
        assertEquals(reader, getField(tokeniser, "reader"));
    }

    @Test
    public void testReadSelfClosingFlagNotAcknowledged() throws Exception {
        // set selfClosingFlagAcknowledged to false
        setField(tokeniser, "selfClosingFlagAcknowledged", false);
        // set isEmitPending to true to exit while loop
        setField(tokeniser, "isEmitPending", true);
        // charBuffer empty
        StringBuilder charBuffer = (StringBuilder) getField(tokeniser, "charBuffer");
        charBuffer.setLength(0);
        // set emitPending to a token
        Token token = mock(Token.class);
        setField(tokeniser, "emitPending", token);

        Token result = tokeniser.read();
        // should have added error about self closing flag
        List<ParseError> errors = (List<ParseError>) getField(tokeniser, "errors");
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getErrorMessage().contains("Self closing flag not acknowledged"));
        // selfClosingFlagAcknowledged should be true now
        assertTrue((Boolean) getField(tokeniser, "selfClosingFlagAcknowledged"));
        // should return emitPending
        assertEquals(token, result);
        // isEmitPending should be false
        assertFalse((Boolean) getField(tokeniser, "isEmitPending"));
    }

    @Test
    public void testReadWithCharBufferContent() throws Exception {
        // set isEmitPending true to skip loop
        setField(tokeniser, "isEmitPending", true);
        StringBuilder charBuffer = (StringBuilder) getField(tokeniser, "charBuffer");
        charBuffer.append("test");
        // set emitPending to some token (should be ignored)
        setField(tokeniser, "emitPending", mock(Token.class));

        Token result = tokeniser.read();
        assertTrue(result instanceof Token.Character);
        assertEquals("test", ((Token.Character) result).getData());
        // charBuffer should be cleared
        assertEquals(0, charBuffer.length());
        // isEmitPending remains true? Actually read() does not change isEmitPending when charBuffer>0, it returns char token and leaves emitPending for next read. So isEmitPending stays true.
        assertTrue((Boolean) getField(tokeniser, "isEmitPending"));
    }

    @Test
    public void testReadWithEmptyCharBuffer() throws Exception {
        setField(tokeniser, "isEmitPending", true);
        StringBuilder charBuffer = (StringBuilder) getField(tokeniser, "charBuffer");
        charBuffer.setLength(0);
        Token token = mock(Token.class);
        setField(tokeniser, "emitPending", token);

        Token result = tokeniser.read();
        assertEquals(token, result);
        assertFalse((Boolean) getField(tokeniser, "isEmitPending"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmitWhenPendingAlready() throws Exception {
        setField(tokeniser, "isEmitPending", true);
        tokeniser.emit(mock(Token.class));
    }

    @Test
    public void testEmitStartTag() throws Exception {
        Token.StartTag startTag = new Token.StartTag();
        startTag.selfClosing = true;
        tokeniser.emit(startTag);
        assertTrue((Boolean) getField(tokeniser, "isEmitPending"));
        assertEquals(startTag, getField(tokeniser, "emitPending"));
        assertEquals(startTag, getField(tokeniser, "lastStartTag"));
        assertFalse((Boolean) getField(tokeniser, "selfClosingFlagAcknowledged"));
    }

    @Test
    public void testEmitStartTagNotSelfClosing() throws Exception {
        Token.StartTag startTag = new Token.StartTag();
        startTag.selfClosing = false;
        tokeniser.emit(startTag);
        assertTrue((Boolean) getField(tokeniser, "selfClosingFlagAcknowledged"));
    }

    @Test
    public void testEmitEndTagWithAttributes() throws Exception {
        Token.EndTag endTag = new Token.EndTag();
        endTag.attributes.put("attr", "val");
        tokeniser.emit(endTag);
        List<ParseError> errors = (List<ParseError>) getField(tokeniser, "errors");
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getErrorMessage().contains("Attributes incorrectly present on end tag"));
    }

    @Test
    public void testEmitEndTagNoAttributes() throws Exception {
        Token.EndTag endTag = new Token.EndTag();
        tokeniser.emit(endTag);
        List<ParseError> errors = (List<ParseError>) getField(tokeniser, "errors");
        assertEquals(0, errors.size());
    }

    @Test
    public void testEmitString() throws Exception {
        tokeniser.emit("hello");
        StringBuilder charBuffer = (StringBuilder) getField(tokeniser, "charBuffer");
        assertEquals("hello", charBuffer.toString());
    }

    @Test
    public void testEmitChar() throws Exception {
        tokeniser.emit('a');
        StringBuilder charBuffer = (StringBuilder) getField(tokeniser, "charBuffer");
        assertEquals("a", charBuffer.toString());
    }

    @Test
    public void testGetState() {
        assertEquals(state, tokeniser.getState());
    }

    @Test
    public void testTransition() throws Exception {
        TokeniserState newState = mock(TokeniserState.class);
        tokeniser.transition(newState);
        assertEquals(newState, getField(tokeniser, "state"));
    }

    @Test
    public void testAdvanceTransition() throws Exception {
        TokeniserState newState = mock(TokeniserState.class);
        tokeniser.advanceTransition(newState);
        verify(reader).advance();
        assertEquals(newState, getField(tokeniser, "state"));
    }

    @Test
    public void testAcknowledgeSelfClosingFlag() throws Exception {
        setField(tokeniser, "selfClosingFlagAcknowledged", false);
        tokeniser.acknowledgeSelfClosingFlag();
        assertTrue((Boolean) getField(tokeniser, "selfClosingFlagAcknowledged"));
    }

    // consumeCharacterReference tests

    @Test
    public void testConsumeCharacterReferenceEmptyReader() {
        when(reader.isEmpty()).thenReturn(true);
        assertNull(tokeniser.consumeCharacterReference(null, false));
    }

    @Test
    public void testConsumeCharacterReferenceAdditionalAllowedCharacterMatches() {
        when(reader.isEmpty()).thenReturn(false);
        when(reader.current()).thenReturn('a');
        assertNull(tokeniser.consumeCharacterReference('a', false));
    }

    @Test
    public void testConsumeCharacterReferenceMatchesAny() {
        when(reader.isEmpty()).thenReturn(false);
        when(reader.current()).thenReturn('x');
        when(reader.matchesAny('\t', '\n', '\f', '<', '&')).thenReturn(true);
        assertNull(tokeniser.consumeCharacterReference(null, false));
    }

    @Test
    public void testConsumeCharacterReferenceNumberedHexSuccess() {
        when(reader.isEmpty()).thenReturn(false);
        when(reader.current()).thenReturn('x');
        when(reader.matchesAny('\t', '\n', '\f', '<', '&')).thenReturn(false);
        when(reader.matchConsume("#")).thenReturn(true);
        when(reader.matchConsumeIgnoreCase("X")).thenReturn(true);
        when(reader.consumeHexSequence()).thenReturn("1A");
        when(reader.matchConsume(";")).thenReturn(true);
        // charval = 0x1A = 26
        Character result = tokeniser.consumeCharacterReference(null, false);
        assertEquals(Character.valueOf((char)26), result);
    }

    @Test
    public void testConsumeCharacterReferenceNumberedDecimalSuccess() {
        when(reader.isEmpty()).thenReturn(false);
        when(reader.current()).thenReturn('x');
        when(reader.matchesAny('\t', '\n', '\f', '<', '&')).thenReturn(false);
        when(reader.matchConsume("#")).thenReturn(true);
        when(reader.matchConsumeIgnoreCase("X")).thenReturn(false);
        when(reader.consumeDigitSequence()).thenReturn("65");
        when(reader.matchConsume(";")).thenReturn(true);
        // charval = 65 -> 'A'
        assertEquals(Character.valueOf('A'), tokeniser.consumeCharacterReference(null, false));
    }

    @Test
    public void testConsumeCharacterReferenceNumberedMissingDigits() {
        when(reader.isEmpty()).thenReturn(false);
        when(reader.current()).thenReturn('x');
        when(reader.matchesAny('\t', '\n', '\f', '<', '&')).thenReturn(false);
        when(reader.matchConsume("#")).thenReturn(true);
        when(reader.matchConsumeIgnoreCase("X")).thenReturn(false);
        when(reader.consumeDigitSequence()).thenReturn("");
        // should error and rewind
        tokeniser.consumeCharacterReference(null, false);
        verify(reader).rewindToMark();
        // error added
        List<ParseError> errors = (List<ParseError>) getField(tokeniser, "errors");
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getErrorMessage().contains("Invalid character reference"));
    }

    @Test
    public void testConsumeCharacterReferenceNumberedMissingSemicolon() {
        when(reader.isEmpty()).thenReturn(false);
        when(reader.current()).thenReturn('x');
        when(reader.matchesAny('\t', '\n', '\f', '<', '&')).thenReturn(false);
        when(reader.matchConsume("#")).thenReturn(true);
        when(reader.matchConsumeIgnoreCase("X")).thenReturn(false);
        when(reader.consumeDigitSequence()).thenReturn("65");
        when(reader.matchConsume(";")).thenReturn(false);
        // should still parse but add error
        Character result = tokeniser.consumeCharacterReference(null, false);
        assertEquals(Character.valueOf('A'), result);
        List<ParseError> errors = (List<ParseError>) getField(tokeniser, "errors");
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getErrorMessage().contains("Invalid character reference"));
    }

    @Test
    public void testConsumeCharacterReferenceNumberedInvalidCharvalSurrogate() {
        when(reader.isEmpty()).thenReturn(false);
        when(reader.current()).thenReturn('x');
        when(reader.matchesAny('\t', '\n', '\f', '<', '&')).thenReturn(false);
        when(reader.matchConsume("#")).thenReturn(true);
        when(reader.matchConsumeIgnoreCase("X")).thenReturn(false);
        when(reader.consumeDigitSequence()).thenReturn("55296"); // 0xD800
        when(reader.matchConsume(";")).thenReturn(true);
        Character result = tokeniser.consumeCharacterReference(null, false);
        assertEquals(Character.valueOf(Tokeniser.replacementChar), result);
        List<ParseError> errors = (List<ParseError>) getField(tokeniser, "errors");
        assertEquals(1, errors.size());
    }

    @Test
    public void testConsumeCharacterReferenceNumberedInvalidCharvalTooLarge() {
        when(reader.isEmpty()).thenReturn(false);
        when(reader.current()).thenReturn('x');
        when(reader.matchesAny('\t', '\n', '\f', '<', '&')).thenReturn(false);
        when(reader.matchConsume("#")).thenReturn(true);
        when(reader.matchConsumeIgnoreCase("X")).thenReturn(false);
        when(reader.consumeDigitSequence()).thenReturn("1114112"); // > 0x10FFFF
        when(reader.matchConsume(";")).thenReturn(true);
        Character result = tokeniser.consumeCharacterReference(null, false);
        assertEquals(Character.valueOf(Tokeniser.replacementChar), result);
    }

    @Test
    public void testConsumeCharacterReferenceNumberedNumberFormatException() {
        when(reader.isEmpty()).thenReturn(false);
        when(reader.current()).thenReturn('x');
        when(reader.matchesAny('\t', '\n', '\f', '<', '&')).thenReturn(false);
        when(reader.matchConsume("#")).thenReturn(true);
        when(reader.matchConsumeIgnoreCase("X")).thenReturn(false);
        when(reader.consumeDigitSequence()).thenReturn("99999999999999999999"); // too large for int
        when(reader.matchConsume(";")).thenReturn(true);
        // NumberFormatException caught, charval remains -1, then replacementChar returned
        Character result = tokeniser.consumeCharacterReference(null, false);
        assertEquals(Character.valueOf(Tokeniser.replacementChar), result);
        List<ParseError> errors = (List<ParseError>) getField(tokeniser, "errors");
        assertEquals(1, errors.size());
    }

    @Test
    public void testConsumeCharacterReferenceNamedFound() {
        when(reader.isEmpty()).thenReturn(false);
        when(reader.current()).thenReturn('x');
        when(reader.matchesAny('\t', '\n', '\f', '<', '&')).thenReturn(false);
        when(reader.matchConsume("#")).thenReturn(false);
        when(reader.consumeLetterSequence()).thenReturn("amp");
        when(reader.matches(';')).thenReturn(true);
        // Entities.isNamedEntity("amp") should be true in real implementation
        when(reader.matchConsume(";")).thenReturn(true);
        // Entities.getCharacterByName("amp") returns '&'
        Character result = tokeniser.consumeCharacterReference(null, false);
        assertEquals(Character.valueOf('&'), result);
    }

    @Test
    public void testConsumeCharacterReferenceNamedNotFoundWithSemicolon() {
        when(reader.isEmpty()).thenReturn(false);
        when(reader.current()).thenReturn('x');
        when(reader.matchesAny('\t', '\n', '\f', '<', '&')).thenReturn(false);
        when(reader.matchConsume("#")).thenReturn(false);
        when(reader.consumeLetterSequence()).thenReturn("unknown");
        when(reader.matches(';')).thenReturn(true);
        // simulate not found: loop will unconsume until empty, then not found
        // we need to mock unconsume and rewindToMark
        // We'll set up: after consumeLetterSequence, reader.matches(';') true, then while loop: isNamedEntity false, then nameRef substring, reader.unconsume(). We'll need to mock unconsume calls.
        // Simpler: we can use a real Entities class, but "unknown" is not an entity, so isNamedEntity returns false. We'll mock unconsume to do nothing.
        when(reader.matchConsume(";")).thenReturn(false); // not consumed because not found
        // Actually, after not found, it checks if looksLegit (true) -> characterReferenceError, then rewindToMark, return null.
        tokeniser.consumeCharacterReference(null, false);
        verify(reader).rewindToMark();
        List<ParseError> errors = (List<ParseError>) getField(tokeniser, "errors");
        assertEquals(1, errors.size());
    }

    @Test
    public void testConsumeCharacterReferenceNamedFoundButInAttributeFollowedByLetter() {
        when(reader.isEmpty()).thenReturn(false);
        when(reader.current()).thenReturn('x');
        when(reader.matchesAny('\t', '\n', '\f', '<', '&')).thenReturn(false);
        when(reader.matchConsume("#")).thenReturn(false);
        when(reader.consumeLetterSequence()).thenReturn("amp");
        when(reader.matches(';')).thenReturn(true);
        // found
        // inAttribute = true, and reader.matchesLetter() true
        when(reader.matchesLetter()).thenReturn(true);
        // should rewindToMark and return null
        assertNull(tokeniser.consumeCharacterReference(null, true));
        verify(reader).rewindToMark();
    }

    @Test
    public void testConsumeCharacterReferenceNamedFoundMissingSemicolon() {
        when(reader.isEmpty()).thenReturn(false);
        when(reader.current()).thenReturn('x');
        when(reader.matchesAny('\t', '\n', '\f', '<', '&')).thenReturn(false);
        when(reader.matchConsume("#")).thenReturn(false);
        when(reader.consumeLetterSequence()).thenReturn("amp");
        when(reader.matches(';')).thenReturn(true);
        when(reader.matchConsume(";")).thenReturn(false); // missing semicolon
        Character result = tokeniser.consumeCharacterReference(null, false);
        assertEquals(Character.valueOf('&'), result);
        List<ParseError> errors = (List<ParseError>) getField(tokeniser, "errors");
        assertEquals(1, errors.size());
    }

    @Test
    public void testCreateTagPendingStart() {
        Token.Tag tag = tokeniser.createTagPending(true);
        assertTrue(tag instanceof Token.StartTag);
        assertEquals(tag, getField(tokeniser, "tagPending"));
    }

    @Test
    public void testCreateTagPendingEnd() {
        Token.Tag tag = tokeniser.createTagPending(false);
        assertTrue(tag instanceof Token.EndTag);
    }

    @Test
    public void testEmitTagPending() throws Exception {
        Token.Tag tag = mock(Token.Tag.class);
        setField(tokeniser, "tagPending", tag);
        tokeniser.emitTagPending();
        verify(tag).finaliseTag();
        // emit should have been called with tag
        assertTrue((Boolean) getField(tokeniser, "isEmitPending"));
        assertEquals(tag, getField(tokeniser, "emitPending"));
    }

    @Test
    public void testCreateCommentPending() {
        tokeniser.createCommentPending();
        assertNotNull(getField(tokeniser, "commentPending"));
        assertTrue(getField(tokeniser, "commentPending") instanceof Token.Comment);
    }

    @Test
    public void testEmitCommentPending() throws Exception {
        Token.Comment comment = new Token.Comment();
        setField(tokeniser, "commentPending", comment);
        tokeniser.emitCommentPending();
        assertTrue((Boolean) getField(tokeniser, "isEmitPending"));
        assertEquals(comment, getField(tokeniser, "emitPending"));
    }

    @Test
    public void testCreateDoctypePending() {
        tokeniser.createDoctypePending();
        assertNotNull(getField(tokeniser, "doctypePending"));
        assertTrue(getField(tokeniser, "doctypePending") instanceof Token.Doctype);
    }

    @Test
    public void testEmitDoctypePending() throws Exception {
        Token.Doctype doctype = new Token.Doctype();
        setField(tokeniser, "doctypePending", doctype);
        tokeniser.emitDoctypePending();
        assertTrue((Boolean) getField(tokeniser, "isEmitPending"));
        assertEquals(doctype, getField(tokeniser, "emitPending"));
    }

    @Test
    public void testCreateTempBuffer() {
        tokeniser.createTempBuffer();
        assertNotNull(getField(tokeniser, "dataBuffer"));
    }

    @Test
    public void testIsAppropriateEndTagToken() throws Exception {
        Token.StartTag lastStart = new Token.StartTag();
        lastStart.tagName = "div";
        setField(tokeniser, "lastStartTag", lastStart);
        Token.EndTag endTag = new Token.EndTag();
        endTag.tagName = "div";
        setField(tokeniser, "tagPending", endTag);
        assertTrue(tokeniser.isAppropriateEndTagToken());
        endTag.tagName = "span";
        assertFalse(tokeniser.isAppropriateEndTagToken());
    }

    @Test
    public void testIsTrackErrors() {
        assertTrue(tokeniser.isTrackErrors()); // default true
    }

    @Test
    public void testSetTrackErrors() {
        tokeniser.setTrackErrors(false);
        assertFalse(tokeniser.isTrackErrors());
    }

    @Test
    public void testErrorState() {
        when(reader.current()).thenReturn('x');
        when(reader.pos()).thenReturn(10);
        tokeniser.error(TokeniserState.Data);
        List<ParseError> errors = (List<ParseError>) getField(tokeniser, "errors");
        assertEquals(1, errors.size());
        ParseError error = errors.get(0);
        assertEquals("Unexpected character in input", error.getErrorMessage());
        assertEquals(10, error.getPosition());
    }

    @Test
    public void testErrorStateTrackErrorsFalse() {
        tokeniser.setTrackErrors(false);
        tokeniser.error(TokeniserState.Data);
        List<ParseError> errors = (List<ParseError>) getField(tokeniser, "errors");
        assertEquals(0, errors.size());
    }

    @Test
    public void testEofError() {
        when(reader.pos()).thenReturn(20);
        tokeniser.eofError(TokeniserState.Data);
        List<ParseError> errors = (List<ParseError>) getField(tokeniser, "errors");
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getErrorMessage().contains("Unexpectedly reached end of file"));
    }

    @Test
    public void testCharacterReferenceError() throws Exception {
        when(reader.pos()).thenReturn(5);
        // call private method via reflection or indirectly via consumeCharacterReference
        // we can call consumeCharacterReference with empty digit sequence to trigger it
        when(reader.isEmpty()).thenReturn(false);
        when(reader.current()).thenReturn('x');
        when(reader.matchesAny('\t', '\n', '\f', '<', '&')).thenReturn(false);
        when(reader.matchConsume("#")).thenReturn(true);
        when(reader.matchConsumeIgnoreCase("X")).thenReturn(false);
        when(reader.consumeDigitSequence()).thenReturn("");
        tokeniser.consumeCharacterReference(null, false);
        List<ParseError> errors = (List<ParseError>) getField(tokeniser, "errors");
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getErrorMessage().contains("Invalid character reference"));
    }

    @Test
    public void testErrorString() throws Exception {
        tokeniser.error("Test error");
        List<ParseError> errors = (List<ParseError>) getField(tokeniser, "errors");
        assertEquals(1, errors.size());
        assertEquals("Test error", errors.get(0).getErrorMessage());
    }

    @Test
    public void testCurrentNodeInHtmlNS() {
        assertTrue(tokeniser.currentNodeInHtmlNS());
    }

    // Additional edge cases for read() loop: state.read is called repeatedly until isEmitPending true.
    @Test
    public void testReadLoopCallsStateReadUntilEmitPending() throws Exception {
        // set isEmitPending false initially
        setField(tokeniser, "isEmitPending", false);
        // mock state.read to set isEmitPending after a few calls
        doAnswer(new Answer() {
            int count = 0;
            public Object answer(InvocationOnMock invocation) throws Throwable {
                count++;
                if (count == 3) {
                    setField(tokeniser, "isEmitPending", true);
                }
                return null;
            }
        }).when(state).read(tokeniser, reader);
        // charBuffer empty
        StringBuilder charBuffer = (StringBuilder) getField(tokeniser, "charBuffer");
        charBuffer.setLength(0);
        Token token = mock(Token.class);
        setField(tokeniser, "emitPending", token);

        Token result = tokeniser.read();
        verify(state, times(3)).read(tokeniser, reader);
        assertEquals(token, result);
    }
}
