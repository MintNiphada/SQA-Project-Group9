package org.jsoup.parser;

import org.jsoup.helper.Validate;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;

public class TokeniserTest {

    @Test
    public void testReadBasicData() {
        CharacterReader reader = new CharacterReader("Hello world");
        Tokeniser tokeniser = new Tokeniser(reader);

        Token token = tokeniser.read();
        Assert.assertNotNull(token);
        Assert.assertTrue(token.isCharacter());
        Assert.assertEquals("Hello world", token.asCharacter().getData());

        Token eofToken = tokeniser.read();
        Assert.assertNotNull(eofToken);
        Assert.assertTrue(eofToken.isEOF());
    }

    @Test
    public void testReadStartTag() {
        CharacterReader reader = new CharacterReader("<div>");
        Tokeniser tokeniser = new Tokeniser(reader);

        Token token = tokeniser.read();
        Assert.assertNotNull(token);
        Assert.assertTrue(token.isStartTag());
        Assert.assertEquals("div", token.asStartTag().name());

        Token eofToken = tokeniser.read();
        Assert.assertTrue(eofToken.isEOF());
    }

    @Test
    public void testReadEndTag() {
        CharacterReader reader = new CharacterReader("</div>");
        Tokeniser tokeniser = new Tokeniser(reader);

        Token token = tokeniser.read();
        Assert.assertNotNull(token);
        Assert.assertTrue(token.isEndTag());
        Assert.assertEquals("div", token.asEndTag().name());
    }

    @Test
    public void testReadSelfClosingTagAcknowledged() {
        CharacterReader reader = new CharacterReader("<img />");
        Tokeniser tokeniser = new Tokeniser(reader);

        Token token = tokeniser.read();
        Assert.assertTrue(token.isStartTag());
        Assert.assertTrue(token.asStartTag().isSelfClosing());

        tokeniser.acknowledgeSelfClosingFlag();

        Token eofToken = tokeniser.read();
        Assert.assertTrue(eofToken.isEOF());
    }

    @Test
    public void testSelfClosingFlagNotAcknowledgedGeneratesError() {
        CharacterReader reader = new CharacterReader("<img />");
        Tokeniser tokeniser = new Tokeniser(reader);

        Token token = tokeniser.read();
        Assert.assertTrue(token.isStartTag());
        Assert.assertTrue(token.asStartTag().isSelfClosing());

        // Do not acknowledge self-closing flag and read next token
        Token eofToken = tokeniser.read();
        Assert.assertTrue(eofToken.isEOF());

        List<ParseError> errors = getErrors(tokeniser);
        boolean foundError = false;
        for (ParseError error : errors) {
            if (error.getErrorMessage().contains("Self closing flag not acknowledged")) {
                foundError = true;
                break;
            }
        }
        Assert.assertTrue(foundError);
    }

    @Test
    public void testEmitPendingValidation() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader);

        Token.StartTag tag1 = new Token.StartTag();
        tag1.nameAttr("div", new org.jsoup.nodes.Attributes());
        tokeniser.emit(tag1);

        try {
            Token.StartTag tag2 = new Token.StartTag();
            tag2.nameAttr("p", new org.jsoup.nodes.Attributes());
            tokeniser.emit(tag2);
            Assert.fail("Expected IllegalArgumentException on emitting token while isEmitPending is true");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("There is an unread token pending"));
        }
    }

    @Test
    public void testEndTagWithAttributesGeneratesError() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader);

        Token.EndTag endTag = new Token.EndTag();
        endTag.name("div");
        endTag.attributes.put("class", "foo");

        tokeniser.emit(endTag);

        List<ParseError> errors = getErrors(tokeniser);
        boolean foundError = false;
        for (ParseError error : errors) {
            if (error.getErrorMessage().contains("Attributes incorrectly present on end tag")) {
                foundError = true;
                break;
            }
        }
        Assert.assertTrue(foundError);
    }

    @Test
    public void testEmitStringAndCharBufferDraining() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader);

        tokeniser.emit("Hello");
        tokeniser.emit(' ');
        tokeniser.emit("World");

        Token.StartTag tag = new Token.StartTag();
        tag.nameAttr("div", new org.jsoup.nodes.Attributes());
        tokeniser.emit(tag);

        // First read should return character buffer content
        Token charToken = tokeniser.read();
        Assert.assertTrue(charToken.isCharacter());
        Assert.assertEquals("Hello World", charToken.asCharacter().getData());

        // Next read should return the pending tag token
        Token tagToken = tokeniser.read();
        Assert.assertTrue(tagToken.isStartTag());
        Assert.assertEquals("div", tagToken.asStartTag().name());
    }

    @Test
    public void testStateAndTransition() {
        CharacterReader reader = new CharacterReader("abc");
        Tokeniser tokeniser = new Tokeniser(reader);

        Assert.assertEquals(TokeniserState.Data, tokeniser.getState());

        tokeniser.transition(TokeniserState.TagOpen);
        Assert.assertEquals(TokeniserState.TagOpen, tokeniser.getState());

        tokeniser.advanceTransition(TokeniserState.TagName);
        Assert.assertEquals(TokeniserState.TagName, tokeniser.getState());
        Assert.assertEquals('b', reader.current());
    }

    @Test
    public void testCreateAndEmitTagPending() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader);

        Token.Tag startTag = tokeniser.createTagPending(true);
        Assert.assertNotNull(startTag);
        Assert.assertTrue(startTag instanceof Token.StartTag);
        startTag.tagName = "span";

        tokeniser.emitTagPending();
        Token token = tokeniser.read();
        Assert.assertTrue(token.isStartTag());
        Assert.assertEquals("span", token.asStartTag().name());

        Token.Tag endTag = tokeniser.createTagPending(false);
        Assert.assertNotNull(endTag);
        Assert.assertTrue(endTag instanceof Token.EndTag);
        endTag.tagName = "span";

        tokeniser.emitTagPending();
        Token token2 = tokeniser.read();
        Assert.assertTrue(token2.isEndTag());
        Assert.assertEquals("span", token2.asEndTag().name());
    }

    @Test
    public void testCreateAndEmitCommentPending() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader);

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
        Tokeniser tokeniser = new Tokeniser(reader);

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
        Tokeniser tokeniser = new Tokeniser(reader);

        Assert.assertNull(tokeniser.dataBuffer);
        tokeniser.createTempBuffer();
        Assert.assertNotNull(tokeniser.dataBuffer);
        tokeniser.dataBuffer.append("temp");
        Assert.assertEquals("temp", tokeniser.dataBuffer.toString());
    }

    @Test
    public void testIsAppropriateEndTagToken() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader);

        Token.StartTag startTag = new Token.StartTag();
        startTag.nameAttr("script", new org.jsoup.nodes.Attributes());
        tokeniser.emit(startTag);
        tokeniser.read();

        tokeniser.createTagPending(false);
        tokeniser.tagPending.tagName = "script";
        Assert.assertTrue(tokeniser.isAppropriateEndTagToken());

        tokeniser.tagPending.tagName = "style";
        Assert.assertFalse(tokeniser.isAppropriateEndTagToken());
    }

    @Test
    public void testTrackErrorsFlag() {
        CharacterReader reader = new CharacterReader("a");
        Tokeniser tokeniser = new Tokeniser(reader);

        Assert.assertTrue(tokeniser.isTrackErrors());

        tokeniser.setTrackErrors(false);
        Assert.assertFalse(tokeniser.isTrackErrors());

        tokeniser.error(TokeniserState.Data);
        tokeniser.eofError(TokeniserState.Data);
        Assert.assertTrue(getErrors(tokeniser).isEmpty());

        tokeniser.setTrackErrors(true);
        tokeniser.error(TokeniserState.Data);
        tokeniser.eofError(TokeniserState.Data);
        Assert.assertEquals(2, getErrors(tokeniser).size());
    }

    @Test
    public void testCurrentNodeInHtmlNS() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader);
        Assert.assertTrue(tokeniser.currentNodeInHtmlNS());
    }

    @Test
    public void testConsumeCharacterReferenceEmptyReader() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader);

        Character ref = tokeniser.consumeCharacterReference(null, false);
        Assert.assertNull(ref);
    }

    @Test
    public void testConsumeCharacterReferenceAdditionalAllowed() {
        CharacterReader reader = new CharacterReader("\"abc");
        Tokeniser tokeniser = new Tokeniser(reader);

        Character ref = tokeniser.consumeCharacterReference('\"', true);
        Assert.assertNull(ref);
        Assert.assertEquals('\"', reader.current());
    }

    @Test
    public void testConsumeCharacterReferenceSpecialChars() {
        char[] specialChars = new char[]{'\t', '\n', '\f', '<', '&'};
        for (char c : specialChars) {
            CharacterReader reader = new CharacterReader(String.valueOf(c));
            Tokeniser tokeniser = new Tokeniser(reader);
            Character ref = tokeniser.consumeCharacterReference(null, false);
            Assert.assertNull(ref);
        }
    }

    @Test
    public void testConsumeCharacterReferenceDecimal() {
        CharacterReader reader = new CharacterReader("#65;rest");
        Tokeniser tokeniser = new Tokeniser(reader);

        Character ref = tokeniser.consumeCharacterReference(null, false);
        Assert.assertNotNull(ref);
        Assert.assertEquals(Character.valueOf('A'), ref);
        Assert.assertEquals('r', reader.current());
    }

    @Test
    public void testConsumeCharacterReferenceHex() {
        CharacterReader reader = new CharacterReader("#x41;rest");
        Tokeniser tokeniser = new Tokeniser(reader);

        Character ref = tokeniser.consumeCharacterReference(null, false);
        Assert.assertNotNull(ref);
        Assert.assertEquals(Character.valueOf('A'), ref);
        Assert.assertEquals('r', reader.current());
    }

    @Test
    public void testConsumeCharacterReferenceHexUppercase() {
        CharacterReader reader = new CharacterReader("#X41;rest");
        Tokeniser tokeniser = new Tokeniser(reader);

        Character ref = tokeniser.consumeCharacterReference(null, false);
        Assert.assertNotNull(ref);
        Assert.assertEquals(Character.valueOf('A'), ref);
        Assert.assertEquals('r', reader.current());
    }

    @Test
    public void testConsumeCharacterReferenceNoSemicolon() {
        CharacterReader reader = new CharacterReader("#65 rest");
        Tokeniser tokeniser = new Tokeniser(reader);

        Character ref = tokeniser.consumeCharacterReference(null, false);
        Assert.assertNotNull(ref);
        Assert.assertEquals(Character.valueOf('A'), ref);
        Assert.assertEquals(' ', reader.current());
        Assert.assertFalse(getErrors(tokeniser).isEmpty());
    }

    @Test
    public void testConsumeCharacterReferenceInvalidNumber() {
        CharacterReader reader = new CharacterReader("#xyz;");
        Tokeniser tokeniser = new Tokeniser(reader);

        Character ref = tokeniser.consumeCharacterReference(null, false);
        Assert.assertNull(ref);
        Assert.assertEquals('#', reader.current());
    }

    @Test
    public void testConsumeCharacterReferenceSurrogateRange() {
        // Surrogate range 0xD800 - 0xDFFF -> replacement char
        CharacterReader reader = new CharacterReader("#xD800;");
        Tokeniser tokeniser = new Tokeniser(reader);

        Character ref = tokeniser.consumeCharacterReference(null, false);
        Assert.assertNotNull(ref);
        Assert.assertEquals(Character.valueOf(Tokeniser.replacementChar), ref);

        CharacterReader reader2 = new CharacterReader("#xDFFF;");
        Tokeniser tokeniser2 = new Tokeniser(reader2);
        Character ref2 = tokeniser2.consumeCharacterReference(null, false);
        Assert.assertNotNull(ref2);
        Assert.assertEquals(Character.valueOf(Tokeniser.replacementChar), ref2);
    }

    @Test
    public void testConsumeCharacterReferenceOutOfBounds() {
        CharacterReader reader = new CharacterReader("#x110000;");
        Tokeniser tokeniser = new Tokeniser(reader);

        Character ref = tokeniser.consumeCharacterReference(null, false);
        Assert.assertNotNull(ref);
        Assert.assertEquals(Character.valueOf(Tokeniser.replacementChar), ref);
    }

    @Test
    public void testConsumeCharacterReferenceNamed() {
        CharacterReader reader = new CharacterReader("lt;rest");
        Tokeniser tokeniser = new Tokeniser(reader);

        Character ref = tokeniser.consumeCharacterReference(null, false);
        Assert.assertNotNull(ref);
        Assert.assertEquals(Character.valueOf('<'), ref);
        Assert.assertEquals('r', reader.current());
    }

    @Test
    public void testConsumeCharacterReferenceNamedNoSemicolon() {
        CharacterReader reader = new CharacterReader("gt rest");
        Tokeniser tokeniser = new Tokeniser(reader);

        Character ref = tokeniser.consumeCharacterReference(null, false);
        Assert.assertNotNull(ref);
        Assert.assertEquals(Character.valueOf('>'), ref);
        Assert.assertEquals(' ', reader.current());
        Assert.assertFalse(getErrors(tokeniser).isEmpty());
    }

    @Test
    public void testConsumeCharacterReferenceNamedInvalid() {
        CharacterReader reader = new CharacterReader("notanentity;rest");
        Tokeniser tokeniser = new Tokeniser(reader);

        Character ref = tokeniser.consumeCharacterReference(null, false);
        Assert.assertNull(ref);
        Assert.assertEquals('n', reader.current());
    }

    @Test
    public void testConsumeCharacterReferenceInAttributeWithFollowingLetterOrDigitOrEqual() {
        CharacterReader reader1 = new CharacterReader("lt=value");
        Tokeniser tokeniser1 = new Tokeniser(reader1);
        Character ref1 = tokeniser1.consumeCharacterReference(null, true);
        Assert.assertNull(ref1);
        Assert.assertEquals('l', reader1.current());

        CharacterReader reader2 = new CharacterReader("lta");
        Tokeniser tokeniser2 = new Tokeniser(reader2);
        Character ref2 = tokeniser2.consumeCharacterReference(null, true);
        Assert.assertNull(ref2);
        Assert.assertEquals('l', reader2.current());

        CharacterReader reader3 = new CharacterReader("lt1");
        Tokeniser tokeniser3 = new Tokeniser(reader3);
        Character ref3 = tokeniser3.consumeCharacterReference(null, true);
        Assert.assertNull(ref3);
        Assert.assertEquals('l', reader3.current());
    }

    @Test
    public void testConsumeCharacterReferencePrefixMatching() {
        CharacterReader reader = new CharacterReader("notin;");
        Tokeniser tokeniser = new Tokeniser(reader);

        Character ref = tokeniser.consumeCharacterReference(null, false);
        Assert.assertNotNull(ref);
        // "notin" may resolve to entity or prefix "not", verify entity match behavior
        Assert.assertTrue(ref > 0);
    }

    @SuppressWarnings("unchecked")
    private List<ParseError> getErrors(Tokeniser tokeniser) {
        try {
            Field field = Tokeniser.class.getDeclaredField("errors");
            field.setAccessible(true);
            return (List<ParseError>) field.get(tokeniser);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
