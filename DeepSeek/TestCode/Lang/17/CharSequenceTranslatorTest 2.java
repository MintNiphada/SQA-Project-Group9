package org.apache.commons.lang3.text.translate;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.junit.Test;

public class CharSequenceTranslatorTest {

    // ---- helper translators for testing ----
    /** A translator that always returns 0 (no consumption), so default output is the input character. */
    private static class NoOpTranslator extends CharSequenceTranslator {
        @Override
        public int translate(CharSequence input, int index, Writer out) throws IOException {
            return 0;
        }
    }

    /** A translator that replaces every code point with 'X' and returns 1. */
    private static class ReplaceWithXTranslator extends CharSequenceTranslator {
        @Override
        public int translate(CharSequence input, int index, Writer out) throws IOException {
            out.write('X');
            return 1;
        }
    }

    /**
     * A translator that for a given position returns a configurable consumed value,
     * optionally writing a replacement string to the writer.
     */
    private static class ConsumingTranslator extends CharSequenceTranslator {
        private final int consumed;
        private final String replacement;

        ConsumingTranslator(int consumed, String replacement) {
            this.consumed = consumed;
            this.replacement = replacement;
        }

        @Override
        public int translate(CharSequence input, int index, Writer out) throws IOException {
            if (index < input.length() && replacement != null) {
                out.write(replacement);
            }
            return consumed;
        }
    }

    /** A writer that always throws IOException when any write method is called. */
    private static class ThrowingWriter extends Writer {
        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            throw new IOException("throwing writer");
        }

        @Override
        public void flush() throws IOException {
            throw new IOException("throwing writer");
        }

        @Override
        public void close() throws IOException {
            throw new IOException("throwing writer");
        }
    }

    // ---- translate(CharSequence) tests ----

    @Test
    public void testTranslateNullInput() {
        assertNull(new NoOpTranslator().translate(null));
    }

    @Test
    public void testTranslateEmpty() {
        assertEquals("", new NoOpTranslator().translate(""));
    }

    @Test
    public void testTranslateNoConsumption() {
        // translator never consumes -> output equals input
        CharSequenceTranslator t = new NoOpTranslator();
        assertEquals("abc", t.translate("abc"));
        // with supplementary characters (surrogate pair)
        String supp = "a\uD83D\uDE00b"; // a + grinning face + b
        assertEquals(supp, t.translate(supp));
    }

    @Test
    public void testTranslateAllConsumedReplaced() {
        // translator consumes every code point and writes X
        CharSequenceTranslator t = new ReplaceWithXTranslator();
        assertEquals("XXX", t.translate("abc"));
        // including supplementary: two code points 'a' and emoji -> both replaced with X -> "XX"
        assertEquals("XX", t.translate("a" + "\uD83D\uDE00"));
    }

    @Test
    public void testTranslateConsumesTwo() {
        // consumes two code points, replacing them with "YY"
        CharSequenceTranslator t = new ConsumingTranslator(2, "YY");
        assertEquals("YYc", t.translate("abc"));
    }

    @Test
    public void testTranslateConsumesTwoAtEnd() {
        // input exactly two code points, both consumed
        CharSequenceTranslator t = new ConsumingTranslator(2, "YY");
        assertEquals("YY", t.translate("ab"));
    }

    @Test
    public void testTranslateConsumesTwoWithSurrogates() {
        // input: first code point is surrogate pair, second is 'b' (so len=2, chars=3)
        String input = "\uD83D\uDE00b";
        // consuming 2 code points, writing "YY"
        CharSequenceTranslator t = new ConsumingTranslator(2, "YY");
        assertEquals("YY", t.translate(input));
    }

    @Test
    public void testTranslateConsumesTwoInMiddle() {
        // input "abcd" len=4; consume first two, replace with "YY", rest passed through
        CharSequenceTranslator t = new ConsumingTranslator(2, "YY") {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                if (index == 0) {
                    return super.translate(input, index, out);
                }
                return 0; // for remaining positions, do not consume
            }
        };
        assertEquals("YYcd", t.translate("abcd"));
    }

    @Test
    public void testTranslateConsumesTwoFromMiddle() {
        // consume two starting at index 1, replace with "YY"
        // input "abcde" len=5
        CharSequenceTranslator t = new ConsumingTranslator(2, "YY") {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                if (index == 1) {
                    return super.translate(input, index, out);
                }
                return 0;
            }
        };
        assertEquals("aYYde", t.translate("abcde"));
    }

    @Test
    public void testTranslateSingleSurrogateConsumed() {
        // input: lone surrogate? Actually codePointCount will treat unpaired surrogate as one code point (U+FFFD? but codePointAt returns the surrogate itself). 
        // We'll just use a regular supplementary char at start. consume 1 code point, replace with "Z"
        String input = "\uD83D\uDE00x";
        CharSequenceTranslator t = new ConsumingTranslator(1, "Z");
        assertEquals("Zx", t.translate(input));
    }

    // ---- translate(CharSequence, Writer) tests ----

    @Test(expected = IllegalArgumentException.class)
    public void testTranslateWriterNull() throws IOException {
        new NoOpTranslator().translate("abc", null);
    }

    @Test
    public void testTranslateWriterInputNull() throws IOException {
        StringWriter out = new StringWriter();
        new NoOpTranslator().translate(null, out);
        assertEquals("", out.toString()); // nothing written
    }

    @Test
    public void testTranslateWriterNoConsumption() throws IOException {
        StringWriter out = new StringWriter();
        new NoOpTranslator().translate("abc", out);
        assertEquals("abc", out.toString());
    }

    @Test
    public void testTranslateWriterReplacement() throws IOException {
        StringWriter out = new StringWriter();
        new ReplaceWithXTranslator().translate("abc", out);
        assertEquals("XXX", out.toString());
    }

    @Test
    public void testTranslateWriterWithSpecialCharacters() throws IOException {
        // includes tab, newline, etc. – no consumption
        String input = "a\tb\nc";
        StringWriter out = new StringWriter();
        new NoOpTranslator().translate(input, out);
        assertEquals(input, out.toString());
    }

    @Test
    public void testTranslateWriterIOExceptionPropagates() throws IOException {
        Writer out = new ThrowingWriter();
        try {
            new ReplaceWithXTranslator().translate("a", out);
            fail("Expected IOException");
        } catch (IOException e) {
            // expected
        }
    }

    @Test
    public void testTranslateWriterConsumesTwoWithSurrogate() throws IOException {
        String input = "\uD83D\uDE00b"; // len=2
        StringWriter out = new StringWriter();
        CharSequenceTranslator t = new ConsumingTranslator(2, "YY");
        t.translate(input, out);
        assertEquals("YY", out.toString());
    }

    @Test
    public void testTranslateWriterMultipleConsumptions() throws IOException {
        // a translator that consumes 1 for first char, 2 for the next, 0 for rest.
        // input "abcdef" len=6
        StringWriter out = new StringWriter();
        CharSequenceTranslator t = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                if (index == 0) { out.write("A"); return 1; }
                else if (index == 1) { out.write("BC"); return 2; }
                else return 0;
            }
        };
        t.translate("abcdef", out);
        assertEquals("ABCdef", out.toString());
    }

    @Test
    public void testTranslateWriterPosUpdateWithLengthTwo() throws IOException {
        // input length 2 in code points (e.g., "ab"), consumed=1 for first, positions advance correctly
        StringWriter out = new StringWriter();
        CharSequenceTranslator t = new ReplaceWithXTranslator(); // returns 1 and writes X
        t.translate("ab", out);
        assertEquals("XX", out.toString());
    }

    @Test
    public void testTranslateWriterPosUpdateWithLengthThree() throws IOException {
        // len=3, no consumption -> output unchanged
        StringWriter out = new StringWriter();
        new NoOpTranslator().translate("abc", out);
        assertEquals("abc", out.toString());
    }

    @Test
    public void testTranslateWriterConsumeExactlyLastCodePoint() throws IOException {
        // input "a" (len=1), consum=1 and replace with "Z"
        StringWriter out = new StringWriter();
        CharSequenceTranslator t = new ConsumingTranslator(1, "Z");
        t.translate("a", out);
        assertEquals("Z", out.toString());
    }

    // ---- with(CharSequenceTranslator...) tests ----

    @Test
    public void testWithNoAdditionalTranslators() {
        CharSequenceTranslator t = new NoOpTranslator();
        CharSequenceTranslator merged = t.with();
        assertNotNull(merged);
        // should behave the same as the original
        assertEquals("abc", merged.translate("abc"));
    }

    @Test
    public void testWithOneAdditionalTranslator() {
        CharSequenceTranslator noop = new NoOpTranslator();
        CharSequenceTranslator replacer = new ReplaceWithXTranslator();
        CharSequenceTranslator merged = noop.with(replacer);
        // the merger should apply replacer (since noop does nothing)
        assertEquals("X", merged.translate("a"));
    }

    @Test
    public void testWithMultipleAdditionalTranslators() {
        CharSequenceTranslator noop = new NoOpTranslator();
        CharSequenceTranslator t1 = new ConsumingTranslator(1, "1");
        CharSequenceTranslator t2 = new ConsumingTranslator(1, "2");
        CharSequenceTranslator merged = noop.with(t1, t2);
        // AggregateTranslator likely applies translators in order; first non-zero consumed wins.
        // Since noop returns 0, t1 will be applied next and consume, so output should be "1".
        assertEquals("1", merged.translate("a"));
    }

    @Test
    public void testWithReturnsAggregate() {
        CharSequenceTranslator t = new NoOpTranslator();
        CharSequenceTranslator merged = t.with(new ReplaceWithXTranslator());
        assertTrue(merged instanceof AggregateTranslator);
    }

    // ---- hex(int) tests ----

    @Test
    public void testHexZero() {
        assertEquals("0", CharSequenceTranslator.hex(0));
    }

    @Test
    public void testHexPositive() {
        assertEquals("A", CharSequenceTranslator.hex(10));
        assertEquals("FF", CharSequenceTranslator.hex(255));
        assertEquals("100", CharSequenceTranslator.hex(256));
    }

    @Test
    public void testHexNegative() {
        // integer negative uses two's complement -> unsigned representation
        assertEquals("FFFFFFFF", CharSequenceTranslator.hex(-1));
        assertEquals("FFFFFFFE", CharSequenceTranslator.hex(-2));
        assertEquals("80000000", CharSequenceTranslator.hex(Integer.MIN_VALUE));
    }

    @Test
    public void testHexUppercase() {
        assertEquals("1A", CharSequenceTranslator.hex(26));
        assertFalse(CharSequenceTranslator.hex(26).contains("a"));
    }
}
