package com.fasterxml.jackson.core.base;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.NumberInput;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.fasterxml.jackson.core.util.VersionUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Test suite for ParserMinimalBase
 */
public class ParserMinimalBaseTest {

    // We'll use a concrete subclass for testing
    private static class TestParser extends ParserMinimalBase {
        private String text;
        private int intVal;
        private long longVal;
        private double doubleVal;
        private Object embeddedObj;
        private String currentName;
        private boolean closed = false;
        private JsonStreamContext context = null; // minimal
        private Iterator<JsonToken> tokenIterator;
        private byte[] binaryValue;

        // Allow setting of protected fields directly via setters
        public void setCurrToken(JsonToken t) {
            _currToken = t;
        }
        public JsonToken getCurrToken() {
            return _currToken;
        }
        public void setLastClearedToken(JsonToken t) {
            _lastClearedToken = t;
        }
        public JsonToken getLastClearedTokenValue() {
            return _lastClearedToken;
        }
        
        // Override abstract methods
        @Override
        public JsonToken nextToken() throws IOException {
            if (tokenIterator != null && tokenIterator.hasNext()) {
                return tokenIterator.next();
            }
            return null;
        }
        
        public void setTokenIterator(Iterator<JsonToken> iter) {
            this.tokenIterator = iter;
        }
        
        @Override
        public String getText() throws IOException {
            return text;
        }
        public void setText(String s) { text = s; }
        
        @Override
        public char[] getTextCharacters() throws IOException {
            return (text == null) ? null : text.toCharArray();
        }
        
        @Override
        public boolean hasTextCharacters() {
            return text != null;
        }
        
        @Override
        public int getTextLength() throws IOException {
            return (text == null) ? 0 : text.length();
        }
        
        @Override
        public int getTextOffset() throws IOException {
            return 0;
        }
        
        @Override
        public String getCurrentName() throws IOException {
            return currentName;
        }
        
        @Override
        public void close() throws IOException {
            closed = true;
        }
        
        @Override
        public boolean isClosed() {
            return closed;
        }
        
        @Override
        public JsonStreamContext getParsingContext() {
            return context;
        }
        public void setParsingContext(JsonStreamContext ctx) { context = ctx; }
        
        @Override
        public byte[] getBinaryValue(Base64Variant b64variant) throws IOException {
            return binaryValue;
        }
        public void setBinaryValue(byte[] b) { binaryValue = b; }
        
        // Value as methods needed by getValueAs... (which call getIntValue etc)
        // We'll override these methods to return our stored values
        @Override
        public int getIntValue() throws IOException { return intVal; }
        @Override
        public long getLongValue() throws IOException { return longVal; }
        @Override
        public double getDoubleValue() throws IOException { return doubleVal; }
        @Override
        public Object getEmbeddedObject() throws IOException { return embeddedObj; }
        
        public void setIntValue(int v) { intVal = v; }
        public void setLongValue(long v) { longVal = v; }
        public void setDoubleValue(double v) { doubleVal = v; }
        public void setEmbeddedObject(Object o) { embeddedObj = o; }
        
        // Override _handleEOF to throw a specific exception for testing
        @Override
        protected void _handleEOF() throws JsonParseException {
            throw new JsonParseException(null, "TEST_EOF");
        }
        
        // Expose protected methods for testing
        public void call_reportUnexpectedChar(int ch, String comment) throws JsonParseException {
            _reportUnexpectedChar(ch, comment);
        }
        public void call_reportInvalidEOF() throws JsonParseException {
            _reportInvalidEOF();
        }
        public void call_reportInvalidEOF(String msg) throws JsonParseException {
            _reportInvalidEOF(msg);
        }
        public void call_reportInvalidEOFInValue() throws JsonParseException {
            _reportInvalidEOFInValue();
        }
        public void call_throwInvalidSpace(int i) throws JsonParseException {
            _throwInvalidSpace(i);
        }
        public void call_throwUnquotedSpace(int i, String ctxtDesc) throws JsonParseException {
            _throwUnquotedSpace(i, ctxtDesc);
        }
        public char call_handleUnrecognizedCharacterEscape(char ch) throws JsonProcessingException {
            return _handleUnrecognizedCharacterEscape(ch);
        }
        public void call_decodeBase64(String str, ByteArrayBuilder builder, Base64Variant b64variant) throws IOException {
            _decodeBase64(str, builder, b64variant);
        }
        public void call_reportInvalidBase64(Base64Variant b64variant, char ch, int bindex, String msg) throws JsonParseException {
            _reportInvalidBase64(b64variant, ch, bindex, msg);
        }
        public void call_reportBase64EOF() throws JsonParseException {
            _reportBase64EOF();
        }
        public boolean call_hasTextualNull(String value) {
            return _hasTextualNull(value);
        }
        
        // public wrapper for constructError (to test exception type)
        public JsonParseException call_constructError(String msg, Throwable t) {
            return _constructError(msg, t);
        }
        
        // public wrapper for _reportError
        public void call_reportError(String msg) throws JsonParseException {
            _reportError(msg);
        }
    }

    private TestParser parser;
    
    @Before
    public void setUp() {
        parser = new TestParser();
    }
    
    // ----------------------------------------------------------------------
    // Tests for token state methods
    // ----------------------------------------------------------------------
    
    @Test
    public void testGetCurrentTokenInitially() {
        assertNull(parser.getCurrentToken());
    }
    
    @Test
    public void testSetCurrentToken() {
        parser.setCurrToken(JsonToken.START_OBJECT);
        assertEquals(JsonToken.START_OBJECT, parser.getCurrentToken());
    }
    
    @Test
    public void testGetCurrentTokenIdNull() {
        parser.setCurrToken(null);
        assertEquals(JsonTokenId.ID_NO_TOKEN, parser.getCurrentTokenId());
    }
    
    @Test
    public void testGetCurrentTokenIdNonNull() {
        parser.setCurrToken(JsonToken.VALUE_TRUE);
        assertEquals(JsonToken.VALUE_TRUE.id(), parser.getCurrentTokenId());
    }
    
    @Test
    public void testHasCurrentToken() {
        assertFalse(parser.hasCurrentToken());
        parser.setCurrToken(JsonToken.FIELD_NAME);
        assertTrue(parser.hasCurrentToken());
    }
    
    @Test
    public void testHasTokenId() {
        // null token
        parser.setCurrToken(null);
        assertTrue(parser.hasTokenId(JsonTokenId.ID_NO_TOKEN));
        assertFalse(parser.hasTokenId(JsonTokenId.ID_STRING));
        
        // non-null token
        parser.setCurrToken(JsonToken.VALUE_NUMBER_INT);
        assertTrue(parser.hasTokenId(JsonToken.VALUE_NUMBER_INT.id()));
        assertFalse(parser.hasTokenId(JsonTokenId.ID_STRING));
    }
    
    @Test
    public void testHasToken() {
        parser.setCurrToken(null);
        assertFalse(parser.hasToken(JsonToken.VALUE_STRING));
        
        parser.setCurrToken(JsonToken.START_ARRAY);
        assertTrue(parser.hasToken(JsonToken.START_ARRAY));
        assertFalse(parser.hasToken(JsonToken.START_OBJECT));
    }
    
    @Test
    public void testIsExpectedStartArrayToken() {
        parser.setCurrToken(JsonToken.START_ARRAY);
        assertTrue(parser.isExpectedStartArrayToken());
        parser.setCurrToken(JsonToken.START_OBJECT);
        assertFalse(parser.isExpectedStartArrayToken());
    }
    
    @Test
    public void testIsExpectedStartObjectToken() {
        parser.setCurrToken(JsonToken.START_OBJECT);
        assertTrue(parser.isExpectedStartObjectToken());
        parser.setCurrToken(JsonToken.START_ARRAY);
        assertFalse(parser.isExpectedStartObjectToken());
    }
    
    // ----------------------------------------------------------------------
    // Tests for clearCurrentToken / getLastClearedToken
    // ----------------------------------------------------------------------
    
    @Test
    public void testClearCurrentTokenFromNull() {
        parser.setCurrToken(null);
        parser.setLastClearedToken(JsonToken.VALUE_STRING);
        parser.clearCurrentToken();
        assertNull(parser.getCurrToken());
        // lastClearedToken should remain unchanged
        assertEquals(JsonToken.VALUE_STRING, parser.getLastClearedTokenValue());
    }
    
    @Test
    public void testClearCurrentTokenFromNonNull() {
        parser.setCurrToken(JsonToken.START_ARRAY);
        parser.setLastClearedToken(JsonToken.VALUE_NULL);
        parser.clearCurrentToken();
        assertNull(parser.getCurrToken());
        assertEquals(JsonToken.START_ARRAY, parser.getLastClearedTokenValue());
    }
    
    @Test
    public void testClearCurrentTokenMultiple() {
        parser.setCurrToken(JsonToken.VALUE_TRUE);
        parser.clearCurrentToken();
        assertEquals(JsonToken.VALUE_TRUE, parser.getLastClearedToken());
        // clear again
        parser.clearCurrentToken();
        // _currToken is null, so lastClearedToken stays VALUE_TRUE
        assertEquals(JsonToken.VALUE_TRUE, parser.getLastClearedToken());
    }
    
    @Test
    public void testGetLastClearedTokenInitially() {
        // initially _lastClearedToken is null
        assertNull(parser.getLastClearedToken());
    }
    
    // ----------------------------------------------------------------------
    // Tests for nextValue()
    // ----------------------------------------------------------------------
    
    @Test
    public void testNextValueSimple() throws IOException {
        parser.setTokenIterator(Arrays.asList(JsonToken.VALUE_STRING).iterator());
        assertEquals(JsonToken.VALUE_STRING, parser.nextValue());
    }
    
    @Test
    public void testNextValueAfterFieldName() throws IOException {
        parser.setTokenIterator(Arrays.asList(JsonToken.FIELD_NAME, JsonToken.VALUE_NUMBER_INT).iterator());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextValue());
    }
    
    @Test
    public void testNextValueFieldNameOnly() throws IOException {
        parser.setTokenIterator(Arrays.asList(JsonToken.FIELD_NAME, null).iterator());
        assertNull(parser.nextValue());
    }
    
    // ----------------------------------------------------------------------
    // Tests for skipChildren()
    // ----------------------------------------------------------------------
    
    @Test
    public void testSkipChildrenNotStruct() throws IOException {
        parser.setCurrToken(JsonToken.VALUE_STRING);
        TestParser p = parser;
        assertSame(p, p.skipChildren()); // returns itself
    }
    
    @Test
    public void testSkipChildrenOnObject() throws IOException {
        parser.setCurrToken(JsonToken.START_OBJECT);
        // tokens: START_OBJECT then END_OBJECT
        parser.setTokenIterator(Arrays.asList(JsonToken.END_OBJECT).iterator());
        assertSame(parser, parser.skipChildren());
    }
    
    @Test
    public void testSkipChildrenNested() throws IOException {
        parser.setCurrToken(JsonToken.START_OBJECT);
        // tokens: VALUE_STRING, START_OBJECT, END_OBJECT, END_OBJECT
        parser.setTokenIterator(Arrays.asList(
                JsonToken.VALUE_STRING,
                JsonToken.START_OBJECT,
                JsonToken.END_OBJECT,                JsonToken.END_OBJECT        ).iterator());
        assertSame(parser, parser.skipChildren());
    }

    @Test(expected = JsonParseException.class)
    public void testSkipChildrenEOF() throws IOException {
        parser.setCurrToken(JsonToken.START_OBJECT);
        parser.setTokenIterator(Collections.<JsonToken>emptyList().iterator());
        parser.skipChildren(); // should call _handleEOF which throws
    }
    
    @Test(expected = JsonParseException.class)
    public void testSkipChildrenNullToken() throws IOException {
        parser.setCurrToken(JsonToken.START_ARRAY);
        parser.setTokenIterator(Arrays.asList((JsonToken)null).iterator());
        parser.skipChildren();
    }
    
    // ----------------------------------------------------------------------
    // Tests for getValueAsBoolean
    // ----------------------------------------------------------------------
    
    @Test
    public void testGetValueAsBooleanDefault() throws IOException {
        parser.setCurrToken(null);
        assertTrue(parser.getValueAsBoolean(true));
        assertFalse(parser.getValueAsBoolean(false));
    }
    
    @Test
    public void testGetValueAsBooleanFromTrueToken() throws IOException {
        parser.setCurrToken(JsonToken.VALUE_TRUE);
        assertTrue(parser.getValueAsBoolean(false));
    }
    
    @Test
    public void testGetValueAsBooleanFromFalseToken() throws IOException {
        parser.setCurrToken(JsonToken.VALUE_FALSE);
        assertFalse(parser.getValueAsBoolean(true));
    }
    
    @Test
    public void testGetValueAsBooleanFromNullToken() throws IOException {
        parser.setCurrToken(JsonToken.VALUE_NULL);
        assertFalse(parser.getValueAsBoolean(true));
    }
    
    @Test
    public void testGetValueAsBooleanFromNumberInt() throws IOException {
        parser.setCurrToken(JsonToken.VALUE_NUMBER_INT);
        parser.setIntValue(0);
        assertFalse(parser.getValueAsBoolean(true));
        parser.setIntValue(5);
        assertTrue(parser.getValueAsBoolean(false));
    }
    
    @Test
    public void testGetValueAsBooleanFromString() throws IOException {
        parser.setCurrToken(JsonToken.VALUE_STRING);
        // true
        parser.setText("true");
        assertTrue(parser.getValueAsBoolean(false));
        // false
        parser.setText("false");
        assertFalse(parser.getValueAsBoolean(true));
        // null
        parser.setText("null");
        assertFalse(parser.getValueAsBoolean(true));
        // other string -> default
        parser.setText("abc");
        assertTrue(parser.getValueAsBoolean(true));
        assertFalse(parser.getValueAsBoolean(false));
    }
    
    @Test
    public void testGetValueAsBooleanFromEmbeddedObject() throws IOException {
        parser.setCurrToken(JsonToken.VALUE_EMBEDDED_OBJECT);
        parser.setEmbeddedObject(Boolean.TRUE);
        assertTrue(parser.getValueAsBoolean(false));
        parser.setEmbeddedObject(Boolean.FALSE);
        assertFalse(parser.getValueAsBoolean(true));
        parser.setEmbeddedObject("not a boolean");
        // should fall through to default
        assertTrue(parser.getValueAsBoolean(true));
    }
    
    // ----------------------------------------------------------------------
    // Tests for getValueAsInt
    // ----------------------------------------------------------------------
    
    @Test
    public void testGetValueAsIntNoArg() throws IOException {
        // from VALUE_NUMBER_INT
        parser.setCurrToken(JsonToken.VALUE_NUMBER_INT);
        parser.setIntValue(42);
        assertEquals(42, parser.getValueAsInt());
        // from VALUE_NUMBER_FLOAT (truncation)
        parser.setCurrToken(JsonToken.VALUE_NUMBER_FLOAT);
        parser.setIntValue(3); // getIntValue returns 3
        assertEquals(3, parser.getValueAsInt());
        // from other, should call getValueAsInt(0)
        parser.setCurrToken(JsonToken.VALUE_STRING);
        parser.setText("hello");
        assertEquals(0, parser.getValueAsInt());
    }
    
    @Test
    public void testGetValueAsIntWithDefault() throws IOException {
        // null token
        parser.setCurrToken(null);
        assertEquals(99, parser.getValueAsInt(99));
        
        // NUMBER_INT
        parser.setCurrToken(JsonToken.VALUE_NUMBER_INT);
        parser.setIntValue(123);
        assertEquals(123, parser.getValueAsInt(0));
        
        // NUMBER_FLOAT
        parser.setCurrToken(JsonToken.VALUE_NUMBER_FLOAT);
        parser.setIntValue(5);
        assertEquals(5, parser.getValueAsInt(0));
        
        // STRING
        parser.setCurrToken(JsonToken.VALUE_STRING);
        parser.setText("37");
        assertEquals(37, parser.getValueAsInt(0));
        // null textual
        parser.setText("null");
        assertEquals(0, parser.getValueAsInt(-1));
        // invalid number -> uses defaultValue
        parser.setText("abc");
        assertEquals(-1, parser.getValueAsInt(-1));
        
        // TRUE -> 1
        parser.setCurrToken(JsonToken.VALUE_TRUE);
        assertEquals(1, parser.getValueAsInt(0));
        // FALSE -> 0
        parser.setCurrToken(JsonToken.VALUE_FALSE);
        assertEquals(0, parser.getValueAsInt(5));
        // NULL token -> 0
        parser.setCurrToken(JsonToken.VALUE_NULL);
        assertEquals(0, parser.getValueAsInt(7));
        
        // EMBEDDED_OBJECT Number
        parser.setCurrToken(JsonToken.VALUE_EMBEDDED_OBJECT);
        parser.setEmbeddedObject(Integer.valueOf(10));
        assertEquals(10, parser.getValueAsInt(0));
        parser.setEmbeddedObject(Double.valueOf(2.7));
        assertEquals(2, parser.getValueAsInt(0));
        parser.setEmbeddedObject("nonumber");
        // falls to default; but since embedded object not Number, uses defaultValue
        assertEquals(8, parser.getValueAsInt(8));
    }
    
    // ----------------------------------------------------------------------
    // Tests for getValueAsLong
    // ----------------------------------------------------------------------
    
    @Test
    public void testGetValueAsLongNoArg() throws IOException {
        parser.setCurrToken(JsonToken.VALUE_NUMBER_INT);
        parser.setLongValue(999L);
        assertEquals(999L, parser.getValueAsLong());
        parser.setCurrToken(JsonToken.VALUE_NUMBER_FLOAT);
        parser.setLongValue(888L);
        assertEquals(888L, parser.getValueAsLong());
        parser.setCurrToken(JsonToken.VALUE_STRING);
        parser.setText("x");
        assertEquals(0L, parser.getValueAsLong());
    }
    
    @Test
    public void testGetValueAsLongWithDefault() throws IOException {
        // null token
        parser.setCurrToken(null);
        assertEquals(100L, parser.getValueAsLong(100L));
        
        // NUMBER_INT
        parser.setCurrToken(JsonToken.VALUE_NUMBER_INT);
        parser.setLongValue(123L);
        assertEquals(123L, parser.getValueAsLong(0L));
        
        // NUMBER_FLOAT
        parser.setCurrToken(JsonToken.VALUE_NUMBER_FLOAT);
        parser.setLongValue(456L);
        assertEquals(456L, parser.getValueAsLong(0L));
        
        // STRING
        parser.setCurrToken(JsonToken.VALUE_STRING);
        parser.setText("789");
        assertEquals(789L, parser.getValueAsLong(0L));
        parser.setText("null");
        assertEquals(0L, parser.getValueAsLong(-1L));
        parser.setText("abc");
        assertEquals(-1L, parser.getValueAsLong(-1L));
        
        // TRUE -> 1L
        parser.setCurrToken(JsonToken.VALUE_TRUE);
        assertEquals(1L, parser.getValueAsLong(0L));
        // FALSE -> 0L
        parser.setCurrToken(JsonToken.VALUE_FALSE);
        assertEquals(0L, parser.getValueAsLong(5L));
        // NULL -> 0L
        parser.setCurrToken(JsonToken.VALUE_NULL);
        assertEquals(0L, parser.getValueAsLong(7L));
        
        // EMBEDDED_OBJECT
        parser.setCurrToken(JsonToken.VALUE_EMBEDDED_OBJECT);
        parser.setEmbeddedObject(Long.valueOf(111L));
        assertEquals(111L, parser.getValueAsLong(0L));
        parser.setEmbeddedObject(Integer.valueOf(222));
        assertEquals(222L, parser.getValueAsLong(0L));
        parser.setEmbeddedObject("nonumber");
        assertEquals(333L, parser.getValueAsLong(333L));
    }
    
    // ----------------------------------------------------------------------
    // Tests for getValueAsDouble
    // ----------------------------------------------------------------------
    
    @Test
    public void testGetValueAsDouble() throws IOException {
        // null token
        parser.setCurrToken(null);
        assertEquals(1.5, parser.getValueAsDouble(1.5), 0.0);
        
        // STRING
        parser.setCurrToken(JsonToken.VALUE_STRING);
        parser.setText("2.5");
        assertEquals(2.5, parser.getValueAsDouble(0.0), 0.0);
        parser.setText("null");
        assertEquals(0.0, parser.getValueAsDouble(1.0), 0.0);
        // invalid number uses defaultValue
        parser.setText("abc");
        assertEquals(1.0, parser.getValueAsDouble(1.0), 0.0);
        
        // NUMBER_INT / NUMBER_FLOAT use getDoubleValue
        parser.setCurrToken(JsonToken.VALUE_NUMBER_INT);
        parser.setDoubleValue(3.0);
        assertEquals(3.0, parser.getValueAsDouble(0.0), 0.0);
        parser.setCurrToken(JsonToken.VALUE_NUMBER_FLOAT);
        parser.setDoubleValue(4.0);
        assertEquals(4.0, parser.getValueAsDouble(0.0), 0.0);
        
        // TRUE -> 1.0
        parser.setCurrToken(JsonToken.VALUE_TRUE);
        assertEquals(1.0, parser.getValueAsDouble(0.0), 0.0);
        // FALSE -> 0.0
        parser.setCurrToken(JsonToken.VALUE_FALSE);
        assertEquals(0.0, parser.getValueAsDouble(1.0), 0.0);
        // NULL -> 0.0
        parser.setCurrToken(JsonToken.VALUE_NULL);
        assertEquals(0.0, parser.getValueAsDouble(1.0), 0.0);
        
        // EMBEDDED_OBJECT Number
        parser.setCurrToken(JsonToken.VALUE_EMBEDDED_OBJECT);
        parser.setEmbeddedObject(Double.valueOf(5.5));
        assertEquals(5.5, parser.getValueAsDouble(0.0), 0.0);
        parser.setEmbeddedObject(Integer.valueOf(6));
        assertEquals(6.0, parser.getValueAsDouble(0.0), 0.0);
        parser.setEmbeddedObject("nonumber");
        assertEquals(7.7, parser.getValueAsDouble(7.7), 0.0);
    }
    
    // ----------------------------------------------------------------------
    // Tests for getValueAsString
    // ----------------------------------------------------------------------
    
    @Test
    public void testGetValueAsStringNoDefault() throws IOException {
        parser.setCurrToken(JsonToken.VALUE_STRING);
        parser.setText("hello");
        assertEquals("hello", parser.getValueAsString());
        parser.setCurrToken(JsonToken.VALUE_NUMBER_INT);
        parser.setText("123");
        // should call getValueAsString(null) which uses getText()
        assertEquals("123", parser.getValueAsString());
    }
    
    @Test
    public void testGetValueAsStringWithDefault() throws IOException {
        // VALUE_STRING
        parser.setCurrToken(JsonToken.VALUE_STRING);
        parser.setText("world");
        assertEquals("world", parser.getValueAsString("default"));
        // null token
        parser.setCurrToken(null);
        assertEquals("default", parser.getValueAsString("default"));
        // VALUE_NULL
        parser.setCurrToken(JsonToken.VALUE_NULL);
        assertEquals("default", parser.getValueAsString("default"));
        // non-scalar token (e.g., START_OBJECT)
        parser.setCurrToken(JsonToken.START_OBJECT);
        assertEquals("default", parser.getValueAsString("default"));
        // scalar token (NUMBER_INT)
        parser.setCurrToken(JsonToken.VALUE_NUMBER_INT);
        parser.setText("987");
        assertEquals("987", parser.getValueAsString("default"));
    }
    
    // ----------------------------------------------------------------------
    // Tests for _getCharDesc
    // ----------------------------------------------------------------------
    @Test
    public void testGetCharDescControlChar() {
        String desc = ParserMinimalBase._getCharDesc(10); // \n
        assertTrue(desc.contains("CTRL-CHAR"));
        assertTrue(desc.contains("10"));
    }
    
    @Test
    public void testGetCharDescHighChar() {
        String desc = ParserMinimalBase._getCharDesc(256);
        assertTrue(desc.contains("'") && desc.contains("256"));
    }
    
    @Test
    public void testGetCharDescRegularChar() {
        String desc = ParserMinimalBase._getCharDesc('a');
        assertTrue(desc.contains("'"));
        assertTrue(desc.contains("code 97"));
    }
    
    // ----------------------------------------------------------------------
    // Tests for error reporting methods that throw exceptions
    // ----------------------------------------------------------------------
    
    @Test(expected = JsonParseException.class)
    public void testReportError() throws JsonParseException {
        parser.call_reportError("test error");
    }
    
    @Test
    public void testConstructError() {
        JsonLocation loc = parser.getCurrentLocation();
        Throwable cause = new IOException("cause");
        JsonParseException ex = parser.call_constructError("msg", cause);
        assertEquals("msg", ex.getOriginalMessage());
        assertSame(loc, ex.getLocation());
        assertSame(cause, ex.getCause());
    }
    
    @Test(expected = JsonParseException.class)
    public void testReportUnexpectedChar() throws JsonParseException {
        parser.call_reportUnexpectedChar(5, "comment");
    }
    
    @Test(expected = JsonParseException.class)
    public void testReportInvalidEOF() throws JsonParseException {
        parser.call_reportInvalidEOF();
    }
    
    @Test(expected = JsonParseException.class)
    public void testReportInvalidEOFWithMsg() throws JsonParseException {
        parser.call_reportInvalidEOF("details");
    }
    
    @Test(expected = JsonParseException.class)
    public void testReportInvalidEOFInValue() throws JsonParseException {
        parser.call_reportInvalidEOFInValue();
    }
    
    @Test(expected = JsonParseException.class)
    public void testThrowInvalidSpace() throws JsonParseException {
        parser.call_throwInvalidSpace('[');
    }
    
    @Test(expected = JsonParseException.class)
    public void testThrowUnquotedSpaceNormal() throws JsonParseException {
        // Without ALLOW_UNQUOTED_CONTROL_CHARS, throws
        parser.call_throwUnquotedSpace(0x20, "string");
    }
    
    @Test    public void testThrowUnquotedSpaceAllowedControlChar() throws JsonParseException {
        // Enable feature to suppress exception for control chars <= INT_SPACE
        parser.enableFeature(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS);
        // should not throw for space (0x20) or tab (0x09)
        parser.call_throwUnquotedSpace(0x09, "string");
        parser.call_throwUnquotedSpace(0x20, "string");
        // but for char > INT_SPACE, still throws
        try {
            parser.call_throwUnquotedSpace('a', "string");
            fail("Should have thrown");
        } catch (JsonParseException e) {
            // expected
        }
    }
    
    // ----------------------------------------------------------------------
    // Tests for _handleUnrecognizedCharacterEscape
    // ----------------------------------------------------------------------
    
    @Test
    public void testHandleUnrecognizedCharacterEscapeDefault() throws JsonProcessingException {
        // default: features disabled, should throw
        try {
            parser.call_handleUnrecognizedCharacterEscape('x');
            fail("Should throw");
        } catch (JsonParseException e) {
            // expected
        }
    }
    
    @Test
    public void testHandleUnrecognizedCharacterEscapeAllowBackslashAny() throws JsonProcessingException {
        parser.enableFeature(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER);
        assertEquals('x', parser.call_handleUnrecognizedCharacterEscape('x'));
    }
    
    @Test
    public void testHandleUnrecognizedCharacterEscapeSingleQuote() throws JsonProcessingException {
        parser.enableFeature(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        assertEquals('\'', parser.call_handleUnrecognizedCharacterEscape('\''));
    }
    
    // ----------------------------------------------------------------------
    // Tests for _decodeBase64
    // ----------------------------------------------------------------------
    
    @Test
    public void testDecodeBase64Valid() throws IOException {
        Base64Variant b64 = Base64Variants.MIME_NO_LINEFEEDS;
        ByteArrayBuilder builder = new ByteArrayBuilder(null);
        String encoded = "SGVsbG8="; // "Hello" in base64
        parser.call_decodeBase64(encoded, builder, b64);
        byte[] result = builder.toByteArray();
        assertArrayEquals("Hello".getBytes("US-ASCII"), result);
    }
    
    @Test(expected = JsonParseException.class)
    public void testDecodeBase64Invalid() throws IOException {
        Base64Variant b64 = Base64Variants.MIME_NO_LINEFEEDS;
        ByteArrayBuilder builder = new ByteArrayBuilder(null);
        parser.call_decodeBase64("!!!", builder, b64);
    }
    
    // ----------------------------------------------------------------------
    // Tests for _reportInvalidBase64 (deprecated)
    // ----------------------------------------------------------------------
    
    @Test(expected = JsonParseException.class)
    public void testReportInvalidBase64() throws JsonParseException {
        Base64Variant b64 = Base64Variants.MIME_NO_LINEFEEDS;
        // character 'a' at bindex 0 with msg
        parser.call_reportInvalidBase64(b64, 'a', 0, "extra");
    }
    
    @Test(expected = JsonParseException.class)
    public void testReportBase64EOF() throws JsonParseException {
        parser.call_reportBase64EOF();
    }
    
    // ----------------------------------------------------------------------
    // Tests for _hasTextualNull
    // ----------------------------------------------------------------------
    @Test
    public void testHasTextualNull() {
        TestParser p = parser;
        assertTrue(p.call_hasTextualNull("null"));
        assertFalse(p.call_hasTextualNull("NULL"));
        assertFalse(p.call_hasTextualNull(""));
        assertFalse(p.call_hasTextualNull("abc");
    }
    
    // ----------------------------------------------------------------------
    // Tests for _asciiBytes and _ascii
    // ----------------------------------------------------------------------
    @Test
    public void testAsciiBytes() {
        String str = "test";
        byte[] bytes = ParserMinimalBase._asciiBytes(str);
        assertEquals(4, bytes.length);
        assertArrayEquals(str.getBytes(), bytes);
    }
    
    @Test
    public void testAscii() {
        byte[] bytes = "test".getBytes();
        String result = ParserMinimalBase._ascii(bytes);
        assertEquals("test", result);
    }
    
 // ----------------------------------------------------------------------
    // Constructor test: with features
    // ----------------------------------------------------------------------
    @Test
    public void testConstructorWithFeatures() {
        TestParser p = new TestParser(JsonParser.Feature.AUTO_CLOSE_SOURCE.getMask());
        assertTrue(p.isEnabled(JsonParser.Feature.AUTO_CLOSE_SOURCE));
    }
    
    // Test that _handleEOF implementation actually throws when needed
    @Test(expected = JsonParseException.class)
    public void testHandleEOF() throws IOException {
        parser._handleEOF(); // directly
    }
}
