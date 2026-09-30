package org.apache.commons.lang3.text.translate;

import static org.junit.Assert.*;
import org.junit.Test;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Locale;

public class CharSequenceTranslatorTest {

    // Helper translators for testing

    private static class ZeroConsumedTranslator extends CharSequenceTranslator {
        @Override
        public int translate(CharSequence input, int index, Writer out) throws IOException {
            return 0;
        }
    }

    private static class FixedConsumedTranslator extends CharSequenceTranslator {
        private final int consumed;
        private final String toWrite;
        private boolean throwIOException = false;

        public FixedConsumedTranslator(int consumed) {
            this(consumed, null);
        }

        public FixedConsumedTranslator(int consumed, String toWrite) {
            this.consumed = consumed;
            this.toWrite = toWrite;
        }

        public void setThrowIOException(boolean throwIOException) {
            this.throwIOException = throwIOException;
        }

        @Override
        public int translate(CharSequence input, int index, Writer out) throws IOException {
            if (throwIOException) {
                throw new IOException("test");
            }
            if (toWrite != null) {
                out.write(toWrite);
            }
            return consumed;
        }
    }

    private static class ThrowingTranslator extends CharSequenceTranslator {
        @Override
        public int translate(CharSequence input, int index, Writer out) throws IOException {
            throw new IOException("test");
        }
    }

    // Tests for translate(CharSequence)

    @Test
    public void testTranslateNullInput() {
        assertNull(new ZeroConsumedTranslator().translate((CharSequence) null));
    }

    @Test
    public void testTranslateNormal() throws IOException {
        CharSequenceTranslator translator = new FixedConsumedTranslator(1, "X");
        String result = translator.translate("a");
        assertEquals("X", result);
    }

    @Test
    public void testTranslateWithIOException() {
        CharSequenceTranslator translator = new ThrowingTranslator();
        try {
            translator.translate("a");
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof IOException);
            assertEquals("test", e.getCause().getMessage());
        }
    }

    // Tests for translate(CharSequence, Writer)

    @Test(expected = IllegalArgumentException.class)
    public void testTranslateNullWriter() throws IOException {
        new ZeroConsumedTranslator().translate("abc", null);
    }

    @Test
    public void testTranslateNullInputToWriter() throws IOException {
        StringWriter writer = new StringWriter();
        new ZeroConsumedTranslator().translate((CharSequence) null, writer);
        assertEquals("", writer.toString());
    }

    @Test
    public void testTranslateEmptyInput() throws IOException {
        StringWriter writer = new StringWriter();
        new ZeroConsumedTranslator().translate("", writer);
        assertEquals("", writer.toString());
    }

    @Test
    public void testTranslateZeroConsumed() throws IOException {
        StringWriter writer = new StringWriter();
        new ZeroConsumedTranslator().translate("a", writer);
        assertEquals("a", writer.toString());
    }

    @Test
    public void testTranslateFixedConsumed() throws IOException {
        StringWriter writer = new StringWriter();
        new FixedConsumedTranslator(1, "X").translate("a", writer);
        assertEquals("X", writer.toString());
    }

    @Test
    public void testTranslateConsumedGreaterThanOne() throws IOException {
        StringWriter writer = new StringWriter();
        // input "ab", translate returns 2, writes "X", pos becomes 2, loop ends
        new FixedConsumedTranslator(2, "X").translate("ab", writer);
        assertEquals("X", writer.toString());
    }

    @Test
    public void testTranslateMixedConsumed() throws IOException {
        // Use a stateful translator: first call returns 0, second returns 1 with write
        StringWriter writer = new StringWriter();
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            private int call = 0;
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                call++;
                if (call == 1) {
                    return 0; // will write 'a'
                } else {
                    out.write("X");
                    return 1; // consume 'b'
                }
            }
        };
        translator.translate("ab", writer);
        assertEquals("aX", writer.toString());
    }

    @Test
    public void testTranslateWithSurrogatePairZeroConsumed() throws IOException {
        String input = new String(Character.toChars(0x1F600)); // 😀
        StringWriter writer = new StringWriter();
        new ZeroConsumedTranslator().translate(input, writer);
        assertEquals(input, writer.toString());
    }

    @Test
    public void testTranslateWithSurrogatePairConsumed() throws IOException {
        String input = new String(Character.toChars(0x1F600)); // 😀
        StringWriter writer = new StringWriter();
        new FixedConsumedTranslator(1, "X").translate(input, writer);
        assertEquals("X", writer.toString());
    }

    @Test(expected = IOException.class)
    public void testTranslateIOExceptionPropagation() throws IOException {
        new ThrowingTranslator().translate("a", new StringWriter());
    }

    // Tests for hex

    @Test
    public void testHexZero() {
        assertEquals("0", CharSequenceTranslator.hex(0));
    }

    @Test
    public void testHexPositive() {
        assertEquals("A", CharSequenceTranslator.hex(10));
        assertEquals("FF", CharSequenceTranslator.hex(255));
    }

    @Test
    public void testHexNegative() {
        assertEquals("FFFFFFFF", CharSequenceTranslator.hex(-1));
        assertEquals("FFFFFFFE", CharSequenceTranslator.hex(-2));
    }

    @Test
    public void testHexMaxAndMin() {
        assertEquals("7FFFFFFF", CharSequenceTranslator.hex(Integer.MAX_VALUE));
        assertEquals("80000000", CharSequenceTranslator.hex(Integer.MIN_VALUE));
    }

    // Tests for with

    @Test
    public void testWithNoArgs() throws IOException {
        CharSequenceTranslator t1 = new ZeroConsumedTranslator();
        CharSequenceTranslator merged = t1.with();
        assertTrue(merged instanceof AggregateTranslator);
        StringWriter writer = new StringWriter();
        merged.translate("a", writer);
        assertEquals("a", writer.toString());
    }

    @Test
    public void testWithOneArg() throws IOException {
        CharSequenceTranslator t1 = new ZeroConsumedTranslator();
        CharSequenceTranslator t2 = new FixedConsumedTranslator(1, "X");
        CharSequenceTranslator merged = t1.with(t2);
        assertTrue(merged instanceof AggregateTranslator);
        StringWriter writer = new StringWriter();
        merged.translate("a", writer);
        // t1 returns 0, then t2 returns 1 and writes "X"
        assertEquals("X", writer.toString());
    }

    @Test
    public void testWithMultipleArgs() throws IOException {
        CharSequenceTranslator t1 = new ZeroConsumedTranslator();
        CharSequenceTranslator t2 = new FixedConsumedTranslator(0, "A"); // never consumes but writes
        CharSequenceTranslator t3 = new FixedConsumedTranslator(1, "B");
        CharSequenceTranslator merged = t1.with(t2, t3);
        assertTrue(merged instanceof AggregateTranslator);
        StringWriter writer = new StringWriter();
        merged.translate("a", writer);
        // t1 returns 0, t2 returns 0 (writes "A"), t3 returns 1 (writes "B") -> output "AB"
        assertEquals("AB", writer.toString());
    }

    @Test
    public void testWithPreservesOrder() throws IOException {
        CharSequenceTranslator t1 = new FixedConsumedTranslator(1, "first");
        CharSequenceTranslator t2 = new FixedConsumedTranslator(1, "second");
        CharSequenceTranslator merged = t1.with(t2);
        StringWriter writer = new StringWriter();
        merged.translate("a", writer);
        // t1 is first in array, so it should consume and write "first"
        assertEquals("first", writer.toString());
    }
}
