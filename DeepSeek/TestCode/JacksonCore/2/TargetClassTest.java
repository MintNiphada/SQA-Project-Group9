package com.fasterxml.jackson.core.json;

import static org.junit.Assert.*;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.base.ParserBase;
import com.fasterxml.jackson.core.io.*;
import com.fasterxml.jackson.core.sym.*;
import com.fasterxml.jackson.core.util.*;

public class ReaderBasedJsonParserTest {

    private IOContext _ioContext;
    private CharsToNameCanonicalizer _symbols;
    private ObjectCodec _codec;

    @Before
    public void setUp() {
        _ioContext = new IOContext(new BufferRecycler(), null, false);
        _symbols = CharsToNameCanonicalizer.createRoot();
        _codec = null;
    }

    private ReaderBasedJsonParser createParser(String json) throws IOException {
        return createParser(json, 0);
    }

    private ReaderBasedJsonParser createParser(String json, int features) throws IOException {
        Reader r = new StringReader(json);
        return new ReaderBasedJsonParser(_ioContext, features, r, _codec, _symbols);
    }

    // Helper to enable specific features
    private int featureMask(Feature... features) {
        int mask = 0;
        for (Feature f : features) {
            mask |= f.getMask();
        }
        return mask;
    }

    // ==================== Lifecycle and basic accessors ====================

    @Test
    public void testGetCodec() throws IOException {
        ReaderBasedJsonParser parser = createParser("{}");
        assertNull(parser.getCodec());
        ObjectCodec codec = new ObjectCodec() {
            @Override
            public <T> T readValue(JsonParser jp, Class<T> valueType) { return null; }
            @Override
            public <T extends TreeNode> T readTree(JsonParser jp) { return null; }
            @Override
            public <T> T treeToValue(TreeNode n, Class<T> valueType) { return null; }
            @Override
            public void writeValue(JsonGenerator gen, Object value) {}
            @Override
            public TreeNode createObjectNode() { return null; }
            @Override
            public TreeNode createArrayNode() { return null; }
            @Override
            public JsonParser treeAsTokens(TreeNode n) { return null; }
            @Override
            public <T> T readValue(JsonParser jp, com.fasterxml.jackson.core.type.TypeReference<?> valueTypeRef) { return null; }
            @Override
            public <T> T readValue(JsonParser jp, com.fasterxml.jackson.core.type.ResolvedType valueType) { return null; }
            @Override
            public void writeTree(JsonGenerator gen, TreeNode tree) {}
            @Override
            public TreeNode missingNode() { return null; }
            @Override
            public TreeNode nullNode() { return null; }
        };
        parser.setCodec(codec);
        assertSame(codec, parser.getCodec());
        parser.close();
    }

    @Test
    public void testGetInputSource() throws IOException {
        Reader r = new StringReader("1");
        ReaderBasedJsonParser parser = new ReaderBasedJsonParser(_ioContext, 0, r, null, _symbols);
        assertSame(r, parser.getInputSource());
        parser.close();
    }

    @Test
    public void testReleaseBuffered() throws IOException {
        ReaderBasedJsonParser parser = createParser("123");
        parser.nextToken(); // read number
        StringWriter sw = new StringWriter();
        int count = parser.releaseBuffered(sw);
        // After reading token, inputPtr may be at end, so count could be 0
        // We'll test with some unread content: create parser with extra whitespace after token
        parser.close();
        parser = createParser("123   ");
        parser.nextToken();
        sw = new StringWriter();
        count = parser.releaseBuffered(sw);
        assertTrue(count >= 0);
        parser.close();
    }

    @Test
    public void testClose() throws IOException {
        ReaderBasedJsonParser parser = createParser("{}");
        parser.close();
        // After close, _reader should be null, _inputBuffer released
        assertTrue(parser.isClosed());
    }

    @Test
    public void testCloseInputWithAutoClose() throws IOException {
        // Test _closeInput when auto-close is enabled
        StringReader r = new StringReader("{}");
        IOContext ctxt = new IOContext(new BufferRecycler(), null, true); // resource managed
        ReaderBasedJsonParser parser = new ReaderBasedJsonParser(ctxt, Feature.AUTO_CLOSE_SOURCE.getMask(), r, null, _symbols);
        parser.close();
        // Reader should be closed; we can't check directly but no exception
    }

    @Test
    public void testCloseInputWithoutAutoClose() throws IOException {
        StringReader r = new StringReader("{}");
        IOContext ctxt = new IOContext(new BufferRecycler(), null, false);
        ReaderBasedJsonParser parser = new ReaderBasedJsonParser(ctxt, 0, r, null, _symbols);
        parser.close();
        // Reader not closed; no exception
    }

    @Test
    public void testReleaseBuffers() throws IOException {
        ReaderBasedJsonParser parser = createParser("{}");
        parser.close(); // calls _releaseBuffers
        // No exception
    }

    // ==================== loadMore and getNextChar ====================

    @Test
    public void testLoadMoreEndOfInput() throws IOException {
        ReaderBasedJsonParser parser = createParser("");
        assertFalse(parser.loadMore());
        parser.close();
    }

    @Test(expected = IOException.class)
    public void testLoadMoreReaderReturnsZero() throws IOException {
        Reader r = new Reader() {
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                return 0; // should throw
            }
            @Override
            public void close() throws IOException {}
        };
        ReaderBasedJsonParser parser = new ReaderBasedJsonParser(_ioContext, 0, r, null, _symbols);
        parser.loadMore();
    }

    @Test
    public void testGetNextChar() throws IOException {
        ReaderBasedJsonParser parser = createParser("a");
        assertEquals('a', parser.getNextChar("EOF"));
        parser.close();
    }

    @Test(expected = JsonParseException.class)
    public void testGetNextCharEOF() throws IOException {
        ReaderBasedJsonParser parser = createParser("");
        parser.getNextChar("EOF");
    }

    // ==================== Text access methods ====================

    @Test
    public void testGetTextOnString() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"hello\"");
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("hello", parser.getText());
        parser.close();
    }

    @Test
    public void testGetTextOnIncompleteString() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"hello\"");
        parser.nextToken(); // token incomplete
        // Access text to finish
        assertEquals("hello", parser.getText());
        parser.close();
    }

    @Test
    public void testGetTextOnOtherTokens() throws IOException {
        ReaderBasedJsonParser parser = createParser("123 true null");
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals("123", parser.getText());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals("true", parser.getText());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals("null", parser.getText());
        parser.close();
    }

    @Test
    public void testGetValueAsString() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"hello\" 123");
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("hello", parser.getValueAsString());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals("123", parser.getValueAsString());
        parser.close();
    }

    @Test
    public void testGetValueAsStringWithDefault() throws IOException {
        ReaderBasedJsonParser parser = createParser("123");
        parser.nextToken();
        assertEquals("123", parser.getValueAsString("default"));
        parser.close();
    }

    @Test
    public void testGetTextCharacters() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"abc\"");
        parser.nextToken();
        char[] chars = parser.getTextCharacters();
        assertEquals("abc", new String(chars, 0, parser.getTextLength()));
        parser.close();
    }

    @Test
    public void testGetTextCharactersFieldName() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"field\":1}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        char[] chars = parser.getTextCharacters();
        assertEquals("field", new String(chars, 0, parser.getTextLength()));
        parser.close();
    }

    @Test
    public void testGetTextLengthAndOffset() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"abc\"");
        parser.nextToken();
        assertEquals(3, parser.getTextLength());
        assertEquals(0, parser.getTextOffset()); // offset is 0 for strings
        parser.close();
    }

    @Test
    public void testGetTextOnNullToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("");
        assertNull(parser.getTextCharacters());
        assertEquals(0, parser.getTextLength());
        assertEquals(0, parser.getTextOffset());
        parser.close();
    }

    // ==================== nextToken and value parsing ====================

    @Test
    public void testNextTokenEndOfInput() throws IOException {
        ReaderBasedJsonParser parser = createParser("");
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testNextTokenSimpleValues() throws IOException {
        ReaderBasedJsonParser parser = createParser("true false null");
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testNextTokenNumbers() throws IOException {
        ReaderBasedJsonParser parser = createParser("0 123 -456 7.89 1e2 -3.4E+5");
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(0, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(123, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(-456, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(7.89, parser.getDoubleValue(), 0.0);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(100.0, parser.getDoubleValue(), 0.0);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(-340000.0, parser.getDoubleValue(), 0.0);
        parser.close();
    }

    @Test
    public void testNextTokenArrays() throws IOException {
        ReaderBasedJsonParser parser = createParser("[ ] [ 1 , 2 ]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        parser.close();
    }

    @Test
    public void testNextTokenObjects() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    @Test
    public void testNextTokenAfterFieldName() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1, \"b\":2}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    @Test
    public void testNextTokenIncompleteStringSkip() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"hello\" 123");
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        // token incomplete, but nextToken will skip it
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(123, parser.getIntValue());
        parser.close();
    }

    @Test(expected = JsonParseException.class)
    public void testMismatchedArrayEnd() throws IOException {
        ReaderBasedJsonParser parser = createParser("}");
        parser.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testMismatchedObjectEnd() throws IOException {
        ReaderBasedJsonParser parser = createParser("]");
        parser.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testMissingComma() throws IOException {
        ReaderBasedJsonParser parser = createParser("[1 2]");
        parser.nextToken(); // [
        parser.nextToken(); // 1
        parser.nextToken(); // should fail
    }

    @Test(expected = JsonParseException.class)
    public void testMissingColon() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\" 1}");
        parser.nextToken(); // {
        parser.nextToken(); // field name
        parser.nextToken(); // should fail
    }

    @Test(expected = JsonParseException.class)
    public void testInvalidValueStart() throws IOException {
        ReaderBasedJsonParser parser = createParser("]");
        parser.nextToken();
    }

    // ==================== nextTextValue, nextIntValue, etc. ====================

    @Test
    public void testNextTextValue() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"hello\"");
        assertEquals("hello", parser.nextTextValue());
        parser.close();
    }

    @Test
    public void testNextTextValueFromField() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":\"b\"}");
        parser.nextToken(); // {
        assertEquals("b", parser.nextTextValue());
        parser.close();
    }

    @Test
    public void testNextIntValue() throws IOException {
        ReaderBasedJsonParser parser = createParser("123");
        assertEquals(123, parser.nextIntValue(0));
        parser.close();
    }

    @Test
    public void testNextIntValueDefault() throws IOException {
        ReaderBasedJsonParser parser = createParser("true");
        assertEquals(42, parser.nextIntValue(42));
        parser.close();
    }

    @Test
    public void testNextLongValue() throws IOException {
        ReaderBasedJsonParser parser = createParser("1234567890123");
        assertEquals(1234567890123L, parser.nextLongValue(0L));
        parser.close();
    }

    @Test
    public void testNextBooleanValue() throws IOException {
        ReaderBasedJsonParser parser = createParser("true false null");
        assertEquals(Boolean.TRUE, parser.nextBooleanValue());
        assertEquals(Boolean.FALSE, parser.nextBooleanValue());
        assertNull(parser.nextBooleanValue());
        parser.close();
    }

    // ==================== Number parsing details ====================

    @Test
    public void testLeadingZeroesAllowed() throws IOException {
        int features = featureMask(Feature.ALLOW_NUMERIC_LEADING_ZEROS);
        ReaderBasedJsonParser parser = createParser("007", features);
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(7, parser.getIntValue());
        parser.close();
    }

    @Test(expected = JsonParseException.class)
    public void testLeadingZeroesDisallowed() throws IOException {
        ReaderBasedJsonParser parser = createParser("007");
        parser.nextToken();
    }

    @Test
    public void testNumberWithExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("1.5e2");
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(150.0, parser.getDoubleValue(), 0.0);
        parser.close();
    }

    @Test(expected = JsonParseException.class)
    public void testInvalidNumberMissingFraction() throws IOException {
        ReaderBasedJsonParser parser = createParser("1.");
        parser.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testInvalidNumberMissingExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("1e");
        parser.nextToken();
    }

    @Test
    public void testNumberSplitAcrossBuffer() throws IOException {
        // Use a reader that returns one char at a time to force buffer boundary
        String number = "1234567890";
        Reader r = new Reader() {
            int pos = 0;
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                if (pos >= number.length()) return -1;
                cbuf[off] = number.charAt(pos++);
                return 1;
            }
            @Override
            public void close() throws IOException {}
        };
        ReaderBasedJsonParser parser = new ReaderBasedJsonParser(_ioContext, 0, r, null, _symbols);
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1234567890, parser.getIntValue());
        parser.close();
    }

    @Test
    public void testNegativeNumberSplit() throws IOException {
        String number = "-123";
        Reader r = new Reader() {
            int pos = 0;
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                if (pos >= number.length()) return -1;
                cbuf[off] = number.charAt(pos++);
                return 1;
            }
            @Override
            public void close() throws IOException {}
        };
        ReaderBasedJsonParser parser = new ReaderBasedJsonParser(_ioContext, 0, r, null, _symbols);
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(-123, parser.getIntValue());
        parser.close();
    }

    @Test
    public void testNonNumericNumbers() throws IOException {
        int features = featureMask(Feature.ALLOW_NON_NUMERIC_NUMBERS);
        ReaderBasedJsonParser parser = createParser("NaN Infinity -Infinity", features);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertTrue(Double.isNaN(parser.getDoubleValue()));
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(Double.POSITIVE_INFINITY, parser.getDoubleValue(), 0.0);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(Double.NEGATIVE_INFINITY, parser.getDoubleValue(), 0.0);
        parser.close();
    }

    @Test(expected = JsonParseException.class)
    public void testNonNumericNumbersDisabled() throws IOException {
        ReaderBasedJsonParser parser = createParser("NaN");
        parser.nextToken();
    }

    @Test
    public void testPlusSignAsNumber() throws IOException {
        int features = featureMask(Feature.ALLOW_NON_NUMERIC_NUMBERS);
        ReaderBasedJsonParser parser = createParser("+INF", features);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(Double.POSITIVE_INFINITY, parser.getDoubleValue(), 0.0);
        parser.close();
    }

    @Test(expected = JsonParseException.class)
    public void testInvalidNumberAfterMinus() throws IOException {
        ReaderBasedJsonParser parser = createParser("-X");
        parser.nextToken();
    }

    // ==================== String parsing and escaping ====================

    @Test
    public void testSimpleString() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"hello\"");
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("hello", parser.getText());
        parser.close();
    }

    @Test
    public void testStringWithEscapes() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"\\\"\\\\\\/\\b\\f\\n\\r\\t\"");
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("\"\\/\b\f\n\r\t", parser.getText());
        parser.close();
    }

    @Test
    public void testStringWithUnicodeEscape() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"\\u0041\\u00E9\"");
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("Aé", parser.getText());
        parser.close();
    }

    @Test(expected = JsonParseException.class)
    public void testInvalidUnicodeEscape() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"\\uGGGG\"");
        parser.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testUnterminatedString() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"hello");
        parser.nextToken();
    }

    @Test
    public void testStringSplitAcrossBuffer() throws IOException {
        String str = "\"hello world\"";
        Reader r = new Reader() {
            int pos = 0;
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                if (pos >= str.length()) return -1;
                cbuf[off] = str.charAt(pos++);
                return 1;
            }
            @Override
            public void close() throws IOException {}
        };
        ReaderBasedJsonParser parser = new ReaderBasedJsonParser(_ioContext, 0, r, null, _symbols);
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("hello world", parser.getText());
        parser.close();
    }

    // ==================== Field name parsing ====================

    @Test
    public void testSimpleFieldName() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"abc\":1}");
        parser.nextToken(); // {
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("abc", parser.getCurrentName());
        parser.close();
    }

    @Test
    public void testFieldNameWithEscapes() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\\\"b\":1}");
        parser.nextToken();
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a\"b", parser.getCurrentName());
        parser.close();
    }

    @Test
    public void testSingleQuotedFieldName() throws IOException {
        int features = featureMask(Feature.ALLOW_SINGLE_QUOTES);
        ReaderBasedJsonParser parser = createParser("{'abc':1}", features);
        parser.nextToken();
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("abc", parser.getCurrentName());
        parser.close();
    }

    @Test
    public void testUnquotedFieldName() throws IOException {
        int features = featureMask(Feature.ALLOW_UNQUOTED_FIELD_NAMES);
        ReaderBasedJsonParser parser = createParser("{abc:1}", features);
        parser.nextToken();
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("abc", parser.getCurrentName());
        parser.close();
    }

    @Test(expected = JsonParseException.class)
    public void testUnquotedFieldNameDisabled() throws IOException {
        ReaderBasedJsonParser parser = createParser("{abc:1}");
        parser.nextToken();
        parser.nextToken();
    }

    @Test
    public void testUnquotedFieldNameWithDigits() throws IOException {
        int features = featureMask(Feature.ALLOW_UNQUOTED_FIELD_NAMES);
        ReaderBasedJsonParser parser = createParser("{a123:1}", features);
        parser.nextToken();
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a123", parser.getCurrentName());
        parser.close();
    }

    @Test(expected = JsonParseException.class)
    public void testUnquotedFieldNameStartingWithDigit() throws IOException {
        int features = featureMask(Feature.ALLOW_UNQUOTED_FIELD_NAMES);
        ReaderBasedJsonParser parser = createParser("{123:1}", features);
        parser.nextToken();
        parser.nextToken();
    }

    // ==================== Single-quoted strings ====================

    @Test
    public void testSingleQuotedString() throws IOException {
        int features = featureMask(Feature.ALLOW_SINGLE_QUOTES);
        ReaderBasedJsonParser parser = createParser("'hello'", features);
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("hello", parser.getText());
        parser.close();
    }

    @Test(expected = JsonParseException.class)
    public void testSingleQuotedStringDisabled() throws IOException {
        ReaderBasedJsonParser parser = createParser("'hello'");
        parser.nextToken();
    }

    // ==================== Comments ====================

    @Test
    public void testLineComment() throws IOException {
        int features = featureMask(Feature.ALLOW_COMMENTS);
        ReaderBasedJsonParser parser = createParser("// comment\n123", features);
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(123, parser.getIntValue());
        parser.close();
    }

    @Test
    public void testBlockComment() throws IOException {
        int features = featureMask(Feature.ALLOW_COMMENTS);
        ReaderBasedJsonParser parser = createParser("/* comment */123", features);
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(123, parser.getIntValue());
        parser.close();
    }

    @Test(expected = JsonParseException.class)
    public void testCommentDisabled() throws IOException {
        ReaderBasedJsonParser parser = createParser("// comment\n123");
        parser.nextToken();
    }

    @Test
    public void testYAMLComment() throws IOException {
        int features = featureMask(Feature.ALLOW_YAML_COMMENTS);
        ReaderBasedJsonParser parser = createParser("# comment\n123", features);
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(123, parser.getIntValue());
        parser.close();
    }

    @Test(expected = JsonParseException.class)
    public void testYAMLCommentDisabled() throws IOException {
        ReaderBasedJsonParser parser = createParser("# comment\n123");
        parser.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testUnterminatedBlockComment() throws IOException {
        int features = featureMask(Feature.ALLOW_COMMENTS);
        ReaderBasedJsonParser parser = createParser("/* comment", features);
        parser.nextToken();
    }

    // ==================== Base64 decoding ====================

    @Test
    public void testGetBinaryValue() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"SGVsbG8=\"");
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        byte[] binary = parser.getBinaryValue(Base64Variants.getDefaultVariant());
        assertEquals("Hello", new String(binary, "UTF-8"));
        parser.close();
    }

    @Test
    public void testReadBinaryValue() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"SGVsbG8=\"");
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int len = parser.readBinaryValue(Base64Variants.getDefaultVariant(), out);
        assertEquals(5, len);
        assertEquals("Hello", new String(out.toByteArray(), "UTF-8"));
        parser.close();
    }

    @Test
    public void testBase64WithPaddingAndMissingPadding() throws IOException {
        // Test with padding
        ReaderBasedJsonParser parser = createParser("\"TQ==\"");
        parser.nextToken();
        byte[] b = parser.getBinaryValue(Base64Variants.getDefaultVariant());
        assertEquals("M", new String(b, "UTF-8"));
        parser.close();
        // Test without padding (variant that doesn't use padding)
        Base64Variant noPadding = new Base64Variant(Base64Variants.getDefaultVariant(), "NO_PADDING", false, '=', 64);
        parser = createParser("\"TQ\"");
        parser.nextToken();
        b = parser.getBinaryValue(noPadding);
        assertEquals("M", new String(b, "UTF-8"));
        parser.close();
    }

    @Test(expected = JsonParseException.class)
    public void testInvalidBase64Char() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"!!!\"");
        parser.nextToken();
        parser.getBinaryValue(Base64Variants.getDefaultVariant());
    }

    // ==================== Error reporting ====================

    @Test(expected = JsonParseException.class)
    public void testInvalidTokenTrue() throws IOException {
        ReaderBasedJsonParser parser = createParser("tr");
        parser.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testInvalidTokenFalse() throws IOException {
        ReaderBasedJsonParser parser = createParser("fa");
        parser.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testInvalidTokenNull() throws IOException {
        ReaderBasedJsonParser parser = createParser("nu");
        parser.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testUnexpectedCharInValue() throws IOException {
        ReaderBasedJsonParser parser = createParser("x");
        parser.nextToken();
    }

    // ==================== White space skipping ====================

    @Test
    public void testSkipWhiteSpace() throws IOException {
        ReaderBasedJsonParser parser = createParser(" \t\r\n 123");
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(123, parser.getIntValue());
        parser.close();
    }

    // ==================== _skipCR ====================
    @Test
    public void testSkipCRLF() throws IOException {
        ReaderBasedJsonParser parser = createParser("\r\n123");
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(123, parser.getIntValue());
        parser.close();
    }

    // ==================== _decodeEscaped edge cases ====================
    @Test
    public void testEscapeUnknownChar() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"\\x\"");
        try {
            parser.nextToken();
            fail("Expected exception");
        } catch (JsonParseException e) {
            // expected
        }
        parser.close();
    }

    // ==================== _matchToken ====================
    @Test
    public void testMatchTokenWithFollowingIdentifierPart() throws IOException {
        ReaderBasedJsonParser parser = createParser("trueX");
        try {
            parser.nextToken();
            fail("Expected exception");
        } catch (JsonParseException e) {
            // expected
        }
        parser.close();
    }

    // ==================== _handleInvalidNumberStart ====================
    @Test
    public void testHandleInvalidNumberStartInf() throws IOException {
        int features = featureMask(Feature.ALLOW_NON_NUMERIC_NUMBERS);
        ReaderBasedJsonParser parser = createParser("+INF", features);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(Double.POSITIVE_INFINITY, parser.getDoubleValue(), 0.0);
        parser.close();
    }

    @Test
    public void testHandleInvalidNumberStartInfinity() throws IOException {
        int features = featureMask(Feature.ALLOW_NON_NUMERIC_NUMBERS);
        ReaderBasedJsonParser parser = createParser("+Infinity", features);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(Double.POSITIVE_INFINITY, parser.getDoubleValue(), 0.0);
        parser.close();
    }

    // ==================== _skipString ====================
    @Test
    public void testSkipStringWithEscapes() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"a\\\"b\" 123");
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        // skip by moving to next token
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(123, parser.getIntValue());
        parser.close();
    }

    // ==================== _finishString2 ====================
    @Test
    public void testFinishString2WithEscapes() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"a\\nb\"");
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("a\nb", parser.getText());
        parser.close();
    }

    // ==================== _parseName2 ====================
    @Test
    public void testParseName2WithEscapes() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\\\"b\":1}");
        parser.nextToken();
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a\"b", parser.getCurrentName());
        parser.close();
    }

    // ==================== _handleOddName2 ====================
    @Test
    public void testUnquotedNameWithUnicode() throws IOException {
        int features = featureMask(Feature.ALLOW_UNQUOTED_FIELD_NAMES);
        ReaderBasedJsonParser parser = createParser("{na\u00E9me:1}", features);
        parser.nextToken();
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("na\u00E9me", parser.getCurrentName());
        parser.close();
    }

    // ==================== _readBinary incremental ====================
    @Test
    public void testReadBinaryIncremental() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"SGVsbG8=\"");
        parser.nextToken();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int len = parser.readBinaryValue(Base64Variants.getDefaultVariant(), out);
        assertEquals(5, len);
        assertEquals("Hello", new String(out.toByteArray(), "UTF-8"));
        parser.close();
    }

    // ==================== _decodeBase64 with missing padding ====================
    @Test
    public void testBase64MissingPadding() throws IOException {
        Base64Variant noPad = new Base64Variant(Base64Variants.getDefaultVariant(), "NO_PAD", false, '=', 64);
        ReaderBasedJsonParser parser = createParser("\"TQ\"");
        parser.nextToken();
        byte[] b = parser.getBinaryValue(noPad);
        assertEquals("M", new String(b, "UTF-8"));
        parser.close();
    }

    // ==================== _skipWSOrEnd with YAML comment ====================
    @Test
    public void testSkipWSOrEndYAMLComment() throws IOException {
        int features = featureMask(Feature.ALLOW_YAML_COMMENTS);
        ReaderBasedJsonParser parser = createParser("# comment\n123", features);
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(123, parser.getIntValue());
        parser.close();
    }

    // ==================== _skipComment with unexpected char ====================
    @Test(expected = JsonParseException.class)
    public void testSkipCommentInvalidChar() throws IOException {
        int features = featureMask(Feature.ALLOW_COMMENTS);
        ReaderBasedJsonParser parser = createParser("/! comment", features);
        parser.nextToken();
    }

    // ==================== _skipCComment with CR, LF, TAB ====================
    @Test
    public void testBlockCommentWithSpecialChars() throws IOException {
        int features = featureMask(Feature.ALLOW_COMMENTS);
        ReaderBasedJsonParser parser = createParser("/*\r\n\t*/123", features);
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(123, parser.getIntValue());
        parser.close();
    }

    // ==================== _skipLine with CR ====================
    @Test
    public void testSkipLineWithCR() throws IOException {
        int features = featureMask(Feature.ALLOW_COMMENTS);
        ReaderBasedJsonParser parser = createParser("// comment\r123", features);
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(123, parser.getIntValue());
        parser.close();
    }

    // ==================== _decodeEscaped with incomplete input ====================
    @Test(expected = JsonParseException.class)
    public void testDecodeEscapedEOF() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"\\");
        parser.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testDecodeEscapedHexEOF() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"\\u00");
        parser.nextToken();
    }

    // ==================== _matchToken with EOF ====================
    @Test(expected = JsonParseException.class)
    public void testMatchTokenEOF() throws IOException {
        ReaderBasedJsonParser parser = createParser("tru");
        parser.nextToken();
    }

    // ==================== _handleOddValue with identifier start ====================
    @Test(expected = JsonParseException.class)
    public void testOddValueIdentifier() throws IOException {
        ReaderBasedJsonParser parser = createParser("abc");
        parser.nextToken();
    }

    // ==================== _verifyNoLeadingZeroes with EOF ====================
    @Test
    public void testLeadingZeroEOF() throws IOException {
        ReaderBasedJsonParser parser = createParser("0");
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(0, parser.getIntValue());
        parser.close();
    }

    // ==================== _parseNumber2 with exponent sign ====================
    @Test
    public void testNumber2WithExponentSign() throws IOException {
        // Use a reader that returns one char at a time to force _parseNumber2
        String num = "1.5e+2";
        Reader r = new Reader() {
            int pos = 0;
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                if (pos >= num.length()) return -1;
                cbuf[off] = num.charAt(pos++);
                return 1;
            }
            @Override
            public void close() throws IOException {}
        };
        ReaderBasedJsonParser parser = new ReaderBasedJsonParser(_ioContext, 0, r, null, _symbols);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(150.0, parser.getDoubleValue(), 0.0);
        parser.close();
    }

    // ==================== _parseNumber2 with missing integer part ====================
    @Test(expected = JsonParseException.class)
    public void testNumber2MissingInteger() throws IOException {
        String num = ".5";
        Reader r = new Reader() {
            int pos = 0;
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                if (pos >= num.length()) return -1;
                cbuf[off] = num.charAt(pos++);
                return 1;
            }
            @Override
            public void close() throws IOException {}
        };
        ReaderBasedJsonParser parser = new ReaderBasedJsonParser(_ioContext, 0, r, null, _symbols);
        parser.nextToken();
    }

    // ==================== _parseNumber2 with missing fraction ====================
    @Test(expected = JsonParseException.class)
    public void testNumber2MissingFraction() throws IOException {
        String num = "1.";
        Reader r = new Reader() {
            int pos = 0;
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                if (pos >= num.length()) return -1;
                cbuf[off] = num.charAt(pos++);
                return 1;
            }
            @Override
            public void close() throws IOException {}
        };
        ReaderBasedJsonParser parser = new ReaderBasedJsonParser(_ioContext, 0, r, null, _symbols);
        parser.nextToken();
    }

    // ==================== _parseNumber2 with missing exponent ====================
    @Test(expected = JsonParseException.class)
    public void testNumber2MissingExponent() throws IOException {
        String num = "1e";
        Reader r = new Reader() {
            int pos = 0;
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                if (pos >= num.length()) return -1;
                cbuf[off] = num.charAt(pos++);
                return 1;
            }
            @Override
            public void close() throws IOException {}
        };
        ReaderBasedJsonParser parser = new ReaderBasedJsonParser(_ioContext, 0, r, null, _symbols);
        parser.nextToken();
    }

    // ==================== _parseNumber2 with EOF after minus ====================
    @Test(expected = JsonParseException.class)
    public void testNumber2EOFAfterMinus() throws IOException {
        String num = "-";
        Reader r = new Reader() {
            int pos = 0;
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                if (pos >= num.length()) return -1;
                cbuf[off] = num.charAt(pos++);
                return 1;
            }
            @Override
            public void close() throws IOException {}
        };
        ReaderBasedJsonParser parser = new ReaderBasedJsonParser(_ioContext, 0, r, null, _symbols);
        parser.nextToken();
    }

    // ==================== _handleInvalidNumberStart with 'I' but not 'N' or 'n' ====================
    @Test(expected = JsonParseException.class)
    public void testInvalidNumberStartI() throws IOException {
        ReaderBasedJsonParser parser = createParser("-Ix");
        parser.nextToken();
    }

    // ==================== _handleOddValue with '+' then invalid ====================
    @Test(expected = JsonParseException.class)
    public void testOddValuePlusInvalid() throws IOException {
        ReaderBasedJsonParser parser = createParser("+X");
        parser.nextToken();
    }

    // ==================== _skipWS with invalid space ====================
    @Test(expected = JsonParseException.class)
    public void testSkipWSInvalidSpace() throws IOException {
        // Character 0x0B is vertical tab, not allowed
        ReaderBasedJsonParser parser = createParser("\u000B123");
        parser.nextToken();
    }

    // ==================== _skipWSOrEnd with invalid space ====================
    @Test(expected = JsonParseException.class)
    public void testSkipWSOrEndInvalidSpace() throws IOException {
        ReaderBasedJsonParser parser = createParser("\u000B");
        parser.nextToken();
    }

    // ==================== _skipCComment with invalid space ====================
    @Test(expected = JsonParseException.class)
    public void testBlockCommentInvalidSpace() throws IOException {
        int features = featureMask(Feature.ALLOW_COMMENTS);
        ReaderBasedJsonParser parser = createParser("/*\u000B*/123", features);
        parser.nextToken();
    }

    // ==================== _skipLine with invalid space ====================
    @Test(expected = JsonParseException.class)
    public void testSkipLineInvalidSpace() throws IOException {
        int features = featureMask(Feature.ALLOW_COMMENTS);
        ReaderBasedJsonParser parser = createParser("//\u000B\n123", features);
        parser.nextToken();
    }

    // ==================== _decodeBase64 with missing padding and quote ====================
    @Test
    public void testBase64MissingPaddingQuote() throws IOException {
        Base64Variant noPad = new Base64Variant(Base64Variants.getDefaultVariant(), "NO_PAD", false, '=', 64);
        ReaderBasedJsonParser parser = createParser("\"TQ\"");
        parser.nextToken();
        byte[] b = parser.getBinaryValue(noPad);
        assertEquals("M", new String(b, "UTF-8"));
        parser.close();
    }

    // ==================== _readBinary with missing padding ====================
    @Test
    public void testReadBinaryMissingPadding() throws IOException {
        Base64Variant noPad = new Base64Variant(Base64Variants.getDefaultVariant(), "NO_PAD", false, '=', 64);
        ReaderBasedJsonParser parser = createParser("\"TQ\"");
        parser.nextToken();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int len = parser.readBinaryValue(noPad, out);
        assertEquals(1, len);
        assertEquals("M", new String(out.toByteArray(), "UTF-8"));
        parser.close();
    }

    // ==================== _readBinary with padding and missing padding char ====================
    @Test(expected = JsonParseException.class)
    public void testReadBinaryInvalidPaddingChar() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"TQ=!\"");
        parser.nextToken();
        parser.readBinaryValue(Base64Variants.getDefaultVariant(), new ByteArrayOutputStream());
    }

    // ==================== _decodeBase64 with invalid padding char ====================
    @Test(expected = JsonParseException.class)
    public void testBase64InvalidPaddingChar() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"TQ=!\"");
        parser.nextToken();
        parser.getBinaryValue(Base64Variants.getDefaultVariant());
    }

    // ==================== _decodeBase64 with escape ====================
    @Test
    public void testBase64WithEscape() throws IOException {
        // Base64 with whitespace inside string? Actually base64 can have whitespace, but we test escape
        // We'll use a variant that allows whitespace? Not needed. Just test that _decodeBase64Escape is called.
        // We'll use a string with a backslash escape that is not a valid base64 char.
        // But that would be an error. Instead, we can test with a string that has a backslash-escaped quote.
        // However, base64 decoding does not handle escapes; it's for the whole string.
        // The method _decodeBase64Escape is called when decodeBase64Char returns negative and ch is not quote.
        // We can trigger that by having a character that is not base64 and not quote, e.g., a newline.
        // But newline is skipped as whitespace. We need a character that is not whitespace, not base64, not quote.
        // For example, '!' is not base64. So "\"!\"". But that would cause an error.
        // We'll just test that it throws.
        try {
            ReaderBasedJsonParser parser = createParser("\"!\"");
            parser.nextToken();
            parser.getBinaryValue(Base64Variants.getDefaultVariant());
            fail("Expected exception");
        } catch (JsonParseException e) {
            // expected
        }
    }

    // ==================== _readBinary with escape ====================
    @Test(expected = JsonParseException.class)
    public void testReadBinaryEscape() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"!\"");
        parser.nextToken();
        parser.readBinaryValue(Base64Variants.getDefaultVariant(), new ByteArrayOutputStream());
    }

    // ==================== _skipString with escape ====================
    @Test
    public void testSkipStringWithEscape() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"a\\nb\" 123");
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        // skip by moving to next token
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(123, parser.getIntValue());
        parser.close();
    }

    // ==================== _finishString2 with escape ====================
    @Test
    public void testFinishString2WithEscape() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"a\\tb\"");
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("a\tb", parser.getText());
        parser.close();
    }

    // ==================== _parseName2 with escape ====================
    @Test
    public void testParseName2WithEscape() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\\nb\":1}");
        parser.nextToken();
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a\nb", parser.getCurrentName());
        parser.close();
    }

    // ==================== _handleOddName2 with escape? Not applicable ====================

    // ==================== _decodeEscaped with unrecognized escape ====================
    @Test(expected = JsonParseException.class)
    public void testDecodeEscapedUnrecognized() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"\\x\"");
        parser.nextToken();
    }

    // ==================== _matchToken with EOF during token ====================
    @Test(expected = JsonParseException.class)
    public void testMatchTokenEOFDuring() throws IOException {
        ReaderBasedJsonParser parser = createParser("tru");
        parser.nextToken();
    }

    // ==================== _reportInvalidToken with EOF ====================
    @Test(expected = JsonParseException.class)
    public void testInvalidTokenEOF() throws IOException {
        ReaderBasedJsonParser parser = createParser("x");
        parser.nextToken();
    }

    // ==================== _handleOddValue with 'N' but not 'a' ====================
    @Test(expected = JsonParseException.class)
    public void testOddValueN() throws IOException {
        ReaderBasedJsonParser parser = createParser("Nx");
        parser.nextToken();
    }

    // ==================== _handleOddValue with 'I' but not 'n' ====================
    @Test(expected = JsonParseException.class)
    public void testOddValueI() throws IOException {
        ReaderBasedJsonParser parser = createParser("Ix");
        parser.nextToken();
    }

    // ==================== _skipWS with '/' but comment disabled ====================
    @Test(expected = JsonParseException.class)
    public void testSkipWSCommentDisabled() throws IOException {
        ReaderBasedJsonParser parser = createParser("/");
        parser.nextToken();
    }

    // ==================== _skipWSOrEnd with '/' but comment disabled ====================
    @Test(expected = JsonParseException.class)
    public void testSkipWSOrEndCommentDisabled() throws IOException {
        ReaderBasedJsonParser parser = createParser("/");
        parser.nextToken();
    }

    // ==================== _skipComment with '/' but not second char ====================
    @Test(expected = JsonParseException.class)
    public void testSkipCommentInvalidSecondChar() throws IOException {
        int features = featureMask(Feature.ALLOW_COMMENTS);
        ReaderBasedJsonParser parser = createParser("/! comment", features);
        parser.nextToken();
    }

    // ==================== _skipCComment with EOF before '*' ====================
    @Test(expected = JsonParseException.class)
    public void testBlockCommentEOFBeforeStar() throws IOException {
        int features = featureMask(Feature.ALLOW_COMMENTS);
        ReaderBasedJsonParser parser = createParser("/* comment", features);
        parser.nextToken();
    }

    // ==================== _skipCComment with EOF after '*' ====================
    @Test(expected = JsonParseException.class)
    public void testBlockCommentEOFAfterStar() throws IOException {
        int features = featureMask(Feature.ALLOW_COMMENTS);
        ReaderBasedJsonParser parser = createParser("/* *", features);
        parser.nextToken();
    }

    // ==================== _skipYAMLComment with feature disabled ====================
    @Test(expected = JsonParseException.class)
    public void testYAMLCommentDisabledInSkipWS() throws IOException {
        ReaderBasedJsonParser parser = createParser("# comment\n123");
        parser.nextToken();
    }

    // ==================== _skipLine with EOF ====================
    @Test
    public void testSkipLineEOF() throws IOException {
        int features = featureMask(Feature.ALLOW_COMMENTS);
        ReaderBasedJsonParser parser = createParser("// comment", features);
        // Should reach EOF without error
        assertNull(parser.nextToken());
        parser.close();
    }

    // ==================== _parseNumber with leading zero and non-digit after ====================
    @Test
    public void testLeadingZeroThenNonDigit() throws IOException {
        ReaderBasedJsonParser parser = createParser("0,");
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(0, parser.getIntValue());
        parser.close();
    }

    // ==================== _parseNumber with minus and EOF ====================
    @Test(expected = JsonParseException.class)
    public void testMinusEOF() throws IOException {
        ReaderBasedJsonParser parser = createParser("-");
        parser.nextToken();
    }

    // ==================== _parseNumber2 with minus and EOF ====================
    @Test(expected = JsonParseException.class)
    public void testMinusEOF2() throws IOException {
        String num = "-";
        Reader r = new Reader() {
            int pos = 0;
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                if (pos >= num.length()) return -1;
                cbuf[off] = num.charAt(pos++);
                return 1;
            }
            @Override
            public void close() throws IOException {}
        };
        ReaderBasedJsonParser parser = new ReaderBasedJsonParser(_ioContext, 0, r, null, _symbols);
        parser.nextToken();
    }

    // ==================== _parseNumber2 with leading zero and non-digit ====================
    @Test
    public void testLeadingZeroThenNonDigit2() throws IOException {
        String num = "0,";
        Reader r = new Reader() {
            int pos = 0;
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                if (pos >= num.length()) return -1;
                cbuf[off] = num.charAt(pos++);
                return 1;
            }
            @Override
            public void close() throws IOException {}
        };
        ReaderBasedJsonParser parser = new ReaderBasedJsonParser(_ioContext, 0, r, null, _symbols);
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(0, parser.getIntValue());
        parser.close();
    }

    // ==================== _verifyNoLeadingZeroes with multiple zeros ====================
    @Test
    public void testMultipleLeadingZerosAllowed() throws IOException {
        int features = featureMask(Feature.ALLOW_NUMERIC_LEADING_ZEROS);
        ReaderBasedJsonParser parser = createParser("000", features);
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(0, parser.getIntValue());
        parser.close();
    }

    @Test(expected = JsonParseException.class)
    public void testMultipleLeadingZerosDisallowed() throws IOException {
        ReaderBasedJsonParser parser = createParser("00");
        parser.nextToken();
    }

    // ==================== _handleInvalidNumberStart with 'I' then 'N' but feature disabled ====================
    @Test(expected = JsonParseException.class)
    public void testInvalidNumberStartINDisabled() throws IOException {
        ReaderBasedJsonParser parser = createParser("-INF");
        parser.nextToken();
    }

    // ==================== _handleInvalidNumberStart with 'I' then 'n' but feature disabled ====================
    @Test(expected = JsonParseException.class)
    public void testInvalidNumberStartInDisabled() throws IOException {
        ReaderBasedJsonParser parser = createParser("-Infinity");
        parser.nextToken();
    }

    // ==================== _handleOddValue with 'N' then 'a' but feature disabled
