package org.apache.commons.lang3.text.translate;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.junit.Test;

public class LookupTranslatorTest {

    // Constructor tests

    @Test
    public void testConstructorNullLookup() {
        LookupTranslator translator = new LookupTranslator((CharSequence[][]) null);
        // Verify that translate returns 0 for any input
        StringWriter writer = new StringWriter();
        int result = translator.translate("abc", 0, writer);
        assertEquals(0, result);
        assertEquals("", writer.toString());
    }

    @Test
    public void testConstructorEmptyLookup() {
        LookupTranslator translator = new LookupTranslator(new CharSequence[0][0]);
        StringWriter writer = new StringWriter();
        int result = translator.translate("abc", 0, writer);
        assertEquals(0, result);
        assertEquals("", writer.toString());
    }

    @Test
    public void testConstructorSingleEntry() throws IOException {
        CharSequence[][] lookup = { { "ab", "AB" } };
        LookupTranslator translator = new LookupTranslator(lookup);
        StringWriter writer = new StringWriter();
        int consumed = translator.translate("ab", 0, writer);
        assertEquals(2, consumed);
        assertEquals("AB", writer.toString());
    }

    @Test
    public void testConstructorMultipleEntriesLengths() throws IOException {
        CharSequence[][] lookup = { { "a", "1" }, { "bc", "23" }, { "def", "456" } };
        LookupTranslator translator = new LookupTranslator(lookup);
        // shortest = 1, longest = 3
        // Test with input "a"
        StringWriter writer = new StringWriter();
        int consumed = translator.translate("a", 0, writer);
        assertEquals(1, consumed);
        assertEquals("1", writer.toString());

        // Test with input "bc"
        writer = new StringWriter();
        consumed = translator.translate("bc", 0, writer);
        assertEquals(2, consumed);
        assertEquals("23", writer.toString());

        // Test with input "def"
        writer = new StringWriter();
        consumed = translator.translate("def", 0, writer);
        assertEquals(3, consumed);
        assertEquals("456", writer.toString());
    }

    // Translate method tests

    @Test
    public void testTranslateExactMatch() throws IOException {
        CharSequence[][] lookup = { { "abc", "ABC" } };
        LookupTranslator translator = new LookupTranslator(lookup);
        StringWriter writer = new StringWriter();
        int consumed = translator.translate("abc", 0, writer);
        assertEquals(3, consumed);
        assertEquals("ABC", writer.toString());
    }

    @Test
    public void testTranslateNoMatch() {
        CharSequence[][] lookup = { { "xyz", "XYZ" } };
        LookupTranslator translator = new LookupTranslator(lookup);
        StringWriter writer = new StringWriter();
        int consumed = translator.translate("abc", 0, writer);
        assertEquals(0, consumed);
        assertEquals("", writer.toString());
    }

    @Test
    public void testTranslateLongerMatchPreferred() throws IOException {
        // Greedy algorithm: longer match preferred
        CharSequence[][] lookup = { { "a", "1" }, { "ab", "12" } };
        LookupTranslator translator = new LookupTranslator(lookup);
        StringWriter writer = new StringWriter();
        int consumed = translator.translate("ab", 0, writer);
        assertEquals(2, consumed);
        assertEquals("12", writer.toString());
    }

    @Test
    public void testTranslateWithIndexNonZero() throws IOException {
        CharSequence[][] lookup = { { "bc", "BC" } };
        LookupTranslator translator = new LookupTranslator(lookup);
        StringWriter writer = new StringWriter();
        int consumed = translator.translate("abc", 1, writer);
        assertEquals(2, consumed);
        assertEquals("BC", writer.toString());
    }

    @Test
    public void testTranslateInputShorterThanLongest() throws IOException {
        // longest = 5, input length = 2 at index=0 -> max becomes 2
        CharSequence[][] lookup = { { "ab", "AB" }, { "abcde", "ABCDE" } };
        LookupTranslator translator = new LookupTranslator(lookup);
        StringWriter writer = new StringWriter();
        int consumed = translator.translate("ab", 0, writer);
        assertEquals(2, consumed); // matches shorter key
        assertEquals("AB", writer.toString());
    }

    @Test
    public void testTranslateInputTooShortForShortestKey() {
        // shortest = 5, input length = 3 -> max = 3, loop i=3 to 5, no iteration
        CharSequence[][] lookup = { { "abcde", "ABCDE" }, { "fghij", "FGHIJ" } };
        LookupTranslator translator = new LookupTranslator(lookup);
        StringWriter writer = new StringWriter();
        int consumed = translator.translate("abc", 0, writer);
        assertEquals(0, consumed);
        assertEquals("", writer.toString());
    }

    @Test
    public void testTranslateNullValueInMapIgnored() {
        // Key exists but maps to null -> result is null -> treated as no match
        CharSequence[][] lookup = { { "test", null } };
        LookupTranslator translator = new LookupTranslator(lookup);
        StringWriter writer = new StringWriter();
        int consumed = translator.translate("test", 0, writer);
        assertEquals(0, consumed);
        assertEquals("", writer.toString());
    }

    @Test(expected = NullPointerException.class)
    public void testTranslateNullInput() {
        CharSequence[][] lookup = { { "a", "1" } };
        LookupTranslator translator = new LookupTranslator(lookup);
        translator.translate(null, 0, new StringWriter());
    }

    @Test(expected = NullPointerException.class)
    public void testTranslateNullWriterWhenMatch() throws IOException {
        CharSequence[][] lookup = { { "a", "1" } };
        LookupTranslator translator = new LookupTranslator(lookup);
        translator.translate("a", 0, null);
    }

    @Test
    public void testTranslateEmptyInput() {
        CharSequence[][] lookup = { { "a", "1" } };
        LookupTranslator translator = new LookupTranslator(lookup);
        StringWriter writer = new StringWriter();
        int consumed = translator.translate("", 0, writer);
        // input length 0, longest 1, max = 0, shortest 1, loop condition false -> returns 0
        assertEquals(0, consumed);
        assertEquals("", writer.toString());
    }

    @Test
    public void testTranslateUsingStringBuilderInput() throws IOException {
        // Test with CharSequence that is not String
        CharSequence[][] lookup = { { "xy", "XY" } };
        LookupTranslator translator = new LookupTranslator(lookup);
        StringBuilder input = new StringBuilder("xyz");
        StringWriter writer = new StringWriter();
        int consumed = translator.translate(input, 0, writer);
        assertEquals(2, consumed);
        assertEquals("XY", writer.toString());
    }

    @Test
    public void testTranslateWithMultipleMatchesSameLength() throws IOException {
        // Both keys length 2; longest=2, shortest=2.
        // Only exact match matters (first found? Actually loop checks all lengths, but if multiple keys of same length,
        // subSequence will be the same; the map returns the value for that key; there is no ambiguity.
        CharSequence[][] lookup = { { "aa", "AA" }, { "bb", "BB" } };
        LookupTranslator translator = new LookupTranslator(lookup);
        StringWriter writer = new StringWriter();
        int consumed = translator.translate("bb", 0, writer);
        assertEquals(2, consumed);
        assertEquals("BB", writer.toString());
    }

    @Test
    public void testTranslateWriterIOExceptionPropagated() throws IOException {
        // Mock a writer that throws IOException on write
        final IOException toThrow = new IOException("test");
        Writer brokenWriter = new Writer() {
            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {
                throw toThrow;
            }

            @Override
            public void flush() throws IOException { }

            @Override
            public void close() throws IOException { }
        };

        CharSequence[][] lookup = { { "x", "X" } };
        LookupTranslator translator = new LookupTranslator(lookup);
        try {
            translator.translate("x", 0, brokenWriter);
            fail("Expected IOException");
        } catch (IOException e) {
            assertSame(toThrow, e);
        }
    }
}
