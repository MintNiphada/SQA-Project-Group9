package org.jsoup.parser;

import org.jsoup.UncheckedIOException;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;

public class CharacterReaderTest {

    @Test(expected = IllegalArgumentException.class)
    public void testNullReader() {
        new CharacterReader((Reader) null, 16);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnmarkedReader() {
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
        new CharacterReader(unmarkable, 16);
    }

    @Test(expected = UncheckedIOException.class)
    public void testReaderIOException() {
        Reader broken = new Reader() {
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("Simulated");
            }

            @Override
            public void close() {
            }

            @Override
            public boolean markSupported() {
                return true;
            }
        };
        new CharacterReader(broken, 16);
    }

    @Test
    public void testConstructorsAndPos() {
        CharacterReader r1 = new CharacterReader(new StringReader("abcdef"));
        Assert.assertEquals(0, r1.pos());
        Assert.assertEquals('a', r1.current());
        r1.advance();
        Assert.assertEquals(1, r1.pos());

        CharacterReader r2 = new CharacterReader("test");
        Assert.assertEquals(0, r2.pos());
        Assert.assertEquals("test", r2.toString());

        CharacterReader r3 = new CharacterReader(new StringReader("large"), CharacterReader.maxBufferLen + 100);
        Assert.assertEquals('l', r3.current());
    }

    @Test
    public void testEmptyAndConsume() {
        CharacterReader r = new CharacterReader("");
        Assert.assertTrue(r.isEmpty());
        Assert.assertEquals(CharacterReader.EOF, r.current());
        Assert.assertEquals(CharacterReader.EOF, r.consume());

        CharacterReader r2 = new CharacterReader("ab");
        Assert.assertFalse(r2.isEmpty());
        Assert.assertEquals('a', r2.consume());
        Assert.assertEquals('b', r2.consume());
        Assert.assertEquals(CharacterReader.EOF, r2.consume());
        Assert.assertTrue(r2.isEmpty());
    }

    @Test
    public void testUnconsume() {
        CharacterReader r = new CharacterReader("abc");
        r.consume();
        Assert.assertEquals('b', r.current());
        r.unconsume();
        Assert.assertEquals('a', r.current());
    }

    @Test(expected = UncheckedIOException.class)
    public void testUnconsumeUnderflow() {
        CharacterReader r = new CharacterReader("abc");
        r.unconsume();
    }

    @Test
    public void testMarkAndRewind() {
        CharacterReader r = new CharacterReader("abcdef");
        r.consume();
        r.mark();
        r.consume();
        r.consume();
        Assert.assertEquals('d', r.current());
        r.rewindToMark();
        Assert.assertEquals('b', r.current());
    }

    @Test(expected = UncheckedIOException.class)
    public void testRewindWithoutMark() {
        CharacterReader r = new CharacterReader("abcdef");
        r.rewindToMark();
    }

    @Test
    public void testNextIndexOfChar() {
        CharacterReader r = new CharacterReader("one two three");
        Assert.assertEquals(3, r.nextIndexOf(' '));
        Assert.assertEquals(-1, r.nextIndexOf('z'));
    }

    @Test
    public void testNextIndexOfSequence() {
        CharacterReader r = new CharacterReader("one two three");
        Assert.assertEquals(4, r.nextIndexOf("two"));
        Assert.assertEquals(-1, r.nextIndexOf("four"));
        Assert.assertEquals(-1, r.nextIndexOf("three!"));
        Assert.assertEquals(0, r.nextIndexOf("one"));
    }

    @Test
    public void testConsumeToChar() {
        CharacterReader r = new CharacterReader("foo&bar");
        Assert.assertEquals("foo", r.consumeTo('&'));
        Assert.assertEquals("&bar", r.toString());
        Assert.assertEquals("&bar", r.consumeTo('z'));
        Assert.assertTrue(r.isEmpty());
    }

    @Test
    public void testConsumeToSequence() {
        CharacterReader r = new CharacterReader("foo<!--comment-->bar");
        Assert.assertEquals("foo", r.consumeTo("<!--"));
        Assert.assertEquals("<!--comment-->bar", r.consumeTo("missing"));
        Assert.assertTrue(r.isEmpty());
    }

    @Test
    public void testConsumeToAny() {
        CharacterReader r = new CharacterReader("foo & bar < baz");
        Assert.assertEquals("foo ", r.consumeToAny('&', '<'));
        Assert.assertEquals("", r.consumeToAny('&', '<'));
        r.consume();
        Assert.assertEquals(" bar ", r.consumeToAny('&', '<'));
        r.consume();
        Assert.assertEquals(" baz", r.consumeToAny('&', '<'));
    }

    @Test
    public void testConsumeToAnySorted() {
        CharacterReader r = new CharacterReader("foo & bar < baz");
        char[] sortedDelims = new char[]{'&', '<'};
        Arrays.sort(sortedDelims);
        Assert.assertEquals("foo ", r.consumeToAnySorted(sortedDelims));
        r.consume();
        Assert.assertEquals(" bar ", r.consumeToAnySorted(sortedDelims));
    }

    @Test
    public void testConsumeData() {
        CharacterReader r = new CharacterReader("some text & more < end");
        Assert.assertEquals("some text ", r.consumeData());
        r.consume();
        Assert.assertEquals(" more ", r.consumeData());
        r.consume();
        Assert.assertEquals(" end", r.consumeData());

        CharacterReader rNull = new CharacterReader("null\0data");
        Assert.assertEquals("null", rNull.consumeData());
    }

    @Test
    public void testConsumeTagName() {
        CharacterReader r1 = new CharacterReader("div\tspan\r\n<foo/>");
        Assert.assertEquals("div", r1.consumeTagName());
        r1.advance();
        Assert.assertEquals("span", r1.consumeTagName());

        CharacterReader r2 = new CharacterReader("a/b>c\0d\fe ");
        Assert.assertEquals("a", r2.consumeTagName());
        r2.advance();
        Assert.assertEquals("b", r2.consumeTagName());
        r2.advance();
        Assert.assertEquals("c", r2.consumeTagName());
        r2.advance();
        Assert.assertEquals("d", r2.consumeTagName());
        r2.advance();
        Assert.assertEquals("e", r2.consumeTagName());
    }

    @Test
    public void testConsumeLetterSequence() {
        CharacterReader r = new CharacterReader("abcDEF123\u00E9g");
        Assert.assertEquals("abcDEF", r.consumeLetterSequence());
        Assert.assertEquals("123", r.consumeDigitSequence());
        Assert.assertEquals("\u00E9g", r.consumeLetterSequence());
    }

    @Test
    public void testConsumeLetterThenDigitSequence() {
        CharacterReader r = new CharacterReader("alpha123-next");
        Assert.assertEquals("alpha123", r.consumeLetterThenDigitSequence());
        Assert.assertEquals("-next", r.toString());

        CharacterReader rEmpty = new CharacterReader("");
        Assert.assertEquals("", rEmpty.consumeLetterThenDigitSequence());
    }

    @Test
    public void testConsumeHexSequence() {
        CharacterReader r = new CharacterReader("1aF9zG0");
        Assert.assertEquals("1aF9", r.consumeHexSequence());
        Assert.assertEquals("zG0", r.toString());
    }

    @Test
    public void testConsumeDigitSequence() {
        CharacterReader r = new CharacterReader("98765abc");
        Assert.assertEquals("98765", r.consumeDigitSequence());
        Assert.assertEquals("abc", r.toString());
    }

    @Test
    public void testMatchesChar() {
        CharacterReader r = new CharacterReader("abc");
        Assert.assertTrue(r.matches('a'));
        Assert.assertFalse(r.matches('b'));
        r.consumeToEnd();
        Assert.assertFalse(r.matches('a'));
    }

    @Test
    public void testMatchesString() {
        CharacterReader r = new CharacterReader("abcdef");
        Assert.assertTrue(r.matches("abc"));
        Assert.assertFalse(r.matches("abd"));
        Assert.assertFalse(r.matches("abcdefg"));
    }

    @Test
    public void testMatchesIgnoreCase() {
        CharacterReader r = new CharacterReader("AbCdEf");
        Assert.assertTrue(r.matchesIgnoreCase("abc"));
        Assert.assertTrue(r.matchesIgnoreCase("ABCDEF"));
        Assert.assertFalse(r.matchesIgnoreCase("abx"));
        Assert.assertFalse(r.matchesIgnoreCase("abcdefg"));
    }

    @Test
    public void testMatchesAny() {
        CharacterReader r = new CharacterReader("abc");
        Assert.assertTrue(r.matchesAny('x', 'a', 'z'));
        Assert.assertFalse(r.matchesAny('x', 'y', 'z'));

        CharacterReader empty = new CharacterReader("");
        Assert.assertFalse(empty.matchesAny('a'));
    }

    @Test
    public void testMatchesAnySorted() {
        CharacterReader r = new CharacterReader("bac");
        char[] sorted = new char[]{'a', 'b', 'c'};
        Assert.assertTrue(r.matchesAnySorted(sorted));

        char[] none = new char[]{'x', 'y', 'z'};
        Assert.assertFalse(r.matchesAnySorted(none));

        CharacterReader empty = new CharacterReader("");
        Assert.assertFalse(empty.matchesAnySorted(sorted));
    }

    @Test
    public void testMatchesLetterAndDigit() {
        CharacterReader rLetters = new CharacterReader("aZ1 ");
        Assert.assertTrue(rLetters.matchesLetter());
        Assert.assertFalse(rLetters.matchesDigit());
        rLetters.advance();
        Assert.assertTrue(rLetters.matchesLetter());
        Assert.assertFalse(rLetters.matchesDigit());
        rLetters.advance();
        Assert.assertFalse(rLetters.matchesLetter());
        Assert.assertTrue(rLetters.matchesDigit());
        rLetters.advance();
        Assert.assertFalse(rLetters.matchesLetter());
        Assert.assertFalse(rLetters.matchesDigit());

        CharacterReader empty = new CharacterReader("");
        Assert.assertFalse(empty.matchesLetter());
        Assert.assertFalse(empty.matchesDigit());
    }

    @Test
    public void testMatchConsume() {
        CharacterReader r = new CharacterReader("hello world");
        Assert.assertTrue(r.matchConsume("hello"));
        Assert.assertEquals(" world", r.toString());
        Assert.assertFalse(r.matchConsume("planet"));
        Assert.assertEquals(" world", r.toString());
    }

    @Test
    public void testMatchConsumeIgnoreCase() {
        CharacterReader r = new CharacterReader("HELLO WORLD");
        Assert.assertTrue(r.matchConsumeIgnoreCase("hello"));
        Assert.assertEquals(" WORLD", r.toString());
        Assert.assertFalse(r.matchConsumeIgnoreCase("planet"));
        Assert.assertEquals(" WORLD", r.toString());
    }

    @Test
    public void testContainsIgnoreCase() {
        CharacterReader r = new CharacterReader("<html><TITLE>Test</TITLE><style>body{}</STYLE></html>");
        Assert.assertTrue(r.containsIgnoreCase("</title>"));
        Assert.assertTrue(r.containsIgnoreCase("</style>"));
        Assert.assertFalse(r.containsIgnoreCase("</script>"));
    }

    @Test
    public void testRangeEquals() {
        CharacterReader r = new CharacterReader("testing range equals");
        Assert.assertTrue(r.rangeEquals(0, 7, "testing"));
        Assert.assertFalse(r.rangeEquals(0, 7, "testing"));
        Assert.assertFalse(r.rangeEquals(0, 6, "testing"));
        Assert.assertFalse(r.rangeEquals(0, 7, "toastin"));
    }

    @Test
    public void testStringCachingAndCollisions() {
        CharacterReader r = new CharacterReader("tag tag tag aVeryLongTagNameExceedingMaxCacheLen tag");
        String t1 = r.consumeTo(' ');
        r.advance();
        String t2 = r.consumeTo(' ');
        r.advance();
        String t3 = r.consumeTo(' ');
        r.advance();
        String longTag = r.consumeTo(' ');
        r.advance();
        String t4 = r.consumeTo(' ');

        Assert.assertSame(t1, t2);
        Assert.assertSame(t2, t3);
        Assert.assertEquals("aVeryLongTagNameExceedingMaxCacheLen", longTag);
        Assert.assertEquals(t1, t4);
    }
}
