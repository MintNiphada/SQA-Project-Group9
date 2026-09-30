package org.apache.commons.lang3.text.translate;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

public class LookupTranslatorTest {

    @Test
    public void testBasicLookup() throws IOException {
        final CharSequence[][] lookup = new CharSequence[][] {
            { "one", "TWO" },
            { "three", "FOUR" }
        };
        final LookupTranslator lt = new LookupTranslator(lookup);
        final StringWriter out = new StringWriter();

        final int resultOne = lt.translate("one", 0, out);
        Assert.assertEquals(3, resultOne);
        Assert.assertEquals("TWO", out.toString());

        final StringWriter out2 = new StringWriter();
        final int resultThree = lt.translate("three", 0, out2);
        Assert.assertEquals(5, resultThree);
        Assert.assertEquals("FOUR", out2.toString());
    }

    @Test
    public void testNoMatch() throws IOException {
        final CharSequence[][] lookup = new CharSequence[][] {
            { "foo", "bar" }
        };
        final LookupTranslator lt = new LookupTranslator(lookup);
        final StringWriter out = new StringWriter();

        final int result = lt.translate("baz", 0, out);
        Assert.assertEquals(0, result);
        Assert.assertEquals("", out.toString());
    }

    @Test
    public void testGreedyMatching() throws IOException {
        final CharSequence[][] lookup = new CharSequence[][] {
            { "ab", "1" },
            { "abc", "2" },
            { "abcd", "3" }
        };
        final LookupTranslator lt = new LookupTranslator(lookup);
        final StringWriter out = new StringWriter();

        final int result = lt.translate("abcdef", 0, out);
        Assert.assertEquals(4, result);
        Assert.assertEquals("3", out.toString());
    }

    @Test
    public void testShortestToLongestBoundaries() throws IOException {
        final CharSequence[][] lookup = new CharSequence[][] {
            { "cat", "dog" },
            { "caterpillar", "butterfly" }
        };
        final LookupTranslator lt = new LookupTranslator(lookup);

        // Exact match with longest
        StringWriter out = new StringWriter();
        int result = lt.translate("caterpillar", 0, out);
        Assert.assertEquals(11, result);
        Assert.assertEquals("butterfly", out.toString());

        // Exact match with shortest
        out = new StringWriter();
        result = lt.translate("cat", 0, out);
        Assert.assertEquals(3, result);
        Assert.assertEquals("dog", out.toString());

        // Shorter than shortest match length
        out = new StringWriter();
        result = lt.translate("ca", 0, out);
        Assert.assertEquals(0, result);
        Assert.assertEquals("", out.toString());
    }

    @Test
    public void testTranslateWithOffsetIndex() throws IOException {
        final CharSequence[][] lookup = new CharSequence[][] {
            { "world", "earth" }
        };
        final LookupTranslator lt = new LookupTranslator(lookup);
        final StringWriter out = new StringWriter();

        final int result = lt.translate("hello world", 6, out);
        Assert.assertEquals(5, result);
        Assert.assertEquals("earth", out.toString());
    }

    @Test
    public void testIndexNearEnd() throws IOException {
        final CharSequence[][] lookup = new CharSequence[][] {
            { "longword", "short" }
        };
        final LookupTranslator lt = new LookupTranslator(lookup);
        final StringWriter out = new StringWriter();

        // index + longest > input.length()
        final int result = lt.translate("word", 0, out);
        Assert.assertEquals(0, result);
        Assert.assertEquals("", out.toString());
    }

    @Test
    public void testNullLookupArray() throws IOException {
        final LookupTranslator lt = new LookupTranslator((CharSequence[][]) null);
        final StringWriter out = new StringWriter();
        final int result = lt.translate("test", 0, out);
        Assert.assertEquals(0, result);
        Assert.assertEquals("", out.toString());
    }

    @Test
    public void testEmptyLookupArray() throws IOException {
        final LookupTranslator lt = new LookupTranslator(new CharSequence[][] {});
        final StringWriter out = new StringWriter();
        final int result = lt.translate("test", 0, out);
        Assert.assertEquals(0, result);
        Assert.assertEquals("", out.toString());
    }

    @Test
    public void testInheritedTranslateMethod() {
        final CharSequence[][] lookup = new CharSequence[][] {
            { "red", "blue" },
            { "green", "yellow" }
        };
        final LookupTranslator lt = new LookupTranslator(lookup);

        final String result = lt.translate("the red car and green grass");
        Assert.assertEquals("the blue car and yellow grass", result);
    }

    @Test
    public void testInheritedTranslateNullInput() {
        final LookupTranslator lt = new LookupTranslator(new CharSequence[][] { { "a", "b" } });
        Assert.assertNull(lt.translate(null));
    }

    @Test
    public void testDifferentCharSequenceTypes() throws IOException {
        final StringBuffer key = new StringBuffer("key");
        final StringBuilder value = new StringBuilder("val");
        final LookupTranslator lt = new LookupTranslator(new CharSequence[][] { { key.toString(), value } });

        final StringWriter out = new StringWriter();
        final int result = lt.translate(new StringBuilder("key123"), 0, out);
        Assert.assertEquals(3, result);
        Assert.assertEquals("val", out.toString());
    }

    @Test
    public void testMultipleSequentialMatches() {
        final CharSequence[][] lookup = new CharSequence[][] {
            { "A", "1" },
            { "B", "2" },
            { "C", "3" }
        };
        final LookupTranslator lt = new LookupTranslator(lookup);
        Assert.assertEquals("123", lt.translate("ABC"));
    }
}
