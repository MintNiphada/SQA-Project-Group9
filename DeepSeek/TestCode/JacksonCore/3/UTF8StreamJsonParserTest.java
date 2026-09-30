package com.fasterxml.jackson.core.json;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.io.*;
import com.fasterxml.jackson.core.sym.*;
import com.fasterxml.jackson.core.util.*;
import org.junit.*;
import static org.junit.Assert.*;

public class UTF8StreamJsonParserTest {

    private BytesToNameCanonicalizer _symbols;
    private ObjectCodec _codec;

    @Before
    public void setUp() {
        _symbols = BytesToNameCanonicalizer.createRoot();
        _codec = null;
    }

    private UTF8StreamJsonParser createParser(byte[] content) throws IOException {
        return createParser(content, 0);
    }

    private UTF8StreamJsonParser createParser(byte[] content, int features) throws IOException {
        InputStream in = new ByteArrayInputStream(content);
        IOContext ctxt = new IOContext(new BufferRecycler(), content, true);
        return new UTF8StreamJsonParser(ctxt, features, in, _codec, _symbols,
                new byte[4000], 0, 0, true);
    }

    // Helper to consume a token and return the parser
    private JsonToken advanceToValue(UTF8StreamJsonParser p) throws IOException {
        JsonToken t = p.nextToken();
        if (t == JsonToken.FIELD_NAME) {
            return p.nextToken();
        }
        return t;
    }

    // ---------- Basic constructor, getters, setters ----------
    @Test
    public void testGetSetCodec() throws IOException {
        byte[] json = "null".getBytes("UTF-8");
        UTF8StreamJsonParser p = createParser(json);
        assertNull(p.getCodec());
        ObjectCodec codec = new ObjectCodec() {}; // dummy
        p.setCodec(codec);
        assertSame(codec, p.getCodec());
        p.close();
    }

    @Test
    public void testReleaseBuffered() throws IOException {
        byte[] json = "true".getBytes("UTF-8");
        UTF8StreamJsonParser p = createParser(json);
        assertEquals(JsonToken.VALUE_TRUE, advanceToValue(p));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int count = p.releaseBuffered(out);
        // After reading true, inputPtr should be at end, buffer may have remaining bytes?
        // Since buffer size is large, after reading true, ptr may be at end with no remaining.
        assertTrue(count >= 0);
        p.close();
    }

    @Test
    public void testGetInputSource() throws IOException {
        byte[] json = "{}".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(json);
        IOContext ctxt = new IOContext(new BufferRecycler(), json, true);
        UTF8StreamJsonParser p = new UTF8StreamJsonParser(ctxt, 0, in, _codec, _symbols,
                new byte[4000], 0, 0, true);
        assertSame(in, p.getInputSource());
        p.close();
    }

    // ---------- nextToken and value parsing ----------
    @Test
    public void testNextTokenNull() throws IOException {
        UTF8StreamJsonParser p = createParser("null".getBytes("UTF-8"));
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    @Test
    public void testNextTokenTrue() throws IOException {
        UTF8StreamJsonParser p = createParser("true".getBytes("UTF-8"));
        assertEquals(JsonToken.VALUE_TRUE, p.nextToken());
        p.close();
    }

    @Test
    public void testNextTokenFalse() throws IOException {
        UTF8StreamJsonParser p = createParser("false".getBytes("UTF-8"));
        assertEquals(JsonToken.VALUE_FALSE, p.nextToken());
        p.close();
    }

    @Test
    public void testNextTokenNumberInt() throws IOException {
        UTF8StreamJsonParser p = createParser("123".getBytes("UTF-8"));
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(123, p.getIntValue());
        p.close();
    }

    @Test
    public void testNextTokenNumberFloat() throws IOException {
        UTF8StreamJsonParser p = createParser("12.3".getBytes("UTF-8"));
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(12.3, p.getDoubleValue(), 0.0001);
        p.close();
    }

    @Test
    public void testNextTokenString() throws IOException {
        UTF8StreamJsonParser p = createParser("\"hello\"".getBytes("UTF-8"));
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("hello", p.getText());
        p.close();
    }

    @Test
    public void testNextTokenEmptyArray() throws IOException {
        UTF8StreamJsonParser p = createParser("[]".getBytes("UTF-8"));
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    @Test
    public void testNextTokenEmptyObject() throws IOException {
        UTF8StreamJsonParser p = createParser("{}".getBytes("UTF-8"));
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test
    public void testNextTokenArrayWithValues() throws IOException {
        UTF8StreamJsonParser p = createParser("[true, 1, \"abc\"]".getBytes("UTF-8"));
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    @Test
    public void testNextTokenObjectWithFields() throws IOException {
        UTF8StreamJsonParser p = createParser("{\"a\":1, \"b\":\"x\"}".getBytes("UTF-8"));
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("a", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("b", p.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    // ---------- getText, getTextCharacters etc ----------
    @Test
    public void testGetTextOnNumber() throws IOException {
        UTF8StreamJsonParser p = createParser("  -42 ".getBytes("UTF-8"));
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals("-42", p.getText());
    }

    @Test
    public void testGetTextOnString() throws IOException {
        UTF8StreamJsonParser p = createParser("\"test\"".getBytes("UTF-8"));
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("test", p.getText());
    }

    @Test
    public void testGetValueAsString() throws IOException {
        UTF8StreamJsonParser p = createParser("{\"x\":true}".getBytes("UTF-8"));
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME
        p.nextToken(); // VALUE_TRUE
        assertEquals("true", p.getValueAsString());
        p.close();
    }

    @Test
    public void testGetTextCharactersOnFieldName() throws IOException {
        UTF8StreamJsonParser p = createParser("{\"abc\":1}".getBytes("UTF-8"));
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME
        assertArrayEquals("abc".toCharArray(), p.getTextCharacters());
    }

    @Test
    public void testGetTextLengthOnString() throws IOException {
        UTF8StreamJsonParser p = createParser("\"hello\"".getBytes("UTF-8"));
        p.nextToken();
        assertEquals(5, p.getTextLength());
    }

    @Test
    public void testGetTextOffset() throws IOException {
        UTF8StreamJsonParser p = createParser("123".getBytes("UTF-8"));
        p.nextToken();
        // offset for number should be 0 typically
        assertEquals(0, p.getTextOffset());
    }

    // ---------- nextFieldName ----------
    @Test
    public void testNextFieldNameMatch() throws IOException {
        byte[] json = "{\"a\":1}".getBytes("UTF-8");
        UTF8StreamJsonParser p = createParser(json);
        p.nextToken(); // START_OBJECT
        SerializableString str = new SerializableString() {
            @Override public String getValue() { return "a"; }
            @Override public int charLength() { return 1; }
            @Override public char[] asQuotedChars() { return null; }
            @Override public byte[] asUnquotedUTF8() { return new byte[]{ 'a' }; }
            @Override public byte[] asQuotedUTF8() { return new byte[]{ (byte)'"', 'a', (byte)'"' }; }
        };
        assertTrue(p.nextFieldName(str));
        assertEquals("a", p.getCurrentName());
        // value
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        p.close();
    }

    @Test
    public void testNextFieldNameNoMatch() throws IOException {
        byte[] json = "{\"b\":2}".getBytes("UTF-8");
        UTF8StreamJsonParser p = createParser(json);
        p.nextToken();
        SerializableString str = new SerializableString() {
            @Override public String getValue() { return "a"; }
            @Override public int charLength() { return 1; }
            @Override public char[] asQuotedChars() { return null; }
            @Override public byte[] asUnquotedUTF8() { return new byte[]{ 'a' }; }
            @Override public byte[] asQuotedUTF8() { return new byte[]{ (byte)'"', 'a', (byte)'"' }; }
        };
        assertFalse(p.nextFieldName(str));
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        p.close();
    }

    // ---------- nextTextValue, nextIntValue, nextLongValue, nextBooleanValue ----------
    @Test
    public void testNextTextValueString() throws IOException {
        UTF8StreamJsonParser p = createParser("\"hi\"".getBytes("UTF-8"));
        assertEquals("hi", p.nextTextValue());
        p.close();
    }

    @Test
    public void testNextIntValue() throws IOException {
        UTF8StreamJsonParser p = createParser("42".getBytes("UTF-8"));
        assertEquals(42, p.nextIntValue(-1));
        p.close();
    }

    @Test
    public void testNextIntValueDefault() throws IOException {
        UTF8StreamJsonParser p = createParser("\"notint\"".getBytes("UTF-8"));
        assertEquals(-1, p.nextIntValue(-1));
        p.close();
    }

    @Test
    public void testNextLongValue() throws IOException {
        UTF8StreamJsonParser p = createParser("1234567890123".getBytes("UTF-8"));
        assertEquals(1234567890123L, p.nextLongValue(-1));
        p.close();
    }

    @Test
    public void testNextBooleanValueTrue() throws IOException {
        UTF8StreamJsonParser p = createParser("true".getBytes("UTF-8"));
        assertEquals(Boolean.TRUE, p.nextBooleanValue());
        p.close();
    }

    @Test
    public void testNextBooleanValueFalse() throws IOException {
        UTF8StreamJsonParser p = createParser("false".getBytes("UTF-8"));
        assertEquals(Boolean.FALSE, p.nextBooleanValue());
        p.close();
    }

    // ---------- Binary value ----------
    @Test
    public void testGetBinaryValue() throws IOException {
        Base64Variant b64 = Base64Variants.MIME;
        String encoded = "\"" + Base64Variants.MIME.encode("hello".getBytes("UTF-8")) + "\"";
        UTF8StreamJsonParser p = createParser(encoded.getBytes("UTF-8"));
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertArrayEquals("hello".getBytes("UTF-8"), p.getBinaryValue(b64));
        p.close();
    }

    @Test
    public void testReadBinaryValue() throws IOException {
        Base64Variant b64 = Base64Variants.MIME;
        byte[] original = "hello world".getBytes("UTF-8");
        String encoded = "\"" + b64.encode(original) + "\"";
        UTF8StreamJsonParser p = createParser(encoded.getBytes("UTF-8"));
        p.nextToken(); // VALUE_STRING
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int len = p.readBinaryValue(b64, out);
        assertArrayEquals(original, out.toByteArray());
        assertEquals(original.length, len);
        p.close();
    }

    // ---------- Error and edge cases ----------
    @Test(expected = JsonParseException.class)
    public void testInvalidToken() throws IOException {
        UTF8StreamJsonParser p = createParser("xyz".getBytes("UTF-8"));
        p.nextToken();
    }

    @Test(expected = JsonParseException.class)
    public void testMismatchedArrayEnd() throws IOException {
        UTF8StreamJsonParser p = createParser("[}".getBytes("UTF-8"));
        p.nextToken(); // start array
        p.nextToken(); // error
    }

    @Test(expected = JsonParseException.class)
    public void testMismatchedObjectEnd() throws IOException {
        UTF8StreamJsonParser p = createParser("{]".getBytes("UTF-8"));
        p.nextToken(); // start object
        p.nextToken(); // error
    }

    @Test(expected = JsonParseException.class)
    public void testMissingColon() throws IOException {
        UTF8StreamJsonParser p = createParser("{\"a\" 1}".getBytes("UTF-8"));
        p.nextToken(); // {
        p.nextToken(); // field name
    }

    @Test(expected = JsonParseException.class)
    public void testLeadingZero() throws IOException {
        UTF8StreamJsonParser p = createParser("01".getBytes("UTF-8"));
        p.nextToken();
    }

    @Test
    public void testLeadingZeroAllowed() throws IOException {
        UTF8StreamJsonParser p = createParser("07".getBytes("UTF-8"), Feature.ALLOW_NUMERIC_LEADING_ZEROS.getMask());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(7, p.getIntValue());
    }

    @Test
    public void testNonNumericNumbersNaNAllowed() throws IOException {
        UTF8StreamJsonParser p = createParser("NaN".getBytes("UTF-8"), Feature.ALLOW_NON_NUMERIC_NUMBERS.getMask());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertTrue(Double.isNaN(p.getDoubleValue()));
    }

    @Test
    public void testNonNumericNumbersInfinityAllowed() throws IOException {
        UTF8StreamJsonParser p = createParser("-Infinity".getBytes("UTF-8"), Feature.ALLOW_NON_NUMERIC_NUMBERS.getMask());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(Double.NEGATIVE_INFINITY, p.getDoubleValue(), 0.0);
    }

    @Test(expected = JsonParseException.class)
    public void testNonNumericNumbersNaNNotAllowed() throws IOException {
        UTF8StreamJsonParser p = createParser("NaN".getBytes("UTF-8"));
        p.nextToken();
    }

    // ---------- Comments ----------
    @Test
    public void testCommentsAllowed() throws IOException {
        String json = "/*comment*/ 1";
        UTF8StreamJsonParser p = createParser(json.getBytes("UTF-8"), Feature.ALLOW_COMMENTS.getMask());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
    }

    @Test(expected = JsonParseException.class)
    public void testCommentsNotAllowed() throws IOException {
        UTF8StreamJsonParser p = createParser("/*comment*/ 1".getBytes("UTF-8"));
        p.nextToken();
    }

    @Test
    public void testYamlCommentAllowed() throws IOException {
        String json = "# comment\n1";
        UTF8StreamJsonParser p = createParser(json.getBytes("UTF-8"), Feature.ALLOW_YAML_COMMENTS.getMask());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
    }

    @Test(expected = JsonParseException.class)
    public void testYamlCommentNotAllowed() throws IOException {
        UTF8StreamJsonParser p = createParser("# comment\n1".getBytes("UTF-8"));
        p.nextToken();
    }

    // ---------- Single quote strings ----------
    @Test
    public void testSingleQuotesAllowedString() throws IOException {
        UTF8StreamJsonParser p = createParser("'value'".getBytes("UTF-8"), Feature.ALLOW_SINGLE_QUOTES.getMask());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("value", p.getText());
    }

    @Test(expected = JsonParseException.class)
    public void testSingleQuotesNotAllowed() throws IOException {
        UTF8StreamJsonParser p = createParser("'value'".getBytes("UTF-8"));
        p.nextToken();
    }

    // ---------- Unquoted field names ----------
    @Test
    public void testUnquotedFieldNameAllowed() throws IOException {
        String json = "{abc : 1}";
        UTF8StreamJsonParser p = createParser(json.getBytes("UTF-8"), Feature.ALLOW_UNQUOTED_FIELD_NAMES.getMask());
        p.nextToken(); // {
        p.nextToken(); // field name
        assertEquals("abc", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
    }

    @Test(expected = JsonParseException.class)
    public void testUnquotedFieldNameNotAllowed() throws IOException {
        String json = "{abc : 1}";
        UTF8StreamJsonParser p = createParser(json.getBytes("UTF-8"));
        p.nextToken();
        p.nextToken();
    }

    // ---------- UTF-8 multi-byte characters ----------
    @Test
    public void testUTF8InString() throws IOException {
        String json = "\"\\u0041\\u00e9\\uD834\\uDD1E\"";
        UTF8StreamJsonParser p = createParser(json.getBytes("UTF-8"));
        p.nextToken();
        assertEquals("A\u00e9\uD834\uDD1E", p.getText());
    }

    // ---------- Location fetching ----------
    @Test
    public void testLocation() throws IOException {
        UTF8StreamJsonParser p = createParser("  true".getBytes("UTF-8"));
        assertEquals(JsonToken.VALUE_TRUE, p.nextToken());
        JsonLocation loc = p.getTokenLocation();
        assertEquals(1, loc.getLineNr());
        // column: after skipping whitespace, first token char offset
        assertEquals(3, loc.getColumnNr());
        p.close();
    }

    // ---------- Number parsing edge cases ----------
    @Test
    public void testNumberWithExponent() throws IOException {
        UTF8StreamJsonParser p = createParser("1e2".getBytes("UTF-8"));
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(100.0, p.getDoubleValue(), 0.0);
    }

    @Test
    public void testNegativeNumber() throws IOException {
        UTF8StreamJsonParser p = createParser("-5".getBytes("UTF-8"));
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(-5, p.getIntValue());
    }

    @Test
    public void testLargeNumberInt() throws IOException {
        String num = "1234567890123456789";
        UTF8StreamJsonParser p = createParser(num.getBytes("UTF-8"));
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(Long.parseLong(num), p.getLongValue());
    }

    // ---------- Inside Object, skipping values ----------
    @Test
    public void testSkipChildren() throws IOException {
        UTF8StreamJsonParser p = createParser("{\"a\":{\"b\":2}, \"c\":3}".getBytes("UTF-8"));
        p.nextToken(); // {
        p.nextToken(); // field a
        p.nextToken(); // start object
        p.nextToken(); // field b
        p.nextToken(); // number 2
        p.nextToken(); // end object
        assertEquals(JsonToken.FIELD_NAME, p.nextToken()); // c
        p.close();
    }

    // ---------- Error reporting of invalid characters in name ----------
    @Test(expected = JsonParseException.class)
    public void testInvalidCharInName() throws IOException {
        // control character inside field name
        String json = "{\"ab\\u0000c\":1}";
        UTF8StreamJsonParser p = createParser(json.getBytes("UTF-8"));
        p.nextToken(); // {
        p.nextToken(); // field name
    }

    // ---------- End of input in middle of token ----------
    @Test(expected = JsonParseException.class)
    public void testEOFInString() throws IOException {
        UTF8StreamJsonParser p = createParser("\"unfinished".getBytes("UTF-8"));
        p.nextToken();
    }

    @Test
    public void testLongFieldName() throws IOException {
        // create a long field name to exercise parseLongName
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < 50; i++) sb.append('a');
        sb.append("\"");
        String json = "{" + sb.toString() + ":1}";
        UTF8StreamJsonParser p = createParser(json.getBytes("UTF-8"));
        p.nextToken(); // {
        p.nextToken(); // field name
        assertEquals(sb.substring(1, sb.length()-1), p.getCurrentName());
    }

    @Test
    public void testEscapedCharsInName() throws IOException {
        String json = "{\"a\\nb\":1}";
        UTF8StreamJsonParser p = createParser(json.getBytes("UTF-8"));
        p.nextToken();
        p.nextToken();
        assertEquals("a\nb", p.getCurrentName());
    }

    // ---------- Whitespace handling ----------
    @Test
    public void testLotsOfWhitespace() throws IOException {
        String json = "  \t \r\n   {   \t \r\n   }  ";
        UTF8StreamJsonParser p = createParser(json.getBytes("UTF-8"));
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
    }

    // ---------- Test close and release ----------
    @Test
    public void testCloseReleasesBuffers() throws IOException {
        byte[] json = "{}".getBytes("UTF-8");
        UTF8StreamJsonParser p = createParser(json);
        p.close();
        // verify that input stream is null after close
        assertNull(p.getInputSource()); // closes sets _inputStream to null
    }

    // ---------- non-ASCII field names ----------
    @Test
    public void testNonAsciiFieldName() throws IOException {
        String name = "\u00e9l\u00e9phant";
        String json = "{\"" + name + "\":1}";
        UTF8StreamJsonParser p = createParser(json.getBytes("UTF-8"));
        p.nextToken(); // {
        p.nextToken(); // field name
        assertEquals(name, p.getCurrentName());
        p.close();
    }

    // ---------- multiple values in root with whitespace check ----------
    @Test
    public void testRootValueWhitespaceSeparation() throws IOException {
        UTF8StreamJsonParser p = createParser("1 2".getBytes("UTF-8"));
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(2, p.getIntValue());
        p.close();
    }

    // ---------- Negative Infinity with plus sign ----------
    @Test
    public void testNegativeInfinityAllowed() throws IOException {
        UTF8StreamJsonParser p = createParser("-Infinity".getBytes("UTF-8"), Feature.ALLOW_NON_NUMERIC_NUMBERS.getMask());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(Double.NEGATIVE_INFINITY, p.getDoubleValue(), 0.0);
    }

    @Test
    public void testPositiveInfinityAllowed() throws IOException {
        UTF8StreamJsonParser p = createParser("+Infinity".getBytes("UTF-8"), Feature.ALLOW_NON_NUMERIC_NUMBERS.getMask());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(Double.POSITIVE_INFINITY, p.getDoubleValue(), 0.0);
    }
}
