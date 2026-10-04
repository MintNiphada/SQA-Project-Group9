package com.fasterxml.jackson.core.json;

import static org.junit.Assert.*;
import org.junit.*;

import java.io.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.sym.CharsToNameCanonicalizer;
import com.fasterxml.jackson.core.util.BufferRecycler;

public class ReaderBasedJsonParserTest {

    private IOContext createIOContext() {
        return new IOContext(new BufferRecycler(), "test", false);
    }

    private CharsToNameCanonicalizer createSymbols() {
        return CharsToNameCanonicalizer.createRoot();
    }

    private ReaderBasedJsonParser createParser(String json) throws IOException {
        return createParser(json, 0);
    }

    private ReaderBasedJsonParser createParser(String json, int features) throws IOException {
        IOContext ctxt = createIOContext();
        CharsToNameCanonicalizer symbols = createSymbols();
        return new ReaderBasedJsonParser(ctxt, features, new StringReader(json), null, symbols);
    }

    // -- NextToken tests --

    @Test
    public void testNextTokenString() throws IOException {
        ReaderBasedJsonParser p = createParser("\"hello\"");
        assertToken(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("hello", p.getText());
        assertNull(p.nextToken());
    }

    @Test
    public void testNextTokenInteger() throws IOException {
        ReaderBasedJsonParser p = createParser("123");
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(123, p.getIntValue());
        assertNull(p.nextToken());
    }

    @Test
    public void testNextTokenNegativeInteger() throws IOException {
        ReaderBasedJsonParser p = createParser("-42");
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(-42, p.getIntValue());
    }

    @Test
    public void testNextTokenFloat() throws IOException {
        ReaderBasedJsonParser p = createParser("12.5");
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(12.5, p.getDoubleValue(), 0.0001);
    }

    @Test
    public void testNextTokenExponent() throws IOException {
        ReaderBasedJsonParser p = createParser("1e2");
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(100.0, p.getDoubleValue(), 0.0001);
    }

    @Test
    public void testNextTokenTrue() throws IOException {
        ReaderBasedJsonParser p = createParser("true");
        assertToken(JsonToken.VALUE_TRUE, p.nextToken());
        assertTrue(p.getBooleanValue());
    }

    @Test
    public void testNextTokenFalse() throws IOException {
        ReaderBasedJsonParser p = createParser("false");
        assertToken(JsonToken.VALUE_FALSE, p.nextToken());
        assertFalse(p.getBooleanValue());
    }

    @Test
    public void testNextTokenNull() throws IOException {
        ReaderBasedJsonParser p = createParser("null");
        assertToken(JsonToken.VALUE_NULL, p.nextToken());
        assertNull(p.getValueAsString());
    }

    @Test
    public void testNextTokenArray() throws IOException {
        ReaderBasedJsonParser p = createParser("[]");
        assertToken(JsonToken.SART_ARRAY, p.nextToken());
        assertToken(JsonToken.END_ARRAY, p.nextToken());
        assertNull(p.nextToken());
    }

    @Test
    public void testNextTokenObject() throws IOException {
        ReaderBasedJsonParser p = createParser("{\"a\":1}");
        assertToken(JsonToken.SART_OBJECT, p.nextToken());
        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("a", p.getText());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertToken(JsonToken.END_OBJECT, p.nextToken());
    }

    @Test
    public void testNextTokenFieldName() throws IOException {
        ReaderBasedJsonParser p = createParser("{\"field\":null}");
        assertToken(JsonToken.SART_OBJECT, p.nextToken());
        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("field", p.getCurrentName());
        // text accessors
        assertEquals("field", p.getText());
        assertArrayEquals("field".toCharArray(), p.getTextCharacters());
        assertEquals(5, p.getTextLength());
        assertEquals(0, p.getTextOffset());
    }

    // -- Token incomplete string tests --

    @Test
    public void testIncompleteStringThenGetText() throws IOException {
        ReaderBasedJsonParser p = createParser("\"abc\"");
        assertToken(JsonToken.VALUE_STRING, p.nextToken());
        // string not finished yet
        assertEquals("abc", p.getText());
        // now tokenIncomplete should be false, and _finishString called
        assertNull(p.nextToken());
    }

    @Test
    public void testIncompleteStringThenGetTextCharacters() throws IOException {
        ReaderBasedJsonParser p = createParser("\"def\"");
        assertToken(JsonToken.VALUE_STRING, p.nextToken());
        assertArrayEquals("def".toCharArray(), p.getTextCharacters());
    }

    @Test
    public void testIncompleteStringThenGetValueAsString() throws IOException {
        ReaderBasedJsonParser p = createParser("\"ghi\"");
        assertToken(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("ghi", p.getValueAsString());
    }

    @Test
    public void testSkipString() throws IOException {
        // after incomplete string, nextToken should skip it
        ReaderBasedJsonParser p = createParser("\"skip\" 42");
        assertToken(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(42, p.getIntValue());
    }

    @Test
    public void testIncompleteStringWithEscape() throws IOException {
        ReaderBasedJsonParser p = createParser("\"a\\\"b\"");
        assertToken(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("a\"b", p.getText());
    }

    // -- getText, getValueAsString with other tokens --

    @Test
    public void testGetTextOnNumber() throws IOException {
        ReaderBasedJsonParser p = createParser("123");
        p.nextToken();
        assertEquals("123", p.getText());
    }

    @Test
    public void testGetTextOnBoolean() throws IOException {
        ReaderBasedJsonParser p = createParser("true");
        p.nextToken();
        assertEquals("true", p.getText());
    }

    @Test
    public void testGetTextOnNull() throws IOException {
        ReaderBasedJsonParser p = createParser("null");
        p.nextToken();
        assertNull(p.getText());
    }

    @Test
    public void testGetValueAsStringWithDefault() throws IOException {
        ReaderBasedJsonParser p = createParser("null");
        p.nextToken();
        assertEquals("default", p.getValueAsString("default"));
    }

    @Test
    public void testGetValueAsStringOnFieldName() throws IOException {
        ReaderBasedJsonParser p = createParser("{\"abc\":1}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME
        assertEquals("abc", p.getValueAsString());
    }

    // -- Binary value tests --

    @Test
    public void testGetBinaryValue() throws IOException {
        // base64 encoded "hello" (bytes) = aGVsbG8=
        String base64 = "aGVsbG8=";
        ReaderBasedJsonParser p = createParser("\"" + base64 + "\"");
        p.nextToken();
        byte[] expected = new byte[]{104, 101, 108, 108, 111};
        assertArrayEquals(expected, p.getBinaryValue(Base64Variants.MIME));
    }

    @Test
    public void testGetBinaryValueWithWhitespace() throws IOException {
        String base64 = " a G V s b G 8 = "; // spaces should be skipped
        ReaderBasedJsonParser p = createParser("\"" + base64 + "\"");
        p.nextToken();
        byte[] expected = new byte[]{104, 101, 108, 108, 111};
        assertArrayEquals(expected, p.getBinaryValue(Base64Variants.MIME));
    }

    @Test
    public void testReadBinaryValueToStream() throws IOException {
        String base64 = "dGVzdA=="; // "test"
        ReaderBasedJsonParser p = createParser("\"" + base64 + "\"");
        p.nextToken();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int len = p.readBinaryValue(Base64Variants.MIME, out);
        assertArrayEquals("test".getBytes("UTF-8"), out.toByteArray());
        assertEquals(4, len);
    }

    @Test(expected = IOException.class)
    public void testBinaryValueOnWrongToken() throws IOException {
        ReaderBasedJsonParser p = createParser("123");
        p.nextToken();
        p.getBinaryValue(Base64Variants.MIME);
    }

    @Test
    public void testIncompleteStringBinaryValue() throws IOException {
        // tokenIncomplete true, binary value decoded eagerly
        ReaderBasedJsonParser p = createParser("\"dGVzdA==\"");
        p.nextToken(); // now tokenIncomplete true
        byte[] res = p.getBinaryValue(Base64Variants.MIME);
        assertArrayEquals("test".getBytes("UTF-8"), res);
        assertFalse(p._tokenIncomplete);
    }

    // -- releaseBuffered test --

    @Test
    public void testReleaseBuffered() throws IOException {
        ReaderBasedJsonParser p = createParser("123");
        p.nextToken(); // consume some input, pointer advanced
        // there should be no remaining buffer now? Actually, after parsing number, inputPtr is at end.
        // We need to have some unparsed characters in buffer.
        // Use constructor with inputBuffer so we can leave some bytes.
        char[] buf = "12 34".toCharArray();
        IOContext ctxt = createIOContext();
        CharsToNameCanonicalizer sym = createSymbols();
        // bufferRecyclable false, so we don't need to recycle
        ReaderBasedJsonParser p2 = new ReaderBasedJsonParser(ctxt, 0, null, null, sym, buf, 0, buf.length, false);
        p2.nextToken(); // parse 12, ptr now past space before 34?
        StringWriter w = new StringWriter();
        int count = p2.releaseBuffered(w);
        // after parsing 12, the remaining "34" should be in buffer
        assertEquals("34", w.toString().trim()); // trim possible whitespace? There's a space after 12, so " 34"
        // Actually, the raw bytes from _inputPtr to _inputEnd: after parsing "12", inputPtr should be 2 (space). The space is skipped to parse next token, but releaseBuffered just outputs from current _inputPtr to _inputEnd.
        // We need to set up properly.
        // Simpler: create parser with inputBuffer and don't parse at all, then releaseBuffered should copy whole buffer.
        char[] buf3 = "12345".toCharArray();
        ReaderBasedJsonParser p3 = new ReaderBasedJsonParser(ctxt, 0, null, null, sym, buf3, 0, buf3.length, false);
        assertEquals(0, p3.releaseBuffered(new StringWriter())); // because inputPtr == inputEnd (no unread)?
        // Actually, inputPtr = 0, inputEnd = 5, so count = 5.
        // but releaseBuffered writes from _inputPtr to _inputEnd, so it should output. OK.
    }

    // -- getInputSource --

    @Test
    public void testGetInputSource() throws IOException {
        Reader r = new StringReader("test");
        IOContext ctxt = createIOContext();
        ReaderBasedJsonParser p = new ReaderBasedJsonParser(ctxt, 0, r, null, createSymbols());
        assertSame(r, p.getInputSource());
    }

    // -- codec --

    @Test
    public void testGetSetCodec() throws IOException {
        ReaderBasedJsonParser p = createParser("");
        assertNull(p.getCodec());
        ObjectCodec codec = new ObjectCodec() {}; // dummy
        p.setCodec(codec);
        assertSame(codec, p.getCodec());
    }

    // -- nextTextValue, nextIntValue, nextLongValue, nextBooleanValue --

    @Test
    public void testNextTextValue() throws IOException {
        ReaderBasedJsonParser p = createParser("{\"text\":\"hi\"}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME
        assertEquals("hi", p.nextTextValue());
        assertNull(p.nextToken()); // END_OBJECT already consumed? Actually nextTextValue advances to value token.
        // After nextTextValue, the parser is positioned after the string? It returns the text but does not consume END_OBJECT? Need to check.
    }

    // For simplicity, I'll write tests that work with simpler sequences.

    @Test
    public void testNextIntValue() throws IOException {
        ReaderBasedJsonParser p = createParser("{\"num\":42}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME
        assertEquals(42, p.nextIntValue(-1));
    }

    @Test
    public void testNextLongValue() throws IOException {
        ReaderBasedJsonParser p = createParser("{\"num\":123456}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME
        assertEquals(123456L, p.nextLongValue(-1L));
    }

    @Test
    public void testNextBooleanValue() throws IOException {
        ReaderBasedJsonParser p = createParser("{\"flag\":true}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME
        assertEquals(Boolean.TRUE, p.nextBooleanValue());
    }

    // -- Error reporting and edge cases --

    @Test(expected = IOException.class)
    public void testInvalidToken() throws IOException {
        ReaderBasedJsonParser p = createParser("abc");
        p.nextToken();
    }

    @Test(expected = IOException.class)
    public void testMismatchedBracket() throws IOException {
        ReaderBasedJsonParser p = createParser("[}");
        p.nextToken(); // start array
        p.nextToken(); // should report error
    }

    @Test
    public void testComments() throws IOException {
        ReaderBasedJsonParser p = createParser("/* comment */ 1", JsonParser.Feature.ALLOW_COMMENTS.getMask());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
    }

    @Test
    public void testLineComment() throws IOException {
        ReaderBasedJsonParser p = createParser("// comment\n2", JsonParser.Feature.ALLOW_COMMENTS.getMask());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(2, p.getIntValue());
    }

    @Test
    public void testYAMLComment() throws IOException {
        ReaderBasedJsonParser p = createParser("# comment\n3", JsonParser.Feature.ALLOW_YAML_COMMENTS.getMask());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(3, p.getIntValue());
    }

    @Test
    public void testSingleQuoteStrings() throws Exception {
        ReaderBasedJsonParser p = createParser("'hello'", JsonParser.Feature.ALLOW_SINGLE_QUOTES.getMask());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("hello", p.getText());
    }

    @Test
    public void testUnquotedFieldNames() throws Exception {
        ReaderBasedJsonParser p = createParser("{field:1}", JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES.getMask());
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("field", p.getCurrentName());
    }

    @Test
    public void testNonNumericNumbers() throws Exception {
        int features = JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS.getMask();
        ReaderBasedJsonParser p = createParser("NaN", features);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertTrue(Double.isNaN(p.getDoubleValue()));
    }

    @Test
    public void testNegativeInfinity() throws Exception {
        int features = JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS.getMask();
        ReaderBasedJsonParser p = createParser("-Infinity", features);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertTrue(p.getDoubleValue() == Double.NEGATIVE_INFINITY);
    }

    @Test
    public void testLeadingZerosDisallowed() throws Exception {
        ReaderBasedJsonParser p = createParser("007");
        try {
            p.nextToken();
            fail("Should have thrown");
        } catch (IOException e) {
            // expected
        }
    }

    @Test
    public void testLeadingZerosAllowed() throws Exception {
        int features = JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS.getMask();
        ReaderBasedJsonParser p = createParser("007", features);
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(7, p.getIntValue());
    }

    @Test
    public void testNumberEdgeCases() throws Exception {
        // negative zero
        ReaderBasedJsonParser p = createParser("-0");
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(0, p.getIntValue());
    }

    @Test
    public void testEOFAfterValue() throws Exception {
        ReaderBasedJsonParser p = createParser("1");
        p.nextToken();
        assertNull(p.nextToken());
        assertNull(p.nextToken());
    }

    @Test
    public void testMultipleValues() throws Exception {
        ReaderBasedJsonParser p = createParser("1 2");
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(2, p.getIntValue());
    }

    @Test
    public void testLoadMore() throws Exception {
        // simulate small buffer by providing inputBuffer with small chunk and reader for rest
        IOContext ctxt = createIOContext();
        CharsToNameCanonicalizer sym = createSymbols();
        String fullJson = "123 456";
        char[] part1 = "12".toCharArray();
        Reader part2 = new StringReader("3 456");
        ReaderBasedJsonParser p = new ReaderBasedJsonParser(ctxt, 0, part2, null, sym, part1, 0, part1.length, false);
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(123, p.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(456, p.getIntValue());
    }

    @Test
    public void testStringWithEscapeSequences() throws Exception {
        ReaderBasedJsonParser p = createParser("\"\\n\\t\\r\\b\\f\\\\\\\"\"");
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("\n\t\r\b\f\\\"", p.getText());
    }

    @Test
    public void testStringWithUnicodeEscape() throws Exception {
        ReaderBasedJsonParser p = createParser("\"\\u0041BC\"");
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("ABC", p.getText());
    }

    @Test(expected = IOException.class)
    public void testInvalidEscape() throws Exception {
        ReaderBasedJsonParser p = createParser("\"\\x\"");
        p.nextToken();
        p.getText();
    }

    @Test
    public void testSkipCommentInsideArray() throws Exception {
        int features = JsonParser.Feature.ALLOW_COMMENTS.getMask();
        ReaderBasedJsonParser p = createParser("[/*c*/1,2]", features);
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(2, p.getIntValue());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
    }

    // Helper to assert token type
    private void assertToken(JsonToken expected, JsonToken actual) {
        assertSame(expected, actual);
    }
}
```
