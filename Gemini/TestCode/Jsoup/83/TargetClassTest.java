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
    public void testConstructorsAndValidation() {
        CharacterReader r1 = new CharacterReader("test");
        Assert.assertEquals("test", r1.toString());

        CharacterReader r2 = new CharacterReader(new StringReader("readerTest"));
        Assert.assertEquals("readerTest", r2.toString());

        CharacterReader r3 = new CharacterReader(new StringReader("szTest"), 100);
        Assert.assertEquals("szTest", r3.toString());

        CharacterReader r4 = new CharacterReader(new StringReader("largeSz"), CharacterReader.maxBufferLen + 100);
        Assert.assertEquals("largeSz", r4.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullReaderThrowsException() {
        new CharacterReader((Reader) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonMarkSupportedReaderThrowsException() {
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
    public void testBufferUpIOException() {
        Reader throwingReader = new Reader() {
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("Simulated read error");
            }

            @Override
            public void close() {
            }

            @Override
            public boolean markSupported() {
                return true;
            }
        };
        new CharacterReader(throwingReader);
    }

    @Test
    public void testEmptyAndCurrentAndPos() {
        CharacterReader r = new CharacterReader("abc");
        Assert.assertEquals(0, r.pos());
        Assert.assertFalse(r.isEmpty());
        Assert.assertEquals('a', r.current());
        Assert.assertEquals('a', r.consume());

        Assert.assertEquals(1, r.pos());
        Assert.assertEquals('b', r.current());
        r.advance();

        Assert.assertEquals(2, r.pos());
        Assert.assertEquals('c', r.consume());

        Assert.assertEquals(3, r.pos());
        Assert.assertTrue(r.isEmpty());
        Assert.assertEquals(CharacterReader.EOF, r.current());
        Assert.assertEquals(CharacterReader.EOF, r.consume());
    }

    @Test
    public void testUnconsumeAndMarkRewind() {
        CharacterReader r = new CharacterReader("abcdef");
        Assert.assertEquals('a', r.consume());
        r.mark();
        Assert.assertEquals('b', r.consume());
        Assert.assertEquals('c', r.consume());
        r.unconsume();
        Assert.assertEquals('c', r.current());
        r.rewindToMark();
        Assert.assertEquals('b', r.current());
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
    public void testNextIndexOfCharSequence() {
        CharacterReader r = new CharacterReader("one two three two one");
        Assert.assertEquals(4, r.nextIndexOf("two"));
        Assert.assertEquals(-1, r.nextIndexOf("four"));
        Assert.assertEquals(-1, r.nextIndexOf("one two three two one extra long string"));
        r.consume();
        Assert.assertEquals(3, r.nextIndexOf("two"));
    }

    @Test
    public void testConsumeToChar() {
        CharacterReader r = new CharacterReader("first/second/third");
        Assert.assertEquals("first", r.consumeTo('/'));
        Assert.assertEquals('/', r.consume());
        Assert.assertEquals("second", r.consumeTo('/'));
        Assert.assertEquals('/', r.consume());
        Assert.assertEquals("third", r.consumeTo('/'));
        Assert.assertTrue(r.isEmpty());
    }

    @Test
    public void testConsumeToString() {
        CharacterReader r = new CharacterReader("foo<!--comment-->bar");
        Assert.assertEquals("foo", r.consumeTo("<!--"));
        Assert.assertEquals("<!--", r.consumeTo("comment"));
        Assert.assertEquals("comment-->bar", r.consumeTo("missing"));
        Assert.assertTrue(r.isEmpty());
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
        CharacterReader r = new CharacterReader("Hello world&<" + TokeniserState.nullChar + "end");
        Assert.assertEquals("Hello world", r.consumeData());
        Assert.assertEquals('&', r.consume());
        Assert.assertEquals("", r.consumeData());
        Assert.assertEquals('<', r.consume());
        Assert.assertEquals("", r.consumeData());
        Assert.assertEquals(TokeniserState.nullChar, r.consume());
        Assert.assertEquals("end", r.consumeData());
    }

    @Test
    public void testConsumeTagName() {
        CharacterReader r = new CharacterReader("div\tspan\nbody\rp\fa b/c>d" + TokeniserState.nullChar + "end");
        Assert.assertEquals("div", r.consumeTagName());
        r.consume();
        Assert.assertEquals("span", r.consumeTagName());
        r.consume();
        Assert.assertEquals("body", r.consumeTagName());
        r.consume();
        Assert.assertEquals("p", r.consumeTagName());
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
    public void testConsumeSequences() {
        CharacterReader rLetters = new CharacterReader("abcXYZ123");
        Assert.assertEquals("abcXYZ", rLetters.consumeLetterSequence());
        Assert.assertEquals("123", rLetters.consumeDigitSequence());

        CharacterReader rUnicode = new CharacterReader("éàü123");
        Assert.assertEquals("éàü", rLetters.consumeLetterSequence().isEmpty() ? "" : "");
        Assert.assertEquals("éàü", rUnicode.consumeLetterSequence());

        CharacterReader rLetDig = new CharacterReader("abc123DEF456!#$");
        Assert.assertEquals("abc123DEF456", rLetDig.consumeLetterThenDigitSequence());
        Assert.assertEquals("", rLetDig.consumeLetterThenDigitSequence());

        CharacterReader rHex = new CharacterReader("0123456789abcdefABCDEFghijk");
        Assert.assertEquals("0123456789abcdefABCDEF", rHex.consumeHexSequence());
        Assert.assertEquals("", rHex.consumeHexSequence());
    }

    @Test
    public void testMatches() {
        CharacterReader r = new CharacterReader("abcdef");
        Assert.assertTrue(r.matches('a'));
        Assert.assertFalse(r.matches('b'));
        Assert.assertTrue(r.matches("abc"));
        Assert.assertFalse(r.matches("abd"));
        Assert.assertFalse(r.matches("abcdefghijk"));

        Assert.assertTrue(r.matchesIgnoreCase("ABC"));
        Assert.assertFalse(r.matchesIgnoreCase("ABD"));
        Assert.assertFalse(r.matchesIgnoreCase("ABCDEFHIJK"));

        Assert.assertTrue(r.matchesAny('x', 'a', 'z'));
        Assert.assertFalse(r.matchesAny('x', 'y', 'z'));

        char[] sorted = new char[]{'a', 'm', 'z'};
        Assert.assertTrue(r.matchesAnySorted(sorted));
        char[] sortedNo = new char[]{'b', 'm', 'z'};
        Assert.assertFalse(r.matchesAnySorted(sortedNo));

        Assert.assertTrue(r.matchesLetter());
        Assert.assertFalse(r.matchesDigit());

        CharacterReader rDigit = new CharacterReader("9abc");
        Assert.assertTrue(rDigit.matchesDigit());
        Assert.assertFalse(rDigit.matchesLetter());

        CharacterReader rSpecial = new CharacterReader("!@#");
        Assert.assertFalse(rSpecial.matchesLetter());
        Assert.assertFalse(rSpecial.matchesDigit());

        CharacterReader empty = new CharacterReader("");
        Assert.assertFalse(empty.matches('a'));
        Assert.assertFalse(empty.matches("a"));
        Assert.assertFalse(empty.matchesIgnoreCase("a"));
        Assert.assertFalse(empty.matchesAny('a'));
        Assert.assertFalse(empty.matchesAnySorted(sorted));
        Assert.assertFalse(empty.matchesLetter());
        Assert.assertFalse(empty.matchesDigit());
    }

    @Test
    public void testMatchConsume() {
        CharacterReader r = new CharacterReader("HelloWorld");
        Assert.assertFalse(r.matchConsume("world"));
        Assert.assertTrue(r.matchConsume("Hello"));
        Assert.assertEquals("World", r.toString());

        Assert.assertFalse(r.matchConsumeIgnoreCase("earth"));
        Assert.assertTrue(r.matchConsumeIgnoreCase("WORLD"));
        Assert.assertTrue(r.isEmpty());
    }

    @Test
    public void testContainsIgnoreCase() {
        CharacterReader r = new CharacterReader("<html><head><Title>Test</TITLE></head></html>");
        Assert.assertTrue(r.containsIgnoreCase("<title>"));
        Assert.assertTrue(r.containsIgnoreCase("</TITLE>"));
        Assert.assertFalse(r.containsIgnoreCase("<body"));
    }

    @Test
    public void testBufferUpRefill() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 40000; i++) {
            sb.append((char) ('a' + (i % 26)));
        }
        CharacterReader r = new CharacterReader(sb.toString());
        for (int i = 0; i < 40000; i++) {
            Assert.assertEquals(i, r.pos());
            Assert.assertEquals((char) ('a' + (i % 26)), r.consume());
        }
        Assert.assertTrue(r.isEmpty());
        Assert.assertEquals(CharacterReader.EOF, r.current());
    }

    @Test
    public void testStringCacheAndRangeEquals() {
        CharacterReader r = new CharacterReader("one one one longstringlongstring");
        String s1 = r.consumeTo(' ');
        r.consume();
        String s2 = r.consumeTo(' ');
        r.consume();
        String s3 = r.consumeTo(' ');
        r.consume();
        String s4 = r.consumeTo(' ');

        Assert.assertSame(s1, s2);
        Assert.assertSame(s2, s3);
        Assert.assertEquals("longstringlongstring", s4);

        char[] chars = "abcdef".toCharArray();
        Assert.assertTrue(CharacterReader.rangeEquals(chars, 0, 3, "abc"));
        Assert.assertFalse(CharacterReader.rangeEquals(chars, 0, 3, "abd"));
        Assert.assertFalse(CharacterReader.rangeEquals(chars, 0, 2, "abc"));

        CharacterReader rInstance = new CharacterReader("abcdef");
        Assert.assertTrue(rInstance.rangeEquals(0, 3, "abc"));
        Assert.assertFalse(rInstance.rangeEquals(0, 3, "xyz"));
    }

    @Test
    public void testCacheStringCollisionAndEmpty() {
        CharacterReader r = new CharacterReader("a b a b");
        String a1 = r.consumeTo(' ');
        r.consume();
        String b1 = r.consumeTo(' ');
        r.consume();
        String a2 = r.consumeTo(' ');
        r.consume();
        String b2 = r.consumeTo(' ');

        Assert.assertEquals("a", a1);
        Assert.assertEquals("b", b1);
        Assert.assertEquals("a", a2);
        Assert.assertEquals("b", b2);
        Assert.assertEquals("", r.consumeTo(' '));
    }
}
