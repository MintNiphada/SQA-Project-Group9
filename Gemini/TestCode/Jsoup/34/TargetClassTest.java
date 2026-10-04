package org.jsoup.parser;

import org.junit.Assert;
import org.junit.Test;

public class CharacterReaderTest {

    @Test(expected = IllegalArgumentException.class)
    public void testNullInput() {
        new CharacterReader(null);
    }

    @Test
    public void testBasicNavigation() {
        CharacterReader reader = new CharacterReader("abc");
        Assert.assertEquals(0, reader.pos());
        Assert.assertFalse(reader.isEmpty());
        Assert.assertEquals('a', reader.current());

        Assert.assertEquals('a', reader.consume());
        Assert.assertEquals(1, reader.pos());
        Assert.assertEquals('b', reader.current());

        reader.advance();
        Assert.assertEquals(2, reader.pos());
        Assert.assertEquals('c', reader.current());

        reader.unconsume();
        Assert.assertEquals(1, reader.pos());
        Assert.assertEquals('b', reader.current());

        Assert.assertEquals("b", reader.consumeAsString());
        Assert.assertEquals(2, reader.pos());

        Assert.assertEquals('c', reader.consume());
        Assert.assertEquals(3, reader.pos());
        Assert.assertTrue(reader.isEmpty());
        Assert.assertEquals(CharacterReader.EOF, reader.current());
        Assert.assertEquals(CharacterReader.EOF, reader.consume());
    }

    @Test
    public void testMarkAndRewind() {
        CharacterReader reader = new CharacterReader("abcdef");
        reader.consume();
        reader.mark();
        Assert.assertEquals(1, reader.pos());

        reader.consume();
        reader.consume();
        Assert.assertEquals(3, reader.pos());

        reader.rewindToMark();
        Assert.assertEquals(1, reader.pos());
        Assert.assertEquals('b', reader.current());
    }

    @Test
    public void testNextIndexOfChar() {
        CharacterReader reader = new CharacterReader("abcdefg");
        Assert.assertEquals(3, reader.nextIndexOf('d'));
        Assert.assertEquals(-1, reader.nextIndexOf('z'));
        reader.consume();
        reader.consume();
        Assert.assertEquals(1, reader.nextIndexOf('d'));
        Assert.assertEquals(-1, reader.nextIndexOf('a'));
    }

    @Test
    public void testNextIndexOfSeq() {
        CharacterReader reader = new CharacterReader("abcdefghij");
        Assert.assertEquals(3, reader.nextIndexOf("def"));
        Assert.assertEquals(-1, reader.nextIndexOf("xyz"));
        Assert.assertEquals(-1, reader.nextIndexOf("defk"));

        CharacterReader reader2 = new CharacterReader("abadef");
        Assert.assertEquals(3, reader2.nextIndexOf("def"));

        CharacterReader reader3 = new CharacterReader("aaaaa");
        Assert.assertEquals(0, reader3.nextIndexOf("aa"));
        Assert.assertEquals(-1, reader3.nextIndexOf("aaaaaa"));
    }

    @Test
    public void testConsumeToChar() {
        CharacterReader reader = new CharacterReader("one,two,three");
        Assert.assertEquals("one", reader.consumeTo(','));
        Assert.assertEquals(',', reader.current());
        reader.consume();
        Assert.assertEquals("two", reader.consumeTo(','));
        reader.consume();
        Assert.assertEquals("three", reader.consumeTo(','));
        Assert.assertTrue(reader.isEmpty());
    }

    @Test
    public void testConsumeToString() {
        CharacterReader reader = new CharacterReader("start<!--comment-->end");
        Assert.assertEquals("start", reader.consumeTo("<!--"));
        Assert.assertEquals("<!--comment-->", reader.consumeTo("end"));
        Assert.assertEquals("end", reader.consumeTo("notfound"));
        Assert.assertTrue(reader.isEmpty());
    }

    @Test
    public void testConsumeToAny() {
        CharacterReader reader = new CharacterReader("foo & bar < baz");
        Assert.assertEquals("foo ", reader.consumeToAny('&', '<'));
        Assert.assertEquals('&', reader.consume());
        Assert.assertEquals(" bar ", reader.consumeToAny('&', '<'));
        Assert.assertEquals('<', reader.consume());
        Assert.assertEquals(" baz", reader.consumeToAny('&', '<'));
        Assert.assertEquals("", reader.consumeToAny('&', '<'));
    }

    @Test
    public void testConsumeToEnd() {
        CharacterReader reader = new CharacterReader("hello world");
        reader.consume();
        Assert.assertEquals("ello world", reader.consumeToEnd());
        Assert.assertTrue(reader.isEmpty());
        Assert.assertEquals("", reader.consumeToEnd());
    }

    @Test
    public void testConsumeLetterSequence() {
        CharacterReader reader = new CharacterReader("abcABC123");
        Assert.assertEquals("abcABC", reader.consumeLetterSequence());
        Assert.assertEquals('1', reader.current());

        CharacterReader reader2 = new CharacterReader("123abc");
        Assert.assertEquals("", reader2.consumeLetterSequence());
        Assert.assertEquals('1', reader2.current());
    }

    @Test
    public void testConsumeLetterThenDigitSequence() {
        CharacterReader reader = new CharacterReader("abc123def");
        Assert.assertEquals("abc123", reader.consumeLetterThenDigitSequence());
        Assert.assertEquals('d', reader.current());

        CharacterReader reader2 = new CharacterReader("123def");
        Assert.assertEquals("123", reader2.consumeLetterThenDigitSequence());
        Assert.assertEquals('d', reader2.current());

        CharacterReader reader3 = new CharacterReader("abc");
        Assert.assertEquals("abc", reader3.consumeLetterThenDigitSequence());
        Assert.assertTrue(reader3.isEmpty());

        CharacterReader reader4 = new CharacterReader("!@#");
        Assert.assertEquals("", reader4.consumeLetterThenDigitSequence());
    }

    @Test
    public void testConsumeHexSequence() {
        CharacterReader reader = new CharacterReader("0123456789abcdefABCDEFghij");
        Assert.assertEquals("0123456789abcdefABCDEF", reader.consumeHexSequence());
        Assert.assertEquals('g', reader.current());

        CharacterReader reader2 = new CharacterReader("xyz");
        Assert.assertEquals("", reader2.consumeHexSequence());
    }

    @Test
    public void testConsumeDigitSequence() {
        CharacterReader reader = new CharacterReader("0123456789abc");
        Assert.assertEquals("0123456789", reader.consumeDigitSequence());
        Assert.assertEquals('a', reader.current());

        CharacterReader reader2 = new CharacterReader("abc");
        Assert.assertEquals("", reader2.consumeDigitSequence());
    }

    @Test
    public void testMatchesChar() {
        CharacterReader reader = new CharacterReader("abc");
        Assert.assertTrue(reader.matches('a'));
        Assert.assertFalse(reader.matches('b'));
        reader.consumeToEnd();
        Assert.assertFalse(reader.matches('a'));
    }

    @Test
    public void testMatchesString() {
        CharacterReader reader = new CharacterReader("abcdef");
        Assert.assertTrue(reader.matches("abc"));
        Assert.assertFalse(reader.matches("abd"));
        Assert.assertFalse(reader.matches("abcdefg"));
        reader.consumeToEnd();
        Assert.assertFalse(reader.matches("a"));
    }

    @Test
    public void testMatchesIgnoreCase() {
        CharacterReader reader = new CharacterReader("ABCdef");
        Assert.assertTrue(reader.matchesIgnoreCase("abc"));
        Assert.assertTrue(reader.matchesIgnoreCase("ABC"));
        Assert.assertTrue(reader.matchesIgnoreCase("abcDEF"));
        Assert.assertFalse(reader.matchesIgnoreCase("abd"));
        Assert.assertFalse(reader.matchesIgnoreCase("abcdefg"));
        reader.consumeToEnd();
        Assert.assertFalse(reader.matchesIgnoreCase("a"));
    }

    @Test
    public void testMatchesAny() {
        CharacterReader reader = new CharacterReader("abc");
        Assert.assertTrue(reader.matchesAny('x', 'y', 'a'));
        Assert.assertFalse(reader.matchesAny('x', 'y', 'z'));
        reader.consumeToEnd();
        Assert.assertFalse(reader.matchesAny('a'));
    }

    @Test
    public void testMatchesLetter() {
        CharacterReader reader = new CharacterReader("aZ0");
        Assert.assertTrue(reader.matchesLetter());
        reader.advance();
        Assert.assertTrue(reader.matchesLetter());
        reader.advance();
        Assert.assertFalse(reader.matchesLetter());
        reader.advance();
        Assert.assertFalse(reader.matchesLetter());
    }

    @Test
    public void testMatchesDigit() {
        CharacterReader reader = new CharacterReader("09a");
        Assert.assertTrue(reader.matchesDigit());
        reader.advance();
        Assert.assertTrue(reader.matchesDigit());
        reader.advance();
        Assert.assertFalse(reader.matchesDigit());
        reader.advance();
        Assert.assertFalse(reader.matchesDigit());
    }

    @Test
    public void testMatchConsume() {
        CharacterReader reader = new CharacterReader("abcdef");
        Assert.assertFalse(reader.matchConsume("abd"));
        Assert.assertEquals(0, reader.pos());

        Assert.assertTrue(reader.matchConsume("abc"));
        Assert.assertEquals(3, reader.pos());
        Assert.assertEquals('d', reader.current());

        Assert.assertFalse(reader.matchConsume("xyz"));
        Assert.assertEquals(3, reader.pos());
    }

    @Test
    public void testMatchConsumeIgnoreCase() {
        CharacterReader reader = new CharacterReader("ABCdef");
        Assert.assertFalse(reader.matchConsumeIgnoreCase("abd"));
        Assert.assertEquals(0, reader.pos());

        Assert.assertTrue(reader.matchConsumeIgnoreCase("abc"));
        Assert.assertEquals(3, reader.pos());
        Assert.assertEquals('d', reader.current());

        Assert.assertFalse(reader.matchConsumeIgnoreCase("xyz"));
        Assert.assertEquals(3, reader.pos());
    }

    @Test
    public void testContainsIgnoreCase() {
        CharacterReader reader = new CharacterReader("foo </TITLE> bar </style> baz");
        Assert.assertTrue(reader.containsIgnoreCase("</title>"));
        Assert.assertTrue(reader.containsIgnoreCase("</STYLE>"));
        Assert.assertTrue(reader.containsIgnoreCase("</style>"));
        Assert.assertFalse(reader.containsIgnoreCase("</script>"));
    }

    @Test
    public void testToString() {
        CharacterReader reader = new CharacterReader("abcdef");
        Assert.assertEquals("abcdef", reader.toString());
        reader.consume();
        reader.consume();
        Assert.assertEquals("cdef", reader.toString());
        reader.consumeToEnd();
        Assert.assertEquals("", reader.toString());
    }
}
