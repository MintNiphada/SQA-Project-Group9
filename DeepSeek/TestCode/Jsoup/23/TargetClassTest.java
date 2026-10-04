package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class CharacterReaderTest {

    @Test(expected = IllegalArgumentException.class)
    public void constructorNullInputThrowsException() {
        new CharacterReader(null);
    }

    @Test
    public void constructorNormalizesCarriageReturnNewline() {
        CharacterReader reader = new CharacterReader("a\r\nb");
        assertEquals("a\nb", reader.toString());
    }

    @Test
    public void constructorNormalizesCarriageReturn() {
        CharacterReader reader = new CharacterReader("a\rb");
        assertEquals("a\nb", reader.toString());
    }

    @Test
    public void constructorKeepsNewline() {
        CharacterReader reader = new CharacterReader("a\nb");
        assertEquals("a\nb", reader.toString());
    }

    @Test
    public void constructorEmptyString() {
        CharacterReader reader = new CharacterReader("");
        assertTrue(reader.isEmpty());
        assertEquals(0, reader.pos());
    }

    @Test
    public void posReturnsCurrentPosition() {
        CharacterReader reader = new CharacterReader("abc");
        assertEquals(0, reader.pos());
        reader.advance();
        assertEquals(1, reader.pos());
    }

    @Test
    public void isEmptyWhenPosAtLength() {
        CharacterReader reader = new CharacterReader("a");
        assertFalse(reader.isEmpty());
        reader.consume();
        assertTrue(reader.isEmpty());
    }

    @Test
    public void isEmptyWhenEmptyInput() {
        CharacterReader reader = new CharacterReader("");
        assertTrue(reader.isEmpty());
    }

    @Test
    public void currentReturnsCharAtPos() {
        CharacterReader reader = new CharacterReader("abc");
        assertEquals('a', reader.current());
        reader.advance();
        assertEquals('b', reader.current());
    }

    @Test
    public void currentReturnsEOFWhenEmpty() {
        CharacterReader reader = new CharacterReader("");
        assertEquals(CharacterReader.EOF, reader.current());
    }

    @Test
    public void consumeReturnsCharAndAdvances() {
        CharacterReader reader = new CharacterReader("abc");
        assertEquals('a', reader.consume());
        assertEquals(1, reader.pos());
        assertEquals('b', reader.consume());
        assertEquals(2, reader.pos());
    }

    @Test
    public void consumeReturnsEOFWhenEmpty() {
        CharacterReader reader = new CharacterReader("");
        assertEquals(CharacterReader.EOF, reader.consume());
        assertEquals(1, reader.pos());
    }

    @Test
    public void unconsumeDecrementsPos() {
        CharacterReader reader = new CharacterReader("abc");
        reader.advance();
        reader.unconsume();
        assertEquals(0, reader.pos());
    }

    @Test
    public void unconsumeCanMakePosNegative() {
        CharacterReader reader = new CharacterReader("");
        reader.unconsume();
        assertEquals(-1, reader.pos());
        assertFalse(reader.isEmpty());
    }

    @Test
    public void advanceIncrementsPos() {
        CharacterReader reader = new CharacterReader("abc");
        reader.advance();
        assertEquals(1, reader.pos());
    }

    @Test
    public void markAndRewindToMark() {
        CharacterReader reader = new CharacterReader("abc");
        reader.advance();
        reader.mark();
        reader.advance();
        reader.rewindToMark();
        assertEquals(1, reader.pos());
    }

    @Test
    public void consumeAsStringReturnsEmptyAndAdvances() {
        CharacterReader reader = new CharacterReader("abc");
        String result = reader.consumeAsString();
        assertEquals("", result);
        assertEquals(1, reader.pos());
    }

    @Test
    public void consumeToCharFound() {
        CharacterReader reader = new CharacterReader("abcde");
        String consumed = reader.consumeTo('c');
        assertEquals("ab", consumed);
        assertEquals(2, reader.pos());
        assertEquals('c', reader.current());
    }

    @Test
    public void consumeToCharNotFound() {
        CharacterReader reader = new CharacterReader("abc");
        String consumed = reader.consumeTo('z');
        assertEquals("abc", consumed);
        assertTrue(reader.isEmpty());
    }

    @Test
    public void consumeToCharEmptyInput() {
        CharacterReader reader = new CharacterReader("");
        String consumed = reader.consumeTo('a');
        assertEquals("", consumed);
        assertTrue(reader.isEmpty());
    }

    @Test
    public void consumeToStringFound() {
        CharacterReader reader = new CharacterReader("abcde");
        String consumed = reader.consumeTo("cd");
        assertEquals("ab", consumed);
        assertEquals(2, reader.pos());
    }

    @Test
    public void consumeToStringNotFound() {
        CharacterReader reader = new CharacterReader("abc");
        String consumed = reader.consumeTo("xyz");
        assertEquals("abc", consumed);
        assertTrue(reader.isEmpty());
    }

    @Test
    public void consumeToAnyFound() {
        CharacterReader reader = new CharacterReader("abcde");
        String consumed = reader.consumeToAny('c', 'e');
        assertEquals("ab", consumed);
        assertEquals(2, reader.pos());
    }

    @Test
    public void consumeToAnyNotFound() {
        CharacterReader reader = new CharacterReader("abc");
        String consumed = reader.consumeToAny('x', 'y');
        assertEquals("abc", consumed);
        assertTrue(reader.isEmpty());
    }

    @Test
    public void consumeToAnyEmptyInput() {
        CharacterReader reader = new CharacterReader("");
        String consumed = reader.consumeToAny('a');
        assertEquals("", consumed);
    }

    @Test
    public void consumeToAnyImmediateMatch() {
        CharacterReader reader = new CharacterReader("abc");
        String consumed = reader.consumeToAny('a');
        assertEquals("", consumed);
        assertEquals(0, reader.pos());
    }

    @Test
    public void consumeToEnd() {
        CharacterReader reader = new CharacterReader("abc");
        reader.advance();
        String remaining = reader.consumeToEnd();
        assertEquals("bc", remaining);
        assertTrue(reader.isEmpty());
    }

    @Test
    public void consumeToEndEmpty() {
        CharacterReader reader = new CharacterReader("");
        String remaining = reader.consumeToEnd();
        assertEquals("", remaining);
    }

    @Test
    public void consumeLetterSequence() {
        CharacterReader reader = new CharacterReader("abc123");
        String letters = reader.consumeLetterSequence();
        assertEquals("abc", letters);
        assertEquals(3, reader.pos());
    }

    @Test
    public void consumeLetterSequenceNoLetters() {
        CharacterReader reader = new CharacterReader("123");
        String letters = reader.consumeLetterSequence();
        assertEquals("", letters);
        assertEquals(0, reader.pos());
    }

    @Test
    public void consumeLetterSequenceEmpty() {
        CharacterReader reader = new CharacterReader("");
        String letters = reader.consumeLetterSequence();
        assertEquals("", letters);
    }

    @Test
    public void consumeHexSequence() {
        CharacterReader reader = new CharacterReader("1aFg");
        String hex = reader.consumeHexSequence();
        assertEquals("1aF", hex);
        assertEquals(3, reader.pos());
    }

    @Test
    public void consumeHexSequenceNoHex() {
        CharacterReader reader = new CharacterReader("xyz");
        String hex = reader.consumeHexSequence();
        assertEquals("", hex);
    }

    @Test
    public void consumeDigitSequence() {
        CharacterReader reader = new CharacterReader("123abc");
        String digits = reader.consumeDigitSequence();
        assertEquals("123", digits);
        assertEquals(3, reader.pos());
    }

    @Test
    public void consumeDigitSequenceNoDigits() {
        CharacterReader reader = new CharacterReader("abc");
        String digits = reader.consumeDigitSequence();
        assertEquals("", digits);
    }

    @Test
    public void matchesCharTrue() {
        CharacterReader reader = new CharacterReader("abc");
        assertTrue(reader.matches('a'));
    }

    @Test
    public void matchesCharFalse() {
        CharacterReader reader = new CharacterReader("abc");
        assertFalse(reader.matches('b'));
    }

    @Test
    public void matchesCharEmpty() {
        CharacterReader reader = new CharacterReader("");
        assertFalse(reader.matches('a'));
    }

    @Test
    public void matchesStringTrue() {
        CharacterReader reader = new CharacterReader("abc");
        assertTrue(reader.matches("ab"));
    }

    @Test
    public void matchesStringFalse() {
        CharacterReader reader = new CharacterReader("abc");
        assertFalse(reader.matches("bc"));
    }

    @Test
    public void matchesIgnoreCaseTrue() {
        CharacterReader reader = new CharacterReader("Abc");
        assertTrue(reader.matchesIgnoreCase("aB"));
    }

    @Test
    public void matchesIgnoreCaseFalse() {
        CharacterReader reader = new CharacterReader("abc");
        assertFalse(reader.matchesIgnoreCase("abC"));
    }

    @Test
    public void matchesAnyTrue() {
        CharacterReader reader = new CharacterReader("abc");
        assertTrue(reader.matchesAny('b', 'a'));
    }

    @Test
    public void matchesAnyFalse() {
        CharacterReader reader = new CharacterReader("abc");
        assertFalse(reader.matchesAny('x', 'y'));
    }

    @Test
    public void matchesAnyEmpty() {
        CharacterReader reader = new CharacterReader("");
        assertFalse(reader.matchesAny('a'));
    }

    @Test
    public void matchesLetterTrue() {
        CharacterReader reader = new CharacterReader("a1");
        assertTrue(reader.matchesLetter());
    }

    @Test
    public void matchesLetterFalse() {
        CharacterReader reader = new CharacterReader("1a");
        assertFalse(reader.matchesLetter());
    }

    @Test
    public void matchesLetterEmpty() {
        CharacterReader reader = new CharacterReader("");
        assertFalse(reader.matchesLetter());
    }

    @Test
    public void matchesDigitTrue() {
        CharacterReader reader = new CharacterReader("1a");
        assertTrue(reader.matchesDigit());
    }

    @Test
    public void matchesDigitFalse() {
        CharacterReader reader = new CharacterReader("a1");
        assertFalse(reader.matchesDigit());
    }

    @Test
    public void matchesDigitEmpty() {
        CharacterReader reader = new CharacterReader("");
        assertFalse(reader.matchesDigit());
    }

    @Test
    public void matchConsumeTrue() {
        CharacterReader reader = new CharacterReader("abc");
        assertTrue(reader.matchConsume("ab"));
        assertEquals(2, reader.pos());
        assertEquals('c', reader.current());
    }

    @Test
    public void matchConsumeFalse() {
        CharacterReader reader = new CharacterReader("abc");
        assertFalse(reader.matchConsume("bc"));
        assertEquals(0, reader.pos());
    }

    @Test
    public void matchConsumeIgnoreCaseTrue() {
        CharacterReader reader = new CharacterReader("Abc");
        assertTrue(reader.matchConsumeIgnoreCase("aB"));
        assertEquals(2, reader.pos());
    }

    @Test
    public void matchConsumeIgnoreCaseFalse() {
        CharacterReader reader = new CharacterReader("abc");
        assertFalse(reader.matchConsumeIgnoreCase("AbC"));
        assertEquals(0, reader.pos());
    }

    @Test
    public void containsIgnoreCaseTrueLowerCase() {
        CharacterReader reader = new CharacterReader("abc</title>def");
        reader.advance();
        assertTrue(reader.containsIgnoreCase("</title>"));
    }

    @Test
    public void containsIgnoreCaseTrueUpperCase() {
        CharacterReader reader = new CharacterReader("abc</TITLE>def");
        reader.advance();
        assertTrue(reader.containsIgnoreCase("</title>"));
    }

    @Test
    public void containsIgnoreCaseFalse() {
        CharacterReader reader = new CharacterReader("abc");
        assertFalse(reader.containsIgnoreCase("xyz"));
    }

    @Test
    public void toStringReturnsRemaining() {
        CharacterReader reader = new CharacterReader("abc");
        reader.advance();
        assertEquals("bc", reader.toString());
    }

    @Test
    public void toStringEmpty() {
        CharacterReader reader = new CharacterReader("");
        assertEquals("", reader.toString());
    }
}
