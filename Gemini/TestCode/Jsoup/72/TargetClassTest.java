package org.jsoup.parser;

import org.jsoup.UncheckedIOException;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;

public class CharacterReaderTest {

    @Test
    public void testConstructorsAndPos() {
        CharacterReader r1 = new CharacterReader("hello");
        Assert.assertEquals(0, r1.pos());
        Assert.assertFalse(r1.isEmpty());
        Assert.assertEquals("hello", r1.toString());

        CharacterReader r2 = new CharacterReader(new StringReader("world"));
        Assert.assertEquals('w', r2.current());

        CharacterReader r3 = new CharacterReader(new StringReader("test"), 2);
        Assert.assertEquals('t', r3.consume());
        Assert.assertEquals(1, r3.pos());

        CharacterReader r4 = new CharacterReader(new StringReader("big"), CharacterReader.maxBufferLen + 10);
        Assert.assertEquals('b', r4.consume());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullReaderThrows() {
        new CharacterReader((Reader) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnmarkableReaderThrows() {
        Reader unmarkable = new Reader() {
            @Override
            public int read(char[] cbuf, int off, int len) {
                return -1;
            }

            @Override
            public void close() {
            }

            @Override
            public boolean markSupported() {
                return false;
            }
        };
        new CharacterReader(unmarkable);
    }

    @Test(expected = UncheckedIOException.class)
    public void testReaderIOExceptionThrowsUnchecked() {
        Reader errorReader = new Reader() {
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("read error");
            }

            @Override
            public void close() {
            }

            @Override
            public boolean markSupported() {
                return true;
            }
        };
        new CharacterReader(errorReader);
    }

    @Test
    public void testConsumeAndUnconsumeAndAdvance() {
        CharacterReader r = new CharacterReader("abc");
        Assert.assertEquals('a', r.current());
        Assert.assertEquals('a', r.consume());
        Assert.assertEquals('b', r.consume());
        r.unconsume();
        Assert.assertEquals('b', r.consume());
        r.advance();
        Assert.assertTrue(r.isEmpty());
        Assert.assertEquals(CharacterReader.EOF, r.current());
        Assert.assertEquals(CharacterReader.EOF, r.consume());
    }

    @Test
    public void testMarkAndRewind() {
        CharacterReader r = new CharacterReader("abcdef");
        r.consume();
        r.consume();
        r.mark();
        Assert.assertEquals('c', r.consume());
        Assert.assertEquals('d', r.consume());
        r.rewindToMark();
        Assert.assertEquals('c', r.consume());
    }

    @Test
    public void testNextIndexOfChar() {
        CharacterReader r = new CharacterReader("one two three");
        Assert.assertEquals(3, r.nextIndexOf(' '));
        Assert.assertEquals(-1, r.nextIndexOf('z'));
        r.consumeTo('t');
        Assert.assertEquals(0, r.nextIndexOf('t'));
    }

    @Test
    public void testNextIndexOfSeq() {
        CharacterReader r = new CharacterReader("abcdefgh abcxyz");
        Assert.assertEquals(4, r.nextIndexOf("efg"));
        Assert.assertEquals(-1, r.nextIndexOf("notfound"));
        Assert.assertEquals(-1, r.nextIndexOf("abcdefgh abcxyz extra"));
        Assert.assertEquals(0, r.nextIndexOf("abc"));
        r.consume();
        Assert.assertEquals(8, r.nextIndexOf("abc"));
    }

    @Test
    public void testConsumeToChar() {
        CharacterReader r = new CharacterReader("foo=bar;");
        Assert.assertEquals("foo", r.consumeTo('='));
        Assert.assertEquals('=', r.consume());
        Assert.assertEquals("bar", r.consumeTo(';'));
        Assert.assertEquals(';', r.consume());
        Assert.assertEquals("", r.consumeTo(';'));
    }

    @Test
    public void testConsumeToString() {
        CharacterReader r = new CharacterReader("hello <b>world</b> end");
        Assert.assertEquals("hello ", r.consumeTo("<b>"));
        Assert.assertEquals("<b>world</b>", r.consumeTo(" end"));
        Assert.assertEquals(" end", r.consumeTo("nonexistent"));
        Assert.assertTrue(r.isEmpty());
    }

    @Test
    public void testConsumeToAny() {
        CharacterReader r = new CharacterReader("foo&bar<baz");
        Assert.assertEquals("foo", r.consumeToAny('&', '<'));
        Assert.assertEquals('&', r.consume());
        Assert.assertEquals("bar", r.consumeToAny('&', '<'));
        Assert.assertEquals('<', r.consume());
        Assert.assertEquals("baz", r.consumeToAny('&', '<'));
        Assert.assertEquals("", r.consumeToAny('&', '<'));
    }

    @Test
    public void testConsumeToAnySorted() {
        char[] sortedDelims = new char[]{'&', '<'};
        Arrays.sort(sortedDelims);
        CharacterReader r = new CharacterReader("foo&bar<baz");
        Assert.assertEquals("foo", r.consumeToAnySorted(sortedDelims));
        Assert.assertEquals('&', r.consume());
        Assert.assertEquals("bar", r.consumeToAnySorted(sortedDelims));
        Assert.assertEquals('<', r.consume());
        Assert.assertEquals("baz", r.consumeToAnySorted(sortedDelims));
        Assert.assertEquals("", r.consumeToAnySorted(sortedDelims));
    }

    @Test
    public void testConsumeData() {
        CharacterReader r = new CharacterReader("Some text & more < tag \0 null");
        Assert.assertEquals("Some text ", r.consumeData());
        Assert.assertEquals('&', r.consume());
        Assert.assertEquals(" more ", r.consumeData());
        Assert.assertEquals('<', r.consume());
        Assert.assertEquals(" tag ", r.consumeData());
        Assert.assertEquals('\0', r.consume());
        Assert.assertEquals(" null", r.consumeData());
        Assert.assertEquals("", r.consumeData());
    }

    @Test
    public void testConsumeTagName() {
        CharacterReader r = new CharacterReader("div \t\n\r\f/>\0span");
        Assert.assertEquals("div", r.consumeTagName());
        Assert.assertEquals("", r.consumeTagName());
        r.consume();
        Assert.assertEquals("", r.consumeTagName());
    }

    @Test
    public void testConsumeToEnd() {
        CharacterReader r = new CharacterReader("abcdef");
        r.consume();
        Assert.assertEquals("bcdef", r.consumeToEnd());
        Assert.assertTrue(r.isEmpty());
        Assert.assertEquals("", r.consumeToEnd());
    }

    @Test
    public void testConsumeLetterSequence() {
        CharacterReader r = new CharacterReader("Hello World 123");
        Assert.assertEquals("Hello", r.consumeLetterSequence());
        Assert.assertEquals("", r.consumeLetterSequence());
        r.consume();
        Assert.assertEquals("World", r.consumeLetterSequence());
        r.consume();
        Assert.assertEquals("", r.consumeLetterSequence());
    }

    @Test
    public void testConsumeLetterThenDigitSequence() {
        CharacterReader r = new CharacterReader("abc123def 456");
        Assert.assertEquals("abc123", r.consumeLetterThenDigitSequence());
        Assert.assertEquals("def", r.consumeLetterThenDigitSequence());
        r.consume();
        Assert.assertEquals("", r.consumeLetterSequence());
        Assert.assertEquals("456", r.consumeDigitSequence());
    }

    @Test
    public void testConsumeHexSequence() {
        CharacterReader r = new CharacterReader("1a2F3z4");
        Assert.assertEquals("1a2F3", r.consumeHexSequence());
        Assert.assertEquals("", r.consumeHexSequence());
        r.consume();
        Assert.assertEquals("4", r.consumeHexSequence());
    }

    @Test
    public void testConsumeDigitSequence() {
        CharacterReader r = new CharacterReader("12345abc67");
        Assert.assertEquals("12345", r.consumeDigitSequence());
        Assert.assertEquals("", r.consumeDigitSequence());
        r.consumeLetterSequence();
        Assert.assertEquals("67", r.consumeDigitSequence());
    }

    @Test
    public void testMatches() {
        CharacterReader r = new CharacterReader("Testing");
        Assert.assertTrue(r.matches('T'));
        Assert.assertFalse(r.matches('e'));
        Assert.assertTrue(r.matches("Test"));
        Assert.assertFalse(r.matches("Testing long extra"));
        Assert.assertFalse(r.matches("Toast"));
        r.consumeToEnd();
        Assert.assertFalse(r.matches('T'));
        Assert.assertFalse(r.matches("Test"));
    }

    @Test
    public void testMatchesIgnoreCase() {
        CharacterReader r = new CharacterReader("Testing");
        Assert.assertTrue(r.matchesIgnoreCase("test"));
        Assert.assertTrue(r.matchesIgnoreCase("TESTING"));
        Assert.assertFalse(r.matchesIgnoreCase("Testing long extra"));
        Assert.assertFalse(r.matchesIgnoreCase("Toast"));
        r.consumeToEnd();
        Assert.assertFalse(r.matchesIgnoreCase("Test"));
    }

    @Test
    public void testMatchesAny() {
        CharacterReader r = new CharacterReader("Testing");
        Assert.assertTrue(r.matchesAny('a', 'b', 'T'));
        Assert.assertFalse(r.matchesAny('a', 'b', 'c'));
        r.consumeToEnd();
        Assert.assertFalse(r.matchesAny('a', 'b', 'T'));
    }

    @Test
    public void testMatchesAnySorted() {
        char[] sorted = new char[]{'T', 'a', 'x'};
        Arrays.sort(sorted);
        CharacterReader r = new CharacterReader("Testing");
        Assert.assertTrue(r.matchesAnySorted(sorted));
        char[] notMatched = new char[]{'a', 'b', 'c'};
        Assert.assertFalse(r.matchesAnySorted(notMatched));
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
        CharacterReader r = new CharacterReader("HelloWorld");
        Assert.assertFalse(r.matchConsume("HelloThere"));
        Assert.assertTrue(r.matchConsume("Hello"));
        Assert.assertFalse(r.matchConsume("world"));
        Assert.assertTrue(r.matchConsume("World"));
        Assert.assertFalse(r.matchConsume("anything"));
    }

    @Test
    public void testMatchConsumeIgnoreCase() {
        CharacterReader r = new CharacterReader("HelloWorld");
        Assert.assertFalse(r.matchConsumeIgnoreCase("HelloThere"));
        Assert.assertTrue(r.matchConsumeIgnoreCase("hello"));
        Assert.assertTrue(r.matchConsumeIgnoreCase("WORLD"));
        Assert.assertFalse(r.matchConsumeIgnoreCase("anything"));
    }

    @Test
    public void testContainsIgnoreCase() {
        CharacterReader r = new CharacterReader("<html><TITLE>Test</TITLE></html>");
        Assert.assertTrue(r.containsIgnoreCase("</title>"));
        Assert.assertTrue(r.containsIgnoreCase("</TITLE>"));
        Assert.assertFalse(r.containsIgnoreCase("</head>"));
    }

    @Test
    public void testRangeEquals() {
        CharacterReader r = new CharacterReader("abcdef");
        Assert.assertTrue(r.rangeEquals(0, 3, "abc"));
        Assert.assertFalse(r.rangeEquals(0, 3, "abcd"));
        Assert.assertFalse(r.rangeEquals(0, 3, "abd"));
        Assert.assertFalse(r.rangeEquals(0, 2, "abc"));
    }

    @Test
    public void testBufferUpPagination() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5000; i++) {
            sb.append("abcdefghij");
        }
        CharacterReader r = new CharacterReader(new StringReader(sb.toString()), 100);
        int count = 0;
        while (!r.isEmpty()) {
            char c = r.consume();
            if (c != CharacterReader.EOF) {
                count++;
            }
        }
        Assert.assertEquals(50000, count);
        Assert.assertEquals(50000, r.pos());
    }

    @Test
    public void testStringCachingAndCollisions() {
        CharacterReader r = new CharacterReader("tag tag tag longerthancachelength tag");
        String s1 = r.consumeTo(' ');
        r.consume();
        String s2 = r.consumeTo(' ');
        r.consume();
        String s3 = r.consumeTo(' ');
        r.consume();
        String longStr = r.consumeTo(' ');
        r.consume();
        String s4 = r.consumeToEnd();

        Assert.assertSame(s1, s2);
        Assert.assertSame(s1, s3);
        Assert.assertSame(s1, s4);
        Assert.assertEquals("longerthancachelength", longStr);
    }
}
