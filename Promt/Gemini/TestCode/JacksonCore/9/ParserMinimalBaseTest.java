package com.fasterxml.jackson.core.base;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;

public class ParserMinimalBaseTest {

    static class MinimalTestParser extends ParserMinimalBase {
        private final List<JsonToken> _tokens = new LinkedList<JsonToken>();
        private String _text;
        private Object _embeddedObject;
        private int _intValue;
        private long _longValue;
        private double _doubleValue;
        private boolean _closed;
        private String _currentName;

        public MinimalTestParser() {
            super();
        }

        public MinimalTestParser(int features) {
            super(features);
        }

        public void setTokens(JsonToken... tokens) {
            _tokens.clear();
            _tokens.addAll(Arrays.asList(tokens));
        }

        public void setCurrToken(JsonToken t) {
            this._currToken = t;
        }

        public void setText(String text) {
            this._text = text;
        }

        public void setEmbeddedObject(Object obj) {
            this._embeddedObject = obj;
        }

        public void setIntValue(int val) {
            this._intValue = val;
        }

        public void setLongValue(long val) {
            this._longValue = val;
        }

        public void setDoubleValue(double val) {
            this._doubleValue = val;
        }

        @Override
        public JsonToken nextToken() throws IOException {
            if (_tokens.isEmpty()) {
                _currToken = null;
            } else {
                _currToken = _tokens.remove(0);
            }
            return _currToken;
        }

        @Override
        protected void _handleEOF() throws JsonParseException {
            _reportInvalidEOF(" in test EOF");
        }

        @Override
        public String getCurrentName() throws IOException {
            return _currentName;
        }

        @Override
        public void close() throws IOException {
            _closed = true;
        }

        @Override
        public boolean isClosed() {
            return _closed;
        }

        @Override
        public JsonStreamContext getParsingContext() {
            return null;
        }

        @Override
        public void overrideCurrentName(String name) {
            _currentName = name;
        }

        @Override
        public String getText() throws IOException {
            return _text;
        }

        @Override
        public char[] getTextCharacters() throws IOException {
            return _text == null ? null : _text.toCharArray();
        }

        @Override
        public boolean hasTextCharacters() {
            return _text != null;
        }

        @Override
        public int getTextLength() throws IOException {
            return _text == null ? 0 : _text.length();
        }

        @Override
        public int getTextOffset() throws IOException {
            return 0;
        }

        @Override
        public byte[] getBinaryValue(Base64Variant b64variant) throws IOException {
            return null;
        }

        @Override
        public ObjectCodec getCodec() {
            return null;
        }

        @Override
        public void setCodec(ObjectCodec c) {
        }

        @Override
        public Version version() {
            return Version.unknownVersion();
        }

        @Override
        public JsonLocation getLocation() {
            return JsonLocation.NA;
        }

        @Override
        public JsonLocation getTokenLocation() {
            return JsonLocation.NA;
        }

        @Override
        public JsonLocation getCurrentLocation() {
            return JsonLocation.NA;
        }

        @Override
        public Object getEmbeddedObject() throws IOException {
            return _embeddedObject;
        }

        @Override
        public Number getNumberValue() throws IOException {
            return _intValue;
        }

        @Override
        public NumberType getNumberType() throws IOException {
            return NumberType.INT;
        }

        @Override
        public int getIntValue() throws IOException {
            return _intValue;
        }

        @Override
        public long getLongValue() throws IOException {
            return _longValue;
        }

        @Override
        public BigInteger getBigIntegerValue() throws IOException {
            return BigInteger.valueOf(_longValue);
        }

        @Override
        public float getFloatValue() throws IOException {
            return (float) _doubleValue;
        }

        @Override
        public double getDoubleValue() throws IOException {
            return _doubleValue;
        }

        @Override
        public BigDecimal getDecimalValue() throws IOException {
            return BigDecimal.valueOf(_doubleValue);
        }
    }

    @Test
    public void testTokenInspectionAndLifecycle() {
        MinimalTestParser p = new MinimalTestParser();
        Assert.assertNull(p.getCurrentToken());
        Assert.assertEquals(JsonTokenId.ID_NO_TOKEN, p.getCurrentTokenId());
        Assert.assertFalse(p.hasCurrentToken());
        Assert.assertTrue(p.hasTokenId(JsonTokenId.ID_NO_TOKEN));
        Assert.assertFalse(p.hasTokenId(JsonTokenId.ID_FIELD_NAME));
        Assert.assertTrue(p.hasToken(null));
        Assert.assertFalse(p.hasToken(JsonToken.START_OBJECT));
        Assert.assertFalse(p.isExpectedStartArrayToken());
        Assert.assertFalse(p.isExpectedStartObjectToken());

        p.setCurrToken(JsonToken.START_OBJECT);
        Assert.assertEquals(JsonToken.START_OBJECT, p.getCurrentToken());
        Assert.assertEquals(JsonTokenId.ID_START_OBJECT, p.getCurrentTokenId());
        Assert.assertTrue(p.hasCurrentToken());
        Assert.assertTrue(p.hasTokenId(JsonTokenId.ID_START_OBJECT));
        Assert.assertFalse(p.hasTokenId(JsonTokenId.ID_START_ARRAY));
        Assert.assertTrue(p.hasToken(JsonToken.START_OBJECT));
        Assert.assertFalse(p.hasToken(JsonToken.START_ARRAY));
        Assert.assertTrue(p.isExpectedStartObjectToken());
        Assert.assertFalse(p.isExpectedStartArrayToken());

        p.setCurrToken(JsonToken.START_ARRAY);
        Assert.assertTrue(p.isExpectedStartArrayToken());
        Assert.assertFalse(p.isExpectedStartObjectToken());

        p.clearCurrentToken();
        Assert.assertNull(p.getCurrentToken());
        Assert.assertEquals(JsonToken.START_ARRAY, p.getLastClearedToken());

        p.clearCurrentToken();
        Assert.assertEquals(JsonToken.START_ARRAY, p.getLastClearedToken());
    }

    @Test
    public void testNextValue() throws IOException {
        MinimalTestParser p = new MinimalTestParser();
        p.setTokens(JsonToken.START_OBJECT, JsonToken.FIELD_NAME, JsonToken.VALUE_STRING, JsonToken.END_OBJECT);

        Assert.assertEquals(JsonToken.START_OBJECT, p.nextValue());
        Assert.assertEquals(JsonToken.VALUE_STRING, p.nextValue());
        Assert.assertEquals(JsonToken.END_OBJECT, p.nextValue());
        Assert.assertNull(p.nextValue());
    }

    @Test
    public void testSkipChildren() throws IOException {
        MinimalTestParser p = new MinimalTestParser();

        p.setCurrToken(JsonToken.VALUE_STRING);
        Assert.assertSame(p, p.skipChildren());

        p.setCurrToken(JsonToken.START_OBJECT);
        p.setTokens(
                JsonToken.FIELD_NAME,
                JsonToken.START_ARRAY,
                JsonToken.VALUE_NUMBER_INT,
                JsonToken.END_ARRAY,
                JsonToken.END_OBJECT,
                JsonToken.VALUE_TRUE
        );
        Assert.assertSame(p, p.skipChildren());
        Assert.assertEquals(JsonToken.END_OBJECT, p.getCurrentToken());

        p.setCurrToken(JsonToken.START_ARRAY);
        p.setTokens(JsonToken.START_OBJECT, JsonToken.END_OBJECT);
        try {
            p.skipChildren();
            Assert.fail("Should have reached EOF and thrown JsonParseException");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("test EOF"));
        }
    }

    @Test
    public void testGetValueAsBoolean() throws IOException {
        MinimalTestParser p = new MinimalTestParser();

        Assert.assertTrue(p.getValueAsBoolean(true));
        Assert.assertFalse(p.getValueAsBoolean(false));

        p.setCurrToken(JsonToken.VALUE_TRUE);
        Assert.assertTrue(p.getValueAsBoolean(false));

        p.setCurrToken(JsonToken.VALUE_FALSE);
        Assert.assertFalse(p.getValueAsBoolean(true));

        p.setCurrToken(JsonToken.VALUE_NULL);
        Assert.assertFalse(p.getValueAsBoolean(true));

        p.setCurrToken(JsonToken.VALUE_NUMBER_INT);
        p.setIntValue(1);
        Assert.assertTrue(p.getValueAsBoolean(false));
        p.setIntValue(0);
        Assert.assertFalse(p.getValueAsBoolean(true));

        p.setCurrToken(JsonToken.VALUE_STRING);
        p.setText("true");
        Assert.assertTrue(p.getValueAsBoolean(false));
        p.setText("  true  ");
        Assert.assertTrue(p.getValueAsBoolean(false));
        p.setText("false");
        Assert.assertFalse(p.getValueAsBoolean(true));
        p.setText("null");
        Assert.assertFalse(p.getValueAsBoolean(true));
        p.setText("unknown");
        Assert.assertTrue(p.getValueAsBoolean(true));

        p.setCurrToken(JsonToken.VALUE_EMBEDDED_OBJECT);
        p.setEmbeddedObject(Boolean.TRUE);
        Assert.assertTrue(p.getValueAsBoolean(false));
        p.setEmbeddedObject(Boolean.FALSE);
        Assert.assertFalse(p.getValueAsBoolean(true));
        p.setEmbeddedObject("not_a_boolean");
        Assert.assertTrue(p.getValueAsBoolean(true));
    }

    @Test
    public void testGetValueAsInt() throws IOException {
        MinimalTestParser p = new MinimalTestParser();

        Assert.assertEquals(42, p.getValueAsInt(42));
        Assert.assertEquals(0, p.getValueAsInt());

        p.setCurrToken(JsonToken.VALUE_NUMBER_INT);
        p.setIntValue(123);
        Assert.assertEquals(123, p.getValueAsInt());
        Assert.assertEquals(123, p.getValueAsInt(99));

        p.setCurrToken(JsonToken.VALUE_NUMBER_FLOAT);
        p.setIntValue(456);
        Assert.assertEquals(456, p.getValueAsInt());
        Assert.assertEquals(456, p.getValueAsInt(99));

        p.setCurrToken(JsonToken.VALUE_TRUE);
        Assert.assertEquals(1, p.getValueAsInt(99));
        p.setCurrToken(JsonToken.VALUE_FALSE);
        Assert.assertEquals(0, p.getValueAsInt(99));
        p.setCurrToken(JsonToken.VALUE_NULL);
        Assert.assertEquals(0, p.getValueAsInt(99));

        p.setCurrToken(JsonToken.VALUE_STRING);
        p.setText("null");
        Assert.assertEquals(0, p.getValueAsInt(99));
        p.setText("789");
        Assert.assertEquals(789, p.getValueAsInt(99));
        p.setText("not_an_int");
        Assert.assertEquals(99, p.getValueAsInt(99));

        p.setCurrToken(JsonToken.VALUE_EMBEDDED_OBJECT);
        p.setEmbeddedObject(Long.valueOf(321L));
        Assert.assertEquals(321, p.getValueAsInt(99));
        p.setEmbeddedObject("abc");
        Assert.assertEquals(99, p.getValueAsInt(99));
    }

    @Test
    public void testGetValueAsLong() throws IOException {
        MinimalTestParser p = new MinimalTestParser();

        Assert.assertEquals(42L, p.getValueAsLong(42L));
        Assert.assertEquals(0L, p.getValueAsLong());

        p.setCurrToken(JsonToken.VALUE_NUMBER_INT);
        p.setLongValue(1234567890123L);
        Assert.assertEquals(1234567890123L, p.getValueAsLong());
        Assert.assertEquals(1234567890123L, p.getValueAsLong(99L));

        p.setCurrToken(JsonToken.VALUE_NUMBER_FLOAT);
        p.setLongValue(9876543210987L);
        Assert.assertEquals(9876543210987L, p.getValueAsLong());
        Assert.assertEquals(9876543210987L, p.getValueAsLong(99L));

        p.setCurrToken(JsonToken.VALUE_TRUE);
        Assert.assertEquals(1L, p.getValueAsLong(99L));
        p.setCurrToken(JsonToken.VALUE_FALSE);
        Assert.assertEquals(0L, p.getValueAsLong(99L));
        p.setCurrToken(JsonToken.VALUE_NULL);
        Assert.assertEquals(0L, p.getValueAsLong(99L));

        p.setCurrToken(JsonToken.VALUE_STRING);
        p.setText("null");
        Assert.assertEquals(0L, p.getValueAsLong(99L));
        p.setText("12345");
        Assert.assertEquals(12345L, p.getValueAsLong(99L));
        p.setText("invalid");
        Assert.assertEquals(99L, p.getValueAsLong(99L));

        p.setCurrToken(JsonToken.VALUE_EMBEDDED_OBJECT);
        p.setEmbeddedObject(Integer.valueOf(654));
        Assert.assertEquals(654L, p.getValueAsLong(99L));
        p.setEmbeddedObject("abc");
        Assert.assertEquals(99L, p.getValueAsLong(99L));
    }

    @Test
    public void testGetValueAsDouble() throws IOException {
        MinimalTestParser p = new MinimalTestParser();

        Assert.assertEquals(42.5, p.getValueAsDouble(42.5), 0.001);

        p.setCurrToken(JsonToken.VALUE_NUMBER_INT);
        p.setDoubleValue(123.0);
        Assert.assertEquals(123.0, p.getValueAsDouble(0.0), 0.001);

        p.setCurrToken(JsonToken.VALUE_NUMBER_FLOAT);
        p.setDoubleValue(456.78);
        Assert.assertEquals(456.78, p.getValueAsDouble(0.0), 0.001);

        p.setCurrToken(JsonToken.VALUE_TRUE);
        Assert.assertEquals(1.0, p.getValueAsDouble(0.0), 0.001);
        p.setCurrToken(JsonToken.VALUE_FALSE);
        Assert.assertEquals(0.0, p.getValueAsDouble(9.0), 0.001);
        p.setCurrToken(JsonToken.VALUE_NULL);
        Assert.assertEquals(0.0, p.getValueAsDouble(9.0), 0.001);

        p.setCurrToken(JsonToken.VALUE_STRING);
        p.setText("null");
        Assert.assertEquals(0.0, p.getValueAsDouble(9.0), 0.001);
        p.setText("12.34");
        Assert.assertEquals(12.34, p.getValueAsDouble(9.0), 0.001);
        p.setText("invalid");
        Assert.assertEquals(9.0, p.getValueAsDouble(9.0), 0.001);

        p.setCurrToken(JsonToken.VALUE_EMBEDDED_OBJECT);
        p.setEmbeddedObject(Float.valueOf(3.14f));
        Assert.assertEquals(3.14, p.getValueAsDouble(9.0), 0.01);
        p.setEmbeddedObject("abc");
        Assert.assertEquals(9.0, p.getValueAsDouble(9.0), 0.001);
    }

    @Test
    public void testGetValueAsString() throws IOException {
        MinimalTestParser p = new MinimalTestParser();

        Assert.assertNull(p.getValueAsString());
        Assert.assertEquals("def", p.getValueAsString("def"));

        p.setCurrToken(JsonToken.VALUE_STRING);
        p.setText("hello");
        Assert.assertEquals("hello", p.getValueAsString());
        Assert.assertEquals("hello", p.getValueAsString("def"));

        p.setCurrToken(JsonToken.VALUE_NULL);
        p.setText("null");
        Assert.assertEquals("def", p.getValueAsString("def"));

        p.setCurrToken(JsonToken.START_OBJECT);
        p.setText("{");
        Assert.assertEquals("def", p.getValueAsString("def"));

        p.setCurrToken(JsonToken.VALUE_NUMBER_INT);
        p.setText("123");
        Assert.assertEquals("123", p.getValueAsString());
        Assert.assertEquals("123", p.getValueAsString("def"));
    }

    @Test
    public void testBase64DecodingAndErrors() throws IOException {
        MinimalTestParser p = new MinimalTestParser();
        ByteArrayBuilder builder = new ByteArrayBuilder();

        p._decodeBase64("YWJj", builder, Base64Variants.MIME_NO_LINEFEEDS);
        byte[] bytes = builder.toByteArray();
        Assert.assertArrayEquals(new byte[] { 'a', 'b', 'c' }, bytes);

        try {
            p._decodeBase64("???", builder, Base64Variants.MIME_NO_LINEFEEDS);
            Assert.fail("Expected decode failure");
        } catch (JsonParseException e) {
            Assert.assertNotNull(e.getMessage());
        }

        try {
            p._reportBase64EOF();
            Assert.fail("Expected EOF failure");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("Unexpected end-of-String in base64 content"));
        }

        try {
            p._reportInvalidBase64(Base64Variants.MIME, ' ', 1, "test msg");
            Assert.fail();
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("Illegal white space character"));
            Assert.assertTrue(e.getMessage().contains("test msg"));
        }

        try {
            p._reportInvalidBase64(Base64Variants.MIME, '=', 0, null);
            Assert.fail();
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("Unexpected padding character"));
        }

        try {
            p._reportInvalidBase64(Base64Variants.MIME, '\u0007', 1, null);
            Assert.fail();
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("Illegal character (code 0x7)"));
        }

        try {
            p._reportInvalidBase64(Base64Variants.MIME, '?', 1, null);
            Assert.fail();
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("Illegal character '?'"));
        }
    }

    @Test
    public void testErrorReportingMethods() {
        MinimalTestParser p = new MinimalTestParser();

        try {
            p._reportUnexpectedChar(-1, "eof");
            Assert.fail();
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("Unexpected end-of-input"));
        }

        try {
            p._reportUnexpectedChar('x', null);
            Assert.fail();
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("Unexpected character ('x'"));
        }

        try {
            p._reportUnexpectedChar('y', "some comment");
            Assert.fail();
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("some comment"));
        }

        try {
            p._reportInvalidEOFInValue();
            Assert.fail();
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("in a value"));
        }

        try {
            p._reportMissingRootWS('z');
            Assert.fail();
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("Expected space separating root-level values"));
        }

        try {
            p._throwInvalidSpace('\u0001');
            Assert.fail();
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("Illegal character ((CTRL-CHAR, code 1))"));
        }

        try {
            p._wrapError("wrapped", new RuntimeException("cause"));
            Assert.fail();
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("wrapped"));
            Assert.assertNotNull(e.getCause());
        }

        try {
            p._throwInternal();
            Assert.fail();
        } catch (RuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("Internal error"));
        }
    }

    @Test
    public void testUnquotedSpaceHandling() throws Exception {
        MinimalTestParser p = new MinimalTestParser(0);
        try {
            p._throwUnquotedSpace('\t', "string value");
            Assert.fail();
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("Illegal unquoted character"));
        }

        MinimalTestParser p2 = new MinimalTestParser(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS.getMask());
        p2._throwUnquotedSpace('\t', "string value"); // Should not throw

        try {
            p2._throwUnquotedSpace(0x0021, "string value"); // > INT_SPACE (0x20)
            Assert.fail();
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("Illegal unquoted character"));
        }
    }

    @Test
    public void testCharacterEscapeHandling() throws Exception {
        MinimalTestParser p = new MinimalTestParser(0);
        try {
            p._handleUnrecognizedCharacterEscape('q');
            Assert.fail();
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("Unrecognized character escape 'q'"));
        }

        MinimalTestParser pBackslash = new MinimalTestParser(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER.getMask());
        Assert.assertEquals('q', pBackslash._handleUnrecognizedCharacterEscape('q'));

        MinimalTestParser pSingleQuote = new MinimalTestParser(JsonParser.Feature.ALLOW_SINGLE_QUOTES.getMask());
        Assert.assertEquals('\'', pSingleQuote._handleUnrecognizedCharacterEscape('\''));

        try {
            pSingleQuote._handleUnrecognizedCharacterEscape('x');
            Assert.fail();
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("Unrecognized character escape 'x'"));
        }
    }

    @Test
    public void testStaticHelperMethods() {
        Assert.assertEquals("(CTRL-CHAR, code 0)", ParserMinimalBase._getCharDesc(0));
        Assert.assertEquals("'a' (code 97)", ParserMinimalBase._getCharDesc('a'));
        Assert.assertEquals("'\u0100' (code 256 / 0x100)", ParserMinimalBase._getCharDesc(256));

        byte[] b = ParserMinimalBase._asciiBytes("Jackson");
        Assert.assertArrayEquals(new byte[] { 'J', 'a', 'c', 'k', 's', 'o', 'n' }, b);

        String s = ParserMinimalBase._ascii(b);
        Assert.assertEquals("Jackson", s);
    }
}
