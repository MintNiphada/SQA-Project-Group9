package org.jsoup.parser;

import org.junit.Assert;
import org.junit.Test;

public class TokeniserTest {

    @Test
    public void testReadBasicTokens() {
        CharacterReader reader = new CharacterReader("<p>Hello &amp; World</p>");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Token t1 = tokeniser.read();
        Assert.assertEquals(Token.TokenType.StartTag, t1.type);
        Assert.assertEquals("p", ((Token.StartTag) t1).name());

        Token t2 = tokeniser.read();
        Assert.assertEquals(Token.TokenType.Character, t2.type);
        Assert.assertEquals("Hello & World", ((Token.Character) t2).getData());

        Token t3 = tokeniser.read();
        Assert.assertEquals(Token.TokenType.EndTag, t3.type);
        Assert.assertEquals("p", ((Token.EndTag) t3).name());

        Token t4 = tokeniser.read();
        Assert.assertEquals(Token.TokenType.EOF, t4.type);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testSelfClosingFlagUnacknowledgedError() {
        CharacterReader reader = new CharacterReader("<img/><p>");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Token t1 = tokeniser.read();
        Assert.assertEquals(Token.TokenType.StartTag, t1.type);
        Assert.assertTrue(((Token.StartTag) t1).isSelfClosing());

        Token t2 = tokeniser.read();
        Assert.assertEquals(Token.TokenType.StartTag, t2.type);
        Assert.assertTrue(errors.size() >= 1);
    }

    @Test
    public void testAcknowledgeSelfClosingFlag() {
        CharacterReader reader = new CharacterReader("<img/>");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Token t1 = tokeniser.read();
        Assert.assertTrue(((Token.StartTag) t1).isSelfClosing());
        tokeniser.acknowledgeSelfClosingFlag();

        tokeniser.read();
        Assert.assertEquals(0, errors.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmitDoublePendingThrows() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        tokeniser.emit(new Token.Character().data("a"));
        tokeniser.emit(new Token.Character().data("b"));
    }

    @Test
    public void testEmitEndTagWithAttributesError() {
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

    @Test
    public void testEmitStringAndCharBufferMerging() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        tokeniser.emit("One");
        tokeniser.emit("Two");
        tokeniser.emit("Three");
        tokeniser.emit(new char[]{' ', 'F', 'o', 'u', 'r'});
        tokeniser.emit(new int[]{0x20, 0x35});
        tokeniser.emit('!');

        Token t = tokeniser.read();
        Assert.assertEquals(Token.TokenType.Character, t.type);
        Assert.assertEquals("OneTwoThree Four 5!", ((Token.Character) t).getData());
    }

    @Test
    public void testEmitSingleString() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        tokeniser.emit("Single");
        Token t = tokeniser.read();
        Assert.assertEquals(Token.TokenType.Character, t.type);
        Assert.assertEquals("Single", ((Token.Character) t).getData());
    }

    @Test
    public void testEmitCharAndIntArrayVariants() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        tokeniser.emit(new char[]{'a', 'b'});
        Token t1 = tokeniser.read();
        Assert.assertEquals("ab", ((Token.Character) t1).getData());

        tokeniser.emit(new int[]{65, 66});
        Token t2 = tokeniser.read();
        Assert.assertEquals("AB", ((Token.Character) t2).getData());

        tokeniser.emit('z');
        Token t3 = tokeniser.read();
        Assert.assertEquals("z", ((Token.Character) t3).getData());
    }

    @Test
    public void testStateTransitions() {
        CharacterReader reader = new CharacterReader("abc");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        Assert.assertEquals(TokeniserState.Data, tokeniser.getState());
        tokeniser.transition(TokeniserState.TagOpen);
        Assert.assertEquals(TokeniserState.TagOpen, tokeniser.getState());

        tokeniser.advanceTransition(TokeniserState.TagName);
        Assert.assertEquals(TokeniserState.TagName, tokeniser.getState());
        Assert.assertEquals('b', reader.current());
    }

    @Test
    public void testTagPendingCreationAndAppropriateEndTag() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        Assert.assertNull(tokeniser.appropriateEndTagName());
        Assert.assertFalse(tokeniser.isAppropriateEndTagToken());

        Token.Tag startTag = tokeniser.createTagPending(true);
        startTag.name("title");
        tokeniser.emitTagPending();

        Assert.assertEquals("title", tokeniser.appropriateEndTagName());

        Token.Tag endTag = tokeniser.createTagPending(false);
        endTag.name("title");
        Assert.assertTrue(tokeniser.isAppropriateEndTagToken());

        endTag.name("div");
        Assert.assertFalse(tokeniser.isAppropriateEndTagToken());
    }

    @Test
    public void testCommentAndDoctypePending() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        tokeniser.createCommentPending();
        tokeniser.commentPending.data.append("comment test");
        tokeniser.emitCommentPending();
        Token t1 = tokeniser.read();
        Assert.assertEquals(Token.TokenType.Comment, t1.type);
        Assert.assertEquals("comment test", ((Token.Comment) t1).getData());

        tokeniser.createDoctypePending();
        tokeniser.doctypePending.name.append("html");
        tokeniser.emitDoctypePending();
        Token t2 = tokeniser.read();
        Assert.assertEquals(Token.TokenType.Doctype, t2.type);
        Assert.assertEquals("html", ((Token.Doctype) t2).getName());
    }

    @Test
    public void testCreateTempBuffer() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        tokeniser.dataBuffer.append("temp-data");
        Assert.assertEquals("temp-data", tokeniser.dataBuffer.toString());
        tokeniser.createTempBuffer();
        Assert.assertEquals(0, tokeniser.dataBuffer.length());
    }

    @Test
    public void testConsumeCharacterReferenceEmptyAndSkippedChars() {
        ParseErrorList errors = ParseErrorList.tracking(10);

        CharacterReader reader1 = new CharacterReader("");
        Tokeniser tokeniser1 = new Tokeniser(reader1, errors);
        Assert.assertNull(tokeniser1.consumeCharacterReference(null, false));

        CharacterReader reader2 = new CharacterReader("abc");
        Tokeniser tokeniser2 = new Tokeniser(reader2, errors);
        Assert.assertNull(tokeniser2.consumeCharacterReference('a', false));

        CharacterReader reader3 = new CharacterReader(" <& \t\n\r\f");
        Tokeniser tokeniser3 = new Tokeniser(reader3, errors);
        Assert.assertNull(tokeniser3.consumeCharacterReference(null, false));
    }

    @Test
    public void testConsumeCharacterReferenceDecimal() {
        ParseErrorList errors = ParseErrorList.tracking(10);
        CharacterReader reader = new CharacterReader("#65;rest");
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        int[] ref = tokeniser.consumeCharacterReference(null, false);
        Assert.assertNotNull(ref);
        Assert.assertEquals(65, ref[0]);
        Assert.assertEquals(0, errors.size());
        Assert.assertEquals('r', reader.current());
    }

    @Test
    public void testConsumeCharacterReferenceHex() {
        ParseErrorList errors = ParseErrorList.tracking(10);
        CharacterReader reader = new CharacterReader("#x41;rest");
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        int[] ref = tokeniser.consumeCharacterReference(null, false);
        Assert.assertNotNull(ref);
        Assert.assertEquals(65, ref[0]);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testConsumeCharacterReferenceHexUppercase() {
        ParseErrorList errors = ParseErrorList.tracking(10);
        CharacterReader reader = new CharacterReader("#X41;");
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        int[] ref = tokeniser.consumeCharacterReference(null, false);
        Assert.assertNotNull(ref);
        Assert.assertEquals(65, ref[0]);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testConsumeCharacterReferenceNoNumerals() {
        ParseErrorList errors = ParseErrorList.tracking(10);
        CharacterReader reader = new CharacterReader("#;");
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        int[] ref = tokeniser.consumeCharacterReference(null, false);
        Assert.assertNull(ref);
        Assert.assertEquals(1, errors.size());
        Assert.assertEquals('#', reader.current());
    }

    @Test
    public void testConsumeCharacterReferenceMissingSemicolon() {
        ParseErrorList errors = ParseErrorList.tracking(10);
        CharacterReader reader = new CharacterReader("#65x");
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        int[] ref = tokeniser.consumeCharacterReference(null, false);
        Assert.assertNotNull(ref);
        Assert.assertEquals(65, ref[0]);
        Assert.assertEquals(1, errors.size());
        Assert.assertEquals('x', reader.current());
    }

    @Test
    public void testConsumeCharacterReferenceInvalidCodePoints() {
        ParseErrorList errors = ParseErrorList.tracking(10);

        CharacterReader readerSurrogate = new CharacterReader("#xD800;");
        Tokeniser tokeniserSurrogate = new Tokeniser(readerSurrogate, errors);
        int[] ref1 = tokeniserSurrogate.consumeCharacterReference(null, false);
        Assert.assertNotNull(ref1);
        Assert.assertEquals(Tokeniser.replacementChar, (char) ref1[0]);

        CharacterReader readerOutOfRange = new CharacterReader("#x110000;");
        Tokeniser tokeniserOutOfRange = new Tokeniser(readerOutOfRange, errors);
        int[] ref2 = tokeniserOutOfRange.consumeCharacterReference(null, false);
        Assert.assertNotNull(ref2);
        Assert.assertEquals(Tokeniser.replacementChar, (char) ref2[0]);
    }

    @Test
    public void testConsumeCharacterReferenceNamedValidAndBase() {
        ParseErrorList errors = ParseErrorList.tracking(10);

        CharacterReader reader1 = new CharacterReader("lt;");
        Tokeniser tokeniser1 = new Tokeniser(reader1, errors);
        int[] ref1 = tokeniser1.consumeCharacterReference(null, false);
        Assert.assertNotNull(ref1);
        Assert.assertEquals('<', (char) ref1[0]);

        CharacterReader reader2 = new CharacterReader("lt");
        Tokeniser tokeniser2 = new Tokeniser(reader2, errors);
        int[] ref2 = tokeniser2.consumeCharacterReference(null, false);
        Assert.assertNotNull(ref2);
        Assert.assertEquals('<', (char) ref2[0]);
    }

    @Test
    public void testConsumeCharacterReferenceNamedInvalid() {
        ParseErrorList errors = ParseErrorList.tracking(10);
        CharacterReader reader = new CharacterReader("notAnEntity;");
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        int[] ref = tokeniser.consumeCharacterReference(null, false);
        Assert.assertNull(ref);
        Assert.assertEquals(1, errors.size());
        Assert.assertEquals('n', reader.current());
    }

    @Test
    public void testConsumeCharacterReferenceInAttributeHandling() {
        ParseErrorList errors = ParseErrorList.tracking(10);
        CharacterReader reader = new CharacterReader("amp=foo");
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        int[] ref = tokeniser.consumeCharacterReference(null, true);
        Assert.assertNull(ref);
        Assert.assertEquals('a', reader.current());
    }

    @Test
    public void testErrorTrackingMethods() {
        ParseErrorList errors = ParseErrorList.tracking(10);
        CharacterReader reader = new CharacterReader("abc");
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        tokeniser.error(TokeniserState.Data);
        tokeniser.eofError(TokeniserState.Data);
        tokeniser.error("Custom error");

        Assert.assertEquals(3, errors.size());
    }

    @Test
    public void testCurrentNodeInHtmlNS() {
        Tokeniser tokeniser = new Tokeniser(new CharacterReader(""), ParseErrorList.noTracking());
        Assert.assertTrue(tokeniser.currentNodeInHtmlNS());
    }

    @Test
    public void testUnescapeEntities() {
        Tokeniser tokeniser = new Tokeniser(new CharacterReader("Hello &amp; &lt;&gt; &notin; &#65; &notAnEntity;"), ParseErrorList.noTracking());
        String unescaped = tokeniser.unescapeEntities(false);
        Assert.assertTrue(unescaped.startsWith("Hello & <>"));
        Assert.assertTrue(unescaped.contains("A"));
    }

    @Test
    public void testUnescapeEntitiesInAttribute() {
        Tokeniser tokeniser = new Tokeniser(new CharacterReader("key=&amp&val=1"), ParseErrorList.noTracking());
        String unescaped = tokeniser.unescapeEntities(true);
        Assert.assertEquals("key=&amp&val=1", unescaped);
    }
}
