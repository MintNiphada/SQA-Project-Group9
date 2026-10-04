package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class CharacterReaderTest {

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNull() {
        new CharacterReader(null);
    }

    @Test
    public void testConstructorNormalizesCarriageReturns() {
        CharacterReader r1 = new CharacterReader("a\r\nb\rc\nd");
        assertEquals("a\nb\nc\nd", r1.toString());
        assertEquals("a\nb\nc\nd", r1.consumeToEnd());

        CharacterReader r2 = new CharacterReader("\r\n\r\n");
        assertEquals("\n\n", r2.consumeToEnd());
    }

    @Test
    public void testPosAndIsEmpty() {
        CharacterReader r = new CharacterReader("abc");
        assertEquals(0, r.pos());
        assertFalse(r.isEmpty());

        r.advance();
        assertEquals(1, r.pos());
        assertFalse(r.isEmpty());

        r.advance();
        r.advance();
        assertEquals(3, r.pos());
        assertTrue(r.isEmpty());

        CharacterReader emptyReader = new CharacterReader("");
        assertEquals(0, emptyReader.pos());
        assertTrue(emptyReader.isEmpty());
    }

    @Test
    public void testCurrentAndConsume() {
        CharacterReader r = new CharacterReader("ab");
        assertEquals('a', r.current());
        assertEquals('a', r.consume());
        assertEquals(1, r.pos());

        assertEquals('b', r.current());
        assertEquals('b', r.consume());
        assertEquals(2, r.pos());

        assertEquals(CharacterReader.EOF, r.current());
        assertEquals(CharacterReader.EOF, r.consume());
        assertEquals(3, r.pos());
    }

    @Test
    public void testUnconsumeAndAdvance() {
        CharacterReader r = new CharacterReader("abc");
        assertEquals('a', r.consume());
        assertEquals('b', r.consume());
        assertEquals(2, r.pos());

        r.unconsume();
        assertEquals(1, r.pos());
        assertEquals('b', r.current());

        r.advance();
        assertEquals(2, r.pos());
        assertEquals('c', r.current());
    }

    @Test
    public void testMarkAndRewind() {
        CharacterReader r = new CharacterReader("abcdef");
        r.consume(); // at 'b' (pos 1)
        r.mark();

        r.consume();
        r.consume();
        assertEquals(3, r.pos());

        r.rewindToMark();
        assertEquals(1, r.pos());
        assertEquals('b', r.current());
    }

    @Test
    public void testConsumeAsString() {
        CharacterReader r = new CharacterReader("abc");
        assertEquals("a", r.consumeAsString());
        assertEquals("b", r.consumeAsString());
        assertEquals("c", r.consumeAsString());
    }

    @Test
    public void testConsumeToChar() {
        CharacterReader r = new CharacterReader("hello world");
        String consumed = r.consumeTo(' ');
        assertEquals("hello", consumed);
        assertEquals(' ', r.current());
        assertEquals(5, r.pos());

        // consume to char not present
        String rest = r.consumeTo('z');
        assertEquals(" world", rest);
        assertTrue(r.isEmpty());
        assertEquals(11, r.pos());
    }

    @Test
    public void testConsumeToString() {
        CharacterReader r = new CharacterReader("one two three");
        String consumed = r.consumeTo("two");
        assertEquals("one ", consumed);
        assertEquals('t', r.current());
        assertEquals(4, r.pos());

        String rest = r.consumeTo("four");
        assertEquals("two three", rest);
        assertTrue(r.isEmpty());
    }

    @Test
    public void testConsumeToAny() {
        CharacterReader r = new CharacterReader("foo & bar < baz");
        String consumed = r.consumeToAny('&', '<');
        assertEquals("foo ", consumed);
        assertEquals('&', r.current());

        r.advance(); // skip '&'
        consumed = r.consumeToAny('&', '<');
        assertEquals(" bar ", consumed);
        assertEquals('<', r.current());

        // not found
        r.advance();
        consumed = r.consumeToAny('x', 'y', 'z');
        assertEquals(" baz", consumed);
        assertTrue(r.isEmpty());

        // empty / when already at matching char
        CharacterReader r2 = new CharacterReader("&test");
        assertEquals("", r2.consumeToAny('&'));
        assertEquals('&', r2.current());

        // empty reader
        CharacterReader r3 = new CharacterReader("");
        assertEquals("", r3.consumeToAny('a', 'b'));
    }

    @Test
    public void testConsumeToEnd() {
        CharacterReader r = new CharacterReader("hello");
        r.advance();
        r.advance();
        assertEquals("llo", r.consumeToEnd());
        assertTrue(r.isEmpty());

        // calling consumeToEnd on empty reader
        assertEquals("", r.consumeToEnd());
    }

    @Test
    public void testConsumeLetterSequence() {
        CharacterReader r = new CharacterReader("HelloWorld123");
        assertEquals("HelloWorld", r.consumeLetterSequence());
        assertEquals('1', r.current());

        CharacterReader r2 = new CharacterReader("123abc");
        assertEquals("", r2.consumeLetterSequence());
        assertEquals('1', r2.current());

        CharacterReader r3 = new CharacterReader("abcXYZ");
        assertEquals("abcXYZ", r3.consumeLetterSequence());
        assertTrue(r3.isEmpty());

        CharacterReader r4 = new CharacterReader("");
        assertEquals("", r4.consumeLetterSequence());
    }

    @Test
    public void testConsumeHexSequence() {
        CharacterReader r = new CharacterReader("0123456789abcdefABCDEFghij");
        assertEquals("0123456789abcdefABCDEF", r.consumeHexSequence());
        assertEquals('g', r.current());

        CharacterReader r2 = new CharacterReader("xyz");
        assertEquals("", r2.consumeHexSequence());
        assertEquals('x', r2.current());

        CharacterReader r3 = new CharacterReader("1aF");
        assertEquals("1aF", r3.consumeHexSequence());
        assertTrue(r3.isEmpty());

        CharacterReader r4 = new CharacterReader("");
        assertEquals("", r4.consumeHexSequence());
    }

    @Test
    public void testConsumeDigitSequence() {
        CharacterReader r = new CharacterReader("1234567890abc");
        assertEquals("1234567890", r.consumeDigitSequence());
        assertEquals('a', r.current());

        CharacterReader r2 = new CharacterReader("abc123");
        assertEquals("", r2.consumeDigitSequence());
        assertEquals('a', r2.current());

        CharacterReader r3 = new CharacterReader("987");
        assertEquals("987", r3.consumeDigitSequence());
        assertTrue(r3.isEmpty());

        CharacterReader r4 = new CharacterReader("");
        assertEquals("", r4.consumeDigitSequence());
    }

    @Test
    public void testMatchesChar() {
        CharacterReader r = new CharacterReader("test");
        assertTrue(r.matches('t'));
        assertFalse(r.matches('e'));

        r.advance();
        assertTrue(r.matches('e'));
        assertFalse(r.matches('t'));

        r.consumeToEnd();
        assertFalse(r.matches('t'));
    }

    @Test
    public void testMatchesString() {
        CharacterReader r = new CharacterReader("abcdef");
        assertTrue(r.matches("abc"));
        assertTrue(r.matches("abcdef"));
        assertFalse(r.matches("bc"));
        assertFalse(r.matches("abcdefg"));

        r.advance();
        assertTrue(r.matches("bc"));
        assertFalse(r.matches("abc"));
    }

    @Test
    public void testMatchesIgnoreCase() {
        CharacterReader r = new CharacterReader("AbCdEf");
        assertTrue(r.matchesIgnoreCase("abcdef"));
        assertTrue(r.matchesIgnoreCase("ABCDEF"));
        assertTrue(r.matchesIgnoreCase("aBc"));
        assertFalse(r.matchesIgnoreCase("bc"));
        assertFalse(r.matchesIgnoreCase("abcdefg"));

        r.advance();
        assertTrue(r.matchesIgnoreCase("bc"));
        assertTrue(r.matchesIgnoreCase("BC"));
    }

    @Test
    public void testMatchesAny() {
        CharacterReader r = new CharacterReader("abc");
        assertTrue(r.matchesAny('x', 'y', 'a'));
        assertFalse(r.matchesAny('x', 'y', 'z'));

        r.advance();
        assertTrue(r.matchesAny('b', 'c'));
        assertFalse(r.matchesAny('a'));

        r.consumeToEnd();
        assertFalse(r.matchesAny('a', 'b', 'c'));
    }

    @Test
    public void testMatchesLetter() {
        CharacterReader r = new CharacterReader("aZ1_");
        assertTrue(r.matchesLetter()); // 'a'
        r.advance();
        assertTrue(r.matchesLetter()); // 'Z'
        r.advance();
        assertFalse(r.matchesLetter()); // '1'
        r.advance();
        assertFalse(r.matchesLetter()); // '_'
        r.advance();
        assertFalse(r.matchesLetter()); // empty

        CharacterReader rUpper = new CharacterReader("A");
        assertTrue(rUpper.matchesLetter());

        CharacterReader rLower = new CharacterReader("z");
        assertTrue(rLower.matchesLetter());

        CharacterReader rNonLetter = new CharacterReader("@");
        assertFalse(rNonLetter.matchesLetter());
    }

    @Test
    public void testMatchesDigit() {
        CharacterReader r = new CharacterReader("09a");
        assertTrue(r.matchesDigit()); // '0'
        r.advance();
        assertTrue(r.matchesDigit()); // '9'
        r.advance();
        assertFalse(r.matchesDigit()); // 'a'
        r.advance();
        assertFalse(r.matchesDigit()); // empty

        CharacterReader rNonDigit = new CharacterReader("/");
        assertFalse(rNonDigit.matchesDigit());
    }

    @Test
    public void testMatchConsume() {
        CharacterReader r = new CharacterReader("one two");
        assertTrue(r.matchConsume("one "));
        assertEquals(4, r.pos());
        assertEquals('t', r.current());

        assertFalse(r.matchConsume("three"));
        assertEquals(4, r.pos());

        assertTrue(r.matchConsume("two"));
        assertTrue(r.isEmpty());
    }

    @Test
    public void testMatchConsumeIgnoreCase() {
        CharacterReader r = new CharacterReader("ONE TWO");
        assertTrue(r.matchConsumeIgnoreCase("one "));
        assertEquals(4, r.pos());
        assertEquals('T', r.current());

        assertFalse(r.matchConsumeIgnoreCase("three"));
        assertEquals(4, r.pos());

        assertTrue(r.matchConsumeIgnoreCase("tWo"));
        assertTrue(r.isEmpty());
    }

    @Test
    public void testContainsIgnoreCase() {
        CharacterReader r = new CharacterReader("<html><TITLE>Test</title><style>...</STYLE></html>");
        assertTrue(r.containsIgnoreCase("</title>"));
        assertTrue(r.containsIgnoreCase("</TITLE>"));
        assertTrue(r.containsIgnoreCase("<style>"));
        assertTrue(r.containsIgnoreCase("<STYLE>"));
        assertFalse(r.containsIgnoreCase("</head>"));

        r.consumeTo('<');
        r.advance();
        r.consumeTo('<'); // at second '<', past TITLE opening
        assertTrue(r.containsIgnoreCase("</title>"));

        CharacterReader r2 = new CharacterReader("something");
        r2.consumeToEnd();
        assertFalse(r2.containsIgnoreCase("something"));
    }

    @Test
    public void testToString() {
        CharacterReader r = new CharacterReader("hello world");
        assertEquals("hello world", r.toString());

        r.consumeTo(' ');
        assertEquals(" world", r.toString());

        r.consumeToEnd();
        assertEquals("", r.toString());
    }
}
