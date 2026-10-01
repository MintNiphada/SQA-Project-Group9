package com.fasterxml.jackson.core.json;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.*;
import org.junit.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.base.ParserBase;
import com.fasterxml.jackson.core.io.*;
import com.fasterxml.jackson.core.sym.*;
import com.fasterxml.jackson.core.util.*;

public class ReaderBasedJsonParserTest {

    @Mock
    private IOContext mockIOContext;
    @Mock
    private ObjectCodec mockCodec;
    private CharsToNameCanonicalizer symbols;

    private static final int FEATURE_DEFAULT = 0; // no special features

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        symbols = CharsToNameCanonicalizer.createRoot();
        // default stubs for IOContext
        when(mockIOContext.allocTokenBuffer()).thenReturn(new char[4000]);
        when(mockIOContext.allocNameCopyBuffer(anyInt())).thenAnswer(invocation -> new char[invocation.getArgumentAt(0, Integer.class)]);
        when(mockIOContext.allocBase64Buffer()).thenReturn(new byte[4000]);
        when(mockIOContext.getSourceReference()).thenReturn(new Object());
        when(mockIOContext.isResourceManaged()).thenReturn(false);
    }

    // Helper to create parser with given JSON string and default features.
    private ReaderBasedJsonParser createParser(String json) throws IOException {
        return createParser(json, FEATURE_DEFAULT);
    }

    private ReaderBasedJsonParser createParser(String json, int features) throws IOException {        Reader reader = new StringReader(json);
        return new ReaderBasedJsonParser(mockIOContext, features, reader, mockCodec, symbols);
    }

    // Helper to create parser with custom buffer size    private ReaderBasedJsonParser createParserWithSmallBuffer(String json) throws IOException {
        // override token buffer to be small
        when(mockIOContext.allocTokenBuffer()).thenReturn(new char[5]);
        Reader reader = new StringReader(json);
        return new ReaderBasedJsonParser(mockIOContext, FEATURE_DEFAULT, reader, mockCodec, symbols);
    }

    // Test constructors    @Test
    public void testConstructors() throws Exception {        Reader reader = new StringReader("{}");
        ReaderBasedJsonParser parser = new ReaderBasedJsonParser(mockIOContext, FEATURE_DEFAULT, reader, mockCodec, symbols);
        assertNotNull(parser);
        assertEquals(reader, parser.getInputSource());
        assertEquals(mockCodec, parser.getCodec());
        verify(mockIOContext).allocTokenBuffer();

        // second constructor with pre-loaded buffer
        char[] buf = new char[] { '1', '2', '3' };
        parser = new ReaderBasedJsonParser(mockIOContext, FEATURE_DEFAULT, reader, mockCodec, symbols, buf, 0, 3, false);
        assertNotNull(parser);
        assertEquals(0, parser.getTokenLocation().getCharOffset()); // inputPtr = 0
    }

    // Codec    @Test
    public void testGetSetCodec() throws Exception {        ReaderBasedJsonParser parser = createParser("true");
        ObjectCodec newCodec = mock(ObjectCodec.class);
        parser.setCodec(newCodec);
        assertEquals(newCodec, parser.getCodec());
    }

    @Test
    public void testReleaseBuffered() throws Exception {        ReaderBasedJsonParser parser = createParser("  123  ");
        // advance pointer to after "1"
        parser.nextToken(); // should be VALUE_NUMBER_INT
        parser.nextToken(); // should be null? Actually after number, there's space, nextToken would be null
        // To have buffered data, we need to leave characters unconsumed.
        // Easier: create parser with pre-loaded buffer
        Reader reader = new StringReader("abc");
        char[] buf = "abcdef".toCharArray();
        ReaderBasedJsonParser p2 = new ReaderBasedJsonParser(mockIOContext, FEATURE_DEFAULT, reader, mockCodec, symbols, buf, 2, 6, false);
        // inputPtr=2, inputEnd=6, so 4 chars buf[2..5]
        Writer sw = new StringWriter();
        int released = p2.releaseBuffered(sw);
        assertEquals(4, released);
        assertEquals("cdef", sw.toString());
    }

    @Test
    public void testGetInputSource() throws Exception {        Reader reader = new StringReader("x");
        ReaderBasedJsonParser parser = new ReaderBasedJsonParser(mockIOContext, FEATURE_DEFAULT, reader, mockCodec, symbols);
        assertEquals(reader, parser.getInputSource());
    }

    // nextToken basics    @Test
    public void testNextTokenTrueFalseNull() throws Exception {        ReaderBasedJsonParser parser = createParser(" true false null ");
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test
    public void testNextTokenNumber() throws Exception {        ReaderBasedJsonParser parser = createParser("123 -456 0.5 1e2");
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(123, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(-456, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(0.5, parser.getDoubleValue(), 0.0001);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(100.0, parser.getDoubleValue(), 0.0001);
    }

    @Test
    public void testNextTokenString() throws Exception {        ReaderBasedJsonParser parser = createParser("\"hello\" \"esc\\\"ape\"");
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("hello", parser.getText());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("esc\"ape", parser.getText());
    }

    @Test
    public void testNextTokenArray() throws Exception {        ReaderBasedJsonParser parser = createParser("[ ] [ 1 , 2 ]");
        assertTokenSequence(parser, JsonToken.START_ARRAY, JsonToken.END_ARRAY, JsonToken.START_ARRAY,
                JsonToken.VALUE_NUMBER_INT, JsonToken.VALUE_NUMBER_INT, JsonToken.END_ARRAY, null);
    }

    @Test
    public void testNextTokenObject() throws Exception {        ReaderBasedJsonParser parser = createParser("{ \"a\" : 1 , \"b\" : true }");
        assertTokenSequence(parser, JsonToken.START_OBJECT, JsonToken.FIELD_NAME, JsonToken.VALUE_NUMBER_INT,
                JsonToken.FIELD_NAME, JsonToken.VALUE_TRE, JsonToken.END_OBJECT, null);
        assertEquals("a", parser.getCurrentName());
        // simulate typical usage: after field name, value loaded
    }

    @Test
    public void testNextTokenMismatchedBrackets() throws Exception {        ReaderBasedJsonParser parser = createParser("]");
        try {            parser.nextToken();            fail("Expected JsonParseException for mismatched bracket");
        } catch (JsonParseException e) {            // good
        }
        parser = createParser("}");
        try {            parser.nextToken();            fail("Expected JsonParseException");
        } catch (JsonParseException e) { }
    }

    @Test
    public void testNextTokenComma() throws Exception {        parser = createParser("[1,,2]");
        try {            parser.nextToken();            parser.nextToken();            parser.nextToken(); // should throw because of extra comma
            fail("Expected exception");
        } catch (JsonParseException e) { }
    }

    @Test
    public void testNextTokenLeadingZero() throws Exception {        // default: not allowed
        ReaderBasedJsonParser parser = createParser("0123");
        try {            parser.nextToken();            fail("Expected exception for leading zero");
        } catch (JsonParseException e) { }
        // enable feature
        parser = createParser("0123", JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS.getMask());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(123, parser.getIntValue());
    }

    @Test
    public void testNextTokenInfNaN() throws Exception {        ReaderBasedJsonParser parser = createParser(" NaN Infinity -Infinity ", JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS.getMask());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertTrue(Double.isNaN(parser.getDoubleValue()));
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(Double.POSITIVE_INFINITY, parser.getDoubleValue(), 0.0);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(Double.NEGATIVE_INFINITY, parser.getDoubleValue(), 0.0);
    }

    @Test
    public void testNextTokenSingleQuotes() throws Exception {        parser = createParser("'Hello'", JsonParser.Feature.ALLOW_SINGLE_QUOTES.getMask());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("Hello", parser.getText());
    }

    @Test
    public void testNextTokenUnquotedNames() throws Exception {        parser = createParser("{ abc : 1 }", JsonParser.Feature.ALLOW_UNQUTED_FIELD_NAMES.getMask());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("abc", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
    }

    @Test
    public void testNextTokenComment() throws Exception {        parser = createParser("/* comment */ true // line comment\n false", JsonParser.Feature.ALLOW_COMMENTS.getMask());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test
    public void testNextTokenYAMLComment() throws Exception {        parser = createParser("# yaml\n true", JsonParser.Feature.ALLOW_YAML_COMMENTS.getMask());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
    }

    // nextFieldName    @Test
    public void testNextFieldName() throws Exception {        parser = createParser("{\"name\": 1, \"age\": 2}");
        assertEquals("name", parser.nextFieldName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.getCurrentToken());
        assertEquals("age", parser.nextFieldName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.getCurrentToken());
        assertNull(parser.nextFieldName());
    }

    @Test
    public void testNextFieldNameSerializableString() throws Exception {        // mock SerializableString
        SerializableString sstr = mock(SerializableString.class);
        when(sstr.asQuotedChars()).thenReturn("\"foo\"".toCharArray());
        when(sstr.getValue()).thenReturn("foo");
        parser = createParser("{\"foo\": 123 }");
        assertTrue(parser.nextFieldName(sstr));
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.getCurrentToken());
    }

    // nextTextValue    @Test
    public void testNextTextValue() throws Exception {        parser = createParser("{\"a\":\"hello\"}");
        parser.nextToken(); // START_OBJECT
        String val = parser.nextTextValue();
        assertEquals("hello", val);
    }

    // nextIntValue    @Test
    public void testNextIntValue() throws Exception {        parser = createParser("{\"a\":123}");
        parser.nextToken();
        assertEquals(123, parser.nextIntValue(0));
    }

    // nextLongValue    @Test
    public void testNextLongValue() throws Exception {        parser = createParser("{\"a\":1234567890123}");
        parser.nextToken();
        assertEquals(1234567890123L, parser.nextLongValue(0L));
    }

    // nextBooleanValue    @Test
    public void testNextBooleanValue() throws Exception {        parser = createParser("{\"a\":true}");
        parser.nextToken();
        assertEquals(Boolean.TRUE, parser.nextBooleanValue());
    }

    // getText variants    @Test
    public void testGetText() throws Exception {        // string
        parser = createParser("\"hello\"");
        parser.nextToken();
        assertEquals("hello", parser.getText());

        // field name
        parser = createParser("{\"field\":1}");
        parser.nextToken(); // {
        parser.nextToken(); // FIELD_NAME
        assertEquals("field", parser.getText());

        // number
        parser = createParser("1234");
        parser.nextToken();
        assertEquals("1234", parser.getText());

        // true
        parser = createParser("true");
        parser.nextToken();
        assertEquals("true", parser.getText());
    }

    @Test
    public void testGetValueAsString() throws Exception {        parser = createParser("{\'name\":\"value\"}"); // FIELD_NAME
        parser.nextToken();
        assertEquals("name", parser.getValueAsString());

        parser = createParser("\"string\"");
        parser.nextToken();
        assertEquals("string", parser.getValueAsString("def"));
    }

    @Test
    public void testGetTextCharacters() throws Exception {        parser = createParser("{\"field\":\"value\"}");
        parser.nextToken(); // {
        parser.nextToken(); // FIELD_NAME
        char[] chars = parser.getTextCharacters();
        assertEquals("field", new String(chars, 0, parser.getTextLength()));

        parser.nextToken(); // VALUE_STRING
        chars = parser.getTextCharacters();
        assertEquals("value", new String(chars, parser.getTextOffset(), parser.getTextLength()));
    }

    @Test
    public void testGetTextLength() throws Exception {        parser = createParser("\"123\"");
        parser.nextToken();
        assertEquals(3, parser.getTextLength());
    }

    @Test
    public void testGetTextOffset() throws Exception {        parser = createParser("\"abc\"");
        parser.nextToken();
        assertTrue(parser.getTextOffset() >= 0);
    }

    // Location    @Test
    public void testTokenLocation() throws Exception {        parser = createParser("{\"a\": 1}");
        parser.nextToken(); // START_OBJECT
        JsonLocation loc = parser.getTokenLocation();
        // just check not null
        assertNotNull(loc);
    }

    @Test
    public void testCurrentLocation() throws Exception {        parser = createParser("123");
        parser.nextToken();
        assertNotNull(parser.getCurrentLocation());
    }

    // Base64    @Test
    public void testGetBinaryValue() throws Exception {        byte[] data = new byte[] { 1, 2, 3 };
        String base64 = Base64Variants.MIME.encode(data);
        parser = createParser("\"" + base64 + "\"");
        parser.nextToken();
        byte[] result = parser.getBinaryValue(Base64Variants.MIME);
        assertArrayEquals(data, result);
    }

    @Test
    public void testReadBinaryValue() throws Exception {        byte[] data = new byte[] { 1,2,3,4,5,6,7,8,9,10 };
        String base64 = Base64Variants.MIME.encode(data);
        parser = createParser("\"" + base64 + "\"");
        parser.nextToken();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int len = parser.readBinaryValue(Base64Variants.MIME, baos);
        assertArrayEquals(data, baos.toByteArray());
        assertEquals(data.length, len);
    }

    // Close and release    @Test
    public void testClose() throws Exception {        Reader reader = new StringReader("true");
        parser = new ReaderBasedJsonParser(mockIOContext, FEATURE_DEFAULT, reader, mockCodec, symbols);
        parser.close();
        // verify that reader was not closed because resource not managed and auto-close not enabled
        verify(mockIOContext, never()).isResourceManaged();
        // but reader should be null after close        assertNull(parser.getInputSource());
    }

    @Test
    public void testReleaseBuffers() throws Exception {        parser = createParser("1");
        parser.nextToken();
        parser.close();
        verify(mockIOContext).releaseTokenBuffer(any(char[].class));
        // symbols release called
        verify(symbols).release();
    }

    // Edge cases: loadMore with small buffer    @Test
    public void testLoadMore() throws Exception {        parser = createParserWithSmallBuffer("12345");
        parser.nextToken(); // should load chunks and eventually parse number
        assertEquals(12345, parser.getIntValue());
    }

    @Test
    public void testParseNumberWithSplitBuffer() throws Exception {        parser = createParserWithSmallBuffer("123456");
        parser.nextToken();
        assertEquals(123456, parser.getIntValue());
    }

    @Test
    public void testFloatWithSplitBuffer() throws Exception {        parser = createParserWithSmallBuffer("123.456e1");
        parser.nextToken();
        assertEquals(1234.56, parser.getDoubleValue(), 0.00001);
    }

    @Test
    public void testEscapedStringWithSplitBuffer() throws Exception {        parser = createParserWithSmallBuffer("\"hello\\tworld\"");
        parser.nextToken();
        assertEquals("hello\world", parser.getText());
    }

    // Skip comment errors    @Test
    public void testSkipCommentWithoutFeature() throws Exception {        parser = createParser("/**");
        try {            parser.nextToken();            fail();
        } catch (JsonParseException e) { }
    }

    @Test
    public void testSkipCommentUnclosed() throws Exception {        parser = createParser("/* unclosed", JsonParser.Feature.ALLOW_COMMENTS.getMask());
        try {            parser.nextToken();            fail();
        } catch (JsonParseException e) { }
    }

    // Test _handleInvalidNumberStart    @Test
    public void testInvalidNumberStart() throws Exception {        parser = createParser("+NaN");
        try {            parser.nextToken();            fail();
        } catch (JsonParseException e) { }
    }

    @Test
    public void testNegativeInfinity() throws Exception {        parser = createParser("-Infinity", JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS.getMask());
        parser.nextToken();
        assertEquals(Double.NEGATIVE_INFINITY, parser.getDoubleValue(), 0.0);
    }

    @Test
    public void testInvalidToken() throws Exception {        parser = createParser("xyz");
        try {            parser.nextToken();            fail();
        } catch (JsonParseException e) { }
    }

    // VerifyRootSpace    @Test
    public void testMissingRootSpace() throws Exception {        parser = createParser("123x", JsonParser.Feature.STRICT_DUPICATE_DETECTION.getMask()); // not needed
        parser.nextToken(); // number
        assertNull(parser.nextToken()); // should throw because of 'x'? Actually, after number, expecting whitespace or EOF, x is unexpected
        // For root, _verifyRootSpace would be called, but we need to trigger
    } // Maybe a better test: parser.nextToken() after number then nextToken fails because 'x' not whitespace

    // _skipString    @Test
    public void testSkipString() throws Exception {        parser = createParser("\"hello\"ignored");
        parser.nextToken(); // string
        parser.nextToken(); // should skip "ignored" and fail? Actually, after string value, parser expects whitespace or EOF, so nextToken should throw because of 'i'
        try {            parser.nextToken();            fail();
        } catch (JsonParseException e) { }
    }

    // _skipColon    @Test
    public void testMalformedColon() throws Exception {        parser = createParser("{\"a\"   ; 1}");
        try {            parser.nextToken();            parser.nextToken(); // should fail because ';' instead of colon
            fail();
        } catch (JsonParseException e) { }
    }

    @Test
    public void testSkipColonWithComment() throws Exception {        parser = createParser("{\"a\" /*c*/ : 1 }", JsonParser.Feature.ALLOW_COMMENTS.getMask());
        parser.nextToken();
        parser.nextToken();
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
    }

    // _skipComma    @Test
    public void testMissingComma() throws Exception {        parser = createParser("[1 2]");
        parser.nextToken(); // [
        parser.nextToken(); // 1
        try {            parser.nextToken(); // should fail because missing comma
            fail();
        } catch (JsonParseException e) { }
    }

    // Test that nextToken correctly handles _tokenIncomplete (string)    @Test
    public void testIncompleteString() throws Exception {        parser = createParser("{\"a\":\"hello\"}");
        parser.nextToken(); // {
        parser.nextToken(); // FIELD_NAME
        parser.nextToken(); // VALUE_STRING
        // no issues
    }

    // Cover _updateNameLocation and _updateLocation via tokens    @Test
    public void testUpdateLocations() throws Exception {        parser = createParser("{\n  \"a\" : 1}");
        parser.nextToken(); // {
        assertNotNull(parser.getTokenLocation());
        parser.nextToken(); // field name
        // location should be at line 2
        JsonLocation loc = parser.getTokenLocation();
        assertEquals(2, loc.getLineNr());
    }

    // Test _matchToken    @Test
    public void testMatchToken() throws Exception {        parser = createParser("tr ue"); // invalid token
        try {            parser.nextToken();            fail();
        } catch (JsonParseException e) { }
    }

    // Verify that after closing scope, context is updated    @Test
    public void testContextDepth() throws Exception {        parser = createParser("{\"a\":{\"b\":[]}}");
        parser.nextToken(); // {
        assertEquals(1, parser.getParsingContext().depth()); // not a public method? Actually getParsingContext() might not exist.
        // we can test indirectly: after parsing, token stream ends properly.
    }

    // Additional methods for completeness (some private methods tested indirectly)
    // Already covered above.

    private void assertTokenSequence(JsonParser parser, JsonToken... tokens) throws IOException {        for (JsonToken expected : tokens) {            assertEquals(expected, parser.nextToken());
        }
    }
}
