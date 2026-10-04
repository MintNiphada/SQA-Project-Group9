package org.jsoup.parser;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class CharacterReaderTest {

    @Test(expected = IllegalArgumentException.class)
    public void testNullInput() {
        new CharacterReader(null);
    }

    @Test
    public void testBasicOperationsAndEOF() {
        CharacterReader r = new CharacterReader("abc");
        Assert.assertEquals(0, r.pos());
        Assert.assertFalse(r.isEmpty());
        Assert.assertEquals('a', r.current());
        Assert.assertEquals("abc", r.toString());

        Assert.assertEquals('a', r.consume());
        Assert.assertEquals(1, r.pos());
        Assert.assertEquals('b', r.current());

        r.unconsume();
        Assert.assertEquals(0, r.pos());
        Assert.assertEquals('a', r.current());

        r.advance();
        Assert.assertEquals(1, r.pos());
        Assert.assertEquals('b', r.consume());
        Assert.assertEquals('c', r.consume());
        Assert.assertTrue(r.isEmpty());
        Assert.assertEquals(CharacterReader.EOF, r.current());
        Assert.assertEquals(CharacterReader.EOF, r.consume());

        r.mark();
        r.unconsume();
        Assert.assertEquals('c', r.current());
        r.rewindToMark();
        Assert.assertEquals(CharacterReader.EOF, r.current());
    }

    @Test
    public void testConsumeAsString() {
        CharacterReader r = new CharacterReader("abc");
        Assert.assertEquals("a", r.consumeAsString());
        Assert.assertEquals("b", r.consumeAsString());
        Assert.assertEquals("c", r.consumeAsString());
    }

    @Test
    public void testNextIndexOfChar() {
        CharacterReader r = new CharacterReader("hello world");
        Assert.assertEquals(0, r.nextIndexOf('h'));
        Assert.assertEquals(4, r.nextIndexOf('o'));
        Assert.assertEquals(-1, r.nextIndexOf('z'));

        r.consume(); // at 'e'
        Assert.assertEquals(3, r.nextIndexOf('o'));
    }

    @Test
    public void testNextIndexOfCharSequence() {
        CharacterReader r = new CharacterReader("abcdefg abcdefg");
        Assert.assertEquals(0, r.nextIndexOf("abc"));
        Assert.assertEquals(3, r.nextIndexOf("def"));
        Assert.assertEquals(8, r.nextIndexOf("abc"));
        Assert.assertEquals(-1, r.nextIndexOf("xyz"));
        Assert.assertEquals(-1, r.nextIndexOf("longtextthatdoesnotmatchandtoolong"));

        CharacterReader r2 = new CharacterReader("mississippi");
        Assert.assertEquals(1, r2.nextIndexOf("iss"));
        Assert.assertEquals(4, r2.nextIndexOf("iss"));
        Assert.assertEquals(-1, r2.nextIndexOf("issipq"));
    }

    @Test
    public void testConsumeToChar() {
        CharacterReader r = new CharacterReader("foo/bar/baz");
        Assert.assertEquals("foo", r.consumeTo('/'));
        Assert.assertEquals('/', r.consume());
        Assert.assertEquals("bar", r.consumeTo('/'));
        Assert.assertEquals('/', r.consume());
        Assert.assertEquals("baz", r.consumeTo('/'));
        Assert.assertTrue(r.isEmpty());
    }

    @Test
    public void testConsumeToString() {
        CharacterReader r = new CharacterReader("<html><head><title>Test</title></head></html>");
        Assert.assertEquals("<html><head><title>", r.consumeTo("Test"));
        Assert.assertEquals("Test", r.consumeTo("</title>"));
        Assert.assertEquals("</title></head></html>", r.consumeTo("notfound"));
    }

    @Test
    public void testConsumeToAny() {
        CharacterReader r = new CharacterReader("foo & bar < baz");
        Assert.assertEquals("foo ", r.consumeToAny('&', '<'));
        Assert.assertEquals('&', r.consume());
        Assert.assertEquals(" bar ", r.consumeToAny('&', '<'));
        Assert.assertEquals('<', r.consume());
        Assert.assertEquals(" baz", r.consumeToAny('&', '<'));
        Assert.assertEquals("", r.consumeToAny('&', '<'));
    }

    @Test
    public void testConsumeToAnySorted() {
        char[] sorted = new char[]{'&', '<'};
        Arrays.sort(sorted);
        CharacterReader r = new CharacterReader("foo & bar < baz");
        Assert.assertEquals("foo ", r.consumeToAnySorted(sorted));
        Assert.assertEquals('&', r.consume());
        Assert.assertEquals(" bar ", r.consumeToAnySorted(sorted));
        Assert.assertEquals('<', r.consume());
        Assert.assertEquals(" baz", r.consumeToAnySorted(sorted));
        Assert.assertEquals("", r.consumeToAnySorted(sorted));
    }

    @Test
    public void testConsumeData() {
        CharacterReader r = new CharacterReader("text &amp; text <tag> \u0000 rest");
        Assert.assertEquals("text ", r.consumeData());
        r.consume();
        Assert.assertEquals("amp; text ", r.consumeData());
        r.consume();
        Assert.assertEquals("tag> ", r.consumeData());
        r.consume();
        Assert.assertEquals(" rest", r.consumeData());
    }

    @Test
    public void testConsumeTagName() {
        CharacterReader r = new CharacterReader("div\tspan\np\rbr\fa b/c>d\u0000end");
        Assert.assertEquals("div", r.consumeTagName());
        r.consume();
        Assert.assertEquals("span", r.consumeTagName());
        r.consume();
        Assert.assertEquals("p", r.consumeTagName());
        r.consume();
        Assert.assertEquals("br", r.consumeTagName());
        r.consume();
        Assert.assertEquals("a", r.consumeTagName());
        r.consume();
        Assert.assertEquals("b", r.consumeTagName());
        r.consume();
        Assert.assertEquals("c", r.consumeTagName());
        r.consume();
        Assert.assertEquals("d", r.consumeTagName());
        r.consume();
        Assert.assertEquals("end", r.consumeTagName());
    }

    @Test
    public void testConsumeLetterSequence() {
        CharacterReader r = new CharacterReader("HelloWorld123");
        Assert.assertEquals("HelloWorld", r.consumeLetterSequence());
        Assert.assertEquals("123", r.consumeToEnd());
    }

    @Test
    public void testConsumeLetterThenDigitSequence() {
        CharacterReader r = new CharacterReader("abc123def456");
        Assert.assertEquals("abc123", r.consumeLetterThenDigitSequence());
        Assert.assertEquals("def456", r.consumeLetterThenDigitSequence());
        Assert.assertEquals("", r.consumeLetterThenDigitSequence());
    }

    @Test
    public void testConsumeHexSequence() {
        CharacterReader r = new CharacterReader("1a2F3z4G");
        Assert.assertEquals("1a2F3", r.consumeHexSequence());
        Assert.assertEquals('z', r.consume());
        Assert.assertEquals("4", r.consumeHexSequence());
        Assert.assertEquals('G', r.consume());
        Assert.assertEquals("", r.consumeHexSequence());
    }

    @Test
    public void testConsumeDigitSequence() {
        CharacterReader r = new CharacterReader("12345abc678");
        Assert.assertEquals("12345", r.consumeDigitSequence());
        Assert.assertEquals("abc", r.consumeLetterSequence());
        Assert.assertEquals("678", r.consumeDigitSequence());
        Assert.assertEquals("", r.consumeDigitSequence());
    }

    @Test
    public void testMatches() {
        CharacterReader r = new CharacterReader("Hello World");
        Assert.assertTrue(r.matches('H'));
        Assert.assertFalse(r.matches('h'));
        Assert.assertTrue(r.matches("Hello"));
        Assert.assertFalse(r.matches("Hello World Long Extra"));
        Assert.assertFalse(r.matches("hello"));

        r.consumeToEnd();
        Assert.assertFalse(r.matches('H'));
        Assert.assertFalse(r.matches("H"));
    }

    @Test
    public void testMatchesIgnoreCase() {
        CharacterReader r = new CharacterReader("Hello World");
        Assert.assertTrue(r.matchesIgnoreCase("hello"));
        Assert.assertTrue(r.matchesIgnoreCase("HELLO"));
        Assert.assertFalse(r.matchesIgnoreCase("world"));
        Assert.assertFalse(r.matchesIgnoreCase("Hello World Long Extra"));

        r.consumeToEnd();
        Assert.assertFalse(r.matchesIgnoreCase("hello"));
    }

    @Test
    public void testMatchesAny() {
        CharacterReader r = new CharacterReader("Hello");
        Assert.assertTrue(r.matchesAny('a', 'e', 'H'));
        Assert.assertFalse(r.matchesAny('a', 'e', 'h'));

        r.consumeToEnd();
        Assert.assertFalse(r.matchesAny('H'));
    }

    @Test
    public void testMatchesAnySorted() {
        char[] sorted = new char[]{'H', 'e', 'l', 'o'};
        Arrays.sort(sorted);
        CharacterReader r = new CharacterReader("Hello");
        Assert.assertTrue(r.matchesAnySorted(sorted));
        r.consume();
        Assert.assertTrue(r.matchesAnySorted(sorted));
        r.consumeToEnd();
        Assert.assertFalse(r.matchesAnySorted(sorted));
    }

    @Test
    public void testMatchesLetterAndDigit() {
        CharacterReader r = new CharacterReader("A1!");
        Assert.assertTrue(r.matchesLetter());
        Assert.assertFalse(r.matchesDigit());

        r.consume();
        Assert.assertFalse(r.matchesLetter());
        Assert.assertTrue(r.matchesDigit());

        r.consume();
        Assert.assertFalse(r.matchesLetter());
        Assert.assertFalse(r.matchesDigit());

        r.consume();
        Assert.assertFalse(r.matchesLetter());
        Assert.assertFalse(r.matchesDigit());
    }

    @Test
    public void testMatchConsume() {
        CharacterReader r = new CharacterReader("Hello World");
        Assert.assertFalse(r.matchConsume("world"));
        Assert.assertEquals(0, r.pos());
        Assert.assertTrue(r.matchConsume("Hello"));
        Assert.assertEquals(5, r.pos());
        Assert.assertFalse(r.matchConsume("world"));
        Assert.assertTrue(r.matchConsume(" World"));
        Assert.assertTrue(r.isEmpty());
    }

    @Test
    public void testMatchConsumeIgnoreCase() {
        CharacterReader r = new CharacterReader("Hello World");
        Assert.assertFalse(r.matchConsumeIgnoreCase("world"));
        Assert.assertEquals(0, r.pos());
        Assert.assertTrue(r.matchConsumeIgnoreCase("hello"));
        Assert.assertEquals(5, r.pos());
        Assert.assertTrue(r.matchConsumeIgnoreCase(" WORLD"));
        Assert.assertTrue(r.isEmpty());
    }

    @Test
    public void testContainsIgnoreCase() {
        CharacterReader r = new CharacterReader("<html><title>Test</title><STYLE>foo</STYLE></html>");
        Assert.assertTrue(r.containsIgnoreCase("</title>"));
        Assert.assertTrue(r.containsIgnoreCase("</TITLE>"));
        Assert.assertTrue(r.containsIgnoreCase("</style>"));
        Assert.assertTrue(r.containsIgnoreCase("</STYLE>"));
        Assert.assertFalse(r.containsIgnoreCase("</head>"));
    }

    @Test
    public void testCacheStringAndRangeEquals() {
        CharacterReader r = new CharacterReader("abc abc abc longstringovertwelvechars longstringovertwelvechars abc1 abc2");
        Assert.assertEquals("abc", r.consumeTo(' '));
        r.consume();
        Assert.assertEquals("abc", r.consumeTo(' '));
        r.consume();
        Assert.assertEquals("abc", r.consumeTo(' '));
        r.consume();
        Assert.assertEquals("longstringovertwelvechars", r.consumeTo(' '));
        r.consume();
        Assert.assertEquals("longstringovertwelvechars", r.consumeTo(' '));
        r.consume();

        Assert.assertTrue(r.rangeEquals(0, 3, "abc"));
        Assert.assertFalse(r.rangeEquals(0, 4, "abc"));
        Assert.assertFalse(r.rangeEquals(0, 3, "abd"));
    }
}
