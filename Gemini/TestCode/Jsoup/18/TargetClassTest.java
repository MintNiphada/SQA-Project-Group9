package org.jsoup.parser;

import org.junit.Assert;
import org.junit.Test;

public class CharacterReaderTest {

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNull() {
        new CharacterReader(null);
    }

    @Test
    public void testEmptyReader() {
        CharacterReader reader = new CharacterReader("");
        Assert.assertEquals(0, reader.pos());
        Assert.assertTrue(reader.isEmpty());
        Assert.assertEquals(CharacterReader.EOF, reader.current());
        Assert.assertEquals(CharacterReader.EOF, reader.consume());
        Assert.assertFalse(reader.matches('a'));
        Assert.assertFalse(reader.matches("a"));
        Assert.assertFalse(reader.matchesIgnoreCase("a"));
        Assert.assertFalse(reader.matchesAny('a', 'b'));
        Assert.assertFalse(reader.matchesLetter());
        Assert.assertFalse(reader.matchesDigit());
        Assert.assertFalse(reader.matchConsume("a"));
        Assert.assertFalse(reader.matchConsumeIgnoreCase("a"));
        Assert.assertEquals("", reader.consumeLetterSequence());
        Assert.assertEquals("", reader.consumeDigitSequence());
        Assert.assertEquals("", reader.consumeHexSequence());
        Assert.assertEquals("", reader.consumeToAny('a', 'b'));
        Assert.assertEquals("", reader.toString());
    }

    @Test
    public void testPosAdvanceAndUnconsume() {
        CharacterReader reader = new CharacterReader("abc");
        Assert.assertEquals(0, reader.pos());
        reader.advance();
        Assert.assertEquals(1, reader.pos());
        Assert.assertEquals('b', reader.current());
        reader.unconsume();
        Assert.assertEquals(0, reader.pos());
        Assert.assertEquals('a', reader.current());
    }

    @Test
    public void testMarkAndRewind() {
        CharacterReader reader = new CharacterReader("abcdef");
        reader.advance(); // pos = 1 ('b')
        reader.mark();
        reader.advance();
        reader.advance(); // pos = 3 ('d')
        Assert.assertEquals(3, reader.pos());
        reader.rewindToMark();
        Assert.assertEquals(1, reader.pos());
        Assert.assertEquals('b', reader.current());
    }

    @Test
    public void testConsume() {
        CharacterReader reader = new CharacterReader("ab");
        Assert.assertFalse(reader.isEmpty());
        Assert.assertEquals('a', reader.consume());
        Assert.assertEquals(1, reader.pos());
        Assert.assertEquals('b', reader.consume());
        Assert.assertEquals(2, reader.pos());
        Assert.assertTrue(reader.isEmpty());
        Assert.assertEquals(CharacterReader.EOF, reader.consume());
        Assert.assertEquals(CharacterReader.EOF, reader.current());
    }

    @Test
    public void testConsumeAsString() {
        CharacterReader reader = new CharacterReader("abc");
        String consumed = reader.consumeAsString();
        Assert.assertEquals("", consumed);
        Assert.assertEquals(1, reader.pos());
        Assert.assertEquals('b', reader.current());
    }

    @Test
    public void testConsumeToChar() {
        CharacterReader reader = new CharacterReader("one-two-three");
        String part1 = reader.consumeTo('-');
        Assert.assertEquals("one", part1);
        Assert.assertEquals(3, reader.pos());
        Assert.assertEquals('-', reader.current());

        reader.advance(); // skip '-'
        String part2 = reader.consumeTo('-');
        Assert.assertEquals("two", part2);
        Assert.assertEquals(7, reader.pos());

        reader.advance(); // skip '-'
        String part3 = reader.consumeTo('z'); // not found -> calls consumeToEnd()
        Assert.assertEquals("threedefault".substring(0, "three".length() - 1), part3);
        Assert.assertEquals(13, reader.pos());
        Assert.assertTrue(reader.isEmpty());
    }

    @Test
    public void testConsumeToString() {
        CharacterReader reader = new CharacterReader("hello <b>world</b>");
        String part1 = reader.consumeTo("<b>");
        Assert.assertEquals("hello ", part1);
        Assert.assertEquals(6, reader.pos());

        String part2 = reader.consumeTo("nonexistent");
        Assert.assertEquals("<b>world</b>".substring(0, "<b>world</b>".length() - 1), part2);
        Assert.assertEquals(reader.pos(), 18);
        Assert.assertTrue(reader.isEmpty());
    }

    @Test
    public void testConsumeToAny() {
        CharacterReader reader = new CharacterReader("foo & bar < baz");
        String part1 = reader.consumeToAny('&', '<');
        Assert.assertEquals("foo ", part1);
        Assert.assertEquals('&', reader.current());

        reader.advance(); // skip '&'
        String part2 = reader.consumeToAny('&', '<');
        Assert.assertEquals(" bar ", part2);
        Assert.assertEquals('<', reader.current());

        reader.advance(); // skip '<'
        String part3 = reader.consumeToAny('x', 'y', 'z');
        Assert.assertEquals(" ba", part3);
        Assert.assertEquals('z', reader.current());

        String empty = reader.consumeToAny('z');
        Assert.assertEquals("", empty);

        reader.advance(); // consume 'z'
        Assert.assertTrue(reader.isEmpty());
        String atEnd = reader.consumeToAny('a');
        Assert.assertEquals("", atEnd);
    }

    @Test
    public void testConsumeToEnd() {
        CharacterReader reader = new CharacterReader("abcdef");
        reader.advance();
        String result = reader.consumeToEnd();
        Assert.assertEquals("bcde", result);
        Assert.assertEquals(6, reader.pos());
        Assert.assertTrue(reader.isEmpty());
    }

    @Test
    public void testConsumeLetterSequence() {
        CharacterReader reader = new CharacterReader("abcXYZ123def");
        String letters1 = reader.consumeLetterSequence();
        Assert.assertEquals("abcXYZ", letters1);
        Assert.assertEquals('1', reader.current());

        String nonLetter = reader.consumeLetterSequence();
        Assert.assertEquals("", nonLetter);

        reader.consumeTo('d');
        String letters2 = reader.consumeLetterSequence();
        Assert.assertEquals("def", letters2);
        Assert.assertTrue(reader.isEmpty());
    }

    @Test
    public void testConsumeHexSequence() {
        CharacterReader reader = new CharacterReader("0123456789abcdefABCDEFghi");
        String hex = reader.consumeHexSequence();
        Assert.assertEquals("0123456789abcdefABCDEF", hex);
        Assert.assertEquals('g', reader.current());

        String nonHex = reader.consumeHexSequence();
        Assert.assertEquals("", nonHex);
    }

    @Test
    public void testConsumeDigitSequence() {
        CharacterReader reader = new CharacterReader("0123456789abc");
        String digits = reader.consumeDigitSequence();
        Assert.assertEquals("0123456789", digits);
        Assert.assertEquals('a', reader.current());

        String nonDigit = reader.consumeDigitSequence();
        Assert.assertEquals("", nonDigit);
    }

    @Test
    public void testMatches() {
        CharacterReader reader = new CharacterReader("Test String");
        Assert.assertTrue(reader.matches('T'));
        Assert.assertFalse(reader.matches('t'));
        Assert.assertTrue(reader.matches("Test"));
        Assert.assertFalse(reader.matches("test"));
        Assert.assertFalse(reader.matches("Testing"));

        reader.consumeTo('S');
        Assert.assertTrue(reader.matches('S'));
        Assert.assertTrue(reader.matches("String"));
        Assert.assertFalse(reader.matches("Strings"));

        reader.consumeToEnd();
        Assert.assertFalse(reader.matches(' '));
        Assert.assertFalse(reader.matches(""));
    }

    @Test
    public void testMatchesIgnoreCase() {
        CharacterReader reader = new CharacterReader("Test String");
        Assert.assertTrue(reader.matchesIgnoreCase("test"));
        Assert.assertTrue(reader.matchesIgnoreCase("TEST"));
        Assert.assertTrue(reader.matchesIgnoreCase("TeSt StRiNg"));
        Assert.assertFalse(reader.matchesIgnoreCase("Test String 123"));
        Assert.assertFalse(reader.matchesIgnoreCase("Best"));
    }

    @Test
    public void testMatchesAny() {
        CharacterReader reader = new CharacterReader("abcdef");
        Assert.assertTrue(reader.matchesAny('x', 'y', 'a'));
        Assert.assertFalse(reader.matchesAny('x', 'y', 'z'));
        reader.consumeToEnd();
        Assert.assertFalse(reader.matchesAny('a', 'b'));
    }

    @Test
    public void testMatchesLetter() {
        CharacterReader reader = new CharacterReader("aA1-");
        Assert.assertTrue(reader.matchesLetter()); // 'a'
        reader.advance();
        Assert.assertTrue(reader.matchesLetter()); // 'A'
        reader.advance();
        Assert.assertFalse(reader.matchesLetter()); // '1'
        reader.advance();
        Assert.assertFalse(reader.matchesLetter()); // '-'
        reader.advance();
        Assert.assertFalse(reader.matchesLetter()); // EOF
    }

    @Test
    public void testMatchesDigit() {
        CharacterReader reader = new CharacterReader("09a-");
        Assert.assertTrue(reader.matchesDigit()); // '0'
        reader.advance();
        Assert.assertTrue(reader.matchesDigit()); // '9'
        reader.advance();
        Assert.assertFalse(reader.matchesDigit()); // 'a'
        reader.advance();
        Assert.assertFalse(reader.matchesDigit()); // '-'
        reader.advance();
        Assert.assertFalse(reader.matchesDigit()); // EOF
    }

    @Test
    public void testMatchConsume() {
        CharacterReader reader = new CharacterReader("one two three");
        Assert.assertTrue(reader.matchConsume("one "));
        Assert.assertEquals(4, reader.pos());
        Assert.assertFalse(reader.matchConsume("ONE "));
        Assert.assertEquals(4, reader.pos());
        Assert.assertTrue(reader.matchConsume("two"));
        Assert.assertEquals(7, reader.pos());
    }

    @Test
    public void testMatchConsumeIgnoreCase() {
        CharacterReader reader = new CharacterReader("ONE two THREE");
        Assert.assertTrue(reader.matchConsumeIgnoreCase("one "));
        Assert.assertEquals(4, reader.pos());
        Assert.assertFalse(reader.matchConsumeIgnoreCase("four"));
        Assert.assertEquals(4, reader.pos());
        Assert.assertTrue(reader.matchConsumeIgnoreCase("TWO "));
        Assert.assertEquals(8, reader.pos());
    }

    @Test
    public void testContainsIgnoreCase() {
        CharacterReader reader = new CharacterReader("<html><title>Hello</title><STYLE>body{}</style></html>");
        Assert.assertTrue(reader.containsIgnoreCase("</TITLE>"));
        Assert.assertTrue(reader.containsIgnoreCase("</title>"));
        Assert.assertTrue(reader.containsIgnoreCase("<style>"));
        Assert.assertTrue(reader.containsIgnoreCase("<STYLE>"));
        Assert.assertFalse(reader.containsIgnoreCase("</script>"));

        reader.consumeTo("<STYLE>");
        Assert.assertTrue(reader.containsIgnoreCase("</style>"));
        Assert.assertFalse(reader.containsIgnoreCase("<title>"));
    }

    @Test
    public void testToString() {
        CharacterReader reader = new CharacterReader("Hello World");
        Assert.assertEquals("Hello World", reader.toString());
        reader.consumeTo('W');
        Assert.assertEquals("World", reader.toString());
        reader.consumeToEnd();
        Assert.assertEquals("", reader.toString());
    }
}
